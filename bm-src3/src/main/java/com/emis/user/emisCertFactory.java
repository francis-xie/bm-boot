/*

 * $Header: /repository/src3/src/com/emis/user/emisCertFactory.java,v 1.1.1.1 2005/10/14 12:43:11 andy Exp $

 *

 * Copyright (c) EMIS Corp.

 */

package com.emis.user;



import com.emis.cipher.emisCipherMgr;

import com.emis.trace.emisError;

import com.emis.trace.emisTracer;
import com.emis.util.emisLangRes;


import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpSession;

import java.util.Properties;



/**

 * 提供系統認證部份的功能

 * 包含了登入系統,和拿取 emisUser 物件 (from session or cookie),

 * 雖然實作可能有好幾種,但一個 application 只能 register

 * 一個 emisCert Object

 */

public class emisCertFactory

{

    public static final String EMIS_USER_BIND_NAME="com.emis";



    private emisCertFactory() { /* null constructor*/ }

    

    /**

     *  emisCertFactory.createCert()由emisServerImpl._initCert()叫用.

     *  oProp即/resin/xxx.cfg, 其內的com.emis.cert定義了系統的認證種類.

     *  you will never need to call it.

     *  it will not do anything if you call it since system has startuped.

     */

    public static emisCert createCert(ServletContext oContext,Properties oProp) throws Exception

    {

        emisCert _oCert = (emisCert) oContext.getAttribute(emisCert.STR_EMIS_CERT);

        if( _oCert != null )

        {

            emisTracer.get(oContext).sysError(null,emisError.ERR_SVROBJ_DUPLICATE,"emisCertFactory");

        }

        String _sCertMethod = oProp.getProperty("com.emis.cert","httpsession");



        if( _sCertMethod.equalsIgnoreCase("httpsession") )

        {

            /** 會將_oCert存入oContex的STR_EMIS_CERT attribute中 */

            _oCert = new emisHttpSessionCert(oContext,oProp);

            return _oCert;

        }



        if( _sCertMethod.equalsIgnoreCase("cookie") )

        {

            // not support yet

        }

        return null;

    } // creatreCert()



    /**

     *  拿取現在正在運作的 emisCert 物件(由emisHttpSessionCert的constructor

     *  setAttribute到ServletContext物件中)

     */

    public static emisCert getCertificate(ServletContext oContext) throws Exception

    {

        emisCert _oCert = (emisCert) oContext.getAttribute(emisCert.STR_EMIS_CERT);

        if(_oCert == null )

        {

            emisTracer.get(oContext).sysError(null,emisError.ERR_SVROBJ_NOT_BIND,"emisCertFactory");

        }

        return _oCert;

    }



    /**

     *  取得<code>emisUserMonitor</code>物件, 可以觀查系統目前的登入狀況 , 

     *  (if a session has been lost , it will not in Monitor)

     */

    public static synchronized emisUserMonitor getUserMonitor(ServletContext oContext) throws Exception

    {

        return getCertificate(oContext).getUserMonitor();

    }



    /**

     *  取得目前的使用者物件; 使用於網頁開頭的Scriptlet上來確認已登入<code>

     *  emisUser _oUser = emisCertFactory.getUser(application,request);

     *  </code>

     */

    public static synchronized emisUser getUser(ServletContext oContext,HttpServletRequest oRequest) throws Exception

    {

        return getCertificate(oContext).getUser(oRequest);

    }



    /**

     *  to login with password , and create session with servlet API

     *  後端是 cookie 還是 session, 是看系統設定,目前 cookie 的並

     *  沒有寫 , 不過有些 Session implement 其實是用 cookie

     */

    public static synchronized emisUser login(ServletContext oContext,

    HttpServletRequest oRequest,String sStoreNo,String sUserName,String sPassWord)

    throws Exception

    {

        // 將前端傳過來的字串加密

        sPassWord = emisCipherMgr.getInstance(oContext).cipherUserData(sPassWord);

        //-emisTracer.get(oContext).warning("password=" + sPassWord);

        return getCertificate(oContext).login(oRequest,sStoreNo,null,sUserName,sPassWord);

    }

    // robert 2010/02/08 , 新增,用來檢查 user/password, 但是不做登入 (binding user object) 的動作
    public static synchronized boolean checkLogin(ServletContext oContext,
    	    HttpServletRequest oRequest,String sStoreNo,String sUserName,String sPassWord)
    	    throws Exception
    {

        // 將前端傳過來的字串加密
        sPassWord = emisCipherMgr.getInstance(oContext).cipherUserData(sPassWord);
        return getCertificate(oContext).checkLogin(oRequest,sStoreNo,null,sUserName,sPassWord);

    }


    public static synchronized emisUser login(ServletContext oContext,

    HttpServletRequest oRequest,String sUserName,String sPassWord)

    throws Exception

    {

        return login(oContext,oRequest,(String)null,sUserName,sPassWord);

    }

    

    /**

     * 除了USERID與Password外還要判斷公司別或使用者類別

     * @author Jerry on 2001/11/09

     */

    public static synchronized emisUser login(ServletContext oContext,

    HttpServletRequest oRequest,String sStoreNo,String sExtraInfo,

    String sUserName,String sPassWord)throws Exception

    {

        // 將前端傳過來的字串加密

        sPassWord = emisCipherMgr.getInstance(oContext).cipherUserData(sPassWord);

        return getCertificate(oContext).login(oRequest,sStoreNo,sExtraInfo,sUserName,sPassWord);

    }



    public static synchronized void logout(ServletContext oContext,HttpSession oSession)

    {

        emisTracer _oTr = emisTracer.get(oContext);

        Object _oUserObject =  oSession.getAttribute(EMIS_USER_BIND_NAME);

        if( _oUserObject == null )

        {

          _oTr.warning("logout session has no User Object bind with");

        }



        if( _oUserObject instanceof emisUser)

        {

          emisUser _oUser = (emisUser) _oUserObject;
          // 2010/05/12 Joe 登出時移除先前記錄的當前User選定的語言
          emisLangRes.removeUserLang(_oUser.getID());
          _oTr.info(_oUser.getID() + " logout");

        } else {

          _oTr.warning("logout session bind user object is not emisUser");

        }

        oSession.invalidate();

    }

}