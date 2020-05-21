package com.emis.db;

import com.emis.spool.emisComplexSpool;
import com.emis.spool.emisPoolable;
import com.emis.trace.emisTracer;

import javax.servlet.ServletContext;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;



/**
 * this class is for internal use not for external
 *
 * 在 JDBC2.0 中,一個 PooledConnection 每次呼叫 getConnection 所
 * 傳回來的 Connection 物件,雖然每次都不一樣 (With different Hash Code),
 * 但其實內部是同一個 Connection物件,且連續呼叫,會把上一個的 Resource
 * 給關掉,所以在 Spool 實作上,一個 PooledConnection 就是代表
 * Database 的一個實際的 Connection
 *
 * 一個 emisDbConnector 繼承 com.emis.spool.emisComplexSpool
 * 所以本身具備了管理具有 Poolable interface 的物件的能力
 */

abstract public class emisDbConnectorJDBC20 extends emisComplexSpool implements emisDbConnector
{
    private PooledConnection  oConnectionPool_; // Database Connection pool Object
    protected ConnectionPoolDataSource oDataSource_;
    protected String sDbCharset_ ;
    protected int nTransferMode_ ;


    /**
     *  create dataSource is a database specific procedure
     */
    protected abstract void createSource(Properties props) throws Exception;

    public emisDbConnectorJDBC20(ServletContext oContext,Properties props) throws Exception
    {
        super(oContext,props);
        setTransferMode(props);
    }
    private void setTransferMode(Properties props) throws Exception
    {
        String _sMode = props.getProperty("transfermode","0");
      // System.out.println("_sMode["+_sMode+"]");
        if( "NONE".equalsIgnoreCase(_sMode) ) {
          nTransferMode_ = this.TRANSFER_NONE;
          return;
        }
        if( "DBTOSYS".equalsIgnoreCase(_sMode) ) {
          nTransferMode_ = this.TRANSFER_DB_TO_SYS;
          return;
        }
        if( "SYSTODB".equalsIgnoreCase(_sMode) ) {
          nTransferMode_ = this.TRANSFER_SYS_TO_DB;
          return;
        }
        if( "ALL".equalsIgnoreCase(_sMode) ) {
          nTransferMode_ = this.TRANSFER_ALL;
          return;
        }
        try {
          nTransferMode_ = Integer.parseInt(_sMode);
        } catch( Exception e ) {
            emisTracer.get(super.oContext_).warning(this,"set database transfer encoding error:"+e.getMessage());
        }
    }


    public synchronized emisConnectProxy getConnection() throws Exception
    {
        emisConnectProxy _oObj = (emisConnectProxy) super.checkOut();
        return _oObj;
    }
    /*
    public synchronized void checkIn(emisConnectProxy oEmisConnect) throws Exception
    {
        if ( oEmisConnect == null ) return;
        super.checkIn( (emisPoolable) oEmisConnect);
    }    */

    public String getCharset()
    {
        return sDbCharset_;
    }

    public int getCharTransferMode()
    {
        return nTransferMode_;
    }

    public emisEncodingTransfer getEncodingTransfer()
    {
      /**
       * 不可用 member , 不然 emisDb.setEncoding 會把 Connector 的給設掉
       */
      return new emisEncodingTransfer(sDbCharset_,nTransferMode_);
    }

    abstract public emisPoolable generateRealPooledObject(int nTimeOut) throws Exception;

    abstract public String getSequenceNumber(Connection oConn,String sSequenceName,boolean checkAutoDrop,String sDropCondition,String sFormat) throws SQLException;
    abstract public String getStoreSequence(Connection oConn,String sSequenceName,String sSNO,String sDropCondition,String sFormat) throws SQLException;
//    abstract public emisMenuPermission getMenuPermission(Connection oConn,String sUserId,String sGroup,String sStoreNo) throws SQLException;
    abstract public void expireSQLCache(Connection oConn,String sSQLName) throws SQLException;
/*
    abstract public String getCreateTempTableScript(String sTableName,String [] sColumns,int [] nSQLType,String [] nSize);

    abstract public String getDropTableScript(String sTableName);


    protected emisPoolable generateRealPooledObject(int nTimeOut)
    {
        PooledConnection _oPool = null;
        emisPoolable _oPooled = null;
        try {
            oDataSource_.setLoginTimeout(nTimeOut);
            _oPool = oDataSource_.getPooledConnection();

            // we set Oracle Database to AMERICAN_AMERICA.WE8ISO8859P1
            // so the client NLS should set to the same...

            Connection _oConn = _oPool.getConnection();

            try {
                Statement _oStmt = _oConn.createStatement();
                _oStmt.execute("ALTER SESSION SET NLS_LANGUAGE = 'AMERICAN'");
                _oStmt.execute("ALTER SESSION SET NLS_TERRITORY = 'AMERICA'");
                _oStmt.close();
            } finally {
                _oConn.close();
            }

            _oPooled = new emisConnectProxy20(oContext_,_oPool,this);
            return _oPooled;
        } catch(SQLException ignore) {
        }
        return _oPooled;
    }
*/
}
