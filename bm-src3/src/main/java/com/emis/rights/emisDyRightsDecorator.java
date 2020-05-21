package com.emis.rights;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Jacky
 * Date: 2005/6/15
 * Time: 上午 10:40:21
 * [3606] 動態附加權限的物件.
 */
public abstract class emisDyRightsDecorator {
  protected HashMap RightsAttributes = null;  //各屬性設定
  protected String RightsId = null; //此權限元件的ID
  protected String RightsType = null; //此權限元件的種類
  protected String RightsClass = null; //此權限元件的分類
  protected String RightsMenuKeys = null; //此權限對應的作業代碼
  protected int RightsSeq = 0; //畫面呈現之順序
  protected String RightsName = null; //名稱
  protected String RightsGroupID = null; //權限群組名稱
  protected boolean RightsEnable = true; //是否有此權限
  protected emisDyRptRightsComp MyContainer = null; //是否有此權限

  /**
   * 依據資料庫欄位名稱抓取屬性設定值
   * @param key
   * @return
   */
  public Object getAttributes(Object key){
    return RightsAttributes.get(key);
  }

  /**
   * 取得完整的屬性資料
   * @return
   */
  public HashMap getRightsAttributes() {
    return RightsAttributes;
  }

  /**
   * 設訂完整權限屬性
   * @param rightsAttributes
   */
  public void setRightsAttributes(HashMap rightsAttributes) {
    RightsAttributes = rightsAttributes;
  }

  /**
   * 取得權限的ID
   * @return
   */
  public String getRightsId() {
    return RightsId;
  }

  /**
   * 設定權限的ID
   * @param rightsId
   */
  public void setRightsId(String rightsId) {
    RightsId = rightsId;
  }

  /**
   * 取得權限種類
   * @return
   */
  public String getRightsType() {
    return RightsType;
  }

  /**
   * 設定權限種類
   * @param rightsType
   */
  public void setRightsType(String rightsType) {
    RightsType = rightsType;
  }

  /**
   * 取得權限的分類
   * @return
   */
  public String getRightsClass() {
    return RightsClass;
  }

  /**
   * 設定權限的分類
   * @param rightsClass
   */
  public void setRightsClass(String rightsClass) {
    RightsClass = rightsClass;
  }

  /**
   * 取得畫面顯示的順序
   * @return
   */
  public int getRightsSeq() {
    return RightsSeq;
  }

  /**
   *設定畫面顯示的順序
   * @param rightsSeq
   */
  public void setRightsSeq(int rightsSeq) {
    RightsSeq = rightsSeq;
  }

  /**
   * 取得權限名稱
   * @return
   */
  public String getRightsName() {
    return RightsName;
  }

  /**
   * 設定權限名稱
   * @param rightsName
   */
  public void setRightsName(String rightsName) {
    RightsName = rightsName;
  }

  /**
   * 取得權限群組ID
   * @return
   */
  public String getRightsGroupID() {
    return RightsGroupID;
  }

  /**
   * 設定權限群組ID
   * @param rightGroupID
   */
  public void setRightsGroupID(String rightGroupID) {
    RightsGroupID = rightGroupID;
  }

  /**
   * 設定是否有此權限
   * @param rightEnable
   */
  public void setRightsEnable(boolean rightEnable) {
    RightsEnable = rightEnable;
  }

  /**
   * 取得是否有此權限
   * @return
   */
  public boolean isEnable() {
    return RightsEnable;
  }

  /**
   * 取得作業代碼
   * @return
   */
  public String getRightsMenuKeys() {
    return RightsMenuKeys;
  }

  /**
   * 設訂作業代碼
   * @param rightsMenuKeys
   */
  public void setRightsMenuKeys(String rightsMenuKeys) {
    RightsMenuKeys = rightsMenuKeys;
  }

  /**
   * 提供每個物件權限的Primary Key取法
   * @return
   */
  public String getPrimaryKeys() {
    return this.getRightsMenuKeys()+this.getRightsClass()+this.getRightsType()+this.getRightsId();
  }

  /**
   * 取得擁有此項權限的主元件
   * @return
   */
  public emisDyRptRightsComp getMyContainer() {
    return MyContainer;
  }

  /**
   * 設定擁有此項權限的主元件
   * @param myContainer
   */
  public void setMyContainer(emisDyRptRightsComp myContainer) {
    MyContainer = myContainer;
  }
}
