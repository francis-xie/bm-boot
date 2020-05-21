/* $Id: emisAbstractFieldTag.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

import com.emis.db.emisFieldFormat;
import com.emis.db.emisFieldFormatBean;
import com.emis.util.emisLangRes;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * HTML Tag的上層類別.
 *
 * @author jacky
 * @version 2004/08/09 Jerry: 由emisFieldFormatBean取FieldFormat的值.
 */
public abstract class emisAbstractFieldTag extends TagSupport {
  /**
   * 外部傳遞參數參數; tag名稱
   */
  protected String sName_ = null;
  /**
   * 外部傳遞參數參數; 輸錯後的提示訊息
   */
  protected String sMessage_ = "";
  /**
   * 外部傳遞參數參數; 顯示長度
   */
  protected String sSize_ = null;
  /**
   * 外部傳遞參數參數; tag格式
   */
  protected String sType_ = null;
  /**
   * 外部傳遞參數參數; 頁籤數
   */
  protected String sPage_ = null;
  /**
   * 外部傳遞參數參數; 顯示型別
   */
  protected String sDisplayType_ = "";
  /**
   * 外部傳遞參數參數; 輸入字元檢查
   */
  protected String sPicture_ = null;

  protected String sId_ = null;

  /**
   * 內部處理參數; ServletContext
   */
  protected ServletContext oContext_ = null;
  /**
   * 內部處理參數; FD_VALIDATION
   */
  protected String sValidType_ = null;
  /**
   * 內部處理參數; 產生的字串
   */
  protected String sPattern_ = null;
  /**
   * 內部處理參數; 最入長度.
   */
  protected String sMaxLen_ = null;
 /**
   * 資源文件
   */
  protected String sBundle = null;
  /**
   * 資源文件中訊息標識
   */
  protected String sKey = null;
  /**
   * 下層實作.
   *
   * @return
   * @throws JspException
   */
  public abstract int doStartTag() throws JspException;

  /**
   * 傳回欄位格式物件.
   *
   * @param sFieldType
   * @return
   */
  public emisValidFormat getFieldFormat(String sFieldType) {
    sFieldType = sFieldType.toUpperCase();  // force to Uppercase
    emisValidFormat _oFormat = null;
    String sPicture = "";// todo age add 不同欄位的 picture 屬性重新取值。
    String sMaxLen = "";// todo age add 不同欄位的 sMaxLen 屬性重新取值。
    String sSize = "";// todo age add 不同欄位的 sSize 屬性重新取值。

    //若有_MAXLEN將原本的_MAXLEN移除
    if (sFieldType.indexOf("_MAXLEN") >= 0)
      sFieldType = sFieldType.substring(0, sFieldType.lastIndexOf("_MAXLEN"));

    try {
      emisFieldFormatBean bean = emisFieldFormat.getInstance(oContext_)
          .getBean(sFieldType);
      int _iMaxLen = bean.getMaxLen();
      int _iSize = bean.getSize();
      // Age upd 畫面有設定maxlen 時，以畫面的為主
      if (( sMaxLen_ == null || "".equals(sMaxLen_)) && _iMaxLen > 0) {
        sMaxLen = "" + _iMaxLen;
      }else{
        sMaxLen = this.sMaxLen_;
      }
      String _sLeftZero = bean.getLeftZero();
      sValidType_ = bean.getValidation().toUpperCase();

      //2004/03/22 Jacky 修正以讀取外部設定為準
      if (this.sPicture_ == null || "".equals(this.sPicture_)) {
//        sPicture_ = bean.getPicture().toUpperCase();
//      }
        // todo age add 不同欄位的 picture 屬性重新取值 start。
           sPicture = bean.getPicture().toUpperCase();
      }else{
        sPicture = this.sPicture_;
        // todo age add 不同欄位的 picture 屬性重新取值 end。
      }

      // Age upd 畫面有設定 Size 時，以畫面的為主
      if (( sSize_ == null || "".equals(sSize_)) && _iSize > 0) {
        sSize = "" + _iSize;
      }else{
        sSize = this.sSize_;
      }
      //  Age upd 畫面沒有設定 Size 時，以畫面的 Maxlen 為主
      if ( sSize == null || "".equals(sSize)){
        sSize = sMaxLen;
      }

      //!!! if (sPage_ == null) sPage_ = "";

      emisValidFactory _oFactory = new emisValidFactory();
      _oFormat = _oFactory.getValidFormat(sValidType_);
      _oFormat.setContext(oContext_);
      // 实现多语
      _oFormat.setLanguage((String) pageContext.getSession().getAttribute("languageType"));
      if (this.sBundle != null && !"".equals(this.sBundle)
          && this.sKey != null && !"".equals(this.sKey)) {
        emisLangRes lang = emisLangRes.getInstance(oContext_);
        lang.setLanguage((String) pageContext.getSession().getAttribute("languageType"));
        this.setmessage(lang.getMessage(this.sBundle, this.sKey));
      }
      _oFormat.setAlertMessage(this.sMessage_);
      _oFormat.setPage(this.sPage_);
      _oFormat.setIsRange(false);
      _oFormat.setFieldName(this.sName_);
      _oFormat.setSize(sSize);
      _oFormat.setMaxLen(sMaxLen);
      _oFormat.setDisplayType(this.sDisplayType_);
      _oFormat.setLeftZero(_sLeftZero);
      //[1175]
      _oFormat.setZeroCnt(bean.getZeroCnt());
      // todo age add 不同欄位的 picture 屬性重新取值 start 。
//      if (this.sPicture_ != null && !"".equals(this.sPicture_)) {
        _oFormat.setPicture(sPicture );
//      }
       // todo age add 不同欄位的 picture 屬性重新取值 end 。
    } catch (Exception e) {
      ; //- e.printStackTrace();  // 2004-08-07 Jerry removed for Test Unit
    } finally {
//      sSize_ = null;
    }
    return _oFormat;
  }

