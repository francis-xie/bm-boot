package com.emis.business;

import com.emis.db.emisConnectProxy;
import com.emis.db.emisDb;
import com.emis.db.emisDbConnector;
import com.emis.db.emisDbMgr;
import com.emis.user.emisEposUserImpl;
import com.runqian.base4.util.DBTypes;
import com.runqian.report4.model.ReportDefine;
import com.runqian.report4.usermodel.*;
import com.runqian.report4.util.ReportUtils;
import com.runqian.report4.view.excel.ExcelReport;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Dana
 * Date: 2012-4-28
 * Time: 15:41:26
 * To change this template use File | Settings | File Templates.
 * Track+[23125] 2013/06/19 Austen.liao 預設列印-發送排程emisPrePrint
 */
public class emisPrePrintQuiee extends emisPrePrintUtil {

  public emisPrePrintQuiee(ServletContext context,HttpServletRequest request) {
    super(context, request);
  }

  /**
   * 排程打印報表
   *
   * @param params param的格式 select.xml.SavePrePrint: @sKEYS + '#' + @sUSERID + '#' + S_NO + '#' + PASSWORD + '#' + @sNAME2
   *               key|title值,用戶ID,門市,登錄密碼,預設報表名稱.以#(井號)分隔
   */
  public Map<Integer, String> createReport(String[] params) {
    log = "參數錯誤";
    String key = params[0], userId = params[1], s_no = params[2], pwd = params[3], name = params[4], guid = params[6];
    xlsPath += userId;
    log = "生成文件名錯誤";
    //報表名
    if (xlsName == null || "".equals(xlsName)) {
      /*
      xlsName = new StringBuffer(key).append(userId).append(name).
          append(new SimpleDateFormat(" yyyy-MM-dd HH-mm-ss ").format(new Date())).
          append(new java.util.Random().nextInt()).toString();
      */
      xlsName = new StringBuffer(key).append("_").append(name).append("_")
          .append(new SimpleDateFormat("yyyyMMddHHmm").format(new Date())).toString();
    }

    try {
      log = "初使化 Business 錯誤";
      //構建User
      emisEposUserImpl user = new emisEposUserImpl(app, s_no, "", userId, pwd, false, null);
      //構建Business
      emisBusinessImpl _oBusiness = new emisBusinessImpl(key, app, user, key + ".xml", false);
      //構建Request
      emisHttpServletRequest request = new emisHttpServletRequest();
      _oBusiness.setParameter(request);
      //獲取&設置列印條件
      emisGetPreQuery query = new emisGetPrePrint(app, key, userId, guid);

      parameters = query.getParams(name);
      _oBusiness.setParameter(parameters);
      //錯誤的數據
      if (_oBusiness.getRequest().getParameter("ACT") == null) return null;

      //設置輸出,仿jsp的out對像
      if (out == null)
        out = creteWrite(app.getRealPath(xlsPath));
      _oBusiness.setWriter(out);

      //設置報表目錄
      _oBusiness.setParameter("PROP_REPORT_DIR", xlsPath);
      //設置報表名稱
      _oBusiness.setParameter("PROP_REPORT_FILE", xlsName);

      log = "產生報表錯誤";
      //列印報表,調用系統的report action
/*      emisPrintData printAct = createAction(loadXML(key), _oBusiness, out);
      printAct.doit();*/
      Map<Integer, String> xlsFileName = new HashMap<Integer, String>();
      xlsFileName.put(1, xlsName);
      createQuieeReport(_oBusiness);

      log = "保存記錄錯誤";
      //發送郵件
      afterReport(xlsFileName, guid, xlsPath + "/" + xlsName, key, name,oRequest_);
      log = "1";
      //如果成功返回路徑
      return xlsFileName;
    } catch (Throwable e) {
      log = e.getMessage();
      if ("NoData".equals(e.getMessage()))  log = "NoData";
      e.printStackTrace();
    } finally {
      saveLog(userId, xlsPath + "/" + xlsName, log);
    }
    return null;
  }

  private void createQuieeReport(emisBusinessImpl _oBusiness) throws Throwable {
    String report = parameters.get("RPT_ACT");                //報表模板.raq文件名稱
    String rpt_file_type = parameters.get("RPT_FILE_TYPE");  //報表類型,xls,pdf,doc
    String reportFileHome = Context.getInitCtx().getMainDir();   //報表模板路徑 quiee/reportFile/

    //保证报表名称的完整性
    int iTmp = 0;
    if ((iTmp = report.lastIndexOf(".raq")) <= 0) {
      report = report + ".raq";
      iTmp = 0;
    }

    //取SQL,最多可以取10個SQL,分別為SQL,SQL1~SQL9,需在作業中使用ACT,ACT1~ACT9指定
    _oBusiness.process(parameters.get("ACT"));  // XML內的action名稱
    parameters.put("SQL", _oBusiness.getReportSql());
    //System.out.println(parameters.get("ACT"));
    //System.out.println("SQL=====" + _oBusiness.getReportSql());
    String act = null;
    for (int i = 1; i <= 9; i++) {
      act = "ACT" + i;
      if (parameters.get(act) != null) {
        _oBusiness.process(parameters.get(act));  // XML內的action名稱
        parameters.put("SQL" + i, _oBusiness.getReportSql());
      }
    }

    //以下代码是检测这个报表是否有相应的参数模板
//    String paramFile = report.substring(0, iTmp) + "_arg.raq";
//    File f = new File(app.getRealPath(reportFileHome + File.separator + paramFile));

    //取得報表模板物理位置
    String reportFile = app.getRealPath(reportFileHome + File.separator + report);

    //构建报表引擎计算环境
    Context ctx = new Context();

    //定義報表模板
    ReportDefine rd = (ReportDefine) ReportUtils.read(reportFile);

    //傳遞參數
    ctx.setParamMap(parameters);

    emisDb oDb = null;
    try {
      ctx.setDefDataSourceName("jdbc/VENUS");

      oDb = emisDb.getInstance(_oBusiness.getContext());
      ctx.setConnection("jdbc/VENUS", oDb.getConnection());

      DataSourceConfig sourceConfig = new DataSourceConfig(DBTypes.SQLSVR, false, "UTF-8", "UTF-8", false);
      ctx.setDataSourceConfig("jdbc/VENUS", sourceConfig);

      //定義報表引擎
      Engine engine = new Engine(rd, ctx);

      //產生報表
      IReport iReport = engine.calc();

      //指定輸出格式
      if (rpt_file_type.equals("pdf")) {
        ReportUtils.exportToPDF(app.getRealPath(xlsPath) + "/" + xlsName + "_1.pdf", iReport);
      } else if (rpt_file_type.equals("doc")) {
        ReportUtils.exportToDOC(app.getRealPath(xlsPath) + "/" + xlsName + "_1.doc", iReport);
      } else {
        ReportUtils.exportToExcel(app.getRealPath(xlsPath) + "/" + xlsName + "_1.xls", iReport, false);
      }

    } finally {
      if (oDb != null) oDb.close();
    }
  }


  public static void main(String[] args) throws IOException {
    emisPrePrintQuiee uu = new emisPrePrintQuiee(null,null);
    System.out.println(uu.batchDownload(null, null, ""));
    uu.batchDownloadDelHistory("ROOT");
  }
}
