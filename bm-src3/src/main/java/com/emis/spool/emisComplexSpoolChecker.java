package com.emis.spool;


/**
  *  此 Class 是跟 emisComplexSpool 一起的,
  *  Spool Checker 是另一個 Thread , 呼叫 Spool 的 CleanUp
  *  但實際的檢查 Resource 動作是寫在 emisComplexSpool
  *  @see com.emis.spool.emisComplexSpool
  */
final public class emisComplexSpoolChecker implements Runnable
{
    private emisComplexSpool oSpool_ ;
    private long nInterval_ ;
    private volatile boolean isStop = false;

    public emisComplexSpoolChecker(long nInterval,emisComplexSpool oSpool)
    {
        oSpool_ = oSpool;
        nInterval_ = nInterval;
    }

    public void stopThread()
    {
        isStop = true;
    }


    public void run()
    {
        while( ! isStop )
        {
            try {
                // get the object's monitor
                synchronized(this)  {
                    wait(this.nInterval_);
                }
            } catch(InterruptedException e) {}

            try {  // cleanUp is synchronized
                if( oSpool_ != null )  {
                    synchronized(oSpool_)  {
                        oSpool_.cleanUp();
                        oSpool_.notifyAll();
                    }
                }
            }catch(Exception ignore) {
            }
        }
    }
}