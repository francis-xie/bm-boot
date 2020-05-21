/**
 * 抓取採購( PO) 單資訊
 * User: shaw
 * Date: Jul 14, 2003
 * Time: 2:28:59 PM
 *
 */
/*
package com.emis.app.migration.action;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

public class emisMiGetPoDetlInfo extends emisMiAction {
  private static HashMap sessionMap = new HashMap();
  private static ResultSet record=null;
  private Object SessionData[];
  private int sessionId[] = new int[1];
  public String act(String[] src, String[] param) {
    Thread t = Thread.currentThread();
    String sql = "select * from po_detl where PO_NO = 'PO' + ? and S_NO = ? and P_NO = ?";
    if (db==null) {
        initStmt(sql);
    }
    boolean UseOldData = false;
    SessionData = (Object[]) sessionMap.get(t);
    if (SessionData!=null) {
        int oldId[] = (int[])SessionData[0];
        int oldSession = oldId[0];
        if (oldSession == session)
             UseOldData = true;
    }
      String value = "";
//      try {
          if (UseOldData) {
              record = (ResultSet) SessionData[1];
             value = record.getString(param[0]);
           } else {
              db.setCurrentPrepareStmt(this.thisStmt);
              db.setString(1, src[0]);
              db.setString(2, src[1]);
              db.setString(3, src[2]);
              record = db.prepareQuery();
              if (!record.next()) {
                  return "";
              } else {
                  SessionData = new Object[2];
                  sessionId[0] = session;
                  SessionData[1] = record;
                  SessionData[0] = sessionId;
                  sessionMap.put(t, SessionData);
                  value = record.getString(param[0]);
              }
          }
//      } catch (SQLException e) {
//          // Log here;
//      }
      return value;
  }
}
*/
/**
 * 抓取採購( PO) 單資訊
 * User: shaw
 * Date: Jul 14, 2003
 * Time: 2:28:59 PM
 *
 */
package com.emis.app.migration.action;

import java.util.StringTokenizer;

public final class emisMiGetPoDetlInfo extends emisMiAction {
//  public String act(String[] src, String[] param, int session) {
  public final String act(final String[] src, final String[] param) {
    final StringTokenizer token = new StringTokenizer(param[0], ".");
    final String[] s = {null, null};
    for (int i = 0; token.hasMoreTokens(); i++) {
      s[i] = token.nextToken();
    }
    final String sql = "select " + s[1] + " from " + s[0] + " where PO_NO = 'PO' + ? and S_NO = ? and P_NO = ?";
    this.initStmt(sql);
    final String value = this.doQuery(src);
    return value;
  }
}
