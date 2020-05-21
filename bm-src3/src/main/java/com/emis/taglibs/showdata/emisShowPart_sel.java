/* $Id: emisShowPart_sel.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.BufferedReader;

/**
 * 輸出part_sel的JavaScript.
 * test script: /jsp/test/part_sel.jsp; 執行後檢視原始檔.
 *
 * @author Jerry
 * @version 2004/08/28
 */
public class emisShowPart_sel extends TagSupport {
  private String name;
  private String type;
  /** 第一個欄位 (輸入欄位, 如: QRY_P_NO1) */
  private String field1;
  /** 第二個欄位 (游標移往之欄位, 如: QRY_P_NO2) */
  private String field2;

  /**
   * start tag.
   *
   * @return
   */
  public int doStartTag() {
    try {
      JspWriter out = pageContext.getOut();
      ServletContext oContext = pageContext.getServletContext();
      //out.println("name=" + name);
      //out.println("height=" + height);
      genScript(oContext, out);
    } catch (Exception e) {
      System.err.println("emisShowPart_sel.doStartTag: " + e.getMessage());
    }
    return (SKIP_BODY);
  }

  private void genScript(ServletContext oContext, JspWriter out) throws Exception {
    emisFileMgr _oFileMgr = emisFileMgr.getInstance(oContext);
    BufferedReader _oReader = null;
    try {
      emisDirectory _oDir = _oFileMgr.getDirectory("root").subDirectory("WEB-INF")
          .subDirectory("SCRIPTS");
      emisFile _oFile = _oDir.getFile("part_sel.txt");
      _oReader = _oFile.getReader();
      String _sLine = null;
      StringBuffer _sbText = new StringBuffer(1024);
      while ((_sLine = _oReader.readLine()) != null) {
        _sLine = emisUtil.stringReplace(_sLine, "%FIELD1%", getField1(),"ia");
        _sLine = emisUtil.stringReplace(_sLine, "%FIELD2%", getField2(),"ia");
        _sbText.append(_sLine).append(emisUtil.LINESEPARATOR);
      }
      out.println(_sbText.toString());
    } catch (Exception e) {
      System.err.println("emisShowPart_sel.genScript: " + e.getMessage());
    } finally {
      if (_oReader != null) {
        _oReader.close();
        _oReader = null;
      }
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /** 第一個欄位 (輸入欄位, 如: QRY_P_NO1).
   *
   * @return
   */
  public String getField1() {
    return field1;
  }

  /** 第一個欄位 (輸入欄位, 如: QRY_P_NO1).
   *
   * @param field1
   */
  public void setField1(String field1) {
    this.field1 = field1;
  }

  /** 第二個欄位 (游標移往之欄位, 如: QRY_P_NO2).
   *
   * @return
   */
  public String getField2() {
    return field2;
  }

  /** 第二個欄位 (游標移往之欄位, 如: QRY_P_NO2).
   *
   * @param field2
   */
  public void setField2(String field2) {
    this.field2 = field2;
  }
}