package com.emis.db;

import com.emis.trace.emisError;
import com.emis.trace.emisTracer;
import com.emis.user.emisUser;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

/**
 *   此 Class 將一些常用的小 SQL,如類別,公司別,...等
 *   常用,大小又不大的 SQL Query 結果存在 Cache 中,
 *   SQLName 和真正 SQL 的 Mapping 在資料庫 SQLCACHE table ??
 *   SQLName 請一律用大寫
 */
public class emisSQLCache
{
    public static final String STR_EMIS_SQLCACHE = "com.emis.db.sqlcache";

    public static final String SQLCACHETABLE = "SQLCACHE";

    // use hashtable, it is thread safe
    private Hashtable oCachedSQL_ = new Hashtable();

    private ServletContext application_;

    private emisSQLCacheLogicInf oCacheLogic_;

    public emisSQLCache(ServletContext application,Properties props) throws Exception
    {
        application_ = application;

        if ( application_.getAttribute(STR_EMIS_SQLCACHE) != null )
        {
            emisTracer.get(application).sysError(this,emisError.ERR_SVROBJ_DUPLICATE,"com.emis.db.emisSqlCache");
        }
        application_.setAttribute(STR_EMIS_SQLCACHE,this);

        String sSqlCacheLogic = props.getProperty("emis.sqlcache.logic");
        if( (sSqlCacheLogic != null) && (!"".equals(sSqlCacheLogic)) ) {
          oCacheLogic_ = (emisSQLCacheLogicInf) Class.forName(sSqlCacheLogic).newInstance();
        } else {
          emisTracer.get(application).sysError(this,emisError.ERR_SVROBJ_SQLCACHE_INIT,"Missing emis.sqlcache.logic in .cfg file");
        }
        reload();
    }

    protected static emisSQLCache getInstance(ServletContext application) throws Exception
    {
        emisSQLCache _oSQLCacheStore = (emisSQLCache) application.getAttribute(STR_EMIS_SQLCACHE);
        if( _oSQLCacheStore == null )
        {
            emisTracer.get(application).sysError(null,emisError.ERR_SVROBJ_NOT_BIND,"emisSQLCache");
        }
        return _oSQLCacheStore;
    }

    /**
     *  不含 User 權限或特殊邏輯的 SQL Cache
     */
    public static String getSQL(ServletContext application,String sSQLName) throws Exception
    {
        emisSQLCache _oSQLCacheStore = emisSQLCache.getInstance(application);
        return _oSQLCacheStore.getCache(sSQLName);
    }
    /**
     *  含 User 權限或特殊邏輯的 SQL Cache
     */
    public static String getSQL(ServletContext application,String sSQLName,emisUser oUser) throws Exception
    {
        emisSQLCache _oSQLCacheStore = emisSQLCache.getInstance(application);
        return _oSQLCacheStore.getCache(sSQLName,oUser);
    }
/*
    public static String getCompany(ServletContext application,emisUser oUser) throws Exception
    {
        emisSQLCache _oSQLCacheStore = emisSQLCache.getInstance(application);
        return _oSQLCacheStore.getCompany(oUser);
    }

    /**
     *  門市別
    private String getStore(emisUser oUser) throws Exception
    {
        String _sSNo = oUser.getSNo();
        if( (_sSNo != null) && (!"".equals(_sSNo)))
        {
          emisDb oDb = emisDb.getInstance(application_);
          try {

            oDb.prepareStmt("SELECT S_NAME FROM STORE WHERE S_NO=? ORDER BY S_NO");
            oDb.setString(1,_sSNo);
            oDb.prepareQuery();
            if( oDb.next())
            {
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
        return  this.getCache("STORE") ;
    }
     */

    /**
     * 公司別
    private String getCompany(emisUser oUser) throws Exception
    {
        String _sCNo = oUser.getCompanyNo();
        if( (_sCNo != null) && (!"".equals(_sCNo)))
        {
          emisDb oDb = emisDb.getInstance(application_);
          try {
            oDb.prepareStmt("SELECT COMPANY_SNAME FROM COMPANY WHERE COMPANY_NO=? order by COMPANY_NO");
            oDb.setString(1,_sCNo);
            oDb.prepareQuery();
            if( oDb.next())
            {
              String _sName = oDb.getString(1);
              if( _sName == null )
                _sName = "";
              return "<option value=\""+_sCNo+"\" SELECTED>"+_sCNo+" " + _sName+"</option>";
            }
          } finally {
            oDb.close();
          }
          return "<option value=\""+_sCNo+"\" SELECTED>"+_sCNo+"</option>";

        }
        return  this.getCache("COMPANY") ;
    }
     */


    private void sqlToCache(emisDb oDb,String sSQLName,String sSQL,String sFmt,long lastupdate) throws Exception
    {
        sSQLName = sSQLName.toUpperCase();

        oDb.executeQuery(sSQL);
        int _nColumnCount = oDb.getColumnCount();
        StringBuffer _oBuf = new StringBuffer();

        String _tmpFmt = sFmt;
//System.out.println(sSQL);
//System.out.println("FMT="+sFmt);
//System.out.println("COL="+_nColumnCount);

        for(int i=1; i<= _nColumnCount ; i++)
        {
//System.out.println("i="+i);
          _tmpFmt = emisUtil.stringReplace(_tmpFmt,"%"+i,"","a");
        }
        _oBuf.append(_tmpFmt).append("\n");

        while(oDb.next())
        {
            _tmpFmt = sFmt;
            for(int i=1; i<= _nColumnCount ; i++)
            {
                String _sValue = oDb.getString(i);
                if( _sValue != null )
                {
                    // 將 sFmt 的 %1,%2 代換掉
                    String _sReplace = "%" + i;
                    _tmpFmt = emisUtil.stringReplace(_tmpFmt,_sReplace,_sValue,"a");
                }
            }
            _oBuf.append(_tmpFmt).append("\n");
        }
        cache _oCache = new cache();
        _oCache.lastload_ = lastupdate;
        _oCache.sCache_ = _oBuf.toString();
        _oBuf = null;
        oCachedSQL_.put(sSQLName,_oCache);
    }

