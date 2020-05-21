package com.emis.app.migration.action;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiGetST_NAME extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    if (db == null)
      initStmt("select st_name from staff where st_key =? ");
    return doQuery(src);
  }
}
