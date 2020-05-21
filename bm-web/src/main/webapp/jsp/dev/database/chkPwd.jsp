
<!-- saved from url=(0022)http://internet.e-mail -->
<!--$Header: /repository/wwwroot/wtn/jsp/dev/database/chkPwd.html,v 1.1 2007/01/31 02:05:00 harry Exp $
  @author harry 2007/01/30 created SQL数据库测试update时密码验证框
-->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.emis.user.*" %>
<%@ page import="com.emis.util.emisUtil" %>
<%@ page import="com.emis.db.emisProp" %>
<%@ taglib prefix="showdata" uri="/WEB-INF/showdata.tld" %>
<%
//  emisUser _oUser = emisCertFactory.getUser(application, request);
  String _sTitle = "";
  String _skeys = "";
  //String _sTaskName = emisUtil.getTaskName(session, _skeys);

    /*固定寫法：設定新增、修改。。。權限，ajax_inc.htm中引用*/
//  emisPermission _oPermission = null;
//  emisProp oProp_ = emisProp.getInstance(application);
%>

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
  <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
  <META HTTP-EQUIV="Expires" CONTENT="0">
  <title>密码验证</title><%--
  <link rel="stylesheet" href="../../../js/style.css">
  <script src="../../../js/epos.js"></script>
  <script src="../../../js/emis.js"></script>--%>
  <%--<%@include file="../../ajax_inc.htm"%>--%>
  <link id="emisSkins" rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/skins/default/style.css" />
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-1.4.2.js"></script>
  <script type="text/javascript" src='<%=request.getContextPath()%>/js/lang/emis-lang_<%=(String)session.getAttribute("languageType")%>.js'></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/ajax.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/ajax_util.js"></script>
  <%@include file="../../ymPrompt_inc.htm"%>
</head>

<body class="wrap">
<form id="idForm" method="post">
  <div class="commonPanel">
    <h1>密码验证</h1>
    <table>
      <tr>
        <th>密码</th>
        <td>
          <input type="password" name="PWD" size="20">
          <font color="red">*</font>
        </td>
      </tr>
      <tr height="40">
        <td align="center" colspan="2" class="functions">
          <button type="button" id="btnOK" accesskey="Y" title="确定:[F10]" class="OKButton">
            <img src="../../../images/save.gif"> 确定(<u>Y</u>)
          </button>&nbsp;
          <button type="button" id="btnClose" accesskey="C" title="取消:[Esc]" class="ExitButton">
            <img src="../../../images/cancel.gif"> 取消(<u>C</u>)
          </button>
        </td>
      </tr>
      <%--<showdata:buttons type="YN" styleClass="functions"/>--%>
    </table>
  </div>
</form>
</body>
</html>

<script>
  $(document).ready(function() {
    //设焦点
    setFocus("PWD");
  });

  jQuery("#btnOK").click(function(){
    if (!emisEmptyValid(formobj("PWD"), "密码")) return;
    //window.returnValue = formobj("PWD").value;
    emisCloseDialog(formobj("PWD").value);
  });

  jQuery("#btnClose").click(function(){
    emisCloseDialog();
  });
</script>

<%--<script for="document" event="onkeydown">
  emisQryOnKeyDown();
</script>--%>
