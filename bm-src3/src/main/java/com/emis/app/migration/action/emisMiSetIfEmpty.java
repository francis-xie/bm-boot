package com.emis.app.migration.action;


/**
 * User: merlin
 * Date: Apr 25, 2004
 * Time: 5:55:58 PM
 */
public final class emisMiSetIfEmpty extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
      if (src[0].trim().length()==0 && (param.length>0))
         return  param[0];
      return src[0];
  }
}
