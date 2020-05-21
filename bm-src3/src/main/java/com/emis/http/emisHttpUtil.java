/*
 * $Header: /repository/src3/src/com/emis/http/emisHttpUtil.java,v 1.1.1.1 2005/10/14 12:42:10 andy Exp $
 * emisHttpUtil.java
 *
 * Created on 2001年11月3日, 下午 2:32
 * Copyright (c) EMIS Corp.
 */

package com.emis.http; 

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author  jerry
 */
public class emisHttpUtil {
  
  /** Creates new emisHttpUtil */
  public emisHttpUtil() {
  }
  /**
   * 傳回WebApp的根目錄(/epos 或 /les)
   */
  public static String getWebappRoot(HttpServletRequest request) {
    String _sRelativePath = request.getRequestURI();
    _sRelativePath = _sRelativePath.substring(0, _sRelativePath.lastIndexOf("/"));
    return _sRelativePath;
  }

  /**
   * 輸出servlet[的路徑資訊
   */
  public static void printPath(HttpServlet oServlet, HttpServletRequest request, PrintWriter out) {
    String _sRelativePath = request.getServletPath();
    out.println("本網頁的相對路徑=" + _sRelativePath + "<br>");  //  "/xxx/Servlet1"
    out.println("本網頁路徑的虛擬目錄路徑=" + request.getRequestURI() + "<br>");  // 
    out.println("網站根目錄的實體路徑=" + request.getRealPath("/") + "<br>"); 
    out.println("本網頁的實體路徑=" + oServlet.getServletContext().getRealPath(_sRelativePath) + "<br>");
  } // printPath(out)
  
  public static void printPath(ServletContext oServlet, HttpServletRequest request, JspWriter out) 
  throws ServletException, IOException {
    String _sRelativePath = request.getServletPath();
    out.println("本網頁的相對路徑=" + _sRelativePath + "<br>");  //  "/xxx/Servlet1"
    out.println("本網頁路徑的虛擬目錄路徑=" + request.getRequestURI() + "<br>");  // 
    out.println("網站根目錄的實體路徑=" + request.getRealPath("/") + "<br>"); 
    out.println("本網頁的實體路徑=" + oServlet.getRealPath(_sRelativePath) + "<br>");
  } // printPath(out)

}
