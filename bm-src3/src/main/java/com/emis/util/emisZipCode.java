/*
 * $Header: /repository/src3/src/com/emis/util/emisZipCode.java,v 1.1.1.1 2005/10/14 12:43:26 andy Exp $
 *
 * Copyright (c) EMIS Corp.
 *
 * 傳入地址以取得郵遞區號
 * 2003/12/30 Jerry: 會員其地址有些因處鄉下，所以地址沒有"路;例如：高雄縣內門鄉華山21號
 *
 */
package com.emis.util;

import com.emis.db.emisDb;
import com.emis.server.emisServerFactory;
import com.emis.qa.emisServletContext;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;

public class emisZipCode {
  // **********************************************************************
  // 取得郵遞區號
  // 例: String _sAddr="台北市信義區信義路四段";
  //     String _sZipCode=emisZipCode.getZipCode(application, _sAddr);
  public static String getZipCode(ServletContext oContext, String sAddr) throws Exception {
    sAddr=sAddr.trim();

    String _sZipCode="";

    // 取出地址之市
    String _sCity="";
    int _iCity=sAddr.indexOf("市");
    _sCity=sAddr.substring(0,_iCity+1);

    // 取出地址之區,鄉,鎮,縣,市
    String _sArea="";
    boolean _bArea=true;
    int _iArea=-1;
    for (int i=1; i<=5; i++) {
      if (i==1)
        _iArea=sAddr.indexOf("區");
      else if (i==2)
        _iArea=sAddr.indexOf("鄉");
      else if (i==3)
        _iArea=sAddr.indexOf("鎮");
      else if (i==4)
        _iArea=sAddr.indexOf("縣");
      else if (i==5)
        _iArea=sAddr.indexOf("市");

      if (_iArea>=0) break;
    }
    if (_iArea==-1) _bArea=false;
    _sArea=sAddr.substring(0,_iArea+1);

    // 取出地址之段,路,街
    String _sRoad="";
    int _iRoad=-1;
    // 當區域名含有"路"字時(如"高雄縣路竹鄉民生路36號", 路竹的路會小於鄉, 此時要由鄉的後面
    //   往後找. 因此indexOf要加上"鄉"的index值
    for (int i=1; i<=3; i++) {
      if (i==1)
        _iRoad=sAddr.indexOf("段", _iArea);
      else if (i==2)
        _iRoad=sAddr.indexOf("路", _iArea);
      else if (i==3)
        _iRoad=sAddr.indexOf("街", _iArea);

      if (_iRoad>=0) break;
    }
    if (_iRoad >= 0) {
      _sRoad=sAddr.substring(_iArea+1, _iRoad+1);
      String _sRoadNew = "";
      if (_sRoad.indexOf("段") > 0) {
        // 將數字的段換成中文, 才能由CITYAREA中找到
        for (int i = 0; i < _sRoad.length(); i++) {
          char ch = _sRoad.charAt(i);
          if (ch >= '0' && ch <= '9') {
            _sRoadNew += getChinaDigit(ch);
          } else {
            _sRoadNew += ch;
          }
        }
        _sRoad = _sRoadNew;
      }
    }

    emisDb _oDB = emisDb.getInstance(oContext);
    try {
      if (_bArea) {
        // Check CityArea 是否有地址之區,鄉,鎮,縣,市
        _oDB.prepareStmt("select * from CityArea where CITYAREA like ? or CITY = ? order by ZIPCODE");
        _oDB.setString(1, "%" + _sArea + "%");
        _oDB.setString(2, _sCity );
        _oDB.prepareQuery();

        while (_oDB.next()) {
          _sZipCode=_oDB.getString("ZIPCODE");
          // 如果第一筆 ZIPCODE = null 則 Check 地址之段,路,街
          if (_sZipCode==null || "".equals(_sZipCode)) {
            while (_oDB.next()) {
              // 先取下一筆資料之 ZIPCODE 之第一碼
              _sZipCode=_oDB.getString("ZIPCODE");
              if (_sZipCode!=null && !"".equals(_sZipCode)) {
                 // 有道路以 ZIPCODE 第一碼並加入"R"", 即 "R"+?+"00A" 取 ZIPCODE
                if (_sRoad!=null && !"".equals(_sRoad)) {
                  _oDB.prepareStmt("select * from R" + _sZipCode.substring(0,1)
                                                     + "00A where ROAD like ?");
                  _oDB.setString(1, "%" + _sRoad + "%");
                  _oDB.prepareQuery();
                  _sZipCode="";
                  if  (_oDB.next())
                    _sZipCode=_oDB.getString("ZIPCODE");
                }
                break;
              }
            }
            break;
          }

          // 判斷符合之 City, CityArea 欄位是否符合
          _sZipCode="";
          if (sAddr.indexOf(_oDB.getString("CITYAREA"))>=0) {
            _sZipCode=_oDB.getString("ZIPCODE");
            break;
          }
        }
      } else {
        // 沒找到地址之區,鄉,鎮,市
        // 取前二個字判斷 CityArea 欄位是否符合地址
        _oDB.prepareStmt("select * from CityArea where CITYAREA like ? order by ZIPCODE");
        _oDB.setString(1, "%" + sAddr.substring(0,1) + "%");
        _oDB.prepareQuery();

        while (_oDB.next()) {
          _sZipCode="";
          if (sAddr.indexOf(_oDB.getString("CITYAREA"))>=0) {
            _sZipCode=_oDB.getString("ZIPCODE");
            break;
          }
        }
      }
    } catch (Exception e) {
      oContext.log("[emisZipCode.getZipCode()] " + e.getMessage());
    } finally {
      _oDB.close();
    }

    return _sZipCode;
  }

  // 有 out 傳入
  public static void getZipCode(ServletContext oContext, JspWriter out,
                                String sAddr) throws Exception {
    out.print(getZipCode(oContext, sAddr));
  }

  private static String getChinaDigit(char c) {
    String _sDigit = null;
    switch (c) {
      case '1': _sDigit = "一"; break;
      case '2': _sDigit = "二"; break;
      case '3': _sDigit = "三"; break;
      case '4': _sDigit = "四"; break;
      case '5': _sDigit = "五"; break;
      case '6': _sDigit = "六"; break;
      case '7': _sDigit = "七"; break;
      case '8': _sDigit = "八"; break;
      case '9': _sDigit = "九"; break;
    }
    return _sDigit;
  }

  public static void main(String[] args) throws Exception {
    emisServletContext application = new emisServletContext();
    emisServerFactory.createServer(application, "c:\\wwwroot\\yes", "c:\\resin\\yes.cfg", true);
    System.out.println("顯示郵遞區號:");
    System.out.println(emisZipCode.getZipCode(application, "台北市信義路4段306號4樓"));
    System.out.println(emisZipCode.getZipCode(application, "台北市大安區信義路4段306號4樓"));
    System.out.println(emisZipCode.getZipCode(application, "高雄縣內門鄉華山21號"));
  }
}
