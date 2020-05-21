package com.emis.report.excel;

import com.emis.messageResource.Messages;
import com.emis.business.emisBusiness;
import com.emis.business.emisDataSrc;
import com.emis.business.emisHttpServletResponse;
import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileFactory;
import com.emis.file.emisFileMgr;
import com.emis.report.emisProviderEventListener;
import com.emis.report.emisReport;
import com.emis.report.emisRightsCheck;
import com.emis.report.emisRptProvider;
import com.emis.report.emisRptXML;
import com.emis.report.emisTd;
import com.emis.report.emisTr;
import com.emis.user.emisUser;
import com.emis.util.emisUtil;
import com.emis.util.emisXMLUtl;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.Region;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.imageio.ImageIO;

import java.io.FileOutputStream;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.awt.image.BufferedImage;

/**
 * Created on 2004/10/20
 *
 * @author wing
 *         <p>
 *         <br>
 *         初步完成日期 2004/10/29<br>
 *         產生EXCEL類型的報表,便於數據統計 <br>
 *         說明:因excel對字體，邊框風格支持數輸小（在幾K以內，需要重用已產生的對象解決報表生成) <br>
 *         單個excel文件應該於3M,數據量輸大時，請分開列印，這是現有Java EXCEL解決方案均存在的問題 <br>
 *         在printTd的基礎上,再分出兩個printCell的方法,並據原報表需求,可以打列整行 <br>
 *         v1.2 initExcePageSetting()node為空則返回的處理 <br>
 *
 * <pre>
 *
 *  v1.3 添加報表無任何excel報表定義時亦可輸出,添加變量 isNoExcelWithRow=true;
 *
 * </pre>
 *
 * wing 修改日期: 2005/06/03 <br>
 *         據MGL專案報表提升EXCEL報表輸出 <br>
 *         據業界報表類介，提出報表分類， <br>
 *         即簡單報表的典型報表實做 <br>
 *         複雜報表的典型報表實做
 *         </p>
 *         <pre>
 *         wing 修改日期：2005/06/09 在printCell中添加對//99表示只創建，沒有VALUE
 *               [3645] wing添加處理公式欄位,其它數據類型處理
 *                      整理代碼及添加多一個構造函數
 *
 *        有效EROS版本2.0
 *           wing[4545] 2005/11/13 wing and mike add  pagebreak func.
 *           前端jsp or servelt添加parameter參數:EXCEL_PH_TITLE(頁眉列印INFO,英文)
 *                                             EXCEL_PF_TITLE(頁尾列印INFO,英文)
 *
 *        </pre>
 *
 */
/**
 * <pre>
 *
 *         其它說明:
 *         bug:未修改
 *         當
 * <tr computeMutiColumnsWidth="true">
 *         內使用的是VAR變量,可能出錯誤或不存在即:在EXCEL中少A column....
 *         POIFS(POI Filesystem)              通用API
 *         HSSF(Horrible Spreadsheet Format)  Excel用
 *                 討厭的試算表格式
 *                 微軟使某些原本簡單的事情過分複雜，
 *                 同時又過分簡單地處理了某些原本需要靈活性的事情，
 *                 讓人不勝佩服！
 *                 HSSF建立在POIFS的基礎上
 *
 *         HDF(Horrible Document Format)      Word用
 *         HPSF(Horrible Property Set Format)
 *
 *            [3645] wing添加處理公式欄位,其它數據類型處理
 *                   整理代碼及添加多一個構造函數
 * </pre>
 * 2005/12/12 [4656] wing review [wayke] 據EPOS需求,12月中甸前完成功能:
 *    突破POI對XLS支持有限文檔大小4MB,當查詢數據量較大時,
 *    修改報表CORE對同一查詢數據產生連續的XLS文檔S,並同時提供多個XLS文檔DOWN的功能
 * 2006/01/04  [4807] wing wrep,一頁式報表實做
 *   Track+[13874] dana.gao 2009/12/02 修改當字體>10時,表格高度不能隨字體自動調整問題.
 */
/**
 * @author wing
 * @version [14349] 20100206 wing SRC日結后,控制台輸出多次logger重复信息 BUG FIX
 * 需要刪除BasicConfigurator.configure();調用
 * 2010/04/30 sunny 增加段落重排的水平對齊方式 justify
 *
 * Track+[14524] tommer.xie 2010/04/10 新增列印EXCEL屬性增加圖片列印及td的height屬性，如果設置td的height則以該height來列印
 * Track+[14814] dana.gao 2010/05/05 增加設定表格backgroundcolor功能.
 * 可在xml的cellstyle中設定FillBackgroundColor="ROSE",需配合FillPattern一同使用
 * 若FillPattern="NO_FILL"或"SOLID_FOREGROUND"等屬性時,FillBackgroundColor會沒有效果
 * Track+[14893] dana.gao 2010/05/17 增加excel報表頁邊距設置.
 * Track+[14986] ken.tang 2010/06/21 調整excel報表頁邊距.
 *
 */
public class emisRptExcelProvider implements emisRptProvider {
	protected String sReportMode_;

	protected String sModeAttr_ = "pmode"; //$NON-NLS-1$

	public static final int NUMBER = 1;

	public static final int TEXT = 2;

	public static final int SEPARATOR = 3;

	public static final int DATE = 4;

  //新增圖片類型 by tommer.xie 
  public static final int IMG = 5;

	protected Element eRoot_;

	protected Hashtable oDataSrc = new Hashtable();

	protected int nWidth_ = 74;

	protected int nHeight_ = 30;

	protected Properties oProp = new Properties();

	protected int nPageNum_ = 1;

	protected emisBusiness oBusiness_;

	protected HttpServletRequest oRequest_;

	protected int nCurrentRowNum_;

	protected emisProviderEventListener listener_;

	protected ServletContext oApplication_;

	protected emisUser oUser_;

	// 建新的Excel 工作簿
	// 是否有數據產生
	protected boolean hasDataOutput = false;

	boolean isTrRunTimeWrong = false;

	int copies = 1;

	// excel總行數累加器
	// wing 20050622修改為公開,方便取得當前處理ROW
	public int allRowCount_ = -1;

  //記錄前一次列印IMG的單元行
  public int allRowCountOld_ = -1;

  public HSSFPatriarch patriarch =null;

  protected int splitPaneRowsCount = 0;

	// excel當前列累加器,用於產生新的column及region columns
	protected int columnCount_ = 0;

	// 最大列數,據列數計算不足最大列的表格,並計算寬度
	protected int maxColumns_ = 0;

	// 應該在這裡宣稱，POI的FAQ有寫到:對於big Data如果只在FOR等直接宣稱，有可能出MANY。。。的錯誤
	protected HSSFWorkbook xlsWb = null;

	protected HSSFSheet sheet = null;

	protected HSSFRow row = null;

	protected HSSFCell cell = null;

	protected HSSFCellStyle xlsStyle = null;

	// 字體，大小，COLOR等設定
	protected HSSFCellStyle defaultXlsStyle = null;

	// cell所使用的風格
	protected HSSFCellStyle currentStyle = null;

	protected HSSFFont defaultRptFont = null;

	// 報表列印使用的字體
	protected HSSFFont mutiRptFont = null;

	// 定制格式輸出
	protected HSSFDataFormat format = null;

	protected String fontName = Messages.getString("emisRptExcelProvider.1"); //$NON-NLS-1$

	protected String defaultFontName = Messages.getString("emisRptExcelProvider.0"); //$NON-NLS-1$

	protected String excelFileName = ""; //$NON-NLS-1$

	// split color設定
	protected String sSplitPaneBackGroupColor = ""; //$NON-NLS-1$

	protected int fontHeight = 10; // 字體高度

	// keep 設定每列寬度的行
	protected ArrayList columnDataRowSettingList = new ArrayList();

	// 因col不同行的字體的大小不一,需要計算最大字體的寬度
	protected ArrayList columnMaxFontIDList = new ArrayList();

	// 預先定義幾種字體
	protected HashMap fontPoolMap = null;

	// 字體不同,也需要不同的顯示風格
	protected HashMap xlsStyleMap = null;

	// echo column cellStyle setting
	protected HashMap cellStyleMap = null;

	// 用於設定TD內的字體
	protected HashMap defaultfontIDmap = new HashMap();

	// 用於設定TD內的style
	protected HashMap defaultStyleIDmap = new HashMap();

	protected ArrayList errorMsgList = new ArrayList();

	// since v1.3
	protected boolean isNoExcelWithRow = true; // 沒有報表定義亦可輸出

	protected int counter = 0;

	// wing 列印功能加強 2005/06/03
	// HSSFPrintSetup ps
	protected HSSFPrintSetup ps;

	// 如無定義需在createXlsRptZip後獲
	protected String _xlsServerFileName = null; // 可供外部取得生成的xls文件名,方便連接及改寫

	protected String printOrient = "L"; // excel初始化為L列印 //$NON-NLS-1$

	// 日誌輸出
	static Logger logger = Logger.getLogger(emisRptExcelProvider.class
			.getName());

	// [3645]由前端決定是否提供標準行設定(EXCEL_REGION_ROW)
	protected boolean isRegionRow = true;

	//wing review[wayke] [4656]
	public HashMap fileNameHM = new HashMap(); //server 端Excel文件名容器
	public final int MAX_TR_SIZE = 6000;          //Excel文件最大列印記錄數
	public int iCount = 1;                     //Excel文件名序列變量
	public boolean hasNewPage_ = false ;       //是否需要拆分文件輸出標誌位
	public int _allRowForDPage = -1;           //整次文件輸出總記錄數
	public emisProp ep = null;                 //列印環境屬性對像
    public boolean havePrintData = true;       //剛好拆頁輸出後無列印記錄標誌位
    public int iLargeFileSize = 0;

    public boolean isPrintPageHeader = false;  //是否正在列印文件頭標誌位
    public int curPageHeaderLine = 0;          //頭列印的當前頭行數
    public boolean isPrintPageFooter = false;  //是否正在列印文件頭Footer 標誌位
    public boolean isNewPageLater = false;     //是否推遲分頁
    public String sInPageHorF = "";            //當前列印所處位置　三種值　inPageheader,inPagefooter,"" //$NON-NLS-1$

  //Track[13481] 2009/09/22 sunny.zhuang 15F按分組分工作表所需要之變量
  public int sheetNum=0;

  public int allRowCount2_=0;
  
  public int allRowCountOld2_=0;

  public int columnCount2_=0;

  public HSSFRow row2 = null;

  public HSSFCell cell2 = null;

  private double dLeftMargin = -1;
  private double dRightMargin = -1;
  private double dBottomMargin = -1;
  private double dTopMargin = -1;

  private short shScacle = -1;

  private double dTrans = 2.53;

  // wing add 200701013 end when -1<value : non set
  // vince add 20070117 start
  private double dPageHead = -1;

  private double dPageFooter = -1;

  private String sFooterAlign;
  private String sFooterFormat;
  private String sFooterMsg;

  // 提供給N報表連接使用
	public emisRptExcelProvider(emisBusiness oBusiness, Element eRoot,
			HSSFWorkbook xlsWb) throws Exception {
		this.xlsWb = xlsWb;
		//[14349] 20100206 wing mark
		//BasicConfigurator.configure();

		oBusiness_ = oBusiness;
		eRoot_ = eRoot;
		oUser_ = oBusiness_.getUser();
		oApplication_ = oBusiness_.getContext();
		oRequest_ = oBusiness_.getRequest();
    
		emisProp _oProp = emisProp.getInstance(oApplication_);
		initPROP(_oProp);
		initSourceMutiRpt(_oProp);
		_xlsServerFileName = null;

		ep = _oProp;
		getLargeFileSize();
	}

	/**
	 * @param oBusiness
	 * @param eRoot
	 *            xml
	 * @throws Exception
	 */
	public emisRptExcelProvider(emisBusiness oBusiness, Element eRoot)
			throws Exception {
		//BasicConfigurator.configure();
		xlsWb = new HSSFWorkbook();
		oBusiness_ = oBusiness;
		eRoot_ = eRoot;
		oUser_ = oBusiness_.getUser();
		oApplication_ = oBusiness_.getContext();
		oRequest_ = oBusiness_.getRequest();
		emisProp _oProp = emisProp.getInstance(oApplication_);
		initPROP(_oProp);
		initMutiRpt(_oProp);

		_xlsServerFileName = null;
		// 字體頁面初始化計完畢;
		ep = _oProp;
		getLargeFileSize();
	}

	 public void getLargeFileSize() {
    try {
      // 文件大小可以由前端指定，update by andy 2008/08/14
      if(oRequest_.getParameter("EXCEL_MAXFILESIZE") != null && !"".equals(oRequest_.getParameter("EXCEL_MAXFILESIZE"))){
        iLargeFileSize = Integer.parseInt(oRequest_.getParameter("EXCEL_MAXFILESIZE"));
      } else {
        iLargeFileSize = Integer.parseInt(ep.get("EXCEL_MAXFILESIZE"));
      }
    } catch (Exception e) {
      iLargeFileSize = -1;
    }
    if (iLargeFileSize <= 0) {
      iLargeFileSize = 3700000;
    }
  }

