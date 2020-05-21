/**
 *
 * User: shaw
 * Date: Jul 14, 2003
 * Time: 3:01:25 PM
 *
 */
package com.emis.app.migration.action;

import java.util.StringTokenizer;

public final class emisMiGetStoreInfo extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    final String value;
    final StringTokenizer token = new StringTokenizer(param[0], ".");
    final String[] s = {null, null};
    for (int i = 0; token.hasMoreTokens(); i++) {
      s[i] = token.nextToken();
    }
    final String sql = "select " + s[1] + " from " + s[0] + " where S_NO = ?";
    this.initStmt(sql);
    value = doQuery(src);
    return value;
  }
}
