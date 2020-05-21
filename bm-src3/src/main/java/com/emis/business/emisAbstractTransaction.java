/* $Header: /repository/src3/src/com/emis/business/emisAbstractTransaction.java,v 1.1.1.1 2005/10/14 12:41:47 andy Exp $
 *
 * 2003/09/03 Jerry: Add debugging information
 * 2004/01/18 增加作業及action的功能
 * 2004/01/30 add by Jacky : 修改輸出錯誤
 * 2004/02/06 Jerry: 錯誤同步輸出至emisServer.log
 */
package com.emis.business;

import com.emis.db.emisDb;

import com.emis.trace.emisTracer;
import com.emis.util.emisUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * c:/wwwroot/專案/WEB-INF/classes/   ***.class 的 super class
 * 負責 DB connection  給予***.class   並把 每一個***.class 的event
 * 轉交於event handle
 */
abstract public class emisAbstractTransaction {
  private final String sBreakTransaction_ = "Transaction";
  private emisDb oSqlConnection_;
  private Hashtable oMap_ = new Hashtable();
  protected emisTracer oTracer_;
  protected emisBusinessResourceBean resourceBean_;
  protected Object oObject_;
  protected HashMap oParameterData_ = new HashMap();

  /**
   * 每個 extends 此 super class 必須要實作 run() 這個 method
   */
  public void run() throws Exception {
    if (process()) {
      commitData();
    }
  }

  /**
   * PBO 程式進入點
   *
   * @param resourceBean 系統資源物件
   * @param oParam  由 XML 檔所傳進之參數
   * @param oExport 將此次 PBO 相關物件傳到下一個 PBO 來使用
   * @throws Exception
   */
  public void runBusiness(emisBusinessResourceBean resourceBean, HashMap oParam, HashMap oExport) throws Exception {
    this.resourceBean_ = resourceBean;
    this.oParameterData_ = oParam;

    oTracer_ = emisTracer.get(resourceBean.getServletContext());
    if (oTracer_.isTraceEnabled()) {
      oTracer_.info("emisAbstractTransaction.runBusiness: run business it---");
    }

    if (oSqlConnection_ == null) oSqlConnection_ = resourceBean_.getEmisDb();
    try {
      //-if (oTracer_.isTraceEnabled()) oTracer_.info("  before importData");
      importData();

      //-if (oTracer_.isTraceEnabled()) oTracer_.info("  after importData, before run");
      run();

      //-if (oTracer_.isTraceEnabled()) oTracer_.info("  after run, before exportData");
      exportData();


      //20040807 Jacky 增加統錯誤訊息統一顯示到前端
      if ( processError() ){
        // 成功就設 RET_SUCCESS
        exportAttribute("RET_SUCCESS", getSuccessReturnMessage());
      }
    } catch (Exception e) {
      String _sMsg = printParameters();  //- 輸出前端的所有參數
      if (oTracer_.isTraceEnabled()) {
        oTracer_.info(_sMsg);
        oTracer_.reportException(e);
        //20040903 [825] jacky 增加輸出錯誤到前端
        exportAttribute("RET1",e.getMessage());
      }
      e.printStackTrace(System.out);
      System.out.println("==Exception End ==  ");
      throw e;
      //20004/08/24 Jacky processError();
    }
  }


  /**
   * 處理錯誤程序
   */
  protected boolean processError() throws Exception {
    boolean isSuccessed = true;
    String _sMessage =  this.resourceBean_.getEmisErrorEvent().getBreakMessage();

    if (! "".equals(_sMessage)) {
      exportAttribute("RET1",_sMessage);
      if (this.resourceBean_.getEmisErrorEvent().isBreakTransation()) {
        throw new Exception(_sMessage);
      }
    }
    return isSuccessed;
  }
  /**
   * 傳前端傳入的參數都輸出到stderr...
   * @return _sMsg
   */
  private String printParameters() {
    HttpServletRequest request = this.resourceBean_.getEmisHttpServletRequset();
    Enumeration e = request.getParameterNames();
    StringBuffer _sbMsg = new StringBuffer("== Exception Start : ")
        .append(emisUtil.todayDate("/"))
        .append("  ").append(emisUtil.todayTimeS());
    //              + "  TITLE:"+request.getParameter("TITLE") +":"
    //              + "  ACT:"+this.resourceBean_.getBusiness().getRequest().getParameter("ACT") +"==";
    while (e.hasMoreElements()) {
      String _sName = (String) e.nextElement();
      String _sValue = request.getParameter(_sName);
      _sbMsg.append("\n  ").append(_sName).append("=").append(_sValue);
    }
    _sbMsg.append("\n");
    System.out.println(_sbMsg.toString()) ;
    return _sbMsg.toString();
  }

  /**
   * 成功傳回值
   * @return
   */
  protected String getSuccessReturnMessage() {
    return "";
  }

  /**
   * 設定參數，目前還未被使用
   * @param value
   */
  public void setParameter(Object value) {
    oObject_ = value;
  }

  /**
   * 新增參數，目前還未被使用
   * @param key
   * @param value
   */
  public void addParameter(Object key, Object value) {
    oMap_.put(key, value);
  }

  /**
   * 關閉 db connection
   */
  public void close() {
    try {
      oSqlConnection_.commit();
    } catch (Exception e) {
      ;
    } finally {
      oSqlConnection_.close();
    }
  }

  /**
   * 提供 PBO 實作輸出物件
   */
  protected void exportData() {
  }

  /**
   * 提供 PBO 實作輸入物件
   */
  protected void importData() {
  }

  /**
   * 取得由xml傳過來的參數
   */
  protected String getParameter(String sKey) {
    return (String) this.oParameterData_.get(sKey);
  }

  /**
   * 將物件輸出至 Business Attribute
   */
  protected void exportAttribute(Object key, Object value) {
    emisBusiness business = this.resourceBean_.getBusiness();
    //if (business != null) {
    business.setAttribute(key, value);
    //}
  }

  /**
   * 取得Business物件的參數
   */
  protected Object importAttribute(Object key) {
    //emisBusiness business = resourceBean_.getBusiness();
    return this.resourceBean_.getBusiness().getAttribute(key);
  }

  /**
   * 讓各個 PBO 自己決定商業行為
   */
  protected abstract boolean process() throws Exception;

  /**
   * 更新資料庫動作
   */
  protected abstract void commitData() throws Exception;
}
