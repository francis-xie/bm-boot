/* $Id: emisDb.java 535 2015-08-14 09:09:00Z andy.he $

 *

 * Copyright (c) EMIS Corp. All Rights Reserved.

 */

package com.emis.db;



import com.emis.business.emisBusiness;

import com.emis.spool.emisComplexSpool;

import com.emis.spool.emisSpoolSnapShot;

import com.emis.trace.emisError;

import com.emis.trace.emisTracer;

import com.emis.util.emisChinese;
import com.emis.util.emisUtil;

import com.emis.util.emisLogger;

import org.apache.log4j.Logger;

import org.apache.log4j.PropertyConfigurator;



import javax.servlet.ServletContext;

import java.io.File;

import java.io.IOException;

import java.math.BigDecimal;

import java.sql.*;

import java.util.*;

import javax.servlet.http.*;

import javax.servlet.*;

/**

 *   和資料庫連結的 Utility Class

 *

 *   this class can't be instanciate by constructor

 *   please use

 *

 *   emisDb.getInstance( 'database name');

 *

 *   一般來說,資料庫的字元集和系統的字元集是一樣的,

 *   但在不一樣的字元集的情型,emisDb 會自動作轉碼的動作,

 *   (除了 getObject() 並不會自動作轉碼)

 *

 *   emisDb 的架構就是 Keep 住一個 Connection

 *   由此產生 Statement,PrepareStatement,CallableStatement(皆可以是多個),

 *   由執行 SQL 產生一個 current ResultSet , 所以連續的

 *   查詢會使 current ResultSet 變成最後一個查詢的 ResultSet,

 *   可以經由 setCurrentResultSet 來同時使用兩個以上的查詢結果,

 *   在 emisDb 內部,同一個時間只能操作一個 Statement,PrepareStatement,

 *   或 CallableStatement, 除了 Statement 只會產生一個外, PreparedStatement

 *   和 CallableStatement 在你每次呼叫 PrepareStmt(String sSQL) 和

 *   CallableStmt(String sSQL) 時,會回傳一個新的 Object.

 *   並將之加入emisDb 內部的 Resource List

 *   (when you close emisDb , it will automaticly free it),

 *   在同一時間內你只能操作一個 , 如果要同時操作兩個以上, 要用

 *   setCurrentPrepareStmt 和

 *   setCurrentCallableStmt

 *   使用範例請參考 epos programming guide

 *

 * @author Robert

 * @version $Revision: 75532 $

 * 2003/12/12 Jerry Revision 1.3: 加入PreparedStatement的addBatch()與executeBatch()

 * 2004/05/25 Jerry Revision: 1.4: 將使用物件的類別名放入description以利emisDbMonitor顯示.

 * 2004/06/22  abel Revision: 1.5: 透過cfg的設定來決定是否要產生emisDbCheckPool.log 以利查出哪一支程式一直hold 住connection

 * 2004/06/23  abel Revision: 1.7: 效能考量取消動態載入log 設定,並用log4j 產生log 檔,

 *    請注意當設定正確後,若沒有wwwroot\專案\web-inf\emisDb.properties也不會產生任何log 檔

 * 2004/06/28 Jerry 1.8: 將main拿掉改用JUnit測試; Refactor logConnection

 * 2004/07/03 Jerry: 增加getMetaData傳回DatabaseMetaData
 *
 * Track+[14915] dana.gao 2010/05/19 改善連線池,增加未關閉連接查找功能,并記錄log

 *     需在resin的.cfg文件中加入emis.db.log.enabled=true設定才能記錄log


 *  2014/02/19 Robert , logConnection is too frequency ,sometimes can cause memoy problem,
 *                       and hard to find out real problem , remove it.
 *             
 */

public class emisDb {

/*-------------------------all variables-------------------------*/

  // 是否須從新讀取emisdblog 的設定

  private static boolean isGenDbLog_ = true;

  private static boolean needCheckConnection_ = true;

  //- private static String sCheckTransaction_ = null;

  private static int iCheckPoolCount_ = 9999;

  //private static Logger oLogger_;



  private emisConnectProxy oEmisProxy_;

  private emisDbConnector oConnector_;

  private Connection oConn_;

  private PreparedStatement oPrep_;

  private Statement oStmt_;

  private CallableStatement oCall_;

  private ServletContext oContext_;

  private int transactionIsolation_ = Connection.TRANSACTION_READ_COMMITTED;

  // 用來檢查 rollback 在 autocommit mode 是否有正確被使用

  private boolean isLockGen;

  private boolean isCommitted;

  private boolean isRollBacked;

//----------------------------------------------------------



  /**

   *  ResultSet will automatically closed by Statement,PrepareStatement

   *  or Connection close, so we don't need to put it in AllResource HashMap

   */

  private ResultSet oRs_;

  private ResultSetMetaData oMeta_;

  private emisEncodingTransfer oEncodingTransfer_;



  // 用來 debug 用的

  private emisBusiness oBusiness_;



  // 放所有的 prepare Statement

  private HashMap oAllResource_ = new HashMap();

/*-------------------------constructor----------------------------*/



  private emisDb() {

  }



  /**

   * Constructor.

   * @param oContext

   * @param sDbName

   * @throws Exception

   */

  private emisDb(ServletContext oContext, String sDbName) throws Exception {

    getConnection(oContext, sDbName);

  }



  private emisDbMgr oDbMgr_;



  /**

   *

   * @param oContext

   * @param sDbName

   * @throws Exception

   */

  private void getConnection(ServletContext oContext, String sDbName)

      throws Exception {

    // put ServletContext in class member

    oContext_ = oContext;

    emisTracer oTr = emisTracer.get(oContext);



    if (oDbMgr_ == null) {

      oDbMgr_ = emisDbMgr.getInstance(oContext);

    }



    if (sDbName == null)

      oConnector_ = oDbMgr_.getConnector(); // get default connector

    else

      oConnector_ = oDbMgr_.getConnector(sDbName);



    /**

     * 設定使用訊息,以後好 trace

     */

    // this is the same with emisConnectProxy.setDescription()

    oEmisProxy_ = oConnector_.getConnection();



    if (oEmisProxy_ == null)

      oTr.sysError(this, emisError.ERR_DB_NOGET_CONNECT_FROM_SPOOL);



    if (this.oBusiness_ != null) {

      oEmisProxy_.setDescription(oBusiness_.getName() + oBusiness_.getUser().getID());

    } else {

      oEmisProxy_.setDescription("system");

    }



    this.oEncodingTransfer_ = oConnector_.getEncodingTransfer();



    Connection c = oEmisProxy_.getConnection();

    if (c == null)

      oTr.sysError(this, emisError.ERR_DB_NOGET_CONNECT_PROXY, sDbName);

    // emisConnection is a wrapper class

    oConn_ = new emisConnection(oDbMgr_, oEmisProxy_, c);

  }





