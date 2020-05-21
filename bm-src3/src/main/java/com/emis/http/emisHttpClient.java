/* $Header: /repository/src3/src/com/emis/http/emisHttpClient.java,v 1.1.1.1 2005/10/14 12:42:10 andy Exp $
 *
 * Created by IntelliJ IDEA.
 * User: jerry
 * Date: 2003/6/16
 * Time: 下午 03:47:51
 *
 * 將commons-httpclient-2.0-beta1.jar, commons-logging.jar拷貝到c:/resin/lib
 * 叫用jsp/sas_save_hq.jsp來存檔
 *
 * 檔案被存入c:/wwwroot/xxx/data/upload目錄中(要改目錄須修改sas_save_hq.jsp第31列)
 *
 * 2004/05/23 Jerry:
 *   1.增加檔案複製方式(是否搬移): getIsMove(), setIsMove()
 *   2.主機路徑可設定多層(修改sas_save_hq.jsp): data/upload, data/upload/test
 */
package com.emis.http;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

import java.io.File;

/**
 * 提供HTTP Client; 使用Jakarta專案的commons-HttpClient元件. 必須存在的JAR檔:
 *     commons-httpclient-2.0-beta1.jar, commons-logging.jar
 */
public class emisHttpClient {
  private static boolean _DEBUG_ = false;
  private String sTargetPath_ = null;
  private boolean isMove_ = true;
  private String[] aDirs_;

  /**
   * 執行form-data的檔案上傳功能; 預設是上傳後刪除local的檔案.
   * @param sURL 接收上傳檔案的JSP或Servlet
   * @param aFiles 要上傳的檔案陣列; 必須包含路徑
   * @return HTTP的Status code
   */
  public int filePost(String sURL, String[] aFiles) {
    return filePost(sURL, aFiles, true);
  }

  /**
   *設定要傳入Client端的路徑,預設從root下
   * @param sDir   Client端的路徑
   */
  public void setDirectory(String sDir) {
    sTargetPath_ = sDir;
  }

  public void setDirs(String[] aDirs) {
    aDirs_ = aDirs;
  }

  public boolean getIsMove() {
    return isMove_;
  }

  public void setIsMove(boolean isMove) {
    isMove_ = isMove;
  }

  /**
   * 執行form-data的檔案上傳功能
   * @param sURL 接收上傳檔案的JSP或Servlet
   * @param aFiles 要上傳的檔案陣列; 必須包含路徑
   * @param needDelete 本地端的檔案在上傳後是否要刪除?
   * @return HTTP的Status code
   */
  public int filePost(String sURL, String[] aFiles, boolean needDelete) {
    HttpClient _oClient = null;
    MultipartPostMethod _oFilePost = null;
    int _iStatus = 0;

    try {
      _oClient = new HttpClient();
      _oClient.setConnectionTimeout(5000);
      _oFilePost = new MultipartPostMethod(sURL);

      for (int i = 0; i < aFiles.length; i++) {
        String _sFile = aFiles[i];
        File _oFile = new File(_sFile);
        if (_oFile.exists()) {
          _oFilePost.addParameter(_oFile.getName(), _oFile);
        }
      }
      if (_oFilePost.getParts().length <= 0) return 0;  // 沒有檔案則不POST了.

      // TARGET0..TARGET99 存每個檔案的存放目錄
      if (aDirs_ != null) {
        for (int i = 0; i < aDirs_.length; i++) {
          _oFilePost.addParameter("TARGET" + i, aDirs_[i]);
          System.out.println("target" + i + "=" + aDirs_[i]);
        }
      }

      //20030618 peace add 增加傳入路徑
      if (sTargetPath_ != null) _oFilePost.addParameter("TARGET", sTargetPath_);
      _iStatus = _oClient.executeMethod(_oFilePost);
      if (_iStatus == HttpStatus.SC_OK) {
        String _sResponse = _oFilePost.getResponseBodyAsString();
        if (_DEBUG_) System.out.println("Upload complete, response=" + _sResponse);
        int _iPos = _sResponse.indexOf("###FILES: ");
        if (_iPos > 0) {  // 上傳成功回Response回其檔名, 再將local的檔案刪除.
          String _sFilenames = _sResponse.substring(_iPos + 11);
          for (int i = 0; i < aFiles.length; i++) {
            File _oFile = new File(aFiles[i]);
            if (getIsMove() && _sFilenames.indexOf(_oFile.getName()) >= 0) {  // 比檔名, 不比路徑
              _oFile.delete();
            }
          }
        }
      } else {
        if (_DEBUG_) System.out.println("Upload failed, response=" + HttpStatus.getStatusText(_iStatus));
      }
    } catch (Exception e) {
      if (_DEBUG_) System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (_oFilePost != null) _oFilePost.releaseConnection();
    }

    return _iStatus;
  }

  public static void main(String[] args) {
//    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//    System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
//    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");

    emisHttpClient obj = new emisHttpClient();
    //  將字串陣列指定的檔案上傳給sas_save_hq.jsp接收.
    String[] _aFiles = {"d:/tmp/test1.txt", "d:/tmp/test2.txt"};
    String[] _aDirs = { "data/upload2", "data/upload2" };
    //obj.setDirectory("data/upload_test/test2");
    obj.setDirs(_aDirs);
    obj.setIsMove(false);  // Using copy-method
    int _iStatus = obj.filePost("http://tpntr01.emis.com.tw/eros/jsp/sas/sas_client_hq.jsp", _aFiles);
    System.out.println("Status=" + _iStatus);
  }
}
