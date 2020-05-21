package com.emis.report;

import java.util.ArrayList;

/**
 * 功能：報表 GROUP 類別
 * 說明：提供 Group Header & Footer 之定義及相關之操作
 * @author James Lee
 * @version 1.00, 07/09/01'
 *
 */
public class emisRptGroup {

  private String sName_;
  private emisRptSection oGroupHeader_;
  private emisRptSection oGroupFooter_;

  private boolean isBreak = false;
  private boolean isHeaderTriggered_ = true;
  private boolean isSubtotalInHeader_ = false;
  private boolean isTopN_ = true;

  private ArrayList alDependentFields_;
  private ArrayList alDerivedFields_;
  private ArrayList alGroupFields_;
  //wing添加,轉向數據轉換
  private boolean  resetDetlRotate=false;


  /**
  * 目的: 建構元
  */
  public emisRptGroup() {

    oGroupHeader_ = new emisRptSection();
    oGroupFooter_ = new emisRptSection();
    alDependentFields_ = new ArrayList();
    resetDetlRotate=false;

  }

  /**
  * 目的: 設定 group 屬性
  * @param  sPropertyName 屬性名稱
  * @param  sValue        屬性值
  * @return true/false 成功與否
  */
  public boolean setProperty(String sPropertyName, String sValue) {
    boolean _isOk = true;

    if ("NAME".equalsIgnoreCase(sPropertyName)) {
      sName_ = sValue;
    } else if ("NewPageAfter".equalsIgnoreCase(sPropertyName)) {
      setFooterProperty(sPropertyName, sValue);
    } else if ("ResetPageNumber".equalsIgnoreCase(sPropertyName)) {
      setFooterProperty(sPropertyName, sValue);
    } else if ("SubtotalInHeader".equalsIgnoreCase(sPropertyName)) {
      if ("TRUE".equalsIgnoreCase(sValue)) {
        isSubtotalInHeader_ = true;
      } else {
        isSubtotalInHeader_ = false;
      }
    //wing修改加入表身轉向計算器重設功能
    }  else if ("resetDetlRotate".equalsIgnoreCase(sPropertyName)) {
		if(sValue==null){
    		sValue="";
    		resetDetlRotate = false;
    	}else
        if ("TRUE".equalsIgnoreCase(sValue)) {
        	resetDetlRotate = true;
        } else {
            resetDetlRotate = false;
        }
    } 
    else {
      _isOk = false;
    }

    return _isOk;

  }

  /**
  * 目的: 設定 group header 屬性
  * @param  sPropertyName 屬性名稱
  * @param  sValue        屬性值
  */
  public void setHeaderProperty(String sPropertyName, String sValue) {


    oGroupHeader_.setProperty(sPropertyName, sValue);
  }

  /**
  * 目的: 設定 group footer 屬性
  * @param  sPropertyName 屬性名稱
  * @param  sValue        屬性值
  */
  public void setFooterProperty(String sPropertyName, String sValue) {

    oGroupFooter_.setProperty(sPropertyName, sValue);
  }

  /**
  * 目的: 檢查是否group break
  * @return true/false Break與否
  */
  public boolean isBreak() {
    boolean _isBreak = false;
    emisRptField _oField;

    for (int i = 0; i < alDependentFields_.size(); i++ ) {
      _oField = (emisRptField) alDependentFields_.get(i);
      if (_oField.isNewValue()) {
        _isBreak = true;
        break;
      }
    }

    return _isBreak;
  }

  /**
  * 目的: 登錄 Break 的判斷欄位
  * @param  oDependentField 屬性名稱
  * @return true/false 登錄成功與否
  */
  public boolean registerDependence(emisRptField oDependentField) {
    boolean _isSetupOk = false;

    if (alDependentFields_ == null) {
      alDependentFields_ = new ArrayList();
    }

    alDependentFields_.add(oDependentField);
    _isSetupOk = true;

    return _isSetupOk;
  }

  /**
  * 目的: 同一個 Group 中，若資料重複不印出的欄位之登錄
  * @param  oGroupField 資料重複不印出的欄位物件
  * @return true/false 登錄成功與否
  */
  public boolean registerGroupField(emisRptField oGroupField) {
    boolean _isSetupOk = false;

    if (alGroupFields_ == null) {
      alGroupFields_ = new ArrayList();
    }

    alGroupFields_.add(oGroupField);
    _isSetupOk = true;

    return _isSetupOk;
  }

  /**
  * 目的: 設定 Group Header 的觸發狀態
  * @param  isTriggerHeader 是否被觸發
  */
  public void setHeaderTriggered(boolean isTriggerHeader) {
    // 設定 表頭觸發 狀態
    isHeaderTriggered_ = isTriggerHeader;

  }

  /**
  * 目的: 檢查 Group Header 是否為觸發狀態
  * @return true/false 觸發與否
  */
  public boolean isHeaderTriggered() {

    return isHeaderTriggered_;
  }

