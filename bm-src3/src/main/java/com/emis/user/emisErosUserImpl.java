/* $Id: emisErosUserImpl.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.user;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.trace.emisTracer;

import javax.servlet.ServletContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * User實作物件.
 *
 * @version 09/08: 修改取的R_NO時原本Int型態 改為varchar
 * @version 2004/07/23 Jacky 修正區域權限無法抓到子區域的問題
 * @version 2004/08/25 Jerry: refactor; add comments; enhance performance
 */
public class emisErosUserImpl extends emisAbstractUser implements emisUser {
  private String sUserId_ = "";
  private String sUserName_ = "";
  private String sSNo_ = "";
  private String sCompanyNo_ = "";
  private String sStKey_ = "";   // 使用者 KEY 值
  private String sRNo_ = "";     // 使用者區權限
  private String sRNoStr_ = "";  // 使用者區權限字串 For SQL
  private String sBuyerKey_ = ""; //主採購員
  private float fBuyerAvaliable_ = 0; //採購員可用採購額度
  private float fBuyerLimit_ = 0; //採購員授權額度
  private boolean isRegionManager_ = false; // 是否為區經理
  private boolean isStoreManager_ = false; // 是不為店長
  private Properties propStore_ = null;  // 門市資料

  /**
   * Constructor.
   *
   * @param oContext
   * @param sSNo
   * @param sExtraInfo
   * @param sUserId
   * @param sPassWord
   * @param isCheckPasswd
   * @param sSessionId
   * @throws Exception
   */
  public emisErosUserImpl(ServletContext oContext, String sSNo,
      String sExtraInfo, String sUserId, String sPassWord, Boolean isCheckPasswd,
      String sSessionId) throws Exception {
    super(oContext, sSessionId);

    emisDb oDb = emisDb.getInstance(oContext_);
    try {
      oDb.setDescription("emisErorUserImpl:constructor");
      if ((sSNo != null) && (!"".equals(sSNo))) {
        // 有門市代碼
        if (!sSNo.equals("-1")) {
          oDb.prepareStmt("select * from Users where USERID=? and S_NO=?");
          oDb.setString(2, sSNo);
        } else { // 只用 USERID 來找
          oDb.prepareStmt("select * from Users where USERID=?");
        }
      } else {
        oDb.prepareStmt("select * from Users where USERID=? and (S_NO is null or S_NO='') ");
      }

      sUserId_ = sUserId.toUpperCase();
      oDb.setString(1, sUserId_);
      oDb.prepareQuery();
      if (oDb.next()) {
        dbToProperty(oDb, oProp_);  // 將資料表的欄位值轉成Properties值.

        if (isCheckPasswd.booleanValue()) {
          String _sPasswd = getProperty("PASSWD");
          if (_sPasswd == null) throw new Exception("passwd is not set yet :" + sUserId_);
          if (!_sPasswd.equals(sPassWord)) throw new Exception("password or account error:" + sUserId_);
        }
      } else {
        throw new Exception("can't find User: " + sUserId_);
      }

      // 取得 SNo
      this.sCompanyNo_ = oProp_.getProperty("COM_NO");

      // 取得 SNo
      sSNo_ = getSNo();

      // 取得使用者 KEY 值
      sStKey_ = getStKey();

      // 取得區權限 RNo && 判斷是否為店長
      if (this.sSNo_ != null && !"".equals(this.sSNo_)) {
        try {
          propStore_ = getUserStoreInfo();
          this.sRNo_ = propStore_.getProperty("R_NO");
          String _sStoreManager = propStore_.getProperty("S_LEADER");
          sCompanyNo_ = propStore_.getProperty("COM_NO");   // 取得公司編號
          if (_sStoreManager.equalsIgnoreCase(sStKey_)) {
            isStoreManager_ = true;
          }
        } catch (Exception e) {
          System.err.println("emisErosUserImpl.constructor: " + e.getMessage());
        }
      }

      sUserName_ = oProp_.getProperty("USERNAME");
      if (sStKey_ != null && !"".equals(sStKey_)) {
        getUserNameFromStaff(oDb);  // 由 ST_KEY 取得員工姓名
        getRegionSettings(oDb);  // 取得區權限字串 RNoStr
      }

      //由 Emisprop Table 取得目前作業會計年度 (??SYS_CLS_YEAR), 加到 CLS_YEAR Attribute 內
      try {
        emisProp prop = emisProp.getInstance(oContext_);
        String sCLS_YEAR = prop.get( this.sCompanyNo_ + "SYS_CLS_YEAR");
//      oDb.prepareStmt("select VALUE from Emisprop where NAME=? ");
//      oDb.setString(1, this.sCompanyNo_ + "SYS_CLS_YEAR");
//      oDb.prepareQuery();
//      if (oDb.next()) {
        if (sCLS_YEAR != null) {
          // String sCLS_YEAR = oDb.getString("VALUE");
          this.setAttribute("CLS_YEAR", sCLS_YEAR);
        }
      } catch (Exception e) {
        System.err.println("emisErosUserImpl.constructor: " + e.getMessage());
      }
      this.setAttribute("CHG_YEAR", "N");

      // 取得 Menu Permission
      oMenuPermission_ = getMenuPermission(oDb, this.getID());

    } catch (Exception e) {
      //emisTracer.get(oContext_).sysError(this,emisError.ERR_USER_CREATE,e.getMessage());
      emisTracer.get(oContext_).info(this, e.getMessage());
      throw e;
    } finally {
      oDb.close();
    }
  }

