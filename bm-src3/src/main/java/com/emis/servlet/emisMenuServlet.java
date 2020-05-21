/* $Id: emisMenuServlet.java 6859 2016-12-02 09:37:44Z during.liu $

 *

 * Created on 2001年10月9日, 下午 5:55

 * Copyright (c) EMIS Corp.

 */

package com.emis.servlet;



import com.emis.app.migration.action.emisMiEncrypt;
import com.emis.db.emisDb;
import com.emis.db.emisProp;

import com.emis.http.emisHttpUtil;

import com.emis.user.emisCertFactory;

import com.emis.user.emisUser;
import com.emis.util.emisDate;
import com.emis.util.emisUtil;
import com.emis.messageResource.Messages;



import javax.servlet.RequestDispatcher;

import javax.servlet.ServletContext;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;



/**

 * emisMenuServlet:接收index.jsp送來的使用者帳號與密碼,做認證與登入系統之處理.

 * 登入成功後會自動跳往MENUS指定欄位之值, 否則跳回index.jsp.

 * param ID=使用者帳號

 * param PASSWD=密碼

 * param S_NO=門市代號

 * param MENUS=要使用的Users table的欄位名稱

 * param NEXTPAGE=認證成功後要跳到那一頁的檔名; MENUS與NEXTPAGE不能同時存在

 * @author Jerry

 * @version 1.0 2001/10/10, 1.1 2001/11/03

 * @version 1.2 2003/09/16 02:47:57 Joe

 * @version 1.3 2004/06/29 Jerry: 修改註解與行寬以通過JCSC
 * _sMethod 2010/04/11 lisa.huang 登錄方式

 *

 * 測試:

 * /epos/index.jsp 正常login與使用錯誤的密碼登入

 * /les/index.jsp 正常login與使用錯誤的密碼登入

 * /les/jsp/web/index.jsp 正常login與使用錯誤的密碼登入

 * /les/index.jsp?userid=root&passwd=123&nextpage=jsp/mtn/part.jsp 自動登入
 * Track+[14704] tommer.xie 2010/05/04 修正utf8版與big5版切換時，big5版登出報錯

 */

public class emisMenuServlet extends HttpServlet {



  /**

   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.

   * @param request servlet request

   * @param response servlet response

   * @throws ServletException

   * @throws java.io.IOException

   */

  protected void processRequest(HttpServletRequest request,

                                HttpServletResponse response)

