/* $Id: emisNumber12Format.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

/**
 * <showdata:text name="RPT_C_TAMT" type="$FLOAT82" maxlen="11" picture="N" />元
 * @author jacky
 * @version Apr 8, 2003, 6:34:16 PM
 * @version 2004/08/10 Jerry: refactor
 */
public class emisNumber12Format extends emisValidFormat {
  /**
   * Constructor.
   */
  public emisNumber12Format (){
    this.sType_ = "NUMBER1_2";
    setPicture("N");
  }

  /**
   * onblur string.
   * @return
   */
  protected String getOnBlur() {
    StringBuffer _sbPattern = new StringBuffer();
    _sbPattern.append(" onblur=\"emisNumValid(this, 1 ,2, '")
        .append(this.sAlertMessage_).append("', '',").append(this.sPage_)
        .append(" ) ");
    if ("Y".equalsIgnoreCase(this.sLeftZero_)) {
      _sbPattern.append(" && (this.value = emisPadl(this.value,")
          .append(sMaxLen_).append(", '0', true ))");
    }
    _sbPattern.append(" && (emisOnblurAfter(this)) ");  // 2005/12/01 andy add
    _sbPattern.append("\"");  // onblur=的第結尾雙引號
    return _sbPattern.toString();
  }

  /**
   * 取回onfocus字串.
   *
   * @return
   */
  protected String getOnFocus() {
    return " onfocus=\"emisQrySel(document.all." + this.sFieldName_ + "1, this)\" ";
  }

  /**
   * HTML Tags.
   * @return
   */
  protected String getPattern() {
    StringBuffer _sbPattern = new StringBuffer();

    boolean _isRange = false;
    String _sFldNo = "";
    if (this.sDisplayType_.indexOf("R") >= 0 ) {
      _sFldNo = "1";
      _isRange = true;
    }
    genInputTag(_sbPattern, _sFldNo);

    if (_isRange) {
      _sbPattern.append("&nbsp;～&nbsp;");
      genInputTag(_sbPattern, "2");
      genInputAll(_sbPattern);
    }
    return _sbPattern.toString();
  }

/*
  private String genInputText(StringBuffer sbPattern, String sFldNo) {
    sbPattern.append("<input type=text DataSrc='#xmlData'  DataFld='")
      .append(this.sFieldName_).append("' name='").append(this.sFieldName_)
      .append(sFldNo).append("' ").append(" size=").append(sSize_)
      .append(" maxlength=").append(sMaxLen_)
      .append(" onkeypress=\" emisPicture('").append(this.sPicture_)
      .append("') \" ").append(this.sIMEMode_)
      .append(" onblur=\"emisNumValid(this, 1 , 2, '")
      .append(this.sAlertMessage_).append("', '',").append(this.sPage_)
      .append(" ) ");
    //2004/05/24 Jacky 增加左補零機制
    if ("Y".equalsIgnoreCase(this.sLeftZero_)){
      sbPattern.append("  &&  (this.value = emisPadl(this.value ,")
          .append(sMaxLen_).append(" , '0' ,true ) ) ");
    }
    if ("2".equals(sFldNo)) {
      sbPattern.append(" onfocus=\"emisQrySel(document.all." + this.sFieldName_ + "1, this)\" ");
}
    sbPattern.append("\" >");
    return sbPattern.toString();
  }
*/

}
