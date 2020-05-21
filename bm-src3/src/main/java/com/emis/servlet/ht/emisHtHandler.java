package com.emis.servlet.ht;

import com.emis.db.emisDb;
import com.emis.qa.emisServletContext;
import com.emis.server.emisServer;
import com.emis.server.emisServerFactory;
import com.emis.util.emisStrBuf;

import javax.servlet.ServletContext;
import java.io.FileReader;
import java.io.LineNumberReader;

/**
 * sequence 原則
 * 2.退貨單：G+門市代碼之前3碼+年份的最後1位+月份(1～C)+流水碼(4碼)
 * 4.轉出單：T+門市代碼之前3碼+年份的最後1位+月份(1～C)+流水碼(4碼)
 * 5.盤點單：O+門市代碼之前3碼+年份的最後1位+月份(1～C)+流水碼(4碼)
 */
public class emisHtHandler
{
  private emisStrBuf buf = new emisStrBuf(40);
  private ServletContext application_;

  public emisHtHandler(ServletContext application)
  {
    application_ = application;
  }


  public void processDataFile(String sCompanyNo,String sFileName) throws Exception
  {
    FileReader ofReader = new FileReader(sFileName);
    try {
      LineNumberReader oReader = new LineNumberReader(ofReader);
      try {
        String sLine = null;
        int nLen = 0;
        int nLine = 1;
        header1 = new emisHeaderIns();
        header2 = new emisHeaderBack();
        header3 = new emisHeaderShiftIn();
        header4 = new emisHeaderShiftOut();
        header5 = new emisHeaderCount();

        while( (sLine = oReader.readLine()) != null ) {
          nLen = sLine.length();
          if( nLen == 0 )
            continue;

          if( nLen < 2 ) {
            if( (nLen == 1) && (sLine.charAt(0) == 26) ) //(0x1A,EOF)
              continue;
            throw new Exception("line "+nLine+" length error:"+nLen);
          }
          try {
            processLine(sLine);
            nLine++;
//            System.out.println("'"+sLine+"'");

          } catch (Exception e ) {
            Exception ex = new Exception("line "+nLine+ " error:"+e.getMessage());
            throw ex;
          }
        }
      } finally {
        oReader = null;
      }
    } finally {
      ofReader.close();
      ofReader = null;
    }

    // after process all files
    // process all data to database
    // 每次盤點會佔用兩個 connection
    // 一個是用來拿 Sequence Number 的
    // 無法 rollback
//    emisDb oSeqDb = emisDb.getInstance(application_);
//    try {
//      oSeqDb.setDescription("盤點轉入給號作業");

      emisDb oDb = emisDb.getInstance(application_);
      try {
        // used to get sequence number
          oDb.setDescription("盤點轉入作業");
          oDb.setAutoCommit(false);
          /**
          header1.processToDatabase(oDb,oSeqDb,sCompanyNo);
          header2.processToDatabase(oDb,oSeqDb,sCompanyNo);
          header3.processToDatabase(oDb,oSeqDb,sCompanyNo);
          header4.processToDatabase(oDb,oSeqDb,sCompanyNo);
          header5.processToDatabase(oDb,oSeqDb,sCompanyNo);
          */
          header1.processToTmpDatabase(oDb,sCompanyNo);
          header2.processToTmpDatabase(oDb,sCompanyNo);
          header3.processToTmpDatabase(oDb,sCompanyNo);
          header4.processToTmpDatabase(oDb,sCompanyNo);
          header5.processToTmpDatabase(oDb,sCompanyNo);
          /** 最後把資料搬到 last 子目錄去
          // 下一次才可以避免重覆
          emisDirectory last = emisFileMgr.getInstance(application_)
                               .getDirectory("root").subDirectory("data")
                               .subDirectory("ht").subDirectory(sCompanyNo)
          f.copyTo(last);
          */
          oDb.commit();
      } catch (Exception e) {
        try {          oDb.rollback();        } catch(Exception ignore) {        }        throw e;
      } finally {
        oDb.close();
        oDb = null;
      }

//    }finally {
//      oSeqDb.close();
//      oSeqDb = null;
//    }

    //start thread to pump data
    //don't let two thread run on concurrent
    emisHtThread ht= new emisHtThread(application_);
    ht.increaseCount();
    if(! ht.isConcurrentRunning() ) {
      ht.start();
    }

  }

  private emisHtHeader header1,header2,header3,header4,header5;
  private emisHtHeader header;
  private emisHtHeader lastHeader;


  private boolean isLastLineHeaderData_ = false;

  private void processLine(String sLine) throws Exception
  {
    char skind = sLine.charAt(1);

    lastHeader = header;

    if( skind == '1') {
      header = header1;
    } else
    // 退貨資料
    if( skind == '2') {
      header = header2;
    } else
    // 轉貨驗收
    if( skind == '3') {
      header = header3;
    } else
    // 轉出
    if( skind == '4') {
      header = header4;
    } else
    // 盤點
    if( skind == '5') {
      header = header5;
    } else {
      throw new Exception("unknow record kind in second bit:"+skind);
    }

    if( (lastHeader != header) && (lastHeader != null) ) // 換資料種類了
    {
      if(isLastLineHeaderData_)
        throw new Exception("header with no body data");
    }

    int ckind = sLine.charAt(0);
    buf.assign(sLine);
    if( ckind == 3 ) // header
    {
      if( isLastLineHeaderData_ ) {
        throw new Exception("sequential header data");
      }
      isLastLineHeaderData_ = true;
      header.lineToData(buf);
    } else
    if( ckind == 5 ) // body
    {
      isLastLineHeaderData_ = false;
      header.addLineData(buf);
    } else {
      throw new Exception("unknow record kind in first bit:" + ckind);
    }
  }


  // test program
  public static void main(String [] args) throws Exception
  {
    ServletContext oContext_ = new emisServletContext();
    emisServer oServer_ = emisServerFactory.createServer(oContext_,"C:\\wwwroot\\les\\","C:\\resin\\les.cfg",false);

    emisHtHandler h = new emisHtHandler(oContext_);
    h.processDataFile("01","D:\\TEMP\\HT.TXT");
  }

}
