/*
 * $Header: /repository/src3/src/com/emis/business/emisShowData.java,v 1.2 2006/02/23 06:29:43 andy Exp $
 *
 * Copyright (c) EMIS Corp.
 lisa.huang 2010/05/07 新增顯示全部按鈕
 */
package com.emis.business;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.db.emisSQLCache;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileFactory;
import com.emis.file.emisFileMgr;
import com.emis.report.emisRightsCheck;
import com.emis.trace.emisMessage;
import com.emis.trace.emisTracer;
import com.emis.user.emisUserButton;
import com.emis.util.*;
import com.emis.xml.emisXmlFactory;
import org.w3c.dom.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.Deflater;

/**
 * 負責 XML 中 showdata 的部份
 * Track+[15054] dana.gao 2V表身F10快速輸入功能調整,Ajax部份由Joe提供
 * Track+[15122] dana.gao emisAjaxGetData查詢,無資料時,返回'',不返回'nodata'
 * Track+[15137] dana.gao 2010/06/23 將js中的特殊字符進行轉譯
 */
public class emisShowData extends emisAction {
  private long start,end;
  private emisTracer oTrace_;
  private emisLangRes oLang_;


  protected emisShowData(emisBusiness oBusiness, Element e, Writer out) throws Exception {
    super(oBusiness, e, out);
  }


  private void writeTableDef(Element eTableDef) throws Exception {
    String _sClickEventId = emisXMLUtl.getAttribute(eTableDef, "clickid");
    if (genTableScript) {
      genTableJavaScript(sTableId_);
      if (_sClickEventId != null) {
        genTableClickEvent(_sClickEventId);
      }
    }
  }

  private void genTableClickEvent(String sClickId) throws Exception {

    oJScript_.append("<script for=" + sClickId + " event=onclick>\n");
    oJScript_.append("  emisOnidRowClick(this,idTBLRec);\n");
    oJScript_.append("</script>");
  }

  private void writeln(String sStr) throws IOException {
    out_.write(sStr);
    out_.write("\n");
  }

  // 針對 <datasrc>
  private void createDataSrc_OLD(Element eDataSrc) throws Exception {
    emisDataSrc oDataSrc = new emisDataSrc(oBusiness_, eDataSrc);
    String _sId = oDataSrc.getId();

    int _nLimitedRowCount = 0;
    boolean _isLimit = false;

    String _sMaxRows = oBusiness_.getRequest().getParameter("L_QRYNUM");
    if (_sMaxRows != null) {
      try {
        _nLimitedRowCount = Integer.parseInt(_sMaxRows);
        _isLimit = true;
        oBusiness_.debug("limited rownum:" + _nLimitedRowCount);
      } catch (Exception e) {
      }
    }


    out_.write("<XML id=\"" + _sId + "\"></XML>\n");

    int _nSize = 0;

    emisFileMgr _FMgr = emisFileMgr.getInstance(this.oContext_);
    emisFileFactory _FFactory = _FMgr.getFactory();
    emisDirectory _oDir = _FFactory.getDirectory("users");
    emisDirectory _oDirRoot = _FFactory.getDirectory("root");
    _oDir = _oDir.subDirectory(oUser_.getID());


    String _sURI = emisUtil.getURIPrefix(oBusiness_.getRequest()) + oUser_.getUniqueID();
    String _xmlDataFileName = _sURI + ".zip";
    String _sExtractName = _sURI + ".xml";

    emisDb oDb = oDataSrc.processSQL();
    try {

      OutputStream _FStream = _FFactory.getZipOutStream(_oDir, _xmlDataFileName, _sExtractName, null);
      try {
        start = System.currentTimeMillis();
        _nSize = genXMLData(oDb, _FStream, oDataSrc, _isLimit, _nLimitedRowCount);
        _FStream.flush();
        end = System.currentTimeMillis();
        this.oBusiness_.debug("Write Data(" + _nSize + ") in:" + (end - start) + " milliseconds");
      } finally {
        if (_FStream != null) {
          try {
            _FStream.close();
          } catch (Exception ignoreIO) {
          }
        }
        _FStream = null;
      }
    } finally {
      oDb.close();
    }
    boolean isEmptyXML = (_nSize == 0) ? true : false;

    genDataSrcJavaScript(isEmptyXML, _sId, sTableId_, _oDirRoot, _oDir.getJavaScriptDirectory() + _xmlDataFileName, _sExtractName);
  }

  /**
   * 杅擂祥淫莉汜善xml恅璃笢ㄛ眻諉迡善珜醱笢(秪峈偌埻懂腔源宒堂隅腔xmlData,ﾈ別婓楛極炵苀笢變潠極epos,杅擂頗岆觴鎢)﹝ update by andy 2006/10/21
   * @param eDataSrc
   * @throws Exception
   */
  private void createDataSrc(Element eDataSrc) throws Exception {
    emisDataSrc oDataSrc = new emisDataSrc(oBusiness_, eDataSrc);
    String _sId = oDataSrc.getId();

    int _nLimitedRowCount = 0;
    boolean _isLimit = false;

    String _sMaxRows = oBusiness_.getRequest().getParameter("L_QRYNUM");
    if (_sMaxRows != null) {
      try {
        _nLimitedRowCount = Integer.parseInt(_sMaxRows);
        _isLimit = true;
        oBusiness_.debug("limited rownum:" + _nLimitedRowCount);
      } catch (Exception e) {
      }
    }


    if("YES".equalsIgnoreCase(request_.getParameter("AJAX"))){
      emisDb oDb = oDataSrc.processSQL();
      try {
        this.genJSData(oDb, out_, oDataSrc, _isLimit, _nLimitedRowCount);
      } finally {
        oDb.close();
      }
    } else {

    out_.write("<XML id=\"" + _sId + "\">");

    int _nSize = 0;

    emisDb oDb = oDataSrc.processSQL();
    try {
      start = System.currentTimeMillis();
      _nSize = genXMLData(oDb, out_, oDataSrc, _isLimit, _nLimitedRowCount);
      out_.flush();
      end = System.currentTimeMillis();
      this.oBusiness_.debug("Write Data(" + _nSize + ") in:" + (end - start) + " milliseconds");
    } finally {
      oDb.close();
    }
    out_.write("</XML>");
    boolean isEmptyXML = (_nSize == 0) ? true : false;

    //genDataSrcJavaScript(isEmptyXML, _sId, sTableId_, _oDirRoot, _oDir.getJavaScriptDirectory() + _xmlDataFileName, _sExtractName);
    genDataSrcJavaScript(isEmptyXML, _sId);
    }
  }

