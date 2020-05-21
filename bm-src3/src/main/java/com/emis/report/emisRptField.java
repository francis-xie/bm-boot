package com.emis.report;

import java.text.DecimalFormat;

/**
 * 功能：報表欄位類別 說明：報表欄位之基礎類別(Base class)，提供基本屬性之設定及讀取功能， 以及格式輸出之實作
 * [4807]wing 20056/01/04 修改權限
 * @author James Lee & Jacky Hsiue
 * @version 1.00, 07/09/01'
 * Track+[14524] tommer.xie 2010/04/10 新增列印EXCEL屬性增加圖片列印及td的height屬性，如果設置td的height則以該height來列印
 * 2010/04/30 sunny 增加段落重排的水平對齊方式 justify
 */
abstract public class emisRptField {

	private String sName_ = "";

	private int iType_ = 1;

	private int iWidth_ = 0;

	private int iAlignment_ = 1; // emisReport.A_LEFT;

	private boolean isSuppress_ = false;

	private boolean isSuppressIfDuplicate_ = false;

	private boolean isSuppressIfZero_ = false;

	private emisRptFormula oFormula_ = null;

	private DecimalFormat oFormat_ = null;

	private String sDataField_ = null;

	private emisRptField oSeqNo_ = null;

	private int iTopLimit_ = 0;

	private boolean isTrim_ = true; // $ 91.9.17 新增

	public static final int NUMBER = 1;

	public static final int TEXT = 2;

	public static final int SEPARATOR = 3;

	public static final int DATE = 4;

  //新增圖片類型
  public static final int IMG = 5;

	private int colspan = 1; // wing default setting為1

	private int dataType = TEXT;// STRING

	private String exceldisplay = emisReport.SBOOLEAN_TRUE; // 是否在MUTIRPT中需要顯示

	private String fontID = emisReport.SEXCEL_DEFALUTFONTID;

	private String cellStyleID = emisReport.SEXCEL_DEFALUTSTYLE;

	// 格式輸出
	private String sFormatPattern;

	private boolean isGroup = false;

	private String dyShow = emisReport.SBOOLEAN_TRUE; // 添加此屬性，方便程式動態決定是否顯示,與pmode有所區別

	private String action = "";

	// [3645] wing add
	private boolean dyGen = false;

	private String dySrcField = "";
	private String sectionName = "none"; // 所屬SECTION
	private int section = emisReport.SECTION_NONE;// 所屬SECTION NONE	
	private int custid = 100;// 不處理任何事項 100表示PERSON/100處理
	private float height = -1;// 高度 -1表示不處理

	/**
	 * 目的: 建構元
	 * 
	 * @param 無
	 * @return 無
	 */
	public emisRptField() {
	}

	/**
	 * 目的: 傳回欄位型態，需由子類別實作
	 * 
	 * @param 無
	 * @return 欄位型態代碼
	 */
	abstract public int myType();

	/**
	 * 目的: 傳回欄位內容，需由子類別實作
	 * 
	 * @param 無
	 * @return 內容字串
	 */
	abstract public String getContent();

	/**
	 * 目的: 檢查欄位值是否換新，需由子類別實作
	 * 
	 * @param 無
	 * @return true/false
	 */
	abstract public boolean isNewValue();

	/**
	 * 目的: 檢查欄位值是否換新SupressIfDuplicate專用判斷，需由子類別實作
	 *
	 * @return true/false
	 */
	abstract public boolean isSupressDuplicateNewValue();

	/**
	 * 目的: 傳回欄位的數值內容，需由子類別實作
	 * 
	 * @param 無
	 * @return double 型態的數值
	 */
	abstract public double getNumber();

	/**
	 * 目的: 設定欄位值，需由子類別實作
	 * 
	 * @param iContent
	 *            int 型態的數值
	 * @return 無
	 */
	abstract public void setContent(int iContent);

	/**
	 * 目的: 設定欄位值，需由子類別實作
	 * 
	 * @param lContent
	 *            long 型態的數值
	 * @return 無
	 */
	abstract public void setContent(long lContent);

	/**
	 * 目的: 設定欄位值，需由子類別實作
	 * 
	 * @param fContent
	 *            float 型態的數值
	 * @return 無
	 */
	abstract public void setContent(float fContent);

	/**
	 * 目的: 設定欄位值，需由子類別實作
	 * 
	 * @param dContent
	 *            double 型態的數值
	 * @return 無
	 */
	abstract public void setContent(double dContent);

