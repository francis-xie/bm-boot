package com.emis.app.migration;



import com.emis.db.emisDb;

import com.emis.db.emisDbConnector;



import java.lang.reflect.InvocationTargetException;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.*;



/**

 * User: merlin

 * Date: Apr 22, 2003

 * Time: 6:42:57 PM

 */

public class emisMiRDBTarget extends emisMiTarget {

  private boolean updateWhenExists = true;

  String dbTable[];

  String dbColumns[][];

  int cSeqs[][];

  String dbKeys[][];

  int kSeqs[][];

  String updateSQL[];

  String insertSQL[];

  String value[][];

  private PreparedStatement insStmt[], updStmt[], qryStmt[];

  emisDb _db;

  private String[] qrySQL;

  private Set incFields=null;

  boolean bOnline = false;

  HashSet onlineSet = null;

  private boolean updateOnly;

  private boolean codingTransfer = true;



  boolean trimFields() {

    return true;

  }



  public void getOnlineSet(emisDb db) throws SQLException {

    onlineSet = new HashSet();

    db.executeQuery("select s_no from online_s");

    while (db.next()) {

      onlineSet.add(db.getString(1));

    }

  }



  public boolean open(emisMiConfig config) throws Exception {

    writeCount = 0;

    this.config = config;

    _db = config.getDb();

    if (!codingTransfer) {

      _db.setEncodingTransferMode(emisDbConnector.TRANSFER_NONE);

    }

    if (_db == null)

      return false;

    setdbTable();

    if (bOnline == true) {

      getOnlineSet(_db);

    }

    if (clear) {

      for (int i = 0; i < dbTable.length; i++)

        _db.executeUpdate("delete from " + dbTable[i]);

    }

    createInsertSQL();

    insStmt = new PreparedStatement[insertSQL.length];

    for (int i = 0; i < dbTable.length; i++) {

      insStmt[i] = _db.prepareStmt(insertSQL[i]);

    }

    if (!clear) {

      createUpdateSQL();

      updStmt = new PreparedStatement[updateSQL.length];

      for (int i = 0; i < dbTable.length; i++) {

        updStmt[i] = _db.prepareStmt(updateSQL[i]);

      }

    }

    if (!updateWhenExists) {

      createQrySQL();

      qryStmt = new PreparedStatement[qrySQL.length];

      for (int i = 0; i < dbTable.length; i++) {

        qryStmt[i] = _db.prepareStmt(qrySQL[i]);

      }

    }

    _db.setAutoCommit(false);

    // or         result = new Object[fldNames.length];

    return true;

  }



  private boolean existsData(String[] data, int i) throws SQLException {

    int p = 1;

    _db.setCurrentPrepareStmt(this.qryStmt[i]);

    for (int j = 0; j < dbKeys[i].length; ++j) {

      String sStr = data[kSeqs[i][j]];

      _db.setString(p++, sStr);

    }

    _db.prepareQuery();

    return (_db.next());

  }



  int updateData(String[] data, int i) throws SQLException {

    int p = 1;

    for (int j = 0; j < dbKeys[i].length; ++j) {

      if (data[kSeqs[i][j]] == null)

        return -1;

    } // 若key值中有一個為null則不寫入也不會新增

    _db.setCurrentPrepareStmt(this.updStmt[i]);

    for (int j = 0; j < dbColumns[i].length; ++j) {

      String sStr = data[cSeqs[i][j]];

      _db.setString(p++, sStr);

    }

    for (int j = 0; j < dbKeys[i].length; ++j) {

      String sStr = data[kSeqs[i][j]];

      _db.setString(p++, sStr);

    }

    return _db.prepareUpdate();

  }



  int insertData(String[] data, int i) throws SQLException {

    int p = 1;

    _db.setCurrentPrepareStmt(this.insStmt[i]);

    for (int j = 0; j < dbColumns[i].length; ++j) {

      String sStr = data[cSeqs[i][j]];

      _db.setString(p++, sStr);

    }

    for (int j = 0; j < dbKeys[i].length; ++j) {

      String sStr = data[kSeqs[i][j]];

      _db.setString(p++, sStr);

    }

    int iRet = 0;

    try {

        iRet = _db.prepareUpdate();

    } catch (SQLException e) {

        System.out.println(insertSQL[i]);

        throw e;

    }

    return iRet;

  }



  public boolean write(String[] data) throws SQLException {

    boolean writeAll = true;

    if (bOnline) {

      if (!onlineSet.contains(data[1]))

        return true;

    }

    if (clear) {

      for (int i = 0; i < dbTable.length; i++) {

        insertData(data, i);

      }

    } else if (updateWhenExists) {

      for (int i = 0; i < dbTable.length; i++)

        if (updateData(data, i) == 0 && !updateOnly)  // -1 表示key值有null 則不作任何異動

          insertData(data, i);

    } else {

      for (int i = 0; i < dbTable.length; i++)

        if (!existsData(data, i)) {

          int abc = insertData(data, i);

          String s = String.valueOf(abc);

        }

        else {

            writeAll = false;

        }

    }

    return writeAll;

  }



