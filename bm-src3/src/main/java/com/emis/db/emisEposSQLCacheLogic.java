package com.emis.db;

import com.emis.user.emisUser;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

/**
 * 修改歷程:
 *               09/08: REGION , REGION_STORE SQLCache 顯示時 以 / 符號分層顯示
 *    2004  /08/24 [636] Jacky 修正若區經理進來又沒負責門市  時會出現SQL錯誤
 *    2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
 * Track+[13309] dana.gao 2009/09/16 調整user 以新UserType(5:供應商,7:旅行社)登錄時,門市選擇框無法正常顯示的問題.
 * Track+[14283] dana.gao 2010/01/25 調整user 以新UserType(5:供應商,7:旅行社)登錄時,區域選擇框無法正常顯示的問題.
 */
public class emisEposSQLCacheLogic implements emisSQLCacheLogicInf {
  public String getSQL(ServletContext application,String sSQLName,emisUser oUser) throws Exception {
    if( sSQLName == null ) return null;

    if("COMPANY".equals(sSQLName))
      return getCompany(application, oUser);
    // 增加查询直营(加盟店)和仓库(工厂)的SQLCache
    else if("STORE".equals(sSQLName) || "STORE_S".equals(sSQLName) || "STORE_W".equals(sSQLName))
      return getStore(application, oUser, sSQLName);
    else if("SM_STORE_SEL".equals(sSQLName))
      return getSM_Store(application, oUser);
    else if("STORE_NOHQ".equals(sSQLName))
      return getStoreNoHQ(application, oUser);
    else if("STORE_FC".equals(sSQLName))
      return getStoreNoHQ(application, oUser);
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
  private String getStore(ServletContext application,emisUser oUser, String sSQLName) throws Exception {
    // 取得 Login User 之身份
    int _iUserType=oUser.getUserType();
    if (_iUserType==0)
      return null;  // 總公司人員可顯示全部

    String _sRetStr="";
    emisDb oDb = emisDb.getInstance(application);
    try {
      // 取得用户门店列表
      oDb.prepareStmt("select S_NO, S_NAME_S from Store s where exists(select S_NO from vACL where USERID = ? and S_NO = s.S_NO)");
      oDb.setString(1, oUser.getID());
      oDb.prepareQuery();
      List<String> ustore  = new ArrayList<String>();
      while(oDb.next()){
        ustore.add(oDb.getString("S_NO"));
      }
      if(ustore.size() == 0){ //没有可访问的门店，返回空选项
        _sRetStr = "<option value='' selected></option>";
      } else {
        oDb.prepareStmt("SELECT * FROM "+emisSQLCache.SQLCACHETABLE+" WHERE SQLNAME=?");
        oDb.setString(1,sSQLName);
        oDb.prepareQuery();
        String sqlcmd  = "", sqlfmt = "" ;
        if(oDb.next()) {
          sqlcmd = oDb.getString("SQLCMD");
          sqlfmt = oDb.getString("SQLFMT");
        }
        oDb.executeQuery(sqlcmd);
        int _nColumnCount = oDb.getColumnCount();
        StringBuffer _oBuf = new StringBuffer();
        String _tmpFmt = sqlfmt;
        if(oUser.getUserType() != 1){ // 非门店人员，增加一个空的选项
          for(int i=1; i<= _nColumnCount ; i++) {
            _tmpFmt = emisUtil.stringReplace(_tmpFmt, "%" + i, "", "a");
          }
          _oBuf.append(_tmpFmt).append("\n");
        }
        while(oDb.next()) {
          if(ustore.contains(oDb.getString("S_NO"))){  // 属于用户门店则组到选项中
            _tmpFmt = sqlfmt;
            for(int i=1; i<= _nColumnCount ; i++) {
              String _sValue = oDb.getString(i);
              if( _sValue != null ) {
                // 將 sFmt 的 %1,%2 代換掉
                String _sReplace = "%" + i;
                _tmpFmt = emisUtil.stringReplace(_tmpFmt,_sReplace,_sValue,"a");
              }
            }
            _oBuf.append(_tmpFmt).append("\n");
          }
        }
        _sRetStr = _oBuf.toString();
      }
    } finally {
      oDb.close();
    }
    return _sRetStr;
  }

    /**
     *  門市別 ,用於購物中心，過濾了非自營專櫃的倉櫃
     */
    private String getSM_Store(ServletContext application,emisUser oUser) throws Exception {
        // 取得 Login User 之身份
        int _iUserType=oUser.getUserType();
        if (_iUserType==0)
            return null;  // 總公司人員可顯示全部

        String _sRetStr="";
        String _sSNo = oUser.getSNo();
        //String _sRNo = oUser.getRNoStr();
        String _sRNo = oUser.getRNoStr();

        if ("".equals(_sRNo))    {
            _sRNo="xxNodata";
        }

        emisDb oDb = emisDb.getInstance(application);
        try {
            if ( oUser.getUserType() ==1 ) {
                // 門市人員
                //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
                oDb.prepareStmt("select S_NO, S_NAME_S from Store where S_NO=?   order by S_NO");
                oDb.setString(1,_sSNo);
            } else if ( oUser.getUserType() ==2 ){
                // 區經理
                //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
                oDb.prepareStmt("select S_NO, S_NAME_S from Store where R_NO in (" + _sRNo + ")  and S_KIND!='1' and S_STATUS='1' order by S_NO");
            } else {
                String sSql_ = "select S_NO, S_NAME_S from Store";
                if ((_sSNo != null) && (!"".equals(_sSNo))) {
                    sSql_ = sSql_ + " where S_NO='" + _sSNo + "'";
                }
                sSql_ = sSql_ + " order by S_NO";
                oDb.prepareStmt(sSql_);
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
    //String _sRNo = oUser.getRNo();

    emisDb oDb = emisDb.getInstance(application);
    try {
      if ( oUser.getUserType() == 1    ) {
        // 門市人員
        //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
        oDb.prepareStmt("select S_NO, S_NAME_S from Store where S_NO=? and S_ATTRIB != '4'  order by S_NO");
        oDb.setString(1,_sSNo);
      } else {
        // 區經理
        //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
        oDb.prepareStmt("select S_NO, S_NAME_S from Store where  S_ATTRIB != '4'  and R_NO in (" + _sRNo + ")  order by S_NO");
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
    //String _sRNo = oUser.getRNo();
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
          oDb.prepareStmt("select S_NO, S_NAME_S from Store where S_NO=? and S_ATTRIB=?   order by S_NO");
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
    if (_iUserType == 0 ) // 總公司人員可顯示全部
      return null;

    String _sRetStr="";
    String _sRNo = oUser.getRNoStr();
    //String _sRNo = oUser.getRNo();
    String _sSNo = oUser.getSNo();
    /*
    // 區經理, 門市人員(_iUserType=1)
    if ( _iUserType==2 || _iUserType==1) {
      emisDb oDb = emisDb.getInstance(application);
      String _sSQLStr = "";
      try {
        if ( _iUserType==2 )
          _sSQLStr = " select R_NO,  R_NAME    from Region where R_NO in (" + _sRNo +") order by R_NO";
        else
          //2004/09/29 [995] Jacky 過濾門市Status ='2' 刪除的門市
          _sSQLStr = " select R_NO,  R_NAME  from Region " +
                     "  where R_NO in (select top 1 R_NO from Store where S_NO='" + _sSNo + "'  ) order by R_NO";

        oDb.prepareStmt(_sSQLStr);
        oDb.prepareQuery();

        // 組 Option String
        _sRetStr = compOptionStr(oDb, "R_NO","R_NAME");
      } finally {
        oDb.close();
      }
    }
    */
    String _sSQLStr = "";
    emisDb oDb = emisDb.getInstance(application);
    try {
      // 门店人员、区域人员、其他人员
      _sSQLStr = "select R_NO, R_NAME from Region r \n" +
          "where exists(select acl.* from vACL acl inner join Store s on s.S_NO = acl.S_NO where acl.USERID = ? and s.R_NO = r.R_NO) \n" +
          "order by R_NO";
      oDb.prepareStmt(_sSQLStr);
      oDb.setString(1, oUser.getID());
      oDb.prepareQuery();
      // 組 Option String
      _sRetStr = compOptionStr(oDb, "R_NO","R_NAME");
    }finally {
      oDb.close();
    }
    return _sRetStr;
  }


  /**
   *  傳回 Option String
   */
  private String compOptionStr(emisDb oDb, String sNo, String sName ) throws Exception {
    String _sRetStr="<option value='' selected></option>";
    String _sNo, _sName;

    try {
      while (oDb.next()) {
        _sNo   = oDb.getString(sNo);
        _sName = oDb.getString(sName);
        if( _sNo == null ) _sNo = "";
        if( _sName == null ) _sName = "";

        _sRetStr=_sRetStr + "<option value=\""+_sNo+"\">"+_sNo+" " + _sName+"</option>";
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
