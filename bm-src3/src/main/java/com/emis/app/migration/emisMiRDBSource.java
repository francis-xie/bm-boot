package com.emis.app.migration;

import com.emis.db.emisDb;
import com.emis.util.emisUtil;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * The <code>emisMiRDBSource</code> class is a SQL data source handler.
 *
 * It executes the Query statements, reads from the resultset and turns the record
 * into a one-dimension String array for furthur process.
 * $Id: emisMiRDBSource.java 4 2015-05-27 08:13:47Z andy.he $
 * @author  Merlin Yeh
 * @version 1.2, 03/17/2005
 * @since   eross
 * Track+[13540] dana.gao 2009/09/27 根據前端勾選門市,將門市資料傳入sql中(排程不會傳)以提高效能
 */
public final class emisMiRDBSource extends emisMiSource {
  final static int DELETE_WHEN_OK = 1;
  final static int UPDATE_WHEN_OK = 2;
  String OKAct;
  String sFlagTable_;
  String FlagField[];
  private PreparedStatement OKStmt_;
  int OKActType;
  private String sSQL;
  private ResultSet rs;
  private emisDb dbSrc_;
  private String sQrySQL_;
  String[] flags = null;
  private String[] sSQLParams_=null;


  private void actOKUpdate(final String[] data) throws SQLException {
    dbSrc_.setCurrentPrepareStmt(this.OKStmt_);
    dbSrc_.clearParameters();
    for (int i = 0; i < FlagField.length; i++) {
      dbSrc_.setString(i + 1, flags[i]);
    }
    dbSrc_.prepareUpdate();
    if (data == null)
      return;
    for (int i = 0; i < FlagField.length; i++) {
      flags[i] = data[config.sourceFieldIndex(FlagField[i])];
    }
  }


  public final void actOK(final String[] data) throws SQLException {
    if (OKStmt_ == null)
      return;
    switch (OKActType) {
      case DELETE_WHEN_OK:
        dbSrc_.setCurrentPrepareStmt(this.OKStmt_);
        dbSrc_.clearParameters();
        for (int i = 0; i < FlagField.length; i++) {
          dbSrc_.setString(i + 1, data[config.sourceFieldIndex(FlagField[i])]);
        }
        dbSrc_.prepareUpdate();
        //dbSrc_.commit();  //robert, we are in auto-commit mode, why need commit ?
        break;
      case UPDATE_WHEN_OK:
        {
          if (flags == null) {
            flags = new String[FlagField.length];
          }
          if (data == null) {
            actOKUpdate(data);
            return;
          }
          for (int i = 0; i < FlagField.length; i++) {
            try {
              if (!data[config.sourceFieldIndex(FlagField[i])].equals(flags[i])) {
                actOKUpdate(data);
                return;
              }
            } catch (java.lang.NullPointerException e) {
              // Log here;
            }
          }
        }
    }
  }