    private int genJSData(emisDb oDb, Writer oOutStream, emisDataSrc oSrc, boolean isLimit, int limitedRows) throws Exception {
    ArrayList oDateData = oSrc.getDateDataColumn();

    int _nCount = oDb.getColumnCount();   // 存欄位數
    if (_nCount <= 0) return 0;

    int[] _nType = new int[_nCount];  // 存各欄位之型態

    for (int i = 1; i <= _nCount; i++) {
      _nType[i - 1] = oDb.getColumnType(i);
    }

    String[] _sColName = new String[_nCount];  // 存欄位名稱
    boolean hasFieldRights = false;
    for (int i = 1; i <= _nCount; i++) {
      String _sCol = oDb.getColumnName(i);
      if (_sCol != null)
        _sCol = _sCol.toUpperCase();
      _sColName[i - 1] = _sCol;
    }

    String _oObject[] = new String[_nCount];  // 存資料
    boolean[] _isDateData = null; // 存 是否普通資料,或 DateDATA


    String _sDateSeparator = oSrc.getDateSeparator();

    if (!"".equals(_sDateSeparator)) {
      _isDateData = new boolean[_nCount];
      if (oDateData != null) {
        for (int i = 1; i <= _nCount; i++) {
          if (oDateData.contains(_sColName[i - 1])) {
            _isDateData[i - 1] = true;
          } else {
            _isDateData[i - 1] = false;
          }
        }
      }
    }


    /*-----------------------JS header-----------------------*/
    int _nSize = 0;
    try {
      StringBuffer ret = new StringBuffer();
      ret.append("[");
      if (oDb.next()) {
        emisStrBuf buf = new emisStrBuf(1024);
        emisStrBuf escapeBuf = new emisStrBuf(128);

        do {
          for (int i = 0; i < _nCount; i++) {
            _oObject[i] = oDb.getString(i + 1);
          }
          writeJSData(buf, escapeBuf, _sColName, _oObject, _isDateData, _sDateSeparator);
          if (_nSize > 0)
            ret.append(",");
          ret.append(buf.toString());
          buf.setZeroLength();
          _nSize++;
          // 用來做筆數的限制
          if (isLimit) {
            if (_nSize >= limitedRows)
              break;
          }
        } while (oDb.next());
      } else {
        ret.append("['']");
      }
      ret.append("]");
      oBusiness_.setAttribute("AJAX_RET", ret.toString());
    } finally {
    }
    return _nSize;
  }


   private void writeJSData(emisStrBuf buf, emisStrBuf escapeBuf, String[] sColName, String[] oValue,
                           boolean[] isDateData, String sDateSeparator) throws Exception {
    int len = sColName.length;
    String _sFieldStr = this.request_.getParameter("sFieldStr_");

    if(_sFieldStr!=null && !"".equals(_sFieldStr.trim()))
        _sFieldStr = ","+_sFieldStr.trim()+",";
    else
      _sFieldStr = null;

    buf.append("[");
    boolean _isDateData;
    for (int idx = 0; idx < len; idx++) {
      String _sName = sColName[idx];
      if(_sFieldStr!=null && _sFieldStr.indexOf(","+_sName+",")== -1) continue;
      _isDateData = (isDateData == null) ? false : isDateData[idx];
      String _oValue = oValue[idx];  // 上層傳來之 _oObject
      if (_oValue == null) {
        _oValue = "";
      } else {
        if (_isDateData) // 處理日期字串問題
        {
          // 從字串後面把字給插進去
          int _nLen = _oValue.length();
          if (_nLen >= 4) {
            escapeBuf.setZeroLength();

            escapeBuf.assign(_oValue);
            escapeBuf.insert(sDateSeparator, _nLen - 2);
            _nLen = escapeBuf.length() - 5;
            if (_nLen > 2)
              escapeBuf.insert(sDateSeparator, escapeBuf.length() - 5);

            _oValue = escapeBuf.toString();
          }
        }
        if (!emisRightsCheck.getShowSet(this.oBusiness_, _sName)) {
          _oValue = emisRightsCheck.getShowSetVal(oBusiness_, _sName);
        }
      }
      escapeBuf.setZeroLength();
      escapeBuf.escapeJSEntity( _oValue );
      buf.append((idx > 0 ? ",\"" : "\"") + escapeBuf+  "\"");
    }
    buf.append("]");
  }


  private StringBuffer oJScript_;

  private void genDataSrcJavaScript(boolean isEmptyXML, String sDataSrc, String sTargetTable, emisDirectory root, String sFile, String extractName) throws Exception {

    oJScript_.append("<script language=\"javascript\">\n");

    /**
     *  因為直接傳 Zip 的 URL 的話, WWW Server 會有 Cache
     *  造成不正確的結果, 所以只好用一個 Servlet 來在中間
     *  當傳輸的管道
     */

    java.util.Random r = new Random();
    int rand = r.nextInt(20) + 1;

    String _sServerAddress = getServerAddress(oContext_, request_);

    String _sFullName = (request_.isSecure() ? "https": "http") + "://" + _sServerAddress + ":" + request_.getServerPort() +
        root.subDirectory("servlet").getRelative() + "com.emis.servlet.loader.emisDataLoader" + rand + "?FILE=" + sFile;

//        oJScript_.append(  "  emisLoadXML("+sDataSrc+",'"+_sFullName+"','"+extractName+"');\n");
    oJScript_.append("  emisLoadXML(" + sDataSrc + ",'" + _sFullName + "','" + extractName + "','" + oUser_.getUniqueID() + "');\n");
    oJScript_.append("</script>\n");

    if (!genTableScript) return;
    oJScript_.append("<script for=\"").append(sDataSrc).append("\" event=ondatasetcomplete>\n");

    if (isEmptyXML) {
      oJScript_.append("    this.recordset.AbsolutePosition = 1;\n");
      // Cliff 90.03.25 黨蜊, 祥珂刉壺, 衾 onload 奀刉壺,
      // recordset 峈諾訧蹋奀 addnew 綴 recordcount 祥頗遜岆諾腔
      //oJScript_.append("    this.recordset.Delete();\n");
    }
    oJScript_.append("  emisOnxmlDataComplete(this,idTBLRec);\n");
    oJScript_.append("</script>\n");
  }

