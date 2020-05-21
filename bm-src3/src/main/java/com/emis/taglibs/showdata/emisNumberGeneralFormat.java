package com.emis.taglibs.showdata;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2010-6-3
 * Time: 17:33:03
 *  lisa.huang 增加整數位數和小數位數自定義的格式
 */
public class emisNumberGeneralFormat extends emisValidFormat{
     /**
   * HTML tag.
   */
  public emisNumberGeneralFormat (String type) {
    this.sType_ = type;
    setPicture("N");
  }

  /**
   * onblur string.
   * @return
   */
  protected String getOnBlur() {
    StringBuffer _sbPattern = new StringBuffer();
    if (this.sType_ != null && this.sType_.toUpperCase().startsWith("NUMBER") && this.sType_.length() > 6) {
      String num = this.sType_.substring(6);
      String[] nums = num.split("_");
      String iLength = "1", dLength = "0";
      if (nums.length == 1) {
        iLength = nums[0];
        dLength = "0";
      } else if (nums.length >= 2) {
        iLength = nums[0];
        dLength = nums[1];
      }
      try {
        iLength = String.valueOf(Integer.parseInt(iLength));
      } catch (Exception e) {
        iLength = "0";
      }
      try {
        dLength = String.valueOf(Integer.parseInt(dLength));
      } catch (Exception e) {
        dLength = "0";
      }
      if (iLength != "0") {
        _sbPattern.append(" decimal=" + dLength);
        _sbPattern.append(" onblur=\" (emisOnblurBefore(this)) && (emisNumValid(this, " + iLength + "," + dLength + ", '")
            .append(this.sAlertMessage_).append("', '',").append(this.sPage_)
            .append(" )) ");
      }
    } else {
      _sbPattern.append(" onblur=\" (emisOnblurBefore(this))");
    }

    if ("Y".equalsIgnoreCase(this.sLeftZero_)) {
      _sbPattern.append(" && (this.value = emisPadl(this.value,")
          .append(sMaxLen_).append(", '0', true ))");
    }
    _sbPattern.append(" && (emisOnblurAfter(this)) ");  // 2005/12/01 andy add
    _sbPattern.append("\"");  // onblur=的第結尾雙引號
    return _sbPattern.toString();
  }

  /**
   * onfocus string.
   * @return
   */
  protected String getOnFocus() {
    return " onfocus=\"emisQrySel(document.getElementsByName('" + this.sFieldName_ + "1')[0], this)\" ";
  }

  /**
   * HTML string.
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
}
