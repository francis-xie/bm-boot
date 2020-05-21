package com.emis.report;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Node;

/**
 * 功能：報表段落類別 說明：提供如表頭、表身、群組等報表段落之定義，並提供相關之服務 <br>
 * wing:2004/10/18改寫，設置EXCEL報表的寬度width 讀入屬性時，要將為NULL，「」等錯誤的數據過慮 [3645]wing
 * 修改報表適應數據轉向 [3645] wing 更新多個java,需要配合本版本使用
 * 
 * <pre>
 *  src\com\emis\report\emisIRptSectionAction.java
 *  src\com\emis\report\emisIRptDyAction.java
 *  src\com\emis\report\emisIRptSectionExAction.java
 * 
 * 
 * 
 *  update:
 * 
 *  src\com\emis\business\emisPrintData.java
 * 
 *  wwwroot\eros\dynamic\emisReportGEM.java
 * 
 * 
 * 
 *  src\com\emis\report\excel\emisRptExcelProvider.java
 * 
 *  src\com\emis\report\excel\emisExcelReportCellStyle.java
 * 
 *  src\com\emis\report\excel\emisRefGetHSSFCellStyle.java
 * 
 *  src\com\emis\report\excel\emisRptExcelHelper.java
 * 
 * 
 * 
 *  src\com\emis\report\emisRptSection.java
 * 
 *  src\com\emis\report\emisRptGroup.java
 * 
 *  src\com\emis\report\emisRptField.java
 * Track+[14524] tommer.xie 2010/04/10 新增列印EXCEL屬性增加圖片列印及td的height屬性，如果設置td的height則以該height來列印
 * 
 * 
 * </pre>
 * 
 * @author James Lee
 * @version 1.00, 07/09/01'
 */
public class emisRptSection {

	private String sName_ = "";
	private String sectionClass = "";// 外部擴展的class名稱
	private int iHeight_ = 0;

	private boolean isSuppress_ = false;

	private boolean isNewPageAfter_ = false;

	private boolean isResetPageNumber_ = false;

	private boolean isSuppressPageBreakFooter_ = false;

	private ArrayList alDataLines_; // collection of Field objects

	private ArrayList alDerivedFields_;

	private ArrayList alResetFields_;

	private emisRptField oSeqNo_ = null;

	private int iTopLimit_ = 0;

	// wing 2005/06/15添加 為MGL,WTN報表使用
	private int ratote = 90; // 180時縱轉橫

	private int rotateMaxColWidth = 0;

	// 用於保存表身的section,每一筆記錄都會產生新的getoutputList();
	// 故需要一LIST保存emisTr數據
	// 在需要時才列印
	// 轉向後的數據橫向較多,幫需要定義換行數據
	// 本屬性為公開,可在emisReportGEM中修改
	public ArrayList allSectionList = new ArrayList();

	// 構造時需要重新newInstance
	public ArrayList computeArray = new ArrayList();

	public int NEW_BLOCK = -1;

	// 用於TITLE輸出，或改寫報表
	private int rotateStartPos = 0;

	public int ROTATE_COUNT = -1;

	private boolean mustPrint = false;

	// 以下屬性定義於TR,用於數據轉向後的TITLE,合計,匯總等運算顯示
	// 目前只支持一次
	HashMap rotateHeaerTitleMap = new HashMap();

	HashMap rotateBlockTitleMap = new HashMap();

	HashMap rotateFooterTitleMap = new HashMap();

	HashMap rotateSeqList = new HashMap();

	public int rotateShowRule = -1;

	// 顯示規則 -1表示不處理,無限顯示,1只顯示一次,2顯示2次,n顯示N次
	// (當groupshow=true時)則不同群組重新計算決定是否顯示
	public boolean groupshow = false;

	public String printPos = "";// :footer,header,blockbreak

	/**
	 * 目的: 建構元
	 */
	public emisRptSection() {
		computeArray = new ArrayList();
		rotateSeqList = new HashMap();
	}

	/**
	 * 目的: 建構元
	 * 
	 * @param iCapacity
	 *            輸出列的預設容量
	 */
	public emisRptSection(int iCapacity) {
		alDataLines_ = new ArrayList();
		computeArray = new ArrayList();
	}

