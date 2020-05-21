/**
 * 抓取採購( PO) 單資訊
 * User: shaw
 * Date: Jul 14, 2003
 * Time: 2:28:59 PM
 *
 */
package com.emis.app.migration.action;

import java.util.StringTokenizer;

public final class emisMiGetPoInfo extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    final StringTokenizer token = new StringTokenizer(param[0], ".");
    final String[] s = {null, null};
    for (int i = 0; token.hasMoreTokens(); i++) {
      s[i] = token.nextToken();
    }
    final String sql = "select " + s[1] + " from " + s[0] + " where PO_NO = 'PO' + ? and S_NO = ?";
    this.initStmt(sql);
    final String value = this.doQuery(src);
    return value;
  }
}
