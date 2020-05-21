/* $Id: emisAbstractXMLTestCase.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (C) EMIS Corp.
 */
package com.emis.test;

import com.emis.business.emisBusiness;
import com.emis.business.emisBusinessMgr;
import com.emis.business.emisHttpServletRequest;
import com.emis.db.emisDb;
import com.emis.user.emisErosUserImpl;
import com.emis.user.emisUser;
import com.emis.util.emisUtil;
import com.emis.report.emisString;

import javax.servlet.http.HttpServletRequest;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * 操作JUnit的抽像類別, 使叫用的類別不用建立ServletContext與EMIS Server.
 * 繼承自 emisAbstractTestCase.
 *
 * @author Jerry
 * @version 2004/07/07
 */
public class emisAbstractXMLTestCase extends emisAbstractTestCase {
  /** ServletContext, 在emisAbstractTestCase內定義 */
  //-protected ServletContext oContext_;
  /** Resource bean: 在emisAbstractPBOTestCase定義 */
  //protected emisBusinessResourceBean oResourceBean_;
  /** request 物件 */
  protected HttpServletRequest request = new emisHttpServletRequest();
  private emisBusiness oBusiness_;
  private String sTitle_;
  private PrintWriter out;
  private String sSQL_;

  /**
   * 設定XML的名稱.
   * @param sTitle
   */
  protected void setTitle(String sTitle) {
    sTitle_ = sTitle;
  }

  /**
   * 傳回XML的名稱.
   * @return
   */
  protected String getTitle() {
    return sTitle_;
  }

  /**
   * 傳回使用者物件.
   *
   * @param sS_NO
   * @param sUserID
   * @param sPasswd
   * @return
   * @throws Exception
   */
  protected emisUser getUser(String sS_NO, String sUserID, String sPasswd)
      throws Exception {
    String _sSessionID = "a" + System.currentTimeMillis();  // 取不重複的Session ID
    emisUser _oUser = new emisErosUserImpl(oContext_, sS_NO, "", sUserID,
        sPasswd, new Boolean(false), _sSessionID);
    _oUser.setDebug(true);

    return _oUser;
  }

  /**
   * 設輸入的Form參數.
   * @param sKey
   * @param sValue
   */
  protected void setParameter(String sKey, String sValue) {
    request.setAttribute(sKey, sValue);
  }

  /**
   * 設定Form的參數.
   * @param sPropFile
   * @throws IOException
   */
  protected void setParameter(String sPropFile) throws IOException {
    FileInputStream in = null;
    try {
      in = new FileInputStream(sPropFile);
      Properties _oProps = new Properties();
      _oProps.load(in);
      Enumeration en = _oProps.keys();
      while (en.hasMoreElements()) {
        String _sKey = (String) en.nextElement();
        String _sValue = _oProps.getProperty(_sKey);
        setParameter(_sKey, _sValue);
      }
    } catch (Exception e) {
      System.out.println("XMLTestCase.setParameter: " + e.getMessage());
    } finally {
      if (in != null) in.close();
    }
  }

  /**
   * 檢查資料長度是否超過欄寬, 若超過則顯示以方便除錯.
   *
   * @param sTable
   * @return
   */
  protected boolean checkFieldWidth(String sTable) {
    boolean _bResult = true;
    emisDb _oDb = null;
    try {
      _oDb = emisDb.getInstance(oContext_);
      _oDb.prepareStmt("select top 1 * from " + sTable);
      ResultSet _oRS = _oDb.prepareQuery();
      ResultSetMetaData _oMeta = _oRS.getMetaData();
      int _iCount = _oMeta.getColumnCount();

      for (int i = 1; i <= _iCount; i++) {
        String _sName = _oMeta.getColumnName(i);
        String _sValue = request.getParameter(_sName);
        if (_sValue != null && !"".equals(_sValue)) {
          int _iDataLength = _sValue.length();
          int _iFieldWidth = _oMeta.getPrecision(i);
          if (_iDataLength > _iFieldWidth) {
            System.out.println("第[" + i + "]個欄位: " + _sName + "欄寬=" +
                _iFieldWidth + " 資料長=" + _iDataLength + " 超長");
            _bResult = false;
          }
        }
      }
    } catch (Exception e) {
      System.out.println("DB error: " + e.getMessage());
    } finally {
      _oDb.close();
    }
    return _bResult;
  }

