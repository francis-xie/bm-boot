/**

 * Track+[18887] dana.gao 2011/11/03 数量可输入小数点格式

 */

package com.emis.taglibs.showdata;



import javax.servlet.jsp.JspException;

import javax.servlet.jsp.JspTagException;

import javax.servlet.jsp.JspWriter;



public class emisDQty extends emisAbstractFieldTag {



  public int doStartTag()  throws JspException {

    oContext_ = pageContext.getServletContext();

    try {

      if (sName_ == null || sName_.equals("")) {

        throw new JspTagException("invalid null or empty 'name'");

      } else {

        String _sFieldType = "";
        _sFieldType = gettype();
        if(_sFieldType == null || _sFieldType.equals(""))
           _sFieldType = "$DQTY";

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

