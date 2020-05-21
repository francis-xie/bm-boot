/*
 * $Header: /repository/src3/src/com/emis/user/emisCert.java,v 1.1.1.1 2005/10/14 12:43:11 andy Exp $
 *
 *  20040513 abel add  extends java.io.Serializable  session 需要 Serializable
 *
 * Copyright (c) EMIS Corp.
 */
package com.emis.user;

import javax.servlet.http.HttpServletRequest;

/**
 * 定義認證 Certification 介面所應該有的功能
 */
public interface emisCert extends java.io.Serializable
{
    public static final String STR_EMIS_CERT = "com.emis.user.cert";

    public emisUser getUser(HttpServletRequest request) throws Exception;

    //emisUser login(HttpServletRequest request,String sStoreNo,String userid,String password) throws Exception;
    //- Jerry added the next login() on 2001/11/09
    public emisUser login(HttpServletRequest request,String sStoreNo,String sExtraInfo,String userid,String password) throws Exception;        // robert 2010/02/08 新增 check user/password but not do login action    public boolean checkLogin(HttpServletRequest request,String sStoreNo,String sExtraInfo,String userid,String password) throws Exception;

    public emisUserMonitor getUserMonitor();
}