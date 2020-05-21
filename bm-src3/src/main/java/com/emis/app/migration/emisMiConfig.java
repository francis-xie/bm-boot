package com.emis.app.migration;


import com.emis.app.migration.emisMiLogToDb;
import com.emis.business.emisBusinessResourceBean;
import com.emis.business.emisHttpServletRequest;
import com.emis.db.emisDb;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;


/**
 * $Id: emisMiConfig.java 1549 2015-11-11 06:31:03Z andy.he $
 *
 * Track+[19474] Dana 2012/02/22 調整migration欄位下傳長度支持在emisprop設定,使用方式:參數名稱用兩個@@包裹,如:@EP_P_NAME_S_LEN@
 */
public final class emisMiConfig implements emisParsableDoc {

  private emisMiSource source;

  private emisMiTarget target;

  private emisMiConverter converter;

  private String lastElem = "";

  private boolean targetAdded = false;

  private emisMiField lastSrcFld;

  private final Vector vSource = new Vector();

  private final Vector vTarget = new Vector();

  private String migrationName;

  private emisMiField[] sourceFields;

  private emisMigration migration;

  private emisMiLogToDb logToDb ;


  public final String getLogPath() {

    return logPath;

  }



  private String logPath;



  public final emisMigration getMigration() {

    return migration;

  }



  public final void setMigration(final emisMigration migration) {

    this.migration = migration;

  }



  public final String getMigrationName() {

    return migrationName;

  }

  public final emisMiLogToDb getMiLogToDb(){
    return this.logToDb;
  }


  private emisMiField[] targetFields;

  private ServletContext _oContext;

  private static emisFileMgr oFileMgr;

  private emisDb _db;

  private emisBusinessResourceBean resourceBean_;



//    private int mode = 0;

//    private static final int MISOURCE = 1;

//    private static final int MITARGET = 2;

  public static emisFile getFile(final ServletContext context, final String mName) throws Exception {

    oFileMgr = emisFileMgr.getInstance(context);

    emisFile _oConfigFile = null;

    final emisDirectory _oConfDir = oFileMgr.getDirectory("root").subDirectory("WEB-INF");

    final emisDirectory _oMDir = _oConfDir.subDirectory("migration");

    if (mName != null && _oMDir != null)

      _oConfigFile = _oMDir.getFile(mName + ".xml");

    if (_oConfigFile != null && _oConfigFile.exists())

      return _oConfigFile;

    _oConfigFile = _oConfDir.getFile("migration.xml");

    return _oConfigFile;

  }



  public final emisMiField[] getSourceFields() {

    return sourceFields;

  }



  public final emisMiField[] getTargetFields() {

    return targetFields;

  }



  public final ServletContext getContext() {

    return _oContext;

  }



  public emisMiConfig() {

  }



  public emisMiConfig(final String migrationName, final ServletContext context) throws Exception {

    final emisDb db;

    db = emisDb.getInstance(context);

    db.setDescription("Migration:" + migrationName);

    init(migrationName, context, db);

  }



  private void init(final String migrationName, final ServletContext context, final emisDb db) throws Exception {

    this.migrationName = migrationName;

    _oContext = context;

    this._db = db;

    BufferedReader br;

    converter = new emisMiConverter();

    br = new BufferedReader(getFile(_oContext, migrationName).getReader("UTF-8"));
    try {
      QDParser.parse(this, br ,_oContext);
    } finally {
    	br.close();
    }

    this.logToDb = new emisMiLogToDb(_oContext,_db);
  }



  public emisMiConfig(final String migrationName, final ServletContext context, final emisDb db) throws Exception {

    init(migrationName, context, db);

  }



  public final emisMiConverter getConverter() {

    return converter;

  }



  public final emisMiSource getSource() {

    return source;

  }



  public final emisMiTarget getTarget() {

    return target;

  }



  boolean parsing = false;



  public final void startDocument() {

  }



  public final void endDocument() {

  }



