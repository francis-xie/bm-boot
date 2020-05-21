/* $Id: emisSasUpload.java 4 2015-05-27 08:13:47Z andy.he $

 *

 * Copyright (c) EMIS Corp. All Rights Reserved.

 */

package com.emis.http;



import com.emis.db.emisProp;

import com.emis.file.emisDirectory;

import com.emis.file.emisFile;

import com.emis.file.emisFileMgr;

import com.emis.util.emisLogger;

import com.emis.util.emisUtil;
import com.emis.util.emisUploadLogH;

import org.apache.log4j.Logger;

import org.apache.log4j.PropertyConfigurator;



import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;

import javax.servlet.jsp.JspWriter;

import java.util.*;

import java.util.Properties;

import java.util.StringTokenizer;
import java.io.File;


/**
 * $Id: emisSasUpload.java 4 2015-05-27 08:13:47Z andy.he $
 * SAS_Upload.

 * 2004/05/24 Jerry: 由sas_upload_hq.jsp中移出主要功能.

 * 2004/07/28 Jerry: 修改成Log4J機制

 * * 測試程序:

 * 1.將Resin啟動成除錯模式

 * 2.執行IDEA讓本程式進入Remote debug狀態(先在download()中設中斷點)

 * 3.拷貝要下傳的檔案到 c:\wwwroot\xxx\data\download\142001

 * 4.在IE網址輸入:

 * http://localhost/eros/jsp/sas/sas_download_v2.jsp?CCRID=1420019&SASCMD=QUERY&COMPANYNO=00&STORENO=142001&PATH=c:\data\download

 * 5.下傳完的檔案會放到 c:\data\download

 */

public class emisSasUpload {

  private static String VERSION = "V2.2.0f";

//  private static int MAX_BUFFER_SIZE = 8192;

//  private Logger oLogger_;

  private ServletContext application;

  private JspWriter out;

  private HttpSession session;

  private HttpServletRequest request;

  private HttpServletResponse response;

  private emisDirectory oRootDir_;

  private emisDirectory oUploadDir_;

  private emisFileMgr oFileMgr_;

  private String sUploadDir_;

  private boolean isDebug_;

  //private HashMap hmProp_;

  Logger oLogger_;



  /**

   * 上傳檔案接收處理.

   *

   * @param oContext  ServletContext

   * @param oSession  Session

   * @param oRequest  request

   * @param oResponse response

   * @param oWriter   output

   */

  public emisSasUpload(final ServletContext oContext, HttpSession oSession,

      HttpServletRequest oRequest, final HttpServletResponse oResponse,

      final JspWriter oWriter) {

    application = oContext;

    out = oWriter;

    request = oRequest;

    response = oResponse;

    session = oSession;

    sUploadDir_ = "data/upload";
    
    //dana 2011/07/12    
    try {      
      oFileMgr_ = emisFileMgr.getInstance(application);      
      oRootDir_ = oFileMgr_.getDirectory("root");    
    } catch (Exception e) {}

    try {
    	oLogger_  = emisLogger.getlog4j(application, this.getClass().getName());
    } catch (Exception e) {}

  }



  /**

   * 傳回上傳目錄.

   *

   * @return

   */

  private emisDirectory getUploadDirectory() {
    return oRootDir_.subDirectory(sUploadDir_);

  }



  /**

   * 設目錄.

   *

   * @param sDir

   * @throws Exception

   */

  public void setUploadDirectory(String sDir) throws Exception {

    sUploadDir_ = sDir;
    oUploadDir_ = getUploadDirectory();

  }



  /**

   * 取傳檔主機檔名字串.

   *

   * @param sFilename

   * @return

   */

  private String getDataServerDirectory(String sFilename) {

    int _iIndex = sFilename.indexOf("data/upload");

    if (_iIndex > 0) {

      sFilename = sFilename.substring(_iIndex);

      _iIndex = sFilename.lastIndexOf("\\");

      sFilename = sFilename.substring(0, _iIndex);

    }

    sFilename = emisUtil.stringReplace(sFilename, "\\", "/", "a");

    return sFilename;

  }



  /**

   * 是否為除錯模式?

   *

   * @return

   */

  public boolean isDebug() {

    return isDebug_;

  }



  /**

   * 設成除錯模式.

   *

   * @param debug_

   */

  public void setDebug(final boolean debug_) {

    isDebug_ = debug_;

  }



  /**

   * 上傳接收處理.

   *

   * @throws Exception

   */

