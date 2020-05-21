/* $Id: emisAbstractCacheTable.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.db;

import com.emis.trace.emisTracer;
import com.emis.trace.emisError;

import javax.servlet.ServletContext;

/**
 * Cachable for Database table. emisProp與emisFieldFormat.
 *
 * @author Jerry 2004/8/9 上午 10:33:22
 * @version 1.0
 */
public abstract class emisAbstractCacheTable {
  /** ServletContext */
  protected ServletContext oContext_;

  /**
   * 由實作類別傳入ServletContext與類別全名, 以將建立好的物件存入ServletContext.
   * @param oContext
   * @param sClassName
   * @throws Exception
   */
  protected emisAbstractCacheTable(ServletContext oContext, String sClassName)
      throws Exception {
    oContext_ = oContext;

    if (oContext.getAttribute(sClassName) != null) {
      emisTracer.get(oContext).sysError(this, emisError.ERR_SVROBJ_DUPLICATE,
          sClassName);
    }
    oContext_.setAttribute(sClassName, this);
    reload();  // 主邏輯
  }

  /** 主邏輯; 由下層類別實作 */
  abstract void reload() throws Exception;
  /** 取出需要的屬性值 (emisProp用) */
  abstract String get(String sName) throws Exception;

  /**
   * Dummy method.
   * @param oContext
   * @param sName
   * @return
   */
  public static emisAbstractCacheTable getInstance(ServletContext oContext,String sName) {
    return null;
  }

  /**
   * 網頁reload使用.
   *
   * @param application
   * @throws Exception
   */
  public static void reload(ServletContext application) throws Exception {
    emisProp.getInstance(application).reload();
  }
}