  /**
   * 取得區域設定.
   *
   * @param oDb
   * @throws SQLException
   */
  private void getRegionSettings(emisDb oDb) throws SQLException {
    String _sRNoStr = "";
    String _sWhere = "";  // SQL的where條件
    int _iLevel = 0;
    ArrayList _alLevels = new ArrayList();
    oDb.setDescription("emisErosUserImpl:getRNo");
    oDb.prepareStmt("select * from Region with (nolock) where R_LEADER=? " +
        " or R_LEVEL1  in (select R_LEVEL1 from Region where R_LEADER=? " +
        " and R_LEVEL2='00')");

    oDb.setString(1, sStKey_);
    oDb.setString(2, sStKey_);
    ResultSet _oRS = oDb.prepareQuery();

    //Jerry 2004/08/25 Regin表中有幾個R_LEVEL開頭的欄位; 取代原先用catch處理的方法
    int _iLevelCount = getLevelCount(_oRS);

    while (oDb.next()) {
      this.isRegionManager_ = true;  // 是區經理
      _iLevel = 0;

      //增加取得區域階層的代碼 若階層代碼的欄位有問題則直接跳出迴圈不處理
      sRNo_ = sRNo_ + ("".equals(sRNo_) ? "" : ",") + oDb.getString("R_NO");
      StringBuffer _sbRNoStr = new StringBuffer();
      for (int i = 1; i <= _iLevelCount; i++) {
        String _sTmpStr = oDb.getString("R_LEVEL" + Integer.toString(i));
        if (_sTmpStr != null && !"".equals(_sTmpStr) && (i == 1 || Integer.parseInt(_sTmpStr) != 0)) {
          _sbRNoStr.append(_sTmpStr);
        }
        _iLevel++;
      }
      _sRNoStr = _sbRNoStr.toString();

      if (_sRNoStr != null && !"".equals(_sRNoStr)) {
        _alLevels.add(_sRNoStr);
      }
    }

    int _iSize = _alLevels.size();
    if (_iSize > 0) {
      //組出LEVEL數字串    "R_LEVEL1+R_LEVEL2+....."
      String _sLevelString = null;
      StringBuffer _sbLevel = new StringBuffer();
      for (int i = 1; i <= _iLevel; i++) {
        _sbLevel.append(i==1 ? "" : " + ")
                .append(" R_LEVEL").append(Integer.toString(i));
//        _sLevelString += ("".equals(_sLevelString) ? "" : " + ")
//            + " R_LEVEL" + Integer.toString(i + 1);
      }
      _sLevelString = _sbLevel.toString();

      //組出SQL 條件 where R_LEVEL1+R_LEVEL2+...... like 'XXXXX%'  or R_LEVEL1+R_LEVEL2+.... like ''AAAAA%'
      for (int i = 0; i <= _iSize - 1; i++) {
        String _sLevel = (String) _alLevels.get(i);
        _sWhere += (i==0 ? " where " : " or ")
            + _sLevelString + " like '" + _sLevel + "%' ";
      }

      //依據條件找尋該階層下的所有區域代碼
      oDb.prepareStmt("select R_NO from Region with (nolock) " + _sWhere);
      oDb.prepareQuery();
      while (oDb.next()) {
        sRNoStr_ = sRNoStr_ + ("".equals(sRNoStr_) ? "" : ",") + oDb.getString("R_NO");
      }
    }
    //依據取得登錄者的採購員資料
    oDb.prepareStmt("select * from buyers where B_NO=? ");
    oDb.setString(1, sStKey_);
    oDb.prepareQuery();
    if (oDb.next()) {
      sBuyerKey_ = oDb.getString("B_NO");
      fBuyerAvaliable_ = oDb.getFloat("B_AVAILABLE");
      fBuyerLimit_ = oDb.getFloat("B_LIMIT");
    }
  }

