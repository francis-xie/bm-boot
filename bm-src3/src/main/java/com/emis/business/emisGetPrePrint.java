package com.emis.business;


import com.emis.db.emisDb;
import com.emis.db.emisProp;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;

/**
 * 報表作業增加預設功能
 * User: zhong.xu
 * Date: 2009-02-18
 */
public class emisGetPrePrint extends emisGetPreQuery {
  // 不写到PRE_PRINT.VALUE的栏位。
  public static final String NOT_INCLUDED_IN_THE_VALUE = "MAIL_TYPE,USGROUP,USERS,MAILADDRS,MAILSUBT,MAILCOTENT,END_DAY,GUID,RUNLEVEL,SYEAR,SMONTH,SDAY,SHOUR,STIME,INTERVAL";

  public emisGetPrePrint(ServletContext context,String key,String userid) {
    super(context,key,userid);
    super.selectCmd = this.selectCmd1;
  }

  public emisGetPrePrint(ServletContext context,String key,String userid,String  guid) {
    super(context,key,userid);
    this.guid= guid;
  }

  /**
   * 生成預設列印用的兩個按鈕(tr)
   * (新增/修改 | 刪除)
   * @return String
   * */
  public static String outPutButton(){
    if(! isEnablePreQuery()) return "";
    return
        "<tr class=\"PreQuery\">\n" +
            "  <td colspan='"+(colspan+2)+"' align=\"center\" class='表格_奇數列'>\n" +
            "    <button id='btnUpdPreQuery' title='保存預設列印' class='OKButton'>\n" +
            "      <img src='../../images/update.gif'></img> 保&nbsp;&nbsp;&nbsp;&nbsp;存\n" +
            "    </button>&nbsp;&nbsp;\n" +
            "    <button id='btnDelPreQuery' title='刪除預設列印' class='ExitButton'>\n" +
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
    String sColspan = "2";
    if(request.getAttribute("PRE_COLSPAN") != null && !"".equals(request.getAttribute("PRE_COLSPAN"))){
      sColspan = (String)request.getAttribute("PRE_COLSPAN");
    }
    return
        "<tr class='PreQuery'>\n" +
            "  <td colspan='"+sColspan+"' align='center' class='functions'>\n" +
            "    <button id='btnUpdPreQuery' type='button' title='"+ getMessage(request, "preprint", "BTNS_OK_HINT") + "' class='OKButton'>\n" +
            "      <img src='../../images/update.gif'></img>"+ getMessage(request, "preprint", "BTNS_OK_TXT") + "\n" +
            "    </button>&nbsp;&nbsp;\n" +
            "    <button id='btnDelPreQuery' type='button' title='"+ getMessage(request, "preprint", "BTNS_DEL_HINT") + "' class='ExitButton'>\n" +
            "      <img src='../../images/delete.gif'></img>"+ getMessage(request, "preprint", "BTNS_DEL_TXT") + "\n" +
            "    </button>&nbsp;&nbsp;\n" +
            "    <button onclick='window.close();' type='button' accesskey='C' title='"+ getMessage(request, "preprint", "BTNS_CANCEL_HINT") + "' class='ExitButton'>\n" +
            "     <img src='../../images/cancel.gif'></img>"+ getMessage(request, "preprint", "BTNS_CANCEL_TXT") + "(<u>C</u>)\n" +
            "    </button>" +
            "  </td>\n" +
            "</tr>";
  }

