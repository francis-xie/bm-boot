package com.emis.app.migration;



import com.emis.db.emisDb;

import com.emis.file.emisDirectory;

import com.emis.file.emisFileMgr;

import com.emis.schedule.emisTask;

import com.emis.util.emisUtil;

import com.emis.user.emisUser;



import javax.servlet.ServletContext;

import java.io.*;

import java.lang.reflect.Field;

import java.net.MalformedURLException;

import java.net.URL;

import java.security.CodeSource;

import java.security.ProtectionDomain;

import java.sql.SQLException;

import java.util.*;


/**
 * $Id: emisMigration.java 11910 2018-07-18 06:12:48Z andy.he $
 */
public   class emisMigration extends emisTask {

  private PrintWriter oLogWriter_;

  private String logFileName_ = null;

  private int FailCount = 0, OKCount = 0;



  public   int getOKCount() {

    return OKCount;

  }



  public   int getFailCount() {

    return FailCount;

  }



  private Date startTime_;

  private String taskName_;

  private emisMiConfig miConfig_;

  private emisMiSource source_;

  private emisMiTarget target_;

  private emisMiConverter converter_;

  private emisDb miDb = null;   // 用來串多個 Migration的 db, 若此db不為null則不可close db

  private String TargetStore[];  //  used by creation rule

  private String SourcePath;

  private boolean logToScreen = false;

  private String fileName = null;



  //2005/05/12 add by abel start

  public emisUser getUser() {

      return oUser_;

  }

  public String getUserName(){
    if(oUser_ != null) {
      return oUser_.getName();
    } else {
      return "自動排程";
    }
  }


  public void setUser(emisUser oUser) {

      this.oUser_ = oUser;

  }



  //傳入 user 物件

  public emisMigration(ServletContext context, String taskName, emisUser oUser) {

     this();

     this.taskName_ = taskName;

     this.oUser_ = oUser;

     oContext_ = context;

     logToScreen = true;

   }

  //2005/05/12 abel end



  public   String getFileName() {

    return fileName;

  }



  public   void setFileName(  String fileName) {

    this.fileName = fileName;

  }



  public   String[] getTargetStore() {

    return TargetStore;

  }



  public   void setSourcePath(  String path) {

    SourcePath = path;

  }



  public   String getSourcePath() {

    return SourcePath;

  }



  public   boolean setTagProperty(  String tag,   String key,   String value) {

    return true;

  }



  public   void setTargetStore(  String[] targetStore) {

    TargetStore = targetStore;

  }



  public   void setMiDb(  emisDb miDb) {  // 只能在run之前設定

    this.miDb = miDb;

  }



  public emisMigration() {

    super();

  }



  public   boolean excuteConvertion() throws Exception { //throws Exception {

    if (target_.getCreationRule() == null)

      return simpleConvertion();

    else

      return groupConvertion();

  }



  public boolean groupConvertion() throws Exception { // throws Exception {

    String[] sKey = null;

    String[] cKey = target_.getCreationKey();

    if (cKey == null)

      cKey = new String[0];

      int[] kIndex = new int[cKey.length];

    for (int i = 0; i < cKey.length; i++) {

      if ((kIndex[i] = miConfig_.sourceFieldIndex(cKey[i])) == -1)

        return false;

    }

    String[] data = null;   //賦一個null值,初始化 update by andy 2005/11/10

    boolean keyChanged;

    try {

      while ((data = source_.next()) != null) {

        if (sKey == null) {

          sKey = new String[kIndex.length];

          for (int i = 0; i < kIndex.length; i++) {

            sKey[i] = data[kIndex[i]];

          }

        } else {

          keyChanged = false;

          for (int i = 0; !keyChanged && i < kIndex.length; i++) {

            keyChanged = !data[kIndex[i]].equals(sKey[i]);

          }

          if (keyChanged) {

            target_.cloneByKey(sKey, true);

            for (int i = 0; i < kIndex.length; i++) {

              sKey[i] = data[kIndex[i]];

            }

          }

        }

        String[] result = converter_.convert(data);

        if (result != null) {

          if (target_.write(result)) {

            source_.actOK(data);

            target_.actOK(result);

          }

          this.OKCount++;

        }

      }

    } catch (Exception e) {

      printLog("[" + emisUtil.now() + "]" + "..Exception in Migration : " + this.taskName_);
      
      printLog(e);

    }

    target_.cloneByKey(sKey, false);

    if (OKCount > 0) {        // [4463] by Merlin BEGIN 更新到 data source 以免最後一筆重複產生

      source_.actOK(data);

    }                         // [4463] by Merlin END

    return true;

  }