  /**
   * set name.
   *
   * @param x
   */
  public void setname(String x) {
    sName_ = x;
  }

  /**
   * set size.
   *
   * @param x
   */
  public void setsize(String x) {

    sSize_ = x;
  }

  /**
   * get name.
   *
   * @return
   */
  public String getname() {
    return sName_;
  }

  /**
   * get size.
   *
   * @return
   */
  public String getsize() {
    return sSize_;
  }

  /**
   * get type.
   *
   * @return
   */
  public String gettype() {
    return sType_;
  }

  /**
   * set type.
   *
   * @param sType
   */
  public void settype(String sType) {
    this.sType_ = sType;
  }

  /**
   * get message.
   *
   * @return
   */
  public String getmessage() {
    return sMessage_;
  }

  /**
   * set message.
   *
   * @param sMessage
   */
  public void setmessage(String sMessage) {
    this.sMessage_ = sMessage;
  }

  /**
   * get page.
   *
   * @return
   */
  public String getpage() {
    return sPage_;
  }

  /**
   * set page.
   *
   * @param sPage
   */
  public void setpage(String sPage) {
    this.sPage_ = sPage;
  }

  /**
   * get displayType.
   *
   * @return
   */
  public String getdisplaytype() {
    return sDisplayType_;
  }

  /**
   * set displayType.
   *
   * @param sDisplayType
   */
  public void setdisplaytype(String sDisplayType) {
    this.sDisplayType_ = sDisplayType;
  }

  /**
   * get picture.
   *
   * @return
   */
  public String getpicture() {
    return sPicture_;
  }

  /**
   * set picture.
   *
   * @param sPicture
   */
  public void setpicture(String sPicture) {
    this.sPicture_ = sPicture;
  }

  /**
   * get Max length.
   *
   * @param sMaxlen_
   */
  public void setmaxlen(String sMaxlen_) {
    this.sMaxLen_ = sMaxlen_;
  }

  public String getId() {
    return sId_;
  }

  public void setId(String sId) {
    this.sId_ = sId;
  }

  /**
   * 供測試程式使用.
   *
   * @param oContext
   */
  public void setServletContext(ServletContext oContext) {
    oContext_ = oContext;
  }

   public String getBundle() {
    return sBundle;
}

  public void setBundle(String sBundle) {
    this.sBundle = sBundle;
  }

  public String getKey() {
    return sKey;
  }

  public void setKey(String sKey) {
    this.sKey = sKey;
  }
}