	/**
	 * 目的: 設定屬性
	 * 
	 * @param sPropertyName
	 *            屬性名稱
	 * @param sValue
	 *            屬性值
	 * @return true/false 設定成功與否
	 */
	public boolean setProperty(String sPropertyName, String sValue) {
		boolean _isOk = true;

		if ("Name".equalsIgnoreCase(sPropertyName)) {
			sName_ = sValue;
		} else if ("Height".equalsIgnoreCase(sPropertyName)) {
			iHeight_ = Integer.parseInt(sValue);
		} else if ("Suppress".equalsIgnoreCase(sPropertyName)) {
			if ("TRUE".equalsIgnoreCase(sValue)) {
				isSuppress_ = true;
			} else {
				isSuppress_ = false;
			}
		} else if ("NewPageAfter".equalsIgnoreCase(sPropertyName)) {
			if ("TRUE".equalsIgnoreCase(sValue)) {
				isNewPageAfter_ = true;
			} else {
				isNewPageAfter_ = false;
			}
		} else if ("ResetPageNumber".equalsIgnoreCase(sPropertyName)) {
			if ("TRUE".equalsIgnoreCase(sValue)) {
				isResetPageNumber_ = true;
			} else {
				isResetPageNumber_ = false;
			}
		} else if ("SuppressPageBreak".equalsIgnoreCase(sPropertyName)) {
			if ("TRUE".equalsIgnoreCase(sValue)) {
				isSuppressPageBreakFooter_ = true;
			} else {
				isSuppressPageBreakFooter_ = false;
			}
		} else if ("Rotate".equalsIgnoreCase(sPropertyName)) {
			if ("180".equalsIgnoreCase(sValue)) {
				ratote = 180;
				ROTATE_COUNT = 0;
			} else {
				ratote = 90;
			}
			// 轉向後列的寬度
		} else if ("Ratote".equalsIgnoreCase(sPropertyName)) {
			if ("180".equalsIgnoreCase(sValue)) {
				ratote = 180;
				ROTATE_COUNT = 0;
			} else {
				ratote = 90;
			}
			// 轉向後列的寬度
		} else if ("maxColWidth".equalsIgnoreCase(sPropertyName)) {
			try {
				if (sValue != null && !"".equals(sValue))
					rotateMaxColWidth = Integer.parseInt(sValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("NEW_BLOCK".equalsIgnoreCase(sPropertyName)) {
			try {

				if (sValue != null && !"".equals(sValue))
					NEW_BLOCK = Integer.parseInt(sValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 因之前有些作業使用到NEW_LINE,作用同NEW_BLOCK
		} else if ("NEW_LINE".equalsIgnoreCase(sPropertyName)) {
			try {

				if (sValue != null && !"".equals(sValue))
					NEW_BLOCK = Integer.parseInt(sValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 擴展輸出功能
		} else if ("sectionClass".equalsIgnoreCase(sPropertyName)) {
			if (sValue != null && !"".equals(sValue))
				sectionClass = sValue;

		} else {
			_isOk = false;
		}

		return _isOk;
	}

	/**
	 * 目的: 設定段落名稱
	 * 
	 * @param sName
	 *            段落名稱
	 */
	public void setName(String sName) {
		sName_ = sName;
	}

	/**
	 * 目的: 取得輸出列數
	 * 
	 * @return 輸出列數
	 */
	public int getHeight() {
		if (isSuppress_) {
			return 0;
		} else {
			return iHeight_;
		}
	}

	/**
	 * 目的: 是否抑制輸出動作
	 * 
	 * @return true/false 抑制輸出與否
	 */
	public boolean isSuppress() {
		return isSuppress_;
	}

	/**
	 * 目的: 換新頁時是否跳頁
	 * 
	 * @return true/false 跳頁與否
	 */
	public boolean isNewPageAfter() {
		return isNewPageAfter_;
	}

	/**
	 * 目的: 換新頁時是否頁碼歸零
	 * 
	 * @return true/false 歸零與否
	 */
	public boolean isResetPageNumber() {
		return isResetPageNumber_;
	}

	/**
	 * 目的: 是否抑制PageBreakFooter的輸出動作
	 * 
	 * @return true/false 抑制輸出與否
	 */
	public boolean isSuppressPageBreakFooter() {

		return isSuppressPageBreakFooter_;
	}

	public ArrayList getOutputList() {

		if (this.ratote == 90) {
			return getOutputList("other");
		}
		// return ArrayList of emisTr
		emisTr _oDataLine;
		emisTd _oDataElement;
		ArrayList _alOutputLines = null;
		ArrayList _alFields;
		ArrayList _tdDate;
		emisRptField _oField = null;

		if (alDataLines_ != null) {
			if (!isSuppress()) {
				_alOutputLines = new ArrayList();
				for (int i = rotateStartPos; i < alDataLines_.size(); i++) {
					if (rotateSeqList != null) {
						Object rowObject = rotateSeqList.get(new Integer(i));
						int needI = -1;
						if (rowObject != null) {
							needI = ((Integer) rowObject).intValue();
						}
						if (needI == i) {
							continue;
						}

					}

					_alFields = (ArrayList) alDataLines_.get(i);
					_tdDate = new ArrayList();
					_oDataLine = new emisTr();
					for (int j = 0; j < _alFields.size(); j++) {
						_oField = (emisRptField) _alFields.get(j);
						_oDataElement = new emisTd(_oField.getFormatedString(),
								_oField.getColspan());
						if (rotateMaxColWidth > 0) {
							_oDataElement.setSize(rotateMaxColWidth);
							_oDataElement.setAlign(emisReport.A_LEFT);
						} else {
							_oDataElement.setSize(_oField.getWidth());
							_oDataElement.setAlign(_oField.getIAlignment_());
						}
            //給emisTd設置高度 add by tommer.xie 2010/04/10
            if(_oField.getHeight()!=-1)
              _oDataElement.setHeight(_oField.getHeight());
            // wing5(標記) 10/18改寫，設置EXCEL報表的寬度width
						try {
							_oDataElement.setDataType(_oField.getDataType());
							if (_oField.getDataType() == 1) {
								_oDataElement.setSFormatPattern(_oField
										.getSFormatPattern());
								_oDataElement.setNumber(_oField.getNumber());
							}
							//[4807]報表emisField-->emisTd							
							setSectionExtends(_oDataElement, _oField);						
						} catch (Exception ee) {							
							//ee.printStackTrace();
						}
						_tdDate.add(_oDataElement);
					}
					allSectionList.add(_tdDate);
				}

			}
		}
		// 轉換方向 wing&GAMU修改 2005/06/10
		HashMap dataMap = new HashMap();
		if (allSectionList != null)
			for (int i = 0; i < allSectionList.size(); i++) {
				ArrayList tdList = (ArrayList) allSectionList.get(i);
				if (tdList == null)
					break;
				if (tdList != null)
					if (tdList.size() > 0) {
						for (int jj = 0; jj < tdList.size(); jj++) {
							if (dataMap.get(new Integer(jj)) == null) {
								ArrayList newtdList = new ArrayList();
								dataMap.put(new Integer(jj), newtdList);
							}
							ArrayList newtdList = (ArrayList) dataMap
									.get(new Integer(jj));
							newtdList.add(tdList.get(jj));
						}
					}
			}
		_alOutputLines = new ArrayList();
		_oDataLine = new emisTr();
		if (dataMap != null) {
			for (int i = 0; i < dataMap.size(); i++) {
				ArrayList newList = (ArrayList) dataMap.get(new Integer(i));
				if (newList != null && newList.size() > 0) {
					_oDataLine = new emisTr();
					for (int j = 0; j < newList.size(); j++) {
						_oDataLine.add((emisTd) newList.get(j));
					}
					_alOutputLines.add(_oDataLine);
				}
			}

		}

		computeArray = _alOutputLines;
		return _alOutputLines;
	}

	// 擴展屬性設定
	public void setSectionExtends(emisTd dataElement, emisRptField field) {
		// 可顯示一次顯示excel子報表有特殊處理[4807]
		dataElement.setName(field.getName());
		dataElement.setExceldisplay(field.getExceldisplay());
		dataElement.setNAlign_(field.getIAlignment_());
		field.setIAlignment_(field.getIAlignment_());		
		dataElement.setIAlignment_(field.getIAlignment_());
		dataElement.setFontID(field.getFontID());
		dataElement.setCellStyleID(field.getCellStyleID());
		dataElement.setShow(field.getDyShow());
		dataElement.setSectionName(field.getSectionName());
		
		dataElement.setHeight(field.getHeight());
		dataElement.setSection(field.getSection());//改為INT檢查
		dataElement.setCustid(field.getCustid());
		
	}

	// 列印數據及ＩＳＢＯＯＴＥＲ
	public ArrayList getOutputList(boolean isBlock, int section) {

		if (this.ratote == 90) {
			return getOutputList("other");
		}
		// return ArrayList of emisTr
		emisTr _oDataLine;
		emisTd _oDataElement;
		ArrayList _alOutputLines = null;
		ArrayList _alFields;
		ArrayList _tdDate;
		emisRptField _oField = null;

		if (alDataLines_ != null) {
			if (!isSuppress()) {
				_alOutputLines = new ArrayList();
				for (int i = rotateStartPos; i < alDataLines_.size(); i++) {
					if (rotateSeqList != null) {
						Object rowObject = rotateSeqList.get(new Integer(i));
						int needI = -1;
						Object blockSecn = this.rotateBlockTitleMap.get("block"
								+ String.valueOf(i));
						if (blockSecn != null) {
							needI = ((Integer) rowObject).intValue();
						}
						if (needI != i) {
							continue;
						}
					}

					_alFields = (ArrayList) alDataLines_.get(i);
					_tdDate = new ArrayList();
					_oDataLine = new emisTr();
					for (int j = 0; j < _alFields.size(); j++) {
						_oField = (emisRptField) _alFields.get(j);
						_oDataElement = new emisTd(_oField.getFormatedString(),
								_oField.getColspan());
						if (rotateMaxColWidth > 0) {
							_oDataElement.setSize(rotateMaxColWidth);
							_oDataElement.setAlign(emisReport.A_LEFT);
						} else {
							_oDataElement.setSize(_oField.getWidth());
							_oDataElement.setAlign(_oField.getIAlignment_());
						}
            //給emisTd設置高度 add by tommer.xie 2010/04/10
            if(_oField.getHeight()!=-1)
              _oDataElement.setHeight(_oField.getHeight());
            
            // wing5(標記) 10/18改寫，設置EXCEL報表的寬度width
						try {
							_oDataElement.setDataType(_oField.getDataType());
							if (_oField.getDataType() == 1) {
								_oDataElement.setSFormatPattern(_oField
										.getSFormatPattern());
								_oDataElement.setNumber(_oField.getNumber());
							}
							//[4807]報表emisField-->emisTd
							setSectionExtends(_oDataElement, _oField);

						} catch (Exception ee) {
							//ee.printStackTrace();
						}
						_tdDate.add(_oDataElement);
					}
					allSectionList.add(_tdDate);
				}

			}
		}
		// 轉換方向 wing&GAMU修改 2005/06/10
		HashMap dataMap = new HashMap();
		if (allSectionList != null)
			for (int i = 0; i < allSectionList.size(); i++) {
				ArrayList tdList = (ArrayList) allSectionList.get(i);
				if (tdList == null)
					break;
				if (tdList != null)
					if (tdList.size() > 0) {
						for (int jj = 0; jj < tdList.size(); jj++) {
							if (dataMap.get(new Integer(jj)) == null) {
								ArrayList newtdList = new ArrayList();
								dataMap.put(new Integer(jj), newtdList);
							}
							ArrayList newtdList = (ArrayList) dataMap
									.get(new Integer(jj));
							newtdList.add(tdList.get(jj));
						}
					}
			}
		_alOutputLines = new ArrayList();
		_oDataLine = new emisTr();
		if (dataMap != null) {
			for (int i = 0; i < dataMap.size(); i++) {
				ArrayList newList = (ArrayList) dataMap.get(new Integer(i));
				if (newList != null && newList.size() > 0) {
					_oDataLine = new emisTr();
					for (int j = 0; j < newList.size(); j++) {
						_oDataLine.add((emisTd) newList.get(j));
					}
					_alOutputLines.add(_oDataLine);
				}
			}

		}

		computeArray = _alOutputLines;
		return _alOutputLines;
	}

	/**
	 * 目的: 取得輸出列之資料 wing5(標記) 2004/10/18改寫， <br>
	 * 設置EXCEL報表的寬度width
	 * 
	 * @return 輸出列
	 */
	public ArrayList getOutputList(String other) {
		// return ArrayList of emisTr
		emisTr _oDataLine;
		emisTd _oDataElement;
		ArrayList _alOutputLines = null;
		ArrayList _alFields;
		emisRptField _oField = null;

		if (alDataLines_ != null) {
			if (!isSuppress()) {
				_alOutputLines = new ArrayList();
				for (int i = 0; i < alDataLines_.size(); i++) {
					_alFields = (ArrayList) alDataLines_.get(i);
					_oDataLine = new emisTr();
					for (int j = 0; j < _alFields.size(); j++) {
						_oField = (emisRptField) _alFields.get(j);
						_oDataElement = new emisTd(_oField.getFormatedString(),
								_oField.getColspan());
						// wing5(標記) 10/18改寫，設置EXCEL報表的寬度width
						try {
							_oDataElement.setDataType(_oField.getDataType());
							_oDataElement.setSize(_oField.getWidth());
							_oDataElement.setSFormatPattern(_oField
									.getSFormatPattern());
							if (_oField.getDataType() == 1) {
								_oDataElement.setNumber(_oField.getNumber());
							}
              //給emisTd設置高度 add by tommer.xie 2010/04/10
              if(_oField.getHeight()!=-1)
                _oDataElement.setHeight(_oField.getHeight());
              //[4807]報表emisField-->emisTd
							setSectionExtends(_oDataElement, _oField);
						} catch (Exception ee) {
							// [4807]
							//ee.printStackTrace();
						}
						_oDataLine.add(_oDataElement);
					}
					_alOutputLines.add(_oDataLine);
				}
			}
		}

		return _alOutputLines;
	}

	// 添加合併的合計,匯總欄位等
	public ArrayList getOutputList(boolean isFooter) {
		emisTr _oDataLine;
		emisTd _oDataElement;
		ArrayList _alOutputLines = null;
		ArrayList _alFields;
		ArrayList _tdDate;
		emisRptField _oField = null;

		if (alDataLines_ != null) {
			if (!isSuppress()) {
				_alOutputLines = new ArrayList();
				// System.out.println(alDataLines_.size());
				for (int i = rotateStartPos; i < alDataLines_.size(); i++) {
					// System.out.println("I"+i);
					if (rotateSeqList != null) {
						Object rowObject = rotateSeqList.get(new Integer(i));
						int needI = -1;
						if (rowObject != null) {
							needI = ((Integer) rowObject).intValue();
						}
						Object footerSecn = rotateFooterTitleMap.get("footer"
								+ String.valueOf(i));
						if (footerSecn != null) {
							needI = ((Integer) rowObject).intValue();
							if (needI == i) {
							} else
								continue;
						} else
							continue;

					}

					// wing[4807] del mark
					_alFields = (ArrayList) alDataLines_.get(i);
					_tdDate = new ArrayList();
					_oDataLine = new emisTr();
					for (int j = 0; j < _alFields.size(); j++) {
						_oField = (emisRptField) _alFields.get(j);
						_oDataElement = new emisTd(_oField.getFormatedString(),
								_oField.getColspan());
						if (rotateMaxColWidth > 0) {
							_oDataElement.setSize(rotateMaxColWidth);
							_oDataElement.setAlign(emisReport.A_LEFT);
						} else {
							_oDataElement.setSize(_oField.getWidth());
							_oDataElement.setAlign(_oField.getIAlignment_());
						}
						// wing5(標記) 10/18改寫，設置EXCEL報表的寬度width
						try {
							_oDataElement.setDataType(_oField.getDataType());
							if (_oField.getDataType() == 1) {
								_oDataElement.setSFormatPattern(_oField
										.getSFormatPattern());
								_oDataElement.setNumber(_oField.getNumber());
							}
              //給emisTd設置高度 add by tommer.xie 2010/04/10
              if(_oField.getHeight()!=-1)
                _oDataElement.setHeight(_oField.getHeight());
                            //[4807]報表emisField-->emisTd
							setSectionExtends(_oDataElement, _oField);
						} catch (Exception ee) {
						}
						_tdDate.add(_oDataElement);
					}
					allSectionList.add(_tdDate);
				}

			}
		}
		// 轉換方向 wing&GAMU修改 2005/06/10
		HashMap dataMap = new HashMap();
		if (allSectionList != null)
			for (int i = 0; i < allSectionList.size(); i++) {
				ArrayList tdList = (ArrayList) allSectionList.get(i);
				if (tdList == null)
					break;
				if (tdList != null)
					if (tdList.size() > 0) {
						for (int jj = 0; jj < tdList.size(); jj++) {
							if (dataMap.get(new Integer(jj)) == null) {
								ArrayList newtdList = new ArrayList();
								dataMap.put(new Integer(jj), newtdList);
							}
							ArrayList newtdList = (ArrayList) dataMap
									.get(new Integer(jj));
							newtdList.add(tdList.get(jj));
						}
					}
			}
		_alOutputLines = new ArrayList();
		_oDataLine = new emisTr();
		if (dataMap != null) {
			for (int i = 0; i < dataMap.size(); i++) {
				ArrayList newList = (ArrayList) dataMap.get(new Integer(i));
				if (newList != null && newList.size() > 0) {
					_oDataLine = new emisTr();
					for (int j = 0; j < newList.size(); j++) {
						_oDataLine.add((emisTd) newList.get(j));
					}
					_alOutputLines.add(_oDataLine);
				}
			}

		}

		computeArray = _alOutputLines;
		return _alOutputLines;
	}

	// 添加合併的合計,匯總欄位等
	public ArrayList getOutputList(boolean isHeader, String output) {
		emisTr _oDataLine;
		emisTd _oDataElement;
		ArrayList _alOutputLines = null;
		ArrayList _alFields;
		ArrayList _tdDate;
		emisRptField _oField = null;

		if (alDataLines_ != null) {
			if (!isSuppress()) {
				_alOutputLines = new ArrayList();
				for (int i = rotateStartPos; i < alDataLines_.size(); i++) {

					if (rotateSeqList != null) {
						Object rowObject = rotateSeqList.get(new Integer(i));
						int needI = -1;
						Object headerSecn = rotateHeaerTitleMap.get("header"
								+ String.valueOf(i));
						if (headerSecn != null) {
							needI = ((Integer) rowObject).intValue();
							if (needI == i) {
							} else
								continue;
						} else
							continue;
					}

					_alFields = (ArrayList) alDataLines_.get(i);
					_tdDate = new ArrayList();
					_oDataLine = new emisTr();
					for (int j = 0; j < _alFields.size(); j++) {
						_oField = (emisRptField) _alFields.get(j);
						_oDataElement = new emisTd(_oField.getFormatedString(),
								_oField.getColspan());
						if (rotateMaxColWidth > 0) {
							_oDataElement.setSize(rotateMaxColWidth);
							_oDataElement.setAlign(emisReport.A_LEFT);
						} else {
							_oDataElement.setSize(_oField.getWidth());
							_oDataElement.setAlign(_oField.getIAlignment_());
						}
						// wing5(標記) 10/18改寫，設置EXCEL報表的寬度width
						try {
							_oDataElement.setDataType(_oField.getDataType());
							if (_oField.getDataType() == 1) {
								_oDataElement.setSFormatPattern(_oField
										.getSFormatPattern());
								_oDataElement.setNumber(_oField.getNumber());
							}
              //給emisTd設置高度 add by tommer.xie 2010/04/10
              if(_oField.getHeight()!=-1)
                _oDataElement.setHeight(_oField.getHeight());
              // 可顯示一次顯示excel子報表有特殊處理[4807]
							setSectionExtends(_oDataElement, _oField);
						} catch (Exception ee) {
							//ee.printStackTrace();
						}
						_tdDate.add(_oDataElement);
					}
					allSectionList.add(_tdDate);
				}

			}
		}
		// 轉換方向 wing&GAMU修改 2005/06/10
		HashMap dataMap = new HashMap();
		if (allSectionList != null)
			for (int i = 0; i < allSectionList.size(); i++) {
				ArrayList tdList = (ArrayList) allSectionList.get(i);
				if (tdList == null)
					break;
				if (tdList != null)
					if (tdList.size() > 0) {
						for (int jj = 0; jj < tdList.size(); jj++) {
							if (dataMap.get(new Integer(jj)) == null) {
								ArrayList newtdList = new ArrayList();
								dataMap.put(new Integer(jj), newtdList);
							}
							ArrayList newtdList = (ArrayList) dataMap
									.get(new Integer(jj));
							newtdList.add(tdList.get(jj));
						}
					}
			}
		_alOutputLines = new ArrayList();
		_oDataLine = new emisTr();
		if (dataMap != null) {
			for (int i = 0; i < dataMap.size(); i++) {
				ArrayList newList = (ArrayList) dataMap.get(new Integer(i));
				if (newList != null && newList.size() > 0) {
					_oDataLine = new emisTr();
					for (int j = 0; j < newList.size(); j++) {
						_oDataLine.add((emisTd) newList.get(j));
					}
					_alOutputLines.add(_oDataLine);
				}
			}

		}

		computeArray = _alOutputLines;
		return _alOutputLines;
	}

	public ArrayList getComputerOutput() {
		return computeArray;
	}

	public void setTd(emisTd td) {
		td.setFontID("SMTH_HEADBIG");
		td.setCellStyleID("cellStyleHead");
		td.setColumnSpan(1);
		td.setAlign(1);
		td.setExceldisplay("true");
		td.setSize(20);
	}

	/**
	 * 目的: 加入輸出列之定義
	 * 
	 * @param alDataLine
	 *            輸出列資料
	 */
	public void addLine(ArrayList alDataLine) {
		if (alDataLines_ == null) {
			alDataLines_ = new ArrayList();
		}

		alDataLines_.add(alDataLine);
	}

	/**
	 * 目的: 登錄歸零的欄位(Break 後歸零)
	 * 
	 * @param oField
	 *            欄位物件
	 * @return true/false 歸零與否
	 */
	public boolean registerResetField(emisRptField oField) {

		if (alResetFields_ == null) {
			alResetFields_ = new ArrayList();
		}
		alResetFields_.add(oField);

		return true;
	}

	/**
	 * 目的: 登錄 Derived Field 物件
	 * 
	 * @param iIndex
	 *            處理順位
	 * @param oField
	 *            欄位物件
	 */
	public void registerDerivedField(int iIndex, emisRptField oField) {

		if (alDerivedFields_ == null) {
			alDerivedFields_ = new ArrayList();
		}
		if (iIndex > alDerivedFields_.size()) {
			int _iCnt = iIndex - alDerivedFields_.size() - 1;
			for (int i = 0; i < _iCnt; i++) {
				alDerivedFields_.add(null);
			}
			alDerivedFields_.add(oField);
		} else {
			alDerivedFields_.set(iIndex - 1, oField);
		}
	}

	/**
	 * 目的: 登錄 Derived Field 物件
	 * 
	 * @param oField
	 *            欄位物件
	 */
	public void registerDerivedField(emisRptField oField) {

		if (alDerivedFields_ == null) {
			alDerivedFields_ = new ArrayList();
		}
		alDerivedFields_.add(oField);

	}

	/**
	 * 目的: 登錄前 N 名條件的欄位物件
	 * 
	 * @param oField
	 *            欄位物件
	 * @param iLimit
	 *            排名邊界值
	 */
	public void registerTopField(emisRptField oField, int iLimit) {

		oSeqNo_ = oField;
		iTopLimit_ = iLimit;

	}

	/**
	 * 目的: 是否在前 N 名次內
	 * 
	 * @return true/false 排名內與否
	 */
	public boolean isTopN() {
		boolean _isTopN;

		if (oSeqNo_ == null || iTopLimit_ == 0) {
			_isTopN = true;
		} else {
			if (oSeqNo_.getNumber() >= iTopLimit_) {
				_isTopN = false;
			} else {
				_isTopN = true;
			}
		}

		return _isTopN;
	}

	/**
	 * 目的: 執行欄位歸零動作
	 */
	public void resetFields() {
		emisRptField _oField;

		if (alResetFields_ != null) {
			for (int i = 0; i < alResetFields_.size(); i++) {
				_oField = (emisRptField) alResetFields_.get(i);
				_oField.setContent(0);
			}
		}
	}

	/**
	 * 目的: 執行欄位歸零動作(轉向後的欄位歸零動作)
	 */
	public void resetFields(boolean isBlock) {
		emisRptField _oField;

		if (alResetFields_ != null) {
			for (int i = 0; i < alResetFields_.size(); i++) {
				_oField = (emisRptField) alResetFields_.get(i);
				_oField.setContent(0);
			}
		}
	}

	/**
	 * 目的: 執行 Derived Field 之處理
	 */
	public void processDerivedFields() {
		emisRptField _oField;

		if (alDerivedFields_ != null) {
			for (int i = 0; i < alDerivedFields_.size(); i++) {
				_oField = (emisRptField) alDerivedFields_.get(i);
				if (_oField != null) {
					_oField.getResult();
				}
			}
		}
	}

	// [3645]添加TITLE顯示
	/**
	 * 目的:用於定義相對穩定TITLE轉向後的輸出
	 */
	public void setRotateTitle(Node oTr, int rowPos, String showPos) {
		rotateSeqList.put(new Integer(rowPos), new Integer(rowPos));// Key
		String groupshow = "false";
		groupshow = emisRptXML.getAttribute(oTr, "groupshow");
		if (showPos.equals("header")) {
			rotateHeaerTitleMap.put("header" + String.valueOf(rowPos), "header"
					+ String.valueOf(rowPos));
			if (groupshow != null) {
				if ("TRUE".equalsIgnoreCase("groupshow")) {
					rotateHeaerTitleMap.put("groupshow"
							+ String.valueOf(groupshow), "true");
				}
			}
			rotateHeaerTitleMap.put("groupshowPos" + String.valueOf(showPos),
					"groupshowPos" + String.valueOf(showPos));

		} else if (showPos.equals("footer")) {
			this.rotateFooterTitleMap.put("footer" + String.valueOf(rowPos),
					"footer" + String.valueOf(rowPos));

		} else if (showPos.equals("block")) {
			this.rotateBlockTitleMap.put("block" + String.valueOf(rowPos),
					"block" + String.valueOf(rowPos));
		}

	}

	/**
	 * @return Returns the sName_.
	 */
	public String getName() {
		return sName_;
	}

	/**
	 * wing修改，可動態增刪Section，據列印要求確定,未提交至CVS
	 * 
	 * @return
	 */
	public ArrayList getAlDataLines_() {
		return alDataLines_;
	}

	public void setAlDataLines_(ArrayList alDataLines_) {
		this.alDataLines_ = alDataLines_;
	}

	public ArrayList getAllSectionList() {
		return allSectionList;
	}

	public void setAllSectionList(ArrayList allSectionList) {
		this.allSectionList = allSectionList;
	}

	// 180時縱轉橫
	public int getRatote() {
		return ratote;
	}

	public void setRatote(int ratote) {
		this.ratote = ratote;
	}

	public int getRotateStartPos() {
		return rotateStartPos;
	}

	public void setRatoteStartPos(int rotateStartPos) {
		this.rotateStartPos = rotateStartPos;
	}

	public boolean isMustPrint() {
		return mustPrint;
	}

	public void setMustPrint(boolean mustPrint) {
		this.mustPrint = mustPrint;
	}

	public String getSectionClass() {
		return sectionClass;
	}

	public void setSectionClass(String sectionClass) {
		this.sectionClass = sectionClass;
	}

}
