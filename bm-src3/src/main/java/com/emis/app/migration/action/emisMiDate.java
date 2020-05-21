package com.emis.app.migration.action;

import com.emis.util.emisDate;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiDate extends emisMiAction {
  final emisMiCDate cdate = new emisMiCDate();
  final String[] actParam = {"", null};

  public final String act(final String[] src, final String[] param) {
    String sDate = new emisDate().toString();
    if (param.length > 0 && param[0].equalsIgnoreCase("C")) {
      actParam[0] = sDate;
      sDate = cdate.act(actParam, null);
    }
    return sDate;
  }
}
