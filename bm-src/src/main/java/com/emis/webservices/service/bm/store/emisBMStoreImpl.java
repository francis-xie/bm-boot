package com.emis.webservices.service.bm.store;

import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;
import com.emis.util.emisUtil;
import com.emis.webservices.service.bm.utils.emisBMUtils;
import com.emis.webservices.service.emisAbstractService;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;

/**
 * 大屏点餐-门店相关接口
 */
public class emisBMStoreImpl extends emisAbstractService {

  private final static String ACT_getStoreInfo = "getStoreInfo";  // 1.3 获取门店基本信息

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
    if (ACT_getStoreInfo.equalsIgnoreCase(sAct)) {
      return doGetIndexImgList(req);
    }
    return null;
  }

  /**
   * 获取门店基本信息
   *
   * @param req 入参
   * @return 轮播图列表
   */
  private String doGetIndexImgList(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";
    StringBuffer sResult = new StringBuffer();
    try {
      emisDirectory oRootDir_ = emisFileMgr.getInstance(context_).getDirectory("root").subDirectory("images").subDirectory("bm");
      emisProp prop = emisProp.getInstance(context_);
      String S_NO = prop.get("S_NO");
      String LOCAL_URL = prop.get("LOCAL_URL");

      oDataSrc_.prepareStmt("select S_NO, S_NAME, S_NAME_S, S_ADDR, S_TEL from Store where S_NO = ?");
      oDataSrc_.clearParameters();
      oDataSrc_.setString(1, S_NO);
      oDataSrc_.prepareQuery();
      if (oDataSrc_.next()) {
        sResult.append(" \"sNo\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("S_NO"))).append("\",\n");
        sResult.append(" \"sName\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("S_NAME"))).append("\",\n");
        sResult.append(" \"sNameS\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("S_NAME_S"))).append("\",\n");
        sResult.append(" \"sAddr\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("S_ADDR"))).append("\",\n");
        sResult.append(" \"sTel\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("S_TEL"))).append("\",\n");
        sResult.append(" \"sIcon\":\"");
        File oImg = new File(oRootDir_.getDirectory(), "compIcon.jpg");
        if (oImg.exists()) {
          sResult.append(LOCAL_URL).append("/images/bm/compIcon.jpg");
        }
        sResult.append("\" ");
      }
      if (sResult.length() > 0) {
        code = "0";
        msg = "成功";
      } else {
        code = "100";
        msg = "查无资料";
      }
    } catch (Exception e) {
      code = "900";
      msg = "查询异常，请重试";
      oLogger_.error(e, e);
    }

    return "{\n\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\",\n"
        + " \"result\":{" + sResult.toString() + "}\n"
        + "}";
  }

}