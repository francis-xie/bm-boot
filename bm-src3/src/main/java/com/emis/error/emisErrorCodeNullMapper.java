package com.emis.error;

import com.emis.cache.emisCachable;
import com.emis.file.emisFileMgr;

import javax.servlet.ServletContext;

import org.w3c.dom.Document;

/**
 * Description :  emisErrorCodeMapper 的 NullObject class.
 * Author : Merlin
 * Date : 2004/8/6 下午 06:10:17
 * Revision : $Revision: 71118 $
 * History:
 * $Log: emisErrorCodeNullMapper.java,v $
 * Revision 1.1.1.1  2005/10/14 12:42:09  andy
 * add src3
 *
 * Revision 1.1  2004/08/07 10:18:37  merlin
 * NC
 *
 */
public class emisErrorCodeNullMapper extends emisCachable {
    public static emisErrorCodeNullMapper mapper = new emisErrorCodeNullMapper(null);

    private emisErrorCodeNullMapper(emisFileMgr oFileMgr) {
    }

    /**
     * @return
     * @throws Exception
     */
    public Document getErrorCodeXML() throws Exception {
        return null;
    }

    /**
     * @return
     */
    public boolean reload(ServletContext context) {
        return false;
    }

    /**
     * @param context
     * @return
     */
    public static emisCachable getInstance(ServletContext context) {
      return mapper;
    }

    public int getErrlevel(String errorCode) {
        return 3;
    }

    public String getErrorMsg(String errorCode) {
        return null;
    }

    public String getErrorMsg(String errorCode, String area) {
        return null;
    }

}
