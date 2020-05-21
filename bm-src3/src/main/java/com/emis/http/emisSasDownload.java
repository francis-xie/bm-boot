/* $Id: emisSasDownload.java 5364 2016-07-14 07:06:06Z during.liu $

 *

 * Copyright (c) EMIS Corp.
 * Track+[14307] fang 2010/01/28 sas增加???店IP功能

 */

package com.emis.http;



import com.emis.app.migration.emisMiLogToDb;
import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



/**

 * 下??案至?店.

 * <p/>

 * * ??程序:

 * 1.?Resin??成除?模式

 * 2.?行IDEA?本程式?入Remote debug??(先在download()中?中??)

 * 3.拷?要下?的?案到 c:\wwwroot\xxx\data\download\142001

 * 4.在IE网址?入:

 * http://localhost/eros/jsp/sas/sas_download_v2.jsp?CCRID=1420019&SASCMD=QUERY&COMPANYNO=00&STORENO=142001&PATH=c:\data\download

 * 5.下?完的?案?放到 c:\data\download

 *

 * @author jerry

 * @version 2005/03/07 Jerry: ?Exception加入printStackTrace()

 */

public class emisSasDownload {

  private static final String VERSION = "V2.3.2($Revision: 1591 $)";

  private static final int MAX_BUFFER_SIZE = 8192;



  // 下?的最大?案大小, ?成-1表示不限大小; Server jsp/sas.properties的MaxDownloadSize优先

  // 有???, 每??都超?10M, ?每次?一??(不管?案大小), 直到每??都?完.

  private static final int MAX_DOWNLOAD_SIZE = 10240000;  // 一次只下?最多10M的?案
  private static final int MAX_DOWNLOAD_FILES = 500;  // 一次只下载最多10M的档案

  private static final int MAX_LOG_SIZE = 512000;

  // 只保留3天的backup?案

  private static final int MAX_DAYS_TO_DELETE = 3 * 24 * 60 * 60 * 1000;

  private static final String FILESEPARATOR = emisUtil.FILESEPARATOR;  // 目?分隔符?, 正斜或反斜

  private static final String LINESEPARATOR = emisUtil.LINESEPARATOR;  // ?行符?, 0d 0a或0a



  private int iMaxDownloadSize_ = MAX_DOWNLOAD_SIZE;
  private int iMaxDownloadFiles_ = MAX_DOWNLOAD_FILES;
  private boolean isDebug_ = false;  // 能由jsp?定, 因此不能?成static

  private boolean needCashID_;

  private boolean needStoreNo_;

  private boolean needCompanyNo_;
  private boolean needDelete_;  // 由外部傳入第3個參數以決定是否可刪除來源檔案
  private PrintWriter oLogWriter_;

  private JspWriter out;

  //-private FileOutputStream oFileOutputStream_;

  private ServletContext application;

  private String companyNo;

  private String storeNo;
  private String sIP;

  private String ccrID;

  private String sasCmd;

  private String path;

  private emisFileMgr oFileMgr_;

  private emisDirectory oRootDir_;
  private String sServerIP;


  /**

   * 下??案至各?店.

   *

   * @param oContext

   * @param oWriter

   */
  public emisSasDownload(ServletContext oContext, JspWriter oWriter) {
    application = oContext;
    out = oWriter;
    needDelete_ = true;  // 設定true以與前面版本相容
  }

  public emisSasDownload(ServletContext oContext, JspWriter oWriter, boolean needDelete) {
    application = oContext;
    out = oWriter;
    needDelete_ = needDelete;
  }

  /**

   * 下??理. Entry point.

   *

   * @param request

   * @param response

   * @return

   * @throws Exception

   */