  /**
   *  從 emisDb Object Spool (emisDbObjectSpool) 中拿一個 emisDb Object 出來,
   *  如果 Spool Cache 中沒有就產生一個新的 emisDb Object
   *  從 emisDbMgr 中拿取 type 設定為 default  的 emisDbConnector (系統設定),
   *  從 Database Connection Spool (emisDbConnector) 中拿一個 JDBC Connection 出來,
   *  將 Connection 放到 emisDb 內,
   *  傳回 emisDb
   *  
   *  @deprecated  
   */



  
    public static emisDb getInstance(String sProjectName) throws Exception {

      return getInstance(emisDbMgr.getServletContext(sProjectName), null, null);

    }
    

   /*

   * @param oContext

   * @return

   * @throws Exception

   */



  public static emisDb getInstance(ServletContext oContext) throws Exception {

    return getInstance(oContext, null, null);

  }



  /**

   *

   * @param oContext

   * @param oBusiness

   * @return

   * @throws Exception

   */

  public static emisDb getInstance(ServletContext oContext, emisBusiness oBusiness)

      throws Exception {

    return getInstance(oContext, oBusiness, null);

  }



  /**

   *

   * @param oContext

   * @param sDbName

   * @return

   * @throws Exception

   */

  public static emisDb getInstance(ServletContext oContext, String sDbName)

      throws Exception {

    return getInstance(oContext, null, sDbName);

  }



  /**

   *  從 emisDb Object Spool (emisDbObjectSpool) 中拿一個 emisDb Object 出來,

   *  如果 Spool Cache 中沒有就產生一個新的 emisDb Object

   *  從 emisDbMgr 中拿取命名為 sDbName 的 emisDbConnector (系統設定),

   *  從 Database Connection Spool (emisDbConnector) 中拿一個 JDBC Connection 出來,

   *  將 Connection 放到 emisDb 內,

   *  傳回 emisDb

   * @param oContext

   * @param oBusiness

   * @param sDbName

   * @return

   * @throws Exception

   */

  public static emisDb getInstance(ServletContext oContext, emisBusiness oBusiness,

                                   String sDbName) throws Exception {

    if (oContext == null)

      throw new Exception("null ServletContext Object");



    emisDbObjectSpool _oSpool = emisDbObjectSpool.getSpool(oContext);



    emisDb _oDb = null;



    if (_oSpool != null) {

      _oDb = _oSpool.checkOutEmisDb();

      if (_oDb != null) {

        _oDb.reset();

      }

    }



    if (_oDb == null) {

      //logConnection(oContext);

       //  robert, 2010/05/14 to add check capability
      increaseCheckPoint(); 

      return new emisDb(oContext, sDbName);

    }



    // we have to do some init staff...

    _oDb.setBusiness(oBusiness);

    _oDb.getConnection(oContext, sDbName);

    //logConnection(oContext);

     //  robert, 2010/05/14 to add check capability
      increaseCheckPoint();

    return _oDb;

  }



  /**

   * 將使用物件的類別名放入description以利emisDbMonitor顯示.

   * @param oContext

   * @param oOwner

   * @return

   * @throws Exception

   */

  public static emisDb getInstance(ServletContext oContext, Object oOwner)

      throws Exception {

    if (oContext == null)

      throw new Exception("null ServletContext Object");



    emisDbObjectSpool _oSpool = emisDbObjectSpool.getSpool(oContext);

    emisDb _oDb = null;



    if (_oSpool != null) {

      _oDb = _oSpool.checkOutEmisDb();

      if (_oDb != null) {

        _oDb.reset();

      }

    }



    if (_oDb == null) {

      _oDb = new emisDb(oContext, null);

      _oDb.setDescription(oOwner.getClass().getName());

      //logConnection(oContext);

      return _oDb;

    }



    // we have to do some init staff...

    _oDb.setBusiness(null);

    _oDb.getConnection(oContext, null);

    _oDb.setDescription(oOwner.getClass().getName());

    //logConnection(oContext);

    return _oDb;

  }



  /**

   *

   * @param oBusiness

   */

  private void setBusiness(emisBusiness oBusiness) {

    if (oBusiness != null) {

      oBusiness.addReferenceCount();

    }

    oBusiness_ = oBusiness;

  }



/*-----------------------poolable Object support------------------------*/



  // set every thing to default

  /**

   * reset.

   */

  private void reset() {

    oEmisProxy_ = null;

    oConn_ = null;

    oPrep_ = null;

    oStmt_ = null;

    oCall_ = null;

    oRs_ = null;

    oMeta_ = null;

    oEncodingTransfer_ = null;

    oAllResource_.clear();

  }



/*------------------------misc function---------------------------*/

  /**

   * 設定 emisDb 使用說明,

   * 在 http://localhost:8000/epos/servlet/com.emis.servlet.emisDbMonitor 中

   * 可以看到 (系統內部作業)

   * @param sStr

   */

  public void setDescription(String sStr) {

    if (this.oEmisProxy_ != null) {

      oEmisProxy_.setDescription(sStr);

    }

  }



  /**

   *  使用系統序號, 通常由 XML 中設定 Format

   *  不需直接使用,請參考 XML 中 tag "pname" 的說明

   * @param sSequenceName

   * @param checkAutoDrop

   * @param sDropCondition

   * @param sFormat

   * @return

   * @throws SQLException

   */

  public String getSequenceNumber(String sSequenceName, boolean checkAutoDrop,

                        String sDropCondition, String sFormat) throws SQLException {

    return oConnector_.getSequenceNumber(this.oConn_, sSequenceName,

        checkAutoDrop, sDropCondition, sFormat);

  }



  /**

   *  使用系統序號, 通常由 XML 中設定 Format

   *  不需直接使用,請參考 XML 中 tag "pname" 的說明

   * @param sSequenceName

   * @param sSNO

   * @param sDropCondition

   * @param sFormat

   * @return

   * @throws SQLException

   */

  public String getStoreSequence(String sSequenceName, String sSNO,

                      String sDropCondition, String sFormat) throws SQLException {

    return oConnector_.getStoreSequence(this.oConn_, sSequenceName, sSNO,

        sDropCondition, sFormat);

  }



  /**

   * 將某個 SQL Cache 設成過期,讓下一次 Query

   * 會重新 Query SQL

   * @param sSQLName

   * @throws SQLException

   */

  public void expireSQLCache(String sSQLName) throws SQLException {

    oConnector_.expireSQLCache(this.oConn_, sSQLName);

  }



/*------------------------private util function-------------------*/



  /**

   *

   * @throws SQLException

   */

  private void checkStatement() throws SQLException {

    if (oStmt_ == null) {

      oStmt_ = oConn_.createStatement();

      oAllResource_.put(oStmt_, oStmt_);

    }

  }



  /**

   *

   * @throws SQLException

   */

  private void checkPrepStmt() throws SQLException {

    if (oPrep_ == null)

      throw new SQLException("Current PreparedStatement not exists");

  }



  /**

   *

   * @throws SQLException

   */

  private void checkResultSetMetaData() throws SQLException {

    if (oRs_ == null)

      throw new SQLException("Current ResultSet is not Existing");



    if (oMeta_ == null) {

      oMeta_ = oRs_.getMetaData();

    }

  }



  /**

   *

   * @throws SQLException

   */

