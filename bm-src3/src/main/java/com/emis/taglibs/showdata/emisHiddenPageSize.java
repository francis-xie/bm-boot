package com.emis.taglibs.showdata;
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

public class emisHiddenPageSize extends TagSupport{

  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      String _sPageSizeSpan = "idTBLspanPageSize";
      _out.write("<input id='" + _sPageSizeSpan + "' type=hidden></input>");
    } catch(Exception e){
      System.out.println("HiddenDataPageSizeError");
    }
    return(SKIP_BODY);
  }
}