  /**
   * 生成預設查詢名稱下拉框(tr)
   * 通常放在第一行
   * @return String
   * */
  public static String outPutPreQuery(){
    if(! isEnablePreQuery()) return "";
    return
        "<tr>\n" +
            "  <td class='表格_栏_文字'>預設列印選擇</td>\n" +
            "  <td class='表格_栏_资料' colspan='0'>\n" +
            "    <select name='selPreQueryName'>\n" +
            "      <option value=''></option>\n" +
            "    </select>\n" +
            "  <span class='PreQuery'>\n" +
            "  <!--<input type='checkbox' name='chkDefaultPreQuery' title='會自動清空已有的默認查詢'>設為默認查詢-->\n" +
            "  <input type='checkbox' name='chkShowMenu' title='會在主頁右邊菜單顯示'>添加到菜單\n" +
            "  <span id='preMenuName' style='margin:0 10 0 5px;'> </span> <input type='text' name='txtMenuID' title='自定義菜單編號'size='5' style='display:inline;'> 自定義菜單編號 <font class='PreQuery' color='red'>*</font>\n" +
            "    <img alt=\"預設列印報表說明\"  src='../../images/righthelp.gif' style=\"cursor:pointer;margin-left:10px;\" id='righthelp'/>\n" +
            "  </span>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery'>\n" +
            "  <td class='表格_栏_文字' width='20%'>預設列印名稱</td>\n" +
            "  <td class='表格_栏_资料' width='80%' colspan='0'>\n" +
            "    <input type='text' id='PreQueryName' size='30' maxlength='50'/> <font class='PreQuery' color='red'>*</font>\n" +
            "    <!--<input type='checkbox' name='chkSingle_page' title='打印一頁式報表'>是否轉一頁式報表-->\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery'>\n" +
            "  <td class='表格_栏_文字' width='20%'>報表樣式</td>\n" +
            "  <td class='表格_栏_资料' width='80%' colspan='0'>\n" +
            "    <select name=\"EXCEL_SINGLE_PAGE\">\n" +
            "      <option value=\"\">Excel報表</option>\n" +
            "      <option value=\"true\">Excel一頁式報表</option>\n" +
            "    </select>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery'>\n" +
            "  <td class='表格_栏_文字'>定時自動生成報表</td>\n" +
            "  <td class='表格_栏_资料' colspan='0'>\n" +
            "    <img style='margin-left:5px;' src='../../images/job.png' alt='啟用訂時自動生成報表'>\n" +
            "    啟用:<input type='checkbox' name='chkRunTask'/>\n" +
            //  "    <a href=\"../sys/Download_PrePrint.jsp\" style=\"margin-left:20px;\" target=\"navTabFrame\" rel='menu'>下載已生成報表</a>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery' name='runTask'>\n" +
            "  <td class='表格_栏_文字'>設定時間</td>\n" +
            "  <td class='表格_栏_资料' colspan='0' id='setRunTask'>\n" +
            "    <select name='RUNLEVEL' onchange='runlevelChange(this)'>\n" +
            "      <!--<option value='I'>每隔</option>-->\n" +
            "      <option value='Y'>每年</option>\n" +
            "      <option value='M'>每月</option>\n" +
            "      <option value='D'>每日</option>\n" +
            "      <option value='S'>特定</option>\n" +
            "    </select>\n" +
            "    <input type='text' name='SYEAR' size='4' maxlength='4' onkeypress=\"checkRange('9')\" title=\"2000-2050\">年\n" +
            "    <input type=\"text\" name=\"SMONTH\" size='2' maxlength='2' onkeypress=\"checkRange('9')\" title=\"1-12\">月\n" +
            "    <input type='text' name='SDAY' size='2' maxlength='2' onkeypress=\"checkRange('9')\" title=\"1-31\">日\n" +
            "    <input type='text' name='SHOUR' size='2' maxlength='2' onkeypress=\"checkRange('9')\" title=\"1-24\">時\n" +
            "    <input type='text' name='STIME' size='2' maxlength='2' onkeypress=\"checkRange('9')\" title=\"1-60\">分\n" +
            "    <span id='everyDay' style=\"display:none;\">\n" +
            "      --\n" +
            "      <input type='text' name='SHOUR_END' size='2' maxlength='2' onkeypress=\"checkRange('9')\">至時\n" +
            "      <input type='text' name='STIME_END' size='2' maxlength='2' onkeypress=\"checkRange('9')\">至分\n" +
            "    </span>\n" +
            "    <input type='text'  style=\"display:none;\" name='INTERVAL' size='2' maxlength='2' onkeypress=\"checkRange('9')\" title=\"1-60\">秒\n" +
            "    <span style='color:green;'>二十四小時制</span>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery'>\n" +
            "  <td colspan='colspan=2'>\n" +
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
    String sColspan = "0";
    if(request.getAttribute("PRE_COLSPAN") != null && !"".equals(request.getAttribute("PRE_COLSPAN"))){
      sColspan = (String)request.getAttribute("PRE_COLSPAN");
    }
    return
        "<thead>\n<tr>\n" +
            "  <th>"+ getMessage(request, "preprint", "CONT_PRE_QRY") +"</th>\n" +
            "  <td colspan='"+sColspan+"'>\n" +
            "    <select name='selPreQueryName' id='selPreQueryName'>\n" +
            "      <option value=''></option>\n" +
            "    </select>&nbsp;&nbsp;\n" +
            "    <button type='button' id='btnCopy' name='btnCopy' title='"+getMessage(request, "preprint", "BTNS_COPY_TXT")+"' style='cursor:hand;display:none' class='small'> \n" +
            "      <img src='../../images/copy.gif'></img> \n" +
            "    </button>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery'>\n" +
            "  <th width='20%'>"+getMessage(request,"preprint","CONT_PRE_NAME")+"</th>\n" +
            "  <td width='80%' colspan='"+sColspan+"'>\n" +
            "    <input type='text' id='PreQueryName' size='30' maxlength='50'/><span> <font  color='red'>*</font></span>&nbsp;\n" +
            "  <span >\n" +
            "  <input type='checkbox' id='chkShowMenu' title='"+getMessage(request,"preprint","SHOWINLEFT")+"' style='display:none'>&nbsp;<span id='spaShowMenu' style='display:none'>"+getMessage(request,"preprint","ADDMENU")+"</span>\n" +
            //"  <span id='preMenuName' style='margin:0 2 0 30px;'> </span> " +
            "  <input type='text' name='txtMenuID' disabled='true' title='"+getMessage(request,"preprint","USERDEFINE")+"'size='5' maxlength='5' style='display:none;'> "+
            //getMessage(request,"preprint","USERDEFINE")+
            //" <font color='red'>*</font>\n" +
            //"    <img alt='預設列印報表說明'  src='../../images/righthelp.gif' style='cursor:pointer;margin-left:10px;' id='righthelp'/>\n" +
            "  </span>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "</thead>\n";
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
            "    <li><a href=\"#\" class=\"active\" hideFocus id='idPage1' pageNum=\"0\" title=\"列印操作\"><span>&nbsp;列&nbsp;印&nbsp;操&nbsp;作</span></a></li>\n" +
            "    <li><a href=\"#\" hideFocus id='idPage2' pageNum=\"1\" title=\"預設列印設定\"><span>預設列印設定</span></a></li>\n" +
            "    <li ><span id='spnDefaultName' style='color:green;margin: 0 5px;padding: 5px;'></span></li> \n"+
            "  </ul>\n" +
            "</div>";
  }
  /**
   * 生成預設查詢第二個页签下的全部內容(tr)
   * 通常放在第二個頁簽下面
   * @return String
   * */

