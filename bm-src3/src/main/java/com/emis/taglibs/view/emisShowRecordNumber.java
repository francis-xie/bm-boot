package com.emis.taglibs.view;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class emisShowRecordNumber extends TagSupport{

  public int doStartTag() {
    try {
      JspWriter out = pageContext.getOut();
      out.println("<span id='idTBLspanCurRecord'></span>/<span id='idTBLspanRecord'>&nbsp;</span>筆");
    } catch(Exception e){
      System.out.println("ShowRecordNumberError");
    }
    return(SKIP_BODY);
  }
}