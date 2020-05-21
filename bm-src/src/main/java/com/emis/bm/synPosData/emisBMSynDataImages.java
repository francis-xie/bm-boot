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

public class emisBMSynDataImages {
  private ServletContext context_;
  protected Logger oLogger_ = null;

  public emisBMSynDataImages(ServletContext context) {
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

  public boolean synSettingImg() {
    boolean bReturn = false;
    try {
      String SME_URL = emisProp.getInstance(context_).get("SME_URL");
      String S_NO = emisProp.getInstance(context_).get("S_NO");

      String data = emisBMSynDataUtils.sendGet(SME_URL + "/ws/wechatV3/bm/getSettingImg?sNo=" + S_NO);
      oLogger_.info(data);

      JSONObject _oJsonObject = emisUtil.parseJSON(data);
      if (_oJsonObject != null && !_oJsonObject.isEmpty() && "0".equals(_oJsonObject.getString("code"))) {
        JSONArray bmSettingImgH = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("bmSettingImgH"));
        JSONArray bmSettingImgD = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("bmSettingImgD"));

        synBmSettingImgH(bmSettingImgH);
        synBmSettingImgD(bmSettingImgD);
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

  private boolean synBmSettingImgH(JSONArray jsonArry) {
    oLogger_.info("-- synBmSettingImgH --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table bm_setting_img_h ";
      String insData = "insert into bm_setting_img_h(BSI_NO,FLS_NO,SG_NO,S_NO,CRE_DATE,CRE_TIME,CRE_USER,UPD_DATE,UPD_TIME,UPD_USER) " +
          " values(?,?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "bsiNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "flsNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sgNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creTime"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creUser"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updTime"));
          oDataSrc_.setString(iParam, parseJsonData(item, "updUser"));
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

  private boolean synBmSettingImgD(JSONArray jsonArry) {
    oLogger_.info("-- synBmSettingImgD --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table bm_setting_img_d ";
      String insData = "insert into bm_setting_img_d(BSI_NO,RECNO,BSI_TYPE,FILE_NAME,F_FILE,SEQNO,B_DATE,E_DATE) " +
          " values(?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "bsiNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "recno"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "bisType"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "fileName"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "fFile"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "seqno"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "bDate"));
          oDataSrc_.setString(iParam, parseJsonData(item, "eDate"));
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