package com.emis.db.oracle;

import com.emis.db.emisConnectProxy20;
import com.emis.spool.emisComplexSpool;
import oracle.jdbc.driver.OracleConnection;

import javax.servlet.ServletContext;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

//import oracle.jdbc.OracleConnection;


public class emisConnectProxyOrcl extends emisConnectProxy20
{
    public emisConnectProxyOrcl(ServletContext oContext,PooledConnection oPoolConnect,emisComplexSpool oSpool )
    {
        super(oContext,oPoolConnect,oSpool);
    }

    public Connection getConnection() throws SQLException
    {
        PooledConnection oPoolConn_ = (PooledConnection) super.oPooledObj_;

        oConnect_ = oPoolConn_.getConnection();
        OracleConnection OraConn = (OracleConnection) oConnect_;
        OraConn.setDefaultRowPrefetch(100);
        /**
         * warning , Oracle Connection 的 setDefaultExecuteBatch
         * 設定此選項,會造成 Oracle 的
         * prepareStatement 字元有問題 , 轉碼不統一
         */        return oConnect_;
    }    public long getPid() {    	return 0;    }

}
