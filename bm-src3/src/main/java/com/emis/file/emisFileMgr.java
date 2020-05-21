/*

 * $Header: /repository/src3/src/com/emis/file/emisFileMgr.java,v 1.1.1.1 2005/10/14 12:42:09 andy Exp $

 *

 * Copyright (c) EMIS Corp.

 * 2003/09/16 Joe: esn_base

 * 2004/01/05 Jerry: 2003/02/18有做remoteroot的擴充, 但未被加入src的source中, 重新加回.

 */

package com.emis.file;



import com.emis.manager.emisAbstractMgr;

import com.emis.trace.emisError;

import com.emis.trace.emisTracer;

import com.emis.util.emisUtil;

import com.emis.qa.emisServletContext;

import com.emis.server.emisServerFactory;



import javax.servlet.ServletContext;

import java.io.InputStream;

import java.io.OutputStream;

import java.io.PrintWriter;

import java.io.Reader;

import java.util.*;



/**

 * 1.由 emisFileMgr 可以取得目錄, 系統的根目錄是

 *    emisFileMgr mgr = emisFileMgr.getInstance(application);

 *    emisDirectory dir = mgr.getDirectory("root");

 *  "root" is a special keyword to stands for the DOCUMENT ROOT of a webapp.

 *  系統使用的目錄限制在webapp的document root以下, 且必須預先註冊; 現有的已註冊

 *  目錄有: root, business, cache, dynamic, images, logs,report_def,

 *          report_out, sql, users

 *

 * 2.由 emisDirectory 取得 emisFile

 *    emisFile f = dir.getFile("TEST");

 *

 * 3.由 emisFile 取得各種 Java.io.* 的物件

 *  f.getInStream, f.getOutStream, f.getWriter, f.getWriter

 */

public class emisFileMgr extends emisAbstractMgr {

  public static final String STR_EMIS_FILEMGR = "com.emis.file";



  private String sDefaultFactoryImpl_;

  private HashMap oFactories_ = new HashMap();

  private emisFileFactory oFactoryLocal_;



  /**

   * /resin/xxx.cfg:

   * documentroot=c:\\wwwroot\\epos

   * relativeroot=/epos

   *   remotedocumentroot=\\tpntr01\data (目錄必須分享出來)

   */

  public emisFileMgr(ServletContext application, Properties oProps) throws Exception {

    super(application, STR_EMIS_FILEMGR, "FileSystem");

    String sDocumentRoot = oProps.getProperty("documentroot");

    String sRelativeRoot = oProps.getProperty("relativeroot");

    String sRemoteDocumentRoot = oProps.getProperty("remotedocumentroot");



    // register system-wided Directory

    oFactoryLocal_ = new emisLocalFileFactory();

    // local file system is named as "local"

    oFactories_.put("local", oFactoryLocal_);



    emisDirectory oDir_ = (emisDirectory) new emisDirectoryImpl("", sDocumentRoot, sRelativeRoot, oFactoryLocal_);



    _initLocalFileSystem(oDir_);

    if (sRemoteDocumentRoot != null) {

      emisDirectory oRemoteDir_ = (emisDirectory) new emisDirectoryImpl("", sRemoteDocumentRoot, sRelativeRoot, oFactoryLocal_);

      _initRemoteFileSystem(oRemoteDir_);

    }



    // register other file factory if exists any

    Properties _oFactorys = emisUtil.subProperties("file.factory.", oProps);

    Properties _oDirProps = emisUtil.subProperties("file.directory.", oProps);

    if ((_oFactorys == null) || (_oDirProps == null)) {

      return;

    }



    Enumeration _oEnum = _oFactorys.keys();

    while (_oEnum.hasMoreElements()) {

      String _sFactory = (String) _oEnum.nextElement();

      int _nIdx = _sFactory.indexOf(".");



      if (_nIdx != -1) {

        String _sFactoryName = _sFactory.substring(0, _nIdx);

        String _sFactoryImpl = _sFactory.substring(_nIdx + 1);

        String _sDirNames = _oFactorys.getProperty(_sFactory);

        _registerFactory(_sFactoryName, _sFactoryImpl, _sDirNames, _oDirProps);

      }

    }

  } //  emisFileMgr()



  /** 由.cfg檔內的設定來建立檔案系統 */

  private void _initLocalFileSystem(emisDirectory oRoot) throws Exception {

    emisFileFactoryBase _oFactoryBase = (emisFileFactoryBase) oFactoryLocal_;



    // 將oRoot目錄以 "root"註冊; 註冊:將目錄存入HashMap之entry內

    _oFactoryBase.register("root", oRoot);



    emisDirectory oTmp_ = oRoot.subDirectory("users");

    _oFactoryBase.register("users", oTmp_);  // 註冊



    oTmp_ = oRoot.subDirectory("images");

    _oFactoryBase.register("images", oTmp_);



    oTmp_ = oRoot.subDirectory("logs");

    _oFactoryBase.register("logs", oTmp_);



    oTmp_ = oRoot.subDirectory("business");

    _oFactoryBase.register("business", oTmp_);



    oTmp_ = oRoot.subDirectory("report_def");

    _oFactoryBase.register("report_def", oTmp_);



    oTmp_ = oRoot.subDirectory("report_out");

    _oFactoryBase.register("report_out", oTmp_);



    /**

     * default SQL command factory directory

     */

    oTmp_ = oRoot.subDirectory("sql");

    _oFactoryBase.register("sql", oTmp_);



    oTmp_ = oRoot.subDirectory("cache");

    _oFactoryBase.register("cache", oTmp_);



    oTmp_ = oRoot.subDirectory("dynamic");

    _oFactoryBase.register("dynamic", oTmp_);

    oTmp_ = oRoot.subDirectory("data");

    _oFactoryBase.register("data", oTmp_);

  } // _initLocalFileSystem



