package com.emis.webservices.service.bm.order;

import com.emis.db.emisProp;
import com.emis.hardware.printer.emisPrinter;
import com.emis.hardware.printer.emisPrinterData;
import com.emis.report.emisString;
import com.emis.util.emisUtil;
import com.emis.webservices.service.bm.utils.emisBMUtils;
import com.emis.webservices.service.emisAbstractService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class emisBMOrderImpl extends emisAbstractService {
  private final static String ACT_ORDERADD = "OrderAdd";

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
  protected String postAction() throws Exception {
    MultivaluedMap<String, String> req = parseRequest();
    // 获取请求Act
    String sAct = req.getFirst("act");
    // 当没有传act时取默认的defaultAct
    if (sAct == null || "".equals(sAct.trim())) {
      sAct = this.defaultAct;
    }
    // 选择响应业务
    if (ACT_ORDERADD.equalsIgnoreCase(sAct)) {
      return doOrderAdd(req);
    }
    return null;
  }

  /**
   * 新增订单
   *
   * @param req
   * @return
   * @throws Exception
   */
  private String doOrderAdd(MultivaluedMap<String, String> req) throws Exception {
    if (req.size() <= 0) {
      return "{\"code\":\"101\",\"msg\":\"无参数\"}";
    }
//    insLog(req, "order");
    oLogger_.info("------------- doOrderAdd BEGIN -------------");




    String code = "";
    String msg = "";
    String WXPAY_APPID = ""; // 微信公众号ID(必填)
    String WXPAY_MCHID = ""; // 微信支付分配的商户号ID(必填)
    String WXPAY_SUBMCHID = ""; // 受理模式下给子商户分配的子商户号(选填)
    String WXPAY_MODE = ""; // 微信支付模式， mode=空or1(默认)表示读取系统参数; mode=2表示读取Z042作业设置
    String prepay_id = ""; // 微信支付统一单号
    String appId = ""; // 公众号ID
    String WXPAY_KEY = ""; // 微信私有Key
    String SL_KEY = "";  // 交易主键
    String ID_NO = "";
    String SL_NO = "0001";  // 交易流水
    String deskId = "";

//    String pfSource_ = emisUtil.parseString(req.getFirst("pfSource_"));
//    String pfAppID_ = emisUtil.parseString(req.getFirst("pfAppID_"));
//    String OpenID = emisUtil.parseString(req.getFirst("OpenID"));
//    String wx_id = emisUtil.parseString(req.getFirst("wx_id"));
    try {
      String SME_URL = "";
      String S_NO = "";

      String sWXPAY_PAY = "";
      String sWM_BOX_P_NO = "";
      String sWM_SEND_P_NO = "";
      String sEROS_P_NON = "";
      String EP_BONUS_EXCHG_PNO = "";
      try {
        emisProp oProp = emisProp.getInstance(context_);
        SME_URL = oProp.get("SME_URL");
        S_NO = oProp.get("S_NO");
        ID_NO = oProp.get("ID_NO");

        sWXPAY_PAY = oProp.get("WXPAY_PAY");
        WXPAY_APPID = oProp.get("WXPAY_APPID");
        appId = oProp.get("WXPAY_APPID");
        WXPAY_MCHID = oProp.get("WXPAY_MCHID");
        WXPAY_SUBMCHID = oProp.get("WXPAY_SUBMCHID");
        WXPAY_MODE = oProp.get("WXPAY_MODE");

        sWM_BOX_P_NO = oProp.get("WM_BOX_P_NO");
        sWM_SEND_P_NO = oProp.get("WM_SEND_P_NO");
        EP_BONUS_EXCHG_PNO = oProp.get("EP_BONUS_EXCHG_PNO");
      } catch (Exception e) {
        oLogger_.error(e, e);
        e.printStackTrace();
      }

      JSONObject order = JSONObject.fromObject(req.getFirst("order"));
      if (order != null && !order.isEmpty()) {
        JSONObject orderInfo = order.getJSONObject("orderInfo");
        JSONArray partList = order.getJSONArray("partList");
        JSONArray fullPmChooseList = null;
        try {
          fullPmChooseList = order.getJSONArray("fullPmChooseList");
        } catch (Exception ex) {
          fullPmChooseList = null;
          oLogger_.error("fullPmChooseList not init");
        }
        /*JSONArray usedPromote = null;
        try {
          usedPromote = order.getJSONArray("usedPromote");
        } catch (Exception ex) {
          usedPromote = null;
          oLogger_.error("usedPromote not init");
        }*/
//        JSONObject orderInput = order.getJSONObject("input");

        String chkPart = checkPart(S_NO, partList);
        if ("nodata".equals(chkPart)) {
          return "{\"code\":\"102\",\"msg\":\"购物车无商品\"}";
        } else if (!"ok".equalsIgnoreCase(chkPart)) {
          return "{\"code\":\"103\",\"msg\":\"购物车有商品已缺货停售\",\"errPart\":[" + chkPart + "]}";
        }


        PreparedStatement oQryPartType = null;
        PreparedStatement oInsSaleOrderH = null;
        PreparedStatement oInsSaleOrderD = null;
        PreparedStatement oInsSaleOrderD_non = null;
        PreparedStatement oInsSaleOrderDis = null;
        PreparedStatement oInsSaleOrderDSM = null;
//        PreparedStatement oInsSaleOrderWMDis = null;
        PreparedStatement oInsPointVUpd = null;
        PreparedStatement oAddbarcardamt = null;
        PreparedStatement oInsSaleOrderWM = null;
        PreparedStatement oUpdSaleOrderH = null;

        try {
          String order_id = "";  // 订单单号
          String nowDate = emisUtil.todayDateAD();  //系统日期
          String nowTime = emisUtil.todayTimeS();  //系统时间

          String SL_DATE = nowDate;  //交易日期（系统日期）
          String SL_TIME = nowTime;  //交易时间（系统时间）
          String C_NO = orderInfo.getString("cNo");
          String CC_NO = "";
          String PAY_SCAN_CODE = orderInfo.getString("payCode");

          Double WXPAY_PAY_AMT = 0.0; //  微信支付金额

          // 配送日期时间， 0表示立即配送(系统日期时间)
          String delivery_time = "0";
          String SEND_DATE = nowDate;
          String SEND_TIME = nowTime;

          // 用餐方式
          String SL_TYPE = orderInfo.getString("oType");
          if ("2".equalsIgnoreCase(SL_TYPE)) {
            SL_TYPE = "0";
          }

/*          String IS_TYPE_TO = "";
          String IS_TYPE_TG = "";
          String IS_TYPE_EI = "";
          String POINT_PAY = "";
          String CUSTAMT_PAY = "";
          String EI_GETFOOD_TYPE = "";
          oDataSrc_.executeQuery("select * from Wechat_Order_Setting where MP_ID = '' ");
          if (oDataSrc_.next()) {
            IS_TYPE_TO = oDataSrc_.getString("IS_TYPE_TO");
            IS_TYPE_TG = oDataSrc_.getString("IS_TYPE_TG");
            IS_TYPE_EI = oDataSrc_.getString("IS_TYPE_EI");
            POINT_PAY = oDataSrc_.getString("POINT_PAY");
            CUSTAMT_PAY = oDataSrc_.getString("CUSTAMT_PAY");
            try {
              EI_GETFOOD_TYPE = oDataSrc_.getString("EI_GETFOOD_TYPE");
            } catch (Exception ex) {
              oLogger_.error(ex);
            }
          }*/

          // 获取会员编号 & 会员卡号
         /* StringBuffer selCustV = new StringBuffer();
          if (emisWxUtils.checkIsWechat(pfSource_)) {
            selCustV.append("select C_NO, CC_NO from Cust_v with(nolock) where WC_ID = ? and ifnull(WC_ID,'') != '' ");
          } else {
            selCustV.append("select c.C_NO, c.CC_NO from \n");
            selCustV.append("(select C_NO from Cust_V_Platform_Id cpi with(nolock) \n");
            selCustV.append(" where cpi.PF_CID = ? and cpi.PF_SOURCE = ? ) cpi \n");
            selCustV.append("inner join Cust_v c with(nolock) on cpi.C_NO = c.C_NO \n");
          }
          oDataSrc_.prepareStmt(selCustV.toString());
          oDataSrc_.clearParameters();
          oDataSrc_.setString(1, emisWxUtils.checkIsWechat(pfSource_) ? wx_id : OpenID);
          if (!emisWxUtils.checkIsWechat(pfSource_)) {
            oDataSrc_.setString(2, pfSource_);
          }
          oDataSrc_.prepareQuery();
          if (oDataSrc_.next()) {
            C_NO = oDataSrc_.getString("C_NO");
            CC_NO = oDataSrc_.getString("CC_NO");
          }*/

          // 付款方式 pay_type	int	支付类型（1：货到付款；2：在线支付）
          String sPayType = "2";
          double total = 0;  //交易总额

          // 获取机台号
          /*oDataSrc_.prepareStmt(" select top 1 ID_NO from Cash_id where S_NO = ? order by ID_NO ");
          oDataSrc_.clearParameters();
          oDataSrc_.setString(1, S_NO);
          oDataSrc_.prepareQuery();
          if (oDataSrc_.next()) {
            ID_NO = oDataSrc_.getString("ID_NO");
          } else {
            ID_NO = emisString.leftB(S_NO + "0", 9);
          }*/
          // 获取交易流水号
          oDataSrc_.prepareStmt(" select ifnull(max(SL_NO)-0,0) + 1 as SL_NO from Sale_order_h where S_NO = ? and ID_NO = ? and SL_DATE = ? ");
          oDataSrc_.clearParameters();
          oDataSrc_.setString(1, S_NO);
          oDataSrc_.setString(2, ID_NO);
          oDataSrc_.setString(3, SL_DATE);
          oDataSrc_.prepareQuery();
          if (oDataSrc_.next()) {
//            System.out.println(oDataSrc_.getInt("SL_NO"));
            SL_NO = emisString.rightB("0000" + oDataSrc_.getInt("SL_NO"),4);
          }
//          System.out.println(SL_NO);
          SL_KEY = S_NO + ID_NO + SL_DATE + SL_NO + "2";
          order_id = "BM" + ID_NO + SL_DATE + SL_NO;

          // 1. 新增表头
          InsSaleOrderH(oInsSaleOrderH, sWXPAY_PAY, SL_KEY, S_NO, ID_NO, SL_DATE, SL_NO, SL_TIME, SEND_DATE, SEND_TIME
              , order_id, total, sPayType, C_NO, "", SL_TYPE, PAY_SCAN_CODE);

          oLogger_.info("------------- doOrder add sale_h end -------------");

          HttpClient _oClient = null;
          int _iStatus = 0;
          Response resp = null;
          String respBody = "";
          try {
            _oClient = new HttpClient();
            _oClient.setConnectionTimeout(120000);
            _oClient.setTimeout(120000);
            PostMethod method = new PostMethod(SME_URL + "/ws/wechatV3/bm/bmOrderAdd");
            method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            method.addParameter("SL_KEY", SL_KEY);
            method.addParameter("S_NO", S_NO);
            method.addParameter("ID_NO", ID_NO);
            method.addParameter("SL_NO", SL_NO);

            for (MultivaluedMap.Entry<String, List<String>> entry : req.entrySet()) {
              List ls = entry.getValue();
              method.addParameter(entry.getKey(), ls.get(0).toString());
            }
            _iStatus = _oClient.executeMethod(method);
            resp = Response.ok(method.getResponseBodyAsString(), MediaType.APPLICATION_JSON).build();
            respBody = method.getResponseBodyAsString();
          } catch (Exception e) {
            oLogger_.error(e, e);
          }

          if (resp != null) {
            oLogger_.warn("resp has data");
          } else {
            oLogger_.warn("resp error");
          }

          oLogger_.warn(respBody);
          JSONObject posResp = null;
          if (respBody == null || "".equals(respBody)) {
            return "{\"code\":\"800\",\"msg\":\"交易处理异常，请重试。\"}";
          } else {
            if (!emisUtil.isJSON(respBody.trim())) {
              return "{\"code\":\"801\",\"msg\":\"交易处理异常，请重试。\"}";
            } else {
              posResp = JSONObject.fromObject(respBody);
              if (posResp == null || posResp.isEmpty()) {
                return "{\"code\":\"802\",\"msg\":\"交易处理异常，请重试。\"}";
              } else {
                String posResp_code = getJsonString(posResp, "code");
                if (!"0".equals(posResp_code) && !"00".equals(posResp_code)) {
                  return respBody;
                }
              }
            }
          }

          deskId = getJsonString(posResp, "deskId") ;  // 取餐码
          if (deskId == null || "".equals(deskId)) {
            deskId = emisString.rightB(ID_NO, 2) + "-" + SL_NO;
          }
          String PAY_TYPE_NO = getJsonString(posResp, "payType") ;  // 付款方式（WXPAY, ALIPAY, CUSTCARD）
          String PAY_NAME = getJsonString(posResp, "payName") ;  // 付款方式-中文(微信支付、支付宝支付、会员卡余额)
          String PAY_PLAN_CODE = getJsonString(posResp, "payPlanCode") ;  // 平台付款单号


          // 2.1 新增表身(一般商品)
          StringBuffer sQryPartType = new StringBuffer();
          sQryPartType.append(" select '0' as P_TYPE, 1 as P_QTY, 1 as CNT from part where P_NO = ? \n ");
          sQryPartType.append(" union all \n ");
          sQryPartType.append(" select '1' as P_TYPE, sum(d.SM_QTY) as P_QTY, count(1) as CNT\n ");
          sQryPartType.append(" from smenu_h h\n ");
          sQryPartType.append(" inner join Smenu_d d on d.SM_NO = h.SM_NO\n ");
          sQryPartType.append(" where h.SM_NO = ? and d.GROUP_TYPE = 'A'\n ");
          sQryPartType.append(" group by h.SM_NO ");
          oQryPartType = oDataSrc_.prepareStmt(sQryPartType.toString());

          StringBuffer sInsSaleOrderD = new StringBuffer();
          sInsSaleOrderD.append(" insert into Sale_order_d (SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, P_NO, P_NO_S, DP_NO, P_TAX\n ");
          sInsSaleOrderD.append(" , SL_QTY, SL_PRICE, SL_AMT, SL_TAXAMT, SL_NOTAXAMT, SL_DISC_AMT, SEA_NO, SEA_AMT\n ");
          sInsSaleOrderD.append(" , SORT_INX, IS_GIVE_GIFT, P_TYPE, P_QTY, WX_PK_RECNO, P_NAME)\n ");
          sInsSaleOrderD.append(" select ?, ?, ?, ?, ?, ?, P_NO, DD_NO, D_NO, P_TAX\n ");
          sInsSaleOrderD.append(" , ?, ?, ?, ?, 0, ?, ?, ?\n ");
          sInsSaleOrderD.append(" , ?, 'N', ?, ?, ?, P_NAME\n ");
          sInsSaleOrderD.append(" from Part where P_NO = ? ");
          oInsSaleOrderD = oDataSrc_.prepareStmt(sInsSaleOrderD.toString());

/*
          StringBuffer sInsSaleOrderD_non = new StringBuffer();
          sInsSaleOrderD_non.append(" insert into Sale_order_d (SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, P_NO, P_NO_S, DP_NO, P_TAX\n ");
          sInsSaleOrderD_non.append(" , SL_QTY, SL_PRICE, SL_AMT, SL_TAXAMT, SL_NOTAXAMT, SL_DISC_AMT, SEA_NO, SORT_INX, IS_GIVE_GIFT, P_TYPE, P_QTY, WX_PK_RECNO, P_NAME)\n ");
          sInsSaleOrderD_non.append(" values( ?, ?, ?, ?, ?, ?, ?, '', '', '2'\n ");
          sInsSaleOrderD_non.append(" , ?, ?, ?, ?, 0, ?, '', ?, 'N', ?, ?, ?, ? ) ");
          oInsSaleOrderD_non = oDataSrc_.prepareStmt(sInsSaleOrderD_non.toString());
*/

          StringBuffer sInsSaleOrderDis = new StringBuffer();
          sInsSaleOrderDis.append(" insert into Sale_order_dis (SL_KEY, RECNO, DISC_SN, S_NO, ID_NO, SL_DATE, SL_NO\n ");
          sInsSaleOrderDis.append(" , DISC_CODE, DISC_NO, DISC_AMT, DISC_QTY, FLS_NO, REASON)\n ");
          sInsSaleOrderDis.append(" values( ?, ?, ?, ?, ?, ?, ?\n ");
          sInsSaleOrderDis.append(" , ?, ?, ?, ?, '3', ? )");
          oInsSaleOrderDis = oDataSrc_.prepareStmt(sInsSaleOrderDis.toString());

          StringBuffer sInsSaleOrderDSM = new StringBuffer();
          sInsSaleOrderDSM.append(" insert into Sale_order_d (SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, P_NO, P_NO_S, DP_NO, P_TAX\n ");
          sInsSaleOrderDSM.append(" , SL_QTY, SL_PRICE, SL_AMT, SL_TAXAMT, SL_NOTAXAMT, SL_DISC_AMT, PK_RECNO, SEA_NO, SEA_AMT\n ");
          sInsSaleOrderDSM.append(" , P_TYPE, P_QTY, SORT_INX, PK_SL_AMT, PK_ADD_PRICE, IS_GIFT, IS_GIVE_GIFT, WX_PK_RECNO, P_NAME)\n ");
          sInsSaleOrderDSM.append(" select ?, ?, ?, ?, ?, ?, h.SM_NO, h.SM_NO, h.SM_DP_NO, h.SM_TAX\n ");
          sInsSaleOrderDSM.append(" , ?, ?, ?, ?, 0, ?, h.SM_NO, '', 0\n ");
          sInsSaleOrderDSM.append(" , '1', 0, ?, sum(d.SM_QTY*d.SM_PRICE), sum(d.ADD_PRICE), '', 'N', ?, h.SM_NAME\n ");
          sInsSaleOrderDSM.append(" from Smenu_h h\n ");
          sInsSaleOrderDSM.append(" inner join Smenu_d d on d.SM_NO = h.SM_NO\n ");
          sInsSaleOrderDSM.append(" where h.SM_NO = ? and ( instr(?, CONCAT('/',d.P_NO,'/') ) > 0 ) \n ");
          sInsSaleOrderDSM.append(" group by h.SM_NO, h.SM_DP_NO, h.SM_TAX\n ");
          sInsSaleOrderDSM.append(" union all\n ");
          sInsSaleOrderDSM.append(" select ?, ? + d.RECNO\n ");
          sInsSaleOrderDSM.append(" , ?, ?, ?, ?, d.P_NO, d.DD_NO, d.D_NO, d.P_TAX\n ");
          sInsSaleOrderDSM.append(" , ?*d.SM_QTY, d.SM_PRICE\n ");
          sInsSaleOrderDSM.append(" , d.SL_AMT, d.SL_AMT\n ");
          sInsSaleOrderDSM.append(" , 0, d.SL_DISC_AMT as DISC_AMT, ?\n ");
          sInsSaleOrderDSM.append(" , case when d.GROUP_TYPE = 'A' and d.RECNO = 1 then ? else  '' end, case when d.GROUP_TYPE = 'A' and d.RECNO = 1 then ? else  0 end\n ");
          sInsSaleOrderDSM.append(" , '2', d.SM_QTY, ?, 0, d.ADD_PRICE, '', 'N', ?, d.P_NAME\n ");
          sInsSaleOrderDSM.append(" from (\n ");
          sInsSaleOrderDSM.append("   select d.*, case when d.SM_AMT_ALL > 0 then round(d.SM_QTY*d.SM_PRICE*?/d.SM_AMT_ALL,2) else 0 end as SL_AMT\n ");
          sInsSaleOrderDSM.append("   , case when d.SM_AMT_ALL > 0 then round(d.SM_QTY*d.SM_PRICE*?/d.SM_AMT_ALL,2) else 0 end as SL_DISC_AMT\n ");
          sInsSaleOrderDSM.append("   from (\n ");
          sInsSaleOrderDSM.append("     select @rownum:=@rownum+1 as RECNO,d.*\n ");
          sInsSaleOrderDSM.append("     from (select @rownum:=0) r,\n ");
          sInsSaleOrderDSM.append("     (select d.P_NO, p.DD_NO, p.D_NO\n ");
//          sInsSaleOrderDSM.append("     , p.P_TAX, d.SM_QTY, d.SM_PRICE, d.ADD_PRICE, sum(d.SM_QTY*d.SM_PRICE) over() as SM_AMT_ALL\n ");
          sInsSaleOrderDSM.append("     , p.P_TAX, d.SM_QTY, d.SM_PRICE, d.ADD_PRICE, 0 as SM_AMT_ALL\n ");
//          sInsSaleOrderDSM.append("     , row_number() over(order by d.SM_QTY*d.SM_PRICE desc) as RECNO_AMT, d.RECNO as RECNO_D, d.GROUP_TYPE \n ");
          sInsSaleOrderDSM.append("     , d.RECNO as RECNO_D, d.GROUP_TYPE, p.P_NAME \n ");
          sInsSaleOrderDSM.append("     from Smenu_h h\n ");
          sInsSaleOrderDSM.append("     inner join Smenu_d d on d.SM_NO = h.SM_NO\n ");
          sInsSaleOrderDSM.append("     inner join Part p on p.P_NO = d.P_NO\n ");
          sInsSaleOrderDSM.append("     where h.SM_NO = ? and ( instr(?, CONCAT('/',d.P_NO,'/') ) > 0 )\n ");
          sInsSaleOrderDSM.append("     order by d.GROUP_TYPE, d.RECNO\n ");
          sInsSaleOrderDSM.append("     ) d\n ");
          sInsSaleOrderDSM.append("   ) d\n ");
          sInsSaleOrderDSM.append(" ) d\n ");
          oInsSaleOrderDSM = oDataSrc_.prepareStmt(sInsSaleOrderDSM.toString());

          StringBuffer sInsSaleOrderWM = new StringBuffer();
          sInsSaleOrderWM.append(" insert into Sale_order_wm (SL_KEY, S_NO, SL_DATE, SL_NO, ORDER_SOURCE, WM_ORD_NO\n ");
          sInsSaleOrderWM.append(" , CRE_TIME, RECEIVE_TIME, PAY_TYPE, SEND_FEE, PACKAGE_FEE, TOTAL_FEE, ORI_PRICE\n ");
          sInsSaleOrderWM.append(" , DELIVERY_SHOP_FEE, DELIVERY_TIPS, DELIVERY_TIPS_AMT, NEED_INVOICE, INVOICE_TITLE\n ");
          sInsSaleOrderWM.append(" , SEND_NOW, DELIVERY_PARTY, ADDR_LNG, ADDR_LAT, DELIVERY_STATUS, DELIVERY_NAME, DELIVERY_PHONE\n ");
          sInsSaleOrderWM.append(" , DELIVERY_ID, ORDER_SEQNO, ORD_ID_VIEW, DS_NO, CLIENT_COUNT, TAXPAYER_ID, SL_TIME)\n ");
          sInsSaleOrderWM.append(" values ( ?, ?, ?, ?, ?, ?\n ");
          sInsSaleOrderWM.append(" , ?, ?, ?, ?, ?, ?, ?\n ");
          sInsSaleOrderWM.append(" , ?, ?, ?, ?, ?\n ");
          sInsSaleOrderWM.append(" , ?, ?, ?, ?, ?, ?, ?\n ");
          sInsSaleOrderWM.append(" , ?, ?, ?, ?, ?, ?, ?) ");
          oInsSaleOrderWM = oDataSrc_.prepareStmt(sInsSaleOrderWM.toString());

          oLogger_.info("------------- doOrder init 2 end -------------");

          String P_TYPE = "";
          int RECNO = 1;
          double SL_QTY = 0;
          double SL_AMT = 0;
          double SL_DISC_AMT = 0;
          double SL_DISC_AMT_WM = 0;
          StringBuffer SL_DISC_AMT_MSG = new StringBuffer();
          int BOX_QTY = 0;  // 餐盒数量
          double BOX_AMT = 0;  // 餐盒费
          double SEND_AMT = 0; //orderInfo.getDouble("shippingFee");  // 配送费
          Map<Double, Integer> box = new HashMap<Double, Integer>();
          HashMap<String, String> seaNames = new HashMap<String, String>();

          int DISC_SN = 1;
          try {
            double SL_AMT_Part = 0;
            int CNT_Part = 0;
            double SL_DIST_sum = 0;
            double SL_DIST_Part = 0;

            for (int iParts = 0; iParts < partList.size(); iParts++) {
              JSONObject part = partList.getJSONObject(iParts);
              if ("0".equals(part.getString("type"))) {
                // 一般商品处理
                JSONArray partSelList = part.getJSONArray("selectors");
                for (int iPartSel = 0; iPartSel < partSelList.size(); iPartSel++) {
                  JSONObject partSel = partSelList.getJSONObject(iPartSel);
                  /*SL_DIST_Part = 0;
                  if (SL_DISC_AMT > 0) {
                    if (SL_AMT_Part != 0) {
                      SL_DIST_Part = emisWxOrderUtils.formatAmt(detlItem.getTotal() * SL_DISC_AMT / SL_AMT_Part);
                      SL_DIST_sum += SL_DIST_Part;
                      if (i == CNT_Part && SL_DIST_sum != SL_DISC_AMT) {
                        SL_DIST_Part += SL_DISC_AMT - SL_DIST_sum;
                      }
                      InsSaleOrderDis(oInsSaleOrderDis, SL_KEY, RECNO, DISC_SN, S_NO, ID_NO, SL_DATE, SL_NO
                          , SL_DIST_Part, detlItem.getQuantity(), emisString.leftB(SL_DISC_AMT_MSG.toString(), 50));
                      DISC_SN++;
                    }
                  }*/
                  SL_DIST_Part = 0;
                  JSONArray pmDetl = null;
                  try {
                    pmDetl = partSel.getJSONArray("pmDetl");
                  } catch (Exception ex) {
                    pmDetl = null;
                    oLogger_.error("pmDetl not init");
                  }
                  if (pmDetl != null && !pmDetl.isEmpty()) {
                    // 包含促销信息
                    for (int iPmsel = 0; iPmsel < pmDetl.size(); iPmsel++) {
                      JSONObject pmItem = pmDetl.getJSONObject(iPmsel);
//                      {pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmQty: (calQty - 0), pmAmt: v.price * useQty }
                      double discAmt = emisUtil.parseDouble(getJsonString(pmItem, "pmAmt"));
                      if (discAmt > 0) {
                        InsSaleOrderDis(oInsSaleOrderDis, SL_KEY, RECNO, DISC_SN, S_NO, ID_NO, SL_DATE, SL_NO
                            , "", getJsonString(pmItem, "pmNo"), discAmt
                            , emisUtil.parseDouble(getJsonString(pmItem, "pmQty"))
                            , emisString.leftB(getJsonString(pmItem, "pmTheme"), 50));
                        DISC_SN++;
                        SL_DIST_Part += discAmt;
                        SL_DISC_AMT += discAmt;
                      }
                    }
                  }

                  InsSaleOrderD(oInsSaleOrderD, SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, SL_DIST_Part, part, partSel, SL_TYPE);
                  RECNO++;
                  SL_QTY += partSel.getInt("qty");
                  SL_AMT += partSel.getInt("qty") * (("1".equals(SL_TYPE) ? part.getDouble("priceWM") : part.getDouble("price")) + partSel.getDouble("seaAmt"));

                  if (("1".equals(SL_TYPE) || "0".equals(SL_TYPE)) && part.getInt("boxQty") > 0) {
                    if (box.get(part.getDouble("boxPrice")) == null) {
                      BOX_QTY = 0;
                    } else {
                      BOX_QTY = box.get(part.getDouble("boxPrice"));
                    }
                    BOX_QTY += part.getInt("boxQty") * partSel.getInt("qty");
                    box.put(part.getDouble("boxPrice"), BOX_QTY);
                  }
                  // 统计用到的调味
                  String seaItemNo = partSel.getString("seaItemNo");
                  if (!"".equals(seaItemNo)) {
                    String [] seaNos = seaItemNo.split("/");
                    for (String seaNo : seaNos) {
                      if(!"".equals(seaNo)) seaNames.put(seaNo, "");
                    }
                  }
                }
              } else if ("1".equals(part.getString("type"))) {
                // 套餐处理
                JSONArray partSelList = part.getJSONArray("selectors");
                for (int iPartSel = 0; iPartSel < partSelList.size(); iPartSel++) {
                  JSONObject partSel = partSelList.getJSONObject(iPartSel);
                  /*SL_DIST_Part = 0;
                  if (SL_DISC_AMT > 0) {
                    if (SL_AMT_Part != 0) {
                      SL_DIST_Part = emisWxOrderUtils.formatAmt(detlItem.getTotal() * SL_DISC_AMT / SL_AMT_Part);
                      SL_DIST_sum += SL_DIST_Part;
                      if (i == CNT_Part && SL_DIST_sum != SL_DISC_AMT) {
                        SL_DIST_Part += SL_DISC_AMT - SL_DIST_sum;
                      }
                      InsSaleOrderDis(oInsSaleOrderDis, SL_KEY, RECNO, DISC_SN, S_NO, ID_NO, SL_DATE, SL_NO
                          , SL_DIST_Part, detlItem.getQuantity(), emisString.leftB(SL_DISC_AMT_MSG.toString(), 50));
                      DISC_SN++;
                    }
                  }*/
                  SL_DIST_Part = 0;
                  JSONArray pmDetl = null;
                  try {
                    pmDetl = partSel.getJSONArray("pmDetl");
                  } catch (Exception ex) {
                    pmDetl = null;
                    oLogger_.error("pmDetl not init");
                  }
                  if (pmDetl != null && !pmDetl.isEmpty()) {
                    // 包含促销信息
                    for (int iPmsel = 0; iPmsel < pmDetl.size(); iPmsel++) {
                      JSONObject pmItem = pmDetl.getJSONObject(iPmsel);
//                      {pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmQty: (calQty - 0), pmAmt: v.price * useQty }
                      double discAmt = emisUtil.parseDouble(getJsonString(pmItem, "pmAmt"));
                      if (discAmt > 0) {
                        InsSaleOrderDis(oInsSaleOrderDis, SL_KEY, RECNO, DISC_SN, S_NO, ID_NO, SL_DATE, SL_NO
                            , "", getJsonString(pmItem, "pmNo"), discAmt
                            , emisUtil.parseDouble(getJsonString(pmItem, "pmQty"))
                            , emisString.leftB(getJsonString(pmItem, "pmTheme"), 50));
                        DISC_SN++;
                        SL_DIST_Part += discAmt;
                        SL_DISC_AMT += discAmt;
                      }
                    }
                  }


                  RECNO += InsSaleOrderDSM(oInsSaleOrderDSM, SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, SL_DIST_Part, part, partSel, SL_TYPE);
//                  RECNO = RECNO + 1; // + oDataSrc_.getInt("CNT");
//                if (detlItem.getSku_id() != null && !"".equals(detlItem.getSku_id())) {
//                  RECNO++;
//                }
                  SL_QTY += partSel.getInt("qty"); // oDataSrc_.getDouble("P_QTY");
                  SL_AMT += partSel.getInt("qty") * (("1".equals(SL_TYPE) ? part.getDouble("priceWM") : part.getDouble("price")) + partSel.getDouble("seaAmt"));

                  if (("1".equals(SL_TYPE) || "0".equals(SL_TYPE)) && part.getInt("boxQty") > 0) {
                    if (box.get(part.getDouble("boxPrice")) == null) {
                      BOX_QTY = 0;
                    } else {
                      BOX_QTY = box.get(part.getDouble("boxPrice"));
                    }
                    BOX_QTY += part.getInt("boxQty") * partSel.getInt("qty");
                    box.put(part.getDouble("boxPrice"), BOX_QTY);
                  }
                  // 统计用到的调味
                  String seaItemNo = partSel.getString("seaItemNo");
                  if (!"".equals(seaItemNo)) {
                    String [] seaNos = seaItemNo.split("/");
                    for (String seaNo : seaNos) {
                      if(!"".equals(seaNo)) seaNames.put(seaNo, "");
                    }
                  }
                }
              }
            }

            if (fullPmChooseList != null) {
              for (int iFullPmGift = 0; iFullPmGift < fullPmChooseList.size(); iFullPmGift++) {
                JSONObject part = fullPmChooseList.getJSONObject(iFullPmGift);
                int pmChooseQty = emisUtil.parseInt(getJsonString(part, "pmChooseQty"));
                String pmCalc = getJsonString(part, "pmCalc");
                if (pmChooseQty > 0 && ("30".equals(pmCalc) || "31".equals(pmCalc))) {
                  // 满额送、满立减
                  JSONArray partSelList = part.getJSONArray("pmFullGift");
                  for (int iPartSel = 0; iPartSel < partSelList.size(); iPartSel++) {
                    JSONObject partSel = partSelList.getJSONObject(iPartSel);
                    int gChoose = emisUtil.parseInt(getJsonString(partSel, "gChoose"));
                    if (gChoose > 0) {
                      double qty = gChoose * partSel.getDouble("gQty");
                      double priceOld = partSel.getDouble("gPriceOld");
                      double amt = gChoose * priceOld;
                      double discAmt = gChoose * (priceOld - partSel.getDouble("gPrice"));

                      InsSaleOrderD_others(oInsSaleOrderD, SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO
                          , getJsonString(partSel, "gPNo"), qty, priceOld, amt, discAmt);
                      InsSaleOrderDis(oInsSaleOrderDis, SL_KEY, RECNO, DISC_SN, S_NO, ID_NO, SL_DATE, SL_NO
                          , "30".equals(pmCalc) ? "E" : "F", getJsonString(part, "pmNo"), discAmt, qty
//                          , "[" + getJsonString(part, "pmTheme") + "]" + ("30".equals(pmCalc) ? "满额送" : "加购赠"));
                          , "[" + getJsonString(part, "pmTheme") + "]");
                      RECNO++;
                      DISC_SN++;

                      SL_QTY += qty;
                      SL_AMT += amt;
                      SL_DISC_AMT += discAmt;
                    }
                  }
                }
              }
            }
          } catch (Exception var11) {
            oLogger_.error(var11, var11);
          }
          oLogger_.info("------------- doOrder insert sale_d* end -------------");

          // 2.2 餐盒费
//          if (("1".equals(SL_TYPE) || "0".equals(SL_TYPE)) && BOX_QTY > 0 && !"".equals(sWM_BOX_P_NO)) {
          if (("1".equals(SL_TYPE) || "0".equals(SL_TYPE)) && BOX_QTY > 0) {
            for (Map.Entry<Double, Integer> boxEntry : box.entrySet()) {
              InsSaleOrderD_non(oInsSaleOrderD_non, SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, 0, sWM_BOX_P_NO, boxEntry.getValue(), boxEntry.getKey(), "餐盒费");
//              InsSaleOrderD_others(oInsSaleOrderD, SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, sWM_BOX_P_NO, boxEntry.getValue(), boxEntry.getKey(), boxEntry.getValue() * boxEntry.getKey());
              RECNO++;
              SL_QTY += boxEntry.getValue();
              SL_AMT += boxEntry.getValue() * boxEntry.getKey();
              BOX_AMT += boxEntry.getValue() * boxEntry.getKey();
            }
          }
          oLogger_.info("------------- doOrder insert box end -------------");


          // 2.3 配送费
          /*if ("1".equals(SL_TYPE) && SEND_AMT != 0 && !"".equals(sWM_SEND_P_NO)) {
            InsSaleOrderD_others(oInsSaleOrderD, SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, sWM_SEND_P_NO, 1, SEND_AMT, SEND_AMT);
            RECNO++;
            SL_QTY += 1;
            SL_AMT += SEND_AMT;
          }*/
          oLogger_.info("------------- doOrder insert send end -------------");



          // 3. 新增外卖扩展信息
          InsSaleOrderWm(oInsSaleOrderWM, SL_KEY, S_NO, SL_DATE, SL_NO, SL_TIME, total
              , sPayType, delivery_time, SEND_AMT, BOX_AMT, deskId, "1", order_id, "1");
          oLogger_.info("------------- doOrder insert sale_order_wm end -------------");

          // 5. 更新订单状态(ED->PP)生效
          WXPAY_PAY_AMT = SL_AMT - 0;
          UpdSaleOrderH(oUpdSaleOrderH, SL_QTY, SL_AMT, SL_DISC_AMT, SL_KEY, emisString.leftB(SL_DISC_AMT_MSG.toString(), 50), sWXPAY_PAY, SL_DISC_AMT_WM, sPayType, 0);

          oLogger_.info("------------- doOrder update sale_order_h end -------------");
          oDataSrc_.commit();

          // 这里要改成微信支付大于0时才调用下面的窗口，现在是总金额
          //可以尝试改成SL_AMT - _UsedAmt>0
          /*if (WXPAY_PAY_AMT > 0) {
            String S_NAME_S = "";
            try {
              oDataSrc_.prepareStmt("select ifnull(S_NAME_S, S_NAME) as S_NAME from Store where S_NO = ? ");
              oDataSrc_.clearParameters();
              oDataSrc_.setString(1, S_NO);
              oDataSrc_.prepareQuery();
              if (oDataSrc_.next()) {
                S_NAME_S = oDataSrc_.getString("S_NAME");
              }
            } catch (Exception ex) {
              oLogger_.error(ex);
            }
            if ("2".equals(WXPAY_MODE)) {
              emisWXPayStoreSettingBean WXPaySetting = emisWXPayStoreSettingKeeper.getInstance().getStoreWXPaySetting(context_, S_NO);
              WXPayV2ConfigImpl wxPayConfig = new WXPayV2ConfigImpl(WXPaySetting.getWXPAY_APPID(), WXPaySetting.getWXPAY_MCHID(), WXPaySetting.getWXPAY_KEY(), WXPaySetting.getWXPAY_CERTLOCALPATH());
              oLogger_.info("PaySetting PS_NO=" + WXPaySetting.getPS_NO() + ", S_NO=" + S_NO);
              HashMap<String, String> data = new HashMap<String, String>();
              if ("2".equals(WXPaySetting.getWXPAY_MODE())) {
                data.put("sub_appid", (emisWxUtils.checkIsWechat(pfSource_) ? WXPaySetting.getWXPAY_SUBAPPID() : pfAppID_));
                data.put("sub_mch_id", WXPaySetting.getWXPAY_SUBMCHID());
                data.put("sub_openid", (emisWxUtils.checkIsWechat(pfSource_) ? wx_id : OpenID));
                appId = (emisWxUtils.checkIsWechat(pfSource_) ? WXPaySetting.getWXPAY_SUBAPPID() : pfAppID_);
              } else {
                data.put("openid", (emisWxUtils.checkIsWechat(pfSource_) ? wx_id : OpenID));
                appId = WXPaySetting.getWXPAY_APPID();
              }
              data.put("device_info", "WEB");
              data.put("body", (emisWxUtils.checkIsWechat(pfSource_) ? (S_NAME_S + "-微信点单") : (S_NAME_S + "-小程序点单")));
//              data.put("detail", "");
//              data.put("attach", "");
              data.put("out_trade_no", SL_KEY);
//              data.put("fee_type", "CNY");
              data.put("total_fee", String.valueOf((int) Math.round(WXPAY_PAY_AMT * 100)));
              // TODO  test change
//              data.put("total_fee", "1");
              data.put("spbill_create_ip", "115.29.195.149");
//              data.put("time_start", "20171013101010");
//              data.put("time_expire", "20171013111111");
//              data.put("goods_tag", "");
              data.put("notify_url", "http://www.weixin.qq.com/wxpay/pay.php");
              data.put("trade_type", "JSAPI");
//              data.put("product_id", "");
//              data.put("limit_pay", "");
//              data.put("scene_info", "");
              *//*oLogger_.info("----- data begin -----");
              for (Map.Entry<String, String> entry : data.entrySet()) {
                oLogger_.info(entry.getKey() + ": " + entry.getValue());
              }
              oLogger_.info("----- data end -----");*//*
              WXPayV2Api wxApi = new WXPayV2Api(wxPayConfig);
              wxApi.setoLogger_(oLogger_);
              Map<String, String> res = wxApi.doUnifiedOrder(data);
              prepay_id = res.get("prepay_id");
              WXPAY_KEY = WXPaySetting.getWXPAY_KEY();
              WXPAY_APPID = WXPaySetting.getWXPAY_APPID();
              WXPAY_MCHID = WXPaySetting.getWXPAY_MCHID();
            } else {
              // 2. 调用微信支付-统一订单接口
              WxData_UnifiedOrder wxPay = new WxData_UnifiedOrder(WXPAY_APPID, WXPAY_MCHID
                  , (emisWxUtils.checkIsWechat(pfSource_) ? (S_NAME_S + "-微信点单") : (S_NAME_S + "-小程序点单")), SL_KEY
                  // TODO  test change
                  , (int) Math.round(WXPAY_PAY_AMT * 100)
//                  , 1
                  , "115.29.195.149", "http://www.weixin.qq.com/wxpay/pay.php"
                  ,(emisWxUtils.checkIsWechat(pfSource_) ? wx_id : OpenID), WxPayUtils.getKey(context_), WXPAY_SUBMCHID);
//      System.out.println("sign= " + wxPay.getSign());

              WxPayApi wxpay = new WxPayApi();
              wxpay.setoLogger_(oLogger_);
              prepay_id = wxpay.unifiedOrder(wxPay);
              WXPAY_KEY = WxPayUtils.getKey(context_);
//      System.out.println(wxpay.unifiedOrder(wxPay));
            }

            code = "0";
            msg = "订单产生成功!";
          } else {*/
            if (orderFin(SL_KEY, "", S_NO)) {
              code = "00";
              msg = "订单产生成功!";

              if (seaNames.size() > 0) {
                seaNames = getSeaName(seaNames);
              }
              // todo print
              print(SL_KEY, S_NO, ID_NO, SL_NO, SL_DATE, SL_TIME, SL_TYPE, deskId, PAY_NAME, PAY_PLAN_CODE, seaNames);

            } else {
              code = "100";
              msg = "付款失败,请重新处理!";
            }
//          }
//          code = "0";
//          msg = "订单产生成功!";
        } catch (Exception ex) {
          code = "901";
          msg = "新增订单异常,请重试!";
          oLogger_.error(ex, ex);
        } finally {
          oDataSrc_.closePrepareStmt(oUpdSaleOrderH);
          oDataSrc_.closePrepareStmt(oInsSaleOrderWM);
          oDataSrc_.closePrepareStmt(oAddbarcardamt);
          oDataSrc_.closePrepareStmt(oInsPointVUpd);
          oDataSrc_.closePrepareStmt(oInsSaleOrderDSM);
          oDataSrc_.closePrepareStmt(oInsSaleOrderD_non);
          oDataSrc_.closePrepareStmt(oInsSaleOrderD);
          oDataSrc_.closePrepareStmt(oInsSaleOrderH);
          oDataSrc_.closePrepareStmt(oQryPartType);
        }
      } else {
        code = "901";
        msg = "新增订单异常,请重试!";
      }
    } catch (Exception ex) {
      code = "901";
      msg = "新增订单异常,请重试!";
      oLogger_.error(ex, ex);
    }

/*    long timeStamp = new Date().getTime();
    String nonceStr = WxPayUtils.getNonce_str();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("appId", appId);
    map.put("timeStamp", timeStamp);
    map.put("nonceStr", nonceStr);
    map.put("package", "prepay_id=" + prepay_id);
    map.put("signType", "MD5");*/


    return "{\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\",\n"
        + " \"slkey\":\"" + SL_KEY + "\",\n"
        + " \"deskNo\":\"" + deskId + "\"\n"
//        + " \"prepay_id\":\"" + prepay_id + "\",\n"
//        + " \"appId\":\"" + appId + "\",\n"
//        + " \"timeStamp\":\"" + timeStamp + "\",\n"
//        + " \"nonceStr\":\"" + nonceStr + "\",\n"
//        + " \"paySign\":\"" + WxPayUtils.getSign(map, WXPAY_KEY) + "\"\n"
        + "}";
//    return "{\"code\":\"" + code + "\",\n \"msg\":\"" + msg + "\"}";
  }

  /**
   * 微信支付完成
   *
   * @param req
   * @return
   * @throws Exception
   */
  private String doOrderPayFin(MultivaluedMap<String, String> req) throws Exception {
    String code = "";
    String msg = "";
    String SL_KEY = req.getFirst("slkey");
    String S_NO = req.getFirst("sno");
    String pfSource_ = emisUtil.parseString(req.getFirst("pfSource_"));
    String pfAppID_ = emisUtil.parseString(req.getFirst("pfAppID_"));
    String OpenID = emisUtil.parseString(req.getFirst("OpenID"));
    String wx_id = emisUtil.parseString(req.getFirst("wx_id"));

    if (orderFin(SL_KEY, "Y", S_NO)) {
      code = "0";
      msg = "成功";
    } else {
      code = "100";
      msg = "付款失败,请重新处理!";
    }
    return "{\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\"}";
  }


  /**
   * 检查订单购物车
   *
   * @param req
   * @return
   */
  private String doOrderCheckCart(MultivaluedMap<String, String> req) {
    String code = "0";
    String msg = "成功";
    oLogger_.info("**************** doOrderCheckCart Debug start ****************");
//    oLogger_.info("order ==>> " + req.getFirst("order"));
    try {
      JSONObject order = JSONObject.fromObject(req.getFirst("order"));
      if (order != null && !order.isEmpty()) {
        JSONArray partList = order.getJSONArray("partList");
        String chkPart = checkPart(req.getFirst("sno"), partList);
        if ("nodata".equals(chkPart)) {
          return "{\"code\":\"102\",\"msg\":\"购物车无商品\"}";
        } else if (!"ok".equalsIgnoreCase(chkPart)) {
          return "{\"code\":\"103\",\"msg\":\"购物车有商品已缺货停售\",\"errPart\":[" + chkPart + "]}";
        }
      }
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
      oLogger_.info("order ==>> " + req.getFirst("order"));
    }
    oLogger_.info("**************** doOrderCheckCart Debug end ****************");
    return "{\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\"\n"
        + "}";
  }

  /**
   * 获取日期字符串转成YYYY/MM/DD hh:mm
   */
  private static String getDatetime(String datetime) {
    return (datetime != null && !"".equals(datetime) && !"null".equalsIgnoreCase(datetime)) ?
        datetime.substring(0, 4) + "/" + datetime.substring(4, 6) + "/" + datetime.substring(6, 8)
            + " " + datetime.substring(8, 10) + ":" + datetime.substring(10, 12) : null;
  }

  /**
   * 获取订单状态名称
   *
   * @param flsno 订单状态编号
   * @return
   */
  private String getFlsName(String flsno, String refundStatus) {
    String msg = "";
    if (flsno.equals("ED")) {
      msg = "待付款";
    } else if (flsno.equals("PP")) {
      msg = "待处理";
    } else if (flsno.equals("AP")) {
      if ("1".equals(refundStatus)) {
        msg = "取消申请";
      } else if ("0".equals(refundStatus)) {
        msg = "拒绝取消";
      } else {
        msg = "门店已接单";
      }
    } else if (flsno.equals("CO")) {
      msg = "已完成";
    } else if (flsno.equals("CL")) {
      msg = "已取消";
    }
    return msg;
  }

  /**
   * 获取用餐方式： 外卖、堂食、外带
   *
   * @param sltype
   * @return
   */
  private String getOrderType(String sltype) {
    String msg = "";
    if (sltype.equals("1")) {
      msg = "外卖";
    } else if (sltype.equals("3")) {
      msg = "堂食";
    } else if (sltype.equals("2") || sltype.equals("0")) {
      msg = "外带";
    }
    return msg;
  }

  /**
   * 获取付款信息
   *
   * @param sl_key 单号号
   * @return
   */
  private String getPayList(String sl_key) {
    StringBuffer sReturn = new StringBuffer("[");

    PreparedStatement pst = null;
    PreparedStatement getOrderAmtStmt = null;
    ResultSet rs = null;
    ResultSet getOrderAmtRs = null;
    try {
      emisProp oProp = emisProp.getInstance(context_);

      ArrayList<String> payList = new ArrayList<String>();
      Map<String, Double> payAmt = new HashMap<String, Double>();

      pst = oDataSrc_.prepareStmt("select name as COLU_NAME from syscolumns where ID = OBJECT_ID('SALE_ORDER_H') and name like 'PAY_%' and name not like ('%_OLD') and name != 'PAY_AMT' ");
      //获取SALE_ORDER_H所有字段为PAY_开头&非_OLD结尾&非PAY_AMT 的付款别
      rs = pst.executeQuery();
      StringBuffer getOrderAmt = new StringBuffer("select ");
      while (rs.next()) {
        payList.add(rs.getString("COLU_NAME"));
        //拼接sql
        getOrderAmt.append(rs.getString("COLU_NAME"));
        getOrderAmt.append(",");
      }
      getOrderAmt.delete(getOrderAmt.length() - 1, getOrderAmt.length());
      getOrderAmt.append("  from SALE_ORDER_H with(nolock) where SL_KEY = ? ");

      getOrderAmtStmt = oDataSrc_.prepareStmt(getOrderAmt.toString());
      getOrderAmtStmt.clearParameters();
      getOrderAmtStmt.setString(1, sl_key);
      getOrderAmtRs = getOrderAmtStmt.executeQuery();
      if (getOrderAmtRs.next()) {
        for (String payItem : payList) {
          String string = getOrderAmtRs.getString(payItem);
          if (string != null) {
            if (getOrderAmtRs.getDouble(payItem) > 0) {
              payAmt.put(payItem, getOrderAmtRs.getDouble(payItem));
            }
          }
        }
      }
      int iCnt = 0;
      for (Map.Entry<String, Double> payAmtEntry : payAmt.entrySet()) {
        if (iCnt++ > 0) {
          sReturn.append("    ,\n");
        }
        sReturn.append("    {\"payId\":\"").append(payAmtEntry.getKey()).append("\",\n");
        sReturn.append("     \"payName\":\"").append(oProp.get(payAmtEntry.getKey())).append("\",\n");
        sReturn.append("     \"payAmt\":\"").append(payAmtEntry.getValue()).append("\",\n");
        sReturn.append("     \"payQty\":\"").append("1").append("\"}\n");
      }
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
      sReturn.setLength(0);
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      if (getOrderAmtRs != null) {
        try {
          getOrderAmtRs.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      if (pst != null) {
        oDataSrc_.closePrepareStmt(pst);
      }
      if (getOrderAmtStmt != null) {
        oDataSrc_.closePrepareStmt(getOrderAmtStmt);
      }
    }

    sReturn.append("]");
    return sReturn.toString();
  }


  private void insLog(MultivaluedMap<String, String> req, String sType) {
    PreparedStatement oInsLogStmt = null;
    try {
      StringBuffer sReq = new StringBuffer();
      for (MultivaluedMap.Entry<String, List<String>> entry : req.entrySet()) {
        sReq.append(entry.getKey()).append("=").append(entry.getValue().get(0)).append(";");
        oLogger_.info("req:" + entry.getKey() + "--->" + entry.getValue().get(0));
      }
      if (sReq.length() <= 0) return;

      String nowDate = emisUtil.todayDateAD();  //系统日期
      String nowTime = emisUtil.todayTimeS();  //系统时间
      oInsLogStmt = oDataSrc_.prepareStmt("insert into WAIMAI_WEBSERVICE_LOG(LO_DATE, LO_TIME, LO_SOURCE, LO_TYPE, LO_REQ_DATA) values(?, ?, ?, ?, ?) ");

      oInsLogStmt.clearParameters();
      oInsLogStmt.setString(1, nowDate);
      oInsLogStmt.setString(2, nowTime);
      oInsLogStmt.setString(3, "BM_ORDER");
      oInsLogStmt.setString(4, sType);
      oInsLogStmt.setString(5, sReq.length() < 2000 ? sReq.toString() : sReq.toString().substring(0, 2000));
      oInsLogStmt.executeUpdate();
      oDataSrc_.commit();
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
    } finally {
      if (oInsLogStmt != null) oDataSrc_.closePrepareStmt(oInsLogStmt);
    }
  }

  private void InsSaleOrderH(PreparedStatement oInsSaleOrderH, String sWXPAY_PAY
      , String SL_KEY, String S_NO, String ID_NO, String SL_DATE, String SL_NO, String SL_TIME
      , String SEND_DATE, String SEND_TIME, String order_id, double total, String sPayType
      , String C_NO, String WC_ID, String SL_TYPE, String PAY_SCAN_CODE
      ) throws Exception {
    StringBuffer sInsSaleOrderH = new StringBuffer();
    sInsSaleOrderH.append(" insert into Sale_order_h ( SL_KEY, S_NO, ORDER_NO, ID_NO, SL_DATE, SL_NO, SL_TIME, SL_INKIND, C_NO, OP_NO, SA_NO, CL_NO, FLS_NO\n ");
    sInsSaleOrderH.append(" , SL_NDISC_AMT, PAY_AMT, PAY_AMT_OLD, CRE_USER, CRE_DATE\n ");
    sInsSaleOrderH.append(" , SL_TYPE, ADDRESS, PHONE, S_NO2, S_NO3, SEND_DATE, SEND_TIME, SHIP_DATE, SHIP_TIME, CUST_NAME\n ");
    sInsSaleOrderH.append(" , SUBFLAG, DEF_V1, DEF_V2, SEND_DATE_OLD, SEND_TIME_OLD, ORDER_SOURCE, ADDR_REGION1, ADDR_REGION2, ADDR_REGION3 \n ");
//    sInsSaleOrderH.append(" , PAY_CASH, ").append(sWXPAY_PAY).append(" , PAY_CASH_OLD, ").append(sWXPAY_PAY).append("_OLD \n");
    sInsSaleOrderH.append(" , REMARK, WM_ORD_NO, WC_ID, WM_WX_PF_SOURCE, WM_WX_PF_APPID, WM_WX_OPENID, PAY_SCAN_CODE )\n ");
    sInsSaleOrderH.append(" values ( ?, ?, ?, ?, ?, ?, ?, '1', ?, '', '', '', 'ED'\n ");
    sInsSaleOrderH.append(" , ?, ?, ?, 'BM_ORDER', ?\n ");
    sInsSaleOrderH.append(" , ?, ?, ?, ?, ?, ?, ?, '', '', ?\n ");
    sInsSaleOrderH.append(" , '01', '997', '', ?, ?, 'BM_ORDER', '', '', ''\n");
//    sInsSaleOrderH.append(" , ?, ?, ?, ?\n ");
    sInsSaleOrderH.append(" , ?, ?, ?, ?, ?, ?, ? ) ");
    oInsSaleOrderH = oDataSrc_.prepareStmt(sInsSaleOrderH.toString());

    oInsSaleOrderH.clearParameters();
    oInsSaleOrderH.setString(1, SL_KEY);
    oInsSaleOrderH.setString(2, S_NO);
    oInsSaleOrderH.setString(3, order_id);
    oInsSaleOrderH.setString(4, ID_NO);
    oInsSaleOrderH.setString(5, SL_DATE);
    oInsSaleOrderH.setString(6, SL_NO);
    oInsSaleOrderH.setString(7, SL_TIME);
    oInsSaleOrderH.setString(8, C_NO);

    oInsSaleOrderH.setDouble(9, 0);
    oInsSaleOrderH.setDouble(10, total);
    oInsSaleOrderH.setDouble(11, total);
    oInsSaleOrderH.setString(12, SL_DATE);

    oInsSaleOrderH.setString(13, SL_TYPE);
    oInsSaleOrderH.setString(14, "");
    oInsSaleOrderH.setString(15, "");
    oInsSaleOrderH.setString(16, S_NO);
    oInsSaleOrderH.setString(17, S_NO);
    oInsSaleOrderH.setString(18, SEND_DATE);
    oInsSaleOrderH.setString(19, SEND_TIME);
    oInsSaleOrderH.setString(20, "");

    oInsSaleOrderH.setString(21, SEND_DATE);
    oInsSaleOrderH.setString(22, SEND_TIME);

//    oInsSaleOrderH.setDouble(23, ("1".equals(sPayType)) ? total : 0.0);
//    oInsSaleOrderH.setDouble(24, ("1".equals(sPayType)) ? 0.0 : total);
//    oInsSaleOrderH.setDouble(25, ("1".equals(sPayType)) ? total : 0.0);
//    oInsSaleOrderH.setDouble(26, ("1".equals(sPayType)) ? 0.0 : total);

    oInsSaleOrderH.setString(23, "");
    oInsSaleOrderH.setString(24, SL_KEY);
    oInsSaleOrderH.setString(25, WC_ID);
    oInsSaleOrderH.setString(26, "");
    oInsSaleOrderH.setString(27, "");
    oInsSaleOrderH.setString(28, "");
    oInsSaleOrderH.setString(29, PAY_SCAN_CODE);
    oInsSaleOrderH.executeUpdate();
    oDataSrc_.commit();
  }

  private void InsSaleOrderD(PreparedStatement oInsSaleOrderD
      , String SL_KEY, int RECNO, String S_NO, String ID_NO, String SL_DATE, String SL_NO, double SL_DISC_AMT
      , JSONObject part, JSONObject partSel, String SL_TYPE) throws Exception {
//    sInsSaleOrderD.append(" insert into Sale_order_d (SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, P_NO, P_NO_S, DP_NO, P_TAX\n ");
//    sInsSaleOrderD.append(" , SL_QTY, SL_PRICE, SL_AMT, SL_TAXAMT, SL_NOTAXAMT, SL_DISC_AMT, SEA_NO, SEA_AMT\n ");
//    sInsSaleOrderD.append(" , SORT_INX, IS_GIVE_GIFT, P_TYPE, P_QTY, WX_PK_RECNO)\n ");
//    sInsSaleOrderD.append(" select ?, ?, ?, ?, ?, ?, P_NO, DD_NO, D_NO, P_TAX\n ");
//    sInsSaleOrderD.append(" , ?, ?, ?, ?, 0, ?, ?, ?\n ");
//    sInsSaleOrderD.append(" , ?, 'N', ?, ?, ?\n ");
//    sInsSaleOrderD.append(" from Part where P_NO = ? ");
    oInsSaleOrderD.clearParameters();
    oInsSaleOrderD.setString(1, SL_KEY);
    oInsSaleOrderD.setInt(2, RECNO);
    oInsSaleOrderD.setString(3, S_NO);
    oInsSaleOrderD.setString(4, ID_NO);
    oInsSaleOrderD.setString(5, SL_DATE);
    oInsSaleOrderD.setString(6, SL_NO);

    oInsSaleOrderD.setDouble(7, partSel.getDouble("qty"));
    oInsSaleOrderD.setDouble(8, ("1".equals(SL_TYPE) ? part.getDouble("priceWM") : part.getDouble("price")) + partSel.getDouble("seaAmt"));
    oInsSaleOrderD.setDouble(9, partSel.getInt("qty") * (("1".equals(SL_TYPE) ? part.getDouble("priceWM") : part.getDouble("price")) + partSel.getDouble("seaAmt")));
    oInsSaleOrderD.setDouble(10, partSel.getInt("qty") * (("1".equals(SL_TYPE) ? part.getDouble("priceWM") : part.getDouble("price")) + partSel.getDouble("seaAmt")) - SL_DISC_AMT);
    oInsSaleOrderD.setDouble(11, SL_DISC_AMT);
    oInsSaleOrderD.setString(12, partSel.getString("seaItemNo"));
    oInsSaleOrderD.setDouble(13, partSel.getDouble("seaAmt"));

    oInsSaleOrderD.setInt(14, RECNO);
    oInsSaleOrderD.setString(15, "0");
    oInsSaleOrderD.setInt(16, 0);
    oInsSaleOrderD.setString(17, "");
    oInsSaleOrderD.setString(18, partSel.getString("pno"));
    oInsSaleOrderD.executeUpdate();
  }

  private int InsSaleOrderDSM(PreparedStatement oInsSaleOrderDSM
      , String SL_KEY, int RECNO, String S_NO, String ID_NO, String SL_DATE, String SL_NO, double SL_DISC_AMT
      , JSONObject part, JSONObject partSel, String SL_TYPE) throws Exception {
//    sInsSaleOrderDSM.append(" insert into Sale_order_d (SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, P_NO, P_NO_S, DP_NO, P_TAX\n ");
//    sInsSaleOrderDSM.append(" , SL_QTY, SL_PRICE, SL_AMT, SL_TAXAMT, SL_NOTAXAMT, SL_DISC_AMT, PK_RECNO, SEA_NO, SEA_AMT\n ");
//    sInsSaleOrderDSM.append(" , P_TYPE, P_QTY, SORT_INX, PK_SL_AMT, PK_ADD_PRICE, IS_GIFT, IS_GIVE_GIFT, WX_PK_RECNO)\n ");
//    sInsSaleOrderDSM.append(" select ?, ?, ?, ?, ?, ?, h.SM_NO, h.SM_NO, h.SM_DP_NO, h.SM_TAX\n ");
//    sInsSaleOrderDSM.append(" , ?, ?, ?, ?, 0, ?, h.SM_NO, '', 0\n ");
//    sInsSaleOrderDSM.append(" , '1', 0, ?, sum(d.SM_QTY*d.SM_PRICE), sum(d.ADD_PRICE), '', 'N', ?\n ");
//    sInsSaleOrderDSM.append(" from Smenu_h h\n ");
//    sInsSaleOrderDSM.append(" inner join Smenu_d d on d.SM_NO = h.SM_NO\n ");
//    sInsSaleOrderDSM.append(" where h.SM_NO = ? and d.GROUP_TYPE = 'A'\n ");
//    sInsSaleOrderDSM.append(" group by h.SM_NO, h.SM_DP_NO, h.SM_TAX\n ");
//    sInsSaleOrderDSM.append(" union all\n ");
//    sInsSaleOrderDSM.append(" select ?, ? + d.RECNO\n ");
//    sInsSaleOrderDSM.append(" , ?, ?, ?, ?, d.P_NO, d.DD_NO, d.D_NO, d.P_TAX\n ");
//    sInsSaleOrderDSM.append(" , ?*d.SM_QTY, d.SM_PRICE\n ");
//    sInsSaleOrderDSM.append(" , case when d.RECNO = 1 then d.SL_AMT + (? - sum(d.SL_AMT) over()) else d.SL_AMT end as D_AMT\n ");
//    sInsSaleOrderDSM.append(" , case when d.RECNO = 1 then d.SL_AMT + (? - sum(d.SL_AMT) over()) else d.SL_AMT end as D_AMT\n ");
//    sInsSaleOrderDSM.append(" , 0, case when d.RECNO = 1 then d.SL_DISC_AMT + (? - sum(d.SL_DISC_AMT) over()) else d.SL_DISC_AMT end as DISC_AMT, ?, '', 0\n ");
//    sInsSaleOrderDSM.append(" , '2', d.SM_QTY, ?, 0, d.ADD_PRICE, '', 'N', ?\n ");
//    sInsSaleOrderDSM.append(" from (\n ");
//    sInsSaleOrderDSM.append("   select d.*, case when d.SM_AMT_ALL > 0 then round(d.SM_QTY*d.SM_PRICE*?/d.SM_AMT_ALL,2) else 0 end as SL_AMT\n ");
//    sInsSaleOrderDSM.append("   , case when d.SM_AMT_ALL > 0 then round(d.SM_QTY*d.SM_PRICE*?/d.SM_AMT_ALL,2) else 0 end as SL_DISC_AMT\n ");
//    sInsSaleOrderDSM.append("   from (\n ");
//    sInsSaleOrderDSM.append("     select row_number() over(order by d.RECNO) as RECNO, d.P_NO, p.DD_NO, p.D_NO\n ");
//    sInsSaleOrderDSM.append("     , p.P_TAX, d.SM_QTY, d.SM_PRICE, d.ADD_PRICE, sum(d.SM_QTY*d.SM_PRICE) over() as SM_AMT_ALL\n ");
//    sInsSaleOrderDSM.append("     , row_number() over(order by d.SM_QTY*d.SM_PRICE desc) as RECNO_AMT, d.RECNO as RECNO_D \n ");
//    sInsSaleOrderDSM.append("     from Smenu_h h\n ");
//    sInsSaleOrderDSM.append("     inner join Smenu_d d on d.SM_NO = h.SM_NO\n ");
//    sInsSaleOrderDSM.append("     inner join Part p on p.P_NO = d.P_NO\n ");
//    sInsSaleOrderDSM.append("     where h.SM_NO = ? and ( charindex('/' + d.P_NO + '/', ? ) > 0 )\n ");
//    sInsSaleOrderDSM.append("   ) d\n ");
//    sInsSaleOrderDSM.append(" ) d\n ");

    double SL_AMT = partSel.getInt("qty") * (("1".equals(SL_TYPE) ? part.getDouble("priceWM") : part.getDouble("price")) + partSel.getDouble("seaAmt"));
    oInsSaleOrderDSM.clearParameters();
    // smenu_h
    oInsSaleOrderDSM.setString(1, SL_KEY);
    oInsSaleOrderDSM.setInt(2, RECNO);
    oInsSaleOrderDSM.setString(3, S_NO);
    oInsSaleOrderDSM.setString(4, ID_NO);
    oInsSaleOrderDSM.setString(5, SL_DATE);
    oInsSaleOrderDSM.setString(6, SL_NO);

    oInsSaleOrderDSM.setDouble(7, partSel.getDouble("qty"));
    oInsSaleOrderDSM.setDouble(8, (("1".equals(SL_TYPE) ? part.getDouble("priceWM") : part.getDouble("price")) + partSel.getDouble("seaAmt")));
    oInsSaleOrderDSM.setDouble(9, SL_AMT - SL_DISC_AMT);
    oInsSaleOrderDSM.setDouble(10, SL_AMT - SL_DISC_AMT);
    oInsSaleOrderDSM.setDouble(11, SL_DISC_AMT);

    oInsSaleOrderDSM.setInt(12, RECNO);
    oInsSaleOrderDSM.setString(13, "");
    oInsSaleOrderDSM.setString(14, partSel.getString("pno"));
    oInsSaleOrderDSM.setString(15, "/" + partSel.getString("smeItemNo"));
    // smenu_d
    oInsSaleOrderDSM.setString(16, SL_KEY);
    oInsSaleOrderDSM.setInt(17, RECNO);
    oInsSaleOrderDSM.setString(18, S_NO);
    oInsSaleOrderDSM.setString(19, ID_NO);
    oInsSaleOrderDSM.setString(20, SL_DATE);
    oInsSaleOrderDSM.setString(21, SL_NO);

    oInsSaleOrderDSM.setDouble(22, partSel.getDouble("qty"));
//    oInsSaleOrderDSM.setDouble(23, SL_AMT - SL_DISC_AMT);
//    oInsSaleOrderDSM.setDouble(24, SL_AMT - SL_DISC_AMT);
//    oInsSaleOrderDSM.setDouble(25, SL_DISC_AMT);
    oInsSaleOrderDSM.setString(23, String.valueOf(RECNO));
    oInsSaleOrderDSM.setString(24, partSel.getString("seaItemNo"));
    oInsSaleOrderDSM.setDouble(25, partSel.getDouble("seaAmt"));
    oInsSaleOrderDSM.setInt(26, RECNO);
    oInsSaleOrderDSM.setString(27, "");

    oInsSaleOrderDSM.setDouble(28, SL_AMT - SL_DISC_AMT);
    oInsSaleOrderDSM.setDouble(29, SL_DISC_AMT);

    oInsSaleOrderDSM.setString(30, partSel.getString("pno"));
    oInsSaleOrderDSM.setString(31, "/" + partSel.getString("smeItemNo"));

    return oInsSaleOrderDSM.executeUpdate();
  }

  private void InsSaleOrderD_others(PreparedStatement oInsSaleOrderD
      , String SL_KEY, int RECNO, String S_NO, String ID_NO, String SL_DATE, String SL_NO
      , String P_NO, double qty, double price, double amt, double SL_DISC_AMT) throws Exception {
    StringBuffer sInsSaleOrderD = new StringBuffer();
//    sInsSaleOrderD.append(" insert into Sale_order_d (SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, P_NO, P_NO_S, DP_NO, P_TAX\n ");
//    sInsSaleOrderD.append(" , SL_QTY, SL_PRICE, SL_AMT, SL_TAXAMT, SL_NOTAXAMT, SL_DISC_AMT, SEA_NO, SEA_AMT\n ");
//    sInsSaleOrderD.append(" , SORT_INX, IS_GIVE_GIFT, P_TYPE, P_QTY, WX_PK_RECNO, P_NAME)\n ");
//    sInsSaleOrderD.append(" select ?, ?, ?, ?, ?, ?, P_NO, DD_NO, D_NO, P_TAX\n ");
//    sInsSaleOrderD.append(" , ?, ?, ?, ?, 0, ?, ?, ?\n ");
//    sInsSaleOrderD.append(" , ?, 'N', ?, ?, ?, P_NAME\n ");
//    sInsSaleOrderD.append(" from Part where P_NO = ? ");
    oInsSaleOrderD.clearParameters();
    oInsSaleOrderD.setString(1, SL_KEY);
    oInsSaleOrderD.setInt(2, RECNO);
    oInsSaleOrderD.setString(3, S_NO);
    oInsSaleOrderD.setString(4, ID_NO);
    oInsSaleOrderD.setString(5, SL_DATE);
    oInsSaleOrderD.setString(6, SL_NO);

    oInsSaleOrderD.setDouble(7, qty);
    oInsSaleOrderD.setDouble(8, price);
    oInsSaleOrderD.setDouble(9, amt);
    oInsSaleOrderD.setDouble(10, amt - SL_DISC_AMT);
    oInsSaleOrderD.setDouble(11, SL_DISC_AMT);
    oInsSaleOrderD.setString(12, "");
    oInsSaleOrderD.setDouble(13, 0);

    oInsSaleOrderD.setInt(14, RECNO);
    oInsSaleOrderD.setString(15, "0");
    oInsSaleOrderD.setInt(16, 0);
    oInsSaleOrderD.setString(17, "");
    oInsSaleOrderD.setString(18, P_NO);
    oInsSaleOrderD.executeUpdate();
  }

  private void InsSaleOrderD_non(PreparedStatement oInsSaleOrderD_non
      , String SL_KEY, int RECNO, String S_NO, String ID_NO, String SL_DATE, String SL_NO, double SL_DISC_AMT
      , String P_NO, double SL_QTY, double SL_PRICE, String P_NAME) throws Exception {
//    sInsSaleOrderD_non.append(" insert into Sale_order_d (SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO, P_NO, P_NO_S, DP_NO, P_TAX\n ");
//    sInsSaleOrderD_non.append(" , SL_QTY, SL_PRICE, SL_AMT, SL_TAXAMT, SL_NOTAXAMT, SL_DISC_AMT, SEA_NO, SORT_INX, IS_GIVE_GIFT, P_TYPE, P_QTY, WX_PK_RECNO, P_NAME)\n ");
//    sInsSaleOrderD_non.append(" values( ?, ?, ?, ?, ?, ?, ?, '', '', '2'\n ");
//    sInsSaleOrderD_non.append(" , ?, ?, ?, ?, 0, ?, '', ?, 'N', ?, ?, ?, ? ) ");
    oInsSaleOrderD_non.clearParameters();
    oInsSaleOrderD_non.setString(1, SL_KEY);
    oInsSaleOrderD_non.setInt(2, RECNO);
    oInsSaleOrderD_non.setString(3, S_NO);
    oInsSaleOrderD_non.setString(4, ID_NO);
    oInsSaleOrderD_non.setString(5, SL_DATE);
    oInsSaleOrderD_non.setString(6, SL_NO);
    oInsSaleOrderD_non.setString(7, P_NO);

    oInsSaleOrderD_non.setDouble(8, SL_QTY);
    oInsSaleOrderD_non.setDouble(9, SL_PRICE);
    oInsSaleOrderD_non.setDouble(10, SL_QTY * SL_PRICE);
    oInsSaleOrderD_non.setDouble(11, SL_QTY * SL_PRICE-SL_DISC_AMT);
    oInsSaleOrderD_non.setDouble(12, SL_DISC_AMT);
    oInsSaleOrderD_non.setInt(13, RECNO);
    oInsSaleOrderD_non.setString(14, "0");
    oInsSaleOrderD_non.setInt(15, 0);
    oInsSaleOrderD_non.setString(16, "");
    oInsSaleOrderD_non.setString(17, P_NAME);
    oInsSaleOrderD_non.executeUpdate();
  }

  private void InsSaleOrderDis(PreparedStatement oInsSaleOrderDis
      , String SL_KEY, int RECNO, int DISC_SN, String S_NO, String ID_NO, String SL_DATE, String SL_NO
      , String DISC_CODE, String DISC_NO, double DISC_AMT, double DISC_QTY, String REASON) throws Exception {
//    sInsSaleOrderDis.append(" insert into Sale_order_dis (SL_KEY, RECNO, DISC_SN, S_NO, ID_NO, SL_DATE, SL_NO\n ");
//    sInsSaleOrderDis.append(" , DISC_CODE, DISC_NO, DISC_AMT, DISC_QTY, FLS_NO, REASON)\n ");
//    sInsSaleOrderDis.append(" values( ?, ?, ?, ?, ?, ?, ?\n ");
//    sInsSaleOrderDis.append(" , ?, ?, ?, ?, '3', ? )");
    oInsSaleOrderDis.clearParameters();
    oInsSaleOrderDis.setString(1, SL_KEY);
    oInsSaleOrderDis.setInt(2, RECNO);
    oInsSaleOrderDis.setInt(3, DISC_SN);
    oInsSaleOrderDis.setString(4, S_NO);
    oInsSaleOrderDis.setString(5, ID_NO);
    oInsSaleOrderDis.setString(6, SL_DATE);
    oInsSaleOrderDis.setString(7, SL_NO);

    oInsSaleOrderDis.setString(8, DISC_CODE);
    oInsSaleOrderDis.setString(9, DISC_NO);
    oInsSaleOrderDis.setDouble(10, DISC_AMT);
    oInsSaleOrderDis.setDouble(11, DISC_QTY);
    oInsSaleOrderDis.setString(12, REASON);
    oInsSaleOrderDis.executeUpdate();
  }

  private void InsSaleOrderWMDis() {

  }

  private void InsSaleOrderWm(PreparedStatement oInsSaleOrderWM
      , String SL_KEY, String S_NO, String SL_DATE, String SL_NO, String SL_TIME
      , double total, String sPayType, String delivery_time, double SEND_AMT, double BOX_AMT
      , String DS_NO, String CLIENT_COUNT, String ORD_ID_VIEW, String EI_GETFOOD_TYPE) throws Exception {
    oInsSaleOrderWM.clearParameters();
    oInsSaleOrderWM.setString(1, SL_KEY);
    oInsSaleOrderWM.setString(2, S_NO);
    oInsSaleOrderWM.setString(3, SL_DATE);
    oInsSaleOrderWM.setString(4, SL_NO);
    oInsSaleOrderWM.setString(5, "BM_ORDER");
    oInsSaleOrderWM.setString(6, SL_KEY);
    // , CRE_TIME, RECEIVE_TIME, PAY_TYPE, SEND_FEE, PACKAGE_FEE, TOTAL_FEE, ORI_PRICE
    oInsSaleOrderWM.setString(7, SL_DATE + SL_TIME);
    oInsSaleOrderWM.setString(8, SL_DATE + SL_TIME);
    oInsSaleOrderWM.setString(9, sPayType);
    oInsSaleOrderWM.setDouble(10, SEND_AMT);
    oInsSaleOrderWM.setDouble(11, BOX_AMT);
    oInsSaleOrderWM.setDouble(12, total);
    oInsSaleOrderWM.setDouble(13, total);
    // , DELIVERY_SHOP_FEE, DELIVERY_TIPS, DELIVERY_TIPS_AMT, NEED_INVOICE, INVOICE_TITLE
    oInsSaleOrderWM.setString(14, "0");
    oInsSaleOrderWM.setString(15, "");
    oInsSaleOrderWM.setString(16, "0");
    oInsSaleOrderWM.setString(17, "0");
    oInsSaleOrderWM.setString(18, "");
    // , SEND_NOW, DELIVERY_PARTY, ADDR_LNG, ADDR_LAT, DELIVERY_STATUS, DELIVERY_NAME, DELIVERY_PHONE
    oInsSaleOrderWM.setString(19, "0".equals(delivery_time) ? "1" : "2");
    // 配送方式  2 自配送； 1 专送
    oInsSaleOrderWM.setString(20, "2");
    oInsSaleOrderWM.setString(21, "0");
    oInsSaleOrderWM.setString(22, "0");
    oInsSaleOrderWM.setString(23, "0");
    oInsSaleOrderWM.setString(24, "");
    oInsSaleOrderWM.setString(25, "");
    // , DELIVERY_ID, ORDER_SEQNO, ORD_ID_VIEW, DS_NO, CLIENT_COUNT, TAXPAYER_ID
    oInsSaleOrderWM.setString(26, "");
    // EI_GETFOOD_TYPE
    oInsSaleOrderWM.setString(27, ("2".equals(EI_GETFOOD_TYPE) && !"".equals(DS_NO)) ? "0" : SL_NO);
    oInsSaleOrderWM.setString(28, ORD_ID_VIEW);
    oInsSaleOrderWM.setString(29, DS_NO);
    oInsSaleOrderWM.setString(30, CLIENT_COUNT);
    oInsSaleOrderWM.setString(31, "");
    oInsSaleOrderWM.setString(32, SL_TIME);
    oInsSaleOrderWM.executeUpdate();
  }

  private void UpdSaleOrderH(PreparedStatement oUpdSaleOrderH, double SL_QTY, double SL_AMT, double SL_DISC_AMT, String SL_KEY
      , String sMANUAL_DISC, String sWM_WECHAT_PAY, double SL_DISC_AMT_WM, String sPayType, double PAY_3) throws Exception {
    StringBuffer sUpdSaleOrderH = new StringBuffer();
    sUpdSaleOrderH.append(" update Sale_order_h set SL_QTY = ?, SL_AMT = ?, SL_TAXAMT = 0, SL_NOTAXAMT = ?, SL_DISC_AMT = ? \n ");
    sUpdSaleOrderH.append(" , MANUAL_DISC = ?, MANUAL_DISC_AMT = ?, MANUAL_DISC_CODE = ? \n ");
//    sUpdSaleOrderH.append(" , PAY_CASH = ?, ").append(sWM_WECHAT_PAY).append(" = ?, PAY_CASH_OLD = ?, ").append(sWM_WECHAT_PAY).append("_OLD = ? \n");
//    sUpdSaleOrderH.append(" , PAY_AMT = ?, PAY_AMT_OLD = ?, PAY_3 = ?, PAY_3_OLD = ? \n ");
    sUpdSaleOrderH.append(" , PAY_AMT = ?, PAY_AMT_OLD = ? \n ");
    sUpdSaleOrderH.append(" where SL_KEY = ? ");
    oUpdSaleOrderH = oDataSrc_.prepareStmt(sUpdSaleOrderH.toString());
    oUpdSaleOrderH.clearParameters();
    oUpdSaleOrderH.setDouble(1, SL_QTY);
    oUpdSaleOrderH.setDouble(2, SL_AMT);
    oUpdSaleOrderH.setDouble(3, SL_AMT - SL_DISC_AMT);
    oUpdSaleOrderH.setDouble(4, SL_DISC_AMT);

    oUpdSaleOrderH.setString(5, sMANUAL_DISC);
    oUpdSaleOrderH.setDouble(6, SL_DISC_AMT);
    oUpdSaleOrderH.setString(7, SL_DISC_AMT != 0 ? "8" : "");

//    oUpdSaleOrderH.setDouble(8, ("1".equals(sPayType)) ? SL_AMT - SL_DISC_AMT - SL_DISC_AMT_WM - PAY_3 : 0.0);
//    oUpdSaleOrderH.setDouble(9, ("1".equals(sPayType)) ? SL_DISC_AMT_WM : SL_AMT - SL_DISC_AMT - PAY_3);
//    oUpdSaleOrderH.setDouble(10, ("1".equals(sPayType)) ? SL_AMT - SL_DISC_AMT - SL_DISC_AMT_WM - PAY_3 : 0.0);
//    oUpdSaleOrderH.setDouble(11, ("1".equals(sPayType)) ? SL_DISC_AMT_WM : SL_AMT - SL_DISC_AMT - PAY_3);
    oUpdSaleOrderH.setDouble(8, SL_AMT - SL_DISC_AMT);
    oUpdSaleOrderH.setDouble(9, SL_AMT - SL_DISC_AMT);
//    oUpdSaleOrderH.setDouble(14, PAY_3);
//    oUpdSaleOrderH.setDouble(15, PAY_3);

    oUpdSaleOrderH.setString(10, SL_KEY);
    oUpdSaleOrderH.executeUpdate();
  }

  /**
   * 订单付款处理
   *
   * @param SL_KEY 客订单号
   * @return
   */
  private boolean orderFin(String SL_KEY, String WXPAY_FLAG, String S_NO_) {
    boolean bReturn = false;
    PreparedStatement oUpdSaleOrderH = null;
    try {
      String wxpay_transaction_id = "";
      int wxpay_total_fee = 0;
/*      if ("Y".equalsIgnoreCase(WXPAY_FLAG)) {
        String WXPAY_APPID = ""; // 微信公众号ID(必填)
        String WXPAY_MCHID = ""; // 微信支付分配的商户号ID(必填)
        String WXPAY_SUBMCHID = ""; // 受理模式下给子商户分配的子商户号(选填)
        String WXPAY_MODE = ""; // 微信支付模式， mode=空or1(默认)表示读取系统参数; mode=2表示读取Z042作业设置
        try {
          emisProp oProp = emisProp.getInstance(context_);
          WXPAY_APPID = oProp.get("WXPAY_APPID");
          WXPAY_MCHID = oProp.get("WXPAY_MCHID");
          WXPAY_SUBMCHID = oProp.get("WXPAY_SUBMCHID");
          WXPAY_MODE = oProp.get("WXPAY_MODE");
        } catch (Exception e) {
          e.printStackTrace();
          oLogger_.error(e, e);
        }

        // 2. 调用微信支付-订单查询接口 获取微信支付订单号
        if ("2".equals(WXPAY_MODE)) {
          emisWXPayStoreSettingBean WXPaySetting = emisWXPayStoreSettingKeeper.getInstance().getStoreWXPaySetting(context_, S_NO_);
          WXPayV2ConfigImpl wxPayConfig = new WXPayV2ConfigImpl(WXPaySetting.getWXPAY_APPID(), WXPaySetting.getWXPAY_MCHID(), WXPaySetting.getWXPAY_KEY(), WXPaySetting.getWXPAY_CERTLOCALPATH());
          oLogger_.info("PaySetting PS_NO=" + WXPaySetting.getPS_NO() + ", S_NO=" + S_NO_);
          HashMap<String, String> data = new HashMap<String, String>();
          if ("2".equals(WXPaySetting.getWXPAY_MODE())) {
            data.put("sub_appid", (emisWxUtils.checkIsWechat(pfSource_) ? WXPaySetting.getWXPAY_SUBAPPID() : pfAppID_));
            data.put("sub_mch_id", WXPaySetting.getWXPAY_SUBMCHID());
          }
          data.put("out_trade_no", SL_KEY);
          WXPayV2Api wxApi = new WXPayV2Api(wxPayConfig);
          wxApi.setoLogger_(oLogger_);
          Map<String, String> res = wxApi.doOrderQuery(data);
          wxpay_transaction_id = res.get("transaction_id") == null ? "" : res.get("transaction_id");
          wxpay_total_fee = emisUtil.parseInt(res.get("total_fee"));

        } else {
          WxData_OrderQuery wxOrder1 = new WxData_OrderQuery(WXPAY_APPID, WXPAY_MCHID, "", SL_KEY, WxPayUtils.getKey(context_), WXPAY_SUBMCHID);
          WxPayApi wxpay = new WxPayApi();
          wxpay.setoLogger_(oLogger_);
          WxData_OrderQueryRes wxOrderRes = wxpay.orderQuery(wxOrder1);
//      System.out.println(wxpay.orderQuery(wxOrder1).getTransaction_id());
          wxpay_transaction_id = wxOrderRes.getTransaction_id() == null ? "" : wxOrderRes.getTransaction_id();
          wxpay_total_fee = wxOrderRes.getTotal_fee();
        }
      }*/

      if ("".equalsIgnoreCase(WXPAY_FLAG) || (wxpay_transaction_id != null && !"".equals(wxpay_transaction_id))) {
        StringBuffer sUpdSaleOrderH = new StringBuffer();
        sUpdSaleOrderH.append(" update Sale_order_h set FLS_NO = 'PP', wxpay_out_trade_no = ?, wxpay_transaction_id = ?, wxpay_total_fee = ? \n ");
        sUpdSaleOrderH.append(" where SL_KEY = ? and ORDER_SOURCE = 'BM_ORDER' and FLS_NO = 'ED' ");
        oUpdSaleOrderH = oDataSrc_.prepareStmt(sUpdSaleOrderH.toString());
        oUpdSaleOrderH.clearParameters();
        oUpdSaleOrderH.setString(1, "".equalsIgnoreCase(WXPAY_FLAG) ? "" : SL_KEY);
        oUpdSaleOrderH.setString(2, wxpay_transaction_id);
        oUpdSaleOrderH.setDouble(3, wxpay_total_fee);
        oUpdSaleOrderH.setString(4, SL_KEY);
        bReturn = oUpdSaleOrderH.executeUpdate() > 0;

        PreparedStatement oSelPointVUpd = null;
        PreparedStatement oupdPointVUpd = null;
        ResultSet rs1 = null;
        PreparedStatement oSelBarcardAmt = null;
        PreparedStatement oUpdBarcardAmt = null;
        PreparedStatement oInsSaleOrderCard = null;
        PreparedStatement oUpdGiftToken = null;
        PreparedStatement oInsGiftLift = null;
        PreparedStatement oSelSaleOrderH = null;
        try {
          /*
          oSelPointVUpd = oDataSrc_.prepareStmt(" select PU_NO, GF_NO, OPT_TYPE, C_NO1 from POINT_V_UPD where PU_NO = ? ");
          oSelPointVUpd.clearParameters();
          oSelPointVUpd.setString(1, SL_KEY);
          rs1 = oSelPointVUpd.executeQuery();
          if (rs1.next()) {
            oupdPointVUpd = oDataSrc_.prepareStmt("exec dbo.eposPointVUpd_CO ?, ?, ?, ?, ?, ? ");
            oupdPointVUpd.setString(1, rs1.getString("PU_NO"));
            oupdPointVUpd.setString(2, rs1.getString("GF_NO"));
            oupdPointVUpd.setString(3, rs1.getString("OPT_TYPE"));
            oupdPointVUpd.setString(4, rs1.getString("C_NO1"));
            oupdPointVUpd.setString(5, "CO");
            oupdPointVUpd.setString(6, "wechat");
            oupdPointVUpd.executeUpdate();
          }

          // 更新会员卡余额
          //获取barcard_amt中的各种需要的参数
          oSelBarcardAmt = oDataSrc_.prepareStmt("select b.GT_NO, b.OPT_AMT, b.BALANCE, b.S_NO, b.ID_NO, b.SL_DATE, b.CRE_TIME, b.SL_NO, h.SL_AMT\n" +
              " from barcard_amt b\n" +
              " inner join Sale_order_h h on h.SL_KEY = b.SL_KEY\n" +
              " where b.SL_KEY = ? and b.OPT_TYPE = '01' and b.FLS_NO = 'ED' ");
          oSelBarcardAmt.clearParameters();
          oSelBarcardAmt.setString(1, SL_KEY);
          rs1 = oSelBarcardAmt.executeQuery();
          if (rs1.next()) {
            String CARD_NO = rs1.getString("GT_NO");  // 卡片编号
            double SL_AMT = rs1.getDouble("SL_AMT");  // 交易金额
            double OPT_AMT = rs1.getDouble("OPT_AMT");  // 刷卡金额
            double BALANCE = rs1.getDouble("BALANCE");  // 异动前余额
            String S_NO = rs1.getString("S_NO");  // 门店编号
            String ID_NO = rs1.getString("ID_NO");  // 机台号码
            String SL_DATE = rs1.getString("SL_DATE");  // 交易日期
            String CRE_TIME = rs1.getString("CRE_TIME");  // 交易时间
            String SL_NO = rs1.getString("SL_NO");  // 流水号
            double IC_AMT = 0; //原卡片余额
            double IC_PCT = 0; //原卡片折数
            String SN = ""; //卡片序列号
            String GS_NO = ""; //卡片种类
            String GS_NAME = ""; //卡片名称
            String GT_FLS_NO = ""; //卡片状态

            //获取GIFT_TOKEN表中的折数(IC_PCT)和卡片序列号(SN)
            oDataSrc_.prepareStmt("select g.IC_AMT, g.IC_PCT, ifnull(g.SN,'') as SN, g.GS_NO, ifnull(gs.GS_NAME,'') as GS_NAME, g.FLS_NO\n" +
                " from Gift_Token g\n" +
                " left join Gift_set gs on gs.GS_NO = g.GS_NO\n" +
                " where g.GT_NO = ? ");
            oDataSrc_.clearParameters();
            oDataSrc_.setString(1, CARD_NO);
            oDataSrc_.prepareQuery();
            if (oDataSrc_.next()) {
              IC_AMT = oDataSrc_.getDouble("IC_AMT");
              IC_PCT = oDataSrc_.getDouble("IC_PCT");
              SN = oDataSrc_.getString("SN");
              GS_NO = oDataSrc_.getString("GS_NO");
              GS_NAME = oDataSrc_.getString("GS_NAME");
              GT_FLS_NO = oDataSrc_.getString("FLS_NO");
            }

            //更新异动表，状态改成CO
            oUpdBarcardAmt = oDataSrc_.prepareStmt("update Barcard_Amt set FLS_NO = 'CO', CO_TIME = dbo.GetLocalDate(), OPT_TIME = ? where SL_KEY = ? and GT_NO = ? and OPT_TYPE = '01' ");
            oUpdBarcardAmt.clearParameters();
            oUpdBarcardAmt.setString(1, SL_DATE + CRE_TIME);
            oUpdBarcardAmt.setString(2, SL_KEY);
            oUpdBarcardAmt.setString(3, CARD_NO);
            oUpdBarcardAmt.executeUpdate();

            // Bug #38641 2017/05/08 Harry modify 修正，消费不重算卡折数
//            Double new_IC_AMT = (double) Math.round((IC_AMT - OPT_AMT) * 100) / 100;
            //新的卡券折数 = （充值前余额 * 充值前折数 / 100 - 本次刷卡金额）/ (充值前余额 - 本次刷卡金额) * 100;
//            Double new_IC_PCT = IC_PCT; // (IC_AMT * IC_PCT / 100 - OPT_AMT) / (IC_AMT - OPT_AMT) * 100;
            //生成一张消费卡刷卡记录明细表 sale_order_card
//            System.out.println("************"+"生成生成一张消费卡刷卡记录明细表"+"********************");
            StringBuffer sInsSaleOrderCard = new StringBuffer();
            sInsSaleOrderCard.append(" insert into Sale_Order_Card (SL_KEY, RECNO, S_NO, ID_NO, SL_DATE, SL_NO\n");
            sInsSaleOrderCard.append(" , FLS_NO, CARD_NO, CARD_NAME, CARD_TYPE, BALANCE, SL_AMT, AMT\n");
            sInsSaleOrderCard.append(" , CRE_DATE, CRE_TIME, FLS_NO_S, REMARK, CARD_PCT, QTY, DISC_AMT, SN)\n");
            sInsSaleOrderCard.append(" values ( ?, 1, ?, ?, ?, ? \n");
            sInsSaleOrderCard.append(" , '3', ?, ?, '1', ?, ?, ? \n");
            sInsSaleOrderCard.append(" , ?, ?, '2', N'微信点餐-会员余额付款', ?, 1, 0, ?)");
            oInsSaleOrderCard = oDataSrc_.prepareStmt(sInsSaleOrderCard.toString());
            oInsSaleOrderCard.clearParameters();
            oInsSaleOrderCard.setString(1, SL_KEY);
            oInsSaleOrderCard.setString(2, S_NO);
            oInsSaleOrderCard.setString(3, ID_NO);
            oInsSaleOrderCard.setString(4, SL_DATE);
            oInsSaleOrderCard.setString(5, SL_NO);

            oInsSaleOrderCard.setString(6, CARD_NO);
            oInsSaleOrderCard.setString(7, GS_NAME);
            // sale_order_card的余额=刷卡前余额-刷卡金额
            oInsSaleOrderCard.setDouble(8, BALANCE - OPT_AMT);
            oInsSaleOrderCard.setDouble(9, SL_AMT);
            oInsSaleOrderCard.setDouble(10, OPT_AMT);

            oInsSaleOrderCard.setString(11, SL_DATE);
            oInsSaleOrderCard.setString(12, CRE_TIME);
            oInsSaleOrderCard.setDouble(13, IC_PCT);
            oInsSaleOrderCard.setString(14, SN);
            oInsSaleOrderCard.executeUpdate();

            // 更新卡余额
            oUpdGiftToken = oDataSrc_.prepareStmt("update Gift_Token set IC_AMT = IC_AMT - ? where GT_NO = ? ");
            oUpdGiftToken.clearParameters();
            oUpdGiftToken.setDouble(1, OPT_AMT);
            oUpdGiftToken.setString(2, CARD_NO);
            oUpdGiftToken.executeUpdate();

            //2.9 新增生命历程GIFT_LIFE
            StringBuffer sAddgiftLift = new StringBuffer();
            sAddgiftLift.append(" insert into Gift_Life (GI_NO, GS_NO, GI_QTY, GI_AMT, GI_FLS_NO_OLD, GI_FLS_NO_NEW\n");
            sAddgiftLift.append(" , GI_BILL_NO, GI_BILL_SNO, GI_BILL_DATE, GI_BILL_TIME, GI_TYPE, REMARK1, REMARK2\n");
            sAddgiftLift.append(" , GI_DATE, GI_TIME, SN)\n");
            sAddgiftLift.append(" values (?, ?, 1, ?, ?, ?\n");
            sAddgiftLift.append(" , ?, ?, ?, ?, 'SOC', N'微信点餐-会员余额付款', ''\n");
            sAddgiftLift.append(" , convert(nvarchar(8), dbo.GetLocalDate(), 112), replace(convert(nvarchar(8),dbo.GetLocalDate(),108), ':',''), ?)");
            oInsGiftLift = oDataSrc_.prepareStmt(sAddgiftLift.toString());
            oInsGiftLift.clearParameters();
            oInsGiftLift.setString(1, CARD_NO);
            oInsGiftLift.setString(2, GS_NO);
            oInsGiftLift.setDouble(3, OPT_AMT);
            oInsGiftLift.setString(4, GT_FLS_NO);
            oInsGiftLift.setString(5, GT_FLS_NO);

            oInsGiftLift.setString(6, SL_KEY);
            oInsGiftLift.setString(7, S_NO);
            oInsGiftLift.setString(8, SL_DATE);
            oInsGiftLift.setString(9, CRE_TIME);
            oInsGiftLift.setString(10, SN);
            oInsGiftLift.executeUpdate();
          }
          oDataSrc_.commit();

          // 推送信息
          int iPPCnt = 1;
          try {
            String sDateB = (new emisDate()).addDay(-2).toString(true);
            oDataSrc_.prepareStmt(" select ifnull(count(1),1) as CNT from Sale_order_h with(nolock) where SL_DATE = ? and S_NO = ? and FLS_NO = 'PP' and ifnull(WM_ORD_NO,'') != '' ");
            oDataSrc_.clearParameters();
            oDataSrc_.setString(1, sDateB);
            oDataSrc_.setString(2, S_NO_);
            oDataSrc_.prepareQuery();
            if (oDataSrc_.next()) {
              iPPCnt = oDataSrc_.getInt("CNT");
              iPPCnt = (iPPCnt == 0) ? 1 : iPPCnt;
            }
            emisMQTTPublish.getInstance().doPublish(context_, S_NO_ + "," + iPPCnt + "," + 0 + "," + String.valueOf((int) (System.currentTimeMillis() / 1000)));
          } catch (Exception ex) {
            iPPCnt = 1;
            oLogger_.error(ex, ex);
          }
          oLogger_.info("------------- doOrder mqtt end -------------");

          try {
            if (emisWxUtils.checkIsWechat(pfSource_)) {
              StringBuffer selOrder = new StringBuffer();
              selOrder.append(" select h.SL_KEY, h.ORDER_NO, dbo.emisCombDate(h.SL_DATE,'/') as SL_DATE, dbo.emisErosTime(h.SL_TIME) as SL_TIME, h.WC_ID \n ");
              selOrder.append(" , ifnull(wm.DS_NO,'') as DS_NO, ifnull(wm.ORDER_SEQNO,0) as ORDER_SEQNO, h.SL_TYPE\n ");
              selOrder.append(" from Sale_order_h h with(nolock) \n ");
              selOrder.append(" inner join Sale_order_wm wm with(nolock) on wm.SL_KEY = h.SL_KEY\n ");
              selOrder.append(" where h.SL_KEY = ? ");
              oSelSaleOrderH = oDataSrc_.prepareStmt(selOrder.toString());
              oSelSaleOrderH.clearParameters();
              oSelSaleOrderH.setString(1, SL_KEY);
              rs1 = oSelSaleOrderH.executeQuery();
              if (rs1.next()) {
                String remark = "";
                if (("0".equals(rs1.getString("SL_TYPE")) || "2".equals(rs1.getString("SL_TYPE"))) && !"0".equals(rs1.getString("ORDER_SEQNO"))) {
                  remark = "  取餐号:#" + rs1.getString("ORDER_SEQNO");
                } else if ("3".equals(rs1.getString("SL_TYPE"))) {
                  if ("0".equals(rs1.getString("ORDER_SEQNO"))) {
                    if (!"".equals(rs1.getString("DS_NO"))) {
                      remark = "  桌号:" + rs1.getString("DS_NO");
                    }
                  } else {
                    remark = "  取餐号:#" + rs1.getString("ORDER_SEQNO");
                  }
                }

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("TITLE", "订单状态变化提醒"); // 标题
                map.put("ORDER_NO", rs1.getString("ORDER_NO"));  // 订单号
                map.put("FLS_NO_NAME", "下单成功");  // 状态名称
                map.put("SL_DATE", rs1.getString("SL_DATE"));  // 处理日期
                map.put("SL_TIME", rs1.getString("SL_TIME"));  // 处理时间
                map.put("REMARK", "欢迎点餐。" + remark);  // 结束语
                emisWxOrderUtils.sendWxOrderMsgTempl(context_, rs1.getString("WC_ID"), map);
              }
            }
          } catch (Exception ex) {
            oLogger_.error(ex, ex);
          }
*/
        } catch (Exception ex) {
          oLogger_.error(ex, ex);
          oDataSrc_.rollback();
        } finally {
          if (rs1 != null) {
            try {
              rs1.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
            rs1 = null;
            if (oSelSaleOrderH != null) oDataSrc_.closePrepareStmt(oSelSaleOrderH);
            if (oupdPointVUpd != null) oDataSrc_.closePrepareStmt(oupdPointVUpd);
            if (oSelPointVUpd != null) oDataSrc_.closePrepareStmt(oSelPointVUpd);
            if (oUpdGiftToken != null) oDataSrc_.closePrepareStmt(oUpdGiftToken);
            if (oUpdBarcardAmt != null) oDataSrc_.closePrepareStmt(oUpdBarcardAmt);
            if (oInsSaleOrderCard != null) oDataSrc_.closePrepareStmt(oInsSaleOrderCard);
            if (oInsGiftLift != null) oDataSrc_.closePrepareStmt(oInsGiftLift);
            if (oSelBarcardAmt != null) oDataSrc_.closePrepareStmt(oSelBarcardAmt);
          }
        }
      }
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
    } finally {
      oDataSrc_.closePrepareStmt(oUpdSaleOrderH);
    }

    return bReturn;
  }

  /**
   * 检查购物车商品是否可售
   *
   * @return
   */
  private String checkPart(String S_NO, JSONArray partList) {
    String sReturn = "";

    if (partList == null || partList.size() == 0) {
      return "nodata";
    } else {
      HashMap<String, String> saleOutPart = new HashMap<String, String>();
      try {
        // TODO check sale_out
       /* String CHECK_PS_QTY = "N"; // 是否检查库存量
        oDataSrc_.executeQuery("select ifnull(CHECK_PS_QTY,'N') as CHECK_PS_QTY from WECHAT_ORDER_SETTING where MP_ID = '' ");
        if (oDataSrc_.next()) {
          CHECK_PS_QTY = oDataSrc_.getString("CHECK_PS_QTY");
        }
        oDataSrc_.prepareStmt(" select ps.P_NO, ifnull(p.P_PS_QTY,'') as P_PS_QTY, ps.WM_SALE_OUT_WECHAT from part_s ps " +
            "left join Part p on p.P_NO = ps.P_NO where ps.S_NO = ? and ( ps.WM_SALE_OUT_WECHAT = 'Y' or ps.PS_QTY <= 0 ) ");
        oDataSrc_.clearParameters();
        oDataSrc_.setString(1, S_NO);
        oDataSrc_.prepareQuery();
        while (oDataSrc_.next()) {
          if ("Y".equalsIgnoreCase(oDataSrc_.getString("WM_SALE_OUT_WECHAT"))) {
            saleOutPart.put(oDataSrc_.getString("P_NO"), oDataSrc_.getString("WM_SALE_OUT_WECHAT"));
          } else {
            if ("Y".equalsIgnoreCase(CHECK_PS_QTY) && "Y".equalsIgnoreCase(oDataSrc_.getString("P_PS_QTY"))) {
              saleOutPart.put(oDataSrc_.getString("P_NO"), oDataSrc_.getString("WM_SALE_OUT_WECHAT"));
            }
          }
        }*/
        // 没有停售商品, 直接返回
        if (saleOutPart.size() == 0) {
          return "ok";
        }

        StringBuffer outPart = new StringBuffer();
        int iOutPart = 0;
        for (int iParts = 0; iParts < partList.size(); iParts++) {
          JSONObject part = partList.getJSONObject(iParts);
          if (part.getDouble("selectorsQty") > 0) {
            if (saleOutPart.get(part.getString("pno")) != null) {
              if (iOutPart++ > 0) {
                outPart.append(", \n");
              }
              outPart.append("{");
              outPart.append(" \"pno\":\"").append(part.getString("pno")).append("\",\n");
              outPart.append(" \"name\":\"").append(emisBMUtils.escapeJson(part.getString("name"))).append("\"");
              outPart.append("}");
              iOutPart++;
            }
          }
        }
        if (outPart.length() > 0) {
          sReturn = outPart.toString();
        }

      } catch (Exception ex) {
        oLogger_.error(ex, ex);
      }
    }

    return "".equals(sReturn) ? "ok" : sReturn;
  }

  private String getJsonString(JSONObject jsonObj, String data) {
    String sReturn = "";
    try {
      if (jsonObj.has(data)) {
        sReturn = jsonObj.getString(data);
      }
    } catch(Exception ex) {
      sReturn = "";
      oLogger_.error("getJsonString error: " + data);
    }
    return sReturn;
  }

  private void print(String SL_KEY, String S_NO, String ID_NO, String SL_NO
      , String SL_DATE, String SL_TIME, String SL_TYPE, String deskId, String PAY_NAME, String PAY_PLAN_CODE
      , HashMap<String, String> seaNames) {

    try {
      emisPrinterData pData = new emisPrinterData();
      ArrayList<String> printData = new ArrayList<String>();

      int i80 = 36;
      String S_NAME_S = "";
      String S_TEL = "";
      String S_ADDR = "";

      oDataSrc_.prepareStmt(" select S_NAME_S, S_TEL, S_ADDR from Store where S_NO = ? ");
      oDataSrc_.clearParameters();
      oDataSrc_.setString(1, S_NO);
      oDataSrc_.prepareQuery();
      if (oDataSrc_.next()) {
        S_NAME_S = oDataSrc_.getString("S_NAME_S");
        S_TEL = oDataSrc_.getString("S_TEL");
        S_ADDR = oDataSrc_.getString("S_ADDR");
      }
      if (!"".equals(S_NAME_S)) {
        int len = emisString.lengthB(S_NAME_S);
        printData.add(emisString.lPadB(S_NAME_S, (i80 - len) /2 + len));
      }
      printData.add(" ");
      printData.add("+++");
      printData.add("取餐编号：" + ((deskId != null && !"".equals(deskId)) ? deskId : emisString.rightB(ID_NO, 2) + "-" + SL_NO));
      printData.add("点单时间：" + emisUtil.todayDateAD("-") + " " + emisUtil.todayTimeS(true));
      printData.add(ID_NO + "-" + SL_NO + " " + ("0".equals(SL_TYPE) ? "外带" : "堂食"));
      printData.add("品名                  数量      金额");
      printData.add("------------------------------------");
      // sale data


      HashMap<String, String> H_SYS_DISC = new HashMap<String, String>();
      HashMap<String, String> H_SSYS_DISC_AMT = new HashMap<String, String>();
      HashMap<String, HashMap<String, discTemp>> D_DISC = new HashMap<String, HashMap<String, discTemp>>();
      oDataSrc_.prepareStmt("select RECNO, DISC_CODE, DISC_NO, DISC_AMT, REASON  from Sale_order_dis where SL_KEY = ? order by RECNO, DISC_NO, DISC_SN");
      oDataSrc_.clearParameters();
      oDataSrc_.setString(1, SL_KEY);
      oDataSrc_.prepareQuery();
      while (oDataSrc_.next()) {
        String RECNO = oDataSrc_.getString("RECNO");
        String DISC_NO = oDataSrc_.getString("DISC_NO");
        if (D_DISC.get(RECNO) == null || D_DISC.get(RECNO).get(DISC_NO) == null) {
          discTemp tmp = new discTemp();
          tmp.setDISC_NO(DISC_NO);
          tmp.setDISC_AMT(oDataSrc_.getDouble("DISC_AMT"));
          tmp.setREASON(oDataSrc_.getString("REASON"));
          HashMap<String, discTemp> D_DISC_REC = new HashMap<String, discTemp>();
          D_DISC_REC.put(DISC_NO, tmp);
          D_DISC.put(RECNO, D_DISC_REC);
        } else {
          double discAmtOld = D_DISC.get(RECNO).get(DISC_NO).getDISC_AMT();
          D_DISC.get(RECNO).get(DISC_NO).setDISC_AMT(discAmtOld + oDataSrc_.getDouble("DISC_AMT"));
        }
      }

      oDataSrc_.prepareStmt("select RECNO, P_NO, ifnull(PK_RECNO,'') as PK_RECNO, P_NAME, SL_QTY, SL_TAXAMT + SL_DISC_AMT as SL_AMT, SL_TAXAMT, ifnull(SEA_NO,'') as SEA_NO from sale_order_d where SL_KEY = ? order by RECNO");
      oDataSrc_.clearParameters();
      oDataSrc_.setString(1, SL_KEY);
      oDataSrc_.prepareQuery();
      double slQty = 0;
      double slAmt = 0;
      while (oDataSrc_.next()) {
        if (!"".equals(oDataSrc_.getString("PK_RECNO")) && !oDataSrc_.getString("P_NO").equals(oDataSrc_.getString("PK_RECNO"))) {
          printData.add(emisString.rPadB("  " + oDataSrc_.getString("P_NAME"), 20)
              + emisString.lPadB(String.valueOf(oDataSrc_.getDouble("SL_QTY")), 6));
        } else {
          slQty += oDataSrc_.getDouble("SL_QTY");
          slAmt += oDataSrc_.getDouble("SL_TAXAMT");
          printData.add(emisString.rPadB(String.valueOf(oDataSrc_.getString("P_NAME")), 20)
              + emisString.lPadB(String.valueOf(oDataSrc_.getDouble("SL_QTY")), 6)
              + emisString.lPadB(String.valueOf(oDataSrc_.getDouble("SL_AMT")), 10));
        }
        if (!"".equals(oDataSrc_.getString("SEA_NO"))) {
          String seaName = "";
          String [] seaNos = oDataSrc_.getString("SEA_NO").split("/");
          for (String seaNo : seaNos) {
            if(seaNames.get(seaNo) != null && !"".equals(seaNames.get(seaNo))) {
              seaName += seaNames.get(seaNo) + "/";
            }
          }
          if (!"".equals(seaName)) printData.add("  +" + seaName);
        }

        if (D_DISC.get(oDataSrc_.getString("RECNO")) != null) {
          for (Object o : D_DISC.get(oDataSrc_.getString("RECNO")).entrySet()) {
            discTemp val = (discTemp)((Map.Entry) o).getValue();
            if (val.DISC_AMT != 0) {
              String discReason = val.getREASON();
              if (emisString.lengthB(discReason) <= 26) {
                printData.add(emisString.leftB(emisString.rPadB(discReason, 26), 26)
                    + emisString.lPadB(String.valueOf(formatAmt(val.DISC_AMT * -1)), 10));
              } else {
                printData.add(emisString.leftB(emisString.rPadB(discReason, 26), 26)
                    + emisString.lPadB(String.valueOf(formatAmt(val.DISC_AMT * -1)), 10));
                printData.add(emisString.leftB(emisString.lPadB(emisString.subStringB(discReason, 26), 26), 26));
              }
            }
          }
        }
      }
      slQty = formatAmt(slQty);
      slAmt = formatAmt(slAmt);
      printData.add("------------------------------------");
      printData.add("合计                " + emisString.lPadB(String.valueOf(slQty), 6) + emisString.lPadB(String.valueOf(slAmt), 10));
      printData.add(emisString.rPadB((PAY_NAME != null && !"".equals(PAY_NAME)) ? PAY_NAME : "支付", 26) + emisString.lPadB(String.valueOf(slAmt), 10));
      if (PAY_PLAN_CODE != null && !"".equals(PAY_PLAN_CODE)) {
        printData.add(emisString.lPadB(PAY_PLAN_CODE, i80));
      }
      printData.add("------------------------------------");
      if (!"".equals(S_TEL)) {
        printData.add(S_TEL);
      }
      if (!"".equals(S_ADDR)) {
        printData.add(S_ADDR);
      }

      /*for(int i = 0; i < printData.size(); i++) {
        oLogger_.info(printData.get(i));
      }*/

      print2File(SL_KEY, printData);

      /*pData.setPrintData(printData);
      emisPrinter print = new emisPrinter();
      print.doPrint(pData);*/

    } catch (Exception ex) {
      oLogger_.error(ex, ex);
    }
  }

  private void print2File(String SL_KEY, ArrayList<String> printData) {
      BufferedWriter _oWriter = null;
      String fileName;
      try {
        fileName = "d:\\emis\\bm\\print\\" + SL_KEY + ".txt";
        _oWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"));

        for(int i = 0; i < printData.size(); i++) {
          _oWriter.write(printData.get(i));
          _oWriter.newLine();
          oLogger_.info(printData.get(i));
        }
        _oWriter.flush();
      }
      catch (Exception e) {
        oLogger_.error(e.getMessage());
      } finally {
        if (_oWriter != null) {
          try {
            _oWriter.close();
          } catch (IOException e) {
          }
        }
      }
  }

  /**
   * 获取调味的中文名称
   * @param seaNos
   * @return
   */
  private HashMap<String, String> getSeaName(HashMap<String, String> seaNos) {
    String seaNo = "";
    for (Map.Entry<String, String> entry : seaNos.entrySet()) {
      seaNo += "'" + entry.getKey() + "',";
    }
    if (!"".equals(seaNo)) {
      seaNo += "''";
      // oLogger_.info("seaNo: " + seaNo);
      try {
        oDataSrc_.executeQuery("select SEA_ITEM_NO, SEA_ITEM_NAME from seasoning_d where SEA_ITEM_NO in (" + seaNo + ")");
        while (oDataSrc_.next()) {
          seaNos.put(oDataSrc_.getString("SEA_ITEM_NO"), oDataSrc_.getString("SEA_ITEM_NAME"));
        }
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }
    }
    return seaNos;
  }

  private class discTemp {
    private String DISC_NO;
    private double DISC_AMT;
    private String REASON;

    public String getDISC_NO() {
      return DISC_NO;
    }

    public void setDISC_NO(String DISC_NO) {
      this.DISC_NO = DISC_NO;
    }

    public double getDISC_AMT() {
      return DISC_AMT;
    }

    public void setDISC_AMT(double DISC_AMT) {
      this.DISC_AMT = DISC_AMT;
    }

    public String getREASON() {
      return REASON;
    }

    public void setREASON(String REASON) {
      this.REASON = REASON;
    }
  }

  /**
   * 保留两位小数
   * @param d  原值
   * @return 格式后的值
   */
  public static double formatAmt(double d) {
    return (double)Math.round(d*100)/100.0;
  }


}