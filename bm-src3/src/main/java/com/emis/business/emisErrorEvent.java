package com.emis.business;

//Roland added

import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.error.emisErrorCodeMapper;import java.io.PrintWriter;

import java.util.HashMap;


/**
 * 此class負責儲存error event
 */
public class emisErrorEvent extends java.lang.Object {
  private javax.servlet.ServletContext oContext_ = null;
  private String sBreakMessage_ = "";
  private boolean isBreakTransation_ = false;
  private String sErrorCode_ = "";
  private PrintWriter oLogWriter_ = null;
  private emisFileMgr oFileMgr_ = null;
  /* 儲存所有ErrorEvent.XML 的內容,key:Error name ,value:Element*/
  private HashMap hmErrorCode_ = new HashMap();
  /* 暫存存一個Error node的內容,key:Event name ,value:Element*/
  private HashMap hmError_ = new HashMap();
  com.emis.error.emisErrorCodeMapper CodeMapper = null;

  public emisErrorEvent() {
  }

  public emisErrorEvent(javax.servlet.ServletContext oContext) {
    try {
      this.oContext_ = oContext;
      this.oFileMgr_ = emisFileMgr.getInstance(oContext);
      CodeMapper = (emisErrorCodeMapper) emisErrorCodeMapper.getInstance(oContext_);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @deprecated be Instead by give ServletContext
   * @param oFileMgr
   */
 public emisErrorEvent (emisFileMgr oFileMgr) {
   try
   {
     this.oFileMgr_ = oFileMgr;
   }catch(Exception e)
   {
     e.printStackTrace();
   }
 }

  /**
   * 設定錯誤物件
   * @param e Exception
   */
  public void setErrorException(Exception e) {
    isBreakTransation_ = true;
    sBreakMessage_ = e.getMessage();
    e.printStackTrace();
  }


  /**
   * 設定錯誤代碼及額外的錯誤訊息
   * @param errorCode     : 錯誤代碼
   * @param sExtraMeg      : 額外的錯誤訊息 ,系統會自動累加到預設定義的錯誤訊息後面
   */
  public void setErrorException(String errorCode, String sExtraMeg) {
    this.sErrorCode_ = errorCode;
    if (CodeMapper!= null ){
      StringBuffer _sMessage = new StringBuffer(CodeMapper.getErrorMsg(errorCode));
      sBreakMessage_ = _sMessage.append(sExtraMeg).toString();
    } else {
      sBreakMessage_ =   sExtraMeg;
    }
  }

  /**
   * 設定錯誤代碼
   * @param sErrorCode : 錯誤代碼
   */
  public void setErrorCode(String sErrorCode) {
    this.sErrorCode_ = sErrorCode;
    if (CodeMapper!= null ){
      this.sBreakMessage_ = CodeMapper.getErrorMsg(sErrorCode);
    }
  }
/**
 * @deprecated 不建議使用
  * @return
 */
  public PrintWriter getWriter() {
     if (this.oLogWriter_ == null ) {
       try {
         emisDirectory _oXmlDir = this.oFileMgr_.getDirectory("root").subDirectory("logs");
         emisFile _oFile =  _oXmlDir.getFile("error.log") ;

         oLogWriter_ = _oFile.getWriter("A");

       } catch (Exception e) {
         e.printStackTrace();  //To change body of catch statement use Options | File Templates.
       }
     }
     return this.oLogWriter_ ;
   }


  /**
   * 取得錯誤代碼
   * @return
   */
  public String getErrorCode() {
    return sErrorCode_;
  }

  public void close() {}

  public boolean isBreakTransation() {
    return this.isBreakTransation_;
  }

  public String getBreakMessage() {
    return sBreakMessage_;
  }
}