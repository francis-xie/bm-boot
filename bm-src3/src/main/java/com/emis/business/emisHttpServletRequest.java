/*
 * $Id: emisHttpServletRequest.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) EMIS corp.
 */
package com.emis.business;

import com.emis.util.emisCommonEnum;
import com.emis.util.emisUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * emisHttpServletRequest 是一個 Wrapper 的 class ,
 * implement HttpServletRequest Interface
 * 把 ServletEngine 傳進的 ServletRequest 放在 member 中
 * 對於不存在的 ParameterName 傳回空字串,而不是 null
 *
 * @author Robert
 * @version 2004/07/07 Jerry: 改變預設值供測試時用
 */
public class emisHttpServletRequest implements HttpServletRequest {

  private HashMap oMap_ = new HashMap();
  HttpServletRequest request_;

  public emisHttpServletRequest() {

  }

  public emisHttpServletRequest(HttpServletRequest request) {
    request_ = request;
    Enumeration e = request.getParameterNames();
    while (e.hasMoreElements()) {
      Object key = e.nextElement();
      Object value = request.getParameter((String) key);
      if (value != null)
        oMap_.put(key, value);
    }
  }

  public HttpServletRequest getHttpRequest() {
    return request_;
  }

  public String getAuthType() {
    if (request_ == null) return "";
    return request_.getAuthType();
  }

  public Cookie[] getCookies() {
    if (request_ == null) return null;
    return request_.getCookies();
  }

  public long getDateHeader(String name) {
    if (request_ == null) return 0;
    return request_.getDateHeader(name);
  }

  public String getHeader(String name) {
    if (request_ == null) return "";
    return request_.getHeader(name);
  }

  public Enumeration getHeaders(String name) {
    if (request_ == null) return new emisCommonEnum();
    return request_.getHeaders(name);
  }

  public Enumeration getHeaderNames() {
    if (request_ == null) return new emisCommonEnum();
    return request_.getHeaderNames();
  }

  public int getIntHeader(String name) {
    if (request_ == null) return 0;
    return request_.getIntHeader(name);
  }

  public String getMethod() {
    if (request_ == null) return "";
    return request_.getMethod();
  }

  public String getPathInfo() {
    if (request_ == null) return "";
    return request_.getPathInfo();
  }

  public String getPathTranslated() {
    if (request_ == null) return "";
    return request_.getPathTranslated();
  }

  public String getContextPath() {
    if (request_ == null) return "";
    return request_.getContextPath();
  }

  public String getQueryString() {
    if (request_ == null) return "";
    return request_.getQueryString();
  }

  public String getRemoteUser() {
    if (request_ == null) return "";
    return request_.getRemoteUser();
  }

  public boolean isUserInRole(String role) {
    if (request_ == null) return false;
    return request_.isUserInRole(role);
  }

  public java.security.Principal getUserPrincipal() {
    if (request_ == null) return null;
    return request_.getUserPrincipal();
  }

  public String getRequestedSessionId() {
    if (request_ == null) return "SESSIONID";
    return request_.getRequestedSessionId();
  }

  public String getRequestURI() {
    if (request_ == null) return "URI";
    return request_.getRequestURI();
  }

  public String getServletPath() {
    if (request_ == null) return "PATH";
    return request_.getServletPath();
  }

  public HttpSession getSession(boolean create) {
    if (request_ == null) return null;
    return request_.getSession(create);
  }

  public HttpSession getSession() {
    if (request_ == null) return null;
    return request_.getSession();
  }

  public boolean isRequestedSessionIdValid() {
    if (request_ == null) return false;
    return request_.isRequestedSessionIdValid();
  }

  public boolean isRequestedSessionIdFromCookie() {
    if (request_ == null) return false;
    return request_.isRequestedSessionIdFromCookie();
  }

  public boolean isRequestedSessionIdFromURL() {
    if (request_ == null) return false;
    return request_.isRequestedSessionIdFromURL();
  }

  public boolean isRequestedSessionIdFromUrl() {
    if (request_ == null) return false;
    // abel modify
    return request_.isRequestedSessionIdFromURL();
    //return request_.isRequestedSessionIdFromUrl();
  }

  public Object getAttribute(String name) {
    if (request_ == null) return null;
    return request_.getAttribute(name);
  }