  /**
   * 執行emisBusiness.process( )以產生SQL敘述.
   *
   * @param sAction
   * @param sJSPFile
   * @param oUser
   * @throws Exception
   */
  protected void run(String sAction, String sJSPFile, emisUser oUser) throws Exception {
    out = new PrintWriter(new FileWriter(sJSPFile));
    header(out);  //  輸出供IE執行的內容
    oBusiness_ = emisBusinessMgr.get(oContext_, getTitle(), oUser);
    oBusiness_.setWriter(out);
    oBusiness_.setParameter(request);

    try {
      oBusiness_.process(sAction);
    } catch (Exception e) {
      System.out.println("XMLTestCase.run: " + e.getMessage());
    } finally {
      String _sMsg = (String) oBusiness_.getAttribute("DEBUG_MSG");
      System.out.println("msg=" + _sMsg);
      sSQL_ = composeSQL(_sMsg);
      if (out != null)
        out.close();
    }
  }

  /**
   * 取出emisBusiness物件.
   * @return
   */
  protected emisBusiness getBusiness() {
    return oBusiness_;
  }

  /**
   * 取出解析後的SQL敘述.
   * @return
   */
  protected String getSQL() {
    return sSQL_;
  }

  /**
   * 取出報表檔名.
   * @param oUser
   * @return
   */
  protected String getReportFile(emisUser oUser) {
    String _sReportFile = emisUtil.getDocumentRoot(oContext_) + "/report_out/" +
        oUser.getID() + "/" + emisUtil.getURIPrefix(request) + oUser.getUniqueID() +
        ".txt";

    return _sReportFile;
  }

  /**
   * 輸出供IE執行的內容.
   * @param out
   */
  protected void header(PrintWriter out) {
    out.println("<%@ page contentType=\"text/html;charset=UTF-8\" %>");

    out.println("<%@ page import=\"com.emis.db.*\" %>");
    out.println("<%@ taglib uri=\"/WEB-INF/showdata.tld\" prefix=\"showdata\" %>");

    out.println("<html>");
    out.println("<head>");
    out.println("<title>TEST</title>");
    out.println("<link rel='stylesheet' href='../../js/style.css'>");
    out.println("<script src='../../js/epos.js'></script>");
    out.println("<script src='../../js/emis.js'></script>");
    out.println("<script src='../../js/emisX.js'></script>");
    out.println("<script> emisActiveX(); </script>");
    out.println("</head>");
    out.println("<body leftmargin='0' topmargin='0' marginwidth='0' marginheight='0'>");
    out.println("<table width='100%' height='100%'>");
    out.println("<tr><td>");
    out.println("<form id=idForm action='part.jsp' method='post'>");

    out.println("<%");
    out.println("    // 取得 EmisProp Table 之系統變數");
    out.println("    emisProp oProp_ = emisProp.getInstance(application);");
    out.println("%>");

    out.println(" <%@ include file=\"../mmt/part.jspf\" %>");
  }

