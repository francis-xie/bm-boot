package com.emis.app.migration;



//import com.emis.app.exception.emisSqlException;

import com.emis.db.emisDb;



import java.sql.SQLException;

import java.util.ArrayList;



/**

 * User: merlin

 * Date: Apr 30, 2003

 * Time: 9:57:27 AM
 * Track+[8514] fang.liu 2007/05/11 當門市狀態為3.倉庫時，在自動排程MIGRATION中不產生該門市的基本資料

 */

public class emisAllStoreCreationRule extends emisCreationRule {

  ArrayList aryS_NO = null;

//    Hashtable hGroup = null;

  void init() throws Exception {

    aryS_NO = new ArrayList();

    emisDb db = null;

    try {

      db = emisDb.getInstance(config.getContext());

      db.executeQuery("select S_NO from vCCRStores ");

      while (db.next()) {

        String s_no = db.getString(1);

        aryS_NO.add(s_no);

      }

    } catch (SQLException e) {

        throw e;

    } finally {

      if (db != null)

        db.close();

    }

  }



  public emisAllStoreCreationRule(emisMiConfig config) {

    super(config);

  }



  public String[] getConfigPath(String[] args) throws Exception {

    if (aryS_NO == null)

      init();

    if (aryS_NO == null)

      return null;

    String[] result = new String[aryS_NO.size()];

    for (int i = 0; i < result.length; i++) {

      result[i] = (String) aryS_NO.get(i);

    }

    return result;

  }

}

