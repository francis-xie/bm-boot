package com.emis.servlet.ht;
import com.emis.db.emisDb;
import com.emis.trace.emisTracer;

import javax.servlet.ServletContext;

public class emisHtThread extends Thread
{
  private ServletContext application;
  private static Object htlock = new Object();
  private static boolean isRunning = false;
  private static int htCounter=0;

  public emisHtThread (ServletContext application) {
    this.setName("盤點資料轉入");
    this.application = application;
  }
  public void run() {
    isRunning = true;
    try {
      while(htCounter > 0) {
        try {
          doDataTransfer();
        } catch (Exception e) {
          try {
            emisTracer.get(application).warning(this,e);
          } catch (Exception err) { }
        }
        synchronized (htlock) {
          htCounter--;
        }
      }
    } finally {
      isRunning = false;
    }
  }

  private void doDataTransfer() throws Exception
  {
    emisDb oDb = emisDb.getInstance(application);
    try {
      oDb.setDescription("盤點資料轉入-背景作業");
      oDb.prepareCall("begin execute epos_TransferHT; end;");
      oDb.executePrepareCall();
    } finally {
      oDb.close();
    }
  }

  public void increaseCount()
  {
    synchronized(htlock) {
      htCounter++;
    }
  }

  public boolean isConcurrentRunning()
  {
    return isRunning;
  }

}
