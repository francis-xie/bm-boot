package com.emis.taglibs.sys;

import com.emis.util.emisUtil;



import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.JspWriter;

import javax.servlet.jsp.tagext.TagSupport;

import java.util.Hashtable;



/**

 * Title:

 * Description: 透過標籤取得Request內的資料

 * Copyright:    Copyright (c) 2001

 * Company: EMIS

 * @author emis Jacky

 * @version 1.0

 */



public class emisRequestTag extends TagSupport{

  private String sProperty_=null;

  public int doStartTag() {

    try {

      if (sProperty_ != null){

        ServletContext _oContext = pageContext.getServletContext();

        Hashtable _oHashReq = (Hashtable) pageContext.getAttribute("HASHREQ");

        if (_oHashReq == null){

           HttpServletRequest _oRequest = (HttpServletRequest) pageContext.getRequest();

           _oHashReq =  emisUtil.processRequest(_oRequest);

           pageContext.setAttribute("HASHREQ",_oHashReq);

        }

        JspWriter _out = pageContext.getOut();



          _out.print( (String) _oHashReq.get(sProperty_));

      }

    } catch(Exception e){

      System.out.println("Request Error");

    }

    return(SKIP_BODY);

  }

  public void setProperty(String sProperty){

    sProperty_ = sProperty;

  }

}