	/**
	 * 目的: 設定欄位值，需由子類別實作
	 * 
	 * @param sContent
	 *            String 型態的字串值
	 * @return 無
	 */
	abstract public void setContent(String sContent);

	/**
	 * 目的: 暫存欄位值，需由子類別實作
	 * 
	 * @param iContent
	 *            int 型態的數值
	 * @return 無
	 */
	abstract public void cacheContent(int iContent);

	/**
	 * 目的: 暫存欄位值，需由子類別實作
	 * 
	 * @param lContent
	 *            long 型態的數值
	 * @return 無
	 */
	abstract public void cacheContent(long lContent);

	/**
	 * 目的: 暫存欄位值，需由子類別實作
	 * 
	 * @param fContent
	 *            float 型態的數值
	 * @return 無
	 */
	abstract public void cacheContent(float fContent);

	/**
	 * 目的: 暫存欄位值，需由子類別實作
	 * 
	 * @param dContent
	 *            double 型態的數值
	 * @return 無
	 */
	abstract public void cacheContent(double dContent);

	/**
	 * 目的: 暫存欄位值，需由子類別實作
	 * 
	 * @param sContent
	 *            String 型態的字串值
	 * @return 無
	 */
	abstract public void cacheContent(String sContent);

	/**
	 * 目的: commit 暫存值，需由子類別實作
	 * 
	 * @param 無
	 * @return 無
	 */
	abstract public void commitContent();

