package com.emis.app.migration.action;

import com.emis.util.emisDate;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiCDate extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    final String sDate;
    String sYear;
    if (src == null) {
      sDate = new emisDate(false).toString();
    } else if (src[0].trim().length() <= 4) {
      return src[0];
    } else
      sDate = src[0];
    final int cYear = Integer.parseInt(sDate.substring(0, 4)) - 1911;
    sYear = String.valueOf(cYear);
    if (sYear.length() == 2) sYear = "0" + sYear;
    sYear = sYear + sDate.substring(4);
    return sYear;
  }
}
