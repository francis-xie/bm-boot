/* $Header: /repository/src3/src/com/emis/taglibs/showdata/emisValidFormat.java,v 1.1.1.1 2005/10/14 12:43:08 andy Exp $

 *

 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.

 *  2005/04/26 [3041] Jacky 增加前端onblur前後的Hook Function

 */

package com.emis.taglibs.showdata;



import com.emis.db.emisProp;
import com.emis.util.emisLangRes;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;


import javax.servlet.ServletContext;





/**

 * HTML格式字串的產生類別.

 * 說明文件: eros_doc/cvn/畫面標籤(jspf)檔說明.doc

 *

 * "R":表示 以區間方式輸入

 * "B":表示區間輸入並帶有全部按鈕

 * "S":表示帶有」…」按鈕

 * "*":表示帶有紅色*符號

 * "A":表示插入一個span 顯示文字

 * "C":開啟日曆視窗

 * 這幾種可以組合方式設定

 *    "…"按鈕名稱為」btn」+name

 * 若為區間的話按鈕名稱為

 *    "btn"+name+"1","btn"+name+"2"



 * @author Jacky

 * @version 2004/08/07 Jerry: add more methods to support emisDateFormat

 * @version 2004/08/25 Jerry: add funcButton style-class & tabindex=-1 to calendar

 * @version 2004/10/19 [1166 ]  Jacky 修正"U"英數狀態不允許輸入中文

 * @version 2004/11/02   Jacky 移除U"英數狀態不允許輸入中文的修改

 */

public abstract class emisValidFormat {

  /**日期格式 */

  protected String sType_;

  /** 頁籤的頁數 */

  protected String sPage_;

  /** 判斷是否是區間輸入模式 */

  protected boolean isRange_ = false;

  /** 使用者鍵盤控制 */

  protected String sPicture_ = "";

  /** 使用者鍵盤控制 */

  protected String sBtnPicture_ = "";

  /** 警告訊息 */

  protected String sAlertMessage_ = "";

  /** 欄位名稱 */

  protected String sFieldName_ = "";

  /** 欄位的顯示大小 */

  protected String sSize_ = "10";

  /** 欄位的實際輸入大小 */

  protected String sMaxLen_ = "10";

  /** 顯示型別 */

  protected String sDisplayType_ = "";

  /** 是否提供中文輸入法 */

  protected String sIMEMode_ = "";

  /** 是否左補0 Y:左補 */

  protected String sLeftZero_ = "";

  /** onblur的檢核字串 */

  protected String sValid_ = "";

  /** onfocus的檢核字串 */

  protected String sFocus_ = "";

  protected ServletContext oContext_;

  /** GUI set */

  protected String sGuiSet_ = "1";



  /**

   * 內部處理參數; 左補零位數.

   * [1175] add y Jacky

   */

  protected int iZeroCnt_ = 0 ;


  // 多语实现-当前语言
  protected String sLanguage_ = "EN";



  /**

   * 設定輸入格式.

   *

   * @param sPicture

   */

  public void setPicture(String sPicture) {

    this.sPicture_ = sPicture;

    if ("U".equalsIgnoreCase(sPicture) || "A".equalsIgnoreCase(sPicture) ||

        "B".equalsIgnoreCase(sPicture)) {

      this.sBtnPicture_ = "Z";

    } else {

      this.sBtnPicture_ = "9";

    }

    // [1166 ]
    if ("$9ABCDNS".indexOf(this.sPicture_) >= 0 && !"".equals(sPicture)) {  // 不許進入中文輸入模式

      this.sIMEMode_ = " style=\"ime-mode : disabled\" ";

    }

  }



  /**

   * 設定目前處理的頁籤頁數.

   *

   * @param sPage

   */

  protected void setPage(String sPage) {

    sPage_ = sPage;

  }



  /**

   * 設定為區間模式.

   *

   * @param isRange

   */

  protected void setIsRange(boolean isRange) {

    isRange_ = isRange;

  }



  /**

   * 設定警告訊息.

   *

   * @param sAlertMessage

   */

  public void setAlertMessage(String sAlertMessage) {

    this.sAlertMessage_ = sAlertMessage;

  }



  /**

   * 設定欄位名稱.

   *

   * @param sFieldName

   */

  public void setFieldName(String sFieldName) {

    this.sFieldName_ = sFieldName;

  }



  /**

   * 設定顯示的大小.

   *

   * @param sSize

   */

  public void setSize(String sSize) {

    this.sSize_ = sSize;

  }



  /**

   * 設定輸入時字元的限制.

   *

   * @param sMaxLen

   */

  public void setMaxLen(String sMaxLen) {

    this.sMaxLen_ = sMaxLen;

  }



  /**

   * 設定顯示型別.

   *

   * @param sDisplayType

   */

  public void setDisplayType(String sDisplayType) {

    this.sDisplayType_ = sDisplayType;

  }



  /**

   * 取得檢核格式.

   *

   * @return

   */

  protected abstract String getPattern();



  /**

   * 取得是否左補0.

   *

   * @return

   */

