package com.emis.util;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.Enumeration;

public class ServletUtil
{
  public static void printHeaders(HttpServletRequest request,PrintWriter out)
  {
    Enumeration e = request.getHeaderNames();
    while ( e.hasMoreElements() )
    {
      String key = (String) e.nextElement();
      out.println(key+"="+request.getHeader(key)+"<BR>");
    }

  }
  public static void printParameters(HttpServletRequest request,PrintWriter out)
  {
    Enumeration e = request.getParameterNames();
    while ( e.hasMoreElements() )
    {
      String key = (String) e.nextElement();
      out.println(key+"="+request.getParameter(key)+"<BR>");
    }
  }
}