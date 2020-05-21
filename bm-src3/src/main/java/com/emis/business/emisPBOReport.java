/**
 * Created by IntelliJ IDEA.
 * User: jacky
 * Date: Nov 28, 2003
 * Time: 8:35:06 AM
 * 提供Java程式呼叫的報表程式
 */
package com.emis.business;

import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.xml.emisXmlFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;

public class emisPBOReport {
  private HashMap hmActions  = new HashMap();;
  private Writer out_=null;
  private ServletContext oContext_;
  private String sBusinessName_;
  private String sAction;
  private Document oBusinessDoc_;
  private emisBusiness oBusiness_;


  public emisPBOReport(ServletContext oContext, emisBusiness oBusiness, String sBusinessName, String sAction) {
    this.oContext_ = oContext;
    this.sBusinessName_ = sBusinessName.toLowerCase();
    this.sAction = sAction;
    this.oBusiness_ = oBusiness;
  }

  /**
   * 開始進行報表製作
   * @throws Exception
   */
  public void createReport() throws Exception {

    emisFileMgr _oMgr = emisFileMgr.getInstance(this.oContext_);
    emisDirectory _oDir = _oMgr.getDirectory("ROOT");
    _oDir = _oDir.subDirectory("business");
    emisFile _oFile = _oDir.getFile(this.sBusinessName_ + ".xml");
    loadXML(_oFile);
    doAction(sAction);
  }

  /**
   * 讀取XML檔案
   * @param f
   * @throws Exception
   */
  protected void loadXML(emisFile f) throws Exception {
 //   emisBusinessCacheMgr _oCacheMgr = emisBusinessCacheMgr.getInstance(oContext_);
 //   emisXMLCache _oBusinessXML = _oCacheMgr.get(sBusinessName_);
 //   if (_oBusinessXML != null) {
 //     oBusinessDoc_ = (Document) _oBusinessXML.getCache();
 //   } else {
      InputStream in = f.getInStream();
      try {
        oBusinessDoc_ = emisXmlFactory.getXML(in);
      } finally {
        in.close();
      }
 //   }
    setActions();
  }
    /**
     * 分析動作
     * @param sAction
     * @throws Exception
     */
  private void doAction(String sAction) throws Exception {

    Element eAct = (Element) hmActions.get(sAction);

    NodeList eAction = eAct.getChildNodes();
    int nLen = eAction.getLength();
    if (nLen > 0) {
      emisAction _oPrintData = null;
      for (int i = 0; i < nLen; i++) {
        Node n = eAction.item(i);
        if (n.getNodeType() != Node.ELEMENT_NODE) continue;
        Element e = (Element) n;
        String _sName = n.getNodeName();

        // 因為 showdata 會重覆進入
        // 但因為,變數只能宣告一次

        if ("report".equals(_sName)) {
          _oPrintData = new emisPrintData(this.oBusiness_, e, out_);
          _oPrintData.doit();
        }
      }
      _oPrintData = null;
    } // end of len > 0
  }

   /**
    * 根據動作設定相關資料
    * @throws Exception
    */
  protected void setActions() throws Exception {
    hmActions.clear();
    NodeList nList = oBusinessDoc_.getElementsByTagName("act");
    if (nList != null) {
      int len = nList.getLength();
      for (int i = 0; i < len; i++) {
        Node oNode = nList.item(i);
        if (oNode.getNodeType() == Node.ELEMENT_NODE) {
          Element e = (Element) oNode;
          String sName = e.getAttribute("name");

          if ((sName != null) && (sName.length() > 0)) {
            hmActions.put(sName, e);
          } else {

          }
        }
      }
    }
  }

  /**
   * 設定輸出的檔檔名
   * @param _sFile
   */
  public void setReportFile(String _sFile){
    oBusiness_.setParameter("PROP_REPORT_FILE" , _sFile);
  }
/**
 * 設定輸出檔目錄
 * @param _sDir
 */
  public void setReportDir(String _sDir){
    oBusiness_.setParameter("PROP_REPORT_DIR" , _sDir);
  }

  /**
   * 設定回應時寫入的Writer
   * @param out
   */
  public void setWriter(Writer out) {
    this.out_ = out;
  }

  /**
   * 設定傳入參數
   * @param _sName
   * @param _sObj
   */
  public void setParameter(String _sName , String _sObj){
    oBusiness_.setParameter(_sName , _sObj);
  }
}