  public final void startElement(final String elem, final Hashtable h) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException, InvocationTargetException, NoSuchMethodException {

    if (parsing == false && !elem.equalsIgnoreCase("migration")) {

      return;

    }

    if (elem.equalsIgnoreCase("migration")) {

      String s = (String) h.get("name");

      if (s.equalsIgnoreCase(this.migrationName)) {

        parsing = true;

      } else {

        parsing = false;

        return;

      }

      s = (String) h.get("logpath");

      if (s != null) {

        this.logPath = s;

      }

    }

    if (elem.equalsIgnoreCase("misource")) {

      final String s = (String) h.get("class");

      source = (emisMiSource) emisMiDataSet.getInstance(s);

      source.setConfig(this);

      source.parse(h);

    } else if (elem.equalsIgnoreCase("mitarget")) {

//            mode = MITARGET;

      final String s = (String) h.get("class");

      target = (emisMiTarget) emisMiDataSet.getInstance(s);

      target.setConfig(this);

      target.parse(h);

    } else if (elem.equalsIgnoreCase("source")) {

      lastSrcFld = new emisMiField(h, this);

      vSource.add(lastSrcFld);

    } else if (elem.equalsIgnoreCase("target")) {

      final emisMiField field;

      final String srcIndex = (String) h.get("src");

      if (vSource.size() == 0 || ((srcIndex != null) && srcIndex.equals("-1"))) {

        field = new emisMiField(null, this);

      } else {

        lastSrcFld = (emisMiField) vSource.get(vSource.size() - 1);

        field = (emisMiField) lastSrcFld.clone();

        field.setConfig(this);

      }

      field.parse(h);

      addTarget(field);

      targetAdded = true;

    } else if (elem.equalsIgnoreCase("data")) {

      targetAdded = false;

//            lastSrcFld = null;

    }

    lastElem = elem;

  }



  private void addTarget(final emisMiField field) {

    field.setSrc(vSource.size() - 1);

    vTarget.add(field);

  }



  public final void endElement(final String elem) {

    if (elem.equalsIgnoreCase("data")) {

      if (lastSrcFld != null && !targetAdded) {

//                addTarget((emisMiField) lastSrcFld.clone());

        lastSrcFld = null;

      }

    } else {

      if (elem.equalsIgnoreCase("migration")) {

        if (parsing) {

          sourceFields = new emisMiField[vSource.size()];

          for (int i = 0; i < sourceFields.length; i++) {

            final emisMiField fld = (emisMiField) vSource.get(i);

            if (fld.getTrim() == emisMiField.TRIM_DEFAULT)

              fld.setTrimFlag(this.source.trimFields());

            sourceFields[i] = fld;

          }

          converter.setSrcField(sourceFields);

          targetFields = new emisMiField[vTarget.size()];

          for (int i = 0; i < targetFields.length; i++) {

            final emisMiField fld = (emisMiField) vTarget.get(i);

            if (fld.getTrim() == emisMiField.TRIM_DEFAULT)

              fld.setTrimFlag(this.target.trimFields());

            final int[] params = fld.getActionParam1();

            if (params != null && params.length != 0) {

              for (int j = 0; j < params.length; j++) {

                params[j] = this.sourceFieldIndex(params[j]);

              }

            }

            targetFields[i] = fld;

          }

          converter.setTarField(targetFields);

        }

        parsing = false;

      }

    }

  }



  public final void text(final String text) throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InstantiationException {

    if (lastElem.equals("SQL")) {

      final Hashtable h = new Hashtable();

      h.put("SQL", text);

      source.parse(h);

      lastElem = "";

    }

  }



  public final int sourceFieldIndex(final String cKey) {

    int index = -1;

    for (int i = 0; i < sourceFields.length && index == -1; i++) {

      if (sourceFields[i].getName().equalsIgnoreCase(cKey))

        index = i;

    }

    return index;

  }



  public final int targetFieldIndex(final String cKey) {

    int index = -1;

    for (int i = 0; i < targetFields.length && index == -1; i++) {

      if (targetFields[i].getName().equalsIgnoreCase(cKey))

        index = i;

    }

    return index;

  }



  public final int sourceFieldIndex(final int seq) {

    int index = -1;

    for (int i = 0; i < sourceFields.length && index == -1; i++) {

      if (sourceFields[i].getSeq() == seq)

        index = i;

    }

    return index;

  }



  public final emisDb getDb() {

    return _db;

  }



  public final emisBusinessResourceBean getResourceBean() {

    if (resourceBean_ == null) {

      try {

        resourceBean_ = new emisBusinessResourceBean();

        resourceBean_.setEmisDb(this._db);

        resourceBean_.setFileMgr(emisFileMgr.getInstance(_oContext));

        resourceBean_.setEmisHttpServletRequest(new emisHttpServletRequest());

      } catch (Exception e) {

      	e.printStackTrace();

        // Log here;  //To change body of catch statement use Options | File Templates.

      }

    }

    return resourceBean_;

  }



  public final void releaseDb() {
    try{
      if(logToDb != null) logToDb.close();
    } catch(Exception e){
      e.printStackTrace();
    }
    if (_db != null) {

      _db.close();

    }
  }
}