/* $Id: emisErosMenuPermImpl.java 7079 2017-01-03 07:24:29Z jerrylee.jie $
 *
 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.
 */
package com.emis.user;

import com.emis.db.emisDb;
import com.emis.util.emisChinese;
import com.emis.util.emisLangRes;

import java.sql.SQLException;
import java.util.*;

/**
 * 此 Class 負責管理 Key (1A,1B...) 和 FunctionID 的 Mapping,
 * 還有 Function ID 和 功能權限(ADD,UPD,DEL...) 的 Mapping.
 * @author Robert
 * @version 2002
 * @version 2003/11/21 Jacky
 * @version 2004/08/25 Jerry 1.6: refactor; add comments; enhance performance;資料改成static
 * @version 2004/09/23 Jerry 1.7: 資料改成static會造成權限按鈕後者被前者影響
 */
public class emisErosMenuPermImpl implements emisMenuPermission {
    private HashMap mapButtons_ = new HashMap(); // 存實際 Button Array 資料
    private TreeMap mapSequences_ = new TreeMap(); // 存 Button Array 順序

    /**
     * Constructor. 由emisErosUserImpl 來new.
     * @param oDb
     * @param sUserId
     * @throws Exception
     */
    public emisErosMenuPermImpl(emisDb oDb, String sUserId) throws Exception {
      if (emisLangRes.getUserLang(sUserId) == null) { // 兼容舊版本
        queryDbToMap(oDb);  // 由table讀出資料存入 mapButtons_ & mapSequences_
      } else { // 2010/05/12 Joe 改用新API，依Userid取得語系并撈回Buttons資料
        queryDbToMap(oDb, sUserId);  // 由table讀出資料存入 mapButtons_ & mapSequences_
      }
      queryFuntionList(oDb, sUserId);  // 取出使用者的功能列
    }

