/* $Header: /repository/src3/src/com/emis/schedule/emisSchSchemaChecker.java,v 1.1.1.1 2005/10/14 12:42:33 andy Exp $
 *
 * 2004/4/17 Jerry: 以排程程式來記錄每天的schema變動
 *           1.1    1.比對SysColumns與dba_Columns, 將不同的欄位寫入dba_log
 *                  2.最後將dba_columns同步成目前的SysColumns, 供下次比對之用
 *
 * 2004/06/01 Jerry 1.2: FMO無法載入(ClassNotFound), 必須有不傳參數的Constructor才可以.
 */
package com.emis.schedule;

import com.emis.db.emisDb;
import com.emis.db.emisRowSet;
import com.emis.qa.emisServletContext;
import com.emis.schedule.emisTask;
import com.emis.server.emisServerFactory;
import com.emis.util.emisDate;

import javax.servlet.ServletContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2004/4/17
 * Time: 下午 03:11:12
 * To change this template use Options | File Templates.
 */
public class emisSchSchemaChecker extends emisTask {
  private PreparedStatement oStmtInsertDbaSchema_;
  private PreparedStatement oStmtUpdateDbaSchema_;
  private PreparedStatement oStmtQueryDbaSchema_;
  private PreparedStatement oStmtQuerySysColumns_;
  private PreparedStatement oStmtQueryOldColumns_;
  private PreparedStatement oStmtQueryOldColumnsNotExists_;
  private PreparedStatement oStmtInsertLog_;

  public emisSchSchemaChecker() {
    super();
  }

  public emisSchSchemaChecker(ServletContext oContext) {
    oContext_ = oContext;
  }

  public void runTask() throws Exception {  // 改寫java.lang.Runnable.run()
    emisDb _oDb = null;
    try {
      _oDb = emisDb.getInstance(oContext_);

      prepareStatement(_oDb);  // 產生需要的PreparedStatement

      String _sSQL = "select Object_name(id) TableName, id, name, crdate, schema_ver from Sysobjects where XTYPE='U' and uid=1";
      _oDb.prepareStmt(_sSQL);
      _oDb.prepareQuery();
      emisRowSet _oTables = new emisRowSet(_oDb);

      int _iCount = 0;

      for (int i = 1; i <= _oTables.size(); i++) {
        _oTables.absolute(i);
        String _sID = _oTables.getString("id");
        String _sTableName = _oTables.getString("TableName");
        String _sName = _oTables.getString("name");
        Date _oCrDate = _oTables.getTimestamp("crdate");
        String _sCurrentVersion = _oTables.getString("schema_ver");

//        if ("test".equals(_sName)) {
//          int k=3;
//        }

        //System.out.println("TableName=" + _sTableName);

        _oDb.clearParameters();
        _oDb.setCurrentPrepareStmt(oStmtQueryDbaSchema_);
        _oDb.setString(1, _sName);
        _oDb.prepareQuery();
        if (_oDb.next()) {
          String _sVersion = _oDb.getString("schema_ver");
          //System.out.println(_sVersion.equals(_sCurrentVersion) + " version=" + _sVersion + "$ curr=" + _sCurrentVersion+"$");
          if (!_sVersion.equals(_sCurrentVersion)) {
            //System.out.println("name=" + _sName + " ver=" + _sCurrentVersion);
            int _iFields = compare(_sID, _sTableName, _oDb);
            if (_iFields > 0) {
              updateDbaSchema(_sTableName, _sVersion, _oDb);
            }
            _iCount += _iFields;
          }
        } else {
          int _iFields = insertDbaSchema(_sTableName, _oCrDate, _sCurrentVersion, _oDb);
          _iCount += _iFields;
        }
      }
      //if (_iCount > 0) {
        // 找出被刪除的table
        delete(_oDb);
        dropDbaSchema(_oDb);
      //}
    } catch (Exception e) {
      System.out.println("" + e.getMessage());
    } finally {
      if (_oDb != null) {
        _oDb.close();
      }
    }
  }

  /**
   * 重新同步dba_Columns與SysColumns.
   * @param _oDb
   * @throws SQLException
   */
  private void dropDbaSchema(emisDb _oDb) throws SQLException {
    _oDb.prepareStmt("drop table Dba_columns");
    _oDb.prepareUpdate();
    _oDb.prepareStmt(" select * into dba_columns from SysColumns");
    _oDb.prepareUpdate();
  }

  /**
   * 將被刪除掉的Table寫dba_log中.
   * @param _oDb
   * @throws Exception
   */
  private void delete(emisDb _oDb) throws Exception {
    _oDb.prepareStmt("select * from dba_schema where not exists "+
        "(select * from sysobjects where xtype='U' and uid = 1 "+
        "and dba_schema.name=sysobjects.name)");
    _oDb.prepareQuery();
    emisRowSet _oRS = new emisRowSet(_oDb);
    _oRS.first();
    int _iCount = 0;
    while (_oRS.next()) {
      String _sName = _oRS.getString("NAME");
      deleteDbaSchema(_sName, _oDb);
      _iCount++;
    }
    if (_iCount > 0) {
      _oDb.prepareStmt("delete from dba_schema where not exists "+
          "(select * from sysobjects where xtype='U' and uid = 1 "+
          "and dba_schema.name=sysobjects.name)");
      _oDb.prepareUpdate();
    }
  }

