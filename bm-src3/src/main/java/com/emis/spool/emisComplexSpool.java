package com.emis.spool;

import com.emis.db.emisAbstractProxy;
import com.emis.trace.emisTracer;
import com.emis.util.emisCommonEnum;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

/**
 *  此 Class 提供了 Connection Spooling 的管理功能
 *  其中 Orphan 指一個 Resource 被借出多久以後會被當成
 *  孤兒,也就是借出不還, Spool 會自動 Release 此 Resource
 *  另外, Expire 是指 Resource 在 Spool 內很久沒被使用
 *  會被當成是不需要使用的 Resource,Spool 會維持一個
 *  最小的 Resource 執,並有一個最大值的限制, 檢查這些 
 *  Resource 的週期也可以設定, 每個 Spool 的這些設定
 *  都是獨立的
 *
 *  @see com.emis.spool.emisComplexSpoolChecker
 *  @see com.emis.spool.emisSimpleSpool
 *  @see com.emis.spool.emisPoolable
 */
abstract public class emisComplexSpool
{

    private  ArrayList aPooledList_  = new ArrayList();
    private  ArrayList aCheckOutList_= new ArrayList() ;

    private static final boolean isDebug_ = false;

    // 在 Spool 的物件時,超過多久沒有被外借會被 Free 掉
    protected long nExpire_;

    // 物件外借後,多久沒還,就幫他 close 掉
    protected long nOrphan_;

    // 在 new Poolable 物件時, 多久沒有回傳會 TimeOut
    // 單位為秒

    protected int nTimeOut_;
    //發生 stack overflow   要紀錄原本的 ntimeout 的值
    protected int nTimeOutnNum_;
    protected long nInterval_;

    protected int nMaxSize_    ;
    protected int nMinSize_    ;
    protected int nInitSize_   ;

    // DB Spool 初始化完成的標記
    protected boolean bInitReady_ = false;

    protected ServletContext oContext_;

    protected Thread  oCheckerThread_;
    private emisComplexSpoolChecker oChecker_;

    public static final int DEFAULT_EXPIRE = 600;
    public static final int DEFAULT_TIMEOUT=10;
    public static final int DEFAULT_ORPHAN = 1800;
    public static final int DEFAULT_INTERVAL= 10;
    public static final int DEFAULT_MAX  = 15;
    public static final int DEFAULT_MIN  = 1;
    public static final int DEFAULT_INIT = 2;


    private int _strToInt( String sStr ,int nDefault )
    {
        int nReturn = nDefault;
        try {
            nReturn = Integer.parseInt(sStr);
        } catch(Exception ignore) { }
        return nReturn;
    }

    protected int _getProperty (Properties oProps,String sName,int nDefault )
    {
        String _sStr = null;
        _sStr = oProps.getProperty(sName);
        if ( _sStr != null )
        {
            int nReturn = _strToInt( _sStr, nDefault);
            return nReturn;
        }
        return nDefault;
    }

    public emisComplexSpool(ServletContext oContext,Properties Props) throws Exception
    {
        oContext_ = oContext;

        createSource(Props);

        nExpire_  = _getProperty(Props,"expire.second",DEFAULT_EXPIRE);
        nTimeOut_ = _getProperty(Props,"timeout.second",DEFAULT_TIMEOUT);
        nTimeOutnNum_ =nTimeOut_;
        nOrphan_  = _getProperty(Props,"orphan.second",DEFAULT_ORPHAN);
        nInterval_= _getProperty(Props,"checker.interval.second",DEFAULT_INTERVAL);
        nMaxSize_ = _getProperty(Props,"maxsize",DEFAULT_MAX);
        nMinSize_ = _getProperty(Props,"minsize", DEFAULT_MIN);
        nInitSize_= _getProperty(Props,"initsize", DEFAULT_INIT);

        _buildSpool();
    }

    abstract protected void createSource(Properties props)throws Exception;

