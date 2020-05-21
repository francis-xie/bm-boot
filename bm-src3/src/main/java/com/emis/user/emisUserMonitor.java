package com.emis.user;

import java.util.HashMap;

/**
 *  這個 Class 提供了可以 Monitor 現在系統的帳號登入情形
 *
 */

public class emisUserMonitor
{
    private HashMap oHash_;

    public emisUserMonitor()
    {
        oHash_ = new HashMap();
    }

    protected void putSessionObject(Object oSessionObject,Object oUserObject)
    {
        oHash_.put(oSessionObject,oUserObject);
    }

    public HashMap getUserList()
    {
        return (HashMap) oHash_.clone();
    }

    public void removeSession(Object oSessionObject)
    {
        oHash_.remove(oSessionObject);
    }

}