  public void upload() throws Exception {

    //oLogger_ = Logger.getLogger("emisSasUpload");

    //hmProp_ = new HashMap();

    //hmProp_.put("%MAXSIZE%", "1024KB");

    //hmProp_.put("%MAXFILES%", "3");

    //hmProp_.put("%LEVEL%", "DEBUG");

    //hmProp_.put("%APPENDER%", "A2");

    //_hmProp.put("%LOGFILE%", "test.log");



    oFileMgr_ = emisFileMgr.getInstance(application);

    oRootDir_ = oFileMgr_.getDirectory("root");

    oUploadDir_ = getUploadDirectory();

    if (oRootDir_ == null) {

      return;

    }

    String _sDir = oUploadDir_.getDirectory();  // 最後會附加back slash
    String sSysDate = emisUtil.todayDateAD();
    String sSysTimeS = emisUtil.todayTimeS();
    String sSysTimeE = "";
    if (isDebug_)

      out.println("emisSasUpload: upload dir=" + _sDir);



    String _sOutput = "Start time: " + emisUtil.now();

    out.println(_sOutput);

    int count = 0;



    final emisHttpTransfer myTransfer = new emisHttpTransfer();

    // Initialization

    myTransfer.initialize(application, session, request, response, out);



    // Upload

    myTransfer.upload();  // 將request內容存到myTransfer內部

    boolean _isOK = true;

    try {

      out.println("  [" + emisUtil.now() + "] Start to receiving zip files...");



      for (int i = 0; i < myTransfer.getFiles().getCount(); i++) {

        // Retreive the current file

        final emisHttpFile myFile = myTransfer.getFiles().getFile(i);

        // Save it only if this file exists

        if (!myFile.isMissing()) {

          // Save the files with its original names in a virtual path of the web server

          myFile.saveAs(_sDir + myFile.getFileName());

          _sOutput = "  FieldName=" + myFile.getFieldName();

          _sOutput += "\n  Size=" + myFile.getSize();

          _sOutput += "\n  FileName=" + myFile.getFileName();

          _sOutput += "\n  FileExt=" + myFile.getFileExt();

          _sOutput += "\n  FilePathName=" + myFile.getFilePathName();

          _sOutput += "\n  ContentType=" + myFile.getContentType();

          _sOutput += "\n  ContentDisp=" + myFile.getContentDisp();

          _sOutput += "\n  TypeMIME=" + myFile.getTypeMIME();

          _sOutput += "\n  SubTypeMIME=" + myFile.getSubTypeMIME();

          out.println(_sOutput);

          count++;

        }

      }



      // Display the number of files which could be uploaded

      out.println("  " + myTransfer.getFiles().getCount() + " files could be uploaded.");

      final boolean _isDeleteAfterUnzip = true;

      final boolean _isAllowOverwrite = true;

      boolean _isCreateDir = true;  // 在目的目錄要建立子目錄嗎?

      out.println("[" + emisUtil.now() + "] Start to unzipping files...");

      for (int i = 0; i < myTransfer.getFiles().getCount(); i++) {

        emisHttpFile myFile = myTransfer.getFiles().getFile(i);



        // 機號_1.zip

        final String _sName = myFile.getFileName();

        //setupLogger(_sName);


        oLogger_.info("---- " + _sName + "-----");


        oLogger_.info("----- Start $Revision: 6295 $");



        emisFile _oFile = oUploadDir_.getFile(myFile.getFileName());

        oLogger_.debug("  unzip filename=" + _oFile.getFullName());

        out.println("  Unzipping FileName=" + _oFile.getFullName());

        // Save it only if this file exists

        sSysTimeE = emisUtil.todayTimeS();
        if (!myFile.isMissing()) {

          ArrayList _sUnzipResult =

              emisUtil.extractAllZip(_oFile, oUploadDir_, _isDeleteAfterUnzip, _isAllowOverwrite,

                  _isCreateDir,true);

          oLogger_.debug("  after unzip=" + _sUnzipResult);

          String[] aUplFileName = null;
          if (!_sUnzipResult.equals("")) {

            _sOutput += "\r\n  ###FILES: ";
            aUplFileName = new String[_sUnzipResult.size()];
            for(int j=0;j<_sUnzipResult.size();j++) {
            	emisFile _of = (emisFile)_sUnzipResult.get(j);
            	_sOutput += _of.getFileName() + " ";
              aUplFileName[j] = _of.getFullName();
            }

            //TODO: upload2DataServer(_sUnzipResult); // 自動上傳到傳輸主機

          }



          _sOutput += "\r\n  Unzipped FileName=" + _oFile.getFullName();

          out.println(_sOutput);
          // 記錄上傳 Log
          emisUploadLogH oUploadLog = null;
          try{
            oUploadLog = new emisUploadLogH(this.application,null);
//            String[] aUplFileName = (String[])_sUnzipResult.toArray();
//            String sUplPath = oUploadDir_.getDirectory();
            String sRootDir_ = oRootDir_.getRelative();
            String sUplType = "";
            String sServleUrl = "http://"+request.getServerName()+":" + request.getServerPort() +  sRootDir_;
            for(i = 0; i< aUplFileName.length; i++) {
              oUploadLog.addLogRow();
              /*
              if(aUplFileName[i].toLowerCase().indexOf("realtime") >= 0) {
                sUplType = "realtime";
                aUplFileName[i] = aUplFileName[i].substring(aUplFileName[i].toLowerCase().indexOf("realtime") + 9);
              } else if(aUplFileName[i].toLowerCase().indexOf("endofday") >= 0) {
                sUplType = "endofday";
                aUplFileName[i] = aUplFileName[i].substring(aUplFileName[i].toLowerCase().indexOf("endofday") + 9);
              } */
              int idx = aUplFileName[i].lastIndexOf("\\");
              if (idx == -1) idx = aUplFileName[i].lastIndexOf("/");  //找不到\\, 再找/
              if (idx != -1) {// 連相對路徑都壓進去了
                sUplType = aUplFileName[i].substring(0,idx);
                aUplFileName[i] = aUplFileName[i].substring(idx+1);
              }
              oUploadLog.setUL_FILE_ZIP(aUplFileName[i]);
              oUploadLog.setUL_FILE_DIR(sUplType);  // 保存絕對路徑
              oUploadLog.setUL_DATE(sSysDate);
              oUploadLog.setUL_TIME_S(sSysTimeS);
              oUploadLog.setUL_TIME_E(sSysTimeE);
              oUploadLog.setUL_ZIP_SIZE(new File(sUplType,aUplFileName[i]).length()+"");
              oUploadLog.setUL_AP(sServleUrl);
            }
            oUploadLog.insert();
          } catch(Exception e){
            e.printStackTrace();
            oLogger_.warn(e);
          } finally {
            if(oUploadLog != null){
              oUploadLog.close();
              oUploadLog = null;
            }
          }
        }
      }
    } catch (Exception e) {

      oLogger_.error("emisSasUpload.upload: " + e.getMessage());

      e.printStackTrace(response.getWriter());

      _isOK = false;

    }

    // Display the number of files uploaded

    if (_isOK) { // Delphi程式透過"***OK***"判斷是否傳送成功

      String _sResult = "  " + count + " file(s) uploaded. ***OK***";

      oLogger_.debug(_sResult);

      out.println("[" + emisUtil.todayDateAD() + " " + emisUtil.todayTimeS(true) + "] " +

          _sResult);

    }

    oLogger_.info("-----End of Job");

    out.flush();

  }



