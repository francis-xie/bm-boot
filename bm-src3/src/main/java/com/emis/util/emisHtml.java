/*
 * $Header: /repository/src3/src/com/emis/util/emisHtml.java,v 1.1.1.1 2005/10/14 12:43:20 andy Exp $
 *
 * Copyright (c) EMIS Corp.
 */
package com.emis.util;

import com.emis.db.emisDb;
import com.emis.server.emisServerFactory;
import com.emis.test.emisServletContext;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import java.io.PrintWriter;

/**
 * 與HTML輸出相關的公用程式.<br>
 * Ex. &lt;% emisHtml.option(application, out,"select S_NO,S_NAME from store","%1 %2"); %gt;<br>
 * Ex. &lt;%= emisHtml.option(application, "select S_NO,S_NAME from store","%1 %2") %gt;<br>
 * 注意：　欄位必須為字串型態
 * ------------ 示範JSP ---------------
 <%@ page contentType="text/html; charset=UTF-8" %>

 <%@ page import="com.emis.util.*" %>
 <select>
 <%
 emisHtml.option(application, out,
 "select S_NO,S_NAME from store","%1 %2");
 %>
 </select>
 <hr>
 <select>
 <%=
 emisHtml.option(application,
 "select S_NO,S_NAME from store","%1 %2")
 %>
 </select>
 */
public class emisHtml {
    /**
     * 輸出&lt;select&gt;的option選項清單內容. 輸出物件用JspWriter或PrintWriter都可以.
     * Ex. emisHtml.option(application, out,"select S_NO,S_NAME from store","%1 %2");
     */
    public static void option(ServletContext oContext, JspWriter out,
                              String sSQL, String sPattern) throws Exception {
        option(oContext, new PrintWriter(out), sSQL, sPattern);
    }

    /**
     * 輸出&lt;select&gt;的option選項清單內容. 輸出物件用JspWriter或PrintWriter都可以.
     * Ex. emisHtml.option(application, out,"select S_NO,S_NAME from store","%1 %2", "%2");
     */
    public static void option(ServletContext oContext, JspWriter out,
                              String sSQL, String sPattern, String sValue) throws Exception {
        option(oContext, new PrintWriter(out), sSQL, sPattern, sValue);
    }

    /**
     * 輸出&lt;select&gt;的option選項清單內容. 輸出物件用JspWriter或PrintWriter都可以<br>
     * Ex. emisHtml.option(application, out,"select S_NO,S_NAME from store","%1 %2");
     */
    public static void option(ServletContext oContext, PrintWriter out,
                              String sSQL, String sPattern) throws Exception {
        String _sOutput = _getOptions(oContext, sSQL, sPattern, "%1");
        out.println(_sOutput);
    }

    /**
     * 輸出&lt;select&gt;的option選項清單內容. 輸出物件用JspWriter或PrintWriter都可以<br>
     * Ex. emisHtml.option(application, out,"select S_NO,S_NAME from store","%1 %2", "%1");
     * 最後參數是option 的value要取自第幾個欄位. "%2"取第二個欄位, "%1"則為第一個欄位
     */
    public static void option(ServletContext oContext, PrintWriter out,
                              String sSQL, String sPattern, String sValue) throws Exception {
        String _sOutput = _getOptions(oContext, sSQL, sPattern, sValue);
        out.println(_sOutput);
    }

    /**
     * 傳回option清單字串之內容. JSP中應使用&lt;%= ... &gt;方式接此輸出字串.
     */
    public static String option(ServletContext oContext, String sSQL,
                                String sPattern) throws Exception {
        return _getOptions(oContext, sSQL, sPattern, "%1");
    }

    /**
     * 傳回option清單字串之內容. JSP中應使用&lt;%= ... &gt;方式接此輸出字串.
     */
    public static String option(ServletContext oContext, String sSQL,
                                String sPattern, String sValue) throws Exception {
        return _getOptions(oContext, sSQL, sPattern, sValue);
    }

    /**
     * 依據SQL命令與輸出的樣式來產生需要的option字串.
     * @param sSQL SQL命令; 例: "select S_NO,S_NAME from store"
     * @param sPattern 輸出樣式; 例: "%1 %2", 表示代碼與名稱間空一個空白
     * @param sValue ＜option value="%1"＞或"%2"表第一個或第二個欄位
     */
    private static String _getOptions(ServletContext oContext, String sSQL,
                                      String sPattern, String sValue) throws Exception {
        String _sHtml = null;
        String _sOutput = "";
        emisDb _oDb = emisDb.getInstance(oContext);
        try {
            _oDb.prepareStmt(sSQL);
            _oDb.prepareQuery();
            while (_oDb.next()) {
                int _iColumnCount = _oDb.getColumnCount();
                _sHtml = "<option value='" + sValue + "'>" + sPattern;
                // 將Pattern中的%1, %2等逐一替換成第１，第２欄位之值
                for (int i = 1; i <= _iColumnCount; i++) {
                    String _sData = _oDb.getString(i).trim();
                    _sHtml = emisUtil.stringReplace(_sHtml, "%" + i, _sData, "a");
                }
                _sOutput += _sHtml;
            }
        } catch (Exception e) {
            oContext.log("[emisHtml._getOptions] " + e.getMessage());
        } finally {
            if (_oDb != null) _oDb.close();
        }
        return _sOutput;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Initialization...");
        emisServletContext servlet = new emisServletContext();
        javax.servlet.ServletContext _oContext = (javax.servlet.ServletContext) servlet;
        System.out.println("After new servlet");
        emisServerFactory.createServer(servlet, "c:\\wwwroot\\wtn", "c:\\resin3X\\wtn.cfg", true);
        System.out.println("After createServer()");
        emisHtml.option(_oContext, new PrintWriter(System.out),
                "select S_NO,S_NAME from store", "%1 %2");
        System.out.println("-----------------------------");
        System.out.println(emisHtml.option(_oContext, "select S_NO,S_NAME from store", "%1 %2"));
        System.out.println("-----------value為第二個參數");
        System.out.println(emisHtml.option(_oContext,
                "select S_NO,S_NAME from store", "%1 %2", "%2"));
        System.out.println("-----------多個欄位");
        System.out.println(emisHtml.option(_oContext,
                "select S_NO,S_NAME,S_TEL1 from store", "%1 %2$$%3 %1", "%3:%2"));
        System.out.println("after option...");
    }
}
