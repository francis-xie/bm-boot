package com.emis.qa;

import com.emis.business.emisBusinessImpl;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用來測試 database 的模組
 * 須要有 DbTest.XML
 */
public class emisDbQa extends HttpServlet
{

  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }

  private ServletContext oContext ;
  private HttpServletRequest oRequest;
  private PrintWriter out;

  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException
  {
    oContext = getServletContext();
    oRequest = request;
    response.setContentType("text/html;charset="+emisUtil.FILENCODING);
    out = response.getWriter();
    try {
      test();
    } catch (Exception errxml) {
      out.println("ERROR AT:"+process+"<BR>");
      out.println("<PRE>");
      out.println(errxml);
      out.println("</PRE>");
    }
    out.println("<BODY></HTML>");
  }


  int process = 1;
  private void test() throws Exception
  {
    emisFile f = emisFileMgr.getInstance(oContext).getDirectory("root").subDirectory("business").getFile("dbtest.xml");
    emisUser oUser = emisCertFactory.getUser(oContext,oRequest);
    emisBusinessImpl b = new emisBusinessImpl("TEST",oContext,oUser,f,false);
    b.setParameter(oRequest);
    b.setWriter(out);

    b.process("del");
    process++;

    b.process("test insert(full insert)");
    process++;

    b.process("test insert(empty)");
    process++;

    b.setParameter("BLANK","");
    b.process("test insert blank values");
    process++;

    b.clearParamter();
    b.setParameter("A","A,B,C");
    b.setParameter("B","1.5,2.5");
    b.setParameter("C","100");
    b.process("transaction test");
    process++;

    b.process("test datasrc null");
    process++;

    b.clearParamter();
    b.setParameter("BLANK","");
    b.process("test datasrc blank");

    process++;

    b.setParameter("A","A");
    b.setParameter("B","1.5");
    b.setParameter("C","100");
    b.process("test datasrc values");
    process++;

  }


}