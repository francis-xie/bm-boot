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
 * @author    emis Jacky
 * @version 1.0
 */
public class emisDataPageSize extends TagSupport{

  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      String _sPageSizeSpan = (String) "idTBLspanPageSize";
      ServletContext _oContext = pageContext.getServletContext();
      emisTracer _oTrace;
      _oTrace = emisTracer.get(_oContext);
      String _sEachPage = _oTrace.getMsg(emisMessage.MSG_SHOWDATA_EACHPAGE);
      String _sRecords = _oTrace.getMsg(emisMessage.MSG_SHOWDATA_PEN);

      _out.write(_sEachPage + "<input id='" + _sPageSizeSpan + "' type=text maxlength='3' size='2' onblur=\"emisSpanPageSize(idTBLRec,this);\"></input>" + _sRecords);
    } catch(Exception e){
      System.out.println("DataPageSizeError");
    }
    return(SKIP_BODY);
  }
}