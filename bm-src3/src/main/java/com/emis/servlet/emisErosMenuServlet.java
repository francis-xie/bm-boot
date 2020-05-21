/*
* $Header: /repository/src3/src/com/emis/servlet/emisErosMenuServlet.java,v 1.1.1.1 2005/10/14 12:42:53 andy Exp $
*
* Created on 2001年10月9日, 下午 5:55
* Copyright (c) EMIS Corp.
*/
package com.emis.servlet;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.http.emisHttpUtil;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * emisMenuServlet:接收index.jsp送來的使用者帳號與密碼,做認證與登入系統之處理.
 * 登入成功後會自動跳往MENUS指定欄位之值, 否則跳回index.jsp.
 * @author Jerry
 * @version 1.0 2001/10/10, 1.1 2001/11/03
 *
 * 測試:
 * /epos/index.jsp 正常login與使用錯誤的密碼登入
 * /les/index.jsp 正常login與使用錯誤的密碼登入
 * /les/jsp/web/index.jsp 正常login與使用錯誤的密碼登入
 * /les/index.jsp?userid=root&passwd=123&nextpage=jsp/mtn/part.jsp 自動登入
 *
 * 2004/06/28 Jacky 修正IPADDRESS="*"字串時未判斷的問題
 * 2004/07/23 Jacky 修正當EROS_LOGIN='0'時的判斷規則
 */
public class emisErosMenuServlet extends HttpServlet {

  /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, java.io.IOException {
    ServletContext application = getServletContext();

	response.setContentType("text/html;charset=UTF-8");
    java.io.PrintWriter out = response.getWriter();
    //out.println("<center><font color='blue'>資料處理中...</font><center>");

    String _sID = request.getParameter("ID");
    String _sPasswd = request.getParameter("PASSWD");
    String _sS_NO = request.getParameter("S_NO");
    String _sCompanyNo = request.getParameter("COMPANY_NO");
    if (_sCompanyNo != null && "".equals(_sCompanyNo)) _sCompanyNo = null;
    String _sUserType = request.getParameter("USER_TYPE");

    if (_sUserType != null && "".equals(_sUserType)) _sUserType = null;
    //- log( )會將文字寫至 /resin/log/error.log
    //-log("ID=" + _sID + ",Passwd=" + _sPasswd + ",SNO=" + _sS_NO);
    boolean _isOK = false;
    emisUser _oUser = null;
    String _sNextPage = null;
    String _sRoot = emisHttpUtil.getWebappRoot(request);
    String _sDebug = request.getParameter("DEBUG");
    boolean _isDebug = _sDebug != null;
    if (_isDebug) {
      out.println("ID=" + _sID + ",Passwd=" + _sPasswd + ",SNO=" + _sS_NO);
      out.println("company=" + _sCompanyNo + ",usertype=" + _sUserType + "<br>");
    }

    try {
      if (_sUserType == null && _sCompanyNo == null) {
        //-out.println("start to login");
        //if (_sS_NO != null && "".equals(_sS_NO)) _sS_NO = null;
        // _sS_NO為"-1"表示不使用S_NO來找user; for Yes
        _oUser = emisCertFactory.login(application, request, _sS_NO, null, _sID, _sPasswd);
        //-out.println("end of login");
      } else {
        String _sExtraInfo = null;
        if (_sUserType == null)
          _sExtraInfo = "C," + _sCompanyNo;
        else if (_sCompanyNo == null)
          _sExtraInfo = "U," + _sUserType;
        //out.println("extra=" + _sExtraInfo);
        _oUser = emisCertFactory.login(application, request, _sS_NO, _sExtraInfo, _sID, _sPasswd);
      }
      _sUserType = Integer.toString(_oUser.getUserType());
      String _sCheckIP = _oUser.getProperty("USERIP");
      _isOK = isExactUserIP(_sCheckIP, request.getRemoteAddr());

      //$ 2003.1.18 新增 User_IP Check IP Address
      if (_isOK) {
        String _sUserIP = _oUser.getProperty("USER_GIP");

        String s_no = _oUser.getSNo();
        _isOK = checkIPADDR(s_no, request.getRemoteAddr(), _sUserType);       //zoe add on 20040621 系統權限修改檢核方式

      }

      _oUser.setDebug(true);    // 預設有 Debug 訊息

      if (!_isOK) {
        _sNextPage = "login_error.jsp";
        request.setAttribute("emisMenuServlet.MSG", "登入失敗，請回上一頁重新登入");
      } else {
        //   參數MENUS定義要用的Users table欄名
        String _sMenus = request.getParameter("MENUS");
        if (_sMenus == null) _sMenus = "MENUS";
        _sNextPage = _oUser.getProperty(_sMenus);
      }
    } catch (Exception e) {
      _sNextPage = "login_error.jsp";
      request.setAttribute("emisMenuServlet.MSG", "登入失敗，請回上一頁重新登入");
      log("[emisMenuServlet] " + e.getMessage());
      //out.println("[emisMenuServlet] " + e.getMessage());
    }
    if (_isDebug)
      out.println("由MENUS取出下一網頁:" + _sNextPage + "<br>");

    if (_sNextPage.indexOf("error") >= 0) {  // 錯誤網頁固定放在/webapp_root/*_error.jsp
      _sNextPage = getErrorPage(_isDebug, out, _sNextPage, _sRoot);
    } else if (_sNextPage == null || "".equals(_sNextPage)) {
      String _sMenusPage = request.getParameter("NEXTPAGE");
      int _iIndex = _sRoot.indexOf("/", 1);
      //out.println("nextpage=" + _sRoot);
      // Dispatch時會自動補上webapp的root, 因此先將root刪除.
      _sNextPage = _sRoot.substring(_iIndex) + "/" + _sMenusPage;
    } else {
      String _sMenusPage = request.getParameter("NEXTPAGE");
      if (_sMenusPage != null && !"".equals(_sMenusPage)) {
        _sNextPage = emisHttpUtil.getWebappRoot(request) + "/" + request.getParameter("NEXTPAGE");
        out.println("<form id=frmMain action='" + _sNextPage + "'>");
        out.println("</form>");
        if (!_isDebug) {
          out.println("<script> frmMain.submit(); </script>");
          return;
        }
      }
      /*
      if (_sNextPage2 != null && !"".equals(_sNextPage2)) {  // 有NEXTPAGE element時以之優先
        _sNextPage2 = emisHttpUtil.getWebappRoot(request) + "/" + _sNextPage2;
        out.println("<html><head><script src='js/emis.js'></script></head><body>");
        out.println("<script for=window event=onload>");
        if (_sDebug == null) {
          out.println("window.emisWinOpen('" + _sNextPage2 + "',-1,-1);");
          out.println("window.width = 10;");
          out.println("window.height = 10;");
        }
        out.println("</script>");
        out.println("</body>");
        out.println("</html>");
        return;
      }*/
    }

    //- 下列可印出各式路徑
    //com.emis.http.emisHttpUtil.printPath(this, request, out);
    // getRequestDispatcher( )的路徑以/開頭是對應於根目錄的路徑
    //_sNextPage = _isError ? _sErrorPage : _sNextPage;
    RequestDispatcher rd = request.getRequestDispatcher(_sNextPage);
    if (!_isDebug)
      rd.forward(request, response);
    else
      out.println("NextPage=" + _sNextPage);
  }

