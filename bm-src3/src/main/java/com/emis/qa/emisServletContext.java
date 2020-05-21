/* $Id: emisServletContext.java 4 2015-05-27 08:13:47Z andy.he $

 *

 * Copyright (c) EMIS Corp.

 */

package com.emis.qa;



import javax.servlet.RequestDispatcher;

import javax.servlet.Servlet;

import javax.servlet.ServletContext;

import java.io.InputStream;

import java.net.URL;

import java.util.Enumeration;

import java.util.HashSet;

import java.util.Hashtable;

import java.util.Set;



/**

 * 模擬ServletContext.

 *

 * @author Robert

 * @version $Revision: 71118 $

 * @version 2004/06/28 Jerry: 增加註解; 將Instance變數變成private

 */

public class emisServletContext implements ServletContext {

  private Hashtable oInitParam_ = new Hashtable();

  private Hashtable oAttribute_ = new Hashtable();



  /**

   * empty.

   */

  public emisServletContext() {

    // do nothing

  }



  /**

   * 設啟始參數.

   * @param sParam

   * @param sValue

   */

  public void setInitParam(String sParam, String sValue) {

    oInitParam_.put(sParam, sValue);

  }



  /**

   * Server Information.

   * @return

   */

  public String getServerInfo() {

    return "emis ServletContext Emulate Context Object";

  }



  /**

   * Major version.

   * @return

   */

  public int getMajorVersion() {

    return 1;

  }



  /**

   * Minor version.

   * @return

   */

  public int getMinorVersion() {

    return 2;



  }



  /**

   * getInitParam.

   * @param sParam

   * @return

   */

  public String getInitParameter(String sParam) {

    return (String) oInitParam_.get(sParam);

  }



  /**

   *

   * @return

   */

  public Enumeration getInitParameterNames() {

    return oInitParam_.keys();



  }



  /**

   *

   * @param parm1

   * @return

   */

  public ServletContext getContext(String parm1) {

    return this;

  }

  public String getContextPath() {
    return null;  
  }


  /**

   *

   * @param sParam

   * @return

   */

  public String getRealPath(String sParam) {

    return sParam;

  }



  /**

   *

   * @param parm1

   * @return

   */

  public RequestDispatcher getRequestDispatcher(String parm1) {

    return null;

  }



  /**

   *

   * @param parm1

   * @return

   */

  public RequestDispatcher getNamedDispatcher(String parm1) {

    return null;

  }



  /**

   *

   * @param parm1

   * @return

   */

  public String getMimeType(String parm1) {

    throw new java.lang.UnsupportedOperationException(

        "Method getMimeType() not yet implemented.");

  }



  /**

   *

   * @param sParam

   * @return

   */

  public Object getAttribute(String sParam) {

    return oAttribute_.get(sParam);

  }



  /**

   *

   * @return

   */

  public Enumeration getAttributeNames() {

    return oAttribute_.keys();

  }



  /**

   *

   * @param sParam

   * @param oObj

   */

  public void setAttribute(String sParam, Object oObj) {

    oAttribute_.put(sParam, oObj);

  }



  /**

   *

   * @param sParam

   */

  public void removeAttribute(String sParam) {

    oAttribute_.remove(sParam);

  }



  /**

   *

   * @param parm1

   */

  public void log(String parm1) {

  }



  /**

   *

   * @param parm1

   * @param parm2

   */

  public void log(String parm1, Throwable parm2) {

  }



  /**

   *

   * @param parm1

   * @return

   * @throws java.net.MalformedURLException

   */

  public URL getResource(String parm1) throws java.net.MalformedURLException {

    throw new java.lang.UnsupportedOperationException(

        "Method getResource() not yet implemented.");

  }



  /**

   *

   * @param parm1

   * @return

   */

  public InputStream getResourceAsStream(String parm1) {

    throw new java.lang.UnsupportedOperationException(

        "Method getResourceAsStream() not yet implemented.");

  }



  /**

   *

   * @param parm1

   * @return

   * @throws javax.servlet.ServletException

   */

  public Servlet getServlet(String parm1) throws javax.servlet.ServletException {

    throw new java.lang.UnsupportedOperationException(

        "Method getServlet() not yet implemented.");

  }



  /**

   *

   * @return

   */

  public Enumeration getServlets() {

    throw new java.lang.UnsupportedOperationException(

        "Method getServlets() not yet implemented.");

  }



  /**

   *

   * @return

   */

  public Enumeration getServletNames() {

    throw new java.lang.UnsupportedOperationException(

        "Method getServletNames() not yet implemented.");

  }



  /**

   *

   * @param parm1

   * @param parm2

   */

  public void log(Exception parm1, String parm2) {

    throw new java.lang.UnsupportedOperationException(

        "Method log() not yet implemented.");

  }



  /**

   *

   * @return

   */

  public String getServletContextName() {

    return "com.emis.qa.emisServletContext";

  }



  /**

   *

   * @param path

   * @return

   */

  public Set getResourcePaths(String path) {

    return new HashSet();

  }

}