/*
 * $Header: /repository/src3/src/com/emis/util/emisDate.java,v 1.1.1.1 2005/10/14 12:43:20 andy Exp $
 *
 * 1996/02/29,2000/02/29,2100/02/'28',2104/02/29...
 *
 * 2003/10/24 Jerry: 加入getWeek
 */
package com.emis.util;

import java.util.Calendar;
import java.util.Date;

/**
 *  提供日期運算函數<BR>
 *  預設為西元格式,若傳入日期參數長度小於8則轉換成
 *  民國格式
 */
public class emisDate {
    private Date oDate_;
    // 是否用西元表示
    private boolean isAD_;

    public emisDate() {
        oDate_ = emisUtil.now();
        isAD_ = true;
    }

    /**
     *  無參數輸入，系統自動抓取現在日期並轉成Date型式<BR>
     */
    public emisDate(boolean isAD) {
        oDate_ = emisUtil.now();
        isAD_ = isAD;
    }

    /**
     *  輸入日期字串，系統自動轉成正確的Date型式<BR>
     *  @param sStr - <BR>
     *  可接受參數類型："20010101","0900101","900101"<BR>
     *  　　　　　　　　"2001/01/01","090/01/01","90/01/01"<BR>
     *  　　　　　　　　"2001.01.01","090.01.01","90.01.01"<BR>
     *  　　　　　　　　"2001-01-01","090-01-01","90-01-01"<BR>
     */
    public emisDate(String sStr) throws Exception {
        try {
            oDate_ = cvtDateStrToLegal(sStr);
        } catch (Exception e) {
        }
    }

    public emisDate(Date oDate, boolean isAD) {
        oDate_ = oDate;
        isAD_ = isAD;
    }

    /**
     *  將輸入的日期字串轉成"090/01/01"格式<BR>
     *  可接受參數類型："20010101","0900101","900101"<BR>
     *  　　　　　　　　"2001/01/01","090/01/01","90/01/01"<BR>
     *  　　　　　　　　"2001.01.01","090.01.01","90.01.01"<BR>
     *  　　　　　　　　"2001-01-01","090-01-01","90-01-01"<BR>
     *  Return: String - "090/01/01"<BR>
     *  PS:不會判別輸入之字串是否為合理之日期字串
     */
    private Date cvtDateStrToLegal(String sDate) throws Exception {
        String _sStr = sDate.trim();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < _sStr.length(); i++) {
            char c = _sStr.charAt(i);
            if ((c == '.') || (c == '-') || (c == '/')) {
                continue;
            }
            buf.append(c);
        }
        boolean _isDCFormat = (buf.length() == 8) ? true : false;

        if ((buf.length() != 7) && (buf.length() != 6) && (!_isDCFormat)) {
            throw new Exception("date string length error:" + buf.toString());
        }
        if (buf.length() == 6) {
            buf.insert(0, "0");
        }
        int _year,_month,_date;