  /**

   * 依傳上來的機號(檔名)為log檔名.

   *

   * @param sFileName

   

  private void setupLogger(String sFileName) {

    final int _iPos = sFileName.indexOf("_");

    if (_iPos > 0) {

      sFileName = sFileName.substring(0, _iPos);

    }

    hmProp_.put("%LOGFILE%", sFileName + ".log");

    

    /PropertyConfigurator.configure(_oProps);

  }
*/


  /**

   * 用filePost將檔案再傳到資料主機.

   *

   * @param sNames

   * @throws Exception

   */

  private void upload2DataServer(final String sNames) throws Exception {

    final String _sDataServerIP = emisProp.getInstance(application).get("EPOS_SERVER_DATA");

    String _sCurrentIP = emisUtil.getServerIP();



    if (isDebug_)

      out.println("  DataServer IP=" + _sDataServerIP + ",current IP=" + _sCurrentIP);

    if (_sDataServerIP == null || _sCurrentIP.equalsIgnoreCase(_sDataServerIP)) {

      return;

    }



    String _sWebapp = request.getContextPath();  // "/xxx"

    String _sDir = "c:/wwwroot" + _sWebapp + "/data/upload/";

    final StringTokenizer _tokenFiles = new StringTokenizer(sNames, " ");

    final String[] _aFiles = new String[_tokenFiles.countTokens()];

    String[] _aDirs = new String[_tokenFiles.countTokens()];

    int i = 0;

    while (_tokenFiles.hasMoreTokens()) {

      String _sName = _sDir + _tokenFiles.nextToken();

      if (isDebug_) {

        System.out.println("emisSasUpload: File name=" + _sName);

      }

      _aFiles[i] = _sName;

      int _iIndex = _sName.indexOf("data/upload/");

      if (_iIndex > 0) {

        final String _sSaveDir = _sName.substring(_iIndex, _sName.lastIndexOf("/"));

        _aDirs[i] = _sSaveDir;

        if (isDebug_) {

          System.out.println("target[" + i + "]=" + _sSaveDir);

        }

      }



      i++;

    }

    if (_aFiles.length == 0) {

      return;

    }

    final emisHttpClient obj = new emisHttpClient();

    //  將字串陣列指定的檔案上傳給sas_save_hq.jsp接收.

    int _iIndex = _aFiles[0].indexOf("data/upload");

    if (_iIndex > 0) {

      _sDir = _aFiles[0].substring(_iIndex + 12);

      _iIndex = _sDir.lastIndexOf("/");

      _sDir = _sDir.substring(0, _iIndex);

      //obj.setDirectory("data/upload/" + _sDir);  // 不設定TARGET, 則以檔案路徑為路徑

//      if (_aDirs.length > 0) {

//        obj.setDirs(_aDirs);

//      }

    }

//    obj.setIsMove(false);  // Using copy-method



    final int _iStatus = obj.filePost("http://" + _sDataServerIP + _sWebapp + "/jsp/sas/sas_client_hq.jsp", _aFiles);

    if (isDebug_)

      System.out.println("Status=" + _iStatus);

  }

}



