/**
 *  $Header: /repository/src3/src/com/emis/schedule/trans/emisTransferDesc.java,v 1.1.1.1 2005/10/14 12:42:51 andy Exp $
 */
package com.emis.schedule.trans;

import com.emis.util.*;
import com.emis.schedule.*;
import java.io.*;


public class emisTransferDesc implements emisTaskDescriptor
{

  public emisTransferDesc() { }

  private String sSrcDb_;
  private String sSrcTable_;

  private String sTarDb_;
  private String sTarTable_;

  private String sMsg_;

  public void setDb(String sSrcDb, String sTarDb)
  {
    sSrcDb_ = sSrcDb;
    sTarDb_ = sTarDb;
  }
  public void setTable(String sSrcTable,String sTarTable)
  {
    sSrcTable_ = sSrcTable;
    sTarTable_ = sTarTable;
  }

  private Exception e_;

  private boolean hasError_;

  public boolean hasError() {
    return hasError_;
  }
  public void setException(Exception e)
  {
    e_ = e;
    hasError_ = true;
  }
  public Exception getError()
  {
    return e_;
  }

  private  boolean isEnd_ = false;

  public void setEnd()
  {
    isEnd_ = true;
  }

  public boolean isFinished()
  {
    return isEnd_;
  }

  public void setMsg(String sStr) {
    sMsg_ = sStr;
  }

  /**
   *  描述轉檔程式處理過程(被外部網頁呼叫)
   */
  public void descript(Writer w)
  {
    PrintWriter out = new PrintWriter(w);
    if( !isEnd_ )
    {
      if( sSrcDb_ != null )
        out.println("來源DB："+ sSrcDb_ +"."+((sSrcTable_==null) ? "":sSrcTable_)+"<BR>");

      if( sTarDb_ != null)
        out.println("目的DB："+ sTarDb_ +"."+((sTarTable_==null) ? "":sTarTable_)+"<BR>");


      if( sMsg_ != null )
        out.println("筆　數："+ sMsg_ + "<BR>");

    } else {
      out.println("作業結束<BR>");
    }

    if( e_ != null )
    {
      out.println("Exception=<BR>");
      out.println(emisUtil.getStackTrace(e_));
    }
    out.flush();
    out = null;
  }
}
