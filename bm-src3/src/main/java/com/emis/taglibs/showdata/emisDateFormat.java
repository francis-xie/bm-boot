/* $Id: emisDateFormat.java 10557 2017-12-28 06:34:46Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;



/**
 * Date HTML格式字串的產生類別.
 * 說明文件: eros_doc/cvn/畫面標籤(jspf)檔說明.doc
 *
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
 * @author Jerry
 * @version 2004/08/07
 */
public class emisDateFormat extends emisValidFormat {
  /**
   * 日期格式的tag字串.
   */
  public emisDateFormat() {
    this.sType_ = "DATE";
    this.sPicture_ = "9";
  }

  protected String getOnBlur() {    
    if ("".equals(this.sValid_)) {
      StringBuffer _sbResult = new StringBuffer();
      _sbResult.append(" onblur=\"emisDateValid(this,'").append(this.sAlertMessage_)
          .append("',").append(this.sPage_).append(") ");
      _sbResult.append("&& (emisOnblurAfter(this)) \"");  // 2005/12/01 andy add
      return _sbResult.toString();
    } else {
      return sValid_;
    }
  }

  /**
   * 傳回&lt;input&gt; tag的onfocus字串.
   *
   * @return
   */
  protected String getOnFocus() {
    String _sType;
    if ("YM".equalsIgnoreCase(sType_)) {
      _sType = "M";
    } else if("Y".equalsIgnoreCase(sType_)) {
      _sType = "Y";
    } else{
      _sType = "D";
    }
    if ("".equals(sFocus_)) {
      StringBuffer _sbResult = new StringBuffer();
      _sbResult.append("onfocus='emisQrySel(document.getElementById(\"")
          .append(this.sFieldName_).append("1\"), this, \"").append(_sType)
          .append("\")'");
      return _sbResult.toString();
    } else {
      return sFocus_;
    }
  }

  /**
   * 傳回HTML string.
   *
   * @return
   */
  public String getPattern() {
    StringBuffer _sbPattern = new StringBuffer();

    boolean _isRange = this.sDisplayType_.indexOf("R") >= 0;
    String _sFldNo1 = "";
    if (_isRange) {  // 區間欄位
      _sFldNo1 = "1";
      if("YM".equalsIgnoreCase(this.sType_) || "Y".equalsIgnoreCase(this.sType_)) { //不影响旧的处理，为YM和Y时不给name属性（否则有些页面会有重复的name）
        _sbPattern.append("<select id=\"selDateScope").append(this.sFieldName_).append("\"")
            .append(" dateStart='").append(this.sFieldName_).append("1' dateEnd='").append(this.sFieldName_).append("2' onchange='selDateScopeChange(this)'><br/>");
      } else {
        _sbPattern.append("<select name=\"selDateScope\" id=\"selDateScope").append(this.sFieldName_).append("\"")
            .append(" dateStart='").append(this.sFieldName_).append("1' dateEnd='").append(this.sFieldName_).append("2' onchange='selDateScopeChange(this)'><br/>");
      }
      _sbPattern.append("  <option value=\"\"></option><br/>");
      if ("YM".equalsIgnoreCase(this.sType_)) {
        _sbPattern.append("  <option value=\"ym_month\">" + getMessage("MONTH") + "</option><br/>")
            .append("  <option value=\"ym_lastMonth\">" + getMessage("LASTMONTH") + "</option><br/>")
            .append("  <option value=\"ym_threeMonth\">" + getMessage("THREEMONTH") + "</option><br/>")
            .append("  <option value=\"ym_thisYear\">" + getMessage("THISYEAR") + "</option><br/>")
            .append("  <option value=\"ym_lastYear\">" + getMessage("LASTYEAR") + "</option><br/>");
      } else if ("Y".equalsIgnoreCase(this.sType_)) {
        _sbPattern.append("  <option value=\"y_thisYear\">" + getMessage("THISYEAR") + "</option><br/>")
            .append("  <option value=\"y_lastYear\">" + getMessage("LASTYEAR") + "</option><br/>");
      } else {
        _sbPattern.append("  <option value=\"today\">" + getMessage("TODAY") + "</option><br/>")
            .append("  <option value=\"yesterday\">" + getMessage("YESTERDAY") + "</option><br/>")
            .append("  <option value=\"threeDay\">" + getMessage("THREEDAY") + "</option><br/>")
            .append("  <option value=\"week\">" + getMessage("WEEK") + "</option><br/>")
            .append("  <option value=\"lastWeek\">" + getMessage("LASTWEEK") + "</option><br/>")
            .append("  <option value=\"month\">" + getMessage("MONTH") + "</option><br/>")
            .append("  <option value=\"lastMonth\">" + getMessage("LASTMONTH") + "</option><br/>")
            .append("  <option value=\"threeMonth\">" + getMessage("THREEMONTH") + "</option><br/>");
      }
      _sbPattern.append("</select>");
      _sbPattern.append("<span id=\"selDateScopeSpace").append(this.sFieldName_).append("\">&nbsp;</span>");
    } else if("YM".equalsIgnoreCase(this.sType_) || "Y".equalsIgnoreCase(this.sType_)) { //非区间，为年月或年的情况。
      _sbPattern.append("<select id=\"selDateScope").append(this.sFieldName_).append("\"")
          .append(" dateStart='").append(this.sFieldName_).append("' dateEnd='").append(this.sFieldName_).append("' onchange='selDateScopeChange(this)'><br/>");
      _sbPattern.append("  <option value=\"\"></option><br/>");
      if ("YM".equalsIgnoreCase(this.sType_)) {
        _sbPattern.append("  <option value=\"ym_month\">" + getMessage("MONTH") + "</option><br/>")
            .append("  <option value=\"ym_lastMonth\">" + getMessage("LASTMONTH") + "</option><br/>");
      }else if ("Y".equalsIgnoreCase(this.sType_)) {
        _sbPattern.append("  <option value=\"y_thisYear\">" + getMessage("THISYEAR") + "</option><br/>")
            .append("  <option value=\"y_lastYear\">" + getMessage("LASTYEAR") + "</option><br/>");
      }
      _sbPattern.append("</select>");
      _sbPattern.append("<span id=\"selDateScopeSpace").append(this.sFieldName_).append("\">&nbsp;</span>");
    }

    //setOnBlur(getOnBlurString());  // 設定檢核字串
    //setOnFocus(getOnFocusString());
    genInputTag(_sbPattern, _sFldNo1);

    if (_isRange) {  // 區間
      _sbPattern.append("&nbsp;～&nbsp;");
      genInputTag(_sbPattern, "2");

      //-genInputAll(_sbPattern);
    }  // isRange
    return _sbPattern.toString();
  }
}
