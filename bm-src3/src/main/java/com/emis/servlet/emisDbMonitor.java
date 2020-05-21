/* $Header: /repository/src3/src/com/emis/servlet/emisDbMonitor.java,v 1.1.1.1 2005/10/14 12:42:52 andy Exp $
 * 2004/05/25 Jerry: 加入建立時間並變更樣式
 */
package com.emis.servlet;

import com.emis.db.*;
import com.emis.spool.emisComplexSpool;
import com.emis.spool.emisSpoolSnapShot;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

public class emisDbMonitor extends HttpServlet
{
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    ServletContext application = getServletContext();
/*
    emisUser oUser = null;
    try {
      oUser = emisCertFactory.getUser(application,request);
    } catch (Exception e) {
      throw new ServletException(e);
    }

    if(! "SYS".equalsIgnoreCase(oUser.getGroups()))  // 非"SYS" Group者，權限不足
    {
      throw new ServletException("Insufficient privilege");
    }
*/
    String sURI = request.getRequestURI();     // epos/servlet/com.emis.servlet.emisDbMonitor
    String sAct = request.getParameter("act"); // document.all.act.value, (null/add/del)
    String sId = null;

	response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();

    out.println("<html>");
    out.println("<title>資料庫監視器</title>");
    out.println("<body>");

    try {
      emisDbMgr _oMgr = emisDbMgr.getInstance(application);

      if( _oMgr == null )
      {
        out.println("Null Database Connector Manager");
        return;
      }
      emisDbConnector _oConnector = _oMgr.getConnector(); // default connector
      if( _oConnector == null )
      {
        out.println("Null Database Connector");
      }
      emisComplexSpool oSpool_ = (emisComplexSpool) _oConnector;

      if( "del".equals(sAct)) {
        sId = request.getParameter("ID");  // document.all.ID.value
        try {
          oSpool_.onLineDelete(sId);
        } catch (Exception e) {
          out.println(e.getMessage());
        }
      } else
      if( "add".equals(sAct))
      {
        try {
          oSpool_.onLineAllocate();
        } catch (Exception ex) {
          out.println(ex.getMessage());
        }
      }

      out.println("<table border='1' width='90%'>");
      out.println("<TR bgcolor='99ccff'><TD>連線過期</TD><TD>" + oSpool_.getExpire() +" 毫秒</TD></TR>");
      out.println("<TR bgcolor='ffffcc'><TD>連線逾時</TD><TD>" + oSpool_.getTimeOut() +" 秒</TD></TR>");
      out.println("<TR bgcolor='99ccff'><TD>借出逾時</TD><TD>"+ oSpool_.getOrphan()+" 毫秒</TD></TR>");
      out.println("<TR bgcolor='ffffcc'><TD>資料庫連線最大數</TD><TD>" + oSpool_.getMaxSize() +"</TD></TR>");
      out.println("<TR bgcolor='99ccff'><TD>連線池檢查週期</TD><TD>"+ oSpool_.getInterval()+" 毫秒</TD></TR>");
      out.println("<TR bgcolor='ffffcc'><TD>資料庫連線最小數</TD><TD>" + oSpool_.getMinSize() +"</TD></TR>");
      out.println("<TR bgcolor='99ccff'><TD>資料庫連線初始值</TD><TD>" + oSpool_.getInitSize() +"</TD></TR>");
      out.println("</table><HR>");

      emisSpoolSnapShot _oShot = oSpool_.getSnapShot();
      out.println("目前連線總數:"+ (_oShot.getPooledSize() + _oShot.getCheckedOutSize())+"<BR>");
      out.println("使用中:"+_oShot.getCheckedOutSize()+"<HR>" );

      out.println("<button  onclick='onLineAllocate();'>新增</button><button  onclick='refresh();'>重整</button><BR>");

      out.println("<table border='0' cellspacing='1' bgColor='Navy' width='96%'>");
      out.println("  <tr bgcolor='99ccff' align='center'><td width='%5'>序</td><td width='%5'>SPID</td><td width='%15'>使用狀況</td><td width='%10'>物　　　件</td><td width='%8'>建立時間</td><td width='%20'>SQL</td><td>功能</td></tr>");

      Enumeration e = _oShot.getDescriptor();
      int _nSeqNo = 1;
      String[] _aColors = { "ffffcc", "silver" };
      while( e.hasMoreElements())
      {
        emisProxyDesc _oDesc = (emisProxyDesc) e.nextElement();
        emisStatementWrapper stmtWrapper = _oDesc.getStatement();
        String _sObjectId = _oDesc.getId();
        String _sTime = _oDesc.getTime();
        String _sColor = _aColors[ _nSeqNo % 2];
        out.print("<tr bgcolor='"+_sColor+"'><td>"+_nSeqNo+ "</td><td>" // 序        		    + _oDesc.getSpid() +"</td><td>"   // spid
                    +_oDesc.getDescription() +"</td><td>"            // 使用狀況(連線池...)
                    +_sObjectId+"</td><td>"                          // 物件 ID
                    +_sTime+"</td><td>");
                                                          // executing SQL
        if( stmtWrapper == null )
          out.print("&nbsp");
        else
          stmtWrapper.desc(out);

        out.println("</td><td><button onclick=\"deleteOnLine('"+_sObjectId+"');\" >刪</button></td></tr>" );
        _nSeqNo++;
      }
    } catch (Exception e ) {
      e.printStackTrace(out);
    }

    out.println("   </table>");
    out.println("<form id='idForm' method='post' action='"+sURI+"'>");
    out.println("  <input type=hidden name=act>");
    out.println("  <input type=hidden name=ID>");
    out.println("</form>");
    out.println("</body>");
    out.println("</html>");
    out.println("<script>");
    out.println("  function refresh()");
    out.println("  {");
    out.println("    document.all.act.disabled=true;");
    out.println("    document.all.idForm.submit();");
    out.println("  }");
    out.println("  function deleteOnLine( sObjectId )");
    out.println("  {");
    out.println("    document.all.ID.value = sObjectId;");
    out.println("    document.all.act.value='del';");
    out.println("    document.all.idForm.submit();");
    out.println("  }");
    out.println("  function onLineAllocate()");
    out.println("  {");
    out.println("    document.all.act.value='add';");
    out.println("    document.all.idForm.submit();");
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