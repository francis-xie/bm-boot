package com.emis.taglibs.showdata;
import com.emis.trace.emisMessage;
import com.emis.trace.emisTracer;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author  emisJacky
 * @version 1.0
 */

public class emisShowPageNumber extends TagSupport{

  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      String _sSpanPage = "idTBLspanPage";
      String _sSpanTotalPage = "idTBLspanTotalPage";
      ServletContext _oContext = pageContext.getServletContext();
      emisTracer _oTrace;
      _oTrace = emisTracer.get(_oContext);
      String _sThe = _oTrace.getMsg(emisMessage.MSG_SHOWDATA_THE);
      String _sPage = _oTrace.getMsg(emisMessage.MSG_SHOWDATA_PAGE);
      _out.write(_sThe + "<span id=\"" + _sSpanPage + "\">&nbsp;</span>/<span id=\"" + _sSpanTotalPage + "\">&nbsp;</span>" + _sPage);
    } catch(Exception e){
      System.out.println("ShowPageNumberError");
    }
    return(SKIP_BODY);
  }
}