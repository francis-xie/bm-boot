package com.emis.report.wrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.emis.business.emisBusiness;
import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.messageResource.Messages;
import com.emis.report.emisReport;
import com.emis.report.emisRightsCheck;
import com.emis.report.emisRptField;
import com.emis.report.emisRptTextProvider;
import com.emis.report.emisRptXML;
import com.emis.report.emisTd;
import com.emis.report.emisTr;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;
import com.emis.util.emisUTF8StringUtil;
import com.emis.util.emisUtil;

/**
 * 
 * 
 * @version 2010/02/04 [14335] wing WREP报表修改 WREP_OUTPUT_CHARSET输出只有两种格式
 *          MS950,GBK
 */
public class emisRptWrepProvider extends emisRptTextProvider {

	public static final String S_NUMBER = "N"; //$NON-NLS-1$

	public static final String S_DATE = "D"; //$NON-NLS-1$

	public static final String S_CHAR = "C"; //$NON-NLS-1$

	public static final String S_PERSENT = "P"; //$NON-NLS-1$

	public static String SMONEY_FLAG = Messages.getString("emisRptWrepProvider.4"); //$NON-NLS-1$

	public boolean needOncePrintPH = true; // 列印pageheader，檢查是否需要列印

	private Element eRoot2_;

	private PrintWriter out2_;

	private emisBusiness oBusiness2_;

	private int iDetailLines = 0;

	private int _iCurSize = 0;

	public static HashMap widthMap = null;

	public emisProp _oProp = null;

	public int iNewBlock = 0;

	protected int iABlockSize = 0;

	protected int iSumPTd = 0;

	protected boolean bJumpCur = false;

	protected String sReportMode_ = ""; //$NON-NLS-1$

	protected String sModeAttr_ = "pmode"; //$NON-NLS-1$

	// Track+ [14335]修改 输出MS950或GBK
	String WREP_OUTPUT_CHARSET = "MS950";

	public emisRptWrepProvider(emisBusiness oBusiness, Element eRoot, PrintWriter out) throws Exception {
		super(oBusiness, eRoot, out);
		this.eRoot2_ = eRoot;
		out2_ = out;
		oBusiness2_ = oBusiness;

		widthMap = new HashMap();
		_oProp = emisProp.getInstance(oBusiness2_.getContext());
		// Track+ [14335]修改
		WREP_OUTPUT_CHARSET = _oProp.get("WREP_OUTPUT_CHARSET", "MS950");
		SMONEY_FLAG = (String) _oProp.get("MONEY_FLAG"); //$NON-NLS-1$
		if (SMONEY_FLAG == null)
			SMONEY_FLAG = "$"; //$NON-NLS-1$

		sReportMode_ = (String) oBusiness2_.getRequest().getParameter(
				Messages.getString("emisRptWrepProvider.9")); //$NON-NLS-1$
		if (sReportMode_ == null)
			sReportMode_ = ""; //$NON-NLS-1$
		sReportMode_ += getDbPmode();
		printWrepHeader();

	}

