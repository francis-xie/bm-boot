package com.emis.app.migration;



import com.emis.app.migration.action.emisMiAction;



import java.text.DecimalFormat;

import java.util.ArrayList;

import java.util.Hashtable;

import java.util.Iterator;

import java.util.StringTokenizer;



/**

 * User: merlin

 * Date: Apr 23, 2003

 * Time: 6:53:00 PM

 */

public class emisMiField {

  private String value;

  private int key = -1;

  private int src;

  private int length;

  private boolean alignRight = false;

  private char padding = ' ';

  private int offset;

  private int seq;

  private DecimalFormat formatter = new DecimalFormat("#######0.00");

  private String type = "String";

  private String name;

  private String table;

  private emisMiAction action;

  private int[] actionParam1;

  private String[] actionParam2;

  private emisMiConfig config;

  public static int TRIM_DEFAULT = -1;

  public static int TRIM_FALSE = 0;

  public static int TRIM_TRUE = 1;

//    public static int TRIM_BOTH  = 2;

  private int trim = TRIM_DEFAULT;

  private boolean autoSize = false;


  private emisMiField() {

    this(0, 1);

  }



  emisMiField(int src, int length) {

    this.src = src;

    this.length = length;

  }



  public emisMiField(Hashtable h, emisMiConfig config) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    this.config = config;

    if (h != null)

      parse(h);

  }



  public void setType(String type) {

    this.type = type;

  }



  public void setSrc(int src) {

    this.src = src;

  }



  public int getTrim() {

    return trim;

  }



  void setTrimFlag(boolean needTrim) {

    if (needTrim)

      trim = TRIM_TRUE;

    else

      trim = TRIM_FALSE;

  }

//        return trim;

//    }

//

  public void parse(Hashtable h) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    Iterator it = h.keySet().iterator();

    String hashKey;

    String val;

    while (it.hasNext()) {

      hashKey = (String) it.next();

      val = (String) h.get(hashKey);

      if (hashKey.equalsIgnoreCase("length")) {

        length = Integer.parseInt(val);

      } else if (hashKey.equalsIgnoreCase("src")) {

        src = Integer.parseInt(val);

      } else if (hashKey.equalsIgnoreCase("seq")) {

        seq = Integer.parseInt(val);

      } else if (hashKey.equalsIgnoreCase("offset")) {

        offset = Integer.parseInt(val);

      } else if (hashKey.equalsIgnoreCase("padding")) {

        padding = val.charAt(0);

      } else if (hashKey.equalsIgnoreCase("format")) {

        formatter = new DecimalFormat(val);

        alignRight = true;

      } else if (hashKey.equalsIgnoreCase("type")) {

        type = val;

        if (val.equalsIgnoreCase("DATE")) {

          alignRight = true;

          padding = '0';

        }

      } else if (hashKey.equalsIgnoreCase("name")) {

        name = val;

      } else if (hashKey.equalsIgnoreCase("table")) {

        table = val;

      } else if (hashKey.equalsIgnoreCase("align")) {

        alignRight = (val.equalsIgnoreCase("right"));

      } else if (hashKey.equalsIgnoreCase("action")) {

        parseParams(val);

      } else if (hashKey.equalsIgnoreCase("key")) {

        key = Integer.parseInt(val);

      } else if (hashKey.equalsIgnoreCase("value")) {

        value = val;

      } else if (hashKey.equalsIgnoreCase("trim")) {

        if (val.equalsIgnoreCase("true"))

          trim = TRIM_TRUE;

        else if (val.equalsIgnoreCase("false"))

          trim = TRIM_FALSE;

//              if (value.equalsIgnoreCase("left"))

//                trim = TRIM_LEFT;

//              else if (value.equalsIgnoreCase("right")|| value.equalsIgnoreCase("true"))

//                trim = TRIM_RIGHT;

//              else if (value.equalsIgnoreCase("none") || value.equalsIgnoreCase("false"))

//                trim = TRIM_NONE;

//              else if (value.equalsIgnoreCase("both"))

//                trim = TRIM_BOTH;

//            }

      } else if (hashKey.equalsIgnoreCase("autosize")) {
        autoSize = (val.equalsIgnoreCase("true"));
      }

    }

  }



  private void parseParams(String val) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    int p1;

    p1 = val.indexOf("(");

    if (p1 >= 0) {

      String actionName = val.substring(0, p1);

      if (!actionName.startsWith("com.")) {

        actionName = "com.emis.app.migration.action." + actionName;

      }

      action = emisMiAction.getInstance(actionName, config);

      int p2 = val.indexOf(")");

      if (p2 >= p1) {

        actionParam1 = parseValues(val.substring(p1 + 1, p2));

        val = val.substring(p2 + 1);

        p1 = val.indexOf("(");

        if (p1 >= 0) {

          p2 = val.indexOf(")");

          if (p2 >= p1) {

            actionParam2 = parseStr(val.substring(p1 + 1, p2));

          }

        }

      }

    }

  }



  public static String[] parseStr(String s) {

    ArrayList ary = new ArrayList();

    for (StringTokenizer stk = new StringTokenizer(s, ","); stk.hasMoreTokens();) {

      String s1 = stk.nextToken();

      ary.add(s1);

    }

    if (ary.size() == 0)

      return null;

    else {

      String[] str = new String[ary.size()];

      for (int i = 0; i < str.length; i++) {

        str[i] = (String) ary.get(i);

      }

      return str;

    }

  }



  public static int[] parseValues(String s) {

    ArrayList ary = new ArrayList();

    for (StringTokenizer stk = new StringTokenizer(s, ","); stk.hasMoreTokens();) {

      String s1 = stk.nextToken();

      Integer iVal = Integer.decode(s1);

      ary.add(iVal);

    }

    if (ary.size() == 0)

      return null;

    else {

      int[] num = new int[ary.size()];

      for (int i = 0; i < num.length; i++) {

        num[i] = ((Integer) ary.get(i)).intValue();

      }

      return num;

    }

  }



  public Object clone() {

    emisMiField fld = new emisMiField();

    fld.src = src;

    fld.length = length;

    fld.alignRight = alignRight;

    fld.padding = padding;

    fld.offset = offset;

    fld.seq = seq;

    fld.type = type;

    fld.name = name;

    fld.table = table;

    fld.autoSize = autoSize;

    return fld;

  }



  int getSrc() {

    return src;

  }



  int getLength() {

    return length;

  }



  String getType() {

    return type;

  }



  boolean isAlignRight() {

    return alignRight;

  }


  boolean isAutoSize() {

    return autoSize;

  }



  char getPadding() {

    return padding;

  }



  String getName() {

    return name;

  }



  String getTable() {

    return table;

  }



  int getOffset() {

    return offset;

  }



  public int getSeq() {

    return seq;

  }



  public int getKey() {

    return key;

  }



  public emisMiAction getAction() {

    return action;

  }



  public int[] getActionParam1() {

    return actionParam1;

  }



  public String[] getActionParam2() {

    return actionParam2;

  }



  DecimalFormat getFormatter() {

    return formatter;

  }



  void setFormatter(DecimalFormat formatter) {

    this.formatter = formatter;

  }



  public String getValue() {

    return this.value;

  }



  public void setConfig(emisMiConfig config) {

    this.config = config;

  }

}