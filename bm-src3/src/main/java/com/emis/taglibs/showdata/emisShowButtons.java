/* $Id: emisShowButtons.java 4 2015-05-27 08:13:47Z andy.he $

 *

 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.

 */

package com.emis.taglibs.showdata;



import com.emis.util.emisLangRes;

import javax.servlet.jsp.JspWriter;

import javax.servlet.jsp.tagext.TagSupport;

import java.io.IOException;



/**

 * 顯示功能按鈕.

 *

 * @author Jerry

 * @version 2004/08/25

 */

public class emisShowButtons extends TagSupport {

  private String name;

  private String type;

  private String styleClass;

  private String colspan;

  private String height;



  /**

   * start tag.

   *

   * @return

   */

  public int doStartTag() {

    try {

      JspWriter out = pageContext.getOut();

      //out.println("name=" + name);

      //out.println("height=" + height);

      if ("YN".equalsIgnoreCase(type)) {

        genYNButtons(out);

      } else if ("SC".equalsIgnoreCase(type)) {

        genSaveCancelButtons(out);

      }

    } catch (Exception e) {

      System.err.println("showdata:emisShowButtons");

    }

    return (SKIP_BODY);

  }



  private void genYNButtons(JspWriter out) throws IOException {

    out.println("<tr height='" + getHeight() + "'>");

    out.println("<td align='center' colspan='" + getColspan() + "' class='" + getStyleClass() + "'>");

//    out.println("<button id='btnOK' accesskey='Y' title='確定:[F10]' class='OKButton'>");
    out.println("<button type='button' id='btnOK' accesskey='Y' title='" + getMessage("SB_BTN_OK_TITLE") + "' class='OKButton'>");

//    out.println("  <img src='../../images/save.gif'></img> 確定(<u>Y</u>)");
    out.println("  <img src='../../images/save.gif'></img>" + getMessage("SB_BTN_OK"));

    out.println("</button>&nbsp;");

//    out.println("<button id='btnClose' accesskey='C' title='取消:[Esc]' class='ExitButton'>");
    out.println("<button type='button' id='btnClose' accesskey='C' title='" + getMessage("SB_BTN_CLOSE_TITLE") + "' class='ExitButton'>");

//    out.println("  <img src='../../images/cancel.gif'></img> 取消(<u>C</u>)");
    out.println("  <img src='../../images/cancel.gif'></img>" + getMessage("SB_BTN_CLOSE"));

    out.println("</button>");

    out.println("</td>");

    out.println("</tr>");

  }



  private void genSaveCancelButtons(JspWriter out) throws IOException {

    out.println("<tr height='" + getHeight() + "'>");

    out.println("<td align='center' colspan='" + getColspan() + "' class='" + getStyleClass() + "'>");

//    out.println("<button id='btnSave' accesskey='S' title='儲存:[F10]' class='OKButton'>");
    out.println("<button type='button' id='btnSave' accesskey='S' title='" + getMessage("SB_BTN_SAVE_TITLE") + "' class='OKButton'>");

//    out.println("  <img src='../../images/save.gif'></img> 儲存(<u>S</u>)");
    out.println("  <img src='../../images/save.gif'></img>" + getMessage("SB_BTN_SAVE"));

    out.println("</button>&nbsp;");

//    out.println("<button id='btnCancel' accesskey='C' title='取消:[Esc]' class='ExitButton'>");
    out.println("<button type='button' id='btnCancel' accesskey='C' title='" + getMessage("SB_BTN_CANCEL_TITLE") + "' class='ExitButton'>");

//    out.println("  <img src='../../images/cancel.gif'></img> 取消(<u>C</u>)");
    out.println("  <img src='../../images/cancel.gif'></img>" + getMessage("SB_BTN_CANCEL"));

    out.println("</button>");

    out.println("</td>");

    out.println("</tr>");

  }

  /**
   * 取得当前语系内容
   * @param key
   * @return
   */
  public String getMessage(String key) {
    emisLangRes _oLang = null;
    try {
      _oLang = emisLangRes.getInstance(pageContext.getServletContext().getRealPath(emisLangRes.ResourceSubPath));
      _oLang.setLanguage((String) pageContext.getSession().getAttribute("languageType"));
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    return _oLang == null ? "" : _oLang.getMessage("Application", key);
  }


  public String getName() {

    return name;

  }



  public void setName(String name) {

    this.name = name;

  }



  public String getStyleClass() {

    return styleClass==null ? "表格_奇數列" : styleClass;

  }



  public void setStyleClass(String aClass) {

    styleClass = aClass;

  }



  public String getColspan() {

    return colspan==null ? "2" : colspan;

  }



  public void setColspan(String colspan) {

    this.colspan = colspan;

  }



  public String getType() {

    return type;

  }



  public void setType(String type) {

    this.type = type;

  }



  public String getHeight() {

    return height==null? "40" : height;

  }



  public void setHeight(String height) {

    this.height = height;

  }

}