  /**
   * 由欄位結構算出R_LEVEL欄位共有幾個.
   *
   * @param rs
   * @return 幾個R_LEVEL開頭的欄位(R_LEVEL1..R_LEVEL99)
   */
  private int getLevelCount(ResultSet rs) {
    int _iCount = 0;
    try {
      ResultSetMetaData meta = rs.getMetaData();
      for (int i = 1; i <= meta.getColumnCount(); i++) {
        String _sName = meta.getColumnName(i);
        if (_sName.startsWith("R_LEVEL")) {
          _iCount++;
        }
      }
    } catch (SQLException e) {
      System.err.println("emisErosUserImpl:getLevelCount: " + e.getMessage());
    }
    return _iCount;
  }

  /**
   * 由 Staff 取出使用者姓名.
   *
   * @param oDb
   * @throws SQLException
   */
  private void getUserNameFromStaff(emisDb oDb) throws SQLException {
    oDb.setDescription("emisErorUserImpl:getStaff");
    oDb.prepareStmt("select ST_NAME from Staff with (nolock) where ST_KEY=?");
    oDb.setString(1, sStKey_);
    oDb.prepareQuery();
    if (oDb.next()) sUserName_ = oDb.getString("ST_NAME");
  }

  /**
   * 取得使用者姓名
   */
  public String getName() {
    return sUserName_;
  }

  /**
   * 目的: 取得 E-Mail Address
   */
  public String getMailAddr() {
    return getProperty("EMAIL");
  }

  /**
   * 目的: 取得公司編號
   */
  public String getCompanyNo() {
    return sCompanyNo_;
  }

  /**
   * 取得使用者資訊.
   *
   * @param sUserId
   * @return Users into Properties
   * @throws Exception
   */
  public Properties getUserInfo(String sUserId) throws Exception {
    if ((sUserId == null) || ("".equals(sUserId))) {
      throw new Exception("emisErorUserImpl.getUserInfo: null or empty UserId");
    }
    emisDb oDb = emisDb.getInstance(oContext_);
    try {
      oDb.setDescription("emisErorUserImpl:getUserInfo");
      oDb.prepareStmt("select * from Users with (nolock) where USERID=? and S_NO=?");
      sUserId = sUserId.toUpperCase();
      oDb.setString(1, sUserId);
      oDb.setString(2, sSNo_);
      oDb.prepareQuery();
      if (oDb.next()) {
        Properties p = new Properties();
        dbToProperty(oDb, p);
        return p;
      } else {
        throw new Exception("emisErorUserImpl.getUserInfo: can't find user:" +
            sUserId + ", S_NO=" + sSNo_);
      }
    } finally {
      oDb.close();
      oDb = null;
    }
  }

