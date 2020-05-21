package com.emis.util;


import com.emis.db.emisDb;
import com.emis.db.emisFieldFormat;
import com.emis.db.emisFieldFormatBean;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lenovo
 * Date: 2009-2-5
 * Time: 11:25:57
 * To change this template use File | Settings | File Templates.
 * fang 2010/02/05 通過dephi　sas上傳時無法直接取得門市編號，需通過文件名來取門店編號
 * Joe 2015/09/30 modify #31728:
 * 1. 修正emisUploadLogH中计算门店编号错误逻辑;
 * 2. 修正更新上传记录档的备注栏位出现异常问题;
 * 3. 修正emisAbstractSchEOD解销售或客订档时因发票档导致多次解档;
 * 4. 修正emisAbstractSchEOD初始化方法init先清除一下工作目录work;
 */
public class emisUploadLogH {
  private emisDb oDb = null;
  private ServletContext oContext = null;
  private boolean bIsNewDb = false;
  private Logger oLog = null;

  private PreparedStatement oInsStmt,oUpdStmt ;
  private List oLogList;
  private HashMap oRowMap;
  private int iS_NO_len = 6;

  public emisUploadLogH(ServletContext oContext, Logger oLog) {
    try{
      this.oContext = oContext;
      this.oLog = oLog;
      oLogList = new ArrayList();
      oDb = emisDb.getInstance(oContext);
      bIsNewDb = true;
    } catch(Exception e ){
      if(bIsNewDb && oDb != null){
        oDb.close();
        oDb = null;
      }
    }
    // 門市編號長度取FieldFormat表中的設定。
    try{
      emisFieldFormatBean bean = emisFieldFormat.getInstance(oContext).getBean("S_NO");
			iS_NO_len = bean.getMaxLen();
    } catch(Exception e){
      iS_NO_len = 6;
    }
  }

