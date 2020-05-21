/*
 * $History: emisRptCell.java $
 * 
 * *****************  Version 1  *****************
 * User: Jerry        Date: 01/04/16   Time: 5:02p
 * Created in $/WWWroot/ePOS/classes/com/emis/report
 */
package com.emis.report;

/**
 * 用來儲存報表欄位格(cell)之類別
 */
public class emisRptCell {
  /** 報表名稱 */
  private String sName_;
  /** 對齊方式 */
  private int iAlign_;
  /** 欄寬 */
  private int iWidth_;
  private static final int _ALIGN_LEFT = 0;
  private static final int _ALIGN_CENTER = 1;
  private static final int _ALIGN_RIGHT = 2;

  /**
   * 報表欄格
   * @param sName   報表名稱
   * @param sAlign  對齊方式字串
   * @param iWidth  報表寬度
   */
  public emisRptCell(String sName, String sAlign, int iWidth) {
    sName_ = sName;
    if ("left".equalsIgnoreCase(sAlign)) iAlign_ = _ALIGN_LEFT;
    else if ("center".equalsIgnoreCase(sAlign)) iAlign_ = _ALIGN_CENTER;
    else if ("right".equalsIgnoreCase(sAlign)) iAlign_ = _ALIGN_RIGHT;

    iWidth_ = iWidth;
  } // cell()

  public String toString() {
    return getName() + " " + getAlign() + " " + getWidth();
  }

  public String getName() {
    return sName_;
  }

  public int getAlign() {
    return iAlign_;
  }

  public int getWidth() {
    return iWidth_;
  }
} // emisRptCell