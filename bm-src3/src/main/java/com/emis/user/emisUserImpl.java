/*
 * $Header: /repository/src3/src/com/emis/user/emisUserImpl.java,v 1.1.1.1 2005/10/14 12:43:17 andy Exp $
 *
 * Copyright (c) EMIS Corp.
 */
package com.emis.user;

import com.emis.db.emisDb;
import com.emis.server.emisServerFactory;
import com.emis.test.emisServletContext;
import com.emis.trace.emisTracer;

import javax.servlet.ServletContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * 實作 emisUser
 * for ePos
 * database available: Oracle,SQL2000
 */
public class emisUserImpl extends emisAbstractUser {
  private static boolean _DEBUG_ = false;

  // sExtraInfo="C,COMPANY_NO" or "U,USER_TYPE"
  public emisUserImpl(ServletContext oContext, String sStoreNo,
                      String sExtraInfo, String sUserID, String sPassWord, Boolean isCheckPasswd,
                      String sSessionId) throws Exception {
    super(oContext, sSessionId);

    emisDb oDb = emisDb.getInstance(oContext_);
    try {
      oDb.setDescription("system:getUser");
      // 有門市代碼
      String _sSQL = null;
      if (sStoreNo != null && !"".equals(sStoreNo)) {
        if (!sStoreNo.equals("-1")) {
          _sSQL = "SELECT * FROM USERS WHERE USERID=? AND S_NO=?";
          oDb.prepareStmt(_sSQL);
          oDb.setString(2,sStoreNo);
        } else { // 只用USERID來找, for Yes
          _sSQL = "SELECT * FROM USERS WHERE USERID=?";
          oDb.prepareStmt(_sSQL);
        }
      } else {
        _sSQL = "SELECT * FROM USERS WHERE USERID=? AND (S_NO is null or S_NO='') ";
        oDb.prepareStmt(_sSQL);
      }
      if (_DEBUG_) System.out.println("SQL=" + _sSQL);
      sUserID = sUserID.toUpperCase();
      oDb.setString(1, sUserID);
      oDb.prepareQuery();
      if (oDb.next()) {
        dbToProperty(oDb, oProp_);

        if (isCheckPasswd.booleanValue()) {
          String _sPasswd = getProperty("PASSWD");
          if (_sPasswd == null) throw new Exception("passwd is not set yet :" + sUserID);
          if (!_sPasswd.equals(sPassWord)) throw new Exception("password or account error:" + sUserID);
        }
      } else {
        throw new Exception("can't find User " + sUserID);
      }
      oMenuPermission_ = getMenuPermission(oDb, this.getID(), this.getGroups(), this.getSNo());

    } catch (Exception e) {
      //emisTracer.get(oContext_).sysError(this,emisError.ERR_USER_CREATE,e.getMessage());
      emisTracer.get(oContext_).info(this, e.getMessage());
      throw e;
    } finally {
      oDb.close();
    }
  }

  public String getMailAddr() {
    return getProperty("EMAIL");
  }

  public Properties getUserStoreInfo() throws Exception {
    String sSNo = getSNo();
    if ((sSNo == null) || "".equals(sSNo)) return null;
    emisDb oDb = emisDb.getInstance(oContext_);
    try {
      oDb.setDescription("system:getUserStoreInfo");
      oDb.prepareStmt("SELECT * FROM STORE WHERE S_NO=?");
      oDb.setString(1, sSNo);
      oDb.prepareQuery();
      if (oDb.next()) {
        Properties p = new Properties();
        dbToProperty(oDb, p);
        return p;
      } else {
        return null;
      }
    } finally {
      oDb.close();
      oDb = null;
    }
  }

  public Properties getUserInfo(String sUserId) throws Exception {
    if ((sUserId == null) || ("".equals(sUserId))) {
      throw new Exception("getUserInfo: null or empty UserId");
    }
    emisDb oDb = emisDb.getInstance(oContext_);
    try {
      oDb.setDescription("system:getUserInfo");
      oDb.prepareStmt("SELECT * FROM USERS WHERE USERID=?");
      sUserId = sUserId.toUpperCase();
      oDb.setString(1, sUserId);
      oDb.prepareQuery();
      if (oDb.next()) {
        Properties p = new Properties();
        int nColumnCnt = oDb.getColumnCount();
        for (int i = 1; i <= nColumnCnt; i++) {
          p.put(oDb.getColumnName(i), oDb.getString(i));
        }
        return p;
      } else {
        throw new Exception("can't find user:" + sUserId);
      }
    } finally {
      oDb.close();
      oDb = null;
    }
  }

  public int getUserType() {
    return -1; // 在 ePos 尚未定義
  }

  /**
  * 傳回使用者 KEY 值 Cliff 91.11.12
  */
  public String getStKey() {
    return "";
  }

  /**
  * 傳回使用者使用者區權限 Cliff 91.11.12
  */
  public String getRNo() throws Exception {
    try {
    } catch (Exception e) { }
    return "";
  }

  /**
  * 目的: 取得使用者區權限字串 For SQL Cliff 91.11.12
  */
  public String getRNoStr() throws Exception {
    try {
    } catch (Exception e) { }
    return "";
  }

  private emisMenuPermission getMenuPermission(emisDb oDb, String sUserId, String sGroupId, String sStoreNo) throws Exception {

    PreparedStatement pstmt = oDb.prepareStmt(
        "select distinct 1 as utype,u.keys,u.BTNADD,u.BTNUPD,u.BTNDEL,u.BTNRPT from Userrights u, Menus m where " +
        "u.USERID=? and u.Rights='Y'  and (u.S_NO is null or u.S_NO=?) and (u.KEYS=m.KEYS or m.MENU_TYPE is null)  " +
        "union " +
        "select distinct 2 as utype,u.keys,u.BTNADD,u.BTNUPD,u.BTNDEL,u.BTNRPT from Userrights u, Menus m where " +
        "u.USERGROUPS=?  and u.Rights='Y'  and (u.KEYS=m.KEYS or m.MENU_TYPE is null) " +
        "order by utype");

    try {
      pstmt.setString(1, sUserId);
      pstmt.setString(2, (sStoreNo == null) ? "":sStoreNo);
      pstmt.setString(3, (sGroupId == null) ? "":sGroupId);
      ResultSet rs = pstmt.executeQuery();
      return new emisMenuPermImpl(rs);
    } finally {
      pstmt.close();
    }
  }

  public static void main(String[] args) throws Exception {
    System.out.println("Initialization...");
    emisServletContext servlet = new emisServletContext();
    ServletContext _oContext = (ServletContext) servlet;
    System.out.println("After new servlet");
    emisServerFactory.createServer(servlet, "c:\\wwwroot\\yes", "c:\\resin\\yes.cfg", true);
    System.out.println("After createServer()");
    emisUserImpl _oUser1 = new emisUserImpl(_oContext,"-1","","00000004","pass",Boolean.FALSE,"Aaa");
    emisUserImpl _oUser2 = new emisUserImpl(_oContext,"000004","","00000004","pass",Boolean.FALSE,"Aaa");
    System.out.println("After run( )");
  }
}
