package com.emis.db;

import javax.servlet.ServletContext;

/**
 *  此 class 為麗嬰房專案網路不穩定,
 *  晚上 JDBC Connection 會斷掉,且不會觸發
 *  fatal Error Event, 所以 Connection Spool
 *  跟本沒機會 reset,所以需要一個背景程式在
 *  背景一直測試
 *  執行的週期設定在系統設定檔
 *  emis.pingdb.interval=300
 *  不設的話就不會執行
 */

public class emisPingDb extends Thread
{

  private ServletContext oContext_;
  private long nInterval_;

  /**
   * 傳進秒數
   */
  public emisPingDb(ServletContext oContext,int interval) throws Exception {
    super("emis ping db");
    oContext_ = oContext;
    nInterval_ = interval * 1000;
  }

  public void run () {
    while(true) {
      try {
        this.sleep(nInterval_);
        try {
          emisDb oDb = emisDb.getInstance(oContext_);
          try {
            oDb.dualTest();
          } finally {
            oDb.close();
            oDb = null;
          }
        } catch (Exception ignore) {
        }
      } catch (Exception ignore1) {
      }
    }
  }

}