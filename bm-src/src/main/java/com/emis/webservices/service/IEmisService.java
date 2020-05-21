package com.emis.webservices.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;

/**
 * 微信会员服务接口
 * 2016/11/11 francis.xie modify 需求 #36444 [外卖平台对接]-饿了么外卖接口
 */
public interface IEmisService {
  /**
   * 设置请求对象，用于取参数
   *
   * @param request
   */
  public void setRequest(Request request);

  /**
   * 设置请求对象，用于取参数
   *
   * @param request
   */
  public void setHttpServletRequest(HttpServletRequest request);

  /**
   * 设置上下文，用于实例化数据源等
   *
   * @param context
   */
  public void setServletContext(ServletContext context);

  /**
   * 响应请求，实现相关业务逻辑
   *
   * @return
   */
  public String doAction() throws Exception;

}