    throws Exception, java.io.IOException {

    ServletContext application = getServletContext();



    response.setContentType("text/html;charset=utf8");
    response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
    response.setHeader("Pragma","no-cache"); //HTTP 1.0
    response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
    response.setDateHeader ("max-age", 0); 

    java.io.PrintWriter out = response.getWriter();

    //out.println("<center><font color='blue'>資料處理中...</font><center>");



    String _sID = request.getParameter("ID");

    String _sPasswd = request.getParameter("PASSWD");
    emisMiEncrypt encrypt = new emisMiEncrypt();
    String _sPassWord[] = {_sPasswd};
    String _sPWD = encrypt.act(_sPassWord,_sPassWord);
    _sPasswd = _sPWD;

    String _sS_NO = request.getParameter("S_NO");

    String _sMacAddress = request.getParameter("EMISMAC");

    String _sCompanyNo = request.getParameter("COMPANY_NO");

    String _sMethod = request.getParameter("METHOD");   //登入方式，

    String app = request.getParameter("APP"); //用于标识是否是app，防止登录错误时返回不到app登录界面
    if(app!=null){
      request.setAttribute("APP", app);
    }
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

    String err_status = ""; // 檢查系統用戶登入時是否已經填入網卡編號。「1」 為否


    if (_isDebug) {

      out.println("ID=" + _sID + ",Passwd=" + _sPasswd + ",SNO=" + _sS_NO);

      out.println("company=" + _sCompanyNo + ",usertype=" + _sUserType + "<br>");

    }



    try {

      if (_sUserType == null && _sCompanyNo == null) {

        //-out.println("start to login");

        //if (_sS_NO != null && "".equals(_sS_NO)) _sS_NO = null;

        // _sS_NO為"-1"表示不使用S_NO來找user; for Yes

        _oUser = emisCertFactory.login(application, request, _sS_NO, null,

            _sID, _sPasswd);

        //-out.println("end of login");

      } else {

        String _sExtraInfo = null;

        if (_sUserType == null)

          _sExtraInfo = "C," + _sCompanyNo;

        else if (_sCompanyNo == null)

          _sExtraInfo = "U," + _sUserType;

        //out.println("extra=" + _sExtraInfo);

        _oUser = emisCertFactory.login(application, request, _sS_NO, _sExtraInfo,

            _sID, _sPasswd);

      }

      String _sCheckIP = _oUser.getProperty("USERIP");

      _isOK = isExactUserIP(_sCheckIP, request.getRemoteAddr());



      //$ 2003.1.18 新增 User_IP Check IP Address

      if (_isOK) {

        String _sUserIP = _oUser.getProperty("USER_GIP");

        _isOK = isCheckIP(_sUserIP, request.getRemoteAddr());

      }


      if (isCheckMac(_sID, _sCompanyNo,_sS_NO,_sMacAddress) == false) {
                err_status = "1";
                _isOK = false;
              }

      _oUser.setDebug(true);    // 預設有 Debug 訊息



      if (!_isOK) {
        _sNextPage = "login_error.jsp";
            if ("1".equals(err_status)) {
              request.setAttribute("emisMenuServlet.MSG", "登錄失敗,請回上一頁重新登錄");
      } else {
              request.setAttribute("emisMenuServlet.MSG", Messages.getString("emisMenuServlet.21"));
            }
          } else {
            //參數  MENUS 定義 要用的UserTable 欄名
        String _sMenus = request.getParameter("MENUS");
        if (_sMenus == null) _sMenus = "MENUS";
        _sNextPage = _oUser.getProperty(_sMenus);
            if("phone".equals(_sMethod)){
              _sNextPage="menusm.jsp";
      }

          }
    } catch (Exception e) {
      _sNextPage = "login_error.jsp";
          request.setAttribute("emisMenuServlet.MSG", Messages.getString("emisMenuServlet.26"));
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

        _sNextPage = emisHttpUtil.getWebappRoot(request) + "/" +

        request.getParameter("NEXTPAGE");

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



  /**

   * 傳回錯誤網頁名.

   * @param isDebug

   * @param out

   * @param sNextPage

   * @param sRoot

   * @return

   */

  private String getErrorPage(boolean isDebug, java.io.PrintWriter out,

                              String sNextPage, String sRoot) {

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



  /**

   * Handles the HTTP <code>GET</code> method.

   * @param request servlet request

   * @param response servlet response

   * @throws ServletException

   * @throws java.io.IOException

   */

  protected void doGet(HttpServletRequest request, HttpServletResponse response)

      throws ServletException, java.io.IOException {

    try {
      processRequest(request, response);
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

  }



  /** Handles the HTTP <code>POST</code> method.

   * @param request servlet request

   * @param response servlet response

   * @throws ServletException

   * @throws java.io.IOException

   */

  protected void doPost(HttpServletRequest request, HttpServletResponse response)

      throws ServletException, java.io.IOException {

    try {
      processRequest(request, response);
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

  }



  /**

   * Returns a short description of the servlet.

   * @return

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
   * check Remote User Macaddress
   * fusblue 20080115
   */
  private boolean isCheckMac(String sUserId,String _sCompanyNo, String _sS_NO,String sMacaddress) throws Exception {
    if (sUserId == null || "".equals(sUserId))
      return true;

    boolean _bReturn = false;
    ServletContext oContext = getServletContext();
    emisProp oProp = emisProp.getInstance(oContext);
    emisDb _oDB = emisDb.getInstance(oContext);
    emisDate date = new emisDate();
    try {
      String com_name = oProp.get("EPOS_COMPANY");
      //_oDB.prepareStmt("select * from Emisprop where [NAME]='EP_CHECK_MAC' and [VALUE]='Y' ");
      //_oDB.prepareQuery();
      if ("Y".equals(oProp.get("EP_CHECK_MAC"))) {
        if ("".equals(oProp.get("EP_CHECK_MACDATE")) ||
            (emisUtil.parseInt(oProp.get("EP_CHECK_MACDATE")) < emisUtil.parseInt(date.toString()))) {
          //_oDB.prepareStmt("select * from NetMac where UserId=? and  NetMac=? and ISFREEZ='N'");
          //todo:MAC
          // TABLE NetMac 判斷是UserID 否凍結
          _oDB.prepareStmt("select * from NetMac where  NetMac=? and ISFREEZ='N'");
          //_oDB.setString(1, sUserId);
          _oDB.setString(1, sMacaddress);
          _oDB.prepareQuery();
          if (_oDB.next()) {

            _bReturn = true;
          }
        } else {
          // 通過MAC 取得用戶ID
          _oDB.prepareStmt("select * from NetMac where NetMac=? ");
          //_oDB.setString(1, sUserId);
          _oDB.setString(1, sMacaddress);
          _oDB.prepareQuery();
          if (!_oDB.next()) {
            _oDB.prepareStmt("select UserName from Users where UserId = ? ");
            _oDB.setString(1, sUserId);
            _oDB.prepareQuery();
            String userName = "";
            if (_oDB.next()) {
              userName = _oDB.getString("UserName");
            }
            _oDB.prepareStmt("select isnull(Max([ID]),0)+1 as ID_NO from NetMac ");
            _oDB.prepareQuery();
            if (_oDB.next()) {
              int id_no = _oDB.getInt("ID_NO");
              String sql =" insert into NetMac ([ID],COM_NO, COM_NAME, S_NO, USERID, \n" +
                  " USERNAME, NETMAC, ISFREEZ, CRE_USER, CRE_DATE)\n" +
                  " values (?, ?, ?, ?, ?,\n" +
                  " ?, ?, ?, ?, ?)";
              _oDB.prepareStmt(sql);
              _oDB.setInt(1, id_no);
              _oDB.setString(2, _sCompanyNo);  //com_no EMISPROP
              _oDB.setString(3, com_name);
              _oDB.setString(4, _sS_NO);
              _oDB.setString(5, sUserId);
              _oDB.setString(6, userName);
              _oDB.setString(7, sMacaddress);
              _oDB.setString(8, "N");
              _oDB.setString(9, userName);
              _oDB.setString(10, date.toString());
              _oDB.prepareUpdate();
}

          }

          _bReturn = true;
        }
      } else {
        _bReturn = true;
      }
    } catch (Exception e) {
      log("[emisMenuServlet] " + e.getMessage()); //$NON-NLS-1$
    } finally {
      _oDB.close();
    }

    return _bReturn;
  }

}
