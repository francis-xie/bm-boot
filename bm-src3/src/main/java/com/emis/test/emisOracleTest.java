package com.emis.test;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


public class emisOracleTest {
    private static PooledConnection  oConnectionPool_; // Database Connection pool Object
    private static ConnectionPoolDataSource oDataSource_;

    public static void main(String[] args) throws Exception
    {
        OracleConnectionPoolDataSource _Ocpds = new OracleConnectionPoolDataSource();
        _Ocpds.setURL("jdbc:oracle:thin:@210.71.250.10:1521:POS");
        _Ocpds.setUser("pos");
        _Ocpds.setPassword("pos");
        oConnectionPool_ = _Ocpds.getPooledConnection();

        Connection c1 = oConnectionPool_.getConnection();
        System.out.println("TRANS:"+c1.getTransactionIsolation());
        c1.setTransactionIsolation(Connection.TRANSACTION_NONE);
        Statement stmt1 = c1.createStatement();
        ResultSet rs1 = stmt1.executeQuery("SELECT V_NAME FROM VENDOR");
/*
        Connection c2 = oConnectionPool_.getConnection();
        Statement stmt2 = c2.createStatement();
        ResultSet rs2 = stmt2.executeQuery("SELECT V_NAME FROM VENDOR");
*/
        rs1.next();
        rs1.next();
/*
        rs2.next();
        rs2.next();
*/

        c1.close();
        c1 = oConnectionPool_.getConnection();
        System.out.println("TRANS:"+c1.getTransactionIsolation());
        c1.close();
        System.out.println("Closed");
  //      rs2.next();
//        System.out.println(rs2.getString("V_NAME"));

//        c2.close();

    }
}