package com.emis.schedule.epos;
import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;
import com.emis.mail.emisMailer;
import com.emis.util.emisUtil;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 执行一些特殊处理排程
 */
public class emisSpecialSched extends emisEposAbstractSchedule {
  /* 排程设定范例
  INSERT INTO [sched]([S_NAME], [S_SERVER], [S_DESC], [S_CLASS], [RUNLEVEL], [SYEAR], [SMONTH], [SDAY], [SHOUR], [STIME], [INTERVAL], [PARAM], [SHOUR_END], [STIME_END], [S_MENU], [THREAD_GROUP], [REMARK])
  VALUES(N'specialSched', N'STOP', N'特别排程-Jijitown', N'com.emis.schedule.epos.emisSpecialSched', N'I', N'', N'', N'', N'', N'', N'60', N'http://localhost:8044/jijitown2/jsp/dev/database/runSqlscript.jsp?ACT=jijitown', N'', N'', '6', N'', NULL)

  INSERT INTO [sched]([S_NAME], [S_SERVER], [S_DESC], [S_CLASS], [RUNLEVEL], [SYEAR], [SMONTH], [SDAY], [SHOUR], [STIME], [INTERVAL], [PARAM], [SHOUR_END], [STIME_END], [S_MENU], [THREAD_GROUP], [REMARK])
  VALUES(N'specialSched-2', N'STOP', N'特别排程-检查Resin状态', N'com.emis.schedule.epos.emisSpecialSched', N'I', N'', N'', N'', N'', N'', N'60', N'act=checkResinStatus&checkUrl=http://localhost:8046/jijitown2&execBat=D:\projects\jijitown2\resin-pro-3.0.25\emis\RestartResinVenus.bat&wait=10', N'', N'', '6', N'', NULL)

insert into SCHED(S_NAME,S_SERVER,S_DESC,S_CLASS,RUNLEVEL,SYEAR,SMONTH,SDAY,SHOUR,STIME,INTERVAL,PARAM,SHOUR_END,STIME_END,S_MENU,THREAD_GROUP,REMARK)
 values (N'specialSched-3',N'XYJ',N'特别排程-检查金点Resin状态',N'com.emis.schedule.epos.emisSpecialSched',N'I',N'',N'',N'',N'',N'',N'300',N'act=checkResinStatus&checkUrl=http://www.ezpos.cn:8105/jindian/&execBat=D:\projects\jindian2\resin-pro-3.0.25\emis\RestartResinVenus.bat&wait=10',N'',N'',N'6',N'',null)

 INSERT INTO [sched]([S_NAME], [S_SERVER], [S_DESC], [S_CLASS], [RUNLEVEL], [SYEAR], [SMONTH], [SDAY], [SHOUR], [STIME], [INTERVAL], [PARAM], [SHOUR_END], [STIME_END], [S_MENU], [THREAD_GROUP], [REMARK])
  VALUES(N'specialSched-4', N'XYJ', N'特别排程-GMX（检查客订上传解档异常）', N'com.emis.schedule.epos.emisSpecialSched', N'I', N'', N'', N'', N'', N'', N'60', N'http://erp.ks-gmx.com:8048/gmx4/jsp/dev/database/runSqlscript.jsp?ACT=GMX', N'', N'', '6', N'', NULL)
   */
  @Override
  protected void postAction() throws Exception {
    if (this.getParam() == null || "".equals(this.getParam())) {

      oLogger_.info("参数为空，不执行处理！");
      return;
    } else {
      oLogger_.info("Param-->"+this.getParam());
    }
    // 直接调用其它系统url
    if(this.getParam().startsWith("http://") || this.getParam().startsWith("https://")){
      this.oLogger_.info(callUrl(this.getParam().replaceAll("＆","&")));
    } else {
      Map<String,String> paramMap = new HashMap<String,String>();
      setParam(this.getParam().replaceAll("＆","&"));
      int idx = 0;
      for(String param : this.getParam().split("&")){
        idx = param.indexOf("=");
        paramMap.put(param.substring(0,idx), param.substring(idx+1));
      }
      // 检查Resin状态(注：不能检查自己系统，且两个系统需在同一台服务器上)
      if("checkResinStatus".equalsIgnoreCase(paramMap.get("act"))){
        int iWait = emisUtil.parseInt(paramMap.get("wait"), 10);

        String wait = (String)this.oContext_.getAttribute(this.getParam());

        if( wait != null && !"".equals(wait)){
          // 如有重启过resin, 则等N 次之后再做检查（因检查排程大概设1分钟执行一次）
          this.oLogger_.info(">>>>>>>> " + wait);
          if(Integer.parseInt(wait) < iWait) {
            this.oContext_.setAttribute(this.getParam(), (Integer.parseInt(wait) + 1) + "");
            return;
          } else {
            this.oContext_.removeAttribute(this.getParam());
          }
        }

        String serverUrl = paramMap.get("checkUrl");
        String execBat = paramMap.get("execBat");
        if(serverUrl == null || "".equals(serverUrl) || execBat == null || "".equals(execBat)){
          this.oLogger_.warn("设定参数不全[checkUrl/execBat]！");
        } else {
          checkResinStatus(serverUrl, execBat, paramMap.get("mailTo"));
        }
      } else if("runSqlscript".equalsIgnoreCase(paramMap.get("act"))){
        emisDirectory oDirRoot  = emisFileMgr.getInstance(oContext_).getDirectory("root");
        BufferedReader br = new BufferedReader(new FileReader(oDirRoot.getDirectory()+"/runSqlscript.cfg"));
        try {
          String line = "";
          int i = 0;
          StringBuffer sql = new StringBuffer("");
          while ((line = br.readLine()) != null) {
            oLogger_.info(">>> " + line.trim());
            oLogger_.info(callUrl(line.trim()));
          }
        } catch (Exception ee) {
          this.oLogger_.error(ee);
        } finally {
          if(br != null ) br.close();
        }
      } else if("callOtherSystem".equalsIgnoreCase(paramMap.get("act"))){
        String ret = callUrl(paramMap.get("url").replaceAll("#","&"));
        oLogger_.info("*** return json >> "+ret);
        try {
          JSONObject returnJson = JSONObject.fromObject(ret);

          if(returnJson.containsKey("content") && StringUtils.isNotEmpty(returnJson.getString("content"))){
            if(returnJson.containsKey("mailTo") && StringUtils.isNotEmpty(returnJson.getString("mailTo"))){
              emisMailer m = new emisMailer();
              m.setTo(returnJson.getString("mailTo"));
              m.setSubject(returnJson.getString("subject"));
              m.setContent(returnJson.getString("content"));
              m.send(oContext_);
              oLogger_.info(">>> Mail Ok!");
            }
          }
        } catch(Exception e){
          this.oLogger_.error(e);
        }
      }
    }
  }

