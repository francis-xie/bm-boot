/*
 * $Header: /repository/src3/src/com/emis/business/emisBusinessMgr.java,v 1.1.1.1 2005/10/14 12:41:50 andy Exp $
 *
 * History:
 *   2003/07/12 Jerry: Add Menu_Name from Menus into attribute of emisBusinessImpl.
 *
 * Copyright EMIS Corp.
 */
package com.emis.business;

import com.emis.db.emisDb;
import com.emis.manager.emisAbstractMgr;
import com.emis.trace.emisError;
import com.emis.trace.emisTracer;
import com.emis.user.emisUser;

import javax.servlet.ServletContext;
import java.util.Properties;

public class emisBusinessMgr extends emisAbstractMgr {
  public static final String STR_EMIS_BUSINESSMGR = "com.emis.business.mgr";
  private boolean bIsPreProcessIf = false;

  public emisBusinessMgr(ServletContext application, Properties oProp) throws Exception {
    super(application, STR_EMIS_BUSINESSMGR, "emisBusiness");
    // default is false

    String _sIsPreProcessIf = oProp.getProperty("emis.xml.if.preprocess", "false");
    if ("true".equalsIgnoreCase(_sIsPreProcessIf)) {
      bIsPreProcessIf = true;
    }
  }

  public static emisBusinessMgr getInstance(ServletContext oContext) throws Exception {
    emisBusinessMgr _oMgr = (emisBusinessMgr) oContext.getAttribute(STR_EMIS_BUSINESSMGR);
    if (_oMgr == null) {
      emisTracer.get(oContext).sysError(null, emisError.ERR_SVROBJ_NOT_BIND, "emisBusinessMgr");
    }
    return _oMgr;
  }

  public static emisBusiness get(ServletContext oContext, String sBusiness, emisUser oUser) throws Exception {
    return getInstance(oContext).get(sBusiness, oUser);
  }



  public synchronized emisBusiness get(String sBusiness, emisUser oUser) throws Exception {
    String _sID = null;
    String _sGroup = null;
    if (oUser == null) throw new Exception("null user object");
    emisDb oDb = emisDb.getInstance(application_);
    try {
      oDb.setDescription("system: get BusinessBeans");
      _sGroup = oUser.getGroups();
      _sID = oUser.getID();
      if (_sID == null)
        emisTracer.get(application_).sysError(this, emisError.ERR_USER_NULL_ID);

      if (_sGroup == null)
        emisTracer.get(application_).sysError(this, emisError.ERR_USER_NULL_GROUPS);

      // user 要放前面
      // 2003/07/12 Jerry: 加入Menu_name
      //2004/01/08 Jacky  加入Menu            //System.out.println("BEAN:" + _sID + ":" + sBusiness+ ":" + _sGroup+ ":");
      oDb.prepareStmt("SELECT BusinessBeans.B_FILE, Menus.* FROM BUSINESSBEANS " +
            "  left outer join Menus on Menus.KEYS=BusinessBeans.B_NAME " +
            "  WHERE B_USER=? AND B_NAME=? " +
            " UNION " +
            " SELECT BusinessBeans.B_FILE , Menus.* FROM BUSINESSBEANS " +
            "  left outer join Menus on Menus.KEYS=BusinessBeans.B_NAME " +
            "  WHERE ( (B_GRP is null or B_GRP='') or B_GRP=?) AND B_NAME=?");

      oDb.setString(1, _sID);
      oDb.setString(2, sBusiness);
      oDb.setString(3, _sGroup);
      oDb.setString(4, sBusiness);

      oDb.prepareQuery();
      if (oDb.next()) {
        String sConfigFile = oDb.getString(1);
        if (sConfigFile != null) {
          String _sMenuName = oDb.getString(2);
          emisBusiness _oBusiness = null;
          if (_sMenuName == null) {
            _oBusiness = loadBusiness(sBusiness, oUser, sConfigFile + ".xml", bIsPreProcessIf);
          } else {
            _oBusiness = loadBusiness(sBusiness, oUser, sConfigFile + ".xml", _sMenuName, bIsPreProcessIf);
          }

          //2004/01/08 add by Jacky  增加設定MenuCode 如果出錯的話則MenuCode為空值  不要丟出錯誤訊息
          String _sMenuCode = "";
          try {
            _sMenuCode = oDb.getString("MENU_CODE");
            _oBusiness.setAttribute("sysMENU_CODE", _sMenuCode);
          } catch(Exception e){}

          if ("".equals(_sMenuCode) || _sMenuCode == null ){
            _sMenuCode = oDb.getString("KEYS");
            _oBusiness.setAttribute("sysMENU_CODE", _sMenuCode);
          }
          if (_oBusiness != null) return _oBusiness;
        }
      } else {
        emisTracer.get(application_).sysError(this, emisError.ERR_BUSINESS_NOGET, "id=" + _sID + " groups=" + _sGroup + " bname=" + sBusiness);
      }
    } catch (Exception e) {
      emisTracer.get(application_).sysError(this, emisError.ERR_BUSINESS_NOGET, "id=" + _sID + " groups=" + _sGroup + " err=" + e.getMessage());
    } finally {
      oDb.close();
      oDb = null;
    }

    return null;
  }


  private emisBusiness loadBusiness(String sBusiness, emisUser oUser, String sConfigFile, boolean bIsPreProcessIf) throws Exception {
    emisBusiness _oBusiness = new emisBusinessImpl(sBusiness, application_, oUser, sConfigFile, bIsPreProcessIf);
    _oBusiness.setAttribute("MENU_NAME", "");
    return _oBusiness;
  }

  private emisBusiness loadBusiness(String sBusiness, emisUser oUser,
                                    String sConfigFile, String sMenuName, boolean bIsPreProcessIf) throws Exception {
    emisBusiness _oBusiness = new emisBusinessImpl(sBusiness, application_, oUser, sConfigFile, bIsPreProcessIf);
    _oBusiness.setAttribute("MENU_NAME", sMenuName);
    return _oBusiness;
  }

/*------------------------------------------------------------------*/
  public void setProperty(int propertyID, Object oValue) throws Exception {
  }
}