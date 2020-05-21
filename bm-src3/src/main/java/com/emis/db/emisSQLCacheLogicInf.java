package com.emis.db;

import com.emis.user.emisUser;

import javax.servlet.ServletContext;
/**
 * SQL Cache 的特別 logic 的 interface
 * 因為是 by 專案會有特例,譬如麗嬰房的
 * STORE 需要看登入者的身份產生不同的結果
 * 所以要定一個 Interface ,再個別去實作
 * 在系統設定檔中設定實作的類別
 * emis.sqlcache.logic=com.emis.db.les.emisLesSQLCacheLogic
 */
public interface emisSQLCacheLogicInf
{
  /**
   * sSQLName 統一傳入大寫
   * 傳回特殊的 SQL Cache,
   * 不然回傳 null
   */
  String getSQL(ServletContext application,String sSQLName,emisUser oUser) throws Exception;
}