  private String callUrl_bak(String sUrl) throws Exception {
    //String sUrl = "http://pos.jijitown.com.cn:8103/jijitown/jsp/dev/database/runSqlscript.jsp?ACT=jijitown";
    HttpURLConnection conn = null;
    //Scanner in = null;
    InputStream in = null;
    InputStreamReader streamReader = null;
    BufferedReader reader = null;
    try {
      this.oLogger_.info("Url-->"+sUrl);
      URL url = new URL(sUrl);
      conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(120 * 1000);
      conn.setReadTimeout(120 * 1000);
      /*
      in = new Scanner(conn.getInputStream());
      while (in.hasNextLine()) this.oLogger_.info(in.nextLine());
      */
      //指定UTF-8编码，不然中文会乱码
      in = conn.getInputStream();
      streamReader = new InputStreamReader(in,"UTF-8");
      reader = new BufferedReader(streamReader);
      StringBuilder sbBuilder = new StringBuilder();
      String s = null;
      while ((s = reader.readLine()) != null) {
        if("".equals(s)) continue;
        if(sbBuilder.length()>0) sbBuilder.append("\n");
        sbBuilder.append(s);
      }
      //this.oLogger_.info(sbBuilder.toString());
      return sbBuilder.toString();
    } finally {
      if(conn != null) {
        conn.disconnect();
        conn = null;
      }
      if(in != null) {
        in.close();
        in = null;
      }
      if(streamReader != null) {
        streamReader.close();
        streamReader = null;
      }
      if(reader != null) {
        reader.close();
        reader = null;
      }
    }
  }

