package com.emis.taglibs.showdata;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

public class emisBillTag extends emisAbstractFieldTag  {

  public emisBillTag() { }

  public int doStartTag()  throws JspException {
    if (this.sPicture_== null || "".equals(this.sPicture_) ){
      this.sPicture_ = "U";
    }
    oContext_ = pageContext.getServletContext();
    try {
      if (sName_ == null || sName_.equals("")) {
        throw new JspTagException("invalid null or empty 'name'");
      } else {
        String _sFieldType = sName_;
        _sFieldType ="$BILL";

        emisValidFormat _oFormat = getFieldFormat(_sFieldType);

        JspWriter out = super.pageContext.getOut();

        out.println( _oFormat.getPattern() );
      }
    } catch (Exception ex) {
      throw new JspTagException(ex.getMessage());
    }
    return 0;
  }

}