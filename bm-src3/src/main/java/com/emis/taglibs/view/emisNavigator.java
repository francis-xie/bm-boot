package com.emis.taglibs.view;
import com.emis.file.emisFileMgr;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class emisNavigator extends TagSupport{

  public int doStartTag() {
    try {
      ServletContext _oContext = pageContext.getServletContext();
      String _sImageRoot = emisFileMgr.getInstance(_oContext).getDirectory("images").getRelative();

      JspWriter out = pageContext.getOut();
      out.println("<IMG id='idTBLNaviFirstRec' value='首筆' alt='首筆' src='"+_sImageRoot+"firstrec.gif' border='0' onclick='NaviFirstRecFun(idTBLRec);' TITLE='移至第一筆記錄' style='cursor:hand'>");
      out.println("<IMG id='idTBLNaviPrevPage' value='上頁' alt='上頁' src='"+_sImageRoot+"prevpage.gif' border='0' onclick='NaviPrevPageFun(idTBLRec);' TITLE='移至上一頁記錄' style='cursor:hand'>");
      out.println("<IMG id='idTBLNaviPrevRec'  value='上筆' alt='上筆' src='"+_sImageRoot+"prevrec.gif'  border='0' onclick='NaviPrevRecFun(idTBLRec);' TITLE='移至上一筆記錄' style='cursor:hand'>");
      out.println("<IMG id='idTBLNaviNextRec'  value='下筆' alt='下筆' src='"+_sImageRoot+"nextrec.gif'  border='0' onclick='NaviNextRecFun(idTBLRec);' TITLE='移至下一筆記錄' style='cursor:hand'>");
      out.println("<IMG id='idTBLNaviNextPage' value='下頁' alt='下頁' src='"+_sImageRoot+"nextpage.gif' border='0' onclick='NaviNextPageFun(idTBLRec);' TITLE='移至下一頁記錄' style='cursor:hand'>");
      out.println("<IMG id='idTBLNaviLastRec'  value='末筆' alt='末筆' src='"+_sImageRoot+"lastrec.gif'  border='0' onclick='NaviLastRecFun(idTBLRec);' TITLE='移至最末筆記錄' style='cursor:hand'>");
    } catch(Exception e){
      System.out.println("HelloWord Error");
    }
    return(SKIP_BODY);
  }
}