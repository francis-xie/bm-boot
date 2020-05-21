/* $Header: /repository/src3/src/com/emis/business/emisFormBean.java,v 1.1.1.1 2005/10/14 12:41:53 andy Exp $
 * @author unknown
 * 2003/09/02 Jerry: 加入傳回Enumeration的method
 * 2004/07/14 Jerry: 加入傳回Set的keySet( )
 */
package com.emis.business;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

/**
 * 此class負責儲存Form的參數資料
 */
public class emisFormBean {
  private HttpServletRequest request_;
  private HashMap oMap_ = new HashMap();

  public emisFormBean(HttpServletRequest request) {
    this.request_ = request;

    Enumeration e = request.getParameterNames();
    while (e.hasMoreElements()) {
      Object key = e.nextElement();
      Object value = request.getParameter((String) key);
      if (value != null)
        oMap_.put(key, value);
    }
  }

  public void setParameter(String name, String value) {
    if (name == null) name = "";
    if (value == null) value = "";
    oMap_.put(name, value);
  }

  public String getParameter(String name) {
    if (name == null)
      return null;

    else
      return (String) oMap_.get(name);
  }

  /**
   * 傳回Enumeration, for debugging output.
   * @return Enumeration parameterNames
   */
  public Enumeration getParameterNames() {
    return request_.getParameterNames();
  }

  /**
   * 傳回Set.
   * @return
   */
  public Set keySet() {
    return oMap_.keySet();
  }
}