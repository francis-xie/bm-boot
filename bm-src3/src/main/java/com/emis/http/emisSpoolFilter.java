package com.emis.http;

import com.emis.db.emisDbConnector;
import com.emis.db.emisDbMgr;
import com.emis.spool.emisComplexSpool;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 1 依需要配置過濾器，在web.xml中配置如下：
 * <filter>
 *   <filter-name>spoolchecker</filter-name>
 *   <filter-class>com.emis.http.emisSpoolFilter</filter-class>
 *   <init-param>
 *     <param-name>errorPage</param-name>
 *     <param-value>/login_error.jsp</param-value>
 *   </init-param>
 * </filter>
 * <filter-mapping>
 *   <filter-name>spoolchecker</filter-name>
 *   <url-pattern>/jsp/sas/*</url-pattern>
 * </filter-mapping>
 *
 * 2 需同步修改：
 *  2.1 com.emis.spool.emisComplexSpool - 加bInitReady_属性及isInitReady方法
 *
 *  // DB Spool 初始化完成的標記
 *  protected boolean bInitReady_ = false;
 *
 *  public boolean isInitReady(){
 *    return bInitReady_;
 *  }
 *
 *  2.2 com.emis.spool.emisSpoolInit  - 修改run方法
 *  public void run() {
 *    ....
 *    while(true){
 *      ....
 *    }
 *    // 標識為初始化完成
 *    oSpool_.bInitReady_ = true;
 *  }
 */
public class emisSpoolFilter implements Filter {

  private FilterConfig oFilterConfig_ = null;
  //private String sContextPath = null;
  private String sErrorPage = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    oFilterConfig_ = filterConfig;
    //sContextPath = oFilterConfig_.getServletContext().getServletContextName() + "/";
    // 错误页面
    sErrorPage = filterConfig.getInitParameter("errorPage");
    // 设置默认错误页面
    if (sErrorPage == null || "".equals(sErrorPage.trim())) {
      sErrorPage = "/login_error.jsp";
    }
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
    String sURI = ((HttpServletRequest) request).getRequestURI();
    // 如果是错误页面，则通过，避免死循环
    if (sErrorPage.equalsIgnoreCase(sURI)) {
      chain.doFilter(request, response);
      return;
    }
    emisDbMgr oDbMgr = null;
    try {
      oDbMgr = emisDbMgr.getInstance(oFilterConfig_.getServletContext());
    } catch (Exception e) {
      sendRedirect(request, response);
      return;
    }
    try {
      if (oDbMgr != null) {
        // get the default connector
        emisDbConnector connector = oDbMgr.getConnector();
        if (connector != null) {
          emisComplexSpool spool = (emisComplexSpool) connector;
          // 判断连接数是否达到初始数
          if (spool.isInitReady()) {
            chain.doFilter(request, response);
          } else {
            sendRedirect(request, response);
          }
        } else {
          sendRedirect(request, response);
        }
      } else {
        sendRedirect(request, response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void destroy() {
    oFilterConfig_ = null;
    //sContextPath = null;
    sErrorPage = null;
  }

  private void sendRedirect(ServletRequest request, ServletResponse response) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    //System.out.println("DB Spool filter: Server has not initialize - " + req.getRequestURI());

    HttpServletResponse res = (HttpServletResponse) response;
    String lang = request.getLocale().toString();
    if("zh_TW".equalsIgnoreCase(lang)){
      request.setAttribute("emisMenuServlet.MSG", "主機服務尚未啟動完成，請稍待一會再試。");
    }else if("zh_CN".equalsIgnoreCase(lang)){
      request.setAttribute("emisMenuServlet.MSG", "主机服务尚未启动完成，请稍待一会再试。");
    }else{
      request.setAttribute("emisMenuServlet.MSG", "The server has not startup, please try later.");
    }
    req.getRequestDispatcher(sErrorPage).forward(request,response);
    //res.sendRedirect(sErrorPage);
  }
}
