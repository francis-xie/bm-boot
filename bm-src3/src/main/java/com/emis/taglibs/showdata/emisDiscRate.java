/**
 * Created by IntelliJ IDEA.
 * User: jacky
 * Date: Apr 9, 2003
 * Time: 11:28:33 AM
 * To change this template use Options | File Templates.
 */
package com.emis.taglibs.showdata;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

public class emisDiscRate extends emisAbstractFieldTag {

  public int doStartTag()  throws JspException {
    oContext_ = pageContext.getServletContext();
    try {
      if (sName_ == null || sName_.equals("")) {
        throw new JspTagException("invalid null or empty 'name'");
      } else {
        String _sFieldType = "$DISCR";

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
