package com.emis.schedule;


import com.emis.business.*;
import com.emis.schedule.emisTask;

/**
 * Created by IntelliJ IDEA.
 * User: zhong.xu
 * Date: 2009-2-26
 * Time: 14:44:20
 * To change this template use File | Settings | File Templates.
 * Track+[23125] 2013/06/19 Austen.liao ÓA‘O¡–”°-∞lÀÕ≈≈≥ÃemisPrePrint
 */
public class emisPrePrint extends emisTask {
  private String defaultEncoding = System.getProperty("file.encoding");

  public void runTask() {
    if (this.sParameter_ == null || this.oContext_ == null)
      return;

    try {
      String  [] params = this.sParameter_.split("#");
      emisPrePrintUtil util = null;
      System.setProperty("file.encoding", "UTF-8");
      if(params[5].equalsIgnoreCase("quiee")){
        util = new emisPrePrintQuiee(this.oContext_,oRequest_);
      } else{
        util = new emisPrePrintUtil(this.oContext_,oRequest_);
      }
      util.createReport(params);
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }finally {
      System.setProperty("file.encoding", defaultEncoding);
    }
  }  
}
