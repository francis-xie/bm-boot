package com.emis.app.migration;
/*$Header: /repository/src3/src/com/emis/app/migration/ePosGroupCreationRule.java,v 1.1 2006/01/10 16:05:54 andy Exp $
  2005/12/09  mike epos 專用
Dana 2012/02/28 修改门店抓vCCRStores,方便针对门店增加过滤条件,不用每次都改java
*/

import com.emis.db.emisDb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

public class    ePosGroupCreationRule extends emisCreationRule {

  Hashtable hComp = null;

  Hashtable hGroup = null;



  void init() throws Exception {
    hComp = new Hashtable();

    hGroup = new Hashtable();

    emisDb db = null;

    try {

      db = emisDb.getInstance(config.getContext());

      db.executeQuery("select S_NO from vCCRStores s  order by S_NO ");

      while (db.next()) {
        String s_no = db.getString(1);
        ArrayList sS_NO;
        if ((sS_NO = (ArrayList) hComp.get("00")) == null) {
          sS_NO = new ArrayList();
          hComp.put("00", sS_NO);
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



  public ePosGroupCreationRule(emisMiConfig config) {

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