    public void setDebug(boolean isDebug)
    {
        //isDebug_ = isDebug;
    }
/*
    public emisComplexSpool(int nExpireSecond,int nTimeOut,int nOrphanSecond,int nIntervalSecond,int nMaxSize,int nMinSize,int nInitSize)
    {

        nExpire_  = nExpireSecond ;
        nTimeOut_ = nTimeOut;
        nOrphan_  = nOrphanSecond ;
        nInterval_= nIntervalSecond;
        nMaxSize_ = nMaxSize;
        nMinSize_ = nMinSize;
        nInitSize_ = nInitSize;
        _buildSpool();
    }
*/
    private void _buildSpool() throws Exception
    {
        // transfer to milisecond
        nExpire_  = nExpire_ * 1000;
        // timeOut is no need to transfer to miliseconds...
        nOrphan_  = nOrphan_ * 1000 ;
        nInterval_ = nInterval_ * 1000;

        if( nMaxSize_ <= 0 ) nMaxSize_ = DEFAULT_MAX;
        if( nInitSize_ > nMaxSize_) nInitSize_ = nMaxSize_;
        if( nMinSize_ > nMaxSize_) nMinSize_ = 0;

        // start init thread
        emisSpoolInit oInit = new emisSpoolInit(oContext_,this);
      //  oInit.handrun();
      //  oInit.handRun();
        Thread oInitThread = new Thread(oInit);
        oInitThread.start();


        // start checker thread
        oChecker_ =  new emisComplexSpoolChecker(nInterval_,this);
        oCheckerThread_ = new Thread(oChecker_ );
        oCheckerThread_.setName("emis Db Spool Checker");
        oCheckerThread_.setDaemon(true);
        oCheckerThread_.start();
        emisTracer.get(oContext_).info("Start Spool Checker Thread Successful:" + this.toString());
    }


    protected emisPoolable getSpoolObjectFromSource(boolean applyTimeOut) throws Exception
    {
        emisPoolable _oObj = null;
        if( applyTimeOut ){
           try{
          _oObj = generateRealPooledObject(nTimeOut_);
           }catch(Exception e ){ e.printStackTrace();}
        }else{
          _oObj = generateRealPooledObject(0);
        }
        _oObj.setDescription("連線池");
        aPooledList_.add(_oObj);
        return _oObj;
    }

    public synchronized emisPoolable checkOut() throws Exception
    {

        long now = System.currentTimeMillis();
        int _nSize = aPooledList_.size();

        if( _nSize > 0 ) // maybe there is one available
        {
          for( int _nIdx = 0 ; _nIdx < _nSize ; _nIdx++)
          {
              emisPoolable _oPool = (emisPoolable) aPooledList_.get(_nIdx);
              // 檢查一下物件是否正常
              if( _oPool.validate() )
              {
                  aPooledList_.remove(_oPool);
                  aCheckOutList_.add(_oPool);
                  if( isDebug_) {
                      emisTracer.get(oContext_).info("checkOut from spool:normal:"+_oPool);
                  }
                  _oPool.setTime(now);
                  _oPool.setDescription("Borrowing");
                   nTimeOut_ = nTimeOutnNum_;
                  return _oPool;
              }
          }
        }

        // no find any available....
        if( totalSize() < nMaxSize_)
        {
            // there is still room for spool
            emisPoolable _oPool = null;
            if ((_oPool = getSpoolObjectFromSource(true)) != null)
            {
                // successful got one
                aPooledList_.remove(_oPool);
                aCheckOutList_.add(_oPool);
                _oPool.setTime(now);
                if( isDebug_ ) {
                     emisTracer.get(oContext_).info("not any available:allocate resource success");
                }
                _oPool.setDescription("Borrowing");
                 nTimeOut_ = nTimeOutnNum_;
                return _oPool;
            }
        }

        if( isDebug_ )
        {
          emisTracer.get(oContext_).info("still no available,thread wait");
        }

        try {
            wait(nTimeOut_);
          nTimeOut_ =nTimeOut_+nTimeOut_;
        }catch(java.lang.Exception ignore) {
            System.out.println("ignore"+nTimeOut_);
        }
        try{

            try{

               System.out.println("CHECK OUt SIZE -------------------------------"+aCheckOutList_.size());
                  // 因此時系統有問題了  故犧牲掉 第一個 pool
                if( aCheckOutList_.size()  > 1){
                 emisPoolable _oPool = (emisPoolable) aCheckOutList_.get( 0);
                 _oPool.freeResource();
                 aCheckOutList_.remove(_oPool);
                }
               //  cleanUp();
            System.out.println("remove pool-------------------------------------"+aCheckOutList_.size());
            }catch(Exception e){e.printStackTrace();}
        }catch(java.lang.StackOverflowError ee){
            System.out.println("  stack over flow -------------------------restart");

        }

        return checkOut();
    }


