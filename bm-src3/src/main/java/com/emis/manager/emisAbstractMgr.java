package com.emis.manager;

import com.emis.trace.emisError;
import com.emis.trace.emisTracer;

import javax.servlet.ServletContext;

/**
 * 所有具有管理功能的 Class 的 Base Class
 */
abstract public class emisAbstractMgr // extends PortableRemoteObject
implements emisMgr
{
    protected String sServiceName_;
    protected ServletContext application_;
    public emisAbstractMgr(ServletContext application,String regName,String serviceName) throws Exception
    {
        // used to export rmi-iiop
//        super();

        application_ = application;
        sServiceName_ = serviceName;
        if( application_.getAttribute(regName) != null )
        {
            emisTracer.get(application_).sysError(this,emisError.ERR_SVROBJ_DUPLICATE,serviceName);
        }
        application_.setAttribute(regName,this);
    }

    public void enableDebug(boolean debug) throws Exception
    {
    }

    public void stop() throws Exception
    {
    }
    public String getMgrVersion() throws Exception
    {
      return "1.0";
    }
    public String getServiceName() throws Exception
    {
      return sServiceName_;
    }

    /**
     *  各 subclass 需要 implement 不同的值有不同的反應
     */
    abstract public void setProperty(int propertyID,Object oValue) throws Exception;
}