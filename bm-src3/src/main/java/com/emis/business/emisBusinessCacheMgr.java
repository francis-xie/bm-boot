package com.emis.business;

import com.emis.trace.emisError;
import com.emis.trace.emisTracer;
import com.emis.xml.emisXMLCache;

import javax.servlet.ServletContext;
import java.util.Hashtable;
import java.util.Properties;

public class emisBusinessCacheMgr
{
    public static final String STR_EMIS_BUSINESS_CACHE_MGR="com.emis.business.cachemgr";

    private ServletContext oContext_;
    private Hashtable oCacheHash_ = new Hashtable();

    public emisBusinessCacheMgr(ServletContext application,Properties oProps) throws Exception
    {
        oContext_ = application;
        if( oContext_.getAttribute(this.STR_EMIS_BUSINESS_CACHE_MGR) != null )
        {
            emisTracer.get(application).sysError(this,emisError.ERR_SVROBJ_DUPLICATE,"emisBusinessCacheMgr");
        }
        oContext_.setAttribute(this.STR_EMIS_BUSINESS_CACHE_MGR,this);
    }

    public static emisBusinessCacheMgr getInstance(ServletContext application) throws Exception
    {
        emisBusinessCacheMgr _oMgr = (emisBusinessCacheMgr)application.getAttribute(STR_EMIS_BUSINESS_CACHE_MGR);
        if( _oMgr == null )
        {
          emisTracer.get(application).sysError(null,emisError.ERR_SVROBJ_NOT_BIND,"emisBusinessCacheMgr");
        }
        return _oMgr;
    }


    public synchronized emisXMLCache get(String sName)
    {
        if( sName == null ) return null;
        sName = sName.toLowerCase();
        emisXMLCache _oCache = (emisXMLCache) oCacheHash_.get(sName);
        if( _oCache != null)
        {
            if(!_oCache.isExpired())
            {
                return _oCache;
            }
            // already expired , remove it
            oCacheHash_.remove(sName);
        }
        return null;
    }

    public synchronized void put (emisXMLCache oCache)
    {
        String _sName = oCache.getName();
        if( _sName == null ) return;
        _sName = _sName.toLowerCase();
        oCacheHash_.put(_sName,oCache);
    }

}