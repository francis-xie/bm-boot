package com.emis.servlet;

import com.emis.file.emisDirectory;
import com.emis.file.emisFileFactory;
import com.emis.file.emisFileMgr;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

public class emisDataSaver extends HttpServlet
{
  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException
  {

      ServletInputStream in = request.getInputStream();
      PrintWriter out = response.getWriter();
      Enumeration e = request.getHeaderNames();
      while( e.hasMoreElements() )
      {
          String key = (String) e.nextElement();
          String header = request.getHeader(key);
          out.println(key+"="+header+"<BR>");
      }

      try {
        ServletContext oContext = this.getServletContext();
        try {


            emisFileMgr _oFMgr = emisFileMgr.getInstance(oContext);
            emisDirectory _oDir = _oFMgr.getDirectory("root").subDirectory("upload");
            emisFileFactory _oFileFactory = _oFMgr.getFactory();


//            ZipInputStream zIn = new ZipInputStream(in);

            byte [] buffer = new byte[4096];
//            ZipEntry zEntry = zIn.getNextEntry();
//            if( zEntry != null)
            {
//              String sExtractName = zEntry.getName();
//              zEntry.getSize();
              OutputStream fout = _oFileFactory.getOutStream(_oDir,"TEST",null);
              out.println("OPEN SUCCESS<BR>");
              try {
                  int _nReaded ;
//                  while( (_nReaded = zIn.read(buffer)) != -1 )
                  while( (_nReaded = in.read(buffer)) != -1 )
                  {

                      fout.write(buffer,0,_nReaded);
                      out.println("WRITE SUCCESS "+_nReaded+"<BR>");
                  }
                  out.println("Successful");
              } catch(Exception e1) {
                  e1.printStackTrace(out);
              } finally {
                  fout.close();
              }
            }
          } catch (Exception e2) {
            e2.printStackTrace(out);
          }

      } catch (Exception e3) {
          e3.printStackTrace(out);

      } finally {
          in.close();
      }
      out.close();

  }

  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
}