  private String callUrl(String sUrl) throws Exception {
    //String sUrl = "http://pos.jijitown.com.cn:8103/jijitown/jsp/dev/database/runSqlscript.jsp?ACT=jijitown";
    InputStream in = null;
    InputStreamReader streamReader = null;
    BufferedReader reader = null;

    HttpClient httpClient = null;
    PostMethod method = null;
    try {
      this.oLogger_.info("Url-->"+sUrl);
      httpClient = new HttpClient();
      method = new PostMethod(sUrl);

      httpClient.getParams().setConnectionManagerTimeout(30*1000);
      httpClient.getParams().setContentCharset("UTF-8");
      method.setRequestHeader("ContentType", "application/x-www-form-urlencoded;charset=UTF-8");
      method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
      int state = httpClient.executeMethod(method);
      this.oLogger_.info("return state >>> " + state);

      in = method.getResponseBodyAsStream();
      streamReader = new InputStreamReader(in,"UTF-8");
      reader = new BufferedReader(streamReader);
      StringBuilder sbBuilder = new StringBuilder();
      String s = null;
      while ((s = reader.readLine()) != null) {
        if("".equals(s)) continue;
        if(sbBuilder.length()>0) sbBuilder.append("\n");
        sbBuilder.append(s);
      }
      return sbBuilder.toString();
    } finally {
      try {
        if (in != null) {
          in.close();
          in = null;
        }
        if (streamReader != null) {
          streamReader.close();
          streamReader = null;
        }
        if (reader != null) {
          reader.close();
          reader = null;
        }
      } catch(Exception e){

      }
      if(method!=null){
        method.releaseConnection();
        method = null;
      }

    }
  }

  private void checkResinStatus(String checkUrl, String execBat, String mailTo) throws Exception {
    this.oLogger_.info(checkUrl);
    URL url = new URL(checkUrl);
    HttpURLConnection conn = null;
    Scanner in = null;
    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(30 * 1000);
      conn.setReadTimeout(30 * 1000);
      int i = 0;
      boolean isExeption = false;
      while (i < 2) {
        isExeption = false;
        try {
          conn.getInputStream();
          this.oLogger_.info("********** OK ***********");
          this.oContext_.setAttribute(checkUrl, "1"); //记录该址有访问成功
          break;
        } catch (ConnectException e) {
          i++;
          isExeption = true;
          this.oLogger_.warn(">>>>>>>>> ConnectException:" + e.getMessage());
        } catch (SocketTimeoutException e2) {
          i++;
          isExeption = true;
          this.oLogger_.warn(">>>>>>>>> SocketTimeoutException:" + e2.getMessage());
        } catch (Exception ee) {
          this.oLogger_.error("访问Server出错", ee);
          break;
        }

        if (isExeption) {
          // 有访问成功过才重启，防止检查服务器的IP有变动末同步更新排程设定时会不断重启Resin
          if (!"1".equals(oContext_.getAttribute(checkUrl))) {
            this.oLogger_.error("请检查URL地址是否正确！[" + checkUrl + "]");
            break;
          }

          if (i == 2) {
            Process child = null;
            try {
              this.oContext_.setAttribute(this.getParam(), "1");
              String cmd = "cmd.exe /C start " + execBat;
              this.oLogger_.info(execBat);
              child = Runtime.getRuntime().exec(cmd);
              printMessage(child.getInputStream());
              printMessage(child.getErrorStream());
              child.waitFor();
            } catch (Exception e2) {
              this.oLogger_.error("", e2);
            } finally {
              if (child != null) child.destroy();
            }
            this.oLogger_.warn("访问Server超时，自动执行重启Bat[" + execBat + "]");
            try {
              if(mailTo != null && !"".equals(mailTo)) {
                emisProp prop = emisProp.getInstance(this.oContext_);
                emisMailer m = new emisMailer();
                m.setTo(mailTo);
                m.setSubject("访问超时，自动重启Resin-[" + prop.get("EPOS_SYSTEM_TITLE") + "]");
                m.setContent("访问地址：" + checkUrl + "  <br>执行重启Bat:" + execBat);
                m.send(oContext_);
              }
            } catch (Exception ee) {
              this.oLogger_.error(ee.getMessage());
            }
            break;
          }
          try {
            Thread.sleep(10 * 1000);  // 等10秒再检查一次
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
          }
        }
      }
    } finally {
      if (conn != null) {
        conn.disconnect();
        conn = null;
      }
      if (in != null){
        in.close();
        in = null;
      }
    }

  }

  private static void printMessage(final InputStream input) {
    new Thread(new Runnable() {
      public void run() {
        Reader reader = new InputStreamReader(input);
        BufferedReader bf = new BufferedReader(reader);
        String line = null;
        try {
          while ((line = bf.readLine()) != null) {
            System.out.println(line);
          }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          try {
            if (reader != null) reader.close();
            if (bf != null) bf.close();
          } catch(Exception e){
            e.printStackTrace();
          }
        }
      }
    }).start();
  }
}
