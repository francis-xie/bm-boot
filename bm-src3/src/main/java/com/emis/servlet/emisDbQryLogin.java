/* $Id: emisDbQryLogin.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.servlet;

import com.emis.db.emisDb;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;

/**
 * 供jsp/dev/database/db.jsp使用的密碼驗證類別.
 *
 * @author Jerry 2004/8/9 下午 06:20:56
 * @version 1.0
 */
public class emisDbQryLogin {
  public static boolean login(ServletContext application, String sPasswd) {
    boolean _isOK = false;
    emisDb _oDb = null;
    try {
      //- emisUser _oUser = emisCertFactory.getUser(application, request);
      if (sPasswd == null || "".equals(sPasswd)) {
        return false;
      }
      StringBuffer _sbValidPasswd = new StringBuffer();
      _oDb = emisDb.getInstance(application);
      _oDb.prepareStmt("select PASSWD from Users where USERID='EMIS' or USERID='ROOT' order by USERID");
      _oDb.prepareQuery();
      if (_oDb.next()) {
        // 密碼是 root 的密碼 + 年度 + 月份(A~L)
        _sbValidPasswd.append(_oDb.getString("PASSWD"));
        java.util.Date _oDate = new java.util.Date();
        String _sYear = emisUtil.formatDateTime("%y%M", _oDate);
        String _sMonth = _sYear.substring(4);
        int _iMonth = emisUtil.parseInt(_sMonth) + 64;
        _sMonth = "" + Character.toUpperCase((char) _iMonth);
        _sYear = _sYear.substring(3, 4);
        _sbValidPasswd.append(_sYear).append(_sMonth);
        //out.println("password=" + _sbValidPasswd.toString());
        if (!sPasswd.equals(_sbValidPasswd.toString())) {
          return false;
        }
      }
      _isOK = true;
    } catch (Exception e) {
      System.out.println("emisQryLogin: " + e.getMessage());
    } finally {
      if (_oDb != null)
        _oDb.close();
    }
    return _isOK;
  }
}
