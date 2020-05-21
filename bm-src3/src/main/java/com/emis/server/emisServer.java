



package com.emis.server;



import java.rmi.Remote;

import java.rmi.RemoteException;

import java.util.Properties;



/**

 *  定義 Server 物件所提供的功能

 */

public interface emisServer extends Remote

{

    public static final String STR_EMIS_SERVER = "com.emis.server";

    Properties getProperties() throws RemoteException;

    void enableDebug(boolean _debug) throws Exception;

    Object getMgrObject(String sMgrName);

    /**

     * 設定在 epos.cfg 中

     * server.bindname 的值

     */

    String getServerName();

    void startup() throws Exception;

    void shutdown() throws Exception;



}
