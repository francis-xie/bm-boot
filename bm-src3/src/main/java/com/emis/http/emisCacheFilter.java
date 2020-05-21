/* $Id: emisCacheFilter.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.http;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class emisCacheFilter implements Filter {
  private FilterConfig filterConfig = null;

  public void destroy() {
    this.filterConfig = null;
  }

  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
      throws ServletException, IOException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;

    // 監視哪些 request 被送出，如果輸出到檔案，就跟 Web log 差不多了.
    // Debugging: stdout.log or http.log
    //System.out.println((new Date()) + " emisCacheFilter: " + request.getRequestURL());

    // 根據 filter 初始化參數來設定 HTTP response parameters.
    for (Enumeration e = filterConfig.getInitParameterNames(); e.hasMoreElements();) {
      String headerName = (String) e.nextElement();
      response.addHeader(headerName, filterConfig.getInitParameter(headerName));
    }

    chain.doFilter(req, resp);
  }

  public void init(FilterConfig config) throws ServletException {
    this.filterConfig = config;
  }
}
