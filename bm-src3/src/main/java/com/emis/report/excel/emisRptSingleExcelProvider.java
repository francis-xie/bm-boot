package com.emis.report.excel;
/* $Id: emisRptSingleExcelProvider.java 4 2015-05-27 08:13:47Z andy.he $
*
* Copyright (c) EMIS Corp. All Rights Reserved.
* 
*/
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Element;

import com.emis.business.emisBusiness;
import com.emis.report.emisReport;
import com.emis.report.emisTd;
import com.emis.report.emisTr;

/**
 *[4807]wing 2006/01/04 檢查表格ESYS_WREPRPTSET權限設定
 *      輸出的EXCEL數據主要是實現數據分析 
 * 2006/01/01
 * @author Wing
 * Track+[14073] fang 2010/01/12 一頁式報表調整為不在打印依行數跳頁（群組轉換時依群組設置）
 * 
 */
public class emisRptSingleExcelProvider extends emisRptExcelProvider {
	public boolean hadPrintDetail=false;
	public boolean needOncePrintPH = true; //	列印pageheader，檢查是否需要列印
    /**
     * 多種XLS文檔輸出
     * @param oBusiness
     * @param eRoot
     * @throws Exception
     */
	public emisRptSingleExcelProvider(emisBusiness oBusiness, Element eRoot)
			throws Exception {
		super(oBusiness, eRoot);
	}

	public emisRptSingleExcelProvider(emisBusiness oBusiness, Element eRoot,
			HSSFWorkbook xlsWb) throws Exception {
		super(oBusiness, eRoot, xlsWb);
	}

	/**
	 * 不用跳頁，
	 */
	public void eject() {
		// 不處理任何ACT
	}

	/**
	 * 單頁式，某些部分reportheader,pageheader只列印一次
	 */
	public void printTr(emisTr tr) {
		if (!checkPrePrintCond(tr))
			return;
		super.printTr(tr);
	}

	/**
	 * 檢查列印前提是否成立
	 * 只列印reportHeader,pageheader各一次
	 * DETAIL按實際數據記錄數輸出 
	 * @param tr_
	 * @return
	 */
	
	public boolean checkPrePrintCond(emisTr tr_) {
		boolean _needPrintTr = true;
    // 恢复显示
//		/** Track+[14073] fang 2010/01/12 取消一頁式不打印部分頭的設定
		if (tr_ != null) {
			if (tr_.size() > 0) {
				emisTd _td = (emisTd) tr_.get(0);// 考慮效能問題，只CHECK一個TD
				if (_td.getSection()==emisReport.SECTION_DETAIL) {
				    // 優先處理,效能方面的考慮
					hadPrintDetail=true;
         //print     SECTION_REPORTFOOTER
        } else if (_td.getSection()==emisReport.SECTION_REPORTFOOTER) {
				   _needPrintTr=true;
				} else if (_td.getSection()==emisReport.SECTION_REPORTHEADER) {
				  // 報表已實現只輸出一次
				} else if (_td.getSection()==emisReport.SECTION_PAGEHEADER) {
					if(hadPrintDetail){
						_needPrintTr = false;						
					}else{
					  _needPrintTr=true;
					}
					if(this.hasNewPage_){
						hadPrintDetail=false;
						_needPrintTr=true;	
					}
				} else {					
					_needPrintTr = false;
				}//end of td section out
			}//end of tr.size
		}else{
			_needPrintTr = false;
		}		
//    **/
		return _needPrintTr;
	}	
}
