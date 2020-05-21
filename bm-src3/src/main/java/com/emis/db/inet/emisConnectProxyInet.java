package com.emis.db.inet;

import com.emis.db.emisConnectProxy20;
import com.emis.spool.emisComplexSpool;

import javax.servlet.ServletContext;
import javax.sql.PooledConnection;
import java.sql.Connection;import java.sql.PreparedStatement;import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MSSQL 的 transfer Mode 要設 1 DB_TO_SYS
 */
public class emisConnectProxyInet extends emisConnectProxy20
{	long m_pid = -1;	
    public emisConnectProxyInet(ServletContext oContext,PooledConnection oPoolConnect,emisComplexSpool oSpool ) throws SQLException
    {
        super(oContext,oPoolConnect,oSpool);                oConnect_ = oPoolConnect.getConnection();        try {        	__getPid(oConnect_);        } finally {        	oConnect_.close();        	oConnect_ = null;        }
    }	protected void __getPid(Connection conn ) throws SQLException {        PreparedStatement stmt = oConnect_.prepareStatement("Select @@SPID");        try {        	ResultSet rs = stmt.executeQuery();        	try {	        	if( rs.next() ){	        		String sPid = rs.getString(1);	        		m_pid = Long.parseLong(sPid);	        		        	}        	} finally {        		rs.close();        	}        } finally {        	stmt.close();                }	}    

    public Connection getConnection() throws SQLException
    {
        PooledConnection oPoolConn_ = (PooledConnection) super.oPooledObj_;
        oConnect_ = oPoolConn_.getConnection();
        return oConnect_;
    }
    public long getPid() {    	return m_pid;    }
}
