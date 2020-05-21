/* $Id: emisShowDate.java 10557 2017-12-28 06:34:46Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

import com.emis.db.emisProp;
import com.emis.util.emisUtil;
import com.emis.util.emisLangRes;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/**
 * Generate date tag.
 * <p/>
 * Created by IntelliJ IDEA.
 *
 * @author jeff
 * @version 2004/08/06 Jerry: Refactor, change += to append
 */
public class emisShowDate extends emisAbstractFieldTag {
  private String sMaxLen_ = "9"; // 預設為民國之日期格式
  private String sValid_ = "";

  /**
   * start tag.
   *
   * @return
   * @throws JspException
   */
  public int doStartTag() throws JspException {
    try {
      oContext_ = this.pageContext.getServletContext();
      // 实现多语
      if (this.sBundle != null && !"".equals(this.sBundle)
          && this.sKey != null && !"".equals(this.sKey)) {
        emisLangRes lang = emisLangRes.getInstance(oContext_);
        lang.setLanguage((String) pageContext.getSession().getAttribute("languageType"));
        this.setmessage(lang.getMessage(this.sBundle, this.sKey));
      }
      emisDateFormat format = getDateFormat();

      String _sPattern = format.getPattern();
      JspWriter out = super.pageContext.getOut();
      out.println(_sPattern);
    } catch (Exception ex) {
      throw new JspTagException(ex.getMessage());
    }
    return 0;
  }

  /**
   * 傳回emisDateFormat. 開放供測試程式使用.
   *
   * @return
   */
  public emisDateFormat getDateFormat() {
    sValid_ = " onblur=\"emisDateValid(this,'" + this.sMessage_ + "'," +
        this.sPage_ + ")  " +
        "&& (emisOnblurAfter(this)) \"";  //2005/12/01 add by andy
    if (isAmericanDateFormat()) {
      getUS_DateFormat();  // 西元日期格式
    } else {
      getROC_DateFormat();  // 中式日期格式
    }

    if (sSize_ == null || "".equals(sSize_)) {
      sSize_ = sMaxLen_;
    }

    String _sNeedCalendar = "N";
    try {
      emisProp prop = emisProp.getInstance(oContext_);
      if (prop != null) {
        _sNeedCalendar = prop.get("EROS_DATECALENDAR");
        if (_sNeedCalendar != null && "Y".equalsIgnoreCase(_sNeedCalendar)) {
          if (!"YM".equalsIgnoreCase(sType_) && !"Y".equalsIgnoreCase(sType_) ) { 
            this.sDisplayType_ = this.sDisplayType_ + "C";  // add "C"alendar
          }
        }
      }
    } catch (Exception e) {
      ;
    }
    emisDateFormat format = new emisDateFormat();
    format.setFieldName(sName_);
    format.setPicture("D");
    format.setMaxLen(sMaxLen_);
    format.setSize(sSize_);
    format.setDisplayType(this.sDisplayType_);
    format.setOnBlur(sValid_);
    // 实现多语
    format.setContext(this.oContext_);
    format.setLanguage((String) pageContext.getSession().getAttribute("languageType"));
    format.sType_ = this.sType_;
    return format;
  }

  private boolean isAmericanDateFormat() {
    String _sDateType = getDateType();
    return "US".equalsIgnoreCase(_sDateType) || "Y".equalsIgnoreCase(_sDateType);
  }

  private void getUS_DateFormat() {
    sMaxLen_ = "10";
    boolean _isCalendar = true;  // 能否指定日曆之選項(displayType)
    if ("YM".equalsIgnoreCase(sType_)) {
      sMaxLen_ = "7";
      sValid_ = " onblur=\"emisMonthValid(this,'" + this.sMessage_ + "'," +
          this.sPage_ + ") " +
          "&& (emisOnblurAfter(this)) \"";  //2005/12/01 add by andy
      _isCalendar = false;
    } else if ("Y".equalsIgnoreCase(sType_)) {
      sMaxLen_ = "4";
      sValid_ = " onblur=\"emisYearValid(this,'" + this.sMessage_ + "'," +
          this.sPage_ + ") " +
          "&& (emisOnblurAfter(this)) \"";  //2005/12/01 add by andy
      _isCalendar = false;
    }
    // 日期格式是YM或Y, 且有指定C時刪去C, 使之不能開啟日曆視窗
    if (!_isCalendar && this.sDisplayType_.indexOf("C") >= 0) {
      this.sDisplayType_ = emisUtil.stringReplace(this.sDisplayType_,"C","","ia");
    }
  }

  private void getROC_DateFormat() {
    sMaxLen_ = "9";
    if ("YM".equalsIgnoreCase(sType_)) {
      sMaxLen_ = "6";
      sValid_ = " onblur=\"emisMonthValid(this,'" + this.sMessage_ + "'," +
          this.sPage_ + ") " +
          "&& (emisOnblurAfter(this)) \"";  //2005/12/01 add by andy
    } else {
      if ("Y".equalsIgnoreCase(sType_)) {
        sMaxLen_ = "3";
        sValid_ = " onblur=\"emisPadl(this.value," + sMaxLen_ + ",'0', true) " +
           "&& (emisOnblurAfter(this)) \"";  //2005/12/01 add by andy
      }
    }
  }

  /**
   * 傳回日期格式, 定義於EmisProp.EPOS_DATETYPE, "US"或"ROC".
   *
   * @return
   */
  private String getDateType() {
    try {
      ServletContext _oContext = pageContext.getServletContext();
      emisProp oProp = emisProp.getInstance(_oContext);
      return oProp.get("EPOS_DATETYPE");
    } catch (Exception e) {
      return "US";  // 預設日期格式為"ROC"
    }
  }
}