  private void checkResultSet() throws SQLException {

    if (oRs_ == null)

      throw new SQLException("Current ResultSet is not Existing");

  }



  /**

   *

   * @throws SQLException

   */

  private void checkPrepareCall() throws SQLException {

    if (oCall_ == null) throw new SQLException("CallableStatement is not existing");

  }



/*---------------------charset support function------------------*/





  /**

   * 在有些 critial section

   * 如日結或即時上傳,因為需要速度,

   * 又全部的資料都是英文,所以不須要 transfer Encoding

   * 可以手動將 emisDb 的 transfer Mode 設成 emisConnector.TRANSFER_NONE

   * 此動作並不會影響其他使用中的 emisDb 的轉碼

   * 也不會影響系統 Default 的轉碼設定

   * @param nMode

   */

  public void setEncodingTransferMode(int nMode) {

    oEncodingTransfer_.setTransferMode(nMode);

  }



  /**

   * 取得目前使用的轉碼物件

   * 可以用來手動轉碼

   * @return

   */

  public emisEncodingTransfer getEncodingTransfer() {

    return oEncodingTransfer_;

  }





  /**

   *   Transaction Isolation 有以下五種

   *

   *   TRANSACTION_NONE

   *   TRANSACTION_READ_UNCOMMITTED

   *   TRANSACTION_READ_COMMITTED

   *   TRANSACTION_REPEATABLE_READ

   *   TRANSACTION_SERIALIZABLE

   * @param isolation

   * @throws SQLException

   */

  public void setTransactionIsolation(int isolation) throws SQLException {

	this.transactionIsolation_ = isolation;
    oConn_.setTransactionIsolation(isolation);

  }
  
  public void setDefaultTransactionIsolation() throws SQLException {

	  setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

  }



  /**

   *  取得目前設定的 Transaction Isolation

   * @return

   * @throws SQLException

   */

  public int getTransactionIsolation() throws SQLException {

    return oConn_.getTransactionIsolation();

  }





/*--------------------------------Statement---------------------------------*/



  /**

   * 在 emisDb 內建立 java.sql.Statement

   * @return

   * @throws SQLException

   * @see #execute(String sSQL)

   * @see #executeQuery(String sSQL)

   * @see #executeUpdate(String sSQL)

   * @see #getMoreResults()

   * @see #getResultSet()

   */

  public Statement createStatement() throws SQLException {

    oStmt_ = oConn_.createStatement();

    oAllResource_.put(oStmt_, oStmt_);

    return oStmt_;

  }





  /**

   *   從 Connection 產生 Statement 物件,

   *   執行 sSQL , 通常用於資料定義的 SQL,

   *   此 Statement 會存在 emisDb 中

   * @param sSQL

   * @return

   * @throws SQLException

   */

  public boolean execute(String sSQL) throws SQLException {

    if (oBusiness_ != null) oBusiness_.debug("SQL=" + sSQL);



    checkStatement();



    sSQL = oEncodingTransfer_.sysToDb(sSQL);

    return oStmt_.execute(sSQL);

  }



  /**

   *   從 Connection 產生 Statement 物件,

   *   執行 sSQL , 通常用於資料查詢的 SQL,

   *   傳回一個 ResultSet

   * @param sSQL

   * @return

   * @throws SQLException

   */

  public ResultSet executeQuery(String sSQL) throws SQLException {

    if (oBusiness_ != null) oBusiness_.debug("SQL=" + sSQL);

    checkStatement();

    sSQL = oEncodingTransfer_.sysToDb(sSQL);

    oRs_ = oStmt_.executeQuery(sSQL);

    oMeta_ = null;

    return oRs_;

  }



  /**

   *  將目前的 ResultSet 設成 oRs Object

   * @param oRs

   * @throws SQLException

   */

  public void setCurrentResultSet(ResultSet oRs) throws SQLException {

    oRs_ = oRs;

    oMeta_ = null;

  }



  /**

   *   從 Connection 產生 Statement 物件,

   *   執行 sSQL , 通常用於資料變更的 SQL,

   *   傳回更新筆數, 如果 Connection 不是在

   *   autoCommit mode, 最後要記得 commit

   * @param sSQL

   * @return

   * @throws SQLException

   */

  public int executeUpdate(String sSQL) throws SQLException {

    if (oBusiness_ != null) oBusiness_.debug("SQL=" + sSQL);

    checkStatement();

    sSQL = oEncodingTransfer_.sysToDb(sSQL);

    commitFlagOff(true);

    return oStmt_.executeUpdate(sSQL);

  }



  /**

   *  拿取 Statement 的 next ResultSet,

   *  有些較特殊的 SQL 會傳回不只一個 ResultSet

   * @return

   * @throws SQLException

   */

  public boolean getMoreResults() throws SQLException {

    checkStatement();

    return oStmt_.getMoreResults();

  }



  /**

   * Returns the current result as a ResultSet object.

   * @return

   * @throws SQLException

   */

  public ResultSet getResultSet() throws SQLException {

    checkStatement();

    oRs_ = oStmt_.getResultSet();

    oMeta_ = null;

    return oRs_;

  }



  /**

   * clear Statement's Batch

   * @throws SQLException

   */

  public void clearStmtBatch() throws SQLException {

    checkStatement();

    oStmt_.clearBatch();

  }



  /**

   *  execute Statement's Batch

   * @return

   * @throws SQLException

   */

  public int[] executeStmtBatch() throws SQLException {

    checkStatement();

    return oStmt_.executeBatch();

  }



/* -------------------------------preparedStatement---------------------------*/



  /**

   * 開啟一個新的 PreparedStatement . 如果要同時操做兩個以上

   * 的 PreparedStatement ,請將回傳值 keep 住

   * @param sSQL

   * @return

   * @throws SQLException

   */

  public PreparedStatement prepareStmt(String sSQL) throws SQLException {

    if (oBusiness_ != null) oBusiness_.debug("prepared SQL=" + sSQL);



    oPrep_ = oConn_.prepareStatement(oEncodingTransfer_.sysToDb(sSQL));

    oAllResource_.put(oPrep_, oPrep_);

    return oPrep_;

  }



  /**

   * 開啟一個新的 PreparedStatement . 如果要同時操做兩個以上

   * 的 PreparedStatement ,請將回傳值 keep 住

   * @param sSQL

   * @param resultSetType

   * @param resultSetConcurrency

   * @return

   * @throws SQLException

   * @see #clearParameters()

   * @see #prepareUpdate()

   * @see #prepareQuery()

   * @see #prepareUpdate()

   * @see #setString(int nNum,String sStr)

   * @see #setLong(int nNum,long l)

   * @see #setFloat(int nNum,Float oFloat)

   * @see #setBigDecimal(int nNum,BigDecimal dec)

   * @see #setFloat(int nNum,float nFloat)

   * @see #setInt(int nNum,int nValue)

   * @see #setNull(int nNum,int nSqlType)

   */

