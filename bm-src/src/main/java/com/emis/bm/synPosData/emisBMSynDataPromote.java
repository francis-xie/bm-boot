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

public class emisBMSynDataPromote {
  private ServletContext context_;
  protected Logger oLogger_ = null;

  public emisBMSynDataPromote(ServletContext context) {
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

  public boolean synPromote() {
    boolean bReturn = false;
    try {
      String SME_URL = emisProp.getInstance(context_).get("SME_URL");
      String S_NO = emisProp.getInstance(context_).get("S_NO");

      String data = emisBMSynDataUtils.sendGet(SME_URL + "/ws/wechatV3/bm/getPromote?sNo=" + S_NO);
      oLogger_.info(data);

      JSONObject _oJsonObject = emisUtil.parseJSON(data);
      if (_oJsonObject != null && !_oJsonObject.isEmpty() && "0".equals(_oJsonObject.getString("code"))) {
        JSONArray promoteH = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("promoteH"));
        JSONArray promoteD = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("promoteD"));

        synPromoteH(promoteH);
        synPromoteD(promoteD);
        bReturn = true;
      } else {
        oLogger_.warn("synPromote 无返回资料or异常数据");
        bReturn = false;
      }
    }catch (Exception ex) {
      bReturn = false;
      oLogger_.error(ex, ex);
    }

    return bReturn;
  }

  public boolean synSaleTime() {
    boolean bReturn = false;
    try {
      String SME_URL = emisProp.getInstance(context_).get("SME_URL");
      String S_NO = emisProp.getInstance(context_).get("S_NO");

      String data = emisBMSynDataUtils.sendGet(SME_URL + "/ws/wechatV3/bm/getSaleTime?sNo=" + S_NO);
      oLogger_.info(data);

      JSONObject _oJsonObject = emisUtil.parseJSON(data);
      if (_oJsonObject != null && !_oJsonObject.isEmpty() && "0".equals(_oJsonObject.getString("code"))) {
        JSONArray saleTimeH = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("saleTimeH"));
        JSONArray saleTimeD = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("saleTimeD"));

        synSaleTimeH(saleTimeH);
        synSaleTimeD(saleTimeD);
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

  private boolean synPromoteH(JSONArray jsonArry) {
    oLogger_.info("-- synPromoteH --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table Promote_h ";
      String insData = "insert into Promote_h(PM_NO,PM_ENABLE,FLS_NO,PM_CUST_LEVEL,PM_PRIORITY,PM_THEME,PM_DATE_KIND" +
          ",PM_INTERVAL,PM_COMBINE,PM_CALC,PM_ACCU,PM_AREA,PM_DATE_S,PM_DATE_E,PM_HOUR_S,PM_HOUR_E,PM_DAY_WEEK" +
          ",PM_DAY_MONTH,PM_S_NO,PM_S_KIND,PM_SG_NO,PM_FULL_AMT,PM_FULL_AMT2,PM_FULL_AMT3,PM_TTL_QTY,PM_TTL_QTY2" +
          ",PM_TTL_QTY3,PM_PRICE,PM_PRICE2,PM_PRICE3,CRE_USER,CRE_DATE,UPD_USER,UPD_DATE,REMARK,PM_SL_TYPE," +
          "PM_GROUP_S_NO,PM_BIRTH_KIND,PM_BIRTH_B,PM_BIRTH_A,PM_CARD_NO,IS_SAP,DIS_RATE) " +
          " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmEnable"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "flsNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmCustLevel"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPriOrity"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmTheme"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmDateKind"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmInterval"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmCombine"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmCalc"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmAccu"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmArea"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmDateS"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmDateE"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmHourS"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmHourE"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmDayWeek"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmDayMonth"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmSNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmSKind"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmSgNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmFullAmt"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmFullAmt2"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmFullAmt3"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmTtlQty"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmTtlQty2"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmTtlQty3"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice2"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice3"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creUser"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updUser"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "remark"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmSlType"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmGroupSNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmBirthKind"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmBirthB"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmBirthA"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmCardNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "isSap"));
          oDataSrc_.setString(iParam, parseJsonData(item, "disRate"));
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

  private boolean synPromoteD(JSONArray jsonArry) {
    oLogger_.info("-- synPromoteD --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table Promote_d ";
      String insData = "insert into Promote_d(PM_NO,RECNO,IS_GIFT,PM_D_KIND,P_NO,P_NO_S,D_NO,PM_QTY,PM_RG" +
          ",PM_PRICE,PM_PRICE2,PM_PRICE3,PM_PRICE4,PM_PRICE5,PM_PRICE6,GIFT_PRICE) " +
          " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "recno"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "isGift"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmDKind"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pNoS"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "dNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmQty"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmRg"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice2"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice3"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice4"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice5"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pmPrice6"));
          oDataSrc_.setString(iParam, parseJsonData(item, "giftPrice"));
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

  private boolean synSaleTimeH(JSONArray jsonArry) {
    oLogger_.info("-- synSaleTimeH --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table sale_time_h ";
      String insData = "insert into sale_time_h(ST_NO,ST_ENABLE,FLS_NO,ST_CUST_LEVEL,ST_THEME,ST_DATE_KIND,ST_INTERVAL" +
          ",ST_AREA,ST_DATE_S,ST_DATE_E,ST_HOUR_S,ST_HOUR_E,ST_DAY_WEEK,ST_DAY_MONTH,ST_S_NO,ST_S_KIND,ST_SG_NO" +
          ",CRE_USER,CRE_DATE,UPD_USER,UPD_DATE,REMARK,SL_TYPE) " +
          " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "stNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stEnable"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "flsNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stCustLevel"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stTheme"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stDateKind"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stInterval"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stArea"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stDateS"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stDateE"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stHourS"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stHourE"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stDayWeek"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stDayMonth"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stSNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stSKind"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stSgNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creUser"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updUser"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "remark"));
          oDataSrc_.setString(iParam, parseJsonData(item, "slType"));
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

  private boolean synSaleTimeD(JSONArray jsonArry) {
    oLogger_.info("-- synSaleTimeD --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table sale_time_d ";
      String insData = "insert into sale_time_d(ST_NO,RECNO,ST_D_KIND,P_NO,P_NO_S,D_NO) " +
          " values(?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "stNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "recno"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "stDKind"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "pNoS"));
          oDataSrc_.setString(iParam, parseJsonData(item, "dNo"));
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