  public final boolean open(final emisMiConfig config) throws SQLException {
    this.config = config;
    // 可指定从哪个DB取资料
    if(this.dbName != null && !"".equals(dbName)) {
      try {
        dbSrc_ = emisDb.getInstance(config.getContext(),dbName);
      } catch (Exception e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
    } else {
      dbSrc_ = config.getDb();
    }
    replaceSQLParams();
    sQrySQL_ = addSnoCond(config,sQrySQL_); //將sql中的"@@@@@"替換為門市條件(排程替換為(1=1));
     // robert , we should use default , no need to set this
    //dbSrc_.prepareStmt(sQrySQL_,  ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    dbSrc_.prepareStmt(sQrySQL_);
    //  [4308] by Merlin 避免受到 updateFlag異動而影響ResultSet. 
//    setSQLParams();
    rs = dbSrc_.prepareQuery();
    if (config.getMigration().getTargetStore() == null) {
      // 手動下傳 Targetstore 不等於null不清除異動flag
      if (OKAct != null && sFlagTable_ != null && FlagField != null && FlagField.length > 0) {
        sSQL = "";
        if (OKAct.equals("DELETE")) {
          OKActType = DELETE_WHEN_OK;
          sSQL = "delete from " + sFlagTable_ + " where " + FlagField[0] + "=?";
          for (int i = 1; i < FlagField.length; i++) {
            sSQL = sSQL + " AND " + FlagField[i] + "=?";
          }
        } else if (OKAct.substring(0, 3).toUpperCase().equals("SET")) {
          OKActType = UPDATE_WHEN_OK;
          sSQL = "update " + sFlagTable_ + " " + OKAct + " where " + FlagField[0] + "=?";
          for (int i = 1; i < FlagField.length; i++) {
            sSQL = sSQL + " AND " + FlagField[i] + "=?";
          }
        }
        flags = new String[FlagField.length];
        for (int i = 0; i < FlagField.length; i++) {
          flags[i] = null;
        }
        OKStmt_ = this.dbSrc_.prepareStmt(sSQL);
      }
    }
    result = new String[config.getSourceFields().length];
    return true;
  }

  //Track+[13540] dana.gao 2009/09/27 根據前端勾選門市,將門市資料傳入sql中(排程不會傳)以提高效能
  private String addSnoCond(emisMiConfig config, String sSql) {
    String finalSql = "";
    emisMigration migration = config.getMigration();

    String sStore[] = config.getMigration().getTargetStore();
    String sCond = "";
    if (sStore != null) {
      for (int i = 0; i < sStore.length; i++) {
        sCond += ",'" + sStore[i] + "'";
      }
      sCond = " S_NO in (" + sCond.replaceFirst(",", "") + ")";
    } else {
      sCond = "(1=1)";
    }
    finalSql = sSql.replaceAll("[@@@@@]{5}", sCond);

    return finalSql;
  }

  /**
   *
   */
    private void replaceSQLParams() throws SQLException {
      if (sSQLParams_ != null) {
        for (int i = 0; i < this.sSQLParams_.length; i++) {
          String param = sSQLParams_[i];
          boolean addSingleQuotes = true;
          if (param.startsWith("@")) {
             param = param.substring(1);
             addSingleQuotes = false;
          }

          //String attrib = (String) config.getContext().getAttribute(param);
          String attrib = null;
          try{  //2005/05/13 andy:放在try中,當為背景排程自動執行時如下一行會拋出異常
            //2005/05/12 andy:修改為取user的設定.
            attrib = (String) config.getMigration().getUser().getAttribute(param);
            System.out.println("ATRIB["+attrib+"]");
          } catch(Exception e){ }
          if (addSingleQuotes
              //andy:加入如下的條件
              && attrib != null
              && attrib.indexOf(" in ") < 0 && attrib.indexOf("=") < 0
              && attrib.indexOf(">") < 0 && attrib.indexOf("<") < 0 ) {
            attrib = "'" + attrib + "'";
          }
          if (attrib == null) {
            attrib = " 1 = 1 " ;   //2005/05/12 andy:WTN客製
             //throw new SQLException("ServeletContext attribute ["+sSQLParams_[i]+"] does not exist!");
          }
          sQrySQL_ = emisUtil.stringReplace(sQrySQL_, "?", attrib, "");        }
      }
    }


/**
 *
  * @throws SQLException
 */
  private void setSQLParams() throws SQLException {
    if (sSQLParams_ != null) {
      for (int i = 0; i < this.sSQLParams_.length; i++) {
        String attrib = (String) config.getContext().getAttribute( sSQLParams_[i]);
        if (attrib == null)
           throw new SQLException("ServeletContext attribute ["+sSQLParams_[i]+"] does not exist!");
        dbSrc_.setString(i+1, attrib);
      }
    }
  }

  public final String[] next() throws SQLException {
    dbSrc_.setCurrentResultSet(rs);
    if (dbSrc_.next()) {
      for (int i = 0; i < result.length; i++) {
        final String s = dbSrc_.getString(config.getSourceFields()[i].getName());
        result[i] = (s == null) ? "" : s;
      }
      return result;
    }
    return null;
  }

  public final boolean close(final boolean closeDb) {
    if(this.dbName != null && !"".equals(dbName)){
      if(this.dbSrc_ != null){
        this.dbSrc_.close();
        this.dbSrc_ = null;
      }
    }
    return false;
  }

  public final boolean backup() {
    return false;
  }

  final void parse(final Hashtable h) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ClassNotFoundException {
    super.parse(h);
    final Iterator it = h.keySet().iterator();
    String key;
    String val;
    while (it.hasNext()) {
      key = (String) it.next();
      val = (String) h.get(key);
      if (key.equalsIgnoreCase("SQL")) {
        sQrySQL_ = val;
      } else if (key.equalsIgnoreCase("FLagTable")) {
        sFlagTable_ = val;
      } else if (key.equalsIgnoreCase("OKAct")) {
        OKAct = val; //.toUpperCase();
      } else if (key.equalsIgnoreCase("FlagField")) {
        FlagField = emisMiField.parseStr(val);
      } else if (key.equalsIgnoreCase("Params")) {
        sSQLParams_ = emisMiField.parseStr(val);
      } else if(key.equalsIgnoreCase("dbname")){
        dbName = val;
      }
    }
  }
}
