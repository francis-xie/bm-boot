/* $Id: emisEncodingTransfer.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.db;

import com.emis.util.emisUtil;

/**
 * 此 Class 負責做 com.emis.db.emisDb 內部的字元轉換.
 *
 * @author Robert
 * @version 2004/08/10 Jerry: add x-windows-950 convert to MS950
 * @see com.emis.db.emisDb
 */
public class emisEncodingTransfer {
  /** 系統字元集 */
  protected String SysCharset_ = emisUtil.FILENCODING;
  /** 資料庫字元集 */
  protected String DbCharset_;
  /** 轉換類型 */
  protected int nTransferMode_;

  /**
   * Constructor.
   * @param dbEncoding
   * @param transferMode
   */
  public emisEncodingTransfer(String dbEncoding, int transferMode) {
    DbCharset_ = dbEncoding;
    nTransferMode_ = transferMode;
  }

  /**
   * System charset to DB charset.
   * @param sStr
   * @return
   */
  public String sysToDb(String sStr) {
    if ((nTransferMode_ & emisDbConnector.TRANSFER_SYS_TO_DB) > 0) {
      if (sStr == null) return null;
      try {
        sStr = new String(sStr.getBytes(SysCharset_), DbCharset_);
      //    System.out.println("1["+new String(sStr.getBytes("ISO8859_1"), "MS950")+"]");
      //    System.out.println("2["+new String(sStr.getBytes("ISO8859-1"), "UTF-8")+"]");
      //    System.out.println("3["+new String(sStr.getBytes("ISO8859-1"), "ISO8859-1")+"]");
      //    System.out.println("4["+new String(sStr.getBytes("MS950"), "ISO8859-1")+"]");
      //    System.out.println("5["+new String(sStr.getBytes("MS950"), "UTF-8")+"]");
      //    System.out.println("6["+new String(sStr.getBytes("MS950"), "MS950")+"]");
      //    System.out.println("7["+new String(sStr.getBytes("UTF-8"), "ISO8859_1")+"]");
      //    System.out.println("8["+new String(sStr.getBytes("UTF-8"), "UTF-8")+"]");
      //    System.out.println("9["+ Escape.expand(sStr)+"]");


         // sStr = new String((Escape.expand(sStr)).getBytes(DbCharset_), SysCharset_);
          // sStr = new String((Escape.UCSHexStringToUnicide(sStr)));
      //    System.out.println("sysToDb["+sStr+"]");
       //   sStr= Escape.expand(sStr);
         // sStr = new String(sStr.getBytes("ISO8859_1"), "UTF-8");
         //  System.out.println("[sStr.getBytes("+SysCharset_+"),"+ DbCharset_+"]");
         // System.out.println("sysToDb["+sStr+"]");
      } catch (Exception ignore) {
        System.err.println("[sysToDb] " + ignore.toString());
      }
    }
    return sStr;
  }

  /**
   * DB charset to System charset.
   * @param sStr
   * @return
   */
  public String dbToSys(String sStr) {
    if ((nTransferMode_ & emisDbConnector.TRANSFER_DB_TO_SYS) > 0) {
      if (sStr == null) return null;
      try {
      /*
       //    System.out.println("10112["+ Escape.unicodeToUCSHexString(sStr)+"]");
          System.out.println("1["+new String(sStr.getBytes("ISO8859_1"), "MS950")+"]");
          System.out.println("2["+new String(sStr.getBytes("ISO8859-1"), "UTF-8")+"]");
          System.out.println("3["+new String(sStr.getBytes("ISO8859-1"), "ISO8859-1")+"]");
          System.out.println("4["+new String(sStr.getBytes("MS950"), "ISO8859-1")+"]");
          System.out.println("5["+new String(sStr.getBytes("MS950"), "UTF-8")+"]");
          System.out.println("6["+new String(sStr.getBytes("MS950"), "MS950")+"]");
          System.out.println("7["+new String(sStr.getBytes("UTF-8"), "ISO8859_1")+"]");
          System.out.println("8["+new String(sStr.getBytes("UTF-8"), "UTF-8")+"]");
          System.out.println("8["+new String(sStr.getBytes("UTF-8"), "MS950")+"]");
          System.out.println("9["+ Escape.expand(sStr)+"]");
          System.out.println("10["+ Escape.unExpand(sStr)+"]");
         */
                 // sStr = new String((Escape.unExpand(sStr)).getBytes(DbCharset_), SysCharset_);
       //    sStr = new String(Escape.unExpand(sStr));
       //   sStr = new String(sStr.getBytes("ISO8859_1"), "UTF-8");
       //   sStr= Escape.unExpand(sStr);

         //  sStr = new String(sStr.getBytes("ISO8859-1"), "UTF-8");
         //  sStr = new String(sStr.getBytes("ISO8859-1"), "ISO8859-1");
         //  sStr = new String(sStr.getBytes("UTF-8"), "UTF-8");

        //  sStr = new String(sStr.getBytes("UTF-8"), "ISO8859-1");


       //   System.out.println("1["+new String(sStr.getBytes("ISO8859_1"), "MS950"));
       //   System.out.println("2["+new String(sStr.getBytes("ISO8859_1"), "ISO8859_1"));
        //  System.out.println("3["+new String(sStr.getBytes("ISO8859_1"), "UTF-8"));


          sStr = new String(sStr.getBytes(DbCharset_), SysCharset_);

         // System.out.println(sStr.getBytes("+DbCharset_+"),"+ SysCharset_+"]");

      } catch (Exception ignore) {
        System.err.println("[dbToSys] " + ignore.toString());
      }
    }
    return sStr;
  }

  /**
   * set transfer mode.
   * @param nMode
   */
  public void setTransferMode(int nMode) {
    nTransferMode_ = nMode;
  }
}