  /** 註冊遠端檔案系統 */

  private void _initRemoteFileSystem(emisDirectory oRemoteRoot) throws Exception {

    emisFileFactoryBase _oFactoryBase = (emisFileFactoryBase) oFactoryLocal_;



    // 將oRoot目錄以 "root"註冊; 註冊:將目錄存入HashMap之entry內

    _oFactoryBase.register("remoteroot", oRemoteRoot);

  } // _initRemoteFileSystem



  /** 目前只實作了localFileFactory, 因此此段皆未執行到 */

  private void _registerFactory(String sFactoryName, String sFactoryImpl, String sDirNames, Properties oDirProps) throws Exception {

    // create and put factory first

    emisFileFactory _oFactory = (emisFileFactory) Class.forName(sFactoryImpl).newInstance();

    oFactories_.put(sFactoryName, _oFactory);



    StringTokenizer oSTokenizer = new StringTokenizer(sDirNames, ",");

    try {

      while (oSTokenizer.hasMoreTokens()) {

        String _sDirName = oSTokenizer.nextToken().toLowerCase();

        String _sDirectory = oDirProps.getProperty(_sDirName);

        String _sRelative = oDirProps.getProperty(_sDirName + ".relative");



        if (_sDirectory != null) {

          emisDirectory _oDir = new emisDirectoryImpl(_sDirName, _sDirectory, _sRelative, _oFactory);

          ((emisFileFactoryBase) _oFactory).register(_oDir);

        }

      }

    } catch (NoSuchElementException ignoreit) {

    }

  }



  /** 取得<code>emisDirectory</code>物件 */

  public synchronized emisDirectory getDirectory(String sDirectoryName) throws Exception {

    sDirectoryName = sDirectoryName.toLowerCase();

    emisDirectory _oDirectory = oFactoryLocal_.getDirectory(sDirectoryName);

    if (_oDirectory == null) throw new Exception("Directory Name:" + sDirectoryName + " Factory not regsitered Directory Value");

    return _oDirectory;

  }



  /** 取得<code>emisFileFactory</code>物件 */

  public synchronized emisFileFactory getFactory() {

    return oFactoryLocal_;

  }



  /**

   * 取得<code>emisDirectory</code>物件 (by name)

   */

  public synchronized emisFileFactory getFactory(String sFactoryName) {

    return (emisFileFactory) oFactories_.get(sFactoryName);

  }



  public void setProperty(int propertyID, Object oValue) throws Exception {

  }



  /*-----------------these functions are used by local file system--------------*/



  public synchronized PrintWriter getWriter(String sDirectoryName, String sFileName, String sMode) throws Exception {

    sDirectoryName = sDirectoryName.toLowerCase();

    return oFactoryLocal_.getWriter(sDirectoryName, sFileName, sMode);

  }



  public synchronized PrintWriter getWriter(emisDirectory oDirectory, String sFileName, String sMode) throws Exception {

    return oFactoryLocal_.getWriter(oDirectory, sFileName, sMode);

  }



  public synchronized Reader getReader(String sDirectoryName, String sFileName) throws Exception {

    sDirectoryName = sDirectoryName.toLowerCase();

    return oFactoryLocal_.getReader(sDirectoryName, sFileName);

  }



  public synchronized Reader getReader(emisDirectory oDirectory, String sFileName) throws Exception {

    return oFactoryLocal_.getReader(oDirectory, sFileName);

  }



  public InputStream getInStream(String sDirectoryName, String sFileName) throws Exception {

    sDirectoryName = sDirectoryName.toLowerCase();

    return oFactoryLocal_.getInStream(sDirectoryName, sFileName);

  }



  public OutputStream getOutStream(String sDirectoryName, String sFileName, String sMode) throws Exception {

    sDirectoryName = sDirectoryName.toLowerCase();

    return oFactoryLocal_.getOutStream(sDirectoryName, sFileName, sMode);

  }



  public static emisFileMgr getInstance(ServletContext oContext) throws Exception {

    emisFileMgr _oMgr = (emisFileMgr) oContext.getAttribute(emisFileMgr.STR_EMIS_FILEMGR);

    if (_oMgr == null) {

      emisTracer.get(oContext).sysError(null, emisError.ERR_SVROBJ_NOT_BIND, "emisFileMgr");

    }

    return _oMgr;

  }



  public static void main(String[] args) throws Exception {

    emisServletContext _oContext = new emisServletContext();

    emisServerFactory.createServer(_oContext,"c:\\wwwroot\\yes","c:\\resin\\yes.cfg", true);



    emisFileMgr _oFileMgr = emisFileMgr.getInstance(_oContext);

    emisDirectory _oDir = _oFileMgr.getDirectory("remoteroot");

    System.out.println("root=" + _oDir.getDirectory());

    Enumeration e = _oDir.getFileList("*.jsp");

    int _iCount = 0;

    while (e.hasMoreElements()) {

      _iCount++;

      emisFile _oFile = (emisFile) e.nextElement();

      System.out.println("file[" + _iCount + "]=" + _oFile.getFullName());

    }



    _iCount = 0;

    _oDir = _oDir.subDirectory("data").subDirectory("upload").subDirectory("all").subDirectory("realtime");

    e = _oDir.getFileList();

    System.out.println("\nFiles in /data/upload/...");

    while (e.hasMoreElements()) {

      _iCount++;

      emisFile _oFile = (emisFile) e.nextElement();

      System.out.println("file[" + _iCount + "]=" + _oFile.getFullName());

    }

  }

}