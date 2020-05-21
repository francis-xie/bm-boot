package com.emis.servlet;

import com.emis.db.emisDbMgr;
import com.emis.db.emisSQLRec;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * 顯示最慢的 N 個 SQL
 */
public class emisSQLTop10 extends HttpServlet
{
  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException
  {	  
    ServletContext oContext = this.getServletContext();
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    try {
      emisDbMgr oDbMgr = emisDbMgr.getInstance(oContext);
      if( "RESET".equals( request.getParameter("act") )) {
        oDbMgr.clearTopSQL();
      }
      emisSQLRec []  list = oDbMgr.getTopSQL();
      if ( list == null ) return;
      out.println("<html><head><title>SQL Top " + list.length + "</title></head><body>");
      out.println("<form method=post action='"+request.getRequestURI()+"'>");
      out.println("<input type='hidden' name='act' value='RESET'>");
      out.println("<input type='submit' value='清除'>");
      out.println("</form>");
      out.println("IP=" + emisUtil.getServerIP());
      out.println("<table border='1' width='95%'><TR><TD width='5%'>序</TD><TD width='75%'>SQL</TD><TD width='15%'>時間</TD><TD width='5%'>秒數</TD></TR>");
      for(int i=0;i<list.length;i++) {
        emisSQLRec rec = list[i];
        if ( rec != null ) {
          Date d = new Date(rec.nStartTime);
          out.println("<TR><TD>"+(i+1)+"</TD><TD>"+rec.sSQL+"</TD><TD>"+d.toString()+"</TD><TD>"+(rec.nExecTime / 1000)+"</TD></TR>");
        }
      }
      out.println("</table></body></html>");
    } catch (Exception e) {
      out.print(e.getMessage());
    }
  }
  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
}