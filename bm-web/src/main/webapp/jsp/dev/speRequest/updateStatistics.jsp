<%@ page import="com.emis.db.emisDb" %>
<%@ page import="com.emis.mail.emisMailer" %>
<%@ page import="com.emis.db.emisProp" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.emis.util.emisDate" %>
<%--
  长沙罗莎-定期更新大数据量表的统计信息，以便查询优化器选择最佳的执行路径
  因罗莎有几次出现过日结排程执行非常慢的情况，执行 update statistics后就恢复正常了。
  (通过emisSpecialSched排程调用,排程设定参考如下SQL,可设为一个固定几天执行)
  /*
  INSERT INTO [sched]([S_NAME], [S_SERVER], [S_DESC], [S_CLASS], [RUNLEVEL], [SYEAR], [SMONTH], [SDAY], [SHOUR], [STIME], [INTERVAL], [PARAM], [SHOUR_END], [STIME_END], [S_MENU], [THREAD_GROUP], [REMARK])
  VALUES(N'updateStatistics', N'STOP', N'特别排程-长沙罗莎-更新大表数据统计信息', N'com.emis.schedule.epos.emisSpecialSched', N'I', N'', N'', N'', N'', N'', N'60', N'http://localhost:8109/rosa2/jsp/dev/speRequest/updateStatistics.jsp?week=6&mailto=', N'', N'', '6', N'', NULL)
   */
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String week = request.getParameter("week");// 可通过week参数指定周几执行(0 是周日， 1 -6 表示周一至周六)
  if(week == null || "".equals(week) || (new emisDate()).getWeek() == Integer.parseInt(week)) {
    emisDb db = null;
    StringBuffer context = new StringBuffer();
    List<String> updTables = new ArrayList<String>();
    int i = 1;
    try {
      //设定需要执行update的table
      updTables.add("sale_h");
      updTables.add("sale_d");
      updTables.add("sale_d_bom");
      updTables.add("tran_h");
      updTables.add("tran_d");
      updTables.add("count_h");
      updTables.add("count_d");
      updTables.add("dclose_report");
      updTables.add("ins_h");
      updTables.add("ins_d");
      updTables.add("Cwsend_h");
      updTables.add("Cwsend_d");
      updTables.add("S_STOR_H");
      updTables.add("S_STOR_D");
      updTables.add("S_STOR_BOM");
      updTables.add("sellgt_h");
      updTables.add("sellgt_d");
      updTables.add("sellgt_se");

      updTables.add("BACK_H");
      updTables.add("BACK_D");
      updTables.add("SCRAP_H");
      updTables.add("SCRAP_D");
      updTables.add("USELESS_H");
      updTables.add("USELESS_D");

      db = emisDb.getInstance(application);
      db.setAutoCommit(true);
      for (String table : updTables) {
        db.execute("UPDATE STATISTICS " + table);
      }
      context.append("更新表统计信息(update statistics)成功：<br/>");
      context.append(updTables.toString());
      out.clear();
      out.print("update statistics success:"+updTables.toString());
    } catch (Exception e) {
      e.printStackTrace();
      context.append("更新表统计信息(update statistics)失败：<br/>");
      context.append(updTables.toString()).append("<br/>");
      context.append(e.getMessage());
      out.clear();
      out.print("update statistics failure:"+updTables.toString()+"\n"+e.getMessage());
    } finally {
      if (db != null) {
        db.close();
        db = null;
      }
    }
    emisProp prop = emisProp.getInstance(application);
    context.append("<br/>").append("====================================")
        .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
        .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
        .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName())
        .append("<br/>").append("执行程序：").append("jsp/dev/speRequest/updateStatistics.jsp");
    emisMailer m = new emisMailer();
    //可通过参数mailto指定邮件接收者
    if (request.getParameter("mailto") != null && !"".equals(request.getParameter("mailto"))) {
      m.setTo(request.getParameter("mailto"));
    } else {
      m.setTo("andy.he@emiszh.com");
    }
    m.setSubject("【" + prop.get("EPOS_COMPANY_NO") + "】UPDATE STATISTICS");
    m.setContent(context.toString());
    m.send(application);
    context.setLength(0);
    context = null;
  } else {
    out.clear();
    out.print("**** 当前周别非设定执行的周别["+week+"] ****");
  }
%>
