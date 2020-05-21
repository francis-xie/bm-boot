package com.emis.taglibs.showdata;
import com.emis.util.emisHtml;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
/**
 * Title:   產生動態下拉式選單
 * Description:    由資料庫內取得對應的下拉式選單資料
 * Copyright:    Copyright (c) 2001
 * Company:     EMIS
 * @author     Jacky
 * @version 1.0
 */
public class emisHtmlOption extends TagSupport{
  private String sSQL_=null;
  private String sPattern_ = "%1 %2" ;
  private String sValue_ ="%1";

  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      ServletContext _oContext = pageContext.getServletContext();
      if (sSQL_!=null)
        _out.write(emisHtml.option(_oContext, sSQL_, sPattern_, sValue_));
  } catch(Exception e){
      System.out.println("HtmlOptionError");
    }
    return(SKIP_BODY);
  }

  public String getsql() {
    return sSQL_;
  }

  public void setsql(String sSQL) {
    this.sSQL_ = sSQL;
  }

  public String getpattern() {
    return sPattern_;
  }

  public void setpattern(String sPattern) {
    this.sPattern_ = sPattern;
  }

  public String getvalue() {
    return sValue_;
  }

  public void setvalue(String sValue) {
    this.sValue_ = sValue;
  }
}