  public PreparedStatement prepareStmt(String sSQL, int resultSetType,

                                       int resultSetConcurrency) throws SQLException {

    if (oBusiness_ != null) oBusiness_.debug("prepared SQL=" + sSQL);

    oPrep_ = oConn_.prepareStatement(oEncodingTransfer_.sysToDb(sSQL),

        resultSetType, resultSetConcurrency);

    oAllResource_.put(oPrep_, oPrep_);

    return oPrep_;

  }





  /**

   * set current PreparedStatement to oPrep

   * @param oPrep

   */

  public void setCurrentPrepareStmt(PreparedStatement oPrep) {

    oPrep_ = oPrep;

  }



  /**

   * clear current PreparedStatement's Parameter

   * @throws SQLException

   */

  public void clearParameters() throws SQLException {

    checkPrepStmt();

    oPrep_.clearParameters();

  }



  /**

   * @return int

   * @throws SQLException

   */

  public int prepareUpdate() throws SQLException {

    checkPrepStmt();

    commitFlagOff(true);

    return oPrep_.executeUpdate();

  }



  /**

   * @return ResultSet

   * @throws SQLException

   */

  public ResultSet prepareQuery() throws SQLException {

    checkPrepStmt();

    oRs_ = oPrep_.executeQuery();

    oMeta_ = null;



    return oRs_;

  }





  /**

   * clear PreparedStatement's Batch

   * Batch操作必須setAutoCommit(false)

   * @throws SQLException

   */

  public void clearPrepareStmtBatch() throws SQLException {

    checkPrepStmt();

    oPrep_.clearBatch();

  }



  /**

   * execute PreparedStatement's Batch

   * Batch操作必須setAutoCommit(false), 執行batch更新後會自動commit.

   * @return

   * @throws SQLException

   */

  public int[] prepareExecuteBatch() throws SQLException {

    checkPrepStmt();

    int[] _iValues = oPrep_.executeBatch();

    // this.commit(); 不應該在這邊加  commit, 由上層決定

    return _iValues;

  }



  /**

   * add PreparedStatement's Batch

   * Batch操作必須setAutoCommit(false)

   * @throws SQLException

   */

  public void prepareAddBatch() throws SQLException {

    checkPrepStmt();

    oPrep_.addBatch();

  }



/*-------------------------- get/set pairs-------------------------------*/



  /**

   *    setXXX , set PreparedStatement's value

   *    to set a null column, you can use setString(1,""),

   *    instead of setNull(1);

   * @param nNum

   * @param sStr

   * @throws SQLException

   */

  public void setString(int nNum, String sStr) throws SQLException {



    checkPrepStmt();

    sStr = oEncodingTransfer_.sysToDb(sStr);

    oPrep_.setString(nNum, sStr);

  }



  /**

   *

   * @param nNum

   * @param l

   * @throws SQLException

   */

  public void setLong(int nNum, long l) throws SQLException {

    checkPrepStmt();

    oPrep_.setLong(nNum, l);

  }



  /**

   *    setXXX , set PreparedStatement's value,

   *    to convert String to Float , use

   *    Float.parseFloat(sFloatString)

   * @param nNum

   * @param oFloat

   * @throws SQLException

   */

  public void setFloat(int nNum, Float oFloat) throws SQLException {

    checkPrepStmt();

    oPrep_.setFloat(nNum, oFloat.floatValue());

  }



  /**

   *    setXXX , set PreparedStatement's value

   * @param nNum

   * @param nFloat

   * @throws SQLException

   */

  public void setFloat(int nNum, float nFloat) throws SQLException {

    checkPrepStmt();

    oPrep_.setFloat(nNum, nFloat);

  }



  /**

   *    setXXX , set PreparedStatement's value,

   *    to convert String to Double

   * @param nNum

   * @param oDouble

   * @throws SQLException

   */

  public void setDouble(int nNum, Double oDouble) throws SQLException {

    checkPrepStmt();

    oPrep_.setDouble(nNum, oDouble.doubleValue());

  }



  /**

   *

   * @param nNum

   * @param value

   * @throws SQLException

   */

  public void setDouble(int nNum, double value) throws SQLException {

    checkPrepStmt();

    oPrep_.setDouble(nNum, value);

  }



  /**

   *

   * @param nNum

   * @param dec

   * @throws SQLException

   */

  public void setBigDecimal(int nNum, BigDecimal dec) throws SQLException {

    checkPrepStmt();

    oPrep_.setBigDecimal(nNum, dec);

  }



  /**

   *    setXXX , set PreparedStatement's value

   *    to convert String to Integer,use

   *    Integer.parseInt();

   * @param nNum

   * @param nValue

   * @throws SQLException

   */

  public void setInt(int nNum, int nValue) throws SQLException {

    checkPrepStmt();



    oPrep_.setInt(nNum, nValue);

  }



  /**

   * nSqlType  is defined in  java.sql.Types

   * @param nNum

   * @param nSqlType

   * @throws SQLException

   */

  public void setNull(int nNum, int nSqlType) throws SQLException {

    checkPrepStmt();

    oPrep_.setNull(nNum, nSqlType);

  }





  /**

   *  關掉某個特定的 PreparedStatement

   * @param oPrep

   */

  public void closePrepareStmt(PreparedStatement oPrep) {

    try {

      oAllResource_.remove(oPrep);

      if (oPrep != null) oPrep.close();

    } catch (Exception ignore) {

      System.out.println("emisDb.closePrepareStmt: " + ignore.getMessage());

    }

  }



/*------------------------------PrepareCall----------------------------------*/



  /**

   * 開啟 CallableStatement (用來執行 stored procedures )

   * @param sSQL

   * @return

   * @throws SQLException

   * @see #callSetString(int nNum,String sStr)

   * @see #callSetLong(int nNum,long l)

   * @see #callSetFloat(int nNum,Float oFloat)

   * @see #callSetFloat(int nNum,float f)

   * @see #callSetBigDecimal(int nNum,BigDecimal bdec)

   * @see #callSetInt(int nNum,int nValue)

   * @see #callSetNull(int nNum,int nSQLType)

   * @see #callGetString( int nNum )

   * @see #callGetInt(int nNum)

   * @see #callGetObject(int nNum)

   */

  public CallableStatement prepareCall(String sSQL) throws SQLException {

    if (oBusiness_ != null) oBusiness_.debug("call SQL=" + sSQL);

    sSQL = oEncodingTransfer_.sysToDb(sSQL);

    oCall_ = oConn_.prepareCall(sSQL);

    oAllResource_.put(oCall_, oCall_);

    return oCall_;

  }



  /**

   * 產生 CallableStatement

   * nResultSetType 和 nResultSetConCurrency 請參看 JDBC2.0

   * @param sSQL

   * @param nResultSetType

   * @param nResultSetConcurrency

   * @return

   * @throws SQLException

   */

  public CallableStatement prepareCall(String sSQL, int nResultSetType,

                                       int nResultSetConcurrency) throws SQLException {

    if (oBusiness_ != null) oBusiness_.debug("call SQL=" + sSQL);

    sSQL = oEncodingTransfer_.sysToDb(sSQL);

    oCall_ = oConn_.prepareCall(sSQL, nResultSetType, nResultSetConcurrency);

    oAllResource_.put(oCall_, oCall_);

    return oCall_;

  }



