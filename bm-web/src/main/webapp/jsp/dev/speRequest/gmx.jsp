<%@ page import="com.emis.schedule.ERP.U8.emisPOS2U8" %>
<%@ page import="com.emis.schedule.ERP.U8.emisPOS2U8_Sale" %>
<%--
  供美香一些客制的特别背景功能
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
  // 修改U8参数后清除原有的token(不然需要等2小时后才会重新获取新的)
  public void cleanU8Token(ServletContext servlet, HttpServletRequest request, JspWriter out) throws Exception{
    emisPOS2U8.cleanToken();
  }
%>
<%
  String sAct = request.getParameter("act");
  if("cleanU8Token".equals(sAct)){
    // http://erp.ks-gmx.com:9001/gmx_test/jsp/dev/speRequest/gmx.jsp?act=cleanU8Token
    cleanU8Token(application, request, out);
    session.invalidate();
  } else if("cleanPayMap".equals(sAct)){
    // http://erp.ks-gmx.com:9001/gmx_test/jsp/dev/speRequest/gmx.jsp?act=cleanPayMap
    emisPOS2U8_Sale.cleanPayMap();
  } else if("POS2U8_BREAK".equalsIgnoreCase(sAct)){
    //http://erp.ks-gmx.com:8048/gmx4/jsp/dev/speRequest/gmx.jsp?act=POS2U8_BREAK
    emisPOS2U8.POS2ERP_BREAK = !emisPOS2U8.POS2ERP_BREAK;
    out.println("emisPOS2U8.POS2ERP_BREAK >> "+emisPOS2U8.POS2ERP_BREAK);
  } else if("POS2U8_RELOAD_EX_GOODS".equalsIgnoreCase(sAct)){
    //http://erp.ks-gmx.com:8048/gmx4/jsp/dev/speRequest/gmx.jsp?act=POS2U8_RELOAD_EX_GOODS
    emisPOS2U8.reloadExcludeGoods = true;
    out.println("emisPOS2U8.reloadExcludeGoods >> "+emisPOS2U8.reloadExcludeGoods);
  }
%>
