/* $Id: emisUserButton.java 4 2015-05-27 08:13:47Z andy.he $

 *

 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.

 */

package com.emis.user;



import com.emis.db.emisDb;



import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.io.Writer;

import java.sql.SQLException;

import java.util.ArrayList;

import java.util.Enumeration;

import java.util.HashMap;

import java.util.StringTokenizer;



/**

 * 寫出使用者可使用的按鈕到網頁上.

 *

 * @author Joe

 * @version 2003/09/16

 * @version 2004/08/25 Jerry: refactor; add comments; add "cursor:hand" style

 * @version 2004/08/26 Jerry: static HashMap will cause all webapps shared the same one

 */

public class emisUserButton {

  /** 存資料表Functions的資料 */

  //private HashMap mapFunctions_ = new HashMap();



  /**

   * 取得 FlowStatus Button 控制.

   *

   * @param oContext

   * @param sTaskTitle

   * @return buttons

   * @throws Exception

   */

  public static String getFlow(ServletContext oContext, String sTaskTitle) throws Exception {

    String _sButton = "";



    emisDb _oDB = emisDb.getInstance(oContext);

    _oDB.setDescription("emisUserButton.getFlow()");

    try {

      _oDB.prepareStmt("select FLS_NO,FLS_DBUTTON from FlowStatus where FLD_NO=?");

      _oDB.setString(1, sTaskTitle);

      _oDB.prepareQuery();



      boolean _bFirst = true;

      while (_oDB.next()) {

        // 格式: ED;btnConfCO,btnConfCL|PP;btnConfPP...

        // 以 "|" 分筆, 以 ";" 隔開 FLS_NO, FLS_DBUTTON 欄位

        _sButton = _sButton + (_bFirst ? "" : "|") +

          _oDB.getString("FLS_NO") + ";" + _oDB.getString("FLS_DBUTTON");

        _bFirst = false;

      }

    } catch (Exception e) {

      oContext.log("[emisUserButton.getFlow()] " + e.getMessage());

    } finally {

      _oDB.close();

    }



    return _sButton == null ? "" : _sButton;

  }



  /**

   * 取得 write Login User Function Button.

   *

   * @param oUser

   * @param sKeys

   * @param out

   */

  public static void writeFuncButton(emisUser oUser, String sKeys, Writer out) {

    emisPermission _oPerm = oUser.getMenuPermission(sKeys);

    Enumeration e = _oPerm.getAllPermission();

    while (e.hasMoreElements()) {

      // 取出此功能 ArrayList

      ArrayList _oMenuFunc = (ArrayList) e.nextElement();

      for (int i = 0; i < _oMenuFunc.size(); i++) {

        // 寫出 Button

        //ArrayList _oFunction = (ArrayList) _oMenuFunc.get(i);

        writeButton((ArrayList) _oMenuFunc.get(i), out);

      }

    }

  }



  /**

   * 取得 write 自定 List Function Button.

   *

   * @param oContext

   * @param sList

   * @param sSpace

   * @param out

   * @throws Exception

   */

  public static void writeListButton(ServletContext oContext,

                                     String sList, String sSpace, Writer out) throws Exception {

    emisDb _oDb = emisDb.getInstance(oContext);

    _oDb.setDescription("emisUserButton.writeListButton()");

    try {

      HashMap _mapFunctions = queryDbtoMap(_oDb);  // 存table: Functions



      StringTokenizer _stList = new StringTokenizer(sList, ",");

      StringTokenizer _stSpace = new StringTokenizer(sSpace == null ? "" : sSpace, ",");

      ArrayList _alFunc = null;

      while (_stList.hasMoreTokens()) {

        String _sFuncID = _stList.nextToken().trim();

        String _sSpace = "";

        if (_stSpace.hasMoreTokens())

          _sSpace = _stSpace.nextToken().trim();



        if (_mapFunctions.get(_sFuncID) != null) {

          _alFunc = (ArrayList) _mapFunctions.get(_sFuncID);

          _alFunc.add(_sSpace);

          writeButton(_alFunc, out);

        }

      }

    } finally {

      _oDb.close();

    }

  }

