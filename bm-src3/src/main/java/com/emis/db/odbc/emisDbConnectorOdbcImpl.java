package com.emis.db.odbc;



import com.emis.db.emisConnectionWrapper;

import com.emis.db.emisDbConnectorJDBC20;

import com.emis.spool.emisPoolable;

import com.emis.trace.emisTracer;

import com.emis.util.emisUtil;



import javax.servlet.ServletContext;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.Properties;





public class emisDbConnectorOdbcImpl extends emisDbConnectorJDBC20

{



    String _url ;

    String _user;

    String _password;



    public emisDbConnectorOdbcImpl(ServletContext oContext,Properties props) throws Exception

    {

        super(oContext,props);

    }



    protected void createSource(Properties props) throws java.lang.Exception

    {



        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();

        _url = (String) props.getProperty("url");

        _user = (String) props.getProperty("username","");

        _password = (String) props.getProperty("password","");

        String _charset =  (String) props.get("encoding");

        if( _charset == null ) _charset = emisUtil.FILENCODING;

        sDbCharset_ = _charset;

    }



    public emisPoolable generateRealPooledObject(int nTimeOut) throws Exception

    {

        try {

            DriverManager.setLoginTimeout(nTimeOut);

            Connection c = DriverManager.getConnection(_url,_user,_password);

            // for JDBC1.0 , you have to use wrapper to

            // protect the close method

            c = new emisConnectionWrapper(c);

            return new emisConnectProxyOdbc(oContext_,c,this);

        } catch(Exception ignore) {

            emisTracer _oTr = emisTracer.get(oContext_);

            _oTr.warning("getConnection "+_url+"/"+_user+"/"+_password);

            _oTr.warning(this,ignore);

            throw ignore;

        }

    }



    public String getSequenceNumber(Connection oConn,String sSequenceName,boolean checkAutoDrop,String sDropCondition,String sFormat) throws SQLException

    {

      throw new SQLException("not supported in ODBC connector");

    }

    public String getStoreSequence(Connection oConn,String sSequenceName,String sSNO,String sDropCondition,String sFormat) throws SQLException

    {

      throw new SQLException("not supported in ODBC connector");

    }



    /*

    public emisMenuPermission getMenuPermission(Connection oConn , String sUserId,String sGroup,String sStoreNo) throws SQLException

    {

      throw new SQLException("not supported in ODBC connector");

    }

    */



    public void expireSQLCache(Connection oConn,String sSQLName) throws SQLException

    {

      throw new SQLException("not supported in ODBC connector");

    }



    public String errToMsg( SQLException e )

    {

      if( e == null ) return null;

      return e.getMessage();

    }

    public boolean isPKError( SQLException e)

    {

      if ( e == null ) return false;

      int nErrCode = e.getErrorCode();

      if( nErrCode == 23000)

        return true;

      return false;

    }

    public String dualTestSQL()

    {

      return "SELECT 1";

    }


  /**
   * 获取数库的类型(如一种驱动可面向多种不同数据请写不同的Impl)
   * @return
   */
  public String getDBType() {
    return "odbc";
  }

}