  /**
   * 產生需要的SQL敘述.
   * @param _oDb
   * @throws SQLException
   */
  private void prepareStatement(emisDb _oDb) throws SQLException {
    String _sSQL = "select * from dba_schema where name=?";
    oStmtQueryDbaSchema_ = _oDb.prepareStmt(_sSQL);

    _sSQL = "insert into Dba_schema(name,status,cre_date,schema_ver,[desc]) " +
        " values (?,?,?,?,?)";
    oStmtInsertDbaSchema_ = _oDb.prepareStmt(_sSQL);

    _sSQL = "update Dba_schema set SCHEMA_VER=?,STATUS=? where NAME=?";
    oStmtUpdateDbaSchema_ = _oDb.prepareStmt(_sSQL);

    _sSQL = "select Object_name(c.id) as TableName,c.name,c.length,c.xprec," +
        "  c.xscale, t.NAME type from SysColumns c " +
        "  left join SysTypes t on t.xtype=c.xtype " +
        "where c.id=? order by c.COLID";
    oStmtQuerySysColumns_ = _oDb.prepareStmt(_sSQL);

    // dba_columns由sysColumns整個複製
    _sSQL = "select Object_name(c.id) as TableName,c.name,c.length,c.xprec," +
        "  c.xscale, t.NAME type from dba_Columns c " +
        "  left join SysTypes t on t.xtype=c.xtype " +
        "where c.id=? and c.name=?";
    oStmtQueryOldColumns_ = _oDb.prepareStmt(_sSQL);

    _sSQL = "select NAME from Dba_Columns a where not exists " +
        "  (select NAME from SysColumns b where b.name=a.name)";
    oStmtQueryOldColumnsNotExists_ = _oDb.prepareStmt(_sSQL);

    _sSQL = "insert into dba_log(table_name,column_name,column_type,length,xprec,xscale,upd_date,log_message)" +
        " values (?,?,?,?,?,?,?,?)";
    oStmtInsertLog_ = _oDb.prepareStmt(_sSQL);
  }

  /**
   * 由SysObjects找出有被變動過的Table.原有Table的版本記錄在dba_schema中.
   * @param sID
   * @param sTableName
   * @param oDb
   * @return
   * @throws Exception
   */
  private int compare(String sID, String sTableName, emisDb oDb) throws Exception {
    if (sTableName.startsWith("dba_")) {
      return 0;
    }
    oDb.setCurrentPrepareStmt(oStmtQuerySysColumns_);
    oDb.setString(1, sID);
    oDb.prepareQuery();
    emisRowSet _oRS = new emisRowSet(oDb);
    //_oRS.show(new PrintWriter(System.out));
    _oRS.first();
    int _iCount = 0;

    while (_oRS.next()) {//for (int i = 0; i < _oRS.size(); i++) {
      //_oRS.absolute(i);
      String _sName = _oRS.getString("name");
      String _sType = _oRS.getString("type");
      String _sLength = _oRS.getString("length");
      String _sPrec = _oRS.getString("xprec");
      String _sScale = _oRS.getString("xscale");
      String _sMsg = "";
      //System.out.println("  name=" + _sName);

      oDb.setCurrentPrepareStmt(oStmtQueryOldColumns_);
      oDb.clearParameters();
      oDb.setString(1, sID);
      oDb.setString(2, _sName);
      oDb.prepareQuery();
      if (oDb.next()) {
        String _sTypeOld = oDb.getString("type");
        String _sLengthOld = oDb.getString("length");
        String _sPrecOld = oDb.getString("xprec");
        String _sScaleOld = oDb.getString("xscale");
        if (!_sTypeOld.equals(_sType)) {
          _sMsg += " 型別變更：" + _sTypeOld + " => " + _sType;
        }
        if (!_sLengthOld.equals(_sLength)) {
          _sMsg += " 長度變更：" + _sLengthOld + " => " + _sLength;
        }
        if (!_sPrecOld.equals(_sPrec)) {
          _sMsg += " 精準度變更：" + _sPrecOld + " => " + _sPrec;
        }
        if (!_sScaleOld.equals(_sScale)) {
          _sMsg += " 小數點位數變更：" + _sScaleOld + " => " + _sScale;
        }
      } else {
        _sMsg += "新增欄位：欄名=" + _sName + " 型別=" + _sType + " 長度=" + _sLength +
            " 精準度=" + _sPrec + " 小數點位數=" + _sScale;
      }

      if (_sMsg.length() > 0) {
        _iCount++;
        oDb.setCurrentPrepareStmt(oStmtInsertLog_);
        oDb.clearParameters();
        oDb.setString(1, sTableName);
        oDb.setString(2, _sName);  // Column_name
        oDb.setString(3, _sType);
        oDb.setString(4, _sLength);
        oDb.setString(5, _sPrec);
        oDb.setString(6, _sScale);
        emisDate _oDate = new emisDate();
        oDb.setString(7, _oDate.toString(true));
        oDb.setString(8, _sMsg);
        oDb.prepareUpdate();
      }
    }

    oDb.setCurrentPrepareStmt(oStmtQueryOldColumnsNotExists_);
    oDb.prepareQuery();
    _oRS = new emisRowSet(oDb);
    //_oRS.show(new PrintWriter(System.out));
    _oRS.first();
    _iCount = 0;
    String _sMsg = "";
    while (_oRS.next()) {//for (int i = 0; i < _oRS.size(); i++) {
      //_oRS.absolute(i);
      String _sName = _oRS.getString("name");
      _sMsg = "刪除欄位：欄名=" + _sName;

      oDb.setCurrentPrepareStmt(oStmtInsertLog_);
      oDb.clearParameters();
      oDb.clearParameters();
      oDb.setString(1, sTableName);
      oDb.setString(2, _sName);  // Column_name
      oDb.setString(3, "");
      oDb.setString(4, "");
      oDb.setString(5, "");
      oDb.setString(6, "");
      emisDate _oDate = new emisDate();
      oDb.setString(7, _oDate.toString(true));
      oDb.setString(8, _sMsg);
      oDb.prepareUpdate();
    }
    return _iCount;
  }

