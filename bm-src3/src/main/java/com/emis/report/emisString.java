/* $Header: /repository/src3/src/com/emis/report/emisString.java,v 1.1.1.1 2005/10/14 12:42:22 andy Exp $
 * @author KC 1.0
 * @version 2003/10/31 Jerry: 增加pad的指定填入字元.
 */
package com.emis.report;

import com.emis.util.emisUTF8StringUtil;

/**
 * Title:        emisString
 * Description:  字串處理
 * Copyright:    Copyright (c) 2000
 * Company:      EMIS
 * Track+[14291] wing 20100202 字符串取长修正，但报表的地方使用了emisUtil，不统一
 * 
 * @author       KC
 * @version 1.0
 */
final public class emisString {
    private String sStr_ = "";

    public emisString(String s) {
        sStr_ = s;
    }

    public String subStringB(int beginIndex, int length) {
        return subStringB(sStr_, beginIndex, length);
    }

    public String subStringB(int beginIndex) {
        return subStringB(sStr_, beginIndex);
    }

    public String leftB(int length) {
        return leftB(sStr_, length);
    }

    public String rightB(int length) {
        return rightB(sStr_, length);
    }

    public String rTrim() {
        return rTrim(sStr_);
    }

    public String lTrim() {
        return lTrim(sStr_);
    }

    public String trim() {
        return trim(sStr_);
    }

    public int lengthB() {
        return lengthB(sStr_);
    }

