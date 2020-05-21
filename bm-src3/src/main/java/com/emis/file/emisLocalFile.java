/*
 * $Id: emisLocalFile.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) EMIS Corp.
 */
package com.emis.file;

import java.io.*;

/**
 *  local 檔案系統對 emisFile interface 的實作
 */
public class emisLocalFile implements emisFile {
  /** 目錄物件 */
  private emisDirectory oDirectory_;
  /** 檔名 */
  private String sFileName_;
  /** 檔案物件 */
  private File oFile_;

  /**
   * 以傳入的目錄物件與檔名形成 File 物件, 並將之傳回.
   */
  protected emisLocalFile(emisDirectory oDirectory, String sFileName) {
    oDirectory_ = oDirectory;
    sFileName_ = sFileName;
    oFile_ = new File(oDirectory_.getDirectory() + sFileName);
  }

  /**
   * 檔案是否存在. implements emisFile.exists()
   */
  public boolean exists() {
    return oFile_.exists();
  }

  /**
   * 檔案最後的修改時間. implements emisFile.lastModified()
   */
  public long lastModified() {
    return oFile_.lastModified();
  }

  /**
   * 取得Writer. implements emisFile.getWriter()
   */
  public PrintWriter getWriter(String sMode) throws Exception {
    if (sMode == null) sMode = "";
    sMode = sMode.toUpperCase();

    FileOutputStream _oFOutput = null;
    boolean _isAppend = false;
    boolean _isAutoFlush = false;

    // is open for append
    if (sMode.indexOf("A") != -1) _isAppend = true;
    if (sMode.indexOf("F") != -1) _isAutoFlush = true;
    _oFOutput = new FileOutputStream(oDirectory_.getDirectory() + sFileName_, _isAppend);
    OutputStreamWriter _oFOutputWriter = null;
    try {
      _oFOutputWriter = new OutputStreamWriter(_oFOutput);
    } catch (Exception e) {
      try {
        _oFOutput.close();
      } catch (Exception ignore) {
      }
      e.fillInStackTrace();
      throw e;
    }

    BufferedWriter _oBufWriter = null;

    try {
      _oBufWriter = new BufferedWriter(_oFOutputWriter);
    } catch (Exception e1) {
      try {
        _oFOutputWriter.close();
      } catch (Exception ignore1) {
      }
      e1.fillInStackTrace();
      throw e1;
    }

    PrintWriter _oPWriter = null;

    try {
      _oPWriter = new PrintWriter(_oBufWriter, _isAutoFlush);
    } catch (Exception e2) {
      try {
        _oBufWriter.close();
      } catch (Exception ignore2) {
      }
      e2.fillInStackTrace();
      throw e2;
    }
    return _oPWriter;
  }

  /**
   * 取得Reader. implements emisFile.getReader()
   */
  public BufferedReader getReader() throws Exception {
    FileReader _oFReader = new FileReader(oDirectory_.getDirectory() + sFileName_);
    try {
      return new BufferedReader(_oFReader);
    } catch (Exception e2) {
      try {
        _oFReader.close();
      } catch (Exception ignore3) {
      }
      throw e2;
    }
  }

  /**
   * 取得带编码格式的Reader. implements emisFile.getReader(charsetName)
   */
  public BufferedReader getReader(String charsetName) throws Exception {
    InputStreamReader _oISReader = new InputStreamReader(new FileInputStream(oDirectory_.getDirectory() + sFileName_), charsetName);
    try {
      return new BufferedReader(_oISReader);
    } catch (Exception e2) {
      try {
        _oISReader.close();
      } catch (Exception ignore3) {
      }
      throw e2;
    }
  }

  /**
   * 取得InputStream. implements emisFile.getInStream()
   */
  public InputStream getInStream() throws Exception {
    return new FileInputStream(oDirectory_.getDirectory() + sFileName_);
  }

  /**
   * 取得OutputStream. implements emisFile.getOutStream()
   */
  public OutputStream getOutStream(String sMode) throws Exception {
    boolean isAppend = false;
    if (sMode != null) {
      sMode = sMode.toUpperCase();
      if (sMode.indexOf("A") != -1) isAppend = true;
    }
    return new FileOutputStream(oDirectory_.getDirectory() + sFileName_, isAppend);
  }

  /**
   * 取得現行的目錄物件. implements emisFile.getDirectory()
   */
  public emisDirectory getDirectory() {
    return oDirectory_;
  }

  /**
   * 取得目前的檔名. implements emisFile.getFileName()
   */
  public String getFileName() {
    return sFileName_;
  }

  /**
   * 取得目前檔名的副檔名. implements emisFile.getFileExt()
   */
  public String getFileExt() {
    if (sFileName_ != null) {
      int idx = -1;
      if ((idx = sFileName_.lastIndexOf(".")) != -1) {
        return sFileName_.substring(idx + 1);
      }
    }
    return "";
  }

  /**
   * 傳回目前的短檔名.implements emisFile.getShortName()
   */
  public String getShortName() {
    if (sFileName_ != null) {
      int idx = -1;
      if ((idx = sFileName_.lastIndexOf(".")) != -1) {
        return sFileName_.substring(0, idx);
      }
    }
    return sFileName_;
  }

  /**
   * 傳回檔案的大小. implements emisFile.getSize()
   */
  public long getSize() {
    return oFile_.length();
  }

