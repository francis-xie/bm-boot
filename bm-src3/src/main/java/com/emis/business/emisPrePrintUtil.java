package com.emis.business;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.mail.emisMailNew;
import com.emis.user.emisEposUserImpl;
import com.emis.util.emisLangRes;
import com.emis.util.emisZipUtil;
import com.emis.xml.emisXMLCache;
import com.emis.xml.emisXmlFactory;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.emis.util.emisLangRes.getInstance;

/**
 * Created by IntelliJ IDEA.
 * User: zhong.xu
 * Date: 2009-2-27
 * Time: 15:41:26
 * To change this template use File | Settings | File Templates.
 * Track+[23125] 2013/06/19 Austen.liao 預設列印-發送排程emisPrePrint
 * Track+[23244] win.wu 2013/06/27 預設列印-預設列印报表下载
 * Track+[23125]Austen.liao 2013/07/08 根據設置畫面設置 勾選【是否帶入附檔】欄位，發送MAIL時判斷是否要發送附檔
 * Track+[23346]Austen.liao 2013/07/09 預設列印-清理過時的下载信息
 */
public class emisPrePrintUtil {

  protected HttpServletRequest oRequest_;

  public emisPrePrintUtil(ServletContext context, HttpServletRequest request) {
    this.app = context;
    this.oRequest_ = request;
    if(request !=  null){
      try {
        _oLang = getInstance(request);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (_oLang == null) { //背景执行时，取默认的
      try {
        _oLang = getInstance(app);
        _oLang.setLanguage("zh_CN");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  /**
   * 排程打印报表
   *
   * @param params param的格式 select.xml.SavePrePrint: @sKEYS + '#' + @sUSERID + '#' + S_NO + '#' + PASSWORD + '#' + @sNAME2
   *              key|title值,用戶ID,門市,登錄密碼,預設报表名稱.以#(井號)分隔
   */
  public Map<Integer, String> createReport(String [] params) {
    log = "參數錯誤";
    String key = params[0], userId = params[1], s_no = params[2], pwd = params[3], name = params[4], guid=params[6];
    xlsPath += userId;
    log = "生成文件名錯誤";
    //报表名
    if (xlsName == null || "".equals(xlsName))
      xlsName = new StringBuffer(key).append(userId).append(name).
        append(new SimpleDateFormat(" yyyy-MM-dd HH-mm-ss ").format(new Date())).
        append(new java.util.Random().nextInt()).toString();

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
      if (_oBusiness.getRequest().getParameter("act") == null) return null;
      //設置輸出,仿jsp的out對像
      if (out == null)
        out = creteWrite(app.getRealPath(xlsPath));
      _oBusiness.setWriter(out);

      //設置列印參數,列印條件已經
      _oBusiness.setParameter("RPT_OUTER_TYPE", "EXCEL");
      _oBusiness.setParameter("EXCEL_REGION_ROW", "false");
      _oBusiness.setParameter("RPT_WREP_DISPLAY", "false");
      _oBusiness.setParameter("RPT_EXCEL2_DISPLAY", "false");
      _oBusiness.setParameter("RPT_OCX_SHOW", "Y");
      //設置报表目錄
      _oBusiness.setParameter("PROP_REPORT_DIR", xlsPath);
      //設置报表名稱
      _oBusiness.setParameter("PROP_REPORT_FILE", xlsName);

      log = "產生报表錯誤";
      //列印报表,調用系統的report action
      emisPrintData printAct = createAction(loadXML(key), _oBusiness, out);
      printAct.doit();

      log = "保存記錄錯誤";

      //發送郵件
      afterReport(printAct.getXlsFileName(), guid, xlsPath + "/" + xlsName, key, name, oRequest_);
      log = "1";
      //如果成功返回路徑
      return printAct.getXlsFileName();
    } catch (Exception e) {
      log = e.getMessage();
      if ("NoData".equals(e.getMessage()))
        log = "NoData";
      e.printStackTrace();
    } finally {
      saveLog(userId, xlsPath + "/" + xlsName, log);
    }
    return null;
  }

  //系統的report action,對取action進行了優化
  public emisPrintData createAction(Document oBusinessDoc_, emisBusinessImpl _oBusiness, Writer out) throws Exception {
    NodeList nList = oBusinessDoc_.getElementsByTagName("act");
    if (nList != null) {
      int len = nList.getLength();
      String act = _oBusiness.getRequest().getParameter("ACT");
      if (act == null || "".equals(act)) act = _oBusiness.getRequest().getParameter("act");
      for (int i = 0; i < len; i++) {
        Node oNode = nList.item(i);
        if (oNode.getNodeType() == Node.ELEMENT_NODE) {
          Element e = (Element) oNode;
          String sName = e.getAttribute("name");
          if (act.equals(sName))
            return new emisPrintData(_oBusiness, (Element) e.getElementsByTagName("report").item(0), out);
        }
      }
    }
    return null;
  }

  //加載XML
  public Document loadXML(String bName) throws Exception {
    Document oBusinessDoc_;
    emisBusinessCacheMgr _oCacheMgr = emisBusinessCacheMgr.getInstance(app);
    emisXMLCache _oBusinessXML = _oCacheMgr.get(bName);
    if (_oBusinessXML != null) {
      return (Document) _oBusinessXML.getCache();
    } else {
      emisFile f = getBusinessFile(bName);
      InputStream in = f.getInStream();
      oBusinessDoc_ = emisXmlFactory.getXML(in);
      in.close();
      emisXMLCache _oXMLCache = new emisXMLCache(bName, f, oBusinessDoc_);
      _oCacheMgr.put(_oXMLCache);
    }
    return oBusinessDoc_;
  }

  //獲取out輸出對像
  public Writer creteWrite(String dir) throws IOException {
    File file = new File(dir);
    file.mkdirs();
    file = new File(dir + "/jsp.out");
    file.createNewFile();
    return new JspOut(file);
  }

  //獲取Business文件
  public emisFile getBusinessFile(String name) throws Exception {
    return emisFileMgr.getInstance(app).getDirectory("ROOT").subDirectory("business").getFile(name + ".xml");
  }

  //保存數據供下载使用
  protected int saveToDownLoad(String xlsName, Map<Integer, String> path, int END_DAY, String... params) throws Exception {
    int count = 0;
    emisDb db = null;

    String rpt_file_type = parameters.get("RPT_FILE_TYPE");
    if (StringUtils.isEmpty(rpt_file_type)) rpt_file_type = "xls";
    try {
      db = emisDb.getInstance(app);
      for (Map.Entry<Integer, String> entry : path.entrySet()) {
        PreparedStatement stmt = db.prepareStmt(sqlCommand);
        stmt.setString(1, params[0]);
        stmt.setString(2, params[1]);
        stmt.setString(3, params[2]);
        stmt.setInt(4, ++count);
        stmt.setString(5, entry.getValue().replace('\\', '/'));
        stmt.setString(6, xlsName + "_" + count + "." + rpt_file_type);
        stmt.setInt(7, END_DAY);
        stmt.executeUpdate();

      }
      //out.write("成功生成" + count + "个报表,文件名:" + xlsName, true);
      return count;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (db != null)
        db.close();
    }
    return count;
  }

  //刪除报表
  public String deletePrint(String ids, String[] delxls) throws Exception {
    int delCount = 0, updCount;
    for (String xls : delxls) {
      if (new File(xls).delete())
        delCount++;
    }
    emisDb db = emisDb.getInstance(app);
    updCount = db.executeUpdate("delete pre_print_download where id in (" + ids + ")");
    db.close();
    return "成功刪除文件: " + delCount + " 个,刪除記錄: " + updCount + " 條";
  }

  //批量下载报表
  public String batchDownload(String ids, String[] xls, String userid, String IPADDR) throws IOException {
    batchDownloadDelHistory(userid);

    String fileName = new StringBuffer(xlsPath).append(userid).append("批量下载").append(new Random().nextInt(1000)).append(".zip").toString();
    File zipName = new File(app.getRealPath(fileName));
    if (zipName.exists()) zipName.delete();
    emisZipUtil zip = new emisZipUtil(zipName);
    //zip.comment.append("报表批量下载!\n\n");
    zip.put(xls);
    //zip.comment.append("\n共 ").append(xls.length).append(" 个报表,成功压缩: ").append(zip.getFileCount()).append(" 个!");
    zip.setComment(zip.comment.toString());
    zip.close();

    try {
      emisDb db = emisDb.getInstance(app);
      db.executeUpdate("update pre_print_download set ISDOWNLOAD = isnull(ISDOWNLOAD,0) + 1 where id in(" + ids + ")");
      db.executeUpdate("insert into Pre_Print_Download_User_Log(USERID,FILENAME,IPADDR,CRE_DATE,CRE_TIME)\n" +
          "select USERID,XLSNAME,'" + IPADDR + "',convert(nvarchar(8),dbo.GetLocalDate(),112),dbo.emisTime(dbo.GetLocalDate(),'')\n" +
          "from pre_print_download where id in (" + ids + ")");
      db.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return fileName;
  }

  //批量下载报表(只將下载报表打包)
  public String batchDownload(String ids, String[] xls, String userid) throws IOException {
    batchDownloadDelHistory(userid);

    String fileName = new StringBuffer(xlsPath).append(userid).append("批量下载").append(new Random().nextInt(1000)).append(".zip").toString();
    File zipName = new File(app.getRealPath(fileName));
    if (zipName.exists()) zipName.delete();
    emisZipUtil zip = new emisZipUtil(zipName);
    //zip.comment.append("报表批量下载!\n\n");
    zip.put(xls);
    //zip.comment.append("\n共 ").append(xls.length).append(" 个报表,成功压缩: ").append(zip.getFileCount()).append(" 个!");
    zip.setComment(zip.comment.toString());
    zip.close();

    return fileName;
  }

  //刪除歷史文件
  void batchDownloadDelHistory(final String userid) {
    try {
      File[] history = new File(app.getRealPath(xlsPath)).listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.startsWith(userid) && name.endsWith(".zip");
        }
      });

      if (history.length > 0)
        for (File f : history) f.delete();
    } catch (Exception ex) {
      System.out.println("刪除歷史文件錯誤!");
      ex.printStackTrace();
    }
  }

  //寫日誌
  public int saveLog(String... params) {
    int count = 0;
    emisDb db = null;
    try {
      db = emisDb.getInstance(app);
      PreparedStatement stmt = db.prepareStmt(logCommand);
      stmt.setString(1, params[0]);
      stmt.setString(2, params[1]);
      stmt.setString(3, params[2]);
      return stmt.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (db != null)
        db.close();
    }
    return count;
  }

  public String getLog() {
    return this.log;
  }

  public void afterReport(Map<Integer, String> xlsFileName, String guid, String path, String key, String name, HttpServletRequest request) throws Exception{
    PreparedStatement GetMail = null;
    PreparedStatement getOtherMail = null;
    String selectUsers = "begin\n" +
        "declare @GUID nvarchar(40)\n" +
        "set @GUID=?\n" +
        "select u.USERID,isnull(u.EMAIL,'') as EMAIL,isnull(u.USERNAME,'') as USERNAME,isnull(END_DAY,30) END_DAY from Pre_Print p\n" +
        "inner join Users u on charindex(','+u.USERID+',' , ','+p.USERS+',')>0\n" +
        "where p.GUID=@GUID \n" +
        "union\n" +
        "select u.USERID,isnull(u.EMAIL,''),isnull(u.USERNAME,''),isnull(END_DAY,30) END_DAY from Pre_Print p\n" +
        "inner join RPTGROUP_U ru on charindex(ru.RG_NO,p.USGROUP)>0\n" +
        "inner join RPTGROUP_H rh on ru.RG_NO=rh.RG_NO\n" +
        "inner join Users u on ru.USERID=u.USERID\n" +
        "where p.GUID=@GUID and rh.RG_STATUS='0'\n" +
        "union\n" +
        "select '',isnull(u.E_MAIL,''),isnull(u.U_NAME,''),isnull(END_DAY,30) END_DAY from Pre_Print p\n" +
        "inner join RPTGROUP_Z u on charindex(','+u.RG_NO+',' , ','+p.USGROUP+',')>0\n" +
        "inner join RPTGROUP_H rh on u.RG_NO=rh.RG_NO\n" +
        "where p.GUID=@GUID and rh.RG_STATUS='0'\n" +
        "end";

    String sGetPrePrint = "select replace(isnull(MAILADDRS,''),'；',',') MAILADDRS,isnull(MAILSUBT,'') MAILSUBT,isnull(MAILCOTENT,'') MAILCOTENT,isnull(MAIL_TYPE,'1') MAIL_TYPE,isnull(ISMAILFILE,'1') ISMAILFILE\n"
        + "from Pre_Print \n"
        + "where GUID=?";

    emisDb oDb_ = null;
    String sMails = "";    //收件地址
    String sSubect = "";   //郵件標題
    String sContent = "";  //郵件內容
    String sMAIL_TYPE = "";//發送類型
    String sISMAILFILE = "1";//是否發送附件 1發送 0不發送

    String sSendToName = "";
    try {
      oDb_ = emisDb.getInstance(app);
      GetMail = oDb_.prepareStmt(selectUsers);
      GetMail.clearParameters();
      GetMail.setString(1, guid);
      ResultSet rs = GetMail.executeQuery();
      while (rs.next()) {
        String sUserId = rs.getString("USERID");
        String sEmail = rs.getString("EMAIL");
        int iEnd_Day = rs.getInt("END_DAY");
        String sUserName = rs.getString("USERNAME");
        if (!"".equalsIgnoreCase(sUserId))
          saveToDownLoad(path, xlsFileName, iEnd_Day, key, sUserId, name);
        if (!"".equalsIgnoreCase(sEmail)){
          sMails += sEmail + ",";
          if(!"1".equals(sMAIL_TYPE))    //非群发模式
            sSendToName += sUserName + ',';
          else if (!"".equals(sUserName))
            sSendToName += sUserName + ',';
        }
      }

      //组成文件列表
      int i = 0;
      String rpt_file_type = parameters.get("RPT_FILE_TYPE");
      if(StringUtils.isEmpty(rpt_file_type)) rpt_file_type = "xls";
      FileNameMap = new HashMap<String, String>(xlsFileName.size());
      for (Map.Entry<Integer, String> entry : xlsFileName.entrySet()) {
        i++;
        FileNameMap.put(entry.getValue()+ "." + rpt_file_type, path + "_" + i + "." + rpt_file_type);
      }
      if (!"".equals(sMails)) sMails = sMails.substring(0, sMails.length() - 1);
      if (!"".equals(sSendToName)) sSendToName = sSendToName.substring(0, sSendToName.length() - 1);
      getOtherMail = oDb_.prepareStmt(sGetPrePrint);
      getOtherMail.clearParameters();
      getOtherMail.setString(1, guid);
      ResultSet rs2 = getOtherMail.executeQuery();
      if (rs2.next()) {
        String sStr = rs2.getString("MAILADDRS");
        if (!"".equalsIgnoreCase(sStr)) {
          if (!"".equals(sMails))
            sMails = sMails + "," + sStr;
          else
            sMails = sStr;
        }

        sSubect = rs2.getString("MAILSUBT");
        sContent = rs2.getString("MAILCOTENT");
        sMAIL_TYPE = rs2.getString("MAIL_TYPE");
        sISMAILFILE = rs2.getString("ISMAILFILE");

      }
      if (!"".equals(sMails)) {
        //如果MAIL_TYPE為1 則是群發模式，否則單人一封郵件
        if ("1".equals(sMAIL_TYPE)) {
          sendForMail(parameters.get("PROP_TITLE"), sMails, sSubect, sContent, request, sISMAILFILE, sSendToName);
        } else {
          String[] mailTo = sMails.split(",");
          String[] toName = sSendToName.split(",");
          String tmpName = null;
          for (int j = 0; j < mailTo.length; j++) {
            if(toName.length > j) tmpName = toName[j];
            else tmpName = "";
            sendForMail(parameters.get("PROP_TITLE"), mailTo[j], sSubect, sContent, request, sISMAILFILE, tmpName);
          }
        }
      }
    } finally {
      if (GetMail != null) oDb_.closePrepareStmt(GetMail);
      if (getOtherMail != null) oDb_.closePrepareStmt(getOtherMail);
      if (oDb_ != null) {
        oDb_.close();
        oDb_ = null;
      }
    }

  }


  private boolean sendForMail(String Prop_Title, String SendTo, String Subject, String Content, HttpServletRequest request, String ISMAILFILE, String SendToName) {
    String sProp_title = Prop_Title;
    String sSendto = SendTo;  //郵件接收人
    String sSubject = Subject; //郵件主旨
    String sContent = Content; //郵件内容
    String sSendToName = SendToName;//接收人姓名
    emisMailNew m = null;

    try {
      if (FileNameMap.size() <= 0 || sSendto == null || sSendto.equals("")) {
        return false;
      }

      emisProp prop = emisProp.getInstance(app);
      m = new emisMailNew();


      if ("".equals(sSubject)) sSubject = getMessage("preprint", "MAILSUB") + "---" + Prop_Title;

      if ("1".equals(ISMAILFILE)) { // 将文件作为附件发送
        if ("".equals(sContent)) sContent = getMessage("preprint", "MAILCONTENT");
        for (Map.Entry<String, String> entry : FileNameMap.entrySet()) {
          m.setAffix(app.getRealPath(entry.getValue()), entry.getKey());
        }
      } else {
        String context = app.getServletContextName();
        String serverUrl = prop.get("EPOS_SERVER_NAME_OUT") + context;
        sContent += "<br/><br/>" + String.format(getMessage("preprint", "MAILCONTENT2"),serverUrl);

        for (Map.Entry<String, String> entry : FileNameMap.entrySet()) {
          sContent += "<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;● &nbsp;<a href='"+ serverUrl + "/" + entry.getValue() + "' >"+entry.getKey()+"</a>";
        }
      }
      sContent = chkKeywords(sContent, sSendToName);

      sContent +=
          "<br/>==================================" +
          "<br/>"+getMessage("preprint", "COMPANY_NAME") + prop.get("EPOS_COMPANY") +
          "<br/>"+getMessage("preprint", "SYSTEM_DESC") + prop.get("EPOS_SYSTEM_TITLE") +
          "<br/>"+getMessage("preprint", "SYSTEM_WEB_SITE") + prop.get("EPOS_SERVER_NAME_OUT") + app.getServletContextName() +
          "<br/>"+getMessage("preprint", "EXEC_PROGRAM") + this.getClass().getName();

      m.setAddress(prop.get("MAIL_SMTP_USER"), sSendto, sSubject, sContent);
      m.send(this.app);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private String chkKeywords(String Content, String SendToUserName) {
    Content = Content.replace("％date％", new SimpleDateFormat(" yyyy/MM/dd ").format(new Date()));
    Content = Content.replace("％name％", SendToUserName);
    return Content;
  }

  public static void main(String[] args) throws IOException {
    emisPrePrintUtil uu = new emisPrePrintUtil(null, null);
    System.out.println(uu.batchDownload(null, null, "", ""));

    uu.batchDownloadDelHistory("ROOT");
  }

  //可自定義
  public String xlsName;//名稱,如果為空會自動生成
  public String xlsPath = "report_out/pre_print/";//路徑
  public ServletContext app;
  public static Writer out; //仿jsp的out對像
  private Map<String, String> FileNameMap = null;
  public HashMap<String, String> parameters = null;

  /**
   * [KEYS] [nvarchar] (10),--作業代碼
   * [USERID] [nvarchar] (20),--用戶
   * [NAME] [nvarchar] (50),--預設列印名稱
   * [RECNO]   [int],--报表有分文件的个數,
   * [XLSPATH] [nvarchar] (500),--路徑
   * [XLSNAME] [nvarchar] (500),--url
   * [CRE_DATE][varchar] (8),
   * [CRE_TIME][varchar] (8)
   */
  String sqlCommand = "insert into Pre_Print_Download (KEYS,USERID,NAME,RECNO,XLSPATH,XLSNAME,ISDOWNLOAD,CRE_DATE,CRE_TIME,END_TIME) values(?,?,?,?,?,?,0,convert(varchar,dbo.GetLocalDate(),112),convert(varchar,dbo.GetLocalDate(),108),convert(nvarchar(8),dateadd(day,?,dbo.GetLocalDate()),112)+replace(convert(nvarchar(8),dbo.GetLocalDate(),108),':',''))";
  String logCommand = "insert into Pre_Print_Download_Log(USERID,XLSNAME,INFO,ISSHOW,CRE_DATE,CRE_TIME) values(?,?,?,0,convert(varchar,dbo.GetLocalDate(),112),convert(varchar,dbo.GetLocalDate(),108))";
  String log;

  class JspOut extends FileWriter {
    public JspOut(File file) throws IOException {
      super(file);
    }

    public void write(String str) throws IOException {
      //什麼都不寫,以免生成一大堆垃圾,下面的方法可以用
    }

    public void write(String str, boolean w) throws IOException {
      if (w) write(str, 0, str.length());
    }
  }

  /**
   * 取得指定多语内容
   *
   * @param request
   * @param bundle
   * @param key
   * @return
   */
  private emisLangRes _oLang = null;  // 多语资源档


  public String getMessage(String bundle, String key) {
    // 无资源档时直接返回 Key值
    if (_oLang == null) return key;

    return _oLang.getMessage(bundle, key);
  }
}
