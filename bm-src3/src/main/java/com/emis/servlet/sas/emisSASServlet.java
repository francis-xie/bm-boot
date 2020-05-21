package com.emis.servlet.sas;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class emisSASServlet
{

  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException
  {


  }
  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }

  public static void  main(String [] argvs) throws Exception
  {
    String sBaseURL = "http://localhost:8000";
    URL baseURL = new URL(sBaseURL);
    HttpURLConnection con = (HttpURLConnection) baseURL.openConnection();
    con.setDoOutput(true);
    con.setDoInput(true);
    con.setUseCaches(false);
    con.setDefaultUseCaches(false);
    con.setRequestMethod("POST");
    con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
    DataOutputStream out = new DataOutputStream( con.getOutputStream() );
    try {
      out.writeChars("POS To SAS");
    } finally {
      out.close();
    }
    BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
    try {
      String sLine = null;
      while ( (sLine=in.readLine()) != null ) {
        System.out.println(sLine);
      }
    } finally {
      in.close();
    }
  }

}

