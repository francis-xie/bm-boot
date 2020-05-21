/**
 *
 * User: shaw
 * Date: Jul 17, 2003
 * Time: 12:39:42 PM
 *
 */
package com.emis.app.migration.action;

public final class emisMiGetPoNo extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    final String poNo = "PO" + src[0];
    return poNo;
  }
}
