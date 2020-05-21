// $Header: /repository/sme_gl/src/src3/com/emis/db/emisEsysDefVal.java,v 1.1 2009/06/12 03:51:00 sea.zhou Exp $
package com.emis.db;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

/**
 * 為了方便取得在資料庫 esys_def table 的 KEYS , FDNAME 和 DEF 的 Mapping.
 *
 * @author Ben
 * @version 2006
 */
public class emisEsysDefVal {
  /**
   * 識別字串
   */
  public static final String STR_EMIS_DEF = "com.emis.db.emisEsysDefVal";

  private HashMap hmProperties_;
  protected ServletContext oContext_;

  private emisEsysDefVal(ServletContext oContext) {
    hmProperties_ = new HashMap();
    oContext_ = oContext;
  }

  /**
   * 清空預設值.
   */
  public synchronized void clear() {
    hmProperties_.clear();
  }

  /**
   * 清空 KEYS
   *
   * @param sKeys
   */
  public synchronized void clear(String sKeys) {
    hmProperties_.remove(sKeys);
  }

  /**
   * 取出存於ServletContext內的Instance.
   *
   * @param oContext
   * @return
   * @throws Exception
   */
  public static emisEsysDefVal getInstance(ServletContext oContext) throws Exception {
    emisEsysDefVal oProp = (emisEsysDefVal) oContext.getAttribute(emisEsysDefVal.STR_EMIS_DEF);
    if (oProp == null) {
      oProp = new emisEsysDefVal(oContext);
      oContext.setAttribute(STR_EMIS_DEF, oProp);
    }
    return oProp;
  }

  /**
   * 返回值根據 sKeys ,sFieldName 從 table esys_def 取得 DEF
   *
   * @param sKeys
   * @param sFieldName
   * @return 返回 Key 對應的欄位名所對應的預設值,不存在的預設為空白。
   */
  public synchronized String get(String sKeys, String sFieldName, String sFunc) {
    Map _mDefVals = (HashMap) hmProperties_.get(sKeys);
    if (_mDefVals == null) {
      _mDefVals = reload(sKeys);
    }
    String _sValue = (String) _mDefVals.get(sFieldName + "@" + sFunc);
    if (_sValue == null) _sValue = "";
    return _sValue;
  }

  public synchronized String getScript(String sKeys) {
    return get(sKeys, "JAVASCRIPT", "JAVASCRIPT");
  }

  public synchronized String getScript(String sKeys, String sFunc) {
    return get(sKeys, "JAVASCRIPT", sFunc);
  }

  /**
   * 從新裝載一個作業的預設值。
   *
   * @param sKeys
   * @return 一個作業的預設值 Map
   */
  private Map reload(String sKeys) {
    Map _mDefVals = new HashMap();
    try {
      emisDb oDb = emisDb.getInstance(oContext_);
      try {
        oDb.setDescription("system: get esys_def ");
//        System.out.println(" select FDNAME , DEF from  Esys_def where KEYS = '" + sKeys + "' ");
        oDb.executeQuery(" select FDNAME , DEF , FUNC from  Esys_def where KEYS = '" + sKeys + "' " +
            " order by FUNC ");
        StringBuffer sbScriptString = new StringBuffer();
        StringBuffer sbFuncScriptString = null;
        String strFunc = null;
        sbScriptString.append(" var ").append(sKeys.toLowerCase()).append("Bean={ ");
        while (oDb.next()) {
          String _sName = oDb.getString("FDNAME");
          String _sFunc = oDb.getString("FUNC");
          String _sValue = oDb.getString("DEF");
          if (strFunc == null || !strFunc.equals(_sFunc)) {
            if (sbFuncScriptString != null) {
              sbFuncScriptString.append(" BEAN_TITLE : '").append(sKeys.toUpperCase()).append("' , ");
              sbFuncScriptString.append(" BEAN_FUNC : '").append(strFunc.toUpperCase()).append("' } ; ");
              _mDefVals.put("JAVASCRIPT@" + strFunc, sbFuncScriptString.toString());
            }
            strFunc = _sFunc;
            sbFuncScriptString = new StringBuffer();
            sbFuncScriptString.append(" var ").append(sKeys.toLowerCase()).append("Bean={ ");
          }
          _mDefVals.put(_sName + "@" + _sFunc, _sValue);
          sbScriptString.append(_sName).append(" : '").append(_sValue).append("' , ");
          sbFuncScriptString.append(_sName).append(" : '").append(_sValue).append("' , ");
        }
        if (sbFuncScriptString != null) {
          sbFuncScriptString.append(" BEAN_TITLE : '").append(sKeys.toUpperCase()).append("' , ");
          sbFuncScriptString.append(" BEAN_FUNC : '").append(strFunc.toUpperCase()).append("' } ; ");
          _mDefVals.put("JAVASCRIPT@" + strFunc, sbFuncScriptString.toString());
        }
        sbScriptString.append(" BEAN_TITLE : '").append(sKeys.toUpperCase()).append("' } ; ");
        _mDefVals.put("JAVASCRIPT@JAVASCRIPT", sbScriptString.toString());
        hmProperties_.put(sKeys, _mDefVals);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        oDb.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return _mDefVals;
  }

}
