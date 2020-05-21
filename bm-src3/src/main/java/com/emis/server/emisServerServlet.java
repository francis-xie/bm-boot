package com.emis.server;



import javax.servlet.ServletConfig;

import javax.servlet.ServletContext;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;

import java.io.PrintWriter;



/**

 * 此 Servlet 需要在 Application Server

 * 的 load at start-up servlet 中

 * 並且要傳入參數 emiscfg ,指定系統設定檔的所在地

 */



public class emisServerServlet extends HttpServlet {



  /**Initialize global variables*/



  private emisServer oServer_;

  private String sStartUpMsg_;



  public void init(ServletConfig oConfig) throws ServletException

  {

      super.init(oConfig);



      // already inited , return...

      if( oServer_ != null ) return;

      // 代碼提前
      // each Web Application has a servlet context

      // -----------server has not start-up yet...-----------

      ServletContext _oContext = oConfig.getServletContext();

      String _sDocumentRoot = _oContext.getRealPath("/");

      if( _sDocumentRoot == null )

      {

        sStartUpMsg_ = "can't get ServletContext Document Root";

        throw new ServletException(sStartUpMsg_);

      }


      String _sConfigFile = getServletContext().getInitParameter("emiscfg");



      if( _sConfigFile == null )

      {

          _sConfigFile = oConfig.getInitParameter("emiscfg");

      }



      if( _sConfigFile == null )

      {

          // this only work for single server

          _sConfigFile = System.getProperty("emiscfg");

      }

      // 插入代碼
      if( _sConfigFile != null )
      {
        File file = new File(_sConfigFile);
        if(!file.exists())
          _sConfigFile = _oContext.getRealPath(_sConfigFile);
      }


      if( _sConfigFile == null )

      {

          System.err.println("error Getting 'emiscfg' System Variable");

          _sConfigFile = "c:\\resin\\epos.cfg";

      }


      try {

          oServer_ = emisServerFactory.createServer(_oContext,_sDocumentRoot,_sConfigFile,true);

          sStartUpMsg_ = "server startup successful:" + oServer_.toString();

      } catch (Exception sServerError) {

          sStartUpMsg_ = "server StartUp Error:" + sServerError.getMessage();

      }



  }



  public void service(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException

  {

      PrintWriter _out = resp.getWriter();

      _out.write(sStartUpMsg_);

  }



  /**Clean up resources*/

  public void destroy() {

      if( oServer_ != null )

      {

        try {
          oServer_.shutdown();
        } catch (Exception ignore) {
          //do nothing....
        }

          oServer_ = null;

      }

  }

}

