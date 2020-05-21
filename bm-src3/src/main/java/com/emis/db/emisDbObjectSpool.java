package com.emis.db;

import javax.servlet.ServletContext;

/**
 *  emisDb is an expensive Object
 *  we should put it to spool.
 *  系統用最簡單的方法, emisDb 的 constructor
 *  並不需要做額外的事情
 *  emisDb.close() 把自己 release 到 spool 來
 *  spool 用 stack 來做 , 不做任何檢查
 *
 *  ps. this Class is just work for internal, not for external
 *
 */

final public class emisDbObjectSpool extends com.emis.spool.emisSimpleSpool
{
    public static final String STR_EMIS_EMISDBOBJSPOOL = "com.emis.db.emisdbobjectspool";

    public emisDbObjectSpool(ServletContext oContext,int nSize) throws Exception
    {
        super(nSize);
        if( oContext.getAttribute(this.STR_EMIS_EMISDBOBJSPOOL) != null )
        {
            throw new Exception(this + " can't be registered twice");
        }
        oContext.setAttribute(this.STR_EMIS_EMISDBOBJSPOOL,this);
    }

    public synchronized emisDb checkOutEmisDb()
    {
        return (emisDb) super.checkOut();
    }

    public synchronized void checkInEmisDb( emisDb oDb )
    {
        super.checkIn(oDb);
    }


    public static emisDbObjectSpool getSpool(ServletContext oContext)
    {
        return (emisDbObjectSpool) oContext.getAttribute(STR_EMIS_EMISDBOBJSPOOL);
    }
}

