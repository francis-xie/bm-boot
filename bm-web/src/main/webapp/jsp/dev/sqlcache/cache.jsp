<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.emis.db.*,java.util.*" %>
<html>
<body>
    <table border=1 width="90%">
<%
    HashMap _oMap = emisSQLCache.getHash(application);
    Set _oSet = _oMap.keySet();
    Iterator it = _oSet.iterator();

    while (it.hasNext())
    {
        Object _okey = it.next();
        out.println("<TR><TD>"+_okey+"</TD><TD><select><option>"+_oMap.get(_okey)+"</option></select></TD></TR>");
    }

%>
  </table>

</body>
</html>
