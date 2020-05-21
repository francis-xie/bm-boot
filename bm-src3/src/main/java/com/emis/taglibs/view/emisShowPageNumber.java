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

public class emisShowPageNumber extends TagSupport{

  public int doStartTag() {
    try {
      JspWriter out = pageContext.getOut();
      out.println("第<span id='idTBLspanPage'>&nbsp;</span>/<span id='idTBLspanTotalPage'>&nbsp;</span>頁");
    } catch(Exception e){
      System.out.println("ShowPageNumberError");
    }
    return(SKIP_BODY);
  }
}