  public   boolean simpleConvertion() throws SQLException { //throws Exception {

    String[] data = null;

    String[] result = null;

    do {

      try {

        data = source_.next();

        if (data != null) {

          result = converter_.convert(data);

          if (result != null) {

            if (target_.write(result)) {

              source_.actOK(data);

              target_.actOK(result);

              this.OKCount++;

            }

          }

        }

      } catch (Exception e) {

        printLog("[" + emisUtil.now() + "]" + "..Exception in Migration : " + this.taskName_);

        oLogWriter_.println("Source:");

//        printAry(data);

//        if (result != null) {

//          oLogWriter_.println("Target:");

//          printAry(result);

//        }

        e.printStackTrace(this.oLogWriter_);  //To change body of catch statement use Options | File Templates.

        this.FailCount++;

        if (result != null)

          target_.ackError(result);

      }

    } while (data != null);

    if (OKCount > 0) {

      source_.actOK(data);

    }

    return true;

  }



  private void printAry(  String[] data) {

    for (int i = 0; i < data.length; i++)

      oLogWriter_.print(data[i]);

    oLogWriter_.println("");

  }



  public emisMigration(  ServletContext context) {

    this();

    oContext_ = context;

  }



  public emisMigration(  ServletContext context,   String taskName) {

    this();

    this.taskName_ = taskName;

    oContext_ = context;

    logToScreen = true;

  }



  public   emisMiConfig getConfig() {

    return miConfig_;

  }



  public   void setConfig(  String miName,   ServletContext context) throws Exception {

      emisMiConfig miConfig;

    if (miDb == null)

      miConfig = new emisMiConfig(miName, context);

    else

      miConfig = new emisMiConfig(miName, context, miDb);

    this.miConfig_ = miConfig;

    miConfig_.setMigration(this);

  }



  protected   void initPath() throws Exception {

    emisDirectory _oTempDir;

      emisFileMgr _oFileMgr = emisFileMgr.getInstance(oContext_);

    _oTempDir = _oFileMgr.getDirectory("root");

      String _logPath;

    if ((_logPath = miConfig_.getLogPath()) != null) {

        StringTokenizer st = new StringTokenizer(_logPath, File.separator);

      while (st.hasMoreTokens()) {

        _oTempDir = _oTempDir.subDirectory(st.nextToken());

      }

    } else {

      _oTempDir = _oTempDir.subDirectory("data").subDirectory("sas_log");

    }

    if (!logToScreen)

      logFileName_ = _oTempDir.getDirectory() + taskName_ + "@" + this.getClass().getName() + ".log";

  }



  protected   boolean prepareLogFileSucess() throws Exception {

      File oLogFile_;

    boolean bRetVal = true;

    initPath();

    if (!logToScreen) {

      oLogFile_ = new File(logFileName_);

      if (!oLogFile_.exists()) {

        oLogFile_.createNewFile();

      } else if (!oLogFile_.canWrite()) {

        bRetVal = false;

      }

      if (bRetVal) {

        oLogWriter_ = new PrintWriter(new BufferedWriter(new FileWriter(logFileName_, true)));

      }

    } else

      oLogWriter_ = new PrintWriter(new OutputStreamWriter(System.out));

    return bRetVal;

  }



