/* $Id: emisRptDataSrc.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) EMIS Corp. All Rights Reserved.
 */
package com.emis.report;

import com.emis.messageResource.Messages;
import com.emis.business.emisDataSrc;
import com.emis.db.emisDb;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Report Data Source.
 * @author KC & James
 * @version $Revision: 71496 $
 */
public class emisRptDataSrc {
  /** Data source */
  protected emisDataSrc oDataSrc_;
  /** emisDb */
  protected emisDb oReportDb_;
  /** fields list */
  protected ArrayList alDataFields_;

  /**
   * Constructor.
   */
  public emisRptDataSrc() {
  }

  /**
   * Constructor.
   * @param oProvider
   * @param sDataSrcName
   */
  public emisRptDataSrc(emisRptProvider oProvider, String sDataSrcName) {
    try {
      if (sDataSrcName == null) {
        sDataSrcName = "xmlData"; //$NON-NLS-1$
      }

      oDataSrc_ = oProvider.getDataSrc(sDataSrcName);
      oReportDb_ = oDataSrc_.processSQL();
    } catch (Exception e) {
      throw new RuntimeException(" emisRptDataSrc: " + //$NON-NLS-1$
          e.getMessage() + "\n"); //$NON-NLS-1$
    }
  }

  /**
   *
   * @return
   * @throws SQLException
   */
  public boolean next() throws SQLException {

    if (oReportDb_ == null)
      throw new SQLException("Null ResultSet"); //$NON-NLS-1$

    if (this.oReportDb_.next()) {
      fillValues();
    } else
      return false;

    return true;
  }

  /**
   * Move the cursor of recordset to previous from current cursor
   *
   * @return
   * @throws SQLException
   */
  public boolean previous() throws SQLException {
    if (oReportDb_ == null)
      throw new SQLException("NULL ResultSet"); //$NON-NLS-1$

    if (oReportDb_.previous())
      fillValues();
    else
      return false;

    return true;
  }

  /**
   * Move the cursor of recordset to previous from current cursor
   *
   * @param iScrollRows
   * @return
   * @throws SQLException
   */
  public boolean previous(int iScrollRows) throws SQLException {
    int _iRowNumber;
    int _iAbsolute;
    int _iShift = 0;

    if (oReportDb_ == null)
      throw new SQLException("NULL ResultSet"); //$NON-NLS-1$

    if (iScrollRows != 0) {
      if (isAfterLast()) {
        oReportDb_.previous();
        _iShift = 1;
      } else {
        _iShift = 0;
      }
      _iRowNumber = oReportDb_.getRow();
      _iAbsolute = _iRowNumber - iScrollRows + _iShift;
      if (_iAbsolute <= 0)
        return false;
      if (oReportDb_.absolute(_iAbsolute))
        fillValues();
      else
        return false;
    }
    return true;
  }

  /**
   *
   * @return
   * @throws SQLException
   */
  public boolean first() throws SQLException {
    if (oReportDb_ == null)
      throw new SQLException("Null ResultSet"); //$NON-NLS-1$

    if (oReportDb_.first())
      fillValues();
    else
      return false;

    return true;
  }

  /**
   *
   * @return
   * @throws SQLException
   */
  public boolean last() throws SQLException {
    if (oReportDb_ == null)
      throw new SQLException("NULL ResultSet"); //$NON-NLS-1$

    if (oReportDb_.last())
      fillValues();
    else
      return false;

    return true;
  }

  /**
   * get Column count.
   * @return
   * @throws SQLException
   */
  public int getColumnCount() throws SQLException {
    ResultSetMetaData _oMetaData;
    int iColumnCnt_;
    if (oReportDb_ == null)
      throw new SQLException("NULL ResultSet"); //$NON-NLS-1$

    iColumnCnt_ = oReportDb_.getColumnCount();
    return iColumnCnt_;
  }

  /**
   *
   * @param iColumnIndex
   * @return
   * @throws SQLException
   */
  public String getData(int iColumnIndex) throws SQLException {
    Object _oDataValue;
    String _sDataType;
    String _sReturnValue = ""; //$NON-NLS-1$
    if (oReportDb_ == null)
      throw new SQLException("NULL ResultSet"); //$NON-NLS-1$

    _oDataValue = oReportDb_.getObject(iColumnIndex);
    _sReturnValue = (String) _oDataValue;

    return _sReturnValue;
  }

  /**
   *
   * @param sColumnName
   * @return
   * @throws SQLException
   */
  public String getData(String sColumnName) throws SQLException {
    Object _oDataValue;
    String _sReturnValue;
    if (oReportDb_ == null)
      throw new SQLException("NULL ResultSet"); //$NON-NLS-1$

    _oDataValue = oReportDb_.getString(sColumnName);

    _sReturnValue = (String) _oDataValue;

    return _sReturnValue;
  }

