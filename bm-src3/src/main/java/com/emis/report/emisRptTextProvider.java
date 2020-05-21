package com.emis.report;

import com.emis.business.emisBusiness;
import com.emis.business.emisDataSrc;
import com.emis.db.emisProp;
import com.emis.user.emisUser;
import com.emis.util.emisUTF8StringUtil;
import com.emis.util.emisUtil;
import com.emis.util.emisXMLUtl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

/**
 * [4807] wing 據DB顯示或隱藏報表欄位
 * wing 添加convertCharset()转码输出 可据request参数设置输出文件的编码,修改用于4B盘点机下传<br>
 * @author emis
 * 
 */
public class emisRptTextProvider implements emisRptProvider {

	private PrintWriter out_;

	private Element eRoot_;

	private Hashtable oDataSrc = new Hashtable();

	private int nWidth_ = 74; // default value

	private int nHeight_ = 30; // default value

	private Properties oProp = new Properties();

	private int nPageNum_ = 1;

	private emisBusiness oBusiness_;

	private HttpServletRequest oRequest_;

	private int nCurrentRowNum_;

	private emisProviderEventListener listener_;

	private ServletContext oApplication_;

	private emisUser oUser_;

	private boolean isMS950 = false;

	private boolean isGBK = false;

	private boolean isUTF8 = true;

	public static final int REPORTOUTCHARSET_UTF8 = 0;

	public static final int REPORTOUTCHARSET_MS950 = 1;

	public static final int REPORTOUTCHARSET_GBK = 9;

	// RPT_OUTPUT_CHARSET UTF-8,MS950,GBK
	private int reportOutType = REPORTOUTCHARSET_UTF8;

