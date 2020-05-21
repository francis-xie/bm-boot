/* $Id: emisPrintData.java 26 2015-06-02 01:40:19Z andy.he $
 *
 * Copyright (c) EMIS Corp. All Rights Reserved.
 *
 */
package com.emis.business;

import com.emis.classloader.emisLoaderMgr;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.messageResource.Messages;
import com.emis.report.emisReport;
import com.emis.report.emisRptProvider;
import com.emis.report.emisRptTextProvider;
import com.emis.report.excel.emisRptExcelProvider;
import com.emis.report.excel.emisRptSingleExcelProvider;
import com.emis.report.wrep.emisRptWrepProvider;
import com.emis.trace.emisError;
import com.emis.trace.emisTracer;
import com.emis.util.emisUtil;
import com.emis.util.emisXMLUtl;
import org.w3c.dom.Element;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * 負責處理 XML tag 中 <report>的部份 <BR>
 * 11/02 wing add excel report output<BR>
 * 2005/07/11 [3645] wing 添加報表特別ACTION轉向功能:extendPrintDataAction<BR>
 * 2005/12/12 [4656] wing review [wayke]
 * 據EPOS需求,12月中甸前完成功能:突破POI對XLS支持有限文檔大小4MB,當查詢數據量較大時,
 * 修改報表CORE對同一查詢數據產生連續的XLS文檔S,並同時提供多個XLS文檔DOWN的功能
 *
 * @author Robert
 * @version 2006/01/04 [4807] wing wrep,excel一頁式報表實做
 * @version 2006/08/15 [6304] Jerry 直接叫用Excel.exe改成叫用.xls檔案
 * @version 2010/02/04 [14335] wing WREP报表修改 WREP_OUTPUT_CHARSET输出只有两种格式
 *          MS950,GBK
 * @version  2010/02/05 [14334] wing emisRptTextProvider文本报表 RPT_OUTPUT_CHARSET request参数
 *           报表文件输出类型，用于盘点机下传档案产生
 */
public class emisPrintData extends emisAction {
	/**
	 * XML列印處理.
	 *
	 * @param oBusiness
	 * @param oRoot
	 * @param out
	 * @throws Exception
	 */
	protected emisPrintData(emisBusiness oBusiness, Element oRoot, Writer out) throws Exception {
		super(oBusiness, oRoot, out);
		// BasicConfigurator.configure();
	}

	boolean bUseParam_; // 當target, dir & file都指定的話，才

	// 依參數指示，否則一切照舊(wshow做法)
	boolean bTarget_;

	boolean bDir_;

	boolean bFile_;

	boolean bExec_;

	boolean bAppend_; // 當產生server檔案時，若FILEMODE="append"

	// 則檔案以append方式處理
	boolean bMsg_; // 當EXEC不執行時，預設為顯示報表檔名