    /**
     * 依Userid取得語系并撈回Buttons資料
     * @param oDb
     * @param sUserId
     * @throws SQLException
     */
    private void queryDbToMap(emisDb oDb, String sUserId) throws SQLException {
      if (mapButtons_.size() > 0)  // 已經取過資料了, 不用再query.
        return;
      String languageType = emisLangRes.getUserLang(sUserId);
      String _sKeys = "", _sFuncSeq = "", _sFuncID = "", _sFuncList = "";

      // 將 Functions Table 寫至 HashTable _oFiuncton
      /*oDb.prepareStmt("select m.*,f.* " +
          " from MenuFuncs m left join Functions f on m.FUNC_ID = f.FUNC_ID" +
          " order by m.KEYS, m.FUNC_SEQ");*/

      // 2010/02/22 Joe.yao add :  增加兩欄用於比對組JS數據--ToolBar.btnMap
      oDb.prepareStmt("select m.KEYS,m.FUNC_ID,m.FUNC_SEQ,m.FUNC_DISPLAY," +
          " m.FUNC_SPACE,f.FUNC_NAME,f.BTN_NAME,f.BTN_ACCESSKEY,f.BTN_STYLE," +
          " f.BTN_TITLE,f.BTN_TEXT,f.BTN_IMGID,f.BTN_IMGFILE,fn.FUNC_NAME BTN_TITLE2" +
          " from MenuFuncs m" +
          " left join Functions f on m.FUNC_ID = f.FUNC_ID and f.NATIVE=?" +
          " left join FUNC_NAME fn on m.FUNC_ID = fn.FUNC_ID and m.KEYS=fn.KEYS and fn.NATIVE=?" +
          " where m.FUNC_DISPLAY='Y'" +      // #36780 [标准版-venus]ZC作业针对按钮是否显示设定未生效
          " order by m.KEYS, m.FUNC_SEQ");
      oDb.setString(1, languageType);
      oDb.setString(2, languageType);
      oDb.prepareQuery();
      ArrayList _oData = null;
      while (oDb.next()) {
        _sKeys = oDb.getString("KEYS");
        _sFuncSeq = oDb.getString("FUNC_SEQ");
        _sFuncID = oDb.getString("FUNC_ID");

        // 將資料存至 mapButtons_
        _oData = new ArrayList();
        _oData.add("");  // 狀態
        _oData.add(oDb.getString("BTN_NAME"));
        _oData.add(oDb.getString("BTN_ACCESSKEY"));
        _oData.add(oDb.getString("BTN_STYLE"));
        _oData.add(oDb.getString("BTN_TITLE"));
        _oData.add(oDb.getString("BTN_TEXT"));
        _oData.add(oDb.getString("BTN_IMGID"));
        _oData.add(oDb.getString("BTN_IMGFILE"));
        _oData.add(oDb.getString("FUNC_SPACE"));

        // 2010/02/22 Joe.yao add :  增加兩欄用於比對組JS數據--ToolBar.btnMap
        _oData.add(oDb.getString("FUNC_ID"));
        _oData.add(oDb.getString("BTN_TITLE2"));

        mapButtons_.put(_sKeys + "," + _sFuncID, _oData);

        // 將順序存至 mapSequences_
        mapSequences_.put(_sKeys + emisChinese.lpad(_sFuncSeq, "0", 2)
            , _sKeys + "," + _sFuncID);
      }
    }
    private void queryDbToMap(emisDb oDb) throws SQLException {
        if (mapButtons_.size() > 0)  // 已經取過資料了, 不用再query.
            return;

        String _sKeys = "", _sFuncSeq = "", _sFuncID = "", _sFuncList = "";

        // 將 Functions Table 寫至 HashTable _oFiuncton
        /*oDb.prepareStmt("select m.*,f.* " +
                " from MenuFuncs m left join Functions f on m.FUNC_ID = f.FUNC_ID" +
                " order by m.KEYS, m.FUNC_SEQ");*/

        // 2010/02/22 Joe.yao add :  增加兩欄用於比對組JS數據--ToolBar.btnMap
       oDb.prepareStmt( "select m.KEYS,m.FUNC_ID,m.FUNC_SEQ,m.FUNC_DISPLAY," +
                " m.FUNC_SPACE,f.FUNC_NAME,f.BTN_NAME,f.BTN_ACCESSKEY,f.BTN_STYLE," +
                " f.BTN_TITLE,f.BTN_TEXT,f.BTN_IMGID,f.BTN_IMGFILE,fn.FUNC_NAME BTN_TITLE2" +
                " from MenuFuncs m" +
                " left join Functions f on m.FUNC_ID = f.FUNC_ID" +
                " left join FUNC_NAME fn on m.FUNC_ID = fn.FUNC_ID and m.KEYS=fn.KEYS" +
                " where m.FUNC_DISPLAY='Y'" +    // #36780 [标准版-venus]ZC作业针对按钮是否显示设定未生效
                " order by m.KEYS, m.FUNC_SEQ" );
        oDb.prepareQuery();
        ArrayList _oData = null;
        while (oDb.next()) {
            _sKeys = oDb.getString("KEYS");
            _sFuncSeq = oDb.getString("FUNC_SEQ");
            _sFuncID = oDb.getString("FUNC_ID");

            // 將資料存至 mapButtons_
            _oData = new ArrayList();
            _oData.add("");  // 狀態
            _oData.add(oDb.getString("BTN_NAME"));
            _oData.add(oDb.getString("BTN_ACCESSKEY"));
            _oData.add(oDb.getString("BTN_STYLE"));
            _oData.add(oDb.getString("BTN_TITLE"));
            _oData.add(oDb.getString("BTN_TEXT"));
            _oData.add(oDb.getString("BTN_IMGID"));
            _oData.add(oDb.getString("BTN_IMGFILE"));
            _oData.add(oDb.getString("FUNC_SPACE"));

            // 2010/02/22 Joe.yao add :  增加兩欄用於比對組JS數據--ToolBar.btnMap
            _oData.add(oDb.getString("FUNC_ID"));
            _oData.add(oDb.getString("BTN_TITLE2"));
          
            mapButtons_.put(_sKeys + "," + _sFuncID, _oData);

            // 將順序存至 mapSequences_
            mapSequences_.put(_sKeys + emisChinese.lpad(_sFuncSeq, "0", 2),
                    _sKeys + "," + _sFuncID);
        }
    }

