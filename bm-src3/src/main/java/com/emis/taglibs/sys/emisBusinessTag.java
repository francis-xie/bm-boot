package com.emis.taglibs.sys;
import com.emis.business.emisBusiness;
import com.emis.business.emisBusinessMgr;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Title:
 * Description: 透過標籤執行使用者選定XML中的商務邏輯動作
 * Copyright:    Copyright (c) 2001
 * Company: EMIS
 * @author emis Jacky
 * @version 1.0
 */

public class emisBusinessTag extends TagSupport{
  private String sAction_=null;
  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      ServletContext _oContext = pageContext.getServletContext();
      HttpServletRequest _oRequest = (HttpServletRequest) pageContext.getRequest();
      emisUser _oUser = emisCertFactory.getUser(_oContext,_oRequest);
      String _sTitle = (String) _oRequest.getParameter("TITLE");
      if (_sTitle == null) {
        _sTitle = (String) pageContext.getAttribute("TITLE");
      }
      emisBusiness _oBusiness = emisBusinessMgr.get(_oContext,_sTitle,_oUser);
      _oBusiness.setWriter(_out);
      _oBusiness.setParameter(_oRequest);
      if (sAction_ != null)
        _oBusiness.process(sAction_);
      else
        _oBusiness.process();


    } catch(Exception e){
      System.out.println("Process Error");
    }
    return(SKIP_BODY);
  }
  public void setaction(String sAction){
    sAction_ = sAction;
  }
}