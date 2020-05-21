package com.emis.business;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.user.emisUser;
import com.emis.util.emisLangRes;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;

/**
 * Track+[15095] sunny.zhuang 2010/06/30 增加"我的工作清單"
 * 2013/07/24 Joe 1. 修正非IE下储存后画面会刷新的问题 2. 增加【N】解决中文时查不到数据之Bug
 *
 */
public class emisGetPreQuery {
  public emisGetPreQuery(ServletContext app,String key,String userid) {
    context = app;
    this.key = key;
    this.userid = userid;
    colspan = 2;
  }

  protected ResultSet getResultSet() throws Exception {
    db = emisDb.getInstance(context);
    statPreQuery = db.prepareStmt(selectCmd);
    statPreQuery.clearParameters();
    statPreQuery.setString(1, key);
    statPreQuery.setString(2, userid);
    return statPreQuery.executeQuery();
  }

  //for javascript
  public String proc() throws Exception {
    StringBuffer result = new StringBuffer();
    try {
      ResultSet rs = getResultSet();
      while (rs.next()) {
        result.append(";").append(rs.getString("NAME")).append("|").append(rs.getString("VALUE").replaceFirst("\\{\\{.*\\}\\}#?", ""));
      }
      int first = result.indexOf(";");
      if (first > -1) {
        result.deleteCharAt(first);
      }
    } finally {
      close();
    }
    return result.toString();
  }

  /**
   * 動態日期
   * */
  public String[] parseDate(String d) {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    String today = format.format(calendar.getTime());

    if("yesterday".equals(d)) {
      calendar.add(Calendar.DATE,-1);
      today = format.format(calendar.getTime());
    }
    else if("threeDay".equals(d))
      calendar.add(Calendar.DATE,-3);
    else if("week".equals(d)){
      calendar.add(Calendar.DATE,-(calendar.get(Calendar.DAY_OF_WEEK)-1));

      Calendar c = Calendar.getInstance();
      c.setTime(calendar.getTime());
      c.add(Calendar.DATE,6);
      today = format.format(c.getTime());
    }
    else if("lastWeek".equals(d)){
      calendar.add(Calendar.DATE,-(calendar.get(Calendar.DAY_OF_WEEK)-1));
      calendar.add(Calendar.DATE,-7);

      Calendar c = Calendar.getInstance();
      c.setTime(calendar.getTime());
      c.add(Calendar.DATE,6);
      today = format.format(c.getTime());
    }
    else if("month".equals(d)) {
      calendar.set(Calendar.DATE,1);

      Calendar c = Calendar.getInstance();
      c.add(Calendar.MONTH,1);
      c.set(Calendar.DATE,0);
      today = format.format(c.getTime());
    }
    else if("lastMonth".equals(d)){
      calendar.add(Calendar.MONTH,-1);
      calendar.set(Calendar.DATE,1);

      Calendar c = Calendar.getInstance();
      c.set(Calendar.DATE,0);
      today = format.format(c.getTime());
    }
    else if("threeMonth".equals(d)){
      calendar.add(Calendar.MONTH,-3);
      calendar.set(Calendar.DATE,1);

      Calendar c = Calendar.getInstance();
      c.set(Calendar.DATE,0);
      today = format.format(c.getTime());
    }
    else if("ym_month".equals(d)) {
      String[] ym = parseDate("month");
      return new String[]{ym[0].substring(0,7),ym[1].substring(0,7)};
    }
    else if("ym_lastMonth".equals(d)){
      String[] ym = parseDate("lastMonth");
      return new String[]{ym[0].substring(0,7),ym[1].substring(0,7)};
    }
    else if("ym_threeMonth".equals(d)){
      String[] ym = parseDate("threeMonth");
      return new String[]{ym[0].substring(0,7),ym[1].substring(0,7)};
    }else if("ym_thisYear".equals(d)){
      String year = today.substring(0,4);
      return new String[]{year + "/01", year + "/12"};
    }else if("ym_lastYear".equals(d)){
      int year = Integer.parseInt(today.substring(0,4)) - 1;
      return new String[]{year + "/01", year + "/12"};
    }else if("y_thisYear".equals(d)){
      String year = today.substring(0,4);
      return new String[]{year, year};
    }else if("y_lastYear".equals(d)){
      int year = Integer.parseInt(today.substring(0,4)) - 1;
      return new String[]{year + "", year + ""};
    }

    return new String[]{format.format(calendar.getTime()),today};
  }