  public boolean delete() {
    return oFile_.delete();
  }

  public boolean equals(emisFile f) {
    if (getFileName().equalsIgnoreCase(f.getFileName()))
      if (getSize() == f.getSize())
        if (lastModified() == f.lastModified())
          return true;
    return false;
  }

  public emisFile moveTo(emisDirectory to) throws Exception {
    return renameTo(to, sFileName_);
  }

  public emisFile renameTo(emisDirectory to, String sNewName) throws Exception {
    if (to == null) throw new Exception("unable rename to null directory");
    File dest = new File(to.getDirectory() + sNewName);
    if (oFile_.renameTo(dest)) {
      return new emisLocalFile(to, sNewName);
    }
    throw new Exception("unable rename file :" + this.getFullName() + " to " + to.getDirectory() + sNewName);
  }

  public emisFile rename(String sNewName) throws Exception {
    return renameTo(oDirectory_, sNewName);
  }

  public emisFile renameTo(emisDirectory to, String sNewName, boolean IfExistsForceDelete) throws Exception {
    if (to == null) throw new Exception("unable rename to null directory");
    File dest = new File(to.getDirectory() + sNewName);
    if (dest.exists() && dest.isFile() && IfExistsForceDelete) {
      dest.delete();
    }
    if (oFile_.renameTo(dest)) {
      return new emisLocalFile(to, sNewName);
    }
    throw new Exception("unable rename file :" + this.getFullName() + " to " + to.getDirectory() + sNewName);
  }

  public emisFile rename(String sNewName, boolean IfExistsForceDelete) throws Exception {
    return renameTo(oDirectory_, sNewName, IfExistsForceDelete);
  }

  /**
   *  拷貝檔案至某目錄
   */
  public emisFile copyTo(emisDirectory to) throws Exception {
    FileOutputStream os = new FileOutputStream(to.getDirectory() + sFileName_);
    try {
      BufferedOutputStream bos = new BufferedOutputStream(os);
      FileInputStream is = new FileInputStream(oDirectory_.getDirectory() + sFileName_);
      try {
        byte[] buf = new byte[8192];
        int readed;
        BufferedInputStream bis = new BufferedInputStream(is);
        while ((readed = bis.read(buf)) != -1) {
          bos.write(buf, 0, readed);
        }
        bos.flush();
      } finally {
        is.close();
        is = null;
      }
    } finally {
      os.close();
      os = null;
    }
    return new emisLocalFile(to, sFileName_);
  }

  /**
   *  拷貝檔案至某目錄(指定檔名)
   */
  public emisFile copyTo(emisDirectory to, String sNewName) throws Exception {
    FileOutputStream os = new FileOutputStream(to.getDirectory() + sNewName);
    try {
      BufferedOutputStream bos = new BufferedOutputStream(os);
      FileInputStream is = new FileInputStream(oDirectory_.getDirectory() + sFileName_);
      try {
        byte[] buf = new byte[8192];
        int readed;
        BufferedInputStream bis = new BufferedInputStream(is);
        while ((readed = bis.read(buf)) != -1) {
          bos.write(buf, 0, readed);
        }
        bos.flush();
      } finally {
        is.close();
        is = null;
      }
    } finally {
      os.close();
      os = null;
    }
    return new emisLocalFile(to, sNewName);
  }

  /**
   *  拷貝檔案至某目錄下，指定檔名及拷貝模式
   */
  public emisFile copyTo(emisDirectory to, String sNewName, boolean isAppend)
      throws Exception {
    FileOutputStream os = new FileOutputStream(to.getDirectory() + sNewName, isAppend);
    try {
      BufferedOutputStream bos = new BufferedOutputStream(os);
      FileInputStream is = new FileInputStream(oDirectory_.getDirectory() + sFileName_);
      try {
        byte[] buf = new byte[8192];
        int readed;
        BufferedInputStream bis = new BufferedInputStream(is);
        while ((readed = bis.read(buf)) != -1) {
          bos.write(buf, 0, readed);
        }
        bos.flush();
      } finally {
        is.close();
        is = null;
      }
    } finally {
      os.close();
      os = null;
    }
    return new emisLocalFile(to, sNewName);
  }

  public String getFullName() {
    return oDirectory_.getDirectory() + sFileName_;
  }

  public long length() {
    return oFile_.length();
  }

  /**
   * 測試檔案是否可以寫入,避免別人正在寫入,而我們開來讀.用open試著打開, 若能打開則認定為可寫,
   * 否則認定為無法寫入; 測試完畢後將檔案立即關閉. implements emisFile.canWrite()
   *
   * @param sFileName 要測試的檔名
   * @return true=可寫, false=不能寫
   * @version 1.0 2002/06/25 Jerry
   */
  public boolean canWrite() {
    try {
        // it is important to test write ablity when we will open
        // a file to read, since it is possible file is upload
        // we can't use java.io.File.canWrite , because it only
        // use file attribute

        //   FileOutputStream f = new FileOutputStream(sFileName_,true);
        FileInputStream f = null;
        try {
            f = new FileInputStream(oDirectory_.getDirectory() + sFileName_);

        } catch (Exception ignore) {
            throw ignore;
        } finally {
            if (f != null)
                f.close();
            f = null;
        }
        return true;
    } catch (Exception err) {
        return false;
    }
  }

}