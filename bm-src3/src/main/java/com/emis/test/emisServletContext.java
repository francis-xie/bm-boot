/*

 * $Header: /repository/src3/src/com/emis/test/emisServletContext.java,v 1.1.1.1 2005/10/14 12:43:10 andy Exp $

 *

 * Copyright (c) EMIS Corp.

 */

package com.emis.test;



import javax.servlet.RequestDispatcher;

import javax.servlet.Servlet;

import javax.servlet.ServletContext;

import java.io.InputStream;

import java.net.URL;

import java.util.Enumeration;

import java.util.HashSet;

import java.util.Hashtable;

import java.util.Set;



public class emisServletContext implements ServletContext {

  Hashtable oInitParam_ = new Hashtable();

  Hashtable oAttribute_ = new Hashtable();

  

  public emisServletContext() {

    // do nothing

  }

  

  public void setInitParam(String sParam,String sValue) {

    oInitParam_.put(sParam,sValue);

  }

  

  public String getServerInfo() {

    return "emis ServletContext Emulate Context Object";

  }

  

  public int getMajorVersion() {

    return 1;

  }

  

  public int getMinorVersion() {

    return 0;

  }

  

  public String getInitParameter(String sParam) {

    return (String) oInitParam_.get(sParam);

  }

  

  public Enumeration getInitParameterNames() {

    return oInitParam_.keys();

  }

  

  public ServletContext getContext(String parm1) {

    return this;

  }

  public String getContextPath() {
    return null;
  }


  public String getRealPath(String sParam) {

    return sParam;

  }

  

  public RequestDispatcher getRequestDispatcher(String parm1) {

    return null;

  }

  public RequestDispatcher getNamedDispatcher(String parm1) {

    return null;

  }

  public String getMimeType(String parm1) {

    throw new java.lang.UnsupportedOperationException("Method getMimeType() not yet implemented.");

  }

  public Object getAttribute(String sParam) {

    return oAttribute_.get(sParam);

  }

  

  public Enumeration getAttributeNames() {

    return oAttribute_.keys();

  }

  

  public void setAttribute(String sParam,Object oObj) {

    oAttribute_.put(sParam,oObj);

  }

  

  public void removeAttribute(String sParam) {

    oAttribute_.remove(sParam);

  }

  

  public void log(String parm1) {

  }

  public void log(String parm1, Throwable parm2) {

  }

  public InputStream getResourceAsStream(String parm1) {

    throw new java.lang.UnsupportedOperationException("Method getResourceAsStream() not yet implemented.");

  }

  public Servlet getServlet(String parm1) throws javax.servlet.ServletException {

    throw new java.lang.UnsupportedOperationException("Method getServlet() not yet implemented.");

  }

  public Enumeration getServlets() {

    throw new java.lang.UnsupportedOperationException("Method getServlets() not yet implemented.");

  }

  public Enumeration getServletNames() {

    throw new java.lang.UnsupportedOperationException("Method getServletNames() not yet implemented.");

  }

  public void log(Exception parm1, String parm2) {

    throw new java.lang.UnsupportedOperationException("Method log() not yet implemented.");

  }

  public String getServletContextName() {

    return "com.emis.test.emisServletContext";

  }

  public Set getResourcePaths(String path) {

    return new HashSet();

  }

  public URL getResource(String parm1) throws java.net.MalformedURLException {

    throw new java.lang.UnsupportedOperationException("Method getResource() not yet implemented.");

  }

}