package com.emis.db.psql;

import com.emis.db.emisConnectProxy10;
import com.emis.spool.emisComplexSpool;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.SQLException;



public class emisConnectProxyPsql extends emisConnectProxy10
{
    public emisConnectProxyPsql(ServletContext oContext,Connection oConnect,emisComplexSpool oSpool )
    {
        super(oContext,oConnect,oSpool);
    }

    public Connection getConnection() throws SQLException
    {
        return (Connection) super.oPooledObj_;
    }    public long getPid() {    	return 0;    }
}