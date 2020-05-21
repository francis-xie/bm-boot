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
 * @author
 * @version 1.0
 */

public class emisShowRecordNumber extends TagSupport{

  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      String _sSpanRecord = "idTBLspanRecord";
      String _sSpanCurrRecord = "idTBLspanCurRecord";
      ServletContext _oContext = pageContext.getServletContext();
      emisTracer _oTrace;
      _oTrace = emisTracer.get(_oContext);
      String _sRecords = _oTrace.getMsg(emisMessage.MSG_SHOWDATA_PEN);
      _out.write("<span id=\"" + _sSpanCurrRecord + "\"></span>/<span id=\"" + _sSpanRecord + "\">&nbsp;</span>" + _sRecords);

    } catch(Exception e){
      System.out.println("ShowRecordNumberError");
    }
    return(SKIP_BODY);
  }
}