  /**
   * 生成頁簽(div)
   * 通常放在Table上面
   * @return String
   * */
  public static String outPutAjaxTabs(HttpServletRequest request){
    if(! isEnablePreQuery()) return "";
    return
        "<div id='selOperateType' class='ajaxtabs' style='width:95%'>" +
            "  <a href='#tab1' title='"+ getMessage(request,"preprint", "TABS_PAGE1") + "'>"+ getMessage(request,"preprint", "TABS_PAGE1") + "</a>" +
            "  <a href='#tab2' title='"+ getMessage(request,"preprint", "TABS_PAGE2") + "'>"+ getMessage(request,"preprint", "TABS_PAGE2") + "</a>" +
            "</div>";
  }
  /*
  * 生成頁簽2(div)
  * 通常放在 【生成預設查詢名稱下拉框】(tr)下面
  * */
  public static String outPutAjaxSubTabs(HttpServletRequest request) {
    if (!isEnablePreQuery()) return "";
    int iColspan = 2;
    if(request.getAttribute("PRE_COLSPAN") != null && !"".equals(request.getAttribute("PRE_COLSPAN"))){
      iColspan = 1 + Integer.parseInt((String)request.getAttribute("PRE_COLSPAN"));
    }
    return
        "<tbody><tr class='PreQuery'>\n" +
            "  <td colspan='"+iColspan+"' style='padding-left:1px;'>\n" +
            "    <div id='selOperateType2' class='ajaxtabs' style='width:100%'>" +
            "       <a href='#tab3' title='" + getMessage(request, "preprint", "TABS_PAGE3") + "'>" + getMessage(request, "preprint", "TABS_PAGE3") + "</a>" +
            "       <a href='#tab4' style='display:none' title='" + getMessage(request, "preprint", "TABS_PAGE4") + "'>" + getMessage(request, "preprint", "TABS_PAGE4") + "</a>" +
            "       <a href='#tab5' style='display:none' title='" + getMessage(request, "preprint", "TABS_PAGE5") + "'>" + getMessage(request, "preprint", "TABS_PAGE5") + "</a>" +
            "       <a href='#tab6' style='display:none' title='" + getMessage(request, "preprint", "TABS_PAGE6") + "'>" + getMessage(request, "preprint", "TABS_PAGE6") + "</a>" +
            "    </div>" +
            " </td>" +
            "</tr></tobdy>" +
            "<tbody id='tabsPage2'>" +
            "<tr class='PreQuery'>\n" +
            "  <th>" + getMessage(request, "preprint", "AUTOREPORT") + "</th>\n" +
            "  <td colspan='0'>\n" +
            "    <img style='margin-left:5px;' src='../../images/job.png' alt='" + getMessage(request, "preprint", "USEAUTOEPORT") + "'>\n" +
            "    " + getMessage(request, "preprint", "USE") + ":<input type='checkbox' id='chkRunTask'/>\n" +
            // "    <a href='../sys/Download_PrePrint.jsp' style='margin-left:20px;' target='navTabFrame' rel='menu'>" + getMessage(request, "preprint", "DOWNREPORT") + "</a>\n" +
            "    <input type='hidden' name='GUID'/>" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery' name='trReportStyle'>\n" +
            "  <th width='20%'>"+getMessage(request,"preprint","REPORTSTYLE")+"</th>\n" +
            "  <td width='80%' colspan='0'>\n" +
            "    <select name='RPT_FILE_TYPE'>\n" +
            "      <option value='xls'>"+getMessage(request,"preprint","REPORT_OPTION1")+"</option>\n" +
            "      <option value='pdf'>"+getMessage(request,"preprint","REPORT_OPTION2")+"</option>\n" +
            //"      <option value='doc'>"+getMessage(request,"preprint","REPORT_OPTION3")+"</option>\n" +
            "    </select>\n" +
            "  </td>\n" +
            "</tr>"+
            "<tr class='PreQuery' name='trRunTask'>\n" +
            "  <th>" + getMessage(request, "preprint", "SETTIME") + "</th>\n" +
            "  <td colspan='0' id='setRunTask'>\n" +
            "    <select name='RUNLEVEL' onchange='runlevelChange(this)'>\n" +
            "      <!--<option value='I'>" + getMessage(request, "preprint", "GE") + "</option>-->\n" +
            "      <option value='Y'>" + getMessage(request, "preprint", "YEAR") + "</option>\n" +
            "      <option value='M'>" + getMessage(request, "preprint", "MONTH") + "</option>\n" +
            "      <option value='D'>" + getMessage(request, "preprint", "DAY") + "</option>\n" +
            "      <option value='S'>" + getMessage(request, "preprint", "TE") + "</option>\n" +
            "    </select>\n" +
            "    <input type='text' name='SYEAR' size='4' maxlength='4' onkeypress='checkRange(9)' title='2000-2050'><span>" + getMessage(request, "preprint", "SYEAR") + "</span>\n" +
            "    <input type='text' name='SMONTH' size='2' maxlength='2' onkeypress='checkRange(9)' title='1-12'><span>" + getMessage(request, "preprint", "SMONTH") + "</span>\n" +
            "    <input type='text' name='SDAY' size='2' maxlength='2' onkeypress='checkRange(9)' title='1-31'><span>" + getMessage(request, "preprint", "SDAY") + "</span>\n" +
            "    <input type='text' name='SHOUR' size='2' maxlength='2' onkeypress='checkRange(9)' title='1-24'><span>" + getMessage(request, "preprint", "SHOUR") + "</span>\n" +
            "    <input type='text' name='STIME' size='2' maxlength='2' onkeypress='checkRange(9)' title='1-60'><span>" + getMessage(request, "preprint", "STIME") + "</span>\n" +
            "    <span id='everyDay' style='display:none;'>\n" +
            "      --\n" +
            "      <input type='text' name='SHOUR_END' size='2' maxlength='2' onkeypress='checkRange(9)'><span>" + getMessage(request, "preprint", "TOHOUR") + "</span>\n" +
            "      <input type='text' name='STIME_END' size='2' maxlength='2' onkeypress='checkRange(9)'><span>" + getMessage(request, "preprint", "TOTIME") + "</span>\n" +
            "    </span>\n" +
            "    <input type='text'  style='display:none;' name='INTERVAL' size='2' maxlength='2' onkeypress='checkRange(9)' title='1-60'>" +//getMessage(request,"preprint","SINTERVAL")+"\n" +
            "    <span style='color:green;'>" + getMessage(request, "preprint", "TAG") + "</span>\n" +
            "  </td>\n"+
            "</tr>\n" +
            "<tr class='PreQuery' name='trEndDay'>" +
            "   <th>" + getMessage(request,"preprint","END_DAY") + "</th>" +
            "   <td colspan='0' id='setEndDay'>" +
            "      <input type='text' name='END_DAY' size='4' maxlength='4' onkeypress='checkRange(9)' title='1-999'>" +
            "   </td>" +
            "</tr>" +
            "</tbody>" +
            "<tbody id='tabsPage3'>" +
            "<tr class='PreQuery' name='userInfo'>\n" +
            "  <th>" + getMessage(request, "preprint", "USGROUP") + "</th>\n" +
            "  <td colspan='0' id='setUserGroup'>\n" +
            "    <input type='text' name='USGROUP' size='45' maxlength='200'/>" +
            "    <button id='selUsersGroups' type='button'>...</button>" +
            "    <img id='clearUSGROUP' src='../../images/images/icons/Item.Delete.gif' style='cursor:pointer' title='"+getMessage(request, "preprint", "CLEAR")+"'></img>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery' name='users'>\n" +
            "  <th>" + getMessage(request, "preprint", "USERS") + "</th>\n" +
            "  <td colspan='0' id='setUsers'>\n" +
            "    <input type='text' name='USERS' size='45' maxlength='200'/>" +
            "    <button id='selUsers' type='button'>...</button>" +
            "    <img id='clearUSERS' src='../../images/images/icons/Item.Delete.gif' style='cursor:pointer' title='"+getMessage(request, "preprint", "CLEAR")+"'></img>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery' name='mailAddress'>\n" +
            "  <th>" + getMessage(request, "preprint", "MAILADDRS") + "</th>\n" +
            "  <td colspan='0' id='setMailAddress'>\n" +
            "  <input type='text' name='MAILADDRS' size='45' maxlength='1000'>&nbsp;<span style='color:red'>(" + getMessage(request, "preprint", "MAILADDRS_SPAN") + ")</span>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery' name='explain'>\n" +
            "  <th>" + getMessage(request, "preprint", "EXPLAIN") + "</th>\n" +
            "  <td colspan='0'>\n" +
            "  <span style='color:red'>" + getMessage(request, "preprint", "EXPLAIN_SPAN") + "</span>\n" +
            "  </td>\n" +
            "</tr>\n" +
            "</tbody>" +
            "<tbody id='tabsPage4'>\n" +
            "<tr class='PreQuery' name='mailType'>\n" +
            "   <th>" + getMessage(request, "preprint", "MAILTYPE") + "</th>" +
            "   <td colspan='0'>\n" +
            "      <select name='MAIL_TYPE'>" +
            "         <option value='1'>" + getMessage(request, "preprint", "MAILTYPE_1") + "</option>" +
            "         <option value='2'>" + getMessage(request, "preprint", "MAILTYPE_2") + "</option>" +
            "      </select>" +
            "    " + getMessage(request, "preprint", "ISMAILFILE") + ":<input type='checkbox' id='chkIsMailFile' value=0>\n" +
            "    </td>" +
            "</tr>\n" +
            "<tr class='PreQuery' name='mailSubt'>\n" +
            "   <th>" + getMessage(request, "preprint", "MAILSUBT") + "</th>\n" +
            "   <td colspan='0'>\n" +
            "   <input type='text' name='MAILSUBT' size='20' maxlength='200'>\n" +
            "   </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery' name='mailCotent'>\n" +
            "   <th>" + getMessage(request, "preprint", "MAILCOTENT") + "</th>\n" +
            "   <td colspan='0'>\n" +
            "   <textarea name='MAILCOTENT' cols='30' rows='5'></textarea><br/>\n" +
            "   </td>\n" +
            "</tr>\n" +
            "<tr class='PreQuery' name='mailCotentFun'>" +
            "   <th>"+getMessage(request,"preprint","MAILCOTENTFUN")+"</th>"+
            "   <td colspan='0'>"+
            "   <span class='red'>"+ getMessage(request,"preprint","KEYWORDS_EX1")+"<br/>"+
            "      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+getMessage(request,"preprint","KEYWORDS_EX2")+
            "   </span>"+
            "</tr>"+
            "</tbody>\n";
  }

