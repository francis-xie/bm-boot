package com.emis.servlet;

import com.emis.util.emisMultipartRequest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

public class emisUpload extends HttpServlet
{

public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException
{
  ServletContext application = getServletContext();
    res.setContentType("text/html;charset=UTF-8");
  PrintWriter out = res.getWriter();

  try {
    emisMultipartRequest multi = new emisMultipartRequest(application,req);
    out.println("<HTML>");
    out.println("<HEAD><TITLE>UploadTest</TITLE></HEAD>");
    out.println("<BODY>");
    out.println("<H1>UploadTest</H1>");

    // Print the parameters we received
    out.println("<H3>Params:</H3>");
    out.println("<PRE>");
    Enumeration params = multi.getParameterNames();
    while (params.hasMoreElements()) {
    String name = (String)params.nextElement();
    String value = multi.getParameter(name);
    out.println(name + " = " + value);
    }
    out.println("</PRE>");
    // Show which files we received
    out.println("<H3>Files:</H3>");
    out.println("<PRE>");
    Enumeration files = multi.getFileNames();
    while (files.hasMoreElements()) {
      String name = (String)files.nextElement();
      String filename = multi.getFilesystemName(name);
      String type = multi.getContentType(name);
      File f = multi.getFile(name);
      out.println("name: " + name);
      out.println("filename: " + filename);
      out.println("type: " + type);
      if (f != null) {
        out.println("length: " + f.length());
        out.println();
      }
      out.println("</PRE>");
    }

  }  catch (Exception e) {
    out.println("<PRE>");
    e.printStackTrace(out);
    out.println("</PRE>");
  }

  out.println("</BODY></HTML>");

}

}

