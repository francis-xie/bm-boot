package com.emis.report;

import java.math.BigDecimal;

/**
 * 功能：報表數值型態欄位類別
 * 說明：處理數值型態之欄位內容
 * @author James Lee
 * @version 1.00, 07/09/01'
 *
 */
public class emisRptNumber extends emisRptField {

  private double dContent_ = 0.0;
  private double dCacheContent_ = 0.0;
  private int iDecimals_ = -1;
  private double dLastContent_ = 0.0;
  private boolean isCommitted_ = true;


  /**
  * 目的: 建構元
  * @param  無
  * @return 無
  */
  public emisRptNumber() {

    super();

  }

  /**
  * 目的: 傳回數值型態
  * @param  無
  * @return 數值型態代碼
  */
  public int myType() {

    return emisRptField.NUMBER;
  }


  /**
  * 目的: 傳回欄位內容
  * @param  無
  * @return 內容字串
  */
  public String getContent() {

    return String.valueOf(getNumber());
  }

  /**
  * 目的: 檢查欄位值是否換新
  * @param  無
  * @return true/false
  */
  public boolean isNewValue() {
    boolean _isNewValue = true;
    if (isCommitted_) {
      if (dContent_ == dLastContent_) {
        _isNewValue = false;
      }
    } else if (dCacheContent_ == dContent_) {
      _isNewValue = false;
    }

    return _isNewValue;
  }

  @Override
  public boolean isSupressDuplicateNewValue() {
    boolean _isNewValue = true;
    if (dContent_ == dLastContent_) {
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
    BigDecimal _oDigit;
    double _dRetVal;

    if (iDecimals_ >= 0) {
      _oDigit = new BigDecimal(String.valueOf(dContent_));
      _oDigit = _oDigit.setScale(iDecimals_, BigDecimal.ROUND_HALF_UP);
      _dRetVal = _oDigit.doubleValue();
    } else {
      _dRetVal = dContent_;
    }

    return _dRetVal;
  }

  /**
  * 目的: 設定欄位值
  * @param  iContent int 型態的數值
  * @return 無
  */
  public void setContent(int iContent) {
    setContent((double) iContent);
  }

  /**
  * 目的: 設定欄位值
  * @param  lContent long 型態的數值
  * @return 無
  */
  public void setContent(long lContent) {
    setContent((double) lContent);
  }

  /**
  * 目的: 設定欄位值
  * @param  fContent float 型態的數值
  * @return 無
  */
  public void setContent(float fContent) {
    setContent((double) fContent);
  }

  /**
  * 目的: 設定欄位值
  * @param  sContent String 型態的字串值
  * @return 無
  */
  public void setContent(String sContent) {
    double _dNumber = 0.0;
    if (sContent == null) {
      sContent = "0.0";
    }
    try {
      _dNumber = Double.parseDouble(sContent);
    } catch (Exception e) {
      _dNumber = 0.0;
    } finally {
      setContent(_dNumber);
    }
  }

  /**
  * 目的: 設定欄位值
  * @param  dContent double 型態的數值
  * @return 無
  */
  public void setContent(double dContent) {

    // 存入新值
    cacheContent(dContent);
    commitContent();
  }

  /**
  * 目的: 暫存欄位值
  * @param  sContent String 型態的字串值
  * @return 無
  */
  public void cacheContent(String sContent) {
    if (sContent == null) {
      sContent = "0.0";
    }
    cacheContent((Double.valueOf(sContent)).doubleValue());
  }

  /**
  * 目的: 暫存欄位值
  * @param  dContent double 型態的數值
  * @return 無
  */
  public void cacheContent(double dContent) {

    dCacheContent_ = dContent;
    isCommitted_ = false;
  }

  /**
  * 目的: 暫存欄位值
  * @param  iContent int 型態的數值
  * @return 無
  */
  public void cacheContent(int iContent) {
    cacheContent((double) iContent);
  }

  /**
  * 目的: 暫存欄位值
  * @param  lContent long 型態的數值
  * @return 無
  */
  public void cacheContent(long lContent) {
    cacheContent((double) lContent);
  }

  /**
  * 目的: 暫存欄位值
  * @param  fContent float 型態的數值
  * @return 無
  */
  public void cacheContent(float fContent) {
    cacheContent((double) fContent);
  }

  /**
  * 目的: commit 暫存值
  * @param  無
  * @return 無
  */
  public void commitContent() {

    if (! isCommitted_) {
      dLastContent_ = dContent_;
      dContent_ = dCacheContent_;
      isCommitted_ = true;
    }
  }

  public boolean setProperty(String sPropertyName, String sValue) {
    boolean _isOk;

    _isOk = super.setProperty(sPropertyName, sValue);
    if (! _isOk) {
      if ("Decimals".equalsIgnoreCase(sPropertyName)) {
        iDecimals_ = Integer.parseInt(sValue);
      } else {
        _isOk = false;
      }
    }

    return _isOk;
  }
}
