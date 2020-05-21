package com.emis.schedule;

import javax.servlet.ServletContext;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2004/11/22
 * Time: 下午 12:45:26
 * To change this template use Options | File Templates.
 */
public class emisScheduleMgrImpl extends emisScheduleMgr{
   // oClassLoader = new emisSchedClassLoader((ClassLoader) this.oContext_.getAttribute("caucho.class-loader"), _lastModified, oProp);
   // oClassLoader = new emisSchedClassLoader(ClassLoader.getSystemClassLoader(), _lastModified, oProp);
    private  emisScheduleMgrImpl(ServletContext oContext, Properties oProp) throws Exception{

      //   super(oContext, oProp,(ClassLoader) oContext.getAttribute("caucho.class-loader"));
       //  super(oContext, oProp,ClassLoader.getSystemClassLoader());
       super(oContext, oProp);

    }
}
