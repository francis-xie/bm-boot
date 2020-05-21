/**
 * $Header: /repository/src3/src/com/emis/servlet/emisUserMonitor.java,v 1.1.1.1 2005/10/14 12:42:55 andy Exp $
 *
 * Copyright (c) 群豐資訊
 */
package com.emis.servlet;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class emisUserMonitor extends HttpServlet
{
  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException
  {
    response.setContentType("text/html;charset=UTF-8");
     PrintWriter out = response.getWriter();

     ServletContext application = getServletContext();
     String _sLogout = request.getParameter("LOGOUT");  // 取至隱藏變數document.all.LOGOUT.value

     out.println("<html>");
     out.println("<title>使用者登入狀態</title>");
     out.println("<body>");

     out.println("<button onclick='refresh();'>重新整理</button><BR>");
     try {
       com.emis.user.emisUserMonitor _oMonitor = emisCertFactory.getUserMonitor(application);
       HashMap _oHash = _oMonitor.getUserList();

       Set keys = _oHash.keySet();      // 將key以Set的形式傳回
       Iterator it =  keys.iterator();  // 再轉成Iterator方式
       out.println("<table width='100%' border='1'>");
       out.println("<TR bgcolor='99ccff' align='center'><TD>序</TD><TD>帳 號</TD><TD>使用者</TD><TD>SessionID</TD><TD>Login 時間</TD><TD>上　次　存　取　時　間</TD><TD>Logout</TD></TR>");
       int _iCount = 0;
       while( it.hasNext() )
       {
         HttpSession sess = (HttpSession) it.next();
         emisUser user = (emisUser) _oHash.get(sess);
         String _sSessionId = sess.getId();
         if( _sLogout != null ) // 按下"logout"後，強迫取消指定之Session
         {
           if( _sLogout.equals(_sSessionId))
           {
             try {
               sess.invalidate();
             } catch (Exception ignore) {}

             _oMonitor.removeSession(sess);
             _sLogout = null;
             continue;
           }
         }
         _iCount++;
         out.println("<TR bgcolor='ffffcc' align='center'><TD>" + _iCount +
             "</TD><TD>" + user.getID() + "</TD><TD>"+user.getName()+
             "</TD><TD>"+  sess.getId() +
             //abel modify
             "</TD><TD>"+(DateFormat.getDateTimeInstance()).format(new Date(sess.getCreationTime())) +             
             "</TD><TD>"+(DateFormat.getDateTimeInstance()).format(new Date(sess.getLastAccessedTime())) +             
             //"</TD><TD>" + new Date(sess.getCreationTime()).toLocaleString() +
             //"</TD><TD>"+new Date(sess.getLastAccessedTime()).toLocaleString()+
             "</TD><TD><button onclick=\"logout('"+sess.getId()+"');\">Logout</button></TD></TR>");
       }
     } catch (Exception e ) {
       e.printStackTrace(out);
     }

     out.println("   </table>");
     out.println("</body>");
     out.println("</html>");
     out.println("<form id='idForm' method='post' target='_self'>");
     out.println("  <input type='hidden' name='LOGOUT'>");
     out.println("</form>");
     out.println("<script>");
     out.println("  function refresh() {");
     out.println("    document.all.LOGOUT.disabled=true;");
     out.println("    idForm.submit();");
     out.println("  }");
     out.println("  function logout(sessid)");
     out.println("  {");
     out.println("    document.all.LOGOUT.value=sessid;");
     out.println("    idForm.submit();");
     out.println("  }");
     out.println("</script>");
  }

  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException
  {
    service(p0,p1);
  }

  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException
  {
    service(p0,p1);
  }
}
