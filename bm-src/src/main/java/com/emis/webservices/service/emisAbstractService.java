package com.emis.webservices.service;

import com.emis.business.emisBusinessResourceBean;
import com.emis.business.emisHttpServletRequest;
import com.emis.db.emisDb;
import com.emis.file.emisFileMgr;
import com.emis.util.emisLogger;
import com.sun.jersey.spi.container.ContainerRequest;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import java.sql.SQLException;

/**
 * 微信会员服务抽象类
 * 2016/11/11 francis.xie modify 需求 #36444 [外卖平台对接]-饿了么外卖接口
 */
public abstract class emisAbstractService implements IEmisService {
  protected emisBusinessResourceBean resourceBean_ = null;
  protected ServletContext context_ = null;
  protected Request request_ = null;
  protected HttpServletRequest httpServletRequest_ = null;
  protected Logger oLogger_ = null;
  protected emisDb oDataSrc_ = null;

  public void setRequest(Request request) {
    this.request_ = request;
  }

  public void setHttpServletRequest(HttpServletRequest request) {
    this.httpServletRequest_ = request;
  }

  public void setServletContext(ServletContext context) {
    this.context_ = context;
  }

  /**
   * 处理Request请求（默认转码）
   *
   * @return
   */
  protected MultivaluedMap<String, String> parseRequest() {
    return parseRequest(true);
  }

  /**
   * 处理Request请求
   *
   * @param decode 是否转码
   * @return
   */
  protected MultivaluedMap<String, String> parseRequest(boolean decode) {
    if ("POST".equals(request_.getMethod())) {
      return ((ContainerRequest) request_).getFormParameters();
    } else {
      return ((ContainerRequest) request_).getQueryParameters(decode);
    }
  }

  /**
   * 取得Db 的商务逻辑.
   *
   * @return resourceBean_
   */
  public final emisBusinessResourceBean getResourceBean() {
    if (resourceBean_ == null) {
      try {
        oLogger_ = emisLogger.getlog4j(context_, this.getClass().getName());
        oDataSrc_ = emisDb.getInstance(context_);
        oDataSrc_.setAutoCommit(false);
        resourceBean_ = new emisBusinessResourceBean();
        resourceBean_.setEmisDb(oDataSrc_);
        resourceBean_.setFileMgr(emisFileMgr.getInstance(context_));
        resourceBean_.setEmisHttpServletRequest(new emisHttpServletRequest());
        resourceBean_.setServletContext(context_);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return resourceBean_;
  }

  public String doAction() throws Exception {
    String sRet = "";
    try {
      getResourceBean();
      oLogger_.info("-----------Service Running");

      // 实作中继承并override该Method
      sRet = postAction();
      oLogger_.info(sRet);

      oLogger_.info("-----------Service End");
      this.oDataSrc_.commit();
    } catch (Exception e) {
      oLogger_.error(e.getMessage(), e);
      try {
        oDataSrc_.rollback();
      } catch (Exception er) {
        er.printStackTrace();
      }
    } finally {
      resourceBean_ = null;
      if (oDataSrc_ != null)
        this.oDataSrc_.close();
    }
    return sRet;
  }

  protected abstract String postAction() throws SQLException, Exception;
}
