package com.emis.servlet.loader;



import javax.servlet.ServletException;

import javax.servlet.ServletOutputStream;

import javax.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import java.io.*;



public class emisDataLoader15 extends HttpServlet

{

  private final static int bufsize=8192;

  private byte [] buf = new byte [bufsize];

  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException

  {



    String sFileName = request.getParameter("FILE");

    if( sFileName == null ) return;

    sFileName = new String(sFileName.getBytes("ISO8859_1"), "Big5");//解決中文文件名問題

    File isF = new File(sFileName);

    if (!isF.isFile()  || !isF.exists() )

    {

        return;

    }



    String sUpperFile = sFileName.toUpperCase();

    if(sUpperFile.endsWith(".JSP")   ||

       sUpperFile.endsWith(".CLASS") ||

       sUpperFile.endsWith(".JAR")   ||

       sUpperFile.endsWith(".HTML")  ||

       sUpperFile.endsWith(".HTM"))

    {

      // dis-allow to download files with jsp,class...etc

      throw new ServletException("File Permission Deny");

    }



    FileInputStream read = new FileInputStream(sFileName);

    try {

        BufferedInputStream bi = new BufferedInputStream(read,bufsize);

        try {

          ServletOutputStream pw = response.getOutputStream();

          try {

            BufferedOutputStream bo = new BufferedOutputStream(pw,bufsize);

            try {

                int readed = -1;

                while( (readed = bi.read(buf)) != -1 ) {

                    bo.write(buf,0,readed);

                }

            } finally {

              bo.close();

              bo=null;

            }

          } finally {

            pw.close();

            pw = null;

          }

        }finally {

          bi.close();

          bi = null;

        }

    } finally {

      read.close();

    }

    // 結束後不要把檔刪掉,不然 "回上一頁" 的功能會 Error



  }



  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }

  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }

}

