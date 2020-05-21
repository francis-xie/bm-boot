package com.emis.taglibs.showdata;
import com.emis.db.emisSQLCache;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
/**
 * Title:   產生下拉式選單
 * Description:    由資料庫內取得對應的下拉式選單資料
 * Copyright:    Copyright (c) 2001
 * Company:     EMIS
 * @author     Jacky
 * @version 1.0
 */
public class emisSqlcache extends TagSupport{
  private String sSqlName_ ;
  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      ServletContext _oContext = pageContext.getServletContext();
      HttpServletRequest _oRequest = (HttpServletRequest) pageContext.getRequest();
      emisUser _oUser = emisCertFactory.getUser(_oContext, _oRequest);

      if ( sSqlName_ != null ) {
        _out.write(emisSQLCache.getSQL(_oContext, sSqlName_, _oUser));
       }
  } catch(Exception e){
      System.out.println("SQLCache Error");
    }
    return(SKIP_BODY);
  }

  public String getname() {
    return sSqlName_;
  }

  public void setname(String sSqlName) {
    this.sSqlName_ = sSqlName;
  }
}