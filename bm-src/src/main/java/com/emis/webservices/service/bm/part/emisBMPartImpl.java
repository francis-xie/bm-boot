package com.emis.webservices.service.bm.part;

import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;
import com.emis.report.emisString;
import com.emis.util.emisDate;
import com.emis.util.emisUtil;
import com.emis.webservices.service.bm.utils.emisBMUtils;
import com.emis.webservices.service.emisAbstractService;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


/**
 * 大屏点餐-商品相关接口
 */
public class emisBMPartImpl extends emisAbstractService {

  private final static String ACT_getDepartList = "getDepartList";  // 1.20 获取商品分类列表
  private final static String ACT_getPartList = "getPartList";  // 1.21 获取商品列表
//  private final static String ACT_getPartInfo = "getPartInfo";  // 1.22 获取商品信息
  private final static String ACT_getPromoteInfo = "getPromoteInfo";  // 1.23 获取促销设置
  private String defaultAct;

  /**
   * 设置默认act
   *
   * @param defaultAct
   */
  public void setDefaultAct(String defaultAct) {
    this.defaultAct = defaultAct;
  }

  @Override
  protected String postAction() {
    MultivaluedMap<String, String> req = parseRequest();
    // 获取请求Act
    String sAct = req.getFirst("act");
    // 当没有传act时取默认的defaultAct
    if (sAct == null || "".equals(sAct.trim())) {
      sAct = this.defaultAct;
    }
    // 选择响应业务
    if (ACT_getDepartList.equalsIgnoreCase(sAct)) {
      return doGetDepartList(req);
    } else if (ACT_getPartList.equalsIgnoreCase(sAct)) {
      return doGetPartList(req);
    } else if (ACT_getPromoteInfo.equalsIgnoreCase(sAct)) {
      return doGetPromoteInfo(req);
    }
    return null;
  }


  /**
   * 分类列表
   *
   * @param req request参数
   * @return 分类列表
   * @throws Exception
   */
  private String doGetDepartList(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";
    StringBuffer sResult = new StringBuffer();

    String C_NO = emisUtil.parseString(req.getFirst("cNo"));  // 会员码(会员编号or卡号or微信码)

    StringBuffer departList = new StringBuffer();
    try {
      String nowDate = emisUtil.todayDateAD();  //系统日期
      String nowTime = emisUtil.todayTimeS();  //系统时间
      emisProp prop = emisProp.getInstance(context_);
      String S_NO = prop.get("S_NO");
      String LOCAL_URL = prop.get("LOCAL_URL");

      StringBuffer selDepart = new StringBuffer();
      selDepart.append(" select td.TD_SEQ as RECNO, dp.WM_D_NO as D_NO, td.TD_NAME as D_CNAME\n ");
      selDepart.append(" , ifnull(di.F_FILE,'') as D_FILE\n ");
      selDepart.append(" from Depart dp\n ");
      selDepart.append(" inner join Tab_d td on td.T_NO = 'WM_D_NO' and td.TD_NO = dp.WM_D_NO\n ");
      selDepart.append(" left join bm_depart_img di on di.D_NO = td.TD_NO\n ");
      selDepart.append(" where dp.USED = 'Y' and ifnull(dp.WM_D_NO,'') != '' and dp.D_DOWN = 'Y'\n ");
      selDepart.append(" group by td.TD_SEQ, dp.WM_D_NO, td.TD_NAME \n");
      selDepart.append(" order by 1, 2 ");
      oDataSrc_.executeQuery(selDepart.toString());
      int i = 0;
      while (oDataSrc_.next()) {
        if (i++ > 0) {
          departList.append(",\n");
        }
        departList.append("{\"dNo\":\"").append(oDataSrc_.getString("D_NO")).append("\",\n");
        departList.append(" \"dName\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("D_CNAME"))).append("\",\n");
        departList.append(" \"dImg\":\"").append(getDepartPicture(oDataSrc_.getString("D_FILE"), LOCAL_URL, "")).append("\",\n");
        departList.append(" \"dSeq\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("RECNO"))).append("\"}");
      }
      if (i > 0) {
        code = "0";
        msg = "成功";
      } else {
        code = "100";
        msg = "查无资料!";
      }
    } catch (Exception ex) {
      departList.setLength(0);
      code = "900";
      msg = "查询异常,请重试";
      oLogger_.error(ex, ex);
    }
    sResult.append(" \"departList\":").append("[").append(departList.toString()).append("]");

    return "{\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\",\n"
        + " \"result\":{" + sResult.toString() + "}"
        + "\n}";
  }

