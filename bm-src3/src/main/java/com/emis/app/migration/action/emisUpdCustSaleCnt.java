package com.emis.app.migration.action;

import java.sql.PreparedStatement;
import java.sql.SQLException;

//import temp.test.*;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisUpdCustSaleCnt extends emisMiAction {
  private PreparedStatement updCust;
  private PreparedStatement insCust;
  private final emisMiSLKey actSLKey = new emisMiSLKey();
  final String[] srcSLKey = new String[4];

  /**
   * 累計顧客消費次數並更新顧客最近消費日期
   *
   * @param src   傳入所有參照到的欄位字串值
   *              依序為  C_NO, SL_DATE
   * @param param is not used in this class
   * @return always returns null
   */
  public final String act(final String[] src, final String[] param) throws SQLException {
    if (db == null) {
      initStmt("select c_no from sale_h where sl_key=?");
    }
    System.arraycopy(src, 2, srcSLKey, 0, 4);
    final String sl_key = actSLKey.act(srcSLKey, param);
    if (this.doQuery(new String[]{sl_key}).length() != 0) // 本筆銷售資料已經存在
      return null;                                    // 不可重複更新會員銷售資料
    final String c_no = src[0].trim();
    String sl_date = src[1].trim();
    sl_date = (Integer.parseInt(sl_date.substring(0, 4)) - 1911) + sl_date.substring(4);
    if (sl_date.length() == 6)
      sl_date = "0" + sl_date;
    if (c_no == null || c_no.length() == 0)
      return null;
    if (updCust == null) {
      updCust = initUpdateCust("update cust set C_YCNT=C_YCNT+1 ,  C_TCNT=C_TCNT+1, C_TRAN_D=? where c_no=?");
    }
//        try {
    db.setCurrentPrepareStmt(updCust);
    db.setString(1, sl_date);
    db.setString(2, c_no);
    if (db.prepareUpdate() == 0) {
      if (insCust == null) {
        updCust = initUpdateCust("insert into cust (C_YCNT, C_TCNT, C_TRAN_D , C_NO) " +
          "values  ( 1, 1, ?, ?) ");
      }
    }
//        } catch (SQLException e) {
//            // Log here;  //To change body of catch statement use Options | File Templates.
//        }
//        u	Cust?C_YCNT(今年消費次數) = Cust.C_YCNT+1
//        u	Cust?C_TCNT(累計消費次數) = Cust.C_TCNT + 1
    return null;
  }

  private PreparedStatement initUpdateCust(final String sSQL) {
    PreparedStatement stmt = null;
    try {
      stmt = config.getDb().prepareStmt(sSQL);
    } catch (SQLException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
    }
    return stmt;
  }
}
