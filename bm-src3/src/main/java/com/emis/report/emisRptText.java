package com.emis.report;



/**
 * 功能：報表文字型態欄位類別
 * 說明：處理文字型態之欄位內容
 * @author James Lee
 * @version 1.00, 07/09/01'
 *
 */
public class emisRptText extends emisRptField {

  private String sContent_ = "";
  private String sCacheContent_ = null;
  private String sLastContent_ = "";


  /**
  * 目的: 建構元
  * @param  無
  * @return 無
  */
  public emisRptText() {

    super();

  }

  /**
  * 目的: 傳回文字型態
  * @param  無
  * @return 文字型態代碼
  */
  public int myType() {

    return emisRptField.TEXT;
  }


  /**
  * 目的: 傳回欄位內容
  * @param  無
  * @return 內容字串
  */
  public String getContent() {

    return sContent_;
  }

  /**
  * 目的: 檢查欄位值是否換新
  * @param  無
  * @return true/false
  */
  public boolean isNewValue() {
    boolean _isNewValue = true;

    if (sCacheContent_ == null) {
      if (sContent_.equals(sLastContent_)) {
        _isNewValue = false;
      }
    } else if (sCacheContent_.equals(sContent_)) {
      _isNewValue = false;
    }

    return _isNewValue;
  }

  @Override
  public boolean isSupressDuplicateNewValue() {
    boolean _isNewValue = true;
    if (sLastContent_.equals(sContent_)) {
      _isNewValue = false;
    }
    return _isNewValue;
  }

  /**
  * 目的: 傳回欄位的數值內容
  * @param  無
  * @return double 型態的數值
  */
  public double getNumber() {
    double _dRetVal=0.0;

    try {
      _dRetVal = Double.parseDouble(sContent_);
    } catch (Exception e) {
      _dRetVal = 0.0;
    }

    return _dRetVal;
  }

  /**
  * 目的: 設定欄位值
  * @param  iContent int 型態的數值
  * @return 無
  */
  public void setContent(int iContent) {
    setContent(String.valueOf(iContent));
  }

  /**
  * 目的: 設定欄位值
  * @param  lContent long 型態的數值
  * @return 無
  */
  public void setContent(long lContent) {
    setContent(String.valueOf(lContent));
  }

  /**
  * 目的: 設定欄位值
  * @param  fContent float 型態的數值
  * @return 無
  */
  public void setContent(float fContent) {
    setContent(String.valueOf(fContent));
  }

  /**
  * 目的: 設定欄位值
  * @param  dContent double 型態的數值
  * @return 無
  */
  public void setContent(double dContent) {
    setContent(String.valueOf(dContent));
  }

  /**
  * 目的: 設定欄位值
  * @param  sContent String 型態的字串值
  * @return 無
  */
  public void setContent(String sContent) {

    if (sContent==null){
      sContent = "";
    }

    // 存入新值
    cacheContent(sContent);
    commitContent();

  }

  /**
  * 目的: 暫存欄位值
  * @param  iContent int 型態的數值
  * @return 無
  */
  public void cacheContent(int iContent) {
    cacheContent(String.valueOf(iContent));
  }

  /**
  * 目的: 暫存欄位值
  * @param  lContent long 型態的數值
  * @return 無
  */
  public void cacheContent(long lContent) {
    cacheContent(String.valueOf(lContent));
  }

  /**
  * 目的: 暫存欄位值
  * @param  fContent float 型態的數值
  * @return 無
  */
  public void cacheContent(float fContent) {
    cacheContent(String.valueOf(fContent));
  }

  /**
  * 目的: 暫存欄位值
  * @param  dContent double 型態的數值
  * @return 無
  */
  public void cacheContent(double dContent) {
    cacheContent(String.valueOf(dContent));
  }

  /**
  * 目的: 暫存欄位值
  * @param  sContent String 型態的字串值
  * @return 無
  */
  public void cacheContent(String sContent) {

    if (sContent==null){
      sContent = "";
    }
    sCacheContent_ = sContent;
  }

  /**
  * 目的: commit 暫存值
  * @param  無
  * @return 無
  */
  public void commitContent() {

    if (sCacheContent_ != null) {
      sLastContent_ = sContent_;
      sContent_ = sCacheContent_;
      sCacheContent_ = null;
    }
  }

}