    public synchronized void checkIn(emisPoolable oObj) throws Exception
    {

        if( aCheckOutList_.remove(oObj) )
        {
          aPooledList_.add(oObj);
          oObj.setTime();
          oObj.setDescription("連線池");
          if(isDebug_)  {
            emisTracer.get(oContext_).info("Check In:"+oObj+ (oObj.hasFatalError() ? "  FatalError":""));
          }
        } else {
          if( isDebug_ ) {
            emisTracer.get(oContext_).info("check In Error,not found in checkOutList");
          }
        }
    }


    /**
     * 檢查 list 中是否有需要 free resource
     *
     */
    private void checkListA(long now) throws Exception
    {
        int _nCheckSize = aPooledList_.size();

        for(int _nIdx = (_nCheckSize - 1) ;_nIdx >= 0  ; _nIdx--)
        {
            emisPoolable _oPool = (emisPoolable) aPooledList_.get( _nIdx);
            long _oLastTouch = _oPool.getTime();

            if((now - _oLastTouch)  >  nExpire_)
            {
              if( totalSize() > this.nMinSize_ )
              {
                if(isDebug_)  {
                  emisTracer.get(oContext_).info("expired, free connection:"+_oPool);
                }
                _oPool.freeResource();
                aPooledList_.remove(_oPool);
              }
            }
        }

        // 先把 Fatal Error 的 free 掉, Fatal Error 的是一
        // 定要強迫 free 掉

        _nCheckSize = aPooledList_.size();

        for(int _nIdx = (_nCheckSize - 1) ;_nIdx >= 0  ; _nIdx--)
        {
          emisPoolable _oPool = (emisPoolable) aPooledList_.get( _nIdx);
          if(_oPool.hasFatalError())
          {
            Exception fatal = _oPool.getFatalError();
            if( fatal != null ) {
              // fatal error happen,set all connection to
              // error and free it
              try {
                emisTracer oTr = emisTracer.get(oContext_);
                oTr.info("fatal error, free connection:"+_oPool);
                oTr.reportException(_oPool.getFatalError());
              } catch (Exception ignore) {}
            }
            _oPool.freeResource();
            aPooledList_.remove(_oPool);
            // set all others to fatal error
            setAllPoolObjectFatalError();
          }
          if (isDebug_ ) {
            emisTracer.get(oContext_).info("not fatal:"+_oPool);
          }
        }

    }

    private void setAllPoolObjectFatalError()
    {
      int _Size = aPooledList_.size();
      for(int _nIdx = 0; _nIdx < _Size;_nIdx++) {
        emisPoolable _oPool = (emisPoolable) aPooledList_.get( _nIdx);
        _oPool.setFatalError();
      }
      _Size = aCheckOutList_.size();
      for(int _nIdx = 0; _nIdx < _Size;_nIdx++) {
        emisPoolable _oPool = (emisPoolable) aCheckOutList_.get( _nIdx);
        _oPool.setFatalError();
      }
    }


    private void checkListB(long now) throws Exception
    {
        // 先把 Fatal Error 的 free 掉, Fatal Error 的是一
        // 定要強迫 free 掉
        // 2001.3.11 mark 這段
        // 因為正在使用中,可能會造成不可預期的 Error, for example,not commit
        /*
        int _nCheckSize = aCheckOutList_.size();

        for(int _nIdx = (_nCheckSize - 1) ;_nIdx >= 0  ; _nIdx--)
        {
            emisPoolable _oPool = (emisPoolable) aCheckOutList_.get( _nIdx);
            if(_oPool.hasFatalError())
            {
                try {
                  emisTracer oTr = emisTracer.get(oContext_);
                  oTr.info("CheckOutList fatal error, free connection");
                  oTr.reportException(_oPool.getFatalError());
                } catch (Exception ignore) {}
                _oPool.freeResource();
                aCheckOutList_.remove(_oPool);
            }
        }
        */

        int _nCheckSize = aCheckOutList_.size();

        for(int _nIdx = (_nCheckSize - 1) ;_nIdx >= 0  ; _nIdx--)
        {
            emisPoolable _oPool = (emisPoolable) aCheckOutList_.get( _nIdx);
            long _oLastTouch = _oPool.getTime();

            if((now - _oLastTouch)  >  nOrphan_)
            {
              if(isDebug_)  {
                emisTracer.get(oContext_).info("Orphan,free connection:"+_oPool);
              }
              _oPool.freeResource();
              aCheckOutList_.remove(_oPool);
            }
        }
    }


    // this method is always called by checker thread


