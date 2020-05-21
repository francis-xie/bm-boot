package com.emis.business;

/**
 * Created by IntelliJ IDEA.
 * User: shaw
 * Date: 2003/9/16
 * Time: 下午 06:34:14
 * To change this template use Options | File Templates.
 */
public class emisThreadLocalObject {
        // 測試 ThreadLocal 用於除錯用, added by Shaw 2003/09/16
    private static ThreadLocal oDebug = new ThreadLocal();

    public static Object isDebug() {
        return oDebug.get();
    }

    public static void setDebug(Object isDebug) {
        oDebug .set(isDebug);
    }
}
