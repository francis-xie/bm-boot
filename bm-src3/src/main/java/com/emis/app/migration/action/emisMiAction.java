package com.emis.app.migration.action;

import com.emis.app.migration.emisMiConfig;
import com.emis.db.emisDb;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * User: merlin
 * Date: Apr 23, 2003
 * Time: 7:37:21 PM
 */
public abstract class emisMiAction {
  protected emisMiConfig config;
  emisDb db = null;
  PreparedStatement thisStmt = null;
  protected String[] sourceData;
  protected String[] targetData;

  public final String[] getSourceData() {
    return sourceData;
  }

  public final void setSourceData(final String[] sourceData) {
    this.sourceData = sourceData;
  }

  public final String[] getTargetData() {
    return targetData;
  }

  public final void setTargetData(final String[] targetData) {
    this.targetData = targetData;
  }

  public final void setConfig(final emisMiConfig config) {
    this.config = config;
  }

  public abstract String act(String[] src, String[] param) throws Exception;

  public static emisMiAction getInstance(final String name, final emisMiConfig miConfig) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    final emisMiAction act;
//        try {
    act = (emisMiAction) Class.forName(name).newInstance();
//        } catch (InstantiationException e1) {
//            e1.printStackTrace();  //To change body of catch statement use Options | File Templates.
//        } catch (IllegalAccessException e1) {
//            e1.printStackTrace();  //To change body of catch statement use Options | File Templates.
//        } catch (ClassNotFoundException e1) {
//            e1.printStackTrace();  //To change body of catch statement use Options | File Templates.
//        }
    act.setConfig(miConfig);
    return act;
  }

  final void initStmt(final String sSQL) {
    try {
      if (db == null)
        db = config.getDb();
      thisStmt = db.prepareStmt(sSQL);
    } catch (SQLException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
    } catch (Exception e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
    }
  }

  protected final String doQuery(final String[] src) {
    String result = "";
    db.setCurrentPrepareStmt(thisStmt);
    try {
      for (int i = 0; i < src.length; i++) {
        db.setString(i + 1, src[i]);
      }
      db.prepareQuery();
      if (db.next()) {
        result = db.getString(1);
      }
    } catch (SQLException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
    }
    return result;
  }

  protected final String doQuery(final String[] src, final String[] params) {
    String result = "";
    db.setCurrentPrepareStmt(thisStmt);
    int c = 0;
    try {
      for (int i = 0; i < src.length; i++) {
        db.setString(++c, src[i]);
      }
      for (int i = 0; i < params.length; i++) {
        db.setString(++c, params[i]);
      }
      db.prepareQuery();
      if (db.next()) {
        result = db.getString(1);
      }
    } catch (SQLException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
    }
    return result;
  }
}
