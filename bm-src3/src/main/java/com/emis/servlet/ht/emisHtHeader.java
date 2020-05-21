package com.emis.servlet.ht;

import com.emis.db.emisDb;
import com.emis.util.emisStrBuf;

public interface emisHtHeader
{
  void lineToData(emisStrBuf sLine) throws Exception;
  void addLineData(emisStrBuf sLine) throws Exception;
  void processToDatabase(emisDb oDb,emisDb oSeqDb,String sCompanyNo) throws Exception;
  // 2002.4.23 robert 新加
  // 因為效能的考量,直接給號並加到 Table 會造成速度上的問題
  // 造成前端誤以為當掉,所以修改為先加到一暫存 Table
  // 還有新加一 HT_MAXNO 來取得盤點單的 C_SN 號碼
  // 這樣會比較快
  void processToTmpDatabase(emisDb oDb,String sCompanyNo) throws Exception;
}