  public boolean download(HttpServletRequest request,

      HttpServletResponse response) throws Exception {


    boolean _isOK = false;

    ArrayList _oLocalFileName = new ArrayList();  // local已?存在的?案之名?

    ArrayList _oLocalFileTime = new ArrayList();  // local已?存在的?案之??

    //-FILESEPARATOR = System.getProperty("file.separator");  // 目?分隔符?, 正斜或反斜

    //-LINESEPARATOR = System.getProperty("line.separator");  // ?行符?, 0d 0a或0a



    out.println("emisSasDownload " + VERSION + " " + now());

    if (isDebug_) {

      out.println("in DEBUG mode, if files more than 100, download.zip may failed.<br>");

    }


    setLocalFile(request, _oLocalFileName, _oLocalFileTime);

    String _sPage = setCcrID(request.getParameter("CCRID"));

    if (_sPage.indexOf("error") >= 0) return false;

    setSasCmd(request.getParameter("SASCMD"));



    // ?要使用的(由local?上?的)???定好.

    setCompanyNo(request.getParameter("COMPANYNO"));

    setStoreNo(request.getParameter("STORENO"));
    setPath(request.getParameter("PATH"));
    setsIP(request.getRemoteAddr());
    saveStoreIP();//?入?店IP
    saveLastConnTime(); // 写入机台最后连线时间


    oFileMgr_ = emisFileMgr.getInstance(application);

    oRootDir_ = oFileMgr_.getDirectory("root").subDirectory("data")

        .subDirectory("download");



    // Log?案?在/data/download_log/CCRID/download.log ?案?.......

    oLogWriter_ = getLogger(oFileMgr_);



    getProperties();  // 是否使用公司, ?店或收?机


    sServerIP = request.getServerName();
    if (isDebug_) {

      out.println("<br>COMPANY NO=" + getCompanyNo());

      out.println("<br>STORE   NO=" + getStoreNo());

      out.println("<br>CCR     ID=" + getCcrID());

      out.println("<br>Data Path =" + getPath());

      out.println("<br>CashID=" + needCashID_);

      out.println("<br>StoreNo=" + needStoreNo_);

      out.println("<br>CompanyNo=" + needCompanyNo_);

    }

    // ?查?份目??的?案是否逾期, 逾期??除

    chkBackupFiles("all");

    chkBackupFiles(getStoreNo());

    chkBackupFiles(getCcrID());



    if (getSasCmd().indexOf("QUERY") >= 0) {  // 在UNIX下SasCmd?多?了?行符?, 故不用equals判?

      ArrayList _oDownloadNameList = new ArrayList();

      ArrayList _oDownloadKindList = new ArrayList();



      // Delphi?Local目?中的?案用POST的方法送上?，　?比?网路上的?案??　　　　　　　　　　　　　　　　　　　　　　　　　　　　　

      // 要下?的?案?被存入_oDownloadNameList与_oDownloadKindList.

      prepareFiles(_oDownloadNameList, _oDownloadKindList, _oLocalFileName,

          _oLocalFileTime);

      try {

        downloadFiles(response, _oDownloadNameList, _oDownloadKindList);

      } catch (Exception e) {

        e.printStackTrace(System.err);

        e.printStackTrace(oLogWriter_);

//        System.out.println("emisSasDownload.download: " + e.getMessage());

//        oLogWriter_.println("emisSasDownload.download: " + e.getMessage());

      } finally {

        oLogWriter_.close();

      }

      _isOK = true;

    }

    return _isOK;

  }



  /**

   * sas.properties可指定是否要使用公司、?店或收?机之??.

   *

   * @throws IOException

   */

  private void getProperties() throws IOException {

    FileInputStream in = null;

    try {

      String _sSize = "", _sFiles = "";

      try {

        in = new FileInputStream(application.getRealPath("/") + "jsp/sas.properties");

        Properties _oProp = new Properties();

        _oProp.load(in);

        needCashID_ = _oProp.getProperty("CashID", "0").equals("1");

        needStoreNo_ = _oProp.getProperty("StoreNo", "0").equals("1");

        needCompanyNo_ = _oProp.getProperty("CompanyNo", "0").equals("1");

        _sSize = _oProp.getProperty("MaxDownloadSize", "0");

        _sFiles = _oProp.getProperty("MaxDownloadFiles", "0");
      } catch (Exception e) {

        needCashID_ = false;

        needStoreNo_ = true;

        needCompanyNo_ = false;

      } finally {

        if (in != null) in.close();

      }



      try {

        iMaxDownloadSize_ = Integer.parseInt(_sSize);

        if (iMaxDownloadSize_ <= 0)

          iMaxDownloadSize_ = MAX_DOWNLOAD_SIZE;

      } catch (NumberFormatException e) {

        iMaxDownloadSize_ = MAX_DOWNLOAD_SIZE;

      }
    try {
      iMaxDownloadFiles_ = Integer.parseInt(_sFiles);
      if (iMaxDownloadFiles_ <= 0)
        iMaxDownloadFiles_ = MAX_DOWNLOAD_FILES;
    } catch (NumberFormatException e) {
      iMaxDownloadFiles_ = MAX_DOWNLOAD_FILES;
    }
    } catch (IOException e) {

      e.printStackTrace(System.err);

      e.printStackTrace(oLogWriter_);

      oLogWriter_.println("emisSasDownload.getProperties: " + e.getMessage());

    }

  }



  /**

   * ?回?Log的PrintWriter物件.

   *

   * @param oFileMgr

   * @return

   * @throws Exception

   */