  // add by andy 2006/10/21
  private void genDataSrcJavaScript(boolean isEmptyXML, String sDataSrc) throws Exception {
    if (!genTableScript) return;
    oJScript_.append("<script for=\"").append(sDataSrc).append("\" event=ondatasetcomplete>\n");

    if (isEmptyXML) {
      oJScript_.append("    this.recordset.AbsolutePosition = 1;\n");
      // Cliff 90.03.25 黨蜊, 祥珂刉壺, 衾 onload 奀刉壺,
      // recordset 峈諾訧蹋奀 addnew 綴 recordcount 祥頗遜岆諾腔
      //oJScript_.append("    this.recordset.Delete();\n");
    }
    oJScript_.append("  emisOnxmlDataComplete(this,idTBLRec);\n");
    oJScript_.append("</script>\n");
  }

  private void genTableJavaScript(String sId) {
    oJScript_.append("<script for=\"" + sId + "\" event=onreadystatechange>\n");
    oJScript_.append("  emisOnTableReadyStateChange(idTBLRec);\n");
    oJScript_.append("  </script>\n");
  }

  private String sTableId_;
  private boolean genTableScript;

  public void doit() throws Exception {
    // init variables...
    oTrace_ = emisTracer.get(super.oContext_);
    oJScript_ = new StringBuffer(1024);

    /************* 有時我們只是要 xml Data, 不須要 TableScript *********/
    genTableScript = true;
    String _sTableScriptOff = emisXMLUtl.getAttribute(eRoot_, "tablescript");
    if ("off".equalsIgnoreCase(_sTableScriptOff)) {
      genTableScript = false;
    }

    String _sMainTableId = emisXMLUtl.getAttribute(eRoot_, "tableid");

    if ((_sMainTableId == null) || _sMainTableId.equals("")) {
      _sMainTableId = "idTBL";
    }

    sTableId_ = _sMainTableId;

    _doit(eRoot_);

    if (oJScript_.length() >= 0) {
      writeln(oJScript_.toString());
    }
    oJScript_ = null;

  }

  public void declareTableRecord(Writer out) throws Exception {

  if(!this.genTableScript) return;

    out.write("<script>\n");
    out.write("  var idTBLRec = {\n");
    out.write("    TableId  : \"" + sTableId_ + "\",\n");
    out.write("    TableObject : null,\n");
//        out.write("    currRowNum : null,\n");
//        out.write("    currRecordNum : null,\n");
    out.write("    MoveRecordNum : null,\n");
    out.write("    dataSrc : null\n");
    out.write("  };\n");
    out.write("  var idTBLcurrRowNum=null;\n");
    out.write("  var idTBLcurrRecordNum=null;\n");

    out.write("  emisSetDataSrc(idTBLRec);\n");
    out.write("</script>\n");
  }


  private void writeNavigator(Element e) throws Exception {
    StringBuffer _oBuf = new StringBuffer(256);
    String _sImageRoot = emisFileMgr.getInstance(oContext_).getDirectory("images").getRelative();
    if (_sImageRoot == null) _sImageRoot = "/images/";

    String _sRecord = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_RECORD);

    String _sFirstRec = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_FIRSTREC);
    String _sPrevPage = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_PREVPAGE);
    String _sPrevRec = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_PREVREC);
    String _sNextRec = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_NEXTREC);
    String _sNextPage = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_NEXTPAGE);
    String _sLastRec = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_LASTREC);

    String _sMoveFR = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVEFIRSTREC);
    String _sMovePP = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVEPREVPAGE);
    String _sMovePR = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVEPREVREC);
    String _sMoveNR = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVENEXTREC);
    String _sMoveNP = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVENEXTPAGE);
    String _sMoveLR = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVELASTREC);

    _oBuf.append("<IMG id=\"" + sTableId_ + "NaviFirstRec\" value=\"" + _sFirstRec + "\" alt=\"" + _sFirstRec + "\" src=\"").append(_sImageRoot).append("firstrec.gif\" border=\"0\" onclick=\"NaviFirstRecFun(idTBLRec); \" TITLE=\"" + _sMoveFR + _sRecord + "\" style=\"cursor:hand\">\n");
    _oBuf.append("<IMG id=\"" + sTableId_ + "NaviPrevPage\" value=\"" + _sPrevPage + "\" alt=\"" + _sPrevPage + "\" src=\"").append(_sImageRoot).append("prevpage.gif\" border=\"0\" onclick=\"NaviPrevPageFun(idTBLRec); \" TITLE=\"" + _sMovePP + _sRecord + "\" style=\"cursor:hand\">\n");
    _oBuf.append("<IMG id=\"" + sTableId_ + "NaviPrevRec\"  value=\"" + _sPrevRec + "\" alt=\"" + _sPrevRec + "\" src=\"").append(_sImageRoot).append("prevrec.gif\"  border=\"0\" onclick=\"NaviPrevRecFun(idTBLRec);  \" TITLE=\"" + _sMovePR + _sRecord + "\" style=\"cursor:hand\">\n");
    _oBuf.append("<IMG id=\"" + sTableId_ + "NaviNextRec\"  value=\"" + _sNextRec + "\" alt=\"" + _sNextRec + "\" src=\"").append(_sImageRoot).append("nextrec.gif\"  border=\"0\" onclick=\"NaviNextRecFun(idTBLRec);  \" TITLE=\"" + _sMoveNR + _sRecord + "\" style=\"cursor:hand\">\n");
    _oBuf.append("<IMG id=\"" + sTableId_ + "NaviNextPage\" value=\"" + _sNextPage + "\" alt=\"" + _sNextPage + "\" src=\"").append(_sImageRoot).append("nextpage.gif\" border=\"0\" onclick=\"NaviNextPageFun(idTBLRec); \" TITLE=\"" + _sMoveNP + _sRecord + "\" style=\"cursor:hand\">\n");
    _oBuf.append("<IMG id=\"" + sTableId_ + "NaviLastRec\"  value=\"" + _sLastRec + "\" alt=\"" + _sLastRec + "\" src=\"").append(_sImageRoot).append("lastrec.gif\"  border=\"0\" onclick=\"NaviLastRecFun(idTBLRec);  \" TITLE=\"" + _sMoveLR + _sRecord + "\" style=\"cursor:hand\">\n");
    writeln(_oBuf.toString());

  }

  private void writeDataBrowser(Element e) throws Exception {
    String _sId = emisXMLUtl.getAttribute(e, "id");
    String _sDataSrc = emisXMLUtl.getAttribute(e, "datasrc");

    if (_sId == null) _sDataSrc = "idbrowser";
    if (_sDataSrc == null) _sDataSrc = "xmlData";

    StringBuffer _oBuf = new StringBuffer(256);
    String _sImageRoot = emisFileMgr.getInstance(oContext_).getDirectory("images").getRelative();
    if (_sImageRoot == null) _sImageRoot = "/images/";


    String _sFirstRec = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_FIRSTREC);
    String _sPrevRec = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_PREVREC);
    String _sNextRec = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_NEXTREC);
    String _sLastRec = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_LASTREC);
