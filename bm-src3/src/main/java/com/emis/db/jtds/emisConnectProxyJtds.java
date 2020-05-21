package com.emis.db.jtds;

import com.emis.db.emisConnectProxy20;
import com.emis.spool.emisComplexSpool;

import javax.servlet.ServletContext;
import javax.sql.*;
import java.sql.*;




/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2005/2/4
 * Time: 下午 02:07:45
 * To change this template use Options | File Templates.
 */
public class emisConnectProxyJtds extends emisConnectProxy20
{
	
	long m_pid = -1;
	
	public emisConnectProxyJtds(ServletContext oContext,PooledConnection oPoolConnect,emisComplexSpool oSpool ) throws SQLException
    {
        super(oContext,oPoolConnect,oSpool);
        
        
        oConnect_ = oPoolConnect.getConnection();
        try {
        	__getPid(oConnect_);
        } finally {
        	oConnect_.close();
        	oConnect_ = null;
        }
    }
	
	protected void __getPid(Connection conn ) throws SQLException {
        PreparedStatement stmt = oConnect_.prepareStatement("Select @@SPID");
        try {
        	ResultSet rs = stmt.executeQuery();
        	try {
	        	if( rs.next() ){
	        		String sPid = rs.getString(1);
	        		m_pid = Long.parseLong(sPid);	        	
	        	}
        	} finally {
        		rs.close();
        	}
        } finally {
        	stmt.close();        
        }
	}

    public Connection getConnection() throws SQLException
    {
        PooledConnection oPoolConn_ = (PooledConnection) super.oPooledObj_;
        oConnect_ = oPoolConn_.getConnection();
        oConnect_.setAutoCommit(true);
        return oConnect_;
    }
    
    public long getPid() {
     	return m_pid;
    }

}
