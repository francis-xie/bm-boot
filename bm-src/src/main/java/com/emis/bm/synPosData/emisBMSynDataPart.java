package com.emis.bm.synPosData;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.util.emisLogger;
import com.emis.util.emisUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class emisBMSynDataPart {
  private ServletContext context_;
  protected Logger oLogger_ = null;

  public emisBMSynDataPart(ServletContext context) {
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

  public boolean synAllPart() {
    boolean bReturn = false;
    try {
      String SME_URL = emisProp.getInstance(context_).get("SME_URL");
      String S_NO = emisProp.getInstance(context_).get("S_NO");

      String data = emisBMSynDataUtils.sendGet(SME_URL + "/ws/wechatV3/bm/getPartAllData?sNo=" + S_NO);
      oLogger_.info(data);

      JSONObject _oJsonObject = emisUtil.parseJSON(data);
      if (_oJsonObject != null && !_oJsonObject.isEmpty() && "0".equals(_oJsonObject.getString("code"))) {
        JSONArray subdep = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("subdep"));
        JSONArray depart = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("depart"));
        JSONArray departTab = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("departTab"));
        JSONArray ddepart = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("ddepart"));
        JSONArray part = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("part"));
        JSONArray smenuH = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("smenuH"));
        JSONArray smenuD = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("smenuD"));
        JSONArray seasoningH = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("seasoningH"));
        JSONArray seasoningD = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("seasoningD"));

        synSubdep(subdep);
        synDepart(depart);
        synDepartTab(departTab);
        synDdepart(ddepart);
        synPart(part);
        synSmenuH(smenuH);
        synSmenuD(smenuD);
        synSeasoningH(seasoningH);
        synSeasoningD(seasoningD);
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

  public boolean synPartSaleOut() {
    boolean bReturn = false;
    try {
      String SME_URL = emisProp.getInstance(context_).get("SME_URL");
      String S_NO = emisProp.getInstance(context_).get("S_NO");

      String data = emisBMSynDataUtils.sendGet(SME_URL + "/ws/wechatV3/bm/getPartSaleOut?sNo=" + S_NO);
      oLogger_.info(data);

      JSONObject _oJsonObject = emisUtil.parseJSON(data);
      if (_oJsonObject != null && !_oJsonObject.isEmpty() && "0".equals(_oJsonObject.getString("code"))) {
        JSONArray partSaleOut = emisUtil.parseJSONArray(_oJsonObject.getJSONObject("result").getString("partSaleOut"));

        emisDb oDataSrc_ = null;
        PreparedStatement updSaleOutStmt = null;
        PreparedStatement updSaleOut4SMStmt = null;
        try {
          oDataSrc_ = emisDb.getInstance(context_);
          oDataSrc_.setAutoCommit(false);

          if (partSaleOut != null) {
            String cleanFlag = "update Part set WM_SALE_OUT_FLAG = '' ";
            String updSaleOut = "update Part set WM_SALE_OUT = 'Y', WM_SALE_OUT_FLAG = 'Y' where P_NO = ? ";
            String updNonSaleOut = "update Part set WM_SALE_OUT = '' where WM_SALE_OUT_FLAG = '' ";
            String cleanFlag4SM = "update Smenu_h set WM_SALE_OUT_FLAG = '' ";
            String updSaleOut4SM = "update Smenu_h set WM_SALE_OUT = 'Y', WM_SALE_OUT_FLAG = 'Y' where SM_NO = ? ";
            String updNonSaleOut4SM = "update Smenu_h set WM_SALE_OUT = '' where WM_SALE_OUT_FLAG = '' ";

            oDataSrc_.executeUpdate(cleanFlag);
            oDataSrc_.executeUpdate(cleanFlag4SM);
            oDataSrc_.commit();

            updSaleOutStmt = oDataSrc_.prepareStmt(updSaleOut);
            updSaleOut4SMStmt = oDataSrc_.prepareStmt(updSaleOut4SM);
            for (Object aJsonArry : partSaleOut) {
              JSONObject item = (JSONObject) aJsonArry;
              oDataSrc_.clearParameters();
              if ("1".equals(parseJsonData(item, "pType"))) {
                updSaleOut4SMStmt.clearParameters();
                updSaleOut4SMStmt.setString(1, parseJsonData(item, "pNo"));
                updSaleOut4SMStmt.executeUpdate();
              } else {
                updSaleOutStmt.clearParameters();
                updSaleOutStmt.setString(1, parseJsonData(item, "pNo"));
                updSaleOutStmt.executeUpdate();
              }
            }
            oDataSrc_.commit();

            oDataSrc_.executeUpdate(updNonSaleOut);
            oDataSrc_.executeUpdate(updNonSaleOut4SM);
            oDataSrc_.commit();
          }
          bReturn = true;
        } catch (Exception ex) {
          try {
            if (oDataSrc_ != null) oDataSrc_.rollback();
          } catch (SQLException e) {
            oLogger_.error(e);
          }
          oLogger_.error(ex);
          bReturn = false;
        } finally {
          if (updSaleOut4SMStmt != null) {
            oDataSrc_.closePrepareStmt(updSaleOut4SMStmt);
          }
          if (updSaleOutStmt != null) {
            oDataSrc_.closePrepareStmt(updSaleOutStmt);
          }
          if (oDataSrc_ != null) {
            oDataSrc_.close();
            oDataSrc_ = null;
          }
        }
        bReturn = true;
      } else {
        oLogger_.warn("synPartSaleOut 无返回资料or异常数据");
        bReturn = false;
      }
    }catch (Exception ex) {
      bReturn = false;
      oLogger_.error(ex, ex);
    }

    return bReturn;
  }

  private boolean synSubdep(JSONArray jsonArry) {
    oLogger_.info("-- synSubdep --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table subdep ";
      String insData = "insert into subdep(SUBDEP,SUB_NAME,CRE_DATE,UPD_DATE) values(?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "sub"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "subName"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creDate"));
          oDataSrc_.setString(iParam, parseJsonData(item, "updDate"));
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

  private boolean synDepart(JSONArray jsonArry) {
    oLogger_.info("-- synDepart --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table depart ";
      String insData = "insert into depart(D_NO, SUBDEP, D_CNAME, D_ENAME, D_TYPE, CRE_DATE, UPD_DATE, D_DOWN, SEA_NO" +
          ", UN_NO, SALE_OPT, USED, DP_TYPE, D_NO_OD, WM_MIN_ORDER_NUM, WM_PACKAGE_BOX_NUM, WM_PACKAGE_BOX_PRICE" +
          ", WM_USED, WM_D_NO, D_OVERTIME, SEQ_NO) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "dNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "sub"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "dName"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "dEName"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "dType"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "creDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "updDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "dDown"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "seaNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "unNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "saleOpt"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "used"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "dpType"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "dNoOd"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmMinOrderNum"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmPackageBoxNum"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmPackageBoxPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmUsed"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmDNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "dOverTime"));
          oDataSrc_.setString(iParam, parseJsonData(item, "seqNo"));
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


  private boolean synDepartTab(JSONArray jsonArry) {
    oLogger_.info("-- synDepartTab --");
    emisDb oDataSrc_ = null;
    PreparedStatement insDataStmt = null;
    PreparedStatement insData2Stmt = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "delete from tab_d where T_NO = 'WM_D_NO' ";
      String delData2 = "truncate table bm_depart_img ";
      String insData = "insert into tab_d(T_NO,TD_NO,TD_NAME,TD_SEQ) values(?,?,?,?) ";
      String insData2 = "insert into bm_depart_img(D_NO,F_FILE,CRE_DATE) values(?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);
        oDataSrc_.executeUpdate(delData2);

        insDataStmt = oDataSrc_.prepareStmt(insData);
        insData2Stmt = oDataSrc_.prepareStmt(insData2);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          insDataStmt.clearParameters();
          int iParam = 1;
          insDataStmt.setString(iParam++, parseJsonData(item,"tNo"));
          insDataStmt.setString(iParam++, parseJsonData(item,"tdNo"));
          insDataStmt.setString(iParam++, parseJsonData(item,"tdName"));
          insDataStmt.setString(iParam, parseJsonData(item,"tdSeq"));
          insDataStmt.executeUpdate();

          insData2Stmt.clearParameters();
          iParam = 1;
          insData2Stmt.setString(iParam++, parseJsonData(item,"tdNo"));
          insData2Stmt.setString(iParam++, parseJsonData(item,"fFile"));
          insData2Stmt.setString(iParam, parseJsonData(item,"creDate"));
          insData2Stmt.executeUpdate();
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

  private boolean synDdepart(JSONArray jsonArry) {
    oLogger_.info("-- synDdepart --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table ddepart ";
      String insData = "insert into ddepart(DD_NO, D_NO, DD_CNAME, DD_ENAME, CRE_DATE, UPD_DATE, SEA_NO, D_DOWN, PRINT_LABLE, DISP_FLAG, ERP_NO, SEQ_NO) values(?,?,?,?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item,"ddNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"dNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"ddName"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"ddEName"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"creDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"updDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"seaNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"dDown"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"printLable"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"dispFlag"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"erpNo"));
          oDataSrc_.setString(iParam, parseJsonData(item,"seqno"));
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

  private boolean synPart(JSONArray jsonArry) {
    oLogger_.info("-- synPart --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table part ";
      String insData = "insert into part(P_NO, P_EAN, P_NAME, P_NAME_S, P_TAX, P_PU, D_NO, P_PHOTO, P_PRICE, P_PRICE2" +
          ", P_PRICE3, P_PRICE4, P_PRICE5, P_PRICE6, UN_NO, P_IN_UN, P_RATE, P_DEF1, P_DEF2, P_DEF4, P_DEF3, P_STATUS" +
          ", P_PS_QTY, CRE_DATE, UPD_DATE, P_DEFA, P_DEFB, P_DEFC, P_DEFD, P_PRICE_ORI, P_TAX_RATE, DD_NO, P_PY, LBL_NAME" +
          ", IS_POR, IS_WEIGH, P_DC, STORAGE_LIFE, P_NO_OD, REM_CODE, WM_SETTING_TYPE, WM_MIN_ORDER_NUM" +
          ", WM_PACKAGE_BOX_NUM, WM_PACKAGE_BOX_PRICE, WM_USED, WM_DESC, WM_ATTR, P_NEW_DATE, DISABLE_SEA_ITEM_NO, WM_SALE_OUT, WM_SALE_OUT_FLAG) " +
          " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'') ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item,"pNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pEan"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pName"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pNameS"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pTax"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPu"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"dNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPhone"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPrice2"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPrice3"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPrice4"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPrice5"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPrice6"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"unNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pInNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pRate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDef1"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDef2"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDef3"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDef4"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pStatus"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPsQty"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"creDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"updDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDefA"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDefB"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDefC"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDefD"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPriceOri"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pTaxRate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"ddNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPy"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"lblName"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"isPor"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"isWeigh"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pDc"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"storageLife"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pNoOd"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"remCode"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"wmSettingType"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"wmMinOrderNum"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"wmPackageBoxNum"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"wmPackageBoxPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"wmUsed"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"wmDesc"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"wmAttr"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pNewDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"disableSeaTimeNo"));
          oDataSrc_.setString(iParam, parseJsonData(item,"wmSaleOut"));
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

  private boolean synSmenuH(JSONArray jsonArry) {
    oLogger_.info("-- synSmenuH --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table smenu_h ";
      String insData = "insert into smenu_h(SM_NO, SM_NAME, SM_UN_NO, SM_TAX, SM_DP_NO, SM_PRICE, SM_PRICE2, SM_PRICE3" +
          ", SM_PRICE4, SM_PRICE5, SM_PRICE6, FLS_NO, CRE_DATE, UPD_DATE, P_PU, SM_TAX_RATE, SM_PY, GROUP_NAME_A" +
          ", MAX_NUM_A, GROUP_NAME_B, MAX_NUM_B, GROUP_NAME_C, MAX_NUM_C, GROUP_NAME_D, MAX_NUM_D, GROUP_NAME_E" +
          ", MAX_NUM_E, P_NO_S_OD, WM_SETTING_TYPE, WM_MIN_ORDER_NUM, WM_PACKAGE_BOX_NUM, WM_PACKAGE_BOX_PRICE" +
          ", WM_USED, WM_DESC, SM_PHOTO, WM_ATTR, SEQ_NO, WM_SALE_OUT, WM_SALE_OUT_FLAG " +
          ", MIN_NUM_A, MIN_NUM_B, MIN_NUM_C, MIN_NUM_D, MIN_NUM_E) " +
          " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'',?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item,"smNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smName"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smUnNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smTax"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smDpNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smPrice2"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smPrice3"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smPrice4"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smPrice5"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smPrice6"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"flsNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"creDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"updDate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pPu"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smTaxRate"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smPy"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"groupNameA"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"maxNumA"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"groupNameB"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"maxNumB"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"groupNameC"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"maxNumC"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"groupNameD"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"maxNumD"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"groupNameE"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"maxNumE"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pNoSOd"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmSettingType"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmMinOrderNum"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmPackageBoxNum"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmPackageBoxPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmUsed"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmDesc"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "smPhoto"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmAttr"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "seqno"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "wmSaleOut"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "minNumA", "0"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "minNumB", "0"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "minNumC", "0"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "minNumD", "0"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "minNumE", "0"));
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

  private boolean synSmenuD(JSONArray jsonArry) {
    oLogger_.info("-- synSmenuD --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table smenu_d ";
      String insData = "insert into smenu_d(SM_NO,RECNO,P_NO,SM_QTY,SM_PRICE,GROUP_TYPE,ADD_PRICE,P_NO_S_OD,WM_GET_PLAN_ATTR) values(?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item,"smNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"recno"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smQty"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"smPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"groupType"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"addPrice"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"pNoSOd"));
          oDataSrc_.setString(iParam, parseJsonData(item,"wmGetPlanAttr"));
          oDataSrc_.prepareUpdate();        }
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

  private boolean synSeasoningH(JSONArray jsonArry) {
    oLogger_.info("-- synSeasoningH --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table seasoning_h ";
      String insData = "insert into seasoning_h(SEA_NO,SEA_NAME,SEA_ENAME,SEA_TYPE,IS_SINGLE,USED,SEQ_NO,WM_CANUSE) values(?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item, "seaNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "seaName"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "seaEName"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "seaType"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "isSingle"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "used"));
          oDataSrc_.setString(iParam++, parseJsonData(item, "seqno"));
          oDataSrc_.setString(iParam, parseJsonData(item, "wmCanUse"));
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

  private boolean synSeasoningD(JSONArray jsonArry) {
    oLogger_.info("-- synSeasoningD --");
    emisDb oDataSrc_ = null;
    try {
      oDataSrc_ = emisDb.getInstance(context_);
      oDataSrc_.setAutoCommit(false);

      String delData = "truncate table seasoning_d ";
      String insData = "insert into seasoning_d(SEA_ITEM_NO,SEA_NO,SEA_ITEM_NAME,SEA_ITEM_ENAME,DEF_CHOOSE,PRICE,PRICE2,PRICE3,USED,SEQ_NO,SEA_ITEM_NO_OD,SEA_REM_CODE) " +
          " values(?,?,?,?,?,?,?,?,?,?,?,?) ";
      if (jsonArry != null) {
        oDataSrc_.executeUpdate(delData);

        oDataSrc_.prepareStmt(insData);
        for (Object aJsonArry : jsonArry) {
          JSONObject item = (JSONObject) aJsonArry;
          oDataSrc_.clearParameters();
          int iParam = 1;
          oDataSrc_.setString(iParam++, parseJsonData(item,"seaItemNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"seaNo"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"seaItemName"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"seaItemEName"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"defChoose"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"price"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"price2"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"price3"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"used"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"seqno"));
          oDataSrc_.setString(iParam++, parseJsonData(item,"seaItemNoOd"));
          oDataSrc_.setString(iParam, parseJsonData(item,"wmCanUse"));
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

  private String parseJsonData(JSONObject item, String name, String def) {
    try {
      return item.getString(name);
    } catch (Exception ex) {
      return def;
    }
  }

}