    protected synchronized void cleanUp() throws Exception
    {
        // 檢查在 Spool 內的是否有過期的
        long now = System.currentTimeMillis();

        if( isDebug_ )
        {
            emisTracer.get(oContext_).info("start dbspool("+this+") cleanup:"+new Date(now));
        }

        // 先處理有 orphan 的情形
        checkListB(now);
        checkListA(now);

        if( isDebug_ )
          emisTracer.get(oContext_).info("total="+totalSize()+ "  min="+nMinSize_);


        // if total size < minsize , we should allocate new resource
        if( totalSize() < nMinSize_)
        {
            if( isDebug_ )
              emisTracer.get(oContext_).info("A");
            int _nAllocSize = nMinSize_ - totalSize() ;
            for(int i=0; i < _nAllocSize ; i++)
            {
              if( isDebug_ )
                emisTracer.get(oContext_).info("B");
              this.getSpoolObjectFromSource(false);
              if( isDebug_ )
                emisTracer.get(oContext_).info("C");
            }
        }

        if( aPooledList_.size() == 0 ) // if resource is empty
        {
              if( isDebug_ )
                emisTracer.get(oContext_).info("D");
            if ( totalSize()  < nMaxSize_ )
            {   // there is still room
              if( isDebug_ )
                emisTracer.get(oContext_).info("E");

                this.getSpoolObjectFromSource(false);
              if( isDebug_ )
                emisTracer.get(oContext_).info("F");

            }
        }

    }

/*-----------------------------info method ----------------------*/
    public int totalSize()
    {
        return aPooledList_.size() + aCheckOutList_.size();
    }

    public long getExpire()
    {
        return nExpire_;
    }

    public int getTimeOut()
    {
        return nTimeOut_;
    }

    public long getOrphan()
    {
        return nOrphan_;
    }

    public long getInterval()
    {
        return nInterval_;
    }

    public int getInitSize()
    {
        return nInitSize_;
    }

    public int getMaxSize()
    {
        return nMaxSize_;
    }

    public int getMinSize()
    {
        return nMinSize_;
    }

    public synchronized emisSpoolSnapShot getSnapShot()
    {
        emisSpoolSnapShot _oSnapShot = new emisSpoolSnapShot();
        // 這兩個值不能分開拿,所以要用 SnapShot 拿
        _oSnapShot.nCheckOutSize_ = aCheckOutList_.size();
        _oSnapShot.nPooledSize_ = aPooledList_.size();

        emisCommonEnum _oEnum = new emisCommonEnum();

        for(int i=0;i< _oSnapShot.nCheckOutSize_; i++)
        {
            _oEnum.add( ((emisAbstractProxy)aCheckOutList_.get(i)).getDescriptor() );
        }
        for(int i=0;i<_oSnapShot.nPooledSize_;i++)
        {
            _oEnum.add( ((emisAbstractProxy)aPooledList_.get(i)).getDescriptor() );
        }
        _oSnapShot.oPoolEnum_ = _oEnum;
        return _oSnapShot;
    }

    public synchronized void onLineDelete(String sObjectId) throws Exception
    {
      if (sObjectId == null ) throw new Exception("unable delete null object id onLine");
      if (totalSize() <= nMinSize_ )
      {
        throw new Exception("spool size already equals or below minsize");
      }
      int _nCheckSize = aCheckOutList_.size();
      for(int _nIdx = (_nCheckSize - 1) ;_nIdx >= 0  ; _nIdx--)
      {
        emisPoolable _oPool = (emisPoolable) aCheckOutList_.get( _nIdx);
        if( sObjectId.equals(_oPool.getPooledObject().toString()))
        {
           _oPool.freeResource();
           aCheckOutList_.remove(_oPool);
           return;
        }
      }
      _nCheckSize = aPooledList_.size();
      for(int _nIdx = (_nCheckSize - 1) ;_nIdx >= 0  ; _nIdx--)
      {
        emisPoolable _oPool = (emisPoolable) aPooledList_.get( _nIdx);
        if( sObjectId.equals(_oPool.getPooledObject().toString()))
        {
           _oPool.freeResource();
           aPooledList_.remove(_oPool);
           return;
        }
      }

    }

    public synchronized void onLineAllocate() throws Exception
    {
      if( totalSize() < nMaxSize_ )
      {
        getSpoolObjectFromSource(true);
      } else {
        throw new Exception("spool already in its maxsize");
      }
    }

    public boolean isInitReady(){
      return bInitReady_;
    }

/*--------------------------abstract method ------------------------*/
    abstract protected emisPoolable generateRealPooledObject(int nTimeOut) throws Exception;
}
