package com.emis.schedule.cleanup;

import com.emis.db.emisDb;
import com.emis.db.emisDbConnector;
import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.schedule.emisTask;
import com.emis.trace.emisTracer;
import com.emis.util.emisDate;
import com.emis.util.emisUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * $Id$
 * 定期排程清理程式：<BR>
 * 於resin\epos.cfg設定server.bindname=xxx<BR>
 * xxx = SCHED Table->S_SERVER定義名稱<BR>
 * 作業內容設定於wwwroot\epos\clean.cfg<BR>
 * 將執行程式設定於SCHED Table->S_CLASS中，以執行定期作業<BR>
 * 執行過程紀錄於wwwroot\epos\data\*.log目錄下<BR>
 */

public class emisCleanUp extends emisTask {

  public emisCleanUp() {
    super();
  }

  protected PrintWriter oPWlog_;
  protected emisFileMgr oFMgr_;
  protected emisDirectory oDirRoot_, oDirCleanLog_;

  // 清理檔案起始及結束期間
  protected long lStartTime_, lEndTime_;

  // 作業名稱不得定義為'0'
  final int CLEAN_TABLE = 1;
  final int CLEAN_LOGS = 2;

  /**
   * run()<BR>
   * <p>
   * <p>
   * <p>
   * 清理程式主程式<BR>
   */
  public void runTask() throws Exception {
    lStartTime_ = System.currentTimeMillis();
    lEndTime_ = 0;

    // 取Log檔之存放目錄($root/data/clean/*.log)
    try {
      oFMgr_ = emisFileMgr.getInstance(oContext_);  //來自emisTask之ServletContext
      oDirRoot_ = oFMgr_.getFactory().getDirectory("root");
      oDirCleanLog_ = oDirRoot_.subDirectory("data").subDirectory("clean");
    } catch (Exception e) {
      emisTracer.get(oContext_).warning(this, e);
      return;
    }

    // 將clean.cfg內容取入_oProp
    Properties _oProp = null;
    InputStream _oIn = null;
    String _sCleanCfg = oDirRoot_.getDirectory() + "/clean.cfg";
    try {
      _oIn = new FileInputStream(_sCleanCfg);
      _oProp = new Properties();
      _oProp.load(_oIn);
    } catch (Exception err) {
      emisTracer.get(oContext_).warning(this, err.getMessage());
      //throw new IOException("Can't find clean config file:"+_sCleanCfg);
      return;
    } finally {
      try {
        if (_oIn != null)
          _oIn.close();
      } catch (Exception err) {
      }
    }

    // 最多可處理99個clean作業(clean.1,clean.2...,clean.99)
    for (int i = 1; i < 100; i++) {
      Properties _oSetSysProp = emisUtil.subProperties("clean." + Integer.toString(i) + ".", _oProp);
      if (_oSetSysProp.isEmpty()) {
        //break;
        continue; // 不一定要按顺序设定
      }
      Hashtable _htClean = new Hashtable();
      Enumeration e = _oSetSysProp.keys();
      int _iMode = 0;
      while (e.hasMoreElements()) {
        Object key = e.nextElement();
        String _key = (String) key;
        _key = _key.toLowerCase();
        if ("table".equals(_key)) {
          _iMode = CLEAN_TABLE;
        }
        if ("log.dir".equals(_key)) {
          _iMode = CLEAN_LOGS;
        }
        // 將每一個作業的參數資料存入Hashtable
        _htClean.put(_key, _oSetSysProp.get(key));
      }
      processCleanCfg(_htClean, _iMode);
    }
  }

  /**
   * processCleanCfg()<BR>
   * <p>
   * <p>
   * <p>
   * 處理每個輸入的 clean.?.xxx 要求<BR>
   */
  private void processCleanCfg(Hashtable htClean, int iMode) {
    switch (iMode) {
      case CLEAN_TABLE:
        processCleanTable(htClean);
        break;
      case CLEAN_LOGS:
        //processCleanLogs(htClean);
        processCleanFiles(htClean);
        break;
    }
  }

