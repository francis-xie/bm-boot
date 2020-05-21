package com.emis.app.migration.action;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiTimeSeq extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    if (db == null) {
      initStmt("select time_no from Time_seg where ? between TIME_BEGIN and TIME_END");
    }
    return doQuery(src);
  }
}
