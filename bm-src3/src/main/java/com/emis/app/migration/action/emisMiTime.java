package com.emis.app.migration.action;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiTime extends emisMiAction {
  static final SimpleDateFormat formater = new SimpleDateFormat("HHmmss");

  public final String act(final String[] src, final String[] param) {
    return formater.format(new Date());
  }
}