	public emisRptTextProvider(emisBusiness oBusiness, Element eRoot, PrintWriter out) throws Exception {
		oBusiness_ = oBusiness;
		eRoot_ = eRoot;
		out_ = out;
		oUser_ = oBusiness_.getUser();
		oApplication_ = oBusiness_.getContext();
		oRequest_ = oBusiness_.getRequest();
		// init args
		// wing 201002预设为MS950,可据request参数设置输出文件的编码
		String sRPT_OUTPUT_CHARSET = this.oBusiness_.getRequest().getParameter("RPT_OUTPUT_CHARSET");
		if (sRPT_OUTPUT_CHARSET == null) {
			sRPT_OUTPUT_CHARSET = "UTF-8";
		}
		if ("MS905".equals(sRPT_OUTPUT_CHARSET)) {
			reportOutType = REPORTOUTCHARSET_MS950;
		} else if ("GBK".equals(sRPT_OUTPUT_CHARSET)) {
			reportOutType = REPORTOUTCHARSET_GBK;

		}
		String _sHeight = emisXMLUtl.getAttribute(eRoot_, "height");

		// $ 91.07.27 修改可至 emisprop 讀取
		emisProp _oProp = emisProp.getInstance(oApplication_);
		String _sValue = "";
		if (_sHeight != null) {
			try {
				nHeight_ = Integer.parseInt(_sHeight);
			} catch (Exception ignore) {
			}
		} else {
			// $ 91.07.27 若高度未設定, 修改可至 emisprop 讀取
			try {
				_sValue = _oProp.get("EPOS_RPT_PHEIGHT");
				if (_sValue != null) {
					nHeight_ = Integer.parseInt(_sValue);
				}
			} catch (Exception e) {
			}
		}

		// $ 91.07.27 修改可至 emisprop 讀取
		String _sOrient = emisXMLUtl.getAttribute(eRoot_, "orient");
		if ("P".equalsIgnoreCase(_sOrient)) {
			// 直印設定
			try {
				_sValue = _oProp.get("EPOS_RPT_PHEIGHT");
				if (_sValue != null) {
					nHeight_ = Integer.parseInt(_sValue);
				}
			} catch (Exception e) {
			}
		} else if ("L".equalsIgnoreCase(_sOrient)) {
			// 橫印設定
			try {
				_sValue = _oProp.get("EPOS_RPT_LHEIGHT");
				if (_sValue != null) {
					nHeight_ = Integer.parseInt(_sValue);
				}
			} catch (Exception e) {
			}
		}

		oBusiness_.debug("height:" + nHeight_);
		String _sWidth = emisXMLUtl.getAttribute(eRoot_, "width");

		if (_sWidth != null) {
			try {
				nWidth_ = Integer.parseInt(_sWidth);
			} catch (Exception ignore) {
			}
		}

		oBusiness_.debug("width:" + nWidth_);

		NodeList nlist = eRoot_.getChildNodes();
		if (nlist != null) {
			int len = nlist.getLength();
			for (int i = 0; i < len; i++) {
				Node n = nlist.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE)
					continue;
				Element e = (Element) n;
				String _sName = n.getNodeName();
				if ("datasrc".equals(_sName)) {
					emisDataSrc oSrc = new emisDataSrc(oBusiness_, e);
					oDataSrc.put(oSrc.getId(), oSrc);
				} else if ("property".equals(_sName)) {
					loadProperty(e);
				}
			}
		}
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
					oBusiness_.debug("set prop:" + _sNodeName + "=" + _sNodeValue);
				}
			}
		}
	}

	public void printTd(emisTd td) {
		if (td == null)
			return;
		// [4807]wing add 權限顯示
		if (td.getCustid() == emisReport.REPORT_NODISPLAY) {
			// wing modify here
			if (!emisRightsCheck.getShowSet(this.oBusiness_, td.getName())) {
				String _sValue = emisRightsCheck.getShowSetVal(oBusiness_, td.getName());
				out_.print(convertCharset(emisUtil.padding(_sValue, td.getNAlign_(), td.getSize())));
			} else
				out_.print(convertCharset(emisUtil.padding("", td.getNAlign_(), td.getSize())));
		} else {
			// 補空輸出
			out_.print(convertCharset(td.toString()));

		}

	}

	/**
	 * 转为输出特定编码,可以据系统参数，也可传入RPT_OUTPUT_CHARSET决定
	 * 
	 * @param str
	 * @return
	 */
	private String convertCharset(String str) {

		switch (reportOutType) {
		case REPORTOUTCHARSET_UTF8:
			return str;
		case REPORTOUTCHARSET_MS950:
			return emisUTF8StringUtil.complToMS950(str);
		case REPORTOUTCHARSET_GBK:
			return emisUTF8StringUtil.complTosimple(str);
		default:
			return str;
		}

	}

	public void printTr(emisTr tr) {
		if (tr == null)
			return;
		for (int i = 0; i < tr.size(); i++) {
			emisTd td = tr.get(i);
			if (td != null) {
				printTd(td);
			}
		}
		println();
	}

	public void printTr(emisTr tr, int nAlign, int nSize) {
		if (tr == null)
			return;
		String sStr = "";
		for (int i = 0; i < tr.size(); i++) {

			emisTd td = tr.get(i);
			if (td != null) { // [4807]
				if (td.getCustid() == emisReport.REPORT_NODISPLAY) {
					if (!emisRightsCheck.getShowSet(oBusiness_, td.getName())) {
						String _sValue = emisRightsCheck.getShowSetVal(oBusiness_, td.getName());
						sStr += emisUtil.padding(_sValue, td.getNAlign_(), td.getSize());
					} else
						sStr = sStr + emisUtil.padding("", td.getNAlign_(), td.getSize());
				} else {
					// 補空輸出
					sStr = sStr + td.toString();
				}
			}

		}
		sStr = padding(sStr, nAlign, nSize);
		out_.print(convertCharset(sStr));
		println();
	}

	public void printTable(emisTr tr) {
		if (tr == null)
			return;
		printTr(tr);
		testEject();
	}

	public void printTable(emisTr tr, int nAlign, int nSize) {
		if (tr == null)
			return;
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

	// //[4807]wing改方法rights
	public void println(String sStr) {
		out_.println(convertCharset(sStr));
		nCurrentRowNum_++;
	}

	// [4807]wing private -->public
	public void println() {
		out_.println();
		nCurrentRowNum_++;
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

		/*
		 * if( nCurrentRowNum_ >= nHeight_ ) { nCurrentRowNum_ = 0; if(
		 * listener_ != null ) { listener_.onEject(); } }
		 */
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
		out_.println("\u000c");
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
		return "";
	}

	public Node getRoot() {
		return eRoot_;
	}
}