    /*
     * subString()
     * 以 byte 數取子字串
     * 傳入: String s       => 來源字串
     *       int beginIndex => 開始位元組位置
     *       int length     => 欲取位元組長度
     * 傳回: String
     */
    public static String subStringB(String s, int beginIndex, int length) {
      if (s == null) s = "";
      String _sRet = "";  // 傳回字串
      int _iByteOff = 0;      // 以 byte 計算的累進位置
      int _iNumBytes = 0;    // 取得 byte 數

      try {
        StringBuffer sb = new StringBuffer(length);

        for (int i = 0; i < s.length(); i++) {

          // 判斷字元是否為中文
          if (Character.UnicodeBlock.of(s.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN) {
            // 中文字元
            if (_iByteOff == (beginIndex - 1)) {
              // 第一byte為中文字後半開始, 該字元不取, 以 1byte 空白取代
              // 修正当第一byte为中文字后半开始时，取全该中文字。
              if (_iNumBytes + 2 > length) { // 如果超出指定的长度，则以空白取代
                sb.append(" ");
                _iNumBytes++;
              } else {
                sb.append(s.charAt(i));
                _iNumBytes += 2;
              }
            } else if (_iByteOff >= beginIndex) {
              if (_iNumBytes + 2 > length) {
                // 已達欲取長度, 最後為中文字的前半, 該字元不取, 以 1byte 空白取代
                sb.append(" ");
                _iNumBytes++;
              } else {
                sb.append(s.charAt(i));
                _iNumBytes += 2;
              }
            }
            _iByteOff += 2; // 中文字元 byte 位置累進 2
          } else {
            // 非中文字元
            if (_iByteOff >= beginIndex) {
              sb.append(s.charAt(i));
              _iNumBytes++;
            }
            _iByteOff++;  // 非中文字元 byte 位置累進 1
          }

          if (_iNumBytes >= length) break;  // 已達欲取長度, 結束迴圈
        } // end for
        _sRet = sb.toString();
      } catch (Exception e) {
        // System.out.println(e.getMessage());
      } finally {

      } // end try
      return _sRet;

    } // end subStringB()

    public static String subStringB(String s, int beginIndex, int length, boolean isGetFirstWord) {
        if (isGetFirstWord) {
            return emisString.subStringB(s, beginIndex, length);
        } else {
            return emisString.subStringBE(s, beginIndex, length);
        }
    }

    public static String subStringBE(String s, int beginIndex, int length) {
        if (s == null) {
            return "";
        }
        if (beginIndex < 0 || length < 0 || (length - beginIndex) < 0) {
            return "";
        }
        if (s == null) s = "";
        String _sRet = "";  // 傳回字串
        int _iByteOff = 0;      // 以 byte 計算的累進位置
        int _iNumBytes = 0;    // 取得 byte 數

        try {
            StringBuffer sb = new StringBuffer(length);

            for (int i = 0; i < s.length(); i++) {

                // 判斷字元是否為中文
                if (Character.UnicodeBlock.of(s.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN) {
                    // 中文字元
                    if (_iByteOff == (beginIndex - 1)) {
                        // 第一byte為中文字後半開始, 該字元不取, 以 1byte 空白取代
                        sb.append(s.charAt(i));
                        _iNumBytes += 2;
                        //_iNumBytes++;
                    } else if (_iByteOff >= beginIndex) {
                        if (_iNumBytes + 2 > length) {
                            // 已達欲取長度, 最後為中文字的前半, 該字元不取, 以 1byte 空白取代
                            sb.append(" ");
                            _iNumBytes++;
                        } else {
                            sb.append(s.charAt(i));
                            _iNumBytes += 2;
                        }
                    }
                    _iByteOff += 2; // 中文字元 byte 位置累進 2
                } else {
                    // 非中文字元
                    if (_iByteOff >= beginIndex) {
                        sb.append(s.charAt(i));
                        _iNumBytes++;
                    }
                    _iByteOff++;  // 非中文字元 byte 位置累進 1
                }

                if (_iNumBytes >= length) break;  // 已達欲取長度, 結束迴圈
            } // end for
            _sRet = sb.toString();
        } catch (Exception e) {
            // System.out.println(e.getMessage());
        } finally {
            
        } // end try
        return _sRet;
    }

    public static String subStringB(String s, int beginIndex) {
        return subStringB(s, beginIndex, (lengthB(s) - beginIndex));
    }

    public static String leftB(String s, int length) {
        return subStringB(s, 0, length);
    }

    public static String rightB(String s, int length) {
        if (s == null) s = "";
        StringBuffer sb = new StringBuffer(s);
        String sTemp = subStringB(new String(sb.reverse()), 0, length);
        sb = new StringBuffer(sTemp);
        return new String(sb.reverse());
    }

    public static String replicate(char c, int length) {
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++)
            sb.append(c);

        return sb.toString();
    }

    public static String replicate(String s, int length) {
        return replicate(s.charAt(0), length);
    }

    public static String space(int length) {
        return replicate(' ', length);
    }

    public String lPadB(int length) {
        return lPadB(sStr_, length, ' ');
    }

    public static String lPadB(String s, int length) {
        return lPadB(s, length, ' ');
    }

    /**
     * 以傳入長度來填左方字元.
     * @param s
     * @param length
     * @param cPad; 要填入的字元, 預設是空白
     * @return 填完後之字串
     */
    public static String lPadB(String s, int length, char cPad) {
        if (s == null) s = "";
        String _sRet = "";
        int _n = length - lengthB(s);

        if (_n <= 0) {
            _sRet = subStringB(s, 0, length);
        } else {
            _sRet = replicate(cPad, _n) + s;
        }
        return _sRet;
    }

    public String rPadB(int length) {
        return rPadB(sStr_, length, ' ');
    }

    public static String rPadB(String s, int length) {
        return rPadB(s, length, ' ');
    }

    /**
     * 以傳入長度來填右方字元.
     * @param s
     * @param length
     * @param cPad; 要填入的字元, 預設是空白
     * @return 填完後之字串
     */
    public static String rPadB(String s, int length, char cPad) {
        if (s == null) s = "";
        String _sRet = "";
        int _n = length - lengthB(s);

        if (_n <= 0) {
            _sRet = subStringB(s, 0, length);
        } else {
            _sRet = s + replicate(cPad, _n);
        }
        return _sRet;
    }

    public String cPadB(int length) {
        return cPadB(sStr_, length);
    }

    public static String cPadB(String s, int length) {
        return cPadB(s, length, ' ');
    }

    /**
     * 以傳入長度來填中間字元.
     * @param s
     * @param length
     * @param cPad; 要填入的字元, 預設是空白
     * @return 填完後之字串
     */
    public static String cPadB(String s, int length, char cPad) {
        if (s == null) s = "";
        String _sRet = "";
        int _n = length - lengthB(s);

        if (_n <= 0) {
            _sRet = subStringB(s, 0, length);
        } else {
            int _mod = _n % 2;
            _n = (int) _n / 2;
            _sRet = replicate(cPad, _n) + s + replicate(cPad, _n) + ((_mod == 1) ? " " : "");
        }
        return _sRet;
    }

    public static String lTrim(String s) {
        if (s == null) s = "";
        String _sRet = "";
        int _iCharOff = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '　'
                    && s.charAt(i) != '\012' && s.charAt(i) != '\015'
                    && s.charAt(i) != '\011')
                break;
            _iCharOff++;
        }
        _sRet = s.substring(_iCharOff);
        return _sRet;
    }

    public static String rTrim(String s) {
        if (s == null) s = "";
        String _sRet = "";
        //   String _sSpace = " 　";
        int _iCharOff = s.length();

        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '　'
                    && s.charAt(i) != '\012' && s.charAt(i) != '\015'
                    && s.charAt(i) != '\011')
                break;
            _iCharOff--;
        }
        _sRet = s.substring(0, _iCharOff);
        return _sRet;
    }

    public static String trim(String s) {
        return lTrim(rTrim(s));
    }

    public static int lengthB(String s) {
    	//Track+[14291] wing 20100202 字符串取长修正
    	return emisUTF8StringUtil.checkLength(s);
        //return s.getBytes().length;
    }
}
