package com.emis.file;

import com.emis.util.emisCommonEnum;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class emisLocalFileFactory extends emisFileFactoryBase {
  protected emisLocalFileFactory() {
    super();
    super.sFileSeparator_ = System.getProperty("file.separator");
  }

  /**
   *   sMode = null 或 "a" 或 "f"  "af"
   *   a 為 append
   *   f 為 autoFlush
   */
  public PrintWriter getWriter(String sDirectoryName, String sFileName, String sMode) throws Exception {
    emisDirectory _oDirectory = this.getDirectory(sDirectoryName);
    return this.createWriter(_oDirectory.getDirectory(), sFileName, sMode);
  }

  /**
   *   sMode = null 或 "a" 或 "f"  "af"
   *   a 為 append
   *   f 為 autoFlush
   */
  public PrintWriter getWriter(emisDirectory oDirectory, String sFileName, String sMode) throws Exception {
    return this.createWriter(oDirectory.getDirectory(), sFileName, sMode);
  }

  public Reader getReader(String sDirectoryName, String sFileName) throws Exception {
    emisDirectory _oDirectory = this.getDirectory(sDirectoryName);
    return this.createReader(_oDirectory.getDirectory(), sFileName);
  }

  public Reader getReader(emisDirectory oDirectory, String sFileName) throws Exception {
    return this.createReader(oDirectory.getDirectory(), sFileName);
  }

  protected boolean mkdir(emisDirectory oDir) {
    File _oF = new File(oDir.getDirectory());
    return _oF.mkdirs();
  }

  protected boolean exists(emisDirectory oDir) {
    File _oF = new File(oDir.getDirectory());
    return (_oF.isDirectory() && _oF.exists());
  }

  private Reader createReader(String sDirectory, String sFileName) throws Exception {
    FileReader _oFinput = new FileReader(sDirectory + sFileName);
    BufferedReader _oBr = null;

    try {
      _oBr = new BufferedReader(_oFinput);
    } catch (Exception e1) {
      try {
        _oFinput.close();
      } catch (Exception ignore1) {
      }
      e1.fillInStackTrace();
      throw e1;
    }
    return _oBr;
  }


  private PrintWriter createWriter(String sDirectory, String sFileName, String sMode) throws Exception {
    if (sMode == null) sMode = "";
    sMode = sMode.toUpperCase();

    FileWriter _oFOutput = null;
    boolean _isAppend = false;
    boolean _isAutoFlush = false;

    // is open for append
    if (sMode.indexOf("A") != -1) _isAppend = true;
    if (sMode.indexOf("F") != -1) _isAutoFlush = true;
    _oFOutput = new FileWriter(sDirectory + sFileName, _isAppend);


    BufferedWriter _oBw = null;
    try {
      _oBw = new BufferedWriter(_oFOutput);
    } catch (Exception e) {
      try {
        _oFOutput.close();
      } catch (Exception ignore) {
      }
      e.fillInStackTrace();
      throw e;
    }

    PrintWriter _oPWriter = null;

    try {
      _oPWriter = new PrintWriter(_oBw, _isAutoFlush);
    } catch (Exception e1) {
      try {
        _oFOutput.close();
      } catch (Exception ignore1) {
      }
      e1.fillInStackTrace();
      throw e1;
    }
    return _oPWriter;
  }

  public InputStream getInStream(String sDirectoryName, String sFileName) throws Exception {
    emisDirectory _oDirectory = this.getDirectory(sDirectoryName);
    return new FileInputStream(_oDirectory.getDirectory() + sFileName);
  }

  public InputStream getInStream(emisDirectory oDirectory, String sFileName) throws Exception {
    return new FileInputStream(oDirectory.getDirectory() + sFileName);
  }


  public OutputStream getOutStream(emisDirectory oDirectory, String sFileName, String sMode) throws Exception {

    boolean isAppend = false;
    if (sMode != null) {
      sMode = sMode.toUpperCase();
      if (sMode.indexOf("A") != -1) isAppend = true;
    }
    return new FileOutputStream(oDirectory.getDirectory() + sFileName, isAppend);

  }

  public OutputStream getOutStream(String sDirectoryName, String sFileName, String sMode) throws Exception {
    emisDirectory _oDirectory = this.getDirectory(sDirectoryName);
    return getOutStream(_oDirectory, sFileName, sMode);
  }

  public ZipOutputStream getZipOutStream(String sDirectoryName, String sFileName, String sExtractName, String sMode) throws Exception {
    emisDirectory _oDirectory = this.getDirectory(sDirectoryName);
    return getZipOutStream(_oDirectory, sFileName, sExtractName, sMode);

  }

  public ZipOutputStream getZipOutStream(emisDirectory oDirectory, String sFileName, String sExtractName, String sMode) throws Exception {
    OutputStream out = getOutStream(oDirectory, sFileName, sMode);
    ZipEntry zEntry = new ZipEntry(sExtractName);
    ZipOutputStream oZip = new ZipOutputStream(out);
    zEntry.setMethod(ZipEntry.DEFLATED);
    oZip.putNextEntry(zEntry);
    oZip.setLevel(Deflater.BEST_SPEED);
    return oZip;
  }

  public Enumeration getDirList(emisDirectory oDir) {
    String sDirectory = oDir.getDirectory();
    File f = new File(sDirectory);
    if (f.isDirectory()) {
      File[] list = f.listFiles();
      if ((list != null) && (list.length > 0)) {
        emisCommonEnum _oEnum = new emisCommonEnum();
        for (int i = 0; i < list.length; i++) {
          File tmpF = list[i];
          if (tmpF.isDirectory()) {
            emisDirectory _oDir = oDir.subDirectory(tmpF.getName());
            _oEnum.add(_oDir);
          }
        }
        return _oEnum;
      }

    }
    return new emisCommonEnum();
  }

  public emisFile getFile(String sDirectoryName, String sFileName) throws Exception {
    emisDirectory _oDirectory = this.getDirectory(sDirectoryName);
    return new emisLocalFile(_oDirectory, sFileName);
  }

  public emisFile getFile(emisDirectory oDirectory, String sFileName) throws Exception {
    return new emisLocalFile(oDirectory, sFileName);
  }

  public Enumeration getFileList(emisDirectory oDir) {
    String sDirectory = oDir.getDirectory();
    File f = new File(sDirectory);
    if (f.isDirectory()) {
      File[] list = f.listFiles();
      if ((list != null) && (list.length > 0)) {
        Arrays.sort(list);  // 2002-11-20
        emisCommonEnum _oEnum = new emisCommonEnum();
        for (int i = 0; i < list.length; i++) {
          File tmpF = list[i];
          if (tmpF.isFile()) {
            _oEnum.add(new emisLocalFile(oDir, tmpF.getName()));
          }
        }
        return _oEnum;
      }

    }
    return new emisCommonEnum();
  }

  public Enumeration getFileList(emisDirectory oDir, String sFilter) {
    Enumeration e = oDir.getFileList();
    if ("*".equals(sFilter) || "*.*".equals(sFilter))
      return e;
    emisCommonEnum oFilterEnum = new emisCommonEnum();

    sFilter = sFilter.toUpperCase();
    if (sFilter.startsWith("*.")) {
      sFilter = sFilter.substring(2);
      while (e.hasMoreElements()) {
        emisFile f = (emisFile) e.nextElement();
        if (sFilter.equalsIgnoreCase(f.getFileExt())) {
          oFilterEnum.add(f);
        }
      }
      return oFilterEnum;
    }
    if (sFilter.endsWith(".*")) {
      sFilter = sFilter.substring(0, sFilter.length() - 2);
      while (e.hasMoreElements()) {
        emisFile f = (emisFile) e.nextElement();
        if (sFilter.equalsIgnoreCase(f.getShortName())) {
          oFilterEnum.add(f);
        }
      }
      return oFilterEnum;
    }
    return oFilterEnum;
  }


  public void cleanFile(emisDirectory oDir, String sFilter) {
    if (sFilter == null) return;
    if ("*".equals(sFilter) || "*.*".equals(sFilter)) {
      Enumeration e = oDir.getFileList();
      while (e.hasMoreElements()) {
        emisFile f = (emisFile) e.nextElement();
        f.delete();
      }
      return;
    }
    sFilter = sFilter.toUpperCase();
    if (sFilter.startsWith("*.")) {
      sFilter = sFilter.substring(2);
      Enumeration e = oDir.getFileList();
      while (e.hasMoreElements()) {
        emisFile f = (emisFile) e.nextElement();
        if (sFilter.equalsIgnoreCase(f.getFileExt())) {
          f.delete();
        }
      }
      return;
    }
    if (sFilter.endsWith(".*")) {
      sFilter = sFilter.substring(0, sFilter.length() - 2);
      Enumeration e = oDir.getFileList();
      while (e.hasMoreElements()) {
        emisFile f = (emisFile) e.nextElement();
        if (sFilter.equalsIgnoreCase(f.getShortName())) {
          f.delete();
        }
      }
    }
  }
/*
    public static void main(String[] args) throws Exception {
      com.emis.test.emisServletContext oContext = new com.emis.test.emisServletContext();
      Properties oProps_ = new Properties();
      oProps_.load(new FileInputStream("c:/resin/epos.cfg"));
      emisFileMgr _oFileMgr = new emisFileMgr(oContext,oProps_);
      emisLocalFileFactory o = new emisLocalFileFactory();
      System.out.println("emisLocalFileFactory=" + o);
      System.out.println("getWriter=" + o.getWriter("images","update.gif","a"));
    }
 */
}