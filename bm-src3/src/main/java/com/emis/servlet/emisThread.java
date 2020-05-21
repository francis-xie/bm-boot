/**
 * $Header: /repository/src3/src/com/emis/servlet/emisThread.java,v 1.1.1.1 2005/10/14 12:42:55 andy Exp $
 */
package com.emis.servlet;

import com.emis.util.emisUtil;

import javax.servlet.ServletException;import javax.servlet.http.HttpServlet;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import java.io.IOException;import java.io.PrintWriter;

public class emisThread extends HttpServlet
{
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();

    ThreadGroup g = Thread.currentThread().getThreadGroup();
    ThreadGroup parent = null;
    while ( ( parent = g.getParent() ) != null )  {
      g = parent;
    }

    int nTCount = g.activeCount();
    if( nTCount > 0 )
    {
      Thread [] tArray = new Thread[nTCount];
      g.enumerate(tArray, true);
      out.println("<html>");
      out.println("<head>");
      out.println("<title>目前執行緒啟動狀態</title>");
      out.println("</head>");
      out.println("<body>");
      out.println("<table width='100%' border='1'>");
      out.println("<TR bgcolor='99ccff' align='center'><TD>Group</TD><TD>Thread Name</TD><TD>Context Class Loader</TD><TD>Priority</TD><TD>Alive</TD><TD>Daemon</TD><TD>Interrupted</TD><TD>stack</TD></TR>");
      for(int i=0;i<nTCount;i++)
      {
        Thread t = tArray[i];
        if( t != null )
        {
          out.println("<TR bgcolor='ffffcc'><TD>"+t.getThreadGroup().getName()+
                                      "</TD><TD>"+t.getName()+
                                      "</TD><TD>"+t.getContextClassLoader()+
                                      "</TD><TD align='center'>"+t.getPriority()+
                                      "</TD><TD align='center'>"+t.isAlive()+
                                      "</TD><TD align='center'>"+t.isDaemon()+
                                      "</TD><TD align='center'>"+t.isInterrupted()+                                      "</TD><TD align='center'>"+ getStack(t) +"</TD></TR>");
        }
      }
      out.println("</table>");
      out.println("</body>");
      out.println("</html>");
    }
  }
  private String getStack(Thread t) {	  StringBuffer sb = new StringBuffer();	  StackTraceElement[] sts = t.getStackTrace();	  if(sts!=null) {		  for(int i=0;i<sts.length;i++) {			  StackTraceElement st = sts[i];			  sb.append( st.getFileName() + "." + st.getMethodName() + " line:" + st.getLineNumber() ).append("<BR>");		  }	  }	  return sb.toString();  }
  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException
  {
    service(p0,p1);
  }

  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException
  {
    service(p0,p1);
  }
}