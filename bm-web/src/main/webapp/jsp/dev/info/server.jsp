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
    setServletContext(request.getSession().getServletContext());
    PrintWriter pw = new PrintWriter(out);
    pw.println("<table width='100%' border='2' cellspacing='2' cellpadding='2' bgcolor='papayawhip'>");

    // Print application values
    printApplication(pw, request);

    // Print application init parameters
    printApplicationInit(pw);

    // Print servlet init parameters
    printServletInit(pw);

    pw.println("</table>");
%>
</body>
</html>

<%!
  ServletContext _oContext = null;
  public  void setServletContext(ServletContext servletContext){
    _oContext = servletContext;
  }
  public  ServletContext getServletContext(){
    return _oContext ;
  }
  public  Enumeration getInitParameterNames(){
    return _oContext.getInitParameterNames();
  }
  public  String getInitParameter(String name){
    return _oContext.getInitParameter(name);
  }
  /**
   *  Write application properties
   */
  private void printApplication(PrintWriter pw, HttpServletRequest req)
                                throws IOException
  {
    ServletContext application = getServletContext();

    // Application Information ...
    pw.println("<tr class='odd_row'><td colspan='2'>　Application Information</td></tr>");
    pw.print("<tr class='even_row'><td>Servlet Context</td><td>");
    pw.println(application + "</td></tr>");
    pw.print("<tr class='even_row'><td>Servlet Context Name</td><td>");
    pw.println(application.getServletContextName() + "</td></tr>");
    pw.print("<tr class='even_row'><td>Major Version</td><td>");
    pw.println(application.getMajorVersion() + "</td></tr>");
    pw.print("<tr class='even_row'><td>Minor Version</td><td>");
    pw.println(application.getMinorVersion() + "</td></tr>");
    pw.print("<tr class='even_row'><td>Server Info</td><td>");
    pw.println(application.getServerInfo() + "</td></tr>");
    pw.print("<tr class='even_row'><td>Real Path (of pathinfo)</td><td>");
    pw.println(req.getPathInfo() == null ? application.getRealPath(req.getRequestURI()) : application.getRealPath(req.getPathInfo()) + "</td></tr>");
    pw.print("<tr class='even_row'><td>Mime-type (of pathinfo)</td><td>");
    pw.println(application.getMimeType(req.getPathInfo() == null ? req.getRequestURI() : req.getPathInfo()) + "</td></tr>");

    // Application Variables ...
    pw.println("<tr class='odd_row'><td colspan='2'>　Application Variables (Attributes)</td></tr>");
    Enumeration e = application.getAttributeNames();
    while (e.hasMoreElements()) {
      String name = (String) e.nextElement();

      pw.print("<tr class='even_row'><td>");
      pw.print(name);
      pw.print("</td><td>");
      pw.println(application.getAttribute(name)+"</td></tr>");
    }
  }

  /**
   *  Write application init parameters
   */
  private void printApplicationInit(PrintWriter pw)
                                    throws IOException
  {
    pw.println("<tr class='odd_row'><td colspan='2'>　Application Init Parameters</td></tr>");

    ServletContext app = getServletContext();

    Enumeration e = app.getInitParameterNames();
    if (! e.hasMoreElements()) {
      pw.println("<tr class='even_row'><td colspan='2'>无资料</td></tr>");
      return;
    }

    while (e.hasMoreElements()) {
      String name = (String) e.nextElement();

      pw.print("<tr class='even_row'><td>");
      pw.print(name);
      pw.print("</td><td>");
      pw.println(app.getInitParameter(name)+"</td></tr>");
    }
  }

  /**
   *  Write servlet init parameters
   */
  private void printServletInit(PrintWriter pw)
                                throws IOException
  {
    pw.println("<tr class='odd_row'><td colspan='2'>　Servlet Init Parameters</td></tr>");

    Enumeration e = getInitParameterNames();
    
    if (! e.hasMoreElements()) {
      pw.println("<tr class='even_row'><td colspan='2'>无资料</td></tr>");
      return;
    }

    while (e.hasMoreElements()) {
      String name = (String) e.nextElement();

      pw.print("<tr class='even_row'><td>");
      pw.print(name);
      pw.print("</td><td>");
      pw.println(getInitParameter(name)+"</td></tr>");
    }
  }
%>

