package com.emis.rights;
import com.emis.db.emisDb;
import javax.servlet.ServletContext;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Jacky
 * Date: 2005/6/15
 * Time: 下午 03:07:47
 * [3606] 產生權限相關物件的抽像類別
 */
public abstract class emisAbstractRightsFactory {
  protected ServletContext Context = null; //伺服器相關物件

  protected emisAbstractRightsFactory(ServletContext context) {
    this.Context = context;
  }

  /**
   * 產生權限主要元件
   *
   * @return
   */
  public abstract emisDyRightsComponent createRightsComponent(String userid ,
                                                    String usergroup) throws Exception;


  /**
   * 產生權限個別權限資料
   * @param comp
   * @param userid
   * @param usergroup
   * @param db
   * @return
   * @throws Exception
   */
  protected abstract HashMap createRightsDecorators(emisDyRptRightsComp comp ,String userid ,
                                                    String usergroup , emisDb db) throws Exception;

}