  /**
   * processCleanTable()<BR>
   * <p>
   * <p>
   * <p>
   * 預先處理各參數，call cleanTable()<BR>
   */
  private void processCleanTable(Hashtable htClean) {
    String _sTable = (String) htClean.get("table");
    String _sReserve = (String) htClean.get("reserve.days");
    String _sField = (String) htClean.get("field");
    String sStartSNO_ = emisUtil.parseString((String) htClean.get("start.store"));
    String sEndSNO_ = emisUtil.parseString((String) htClean.get("end.store"));

    try {
      emisFile _oFilog = oDirCleanLog_.getFile(_sTable + emisUtil.todayDate().substring(0, 5) + ".log");
      oPWlog_ = _oFilog.getWriter("A"); // open in append mode

      // 假如門市編號前後順序顛倒，則互換之
      if (sStartSNO_.compareTo(sEndSNO_) < 0) {
        String tmp = sStartSNO_;
        sStartSNO_ = sEndSNO_;
        sEndSNO_ = tmp;
      }

      // 如果沒有指定reserve days，則從EPOS_CLEANCYCLE拿取。
      if ((_sReserve == null) || ("".equals(_sReserve))) {
        _sReserve = emisProp.getInstance(oContext_).get("EPOS_CLEANCYCLE");
      }
      int _iCycle = 90;  // 預設值為90天
      if (_sReserve != null && !("".equals(_sReserve))) {
        _iCycle = Integer.parseInt(_sReserve);
      }
      _iCycle = 0 - _iCycle; // 取負值
      String _sDelDate = (new emisDate()).addDay(_iCycle).toString();
      cleanTable(_sTable, _sField, sStartSNO_, sEndSNO_, _sDelDate);

      oPWlog_.flush();
    } catch (Exception err) {
      logErr(this, err);
    } finally {
      oPWlog_.close();
    }
  }

  /**
   * cleanTable()<BR>
   * <p>
   * <p>
   * <p>
   * 刪除SQL sTable所有日期在sDelDate之前之門市資料<BR>
   * <p>
   * sTable必須具有S_NO欄位儲存門市代號<BR>
   *
   * @param sTable    - SQL Table name<BR>
   *                  <p>
   * @param sField    - SQL Table的日期欄位<BR>
   *                  <p>
   * @param sStartSNo - 起始門市區間<BR>
   * @param sEndSNo   - 終止門市區間<BR>
   *                  <p>
   * @param sDelDate  - 刪除之日期期限<BR>
   *                  <p>
   *                  每次的執行結果紀錄於$root/data/clean下，依sTable+YYYMM.LOG命名<BR>
   */
  private void cleanTable(String sTable, String sField, String sStartSNo, String sEndSNo, String sDelDate) {
    // LOG file (YYYMM.LOG)儲存至 $root/data/clean目錄下
    log("=================================================================");
    log("起始時間：" + emisUtil.now());
    log("處理門市：" + sStartSNo + " 至：" + sEndSNo);
    log("清理檔案：" + sTable + " 日期欄位：" + sField);
    log("刪除區間：" + sDelDate + "日前之資料");

    if ((sField == null) || ("".equals(sTable)) || ("".equals(sField))) {
      log("\n檔案或欄位參數輸入錯誤！");
      return;
    }

    try {
      emisDb oDb = null;
      // 刪除資料
      try {
        oDb = emisDb.getInstance(oContext_);
        log("使用連線：" + oDb.getConnectionIdentifier());
        oDb.setEncodingTransferMode(emisDbConnector.TRANSFER_NONE);
        oDb.setDescription(this.getName());

        log("執行指令：DELETE FROM " + sTable + " WHERE " + sField + " <= " + sDelDate + " AND S_NO >= " + sStartSNo + " AND S_NO <= " + sEndSNo);
        oDb.prepareStmt("DELETE FROM " + sTable + " WHERE " + sField + " <= ? "
            + (sStartSNo != null && !"".equals(sStartSNo.trim()) ? " AND S_NO >= ? " : "")
            + (sEndSNo != null && !"".equals(sEndSNo.trim()) ? " AND S_NO <= ? " : ""));
        int i = 1;
        oDb.setString(i++, sDelDate);
        if (sStartSNo != null && !"".equals(sStartSNo.trim())) {
          oDb.setString(i++, sStartSNo);
        }
        if (sEndSNo != null && !"".equals(sEndSNo.trim())) {
          oDb.setString(i, sEndSNo);
        }
        oDb.prepareUpdate();

        lEndTime_ = System.currentTimeMillis();
        log("處理時間：" + (lEndTime_ - lStartTime_) + " miliseconds");
      } catch (Exception e) {
        logErr(this, e);
      } finally {
        if (oDb != null) {
          oDb.close();
        }
      }
    } catch (Exception e) {
      logErr(this, e);
    }
  }

