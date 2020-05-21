<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.emis.user.*" %>
<%@ page import="com.emis.business.*" %>
<%
    emisUser _oUser = emisCertFactory.getUser(application,request);
%>
<HTML>
<body>
<pre>
<%

    String _p_no = request.getParameter("P_NO1");
    String _p_next = request.getParameter("P_NO2");

    emisBusiness _oBusiness = emisBusinessMgr.getInstance(application).get("XML资料测试",_oUser);
    _oBusiness.setWriter(out);
    _oBusiness.setParameter(request);
    _oBusiness.process();

    long next = Long.parseLong(_p_next);

    String _next_p1 = _p_next;
    String _next_p2 = String.valueOf(next+10000000);


%>
</pre>
<form id=idform action=xml.jsp>
    <input type=text name=P_NO1 value="<%=_next_p1%>"> to  <input type=text name=P_NO2 value="<%=_next_p2%>"><BR>
    <input type=submit value=ok>
</form>
</body>
</HTML>

<script for=xmlData event=ondatasetcomplete>
    var Error = xmlData.XMLDocument.parseError;
//    alert("Reason="+Error.reason);
//    alert("Line="+Error.line);
//    alert("Code="+Error.ErrorCode);
    var t = xmlData.recordset.recordCount;
    idform.submit();
</script>