  private PrintWriter getLogger(emisFileMgr oFileMgr) throws Exception {

    emisDirectory _oLogDir = oFileMgr.getDirectory("root").subDirectory("data")

        .subDirectory("download_log").subDirectory(getCcrID());

    emisFile _oLogFile = _oLogDir.getFile("download.log");

    if (_oLogFile.getSize() >= MAX_LOG_SIZE) {

      String _sDir = _oLogDir.getDirectory();

      if (isDebug_) out.println("Download log 超?" + MAX_LOG_SIZE + ", dir=" + _sDir);

      deleteLogBackup(_oLogDir);  // ?除逾期?份, 以防?案?多, ?大

      String _sNow = emisUtil.formatDateTime("%y%M%D-%h%m%s", new Date());

      _oLogFile.rename("download." + _sNow);



      _oLogFile = _oLogDir.getFile("download.log");

    }

    return _oLogFile.getWriter("a");

  }



  /**

   * ?定?店?上?的?案.

   *

   * @param request

   * @param oLocalFileName

   * @param oLocalFileTime

   * @throws IOException

   */

  private void setLocalFile(HttpServletRequest request, ArrayList oLocalFileName,

      ArrayList oLocalFileTime) throws IOException {

    // Delphi程式透?query.dat?????JSP.

    Enumeration n = request.getParameterNames();



    while (n.hasMoreElements()) {

      String _sName = (String) n.nextElement();

      String _sValue = trimNL(request.getParameter(_sName));



      if (isDebug_)

        out.println("Name=" + _sName + ",Value=" + _sValue + "$");

      if (_sName.indexOf("FILE") < 0) continue;  // Only need FILE1..FILE999



      StringTokenizer _oTokens = new StringTokenizer(_sValue, ",");

      String _sFileName = _oTokens.nextToken();  // Strip "c:\"

      if (FILESEPARATOR.equals("\\")) { // Only Windows do the following

        if (isDebug_) out.println("sep=" + FILESEPARATOR);

        _sFileName = _sFileName.substring(2);

      }



      _oTokens.nextToken();  // File size

      String _sFileTime = _oTokens.nextToken();  // File time

      oLocalFileName.add(_sFileName);

      oLocalFileTime.add(_sFileTime);

    }

  }



  /**

   * ?描主机目?, ?要下?的?案都存入oDownloadNameList与oDownloadKindList.

   *

   * @param oDownloadNameList

   * @param oDownloadKindList

   * @param oLocalFileName

   * @param oLocalFileTime

   * @throws Exception

   */

  private void prepareFiles(ArrayList oDownloadNameList, ArrayList oDownloadKindList,

      ArrayList oLocalFileName, ArrayList oLocalFileTime)

