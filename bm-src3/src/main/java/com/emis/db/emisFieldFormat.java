/* $Id: emisFieldFormat.java 4 2015-05-27 08:13:47Z andy.he $

 *

 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.

 */

package com.emis.db;



import com.emis.trace.emisError;

import com.emis.trace.emisTracer;



import javax.servlet.ServletContext;

import java.sql.SQLException;

import java.util.HashMap;

import java.util.Iterator;



/**

 * 取出FieldFormat table的資料值.

 * @author Jerry

 * @version 2004/08/09

 * @version 2004/08/16 Jerry: 舊版本無FD_LEFTZERO欄位會出現錯誤

 * @version 2004/08/20 Jerry: 修正FD_LEFTZERO不會被存入emisFieldFormatBean的錯誤

 * @version 2004/08/23 1.6 Jerry: when getBean() get null, throw NoSuchFieldException

 * @version 2004/08/26 1.7 Jerry: static hmProperties_ will cause the others webapps

 *                                share the same one values
 *
 * Track+[13130] tommer.xie 2009/08/12 (v200908)儲存後重啟Resin 改用重載 FIELDFORMAT 的方式

 */

public class emisFieldFormat extends emisAbstractCacheTable {

  /** 識別字串 */

  public static final String STR_EMIS_FIELDFORMAT = "com.emis.db.emisFieldFormat";

  private HashMap hmProperties_;



  /**

   * Constructor. 在emisAbstractCacheTable建立起來.

   * @param oContext

   * @throws Exception

   */

  public emisFieldFormat(ServletContext oContext) throws Exception {

    super(oContext, STR_EMIS_FIELDFORMAT);

    // System.out.println("app=" + oContext + ",map=" + hmProperties_);

  }

 /**
   * 新增此方法，??在emisReloader中才能?到。  add by tommer.xie  2009/08/11
   * @param application
   * @throws Exception
   */
  public static void reload(ServletContext application) throws Exception {
    emisFieldFormat _oFieldFormat = emisFieldFormat.getInstance(application);
    _oFieldFormat.reload();
  }

  /**

   * 第一次啟動後使用: 重新載入, 尚有一個供網頁使用的reload寫在emisAbstractCacheTable.

   *

   * @throws Exception

   */

  protected synchronized void reload() throws Exception {

    emisDb oDb = emisDb.getInstance(oContext_);

    try {

      hmProperties_ = new HashMap();

      oDb.setDescription("system: get FieldFormat");

      oDb.executeQuery("SELECT * FROM FieldFormat");

      emisFieldFormatBean bean = null;

      while (oDb.next()) {

        String _sName = oDb.getString("FD_TYPE");

        int _iMaxLen = oDb.getInt("FD_MAXLEN");

        String _sValid = oDb.getString("FD_VALIDATION");

        String _sPicture = oDb.getString("FD_PICTURE");

        String _sLeftZero = null;

        //[1175] Jacky

        int _iZero = _iMaxLen;

         int _iSize = _iMaxLen;



        try {  // 舊版本都沒有FD_LEFTZERO欄位

          _sLeftZero = oDb.getString("FD_LEFTZERO");

        } catch (SQLException e) {}



        try {  // 舊版本都沒有FD_ZEROCNT欄位 [1175] Jacky

          _iZero = oDb.getInt("FD_ZEROCNT");
          _iSize = oDb.getInt("FD_SIZE");

          if (_iZero ==0 )

            _iZero = _iMaxLen;

           if (_iSize ==0 )

            _iSize = _iMaxLen;

        } catch (SQLException e) {

          _iZero = _iMaxLen;
          _iSize = _iMaxLen;

        }

        bean = new emisFieldFormatBean(_sName, _iMaxLen, _sValid, _sPicture, _sLeftZero);

        //[1175]

        bean.setZeroCnt(_iZero);
        bean.setSize(_iSize);

        hmProperties_.put(_sName, bean);

      }

    } catch (Exception e) {

//      System.out.println("emisFieldFormat: " + e.getMessage());

      emisTracer.get(oContext_).sysError(this, emisError.ERR_SVROBJ_PROP_RESET,

          e.getMessage());

    } finally {

      oDb.close();

    }

  }



  /**

   * 取回emisFieldFormat的Instance.

   * @param oContext

   * @return

   * @throws Exception

   */

  public static emisFieldFormat getInstance(ServletContext oContext) throws Exception {

    emisFieldFormat _oFieldFormat = (emisFieldFormat)

        oContext.getAttribute(STR_EMIS_FIELDFORMAT);

    if (_oFieldFormat == null) {

      _oFieldFormat = new emisFieldFormat(oContext);

      //emisTracer.get(oContext).sysError(null, emisError.ERR_SVROBJ_NOT_BIND,

      //    "emisFieldFormat");

    }

    return _oFieldFormat;

  }



  /**

   * 以FD_TYPE傳回該記錄其他欄位的值.

   * @param sName

   * @return emisFieldFormatBean

   * @throws NoSuchFieldException

   */

  public synchronized emisFieldFormatBean getBean(String sName)

      throws NoSuchFieldException {

    emisFieldFormatBean bean = (emisFieldFormatBean) hmProperties_.get(sName);

    if (bean == null) {

      throw new NoSuchFieldException("FieldFormat資料表中未設定欄位[" + sName + "]");

    }

    return bean;

  }



  /**

   * 範例:emisFieldFormat.getInstance(application).get("S_NO", "VALIDATION");

   * @param sFD_TYPE FD_TYPE

   * @param sField MAXLEN, VALIDATION, PICTURE, LEFTZERO

   * @return

   * @throws Exception

   */

  public synchronized String get(String sFD_TYPE, String sField) throws Exception {

    emisFieldFormatBean bean = getBean(sFD_TYPE);

    if ("MAXLEN".equalsIgnoreCase(sField)) {

      return ""+bean.getMaxLen();

    } else if ("VALIDATION".equalsIgnoreCase(sField)) {

      return bean.getValidation();

    } else if ("PICTURE".equalsIgnoreCase(sField)) {

      return bean.getPicture();

    } else if ("LEFTZERO".equalsIgnoreCase(sField)) {

      return bean.getLeftZero();

    } else if ("ZEROCNT".equalsIgnoreCase(sField)) {

      //[1175]

      return ""+bean.getZeroCnt();

    }else if ("SIZE".equalsIgnoreCase(sField)) {

      return ""+bean.getSize();

    }

    return "";

  }



  /**

   * Dummy method.

   * @param sName

   * @return

   * @throws Exception

   */

  public synchronized String get(String sName) throws Exception {

    return null;

  }



  /**

   * 傳回迭代器.

   * @return

   */

  public Iterator getValues() {

    return hmProperties_.values().iterator();

  }

}