  /**

   * set current CallableStatemtn to oCall Object

   * @param oCall

   * @throws SQLException

   */

  public void setCurrentCallableStmt(CallableStatement oCall) throws SQLException {

    oCall_ = oCall;

  }



  /**

   * 當使用 CallableStatement 時,要拿取 Out 變數時,要記得先 register

   * 如

   * begin

   *     ? := storedproc(?);

   * end;

   * --------------------------------------------

   *    registerOutParameter(1,Types.VARCHAR); // if storedproc return varchar

   *    ....

   *    execute callableStatement

   *    ....

   *    String returnValue = callGetString(1);

   * @param nNum

   * @param nSQLType

   * @throws SQLException

   */

  public void registerOutParameter(int nNum, int nSQLType) throws SQLException {

    checkPrepareCall();

    oCall_.registerOutParameter(nNum, nSQLType);

  }



  /**

   * add batch , used for CallableStatement

   * @throws SQLException

   */

  public void callAddBatch() throws SQLException {

    checkPrepareCall();

    oCall_.addBatch();

  }



  /**

   *

   * @return

   * @throws SQLException

   */

  public int[] callExecuteBatch() throws SQLException {

    checkPrepareCall();

    return oCall_.executeBatch();

  }



  /**

   *

   * @param nNum

   * @param sStr

   * @throws SQLException

   */

  public void callSetString(int nNum, String sStr) throws SQLException {

    checkPrepareCall();

    sStr = oEncodingTransfer_.sysToDb(sStr);

    oCall_.setString(nNum, sStr);

  }



  /**

   *

   * @param nNum

   * @param l

   * @throws SQLException

   */

  public void callSetLong(int nNum, long l) throws SQLException {

    checkPrepareCall();

    oCall_.setLong(nNum, l);

  }



  /**

   *

   * @param nNum

   * @param oFloat

   * @throws SQLException

   */

  public void callSetFloat(int nNum, Float oFloat) throws SQLException {

    checkPrepareCall();

    oCall_.setFloat(nNum, oFloat.floatValue());

  }



  /**

   *

   * @param nNum

   * @param f

   * @throws SQLException

   */

  public void callSetFloat(int nNum, float f) throws SQLException {

    checkPrepareCall();

    oCall_.setFloat(nNum, f);

  }



  /**

   *

   * @param nNum

   * @param bdec

   * @throws SQLException

   */

  public void callSetBigDecimal(int nNum, BigDecimal bdec) throws SQLException {

    checkPrepareCall();

    oCall_.setBigDecimal(nNum, bdec);

  }



  /**

   *

   * @param nNum

   * @param nValue

   * @throws SQLException

   */

  public void callSetInt(int nNum, int nValue) throws SQLException {

    checkPrepareCall();

    oCall_.setInt(nNum, nValue);

  }



  /**

   *

   * @param nNum

   * @param nSQLType

   * @throws SQLException

   */

  public void callSetNull(int nNum, int nSQLType) throws SQLException {

    checkPrepareCall();

    oCall_.setNull(nNum, nSQLType);

  }



  /**

   *

   * @param nNum

   * @return

   * @throws SQLException

   */

  public String callGetString(int nNum) throws SQLException {

    checkPrepareCall();

    String sStr = oCall_.getString(nNum);

    return oEncodingTransfer_.dbToSys(sStr);

  }



  /**

   *

   * @param nNum

   * @return

   * @throws SQLException

   */

  public int callGetInt(int nNum) throws SQLException {

    checkPrepareCall();

    return oCall_.getInt(nNum);

  }



  /**

   *

   * @param nNum

   * @return

   * @throws SQLException

   */

  public Object callGetObject(int nNum) throws SQLException {

    checkPrepareCall();

    return oCall_.getObject(nNum);

  }



  /**

   * 關掉某個特定的 CallableStatement

   * @param oCall

   */

  public void closePrepareCall(CallableStatement oCall) {

    oAllResource_.remove(oCall);

    try {

      oCall.close();

    } catch (Exception ignore) {

      System.out.println("emisDb.closePrepareCall: " + ignore.getMessage());

    }

  }



  /**

   * execute current CallableStatement

   * @throws SQLException

   */

  public boolean executePrepareCall() throws SQLException {

    commitFlagOff(true);    
    return oCall_.execute();

  }





/*------------------------- ResultSet Function-----------------------------*/



  /**

   *  將目前的 ResultSet 移到下一筆

   *  return false if it is End of the ResultSet

   * @return

   * @throws SQLException

   *  @see #isAfterLast()

   *  @see #isBeforeFirst()

   *  @see #previous()

   *  @see #first()

   *  @see #last()

   *  @see #absolute(int i)

   */

  public boolean next() throws SQLException {

    checkResultSet();

    return oRs_.next();

  }



  /**

   * @return

   * @throws SQLException

   *  @see #next()

   *  @see #isBeforeFirst()

   *  @see #previous()

   *  @see #first()

   *  @see #last()

   *  @see #absolute(int i)

   */

  public boolean isAfterLast() throws SQLException {

    checkResultSet();

    return oRs_.isAfterLast();

  }



  /**

   * @return

   * @throws SQLException

   *  @see #next()

   *  @see #isAfterLast()

   *  @see #previous()

   *  @see #first()

   *  @see #last()

   *  @see #absolute(int i)

   */

  public boolean isBeforeFirst() throws SQLException {

    checkResultSet();

    return oRs_.isBeforeFirst();

  }



  /**

   *  將目前的 ResultSet 移到上一筆

   * @return

   * @throws SQLException

   *  @see #next()

   *  @see #isAfterLast()

   *  @see #isBeforeFirst()

   *  @see #first()

   *  @see #last()

   *  @see #absolute(int i)

   */

  public boolean previous() throws SQLException {

    checkResultSet();

    return oRs_.previous();

  }



  /**

   *  將目前的 ResultSet 移到第一筆,如果是往上的話

   *  必需在 Query 時指定 ResultSet.TYPE_SCROLL_SENSITIVE

   * @return

   * @throws SQLException

   *  @see #next()

   *  @see #isAfterLast()

   *  @see #isBeforeFirst()

   *  @see #previous()

   *  @see #last()

   *  @see #absolute(int i)

   */

  public boolean first() throws SQLException {

    checkResultSet();

    return oRs_.first();

  }





  /**

   *  將目前的 ResultSet 移到最後一筆

   * @return

   * @throws SQLException

   * @see #first()

   * @see #next()

   * @see #isAfterLast()

   * @see #isBeforeFirst()

   * @see #previous()

   * @see #absolute(int i)

   */

  public boolean last() throws SQLException {

    checkResultSet();

    return oRs_.last();

  }



  /**

   * 將目前的 ResultSet 移到特定一筆記錄.

   * @param i

   * @return

   * @throws SQLException

   */

  public boolean absolute(int i) throws SQLException {

    checkResultSet();

    return oRs_.absolute(i);

  }



  /**

   *

   * @return

   * @throws SQLException

   */

