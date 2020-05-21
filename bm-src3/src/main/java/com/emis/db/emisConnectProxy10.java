package com.emis.db;

import com.emis.spool.emisComplexSpool;
import com.emis.trace.emisTracer;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *  此 class 為 ConnectionProxy 針對 JDBC1.0 的實作
 *  (but still an abstract class)
 *  不過目前幾乎都已使用 JDBC2.0,所以沒甚麼用到
 *
 *  @see com.emis.db.emisConnectProxy
 *  @see com.emis.db.emisConnectProxy20
 *  @see com.emis.db.emisAbstractProxy
 */

abstract public class emisConnectProxy10 extends emisAbstractProxy 
{
    protected Connection oConnect_ ;

    public emisConnectProxy10(ServletContext oContext,Connection oConnect,emisComplexSpool oSpool )
    {
        // pool oPoolConnect Object
        // it will be saved in oPooledObj_
        super(oContext,oConnect,oSpool);
        oConnect_ = oConnect;
    }

    public boolean hasFatalError()
    {
        if( hasFatalError_ )
          return hasFatalError_;
        try {
            hasFatalError_ = oConnect_.isClosed();
            if( hasFatalError_ )
              fatalError_ = new Exception("connection already closed");
            return hasFatalError_;
        } catch (Exception fatal) {
            fatalError_ = fatal;
            hasFatalError_ = true;
            return true;
        }
    }

    public void freeResource()  {
      oPooledObj_     = null;
      oSpool_   = null;
      oStmt_ = null;
    }


    public void close()  {      //oStmt_ = null; , robert, 2011/11/25 保留,可以在 dbMonitor 看到最後一個 Statement 執行狀況	

      try {
        // 不會真的關了 connection
        // 有一個 wrapper class  會清 resource
        // oConnect_.close();
        oSpool_.checkIn(this);
      }catch(Exception ignore) {
        emisTracer.get(oContext_).warning(this,ignore.getMessage());
      }
    }

    abstract public Connection getConnection() throws SQLException;

}