  public emisUploadLogH(emisDb oDb, Logger oLog) throws Exception {
    this.oLog = oLog;
    oLogList = new ArrayList();
    this.oDb = oDb;
    bIsNewDb = false;
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

  public void setUL_DATE(String sVal){
    oRowMap.put("UL_DATE",sVal);
  }

  public void setUL_ZIP_SIZE(String sVal){
    oRowMap.put("UL_ZIP_SIZE",sVal);
  }

  public void setUL_TIME_S(String sVal){
    oRowMap.put("UL_TIME_S",sVal);
  }

  public void setUL_TIME_E(String sVal){
    oRowMap.put("UL_TIME_E",sVal);
  }

  /**
  *  轉檔狀態：
    0-	暫未處理
    1-	轉檔成功
    2-	已處理，有資料重複
    3-	已處理，有異常
    4- 轉檔失敗
   */
  public void setFLS_NO(String sVal){
    if(oRowMap.get("FLS_NO") == null)  {
       oRowMap.put("FLS_NO",sVal);
    } else if(sVal.compareTo((String)oRowMap.get("FLS_NO")) > 0 ){
      oRowMap.put("FLS_NO",sVal);
    }
  }

  public void setUL_CLEAR(String sVal){
    oRowMap.put("UL_CLEAR",sVal);
  }

  public void setUL_FILE_DIR(String sVal){
    oRowMap.put("UL_FILE_DIR",sVal);
  }

  public void setUL_FILE_BAK(String sVal){
    oRowMap.put("UL_FILE_BAK",sVal);
  }

  public void setUL_AP(String sVal){
    oRowMap.put("UL_AP",sVal);
  }

  public void setUL_PROCE_D(String sVal){
    oRowMap.put("UL_PROCE_D",sVal);
  }

  public void setDEF1(String sVal){
    oRowMap.put("DEF1",sVal);
  }

  public void setDEF2(String sVal){
    oRowMap.put("DEF2",sVal);
  }

  public void setREMARK(String sVal){
    if(sVal != null){
      if(oRowMap.get("REMARK") != null){
        sVal = oRowMap.get("REMARK")  + "; "+ sVal  ;
      }
      if(sVal.length() > 500){
        sVal = sVal.substring(0,500);
      }
    }
    oRowMap.put("REMARK",sVal);
  }

  public String getStoreNo() {
    String sStoreNo = (String) oRowMap.get("UL_S_NO");
    // 通過dephi　sas上傳時無法直接取得門市編號，需通過文件名來取門店編號
    if (sStoreNo == null || "".equals(sStoreNo)) {
      sStoreNo = (String) oRowMap.get("UL_FILE_ZIP");
      // X.0050051.0001.20150930125355.ZIP
      // X.0050051.20150930124356VERSION.TXT
      if (sStoreNo.startsWith("X")) {
        if (sStoreNo.split("\\.").length > 3) {   //非日結檔
          sStoreNo = sStoreNo.split("\\.")[1];
        } else {   //　日結檔（X000001001.20100201210307.ZIP）
          sStoreNo = sStoreNo.substring(1, iS_NO_len + 1);
        }
      } else if (sStoreNo.startsWith("TXT")) {
        // TXT.20150929.005.0051.20150930125332.ZIP
        return sStoreNo.split("\\.")[2];
      } else if (sStoreNo.startsWith("EXPORT.")) {
        // EXPORT.05059524.050595241.20170616080200.USERRIGHTS.DAT
        return sStoreNo.split("\\.")[1];
      }
      // 20150930 Joe 先从DB查询，再依原来规则截取
      String sSno = queryStoreNo(sStoreNo);
      if ("".equals(sSno) && sStoreNo.length() > iS_NO_len) {
        sStoreNo = sStoreNo.substring(0, iS_NO_len);
      } else {
        sStoreNo = sSno;
      }
    }
    return sStoreNo;
  }

  /**
   * 根据档案中的门店编号+机台号查询真正的门店编号
   * @param sSnoIdno
   * @return
   */
  private String queryStoreNo(String sSnoIdno) {
    PreparedStatement oQrySnoStmt = null;
    ResultSet rs = null;
    String sS_NO = "";
    try {
      oQrySnoStmt = oDb.prepareStmt("select S_NO from cash_id with (nolock) where S_NO+ID_NO=?");
      oQrySnoStmt.setString(1, sSnoIdno);
      rs = oQrySnoStmt.executeQuery();
      if (rs.next()) {
        sS_NO = rs.getString("S_NO");
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (this.oLog != null) oLog.warn("emisUploadLogH queryStoreNo", e);
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (oQrySnoStmt != null)
          oQrySnoStmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
        if (this.oLog != null) oLog.warn("emisUploadLogH queryStoreNo close resource", e);
      }
    }
    return sS_NO;
  }

  public int getUlType(){
    int iRetVal = 0 ;

    String ulFileDir = ((String) oRowMap.get("UL_FILE_DIR")).toLowerCase();
    String sTemp = ((String) oRowMap.get("UL_FILE_ZIP")).toUpperCase();
    if(ulFileDir.indexOf("realtime") > 0 ){
      if(sTemp.indexOf("DAILYCLOSE_AMT") > 0) {
        iRetVal = 2;  // 日結对帐資料
      } else {
        iRetVal = 1; // 即時交易資料。
      }
    } else if (ulFileDir.indexOf("endofday") > 0 || ulFileDir.indexOf("txt") > 0){
      iRetVal = 2;  // 日結資料
    } else if(ulFileDir.indexOf("bill") > 0 ){ // 单据上传档
      iRetVal = 6;
    } else if(ulFileDir.indexOf("logs") > 0 ){
      if (sTemp.indexOf("ERROR.TXT") > 0){
        iRetVal = 5;  // 门店异常信息
      } else {
        iRetVal = 3;  // 前台解檔/版本信息
      }
    } else if(ulFileDir.indexOf("backupdat") > 0 ){
      iRetVal = 4;  // 前台备份档
    }
    return iRetVal;
  }

  // 是否將該ＬＯＧ記錄到ＤＢ？　Ｎ-不記錄，否則記錄。
  public void setIS_INSDB(String sVal){
    oRowMap.put("IS_INSDB",sVal);
  }

  /**
   * 記錄初始ＬＯＧ信息,上傳資料時
   */
  public void insert(){
    try {
      if(oInsStmt == null){
        oInsStmt = oDb.prepareStmt("insert into Upload_log_h(UL_FILE_ZIP,UL_S_NO,UL_DATE,UL_ZIP_SIZE,UL_TIME_S,UL_TIME_E,FLS_NO,UL_FILE_DIR,UL_AP,UL_TYPE) \n" +
            " values(?,?,?,?,?,?,?,?,?,?);");
      }
      String ulFileZip = null;
      for(int i = 0; i < this.oLogList.size(); i++){
        try{
          this.oRowMap = (HashMap)oLogList.get(i);
          ulFileZip = (String)oRowMap.get("UL_FILE_ZIP");
          if("".equals(ulFileZip))  continue;
          oInsStmt.clearParameters();
          oInsStmt.setString(1,ulFileZip);
          oInsStmt.setString(2,getStoreNo());
          oInsStmt.setString(3,(String)oRowMap.get("UL_DATE"));
          oInsStmt.setString(4,(String)oRowMap.get("UL_ZIP_SIZE"));
          oInsStmt.setString(5,(String)oRowMap.get("UL_TIME_S"));
          oInsStmt.setString(6,(String)oRowMap.get("UL_TIME_E"));
          if(ulFileZip.startsWith("TXT.") && ulFileZip.endsWith(".ZIP")){  //前台清机的报表，无需处理
            oInsStmt.setString(7, "1");
          } else {
            oInsStmt.setString(7, "0");
          }
          oInsStmt.setString(8,(String)oRowMap.get("UL_FILE_DIR"));
          oInsStmt.setString(9,(String)oRowMap.get("UL_AP"));
          oInsStmt.setInt(10,getUlType());
          oInsStmt.executeUpdate();
        } catch(Exception e){
          e.printStackTrace();
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (Exception ee) {
      ee.printStackTrace();
    } finally {
      clear();
    }
  }

  public void addUpdRow(String UL_FILE_ZIP,String FLS_NO,String UL_FILE_BAK,String UPD_DATE_TIME,String REMARK){
    oRowMap = new HashMap();
    oLogList.add(oRowMap);
    oRowMap.put("UL_FILE_ZIP",UL_FILE_ZIP);
    oRowMap.put("FLS_NO", FLS_NO);
    oRowMap.put("UL_FILE_BAK",UL_FILE_BAK);
    oRowMap.put("DEF2",UPD_DATE_TIME);
    oRowMap.put("REMARK",REMARK);
  }

  public void addUpdRow(String UL_FILE_ZIP,String FLS_NO,String REMARK){
    oRowMap = new HashMap();
    oLogList.add(oRowMap);
    oRowMap.put("UL_FILE_ZIP",UL_FILE_ZIP);
    oRowMap.put("FLS_NO",FLS_NO);
    oRowMap.put("REMARK",REMARK);
  }

  public void update() {
    try {
      if(oUpdStmt == null) {
        oUpdStmt = oDb.prepareStmt("update Upload_log_h set FLS_NO = ?,UL_FILE_BAK = ?,UL_PROCE_D = ?,REMARK = ? where UL_FILE_ZIP = ?");
      }
      int idx;
      String sRemark;
      for (int i = 0; i < this.oLogList.size(); i++) {
        try {
          this.oRowMap = (HashMap) oLogList.get(i);
          sRemark = (String) oRowMap.get("REMARK");

          oUpdStmt.clearParameters();
          idx = 1;
          oUpdStmt.setString(idx++, (String) oRowMap.get("FLS_NO"));
          oUpdStmt.setString(idx++, (String) oRowMap.get("UL_FILE_BAK"));
          oUpdStmt.setString(idx++, (String) oRowMap.get("UL_PROCE_D"));
          oUpdStmt.setString(idx++, (sRemark == null? "": sRemark));
          oUpdStmt.setString(idx, (String) oRowMap.get("UL_FILE_ZIP"));
          oUpdStmt.executeUpdate();
        } catch (Exception e) {
          e.printStackTrace();
          if (this.oLog != null) oLog.warn("emisUploadLogH", e);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (Exception ee) {
      ee.printStackTrace();
    } finally {
      clear();
    }
  }


  public void update2(){
    try {
      if(oUpdStmt == null){
        oUpdStmt = oDb.prepareStmt("update Upload_log_h set FLS_NO = ?,REMARK = ? where UL_FILE_ZIP = ? and FLS_NO = '3'");
      }
      for(int i = 0; i < this.oLogList.size(); i++){
        try{
          this.oRowMap = (HashMap)oLogList.get(i);
          oUpdStmt.clearParameters();
          oUpdStmt.setString(1,(String)oRowMap.get("FLS_NO"));
          oUpdStmt.setString(2,(String)oRowMap.get("REMARK"));
          oUpdStmt.setString(3,(String)oRowMap.get("UL_FILE_ZIP"));
          oUpdStmt.executeUpdate();
        } catch(Exception e){
          e.printStackTrace();
          if(this.oLog != null) oLog.warn("emisUploadLogH",e);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (Exception ee) {
      ee.printStackTrace();
    } finally {
      clear();
    }
  }

  public void clear(){
    if(this.oLogList != null){
      for(int i=0 ; i<oLogList.size(); i++){
        ((HashMap)oLogList.get(i)).clear();
      }
      oLogList.clear();
    }
  }

  public void close(){
    try{
      if(oInsStmt != null){
        oDb.closePrepareStmt(oInsStmt);
        oInsStmt = null;
      }
      if(oUpdStmt != null){
        oDb.closePrepareStmt(oUpdStmt);
        oUpdStmt = null;
      }
      if(bIsNewDb && oDb != null) {
        oDb.close();
        oDb = null;
      }
      if(this.oLogList != null){
        for(int i=0 ; i<oLogList.size(); i++){
          ((HashMap)oLogList.get(i)).clear();
        }
        oLogList.clear();
        oLogList = null;
      }
    } catch(Exception e){
      ;
    }
  }
}