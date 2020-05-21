package com.emis.schedule;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

/**
 *  提供 emisTask 的子 class 有 dynamic loading 的功能
 *  但是和系統的 dynamic 目錄下所提供的 dynamic loading 不同的是,
 *  此功能的 class 不限定目錄,只需在 classpath 內,且不會自動重新
 *  compile java 檔,只會在有新的 class 檔時才自動 reload class
 *  這樣上線系統可以即時更新 class
 *
 * Jerry: 本類別由emisScheduleMgr使用. loadClass()會先被使用, 找不到才會用findClass()
 *  @see com.emis.schedule.emisTask
 */
public class emisSchedClassLoader extends ClassLoader {

    //public static final boolean SCHED_RUN_IN_WEBINF = false;
    //public static final String SCHED_EMIS_ROOT = "C:/wwwroot/";
    //public static final String SCHED_EMIS_WEBINF = "/WEB-INF/classes/";

    private long lastModified_;
    private Properties oProp_ = null;

    public long getLastModified() {
        return lastModified_;
    }

    public emisSchedClassLoader(ClassLoader parent, long lastModified) {
        this(parent, lastModified, null);
    }

    public emisSchedClassLoader(ClassLoader parent, long lastModified, Properties oProp) {
        super(parent);
        this.lastModified_ = lastModified;
        this.oProp_ = oProp;
    }

    protected Class findClass(String sClassName) throws ClassNotFoundException {
        Class oCachedClass = super.findLoadedClass(sClassName);
        if (oCachedClass != null) {
            return oCachedClass;
        }

        FileInputStream fis = null;

        try {
            if (emisScheduleMgr.SCHED_RUN_IN_WEBINF) {
                fis = new FileInputStream(emisScheduleMgr.SCHED_EMIS_ROOT + emisScheduleMgr.SCHED_EMIS_WEBINF + sClassName.replace('.', '/') + ".class");
            } else {
              //   URL url = ClassLoader.getSystemClassLoader().getSystemResource(sClassName.replace('.', '/') + ".class");
                //改為  抓取副類別的classloader
               URL url = this.getParent().getResource(sClassName.replace('.', '/') + ".class");
                fis = new FileInputStream(url.getFile());
                //   fis = new FileInputStream(sClassName.replace('.', '/') + ".class");
            }

            byte[] temp = new byte[fis.available()];

            int flag = fis.read(temp);

            return super.defineClass(sClassName, temp, 0, temp.length);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
                ;
            }
        }
    }
}
