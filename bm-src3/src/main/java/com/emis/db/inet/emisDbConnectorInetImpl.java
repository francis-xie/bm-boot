/*

 * $Header: /repository/src3/src/com/emis/db/inet/emisDbConnectorInetImpl.java,v 1.1.1.1 2005/10/14 12:42:06 andy Exp $

 *

 * Copyright (c) EMIS Corp.

 */

package com.emis.db.inet;



import com.emis.db.emisDbConnectorJDBC20;

import com.emis.db.emisSQLCache;

import com.emis.spool.emisPoolable;

import com.emis.trace.emisTracer;



import javax.servlet.ServletContext;

import javax.sql.PooledConnection;

import java.sql.*;

import java.util.Properties;



/**

 * Database Driver for MS-SQL Server 2000<br>

 * http://www.inetsoftware.de<br>

 * eMail: support@inetsoftware.de

 */

public class emisDbConnectorInetImpl extends emisDbConnectorJDBC20 {

  public emisDbConnectorInetImpl(ServletContext oContext, Properties props) throws Exception {

    super(oContext, props);

  }



  protected void createSource(Properties props) throws java.lang.Exception {

    com.inet.pool.PDataSource pds = new com.inet.pool.PDataSource();

   // com.inet.tds.PDataSource pds = new com.inet.tds.PDataSource();

    String _serverName = (String) props.get("servername");

    String _dbname = (String) props.get("database");

    String _username = (String) props.get("username");

    String _password = (String) props.get("password");

    String _charset = (String) props.get("encoding");



    if (_charset == null) _charset = "UTF-8";

    sDbCharset_ = _charset;



    pds.setServerName(_serverName);

    pds.setDatabaseName(_dbname);

    pds.setUser(_username);

    pds.setPassword(_password);

    pds.setLoginTimeout(10);

    pds.setMode(pds.MODE_SQLSERVER_70_ASCII);

    //pds.setMode(pds.MODE_SQLSERVER_70); // FOR  NVARCHAR

    pds.setProperty("useCursorsAlways", "true");

    pds.setProperty("prepare", "false");

//        pds.setProperty("impltran",true"); // jdbc-odbc compliant



    this.oDataSource_ = pds;

  }



  public emisPoolable generateRealPooledObject(int nTimeOut) throws Exception {

    PooledConnection _oPool = null;

    emisPoolable _oPooled = null;

    try {

      oDataSource_.setLoginTimeout(nTimeOut);

      _oPool = oDataSource_.getPooledConnection();

      _oPooled = new emisConnectProxyInet(oContext_, _oPool, this);

      return _oPooled;

    } catch (SQLException ignore) {

      emisTracer.get(oContext_).warning(this, ignore);

      throw ignore;

    }

  }



  public String getSequenceNumber(Connection oConn, String sSequenceName, boolean checkAutoDrop, String sDropCondition, String sFormat) throws SQLException {

    int _nNeedCheck = checkAutoDrop ? 1 : 0;



    if (sDropCondition == null)

      sDropCondition = "";



    CallableStatement call = oConn.prepareCall("execute epos_getIdentity ?,?,?,?,?");

    try {



      call.setString(1, sSequenceName);

      call.setInt(2, _nNeedCheck);

      call.setString(3, sDropCondition);

      call.setString(4, sFormat);

      call.registerOutParameter(5, Types.VARCHAR);

      call.execute();

      String _sSeqValue = call.getString(5);

      return _sSeqValue;

    } finally {

      try {

        call.close();

      } catch (Exception ignore) {

      }

    }

  }



  public String getStoreSequence(Connection oConn, String sSequenceName, String sSNO, String sDropCondition, String sFormat) throws SQLException {

    if (sDropCondition == null)

      sDropCondition = "";



    CallableStatement call = oConn.prepareCall("execute epos_getStoreIdent ?,?,?,?,?");

    try {

//        System.out.println(sSNO+":"+sSequenceName+":"+sDropCondition+":"+sFormat);

      call.setString(1, sSNO);

      call.setString(2, sSequenceName);

      call.setString(3, sDropCondition);

      call.setString(4, sFormat);

      call.registerOutParameter(5, Types.VARCHAR);

      call.execute();

      String _sSeqValue = call.getString(5);

      return _sSeqValue;

    } finally {

      try {

        call.close();

      } catch (Exception ignore) {

      }

    }

  }



/*

    public emisMenuPermission getMenuPermission(Connection oConn , String sUserId,String sGroup,String sStoreNo) throws SQLException

    {

        PreparedStatement pstmt = oConn.prepareStatement(

        "select distinct u.keys,u.BTNADD,u.BTNUPD,u.BTNDEL,u.BTNRPT from Userrights u, Menus m where "+

        "u.USERID=? and u.Rights='Y'  and (u.S_NO is null or u.S_NO=?) and (u.KEYS=m.KEYS or m.MENU_TYPE is null)  "+

        "union "+

        "select distinct u.keys,u.BTNADD,u.BTNUPD,u.BTNDEL,u.BTNRPT from Userrights u, Menus m where "+

        "u.USERGROUPS=?  and u.Rights='Y'  and (u.KEYS=m.KEYS or m.MENU_TYPE is null) ");



        try {

            pstmt.setString(1,sUserId);

            pstmt.setString(2,(sStoreNo == null) ? "":sStoreNo);

            pstmt.setString(3,(sGroup == null) ? "":sGroup);

            ResultSet rs = pstmt.executeQuery();

            emisMenuPermission m =  new emisMenuPermission(rs);

            return m;

        } finally {

            pstmt.close();

        }

    }

*/



  public void expireSQLCache(Connection oConn, String sSQLName) throws SQLException {

    PreparedStatement pstmt = oConn.prepareStatement("UPDATE " + emisSQLCache.SQLCACHETABLE + " SET LASTUPDATE=dbo.GetLocalDate() WHERE SQLNAME=?");

    try {

      pstmt.setString(1, sSQLName);

      pstmt.executeUpdate();

    } finally {

      pstmt.close();

      pstmt = null;

    }

  }



  /** 將錯誤碼轉換成字串, 以利使用者描述與理解. */

  public String errToMsg(SQLException e) {

    if (e == null) return null;

    int nErrCode = e.getErrorCode();



    if (nErrCode == 2627)

      return "編號重覆:請重新輸入(2627)";

    return e.getMessage() + "(" + nErrCode + ")";

  }



  /**

   * 是否是 PrimaryKey Exception

   */

  public boolean isPKError(SQLException e) {

    if (e == null) return false;

    int nErrCode = e.getErrorCode();

    if (nErrCode == 2627)

      return true;

    return false;

  }



  public String dualTestSQL() {

    return "SELECT 1";

  }



/*

  select count(*) as cnt from information_Schema.tables

  where table_name=upper('mm_temp')



  alter database les set CONCAT_NULL_YIELDS_NULL OFF;

  alter database les set ANSI_NULLS OFF;



  EXEC sp_dboption 'Les','ANSI_NULL_DEFAULT','false';

  EXEC sp_dboption 'Les','CONCAT_NULL_YIELDS_NULL','OFF';

  EXEC sp_dboption 'Les','ANSI_NULLS','OFF';



  SET CONCAT_NULL_YIELDS_NULL OFF;

  set ANSI_NULLS OFF;

 */

  /**
   * 获取数库的类型(如一种驱动可面向多种不同数据请写不同的Impl)
   * @return
   */
  public String getDBType() {
    return "mssql";
  }

}