  /**
   * 依語系取得 write 自定 List Function Button.
   * @param oContext
   * @param sList
   * @param sSpace
   * @param out
   * @param _oRequest
   * @throws Exception
   */
  public static void writeListButton(ServletContext oContext,
                                     String sList, String sSpace, Writer out, HttpServletRequest _oRequest) throws Exception {
    emisDb _oDb = emisDb.getInstance(oContext);
    _oDb.setDescription("emisUserButton.writeListButton()");
    try {
      HashMap _mapFunctions = queryDbtoMap(_oDb, _oRequest);  // 存table: Functions
      StringTokenizer _stList = new StringTokenizer(sList, ",");
      StringTokenizer _stSpace = new StringTokenizer(sSpace == null ? "" : sSpace, ",");
      ArrayList _alFunc = null;
      while (_stList.hasMoreTokens()) {
        String _sFuncID = _stList.nextToken().trim();
        String _sSpace = "";
        if (_stSpace.hasMoreTokens())
          _sSpace = _stSpace.nextToken().trim();

        if (_mapFunctions.get(_sFuncID) != null) {
          _alFunc = (ArrayList) _mapFunctions.get(_sFuncID);
          _alFunc.add(_sSpace);
          writeButton(_alFunc, out);
        }
      }
    } finally {
      _oDb.close();
    }
  }

  /**

   * 存資料表Functions的資料.

   *

   * @param oDb

   * @return

   * @throws SQLException

   */

  private static HashMap queryDbtoMap(emisDb oDb) throws SQLException {

//    if (mapFunctions_.size() > 0) {

//      return mapFunctions_;

//    }

    HashMap _mapFunctions = new HashMap();

    oDb.prepareStmt("select FUNC_ID,BTN_NAME,BTN_ACCESSKEY,BTN_STYLE,BTN_TITLE,"+

      "BTN_TEXT,BTN_IMGID,BTN_IMGFILE from Functions");

    oDb.prepareQuery();

    String _sStyle = null;

    while (oDb.next()) {

      String _sFuncID = oDb.getString("FUNC_ID");



      // 將資料存至 mapFunctions_

      ArrayList _oData = new ArrayList();

      _oData.add("Y");  // 狀態

      _oData.add(oDb.getString("BTN_NAME"));

      _oData.add(oDb.getString("BTN_ACCESSKEY"));

      _oData.add(oDb.getString("BTN_STYLE"));

      _oData.add(oDb.getString("BTN_TITLE"));

      _oData.add(oDb.getString("BTN_TEXT"));

      _oData.add(oDb.getString("BTN_IMGID"));

      _oData.add(oDb.getString("BTN_IMGFILE"));

      _mapFunctions.put(_sFuncID, _oData);

    }

    return _mapFunctions;

  }

  /**
   * 依語系取得資料表Functions的資料.
   * @param oDb
   * @param _oRequest
   * @return
   * @throws SQLException
   */

  private static HashMap queryDbtoMap(emisDb oDb, HttpServletRequest _oRequest) throws SQLException {

//    if (mapFunctions_.size() > 0) {
//      return mapFunctions_;
//    }
    String languageType = (String) _oRequest.getSession().getAttribute("languageType");
    HashMap _mapFunctions = new HashMap();
    oDb.prepareStmt("select FUNC_ID,BTN_NAME,BTN_ACCESSKEY,BTN_STYLE,BTN_TITLE,"+
      "BTN_TEXT,BTN_IMGID,BTN_IMGFILE from Functions where NATIVE=?");
    oDb.setString(1, languageType);
    oDb.prepareQuery();
    String _sStyle = null;
    while (oDb.next()) {
      String _sFuncID = oDb.getString("FUNC_ID");
      // 將資料存至 mapFunctions_
      ArrayList _oData = new ArrayList();
      _oData.add("Y");  // 狀態
      _oData.add(oDb.getString("BTN_NAME"));
      _oData.add(oDb.getString("BTN_ACCESSKEY"));
      _oData.add(oDb.getString("BTN_STYLE"));
      _oData.add(oDb.getString("BTN_TITLE"));
      _oData.add(oDb.getString("BTN_TEXT"));
      _oData.add(oDb.getString("BTN_IMGID"));
      _oData.add(oDb.getString("BTN_IMGFILE"));
      _mapFunctions.put(_sFuncID, _oData);
    }

    return _mapFunctions;

  }

