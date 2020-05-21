/* $Id: emisNumberFormat.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

/**
 * Number format.
 * @author jacky
 * @version Apr 8, 2003, 6:34:16 PM
 * @version 2004/03/22 Jacky 修正無法從外部設定Picture的問題
 * @version 2004/08/10 Jerry: Refactor
 */
public class emisNumberFormat extends emisValidFormat {
  /**
   * Constructor.
   */
  public emisNumberFormat (){
    this.sType_ = "NUMBER";
    this.sPicture_ = "9";
  }

  /**
   * HTML Tags.
   * @return
   */
  public String getPattern() {
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
    String _sPattern = "";
    if (this.sDisplayType_.indexOf("R") >= 0 ) {
      _sPattern += "<input type=text DataSrc='#xmlData'  DataFld='"+ this.sFieldName_ +"' name='"+ this.sFieldName_ + "1' " ;
      _sPattern += " size=" + sSize_  ;
      _sPattern += " maxlength=" + sMaxLen_  ;
      _sPattern += " onkeypress=\" emisPicture('"+this.sPicture_+"') \" " + this.sIMEMode_;
      // _sPattern += " onkeyup=\"value=value.replace(/[\\u4E00-\\u9FA5]/g,'')\" ";
      _sPattern += "  onblur=\"emisNumValid(this,"+sMaxLen_+" , 0, '"+this.sAlertMessage_ +"', '',"+this.sPage_+" ) " ;

      //2004/05/24 Jacky 增加左補零機制
      if ("Y".equalsIgnoreCase(this.sLeftZero_)){
        _sPattern += "  &&  (this.value = emisPadl(this.value ,"+sMaxLen_+" , '0' , true ) ) " ;
      }
      _sPattern +="\" >";
      if (this.sDisplayType_.indexOf("S") >= 0 ) {
        _sPattern += " <input type=button value='...' tabindex='-1'  name='btn"+this.sFieldName_+"1'>";
      }
      if (this.sDisplayType_.indexOf("*") >= 0 ) {
        _sPattern += "<font id='fnt"+this.sFieldName_+"1' color=\"red\">*</font>";
      }
      if (this.sDisplayType_.indexOf("A") >= 0 ) {
        _sPattern += " <span  id='spa"+this.sFieldName_+"1'></span>";
      }
      _sPattern += "&nbsp;～&nbsp;" ;
      _sPattern += "<input type=text DataSrc='#xmlData'  DataFld='"+ this.sFieldName_ +"' name='"+ this.sFieldName_ + "2' " ;
      _sPattern += " size=" + sSize_  ;
      _sPattern += " maxlength=" + sMaxLen_  ;
      _sPattern += " onkeypress=\" emisPicture('"+this.sPicture_+"') \" " + this.sIMEMode_;
      _sPattern += " onfocus=\"emisQrySel(document.all." + this.sFieldName_ + "1, this)\" ";
      // _sPattern += " onkeyup=\"value=value.replace(/[\\u4E00-\\u9FA5]/g,'')\" ";
      _sPattern += "  onblur=\"emisNumValid(this,"+sMaxLen_+" , 0, '"+this.sAlertMessage_ +"', '',"+this.sPage_+" ) ";

      //2004/05/24 Jacky 增加左補零機制
      if ("Y".equalsIgnoreCase(this.sLeftZero_)){
        _sPattern += "  &&  (this.value = emisPadl(this.value ,"+sMaxLen_+" , '0'  , true) ) " ;
      }
      _sPattern +="\" >";

      if (this.sDisplayType_.indexOf("S") >= 0 ) {
        _sPattern += " <input type=button value='...' tabindex='-1'  name='btn"+this.sFieldName_+"2'>";
      }
      if (this.sDisplayType_.indexOf("*") >= 0 ) {
        _sPattern += "<font id='fnt"+this.sFieldName_+"2' color=\"red\">*</font>";
      }
      if (this.sDisplayType_.indexOf("A") >= 0 ) {
        _sPattern += " <span  id='spa"+this.sFieldName_+"2'></span>";
      }
      if (this.sDisplayType_.indexOf("B") >= 0 ) {
        _sPattern += " <input id='btn"+this.sFieldName_+"_All'  type=button value='全部' tabindex='-1' onclick=\"emisNumAll("+this.sFieldName_ +
                     "1," + this.sFieldName_ +"2 ,"+sMaxLen_+",'9')\">";
      }
    } else {
      _sPattern += "<input type=text DataSrc='#xmlData'  DataFld='"+ this.sFieldName_ +"' name='"+ this.sFieldName_ + "' " ;
      _sPattern += " size=" + sSize_  ;
      _sPattern += " maxlength=" + sMaxLen_  ;
      _sPattern += " onkeypress=\" emisPicture('"+this.sPicture_+"') \" " + this.sIMEMode_;
      // _sPattern += " onkeyup=\"value=value.replace(/[\\u4E00-\\u9FA5]/g,'')\" ";
      _sPattern += " onblur=\" emisNumValid(this,"+sMaxLen_+" , 0, '"+this.sAlertMessage_ +"', '',"+this.sPage_+" ) ";

      //2004/05/24 Jacky 增加左補零機制
      if ("Y".equalsIgnoreCase(this.sLeftZero_)){
        _sPattern += "  &&  (this.value = emisPadl(this.value ,"+sMaxLen_+" , '0', true  ) ) " ;
      }
      _sPattern +="\" >";

      if (this.sDisplayType_.indexOf("S") >= 0 ) {
        _sPattern += " <input type=button value='...' tabindex='-1'  name='btn"+this.sFieldName_+"'>";
      }
      if (this.sDisplayType_.indexOf("*") >= 0 ) {
        _sPattern += "<font id='fnt"+this.sFieldName_+"' color=\"red\">*</font>";
      }
      if (this.sDisplayType_.indexOf("A") >= 0 ) {
        _sPattern += " <span  id='spa"+this.sFieldName_+"'></span>";
      }
    }
    return _sPattern;
  }
*/
}