  private Map<Integer,String> xlsFileName = new HashMap<Integer,String>();
	/**
	 * 執行.
	 *
	 * @throws Exception
	 */
	public void doit() throws Exception {
		// because the BusinessBean Object is possible to be re-used , we
		// have to reset this member variables
		bUseParam_ = false;
		bTarget_ = false;
		bDir_ = false;
		bFile_ = false;
		bExec_ = false;
		bAppend_ = false;
		bMsg_ = true;
		boolean bWrepReport = false;

		Class _oRptClass = null;
		// 處理class
		String _sClassName = emisXMLUtl.getAttribute(eRoot_, "class");
		if (_sClassName == null) {
			emisTracer.get(oContext_).sysError(this, emisError.ERR_CLASS_NO_SET_CLASSNAME, "printdata");
		}

		boolean _isNonPrint = "preview".equals(emisXMLUtl.getAttribute(eRoot_, "prtmode")) ? true : false;

		// [3645]用於一些比較特殊的報表轉向
		if (request_.getParameter("RPT_EXTENDS_RPTACTIONS") != null) {
			// 處理class
			String extendRptActionClass = emisXMLUtl.getAttribute(eRoot_, "extendRptAct");
			if (extendPrintDataAction(extendRptActionClass, oBusiness_, eRoot_, out_)) {
				return;
			}
		}
		// Load report class
		try {
			emisLoaderMgr oLoaderMgr = emisLoaderMgr.getInstance(oContext_);
			_oRptClass = oLoaderMgr.findClass(_sClassName);
		} catch (Exception e) {
			oBusiness_.debug(e.getMessage());
			throw e;
		}
		oBusiness_.debug("Get Report Class:" + _oRptClass);

		// 取 EXEC 所輸入之顯示報表程式，可處理"\","\\"&"/"，都轉換成"\\"以符合
		// JavaScript format
		String _sExec = emisXMLUtl.getAttribute(eRoot_, "exec");
		_sExec = fixUpExec(_sExec);

		// 取 TARGET 所指定輸出之地點(server or client)
		String _sTarget = emisXMLUtl.getAttribute(eRoot_, "target");
		if ((_sTarget != null) && (!"".equals(_sTarget))) {
			_sTarget = _sTarget.toLowerCase();
			bTarget_ = true;
		}

		// 將 DIR 所輸入之字串轉成正確的目錄名稱，可處理".","/","\\"&"\"等字元
		String _sDir = emisXMLUtl.getAttribute(eRoot_, "dir");
		_sDir = fixUpDir(_sDir);

		// 取 FILE 所輸入報表檔名
		String _sFile = emisXMLUtl.getAttribute(eRoot_, "file");
		if ((_sFile != null) && (!"".equals(_sFile))) {
			bFile_ = true;
		}

		// 取 FILEMODE 所輸入檔案模式
		String _sFileMode = emisXMLUtl.getAttribute(eRoot_, "filemode");
		if ((_sFileMode != null) && "append".equals(_sFileMode.toLowerCase())) {
			bAppend_ = true;
		}

		// 取 MESSAGE 設定值
		String _sMsg = emisXMLUtl.getAttribute(eRoot_, "message");
		if ((_sMsg != null) && "false".equals(_sMsg.toLowerCase())) {
			bMsg_ = false;
		}

		// 檢查request_參數中是否有PROP_REPORT_xxx，其優先權大於在XML中的Tag指定
		// 且request_參數可以和XML中的指定參數'or'起來用

		// 測試證明所有參數值會一併存入_sValues中，所以用_param來取出正確的值
		// System.out.print(_sProp+"=");
		// for(int i=0; i<_sValues.length; i++)
		// {
		// System.out.print("["+_sValues[i]+"]");
		// System.out.println("["+_sValues[_param]+"]");
		// }
		// System.out.println();

		String _sValue = request_.getParameter("PROP_REPORT_EXEC");
		if ((_sValue != null) && (!"".equals(_sValue))) {
			_sExec = fixUpExec(_sValue);
		}

		_sValue = request_.getParameter("PROP_REPORT_TARGET");
		if ((_sValue != null) && (!"".equals(_sValue))) {
			_sTarget = _sValue;
			_sTarget = _sTarget.toLowerCase();
			bTarget_ = true;
		}

		_sValue = request_.getParameter("PROP_REPORT_DIR");
		if ((_sValue != null) && (!"".equals(_sValue))) {
			_sDir = fixUpDir(_sValue);
		}

		_sValue = request_.getParameter("PROP_REPORT_FILE");
		if ((_sValue != null) && (!"".equals(_sValue))) {
			_sFile = _sValue;
			bFile_ = true;
		}

		_sValue = request_.getParameter("PROP_REPORT_FILEMODE");
		if ((_sValue != null) && (!"".equals(_sValue))) {
			_sFileMode = _sValue;
			if ((_sFileMode != null) && "append".equals(_sFileMode.toLowerCase())) {
				bAppend_ = true;
			}
		}

		_sValue = request_.getParameter("PROP_REPORT_MESSAGE");
		if ((_sValue != null) && (!"".equals(_sValue))) {
			_sMsg = _sValue;
			if ((_sMsg != null) && "false".equals(_sMsg.toLowerCase())) {
				bMsg_ = false;
			}
		}

		_sValue = request_.getParameter("PROP_REPORT_PRTMODE");
		if ((_sValue != null) && (!"".equals(_sValue))) {
			if ("preview".equalsIgnoreCase(_sValue)) {
				_isNonPrint = true;
			}
		}
		// 當target, dir & file都存在的時候才以exec來顯示報表，若exec不存在則
		// 視message設定值來決定是否顯示報表檔名於螢幕上
		if (bTarget_ && bDir_ && bFile_) {
			bUseParam_ = true;
		}

		if (bExec_) {
			bMsg_ = false;
		}

		emisFileMgr _oFMgr = emisFileMgr.getInstance(super.oContext_);

		// 是否快速列印
		boolean _isQuickPrint = "true".equalsIgnoreCase(emisXMLUtl.getAttribute(eRoot_, "quickprint"));

		// 報表先產生在:$root/report_out/userid/*.txt下
		// PS:_sFile是file指定的報表檔名
		// _sTmpFile是系統內定的報表檔名
		emisDirectory _oRoot = _oFMgr.getDirectory("root");
		emisDirectory _oDir = _oFMgr.getDirectory("report_out");
		String _sUserId = oUser_.getID();
		_oDir = _oDir.subDirectory(_sUserId);
		// String _sTmpFile = oBusiness_.getID() + oUser_.getUniqueID()+".txt";

		/*
		 * 修改 [專案 EROS] 事項 1055 -- KC 2004/10/12 報表檔名加上時間, 以避免相同 User 產生相同檔名
		 * Wshow 無法開檔
		 */
		String _sTmpFile = emisUtil.getURIPrefix(oBusiness_.getRequest()) + oUser_.getUniqueID()
				+ (new Date()).getTime() + ".txt";

		OutputStream out = _oFMgr.getFactory().getOutStream(_oDir, _sTmpFile, null);

		oUser_.setAttribute("RPT_FILE", _oDir.getDirectory() + _sTmpFile);

		// 利用dynamic/java報表程式產生報表
		// 以下這個,是把寫出的 File Name , 改 mail API 用 attachment 用的
		// [3645]擴展action
		String _sExtendsClassName = emisXMLUtl.getAttribute(eRoot_, "extendsClassName");
		// [3645] wing添加擴展的action功能
		if (_sExtendsClassName != null) {
			emisRptProvider Provider = null;
			try {

				emisLoaderMgr oLoaderMgrEx = emisLoaderMgr.getInstance(oContext_);
				Class actionS = null;
				actionS = oLoaderMgrEx.findClass(_sExtendsClassName);

				PrintWriter p = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));

				if (request_.getParameter("RPT_OUTER_TYPE") != null
						&& "EXCEL".equals(request_.getParameter("RPT_OUTER_TYPE"))) {
					Provider = new emisRptExcelProvider(oBusiness_, eRoot_);
				} else {
					Provider = new emisRptTextProvider(oBusiness_, eRoot_, p);
				}
				Class[] _oArray = { com.emis.report.emisRptProvider.class };
				Object[] _objArray = { Provider };
				Constructor othConstructor = _oRptClass.getConstructor(_oArray);

				emisReport otherRpt = (emisReport) othConstructor.newInstance(_objArray);
				otherRpt.printRpt();
				return;
			} catch (Exception e) {
				oBusiness_.debug(e.getMessage());
				throw e;
			} finally {
				if (null != Provider)
					Provider.close();
			}
		}

		boolean otherReport = false;
		emisRptProvider oProvider = null;

		try {
			PrintWriter p = null;
			//修改空bug
			p = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
			Class[] _oArray = { com.emis.report.emisRptProvider.class };
			Constructor _oConstructor = _oRptClass.getConstructor(_oArray);

			String sOutputType = request_.getParameter("RPT_OUTER_TYPE");

			if ("EXCEL".equals(sOutputType)) {
				// [4807]一頁式報表設定參數EXCEL_SINGLE_PAGE
				String _sSinglePage = request_.getParameter("EXCEL_SINGLE_PAGE");
				if(null!=_sSinglePage&&"true".equalsIgnoreCase(_sSinglePage)){
				   oProvider = new emisRptSingleExcelProvider(oBusiness_, eRoot_);
				}else
				  oProvider = new emisRptExcelProvider(oBusiness_, eRoot_);
			    emisBusinessImpl business = (emisBusinessImpl) oBusiness_;
			    emisRptExcelProvider excelPP = (emisRptExcelProvider) oProvider;
			    excelPP.setHttpServletResponse(business.getResponse_());
			    this.setHttpResposne(business.getResponse_());
				otherReport = true;
			} else {
				//[4807]Wrep图形报表
                String _sWrepReport= request_.getParameter("WREP");
                if(null!=_sWrepReport&&"true".equalsIgnoreCase(_sWrepReport)){
                   bWrepReport=true;
                   oProvider = new emisRptWrepProvider(oBusiness_, eRoot_, p);
                }else{
				   oProvider = new emisRptTextProvider(oBusiness_, eRoot_, p);
		        }
				otherReport = false;
			}

			try {
				Object[] _objArray = { oProvider };

				emisReport _oRpt = (emisReport) _oConstructor.newInstance(_objArray);
				_oRpt.printRpt();

			} catch (Exception e) {
				oBusiness_.debug(e.getMessage());
				throw e;
			} finally {
				oProvider.close();
			}


			p.flush();

			// } catch (Exception eee) {
			// logger.info("load" + eee.getMessage());
		} finally {
			out.close();
		}

		if (otherReport) {
			// [4807]EXCEL_SINGLE_PAGE一頁式報表設定參數EXCEL_SINGLE_PAGE
			emisRptExcelProvider xlsProvider = (emisRptExcelProvider) oProvider;
			// call excel.exe,並結束流程
			xlsProvider.createXlsRptZip();
			// [4807] 2005/12/06 wing review [wayke] 加入多文件開啟
			xlsProvider.setICount(1);
			if(xlsProvider.getFileNameHM()!=null){
			    genExcelFileCaller(xlsProvider.getFileNameHM());
			}else {
			    //genExcelFileCaller(xlsProvider.getExcelFileName());
			}

      this.xlsFileName.putAll(xlsProvider.getFileNameHM());
      xlsProvider.freeFileNameHM();
			return;
		}

		// 是否指定產生server報表名稱
		if (bUseParam_ && "server".equals(_sTarget)) {
			emisFile _oSrcFile = _oDir.getFile(_sTmpFile);

			// 如果user指定了server的磁碟機名稱，則略去磁碟機名，以避免拷貝到$root之外
			if (_sDir.charAt(1) == ':') {
				_sDir = _sDir.substring(2);
			}
			emisDirectory _oTarDir = _oFMgr.getDirectory("root").subDirectory(_sDir);

			// 將_sTmpFile拷貝到server的指定目錄下_sFile，可能為overwrite及append兩種
			_oSrcFile.copyTo(_oTarDir, _sFile, bAppend_);

			// 如為append模式，則將產生的報表拷貝_sTmpFile，以備下傳時正確顯示
			if (bAppend_) {
				emisFile _oTarFile = _oTarDir.getFile(_sFile);
				_oTarFile.copyTo(_oDir, _sTmpFile);
			}
		}

		java.util.Random r = new Random();
		int rand = r.nextInt(20) + 1;

		// generate javaScript to call editor
		// 將server所產生的報表下傳至client後，以編輯程式顯示在螢幕上
		String _URL;
		String _sServerAddress = emisShowData.getServerAddress(oContext_, request_);

		_URL = (request_.isSecure() ? "https" : "http") + "://"
				+ _sServerAddress + ":" + request_.getServerPort()
				+ _oRoot.subDirectory("servlet").getRelative() + // "/epos/servlet/"
				"com.emis.servlet.loader.emisDataLoader" + rand + "?FILE="
				+ _oDir.getJavaScriptDirectory() + _sTmpFile;

		String _sClientFileName = "C:\\\\temp\\\\" + _sTmpFile;
		// 如果有指定client報表名稱，則改變下傳檔名
		if (bUseParam_ && "client".equals(_sTarget)) {
			// 如果Client指定的目錄不包含':"且1st byte不是'\'，表示在$root下
			if ((_sDir.indexOf(":") == -1) && (_sDir.charAt(0) != '\\')) {
				_sDir = _oRoot.getDirectory() + _sDir;
			}
			_sClientFileName = _sDir + "\\" + _sFile;
			_sClientFileName = emisUtil.stringReplace(_sClientFileName, "\\", "\\\\", "a");
		}
		StringBuffer _sBuf = new StringBuffer();
		_sBuf.append("<script>\n");
		// _sBuf.append(" alert('"+_URL+"');");
		// _sBuf.append("
		// xmlUtil.download('"+_URL+"','"+_sClientFileName+"');\n");
		_sBuf.append("  xmlUtil.downloadx('" + _URL + "','" + _sClientFileName + "','" + oUser_.getUniqueID()
				+ "');\n");

		if (bUseParam_) {
			if (bExec_) {
				// 若是执行外部编辑程式，则不处理quickprint Tag
				_sBuf.append("  var Result = xmlUtil.execute('" + _sExec
						+ "','" + _sClientFileName + "');\n");
				_sBuf.append("  emisCheckExecuteError(Result, '" + _sExec
						+ "');\n");
			} else if (bMsg_) {
				if ("server".equals(_sTarget)) {
					_sClientFileName = "[Server] "
							+ _oFMgr.getDirectory("root").subDirectory(_sDir).getDirectory() + _sFile;
					_sClientFileName = emisUtil.stringReplace(_sClientFileName, "\\", "\\\\", "a");
				}
				_sBuf.append("  alert('" + _sClientFileName + Messages.getString("emisPrintData.83"));
			}
		} else {

			// //[4807]txt出現的概率要比WREP報表高
			if (!bWrepReport) {
				_sBuf.append("  var Result = xmlUtil.execute('c:\\\\emis\\\\bin\\\\wshow.exe','"
						+ _sClientFileName + (_isQuickPrint ? (" /AP") : "") + (_isNonPrint ? " /NP" : "")
						+ "');\n");
				_sBuf.append("  emisCheckExecuteError(Result, 'wshow.exe');\n");
			} else {
				// [4807] wrep圖形報表
				_sBuf.append("  var Result = xmlUtil.execute('c:\\\\emis\\\\bin\\\\Wrep.exe','"
						+ _sClientFileName + "');\n");
				_sBuf.append("  emisCheckExecuteError(Result, 'wrep.exe');\n");
			}
		}
		_sBuf.append("</script>\n");
		out_.write(_sBuf.toString());
	}

	/**
	 * 目的:產生call excel.exe的xmlUtil' script
	 *
	 * @param _sTmpFile
	 * @throws Exception
	 */
	public void genExcelFileCaller(String _sTmpFile) throws Exception {

		emisFileMgr _oFMgr = emisFileMgr.getInstance(super.oContext_);
		emisDirectory _oRoot = _oFMgr.getDirectory("root");
		emisDirectory _oDir = _oFMgr.getDirectory("report_out");
		String _sUserId = oUser_.getID();
		_oDir = _oDir.subDirectory(_sUserId);
		java.util.Random r = new Random();
		int rand = r.nextInt(20) + 1;

		String _URL;
		String _sServerAddress = emisShowData.getServerAddress(oContext_,
				request_);
		_URL = (request_.isSecure() ? "https" : "http") + "://"
				+ _sServerAddress + ":" + request_.getServerPort()
				+ _oRoot.subDirectory("servlet").getRelative() + // "/epos/servlet/"
				"com.emis.servlet.loader.emisDataLoader" + rand + "?FILE="
				+ _oDir.getJavaScriptDirectory() + _sTmpFile + ".xls";
		String _sClientFileName = "C:\\\\temp\\\\"
				+ fixUpExec(_sTmpFile + ".xls");
		// 如果有指定client报表名称，则改变下传档名
		StringBuffer _sBuf = new StringBuffer();
		_sBuf.append("<script>\n");
		_sBuf.append(" try{ ");
		_sBuf.append("  xmlUtil.download('" + _URL + "','" + _sClientFileName
				+ "');\n");
		_sBuf.append("xmlUtil.execute('" + _sClientFileName
				+ "','');}");  // 2006/09/18 Jerry
//  	_sBuf.append("  var Result = xmlUtil.execute('" + _sExec
//						+ "','" + _sClientFileName + "');\n");

		_sBuf.append("catch(e){" + Messages.getString("emisPrintData.82")
				+ "}");
		_sBuf.append("</script>\n");
		out_.write(_sBuf.toString());
	}

	/**
	 * 重寫genExcelFileCaller方法 將參數改為 HashMap 以打開多個Excal文件
	 *
	 * @author wayke 2005/12/07
	 * @param fileNameHM
	 */
	public void genExcelFileCaller(HashMap fileNameHM) throws Exception {
		if(fileNameHM != null){
      if("Y".equalsIgnoreCase(request_.getParameter("RPT_OCX_SHOW"))){   //前端指定用OCX下载方式
        for(int i=0;i<fileNameHM.size();i++){
          String fileName = (String)fileNameHM.get(new Integer(i+1));
          int _iSXpoint = fileName.lastIndexOf("\\");
          int _iEXpoint = fileName.lastIndexOf(".");
          genExcelFileCaller(fileName.substring(_iSXpoint+1,_iEXpoint));
        }
      } else {
        //　在网页中直接开启档案的处理，如果是多个档时以压缩档的方式打开。
        String fileName = "";
        int _iSXpoint = 0;
        int _iEXpoint = 0;
        emisFileMgr _oFMgr = emisFileMgr.getInstance(super.oContext_);
        emisDirectory _oDir = _oFMgr.getDirectory("report_out");
        String _sUserId = oUser_.getID();
        _oDir = _oDir.subDirectory(_sUserId);
        ArrayList oList = new ArrayList();
        for(int i=0;i<fileNameHM.size();i++){
          fileName = (String)fileNameHM.get(new Integer(i+1));
          _iSXpoint = fileName.lastIndexOf("\\");
          _iEXpoint = fileName.lastIndexOf(".");
          oList.add(new File(_oDir.getJavaScriptDirectory() + fileName.substring(_iSXpoint+1,_iEXpoint) + ".xls"));
        }
        response_.reset();
        response_.setHeader("Pragma", "");
        response_.setHeader("Cache-Control", "");
        response_.setContentType("application/whatever");
        if(oList.size() > 1) { // 多个文件，以压缩档下载。
          response_.setHeader("Content-Disposition", "attachment; filename=" + fileName.substring(_iSXpoint+1,_iEXpoint) + ".ZIP;"); //直接開啟檔案
          emisUtil.downloadZip(oList,response_);
        } else { //单个文件
          response_.setHeader("Content-Disposition", "attachment; filename=" + fileName.substring(_iSXpoint+1,_iEXpoint) + ".xls;"); //直接開啟檔案
          emisUtil.downloadFile((File)oList.get(0),response_);
        }
      }
    }
	}

	/**
	 * fixUpExec() EXEC必須轉成JavaScript檔案格式
	 *
	 * @param sExec
	 * @return
	 */
	private String fixUpExec(String sExec) {
		if ((sExec != null) && (!"".equals(sExec))) {
			// 先轉成單一"\"型式
			sExec = emisUtil.stringReplace(sExec, "/", "\\", "a");
			sExec = emisUtil.stringReplace(sExec, "\\\\", "\\", "a");
			// 再統一轉成雙"\\"型式
			sExec = emisUtil.stringReplace(sExec, "\\", "\\\\", "a");

			bExec_ = true;
		}
		return sExec;
	}

	/**
	 * 調整目錄字串.
	 *
	 * @param sDir
	 * @return
	 */
	private String fixUpDir(String sDir) {
		if ((sDir != null) && (!"".equals(sDir))) {
			sDir = emisUtil.stringReplace(sDir, "/", "\\", "a");
			sDir = emisUtil.stringReplace(sDir, ".", "\\", "a");
			sDir = emisUtil.stringReplace(sDir, "\\\\", "\\", "a");

			bDir_ = true;
		}
		return sDir;
	}

	// [3645]用於待後會實現的複雜文本報表擴展,XLS報表擴展<br>
	// PDF,CVS,doc等資料輸出
	// 實驗性報表,一律取報表動態目錄下的CLASS<br>
	// 經較長時間後才加入穩定的SRC模組中<br>
	public boolean extendPrintDataAction(String extendRptActionClass, emisBusiness oBusiness, Element oRoot,
			Writer out) {
		emisAction extendsPrintData = null;
		boolean runBackPrintAction = false; // 不會執行原emisPrintData後面的動作
		if (request_.getParameter("RPT_ACTION_RUN_BACK") != null)
			runBackPrintAction = true;
		try {
			// 從dynamic下載入ＣＬＡＳＳ
			if (extendsPrintData != null) {
				extendsPrintData.set(oBusiness, oRoot, out_);
			} else {
				emisLoaderMgr oLoaderMgr = emisLoaderMgr.getInstance(oContext_);
				Class actionS = null;
				actionS = oLoaderMgr.findClass(extendRptActionClass);
				Class[] classType = new Class[] { com.emis.business.emisBusiness.class,
						org.w3c.dom.Element.class, java.io.Writer.class };
				Object[] param = new Object[] { oBusiness, oRoot, out_ };
				Object oAction = actionS.getConstructor(classType).newInstance(param);
				extendsPrintData = (emisAction) oAction;
			}
			extendsPrintData.doit();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return runBackPrintAction;
	}

	public String genJsString(String _sTmpFile, ServletContext oContext_, HttpServletRequest request_)
			throws Exception {
		emisFileMgr _oFMgr = emisFileMgr.getInstance(oContext_);
		emisDirectory _oRoot = _oFMgr.getDirectory("root");
		emisDirectory _oDir = _oFMgr.getDirectory("report_out");
		String _sUserId = oUser_.getID();
		_oDir = _oDir.subDirectory(_sUserId);
		java.util.Random r = new Random();
		int rand = r.nextInt(20) + 1;

		String _URL;
		String _sServerAddress = emisShowData.getServerAddress(oContext_,
				request_);
		_URL = (request_.isSecure() ? "https" : "http") + "://"
				+ _sServerAddress + ":" + request_.getServerPort()
				+ _oRoot.subDirectory("servlet").getRelative() + // "/epos/servlet/"
				"com.emis.servlet.loader.emisDataLoader" + rand + "?FILE="
				+ _oDir.getJavaScriptDirectory() + _sTmpFile + ".txt";
		String _sClientFileName = "C:\\\\temp\\\\"
				+ fixUpExec(_sTmpFile + ".xls");
		// 如果有指定client报表名称，则改变下传档名
		StringBuffer _sBuf = new StringBuffer();
		_sBuf.append("<script>\n");
		_sBuf.append(" try{ ");
		_sBuf.append("  xmlUtil.download('" + _URL + "','" + _sClientFileName
				+ "');\n");
		_sBuf.append("xmlUtil.execute('" + _sClientFileName + "','');}");  // [6304] Jerry
		_sBuf.append("catch(e){" + Messages.getString("emisPrintData.81")
				+ "}");
		_sBuf.append("</script>\n");
		return _sBuf.toString();
	}

	//WING 20071105 ADD START
	public emisHttpServletResponse response_;
	public void setHttpResposne(emisHttpServletResponse response_) {
		this.response_ = response_;
	}

  public Map<Integer,String> getXlsFileName(){
    return this.xlsFileName;
  }

}
