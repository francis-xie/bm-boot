// $Header: /repository/sme_gl/src/src3/com/emis/taglibs/showdata/emisDefaultValues.java,v 1.1 2009/06/12 03:51:02 sea.zhou Exp $
package com.emis.taglibs.showdata;

import com.emis.db.emisEsysDefVal;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 產生預設值的 javaScript 代碼.
 *
 * @author Ben
 * @version 1.0
 */
public class emisDefaultValues extends TagSupport {
  private String keys = "";
  private String func = "";

  /**
   * start tag.
   *
   * @return int
   */
  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      ServletContext _oContext = pageContext.getServletContext();
      _out.println("<script>");
      _out.println(emisEsysDefVal.getInstance(_oContext).getScript(keys, func));
      _out.println("</script>");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return (SKIP_BODY);
  }

  /**
   * attribute of showdata.tld
   *
   * @return keys
   */
  public String getKeys() {
    return keys;
  }

  /**
   * attribute of showdata.tld
   *
   * @param sKeys KEYS
   */
  public void setKeys(String sKeys) {
    this.keys = sKeys;
  }

  /**
   * attribute of showdata.tld
   *
   * @return func
   */
  public String getFunc() {
    return func;
  }

  /**
   * attribute of showdata.tld
   *
   * @param func
   */
  public void setFunc(String func) {
    this.func = func;
  }

}
