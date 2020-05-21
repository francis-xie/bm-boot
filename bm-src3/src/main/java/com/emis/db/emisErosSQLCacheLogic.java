package com.emis.db;

import com.emis.user.emisUser;

import javax.servlet.ServletContext;

/**
 * 修改歷程:
 *               09/08: REGION , REGION_STORE SQLCache 顯示時 以 / 符號分層顯示
 *    2004  /08/24 [636] Jacky 修正若區經理進來又沒負責門市  時會出現SQL錯誤
 *    2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
 */
public class emisErosSQLCacheLogic implements emisSQLCacheLogicInf {
  public String getSQL(ServletContext application,String sSQLName,emisUser oUser) throws Exception {
    if( sSQLName == null ) return null;

    if("COMPANY".equals(sSQLName))
      return getCompany(application,oUser);
    else if("STORE".equals(sSQLName))
      return getStore(application,oUser);
    else if("STORE_NOHQ".equals(sSQLName))
      return getStoreNoHQ(application,oUser);
    else if("STORE_FC".equals(sSQLName))
      return getStoreNoHQ(application,oUser);
    else if("REGION".equals(sSQLName))
      return getRegion(application,oUser);
    else if("REGION_STORE".equals(sSQLName))
      return getRegionStore(application,oUser);
    else
      return null;
  }

  /**
   *  公司別
   */
  private String getCompany(ServletContext application,emisUser oUser) throws Exception {
    String _sCNo = oUser.getCompanyNo();
    if( (_sCNo != null) && (!"".equals(_sCNo))) {
      emisDb oDb = emisDb.getInstance(application);
      try {
        oDb.prepareStmt("select COM_NAME_S from Company where COM_NO=? order by COM_NO");
        oDb.setString(1,_sCNo);
        oDb.prepareQuery();
        if( oDb.next()) {
          String _sName = oDb.getString(1);
          if( _sName == null )
            _sName = "";
          return "<option value=\""+_sCNo+"\" selected>"+_sCNo+" " + _sName+"</option>";
        }
      } finally {
        oDb.close();
      }
      return "<option value=\""+_sCNo+"\" selected>"+_sCNo+"</option>";
    }
    return null;
  }

  /**
   *  門市別
   */
  private String getStore(ServletContext application,emisUser oUser) throws Exception {
    // 取得 Login User 之身份
    int _iUserType=oUser.getUserType();
    if (_iUserType==0)
      return null;  // 總公司人員可顯示全部

    String _sRetStr="";
    String _sSNo = oUser.getSNo();
    String _sRNo = oUser.getRNoStr();

    if ("".equals(_sRNo))    {
      _sRNo="'xxNodata'";
    }

    emisDb oDb = emisDb.getInstance(application);
    try {
      if ( oUser.getUserType() ==1 ) {
        // 門市人員
        //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
        oDb.prepareStmt("select S_NO, S_NAME_S from Store where S_NO=?  and S_STATUS != '2'  order by S_NO");
        oDb.setString(1,_sSNo);
      } else {
        // 區經理
        //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
        oDb.prepareStmt("select S_NO, S_NAME_S from Store where R_NO in (" + _sRNo + ")  and S_STATUS != '2' order by S_NO");
      }
      oDb.prepareQuery();

      // 組 Option String
      _sRetStr = compOptionStr(oDb, "S_NO", "S_NAME_S");
    } finally {
      oDb.close();
    }
    return _sRetStr;
  }

  /**
   *  門市別-無總公司門市
   */
  private String getStoreNoHQ(ServletContext application,emisUser oUser) throws Exception {
    // 取得 Login User 之身份
    int _iUserType=oUser.getUserType();
    if (_iUserType==0)
      return null;  // 總公司人員可顯示全部

    String _sRetStr="";
    String _sSNo = oUser.getSNo();
    String _sRNo = oUser.getRNoStr();
    emisDb oDb = emisDb.getInstance(application);
    try {
      if ( oUser.getUserType() == 1    ) {
        // 門市人員
        //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
        oDb.prepareStmt("select S_NO, S_NAME_S from Store where S_NO=? and S_ATTRIB != '4' and S_STATUS != '2' order by S_NO");
        oDb.setString(1,_sSNo);
      } else {
        // 區經理
        //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
        oDb.prepareStmt("select S_NO, S_NAME_S from Store where  S_ATTRIB != '4' and S_STATUS != '2' and R_NO in (" + _sRNo + ")  order by S_NO");
      }
      oDb.prepareQuery();

      // 組 Option String
      _sRetStr = compOptionStr(oDb, "S_NO", "S_NAME_S");
    } finally {
      oDb.close();
    }
    return _sRetStr;
  }