  /**

   * 產生 Functions Button.

   *

   * @param alButton

   * @param out

   */

  private static void writeButton(ArrayList alButton, Writer out) {

    String _sStatus = (String) alButton.get(0);

    String _sBtnName = (String) alButton.get(1);

    String _sBtnAccessKey = (String) alButton.get(2);

    String _sBtnStyle = (String) alButton.get(3);

    if (_sBtnStyle != null && _sBtnStyle.indexOf("cursor:") < 0) {

      _sBtnStyle = "cursor:hand;" + _sBtnStyle;

    }

    String _sBtnTitle = (String) alButton.get(4);

    String _sBtnText = (String) alButton.get(5);

    String _sBtnImgId = (String) alButton.get(6);

    String _sBtnImgFile = (String) alButton.get(7);

    String _sFuncSpace = (String) alButton.get(8);

    String _sBtnTitle2 =null;

    if(alButton.size()>10){
      _sBtnTitle2 = (String) alButton.get(10);
    }

    // _sStatus = "Y" 才處理此 Button

    if (isNotEmpty(_sStatus)) {

      StringBuffer _oBuf = new StringBuffer();

      _oBuf.append("<button type=\"button\"");

      if (isNotEmpty(_sBtnName))   {
        // 2010/05/12 Joe 增加Button屬性id(與Name相同)，方便前端用jQuery操作
        _oBuf.append(" id='").append(_sBtnName).append("'");
        _oBuf.append(" name='").append(_sBtnName).append("'");
      }

      //Track+[19001] dana 2011/11/16 改变表头按钮名称,只需在FUNC_NAME中插入一条sql即可,不需在页面另写代码.
      if (isNotEmpty(_sBtnTitle2))    //如果有定义func_name,优先抓func_name资料.

        _oBuf.append(" title='").append(_sBtnTitle2).append("'");

      else if (isNotEmpty(_sBtnTitle))

        _oBuf.append(" title='").append(_sBtnTitle).append("'");

      if (isNotEmpty(_sBtnAccessKey))

        _oBuf.append(" accesskey='").append(_sBtnAccessKey).append("'");

      if (isNotEmpty(_sBtnStyle))

        _oBuf.append(" style='").append(_sBtnStyle).append("'");

      if (!isNotEmpty(_sBtnText))
        _oBuf.append(" class='small'");

      _oBuf.append(">");



      if (isNotEmpty(_sBtnImgId) && isNotEmpty(_sBtnImgFile)) {

        _oBuf.append("<img id='")

          .append(_sBtnImgId).append("' src='")

          .append(_sBtnImgFile).append("'>");

      }



      if (isNotEmpty(_sBtnText))

        _oBuf.append(_sBtnText);



      if (isNotEmpty(_sBtnAccessKey))

        _oBuf.append("(<u>").append(_sBtnAccessKey).append("</u>)");



      _oBuf.append("</button>");



      if (isNotEmpty(_sFuncSpace)) {

        for (int i = 0; i < Integer.parseInt(_sFuncSpace); i++)

          _oBuf.append("&nbsp;");

      }

      _oBuf.append("\n");



      // 寫入 out

      try {

        out.write(_oBuf.toString());

      } catch (Exception e) {

        System.err.println("emisUserButton.writeButton: " + e.getMessage());

      }

    }

  }



  /**

   * 是否是null或空白的檢查.

   *

   * @param sStatus

   * @return 不為空值=true

   */

  private static boolean isNotEmpty(String sStatus) {

    return sStatus != null && !"".equals(sStatus);

  }

}

