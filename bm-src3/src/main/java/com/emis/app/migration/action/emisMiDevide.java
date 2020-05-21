package com.emis.app.migration.action;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiDevide extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    double d = Double.parseDouble(src[0]);
    d = d / Double.parseDouble(param[0]);
    return String.valueOf(d);
  }
}
