package com.emis.app.migration.action;

import java.sql.SQLException;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiPriceRange extends emisMiAction {
  public final String act(final String[] src, final String[] param) throws SQLException {
    if (db == null)
      initStmt("select min(pr_no) from pricerange where abs(?)  <= pr_price");
    db.setCurrentPrepareStmt(thisStmt);
    String pr_no = "";
//        try {
    db.setDouble(1, Double.parseDouble(src[0]));
    db.prepareQuery();
    if (db.next()) {
      pr_no = db.getString(1);
    }
//        } catch (SQLException e) {
//            // Log here;  //To change body of catch statement use Options | File Templates.
//        } catch (NumberFormatException e) {
//            // Log here;  //To change body of catch statement use Options | File Templates.
//        }
    return pr_no;
  }
}
