/* $Header: /repository/src3/src/com/emis/db/emisProxyDesc.java,v 1.1.1.1 2005/10/14 12:42:04 andy Exp $
 *
 * 2004/05/25 Jerry: 增加時間欄位
 */
package com.emis.db;




/**
 *  此 Class 為方便描述 Proxy 內部使用情形
 *
 *  @see com.emis.db.emisConnectProxy
 */
public class emisProxyDesc {
  String sDescription_;
  emisStatementWrapper oStmt_=null;
  String sObjectId_;
  String sTime_;    long pid_;
/*
  public emisProxyDesc(String sObjectId, String sStr, emisStatementWrapper stmt) {
    sObjectId_ = sObjectId;
    sDescription_ = sStr;
    oStmt_ = stmt;
  }*/

  /**
   * 增加傳入時間的constructor.
   * @param sObjectId
   * @param sStr
   * @param stmt
   * @param sTime
   */
  public emisProxyDesc(String sObjectId, String sStr, emisStatementWrapper stmt, String sTime,long spid) {
    sObjectId_ = sObjectId;
    sDescription_ = sStr;
    oStmt_ = stmt;
    sTime_ = sTime;    pid_ = spid;
  }

  public String getId() {
    return sObjectId_;
  }

  public String getDescription() {
    return sDescription_;
  }

  public emisStatementWrapper getStatement() {
    return oStmt_;
  }

  public String getTime() {
    return sTime_;
  }    public long getSpid() {	  	  return pid_;	    }
}