  public int getRow() throws SQLException {

    checkResultSet();

    return oRs_.getRow();

  }



  /**

   * get current ResultSet's ColumnCount

   * @return

   * @throws SQLException

   */

  public int getColumnCount() throws SQLException {

    checkResultSetMetaData();

    return oMeta_.getColumnCount();

  }



  /**

   * get specific ColumnName of ResultSet

   * nNum from 1 to getColumnCount

   * @param nNum

   * @return

   * @throws SQLException

   */

  public String getColumnName(int nNum) throws SQLException {

    checkResultSetMetaData();

    String _sStr = oMeta_.getColumnName(nNum);

    
    return oEncodingTransfer_.dbToSys(_sStr);
    

  }



  /**

   * get Column's Data Type of specific Column of current ResultSet

   * return value is defined in java.sql.Types

   * @param nNum

   * @return

   * @throws SQLException

   */

  public int getColumnType(int nNum) throws SQLException {

    checkResultSetMetaData();

    return oMeta_.getColumnType(nNum);

  }



  /**

   * just for compatible with getNumber

   * @param nColumn

   * @param nPrecision

   * @param nScale

   * @return

   * @throws SQLException

   */

  public String getString(int nColumn, int nPrecision, int nScale) throws SQLException {

    checkResultSet();

    String _sStr = oRs_.getString(nColumn);

    return getNumberString(_sStr, nPrecision, nScale);

  }



  /**

   *

   * @param nColumn

   * @return

   * @throws SQLException

   */

  public long getLong(int nColumn) throws SQLException {

    checkResultSet();

    return oRs_.getLong(nColumn);

  }



  /**

   *

   * @param sColumnName

   * @return

   * @throws SQLException

   */

  public long getLong(String sColumnName) throws SQLException {

    checkResultSet();

    return oRs_.getLong(sColumnName);

  }



  /**

   * just for compatible with getNumber

   * @param sColumn

   * @param nPrecision

   * @param nScale

   * @return

   * @throws SQLException

   */

  public String getString(String sColumn, int nPrecision, int nScale)

      throws SQLException {

    checkResultSet();

    String _sStr = oRs_.getString(oEncodingTransfer_.sysToDb(sColumn));

    return getNumberString(_sStr, nPrecision, nScale);

  }





  /**

   * 為了產生浮點數 formatted 的輸出 ( 5 -> 5.00 )

   *  getString("P_NO") = "5"

   *  getNumber("P_NO",10,2) = "5.00"

   *  nPrecision 為小數點後幾位,nScale 為總共的長度(含小數點)

   * @param sColumn

   * @param nPrecision

   * @param nScale

   * @return

   * @throws SQLException

   */

  public String getNumber(String sColumn, int nPrecision, int nScale)

      throws SQLException {

    checkResultSet();

    String _sStr = oRs_.getString(oEncodingTransfer_.sysToDb(sColumn));

    return getNumberString(_sStr, nPrecision, nScale);

  }



  /**

   *  為了產生浮點數 formatted 的輸出 ( 5 -> 5.00 )

   *  getString("P_NO") = "5"

   *  getNumber("P_NO",10,2) = "5.00"

   *  nPrecision 為小數點後幾位,nScale 為總共的長度(含小數點)

   * @param nColumn

   * @param nPrecision

   * @param nScale

   * @return

   * @throws SQLException

   */

  public String getNumber(int nColumn, int nPrecision, int nScale) throws SQLException {

    checkResultSet();

    String _sStr = oRs_.getString(nColumn);

    return getNumberString(_sStr, nPrecision, nScale);

  }



  /**

   *

   * @param sNumber

   * @param nPrecision

   * @param nScale

   * @return

   * @throws SQLException

   */

  private String getNumberString(String sNumber, int nPrecision, int nScale)

      throws SQLException {

    if (sNumber == null) sNumber = "0";

    int _iLen = sNumber.length();

    int _iPos = sNumber.indexOf(".");

    if (_iPos < 0) { // 整數找不到小數點; 最後再加上.00

      sNumber = sNumber + "." + emisChinese.rpad("0", "0", nPrecision);

    } else if (_iPos == 0) { // .12

      sNumber = '0' + sNumber;  // .12=>0.12

    } else if (_iLen - _iPos <= nPrecision) {

      // file://整數; 最後再加上.00

      sNumber = sNumber + emisChinese.rpad("0", "0", nPrecision - (_iLen - _iPos));

    }

    return emisChinese.lpad(sNumber, nScale);

  }



  /**

   * 這個 function 是和 getString 完全一樣,只是不做資料庫的轉碼動作,所以如果你確定

   * 欄位不會有中文, 可以用 getAscii, 會比較快.

   * @param sColumn

   * @return

   * @throws SQLException

   */

  public String getAscii(String sColumn) throws SQLException {

    checkResultSet();

    return oRs_.getString(sColumn);

  }



  /**

   * 這個 function 是和 getString 完全一樣,只是不做資料庫的轉碼動作,所以如果你確定

   * 欄位不會有中文, 可以用 getAscii, 會比較快.

   * @param nColumn

   * @return

   * @throws SQLException

   */

  public String getAscii(int nColumn) throws SQLException {

    checkResultSet();

    return oRs_.getString(nColumn);

  }



  /**

   * getDouble.

   * @param nColumn

   * @return

   * @throws SQLException

   */

  public double getDouble(int nColumn) throws SQLException {

    checkResultSet();

    return oRs_.getDouble(nColumn);

  }

  /**

   * getBigDecimal.

   * @param nColumn

   * @return

   * @throws SQLException

   */

  public BigDecimal getBigDecimal(int nColumn) throws SQLException {

    checkResultSet();

    return oRs_.getBigDecimal(nColumn);

  }



  /**

   * getBigDecimal.

   * @param sColumnName

   * @return

   * @throws SQLException

   */

  public BigDecimal getBigDecimal(String sColumnName) throws SQLException {

    checkResultSet();

    return oRs_.getBigDecimal(sColumnName);

  }



  /**

   * getDouble.

   * @param sColumn

   * @return

   * @throws SQLException

   */

  public double getDouble(String sColumn) throws SQLException {

    checkResultSet();

    return oRs_.getDouble(sColumn);

  }



  /**

   * 取出第 nNum 欄位的值 , 有些資料庫在取 Long 的欄位或 Memo 時有特別的限制,請注意.

   * @param sColumn

   * @return

   * @throws SQLException

   */

  public String getString(String sColumn) throws SQLException {

    checkResultSet();

    String _sStr = oRs_.getString(oEncodingTransfer_.sysToDb(sColumn));

    return oEncodingTransfer_.dbToSys(_sStr);

  }



  /**

   * 取出第 nNum 欄位的值 , 有些資料庫在取 Long 的欄位或 Memo 時有特別的限制,請注意.

   * @param nNum

   * @return

   * @throws SQLException

   */

  public String getString(int nNum) throws SQLException {

    checkResultSet();

    String _sStr = oRs_.getString(nNum);

    return oEncodingTransfer_.dbToSys(_sStr);

  }



