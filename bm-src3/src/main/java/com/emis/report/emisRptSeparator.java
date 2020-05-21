package com.emis.report;


/**
 * 功能：報表日期型態欄位類別
 * 說明：處理日期型態之欄位內容
 * @author Jacky Hsiue
 * @version 1.00, 07/09/01'
 *
 */
public class emisRptSeparator extends emisRptField {

  private String sContent_ = "";


  /**
  * 目的: 建構元
  * @param  無
  * @return 無
  */
  public emisRptSeparator() {

    super();

  }

  /**
  * 目的: 建構元
  * @param  sString 內容字串
  * @param  iWidth  欄寬
  * @return 無
  */
  public emisRptSeparator(String sString , int iWidth) {
    super();
    setContent(sString);
    setProperty("WIDTH",iWidth + "");
  }

  /**
  * 目的: 建構元
  * @param  sString 內容字串
  * @return 無
  */
  public emisRptSeparator(String sString) {
    super();
    setContent(sString);
  }

  /**
  * 目的: 傳回分隔欄位型態
  * @param  無
  * @return 分隔型態代碼
  */
  public int myType() {

    return emisRptField.SEPARATOR;
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

    return false;
  }

  @Override
  public boolean isSupressDuplicateNewValue() {
    return false;
  }

  /**
  * 目的: 傳回欄位的數值內容
  * @param  無
  * @return double 型態的數值
  */
  public double getNumber() {

    return 0.0;
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

    sContent_ = sContent;
  }

  /**
  * 目的: 暫存欄位值
  * @param  iContent int 型態的數值
  * @return 無
  */
  public void cacheContent(int iContent) {}

  /**
  * 目的: 暫存欄位值
  * @param  lContent long 型態的數值
  * @return 無
  */
  public void cacheContent(long lContent) {}

  /**
  * 目的: 暫存欄位值
  * @param  fContent float 型態的數值
  * @return 無
  */
  public void cacheContent(float fContent) {}

  /**
  * 目的: 暫存欄位值
  * @param  dContent double 型態的數值
  * @return 無
  */
  public void cacheContent(double dContent) {}

  /**
  * 目的: 暫存欄位值
  * @param  sContent String 型態的字串值
  * @return 無
  */
  public void cacheContent(String sContent) {}

  /**
  * 目的: commit 暫存值
  * @param  無
  * @return 無
  */
  public void commitContent() {}

  /**
  * 目的: 取得格式化字串，受 Width, Format 等屬性影響
  * @param  無
  * @return 格式化字串
  */
  public String getFormatedString() {
    String _sContent = getContent();
    String _sFormatedString ;
    if (_sContent == null)
      _sContent = " ";

    _sFormatedString = emisString.replicate(getContent(),getWidth());
    _sFormatedString = emisString.leftB(_sFormatedString,getWidth());
    return _sFormatedString;
  }

}
