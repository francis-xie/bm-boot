package com.emis.user;

import com.emis.db.emisDb;

import javax.servlet.ServletContext;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

public class emisErosUserButton {

  /**
   * 取得 FlowStatus Button 控制
   */
  public static String getFlow(ServletContext oContext, String sTaskTitle) throws Exception {
    String _sButton = "";

    emisDb _oDB = emisDb.getInstance(oContext);
    try {
      _oDB.prepareStmt("select * from FlowStatus where FLS_KEYS = ?");
      _oDB.setString(1, sTaskTitle);
      _oDB.prepareQuery();

      boolean _bFirst = true;
      while (_oDB.next()) {
        // 格式: ED;btnConfCO,btnConfCL|PP;btnConfPP...
        // 以 "|" 分筆, 以 ";" 隔開 FLS_NO, FLS_DBUTTON 欄位
        _sButton = _sButton + (_bFirst? "": "|") +
                   _oDB.getString("FLS_NO") + ";" + _oDB.getString("FLS_DBUTTON");
        _bFirst = false;
      }
    } catch (Exception e) {
      oContext.log("[emisUserButton.getFlow()] " + e.getMessage());
    } finally {
      _oDB.close();
    }

    return _sButton==null? "": _sButton;
  }

  /**
   * 取得 write Login User Function Button
   */
  public static void writeFuncButton(emisUser oUser, String sKeys, Writer out) {
    emisPermission _oPerm = oUser.getMenuPermission(sKeys);
    Enumeration e = _oPerm.getAllPermission();
    while (e.hasMoreElements()) {
      // 取出此功能 ArrayList
      ArrayList _oMenuFunc = (ArrayList) e.nextElement();
      for (int i=0; i<_oMenuFunc.size(); i++) {
        // 寫出 Button
        //ArrayList _oFunction = (ArrayList) _oMenuFunc.get(i);
        writeButton((ArrayList) _oMenuFunc.get(i), out);
      }
    }
  }


  /**
   * 取得 write 自定 List Function Button
   */
  public static void writeListButton(ServletContext oContext,
                                     String sList, String sSpace, Writer out) throws Exception {
    emisDb _oDb = emisDb.getInstance(oContext);
    try {
      HashMap _oFunction = new HashMap();

      _oDb.prepareStmt("select * from Functions");
      _oDb.prepareQuery();
      while (_oDb.next()) {
        String _sFuncID = _oDb.getString("FUNC_ID");

        // 將資料存至 _oFunction
        ArrayList _oData = new ArrayList();
        _oData.add("Y");  // 狀態
        _oData.add(_oDb.getString("BTN_NAME"));
        _oData.add(_oDb.getString("BTN_ACCESSKEY"));
        _oData.add(_oDb.getString("BTN_STYLE"));
        _oData.add(_oDb.getString("BTN_TITLE"));
        _oData.add(_oDb.getString("BTN_TEXT"));
        _oData.add(_oDb.getString("BTN_IMGID"));
        _oData.add(_oDb.getString("BTN_IMGFILE"));
        _oFunction.put(_sFuncID, _oData);
      }

      StringTokenizer _stList  = new StringTokenizer(sList, ",");
      StringTokenizer _stSpace = new StringTokenizer(sSpace==null?"":sSpace, ",");
      while (_stList.hasMoreTokens()) {
        String _sFuncID = _stList.nextToken().trim();
        String _sSpace = "";
        if (_stSpace.hasMoreTokens())
          _sSpace  = _stSpace.nextToken().trim();

        if (_oFunction.get(_sFuncID)!=null) {
          ArrayList _oFunc = (ArrayList) _oFunction.get(_sFuncID);
          _oFunc.add(_sSpace);
          writeButton(_oFunc, out);
        }
      }
    } finally {
      _oDb.close();
    }
  }


  /**
   * 產生 Functions Button
   */
  static private void writeButton(ArrayList oButton, Writer out) {
    String _sStatus       = (String) oButton.get(0);
    String _sBtnName      = (String) oButton.get(1);
    String _sBtnAccessKey = (String) oButton.get(2);
    String _sBtnStyle     = (String) oButton.get(3);
    String _sBtnTitle     = (String) oButton.get(4);
    String _sBtnText      = (String) oButton.get(5);
    String _sBtnImgId     = (String) oButton.get(6);
    String _sBtnImgFile   = (String) oButton.get(7);
    String _sFuncSpace    = (String) oButton.get(8);

    // _sStatus = "Y" 才處理此 Button
    if (_sStatus!=null && !"".equals(_sStatus)) {
      StringBuffer _oBuf = new StringBuffer();
      _oBuf.append("<button ");
      if (_sBtnName != null)
        _oBuf.append(" name='").append(_sBtnName).append("'");
      if (_sBtnTitle != null)
        _oBuf.append(" title='").append(_sBtnTitle).append("'");
      if (_sBtnAccessKey != null && !"".equals(_sBtnAccessKey))
        _oBuf.append(" accesskey='").append(_sBtnAccessKey).append("'");
      if (_sBtnStyle != null && !"".equals(_sBtnStyle))
        _oBuf.append(" style='").append(_sBtnStyle).append("'");
      _oBuf.append(">");

      if (_sBtnText != null)
        _oBuf.append(_sBtnText);

      if (_sBtnAccessKey != null && !"".equals(_sBtnAccessKey))
          _oBuf.append("(<u>").append(_sBtnAccessKey).append("</u>)");

      if ((_sBtnImgId != null) && (_sBtnImgFile != null)) {
        _oBuf.append("<img id='")
            .append(_sBtnImgId).append("' src='")
            .append(_sBtnImgFile).append("'>");
      }

      _oBuf.append("</button>");

      if (_sFuncSpace != null && !"".equals(_sFuncSpace)) {
        for (int i=0; i<Integer.parseInt(_sFuncSpace); i++)
          _oBuf.append("&nbsp;");
      }

      _oBuf.append("\n");

      // 寫入 out
      try {
        out.write(_oBuf.toString());
      } catch (Exception e) {}
    }
  }

}
