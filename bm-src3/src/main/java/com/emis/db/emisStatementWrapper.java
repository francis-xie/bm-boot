package com.emis.db;

import java.io.PrintWriter;

/**
 * Statement wrapper 的 interface
 * 用來描述 wrapper 的情形
 * @see com.emis.db.emisConnection
 * @see com.emis.db.emisStatement
 * @see com.emis.db.emisPreparedStatement
 * @see com.emis.db.emisCallableStatement
 */
public interface emisStatementWrapper {
  public void desc(PrintWriter out);
}