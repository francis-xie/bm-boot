/** $Id: jquery.emis.jumpto.js 9386 2017-08-25 01:12:47Z andy.he $
 * 例子：
 * HTML：
 * 以Tab方式打开ZI作业
 * <div DataFld='SH_NO' class="jumpto" emis-data="title:'',target:'ZI',keys:'H01',val:"LEASE_STATE"></div>
 * 以Open方式打开ZI作业
 * <div DataFld='SH_NO' class="jumpto" emis-data="title:'',target:'ZI',keys:'H01',val:getVal, mode:'open'"></div>

 * JS：
 * $(document).ready(function () {
 *   $(".jumpto").jumpto();
 * });
 *
 * function getVal(obj){
 *  return "&QRY_TNO1=LEASE_STATE&QRY_TNO2=LEASE_STATE";
 * }
 */
;
(function ($) {

  var basePath = (function () {
    var sFullPath = window.document.location.href;
    var sUriPath = window.document.location.pathname;
    var pos = sFullPath.indexOf(sUriPath);
    var prePath = sFullPath.substring(0, pos);
    var postPath = sUriPath.substring(0, sUriPath.substr(1).indexOf('/') + 1);
    var webPath = prePath + postPath + "/";
    return webPath;
  })();
  // 缓存功能表
  var _MENUS_KEYS = null;
  /**
   * 获取当前User的所有权限
   * @return {string}
   */
  function getMenus() {
    var menus = "";
    if (_MENUS_KEYS != null) {
      menus = _MENUS_KEYS;
    } else if (typeof window.sMenus != "undefined") {
      menus = window.sMenus;
    } else {
      jQuery.ajax({
        type: "POST",
        url: basePath + "dwz/jsp/load_session_data.jsp",
        data: {"SESSION_KEY": "MENUS"},
        dataType: 'text',
        success: function (data) {
          menus = data
        },
        async: false
      });
    }
    return menus;
  }


  // 缓存功能表很名称和路径对照表,避免多次从服务端查询
  var _MENUS_CACHE = {};
  /**
   * 从Session中获取相关数据
   * @param keys
   * @param sessionKey
   * @return {string}
   */
  function getData(keys, sessionKey) {
    if (!keys || !sessionKey) return "";
    // 获取缓存数据
    var obj = _MENUS_CACHE[keys];
    // 不存在则创建一个新的空对象
    if (!obj){
      obj = {};
      _MENUS_CACHE[keys] = obj;
    }
    // 获取缓存中的内容
    if (obj[sessionKey]) {
      str = obj[sessionKey];
    }
    // 缓存不存在，从服务端获取
    else {
      var str = "";
      jQuery.ajax({
        type: "POST",
        url: basePath + "dwz/jsp/load_session_data.jsp",
        data: {"SESSION_KEY": sessionKey, "TYPE": "MAP", "KEYS": keys},
        dataType: 'text',
        success: function (data) {
          str = data;
          // 写入缓存
          obj[sessionKey] = data;
        },
        async: false
      });
    }
    return /MENUS_MSG/i.test(sessionKey) ? (keys + " " + str) : str;
  }

  /**
   * 解析獲取內容
   * @param val
   * @return {*}
   */
  function getValue(val, target) {
    var sVal = val || "";
    return $.isFunction(sVal) ? (target ? sVal.call(window, target) : sVal.call(window)) : sVal;
  }
  /**
   * 包将生成连接
   * @param node
   * @return {*}
   */
  function wrap(node) {
    // 2013/03/12 Joe 修正在V3中遇到绑定栏位时取值错误问题
    //return $(node).wrapInner('<a href="javascript:void(0);" style="color:#0000FF" tabindex="-1"></a>').find("a");
    return $(node).css({"color":"#0000FF", "cursor":"pointer"});
  }

  /**
   * 检查权限
   * @param keys
   * @return {*}
   */
  function checkRight(keys) {
    return $.inArray(keys, $.trim((getMenus() || "")).split('|')) != -1;
  }

  /**
   * 生成URL
   * @param target 目标JSP名称，通常为作业代码
   * @param keys   来源作业代码，传入目标作业
   * @param val    传入目标的参数值, 若val为函数，则调用并得到结果，要求返回结果 如 &para1=xxx&para2=yyy
   * @url          目标url（如作业内部的跳转，jsp不是对应的作业代码，则可以指定url）。
   * @param obj    事件源对象
   * @return {string}
   */
  function generateURL(target, keys, val, url, obj) {
    // 若val为函数，则调用并得到结果，要求返回结果 如 &para1=xxx&para2=yyy
    url = (!!url ? url : $.fn.JumpTo.methods.GetEXE(target) );
    return basePath + url + ( url.indexOf("?") > -1 ? "&" : "?" ) + "target=" + keys +
        ($.isFunction(val) ? getValue(val, obj) : ("&QRY_TNO1=" + val + "&QRY_TNO2=" + val));
  }

  /**
   * 跳转到目标作业，默认以Tab方式打开
   * @param obj
   * @param opts
   */
  function jumpto(obj, opts) {
    if (!opts) return;
    var target = getValue(opts.target, obj),
        keys = opts.keys,
        val = opts.val,
        url = opts.url || "",
        title = opts.title || $.fn.JumpTo.methods.GetMSG(target),
        mode = opts.mode || "tab";
    // 用A标签包装并绑定点击事件
    wrap(obj).attr('title',title).click(function (e) {
      /*if (mode == 'tab' && $.isFunction(_emisTop.dwzOpenNavTab)) {
        //_emisTop.dwzOpenNavTab(title, target, sUrl, {'win': _emisTop});
        // tabid 固定为 menu ,以便与Load_menus.jsp 一致，以达到控制面签个数的目标
        _emisTop.dwzOpenNavTab(title, 'menu', sUrl, {'win': _emisTop});
      }
      else if ($.isFunction(emisWinOpen)) {
        emisWinOpen(sUrl, -1, -1, 0, 0, target);
      }*/
      // 2013/08/13 Joe 改为真正要全用时才生成URL，避免val为函数时被提前执行而导致结果错误
      // 2015/10/23 Andy generateURL 增加url参数
      var sUrl = generateURL(target, keys, val, url, obj);
      $.JumpTo({mode: mode, target: target, title: title, url: sUrl});
      return false;
    });
  }

  $.fn.JumpTo = function (opts) {
    this.each(function () {
      // JSON参数传入
      if(opts){
        jumpto(this, opts);
      }else{
        // HTML参数传入
        var $this = $(this),
            datas = $this.attr("emis-data");
        if ($.trim(datas)) {
          eval("datas = {" + $this.attr("emis-data") + "}");
          if (checkRight(getValue(datas["target"],this))) {
            jumpto(this, datas);
          }
        }
      }
    })
  };

  // 对外公开的方法
  $.fn.JumpTo.methods = {
    GetMSG: function(keys){
      return getData(keys, "MENUS_MSG");
    },
    GetEXE: function(keys){
      return getData(keys, "MENUS_EXE");
    }
  };
  // 静态方法
  $.extend({
    JumpTo: function (options, params) {
      if (typeof options == "string" && $.isFunction($.fn.JumpTo.methods[options])) {
        return $.fn.JumpTo.methods[options](params);
      } else {

        var mode = options.mode || "",
            target = options.target || "",
            title = options.title || "",
            url = options.url || "";

        if (url != "" && target != "" &&  checkRight(target)) {
          if (title == "" && target != "") {
            title = $.fn.JumpTo.methods.GetMSG(target);
          }
          if (mode == 'tab' && typeof _emisTop != "undefined" && $.isFunction(_emisTop.dwzOpenNavTab)) {
            //_emisTop.dwzOpenNavTab(title, target, sUrl, {'win': _emisTop});
            // tabid 固定为 menu ,以便与Load_menus.jsp 一致，以达到控制面签个数的目标
            _emisTop.dwzOpenNavTab(title, 'menu', url, {'win': _emisTop});
          }
          else if ($.isFunction(emisWinOpen)) {
            emisWinOpen(url, -1, -1, 0, 0, target || title);
          }
        }
      }
    }
  });

  $(document).ready(function () {
    $(".jumpto").JumpTo();
  });
})(jQuery);