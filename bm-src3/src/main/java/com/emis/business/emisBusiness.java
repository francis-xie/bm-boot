/**

 *  20040513 abel add  extends java.io.Serializable  httpsession 需要 Serializable
 * Track+[15095] sunny.zhuang 2010/06/30 增加"我的工作清單" 

 */

package com.emis.business;





import com.emis.user.emisUser;



import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


public interface emisBusiness extends java.io.Serializable

{

    /**

     * 傳回 business name

     */

    String getName();

    String getID();

    String getReportSql();



    void process() throws Exception;

    void process(String sAction) throws Exception;

    void process(String sAction, Map oMap) throws Exception;
    void processAjax() throws Exception ;
    	
    
    String getConfigFile();

    emisUser getUser();

    HttpServletRequest getRequest();

    ServletContext getContext();

    void setWriter(Writer out);

    /**

     * 讓 parameter 可以獨立開來,是因為可以中途差入一個

     * 不同的 HttpServletRequest  與  HttpServletResponse

     */

    void setParameter(HttpServletRequest request,HttpServletResponse response);

    /**

     * 讓 parameter 可以獨立開來,是因為可以中途差入一個

     * 不同的 HttpServletRequest

     */

    void setParameter(HttpServletRequest request);



    /**

     * 可以讓你,在連續的 process 之間給便 request 的

     * parameter 的值

     */

    void setParameter(String sKey,String sValue);

    void setParameter(HashMap oMap) ;

    void clearParamter();



    void debug(String msg);

    void debug(Exception e);



    boolean isDebug();

    void setAttribute( Object key , Object value );

    Object getAttribute ( Object key );



    String errToMsg(Exception e);



    /**

     * 此 function 為系統內部 debug 用

     */

    void addReferenceCount();

    /**

     * 此 function 為系統內部 debug 用

     */

    void decReferenceCount();

    /**

     * 此 function 為系統內部 debug 用

     */



  /**

   * 2004/01/08 add by Jacky

   * 取得MenuCode簡碼

   * @return

   */



    public String getMenuCode();



  /**

   * 2004/03/15 提供前端JSP程式取得相對應的FIELDFORMAT對應TYPE的欄位寬度

   *

   * @param Type  對應到FiledFormat 內的FD_TYPE欄位

   * @return

   * @throws Exception

   */

  public int getFieldWidth(String Type) throws Exception ;



   /**

    * 2004/08/09: 提供前端JSP程式取得相對應的FIELDFORMAT對應TYPE的欄位是否左補零機制.

    *

    * @param sType  對應到FiledFormat 內的FD_TYPE欄位

    * @return

    * @throws Exception

    */

   public String getFieldLeftZero(String sType) throws Exception ;





}