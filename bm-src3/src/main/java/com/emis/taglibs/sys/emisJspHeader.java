package com.emis.taglibs.sys;
import com.emis.user.emisCertFactory;
import com.emis.user.emisPermission;
import com.emis.user.emisUser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Title: JSP檔頭載入功能
 * Description:透過此一標籤能夠決定使用者的權限及
 * Copyright:    Copyright (c) 2001
 * Company: EMIS
 * @author: EMIS Jacky
 * @version 1.0
 */

public class emisJspHeader extends TagSupport{
  private String sTitle_ = null;
  private String sSysID_=null;
  public int doStartTag() {
    try {
      ServletContext _oContext = pageContext.getServletContext();
      HttpServletRequest _oRequest = (HttpServletRequest) pageContext.getRequest();
      JspWriter out = pageContext.getOut();
        emisUser _oUser = emisCertFactory.getUser(_oContext,_oRequest);
      if (sTitle_ != null)
        pageContext.setAttribute("TITLE", sTitle_);

      // 設定新增、修改...權限
      emisPermission _oPermission = _oUser.getMenuPermission(sSysID_);
    } catch(Exception e){
      System.out.println("Header Error");
    }
    return(SKIP_BODY);
  }
  public void settitle(String sTitle){
    sTitle_ = sTitle;
  }
  public void setsysid(String sSysID){
    sSysID_ = sSysID;
  }
}