	/**
	 * 列印wrep圖形報表頭部分
	 * 
	 */
	public void printWrepHeader() throws Exception {

		emisRptXML _oRptXML = new emisRptXML(eRoot2_);
		Node ndPrint = _oRptXML.getElement("print"); //$NON-NLS-1$
		emisRptXML _oPrintXML = new emisRptXML(ndPrint);
		Node ndDetail = _oPrintXML.getElement("detail"); //$NON-NLS-1$

		if (ndDetail == null)
			throw new RuntimeException(Messages.getString("emisRptWrepProvider.13")); //$NON-NLS-1$

		String sNewBlock = emisRptXML.getAttribute(ndDetail, "NEW_BLOCK"); //$NON-NLS-1$
		if (null != sNewBlock && !"".equals(sNewBlock)) { //$NON-NLS-1$
			iNewBlock = Integer.parseInt(sNewBlock);
		}

		ArrayList typeList = new ArrayList();
		ArrayList showList = new ArrayList();
		HashMap varMap = new HashMap();
		// 1.[Field]
		genFieldStruct(varMap, ndDetail, showList, typeList);
		// 2.[TYPE]
		out2_.println("[TYPE]"); //$NON-NLS-1$
		for (int x = 0; x < typeList.size(); x++) {
			out2_.println((x + 1) + "=" + (String) typeList.get(x)); //$NON-NLS-1$
		}
		typeList.clear();
		out2_.println("\n"); //$NON-NLS-1$

		// 3.[SHOW]
		String _sRField = ""; //$NON-NLS-1$
		String _sRShow = null;// default setting show
		out2_.println("[SHOW]"); //$NON-NLS-1$
		iABlockSize = showList.size();
		for (int i = 0; i < showList.size(); i++) {
			_sRShow = "1";// default setting show //$NON-NLS-1$
			_sRField = (String) showList.get(i);
			// ='N'時則不需要顯示
			if (!emisRightsCheck.getShowSet(this.oBusiness2_, _sRField)) {
				_sRShow = "0"; //$NON-NLS-1$
			}
			out2_.println(String.valueOf(i + 1) + "=" + _sRShow); //$NON-NLS-1$
		}

		// 4.[SHOW]
		Node ndPGHeader = _oPrintXML.getElement("pageheader"); //$NON-NLS-1$
		printMark(ndPGHeader);
		Node ndRPHeader = _oPrintXML.getElement("reportheader"); //$NON-NLS-1$
		printMark(ndRPHeader);
		out2_.println("\n"); //$NON-NLS-1$

		// 5[TITLE]
		out2_.println("[TITLE]"); //$NON-NLS-1$
		try {
			out2_.println("1=" + (String) _oProp.get("EPOS_COMPANY")); //$NON-NLS-1$ //$NON-NLS-2$
			out2_.println("2=" + this.getParameter("PROP_TITLE")); //$NON-NLS-1$ //$NON-NLS-2$
			out2_.println("\n"); //$NON-NLS-1$
		} catch (Exception e) {
		}
		out2_.println("\n"); //$NON-NLS-1$
		// 6[DATA]
		out2_.println("[DATA]"); //$NON-NLS-1$
	}

