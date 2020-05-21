/**
 *  $Id: emisFile.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) EMIS Corp.
 */

package com.emis.file;

import java.io.BufferedReader;

import java.io.InputStream;

import java.io.OutputStream;

import java.io.PrintWriter;

/**
 * 群豐檔案系統中的檔名介面. 與java.io.File扮演的角色類似.
 */

public interface emisFile {

  /** 檔案是否存在? */
  boolean exists();

  /** 最後修改時間 */
  long lastModified();

  PrintWriter getWriter(String sMode) throws Exception;

  BufferedReader getReader() throws Exception;

  BufferedReader getReader(String charsetName) throws Exception;

  InputStream getInStream() throws Exception;

  OutputStream getOutStream(String sMode) throws Exception;

  emisDirectory getDirectory();

  /** 傳回檔名 ,"AAA.TXT" */
  String getFileName();

  /** 副檔名,不含 "." */
  String getFileExt();

  /** 檔名,不含 extension */
  String getShortName();

  /** 含 path */
  String getFullName();

  long getSize();

  boolean delete();

  /** 將 emisFile 搬到 to */
  emisFile moveTo(emisDirectory to) throws Exception;

  /** 將 emisFile 搬到 to ,並改成 sNewName */
  emisFile renameTo(emisDirectory to, String sNewName) throws Exception;

  emisFile rename(String sNewName) throws Exception;

  emisFile renameTo(emisDirectory to, String sNewName, boolean IfExistsForceDelete) throws Exception;

  emisFile rename(String sNewName, boolean IfExistsForceDelete) throws Exception;

  emisFile copyTo(emisDirectory to) throws Exception;

  emisFile copyTo(emisDirectory to, String sNewName) throws Exception;

  emisFile copyTo(emisDirectory to, String sNewName, boolean isAppend) throws Exception;

  boolean equals(emisFile f);

  long length();

  /** 開檔測試是否能寫入資料 */
  public boolean canWrite();

}