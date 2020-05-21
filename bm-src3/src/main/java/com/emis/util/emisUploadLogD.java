package com.emis.util;


import com.emis.db.emisDb;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 上傳檔解檔明細 LOG.
 * User: Andy
 * Date: 2009-2-5
 * Time: 11:25:57
 * To change this template use File | Settings | File Templates.
 */
public class emisUploadLogD {
  private emisDb oDb = null;
  private ServletContext oContext = null;
  private Logger oLog = null;

  private PreparedStatement oInsStmt, oUpdStmt ;
  private List oLogList;
  private HashMap oRowMap;

  public emisUploadLogD(emisDb oDb, Logger oLog){
    this.oDb = oDb;
    this.oLog = oLog;
    oLogList = new ArrayList();
    try{
      oInsStmt = oDb.prepareStmt("insert into Upload_log_d(UL_FILE_ZIP,UL_FILE,UL_FILE_SIZE,UL_PROC_D,UL_PROC_T,UL_OK_COUNT,UL_FAIL_COUNT,UL_EXCEPTION,DEF1,DEF2) \n" +
              " values(?,?,?,?,?,?,?,?,?,?);");
      oUpdStmt = oDb.prepareStmt("update Upload_log_d set UL_FILE_SIZE = ?,UL_PROC_D = ?,UL_PROC_T = ?,UL_OK_COUNT = ?,UL_FAIL_COUNT = ?,UL_EXCEPTION = ?" +
          ",DEF1 = ?,DEF2 = Isnull(DEF2,?) \nwhere UL_FILE_ZIP = ? and UL_FILE = ?");
    } catch(SQLException e){
      e.printStackTrace();
      if(oLog != null) oLog.warn("emisUploadLogD",e);
    }
  }

  public void addLogRow(){
    oRowMap = new HashMap();
    oLogList.add(oRowMap);
  }

  public void setUL_S_NO(String sVal){
    oRowMap.put("UL_S_NO",sVal);
  }

  public void setUL_FILE_ZIP(String sVal){
    oRowMap.put("UL_FILE_ZIP",sVal);
  }

  public void setUL_FILE(String sVal){
    oRowMap.put("UL_FILE",sVal);
  }

  public void setUL_FILE_SIZE(String sVal){
    oRowMap.put("UL_FILE_SIZE",sVal);
  }

  public void setUL_PROC_D(String sVal){
    oRowMap.put("UL_PROC_D",sVal);
  }

  public void setUL_PROC_T(String sVal){
    oRowMap.put("UL_PROC_T",sVal);
  }

  public void setUL_OK_COUNT(String sVal){
    oRowMap.put("UL_OK_COUNT",sVal);
  }

  public void setUL_FAIL_COUNT(String sVal){
    oRowMap.put("UL_FAIL_COUNT",sVal);
  }

  public void setUL_EXCEPTION(String sVal){
    if(sVal != null){
      if(sVal.length() > 2500){
        sVal = sVal.substring(0,2500);
      }
    }
    oRowMap.put("UL_EXCEPTION",sVal);
  }

  public void setDEF1(String sVal){
    oRowMap.put("DEF1",sVal);
  }

  public void setDEF2(String sVal){
    oRowMap.put("DEF2",sVal);
  }

  public void addRow(String UL_FILE_ZIP,String UL_FILE,String UL_FILE_SIZE,String UL_PROC_D,String UL_PROC_T,String UL_OK_COUNT
      ,String UL_FAIL_COUNT,String UL_EXCEPTION,String DEF1,String DEF2){
    oRowMap = new HashMap();
    oLogList.add(oRowMap);
    oRowMap.put("UL_FILE_ZIP",UL_FILE_ZIP);
    oRowMap.put("UL_FILE",UL_FILE);
    oRowMap.put("UL_FILE_SIZE",UL_FILE_SIZE);
    oRowMap.put("UL_PROC_D",UL_PROC_D);
    oRowMap.put("UL_PROC_T",UL_PROC_T);
    oRowMap.put("UL_OK_COUNT",UL_OK_COUNT);
    oRowMap.put("UL_FAIL_COUNT",UL_FAIL_COUNT);
    if(UL_EXCEPTION != null && UL_EXCEPTION.length()> 2000){
      oRowMap.put("UL_EXCEPTION",UL_EXCEPTION.substring(0,2000));
    } else {
      oRowMap.put("UL_EXCEPTION",UL_EXCEPTION);
    }
    oRowMap.put("DEF1",DEF1);
    oRowMap.put("DEF2",DEF2);
  }

