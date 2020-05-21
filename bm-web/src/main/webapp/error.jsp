<!-- $Id: error.jsp 5 2015-05-27 08:15:12Z andy.he $
  2004/08/25 Jerry: Fix missing import
-->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.PrintWriter,
                 java.io.StringWriter" %>
<%@ page import="java.sql.*" %>
<%@ page isErrorPage="true" %>
<%
  // 由下层网页而来, 表示尚未登入而直接叫用某网页
  String _sMsg = exception.getLocalizedMessage();
  if (_sMsg == null) _sMsg = "未知的错误";
%>
<html>
<head>
</head>
<body bgcolor="lavenderblush">
<p>检查Log档：/resin/logs/*.log, /wwwroot/xxx/emisServer*.log</p>
<h3 style="color:red">
500 Servlet Exception：<%= _sMsg %>
</h3>
<hr>
<h3 style="color:red">错误追踪：</h3>
<%
  try {
    StringWriter _oWrt = new StringWriter();
    PrintWriter _oOut = new PrintWriter(_oWrt);
    if( exception instanceof SQLException)
    {
      SQLException sql = (SQLException) exception;
      //_oOut.println("ErrorCode="+sql.getErrorCode()+"<BR>");
    }
    exception.printStackTrace(new PrintWriter(_oOut));


    String _sTrace = _oWrt.toString();
    String _sTraceSaved = _sTrace;

    int _iPos, _iPos2, _iPos3;
    String _sFilename, _sLine, _sMethod;

    out.println("<table border='1' cellspacing='0' cellpadding='2' borderColor='cornflowerblue' bgColor='aliceBlue'>");
    out.println("<tr bgcolor='cornflowerblue' borderColor='aliceBlue' style='color:aliceBlue'>");
    out.println("<td align='center'>程式名称</td>");
    out.println("<td align='center'>行号</td><td align='center'>方法名称</td></tr>");
    while (true) {
      _iPos = _sTrace.indexOf("(");
      if (_iPos < 0) break;
      _iPos3 = _sTrace.indexOf("at ") + 3;

      _sMethod = _iPos3 < 0 ? "&nbsp;" : _sTrace.substring(_iPos3, _iPos);

      _iPos2 = _sTrace.indexOf(":", _iPos);
      _sFilename = _sTrace.substring(_iPos+1, _iPos2);

      _iPos3 = _sTrace.indexOf(")", _iPos);
      _sLine = _sTrace.substring(_iPos2+1, _iPos3);

      out.println("<tr><td>" + _sFilename + "</td>");
      out.println("<td align='center'>" + _sLine + "</td>");
      out.println("<td>" + _sMethod + "</td><tr>");
      _sTrace = _sTrace.substring(_iPos3+1, _sTrace.length());
    }
    out.println("</table><hr>");
    out.println("<h3 style='color:red'>错误讯息：</h3>");
    out.println(_sTraceSaved);
    out.flush();
  } catch (Exception e) {
    out.println("err:" + e.getMessage());
  }
%>
</body>
</html>
