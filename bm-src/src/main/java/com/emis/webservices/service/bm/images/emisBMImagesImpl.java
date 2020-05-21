package com.emis.webservices.service.bm.images;

import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;
import com.emis.util.emisUtil;
import com.emis.webservices.service.bm.utils.emisBMUtils;
import com.emis.webservices.service.emisAbstractService;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;

/**
 * 大屏点餐-图片相关接口
 */
public class emisBMImagesImpl extends emisAbstractService {

  private final static String ACT_getIndexImgList = "getIndexImgList";  // 1.1 获取主页轮播图
  private final static String ACT_getOrderHeadImgList = "getOrderHeadImgList";  // 1.2 获取点餐页头部轮播图

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
    if (ACT_getIndexImgList.equalsIgnoreCase(sAct)) {
      return doGetIndexImgList(req);
    } else if (ACT_getOrderHeadImgList.equalsIgnoreCase(sAct)) {
      return doGetOrderHeadImgList(req);
    }
    return null;
  }

  /**
   * 获取主页轮播图
   *
   * @param req 入参
   * @return 轮播图列表
   */
  private String doGetIndexImgList(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";
    StringBuffer sResult = new StringBuffer();

    try {
      emisDirectory oRootDir_ = emisFileMgr.getInstance(context_).getDirectory("root").subDirectory("images").subDirectory("bm").subDirectory("setting");
      emisProp prop = emisProp.getInstance(context_);
      String S_NO = prop.get("S_NO");
      String today = emisUtil.todayDateAD();
      String LOCAL_URL = prop.get("LOCAL_URL");

      StringBuffer getImages = new StringBuffer();
      getImages.append(" select h.BSI_NO, d.SEQNO, d.RECNO, d.FILE_NAME, d.F_FILE\n ");
      getImages.append(" from Bm_Setting_Img_H h\n ");
      getImages.append(" inner join Bm_Setting_Img_D d on d.BSI_NO = h.BSI_NO\n ");
      getImages.append(" where h.S_NO = ? and h.FLS_NO = 'AP'\n ");
      getImages.append("   and d.B_DATE <= ? and d.E_DATE >= ? and d.BSI_TYPE = '1'\n ");
      getImages.append(" order by d.SEQNO, d.RECNO ");
      oDataSrc_.prepareStmt(getImages.toString());
      oDataSrc_.clearParameters();
      oDataSrc_.setString(1, S_NO);
      oDataSrc_.setString(2, today);
      oDataSrc_.setString(3, today);
      oDataSrc_.prepareQuery();
      int i = 0;
      while (oDataSrc_.next()) {
        if (i > 0) {
          sResult.append(",\n");
        }
        File oImg = new File(oRootDir_.getDirectory(), oDataSrc_.getString("F_FILE"));
        if (oImg.exists()) {
          sResult.append("{\"indexImgSeq\":").append(oDataSrc_.getString("SEQNO")).append(",\n");
          sResult.append(" \"indexImgName\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("FILE_NAME"))).append("\",\n");
          sResult.append("\"indexImgUrl\":\"");
          sResult.append(LOCAL_URL).append("/images/bm/setting/").append(oDataSrc_.getString("F_FILE")).append("\"}");
          i++;
        }
      }
      if (sResult.length() > 0) {
        code = "0";
        msg = "成功";
      } else {
        code = "100";
        msg = "无广告图";
      }
    } catch (Exception e) {
      code = "900";
      msg = "查询异常，请重试";
      oLogger_.error(e, e);
    }

    return "{\n\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\",\n"
        + " \"result\":[" + sResult.toString() + "]\n"
        + "}";
  }

  /**
   * 获取点餐页头部轮播图
   *
   * @param req 入参
   * @return 轮播图列表
   */
  private String doGetOrderHeadImgList(MultivaluedMap<String, String> req) {
    String code = "";
    String msg = "";
    StringBuffer sResult = new StringBuffer();

    try {
      emisDirectory oRootDir_ = emisFileMgr.getInstance(context_).getDirectory("root").subDirectory("images").subDirectory("bm").subDirectory("setting");
      emisProp prop = emisProp.getInstance(context_);
      String S_NO = prop.get("S_NO");
      String today = emisUtil.todayDateAD();
      String LOCAL_URL = prop.get("LOCAL_URL");

      StringBuffer getImages = new StringBuffer();
      getImages.append(" select h.BSI_NO, d.SEQNO, d.RECNO, d.FILE_NAME, d.F_FILE\n ");
      getImages.append(" from Bm_Setting_Img_H h\n ");
      getImages.append(" inner join Bm_Setting_Img_D d on d.BSI_NO = h.BSI_NO\n ");
      getImages.append(" where h.S_NO = ? and h.FLS_NO = 'AP'\n ");
      getImages.append("   and d.B_DATE <= ? and d.E_DATE >= ? and d.BSI_TYPE = '2'\n ");
      getImages.append(" order by d.SEQNO, d.RECNO ");
      oDataSrc_.prepareStmt(getImages.toString());
      oDataSrc_.clearParameters();
      oDataSrc_.setString(1, S_NO);
      oDataSrc_.setString(2, today);
      oDataSrc_.setString(3, today);
      oDataSrc_.prepareQuery();
      int i = 0;
      while (oDataSrc_.next()) {
        if (i > 0) {
          sResult.append(",\n");
        }
        File oImg = new File(oRootDir_.getDirectory(), oDataSrc_.getString("F_FILE"));
        if (oImg.exists()) {
          sResult.append("{\"ordImgSeq\":").append(oDataSrc_.getString("SEQNO")).append(",\n");
          sResult.append(" \"ordImgName\":\"").append(emisBMUtils.escapeJson(oDataSrc_.getString("FILE_NAME"))).append("\",\n");
          sResult.append("\"ordImgUrl\":\"");
          sResult.append(LOCAL_URL).append("/images/bm/setting/").append(oDataSrc_.getString("F_FILE")).append("\"}");
          i++;
        }
      }
      if (sResult.length() > 0) {
        code = "0";
        msg = "成功";
      } else {
        code = "100";
        msg = "无广告图";
      }
    } catch (Exception e) {
      code = "900";
      msg = "查询异常，请重试";
      oLogger_.error(e, e);
    }

    return "{\n\"code\":\"" + code + "\",\n"
        + " \"msg\":\"" + msg + "\",\n"
        + " \"result\":[" + sResult.toString() + "]\n"
        + "}";
  }

}