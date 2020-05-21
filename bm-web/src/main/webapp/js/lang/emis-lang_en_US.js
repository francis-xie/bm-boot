//$Id: emis-lang_en_US.js 10553 2017-12-28 03:53:46Z andy.he $
// emis-lang_zh-cn.js
(function($) {
  if (typeof String.prototype.format != "function") {
    String.prototype.format = function() {
      var args = arguments;
      return this.replace(/\{(\d+)\}/g, function(m, i) {
        return args[i];
      });
    }
  }
  var messageSource = {
    /* ****** 右键菜单 ***** */
    contextmenu_reload: "Refresh",
    contextmenu_properties: "Properties",

    /* ****** 工作区域 ***** */
    show_active_head: "Header",
    show_active_body: "Details",

    /* ****** 星期对照 ***** */
    week_day: "['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday']",

    /* ****** 验证讯息 ***** */
    field_valid_result : "『{0}』field ",

    // 数值验证讯息
    number_is_not_a_number : "is not numerical patterns!",
    number_not_as_negative : "do not allow the negative！",
    number_out_of_integer_digits : "integer places over than {0} digits!",
    number_can_not_have_decimal : "do not allow the decimal!",
    number_out_of_decimal_digits : "decimal places over than {0} digits!",

    // 区间检查讯息
    sequence_begin_over_end_error : "『{0}』field begin value cannot be greater than the end value!",

    // 字符验证讯息
    string_length_over_error : "『{0}』 field input length over than {1} chars！",
    string_CE_length_over_error : "『{0}』filed, input length over than {1} chars！",
    string_is_required : "『{0}』field is required！",
    string_length_no_full_error : "『{0}』field length too short！",

    /* ****** 字符验证讯息  ***** */
    pid_length_error: "input length invalid!",
    pid_is_not_number_error: "Number only for digital!",
    pid_error: "Number invalid!",

    /* ****** 身份证验证 ID_NO  ***** */
    id_no_length_error: "input length invalid!",
    id_no_first_must_be_letter: "ID card NO. first char should be letters!",
    id_no_must_end_with_letter: "ID card NO. last 9 char should be digital!",
    id_no_error: "ID card NO. invalid!",

    /* ****** 统一发票  ***** */
    invoice_length_error: "input length invalid!",
    invoice_first_must_be_letter: "Invoice NO. first char should be letters!",
    invoice_second_must_be_letter: "Invoice NO. second char should be letters!",
    invoice_must_end_with_letter: "Invoice NO. last 8 char should be digital!",
    invoice_error: "Invoice NO. invalid!",

    /* ****** 日期时间验证讯息  ***** */
    date_is_invalidation : "『{0}』field date invalid!",
    date_is_invalidation2 : "『{0}』+『{1}』+『{2}』date invalid,please input again!",
    date_YM_is_invalidation : "『{0}』field format invalid(year/month)！",
    date_YM_is_invalidation2 : "『{0}』field monthly format invalid!",
    year_is_invalidation : "『{0}』field is a invalid year,please input again!",
    month_is_invalidation : "『{0}』field is a invalid month,please input again!",
    day_is_invalidation : "『{0}』field is a invalid day,please input again!",
    season_is_invalidation : "『{0}』field is a invalid quarter,please input again!",
    time_is_invalidation : "『{0}』field time format invalid!",

    /* ****** 邮件验证讯息  ***** */
    email_required_character : "『{0}』field must input \"@\" and \".\" char！",

    /* ****** 网址验证讯息  ***** */
    url_required_character : "『{0}』field must input \".\" char！",

    /* ****** E_MAIL 讯息处理  ***** */
    mail_send_successful : "mail has send!",
    mail_can_not_send_error : "mail cannot send!\n\n[Error Message]:\n",

    /* ****** UpLoad 讯息处理  ***** */
    file_upload_successful : "File has uploaded!",
    file_can_not_upload_error : "File cannot upload！\n\n[Error Message]:\n",

    /* ****** 查询讯息处理  ***** */
    load_nothing_msg : "Cannot query any data!",
    query_input_error : "『{0}』field input error，\n\n",
    query_nothing_msg : "Cannot query any data,please input again",
    query_can_not_focus : "Cannot set the filed value : ",
    query_can_not_focus_next : "Cannot focus to next field!",
    query_have_not_condition : "Please input query conditions！",

    /* ****** 重复编号讯息  ***** */
    check_input_repeat : "Repeat,Please input again！",

    /* ****** 确认提示讯息  ***** */
    confirm_for_save : "Are you sure to store? <Y/N>",
    confirm_for_save_go_on : "Are you sure to store current data[Cancel=return to edit]?",
    confirm_for_change : "Are you sure to modify it？ <Y/N>",
    confirm_for_something : "Are you sure to {0} current data？ <Y/N>",
    confirm_for_delete : "Are you sure to delete？ <Y/N>",

    selection_of_data_too_more : "You selected data too much, please batchwise select!",
    process_failed : "process failed",
    process_success : "process done({0})!",
    has_no_data_selected : "You have not select any data",
    has_no_data_update : "No data has been modified",
    can_not_found_data : "Cannot found {0} field, please defined the {0} field!",
    query_button_title : "，Current query conditions: ",
    ajax_query_button_title : "\n*****************\nThe query conditions:",
    must_be_input_range_value : "Please input the Commodity NO. range!!",
    //no choice of information : "没有选择的资料",
    no_choice_anything : "Not found selective data",

    /* ****** 单据状态讯息  ***** */
    fls_no_ed : "Edit",
    fls_no_co : "Settled",
    fls_no_cf : "Confirmed",
    fls_no_ff : "Background Confirmed",
    fls_no_mo : "Monthly Closed",
    fls_no_9 : "已冲销",
    fls_no_cl : "Cancellation",
    fls_no_cs : "Background Settled",
    fls_no_cg : "配送中",
    fls_no_sh : "Deliveries",
    fls_no_pp : "Presented",
    fls_no_dg : "Delivery",
    fls_no_bl : "Unsubscribe",
    fls_no_dl : "Deleted",
    fls_no_ap : "Approved",
    fls_no_ag : "采购同意",
    fls_no_gp : "采购主管确认",
    fls_no_ex : "转出货",
    fls_no_gn : "已制单",
    fls_no_gr : "部分验收",
    fls_no_0 : "Background Deleted",
    fls_no_rj : "Reject",

    /* ****** XML Search 讯息  ***** */
    xml_search_found_and_focus : "Has been move to the found data!",
    xml_search_not_found : "Can not found data!!",
    xml_search_parameter_not_match : "Different number of parameters to Use the XmlSearch!!",
    xmldata_field_not_found : "xmlData can not found field :",

    /* ****** 数据库异常讯息  ***** */
    db_can_not_save_error : "Data can not stored！\n\n[error message]:\n",
    db_can_not_delete_error : "Data can not deleted！\n\n[error message]:\n" ,
    db_can_not_update_error : "Data update failed, please try again later:",
    db_save_unique_msg : "Number repeat, please input again!",

    /* ***** ajax_table.js ***** */
    save_cancel_title : "Save:[F10] Cancel:[Esc]",
    toolbar_mode_txt: 'Mode：',
    toolbar_mode_browse: 'Browse',
    toolbar_mode_add: 'Add',
    toolbar_mode_upd: 'Update',
    toolbar_page_size: 'per page {0} rows',
    toolbar_page_unit: ' pages',
    toolbar_page_rows: ' rows'

    ,sort_disabled_msg: 'Can not sorted on add or update mode！'
    ,sort_title_msg: 'The current sorted：'
    ,act_is_require: 'ACT can not be empty!'
    ,act_object_is_lost: '缺少ACT隱藏物件!'
    ,edit_disabled_msg: 'Can not selected on add or update mode！'

    ,nav_first: 'Move to first row'
    ,nav_last: 'Move to last row'
    ,nav_next_rec: 'Move to next row'
    ,nav_prev_rec: 'Move to previous row'
    ,nav_next_page: 'Move to next page'
    ,nav_prev_page: 'Move to previous page'

    ,f5_disabled_msg: 'Please do not use F5 to refresh page'
    ,ctrl_r_disabled_msg: 'Please do not use Ctrl-R to refresh page'

    /* ***** ajax_mtn.js ***** */
    ,qqry_msg: "Please input the 【Looking for】 of 『{0}』："
    ,qqry_notfound_msg: "Con not found 『{0}』：{1}！"
    ,downrec_end_msg: "Has been query to lastest row, can not be go on!"
    ,downrec_not_qry: "Please choose the query conditions！"

    /* ***** ajax_util.js ***** */
    ,marquee_msg: 'In data processing, please wait... '
    ,up_no_form_msg: 'Can not found the form!'
    ,rbl_tvh_recall_msg: 'Can not be recall,it was transfer to {0}'
    ,rbl_tvh_restore_msg: 'Can not be restore,it was transfer to {0}'
    ,no_power_msg: 'You have no permission to perform this operation!'

    ,check_execute_err_0: 'System memory has been exhausted!'
    ,check_execute_err_2: '{0} Execution file not exist, please download it！'
    ,check_execute_err_3: 'The specified path does not exist.'
    ,check_execute_err_11: 'Execution file format error (non-Win32 .EXE or error in .EXE image)'
    ,check_execute_err_5: 'Decline Access'
    ,check_execute_err_27: 'File name is not complete'
    ,check_execute_err_8: 'Out of memory'
    ,check_execute_err_def: 'unknown error:{0}'
    ,chkNewPasswd_paswd_length: 'Password can\'t be less than {0}!'
    ,chkNewPasswd_require_latter_number: '"Password" to be composed of letters and numbers!'
    ,chkIsMclock_msg1: 'The current month has been closed,ban related operations'
    ,chkIsMclock_msg2: "{0}\n\n     Please close the accounts for 4D[month] delete closing date store function!"
    ,ajax_check_flsno_msg:"The document status has been changed, please refresh and try again!"
    /* ***** ajax_inc.htm ***** */
    ,qry_total: "total {0} rows"

    /* ***** ajax_sel.jsp ***** */
    ,ajax_sel_remark: "Note：Query rows number has more than the default limit：{0} rows，it is will auto load top {0} rows！"
    ,ajax_sel_nofound: "Can not found : {0}, Please check select field!"
    ,ajax_sel_no_data:"Please check the data!"
    ,ajax_search_title:"Press enter to begin search"
    ,ajax_search_value:"Search local"
    ,ajax_search_searching:"Searching...."
    ,ajax_search_percent:"Searching:"
    ,ajax_search_finish:"Search done!"
    ,ajax_search_no_data_found:"search no data found!"
    /* ***** ajax_util.js AjaxIdbrowserMove1Left 侧边栏显示***** */
    ,ajax_util_FilterCond: "Filters"
    ,ajax_util_nodata: "Can not found:[{0}]，Please Confirm [sQQryMsgs_] setting name whether error or no bind!"
    ,ajax_util_divopenup: "Expand"
    ,ajax_util_divclose: "Collapse"

    ,ajax_util_no_authority:"You don't have access"
    ,ajax_mtn_has_not_loaded:"The body data has not been loaded, not allowed to perform this operation"

    /* ***** jquery.emis.sidebar.js ***** */
    ,layer_bar_open_title: "Expand"
    ,layer_bar_close_title: "Collapse"

    ,filter_head_title_Y: "Data List"
    ,filter_head_title_N: "All/Cancel(filtered {0} / total {1} rows)"
    ,filter_head_flag_Y: "Show filters>"
    ,filter_head_flag_N: "Hide filters>"

    ,recno_title: "NO."
    ,empty_data: "[NULL]"
    ,show_rpt_title: "Download the report"

    /* ***** ymPrompt_inc.htm ***** */
    ,ymprompt_btn_ok: "Confirm"
    ,ymprompt_btn_cancel: "Cancel"
    ,ymprompt_btn_close: "Close"
    ,ymprompt_btn_yes: "Yes(Y)"
    ,ymprompt_btn_no: "No(N)"
    ,ymprompt_tip_title: "Warm prompt"

    /* ***** multiselect.js***** */
    ,multiselect_all:"all"
    ,multiselect_none:"none"
    ,multiselect_reset:"reset"
    ,multiselect_filter_remark:"* Data search *"
  }


  $.extend({
    getMessage : function() {
      if (arguments.length == 0)
        return '';

      var key = arguments[0];
      var message = messageSource[key];
      if (typeof message == 'undefined' || $.trim(message) == '')
        return '';

      var args = arguments.length > 1 ? $.makeArray(arguments)
          .slice(1) : null;
      return args
          ? new String().format.apply(message, args)
          : message;
    }
  });

})(jQuery);
