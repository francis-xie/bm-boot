package com.emis.servlet;



import com.emis.db.emisDbMgr;
import com.emis.db.emisSQLRec;
import com.emis.util.emisUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Vector;



/**

 * 顯示最慢的 N 個 SQL

 */

public class emisSQLErr20 extends HttpServlet

{

  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException

  {
	  

    ServletContext oContext = this.getServletContext();

    response.setContentType("text/html;charset=UTF-8");

    PrintWriter out = response.getWriter();

    try {

      emisDbMgr oDbMgr = emisDbMgr.getInstance(oContext);

      Vector list = oDbMgr.getSQLErr20();

      if ( list == null ) return;

      out.println("<html><head><title>SQL Error " + list.size() + "</title></head><body>");


      out.println("<table border='1' width='95%'><TR><TD width='5%'>序</TD><TD width='45%'>SQL</TD><TD width='10%'>執行時間</TD><TD width='40%'>錯誤</TD></TR>");

      for(int i=0;i<list.size();i++) {

        emisSQLRec rec = (emisSQLRec) list.get(i);

        if ( rec != null ) {

          Date d = new Date(rec.nStartTime);

          out.println("<TR><TD>"+(i+1)+"</TD><TD>"+rec.sSQL+"</TD><TD>"+d.toString()+"</TD><TD>");
          if(rec.m_sqlerr != null) {
        	  rec.m_sqlerr.printStackTrace(out);
          }
          out.println("</TD></TR>");

        }
 
      }

      out.println("</table></body></html>");

    } catch (Exception e) {

      out.print(e.getMessage());

    }

  }

  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }

  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }

}