  private String getErrorPage(boolean isDebug, java.io.PrintWriter out, String sNextPage, String sRoot) {
    int _iIndex = sRoot.indexOf("/", 1);
    // 傳入root="/les/jsp/web"時要變成"/les"
    if (_iIndex != -1) sRoot = sRoot.substring(0, _iIndex);

    if (sNextPage.indexOf(sRoot) >= 0) {  // 網址含有root的話先將之刪去
      sNextPage = sNextPage.substring(sNextPage.lastIndexOf("/"));
    }
    //-out.println("ererpage=" + sNextPage + " root=" + sRoot);
    sNextPage = /*sRoot +*/ "/" + sNextPage;
    if (!isDebug)
      out.println("<html><head>" +
              "<script>" +
              "  window.location = '" + sNextPage + "';" +
              "</script>" +
              "</head></html>");
    return sNextPage;
  } // getErrorPage( )

  /** Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, java.io.IOException {
    processRequest(request, response);
  }

  /** Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, java.io.IOException {
    processRequest(request, response);
  }

  /** Returns a short description of the servlet.
   */
  public String getServletInfo() {
    return "Short description";
  }

  /**
   *check Remote Ip is exact  that it is defined by "USERIP" of
   * Users Table
   */
  private boolean isExactUserIP(String sUserIP, String sIP) {
    boolean _bRetval = true;
    if (sUserIP != null && sUserIP != "") {
      int _iPos = sUserIP.indexOf("*");
      if (_iPos >= 0) {
        String _sCheckIP = sUserIP.substring(0, _iPos);
        sIP = sIP.substring(0, _iPos);
        if (!_sCheckIP.equalsIgnoreCase(sIP)) {
          _bRetval = false;
        }
      } else {
        if (!sUserIP.equalsIgnoreCase(sIP)) {
          _bRetval = false;
        }
      }
    }
    return _bRetval;
  }

