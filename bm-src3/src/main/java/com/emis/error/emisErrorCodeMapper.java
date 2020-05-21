package com.emis.error;

import com.emis.cache.emisCachable;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;
import com.emis.file.emisFile;
import com.emis.xml.emisXmlFactory;
import com.emis.server.emisServerFactory;
import com.emis.qa.emisServletContext;
import com.emis.util.emisProperties;

import javax.servlet.ServletContext;
import java.util.Locale;
import java.util.HashMap;
//import java.util.Properties;
import java.io.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * Description :  errorCode 和 error
 * Author : Merlin
 * Date : 2004/8/6 下午 06:10:17
 * Revision : $Revision: 71118 $
 * History:
 * $Log: emisErrorCodeMapper.java,v $
 * Revision 1.1.1.1  2005/10/14 12:42:09  andy
 * add src3
 *
 * Revision 1.5  2004/10/01 08:01:57  merlin
 * NC
 *
 * Revision 1.4  2004/08/07 13:27:20  merlin
 * 摰????ErrorCode ???憭?隤?蝟聚rrorMessage撠????
 *
 * Revision 1.3  2004/08/07 10:21:09  merlin
 * NC
 * <p/>
 * Revision 1.2  2004/08/07 07:36:28  merlin
 * NC
 * <p/>
 * Revision 1.1  2004/08/06 10:37:09  merlin
 * NC
 */
public class emisErrorCodeMapper extends emisCachable {

    private HashMap hmErrorCode_ = null;
    private HashMap hmError_ = null;
    private HashMap PropertyMap = null;
    private emisFileMgr oFileMgr_ = null;
    private Document oErrorCodeDoc_;
    private DataOutputStream f;
    private ServletContext oContext_;

    private void initErrorCode() {

        hmErrorCode_.clear();
        NodeList nList = oErrorCodeDoc_.getElementsByTagName("Error");
        /*分析所有的Error node*/
        if (nList != null) {
            int len = nList.getLength();
            /* 開始分析Error Code Tree*/
            for (int i = 0; i < len; i++) {
                Node oNode = nList.item(i);
                if (oNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) oNode;
                    /* Error Code 的 node name*/
                    String sName = e.getAttribute("name");
                    hmErrorCode_.put(sName, e);
                }
            }
        }
    }

    private emisErrorCodeMapper(ServletContext context) {
        this.oContext_ = context;
        try {
            this.oFileMgr_ = emisFileMgr.getInstance(context);
            /* 準備Document*/
            PropertyMap = (HashMap) context.getAttribute("PropertyMap");
            if (PropertyMap == null) {
                PropertyMap = new HashMap();
                context.setAttribute("PropertyMap", PropertyMap);
            }

//            oErrorCodeDoc_ = getErrorCodeXML();
//            /* 分析Document並轉成HashMap*/
//            initErrorCode();

            //setErrorCode ("GRN0001");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @return
     * @throws Exception
     */
    public Document getErrorCodeXML() throws Exception {
        FileInputStream fis = null;
        emisDirectory _oXmlDir = this.oFileMgr_.getDirectory("root").subDirectory("WEB-INF");

        fis = new FileInputStream(_oXmlDir.getDirectory() + "errorEvent.xml");
        return emisXmlFactory.getXML(fis);
    }

    /**
     * @return
     */
    public boolean reload(ServletContext context) {
        return true;
    }

    /**
     * @param context
     * @return
     */
    public static emisCachable getInstance(ServletContext context) {
        emisErrorCodeMapper mapper = null;
        try {
            mapper = (emisErrorCodeMapper) context.getAttribute("ErrorCodeMapper");
            if (mapper==null) {
                mapper =  new emisErrorCodeMapper(context);
                context.setAttribute("ErrorCodeMapper", mapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapper;
    }

    public int getErrlevel(String errorCode) {
        // Todo : not implemented
        return 3;
    }

    public String getErrorMsg(String errorCode) {
        return getErrorMsg(errorCode, Locale.TAIWAN);
    }

    public String getErrorMsg(String errorCode, Locale locale) {
        emisProperties localeProperties = getLocaleProperties(locale);
        if (localeProperties == null)
            return "";
        String Message = localeProperties.getProperty(errorCode);
        return (Message != null) ? Message : "";
    }

    private emisProperties getLocaleProperties(Locale locale) {
        emisProperties p = (emisProperties) this.PropertyMap.get(locale);
        if (p == null) {
            p = loadLocaleMessage(locale);
            this.PropertyMap.put(locale, p);
        }
        return p;
    }

    private emisProperties loadLocaleMessage(Locale locale) {
        emisProperties p = new emisProperties();
        try {
            emisDirectory dir = this.oFileMgr_.getDirectory("root").
                    subDirectory("WEB-INF" + File.separator + "ErrorCode");

            dir.getDirectory();
            emisFile f = dir.getFile("ErrorCode_" + locale.getLanguage() + "_" + locale.getCountry() + ".properties");
            p.load(f.getInStream());

        } catch (Exception e) {
            e.printStackTrace();
            p = null;
        }
        return p;
    }

    public static void main(String[] args) {
        emisServletContext _oContext = new emisServletContext();
        try {
            emisServerFactory.createServer(_oContext, "c:\\wwwroot\\eros",
                    "c:\\resin\\eros.cfg", true);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(1);
        }
        emisErrorCodeMapper m = (emisErrorCodeMapper) emisErrorCodeMapper.getInstance(_oContext);
        System.out.println(m.getErrorMsg("R0123"));
        System.out.println(m.getErrorMsg("R0124"));
        System.out.println(m.getErrorMsg("R0123", Locale.US));
        System.out.println(m.getErrorMsg("R0124", Locale.US));
    }
}
