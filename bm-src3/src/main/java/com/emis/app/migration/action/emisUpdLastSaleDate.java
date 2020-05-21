package com.emis.app.migration.action;

import com.emis.util.emisDate;

import java.sql.SQLException;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisUpdLastSaleDate extends emisMiAction {
  /**
   * @param src   傳入所有參照到的欄位字串值
   *              依序為 S_NO, P_NO, SL_DATE
   * @param param is not used in this class
   * @return always returns null
   */
  public final String act(final String[] src, final String[] param) throws SQLException {
    if (db == null) {
      db = this.config.getDb();
//            try {
      this.thisStmt = db.prepareStmt(" update part_s set ps_sl_d=? where s_no=? and p_no =? and " +
        " (ps_sl_d <? or ps_sl_d is null)");
//            } catch (SQLException e) {
//                // Log here;  //To change body of catch statement use Options | File Templates.
//            }
    }
    db.setCurrentPrepareStmt(thisStmt);
    try {
      final String _sS_NO = src[0].trim();
      final String _sP_NO = src[1].trim();
      String _sSL_Date = "";
      try {
        _sSL_Date = new emisDate(src[2].trim()).toString(false);
      } catch (Exception e) {
      	e.printStackTrace();
        // Log here;  //To change body of catch statement use Options | File Templates.
      }
      db.setString(1, _sSL_Date);
      db.setString(2, _sS_NO);
      db.setString(3, _sP_NO);
      db.setString(4, _sSL_Date);
      db.prepareUpdate();
    } catch (SQLException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
    }
    return null;
  }
}
