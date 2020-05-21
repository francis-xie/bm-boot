<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="showdata" uri="/WEB-INF/showdata.tld" %>
<%@ page import="com.emis.db.emisDb,
                 java.sql.ResultSet,
                 com.emis.util.emisUtil,
                 com.emis.file.emisFileMgr,
                 java.util.Enumeration,
                 com.emis.file.emisFile"%>
<%@ page import="java.io.*" %>
<OBJECT id='xmlUtil' classid='clsid:8E909573-2ACD-11D5-8D0E-0050BA0264C5'>
</OBJECT>
<%!
  public boolean checkConfFile(File conf, String contextPath, String cfg) {
    BufferedReader _oReader = null;
    try {
      _oReader = new BufferedReader(new InputStreamReader(new FileInputStream(conf), "UTF-8"));
      String sLine = null, sCfg = cfg.toLowerCase();
      boolean bPath = false, bCFG = false;
      while ((sLine = _oReader.readLine()) != null) {
        if (bPath && bCFG) break;
        sLine = sLine.toLowerCase();
        if (!bPath && sLine.indexOf("web-app") > 0 && sLine.indexOf("id") > 0 && sLine.indexOf(contextPath) > 0) {
          bPath = true;
          continue;
        } else if (!bCFG && sLine.indexOf("context-param") > 0 && sLine.indexOf("emiscfg") > 0 && sLine.indexOf(sCfg) > 0) {
          bCFG = true;
          continue;
        }
      }
      return bPath && bCFG;
    } catch (Exception e) {
      return false;
    } finally {
      if (_oReader != null) {
        try {
          _oReader.close();
        } catch (IOException e) {
        }
      }
    }
  }
%>
<%
  String _sRoot = application.getRealPath("/");
  _sRoot = _sRoot.replace('\\','/');
  _sRoot = _sRoot.substring(2);

  int _iPos1 = _sRoot.indexOf("/wwwroot");
  int _iPos2 = _sRoot.lastIndexOf("/");
  String _sSysID = "", _sCfgID = "";
  try {
    if ((_iPos1 + 9) < _sRoot.length() && _iPos2 < _sRoot.length())
      _sSysID = _sRoot.substring(_iPos1 + 9, _iPos2);
  } catch (Exception e) {
  }

  String _sResinDir = System.getProperty("user.dir").replace('\\','/');
  String _sResinCfg =application.getInitParameter("emiscfg").replace('\\', '/');
  //out.println("id=<h3>"+_sRoot+"/"+_sSysID+"$</h3>");
  if ( _sResinCfg != null) {
    File conf = new File(_sResinDir + "\\conf");
    File[] files = conf.listFiles();
    String sContext = request.getContextPath();
    String sCfg = _sResinCfg.substring(_sResinCfg.lastIndexOf("/") + 1);
    for (int i = 0; i < files.length; i++) {
      if (checkConfFile(files[i], sContext, sCfg)) {
        _sSysID = files[i].getName().replaceAll(".conf", "");
        break;
      }
    }
    try {
      _iPos1 = _sResinCfg.lastIndexOf("/");
      _iPos2 = _sResinCfg.toLowerCase().indexOf(".cfg");
      _sCfgID = _sResinCfg.substring(_iPos1 + 1, _iPos2);
    } catch (Exception e) {
      _sCfgID = _sSysID;
    }
  } else {
    _sCfgID = _sSysID;
  }
%>
<a name="TOP"> </a>
<input type=radio name=EDITOR value='C:\WINSTAR\WINSTAR' checked>C:\winstar\Winstar
<input type=radio name=EDITOR value='D:\WINSTAR\WINSTAR'>D:\winstar\Winstar
<input type=radio name=EDITOR value='NOTEPAD'>Notepad
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="#TOP">TOP</a> | <a href="#LOG">LOG</a> | <a href="#JSP">JSP</a> | <a href="#BOTTOM">BOTTOM</a>
<table border=1>
  <tr>
    <td>
      <a href='javascript:_open("jsp/emis_data.jsp","emis_data.jsp");'>emis_data.jsp</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/emis_mtn.htm","emis_mtn.htm");'>emis_mtn.htm</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/emis_mtn.js","emis_mtn.js");'>emis_mtn.js</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/emis_save.jsp","emis_save.jsp");'>emis_save.jsp</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/emis_sel.jsp","emis_sel.jsp");'>emis_sel.jsp</a>
    </td>
  </tr>
  <tr>
    <td>
      <a href='javascript:_open("jsp/emis_tab.htm","emis_tab.htm");'>emis_tab.htm</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/emis_tab.js","emis_tab.js");'>emis_tab.js</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/emis_del.jsp","emis_del.jsp");'>emis_del.jsp</a>
    </td>
<%--    <td>
      <a href='javascript:_open("jsp/tab/tab_inc.htm","tab_inc.htm");'>tab_inc.htm</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/tab/tab_fun.htm","tab_fun.htm");'>tab_fun.htm</a>
    </td>--%>
