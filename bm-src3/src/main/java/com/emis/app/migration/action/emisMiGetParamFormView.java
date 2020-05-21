/**
 *抓取由前端傳入之參數
 * User: shaw
 * Date: Jul 14, 2003
 * Time: 2:14:16 PM
 *
 */
package com.emis.app.migration.action;

import javax.servlet.ServletContext;

public final class emisMiGetParamFormView extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    final ServletContext context = this.config.getContext();
    String value = (String) context.getAttribute(param[0]);
    if (value == null && param.length >= 2)
      value = param[1];
    return value;
  }

}
