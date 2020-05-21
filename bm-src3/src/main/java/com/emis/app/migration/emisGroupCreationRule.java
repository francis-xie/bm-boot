package com.emis.app.migration;

import com.emis.db.emisDb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * User: merlin
 * Date: Apr 30, 2003
 * Time: 9:57:27 AM
 */
public class    emisGroupCreationRule extends emisCreationRule {
  Hashtable hComp = null;
  Hashtable hGroup = null;

  void init() throws Exception {
    hComp = new Hashtable();
    hGroup = new Hashtable();
    emisDb db = null;
    try {
      db = emisDb.getInstance(config.getContext());
      db.executeQuery("select com_NO,S_NO from store s  order by com_no ,S_NO ");
      while (db.next()) {
        String com_no = db.getString(1);
        String s_no = db.getString(2);
        ArrayList sS_NO;
        if ((sS_NO = (ArrayList) hComp.get(com_no)) == null) {
          sS_NO = new ArrayList();
          hComp.put(com_no, sS_NO);
        }
        sS_NO.add(s_no);
      }
      db.executeQuery("select G_NO, S_NO from GROUP_S  order by G_NO");
      while (db.next()) {
        String gr_no = db.getString(1);
        String s_no = db.getString(2);
        ArrayList sS_NO;
        if ((sS_NO = (ArrayList) hGroup.get(gr_no)) == null) {
          sS_NO = new ArrayList();
          hGroup.put(gr_no, sS_NO);
        }
        sS_NO.add(s_no);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      if (db != null)
        db.close();
    }
  }

  public emisGroupCreationRule(emisMiConfig config) {
    super(config);
  }

  public String[] getConfigPath(String[] args) throws Exception {
    if (args == null || args.length == 0)
      return null;
    if (hGroup == null)
      init();
    ArrayList sS_NO;
    if (args[0] != null && args[0].length() > 0)
      return new String[]{args[0]};
    if (args[1] != null && args[1].length() > 0)
      sS_NO = (ArrayList) hGroup.get(args[1]);
    else
      sS_NO = (ArrayList) hComp.get(args[2]);
    if (sS_NO == null)
      return null;
    String[] result = new String[sS_NO.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = (String) sS_NO.get(i);
    }
    return result;
  }
}
