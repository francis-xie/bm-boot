/*
 * Created on 2004/11/1
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.emis.report.excel;

import java.util.HashMap;
 
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.w3c.dom.Node;

import com.emis.messageResource.Messages;
import com.emis.report.emisRptField;
import com.emis.report.emisRptGroup;
import com.emis.report.emisRptSection;
import com.emis.report.emisRptXML;

/**
 * @author wing
 *
 * @version [14349] 20100206 wing SRC日结后,控制台输出多次logger重复信息 BUG FIX  
 * 需要删除BasicConfigurator.configure();调用
 */
public class emisRptExcelHelper {

	static Logger logger = Logger.getLogger(emisRptExcelProvider.class
			.getName());

	private HSSFFont initFontFromXml = null;

	public static void setupXmlFont(Node _oTd,
			emisRptExcelProvider xlsProvider, emisRptSection oSection,
			emisRptGroup oGroup, emisRptField _oField) {
		//[14349] 20100206 wing BUG FIX
		//BasicConfigurator.configure();

		HashMap fontPoolMap = xlsProvider.getFontPoolMap();
		HSSFWorkbook xlsWb = xlsProvider.getXlsWb();
		HashMap defaultfontIDmap = xlsProvider.getDefaultfontIDmap();
		HashMap defaultStyleIDmap = xlsProvider.getDefaultStyleIDmap();

		// 字體設定
		// 設置 provider的一些屬性,處理 font,取得color,size ,fontname
		HSSFFont reportRptFont = null;
		String fontId = ""; //$NON-NLS-1$
		String sCellStyleID;

		try {

			fontId = emisRptXML.getAttribute(_oTd, "fontID"); //$NON-NLS-1$
			sCellStyleID = emisRptXML.getAttribute(_oTd, "cellStyleID"); //$NON-NLS-1$
			if (null == fontId || "".equals(fontId)) { //$NON-NLS-1$
				if (null != ((String) defaultfontIDmap.get(oSection.getName()))) {
					_oField.setFontID((String) defaultfontIDmap.get(oSection
							.getName()));
				}
			}
			if (null == sCellStyleID || "".equals(sCellStyleID)) { //$NON-NLS-1$
				if (null != ((String) defaultStyleIDmap.get(oSection.getName()))) {
					_oField.setCellStyleID((String) defaultStyleIDmap
							.get(oSection.getName()));
				}
			}

			if (null != fontId && (!"".equals(fontId))) { //$NON-NLS-1$
				reportRptFont = (HSSFFont) fontPoolMap.get(fontId);
			}
			if (null != reportRptFont) {
				// logger.info("已有字體裝入pool中");
				return;
			}

		} catch (Exception ee) {
			throw new RuntimeException(Messages.getString("emisRptExcelHelper.6") + ee.getMessage()); //$NON-NLS-1$
		}

	}

}