  /**
   * 商品列表
   *
   * @param req request参数
   * @return 商品列表
   * @throws Exception
   */
  private String doGetPartList(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";
    StringBuffer sResult = new StringBuffer();
    StringBuffer partImgs = new StringBuffer();

    String C_NO = emisUtil.parseString(req.getFirst("cNo"));  // 会员码(会员编号or卡号or微信码)
//    String S_NO = emisUtil.parseString(req.getFirst("sNo"));  // 门店编号

    PreparedStatement selPartStmt = null;
    PreparedStatement selSeasoningStmt = null;
    PreparedStatement selSmenuStmt = null;
    PreparedStatement selPartImgsStmt = null;
    try {
      String nowDate = emisUtil.todayDateAD();  //系统日期
      String nowTime = emisUtil.todayTimeS();  //系统时间
      emisProp prop = emisProp.getInstance(context_);
      String S_NO = prop.get("S_NO");
      String LOCAL_URL = prop.get("LOCAL_URL");
      /*if (!"/".equalsIgnoreCase(emisString.rightB(LOCAL_URL, 1))) {
        LOCAL_URL = LOCAL_URL + "/";
      }*/

      String DefSetImg = "";
      String CHECK_PS_QTY = "N"; // 是否检查库存量

      String WM_PART_MODE = emisProp.getInstance(context_).get("WM_PART_MODE", "3");

      String condField = "WM_WECHAT_USED"; // 判断是否可售商品的检核栏位
      String WM_PART_WECHAT = emisProp.getInstance(context_).get("WM_PART_WECHAT");
      // 用餐方式
      String SL_TYPE = req.getFirst("oType");
      if ("2".equalsIgnoreCase(SL_TYPE)) {
        SL_TYPE = "0";
      }
      if ("N".equals(WM_PART_WECHAT) && ("0".equals(SL_TYPE) || "3".equals(SL_TYPE))) {
        condField = "WM_WECHAT_IN_USED";
      }

      StringBuffer selPart = new StringBuffer();
      selPart.append(" select td.TD_SEQ as RECNO, dp.SEQ_NO, ddp.SEQ_NO as RECNO_D\n ");
      selPart.append(" , dp.WM_D_NO as D_NO, td.TD_NAME as D_CNAME, ifnull(di.F_FILE,'') as D_FILE\n ");
      selPart.append(" , replace(p.P_NO, ' ','') as P_NO, p.P_NAME, ifnull(p.UN_NO,'') as UN_NO, '0' as P_TYPE\n ");
      selPart.append(" , p.P_PRICE, p.P_PRICE as P_PRICE_SL  \n ");// TODO 会员价
      selPart.append(" , case when p.WM_SETTING_TYPE = '1' then ifnull(p.WM_USED,0) else 'Y' end as WM_USED\n ");
      selPart.append(" , case when p.WM_SETTING_TYPE = '1' then ifnull(p.WM_MIN_ORDER_NUM,1) else ifnull(dp.WM_MIN_ORDER_NUM,1) end as WM_MIN_ORDER_NUM\n ");
      selPart.append(" , case when p.WM_SETTING_TYPE = '1' then ifnull(p.WM_PACKAGE_BOX_NUM,0) else ifnull(dp.WM_PACKAGE_BOX_NUM,0) end as WM_PACKAGE_BOX_NUM\n ");
      selPart.append(" , case when p.WM_SETTING_TYPE = '1' then ifnull(p.WM_PACKAGE_BOX_PRICE,0) else ifnull(dp.WM_PACKAGE_BOX_PRICE,0) end as WM_PACKAGE_BOX_PRICE\n ");
      selPart.append(" , ifnull(p.WM_DESC,'') as WM_DESC, ifnull(p.P_PHOTO,'') as P_PHOTO\n ");
      selPart.append(" , ifnull(p.WM_SALE_OUT,'') as WM_SALE_OUT  \n ");// TODO 售完标记
      selPart.append(" , '' as WM_HAS_SKU, ifnull(p.WM_ATTR,'') as WM_ATTR, ifnull(td2.TD_NAME,'') as WM_ATTR_NAME, ddp.SEA_NO, p.P_PS_QTY\n ");//, ifnull(ps.PS_QTY,0) as PS_QTY
      selPart.append(" , p.DD_NO as CHK_DD_NO, p.D_NO as CHK_D_NO\n ");//, s.MU_NO
      selPart.append(" from Depart dp\n ");
      selPart.append(" inner join Tab_d td on td.T_NO = 'WM_D_NO' and td.TD_NO = dp.WM_D_NO\n ");
      selPart.append(" left join bm_depart_img di on di.D_NO = td.TD_NO\n ");
      selPart.append(" inner join DDepart ddp on ddp.D_NO = dp.D_NO\n ");
      selPart.append(" inner join Part p on p.DD_NO = ddp.DD_NO\n ");
      selPart.append(" left join Tab_d td2 on td2.T_NO = 'WM_ATTR' and td2.TD_NO = p.WM_ATTR\n ");
      selPart.append(" where dp.USED = 'Y' and ifnull(dp.WM_D_NO,'') != '' and ifnull(p.WM_SALE_OUT,'') != 'Y' \n ");
      selPart.append(" and dp.D_DOWN = 'Y' and ddp.D_DOWN = 'Y' and p.P_STATUS != '3' \n ");
//and(ifnull(h.WM_START_DATE,'')=''orh.WM_START_DATE+h.WM_START_TIME<='20190816110314')
      selPart.append(" union all\n ");
      selPart.append(" select td.TD_SEQ as RECNO, dp.SEQ_NO, p.SEQ_NO as RECNO_D\n ");
      selPart.append(" , dp.WM_D_NO as D_NO, td.TD_NAME as D_CNAME, ifnull(di.F_FILE,'') as D_FILE\n ");
      selPart.append(" , replace(p.SM_NO, ' ','') as P_NO, p.SM_NAME as P_NAME, ifnull(p.SM_UN_NO,'') as UN_NO, '1' as P_TYPE\n ");
      selPart.append(" , p.SM_PRICE, p.SM_PRICE as P_PRICE_SL  \n ");// TODO 会员价
      selPart.append(" , case when p.WM_SETTING_TYPE = '1' then ifnull(p.WM_USED,0) else 'Y' end as WM_USED\n ");
      selPart.append(" , case when p.WM_SETTING_TYPE = '1' then ifnull(p.WM_MIN_ORDER_NUM,1) else ifnull(dp.WM_MIN_ORDER_NUM,1) end as WM_MIN_ORDER_NUM\n ");
      selPart.append(" , case when p.WM_SETTING_TYPE = '1' then ifnull(p.WM_PACKAGE_BOX_NUM,0) else ifnull(dp.WM_PACKAGE_BOX_NUM,0) end as WM_PACKAGE_BOX_NUM\n ");
      selPart.append(" , case when p.WM_SETTING_TYPE = '1' then ifnull(p.WM_PACKAGE_BOX_PRICE,0) else ifnull(dp.WM_PACKAGE_BOX_PRICE,0) end as WM_PACKAGE_BOX_PRICE\n ");
      selPart.append(" , ifnull(p.WM_DESC,'') as WM_DESC, ifnull(p.SM_PHOTO,'') as P_PHOTO\n ");
      selPart.append(" , ifnull(p.WM_SALE_OUT,'') as WM_SALE_OUT  \n ");// TODO 售完标记
      selPart.append(" , 'Y' as WM_HAS_SKU, ifnull(p.WM_ATTR,'') as WM_ATTR, ifnull(td2.TD_NAME,'') as WM_ATTR_NAME, '' as SEA_NO, 'N' as P_PS_QTY\n ");//, ifnull(ps.PS_QTY,0) as PS_QTY
      selPart.append(" , p.SM_NO as CHK_DD_NO, p.SM_DP_NO as CHK_D_NO\n ");//, s.MU_NO
      selPart.append(" from Depart dp\n ");
      selPart.append(" inner join Tab_d td on td.T_NO = 'WM_D_NO' and td.TD_NO = dp.WM_D_NO\n ");
      selPart.append(" left join bm_depart_img di on di.D_NO = td.TD_NO\n ");
      selPart.append(" inner join Smenu_h p on p.SM_DP_NO = dp.D_NO\n ");
      selPart.append(" left join Tab_d td2 on td2.T_NO = 'WM_ATTR' and td2.TD_NO = p.WM_ATTR\n ");
      selPart.append(" where dp.USED = 'Y' and ifnull(dp.WM_D_NO,'') != '' and ifnull(p.WM_SALE_OUT,'') != 'Y' \n ");
      selPart.append(" and dp.D_DOWN = 'Y' and p.FLS_NO = 'CO' \n ");
//and(ifnull(h.WM_START_DATE,'')=''orh.WM_START_DATE+h.WM_START_TIME<='20190816110314')
      selPart.append(" order by 1,2,3 ");

      StringBuffer selSeasoning = new StringBuffer();
      selSeasoning.append(" select h.SEA_NO, h.SEA_NAME, h.IS_SINGLE, d.SEA_ITEM_NO, d.SEA_ITEM_NAME, ifnull(d.PRICE,0) as PRICE\n ");
      selSeasoning.append(" from Seasoning_h h\n ");
      selSeasoning.append(" inner join Seasoning_d d on d.SEA_NO = h.SEA_NO\n ");
      selSeasoning.append(" where h.USED = 'Y' and ifnull(h.WM_CANUSE,'') != 'Y' and d.USED = 'Y' and h.SEA_NO = ? ");
      selSeasoningStmt = oDataSrc_.prepareStmt(selSeasoning.toString());

      StringBuffer selSmenu = new StringBuffer();
      selSmenu.append(" select h.SM_NO, h.GROUP_NAME_A, h.MAX_NUM_A, h.GROUP_NAME_B, h.MAX_NUM_B \n ");
      selSmenu.append(" , h.GROUP_NAME_C, h.MAX_NUM_C, h.GROUP_NAME_D, h.MAX_NUM_D, h.GROUP_NAME_E, h.MAX_NUM_E \n ");
      selSmenu.append(" , ifnull(h.MIN_NUM_A,0) as MIN_NUM_A, ifnull(h.MIN_NUM_B,0) as MIN_NUM_B, ifnull(h.MIN_NUM_C,0) as MIN_NUM_C \n ");
      selSmenu.append(" , ifnull(h.MIN_NUM_D,0) as MIN_NUM_D, ifnull(h.MIN_NUM_E,0) as MIN_NUM_E \n ");
      selSmenu.append(" , d.GROUP_TYPE, d.RECNO, d.P_NO, ifnull(p.P_NAME,'') as P_NAME, d.ADD_PRICE, d.SM_QTY \n ");
      selSmenu.append(" , case when ifnull(pdd.SEA_NO,'') = '' then ifnull(dd.SEA_NO,'') else ifnull(pdd.SEA_NO,'') end as SEA_NO \n ");
      selSmenu.append(" , ifnull(p.WM_SALE_OUT,'') as WM_SALE_OUT\n ");//, ifnull(ps.WM_SALE_OUT_WECHAT,'') as WM_SALE_OUT
      selSmenu.append(" from Smenu_h h \n ");
      selSmenu.append(" inner join Smenu_d d on d.SM_NO = h.SM_NO \n ");
      selSmenu.append(" inner join Part p on p.P_NO = d.P_NO \n ");
      selSmenu.append(" left join Ddepart pdd on pdd.DD_NO = p.DD_NO\n ");
//leftjoinPart_Menu_ppponpp.MU_NO=?andpp.P_NO=p.P_NO
//leftjoinPart_Menu_ddpddonpdd.MU_NO=pp.MU_NOandpdd.D_NO=pp.D_NOandpdd.DD_NO=pp.DD_NO
      selSmenu.append(" left join Ddepart dd on dd.DD_NO = p.DD_NO \n ");
//leftjoinPart_spsonps.S_NO=?andps.P_NO=d.P_NO
      selSmenu.append(" where h.SM_NO = ? and (d.GROUP_TYPE = 'A' or ifnull(p.WM_SALE_OUT,'') != 'Y' ) \n ");//and (d.GROUP_TYPE = 'A' or ifnull(ps.WM_SALE_OUT_WECHAT,'') != 'Y')
      selSmenu.append(" order by d.GROUP_TYPE, d.ADD_PRICE, d.RECNO ");
      selSmenuStmt = oDataSrc_.prepareStmt(selSmenu.toString());

      selPartStmt = oDataSrc_.prepareStmt(selPart.toString());
      selPartStmt.clearParameters();
      /*selPartStmt.setString(1, S_NO);
      selPartStmt.setString(2, nowDate + nowTime);
      selPartStmt.setString(3, S_NO);
      selPartStmt.setString(4, nowDate + nowTime);
      if (req.getFirst("dno") != null && !"".equalsIgnoreCase(req.getFirst("dno"))) {
        selPartStmt.setString(5, req.getFirst("dno"));
      }*/
      oDataSrc_.setCurrentPrepareStmt(selPartStmt);

      oDataSrc_.prepareQuery();
      int i = 0;
      String dno = "";
      HashMap<String, String> seasoningList = new HashMap<String, String>();

      String dataType = req.getFirst("dataType");
      if (dataType == null || "".equalsIgnoreCase(dataType)) {
        dataType = "";
      }
      StringBuffer sDepartList = new StringBuffer();
      StringBuffer sPartList = new StringBuffer();
      boolean doGetSaleTimePart = true;
      while (oDataSrc_.next()) {
        String MU_NO = ""; //oDataSrc_.getString("MU_NO");   TODO
        if (doGetSaleTimePart) {
          doGetSaleTimePart = false;
          getSaleTimePart(S_NO);
        }
        if (!checkPart(oDataSrc_.getString("P_NO"), oDataSrc_.getString("CHK_DD_NO"), oDataSrc_.getString("CHK_D_NO"))) {
          continue;
        }

        if ("2".equals(dataType)) {
          if (!dno.equals(oDataSrc_.getString("D_NO"))) {
            if (i++ > 0) {
              sDepartList.append(",\n");
              sPartList.append("],\n");
            } else {
              sDepartList.append("\"departList\":[");
            }
            /*sDepartList.append(" {\"dno\":\"dp_").append(oDataSrc_.getString("D_NO")).append("\",\n");
            sDepartList.append("  \"dname\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("D_CNAME"))).append("\"}");*/
            sDepartList.append("{\"dNo\":\"dp_").append(oDataSrc_.getString("D_NO")).append("\",");
            sDepartList.append(" \"dName\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("D_CNAME"))).append("\",\n");
            sDepartList.append(" \"dImg\":\"").append(getDepartPicture(oDataSrc_.getString("D_FILE"), LOCAL_URL, "")).append("\",\n");
            sDepartList.append(" \"dSeq\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("RECNO"))).append("\"}");

//            sResult.append(" {\"dno\":\"").append(oDataSrc_.getString("D_NO")).append("\",\n");
//            sResult.append("  \"dname\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("D_CNAME"))).append("\",\n");
//            sResult.append("  \"parts\":[\n");

            sPartList.append(" \"dp_").append(oDataSrc_.getString("D_NO")).append("\":[");
          } else if (i > 0) {
            sPartList.append(",\n");
          }
          dno = oDataSrc_.getString("D_NO");

          sPartList.append("   {\"pno\":\"").append(oDataSrc_.getString("P_NO")).append("\",");
          sPartList.append(" \"name\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("P_NAME"))).append("\",\n");
          sPartList.append("    \"desc\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("WM_DESC"))).append("\",\n");
          sPartList.append("    \"priceWM\":").append(oDataSrc_.getDouble("P_PRICE")).append(",");
          sPartList.append(" \"price\":").append(oDataSrc_.getDouble("P_PRICE_SL")).append(",\n");
          sPartList.append("    \"minOrdCnt\":").append(oDataSrc_.getString("WM_MIN_ORDER_NUM")).append(",");
          sPartList.append(" \"unit\":\"").append(oDataSrc_.getString("UN_NO")).append("\",\n");
          sPartList.append("    \"boxPrice\":").append(oDataSrc_.getString("WM_PACKAGE_BOX_PRICE")).append(",");
          sPartList.append(" \"boxQty\":").append(oDataSrc_.getString("WM_PACKAGE_BOX_NUM")).append(",\n");
          sPartList.append("    \"imgUrl\":\"").append(getPartPicture(oDataSrc_.getString("P_PHOTO"), LOCAL_URL, DefSetImg)).append("\",\n");
          sPartList.append("    \"type\":\"").append(oDataSrc_.getString("P_TYPE")).append("\",");
          sPartList.append(" \"attr\":\"").append(oDataSrc_.getString("WM_ATTR")).append("\",");
          sPartList.append(" \"attrName\":\"").append(oDataSrc_.getString("WM_ATTR_NAME")).append("\",\n");
          sPartList.append("    \"seq\":\"").append(oDataSrc_.getString("RECNO_D")).append("\",");
          sPartList.append(" \"pDNo\":\"").append(oDataSrc_.getString("CHK_D_NO")).append("\",");
          sPartList.append(" \"pDDNo\":\"").append(oDataSrc_.getString("CHK_DD_NO")).append("\",\n");

          // 调味
          int iSea = 0;
          if (!"".equals(oDataSrc_.getString("SEA_NO")) && "0".equals(oDataSrc_.getString("P_TYPE"))) {
            String[] sea = oDataSrc_.getString("SEA_NO").split("/");
            for (String aSea : sea) {
              if (seasoningList.get(aSea) == null) {
                seasoningList.put(aSea, getSeasoning(aSea, selSeasoningStmt));
              }
              String seaTmp = seasoningList.get(aSea);

              if (seaTmp != null && !"".equals(seaTmp)) {
                if (iSea == 0) {
                  sPartList.append("    \"seasoning\":[\n");
                } else {
                  sPartList.append(",\n");
                }
                sPartList.append(seaTmp);
                iSea++;
              }
            }
          }
          if (iSea > 0) {
            sPartList.append("],\n");
          }

          bSMFixItemSaleOut = false;
          // 套餐项
          if ("1".equals(oDataSrc_.getString("P_TYPE"))) {
            String smenuTmp = getSmenu(oDataSrc_.getString("P_NO"), selSmenuStmt, MU_NO, seasoningList, selSeasoningStmt, S_NO);
            if (smenuTmp != null && !"".equals(smenuTmp)) {
              sPartList.append(smenuTmp);
            }
          }

          if ("Y".equals(CHECK_PS_QTY)) {
            if ("Y".equalsIgnoreCase(oDataSrc_.getString("P_PS_QTY")) && oDataSrc_.getDouble("PS_QTY") <= 0) {
              sPartList.append("  \"saleOut\":\"Y\"\n");
            } else {
              sPartList.append("  \"saleOut\":\"").append(bSMFixItemSaleOut ? "Y" : oDataSrc_.getString("WM_SALE_OUT")).append("\"\n");
            }
          } else {
            sPartList.append("  \"saleOut\":\"").append(bSMFixItemSaleOut ? "Y" : oDataSrc_.getString("WM_SALE_OUT")).append("\"\n");
          }

          sPartList.append("  }");

        } else {
          if (!dno.equals(oDataSrc_.getString("D_NO"))) {
            if (i++ > 0) {
              sResult.append("  ]},\n");
            }
            sResult.append(" {\"dno\":\"").append(oDataSrc_.getString("D_NO")).append("\",\n");
            sResult.append("  \"dname\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("D_CNAME"))).append("\",\n");
            sResult.append("  \"parts\":[\n");
          } else if (i > 0) {
            sResult.append(",\n");
          }
          dno = oDataSrc_.getString("D_NO");
          sResult.append("   {\"pno\":\"").append(oDataSrc_.getString("P_NO")).append("\",");
          sResult.append(" \"name\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("P_NAME"))).append("\",\n");
          sResult.append("    \"desc\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("WM_DESC"))).append("\",\n");
          sResult.append("    \"priceWM\":").append(oDataSrc_.getDouble("P_PRICE")).append(",");
          sResult.append(" \"price\":").append(oDataSrc_.getDouble("P_PRICE_SL")).append(",\n");
          sResult.append("    \"minOrdCnt\":").append(oDataSrc_.getString("WM_MIN_ORDER_NUM")).append(",");
          sResult.append(" \"unit\":\"").append(oDataSrc_.getString("UN_NO")).append("\",\n");
          sResult.append("    \"boxPrice\":").append(oDataSrc_.getString("WM_PACKAGE_BOX_PRICE")).append(",");
          sResult.append(" \"boxQty\":").append(oDataSrc_.getString("WM_PACKAGE_BOX_NUM")).append(",\n");
          sResult.append("    \"imgUrl\":\"").append(getPartPicture(oDataSrc_.getString("P_PHOTO"), LOCAL_URL, DefSetImg)).append("\",\n");
          sResult.append("    \"type\":\"").append(oDataSrc_.getString("P_TYPE")).append("\",\n");
          sResult.append(" \"attr\":\"").append(oDataSrc_.getString("WM_ATTR")).append("\",");
          sResult.append(" \"attrName\":\"").append(oDataSrc_.getString("WM_ATTR_NAME")).append("\",");
          sResult.append(" \"seq\":\"").append(oDataSrc_.getString("RECNO_D")).append("\",");
          sResult.append(" \"pDNo\":\"").append(oDataSrc_.getString("CHK_D_NO")).append("\",");
          sResult.append(" \"pDDNo\":\"").append(oDataSrc_.getString("CHK_DD_NO")).append("\",\n");

          // 调味
          int iSea = 0;
          if (!"".equals(oDataSrc_.getString("SEA_NO")) && "0".equals(oDataSrc_.getString("P_TYPE"))) {
            String[] sea = oDataSrc_.getString("SEA_NO").split("/");
            for (String aSea : sea) {
              if (seasoningList.get(aSea) == null) {
                seasoningList.put(aSea, getSeasoning(aSea, selSeasoningStmt));
              }
              String seaTmp = seasoningList.get(aSea);

              if (seaTmp != null && !"".equals(seaTmp)) {
                if (iSea == 0) {
                  sResult.append("    \"seasoning\":[\n");
                } else {
                  sResult.append(",\n");
                }
                sResult.append(seaTmp);
                iSea++;
              }
            }
          }
          if (iSea > 0) {
            sResult.append("],\n");
          }

          bSMFixItemSaleOut = false;
          // 套餐项
          if ("1".equals(oDataSrc_.getString("P_TYPE"))) {
            String smenuTmp = getSmenu(oDataSrc_.getString("P_NO"), selSmenuStmt, MU_NO, seasoningList, selSeasoningStmt, S_NO);
            if (smenuTmp != null && !"".equals(smenuTmp)) {
              sResult.append(smenuTmp);
            }
          }

          if ("Y".equals(CHECK_PS_QTY)) {
            if ("Y".equalsIgnoreCase(oDataSrc_.getString("P_PS_QTY")) && oDataSrc_.getDouble("PS_QTY") <= 0) {
              sResult.append("  \"saleOut\":\"Y\"\n");
            } else {
              sResult.append("  \"saleOut\":\"").append(bSMFixItemSaleOut ? "Y" : oDataSrc_.getString("WM_SALE_OUT")).append("\"\n");
            }
          } else {
            sResult.append("  \"saleOut\":\"").append(bSMFixItemSaleOut ? "Y" : oDataSrc_.getString("WM_SALE_OUT")).append("\"\n");
          }

          sResult.append("  }");
        }
      }
      if (i > 0) {
        if ("2".equals(dataType)) {
          sDepartList.append("]");
          sPartList.append("  ]\n");
          sResult.append("{\n").append(sDepartList.toString()).append(",\n").append(sPartList.toString()).append("}");
        } else {
          sResult.append("  ]}\n");
        }

//        partImgs
        try {
          StringBuffer selPartImg = new StringBuffer();
          selPartImg.append(" select p.P_NO, p.P_NAME, ifnull(p.P_PHOTO,'') as P_PHOTO from Part p\n ");
          selPartImg.append(" union all\n ");
          selPartImg.append(" select replace(p.SM_NO, ' ','') as P_NO, p.SM_NAME as P_NAME, ifnull(p.SM_PHOTO,'') as P_PHOTO from Smenu_h p ");
          selPartImgsStmt = oDataSrc_.prepareStmt(selPartImg.toString());
          oDataSrc_.setCurrentPrepareStmt(selPartImgsStmt);
          oDataSrc_.clearParameters();
          oDataSrc_.prepareQuery();
          int iImgCnt = 0;
          while (oDataSrc_.next()) {
            if (iImgCnt++ > 0) {
              partImgs.append(",\n");
            }
            partImgs.append("\"i_").append(oDataSrc_.getString("P_NO")).append("\":");
            partImgs.append("{\"name\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("P_NAME"))).append("\",\n");
            partImgs.append(" \"imgUrl\":\"").append(getPartPicture(oDataSrc_.getString("P_PHOTO"), LOCAL_URL, DefSetImg)).append("\"}");
          }
        } catch (Exception ex) {
          oLogger_.error(ex, ex);
        }

        code = "0";
        msg = "成功";
      } else {
        code = "100";
        msg = "查无资料!";
      }
    } catch (Exception ex) {
      code = "901";
      msg = "查询异常,请重新查询!";
      oLogger_.error(ex, ex);
    } finally {
      oDataSrc_.closePrepareStmt(selSmenuStmt);
      oDataSrc_.closePrepareStmt(selSeasoningStmt);
      oDataSrc_.closePrepareStmt(selPartStmt);
      oDataSrc_.closePrepareStmt(selPartImgsStmt);
    }

    return "{\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\",\n"
        + " \"partList\":[" + sResult.toString() + "],\n"
        + " \"partImgs\":{" + partImgs.toString() + "}"
        + "}";
  }

