<%@ page contentType="text/html;charset=UTF-8" %>
<%--
  V2.0 2003/01/20 增加产生栏位结构之功能
  2004/04/26 Jerry Revision: 1.4: 使用Servlet-mapping
  2004/08/09 Jerry $Revision: 1 $: add login function -> dbqry.jsp
--%>
<html>
<head>
<title>密碼檢核</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
  <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
  <META HTTP-EQUIV="Expires" CONTENT="0">
</head>
<body onload="ready()">
<center>
<h3>密碼檢核</h3>
<form method='POST' action='dbqry.jsp' >
<table border="0" cellspacing="1" cellpadding="8" bgColor='Blue'>
  <tr bgColor='LightYellow'>
		<td>請輸入資料查詢密碼</td>
		<td><input name='PASSWD' id="PASSWD" type='password' maxlength="20" size="16"></td>
	</tr>
	<tr bgColor='LightBlue'>
		<td colspan='2' align='center'>
		  <input type='Submit' value='確定'>
		  <input type='Button' value='關閉' onclick='window.close();'>
	  </td>
  </tr>
</table>
</form>
</center>
</body>
</html>
<script>
  function ready() {
    document.getElementById("PASSWD").focus();
  }
</script>
