package com.emis.xml;


import com.emis.cache.emisCache;
import com.emis.file.emisFile;
import org.w3c.dom.Document;

public class emisXMLCache implements emisCache
{

    private String sName_ ;
    private Object oDoc_;
    private emisFile oFile_;
    private long loadTime_;

    public emisXMLCache(String sName,emisFile oFile,Document oDoc)
    {
        sName_ = sName;
        oFile_ = oFile;
        oDoc_ = oDoc;
        loadTime_ = oFile_.lastModified();
    }
    public String getName()
    {
        return sName_;
    }
    public boolean isExpired()
    {
        if( oFile_.lastModified() > loadTime_ )
        {
            return true;
        }
        return false;
    }

    public Object getCache()
    {
        return oDoc_;
    }
}