  /**
   * 促销信息
   *
   * @param req
   * @return
   * @throws Exception
   */
  private String doGetPromoteInfo(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";
    StringBuffer sResult = new StringBuffer();

//    String S_NO = emisUtil.parseString(req.getFirst("sno"));
//    String WC_ID = emisUtil.parseString(req.getFirst("wx_id"));
    String C_NO = emisUtil.parseString(req.getFirst("cNo"));
    String CARD_NO = emisUtil.parseString(req.getFirst("cardNo"));

    PreparedStatement selPromoteHStmt = null;
    ResultSet selPromoteHRs = null;
    try {
      emisProp prop = emisProp.getInstance(context_);
      String S_NO = prop.get("S_NO");

      // 日期信息
      String todayDate = emisUtil.todayDateAD();  // 当天
//      String toodayTime = emisUtil.todayTimeS();  // 当时
      String todayDay = emisString.rightB(todayDate, 2);  // 日
      emisDate today = new emisDate(todayDate);
      int todayWeek = today.getWeek();
      if (todayWeek == 0) {
        todayWeek = 7;
      }

      /*String C_NO = "";
      String CARD_NO = "";
      // 1. 检查是否会员
      oDataSrc_.prepareStmt("select c.C_NO, c.CARD_NO from Cust_v c where WC_ID = ? and WC_ID != '' ");
      oDataSrc_.clearParameters();
      oDataSrc_.setString(1, WC_ID);
      oDataSrc_.prepareQuery();
      if (oDataSrc_.next()) {
        C_NO = oDataSrc_.getString("C_NO");
        CARD_NO = oDataSrc_.getString("CARD_NO");
      }*/

      // 获取促销头信息
      StringBuffer selPromoteH = new StringBuffer();
      selPromoteH.append(" select ph.PM_NO, ph.PM_THEME, ph.PM_PRIORITY, ph.PM_COMBINE, ph.PM_CALC\n ");
      selPromoteH.append(" , ph.PM_CUST_LEVEL, ph.PM_HOUR_S, ph.PM_HOUR_E, ph.PM_ACCU\n ");
      selPromoteH.append(" , ph.PM_FULL_AMT, ph.PM_FULL_AMT2, ph.PM_FULL_AMT3\n ");
      selPromoteH.append(" , ph.PM_TTL_QTY, ph.PM_TTL_QTY2, ph.PM_TTL_QTY3\n ");
      selPromoteH.append(" , ph.PM_PRICE, ph.PM_PRICE2, ph.PM_PRICE3\n ");
      selPromoteH.append(" , case when PM_COMBINE = '10' and PM_CALC = '22' then 'P310'  \n "); //  时段
      selPromoteH.append("   when (PM_COMBINE = '10' and PM_CALC in ('40', '41', '42')) or (PM_COMBINE = '11' and PM_CALC in ('20', '21', '22', '30', '40', '41', '42')) then 'P320'  \n "); // 组合
      // 'P330' //套餐/成套
//      selPromoteH.append("   when PM_COMBINE = '30' and PM_CALC in ('20', '21', '22', '30', '40', '41', '42') then 'P340' \n "); // 红绿配
      selPromoteH.append("   when PM_COMBINE = '30' then 'P340' \n "); // 红绿配
      selPromoteH.append("   when PM_COMBINE = '12' and PM_CALC in ('40', '41', '42') then 'P350' \n "); // 多重组合
      selPromoteH.append("   when PM_COMBINE = '12' and PM_CALC in ('50', '51') then 'P360' \n "); // 多买多送
      selPromoteH.append("   when PM_COMBINE in ('20', '21', '22') and PM_CALC in ('10', '11', '30', '31') then 'P370' \n "); // 满额
      selPromoteH.append("   when PM_COMBINE = '10' and PM_CALC in ('60', '61', '62') then 'P380' \n "); // 第二杯N价
      // 'P390' //限时供应
      selPromoteH.append("   else '' end PM_KIND\n ");
      selPromoteH.append(" , ifnull(pd.PM_NO,'') as PM_NO_DETL, pd.RECNO, pd.IS_GIFT, pd.PM_D_KIND, pd.P_NO, pd.P_NO_S, pd.D_NO, pd.PM_QTY, pd.PM_RG\n ");
      selPromoteH.append(" , pd.PM_PRICE as D_PM_PRICE, pd.PM_PRICE2 as D_PM_PRICE2, pd.PM_PRICE3 as D_PM_PRICE3\n ");
      selPromoteH.append(" , pd.PM_PRICE4 as D_PM_PRICE4, pd.PM_PRICE5 as D_PM_PRICE5, pd.PM_PRICE6 as D_PM_PRICE6, pd.GIFT_PRICE\n ");
      selPromoteH.append(" , ifnull(pd2.PMDCNT,0) as PMDCNT, ifnull(p.P_NAME,'') as P_NAME, ifnull(p.P_PRICE,0) as P_PRICE\n ");
      selPromoteH.append(" from Promote_h ph\n ");
      selPromoteH.append(" left join Promote_d pd on pd.PM_NO = ph.PM_NO\n ");
      selPromoteH.append(" left join Part p on p.P_NO = pd.P_NO\n ");
      selPromoteH.append(" left join (select PM_NO, count(PM_NO) as PMDCNT from promote_d group by PM_NO) pd2 on pd2.PM_NO = ph.PM_NO\n ");
      selPromoteH.append(" where ph.FLS_NO = 'CO' and ph.PM_ENABLE = 'Y' and ph.PM_CALC != 0 and ph.PM_SL_TYPE in ('0', '1')\n ");
      selPromoteH.append("   and ( ph.PM_DATE_KIND = '2' or (ph.PM_DATE_S <= ? and ph.PM_DATE_E >= ?) )\n ");
      selPromoteH.append("   and ( ph.PM_DAY_WEEK = '' or instr(ph.PM_DAY_WEEK, ?) > 0 )\n ");
      selPromoteH.append("   and ( ph.PM_DAY_MONTH = '' or instr(ph.PM_DAY_MONTH, ?) > 0 )\n ");
      selPromoteH.append("   and ( (ph.PM_AREA = '1' and ph.PM_S_NO = ?)\n ");
      // 后台已处理，固定给的1了
//      selPromoteH.append("     or (ph.PM_AREA = '2' and exists (select S_NO from Store_gd where SG_NO = ph.PM_SG_NO and S_NO = ?) )\n ");
//      selPromoteH.append("     or (ph.PM_AREA = '3' and instr('/' + ph.PM_GROUP_S_NO + '/'  ,  '/' + ? + '/') > 0)\n ");
      selPromoteH.append("   )\n ");
      selPromoteH.append("   and ( ph.PM_CUST_LEVEL in ('-2', '-1') or (? = 'noMember' and ph.PM_CUST_LEVEL = '0') or (? = 'isMember' and ph.PM_CUST_LEVEL in('1', ?)) )\n ");
      selPromoteH.append(" order by ph.PM_PRIORITY, ph.PM_NO desc, pd.PM_RG, pd.RECNO\n ");

      selPromoteHStmt = oDataSrc_.prepareStmt(selPromoteH.toString());
      selPromoteHStmt.clearParameters();
      selPromoteHStmt.setString(1, todayDate);
      selPromoteHStmt.setString(2, todayDate);
      selPromoteHStmt.setString(3, String.valueOf(todayWeek));
      selPromoteHStmt.setString(4, todayDay);
      selPromoteHStmt.setString(5, S_NO);
//      selPromoteHStmt.setString(6, S_NO);
//      selPromoteHStmt.setString(7, S_NO);
      selPromoteHStmt.setString(6, "".equals(C_NO) ? "noMember" : "");
      selPromoteHStmt.setString(7, "".equals(C_NO) ? "" : "isMember");
      selPromoteHStmt.setString(8, CARD_NO);
      selPromoteHRs = selPromoteHStmt.executeQuery();
      String PM_NO = "";
      int RECNO = 1;
      int RECNO_DETL = 1;

      boolean isMix = false; // 是否promote_d (促销条件表身)的多个组合到一起; 当是红配绿和任意组合的时才有可能需要组合promote_d; 如果是，P_NO,P_NO_S,D_NO采用[][][]的方式隔开
      boolean isFull = false; // 是否满额促销
      String curPM_RG = ""; // 红配绿组别
      String PM_RG = "";
      String PM_RG_QTY = "";
      String P_NO = "";
      String P_NO_S = "";
      String D_NO = "";

      StringBuffer FULL_P_NO_GIFT = new StringBuffer();
      String FULL_P_NO = "";
      String FULL_P_NO_S = "";
      String FULL_D_NO = "";

      String D_P_NO = "";
      String D_P_NO_S = "";
      String D_D_NO = "";
      while (selPromoteHRs.next()) {
        if (!PM_NO.equals(selPromoteHRs.getString("PM_NO"))) {
          // 表头
          if (RECNO > 1) {
            if (RECNO_DETL > 1) {
              if (isMix) {
                // detail end msg
                sResult.append("   \"pmdPNo\":\"").append(D_P_NO).append("\",");
                sResult.append(" \"pmdPNoS\":\"").append(D_P_NO_S).append("\",");
                sResult.append(" \"pmdDNo\":\"").append(D_D_NO).append("\",\n");
                sResult.append("   \"pmdExt\":{");
                sResult.append(" \"isMix\":\"").append(isMix ? "Y" : "N").append("\"");
                sResult.append(" }\n");
                sResult.append("  }");
                D_P_NO = "";
                D_P_NO_S = "";
                D_D_NO = "";
              }
            }
            // pmDetails end
            sResult.append(" ],\n");
            sResult.append(" \"pmPNo\":\"").append(P_NO).append("\",");
            sResult.append(" \"pmPNoS\":\"").append(P_NO_S).append("\",");
            sResult.append(" \"pmDNo\":\"").append(D_NO).append("\",");
            sResult.append(" \"pmRG\":\"").append(PM_RG).append("\",");
            sResult.append(" \"pmRGQty\":\"").append(PM_RG_QTY).append("\",\n");
            sResult.append(" \"pmFullGift\":[").append(FULL_P_NO_GIFT.toString()).append("],\n");
            sResult.append(" \"pmFullPNo\":\"").append(FULL_P_NO).append("\",");
            sResult.append(" \"pmFullPNos\":\"").append(FULL_P_NO_S).append("\",");
            sResult.append(" \"pmFullDNo\":\"").append(FULL_D_NO).append("\"\n");
            sResult.append("},\n");

          }
          PM_NO = selPromoteHRs.getString("PM_NO");
          String PM_COMBINE = selPromoteHRs.getString("PM_COMBINE");
          String PM_CALC = selPromoteHRs.getString("PM_CALC");

          sResult.append("{\"pmNo\":\"").append(PM_NO).append("\",");
          sResult.append(" \"pmTheme\":\"").append(emisBMUtils.escapeJson(selPromoteHRs.getString("PM_THEME"))).append("\",\n");
          sResult.append(" \"pmPriority\":\"").append(selPromoteHRs.getString("PM_PRIORITY")).append("\",");
          sResult.append(" \"pmCombine\":\"").append(PM_COMBINE).append("\",");
          sResult.append(" \"pmCalc\":\"").append(PM_CALC).append("\",");
          sResult.append(" \"pmCustLevel\":\"").append(selPromoteHRs.getString("PM_CUST_LEVEL")).append("\",\n");
          sResult.append(" \"pmHourS\":\"").append(selPromoteHRs.getString("PM_HOUR_S")).append("\",");
          sResult.append(" \"pmHourE\":\"").append(selPromoteHRs.getString("PM_HOUR_E")).append("\",");
          sResult.append(" \"pmAccu\":\"").append(selPromoteHRs.getString("PM_ACCU")).append("\",\n");
          sResult.append(" \"pmFullAmt\":").append(selPromoteHRs.getDouble("PM_FULL_AMT")).append(",");
          sResult.append(" \"pmFullAmt2\":").append(selPromoteHRs.getDouble("PM_FULL_AMT2")).append(",");
          sResult.append(" \"pmFullAmt3\":").append(selPromoteHRs.getDouble("PM_FULL_AMT3")).append(",");
          sResult.append(" \"pmTtlQty\":").append(selPromoteHRs.getInt("PM_TTL_QTY")).append(",");
          sResult.append(" \"pmTtlQty2\":").append(selPromoteHRs.getInt("PM_TTL_QTY2")).append(",");
          sResult.append(" \"pmTtlQty3\":").append(selPromoteHRs.getInt("PM_TTL_QTY3")).append(",");
          sResult.append(" \"pmPrice\":").append(selPromoteHRs.getDouble("PM_PRICE")).append(",");
          sResult.append(" \"pmPrice2\":").append(selPromoteHRs.getDouble("PM_PRICE2")).append(",");
          sResult.append(" \"pmPrice3\":").append(selPromoteHRs.getDouble("PM_PRICE3")).append(",\n");
          sResult.append(" \"pmKind\":\"").append(selPromoteHRs.getString("PM_KIND")).append("\",");
          sResult.append(" \"pmRecno\":\"").append(RECNO).append("\",\n");
          sResult.append(" \"pmDetails\":[");

          RECNO++;
          RECNO_DETL = 1;
          PM_RG = "";
          PM_RG_QTY = "";
          curPM_RG = "";
          P_NO = "";
          P_NO_S = "";
          D_NO = "";

          FULL_P_NO_GIFT.setLength(0);
          FULL_P_NO = "";
          FULL_P_NO_S = "";
          FULL_D_NO = "";

          D_P_NO = "";
          D_P_NO_S = "";
          D_D_NO = "";
          if (("10".equals(PM_COMBINE) && !"22".equals(PM_CALC)) || "12".equals(PM_COMBINE)) {
            isMix = selPromoteHRs.getInt("PMDCNT") > 1;
          } else isMix = "30".equals(PM_COMBINE);

          if ("20".equals(PM_COMBINE) || "21".equals(PM_COMBINE) || "22".equals(PM_COMBINE)) {
            isFull = true;
          } else {
            isFull = false;
          }
        }

        // 表身
        if (!"".equals(selPromoteHRs.getString("PM_NO_DETL"))) {
          String curP_NO = selPromoteHRs.getString("P_NO");
          String curP_NO_S = selPromoteHRs.getString("P_NO_S");
          String curD_NO = selPromoteHRs.getString("D_NO");
          String tempPM_RG = selPromoteHRs.getString("PM_RG");
          int tempPM_QTY = selPromoteHRs.getInt("PM_QTY");
          if (!isMix && !isFull) {
            if (RECNO_DETL > 1) {
              sResult.append(",\n");
            }
            sResult.append("  {\"pmdRecno\":\"").append(selPromoteHRs.getString("RECNO")).append("\",\n");
            sResult.append("   \"pmdIsGift\":\"").append(selPromoteHRs.getString("IS_GIFT")).append("\",");
            sResult.append(" \"pmdDKind\":\"").append(selPromoteHRs.getString("PM_D_KIND")).append("\",\n");
            sResult.append("   \"pmdPNo\":\"").append(curP_NO).append("\",");
            sResult.append(" \"pmdPNoS\":\"").append(curP_NO_S).append("\",");
            sResult.append(" \"pmdDNo\":\"").append(curD_NO).append("\",\n");
            sResult.append("   \"pmdQty\":").append(tempPM_QTY).append(",");
            sResult.append(" \"pmdRG\":\"").append(tempPM_RG).append("\",");
            sResult.append(" \"pmdPrice\":").append(selPromoteHRs.getDouble("D_PM_PRICE")).append(",");
            sResult.append(" \"pmdPrice2\":").append(selPromoteHRs.getDouble("D_PM_PRICE2")).append(",");
            sResult.append(" \"pmdPrice3\":").append(selPromoteHRs.getDouble("D_PM_PRICE3")).append(",");
            sResult.append(" \"pmdPrice4\":").append(selPromoteHRs.getDouble("D_PM_PRICE4")).append(",");
            sResult.append(" \"pmdPrice5\":").append(selPromoteHRs.getDouble("D_PM_PRICE5")).append(",");
            sResult.append(" \"pmdPrice6\":").append(selPromoteHRs.getDouble("D_PM_PRICE6")).append(",");
            sResult.append(" \"pmdGiftPrice\":").append(selPromoteHRs.getDouble("GIFT_PRICE")).append(",\n");
            sResult.append("   \"pmdExt\":{");
            sResult.append(" \"isMix\":\"").append(isMix ? "Y" : "N").append("\"");
            sResult.append(" }\n");
            sResult.append("  }");
          } else if (isFull) {
            if ("Y".equalsIgnoreCase(selPromoteHRs.getString("IS_GIFT"))) {
              if (FULL_P_NO_GIFT.length() > 0) {
                FULL_P_NO_GIFT.append(",");
              }
              FULL_P_NO_GIFT.append("  {\"gPNo\":\"").append(curP_NO).append("\",");
              FULL_P_NO_GIFT.append("   \"gPName\":\"").append(emisBMUtils.escapeJson(selPromoteHRs.getString("P_NAME"))).append("\",\n");
              FULL_P_NO_GIFT.append("   \"gQty\":").append(tempPM_QTY).append(",");
              FULL_P_NO_GIFT.append("   \"gPrice\":").append(selPromoteHRs.getDouble("D_PM_PRICE")).append(",");
              FULL_P_NO_GIFT.append("   \"gPriceOld\":").append(selPromoteHRs.getDouble("P_PRICE")).append(",");
              FULL_P_NO_GIFT.append("   \"gChoose\":").append(0).append("}\n");
            } else {
              if (!"".equals(curP_NO)) FULL_P_NO += "[" + curP_NO + "]";
              if (!"".equals(curP_NO_S)) FULL_P_NO_S += "[" + curP_NO_S + "]";
              if (!"".equals(curD_NO)) FULL_D_NO += "[" + curD_NO + "]";
            }
          } else {
            if (RECNO_DETL == 1 || !tempPM_RG.equals(curPM_RG)) {
              if (RECNO_DETL > 1) {
                sResult.append("   \"pmdPNo\":\"").append(D_P_NO).append("\",");
                sResult.append(" \"pmdPNoS\":\"").append(D_P_NO_S).append("\",");
                sResult.append(" \"pmdDNo\":\"").append(D_D_NO).append("\",\n");
                sResult.append("   \"pmdExt\":{");
                sResult.append(" \"isMix\":\"").append(isMix ? "Y" : "N").append("\"");
                sResult.append(" }\n");
                sResult.append("  },");
                D_P_NO = "";
                D_P_NO_S = "";
                D_D_NO = "";
              }
              sResult.append("  {\"pmdRecno\":\"").append(selPromoteHRs.getString("RECNO")).append("\",\n");
              sResult.append("   \"pmdIsGift\":\"").append(selPromoteHRs.getString("IS_GIFT")).append("\",");
              sResult.append(" \"pmdDKind\":\"").append(selPromoteHRs.getString("PM_D_KIND")).append("\",\n");
              sResult.append("   \"pmdQty\":").append(tempPM_QTY).append(",");
              sResult.append(" \"pmdRG\":\"").append(tempPM_RG).append("\",");
              sResult.append(" \"pmdPrice\":").append(selPromoteHRs.getDouble("D_PM_PRICE")).append(",");
              sResult.append(" \"pmdPrice2\":").append(selPromoteHRs.getDouble("D_PM_PRICE2")).append(",");
              sResult.append(" \"pmdPrice3\":").append(selPromoteHRs.getDouble("D_PM_PRICE3")).append(",");
              sResult.append(" \"pmdPrice4\":").append(selPromoteHRs.getDouble("D_PM_PRICE4")).append(",");
              sResult.append(" \"pmdPrice5\":").append(selPromoteHRs.getDouble("D_PM_PRICE5")).append(",");
              sResult.append(" \"pmdPrice6\":").append(selPromoteHRs.getDouble("D_PM_PRICE6")).append(",");
              sResult.append(" \"pmdGiftPrice\":").append(selPromoteHRs.getDouble("GIFT_PRICE")).append(",\n");
              if (!"".equals(curP_NO)) D_P_NO = "[" + curP_NO + "]";
              if (!"".equals(curP_NO_S)) D_P_NO_S = "[" + curP_NO_S + "]";
              if (!"".equals(curD_NO)) D_D_NO = "[" + curD_NO + "]";
            } else {
              if (!"".equals(curP_NO)) D_P_NO += "[" + curP_NO + "]";
              if (!"".equals(curP_NO_S)) D_P_NO_S += "[" + curP_NO_S + "]";
              if (!"".equals(curD_NO)) D_D_NO += "[" + curD_NO + "]";
            }
          }
          if (!"".equals(curP_NO)) P_NO += "[" + curP_NO + "]";
          if (!"".equals(curP_NO_S)) P_NO_S += "[" + curP_NO_S + "]";
          if (!"".equals(curD_NO)) D_NO += "[" + curD_NO + "]";
          if (!tempPM_RG.equals(curPM_RG)) {
            PM_RG += tempPM_RG + ",";
            PM_RG_QTY += tempPM_QTY + ",";
          }
          curPM_RG = selPromoteHRs.getString("PM_RG");
          RECNO_DETL++;
        }
      }

      if (RECNO > 1) {
        if (RECNO_DETL > 1) {
          if (isMix) {
            // detail end msg
            sResult.append("   \"pmdPNo\":\"").append(D_P_NO).append("\",");
            sResult.append(" \"pmdPNoS\":\"").append(D_P_NO_S).append("\",");
            sResult.append(" \"pmdDNo\":\"").append(D_D_NO).append("\",\n");
            sResult.append("   \"pmdExt\":{");
            sResult.append(" \"isMix\":\"").append(isMix ? "Y" : "N").append("\"");
            sResult.append(" }\n");
            sResult.append("  }");
            D_P_NO = "";
            D_P_NO_S = "";
            D_D_NO = "";
          }
        }
        // pmDetails end
        sResult.append(" ],\n");
        sResult.append(" \"pmPNo\":\"").append(P_NO).append("\",");
        sResult.append(" \"pmPNoS\":\"").append(P_NO_S).append("\",");
        sResult.append(" \"pmDNo\":\"").append(D_NO).append("\",\n");
        sResult.append(" \"pmRG\":\"").append(PM_RG).append("\",");
        sResult.append(" \"pmRGQty\":\"").append(PM_RG_QTY).append("\",\n");
        sResult.append(" \"pmFullGift\":[").append(FULL_P_NO_GIFT.toString()).append("],\n");
        sResult.append(" \"pmFullPNo\":\"").append(FULL_P_NO).append("\",");
        sResult.append(" \"pmFullPNos\":\"").append(FULL_P_NO_S).append("\",");
        sResult.append(" \"pmFullDNo\":\"").append(FULL_D_NO).append("\"\n");
        sResult.append("}\n");
        code = "0";
        msg = "成功";
      } else {
        code = "100";
        msg = "查无资料!";
      }
    } catch (Exception ex) {
      code = "901";
      msg = "查询异常,请重新查询!";
      oLogger_.error(ex, ex);
    } finally {
      if (selPromoteHRs != null) {
        try {
          selPromoteHRs.close();
          selPromoteHRs = null;
        } catch (Exception ex) {
          oLogger_.error(ex, ex);
        }
      }
      oDataSrc_.closePrepareStmt(selPromoteHStmt);
    }

    return "{\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\",\n"
        + " \"prmoteList\":[" + sResult.toString() + "]"
        + "}";
  }