  /**
   * 依傳入字串組成SQL敘述, 並將?號替換成輸入的參數.
   * @param sMsg
   * @return
   */
  protected String composeSQL(String sMsg) {
    if (sMsg == null) return null;
    int _iIndex = sMsg.indexOf("prepared SQL=");
    if (_iIndex < 0) return null;

    String _sSQL = sMsg.substring(_iIndex + 14);
    int _iIndex2 = _sSQL.indexOf("set PName");
    if (_iIndex2 < 0) {
      _iIndex2 = _sSQL.indexOf("ExecuteQuery(Condition)");
      if (_iIndex2 < 0)
        return null;
    }

    _sSQL = _sSQL.substring(0, _iIndex2);

    HashMap _hmValues = new HashMap();
    // 以"set PName"找到參數, 取出要設定的參數值, 並存入 _hmValues內
    sMsg = sMsg.substring(_iIndex2);
    int i = 1;
    while (i <= 99) {
      String _sToken = "set PName" + i;
      _iIndex = sMsg.indexOf(_sToken);
      if (_iIndex < 0) break;
      String _sValue = sMsg.substring(_iIndex + _sToken.length() + 1);
      _iIndex2 = _sValue.indexOf(emisUtil.LINESEPARATOR);
      _sValue = _sValue.substring(0, _iIndex2);
      _hmValues.put("QRY" + i, _sValue);
      //System.out.println(i + "=" + _sValue);
      sMsg = sMsg.substring(_iIndex + _sToken.length() + _sValue.length() + 3);
      i++;
    }

    // 兩個空行變成一個空行
    //_sSQL = emisUtil.stringReplace(_sSQL,
    //    emisUtil.LINESEPARATOR+emisUtil.LINESEPARATOR, emisUtil.LINESEPARATOR, "");
    // 將SQL內的問號換成set PName?的值
    int _iSize = _hmValues.size();
    for (i = 1; i <= _iSize; i++) {
      String _sValue = (String) _hmValues.get("QRY" + i);
      _sSQL = emisUtil.stringReplace(_sSQL, "?", "'" + _sValue + "'", "");
    }

    out.println("<!-- SQL=");
    out.println(_sSQL);
    out.println("-->");
    System.out.println("sql=" + _sSQL);

    return _sSQL;
  }

  /**
   * emis 列印處理 Header
   * @param sHead: Head 之每一欄位之列印抬頭,各欄位以 ","隔開
   * @param sSize: Head 之每一欄位之列印寬度,各欄位以 ","隔開
   * @param sHead: Head 之每一欄位之列印間隔,各欄位以 ","隔開
   * @return {oHead: 組好之列印 Head 物件, oHeadLine: 組好之列印 Head 底線物件 }
   * Ex:
    String _sHeader = "序號,商品編號,商品名稱,現有庫存,零售價,庫存金額,當月銷售量,當年銷售量,商品狀態,存銷比,當月迴轉率,當年迴轉率";
    String _sSize = "4,20,20,8,10,11,11,11,8,9,10,10";
    String _sGap = "1,1,1,1,1,1,1,1,1,1,1,1";
    String[] _aHeader = emisRptHead(_sHeader, _sSize, _sGap);
    setParameter("PROP_HEAD", _aHeader[0]);
    setParameter("PROP_WIDTH", "" + emisString.lengthB(_aHeader[0]));
    setParameter("PROP_HEADLINE", _aHeader[1]);
    setParameter("PROP_SIZE", _sSize);
    setParameter("PROP_GAP", _sGap);
   */
  protected String[] emisRptHead(String sHead, String sSize, String sGap) {
    StringTokenizer _stHead = new StringTokenizer(sHead, ",");
    StringTokenizer _stSize = new StringTokenizer(sSize, ",");
    StringTokenizer _stGap = new StringTokenizer(sGap, ",");
    StringBuffer _sbHead = new StringBuffer();
    StringBuffer _sbHeadLine = new StringBuffer();

    while (_stHead.hasMoreTokens()) {
      int _iSize = emisUtil.parseInt(_stSize.nextToken());
      int _iGap = emisUtil.parseInt(_stGap.nextToken());
      _sbHead.append(emisString.cPadB(_stHead.nextToken(), _iSize))
          .append(emisString.replicate(" ", _iGap));
      _sbHeadLine.append(emisString.replicate("-", _iSize))
          .append(emisString.replicate(" ", _iGap));
    }

    return new String[]{_sbHead.toString(), _sbHeadLine.toString()};
  }
}