  protected ResultSet getResultSet() throws Exception {
    if("".equals(guid)){
      return super.getResultSet();
    }
    db = emisDb.getInstance(context);
    statPreQuery = db.prepareStmt(selectCmd);
    statPreQuery.clearParameters();
    //因為預設列印不再通過UserRights控制權限，故更改掉通過群組控制權限 Austen.liao 2013/06/26
//    statPreQuery.setString(1, userid);
    statPreQuery.setString(1, guid);
//    statPreQuery.setString(3, userid);
    return statPreQuery.executeQuery();
  }

  protected String selectCmd1 = "select p.*,\n" +
      " isnull(sc.RUNLEVEL,'') as RUNLEVEL, isnull(sc.SYEAR,'') as SYEAR, isnull(sc.SMONTH,'') as SMONTH, isnull(sc.SDAY,'') as SDAY,\n" +
      " isnull(sc.SHOUR,'') as SHOUR, isnull(sc.STIME,'') as STIME, isnull(sc.INTERVAL,'') as INTERVAL \n" +
      "from Pre_Print p left join SCHED sc on sc.S_NAME = p.GUID \n" +
      "where keys = ? and userid = ?";

  //因為預設列印不再通過UserRights控制權限，故更改掉通過群組控制權限 Austen.liao 2013/06/26
  protected String selectCmd =
      "select top 1 p.*, \n" +
      " isnull(sc.RUNLEVEL,'') as RUNLEVEL, isnull(sc.SYEAR,'') as SYEAR, isnull(sc.SMONTH,'') as SMONTH, isnull(sc.SDAY,'') as SDAY,\n" +
      " isnull(sc.SHOUR,'') as SHOUR, isnull(sc.STIME,'') as STIME, isnull(sc.INTERVAL,'') as INTERVAL \n" +
      "from Pre_Print p left join SCHED sc on sc.S_NAME = p.GUID\n" +
      "where p.guid=?";

