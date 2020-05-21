<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.io.*" %>
<html>
<head>
  <style>
    table { font:10pt Arial; }
    .odd_row  { background-color:'99ccff'; }
    .even_row { background-color:'ffffcc'; }
  </style>
</head>
<body>
<%
    PrintWriter pw = new PrintWriter(out);
    pw.println("<table width='100%' border='2' cellspacing='2' cellpadding='2' bgcolor='papayawhip'>");

    // Print Servlet Information
    printServletInfo(pw, request);

    pw.println("</table>");
%>

</body>
</html>

<%!
  /**
   *  Write Servlet Information
   */
  private void printServletInfo(PrintWriter pw, HttpServletRequest req)
                                throws IOException
  {
    pw.println("<tr class='odd_row'><td colspan='2'>　Servlet Information</td></tr>");

    Enumeration e = req.getHeaderNames();
    while( e.hasMoreElements() )
    {
      String key = (String)e.nextElement();
      Object value = req.getHeader(key);

      pw.print("<tr class='even_row'><td>");
      pw.print(key);
      pw.print("</td><td>");
      pw.println(value+"</td></tr>");
    }

    pw.println("<tr class='odd_row'><td colspan='2'>　Miscellany</td></tr>");
    pw.print("<tr class='even_row'><td>Methods</td><td>");
    pw.println(req.getMethod()+"</td></tr>");
    pw.print("<tr class='even_row'><td>AuthType</td><td>");
    pw.println(req.getAuthType()+"</td></tr>");
    pw.print("<tr class='even_row'><td>ContentType</td><td>");
    pw.println(req.getContentType()+"</td></tr>");


    pw.println("<tr class='odd_row'><td colspan='2'>　Cookies</td></tr>");
    Cookie [] cks = req.getCookies();
    if( cks != null )
    {
      for(int i=0; i< cks.length; i++)
      {
        Cookie c = cks[i];
        if(c != null ) {
          pw.print("<tr class='even_row'><td>");
          pw.print(c.getName());
          pw.print("</td><td>");
          pw.println(c.getValue()+"</td></tr>");
        }
      }
    }

    pw.println("<tr class='odd_row'><td colspan='2'>　Parameters</td></tr>");
    e = req.getParameterNames();

    if (! e.hasMoreElements()) {
      pw.println("<tr class='even_row'><td colspan='2'>无资料</td></tr>");
      return;
    }

    while( e.hasMoreElements() )
    {
      String key =(String) e.nextElement();
      Object value = req.getParameter(key);

      pw.print("<tr class='even_row'><td>");
      pw.print(key);
      pw.print("</td><td>");
      pw.println(value+"</td></tr>");
    }
  }
%>
