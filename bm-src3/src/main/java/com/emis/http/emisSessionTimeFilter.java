/* $Id: emisSessionTimeFilter.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2005 EMIS Corp. All Rights Reserved.
 */
package com.emis.http;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/* $Id: emisSessionTimeFilter.java 4 2015-05-27 08:13:47Z andy.he $
 * 功能：將session的時間記錄下來，供checktime.jsp判斷是否連線逾期.
 * @author Jerry
 * Copyright (c) 2005 EMIS Corp. All Rights Reserved.
 */

public class emisSessionTimeFilter implements Filter {
    private FilterConfig filterConfig = null;

    /**
     * @param req
     * @param res
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            PrintWriter out = response.getWriter();
            String _sURL = request.getServletPath();
            HttpSession session = request.getSession();

            //out.println("path=" + _sURL);
            //out.println("max=" + session.getMaxInactiveInterval());
            if (session != null && _sURL.indexOf("checktime.jsp") < 0 &&
                    _sURL.indexOf("error.jsp") < 0 && _sURL.indexOf("index.jsp") < 0) {

                //out.println("Last access=" + _oDate);
                session.setAttribute("LAST_ACCESS", new Date());
                // <META http-equiv="Refresh" content="300; url=/cgi-bin/prod/ch/gt/logout_auto.jsp?BV_SessionID=@@@@2051023182.1113992336@@@@&BV_EngineID=cccfaddefmemdmdcfngcfkmdhghdfkf.0">
                //,'"+session.getId()+"',
                //response.addHeader("Refresh", "30;url=javascript:void(window.open('/mis/checktime.jsp','_self','width=300px,height=300px,border=thin,help=no,menubar=no,toolbar=no,location=no,directories=no,status=no,resizable=0,scrollbars=1'))");
                String _sSeconds = filterConfig.getInitParameter("TimeoutSeconds");
                String sRoot = emisHttpUtil.getWebappRoot((HttpServletRequest) req);
               // System.out.println("["+sRoot+"]");
                int _index = sRoot.indexOf("/jsp");
                if(_index > 1){
                 sRoot = sRoot.substring(0, _index);
                }
                response.addHeader("Refresh", _sSeconds + ";url=javascript:void(emisWinOpen('" + sRoot + "/checktime.jsp',300,300))");
                /*
                out.write("<script>\n");
                out.write("  var iTop =(screen.availHeight/2)-(250/2)-1;");
                out.write("  iLeft=(screen.availWidth/2)-(300/2)-1;");
                out.write("  setInterval(\"emisWinOpen('/mis/checktime.jsp',300,250,"+
                    "iTop,iLeft,'checktime');\",60000);\n");
                out.println("</script>");
                */
            }
            chain.doFilter(req, res);
        }
    }

    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
        // noop
    }
}
