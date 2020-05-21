<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="showdata" uri="/WEB-INF/showdata.tld" %>
<%@ page import="java.io.InputStream,
                 java.io.FileReader,
                 java.io.PrintWriter,
                 java.io.File,
                 com.emis.util.emisUtil,
                 com.emis.file.emisFile,
                 com.emis.file.emisFileMgr"%>
<OBJECT id='xmlUtil' classid='clsid:8E909573-2ACD-11D5-8D0E-0050BA0264C5'>
</OBJECT>
<%!
  private void _table(ServletContext application,JspWriter out, String sName, String sFile)
  throws Exception {
    File _oFile = new File(sFile);
    if (!_oFile.exists()) return;
    out.println("<a name='" + sName + "'></a>");
    out.println("<table border=1 width='100%'>");
    out.println("<tr><td>");
    out.println("<input type=Button value='Edit " + sFile +
        "' name='edit" + sName + "'>");
    out.println("<input type=button value='CLOSE' onclick='window.close()'>");
    out.println(" | Jump: <a href='#JSP'>JSP</a> | <a href='#XML'>XML</a>");
    out.println("</td></tr>");
    out.println("<tr>");
    out.println("<td width='50%' valign='top'>");
    out.println("<xmp>");
    FileReader _oReader = new FileReader(sFile);
    try {
      int _iBytes;
      char[] _aData = new char[8192];
      while (( _iBytes = _oReader.read(_aData)) != -1) {
        out.write(_aData, 0, _iBytes);
      }
    } catch (Exception e) {
      e.printStackTrace(new PrintWriter(out));
    } finally {
      out.println("</xmp>");
      if (_oReader != null) _oReader.close();
    }
    out.println("</td></tr>");
    out.println("</table>");
  }
%>
<input type=radio name=EDITOR value='C:\WINSTAR\WINSTAR' checked>Winstar
<input type=radio name=EDITOR value='NOTEPAD'>Notepad
<%
  String _sFile = request.getParameter("FILE");
  String _sTitle = request.getParameter("NAME");
  if (_sFile == null) return;

  _sFile = emisUtil.stringReplace(_sFile, "\\", "\\\\","a");

  File _oFile = new File(_sFile);
  out.println("<h4>"+_sTitle+" -- " + _sFile + "</h4>");

  _table(application, out, "JSP", _sFile);
%>
<script for=editJSP event=onclick>
  var _sFile = "<%= _sFile %>";
  var _sEditor = EDITOR[0].checked ? EDITOR[0].value : EDITOR[1].value;
  xmlUtil.execute(_sEditor, _sFile);
</script>