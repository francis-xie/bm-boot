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

public class emisDataPageSize extends TagSupport{

  public int doStartTag() {
    try {
      JspWriter out = pageContext.getOut();
      out.println("每頁<input id='idTBLspanPageSize' type=text maxlength='3' size='2' onblur='emisSpanPageSize(idTBLRec,this);'></input>筆 ");
    } catch(Exception e){
      System.out.println("DataPageSizeError");
    }
    return(SKIP_BODY);
  }
}