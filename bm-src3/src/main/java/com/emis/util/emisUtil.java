/* $Id: emisUtil.java 26 2015-06-02 01:40:19Z andy.he $
 *
 * Copyright (c) EMIS Corp. All Rights Reserved.
 */
package com.emis.util;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 公用程式.
 * $Id: emisUtil.java 26 2015-06-02 01:40:19Z andy.he $
 * @author Robert
 * @author Jerry
 * @version 2004/07/03: 增加數個method
 * @version 2004/08/10 Jerry: add x-windows-950 convert to UTF-8
 */
public class emisUtil {
    /**
     * 語言
     */
    public static String LANGUAGE = System.getProperty("user.language", "zh");
    /**
     * 國碼
     */
    public static String COUNTRY = System.getProperty("user.region", "TW");
    /**
     * 語系
     */
    public static Locale LOCALE = new Locale(LANGUAGE, COUNTRY);
    /**
     * 檔案編碼
     */
    public static String FILENCODING = System.getProperty("file.encoding", "UTF-8");
    /**
     * 路徑分隔字元
     */
    public static String FILESEPARATOR = System.getProperty("file.separator");
    /**
     * 換行字元
     */
    public static String LINESEPARATOR = System.getProperty("line.separator");

    static {
        //- IDEA 4.5執行時會傳入-Dfile.encoding=x-windows-950, 導致轉碼錯誤
        if ("x-windows-950".equals(FILENCODING)) {
            FILENCODING = "MS950";
        }
    }

    /**
     * private的constructor無法被new
     */
    private emisUtil() {
    }

    /**
     * 搜尋oProps Properties物件的Key值，如果有相符於sPrefix的值
     * 才加在回傳的Properties中
     *
     * @param sPrefix
     * @param oProps
     * @return
     */
    public static Properties subProperties(String sPrefix, Properties oProps) {
        if (oProps == null) return null;
        if (sPrefix == null) return null;

        Enumeration _oE = oProps.keys();

        Properties _oOutProps = new Properties();
        while (_oE.hasMoreElements()) {
            String _sKey = (String) _oE.nextElement();
            int _Idx = -1;
            if ((_Idx = _sKey.indexOf(sPrefix)) != -1) { // 找到了.
                _Idx = _Idx + sPrefix.length();
                String _sNewKey = _sKey.substring(_Idx);
                _oOutProps.put(_sNewKey, oProps.get(_sKey));
            }
        }
        return _oOutProps;
    }

    public static String replaceParam(String sSQL, List oParam) {
        String SQL = sSQL;
        if (oParam == null) return SQL;
        int size = oParam.size();
        if (size == 0) return SQL;

        // 2006/04/12 update by andy start
        int iTopPost = 0;
        boolean bTop = false;   //判斷是否為TOP後面的參數
        SQL = emisUtil.stringReplace(SQL," TOP "," top ","a");
        SQL = emisUtil.stringReplace(SQL," Top "," top ","a");
        // update by andy end

        for (int i = 0; i < size; i++) {
            int idx = SQL.indexOf("?");
            if (idx == -1) // no ?
                break;

            // 2006/04/12 update by andy start
            if( iTopPost >= 0 && iTopPost < idx ) { //SQL中有TOP且TOP位置比當前？的位置小才處理.
              iTopPost = SQL.indexOf(" top ",iTopPost);
              if(iTopPost > 0 && iTopPost < idx
                  && "".equals(SQL.substring(iTopPost +5,idx).trim())){
                bTop = true;
                iTopPost = idx + 1;  //下次搜索從當前？後開始。
              } else {
                bTop = false;
              }
            }
            // update by andy end

            String o = emisUtil.stringReplace(oParam.get(i) + "", "/", "", "a");
            o = emisUtil.stringReplace(o, "?", "", "a");  //add by abel 2005/12/25
            o = emisUtil.stringReplace(o,"''","'","a");
            o = emisUtil.stringReplace(o, "'", "''", "a");
            if (o == null) {
                SQL = SQL.substring(0, idx) + "''" + SQL.substring(idx + 1);
            } else if( bTop ){  // 是TOP的參數，不能加單引號'' 2006/04/12 update by andy
              SQL = SQL.substring(0, idx) + o.toString() + SQL.substring(idx + 1);
            } else {
              SQL = SQL.substring(0, idx) + "'" + o.toString() + "'" + SQL.substring(idx + 1);
            }
        }

        return SQL;
    }
    /*
    public static String replaceParamConvertDate(Object oParam ){
         String sParam = oParam +"";
        if( sParam.substring(0,2).equalsIgnoreCase("20") ||  ){

        }else{
          return sParam;
        }

    } */
    /**
     * 檢查傳入路徑是否以指定的分隔字元結尾, 若不是則附加在其後.<br>
     * Ex:"C:\test\" = checkDirectory("c:\test","\");
     *
     * @param sDirectory
     * @param sSeparator
     * @return
     */
    public static String checkDirectory(String sDirectory, String sSeparator) {
        if (sDirectory != null) {
            if (sDirectory.length() > 0) {
                String _sLast = sDirectory.substring(sDirectory.length() - 1);

                if (!_sLast.equals(sSeparator))
                    sDirectory = sDirectory.concat(sSeparator);
            }
        }
        return sDirectory;
    }

    /**
     * 依據語系傳回目前的java.util.Date時間<BR>
     * Ex:"Tue Aug 28 15:00:00 GMT+08:00 2001"
     *
     * @return
     */
    public static java.util.Date now() {
        return Calendar.getInstance(LOCALE).getTime();
    }

    /**
     * 傳回系統所在的 Calendar 物件,相當於<BR>
     * Calendar.getInstance(<BR>
     * new Locale( System.getProperty("user.region"),<BR>
     * System.getProperty("user.language")));
     *
     * @return
     */
    public static Calendar getLocaleCalendar() {
        return Calendar.getInstance(LOCALE);
    }

    /**
     * 傳回目前的日期:民國年(整數)<br>
     * Ex:2001 -> 90
     *
     * @param oCalendar
     * @return
     */
    public static int getYearN(Calendar oCalendar) {
        return oCalendar.get(Calendar.YEAR) - 1911;
    }

    /**
     * 傳回目前的日期:民國年(字串)<br>
     * Ex:2001 -> "090"
     *
     * @param oCalendar
     * @return
     */
    public static String getYearS(Calendar oCalendar) {
        int _nYear = oCalendar.get(Calendar.YEAR) - 1911;
        if (_nYear < 100) {
            return "0" + String.valueOf(_nYear);
        }
        return String.valueOf(_nYear);
    }