  public String getLeftZero() {

    return sLeftZero_;

  }



  /**

   * 設定是否左補0.

   *

   * @param sLeftZero_

   */

  public void setLeftZero(String sLeftZero_) {

    this.sLeftZero_ = sLeftZero_;

  }



  /**

   * 設定檢核字串.

   *

   * @param sValid

   */

  protected void setOnBlur(String sValid) {

    sValid_ = sValid;

  }



  public ServletContext getContext() {

    return oContext_;

  }



  public void setContext(ServletContext oContext) {

    oContext_ = oContext;

  }



  /**

   * 取回onblur字串.

   *

   * @return

   */

  protected String getOnBlur() {
    // Age upd 商品編碼查詢時可輸入同國際條碼同樣的位數
    String sMaxLen ="";
    if(this.sFieldName_.indexOf("P_NO") >= 0){
      sMaxLen = sSize_;
    }else{
      sMaxLen = sMaxLen_;
    }
    

    if ("".equals(sValid_)) {

      StringBuffer _sbResult = new StringBuffer();



      //2005/04/26 [3041] Jacky 增加前端onblur前後的Hook Function

      _sbResult.append(" onblur=\"!this.disabled && !this.readOnly && (emisOnblurBefore(this)) && emisLengthValid(this,")

          .append(sMaxLen).append(",'").append(this.sAlertMessage_)

          .append("', '',").append(this.sPage_).append(")");

      //2004/05/24 Jacky 增加左補零機制

      //2004/11/10 [1175] Jakcy 增加補零位數

      if ("Y".equalsIgnoreCase(this.sLeftZero_)) {

        _sbResult.append(" && (emisOpenSelectWindow(this,")

            .append(this.iZeroCnt_).append(", '0', true ))");

      }

      _sbResult.append("&& (emisOnblurAfter(this)) \"");  // onblur=的第結尾雙引號

      return _sbResult.toString();

    } else {

      return sValid_;

    }

  }



  /**

   * 設定檢核字串.

   *

   * @param sFocus

   */

  protected void setOnFocus(String sFocus) {

    sFocus_ = sFocus;

  }



  /**

   * 取回onfocus字串.

   *

   * @return

   */

  protected String getOnFocus() {

    if ("".equals(sFocus_)) {

      StringBuffer _sbResult = new StringBuffer();

      _sbResult.append("onfocus='emisQrySel(document.all.")

          .append(this.sFieldName_).append("1, this)'");

      return _sbResult.toString();

    } else {

      return sFocus_;

    }

  }



  /**

   * 產生輸入用的&lt;tag&gt;字串.

   *

   * @param sbPattern

   * @param sFldNo

   */

  protected void genInputTag(StringBuffer sbPattern, String sFldNo) {

    if (oContext_ != null) {

      try {

        emisProp prop = emisProp.getInstance(oContext_);

        String _sGuiSet = prop.get("EROS_GUI_SET");

        if (_sGuiSet != null && !"".equals(_sGuiSet)) {

          sGuiSet_ = _sGuiSet;

        }

      } catch (Exception e) {

        ;

      }

    }

    genInputText(sbPattern, sFldNo);  // input type='text'



    if (this.sDisplayType_.indexOf("S") >= 0) {

      genSelectButton(sbPattern, sFldNo);  // input type='button' for select

    }

    if (this.sDisplayType_.indexOf("*") >= 0) {

      genFont(sbPattern, sFldNo);  // red-font '*'

    }



    if (this.sDisplayType_.indexOf("A") >= 0) {

      genSpan(sbPattern, sFldNo);  // span

    }



    if (this.sDisplayType_.indexOf("C") >= 0 && this.sDisplayType_.indexOf("!C") < 0) {

      genCalendar(sbPattern, sFldNo);  // Calendar

    }

  }



  /**

   * input 字串. xxx1, xxx2; 例: S_NO1 ~ S_NO2

   * 第二個欄位需要多一個onfocus.

   *

   * @param sbPattern

   * @param sFldNo

   */

  protected void genInputText(StringBuffer sbPattern, String sFldNo) {
    // Age upd 商品編碼查詢時可輸入同國際條碼同樣的位數
    String sMaxLen ="";
    if(this.sFieldName_.indexOf("P_NO") >= 0){
      sMaxLen = sSize_;
    }else{
      sMaxLen = sMaxLen_;
    }

    sbPattern.append("<input type='text' DataSrc='#xmlData'  DataFld='")

        .append(this.sFieldName_).append("' name='").append(this.sFieldName_).append(sFldNo)

        .append("' id='").append(this.sFieldName_).append(sFldNo)

        .append("' size='").append(sSize_)

        .append("' maxlength='").append(sMaxLen)

        .append("' onkeypress=\"emisPicture('")

        .append(this.sPicture_).append("',this)\" ").append(this.sIMEMode_);

    if ("2".equals(sFldNo)) {

      sbPattern.append(getOnFocus());

    }

    sbPattern.append(getOnBlur());



    sbPattern.append(">\n");

  }