  /**
   * processCleanLogs()<BR>
   * <p>
   * <p>
   * <p>
   * 預先處理各參數，call cleanLogs()<BR>
   */

  private void processCleanLogs(Hashtable htClean) {
    // 如果sDirLogs == "" 表示清除根目錄($root)下之log files
    String _sDirLogs = (String) htClean.get("log.dir");
    _sDirLogs = emisUtil.stringReplace(_sDirLogs, ".", "/", "a");
    String _sReserve = (String) htClean.get("reserve.days");

    try {
      emisFile _oFilog = oDirCleanLog_.getFile(emisUtil.todayDate().substring(0, 5) + "clean.log");
      oPWlog_ = _oFilog.getWriter("A"); // open in append mode

      // 如果沒有指定reserve days，則從EPOS_CLEANCYCLE拿取。
      if ((_sReserve == null) || ("".equals(_sReserve))) {
        _sReserve = emisProp.getInstance(oContext_).get("EPOS_CLEANCYCLE");
      }

      int _iCycle = 90;  // 預設值為90天
      if (_sReserve != null && !("".equals(_sReserve))) {
        _iCycle = Integer.parseInt(_sReserve);
      }
      _iCycle = 0 - _iCycle; // 取負值

      Calendar _oCalendar = emisUtil.getLocaleCalendar();
      _oCalendar.add(Calendar.DATE, _iCycle);

      String _sDelDate = emisUtil.getYearS(_oCalendar) +
          emisUtil.getMonthS(_oCalendar) +
          emisUtil.getDateS(_oCalendar);

      emisDirectory _dir_log = oDirRoot_.subDirectory(_sDirLogs);
      cleanLogs(_dir_log, _sDelDate);

      oPWlog_.flush();
    } catch (Exception err) {
      logErr(this, err);
    } finally {
      oPWlog_.close();
    }
  }


  /**
   * cleanLogs()<BR>
   * <p>
   * <p>
   * <p>
   * 刪除在sDirLogs目錄下所有日期在sDelDate之前之.log檔案<BR>
   *
   * @param sDirLogs - 預刪除log之目錄<BR>
   *                 <p>
   *                 　　　　　String sDelDate - 刪除之日期期限<BR>
   *                 <p>
   *                 每次的執行結果紀錄於$root/data/clean下，依sYYYMMclean.LOG命名
   */
  private void cleanLogs(emisDirectory sDirLogs, String sDelDate) {
    // LOG file (YYYMMclean.LOG)儲存至 $root/data/clean目錄下
    log("=================================================================");
    log("起始時間：" + emisUtil.now());
    log("清理目錄：" + sDirLogs.getDirectory());
    log("刪除區間：" + sDelDate + "日前之*.log檔案");

    try {
      emisDate _oD = new emisDate(sDelDate);
      long _lDelDate = _oD.getTime();

      Enumeration e = sDirLogs.getFileList();
      while (e.hasMoreElements()) {
        emisFile _sFile = (emisFile) e.nextElement();
        if ("log".equalsIgnoreCase(_sFile.getFileExt()) && (_sFile.lastModified() < _lDelDate)) {
          //log("刪除"+_sFile.getFileName());
          _sFile.delete();
        }
      }
      lEndTime_ = System.currentTimeMillis();
      log("處理時間：" + (lEndTime_ - lStartTime_) + " miliseconds");
    } catch (Exception e) {
      logErr(this, e);
    }
  }

  /**
   * log(String sStr)<BR>
   * <p>
   * 將字串寫入LOG file<BR>
   */
  private void log(String sStr) {
    oPWlog_.println(sStr);
  }