  /**
   * 目的: 取得使用者門市資訊
   */
  public Properties getUserStoreInfo() throws Exception {
    if ((sSNo_ == null) || "".equals(sSNo_)) return null;

    emisDb oDb = emisDb.getInstance(oContext_);
    try {
      oDb.setDescription("emisErorUserImpl:getUserStoreInfo");
      oDb.prepareStmt("select * from Store with (nolock) where S_NO=?");
      oDb.setString(1, sSNo_);
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

  /**
   * 目的: 取得使用者 KEY 值
   */
  public String getStKey() {
    return getProperty("ST_KEY");
  }

  /**
   * 目的: 取得使用者區權限
   */
  public String getRNo() throws Exception {
    return sRNo_;
  }

  /**
   * 目的: 取得使用者區權限字串 For SQL
   */
  public String getRNoStr() throws Exception {
    return sRNoStr_;
  }

  /**
   * 目的: 取得 Login User 之身份
   * 0: 總公司人員
   * 1: 門市人員
   * 2: 區經理
   * -1: 其他
   */
  public int getUserType() {
    String _sUserType = this.getProperty("USER_TYPE");
    int _iUserType = -1;
    if (_sUserType == null || "".equals(_sUserType)) {
      if (isRegionManager_) {
        _iUserType = 2;   // 區經理
      } else {
        String _sCompareSno = null;
        try {
          emisProp _oProp = emisProp.getInstance(this.oContext_);
          _sCompareSno = _oProp.get("EROS_HEAD_STORE");
        } catch (Exception e) {
          e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        if (("".equals(sSNo_) || sSNo_ == null || sSNo_.equalsIgnoreCase(_sCompareSno)))
          _iUserType = 0;   // 總公司人員
        else
          _iUserType = 1;   // 門市人員
      }
    } else {
      _iUserType = Integer.parseInt(_sUserType);
      if (_iUserType > 2 || _iUserType < 0) {
        _iUserType = -1;
      }
    }
    return _iUserType;
  }

  /**
   * 目的: 是否為店長
   */
  public boolean isStoreManager() throws Exception {
    return isStoreManager_;
  }

  /**
   * 目的: 取得使用者權限
   */
  private emisMenuPermission getMenuPermission(emisDb oDb, String sUserId) throws Exception {
    emisProp oProp_ = emisProp.getInstance(oContext_);
    // 以 EmisProp Table EPOS_MENUS_TYPE 設定
    // 是否使用Function設定Button權限
    String _sFuncButton = oProp_.get("EPOS_FUNC_BUTTON");

    if (_sFuncButton == null || "".equals(_sFuncButton)) {
      // 舊的 Button Right 設定
      PreparedStatement pstmt = oDb.prepareStmt("select distinct 1 as utype,u.keys,u.BTNADD,u.BTNUPD,u.BTNDEL,u.BTNRPT from Userrights u, Menus m where " +
          "       u.USERID=? and u.Rights='Y'  and (u.S_NO is null or u.S_NO=?) and (u.KEYS=m.KEYS or m.MENU_TYPE is null)  " +
          "union " +
          "select distinct 2 as utype,u.keys,u.BTNADD,u.BTNUPD,u.BTNDEL,u.BTNRPT from Userrights u, Menus m where " +
          "       u.USERGROUPS=?  and u.Rights='Y'  and (u.KEYS=m.KEYS or m.MENU_TYPE is null) " +
          "       order by utype");

      try {
        String sGroupId = this.getGroups();
        pstmt.setString(1, sUserId);
        pstmt.setString(2, (sSNo_ == null) ? "" : sSNo_);
        pstmt.setString(3, (sGroupId == null) ? "" : sGroupId);
        ResultSet rs = pstmt.executeQuery();
        return new emisMenuPermImpl(rs);
      } finally {
        pstmt.close();
      }
    } else {
      // 新的 Button Right 設定
      return new emisErosMenuPermImpl(oDb, sUserId);
    }
  }

  /**
   * 取得採購員員工代碼
   *
   * @return
   */
  public String getBuyerKey() {
    return sBuyerKey_;
  }

  /**
   * 取得可用額度
   *
   * @return
   */
  public float getBuyerAvaliable() {
    return fBuyerAvaliable_;
  }

  /**
   * 取得授權額度
   *
   * @return
   */
  public float getBuyerLimit() {
    return fBuyerLimit_;
  }

  /**
   * 傳回門市代號  若為總公司人員 則些檢查 emisProp 資料表內有沒有 定義EPOS_HEAD_STORE變數
   */
  public String getSNo() {
    String _sStoreNo = getProperty("S_NO");
    if (this.getUserType() == 0 && (_sStoreNo == null || "".equals(_sStoreNo))) {
      try {
        emisProp _oProp = emisProp.getInstance(this.oContext_);
        String _ssStoreNo = _oProp.get("EROS_HEAD_STORE");
        if (_ssStoreNo != null && !"".equals(_ssStoreNo)) {
          _sStoreNo = _ssStoreNo;
        }
      } catch (Exception e) {
        e.printStackTrace();  //To change body of catch statement use Options | File Templates.
      }
    }
    return _sStoreNo;
  }
}
