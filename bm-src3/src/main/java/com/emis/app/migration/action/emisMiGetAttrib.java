package com.emis.app.migration.action;



/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiGetAttrib extends emisMiAction {

  public final String act(final String[] src, final String[] param) {
    // 取得庫存調整單表身之 recno
    // 傳入的參數需為 [S_no][]
   String sRet;
    if (param != null)
       sRet = (String) config.getContext().getAttribute(param[0]);
    else
       sRet = (String) config.getContext().getAttribute(src[0]);
      if (sRet ==null && param != null && param.length>=2)
          sRet = param[1];

    return sRet;

  }
}
