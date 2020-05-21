package com.emis.report;



/**
 * 功能：報表日期型態欄位類別
 * 說明：處理日期型態之欄位內容
 * @author James Lee
 * @version 1.00, 07/09/01'
 *
 */
public class emisRptDate extends emisRptField {

  private String sContent_ = "";
  private String sCacheContent_ = null;
  private String sLastContent_ = "";
  private String sFormat_ = "ROC";

  /**
  * 目的: 建構元
  * @param  無
  * @return 無
  */
  public emisRptDate() {

    super();

  }

  /**
  * 目的: 傳回日期型態
  * @param  無
  * @return 日期型態代碼
  */
  public int myType() {

    return emisRptField.DATE;
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
      if (sContent_.equals(sLastContent_)) {
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

  /**
  * 目的: 取得格式內容，傳回 "YYY/MM/DD" 格式的字串供父類別的
  *       getFormatedString() 方法使用
  * @param  無
  * @return 日期字串
  */
  protected String getFormatContent() {
    String _sDateString = getContent();

    if (_sDateString != null) {
      if ("US".equalsIgnoreCase(sFormat_) && _sDateString.length() > 7) {
        _sDateString = _sDateString.substring(0,4) + "/" +
                       _sDateString.substring(4,6) + "/" +
                       _sDateString.substring(6);
      } else if (_sDateString.length() > 6) {
        _sDateString = _sDateString.substring(0,3) + "/" +
                       _sDateString.substring(3,5) + "/" +
                       _sDateString.substring(5);
      } else {
        _sDateString = "";
      }
    }

    return _sDateString;
  }

  /**
  * 目的: 設定欄位輸出樣式
  * @param  sFormatPattern 樣式字串
  * @return 無
  */
  public void setFormatPattern(String sFormatPattern) {

    sFormat_ = sFormatPattern;

    return;
  }

}