	public void initPROP(emisProp _oProp) throws Exception {

		String _sHeight = emisXMLUtl.getAttribute(eRoot_, "height"); //$NON-NLS-1$
		// $ 91.07.27 修改可至 emisprop 讀取

    String LeftMargin =  oRequest_.getParameter("EXCEL_LEFT_MARGIN");
    String RightMargin =  oRequest_.getParameter("EXCEL_RIGHT_MARGIN");
    String TopMargin =  oRequest_.getParameter("EXCEL_TOP_MARGIN");
    String BottomMargin =  oRequest_.getParameter("EXCEL_BOTTOM_MARGIN");

    try {
      if (LeftMargin != null && !"".equals(LeftMargin)) {
        dLeftMargin = Double.parseDouble(LeftMargin)/2.54;
      }
      if (RightMargin != null && !"".equals(RightMargin)) {
        dRightMargin = Double.parseDouble(RightMargin)/2.54;
      }
      if (TopMargin != null && !"".equals(TopMargin)) {
        dTopMargin = Double.parseDouble(TopMargin)/2.54;
      }
      if (BottomMargin != null && !"".equals(BottomMargin)) {
        dBottomMargin = Double.parseDouble(BottomMargin)/2.54;
      }
    } catch (Exception e) {
    }
		// [3645]
		String sRegionRow = oRequest_.getParameter("EXCEL_REGION_ROW"); //$NON-NLS-1$
		try {
			if (sRegionRow == null || "".equals(sRegionRow)) //$NON-NLS-1$
				sRegionRow = (String) oBusiness_
						.getAttribute("EXCEL_REGION_ROW"); //$NON-NLS-1$
		} catch (Exception e) {
		}
		if (sRegionRow == null || "".equals(sRegionRow)) //$NON-NLS-1$
			sRegionRow = "true"; //$NON-NLS-1$
		if (sRegionRow.equals("false")) //$NON-NLS-1$
			isRegionRow = false;
		else
			isRegionRow = true;

		String _sValue = ""; //$NON-NLS-1$
		if (_sHeight != null) {
			try {
				nHeight_ = Integer.parseInt(_sHeight);
			} catch (Exception ignore) {
			}
		} else {
			// $ 91.07.27 若高度未設定, 修改可至 emisprop 讀取
			try {
				_sValue = _oProp.get("EPOS_RPT_PHEIGHT"); //$NON-NLS-1$
				if (_sValue != null) {
					nHeight_ = Integer.parseInt(_sValue);
				}
			} catch (Exception e) {
			}
		}

		// $ 91.07.27 修改可至 emisprop 讀取
		String _sOrient = emisXMLUtl.getAttribute(eRoot_, "orient"); //$NON-NLS-1$
		if ("P".equalsIgnoreCase(_sOrient)) { //$NON-NLS-1$
			// 直印設定
			try {
				printOrient = "P"; //$NON-NLS-1$
				_sValue = _oProp.get("EPOS_RPT_PHEIGHT"); //$NON-NLS-1$
				if (_sValue != null) {
					nHeight_ = Integer.parseInt(_sValue);
				}
			} catch (Exception e) {
			}
		} else if ("L".equalsIgnoreCase(_sOrient)) { //$NON-NLS-1$
			// 橫印設定
			try {
				printOrient = "L"; //$NON-NLS-1$
				_sValue = _oProp.get("EPOS_RPT_LHEIGHT"); //$NON-NLS-1$
				if (_sValue != null) {
					nHeight_ = Integer.parseInt(_sValue);
				}
			} catch (Exception e) {
			}
		}else{
      try {
				printOrient = "L"; //$NON-NLS-1$
				_sValue = _oProp.get("EPOS_RPT_LHEIGHT"); //$NON-NLS-1$
				if (_sValue != null) {
					nHeight_ = Integer.parseInt(_sValue);
				}
			} catch (Exception e) {
			}
    }

		oBusiness_.debug("height:" + nHeight_); //$NON-NLS-1$
		String _sWidth = emisXMLUtl.getAttribute(eRoot_, "width"); //$NON-NLS-1$

		if (_sWidth != null) {
			try {
				nWidth_ = Integer.parseInt(_sWidth);
			} catch (Exception ignore) {
			}
		}

		oBusiness_.debug("width:" + nWidth_); //$NON-NLS-1$

		NodeList nlist = eRoot_.getChildNodes();
		if (nlist != null) {
			int len = nlist.getLength();
			for (int i = 0; i < len; i++) {
				Node n = nlist.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE)
					continue;
				Element e = (Element) n;
				String _sName = n.getNodeName();
				if ("datasrc".equals(_sName)) { //$NON-NLS-1$
					emisDataSrc oSrc = new emisDataSrc(oBusiness_, e);
					oDataSrc.put(oSrc.getId(), oSrc);
				} else if ("property".equals(_sName)) { //$NON-NLS-1$
					loadProperty(e);
				}
			}
		}
	}

	/**
	 * wing :Excel報表init設定 已有一個HSSFWOKR存在
	 *
	 * @param _oProp
	 */
	public void initSourceMutiRpt(emisProp _oProp) {
		sReportMode_ = getParameter("PROP_MODE"); //$NON-NLS-1$
		sheet = xlsWb.getSheetAt(0);
		initStyleAndFontMap(_oProp);


	}

	/**
	 * wing :Excel報表init設定
	 *
	 * @param _oProp
	 */
	public void initMutiRpt(emisProp _oProp) {
		sReportMode_ = getParameter("PROP_MODE"); //$NON-NLS-1$
		sheet = xlsWb.createSheet();
    //新建工作薄的時候就新創建一個圖片生成對象
    patriarch=sheet.createDrawingPatriarch();
    // wing添加xls sheet 名稱從前端獲得
		initStyleAndFontMap(_oProp);

	}

	public void initStyleAndFontMap(emisProp _oProp) {
		String sheetName = getParameter("EXCEL_SHEET_NAME"); //$NON-NLS-1$
		if (null == sheetName || "".equals(sheetName)) //$NON-NLS-1$
			sheetName = "emisXls"; //$NON-NLS-1$

		// (Short)1==ENCODING_UTF_16 中文指定
		xlsWb.setSheetName(0, sheetName);
		// 取系統default Fontname,color,hieght,size
		defaultRptFont = xlsWb.createFont();
		String _sValue = ""; //$NON-NLS-1$
		try {
      // wing add 20070113 start
      //dTrans
      _sValue = _oProp.get("EXCEL_TRANSMARGIN");
      if ((_sValue != null) && (!"".equals(_sValue))) {
        this.dTrans = Double.parseDouble(_sValue);
      }
      _sValue = _oProp.get("EXCEL_LEFTMARGIN");
      if ((_sValue != null) && (!"".equals(_sValue))) {
        this.dLeftMargin = Double.parseDouble(_sValue) / dTrans;
      }
      _sValue = _oProp.get("EXCEL_RIGHTMARGIN");
      if ((_sValue != null) && (!"".equals(_sValue))) {
        this.dRightMargin = Double.parseDouble(_sValue) / dTrans;
      }

      _sValue = _oProp.get("EXCEL_TOPMARGIN");
      if ((_sValue != null) && (!"".equals(_sValue))) {
        this.dTopMargin = Double.parseDouble(_sValue) / dTrans;
      }

      _sValue = _oProp.get("EXCEL_BOTTOMMARGIN");
      if ((_sValue != null) && (!"".equals(_sValue))) {
        this.dBottomMargin = Double.parseDouble(_sValue) / dTrans;
      }
      // wing add 20070113 end
      //vince add 20070117 start
       _sValue = _oProp.get("EXCEL_PH_MARGIN");
      if ((_sValue != null) && (!"".equals(_sValue))) {
        this.dPageHead = Double.parseDouble(_sValue) / dTrans;
      }
        _sValue = _oProp.get("EXCEL_PF_MARGIN");
      if ((_sValue != null) && (!"".equals(_sValue))) {
        this.dPageFooter = Double.parseDouble(_sValue) / dTrans;
      }
      //vince add 20070117 end
			_sValue = _oProp.get("MUTI_RPT_FONT"); //$NON-NLS-1$
			if ((_sValue != null) && (!"".equals(_sValue))) { //$NON-NLS-1$
				defaultRptFont.setFontName(_sValue.trim());
			}
			_sValue = _oProp.get("MUTI_RPT_FONTHEIGHT"); //$NON-NLS-1$
			// 字體高度
			try {
				int fonthieghts = 10;
				if ((_sValue != null) && (!"".equals(_sValue))) { //$NON-NLS-1$
					fonthieghts = Integer.parseInt(_sValue);
				}
				defaultRptFont.setFontHeightInPoints((short) fonthieghts);
			} catch (Exception numE) {
				this.hasDataOutput = false;
				errorMsgList.add(Messages.getString("emisRptExcelProvider.41") + numE.getMessage() + " !\n"); //$NON-NLS-1$ //$NON-NLS-2$
				throw new RuntimeException(Messages.getString("emisRptExcelProvider.43") + numE.getMessage() //$NON-NLS-1$
						+ " !\n"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			this.hasDataOutput = false;
			errorMsgList.add(Messages.getString("emisRptExcelProvider.45") + e.getMessage() + " !\n"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.47") + e.getMessage() + " !\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		mutiRptFont = defaultRptFont;
		defaultXlsStyle = xlsWb.createCellStyle();
		defaultXlsStyle.setFont(mutiRptFont);

		xlsStyle = defaultXlsStyle;
		fontPoolMap = new HashMap();
		fontPoolMap.put("defaultFontID", mutiRptFont); //$NON-NLS-1$
		xlsStyleMap = new HashMap();
		// 已有default字體,所以需要加defaultFontID
		xlsStyleMap.put("defaultCellStyledefaultFontID1", xlsStyle); //$NON-NLS-1$
		xlsStyle = defaultXlsStyle;
		fontPoolMap.put("defaultFontID", mutiRptFont); //$NON-NLS-1$
		xlsStyleMap.put("defaultCellStyledefaultFontID2", xlsStyle); //$NON-NLS-1$
		xlsStyle = defaultXlsStyle;
		fontPoolMap.put("defaultFontID", mutiRptFont); //$NON-NLS-1$
		xlsStyleMap.put("defaultCellStyledefaultFontID3", xlsStyle); //$NON-NLS-1$
		cellStyleMap = new HashMap();
	}

	private void loadProperty(Element property) {
		NodeList nlist = property.getChildNodes();
		if (nlist != null) {
			int len = nlist.getLength();
			for (int i = 0; i < len; i++) {
				Node n = nlist.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE)
					continue;
				Element e = (Element) n;
				String _sNodeName = e.getNodeName();
				String _sNodeValue = emisXMLUtl.getElementValue(e);
				if (_sNodeValue != null) {
					oProp.put(_sNodeName, _sNodeValue);
					oBusiness_.debug("set prop:" + _sNodeName + "=" //$NON-NLS-1$ //$NON-NLS-2$
							+ _sNodeValue);
				}
			}
		}
	}

	/**
	 * 初始化字體設定,報表行數,SHEET打印頁數設計
	 *
	 * @param _oReport
	 */
	public void rptGEMinitProvider(Node _oReport) {

		// 初始化,EmisPROP沒有定義,因為這是定義一個無邊無框,無COLOR的
		// 提供default風格
		emisExcelReportCellStyle initReportCellStyle = new emisExcelReportCellStyle();
		initReportCellStyle.setCellStyleID("defaultCellStyle"); //$NON-NLS-1$
		initReportCellStyle.setSetFillPattern(HSSFCellStyle.NO_FILL);
		cellStyleMap.put("defaultCellStyle", initReportCellStyle); //$NON-NLS-1$

		// excel列印設定
		Node oNode = searchSection(_oReport, "reportmode"); //$NON-NLS-1$
		initExcelReport(oNode, "RM"); //$NON-NLS-1$
		initExcePageSetting(oNode);
		oNode = searchSection(_oReport, "reportheader"); //$NON-NLS-1$
		initExcelReport(oNode, "RH"); //$NON-NLS-1$
		oNode = searchSection(_oReport, "pageheader"); //$NON-NLS-1$
		initExcelReport(oNode, "PH"); //$NON-NLS-1$
		oNode = searchSection(_oReport, "groupheader"); //$NON-NLS-1$
		initExcelReport(oNode, "GH"); //$NON-NLS-1$
		oNode = searchSection(_oReport, "detail"); //$NON-NLS-1$
		initExcelReport(oNode, "DETAIL"); //$NON-NLS-1$
		oNode = searchSection(_oReport, "groupfooter"); //$NON-NLS-1$
		initExcelReport(oNode, "PG"); //$NON-NLS-1$
		oNode = searchSection(_oReport, "pagefooter"); //$NON-NLS-1$
		initExcelReport(oNode, "PF"); //$NON-NLS-1$
		oNode = searchSection(_oReport, "pagebreak"); //$NON-NLS-1$
		initExcelReport(oNode, "PB"); //$NON-NLS-1$
		oNode = searchSection(_oReport, "reportfooter"); //$NON-NLS-1$
		initExcelReport(oNode, "RF"); //$NON-NLS-1$
		componetCellStyleAndFont();
	}

	public void computeExcelSlitPane(Node _oReport) {
		searchSection(_oReport, "reportmode"); //$NON-NLS-1$
	}

	/**
	 * 合併一行
	 *
	 * @param lineStr
	 * @param td
	 */
	public void printTd(String lineStr, emisTd td) {

		int iStartPos = 0;
		int columnNumber = this.maxColumns_;
		for (int i = 0; i < columnNumber; i++) {
			cell = row.createCell((short) (iStartPos));
			String valuess = lineStr;
			if (null == valuess) {
				valuess = ""; //$NON-NLS-1$
			}
			int nCellAlgin = 1;
			try {
				nCellAlgin = td.getNAlign_();
			} catch (Exception ee) {
				//logger.info(ee.getMessage());
				nCellAlgin = 1;
			}
			currentStyle = getXlsStyleMap(td.getCellStyleID(), td.getFontID(),
					nCellAlgin);
			if (null == currentStyle) {
				logger.debug(Messages.getString("emisRptExcelProvider.79") + td.getCellStyleID() //$NON-NLS-1$
						+ "  fontID=" + td.getFontID()); //$NON-NLS-1$
				this.hasDataOutput = false;
				this.errorMsgList.add(Messages.getString("emisRptExcelProvider.81") //$NON-NLS-1$
						+ td.getCellStyleID() + "  fontID=" + td.getFontID()); //$NON-NLS-1$
				throw new RuntimeException(Messages.getString("emisRptExcelProvider.83")); //$NON-NLS-1$
			}
			// 可中文輸出指定
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell.setCellStyle(currentStyle);
			cell.setCellValue(valuess);
			// 合併計算
			iStartPos++;
		}

		// 由前台參數決定是否合併
		setRowHeight(td);
		sheet.addMergedRegion(new Region(allRowCount_, (short) 0, allRowCount_,
				(short) (iStartPos - 1)));

	}
  public void printTd(String lineStr, emisTd td,HSSFSheet sheet_) {

		int iStartPos = 0;
		int columnNumber = this.maxColumns_;
		for (int i = 0; i < columnNumber; i++) {
			cell2 = row2.createCell((short) (iStartPos));
			String valuess = lineStr;
			if (null == valuess) {
				valuess = ""; //$NON-NLS-1$
			}
			int nCellAlgin = 1;
			try {
				nCellAlgin = td.getNAlign_();
			} catch (Exception ee) {
				//logger.info(ee.getMessage());
				nCellAlgin = 1;
			}
			currentStyle = getXlsStyleMap(td.getCellStyleID(), td.getFontID(),
					nCellAlgin);
			if (null == currentStyle) {
				logger.debug(Messages.getString("emisRptExcelProvider.79") + td.getCellStyleID() //$NON-NLS-1$
						+ "  fontID=" + td.getFontID()); //$NON-NLS-1$
				this.hasDataOutput = false;
				this.errorMsgList.add(Messages.getString("emisRptExcelProvider.81") //$NON-NLS-1$
						+ td.getCellStyleID() + "  fontID=" + td.getFontID()); //$NON-NLS-1$
				throw new RuntimeException(Messages.getString("emisRptExcelProvider.83")); //$NON-NLS-1$
			}
			// 可中文輸出指定
//			cell2.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell2.setCellStyle(currentStyle);
			cell2.setCellValue(valuess);
			// 合併計算
			iStartPos++;
		}

		// 由前台參數決定是否合併
		setRowHeight(td);
		sheet_.addMergedRegion(new Region(allRowCount2_, (short) 0, allRowCount2_,
				(short) (iStartPos - 1)));

	}

  public void setRowHeight(emisTd td) {
		// TODO Auto-generated method stub
		HSSFCellStyle checkStyle = getXlsStyleMap(td.getCellStyleID(), td
				.getFontID(), td.getNAlign_());
		if (null != checkStyle) {
			short hpos = xlsWb.getFontAt(checkStyle.getFontIndex())
					.getFontHeightInPoints();
      hpos = computFontHeight(hpos);
      //抽成公用方法
//			if (hpos >= 35) {
//				hpos += 11;
//			} else if (hpos >= 25) {
//				hpos += 9;
//			} else if (hpos >= 17) {
//				hpos += 8;
//			} else if (hpos >= 13) {
//				hpos += 5;
//			} else if (hpos >= 11) {
//				hpos += 4;
//			}

			if (hpos >= 11)
				sheet.getRow(allRowCount_).setHeightInPoints(hpos);
      //給工作薄單元行設置高度 add by tommer.xie 2010/04/10
      if(td.getHeight()!=-1)
        sheet.getRow(allRowCount_).setHeightInPoints(td.getHeight());
    }
	}

   public void setRowHeight(emisTd td, short heighterTd) {
    if (td == null)
      return;// no set
    short hpos;
    if (heighterTd != -1) {
      hpos = heighterTd;
      sheet.getRow(allRowCount_).setHeightInPoints(hpos);
    } else {
      HSSFCellStyle checkStyle = getXlsStyleMap(td.getCellStyleID(), td
          .getFontID(), td.getNAlign_());
      hpos = xlsWb.getFontAt(checkStyle.getFontIndex())
          .getFontHeightInPoints();
      hpos = computFontHeight(hpos);
      sheet.getRow(allRowCount_).setHeightInPoints(hpos);
    }

  }

  public void setRowHeight(emisTd td,HSSFSheet sheet_) {
		// TODO Auto-generated method stub
		HSSFCellStyle checkStyle = getXlsStyleMap(td.getCellStyleID(), td
				.getFontID(), td.getNAlign_());
		if (null != checkStyle) {
			short hpos = xlsWb.getFontAt(checkStyle.getFontIndex())
					.getFontHeightInPoints();
			if (hpos >= 35) {
				hpos += 11;
			} else if (hpos >= 25) {
				hpos += 9;
			} else if (hpos >= 17) {
				hpos += 8;
			} else if (hpos >= 13) {
				hpos += 5;
			} else if (hpos >= 11) {
				hpos += 4;
			}
			if (hpos >= 11)
				sheet_.getRow(allRowCount2_).setHeightInPoints(hpos);
      //給工作薄單元行設置高度 add by tommer.xie 2010/04/10
      if(td.getHeight()!=-1)
        sheet_.getRow(allRowCount2_).setHeightInPoints(td.getHeight());
    }
	}

  /**
	 * 打印整行的資料 多列合併 regionColumn==1,現在設為1行
	 */
	public void printTd(emisTd td, int regionColumn) {
		regionColumn = 1;
		if (td == null)
			return;
		// 佔多少列getOutputList,只佔一列
		int columnNumber = td.getNColumnSpan_();
		if (columnNumber <= 1) {
			printTd(td);
			printCell(td);
			columnCount_++;
			sheet.addMergedRegion(new Region(this.allRowCount_,
					(short) (columnCount_ - 1), allRowCount_,
					(short) (columnCount_)));

		} else {
			int iStartPosition = columnCount_;
			for (int i = 0; i < columnNumber; i++) {
				printCell(td);
				columnCount_++;
			}
			columnCount_++;
			printCell(td);

			sheet.addMergedRegion(new Region(allRowCount_,
					(short) iStartPosition, allRowCount_,
					(short) (iStartPosition + columnNumber)));
			columnCount_--;
		}
	}

	public void printTd(emisTd td) {
		if (td == null)
			return;

		// 佔多少列getOutputList
		int columnNumber = td.getNColumnSpan_();
		// 不用合併
		if (columnNumber <= 1) {
			printCell(td);
		} else {
			// 多列時，需要KEEP開始列至結束列，以便合併多列
			int iStartPosition = columnCount_;
			for (int i = 0; i < columnNumber; i++) {
				printCell(td);
				columnCount_++;
			}
			// 合併
			sheet.addMergedRegion(new Region(this.allRowCount_,
					(short) iStartPosition, allRowCount_,
					(short) (iStartPosition + columnNumber - 1)));
			columnCount_--;
		}

	}
  public void printTd(emisTd td,HSSFSheet sheet_) {
		if (td == null)
			return;

		// 佔多少列getOutputList
		int columnNumber = td.getNColumnSpan_();
		// 不用合併
		if (columnNumber <= 1) {
			printCell(td,sheet_);
		} else {
			// 多列時，需要KEEP開始列至結束列，以便合併多列
			int iStartPosition = columnCount2_;
			for (int i = 0; i < columnNumber; i++) {
				printCell(td,sheet_);
				columnCount2_++;
			}
			// 合併
			sheet_.addMergedRegion(new Region(this.allRowCount2_,
					(short) iStartPosition, allRowCount2_,
					(short) (iStartPosition + columnNumber - 1)));
			columnCount2_--;
		}

	}
	/**
	 * *列印VALUE
	 */
	public void printCell(emisTd td) {
		String values = td.toString();
		if (null == values) {
			values = ""; //$NON-NLS-1$
		}

		printCell(td, values);
	}
  public void printCell(emisTd td,HSSFSheet sheet_) {
		String values = td.toString();
		if (null == values) {
			values = ""; //$NON-NLS-1$
		}

		printCell(td, values,sheet_);
	}
	/**
	 * 列印VALUE及字體風格設定 具體內容輸出,有數字類型轉換
	 */
	public void printCell(emisTd td, String inVvalue) {

		if (null == inVvalue) {
			inVvalue = ""; //$NON-NLS-1$
		}
		if (td == null)
			return;

		int nCellAlgin = 1;
		try {
			nCellAlgin = td.getNAlign_();
		} catch (Exception ee) {
			//logger.debug(ee.getMessage());
			nCellAlgin = 1;
		}

		currentStyle = getXlsStyleMap(td.getCellStyleID(), td.getFontID(),
				nCellAlgin);
    if(null == currentStyle){
      currentStyle = (HSSFCellStyle) this.xlsStyleMap.get("defaultCellStyledefaultFontID1");
    }
    if (null == currentStyle) {
			logger.debug(Messages.getString("emisRptExcelProvider.86") + td.getCellStyleID() //$NON-NLS-1$
					+ "  fontID=" + td.getFontID()); //$NON-NLS-1$
			errorMsgList.add(Messages.getString("emisRptExcelProvider.88") + td.getCellStyleID() //$NON-NLS-1$
					+ "  fontID=" + td.getFontID()); //$NON-NLS-1$
			hasDataOutput = false;
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.90")); //$NON-NLS-1$
		}

		cell = row.createCell((short) (columnCount_));
		// 中文處理
//		cell.setEncoding(HSSFCell.ENCODING_UTF_16);
		currentStyle.setAlignment((short) getXlsAlignment(td.getNAlign_()));

		//[4807]報表動態權限實作
		if(td.getCustid()==emisReport.REPORT_NODISPLAY){
		  if(!emisRightsCheck.getShowSet(this.oBusiness_, td.getName())){
			  String _sValue=emisRightsCheck.getShowSetVal(oBusiness_, td.getName());

			  if(td.getDataType()==emisReport.REPORT_NUMBER){
				  if(_sValue==null||"".equals(_sValue)){
					  _sValue="0";
				  }
				  td.setDataType(emisReport.REPORT_NUMBER);
				  td.setContent(_sValue);
				  double dValue=Double.parseDouble(_sValue);
				  td.setNumber(dValue);
			  }else{
				  td.setDataType(emisReport.REPORT_TEXT);
			  }
			  inVvalue=_sValue;
		 }else  td.setDataType(emisReport.REPORT_NODISPLAY);
		}
		// 將輸出改為switch...case結構
		switch (td.getDataType()) {
		case TEXT:
			format = xlsWb.createDataFormat();
			if (td.getSFormatPattern() != null)  {
        if (currentStyle.getDataFormat() != HSSFDataFormat
              .getBuiltinFormat("text")) {
				currentStyle.setDataFormat(format.getFormat(td
						.getSFormatPattern()));
        }
      }
			cell.setCellStyle(currentStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			// 格式指定
			cell.setCellValue(inVvalue.trim());
			break;
		case NUMBER:
		    //wing Remark
			// 格式指定[4807]
			format = xlsWb.createDataFormat();
			if (td.getSFormatPattern() != null) {
				if (td.getSFormatPattern().indexOf('%') != -1) {
					currentStyle.setDataFormat(HSSFDataFormat
							.getBuiltinFormat("0.00%")); //$NON-NLS-1$
					if (td.getNumber() != 0)
					  td.setNumber(td.getNumber() / 100);
				} else {
          if (currentStyle.getDataFormat() != HSSFDataFormat
                .getBuiltinFormat("text"))
					  currentStyle.setDataFormat(format.getFormat(td
							.getSFormatPattern()));
        }
			}
			cell.setCellStyle(currentStyle);
			// 數字類型輸出
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			cell.setCellValue(td.getNumber());
			break;
		case DATE:
			format = xlsWb.createDataFormat();
			if (td.getSFormatPattern() != null)
				currentStyle.setDataFormat(format.getFormat(td
						.getSFormatPattern()));
			cell.setCellStyle(currentStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			// 格式指定
			cell.setCellValue(inVvalue.trim());
			break;
    case IMG:
			try{
        if(allRowCountOld_!=allRowCount_){
          ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
          BufferedImage bufferImg = ImageIO.read(new File(oApplication_.getRealPath(td.getContent().trim())));
          ImageIO.write(bufferImg,"jpg",byteArrayOut);
//          HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, bufferImg.getWidth(), bufferImg.getHeight(), (short) 0, allRowCount_, (short) 3, allRowCount_+3);
          HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, td.getSize(), (int)td.getHeight(), cell.getCellNum(), allRowCount_, (short)(cell.getCellNum()+td.getNColumnSpan_()), allRowCount_+1);
          patriarch.createPicture(anchor, xlsWb.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
          allRowCountOld_=allRowCount_;
        }
      }catch(Exception e){
        e.printStackTrace();
      }
			break;
		case emisReport.REPORT_NODISPLAY:
			// 99表示只創建，沒有VALUE
			cell.setCellStyle(currentStyle);

			break;
		case 98:
			// 數字類型輸出
			cell.setCellStyle(currentStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			// 格式指定

			cell.setCellValue(inVvalue.trim());
			// 數字類型輸出
			break;
		case 97:
			// 數字類型輸出
			cell.setCellStyle(currentStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			// 格式指定

			cell.setCellValue(inVvalue.trim());
			// 公式輸出
			cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
			cell.setCellFormula(td.getContent());
			cell.setCellStyle(currentStyle);
			break;
		case 96:
			// 替代符號
			cell.setCellStyle(currentStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(td.getSFormatPattern());
			break;
		default:
			// 格式指定
			format = xlsWb.createDataFormat();
			if (td.getSFormatPattern() != null) {
          if (currentStyle.getDataFormat() != HSSFDataFormat
              .getBuiltinFormat("text")) {
            currentStyle.setDataFormat(format.getFormat(td
                .getSFormatPattern()));
          }
          currentStyle.setDataFormat(HSSFDataFormat
              .getBuiltinFormat("text"));
        }
			// 字符串格式,未轉換日期類型
			cell.setCellStyle(currentStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(inVvalue.trim());
		}

	}
  public void printCell(emisTd td, String inVvalue,HSSFSheet sheet_) {

		if (null == inVvalue) {
			inVvalue = ""; //$NON-NLS-1$
		}
		if (td == null)
			return;

		int nCellAlgin = 1;
		try {
			nCellAlgin = td.getNAlign_();
		} catch (Exception ee) {
			//logger.debug(ee.getMessage());
			nCellAlgin = 1;
		}

		currentStyle = getXlsStyleMap(td.getCellStyleID(), td.getFontID(),
				nCellAlgin);
    if(null == currentStyle){
      currentStyle = (HSSFCellStyle) this.xlsStyleMap.get("defaultCellStyledefaultFontID1");
    }
    if (null == currentStyle) {
			logger.debug(Messages.getString("emisRptExcelProvider.86") + td.getCellStyleID() //$NON-NLS-1$
					+ "  fontID=" + td.getFontID()); //$NON-NLS-1$
			errorMsgList.add(Messages.getString("emisRptExcelProvider.88") + td.getCellStyleID() //$NON-NLS-1$
					+ "  fontID=" + td.getFontID()); //$NON-NLS-1$
			hasDataOutput = false;
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.90")); //$NON-NLS-1$
		}

		cell2 = row2.createCell((short) (columnCount2_));
		// 中文處理
//		cell2.setEncoding(HSSFCell.ENCODING_UTF_16);
		currentStyle.setAlignment((short) getXlsAlignment(td.getNAlign_()));

		//[4807]報表動態權限實作
		if(td.getCustid()==emisReport.REPORT_NODISPLAY){
		  if(!emisRightsCheck.getShowSet(this.oBusiness_, td.getName())){
			  String _sValue=emisRightsCheck.getShowSetVal(oBusiness_, td.getName());

			  if(td.getDataType()==emisReport.REPORT_NUMBER){
				  if(_sValue==null||"".equals(_sValue)){
					  _sValue="0";
				  }
				  td.setDataType(emisReport.REPORT_NUMBER);
				  td.setContent(_sValue);
				  double dValue=Double.parseDouble(_sValue);
				  td.setNumber(dValue);
			  }else{
				  td.setDataType(emisReport.REPORT_TEXT);
			  }
			  inVvalue=_sValue;
		 }else  td.setDataType(emisReport.REPORT_NODISPLAY);
		}
		// 將輸出改為switch...case結構
		switch (td.getDataType()) {
		case TEXT:
			format = xlsWb.createDataFormat();
			if (td.getSFormatPattern() != null)
				currentStyle.setDataFormat(format.getFormat(td
						.getSFormatPattern()));
			cell2.setCellStyle(currentStyle);
			cell2.setCellType(HSSFCell.CELL_TYPE_STRING);
			// 格式指定
			cell2.setCellValue(inVvalue.trim());
			break;
		case NUMBER:
		    //wing Remark
			// 格式指定[4807]
			format = xlsWb.createDataFormat();
			if (td.getSFormatPattern() != null) {
				if (td.getSFormatPattern().indexOf('%') != -1) {
					currentStyle.setDataFormat(HSSFDataFormat
							.getBuiltinFormat("0.00%")); //$NON-NLS-1$
					if (td.getNumber() != 0)
					  td.setNumber(td.getNumber() / 100);
				} else
					currentStyle.setDataFormat(format.getFormat(td
							.getSFormatPattern()));
			}
			cell2.setCellStyle(currentStyle);
			// 數字類型輸出
			cell2.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			cell2.setCellValue(td.getNumber());
			break;
		case DATE:
			format = xlsWb.createDataFormat();
			if (td.getSFormatPattern() != null)
				currentStyle.setDataFormat(format.getFormat(td
						.getSFormatPattern()));
			cell2.setCellStyle(currentStyle);
			cell2.setCellType(HSSFCell.CELL_TYPE_STRING);
			// 格式指定
			cell2.setCellValue(inVvalue.trim());
			break;
    case IMG:
			try{
        if(allRowCountOld2_!=allRowCount2_){
          ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
          BufferedImage bufferImg = ImageIO.read(new File(oApplication_.getRealPath(td.getContent().trim())));
          ImageIO.write(bufferImg,"jpg",byteArrayOut);
          HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, bufferImg.getWidth(), bufferImg.getHeight(), (short) 0, allRowCount2_, (short) 3, allRowCount2_+3);
          patriarch.createPicture(anchor, xlsWb.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
          allRowCountOld2_=allRowCount2_;
        }
      }catch(Exception e){
        e.printStackTrace();
      }
			break;
    case emisReport.REPORT_NODISPLAY:
			// 99表示只創建，沒有VALUE
			cell2.setCellStyle(currentStyle);

			break;
		case 98:
			// 數字類型輸出
			cell2.setCellStyle(currentStyle);
			cell2.setCellType(HSSFCell.CELL_TYPE_STRING);
			// 格式指定

			cell2.setCellValue(inVvalue.trim());
			// 數字類型輸出
			break;
		case 97:
			// 數字類型輸出
			cell2.setCellStyle(currentStyle);
			cell2.setCellType(HSSFCell.CELL_TYPE_STRING);
			// 格式指定

			cell2.setCellValue(inVvalue.trim());
			// 公式輸出
			cell2.setCellType(HSSFCell.CELL_TYPE_FORMULA);
			cell2.setCellFormula(td.getContent());
			cell2.setCellStyle(currentStyle);
			break;
		case 96:
			// 替代符號
			cell2.setCellStyle(currentStyle);
			cell2.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell2.setCellValue(td.getSFormatPattern());
			break;
		default:
			// 格式指定
			format = xlsWb.createDataFormat();
			if (td.getSFormatPattern() != null)
				currentStyle.setDataFormat(format.getFormat(td
						.getSFormatPattern()));
			// 字符串格式,未轉換日期類型
			cell2.setCellStyle(currentStyle);
			cell2.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell2.setCellValue(inVvalue.trim());
		}

	}
  /**
	 * 輸出當前HSSFWorkbook　並重新new HSSFWorkbook() 並進行一系列列印樣式初始化處理
	 * @author wing [wayke] 2005/12/12
	 * */
	public void doNewWorkbook(){
		try{
			if("inPageheader".equals(sInPageHorF)){ //$NON-NLS-1$
				int shCnt = sheet.getLastRowNum();
		    	for(int i=0;i<this.curPageHeaderLine;i++){
		    		HSSFRow last=sheet.getRow(shCnt-i);
		    		sheet.removeRow(last);
		    		sInPageHorF = ""; //$NON-NLS-1$
		    	}
			}
//			Excel文檔輸出
		    this.createXlsRptZip();
            //初始化Excel工作薄　得到工作薄顯示sheet
		    xlsWb = new HSSFWorkbook();
		    sheet = xlsWb.createSheet();

		    initPROP(ep);
		    //重新初始化Cell 字體和顯示風格
		    initStyleAndFontMap(ep);
		}catch(Exception e){
			e.printStackTrace();
		}
		//列印　Excel file Name 序列累加　（用於產生下一個Excel文件名）
		iCount ++;
		//單文件行數記錄值初始話
		allRowCount_ = -1;
		curPageHeaderLine = 0;
		this.setHavePrintData(false);
	}
/**
 * 判斷WorkBook 是否超過Menoney最大值
 * 並處理了剛好是在頁頭或頁尾跳頁的特殊狀況
 * 2005/12/07  wing [wayke] [4656]
 * @return false || true
 * */
	public boolean hasNewPage(){

		if(allRowCount_>2000){

			if(allRowCount_%100==0){
				//System.out.println("IIIIIIIIIIIIIIIII "+allRowCount_ +"  "+xlsWb.getBytes().length);
		       if( xlsWb.getBytes().length >= iLargeFileSize){
				//if (_allRowForDPage >= MAX_TR_SIZE){
					//if (_allRowForDPage % MAX_TR_SIZE == 0) {
		    	   if(this.isPrintPageHeader){
				    	sInPageHorF = "inPageheader"; //$NON-NLS-1$
				    	isNewPageLater = true;
				    	return false;

				   }else if(this.isPrintPageFooter){
					    sInPageHorF = "inPageFooter"; //$NON-NLS-1$
				    	isNewPageLater = true;
				    	return false;
				   }else{
					    sInPageHorF = ""; //$NON-NLS-1$
		    	        doNewWorkbook();
			            return true;
				   }
				    //}
			    }
			}
		}
		return false;
	}

	/**
	 * wing 重寫printTr,createRow
	 * wing [wayke] 2005/12/12 加入文件過大拆分判斷
	 */
	public void printTr(emisTr tr) {

		isTrRunTimeWrong = false;
		if (tr == null)
			return;

		allRowCount_++;


		// 防內存OVEROUT
		if (allRowCount_ > 63000) {
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.258") //$NON-NLS-1$
					+ String.valueOf(allRowCount_)
					+ Messages.getString("emisRptExcelProvider.259")); //$NON-NLS-1$
		}
		columnCount_ = 0; // 歸0
		// 格數不足的合成一行輸出
		int currrentSpanCount = 0;
		for (int j = 0; j < tr.size(); j++) {
			emisTd td = tr.get(j);
			if (td != null && (td.getExceldisplay().equalsIgnoreCase("true"))) { //$NON-NLS-1$
				isTrRunTimeWrong = true;
				currrentSpanCount += td.getNColumnSpan_();
			}
		}


		if (!isTrRunTimeWrong) {
			allRowCount_--;
			columnCount_ = 0; // 歸0
			return;
		} else {
			row = sheet.createRow(allRowCount_);
		}
    //提取行中最高的TD
    short maxHeightTd = getMaxHeightStyle(tr);



		// 不是標準行則合併
		if (currrentSpanCount < maxColumns_ || currrentSpanCount > maxColumns_) {
			if (!isRegionRow) {
				for (int i = 0; i < tr.size(); i++) {
					emisTd td = tr.get(i);
					// 過濾不需要列印的分隔符
					if (td != null
							&& (td.getExceldisplay().equalsIgnoreCase("true"))) { //$NON-NLS-1$
						printTd(td);
						columnCount_++;
					}
				}
				if (tr.size() > 0) {
					emisTd td = tr.get(0);

					if (maxHeightTd == -1) {
            setRowHeight(td);
          } else {
            setRowHeight(td, maxHeightTd);
          }
				}

			} else {
				String lineStr = ""; //$NON-NLS-1$
				emisTd fontSettingTd = null;
				for (int i = 0; i < tr.size(); i++) {
					emisTd td = tr.get(i);
					if (td != null
							&& (td.getExceldisplay().equalsIgnoreCase("true"))) { //$NON-NLS-1$
						//不顯示
						if(td.getCustid()!=emisReport.REPORT_NODISPLAY){
							lineStr += td.getContent();
						}else{
							lineStr+=emisUtil.padding("",td.getNAlign_(),td.getSize());	 //$NON-NLS-1$
						}
						columnCount_++;
						if (td.getFontID() != null
								&& (!"".equalsIgnoreCase(td.getFontID()))) //$NON-NLS-1$
							fontSettingTd = td;
					}
				}
				 // 不足,超过的都以第一列的字体设定
        printTd(lineStr, fontSettingTd);
        if (maxHeightTd == -1) {
          setRowHeight(fontSettingTd);
        } else {
          setRowHeight(fontSettingTd, maxHeightTd);
        }
      }

		} else {
      // wing add 20070113 start
      short iMaxTdHeight = 0;
      emisTd maxTd = null;
			for (int i = 0; i < tr.size(); i++) {
				emisTd td = tr.get(i);
				// 過濾不需要列印的分隔符
				if (td != null
						&& (td.getExceldisplay().equalsIgnoreCase("true"))) { //$NON-NLS-1$
					printTd(td);
					columnCount_++;
          // add maxFont
          Object fontObj = fontPoolMap.get(td.getFontID());
          if (fontObj != null) {
            HSSFFont compareRptFont = (HSSFFont) fontObj;
            if (compareRptFont.getFontHeightInPoints() > iMaxTdHeight) {
              iMaxTdHeight = compareRptFont
                  .getFontHeightInPoints();
              maxTd = td;
            }
				}
			}
      }
      if (tr.size() > 0) {
				if (maxHeightTd == -1) {
          this.setRowHeight(maxTd);
        } else {
          this.setRowHeight(maxTd, maxHeightTd);
        }
			}
    }
		//wing review[wayke] 2005/12/12
		if(this.isPrintPageHeader){
			this.curPageHeaderLine++;
		}else{
			this.curPageHeaderLine = 0;
		}
		//是否分頁列印
		hasNewPage_ = hasNewPage();
	}

  public void printTr(emisTr tr,HSSFSheet sheet_) {

		isTrRunTimeWrong = false;
		if (tr == null)
			return;

    allRowCount2_++;


    // 防內存OVEROUT
		if (allRowCount2_ > 63000) {
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.258") //$NON-NLS-1$
					+ String.valueOf(allRowCount2_)
					+ Messages.getString("emisRptExcelProvider.259")); //$NON-NLS-1$
		}
		columnCount2_ = 0; // 歸0
		// 格數不足的合成一行輸出
		int currrentSpanCount = 0;
		for (int j = 0; j < tr.size(); j++) {
			emisTd td = tr.get(j);
			if (td != null && (td.getExceldisplay().equalsIgnoreCase("true"))) { //$NON-NLS-1$
				isTrRunTimeWrong = true;
				currrentSpanCount += td.getNColumnSpan_();
			}
		}


		if (!isTrRunTimeWrong) {
			allRowCount2_--;
			columnCount2_ = 0; // 歸0
			return;
		} else {
      row2 = sheet_.createRow(allRowCount2_);
    }



		// 不是標準行則合併
		if (currrentSpanCount < maxColumns_ || currrentSpanCount > maxColumns_) {
			if (!isRegionRow) {
				for (int i = 0; i < tr.size(); i++) {
					emisTd td = tr.get(i);
					// 過濾不需要列印的分隔符
					if (td != null
							&& (td.getExceldisplay().equalsIgnoreCase("true"))) { //$NON-NLS-1$
						printTd(td,sheet_);
						columnCount2_++;
					}
				}
				if (tr.size() > 0) {
					emisTd td = tr.get(0);
					setRowHeight(td,sheet_);
				}

			} else {
				String lineStr = ""; //$NON-NLS-1$
				emisTd fontSettingTd = null;
				for (int i = 0; i < tr.size(); i++) {
					emisTd td = tr.get(i);
					if (td != null
							&& (td.getExceldisplay().equalsIgnoreCase("true"))) { //$NON-NLS-1$
						//不顯示
						if(td.getCustid()!=emisReport.REPORT_NODISPLAY){
							lineStr += td.getContent();
						}else{
							lineStr+=emisUtil.padding("",td.getNAlign_(),td.getSize());	 //$NON-NLS-1$
						}
						columnCount2_++;
						if (td.getFontID() != null
								&& (!"".equalsIgnoreCase(td.getFontID()))) //$NON-NLS-1$
							fontSettingTd = td;
					}
				}
				// 不足,超過的都以第一列的字體設定
				printTd(lineStr, fontSettingTd,sheet_);
			}

		} else {
			for (int i = 0; i < tr.size(); i++) {
				emisTd td = tr.get(i);
				// 過濾不需要列印的分隔符
				if (td != null
						&& (td.getExceldisplay().equalsIgnoreCase("true"))) { //$NON-NLS-1$
					printTd(td,sheet_);
					columnCount2_++;
				}
			}
		}
		//wing review[wayke] 2005/12/12
		if(this.isPrintPageHeader){
			this.curPageHeaderLine++;
		}else{
			this.curPageHeaderLine = 0;
		}
		//是否分頁列印
		hasNewPage_ = hasNewPage();
	}
  public void printTr(emisTr tr, int nAlign, int nSize) {
		if (tr == null)	return;
		// 沒有實做,調用default實現
		printTr(tr);
	}

	public void printTable(emisTr tr) {
		if (tr == null) return;
		printTr(tr);
		testEject();
	}

	public void printTable(emisTr tr, int nAlign, int nSize) {
		if (tr == null)	return;
		printTr(tr, nAlign, nSize);
		testEject();
	}

	private void testEject() {
		if (isPageExceed()) {
			listener_.onBeforeEject();
			eject();
			listener_.onAfterEject();
		}

	}

	public int getCurrentRow() {
		return nCurrentRowNum_;
	}

	private boolean isPageExceed() {
		if (nCurrentRowNum_ >= nHeight_)
			return true;
		return false;
	}

	public String padding(String sStr, int nAlign, int nTotalSize) {
		return emisUtil.padding(sStr, nAlign, nTotalSize);
	}

	public int getPageNum() {
		return nPageNum_;
	}

	public int getWidth() {
		return nWidth_;
	}

	public int getHeight() {
		return nHeight_;
	}

	public void incRowNum(int count) {
		nCurrentRowNum_ += count;
	}

	/**
	 * property support function
	 */
	public String getProperty(String sKey, String sDefault) {
		return oProp.getProperty(sKey, sDefault);
	}

	/**
	 * property support function
	 */
	public String getProperty(String sKey) {
		return oProp.getProperty(sKey);
	}

	/**
	 * property support function
	 */
	public int getProperty(String sKey, int nDefault) {
		String sValue = oProp.getProperty(sKey);
		if (sValue != null) {
			try {
				nDefault = Integer.parseInt(sValue);
			} catch (Exception ignore) {
			}
		}
		return nDefault;
	}

	/**
	 * 依照 datasrc tag 所定的 id 的 emisDataSrc 事實上 emisDataSrc 所 implement 的就是 XML
	 * 中的 datasrc tag
	 */
	public emisDataSrc getDataSrc(String sDataSrcName) {
		return (emisDataSrc) oDataSrc.get(sDataSrcName);
	}

	public void eject() {
	    //wing[4545] 2005/11/13 wing and mike add  pagebreak func.
		xlsWb.getSheetAt(0).setRowBreak(this.allRowCount_);
		nCurrentRowNum_ = 0;
		nPageNum_++;
	}

	public void registerListener(emisProviderEventListener listener) {
		listener_ = listener;
	}

	public ServletContext getContext() {
		return oApplication_;
	}

	public emisUser getUser() {
		return oUser_;
	}

	/**
	 * 列數，寬度設置 , int maxColumns
	 *
	 * @param columnDataList
	 * @param maxColumns
	 */
	public void setRowWidthFormat(ArrayList columnDataList,
			ArrayList columnMaxFontIDList, int maxColumns) {
		try {
			// setting每列的寬度
			setColumnDataRowSettingList(columnDataList);
			setMaxColumns(maxColumns);
			setColumnMaxFontIDList(columnMaxFontIDList);
			setRptCellWidth();
		} catch (Exception ee) {
			if (isRegionRow) {
				this.hasDataOutput = false;
				errorMsgList.add(Messages.getString("emisRptExcelProvider.108") + ee.getMessage()); //$NON-NLS-1$
				//throw new RuntimeException(
				//		"報表標準行<tr> width或字體設置(fontID,fontMaxID,cellstyle)有誤"
				//				+ ee.getMessage());
			}
		}
	}

	/**
	 * 據最多列數＜TD＞的行設定SHEET的ColumnWithd wing 10/18 直接設定不用emisRptField
	 */

	public void setRptCellWidth() {
		// 取得cellWidthList,keep int
		int oldWidth = 0;
		HSSFFont oldmaxFont = null;
		try {
			HSSFFont maxFont = null;
			for (int i = 0; i < columnDataRowSettingList.size(); i++) {
				int width = 0;
				try {
					if (columnDataRowSettingList.get(i) == null) {
						width = oldWidth;
					} else
						width = ((Integer) columnDataRowSettingList.get(i))
								.intValue();
				} catch (Exception e) {
					width = oldWidth;
				}
				oldWidth = width;
				try {
					if (columnMaxFontIDList.get(i) == null) {
						maxFont = oldmaxFont;
					} else
						maxFont = (HSSFFont) getRptFont((String) columnMaxFontIDList
								.get(i));
					// logger.debug(String.valueOf(maxFont == null));
				} catch (Exception fone) {
					maxFont = this.mutiRptFont;
				}
				oldmaxFont = maxFont;

				int fontPer = maxFont.getFontHeightInPoints() + 2;
				// 大字體在txt報表的寬度是足夠的,但在excel中寬度就小了些
				if (fontPer > 19) {
					width = width + 2;
				} else if (fontPer > 14) {
					width = width + 1;
				}
				sheet.setColumnWidth((short) i,
						(short) ((width * fontPer) / ((double) 1 / 22)));
				// 列印頁碼在pagefoot
				// wing[4545]
				String headerTitle = this.getParameter("EXCEL_PH_TITLE"); //$NON-NLS-1$

				if (headerTitle != null && !("".equals(headerTitle))) { //$NON-NLS-1$
					HSSFHeader header = sheet.getHeader();
					header.setRight(headerTitle);
				}

				String footerTitle = this.getParameter("EXCEL_PF_TITLE"); //$NON-NLS-1$
				if(footerTitle==null)footerTitle=""; //$NON-NLS-1$

				if(this.sFooterAlign==null)
          sFooterAlign = "";

        if(this.sFooterFormat==null || "".equalsIgnoreCase(this.sFooterFormat)){
        footerTitle += " " + HSSFFooter.page() + "/" //$NON-NLS-2$
            + HSSFFooter.numPages();
        }else{
          this.sFooterMsg = this.sFooterFormat;
          footerTitle+=" "+this.sFooterMsg;
        }

				HSSFFooter footer = sheet.getFooter();
				footer.fontSize((short)7);
				 HSSFFooter.font("宋体","NORMAL");
        //add vince 20070117
        if(sFooterAlign.equalsIgnoreCase("left")){
           footer.setLeft(footerTitle);
        } else if(sFooterAlign.equalsIgnoreCase("center")){
           footer.setCenter(footerTitle);
        } else{
          footer.setRight(footerTitle);
        }

			}
		} catch (Exception e) {
			if (isRegionRow) {
				//e.printStackTrace();
			}
		}

	}

	/**
	 * 會幫你 close 所有的 resource
	 */
	public void close() {
		// close all emisDataSrc
		Collection c = oDataSrc.values();
		if (c != null) {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				try {
					emisDataSrc _oDataSrc = (emisDataSrc) it.next();
					_oDataSrc.freeAllResource();
				} catch (Exception ignore) {
				}
			}
		}

	}

	protected Node searchSection(Node oTopNode, String sSectionName) {
		Node _oRet = null;
		Node _oNode;
		NodeList _nlSection = emisRptXML.searchNodes(oTopNode, sSectionName);
		if (_nlSection != null) {
			for (int i = 0; i < _nlSection.getLength(); i++) {
				_oNode = _nlSection.item(i);
				_oRet = _oNode;
				break;
			}
		}
		return _oRet;
	}

	/**
	 * 字體讀入設置(xml中設置)
	 */
	protected void initExcelReport(Node oNode, String sectionName) {
		if (oNode == null) {
			return;
		}
		String sectionName_ = sectionName;
		// 字體定義
		initParseXmlFont(oNode, sectionName_);
		// style
		initExcelColorStyle(oNode, sectionName_);
	}

	private void initParseXmlFont(Node oNode, String sectionName) {

		String sectionName_ = sectionName;
		// 字體設定
		NodeList _nlTr = ((Element) oNode).getElementsByTagName("font"); //$NON-NLS-1$
		Node _oTr;
		// 設置 provider的一些屬性,處理 font,取得color,size ,fontname
		HSSFFont reportRptFont = null;
		String fontId = ""; //$NON-NLS-1$
		String fontName = this.defaultFontName;
		String underLine = ""; //$NON-NLS-1$
		String isBold = "false"; //$NON-NLS-1$
		String fontColor = "";// default //$NON-NLS-1$
		String sIsSectionFont = "false";// default //$NON-NLS-1$
		int fontSize = fontHeight;
		try {
			for (int i = 0; i < _nlTr.getLength(); i++) {
				_oTr = _nlTr.item(i);

				fontId = emisRptXML.getAttribute(_oTr, "fontID"); //$NON-NLS-1$
				fontName = emisRptXML.getAttribute(_oTr, "fontName"); //$NON-NLS-1$
				underLine = emisRptXML.getAttribute(_oTr, "underLine"); //$NON-NLS-1$
				isBold = emisRptXML.getAttribute(_oTr, "isBold"); //$NON-NLS-1$
				fontColor = emisRptXML.getAttribute(_oTr, "color"); //$NON-NLS-1$
				sIsSectionFont = emisRptXML.getAttribute(_oTr, "isSectionFont"); //$NON-NLS-1$

				try {
					fontSize = Integer.parseInt(emisRptXML.getAttribute(_oTr,
							"fontSize")); //$NON-NLS-1$
				} catch (Exception e) {
					this.hasDataOutput = false;
					errorMsgList.add(Messages.getString("emisRptExcelProvider.128") + e.getMessage()); //$NON-NLS-1$
					throw new RuntimeException(Messages.getString("emisRptExcelProvider.129") + e.getMessage()); //$NON-NLS-1$
				}
				if (null != fontId && (!"".equalsIgnoreCase(fontId))) { //$NON-NLS-1$
					if (null != sIsSectionFont
							&& ("true".equalsIgnoreCase(sIsSectionFont))) { //$NON-NLS-1$
						defaultfontIDmap.put(sectionName_, fontId);
					}
					reportRptFont = (HSSFFont) fontPoolMap.get(fontId);
					if (null != reportRptFont)
						return;
					try {
						reportRptFont = xlsWb.createFont();
						reportRptFont.setFontName(fontName);
						reportRptFont.setFontHeightInPoints((short) fontSize);
						// 下劃線形式設定
						if (null != underLine
								&& (!"".equalsIgnoreCase(underLine))) { //$NON-NLS-1$
							reportRptFont.setUnderline(emisRefGetHSSFCellStyle
									.getFontUnderLine(underLine));
						}

						// 是否是粗體字
						if (null != isBold && ("true".equalsIgnoreCase(isBold))) { //$NON-NLS-1$
							reportRptFont
									.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
						}
						if (null != isBold
								&& ("false".equalsIgnoreCase(isBold))) { //$NON-NLS-1$
							reportRptFont
									.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
						}

						if (null != fontColor
								&& (!"".equalsIgnoreCase(fontColor))) { //$NON-NLS-1$
							fontColor = "HSSFColor." + fontColor.toUpperCase() //$NON-NLS-1$
									+ ".index"; //$NON-NLS-1$
							reportRptFont.setColor(emisRefGetHSSFCellStyle
									.getCellStyleColor(fontColor));
						} else {
							reportRptFont.setColor(HSSFColor.BLACK.index);
						}
					} catch (Exception fontEx) {
						// 出錯則取系統init字體
						mutiRptFont.setFontHeightInPoints((short) fontSize);
						reportRptFont = mutiRptFont;
					}
					fontPoolMap.put(fontId, reportRptFont);
				}
			}

		} catch (Exception fontEx) {
			this.hasDataOutput = false;
			errorMsgList.add(Messages.getString("emisRptExcelProvider.138") + fontEx.getMessage()); //$NON-NLS-1$
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.139")); //$NON-NLS-1$
		}

	}

	/**
	 * Excel列印設定
	 *
	 * @param oNode
	 */
	private void initExcePageSetting(Node oNode) {
		if (oNode == null)
			return;
		NodeList _nlTr;
		Node _oTr;
		// 每頁列印行數
		int pageRows = 0; // default;
		int SheetPrintPages = 1;
		int excelorient = 90; // default;

		_nlTr = ((Element) oNode).getElementsByTagName("excelPrint"); //$NON-NLS-1$
		try {
			for (int i = 0; i < _nlTr.getLength(); i++) {
				_oTr = _nlTr.item(i);
				try {
					String spageRows = emisRptXML
							.getAttribute(_oTr, "pageRows"); //$NON-NLS-1$
					if (null != spageRows && (!"".equalsIgnoreCase(spageRows))) //$NON-NLS-1$
						pageRows = Integer.parseInt(spageRows);

					String sSheetPrintPages = emisRptXML.getAttribute(_oTr,
							"sheetPrintPages"); //$NON-NLS-1$
					if (null != sSheetPrintPages
							&& (!"".equalsIgnoreCase(sSheetPrintPages))) //$NON-NLS-1$
						SheetPrintPages = Integer.parseInt(sSheetPrintPages);

					String sExcelorient = emisRptXML.getAttribute(_oTr,
							"excelorient"); //$NON-NLS-1$
					if (null != sExcelorient
							&& (!"".equalsIgnoreCase(sExcelorient))) //$NON-NLS-1$
						excelorient = Integer.parseInt(sExcelorient);

					String sCopies = emisRptXML.getAttribute(_oTr, "copies"); //$NON-NLS-1$
					if (null != sCopies && (!"".equalsIgnoreCase(sExcelorient))) //$NON-NLS-1$
						copies = Integer.parseInt(sCopies);
             // wing add 20070113 start
          String sLeftMargin = emisRptXML.getAttribute(_oTr,
              "LeftMargin");
          if (null != sLeftMargin
              && (!"".equalsIgnoreCase(sLeftMargin))) {
            dLeftMargin = Double.parseDouble(sLeftMargin) / dTrans;
          }
          String sRightMargin = emisRptXML.getAttribute(_oTr,
              "RightMargin");
          if (null != sRightMargin
              && (!"".equalsIgnoreCase(sRightMargin))) {
            dRightMargin = Double.parseDouble(sRightMargin) / dTrans;
          }
          String sTopMargin = emisRptXML.getAttribute(_oTr,
              "TopMargin");
          if (null != sTopMargin
              && (!"".equalsIgnoreCase(sTopMargin))) {
            this.dTopMargin = Double.parseDouble(sTopMargin) / dTrans;
          }
          String sBottomMargin = emisRptXML.getAttribute(_oTr,
              "BottomMargin");
          if (null != sBottomMargin
              && (!"".equalsIgnoreCase(sBottomMargin))) {
            this.dBottomMargin = Double.parseDouble(sBottomMargin) / dTrans;
          }
          //add vince 20070117 start
          String sPageHead = emisRptXML.getAttribute(_oTr,
              "PageHeadMargin");
          if (null != sPageHead
              && (!"".equalsIgnoreCase(sPageHead))) {
            this.dPageHead = Double.parseDouble(sPageHead) / dTrans;
          }
          String sPageFooter = emisRptXML.getAttribute(_oTr,
              "PageFooterMargin");
          if (null != sPageFooter
              && (!"".equalsIgnoreCase(sPageFooter))) {
            this.dPageFooter = Double.parseDouble(sPageFooter) / dTrans;
          }
          this.sFooterAlign = emisRptXML.getAttribute(_oTr,
              "PageFooterAlign");

          this.sFooterFormat = emisRptXML.getAttribute(_oTr,
              "PageFooterFormat");
          //add vince 20070117 end
          String sScacle = emisRptXML.getAttribute(_oTr, "scacle");
          if (null != sBottomMargin
              && (!"".equalsIgnoreCase(sScacle))) {
            this.shScacle = (short) Integer.parseInt(sScacle);
          }
				} catch (Exception e) {
					// 頁數設置-數字出錯
					pageRows = 80; // default;
					SheetPrintPages = 1;
				}

			}
			// 自動調整列印頁面
			// if (pageRows != 0) {
			// sheet.setAutobreaks(true);
			// ps.setFitHeight((short) pageRows);
			// ps.setFitWidth((short) SheetPrintPages);
			// ps.setScale((short) excelorient);
			// }
			// 縱打 wing
			// sheet.setAutobreaks(true);
			// ps.setHeaderMargin(0);
			// ps.setFooterMargin(90);
			// ps.setScale((short) 100);
			// ps.setHResolution((short)60);
		} catch (Exception excePrintEx) {
			this.hasDataOutput = false;
			this.errorMsgList.add(Messages.getString("emisRptExcelProvider.149") //$NON-NLS-1$
					+ excePrintEx.getMessage());
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.150")); //$NON-NLS-1$
		}

	}

	public void initExcelColorStyle(Node oNode, String sectionName) {
		NodeList _nlTr;
		Node _oTr;
		// default
		String sIsSectionStyle = "false"; //$NON-NLS-1$
		String cellStyleID;
		String BorderBottom;
		String BottomBorderColor;
		String BorderLeft;
		String LeftBorderColor;
		String BorderRight;
		String RightBorderColor;
		String BorderTop;
		String TopBorderColor;
		String sFillPattern;
		String sFillForegroundColor;
		String sFillBackgroundColor;
		String sVerticalAlignment;

    String sRowHeight = null;
    String sTransFormat = null;
		// color設定
		sSplitPaneBackGroupColor = "GREY_25_PERCENT"; // default; //$NON-NLS-1$
		_nlTr = ((Element) oNode).getElementsByTagName("excelSplitPane"); //$NON-NLS-1$
		try {
			for (int i = 0; i < _nlTr.getLength(); i++) {
				_oTr = _nlTr.item(i);
				sSplitPaneBackGroupColor = emisRptXML.getAttribute(_oTr,
						"backgroupColor"); //$NON-NLS-1$
			}
		} catch (Exception styeEx) {
			this.hasDataOutput = false;
			this.errorMsgList.add(Messages.getString("emisRptExcelProvider.155") + styeEx.getMessage()); //$NON-NLS-1$
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.156")); //$NON-NLS-1$
		}

		_nlTr = ((Element) oNode).getElementsByTagName("cellstyle"); //$NON-NLS-1$
		emisExcelReportCellStyle echoCellStyle = null;//
		try {
			for (int i = 0; i < _nlTr.getLength(); i++) {
				_oTr = _nlTr.item(i);
				try {
					cellStyleID = emisRptXML.getAttribute(_oTr, "cellStyleID"); //$NON-NLS-1$
					BorderBottom = emisRptXML
							.getAttribute(_oTr, "BorderBottom"); //$NON-NLS-1$
					BottomBorderColor = emisRptXML.getAttribute(_oTr,
							"BottomBorderColor"); //$NON-NLS-1$
					BorderLeft = emisRptXML.getAttribute(_oTr, "BorderLeft"); //$NON-NLS-1$
					LeftBorderColor = emisRptXML.getAttribute(_oTr,
							"LeftBorderColor"); //$NON-NLS-1$
					BorderRight = emisRptXML.getAttribute(_oTr, "BorderRight"); //$NON-NLS-1$
					RightBorderColor = emisRptXML.getAttribute(_oTr,
							"RightBorderColor"); //$NON-NLS-1$
					BorderTop = emisRptXML.getAttribute(_oTr, "BorderTop"); //$NON-NLS-1$
					TopBorderColor = emisRptXML.getAttribute(_oTr,
							"TopBorderColor"); //$NON-NLS-1$
					sFillPattern = emisRptXML.getAttribute(_oTr, "FillPattern"); //$NON-NLS-1$
					sFillForegroundColor = emisRptXML.getAttribute(_oTr,
							"FillForegroundColor"); //$NON-NLS-1$
					sFillBackgroundColor = emisRptXML.getAttribute(_oTr,
							"FillBackgroundColor"); //$NON-NLS-1$

					sIsSectionStyle = emisRptXML.getAttribute(_oTr,
							"isSectionStyle"); //$NON-NLS-1$
					sVerticalAlignment = emisRptXML.getAttribute(_oTr,
							"VerticalAlignment"); //$NON-NLS-1$

           //
          sTransFormat = emisRptXML.getAttribute(_oTr, "TransFormat");
          //add 20070116 vince
          sRowHeight = emisRptXML.getAttribute(_oTr, "RowHeight");

					echoCellStyle = new emisExcelReportCellStyle();
					// border.
					if (null != BorderBottom
							&& (!"".equalsIgnoreCase(BorderBottom))) { //$NON-NLS-1$
						echoCellStyle.setBorderBottom(emisRefGetHSSFCellStyle
								.getCellBorder(BorderBottom));
					}
					if (null != BorderLeft
							&& (!"".equalsIgnoreCase(BorderLeft))) { //$NON-NLS-1$
						echoCellStyle.setBorderLeft(emisRefGetHSSFCellStyle
								.getCellBorder(BorderLeft));
					}
					if (null != BorderRight
							&& (!"".equalsIgnoreCase(BorderRight))) { //$NON-NLS-1$
						echoCellStyle.setBorderRight(emisRefGetHSSFCellStyle
								.getCellBorder(BorderRight));
					}
					if (null != BorderTop && (!"".equalsIgnoreCase(BorderTop))) { //$NON-NLS-1$
						echoCellStyle.setBorderTop(emisRefGetHSSFCellStyle
								.getCellBorder(BorderTop));
					}
					// color
					if (null != BottomBorderColor
							&& (!"".equalsIgnoreCase(BottomBorderColor))) { //$NON-NLS-1$
						echoCellStyle
								.setBottomBorderColor(emisRefGetHSSFCellStyle
										.getCellStyleColor("HSSFColor." //$NON-NLS-1$
												+ BottomBorderColor + ".index")); //$NON-NLS-1$
					}
					if (null != LeftBorderColor
							&& (!"".equalsIgnoreCase(LeftBorderColor))) { //$NON-NLS-1$
						echoCellStyle
								.setLeftBorderColor(emisRefGetHSSFCellStyle
										.getCellStyleColor("HSSFColor." //$NON-NLS-1$
												+ LeftBorderColor + ".index")); //$NON-NLS-1$
					}
					if (null != RightBorderColor
							&& (!"".equalsIgnoreCase(RightBorderColor))) { //$NON-NLS-1$
						echoCellStyle
								.setRightBorderColor(emisRefGetHSSFCellStyle
										.getCellStyleColor("HSSFColor." //$NON-NLS-1$
												+ RightBorderColor + ".index")); //$NON-NLS-1$
					}
					if (null != TopBorderColor
							&& (!"".equalsIgnoreCase(TopBorderColor))) { //$NON-NLS-1$
						echoCellStyle.setTopBorderColor(emisRefGetHSSFCellStyle
								.getCellStyleColor("HSSFColor." //$NON-NLS-1$
										+ TopBorderColor + ".index")); //$NON-NLS-1$
					}

					if (null != sFillForegroundColor
							&& (!"".equalsIgnoreCase(sFillForegroundColor))) { //$NON-NLS-1$
						echoCellStyle
								.setFillForegroundColor(emisRefGetHSSFCellStyle
										.getCellStyleColor("HSSFColor." //$NON-NLS-1$
												+ sFillForegroundColor
														.toUpperCase()
												+ ".index")); //$NON-NLS-1$
					}
          if (null != sVerticalAlignment
              && (!"".equalsIgnoreCase(sVerticalAlignment))) {
            echoCellStyle.setVerticalAlignment(sVerticalAlignment);
          }

					if (null != sFillBackgroundColor
							&& (!"".equalsIgnoreCase(sFillBackgroundColor))) { //$NON-NLS-1$
						echoCellStyle
								.setFillBackgroundColor(emisRefGetHSSFCellStyle
										.getCellStyleColor("HSSFColor." //$NON-NLS-1$
												+ sFillBackgroundColor
														.toUpperCase()
												+ ".index")); //$NON-NLS-1$
					}

					if (null == sFillBackgroundColor
							|| ("".equalsIgnoreCase(sFillBackgroundColor))) { //$NON-NLS-1$
						echoCellStyle
								.setFillBackgroundColor(emisRefGetHSSFCellStyle
										.getCellStyleColor("HSSFColor.WHITE.index")); //$NON-NLS-1$
					}


					if (null != sVerticalAlignment
							&& (!"".equalsIgnoreCase(sVerticalAlignment))) { //$NON-NLS-1$
						echoCellStyle.setVerticalAlignment(sVerticalAlignment);
					}

					if (null != sFillPattern
							&& (!"".equalsIgnoreCase(sFillPattern))) { //$NON-NLS-1$
						echoCellStyle.setSetFillPattern(emisRefGetHSSFCellStyle
								.getCellBorder(sFillPattern.toUpperCase()));
					}
          // 20070113 wing add start
          if (null != sTransFormat
              && (!"".equalsIgnoreCase(sTransFormat))) {
            echoCellStyle.setDataFormart(HSSFDataFormat.getBuiltinFormat(sTransFormat));//sTransFormat
          }
          // 20070113 wing add end

          // 20070116 add vince start
          if (null != sRowHeight && (!"".equalsIgnoreCase(sRowHeight))) {
            try {
              echoCellStyle.setRowHeight(Short.parseShort(sRowHeight));
            } catch (NumberFormatException e) {
              logger.debug(e.getMessage());
            }
          }
          //20070116 vince add end
					if (null != sIsSectionStyle
							&& ("true".equalsIgnoreCase(sIsSectionStyle))) { //$NON-NLS-1$
						defaultStyleIDmap.put(sectionName, cellStyleID);
					}

					echoCellStyle.setCellStyleID(cellStyleID);
					if (null != cellStyleID
							&& (!"".equalsIgnoreCase(cellStyleID))) { //$NON-NLS-1$
						cellStyleMap.put(cellStyleID, echoCellStyle);
					}
				} catch (Exception e) {
					logger.debug(Messages.getString("emisRptExcelProvider.194")); //$NON-NLS-1$
				}

			}
		} catch (Exception cellStyleEx) {
			this.hasDataOutput = false;
			this.errorMsgList.add(Messages.getString("emisRptExcelProvider.195") + cellStyleEx.getMessage()); //$NON-NLS-1$
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.196")); //$NON-NLS-1$
		}

	}

	/**
	 * 將Stylele的ID及字體的種類組合起來 cellStyleMap+fontPool
	 */
	public void componetCellStyleAndFont() {
		String StylePKID = "";  //$NON-NLS-1$
		String fontIDD = null;
		HSSFFont font = null;

		emisExcelReportCellStyle ecCellSty = null;
		HSSFCellStyle style = null;
		Iterator cellIt = cellStyleMap.values().iterator();
		while (cellIt.hasNext()) {

			ecCellSty = (emisExcelReportCellStyle) cellIt.next();
			Iterator it = fontPoolMap.keySet().iterator();
			while (it.hasNext()) {
				fontIDD = (String) it.next();
				font = (HSSFFont) fontPoolMap.get(fontIDD);

				// 有左,中右三種
				for (int i = 1; i < 6; i++) {
					style = xlsWb.createCellStyle();
					style.setFont(font);
					switch (i) {
					case 1:
						style.setAlignment((short) HSSFCellStyle.ALIGN_LEFT);
						break;
					case 2:
						style.setAlignment((short) HSSFCellStyle.ALIGN_CENTER);
					case 3:
						style.setAlignment((short) HSSFCellStyle.ALIGN_RIGHT);
          case 4:
						style.setAlignment((short) HSSFCellStyle.ALIGN_FILL);
          case 5:
						style.setAlignment((short) HSSFCellStyle.ALIGN_JUSTIFY);
          default:
						style.setAlignment((short) HSSFCellStyle.ALIGN_LEFT);
					}
					StylePKID = ecCellSty.getCellStyleID() + fontIDD
							+ String.valueOf(i);
					setCellStyles(ecCellSty.getCellStyleID(), style);
					// logger.info("加入了風格及字體" + StylePKID);
					try {
						xlsStyleMap.put(StylePKID, style);
						// logger.info("StylePKID:" + StylePKID);
					} catch (Exception ee) {
						logger.debug(Messages.getString("emisRptExcelProvider.198") + ecCellSty.getCellStyleID() //$NON-NLS-1$
								+ fontIDD + String.valueOf(i));
						logger.debug(ee.getMessage());
					}
				}
			}
			// this.fontPool
		}
	}

	/**
	 * 從font pool中取得字體
	 *
	 * @param fontID
	 * @return
	 */
	public HSSFFont getRptFont(String fontID) {
		if (null == fontID || "".equalsIgnoreCase(fontID)) //$NON-NLS-1$
			fontID = "defaultFontID"; //$NON-NLS-1$
		HSSFFont reportRptFont = null;
		try {

			reportRptFont = (HSSFFont) fontPoolMap.get(fontID);
		} catch (Exception e) {
			// 出錯則取系統字體
			reportRptFont = defaultRptFont;
		}
		return reportRptFont;
	}

	public HSSFCellStyle getXlsStyleMap(String cellStyleID, String fontID,
			int nAlign) {
		if (null == cellStyleID || "".equalsIgnoreCase(cellStyleID)) //$NON-NLS-1$
			cellStyleID = "defaultCellStyle"; //$NON-NLS-1$
		if (null == fontID || "".equalsIgnoreCase(fontID)) //$NON-NLS-1$
			fontID = "defaultFontID"; //$NON-NLS-1$
		HSSFCellStyle xlsStyleTemp = null;
		try {
			xlsStyleTemp = (HSSFCellStyle) this.xlsStyleMap.get(cellStyleID
					+ fontID + String.valueOf(nAlign));
		} catch (Exception getStylMapEx) {
			// 系統設定,style有誤
			xlsStyleTemp = this.xlsStyle;
			this.hasDataOutput = false;
			errorMsgList.add(Messages.getString("emisRptExcelProvider.205") + getStylMapEx.getMessage()); //$NON-NLS-1$
			throw new RuntimeException(Messages.getString("emisRptExcelProvider.206")); //$NON-NLS-1$

		}
		return xlsStyleTemp;

	}
/**
 * 用於產生輸出報表文件名
 * @author wayke 2005/12/07 [4656]
 * @param
 * @return String
 */
	public void setRptFileName(int theFileCnt) throws Exception{
		//判斷文件名容器是否已有文件名記錄
        if(fileNameHM.size()<=0){
        	//沒有則按原來獲取文件名之算法　取的一個文件名
			emisFileMgr _FMgr = emisFileMgr.getInstance(this.getContext());
			emisFileFactory _FFactory = _FMgr.getFactory();
			emisDirectory _oDir = _FFactory.getDirectory("report_out"); //$NON-NLS-1$
			String _sUserId = oUser_.getID();
			_oDir = _oDir.subDirectory(_sUserId);
			// emisReport會自動刪除.txt,Excel報表文件暫時以.txt命名(實為xls)
			String _sURI = emisUtil.getURIPrefix(oBusiness_.getRequest())
					+ oUser_.getUniqueID() + (new Date()).getTime();
			// TODO [3645]
			if (_xlsServerFileName == null || "".equals(_xlsServerFileName)) { //$NON-NLS-1$
				_xlsServerFileName = _sURI;
				excelFileName = _xlsServerFileName;
				// 取 FILE 所輸入報表檔名
				String _sFile = emisXMLUtl.getAttribute(eRoot_, "file"); //$NON-NLS-1$
				if ((_sFile != null) && (!"".equals(_sFile))) { //$NON-NLS-1$
					_xlsServerFileName = _sFile;
					excelFileName = _sFile;
				} else {
					// [3645]wing 添加輸出文件 PROP_REPORT_DIR
					if (null != oRequest_.getParameter("PROP_REPORT_DIR")) { //$NON-NLS-1$
						String dirPath = oRequest_.getParameter("PROP_REPORT_DIR"); //$NON-NLS-1$
						if (dirPath != null) {
							StringTokenizer token = new StringTokenizer(dirPath,
									"/"); //$NON-NLS-1$
							if (token.hasMoreTokens()) {
								_oDir = _FFactory.getDirectory(token.nextToken());
							} else {
								_oDir = _FFactory.getDirectory(dirPath);
							}
							while (token.hasMoreTokens()) {
								_oDir = _oDir.subDirectory(token.nextToken());
							}
						}
					}
					if (null != oRequest_.getParameter("PROP_REPORT_FILE")) { //$NON-NLS-1$
						_sFile = oRequest_.getParameter("PROP_REPORT_FILE"); //$NON-NLS-1$
						_xlsServerFileName = _sFile;
						excelFileName = _sFile;
					}

				}

				//按老算法取的文件名後 加文件序列號處理
				_xlsServerFileName = _oDir.getDirectory() + _xlsServerFileName + "_" + theFileCnt //$NON-NLS-1$
						+ ".xls"; //$NON-NLS-1$

			}
        } else {
          //如果存在則直接竊取文件名　更換文件序列號處理
		  String firstFileName = (String)fileNameHM.get(new Integer(1));

		  int ipoint = firstFileName.indexOf("."); //$NON-NLS-1$
		  int _inPoint = firstFileName.lastIndexOf("_"); //$NON-NLS-1$
		  _xlsServerFileName = firstFileName.substring(0,_inPoint+1) + theFileCnt + firstFileName.substring(ipoint,firstFileName.length());

		}

        int _iSXpoint = _xlsServerFileName.lastIndexOf("\\"); //$NON-NLS-1$
        int _iEXpoint = _xlsServerFileName.lastIndexOf("."); //$NON-NLS-1$
        excelFileName = _xlsServerFileName.substring(_iSXpoint+1,_iEXpoint);

        fileNameHM.put(new Integer(theFileCnt),_xlsServerFileName);

	}
	/**
	 * 創建zip檔案
	 *
	 * @throws Exception
	 */
	public void createXlsRptZip() throws Exception {
        if(havePrintData){
			try{
			  setRptFileName(iCount);
			}catch(Exception ei){
				ei.printStackTrace();
			}
			try {
				FileOutputStream fileOut = new FileOutputStream(_xlsServerFileName);
				// 頁頭大於0才splitPane
				if (splitPaneRowsCount > 0) {
					sheet.createFreezePane(0, splitPaneRowsCount, 0,
							splitPaneRowsCount);
				}
				try {
					getPs().setCopies((short) copies);
					//getPs().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
					if (printOrient.equals("L")) { //$NON-NLS-1$
						getPs().setLandscape(true);
					} else
						getPs().setLandscape(false);

          if (this.dLeftMargin > -1) {
            sheet.setMargin(HSSFSheet.LeftMargin, dLeftMargin);
          }
          if (this.dRightMargin > -1) {
            sheet.setMargin(HSSFSheet.RightMargin, dRightMargin);
          }
          if (this.dTopMargin > -1) {
            sheet.setMargin(HSSFSheet.TopMargin, dTopMargin);
          }
          if (this.dBottomMargin > -1) {
            sheet.setMargin(HSSFSheet.BottomMargin, dBottomMargin);
          }
           //add vince 20070117 start
          if (this.dPageHead > -1) {
            getPs().setHeaderMargin(dPageHead);
          }
          if (this.dPageFooter > -1) {
             getPs().setFooterMargin(dPageFooter);
          }
          if (this.shScacle > -1) {
            getPs().setScale(shScacle);
          }
          //add vince 20070117 end
          // 2011/01/17 Harry modify Track+[16678] 产出的excel报表放大显示比例为140%
          sheet.setZoom(7,5);

				} catch (Exception e) {
					e.printStackTrace();
				}
				xlsWb.write(fileOut);

				fileOut.close();
			}catch(Exception ei){
				ei.printStackTrace();
			}
        }
	}

	/**
	 * 目的: 判斷該Node是否符合列印條件
	 *
	 * @param o
	 *            要判斷之 Node 物件
	 * @return 是否符合列印mode的 boolean值
	 */

	protected boolean isReportMode(Node o) {
		boolean _bRet = false;
		String _subMode;
		String _sMode = emisRptXML.getAttribute(o, sModeAttr_);
		if (_sMode == null) {
			_bRet = true;
		} else {
			_bRet = false;
			StringTokenizer _stTemp = new StringTokenizer(_sMode, ","); //$NON-NLS-1$
			while (_stTemp.hasMoreTokens()) {
				_subMode = _stTemp.nextToken();
				_subMode.trim();
				_bRet = true;
				int _iXMLmodeLen = _subMode.length();
				int _iPropModeLen = sReportMode_.length();
				for (int i = 0; i < _iPropModeLen; i++) {
					if (i >= _iXMLmodeLen)
						break;
					char _cXMLmode = _subMode.charAt(i);
					char _cPropMode = sReportMode_.charAt(i);
					if (_cXMLmode != '?' && _cXMLmode != _cPropMode) {
						_bRet = false;
						break;
					}
				}
				if (_bRet) {
					break;
				}
			} // while
		} // if
		return _bRet;
	}

	/**
	 * 替代符號,過濾數字中的符號
	 *
	 * @param sExec
	 * @return
	 */
	private String fixUpExec(String sExec) {
		if ((sExec != null) && (!"".equals(sExec))) { //$NON-NLS-1$
			// 先轉成單一"\"型式
			sExec = emisUtil.stringReplace(sExec, ",", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sExec = emisUtil.stringReplace(sExec, "#", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// 再統一轉成雙"\\"型式
			sExec = emisUtil.stringReplace(sExec, "$", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sExec = emisUtil.stringReplace(sExec, "@", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sExec = emisUtil.stringReplace(sExec, "*", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sExec = emisUtil.stringReplace(sExec, "%", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return sExec;
	}

	/**
	 * xls及emisreport對齊方式的轉換
	 *
	 * @param alight
	 * @return
	 */
	public static short getXlsAlignment(int alight) {
		int Aligntment = HSSFCellStyle.ALIGN_LEFT;
		switch (alight) {
		case 1:
			Aligntment = (short) HSSFCellStyle.ALIGN_LEFT;
			break;
		case 2:
			Aligntment = (short) HSSFCellStyle.ALIGN_CENTER;
			break;
		case 3:
			Aligntment = (short) HSSFCellStyle.ALIGN_RIGHT;
			break;
		case 4:
			Aligntment = (short) HSSFCellStyle.ALIGN_FILL;
			break;
		case 5:
			Aligntment = (short) HSSFCellStyle.ALIGN_JUSTIFY;
			break;
		default:
			Aligntment = (short) HSSFCellStyle.ALIGN_LEFT;
			break;
		}
		return (short) Aligntment;

	}

	public void setCellStyles(String cellstyleID, HSSFCellStyle style) {
		if (cellstyleID == null)
			return;
		emisExcelReportCellStyle excelStyle = (emisExcelReportCellStyle) cellStyleMap
				.get(cellstyleID);
		if (null != excelStyle) {
			try {
				style.setBorderBottom(excelStyle.getBorderBottom());
			} catch (Exception e) {
				logger.debug("setBorderBottom" + e.getMessage()); //$NON-NLS-1$
			}
			try {
				style.setBorderLeft(excelStyle.getBorderLeft());
			} catch (Exception e) {
				logger.debug("setBorderLeft" + e.getMessage()); //$NON-NLS-1$
			}
			try {
				style.setBorderRight(excelStyle.getBorderRight());
			} catch (Exception e) {
				logger.debug("setBorderRight" + e.getMessage()); //$NON-NLS-1$
			}
			try {
				style.setBorderTop(excelStyle.getBorderTop());
			} catch (Exception e) {
				logger.debug("setBorderTop" + e.getMessage()); //$NON-NLS-1$
			}

			try {
				style.setBottomBorderColor(excelStyle.getBottomBorderColor());
			} catch (Exception e) {
				logger.debug("setBottomBorderColor" + e.getMessage()); //$NON-NLS-1$
			}
			try {
				style.setLeftBorderColor(excelStyle.getLeftBorderColor());
			} catch (Exception e) {
				logger.debug("setLeftBorderColor" + e.getMessage()); //$NON-NLS-1$
			}
			try {
				style.setRightBorderColor(excelStyle.getRightBorderColor());
			} catch (Exception e) {
				logger.debug("setRightBorderColor" + e.getMessage()); //$NON-NLS-1$
			}
			try {
				style.setTopBorderColor(excelStyle.getTopBorderColor());
			} catch (Exception e) {
				logger.debug("setTopBorderColor" + e.getMessage()); //$NON-NLS-1$
			}

			try {
				if (excelStyle.getFillForegroundColor() != 0) {
					style.setFillForegroundColor(excelStyle
							.getFillForegroundColor());
				}
			} catch (Exception e) {
				logger.debug("setFillForegroundColor" + e.getMessage()); //$NON-NLS-1$
			}

			try {
				if (excelStyle.getFillBackgroundColor() != 0) {
					style.setFillBackgroundColor(excelStyle
							.getFillBackgroundColor());
				}
			} catch (Exception e) {
				logger.debug("setFillBackgroundColor" + e.getMessage()); //$NON-NLS-1$
			}

			try {
				if (excelStyle.getSetFillPattern() != 0) {
					style.setFillPattern(excelStyle.getSetFillPattern());
				}
			} catch (Exception e) {
				logger.debug("getSetFillPattern" + e.getMessage()); //$NON-NLS-1$
			}
			try {
				//if (excelStyle.getVerticalAlignment() != 0) {
          style.setVerticalAlignment(excelStyle
							.getVerticalAlignment());
				//}
			} catch (Exception e) {
				logger.debug(Messages.getString("emisRptExcelProvider.253") //$NON-NLS-1$
						+ e.getMessage());
			}

      // wing 20070113 add start
      if (excelStyle.getDataFormart() != -1) {
        style.setDataFormat(excelStyle.getDataFormart());
      }
      // wing 20070113 add end
		}
	}

  //20070116 add vince get the heightest td of the tr  start
  public short getMaxHeightStyle(emisTr tr) {
    emisTd td = null;
    emisExcelReportCellStyle temp;
    //HSSFFont small;
    short maxRowHeight = -1, newRowHeight;//, fontHeight;

    for (int i = 0; i < tr.size(); i++) {
      newRowHeight = -1;
      fontHeight = -1;
      td = tr.get(i);
      if (td == null)
        continue;
      temp = (emisExcelReportCellStyle) cellStyleMap.get(td.getCellStyleID());
      //small = (HSSFFont) this.fontPoolMap.get(tr.get(i).getFontID());
      //if (small != null) {
      //  fontHeight = computFontHeight(small.getFontHeightInPoints());
      //}
      if (temp == null) {
        //newRowHeight = fontHeight;
        continue;
      } //else {
        //newRowHeight = temp.getRowHeight() >= fontHeight ? temp.getRowHeight() : fontHeight;
          newRowHeight = temp.getRowHeight();
      //}
      maxRowHeight = maxRowHeight >= newRowHeight ? maxRowHeight : newRowHeight;
    }
    return maxRowHeight;
  }

  public short computFontHeight(short fontHeightInPoints) {
    short fontHeight = fontHeightInPoints;
    if (fontHeight >= 35) {
      fontHeight += 11;
    } else if (fontHeight >= 25) {
      fontHeight += 9;
    } else if (fontHeight >= 17) {
      fontHeight += 8;
    } else if (fontHeight >= 13) {
      fontHeight += 5;
    } else if (fontHeight >= 11) {
      fontHeight += 4;
    }
    return fontHeight;
  }
  //20070116 vince add end

	public String outputErrorMsg() {
		StringBuffer errBuffer = new StringBuffer();
		for (int i = 0; i < this.errorMsgList.size(); i++) {
			this.hasDataOutput = false;
			errBuffer.append((String) errorMsgList.get(i));
		}
		if (!hasDataOutput)
			return errBuffer.toString();
		else
			return ""; //$NON-NLS-1$

	}

	public void debug(String sStr) {
		oBusiness_.debug(sStr);
	}

	public emisBusiness getBusiness() {
		return oBusiness_;
	}

	public String getParameter(String sName) {
		if (oRequest_ != null) {
			return oRequest_.getParameter(sName);
		}
		return ""; //$NON-NLS-1$
	}

	public Node getRoot() {
		return eRoot_;
	}

	/**
	 * @return Returns the cell.
	 */
	public HSSFCell getCell() {
		return cell;
	}

	/**
	 * @param cell
	 *            The cell to set.
	 */
	public void setCell(HSSFCell cell) {
		this.cell = cell;
	}

	/**
	 * @return Returns the row.
	 */
	public HSSFRow getRow() {
		return row;
	}

	/**
	 * @param row
	 *            The row to set.
	 */
	public void setRow(HSSFRow row) {
		this.row = row;
	}

	/**
	 * @return Returns the sheet.
	 */
	public HSSFSheet getSheet() {
		return sheet;
	}

	/**
	 * @param sheet
	 *            The sheet to set.
	 */
	public void setSheet(HSSFSheet sheet) {
		this.sheet = sheet;
	}

	/**
	 * @return Returns the style.
	 */
	public HSSFCellStyle getStyle() {
		return xlsStyle;
	}

	/**
	 * @param xlsStyle
	 *            The style to set.
	 */
	public void setStyle(HSSFCellStyle xlsStyle) {
		this.xlsStyle = xlsStyle;
	}

	/**
	 * @return Returns the xlsWb.
	 */
	public HSSFWorkbook getXlsWb() {
		return xlsWb;
	}

	/**
	 * @param xlsWb
	 *            The xlsWb to set.
	 */
	public void setXlsWb(HSSFWorkbook xlsWb) {
		this.xlsWb = xlsWb;

	}

	/**
	 * @return Returns the allRowCount_.
	 */
	public int getAllRowCount() {
		return allRowCount_;
	}

	/**
	 * @param allRowCount
	 *            The allRowCount to set.
	 */
	public void setAllRowCount(int allRowCount) {
		allRowCount_ = allRowCount;
	}

	/**
	 * @return Returns the columnCount_.
	 */
	public int getColumnCount() {
		return columnCount_;
	}

	/**
	 * @param columnCount
	 *            The columnCount to set.
	 */
	public void setColumnCount(int columnCount) {
		columnCount_ = columnCount;
	}

	/**
	 * @return Returns the columnDataRowSettingList.
	 */
	public ArrayList getColumnDataRowSettingList() {
		return columnDataRowSettingList;
	}

	/**
	 * @param columnDataRowSettingList
	 *            The columnDataRowSettingList to set.
	 */
	public void setColumnDataRowSettingList(ArrayList columnDataRowSettingList) {
		this.columnDataRowSettingList = columnDataRowSettingList;
	}

	/**
	 * @return Returns the maxColumns_.
	 */
	public int getMaxColumns() {
		return maxColumns_;
	}

	/**
	 * @param maxColumns
	 *            The maxColumns_ to set.
	 */
	public void setMaxColumns(int maxColumns) {
		maxColumns_ = maxColumns;
	}

	/**
	 * @return Returns the hasDataOutput.
	 */
	public boolean isHasDataOutput() {
		return hasDataOutput;
	}

	/**
	 * @param hasDataOutput
	 *            The hasDataOutput to set.
	 */
	public void setHasDataOutput(boolean hasDataOutput) {
		this.hasDataOutput = hasDataOutput;
	}

	/**
	 * @return Returns the fontName.
	 */
	public String getFontName() {
		return fontName;
	}

	/**
	 * @param fontName
	 *            The fontName to set.
	 */

	/**
	 * @return Returns the excelFileName.
	 */
	public String getExcelFileName() {
		return excelFileName;
	}

	/**
	 * @return Returns the columnMaxFontIDList.
	 */
	public ArrayList getColumnMaxFontIDList() {
		return columnMaxFontIDList;
	}

	/**
	 * @param columnMaxFontIDList
	 *            The columnMaxFontIDList to set.
	 */
	public void setColumnMaxFontIDList(ArrayList columnMaxFontIDList) {
		this.columnMaxFontIDList = columnMaxFontIDList;
	}

	/**
	 * @return Returns the splitPaneRowsCount.
	 */
	public int getSplitPaneRowsCount() {
		return splitPaneRowsCount;
	}

	/**
	 * @param splitPaneRowsCount
	 *            The splitPaneRowsCount to set.
	 */
	public void setSplitPaneRowsCount(int splitPaneRowsCount) {
		this.splitPaneRowsCount = splitPaneRowsCount;
	}

	/**
	 * @return Returns the fontPoolMap.
	 */
	public HashMap getFontPoolMap() {
		return fontPoolMap;
	}

	/**
	 * @param fontPoolMap
	 *            The fontPoolMap to set.
	 */
	public void setFontPoolMap(HashMap fontPoolMap) {
		this.fontPoolMap = fontPoolMap;
	}

	/**
	 * @return Returns the cellStyleMap.
	 */
	public HashMap getCellStyleMap() {
		return cellStyleMap;
	}

	/**
	 * @param cellStyleMap
	 *            The cellStyleMap to set.
	 */
	public void setCellStyleMap(HashMap cellStyleMap) {
		this.cellStyleMap = cellStyleMap;
	}

	/**
	 * @return Returns the defaultfontIDmap.
	 */
	public HashMap getDefaultfontIDmap() {
		return defaultfontIDmap;
	}

	/**
	 * @return Returns the defaultStyleIDmap.
	 */
	public HashMap getDefaultStyleIDmap() {
		return defaultStyleIDmap;
	}

	/**
	 * @return Returns the errorMsgList.
	 */
	public ArrayList getErrorMsgList() {
		return errorMsgList;
	}

	// 添加公共接口，加強列印功能
	public HSSFPrintSetup getPs() {
		//if (ps == null)
			ps = sheet.getPrintSetup();
		return ps;
	}

	public void setPs(HSSFPrintSetup ps) {
		this.ps = ps;
	}

	/**
	 * HSSFWorkbook workbook=xlsProvider.getXlsWb();<br>
	 * cell.setCellStyle(percent(workbook)); <br>
	 * cell.setCellFormula("A1/A2"); <br>
	 * 取得百分比
	 *
	 * @param wb
	 * @return
	 */
	public static HSSFCellStyle percent(HSSFWorkbook wb) {
		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00%")); //$NON-NLS-1$
		return cellStyle;

	}

	/**
	 * 方便改寫,取得xls,合併:wing
	 *
	 * @return
	 */
	public String get_xlsServerFileName() {
		return _xlsServerFileName;
	}

	public void set_xlsServerFileName(String serverFileName) {
		_xlsServerFileName = serverFileName;
	}

	public void setExcelFileName(String excelFileName) {
		this.excelFileName = excelFileName;
	}

	public int getICount() {
		return iCount;
	}

	public void setICount(int count) {
		iCount = count;
	}

	public boolean isHasNewPage_() {
		return hasNewPage_;
	}
	public void setHasNewPage_(boolean hasNewPage_) {
		this.hasNewPage_ = hasNewPage_;
	}


	public void freeFileNameHM() {
		fileNameHM.clear();
	}

	public HashMap getFileNameHM() {
		return fileNameHM;
	}

	public boolean isHavePrintData() {
		return havePrintData;
	}

	public void setHavePrintData(boolean havePrintData) {
		this.havePrintData = havePrintData;
	}

	public int getCurPageHeaderLine() {
		return curPageHeaderLine;
	}

	public void setCurPageHeaderLine(int curPageHeaderLine) {
		this.curPageHeaderLine = curPageHeaderLine;
	}

	public boolean isPrintPageHeader() {
		return isPrintPageHeader;
	}

	public void setPrintPageHeader(boolean isPrintPageHeader) {
		this.isPrintPageHeader = isPrintPageHeader;
	}

	public boolean isPrintPageFooter() {
		return isPrintPageFooter;
	}

	public void setPrintPageFooter(boolean isPrintPageFooter) {
		this.isPrintPageFooter = isPrintPageFooter;
	}

	public boolean isNewPageLater() {
		return isNewPageLater;
	}

	public void setNewPageLater(boolean isNewPageLater) {
		this.isNewPageLater = isNewPageLater;
	}

  // start 2008/04/12 add by andy: 取得response对象
  private emisHttpServletResponse response_;

  public void setHttpServletResponse(emisHttpServletResponse response_) {
    this.response_ = response_;

  }
  // end

}