//    String _sListCul = "全部";

    String _sMoveFR = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVEFIRSTREC);
    String _sMovePR = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVEPREVREC);
    String _sMoveNR = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVENEXTREC);
    String _sMoveLR = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_MOVELASTREC);


    _oBuf.append("<IMG id=\"" + sTableId_ + "BrowFirstRec\" value=\"" + _sFirstRec + "\" alt=\"" + _sFirstRec + "\" src=\"").append(_sImageRoot).append("firstrec.gif\" border=\"0\" onclick=\"emisIdbrowserMove('" + _sId + "'," + _sDataSrc + ".recordset,1); \" TITLE=\"" + _sMoveFR + "\" style=\"cursor:hand\">\n");
    _oBuf.append("<IMG id=\"" + sTableId_ + "BrowPrevRec\"  value=\"" + _sPrevRec + "\" alt=\"" + _sPrevRec + "\" src=\"").append(_sImageRoot).append("prevrec.gif\"  border=\"0\" onclick=\"emisIdbrowserMove('" + _sId + "'," + _sDataSrc + ".recordset,2); \" TITLE=\"" + _sMovePR + "\" style=\"cursor:hand\">\n");
//    _oBuf.append("<IMG id=\"" + sTableId_ + "BrowListCul\"  value=\"" + _sListCul + "\" alt=\"" + _sListCul + "\" src=\"").append(_sImageRoot).append("ListCul.gif\"  border=\"0\" onclick=\"emisIdbrowserMove('" + _sId + "'," + _sDataSrc + ".recordset,5); \" TITLE=\"" + "顯示全部" + "\" style=\"cursor:hand\">\n");
    _oBuf.append("<IMG id=\"" + sTableId_ + "BrowNextRec\"  value=\"" + _sNextRec + "\" alt=\"" + _sNextRec + "\" src=\"").append(_sImageRoot).append("nextrec.gif\"  border=\"0\" onclick=\"emisIdbrowserMove('" + _sId + "'," + _sDataSrc + ".recordset,3); \" TITLE=\"" + _sMoveNR + "\" style=\"cursor:hand\">\n");
    _oBuf.append("<IMG id=\"" + sTableId_ + "BrowLastRec\"  value=\"" + _sLastRec + "\" alt=\"" + _sLastRec + "\" src=\"").append(_sImageRoot).append("lastrec.gif\"  border=\"0\" onclick=\"emisIdbrowserMove('" + _sId + "'," + _sDataSrc + ".recordset,4); \" TITLE=\"" + _sMoveLR + "\" style=\"cursor:hand\">\n");
    writeln(_oBuf.toString());
  }


  private void writeNonLeaf(Element e) throws Exception {
    String _sNodeName = e.getNodeName();
    NamedNodeMap _oMap = e.getAttributes();
    StringBuffer _oBuf = new StringBuffer();
    _oBuf.append("<").append(_sNodeName);
    if (_oMap != null) {
      int _nLen = _oMap.getLength();

      for (int i = 0; i < _nLen; i++) {
        Node n = _oMap.item(i);
        String _sAttrName = n.getNodeName();
        String _sAttrValue = n.getNodeValue();
        if (_sAttrValue.length() > 2) {
          if (_sAttrValue.charAt(0) == '@') {
            String _sProp = _sAttrValue.substring(1, _sAttrValue.length() - 1);
            _sAttrValue = emisProp.getInstance(oContext_).get(_sProp);
          }
        }
        _oBuf.append(" ").append(_sAttrName).append("=\"");
        _oBuf.append(_sAttrValue).append("\"");
      }
    }
    _oBuf.append(">");
    out_.write(_oBuf.toString());
  }


  private void createRecordNum() throws Exception {
//        String _sSpanRecord =  sTableId_+"spanRecord";
//        String _sSpanCurrRecord =  sTableId_+"spanCurRecord";

    String _sSpanRecord = "idTBLspanRecord";
    String _sSpanCurrRecord = "idTBLspanCurRecord";

    String _sRecords = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_PEN);


    out_.write("<span id=\"" + _sSpanCurrRecord + "\"></span>/<span id=\"" + _sSpanRecord + "\">&nbsp;</span>" + _sRecords);
  }

  private void createPageNum() throws Exception {
    String _sSpanPage = "idTBLspanPage";
    String _sSpanTotalPage = "idTBLspanTotalPage";
    String _sThe = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_THE);
    String _sPage = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_PAGE);
    out_.write(_sThe + "<span id=\"" + _sSpanPage + "\">&nbsp;</span>/<span id=\"" + _sSpanTotalPage + "\">&nbsp;</span>" + _sPage);
  }


  private void writeInput(Element e) throws Exception {
    NamedNodeMap _oMap = e.getAttributes();
    int nLen = _oMap.getLength();
    out_.write("<input ");
    for (int i = 0; i < nLen; i++) {
      Node n = _oMap.item(i);
      String _sNodeName = n.getNodeName();
      String _sNodeValue = n.getNodeValue();
      out_.write(_sNodeName);
      out_.write("=\"");
      out_.write(_sNodeValue);
      out_.write("\" ");
    }
    String _sValue = emisXMLUtl.getElementValue(e);
    if (_sValue != null) {
      _sValue = request_.getParameter(_sValue);
      if (_sValue == null) _sValue = "";
      out_.write("value=\"");
      out_.write(_sValue);
      out_.write("\"");
    }
    writeln(">");
  }

  private void writeDataPageSize(Element e) throws Exception {
    String _sPageSizeSpan = (String) "idTBLspanPageSize";

    String _sEachPage = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_EACHPAGE);
    String _sRecords = oTrace_.getMsg(emisMessage.MSG_SHOWDATA_PEN);

    out_.write(_sEachPage + "<input id='" + _sPageSizeSpan + "' type=text maxlength='3' size='2' onblur=\"emisSpanPageSize(idTBLRec,this);\"></input>" + _sRecords);
  }

  // Cliff 90.03.13 新增
  private void writeHiddenPageSize(Element e) throws Exception {
    String _sPageSizeSpan = "idTBLspanPageSize";
    out_.write("<input id='" + _sPageSizeSpan + "' type=hidden></input>");
  }


  private void writeSQLCache(Element e) throws Exception {
    NamedNodeMap _oMap = e.getAttributes();
    int nLen = _oMap.getLength();
    for (int i = 0; i < nLen; i++) {
      Node n = _oMap.item(i);
      String _sNodeName = n.getNodeName();
      String _sNodeValue = n.getNodeValue();
      if ("name".equals(_sNodeName)) {
        out_.write(emisSQLCache.getSQL(oContext_, _sNodeValue, oUser_));
      }
    }
  }

  //$ 91.08.09 加入 emisHtml Option
  private void writeHtmlOption(Element e) throws Exception {
    NamedNodeMap _oMap = e.getAttributes();
    int nLen = _oMap.getLength();
    String _sSQL=null, _sPattern="%1 %2", _sValue="%1";
    for (int i = 0; i < nLen; i++) {
      Node n = _oMap.item(i);
      String _sNodeName = n.getNodeName();
      String _sNodeValue = n.getNodeValue();
      if ("sql".equals(_sNodeName)) {
        _sSQL=_sNodeValue;
      }
      if ("get".equals(_sNodeName)) {
        _sSQL=oBusiness_.getRequest().getParameter(_sNodeValue);
      }
      if ("fromat".equals(_sNodeName)) {
        _sPattern=_sNodeValue;
      }
      if ("value".equals(_sNodeName)) {
        _sValue=_sNodeValue;
      }
    }
    if (_sSQL!=null)
      out_.write(emisHtml.option(oContext_, _sSQL, _sPattern, _sValue));
  }

  // store 比較特別,需要作總公司與門市的管制

  private void _doit(Element root) throws Exception {
    NodeList nList = root.getChildNodes();
    int nLen = nList.getLength();
    if (nLen > 0) {
      for (int i = 0; i < nLen; i++) {
        Node n = nList.item(i);
        String _sNodeName = n.getNodeName();

        if (
            (n.getNodeType() == Node.CDATA_SECTION_NODE) ||
            (n.getNodeType() == Node.TEXT_NODE)
        ) {
          if(n.getNodeValue().indexOf("i18n")>0)
            parseCdata(n.getNodeValue());
          else
            out_.write(n.getNodeValue());
          continue;
        }

        if (n.getNodeType() != Node.ELEMENT_NODE) continue;
        Element e = (Element) n;


        if ("showrecordnum".equals(_sNodeName)) {
          createRecordNum();
          continue;
        }

        if ("showpagenum".equals(_sNodeName)) {
          createPageNum();
          continue;
        }

        if ("datasrc".equals(_sNodeName)) {
          createDataSrc(e);
          continue;
        }

        if ("tabledef".equals(_sNodeName)) {
          this.writeTableDef(e);
          _doit(e);
          continue;
        }


        if ("navigator".equals(_sNodeName)) {
          writeNavigator(e);
          continue;
        }

        if ("databrowser".equals(_sNodeName)) {
          writeDataBrowser(e);
          continue;
        }


        if ("datapagesize".equals(_sNodeName)) {
          writeDataPageSize(e);
          continue;
        }

        // Cliff 90.03.13 新增
        if ("hiddenpagesize".equals(_sNodeName)) {
          writeHiddenPageSize(e);
          continue;
        }

        if ("sqlcache".equals(_sNodeName)) {
          writeSQLCache(e);
          continue;
        }

        //$ 91.08.09 加入 emisHtml Option
        if ("htmloption".equals(_sNodeName)) {
          writeHtmlOption(e);
          continue;
        }
/* emisSQLCache 修改後,介面取消
                if( "storeno".equals(_sNodeName))
                {
                    out_.write( emisSQLCache.getStore(oContext_,oUser_) );
                    continue;
                }

                if("companyno".equals(_sNodeName))
                {
                    out_.write( emisSQLCache.getCompany(oContext_,oUser_) );
                    continue;
                }
*/
        // 2002.5.6 新增
        if ("functions".equals(_sNodeName)) {
          writeFunctions(e);
          continue;
        }

        if ("i18n".equals(e.getAttribute("type"))) {
          e = parseI18N(e);
        }
        // otherwise , 照原本樣子輸出
        writeNonLeaf(e);
        _doit(e);
        out_.write("</" + _sNodeName + ">");

        continue;
      }
    }
  }

  /**
   * 当有多语内容时进行CDATA处理
   * 先转成Document再对其Node处理逐一处理
   * @param cdata
   */
  private void parseCdata(String cdata) throws Exception {
    Document doc = emisXmlFactory.getXML(new ByteArrayInputStream(cdata.getBytes("UTF-8")));
    writeCdata(doc.getChildNodes());
  }

  /**
   * 处理多语并输出CDATA内容
   * @param list
   * @throws Exception
   */
  private void writeCdata(NodeList list) throws Exception {
    if (list != null) {
      int nLen = list.getLength();
      if (nLen > 0) {
        for (int i = 0; i < nLen; i++) {
          Node n = list.item(i);
          String _sNodeName = n.getNodeName();
          // 2010/11/11 Joe 修正純文本或Cdata內容時輸出空白Bug
          if ((n.getNodeType() == Node.CDATA_SECTION_NODE) || (n.getNodeType() == Node.TEXT_NODE)){
            if(n.getParentNode() != null){
              Element e = (Element) n.getParentNode();
              if (!"i18n".equals(e.getAttribute("type"))) {
                out_.write(n.getNodeValue());
              }
            }else {
              out_.write(n.getNodeValue());
            }
            continue;
          }
          if (n.getNodeType() == Element.ELEMENT_NODE) {
            Element e = (Element) n;
            if ("i18n".equals(e.getAttribute("type"))) {
              e = parseI18N(e);
              writeNonLeaf(e);
              out_.write(emisXMLUtl.getElementValue(e));
            } else {
              writeNonLeaf(e);
            }
            writeCdata(n.getChildNodes()); // recursive
            out_.write("</" + _sNodeName + ">");
          }
        }
      }
    }
  }

  /**
   * 多语内容替换实现
   * @param e
   * @return
   */
  private Element parseI18N(Element e) {
    String _sBundle = e.getAttribute("bundle");
    if (_sBundle == null || "".equals(_sBundle))
      return e;

    Element _elm = (Element) e.cloneNode(true);
    String _sDescription = _elm.getAttribute("description");

    String _sKey = emisXMLUtl.getElementValue(e);
    if (_sKey  != null && !"".equals(_sKey.trim())){
      _elm.setTextContent(getMessage(_sBundle, _sKey, _sDescription));
    }

    String _sTitle = _elm.getAttribute("title");
    if (_sTitle != null && !"".equals(_sTitle)) {
      _elm.setAttribute("title", getMessage(_sBundle, _sTitle, _sDescription));
    }

    String _sAlt = _elm.getAttribute("alt");
    if (_sAlt != null && !"".equals(_sAlt)) {
      _elm.setAttribute("alt", getMessage(_sBundle, _sAlt, _sDescription));
    }
    // 2012/05/21 Added by Joe 修复<button>在非IE下默认变为Submit自动响应回车的问题
    if ("button".equalsIgnoreCase(_elm.getTagName())) {
      _elm.setAttribute("type", "button");
    }
    return _elm;
  }

  // 92.01.23 Cliff 新增 Button 控制
  private void writeFunctions(Element e) throws Exception {

      String _sList = emisXMLUtl.getAttribute(e, "list");


    if (_sList != null) {
      String _sSpace = emisXMLUtl.getAttribute(e, "space");
      //emisUserButton.writeListButton(oContext_, _sList, _sSpace, out_);
      // 2010/05/12 Joe 增加傳request的API，方便加語系條件過濾資料
      emisUserButton.writeListButton(oContext_, _sList, _sSpace, out_, request_);
    } else {
      String _sKeys = emisXMLUtl.getAttribute(e, "keys");

      if (_sKeys==null || "".equals(_sKeys)) {
        _sKeys = oBusiness_.getRequest().getParameter("FUNC_KEYS");

      }
      if (_sKeys != null){
        emisUserButton.writeFuncButton(oUser_, _sKeys, out_);

      }
    }
  }

  /**
   *  This routine will generate a common xml. data format from emisDb<BR>
   *  you can generate oCData by<BR>
   *  Vector oCData = emisUtil.tokenizer("T1,T2");<BR>
   *  where T1,T2 is columname which need CDATA tag<BR>
   *  另外,若 result set 為空的,要產生一筆空資料<BR>
   *  不然會無法用 JavaScript addNew 新加一筆<BR>
   *  ,傳回 emisDb 內的筆數<BR>
   *  @@param emisDb oDb - Table<BR>
   *  @@param OutputStream oOutStream - 寫入之檔案<BR>
   *  @@param Vector oCData - CDATA定義之欄位<BR>
   *  @@param boolean isLimit - 是否限制筆數<BR>
   *  @@param int LimitedRows - 取出資料之筆數
   */
  private int genXMLData(emisDb oDb, OutputStream oOutStream, emisDataSrc oSrc, boolean isLimit, int limitedRows) throws Exception {
//        ArrayList oCData = oSrc.getCDataColumn();
    ArrayList oDateData = oSrc.getDateDataColumn();

    int _nCount = oDb.getColumnCount();   // 存欄位數
    if (_nCount <= 0) return 0;

    int[] _nType = new int[_nCount];  // 存各欄位之型態

    for (int i = 1; i <= _nCount; i++) {
      _nType[i - 1] = oDb.getColumnType(i);
    }

    String[] _sColName = new String[_nCount];  // 存欄位名稱
    boolean hasFieldRights=false;
    for (int i = 1; i <= _nCount; i++) {
      String _sCol = oDb.getColumnName(i);
      if (_sCol != null)
        _sCol = _sCol.toUpperCase();
       _sColName[i - 1] = _sCol;
     }

    String _oObject[] = new String[_nCount];  // 存資料

//        boolean [] _isCData = null; // 存 是否普通資料,或 CDATA
    boolean[] _isDateData = null; // 存 是否普通資料,或 DateDATA

 //       _isCData = new boolean [_nCount];  // true表示oCData內定義之欄位

    /**
     if( oCData != null )
     {
     for( int i=1;i<=_nCount;i++)
     {
     if( oCData.contains(_sColName[i-1]) )
     {
     _isCData[i-1] = true;
     }else{
     _isCData[i-1] = false;
     }
     }
     }
     */
    String _sDateSeparator = oSrc.getDateSeparator();

    if (!"".equals(_sDateSeparator)) {
      _isDateData = new boolean[_nCount];
      if (oDateData != null) {
        for (int i = 1; i <= _nCount; i++) {
          if (oDateData.contains(_sColName[i - 1])) {
            _isDateData[i - 1] = true;
          } else {
            _isDateData[i - 1] = false;
          }
        }
      }
    }


    /*-----------------------xml header-----------------------*/

    BufferedOutputStream oBufStream = new BufferedOutputStream(oOutStream);
    //OutputStreamWriter oDataOut = new OutputStreamWriter(oBufStream);
    OutputStreamWriter oDataOut = new OutputStreamWriter(oBufStream,"UTF-8");
    int _nSize = 0;
    try {
      oDataOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      //oDataOut.write("<?xml version=\"1.0\" ?>\n");
      oDataOut.write("<DATA>\n");

      if (oDb.next()) {
        emisStrBuf buf = new emisStrBuf(1024);
        emisStrBuf escapeBuf = new emisStrBuf(128);

        do {
          for (int i = 0; i < _nCount; i++) {
            _oObject[i] = oDb.getString(i + 1);
            //  oBusiness_.debug("Get:"+_oObject[i]);
          }
           writeData(buf, escapeBuf, oDataOut, _sColName, _oObject, _isDateData, _sDateSeparator);
          buf.setZeroLength();

          _nSize++;
          // 用來做筆數的限制
          if (isLimit)
            if (_nSize >= limitedRows)
              break;
        } while (oDb.next());

      } else {
        // 寫一筆空資料
//                oBusiness_.debug("write empty data");
        writeEmptyData(oDataOut, _sColName);
      }
      oDataOut.write("</DATA>\n");
      oDataOut.flush();

    } finally {

      oBufStream.flush();
      oBufStream = null;
      oDataOut = null;
    }
    return _nSize;
  }

  // add by andy 2006/10/21 : 杅擂眻諉迡善珜醱笢ㄛ祥淫迡善xml恅璃笢剩﹝
  private int genXMLData(emisDb oDb, Writer oDataOut, emisDataSrc oSrc, boolean isLimit, int limitedRows) throws Exception {
//        ArrayList oCData = oSrc.getCDataColumn();
    ArrayList oDateData = oSrc.getDateDataColumn();

    int _nCount = oDb.getColumnCount();   // 湔戲弇杅
    if (_nCount <= 0) return 0;

    int[] _nType = new int[_nCount];  // 湔跪戲弇眳倰怓

    for (int i = 1; i <= _nCount; i++) {
      _nType[i - 1] = oDb.getColumnType(i);
    }

    String[] _sColName = new String[_nCount];  // 湔戲弇靡備
    boolean hasFieldRights=false;
    for (int i = 1; i <= _nCount; i++) {
      String _sCol = oDb.getColumnName(i);
      if (_sCol != null)
        _sCol = _sCol.toUpperCase();
       _sColName[i - 1] = _sCol;
     }

    String _oObject[] = new String[_nCount];  // 湔訧蹋

//        boolean [] _isCData = null; // 湔 岆瘁⅛籵訧蹋,麼 CDATA
    boolean[] _isDateData = null; // 湔 岆瘁⅛籵訧蹋,麼 DateDATA

 //       _isCData = new boolean [_nCount];  // true桶尨oCData囀隅砱眳戲弇

    /**
     if( oCData != null )
     {
     for( int i=1;i<=_nCount;i++)
     {
     if( oCData.contains(_sColName[i-1]) )
     {
     _isCData[i-1] = true;
     }else{
     _isCData[i-1] = false;
     }
     }
     }
     */
    String _sDateSeparator = oSrc.getDateSeparator();

    if (!"".equals(_sDateSeparator)) {
      _isDateData = new boolean[_nCount];
      if (oDateData != null) {
        for (int i = 1; i <= _nCount; i++) {
          if (oDateData.contains(_sColName[i - 1])) {
            _isDateData[i - 1] = true;
          } else {
            _isDateData[i - 1] = false;
          }
        }
      }
    }


    /*-----------------------xml header-----------------------*/

    //  OutputStreamWriter oDataOut = new OutputStreamWriter(oBufStream,"UTF-8");
    int _nSize = 0;
    try {
      //      oDataOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      //oDataOut.write("<?xml version=\"1.0\" ?>\n");
      oDataOut.write("<DATA>\n");

      if (oDb.next()) {
        emisStrBuf buf = new emisStrBuf(1024);
        emisStrBuf escapeBuf = new emisStrBuf(128);

        do {
          for (int i = 0; i < _nCount; i++) {
            _oObject[i] = oDb.getString(i + 1);
//                        oBusiness_.debug("Get:"+_oObject[i]);
          }
          //           writeData(buf,escapeBuf,oDataOut,_sColName,_oObject,_isCData,_isDateData,_sDateSeparator);
          writeData(buf, escapeBuf, oDataOut, _sColName, _oObject, _isDateData, _sDateSeparator);
          //writeData1(buf, escapeBuf, oDataOut, _sColName, _oObject, _isDateData, _sDateSeparator);
          buf.setZeroLength();

          _nSize++;
          // 蚚懂酕捩杅腔癹秶
          if (isLimit)
            if (_nSize >= limitedRows)
              break;
        } while (oDb.next());

      } else {
        // 迡珨捩諾訧蹋
//                oBusiness_.debug("write empty data");
        writeEmptyData(oDataOut, _sColName);
      }
      oDataOut.write("</DATA>\n");
      oDataOut.flush();
    } finally {

    }
    return _nSize;
  }

  /**
   *  寫入資料至xmlData<BR>
   *  @@param emisStrBuf buf - buffer[1024]<BR>
   *  @@param emisStrBuf escapeBuf - buffer[128] 處裡中文用<BR>
   *  @@param OutputStreamWriter oDataStream - 寫入之檔案<BR>
   *  @@param String [] sColName - 欄位名稱<BR>
   *  @@param String [] oValue - 資料<BR>
   *  @@param boolean [] isCData - 是否為中文資料
   */

  private void writeData(emisStrBuf buf, emisStrBuf escapeBuf, OutputStreamWriter oDataStream,
                         String[] sColName, String[] oValue, boolean[] isDateData, String sDateSeparator) throws Exception {
    int len = sColName.length;

    buf.append("<REC>\n");
//        boolean _isCData;
    boolean _isDateData;
    for (int idx = 0; idx < len; idx++) {
      String _sName = sColName[idx];
//            _isCData = isCData[idx];
      _isDateData = (isDateData == null) ? false : isDateData[idx];

      buf.append("  <");
      buf.append(_sName);
      buf.append(">");

      String _oValue = oValue[idx];  // 上層傳來之 _oObject
      // write empty string will cause table border 不連續
      // &amp; 是 xml 的 &  , &nbsp; 是 html 的
      if (_oValue == null) {
        _oValue = "";
      } else {
        if (_isDateData) // 處理日期字串問題
        {
          // 從字串後面把字給插進去
          int _nLen = _oValue.length();
          if (_nLen >= 4) {
            escapeBuf.setZeroLength();


            escapeBuf.assign(_oValue);
            escapeBuf.insert(sDateSeparator, _nLen - 2);
            // 2002/06/17 Jacky: 若為完整的日期才需加第二個"/"符號
            _nLen = escapeBuf.length() - 5;
            if (_nLen > 2)
              escapeBuf.insert(sDateSeparator, escapeBuf.length() - 5);

            _oValue = escapeBuf.toString();
          }
        }
//      if( _isCData ) // 處裡中文問題
//                {
        escapeBuf.setZeroLength();
        //wing modify here
        if(!emisRightsCheck.getShowSet(this.oBusiness_, _sName)){
          _oValue=emisRightsCheck.getShowSetVal(oBusiness_, _sName);
        }
        escapeBuf.escapeXMLEntity(_oValue);
        buf.append(escapeBuf);
//                } else {
//                  buf.append(_oValue);
//                }

      }
      buf.append("</");
      buf.append(_sName);
      buf.append(">");
    }
    buf.append("</REC>\n");
    // to avoid a instanciate of class String
//        oBusiness_.debug("write:"+buf.toString());
    oDataStream.write(buf.getArray(), 0, buf.length());
    oDataStream.flush();

    // make the performance and memory usage more predictable...
  }

  // add by andy 2006/10/21 杅擂眻諉迡善珜醱笢ㄛ祥淫迡善xml恅璃笢
  private void writeData(emisStrBuf buf, emisStrBuf escapeBuf, Writer oDataStream,
                         String[] sColName, String[] oValue, boolean[] isDateData, String sDateSeparator) throws Exception {
    int len = sColName.length;

      buf.append("<REC>\n");
//        boolean _isCData;
      boolean _isDateData;
      for (int idx = 0; idx < len; idx++) {
        String _sName = sColName[idx];
//            _isCData = isCData[idx];
        _isDateData = (isDateData == null) ? false : isDateData[idx];

        buf.append("  <");
        buf.append(_sName);
        buf.append(">");

        String _oValue = oValue[idx];  // 奻脯換懂眳 _oObject
        // write empty string will cause table border 祥蟀哿
        // &amp; 岆 xml 腔 &  , &nbsp; 岆 html 腔
        if (_oValue == null) {
          _oValue = "";
        } else {
          if (_isDateData) // 揭燴ｶヽ趼背恀枙
          {
            // 植趼背綴醱參趼跤唇輛
            int _nLen = _oValue.length();
            if (_nLen >= 4) {
              escapeBuf.setZeroLength();


              escapeBuf.assign(_oValue);
              escapeBuf.insert(sDateSeparator, _nLen - 2);
              // 2002/06/17 Jacky: ﾕ峈俇淕腔ｶヽ符剒樓庵媼跺"/"睫瘍
              _nLen = escapeBuf.length() - 5;
              if (_nLen > 2)
                escapeBuf.insert(sDateSeparator, escapeBuf.length() - 5);

              _oValue = escapeBuf.toString();
          }
        }
//      if( _isCData ) // 揭爵笢恅恀枙
//                {
          escapeBuf.setZeroLength();
          //wing modify here
          if(!emisRightsCheck.getShowSet(this.oBusiness_, _sName)){
             _oValue=emisRightsCheck.getShowSetVal(oBusiness_, _sName);
          }
          escapeBuf.escapeXMLEntity(_oValue);
          buf.append(escapeBuf);
//                } else {
//                  buf.append(_oValue);
//                }

      }
      buf.append("</");
      buf.append(_sName);
      buf.append(">");
    }
    buf.append("</REC>\n");
    // to avoid a instanciate of class String
//        oBusiness_.debug("write:"+buf.toString());
      oDataStream.write(buf.getArray(), 0, buf.length());
      oDataStream.flush();

      // make the performance and memory usage more predictable...
    }


  private void writeData1(emisStrBuf buf, emisStrBuf escapeBuf,OutputStreamWriter  oDataStream,
                          String[] sColName, String[] oValue, boolean[] isDateData, String sDateSeparator) throws Exception {
    int len = sColName.length;

    buf.append("<REC>\n");
//        boolean _isCData;
    boolean _isDateData;
    for (int idx = 0; idx < len; idx++) {
      String _sName = sColName[idx];
//            _isCData = isCData[idx];
      _isDateData = (isDateData == null) ? false : isDateData[idx];

      buf.append("  <");
      buf.append(_sName);
      buf.append(">");

      String _oValue = oValue[idx];  // 上層傳來之 _oObject
      // write empty string will cause table border 不連續
      // &amp; 是 xml 的 &  , &nbsp; 是 html 的
      if (_oValue == null) {
        _oValue = "";
      } else {
        if (_isDateData) // 處理日期字串問題
        {
          // 從字串後面把字給插進去
          int _nLen = _oValue.length();
          if (_nLen >= 4) {
            escapeBuf.setZeroLength();
            escapeBuf.assign(_oValue);
            escapeBuf.insert(sDateSeparator, _nLen - 2);
            // 2002/06/17 Jacky: 若為完整的日期才需加第二個"/"符號
            _nLen = escapeBuf.length() - 5;
            if (_nLen > 2)
              escapeBuf.insert(sDateSeparator, escapeBuf.length() - 5);

            _oValue = escapeBuf.toString();
          }
        }
//      if( _isCData ) // 處裡中文問題
//                {
        escapeBuf.setZeroLength();
        escapeBuf.escapeXMLEntity(_oValue);
        buf.append(escapeBuf);
//                } else {
//                  buf.append(_oValue);
//                }

      }
      buf.append("</");
      buf.append(_sName);
      buf.append(">");
    }
    buf.append("</REC>\n");

    // to avoid a instanciate of class String
//        oBusiness_.debug("write:"+buf.toString());
   // oDataStream.write(buf.getArray(), 0, buf.length());
      String s = buf.toString();

          OutputStream out1 = new FileOutputStream("c:/zz.txt");
          OutputStream out = new FileOutputStream("c:/t2.zip");
    ZipEntry zEntry = new ZipEntry("zz.txt");
    ZipOutputStream oZip = new ZipOutputStream(out);
    zEntry.setMethod(ZipEntry.DEFLATED);
    oZip.putNextEntry(zEntry);
    oZip.setLevel(Deflater.BEST_SPEED);
       OutputStreamWriter f = new OutputStreamWriter(out1,"UTF-8");
       OutputStreamWriter f1 = new OutputStreamWriter(oZip);
       f.write(s);
       f.close();
       f1.write(s);
       f1.close();
     oDataStream.write(s) ;
    oDataStream.flush();

        // make the performance and memory usage more predictable...
  }
  /**
   *  寫入空資料至xmlData(僅有欄名)<BR>
   *  @@param OutputStreamWriter oDataStream - 寫入之檔案<BR>
   *  @@param String [] sColName - 欄位名稱
   */
  private void writeEmptyData(OutputStreamWriter oOutStream, String[] sColName) throws Exception {
    int len = sColName.length;
    oOutStream.write("<REC>\n");
    for (int idx = 0; idx < len; idx++) {
      String _sName = sColName[idx];
      oOutStream.write("  <");
      oOutStream.write(_sName);
      oOutStream.write("></");
      oOutStream.write(_sName);
      oOutStream.write(">");
    }
    oOutStream.write("</REC>\n");
  }

  private void writeEmptyData(Writer oOutStream, String[] sColName) throws Exception {
    int len = sColName.length;
    oOutStream.write("<REC>\n");
    for (int idx = 0; idx < len; idx++) {
      String _sName = sColName[idx];
      oOutStream.write("  <");
      oOutStream.write(_sName);
      oOutStream.write("></");
      oOutStream.write(_sName);
      oOutStream.write(">");
    }
    oOutStream.write("</REC>\n");
  }

  public static String getServerAddress(ServletContext oContext, HttpServletRequest request) {
    Object resolve = oContext.getAttribute("emis.client.resolvename");
    if (resolve == null) {
      return request.getServerName();
    } else {
      Boolean bool = (Boolean) resolve;
      if (bool.booleanValue()) {
        return request.getServerName();
      } else {
        return emisUtil.getServerIP();
      }
    }
  }

  /**
   * 取得指定语系内容
   * @param file
   * @param key
   * @param description 若无法取到指定资源时且description有值,则直接返回description,否则返回空
   * @return
   */
  public String getMessage(String file, String key, String description){
    if(oLang_ == null){
      oLang_ = getEmisLangRes();
    }
    if(oLang_== null){
      return description == null ? (key == null ? "" : key) : description;
    }
    // 设定语系
    oLang_.setLanguage((String) request_.getSession().getAttribute("languageType"));

    return oLang_.getMessage(file, key);
  }

  /**
   * 初始化并返回语系资源
   * @return
   */
  private emisLangRes getEmisLangRes() {
    emisLangRes _oLang = null;
    try {
      _oLang = emisLangRes.getInstance(oContext_.getRealPath(emisLangRes.ResourceSubPath));
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    return _oLang;
  }
}