  /**

   * getDate.

   * @param sColumn

   * @return

   * @throws SQLException

   */

  public java.util.Date getDate(String sColumn) throws SQLException {

    checkResultSet();

    return oRs_.getDate(sColumn);

  }



  /**

   * getDate.

   * @param nNum

   * @return

   * @throws SQLException

   */

  public java.sql.Date getDate(int nNum) throws SQLException {

    checkResultSet();

    return oRs_.getDate(nNum);

  }



  /**

   * getTimestamp.

   * @param sColumn

   * @return

   * @throws SQLException

   */

  public java.util.Date getTimestamp(String sColumn) throws SQLException {

    checkResultSet();

    return oRs_.getTimestamp(sColumn);

  }



  /**

   * getTimestamp.

   * @param nNum

   * @return

   * @throws SQLException

   */

  public java.util.Date getTimestamp(int nNum) throws SQLException {

    checkResultSet();

    return oRs_.getTimestamp(nNum);

  }



  /**

   * 要判斷 getInt 傳回 0 的情形,請看 wasNull()

   * @param sColumn

   * @return

   * @throws SQLException

   */

  public int getInt(String sColumn) throws SQLException {

    checkResultSet();

    return oRs_.getInt(oEncodingTransfer_.sysToDb(sColumn));

  }



  /**

   * getInt.

   * @param nNum

   * @return

   * @throws SQLException

   */

  public int getInt(int nNum) throws SQLException {

    checkResultSet();

    return oRs_.getInt(nNum);

  }



  /**

   * getFloat.

   * @param sColumn

   * @return

   * @throws SQLException

   */

  public float getFloat(String sColumn) throws SQLException {

    checkResultSet();

    return oRs_.getFloat(sColumn);

  }



  /**

   * getFloat.

   * @param nNum

   * @return

   * @throws SQLException

   */

  public float getFloat(int nNum) throws SQLException {

    checkResultSet();

    return oRs_.getFloat(nNum);

  }



  /**

   * getObject.

   * @param sColumn

   * @return

   * @throws SQLException

   */

  public Object getObject(String sColumn) throws SQLException {

    checkResultSet();

    return oRs_.getObject(oEncodingTransfer_.sysToDb(sColumn));

  }



  /**

   * getObject.

   * @param nNum

   * @return

   * @throws SQLException

   */

  public Object getObject(int nNum) throws SQLException {

    checkResultSet();

    return oRs_.getObject(nNum);

  }





  /**

   * 專門用來判斷上一個 getXXX 是否為 null

   * 因為 getInt 在null 的情形回傳為 0, 為

   * 法判斷資料是真的為 0 或是 null

   * @return boolean

   * @throws SQLException

   */

  public boolean wasNull() throws SQLException {

    checkResultSet();

    return oRs_.wasNull();

  }



/*-------------------------connection function-----------------------------*/



  /**

   * 設定 Connection 的 commit mode

   * @param auto

   * @throws SQLException

   */

  public void setAutoCommit(boolean auto) throws SQLException {

    oConn_.setAutoCommit(auto);

    if (!auto) {

      commitFlagOff(false);

    }

  }



  /**

   * off commit flag.

   * @param lock

   */

  private void commitFlagOff(boolean lock) {

    isLockGen = lock;

    isCommitted = false;

    isRollBacked = false;

  }



  /**

   * 傳回 Connection 是否在 autocommit mode

   * @return boolean

   * @throws SQLException

   */

  public boolean getAutoCommit() throws SQLException {

    return oConn_.getAutoCommit();

  }



  /**

   * Connection 做 RollBack

   * @throws SQLException

   */

  public void rollback() throws SQLException {

    oConn_.rollback();

    isRollBacked = true;



  }



  /**

   * Connection 做 commit 動作

   * @throws SQLException

   */

  public void commit() throws SQLException {

    oConn_.commit();

    isCommitted = true;

  }



  /**

   * 將目前所有的 Resulset , Statement 都關掉

   * 但 connection 不關

   */

  public void freeCurrentResource() {

    if (oAllResource_.size() > 0) {

      Set _oSet = oAllResource_.keySet();

      Iterator it = _oSet.iterator();



      if (it != null) {

        while (it.hasNext()) {

          Object _oKey = it.next();



       //   Object _oValue = oAllResource_.get(_oKey);

          try {

              if( _oKey instanceof java.sql.PreparedStatement){

                 java.sql.PreparedStatement  _oValueS = (PreparedStatement)oAllResource_.get(_oKey);

                 _oValueS.close();

              }else if(_oKey instanceof java.sql.CallableStatement){

                  java.sql.CallableStatement  _oValueS = (CallableStatement)oAllResource_.get(_oKey);

                 _oValueS.close();

              }else{

                 // System.out.println("close statment ");

                 java.sql.Statement  _oValueS = (Statement)oAllResource_.get(_oKey);

                 _oValueS.close();

              }

          } catch (Exception ignore) {

            System.out.println("emisDb.freeCurrentResource:" + ignore.getMessage());

          }

        }

      }

    }

    oAllResource_.clear();

  }



  /**

   * 此連線是否已關閉

   * @return

   * @throws SQLException

   */

  public boolean isClosed() throws SQLException {

    return oConn_.isClosed();

  }



  /**

   * 關閉所有的 Resource

   */

  public void close() {

    if (oConn_ == null) return;

    //  robert, 2010/05/14 to add check capability
    decreaseCheckPoint();

    // robert , 2009/12/11 , add set it to default.
    if( this.transactionIsolation_ != Connection.TRANSACTION_READ_COMMITTED)
    {
    	try {
    		this.setDefaultTransactionIsolation();
    	} catch (Exception ignore) {}
    }

    try {

      // check for auto commit , it will cause dead lock      
      if (!oConn_.getAutoCommit()) {

        if (isLockGen && !(isCommitted || isRollBacked)) {

          // it is caused by error programming

          // we should log it

          emisTracer tr = emisTracer.get(oContext_);

          if (!isCommitted) {

            tr.info(this, "no commit in autocommited mode detected");

          }

          if (!isRollBacked) {

            tr.info(this, "no rollback in autocommited mode detected");

          }



          if (oBusiness_ != null) {

            tr.info("business=" + oBusiness_.getName());

          }

          // tr.info("executing SQL="+oEmisProxy_.getExecutingSQL());

          oConn_.rollback();

          tr.info("data auto rollback!!!");

          tr = null;

        }

      }

    } catch (Exception ignore) {

      System.out.println("emisDb.close:" + ignore.getMessage());

    }



    if (oBusiness_ != null) {

      oBusiness_.decReferenceCount();

    }

    freeCurrentResource();

    try {

      oEmisProxy_.close();

    } catch (Exception ignore1) {

      System.out.println("emisDb.close:" + ignore1.getMessage());

    } finally {

      oConn_ = null;

      oEmisProxy_ = null;

    }

    // check in object spool , for cache
    emisDbObjectSpool _oDbObjectSpool = emisDbObjectSpool.getSpool(oContext_);
    if (_oDbObjectSpool != null) {
      _oDbObjectSpool.checkInEmisDb(this);
    }

  }



