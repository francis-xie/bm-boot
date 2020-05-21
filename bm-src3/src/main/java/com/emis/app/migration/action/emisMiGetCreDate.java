package com.emis.app.migration.action;

import com.emis.util.emisDate;

import javax.servlet.ServletContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * User: shaw
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiGetCreDate extends emisMiAction {
//    private emisMiSeq action = new emisMiSeq();
  final String[] sFlow = {"SHIP"};
  private PreparedStatement stmt;
  private PreparedStatement stmt2;

  public final String act(final String[] src, final String[] param) throws SQLException {
    // 判斷是否要反建
    // 反建條件 : 進貨門市 + PO_NO + SH_CTN_NO
    if (db == null) {
      db = config.getDb();
      final String sql = " select SH_NO, FLS_NO, CRE_DATE from  SHIP_H where s_no_in = ? and PO_NO='PO'+? and  SH_CTN_NO = ? and s_no_out=? and fls_no != 'CL'";
      final String sql2 = " select FLS_NO from  PO where s_no = ? and PO_NO='PO'+?";
//            try {
      stmt = db.prepareStmt(sql);
      stmt2 = db.prepareStmt(sql2);
//            } catch (SQLException e) {
//                // Log here;  //To change body of catch statement use Options | File Templates.
//            }
//            action.setConfig(this.config);
    }
    String fls_no = "";
    String sh_no = null;
    boolean notExists = false;
    final ServletContext context = this.config.getContext();
    String s_no_out = (String) context.getAttribute("S_NO");
    String Cre_date = "";
    if (s_no_out == null) s_no_out = "A1001";
    try {
      db.setCurrentPrepareStmt(stmt);
      db.setString(1, src[0]);
      db.setString(2, src[1]);
      db.setString(3, src[2]);
      db.setString(4, s_no_out);
      db.prepareQuery();
      if (db.next()) {
        fls_no = db.getString("FLS_NO");
        sh_no = db.getString("SH_NO");
        Cre_date = db.getString("CRE_DATE");
      } else {
        notExists = true;
        Cre_date = new emisDate(false).toString();
        ;
      }
    } catch (SQLException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
    }
    return Cre_date;
  }
}

