package com.emis.app.migration;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.db.emisRowSet;

import javax.servlet.ServletContext;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 下載Log記錄.
 * User: andy
 * Date: 2009-2-5
 * Time: 11:25:57
 */
public class emisMiLogToDb {
  private emisDb oDb = null;
  private ServletContext oContext = null;
  private String DL_FILE,DL_FILE_D,DL_FILE_T,DL_S_NO,DL_ID_NO="",DL_FILE_ROWS,DL_FILE_DIR,DL_FILE_BAK,FLS_NO ;
  private String DL_NEW_FILE,DL_DWN_D,DL_DWN_T1,DL_DWN_T2,DL_IMP_D,DL_IMP_T1,DL_IMP_T2,DL_IMP_ROWS,DL_IMP_INFO;
  private long DL_FILE_SIZE;
  private String CRE_USER,DL_AP;
  private String sEP_DOWNFORIDNO = "N";
  private String sEP_IS_STORESERVER = "N";
  private emisRowSet oCashIdSet;
  private PreparedStatement oInsStmt,oUpdStmt,oUpdStmtExp,oUpdStmt2,oUpdStmt3,oQryStmt ;
  private boolean bNewDb = false;

  public emisMiLogToDb(ServletContext oContext){
    this.oContext = oContext;
    try {
      this.oDb = emisDb.getInstance(oContext);
      bNewDb = true;
      sEP_DOWNFORIDNO = emisProp.getInstance(oContext).get("EP_DOWNFORIDNO");
      sEP_IS_STORESERVER = emisProp.getInstance(oContext).get("EP_IS_STORESERVER");
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  public emisMiLogToDb(ServletContext oContext,emisDb oDb){
    this.oContext = oContext;
    try {
      this.oDb = oDb;
      sEP_DOWNFORIDNO = emisProp.getInstance(oContext).get("EP_DOWNFORIDNO");
      sEP_IS_STORESERVER = emisProp.getInstance(oContext).get("EP_IS_STORESERVER");
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  public void setDL_FILE(String DL_FILE) {         
    this.DL_FILE = DL_FILE;
  }

  public void setDL_FILE_D(String DL_FILE_D) {
    this.DL_FILE_D = DL_FILE_D;
  }

  public void setDL_FILE_T(String DL_FILE_T) {
    this.DL_FILE_T = DL_FILE_T;
  }

  public void setDL_S_NO(String DL_S_NO) {
    this.DL_S_NO = DL_S_NO;
  }

  public void setDL_ID_NO(String DL_ID_NO) {
    if(DL_ID_NO != null){
      this.DL_ID_NO = DL_ID_NO.trim();
    } else {
      this.DL_ID_NO = "";
    }
  }

  public void setDL_FILE_ROWS(String DL_FILE_ROWS) {
    this.DL_FILE_ROWS = DL_FILE_ROWS;
  }

  public void setDL_FILE_SIZE(long DL_FILE_SIZE) {
    this.DL_FILE_SIZE = DL_FILE_SIZE;
  }

  public void setDL_FILE_DIR(String DL_FILE_DIR) {
    if(DL_FILE_DIR != null && !"".equals(DL_FILE_DIR)){
      this.DL_FILE_DIR =DL_FILE_DIR.replaceAll("\\\\","/");
    } else {
      this.DL_FILE_DIR = "";
    }
  }

  public void setDL_FILE_BAK(String DL_FILE_BAK) {
    this.DL_FILE_BAK = DL_FILE_BAK;
  }

  public void setFLS_NO(String FLS_NO) {
    this.FLS_NO = FLS_NO;
  }

  public void setDL_NEW_FILE(String DL_NEW_FILE) {
    this.DL_NEW_FILE = DL_NEW_FILE;
  }

  public void setDL_DWN_D(String DL_DWN_D) {
    this.DL_DWN_D = DL_DWN_D;
  }

  public void setDL_DWN_T1(String DL_DWN_T1) {
    this.DL_DWN_T1 = DL_DWN_T1;
  }

  public void setDL_DWN_T2(String DL_DWN_T2) {
    this.DL_DWN_T2 = DL_DWN_T2;
  }

  public void setDL_IMP_D(String DL_IMP_D) {
    this.DL_IMP_D = DL_IMP_D;
  }

  public void setDL_IMP_T1(String DL_IMP_T1) {
    this.DL_IMP_T1 = DL_IMP_T1;
  }

  public void setDL_IMP_T2(String DL_IMP_T2) {
    this.DL_IMP_T2 = DL_IMP_T2;
  }

  public void setDL_IMP_ROWS(String DL_IMP_ROWS) {
    this.DL_IMP_ROWS = DL_IMP_ROWS;
  }

  public void setDL_IMP_INFO(String DL_IMP_INFO) {
    this.DL_IMP_INFO = DL_IMP_INFO;
  }

  public void setCRE_USER(String CRE_USER) {
    if(CRE_USER == null || "".equals(CRE_USER)){
      this.CRE_USER = "排程自動執行";
    } else {
      this.CRE_USER = CRE_USER;
    }
  }

  public void setDL_AP(String DL_AP) {
    this.DL_AP = DL_AP;
  }

  /**
   * 記錄初如ＬＯＧ信息，並將舊檔清除及作標識
   */
  public void insertProc(){
    emisRowSet oRS = null;
    File oFile = null;
    try {
      if(oQryStmt == null ){
        oQryStmt = oDb.prepareStmt("select DL_FILE,DL_FILE_DIR from Download_log with(nolock) where DL_FILE like ? and DL_S_NO = ? and DL_ID_NO = ? and FLS_NO='0'");
      }

      oQryStmt.setString(1,DL_FILE.split("\\.")[0]+".%");
      oQryStmt.setString(2,DL_S_NO);
      oQryStmt.setString(3,DL_ID_NO);
      oRS = new emisRowSet(oQryStmt.executeQuery());

      if(oUpdStmt==null){
        oUpdStmt = oDb.prepareStmt("update Download_log set FLS_NO = '2', DL_NEW_FILE = ? where DL_FILE = ? and DL_S_NO = ? and DL_ID_NO = ? and FLS_NO ='0'");
      }
      String sOldDL_FILE = "";
      while(oRS.next()){
        sOldDL_FILE = oRS.getString("DL_FILE");

        oFile = new File(oRS.getString("DL_FILE_DIR"),sOldDL_FILE);
        if(oFile != null && oFile.canWrite()
            && (!DL_FILE.startsWith("ADD")     // 非異動檔
               || DL_FILE.split("\\.")[1].substring(0,8).equals(sOldDL_FILE.split("\\.")[1].substring(0,8)))){     //　異動檔只替換當天的。
          if(oFile.delete()){
            
          }
          oUpdStmt.setString(1,DL_FILE);
          oUpdStmt.setString(2,sOldDL_FILE);
          oUpdStmt.setString(3,DL_S_NO);
          oUpdStmt.setString(4,DL_ID_NO);
          oUpdStmt.executeUpdate();
        }
      }
      if(oInsStmt == null){
        oInsStmt = oDb.prepareStmt("insert into Download_log(DL_FILE,DL_FILE_D,DL_FILE_T,DL_S_NO,DL_ID_NO,DL_FILE_SIZE,DL_FILE_ROWS,DL_FILE_DIR,CRE_USER,FLS_NO) \n" +
          " values(?,?,?,?,?,?,?,?,?,'0');");
      }
      oInsStmt.setString(1,DL_FILE);
      oInsStmt.setString(2,DL_FILE_D);
      oInsStmt.setString(3,DL_FILE_T);
      oInsStmt.setString(4,DL_S_NO);
      oInsStmt.setString(5,DL_ID_NO);
      oInsStmt.setLong(6,DL_FILE_SIZE);
      oInsStmt.setString(7,DL_FILE_ROWS);
      oInsStmt.setString(8,DL_FILE_DIR);
      oInsStmt.setString(9,CRE_USER);
      oInsStmt.executeUpdate();
    } catch (SQLException e) {
      if(e.getErrorCode() == 2627 || e.getErrorCode() == 1){   // 主鍵重複，可能下載的Log先寫了。
        try{
          if(oUpdStmtExp == null){
            oUpdStmtExp = oDb.prepareStmt("update Download_log set DL_FILE_D = ?,DL_FILE_T = ?,DL_FILE_SIZE = ?,DL_FILE_ROWS  =?, DL_FILE_DIR = ?,CRE_USER = ? \n" +
                "where DL_FILE = ? and DL_S_NO = ? and DL_ID_NO = ?");
          }
          oUpdStmtExp.setString(1,DL_FILE_D);
          oUpdStmtExp.setString(2,DL_FILE_T);
          oUpdStmtExp.setLong(3,DL_FILE_SIZE);
          oUpdStmtExp.setString(4,DL_FILE_ROWS);
          oUpdStmtExp.setString(5,DL_FILE_DIR);
          oUpdStmtExp.setString(6,CRE_USER);
          oUpdStmtExp.setString(7,DL_FILE);
          oUpdStmtExp.setString(8,DL_S_NO);
          oUpdStmtExp.setString(9,DL_ID_NO);
          oUpdStmtExp.executeUpdate();
        } catch(Exception e2){
          e2.printStackTrace();
        }
      } else {
        e.printStackTrace();
      }
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }

  /**
   * 記錄下傳信息
   */
  public void download(){
    try{
      if(oUpdStmt2 == null){
        if(!"Y".equalsIgnoreCase(sEP_IS_STORESERVER)){   // 門市無小後台的架構
          oUpdStmt2 = oDb.prepareStmt("update Download_log set DL_FILE_BAK = ?, FLS_NO = '1', DL_DWN_D = ?, DL_DWN_T1 = ?, DL_DWN_T2 =?, DL_AP = ? \n" +
              "where DL_FILE = ? and DL_S_NO = ? and DL_ID_NO = ? ");
        } else {  //　門市有小後台的架構
          oUpdStmt2 = oDb.prepareStmt("update Download_log set DL_FILE_BAK = ?, FLS_NO = '1', DL_DWN_D = ?, DL_DWN_T1 = ?, DL_DWN_T2 =?, DL_AP = ? \n" +
              "where DL_FILE = ? and DL_S_NO = ?");
        }
      }
      oUpdStmt2.setString(1,DL_FILE_BAK);
      oUpdStmt2.setString(2,DL_DWN_D);
      oUpdStmt2.setString(3,DL_DWN_T1);
      oUpdStmt2.setString(4,DL_DWN_T2);
      oUpdStmt2.setString(5,DL_AP);
      oUpdStmt2.setString(6,DL_FILE);
      oUpdStmt2.setString(7,DL_S_NO);
      if(!"Y".equalsIgnoreCase(sEP_IS_STORESERVER)) {    // 門店無小後台的架構
        if("Y".equalsIgnoreCase(sEP_DOWNFORIDNO)){    // 系統參數設定為按門店產生下傳檔的情況。
          oUpdStmt2.setString(8,DL_ID_NO);
        } else {
          oUpdStmt2.setString(8,"");
        }
      }
      if(oUpdStmt2.executeUpdate() == 0){  // 更新不到，則作新增。
        if(oInsStmt == null){
          oInsStmt = oDb.prepareStmt("insert into Download_log (DL_FILE,DL_S_NO,DL_ID_NO,DL_FILE_BAK,DL_DWN_D,DL_DWN_T1,DL_DWN_T2,FLS_NO,DL_AP)\n" +
              "values(?,?,?,?,?,?,?,?,?)");
        }
        oInsStmt.setString(1,DL_FILE);
        oInsStmt.setString(2,DL_S_NO);
        // oInsStmt.setString(3,DL_ID_NO);
        oInsStmt.setString(4,DL_FILE_BAK);
        oInsStmt.setString(5,DL_DWN_D);
        oInsStmt.setString(6,DL_DWN_T1);
        oInsStmt.setString(7,DL_DWN_T2);
        oInsStmt.setString(8,"1");
        oInsStmt.setString(9,DL_AP);
        if(!"Y".equalsIgnoreCase(sEP_IS_STORESERVER)){    // 無門店小後台的架構
          if("Y".equalsIgnoreCase(sEP_DOWNFORIDNO)){    // 系統參數設定為按門店產生下傳檔的情況。
            oInsStmt.setString(3,DL_ID_NO);
          } else {
            oInsStmt.setString(3,"");
          }
          oInsStmt.executeUpdate();
        } else {     // 有門店小後台的架構
          if(oCashIdSet == null) {
            oCashIdSet = new emisRowSet(oDb.executeQuery("select ID_NO from Cash_id where S_NO='" + DL_S_NO +"'"));
          }
          oCashIdSet.first();
          while(oCashIdSet.next()){
            oInsStmt.setString(3,oCashIdSet.getString("ID_NO"));
            oInsStmt.executeUpdate();
          }
        }
      }
    } catch(SQLException e){
      e.printStackTrace();
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * 記錄門市轉入信息
   */
  public void update(){
    try{
      if(oUpdStmt3 == null){
        oUpdStmt3 = oDb.prepareStmt("update Download_log set FLS_NO = ? \n" +
          "  ,DL_IMP_D = ?, DL_IMP_T1 = ?, DL_IMP_T2 =?, DL_IMP_ROWS = ?, DL_IMP_INFO = ? \n" +
          "where DL_FILE = ? and DL_S_NO = ? and DL_ID_NO = ? ");
      }
      oUpdStmt3.setString(1,FLS_NO);
      oUpdStmt3.setString(2,DL_IMP_D);
      oUpdStmt3.setString(3,DL_IMP_T1);
      oUpdStmt3.setString(4,DL_IMP_T2);
      oUpdStmt3.setString(5,DL_IMP_ROWS);
      oUpdStmt3.setString(6,DL_IMP_INFO);
      oUpdStmt3.setString(7,DL_FILE);
      oUpdStmt3.setString(8,DL_S_NO);
      if("N".equalsIgnoreCase(this.sEP_DOWNFORIDNO) && !"Y".equalsIgnoreCase(sEP_IS_STORESERVER)){
        DL_ID_NO = "";
      }
      oUpdStmt3.setString(9,DL_ID_NO);
      if(oUpdStmt3.executeUpdate() == 0){
        if(oInsStmt == null){
          oInsStmt = oDb.prepareStmt("insert Download_log (DL_FILE,DL_FILE_D,DL_S_NO,DL_ID_NO,FLS_NO,DL_IMP_D,DL_IMP_T1,DL_IMP_T2,DL_IMP_INFO) \n" +
              "values(?,?,?,?,?,?,?,?,?)");
        }
        oInsStmt.setString(1,DL_FILE);
        oInsStmt.setString(2,DL_IMP_D);
        oInsStmt.setString(3,DL_S_NO);
        oInsStmt.setString(4,DL_ID_NO);
        oInsStmt.setString(5,FLS_NO);
        oInsStmt.setString(6,DL_IMP_D);
        oInsStmt.setString(7,DL_IMP_T1);
        oInsStmt.setString(8,DL_IMP_T2);
        oInsStmt.setString(9,DL_IMP_INFO);
        oInsStmt.executeUpdate();
      }
    } catch(SQLException e){
      e.printStackTrace();
    }
  }

  public void close(){
    if(oQryStmt != null){
      oDb.closePrepareStmt(oQryStmt); oQryStmt = null;
    }
    if(oInsStmt != null){
      oDb.closePrepareStmt(oInsStmt); oInsStmt = null;
    }
    if(oUpdStmt != null){
      oDb.closePrepareStmt(oUpdStmt); oUpdStmt = null;
    }
    if(oUpdStmtExp != null){
      oDb.closePrepareStmt(oUpdStmtExp); oUpdStmtExp = null;
    }
    if(oUpdStmt2 != null){
      oDb.closePrepareStmt(oUpdStmt2); oUpdStmt2 = null;
    }
    if(oUpdStmt3 != null){
      oDb.closePrepareStmt(oUpdStmt3); oUpdStmt3 = null;
    }
    if(oDb != null && bNewDb){
      oDb.close();
      oDb = null;
    }
    if(oCashIdSet != null){
      oCashIdSet.close();
      oCashIdSet = null;
    }
  }
}
