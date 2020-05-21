/* $Id: emisAbstractTestCase.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (C) EMIS Corp.
 */
package com.emis.test;

import com.emis.qa.emisServletContext;
import com.emis.server.emisServerFactory;
import com.emis.db.emisDb;
import com.emis.report.emisString;
import junit.framework.TestCase;

import javax.servlet.ServletContext;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

/**
 * 操作JUnit的抽像類別, 使叫用的類別不用建立ServletContext與EMIS Server.
 * @author Jerry
 * @version 2004/06/21
 * @version 2004/06/28 Jerry 1.1: 將emiscfg參數放入InitParam內
 * @version 2004/06/28 Jerry 1.2: 將ResourceBean push到emisAbstractPBOTestCase;改package
 * @version 2004/07/07 Jerry 1.3: query等SQL函數移至此層
 */
public class emisAbstractTestCase extends TestCase {
  /** ServletContext */
  protected ServletContext oContext_;
  protected emisDb oDb_;
  /** Resource bean */
  //protected emisBusinessResourceBean oResourceBean_;

  /**
   * setUp.
   * @param sRoot
   * @param sCfgFile
   * @throws Exception
   */
  protected void setUp(String sRoot, String sCfgFile) throws Exception {
    super.setUp();
    oContext_ = new emisServletContext();
    ((emisServletContext) oContext_).setInitParam("emiscfg", sCfgFile);
    // createServer最後參數eventBoot用來控制是否用另一個Thread來啟動Server第二階段啟動
    //  (透過startup()), 設成false才會以直接叫用而非另一Thread的方法來啟動, 方便測試.
    emisServerFactory.createServer(oContext_, sRoot, sCfgFile, false);

    // 有程式需要此Loader, 因此建立一個起來
    oContext_.setAttribute("caucho.class-loader",
        Thread.currentThread().getContextClassLoader());

    oDb_ = emisDb.getInstance(oContext_);
  }

  /**
   * setUp.
   * @param sPrjID
   * @throws Exception
   */
  protected void setUp(String sPrjID) throws Exception {
    super.setUp();
    this.setUp("c:\\wwwroot\\"+sPrjID, "c:\\resin\\"+sPrjID+".cfg");
  }

  /**
   * setUp.
   * @throws Exception
   */
  protected void setUp() throws Exception {
    super.setUp();
    this.setUp("c:\\wwwroot\\eros", "c:\\resin\\eros.cfg");
  }

  /**
   * shutdown.
   * @throws Exception
   */
  protected void tearDown() throws Exception {
    super.tearDown();
    if (oDb_ != null)
      oDb_.close();
  }

  /**
   * 將傳入的SQL敘述與資料參數做 Query, 並將結果輸出到console.
   * @param sSQL
   * @param aKeys
   */
  protected void query(String sSQL, String[] aKeys) {
    try {
      emisDb _oDb = oDb_;  // MyResourceBean.getEmisDb();
      _oDb.prepareStmt(sSQL);
      if (aKeys != null) {
        for (int i = 1; i <= aKeys.length; i++) {
          _oDb.setString(i, aKeys[i - 1]);
        }
      }
      ResultSet _oRS = _oDb.prepareQuery();
      while (_oDb.next()) {
        printFieldData(_oDb, _oRS);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * 顯示SQL查詢結果.
   * @param sSQL
   */
  protected void query(String sSQL) {
    query(sSQL, null);
  }

  /**
   * 將傳入的SQL敘述與資料參數做 Query, 並將結果輸出到console.
   * @param sSQL
   * @param aKeys
   * @param isMultiRows 是否顯示成多列
   */
  protected void query(String sSQL, String[] aKeys, boolean isMultiRows) {
    try {
      emisDb _oDb = oDb_;  // MyResourceBean.getEmisDb();
      _oDb.prepareStmt(sSQL);
      if (aKeys != null) {
        for (int i = 1; i <= aKeys.length; i++) {
          _oDb.setString(i, aKeys[i - 1]);
        }
      }
      ResultSet _oRS = _oDb.prepareQuery();
      ResultSetMetaData _oMeta = _oRS.getMetaData();
      int _iCount = _oMeta.getColumnCount();
      for (int i = 1; i <= _iCount; i++) {
        String _sName = _oMeta.getColumnName(i);
        int _iWidth = Math.max(_oMeta.getPrecision(i), _sName.length());
        System.out.print(emisString.rPadB(_sName, _iWidth) + " ");
      }
      System.out.println("");

      for (int i = 1; i <= _iCount; i++) {
        String _sName = _oMeta.getColumnName(i);
        int _iWidth = Math.max(_oMeta.getPrecision(i), _sName.length());
        System.out.print(emisString.replicate('-', _iWidth) + " ");
      }
      System.out.println("");

      while (_oDb.next()) {
        printFieldDataMultiRows(_oDb, _oRS);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * 將select的欄位逐一輸出, 每欄一列, 以 . 結尾, 以辨識是否有結尾空白.
   * @param oDb
   * @param oRS
   */
  private void printFieldData(emisDb oDb, ResultSet oRS) {
    try {
      ResultSetMetaData _oMeta = oRS.getMetaData();
      for (int i = 1; i <= _oMeta.getColumnCount(); i++) {
        System.out.println("  " + i + " " + _oMeta.getColumnName(i) + "=" + oDb.getString(i) + ".");
      }
      System.out.println("--------------------");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * 將select的欄位逐一輸出, 每欄一列, 以 . 結尾, 以辨識是否有結尾空白.
   * @param oDb
   * @param oRS
   */
  private void printFieldDataMultiRows(emisDb oDb, ResultSet oRS) {
    try {
      ResultSetMetaData _oMeta = oRS.getMetaData();
      for (int i = 1; i <= _oMeta.getColumnCount(); i++) {
        String _sName = _oMeta.getColumnName(i);
        String _sValue = oDb.getString(i);
        if (_sValue == null) _sValue = "null";
        int _iWidth = Math.max(_oMeta.getPrecision(i), _sName.length());
        System.out.print(emisString.rPadB(_sValue, _iWidth) + " ");
      }
      System.out.println("");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
