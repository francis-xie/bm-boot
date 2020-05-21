package com.emis.servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

public class emisServletInfo extends HttpServlet
{
  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException
  {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();

    out.println("<html><body><pre>");
    out.println("<HR>headers<BR>");
    Enumeration e = request.getHeaderNames();
    while( e.hasMoreElements() )
    {
      String key = (String)e.nextElement();
      Object value = request.getHeader(key);
      out.println(key+"="+value);
    }
    out.println("<HR>MISC<BR>");
    out.println("method="+request.getMethod());
    out.println("AuthType="+request.getAuthType());
    out.println("ContentType="+request.getContentType());

    out.println("<HR>cookies<BR>");

    Cookie [] cks = request.getCookies();
    if( cks != null )
    {
      for(int i=0; i< cks.length; i++)
      {
        Cookie c = cks[i];
        if(c != null )
        out.println(c.getName()+"="+c.getValue());
      }
    }


    out.println("<HR>parameters<BR>");
    e = request.getParameterNames();
    while( e.hasMoreElements() )
    {
      String key =(String) e.nextElement();
      Object value = request.getParameter(key);
      out.println(key+"="+value);
    }
    out.println("</pre></body></html>");

  }

  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }

}