  /**
   *  門市別-加盟門市
   */
  private String getStoreFC(ServletContext application,emisUser oUser) throws Exception {
    // 取得 Login User 之身份
    int _iUserType=oUser.getUserType();
    if (_iUserType==0)
      return null;  // 總公司人員可顯示全部

    String _sRetStr="";
    String _sSNo = oUser.getSNo();
    String _sRNo = oUser.getRNoStr();
    emisDb oDb = emisDb.getInstance(application);
    try {
      // 先取得門市屬性
      oDb.prepareStmt("select TD_NO from TAB_D where T_NO='S_ATTRIB' and T_NAME like '%加盟%'");
      oDb.prepareQuery();

      if ( oDb.next()) {
         String _sSAttrib = oDb.getString("TD_NO");

        if ( oUser.getUserType() == 1 ) {
          // 門市人員
          //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
          oDb.prepareStmt("select S_NO, S_NAME_S from Store where S_NO=? and S_ATTRIB=? and S_STATUS != '2'  order by S_NO");
          oDb.setString(1,_sSNo);
          oDb.setString(2,_sSAttrib);
        } else {
          // 區經理
          //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
          oDb.prepareStmt("select S_NO, S_NAME_S from Store where R_NO in (" + _sRNo + ") and S_ATTRIB=? and S_STATUS !='2' order by S_NO");
          oDb.setString(1,_sSAttrib);
        }
        oDb.prepareQuery();

        // 組 Option String
        _sRetStr = compOptionStr(oDb, "S_NO", "S_NAME_S");
      }
    } finally {
      oDb.close();
    }
    return _sRetStr;
  }

  /**
   *  區域別
   */
  private String getRegion(ServletContext application,emisUser oUser) throws Exception {
    // 取得 Login User 之身份
/*
    int _iUserType=oUser.getUserType();
    if (_iUserType==0)
      return null;  // 總公司人員可顯示全部

    String _sRetStr="";
    String _sRNo = oUser.getRNoStr();
    // 區經理
    if ( oUser.getUserType() == 2 ) {
      emisDb oDb = emisDb.getInstance(application);
      try {
        oDb.prepareStmt("select R_NO, (R_LEVEL1+'/'+R_LEVEL2) as R_LEVEL  ,  R_NAME_S  from Region where R_NO in (" + _sRNo + ") order by R_LEVEL1,R_LEVEL2");
        oDb.prepareQuery();

        // 組 Option String
        _sRetStr = compOptionStr(oDb , "R_NO","R_LEVEL", "R_NAME_S");
      } finally {
        oDb.close();
      }
    }
*/
    return getRegionStore(application,  oUser);
  }

  /**
   *  區域別-by身份
   */
  private String getRegionStore(ServletContext application,emisUser oUser) throws Exception {
    // 取得 Login User 之身份
    int _iUserType=oUser.getUserType();
    if (_iUserType==0)
      return null;  // 總公司人員可顯示全部

    String _sRetStr="";
    String _sRNo = oUser.getRNoStr();
    String _sSNo = oUser.getSNo();

    // 區經理, 門市人員(_iUserType=1)
    if ( _iUserType==2 || _iUserType==1) {
      emisDb oDb = emisDb.getInstance(application);
      String _sSQLStr = "";
      try {
        if ( _iUserType==2 )
          _sSQLStr = " select R_NO, (R_LEVEL1+'/'+R_LEVEL2) as R_LEVEL  ,  R_NAME_S    from Region where R_NO in (" + _sRNo + ") order by R_LEVEL1,R_LEVEL2";
        else
          //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
          _sSQLStr = " select R_NO, (R_LEVEL1+'/'+R_LEVEL2) as R_LEVEL  ,  R_NAME_S  from Region " +
                     "  where R_NO in (select top 1 R_NO from Store where S_NO='" + _sSNo + "'  and S_STATUS !='2' ) order by R_LEVEL1+R_LEVEL2";

        oDb.prepareStmt(_sSQLStr);
        oDb.prepareQuery();

        // 組 Option String
        _sRetStr = compOptionStr(oDb, "R_NO", "R_LEVEL" , "R_NAME_S");
      } finally {
        oDb.close();
      }
    }
    return _sRetStr;
  }


  /**
   *  傳回 Option String
   */
  private String compOptionStr(emisDb oDb, String sNo, String sName ) throws Exception {
    boolean _bFirst=true;
    String _sRetStr="";
    String _sNo, _sName;

    try {
      while (oDb.next()) {
        _sNo   = oDb.getString(sNo);
        _sName = oDb.getString(sName);
        if( _sNo == null ) _sNo = "";
        if( _sName == null ) _sName = "";

        if (_bFirst)
          _sRetStr=_sRetStr + "<option value=\""+_sNo+"\" selected>"+_sNo+" " + _sName+"</option>";
        else
          _sRetStr=_sRetStr + "<option value=\""+_sNo+"\">"+_sNo+" " + _sName+"</option>";
        _bFirst=false;
      }
      return _sRetStr;
    } finally {}
  }

  /**
   *  傳回 三層式 Option String
   */
  private String compOptionStr(emisDb oDb, String sNo, String sPrefix , String sName ) throws Exception {
    boolean _bFirst=true;
    String _sRetStr="";
    String _sNo, _sName , _sPrefix;

    try {
      while (oDb.next()) {
        _sNo   = oDb.getString(sNo);
        _sName = oDb.getString(sName);
        _sPrefix = oDb.getString(sPrefix);
        if( _sNo == null ) _sNo = "";
        if( _sName == null ) _sName = "";

        if (_bFirst)
          _sRetStr=_sRetStr + "<option value=\""+_sNo+"\" selected>"+_sPrefix+" " + _sName+"</option>";
        else
          _sRetStr=_sRetStr + "<option value=\""+_sNo+"\">"+_sPrefix+" " + _sName+"</option>";
        _bFirst=false;
      }
      return _sRetStr;
    } finally {}
  }

}
