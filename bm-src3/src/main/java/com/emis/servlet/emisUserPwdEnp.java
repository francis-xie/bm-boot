package com.emis.servlet;

import com.emis.util.emisUtil;
import com.emis.schedule.emisScheduleMgr;
import com.emis.db.emisSQLCache;
import com.emis.db.emisDb;
import com.emis.app.migration.action.emisMiEncrypt;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class emisUserPwdEnp extends HttpServlet {
  private emisDb oDb;
  private String _sUpdateUserPwd=""; //Sql statement  Update USERS.PASSWD
  /**
   * Servlet service.
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */

  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html;charset=" + emisUtil.FILENCODING);
    PrintWriter out = response.getWriter();
    ServletContext application = this.getServletContext();
    String _sReloadTarget = request.getParameter("target");
    ResultSet rs = null;
    ArrayList rows = new ArrayList();
    String sS_NO,sUser_ID,sPassWD,sOldPassWD;
    emisMiEncrypt encrypt = new emisMiEncrypt();
    userInfo user;
    _sUpdateUserPwd = "update USERS Set PASSWD=? where S_NO=? and USERID=?";
    try {
      out.println("<html>");
      out.println("<style>");
      out.println("  TABLE     { font: 8pt Arial; }");
      out.println("  H1        { font:12pt Arial; }");
      out.println("  .odd_row  { background-color:'99ccff'; }");
      out.println("  .even_row { background-color:'ffffcc'; }");
      out.println("</style>");
      out.println("<body>");
      out.println("<table width='100%' border='1'>");
      // SQL Cache
    String _sSQLField1 = "S_NO", _sSQLField2 = "USERID",
        _sSQLField3 = "PASSWORD";
//    emisSQLCache.reload(application);
    out.println("<tr align='center' class='odd_row'><td colspan='3'>" +
        "<h1>USER PASSWORD 重新設定成功</h1></td></tr>");
    oDb = emisDb.getInstance(application);
    oDb.setDescription("system:USERS");
    rs = oDb.executeQuery("SELECT S_NO,USERID,PASSWD FROM USERS WHERE len(PASSWD) <=20");
    while(rs.next()){
      sS_NO = rs.getString("S_NO");
      sUser_ID = rs.getString("USERID");
      sPassWD = rs.getString("PASSWD");
      user = new userInfo(sS_NO,sUser_ID,sPassWD);
      rows.add(user);
    }
    out.println("<tr align='center' class='odd_row'><td>" +
        _sSQLField1 + "</td><td>" + _sSQLField2 + "</td><td>" +
        _sSQLField3 + "</td></tr>");
    for(int i=0;i<rows.size();i++){
      user = (userInfo)rows.get(i);
      sOldPassWD = user.getPassWD();
      String _sPassWord[]  = {sOldPassWD};
      int compelete = updateUserPWD(user.getS_NO(),user.getUser_ID(),encrypt.act(_sPassWord,_sPassWord));
      out.println("<tr class='even_row'><td>" +setEmpty( user.getS_NO()));
      out.println("</td><td>" + user.getUser_ID());
      out.println("</td><td>" + (compelete>0?"已更新":"更新失敗"));
      out.println("</td></tr>");
    }
    //emisRowSet _oCaches = new emisRowSet(oDb);
    } catch (Exception e) {
        out.println("<tr align='center' class='even_row'><td colspan='3'>" +
            "<h1>Users Table Open Error</h1></td></tr>");
    } finally {
      out.println("</table>");
      out.println("</body>");
      out.println("</html>");
      oDb.close();
    }

  }
  public int updateUserPWD(String sS_NO,String sUser_ID,String sPassword) throws SQLException {
    int iComplete=0,index=1;
    oDb.prepareStmt(_sUpdateUserPwd);
    oDb.setString(index++,sPassword);
    oDb.setString(index++,sS_NO);
    oDb.setString(index++,sUser_ID);

    iComplete = oDb.prepareUpdate();
    return iComplete;
  }
  public String setEmpty(String sStr){
    if("".equals(sStr) || sStr==null){
      return "&nbsp;";
    }else{
      return sStr;
    }
  }
  /**

   * doGet.

   * @param p0

   * @param p1

   * @throws javax.servlet.ServletException

   * @throws java.io.IOException

   */

  public void doGet(HttpServletRequest p0, HttpServletResponse p1)

      throws ServletException, IOException {

    service(p0, p1);

  }



  /**

   * doPost.

   * @param p0

   * @param p1

   * @throws ServletException

   * @throws IOException

   */

  public void doPost(HttpServletRequest p0, HttpServletResponse p1)

      throws ServletException, IOException {

    service(p0, p1);

  }
  class userInfo{
    private String sS_NO;
    private String sUser_ID;
    private String sPassWD;
    public userInfo(){

    }

    public userInfo(String sS_NO, String sUser_ID, String sPassWD) {
      this.sS_NO = sS_NO;
      this.sUser_ID = sUser_ID;
      this.sPassWD = sPassWD;
    }

    public String getS_NO() {
      return sS_NO;
    }

    public void setS_NO(String sS_NO) {
      this.sS_NO = sS_NO;
    }

    public String getUser_ID() {
      return sUser_ID;
    }

    public void setUser_ID(String sUser_ID) {
      this.sUser_ID = sUser_ID;
    }

    public String getPassWD() {
      return sPassWD;
    }

    public void setPassWD(String sPassWD) {
      this.sPassWD = sPassWD;
    }
  }
}