      throws Exception {

    ArrayList _oNameList = new ArrayList();

    ArrayList _oKindList = new ArrayList();



    // _oKindList: 下??型=COPY or MOVE

    getFiles(_oNameList, _oKindList, "all");  // 取得所有?店都要下?的?案



    String _sCompanyNo = getCompanyNo();

    if (needCompanyNo_ && _sCompanyNo != null)

      getFiles(_oNameList, _oKindList, _sCompanyNo);  // 取得符合公司之?案



    String _sStoreNo = getStoreNo();

    if (needStoreNo_ && _sStoreNo != null)

      getFiles(_oNameList, _oKindList, _sStoreNo);  // 取得某?店之下??案



    String _sCcrID = getCcrID();

    if (needCashID_)

      getFiles(_oNameList, _oKindList, _sCcrID);  // 取得某一收?机之下??案



    int j = 1;

    if (isDebug_) out.println("Files to be downloaded=" + _oNameList.size());

    boolean _isAdd;



    // ?各目?所有?案?一次，再???的Local?案?除

    // 下??型与?名之??相同; 下??型=COPY or MOVE

    Iterator _itrKind = _oKindList.iterator();

    emisFile _oServerFile = null;

    for (Iterator i = _oNameList.iterator(); i.hasNext(); j++) {

      _oServerFile = (emisFile) i.next();

      if (isDebug_) {

        Date _oDate = new Date(_oServerFile.lastModified());

        String _sDatetime = emisUtil.formatDateTime("%y/%M/%D %h:%m:%s", _oDate);

        out.println("FILE[" + j + "]=" + _oServerFile.getFullName() + "," +

            _oServerFile.getSize() + "," + _sDatetime);

      }

      // 下??型?定?COPY

      String _sTransferKind = "COPY";

      if (_itrKind.hasNext()) {

        _sTransferKind = (String) _itrKind.next();  // 取得此?案的下??型

      }

      _isAdd = true;

      // _oLocalFileName ?前端Query.dat??的?案清?.......

      int _iSize = oLocalFileName.size();

      for (int k = 0; k < _iSize; k++) {

        String _sLocalName = (String) oLocalFileName.get(k);

        _sLocalName = _sLocalName.toLowerCase();

        String _sServerFullName = _oServerFile.getFullName().toLowerCase();

        // download_datapath可能??到c:\sas\data\download..., ??的\sas\必?略去不比?

        String _sCompareName = _sLocalName.substring(_sLocalName.indexOf(FILESEPARATOR + "data"));

        if (isDebug_) {

          out.println("  full=" + _sServerFullName + ",compare=" + _sCompareName);

        }



        if (_sServerFullName.indexOf(_sCompareName) >= 0) {

          // get the file time from _oFile, the server's file

          String _sLocalTime = (String) oLocalFileTime.get(k);  // Local's file time

          //- 2002/05/20 ?案??只比到分?......

          _sLocalTime = _sLocalTime.substring(0, _sLocalTime.length() - 2) + "00";

          // "YYYY-MM-DD HH:MM:SS"

          String _sServerDate = getSDate(_oServerFile.lastModified());



          // if the filename of local and server is equal, then compare the file time.

          if (isDebug_) {

            out.println(" index[" + k + "]=" + _sServerFullName.indexOf(_sCompareName) +

                ",server=" + _sServerFullName + ",local=" + _sLocalName);

            out.println("    Client name(from query.dat)=" + _sLocalName +

                " server name=" + _sServerFullName);

            out.println("    local time=" + _sLocalTime + ",server=" + _sServerDate);

            out.println("    Is time equal? " + _sServerDate.equals(_sLocalTime));

          }



          _isAdd = false;

          // Local's file is newer than server's

          if (!_sServerDate.equals(_sLocalTime)) {

            _isAdd = true;

          }

          break;

        } // ?名符合

      } // ?理每?Local?名



      if (_isAdd) {  // Local do not have this file, so add it into the _oDownloadList

        oDownloadNameList.add(_oServerFile);

        oDownloadKindList.add(_sTransferKind);

      }

    } // ?理主机上要下?的每??案



    if (isDebug_) {  // Display the final downloadable file list

      out.println("---------------------Downloadble file list--------------------");

      j = 1;

      emisFile _oFile = null;

      for (Iterator i = oDownloadNameList.iterator(); i.hasNext(); j++) {

        _oFile = (emisFile) i.next();

        out.println("FILE" + j + "=" + _oFile.getFullName() + "," +

            _oFile.getSize() + "," + _oFile.lastModified());

      }

    }

  } // prepareFiles()



  /**

   * 由指定目?中找出?名.

   *

   * @param oNameList

   * @param oKindList

   * @param sDir

   * @throws Exception

   */

  private void getFiles(ArrayList oNameList, ArrayList oKindList,

      String sDir) throws Exception {

    // data/download/all, data/download/?店, data/download/机?
    // SME如果是按机台??生?，机台?是放在?店目?下。
    //emisDirectory _oDir = oRootDir_.subDirectory(sDir);
    emisDirectory _oDir = oRootDir_.subDirectory(this.storeNo).subDirectory(sDir);

    if (_oDir == null) {

      return;

    }

    if (isDebug_)

      out.println("<hr>Scan Directory: " + _oDir.getDirectory());



    String _sName = _oDir.getDirectory() + "file.ini";

    //-Hashtable _htTransferKind = new Hashtable();

    Properties _oProp = new Properties();

    FileInputStream _fis = null;

    try {

      _fis = new FileInputStream(_sName);

      _oProp.load(_fis);

    } catch (Exception e) {

      if (isDebug_)

        out.println("  [emisSasDownload.getFiles: open " + _sName +

            " failed");

    } finally {

      if (_fis != null) _fis.close();

    }

    Enumeration e = _oDir.getFileList();



    emisFile _oFile = null;

    while (e.hasMoreElements()) {

      _oFile = (emisFile) e.nextElement();

      String _sFileName = _oFile.getFileName();

      if (!canDownload(_oFile)) {

        continue;

      }
      // 下传档殊名称过滤，目前 暂时处理中文和空格
      downloadFileFilter(_oFile);


      String _sFullName = _oFile.getFullName();

      String _sTransferKind = getMoveOrCopy(_oProp, _sFileName, _sFullName);

      oNameList.add(_oFile);

      oKindList.add(_sTransferKind);

      if (isDebug_) {

        out.println("  FileName: " + _sFullName + " Action: " + _sTransferKind);

      }

    }

  }

