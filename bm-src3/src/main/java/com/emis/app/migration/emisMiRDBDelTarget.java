package com.emis.app.migration;



import com.emis.db.emisDb;



import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.ArrayList;

import java.util.Hashtable;

import java.util.Iterator;



/**

 * User: merlin

 * Date: Apr 22, 2003

 * Time: 6:42:57 PM

 */

public class emisMiRDBDelTarget extends emisMiTarget {

  String dbTable[];

  String dbKeys[][];

  int kSeqs[][];

  String deleteSQL[];

  String value[][];

  private PreparedStatement delStmt[];

  emisDb _db;

  private String[] qrySQL;



  boolean trimFields() {

    return true;

  }



  public boolean open(emisMiConfig config) throws Exception {

    writeCount = 0;

    this.config = config;

    _db = config.getDb();

    if (_db == null)

      return false;

    setdbTable();

    createDeleteSQL();

    delStmt = new PreparedStatement[deleteSQL.length];

    for (int i = 0; i < dbTable.length; i++) {

      delStmt[i] = _db.prepareStmt(deleteSQL[i]);

    }

    return true;

  }



  int deleteData(String[] data, int i) throws SQLException {

    int p = 1;

    _db.setCurrentPrepareStmt(this.delStmt[i]);

    for (int j = 0; j < dbKeys[i].length; ++j) {

      String sStr = data[kSeqs[i][j]];

      _db.setString(p++, sStr);

    }

    return _db.prepareUpdate();

  }



  public boolean write(String[] data) throws SQLException {

    for (int i = 0; i < dbTable.length; i++)

      deleteData(data, i);

    return true;

  }



  public boolean close(boolean closeDb) {

    if (!closeDb) {

      this._db.close();

    }

    return false;

  }



  public void append(String[] path, boolean reopen) {

  }



    public boolean clearTemp() {

        return false;

    }



    void setdbTable() {

    emisMiField[] flds = config.getTargetFields();

    Hashtable h = new Hashtable();

    for (int i = 0; i < flds.length; i++) {

      emisMiField fld = flds[i];

      String sTable = fld.getTable();

      String ColumnName = fld.getName();

      if (sTable == null || ColumnName == null)

        continue;

      emisMiDbTable table;

      if ((table = (emisMiDbTable) h.get(sTable)) == null) {

        table = new emisMiDbTable();

        h.put(sTable, table);

        table.name = sTable;

        table.columns = new ArrayList();

        table.keys = new ArrayList();

        table.cSeq = new ArrayList();

        table.kSeq = new ArrayList();

        table.values = new ArrayList();

      }

      if (fld.getKey() < 0) {         // sorting not support yet

        table.columns.add(ColumnName);

        table.cSeq.add(new Integer(i));

        table.values.add(fld.getValue());

      } else {

        table.keys.add(fld.getName());

        table.kSeq.add(new Integer(i));

        table.values.add(fld.getValue());

      }

      h.put(sTable, table);

    }

    Iterator it = h.keySet().iterator();

    dbTable = new String[h.size()];

    qrySQL = new String[h.size()];

    dbKeys = new String[h.size()][];

    kSeqs = new int[h.size()][];

    for (int i = 0; i < dbTable.length; i++) {

      String key = (String) it.next();

      emisMiDbTable val = (emisMiDbTable) h.get(key);

      dbTable[i] = val.name;

      dbKeys[i] = new String[val.keys.size()];

      kSeqs[i] = new int[val.keys.size()];

      for (int j = 0; j < val.keys.size(); j++) {

        dbKeys[i][j] = (String) val.keys.get(j);

        kSeqs[i][j] = ((Integer) val.kSeq.get(j)).intValue();

      }

    }

  }



  public void createDeleteSQL() {

    for (int i = 0; i < dbTable.length; i++) {

      StringBuffer sbKey = new StringBuffer(dbKeys[i][0]);

      sbKey.append(" =? ");

      for (int j = 1; j < dbKeys[i].length; j++) {

        sbKey.append(" and ");

        sbKey.append(dbKeys[i][j]);

        sbKey.append(" =? ");

      }

      String sDelete;

      if (sbKey.length() > 0) {

        sDelete = "delete from " + dbTable[i] + " where " + sbKey;

        this.deleteSQL[i] = sDelete;

      }

    }

  }



  public void createQrySQL() {

    for (int i = 0; i < dbTable.length; i++) {

      StringBuffer sbKey = new StringBuffer("select * from " + dbTable[i] + " where ");

      sbKey.append(dbKeys[i][0]);

      sbKey.append(" =? ");

      for (int j = 1; j < dbKeys[i].length; j++) {

        sbKey.append(" and ");

        sbKey.append(dbKeys[i][j]);

        sbKey.append(" =? ");

      }

      this.qrySQL[i] = sbKey.toString();

    }

  }

}

