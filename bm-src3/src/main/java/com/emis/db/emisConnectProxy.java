package com.emis.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *  Connection Proxy 虛擬了 JDBC Connection Cache.
 *  因為 JDBC2.0 才有 javax.sql.PooledConnection
 *  但 JDBC 2.0 之前只有 java.sql.Connection,
 *  所有透過 emisConnectProxy Interface 將這兩種 Cache
 *  統一介面,透過 getConnection 來取得 Connection
 *  不管背後是 JDBC1.0 還是 2.0
 *
 *  @see com.emis.db.emisProxyDesc
 */

public interface emisConnectProxy
{
    /**
     * 從此 Proxy 中拿取一個 Connection Wrapper (emisConnection)
     */
    Connection getConnection() throws SQLException;

    /**
     * 將此 Proxy 關畢,並將 Resource 還給系統 (checkIn spool)
     */
    void close();
    /**
     * 設定使用訊息,以後好 debug
     * 可以從 emisDbMonitor Servlet 看到訊息
     * @see com.emis.servlet.emisDbMonitor
     */
    void setDescription(String sStr);
    String getDescription();
    /**     * 為了方便可以對應到 database 的 pid     */    long getPid();        /**
     * 此功能是為了讓 DbMonitor 看到哪一個
     * 正在執行哪個 SQL
     */
    public void setExecutingStatement(emisStatementWrapper stmt);
    public emisStatementWrapper getExecutingStatement();    // Robert, 2011/11/25    // update the last touch time to current time    public void touch();

}
