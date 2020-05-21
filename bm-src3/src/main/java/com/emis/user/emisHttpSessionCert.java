/**

 *  $Id: emisHttpSessionCert.java 5726 2016-08-05 09:59:06Z andy.he $
 *  Track+[10977] Dana.gao 2008/06/12  解決自行輸入網址的漏洞
 */

package com.emis.user;



import com.emis.trace.emisError;

import com.emis.trace.emisTracer;
import com.emis.util.emisLangRes;


import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpSession;

import java.lang.reflect.Constructor;

import java.util.Properties;



/**

 * 認證物件的實作;建立程序: 

 *   emisServerImpl._initCert()->emisCertFactory.createCert()->new emisHttpSessionCert()
 *   Track+[11753] zhong.xu 2008/10/09 增加權限控制
 *  Track+[14652] tommer.xie 2010/03/31 針對常常第一次都入的時候都會無法登入，故先mark掉對session ID的處理

 */

public class emisHttpSessionCert implements emisCert

{

    private static final String USER_DEFAULT_SOURCE = "com.emis.user.emisUserImpl";

    private boolean isCheckPassword_ = true;

    private emisUserMonitor oMonitor_ = new emisUserMonitor();

    private ServletContext oContext_;

    

    /** 

     * 使用者類別的建構元物件;即/resin/xxx.cfg中的emis.user.source指定類別之

     * Constructor

     */

    private Constructor oUserSource_ = null;



    /**

     * emisCertFactory.createCert()建立emisHttpSessionCert物件

     * 由/resin/xxx.cfg中讀取設定 

     *     emis.user.source=com.emis.user.emisUserImpl

     * 以其指定的類別作為使用者物件之實作物件; 不同專案可變動上述類別來實作不同

     * 的使用者物件

     */

    public emisHttpSessionCert(ServletContext oContext,Properties oProp) throws Exception

    {

        oContext_ = oContext;

        if( oContext_.getAttribute(emisCert.STR_EMIS_CERT) != null )

        {

            throw new Exception(this + " already registered in ServletContext ");

        }

        oContext_.setAttribute(emisCert.STR_EMIS_CERT,this);



        String _sSecurityMode = oProp.getProperty("com.emis.security","true");



        // 設成 false, 任何人都不會檢查密碼

        if( _sSecurityMode.equalsIgnoreCase("false") )

        {

            isCheckPassword_ = false;

        }

        String sUserDefaultSource = oProp.getProperty("emis.user.source");

        if ((sUserDefaultSource == null) || ("".equals(sUserDefaultSource)) )

        {

          sUserDefaultSource = USER_DEFAULT_SOURCE;

        }

        // 使用Reflection來達成不同使用者類別之自動指定功能

        Class oUserClass = Class.forName(sUserDefaultSource);

        Class param [] = {

          javax.servlet.ServletContext.class, // ServletContext

          java.lang.String.class,  // sStoreNo

          java.lang.String.class,  // sExtraInfo

          java.lang.String.class,  // sUserID

          java.lang.String.class,  // sPassWord

          java.lang.Boolean.class,  // isCheckPasswd

          java.lang.String.class  };  // sSessionId

        oUserSource_ = oUserClass.getConstructor(param);

    } // emisHttpSessionCert


    
    /**

     * 依傳入的參數來登入, 登入成功後將使用者物件放入session物件內

     *

     * @param sExtraInfo "C,COMPANY_NO", "U,USER_TYPE"

     * @author Jerry on 2001/11/09

     */

    public emisUser login(HttpServletRequest request,String sStoreNo,String sExtraInfo, String sUserID,String sPassWord) throws Exception

