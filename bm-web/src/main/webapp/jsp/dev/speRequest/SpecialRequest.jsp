<%@ page import="com.emis.db.emisDb" %>
<%@ page import="com.emis.mail.emisMailer" %>
<%@ page import="com.emis.db.emisProp" %>
<%@ page import="com.emis.util.emisDate" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="com.emis.util.emisLogger" %>
<%@ page import="com.emis.util.emisUtil" %>
<%@ page import="com.emis.db.emisRowSet" %>
<%@ page import="com.emis.schedule.epos.dailyclose.emisDailyCloseCount" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.emis.file.emisDirectory" %>
<%@ page import="com.emis.file.emisFileMgr" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.emis.server.emisServer" %>
<%@ page import="com.emis.server.emisServerFactory" %>
<%@ page import="java.sql.Types" %>
<%@ page import="net.sf.json.JSONObject" %>
<%@ page import="java.util.zip.ZipOutputStream" %>
<%@ page import="java.util.zip.ZipEntry" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
  /*
    长沙罗莎-定期更新大数据量表的统计信息，以便查询优化器选择最佳的执行路径
    因罗莎有几次出现过日结排程执行非常慢的情况，执行 update statistics后就恢复正常了。
    (通过emisSpecialSched排程调用,排程设定参考如下SQL,可设为一个固定几天执行)
    INSERT INTO [sched]([S_NAME], [S_SERVER], [S_DESC], [S_CLASS], [RUNLEVEL], [SYEAR], [SMONTH], [SDAY], [SHOUR], [STIME], [INTERVAL], [PARAM], [SHOUR_END], [STIME_END], [S_MENU], [THREAD_GROUP], [REMARK])
    VALUES(N'updateStatistics', N'STOP', N'特别排程-长沙罗莎-更新大表数据统计信息', N'com.emis.schedule.epos.emisSpecialSched', N'I', N'', N'', N'', N'', N'', N'60', N'http://localhost:8109/rosa2/jsp/dev/speRequest/SpecialRequest.jsp?act=updateStatistics&week=&mailto=', N'', N'', '6', N'', NULL)
  */
  public void updateStatistics(ServletContext application, HttpServletRequest request,JspWriter out){
    try {
      String week = request.getParameter("week");// 可通过week参数指定周几执行(0 是周日， 1 -6 表示周一至周六)
      if (week == null || "".equals(week) || (new emisDate()).getWeek() == Integer.parseInt(week)) {
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
          out.print("update statistics success:" + updTables.toString());
          try{
            out.print("**** 20180825 =" + (new emisDate().toString()));
            if("20180825".equals(new emisDate().toString())) {
              String [] dbccTables = new String[]{"GIFT_TOKEN","CUST_V","BARCARD_AMT","SALE_H","SALE_D","SALE_CARD","SALE_ORDER_CARD","IC_GIFT_RECHARGE","GIFT_LIFE"};
              for(int j = 0; j<dbccTables.length; j++){
                db.execute("DBCC DBREINDEX ("+dbccTables[j]+",'' ,80)");
                System.err.println("*************  DBCC DBREINDEX "+dbccTables[j]+" OK ************");
                context.append("DBCC DBREINDEX "+dbccTables[j]+" OK <br/>");
              }
              int row = db.executeUpdate("delete gl from GIFT_LIFE gl with(nolock) inner join GIFT_TOKEN_@BK gt on gt.GT_NO=gl.GI_NO");
              context.append("delete GIFT_LIFE >> "+row+" <br/>");
              db.executeUpdate("DBCC SHRINKDATABASE(freemori3)");
            }
            out.println(context.toString());
          } catch(Exception ee){
            ee.printStackTrace();
          }
        } catch (Exception e) {
          e.printStackTrace();
          context.append("更新表统计信息(update statistics)失败：<br/>");
          context.append(updTables.toString()).append("<br/>");
          context.append(e.getMessage());
          out.clear();
          out.print("update statistics failure:" + updTables.toString() + "\n" + e.getMessage());
        } finally {
          if (db != null) {
            db.close();
            db = null;
          }
        }
        emisServer oServer = emisServerFactory.getServer(application);
        String sServerName = oServer.getServerName();
        emisProp prop = emisProp.getInstance(application);
        context.append("<br/>").append("====================================")
            .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
            .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE")).append("[").append(sServerName).append("]")
            .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName())
            .append("<br/>").append("执行程序：").append("jsp/dev/speRequest/SpecialRequest.jsp");
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
        out.print("**** 当前周别非设定执行的周别[" + week + "] ****");
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  /*
    因九园客户环境中毒,暂将环境移至我们的西数云上,暂每天将DB备份发送给ERP
    INSERT INTO [sched]([S_NAME], [S_SERVER], [S_DESC], [S_CLASS], [RUNLEVEL], [SYEAR], [SMONTH], [SDAY], [SHOUR], [STIME], [INTERVAL], [PARAM], [SHOUR_END], [STIME_END], [S_MENU], [THREAD_GROUP], [REMARK])
VALUES(N'emailJiuyuanDb2ERP', N'STOP', N'特别排程-九园包子-邮件发送POS的数据库备份给ERP', N'com.emis.schedule.epos.emisSpecialSched', N'I', N'', N'', N'', N'', N'', N'60', N'http://localhost:8059/jiuyuan/jsp/dev/speRequest/SpecialRequest.jsp?act=emailJiuyuanDb2ERP&mailto=', N'', N'', '6', N'', NULL)
   */
  private void emailJiuyuanDb2ERP(ServletContext application, HttpServletRequest request,JspWriter out) throws Exception {
    try {
      int week = new emisDate().getWeek();
      File bak = new File("D:\\projects\\BackupDb", "jiuyuan.bak");
      File zip = new File("D:\\projects\\BackupDb", "jiuyuan" + week + ".zip");
      File ok = new File("D:\\projects\\BackupDb", "jiuyuan" + week + "-email.ok");
      if (!bak.exists() && !ok.exists() && zip.exists()) {
        StringBuffer context = new StringBuffer();
        emisProp prop = emisProp.getInstance(application);
        context.append("九园POS系统DB备份如附件,请查阅!<br/>");
        context.append("<br/>").append("====================================")
            .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
            .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
            .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName());
        //.append("<br/>").append("发&nbsp;送&nbsp;者：").append("系统自动发送");

        String mailto = "andy.he@emiszh.com";

        emisMailer m = new emisMailer();
        m.setAttachment(zip.getPath());
        m.setSubject("【" + prop.get("EPOS_COMPANY") + "】POS系统DB备份");
        m.setContent(context.toString());
        m.setTo(mailto);
        m.send(application);

        context.setLength(0);
        context = null;
        ok.mkdir();
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * 长沙罗莎备份资料至历史数据库
   */
  public void backupData2History(ServletContext application, HttpServletRequest request,JspWriter out){
    Logger log = null;
    try {
      log  = emisLogger.getlog4j(application,"jsp.dev.speRequest.SpecialRequest.backupData2History");
      List<String[]> sqllist = new ArrayList<String[]>();
      sqllist.add(new String[]{
          "DCLOSE_REPORT",
          "insert into [DCLOSE_REPORT] ([DC_DATE],[S_NO],[P_NO],[DC_PRICE],[DC_QTY_BEGIN],[DC_QTY_SS],[DC_QTY_IN],[DC_QTY_BA],[DC_QTY_SL],[DC_QTY_TO],[DC_QTY_TI],[DC_QTY_US],[DC_QTY_PC],[DC_QTY_SH],[DC_QTY_PA],[DC_QTY_WP],[DC_QTY_WB],[DC_QTY_END],[CRE_DATE],[CRE_TIME],[CRE_USER],[DC_QTY_CH],[DC_QTY_SC],[DC_SPICK],[DC_BACK],[DC_CWSEND],[DC_QTY_BOM_SL],[DC_QTY_BOM_SS],[PS_QTY],[DC_QTY_CH_ADD],[DC_QTY_CH_LOSS],[DC_QTY_US_ADD],[DC_QTY_US_LOSS]) \n" +
              "select [DC_DATE],[S_NO],[P_NO],[DC_PRICE],[DC_QTY_BEGIN],[DC_QTY_SS],[DC_QTY_IN],[DC_QTY_BA],[DC_QTY_SL],[DC_QTY_TO],[DC_QTY_TI],[DC_QTY_US],[DC_QTY_PC],[DC_QTY_SH],[DC_QTY_PA],[DC_QTY_WP],[DC_QTY_WB],[DC_QTY_END],[CRE_DATE],[CRE_TIME],[CRE_USER],[DC_QTY_CH],[DC_QTY_SC],[DC_SPICK],[DC_BACK],[DC_CWSEND],[DC_QTY_BOM_SL],[DC_QTY_BOM_SS],[PS_QTY],[DC_QTY_CH_ADD],[DC_QTY_CH_LOSS],[DC_QTY_US_ADD],[DC_QTY_US_LOSS] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[DCLOSE_REPORT] d with (nolock)\n" +
              "where DC_DATE=convert(nvarchar(8),getdate()-180-1,112)"
      });
      sqllist.add(new String[]{
          "COUNT_D",
          "insert into [COUNT_D] ([CO_NO],[S_NO],[RECNO],[P_NO],[PS_COST],[PREV_QTY],[CO_QTY],[STOCK_QTY],[REMARK],[PREV_QTY_OLD],[RECNO_TEMP],[SL_SOURCE],[TR_WAY_QTY]) \n" +
              "select [CO_NO],[S_NO],[RECNO],[P_NO],[PS_COST],[PREV_QTY],[CO_QTY],[STOCK_QTY],[REMARK],[PREV_QTY_OLD],[RECNO_TEMP],[SL_SOURCE],[TR_WAY_QTY] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[COUNT_D] d with (nolock)\n" +
              "where exists(select CO_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[COUNT_H] with (nolock) where CO_NO=d.CO_NO and S_NO=d.S_NO and CO_DATE=convert(nvarchar(8),getdate()-90-1,112))"
      });
      sqllist.add(new String[]{
          "COUNT_H",
          "insert into [COUNT_H] ([CO_NO],[S_NO],[CO_DATE],[FLD_NO],[FLS_NO],[PS_DATE],[PS_TIME],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[EOM_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[CRE_TIME],[SL_KEY],[PM_NO],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[RE_DATE],[RE_TIME]) \n" +
              "select [CO_NO],[S_NO],[CO_DATE],[FLD_NO],[FLS_NO],[PS_DATE],[PS_TIME],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[EOM_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[CRE_TIME],[SL_KEY],[PM_NO],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[RE_DATE],[RE_TIME] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[COUNT_H] d with (nolock)\n" +
              "where CO_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });

      sqllist.add(new String[]{
          "TRAN_D",
          "insert into [TRAN_D] ([TR_NO],[S_NO_OUT],[RECNO],[P_NO],[TR_COST],[TR_OUT_QTY],[TR_OUT_AMT],[TR_IN_QTY],[TR_ORI_QTY],[TR_RESION],[GT_NO_S],[GT_NO_E],[TR_OUT_REASON],[P_PRICE],[REMARK],[IS_CHANGE_CARD],[UN_NO],[tr_in_amt]) \n" +
              "select [TR_NO],[S_NO_OUT],[RECNO],[P_NO],[TR_COST],[TR_OUT_QTY],[TR_OUT_AMT],[TR_IN_QTY],[TR_ORI_QTY],[TR_RESION],[GT_NO_S],[GT_NO_E],[TR_OUT_REASON],[P_PRICE],[REMARK],[IS_CHANGE_CARD],[UN_NO],[tr_in_amt] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[TRAN_D] d with (nolock)\n" +
              "where /*isnull(d.GT_NO_S,'')='' and*/ exists( select TR_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[TRAN_H] with (nolock)  where TR_NO=d.TR_NO and S_NO_OUT=d.S_NO_OUT and TR_DATE=convert(nvarchar(8),getdate()-90-1,112))"
      });
      sqllist.add(new String[]{
          "TRAN_H",
          "insert into [TRAN_H] ([TR_NO],[S_NO_OUT],[S_NO_IN],[TR_DATE],[PS_DATE],[PO_NO],[IN_NO],[FLD_NO],[TR_OUT_QTY],[TR_OUT_AMT],[FLS_NO],[TR_MONTH],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[TR_ORI_NO],[PS_USER],[ORI_S_NO],[PS_TIME],[UPD_TIME],[SL_KEY],[SL_KEY_OUT],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[OP_NO],[CO_USER],[CO_DATE],[CO_TIME]) \n" +
              "select [TR_NO],[S_NO_OUT],[S_NO_IN],[TR_DATE],[PS_DATE],[PO_NO],[IN_NO],[FLD_NO],[TR_OUT_QTY],[TR_OUT_AMT],[FLS_NO],[TR_MONTH],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[TR_ORI_NO],[PS_USER],[ORI_S_NO],[PS_TIME],[UPD_TIME],[SL_KEY],[SL_KEY_OUT],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[OP_NO],[CO_USER],[CO_DATE],[CO_TIME] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[TRAN_H] d with (nolock)\n" +
              "where TR_DATE=convert(nvarchar(8),getdate()-90-1,112) "
              //"  and not exists(select TR_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[TRAN_D] where TR_NO=d.TR_NO and S_NO_OUT=d.S_NO_OUT and isnull(GT_NO_S,'')!='')"
      });

      sqllist.add(new String[]{
          "INS_D",
          "insert into [INS_D] ([IN_NO],[S_NO],[RECNO],[P_NO],[P_COST],[P_QTY],[P_AMT],[P_ADD_QTY],[UN_NO],[IN_RATE],[IS_PO],[P_SDISC_AMT],[PV_PRICE_BAT],[QTY_BAT],[P_SUBJOIN_QTY],[PK_QTY],[P_PRICE],[F_TAX],[F_TAX_RATE],[F_WOTAXAMT],[F_TAXAMT],[F_AMT],[E_DATE],[PO_NO],[BOX_ID],[P_AMT_QTY_BAT],[P_NO_BOM],[BOM_QTY],[DIFF_REASON]) \n" +
              "select [IN_NO],[S_NO],[RECNO],[P_NO],[P_COST],[P_QTY],[P_AMT],[P_ADD_QTY],[UN_NO],[IN_RATE],[IS_PO],[P_SDISC_AMT],[PV_PRICE_BAT],[QTY_BAT],[P_SUBJOIN_QTY],[PK_QTY],[P_PRICE],[F_TAX],[F_TAX_RATE],[F_WOTAXAMT],[F_TAXAMT],[F_AMT],[E_DATE],[PO_NO],[BOX_ID],[P_AMT_QTY_BAT],[P_NO_BOM],[BOM_QTY],[DIFF_REASON] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[INS_D] d with (nolock)\n" +
              "where exists(select IN_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[INS_H] with (nolock) where IN_NO=d.IN_NO and S_NO=d.S_NO and IN_DATE=convert(nvarchar(8),getdate()-90-1,112))"
      });
      sqllist.add(new String[]{
          "INS_H",
          "insert into [INS_H] ([IN_NO],[S_NO],[PO_NO],[IN_DATE],[PS_DATE],[T_DATE],[T_NO],[V_NO],[FLD_NO],[FLS_NO],[IN_PAY_AMT],[IN_MONTH],[P_QTY],[P_AMT],[P_DISC_RATE],[P_SDISC_AMT],[F_TAX],[F_WOTAXAMT],[F_TAXAMT],[F_AMT],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[FAXSEND_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[D_SOURCE],[SH_NO],[IS_TRAN_AP],[APB_NO],[SL_KEY],[PM_NO],[SD_NO],[UPD_TIME],[PS_TIME],[PS_USER],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME]) \n" +
              "select [IN_NO],[S_NO],[PO_NO],[IN_DATE],[PS_DATE],[T_DATE],[T_NO],[V_NO],[FLD_NO],[FLS_NO],[IN_PAY_AMT],[IN_MONTH],[P_QTY],[P_AMT],[P_DISC_RATE],[P_SDISC_AMT],[F_TAX],[F_WOTAXAMT],[F_TAXAMT],[F_AMT],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[FAXSEND_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[D_SOURCE],[SH_NO],[IS_TRAN_AP],[APB_NO],[SL_KEY],[PM_NO],[SD_NO],[UPD_TIME],[PS_TIME],[PS_USER],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[INS_H] d with (nolock)\n" +
              "where IN_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "CWSEND_D",
          "insert into [CWSEND_D] ([S_NO_OUT],[WD_NO],[RECNO],[P_SALE_W],[S_NO_IN],[V_NO],[PS_COST],[WD_RATE],[P_NO],[PD_NO],[POR_QTY],[P_QTY],[UN_NO],[P_COST],[F_AMT],[F_AMTWOTAX],[F_TAXAMT],[REMARK],[BOX_ID],[P_PRICE],[PV_PRICE_BAT],[PV_RATE],[CHECK_QTY]) \n" +
              "select [S_NO_OUT],[WD_NO],[RECNO],[P_SALE_W],[S_NO_IN],[V_NO],[PS_COST],[WD_RATE],[P_NO],[PD_NO],[POR_QTY],[P_QTY],[UN_NO],[P_COST],[F_AMT],[F_AMTWOTAX],[F_TAXAMT],[REMARK],[BOX_ID],[P_PRICE],[PV_PRICE_BAT],[PV_RATE],[CHECK_QTY] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[CWSEND_D] d with (nolock)\n" +
              "where exists(select WD_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[CWSEND_H] with (nolock) where WD_NO=d.WD_NO and S_NO_OUT=d.S_NO_OUT and WD_DATE=convert(nvarchar(8),getdate()-90-1,112))"
      });
      sqllist.add(new String[]{
          "CWSEND_H",
          "insert into [CWSEND_H] ([S_NO_OUT],[WD_NO],[S_NO_IN],[PD_NO],[WD_DATE],[F_TYPE],[F_DATE],[F_CLS_YM],[F_TAX],[F_TAX_RATE],[F_CUR],[D_CUR_RATE],[F_AMT],[F_AMTWOTAX],[F_TAXAMT],[REMARK],[FLD_NO],[FLS_NO],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[PRINT_TM],[EDI_TM],[CRE_TIME],[SD_NO],[PM_NO],[PA_NO],[UPD_TIME],[FLS_NO2]) \n" +
              "select [S_NO_OUT],[WD_NO],[S_NO_IN],[PD_NO],[WD_DATE],[F_TYPE],[F_DATE],[F_CLS_YM],[F_TAX],[F_TAX_RATE],[F_CUR],[D_CUR_RATE],[F_AMT],[F_AMTWOTAX],[F_TAXAMT],[REMARK],[FLD_NO],[FLS_NO],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[PRINT_TM],[EDI_TM],[CRE_TIME],[SD_NO],[PM_NO],[PA_NO],[UPD_TIME],[FLS_NO2] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[CWSEND_H] d with (nolock)\n" +
              "where WD_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "USELESS_D",
          "insert into [USELESS_D] ([US_NO],[S_NO],[RECNO],[P_NO],[US_COST],[US_QTY],[US_REASON],[US_REAMARK],[GT_NO_S],[GT_NO_E],[P_PRICE]) \n" +
              "select [US_NO],[S_NO],[RECNO],[P_NO],[US_COST],[US_QTY],[US_REASON],[US_REAMARK],[GT_NO_S],[GT_NO_E],[P_PRICE] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[USELESS_D] d with (nolock)\n" +
              "where exists(select US_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[USELESS_H] with (nolock) where US_NO=d.US_NO and S_NO=d.S_NO and US_DATE=convert(nvarchar(8),getdate()-90-1,112))"
      });
      sqllist.add(new String[]{
          "USELESS_H",
          "insert into [USELESS_H] ([US_NO],[S_NO],[US_DATE],[PS_DATE],[FLD_NO],[FLS_NO],[US_REASON],[US_MONTH],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[SL_KEY],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[UPD_TIME],[PS_TIME],[PS_USER]) \n" +
              "select [US_NO],[S_NO],[US_DATE],[PS_DATE],[FLD_NO],[FLS_NO],[US_REASON],[US_MONTH],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[SL_KEY],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[UPD_TIME],[PS_TIME],[PS_USER] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[USELESS_H] d with (nolock)\n" +
              "where US_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "S_STOR_BOM",
          "insert into [S_STOR_BOM] ([SST_NO],[RECNO_D],[RECNO],[S_NO],[BP_NO],[P_NO],[B_QTY],[SL_QTY],[B_PS_QTY],[SPK_DATE],[P_PRICE],[P_AMT]) \n" +
              "select [SST_NO],[RECNO_D],[RECNO],[S_NO],[BP_NO],[P_NO],[B_QTY],[SL_QTY],[B_PS_QTY],[SPK_DATE],[P_PRICE],[P_AMT] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[S_STOR_BOM] d with (nolock)\n" +
              "where SPK_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "S_STOR_D",
          "insert into [S_STOR_D] ([S_NO],[SST_NO],[RECNO],[P_NO],[SST_QTY],[PLN_QTY],[REASON],[RL_QTY],[PLN_REASON],[SST_PRICE],[P_PRICE],[CHK_QTY],[SST_COST]) \n" +
              "select [S_NO],[SST_NO],[RECNO],[P_NO],[SST_QTY],[PLN_QTY],[REASON],[RL_QTY],[PLN_REASON],[SST_PRICE],[P_PRICE],[CHK_QTY],[SST_COST] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[S_STOR_D] d with (nolock)\n" +
              "where exists(select SST_NO from dl_rosa2_Vi.rosa2_Vi.dbo.S_STOR_H with (nolock) where S_NO=d.S_NO and SST_NO=d.SST_NO and SPK_DATE=convert(nvarchar(8),getdate()-90-1,112))"
      });
      sqllist.add(new String[]{
          "S_STOR_H",
          "insert into [S_STOR_H] ([S_NO],[SST_NO],[FLS_NO],[SD_NO],[SPK_DATE],[SPL_NO],[REMARK],[CRE_DATE],[CRE_TIME],[CRE_USER],[UPD_DATE],[UPD_USER],[SL_KEY],[SST_SOURCE],[UPD_TIME],[PS_DATE],[PS_TIME],[PS_USER]) \n" +
              "select [S_NO],[SST_NO],[FLS_NO],[SD_NO],[SPK_DATE],[SPL_NO],[REMARK],[CRE_DATE],[CRE_TIME],[CRE_USER],[UPD_DATE],[UPD_USER],[SL_KEY],[SST_SOURCE],[UPD_TIME],[PS_DATE],[PS_TIME],[PS_USER] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[S_STOR_H] d with (nolock)\n" +
              "where SPK_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "BACK_D",
          "insert into [BACK_D] ([BA_NO],[S_NO],[RECNO],[P_NO],[P_COST],[P_QTY],[P_AMT],[P_ADD_QTY],[UN_NO],[BA_RATE],[P_SDISC_AMT],[PV_PRICE_BAT],[QTY_BAT],[P_SUBJOIN_QTY],[P_PRICE],[BA_REASON],[F_TAX],[F_TAX_RATE],[F_WOTAXAMT],[F_TAXAMT],[F_AMT],[BA_CHK_Q],[IS_CRAP],[P_NO_BOM],[BOM_QTY]) \n" +
              "select [BA_NO],[S_NO],[RECNO],[P_NO],[P_COST],[P_QTY],[P_AMT],[P_ADD_QTY],[UN_NO],[BA_RATE],[P_SDISC_AMT],[PV_PRICE_BAT],[QTY_BAT],[P_SUBJOIN_QTY],[P_PRICE],[BA_REASON],[F_TAX],[F_TAX_RATE],[F_WOTAXAMT],[F_TAXAMT],[F_AMT],[BA_CHK_Q],[IS_CRAP],[P_NO_BOM],[BOM_QTY] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[BACK_D] d with (nolock)\n" +
              "where exists(select BA_NO from dl_rosa2_Vi.rosa2_Vi.dbo.BACK_H with (nolock) where BA_NO=d.BA_NO and S_NO=d.S_NO and BA_DATE=convert(nvarchar(8),getdate()-90-1,112))"
      });
      sqllist.add(new String[]{
          "BACK_H",
          "insert into [BACK_H] ([BA_NO],[S_NO],[BA_DATE],[PS_DATE],[IN_NO],[V_NO],[FLD_NO],[FLS_NO],[BA_MONTH],[BA_PAY_AMT],[P_QTY],[P_AMT],[P_DISC_RATE],[P_SDISC_AMT],[F_TAX],[F_WOTAXAMT],[F_TAXAMT],[F_AMT],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[FAXSEND_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[T_NO],[D_SOURCE],[PS_USER],[BA_CONF_D],[IS_TRAN_AP],[APB_NO],[T_DATE],[SL_KEY],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[UPD_TIME],[PS_TIME]) \n" +
              "select [BA_NO],[S_NO],[BA_DATE],[PS_DATE],[IN_NO],[V_NO],[FLD_NO],[FLS_NO],[BA_MONTH],[BA_PAY_AMT],[P_QTY],[P_AMT],[P_DISC_RATE],[P_SDISC_AMT],[F_TAX],[F_WOTAXAMT],[F_TAXAMT],[F_AMT],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[FAXSEND_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[T_NO],[D_SOURCE],[PS_USER],[BA_CONF_D],[IS_TRAN_AP],[APB_NO],[T_DATE],[SL_KEY],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[UPD_TIME],[PS_TIME] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[BACK_H] d with (nolock)\n" +
              "where BA_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "SCRAP_D",
          "insert into [SCRAP_D] ([SC_NO],[S_NO],[RECNO],[P_NO],[SC_COST],[SC_QTY],[SC_REASON],[SC_REAMARK],[SC_DATE],[P_PRICE],[CHK_QTY]) \n" +
              "select [SC_NO],[S_NO],[RECNO],[P_NO],[SC_COST],[SC_QTY],[SC_REASON],[SC_REAMARK],[SC_DATE],[P_PRICE],[CHK_QTY] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[SCRAP_D] d with (nolock)\n" +
              "where exists(select SC_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[SCRAP_H] with (nolock) where SC_NO=d.SC_NO and S_NO=d.S_NO and SC_DATE=convert(nvarchar(8),getdate()-90-1,112))"
      });
      sqllist.add(new String[]{
          "SCRAP_H",
          "insert into [SCRAP_H] ([SC_NO],[S_NO],[SC_DATE],[PS_DATE],[FLD_NO],[FLS_NO],[SC_REASON],[SC_MONTH],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[SL_KEY],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[UPD_TIME],[PS_TIME],[PS_USER]) \n" +
              "select [SC_NO],[S_NO],[SC_DATE],[PS_DATE],[FLD_NO],[FLS_NO],[SC_REASON],[SC_MONTH],[REMARK],[CRE_USER],[CRE_DATE],[UPD_USER],[UPD_DATE],[CRE_TIME],[EOM_DATE],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[SL_KEY],[POS2ERP_NO],[POS2ERP_FLAG],[POS2ERP_TIME],[POS2ERP_REC_TIME],[SD_NO],[UPD_TIME],[PS_TIME],[PS_USER] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[SCRAP_H] d with (nolock)\n" +
              "where SC_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "RAT_PREALLOT_P",
          "insert into [RAT_PREALLOT_P] ([PA_NO],[P_NO],[RECNO],[PS_QTY],[S_NO_OUT]) \n" +
              "select [PA_NO],[P_NO],[RECNO],[PS_QTY],[S_NO_OUT] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[RAT_PREALLOT_P] d with (nolock)\n" +
              "where exists(select PA_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[RAT_PREALLOT_H] with (nolock) where PA_NO=d.PA_NO and S_NO_OUT=d.S_NO_OUT and POR_DATE=convert(nvarchar(8),getdate()-30-1,112))"
      });
      sqllist.add(new String[]{
          "RAT_PREALLOT_S",
          "insert into [RAT_PREALLOT_S] ([PA_NO],[P_NO],[S_NO],[POR_QTY],[PA_QTY],[S_NO_OUT]) \n" +
              "select [PA_NO],[P_NO],[S_NO],[POR_QTY],[PA_QTY],[S_NO_OUT] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[RAT_PREALLOT_S] d with (nolock)\n" +
              "where exists(select PA_NO from dl_rosa2_Vi.rosa2_Vi.dbo.[RAT_PREALLOT_H] with (nolock) where PA_NO=d.PA_NO and S_NO_OUT=d.S_NO_OUT and POR_DATE=convert(nvarchar(8),getdate()-30-1,112))"
      });
      sqllist.add(new String[]{
          "RAT_PREALLOT_H",
          "insert into [RAT_PREALLOT_H] ([PA_NO],[POR_DATE],[PM_NO],[FLS_NO],[REMARK],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[CONF_DATE],[CONF_USER],[S_NO_OUT],[PO_D_DATE],[PO_D_TIME_S],[PO_D_TIME_E],[SEND_TIME],[IS_IMPORT_ZERO],[CRE_TIME],[CONF_TIME]) \n" +
              "select [PA_NO],[POR_DATE],[PM_NO],[FLS_NO],[REMARK],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[CONF_DATE],[CONF_USER],[S_NO_OUT],[PO_D_DATE],[PO_D_TIME_S],[PO_D_TIME_E],[SEND_TIME],[IS_IMPORT_ZERO],[CRE_TIME],[CONF_TIME] \n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.[RAT_PREALLOT_H] d with (nolock)\n" +
              "where POR_DATE=convert(nvarchar(8),getdate()-30-1,112)"
      });
      sqllist.add(new String[]{
          "SALE_H",
          "insert into SALE_H([SL_KEY],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[SL_TIME],[SL_INKIND],[SL_INVID],[C_NO],[OP_NO],[SA_NO],[SL_SOURCE],[SL_INVTYPE],[SL_INVNO_S],[SL_INVNO_E],[SL_KEY_O],[TIME_NO],[PR_NO],[CL_NO],[FLS_NO],[SL_CONFIRM_E],[SL_CONFIRM_D],[EO_DATE],[SL_QTY],[SL_AMT],[SL_TAXAMT],[SL_NOTAXAMT],[SL_DISC_AMT],[SL_NDISC_AMT],[SL_RECV_AMT],[PAY_AMT],[PAY_CASH],[PAY_CARD],[PAY_3],[PAY_4],[PAY_5],[PAY_6],[PAY_7],[PAY_8],[PAY_9],[PAY_10],[PAY_11],[PAY_12],[CARD_NO],[REMARK],[CRE_USER],[CRE_DATE],[EOM_DATE],[SL_EXP],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[SL_RAAMT],[SL_RAQTY],[SL_BUY_PERC],[SL_BUY_POINT1],[SL_BUY_POINT2],[SL_BUY_POINT3],[SL_TAXAMT_NOTAX],[SL_TAXAMT_TAX],[D_NO],[PAY_13],[IS_TRAN_AP],[ARB_NO],[P_TAX_R],[P_TAX],[SL_TYPE],[SHIFTWORK_TYPE],[STORE_SD],[POS2ERP_NO],[POS2ERP_FLAG],[CLIENT_COUNT],[SL_KEY_ORDER],[C_TEL],[C_ADDR],[SH_DATE],[C_NO_V],[PNT_TRFLAG],[SL_POINT],[GEN_POINT],[BNS_POINT],[BB_POINT],[DB_POINT],[PAY_POINT],[PA_POINT],[PA_NO],[PG_POINT],[PB_EXPNT],[PB_DISC],[CC_NO],[SYNC_HIST_FLAG],[POS2ERP_TIME],[CUST_PM_NO],[WO_NO],[SL_INVAMT],[PAY_15],[PAY_16],[PAY_17],[PAY_18],[PAY_19],[PAY_20],[PAY_21],[PAY_22],[PAY_23],[PAY_24],[PAY_25],[PAY_26],[PAY_27],[PAY_28],[PAY_29],[PAY_30],[PAY_31],[PAY_32],[PAY_33],[PAY_34],[PAY_35],[PAY_36],[PAY_37],[PAY_38],[PAY_39],[PAY_40],[PAY_41],[PAY_42],[PAY_43],[PAY_44],[PAY_45],[PAY_46],[PAY_47],[PAY_48],[PAY_49],[PAY_50],[OUT_TRADE_NO],[AMOUNT],[FUND_CHANNEL],[TRADE_NO],[BUYER_LOGON_ID],[EXT_TAX_AMT],[IS_SL_DP],[SL_EXP_DATE],[ALIPAY_DISC_AMT],[ALIPAY_REAL_AMT],[ALIPAY_RECE_AMT],[ALIPAY_INVO_AMT],[CRE_TIME],[wxpay_out_trade_no],[wxpay_transaction_id],[wxpay_coupon_fee],[wxpay_total_fee],[wxpay_refund_id],[IS_SEND_WECHAT_MSG],[SP_OP_NO],[POS2WX_FLAG],[POS2WX_DATETIME],[CHANNEL_SOURCE],[CHANNEL_NO],[CHANNEL_DEF1],[CHANNEL_DEF2],[CHANNEL_DEF3],[CHANNEL_DEF4])\n" +
          "SELECT [SL_KEY],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[SL_TIME],[SL_INKIND],[SL_INVID],[C_NO],[OP_NO],[SA_NO],[SL_SOURCE],[SL_INVTYPE],[SL_INVNO_S],[SL_INVNO_E],[SL_KEY_O],[TIME_NO],[PR_NO],[CL_NO],[FLS_NO],[SL_CONFIRM_E],[SL_CONFIRM_D],[EO_DATE],[SL_QTY],[SL_AMT],[SL_TAXAMT],[SL_NOTAXAMT],[SL_DISC_AMT],[SL_NDISC_AMT],[SL_RECV_AMT],[PAY_AMT],[PAY_CASH],[PAY_CARD],[PAY_3],[PAY_4],[PAY_5],[PAY_6],[PAY_7],[PAY_8],[PAY_9],[PAY_10],[PAY_11],[PAY_12],[CARD_NO],[REMARK],[CRE_USER],[CRE_DATE],[EOM_DATE],[SL_EXP],[DEF_V1],[DEF_V2],[DEF_N3],[DEF_N4],[SL_RAAMT],[SL_RAQTY],[SL_BUY_PERC],[SL_BUY_POINT1],[SL_BUY_POINT2],[SL_BUY_POINT3],[SL_TAXAMT_NOTAX],[SL_TAXAMT_TAX],[D_NO],[PAY_13],[IS_TRAN_AP],[ARB_NO],[P_TAX_R],[P_TAX],[SL_TYPE],[SHIFTWORK_TYPE],[STORE_SD],[POS2ERP_NO],[POS2ERP_FLAG],[CLIENT_COUNT],[SL_KEY_ORDER],[C_TEL],[C_ADDR],[SH_DATE],[C_NO_V],[PNT_TRFLAG],[SL_POINT],[GEN_POINT],[BNS_POINT],[BB_POINT],[DB_POINT],[PAY_POINT],[PA_POINT],[PA_NO],[PG_POINT],[PB_EXPNT],[PB_DISC],[CC_NO],[SYNC_HIST_FLAG],[POS2ERP_TIME],[CUST_PM_NO],[WO_NO],[SL_INVAMT],[PAY_15],[PAY_16],[PAY_17],[PAY_18],[PAY_19],[PAY_20],[PAY_21],[PAY_22],[PAY_23],[PAY_24],[PAY_25],[PAY_26],[PAY_27],[PAY_28],[PAY_29],[PAY_30],[PAY_31],[PAY_32],[PAY_33],[PAY_34],[PAY_35],[PAY_36],[PAY_37],[PAY_38],[PAY_39],[PAY_40],[PAY_41],[PAY_42],[PAY_43],[PAY_44],[PAY_45],[PAY_46],[PAY_47],[PAY_48],[PAY_49],[PAY_50],[OUT_TRADE_NO],[AMOUNT],[FUND_CHANNEL],[TRADE_NO],[BUYER_LOGON_ID],[EXT_TAX_AMT],[IS_SL_DP],[SL_EXP_DATE],[ALIPAY_DISC_AMT],[ALIPAY_REAL_AMT],[ALIPAY_RECE_AMT],[ALIPAY_INVO_AMT],[CRE_TIME],[wxpay_out_trade_no],[wxpay_transaction_id],[wxpay_coupon_fee],[wxpay_total_fee],[wxpay_refund_id],[IS_SEND_WECHAT_MSG],[SP_OP_NO],[POS2WX_FLAG],[POS2WX_DATETIME],[CHANNEL_SOURCE],[CHANNEL_NO],[CHANNEL_DEF1],[CHANNEL_DEF2],[CHANNEL_DEF3],[CHANNEL_DEF4]  \n" +
          "FROM dl_rosa2_Vi.rosa2_Vi.dbo.[SALE_H] with (nolock)\n" +
          "where SL_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "SALE_D",
          "insert into SALE_D ([SL_KEY],[RECNO],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[FLS_NO],[P_NO],[DP_NO],[P_TAX],[SL_QTY],[SL_PRICE],[SL_AMT],[SL_TAXAMT],[SL_NOTAXAMT],[SL_DISC_AMT],[SL_NDISC_AMT],[SL_COST],[PK_RECNO],[SL_RAAMT],[SL_RAQTY],[SL_DISC_RATE],[SL_BK_QTY],[SL_BUY_POINT1],[SL_BUY_POINT2],[SL_BUY_POINT3],[SL_TAXAMT_NOTAX],[SL_TAXAMT_TAX],[P_TAX_RATE],[D_DISC],[D_NO],[SEA_NO],[PG_POINT],[PG_NO],[PB_EXPNT],[PB_DISC],[PB_NO],[P_NAME],[SEA_AMT])\n" +
              "SELECT [SL_KEY],[RECNO],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[FLS_NO],[P_NO],[DP_NO],[P_TAX],[SL_QTY],[SL_PRICE],[SL_AMT],[SL_TAXAMT],[SL_NOTAXAMT],[SL_DISC_AMT],[SL_NDISC_AMT],[SL_COST],[PK_RECNO],[SL_RAAMT],[SL_RAQTY],[SL_DISC_RATE],[SL_BK_QTY],[SL_BUY_POINT1],[SL_BUY_POINT2],[SL_BUY_POINT3],[SL_TAXAMT_NOTAX],[SL_TAXAMT_TAX],[P_TAX_RATE],[D_DISC],[D_NO],[SEA_NO],[PG_POINT],[PG_NO],[PB_EXPNT],[PB_DISC],[PB_NO],[P_NAME],[SEA_AMT]\n" +
              "FROM dl_rosa2_Vi.rosa2_Vi.dbo.[SALE_D] with (nolock)\n" +
              "where SL_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "SALE_DIS",
          "insert into SALE_DIS ([SL_KEY],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[SL_SOURCE],[RECNO],[DISC_SN],[DISC_CODE],[DISC_AMT],[DISC_QTY],[DISC_TYPE],[DISC_NO],[REASON])\n" +
              "SELECT [SL_KEY],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[SL_SOURCE],[RECNO],[DISC_SN],[DISC_CODE],[DISC_AMT],[DISC_QTY],[DISC_TYPE],[DISC_NO],[REASON]\n" +
              "FROM dl_rosa2_Vi.rosa2_Vi.dbo.[SALE_DIS]  with (nolock)\n" +
              "where SL_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "SALE_PK",
          "insert into SALE_PK ([SL_KEY],[PK_RECNO],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[FLS_NO],[P_NO],[DP_NO],[P_TAX],[SL_QTY],[SL_PRICE],[SL_AMT],[SL_TAXAMT],[SL_NOTAXAMT],[SL_DISC_AMT],[SL_NDISC_AMT],[SL_COST],[SL_RAAMT],[SL_RAQTY],[SL_DISC_RATE],[SL_TAXAMT_NOTAX],[SL_TAXAMT_TAX],[P_TAX_RATE],[D_NO],[D_DISC])\n" +
              "SELECT [SL_KEY],[PK_RECNO],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[FLS_NO],[P_NO],[DP_NO],[P_TAX],[SL_QTY],[SL_PRICE],[SL_AMT],[SL_TAXAMT],[SL_NOTAXAMT],[SL_DISC_AMT],[SL_NDISC_AMT],[SL_COST],[SL_RAAMT],[SL_RAQTY],[SL_DISC_RATE],[SL_TAXAMT_NOTAX],[SL_TAXAMT_TAX],[P_TAX_RATE],[D_NO],[D_DISC]\n" +
              "FROM dl_rosa2_Vi.rosa2_Vi.dbo.[SALE_PK] with (nolock)\n" +
              "where SL_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "SALE_D_BOM",
          "insert into SALE_D_BOM([SL_KEY],[RECNO_D],[RECNO],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[FLS_NO],[BP_NO],[P_NO],[B_QTY],[SL_QTY],[B_PS_QTY],[P_PRICE],[P_AMT])\n" +
              "SELECT [SL_KEY],[RECNO_D],[RECNO],[S_NO],[ID_NO],[SL_DATE],[SL_NO],[FLS_NO],[BP_NO],[P_NO],[B_QTY],[SL_QTY],[B_PS_QTY],[P_PRICE],[P_AMT]\n" +
              "FROM dl_rosa2_Vi.rosa2_Vi.dbo.[SALE_D_BOM] with (nolock)\n" +
              "where SL_DATE=convert(nvarchar(8),getdate()-90-1,112)"
      });
      sqllist.add(new String[]{
          "DCLOSE_P_PRICE",
          "insert into DCLOSE_P_PRICE([DC_DATE],[P_NO],[DC_PRICE])\n" +
              "SELECT [DC_DATE],[P_NO],[DC_PRICE]\n" +
              "FROM dl_rosa2_Vi.rosa2_Vi.dbo.[DCLOSE_P_PRICE] with (nolock)\n" +
              "where DC_DATE=convert(nvarchar(8),getdate()-180-1,112)"
      });
      sqllist.add(new String[]{
          "STORE/Subdep/Depart/DDepart/Part/Store_sd_h/STORE_SD_D/Seasoning_h/Seasoning_d/BOM_H_S/BOM_D_S",
          "if(datepart(day,getdate())=1)\n" +
              "begin\n" +
              "truncate table rosa2_Vi_his.dbo.STORE\n" +
              "insert into rosa2_Vi_his.dbo.STORE([S_NO],[S_NAME],[S_NAME_S],[R_NO],[S_ID],[S_TAXNO],[S_KIND],[S_LEVEL],[S_ADDR],[S_TEL],[S_FAX],[S_EMAIL],[S_LEADER],[S_NUM],[S_AREA],[S_RENT],[S_FEE],[S_OPEN],[CO_DATE],[CO_TIME],[S_CUST_NUM],[S_CUST_PRICE],[PRE_D_AMT],[PRE_M_AMT],[PRE_Y_AMT],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[REMARK],[LR_NO],[V_NO],[S_INV_TITLE],[S_INV_ADDR],[S_IP],[IS_SOE],[S_TAX_R],[S_STATUS],[MU_NO],[S_TYPE],[S_REGION_B],[S_PROVINCE],[S_CITY],[S_REGION_S],[S_CLOSE_D],[S_NUM_P],[S_NUM_B],[COM_TYPE],[DESK_QTY],[B_BRAND],[S_HOLD],[K3_NO],[K3_FITEMID],[SALE_OUT_P],[SALE_PACK_P],[SEND_LINE],[S_MANAGER],[S_0TAX_INVCITY],[S_TR_NO],[INV_TYPE],[INV_MAIN],[C401_NAME],[C401_ID],[MU_NO_PLU],[MU_NO_FUN],[C401_NUMBER],[C401_TEL],[C401_EXTENSION],[ESC_COM_ID],[TABLES],[S_SALES],[EI_S_ID],[com_no],[S_BOSS],[S_SOURCE],[SP_NO],[WC_STORE],[MAP_LNG],[MAP_LAT],[WM_MU_NO],[S_DB_IP],[WECHAT_SEND_RANGE],[RETTERMNO],[DATAKEY],[SIGNKEY],[WEIMOB_ID])\n" +
              "select [S_NO],[S_NAME],[S_NAME_S],[R_NO],[S_ID],[S_TAXNO],[S_KIND],[S_LEVEL],[S_ADDR],[S_TEL],[S_FAX],[S_EMAIL],[S_LEADER],[S_NUM],[S_AREA],[S_RENT],[S_FEE],[S_OPEN],[CO_DATE],[CO_TIME],[S_CUST_NUM],[S_CUST_PRICE],[PRE_D_AMT],[PRE_M_AMT],[PRE_Y_AMT],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[REMARK],[LR_NO],[V_NO],[S_INV_TITLE],[S_INV_ADDR],[S_IP],[IS_SOE],[S_TAX_R],[S_STATUS],[MU_NO],[S_TYPE],[S_REGION_B],[S_PROVINCE],[S_CITY],[S_REGION_S],[S_CLOSE_D],[S_NUM_P],[S_NUM_B],[COM_TYPE],[DESK_QTY],[B_BRAND],[S_HOLD],[K3_NO],[K3_FITEMID],[SALE_OUT_P],[SALE_PACK_P],[SEND_LINE],[S_MANAGER],[S_0TAX_INVCITY],[S_TR_NO],[INV_TYPE],[INV_MAIN],[C401_NAME],[C401_ID],[MU_NO_PLU],[MU_NO_FUN],[C401_NUMBER],[C401_TEL],[C401_EXTENSION],[ESC_COM_ID],[TABLES],[S_SALES],[EI_S_ID],[com_no],[S_BOSS],[S_SOURCE],[SP_NO],[WC_STORE],[MAP_LNG],[MAP_LAT],[WM_MU_NO],[S_DB_IP],[WECHAT_SEND_RANGE],[RETTERMNO],[DATAKEY],[SIGNKEY],[WEIMOB_ID]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.STORE with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.Subdep\n" +
              "insert into rosa2_Vi_his.dbo.Subdep([SUBDEP],[SUB_NAME],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[P_BRAND])\n" +
              "select [SUBDEP],[SUB_NAME],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[P_BRAND]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.Subdep with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.Depart\n" +
              "insert into rosa2_Vi_his.dbo.Depart([D_NO],[GROUP],[D_CNAME],[D_ENAME],[D_TAX],[DISC],[D_TYPE],[D_PROF],[D_EST1],[D_EST2],[D_EST3],[D_EST4],[D_EST5],[D_EST6],[D_EST7],[D_EST8],[D_EST9],[D_EST10],[D_EST11],[D_EST12],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[REMARK],[D_SHOPPE],[D_SHOPPE_TYPE],[D_DOWN],[SEA_NO],[UN_NO],[SALE_OPT],[USED],[PR_TYPE],[DP_TYPE],[PAIMING],[P_DISC2],[P_DISC3],[P_DISC4],[P_DISC5],[P_DISC6],[P_DISC21],[D_NO_OD],[WM_MIN_ORDER_NUM],[WM_PACKAGE_BOX_NUM],[WM_PACKAGE_BOX_PRICE],[WM_USED],[WM_D_NO],[D_OVERTIME])\n" +
              "select [D_NO],[GROUP],[D_CNAME],[D_ENAME],[D_TAX],[DISC],[D_TYPE],[D_PROF],[D_EST1],[D_EST2],[D_EST3],[D_EST4],[D_EST5],[D_EST6],[D_EST7],[D_EST8],[D_EST9],[D_EST10],[D_EST11],[D_EST12],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[REMARK],[D_SHOPPE],[D_SHOPPE_TYPE],[D_DOWN],[SEA_NO],[UN_NO],[SALE_OPT],[USED],[PR_TYPE],[DP_TYPE],[PAIMING],[P_DISC2],[P_DISC3],[P_DISC4],[P_DISC5],[P_DISC6],[P_DISC21],[D_NO_OD],[WM_MIN_ORDER_NUM],[WM_PACKAGE_BOX_NUM],[WM_PACKAGE_BOX_PRICE],[WM_USED],[WM_D_NO],[D_OVERTIME]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.Depart with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.DDepart\n" +
              "insert into rosa2_Vi_his.dbo.DDepart([DD_NO],[D_NO],[DD_CNAME],[DD_ENAME],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[REMARK],[SEA_NO],[D_DOWN],[PRINT_LABLE],[DISP_FLAG])\n" +
              "select [DD_NO],[D_NO],[DD_CNAME],[DD_ENAME],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[REMARK],[SEA_NO],[D_DOWN],[PRINT_LABLE],[DISP_FLAG]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.DDepart with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.Part\n" +
              "insert into rosa2_Vi_his.dbo.Part([P_NO],[P_EAN],[P_NAME],[P_NAME_S],[P_TAX],[P_PU],[D_NO],[P_PHOTO],[P_PRICE],[P_PRICE2],[P_PRICE3],[P_PRICE4],[P_PRICE5],[P_PRICE6],[P_KIND],[UN_NO],[P_IN_UN],[P_RATE],[V_NO],[P_DEF1],[P_DEF2],[P_DEF4],[P_DEF3],[P_COST],[P_OUT_DATE],[P_IN_DATE],[P_LASTP],[P_IN_QTY],[P_TOT_Q],[P_STATUS],[P_STOP_DATE],[P_ABC],[P_PERCT],[P_PS_QTY],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[REMARK],[P_DEFA],[P_DEFB],[P_DEFC],[P_DEFD],[P_PRICE_ORI],[P_BUY_POINT],[P_POINT],[P_CHG_POINT],[P_MADE],[P_TAX_RATE],[F_TAX],[F_TAX_RATE],[DD_NO],[P_S_LEVEL],[IS_D_P_NO],[P_TR_DATE],[P_PY],[P_DAILE],[P_SPMODE],[P_DRAW_UN],[P_DRAW_RATE],[P_OUT_UN],[P_OUT_RATE],[P_USE],[P_THOUSAND],[P_MULTIPLE],[P_ME_MU],[P_HQ_PRICE],[WSL_LOW_QTY],[P_HQ_PRICE2],[P_HQ_PRICE3],[SALE_TAX_TYPE],[P_SL_BOM],[LBL_NAME],[K3_SALE_TYPE],[SEND_TIME],[IS_POR],[IS_WEIGH],[IS_RETUEN],[IS_TRAN],[P_DC],[STORAGE_LIFE],[P_NO_OD],[REM_CODE],[IS_SAP],[DP_NO],[WM_SETTING_TYPE],[WM_MIN_ORDER_NUM],[WM_PACKAGE_BOX_NUM],[WM_PACKAGE_BOX_PRICE],[WM_USED],[WM_DESC],[WM_ATTR],[K3_UNITNO_JB],[P_NEW_DATE])\n" +
              "select [P_NO],[P_EAN],[P_NAME],[P_NAME_S],[P_TAX],[P_PU],[D_NO],[P_PHOTO],[P_PRICE],[P_PRICE2],[P_PRICE3],[P_PRICE4],[P_PRICE5],[P_PRICE6],[P_KIND],[UN_NO],[P_IN_UN],[P_RATE],[V_NO],[P_DEF1],[P_DEF2],[P_DEF4],[P_DEF3],[P_COST],[P_OUT_DATE],[P_IN_DATE],[P_LASTP],[P_IN_QTY],[P_TOT_Q],[P_STATUS],[P_STOP_DATE],[P_ABC],[P_PERCT],[P_PS_QTY],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[REMARK],[P_DEFA],[P_DEFB],[P_DEFC],[P_DEFD],[P_PRICE_ORI],[P_BUY_POINT],[P_POINT],[P_CHG_POINT],[P_MADE],[P_TAX_RATE],[F_TAX],[F_TAX_RATE],[DD_NO],[P_S_LEVEL],[IS_D_P_NO],[P_TR_DATE],[P_PY],[P_DAILE],[P_SPMODE],[P_DRAW_UN],[P_DRAW_RATE],[P_OUT_UN],[P_OUT_RATE],[P_USE],[P_THOUSAND],[P_MULTIPLE],[P_ME_MU],[P_HQ_PRICE],[WSL_LOW_QTY],[P_HQ_PRICE2],[P_HQ_PRICE3],[SALE_TAX_TYPE],[P_SL_BOM],[LBL_NAME],[K3_SALE_TYPE],[SEND_TIME],[IS_POR],[IS_WEIGH],[IS_RETUEN],[IS_TRAN],[P_DC],[STORAGE_LIFE],[P_NO_OD],[REM_CODE],[IS_SAP],[DP_NO],[WM_SETTING_TYPE],[WM_MIN_ORDER_NUM],[WM_PACKAGE_BOX_NUM],[WM_PACKAGE_BOX_PRICE],[WM_USED],[WM_DESC],[WM_ATTR],[K3_UNITNO_JB],[P_NEW_DATE]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.Part with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.Store_sd_h\n" +
              "insert into rosa2_Vi_his.dbo.Store_sd_h([SD_NO],[SD_NAME],[FLS_NO],[REMARK],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER])\n" +
              "select [SD_NO],[SD_NAME],[FLS_NO],[REMARK],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.Store_sd_h with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.STORE_SD_D\n" +
              "insert into rosa2_Vi_his.dbo.STORE_SD_D([SD_NO],[S_NO],[RECNO],[REMARK])\n" +
              "select [SD_NO],[S_NO],[RECNO],[REMARK]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.STORE_SD_D with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.Seasoning_h\n" +
              "insert into rosa2_Vi_his.dbo.Seasoning_h([SEA_NO],[SEA_NAME],[SEA_ENAME],[SEA_TYPE],[IS_SINGLE],[USED],[REMARK],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER])\n" +
              "select [SEA_NO],[SEA_NAME],[SEA_ENAME],[SEA_TYPE],[IS_SINGLE],[USED],[REMARK],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.Seasoning_h with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.Seasoning_d\n" +
              "insert into rosa2_Vi_his.dbo.Seasoning_d([SEA_ITEM_NO],[SEA_NO],[SEA_ITEM_NAME],[SEA_ITEM_ENAME],[DEF_CHOOSE],[PRICE],[PRICE2],[PRICE3],[USED],[P_NO],[MOD_QTY],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[SEA_ITEM_NO_OD],[SEA_REM_CODE])\n" +
              "select [SEA_ITEM_NO],[SEA_NO],[SEA_ITEM_NAME],[SEA_ITEM_ENAME],[DEF_CHOOSE],[PRICE],[PRICE2],[PRICE3],[USED],[P_NO],[MOD_QTY],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[SEA_ITEM_NO_OD],[SEA_REM_CODE]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.Seasoning_d with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.BOM_H_S\n" +
              "insert into rosa2_Vi_his.dbo.BOM_H_S([BP_NO],[FLS_NO],[REMARK],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[S_NO],[DATE_S],[DATE_D],[IS_SUIT])\n" +
              "select [BP_NO],[FLS_NO],[REMARK],[CRE_DATE],[CRE_USER],[UPD_DATE],[UPD_USER],[S_NO],[DATE_S],[DATE_D],[IS_SUIT]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.BOM_H_S with(nolock)\n" +
              "\n" +
              "truncate table rosa2_Vi_his.dbo.BOM_D_S\n" +
              "insert into rosa2_Vi_his.dbo.BOM_D_S([BP_NO],[RECNO],[P_NO],[B_QTY],[B_PS_QTY],[B_LOSS_RATE],[DATE_S],[DATE_D],[S_DATE_S],[B_UN_NO],[B_RATE],[B_USE_QTY])\n" +
              "select [BP_NO],[RECNO],[P_NO],[B_QTY],[B_PS_QTY],[B_LOSS_RATE],[DATE_S],[DATE_D],[S_DATE_S],[B_UN_NO],[B_RATE],[B_USE_QTY]\n" +
              "from dl_rosa2_Vi.rosa2_Vi.dbo.BOM_D_S with(nolock)\n" +
              "end\n"
      });
      /*
      sqllist.add(new String[]{
          "",
          ""
      });
      sqllist.add(new String[]{
          "",
          ""
      });*/
      emisDb db = null;
      StringBuffer context = new StringBuffer();
      int moveCount =0;
      log.info("************ Start ***************");
      try {
        db = emisDb.getInstance(application);
        db.setAutoCommit(false);
        for(String[] sql : sqllist){
          try{
            moveCount = db.executeUpdate(sql[1]);
            db.commit();
            context.append(sql[0]).append("&nbsp;&nbsp;").append(moveCount).append("<br/>");
          } catch(Exception e){
            db.rollback();
            log.error(sql[0],e);
            context.append(sql[0]).append(">>出错:").append(e.getMessage()).append("<br/>");
          }
        }
        try{
          db.execute("DBCC SHRINKDATABASE(rosa2_Vi_his)");
          db.commit();
        } catch(Exception e){
          log.error(e);
        }
        out.clear();
      } catch (Exception e) {
        log.error(e);
        context.append("*** 单据资料备份到历史数据库出错 *** <br/>");
        context.append(e.getMessage());
      } finally {
        if (db != null) {
          db.close();
          db = null;
        }
      }
      log.info(context);
      log.info("************ End ***************");
      if(context.length() > 0 ) {
        emisProp prop = emisProp.getInstance(application);
        context.append("<br/>").append("====================================")
            .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
            .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
            .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName())
            .append("<br/>").append("执行程序：").append("jsp/dev/speRequest/SpecialRequest.jsp.backupData2History()");
        emisMailer m = new emisMailer();
        //可通过参数mailto指定邮件接收者
        if (request.getParameter("mailto") != null && !"".equals(request.getParameter("mailto"))) {
          m.setTo(request.getParameter("mailto"));
        } else {
          m.setTo("andy.he@emiszh.com;84888322@qq.com");
        }
        m.setSubject("【" + prop.get("EPOS_COMPANY_NO") + "】数据库资料备份历史库记录-"+(new emisDate().toString()));
        m.setContent(context.toString());
        m.send(application);
      }
      context.setLength(0);
      context = null;
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * 长沙罗莎，删除历史资料
   * @param application
   * @param request
   * @param out
   */
  public void deleteHistoryData(ServletContext application, HttpServletRequest request,JspWriter out){
    Logger log = null;
    try {
      log  = emisLogger.getlog4j(application,"jsp.dev.speRequest.SpecialRequest.deleteHistoryData");
      List<String[]> sqllist = new ArrayList<String[]>();
      sqllist.add(new String[]{
          "DCLOSE_REPORT",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-180-1,112) \n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.DCLOSE_REPORT with(nolock) where DC_DATE=@DATE)\n" +
          "  delete d from [DCLOSE_REPORT] d with (nolock)\n" +
          "  where DC_DATE=@DATE"
      });
      sqllist.add(new String[]{
          "COUNT_D",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112) \n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.COUNT_D d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[COUNT_H] h with (nolock) on h.CO_NO=d.CO_NO and h.S_NO=d.S_NO \n" +
          " where h.CO_DATE=@DATE\n" +
          ")\n" +
          "  delete d from [COUNT_D] d with (nolock)\n" +
          "  where exists(select CO_NO from [COUNT_H] with (nolock) where CO_NO=d.CO_NO and S_NO=d.S_NO and CO_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "COUNT_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112) \n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.COUNT_H with(nolock) where CO_DATE=@DATE)\n" +
          "  delete from [COUNT_H] where CO_DATE=@DATE"
      });

      sqllist.add(new String[]{
          "TRAN_D",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112) \n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.TRAN_D d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[TRAN_H] h with (nolock) on h.TR_NO=d.TR_NO and h.S_NO_OUT=d.S_NO_OUT \n" +
          " where h.TR_DATE=@DATE\n" +
          ")\n" +
          "delete d\n" +
          "from [TRAN_D] d with (nolock)\n" +
          "where exists( select TR_NO from [TRAN_H] with (nolock)  where TR_NO=d.TR_NO and S_NO_OUT=d.S_NO_OUT and TR_DATE=@DATE) \n" +
          "  and isnull(d.GT_NO_S,'')=''"
      });
      sqllist.add(new String[]{
          "TRAN_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.TRAN_H with(nolock) where TR_DATE=@DATE)\n" +
          "delete h from [TRAN_H] h where TR_DATE=@DATE \n" +
          "  and not exists(select TR_NO from [TRAN_D] where TR_NO=h.TR_NO and S_NO_OUT=h.S_NO_OUT and isnull(GT_NO_S,'')!='')"
      });

      sqllist.add(new String[]{
          "INS_D",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.INS_D d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[INS_H] h with (nolock) on h.IN_NO=d.IN_NO and h.S_NO=d.S_NO \n" +
          " where h.IN_DATE=@DATE\n" +
          ")\n" +
          "delete d\n" +
          "from [INS_D] d with (nolock)\n" +
          "where exists(select IN_NO from [INS_H] with (nolock) where IN_NO=d.IN_NO and S_NO=d.S_NO and IN_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "INS_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.INS_H with(nolock) where IN_DATE=@DATE)\n" +
          "begin\n" +
          "delete from [INS_H] where IN_DATE=@DATE\n" +
          "insert into [INS_H] ([IN_NO],[S_NO],[IN_DATE],[FLS_NO],[REMARK],[CRE_USER])\n" +
          "select max(IN_NO) as BILL_NO,S_NO,@DATE as BILL_DATE,'ED',N'【这是一张单号占位的单，请不要操作】',N'系统创建'\n" +
          "from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[INS_H] h with(nolock)\n" +
          "where h.IN_DATE = @DATE \n" +
          "  and not exists(select top 1 S_NO from [INS_H] where S_NO=h.S_NO and IN_DATE > @DATE)\n" +
          "group by h.S_NO " +
          "end"
      });
      sqllist.add(new String[]{
          "CWSEND_D",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.CWSEND_D d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[CWSEND_H] h with (nolock) on h.WD_NO=d.WD_NO and h.S_NO_OUT=d.S_NO_OUT \n" +
          " where h.WD_DATE=@DATE\n" +
          ")\n" +
          "delete d\n" +
          "from [CWSEND_D] d with (nolock)\n" +
          "where exists(select WD_NO from [CWSEND_H] with (nolock) where WD_NO=d.WD_NO and S_NO_OUT=d.S_NO_OUT and WD_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "CWSEND_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.CWSEND_H with(nolock) where WD_DATE=@DATE)\n" +
          "delete from [CWSEND_H] where WD_DATE=@DATE"
      });
      sqllist.add(new String[]{
          "USELESS_D",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.USELESS_D d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[USELESS_H] h with (nolock) on h.US_NO=d.US_NO and h.S_NO=d.S_NO \n" +
          " where h.US_DATE=@DATE\n" +
          ")\n" +
          "delete d\n" +
          "from [USELESS_D] d with (nolock)\n" +
          "where exists(select US_NO from [USELESS_H] with (nolock) where US_NO=d.US_NO and S_NO=d.S_NO and US_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "USELESS_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.USELESS_H with(nolock) where US_DATE=@DATE)\n" +
          "delete from [USELESS_H] where US_DATE=@DATE"
      });
      // TODO 注： 生产入库单这里删除是一年之前的数据，但前面备份是三个月之前的（因有发生备份出现主键重复的问题，所以保留最近一年的数据，这样单号取号才不会重复）
      sqllist.add(new String[]{
          "S_STOR_BOM",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-365-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.S_STOR_BOM with(nolock) where SPK_DATE=@DATE)\n" +
          "delete from [S_STOR_BOM] where SPK_DATE=@DATE"
      });
      sqllist.add(new String[]{
          "S_STOR_D",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-365-1,112)\n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.S_STOR_D d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[S_STOR_H] h with (nolock) on h.SST_NO=d.SST_NO and h.S_NO=d.S_NO \n" +
          " where h.SPK_DATE=@DATE\n" +
          ")\n" +
          "delete d\n" +
          "from [S_STOR_D] d with (nolock)\n" +
          "where exists(select SST_NO from S_STOR_H with (nolock) where S_NO=d.S_NO and SST_NO=d.SST_NO and SPK_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "S_STOR_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-365-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.S_STOR_H with(nolock) where SPK_DATE=@DATE)\n" +
          "delete d from [S_STOR_H] d where SPK_DATE=@DATE"
      });
      sqllist.add(new String[]{
          "BACK_D",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.BACK_D d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[BACK_H] h with (nolock) on h.BA_NO=d.BA_NO and h.S_NO=d.S_NO \n" +
          " where h.BA_DATE=@DATE\n" +
          ")\n" +
          "delete d\n" +
          "from [BACK_D] d with (nolock)\n" +
          "where exists(select BA_NO from BACK_H with (nolock) where BA_NO=d.BA_NO and S_NO=d.S_NO and BA_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "BACK_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.BACK_H with(nolock) where BA_DATE=@DATE)\n" +
          "delete d from [BACK_H] d where BA_DATE=@DATE"
      });
      sqllist.add(new String[]{
          "SCRAP_D",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.SCRAP_D d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[SCRAP_H] h with (nolock) on h.SC_NO=d.SC_NO and h.S_NO=d.S_NO \n" +
          " where h.SC_DATE=@DATE\n" +
          ")\n" +
          "delete d \n" +
          "from [SCRAP_D] d with (nolock)\n" +
          "where exists(select SC_NO from [SCRAP_H] with (nolock) where SC_NO=d.SC_NO and S_NO=d.S_NO and SC_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "SCRAP_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-90-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.SCRAP_H with(nolock) where SC_DATE=@DATE)\n" +
          "delete d from [SCRAP_H] d where SC_DATE=@DATE"
      });
      sqllist.add(new String[]{
          "RAT_PREALLOT_P",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-30-1,112)\n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.RAT_PREALLOT_P d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[RAT_PREALLOT_H] h with (nolock) on h.PA_NO=d.PA_NO and h.S_NO_OUT=d.S_NO_OUT \n" +
          " where h.POR_DATE=@DATE\n" +
          ")\n" +
          "delete d from [RAT_PREALLOT_P] d with (nolock)\n" +
          "where exists(select PA_NO from [RAT_PREALLOT_H] with (nolock) where PA_NO=d.PA_NO and S_NO_OUT=d.S_NO_OUT and POR_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "RAT_PREALLOT_S",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-30-1,112)\n" +
          "if exists(\n" +
          " select top 1 d.* from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.RAT_PREALLOT_S d with(nolock) \n" +
          " inner join dl_rosa2_Vi_his.rosa2_Vi_his.dbo.[RAT_PREALLOT_H] h with (nolock) on h.PA_NO=d.PA_NO and h.S_NO_OUT=d.S_NO_OUT \n" +
          " where h.POR_DATE=@DATE\n" +
          ")\n" +
          "delete d from [RAT_PREALLOT_S] d with (nolock)\n" +
          "where exists(select PA_NO from [RAT_PREALLOT_H] with (nolock) where PA_NO=d.PA_NO and S_NO_OUT=d.S_NO_OUT and POR_DATE=@DATE)"
      });
      sqllist.add(new String[]{
          "RAT_PREALLOT_H",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-30-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.RAT_PREALLOT_H with(nolock) where POR_DATE=@DATE)\n" +
          "delete d from [RAT_PREALLOT_H] d where POR_DATE=@DATE"
      });
      sqllist.add(new String[]{
          "DCLOSE_P_PRICE",
          "declare @DATE nvarchar(10) = convert(nvarchar(8),getdate()-180-1,112)\n" +
          "if exists(select top 1 * from dl_rosa2_Vi_his.rosa2_Vi_his.dbo.DCLOSE_P_PRICE with(nolock) where [DC_DATE]=@DATE)\n" +
          "delete d from [DCLOSE_P_PRICE] d where DC_DATE=@DATE"
      });
      /*
      sqllist.add(new String[]{
          "",
          ""
      });
      sqllist.add(new String[]{
          "",
          ""
      });*/
      emisDb db = null;
      StringBuffer context = new StringBuffer();
      int deleteCount =0;
      log.info("************ Start ***************");
      try {
        db = emisDb.getInstance(application);
        db.setAutoCommit(false);
        for(String[] sql : sqllist){
          try{
            deleteCount = db.executeUpdate(sql[1]);
            db.commit();
            context.append(sql[0]).append("&nbsp;&nbsp;").append(deleteCount).append("<br/>");
          } catch(Exception e){
            db.rollback();
            log.error(sql[0],e);
            context.append(sql[0]).append(">>出错:").append(e.getMessage()).append("<br/>");
          }
        }
        /*
        try{
          db.execute("DBCC SHRINKDATABASE(rosa2_Vi)");
          db.commit();
        } catch(Exception e){
          log.error(e);
        }
        */
        out.clear();
      } catch (Exception e) {
        log.error(e);
        context.append("*** 删除历史数据库出错 *** <br/>");
        context.append(e.getMessage());
      } finally {
        if (db != null) {
          db.close();
          db = null;
        }
      }
      log.info(context);
      log.info("************ End ***************");
      if(context.length() > 0 ) {
        emisProp prop = emisProp.getInstance(application);
        context.append("<br/>").append("====================================")
            .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
            .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
            .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName())
            .append("<br/>").append("执行程序：").append("jsp/dev/speRequest/SpecialRequest.jsp.deleteHistoryData()");
        emisMailer m = new emisMailer();
        //可通过参数mailto指定邮件接收者
        if (request.getParameter("mailto") != null && !"".equals(request.getParameter("mailto"))) {
          m.setTo(request.getParameter("mailto"));
        } else {
          m.setTo("andy.he@emiszh.com;84888322@qq.com");
        }
        m.setSubject("【" + prop.get("EPOS_COMPANY_NO") + "】删除历史数据记录-"+(new emisDate().toString()));
        m.setContent(context.toString());
        m.send(application);
      }
      context.setLength(0);
      context = null;
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  private void checkSale2ERP(ServletContext application, HttpServletRequest request,JspWriter out) throws Exception {
    emisDb db = null;
    StringBuffer context = new StringBuffer(), sql = new StringBuffer();
    emisProp prop = emisProp.getInstance(application);
    String subject = null;
    int i = 1;
    try {
      db = emisDb.getInstance(application);
      /*
        select top 20 N'销售单' as TYPE, h.SL_KEY from SALE_H h with(nolock) inner join STORE s with(nolock) on s.S_NO = h.S_NO
        where (
          (h.SL_DATE between convert(nvarchar(8),dbo.GetLocalDate()-2,112) and convert(nvarchar(8),dbo.GetLocalDate()-1,112)) or
          (h.SL_DATE=convert(nvarchar(8),dbo.GetLocalDate(),112) and h.SL_TIME<replace(convert(varchar(100), DateAdd (Hh,-2,dbo.GetLocalDate()), 8),':',''))
         )
         and h.FLS_NO >='3' and isnull(h.POS2ERP_FLAG,'')='' and s.S_KIND in('1','3','4')
        union all
        select top 20 N'客订单' as TYPE, h.SL_KEY from SALE_ORDER_H h with(nolock) inner join STORE s  with(nolock) on s.S_NO = h.S_NO
        where (
          (h.SL_DATE between convert(nvarchar(8),dbo.GetLocalDate()-2,112) and convert(nvarchar(8),dbo.GetLocalDate()-1,112)) or
          (h.SL_DATE=convert(nvarchar(8),dbo.GetLocalDate(),112) and h.SL_TIME<replace(convert(varchar(100), DateAdd (Hh,-2,dbo.GetLocalDate()), 8),':',''))
         )
         and ((h.FLS_NO in('PP','AP','CO') and isnull(h.POS2ERP_FLAG,'')='') or h.FLS_NO='CL' and isnull(h.POS2ERP_FLAG,'')='C') and s.S_KIND in('1','3','4')
       */
      /*
      sql.append("select top 10 N'销售单' as TYPE, h.SL_KEY from SALE_H h with(nolock) inner join STORE s with(nolock) on s.S_NO = h.S_NO\n" +
          "where h.SL_DATE between convert(nvarchar(8),dbo.GetLocalDate()-2,112) and convert(nvarchar(8),dbo.GetLocalDate()-1,112) \n" +
          " and h.FLS_NO >='3' and isnull(h.POS2ERP_FLAG,'')=''");
      if(!"".equals(prop.get("POS2ERP_STORE_KIND")))
        sql.append(" and s.S_KIND in(").append(prop.get("POS2ERP_STORE_KIND")).append(")");

      sql.append("\nunion all\n");
      sql.append("select top 10 N'销售单' as TYPE, h.SL_KEY from SALE_H h with(nolock) inner join STORE s with(nolock) on s.S_NO = h.S_NO\n" +
          "where h.SL_DATE=convert(nvarchar(8),dbo.GetLocalDate(),112) and h.SL_TIME<replace(convert(varchar(100), DateAdd (Hh,-2,dbo.GetLocalDate()), 8),':','')\n" +
          " and h.FLS_NO >='3' and isnull(h.POS2ERP_FLAG,'')=''");
      if(!"".equals(prop.get("POS2ERP_STORE_KIND")))
        sql.append(" and s.S_KIND in(").append(prop.get("POS2ERP_STORE_KIND")).append(")");
      if("ICHIDO".equals(prop.get("EPOS_COMPANY_NO"))){
        sql.append("\nunion all\n");
        sql.append("select top 10 N'客订单' as TYPE, h.SL_KEY from SALE_ORDER_H h with(nolock) inner join STORE s  with(nolock) on s.S_NO = h.S_NO\n" +
            "where (\n" +
            "  (h.SL_DATE between convert(nvarchar(8),dbo.GetLocalDate()-2,112) and convert(nvarchar(8),dbo.GetLocalDate()-1,112)) or\n" +
            "  (h.SL_DATE=convert(nvarchar(8),dbo.GetLocalDate(),112) and h.SL_TIME<replace(convert(varchar(100), DateAdd (Hh,-2,dbo.GetLocalDate()), 8),':','')) \n" +
            " )\n" +
            " and ((h.FLS_NO in('PP','AP','CO') and isnull(h.POS2ERP_FLAG,'')='') or h.FLS_NO='CL' and isnull(h.POS2ERP_FLAG,'')='C')");
        if(!"".equals(prop.get("POS2ERP_STORE_KIND")))
          sql.append(" and s.S_KIND in(").append(prop.get("POS2ERP_STORE_KIND")).append(")").append("\n");
      }
      */
      String qrysql = "SELECT top 10 N'SALE' as TYPE, h.SL_KEY FROM (\n" +
          " select h.SL_KEY,FLS_NO,POS2ERP_FLAG from SALE_H h with(nolock) \n" +
          "where h.SL_DATE between convert(nvarchar(8),dbo.GetLocalDate()-2,112) and convert(nvarchar(8),dbo.GetLocalDate()-1,112) \n" +
          ") h where \n" +
          "  h.FLS_NO >='3' and isnull(h.POS2ERP_FLAG,'')=''";
      out.clear();
      out.println("-------------------------------------------------------"+emisUtil.todayTimeS(true));
      out.println(qrysql);
      db.executeQuery(qrysql);
      while (db.next()) {
        context.append(db.getString("TYPE")).append(" >> ").append(db.getString("SL_KEY")).append("@");
      }

      qrysql = "select top 50 N'SALE' as TYPE, h.SL_KEY,h.SL_TIME,replace(convert(varchar(100), DateAdd (Hh,-1,dbo.GetLocalDate()), 8),':','') as T from (  \n" +
          " select h.SL_KEY,h.FLS_NO,h.POS2ERP_FLAG,h.SL_TIME from SALE_H h with(nolock) \n" +
          " where h.SL_DATE=convert(nvarchar(8),dbo.GetLocalDate(),112)\n" +
          ") h where h.FLS_NO >='3' and isnull(h.POS2ERP_FLAG,'')=''";
      //out.clear();

      out.println("-------------------------------------------------------"+emisUtil.todayTimeS(true));
      out.println(qrysql);
      db.executeQuery(qrysql);
      i =0;
      while (db.next()) {
        if(db.getString("SL_TIME").compareTo(db.getString("T"))>=0) continue;
        context.append(db.getString("TYPE")).append(" >> ").append(db.getString("SL_KEY")).append("@");
        if(i++>5) break;
      }

      qrysql = "select top 10 N'SALE_ORDER' as TYPE, h.SL_KEY from ( \n" +
          " select h.SL_KEY,FLS_NO,POS2ERP_FLAG from SALE_ORDER_H h with(nolock)\n" +
          " where h.SL_DATE between convert(nvarchar(8),dbo.GetLocalDate()-3,112) and convert(nvarchar(8),dbo.GetLocalDate()-1,112)\n" +
          ") h where (h.FLS_NO in('PP','AP','CO') and isnull(h.POS2ERP_FLAG,'')='') or (h.FLS_NO='CL' and isnull(h.POS2ERP_FLAG,'')='C')";
      //out.clear();
      out.println("-------------------------------------------------------"+emisUtil.todayTimeS(true));
      out.println(qrysql);
      db.executeQuery(qrysql);
      while (db.next()) {
        context.append(db.getString("TYPE")).append(" >> ").append(db.getString("SL_KEY")).append("@");
      }

      qrysql = "select top 50 N'SALE_ORDER' as TYPE, h.SL_KEY,h.SL_TIME,replace(convert(varchar(100), DateAdd (Hh,-1,dbo.GetLocalDate()), 8),':','') as T from ( \n" +
          " select h.SL_KEY,FLS_NO,POS2ERP_FLAG,h.SL_TIME from SALE_ORDER_H h with(nolock)\n" +
          " where h.SL_DATE=convert(nvarchar(8),dbo.GetLocalDate(),112)\n" +
          ") h where (h.FLS_NO in('PP','AP','CO') and isnull(h.POS2ERP_FLAG,'')='') or (h.FLS_NO='CL' and isnull(h.POS2ERP_FLAG,'')='C')";
      //out.clear();
      out.println("-------------------------------------------------------"+emisUtil.todayTimeS(true));
      out.println(qrysql);
      db.executeQuery(qrysql);
      i=0;
      while (db.next()) {
        if(db.getString("SL_TIME").compareTo(db.getString("T"))>=0) continue;
        context.append(db.getString("TYPE")).append(" >> ").append(db.getString("SL_KEY")).append("@");
        if(i++>5) break;
      }

      qrysql = "select top 10 N'SALE_POS2STAGE' as TYPE, h.SL_KEY from SALE_H_ICHIDO h with(nolock)\n" +
          "where h.SL_DATE between convert(nvarchar(8),dbo.GetLocalDate()-3,112) and convert(nvarchar(8),dbo.GetLocalDate()-1,112) and SYNC_FLAG!='Y'";
      //out.clear();
      out.println("-------------------------------------------------------"+emisUtil.todayTimeS(true));
      out.println(qrysql);
      db.executeQuery(qrysql);
      while (db.next()) {
        context.append(db.getString("TYPE")).append(" >> ").append(db.getString("SL_KEY")).append("@");
      }
      out.println("-------------------------------------------------------"+emisUtil.todayTimeS(true));
      subject = "【" + prop.get("EPOS_COMPANY_NO") + "】最近四天(含当天)未同步ERP数据列表 ，请检查";
      //out.println(sql.toString());
      if(context.length()==0){
        out.println("***** No Data *****");
      } else {
        out.print(context.toString().replaceAll("@", "\n"));
      }
    } catch (Exception e) {
      e.printStackTrace();
      context.append(e.getMessage());
      subject = "【" + prop.get("EPOS_COMPANY_NO") + "】最近四天(含当天)未同步ERP数据检查出错";
      out.clear();
      out.println("*** Error >> "+e.getMessage());
    } finally {
      if (db != null) {
        db.close();
        db = null;
      }
    }
    /*
    if (context.length() > 0) {
      context.append("<br/>").append("====================================")
          .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
          .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
          .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName())
          .append("<br/>").append("执行程序：").append("jsp/dev/speRequest/SpecialRequest.jsp.checkSale2ERP()");
      try {
        emisMailer m = new emisMailer();
        m.setTo("andy.he@emiszh.com;84888322@qq.com");
        m.setSubject(subject);
        m.setContent(context.toString());
        m.send(application);
      } catch(Exception e){
        e.printStackTrace();
        out.println("***** Mail Error *****");
        out.println(e.getMessage());
      }
    }
    */
    context.setLength(0);
    context = null;
    sql.setLength(0);
    sql = null;
  }

  private void getIC_AMT(ServletContext application, HttpServletRequest request,JspWriter out) throws Exception {
    emisDb db = null;
    StringBuffer context = new StringBuffer(), sql = new StringBuffer();
    emisProp prop = emisProp.getInstance(application);
    String subject = null;
    int i = 1;
    try {
       /*
        drop table GIFT_TOKEN_20180411
       */
      db = emisDb.getInstance(application);
      String date = new emisDate().add(-1).toString();
      String bkTable = "GIFT_TOKEN_"+date;
      int row = 0;

      db.executeQuery("select * from sysobjects where name= '" + bkTable + "' and XTYPE='U'");
      if(db.next()){
        out.clear();
        out.println(">> The " + bkTable + " has already existed.");
        return;
      }

      row = db.executeUpdate("select GT_NO,IC_AMT,FLS_NO,IC_PCT into " + bkTable + " from GIFT_TOKEN where isnull(SELL_DATE,'')!='' and IC_AMT<>0");
      context.append(bkTable).append(" >> ").append(row).append("@");

      subject = "【" + prop.get("EPOS_COMPANY_NO") + "】卡券余额备份";
      out.clear();
      out.println(context.toString().replaceAll("@","\n"));
    } catch (Exception e) {
      e.printStackTrace();
      context.append(e.getMessage());
      subject ="【" + prop.get("EPOS_COMPANY_NO") + "】卡券余额备份出错" ;
      out.clear();
      out.println("*** Error >> "+e.getMessage());
    } finally {
      if (db != null) {
        db.close();
        db = null;
      }
    }
    if (context.length() > 0) {
      context.append("<br/>").append("====================================")
          .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
          .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
          .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName())
          .append("<br/>").append("执行程序：").append("jsp/dev/speRequest/SpecialRequest.jsp.getIC_AMT()");
      try {
        emisMailer m = new emisMailer();
        m.setTo("andy.he@emiszh.com;");
        m.setSubject(subject);
        m.setContent(context.toString().replaceAll("@","<br/>"));
        m.send(application);
      } catch(Exception e){
        e.printStackTrace();
        out.println("***** Mail Error *****");
        out.println(e.getMessage());
      }
    }
    context.setLength(0);
    context = null;
    sql.setLength(0);
    sql = null;
  }

  private void checkGiftAmt(ServletContext application, HttpServletRequest request,JspWriter out) throws Exception {
    emisDb db = null;
    StringBuffer context = new StringBuffer(), sql = new StringBuffer();
    emisProp prop = emisProp.getInstance(application);
    String subject = null;
    String date = new emisDate().add(-1).toString();
    String yd = new emisDate().add(-2).toString();
    String tblTitle = "";
    try {
      db = emisDb.getInstance(application);
      double diffAmt = 0;
      DecimalFormat dFormat = new DecimalFormat("##0.00");
      List<String> tfield = new ArrayList<String>();
      double total [] = new double[]{0,0,0,0,0,0,0,0,0,0,0,0};
      try {
        db.prepareStmt("select isnull(t1.gs_no,t2.gs_no)+' '+gs.gs_name as gs_no,isnull(p.p_name,'') as p_name\n" +
            " ,sum(t1.end_amt) end_amt,sum(t1.start_amt) start_amt,sum(t1.end_ic_amt) end_ic_amt,sum(t1.disc_ic_amt) disc_ic_amt,sum(t1.ic_amt) ic_amt\n" +
            " ,sum(isnull(t2.open_amt,0)) open_amt,sum(isnull(t2.add_amt,0)) add_amt, sum(isnull(t2.pay_3,0)) as pay_3" +
            " ,sum(isnull(t2.pay_4,0)) as pay_4, sum(isnull(t2.chg_amt,0)) as chg_amt \n" +
            " ,sum(isnull(t2.open_amt,0)+isnull(t2.add_amt,0)-isnull(t2.pay_3,0)-isnull(t2.pay_4,0)+isnull(t2.chg_amt,0)) as change_amt \n" +
            "from (\n" +
            "  select gt.gs_no,gt.p_no,\n" +
            "    convert(decimal(12,2),sum(isnull(gt2.ic_amt,0)*gt.ic_pct/100)) as end_ic_amt,convert(decimal(12,2),sum(isnull(gt2.ic_amt,0)*(100-gt.ic_pct)/100)) as disc_ic_amt,\n" +
            "    sum(isnull(gt2.IC_AMT,0)) as end_amt, sum(isnull(gt1.IC_AMT,0)) as start_amt,sum(isnull(gt2.IC_AMT,0)-isnull(gt1.IC_AMT,0)) as ic_amt\n" +
            "  from GIFT_TOKEN_" + yd + " gt1 \n" +
            "  full join GIFT_TOKEN_" + date + " gt2 on gt2.GT_NO=gt1.GT_NO \n" +
            "  inner join gift_token gt with(nolock) on gt.gt_no = isnull(gt1.gt_no,gt2.gt_no)\n" +
            "  group by gt.gs_no,gt.p_no " +
            ") t1 full join (\n" +
            "  select gs_no,p_no,sum(open_amt) as open_amt,sum(add_amt) as add_amt,sum(pay_3) as pay_3,sum(pay_4) as pay_4,sum(chg_amt) as chg_amt\n" +
            "  from(\n" +
            "    /******卡券销售*****/\n" +
            "    select gt.gs_no,gt.p_no,(sd.OPEN_VALUE*case sh.ST_TYPE when '1' then -1 else 1 end) open_amt,0 as add_amt,0 as pay_3,0 as pay_4,0 as chg_amt\n" +
            "    from sellgt_d sd with(nolock) inner join sellgt_h sh with(nolock) on sh.st_no=sd.st_no and sh.s_no=sd.s_no\n" +
            "    inner join gift_token gt with(nolock) on gt.gt_no=sd.gt_no \n" +
            "    where sh.UPD_DATE=? and sh.FLS_NO='CO' \n" +
            "    union all\n" +
            "    /******卡充值*****/\n" +
            "    select gt.gs_no,gt.p_no,0 as open_amt,(h.ADD_AMT) as add_amt,0 as pay_3,0 as pay_4,0 as chg_amt\n" +
            "    from ic_gift_recharge h with(nolock) \n" +
            "    inner join gift_token gt with(nolock) on gt.gt_no=h.ic_no \n" +
            "    where h.CRE_DATE=? and h.RE_TYPE not in('2','3') \n" +
            "    union all\n" +
            "    /******卡消费*****/\n" +
            "    select p3.gs_no,p3.p_no,0 as open_amt,0 as add_amt,sum(PAY_AMT) as pay_3,0 as pay_4,0 as chg_amt\n" +
            "    from(\n" +
            "      select gt.gs_no,gt.p_no,sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_card h with(nolock) inner join sale_h sh with(nolock) on sh.sl_key=h.sl_key and isnull(sh.SL_KEY_ORDER,'')=''\n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where h.SL_DATE=? and h.CARD_TYPE in('1') group by gt.gs_no,gt.p_no\n" +
            "      union all\n" +
            "      select gt.gs_no,gt.p_no,sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_order_card h with(nolock) \n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where h.CRE_DATE=? and h.CARD_TYPE in('1') group by gt.gs_no,gt.p_no \n" +
            "      /*******客订取消卡通过充值方式返还*******/\n" +
            "      union all\n" +
            "      select gt.gs_no,gt.p_no,-sum(h.ADD_AMT) as PAY_AMT\n" +
            "      from ic_gift_recharge h with(nolock) \n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.ic_no \n" +
            "      where h.CRE_DATE=? and h.RE_TYPE in('3') group by gt.gs_no,gt.p_no \n" +
            "    ) p3  group by p3.gs_no,p3.p_no  \n" +
            "    union all\n" +
            "    /******券销售*****/\n" +
            "    select p4.gs_no,p4.p_no ,0 as open_amt,0 as add_amt,0 as pay_3,sum(PAY_AMT) as pay_4,0 as chg_amt\n" +
            "    from(\n" +
            "      select gt.gs_no,gt.p_no,sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_card h with(nolock) inner join sale_h sh with(nolock) on sh.sl_key=h.sl_key and isnull(sh.SL_KEY_ORDER,'')=''\n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where h.SL_DATE=? and h.CARD_TYPE in('3','4') group by gt.gs_no,gt.p_no \n" +
            "      union all\n" +
            "      select gt.gs_no,gt.p_no,sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_order_card h with(nolock) \n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where h.CRE_DATE=? and h.CARD_TYPE in('3','4') group by gt.gs_no,gt.p_no \n" +
            "      /**********客订取消用券付订金的***********/\n" +
            "      union all\n" +
            "      select gt.gs_no,gt.p_no,-sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_order_card h with(nolock) inner join sale_order_h sh with(nolock) on sh.sl_key=h.sl_key\n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where sh.UPD_DATE=? and sh.FLS_NO='CL' and h.CARD_TYPE in('3','4') group by gt.gs_no,gt.p_no\n" +
            "    ) p4 group by p4.gs_no,p4.p_no \n" +
            "    union all\n" +
            "    /******旧系统卡换卡*****/\n" +
            "    select gt.gs_no,gt.p_no,0 as open_amt,0 as add_amt,0 as pay_3,0 as pay_4,sum(BALANCE) as chg_amt\n" +
            "    from CHANGE_CARD cc with(nolock) \n" +
            "    inner join gift_token gt with(nolock) on gt.gt_no=cc.new_card\n" +
            "    where cc.CHG_DATE=?  group by gt.gs_no,gt.p_no \n" +
            "  ) tmp\n" +
            "  group by gs_no,p_no \n" +
            ") t2 on t2.gs_no=t1.gs_no and isnull(t2.p_no,'')=isnull(t1.p_no,'')\n" +
            "inner join gift_set gs with(nolock) on gs.gs_no=isnull(t1.gs_no,t2.gs_no) \n" +
            "left join part p with(nolock) on p.p_no=isnull(t1.p_no,t2.p_no) \n" +
            "group by isnull(t1.gs_no,t2.gs_no),gs.gs_name,isnull(p.p_name,'')" +
            "order by 1,2,3");
        for (int idx = 1; idx <= 9; idx++) {
          db.setString(idx, date);
        }
        db.prepareQuery();


        tfield.add("结算日期");
        tfield.add("卡券种类");
        tfield.add("卡券名称");
        tfield.add("当日结余<br>(A)");
        tfield.add("前日结余<br>(B)");
        tfield.add("当日净收结余");
        tfield.add("当日折扣结余");
        tfield.add("当日余额异动<br>(C=A-B)");
        tfield.add("卡券销售<br>(D)");
        tfield.add("卡充值<br>(E)");
        tfield.add("卡消费<br>(F)");
        tfield.add("券消费(含溢收)<br>(G)");
        tfield.add("旧系统卡换卡<br>(H)");
        tfield.add("当日交易金额合计<br>(I=D+E-F-G+H)");
        tfield.add("差额<br>(J=C-I)");

        context.append("<table width='100' border='1'>");
        context.append("<tr>");
        for (String f : tfield) {
          context.append("<td align='center' nowrap='0'>").append(f).append("</td>");
        }
        context.append("</tr>");

        while (db.next()) {
          context.append("<tr>");
          context.append("<td align='center' nowrap='0'>").append(date).append("</td>");
          for (int col = 1; col <= db.getColumnCount(); col++) {
            context.append("<td align='").append(col<3?"left":"right").append("' nowrap='0'>")
                .append(col<3?db.getString(col):dFormat.format(db.getDouble(col)))
                .append("</td>");
            if(col>=3) total[col-3]+=db.getDouble(col);
          }
          diffAmt = db.getDouble("change_amt") - db.getDouble("ic_amt");
          total[11]+= (db.getDouble("change_amt") - db.getDouble("ic_amt"));
          context.append("<td align='right' nowrap='0'>").append(dFormat.format(diffAmt)).append("</td>");
          context.append("</tr>");
        }

        context.append("<tr>");
        context.append("<td align='center' colspan='3'>").append("合　计：").append("</td>");
        for(int i =0;i<total.length;i++){
          context.append("<td align='right' nowrap='0'>").append(dFormat.format(total[i])).append("</td>");
        }
        context.append("</tr>");
        context.append("</table>");
      } catch(Exception e){

      }

      if(Double.parseDouble(dFormat.format(total[11])) != 0){
        db.prepareStmt("select isnull(t1.gt_no,t2.gt_no) as gt_no,t1.end_amt,t1.start_amt,t1.ic_amt,\n" +
            "  isnull(t2.open_amt,0) open_amt,isnull(t2.add_amt,0) add_amt, isnull(t2.pay_3,0) as pay_3, isnull(t2.pay_4,0) as pay_4, isnull(t2.chg_amt,0) as chg_amt,\n" +
            "  isnull(t2.open_amt,0)+isnull(t2.add_amt,0)-isnull(t2.pay_3,0)-isnull(t2.pay_4,0)+isnull(t2.chg_amt,0) as change_amt \n" +
            "from (\n" +
            "  select isnull(gt1.gt_no,gt2.gt_no) as gt_no,isnull(gt1.ic_amt,0) as start_amt, isnull(gt2.ic_amt,0) as end_amt,(isnull(gt2.IC_AMT,0)-isnull(gt1.IC_AMT,0)) as ic_amt\n" +
            "  from GIFT_TOKEN_"+yd+" gt1 \n" +
            "  full join GIFT_TOKEN_"+date+" gt2 on gt2.GT_NO=gt1.GT_NO \n" +
            //"  where (isnull(gt2.IC_AMT,0)-isnull(gt1.IC_AMT,0))!=0\n" +
            ") t1 full join (\n" +
            "  select gt_no,sum(open_amt) as open_amt,sum(add_amt) as add_amt,sum(pay_3) as pay_3,sum(pay_4) as pay_4,sum(chg_amt) as chg_amt\n" +
            "  from(\n" +
            "    -- ST_TYPE=1,是卡券退货\n" +
            "    select sd.gt_no,(sd.OPEN_VALUE*case sh.ST_TYPE when '1' then -1 else 1 end) open_amt,0 as add_amt,0 as pay_3,0 as pay_4,0 as chg_amt\n" +
            "    from sellgt_d sd with(nolock) inner join sellgt_h sh with(nolock) on sh.st_no=sd.st_no and sh.s_no=sd.s_no\n" +
            "    where sh.UPD_DATE=? and sh.FLS_NO='CO'  \n" +
            "    union all\n" +
            "    select h.ic_no as gt_no,0 as open_amt,(h.ADD_AMT) as add_amt,0 as pay_3,0 as pay_4,0 as chg_amt\n" +
            "    from ic_gift_recharge h with(nolock) \n" +
            "    where h.CRE_DATE=? and h.RE_TYPE not in('2','3')\n" +
            "    union all\n" +
            "    select gt_no,0 as open_amt,0 as add_amt,sum(PAY_AMT) as pay_3,0 as pay_4,0 as chg_amt\n" +
            "    from(\n" +
            "      select gt.gt_no,sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_card h with(nolock) inner join sale_h sh with(nolock) on sh.sl_key=h.sl_key and isnull(sh.SL_KEY_ORDER,'')=''\n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where h.SL_DATE=? and h.CARD_TYPE in('1')\n" +
            "      group by gt.gt_no\n" +
            "      union all\n" +
            "      select gt.gt_no,sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_order_card h with(nolock) \n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where h.CRE_DATE=? and h.CARD_TYPE in('1')\n" +
            "      group by gt.gt_no\n" +
            "      /*******客订取消卡通过充值方式返还*******/\n" +
            "      union all\n" +
            "      select h.ic_no,-sum(h.ADD_AMT) as PAY_AMT\n" +
            "      from ic_gift_recharge h with(nolock) \n" +
            "      where h.CRE_DATE=? and h.RE_TYPE in('3')\n" +
            "      group by h.ic_no\n" +
            "    ) p3\n" +
            "    group by gt_no\n" +
            "    union all\n" +
            "    select gt_no,0 as open_amt,0 as add_amt,0 as pay_3,sum(PAY_AMT) as pay_4,0 as chg_amt\n" +
            "    from(\n" +
            "      select gt.gt_no,sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_card h with(nolock) inner join sale_h sh with(nolock) on sh.sl_key=h.sl_key and isnull(sh.SL_KEY_ORDER,'')=''\n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where h.SL_DATE=? and h.CARD_TYPE in('3','4')\n" +
            "      group by gt.gt_no\n" +
            "      union all\n" +
            "      select gt.gt_no,sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_order_card h with(nolock) \n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where h.CRE_DATE=? and h.CARD_TYPE in('3','4')\n" +
            "      group by gt.gt_no  \n" +
            "      /**********客订取消用券付订金的***********/\n" +
            "      union all\n" +
            "      select gt.gt_no,-sum(case when isnull(h.QTY,0)=0 then 1 else h.QTY end * h.AMT) as PAY_AMT\n" +
            "      from sale_order_card h with(nolock) inner join sale_order_h sh with(nolock) on sh.sl_key=h.sl_key\n" +
            "      inner join gift_token gt with(nolock) on gt.gt_no=h.card_no\n" +
            "      where sh.UPD_DATE=? and sh.FLS_NO='CL' and h.CARD_TYPE in('3','4')\n" +
            "      group by gt.gt_no  \n" +
            "    ) p4\n" +
            "    group by gt_no\n" +
            "    union all\n" +
            "    select cc.NEW_CARD as gt_no,0 as open_amt,0 as add_amt,0 as pay_3,0 as pay_4,sum(BALANCE) as chg_amt\n" +
            "    from CHANGE_CARD cc with(nolock) \n" +
            "    where cc.CHG_DATE=? \n" +
            "    group by cc.NEW_CARD\n" +
            "  ) tmp\n" +
            "  group by gt_no\n" +
            ") t2 on t1.gt_no=t2.gt_no\n" +
            "where isnull(t1.ic_amt,0)!=isnull(t2.open_amt,0)+isnull(t2.add_amt,0)-isnull(t2.pay_3,0)-isnull(t2.pay_4,0)+isnull(t2.chg_amt,0)");
        tfield.clear();
        tfield.add("卡券编号");
        tfield.add("当日结余<br>(A)");
        tfield.add("前日结余<br>(B)");
        tfield.add("当日余额异动<br>(C=A-B)");
        tfield.add("卡券销售<br>(D)");
        tfield.add("卡充值<br>(E)");
        tfield.add("卡消费<br>(F)");
        tfield.add("券消费(含溢收)<br>(G)");
        tfield.add("旧系统卡换卡<br>(H)");
        tfield.add("当日交易金额合计<br>(I=D+E-F-G+H)");

        context.append("<br><b>卡券余额差异明细表<b><br>");

        context.append("<table width='100' border='1'>");
        context.append("<tr>");
        for(String f:tfield){
          context.append("<td align='center' nowrap='0'>").append(f).append("</td>");
        }
        context.append("</tr>");
        for(int idx= 1;idx<=9;idx++){
          db.setString(idx,date);
        }
        db.prepareQuery();
        while(db.next()){
          context.append("<tr>");
          for(int col = 1; col<=db.getColumnCount();col++){
            context.append("<td align='right' nowrap='0'>").append(db.getString(col)).append("</td>");
          }
          context.append("</tr>");
        }
        context.append("</table>");
      }

      subject = "【" + prop.get("EPOS_COMPANY_NO") + "】"+date+" 卡券余额结存统计表";
      tblTitle = "<center><b>卡券余额对比总表</b> (总差额:"+dFormat.format(total[11])+")</center>";
      out.clear();
      out.println("卡券余额对比差异 >>" + dFormat.format(total[11]));
      //out.println(context.toString().replaceAll("@","\n"));
    } catch (Exception e) {
      e.printStackTrace();
      context.append(e.getMessage());
      subject ="【" + prop.get("EPOS_COMPANY_NO") + "】"+date+" 卡券余额结存统计出错" ;
      out.clear();
      out.println("*** Error >> "+e.getMessage());
    } finally {
      if (db != null) {
        db.close();
        db = null;
      }
    }
    if (context.length() > 0) {
      context.append("<br/>").append("====================================")
          .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
          .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
          .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName());
      //.append("<br/>").append("执行程序：").append("jsp/dev/speRequest/SpecialRequest.jsp.getIC_AMT()");
      try {
        emisMailer m = new emisMailer();
        String mailTo = request.getParameter("mailTo");
        if(mailTo == null || "".equals(mailTo)) {
          mailTo = "andy.he@emiszh.com;";
        }

        out.println(" >>>> "+mailTo);
        m.setTo(mailTo);
        m.setSubject(subject);
        m.setContent(tblTitle+context.toString().replaceAll("@","<br/>"));
        m.send(application);
      } catch(Exception e){
        e.printStackTrace();
        out.println("***** Mail Error *****");
        out.println(e.getMessage());
      }
    }
    context.setLength(0);
    context = null;
    sql.setLength(0);
    sql = null;
  }

  /**
   * 宜芝多客制：名人广场店（160130）为商场门店，需要通过中间数据库的方式将销售资料给商场对接。
   * @param application
   * @param request
   * @param out
   */
  private void pos2stage(ServletContext application, HttpServletRequest request,JspWriter out) throws IOException {
    emisDb sourceDb = null, targetDb = null;
    emisRowSet sourceRs = null;
    try {
      out.clear();
      out.println("[" + emisUtil.todayTimeS(true) + "]" + " Start ");
      sourceDb = emisDb.getInstance(application);
      sourceDb.setAutoCommit(false);
      int x = 0;
      // 不知道什么原因取ichido_stage的db会为空，暂用循环的方式重新取一下。
      while(true) {
        try {
          targetDb = emisDb.getInstance(application, "ichido_stage");
          targetDb.setAutoCommit(false);
        } catch (java.sql.SQLException sqle) {
          out.print(">targetDb Error >> " + x + " > ");
          if(x++<5) continue;
          else throw sqle;
        }
        break;
      }
      StringBuffer qrysql = new StringBuffer();
      qrysql.append("select * from SALE_H_ICHIDO ");
      if(request.getParameter("SL_DATE") != null && "".equals(request.getParameter("SL_DATE"))){
        qrysql.append("where SL_DATE='").append(request.getParameter("SL_DATE")).append("'");
      } else {
        qrysql.append("where SL_DATE between convert(nvarchar(8), dbo.GetLocalDate()-4, 112) and convert(nvarchar(8), dbo.GetLocalDate(), 112) and isnull(SYNC_FLAG,'')!='Y'");
      }
      sourceRs = new emisRowSet(sourceDb.executeQuery(qrysql.toString()));
      targetDb.prepareStmt("insert into SALE_H_ICHIDO (SL_KEY,S_NO,SL_DATE,SL_TIME,SL_QTY,SL_AMT,PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6,PAY_7,PAY_8,PAY_9,PAY_10,PAY_11,PAY_12,PAY_13,PAY_14,PAY_15,PAY_16,PAY_17,PAY_18,PAY_19,PAY_20,PAY_21,PAY_22,PAY_23,PAY_24,PAY_25,PAY_26,PAY_27,PAY_28,PAY_29,PAY_30,PAY_31,PAY_32,PAY_33,PAY_34,PAY_35,PAY_36,PAY_37,PAY_38,PAY_39,PAY_40,PAY_41,PAY_42,PAY_43,PAY_44,PAY_45,PAY_46,PAY_47,PAY_48,PAY_49,PAY_50)\n" +
          "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      int i = 0;
      while(sourceRs.next()){
        i = 1;
        targetDb.setString(i++,sourceRs.getString("SL_KEY"));
        targetDb.setString(i++,sourceRs.getString("S_NO"));
        targetDb.setString(i++,sourceRs.getString("SL_DATE"));
        targetDb.setString(i++,sourceRs.getString("SL_TIME"));
        targetDb.setString(i++,sourceRs.getString("SL_QTY"));
        targetDb.setString(i++,sourceRs.getString("SL_AMT"));
        targetDb.setString(i++,sourceRs.getString("PAY_1"));
        targetDb.setString(i++,sourceRs.getString("PAY_2"));
        targetDb.setString(i++,sourceRs.getString("PAY_3"));
        targetDb.setString(i++,sourceRs.getString("PAY_4"));
        targetDb.setString(i++,sourceRs.getString("PAY_5"));
        targetDb.setString(i++,sourceRs.getString("PAY_6"));
        targetDb.setString(i++,sourceRs.getString("PAY_7"));
        targetDb.setString(i++,sourceRs.getString("PAY_8"));
        targetDb.setString(i++,sourceRs.getString("PAY_9"));
        targetDb.setString(i++,sourceRs.getString("PAY_10"));
        targetDb.setString(i++,sourceRs.getString("PAY_11"));
        targetDb.setString(i++,sourceRs.getString("PAY_12"));
        targetDb.setString(i++,sourceRs.getString("PAY_13"));
        targetDb.setString(i++,sourceRs.getString("PAY_14"));
        targetDb.setString(i++,sourceRs.getString("PAY_15"));
        targetDb.setString(i++,sourceRs.getString("PAY_16"));
        targetDb.setString(i++,sourceRs.getString("PAY_17"));
        targetDb.setString(i++,sourceRs.getString("PAY_18"));
        targetDb.setString(i++,sourceRs.getString("PAY_19"));
        targetDb.setString(i++,sourceRs.getString("PAY_20"));
        targetDb.setString(i++,sourceRs.getString("PAY_21"));
        targetDb.setString(i++,sourceRs.getString("PAY_22"));
        targetDb.setString(i++,sourceRs.getString("PAY_23"));
        targetDb.setString(i++,sourceRs.getString("PAY_24"));
        targetDb.setString(i++,sourceRs.getString("PAY_25"));
        targetDb.setString(i++,sourceRs.getString("PAY_26"));
        targetDb.setString(i++,sourceRs.getString("PAY_27"));
        targetDb.setString(i++,sourceRs.getString("PAY_28"));
        targetDb.setString(i++,sourceRs.getString("PAY_29"));
        targetDb.setString(i++,sourceRs.getString("PAY_30"));
        targetDb.setString(i++,sourceRs.getString("PAY_31"));
        targetDb.setString(i++,sourceRs.getString("PAY_32"));
        targetDb.setString(i++,sourceRs.getString("PAY_33"));
        targetDb.setString(i++,sourceRs.getString("PAY_34"));
        targetDb.setString(i++,sourceRs.getString("PAY_35"));
        targetDb.setString(i++,sourceRs.getString("PAY_36"));
        targetDb.setString(i++,sourceRs.getString("PAY_37"));
        targetDb.setString(i++,sourceRs.getString("PAY_38"));
        targetDb.setString(i++,sourceRs.getString("PAY_39"));
        targetDb.setString(i++,sourceRs.getString("PAY_40"));
        targetDb.setString(i++,sourceRs.getString("PAY_41"));
        targetDb.setString(i++,sourceRs.getString("PAY_42"));
        targetDb.setString(i++,sourceRs.getString("PAY_43"));
        targetDb.setString(i++,sourceRs.getString("PAY_44"));
        targetDb.setString(i++,sourceRs.getString("PAY_45"));
        targetDb.setString(i++,sourceRs.getString("PAY_46"));
        targetDb.setString(i++,sourceRs.getString("PAY_47"));
        targetDb.setString(i++,sourceRs.getString("PAY_48"));
        targetDb.setString(i++,sourceRs.getString("PAY_49"));
        targetDb.setString(i++,sourceRs.getString("PAY_50"));
        targetDb.prepareUpdate();
      }
      sourceDb.prepareStmt("update SALE_H_ICHIDO set SYNC_FLAG='Y' where SL_KEY=?");
      sourceRs.first();
      while(sourceRs.next()){
        sourceDb.setString(1, sourceRs.getString("SL_KEY"));
        sourceDb.prepareUpdate();
      }
      targetDb.commit();
      sourceDb.commit();
      out.println("["+emisUtil.todayTimeS(true)+"] " +sourceRs.size()+ " Ok! ");
    } catch(Exception e){
      //System.out.println(">>>> 1");
      e.printStackTrace();
      try {
        sourceDb.rollback();
      } catch (Exception e1) {
        out.print(">> 2 >");
        e1.printStackTrace();
      }
      try {
        targetDb.rollback();
      } catch (Exception e1) {
        out.print(">> 3 >");
        e1.printStackTrace();
      }

      out.println("["+emisUtil.todayTimeS(true)+"]" + " Error ");
      out.println(e.getMessage());
    } finally {
      try {
        if (targetDb != null) {
          targetDb.close();
          targetDb = null;
        }
      } catch(Exception e){
        out.println(">>>> 4");
        e.printStackTrace();
      }
      try {
        if (sourceDb != null) {
          sourceDb.close();
          sourceDb = null;
        }
        if(sourceRs != null){
          sourceRs.close();
          sourceRs = null;
        }
      } catch(Exception e){
        System.out.println(">>>> 5");
        e.printStackTrace();
      }
    }
  }

  /**
   * 长沙罗莎-检查日结是否正常,如果不正常重新执行一次.
   * @param application
   * @param request
   * @param out
   * @throws Exception
   */
  private void checkDailyClose(ServletContext application, HttpServletRequest request,JspWriter out) throws Exception {
    emisDb db = null;
    StringBuffer context = new StringBuffer(), sql = new StringBuffer();
    emisProp prop = emisProp.getInstance(application);
    String subject = null;
    int i = 1;
    try {
      db = emisDb.getInstance(application);
      String date = new emisDate().add(-1).toString();
      db.prepareStmt("select VALUE from EMISPROP where NAME = 'EP_DCCR_DATE' and VALUE < ?");
      db.setString(1, date);
      db.prepareQuery();
      out.clear();
      if(db.next()){
        out.println(db.getString("VALUE"));
        emisDailyCloseCount dc = new emisDailyCloseCount(application);
        dc.runTask();
        context.append("日结排程有异常,系统自动重新执行!请检查是否仍有异常?");
        subject = "【" + prop.get("EPOS_COMPANY_NO") + "】日结异常,系统自动重新执行";
        out.println(context.toString());
      } else{
        out.println("Ok!");
      }
    } catch (Exception e) {
      e.printStackTrace();
      context.append(e.getMessage());
      subject ="【" + prop.get("EPOS_COMPANY_NO") + "】日结异常检查出错" ;
      out.clear();
      out.println("*** Error >> "+e.getMessage());
    } finally {
      if (db != null) {
        db.close();
        db = null;
      }
    }
    if (context.length() > 0) {
      context.append("<br/>").append("====================================")
          .append("<br/>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
          .append("<br/>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
          .append("<br/>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(application.getServletContextName())
          .append("<br/>").append("执行程序：").append("jsp/dev/speRequest/SpecialRequest.jsp.checkDailyClose()");
      try {
        emisMailer m = new emisMailer();
        m.setTo("andy.he@emiszh.com;keven.he@emiszh.com");
        m.setSubject(subject);
        m.setContent(context.toString().replaceAll("@","<br/>"));
        m.send(application);
      } catch(Exception e){
        e.printStackTrace();
        out.println("***** Mail Error *****");
        out.println(e.getMessage());
      }
    }
    context.setLength(0);
    context = null;
    sql.setLength(0);
    sql = null;
  }

  /**
   * 获取resin目录下的log文件
   * @param application
   * @param request
   * @param out
   * @throws Exception
   */
  private void showResinLogs(ServletContext application, HttpServletRequest request,JspWriter out) throws Exception {
    String subdir = request.getParameter("subdir");
    if(StringUtils.isEmpty(subdir)) subdir="logs";
    String path = System.getProperty("user.dir")+"\\"+subdir;
    File dir = new File(path);
    StringBuffer content = new StringBuffer();
    String dwurl = null;
    String size = null;
    content.append("<ul>");
    for(File f : dir.listFiles()){
      /*
      if(f.length() < 1024) size = f.length()+"B";
      else if(f.length() >=1024 && f.length() < 1024*1024 ) size = f.length()/1024.0 + "K";
      else if(f.length() >=1024*1024 && f.length() < 1024*1024*1024 ) size = f.length()/1024/1024 + "M";
      else size = f.length()/1024/1024/1024 + "G";
      */
      size = f.length()/1024 + "K";
      dwurl = "../../onlineDownload.jsp?FILE_NAME="+f.getName()+"&FILE_DIR="+path;
      content.append("<li><a href='").append(dwurl).append("'>").append(f.getName()).append("</a>&nbsp;&nbsp(").append(size).append(")</li>");
    }
    content.append("</ul>");
    out.clear();
    out.print(content);
    content.setLength(0);
    content = null;
  }

  public void checkFileDate(String dir, String when, String exclude, StringBuffer out, SimpleDateFormat dateFormater){
    String name = "";
    File fDir = new File(dir);
    if(!fDir.exists()) return; //指定目录不存，不继续往下处理。

    for(File f : fDir.listFiles()){
      name = f.getName();
      if(f.isDirectory()) {
        // 如果目录为设定的排除目录则不作处理
        if (exclude.indexOf(";" + name + ";") >= 0) continue;
        else checkFileDate(f.getPath(), when, exclude, out, dateFormater);
      } else {
        Date fileDate = new Date(f.lastModified());
        //String temp =  dateFormater.format(fileDate);
        //if(when.compareTo(fileDate) <=0 ){
        if(dateFormater.format(fileDate).compareTo(when) >=0 ){
          out.append(f.getPath()).append("</br>");
        }
      }
    }
  }

  // 宜芝多：手动导出C840卡券交易明细资料
  public void ichidoExportC840(ServletContext servlet, HttpServletRequest request, HttpServletResponse response) throws Exception{
    emisDb db = null;
    try{
      String date1 = request.getParameter("SL_DATE1");
      String date2 = request.getParameter("SL_DATE2");
      if(StringUtils.isEmpty(date1) || StringUtils.isEmpty(date2)){
        response.getOutputStream().write("** 开始日期（SL_DATE1）和结束日期（SL_DATE2）参数不可为空 **".getBytes("UTF-8"));
        return;
      }
      if(new emisDate(date1).getDiff(new emisDate(date2)) > 31){
        response.getOutputStream().write("** 查询日期区间不可大于31天 **".getBytes("UTF-8"));
        return;
      }
      String type = request.getParameter("TYPE");
      response.setContentType("application/x-msdownload");
      response.setHeader("Content-Disposition", "attachment;filename=C840("+date1+"-"+date2+")"+(StringUtils.isEmpty(type)?"":"_"+type)+".csv");
      String sqlCond = "";
      if("DLM".equalsIgnoreCase(type)){
        sqlCond = "and ((when sc.CARD_NO like '2605%') or (sc.CARD_NO like '90012605%') or (len(sc.CARD_NO) in (21,25) and substring(sc.CARD_NO,2,1) = '2') or (len(sc.CARD_NO) in (21,25) and substring(sc.CARD_NO,2,1) = '9') ) \n";
      } else if("WM".equalsIgnoreCase(type)){
        sqlCond = "and ((substring(sc.CARD_NO,1,3) between '918' and '948') or (substring(sc.CARD_NO,1,3) between '818' and '848') ) \n";
      }
      db = emisDb.getInstance(servlet);

      db.executeQuery(
          "select ''''+sc.S_NO 门店编号,s.S_NAME as 门店名称,dbo.emisCombDate(sc.SL_DATE,'/') as 交易日期,dbo.emisTimeFlag(sc.CRE_TIME) as 交易时间, /*isnull(gs.GS_NAME,sc.CARD_NAME) as 卡券种类, dbo.emisGetSysTabName('CARD_TYPE',gt.CARD_TYPE) [卡 别],\n" +
          "  gt.P_AMT 面值金额,*//*dbo.emisCombDate(sc.CRE_DATE,'/') as 消费日期,*/\n" +
          "  ''''+sc.SL_NO 交易序号, ''''+sc.CARD_NO as 卡券编号,/*N'交易' as 交易状态,\n" +
          "  (sc.BALANCE + sc.AMT)*case when QTY>0 then QTY else 1 end as 交易前余额,*/\n" +
          "  sc.AMT*case when QTY>0 then QTY else 1 end as 交易金额/*,sc.BALANCE*case when QTY>0 then QTY else 1 end as 交易后余额,\n" +
          "  sc.CARD_PCT [折  数],round(isnull(AMT,0)* (100.00-isnull(CARD_PCT,0)) / 100.00,2) as 折扣金额,\n" +
          "  round((isnull(AMT*case when QTY>0 then QTY else 1 end,0) - round(isnull(AMT*case when QTY>0 then QTY else 1 end,0)* (100.00-isnull(CARD_PCT,0)) / 100.00,2)),2) as 拆账金额,\n" +
          "  sc.REMARK [备  注]*/\n" +
          "from Sale_card sc with(nolock)\n" +
          "left join Gift_token gt  with(nolock) on gt.GT_NO = sc.CARD_NO\n" +
          "left join gift_set gs  with(nolock) on gs.GS_NO = gt.GS_NO\n" +
          "left join Store s  with(nolock) on s.S_NO=sc.S_NO\n" +
          "where ( sc.CARD_TYPE <> '1D' )  \n" +
          "  and ( sc.SL_DATE >= '"+date1+"' )  and  ( sc.SL_DATE <= '"+date2+"' )\n" +
             sqlCond +
          "order by 门店编号,交易日期,交易序号,卡券编号"
      );

      StringBuffer sbTmp = new StringBuffer("");
      for(int i = 1; i<=db.getColumnCount(); i++){
        // System.out.println(db.getColumnName(i) +">>>>>>>>"+db.getColumnType(i));
        if(i>1) sbTmp.append(",");
        sbTmp.append(db.getColumnName(i));
      }
      sbTmp.append("\r\n");
      response.getOutputStream().write(sbTmp.toString().getBytes());
      sbTmp.setLength(0);
      while(db.next()){
        for(int i = 1; i<=db.getColumnCount(); i++){
          if(i>1) sbTmp.append(",");
          switch (db.getColumnType(i)) {
            case Types.CHAR:
            case Types.VARCHAR:
              if(db.getString(i) == null) sbTmp.append("");
              else sbTmp.append(db.getString(i));
              break;
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
              sbTmp.append(db.getFloat(i));
              break;
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
              sbTmp.append(db.getInt(i));
              break;
            default:
              sbTmp.append("");
              break;
          }
        }
        response.getOutputStream().write(sbTmp.toString().getBytes());
        sbTmp.setLength(0);
        sbTmp.append("\r\n");
      }
      sbTmp = null;
    } finally {
      if(db != null) db.close();
    }
  }

  public void checkUpload(ServletContext servlet, HttpServletRequest request, JspWriter out) throws Exception{
    /*
    insert into SCHED(S_NAME,S_SERVER,S_DESC,S_CLASS,RUNLEVEL,SYEAR,SMONTH,SDAY,SHOUR,STIME,INTERVAL,PARAM,SHOUR_END,STIME_END,S_MENU,THREAD_GROUP,REMARK)
    values (N'specialSched-Upload',N'STOP',N'特别排程-检查数据上传第三方排程',N'com.emis.schedule.epos.emisSpecialSched',N'D',N'',N'',N'',N'08',N'32',N'600',N'act=callOtherSystem&url=http://ichido-pos-08r2.chinanorth.cloudapp.chinacloudapi.cn:8080/ichido/jsp/dev/speRequest/SpecialRequest.jsp?act=checkUpload',N'08',N'32',N'6',N'OTHER_GROUP',null)
     */
    emisDb db = null;
    JSONObject retJson = new JSONObject();
    retJson.put("mailTo","andy.he@emiszh.com;84888322@qq.com");
    retJson.put("subject","FTP排程执行异常");
    StringBuffer content = new StringBuffer();
    content.append("[宜芝多-96广场店销售数据上传]排程执行失败！<br>\n");
    content.append("[宜芝多-地铁门店销售数据上传]排程执行失败！<br>\n");
    retJson.put("content", content.toString());
    out.clear();
    out.print(retJson.toString());
  }

  public void checkC410(ServletContext servlet, HttpServletRequest request, JspWriter out) throws Exception{
    /*
    insert into SCHED(S_NAME,S_SERVER,S_DESC,S_CLASS,RUNLEVEL,SYEAR,SMONTH,SDAY,SHOUR,STIME,INTERVAL,PARAM,SHOUR_END,STIME_END,S_MENU,THREAD_GROUP,REMARK)
    values (N'specialSched-checkC410-freemori',N'Vi',N'特别排程-检查C410预授权未确认记录-浮力森林',N'com.emis.schedule.epos.emisSpecialSched',N'D',N'',N'',N'',N'05',N'00',N'600',N'act=callOtherSystem&url=http://124.160.56.155:8601/freemori/jsp/dev/speRequest/SpecialRequest.jsp?act=checkC410',N'05',N'00',N'6',N'OTHER_GROUP',null)
   */
    emisDb db = null;
    emisDb db2 = null;
    emisDb db3 = null;
    PreparedStatement settleData = null;
    StringBuffer content = new StringBuffer();
    JSONObject retJson = new JSONObject();
    try {
      String dateS = new emisDate().add(-2).toString();
      String dateE = new emisDate().add(-1).toString();
      String slKey = "";
      String gtNo = "";
      String menusKey = "";
      String optType = "";
      //date = new emisDate().toString();

      emisProp prop = emisProp.getInstance(servlet);

      db = emisDb.getInstance(servlet);
      db2 = emisDb.getInstance(servlet);
      db3 = emisDb.getInstance(servlet);
      db.prepareStmt("select top 10 case ba.OPT_TYPE when '01' then N'S340' \n" +
          "when '02' then N'S530' \n" +
          "when '03' then N'C722' \n" +
          "when '04' then N'C350' end as OPT_TYPE_NAME, \n" +
          "ba.SL_KEY, ba.GT_NO,ba.OPT_AMT,ba.OPT_TYPE from BARCARD_AMT ba with(nolock) where ba.SL_DATE between ? and ? and ba.FLS_NO='ED'");
      db.setString(1,dateS);
      db.setString(2,dateE);
      db.prepareQuery();
      int cnt = 0;
      while (db.next()) {
        menusKey = db.getString("OPT_TYPE_NAME");
        slKey = db.getString("SL_KEY");
        gtNo = db.getString("GT_NO");
        optType = db.getString("OPT_TYPE");
        if ("S340".equals(menusKey)) {
          db2.prepareStmt("select 1 from sale_h where SL_KEY=?");
          db2.setString(1, slKey + '1');
        } else if ("S530".equals(menusKey)) {
          db2.prepareStmt("select 1 from SALE_ORDER_H where SL_KEY=?");
          db2.setString(1, slKey + '1');
        } else if ("C722".equals(menusKey)) {
          db2.prepareStmt("select 1 from sale_card sl\n" +
              "left join GIFT_TOKEN gt on gt.GT_NO=sl.CARD_NO\n" +
              "left join GIFT_SET gs on gt.GS_NO=gs.GS_NO \n" +
              "where sl.CARD_TYPE='1' and isnull(sl.PAY_TYPE,'')='1' and isnull(sl.S_SOURCE,'')='' and gs.GS_KIND='3' and gs.GS_TYPE='2'\n" +
              "and sl.sl_key=?");
          db2.setString(1, slKey);
        } else {
          db2.prepareStmt("select 1 from IC_GIFT_RECHARGE where GP_KEY=?");
          db2.setString(1, slKey);
        }
        db2.prepareQuery();
        if (db2.next()) {
          settleData = db3.prepareStmt("exec dbo.eposUpdBarcard_amt ?,?,?,'CO'");
          settleData.setString(1, slKey);
          settleData.setString(2, optType);
          settleData.setString(3, gtNo);
          if (settleData.executeUpdate() <= 0) {
            content.append(slKey).append("，").append(gtNo).append("<br>");
          }
        } else {
          content.append(slKey).append("，").append(gtNo).append("<br>");
        }
        cnt++;
      }

      if(content.length()>0){
        retJson.put("subject","【" + prop.get("EPOS_COMPANY_NO") + "】近两日("+dateS+" - "+dateE+")卡消费和充值预授权未确认记录");
        //retJson.put("mailTo","andy.he@emiszh.com;fang.liu@emiszh.com;tim.guo@emiszh.com;viva.chen@emiszh.com;kiro.tang@emiszh.com;");
        retJson.put("mailTo",prop.get("MAIL_SMTP_ADDRESS_TO_ERRORCARD","fang.liu@emiszh.com;kiro.tang@emiszh.com;keven.he@emiszh.com;andy.he@emiszh.com"));
        content.insert(0,"单号，卡号<br>");
        if( cnt >= 10 ) content.append("(**以上数据仅显示前10笔记录,实际请以C410作业查询为准**)<br>");
        content.append("<br>").append("====================================")
            .append("<br>").append("公司名称：").append(prop.get("EPOS_COMPANY"))
            .append("<br>").append("系统描述：").append(prop.get("EPOS_SYSTEM_TITLE"))
            .append("<br>").append("系统网址：").append(prop.get("EPOS_SERVER_NAME_OUT")).append(servlet.getServletContextName());

        retJson.put("content", content.toString());
      } else {
        retJson.put("content", "");
      }
    } catch (Exception e) {
      e.printStackTrace();
      retJson.clear();
      retJson.put("content", "*** Error >> " + e.getMessage());
    } finally {
      if (db != null) {
        db.close();
      }
      if (db2 != null) {
        db2.close();
      }
      if (db3 != null) {
        db3.close();
      }
    }
    content.setLength(0);
    content = null;
    out.clear();
    out.print(retJson.toString());
  }

  private void zip(ServletContext context,HttpServletResponse response) throws Exception {
    List<String> files = new ArrayList<String>();
    emisDirectory oRootDir = emisFileMgr.getInstance(context).getDirectory("root");
    BufferedReader br = new BufferedReader(new FileReader(new File(oRootDir.subDirectory("data").getDirectory(),"zip.cfg")));
    try {
      String line = null;
      while ((line = br.readLine()) != null) {
        files.add(line);
      }
    } finally{
      br.close();
      br = null;
    }
//    FileOutputStream fileOut = null;
//    try {
//      fileOut = new FileOutputStream(oRootDir.subDirectory("data").getDirectory()+(emisUtil.formatDateTime("%y%M%D%h%m%s",new Date()))+".zip");
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    }
//    ZipOutputStream out = new ZipOutputStream(fileOut);
    response.setContentType("application/x-msdownload");
    response.setHeader("Content-Disposition", "attachment;filename="+(emisUtil.formatDateTime("%y%M%D%h%m%s",new Date()))+".zip");
    ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
    for(String path : files ) {
      File souceFile = new File(path);
      if(souceFile.exists()) {
        zip(souceFile, out, souceFile.getPath().substring(4));
      } else {
        System.out.println(">>>>>>>>>>>>> 文件不存在：" + path);
      }
    }
    out.close();
  }

  private void zip(File souceFile, ZipOutputStream out, String base)  throws IOException {
    if (souceFile.isDirectory()) {
      File[] files = souceFile.listFiles();
      out.putNextEntry(new ZipEntry(base + "/"));
      base = base.length() == 0 ? "" : base + "/";
      for (File file : files) {
        zip(file, out, base + file.getName());
      }
    } else {
      if (base.length() > 0) {
        out.putNextEntry(new ZipEntry(base));
      } else {
        out.putNextEntry(new ZipEntry(souceFile.getName()));
      }
      System.out.println("filepath=" + souceFile.getPath());
      FileInputStream in = new FileInputStream(souceFile);

      int b;
      byte[] by = new byte[1024];
      while ((b = in.read(by)) != -1) {
        out.write(by, 0, b);
      }
      in.close();
    }
  }

  public void checkUpdateFiles(ServletContext servlet, HttpServletRequest request, JspWriter out) throws Exception {
    emisDirectory oRootDir = emisFileMgr.getInstance(servlet).getDirectory("root");
    String dir = oRootDir.getDirectory();

    Hashtable oRequest_ = emisUtil.processRequest(request);
    if(StringUtils.isNotEmpty((String)oRequest_.get("dir"))) {
      dir = (String)oRequest_.get("dir");
      if(dir.startsWith("@user.dir")) {  //指resin的安装目录
        dir = emisUtil.stringReplace(dir, "@user.dir", System.getProperty("user.dir"), "a");
      }else if(dir.startsWith("@")){  //指wwwroot下的目录
        dir = oRootDir.getDirectory() + dir.substring(1);
      }
    }
    //String whenDate = "2017-01-03 11:10";
    String whenDate = (String)oRequest_.get("fromDate");
    if(StringUtils.isEmpty(whenDate)) {
      out.println("检查开始日期（fromDate）未指定");
      return;
    }
    String exclude=".svn;cache;data;dynamic;logs;report_def;report_out;users;migration;tmp;work;version;sql";
    int whenY = Integer.parseInt(whenDate.substring(0,4)) - 1900;
    int whenM = Integer.parseInt(whenDate.substring(5,7)) - 1;
    int whenD = Integer.parseInt(whenDate.substring(8,10));
    int whenH = Integer.parseInt(whenDate.substring(11,13));
    int whenMin = Integer.parseInt(whenDate.substring(14,16));
    Date when = new Date( whenY, whenM,whenD,whenH, whenMin);
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    out.println("***************** 如下文件目录不检查 ******************* </br>");
    out.println(exclude+" </br>");
    out.println("********************************************************* </br>");
    out.println("******* " + dateFormater.format(when) + "后异动的文件列表 ******** </br>");
    dateFormater.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    StringBuffer filelist =  new StringBuffer();
    exclude = ";" + exclude + ";";
    checkFileDate(dir, whenDate, exclude, filelist, dateFormater);
    out.println(filelist.toString());
  }

  public void downloadFile(ServletContext servlet, HttpServletRequest request, JspWriter out) throws Exception {
    String url = request.getParameter("fileUrl");
    String savePath = request.getParameter("savePath");
    String saveName = request.getParameter("saveName");
    String method = "GET";

    FileOutputStream fileOut = null;
    HttpURLConnection conn = null;
    InputStream inputStream = null;
    BufferedOutputStream bos = null;
    BufferedInputStream bis = null;
    try {
      if (savePath.startsWith("@/")) {
        emisDirectory dir = emisFileMgr.getInstance(servlet).getDirectory("root");
        savePath = dir.subDirectory(savePath.substring(2)).getDirectory();
      } else if (savePath.startsWith("@user.dir/")) {
        savePath = emisUtil.stringReplace(savePath, "@user.dir", System.getProperty("user.dir"), "a");
      }
      if (StringUtils.isEmpty(saveName)) saveName = url.substring(url.lastIndexOf("/"));

      File file = new File(savePath);

      //判断文件夹是否存在
      if (!file.exists()) {
        //如果文件夹不存在，则创建新的的文件夹
        file.mkdirs();
      }
      // 建立链接
      URL httpUrl = new URL(url);
      conn = (HttpURLConnection) httpUrl.openConnection();
      //以Post方式提交表单，默认get方式
      conn.setRequestMethod(method);
      conn.setDoInput(true);
      conn.setDoOutput(true);
      // post方式不能使用缓存
      conn.setUseCaches(false);
      //连接指定的资源
      conn.connect();
      //获取网络输入流
      inputStream = conn.getInputStream();
      bis = new BufferedInputStream(inputStream);
      //判断文件的保存路径后面是否以/结尾
      if (!savePath.endsWith("/")) {
        savePath += "/";
      }
      //写入到文件（注意文件保存路径的后面一定要加上文件的名称）
      fileOut = new FileOutputStream(savePath + saveName);
      bos = new BufferedOutputStream(fileOut);

      byte[] buf = new byte[8192];
      int length = bis.read(buf);
      //保存文件
      while (length != -1) {
        bos.write(buf, 0, length);
        length = bis.read(buf);
      }
      bos.flush(); //加上这句，不然文件写不全。

      conn.disconnect();
      conn = null;

      out.println("{'msg':'ok'}");
    } catch (Exception e) {
      e.printStackTrace();
      out.println("{'msg':" + e.getMessage() + "'}");
    } finally {
      try {
        if (fileOut != null) fileOut.close();
        if (bis != null) bis.close();
        if (bos != null) bos.close();
        if (conn != null) conn.disconnect();
      } catch (Exception e) {
        e.printStackTrace();
        out.println("{'msg':" + e.getMessage() + "'}");
      }
    }
  }
%>
<%
  String sAct = request.getParameter("act");
  if("updateStatistics".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=updateStatistics
    updateStatistics(application,request,out);
    session.invalidate();
  } else if("emailJiuyuanDb2ERP".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=emailJiuyuanDb2ERP
    emailJiuyuanDb2ERP(application,request,out);
    session.invalidate();
  } else if("backupData2History".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=backupData2History
    backupData2History(application,request,out);
  } else if("deleteHistoryData".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=deleteHistoryData
    deleteHistoryData(application, request, out);
    session.invalidate();
  } else if("checkSale2ERP".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=checkSale2ERP
    checkSale2ERP(application,request,out);
    session.invalidate();
  } else if("getIC_AMT".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=getIC_AMT
    getIC_AMT(application, request, out);
    session.invalidate();
  } else if("pos2stage".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=pos2stage
    pos2stage(application, request, out);
    session.invalidate();
  } else if("C840".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=C840&SL_DATE1=20180701&SL_DATE2=20180725&TYPE=WM
    out.clear();
    ichidoExportC840(application ,request, response);
    session.invalidate();
  } else if("checkDailyClose".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=checkDailyClose
    checkDailyClose(application, request, out);
    session.invalidate();
  } else if("showResinLogs".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=showResinLogs
    showResinLogs(application, request, out);
  } else if("checkGiftAmt".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=checkGiftAmt
    checkGiftAmt(application, request, out);
    session.invalidate();
  } else if("checkUpdateFiles".equalsIgnoreCase(sAct)){
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=checkUpdateFiles&fromDate=2017-01-03 11:10
    checkUpdateFiles(application,request,out);
    session.invalidate();
  } else if("checkUpload".equals(sAct)){
    checkUpload(application, request, out);
    session.invalidate();
  } else if("checkC410".equals(sAct)){
    checkC410(application, request, out);
    session.invalidate();
  } else if("zipFiles".equals(sAct)){
    zip(application,response);
    out.print("*********** Ok **************");
    session.invalidate();
  } else if("downloadFile".equals(sAct)){
    //从另一台AP上下载文件至本AP上。
    // http://localhost:8081/Vi/jsp/dev/speRequest/SpecialRequest.jsp?act=downloadFile&fileUrl=http://localhost:8081/Vi/data/M331.xml&savePath=@/data/tmp
    downloadFile(application, request, out);
    session.invalidate();
  }
%>
