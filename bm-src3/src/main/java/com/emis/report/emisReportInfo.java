package com.emis.report;

/**
 * 功能：報表資訊提供者介面
 * 說明：定義報表資訊提供者，所必須提供的服務
 * @author James Lee
 * @version 1.00, 07/09/01'
 *
 */
public interface emisReportInfo {

  /**
   * 目的: 取得欄位物件
   * @param  sFieldName 欄位名稱
   * @return 欄位物件
   */
  public emisRptField fetchField(String sFieldName);
  /**
   * 目的: 取得資料源物件
   * @return 資料源物件
   */
  public emisRptDataSrc fetchDataSource();
  /**
   * 目的: 產生新的欄位物件
   * @param  sFieldName 欄位名稱
   * @param  iFieldType 欄位型態
   * @return 欄位物件
   */
  public emisRptField newField(String sFieldName, int iFieldType);

}
