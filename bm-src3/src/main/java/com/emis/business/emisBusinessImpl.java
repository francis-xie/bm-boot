/* $Id: emisBusinessImpl.java 9388 2017-08-25 03:52:04Z andy.he $ * * Copyright (c) EMIS Corp. All Rights Reserved. */package com.emis.business;import com.emis.audit.emisAudit;import com.emis.db.*;import com.emis.file.emisFile;import com.emis.file.emisFileMgr;import com.emis.trace.emisError;import com.emis.trace.emisTracer;import com.emis.user.emisUser;import com.emis.util.emisUtil;import com.emis.util.emisXMLUtl;import com.emis.xml.emisXMLCache;import com.emis.xml.emisXmlFactory;//-import com.emis.schedule.dailyclose.caves.Eros2Caves_Line;import org.w3c.dom.*;import javax.servlet.ServletContext;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import java.io.InputStream;import java.io.OutputStream;import java.io.PrintWriter;import java.io.Writer;import java.sql.SQLException;import java.util.*;/** * 實作emisBusiness. * * @author Robert * @version 1.6 2004/08/09 Jerry: 由emisFieldFormat取欄寬 * Track+[15095] sunny.zhuang 2010/06/30 增加"我的工作清單"  * fang 2010/09/19 修正关闭预设查询后，会跑query，但未带入条件导致查全部数据的BUG */public class emisBusinessImpl implements emisBusiness {  private Writer out_;  private emisUser user_;  private emisHttpServletRequest request_;  private emisHttpServletResponse response_;  private HashMap hmActions = new HashMap();  private boolean isDebug_;  private Hashtable oHashVar_ = new Hashtable();  private emisAjax ajax ;  private Document oBusinessDoc_;  private ServletContext oContext_;  private StringBuffer oDebugMsg_;  private String sBusinessName_;  private String sConfigFile_;  private String sURL_;  private String sMenuCode_ = "";  private String sReportSql_ = "";  private int nDbMaxRef;  private int nDbTotalRef;  private Map oMap_ = null;  private String showCount = null;  private boolean enablePreQuery;  private boolean enablePrePrint;  /**   * @param sBusinessName   * @param application   * @param oUser   * @param sConfigFile   * @param bIsPreProcessIf   * @throws Exception   */  public emisBusinessImpl(String sBusinessName, ServletContext application,                          emisUser oUser, String sConfigFile, boolean bIsPreProcessIf) throws Exception {    emisFile _oFile = emisFileMgr.getInstance(application).getFactory()        .getFile("business", sConfigFile);    loadBusiness(sBusinessName, application, oUser, _oFile, bIsPreProcessIf);    try {      emisProp prop = emisProp.getInstance(oContext_);      enablePreQuery = prop.get("preQuery","N").equalsIgnoreCase("Y");      enablePrePrint = prop.get("prePrint","N").equalsIgnoreCase("Y");    } catch(Exception ex){      enablePreQuery = false;    }  }  /**   * @param sBusinessName   * @param application   * @param oUser   * @param f   * @param bIsPreProcessIf   * @throws Exception   */  public emisBusinessImpl(String sBusinessName, ServletContext application,                          emisUser oUser, emisFile f, boolean bIsPreProcessIf) throws Exception {    loadBusiness(sBusinessName, application, oUser, f, bIsPreProcessIf);  }  /**   * @return   */  public String getConfigFile() {    return sConfigFile_;  }  /**   * @param sBusinessName   * @param application   * @param oUser   * @param f   * @param bIsPreProcessIf   * @throws Exception   */  private void loadBusiness(String sBusinessName, ServletContext application,                            emisUser oUser, emisFile f, boolean bIsPreProcessIf) throws Exception {    sBusinessName_ = sBusinessName;    sConfigFile_ = f.getFileName();    oContext_ = application;    user_ = oUser;    isDebug_ = user_.isDebug();    if (isDebug_)      setDebug(true);    loadXML(f, bIsPreProcessIf);  }  /**   * @return   */  public String getName() {    return sBusinessName_;  }  /**   * @return   */  public String getID() {    // 把 XXX.XML 轉成 XXX 傳回    int idx = sConfigFile_.lastIndexOf(".");    if (idx != -1) {      return sConfigFile_.substring(0, idx);    } else {      return sConfigFile_;    }  }  /**   * 設成除錯模式; 寫出SQL敘述.   *   * @return   */  public boolean isDebug() {    return isDebug_;  }  /**   * now it is cacheable...   *   * @param f   * @param bIsPreProcessIf   * @throws Exception   */  protected void loadXML(emisFile f, boolean bIsPreProcessIf) throws Exception {    emisBusinessCacheMgr _oCacheMgr = emisBusinessCacheMgr.getInstance(oContext_);    emisXMLCache _oBusinessXML = _oCacheMgr.get(sBusinessName_);    if (_oBusinessXML != null) {      oBusinessDoc_ = (Document) _oBusinessXML.getCache();    } else {      InputStream in = f.getInStream();      try {        oBusinessDoc_ = emisXmlFactory.getXML(in);        if (bIsPreProcessIf) {          preProcess(oBusinessDoc_);          saveToSeeWhatIsWrong(oBusinessDoc_, f);        }else{          // 预处理include标记          prePprocessInclude(oBusinessDoc_);          // use for  debug          //saveToSeeWhatIsWrong(oBusinessDoc_, f);        }      } finally {        in.close();      }      // test , save processesed file to business directory      /* put it in cache... */      emisXMLCache _oXMLCache = new emisXMLCache(sBusinessName_, f, oBusinessDoc_);      _oCacheMgr.put(_oXMLCache);    }    setActions();  }  /**   * this is just called and save the docment object to   * cache directory for debug usage   */  private void saveToSeeWhatIsWrong(Document doc, emisFile f) throws Exception {    emisFile cacheFile = f.getDirectory().subDirectory("cache").getFile(f.getFileName());    OutputStream os = cacheFile.getOutStream(null);    try {      PrintWriter _out = new PrintWriter(os);      emisXmlFactory.saveXML(doc, _out);      _out.flush();    } finally {      os.close();      os = null;    }  }  /**   * 2002.5.6 Tom,Michael 說要加 <if> .. </if>   * 寫成同一個 Source,用 if 分不同客戶   * 因為是用 pre-process 的方式先處理   * 且處理過後是放在 cache, 所以系統效能影響不大   *   * @param doc   * @throws Exception   */  protected void preProcess(Document doc) throws Exception {    emisProp prop = emisProp.getInstance(oContext_);    NodeList list = doc.getChildNodes();    removeIf(prop, list);    // 2010/07/22 Joe add 预处理include标记    prePprocessInclude(doc);  }  /**   * 增加<include target='XX' />预处理逻辑,实现共用XML   * 2010/07/22 Joe   * @param doc   * @throws Exception   */  private void prePprocessInclude(Document doc) throws Exception {    if (doc == null ) return;    NodeList list = doc.getElementsByTagName("include");    int nLen = list.getLength();    if (nLen == 0) return;    for (int i = nLen - 1; i >= 0; i--) {      Node n = list.item(i);      String id = ((Element)n).getAttribute("target");      Element include = doc.getElementById(id);      n.getParentNode().replaceChild(include.cloneNode(true), n);    }  }  private void removeIf(emisProp prop, NodeList list) throws Exception {    if (list == null) return;    int nLen = list.getLength();    if (nLen == 0) return;    for (int i = nLen - 1; i >= 0; i--) {      Node n = list.item(i);      if (n.getNodeType() == Element.ELEMENT_NODE) {        NodeList childs = n.getChildNodes();        removeIf(prop, childs); // recursive        if ("if".equalsIgnoreCase(n.getNodeName())) {          NamedNodeMap map = n.getAttributes();          int mLen = map.getLength();          boolean match = true;          for (int j = 0; j < mLen; j++) {// 要每個都對了, <if> 才算成立            Node attrNode = map.item(j);            String attrName = attrNode.getNodeName();            String attrValue = attrNode.getNodeValue();            String propValue = prop.get(attrName);            if (propValue == null) { // emisProp 沒有此設定,不成立              n.getParentNode().removeChild(n);              match = false;              break;            }            if (!simpleMatch(attrValue, propValue)) { // if 不成立,just remove it              n.getParentNode().removeChild(n);              match = false;              break;            }          } // end of for          // 通過 match, 表示成立,把下一層的 Node,連到上一層來          if (match) {            Node parent = n.getParentNode();            NodeList nlist = n.getChildNodes();            int _iLength = nlist.getLength();            for (int j = 0; j < _iLength; j++) {              Node append = nlist.item(j);              // you have to clone the node,or              // it will be deleted when the Node n is deleted              parent.appendChild(append.cloneNode(true));            }            parent.removeChild(n);          }        }      }    }  }  // simple match two string ,譬如  // %群豐% match 群豐資訊股份  private boolean simpleMatch(String regex, String match) {    int rLen = regex.length();    int mLen = match.length();    int i = 0;    int j = 0;    while (i < rLen) {      if (regex.charAt(i) == '%') {        i++; // 要往後找到一個 match,才算可以        if (i >= rLen) return true; // 最後一個 % , 表示後面應該都 match        if (j >= mLen) return true;        int newIdx = match.indexOf(regex.charAt(i), j);        if (newIdx == -1) return false; // 沒找到,不 match        j = newIdx;      } else {        if (regex.charAt(i) != match.charAt(j)) {          return false;        }      }      i++;      j++;    }    if (j != mLen) return false;    return true;  }  /**   * @throws Exception   */  protected void setActions() throws Exception {    hmActions.clear();    NodeList nList = oBusinessDoc_.getElementsByTagName("act");    if (nList != null) {      int len = nList.getLength();      for (int i = 0; i < len; i++) {        Node oNode = nList.item(i);        if (oNode.getNodeType() == Node.ELEMENT_NODE) {          Element e = (Element) oNode;          String sName = e.getAttribute("name");          // robert, 2009/12/11 修改成可以用  <act name="default,query"> 格式          int idx = sName.indexOf(",");          while( idx != -1 ) {        	  String sPreName = sName.substring(0,idx);              if ((sPreName != null) && (sPreName.length() > 0)) {                  hmActions.put(sPreName, e);              }        	          	  sName = sName.substring(idx+1);        	  idx = sName.indexOf(",");          }                    if ((sName != null) && (sName.length() > 0)) {            hmActions.put(sName, e);          }         }      }    }  }  /**   * 設輸出物件.   *   * @param out   */  public void setWriter(Writer out) {    out_ = out;  }  /**   * 設參數.   *   * @param request   * @param response   */  public void setParameter(HttpServletRequest request, HttpServletResponse response) {    if (request != null && response != null) {      sURL_ = request.getRequestURI();      request_ = new emisHttpServletRequest(request);      response_ = emisHttpServletResponse.getInstance(response);    }  }  /**   * @param request   */  public void setParameter(HttpServletRequest request) {    if (request != null) {      sURL_ = request.getRequestURI();      request_ = new emisHttpServletRequest(request);    }  }  /**   * @throws Exception   */  public void process() throws Exception {    String sAction = null;        // accept lower and upper case of 'act'    if (request_ != null) {        sAction = request_.getParameter("ACT");    }    if (sAction == null) {        sAction = request_.getParameter("act");    }    if (sAction == null)      sAction = "default";    process(sAction);  }  private void startDebug(String sAction) {      oDebugMsg_ = new StringBuffer(1024);      debug("<!--EMIS XML DEBUG----");      //debug("<p style='display:none'>");      debug("Business:" + sBusinessName_);      debug("BusinessFile:" + sConfigFile_);      debug("page:" + request_.getServletPath());      Set _actSet = hmActions.keySet();      Iterator _it = _actSet.iterator();      debug("--------support action name---------");      while (_it.hasNext()) {        Object key = _it.next();        debug("act=" + key);      }      debug("current ACTION=" + sAction + "------");      debug("傳入參數------");      Enumeration e = request_.getParameterNames();      while (e.hasMoreElements()) {        String key = (String) e.nextElement();        String value = request_.getParameter(key);        debug(key + "=" + value);      }	    }   private HashMap processPrePrint(){    if(!enablePrePrint) return null;    String name = request_.getParameter("prePrintName");//先取參數    if(name == null || "".equals(name))      name = (String)request_.getAttribute("prePrintName");//沒有再取屬性    //System.out.println("================");    //System.out.println("name = " + name);    if(name == null || "".equals(name)) return null;    emisGetPreQuery query = new emisGetPrePrint(oContext_,sBusinessName_,user_.getID(),request_.getParameter("guid"));    HashMap params = query.getParams(name);    this.setParameter(params); //設置參數    return params;  }  /**	 * process 某一個 action. 舉凡產生網頁,列印報表,圖形分析,都會透過這 個 method, 而所有的 Error 也會從這?   *   * @param sAction   * @throws Exception   */  public void process(String sAction) throws Exception {    sAction = processPreQuery(sAction,this.oMap_);    if (isDebug_) {			oDebugMsg_ = new StringBuffer(1024);			debug("<!--EMIS XML DEBUG----");			debug("Business:" + sBusinessName_);			debug("BusinessFile:" + sConfigFile_);			debug("page:" + request_.getServletPath());			Set _actSet = hmActions.keySet();			Iterator _it = _actSet.iterator();			debug("--------support action name---------");			while (_it.hasNext()) {				Object key = _it.next();				debug("act=" + key);			}			debug("current ACTION=" + sAction + "------");			debug("傳入參數------");			Enumeration e = request_.getParameterNames();			while (e.hasMoreElements()) {				String key = (String) e.nextElement();				String value = request_.getParameter(key);				debug(key + "=" + value);			}		}    // reset Db reference Counter    this.nDbMaxRef = 0;    this.nDbTotalRef = 0;    Exception exc = null;    try {      emisAudit audit = emisAudit.getInstance(oContext_);      if (audit != null) {        audit.audit(user_, sBusinessName_, sAction);        if (request_ != null) {          request_.setAttribute("SQL_LOG_USERID", user_.getName());          request_.setAttribute("SQL_LOG_BusinessName", sBusinessName_);          oContext_.setAttribute("SQLFilterHttpRequest", request_);        }      }      doAction(sAction);    } catch (Exception e) {      debug("Exception:" + emisUtil.getStackTrace(e));      throw e;    } finally {      if (isDebug_) {        debug("emisDb Total Count:" + this.nDbTotalRef + "(not Freed,should be 0)");        debug("emisDb Max Count:" + this.nDbMaxRef +            "(Concurrent Max Connection,the fewer the better)");        debug("EMIS XML DEBUG-->"); // 結尾的 mark        if (out_ != null) {          out_.write(oDebugMsg_.toString());        }        this.setAttribute("DEBUG_MSG", oDebugMsg_.toString());      }    }  }  /**   * 预设查询   *   * 内部process调用   * */  private String processPreQuery(String sAction,Map oMap){    if(!"preQuery".equalsIgnoreCase(sAction)) //_oBusiness.process("preQuery");明确写定      return sAction;    //if(!enablePreQuery) return "query";    String act = request_.getParameter("act");    if(!(act == null || "".equals(act)))//如果有手动查询,忽略      return act;    else if(!enablePreQuery)      return "default";        String keys=(String)request_.getParameter("keys");//取出KEYS與簡體區分    emisGetPreQuery query = new emisGetPreQuery(oContext_,keys,user_.getID());    String name = request_.getParameter("preQueryName");//先取参数    showCount = (String)request_.getAttribute("showCount");    if(name == null || "".equals(name))      name = (String)request_.getAttribute("preQueryName");//没有再取属性    HashMap params = query.getParams(name);    //System.out.println("更新我的工作事项行数:"+name+"&&&"+request_.getParameter("preQueryCount"));    query.updPreQueryCount(name,request_.getParameter("preQueryCount")); //更新我的工作事项行数    if(params.size() < 1 && showCount == null) return "default";//没有数据时返回default    this.setParameter(params); //设置参数    if(oMap != null)      oMap.putAll(params);    return "query";//preQuery >>> query  }  /**   * 网页显示查询条件   * @param oMap : 查询条件   * */  public void process(String sAction, Map oMap) throws Exception {    this.oMap_ = oMap;    process(sAction);  }  /**	 * 设Business的参数.	 *	 * @param oMap	 */  public void setParameter(HashMap oMap) {    request_.setParameter(oMap);  }  /**   * 特別隔離出來是因為 ajax 不能有其他的 debug 輸出   * @throws Exception   */  public void processAjax() throws Exception {	  isDebug_ = false; 	  String ajaxDebug = request_.getParameter("AJAX_DEBUG");	  if( "true".equalsIgnoreCase(ajaxDebug) ) {		  isDebug_ = true;	  }	  process();  }    private void doAction(String sAction) throws Exception {    if (out_ == null)      emisTracer.get(oContext_).sysError(this, emisError.ERR_BUSINESS_OUTPUT_NOT_SET);    Element eAct = (Element) hmActions.get(sAction);    if (eAct == null) {      emisTracer.get(oContext_).sysError(this, emisError.ERR_BUSINESS_ACT_NOT_EXIST,          "act=" + sAction + " file=" + sConfigFile_ + " url=" + sURL_ +          " user title=" + (String) user_.getAttribute("TITLE") + " request title=" +          request_.getParameter("TITLE"));    }    NodeList eAction = eAct.getChildNodes();    int nLen = eAction.getLength();    if (nLen > 0) {      emisAction _oBussinessData = null;      emisAction _oShowData = null;      emisAction _oPrintData = null;      emisAction _oDataBase = null;      emisAction _oWriteChart = null;      emisAction _oPrintChart = null;      emisAction _oBarcode = null;      emisTransaction _oTrans = null;      for (int i = 0; i < nLen; i++) {        Node n = eAction.item(i);        if (n.getNodeType() != Node.ELEMENT_NODE) continue;        Element e = (Element) n;        String _sName = n.getNodeName();        // 因為 showdata 會重覆進入        // 但因為,變數只能宣告一次        if (_sName.equals("classes")) {          if (_oBussinessData != null) {            //_oBussinessData.set(this,e,out_);          } else {            _oBussinessData = new emisBusinessObject(this, e, out_, oContext_,                request_, response_, user_);          }          _oBussinessData.doit();        } else if ("showdata".equals(_sName)) {          if (_oShowData != null) {            _oShowData.set(this, e, out_);          } else {            if (showCount == null || "".equals(showCount))              _oShowData = new emisShowData(this, e, out_);            else {              _oShowData = new emisShowDataCount(this, e, out_, showCount);              showCount = null;            }          }          _oShowData.doit();        } else if( "ajax".equals(_sName)) {            if (showCount == null || "".equals(showCount)) {              if (ajax != null) {                ajax.set(this, e, out_);              } else {                ajax = new emisAjax(this, e, out_);              }              ajax.doit();            } else {              _oShowData = new emisShowDataCount(this, e, out_, showCount);              _oShowData.doit();              showCount = null;            }        } else if ("database".equals(_sName)) {          if (_oDataBase != null) {            _oDataBase.set(this, e, out_);          } else {            _oDataBase = new emisDatabase(this, e, out_);          }          _oDataBase.doit();        } else if ("report".equals(_sName)) {          processPrePrint();          if (emisXMLUtl.getAttribute(e, "isGetReportSql") != null) {            if (n.getNodeType() != Node.ELEMENT_NODE) continue;            Element ee = (Element) n;            emisXMLUtl.getElementValue(ee, "sql");            ee.getElementsByTagName("condition");            //Dana 2011/07/20 实例化emisDB未使用,也未关闭,所以直接传null            Object[] obj = doConditionStmt(this, null, emisXMLUtl.getElementValue(ee, "sql"), false, ee.getElementsByTagName("condition"));            sReportSql_ = emisUtil.replaceParam((String) obj[0], (List) obj[1]);             return;          } else if (_oPrintData != null) {            _oPrintData.set(this, e, out_);          } else {            _oPrintData = new emisPrintData(this, e, out_);          }          _oPrintData.doit();        } /*else if ("printchart".equals(_sName)) {          if (_oPrintChart != null) {            _oPrintChart.set(this, e, out_);          } else {            _oPrintChart = new emisPrintChart(this, e, out_);          }          _oPrintChart.doit();        } else if ("writechart".equals(_sName)) {          if (_oWriteChart != null) {            _oWriteChart.set(this, e, out_);          } else {            _oWriteChart = new emisWriteChart(this, e, out_);          }          _oWriteChart.doit();        }*/ else if ("expirecache".equals(_sName)) {          String _sReloadName = e.getAttribute("name");          if (_sReloadName != null) {            emisSQLCache.expire(this.oContext_, _sReloadName);            this.debug("expire SQLCache:" + _sReloadName);          } else {            this.debug("reload sql cache , but no sql name setting");          }        } else if ("barcode".equals(_sName)) {          if (_oBarcode != null) {            _oBarcode.set(this, e, out_);          } else {            _oBarcode = new emisBarcode(this, e, out_);          }          _oBarcode.doit();        } else if ("reloadprop".equals(_sName)) {          emisProp.reload(oContext_);        } else if ("reloadsqlcache".equals(_sName)) {          emisSQLCache.reload(oContext_);        } else if ("transaction".equals(_sName)) {          if (_oTrans != null) {            _oTrans.set(this, e, out_);          } else {            _oTrans = new emisTransaction(this, e, out_);          }          _oTrans.doit();        } else if ("next".equals(_sName)) {          NodeList nNext = e.getElementsByTagName("goto");          if (nNext.getLength() > 0) {            Element eGoto = (Element) nNext.item(0);            Node f = eGoto.getFirstChild();            if (f != null) {              String sGoto = f.getNodeValue();              eAct = (Element) hmActions.get(sGoto);              eAction = eAct.getChildNodes();              nLen = eAction.getLength();              i = -1;            }          }        }      }      if (_oShowData != null) {        emisShowData data = (emisShowData) _oShowData;        data.declareTableRecord(out_);      }      _oShowData = null;      _oPrintData = null;      _oDataBase = null;      _oWriteChart = null;      _oPrintChart = null;      _oBarcode = null;      _oTrans = null;    } // end of len > 0  }  public Object[] doConditionStmt(emisBusiness oBusiness, emisDb oDb, String sSQL,                                  boolean isScrollable, NodeList eCondition) throws Exception {    Object[] obj = new Object[2];    oBusiness.debug("do Condition");    List value = new ArrayList();    ArrayList _oConditionList = new ArrayList();    int _iLength = eCondition.getLength();    for (int i = 0; i < _iLength; i++) {      Element _eCondition = (Element) eCondition.item(i);      oBusiness.debug("Condition" + i);      emisCondition _oCondition = new emisCondition(oBusiness, _eCondition);      sSQL = _oCondition.replace(sSQL);      _oConditionList.add(_oCondition);    }    oBusiness.debug("Get " + _oConditionList.size() + " condition");    try {      int _nSetter = 1;      int _iSize = _oConditionList.size();      for (int i = 0; i < _iSize; i++) {        emisCondition _oCondition = (emisCondition) _oConditionList.get(i);        _oCondition.setSQL2ReportSql(_nSetter, value);      }      obj[0] = sSQL;      obj[1] = value;    } catch (Exception ignore) {      oBusiness.debug("[emisCondition] doCondition:" + ignore.getMessage());    }    return obj;  }  /**   * 設定是否為 debug   */  private void setDebug(boolean isDebug) {    isDebug_ = isDebug;    if (!isDebug_) {      // debug is only used within process method !!!      oDebugMsg_ = null;    }  }  /**   * 加入一個訊息到 debug message 中   *   * @param msg   */  public void debug(String msg) {    if (!isDebug_) return;    if (oDebugMsg_ == null) return;    oDebugMsg_.append(msg).append(emisUtil.LINESEPARATOR);  }  /**   * 除錯.   *   * @param e   */  public void debug(Exception e) {    if (!isDebug_) return;    if (oDebugMsg_ == null) return;    String _sStackTrace = emisUtil.getStackTrace(e);    oDebugMsg_.append(_sStackTrace).append(emisUtil.LINESEPARATOR);  }  /**	 * 傳回除錯訊息, 供TestCase使用. emisBusiness無此method, 無法輸出. 改以	 * getAttribute("DEBUG_MSG")取字串.   *   * @return   */  public String getDebugMsg() {    return oDebugMsg_.toString();  }  /**	 * 傳回執行act 的 sql 改以 getAttribute("DEBUG_MSG")取字串.   *   * @return   */  public String getReportSql() {    return this.sReportSql_;  }  /**   * 設屬性.   *   * @param key   * @param value   */  public void setAttribute(Object key, Object value) {    if (value != null)      oHashVar_.put(key, value);  }  /**   * 取屬性.   *   * @param key   * @return   */  public Object getAttribute(Object key) {    return oHashVar_.get(key);  }  /**   * 傳回使用者物件.   *   * @return   */  public emisUser getUser() {    return user_;  }  /**   * @return   */  public HttpServletRequest getRequest() {    return request_;  }  /**   * @return   */  public ServletContext getContext() {    return oContext_;  }  /**   * 設Business的參數.   *   * @param sKey   * @param sValue   */  public void setParameter(String sKey, String sValue) {    request_.setParameter(sKey, sValue);  }  /**   *   */  public void clearParamter() {    request_.clearParameter();  }  /**	 * 我們需要知道一次 action 中,同一時間最多用了幾個 emisDb. used for debugging   */  public void addReferenceCount() {    nDbTotalRef++;    if (nDbTotalRef > nDbMaxRef) {      nDbMaxRef = nDbTotalRef;    }  }  /**   *   */  public void decReferenceCount() {    nDbTotalRef--;  }  /**   * err to msg.   *   * @param e   * @return   */  public String errToMsg(Exception e) {    if (e == null) return null;    if (e instanceof SQLException) {      try {        emisDbConnector _oConnector = emisDbMgr.getInstance(oContext_).getConnector();        return _oConnector.errToMsg((SQLException) e);      } catch (Exception ignore) {        return e.getMessage();      }    }    return e.getMessage();  }  /**   * @return   */  public String getMenuCode() {    return (String) this.getAttribute("sysMENU_CODE");  }  /**   * 2004/03/15: 提供前端JSP程式取得相對應的FIELDFORMAT對應TYPE的欄位寬度.   * 20040809改由emisFieldFormat取出欄寬.   *   * @param sType 對應到FiledFormat 內的FD_TYPE欄位   * @return   * @throws Exception   */  public int getFieldWidth(String sType) throws Exception {    int _iMaxlen = 0;    try {      emisFieldFormatBean bean = emisFieldFormat.getInstance(oContext_)          .getBean(sType);      _iMaxlen = bean.getMaxLen();    } catch (Exception e) {      throw e;    }    return _iMaxlen;  }  /**   * 2004/08/09: 提供前端JSP程式取得相對應的FIELDFORMAT對應TYPE的欄位是否左補零機制.   *   * @param sType 對應到FiledFormat 內的FD_TYPE欄位   * @return   * @throws Exception   */  public String getFieldLeftZero(String sType) throws Exception {    String _sLeftZero = "N";    try {      emisFieldFormatBean bean = emisFieldFormat.getInstance(oContext_)          .getBean(sType);      _sLeftZero = bean.getLeftZero();    } catch (Exception e) {      throw e;    }    return _sLeftZero;  }	public emisHttpServletResponse getResponse_() {		return response_;	}	public void setResponse_(emisHttpServletResponse response_) {		this.response_ = response_;	}}