package com.emis.business;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * emisHttpServletResponse 是一個 Wrapper 的 class ,
 * implement HttpServletResponse Interface
 * 把 ServletEngine 傳進的 ServletResponse 放在 member 中
 */


public class emisHttpServletResponse implements HttpServletResponse {
  private HttpServletResponse response_;

  private emisHttpServletResponse(HttpServletResponse response) {
    response_ = response;

  }

  public static emisHttpServletResponse getInstance(HttpServletResponse response) {

    return new emisHttpServletResponse(response);

  }

  public void addCookie(Cookie cookie) {
    response_.addCookie(cookie);
  }

  public void addDateHeader(String name, long date) {
    response_.addDateHeader(name, date);
  }

  public void addHeader(String name, String value) {
    response_.addHeader(name, value);
  }

  public void addIntHeader(String name, int value) {
    response_.addIntHeader(name, value);
  }

  public boolean containsHeader(String name) {
    return response_.containsHeader(name);
  }

  public String encodeRedirectUrl(String url) {
    return response_.encodeRedirectUrl(url);
  }

  public String encodeRedirectURL(String url) {
    return response_.encodeRedirectURL(url);
  }

  public String encodeUrl(String url) {
    return response_.encodeUrl(url);
  }

  public String encodeURL(String url) {
    return response_.encodeURL(url);
  }

  public void sendError(int sc) {
    try {
      response_.sendError(sc);
    } catch (IOException ignore) {
      ;
    }
  }

  public void sendError(int sc, String msg) {
    try {
      response_.sendError(sc, msg);
    } catch (IOException ignore) {
      ;
    }
  }

  public void sendRedirect(String location) {
    try {
      response_.sendRedirect(location);
    } catch (IOException ugnore) {
      ;
    }
  }

  public void setDateHeader(String name, long date) {
    response_.setDateHeader(name, date);
  }

  public void setHeader(String name, String value) {
    response_.setHeader(name, value);
  }

  public void setIntHeader(String name, int value) {
    //   try{
    response_.setIntHeader(name, value);
    //   }catch(IOException ignore){;}
  }

  public void setStatus(int sc) {
    response_.setStatus(sc);
  }

  public void setStatus(int sc, String sm) {
    response_.setStatus(sc, sm);
  }

  public void flushBuffer() {
    try {
      response_.flushBuffer();
    } catch (IOException ignore) {
      ;
    }
  }

  public int getBufferSize() {
    return response_.getBufferSize();
  }

  public String getCharacterEncoding() {
    return response_.getCharacterEncoding();
  }

  public Locale getLocale() {
    return response_.getLocale();
  }

  public ServletOutputStream getOutputStream() {
    ServletOutputStream oServletOutputStream = null;
    try {
      oServletOutputStream = response_.getOutputStream();
    } catch (IOException ignore) {
      ;
    }
    return oServletOutputStream;
  }

  public PrintWriter getWriter() {
    PrintWriter oPrintWriter = null;
    try {
      oPrintWriter = response_.getWriter();
    } catch (IOException ignore) {
      ;
    }
    return oPrintWriter;
  }

  public boolean isCommitted() {
    return response_.isCommitted();
  }

  public void reset() {
    response_.reset();
  }

  public void resetBuffer() {
    response_.resetBuffer();
  }

  public void setBufferSize(int size) {
    response_.setBufferSize(size);
  }

  public void setContentLength(int len) {
    response_.setContentLength(len);
  }

  public void setContentType(String type) {
    response_.setContentType(type);
  }

  public void setLocale(Locale loc) {
    response_.setLocale(loc);
  }

  public HttpServletResponse getEmisResponse() {
    return response_;
  }

  /**
   * for JSDK 2.4.
   * @return String
   */
  public String getContentType() {
    return response_.getContentType();
  }

  /**
   * for JSDK 2.4.
   */
  public void setCharacterEncoding(String string) {
    response_.setCharacterEncoding(string);
  }
}