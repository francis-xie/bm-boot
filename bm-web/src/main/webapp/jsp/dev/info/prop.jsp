<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.emis.server.*,java.io.*" %>
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

    // Print JVM Properties
    printJVMProp(pw);

    pw.println("</table>");
%>
</body>
</html>

<%!
  /**
   *  Write JVM Properties
   */
  private void printJVMProp(PrintWriter pw)
                            throws IOException
  {
    pw.println("<tr class='odd_row'><td colspan='2'>　JVM Properties</td></tr>");

    Properties props = System.getProperties ();
    Enumeration e = props.keys();

    if (! e.hasMoreElements()) {
      pw.println("<tr class='odd_row'><td colspan='2'>无资料</td></tr>");
      return;
    }

    while ( e.hasMoreElements() )
    {
      Object k = e.nextElement();
      Object v = props.get(k);

      pw.print("<tr class='even_row'><td>");
      pw.print(k);
      pw.print("</td><td>");
      pw.print(v);
      pw.println("</td></tr>");
    }
    pw.print("<tr class='even_row'><td>");
    pw.print("-Xmx");
    pw.print("</td><td>");
    pw.print((Runtime.getRuntime().maxMemory()/1000/1000)+"(M)");
    pw.println("</td></tr>");
  }
%>
