package com.emis.app.migration.action;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiStrTrimCat extends emisMiAction {
  private final StringBuffer sb = new StringBuffer();

  public final String act(final String[] src, final String[] param) {
    sb.setLength(0);
    if (src != null) {
      for (int i = 0; i < src.length; i++)
        sb.append(src[i].trim());
    }
    if (param != null) {
      for (int i = 0; i < param.length; i++)
        sb.append(param[i].trim());
    }
    return (sb.toString());
  }
}
