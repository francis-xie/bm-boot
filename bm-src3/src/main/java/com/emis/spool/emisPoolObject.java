package com.emis.spool;


/**
 *  emisSpoolObject 提供了 emisPoolable 界面
 *  想要提供 Poolable 的 Class 只要繼承  emisPoolObject
 *  可以使 implement 簡單一點   
 *
 *  @see com.emis.spool.emisPoolable
 *  @see com.emis.spool.emisComplexSpool
 */
abstract public class emisPoolObject implements emisPoolable
{
    protected Object oPooledObj_ ;
    protected long oLastAttach_;

    protected boolean isResourceLost;

    public emisPoolObject( Object oPooledObj )    {
        oPooledObj_ = oPooledObj;
        isResourceLost = false;
        setTime();
    }

    public long getTime()
    {
        return oLastAttach_;
    }

    public void setTime(long now)
    {
        this.oLastAttach_ = now;
    }

    public void setTime()
    {
        this.oLastAttach_ = System.currentTimeMillis();
    }

    public Object getPooledObject()
    {
        return oPooledObj_;
    }

    protected void setResourceLost()
    {
        isResourceLost = true;
    }

    /**
        * is this object available for user ?
        */
    public boolean validate ()
    {
        // if it has occured a fatal error...
        // or resource already closed
        if( isResourceLost )
        {
//            System.out.println("Resource lost");
            return false;
        }
        if( oPooledObj_ == null )
        {
//            System.out.println("OOBJ_ is null");
            return false;
        }
        return true;
    }

    abstract public void freeResource();

}