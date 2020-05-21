package com.emis.bm.synPosData;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.util.emisLogger;
import com.emis.util.emisUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.sql.SQLException;

public class emisBMSynDataStore {
  private ServletContext context_;
  protected Logger oLogger_ = null;

  public emisBMSynDataStore(ServletContext context) {
    this.context_ = context;
    try {
      oLogger_ = emisLogger.getlog4j(context_, this.getClass().getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setServletContext(ServletContext context) {
    this.context_ = context;
    try {
      oLogger_ = emisLogger.getlog4j(context_, this.getClass().getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean synStore() {
    boolean bReturn = false;
    try {
      String SME_URL = emisProp.getInstance(context_).get("SME_URL");
      String S_NO = emisProp.getInstance(context_).get("S_NO");

      String data = emisBMSynDataUtils.sendGet(SME_URL + "/ws/wechatV3/bm/getStore?sNo=" + S_NO);
      oLogger_.info(data);

      JSONObject _oJsonObject = emisUtil.parseJSON(data);
      if (_oJsonObject != null && !_oJsonObject.isEmpty() && "0".equals(_oJsonObject.getString("code"))) {
        JSONArray store = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("store"));

        synStore(store, S_NO);
        bReturn = true;
      } else {
        oLogger_.warn("无返回资料or异常数据");
        bReturn = false;
      }
    }catch (Exception ex) {
      bReturn = false;
      oLogger_.error(ex, ex);
    }

    return bReturn;
  }

  private boolean synStore(JSONArray jsonArry, String S_NO) {
    oLogger_.info("-- synStore --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "delete from Store where S_NO = ? ";
      String insData = "insert into Store(S_NO,S_NAME,S_NAME_S,R_NO,S_KIND,S_LEVEL,S_ADDR,S_TEL,S_FAX,S_EMAIL" +
          ",CRE_DATE,CRE_USER,UPD_DATE,UPD_USER,REMARK,S_STATUS,MU_NO,S_TYPE,S_REGION_B,S_PROVINCE,S_CITY,S_REGION_S" +
          ",S_CLOSE_D,SEND_LINE,WC_STORE,MAP_LNG,MAP_LAT,WM_MU_NO,WECHAT_SEND_RANGE) " +
          " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.prepareStmt(delData);
        oDataSrc_.clearParameters();
        oDataSrc_.setString(1, S_NO);
        oDataSrc_.prepareUpdate();

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "sNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sName"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sNameS"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "rNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sKind"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sLevel"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sAddr"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sTel"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sFax"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sEmail"));

          oDataSrc_.setString(iParam++, parseJsonData(item, "creDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creUser"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updUser"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "remark"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sStatus"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "muNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sType"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sRegionB"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sProvince"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sCityS"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sRegionS"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sCloseD"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sendLine"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wcStore"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "mapLng"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "mapLat"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmMuNo"));
          oDataSrc_.setString(iParam, parseJsonData(item, "wechatSendRange"));
          oDataSrc_.prepareUpdate();
       }
        oDataSrc_.commit();
      }
      return true;
    } catch (Exception ex) {
      try {
        if (oDataSrc_ != null) oDataSrc_.rollback();
      } catch (SQLException e) {
        oLogger_.error(e);
      }
      oLogger_.error(ex);
      return false;
    } finally {
      if (oDataSrc_ != null) {
        oDataSrc_.close();
        oDataSrc_ = null;
      }
    }
  }

  private String parseJsonData(JSONObject item, String name) {
    try {
      return item.getString(name);
    } catch (Exception ex) {
      return "";
    }
  }

}