  public Enumeration getAttributeNames() {
    if (request_ == null) return null;
    return request_.getAttributeNames();
  }

  public String getCharacterEncoding() {
    if (request_ == null) return emisUtil.FILENCODING;
    return request_.getCharacterEncoding();
  }

  public int getContentLength() {
    if (request_ == null) return 0;
    return request_.getContentLength();
  }


  public String getContentType() {
    if (request_ == null) return "text/html";
    return request_.getContentType();
  }


  public ServletInputStream getInputStream() throws IOException {
    if (request_ == null) return null;
    return request_.getInputStream();
  }

  public void setParameter(String name, String value) {
    if (value == null) value = "";  // the key or value can't be null
    oMap_.put(name, value);
  }

  public void setParameter(HashMap oMap) {
    if(oMap != null)
     oMap_.putAll(oMap);
  }

  public String getParameter(String name) {
    return (String) oMap_.get(name);
  }


  public Enumeration getParameterNames() {
    emisCommonEnum e = new emisCommonEnum();
    Iterator it = oMap_.keySet().iterator();
    while (it.hasNext()) {
      e.add(it.next());
    }
    return e;
  }

  /**
   * ?a=hello&a=world
   * request.getParameterValues("a") 應傳回
   * ["hello","world"],但我們應該不會有
   * 這樣的情形所以不 support 這個 method
   * 固定傳回和 getParameter 一樣的值
   */
  public String[] getParameterValues(String name) {
    String _sValue = (String) oMap_.get(name);
    if (_sValue != null) {
      String[] array = new String[1];
      array[0] = _sValue;
      return array;
    } else {
      return null;
    }
  }

  public void clearParameter() {
    oMap_.clear();
  }

  public String getProtocol() {
    if (request_ == null) return "http1.1";
    return request_.getProtocol();
  }

  public String getScheme() {
    if (request_ == null) return "";
    return request_.getScheme();
  }

  public String getServerName() {
    if (request_ == null) return "localhost";  //"emis.pseudo.server";
    return request_.getServerName();
  }

  public int getServerPort() {
    if (request_ == null) return 80;  // 8080;
    return request_.getServerPort();
  }

  public BufferedReader getReader() throws IOException {
    if (request_ == null) return null;
    return request_.getReader();
  }

  public String getRemoteAddr() {
    if (request_ == null) return "127.0.0.1";
    return request_.getRemoteAddr();
  }

  public String getRemoteHost() {
    if (request_ == null) return "remote.host";
    return request_.getRemoteHost();
  }

  public void setAttribute(String name, Object o) {
    if (request_ == null) {
      oMap_.put(name, o);
      return;
    }
    request_.setAttribute(name, o);
  }


  public void removeAttribute(String name) {
    if (request_ == null) {
      oMap_.remove(name);
      return;
    }
    request_.removeAttribute(name);
  }

  public Locale getLocale() {
    if (request_ == null) return null;
    return request_.getLocale();
  }


  public Enumeration getLocales() {
    if (request_ == null) return null;
    return request_.getLocales();
  }


  public boolean isSecure() {
    if (request_ == null) return false;
    return request_.isSecure();
  }

  public RequestDispatcher getRequestDispatcher(String path) {
    if (request_ == null) return null;
    return request_.getRequestDispatcher(path);
  }

  public String getRealPath(String path) {
    if (request_ == null) return "";
    return request_.getRealPath(path);
  }
//  新版 resin 要用到

  public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
    request_.setCharacterEncoding(encoding);
  }

  public Map getParameterMap() {
    return request_.getParameterMap();
  }

  public StringBuffer getRequestURL() {
    return request_.getRequestURL();
  }

  /**
   * for JSDK 2.4.
   * @return String
   */
  public int getRemotePort() {
    return request_.getRemotePort();
  }

  /**
   * for JSDK 2.4.
   * @return String
   */
  public String getLocalAddr() {
    return request_.getLocalAddr();
  }

  /**
   * for JSDK 2.4.
   * @return String
   */
  public String getLocalName() {
    return request_.getLocalName();
  }
  /**
   * for JSDK 2.4.
   * @return String
   */
  public int getLocalPort() {
    return request_.getLocalPort();
  }
}