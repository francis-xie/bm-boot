//$Id: emis-lang_zh_CN.js 10553 2017-12-28 03:53:46Z andy.he $
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
    contextmenu_reload: "重新整理",
    contextmenu_properties: "内容",

    /* ****** 工作区域 ***** */
    show_active_head: "表头",
    show_active_body: "表身",

    /* ****** 星期对照 ***** */
    week_day: "['星期日','星期一','星期二','星期三','星期四','星期五','星期六']",

    /* ****** 验证讯息 ***** */
    field_valid_result : "『{0}』字段",

    // 数值验证讯息
    number_is_not_a_number : "不是数值型态！",
    number_not_as_negative : "不可为负数！",
    number_out_of_integer_digits : "整数位超出 {0} 位数！",
    number_can_not_have_decimal : "不可有小数位！",
    number_out_of_decimal_digits : "小数位超出 {0} 位数！",

    // 区间检查讯息
    sequence_begin_over_end_error : "『{0}』字段起始值不可大于终止值！",

    // 字符验证讯息
    string_length_over_error : "『{0}』字段输入长度超过 {1} 字符！",
    string_CE_length_over_error : "『{0}』字段,中英文输入长度超过 {1} 字符！",
    string_is_required : "『{0}』字段不可为空白！",
    string_length_no_full_error : "『{0}』字段输入长度不足！",

    /* ****** 字符验证讯息  ***** */
    pid_length_error: "输入长度错误！",
    pid_is_not_number_error: "编号只能为数字！",
    pid_error: "编码错误!!",

    /* ****** 身份证验证 ID_NO  ***** */
    id_no_length_error: "输入长度错误！",
    id_no_first_must_be_letter: "证号第一码只能为英文字母！",
    id_no_must_end_with_letter: "证号后九码只能为数字！",
    id_no_error: "证号编码错误！",

    /* ****** 统一发票  ***** */
    invoice_length_error: "输入长度错误！",
    invoice_first_must_be_letter: "证号第一码只能为英文字母！",
    invoice_second_must_be_letter: "编号第二码只能为英文字母！",
    invoice_must_end_with_letter: "统一发票后八码只能为数字！",
    invoice_error: "证号编码错误！",

    /* ****** 日期时间验证讯息  ***** */
    date_is_invalidation : "『{0}』字段日期格式错误！",
    date_is_invalidation2 : "『{0}』+『{1}』+『{2}』不是有效的日期，请重新输入！",
    date_YM_is_invalidation : "『{0}』字段格式错误(年/月)！",
    date_YM_is_invalidation2 : "『{0}』字段年月格式错误！",
    year_is_invalidation : "『{0}』字段不是有效的年份，请重新输入！",
    month_is_invalidation : "『{0}』字段不是有效的月份，请重新输入！",
    day_is_invalidation : "『{0}』字段不是有效的日，请重新输入！",
    season_is_invalidation : "『{0}』字段不是有效的季度，请重新输入！",
    time_is_invalidation : "『{0}』字段时间格式错误！",

    /* ****** 邮件验证讯息  ***** */
    email_required_character : "『{0}』字段必须输入 \"@\" 和 \".\" 符号！",

    /* ****** 网址验证讯息  ***** */
    url_required_character : "『{0}』字段必须输入 \".\" 符号！",

    /* ****** E_MAIL 讯息处理  ***** */
    mail_send_successful : "邮件寄出成功！",
    mail_can_not_send_error : "邮件无法寄出！\n\n[错误讯息]:\n",

    /* ****** UpLoad 讯息处理  ***** */
    file_upload_successful : "档案上载成功！",
    file_can_not_upload_error : "档案无法上载！\n\n[错误讯息]:\n",

    /* ****** 查询讯息处理  ***** */
    load_nothing_msg : "无任何查询数据！",
    query_input_error : "『{0}』字段输入错误，\n\n",
    query_nothing_msg : "无任何查询数据，请重新输入！",
    query_can_not_focus : "无法设定字段值 : ",
    query_can_not_focus_next : "驻点无法移至下一字段!",
    query_have_not_condition : "请输入欲查询之条件 ！",

    /* ****** 重复编号讯息  ***** */
    check_input_repeat : "重复，请重新输入！",

    /* ****** 确认提示讯息  ***** */
    confirm_for_save : "是否确定欲储存？ <Y/N>",
    confirm_for_save_go_on : "是否确定欲储存此资料[取消=继续修改]？",
    confirm_for_change : "是否确定修改资料？ <Y/N>",
    confirm_for_something : "是否确定欲{0}资料？ <Y/N>",
    confirm_for_delete : "是否确定欲删除资料？ <Y/N>",

    selection_of_data_too_more : "您选取的数据过多，请分批选取！",
    process_failed : "处理失败!",
    process_success : "处理完成({0})!",
    has_no_data_selected : "您尚未选取任何数据",
    has_no_data_update : "您未做任何修改",
    can_not_found_data : "找不到 {0} 字段, 请宣告 {0} 字段!",
    query_button_title : "，目前查询条件: ",
    ajax_query_button_title : "\n*****************\n目前查询条件:",
    must_be_input_range_value : "请同时输入商品起讫区间!!",
    //no choice of information : "没有选择的资料",
    no_choice_anything : "您尚未选取任何数据",

    /* ****** 单据状态讯息  ***** */
    fls_no_ed : "编辑",
    fls_no_co : "结案",
    fls_no_cf : "确认",
    fls_no_ff : "背景确认",
    fls_no_mo : "月结",
    fls_no_9 : "已冲销",
    fls_no_cl : "注销",
    fls_no_cs : "背景结案",
    fls_no_cg : "配送中",
    fls_no_sh : "出货",
    fls_no_pp : "提出",
    fls_no_dg : "送货",
    fls_no_bl : "退订",
    fls_no_dl : "删除",
    fls_no_ap : "核准",
    fls_no_ag : "采购同意",
    fls_no_gp : "采购主管确认",
    fls_no_ex : "转出货",
    fls_no_gn : "已制单",
    fls_no_gr : "部分验收",
    fls_no_0 : "背景删除",
    fls_no_rj : "拒绝",

    /* ****** XML Search 讯息  ***** */
    xml_search_found_and_focus : "数据已移至所寻找之数据 ！",
    xml_search_not_found : "找不到对应数据!!",
    xml_search_parameter_not_match : "使用XmlSearch对象时,所传参数之元素个数错误!!",
    xmldata_field_not_found : "xmlData 找不到字段 :",

    /* ****** 数据库异常讯息  ***** */
    db_can_not_save_error : "数据无法储存！\n\n[错误讯息]:\n",
    db_can_not_delete_error : "数据无法删除！\n\n[错误讯息]:\n" ,
    db_can_not_update_error : "数据更新失败，请稍后再试:",
    db_save_unique_msg : "编号重复,请重新输入!",

    /* ***** ajax_table.js ***** */
    save_cancel_title : "储存:[F10] 取消:[Esc]",
    toolbar_mode_txt: '模式：',
    toolbar_mode_browse: '浏览',
    toolbar_mode_add: '新增',
    toolbar_mode_upd: '修改',
    toolbar_page_size: '每页{0}笔',
    toolbar_page_unit: '页',
    toolbar_page_rows: '笔'

    ,sort_disabled_msg: '新增或修改模式不可排序'
    ,sort_title_msg: '当前排序：'
    ,act_is_require: 'ACT不能为空值!'
    ,act_object_is_lost: '缺少ACT隐藏对象!'
    ,edit_disabled_msg: '新增或修改模式不可选择！'

    ,nav_first: '移至首笔'
    ,nav_last: '移至末笔'
    ,nav_next_rec: '移至下一笔'
    ,nav_prev_rec: '移至上一笔'
    ,nav_next_page: '移至下一页'
    ,nav_prev_page: '移至上一页'

    ,f5_disabled_msg: '请勿使用F5重新刷新页面'
    ,ctrl_r_disabled_msg: '请勿使用 Ctrl-R 重新刷新页面'

    /* ***** ajax_mtn.js ***** */
    ,qqry_msg: "请输入欲【寻找】之『{0}』："
    ,qqry_notfound_msg: "查无此输入之『{0}』：{1}！"
    ,downrec_end_msg: "已至此查询条件之最后笔数，无法继续往下查询！"
    ,downrec_not_qry: "请先选择查询条件！"

    /* ***** ajax_util.js ***** */
    ,marquee_msg: '资料处理中，请稍待片刻．．．．'
    ,up_no_form_msg: '找不到Form表单!'
    ,rbl_tvh_recall_msg: '不允许回溯，已抛初稿{0}'
    ,rbl_tvh_restore_msg: '不允许还原，已拋初稿{0}'
    ,no_power_msg: '您尚无权限执行此操作！'

    ,check_execute_err_0: '系统内存资源耗尽！'
    ,check_execute_err_2: '{0} 执行档不存在,请先下载！'
    ,check_execute_err_3: '指定的路径不存在'
    ,check_execute_err_11: '执行档档案格式不合 (non-Win32 .EXE or error in .EXE image)'
    ,check_execute_err_5: '系统拒绝存取'
    ,check_execute_err_27: '档名不完整'
    ,check_execute_err_8: '内存不足'
    ,check_execute_err_def: '未知的错误:{0}'
    ,chkNewPasswd_paswd_length: '“密码”长度不能少于{0}码！'
    ,chkNewPasswd_require_latter_number: '“密码”需由字母和数字组成!'
    ,chkIsMclock_msg1: '当前单据资料所属月份已关帐，禁止相关操作!'
    ,chkIsMclock_msg2: "{0}\n\n      请于4D[关帐月份设定]功能中删除门市关帐年月!"
    ,ajax_check_flsno_msg:"单据状态已被改变，请刷新后再试！"
    /* ***** ajax_inc.htm ***** */
    ,qry_total: "总共 {0} 笔"

    /* ***** ajax_sel.jsp ***** */
    ,ajax_sel_remark: "注意：查询之笔数超过预设最多笔数：{0}笔，系统自动仅取前{0}笔资料！"
    ,ajax_sel_nofound: "无法找到栏位: {0}, 请检查 select 栏位!"
    ,ajax_sel_no_data:"请勾选资料!"
    ,ajax_search_title:"回车开始页面数据搜索"
    ,ajax_search_value:"页面数据搜索"
    ,ajax_search_searching:"搜索中...."
    ,ajax_search_percent:"正在搜索:"
    ,ajax_search_finish:"搜索完成!"
    ,ajax_search_no_data_found:"搜索不到数据!"
    /* ***** ajax_util.js AjaxIdbrowserMove1Left 侧边栏显示***** */
    ,ajax_util_FilterCond: "筛选条件"
    ,ajax_util_nodata: "查无资料:[{0}]，请确认[sQQryMsgs_]设定名称是否有误或未绑定！"
    ,ajax_util_divopenup: "展开"
    ,ajax_util_divclose: "关闭"

    ,ajax_util_no_authority:"当前没有对照表权限"
    ,ajax_mtn_has_not_loaded:"表身资料尚未加载完成，不允许执行此操作"

    /* ***** jquery.emis.sidebar.js ***** */
    ,layer_bar_open_title: "展开"
    ,layer_bar_close_title: "收起"

    ,filter_head_title_Y: "资料列表"
    ,filter_head_title_N: "全选/取消(滤后{0}/共{1}笔)"
    ,filter_head_flag_Y: "显示过滤条件>"
    ,filter_head_flag_N: "隐藏过滤条件>"

    ,recno_title: "序"
    ,empty_data: "[空白]"
    ,show_rpt_title: "EMIS_报表下载"

    /* ***** ymPrompt_inc.htm ***** */
    ,ymprompt_btn_ok: "确定"
    ,ymprompt_btn_cancel: "取消"
    ,ymprompt_btn_close: "关闭"
    ,ymprompt_btn_yes: "是(Y)"
    ,ymprompt_btn_no: "否(N)"
    ,ymprompt_tip_title: "温馨提示"

    /* ***** dwz\src\emis.ui.js ***** */
    ,error_503: "503 服务忙碌，请稍等30秒后再试..."

    /* ***** multiselect.js***** */
    ,multiselect_all:"全选"
    ,multiselect_none:"取消"
    ,multiselect_reset:"重置"
    ,multiselect_filter_remark:"* 数据搜索 *"
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
