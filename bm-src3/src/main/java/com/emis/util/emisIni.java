/*
 * $Header: /repository/src3/src/com/emis/util/emisIni.java,v 1.1.1.1 2005/10/14 12:43:20 andy Exp $
 *
 * Copyright (c) EMIS Corp.
 *
 * 2004/01.06 Jerry: readInteger: 若有逗點或NT$, 則都刪去.
 */
package com.emis.util;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;

/**
 * 讀取.ini檔案, 比照Delphi的method name.
 */
public class emisIni {
  private boolean isReady_ = false;
  private HashMap oMap_ = null;

  /**
   * 傳入ini檔名路徑(包含目錄名稱), 將檔內各entry以下列格式存入HashMap:
   *   SectionName.EntryName=value
   *
   * @param sIniFile ini檔名路徑
   * @throws Exception
   */
  public emisIni(String sIniFile) throws Exception {
    File _oFile = new File(sIniFile);
    if (!_oFile.exists()) {
      isReady_ = false;
    } else {
      isReady_ = true;
      FileReader _oReader = new FileReader(_oFile);
      LineNumberReader _oLines = new LineNumberReader(_oReader);

      oMap_ = new HashMap(); // 儲存ini內容.
      _readIniFile(_oLines);
      _oLines.close();
    }
  }

  public boolean isReadey() {
    return isReady_;
  }

  public String readString(String sSection, String sName, String sDefaultValue) {
    sSection = sSection.toUpperCase();
    sName = sName.toUpperCase();
    String _sValue = (String) oMap_.get(sSection+"."+sName);
    if (_sValue == null) _sValue = sDefaultValue;

    return _sValue;
  }

  /**
   * 由ini檔案中讀取數值. 若有逗點或NT$, 則都刪去.
   *
   * @param sSection
   * @param sName
   * @param iDefaultValue
   * @return
   */
  public int readInteger(String sSection, String sName, int iDefaultValue) throws Exception {
    String _sValue = readString(sSection, sName, "@");
    int _iValue = 0;
    if ("".equals(_sValue)) _iValue = iDefaultValue;
    else if (!_sValue.equals("@")) {
      try {
        if (_sValue.indexOf(",") > 0)  // 刪去數值字串中的逗點
          _sValue = emisUtil.stringReplace(_sValue, ",","","a");
        if (_sValue.startsWith("NT$")) {
          _sValue = emisUtil.stringReplace(_sValue, "NT$","","a");
        }
        _iValue = Integer.valueOf(_sValue).intValue();
      } catch (NumberFormatException e) {
        throw new Exception("emisIni.readInteger() " + e.getMessage());
      }
    }
    return _iValue;
  }

  /**
   * 傳回整個HashMap供外部程式自行使用, 例如:以map.keySet().iterator()取出Iterator
   * @return HashMap map
   */
  public HashMap getAll() {
    return oMap_;
  }

  /**
   * 由ini檔中讀取字串.
   *
   * @param oReader
   * @throws Exception
   */
  private void _readIniFile(LineNumberReader oReader) throws Exception {
    String _sLine = null, _sSection = "_begin";

    while ((_sLine = oReader.readLine())!=null) {
      if (_sLine.startsWith("[") && _sLine.endsWith("]")) { // Section name
        _sLine = _sLine.substring(1, _sLine.length()-1);
        _sSection = _sLine.toUpperCase();
        if ("RECEIVABLES".equals(_sSection))
          _sSection = _sSection + "";
      } else {
        int _iPos = _sLine.indexOf("=");
        if (_iPos >= 0) {
          String _sName = _sLine.substring(0, _iPos).toUpperCase();
          String _sValue = _sLine.substring(_iPos+1);
          oMap_.put(_sSection+"."+_sName, _sValue); // Section.Name=Value
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    emisIni ini = new emisIni("c:/wwwroot/yes/data/upload/all/endofday/32310110.ini");
    System.out.println("BOOKVALUE1=>" + ini.readString("PAYMENT", "bookvalue", "0"));
    System.out.println("BOOKVALUE2=>" + ini.readInteger("PAYMENT", "bookvalue", 0));
    System.out.println("test3=>" + ini.readInteger("NONOPERATING", "INCOME", 0));
    System.out.println("test4=>" + ini.readInteger("Receivables", "Total", 0));
  }
}
