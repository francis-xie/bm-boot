package com.emis.webservices.service;

import com.emis.util.emisLogger;
import com.emis.webservices.xml.util.BeanUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;

/**
 * 大屏点餐 API接口
 * User: harry
 * Date: 2019/06/20
 */
@Path("/bm")
public class emisWebServiceEntry {
  @Context
  protected ServletContext context_;

  protected Logger oLogger_ = null;
  private final String RET = "{\"code\":\"-1\",\"msg\":\"unknown\"}";

  public void initLogger() {
    try {
      if (oLogger_ == null)
        oLogger_ = emisLogger.getlog4j(context_, this.getClass().getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Call Service by POST METHOD
   * /XX/ws/bm/part/list (post: JSON Data)
   *
   * @param sType
   * @param sAction
   * @param request
   * @return
   */
  @POST
  @Path("/{type}/{action}")
  public Response post(@PathParam("type") String sType, @PathParam("action") String sAction, @Context Request request) {
    initLogger();

    String sRet = null;
    // 实现逻辑（建议独立的处理类）
    IEmisService service = (IEmisService) BeanUtil.getBean("bm", sType + "_" + sAction);
    if (service != null) {
      service.setServletContext(context_);
      service.setRequest(request);
      try {
        sRet = service.doAction();
      } catch (Exception e) {
        oLogger_.error(e.getMessage(), e);
      }
    }
    if (sRet == null || "".equals(sRet.trim())) {
      sRet = RET;
    }
    // 返回结果
    return Response.ok(sRet, MediaType.APPLICATION_JSON).build();
  }

  /**
   * Call Service by GET METHOD
   * /XX/ws/bm/part/list (post: JSON Data)
   *
   * @param sType
   * @param sAction
   * @param request
   * @return
   */
  @GET
  @Path("/{type}/{action}")
  public Response get(@PathParam("type") String sType, @PathParam("action") String sAction, @Context Request request) {
    // 返回结果
    return post(sType, sAction, request);
  }

}