    /**
     * 取出使用者的功能列.
     * @param oDb
     * @param sUserId
     * @throws SQLException
     */
    private void queryFuntionList(emisDb oDb, String sUserId) throws SQLException {
        String _sKeys;
        String _sFuncList;
        String _sFuncID;

        // 找出此 User 有權限的 Keys
        // 一個 Key 可能有兩筆,一筆是和 USERID 合的
        // 一個是 USERGROUPS 合的,但 FUNC_LIST 要 join 在一起
        // 如果 FunctionList 為 空或 null,表示不能使用
        // FunctionList sample: "QRY,ADD,UPD,DEL,RPT,QQRY,DOWNREC,CLOSE,SAVE,CANCEL"
        /*
      oDb.prepareStmt(
             "select distinct u.KEYS,u.FUNC_LIST " +
              "  from Users s, Userrights u " +
            "  where s.USERID=? and (s.USERID=u.USERID or (s.USERGROUPS=u.USERGROUPS"+
            "  and isnull(s.USERGROUPS,'')!='' and isnull(u.USERGROUPS,'')!=''))" +
              "  and u.RIGHTS='Y' and u.FUNC_LIST>''");
              */

        oDb.prepareStmt("select u.KEYS,u.FUNC_LIST   from Users s, Userrights u   where s.USERID=? "+
"and s.USERID=u.USERID and u.RIGHTS='Y' and u.FUNC_LIST>'' "+
"union all "+
"select KEYS,FUNC_LIST  from  Userrights  where   RIGHTS='Y' "+
"and Usergroups=(Select UserGroups from Users Where Userid=? and Isnull(UserGroups,'')<>'') "+
"and KEYS not in (select u.KEYS   from  Userrights u  where u.USERID=? and u.FUNC_LIST>'')");

        oDb.setString(1, sUserId);
        oDb.setString(2, sUserId);
                oDb.setString(3, sUserId);
        oDb.prepareQuery();
        while (oDb.next()) {
            _sKeys = oDb.getString("KEYS");
            _sFuncList = oDb.getString("FUNC_LIST");
            if ((_sFuncList == null) || "".equals(_sFuncList)) continue;

            // 將 FUNC_LIST 分隔取出 FUNC_ID
            StringTokenizer _oFuncList = new StringTokenizer(_sFuncList, ",");
            ArrayList _alFunc = null;
            while (_oFuncList.hasMoreTokens()) {
                _sFuncID = _oFuncList.nextToken();
                if ("".equals(_sFuncID)) continue;

                // 取出 Function List, 並將第 0 個 Data="Y"
                _alFunc = (ArrayList) mapButtons_.get(_sKeys + "," + _sFuncID);
                if (_alFunc != null)
                    _alFunc.set(0, "Y");

                // 再放回 HashMap 中
                mapButtons_.put(_sKeys + "," + _sFuncID, _alFunc);
            }
        }
    }

    /**
     * 傳回允許權限.
     * @param sKey
     * @return
     */
    public emisPermission getPermission(String sKey) {
        ArrayList _alPermissions = new ArrayList();
        Iterator itr = mapSequences_.keySet().iterator();
        StringTokenizer _oTokenizer = null;
        String _sKeyCmp = null;
        String _sKeyFinc = null;
        while (itr.hasNext()) {
            // 取出 mapSequences_ 之 Key 值
            String _sSeqKeys = (String) itr.next();
            String _sKeys = (String) mapSequences_.get(_sSeqKeys); // 取出實際 Key 值
            _oTokenizer = new StringTokenizer(_sKeys, ",");
            _sKeyCmp = "";
            _sKeyFinc = "";
            if (_oTokenizer.hasMoreTokens()) {
                _sKeyCmp = _oTokenizer.nextToken();
            }

            // 若符合 Key 值之 mapButtons_, 加至 _alPermissions
            if (_sKeyCmp.equals(sKey.trim())) {
                _alPermissions.add((ArrayList) mapButtons_.get(_sKeys));
            }
        }
        return new emisErosPermImpl(_alPermissions);
    }

}