  public boolean close(boolean closeDb) {

    if (closeDb) {

      try {

        _db.commit();

      } catch (SQLException e) {

        // Log here;  //To change body of catch statement use Options | File Templates.

      }

    }

    return false;

  }



  void parse(Hashtable h) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ClassNotFoundException {

    super.parse(h);

    Iterator it = h.keySet().iterator();

    String key;

    String val;

    while (it.hasNext()) {

      key = (String) it.next();

      val = (String) h.get(key);

      if (key.equalsIgnoreCase("Exist")) {

        if (val.equalsIgnoreCase("update"))

          updateWhenExists = true;

        else

          updateWhenExists = false;

      } else if (key.equalsIgnoreCase("online")) {

        if (val.equalsIgnoreCase("true"))

          bOnline = true;

        else

          bOnline = false;

      } else if (key.equalsIgnoreCase("updateonly")) {

        if (val.equalsIgnoreCase("true"))

          updateOnly = true;

        else

          updateOnly = false;

      } else if (key.equalsIgnoreCase("increment")) {

          StringTokenizer st = new StringTokenizer(val, ",");

          incFields = new HashSet();

          while (st.hasMoreTokens()) {

              incFields.add(st.nextToken());

          }

      } else if (key.equalsIgnoreCase("transfer")) {

        if (val.equalsIgnoreCase("false"))

          codingTransfer = false;

        else

          codingTransfer = true;

      }

    }

  }



  public void append(String[] path, boolean reopen) {

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

    updateSQL = new String[h.size()];

    qrySQL = new String[h.size()];

    insertSQL = new String[h.size()];

    dbColumns = new String[h.size()][];

    dbKeys = new String[h.size()][];

    cSeqs = new int[h.size()][];

    kSeqs = new int[h.size()][];

    for (int i = 0; i < dbTable.length; i++) {

      String key = (String) it.next();

      emisMiDbTable val = (emisMiDbTable) h.get(key);

      dbTable[i] = val.name;

      int cSize = val.columns.size();

      dbColumns[i] = new String[cSize];

      cSeqs[i] = new int[cSize];

      for (int j = 0; j < cSize; j++) {

        String s = (String) val.columns.get(j);

        dbColumns[i][j] = s;

        Integer integer = (Integer) val.cSeq.get(j);

        cSeqs[i][j] = integer.intValue();

      }

      dbKeys[i] = new String[val.keys.size()];

      kSeqs[i] = new int[val.keys.size()];

      for (int j = 0; j < val.keys.size(); j++) {

        dbKeys[i][j] = (String) val.keys.get(j);

        kSeqs[i][j] = ((Integer) val.kSeq.get(j)).intValue();

      }

    }

  }



    public void createUpdateSQL() {

        for (int i = 0; i < dbTable.length; i++) {

            StringBuffer sb = new StringBuffer();

            for (int j = 0; j < dbColumns[i].length; j++) {

                sb.append(dbColumns[i][j]);

                if (incFields != null && incFields.contains(dbColumns[i][j])) {

                    sb.append(" = " + dbColumns[i][j] + " + ?,");

                } else {

                    sb.append(" = ?,");

                }

            }

            if (sb.length() > 0) {

                sb.setLength(sb.length() - 1);

            }

            StringBuffer sbKey = new StringBuffer(dbKeys[i][0]);

            sbKey.append(" =? ");

            for (int j = 1; j < dbKeys[i].length; j++) {

                sbKey.append(" and ");

                sbKey.append(dbKeys[i][j]);

                sbKey.append(" =? ");

            }

            String sUpdate = "update " + dbTable[i] + " set " + sb.toString();

            if (sbKey.length() > 0)

                sUpdate = sUpdate + " where " + sbKey;

            this.updateSQL[i] = sUpdate;

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



  public void createInsertSQL() {

    for (int i = 0; i < dbTable.length; i++) {

      StringBuffer sFld = new StringBuffer();

      StringBuffer sValue = new StringBuffer();

      for (int j = 0; j < dbColumns[i].length; j++) {

        sFld.append(dbColumns[i][j]);

        sFld.append(", ");

        sValue.append("?, ");

      }

      for (int j = 0; j < dbKeys[i].length; j++) {

        sFld.append(dbKeys[i][j]);

        sFld.append(", ");

        sValue.append("?, ");

      }

      if (sFld.length() > 0) {

        sFld.setLength(sFld.length() - 2);

        sValue.setLength(sValue.length() - 2);

      }

      String sInsert = "insert into " + dbTable[i] + " (" + sFld.toString() + " ) values ( " + sValue.toString() + ")";

      this.insertSQL[i] = sInsert;

    }

  } //  createInsertSQL



  public boolean clearTemp() { return true; } // no temp data

}