	/**
	 * 目的: 設定欄位屬性值 wing modify: set colspan
	 * 
	 * @param sPropertyName
	 *            屬性名稱
	 * @param sValue
	 *            屬性內容
	 * @return true/false 成功與否
	 */
	public boolean setProperty(String sPropertyName, String sValue) {
		boolean _isOk = true;

		if ("NAME".equalsIgnoreCase(sPropertyName)) {
			sName_ = sValue;
		} else if ("ALIGN".equalsIgnoreCase(sPropertyName)) {
			if ("LEFT".equalsIgnoreCase(sValue)) {
				iAlignment_ = emisReport.A_LEFT;
			} else if ("CENTER".equalsIgnoreCase(sValue)) {
				iAlignment_ = emisReport.A_CENTER;
			} else if ("RIGHT".equalsIgnoreCase(sValue)) {
				iAlignment_ = emisReport.A_RIGHT;
			} else if( "FILL".equalsIgnoreCase(sValue)){
        iAlignment_ = emisReport.A_FILL;
      } else if( "justify".equalsIgnoreCase(sValue)){
        iAlignment_ = emisReport.A_JUSTIFY;
      }
		} else if ("WIDTH".equalsIgnoreCase(sPropertyName)) {
			// if (sValue.indexOf("@") < 0 && sValue.indexOf("@") < 0)
			iWidth_ = Integer.parseInt(sValue);
		} else if ("SUPPRESS".equalsIgnoreCase(sPropertyName)) {
			if ("TRUE".equalsIgnoreCase(sValue)) {
				isSuppress_ = true;
			} else {
				isSuppress_ = false;
			}
		} else if ("SUPPRESSIFDUPLICATE".equalsIgnoreCase(sPropertyName)) {
			if ("TRUE".equalsIgnoreCase(sValue)) {
				isSuppressIfDuplicate_ = true;
			} else {
				isSuppressIfDuplicate_ = false;
			}
		} else if ("SUPPRESSIFZERO".equalsIgnoreCase(sPropertyName)) {
			if ("TRUE".equalsIgnoreCase(sValue)) {
				isSuppressIfZero_ = true;
			} else {
				isSuppressIfZero_ = false;
			}
		} else if ("FORMAT".equalsIgnoreCase(sPropertyName)) {
      if( sValue != null ) { // Robert, add null check track 17406
        setFormatPattern(sValue);
        int _idx = sValue.indexOf(".");
        if (_idx >= 0) {
          this.dataType = NUMBER;
        } else {
          this.dataType = NUMBER;
        }
      }
			if ("US".equalsIgnoreCase(sValue) || "TW".equalsIgnoreCase(sValue) )
				this.dataType = DATE;
		} else if ("CONTENT".equalsIgnoreCase(sPropertyName)) {
			setContent(sValue);
		} else if ("DATAFIELD".equalsIgnoreCase(sPropertyName)) {
			setDataField(sValue);
		} else if ("TOP".equalsIgnoreCase(sPropertyName)) {
			iTopLimit_ = Integer.parseInt(sValue);
		} else if ("TRIM".equalsIgnoreCase(sPropertyName)) {
			// $ 91.9.17 新增
			if ("TRUE".equalsIgnoreCase(sValue)) {
				isTrim_ = true;
			} else {
				isTrim_ = false;
			}
			// wing修改這裡
		} else if ("COLSPAN".equalsIgnoreCase(sPropertyName)) {
			colspan = Integer.parseInt(sValue);
		} else if ("exceldisplay".equalsIgnoreCase(sPropertyName)) {
			// wing modify here
			if (null == sValue || "".equals(sValue))
				sValue = "true";
			exceldisplay = sValue;
			// 字體設定，有需要設定PDF及DEFAULT字體
			// reportFont name="Arial_Normal" isDefault="true" fontName="Arial"
			// size="8" isBold="fals
		}
		// [wing]3645,用於報表改寫,不同步PMODE(因為PMODE在解析XML後不起作用),
		// DYSHOW設計願意在於在隨時決定是否顯示(EXCEL用統一於原EXCELDISPLAY=:TRUE:FALSE
		// dyshow計劃在xls,txt中設置為動態顯示,不佔用空間,與supress有所區別
		else if ("DyShow".equalsIgnoreCase(sPropertyName)) {
			this.dyShow = sValue;

		} else if ("action".equalsIgnoreCase(sPropertyName)) {
			// System.out.println("action"+":"+action);
			action = sValue;
		} else if ("fontID".equalsIgnoreCase(sPropertyName)) {
			fontID = sValue;
			// forecolor
		} else if ("cellStyleID".equalsIgnoreCase(sPropertyName)) {
			cellStyleID = sValue;
			// forecolor
		} else if ("datatype".equalsIgnoreCase(sPropertyName)) {
			// wing modify here
			if ("date".equalsIgnoreCase(sValue)) {
				this.dataType = TEXT;
			} else if ("number".equalsIgnoreCase(sValue)) {
				this.dataType = NUMBER;
			} else if ("date".equalsIgnoreCase(sValue)) {
				this.dataType = DATE;
				// 99在excel中表示為無內容輸出,但創建一個HSSFCell
			}  else if ("img".equalsIgnoreCase(sValue)) {
				this.dataType = IMG;
				// 99在excel中表示為無內容輸出,但創建一個HSSFCell
			} else if ("99".equalsIgnoreCase(sValue)) {
				this.dataType = 99;
				// wing 預留類型及接口,功能待後實現(各個provider內處理)
				// 98在excel中表示為公式
			} else if ("98".equalsIgnoreCase(sValue)) {
				this.dataType = 98;
			} else if ("97".equalsIgnoreCase(sValue)) {
				this.dataType = 97;
			} else if ("96".equalsIgnoreCase(sValue)) {
				this.dataType = 96;
			} else if ("95".equalsIgnoreCase(sValue)) {
				this.dataType = 95;
			} else if ("94".equalsIgnoreCase(sValue)) {
				this.dataType = 94;
			} else
				dataType = TEXT;
		} else if ("type".equalsIgnoreCase(sPropertyName)) {
			if ("separator".equalsIgnoreCase(sValue))
				dataType = emisRptField.SEPARATOR;
			// [3645]
		} else if ("dyGen".equalsIgnoreCase(sPropertyName)) {
			if ("true".equalsIgnoreCase(sValue))
				dyGen = true;
			else
				dyGen = false;
			// [3645]
		} else if ("dySrcField".equalsIgnoreCase(sPropertyName)) {
			dySrcField = sValue;
		} else if ("persent".equalsIgnoreCase(sPropertyName)) {
			this.custid = 100;
		} else if ("custid".equalsIgnoreCase(sPropertyName)) {
			try {
				custid = Integer.parseInt(sValue);
			} catch (Exception cue) {
				custid = 100;
			}
		// [4807] wingadd,擴展屬性,excel
		} else if ("height".equalsIgnoreCase(sPropertyName)) {
			try {
				height = Float.parseFloat(sValue);
			} catch (Exception cue) {
				height = -1; // -1表示不處理
			}

		} else {
			_isOk = false;
		}

		return _isOk;
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

	/**
	 * 目的: 檢查計算公式是否正確
	 * 
	 * @param oReportInfo
	 *            emisReportInfo 物件
	 * @return true/false 正確與否
	 */
	public boolean justifyFormula(emisReportInfo oReportInfo) {
		boolean _isValid;

		if (hasFormula()) {
			_isValid = oFormula_.justifyFormula(this, oReportInfo);
		} else {
			_isValid = true; //
		}

		return _isValid;
	}

	/**
	 * 目的: 檢查是否設定計算公式
	 * 
	 * @param 無
	 * @return true/false 存在與否
	 */
	public boolean hasFormula() {
		boolean _hasFormula = true;

		if (oFormula_ == null) {
			_hasFormula = false;
		}

		return _hasFormula;
	}

	/**
	 * 目的: 取得欄位寬度
	 * 
	 * @param 無
	 * @return 欄寬數值
	 */
	public int getWidth() {
		if (isSuppress_) {
			return 0;
		} else {
			return iWidth_;
		}
	}

	/**
	 * 目的: 檢查是否列印的屬性
	 * 
	 * @param 無
	 * @return true/false 列印與否
	 */
	public boolean isSuppress() {
		return isSuppress_;
	}

	/**
	 * 目的: 檢查內容與前筆資料相同時，是否列印之屬性
	 * 
	 * @param 無
	 * @return true/false 列印與否
	 */
	public boolean isSuppressIfDuplicate() {
		return isSuppressIfDuplicate_;
	}

	/**
	 * 目的: 檢查內容數值為零時，是否列印之屬性
	 * 
	 * @param 無
	 * @return true/false 列印與否
	 */
	public boolean isSuppressIfZero() {
		return isSuppressIfZero_;
	}

	/**
	 * 目的: 取得格式內容，本方法配合 getFormatedString() 格式化字串之輸出 ，供該方法呼叫
	 * 
	 * @param 無
	 * @return 格式內容字串
	 */
	protected String getFormatContent() {
		return getContent();
	}

	/**
	 * 目的: 取得格式化字串，受 Suppress, SuppressIfDuplicate, SuppressIfZero, Width,
	 * Format, Align 等影響
	 * 
	 * @param 無
	 * @return 格式化字串
	 */
	public String getFormatedString() {
		String _sContent = "";
		String _sFormatedString;
		boolean _isDoFormating = true;
		// 取得 content
		_sContent = getFormatContent();
		_sFormatedString = _sContent;

		if (_sContent == null) {
			_sFormatedString = emisString.space(iWidth_);
			_isDoFormating = false;
		} else if (isSuppress()) {
			_sFormatedString = "";
			_isDoFormating = false;
		} else if (isSuppressIfDuplicate()) {
      //因為報表表身列印方式都是採列印前一筆的方式，例如 目前DataSrc已經跑到第三筆
      //才印出第二筆之資料。isSuppressIfDuplicate比較時因為要印的內容是第二筆
      // 所以需用sContent變數(目前記錄的前一筆內容) 去跟  sLastContent變數(目前記錄的前前一筆內容)比較，
      // 但是GROUPHEADER isBreak()也會透過也會過 isNewValue()判斷目前的記錄是否達到群組條件，
      // 因此為了不影響原isBreak的判斷，固新增一個 isSupressDuplicateNewValue()僅供isSuppressIfDuplicate判斷
			if (! isSupressDuplicateNewValue()) {
				_sFormatedString = emisString.space(iWidth_);
				_isDoFormating = false;
			}
		} else if (isSuppressIfZero()) {
			if (getNumber() == 0.0) {
				_sFormatedString = emisString.space(iWidth_);
				_isDoFormating = false;
			}
		}
		if (_isDoFormating) {
			if (oFormat_ != null) {
				_sFormatedString = oFormat_.format(getNumber());
			}

			if (iWidth_ == 0) {
				// $ 91.9.17 新增
				if (isTrim_)
					_sFormatedString = _sFormatedString.trim();
			} else {
				if (myType() == NUMBER) {
					if (_sFormatedString.length() > iWidth_) {
						_sFormatedString = emisString.replicate("*", iWidth_);
					}
				}
				if (iAlignment_ == emisReport.A_LEFT) {
					_sFormatedString = emisString.rPadB(_sFormatedString,
							iWidth_);
				} else if (iAlignment_ == emisReport.A_RIGHT) {
					_sFormatedString = emisString.lPadB(_sFormatedString,
							iWidth_);
				} else if (iAlignment_ == emisReport.A_CENTER) {
					_sFormatedString = emisString.cPadB(_sFormatedString,
							iWidth_);
				} else if (iAlignment_ == emisReport.A_FILL) {
					_sFormatedString = emisString.rPadB(_sFormatedString,
							iWidth_);
				} else if (iAlignment_ == emisReport.A_JUSTIFY) {
					_sFormatedString = emisString.rPadB(_sFormatedString,
							iWidth_);
        }
			}
		}

		// 若 isSuppressifDuplicate ， 若值重複輸出空白
		// 若 isSuppressifZero ， 若值為 0 輸出空白
		// 上述情況不成立，依照 sFormat_, iAlignment, iWidth 的定義，輸出字串
		return _sFormatedString;
	}

	/**
	 * 目的: 設定欄位輸出樣式
	 * 
	 * @param sFormatPattern
	 *            樣式字串
	 * @return 無
	 */
	public void setFormatPattern(String sFormatPattern) {
		if (oFormat_ == null) {
			this.sFormatPattern = sFormatPattern;
			oFormat_ = new DecimalFormat(sFormatPattern);
		} else {

			this.sFormatPattern = sFormatPattern;
			oFormat_.applyPattern(sFormatPattern);
		}

		return;
	}

	/**
	 * 目的: 取得內容值，若本物件存在計算公式之屬性，會先計算後再輸出結果
	 * 
	 * @param 無
	 * @return double型態之數值
	 */
	public double getResult() {

		// 檢查是否存在 Formula
		if (hasFormula()) {
			if (isTopN()) {
				oFormula_.getResult();
			}
		}

		return getNumber();
	}

	/**
	 * 目的: 設定計算公式
	 * 
	 * @param oLeftOperand
	 *            左邊運算子物件
	 * @param o1stOperand
	 *            右邊第一個運算子物件
	 * @param iOperator
	 *            運算元代碼
	 * @param o2ndOperand
	 *            右邊第二個運算子物件
	 * @return 無
	 */
	public void setFormula(emisRptField oLeftOperand, emisRptField o1stOperand,
			int iOperator, emisRptField o2ndOperand) {

		if (oFormula_ == null) {
			oFormula_ = new emisRptFormula();
		}
		oFormula_.setFormula(oLeftOperand, o1stOperand, iOperator, o2ndOperand);
	}

	/**
	 * 目的: 列出計算公式字串
	 * 
	 * @param 無
	 * @return 無
	 */
	public void listFormula() {
		oFormula_.listFormula();
	}

	/**
	 * 目的: 設定計算公式
	 * 
	 * @param sFormula
	 *            公式字串
	 * @param oReportInfo
	 *            emisReportInfo物件
	 * @return true/false 成功與否
	 */
	public boolean setFormula(String sFormula, emisReportInfo oReportInfo) {
		if (oFormula_ == null) {
			oFormula_ = new emisRptFormula();
		}

		return oFormula_.parseFormula(this, sFormula, oReportInfo);
	}

	/**
	 * 目的: 設定資料源之欄位名稱
	 * 
	 * @param sDataField
	 *            資料源之欄名
	 * @return 無
	 */
	public void setDataField(String sDataField) {
		sDataField_ = sDataField;
	}

	/**
	 * 目的: 取得資料源之欄位名稱
	 * 
	 * @param 無
	 * @return 資料源之欄名
	 */
	public String getDataField() {
		return sDataField_;
	}

	public void setTopField(emisRptField oField) {
		oSeqNo_ = oField;
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
			if (oSeqNo_.getNumber() > iTopLimit_) {
				_isTopN = false;
			} else {
				_isTopN = true;
			}
		}

		return _isTopN;
	}

