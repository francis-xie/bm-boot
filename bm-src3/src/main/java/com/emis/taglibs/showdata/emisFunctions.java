/* $Id: emisFunctions.java 4 2015-05-27 08:13:47Z andy.he $

 *

 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.

 */

package com.emis.taglibs.showdata;



import com.emis.business.emisBusiness;

import com.emis.user.emisCertFactory;

import com.emis.user.emisUser;

import com.emis.user.emisUserButton;



import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.JspWriter;

import javax.servlet.jsp.tagext.TagSupport;



/**

 * 依使用者權限寫出功能按鈕.

 *

 * @author Jacky

 * @version 1.0

 * @version 2004/08/25 Jerry: refactor

 */

public class emisFunctions extends TagSupport {

  private String sSpace_;

  private String sList_;

  private String sKeys_;

  private String sTitle_;



  /**

   * start tag.

   * @return int

   */

  public int doStartTag() {

    try {



      JspWriter _out = pageContext.getOut();

      ServletContext _oContext = pageContext.getServletContext();

      HttpServletRequest _oRequest = (HttpServletRequest) pageContext.getRequest();



      if (sList_ != null) {

        //emisUserButton.writeListButton(_oContext, sList_, sSpace_, _out);
        // 2010/05/12 Joe 增加傳request的API，方便加語系條件過濾資料
        emisUserButton.writeListButton(_oContext, sList_, sSpace_, _out, _oRequest);

      } else {

        if (sKeys_ == null || "".equals(sKeys_)) {

          emisBusiness _oBusiness = (emisBusiness) pageContext.getAttribute("business");

          sKeys_ = _oBusiness.getRequest().getParameter("FUNC_KEYS");

        }



        if (sKeys_ != null) {

          emisUser _oUser = emisCertFactory.getUser(_oContext, _oRequest);

          emisUserButton.writeFuncButton(_oUser, sKeys_, _out);

        }

      }

    } catch (Exception e) {

      e.printStackTrace();

    }

    return (SKIP_BODY);

  }



  /**

   * attribute of showdata.tld

   * @return space

   */

  public String getspace() {

    return sSpace_;

  }



  /**

   * attribute of showdata.tld

   *

   * @param sSpace

   */

  public void setspace(String sSpace) {

    this.sSpace_ = sSpace;

  }



  /**

   * attribute of showdata.tld

   * @return list

   */



  public String getlist() {

    return sList_;

  }



  /**

   * attribute of showdata.tld

   * @param sList list

   */

  public void setlist(String sList) {

    this.sList_ = sList;

  }



  /**

   * attribute of showdata.tld

   * @return keys

   */

  public String getkeys() {

    return sKeys_;

  }



  /**

   * attribute of showdata.tld

   * @param sKeys FUNC_ID

   */

  public void setkeys(String sKeys) {

    this.sKeys_ = sKeys;

  }



  /**

   * attribute of showdata.tld

   * @return title of button

   */



  public String gettitle() {

    return sTitle_;

  }



  /**

   * attribute of showdata.tld

   * @param sTitle title of button

   */

  public void settitle(String sTitle) {

    this.sTitle_ = sTitle;

  }

}