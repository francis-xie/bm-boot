package com.emis.db;

import com.emis.spool.emisComplexSpool;
import com.emis.trace.emisTracer;

import javax.servlet.ServletContext;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

/** *  此 class 為 ConnectionProxy 針對 JDBC2.0 的實作 *  (but still an abstract class) *  Connection Proxy 內含有一個 JDBC Connection, *  因為 2.0 有 PooledConnection,所以多了一層 proxy *  還多了 ConnectionEventListener interface   *   * *  @see com.emis.db.emisConnectProxy *  @see com.emis.db.emisConnectProxy10 *  @see com.emis.db.emisAbstractProxy */

abstract public class emisConnectProxy20 extends emisAbstractProxy implements ConnectionEventListener
{
    /**
     * 此 oConnect_ 是讓下一層的 implementation 用的
     */
    protected Connection oConnect_ ;

    public emisConnectProxy20( ServletContext oContext,PooledConnection oPoolConnect,emisComplexSpool oSpool )
    {
        // pool oPoolConnect Object
        // it will be saved in oPooledObj_
        super(oContext,oPoolConnect,oSpool);
        oPoolConnect.addConnectionEventListener(this);
    }



    public boolean hasFatalError()
    {
      return hasFatalError_;
    }


    public void freeResource()
    {
        PooledConnection _oPC = (PooledConnection) super.oPooledObj_;

        if( _oPC == null ) return;

        _oPC.removeConnectionEventListener(this);

        try {
            _oPC.close();
        } catch(Exception ignore2) {
            emisTracer.get(oContext_).warning(this,"FreeResource " + ignore2.getLocalizedMessage());
        }

        super.oPooledObj_     = null;
        oConnect_ = null;
        oSpool_   = null;
    }

    /**
     *  this is used for support 'ConnectionEventListener'
     */
    public void connectionClosed(ConnectionEvent e)
    {
       // log , or do some deal
       // we can't put FFatalError here , because common close
       // will trigger this event
    }

    /**
     *  this is only occurred when fatal connection error
     *  , so we have to close resource , this function
     *  is used for support 'ConnectionEventListener'
     */
    public void connectionErrorOccurred(ConnectionEvent e)
    {
        hasFatalError_ = true;

        SQLException _oSQLE = e.getSQLException();
        fatalError_ = _oSQLE;

        while( _oSQLE != null )
        {
            emisTracer.get(oContext_).warning(this,_oSQLE.getMessage());
            _oSQLE = _oSQLE.getNextException();
        }
    }


    private void closeConnection()
    {
      //oStmt_ = null; , robert, 2011/11/25 保留,可以在 dbMonitor 看到最後一個 Statement 執行狀況
      if(oConnect_ != null )      {
        try {
          oConnect_.close();
        }catch(Exception ignore) {
          emisTracer.get(oContext_).warning(this,ignore.getMessage());
        }finally{
          oConnect_ = null;
        }
      }
    }

    public void close()
    {
        closeConnection();
        try {
            oSpool_.checkIn(this);
        }catch(Exception ignore) {
            emisTracer.get(oContext_).warning(this,ignore.getMessage());
        }
    }


    abstract public Connection getConnection() throws SQLException;

}
