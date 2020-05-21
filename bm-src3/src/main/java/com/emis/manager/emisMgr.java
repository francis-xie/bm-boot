//Source file: d:/pos_framework/classes/com/emis/manager/Mgr.java

package com.emis.manager;



/**
 * 定義了所有管理介面
 * 尚未規劃完畢,亦未實作
 */
public interface emisMgr //extends Remote
{
    void enableDebug(boolean debug) throws Exception;
    void stop() throws Exception;
    void setProperty(int propertyID,Object oValue) throws Exception;
    String getMgrVersion() throws Exception;
    String getServiceName() throws Exception;
}
