package com.emis.schedule.trans;

import com.emis.db.*;
import com.emis.schedule.*;
import com.emis.file.*;
import com.emis.util.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.io.*;

public class emisDbTransfer extends emisTask 
{
  private PrintWriter out_ ;
  private emisTransferDesc oDesc_ = new emisTransferDesc();

  public void runTask() throws Exception {
    boolean hasError=false;
//    emisTracer oTr = emisTracer.get(oContext_);
    try {
//      oTr.info("TRANS RUN");
      doJob();
    } catch (Exception e ) {
      hasError = true;
      oDesc_.setException(e);
//      oTr.info("TRANS ERROR");    } finally {
//      oTr.info("TRANS SET END");      oDesc_.setEnd();
    }
  }
  public void doJob() throws Exception
  {
    emisFileMgr _oMgr = emisFileMgr.getInstance(oContext_);
    emisDirectory _logDir = _oMgr.getDirectory("root").subDirectory("data").subDirectory("trans");
    emisFile _log = _logDir.getFile(emisUtil.todayDate()+".log");
    out_ = _log.getWriter(null);
    try {
      emisDb oSysDb = emisDb.getInstance(oContext_); //default
      try {
        transferDb(oSysDb,oRequest_);
      } catch (Exception e) {
        e.printStackTrace(out_);
        throw e;
      } finally {
        oSysDb.close();
      }
    } finally {
      out_.close();
    }
  }

