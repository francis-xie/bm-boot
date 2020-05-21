package com.emis.qa;

import com.emis.db.emisDb;
import com.emis.db.emisDbConnector;
import com.emis.db.emisDbMgr;
import com.emis.server.emisServer;
import com.emis.server.emisServerFactory;
import com.emis.spool.emisComplexSpool;

import javax.servlet.ServletContext;
import java.util.Random;

public class emisDbSpoolMulti implements Runnable
{


  private static emisComplexSpool oSpool_;
  private static Random _oRand = new Random();

  public static final int ERROR_HAPPENED = 3;
  public static final int LESS_THAN_ONE_SECOND = 2;
  public static final int MORE_THAN_ONE_SECOND = 1;
  public static final int MORE_THAN_FIVE_SECOND = 0;

  private ServletContext oContext_;


  private static long [] aTimeCounter_ = new long[4];


  public static void main(String[] args) throws Exception
  {
      if (args.length == 0 )
      {
          System.out.println("please set the thread number");
          return;
      }

      ServletContext _oContext = new emisServletContext();
      // init system
      emisServer oServer_ =emisServerFactory.createServer(_oContext,"C:\\wwwroot\\epos","c:\\resin\\epos.cfg",false);

      // get Spool
      emisDbMgr _oMgr = emisDbMgr.getInstance(_oContext);
      if( _oMgr == null )
      {
          System.out.println("Null Database Connector Manager");
          return;
      }
      emisDbConnector _oConnector = _oMgr.getConnector("pos");
      if( _oConnector == null )
      {
          System.out.println("Null Database Connector");
      }
      oSpool_ = (emisComplexSpool) _oConnector;
      oSpool_.setDebug(false);

      System.out.println("Expire:" + oSpool_.getExpire());
      System.out.println("TimeOut:" + oSpool_.getTimeOut());
      System.out.println("Orphan:"+ oSpool_.getOrphan());
      System.out.println("Interval:"+ oSpool_.getInterval());
      System.out.println("Max:" + oSpool_.getMaxSize() );
      System.out.println("Min:" + oSpool_.getMinSize() );
      System.out.println("Init:" + oSpool_.getInitSize() );

      int _nThreadNumber = Integer.parseInt(args[0]);

      for(int _nIdx = 0 ; _nIdx < _nThreadNumber ; _nIdx++)
      {
          Thread _oT = new Thread( new emisDbSpoolMulti(_oContext,_nIdx+1) );
          _oT.setDaemon(true);
          _oT.start();
          System.out.println("START THREAD" + (_nIdx+1));
      }
      StringBuffer _oBuf = new StringBuffer();
      while(true)
      {
          Thread.currentThread().sleep(30 *1000);
          _oBuf.setLength(0);
          _oBuf.append("------------Performance REPORT---------\n");
          _oBuf.append("less than 1 second:" + aTimeCounter_[ LESS_THAN_ONE_SECOND ] + "\n");
          _oBuf.append("less than 5 second:" + aTimeCounter_[ MORE_THAN_ONE_SECOND ] + "\n");
          _oBuf.append("more than 5 second:" + aTimeCounter_[ MORE_THAN_FIVE_SECOND ] + "\n");
          _oBuf.append("error happened Num:" + aTimeCounter_[ ERROR_HAPPENED ] + "\n");
          _oBuf.append("------------End Performance REPORT-----\n");
          output(_oBuf.toString());
      }

  }


  private String sThreadID_;

  public emisDbSpoolMulti(ServletContext oContext,int nThreadID)
  {
      oContext_ = oContext;
      try {
         sThreadID_ = "Thread" + String.valueOf(nThreadID);
      }catch(Exception ignore) {
         sThreadID_ = "Thread_Unknow";
      }

  }

  public void run()
  {
      StringBuffer _oBuf = new StringBuffer();
      try {
          for(;;)
          {
              _oBuf.setLength(0);

              _oBuf.append("--------" + sThreadID_ + " Start----------\n");
              try {
                  long now = System.currentTimeMillis();
                  emisDb _oDb = emisDb.getInstance(oContext_,"pos");
                  long end = System.currentTimeMillis();

                  if( _oDb == null )
                  {
                      _oBuf.append("get Null emisDb\n");
                  } else {
                      _oDb.executeQuery("SELECT V_NAME FROM VENDOR");
                      while( _oDb.next() ) ;
                      _oDb.close();
                  }

                  if( _oDb != null )
                  {
                      long milisecond = end - now;
                      _oBuf.append("Get Connection in :" + milisecond + " miliseconds\n");

                      synchronized( aTimeCounter_ )
                      {
                          if (milisecond > 5000)
                              aTimeCounter_[ MORE_THAN_FIVE_SECOND ]++;
                          else
                          if (milisecond >= 1000 )
                              aTimeCounter_[ MORE_THAN_ONE_SECOND ]++;
                          else
                              aTimeCounter_[ LESS_THAN_ONE_SECOND ]++;
                      }
                  }

              }catch(Exception e) {
                  _oBuf.append("Exception:" + e.getMessage() + "\n");
                  aTimeCounter_ [ ERROR_HAPPENED ]++;
              }

              // 睡 1 到 10 秒, 不一定

              long _nSleepTime = 10;
              synchronized (_oRand)
              {
                  _nSleepTime = _oRand.nextInt(9)+1 ;
              }
              _oBuf.append("Sleep for " + _nSleepTime + " seconds\n");
              _oBuf.append("----------------------------------------------\n");

              output(_oBuf.toString());
              Thread.currentThread().sleep(_nSleepTime * 1000);

          }

      } catch (Exception ignore ) {

      }
  }

  public static synchronized void output(String sStr)
  {
      System.out.print(sStr);
  }

}