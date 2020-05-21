package com.emis.rights;
import javax.servlet.ServletContext;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Jacky
 * Date: 2005/6/15
 * Time: 上午 10:20:48
 * [3606]提供各項權限的基本原件
 */
public abstract class emisDyRightsComponent {
  protected HashMap Rights = null;  //權限元件集合
  protected ServletContext Context = null;  //伺服器資訊物件
  protected String UserGroups = null;   //擁有此權限的使用者群組
  protected String UserID = null;      //擁有此權限的使用者

  protected emisDyRightsComponent(ServletContext context) {
    Context = context;
  }

  /**
   * 傳回權限設定
   * @return
   */
  public HashMap getRights(){
    return Rights;
  }

  /**
   * 設定權限清單
   * @param rights
   */
  public void setRights(HashMap rights) {
    Rights = rights;
  }

  /**
   * 取得伺服器資訊物件
   * @return
   */
  public ServletContext getContext() {
    return Context;
  }

  /**
   * 設定伺服器資訊物件
   * @param context
   */
  public void setContext(ServletContext context) {
    Context = context;
  }

  /**
   * 取得使用者群組
   * @return
   */
  public String getUserGroups() {
    return UserGroups;
  }

  /**
   * 設定使用者群組
   * @param userGroups
   */
  public void setUserGroups(String userGroups) {
    UserGroups = userGroups;
  }

  /**
   * 設定使用者ID
   * @return
   */
  public String getUserID() {
    return UserID;
  }

  /**
   * 設定使用者ID
   * @param userID
   */
  public void setUserID(String userID) {
    UserID = userID;
  }
}