  /**

   * 僅供 Database Spooling Debug 用

   * @return

   */

  public String getConnectionIdentifier() {

    return oConn_.toString();

  }



  /**

   * 僅供 Database Spooling Debug 用

   * @return

   */

  public String getConnectionProxyIdentifier() {

    return this.oEmisProxy_.toString();

  }



  /*-------------------- test section-------------------------------*/



  /**

   * 針對此 Connection (Database), 此 Exception 是否為 Integrity

   * constraint violation

   * @param e

   * @return

   */

  public boolean isPKError(SQLException e) {

    return oConnector_.isPKError(e);

  }



  /**

   * 傳回資料庫的MetaData.

   * @return DatabaseMetaData

   * @throws SQLException

   */

  public DatabaseMetaData getMetaData() throws SQLException {

    return oConn_.getMetaData();

  }



  /**

   * test.

   * @throws SQLException

   */

  public void dualTest() throws SQLException {

    String sSQL = oConnector_.dualTestSQL();

    Statement stmt = oConn_.createStatement();

    try {

      ResultSet rs = stmt.executeQuery(sSQL);

      rs.close();

    } finally {

      stmt.close();

      stmt = null;

    }

  }



  /**

   * 查看 哪一支程式使用connection

   * 設定方式會動態讀取 專案.cfg的內容

   *  emis.db.log.checktranction =YES 若設定為no 則不會在resin\log\  產生log

   *  emis.db.log.checkPoolCount =5 此為產生log的條件 若db pool使用超過5 則會產生log

   * @param oContext


  private static void logConnection(ServletContext oContext) {

    try {

      //第一次讀取emisDb log 設定

      if (isGenDbLog_) {

        isGenDbLog_ = false;

        needCheckConnection_ = initLogConnection(oContext);

      }

      if (needCheckConnection_) {

        checkLogConnection(oContext);

      }

    } catch (Exception e) {

      oLogger_.error(e.getMessage());

      e.printStackTrace(System.out);

    }

  }
   */



  /**

   * 由xxx.conf中指定的emiscfg讀取xxx.cfg, 據以讀入下列變數並建立Logger:

   *   emis.db.log.enabled=true

   *   emis.db.log.checkPoolCount=9999

   *

   * @param oContext

   * @return

   * @throws IOException

   

  private static boolean initLogConnection(ServletContext oContext) throws IOException {

    needCheckConnection_ = false;

    try {


      Properties _oProps = (Properties) oContext.getAttribute("CFG_PROPS");

      if (_oProps == null) return false;


      String _sNeedCheck = _oProps.getProperty("emis.db.log.enabled");

      if (_sNeedCheck != null && "true".equalsIgnoreCase(_sNeedCheck)) {

        needCheckConnection_ = true;

      }

      if (needCheckConnection_) {

        String _sCount = _oProps.getProperty("emis.db.log.checkPoolCount");

        if (_sCount != null) {

          try {  // 字串轉數值一定要catch exception!

            iCheckPoolCount_ = Integer.parseInt(_sCount);

          } catch (NumberFormatException e) {

            ;

          }

        }


        oLogger_ = emisLogger.getlog4j(oContext,"com.emis.db.emisDb");

      }

    } catch (Exception e) {

      if (oLogger_ != null) oLogger_.error("initLogConnection failed.");

      e.printStackTrace(System.err);

    }

    return needCheckConnection_;

  }
*/


  /**

   * 檢查Connection是否已超過設定值 emis.db.log.checkPoolCount.

   * @param oContext

   * @throws Exception

   

  private static void checkLogConnection(ServletContext oContext) throws Exception {

    emisDbMgr _oMgr = emisDbMgr.getInstance(oContext);

    emisDbConnector _oConnector = _oMgr.getConnector(); // default connector

    emisComplexSpool _oSpool = (emisComplexSpool) _oConnector;

    emisSpoolSnapShot _oShot = _oSpool.getSnapShot();



    //當 emisdb 的使用數超過設定觀察數才紀錄log

    if ((_oShot.getPooledSize() + _oShot.getCheckedOutSize()) > iCheckPoolCount_) {

      emisProxyDesc _oDesc = null;

      String _sMsg = "\n";

      int _iCount = 0;

      Enumeration e = _oShot.getDescriptor();

      while (e.hasMoreElements()) {

        _oDesc = (emisProxyDesc) e.nextElement();

        _iCount++;

        _sMsg += _iCount + " 使用 emisDb 的物件[" + _oDesc.getDescription() +

            "]  開始時間: " + _oDesc.getTime() + "\n";

      }

      if (oLogger_ != null)

        oLogger_.info(_sMsg);

    }

  }
*/
  // robert 2010/05/14
  // we want to capture emisDb not closed problem
  // 先以 thread 觀點來試做
  static boolean doDbCheckPointCheck = true;
  static HashMap dbResourceCheckPointMap = new HashMap();

  // used by getInstance...
  static void increaseCheckPoint() {
	  Thread t = Thread.currentThread();
	  Integer i = (Integer) dbResourceCheckPointMap.get(t);
	  if( i != null ) {
		  i = i.intValue() + 1;
		  dbResourceCheckPointMap.put(t,i);
	  }
  }
  // used by close...
  static void decreaseCheckPoint() {
	  Thread t = Thread.currentThread();
	  Integer i = (Integer) dbResourceCheckPointMap.get(t);
	  if( i != null ) {
		  i = i.intValue() - 1;
		  dbResourceCheckPointMap.put(t,i);
	  }
  }

  public static void startCheckPoint(Object source , ServletRequest request) {

	  if( ! doDbCheckPointCheck ) return;

	  Thread t = Thread.currentThread();
	  dbResourceCheckPointMap.put(t, new Integer(0) ); // db resource count set to zero
  }

	public static void endCheckPoint(Object source, ServletRequest request) {
		if (!doDbCheckPointCheck)
			return;

		Thread t = Thread.currentThread();
		Integer iDbResourceCount = (Integer) dbResourceCheckPointMap.get(t);
		if (iDbResourceCount != null) {
			// System.out.println("db Resource count :" + iDbResourceCount );
			if (iDbResourceCount.intValue() != 0) {
				System.out.println("db Resource count not zero:"+ iDbResourceCount);
				if ((request != null)
						&& (request instanceof HttpServletRequest)) {
					HttpServletRequest h = (HttpServletRequest) request;
					System.out.println("db Resource not free in:"	+ h.getRequestURI());
				}
				if (source != null) {
					System.out.println("db Resource not free in:" + source);

				}
				StackTraceElement[] trace = (new Throwable()).getStackTrace();
				for (int i = 0; i < trace.length - 1; i++) {
					System.out.println(trace[i].toString());
				}
				trace = null;

			}
		}

	}

  /*
   *  2012/11/23 Robert, add for 個資法會員加解密
   *  Robert, don't call this....
   */
  public Connection getConnection() {
	  return oConn_;
  }

  public String getDBType(){
    return this.oConnector_.getDBType();
  }

  public ServletContext getServletContext(){
    return oContext_;
  }
}

