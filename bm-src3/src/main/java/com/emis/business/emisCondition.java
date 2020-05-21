/* $Id: emisCondition.java 4 2015-05-27 08:13:47Z andy.he $ * * Copyright (c) EMIS Corp. All Rights Reserved. */package com.emis.business;import com.emis.db.emisDb;import com.emis.util.emisUtil;import com.emis.util.emisXMLUtl;import org.w3c.dom.Element;import org.w3c.dom.NodeList;import javax.servlet.ServletContext;import javax.servlet.http.HttpServletRequest;import java.sql.ResultSet;import java.util.ArrayList;import java.util.List;/** * emisDatabase 會用到此 Class, 負責處理 XML tag 中的 condition. * condition 的參數只處理 pname 和 where. * * @author Robert * @version 2002 * @version 2004/07/08 Jerry: 增加直接替換的%TODATE_DATE%, %TODAY_TIME% */public class emisCondition {  /** 除錯用 */  public static final boolean _DEBUG_ = true;  private ArrayList oCondList_ = new ArrayList();  private HttpServletRequest oRequest_;  private String sReplace_;  private emisBusiness oBusiness_;  /**   * 主邏輯方法.   *   * @param oBusiness   * @param oDb   * @param sSQL   * @param isScrollable   * @param eCondition   * @throws Exception   */  public static String doCondition(emisBusiness oBusiness, emisDb oDb, String sSQL,      boolean isScrollable, NodeList eCondition) throws Exception {    oBusiness.debug("do Condition");    ArrayList _oConditionList = new ArrayList();    int _iLength = eCondition.getLength();    for (int i = 0; i < _iLength; i++) {      Element _eCondition = (Element) eCondition.item(i);      oBusiness.debug("Condition" + i);      emisCondition _oCondition = new emisCondition(oBusiness, _eCondition);      sSQL = _oCondition.replace(sSQL);      _oConditionList.add(_oCondition);    }    oBusiness.debug("Get " + _oConditionList.size() + " condition");    try {      int _nSetter = 1;      if (isScrollable) {        oDb.prepareStmt(sSQL, ResultSet.TYPE_SCROLL_INSENSITIVE,            ResultSet.CONCUR_READ_ONLY);      } else {        oDb.prepareStmt(sSQL);      }       ArrayList value= new ArrayList();      int _iSize = _oConditionList.size();      for (int i = 0; i < _iSize; i++) {        emisCondition _oCondition = (emisCondition) _oConditionList.get(i);        _nSetter = _oCondition.setSQL(oDb, _nSetter, value);      }    } catch (Exception ignore) {      oBusiness.debug("[emisCondition] doCondition:" + ignore.getMessage());    }         return sSQL;  }    /**      * 這邊現在是給 emisDataSrc 用的 , 主要會多回傳一個 object[2] ,一個 SQL , 一個參數 array       * 給後面使用參數帶換      * @param oBusiness      * @param oDb      * @param sSQL      * @param isScrollable      * @param eCondition      * @throws Exception      */     public static Object[] doConditionStmt(emisBusiness oBusiness, emisDb oDb, String sSQL,         boolean isScrollable, NodeList eCondition) throws Exception {    	oBusiness.debug("do Condition");            	Object[] obj = new Object[2];       List value= new ArrayList();       ArrayList _oConditionList = new ArrayList();       int _iLength = eCondition.getLength();       for (int i = 0; i < _iLength; i++) {         Element _eCondition = (Element) eCondition.item(i);         oBusiness.debug("Condition" + i);         emisCondition _oCondition = new emisCondition(oBusiness, _eCondition);         sSQL = _oCondition.replace(sSQL);         _oConditionList.add(_oCondition);       }       oBusiness.debug("Get " + _oConditionList.size() + " condition");       try {         int _nSetter = 1;                  if (isScrollable) {           oDb.prepareStmt(sSQL, ResultSet.TYPE_SCROLL_INSENSITIVE,               ResultSet.CONCUR_READ_ONLY);         } else {           oDb.prepareStmt(sSQL);         }                  int _iSize = _oConditionList.size();         for (int i = 0; i < _iSize; i++) {           emisCondition _oCondition = (emisCondition) _oConditionList.get(i);           _nSetter = _oCondition.setSQL(oDb, _nSetter, value);         }          obj[0]=sSQL;          obj[1]=value;       } catch (Exception ignore) {         oBusiness.debug("[emisCondition] doCondition:" + ignore.getMessage());       }        return obj;     }  /**   * 這邊現在是給 emisDatabase 用的   * @param oBusiness   * @param eCondition   * @throws Exception   */  public emisCondition(emisBusiness oBusiness, Element eCondition) throws Exception {    oBusiness_ = oBusiness;    oRequest_ = oBusiness.getRequest();    sReplace_ = eCondition.getAttribute("replace");    if (sReplace_ == null) // 不知道要換哪個字串      throw new Exception("[emisCondition] unknow replace");    sReplace_ = "%" + sReplace_ + "%";    NodeList _eCondList = emisXMLUtl.getNodeList(eCondition, "cond");    int _iLength = _eCondList.getLength();    for (int i = 0; i < _iLength; i++) {      Element _eCond = (Element) _eCondList.item(i);      try {        emisCond _oCond = new emisCond(oBusiness_, _eCond);        oCondList_.add(_oCond);      } catch (Exception ignore) {        // 可能設定不全或 condition 不合 (exists)        oBusiness.debug(ignore.getMessage());      }    }  }  /**   * <cond replace=".../>與%TODAY_DATE%等變數: 替換SQL.   * @param sSQL   * @return   */  public String replace(String sSQL) {    StringBuffer _oSQLBuf = new StringBuffer();    emisCond _oPrevCond = null;    int _iSize = oCondList_.size();    for (int i = 0; i < _iSize; i++) {      emisCond _oCond = (emisCond) oCondList_.get(i);        //_oCond.      if (_oPrevCond != null) {        _oSQLBuf.append(_oPrevCond.getRelation());      }      _oSQLBuf.append(" " + _oCond.toString() + " ");      _oPrevCond = _oCond;    }    if (sSQL == null) return sSQL;    int idx = sSQL.indexOf(sReplace_);    if (idx != -1) {      sSQL = emisUtil.stringReplace(sSQL, sReplace_, _oSQLBuf.toString(), "a");    }    // 轉換%TODAY_DATE%, %TODAY_TIME%, 固定為西元格式, 時間沒有冒號    sSQL = replaceSystemDate(sSQL);    sSQL = replaceSystemTime(sSQL);    return sSQL;  }  /**   * 將%TODAY_DATE%替換成系統日期, 西元格式.   * @param sSQL   * @return   */  private static String replaceSystemDate(String sSQL) {    return replaceSystemVariables(sSQL, "%TODAY_DATE%", emisUtil.todayDateAD());  }  /**   * 將%TODAY_TIME%替換成系統時間(hhmmss), 沒有冒號.   * @param sSQL   * @return   */  private static String replaceSystemTime(String sSQL) {    return replaceSystemVariables(sSQL, "%TODAY_TIME%", emisUtil.todayTimeS());  }  /**   * 替換系統變數, 目前只有%TODAY_DATE%, %TODAY_TIME%.   * @param sSQL   * @param sVariable 變數字串   * @param sValue 要換成什麼   * @return   */  private static String replaceSystemVariables(String sSQL, String sVariable,      String sValue) {    int idx = sSQL.indexOf(sVariable);    if (idx != -1) {      sSQL = emisUtil.stringReplace(sSQL, sVariable, sValue, "ia");    }    return sSQL;  }   /**   *  傳入開始的 index      *   * @param nCounter   * @throws Exception   */  public void setSQL2ReportSql(int nCounter,List parem) throws Exception {    int _iSize = oCondList_.size();    for (int i = 0; i < _iSize; i++) {      emisCond _oCond = (emisCond) oCondList_.get(i);      int _iSize2 = _oCond.getParameterSize();      for (int j = 0; j < _iSize2; j++) {        String _sParameterName = _oCond.getParameter(j);        String sValue = oRequest_.getParameter(_sParameterName);        parem.add(sValue);      }    }  }  /**   *  傳入開始的 index   *  return new Index   *   * @param oDb   * @param nCounter   * @return   * @throws Exception   */  public int setSQL(emisDb oDb, int nCounter,List param) throws Exception {    int _iSize = oCondList_.size();    for (int i = 0; i < _iSize; i++) {      emisCond _oCond = (emisCond) oCondList_.get(i);      String _sSetType = _oCond.getSetType();      int _nSQLType = _oCond.getType();      ServletContext oContext = oBusiness_.getContext();      int _iSize2 = _oCond.getParameterSize();      for (int j = 0; j < _iSize2; j++) {        String _sParameterName = _oCond.getParameter(j);        String sValue = oRequest_.getParameter(_sParameterName);        if ("pname".equalsIgnoreCase(_sSetType)) {          emisDatabase.setPname(oContext, oDb, _nSQLType, sValue, nCounter);          oBusiness_.debug("set PName" + nCounter + "=" + sValue);        } else {          emisDatabase.setWhere(oContext, oDb, _nSQLType, sValue, nCounter);          oBusiness_.debug("set Where" + nCounter + "=" + sValue);        }         param.add(sValue);        nCounter++;      }    }    return nCounter;  }    /**   *  傳入開始的 index   *  return new Index   *   * @param oDb   * @param nCounter   * @return   * @throws Exception   */  public int setSQL(emisDb oDb, int nCounter) throws Exception {    int _iSize = oCondList_.size();    for (int i = 0; i < _iSize; i++) {      emisCond _oCond = (emisCond) oCondList_.get(i);      String _sSetType = _oCond.getSetType();      int _nSQLType = _oCond.getType();      ServletContext oContext = oBusiness_.getContext();      int _iSize2 = _oCond.getParameterSize();      for (int j = 0; j < _iSize2; j++) {        String _sParameterName = _oCond.getParameter(j);        String sValue = oRequest_.getParameter(_sParameterName);        if ("pname".equalsIgnoreCase(_sSetType)) {          emisDatabase.setPname(oContext, oDb, _nSQLType, sValue, nCounter);          oBusiness_.debug("set PName" + nCounter + "=" + sValue);        } else {          emisDatabase.setWhere(oContext, oDb, _nSQLType, sValue, nCounter);          oBusiness_.debug("set Where" + nCounter + "=" + sValue);        }        nCounter++;      }    }    return nCounter;  }}