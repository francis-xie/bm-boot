package com.emis.webservices.service.bm.syndata;

import com.emis.bm.synPosData.emisBMSynDataImages;
import com.emis.bm.synPosData.emisBMSynDataPart;
import com.emis.bm.synPosData.emisBMSynDataPromote;
import com.emis.bm.synPosData.emisBMSynDataStore;
import com.emis.db.emisProp;
import com.emis.schedule.emisScheduleMgr;
import com.emis.schedule.emisTask;
import com.emis.util.emisUtil;
import com.emis.webservices.service.emisAbstractService;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * 大屏点餐-同步后台数据接口
 */
public class emisBMSynDataImpl extends emisAbstractService {

  private final static String ACT_synPartData = "synPartData";  // 1.90 同步基础数据
  private final static String ACT_checkLogin = "checkLogin";  // 1.91 登录身份检查
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
    if (ACT_synPartData.equalsIgnoreCase(sAct)) {
      return doSynPartData(req);
    } else if (ACT_checkLogin.equalsIgnoreCase(sAct)) {
      return doCheckLogin(req);
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
  private String doSynPartData(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";

    try {
      emisBMSynDataStore syn0 = new emisBMSynDataStore(context_);
      syn0.synStore();

      emisBMSynDataPart syn = new emisBMSynDataPart(context_);
      syn.synAllPart();

      emisBMSynDataImages syn2 = new emisBMSynDataImages(context_);
      syn2.synSettingImg();

      emisBMSynDataPromote syn3 = new emisBMSynDataPromote(context_);
      syn3.synPromote();
      syn3.synSaleTime();

      code = "0";
      msg = "成功";
    } catch (Exception ex) {
      code = "900";
      msg = "处理异常,请重试";
      oLogger_.error(ex, ex);
    }

    return "{\"code\":\"" + code + "\", \"msg\":\"" + msg + "\"}";
  }

  /**
   * 登录检查
   *
   * @param req request参数
   * @return
   * @throws Exception
   */
  private String doCheckLogin(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";

    String sPosUrl = emisUtil.parseString(req.getFirst("sPosUrl"));
    String sNo = emisUtil.parseString(req.getFirst("sNo"));
    String idNo = emisUtil.parseString(req.getFirst("idNo"));
    String uId = emisUtil.parseString(req.getFirst("uId"));
    String uPwd = emisUtil.parseString(req.getFirst("uPwd"));

    try {
      emisProp prop = emisProp.getInstance(context_);
      String SME_URL = prop.get("SME_URL");
      String S_NO = prop.get("S_NO");
      String ID_NO = prop.get("ID_NO");
      String POS_USERPWD = prop.get("POS_USERPWD");
      String POS_USERID = prop.get("POS_USERID");
      String BM_VERSION = prop.get("BM_VERSION");

      HttpClient _oClient = null;
      int _iStatus = 0;
      Response resp = null;
      String respBody = "";
      try {
        _oClient = new HttpClient();
        _oClient.setConnectionTimeout(180000);
        _oClient.setTimeout(180000);
        PostMethod method = new PostMethod(sPosUrl + "/ws/wechatV3/bm/checkLogin");
        method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
        method.addParameter("sNo", sNo);
        method.addParameter("idNo", idNo);
        method.addParameter("uId", uId);
        method.addParameter("uPwd", uPwd);
        method.addParameter("bmVersion", BM_VERSION);

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
        return "{\"code\":\"800\",\"msg\":\"后台连线异常，请重试。\"}";
      } else {
        if (!emisUtil.isJSON(respBody.trim())) {
          return "{\"code\":\"801\",\"msg\":\"后台连线异常，请重试。\"}";
        } else {
          posResp = JSONObject.fromObject(respBody);
          if (posResp == null || posResp.isEmpty()) {
            return "{\"code\":\"802\",\"msg\":\"后台连线异常，请重试。\"}";
          } else {
            String posResp_code = getJsonString(posResp, "code");
            if (!"0".equals(posResp_code) && !"00".equals(posResp_code)) {
              return respBody;
            }
          }
        }
      }

      boolean firstLogin = false;
      if ("".equals(SME_URL) || "".equals(S_NO) || "".equals(ID_NO) || "".equals(POS_USERPWD) || "".equals(POS_USERID)) {
        // 系统参数不完整，不执行后续动作。
        firstLogin = true;
      }

      updEmisprop(sPosUrl, sNo, idNo, uId, uPwd);

      emisProp.reload(context_);

      if (firstLogin) {
        try {
          // 首次登录成功，立即调用下载排程
          if (emisScheduleMgr.getInstance(context_).isExists("emisBMDownload")) {
            ClassLoader oClassLoader = Thread.currentThread().getContextClassLoader();
            Class obj = oClassLoader.loadClass("com.emis.schedule.epos.bm.emisBMDownload");
            emisTask task = (emisTask) (obj).newInstance();
            task.setName("emisBMDownload");
            task.setContext(context_);
            Thread t = new Thread(task);
            t.start();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      code = "0";
      msg = "成功";
    } catch (Exception ex) {
      code = "900";
      msg = "处理异常,请重试";
      oLogger_.error(ex, ex);
    }

    return "{\"code\":\"" + code + "\", \"msg\":\"" + msg + "\"}";
  }

  private String getJsonString(JSONObject jsonObj, String data) {
    String sReturn = "";
    try {
      if (jsonObj.has(data)) {
        sReturn = jsonObj.getString(data);
      }
    } catch (Exception ex) {
      sReturn = "";
      ex.printStackTrace();
    }
    return sReturn;
  }

  private boolean updEmisprop(String sPosUrl, String sNo, String idNo, String uId, String uPwd) {

    PreparedStatement updEmispropStmt = null;
    PreparedStatement insEmispropStmt = null;
    try {
      String today = emisUtil.todayDateAD();
      updEmispropStmt = oDataSrc_.prepareStmt("update emisprop set VALUE = ?, UPD_DATE = ? where NAME = ?");
      insEmispropStmt = oDataSrc_.prepareStmt("insert into emisprop (NAME, VALUE, UPD_DATE) values (?, ?, ?)");

      // SME_URL
      try {
        updEmispropStmt.clearParameters();
        updEmispropStmt.setString(1, sPosUrl);
        updEmispropStmt.setString(2, today);
        updEmispropStmt.setString(3, "SME_URL");
        if (updEmispropStmt.executeUpdate() <= 0) {
          insEmispropStmt.clearParameters();
          insEmispropStmt.setString(1, "SME_URL");
          insEmispropStmt.setString(2, sPosUrl);
          insEmispropStmt.setString(3, today);
          insEmispropStmt.executeUpdate();
        }
        oDataSrc_.commit();
      } catch (Exception ex) {
        oLogger_.error("update SME_URL error");
      }

      // S_NO
      try {
        updEmispropStmt.clearParameters();
        updEmispropStmt.setString(1, sNo);
        updEmispropStmt.setString(2, today);
        updEmispropStmt.setString(3, "S_NO");
        if (updEmispropStmt.executeUpdate() <= 0) {
          insEmispropStmt.clearParameters();
          insEmispropStmt.setString(1, "S_NO");
          insEmispropStmt.setString(2, sNo);
          insEmispropStmt.setString(3, today);
          insEmispropStmt.executeUpdate();
        }
        oDataSrc_.commit();
      } catch (Exception ex) {
        oLogger_.error("update SME_URL error");
      }

      // ID_NO
      try {
        updEmispropStmt.clearParameters();
        updEmispropStmt.setString(1, idNo);
        updEmispropStmt.setString(2, today);
        updEmispropStmt.setString(3, "ID_NO");
        if (updEmispropStmt.executeUpdate() <= 0) {
          insEmispropStmt.clearParameters();
          insEmispropStmt.setString(1, "ID_NO");
          insEmispropStmt.setString(2, idNo);
          insEmispropStmt.setString(3, today);
          insEmispropStmt.executeUpdate();
        }
        oDataSrc_.commit();
      } catch (Exception ex) {
        oLogger_.error("update SME_URL error");
      }

      // POS_USERID
      try {
        updEmispropStmt.clearParameters();
        updEmispropStmt.setString(1, uId);
        updEmispropStmt.setString(2, today);
        updEmispropStmt.setString(3, "POS_USERID");
        if (updEmispropStmt.executeUpdate() <= 0) {
          insEmispropStmt.clearParameters();
          insEmispropStmt.setString(1, "POS_USERID");
          insEmispropStmt.setString(2, uId);
          insEmispropStmt.setString(3, today);
          insEmispropStmt.executeUpdate();
        }
        oDataSrc_.commit();
      } catch (Exception ex) {
        oLogger_.error("update SME_URL error");
      }

      // POS_USERPWD
      try {
        updEmispropStmt.clearParameters();
        updEmispropStmt.setString(1, uPwd);
        updEmispropStmt.setString(2, today);
        updEmispropStmt.setString(3, "POS_USERPWD");
        if (updEmispropStmt.executeUpdate() <= 0) {
          insEmispropStmt.clearParameters();
          insEmispropStmt.setString(1, "POS_USERPWD");
          insEmispropStmt.setString(2, uPwd);
          insEmispropStmt.setString(3, today);
          insEmispropStmt.executeUpdate();
        }
        oDataSrc_.commit();
      } catch (Exception ex) {
        oLogger_.error("update SME_URL error");
      }
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
    } finally {
      if (updEmispropStmt != null) {
        oDataSrc_.closePrepareStmt(updEmispropStmt);
        updEmispropStmt = null;
      }
      if (insEmispropStmt != null) {
        oDataSrc_.closePrepareStmt(insEmispropStmt);
        insEmispropStmt = null;
      }
    }

    return true;
  }

}