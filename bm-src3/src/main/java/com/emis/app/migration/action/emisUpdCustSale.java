package com.emis.app.migration.action;

import java.sql.PreparedStatement;
import java.sql.SQLException;

//import temp.test.*;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisUpdCustSale extends emisMiAction {
  private final emisMiSLKey actSLKey = new emisMiSLKey();
  final String[] str = {null, null, null, "1"};//new String[3];
  final String[] part_s = new String[2];
  private PreparedStatement updCust = null;
//    private PreparedStatement insCust;
  private emisMiGetPartSCost actGetPartSCost;

  /**
   * @param src   傳入所有參照到的欄位字串值
   *              依序為 ID_NO, SL_DATE, SL_NO, SL_AMT, SL_QTY, P_NO, S_NO
   * @param param is not used in this class
   * @return always returns null
   */
  public final String act(final String[] src, final String[] param) throws SQLException {
    if (db == null)
      initStmt("select c_no from sale_h where sl_key=?");
    if (actGetPartSCost == null) {
      actGetPartSCost = new emisMiGetPartSCost();
      actGetPartSCost.setConfig(config);
    }
    System.arraycopy(src, 0, str, 0, 4);
    final String[] slkey = {actSLKey.act(str, null)};
    final String c_no = this.doQuery(slkey);
    if (c_no == null || c_no.length() == 0)
      return null;
    final boolean recordExists;

//        try {
    db.prepareStmt("select p_no from sale_d where sl_key=? and recno=?");
    db.setString(1, slkey[0]);
    db.setString(2, src[8]);
    db.prepareQuery();
    recordExists = db.next();
//        } catch (SQLException e) {
//            // Log here;  //To change body of catch statement use Options | File Templates.
//        }
    if (recordExists)
      return null;

    part_s[0] = src[6].trim();
    part_s[1] = src[7].trim();
    final String sCost = actGetPartSCost.act(part_s, null);

    if ("".equals(c_no))
      return null;
    if (updCust == null) {
      updCust = initUpdateCust("update cust set C_YAMT=C_YAMT+?, C_YGP=C_YGP+?,  C_TAMT = C_TAMT+?, " +
        "C_TGP=C_TGP+? ,  C_TRAN_D=? where c_no=?");
    }
    final float sl_amt = Float.parseFloat(src[4]);
    final float sl_cost = Float.parseFloat(sCost);
    final float sl_taxamt = Float.parseFloat(src[5]);
    String sl_date = src[1].trim();
    sl_date = (Integer.parseInt(sl_date.substring(0, 4)) - 1911) + sl_date.substring(4);
    if (sl_date.length() == 6)
      sl_date = "0" + sl_date;
    try {
      db.setCurrentPrepareStmt(updCust);
      db.setFloat(1, sl_amt);
      final float gp = sl_amt - sl_taxamt - sl_cost;
      db.setFloat(2, gp);
      db.setFloat(3, sl_amt);
      db.setFloat(4, gp);
      db.setString(5, sl_date);
      db.setString(6, c_no);
      if (db.prepareUpdate() == 0) {
      }
    } catch (SQLException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
    }
//        u	Cust.C_YAMT(今年消費額)  =  Cust.C_YAMT + Sale_d.SL_AMT
//        u	Cust.C_YGP(今年毛利) = Cust.C_YGP +( Sale_d.SL_AMT - Sale_d.SL_TAXAMT–Sale_d.SL_COST)
//        u	Cust?C_YCNT(今年消費次數) = Cust?C_YCNT+1
//        u	Cust?C_TAMT (累計消費) = Cust?C_TAMT+ Sale_d.SL_AMT
//        u	Cust?C_TGP(累計毛利) = Cust?C_TGP +( Sale_d.SL_AMT - Sale_d.SL_TAXAMT–Sale_d.SL_COST)
//        u	Cust?C_TCNT(累計消費次數) = Cust?C_TCNT + 1
//        u	Cust?C_TRAN_D(最後交易日) = Sale_d.SL_DATE
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
