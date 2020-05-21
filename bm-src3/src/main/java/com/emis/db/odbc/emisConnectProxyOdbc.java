package com.emis.db.odbc;

import com.emis.db.emisConnectProxy10;
import com.emis.spool.emisComplexSpool;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.SQLException;

public class emisConnectProxyOdbc extends emisConnectProxy10
{
    public emisConnectProxyOdbc(ServletContext oContext,Connection oConnect,emisComplexSpool oSpool )
    {
        super(oContext,oConnect,oSpool);
    }

    public Connection getConnection() throws SQLException
    {
        return (Connection) super.oPooledObj_;
    }
    public long getPid() {    	return 0;    }
}