/* $Id: emisSTRLENFormat.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

/**
 * 產生&lt;input&gt;的字串.
 * 說明文件: eros_doc/cvn/畫面標籤(jspf)檔說明.doc
 * <p/>
 * "R":表示 以區間方式輸入
 * "B":表示區間輸入並帶有全部按鈕
 * "S":表示帶有」…」按鈕
 * "*":表示帶有紅色*符號
 * "A":表示插入一個span 顯示文字
 * "C":開啟日曆視窗
 * 這幾種可以組合方式設定
 *    "…"按鈕名稱為」btn」+name
 * 若為區間的話按鈕名稱為
 *    "btn"+name+"1","btn"+name+"2"
 *
 * @author jacky
 * @version Apr 8, 2003 6:34:16 PM
 * @version 2004/08/06 Jerry: Refactor
 */
public class emisSTRLENFormat extends emisValidFormat {
  public emisSTRLENFormat() {
    this.sType_ = "STRLENGTH";
  }

  /**
   * 傳回 input 的樣式. 為了方便測試由protected改成public.
   *
   * @return pattern
   */
  public String getPattern() {
    StringBuffer _sbPattern = new StringBuffer();

    setOnBlur("");  // 使用預設的檢核字串; 檢查長度.
    boolean _isRange = this.sDisplayType_.indexOf("R") >= 0;
    String _sFldNo1 = "";
    if (_isRange) {  // 區間欄位
      _sFldNo1 = "1";
    }
    genInputTag(_sbPattern, _sFldNo1);

    if (_isRange) {  // 區間
      _sbPattern.append("&nbsp;～&nbsp;");
      genInputTag(_sbPattern, "2");

      genInputAll(_sbPattern);
    }  // isRange
    return _sbPattern.toString();
  }
}