    {

        HttpSession _oSess = request.getSession();

        if( _oSess == null )

        {
            emisTracer.get(oContext_).sysError(this,emisError.ERR_USER_GET_NULL_SESSION,"in login");
        }

        // mark by tommer.xie 2010/03/31 由於同一個session ID登錄失敗，故先mark，不作處理     
        // IE 不同頁籤會用同一個 Session,我們開不同的 頁籤 會 share 同一個 session ,會有問題
        // 這個檢查可以避免錯誤
        // we have successful get user object , check if it is the same id 
//        emisUser _oPrevUser = (emisUser) _oSess.getAttribute(emisCertFactory.EMIS_USER_BIND_NAME);
//        if( _oPrevUser != null ) {
//        		throw new Exception("Session already have logined user , Please Open a new Browser");
//        }

        String _sSessionId = _oSess.getId();
        

        Object param [] = new Object[7];

//        emisUser _oUser = new emisUserImpl(oContext_,sStoreNo,sUserID,sPassWord,isCheckPassword_,_sSessionId);

        param[0]= oContext_;

        param[1]= sStoreNo;

        param[2]= sExtraInfo;

        param[3]= sUserID;

        param[4]= sPassWord;

        param[5]= new Boolean(isCheckPassword_);

        param[6]= _sSessionId;

        // 2010/05/12 Joe Login成功后設定當前User選定的語言記錄，方便加語系條件過濾資料
        if (request.getParameter("languageType") != null && !"".equals(request.getParameter("languageType").trim())) {
          _oSess.setAttribute("languageType", request.getParameter("languageType").trim());
        } else if (_oSess.getAttribute("languageType") == null) {
          _oSess.setAttribute("languageType", request.getLocale().toString());
        }
        emisLangRes.setUserLang(sUserID, (String) _oSess.getAttribute("languageType"));


        /* oUserSource_為emisHttpSessionCert(Currently); 於constructor中由.cfg的

         * emis.user.source取得; 

         * 預設emis.user.source=com.emis.user.emisUserImpl

         * 因為emisUserImpl要7個參數,在此必須備妥放入param

         */

        emisUser _oUser = (emisUser) oUserSource_.newInstance(param);

        //获取用户登录的IP并寄存到LOGIN_IP属性中，在emisAudit.audit中使用，保存到userlog表中。
        _oUser.setAttribute("LOGIN_IP", request.getRemoteAddr());
        

        // 將使用者物件存入session物件中
    	_oSess.setAttribute(emisCertFactory.EMIS_USER_BIND_NAME,_oUser);
    	return _oUser;

    }
    // robert 2010/02/08 新增 check user/password but not do login action
    public boolean checkLogin(HttpServletRequest request,String sStoreNo,String sExtraInfo, String sUserID,String sPassWord) throws Exception {

    	emisUser _oUser = null;
    	try {
	        HttpSession _oSess = request.getSession();
	
	        if( _oSess == null )
	        {
	            return false;
	        }
	        
	        String _sSessionId = _oSess.getId();
	        
	
	        Object param [] = new Object[7];
	        param[0]= oContext_;
	        param[1]= sStoreNo;
	        param[2]= sExtraInfo;
	        param[3]= sUserID;
	        param[4]= sPassWord;
	        param[5]= new Boolean(isCheckPassword_);
	        param[6]= _sSessionId;
	
	        _oUser = (emisUser) oUserSource_.newInstance(param);
    	} catch (Exception e) {
    		return false;
    	} finally {
    		_oUser = null;
    	}
    	return true;
    }

    

    /** 以USERID與PASSWD登入 */

    public emisUser login(HttpServletRequest request,String sUserID,String sPassWord) throws Exception

    {

       return login(request,null,null,sUserID,sPassWord);

    }

    

    /** 以S_NO, USERID與PASSWD登入 */

    public emisUser login(HttpServletRequest request, String sStoreNo, String userid, String password) throws Exception {

        return login(request, sStoreNo, null, userid, password);

    }

    
    

    /**

     * 取得使用者物件; 在login()內已將emisUser物件存入session了

     */

    public emisUser getUser( HttpServletRequest request ) throws Exception

    {

        HttpSession _oSess = request.getSession(false);

        if( _oSess == null )

        {

            emisTracer.get(oContext_).sysError(this,emisError.ERR_USER_GET_NULL_SESSION);

        }



        emisUser _oUser = (emisUser) _oSess.getAttribute(emisCertFactory.EMIS_USER_BIND_NAME);

        if( _oUser == null )

        {

            emisTracer.get(oContext_).sysError(this,emisError.ERR_USER_NOGET_FROM_SESSION);

        } else {   // Track+[11753]
          String _sPath = request.getServletPath().substring(1).toUpperCase();  // remove first slash.          
          Object obj = request.getSession().getAttribute("EMIS.MENUS");
          if (obj != null) {
        //update by fang 區分大小寫
        int _iIndex = _sPath.indexOf("JSP/EMIS_");  // emis_data.jsp, emis_save.jsp, ...
            if (_iIndex < 0) {
              _iIndex = _sPath.indexOf("_");  // 表頭aaa.jsp, 表身aaa_detl.jsp
              if (_iIndex > 0) {
                _sPath = _sPath.substring(0, _iIndex);  // 只比較到aaa
              }
          //取得用戶可用菜單
              String _sMenus = (String) request.getSession().getAttribute("EMIS.MENUS");
          //取得全部菜單
              String _sMenus_All = (String) request.getSession().getAttribute("EMIS.MENUS_All");
              //out.println("URI=" + _sPath+","+_sMenus.indexOf(_sPath));
          // 用戶點擊的菜單在全部菜單中存在,但在用戶可用菜單中不存在,則拋出異常
              if (_sMenus_All.indexOf(_sPath) > 0 && _sMenus.indexOf(_sPath) < 0) {
                emisTracer.get(oContext_).sysError(this, emisError.ERR_USER_NOGET_FROM_SESSION);
              }
            }
          }
        }

        return _oUser;

    }



    /**

     * 取得使用者監控物件

     */

    public emisUserMonitor getUserMonitor()

    {

        return oMonitor_;

    }

}

