/* $Id: emisFieldFormatBean.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.db;

/**
 * 存放 FieldFormat table一筆記錄的物件.
 * @author Jerry
 * @version 2004/08/09
 */
public class emisFieldFormatBean {
  private String type = "";
  private int maxLen = 0;
  private String validation = "";
  private String picture = "";
  private String leftZero = "";
  private int size = 0;

  //[1175] Jacky
  private int ZeroCnt = 0;

  /**
   * Constructor. 傳入一筆記錄各欄位資料以建立物件.
   * @param sFD_MAXLEN
   * @param sFD_VALIDATION
   * @param sFD_PICTURE
   * @param sFD_LEFTZERO
   */
  public emisFieldFormatBean(String sFD_TYPE,int sFD_MAXLEN, String sFD_VALIDATION,
      String sFD_PICTURE,
      String sFD_LEFTZERO) {
    this.type = getNotNullValue(sFD_TYPE);
    this.maxLen = sFD_MAXLEN;
    this.validation = getNotNullValue(sFD_VALIDATION);
    this.picture = getNotNullValue(sFD_PICTURE);
    this.leftZero = getNotNullValue(sFD_LEFTZERO);
  }

  /**
   * 將null轉成空字串.
   * @param sData
   * @return
   */
  private String getNotNullValue(String sData) {
    return (sData == null) ? "" : sData;
  }

  /**
   * 傳回欄名(FD_TYPE).
   * @return
   */
  public String getType() {
    return type;
  }

  /**
   * 設定欄名(FD_TYPE).
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * 取欄寬.
   * @return
   */
  public int getMaxLen() {
    return maxLen;
  }

  /**
   * 設欄寬.
   * @param maxLen
   */
  public void setMaxLen(int maxLen) {
    this.maxLen = maxLen;
  }
 /**

   * 取欄寬.

   * @return

   */

  public int getSize() {

    return size;

  }



  /**

   * 設欄寬.

   * @param size

   */

  public void setSize(int size) {

    this.size = size;

  }



  /**
   * 取檢核字串: NUMBER8_2, NUMBER1_4, NUMBER3_4, NUMBER8_2, NUMBER等.
   * @return
   */
  public String getValidation() {
    return validation;
  }

  /**
   * 設檢核字串: NUMBER8_2, NUMBER1_4, NUMBER3_4, NUMBER8_2, NUMBER等.
   * @param validation
   */
  public void setValidation(String validation) {
    this.validation = validation;
  }

  /**
   * 取設欄位格式: $, 9, B, N, U等.
   * @return
   */
  public String getPicture() {
    return picture;
  }

  /**
   * 設欄位格式: $, 9, B, N, U等.
   * @param picture
   */
  public void setPicture(String picture) {
    this.picture = picture;
  }

  /**
   * 取是否左補零("Y").
   * @return
   */
  public String getLeftZero() {
    return leftZero;
  }

  /**
   * 設是否左補零("Y").
   * @param leftZero
   */
  public void setLeftZero(String leftZero) {
    this.leftZero = leftZero;
  }

  /**
   * 取得左補零位數
   * @return
   * [1175] Jacky
   */
  public int getZeroCnt() {
    return ZeroCnt;
  }

  /**
   * 設定左補零位數
   * [1175] Jacky
   * */
  public void setZeroCnt(int zeroCnt) {
    ZeroCnt = zeroCnt;
  }
}