  /**
   * 下传档殊名称过滤，目前 暂时处理中文和空格
   * 2014/04/03 add by Joe
   * @param oFile
   * @throws IOException
   */
  private void downloadFileFilter(emisFile oFile) throws IOException {
    String _sFileName = oFile.getFileName();
    boolean hasChinese = false;
    for (int i = 0, len = _sFileName.length(); i < len; i++) {
      // 判斷字元是否為中文
      if (Character.UnicodeBlock.of(_sFileName.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN) {
        hasChinese = true;
        break;
      }
    }
    // 当遇到中文，半形空格，全形空格都视为错误档名，直接移到error目录
    if (hasChinese || _sFileName.indexOf(" ") > 0 || _sFileName.indexOf("　") > 0) {
      try {
        emisFile localFile = oFile.getDirectory().subDirectory("error").getFile(_sFileName);
        // 存在时先删除，不然moveTo时调用renameTo会发生异常
        if(localFile.exists()){
          localFile.delete();
        }
        oFile.moveTo(oFile.getDirectory().subDirectory("error"));
      } catch (Exception e) {
        e.printStackTrace(System.err);
        e.printStackTrace(oLogWriter_);
      }
    }
  }

  /**

   * ?查?案是否需要下?.

   *

   * @param oFile

   * @return true=要下?, false=不下?

   */

  private boolean canDownload(emisFile oFile) throws IOException {

    String _sFileName = oFile.getFileName();



    // 不?理?案 file.ini

    if (_sFileName.equalsIgnoreCase("file.ini")) {

      return false;

    }

    if (_sFileName.startsWith("~")) {  // "~"??的?存?不?理.

      return false;

    }

    if (!canWrite(oFile)) {  // 能?成可??案??

      oLogWriter_.println("  cannot acess the writing file: " + oFile.getFullName());

      return false;

    }



    return true;

  }



  /**

   * 取出??模式: MOVE 或 COPY.

   *

   * @param oProp

   * @param sFileName

   * @param sFullName

   * @return String

   */

  private String getMoveOrCopy(Properties oProp, String sFileName, String sFullName) {

    String _sTransferKind = oProp.getProperty(sFileName);

    if (_sTransferKind == null) {

      if (sFullName.indexOf(FILESEPARATOR + "all") > 0)  // all=COPY, 其他=MOVE

        _sTransferKind = "COPY";

      else

        _sTransferKind = "MOVE";

    } else

      _sTransferKind = _sTransferKind.toUpperCase();

    return _sTransferKind;

  }



  /**

   * ?取要下?的?案, ?其?容?出到HTTP.

   *

   * @param response

   * @param oNameList

   * @param oKindList

   * @return int 成功下?的?案??

   * @throws Exception

   */

  private int downloadFiles(ServletResponse response,

      ArrayList oNameList, ArrayList oKindList) throws Exception {

    int j = 1;

    InputStream in = null;

    byte[] buffer = new byte[MAX_BUFFER_SIZE];



    ArrayList _alMoveFile = new ArrayList();  // 要搬移的?案

    oLogWriter_.println("--------------------");

    oLogWriter_.println("Start time: " + now() + " CCRID=" + getCcrID());

    out.print("$FILE@BEGIN$");

    String _sOutput = "", _sToNameUpper = null;
    String sDwnDate = emisUtil.todayDateAD();
    String sDwnTimeS = emisUtil.todayTimeS();
    String sDwnTimeE = "";
    ZipOutputStream zipOutputStream = null;

    try {

      zipOutputStream = new ZipOutputStream(response.getOutputStream());

      ZipEntry zipEntry = null;

      int _iTotalSize = 0;



      // HashMap用get比用iterator快.

      int _iSize = oNameList.size();

      emisFile _oFile = null;

      for (int i = 0; i < _iSize; i++) {

        _oFile = (emisFile) oNameList.get(i);

        if (_oFile == null) continue;

        if (!canWrite(_oFile)) {

          oLogWriter_.println("  cannot writes: " + _oFile.getFullName());

          continue;

        }

        _iTotalSize += _oFile.getSize();

        if (_oFile.getSize() == 0) {

          _oFile.delete();

          continue;

        }

        String _sFullName = _oFile.getFullName();

        String _sTransferKind = (String) oKindList.get(i);



        _sOutput = "";

        // /data/ or \\data\\

        int _iPos = _sFullName.indexOf(FILESEPARATOR + "data" + FILESEPARATOR);

        if (_iPos < 0) {

          out.println("File name error: " + _sFullName);

          continue;

        }

        int _iPos1 = _sFullName.indexOf(FILESEPARATOR + "data" +

            FILESEPARATOR + "download");

        String _sName = _sFullName.substring(_iPos1 + 14);

        String _sToName = getPath() + _sName;

        // TO: local, it must be Windows filename.

        _sToName = _sToName.replace('/', '\\');

        _sToNameUpper = _sToName.toUpperCase();

        if (_sToNameUpper.startsWith("C:") || _sToNameUpper.startsWith("D:")) {

          _sToName = _sToName.substring(2);  // ?去"C:"或"D:"

        }

// _sStr的格式不能修改, 否?拆????生??...........

        String _sStr = "***FILE NAME=" + _sFullName +

            " TO=" + _sToName +

            " TIME=" + getSDate(_oFile.lastModified());

        _sOutput += _sStr;



        if (_sToName.startsWith("\\")) {

          _sToName = _sToName.substring(1);

        }

        zipEntry = new ZipEntry(_sToName);  // 增加一???的?容;

        zipEntry.setTime(_oFile.lastModified());  //?定???的??,如果不?定,?以目前?????值



        zipOutputStream.putNextEntry(zipEntry);

//- zipOutputStream.write(_sStr.getBytes());



        in = _oFile.getInStream();

        int _iBytesRead;

        while ((_iBytesRead = in.read(buffer)) >= 0) {

          zipOutputStream.write(buffer, 0, _iBytesRead);

        }

        in.close();

        zipOutputStream.closeEntry();

        if (_sTransferKind.equalsIgnoreCase("MOVE")) {

          _sOutput += LINESEPARATOR + "    Move " + _oFile.getFullName();

          _alMoveFile.add(_oFile);

        }

        _sOutput += LINESEPARATOR + "  end of write: " + now() + LINESEPARATOR;

        oLogWriter_.println("  " + _sOutput);



/* ?放在all??案太大???致all??案永?不?被下?到, 因此拿掉此功能.

        if (iMaxDownloadSize_ != -1 && _iTotalSize > iMaxDownloadSize_) {

          break;  // 超?下?的最大?案大小, 跳出等下一?再?.

        }

*/
        // 目前測試極限：625個檔 72.9 MB (76,465,714 字节)，但抽樣不全，建議小於這兩個參考值
        // 注意：這邊的大小比較不含判斷前的文件，所以當有檔案大於限制時在加上它之前仍會被下載，只是後續檔案會排在下一個批次
        if (i > iMaxDownloadFiles_ || (iMaxDownloadSize_ != -1 && _iTotalSize > iMaxDownloadSize_)) {
          break;  // 超过下传的最大档案大小或檔案數, 跳出等下一轮再传.
        }
      }
      sDwnTimeE = emisUtil.todayTimeS();
    } catch (IOException e) {

      out.println("[sas_hq.jsp downloadFiles()] " + e.getMessage());

      e.printStackTrace(System.err);

      e.printStackTrace(oLogWriter_);

    } finally {

      if (in != null) in.close();

      //- 在?里就要??, 否?backup( )??入?的?容到zip?, 致解???.

      if (zipOutputStream != null) {

        try {

          if (zipOutputStream != null)

            zipOutputStream.close();

        } catch (Exception ignore) {

          // ???案要下??, 此?close??生Exception:

          // ZIP file must have at least one entry

          // java.util.zip.ZipException: ZIP file must have at least one entry

        }

      }

      // ??于MOVE的?案搬到backup目?中
      emisMiLogToDb oDwnLog = null;
      try{
        oDwnLog = new emisMiLogToDb(this.application);
        oDwnLog.setDL_DWN_D(sDwnDate);
        oDwnLog.setDL_DWN_T1(sDwnTimeS);
        oDwnLog.setDL_DWN_T2(sDwnTimeE);
        oDwnLog.setDL_S_NO(this.storeNo);
        oDwnLog.setDL_ID_NO(this.ccrID);
        oDwnLog.setDL_AP(sServerIP);

        int _iSize = _alMoveFile.size();

        emisFile _oFile = null;
        emisDirectory oBackupDir = null;
        for (int k = 0; k < _iSize; k++) {

          _oFile = (emisFile) _alMoveFile.get(k);
          oBackupDir = _oFile.getDirectory().subDirectory("backup");
          oDwnLog.setDL_FILE(_oFile.getFileName());
          oDwnLog.setDL_FILE_SIZE(_oFile.length());
          oDwnLog.setDL_FILE_BAK(oBackupDir.getDirectory());
          backup(_oFile,oBackupDir);
          oDwnLog.download();
        }
      } catch(Exception e){
        oLogWriter_.println(e);
      } finally{
        if( oDwnLog != null ){
          oDwnLog.close();
        }
      }


      oLogWriter_.println("End time: " + now() + " CCRID=" + getCcrID());

    }

    return j;

  }



  /**

   * ?去?行符?.

   *

   * @param sData

   * @return String

   */

  private String trimNL(String sData) {

    if (FILESEPARATOR.substring(0, 1).equals("/")) {

      sData = sData.substring(0, sData.length() - 2);

    } else {

      if (sData.indexOf(LINESEPARATOR) > 0) {

        sData = sData.substring(0, sData.length() - LINESEPARATOR.length());

      }

    }

    return sData;

  }



  /**

   * 取???字.

   *

   * @param iValue

   * @return String

   */

  private String getTwoDigit(int iValue) {

    return (iValue < 10 ? "0" + iValue : "" + iValue);

  }



  /**

   * ?回日期之YYYY-MM-DD HH:MM:SS 格式.

   *

   * @param lDate

   * @return String

   */

  private String getSDate(long lDate) {

    Date _oDate = new Date(lDate);

    Calendar _oCal = Calendar.getInstance();

    _oCal.setTime(_oDate);

    String _sMonth = getTwoDigit(_oCal.get(Calendar.MONTH) + 1);

    String _sDay = getTwoDigit(_oCal.get(Calendar.DAY_OF_MONTH));

    String _sHour = getTwoDigit(_oCal.get(Calendar.HOUR_OF_DAY));

    String _sMin = getTwoDigit(_oCal.get(Calendar.MINUTE));

    String _sSec = "00"; //getTwoDigit(_oCal.get(Calendar.SECOND));



    String _sDate = _oCal.get(Calendar.YEAR) + "/" + _sMonth + "/" +

        _sDay + " " + _sHour + ":" + _sMin + ":" + _sSec;

    return _sDate;

  }



  /**

   * 取公司??.

   *

   * @return String

   */

  public String getCompanyNo() {

    return companyNo;

  }



  /**

   * ?公司??.

   *

   * @param companyNo

   */

  public void setCompanyNo(String companyNo) {

    if (companyNo != null)

      this.companyNo = trimNL(companyNo);

    else

      this.companyNo = "";

  }



  /**

   * 取?店??.

   *

   * @return String

   */

  public String getStoreNo() {

    return storeNo;

  }



  /**

   * ??店??.

   *

   * @param storeNo

   */

  public void setStoreNo(String storeNo) {

    if (storeNo != null)

      this.storeNo = trimNL(storeNo);

    else

      this.storeNo = "";

  }



  /**

   * 取CCR机?.

   *

   * @return String

   */

  public String getCcrID() {

    return ccrID;

  }



  /**

   * ?CCR机?.

   *

   * @param ccrID

   * @return String

   */

  public String setCcrID(String ccrID) {

    if (ccrID != null) {

      this.ccrID = trimNL(ccrID);

      return "";

    }

    return "error.htm";

  }



  /**

   * 取??命令.

   *

   * @return String

   */

  public String getSasCmd() {

    return sasCmd;

  }



  /**

   * ???命令.

   *

   * @param sasCmd

   * @return String

   */

  public String setSasCmd(String sasCmd) {

    if (sasCmd != null) {

      this.sasCmd = trimNL(sasCmd).toUpperCase();

      return "";

    }

    return "error.htm";

  }



  /**

   * 取路?.

   *

   * @return String

   */

  public String getPath() {

    return path;

  }



  /**

   * 路??定.

   *

   * @param path

   */

  public void setPath(String path) {

    if (path == null)

      this.path = "c:\\data\\download";

    else

      this.path = trimNL(path);

  }
  /**

   * ip?定.

   *

   * @param sIP

   */

  public void setsIP(String sIP) {
    if (sIP != null)

      this.sIP = trimNL(sIP);

    else

      this.sIP = "";

  }



  /**

   * ?份?理.

   *

   * @param oFile

   * @return

   * @throws Exception

   */

  private boolean backup(emisFile oFile,emisDirectory oBackupDir) throws Exception {

    //emisDirectory _oDir = oFile.getDirectory();

    try {

      //_oDir = _oDir.subDirectory("backup");

      out.println(oBackupDir.getDirectory() + oFile.getFileName());

      oFile.copyTo(oBackupDir);

      if (needDelete_ && oBackupDir.getFile(oFile.getFileName()).exists()) {

        out.println("delete " + oFile.getFullName());

        oFile.delete();

      }
      
    } catch (Exception e) {

      out.println("err: " + e.getMessage());

      e.printStackTrace(System.err);

      e.printStackTrace(oLogWriter_);

    }

    return true;

  }



  /**

   * ?查?份?案是否逾期.

   *

   * @param sDir

   * @throws Exception

   */

  private void chkBackupFiles(String sDir) throws Exception {

    emisDirectory _oDir = oRootDir_.subDirectory(sDir).subDirectory("backup");

    File _oBackupDir = new File(_oDir.getDirectory());

    if (!_oBackupDir.exists()) {

      _oBackupDir.mkdir();

      //out.println("dir="+_oDir.getDirectory()+"\backup");

      //out.println("mkdir backup");

    }

    deleteBackup(_oDir);  // 防止backup中的?案太多了.

  }



  /**

   * ?查?份Log?案是否逾期.

   *

   * @param oDir

   * @throws Exception

   */

  private void deleteLogBackup(emisDirectory oDir) throws Exception {

    Date _oToday = new Date();

    Enumeration e = oDir.getFileList();

    long _lTime = _oToday.getTime();

    emisFile _oFile = null;

    while (e.hasMoreElements()) {

      _oFile = (emisFile) e.nextElement();

      if ((_lTime - _oFile.lastModified()) > MAX_DAYS_TO_DELETE || _oFile.getSize() == 0) {

        _oFile.delete();

      }

    }

  }



  /**

   * ?定是否要除?.

   *

   * @param isDebug

   */

  public void setDebug(boolean isDebug) {

    isDebug_ = isDebug;

  }



  /**

   * 防止backup中的?案太多了.

   */

  private void deleteBackup(emisDirectory oDir) throws Exception {

    Date _oToday = new Date();

    Enumeration e = oDir.getFileList();

    emisFile _oFile = null;

    while (e.hasMoreElements()) {

      _oFile = (emisFile) e.nextElement();

      if ((_oFile.getFileExt().endsWith("dwn") &&

          (_oToday.getTime() - _oFile.lastModified()) > MAX_DAYS_TO_DELETE) ||

          _oFile.getSize() == 0) {

        _oFile.delete();

        oLogWriter_.println("  delete backup " + _oFile.getFullName());

      }

    }

  }



  private String now() {

    return emisUtil.formatDateTime("%y/%M/%D %h:%m:%s", new Date());

  }

  /**
   * ???市IP，因有些?市是??IP。
   * add by Andy 2008/08/25
   */
  public void saveStoreIP(){
    try{
      emisProp oProp = emisProp.getInstance(application);
      // 由系???定?是否需???市IP
      if("Y".equalsIgnoreCase(oProp.get("EP_IS_SAVE_STOREIP"))){
        if(!sIP.equalsIgnoreCase((String)application.getAttribute(storeNo))) {
          application.setAttribute(storeNo,sIP);
          emisDb oDb = null;
          try{
            oDb = emisDb.getInstance(application);
            oDb.prepareStmt("update Store set S_IP = ? where S_NO = ? ");
            oDb.setString(1,sIP);
            oDb.setString(2,storeNo);
            oDb.prepareUpdate();
          } finally {
            if(oDb != null) {
              oDb.close();
              oDb = null;
            }
          }
        }
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  /**

   * ???案是否可以?入,避免?人正在?入,而我????.

   * 用setLastModified()??案存取???成?在, 若正在??, ??回false.

   * V1.0 2002/06/25 Jerry

   * V1.1 2004/06/30 Jerry

   *

   * @param oFile 要??的?名

   * @return true=可?, false=不能?

   */

  public boolean canWrite(emisFile oFile) {

    try {

      // File.canWrite()只?查?案?性, ?法在此使用.

      File _oFile = new File(oFile.getFullName());

      return _oFile.setLastModified((new Date()).getTime());

    } catch (Exception err) {

      return false;

    }

  }

  /**
   * 储存机台最后连线时间。
   * add by Joe 2015/03/06
   */
  public void saveLastConnTime() {
    try {
      emisProp oProp = emisProp.getInstance(application);
      // 由系统参数定义是否需要记录机台最后连线时间
      if ("Y".equalsIgnoreCase(oProp.get("EP_IS_SAVE_LAST_CONN_TIME", "N"))) {
        emisDb oDb = null;
        try {
          oDb = emisDb.getInstance(application);
          oDb.prepareStmt("update CASH_ID set LAST_CONN_TIME=? where ID_NO = ? and S_NO = ? ");
          oDb.setString(1, emisUtil.todayDateAD() + emisUtil.todayTimeS());
          oDb.setString(2, ccrID);
          oDb.setString(3, storeNo);
          oDb.prepareUpdate();
        } finally {
          if (oDb != null) {
            oDb.close();
            oDb = null;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

