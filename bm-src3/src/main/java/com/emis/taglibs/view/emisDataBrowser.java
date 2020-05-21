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
 * @version 1.0 * lisa.huang 2010/05/07 新增顯示全部按鈕

 */

public class emisDataBrowser extends TagSupport{

  public int doStartTag() {
    try {
      ServletContext _oContext = pageContext.getServletContext();
      String _sImageRoot = emisFileMgr.getInstance(_oContext).getDirectory("images").getRelative();
      JspWriter out = pageContext.getOut();
      out.println("<IMG id='idTBLBrowFirstRec' value='首筆' alt='首筆' src='"+_sImageRoot+"firstrec.gif' border='0' onclick=emisIdbrowserMove('idbrowser',xmlData.recordset,1); ' TITLE='移至第一筆' style='cursor:hand'>");
      out.println("<IMG id='idTBLBrowPrevRec'  value='上筆' alt='上筆' src='"+_sImageRoot+"prevrec.gif'  border='0' onclick=emisIdbrowserMove('idbrowser',xmlData.recordset,2); ' TITLE='移至上一筆' style='cursor:hand'>");
      out.println("<IMG id='idTBLBrowListCul'  value='全部' alt='全部' src='"+_sImageRoot+"ListCul.gif'  border='0' onclick=emisIdbrowserMove('idbrowser',xmlData.recordset,5); ' TITLE='顯示全部' style='cursor:hand'>");

      out.println("<IMG id='idTBLBrowNextRec'  value='下筆' alt='下筆' src='"+_sImageRoot+"nextrec.gif'  border='0' onclick=emisIdbrowserMove('idbrowser',xmlData.recordset,3); ' TITLE='移至下一筆' style='cursor:hand'>");
      out.println("<IMG id='idTBLBrowLastRec'  value='末筆' alt='末筆' src='"+_sImageRoot+"lastrec.gif'  border='0' onclick=emisIdbrowserMove('idbrowser',xmlData.recordset,4); ' TITLE='移至最末筆' style='cursor:hand'>");
    } catch(Exception e){
      System.out.println("DataBrowserError");
    }
    return(SKIP_BODY);
  }
}