        if (_isDCFormat) {
            isAD_ = true;
            _year = Integer.parseInt(buf.substring(0, 4));
            _month = Integer.parseInt(buf.substring(4, 6)) - 1;
            _date = Integer.parseInt(buf.substring(6, 8));
        } else {
            isAD_ = false;
            _year = Integer.parseInt(buf.substring(0, 3)) + 1911;
            _month = Integer.parseInt(buf.substring(3, 5)) - 1;
            _date = Integer.parseInt(buf.substring(5, 7));
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, _year);
        calendar.set(Calendar.MONTH, _month);
        calendar.set(Calendar.DATE, _date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    /**
     *  輸出日期字串，無參數表示直接輸出此emisDate之日期<BR>
     */
    public String toString() {
        if (isAD_) {
            return emisUtil.formatDateTime("%y%M%D", oDate_);
        } else {
            return emisUtil.formatDateTime("%Y%M%D", oDate_);
        }
    }

    public String toString(boolean isAD) {
        if (isAD) {
            return emisUtil.formatDateTime("%y%M%D", oDate_);
        } else {
            return emisUtil.formatDateTime("%Y%M%D", oDate_);
        }
    }

    /**
     *  輸出日期字串，參數為輸出之格式定義<BR>
     *  @param sSepr - 可為"/","-"等，不可使用"%"<BR>
     */
    public String toString(String sSepr) {
        if (isAD_) {
            return emisUtil.formatDateTime("%y" + sSepr + "%M" + sSepr + "%D", oDate_);
        } else {
            return emisUtil.formatDateTime("%Y" + sSepr + "%M" + sSepr + "%D", oDate_);
        }
    }

    /**
     *  輸出日期字串，參數為輸出之格式定義<BR>
     *  @param sSepr - 可為"/","-"等，不可使用"%"<BR>
     *  @param bAD - true表示輸出以西元年表示<BR>
     */
    public String toString(String sSepr, boolean bAD) {
        if (bAD)
            return emisUtil.formatDateTime("%y" + sSepr + "%M" + sSepr + "%D", oDate_);
        else
            return emisUtil.formatDateTime("%Y" + sSepr + "%M" + sSepr + "%D", oDate_);
    }

    /**
     *  輸出自1970/01/01日 00:00:00 GMT至今之Milliseconds<BR>
     */
    public long getTime() {
        return oDate_.getTime();
    }

    /**
     *  計算emisDate加上iDays後之日期<BR>
     *  @param iDays - iDays可為負值<BR>
     */
    public emisDate addDay(int iDays) throws Exception {
        return this.addProc(Calendar.DATE, iDays);
    }

    /**
     *  計算emisDate加上iDays後之日期<BR>
     *  @param iDays - iDays可為負值<BR>
     */
    public emisDate add(int iDays) throws Exception {
        return this.addProc(Calendar.DATE, iDays);
    }

    /**
     *  計算emisDate加上iMonths後之月份<BR>
     *  @param iMonths 可為負值<BR>
     */
    public emisDate addMonth(int iMonths) throws Exception {
        return this.addProc(Calendar.MONTH, iMonths);
    }

    /**
     *  計算emisDate加上iYears後之年份<BR>
     *  @param iYears - iYears可為負值<BR>
     */
    public emisDate addYear(int iYears) throws Exception {
        return this.addProc(Calendar.YEAR, iYears);
    }

    /**
     *  addProc() call by 'add' functions
     */
    private emisDate addProc(int iType, int iDays) throws Exception {
        Calendar _oCal = emisUtil.getLocaleCalendar();

        _oCal.setTime(oDate_);
        _oCal.add(iType, iDays);
        //oDate_ = _oCal.getTime();  // 修正oDate_

        emisDate _oED = new emisDate(_oCal.getTime(), isAD_);
        return _oED;
    }

    /**
     *  計算輸入參數之"年月"最後一天之日期<BR>
     *  @param sYYMM - <BR>
     *  可接受參數類型："200101","09001","9001"<BR>
     *  　　　　　　　　"2001/01","090/01","90/01"<BR>
     *  　　　　　　　　"2001.01","090.01","90.01"<BR>
     *  　　　　　　　　"2001-01","090-01","90-01"<BR>
     */
    public static emisDate getLastDate(String sYYMM) throws Exception {
        String _sStr = sYYMM + "01";
        boolean isAD = (_sStr.length() == 8) ? true : false;

        // 下列經由strToDate()來處理不合理之日期字串
        Date _oD = new emisDate(_sStr).getDate();
        Calendar _oCal = emisUtil.getLocaleCalendar();
        _oCal.setTime(_oD);
        // 取出當月最後一天日期
        int _iLast = _oCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        _oCal.set(Calendar.DATE, _iLast);
        return new emisDate(_oCal.getTime(), isAD);
    }

    public emisDate getLastDate() throws Exception {
        Calendar _oCal = emisUtil.getLocaleCalendar();
        _oCal.setTime(oDate_);
        // 取出當月最後一天日期
        int _iLast = _oCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        _oCal.set(Calendar.DATE, _iLast);
        return new emisDate(_oCal.getTime(), isAD_);
    }

    /**
     *  取出emisDate的日期<BR>
     */
    public Date getDate() {
        return this.oDate_;
    }

    /**
     *  取出emisDate的日期字串<BR>
     */
    public String getDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oDate_);
        String _date = String.valueOf(calendar.get(Calendar.DATE));
        if (_date.length() < 2) {
            _date = "0" + _date;
        }
        return _date;
    }

    /**
     *  取出emisDate的月份字串<BR>
     */
    public String getMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oDate_);
        String _date = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        if (_date.length() < 2) {
            _date = "0" + _date;
        }
        return _date;
    }

    /**
     *  取出emisDate的年份字串<BR>
     *  傳入是否為西元年
     */
    public String getYear(boolean isAD) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oDate_);
        int _year = calendar.get(Calendar.YEAR);
        if (isAD) {
            return String.valueOf(_year);
        } else {
            String _date = String.valueOf(_year);
            if (_date.length() < 3) {
                _date = "0" + _date;
            }
            return _date;
        }
    }

    /**
     *  取出emisDate的年份字串<BR>
     *  傳入是否為西元年
     */
    public String getYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oDate_);
        int _year = calendar.get(Calendar.YEAR);
        if (isAD_) {
            return String.valueOf(_year);
        } else {
            String _date = String.valueOf(_year);
            if (_date.length() < 3) {
                _date = "0" + _date;
            }
            return _date;
        }
    }

    /**
     *  比較兩個emisDate的日期是否相同<BR>
     *  Return: true - 相同 / false - 不同<BR>
     */
    public boolean equals(emisDate oED) {
        String _sSrc = this.toString();
        if (_sSrc.equals(oED.toString("", isAD_)))  // 不直接拿雙方之oDate_相比是因為
            return true;                  // oDate_包含了"時分秒"，比較不準
        return false;
    }

    /**
     *  比較emisDate的日期是否與輸入參數相同<BR>
     *  @param sDate - 日期字串<BR>
     *  可接受參數類型："20010101","0900101","900101"<BR>
     *  　　　　　　　　"2001/01/01","090/01/01","90/01/01"<BR>
     *  　　　　　　　　"2001.01.01","090.01.01","90.01.01"<BR>
     *  　　　　　　　　"2001-01-01","090-01-01","90-01-01"<BR>
     *  Return: true - 相同 / false - 不同<BR>
     */
    public boolean equals(String sDate) throws Exception {
        emisDate oED = new emisDate(sDate);
        return equals(oED);
    }

    /**
     *  比較兩個emisDate的日期相差多少日？<BR>
     */
    public int getDayDiff(emisDate oED) {
        return this.getDayDiffProc(oDate_, oED.getDate());
    }

    /**
     *  比較兩個emisDate的日期相差多少日？<BR>
     */
    public int getDiff(emisDate oED, boolean isAbs) {
        if (isAbs) {
            return this.getDayDiffProc(oDate_, oED.getDate());
        } else {
            return this.getDayDiffProcN(oDate_, oED.getDate());
        }
    }

    /**
     *  比較兩個emisDate的日期相差多少日？<BR>
     */
    public int getDiff(emisDate oED) {
        return this.getDayDiffProc(oDate_, oED.getDate());
    }


    /**
     *  比較emisDate和輸入之日期參數相差多少日？<BR>
     *  @param sStr
     *  可接受參數類型："20010101","0900101","900101"<BR>
     *  　　　　　　　　"2001/01/01","090/01/01","90/01/01"<BR>
     *  　　　　　　　　"2001.01.01","090.01.01","90.01.01"<BR>
     *  　　　　　　　　"2001-01-01","090-01-01","90-01-01"<BR>
     */
    public int getDayDiff(String sStr) throws Exception {
        return this.getDiff(sStr);
    }

    /**
     *  比較emisDate和輸入之日期參數相差多少日？<BR>
     *  注意事項：回傳值不會有負值，單純取得相差日數<BR>
     *  @param sStr
     *  可接受參數："20010101","0900101","900101"<BR>
     *  　　　　　　"2001/01/01","090/01/01","90/01/01"<BR>
     *  　　　　　　"2001.01.01","090.01.01","90.01.01"<BR>
     *  　　　　　　"2001-01-01","090-01-01","90-01-01"<BR>
     */
    public int getDiff(String sStr) throws Exception {
        Date _oD = cvtDateStrToLegal(sStr);
        return this.getDayDiffProc(oDate_, _oD);
    }

     /**
     *  比較emisDate和輸入之日期參數相差多少日？<BR>
     *  注意事項：回傳值不會有負值，單純取得相差日數<BR>
     *  @param sStr
     *  可接受參數："20010101","0900101","900101"<BR>
     *  　　　　　　"2001/01/01","090/01/01","90/01/01"<BR>
     *  　　　　　　"2001.01.01","090.01.01","90.01.01"<BR>
     *  　　　　　　"2001-01-01","090-01-01","90-01-01"<BR>
     *
     */
    public int getDiff(String sStr, boolean isAbs) throws Exception {
        Date _oD = cvtDateStrToLegal(sStr);
        if (isAbs) {
            return this.getDayDiffProc(oDate_, _oD);
        } else {
            return this.getDayDiffProcN(oDate_, _oD);
        }
    }

    /**
     *  getDayDiffProc(Date, Date) call by getDayDiff functions
     */
    private int getDayDiffProc(Date oSrc, Date oTar) {
        long _lSrc = oSrc.getTime();  // convert to milli-seconds
        long _lTar = oTar.getTime();

        _lSrc /= (24 * 60 * 60 * 1000); // 換算成天數
        _lTar /= (24 * 60 * 60 * 1000); // 換算成天數

        if (_lSrc < _lTar) {
            long _lTmp = _lSrc;
            _lSrc = _lTar;
            _lTar = _lTmp;
        }
        return (int) (_lSrc - _lTar);
    }

    private int getDayDiffProcN(Date oSrc, Date oTar) {
        long _lSrc = oSrc.getTime();  // convert to milli-seconds
        long _lTar = oTar.getTime();

        _lSrc /= (24 * 60 * 60 * 1000); // 換算成天數
        _lTar /= (24 * 60 * 60 * 1000); // 換算成天數
        /*
         if (_lSrc < _lTar) {
           long _lTmp = _lSrc;
           _lSrc = _lTar;
           _lTar = _lTmp;
         }*/
        return (int) (_lSrc - _lTar);
    }


    /**
     *  比較emisDate和輸入之日期參數相差多少月？<BR>
     *  @param oED
     */
    public int getMonthDiff(emisDate oED) {
        return this.getMonthProc(oED);
    }

    /**
     *  比較emisDate和輸入之日期參數相差多少月？<BR>
     *  @param sStr
     *  可接受參數類型："200101xx","09001xx","9001xx"<BR>
     *  　　　　　　　　"2001/01/xx","090/01/xx","90/01/xx"<BR>
     *  　　　　　　　　"2001.01.xx","090.01.xx","90.01.xx"<BR>
     *  　　　　　　　　"2001-01-xx","090-01-xx","90-01-xx"<BR>
     */
    public int getMonthDiff(String sStr) throws Exception {
        emisDate oED = new emisDate(sStr);
        return this.getMonthProc(oED);
    }

    /**
     *  getMonthProc(String, String) call by getMonthDiff functions
     */
    private int getMonthProc(emisDate oMonDiff) {
        int nDiffYear = Integer.parseInt(oMonDiff.getYear(true));
        int nDiffMonth = Integer.parseInt(oMonDiff.getMonth());

        int nYear = Integer.parseInt(getYear(true));
        int nMonth = Integer.parseInt(getMonth());

        nDiffMonth = nDiffYear * 12 + nDiffMonth;
        nMonth = nYear * 12 + nMonth;

        if (nMonth > nDiffMonth) {
            return nMonth - nDiffMonth;
        } else {
            return nDiffMonth - nMonth;
        }
    }

    /**
     *  比較emisDate和輸入之日期參數相差多少年？<BR>
     *  @param oED
     */
    public int getYearDiff(emisDate oED) {
        return this.getYearProc(oED);
    }

    /**
     *  比較emisDate和輸入之日期參數相差多少年？<BR>
     *  @param sStr
     *  可接受參數類型："2001xxxx","090xxxx","90xxxx"<BR>
     *  　　　　　　　　"2001/xx/xx","090/xx/xx","90/xx/xx"<BR>
     *  　　　　　　　　"2001.xx.xx","090.xx.xx","90.xx.xx"<BR>
     *  　　　　　　　　"2001-xx-xx","090-xx-xx","90-xx-xx"<BR>
     */
    public int getYearDiff(String sStr) throws Exception {
        emisDate d = new emisDate(sStr);
        return this.getYearProc(d);
    }

    /**
     *  getYearProc(String, String) call by getYearDiff functions
     */
    private int getYearProc(emisDate oDiffYear) {
        int nDiffYear = Integer.parseInt(oDiffYear.getYear(true));
        int nYear = Integer.parseInt(getYear(true));
        if (nYear > nDiffYear) {
            return nYear - nDiffYear;
        } else {
            return nDiffYear - nYear;
        }
    }

    /**
     * 傳回星期幾, 0 (週日), 1..6
     * @return
     */
    public int getWeek() {
        Calendar _oCalendar = Calendar.getInstance();
        _oCalendar.setTime(oDate_);
        int _iWeek = _oCalendar.get(Calendar.DAY_OF_WEEK);
        _oCalendar = null;
        return _iWeek - 1;
    }

    public static void main(String[] argvs) throws Exception {
        /**
         emisDate _oD = new emisDate("18001228");
         _oD.add(73390);
         System.out.print(_oD.toString());
         */
        System.out.println("=======TEST=======");
        System.out.println("Immutable test-------");
        emisDate d = new emisDate("0910121");
        emisDate newd = d.add(15);
        System.out.println(d);
        System.out.println(newd);

        System.out.println("format test-------");
        d = new emisDate("0910121");
        newd = new emisDate("20020121");
        if (d.equals(newd)) {
            System.out.println("equal");
        }
        if (d.equals("20020121")) {
            System.out.println("equal");
        }

        System.out.println("diff test-------");
        d = new emisDate("0910121");
        newd = new emisDate("20020121");
        System.out.println("DAY  DIFF:" + d.getDayDiff(newd));
        System.out.println("MON  DIFF:" + d.getMonthDiff(newd));
        System.out.println("YEAR DIFF:" + d.getYearDiff(newd));

        d = new emisDate();
        newd = d.add(-7);
        System.out.println("d=" + d.toString(false));
        System.out.println("newd=" + newd.toString(false));


        System.out.println("get test-------");
        d = new emisDate("0910121");
        System.out.println(d.getLastDate());

        d = new emisDate("0910221");
        System.out.println(d.getLastDate());
        System.out.println(d.getYear() + ":" + d.getMonth() + ":" + d.getDay());

        System.out.println("set test-------");

        d = new emisDate("20031023");
        System.out.println("week of d=" + d.getWeek());
        d = new emisDate();
        System.out.println("week of d=" + d.getWeek());
    }
}