  /**

   * 產生&lt;input type='button'&gt; 的按鈕tag.

   *

   * @param sbPattern

   * @param sFldNo

   */

  private void genSelectButton(StringBuffer sbPattern, String sFldNo) {

    if ("2".equals(sGuiSet_)) {

      sbPattern.append("<img src='../../images/search.png' ")

//        .append("tabindex='-1' title='開啟選擇視窗' name='btn")
        .append("tabindex='-1' title='"+getMessage("VF_OPEN_SELECTOR")+"' name='btn")

        .append(this.sFieldName_).append(sFldNo).append("' style='cursor:hand'></img>");

    } else {

      sbPattern.append("<input type='button' value='...' ")

//        .append("tabindex='-1' title='開啟選擇視窗' name='btn")
        .append("tabindex='-1' title='"+getMessage("VF_OPEN_SELECTOR")+"' name='btn")

        .append(this.sFieldName_).append(sFldNo).append("' style='cursor:hand'>");

    }

  }



  /**

   * 產生 "全部" 的按鈕.

   *

   * @param sbPattern

   */

  protected void genInputAll(StringBuffer sbPattern) {

    if (this.sDisplayType_.indexOf("B") >= 0) {

      sbPattern.append("&nbsp;<button id='btn").append(this.sFieldName_)

//          .append("_All' type='button' title='[帶入/清除]全部資料值' tabindex='-1' ");
          .append("_All' type='button' title='"+getMessage("VF_ALL_OR_CLEAR")+"' tabindex='-1' ");

      if ("2".equals(sGuiSet_)) {

        sbPattern.append("class='FuncButton' ");

      }

      sbPattern.append(" onclick=\"emisNumAll(")

          .append(this.sFieldName_).append("1,").append(this.sFieldName_)

          .append("2 ,").append(sMaxLen_).append(",'").append(this.sBtnPicture_)

          .append("')\">\n");

      if ("2".equals(sGuiSet_)) {

        sbPattern.append("<img src='../../images/all.png'></img>");

      }

//      sbPattern.append("全部</button>");
      sbPattern.append(getMessage("VF_BTN_ALL_TEXT")+"</button>");

    }

  }



  /**

   * 產生 &lt;span&gt;.

   *

   * @param sbPattern

   * @param sFldNo

   */

  private void genSpan(StringBuffer sbPattern, String sFldNo) {

    sbPattern.append(" <span id='spa").append(this.sFieldName_)

        .append(sFldNo).append("'> </span>");

  }



  /**

   * 產生必須輸入的紅色星號.

   *

   * @param sbPattern

   * @param sFldNo

   */

  private void genFont(StringBuffer sbPattern, String sFldNo) {

    sbPattern.append("<font id='fnt").append(this.sFieldName_)

        .append(sFldNo).append("' color='red'>*</font>");

  }



  /**

   * 產生日曆視窗(js/popcalendar.js).

   * 2004/12/09 Jerry: img元件加入欄位名稱

   * @param sbPattern

   * @param sFldNo

   */

  private void genCalendar(StringBuffer sbPattern, String sFldNo) {

    sbPattern.append("  <img name='imgCalendar").append(sFieldName_).append(sFldNo)

      .append("' style='cursor:hand' ")

      .append("tabindex='-1' ")

//      .append("title='開啟日曆視窗' onclick='popUpCalendar(this, ")
      .append("title='" + getMessage("VF_OPEN_CALENDAR") + "' onclick='popUpCalendar(this, ")

      .append(sFieldName_).append(sFldNo)

      .append(", \"yyyy/mm/dd\")' src='../../images/calendar.png'>");

//    sbPattern.append("<button name='btnCalendar' onclick='popUpCalendar(this, ")

//      .append(sFieldName_).append(sFldNo)

//      .append(", \"yyyy/mm/dd\")'><img name='imgCalendar' src='../../images/calendar.png'></button>");

//    sbPattern.append("<input type='button' onclick='popUpCalendar(this, ")

//      .append(sFieldName_).append(sFldNo)

//      .append(", \"yyyy/mm/dd\")' value='日曆' class='funcButton'>");

  }



  /**

   * 取得左補零位數

   * @return

   */

  public int getZeroCnt() {

    return iZeroCnt_;

  }



  /**

   * 設定左補劉位數

   * @param iZeroCnt_

   */

  public void setZeroCnt(int iZeroCnt_) {

    this.iZeroCnt_ = iZeroCnt_;

  }

  /**
   * 设定当前语系
   * @param sLang
   */
  public void setLanguage(String sLang){
    this.sLanguage_ = sLang;
  }

  /**
   * 取得当前语系内容
   * @param key
   * @return
   */
  public String getMessage(String key){
    emisLangRes _oLang = null;
    try {
      _oLang = emisLangRes.getInstance(oContext_.getRealPath(emisLangRes.ResourceSubPath));
      _oLang.setLanguage(this.sLanguage_);
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    return _oLang == null ? "" : _oLang.getMessage("Application", key);
  }
}

