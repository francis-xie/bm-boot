package com.emis.qa;

import com.emis.http.HttpCookie;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class emisURLTest
{

  Properties oConfig = new Properties();
  LineNumberReader reader = null;
  String sOutput ;
  HashMap postVariables = new HashMap();

  HttpCookie myCookie = null;

  String sBaseURL;
  String sloginURL;
  String slogoutURL;
  boolean isShowdebugonly;
  boolean isEditoutput;
  String sEditor;
  PrintWriter out = null;
  boolean isOutputEditable = false;


  public emisURLTest ( String sConfigFile,String sParamFile) throws Exception
  {
    InputStream in = new FileInputStream(sConfigFile);
    try {
      oConfig.load(in);
    } finally {
      in.close();
    }
    reader = new LineNumberReader( new FileReader(sParamFile));
    init();
  }


  private void init() throws Exception
  {
    String _sOutput = oConfig.getProperty("output","console");
    if( "console".equalsIgnoreCase(_sOutput))
    {
      out = new PrintWriter(System.out);
    } else {
      out = new PrintWriter( new FileWriter(_sOutput,false) );
      sOutput = _sOutput;
      isOutputEditable = true;
    }

    // 再來就有 Output 了

    String sHost = oConfig.getProperty("host");
    int nPort=Integer.parseInt(oConfig.getProperty("port","80"));
    String sURLRoot =oConfig.getProperty("URLRoot","/epos");

    sBaseURL = "http://"+sHost+":"+nPort+sURLRoot;

    out.println("====root="+sBaseURL);

    sloginURL=sBaseURL+oConfig.getProperty("loginURL");
    slogoutURL=sBaseURL+oConfig.getProperty("logoutURL");
    isShowdebugonly=strToBoolean("showdebugonly");
    isEditoutput=strToBoolean("editoutput");
    sEditor=oConfig.getProperty("editor","edit");


  }
  private boolean strToBoolean(String sKey)
  {
    return "true".equalsIgnoreCase(oConfig.getProperty(sKey,"false")) ? true : false;
  }

  public void get(String sURL) throws Exception
  {
      out.println("====GET "+sBaseURL+sURL);
      URL baseURL = new URL(sBaseURL+sURL);
      HttpURLConnection con = (HttpURLConnection) baseURL.openConnection();
      con.setDoInput(true);
      con.setUseCaches(false);
      con.setDefaultUseCaches(false);

      if (myCookie != null )  {
        con.setRequestProperty("Cookie",myCookie.getNameValue());
      }
      getInputFromHttp(con);
      con.disconnect();

  }

  public void post(String sURL) throws Exception
  {
      out.println("====POST "+sBaseURL+sURL);
      URL baseURL = new URL(sBaseURL+sURL);
      HttpURLConnection con = (HttpURLConnection) baseURL.openConnection();
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setUseCaches(false);
      con.setRequestMethod("POST");
      con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
      if (myCookie != null )  {
        con.setRequestProperty("Cookie",myCookie.getNameValue());
      }
      DataOutputStream printout = new DataOutputStream( con.getOutputStream() );
      try {
        String sContent = "";
        Set s = postVariables.keySet();
        Iterator it = s.iterator();
        while ( it.hasNext() )
        {
          String sKey = (String) it.next();
          String sValue = (String) postVariables.get(sKey);
          sContent = encode(sContent,sKey,sValue);
        }
        printout.writeBytes( sContent );
        printout.flush();
      } finally {
        printout.close();
      }
      getInputFromHttp(con);
      con.disconnect();
  }
  public void login(String sExpression) throws Exception
  {
      String _sUserId=null;
      String _sPasswd=null;
      String _sSNo=null;
      int idx = sExpression.indexOf("/");
      if( idx != -1 )
      {
        _sUserId = sExpression.substring(0,idx);
        sExpression = sExpression.substring(idx+1);
      }
      idx = sExpression.indexOf("/");
      if( idx != -1 )
      {
        _sPasswd = sExpression.substring(0,idx);
        _sSNo = sExpression.substring(idx+1);
      }

      URL baseURL = new URL(sloginURL);
      HttpURLConnection con = (HttpURLConnection) baseURL.openConnection();
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setUseCaches(false);
      con.setRequestMethod("POST");
      con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
      DataOutputStream printout = new DataOutputStream( con.getOutputStream() );
      try {
        String content = "";
        content = encode(content,"ID",_sUserId);
        content = encode(content,"PASSWD",_sPasswd);
        content = encode(content,"S_NO",_sSNo);
        printout.writeBytes( content );
        printout.flush();
      } finally {
        printout.close();
      }
      for (int i=1; i < 100 ; i++)
      {
        String key = con.getHeaderFieldKey(i);
        if( key == null ) break;
        String value = con.getHeaderField(i);
        if("set-cookie".equalsIgnoreCase(key))
        {
          myCookie = new HttpCookie(value);
        }
      }

      con.disconnect();

      if( myCookie != null )
        out.println("====login success");
      else
        out.println("====login failure");
  }

  private String encode(String sContent,String sKey,String sValue)
  {
    if( (sValue == null) || "".equals(sValue))
      return sContent;
    if( "".equals(sContent))
      return sKey + "="+URLEncoder.encode(sValue);
    return sContent + "&" +sKey + "="+URLEncoder.encode(sValue);
  }


  public void logout() throws Exception
  {
    if( myCookie != null )
    {
      URL baseURL = new URL(slogoutURL);
      HttpURLConnection con = (HttpURLConnection) baseURL.openConnection();
      con.setDoInput(true);
      con.setDoOutput(true);
      con.setUseCaches(false);
      if (myCookie != null )  {
        con.setRequestProperty("Cookie",myCookie.getNameValue());
      }
      con.getInputStream().close();
      con.disconnect();
      out.println("logout");
      myCookie = null;
    } else {
      out.println("logout ignored");
    }
  }

  public void set(String sExpression) throws Exception
  {
    int eqIndex = sExpression.indexOf("=");
    if( eqIndex == -1 ) throw new Exception("Error Command Format, correct SET FORMAT is set A=B");
    String _sKey = sExpression.substring(0,eqIndex);
    String _sValue = sExpression.substring(eqIndex+1);
    postVariables.put(_sKey,_sValue);
    out.println("====SET "+_sKey+"="+_sValue);
  }
  public void unsetall()
  {
     postVariables.clear();
     out.println("====UNSETALL");
  }
  public void unset(String sKey)
  {
     postVariables.remove(sKey);
     out.println("====UNSET "+sKey);
  }

  public void close() throws Exception
  {
    if( reader != null ) {
      try {
        reader.close();
      } catch (Exception ignore) {}
    }
    if( out != null ) {
      try {
        out.close();
      } catch (Exception ignore) {}
    }
    if( this.isEditoutput )
      if(isOutputEditable)
        Runtime.getRuntime().exec(sEditor+ " " + sOutput);
  }

  private void getInputFromHttp(HttpURLConnection con) throws Exception
  {
      BufferedReader input = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
      try
      {
          out.println("------------------------------------------------------------------------");
          String line = null;
          boolean _isStart = false;
          while((line = input.readLine()) != null) {
            if( this.isShowdebugonly )
            {
              if(! _isStart )
              {
                if(line.indexOf("<!--EMIS XML DEBUG") != -1)
                {
                  _isStart = true;
                }
                continue;
              }
              if( line.indexOf("EMIS XML DEBUG-->") != -1 ) break;
              out.println(line);
            } else {
              out.println(line);
            }
          }
          out.println("------------------------------------------------------------------------");
          out.flush();
      } finally {
        input.close();
      }
  }


  public void test() throws Exception
  {
      String sLine = null;
      String sAct = null;
      String sValue = null;
      int _nLineNumber = 1;

      while ( (sLine = reader.readLine()) != null )
      {
        if( sLine.trim().equals("")) continue;
        String _tmp = sLine.trim();
        if((sLine.charAt(0) == ';') || (sLine.charAt(0) == '#')) {
          // comment ..
          break;
        }


        int spaceIndex = sLine.indexOf(" ");
        sAct = null;
        sValue = null;
        if( spaceIndex != -1 )
        {
          sAct = sLine.substring(0,spaceIndex).trim().toUpperCase();
          sValue = sLine.substring(spaceIndex+1).trim();
        } else {
          sAct = sLine.trim().toUpperCase();
        }

        if("SET".equals(sAct)) {
          if(sValue != null )
            set(sValue);
        } else
        if("UNSET".equals(sAct)) {
          if( sValue != null )
            unset(sValue);
        } else
        if("UNSETALL".equals(sAct)) {
          unsetall();
        } else
        if("POST".equals(sAct)) {
          post(sValue);
        } else
        if("GET".equals(sAct)) {
          get(sValue);
        } else
        if("LOGIN".equals(sAct))  {
          login(sValue);
        } else
        if("LOGOUT".equals(sAct)) {
          logout();
        } else
        if("SHOWALL".equals(sAct)) {
          this.isShowdebugonly = false;
        } else
        if("SHOWDEBUGONLY".equals(sAct)) {
          this.isShowdebugonly = true;
        } else {
          out.println("==== *UNKNOW COMMAND:"+sAct + " at line "+_nLineNumber);
        }
        _nLineNumber++;
      }
      out.flush();

  }
  public static void main(String[] args) throws Exception
  {
    if( args.length < 2 )
    {
      System.out.println("Usage:java com.emis.qa.emisURLTest ConfigFile ParamFile");
      return;
    }

    emisURLTest urlTest = new emisURLTest(args[0],args[1]);
//    emisURLTest urlTest = new emisURLTest("C:\\config.dat","c:\\param.dat");
    try {
      urlTest.test();
    } finally {
      urlTest.close();
    }
  }

}