<%--  </tr>
  <tr>
    <td>
      <a href='javascript:_open("jsp/tab/tab_upd.htm","tab_upd.htm");'>tab_upd.htm</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/mtn/mtn_inc.htm","mtn_inc.htm");'>mtn_inc.htm</a>
    </td>
    <td>
      <a href='javascript:_open("jsp/ord/ord_inc.htm","ord_inc.htm");'>ord_inc.htm</a>
    </td>--%>
    <td>
      <a href='javascript:_open("js/emis.js","emis.js");'>emis.js</a>
    </td>
    <td>
      <a href='javascript:_open("js/emisX.js","emisx.js");'>emisX.js</a>
    </td>
  </tr>
  <tr>
    <td>
      <a href='javascript:_open("js/epos.js","epos.js");'>epos.js</a>
    </td>
    <td>
      <a href='javascript:_openResin("<%=_sResinDir%>\\conf\\resin.conf","resin.conf");'>resin.conf</a>
    </td>
    <td>
      <a href='javascript:_openResin("<%=_sResinDir%>\\conf\\<%=_sSysID%>.conf","<%=_sSysID%>.conf");'><%=_sSysID%>.conf</a>
    </td>
    <td>
      <a href='javascript:_openResin("<%=_sResinCfg%>");'><%=_sCfgID%>.cfg</a>
    </td>
  </tr>
  <tr>
    <td>
      <a href='javascript:_openResin("<%=_sResinDir%>\\log\\stdout.log","stdout.log");'>stdout.log</a>
    </td>
    <td>
      <a href='javascript:_openResin("<%=_sResinDir%>\\log\\stderr.log","stderr.log");'>stderr.log</a>
    </td>
    <td>
      <a href='javascript:_openResin("<%=_sResinDir%>\\log\\error.log","error.log");'>error.log</a>
    </td>
    <td>
      <a href='javascript:_openResin("<%=_sResinDir%>\\log\\jvm.log","jvm.log");'>jvm.log</a>
    </td>
    <td>
      <a href='javascript:_openResin("<%=_sResinDir%>\\log\\runqianReport.log","runqianReport.log");'>runqianReport.log</a>
    </td>
  </tr>
</table>
<br>
<a href="#TOP">TOP</a> | <a href="#LOG">LOG</a> | <a href="#JSP">JSP</a> | <a href="#BOTTOM">BOTTOM</a>
<a name="LOG"> </a>
<table border=1>
  <tr>
  <%
    String _sDir = "c:" + _sRoot + "logs";
    _sDir = emisUtil.stringReplace(_sDir, "/", "\\", "a");
    Enumeration e = emisFileMgr.getInstance(application)
                               .getDirectory("root")
                               .subDirectory("logs")
                               .getFileList();
    int _iCount = 0;
    while (e.hasMoreElements()) {
      emisFile _oFile = (emisFile) e.nextElement();
      String _sName = _oFile.getFullName();
      _sName = emisUtil.stringReplace(_sName, "\\", "\\\\","a");
      out.println("<td><a href='javascript:_openResin(\"" +
         _sName + "\",\"" + _oFile.getFileName() +
        "\");'>" + _oFile.getFileName() + "</a>");
      out.println("</td>");
      _iCount++;
      if (_iCount % 3 == 0)
        out.println("</tr><tr>");
    }
  %>
  </tr>
</table>
<br>
<a href="#TOP">TOP</a> | <a href="#LOG">LOG</a> | <a href="#JSP">JSP</a> | <a href="#BOTTOM">BOTTOM</a>
<a name="JSP"> </a>
<%
  emisDb _oDb = emisDb.getInstance(application);
  try {
    _oDb.prepareStmt("select * from Menus where MENU_TYPE='I' order by keys");
    String _sName = "";

    out.println("<table border=1>");
    _oDb.prepareQuery();

    while (_oDb.next()) {
      String _sKey = _oDb.getString("KEYS");
      _sName = _oDb.getString("MENU_NAME");
      String _sFile = _oDb.getString("MENU_EXE");
      out.println("<tr>");
      out.println("<td>"+_sKey+"</td>");
      out.println("<td><a href='javascript:_open(\"" + _sFile+ "\",\""+
          _sName + "\")'>"+_sName+"</a>");
      out.println("</td>");

      String _sJspFile = _sRoot + _sFile;
      File _oFile = new File(_sJspFile);
      String _sXmlName = _oFile.getName();
      _sXmlName = emisUtil.stringReplace(_sXmlName,".jsp",".xml","a");
      String _sXmlFile = _sRoot + "business/" + _sXmlName;

      out.println("<td>");
      out.println("<input type=button name=editJSP value='JSP'"+
          " title='" + _sJspFile + "'>");
      out.println("<input type=button name=editXML value='XML'"+
          " title='" + _sXmlFile + "'>");
      out.println("</td>");
      out.println("</tr>");
    }
    out.println("</table>");
  } finally {
    if (_oDb != null) _oDb.close();
  }
%>
<a name="BOTTOM"> </a>
<a href="#TOP">TOP</a> | <a href="#LOG">LOG</a> | <a href="#JSP">JSP</a> | <a href="#BOTTOM">BOTTOM</a>
<script>
  function _open(sFile, sName) {
    window.open('viewjsp.jsp?FILE='+sFile+'&NAME=' + sName);
  }
  function _openResin(sFile, sName) {
    window.open('viewresin.jsp?FILE='+sFile+'&NAME=' + sName);
  }

</script>
<script for=editJSP event=onclick>
  var _sFile = this.title;
  _sFile = _sFile.replace("/", "\\");

  var _sEditor = EDITOR[0].checked ? EDITOR[0].value : EDITOR[1].value;
  xmlUtil.execute(_sEditor, _sFile);
</script>
<script for=editXML event=onclick>
  var _sFile = this.title;
  _sFile = _sFile.replace("/", "\\");
  var _sEditor = EDITOR[0].checked ? EDITOR[0].value : EDITOR[1].value;
  xmlUtil.execute(_sEditor, _sFile);
</script>