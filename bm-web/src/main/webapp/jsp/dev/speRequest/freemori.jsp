<%@ page import="com.emis.db.emisDb" %>
<%@ page import="com.emis.mail.emisMailer" %>
<%@ page import="com.emis.util.emisDate" %>
<%@ page import="com.emis.db.emisProp" %>
<%--
  浮力森林一些客制的特别背景功能
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
  // 删除与SAP对接的中间库的历史数据
  public void deleteSapData(ServletContext servlet, HttpServletRequest request, JspWriter out) throws Exception{
    /*
    insert into SCHED(S_NAME,S_SERVER,S_DESC,S_CLASS,RUNLEVEL,SYEAR,SMONTH,SDAY,SHOUR,STIME,INTERVAL,PARAM,SHOUR_END,STIME_END,S_MENU,THREAD_GROUP,REMARK)
    values (N'specialSched-deleteSapData',N'freemori3',N'特别排程-删除SAP对接中间库历史数据-浮力森林',N'com.emis.schedule.epos.emisSpecialSched',N'D',N'',N'',N'',N'04',N'30',N'600',N'http://localhost:8601/freemori/jsp/dev/speRequest/freemori.jsp?act=deleteSapData',N'04',N'30',N'6',N'OTHER_GROUP',null)
   */
    emisDb db = null;
    StringBuffer content = new StringBuffer();

    try{
      String deleteDate = new emisDate().add(-90).toString();  //清理3个月之前的资料。
      content.append("** Delete SAP data before ").append(deleteDate);
      emisProp prop = emisProp.getInstance(servlet);
      String sapSchema = prop.get("SAP_SCHEMA"); //SAPFE3
      db = emisDb.getInstance(servlet, "SAP");
      //db.setAutoCommit(true);
      int delRows = db.executeUpdate(
          "delete from "+sapSchema+".ZIFT0071 d " +
          "where exists(select ORDNO from "+sapSchema+".ZIFT007 h where h.MANDT=d.MANDT and h.ORDNO=d.ORDNO and ODDATE<'"+deleteDate+"')"
      );
      db.commit();
      content.append(" >> ").append(sapSchema).append(".ZIFT0071:").append(delRows);
      delRows = db.executeUpdate("delete from "+sapSchema+".ZIFT007 where ODDATE<'"+deleteDate+"'");
      db.commit();
      content.append(" >> ").append(sapSchema).append(".ZIFT007:").append(delRows);
      content.append(" >> Ok!");
    } catch(Exception e){
      e.printStackTrace();
      content.append(" >> Failure >> ").append(e.getMessage());
    } finally{
      if(db!=null){
        db.close();
      }
    }
    out.clear();
    out.print(content.toString());
    content.setLength(0);
    content = null;
  }
%>
<%
  String sAct = request.getParameter("act");
  if("deleteSapData".equals(sAct)){
    deleteSapData(application, request, out);
    session.invalidate();
  }
%>
