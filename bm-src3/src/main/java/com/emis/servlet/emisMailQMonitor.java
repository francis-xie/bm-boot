/**
 * $Header: /repository/src3/src/com/emis/servlet/emisMailQMonitor.java,v 1.1.1.1 2005/10/14 12:42:53 andy Exp $
 */
package com.emis.servlet;

import com.emis.mail.emisMailQueue;
import com.emis.util.emisUtil;

import javax.mail.Address;
import javax.mail.Message;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

public class emisMailQMonitor extends HttpServlet
{
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
	response.setContentType("text/html;charset=UTF-8");	
    PrintWriter out = response.getWriter();

    ServletContext application = getServletContext();

    out.println("<html>");
    out.println("<title>郵件傳送狀態</title>");
    out.println("<head>");
    out.println("<META HTTP-EQUIV='Refresh' CONTENT='5'>");
    out.println("</head>");
    out.println("<body>");

    try {
      emisMailQueue q = emisMailQueue.getInstance(application);

      out.println("已傳送郵件："+q.getMailSentCount()+"封<BR>");
      out.println("待送郵件：<BR>");
      out.println("<table width='100%' border='1'>");
      out.println("<TR bgcolor='99ccff' align='center'><TD>送　　　件　　　人</TD><TD>主　　　　　　旨</TD></TR>");

      Iterator it = q.iterator();
      while (it.hasNext())
      {
        Message m = (Message) it.next();
        Address [] from = m.getFrom();
        String sSubject = m.getSubject();
        if( sSubject == null )
          sSubject = "Unknow";

        if( (from != null) && (from.length > 0) && (from[0] != null))
        {
          out.println("<TR bgcolor='ffffcc'><TD>"+from[0].toString()+"</TD><TD>"+sSubject+"</TD></TR>");
        } else {
          out.println("<TR bgcolor='ffffcc'><TD>Unknow</TD><TD>"+sSubject+"</TD></TR>");
        }
      }
    } catch (Exception e)  {
      e.printStackTrace(out);
    }

    out.println("</table>");
    out.println("</body>");
    out.println("</html>");
  }

  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException
  {
    service(p0, p1);
  }

  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException
  {
    service(p0, p1);
  }
}