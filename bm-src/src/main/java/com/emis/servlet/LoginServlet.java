package com.emis.servlet;

import com.emis.app.migration.action.emisMiEncrypt;
import com.emis.db.emisDb;
import com.emis.user.emisAbstractUser;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;
import com.emis.util.emisDate;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class LoginServlet extends HttpServlet implements Servlet {

  protected void login(HttpServletRequest request, HttpServletResponse response)
      throws Exception, IOException {

    response.setContentType("text/html");            //转码
    String result = "";
    // 获取用户名
    String sUserName = request.getParameter("c_no");
    // 获取密码
    String sPasswd = request.getParameter("passwd");
    emisMiEncrypt encrypt = new emisMiEncrypt();
    String _sPassWord[] = {sPasswd};
    String _sPWD = encrypt.act(_sPassWord, _sPassWord);
    sPasswd = _sPWD;
    //System.out.println("name=" + sUserName);
    //System.out.println("pwd=" + sPasswd);

    emisDb _oDB = null;
    try {
      _oDB = emisDb.getInstance(request.getSession().getServletContext());
      emisDate date = new emisDate();
      // SQL语句
      String sql = "select isnull(LOGIN_NUM,0)+1 NUM,LOGIN_TIME,C_NO,C_NAME" +
          ",CONNECTER,CNT_TITLE,TEL,FAX,MOBILE,EMAIL,ADDR" +
          ",ZIP,WEBSITE from SMH_Customer " +
          "where C_NO='" + sUserName + "' and PASSWD = '" + sPasswd + "'";
      //System.out.println(sql);
      _oDB.prepareStmt(sql);// 返回查询结果
      _oDB.prepareQuery();
      String c_name = "", login_time = "";
      String connecter = "", cnt_title = "", tel = "", fax = "", mobile = "", email = "", addr = "", zip = "", website = "";
      int login_num = 0;
      if (_oDB.next()) {
        // 如果记录集非空，表明有匹配的用户名和密码，登陆成功
        request.getSession().setAttribute("c_no", sUserName);
        login_num = _oDB.getInt("NUM");//取得当前用户第几次登录次数
        login_time = _oDB.getString("LOGIN_TIME");//取得上次登錄時間
        c_name = _oDB.getString("C_NAME"); //取得用户名
        connecter = _oDB.getString("CONNECTER");
        cnt_title = _oDB.getString("CNT_TITLE");
        tel = _oDB.getString("TEL");
        fax = _oDB.getString("FAX");
        mobile = _oDB.getString("MOBILE");
        email = _oDB.getString("EMAIL");
        addr = _oDB.getString("ADDR");
        zip = _oDB.getString("ZIP");
        website = _oDB.getString("WEBSITE");
        request.getSession().setAttribute("c_name", c_name);
        request.getSession().setAttribute("login_num", login_num);
        request.getSession().setAttribute("login_time", login_time);
        request.getSession().setAttribute("connecter", connecter);
        request.getSession().setAttribute("cnt_title", cnt_title);
        request.getSession().setAttribute("tel", tel);
        request.getSession().setAttribute("fax", fax);
        request.getSession().setAttribute("mobile", mobile);
        request.getSession().setAttribute("email", email);
        request.getSession().setAttribute("addr", addr);
        request.getSession().setAttribute("zip", zip);
        request.getSession().setAttribute("website", website);
        //System.out.println("login_num:" + login_num + "  " + "login_time:" + login_time);
        //System.out.println("success!");
        // 虚拟一个登入用户
        setLoginUser(request);

        RequestDispatcher rq = request.getRequestDispatcher("jsp/acc/account.jsp");
        rq.forward(request, response);
        //java.util.Calendar c=java.util.Calendar.getInstance();
        //java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        //登录成功后，向用户表插入登录次数，和登录时间
        String sql1 = "update SMH_Customer set LOGIN_NUM=?," +
            "LOGIN_TIME=CONVERT(varchar(100), dbo.GetLocalDate(), 111)+' '+CONVERT(varchar(100), dbo.GetLocalDate(), 8) " +
            "where C_NO='" + sUserName + "'";
        _oDB.prepareStmt(sql1);
        _oDB.setInt(1, login_num);
        // _oDB.setString(2, f.format(c.getTime()));
        //_oDB.setString(2, sdf.format(System.currentTimeMillis()));
        _oDB.prepareUpdate();
      } else {
        // 否则登录失败
        //System.out.println("failed!");
        response.sendRedirect("login_failure.jsp");
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      _oDB.close();
    }

  }

  /**
   * 虚拟一个登入用户
   * @param request
   */
  protected void setLoginUser(HttpServletRequest request) {
    HttpSession session = request.getSession();
    emisUser _oUser = (emisUser) session.getAttribute(emisCertFactory.EMIS_USER_BIND_NAME);
    if (_oUser == null) {
      _oUser = new emisAbstractUser(session.getServletContext(), session.getId()) {
        @Override
        public Properties getUserStoreInfo() throws Exception {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Properties getUserInfo(String sUserId) throws Exception {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getUserType() {
          return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getMailAddr() {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getStKey() {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getRNo() throws Exception {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getRNoStr() throws Exception {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
      };

      session.setAttribute(emisCertFactory.EMIS_USER_BIND_NAME, _oUser);
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)

      throws ServletException, IOException {

    try {
      login(request, response);
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)

      throws ServletException, IOException {

    try {
      login(request, response);
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

  }

  /**
   *
   */
  private static final long serialVersionUID = 1L;
}