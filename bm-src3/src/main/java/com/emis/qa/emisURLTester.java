/*
 * $Header: /repository/src3/src/com/emis/qa/emisURLTester.java,v 1.1.1.1 2005/10/14 12:42:14 andy Exp $
 * Created on 2001年10月16日, 下午 12:06
 *
 * Copyright (c) EMIS Corp.
 */

package com.emis.qa;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 *
 * @author  jerry
 * @version 0.1
 * 
 * 讀取指定的參數檔, 依Tx.loop等值重覆測試(x=1..100)
 * Key:  Tx.loop=10
 *          Tx.sleepSeconds=5
 *           Tx.configFile=d:/test/trade/config.dat
 *           Tx.paramFile=d:/test/trade/param.dat
 * configFile: 如何測試的指示檔
 * paramFile: 網頁處理的命令檔
 *
 * emisURLTester --> emisURLTestlet (Runnable)
 */
public class emisURLTester /*implements java.lang.Runnable*/ {
  private String sConfigFile_, sParamFile_;
  private Hashtable htStat_;
  private int iCurrentLoop_;
  static int iCount_;
  
  public emisURLTester() {
/*    sConfigFile_ = sConfigFile;
    sParamFile_ = sParamFile;
    iCurrentLoop_ = iCurrentLoop;
    htStat_ = htStat;
 */
  }
  
  public static void main(String[] args) throws Exception {
    if( args.length < 1 ) {
      System.out.println("Usage:java com.emis.qa.emisURLTester TestFile");
      return;
    }
    
    emisURLTester _oTester = new emisURLTester();
    _oTester.test(args[0]);
  }
  
  /**
   * @param sTestFile
   * @throws Exception  */  
  public void test(String sTestFile) throws Exception {
    Properties _oTestFile = new Properties();
    InputStream in = new FileInputStream(sTestFile);
    try {
      _oTestFile.load(in);
    } finally {
      in.close();
    }
    
    Hashtable htStat_ = new Hashtable();
    Hashtable _htThread = new Hashtable();
    String _sKey = "";
    // T1.loop, T1.delaySeconds, T1.configFile, T1.paramFile
    for (int i=1; i <= 100; i++) {
      _sKey = "T" + i;
      String _sLoop = _oTestFile.getProperty(_sKey + ".loop");
      if (_sLoop == null) break;
      int _iLoop = Integer.parseInt(_sLoop);
      long _lSleepSeconds = Integer.parseInt(_oTestFile.getProperty(_sKey+".sleepSeconds","5"));
      String _sConfigFile = _oTestFile.getProperty(_sKey+".configFile");
      String _sParamFile = _oTestFile.getProperty(_sKey+".paramFile");
      if (_sConfigFile == null || _sParamFile == null) break;
      
      System.out.println("Running job " + i + " using " + _sConfigFile + ", " +
         _sParamFile + " sleeping " + _lSleepSeconds);
      for (int j=1; j <= _iLoop ; j++) {
        //System.out.println("  [" + j + "]");
        emisURLTestlet urlTest = new emisURLTestlet(_sConfigFile,_sParamFile, j, htStat_,this);
        Thread _oThread = new Thread(urlTest);
        iCount_++;        
        _oThread.setDaemon(true);
        _htThread.put(_sConfigFile+j, _oThread);
        //t.start();
        //Thread.currentThread().sleep(_lSleepSeconds * 1000);
      }
    }
    
    Enumeration eThread = _htThread.elements();
    while (eThread.hasMoreElements()) {
      Thread obj = (Thread) eThread.nextElement();
      obj.start();
    }
    //System.out.println("Counter=" + iCount_);
    /*while (iCount_ > 0) {
      System.out.println("counter=" + iCount_);
      synchronized (this) {
        this.wait();
      }
    }*/
    while (iCount_ > 0) {
      Thread.currentThread().sleep(1500);
    }
    /*System.out.println("activeCount="+Thread.activeCount());
    while (Thread.activeCount() > 1) {
      System.out.println("waiting...activeCount=" + Thread.activeCount());
      Thread.currentThread().sleep(1000);
    }
     */ 
    System.out.println("暫停以等待所有thread都已執行完畢");
    Thread.currentThread().sleep(10 * 1000);  // 暫停以等待所有thread都已執行完畢
    boolean _needStatistic = true;
    if (_needStatistic) {
      Hashtable _htSum = new Hashtable(htStat_.size());  // 某測試檔名之秒數合計
      Hashtable _htCount = new Hashtable(htStat_.size());  // 某測試檔名之個數合計

      Enumeration e = htStat_.keys();
      Object obj;
      long _lVal;
      long _lTime;
      int _iCount;
      while (e.hasMoreElements()) {
        _sKey = (String) e.nextElement();
        _lVal = ((Long) htStat_.get(_sKey)).longValue();
        System.out.println("Key=" + _sKey + " time=" + _lVal);
        _sKey = _sKey.substring(0, _sKey.indexOf("-"));
        obj = _htSum.get(_sKey);
        _lTime = (obj == null) ? 0 : ((Long) obj).longValue();
        _htSum.put(_sKey, new Long(_lTime + _lVal));

        obj = _htCount.get(_sKey);
        _iCount = (obj == null) ? 0 : ((Integer) obj).intValue();
        _htCount.put(_sKey, new Integer(_iCount + 1));
      }

      e = _htSum.keys();
      while (e.hasMoreElements()) {
        _sKey = (String) e.nextElement();
        _lVal = ((Long) _htSum.get(_sKey)).longValue();
        _iCount = ((Integer) _htCount.get(_sKey)).intValue();
        System.out.println("Key=" + _sKey + ", sum=" + _lVal + 
          ", Count=" + _iCount + ", Average=" + (_lVal/_iCount));
      }
    }  
    //    emisURLTest urlTest = new emisURLTest("C:\\config.dat","c:\\param.dat");
    System.out.println("The End!");
  }
}
