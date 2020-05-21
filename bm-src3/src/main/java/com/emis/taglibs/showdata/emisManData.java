/* $Id: emisManData.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 顯示建檔日期等欄位.
 *
 * @author emisJacky
 * @version 1.0
 * @version 2004/08/25 Jerry: add comment
 */
public class emisManData extends TagSupport {
  /**
   * start tag.
   *
   * @return
   */
  public int doStartTag() {
    try {
      JspWriter _out = pageContext.getOut();
      _out.write("    <table  width='100%' height='100%'  border='0' cellspacing='1' cellpadding='2' >");
      _out.write("      <tr>");
      _out.write("        <td class='表格_欄_文字' NOWRAP>建檔日期</td>");
      _out.write("        <td class='表格_欄_資料' width='20%'>");
      _out.write("          <span datasrc='#xmlData' DataFld='CRE_DATE' id='spaCRE_DATE'></span>");
      _out.write("        </td>");
      _out.write("        <td class='表格_欄_文字' NOWRAP >建 檔 人</td>");
      _out.write("        <td class='表格_欄_資料' width='20%'>");
      _out.write("          <span datasrc='#xmlData' DataFld='CRE_USER' id='spaCRE_USER'></span>");
      _out.write("        </td>");
      _out.write("    <td class='表格_欄_文字' NOWRAP >修改日期</td>");
      _out.write("    <td class='表格_欄_資料' width='20%'>");
      _out.write("      <span datasrc='#xmlData' DataFld='UPD_DATE' id='spaUPD_DATE'></span>");
      _out.write("    </td>");
      _out.write("    <td class='表格_欄_文字' NOWRAP>修 改 人</td>");
      _out.write("    <td class='表格_欄_資料' width='20%'>");
      _out.write("      <span datasrc='#xmlData' DataFld='UPD_USER' id='spaUPD_USER'></span>");
      _out.write("    </td>");
      _out.write("  </tr>");
      _out.write("</table>");
    } catch (Exception e) {
      System.err.println("showdata:emisManManData");
    }
    return (SKIP_BODY);
  }
}