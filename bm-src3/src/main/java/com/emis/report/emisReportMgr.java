/*
 * $History: emisReportMgr.java $
 * 
 * *****************  Version 1  *****************
 * User: Robert       Date: 01/12/06   Time: 10:03p
 * Created in $/resin/com/emis/report
 */
package com.emis.report;

import com.emis.user.emisUser;

import javax.servlet.ServletContext;
import java.lang.reflect.Constructor;
import java.util.Properties;

public class emisReportMgr
{
    public static final int PDF = 1;
    public static final int TEXT = 2;
    public static final int HTML = 3;

    public static final String STR_EMIS_REPORT_BUILDER = "com.emis.report.builder";

    private emisReportBuilder oBuilder_;
    private ServletContext oContext_;

    public emisReportMgr(ServletContext oContext, Properties oProps ) throws Exception
    {
        oContext_ = oContext;
        if( oContext_.getAttribute(this.STR_EMIS_REPORT_BUILDER) != null )
        {
            throw new Exception(this + " 已在 ServletContext 中註冊過了");
        }
        oContext_.setAttribute(this.STR_EMIS_REPORT_BUILDER,this);

        String _sBuilder = oProps.getProperty("com.emis.report.builder","com.emis.report.emisStyleReportBuilder");
        Class _oClass = Class.forName(_sBuilder);
        Class []  list =  { javax.servlet.ServletContext.class,java.util.Properties.class };
        Object [] oParam = { oContext,oProps };
        try {
            Constructor _oConstruct = _oClass.getConstructor(list);
            oBuilder_ = (emisReportBuilder)  _oConstruct.newInstance(oParam);
        } catch (Exception ignore) {
            oBuilder_ = (emisReportBuilder) _oClass.newInstance();
        }
    }

    public static emisReportMgr getBuilder(ServletContext oContext)
    {
        return (emisReportMgr) oContext.getAttribute(emisReportMgr.STR_EMIS_REPORT_BUILDER);
    }

    public emisReport getReport(String sReportID,emisUser oUser) throws Exception
    {
        return oBuilder_.getReport(sReportID,oUser);
    }

}
