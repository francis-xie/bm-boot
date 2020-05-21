package com.emis.webservices.service.bm.common;

import com.emis.db.emisProp;
import com.emis.util.emisUtil;
import com.emis.webservices.service.bm.utils.emisBMUtils;
import com.emis.webservices.service.emisAbstractService;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;

/**
 * 微信-公共资料查询
 */
public class emisBMCommonDatasImpl extends emisAbstractService {

  private final static String ACT_commonProp = "commonProp";  // 1.1.1 公共资料(系统参数)查询

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
    if (ACT_commonProp.equalsIgnoreCase(sAct)) {
      return doCommonProp(req);
    }
    return null;
  }

  /**
   * 公共资料(系统参数)查询
   *
   * @param req 入参(propKey)
   * @return 参数值
   */
  private String doCommonProp(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";
    StringBuffer sResult = new StringBuffer();
    try {
      // 1. 设置系统参数
      emisProp prop = emisProp.getInstance(context_);
      HashMap<String, String> props = new HashMap<String, String>();
      props.put("bmSNo", prop.get("S_NO"));    // 门店编号
      props.put("bmIdNo", prop.get("ID_NO"));  // 机台编号
      props.put("bmPosUrl", prop.get("SME_URL"));    // POS后台地址
      props.put("bmPosUId", prop.get("POS_USERID"));    // 登录账号
      props.put("bmPosUPwd", prop.get("POS_USERPWD"));    // 登录密码
      props.put("bmVersion", prop.get("BM_VERSION"));    // 版本号
      props.put("bmUpdateTime", prop.get("BM_UPDATE_TIME"));    // 更新时间

      // 2. 拼接返回的参数
      String propKey = emisUtil.parseString(req.getFirst("propKey"));
      if (!"".equals(propKey)) {
        String[] propKeys = propKey.split(",");
        if (propKeys.length > 0) {
          sResult.append("  \"result\":{\n");
        }
        for (int i = 0; i < propKeys.length; i++) {
          if (i > 0) {
            sResult.append(",\n");
          }
          sResult.append("  \"").append(propKeys[i]).append("\":\"").append(emisBMUtils.escapeJson(emisUtil.parseString(props.get(propKeys[i])))).append("\"");
        }
        if (propKeys.length > 0) {
          sResult.append("\n  }\n");
        }
      }
      code = "0";
      msg = "成功";
    } catch (Exception ex) {
      code = "900";
      msg = "查询异常，请重新查询！";
      sResult.setLength(0);
      oLogger_.error(ex, ex);
    }
    return "{\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg
        + (sResult.length() > 0 ? "\",\n" : "\"\n")
        + sResult.toString()
        + "}";
  }

}