  public void insert(){
    try {
      for(int i = 0; i < this.oLogList.size(); i++){
        try{
          this.oRowMap = (HashMap)oLogList.get(i);
          if(oRowMap.get("UL_FILE") == null || "".equals((String)oRowMap.get("UL_FILE"))) continue;
          try{
            oInsStmt.setString(1,(String)oRowMap.get("UL_FILE_ZIP"));
            oInsStmt.setString(2,(String)oRowMap.get("UL_FILE"));
            oInsStmt.setString(3,(String)oRowMap.get("UL_FILE_SIZE"));
            oInsStmt.setString(4,(String)oRowMap.get("UL_PROC_D"));
            oInsStmt.setString(5,(String)oRowMap.get("UL_PROC_T"));
            oInsStmt.setString(6,(String)oRowMap.get("UL_OK_COUNT"));
            oInsStmt.setString(7,(String)oRowMap.get("UL_FAIL_COUNT"));
            oInsStmt.setString(8,(String)oRowMap.get("UL_EXCEPTION"));
            oInsStmt.setString(9,(String)oRowMap.get("DEF1"));
            //oInsStmt.setString(10,(String)oRowMap.get("DEF2"));
            oInsStmt.setString(10,(String)oRowMap.get("UL_PROC_T"));
            oInsStmt.executeUpdate();
          } catch(SQLException sqle){
            if(sqle.getErrorCode() == 2627){  // 主鍵重複
              oUpdStmt.setString(1,(String)oRowMap.get("UL_FILE_SIZE"));
              oUpdStmt.setString(2,(String)oRowMap.get("UL_PROC_D"));
              oUpdStmt.setString(3,(String)oRowMap.get("UL_PROC_T"));
              oUpdStmt.setString(4,(String)oRowMap.get("UL_OK_COUNT"));
              oUpdStmt.setString(5,(String)oRowMap.get("UL_FAIL_COUNT"));
              oUpdStmt.setString(6,(String)oRowMap.get("UL_EXCEPTION"));
              oUpdStmt.setString(7,(String)oRowMap.get("DEF1"));
              oUpdStmt.setString(8,(String)oRowMap.get("DEF2"));
              oUpdStmt.setString(9,(String)oRowMap.get("UL_FILE_ZIP"));
              oUpdStmt.setString(10,(String)oRowMap.get("UL_FILE"));
              oUpdStmt.executeUpdate();
            }
          }
        } catch(Exception e){
          e.printStackTrace();
          oLog.warn("emisUploadLogD",e);
        }
      }
      clear();
    } catch (Exception e) {
      e.printStackTrace();
      if(oLog != null) oLog.warn("emisUploadLogD",e);
    }
  }

  // 因鎖原因的上傳檔再處理排程中調用，更新ＬＯＧ記錄。
  public void update(){
    String sDef1 = "";
    try {
      for(int i = 0; i < this.oLogList.size(); i++){
        try{
          this.oRowMap = (HashMap)oLogList.get(i);
          if(oRowMap.get("UL_FILE") == null || "".equals((String)oRowMap.get("UL_FILE"))) continue;

          oUpdStmt.setString(1,(String)oRowMap.get("UL_FILE_SIZE"));
          oUpdStmt.setString(2,(String)oRowMap.get("UL_PROC_D"));
          oUpdStmt.setString(3,(String)oRowMap.get("UL_PROC_T"));
          oUpdStmt.setString(4,(String)oRowMap.get("UL_OK_COUNT"));
          oUpdStmt.setString(5,"0");
          oUpdStmt.setString(6,(String)oRowMap.get("UL_EXCEPTION"));
          sDef1 = (String)oRowMap.get("DEF1");
          if("".equals(sDef1)){
            sDef1 = "2";   // 區別一下解檔正常的ＬＯＧ，後續可以查看有哪些上傳資料解檔時有被鎖過。
          }
          oUpdStmt.setString(7,sDef1);
          oUpdStmt.setString(8,(String)oRowMap.get("DEF2"));
          oUpdStmt.setString(9,(String)oRowMap.get("UL_FILE_ZIP"));
          oUpdStmt.setString(10,(String)oRowMap.get("UL_FILE"));
          oUpdStmt.executeUpdate();
        } catch(Exception e){
          e.printStackTrace();
          oLog.warn("emisUploadLogD",e);
        }
      }
      clear();
    } catch (Exception e) {
      e.printStackTrace();
      if(oLog != null) oLog.warn("emisUploadLogD",e);
    }
  }

  public void clear(){
    if(this.oLogList != null && oLogList.size() > 0){
      for(int i=0 ; i<oLogList.size(); i++){
        ((HashMap)oLogList.get(i)).clear();
      }
      oLogList.clear();
    }
  }

  public void close(){
    try{
      clear();
      if(this.oInsStmt != null){
        oDb.closePrepareStmt(oInsStmt);
        oInsStmt = null;
      }
    } catch(Exception e){
      ;
    }
  }
}