/* $Id: emisShowStore.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.taglibs.showdata;

import com.emis.db.emisFieldFormat;
import com.emis.db.emisFieldFormatBean;
import com.emis.db.emisSQLCache;
import com.emis.user.emisCertFactory;
import com.emis.user.emisUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/**
 * 显示门店HTML tag.
 * <p/>
 * Sample:
 * &lt;showdata:store name="QRY_S_NO" sqlcache="STORE_ALL" displaytype="N"/&gt;
 *
 * @author jacky
 * @version 2004/08/09 Jerry: refactor; 增加左补零功能
 * @version 2004/08/25 Jerry: fix LeftZero onkeypress naming error
 * @version 2004/08/31 Jerry: fix onchange error
 */
public class emisShowStore extends emisAbstractFieldTag {
  private static boolean _JUNIT_ = false;  // 用emisShowStoreTest测试时才改成true
  private String sSqlName_;
  private boolean isBlank = false;
  private emisValidFormat oFormat_;

  /**
   * start tag.
   *
   * @return
   * @throws JspException
   */
  public int doStartTag() throws JspException {
    JspWriter out = pageContext.getOut();
    oContext_ = pageContext.getServletContext();
    try {
      out.println(getPattern());
    } catch (Exception ex) {
      throw new JspTagException(ex.getMessage());
    }
    return 0;
  }

  /**
   * 传回HTML tag字串.
   *
   * @return
   * @throws Exception
   */
  public String getPattern() throws Exception {
    String _sSQLOutput = "";
    this.sType_ = "S_NO";

    try {
      HttpServletRequest _oRequest = (HttpServletRequest) pageContext.getRequest();
      emisUser _oUser = emisCertFactory.getUser(oContext_, _oRequest);

      if (sSqlName_ != null) {
        _sSQLOutput = emisSQLCache.getSQL(oContext_, sSqlName_, _oUser);
      }
    } catch (Exception e) {
      if (_JUNIT_) {
        _sSQLOutput = "<option value='000001'>000001<option value='000002'>000002" +
            "<option value='000001'>000001";
      }
      ;
    }

    oFormat_ = this.getFieldFormat(this.sType_);

    if (this.sPicture_ == null || "".equals(this.sPicture_)) {
      this.sPicture_ = "9";
    }

    if (sName_ == null || sName_.equals("")) {
      throw new JspTagException("invalid null or empty 'name'");
    } else {
      getFieldLen(this.sType_);
      StringBuffer _sbPattern = new StringBuffer();

      String _sFldNo = "";
      boolean _isRange = false;
      if (this.sDisplayType_.indexOf("R") >= 0) {
        _sFldNo = "1";
        _isRange = true;
      }
      genInputText(_sbPattern, _sFldNo);
      genSelect(_sbPattern, _sSQLOutput, _sFldNo);
      genStarSymbol(_sbPattern, _sFldNo);

      if (_isRange) {
        _sbPattern.append("&nbsp;～&nbsp;");
        genInputText(_sbPattern, "2");
        genSelect(_sbPattern, _sSQLOutput, "2");
        genStarSymbol(_sbPattern, "2");
      }
      return _sbPattern.toString();
    }
  }

  private void genStarSymbol(StringBuffer sbPattern, String sFldNo) {
    if (this.sDisplayType_.indexOf("*") >= 0) {
      sbPattern.append("<font id='fnt").append(sName_).append(sFldNo)
          .append("' color='red'>*</font>");
    }
  }

  /**
   * 产生门店的输入栏位.
   *
   * @param sbPattern
   * @param sFldNo
   */
  private void genInputText(StringBuffer sbPattern, String sFldNo) {
    sbPattern.append("<input name='").append(sName_).append(sFldNo);
    if ("".equals(sFldNo)) {
      sbPattern.append("' datasrc='#xmlData' DataFld='").append(sName_);
    }
    sbPattern.append("' size='").append(sSize_)
        .append("' maxlength='" + sMaxLen_)
        .append("' onkeypress=\" emisPicture('").append(this.sPicture_)
        .append("') \" style=\"ime-mode:disabled\" ")
        .append(" onkeyup=\"emisOptionSearch(document.all.sel")
        .append(sName_).append(sFldNo).append(", this.value)\" ");
    if ("Y".equalsIgnoreCase(oFormat_.getLeftZero())) {
      sbPattern.append(" onchange=\"if (document.all.sel").append(sName_)
        .append(sFldNo).append(".value!=''){ this.value = document.all.sel")
        .append(sName_).append(sFldNo).append(".value;} else { ")
        .append("this.value = emisPadl(this.value,").append(sMaxLen_)
        .append(", '0', true);} emisOptionSearch(document.all.sel")
        .append(sName_).append(sFldNo).append(", this.value);\"")
        .append("  onblur=\"this.value=document.all.sel"+ sName_+".value;\"")  ;
    } else {
      sbPattern.append(" onchange=\" this.value = document.all.sel")
          .append(sName_).append(sFldNo).append(".value\"");
    }
    sbPattern.append("/>\n");
  }

  /**
   * 产生门店的下拉选单 select.
   *
   * @param sPattern
   * @param sSQLOutput
   * @param sFldNo
   */
  private void genSelect(StringBuffer sPattern, String sSQLOutput, String sFldNo) {
    sPattern.append("<select name='sel" + sName_).append(sFldNo)
        .append("' datasrc='#xmlData' DataFld='")
        .append(sName_).append("' onchange='document.all.").append(sName_)
        .append(sFldNo).append(".value=this.value'>");
    if (sSqlName_ != null) {
      if (this.sDisplayType_.indexOf("N") >= 0) {
        sPattern.append("<option value='' > </option> \n");
      }
      sPattern.append(sSQLOutput);
    }
    sPattern.append("</select>");
  }

  public void setsqlcache(String sSqlName_) {
    this.sSqlName_ = sSqlName_;
  }

  /**
   * 由emisFieldFormat物件取出门店栏宽.
   */
  public void getFieldLen(String sType) {
    try {
      emisFieldFormatBean bean = emisFieldFormat.getInstance(oContext_)
          .getBean(sType);
      int _iMaxLen = bean.getMaxLen();
      if (_iMaxLen > 0) {
        sMaxLen_ = Integer.toString(_iMaxLen);
      }
      if (sSize_ == null || "".equals(sSize_)) {
        sSize_ = sMaxLen_;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