  public static void main(String[] args) {
    //emisGetPreQuery pre = new emisGetPreQuery(null,null,null);
    //System.out.println("pre = " + pre.parseDate("ym_threeMonth")[0]);
    //System.out.println("pre = " + pre.parseDate("ym_threeMonth")[1]);
  }
  //for java
  public HashMap getParams(){
    return getParams(null);
  }

  public HashMap getParams(String preQueryName){
    HashMap<String,String> params = new HashMap<String,String>();
    //System.out.println("preQueryName = " + preQueryName);
    // 2013/07/24 Joe 增加【N】解决中文时查不到数据之Bug
    if(preQueryName != null && !"".equals(preQueryName))
      selectCmd += " and replace(name,'#','') = N'" + preQueryName + "'";
    else
      selectCmd += " and name = replace(name,'#','') + '#'"; //defalut;

    try {
      ResultSet rs = getResultSet();
      if(rs.next()) {
        params.put("L_QRYNUM","100"); //默認前100條數據
        String value = rs.getString("VALUE").replaceAll("\\{{2}#?|\\}{2}#?","");
        String[] ps = value.split("#");
        int len =  ps.length;
        //System.out.println("=====================");
        for (int i = 0; i < len; i++) {
          String[] nv = ps[i].split("=");
          if(nv.length < 2) continue; //有名字沒有值
          //System.out.println((!"".equals(nv[0]) && !"".equals(nv[1]))+ " nv " + nv[0] + "=" + nv[1]);
          if(!"".equals(nv[0]) && !"".equals(nv[1])){
            if(nv[0].indexOf('~') < 0){
              params.put(nv[0],nv[1].replace('^','='));
            } else {
              String[] date = nv[0].split("~");//日期字段:QRYPO_DATE1~QRYPO_DATE2
              params.put(date[0],parseDate(nv[1])[0]);
              params.put(date[1],parseDate(nv[1])[1]);
            }
          }
        }
        //System.out.println("=====================");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      close();
    }
    return params;
  }

  //更新我的工作顯示的數量
  public int updPreQueryCount(String name, String count) {
    if(name == null || count == null || "".equals(name) || "".equals(count) || !count.matches("\\d+")) return -1;
    //System.out.println(count + " !count.matches(\"\\\\d+\") = " + !count.matches("\\d+"));
    String updateCommand = "update Pre_Query set lastcount = "+count+" where keys = '"+this.key+"' and userid = '"+this.userid+"' and replace(name,'#','') = '" + name + "'";
    try {
      //System.out.println("updateCommand = " + updateCommand);
      db = emisDb.getInstance(context);
      return db.executeUpdate(updateCommand);
    } catch (Exception e) {
      e.printStackTrace();
    } finally{
      if (db != null)
        db.close();
    }
    return -1;
  }

  /**
   * 生成預設查詢用的兩個按鈕(tr)
   * (新增/修改 | 刪除)
   * @return String
   * */
  public static String outPutButton(){
    if(! isEnablePreQuery()) return "";
    return
      "<tr class=\"PreQuery\">\n" +
      "  <td colspan='"+colspan+"' align=\"center\" class='表格_奇數列'>\n" +
      "    <button id='btnUpdPreQuery' title='儲存預設查詢' class='OKButton'>\n" +
      "      <img src='../../images/update.gif'></img> 儲&nbsp;&nbsp;&nbsp;&nbsp;存\n" +
      "    </button>&nbsp;&nbsp;\n" +
      "    <button id='btnDelPreQuery' title='刪除預設查詢' class='ExitButton'>\n" +
      "      <img src='../../images/delete.gif'></img> 刪&nbsp;&nbsp;&nbsp;&nbsp;除 \n" +
      "    </button>\n" +
      "    &nbsp;&nbsp; <button onclick='window.close();' accesskey='C' title='取消:[Esc]' class='ExitButton'>\n" +
      "     <img src='../../images/cancel.gif'></img> &nbsp;&nbsp;取消(<u>C</u>)\n" +
      "     </button>" +
      "  </td>\n" +
      "</tr>";
  }

  /**
   * 生成預設查詢用的兩個按鈕(tr)
   * (新增/修改 | 刪除)
   * @return String
   * */
  public static String outPutAjaxButton(HttpServletRequest request){
    if(! isEnablePreQuery()) return "";
    // 2013/07/24 Joe 修正非IE下储存后画面会刷新的问题
    return
      "<tr class='PreQuery'>\n" +
      "  <td colspan='"+colspan+"' align='center' class='functions'>\n" +
      "    <button type='button' id='btnUpdPreQuery' title='"+ getMessage(request, "prequery", "BTNS_OK_HINT") + "' class='OKButton'>\n" +
      "      <img src='../../images/update.gif'></img>"+ getMessage(request, "prequery", "BTNS_OK_TXT") + "\n" +
      "    </button>&nbsp;&nbsp;\n" +
      "    <button type='button' id='btnDelPreQuery' title='"+ getMessage(request, "prequery", "BTNS_DEL_HINT") + "' class='ExitButton'>\n" +
      "      <img src='../../images/delete.gif'></img>"+ getMessage(request, "prequery", "BTNS_DEL_TXT") + "\n" +
      "    </button>&nbsp;&nbsp;\n" +
      "    <button type='button' onclick='window.close();' accesskey='C' title='"+ getMessage(request, "prequery", "BTNS_CANCEL_HINT") + "' class='ExitButton'>\n" +
      "     <img src='../../images/cancel.gif'></img>"+ getMessage(request, "prequery", "BTNS_CANCEL_TXT") + "(<u>C</u>)\n" +
      "    </button>" +
      "  </td>\n" +
      "</tr>";
  }
  /**
   * 生成動態日期下拉框(select)
   * @param dateStart:
   * @param dateEnd:
   * @return String
   * */
  public static String outPutAjaxDate(HttpServletRequest request, String dateStart,String dateEnd){
    return "";
    //不再通过该方法处理，由<showdata:date>处理。
    /*
      "<select style='display:inline;' name=\"selDateScope\" dateStart='"+dateStart+"' dateEnd='"+dateEnd+"' onchange='selDateScopeChange(this)'>\n" +
      "  <option value=\"\"></option>\n" +
      "  <option value=\"today\">1 "+getMessage(request, "prequery", "D_TODAY")+"</option>\n" +
      "  <option value=\"yesterday\">2 "+getMessage(request, "prequery", "D_YESTERDAY")+"</option>\n" +
      "  <option value=\"threeDay\">3 "+getMessage(request, "prequery", "D_THREEDAY")+"</option>\n" +
      "  <option value=\"week\">4 "+getMessage(request, "prequery", "D_WEEK")+"</option>\n" +
      "  <option value=\"lastWeek\">5 "+getMessage(request, "prequery", "D_LASTWEEK")+"</option>\n" +
      "  <option value=\"month\">6 "+getMessage(request, "prequery", "D_MONTH")+"</option>\n" +
      "  <option value=\"lastMonth\">7 "+getMessage(request, "prequery", "D_LASTMONTH")+"</option>\n" +
      "  <option value=\"threeMonth\">8 "+getMessage(request, "prequery", "D_THREEMONTH")+"</option>\n" +
      "</select>";
      */
  }
  /**
   * 生成動態日期下拉框(select)
   * @param dateStart:
   * @param dateEnd:
   * @return String
   * */
  public static String outPutDate(String dateStart,String dateEnd){
    return
      "<select style='display:inline;' name=\"selDateScope\" dateStart='"+dateStart+"' dateEnd='"+dateEnd+"' onchange='selDateScopeChange(this)'>\n" +
      "  <option value=\"\"></option>\n" +
      "  <option value=\"today\">1 當天</option>\n" +
      "  <option value=\"yesterday\">2 昨天</option>\n" +
      "  <option value=\"threeDay\">3 最近三天</option>\n" +
      "  <option value=\"week\">4 本周</option>\n" +
      "  <option value=\"lastWeek\">5 上周</option>\n" +
      "  <option value=\"month\">6 本月</option>\n" +
      "  <option value=\"lastMonth\">7 上月</option>\n" +
      " <option value=\"threeMonth\">8 前三個月</option>\n" +
      "</select>";
  }

  /**
   * 生成動態年月下拉框(select)
   *
   * @param dateStart:
   * @param dateEnd:
   * @return String
   */
  public static String outPutAjaxYearMonth(HttpServletRequest request, String dateStart, String dateEnd) {
    return
      "<select style='display:inline;' name=\"selDateScope\" dateStart='"+dateStart+"' dateEnd='"+dateEnd+"' onchange='selDateScopeChange(this)'>\n" +
      "  <option value=\"\"></option>\n" +
      "  <option value=\"ym_month\">1 "+getMessage(request, "prequery", "YM_MONTH")+"</option>\n" +
      "  <option value=\"ym_lastMonth\">2 "+getMessage(request, "prequery", "YM_LASTMONTH")+"</option>\n" +
      "  <option value=\"ym_threeMonth\">3 "+getMessage(request, "prequery", "YM_THREEMONTH")+"</option>\n" +
      "</select>";
  }
  /**
   * 生成動態年月下拉框(select)
   *
   * @param dateStart:
   * @param dateEnd:
   * @return String
   */
  public static String outPutYearMonth(String dateStart, String dateEnd) {
    return
      "<select style='display:inline;' name=\"selDateScope\" dateStart='"+dateStart+"' dateEnd='"+dateEnd+"' onchange='selDateScopeChange(this)'>\n" +
      "  <option value=\"\"></option>\n" +
      "  <option value=\"ym_month\">1 本月</option>\n" +
      "  <option value=\"ym_lastMonth\">2 上月</option>\n" +
      "  <option value=\"ym_threeMonth\">3 前三個月</option>\n" +
      "</select>";
  }

  /**
   * 生成預設查詢名稱下拉框(tr)
   * 通常放在第一行
   * @return String
   * */
  public static String outPutPreQuery(){
    if(! isEnablePreQuery()) return "";
    return
      "<tr >\n" +
      "  <td class=\"表格_欄_文字\">預設查詢選擇</td>\n" +
      "  <td class=\"表格_欄_資料\" colspan='"+colspan+"'>\n" +
      "    <select name=\"selPreQueryName\">\n" +
      "      <option value=\"\"></option>\n" +
      "    </select>\n" +
      "    <span class='PreQuery'><input type='checkbox' name='chkDefaultPreQuery' title='會自動清空已有的默認查詢'>設為默認查詢</span>\n" +
      "  </td>\n" +
      "</tr>" +
      "<tr class='PreQuery'>\n" +
      "  <td class=\"表格_欄_文字\" width=\"20%\">預設查詢名稱</td>\n" +
      "  <td class=\"表格_欄_資料\" width=\"80%\" colspan='"+colspan+"'>\n" +
      "    <input type='text' id='PreQueryName' size='30'  maxlength='50' /> <font class='PreQuery' color='red'>*</font>\n" +
      "  </td>\n" +
      "</tr>" +
      "<tr class=\"PreQuery\">\n" +
      "  <td class=\"表格_欄_文字\">我的工作設定</td>\n" +
      "  <td class=\"表格_欄_資料\" colspan='"+colspan+"'>\n" +
      "    <img style=\"margin-left:5px;\" src=\"../../images/job.png\" alt=\"加入我的工作\"> 加入我的工作:<input type='checkbox' name='isMyJob'/><br>\n" +
      "    <img style=\"margin-left:5px;\" src=\"../../images/sound.png\" alt=\"提示方式\"><span id=\"spnShowType\">&nbsp;&nbsp;選擇提示方式: </span>\n" +
      "    <input type=\"radio\" name=\"showType\" value=\"1\" disabled=\"true\">始終提示\n" +
      "    <input type=\"radio\" name=\"showType\" checked=\"true\" value=\"2\" disabled=\"true\">有數據顯示\n" +
      "    <input type=\"radio\" name=\"showType\" value=\"3\" disabled=\"true\">無數據顯示<br>\n" +

      "    <img style=\"margin-left:5px;\" src=\"../../images/showLevel.png\" alt=\"提示圖標\"><span id=\"spnShowLevel\">&nbsp;&nbsp;選擇提示圖標: </span>\n" +
      "    <input type=\"radio\" name=\"showLevel\" checked=\"true\" value=\"1\" disabled=\"true\">普通<img style=\"margin-left:5px;\" src=\"../../images/natural.png\" alt=\"普通\">\n" +
      "    <input type=\"radio\" name=\"showLevel\"  value=\"2\" disabled=\"true\">警告<img style=\"margin-left:5px;\" src=\"../../images/alert.png\" alt=\"警告\">\n" +
      "    <input type=\"radio\" name=\"showLevel\" value=\"3\" disabled=\"true\">嚴重<img style=\"margin-left:5px;\" src=\"../../images/error.png\" alt=\"嚴重\">" +
      "  </td>\n" +
      "</tr>" +
      "<tr class=\"PreQuery\">\n" +
      "  <td colspan='"+(colspan+2)+"'>\n" +
      "  </td>\n" +
      "</tr>";
  }

  /**
   * 生成預設查詢名稱下拉框(tr)
   * 通常放在第一行
   * @return String
   * */
  public static String outPutAjaxPreQuery(HttpServletRequest request){
    if(! isEnablePreQuery()) return "";
    return   
      "<tr >\n" +
      "  <th>"+ getMessage(request, "prequery", "CONT_DEF_QRY") + "</th>\n" +  // 當前默認查詢名稱
      "  <td colspan='"+(colspan-1)+"'>\n" +
      "    <div id='spnDefaultName' style='color:green;'></div>\n" +
      "  </td>\n" +
      "</tr>" +

      "<tr >\n" +
      "  <th>"+ getMessage(request, "prequery", "CONT_PRE_QRY") + "</th>\n" +     // 預設查詢選擇
      "  <td colspan='"+(colspan-1)+"'>\n" +
      "    <select id='selPreQueryName' name='selPreQueryName'>\n" +
      "      <option value=''></option>\n" +
      "    </select>\n" +
      "    <span class='PreQuery'><input type='checkbox' name='chkDefaultPreQuery' " +
      "       title='"+ getMessage(request, "prequery", "CONT_PRE_SET_HINT") + "'>"+ getMessage(request, "prequery", "CONT_PRE_SET_DEF") + "</span>\n" +
      "  </td>\n" +
      "</tr>" +

      "<tr class='PreQuery'>\n" +
      "  <th>"+ getMessage(request, "prequery", "CONT_PRE_NAME") + "</th>\n" +     // 預設查詢名稱
      "  <td colspan='"+(colspan-1)+"'>\n" +
      "    <input type='text' id='PreQueryName' size='30'  maxlength='50' /> <font class='PreQuery' color='red'>*</font>\n" +
      "  </td>\n" +
      "</tr>" +
      "<tr class='PreQuery'>\n" +
      "  <th>"+ getMessage(request, "prequery", "CONT_SET_MYWORK") + "</th>\n" +     // 我的工作設定
      "  <td colspan='"+(colspan-1)+"'>\n" +
      "    <img style='margin-left:5px;' src='../../images/job.png'" +
          " alt='"+ getMessage(request, "prequery", "CONT_ADD_MYWORK") + "'><input type='checkbox' name='isMyJob'/>"+
          getMessage(request, "prequery", "CONT_ADD_MYWORK") + "<br>\n" + // 加入我的工作
      "    <img style='margin-left:5px;' src='../../images/sound.png'" +
          " alt='"+ getMessage(request, "prequery", "CONT_HINT_TYPE") + "'><span id='spnShowType'>&nbsp;&nbsp;"+
          getMessage(request, "prequery", "CONT_HINT_TYPE") + ": </span>"+ // 選擇提示方式
      "    <input type='radio' name='showType' value='1' disabled='true'>" +
          getMessage(request, "prequery", "CONT_HINT_ALL") + "\n"+ // 始終提示
      "    <input type='radio' name='showType' checked='true' value='2' disabled='true'>" +
          getMessage(request, "prequery", "CONT_HINT_HASDATA") + "\n"+ // 有數據顯示
      "    <input type='radio' name='showType' value='3' disabled='true'>" +
          getMessage(request, "prequery", "CONT_HINT_NODATA") + "<br>\n"+ // 無數據顯示
      "    <img style='margin-left:5px;' src='../../images/showLevel.png' alt='"+
          " alt='"+ getMessage(request, "prequery", "CONT_SHOW_LEVEL") + "'><span id='spnShowLevel'>&nbsp;&nbsp;"+
          getMessage(request, "prequery", "CONT_SHOW_LEVEL") + ": </span>\n"+ // 選擇提示圖標
      "    <input type='radio' name='showLevel' checked='true' value='1' disabled='true'>" +
          getMessage(request, "prequery", "CONT_ICON_NORMAL")+"<img style='margin-left:5px;' src='../../images/natural.png'" +
          " alt='"+getMessage(request, "prequery", "CONT_ICON_NORMAL")+"'>\n" + // 普通
      "    <input type='radio' name='showLevel'  value='2' disabled='true'>"+
          getMessage(request, "prequery", "CONT_ICON_WARN")+"<img style='margin-left:5px;' src='../../images/alert.png' " +
          "alt='"+getMessage(request, "prequery", "CONT_ICON_WARN")+"'>\n" + //警告
      "    <input type='radio' name='showLevel' value='3' disabled='true'>"+
          getMessage(request, "prequery", "CONT_ICON_SERIOUS")+"<img style='margin-left:5px;' src='../../images/error.png' alt='"+
          getMessage(request, "prequery", "CONT_ICON_SERIOUS")+"'>" +  // 嚴重
      "  </td>\n" +
      "</tr>" +
      "<tr style='height:1px'>\n" +
      "  <td colspan='"+colspan+"' style='height:1px'>\n" +
      "  </td>\n" +
      "</tr>";
  }

  /**
   * 生成頁簽(div)
   * 通常放在Table上面
   * @return String
   * */
  public static String outPutTabs(){
    if(! isEnablePreQuery()) return "";
    return
      "<div class=\"tabs\" style=\"background:''\" id='selOperateType'>\n" +
      "  <ul>\n" +
      "    <li><a href=\"#\" class=\"active\" hideFocus id='idPage1' pageNum=\"0\" title=\"查詢操作\"><span>&nbsp;查&nbsp;詢&nbsp;操&nbsp;作</span></a></li>\n" +
      "    <li><a href=\"#\" hideFocus id='idPage2' pageNum=\"1\" title=\"預設查詢設定\"><span>預設查詢設定</span></a></li>\n" +
      "    <li ><span id='spnDefaultName' style='color:green;margin: 0 5px;padding: 5px;'></span></li> \n"+
      "  </ul>\n" +
      "</div>";
  }
  /**
   * 生成頁簽(div)
   * 通常放在Table上面
   * @return String
   * */
  public static String outPutAjaxTabs(HttpServletRequest request){
    if(! isEnablePreQuery()) return "";
    return
        "<div id=\"selOperateType\" class=\"ajaxtabs\" style=\"width:95%\">" +            
        "  <a href=\"#tab1\" title='"+ getMessage(request,"prequery", "TABS_PAGE1") + "'>"+ getMessage(request,"prequery", "TABS_PAGE1") + "</a>" +
        "  <a href=\"#tab2\" title='"+ getMessage(request,"prequery", "TABS_PAGE2") + "'>"+ getMessage(request,"prequery", "TABS_PAGE2") + "</a>" +
        "</div>";
  }

  /**
   * 設置合併單元格
   * */
  public static void setColspan(int c){
    if( c > 0)
      colspan = c;
  }

  public static String getSQL(JspWriter out, HttpServletRequest request,String title,String preQueryName,emisUser _oUser){
    return getSQL(out,request,title,preQueryName,_oUser,false);
  }

  public static String getSQL(JspWriter out,HttpServletRequest request,String title,String preQueryName,emisUser _oUser,boolean force){
    if(preQueryName == null || "".equals(preQueryName)) return emisShowDataCount.DEFAULT_SQL;
    String sql = preQuerys.get(preQueryName);
    if(force || sql == null) {
      try {
        if (_oBusiness == null || !title.equals(oldTitle)) {
          //System.out.println("==========執行emisBusiness=============" + title + " oldTitle = " +oldTitle );
          oldTitle = title;
          _oBusiness = new emisBusinessImpl(title, request.getSession().getServletContext(), _oUser, title+".xml", false);
        }
        //System.out.println("showCount = " + preQueryName);
        //System.out.println("preQueryName = " + preQueryName);
        request.setAttribute("showCount",preQueryName);
        request.setAttribute("preQueryName",preQueryName);

        _oBusiness.setWriter(out);
        _oBusiness.setParameter(request);
        _oBusiness.setParameter("keys",title);
        _oBusiness.process("preQuery");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    //執行完 _oBusiness.process,emisShowDataCount會調用下面的 setSQL
    return preQuerys.get(preQueryName);
  }

  public static void setSQL(String preQueryName,String sql){
    if((preQueryName == null || "".equals(preQueryName))) return;
    preQuerys.put(preQueryName,sql);
  }

  protected void close() {
    try {
      if (statPreQuery != null)
        statPreQuery.close();
      if (db != null)
        db.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  //返回預設查詢前20條
  protected String selectCmd = "select * from Pre_Query where keys = ? and userid = ?";
  protected static ServletContext context = null;
  protected String key = ""; //title
  protected String userid = ""; //user
  protected PreparedStatement statPreQuery = null;
  protected emisDb db = null;
  protected static Map<String,String> preQuerys = new HashMap<String,String>();
  protected static emisBusiness _oBusiness = null;
  protected static String oldTitle = null;
  protected static int colspan = 2;

  protected static String enablePreQuery = null;

  protected static emisLangRes _oLang = null;  // 多语资源档

  public static boolean isEnablePreQuery() {
    try {
      if (enablePreQuery == null)
        enablePreQuery = emisProp.getInstance(context).get("preQuery", "N");
      return enablePreQuery.equalsIgnoreCase("Y");
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 取得指定多语内容
   * @param request
   * @param bundle
   * @param key
   * @return
   */
  public static String getMessage(HttpServletRequest request, String bundle, String key) {
    try {
      if (_oLang == null) { // 首次实例
        _oLang = emisLangRes.getInstance(request);
      } else { // 已实例后重设当前语系确保正确取得资源
        String languageType = (String) request.getSession().getAttribute("languageType");
        if (languageType != null && !"".equals(languageType.trim()))
          _oLang.setLanguage(languageType);
      }
    } catch (Exception e) {
    }

    // 无资源档时直接返回 Key值
    if (_oLang == null) return key;
    
    return _oLang.getMessage(bundle, key);
  }


  public  int getTotalQuery(String sTable){

    String sql = "select count(*) cnt from "+ sTable;
    int totalRowCount = 0;
    try {
      db = emisDb.getInstance(context);
      db.executeQuery(sql);
      if(db.next()){
        totalRowCount = db.getInt("cnt");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally{
      if (db != null)
        db.close();
    }
    return totalRowCount;
  }
}
