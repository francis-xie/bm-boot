package com.emis.report.excel;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

/**
 * Created on 2004/10/30
 * <br>[3645]wing添加多行字體對齊方式處理 sVerticalAlignment()
 * @author Wing
 *
 * Track+[14814] dana.gao 2010/05/05 增加設定表格backgroundcolor功能.
 */
public class emisExcelReportCellStyle {
	private String cellStyleID;
	private short BorderBottom;
	private short BottomBorderColor;
	private short BorderLeft;
	private short LeftBorderColor;
	private short BorderRight;
	private short RightBorderColor;
	private short BorderTop;
	private short TopBorderColor;
	private short FillForegroundColor = 0;
  private short FillBackgroundColor = 0;
	private short setFillPattern = 0;
	// 多行對齊方式:頂,中央,底,一般
	private short verticalAlignment = HSSFCellStyle.VERTICAL_JUSTIFY;
  private short dataFormart = -1;
  private short rowHeight = -1;

	// HSSFCellStyle.VERTICAL_TOP 頂
	// HSSFCellStyle.VERTICAL_CENTER 中央
	// HSSFCellStyle.VERTICAL_BOTTOM 底
	// HSSFCellStyle.VERTICAL_JUSTIFY
	/**
	 * @return Returns the fillForegroundColor.
	 */
	public short getFillForegroundColor() {
		return FillForegroundColor;
	}

	/**
   * @param fillForegroundColor The fillForegroundColor to set.
	 */
	public void setFillForegroundColor(short fillForegroundColor) {
		FillForegroundColor = fillForegroundColor;
	}

	/**
	 * @return Returns the setFillPattern.
	 */
	public short getSetFillPattern() {
		return setFillPattern;
	}

	/**
   * @param setFillPattern The setFillPattern to set.
	 */
	public void setSetFillPattern(short setFillPattern) {
		this.setFillPattern = setFillPattern;
	}

	public short getBorderBottom() {
		return BorderBottom;
	}

	/**
   * @param borderBottom The borderBottom to set.
	 */
	public void setBorderBottom(short borderBottom) {
		BorderBottom = borderBottom;
	}

	/**
	 * @return Returns the borderLeft.
	 */
	public short getBorderLeft() {
		return BorderLeft;
	}

	/**
   * @param borderLeft The borderLeft to set.
	 */
	public void setBorderLeft(short borderLeft) {
		BorderLeft = borderLeft;
	}

	/**
	 * @return Returns the borderRight.
	 */
	public short getBorderRight() {
		return BorderRight;
	}

	/**
   * @param borderRight The borderRight to set.
	 */
	public void setBorderRight(short borderRight) {
		BorderRight = borderRight;
	}

	/**
	 * @return Returns the borderTop.
	 */
	public short getBorderTop() {
		return BorderTop;
	}

	/**
   * @param borderTop The borderTop to set.
	 */
	public void setBorderTop(short borderTop) {
		BorderTop = borderTop;
	}

	/**
	 * @return Returns the bottomBorderColor.
	 */
	public short getBottomBorderColor() {
		return BottomBorderColor;
	}

	/**
   * @param bottomBorderColor The bottomBorderColor to set.
	 */
	public void setBottomBorderColor(short bottomBorderColor) {
		BottomBorderColor = bottomBorderColor;
	}

	/**
	 * @return Returns the leftBorderColor.
	 */
	public short getLeftBorderColor() {
		return LeftBorderColor;
	}

	/**
   * @param leftBorderColor The leftBorderColor to set.
	 */
	public void setLeftBorderColor(short leftBorderColor) {
		LeftBorderColor = leftBorderColor;
	}

	/**
	 * @return Returns the rightBorderColor.
	 */
	public short getRightBorderColor() {
		return RightBorderColor;
	}

	/**
   * @param rightBorderColor The rightBorderColor to set.
	 */
	public void setRightBorderColor(short rightBorderColor) {
		RightBorderColor = rightBorderColor;
	}

	/**
	 * @return Returns the topBorderColor.
	 */
	public short getTopBorderColor() {
		return TopBorderColor;
	}

	/**
   * @param topBorderColor The topBorderColor to set.
	 */
	public void setTopBorderColor(short topBorderColor) {
		TopBorderColor = topBorderColor;
	}

	/**
	 * @return Returns the cellStyleID.
	 */
	public String getCellStyleID() {
		return cellStyleID;
	}

	/**
   * @param cellStyleID The cellStyleID to set.
	 */
	public void setCellStyleID(String cellStyleID) {
		this.cellStyleID = cellStyleID;
	}

	public short getVerticalAlignment() {
		return verticalAlignment;
	}

  public short getFillBackgroundColor() {
    return FillBackgroundColor;
  }

  public void setFillBackgroundColor(short fillBackgroundColor) {
    FillBackgroundColor = fillBackgroundColor;
  }

	/**
	 * 添加多行對齊
	 * [3645]wing 2005/06/29
	 * @param sVerticalAlignment
	 */
	public void setVerticalAlignment(String sVerticalAlignment) {
		if (sVerticalAlignment == null || "".equals(sVerticalAlignment)) {
			this.verticalAlignment = HSSFCellStyle.VERTICAL_JUSTIFY;
		} else if ("VERTICAL_TOP".equals(sVerticalAlignment)) {
			this.verticalAlignment = HSSFCellStyle.VERTICAL_TOP;
		} else if ("VERTICAL_BOTTOM".equals(sVerticalAlignment)) {
			this.verticalAlignment = HSSFCellStyle.VERTICAL_BOTTOM;
		} else if ("VERTICAL_JUSTIFY".equals(sVerticalAlignment)) {
			this.verticalAlignment = HSSFCellStyle.VERTICAL_JUSTIFY;
		} else if ("VERTICAL_CENTER".equals(sVerticalAlignment)) {
			this.verticalAlignment = HSSFCellStyle.VERTICAL_CENTER;
		}
	}

  //20070113 add wing
  public short getDataFormart() {
    return dataFormart;
}

  public void setDataFormart(short dataFormart) {
    this.dataFormart = dataFormart;
  }

  //20070116 add vince
  public short getRowHeight() {
    return rowHeight;
  }

  public void setRowHeight(short rowHeight) {
    this.rowHeight = rowHeight;
  }

}

