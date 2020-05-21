<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.emis.server.*" %>
<html>
<head>
  <style>
    table { font:10pt Arial; }
    .odd_row  { background-color:'99ccff'; }
    .even_row { background-color:'ffffcc'; }
  </style>
</head>
<body>
<h3>HTTP Server Statistics</h3>
<table width='100%' border='2' cellspacing='2' cellpadding='2' bgcolor='papayawhip'>
<%
  com.caucho.server.http.Statistics stat =
    (com.caucho.server.http.Statistics) application.getAttribute("caucho.statistics");

  if (stat != null) {
    out.println("<tr>");
/*    out.println("<td>the time the server started</td>");
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(stat.getServerStart());

    out.println("<td>" + cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) +
        "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR));
    out.println("</td>");
*/
    out.println("<tr>");
    out.println("<td>the number live sessions</td>");
    out.println("<td>" + stat.getLiveSessions() + "</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td>the average cpu load for the last minute </td>");
    String value = String.valueOf(stat.getMinuteCpu());
    out.println("<td>" + value + "</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td>the average number of active threads in the last minute</td>");
    out.println("<td>" + String.valueOf(stat.getMinuteThreads()) + "</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td>the average cpu load for the last hour</td>");
    out.println("<td>" + String.valueOf(stat.getHourCpu()) + "</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td>the average number of active threads in the last hour</td>");
    out.println("<td>" + String.valueOf(stat.getHourThreads()) + "</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td>the average cpu load for the last day</td>");
    out.println("<td>" + String.valueOf(stat.getDayCpu()) + "</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td>the average number of active threads in the last day</td>");
    out.println("<td>" + String.valueOf(stat.getDayThreads()) + "</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td>the number of 'slow' threads</td>");
    out.println("<td>" + String.valueOf(stat.getSlowThreads()) + "</td>");
    out.println("</tr>");
  } else {
  }
%>
</table>
</body>
</html>