  protected   void printLog(  String sLogString) {

    try {

      if (oLogWriter_ != null)

        oLogWriter_.println(sLogString);

      oLogWriter_.flush();

    } catch (Exception oe) {

    	oe.printStackTrace();

    }

  }
  protected   void printLog(  Exception e ) {

	    try {

	      if (oLogWriter_ != null)
	    	  e.printStackTrace(oLogWriter_);


	    } catch (Exception ignore) {


	    }

  }


  public   void runTask() throws Exception {

    this.startTime_ = emisUtil.now();

    // 只要this.getParam() 不为空时则taskNmae_都取this.getParam(), 这样可以只用实例化一个对象,而可以通过指定param来执行不同的资料下传.
    if (taskName_ == null || (this.getParam() != null && !"".equals(this.getParam())))
      taskName_ = this.getParam();

    System.out.println("[" + emisUtil.now() + "]" + "  running " + taskName_);

    try {
      // 手动排程执行下传时可以指定门店下传
      if(this.oRequest_ != null && this.oRequest_.getParameter("S_NO") != null && !"".equals(this.oRequest_.getParameter("S_NO"))){
        this.setTargetStore(this.oRequest_.getParameter("S_NO").split("\\,"));
      }
      setConfig(taskName_, this.oContext_);

      if (prepareLogFileSucess()) {

        printLog("[" + emisUtil.now() + "]" + "..開始準備資料");

        prepareConfig();

        if (source_ == null) {

          printLog("[" + emisUtil.now() + "]" + "..來源資料準備失敗");

          if (target_ != null)

            target_.close(true);

          return;

        }

        if (target_ == null) {

          printLog("[" + emisUtil.now() + "]" + "..目標資料準備失敗");

          if (source_ != null)

            source_.close(true);

          return;

        }
        // robert, 2010/03/02 put log object to target
        // this is for emisMiTextTarget.doCheck
        target_.oLogWriter_ = this.oLogWriter_;
        

//        if (fileName != null)
//
//          source_.setFileName(fileName);

        if (!source_.open(miConfig_))

          return;

        if (!target_.open(miConfig_))

          return;

        printLog("[" + emisUtil.now() + "]" + "..資料準備完成");

        excuteConvertion();

        printLog("[" + emisUtil.now() + "]  " + taskName_ + "..作業完畢...");

         //Track+[15647] dana.gao 2010/08/24 下傳完成后刪除多餘的無用文件

        deleteUnuseFile();

        source_.close(false);

        if (this.miDb == null) {

          target_.close(true);

          source_.backup();

        } else {

          target_.close(false);
        }

        //  backup will delete the generated text file, it is needed for groupConvertion
        // but for simple convertion the result will lost
        if  ( target_.creationRule != null ||  (target_.backupPath_!=null && target_.backupPath_.trim().length()>0) ) {
        	target_.clearTemp();
        }



      } // end of prepareLogFileSucess

    } catch (Exception e1) {

      printLog(e1);

    } finally {

      if (miDb == null)
        miConfig_.releaseDb();
      
      if (!logToScreen) {
		if( oLogWriter_ != null ) {
			oLogWriter_.close();
		}
	  } else {
		  // log to screen , we free the pointer
		  oLogWriter_ = null;
	  }

    }

  }
  //Track+[15647] dana.gao 2010/08/24 下傳完成后刪除多餘的無用文件
  private void deleteUnuseFile() {
    try {
      File director = new File(target_.path);
      File[] fileList = director.listFiles();
      for (File f : fileList) {
        if (f.isFile()) {
          f.delete();
        }
      }
    } catch (Exception e) {

    }
  }


  protected   boolean prepareConfig() {

    source_ = miConfig_.getSource();

    target_ = miConfig_.getTarget();

    converter_ = miConfig_.getConverter();

    return true;

  }

//    protected void setLogger(Writer writer) {

//        logger = writer;

//    }

}



