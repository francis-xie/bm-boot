<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="showdata" uri="/WEB-INF/showdata.tld" %>
<%@ page import="com.emis.util.emisUtil,
                 com.emis.file.emisFile,
                 com.emis.file.emisFileMgr"%>
<%@ page import="java.io.*" %>
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
//    FileReader _oReader = new FileReader(sFile);
    //Dana 2011/03/29 修正查看源碼時,中文亂碼問題.
    BufferedReader _oReader = new BufferedReader(new InputStreamReader(new FileInputStream(sFile), "UTF-8"));
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
  String _sRoot = application.getRealPath("/");
  _sRoot = _sRoot.replace('\\','/');
  _sRoot = _sRoot.substring(2);
  _sFile = _sRoot + _sFile;

  File _oFile = new File(_sFile);
  String _sName = _oFile.getName();
  _sName = emisUtil.stringReplace(_sName,".jsp",".xml","a");
  String _sXmlFile = _sRoot + "/business/" + _sName;

  out.println("<h4>"+_sTitle+"</h4>");

  _table(application, out, "JSP", _sFile);
  out.println("<br>");
  _table(application, out, "XML", _sXmlFile);
%>
<script for=editJSP event=onclick>
  var _sFile = "<%= _sFile %>";
  _sFile = _sFile.replace("/", "\\");

  var _sEditor = EDITOR[0].checked ? EDITOR[0].value : EDITOR[1].value;
  xmlUtil.execute(_sEditor, _sFile);
</script>
<script for=editXML event=onclick>
  var _sFile = "<%= _sXmlFile %>";
  _sFile = _sFile.replace("/", "\\");
  var _sEditor = EDITOR[0].checked ? EDITOR[0].value : EDITOR[1].value;
  xmlUtil.execute(_sEditor, _sFile);
</script>