  private void transferDb(emisDb oSysDb,HttpServletRequest oRequest) throws Exception
  {
    oSysDb.executeQuery("SELECT * FROM TRANS_H WHERE IS_TRANS='Y' or IS_TRANS='1'");

    String _sGlobalSourceDb = oRequest.getParameter("F_DB");
    emisRowSet oTransH = new emisRowSet( oSysDb );
    oTransH.first();
    while( oTransH.next() ) {
      String _sSourceDb = null;

      if( _sGlobalSourceDb == null ) {
        _sSourceDb = oTransH.getString("F_DB");
      } else {
        _sSourceDb =_sGlobalSourceDb;
      }

      String _sDestDb   = oTransH.getString("T_DB");
      out_.println();
      out_.println("來源資料庫:"+_sSourceDb);
      out_.println("目的資料庫:"+_sDestDb );
      out_.println("開始時間:"+emisUtil.todayDate()+":"+emisUtil.todayTime());
      oDesc_.setDb(_sSourceDb,_sDestDb);

      emisDb oSource = null;
      if((_sSourceDb == null) || "".equals(_sSourceDb) ) {
        oSource = emisDb.getInstance(oContext_);
      } else {
        oSource = emisDb.getInstance(oContext_,_sSourceDb);
      }
      try {
        emisDb oDest = null;
        if( (_sDestDb == null) || "".equals(_sDestDb) ) {
          oDest = emisDb.getInstance(oContext_);
        } else {
          oDest = emisDb.getInstance(oContext_,_sDestDb);
        }
        try {
          oDest.setAutoCommit(false);
          transferOneTable(oTransH,oSysDb,oSource,oDest,oRequest);
        } catch (Exception e) {
          out_.println("錯誤:");
          e.printStackTrace(out_);
          oDest.rollback();
          throw e;
        } finally {
          oDest.close();
        }
      } finally {
        oSource.close();
      }
    }

  }
  private void transferOneTable(emisRowSet oTransH,emisDb oSysDb,emisDb oSource,emisDb oDest,HttpServletRequest oRequest) throws Exception
  {
      String sSourceTable = oTransH.getString("F_TABLE");
      String sDestTable   = oTransH.getString("T_TABLE");
      out_.println();
      out_.println("來源TABLE:"+sSourceTable);
      out_.println("目的TABLE:"+sDestTable);

      oDesc_.setTable(sSourceTable,sDestTable);

      //transform the update SQL , including update and insert

      // make unique key mapping
      ArrayList oSourcePkColumn=new ArrayList();
      ArrayList oDestPkColumn=new ArrayList();
      HashMap oColumnPkMap = new HashMap();

      String _sFromKey = oTransH.getString("F_KEY");
      String _sToKey   = oTransH.getString("T_KEY");
      if( (_sFromKey== null ) || ("".equals(_sFromKey)) ) {
        throw new Exception("沒有設定 F_KEY:"+sSourceTable);
      }
      if( (_sToKey== null ) || ("".equals(_sToKey)) ) {
        throw new Exception("沒有設定 T_KEY:"+sDestTable);
      }

      StringTokenizer SourceTk = new StringTokenizer(_sFromKey,",");
      StringTokenizer DestTt = new StringTokenizer  (_sToKey  ,",");
      while (SourceTk.hasMoreTokens() ) {
        String _sSrc = SourceTk.nextToken();
        String _sDest = DestTt.nextToken();
        _sSrc = _sSrc.toUpperCase().trim();
        _sDest = _sDest.toUpperCase().trim();
        oSourcePkColumn.add(_sSrc);
        oDestPkColumn.add(_sDest);
        oColumnPkMap.put(_sDest,_sSrc);
      }


      // this param is used to set up source SQL parameters
      ArrayList oParam = null;
      String _sParam = oTransH.getString("TRANS_PARAM");
      if( (_sParam != null) && (!"".equals(_sParam)) ) {
        oParam = new ArrayList();
        StringTokenizer st = new StringTokenizer(_sParam,",");
        while ( st.hasMoreTokens() ) {
          oParam.add(st.nextToken());
        }
      }


      // execute the Source SQL
      String _sSQL = oTransH.getString("TRANS_SQL");
      _sSQL = _sSQL.toUpperCase();
      // transfer SQL
      if( oParam != null ) {
        for(int i=0; i < oParam.size(); i++) {
          int idx = _sSQL.indexOf("?") ;
          if( idx != -1 ) {
            String _sParamName = (String) oParam.get(i);
            String _sParameter = oRequest.getParameter(_sParamName);
            _sSQL = _sSQL.substring(0,idx) +"'"+_sParameter+"'"+ _sSQL.substring(idx+1);
          }
        }
      }
      out_.println("EXEC SQL:"+_sSQL);
      oDesc_.setMsg("執行查詢中....");
      oSource.executeQuery(_sSQL);

      oSysDb.prepareStmt("SELECT * FROM TRANS_D WHERE F_TABLE=?");
      oSysDb.setString(1,sSourceTable);
      oSysDb.prepareQuery();
      if( ! oSysDb.next() ) {
        throw new Exception("Table with no Detail Data:"+sSourceTable);
      }

      // build column mapping
      ArrayList oSourceColumn=new ArrayList();
      ArrayList oDestColumn=new ArrayList();
      HashMap oColumnMap = new HashMap();
      HashMap oPreDataMap = new HashMap();
      do {
        String _sSourceColumn = oSysDb.getString("F_FIELD");
        if( (_sSourceColumn == null) || "".equals(_sSourceColumn) ) {
          throw new Exception("Empty Source Field in Table:"+sSourceTable);
        }
        _sSourceColumn = _sSourceColumn.toUpperCase().trim();
        String _sDestColumn   = oSysDb.getString("T_FIELD");
        if( (_sDestColumn == null) || "".equals(_sDestColumn) ) {
          throw new Exception("Empty Dest Field in Table:"+sDestTable);
        }
        _sDestColumn   = _sDestColumn.toUpperCase().trim();

        String _sPreData = oSysDb.getString("T_PREDATA");
        if( (_sPreData != null) && (!"".equals(_sPreData)) ) {
          oPreDataMap.put( _sDestColumn,_sPreData);
        }

        oSourceColumn.add(_sSourceColumn);
        oDestColumn.add(_sDestColumn);
        oColumnMap.put(_sDestColumn,_sSourceColumn);
      } while ( oSysDb.next() );

      // build update SQL
      StringBuffer _sbUpdate = new StringBuffer("UPDATE "+sDestTable+ " SET ");
      for( int i =0 ; i< oDestColumn.size(); i++) {
        if( i != 0 )
          _sbUpdate.append(",");
        _sbUpdate.append( (String) oDestColumn.get(i) ).append("=? ");
      }
      _sbUpdate.append(" WHERE ");
      for( int i =0 ; i< oDestPkColumn.size(); i++) {
        if( i != 0 )
          _sbUpdate.append(" AND ");
        _sbUpdate.append( (String) oDestPkColumn.get(i) ).append("=?");
      }

      StringBuffer _sbInsert = new StringBuffer("INSERT INTO "+sDestTable+" (");
      for( int i =0 ; i< oDestColumn.size(); i++) {
        if( i != 0 )
          _sbInsert.append(",");
        _sbInsert.append( (String) oDestColumn.get(i) );
      }
      _sbInsert.append(") VALUES (");
      for( int i =0 ; i< oDestColumn.size(); i++) {
        if( i != 0 )
          _sbInsert.append(",");
        _sbInsert.append("?");
      }
      _sbInsert.append(")");

      out_.println("Update SQL="+_sbUpdate.toString());
      out_.println("INSERT SQL="+_sbInsert.toString());
      PreparedStatement insert = oDest.prepareStmt(_sbInsert.toString());
      PreparedStatement update = oDest.prepareStmt(_sbUpdate.toString());
      try {
        HashMap oValueMap = new HashMap();
        int nCounter=1;
        while( oSource.next() ) {
          oDesc_.setMsg(String.valueOf(nCounter));
          nCounter++;
        // 先 update
          oValueMap.clear();
          oDest.setCurrentPrepareStmt(update);
          oDest.clearParameters();

          int nSetter= 1;
          for(int i=1; i<= oDestColumn.size() ; i++) {
            Object oDestCol = (Object) oDestColumn.get(i-1);
            String sSourceColumn = (String) oColumnMap.get(oDestCol);
            if( sSourceColumn == null )
              throw new Exception("Update Error Column Mapping");
            try {
              String sStr = oSource.getString(sSourceColumn);
              if( (sStr != null) && !"".equals(sStr) ) {
                String sPreData = (String) oPreDataMap.get(oDestCol);
                if( sPreData != null )
                  sStr = sPreData + sStr;
              }
              oValueMap.put(sSourceColumn,sStr);
              oDest.setString(nSetter++,sStr);
            } catch (Exception err) {
              out_.println("ERROR Get Column:'"+sSourceColumn+"/"+oDestCol+"'");
              throw err;
            }
          }
          for(int i=1; i<= oDestPkColumn.size() ; i++) {
            Object oDestCol = (Object) oDestPkColumn.get(i-1);
            String sSourceColumn = (String) oColumnPkMap.get(oDestCol);
            if( sSourceColumn == null )
              throw new Exception("PK Error Column Mapping");
            String sStr = (String) oValueMap.get(sSourceColumn);
//            out_.println("PK Set:"+nSetter+":"+sSourceColumn+"="+sStr);
            oDest.setString(nSetter++,sStr);
          }

          try {
            int iUpdated = oDest.prepareUpdate();
            if ( iUpdated> 0 )
              continue;
          } catch (Exception updateErr) {
            out_.println("UPDATE 產生錯誤");
            showColumn(oSource,oSourceColumn,oDestColumn);
            throw updateErr;
          }

          oDest.setCurrentPrepareStmt(insert);
          oDest.clearParameters();
          for(int i=1; i<= oDestColumn.size() ; i++) {
            Object oDestCol = (Object) oDestColumn.get(i-1);
            String sSourceColumn = (String) oColumnMap.get(oDestCol);
            if( sSourceColumn == null )
              throw new Exception("Insert Error Column Mapping");
            String sStr = (String) oValueMap.get(sSourceColumn);
            oDest.setString(i,sStr);
          }
          try {
            oDest.prepareUpdate();
          } catch (Exception insertErr) {
            out_.println("INSERT 產生錯誤");
            showColumn(oSource,oSourceColumn,oDestColumn);
            throw insertErr;
          }
        }
      } finally {
        insert.close();
        update.close();
      }
      oDest.commit();
      oDesc_.setTable(null,null);
      oDesc_.setMsg(null);

      out_.println("SUCCESSFUL !!!<HR>");
  }