  /**
   * 获取调味组信息
   *
   * @param seano            调味组编号
   * @param selSeasoningStmt 查询statement
   * @return
   */
  private String getSeasoning(String seano, PreparedStatement selSeasoningStmt) {
    StringBuffer sReturn = new StringBuffer();
    ResultSet rs = null;
    try {
      selSeasoningStmt.clearParameters();
      selSeasoningStmt.setString(1, seano);
      rs = selSeasoningStmt.executeQuery();
      int i = 0;
      while (rs.next()) {
        if (i == 0) {
          sReturn.append("    {\"seano\":\"").append(rs.getString("SEA_NO")).append("\",");
          sReturn.append(" \"seaname\":\"").append(emisBMUtils.escapeJson(rs.getString("SEA_NAME"))).append("\",");
          sReturn.append(" \"single\":\"").append(rs.getString("IS_SINGLE")).append("\",\n");
          sReturn.append("     \"item\":[\n");
        } else {
          sReturn.append(",\n");
        }
        i++;
        sReturn.append("      {\"ino\":\"").append(rs.getString("SEA_ITEM_NO")).append("\",");
        sReturn.append(" \"iname\":\"").append(emisBMUtils.escapeJson(rs.getString("SEA_ITEM_NAME"))).append("\",");
        sReturn.append(" \"iprice\":").append(rs.getString("PRICE")).append("}");
      }
      if (i > 0) {
        sReturn.append("\n    ]}");
      }
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
      sReturn.setLength(0);
    } finally {
      if (rs != null) {
        try {
          rs.close();
          rs = null;
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    return sReturn.toString();
  }

  private boolean bSMFixItemSaleOut = false;

  /**
   * 获取套餐组信息
   *
   * @param smno         套餐编号
   * @param selSmenuStmt 查询statement
   * @return
   */
  private String getSmenu(String smno, PreparedStatement selSmenuStmt
      , String MU_NO, HashMap<String, String> seasoningList, PreparedStatement selSeasoningStmt, String S_NO) {
    StringBuffer sReturn = new StringBuffer();
    ResultSet rs = null;
    bSMFixItemSaleOut = false;
    try {
      StringBuffer sSmenuJson = new StringBuffer();
      /*selSmenuStmt.clearParameters();
      selSmenuStmt.setString(1, MU_NO);
      selSmenuStmt.setString(2, S_NO);
      selSmenuStmt.setString(3, smno);*/
      selSmenuStmt.setString(1, smno);
      rs = selSmenuStmt.executeQuery();
      int i = 0;
      String groupType = "";
      String groupA_SEA_NO = "";
      int groupA_Cnt = 0;

      while (rs.next()) {
        if ("A".equalsIgnoreCase(rs.getString("GROUP_TYPE"))) {
          if ("Y".equalsIgnoreCase(rs.getString("WM_SALE_OUT"))) {
            bSMFixItemSaleOut = true;
          }
          if (groupA_Cnt == 0) {
            groupA_SEA_NO = rs.getString("SEA_NO");
          }
          groupA_Cnt++;
        }
        if ("".equals(groupType) || !groupType.equalsIgnoreCase(rs.getString("GROUP_TYPE"))) {
          if (!"".equals(groupType)) {
            sSmenuJson.append("]},\n    ");
          }
          groupType = rs.getString("GROUP_TYPE").toUpperCase();
          sSmenuJson.append("    {\"smenuNo\":\"").append(groupType).append("\",");
          sSmenuJson.append(" \"smenuName\":\"").append(emisBMUtils.escapeJson(rs.getString("GROUP_NAME_" + groupType))).append("\",\n");
          sSmenuJson.append("     \"smenuNum\":\"").append(rs.getString("MAX_NUM_" + groupType)).append("\",");
          sSmenuJson.append(" \"smenuMinNum\":\"").append(rs.getString("MIN_NUM_" + groupType)).append("\",\n");
          sSmenuJson.append("     \"smenuItem\":[\n");
        } else {
          sSmenuJson.append(",\n");
        }
        sSmenuJson.append("      {\"ino\":\"").append(rs.getString("P_NO")).append("\",");
        sSmenuJson.append(" \"iname\":\"").append(emisBMUtils.escapeJson(rs.getString("P_NAME"))).append("\",");
        sSmenuJson.append(" \"iprice\":").append(rs.getString("ADD_PRICE")).append(",");
        sSmenuJson.append(" \"iqty\":").append(rs.getString("SM_QTY")).append("}");
      }
      if (!"".equals(groupType)) {
        sSmenuJson.append("\n    ]}");

//        if (groupA_Cnt == 1 && !"".equals(groupA_SEA_NO)) {
        if (!"".equals(groupA_SEA_NO)) {
          // 调味
          int iSea = 0;
          String[] sea = groupA_SEA_NO.split("/");
          for (String aSea : sea) {
            if (seasoningList.get(aSea) == null) {
              seasoningList.put(aSea, getSeasoning(aSea, selSeasoningStmt));
            }
            String seaTmp = seasoningList.get(aSea);

            if (seaTmp != null && !"".equals(seaTmp)) {
              if (iSea == 0) {
                sReturn.append("\n    \"seasoning\":[\n");
              } else {
                sReturn.append(",\n");
              }
              sReturn.append(seaTmp);
              iSea++;
            }
          }
          if (iSea > 0) {
            sReturn.append("],");
          }
        }

        sReturn.append("\n    \"smenu\":[\n");
        sReturn.append(sSmenuJson.toString());
        sReturn.append("],");
      }
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
      sReturn.setLength(0);
    } finally {
      if (rs != null) {
        try {
          rs.close();
          rs = null;
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    return sReturn.toString();
  }

  private HashMap<String, String> saleTimePart = new HashMap<String, String>();
  private HashMap<String, String> saleTimeDDepart = new HashMap<String, String>();
  private HashMap<String, String> saleTimeDepart = new HashMap<String, String>();

  /**
   * 获取门店限时供应商品/小分类/中分类
   *
   * @param S_NO 门店编号
   */
  private void getSaleTimePart(String S_NO) {
    PreparedStatement selSaleTimeStmt = null;
    ResultSet rs = null;
    try {
      saleTimePart.clear();
      saleTimeDDepart.clear();
      saleTimeDepart.clear();

      String sysDate = emisUtil.todayDateAD();
      emisDate today = new emisDate();
      int week = today.getWeek();
      if (week == 0) {
        week = 7;
      }
      String day = today.getDay();
      String sysTime = emisUtil.todayTimeS();

      StringBuffer selSaleTime = new StringBuffer();
      selSaleTime.append(" select h.ST_NO, h.ST_THEME, h.CAN_SALE, d.RECNO, d.ST_D_KIND, d.P_NO, d.P_NO_S, d.D_NO\n ");
      selSaleTime.append(" from ( select h.ST_NO, h.ST_THEME\n ");
      selSaleTime.append("   , case when ((h.ST_INTERVAL = '1' and h.ST_HOUR_S <= ? and h.ST_HOUR_E >= ?)\n ");
      selSaleTime.append("       or (h.ST_INTERVAL = '2' and (h.ST_DAY_WEEK = '' or  instr(',' + h.ST_DAY_WEEK + ','  ,  ',' + ? + ',') > 0))\n ");
      selSaleTime.append("       or (h.ST_INTERVAL = '3' and (h.ST_DAY_MONTH = '' or  instr(',' + h.ST_DAY_MONTH + ','  ,  ',' + ? + ',' ) > 0)) ) then 'Y' else 'N' end as CAN_SALE\n ");
      selSaleTime.append("   from Sale_Time_H h\n ");
//      selSaleTime.append("   left join Store s on s.S_NO = h.ST_S_NO\n ");
//      selSaleTime.append("   left join Store_gd sg on sg.SG_NO = h.ST_SG_NO\n ");
//      selSaleTime.append("   where  h.FLS_NO = 'CO' and h.ST_ENABLE = 'Y' and h.SL_TYPE = '0' and ifnull(s.S_NO, ifnull(sg.S_NO, '')) = ?\n ");
      selSaleTime.append("   where  h.FLS_NO = 'CO' and h.ST_ENABLE = 'Y' and h.SL_TYPE = '0' and h.ST_S_NO = ?\n ");
      selSaleTime.append("     and ((h.ST_DATE_KIND = '1' and h.ST_DATE_S <= ? and h.ST_DATE_E >= ?) or (h.ST_DATE_KIND = '2'))\n ");
      selSaleTime.append(" ) h\n ");
      selSaleTime.append(" inner join Sale_Time_D d on d.ST_NO = h.ST_NO\n ");
      selSaleTimeStmt = oDataSrc_.prepareStmt(selSaleTime.toString());
      selSaleTimeStmt.clearParameters();
      selSaleTimeStmt.setString(1, sysTime);
      selSaleTimeStmt.setString(2, sysTime);
      selSaleTimeStmt.setString(3, String.valueOf(week));
      selSaleTimeStmt.setString(4, day);
      selSaleTimeStmt.setString(5, S_NO);
      selSaleTimeStmt.setString(6, sysDate);
      selSaleTimeStmt.setString(7, sysDate);
      rs = selSaleTimeStmt.executeQuery();
      while (rs.next()) {
        if ("1".equals(rs.getString("ST_D_KIND"))) {
          // 商品
          if (saleTimePart.get(rs.getString("P_NO")) == null) {
            saleTimePart.put(rs.getString("P_NO"), rs.getString("CAN_SALE"));
          } else if ("Y".equals(rs.getString("CAN_SALE"))) {
            saleTimePart.put(rs.getString("P_NO"), rs.getString("CAN_SALE"));
          }
        } else if ("2".equals(rs.getString("ST_D_KIND"))) {
          // 小分类
          if (saleTimeDDepart.get(rs.getString("P_NO_S")) == null) {
            saleTimeDDepart.put(rs.getString("P_NO_S"), rs.getString("CAN_SALE"));
          } else if ("Y".equals(rs.getString("CAN_SALE"))) {
            saleTimeDDepart.put(rs.getString("P_NO_S"), rs.getString("CAN_SALE"));
          }
        } else if ("3".equals(rs.getString("ST_D_KIND"))) {
          // 中分类
          if (saleTimeDepart.get(rs.getString("D_NO")) == null) {
            saleTimeDepart.put(rs.getString("D_NO"), rs.getString("CAN_SALE"));
          } else if ("Y".equals(rs.getString("CAN_SALE"))) {
            saleTimeDepart.put(rs.getString("D_NO"), rs.getString("CAN_SALE"));
          }
        }
      }
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
    } finally {
      if (rs != null) {
        try {
          rs.close();
          rs = null;
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      if (selSaleTimeStmt != null) {
        oDataSrc_.closePrepareStmt(selSaleTimeStmt);
      }
    }

  }

  /**
   * 检查商品是否可售 (针对限时商品)
   *
   * @param P_NO  商品编号
   * @param DD_NO 小分类编号
   * @param D_NO  中分类编号
   * @return
   */
  private boolean checkPart(String P_NO, String DD_NO, String D_NO) {
    boolean pass = true;

    if (saleTimePart.get(P_NO) != null) {
      if ("Y".equals(saleTimePart.get(P_NO))) {
        return true;
      }
      pass = false;
    }
    if (saleTimeDDepart.get(DD_NO) != null) {
      if ("Y".equals(saleTimeDDepart.get(DD_NO))) {
        return true;
      }
      pass = false;
    }
    if (saleTimeDepart.get(D_NO) != null) {
      if ("Y".equals(saleTimeDepart.get(D_NO))) {
        return true;
      }
      pass = false;
    }

    return pass;
  }

  /**
   * 获取商品分类图片URL
   *
   * @param FileName  商品分类图片文件名
   * @param LOCAL_URL 网页地址
   * @param DefSetImg 默认图
   * @return 完整商品分类图片网页URL
   */
  public String getDepartPicture(String FileName, String LOCAL_URL, String DefSetImg) {
    String sReturn = "";
    try {
      if (FileName != null && !"".equals(FileName.trim())) {
        // 1. 取得商品图片目录(系统目录\images\part
        emisDirectory oRootDir_ = emisFileMgr.getInstance(context_).getDirectory("root").subDirectory("images").subDirectory("bm").subDirectory("depart");
        File oPic = new File(oRootDir_.getDirectory(), FileName);
        // 2. 检查图片是否存在
        if (oPic.exists()) {
          sReturn = LOCAL_URL + "/images/bm/depart/" + FileName + "?v=" + oPic.lastModified();
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      sReturn = "";
    }

    if ("".equals(sReturn) && (DefSetImg != null && !"".equals(DefSetImg))) {
      sReturn = LOCAL_URL + "/" + DefSetImg;
    }
    return sReturn;
  }

  /**
   * 获取商品图片URL
   *
   * @param FileName  商品图片文件名
   * @param LOCAL_URL 网页地址
   * @param DefSetImg 默认图
   * @return 完整商品图片网页URL
   */
  public String getPartPicture(String FileName, String LOCAL_URL, String DefSetImg) {
    String sReturn = "";
    try {
      if (FileName != null && !"".equals(FileName.trim())) {
        // 1. 取得商品图片目录(系统目录\images\part
        emisDirectory oRootDir_ = emisFileMgr.getInstance(context_).getDirectory("root").subDirectory("images").subDirectory("part");
        File oPic = new File(oRootDir_.getDirectory(), FileName);
        // 2. 检查图片是否存在
        if (oPic.exists()) {
          sReturn = LOCAL_URL + "/images/part/" + FileName + "?v=" + oPic.lastModified();
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      sReturn = "";
    }

    if ("".equals(sReturn) && (DefSetImg != null && !"".equals(DefSetImg))) {
      sReturn = LOCAL_URL + "/" + DefSetImg;
    } else if ("".equals(sReturn)) {
      sReturn = LOCAL_URL + "/images/part/none.jpg";
    }
    return sReturn;
  }

}