	public void genFieldStruct(HashMap varMap, Node ndDetail, ArrayList showList, ArrayList typeList) {
		String sRotateShowRule = ""; //$NON-NLS-1$
		int iRotateShowRule = 0;
		// <var>
		getVarTagField(varMap, ndDetail);
		NodeList nTrList = emisRptXML.searchNodes(ndDetail, "tr"); //$NON-NLS-1$
		int iFieldCnt = 0;
		if (nTrList == null)
			throw new RuntimeException(Messages.getString("emisRptWrepProvider.37")); //$NON-NLS-1$

		out2_.println("[FIELD]"); //$NON-NLS-1$
		for (int j = 0; j < nTrList.getLength(); j++) {
			Node ndTr = nTrList.item(j);
			if (ndTr != null) {

				sRotateShowRule = emisRptXML.getAttribute(ndTr, "rotateShowRule"); //$NON-NLS-1$
				if (null != sRotateShowRule && !"".equals(sRotateShowRule)) { //$NON-NLS-1$
					iRotateShowRule++;

				}
				if (iRotateShowRule >= 2) {
					throw new RuntimeException(Messages.getString("emisRptWrepProvider.41")); //$NON-NLS-1$
				}

				NodeList nTrVarlist = emisRptXML.searchNodes(ndTr, "var");// ///
				// //$NON-NLS-1$
				if (nTrVarlist != null) {
					for (int n = 0; n < nTrVarlist.getLength(); n++) {
						Node ndTrVar = nTrVarlist.item(n);
						varMap.put(emisRptXML.getAttribute(ndTrVar, "name"), //$NON-NLS-1$
								ndTrVar);
					}
				}

				NodeList nTdlist = emisRptXML.searchNodes(ndTr, "td"); //$NON-NLS-1$

				if (nTdlist != null) {
					for (int i = 0; i < nTdlist.getLength(); i++) {

						Node ndTd = nTdlist.item(i);

						if (ndTd != null) {
							boolean bIsReportMode = isReportMode(ndTd);

							if (bIsReportMode) {

								String sTdType = emisRptXML.getAttribute(ndTd, "type"); //$NON-NLS-1$
								if (null != sTdType && !"".equals(sTdType)) { //$NON-NLS-1$
									String sTdName = emisRptXML.getAttribute(ndTd, "name"); //$NON-NLS-1$
									if (null != sTdName && !"".equals(sTdName)) { //$NON-NLS-1$
										iFieldCnt++;
										// wing modify here 20060205, process
										// "get"
										if ("get".equalsIgnoreCase(sTdType)) { //$NON-NLS-1$
											String sGetValName = emisRptXML.getValue(ndTd);
											if (null != sGetValName) {
												showList.add(sGetValName);
											}
										} else {
											showList.add(sTdName);
										}
										out2_.println(iFieldCnt + "=" //$NON-NLS-1$
												+ sTdName);

										String sType = emisRptXML.getAttribute(ndTd, "datatype"); //$NON-NLS-1$
										String str = ""; //$NON-NLS-1$

										int iMovVal = 0;
										String sFlag = ""; //$NON-NLS-1$
										String iWidth = emisRptXML.getAttribute(ndTd, "width"); //$NON-NLS-1$
										String sGetWidth = ""; //$NON-NLS-1$

										if (iWidth.indexOf("$") > -1) { //$NON-NLS-1$
											iWidth = emisUtil.stringReplace(iWidth, "$", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											if (iWidth.indexOf("+") > -1) { //$NON-NLS-1$
												sFlag = "+"; //$NON-NLS-1$
												iMovVal = Integer.parseInt(iWidth.substring(iWidth
														.indexOf('+') + 1, iWidth.length()));
												iWidth = iWidth.substring(0, iWidth.indexOf('+'));

											} else if (iWidth.indexOf("-") > -1) { //$NON-NLS-1$
												sFlag = "-"; //$NON-NLS-1$
												iMovVal = Integer.parseInt(iWidth.substring(iWidth
														.indexOf('-') + 1, iWidth.length()));
												iWidth = iWidth.substring(0, iWidth.indexOf('-'));
											}

											Object ob = widthMap.get(iWidth);
											if (ob != null) {
												sGetWidth = (String) widthMap.get(iWidth);

											} else {
												sGetWidth = new Integer(getFormatWidth(iWidth)).toString();
												widthMap.put(iWidth, sGetWidth);

											}
											iWidth = sGetWidth;

										}
										if (!"".equals(sFlag)) { //$NON-NLS-1$
											int iLastLen = 0;
											if ("+".equals(sFlag)) { //$NON-NLS-1$

												iLastLen = Integer.parseInt(iWidth) + iMovVal;
											} else if ("-".equals(sFlag)) { //$NON-NLS-1$
												iLastLen = Integer.parseInt(iWidth) - iMovVal;
											}
											iWidth = new Integer(iLastLen).toString();
										}

										iDetailLines = iDetailLines + Integer.parseInt(iWidth);

										str = getTypeStr(ndTd, iWidth);

										typeList.add(str);

									}
								} else {

									String ndVarValue = emisRptXML.getValue(ndTd);

									Node ntmp = (Node) varMap.get(ndVarValue);
									if (ntmp != null) {
										iFieldCnt++;
										out2_.println(iFieldCnt + "=" //$NON-NLS-1$
												+ ndVarValue);
										showList.add(ndVarValue);
										String sType = emisRptXML.getAttribute(ntmp, "datatype"); //$NON-NLS-1$
										String str = ""; //$NON-NLS-1$
										String iWidth = emisRptXML.getAttribute(ntmp, "width"); //$NON-NLS-1$
										iDetailLines = iDetailLines + Integer.parseInt(iWidth);

										str = getTypeStr(ntmp, iWidth);

										typeList.add(str);

									}

								}
							}

						}// ////////
					}
				}
			}
		}
		out2_.println("\n"); //$NON-NLS-1$
	}

	/**
	 * 取XML <var> TAG定義的emisRptField
	 * 
	 * @param varMap
	 */
	public void getVarTagField(HashMap varMap, Node ndDetail) {
		NodeList nVarList = emisRptXML.searchNodes(ndDetail, "var"); //$NON-NLS-1$
		if (nVarList != null) {
			for (int m = 0; m < nVarList.getLength(); m++) {
				Node ndVar = nVarList.item(m);
				if (ndVar != null) {
					varMap.put(emisRptXML.getAttribute(ndVar, "name"), ndVar); //$NON-NLS-1$
				}
			}
		}
	}

	public String getTypeStr(Node ndTd, String iWidth) {
		String str = ""; //$NON-NLS-1$
		String sType = emisRptXML.getAttribute(ndTd, "datatype"); //$NON-NLS-1$
		if ("number".equalsIgnoreCase(sType)) { //$NON-NLS-1$
			String sNumFormat = emisRptXML.getAttribute(ndTd, "format"); //$NON-NLS-1$
			if (null != sNumFormat && !"".equals(sNumFormat)) { //$NON-NLS-1$
				int iTmpPnt = sNumFormat.indexOf("%"); //$NON-NLS-1$
				if (iTmpPnt > 0) {
					int tmpiwidth = Integer.parseInt(iWidth) + 2;
					iWidth = new Integer(tmpiwidth).toString();
					str = S_PERSENT;
				} else {
					str = S_NUMBER;
				}

				int iPoint = sNumFormat.indexOf("."); //$NON-NLS-1$
				if (iPoint > 0) {
					str = str + iWidth + "." //$NON-NLS-1$
							+ (sNumFormat.length() - (iPoint + 1));
				} else {
					str = str + iWidth;
				}
			} else {
				str = S_NUMBER + iWidth;
			}
		} else if ("date".equalsIgnoreCase(sType)) { //$NON-NLS-1$
			str = S_DATE + iWidth;
		} else {
			str = S_CHAR + iWidth;
		}
		return str;
	}

	/**
	 * 目的: 判斷該Node是否符合列印條件
	 * 
	 * @param o
	 *            要判斷之 Node 物件
	 * @return 是否符合列印mode的 boolean 值 copy from emisReportGEM 2005/12/30 wayke
	 */
	protected boolean isReportMode(Node o) {
		boolean _bRet = false;
		String _subMode;
		String _sMode = emisRptXML.getAttribute(o, sModeAttr_);
		if (_sMode == null) {
			_bRet = true;
		} else {
			_bRet = false;
			int _iPropModeLen = sReportMode_.length();
			StringTokenizer _stTemp = new StringTokenizer(_sMode, ","); //$NON-NLS-1$

			while (_stTemp.hasMoreTokens()) {
				_subMode = _stTemp.nextToken();
				_subMode.trim();
				_bRet = true;
				int _iXMLmodeLen = _subMode.length();
				for (int i = 0; i < _iPropModeLen; i++) {
					if (i >= _iXMLmodeLen)
						break;
					char _cXMLmode = _subMode.charAt(i);
					char _cPropMode = sReportMode_.charAt(i);
					if (_cXMLmode != '?' && _cXMLmode != _cPropMode) {
						_bRet = false;
						break;
					}
				} // for
				if (_bRet) {
					break;
				}
			} // while
		} // if

		return _bRet;
	}

	public int getFormatWidth(String sType) {
		int _iWidth = 0;
		emisDb _oDb = null;
		try {
			_oDb = emisDb.getInstance(oBusiness2_.getContext());
			_oDb.prepareStmt("select * from FieldFormat where FD_TYPE=?"); //$NON-NLS-1$
			_oDb.setString(1, sType);
			_oDb.prepareQuery();
			if (_oDb.next()) {
				_iWidth = _oDb.getInt("FD_MAXLEN"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			e.printStackTrace(); // To change body of catch statement use

		} finally {
			if (_oDb != null)
				_oDb.close();
		}

		return _iWidth;
	}

	public void printMark(Node ndHeader) {
		if (ndHeader == null) {
			return;
		}
		NodeList nTrList_h = emisRptXML.searchNodes(ndHeader, "tr"); //$NON-NLS-1$
		boolean bPrintMark = true;
		int iTitleCnt = 0;
		if (nTrList_h != null) {
			for (int y = 0; y < nTrList_h.getLength(); y++) {

				Node ndTr_h = nTrList_h.item(y);
				String sWrepTitle = emisRptXML.getAttribute(ndTr_h, "wrep"); //$NON-NLS-1$

				if (null != sWrepTitle && !"".equals(sWrepTitle)) { //$NON-NLS-1$

					NodeList nTdlist = emisRptXML.searchNodes(ndTr_h, "td"); //$NON-NLS-1$
					if (nTdlist != null) {
						for (int z = 0; z < nTdlist.getLength(); z++) {
							Node ndTd_d = nTdlist.item(z);
							String ndValue = emisRptXML.getValue(ndTd_d);
							String sType = emisRptXML.getAttribute(ndTd_d, "type"); //$NON-NLS-1$
							if (null != ndValue && !"".equals(ndValue)) { //$NON-NLS-1$
								iTitleCnt++;
								// 有n個td,且不為空
								if (bPrintMark) {
									out2_.println("\n"); //$NON-NLS-1$
									out2_.println("[MARK]"); //$NON-NLS-1$
								}
								bPrintMark = false;
								// wing modify
								if (sType != null && "get".equalsIgnoreCase(sType)) { //$NON-NLS-1$
									out2_.println(complToMS950OrGBK(String.valueOf(iTitleCnt) + "=" //$NON-NLS-1$
											+ this.getParameter(emisRptXML.getValue(ndTd_d))));
								} else {
									out2_.println(complToMS950OrGBK(String.valueOf(iTitleCnt) + "=" //$NON-NLS-1$
											+ emisRptXML.getValue(ndTd_d)));
								}
							}
						}
					}
				}
				// sWrepTitle
				// wing modify,可從JSP,前端取得WREP_TITLE
				sWrepTitle = this.getParameter("WREP_TITLE"); //$NON-NLS-1$
				if (sWrepTitle != null && (!"".equals(sWrepTitle))) { //$NON-NLS-1$
					// src3實做
					String titleArray[] = sWrepTitle.split(","); //$NON-NLS-1$
					if (titleArray.length > 0) {
						out2_.println("\n"); //$NON-NLS-1$
						out2_.println("[MARK]"); //$NON-NLS-1$
						for (int i = 0; i < titleArray.length; i++) {
							out2_.println(complToMS950OrGBK(String.valueOf(i + 1) + "=" //$NON-NLS-1$
									+ titleArray[i]));
						}

					}
				}
			}
		}
	}

	/**
	 * 不用跳頁，
	 */
	public void eject() {
		// 不處理任何ACT
	}

	/**
	 * 不用跳頁，某些部分只列印一次
	 */
	public void printTr(emisTr tr) {
		if (!checkPrePrintCond(tr))
			return;

		for (int i = 0; i < tr.size(); i++) {
			emisTd td = tr.get(i);
			if (td != null && td.getDataType() != 3) {
				printTd(td);

			}
		}
		jmpLForBlock();
	}

	public void printTr(emisTr tr, int nAlign, int nSize) {
		if (!checkPrePrintCond(tr))
			return;
		String sStr = ""; //$NON-NLS-1$
		for (int i = 0; i < tr.size(); i++) {
			emisTd td = tr.get(i);
			if (td != null && td.getDataType() != 3) {
				if (td.getDataType() == emisRptField.DATE) {
					sStr = sStr + td.toString();
					sStr = emisUtil.stringReplace(sStr, "/", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					sStr = emisUtil.padding(sStr, td.getNAlign_(), td.getSize());
					iSumPTd++;
				} else if (!"%".equals(td.toString())) { //$NON-NLS-1$
					sStr = sStr + td.toString();
					iSumPTd++;
				}
			}
		}
		sStr = emisUtil.stringReplace(sStr, ",", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sStr = emisUtil.stringReplace(sStr, SMONEY_FLAG, "", "a"); //$NON-NLS-1$ //$NON-NLS-2$

		if (sStr.indexOf("%") > -1) { //$NON-NLS-1$
			sStr = emisUtil.stringReplace(sStr, "%", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			nSize = nSize + 2;
		}

		sStr = padding(sStr, nAlign, nSize);

		_iCurSize = _iCurSize + nSize;
		out2_.print(complToMS950OrGBK(sStr));

		jmpLForBlock();

	}

	/**
	 * 先不做字符转换,将utf-8数据转换为MS950或GBK,经测试繁体字数据若输出GBK格式，在简体wrep环境下显示也不会乱码 Track+
	 * [14335]修改
	 * 
	 * @param str
	 * @return
	 */
	private String complToMS950OrGBK(String str) {
		if (WREP_OUTPUT_CHARSET.equals("GBK")) {
			// convert
			return emisUTF8StringUtil.complTosimpleNoMappingBig5(str);
		} else {
			return emisUTF8StringUtil.complToMS950NoMappingGBK(str);
		}
	}

	public void printTd(emisTd td) {

		String sStr = ""; //$NON-NLS-1$
		if (td == null)
			return;
		if (td.getSection() == emisReport.SECTION_NONE) {
			// reportmode width="90" height="92" left="6" 左空6格
			return;
		}

		// wing modify here
		if (td.getDataType() != emisRptField.NUMBER) {

			if (td.getDataType() == emisRptField.DATE) {
				String sPrtStr = td.toString();
				sPrtStr = emisUtil.stringReplace(sPrtStr, "/", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sPrtStr = emisUtil.padding(sPrtStr, td.getNAlign_(), td.getSize());
				out2_.print(complToMS950OrGBK(sPrtStr));
				_iCurSize = _iCurSize + td.getSize();
				iSumPTd++;
			} else if (!"%".equals(td.toString())) { //$NON-NLS-1$
				String sPrtStr = complToMS950OrGBK(padding(td.toString(), td.getNAlign_(), td.getSize()));
				out2_.print(complToMS950OrGBK(sPrtStr));
				_iCurSize = _iCurSize + td.getSize();
				iSumPTd++;
			}
		} else {
			_iCurSize = _iCurSize + td.getSize();
			sStr = emisUtil.stringReplace(td.toString(), ",", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sStr = emisUtil.stringReplace(sStr, SMONEY_FLAG, "", "a"); //$NON-NLS-1$ //$NON-NLS-2$
			if (sStr.indexOf("%") > -1) { //$NON-NLS-1$
				sStr = emisUtil.stringReplace(sStr, "%", "", "a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sStr = padding(sStr, td.getNAlign_(), td.getSize() + 2);

			} else {
				sStr = padding(sStr, td.getNAlign_(), td.getSize());
			}

			out2_.print(complToMS950OrGBK(sStr));
			iSumPTd++;
		}
		jmpLForBlock();
	}

	public void jmpLForBlock() {
		if (_iCurSize >= iDetailLines) {

			println();
			bJumpCur = true;
			_iCurSize = 0;
			iSumPTd = 0;

		}

		if (iNewBlock > 0) {
			if (iSumPTd >= iABlockSize) {
				if (bJumpCur) {
					bJumpCur = false;
					iSumPTd = 0;
				} else {
					println();
					iSumPTd = 0;

				}
			}
		}
	}

	/**
	 * 檢查列印前提是否成立 類型為TD類型為3，不列印
	 * 
	 * @param tr_
	 * @return
	 */
	public boolean checkPrePrintCond(emisTr tr_) {
		boolean _needPrintTr = true;
		if (tr_ != null) {
			if (tr_.size() > 0) {
				emisTd _td = (emisTd) tr_.get(0);// 考慮效能問題，只CHECK一個TD
				if (_td.getSection() == emisReport.SECTION_NONE) {
					// reportmode width="90" height="92" left="6" 左空6格
					if (tr_.size() > 1)
						_td = (emisTd) tr_.get(1);
				}
				int iCount = 1;
				int iType = _td.getDataType();
				while (iType == 3) {
					if (iCount < tr_.size()) {
						_td = (emisTd) tr_.get(iCount);
						iType = _td.getDataType();
						iCount++;
					} else {
						break;
					}
				}
				if (iNewBlock > 0) {
					if ((_td.getSection() == emisReport.SECTION_DETAIL && (iType != 3))
							|| (_td.getSection() == emisReport.SECTION_NONE && (iType != 3))) {
						_needPrintTr = true;
					} else {
						_needPrintTr = false;

					}
				} else {
					if (_td.getSection() == emisReport.SECTION_DETAIL && (iType != 3)) {
						_needPrintTr = true;
					} else {
						_needPrintTr = false;
					}
				}
			}
		} else {
			_needPrintTr = false;

		}
		return _needPrintTr;
	}

	/*
	 * 取原emisReportGEM方法,因provider使用
	 */
	protected String getDbPmode() {
		String _sRet = ""; //$NON-NLS-1$

		String _sPrgID = getParameter("TITLE"); // 作業代碼 //$NON-NLS-1$
		if (_sPrgID == null) {
			return _sRet;
		}
		emisUser _oUser = null;
		emisDb _oDb = null;
		try {
			_oUser = emisCertFactory.getUser(getContext(), this.oBusiness2_.getRequest());
			String _sUserID = _oUser.getID();

			_oDb = emisDb.getInstance(getContext());
			_oDb.prepareStmt("select top 1 PMODE, ORIENT from Pmodesetting\n" //$NON-NLS-1$
					+ " where ((USERID='' and USERGROUPS='') or USERID=? or\n" //$NON-NLS-1$
					+ " (USERGROUPS in (select USERGROUPS from Users where USERID=?)))\n" //$NON-NLS-1$
					+ " and KEYS=? and ENABLE = 'Y'\n" //$NON-NLS-1$
					+ "order by USERID desc, USERGROUPS desc"); //$NON-NLS-1$
			_oDb.setString(1, _sUserID);
			_oDb.setString(2, _sUserID);
			_oDb.setString(3, _sPrgID);
			_oDb.prepareQuery();
			if (_oDb.next()) {
				_sRet = _oDb.getString("PMODE"); //$NON-NLS-1$
				// Pmodesetting 增加
			}
		} catch (Exception e) {
		} finally {
			if (_oDb != null)
				_oDb.close();
		}
		return _sRet;
	}

}