  public emisTaskDescriptor getDescriptor()
  {
    return oDesc_;
  }
  private void showColumn(emisDb oSource,ArrayList oSourceColumn,ArrayList oDestColumn) throws SQLException
  {
    if ( oSourceColumn.size() != oDestColumn.size()) {
      out_.println("Source Column and Destination Column size not match");
    }
    out_.println("SOURCE   TARGET");
    ResultSetMetaData meta = oSource.getResultSet().getMetaData();
    for(int i=0;i < oSourceColumn.size(); i++) {
      String s = (String) oSourceColumn.get(i);
      String t = (String) oDestColumn.get(i);
      out_.println(s + "    "+t+ "    " + meta.getColumnDisplaySize(i+1)+"/"+meta.getPrecision(i+1)+"/"+meta.getScale(i+1));
    }
  }
/*
  public static void main(String [] argvs) throws Exception
  {
    emisServletContext servlet = new emisServletContext();
    emisServerFactory.createServer(servlet,"c:\\wwwroot\\epos","c:\\resin\\epos.cfg",false);
    emisDbTransfer tr = new emisDbTransfer();
    emisHttpServletRequest req = new emisHttpServletRequest();
//    req.setParameter("COMPANY_NO","01");
//    req.setParameter("QRY_DATE1","870101");
//    req.setParameter("QRY_DATE2","920101");
    tr.setContext(servlet);
    tr.setRequest(req);
    tr.run();
  }
  */

}

