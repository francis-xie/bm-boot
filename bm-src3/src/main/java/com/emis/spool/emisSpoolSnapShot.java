package com.emis.spool;

import java.util.Enumeration;

/**
 *  此 Class 是用來描述 emisComplexSpool 內的運作情形用的
 *  可以得到幾個 Resource 正在 Spool 中,幾個已借出....etc
 *
 *  @see com.emis.spool.emisComplexSpool
 */

public class emisSpoolSnapShot
{
    protected int nPooledSize_;
    protected int nCheckOutSize_;
    protected Enumeration oPoolEnum_;

    public int getPooledSize()
    {
        return nPooledSize_;
    }

    public int getCheckedOutSize()
    {
        return nCheckOutSize_;
    }

    public Enumeration getDescriptor()
    {
        return oPoolEnum_;
    }
}
