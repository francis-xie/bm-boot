/* $Id: emisReportFactory.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) EMIS Corp. All Rights Reserved.
 */
package com.emis.report;


/**
 * 功能：報表相關物件建立類別
 * 說明：提供報表架構所需類別之建立服務
 * @author James Lee
 * @version 1.00, 07/09/01'
 * $Revision: 71118 $
 */
public class emisReportFactory {
  /** Report provider */
  protected emisRptProvider oProvider_;

  /**
   * 建構元.
   * @param  oProvider  I/O 服務提供者物件
   */
  public emisReportFactory(emisRptProvider oProvider) {
    oProvider_ = oProvider;
  }

  /**
   * 產生PageHeader物件.
   * @return 屬於PageHeader之報表段落物件
   */
  public emisRptSection createPageHeader() {
    return new emisRptSection();
  }

  /**
   * 產生PageFooter物件.
   * @return 屬於PageFooter之報表段落物件
   */
  public emisRptSection createPageFooter() {
    return new emisRptSection();
  }

  /**
   * 產生ReportHeader物件.
   * @return 屬於ReportHeader之報表段落物件
   */
  public emisRptSection createReportHeader() {

    return new emisRptSection();
  }

  /**
   * 產生ReportFooter物件.
   * @return 屬於ReportFooter之報表段落物件
   */
  public emisRptSection createReportFooter() {
    return new emisRptSection();
  }

  /**
   * 產生Group物件.
   * @return 屬於Group之報表段落物件
   */
  public emisRptGroup createGroup() {
    return new emisRptGroup();
  }

  /**
   * 產生資料源物件.
   * @param sDataSrcID 資料源物件
   * @return
   */
  public emisRptDataSrc createDataSource(String sDataSrcID) {
    emisRptDataSrc _oDataSrc = null;
    try {
      _oDataSrc = new emisRptDataSrc(oProvider_, sDataSrcID);
    } catch (Exception e) {
      throw new RuntimeException("  emisReportFactory.createDataSource(): " +
          "DataSrcId=" + sDataSrcID + "\n" + e.getMessage() + "\n");
    }
    return _oDataSrc;
  }
}
