/* $Id: emisReloader.java 4 2015-05-27 08:13:47Z andy.he $ * * Copyright (c) 2004 EMIS Corp. All Rights Reserved. */package com.emis.servlet;import com.emis.db.*;import com.emis.schedule.emisScheduleMgr;import com.emis.util.emisLangRes;import com.emis.util.emisUtil;import javax.servlet.ServletContext;import javax.servlet.ServletException;import javax.servlet.http.HttpServlet;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import java.io.IOException;import java.io.PrintWriter;import java.util.Iterator;/** * 資源重載入程式，目前可處理emisSQLCache及emisProp Tables, Sched table. * * @author Robert * @version 2004/08/09 Jerry: Refactor; add reloadFieldFormat */public class emisReloader extends HttpServlet {  /**   * Servlet service.   * @param request   * @param response   * @throws ServletException   * @throws IOException   */  public void service(HttpServletRequest request, HttpServletResponse response)      throws ServletException, IOException {	  response.setContentType("text/html;charset=UTF-8");    PrintWriter out = response.getWriter();    ServletContext application = this.getServletContext();    String _sReloadTarget = request.getParameter("target");    try {      out.println("<html>");      out.println("<style>");      out.println("  TABLE     { font: 8pt Arial; }");      out.println("  H1        { font:12pt Arial; }");      out.println("  .odd_row  { background-color:'99ccff'; }");      out.println("  .even_row { background-color:'ffffcc'; }");      out.println("</style>");      out.println("<body>");      out.println("<table width='100%' border='1'>");      // SQL Cache      if ("sqlcache".equalsIgnoreCase(_sReloadTarget)) {        reloadSQLCache(application, out);      } else if ("prop".equalsIgnoreCase(_sReloadTarget)) {        reloadEmisProp(application, out);      } else if ("sched".equalsIgnoreCase(_sReloadTarget)) {        emisScheduleMgr.getInstance(application).reload(out);        out.println("<h1>排程資料已經重新載入.</h1>");      } else if ("fieldformat".equalsIgnoreCase(_sReloadTarget)) {        reloadFieldFormat(application, out);      }else if ("properties".equalsIgnoreCase(_sReloadTarget)) {        reloadProperties(application, out);      } else {        out.println("<tr class='odd_row'><td>不支援的參數　：" + _sReloadTarget + "</td></tr>");        out.println("<tr class='odd_row'><td>目前支援參數為：'SQLCache' & 'Prop'</td></tr>");        out.println("<tr class='odd_row'><td>例：http://hostname/application/" +            "servlet/com.emis.servlet.emisReloader?target=prop</td></tr>");      }    } catch (Exception e) {      e.printStackTrace(out);    } finally {      out.println("</table>");      out.println("</body>");      out.println("</html>");    }  }  private void reloadSQLCache(ServletContext application, PrintWriter out)      throws Exception {    String _sSQLField1 = "SQLNAME", _sSQLField2 = "SQLCMD",        _sSQLField3 = "SQLFMT", _sSQLField4 = "LASTUPDATE";    emisSQLCache.reload(application);    out.println("<tr align='center' class='odd_row'><td colspan='4'>" +        "<h1>SQL Cache 重新載入成功</h1></td></tr>");    emisDb oDb = emisDb.getInstance(application);    oDb.setDescription("system:sqlCache");    oDb.executeQuery("SELECT * FROM " + emisSQLCache.SQLCACHETABLE);    //emisRowSet _oCaches = new emisRowSet(oDb);    out.println("<tr align='center' class='odd_row'><td>" +        _sSQLField1 + "</td><td>" + _sSQLField2 + "</td><td>" +        _sSQLField3 + "</td><td>" + _sSQLField4 + "</td></tr>");    try {      while (oDb.next()) {        out.println("<tr class='even_row'><td>" + oDb.getString(_sSQLField1));        out.println("</td><td>" + oDb.getString(_sSQLField2));        out.println("</td><td>" + oDb.getString(_sSQLField3));        out.println("</td><td>" + oDb.getString(_sSQLField4) + "</tr>");      }    } catch (Exception e) {      out.println("<tr align='center' class='even_row'><td colspan='4'>" +          "<h1>SQLCache Table Open Error</h1></td></tr>");    } finally {      oDb.close();    }  }  /**   * reload EmisProp.   * @param application   * @param out   * @throws Exception   */  private void reloadEmisProp(ServletContext application, PrintWriter out)      throws Exception {    String _sField1 = "NAME", _sField2 = "VALUE";    emisProp.reload(application);    out.println("<tr align='center' class='odd_row'><td colspan='2'>" +        "<h1>EMIS Property 重新載入成功</h1></td></tr>");    emisDb oDb = emisDb.getInstance(application);    out.println("<tr align='center' class='odd_row'><td>" +        _sField1 + "</td><td>" + _sField2 + "</td></tr>");    try {      oDb.setDescription("system:get EMIS Properties");      oDb.executeQuery("SELECT * FROM EMISPROP");      emisDb oDataDb = emisDb.getInstance(application);      try {        while (oDb.next()) {          out.println("<tr class='even_row'><td width='30%'>" + oDb.getString(_sField1));          out.println("</td><td>" + oDb.getString(_sField2) + "</td></tr>");        }      } finally {        oDataDb.close();      }    } catch (Exception e) {      out.println("<tr align='center' class='even_row'><td>" +          "<h1>EMIS Property Table Open Error</h1></td></tr>");    } finally {      oDb.close();    }  }  private void reloadFieldFormat(ServletContext application, PrintWriter out)      throws Exception {    emisFieldFormat fmt = emisFieldFormat.getInstance(application);    fmt.reload(application);    out.println("<tr align='center' class='odd_row'><td colspan='6'>" +        "<h1>FieldFormat 重新載入成功</h1></td></tr>");    out.println("<tr class='even_row'><td>序</td><td>欄名</td><td>欄寬</td>" +        "<td>檢核方式</td>" + "<td>輸入格式</td><td>左補零?</td>");    Iterator itr = fmt.getValues();    int _iCount = 0;    while (itr.hasNext()) {      emisFieldFormatBean bean = (emisFieldFormatBean) itr.next();      _iCount++;      out.println("<tr><td>" + _iCount + "</td>");      out.println("<td>" + bean.getType() + "</td>");      out.println("<td>" + bean.getMaxLen() + "</td>");      out.println("<td>" + bean.getValidation() + "</td>");      out.println("<td>" + bean.getPicture() + "</td>");      out.println("<td>" + bean.getLeftZero() + "</td></tr>");    }    fmt = null;  }  private void reloadProperties(ServletContext application, PrintWriter out) throws Exception{    emisLangRes lang = emisLangRes.getInstance(application);    lang.reload();     out.println("<tr align='center' class='odd_row'><td colspan='6'>" +        "<h1>Properties 重新載入成功</h1></td></tr>");  }  /**   * doGet.   * @param p0   * @param p1   * @throws ServletException   * @throws IOException   */  public void doGet(HttpServletRequest p0, HttpServletResponse p1)      throws ServletException, IOException {    service(p0, p1);  }  /**   * doPost.   * @param p0   * @param p1   * @throws ServletException   * @throws IOException   */  public void doPost(HttpServletRequest p0, HttpServletResponse p1)      throws ServletException, IOException {    service(p0, p1);  }}