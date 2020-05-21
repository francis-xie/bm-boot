package com.emis.servlet.ftp;

import com.emis.db.emisDb;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * this service is responsible for pass
 * encrypted password into eposftp through the internet
 *
 * example: user='emis' , passwd='qwe098qaz'
 * encrypt: user='', passwd=''        String sUser=null;
        String sPasswd = null;

 */
public class emisFtp extends HttpServlet
{
  public void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
  {
    PrintWriter out = response.getWriter();
    try {
      Random r = new Random();
      ServletContext application = this.getServletContext();
      emisDb oDb = emisDb.getInstance(application);
      try {
        String sUser=null;
        String sPasswd = null;
        String sPort = null;

        oDb.prepareStmt("SELECT VALUE FROM EMISPROP WHERE NAME=?");
        oDb.setString(1,"EPOS_FTP_USER");
        oDb.prepareQuery();

        if( oDb.next()) {
          sUser = oDb.getString(1);
        } else {
          return;
        }
        oDb.setString(1,"EPOS_FTP_PASSWD");
        oDb.prepareQuery();
        if( oDb.next()) {
          sPasswd = oDb.getString(1);
        } else {
          return;
        }

        oDb.setString(1,"EPOS_FTP_PORT");
        oDb.prepareQuery();
        if( oDb.next()) {
          sPort = oDb.getString(1);
        } else {
          return;
        }

        if( (sUser == null) || ("".equals(sUser)) ||
            (sPasswd == null) || ("".equals(sPasswd)) ||
            (sPort == null) || ("".equals(sPort))
          ) {
          return;
        }

        sUser = encrypt(sUser,r);
        sPasswd = encrypt(sPasswd,r);
        sPort = encrypt(sPort,r);
        out.println(sUser);
        out.println(sPasswd);
        out.print(sPort);
      } finally {
        oDb.close();
      }
    } catch (Exception e) {

    } finally {
      out.close();
    }
  }
  private static String encrypt(String sStr,Random r)
  {
    StringBuffer buf = new StringBuffer();
    int len = sStr.length();
    for(int i=0;i<len;i++) {
      int iValue = (int) sStr.charAt(i);
      int iRand = r.nextInt(256);
      int jValue = iRand * iValue + iValue; // j=r*n+n  n=j/(r+1)
      buf.append( toFixFormat(iRand,2));
      buf.append( toFixFormat(jValue,6));
    }
    return buf.reverse().toString(); // reverse
  }
  private static String toFixFormat(int iRand,int nDigit)
  {
    String sRand = Integer.toHexString(iRand);
    int nDiff = nDigit-sRand.length();
    while(nDiff > 0 ) {
      sRand = "0" + sRand;
      nDiff--;
    }
    return sRand.toUpperCase();
  }

  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
/*
  public static void main(String [] argvs) throws Exception
  {
    String s = "emis";
    Random r = new Random();
    System.out.println( emisFtp.encrypt(s,r) );

  }
  */
}