  protected static String enablePreQuery = null;
  protected String guid ="";

  public static boolean isEnablePreQuery() {
    try {
      if (enablePreQuery == null)
        enablePreQuery = emisProp.getInstance(context).get("prePrint", "N");
      return enablePreQuery.equalsIgnoreCase("Y");
    } catch (Exception e) {
      return false;
    }
  }
  


  //for javascript
  public String proc() throws Exception {
    StringBuffer result = new StringBuffer();
    try {
      ResultSet rs = getResultSet();
      String fldVal = "";
      while (rs.next()) {
        result.append(";").append(rs.getString("NAME")).append("|").append(rs.getString("VALUE").replaceAll("\r","<br>").replaceAll("\n", "<br>").replaceFirst("\\{\\{.*\\}\\}#?", ""));

        for(String field : NOT_INCLUDED_IN_THE_VALUE.split(",")){
          field = field.trim();

          if("MAILCOTENT".equals(field) && rs.getString(field) != null){
            result.append("#").append(field).append("=").append(rs.getString(field).replaceAll("\r", "<br>").replaceAll("\n", "<br>"));
          } else {
            result.append("#").append(field).append("=").append(rs.getString(field));
          }
        }
        // 以下参数不从VALUE字段值中取，组在后面会以后面的为准。
        result.append("#chkShowMenu=").append(rs.getString("showMenu"))
            .append("#chkRunTask=").append(rs.getString("RUNTASK"))
            .append("#chkIsMailFile=").append(rs.getString("ISMAILFILE"));

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
}