	/**
	 * @return Returns the colspan.
	 */
	public int getColspan() {
		return colspan;
	}

	/**
	 * @param colspan
	 *            The colspan to set.
	 */
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}

	/**
	 * @return Returns the iType_.
	 */
	public int getIType_() {
		return iType_;
	}

	/**
	 * @param type_
	 *            The iType_ to set.
	 */
	public void setIType_(int type_) {
		iType_ = type_;
	}

	/**
	 * @return Returns the dataType.
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            The dataType to set.
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
	 * @return Returns the font.
	 */
	public String getFontID() {
		return fontID;
	}

	/**
	 * @param font
	 *            The font to set.
	 */
	public void setFontID(String fontID) {
		this.fontID = fontID;
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
	 * @return Returns the oFormula_.
	 */
	public emisRptFormula getOFormula_() {
		return oFormula_;
	}

	/**
	 * @return Returns the sFormatPattern.
	 */
	public String getSFormatPattern() {
		return sFormatPattern;
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

	public String getDyShow() {
		return dyShow;
	}

	public void setDyShow(String dyShow) {
		this.dyShow = dyShow;
	}

	/**
	 * 擴展功能實現,實現interface 兩種目錄:報表或web-inf\classes
	 * 
	 * @return
	 */
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isDyGen() {
		return dyGen;
	}

	public void setDyGen(boolean dyGen) {
		this.dyGen = dyGen;
	}

	public String getDySrcField() {
		return dySrcField;
	}

	public void setDySrcField(String dySrcField) {
		this.dySrcField = dySrcField;
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
}