    /**
     * 傳回目前的日期:月份(整數)<br>
     * Ex:Aug -> 8 (int)
     *
     * @param oCalendar
     * @return
     */
    public static int getMonthN(Calendar oCalendar) {
        return oCalendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 傳回目前的日期:月份(字串)<br>
     * Ex:Aug -> "08"
     *
     * @param oCalendar
     * @return
     */
    public static String getMonthS(Calendar oCalendar) {
        int _nMonth = oCalendar.get(Calendar.MONTH) + 1;
        String _sMonth = String.valueOf(_nMonth);
        if (_nMonth < 10) _sMonth = "0".concat(_sMonth);
        return _sMonth;
    }

    /**
     * 傳回目前的日期:日(整數)<br>
     * Ex:8 -> 8 (int)
     *
     * @param oCalendar
     * @return
     */
    public static int getDateN(Calendar oCalendar) {
        return oCalendar.get(Calendar.DATE);
    }

    /**
     * 傳回目前的日期:日(字串)<br>
     * Ex:8 -> "08"
     *
     * @param oCalendar
     * @return
     */
    public static String getDateS(Calendar oCalendar) {
        int _nDate = oCalendar.get(Calendar.DATE);
        String _sDate = String.valueOf(_nDate);
        if (_nDate < 10) _sDate = "0".concat(_sDate);
        return _sDate;
    }

    /**
     * 傳回目前的時間:時<BR>
     * Ex:16:00PM -> 4 (int)
     *
     * @param oCalendar
     * @return
     */
    public static int getHourN(Calendar oCalendar) {
        return oCalendar.get(Calendar.HOUR);
    }

    /**
     * 傳回目前的時間:時<BR>
     * Ex: 8:00AM -> "08"<BR>
     * 16:00PM -> "16"
     *
     * @param oCalendar
     * @return
     */
    public static String getHourS(Calendar oCalendar) {
        int _nHour = oCalendar.get(Calendar.HOUR_OF_DAY);
        String _sHour = String.valueOf(_nHour);
        if (_nHour < 10) _sHour = "0".concat(_sHour);
        return _sHour;
    }

    /**
     * 傳回目前的時間:分<BR>
     * Ex:16:08PM -> 8 (int)
     *
     * @param oCalendar
     * @return
     */
    public static int getMinN(Calendar oCalendar) {
        return oCalendar.get(Calendar.MINUTE);
    }

    /**
     * 傳回目前的時間:分<BR>
     * Ex:16:08PM -> "08"
     *
     * @param oCalendar
     * @return
     */
    public static String getMinS(Calendar oCalendar) {
        int _nMin = oCalendar.get(Calendar.MINUTE);
        String _sMin = String.valueOf(_nMin);
        if (_nMin < 10) _sMin = "0".concat(_sMin);
        return _sMin;
    }

    /**
     * 傳回目前的時間:秒<BR>
     * Ex:16:08:09PM -> 9 (int)
     *
     * @param oCalendar
     * @return
     */
    public static int getSecondN(Calendar oCalendar) {
        return oCalendar.get(Calendar.SECOND);
    }

    /**
     * 傳回目前的時間:秒<BR>
     * Ex:16:08:09PM -> "09"
     *
     * @param oCalendar
     * @return
     */
    public static String getSecondS(Calendar oCalendar) {
        int _nSecond = oCalendar.get(Calendar.SECOND);
        String _sSecond = String.valueOf(_nSecond);
        if (_nSecond < 10) _sSecond = "0".concat(_sSecond);
        return _sSecond;
    }

    /**
     * 以PrintWriter的轉向輸入至檔案中<BR>
     * isAppend = true/false, true表示寫入至檔案的尾端
     *
     * @param sFileName
     * @param isAppend
     * @return
     * @throws Exception
     */
    public static PrintWriter openPrintWriter(String sFileName, boolean isAppend)
            throws Exception {
        FileOutputStream _fOut = new FileOutputStream(sFileName, isAppend);
        BufferedOutputStream _bOut = null;
        PrintWriter _pOut = null;
        try {
            // default size is 512
            _bOut = new BufferedOutputStream(_fOut, 4096);
        } catch (Exception e1) {
            try {
                _fOut.close();
            } catch (Exception ignore1) {
                ;
            }
            e1.fillInStackTrace();
            throw e1;
        }

        try {
            _pOut = new PrintWriter(_bOut, true);
        } catch (Exception e2) {
            try {
                _bOut.close();
            } catch (Exception ignore2) {
            }
            e2.fillInStackTrace();
            throw e2;
        }
        return _pOut;
    }

    /**
     * 取得系統資源Properties<BR>
     * Ex:getResourceBundle("com.emis.server.message.Messages")<BR>
     * -> ResourceBundle.getBundle("com.emis.server.message.Messages", new Locale("zh", "TW"))<BR>
     * -> com.emis.server.message.Messages_zh_TW.properties
     *
     * @param obj
     * @param req
     * @param sBaseName
     * @return
     * @throws Exception
     */
    public static ResourceBundle getResourceBundle(Object obj, javax.servlet.http.HttpServletRequest req,
                                                   String sBaseName) throws Exception {
        return ResourceBundle.getBundle(sBaseName, req.getLocale(),
                obj.getClass().getClassLoader());
    }

    /**
     * @param sBaseName
     * @return
     * @throws Exception
     */
    public static ResourceBundle getResourceBundle(String sBaseName) throws Exception {
        return (ResourceBundle) PropertyResourceBundle.getBundle(sBaseName,
                new Locale(LANGUAGE, COUNTRY));
    }

    /**
     *  傳回時間<BR>
     *  oCalendar.set(0,0,0,nHour,nMin,nSecond)
     */
    /* Never used
    private static java.util.Date getTime(int nHour, int nMin, int nSecond) {
        Calendar oCalendar = Calendar.getInstance(LOCALE);
        oCalendar.set(0, 0, 0, nHour, nMin, nSecond);
        return oCalendar.getTime();
    } */

    /**
     * 轉換日期字串為Date型式<BR>
     * 接受以下型式的時間表示<BR>
     * 0881124  <BR>
     * 088/11/24 <BR>
     * 將會自動省略小時以下的單位
     *
     * @param sTimeStr
     * @return
     */
    public static java.util.Date strToDate(String sTimeStr) {
        if (sTimeStr == null) return null;
        sTimeStr = sTimeStr.trim();
        if (sTimeStr.equals("")) return null;
        Calendar _oCalendar = getLocaleCalendar();
        if (sTimeStr.indexOf('/') != -1)   // found '/'
            return _SlashStrToDate(sTimeStr, _oCalendar);

        return _StrToDate(sTimeStr, _oCalendar);
    }

    /**
     * 轉換日期字串(Ex:"088/23/25") to Date
     *
     * @param sTimeStr
     * @param calendar
     * @return
     */
    private static java.util.Date _SlashStrToDate(String sTimeStr, Calendar calendar) {
        int counter = 2;  // for two '/'
        int lastidx = sTimeStr.length();
        String _tmp = null;
        int[] container = {0, 0, 0};  // 將年,月,日放入container
        for (int idx = sTimeStr.length() - 1; idx >= 0; idx--) {
            if (sTimeStr.charAt(idx) == '/') {
                _tmp = sTimeStr.substring(idx + 1, lastidx);
                lastidx = idx;
                if (counter > 0)
                    container[counter--] = Integer.parseInt(_tmp);
                else
                    return null;
            }
        }
        _tmp = sTimeStr.substring(0, lastidx);
        if (counter >= 0)
            container[counter] = Integer.parseInt(_tmp) + 1911;
        else
            return null;

        // month is base on 1
        // if month over 12 , the year will +1
        //   year       month        date     hour....
        calendar.set(container[0], container[1] - 1, container[2], 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 轉換日期字串(Ex:"0882325") to Date
     *
     * @param sTimeStr
     * @param calendar
     * @return
     */
    private static java.util.Date _StrToDate(String sTimeStr, Calendar calendar) {
        // we assume date and month must be two digits
        int _len = sTimeStr.length();
        if (_len <= 5) return null; // it must be wrong

        calendar.set(Integer.parseInt(sTimeStr.substring(0, _len - 4)) + 1911, //year
                Integer.parseInt(sTimeStr.substring(_len - 4, _len - 2)) - 1,
                //month, month is based on 0
                Integer.parseInt(sTimeStr.substring(_len - 2, _len)), //date
                0, 0, 0);

        return calendar.getTime();
    }

    /**
     * 傳回今天日期(0890331)
     *
     * @return
     */
    public static String todayDate() {
        return formatDateTime("%Y%M%D", emisUtil.now());
    }

    /**
     * 傳回今天日期(089/03/31)<BR>
     * todayDate("/");<BR>
     * 分別字不可為 %
     *
     * @param seperator
     * @return
     */
    public static String todayDate(String seperator) {
        return formatDateTime("%Y" + seperator + "%M" + seperator + "%D", emisUtil.now());
    }

    /**
     * 傳回美式日期.
     *
     * @return
     */
    public static String todayDateAD() {
        return formatDateTime("%y%M%D", emisUtil.now());
    }

    /**
     * 傳回美式日期, 含分隔字元.
     *
     * @param seperator
     * @return 2004/07/03 Jerry added.
     */
    public static String todayDateAD(String seperator) {
        return formatDateTime("%y" + seperator + "%M" + seperator + "%D", emisUtil.now());
    }

    /**
     * 傳回目前時間(13:12)<BR>
     * todayTime(":");<BR>
     * 分別字元不可為 %<BR>
     * 24 小時制
     *
     * @param seperator
     * @return
     */
    public static String todayTime(String seperator) {
        return formatDateTime("%h" + seperator + "%m", emisUtil.now());
    }

    /**
     * 傳回目前時間(1312)<BR>
     * 24 小時制
     *
     * @return
     */
    public static String todayTime() {
        return formatDateTime("%h%m", emisUtil.now());
    }

    /**
     * 傳回目前時間-含秒數(131201)<BR>
     * 24 小時制
     *
     * @return
     */
    public static String todayTimeS() {
        return formatDateTime("%h%m%s", emisUtil.now());
    }

    /**
     * 傳回目前時間-含秒數(131201)<BR>
     * 24 小時制
     *
     * @param needsSeparator 是否需要冒號
     * @return 2004/07/04 Jerry added
     */
    public static String todayTimeS(boolean needsSeparator) {
        if (needsSeparator) {
            return formatDateTime("%h:%m:%s", emisUtil.now());
        } else {
            return formatDateTime("%h%m%s", emisUtil.now());
        }
    }

    /**
     * 將輸入之日期時間格式化輸出<BR>
     *
     * @param sFormat - <BR>
     *                <FONT COLOR=RED>%Y %y %M %D %h %m %s</FONT><BR>
     * @param Time    <br>
     *                注意sFormat參數之大小寫不同 - %Y表民國年，%y表西元年<BR>
     *                Ex:"民國%Y年%M月%D日"<BR>
     *                其中, 民國的年會填滿3位，自動補 "0" <BR>
     *                月日時分秒會填滿2位，自動補 "0" <BR>
     * @param sFormat
     * @param Time
     * @return
     */
    public static String formatDateTime(String sFormat, java.util.Date Time) {
        StringBuffer sBuf = new StringBuffer();
        Calendar _oCalendar = getLocaleCalendar();
        _oCalendar.setTime(Time);
        int _nLen = sFormat.length();

        for (int i = 0; i < _nLen; i++) {
            char c = sFormat.charAt(i);
            if (c == '%') {
                if (i == (_nLen - 1)) {
                    sBuf.append(c);
                    break; // already end of String
                }
                char d = sFormat.charAt(++i);
                if (d == 'Y') {
                    sBuf.append(getYearS(_oCalendar));
                } else if (d == 'y') {
                    sBuf.append(_oCalendar.get(Calendar.YEAR));
                } else if (d == 'M') {
                    // month is 0 based
                    sBuf.append(getMonthS(_oCalendar));
                } else if (d == 'D') {
                    sBuf.append(getDateS(_oCalendar));
                } else if (d == 'h') {
                    sBuf.append(getHourS(_oCalendar));
                } else if (d == 'm') {
                    sBuf.append(getMinS(_oCalendar));
                } else if (d == 's') {
                    sBuf.append(emisChinese.lpad(String.valueOf(
                            _oCalendar.get(Calendar.SECOND)), "0", 2));
                } else if(d=='S'){
                    sBuf.append(emisChinese.lpad(String.valueOf(
                            _oCalendar.get(Calendar.MILLISECOND)), "0", 3));
                } else {
                    // there is no such option , append it all
                    sBuf.append(c);
                    sBuf.append(d);
                }
            } else {
                sBuf.append(c);
            }
        }
        return sBuf.toString();
    }

    /**
     * 轉換字串之內容<BR>
     *
     * @param Source     - 原始字串<BR>
     * @param OldPattern - 原始字串內欲被取代之字串<BR>
     * @param NewPattern - 取代後的新字串<BR>
     * @param Options    - 'ia' = ignore case sensitive and replace all (可僅取某一字元使用)<BR>
     *                   Default is case sensitive and replace only first occurence<BR>
     *                   Ex:<BR>
     *                   String Test="Test AAA BBB CCC";<BR>
     *                   // default<BR>
     *                   Test = oUtil.stringReplace(Test,"Test","Lala","");<BR>
     *                   // ignorecase and replace all occurence<BR>
     *                   Test = oUtil.stringReplace(Test,"Test","Lala","ia");<BR>
     * @param Source
     * @param OldPattern
     * @param NewPattern
     * @param Options
     * @return
     */
    public static String stringReplace(String Source, String OldPattern,
                                       String NewPattern, String Options) {
        if ((OldPattern == null) || (OldPattern.length() == 0))
            return Source;

        if (Options == null)
            Options = "";
        else
            Options = Options.toLowerCase();

        boolean REPLACE_ALL = ((Options.indexOf('a')) != -1) ? true : false;
        boolean REPLACE_ICASE = ((Options.indexOf('i')) != -1) ? true : false;
        String _source = null;
        String _oldpattern = null;
        int oldlen = OldPattern.length();
        int srclen = Source.length();
        if (REPLACE_ICASE) {
            _source = Source.toLowerCase();
            _oldpattern = OldPattern.toLowerCase();
        }
        StringBuffer buf = new StringBuffer();
        int idx = 0;
        while (idx != -1) {
            int pre_idx = idx;
            if (REPLACE_ICASE)
                idx = _source.indexOf(_oldpattern, idx);
            else
                idx = Source.indexOf(OldPattern, idx);

            if (idx != -1) {
                buf.append(Source.substring(pre_idx, idx));
                buf.append(NewPattern);
                idx = idx + oldlen;
            } else {
                if (pre_idx < srclen)
                    buf.append(Source.substring(pre_idx));
            }
            if (!REPLACE_ALL) {
                if (idx != -1) {
                    if (idx < srclen)
                        buf.append(Source.substring(idx));
                }
                break;
            }
        }
        return buf.toString();
    }

    /**
     *  轉換科學數字為字串<BR>
     *  @param sNumber <BR>
     *  @param iEPos ('E' position)<BR>
     *  Ex:把 "9.8E-6" 換成 "0.0000098"
     */
    /* Never used
    private static String expandNumber(String sNumber, int iEPos) throws Exception {
        String _sDigit = sNumber.substring(iEPos + 1);
        sNumber = sNumber.substring(0, iEPos);
        boolean isNeg = false;
        if (sNumber.length() > 0)
            if (sNumber.charAt(0) == '-') {
                sNumber = sNumber.substring(1);
                isNeg = true;
            }
        int _iDigit = Integer.parseInt(_sDigit);
        int _nPos = sNumber.indexOf(".");
        if (_nPos != -1) {
            sNumber = sNumber.substring(0, _nPos) + sNumber.substring(_nPos + 1);
        } else {
            _nPos = sNumber.length();
        }

        if (_iDigit > 0) {
            int _iPadding = _iDigit - (sNumber.length() - _nPos);
            sNumber = sNumber + emisChinese.duplicate("0", _iPadding);
        } else {
            int _iPadding = _iDigit + (_nPos);
            sNumber = "0." + emisChinese.duplicate("0", -_iPadding) + sNumber;
        }

        return (isNeg ? "-" : "") + sNumber;
    } */

    /**
     * 將浮點數字串依指定長度與小數點位數填成等長之字串.<BR>
     * 小數會四捨五入
     *
     * @param sNumber 數字字串<BR>
     * @param iMaxLen <BR>
     * @param iDigit  小數點位數
     * @return
     * @throws Exception
     */
    public static String stringRound(String sNumber, int iMaxLen, int iDigit) throws Exception {
        double d = Double.parseDouble(sNumber);
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(iDigit);
        nf.setMinimumFractionDigits(iDigit);
        nf.setGroupingUsed(false);
        sNumber = nf.format(d);
        return emisChinese.lpad(sNumber, iMaxLen);
    } // stringRound( )

    /**
     * @param fNumber
     * @param iMaxLen
     * @param iDigit
     * @return
     * @throws Exception
     */
    public static String stringRound(float fNumber, int iMaxLen, int iDigit) throws Exception {
        return stringRound(String.valueOf(fNumber), iMaxLen, iDigit);
    }

    /**
     * 取得Server IP
     *
     * @return
     */
    public static String getServerIP() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (java.net.UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
     * 轉換輸入之字串(以','隔開)為Vector型態<BR>
     * Ex:tokenizer("123,456,789") => [123,456,789]
     *
     * @param sStr
     * @return
     */
    public static Vector tokenizer(String sStr) {
        if (sStr == null) return null;

        Vector _oData = new Vector();
        int _nIdx = sStr.indexOf(",");
        while (_nIdx != -1) {
            _oData.add(sStr.substring(0, _nIdx).trim());
            sStr = sStr.substring(_nIdx + 1);
            _nIdx = sStr.indexOf(",");
        }
        if (!"".equals(sStr)) {
            _oData.add(sStr.trim());
        }
        if (_oData.size() > 0) return _oData;
        return null;
    }


    /**
     * This routine will generate a common xml. data format from emisDb<BR>
     * you can generate oCData by<BR>
     * Vector oCData = emisUtil.tokenizer("T1,T2");<BR>
     * where T1,T2 is columname which need CDATA tag<BR>
     * 另外,若 result set 為空的,要產生一筆空資料<BR>
     * 不然會無法用 JavaScript addNew 新加一筆<BR>
     * ,傳回 emisDb 內的筆數<BR>
     *
     * @param oDb         - Table<BR>
     * @param oOutStream  - 寫入之檔案<BR>
     * @param oCData      - CDATA定義之欄位<BR>
     * @param isLimit     - 是否限制筆數<BR>
     * @param limitedRows - 取出資料之筆數
     */
    public static int genXMLData(emisDb oDb, OutputStream oOutStream, Vector oCData,
                                 boolean isLimit, int limitedRows) throws Exception {
        int _nCount = oDb.getColumnCount();   // 存欄位數
        if (_nCount <= 0) return 0;

        int[] _nType = new int[_nCount];  // 存各欄位之型態

        for (int i = 1; i <= _nCount; i++) {
            _nType[i - 1] = oDb.getColumnType(i);
        }

        String[] _sColName = new String[_nCount];  // 存欄位名稱
        for (int i = 1; i <= _nCount; i++) {
            _sColName[i - 1] = oDb.getColumnName(i);
        }

        String _oObject[] = new String[_nCount];  // 存資料

        boolean _isCData[] = null;
        _isCData = new boolean[_nCount];  // true表示oCData內定義之欄位
        for (int i = 0; i < _nCount; i++) _isCData[i] = false;

        if (oCData != null) {
            for (int i = 1; i <= _nCount; i++) {
                if (oCData.contains(_sColName[i - 1])) {
                    _isCData[i - 1] = true;
                } else {
                    _isCData[i - 1] = false;
                }
            }
        }

        /*-----------------------xml header-----------------------*/

        BufferedOutputStream oBufStream = new BufferedOutputStream(oOutStream);
        OutputStreamWriter oDataOut = new OutputStreamWriter(oBufStream);
        int _nSize = 0;
        try {
//            oDataOut.write("<?xml version=\"1.0\" encoding=\"Big5\"?>\n");
            oDataOut.write("<?xml version=\"1.0\" ?>\n");
            oDataOut.write("<DATA>\n");

            if (oDb.next()) {
                emisStrBuf buf = new emisStrBuf(1024);
                emisStrBuf escapeBuf = new emisStrBuf(128);

                do {
                    for (int i = 0; i < _nCount; i++) {
                        _oObject[i] = oDb.getString(i + 1);
                    }
                    writeData(buf, escapeBuf, oDataOut, _sColName, _oObject, _isCData);
                    buf.setZeroLength();
                    _nSize++;
                    // 用來做筆數的限制
                    if (isLimit)
                        if (_nSize >= limitedRows)
                            break;
                } while (oDb.next());
            } else {
                // 寫一筆空資料
                writeEmptyData(oDataOut, _sColName);
            }
            oDataOut.write("</DATA>\n");
        } finally {
            oDataOut.close();
        }
        return _nSize;
    }

    /**
     * 寫入資料至xmlData<BR>
     *
     * @param buf         - buffer[1024]<BR>
     * @param escapeBuf   - buffer[128] 處裡中文用<BR>
     * @param oDataStream - 寫入之檔案<BR>
     * @param sColName    - 欄位名稱<BR>
     * @param oValue      - 資料<BR>
     * @param isCData     - 是否為中文資料
     */
    private static void writeData(emisStrBuf buf, emisStrBuf escapeBuf,
                                  OutputStreamWriter oDataStream,
                                  String[] sColName, String[] oValue, boolean[] isCData) throws Exception {
        int len = sColName.length;

        buf.append("<REC>\n");
        for (int idx = 0; idx < len; idx++) {
            String _sName = sColName[idx];
            boolean _isCData = isCData[idx];

            buf.append("  <");
            buf.append(_sName);
            buf.append(">");

            String _oValue = oValue[idx];  // 上層傳來之 _oObject
            // write empty string will cause table border 不連續
            // &amp; 是 xml 的 &  , &nbsp; 是 html 的
            if (_oValue == null) {
                _oValue = "";
            } else {
                if (_isCData) // 處裡中文問題
                {
                    escapeBuf.escapeXMLEntity(_oValue);
                    buf.append(escapeBuf);
                } else {
                    buf.append(_oValue);
                }
            }
            buf.append("</");
            buf.append(_sName);
            buf.append(">");
        }
        buf.append("</REC>\n");
        // to avoid a instanciate of class String
        oDataStream.write(buf.getArray(), 0, buf.length());

        // make the performance and memory usage more predictable...
    }

    /**
     * 寫入空資料至xmlData(僅有欄名)<BR>
     *
     * @param oOutStream - 寫入之檔案<BR>
     * @param sColName   - 欄位名稱
     */
    private static void writeEmptyData(OutputStreamWriter oOutStream,
                                       String[] sColName) throws Exception {
        int len = sColName.length;
        oOutStream.write("<REC>\n");
        for (int idx = 0; idx < len; idx++) {
            String _sName = sColName[idx];
            oOutStream.write("  <");
            oOutStream.write(_sName);
            oOutStream.write("></");
            oOutStream.write(_sName);
            oOutStream.write(">");
        }
        oOutStream.write("</REC>\n");
    }

    /**
     * 將 sStr 中的 "<" , ">" , "&" ,"'" , """ 等符號<BR>
     * 轉成 "&lt;" ,"&gt;" ,"&amp;" ,"&apos;" , "&quot;" 等<BR>
     * xml 的 escape
     *
     * @param sStr
     * @return
     */
    public static String escapeXMLString(String sStr) {
        StringBuffer buf = new StringBuffer(sStr);
        int i = 0;
        while (i < buf.length()) {
            char c = buf.charAt(i);
            if (c == '&') {
                buf.deleteCharAt(i);
                buf.insert(i, "&amp;");
                i += 5;
                continue;
            } else if (c == '<') {
                buf.deleteCharAt(i);
                buf.insert(i, "&lt;");
                i += 4;
                continue;
            } else if (c == '>') {
                buf.deleteCharAt(i);
                buf.insert(i, "&gt;");
                i += 4;
                continue;
            } else if (c == '\'') {
                buf.deleteCharAt(i);
                buf.insert(i, "&apos;");
                i += 6;
                continue;
            } else if (c == '"') {
                buf.deleteCharAt(i);
                buf.insert(i, "&quot;");
                i += 6;
                continue;
            }
            i++;
        }
        return buf.toString();
    }

    /**
     * 轉換字串為ISO8859_1型態
     *
     * @param str
     * @return
     */
    public static String sysToISO(String str) {
        try {
            if (str != null) {
                str = new String(str.getBytes(FILENCODING), "ISO8859_1");
            }
        } catch (Exception ignore) {
            ;
        }
        return str;
    }

    /**
     * 產生單一檔名
     * Ex:http://127.0.0.1:8080/epos/invoiceXX.jsp => "invoice1"  ('1' 為 IP,XX 為亂數)
     *
     * @param request
     * @return
     */
    public static String getURIPrefix(HttpServletRequest request) {
        String _sURI = request.getRequestURI();
        int idx = -1;
        if (_sURI == null) {
            _sURI = "unknow";
        } else {
            idx = _sURI.lastIndexOf("/");
            if (idx != -1) {
                _sURI = _sURI.substring(idx + 1);
            }
            idx = _sURI.toUpperCase().lastIndexOf(".JSP");
            if (idx != -1) {
                _sURI = _sURI.substring(0, idx);
            }
        }
        /*
        String sRand = String.valueOf(System.currentTimeMillis());
        if ( sRand.length() > 4 )  {
          sRand = sRand.substring(0,4);
        } else {
          while ( sRand.length() < 4 ) {
            sRand = "0" + sRand;
          }
        }
        */
        return _sURI;
    }

    /**
     * 取得輸入URL之參數陣列.
     *
     * @param request
     * @return
     */
    public static Hashtable processRequest(HttpServletRequest request) {
        Enumeration e = request.getParameterNames();
        Hashtable table = (Hashtable) new emisHashtable();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = request.getParameter(key);
            if (value == null) value = "";
//            System.err.println("put "+key+ "="+ value);
            table.put(key, value);
        }
        return table;
    }

    /**
     * 字串補空白<BR>
     *
     * @param sStr       - string<BR>
     * @param nAlign     - emisReport.A_LEFT/A_CENTER/A_RIGHT<BR>
     * @param nTotalSize - padding後之字串長度
     * @return
     */
    public static String padding(String sStr, int nAlign, int nTotalSize) {
        if (sStr == null)
            sStr = "";
        return  emisUTF8StringUtil.padding(sStr, nAlign, nTotalSize) ;
        /*
        int nLen = emisChinese.clen(sStr);
        if (nLen >= nTotalSize) {
            // already exceed , truncate it ?
            return sStr;   // 超長則不處理
        }
        if (nAlign == emisReport.A_LEFT) {
            return emisChinese.rpad(sStr, nTotalSize);
        } else if (nAlign == emisReport.A_CENTER) {
            int _nLeftPadding = ((nTotalSize - nLen) / 2) + nLen;
            if (_nLeftPadding > 0)
                sStr = emisChinese.lpad(sStr, _nLeftPadding);
            sStr = emisChinese.rpad(sStr, nTotalSize);
            return sStr;
        } else {
            return emisChinese.lpad(sStr, nTotalSize);
        }
        */
    }

    /**
     * 傳回今天日期<BR>
     * Ex:090/01/05<BR>
     * PS:功能和todayDate()一樣
     *
     * @param sSeparator
     * @return
     */
    public static String getFullDate(String sSeparator) {
        return formatDateTime("%Y" + sSeparator + "%M" + sSeparator + "%D", emisUtil.now());
    }

    /**
     * 只從ZIP中解開第一個檔
     *
     * @param oZipFile
     * @param oDestDir
     * @param isDeleteAfterUnzip
     * @return
     * @throws Exception
     */
    public static emisFile extractZip(emisFile oZipFile, emisDirectory oDestDir,
                                      boolean isDeleteAfterUnzip) throws Exception {
        int _iBytes;
        long _nRead;
        byte[] buffer = new byte[8192];
        String _sUnzippedFile = "";

        emisFile oOutputFile = null;
        InputStream fin = oZipFile.getInStream();
        try {
            ZipInputStream in = new ZipInputStream(fin);
            BufferedInputStream bin = new BufferedInputStream(in);
            ZipEntry _oEntry;

            while (in.available() == 1) {
                _oEntry = in.getNextEntry();
                if (_oEntry == null) break;
                _sUnzippedFile = _oEntry.getName();
                //      System.out.println("get ZIP:" + _sUnzippedFile);
                int idx = _sUnzippedFile.lastIndexOf("\\");
                if (idx != -1) // 連路徑都壓進去了
                {
                    _sUnzippedFile = _sUnzippedFile.substring(idx + 1);
                }
                idx = _sUnzippedFile.lastIndexOf("/");
                if (idx != -1) // 連路徑都壓進去了
                {
                    _sUnzippedFile = _sUnzippedFile.substring(idx + 1);
                }

                oOutputFile = oDestDir.getFile(_sUnzippedFile);
                OutputStream out = oOutputFile.getOutStream(null);
                try {
                    _nRead = 0;
                    BufferedOutputStream bout = new BufferedOutputStream(out);
                    while ((_iBytes = bin.read(buffer)) != -1) {
                        bout.write(buffer, 0, _iBytes);
                        _nRead += _iBytes;
                    }
                    bout.flush();
                    bout = null;
                } finally {
                    out.close();
                    out = null;
                }
            }
            bin = null;

        } finally {
            fin.close();
            fin = null;
        }
        if (isDeleteAfterUnzip) {
            if (!oZipFile.delete()) {
                throw new Exception("unable to delete file:" + oZipFile.getFullName());
            }
        }
        return oOutputFile;

    }

    /**
     * 從 ZIP 中解開所有的檔
     * 解壓縮的動作試為一個 atom
     * 如果半途有 Error , 會把
     * 解開的檔刪除
     *
     * @param oZipFile
     * @param oDestDir
     * @param isDeleteAfterUnzip
     * @param allowOverwrite
     * @throws Exception
     */
    public static void extractAllZip(emisFile oZipFile, emisDirectory oDestDir,
                                     boolean isDeleteAfterUnzip, boolean allowOverwrite) throws Exception {

    	extractAllZip(oZipFile, oDestDir,isDeleteAfterUnzip, allowOverwrite,true,false);
    }

    /**
     * 從 ZIP 中解開所有的檔
     * 解壓縮的動作試為一個 atom
     * 如果半途有 Error , 會把
     * 解開的檔刪除
     *
     * @param oZipFile           壓縮檔名
     * @param oDestDir           要解開的目的目錄
     * @param isDeleteAfterUnzip 來源檔案是否要刪除?
     * @param allowOverwrite     允許覆寫嗎?
     * @param isCreateDir        在目的目錄建立路徑嗎?
     */
    public static void extractAllZip(emisFile oZipFile, emisDirectory oDestDir,
                                     boolean isDeleteAfterUnzip, boolean allowOverwrite, boolean isCreateDir)
            throws Exception {

    	
    	    extractAllZip(oZipFile, oDestDir,isDeleteAfterUnzip, allowOverwrite,isCreateDir,false);

    }

    /**
     * 從 ZIP 中解開所有的檔
     * 解壓縮的動作試為一個 atom
     * 如果半途有 Error , 會把
     * 解開的檔刪除
     *
     * @param oZipFile           壓縮檔名
     * @param oDestDir           要解開的目的目錄
     * @param isDeleteAfterUnzip 來源檔案是否要刪除?
     * @param allowOverwrite     允許覆寫嗎?
     * @param isCreateDir        在目的目錄建立路徑嗎?
     * @return                   emisFile 的 ArrayList
     * @throws Exception
     */

    public static ArrayList extractAllZip(emisFile oZipFile,
    		                              emisDirectory oDestDir,
                                          boolean isDeleteAfterUnzip,
                                          boolean allowOverwrite,
                                          boolean isCreateDir,
                                          boolean needFilename) throws Exception 
    {
        int _iBytes;
        long _nRead;
        byte[] buffer = new byte[8192];
        String _sUnzippedFile = "";
        ArrayList aExtracted = new ArrayList();

        try {
            InputStream fin = oZipFile.getInStream();
            try {
                ZipInputStream in = new ZipInputStream(fin);
                try {
                    //BufferedInputStream bin = new BufferedInputStream(in);
                	DataInputStream bin = new DataInputStream(in);
                    try {
                        ZipEntry _oEntry = in.getNextEntry();
                        while (_oEntry != null) {
                            if (_oEntry.isDirectory()) {
                                _oEntry = in.getNextEntry();
                                continue;
                            }
                            try {
	                            _sUnzippedFile = _oEntry.getName();

	                            //System.out.println("entry name=" + _sUnzippedFile);
	                            int idx = _sUnzippedFile.lastIndexOf("\\");
	                            if (idx == -1)
	                                idx = _sUnzippedFile.lastIndexOf("/");  //找不到\\, 再找/
	                            String _sDir = null;
	                            if (idx != -1) // 連路徑都壓進去了
	                            {
	                                if (isCreateDir) {
	                                    _sDir = _sUnzippedFile.substring(0, idx);
	                                    oDestDir.subDirectory(_sDir);  // 建立子目錄
	                                }
	                                _sUnzippedFile = _sUnzippedFile.substring(idx + 1);
	                            }
	                            emisFile _outFile = null;
	                            if (isCreateDir && _sDir != null) {
	                                //System.out.println("dir=" + _sDir + ",fn=" + _sUnzippedFile);
	                                _outFile = oDestDir.subDirectory(_sDir).getFile(_sUnzippedFile);
	                            } else
	                                _outFile = oDestDir.getFile(_sUnzippedFile);
	                            if (!allowOverwrite) {
	                                //不准 overwrite
	                                if (_outFile.exists()) {
	                                    throw new Exception("file already exists:" + _outFile.getFullName());
	                                }
	                            }

	                            OutputStream out = _outFile.getOutStream(null);
	                            try {
	                                _nRead = 0;
	                                BufferedOutputStream bout = new BufferedOutputStream(out);
	                                try {
	                                    while ((_iBytes = bin.read(buffer)) != -1) {
	                                        bout.write(buffer, 0, _iBytes);
	                                        _nRead += _iBytes;
	                                    }
	                                    bout.flush();
	                                } finally {
	                                    bout.close();
	                                    bout = null;
	                                }
	                            } finally {
	                                out.close();
	                                out = null;
	                            }
	                            aExtracted.add(_outFile);
                            } finally {
                            	in.closeEntry();
                            }
                            _oEntry = in.getNextEntry();
                        }
                    } finally {
                        bin.close();
                        bin = null;
                    }
                } finally {
                    in.close();
                    in = null;
                }
            } finally {
                fin.close();
                fin = null;
                buffer = null;
            }
            if (isDeleteAfterUnzip) {
                if (!oZipFile.delete()) {
                    throw new IOException("unable to delete file:" + oZipFile.getFullName());
                }
            }
        } catch (Exception decompresserr) {
            // 有 Error 把已開的檔清掉,ignore the delete error and re-throw decompress err
            int _iSize = aExtracted.size();
            for (int i = 0; i < _iSize; i++) {
                emisFile f = (emisFile) aExtracted.get(i);
                f.delete();
            }
            throw decompresserr;
        }
        return aExtracted;
    } // extractAllZip

    /**
     * 將一個目錄下的檔案全部壓縮起來
     * 使用 append mode 開 zip 檔
     *
     * @param oSourceDir
     * @param sSourceFile
     * @param oDestDir
     * @param sDestFile
     * @param isDeleteAfterCompress
     * @throws Exception
     */
    public static void compress(emisDirectory oSourceDir, String sSourceFile,
                                emisDirectory oDestDir, String sDestFile, boolean isDeleteAfterCompress)
            throws Exception {
        String _sSrc;
        String _sDest;
        emisFile destZip = oDestDir.getFile(sDestFile);
        _sDest = destZip.getFullName();
        _sSrc = oSourceDir.getDirectory() + sSourceFile;

        String sMode = destZip.exists() ? "-uvfM" : "-cvfM";
        String[] argvs = {sMode, _sDest, "-C", oSourceDir.getDirectory(), sSourceFile};
        sun.tools.jar.Main jar = new sun.tools.jar.Main(System.out, System.err, "jar");
        System.out.println("Compress: Source=" + _sSrc + ",Dest=" + _sDest);
        jar.run(argvs);  // call jar.exe, 會輸出至console.
        if (isDeleteAfterCompress) {
            emisFile f = oDestDir.getFile(sSourceFile);
            f.delete();
        }

        /*   compress by ZipEntry,ZipOutputStream
          emisFile f=oSourceDir.getFile(sSourceFile);
          emisFile odestZip = oDestDir.getFile(sDestFile);

          if (!f.exists()) throw new Exception(sSourceFile+"doesn't exist!");
          if (!odestZip.exists()) throw new Exception(sDestFile+"doesn't exist!");

          //Enumeration e=;
          OutputStream o = odestZip.getOutStream("a");
          try {
           ZipOutputStream out = new ZipOutputStream(o);
           try {
             int byteRead = 0;
             byte [] buffer = new byte[8192];
             //while(e.hasMoreElements()) {
               //emisFile f = (emisFile) e.nextElement();
               InputStream in = f.getInStream();
               try {
                 ZipEntry z = new ZipEntry( f.getFileName() );
                 out.putNextEntry(z);
                 while((byteRead=in.read(buffer))!=-1) {
                   out.write(buffer,0,byteRead);
                 }
               } finally {
                 in.close();
               }
               //if(isDeleteAfterCompress)
                 //f.delete();
             //}
             out.flush();
             buffer = null;
           } finally {
             out.close();
           }
         } finally {
           o.close();
         } */
    }


/*
 public static void compressAll(emisDirectory oCompressDir,emisFile oZip,boolean isDeleteAfterCompress) throws Exception
 {

   if(oZip.exists()) {
     if( !oZip.delete() ) throw new IOException("can't delete file:"+oZip.getFullName());
   }
   Enumeration e = oCompressDir.getFileList();
   OutputStream o = oZip.getOutStream("a");
   try {
     ZipOutputStream out = new ZipOutputStream(o);
     try {
       int byteRead = 0;
       byte [] buffer = new byte[8192];

       while(e.hasMoreElements()) {
         emisFile f = (emisFile) e.nextElement();
         InputStream in = f.getInStream();
         try {
           ZipEntry z = new ZipEntry( f.getFileName() );
           out.putNextEntry(z);
           while((byteRead=in.read(buffer))!=-1) {
             out.write(buffer,0,byteRead);
           }
         } finally {
           in.close();
         }
         if(isDeleteAfterCompress)
           f.delete();
       }
       out.flush();
       buffer = null;
     } finally {
       out.close();
     }
   } finally {
     o.close();
   }
 }
*/


    /**
     * 將一個目錄下的檔案全部壓縮起來
     *
     * @param oCompressDir
     * @param oZip
     * @param isDeleteAfterCompress
     * @throws Exception
     */
    public static void compressAll(emisDirectory oCompressDir, emisFile oZip,
                                   boolean isDeleteAfterCompress) throws Exception {
        sun.tools.jar.Main jar = new sun.tools.jar.Main(System.out, System.err, "jar");

        ArrayList oCommand = new ArrayList();
        ArrayList oDeleteList = new ArrayList();

        if (oZip.exists()) {
            oCommand.add("-uvfM");
        } else {
            oCommand.add("-cvfM");
        }
        oCommand.add(oZip.getFullName());
        System.out.println("CompressAll: Dest=" + oCompressDir.getDirectory());
        Enumeration e = oCompressDir.getFileList();
        while (e.hasMoreElements()) {
            emisFile f = (emisFile) e.nextElement();
            if (!f.getFileName().equalsIgnoreCase(oZip.getFileName())) {
                oCommand.add("-C");  // each file , must one Directory command
                oCommand.add(oCompressDir.getDirectory());
                oCommand.add(f.getFileName());
                oDeleteList.add(f);
            }
        }
        if (oDeleteList.size() == 0) // nothing to zip
            return;

        jar.run((String[]) oCommand.toArray(new String[]{}));

        if (isDeleteAfterCompress) {
            int _iSize = oDeleteList.size();
            for (int i = 0; i < _iSize; i++) {
                emisFile f = (emisFile) oDeleteList.get(i);
                f.delete();
            }
        }
    }

  //　将文件以压缩流方式输出到response中。
  public static void downloadZip(ArrayList oSourceFile, HttpServletResponse oResponse) throws Exception{
    BufferedInputStream inputStream = null;
    FileOutputStream outputStream = null;
    ZipOutputStream zipStream = null;
    File oFile = null;
    int iBuffer = 8192;
    try{
      zipStream = new ZipOutputStream(oResponse.getOutputStream()) ;
      byte data[] = new byte[iBuffer];
      for(int i = 0; i< oSourceFile.size(); i++){
        oFile = (File) oSourceFile.get(i);
        if(!oFile.exists()) continue;
        inputStream = new BufferedInputStream(new FileInputStream(oFile),iBuffer);
        ZipEntry entry = new ZipEntry(oFile.getName());
        zipStream.putNextEntry(entry);
        int count;
        while((count = inputStream.read(data, 0, iBuffer)) != -1) {
          zipStream.write(data, 0, count);
        }
        zipStream.closeEntry();
        inputStream.close();
        inputStream = null;
      }
      zipStream.close();
      zipStream = null;
    } catch(Exception e) {
      throw e;
    } finally{
      if(zipStream != null) {
        zipStream.close(); zipStream = null;
      }
      if(inputStream != null){
        inputStream.close(); inputStream = null;
      }
      if(outputStream != null){
        outputStream.close(); outputStream = null;
      }
    }
  }

  /**
   * 单个文件下载（以字节流的方式写到response中）。
   * @param response
   * @param oFile
   * @param bDelete  下载后是否删除源文件。
   * @throws Exception
   */
  public static void downloadFile(File oFile, HttpServletResponse response, boolean bDelete) throws Exception{
    BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
    FileInputStream is = null;
    try {
      is = new FileInputStream(oFile);
      byte[] buf = new byte[8192];
      int readed;
      BufferedInputStream bis = new BufferedInputStream(is);
      while ((readed = bis.read(buf)) != -1) {
        bos.write(buf, 0, readed);
      }
      bos.flush();
      is.close();
      is = null;
      if(bDelete)  oFile.delete();
    } finally {
      if(is!=null){
        is.close();
        is = null;
      }
    }
  }

  public static void downloadFile(File oFile,HttpServletResponse response) throws Exception{
    downloadFile(oFile,response,false);
  }
    /**
     * Execute float a/b，if b==0 then return fDefault
     *
     * @param a
     * @param b
     * @param fDefault
     * @return
     */
    public static float divide(float a, float b, float fDefault) {
        if (b == 0) return fDefault;
        return a / b;
    }

    /**
     * Execute double a/b，if b==0 then return dDefault
     *
     * @param a
     * @param b
     * @param dDefault
     * @return
     */
    public static double divide(double a, double b, double dDefault) {
        if (b == 0) return dDefault;
        return a / b;
    }

    /**
     * Execute int a/b，if b==0 then return nDefault
     *
     * @param a
     * @param b
     * @param nDefault
     * @return
     */
    public static int divide(int a, int b, int nDefault) {
        try {
            return a / b;
        } catch (Exception ignore) {
            ;
        }
        return nDefault;
    }

    /**
     * Execute long a/b，if b==0 then return nDefault
     *
     * @param a
     * @param b
     * @param nDefault
     * @return
     */
    public static long divide(long a, long b, long nDefault) {
        try {
            return a / b;
        } catch (Exception ignore) {
            ;
        }
        return nDefault;
    }

    /**
     * 隨機取出一個INT變數(可為負值)
     */
    private static Random oRand = new Random(System.currentTimeMillis());

    /**
     * @return
     */
    public static int random() {
        return oRand.nextInt();
    }

    /**
     * @param e
     * @return
     */
    public static String getStackTrace(Exception e) {
        if (e == null) return "";
        PrintWriter pw = null;
        try {
            StringWriter sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        } catch (Exception ignore) {
            ;
        } finally {
            if (pw != null) pw.close();
        }
        return e.getMessage();
    }

    /**
     * 傳回 EAN 的檢查碼，目前只實作 EAN13 碼<BR>
     * PS:參數二"sType"沒使用到
     *
     * @param sPNo
     * @param sType
     * @return
     */
    public static String getEANCheckBit(String sPNo, String sType) {
        sPNo = emisChinese.lpad(sPNo, "0", 12);
        char[] list = sPNo.toCharArray();
        int _iChk = 0;
        for (int _iCnt = 0; _iCnt <= 10; _iCnt += 2) {
            char c = list[_iCnt];
            _iChk = _iChk + (c - '0');
            c = list[_iCnt + 1];
            _iChk = _iChk + ((c - '0') * 3);
        }
        _iChk %= 10;
        return ((_iChk == 0) ? "0" : String.valueOf(10 - _iChk));
    }

    /**
     * 系統公用加密 Tool function<BR>
     * 不可修改...
     *
     * @param s
     * @return
     * @throws Exception
     */
    public static String digest(String s) throws Exception {
        if (s == null) throw new Exception("can't digest null String");
        MessageDigest m = MessageDigest.getInstance("sha");  // MD5 or SHA algorithm
        byte[] d = m.digest(s.getBytes());
        StringBuffer b = new StringBuffer();
        int bLen = d.length;
        for (int i = 0; i < bLen; i++) {
            int j = (int) d[i];
            if (j < 0) j = -j;
            b.append(Integer.toHexString(j));
        }
        return b.toString().toUpperCase();
    }


    /**
     * byteToStr 將 byte array 轉換成 unsigned byte 的字串 (0~255)
     * 原本 Java 是 -127 ~ +128
     *
     * @param b
     * @return
     * @throws Exception
     */
    public static String byteToStr(byte[] b) throws Exception {
        if (b == null) return null;
        int len = b.length;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int bValue = (int) b[i];
            if (bValue < 0) bValue = bValue + 256;
            if (bValue < 16) // 少了一位
                buf.append("0");
            buf.append(Integer.toHexString(bValue).toUpperCase());
        }
        return buf.toString();
    }

    /**
     * 將 byteToStr 的字串轉回 byte array
     *
     * @param sStr
     * @return
     * @throws Exception
     */
    public static byte[] strToByte(String sStr) throws Exception {
        if (sStr == null) return null;
        int len = sStr.length();
        if ((len % 2) != 0)
            throw new Exception("Error Length in Input String");
        byte[] b = new byte[len / 2];
        int j = 0;
        for (int i = 0; i < len; i += 2) {
            String sValue = sStr.substring(i, i + 2);
            int nValue = Integer.parseInt(sValue, 16);
            if (nValue > 128)
                nValue = nValue - 256;
            b[j++] = (byte) nValue;

        }
        return b;
    }

    /**
     * 將 byte array 轉成 hex String, 如 byte [] b = { 0xff, 0x1c };
     * 則回傳字串 = "ff1c";.
     *
     * @param b
     * @return
     */
    public static String EncodeByteToStr(byte[] b) {
        // a byte is a value between -128 and 127
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            int bValue = (int) b[i];
            if (bValue < 0)
                bValue = (-bValue) + 127;
            if (bValue < 16)
                sb.append("0").append(Integer.toHexString(bValue));
            else
                sb.append(Integer.toHexString(bValue));
        }
        return sb.toString();
    }

    /**
     * 將 byte array 轉成 hex String
     * 如 byte [] b = { 0xff, 0x1c };
     * 則回傳字串 = "ff1c";
     *
     * @param sStr
     * @return
     */
    public static byte[] DecodeStrToByte(String sStr) {
        int len = sStr.length();
        byte b [] = new byte[len / 2];
        int j = 0;
        for (int i = 0; (i + 1) < len; i += 2) {
            String sByte = sStr.substring(i, i + 2);
            int nByte = Integer.parseInt(sByte, 16);
            if (nByte > 127)
                nByte = (-nByte) + 127;
            b[j++] = (byte) nByte;
        }
        return b;
    }

    /**
     * 字串數值解析成數值, 預設值內定為0.
     *
     * @param sValue
     * @return
     */
    public static int parseInt(String sValue) {
        return parseInt(sValue, 0);
    }

    /**
     * 字串數值解析成數值.
     *
     * @param sValue
     * @param iDefault
     * @return
     */
    public static int parseInt(String sValue, int iDefault) {
        if (sValue == null) return iDefault;
        int _iValue = iDefault;
        try {
            _iValue = Integer.parseInt(sValue);
        } catch (NumberFormatException e) {
            ;
        }
        return _iValue;
    }

    /**
     * 字串數值解析成數值, 預設值內定為0.
     *
     * @param sValue
     * @return
     */
    public static long parseLong(String sValue) {
        return parseLong(sValue, 0L);
    }

    /**
     * 字串數值解析成數值.
     *
     * @param sValue
     * @param lDefault
     * @return
     */
    public static long parseLong(String sValue, long lDefault) {
        if (sValue == null) return lDefault;
        long _lValue = lDefault;
        try {
            _lValue = Long.parseLong(sValue);
        } catch (NumberFormatException e) {
            ;
        }
        return _lValue;
    }

    /**
     * 字串數值解析成數值, 預設值內定為0.
     *
     * @param sValue
     * @return
     */
    public static float parseFloat(String sValue) {
        return parseFloat(sValue, 0);
    }

    /**
     * 字串數值解析成數值.
     *
     * @param sValue
     * @param fDefault
     * @return
     */
    public static float parseFloat(String sValue, float fDefault) {
        if (sValue == null) return fDefault;
        float _fValue = fDefault;
        try {
            _fValue = Float.parseFloat(sValue);
        } catch (NumberFormatException e) {
            ;
        }
        return _fValue;
    }

    /**
     * 字串數值解析成數值, 預設值內定為0.
     *
     * @param sValue
     * @return
     */
    public static double parseDouble(String sValue) {
        return parseDouble(sValue, 0.0);
    }

    /**
     * 字串數值解析成數值.
     *
     * @param sValue
     * @param dDefault
     * @return
     */
    public static double parseDouble(String sValue, double dDefault) {
        if (sValue == null) return dDefault;
        double _dValue = dDefault;
        try {
            _dValue = Double.parseDouble(sValue);
        } catch (NumberFormatException e) {
            ;
        }
        return _dValue;
    }

  /**
   * 把Object转换成String，如Object==null 返回""
   *
   * @param obj
   * @return
   */
  public static String parseString(Object obj) {
    return parseString(obj, ""); //$NON-NLS-1$
  }

  /**
   * 把Object转换成String，如Object==null 返回默认值
   *
   * @param obj
   * @param defaultValue
   * @return
   */
  public static String parseString(Object obj, String defaultValue) {
    if (obj == null)
      return defaultValue;
    else
      return String.valueOf(obj);
  }

    /**
     * 由cfg檔案中取出Document root. 預設為 c:/wwwroot/eros
     *
     * @param oContext
     * @return
     */
    public static String getDocumentRoot(ServletContext oContext) {
        String _sRoot = "c:/wwwroot/eros";
        Properties _oProps = (Properties) oContext.getAttribute("CFG_PROPS");
        if (_oProps == null) {
            System.out.println("emisUtil.getDocumentRoot: No CFG properties in Context.");
            return _sRoot;
        }
        try {
            _sRoot = _oProps.getProperty("documentroot");
            _sRoot = emisUtil.stringReplace(_sRoot, "\\", "/", "a");
        } catch (Exception e) {
            System.err.println("emisUtil.getDocumentRoot: " + e.getMessage());
        }
        return _sRoot;
    }
    //select * from weekno where firstday <='20031230' order by firstday desc
     public static String getWeekInfo(ServletContext oContext, String sDate){
         String _sRet = ",,,"; // 第一天    該週    該週起  該週迄
        emisDb wtnDb = null;
        try {
            wtnDb = emisDb.getInstance(oContext);
            wtnDb.prepareStmt(" select  *  from weekno where firstday <= ? order by firstday desc");
            wtnDb.setString(1, sDate);
            wtnDb.prepareQuery();
            if (wtnDb.next()) {
                String FirstDay = wtnDb.getString("FIRSTDAY").trim();
                int temp = new emisDate(FirstDay).getDiff(new emisDate(sDate));
                //System.out.println(temp);
                int week = 1;
                week = (temp /7)+1 ;
                String weekFirstDay = new emisDate(sDate).add((temp % 7) * -1).toString();
                String weekLastDay  = new emisDate(sDate).add(6 - (temp % 7)).toString();

                _sRet = FirstDay + "," + week + "," + weekFirstDay + "," + weekLastDay;
            }
        } catch (Exception e) {
            ;
        } finally {
            if (wtnDb != null) wtnDb.close();
        }
        return _sRet;
    }

    public static String getWeekInfo(ServletContext oContext) {
        String date = new emisDate().toString();
        return emisUtil.getWeekInfo(oContext, date.substring(0,4),date);
    }

    public static String getWeekInfo(ServletContext oContext, String sYear, String sDate) {
        String _sRet = ",,,"; // 第一天    該週    該週起  該週迄
        String sQuery = "select ";
        emisDb wtnDb = null;
        try {
            wtnDb = emisDb.getInstance(oContext);
            wtnDb.prepareStmt(" select  * from weekno where year=?");
            wtnDb.setString(1, sYear);
            wtnDb.prepareQuery();
            if (wtnDb.next()) {
                String FirstDay = wtnDb.getString("FIRSTDAY").trim();
                int temp = new emisDate(FirstDay).getDiff(new emisDate(sDate));
                System.out.println(temp);
                int week = 1;
                week = (temp /7)+1 ;
                /*
                if (sDate.equalsIgnoreCase(FirstDay)) {
                    ;
                } else {

                    if (temp % 7 > 0) {
                        week = temp / 7 +1 ;
                    } else {
                        week = temp / 7;

                    }
                }
                  */
                String weekFirstDay = new emisDate(sDate).add((temp % 7) * -1).toString();
                String weekLastDay = new emisDate(sDate).add(6 - (temp % 7)).toString();

                _sRet = FirstDay + "," + week + "," + weekFirstDay + "," + weekLastDay;
            }
        } catch (Exception e) {
            ;
        } finally {
            if (wtnDb != null) wtnDb.close();
        }
        return _sRet;
    }

    /**
     * 抓取輸入date 的第一天  每週的第一天為星期一
     * @param Fromat yyyyMMdd
     * @param sDate  2004/01/01  需要有分隔字串
     * @return  依照format 回傳
     * @throws Exception
     */
    public static String getFirstDayOfWeek(String Fromat, String sDate) throws Exception {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat(Fromat);
        calendar.setTime(DateFormat.getDateInstance().parse(sDate));
        int currentDiff = calendar.get(Calendar.DAY_OF_WEEK);
        if (currentDiff == 1) {
            currentDiff = -6;
        } else {
            currentDiff = (currentDiff - 2) * -1;
        }
        calendar.add(Calendar.DATE, currentDiff);

        return formatter.format(calendar.getTime());
    }

    /**
     * 回傳輸入該週有的星期一到星期日的日期
     * @param sDate 2004/01/01
     * @return
     * @throws Exception
     */
    public static String[] getDayOfWeekArray(String sDate) throws Exception {

        String ret [] = new String[7];
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        calendar.setTime(DateFormat.getDateInstance().parse(getFirstDayOfWeek("yyyy/MM/dd", sDate)));
        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DATE, i);
            ret[i] = formatter.format(calendar.getTime());
            calendar.add(Calendar.DATE, i * -1);
        }
        return ret;
    }

    public static boolean copyTo( String sDest , String sSource ) throws Exception {
    	FileInputStream fin = new FileInputStream( sSource );
    	try {
    		FileOutputStream fout = new FileOutputStream( sDest ,false );
    		try {
    			fout.getChannel().tryLock();
    			byte buf [] = new byte[4096];
    			int readed;
    			while( (readed = fin.read(buf)) != -1 ) {
    				fout.write(buf, 0 , readed);
    			}
    		} finally {
    			fout.close();
    		}
    	} finally {
    		fin.close();
    	}
    	return true;
    }

    public static String getDayOfWeekString(String sDate) throws Exception {

        String ret = "";
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        calendar.setTime(DateFormat.getDateInstance().parse(getFirstDayOfWeek("yyyy/MM/dd", sDate)));
        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DATE, i);
            ret += formatter.format(calendar.getTime()) + ",";
            calendar.add(Calendar.DATE, i * -1);
        }
        return ret.substring(0, ret.length() - 1);
    }

  public static File copyFile(File SourceFile,String sTargetPath) throws Exception {
    File oFile = new File(sTargetPath);
    if(!oFile.exists()) oFile.mkdirs();

    oFile = new File(sTargetPath,SourceFile.getName());
    FileOutputStream os = new FileOutputStream(oFile);
    try {
      BufferedOutputStream bos = new BufferedOutputStream(os);
      FileInputStream is = new FileInputStream(SourceFile);
      try {
        byte[] buf = new byte[8192];
        int readed;
        BufferedInputStream bis = new BufferedInputStream(is);
        while ((readed = bis.read(buf)) != -1) {
          bos.write(buf, 0, readed);
        }
        bos.flush();
      } finally {
        is.close();
        is = null;
      }
    } finally {
      os.close();
      os = null;
    }
    return oFile;
  }

  //dana.gao 2011/06/24 修改取taskName的方法,避免前臺ccr直接調用時出錯
  public static String getTaskName(HttpSession session, String sKey_, String defval) {
    HashMap map = (HashMap) session.getAttribute("MENUS_MSG");
    if (map == null) return defval;
    else
      return (String) map.get(sKey_);
  }

  public static String getTaskName(HttpSession session, String sKey_) {
    return getTaskName(session, sKey_, "");
  }


  /*
  * 判斷使用者的所使用的 Server IP 和 Server local IP, 來決定是否使用 內/外部 port / Ip
  * Robert 2011/12/08 , 整理 jsp code 進來這邊
  */
  public static String getAutoServerURL(ServletContext oContext , HttpServletRequest request) throws Exception {
    return emisUtil.formCompleteURL(oContext, request);
    /*

     String sSeverIP_ = request.getServerName();

     emisProp oProp_ = emisProp.getInstance(oContext);

     //獲取服務器IP
     String ipv4 = null;
     Enumeration<NetworkInterface> netIf = NetworkInterface.getNetworkInterfaces();
     while (netIf.hasMoreElements()) {
       NetworkInterface nwi = netIf.nextElement();
       Enumeration address = nwi.getInetAddresses();
       while (address.hasMoreElements()) {
         InetAddress in = (InetAddress) address.nextElement();
         if (in.isSiteLocalAddress()) {
           ipv4 = in.getHostAddress();
           break;
         }
       }
     }
     String sServName_ = "";

     if (sSeverIP_.equals(ipv4)) {
       sServName_ = oProp_.get("EPOS_SERVER_NAME");
     } else {
       sServName_ = oProp_.get("EPOS_SERVER_NAME_OUT");
       if ("".equals(sServName_))
         sServName_ = oProp_.get("EPOS_SERVER_NAME");
     }
     if (sServName_.equals(""))
       sServName_ = (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + ":" + request.getServerPort();;

     return sServName_;
    	  */

  }

  /*
  *  Robert 2012/05/20
  *  拿取 http://xxxx:8888
  *  這邊因為雲端應用,我們寫了轉址的程式,把 SSL 443 轉到 80xx ,所以配合
  *  雲端,這邊要統一修改
  */
  public static String formCompleteURL(ServletContext application, HttpServletRequest request) throws Exception  {

    String sSeverIP_ = request.getServerName();

    emisProp oProp_ = emisProp.getInstance(application);

    //獲取服務器IP
    String ipv4 = null;
    Enumeration<NetworkInterface> netIf = NetworkInterface.getNetworkInterfaces();
    while (netIf.hasMoreElements()) {
      NetworkInterface nwi = netIf.nextElement();
      Enumeration address = nwi.getInetAddresses();
      while (address.hasMoreElements()) {
        InetAddress in = (InetAddress) address.nextElement();
        if (in.isSiteLocalAddress()) {
          ipv4 = in.getHostAddress();
          break;
        }
      }
    }
    String sServName_ = "";

    if (sSeverIP_.equals(ipv4)) {
      sServName_ = oProp_.get("EPOS_SERVER_NAME");
    } else {
      sServName_ = oProp_.get("EPOS_SERVER_NAME_OUT");
      if ("".equals(sServName_))
        sServName_ = oProp_.get("EPOS_SERVER_NAME");
    }
    if (sServName_.equals(""))
      sServName_ = (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + ":" + request.getServerPort();;

    return sServName_;

    /*
    //String _sServerAddress = emisShowData.getServerAddress(application, request);
    //String _sServerAddress = getServerIP();

    String sURL;
    String fwdHostPort = request.getHeader("EMISFWD");

    if (fwdHostPort == null) { // 不是雲端轉址來的
      sURL = (request.isSecure() ? "https" : "http") + "://" + emisShowData.getServerAddress(application, request) + ":" + request.getServerPort();
    } else {
      String[] sfwd = fwdHostPort.trim().split(",");
      int port = Integer.parseInt(sfwd[1]);
      if (port == 443) {
        sURL = "https://" + sfwd[0];
      } else {
        if (port != 80) {
          sURL = "http://" + sfwd[0] + ":" + port;
        } else {
          sURL = "http://" + sfwd[0];
        }
      }
    }

    return sURL;
    */
  }


  /**
   * 多AP上传时获取档案储存的根路径
   * @param oContext
   * @return
   * @throws Exception
   */
  public static String getMainAPServerRootPath(ServletContext oContext) throws Exception {
    emisProp oProp_ = emisProp.getInstance(oContext);

    if ("Y".equalsIgnoreCase(oProp_.get("EPOS_SERVER_IS_AP"))) {
      return "//" + oProp_.get("EPOS_SERVER_LAN") + "/" ;
    } else {
      emisDirectory oRootDir_ = emisFileMgr.getInstance(oContext).getDirectory("root");
      return oRootDir_.getDirectory() ;
    }
  }

  /**
   * 验证是否JSON格式数据
   */
  public static boolean isJSON(String sContentnt) {
    if (sContentnt == null || "".equals(sContentnt)
      || !sContentnt.startsWith("{")) {
      return false;
    }
    return true;
  }

  /**
   * 将String转换为JSON格式
   *
   * @return 内容为空或格式不正确返回null
   */
  public static JSONObject parseJSON(String sContentnt) {
    if (!emisUtil.isJSON(sContentnt)) {
      return null;
    }
    JSONObject _oJSON = null;
    if (!"".equals(sContentnt)) {
      try {
        sContentnt = StringUtils.replace(sContentnt, ":\"null\"",
          ":\"\"");
        sContentnt = StringUtils.replace(sContentnt, ":\"NULL\"",
          ":\"\"");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      _oJSON = JSONObject.fromObject(sContentnt);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return _oJSON;
  }

  /**
   * 将String转换为JSON格式
   *
   * @return 内容为空或格式不正确返回null
   */
  public static JSONArray parseJSONArray(String sContentnt) {
    JSONArray _oJSON = null;
    if (!"".equals(sContentnt)) {
      try {
        sContentnt = StringUtils.replace(sContentnt, ":\"null\"",
          ":\"\"");
        sContentnt = StringUtils.replace(sContentnt, ":\"NULL\"",
          ":\"\"");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      _oJSON = JSONArray.fromObject(sContentnt);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return _oJSON;
  }

  /**
   * 當前日期減去對應的天數
   *
   * @param sDate     日期字符串
   * @param iResetDay 加减天数
   * @return
   */
  public static String getDateString(String sDate, int iResetDay) {
    if (null == sDate || "".equals(sDate) || sDate.length() != 8) {
      return sDate;
    }
    SimpleDateFormat dfyMd = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat dfy_M_d = new SimpleDateFormat("yyyy-MM-dd");
    Date dt = null;
    try {
      dt = dfy_M_d.parse(dfy_M_d.format(dfyMd.parse(sDate)));
    } catch (java.text.ParseException e) {
      e.printStackTrace();
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(dt);
    cal.add(Calendar.DATE, iResetDay);
    String a = dfyMd.format(cal.getTime());
    return a;
  }

  // ８或１３码编号由系统产生最后一码检核码
  public static String getEAN13(String sEan){
    String sEAN13 = "";
    int iOddCount =0; //奇数合计
    int iEvenCount = 0; //偶数合计
    int iTemp = 0;
    // if(sEan !=null && sEan.length() == 12){
    if(sEan != null ){
      for(int i=0; i<sEan.length(); i++){
        iTemp = Integer.parseInt(String.valueOf(sEan.charAt(i)));
        if((i+1)%2==0){
          iEvenCount += iTemp;
        } else {
          iOddCount += iTemp;
        }
      }
      iEvenCount = iEvenCount * 3; //偶数位之合再　*　3
      String sTemp = String.valueOf(iEvenCount + iOddCount);
      sTemp =  sTemp.substring(sTemp.length()-1,sTemp.length()); // 取个位数;

      if("0".equals(sTemp)){
        sEAN13 = "0";
      } else {
        sEAN13 = String.valueOf(10 - Integer.parseInt(sTemp));
      }
    }
    return sEAN13;
  }

  public static String errorToString(Exception e,int iLen){
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(output));
    if(iLen == 0){
      return new String(output.toByteArray());
    } else {
      byte[] b = output.toByteArray();
      if(b.length > iLen){
        return new String(output.toByteArray(),0,iLen);
      } else {
        return new String(output.toByteArray());
      }
    }
  }

  static String SimplifiedStr  = "东五店皑蔼碍爱袄奥坝罢摆败颁办绊帮绑镑谤剥饱宝报鲍辈贝钡狈备惫绷笔毕毙闭边编贬变辩辫瘪濒滨宾摈饼拨钵铂驳卜补参蚕残惭惨灿苍舱仓沧厕侧册测层诧搀掺蝉馋谗缠铲阐颤场尝长偿肠厂畅钞车彻尘陈衬撑称惩诚骋痴迟驰耻齿炽冲虫宠畴踌筹绸丑橱厨锄雏础储触处传疮闯创锤纯绰辞词赐聪葱囱从丛凑窜错达带贷担单郸掸胆惮诞弹当挡党荡档捣岛祷导盗灯邓敌涤递缔点垫电淀钓调谍叠钉顶锭订动栋冻斗犊独读赌镀锻断缎兑队对吨顿钝夺鹅额讹恶饿儿尔饵贰发罚阀珐矾钒烦范贩饭访纺飞废费纷坟奋愤粪丰枫锋风疯冯缝讽凤肤辐抚辅赋复负讣妇缚该钙盖干赶秆赣冈刚钢纲岗镐搁鸽阁铬个给龚宫巩贡沟构购够蛊顾剐关观馆惯贯广规硅归龟闺轨诡柜贵刽辊滚锅国过骇韩汉阂鹤贺横轰鸿红后壶护沪户哗华画划话怀坏欢环还缓换唤痪焕涣黄谎挥辉毁贿秽会烩汇讳诲绘荤浑伙获货祸击机积饥讥鸡绩缉极辑级挤几蓟剂济计记际继纪夹荚颊贾钾价驾歼监坚笺间艰缄茧检碱硷拣捡简俭减荐槛鉴践贱见键舰剑饯渐溅涧浆蒋桨奖讲酱胶浇骄娇搅铰矫侥脚饺缴绞轿较阶节茎惊经颈静镜径痉竞净纠厩旧驹举据锯惧剧鹃绢杰洁结诫届紧锦仅谨进晋烬尽劲荆觉决诀绝钧军骏开凯颗壳课垦恳抠库裤夸块侩宽矿旷况亏岿窥馈溃扩阔蜡腊莱来赖蓝栏拦篮阑兰澜谰揽览懒缆烂滥捞劳涝乐镭垒类泪篱离鲤礼丽厉励砾历沥隶俩联莲连镰怜涟帘敛脸链恋炼练粮凉两辆谅疗辽镣猎临邻鳞凛赁龄铃凌灵岭领馏刘龙聋咙笼垄拢陇楼娄搂篓芦卢颅庐炉掳卤虏鲁赂禄录陆驴吕铝侣屡缕虑滤绿峦挛孪滦乱抡轮伦仑沦纶论萝罗逻锣箩骡骆络妈玛码蚂马骂吗买麦卖迈脉瞒馒蛮满谩猫锚铆贸霉没镁门闷们锰梦谜弥觅绵缅庙灭闵悯闽鸣铭谬谋亩钠纳难挠脑恼闹馁腻撵捻酿鸟聂啮镊镍柠狞宁拧泞钮纽脓浓农疟诺欧鸥殴呕沤盘庞国爱赔喷鹏骗飘频贫苹凭评泼颇扑铺朴谱脐齐骑岂气弃讫牵扦铅迁签谦钱钳潜浅谴堑枪呛墙蔷强抢锹桥乔侨翘窍窃钦亲轻氢倾顷请庆琼穷趋区躯驱龋颧权劝却鹊让饶扰绕热韧认纫荣绒软锐闰润洒萨鳃赛伞丧骚扫涩杀纱筛晒闪陕赡缮伤赏烧绍赊摄慑设绅审婶肾渗声绳胜圣师狮湿诗尸时蚀实识驶势释饰视试寿兽枢输书赎属术树竖数帅双谁税顺说硕烁丝饲耸怂颂讼诵擞苏诉肃虽绥岁孙损笋缩琐锁獭挞摊贪瘫滩坛谭谈叹汤烫涛腾誊锑题体屉条贴铁厅听烃铜统头图涂团颓蜕脱鸵驮驼椭洼袜弯湾顽万网韦违围潍维苇伟纬谓卫温闻纹稳问瓮挝蜗涡窝呜钨乌诬无芜吴坞雾务误锡牺袭习铣戏细虾辖峡侠狭厦鲜纤咸贤衔闲显险现献县馅羡宪线厢镶乡详响项萧销晓啸蝎协挟携胁谐写泻谢锌衅兴汹锈绣虚嘘须许绪续轩悬选癣绚学勋询寻驯训讯逊压鸦鸭哑亚讶阉烟盐严阎艳厌砚彦谚验鸯杨扬疡阳痒养样瑶摇尧遥窑谣药爷页业叶医铱颐遗仪蚁艺亿忆义诣议谊译异绎荫阴银饮樱婴鹰应缨莹萤营荧蝇颖哟拥佣痈踊咏涌优忧邮铀犹游诱舆鱼渔娱与屿语吁御狱誉预驭鸳渊辕园员圆缘远愿约跃钥岳粤悦阅云郧匀陨运蕴酝晕韵杂灾载攒暂赞赃脏凿枣责择则泽贼赠扎札轧铡闸诈斋债毡盏斩辗崭栈战绽张涨帐账胀赵蛰辙锗这贞针侦诊镇阵挣睁狰帧郑证织职执纸挚掷帜质钟终种肿诌轴皱昼骤猪诸诛烛瞩嘱贮铸筑驻专砖转赚桩庄装妆壮状锥赘坠缀谆浊兹资渍踪综总纵邹诅组钻致钟么为只凶准启板里雳余链泄";
  static String TraditionalStr = "東五店皚藹礙愛襖奧壩罷擺敗頒辦絆幫綁鎊謗剝飽寶報鮑輩貝鋇狽備憊繃筆畢斃閉邊編貶變辯辮癟瀕濱賓擯餅撥缽鉑駁蔔補參蠶殘慚慘燦蒼艙倉滄廁側冊測層詫攙摻蟬饞讒纏鏟闡顫場嘗長償腸廠暢鈔車徹塵陳襯撐稱懲誠騁癡遲馳恥齒熾沖蟲寵疇躊籌綢醜櫥廚鋤雛礎儲觸處傳瘡闖創錘純綽辭詞賜聰蔥囪從叢湊竄錯達帶貸擔單鄲撣膽憚誕彈當擋黨蕩檔搗島禱導盜燈鄧敵滌遞締點墊電澱釣調諜疊釘頂錠訂動棟凍鬥犢獨讀賭鍍鍛斷緞兌隊對噸頓鈍奪鵝額訛惡餓兒爾餌貳發罰閥琺礬釩煩範販飯訪紡飛廢費紛墳奮憤糞豐楓鋒風瘋馮縫諷鳳膚輻撫輔賦複負訃婦縛該鈣蓋幹趕稈贛岡剛鋼綱崗鎬擱鴿閣鉻個給龔宮鞏貢溝構購夠蠱顧剮關觀館慣貫廣規矽歸龜閨軌詭櫃貴劊輥滾鍋國過駭韓漢閡鶴賀橫轟鴻紅後壺護滬戶嘩華畫劃話懷壞歡環還緩換喚瘓煥渙黃謊揮輝毀賄穢會燴彙諱誨繪葷渾夥獲貨禍擊機積饑譏雞績緝極輯級擠幾薊劑濟計記際繼紀夾莢頰賈鉀價駕殲監堅箋間艱緘繭檢堿鹼揀撿簡儉減薦檻鑒踐賤見鍵艦劍餞漸濺澗漿蔣槳獎講醬膠澆驕嬌攪鉸矯僥腳餃繳絞轎較階節莖驚經頸靜鏡徑痙競淨糾廄舊駒舉據鋸懼劇鵑絹傑潔結誡屆緊錦僅謹進晉燼盡勁荊覺決訣絕鈞軍駿開凱顆殼課墾懇摳庫褲誇塊儈寬礦曠況虧巋窺饋潰擴闊蠟臘萊來賴藍欄攔籃闌蘭瀾讕攬覽懶纜爛濫撈勞澇樂鐳壘類淚籬離鯉禮麗厲勵礫曆瀝隸倆聯蓮連鐮憐漣簾斂臉鏈戀煉練糧涼兩輛諒療遼鐐獵臨鄰鱗凜賃齡鈴淩靈嶺領餾劉龍聾嚨籠壟攏隴樓婁摟簍蘆盧顱廬爐擄鹵虜魯賂祿錄陸驢呂鋁侶屢縷慮濾綠巒攣孿灤亂掄輪倫侖淪綸論蘿羅邏鑼籮騾駱絡媽瑪碼螞馬罵嗎買麥賣邁脈瞞饅蠻滿謾貓錨鉚貿黴沒鎂門悶們錳夢謎彌覓綿緬廟滅閔憫閩鳴銘謬謀畝鈉納難撓腦惱鬧餒膩攆撚釀鳥聶齧鑷鎳檸獰甯擰濘鈕紐膿濃農瘧諾歐鷗毆嘔漚盤龐國愛賠噴鵬騙飄頻貧蘋憑評潑頗撲鋪樸譜臍齊騎豈氣棄訖牽扡鉛遷簽謙錢鉗潛淺譴塹槍嗆牆薔強搶鍬橋喬僑翹竅竊欽親輕氫傾頃請慶瓊窮趨區軀驅齲顴權勸卻鵲讓饒擾繞熱韌認紉榮絨軟銳閏潤灑薩鰓賽傘喪騷掃澀殺紗篩曬閃陝贍繕傷賞燒紹賒攝懾設紳審嬸腎滲聲繩勝聖師獅濕詩屍時蝕實識駛勢釋飾視試壽獸樞輸書贖屬術樹豎數帥雙誰稅順說碩爍絲飼聳慫頌訟誦擻蘇訴肅雖綏歲孫損筍縮瑣鎖獺撻攤貪癱灘壇譚談歎湯燙濤騰謄銻題體屜條貼鐵廳聽烴銅統頭圖塗團頹蛻脫鴕馱駝橢窪襪彎灣頑萬網韋違圍濰維葦偉緯謂衛溫聞紋穩問甕撾蝸渦窩嗚鎢烏誣無蕪吳塢霧務誤錫犧襲習銑戲細蝦轄峽俠狹廈鮮纖鹹賢銜閑顯險現獻縣餡羨憲線廂鑲鄉詳響項蕭銷曉嘯蠍協挾攜脅諧寫瀉謝鋅釁興洶鏽繡虛噓須許緒續軒懸選癬絢學勳詢尋馴訓訊遜壓鴉鴨啞亞訝閹煙鹽嚴閻豔厭硯彥諺驗鴦楊揚瘍陽癢養樣瑤搖堯遙窯謠藥爺頁業葉醫銥頤遺儀蟻藝億憶義詣議誼譯異繹蔭陰銀飲櫻嬰鷹應纓瑩螢營熒蠅穎喲擁傭癰踴詠湧優憂郵鈾猶遊誘輿魚漁娛與嶼語籲禦獄譽預馭鴛淵轅園員圓緣遠願約躍鑰嶽粵悅閱雲鄖勻隕運蘊醞暈韻雜災載攢暫贊贓髒鑿棗責擇則澤賊贈紮劄軋鍘閘詐齋債氈盞斬輾嶄棧戰綻張漲帳賬脹趙蟄轍鍺這貞針偵診鎮陣掙睜猙幀鄭證織職執紙摯擲幟質鍾終種腫謅軸皺晝驟豬諸誅燭矚囑貯鑄築駐專磚轉賺樁莊裝妝壯狀錐贅墜綴諄濁茲資漬蹤綜總縱鄒詛組鑽緻鐘麼為隻兇準啟闆裡靂餘鍊洩";

  // 简体字转繁体
  public static String Traditionalized(String cc){
    String str="";
    for(int i=0;i<cc.length();i++) {
      if((int)cc.charAt(i)>10000&& SimplifiedStr.indexOf(cc.charAt(i))!=-1) str+= TraditionalStr.charAt(SimplifiedStr.indexOf(cc.charAt(i)));
      else str+=cc.charAt(i);
    }
    return str;
  }

  // 繁体字转简体
  public static String Simplized(String cc){
    String str="";
    for(int i=0;i<cc.length();i++) {
      if(cc.charAt(i)>10000&& TraditionalStr.indexOf(cc.charAt(i))!=-1)str+= SimplifiedStr.charAt(TraditionalStr.indexOf(cc.charAt(i)));
      else str+=cc.charAt(i);
    }
    return str;
  }

    public static void main(String[] args) {
        emisUtil t = new emisUtil();
        //  System.out.println(emisUtil.formatDateTime("%m",new Date()));
        try {
//            emisServletContext servlet = new emisServletContext();
//            emisServerFactory.createServer(servlet, "c:\\wwwroot\\wtn",
//                    "c:\\resin3X\\wtn.cfg", true);
//            System.out.println(emisUtil.getWeekInfo(servlet,"20050102"));
            /*
        System.out.println( ((new emisDate("20041227").getDiff(new emisDate("20050121"))+1) % 7));
           String[] a =t.getDayOfWeekArray("2005/01/01");
           for(int i =0; i<7;i++){
              System.out.println(a[i]);
           }
           System.out.println(t.getDayOfWeekString("2005/01/01"));
        */
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
