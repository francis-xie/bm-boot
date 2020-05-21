/* $Id: emisText.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/**
 * text attribute.
 */
public class emisText extends emisAbstractFieldTag {
  public emisText() { }

  /**
   * strart tag.
   * @return
   * @throws JspException
   */
  public int doStartTag() throws JspException {
    oContext_ = pageContext.getServletContext();
    try {
      if (sName_ == null || sName_.equals("")) {
        throw new JspTagException("invalid null or empty 'name'");
      } else {
        String _sFieldType = sName_;
        if (sType_ != null && !"".equals(sType_)) {
          _sFieldType = sType_;
        }

        emisValidFormat _oFormat = getFieldFormat(_sFieldType);
        JspWriter out = super.pageContext.getOut();
        if (_oFormat != null) {
          out.println(_oFormat.getPattern());
        } else {
          out.println("<!-- get emisValidFormat failed: " +
              "name=" + sName_ + " Type=" + sType_  + "-->");
        }
      }
    } catch (Exception ex) {
      throw new JspTagException("name=" + sName_ + " Type=" + sType_ +
          " emisText:" +  ex.getMessage());
    }
    return 0;
  }
}