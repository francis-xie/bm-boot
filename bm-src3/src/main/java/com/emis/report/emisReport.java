package com.emis.report;
/* $Id: emisReport.java 4 2015-05-27 08:13:47Z andy.he $
*
* Copyright (c) EMIS Corp. All Rights Reserved.
* 4807] wing 2006/01/04 add添加const,統一管理
* 群組處理6層
* 2010/04/30 sunny 增加段落重排的水平對齊方式 justify
*/
public interface emisReport {

	public static final int A_LEFT = 1;
	public static final int A_CENTER = 2;
	public static final int A_RIGHT = 3;
  public static final int A_FILL = 4;
  public static final int A_JUSTIFY = 5;

  //[4807] wing 2006/01/04 add
	public static final int SECTION_NONE = 0;// none
	public static final int SECTION_DETAIL = 1;// detail
	public static final int SECTION_REPORTHEADER = 2;// reportheaer
	public static final int SECTION_REPORTFOOTER = 3;// reportfooter
	public static final int SECTION_PAGEHEADER = 4;// pageheader
	public static final int SECTION_PAGEFOOTER = 5;// pagefooter
	public static final int SECTION_GROUPHEADER0 = 6;// groupheader0
	public static final int SECTION_GROUPHEADER1 = 7;// groupheader1
	public static final int SECTION_GROUPHEADER2 = 8;// groupheader2
	public static final int SECTION_GROUPHEADER3 = 9;// groupheader3
	public static final int SECTION_GROUPHEADER4 = 10;// groupheader4
	public static final int SECTION_GROUPHEADER5 = 11;// groupheader5
	public static final int SECTION_GROUPHEADER6 = 12;// groupheader6

	public static final int SECTION_GROUPFOOTER0 = 13;// groupfooter0
	public static final int SECTION_GROUPFOOTER1 = 14;// groupfooter1
	public static final int SECTION_GROUPFOOTER2 = 15;// groupfooter2
	public static final int SECTION_GROUPFOOTER3 = 16;// groupfooter3
	public static final int SECTION_GROUPFOOTER4 = 17;// groupfooter4
	public static final int SECTION_GROUPFOOTER5 = 18;// groupfooter5
	public static final int SECTION_GROUPFOOTER6 = 19;// groupfooter6
	public static final int SECTION_PAGEBREAK = 20;// pagebreak

	public static final String SBOOLEAN_TRUE = "true";
	public static final String SBOOLEAN_FALSE = "false";

	public static final String SEXCEL_DEFALUTFONTID = "defaultFontID";
	public static final String SEXCEL_DEFALUTSTYLE = "defaultCellStyle";

	public static final int EMIS_ZERO = 0;
	public static final double EMIS_DZERO = 0;

	public static final int REPORT_TEXT = 2;// TEXT 2 NUMBER 1 SEPERTOR
	public static final int REPORT_NUMBER = 1;// TEXT 2 NUMBER 1 SEPERTOR
	public static final int REPORT_SEPERTOR = 3;// TEXT 2 NUMBER 1 SEPERTOR

	public static final int REPORT_NODISPLAY = 9;// 報表動態權限不顯示,只創建，沒有VALUE

	/**
	 * 這是惟一的子 Class 需要 implement 的 method 同時也是 emisBusiness 所呼叫的 function call
	 */
	public void printRpt() throws Exception;

}