  /**
   * logErr(Object source , Exception e)<BR>
   * <p>
   * 儲存系統錯誤訊息至LOG file<BR>
   */
  private void logErr(Object source, Exception e) {
    oPWlog_.println(source.toString());
    e.printStackTrace(oPWlog_);
  }

  private void processCleanFiles(Hashtable htClean) {
    String logDir = (String) htClean.get("log.dir");
    String reserve = (String) htClean.get("reserve.days");
    String exclude = (String) htClean.get("exclude.subdir");
    String prefix = (String) htClean.get("filename.prefix");
    String postfix = (String) htClean.get("filename.postfix");
    try {
      logDir = emisUtil.stringReplace(logDir, "%user.dir%", System.getProperty("user.dir"), "a");
      //logDir = emisUtil.stringReplace(logDir, ".", "/", "a");
      if (logDir.startsWith("@")) {
        logDir = oDirRoot_.subDirectory(logDir.substring(1)).getDirectory();
      }
      exclude = (exclude != null && !"".equals(exclude)) ? ";" + exclude + ";" : "";
      prefix = (prefix != null && !"".equals(prefix)) ? prefix.toLowerCase() : "";
      postfix = (postfix != null && !"".equals(postfix)) ? postfix.toLowerCase() : "";

      emisFile _oFilog = oDirCleanLog_.getFile("clean." + emisUtil.todayDateAD("-") + ".log");

      oPWlog_ = _oFilog.getWriter("A"); // open in append mode
      // 如果沒有指定reserve days，則從EPOS_CLEANCYCLE拿取。
      if ((reserve == null) || ("".equals(reserve))) {
        reserve = emisProp.getInstance(oContext_).get("EPOS_CLEANCYCLE");
      }
      int _iCycle = 90;  // 預設值為90天
      if (reserve != null && !("".equals(reserve))) {
        _iCycle = Integer.parseInt(reserve);
      }
      _iCycle = 0 - _iCycle; // 取負值

      String delDate = new emisDate().add(_iCycle).toString();
      cleanFiles(logDir, delDate, exclude, prefix.split(";"), postfix.split(";"));
      oPWlog_.flush();
    } catch (Exception err) {
      logErr(this, err);
    } finally {
      oPWlog_.close();
    }
  }

  private void cleanFiles(String dir, String delDate, String exclude, String[] prefix, String[] postfix) {
    String name = "";
    File fDir = new File(dir);
    if (!fDir.exists()) return; //指定目录不存，不继续往下处理。

    for (File f : fDir.listFiles()) {
      name = f.getName();
      if (f.isDirectory()) {
        // 如果目录为设定的排除目录则不作处理
        if (exclude.indexOf(";" + name + ";") >= 0) continue;
        else cleanFiles(f.getPath(), delDate, exclude, prefix, postfix);

        if (new File(f.getPath()).listFiles().length == 0) { // 子目录下为空时，将目录也删除掉
          if (f.delete()) {
            log("D  [" + emisUtil.todayDateAD("-") + " " + emisUtil.todayTimeS(true) + "] " + f.getPath());
          }
        }
      } else {
        String fileDate = emisUtil.formatDateTime("%y%M%D", new Date(f.lastModified()));
        if (isFilter(name, prefix, postfix) && (delDate.compareTo(fileDate) >= 0)) {
          if (f.delete()) {
            log("D  [" + emisUtil.todayDateAD("-") + " " + emisUtil.todayTimeS(true) + "] " + f.getPath());
          }
        }
      }
    }
  }

  private boolean isFilter(String filename, String[] prefix, String[] postfix) {
    filename = filename.toLowerCase();

    boolean b1 = false;
    if (prefix.length == 0) b1 = true;
    else {
      for (String pre : prefix) {
        if (filename.startsWith(pre)) {
          b1 = true;
          break;
        }
      }
    }

    boolean b2 = false;
    if (postfix.length == 0) b2 = true;
    else {
      for (String post : postfix) {
        if (filename.endsWith(post)) {
          b2 = true;
          break;
        }
      }
    }
    return b1 && b2;
  }
}