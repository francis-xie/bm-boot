/*
 * Robert 2010/07/16,為了和 db_analysis 配合,把一般 .java 檔
 * 
 * Example:
 * 
 -------------business/test.xml-------------
 <business>   
      <database id="t1">
        <sql>
          select count(*) count from Vendor
        </sql>
      </database>

     <database id="t2">
        <sql>
          Update  Vendor set
              V_NAME= ?,V_NAME_S= ?
          where V_NO =?
        </sql>
        <param>
          <pname type="string">V_NAME</pname>
          <pname type="string">V_NAME_S</pname>
          <pname type="string">V_NO</pname>
        </param>
      </database>
	  
</business>
----------------------------------------------------------jsp------------------------------------------
    emisSQLMgr sqlMgr = emisSQLMgr.getInstance(application);
   //sqlMgr.setDBType(oDb.getDBType());  // used for non mssql
	String sSQL = sqlMgr.getSQL("test","t1");
	String sSQL1 = sqlMgr.getSQL("test","t2");
	
	emisDb oDb = emisDb.getInstance(application);
	try {
		PreparedStatement p1 = sqlMgr.getPreparedStmt(oDb,"test","t1");
		PreparedStatement p2 = sqlMgr.getPreparedStmt(oDb,"test","t2");
		out.println(p1);
		out.println(p2);
		
	} finally {
		oDb.close();
	}
-------------------------------------------------------------------------------------------------------
 * 
 * 
 */
package com.emis.business;
import com.emis.db.*;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.trace.emisError;
import com.emis.trace.emisTracer;
import com.emis.xml.emisXMLCache;
import com.emis.xml.emisXmlFactory;

import java.io.InputStream;
import java.sql.*;
import java.util.Hashtable;
import java.util.Properties;
import java.io.*;
import com.emis.xml.emisXMLCache;
import javax.servlet.ServletContext;
import org.w3c.dom.*;

public class emisSQLMgr {
  private  static final String STR_EMIS_SQL_MGR = "com.emis.business.emisSQLMgr";
  private ServletContext oContext_;
  private Hashtable oCacheHash_ = new Hashtable();
  emisDirectory oBusinessDir = null;
  private String sType = null;

  emisSQLMgr(ServletContext application) throws Exception {
    oContext_ = application;
    if( oContext_.getAttribute(this.STR_EMIS_SQL_MGR) != null )  {
      emisTracer.get(application).sysError(this,emisError.ERR_SVROBJ_DUPLICATE,"emisSQLMgr");
    }
    oContext_.setAttribute(this.STR_EMIS_SQL_MGR,this);

    oBusinessDir = emisFileMgr.getInstance(application).getFactory().getDirectory("business");
  }

  private static emisSQLMgr oInstance_ = null;
  public static emisSQLMgr getInstance(ServletContext application)throws Exception {
    if(oInstance_ == null)
      oInstance_ = new emisSQLMgr(application);

    return oInstance_;
  }


  public PreparedStatement getPreparedStmt ( emisDb oDb , String sFileName , String sId ) throws Exception {
    this.setDBType(oDb.getDBType());
    String sSQL = getSQL( sFileName,sId );
    if( sSQL == null ) {
      throw new Exception("Unable to find '" + sId + "' for " + sFileName + ".xml");
    }
    return oDb.prepareStmt(sSQL);
  }


  public String getSQL ( String sFileName , String sId ) throws Exception	{

    Document oBusinessDoc_ = null;
    emisFile f = getXmlFile(sFileName);
    if(f == null)
      return null;
    String cache_entry = "smgr." + sFileName.toLowerCase();
    emisXMLCache _oXMLCache = (emisXMLCache) oCacheHash_.get(cache_entry);

    if (_oXMLCache != null) {
      oBusinessDoc_ = (Document) _oXMLCache.getCache();


    } else {
      // if not in cache, we load and put it
      InputStream in = f.getInStream();
      try {
        oBusinessDoc_ = emisXmlFactory.getXML(in);
      } finally {
        in.close();
      }
      _oXMLCache = new emisXMLCache(cache_entry, f, oBusinessDoc_);
      oCacheHash_.put(cache_entry,_oXMLCache);
    }

    // we search for <database> tag and search for id or name attribute
    NodeList nl = oBusinessDoc_.getElementsByTagName("database");
    if( nl != null ) {
      for(int i=0;i<nl.getLength();i++) {
        Element e = (Element) nl.item(i);
        String id = e.getAttribute("id");
        String name = e.getAttribute("name");
        if( sId.equalsIgnoreCase(id) || sId.equalsIgnoreCase(name) ) {
          NodeList eSQL = e.getElementsByTagName("sql");
          Element nSQL = (Element) eSQL.item(0);

          return  getText(nSQL);
        }
      }
    }
    return null;
  }

  static String getText(Element e) {
    Node n = e.getFirstChild(); // <sql>..</sql>
    if( n != null ) {
      return n.getNodeValue();
    }
    return null;
  }

  private emisFile getXmlFile(String sFileName) throws Exception {
    if (isMSSQL(sType)) {
      return oBusinessDir.getFile(sFileName + ".xml");
    }else{
      emisDirectory sub = oBusinessDir.subDirectory(sType);
      if (sub != null) {
        return sub.getFile(sFileName + ".xml");
      } else {
        return oBusinessDir.getFile(sFileName + ".xml");
      }
    }
  }

  public void setDBType(String type){
    this.sType = type;
  }

  public static boolean isMSSQL(String sType){
    return (sType == null || "".equals(sType.trim()) || "mssql".equalsIgnoreCase(sType));
  }

  public static boolean isMySql(String sType){
    return (sType == null || "".equals(sType.trim()) || "mysql".equalsIgnoreCase(sType));
  }
}