  /**
   * 將新增的Table寫入dba_log.
   * @param sName
   * @param oDate
   * @param sVersion
   * @param oDb
   * @return
   * @throws Exception
   */
  private int insertDbaSchema(String sName, Date oDate, String sVersion, emisDb oDb) throws Exception {
    oDb.setCurrentPrepareStmt(oStmtInsertDbaSchema_);
    oDb.clearParameters();
    oDb.setString(1, sName);
    oDb.setString(2, "A");  // Column_name
    oDb.setString(3, oDate.toString());
    oDb.setString(4, sVersion);
    oDb.setString(5, "新增資料表");
    oDb.prepareUpdate();

    oDb.setCurrentPrepareStmt(oStmtInsertLog_);
    oDb.clearParameters();
    oDb.setString(1, sName);
    oDb.setString(2, "");  // Column_name
    oDb.setString(3, "");
    oDb.setString(4, "");
    oDb.setString(5, "");
    oDb.setString(6, "");
    emisDate _oDate = new emisDate();
    oDb.setString(7, _oDate.toString(true));
    oDb.setString(8, "新增資料表");
    oDb.prepareUpdate();

    return 1;
  }

  /**
   * 將異動的Table寫入dba_log.
   * @param sName
   * @param sVersion
   * @param oDb
   * @return
   * @throws Exception
   */
  private int updateDbaSchema(String sName, String sVersion, emisDb oDb) throws Exception {
    oDb.setCurrentPrepareStmt(oStmtUpdateDbaSchema_);
    oDb.clearParameters();
    oDb.setString(1, sVersion);
    oDb.setString(2, "U");
    oDb.setString(3, sName);
    oDb.prepareUpdate();

    return 1;
  }

  /**
   * 將刪除的Table寫入dba_log.
   * @param sName
   * @param oDb
   * @return
   * @throws Exception
   */
  private int deleteDbaSchema(String sName, emisDb oDb) throws Exception {
    oDb.setCurrentPrepareStmt(oStmtInsertDbaSchema_);
    oDb.clearParameters();
    oDb.setString(1, sName);
    oDb.setString(2, "D");  // Column_name
    emisDate _oDate = new emisDate(true);
    oDb.setString(3, _oDate.toString());
    oDb.setString(4, _oDate.toString());
    oDb.setString(5, "刪除資料表");
    oDb.prepareUpdate();

    oDb.setCurrentPrepareStmt(oStmtInsertLog_);
    oDb.clearParameters();
    oDb.setString(1, sName);
    oDb.setString(2, "");  // Column_name
    oDb.setString(3, "");
    oDb.setString(4, "");
    oDb.setString(5, "");
    oDb.setString(6, "");
    oDb.setString(7, _oDate.toString(true));
    oDb.setString(8, "刪除資料表");
    oDb.prepareUpdate();

    return 1;
  }

  public static void main(String[] args) throws Exception {
    emisServletContext _oContext = new emisServletContext();
    emisServerFactory.createServer(_oContext, "c:\\wwwroot\\eros", "c:\\resin\\eros.cfg", true);
    emisSchSchemaChecker obj = new emisSchSchemaChecker(_oContext);
    obj.run();
  }
}
