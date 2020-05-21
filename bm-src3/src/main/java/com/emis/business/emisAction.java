package com.emis.business;

import com.emis.trace.emisTracer;
import com.emis.user.emisUser;
import org.w3c.dom.Element;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Writer;

/**
 *  emisAction 是 emisDatabase,emisShowData,
 *  emisPrintData,emisPrintChart,emisWriteChart 的底層
 */
abstract public class emisAction
{
    protected emisUser oUser_;
    protected Element eRoot_ ;
    protected Writer out_ ;
    protected HttpServletRequest request_ ;
    protected ServletContext oContext_;
    protected emisTracer oTrace_;
    protected emisBusiness oBusiness_;

    protected emisAction(emisBusiness oBusiness,Element root,Writer out ) throws Exception
    {
        set(oBusiness,root,out);
    }

    protected void set(emisBusiness oBusiness,Element root,Writer out) throws Exception
    {
        oBusiness_ = oBusiness;
        oContext_ = oBusiness.getContext();
        oUser_ = oBusiness.getUser();
        request_ = oBusiness.getRequest();
        eRoot_ = root;
        out_ = out;
        oTrace_ = emisTracer.get(oContext_);
    }

    abstract public void doit() throws Exception;
}