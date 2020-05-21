/**
 * $Header: /repository/src3/src/com/emis/servlet/emisThread.java,v 1.1.1.1 2005/10/14 12:42:55 andy Exp $
 */
package com.emis.servlet;

import com.emis.util.emisUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;import com.emis.user.*;import com.emis.business.*;

public class emisAjaxServlet extends HttpServlet
{
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {	  ServletContext application = this.getServletContext();	  response.setContentType("text/xml;charset=UTF-8");	  PrintWriter out = response.getWriter();	  try {		  emisUser _oUser = emisCertFactory.getUser(application,request);		  String title = request.getParameter("TITLE");		  emisBusiness _oBusiness = emisBusinessMgr.get(application,title,_oUser);		  _oBusiness.setWriter(out);		  _oBusiness.setParameter(request);		  _oBusiness.processAjax();	  } catch (Exception e ) {		  		  emisAjax.outputAjaxError(e,out);	  }	
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