package com.emis.taglibs.i18n;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.ResourceBundle;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class emisMessageResource extends TagSupport{
  private String SysID_ = null;
  private String sKey_ = null;
  public int doStartTag() {
    JspWriter _out = pageContext.getOut();
    try {
      //ServletRequest _oRequest = pageContext.getRequest();
      ResourceBundle _oMyResource =
            ResourceBundle.getBundle("com.emis.messageResource.emisMessageBox");
      String _sContext ;
      //_out.print("key=" + sKey_);
      _sContext = _oMyResource.getString(sKey_);
      //_sContext = new String(_oMyResource.getString(sKey_).getBytes("ISO8859-1"),emisUtil.FILENCODING);
      if (_sContext == null){
        _sContext=sKey_;
      }

      _out.print(_sContext);
    } catch(Exception e){}
    return(SKIP_BODY);
  }
  public void setKey(String sKey) {
    sKey_ = sKey;
  }

}