package com.emis.spool;



/**
 *  提供最簡單的 Object Spool
 *  只負責 Object 的 存/取,
 *  其實 JDK1.3 以後有大幅改善系統面的 Object pooling
 *  我們只針對 emisDb 這個 class 做 Object pooling
 *  因為用得很頻繁,而且此 Class Size 不小
 *
 *  @see com.emis.db.emisDb
 */
abstract public class emisSimpleSpool
{


    private int nSize_;
    private int nCurrentSize_;
    private Object [] aPooledObject;
    private int nPos_;

    public emisSimpleSpool (int nSize)
    {
        nSize_ = nSize;
        nCurrentSize_ = 0;
        aPooledObject = new Object [nSize_];
    }

    public int getCheckOutPos()
    {
        return nPos_;
    }

    public int setCheckOutPos(int nPos)
    {
        return nPos_ = nPos;
    }

/*-------------------------- report status method-----------------------*/

    public int getSpoolSize()
    {
        return nSize_;
    }

   /**
    *  @return Current Pooled Object Number
    */

    public int getSpoolObjectSize()
    {
        return nCurrentSize_;
    }

/*-------------------------- really used method-------------------------*/

    protected Object checkOut()
    {
        if(nCurrentSize_ <= 0 ) return null;
        --nCurrentSize_;
        return aPooledObject[nCurrentSize_];
    }


    protected void checkIn(Object oPooled)
    {
        if( (nCurrentSize_+1) < nSize_  )
        {
            aPooledObject[nCurrentSize_] = oPooled;
            ++nCurrentSize_;
        }
    }
}
