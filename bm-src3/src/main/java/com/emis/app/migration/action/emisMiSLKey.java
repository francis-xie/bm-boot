package com.emis.app.migration.action;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiSLKey extends emisMiAction {
  private final StringBuffer sb = new StringBuffer();

  public final String act(final String[] src, final String[] param) {
    sb.setLength(0);
    sb.append(src[0].trim());
    sb.append(src[1].trim());
    String SL_No = "0000" + src[2].trim();
    SL_No = SL_No.substring(SL_No.length() - 4);
    sb.append(SL_No);
    if (src.length >= 4)
      sb.append(src[3].trim());
    return (sb.toString());
  }
}
