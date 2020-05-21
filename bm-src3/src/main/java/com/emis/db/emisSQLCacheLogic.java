package com.emis.db;

import com.emis.user.emisUser;

import javax.servlet.ServletContext;

/**
 *   ePos 所 Default implement 的 SQLCache
 *
 *   @see com.emis.db.emisSQLCacheLogicInf
 */
public class emisSQLCacheLogic implements emisSQLCacheLogicInf
{
  public String getSQL(ServletContext application,String sSQLName, emisUser oUser) throws Exception
  {
    if("STORE".equals(sSQLName)) {
      return getStore(application,oUser);
    }
    return null;
  }

  private String getStore(ServletContext application,emisUser oUser) throws Exception
  {
    String _sSNo = oUser.getSNo();
    if( (_sSNo != null) && (!"".equals(_sSNo)))
    {
      emisDb oDb = emisDb.getInstance(application);
      try {
        oDb.prepareStmt("SELECT S_NAME FROM STORE WHERE S_NO=? ORDER BY S_NO");
        oDb.setString(1,_sSNo);
        oDb.prepareQuery();
        if( oDb.next()) {
          String _sName = oDb.getString(1);
          if( _sName == null )
            _sName = "";
          return "<option value=\""+_sSNo+"\" SELECTED>"+_sSNo+" " + _sName+"</option>";
        }
      } finally {
        oDb.close();
      }
      return "<option value=\""+_sSNo+"\" SELECTED>"+_sSNo+"</option>";
    }
    return  null;
  }

}