  /**
  * 目的: 取得 Group Header 物件
  * @return Group Header 物件
  */
  public emisRptSection getGroupHeader() {

    return oGroupHeader_;
  }

  /**
  * 目的: 取得 Group Footer 物件
  * @return Group Footer 物件
  */
  public emisRptSection getGroupFooter() {

    return oGroupFooter_;
  }

  /**
  * 目的: 檢查 Group Footer 是否觸發後是否跳頁
  * @return true/false Group Footer 被觸發後跳頁與否
  */
  public boolean isNewPageAfterOnFooter() {

    return oGroupFooter_.isNewPageAfter();
  }

  /**
  * 目的: 檢查是否要將頁碼歸零
  * @return true/false Group Footer 歸零與否
  */
  public boolean isResetPageNumber() {

    return oGroupFooter_.isResetPageNumber();
  }

  /**
  * 目的: 設定頁碼歸零是否歸零之狀態
  * @param true/false 歸零狀態與否
  */
  public void resetPageNumber(boolean isReset) {
    String _sMode;

    if (isReset) {
      _sMode = "true";
    } else {
      _sMode = "false";
    }

    oGroupFooter_.setProperty("ResetPageNumber", _sMode);

  }

  /**
  * 目的: 檢查是否要在 Group Header 計算明細
  * @return true/false 計算與否
  */
  public boolean isSubtotalInHeader() {

    return isSubtotalInHeader_;

  }

  /**
  * 目的: 傳入欄位物件，登錄為 Derived Field
  * @param oField Derived Field 物件
  */
  public void registerDerivedField(emisRptField oField) {

    if ( alDerivedFields_ == null) {
      alDerivedFields_ = new ArrayList();
    }
    alDerivedFields_.add(oField);

  }

  /**
  * 目的: 傳入欄位物件，登錄為 Derived Field，並指定其計算順序
  * @param iIndex 計算順序
  * @param oField Derived Field 物件
  */
  public void registerDerivedField(int iIndex, emisRptField oField) {

    if (alDerivedFields_ == null) {
      alDerivedFields_ = new ArrayList();
    }
    if (iIndex > alDerivedFields_.size()) {
      int _iCnt = iIndex - alDerivedFields_.size() - 1;
      for (int i = 0; i < _iCnt; i++) {
        alDerivedFields_.add(null);
      }
      alDerivedFields_.add(oField);
    } else {
      alDerivedFields_.set(iIndex-1,oField);
    }
  }

  /**
  * 目的: 執行 Derived Field 的運算
  */
  public void processDerivedFields() {
    emisRptField _oField;

    if ( alDerivedFields_ != null) {
      for (int i = 0; i < alDerivedFields_.size(); i++) {
        _oField = (emisRptField) alDerivedFields_.get(i);
        if (_oField != null) {
          _oField.getResult();
        }
      }
    }
  }

  /**
  * 目的: 啟動 Group Fields 使其值重複時不印出
  */
  public void processGroupFields() {
    emisRptField _oField;

    if ( alGroupFields_ != null) {
      for (int i = 0; i < alGroupFields_.size(); i++) {
        _oField = (emisRptField) alGroupFields_.get(i);
        if (_oField != null) {
          _oField.setProperty("SUPPRESSIFDUPLICATE", "false");
        }
      }
    }
  }

  /**
  * 目的: 取消 Group Fields 使其值印出
  */
  public void resetGroupFields() {
  emisRptField _oField;

    if ( alGroupFields_ != null) {
      for (int i = 0; i < alGroupFields_.size(); i++) {
        _oField = (emisRptField) alGroupFields_.get(i);
        if (_oField != null) {
          _oField.setProperty("SUPPRESSIFDUPLICATE", "true");
        }
      }
    }
  }

  /**
  * 目的: 在 Group Header 檢查是否目前的筆數是否在前 N 名的排名內
  * @return true/false 在排名內與否
  */
  public boolean isHeaderTopN() {
    boolean _isTopN;
    if (oGroupHeader_.isTopN()) {
      isTopN_ = true;
      _isTopN = true;
    } else {
      isTopN_ = false;
      _isTopN = false;
    }

    return _isTopN;
  }

  /**
  * 目的: 在 Group Footer 檢查是否目前的筆數是否在前 N 名的排名內
  * @return true/false 在排名內與否
  */
  public boolean isFooterTopN() {
    return isTopN_;
  }

  /**
  * 目的: 設定 Group Footer 是否在前 N 名的排名內的狀態
  * @param isTopN 在排名內與否
  */
  public void setFooterTopN(boolean isTopN) {
    isTopN_ = isTopN;
  }

public String getSName_() {
	return sName_;
}

public void setSName_(String name_) {
	sName_ = name_;
}

public boolean isResetRotate() {
	return resetDetlRotate;
}

public void setResetRotate(boolean resetDetlRotate) {
	this.resetDetlRotate = resetDetlRotate;
}
}
