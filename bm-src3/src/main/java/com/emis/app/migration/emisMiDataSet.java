package com.emis.app.migration;

import com.emis.file.emisFileMgr;
import com.emis.file.emisDirectory;

import java.io.IOException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: merlin
 * Date: Apr 30, 2003
 * Time: 12:00:47 PM
 */
public abstract class emisMiDataSet {
  String prefix = "";
  String timestamp = "";
  String ext = "";
  String path = "";
  String backupPath_ = "";
  String bkTimestamp = "";
  String fileName;
  String dbName = "";
  String sep = ""; // 分隔符
  String encoding = "";
  emisMiConfig config;

  String subdir = ""; //2005/05/08 andy:加入此屬性,讓前端設定,可將檔案產生在S_NO"\"+subdir下;
  boolean foridno = false; // 2008/01/10 add by chou 加入此屬性,讓前端設定可以將檔案產生些s_no/id_no(機台)下

  public void setConfig(emisMiConfig config) throws SQLException {
    this.config = config;
  }

  public abstract boolean open(emisMiConfig config) throws Exception;

  public abstract boolean close(boolean closeDb) throws IOException;

  boolean trimFields() {
    return false;
  }

  public void actOK(String[] data) throws SQLException {
  }

  void ackError(String[] data) throws SQLException {
  }

  void parse(Hashtable h) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
    Iterator it = h.keySet().iterator();
    String key;
    String val;
    while (it.hasNext()) {
      key = (String) it.next();
      val = (String) h.get(key);
      if (key.equalsIgnoreCase("path")) {
        path = getDownFilePath(val); //add by chou 修改Migration下傳時路徑只需要輸入 @\data\download即可 2008/04/14
      } else if (key.equalsIgnoreCase("remotepath")) { //多AP SERVER時，取固定AP的路徑 update by andy 2006/04/15
        path = resolveRemotePath(val);
      } else if (key.equalsIgnoreCase("backuppath")) {
        backupPath_ = val;
      } else if (key.equalsIgnoreCase("bkTimestamp")) {
        bkTimestamp = reformat(val);
      } else if (key.equalsIgnoreCase("ext")) {
        ext = val;
      } else if (key.equalsIgnoreCase("timestamp")) {
        timestamp = reformat(val);
      } else if (key.equalsIgnoreCase("prefix")) {
        prefix = val;
      } else if (key.equalsIgnoreCase("subdir")){   //2005/05/08 andy
        subdir = val ;
      } else if (key.equalsIgnoreCase("foridno")){ // add by chou 2008/01/10
        foridno = ("Y".equalsIgnoreCase(val));
      } else if (key.equalsIgnoreCase("dbname")){ // add by Andy 2008/12/10
        dbName = val;
      } else if (key.equalsIgnoreCase("sep")){ // add by Andy 2008/12/10
        sep = val;
      } else if (key.equalsIgnoreCase("encoding")){ // add by Andy 2008/12/10
        encoding = val;
      }
    }
  }

  private String reformat(String val) {
    String timestamp;
    // XML中    除了 月份 M 和分鐘 m 有大小寫之分 其他不分大小寫
    // 但java之dateformat有分 所以在此將之轉為正規的pattern
    val = val.replace('c', 'C');
    val = val.replace('D', 'd');
    val = val.replace('S', 's');    // 不允許 minisecond 的timestamp
    timestamp = val.replace('Y', 'y');
    return timestamp;
  }

  public static emisMiDataSet getInstance(String className)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    emisMiDataSet dataset;
    if (!className.startsWith("com.")) {
      className = "com.emis.app.migration." + className;
    }
    dataset = (emisMiDataSet) Class.forName(className).newInstance();
    return dataset;
  }

  String getFileName() {
    if (fileName != null && fileName.length() > 0)
      return fileName;
    String fName;
    fName = prefix;
    if (timestamp != null && timestamp.length() > 0) {
      String pattern;
      Date date = new Date();
      if (timestamp.indexOf('C') >= 0) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1911);
        date = calendar.getTime();
        pattern = timestamp.replace('C', 'y');
      } else {
        pattern = timestamp;
      }
      SimpleDateFormat dFormat = new SimpleDateFormat(pattern);
      fName = fName + dFormat.format(date);
    }
    if (ext != null && ext.length() > 0)
      fName = fName + "." + ext;
    return fName;
  }

  String getFileName(String timePattern) {
    String fName;
    fName = prefix;
    if (timePattern != null && timePattern.length() > 0) {
      String pattern;
      Date date = new Date();
      if (timePattern.indexOf('C') >= 0) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1911);
        date = calendar.getTime();
        pattern = timePattern.replace('C', 'y');
      } else {
        pattern = timePattern;
      }
      SimpleDateFormat dFormat = new SimpleDateFormat(pattern);
      fName = fName + dFormat.format(date);
    }
    if (ext != null && ext.length() > 0)
      fName = fName + "." + ext;
    return fName;
  }

  /**
   * 當指定remotepath時，表示為多AP SERVER的情況，下傳檔案應固定在一台AP 上。
   * 該AP的路徑由cfg檔中的remotedocumentroot的參數指定(\\IP\C$的格式)。
   * @param path  : 原始路徑
   * @return 轉換過的路徑
   */
  private String resolveRemotePath(String path) {
    String sRetPath = path;
    String sRelativePath = "";
    try {
      if(path.startsWith("@")){
        sRelativePath = path.substring(1);
      } else {
      if (path.indexOf(':') > 0) {  //為絕對路徑。
        sRelativePath = path.substring(2);
      }
      if (sRelativePath.indexOf(File.separator) == 0){
        sRelativePath = sRelativePath.substring(1);
      }
      }
      emisFileMgr m = emisFileMgr.getInstance(config.getContext());
      sRetPath = m.getDirectory("remoteroot").getDirectory() + sRelativePath;
    } catch (Exception e) {
      sRetPath = "";
    }
    sRetPath = sRetPath.replaceAll("\\\\","/");
    return sRetPath;
  }

  /**
   * 在下傳時將@附號替換成工程所在目錄
   * @param path
   * @return String
   */
  private String getDownFilePath(String path){
    String sTmpPath = "";
    emisDirectory oTmpDir = null;
    try{
      if (path.startsWith("@")){
        emisFileMgr m = emisFileMgr.getInstance(config.getContext());
        oTmpDir = m.getDirectory("root");
        //sTmpPath = oTmpDir.getJavaScriptDirectory() + path.replace("@","");
        //Fang.liu 2008/06/04 path.repalce("@","") jdk1.5支持
        sTmpPath = oTmpDir.getJavaScriptDirectory() + path.substring(1);
        //add by Fang.liu 2008/06/04jdk1.4、1.5都支持
        sTmpPath = sTmpPath.replaceAll("\\\\","/");
      } else {
        sTmpPath = path;
      }
    }catch(Exception e){
      sTmpPath = "";
    }
    return sTmpPath;
  }

}
