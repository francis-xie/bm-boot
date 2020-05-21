package com.emis.rights;

import com.emis.rights.emisDyRightsDecorator;

/**
 * Created by IntelliJ IDEA.
 * User: Jacky
 * Date: 2005/6/15
 * Time: 上午 11:19:36
 *[3606]提供報表相關的動態權限設定
 */
public class emisDyRptRightsDeco extends emisDyRightsDecorator{
  protected boolean isDefaultChecked = false; //是否預設打勾

  /**
   * 取的預設的直
   * @return
   */
  public boolean isDefaultChecked() {
    return isDefaultChecked;
  }

  /**
   * 設定預設值
   * @param defaultChecked
   */
  public void setDefaultChecked(boolean defaultChecked) {
    isDefaultChecked = defaultChecked;
  }
}