  /**
   * Binding ReportField into EmisRptDataSrc.
   * If user moved the cursor of resultset of EmisRptDataSrc,
   * EmisRptDataSrc will refresh the field's data automatically.
   *
   * @param oField
   * @return
   */
  public boolean registerField(emisRptField oField) {
    String _sColumnName = oField.getDataField();
    if (_sColumnName != null) {
      if (alDataFields_ == null) {
        alDataFields_ = new ArrayList();
      }
      alDataFields_.add(oField);
    } else {
      throw new RuntimeException("->emisRptDataSrc.registerField error:" + //$NON-NLS-1$
          oField.getName() + Messages.getString("emisRptDataSrc.13")); //$NON-NLS-1$
    }

    return true;
  }

  /**
   * Refresh Registed emisRptField.
   * @return
   * @throws SQLException
   */
  public boolean fillValues() throws SQLException {
    String _sTmpKeyField;
    emisRptField _oField;
    String _sTmpValue;

    if (oReportDb_ == null) {
      throw new SQLException("Null ResultSet"); //$NON-NLS-1$
    }

    if (alDataFields_ != null) {
      int _iSize = alDataFields_.size();
      for (int i = 0; i < _iSize; i++) {
        _oField = (emisRptField) alDataFields_.get(i);
        _sTmpKeyField = _oField.getDataField();
        try {
          _sTmpValue = oReportDb_.getString(_sTmpKeyField);
          _oField.cacheContent(_sTmpValue);
        } catch (SQLException e) {
          throw new SQLException("  emisRptDataSrc.fillValue(): [" + //$NON-NLS-1$
              _sTmpKeyField + Messages.getString("emisRptDataSrc.16")); //$NON-NLS-1$
        }
      }
    }

    return true;
  }

  /**
   *
   * @return
   */
  public boolean commitValues() {
    String _sTmpKeyField;
    emisRptField _oField;
    String _sTmpValue;

    if (alDataFields_ != null) {
      int _iSize = alDataFields_.size();
      for (int i = 0; i < _iSize; i++) {
        _oField = (emisRptField) alDataFields_.get(i);
        _oField.commitContent();
      }
    }

    return true;
  }

  /**
   *
   * @return
   * @throws SQLException
   */
  public boolean isAfterLast() throws SQLException {
    return oReportDb_.isAfterLast();
  }

  /**
   *
   * @throws SQLException
   */
  public void close() throws SQLException {
    oReportDb_.close();
  }

  /**
   *
   * @param oField
   * @return
   */
  public boolean deregisterField(emisRptField oField) {
    String _sObjectName = oField.getName();
    emisRptField _oField;

    if (_sObjectName != null) {
      int _iSize = alDataFields_.size();
      for (int i = 0; i < _iSize; i++) {
        _oField = (emisRptField) alDataFields_.get(i);
        if (_sObjectName.equals(_oField.getName())) {
          alDataFields_.remove(i);
          break;
        }
      }
    } else {
      throw new RuntimeException("->emisRptDataSrc.deregisterField error:" + //$NON-NLS-1$
          oField.getName() + Messages.getString("emisRptDataSrc.18")); //$NON-NLS-1$
    }

    return true;
  }

  public emisDb getReportDb() {
    return oReportDb_;
  }

  /**
   *
   * @param sSqlCommand
   * @return
   * @throws SQLException
   */
  public ResultSet executeQuery(String sSqlCommand) throws SQLException {
    if (oReportDb_ == null)
      throw new SQLException("Null SQLExecuter"); //$NON-NLS-1$

    return oReportDb_.executeQuery(sSqlCommand);
  }
}

/*
  public String sysToDb(String sStr)  {
    if( sStr == null ) return null;
    try {
        sStr = new String( sStr.getBytes("ISO8859_1"),"UTF-8");
    } catch( Exception ignore) {
        System.err.println("[sysToDb] "+ ignore.toString());
    }
        return sStr;
  }
}
//測試用DataBase模擬 emisDb
//因emisDb的FetchDirection是FOWARD_ONLY
class ReportDb{
  private ResultSet oResultSet_;
  private Connection oConn_;
  private Statement oSqlStatment_;

  public ReportDb(){
    try {
      String _sUrl = "jdbc:oracle:thin:@192.168.16.200:1521:POS";
      String _sDriverName = "oracle.jdbc.driver.OracleDriver" ;
      int _iCnt ;
      Class.forName(_sDriverName ) ;

      oConn_ = DriverManager.getConnection(_sUrl,"pos","pos") ;

      oSqlStatment_ = oConn_.createStatement(
                          ResultSet.TYPE_SCROLL_INSENSITIVE,
                          ResultSet.CONCUR_READ_ONLY);

    } catch(Exception e){System.out.println(e.getMessage());
    }  finally {}
  }
  public ResultSet getResultSet() throws SQLException{
    return oResultSet_;
  }
  public ResultSet executeQuery( String sSqlCommand) throws SQLException{

    oResultSet_ = oSqlStatment_.executeQuery(sSqlCommand);
    return oResultSet_;
  }
  public void close() throws SQLException {
    oConn_.close();
  }
}
*/
