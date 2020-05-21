//$Id: emis-lang_zh_TW.js 10553 2017-12-28 03:53:46Z andy.he $
// emis-lang_zh-tw.js
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
    /* ****** 右鍵菜單 ***** */
    contextmenu_reload: "重新整理",
    contextmenu_properties: "內容",

    /* ****** 工作區域 ***** */
    show_active_head: "表頭",
    show_active_body: "表身",

    /* ****** 星期對照 ***** */
    week_day: "['星期日','星期一','星期二','星期三','星期四','星期五','星期六']",

    /* ****** 驗證訊息 ***** */
    field_valid_result : "『{0}』欄位",

    /* ****** 數值驗證訊息  ***** */
    number_is_not_a_number : "不是數值型態！",
    number_not_as_negative : "不可為負數！",
    number_out_of_integer_digits : "整數位超出 {0} 位數！",
    number_can_not_have_decimal : "不可有小數位！",
    number_out_of_decimal_digits : "小數位超出 {0} 位數！",

    /* ****** 區間檢查訊息  ***** */
    sequence_begin_over_end_error : "『{0}』欄位起始值不可大於終止值！",

    /* ****** 字符驗證訊息  ***** */
    string_length_over_error : "『{0}』欄位輸入長度超過 {1} 字元！",
    string_CE_length_over_error : "『{0}』欄位,中英文輸入長度超過 {1} 字元！",
    string_is_required : "『{0}』欄位不可為空白！",
    string_length_no_full_error : "『{0}』欄位輸入長度不足！",

    /* ****** PID驗證訊息  ***** */
    pid_length_error: "輸入長度錯誤！",
    pid_is_not_number_error: "編號只能為數字！",
    pid_error: "編碼錯誤!!",

    /* ****** 身份證驗證 ID_NO  ***** */
    id_no_length_error: "輸入長度錯誤！",
    id_no_first_must_be_letter: "證號第一碼只能為英文字母！",
    id_no_must_end_with_letter: "證號後九碼只能為數字！",
    id_no_error: "證號編碼錯誤！",

    /* ****** 統一發票  ***** */
    invoice_length_error: "輸入長度錯誤！",
    invoice_first_must_be_letter: "證號第一碼只能為英文字母！",
    invoice_second_must_be_letter: "編號第二碼只能為英文字母！",
    invoice_must_end_with_letter: "統一發票後八碼只能為數字！",
    invoice_error: "證號編碼錯誤！",

    /* ****** 日期時間驗證訊息  ***** */
    date_is_invalidation : "『{0}』欄位日期格式錯誤！",
    date_is_invalidation2 : "『{0}』+『{1}』+『{2}』不是有效的日期，請重新輸入！",
    date_YM_is_invalidation : "『{0}』欄位格式錯誤(年/月)！",
    date_YM_is_invalidation2 : "『{0}』欄位年月格式錯誤！",
    year_is_invalidation : "『{0}』欄位不是有效的年份，請重新輸入！",
    month_is_invalidation : "『{0}』欄位不是有效的月份，請重新輸入！",
    day_is_invalidation : "『{0}』欄位不是有效的日，請重新輸入！",
    season_is_invalidation : "『{0}』欄位不是有效的季度，請重新輸入！",
    time_is_invalidation : "『{0}』欄位時間格式錯誤！",

    /* ****** 郵件驗證訊息  ***** */
    email_required_character : "『{0}』欄位必須輸入 \"@\" 和 \".\" 符號！",

    /* ****** 網址驗證訊息  ***** */
    url_required_character : "『{0}』欄位必須輸入 \".\" 符號！",

    /* ****** E_MAIL 訊息處理  ***** */
    mail_send_successful : "郵件寄出成功！",
    mail_can_not_send_error : "郵件無法寄出！\n\n[錯誤訊息]:\n",

    /* ****** UpLoad 訊息處理  ***** */
    file_upload_successful : "檔案上載成功！",
    file_can_not_upload_error : "檔案無法上載！\n\n[錯誤訊息]:\n",

    /* ****** 查詢訊息處理  ***** */
    load_nothing_msg : "無任何查詢資料！",
    query_input_error : "『{0}』欄位輸入錯誤，\n\n",
    query_nothing_msg : "無任何查詢資料，請重新輸入！",
    query_can_not_focus : "無法設定欄位值 : ",
    query_can_not_focus_next : "駐點無法移至下一欄位!",
    query_have_not_condition : "請輸入欲查詢之條件 ！",

    /* ****** 重複編號訊息  ***** */
    check_input_repeat : "重複，請重新輸入！",

    /* ****** 確認及提示訊息  ***** */
    confirm_for_save : "是否確定欲儲存？ <Y/N>",
    confirm_for_save_go_on : "是否確定欲儲存此資料[取消=繼續修改]？",
    confirm_for_change : "是否確定修改資料？ <Y/N>",
    confirm_for_something : "是否確定欲{0}資料？ <Y/N>",
    confirm_for_delete : "是否確定欲刪除資料？ <Y/N>",

    selection_of_data_too_more : "您選取的資料過多，請分批選取！",
    process_failed : "處理失敗!",
    process_success : "處理完成({0})!",
    has_no_data_selected : "您尚未選取任何資料",
    has_no_data_update : "您未做任何修改",
    can_not_found_data : "找不到 {0} 欄位, 請宣告 {0} 欄位!",
    query_button_title : "，目前查詢條件: ",
    ajax_query_button_title : "\n*****************\n目前查詢條件:",
    must_be_input_range_value : "請同時輸入商品起訖區間!!",
    //no choice of information : "沒有選擇的資料",
    no_choice_anything : "您尚未選取任何資料",

    /* ****** 單據狀態訊息  ***** */
    fls_no_ed : "編輯",
    fls_no_co : "結案",
    fls_no_cf : "確認",
    fls_no_ff : "背景確認",
    fls_no_mo : "月結",
    fls_no_9 : "已沖銷",
    fls_no_cl : "註銷",
    fls_no_cs : "背景結案",
    fls_no_cg : "配送中",
    fls_no_sh : "出貨",
    fls_no_pp : "提出",
    fls_no_dg : "送貨",
    fls_no_bl : "退訂",
    fls_no_dl : "刪除",
    fls_no_ap : "核準",
    fls_no_ag : "採購同意",
    fls_no_gp : "採購主管確認",
    fls_no_ex : "轉出貨",
    fls_no_gn : "已制單",
    fls_no_gr : "部分驗收",
    fls_no_0 : "背景刪除",
    fls_no_rj : "拒絕",

    /* ****** XML Search 訊息  ***** */
    xml_search_found_and_focus : "資料已移至所尋找之資料 ！",
    xml_search_not_found : "找不到對應資料!!",
    xml_search_parameter_not_match : "使用XmlSearch物件時,所傳參數之元素個數錯誤!!",
    xmldata_field_not_found : "xmlData 找不到欄位 :",

    /* ****** 數據庫異常訊息  ***** */
    db_can_not_save_error : "資料無法儲存！\n\n[錯誤訊息]:\n",
    db_can_not_delete_error : "資料無法刪除！\n\n[錯誤訊息]:\n",
    db_can_not_update_error : "資料更新失敗，請稍後再試:",
    db_save_unique_msg : "編號重覆,請重新輸入!",

    /* ***** ajax_table.js ***** */
    save_cancel_title : "儲存:[F10] 取消:[Esc]",
    toolbar_mode_txt: '模式：',
    toolbar_mode_browse: '瀏覽',
    toolbar_mode_add: '新增',
    toolbar_mode_upd: '修改',
    toolbar_page_size: '每頁{0}筆',
    toolbar_page_unit: '頁',
    toolbar_page_rows: '筆'

    ,sort_disabled_msg: '新增或修改模式不可排序'
    ,sort_title_msg: '當前排序：'
    ,act_is_require: 'ACT不能為空值!'
    ,act_object_is_lost: '缺少ACT隱藏物件!'
    ,edit_disabled_msg: '新增或修改模式不可選擇！'

    ,nav_first: '移至首筆'
    ,nav_last: '移至末筆'
    ,nav_next_rec: '移至下一筆'
    ,nav_prev_rec: '移至上一筆'
    ,nav_next_page: '移至下一頁'
    ,nav_prev_page: '移至上一頁'

    ,f5_disabled_msg: '請勿使用F5重新整理頁面'
    ,ctrl_r_disabled_msg: '請勿使用 Ctrl-R 重新整理頁面'

    /* ***** ajax_mtn.js ***** */
    ,qqry_msg: "請輸入欲【尋找】之『{0}』："
    ,qqry_notfound_msg: "查無此輸入之『{0}』：{1}！"
    ,downrec_end_msg: "已至此查詢條件之最後筆數，無法繼續往下查詢！"
    ,downrec_not_qry: "請先選擇查詢條件！"

    /* ***** ajax_util.js ***** */
    ,marquee_msg: '資料處理中，請稍待片刻．．．．'
    ,up_no_form_msg: '找不到Form表單!'
    ,rbl_tvh_recall_msg: '不允許回溯，已拋初稿{0}'
    ,rbl_tvh_restore_msg: '不允許還原，已拋初稿{0}'
    ,no_power_msg: '您尚無權限執行此操作！'

    ,check_execute_err_0: '系統記憶體資源耗盡！'
    ,check_execute_err_2: '{0} 執行檔不存在,請先下載！'
    ,check_execute_err_3: '指定的路徑不存在'
    ,check_execute_err_11: '執行檔檔案格式不合 (non-Win32 .EXE or error in .EXE image)'
    ,check_execute_err_5: '系統拒絕存取'
    ,check_execute_err_27: '檔名不完整'
    ,check_execute_err_8: '記憶體不足'
    ,check_execute_err_def: '未知的錯誤:{0}'
    ,chkNewPasswd_paswd_length: '「密碼」長度不能少於{0}碼！'
    ,chkNewPasswd_require_latter_number: '「密碼」需由字母和數字組成!'
    ,chkIsMclock_msg1: '當前單據資料所屬月份已關帳，禁止相關操作!'
    ,chkIsMclock_msg2: "{0}\n\n      請於4D[關帳月份設定]功能中刪除門市關帳年月!"
    ,ajax_check_flsno_msg:"单据状态已被改变，请刷新后再试！"
    /* ***** ajax_inc.htm ***** */
    ,qry_total: "總共 {0} 筆"

    /* ***** ajax_sel.jsp ***** */
    ,ajax_sel_remark: "注意: 查詢之筆數超過預設最多筆數：{0}筆，系統自動僅取前{0}筆資料！"
    ,ajax_sel_nofound: "無法找到欄位: {0}, 請檢查 select 欄位!"
    ,ajax_sel_no_data:"請勾選資料!"
    ,ajax_search_title:"回車開始頁面數據搜索"
    ,ajax_search_value:"頁面數據搜索"
    ,ajax_search_searching:"搜索中...."
    ,ajax_search_percent:"正在搜索:"
    ,ajax_search_finish:"搜索完成!"
    ,ajax_search_no_data_found:"搜索不到數據!"
    /* ***** ajax_util.js AjaxIdbrowserMove1Left 側邊欄顯示***** */
    ,ajax_util_FilterCond: "篩選條件"
    ,ajax_util_nodata: "查無資料:[{0}]，請確認[sQQryMsgs_]設定名稱是否有誤或未綁定！"
    ,ajax_util_divopenup: "展開"
    ,ajax_util_divclose: "關閉"

    ,ajax_util_no_authority:"當前沒有對照表權限"
    ,ajax_mtn_has_not_loaded:"表身資料尚未加載完成，不允許執行此操作"

    /* ***** jquery.emis.sidebar.js ***** */
    ,layer_bar_open_title: "展開"
    ,layer_bar_close_title: "收起"

    ,filter_head_title_Y: "資料列表"
    ,filter_head_title_N: "全選/取消(濾後{0}/共{1}筆)"
    ,filter_head_flag_Y: "顯示過濾條件>"
    ,filter_head_flag_N: "隱藏過濾條件>"

    ,recno_title: "序"
    ,empty_data: "[空白]"
    ,show_rpt_title: "EMIS_報表下載"

    /* ***** ymPrompt_inc.htm ***** */
    ,ymprompt_btn_ok: "確定"
    ,ymprompt_btn_cancel: "取消"
    ,ymprompt_btn_close: "關閉"
    ,ymprompt_btn_yes: "是(Y)"
    ,ymprompt_btn_no: "否(N)"
    ,ymprompt_tip_title: "溫馨提示"

    /* ***** dwz\src\emis.ui.js ***** */
    ,error_503: "503 服務忙碌，請稍等30秒後再試..."

    /* ***** multiselect.js***** */
    ,multiselect_all:"全選"
    ,multiselect_none:"取消"
    ,multiselect_reset:"重置"
    ,multiselect_filter_remark:"* 數據搜索 *"
  }

  $.extend({
    getMessage: function () {
      if (arguments.length == 0) return '';

      var key = arguments[0];
      var message = messageSource[key];
      if (typeof message == 'undefined' || $.trim(message) == '') return '';

      var args = arguments.length > 1 ? $.makeArray(arguments).slice(1) : null;
      return args ? new String().format.apply(message, args) : message;
    }
  });

})(jQuery);
