/*
 * $Header: /repository/src3/src/com/emis/db/emisDbDebug.java,v 1.1.1.1 2005/10/14 12:42:03 andy Exp $
 *
 * Copyright (c) EMIS Corp.
 * 2003/12/24 Jerry: 由emisDb修改, 以方便產生帶有問號的敘述成為有值的敘述.

   emisDbDebug _oDb = emisDbDebug.getInstance(oContext_);
   _oDb.prepareStmt("select .... ? and ?...");
   oDb_.setString(1, "123");
   ...
   _oDb.getSQL();  // 傳回已填入值的SQL statement
 */
package com.emis.db;

import com.emis.util.emisUtil;

import javax.servlet.ServletContext;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;

public class emisDbDebug {
/*-------------------------all variables-------------------------*/
  private HashMap hmSets = new HashMap();
  private ServletContext oContext_;

  private ResultSet oRs_;
  private ResultSetMetaData oMeta_;

  // 放所有的 prepare Statement
  private HashMap oAllResource_ = new HashMap();

  //- Debug
  emisDb oDb_;
  String sSQL_ = "";

  private emisDbDebug(ServletContext oContext) throws Exception {
    oContext_ = oContext;
    oDb_ = emisDb.getInstance(oContext);
  }

  public static emisDbDebug getInstance(ServletContext oContext) throws Exception {
    return new emisDbDebug(oContext);
  }

  public void setCurrentResultSet(ResultSet oRs) throws SQLException {
    oDb_.setCurrentResultSet(oRs);
  }

  public boolean getMoreResults() throws SQLException {
    return oDb_.getMoreResults();
  }

  public ResultSet getResultSet() throws SQLException {
    oRs_ = oDb_.getResultSet();
    return oRs_;
  }

  public PreparedStatement prepareStmt(String sSQL) throws SQLException {
    sSQL_ = sSQL;
    return oDb_.prepareStmt(sSQL);
  }

  public PreparedStatement prepareStmt(String sSQL, int resultSetType, int resultSetConcurrency) throws SQLException {
    return oDb_.prepareStmt(sSQL, resultSetType, resultSetConcurrency);
  }

  public void setCurrentPrepareStmt(PreparedStatement oPrep) {
    oDb_.setCurrentPrepareStmt(oPrep);
  }

  public void clearParameters() throws SQLException {
    oDb_.clearParameters();
  }

  public int prepareUpdate() throws SQLException {
    return oDb_.prepareUpdate();
  }

  public ResultSet prepareQuery() throws SQLException {
    return oDb_.prepareQuery();
  }

  public void clearPrepareStmtBatch() throws SQLException {
    oDb_.clearPrepareStmtBatch();
  }

  public int[] prepareExecuteBatch() throws SQLException {
    return oDb_.prepareExecuteBatch();
  }

  public void prepareAddBatch() throws SQLException {
    oDb_.prepareAddBatch();
  }

  public void setString(int nNum, String sStr) throws SQLException {
    hmSets.put("" + nNum, "'" + sStr + "'");  // ("1", setting") 供取SQL敘述用
    oDb_.setString(nNum, sStr);
  }

  public void setLong(int nNum, long l) throws SQLException {
    hmSets.put("" + nNum, "" + l);  // ("1", "setting") 供取SQL敘述用
    oDb_.setLong(nNum, l);
  }

  public void setFloat(int nNum, Float oFloat) throws SQLException {
    hmSets.put("" + nNum, oFloat);  // ("1", "setting") 供取SQL敘述用
    oDb_.setFloat(nNum, oFloat.floatValue());
  }

  public void setFloat(int nNum, float nFloat) throws SQLException {
    hmSets.put("" + nNum, ""+nFloat);  // ("1", "setting") 供取SQL敘述用
    oDb_.setFloat(nNum, nFloat);
  }

  public void setDouble(int nNum, Double oDouble) throws SQLException {
    hmSets.put("" + nNum, oDouble);  // ("1", "setting") 供取SQL敘述用
    oDb_.setDouble(nNum, oDouble.doubleValue());
  }

  public void setDouble(int nNum, double value) throws SQLException {
    hmSets.put("" + nNum, "" + value);  // ("1", type+"setting") 供取SQL敘述用
    oDb_.setDouble(nNum, value);
  }

  public void setBigDecimal(int nNum, BigDecimal dec) throws SQLException {
    hmSets.put("" + nNum, "" + dec);  // ("1", type+"setting") 供取SQL敘述用
    oDb_.setBigDecimal(nNum, dec);
  }

  public void setInt(int nNum, int nValue) throws SQLException {
    hmSets.put("" + nNum, "" + nValue);  // ("1", type+"setting") 供取SQL敘述用
    oDb_.setInt(nNum, nValue);
  }

  public void setNull(int nNum, int nSqlType) throws SQLException {
    oDb_.setNull(nNum, nSqlType);
  }

  public void closePrepareStmt(PreparedStatement oPrep) {
    try {
      oDb_.closePrepareStmt(oPrep);
      if (oPrep != null) oPrep.close();
    } catch (Exception ignore) {
    }
  }

  public void registerOutParameter(int nNum, int nSQLType) throws SQLException {
    oDb_.registerOutParameter(nNum, nSQLType);
  }

  public boolean next() throws SQLException {
    return oDb_.next();
  }

  public boolean isAfterLast() throws SQLException {
    return oDb_.isAfterLast();
  }

  public boolean isBeforeFirst() throws SQLException {
    return oDb_.isBeforeFirst();
  }

  public boolean previous() throws SQLException {
    return oDb_.previous();
  }

  public boolean first() throws SQLException {
    return oDb_.first();
  }


  public boolean last() throws SQLException {
    return oDb_.last();
  }

  public boolean absolute(int i) throws SQLException {
    return oDb_.absolute(i);
  }

  public int getRow() throws SQLException {
    return oDb_.getRow();
  }

  public int getColumnCount() throws SQLException {
    return oMeta_.getColumnCount();
  }

  public void setAutoCommit(boolean auto) throws SQLException {
    oDb_.setAutoCommit(auto);
  }

  /**
   * Connection 做 RollBack
   */
  public void rollback() throws SQLException {
    oDb_.rollback();
  }

  /**
   * Connection 做 commit 動作
   */
  public void commit() throws SQLException {
    oDb_.commit();
  }

  public void close() {
    oDb_.close();
  }

  public String getSQL() {
    String _sSQL = sSQL_;
    for (int i = 1; i <= hmSets.size(); i++) {
      String _sKey = "" + i;
      String _sValue = (String) hmSets.get(_sKey);
      _sSQL = emisUtil.stringReplace(_sSQL, "?", _sValue, "i");
    }
    return _sSQL;
  }
}
