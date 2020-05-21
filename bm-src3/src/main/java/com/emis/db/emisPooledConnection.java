package com.emis.db;

import javax.sql.ConnectionEventListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * 為了讓 jdbc1.0 的 driver 也可以用 jdbc2.0 的機制
 * 目前並沒有使用
 */
public class emisPooledConnection
{
  Hashtable oListener = new Hashtable();

  void addConnectionEventListener(ConnectionEventListener p0)
  {
      if (! oListener.containsKey(p0) )
      {
          oListener.put(p0,p0);
      }
  }

  void close() throws SQLException
  {

  }

  Connection getConnection() throws SQLException
  {
      return null;
  }

  void removeConnectionEventListener(ConnectionEventListener p0)
  {
      oListener.remove(p0);
  }
}