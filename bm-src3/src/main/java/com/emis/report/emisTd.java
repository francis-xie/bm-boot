package com.emis.report;

import com.emis.util.emisChinese;
import com.emis.util.emisUtil;

/**
 * 
 * 目的： wing 2004/10/19 添加 fontName 
 * 便於計算不等於最多列數的Excel xls file的TR，
 * 合併多個列column 說明:
 * 
 *  [4807]wing 20056/01/04 修改並重構
 */
public class emisTd {
	
	protected String sStr_;
	protected int nSize_;
	protected int nAlign_;
	protected int nColumnSpan_ = 1;
	private int dataType = emisReport.REPORT_TEXT; // TEXT 2 NUMBER 1 SEPERTOR (Excel,不處理)
	private String exceldisplay = emisReport.SBOOLEAN_TRUE;
	private int iAlignment_ = 1; // emisReport.A_LEFT;
	private String fontID = emisReport.SEXCEL_DEFALUTFONTID;// 從EMISPRO中取得，沒有則取OS字體
	private String cellStyleID = emisReport.SEXCEL_DEFALUTSTYLE;
	private String sFormatPattern;
	private String sDyShow = emisReport.SBOOLEAN_TRUE;// 是否需要在PROVIDER內處理，一般報表下只需要設定TR的第一EMISTD即可

	private double number = emisReport.EMIS_DZERO;
	boolean isGroup = false;
	private String sectionName = "none";
	private int section = emisReport.SECTION_NONE;// NONE
	private int custid = 100;// 100處理
	private float height = -1;// -1表示不處理
	private String sName_ = "";
	/**
	 * set Size = sStr.length align = A_LEFT columnSpan = 1
	 */
	public emisTd(String sStr) {
		sStr_ = sStr;
		if (sStr_ != null) {
			nSize_ = emisChinese.clen(sStr_);
		}
		nAlign_ = emisReport.A_LEFT;
	}

	public emisTd(String sStr, int nAlign, int nSize) {
		sStr_ = sStr;
		nSize_ = nSize;
		nAlign_ = nAlign;
	}

	public emisTd(String sStr, int nAlign, int nSize, int nColumnSpan) {
		sStr_ = sStr;
		nSize_ = nSize;
		nAlign_ = nAlign;
		nColumnSpan_ = nColumnSpan;
	}

	public emisTd(int nAlign, int nSize, int nColumnSpan) {
		nSize_ = nSize;
		nAlign_ = nAlign;
		nColumnSpan_ = nColumnSpan;
	}

	// wing
	public emisTd(String sStr, int nColumnSpan) {
		sStr_ = sStr;
		nColumnSpan_ = nColumnSpan;
	}

	public emisTd(int nAlign, int nSize) {
		nSize_ = nSize;
		nAlign_ = nAlign;
	}

	public void setContent(String sStr) {
		sStr_ = sStr;
	}

	public String getContent() {
		return sStr_;
	}

	public void setSize(int nSize) {
		nSize_ = nSize;
	}

	public void setAlign(int nAlign) {
		nAlign_ = nAlign;
	}

	public void setColumnSpan(int nSize) {
		nColumnSpan_ = nSize;
	}

	public String toString() {
		return emisUtil.padding(sStr_, nAlign_, nSize_);
	}

	public int getSize() {
		return this.nSize_;
	}

	/**
	 * @return Returns the nColumnSpan_.
	 */
	public int getNColumnSpan_() {
		return nColumnSpan_;
	}

	/**
	 * @param columnSpan_
	 *            The nColumnSpan_ to set.
	 */
	public void setNColumnSpan_(int columnSpan_) {
		nColumnSpan_ = columnSpan_;
	}

	/**
	 * @return Returns the type.
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return Returns the exceldisplay.
	 */
	public String getExceldisplay() {
		return exceldisplay;
	}

	/**
	 * @param exceldisplay
	 *            The exceldisplay to set.
	 */
	public void setExceldisplay(String exceldisplay) {
		this.exceldisplay = exceldisplay;
	}

	/**
	 * @return Returns the iAlignment_.
	 */
	public int getIAlignment_() {
		return iAlignment_;
	}

	/**
	 * @param alignment_
	 *            The iAlignment_ to set.
	 */
	public void setIAlignment_(int alignment_) {
		iAlignment_ = alignment_;
	}

	/**
	 * @return Returns the fontName.
	 */
	public String getFontID() {
		return fontID;
	}

	/**
	 * @param fontName
	 *            The fontName to set.
	 */
	public void setFontID(String fontID) {
		this.fontID = fontID;
	}

	/**
	 * @return Returns the nAlign_.
	 */
	public int getNAlign_() {
		return nAlign_;
	}

	/**
	 * @param align_
	 *            The nAlign_ to set.
	 */
	public void setNAlign_(int align_) {
		nAlign_ = align_;
	}

	/**
	 * @return Returns the number.
	 */
	public double getNumber() {
		return number;
	}

	/**
	 * @param number
	 *            The number to set.
	 */
	public void setNumber(double number) {
		this.number = number;
	}

	/**
	 * @return Returns the sFormatPattern.
	 */
	public String getSFormatPattern() {
		return sFormatPattern;
	}

	/**
	 * @param formatPattern
	 *            The sFormatPattern to set.
	 */
	public void setSFormatPattern(String formatPattern) {
		sFormatPattern = formatPattern;
	}

	/**
	 * @return Returns the cellStyleID.
	 */
	public String getCellStyleID() {
		return cellStyleID;
	}

	/**
	 * @param cellStyleID
	 *            The cellStyleID to set.
	 */
	public void setCellStyleID(String cellStyleID) {
		this.cellStyleID = cellStyleID;
	}

	/**
	 * @return Returns the isGroup.
	 */
	public boolean isGroup() {
		return isGroup;
	}

	/**
	 * @param isGroup
	 *            The isGroup to set.
	 */
	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	/**
	 * 用於EXCEL等，只列的報表
	 * 
	 * @param needShow
	 */
	public void setShow(String sDyShow) {
		this.sDyShow = sDyShow;
	}

	public boolean needShow() {
		if ("true".equalsIgnoreCase(sDyShow))
			return true;
		else
			return false;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public int getCustid() {
		return custid;
	}

	public void setCustid(int custid) {
		this.custid = custid;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}
	/**
	 * 目的: 設定欄位名稱
	 * 
	 * @param sName
	 *            欄位名稱
	 * @return 無
	 */
	public void setName(String sName) {
		sName_ = sName;
	}

	/**
	 * 目的: 取得欄位名稱
	 * 
	 * @param 無
	 * @return 欄位名稱
	 */
	public String getName() {

		return sName_;
	}
}