    public static HashMap getHash(ServletContext application) throws Exception
    {
        emisSQLCache _oSQLCacheStore = (emisSQLCache) application.getAttribute(STR_EMIS_SQLCACHE);
        if( _oSQLCacheStore == null )
        {
            emisTracer.get(application).sysError(null,emisError.ERR_SVROBJ_NOT_BIND,"emisSQLCache");
        }
        return _oSQLCacheStore.getHash();
    }

    public HashMap getHash() throws Exception
    {
        return (HashMap) oCachedSQL_.clone();
    }



    public static void expire(ServletContext application,String sName) throws Exception
    {
        emisSQLCache _oSQLCacheStore = (emisSQLCache) application.getAttribute(STR_EMIS_SQLCACHE);
        if( _oSQLCacheStore == null )
        {
            emisTracer.get(application).sysError(null,emisError.ERR_SVROBJ_NOT_BIND,"emisSQLCache");
        }
        _oSQLCacheStore.expire(sName);
    }


    private void expire ( String sSQLName) throws Exception
    {
      emisDb oDb = emisDb.getInstance(application_);
      try {
        oDb.setDescription("expire sql cache:"+sSQLName);
        oDb.expireSQLCache(sSQLName);
      } finally {
        oDb.close();
      }
    }

    /**
     * 表示有 User 管控的 SQLCache
     * 這段會和專案有差
     */
    private String getCache(String sSQLName,emisUser oUser) throws Exception
    {
      if ( sSQLName != null ) {
        sSQLName = sSQLName.toUpperCase();
      } else {
        return "";
      }
      if(oCacheLogic_!= null) {
        String _sSpecialLogic = oCacheLogic_.getSQL(application_,sSQLName,oUser);
        if( _sSpecialLogic != null )
          return _sSpecialLogic;
      }
      return getCache(sSQLName);
    }

    /**
     * 表示沒有 User 管控的 SQLCache
     */
    private String getCache(String sSQLName) throws Exception
    {
      if ( sSQLName != null ) {
        sSQLName = sSQLName.toUpperCase();
      } else {
        return "";
      }
      emisDb oDb = emisDb.getInstance(application_);
      try {
        oDb.setDescription("system:check sql cache expire");
        oDb.prepareStmt("SELECT * FROM "+SQLCACHETABLE+" WHERE SQLNAME=?");
        oDb.setString(1,sSQLName);
        oDb.prepareQuery();
        cache _oCache = (cache) oCachedSQL_.get(sSQLName);
        if( oDb.next())
        {
          java.util.Date oLastupdate = oDb.getTimestamp("LASTUPDATE");
          if( oLastupdate != null )
          {
            long lastupdate = oLastupdate.getTime();
            if( (_oCache == null) || (lastupdate > _oCache.lastload_) )
            {
              String _sSQL = oDb.getString("SQLCMD");
              String _sFmt = oDb.getString("SQLFMT");
              sqlToCache(oDb,sSQLName,_sSQL,_sFmt,lastupdate);
              _oCache = (cache) oCachedSQL_.get(sSQLName);
            }
          }
        }
        if( _oCache != null )
          return _oCache.sCache_;
        return "";
      } finally {
        oDb.close();
      }

    }


    public static void reload(ServletContext application) throws Exception
    {
        emisSQLCache _oSQLCacheStore = emisSQLCache.getInstance(application);
        _oSQLCacheStore.reload();
    }

    protected synchronized void reload() throws Exception
    {
        emisDb oDb = emisDb.getInstance(application_);
        try {

            oDb.setDescription("system:sqlCache");
            oDb.executeQuery("SELECT * FROM " + SQLCACHETABLE);
            emisRowSet _oCaches = new emisRowSet(oDb);
            try {
                while(_oCaches.next())
                {
                    String _sSQLName = _oCaches.getString("SQLNAME");
                    String _sSQL     = _oCaches.getString("SQLCMD");
                    String _sFmt     = _oCaches.getString("SQLFMT");
                    Date _d = _oCaches.getTimestamp("LASTUPDATE");

                    try {
                      sqlToCache(oDb,_sSQLName,_sSQL,_sFmt,(_d==null) ? 0 : _d.getTime());
                    }catch(Exception cache) {
                      emisTracer.get(application_).warning(this,"[emisSQLCache]" +_sSQL+":"+ cache.getMessage());
                    }
                }

            } catch (Exception err) {
                emisTracer.get(application_).warning(this,err.getMessage());
            }
        } catch (Exception e) {
            emisTracer.get(application_).warning(this,e.getMessage());
        } finally {
            oDb.close();
        }

    }

    class cache
    {
      long lastload_;
      String sCache_;
    }
}