  /**
   * check Remote Ip is exact  that it is defined by "USER_IP" of
   * Users Table 2003.01.18 Cliff
   */
  private boolean isCheckIP(String sUserIP, String sRemoteIP) throws Exception {
    if (sUserIP == null || "".equals(sUserIP))
      return true;

    boolean _bReturn = true;
    ServletContext oContext = getServletContext();
    emisDb _oDB = emisDb.getInstance(oContext);
    try {
      _oDB.prepareStmt("select IP_ADDR from Group_I" +
              "  where G_NO in (select G_NO from Group_H" +
              "                   where G_TABLE='GROUP_I' and G_NO=?)");
      _oDB.setString(1, sUserIP);
      _oDB.prepareQuery();

      sRemoteIP = sRemoteIP.toUpperCase();
      String _sIPAddr = "";
      while (_oDB.next()) {
        _sIPAddr = _oDB.getString("IP_ADDR").toUpperCase();
        if (sRemoteIP.indexOf(_sIPAddr) >= 0) {
          // Login User IP 為 Group_I 之 IP_ADDR 設定則 Return True
          break;
        }
        _bReturn = false;
      }
    } catch (Exception e) {
      log("[emisMenuServlet] " + e.getMessage());
    } finally {
      _oDB.close();
    }

    return _bReturn;
  }

  /**
   *
   * @param sUserStore 使用者門市
   * @param sUserIP      使用者登入ip
   * @param sUserType  使用者類型
   * @return                    true可登入,false不可登入
   * @throws Exception
   * 2004/06/28 Jacky 修正IPADDRESS="*"字串時未判斷的問題
   */
  private boolean checkIPADDR(String sUserStore, String sUserIP, String sUserType) throws Exception {
    //sUserType=1表門市登入,sUserType=2表區經理,sUserType=0表總公司
    if (sUserIP == null || "".equals(sUserIP))
      return true;

    String sql = null;
    ServletContext oContext = getServletContext();
    emisDb _oDB = emisDb.getInstance(oContext);
    emisProp prop = emisProp.getInstance(oContext);
    boolean _bReturn = true;
    String eros_login = null;

    try {
      if (prop != null) {
        eros_login = prop.get("EROS_LOGIN", "0");  //如果取不到,就預設為0
      }

      //    當EROS_LOGIN=0時 門市, 總公司, 區經理login電腦的IP只要在IPADDR.IP_ADDR中存在, 即可login
      if (eros_login.equalsIgnoreCase("0")) {
        sql = " select S_NO, IP_ADDR from Ipaddr   where  (  IP_ADDR = ? or IP_ADDR='*') ";
        _oDB.prepareStmt(sql);
        _oDB.setString(1, sUserIP);
      } else if ("1".equalsIgnoreCase(eros_login)) {
        //	門市login電腦的IP必須存在於IPADDR.S_NO=login門市的IPADDR.IP_ADDR中, 才可login
        if ("1".equalsIgnoreCase(sUserType)) {
          sql = "select S_NO, IP_ADDR from Ipaddr  where S_NO = ?  and IP_ADDR = ? ";
          _oDB.prepareStmt(sql);
          _oDB.setString(1, sUserStore);
          _oDB.setString(2, sUserIP);
        } else {
          //	總公司, 區經理login電腦的IP只要在IPADDR.IP_ADDR中存在, 即可login
          sql = " select S_NO, IP_ADDR from Ipaddr   where  ( IP_ADDR = ?  or (  S_NO = ? and  IP_ADDR='*' ) )";
          _oDB.prepareStmt(sql);
          _oDB.setString(1, sUserIP);
          _oDB.setString(2, sUserStore);
        }
      } else if ("2".equalsIgnoreCase(eros_login)) {
        // 門市, 總公司, 區經理login電腦的IP必須存在於IPADDR.S_NO=所屬門市的IPADDR.IP_ADDR中, 才可login
        sql = "select S_NO, IP_ADDR from Ipaddr  where S_NO = ?  and IP_ADDR = ? ";
        _oDB.prepareStmt(sql);
        _oDB.setString(1, sUserStore);
        _oDB.setString(2, sUserIP);
      }
      _oDB.prepareQuery();
      if (!_oDB.next()) {
        _bReturn = false;
      }
    } catch (Exception e) {
      log("[emisMenuServlet/CheckIPADDR] " + e.getMessage() + "/n  SQL=" + sql);
      throw e;
    } finally {
      if (_oDB != null)
        _oDB.close();
    }

    return _bReturn;
  }
}
