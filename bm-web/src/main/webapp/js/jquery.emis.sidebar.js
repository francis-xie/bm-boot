/** $Id: jquery.emis.sidebar.js 5 2015-05-27 08:15:12Z andy.he $
 * 侧栏查询功能插件
 */
(function ($, undefined) {
  var className = "emisSidebar";
  // 控制显示记录行下标，空表显示全部显示,非空则表示要显示指定下示的行
  var expectation = [];

  // 项目的根路径
  var basePath = (function () {
    var sFullPath = window.document.location.href;
    var sUriPath = window.document.location.pathname;
    var pos = sFullPath.indexOf(sUriPath);
    var prePath = sFullPath.substring(0, pos);
    var postPath = sUriPath.substring(0, sUriPath.substr(1).indexOf('/') + 1);
    var webPath = prePath + postPath + "/";
    return webPath;
  })();
  /**
   * 通常是以body来增加一个侧栏
   * @param options
   * @param params
   */
  $.fn.emisSidebar = function (options, params) {
    // 方法调用
    if (typeof options == 'string') {
      if ($.isFunction($.fn.emisSidebar.methods[options])) {
        this.each(function () {
          return $.fn.emisSidebar.methods[options](this, params);
        });
      }
      return false;
    }

    // 初始化
    options = options || {};
    return this.each(function () {
      var opts, state = $.data(this, className);
      // 第二次处理初始化参数
      if (state) {
        opts = $.extend({}, state.options, options);
      }
      // 首次处理初始化参数
      else {
        opts = $.extend({}, $.fn.emisSidebar.defaults, options);
      }
      // 检查是否启用侧栏，否则不做处理并返回
      if (!opts.used) return;
      // 储存参数
      $.data(this, className, {options: opts});
      // 2013/07/10 改用鼠标第一次进入时才初始化，避免以navTabFrame时计算错误问题发生
      var that = this;
      $(document).one("mouseover", function () {
        // 2014/08/27 joe 增加延时处理，避免计算左边坐标错误问题
        setTimeout(function(){
          // 呼叫初始化
          setup(that,  $.extend({}, opts));
        },10);
      });
    });
  };

  // 接口方法定义
  $.fn.emisSidebar.methods = {
    qqry: function(target, options) {
      //alert([options.field, options.text, options.index]);
      // 获取源参数
      var state = $.data(target, className), opts = state.options;
      // 数据定位
      if (opts.xmlData && $.isFunction(opts.xmlData.go)) {
        opts.xmlData.go(options.index);
      }
      // 触发 点击事件隐藏侧栏
      //$("#emisSidebar_switchPoint").trigger("click");
    }
  };

  // 默认参数
  $.fn.emisSidebar.defaults = {
    // 侧栏ID
    layerId: "emisSidebar",
    // 宽度
    width: 300,
    // 高度（-1则取最大高度）
    height: -1,
    // 默認高度（指示標高度）
    min_height: 57,
    // 上边距
    marginTop: 0,
    // 画面Tab的ID，用于计算上边距距离
    tabId: "tabs",
    // 是否启用侧栏，ture 启用， false 不启用
    used: true,
    // 是否启用动画， ture 启用， false 不启用
    animate: true,
    // 过滤规则：AND 或 OR
    filterRule: "AND",
    // 数据对象，通常为MasterTable
    xmlData: null,
    // 列表显示栏位设定，align 默认左对齐，width 默认不设定， qqry 可以组合多个栏位
    // ex:[{text:"商品编号",field:"P_NO",align:"left", width: 20, date:false, qqry:["S_NO","P_NO"]},
    //     {text:"建档日期",field:"CRE_DATE",align:"center", width: 20}]
    list:[],
    // 过滤条件栏位设定，ex:[{text:"商品编号",field:"P_NO",date:false},{text:"建档日期",field:"CRE_DATE",date:true}]
    filter:[],

    lang:{
      layer_bar_open_title: "展开",
      layer_bar_close_title: "收起",

      filter_head_title_Y: "资料列表",
      filter_head_title_N: "全选/取消(滤后{0}/共{1}笔)",
      filter_head_flag_Y: "显示过滤条件>",
      filter_head_flag_N: "隐藏过滤条件>",

      empty_data: "[空白]"
    },
    image: {
        open_side: basePath + "images/open_silder.png",
        close_side: basePath + "images/close_silder.png",
        open_filter: basePath + "images/none_1.jpg",
        close_filter: basePath + "images/none_2.jpg"
    },
    adapter: {
      DATE: function (options, value) {
        if (!value){
          return "";
        } else if (value.length >= 7){
          return value.substring(0, 7);
        }else{
          return value;
        }
      }
    }
  };

  /**
   * 获取本地化资源文本 
   * @param key
   */
  function i18n(key){
    return $.fn.emisSidebar.defaults.lang[key] || key;
  }
  /**
   * 获取图片资源
   * @param key
   */
  function img(key){
    return $.fn.emisSidebar.defaults.image[key] || key;
  }
  /**
   * 格式化字符串<br/>
   * 调用： format("全选/取消(滤后{0}/共{1}笔)", 10, 20);<br/>
   * 结果： 全选/取消(滤后10/共20笔)
   */
  function format() {
    var str = arguments[0],args = Array.prototype.slice.call(arguments, 1);
    return str.replace(/\{(\d+)\}/g, function(m, i) {
      return args[i];
    });
  }
  /**
   * 检查指定数组中是否包含指定元素
   * @param arrays
   * @param obj
   * @return {boolean}
   */
  function contains(arrays, obj) {
    var i = arrays.length;
    while (i--) {
      if (arrays[i] == obj) {
        return true;
      }
    }
    return false;
  }
  /**
   * 取数组唯一
   * @param arrays
   * @return {Array}
   */
  function unique(arrays) {
    if (typeof arrays == undefined) return [];
    arrays.sort();
    var re = [];
    for (var i = 0, len = arrays.length; i < len; i++) {
      if (!contains(re, arrays[i]))
        re.push(arrays[i]);
    }
    return re;
  }
  /**
   * 求两个数组的并集
   * @param a
   * @param b
   * @return {Array}
   */
  function intersect(a, b) {
    var re = [];
    for (var i = 0, len = a.length; i < len; i++) {
      if (b.contains(a[i])) re.push(a[i]);
    }
    return re;
  }
  /**
   * 初始化
   * @param options
   */
  function setup(target, options) {
    if (options.xmlData == undefined || options.xmlData.totalRowCount == 0) {
      // 2014/09/10 Joe 当没有数据时移除侧栏，解决无清空前交数据问题
      $("#"+options.layerId).remove();
      return false;
    }

    // 呼叫预处理
    prepare(target, options);
    //动态的创建一个层
    var $layer = $(generateLayerHtml(options).join(''));
    $layer.appendTo(target || "body");    

    // 初始化界面布局
    initLayout(options);
    
    // 显示记录数
    showFilterResultCount(options);
    // 固定表头
    fixedHead(options);
    // 注册过滤条件切换事件
    registerFilterSwitchEvent(options);
    resisterFilterChecobxEvent(options);
    
    // 注册侧栏开关事件
    registerSwitchEvent(options);

  }
  /**
   * 预处理
   * @param options
   */
  function prepare(target, options){
    $("#"+options.layerId).remove();
    expectation = [];
    var tops = options.marginTop || 37, $tabs = $("#" + options.tabId);
    if ($tabs.length > 0 ) {
      tops += $tabs.outerHeight() + 2;
    }
    options.marginTop = tops;
    // // 计算宽度
    var maxWidth = document.body.clientWidth;
    if (options.width == -1) {
      options.width = maxWidth;
    } else {
      options.width = Math.min(options.width, maxWidth);
    }
    // 计算高度(去掉 上边距，再去掉一点点)
    var maxHeight = document.body.clientHeight  - tops - 8;
    if (options.height == -1) {
      options.height = maxHeight;
    } else {
      options.height = Math.min(options.height, maxHeight);
    }
  }

  /**
   * 初始化界面布局
   * @param opts
   */
  function initLayout(opts){
    var $layer = $('#' + opts.layerId);
    var maxWidth = opts.width,
        maxHeight = opts.height,
        minHeight = opts.min_height;
    $layer.css({"height": maxHeight,"width": maxWidth + $("#emisSidebar_LayerBar").outerWidth() + 2, "left": -1 * (maxWidth + 2)});
    $("#emisSidebar_LayerData").css("width", maxWidth).css("height", maxHeight);
    var filterHeadHieght =  $("#emisSidebar_filterHead").css("width", maxWidth).height();
    $("#emisSidebar_mainDiv1").css({"width": maxWidth, "height": maxHeight - filterHeadHieght});
    $("#emisSidebar_side_data").css({"width": maxWidth, "height": maxHeight - filterHeadHieght});
    $("#emisSidebar_mainDiv2").css({"width": maxWidth, "height": maxHeight - filterHeadHieght});
    // 調整為默認高度，避免干擾表身新增和全選
    $layer.css({"height": minHeight});
  }

  /**
   * 注册侧栏开关事件
   * @param opts
   */
  function registerSwitchEvent(opts) {
    var $layer = $('#' + opts.layerId);
    var maxWidth = opts.width;
    var $emisSidebar_display_img_Y = $("#emisSidebar_display_img_Y");
    var $emisSidebar_display_img_N = $("#emisSidebar_display_img_N");

    // 表身是否已注册事件的检查标记
    var hasDetlEvent = false;
    // 显示
    var show = function(){
      if ($emisSidebar_display_img_N.is(":visible")) return;
      if (opts.animate) {
        $layer.animate({left: 0 , height: opts.height}, 400);
      } else {
        $layer.css({left: 0, height: opts.height });
      }
      $emisSidebar_display_img_Y.hide();
      $emisSidebar_display_img_N.show();
      $(this).attr("title", i18n('layer_bar_close_title'));
      // 注册表身点击时隐藏侧栏
      if(!hasDetlEvent && window.frames[0]){
        $(window.frames[0].document).click(function (e) {
          hide();
        });
        hasDetlEvent = true;
      }
    };
    // 隐藏
    var hide = function () {
      if ($emisSidebar_display_img_Y.is(":visible")) return;
      if (opts.animate) {
        $layer.animate({left: -(maxWidth + 2), height: opts.min_height }, 400);
      } else {
        $layer.css({left: -(maxWidth + 2), height: opts.min_height });
      }
      $emisSidebar_display_img_N.hide();
      $emisSidebar_display_img_Y.show();
      $(this).attr("title", i18n('layer_bar_open_title'));
    };
    // 注册点击事件进行切换
    $("#emisSidebar_switchPoint").click(function () {
      $emisSidebar_display_img_Y.is(":visible") ? show.apply(this) : hide.apply(this);
    }).attr("title", i18n('layer_bar_open_title'));

    // 注册点击侧栏以外的地方自动收起自动收起(表头)
    $(document).click(function (e) {
      if ($(e.target).closest(["#emisSidebar_LayerData","#emisSidebar_switchPoint"]).length == 0) hide();
    });
  }
  
  /**
   * 注册过滤条件事件
   * @param opts
   */
  function registerFilterSwitchEvent(opts){
    // 显示 过滤条件
    $("#emisSidebar_display_Y").click(function() {
      if(opts.animate){
        $("#emisSidebar_mainDiv1").slideUp();
        $("#emisSidebar_mainDiv2").slideDown();
      }else{
        $("#emisSidebar_mainDiv1").hide();
        $("#emisSidebar_mainDiv2").show();
      }
      $("#emisSidebar_filter_title_Y").hide();
      $("#emisSidebar_filter_flag_Y").hide();
      $("#emisSidebar_filter_title_N").show();
      $("#emisSidebar_filter_flag_N").show();
    });
    // 隐藏 过滤条件
    $("#emisSidebar_display_N").click(function() {
      if(opts.animate){
        $("#emisSidebar_mainDiv2").slideUp();
        $("#emisSidebar_mainDiv1").slideDown();
      }else{
        $("#emisSidebar_mainDiv2").hide();
        $("#emisSidebar_mainDiv1").show();
      }
      $("#emisSidebar_filter_title_N").hide();
      $("#emisSidebar_filter_flag_N").hide();
      $("#emisSidebar_filter_title_Y").show();
      $("#emisSidebar_filter_flag_Y").show();    
    });
  }
  /**
   * 注册过滤条件过滤事件
   * @param opts
   */
  function resisterFilterChecobxEvent(opts){
    var $filterTable = $("#emisSidebar_mainDiv2");
    $("#emisSidebar_chkAllSS").click(function() {
      $filterTable.find(":checkbox").attr("checked", this.checked);
      refreshDataList(opts);
    });
    $filterTable.find("th :checkbox[ref^='#emisSidebar_']").click(function() {
      $($(this).attr("ref")).find(":checkbox").attr("checked", this.checked);
      refreshDataList(opts);
    });
    $filterTable.find("td :checkbox").change(function(){
      refreshDataList(opts);
    });
  }
  /**
   * 刷新数据列表
   * @param opts
   */
  function refreshDataList(opts) {
    var $filterTable = $("#emisSidebar_mainDiv2");
    var group, groups = [], empty = true;
    $filterTable.find("td").each(function(i, ele) {
      group = [];
      $(ele).find(":checkbox:checked").each(function(k, elm){
        group = group.concat($(elm).attr("rows").split(","));
      });
      if(group.length > 0){
        empty = false;
        groups.push(unique(group));
      }
    });

    if (groups.length > 0) {
      var gp = groups[0];
      if (groups.length > 1) {
        var i , len;
        if (/AND/i.test(opts.filterRule)) {
          for (i = 1, len = groups.length; i < len; i++) {
            // 求交集
            gp = intersect(gp, groups[i]);
            // 空集直接结束
            if(gp.length == 0) break;
          }
        }else{
          for (i = 1, len = groups.length; i < len; i++) {
            // 求并集
            gp = gp.concat(groups[i]);
          }
        }
      }
      // 去重复
      gp = unique(gp);
      expectation = (gp.length == 0 ? [-1] : gp);
    } else {
      expectation = [-1];
    }
    // 重新生成数据
    var $newData = $(generateDataTable(opts).join(""));
    $("#emisSidebar_mainDiv1").replaceWith($newData);

    // 重算高度
    var maxWidth = opts.width,maxHeight = opts.height, 
        filterHeadHieght =  $("#emisSidebar_filterHead").css("width", maxWidth).height();
    $("#emisSidebar_mainDiv1").css({"width": maxWidth, "height": maxHeight - filterHeadHieght});
    $("#emisSidebar_side_data").css({"width": maxWidth, "height": maxHeight - filterHeadHieght});
    // 固定表头处理
    fixedHead(opts);
    // 立刻隐藏，避免显示两个层
    $newData.hide();
    // 显示过滤后记录数
    showFilterResultCount(opts);
  }
  /**
   * 修复IE6层被穿透问题
   */
  function fixedLayerForIE6() {
    //<iframe style="position:absolute;z-index:expression(this.nextSibling.style.zIndex-1);width:expression(this.nextSibling.offsetWidth);height:expression(this.nextSibling.offsetHeight);top:expression(this.nextSibling.offsetTop);left:expression(this.nextSibling.offsetLeft);" frameborder="0"></iframe>
    var $frame = $('<iframe style="position:absolute;" frameborder="0"></iframe>');
    var $layerData = $("#emisSidebar_LayerData");
    var offset = $layerData.offset();
    $frame.css({
      "z-index": ($layerData.css("z-index") || 0) * -1,
      "width": $layerData.css("width"),
      "height": $layerData.css("height"),
      "top": offset.top,
      "lef": offset.left
    });
    $frame.insertBefore($layerData);
  }
  /**
   * 获取滚动条宽度
   */
  function getScrollbarWidth() {
    var parent, child, width;

    if (width === undefined) {
      parent = $('<div style="width:50px;height:50px;overflow:auto"><div></div></div>').appendTo('body');
      child = parent.children();
      width = child.innerWidth() - child.height(99).innerWidth();
      parent.remove();
    }

    return width;
  }
  /**
   * 显示过滤后记录数
   * @param opts
   */
  function showFilterResultCount(opts) {
    // 数据源表
    var $source = $("#emisSidebar_side_data").find("table");
    var row_count = $source.find("tbody tr").length,
        total_row = (opts.xmlData && opts.xmlData.totalRowCount) || 0;
    $("#emisSidebar_filter_title_N").find("span").text(format(i18n('filter_head_title_N'), row_count, total_row));
  }
  /**
   * 重算固定表头
   * @param opts
   */
  function fixedHead(opts) {
    // 移除固定表头
    $("#emisSidebar_fixedHead").remove();
    var $sideData = $("#emisSidebar_side_data");
    // 数据源表
    var $source = $sideData.find("table");

    // 检查是否有纵向滚动条
    var hasVerticalScroll = ($sideData[0].scrollHeight - $sideData[0].clientHeight) > 0;
    // 无纵向滚动条直接结束处理
    if (!hasVerticalScroll) return;
    
    var maxWidth = opts.width;

    // 动态生成固定表头
    var headHtml = '<div id="emisSidebar_fixedHead" style="position:relative;border: 0 solid #b8d0d6;border-right-width: 1px;overflow:hidden;"></div>';
    var $divHeader = $source.clone().find("tbody").remove().end().wrap(headHtml).parent();
    $divHeader.insertBefore($sideData);
    // 设置右边距，空出滚动条的位置
    // TODO:这里要再想想是否需要这样处理？？
    if ($.browser.msie) {
      $divHeader.css("width", maxWidth);
      $divHeader.css("padding-right", getScrollbarWidth());
    } else {
      $divHeader.css("margin-right", getScrollbarWidth());
    }

    // 表头表
    var $target = $divHeader.find("table");
    // 数据源表头
    var $th = $source.find("th");
    // 数据源表头上边距（负数）为配合固定表头处理
    var headHeight = $divHeader.outerHeight();
    // 设置数据表高度（减掉源表头高度）
    $source.css("margin-top", headHeight * -1).find("thead").css("visibility", "hidden");
    // 减掉动态增加的固定表头的高度
    $sideData.css("height", $sideData.height() - headHeight);

    // 设置同步滚动条
    $sideData.bind("scroll", function() {
      $divHeader[0].scrollLeft = this.scrollLeft;
    });

    // 设定表头宽度与数据表同步
    $target.css("width", $source.outerWidth());

    // 依真实表头宽度设置显示的表头宽度
    $target.find("th").each(function(i, th) {
      // 设置宽度加1，不加会错位
      $(this).css("width", $th.eq(i).width() + 1);
    });
  }

  /**
   * 获取当前最高的zIndex
   * @return {Number}
   */
  function topZindex() {
    // 获取 表单式 表头 z-Index
    var headZ = $("div.headPanel").css('z-index');
    // 获取 主层 z-Index
    var mainZ = $("div.mainPanel").css('z-index');
    var maxZ = Math.max.apply(null, $.map($('body > *'), function (e, n) {
      if (e.nodeName && $(e).css('position') == 'absolute')
        return parseInt($(e).css('z-index') || 0, 10) || 1;
    }));
    maxZ = (typeof headZ == "undefined" ? (typeof mainZ == "undefined" ? maxZ : mainZ) : headZ);
    return Math.max(0, maxZ || 0);
  }
  /**
   * 生成侧栏主层
   * @param opts
   */
  function generateLayerHtml(opts) {
    var top = opts.marginTop,left = -1 * opts.width,
        width = opts.width, height = opts.height;
    var html = [];
    html.push('<div id="' + opts.layerId + '" style="position:absolute;z-index:'+(topZindex() + 1)+';left:' + left + ';top:' + top + 'px;width:' + (width + 17) + 'px;height:' + height + 'px;" onselectstart="return false">');
    html.push('  <div id="emisSidebar_LayerData" style="z-index:2;width:' + width + 'px;height:100%;text-align:center;float:left;border: 1px solid #b8d0d6;">');
    html = html.concat(generateFilterHeadHtml(opts));
    html = html.concat(generateDataTable(opts));
    html = html.concat(generateFilterTable(opts));
    html.push('  </div>');
    html.push('  <div id="emisSidebar_LayerBar" style="z-index:3;width:15px;height:100%;float:right;">');
    html.push('    <div id="emisSidebar_switchPoint" style="background:#3FF;padding:20px 0 20px 3px;cursor: pointer;" title="Open">');
    html.push('      <img id="emisSidebar_display_img_Y" src="' + img("open_side") + '" alt="" title=""/>');
    html.push('      <img id="emisSidebar_display_img_N" src="' + img("close_side") + '" style="display: none;" alt="" title=""/>');
    html.push("    </div>");
    html.push("  </div>");
    html.push("</div>");
    return html;
  }
  /**
   * 生成过虑标题栏
   * @param opts
   */
  function generateFilterHeadHtml(opts){
    var html = [];
    html.push('<div id="emisSidebar_filterHead" style="vertical-align: middle;position:relative;width:100%;background:#b8d0d6;border:0 solid #b8d0d6;">');
    html.push('  <table class="detail_data" cellspacing="0" cellpadding="0" border="0" style="width:100%;">');
    html.push('    <thead>');
    html.push('    <tr>');
    html.push('      <th>');
    html.push('        <div style="float:left">');
    html.push('          <div id="emisSidebar_filter_title_Y" style="padding-top: 2px;padding-left: 5px;">' + i18n('filter_head_title_Y') + '</div>');
    html.push('          <div id="emisSidebar_filter_title_N" style="padding-top: 2px;display: none">');
    html.push('            <input type="checkbox" id="emisSidebar_chkAllSS" checked="true" style="vertical-align: middle;"><span style="vertical-align:middle;">' + i18n('filter_head_title_N') + '</span>');
    html.push('          </div>');
    html.push('        </div>');
    html.push('        <div style="float:right;">');
    html.push('          <div id="emisSidebar_filter_flag_Y" style="padding-top: 2px;">');
    html.push('            ' + i18n('filter_head_flag_Y') + '<img id="emisSidebar_display_Y" style="cursor: pointer"');
    html.push('                        src="' + img("open_filter") + '" width="16" height="12" title="" alt="">');
    html.push('          </div>');
    html.push('          <div id="emisSidebar_filter_flag_N" style="padding-top: 2px;display: none;">');
    html.push('            ' + i18n('filter_head_flag_N') + '<img id="emisSidebar_display_N" style="cursor: pointer;"');
    html.push('                        src="' + img("close_filter")  + '" width="16" height="12" title="" alt="">');
    html.push('          </div>');
    html.push('        </div>');
    html.push('      </th>');
    html.push('    </tr>');
    html.push('    </thead>');
    html.push('  </table>');
    html.push('</div>');
    return html;
  }
  /**
   * 生成数据表格
   * @param opts
   */
  function generateDataTable(opts){
    var html = [];
    html.push('<div id="emisSidebar_mainDiv1" style="position:relative;background:#fff;width:100%;height:95%;" onselectstart="return false">');
    html.push('  <div id="emisSidebar_side_data" style="height: 100%; border: 0 solid blue; overflow:auto; ">');
    html.push('    <table class="detail_data" cellspacing="0" cellpadding="0" border="1" style="width: 100%;">');
    html.push('      <thead>');
    html.push('      <tr>');
    html.push('        <th nowrap="true" style="white-space: nowrap; text-align: center;">' + i18n("recno_title") + '</th>');
    // 动态生成
    $.each(opts.list, function(i, item) {
      html.push('        <th nowrap="true" style="white-space: nowrap; text-align: center;' +
                (item.width ? ('width:' + item.width + 'px;') : '') + '">' + item.text + '</th>');
    });
    html.push('      </tr>');
    html.push('      </thead>');
    html.push('      <tbody>');
    // 动态生成
    html = html.concat(generateDetailData(opts));
    html.push('      </tbody>');
    html.push('    </table>');
    html.push('  </div>');
    html.push('</div>');
    return html;
  }
  /**
   * 生成明细数据
   * @param opts
   */
  function generateDetailData(opts) {
    if (expectation.length == 1 && expectation[0] == -1) {
      //expectation = [];
      return;
    }
    var masterTbl = opts.xmlData;
    var len = (expectation.length == 0 ? masterTbl.totalRowCount : expectation.length),
        rows = new Array(len),
        rowIndex = 0;
    var row, i, qqry, align, tmp, click = "$(document.body).emisSidebar('qqry',{field:'{0}',text:'{1}', index:{2}})";
    for (i = 0; i < len; i++) {
      // 依 expectation 指定下标控制显示资料行，expectation时直接显示所有数据
      rowIndex = (expectation.length == 0 ? i : expectation[i]);
      row = [];

      row.push(i % 2 == 0 ? '<tr>' : '<tr class="odd">');
      // 序号
      row.push('  <td nowrap="true" style="white-space:nowrap;">' + (i + 1) + '</td>');

      // 选定栏位
      $.each(opts.list, function(j, item) {
        // 处理对齐方式，默认左对齐
        align = item.align || "left";
        row.push('  <td nowrap="true" style="white-space:nowrap;text-align:' + align + '">');

        // 栏位内容处理
        tmp = dataParse(item, masterTbl.get(rowIndex, item.field));

        // 跳转栏位处理
        if (item.qqry) {
          // 转换为数组
          if (!$.isArray(item.qqry)) {
            item.qqry = [item.qqry];
          }
          // 清空并储存新的内容
          qqry = [];
          $.each(item.qqry, function(k, field) {
            qqry.push(masterTbl.get(rowIndex, field));
          });

            // 链接事件处理
          row.push('<a href="javascript:void(0);" style="color:blue;text-decoration:none;"' +
                   ' onclick="' + format(click, item.field, qqry.join(""), rowIndex) + '">' + tmp + '</a>');
        }
        // 纯显示栏位处理
        else {
          row.push(tmp);
        }
        row.push('  </td>');
      });
      row.push('</tr>');
      rows.push(row.join(""));
    }
    return rows;
  }
  /**
   * 生成过滤数据表格
    * @param opts
   */
  function generateFilterTable(opts){
    var masterTbl = opts.xmlData;
    var html = [], datas, j, len, tmp;
    // 数据预处理，把各条件对应的记录下标缓存起来
    for (j = 0,len = masterTbl.totalRowCount; j < len; j++) {
      $.each(opts.filter, function(i, item) {
        // 栏位处理(默认有日期)
        tmp = (dataParse(item, masterTbl.get(j, item.field)) || "").replace(/(^[\s　]*)|([\s　]*$)/g, "");
        if (!item.rows) item.rows = {};
        if (!item.rows[tmp]) item.rows[tmp] = [];
        // 记录资料行下标值
        item.rows[tmp].push(j);
      });
    }
    html.push('<div id="emisSidebar_mainDiv2" style="position:relative;background:#fff;width:100%;height:95%;display:none;overflow:auto" onselectstart="return false">');
    html.push('  <div id="emisSidebar_side_data2" style="height: 100%; border: 0 solid blue; overflow:auto; ">');
    html.push('    <table class="detail_data" cellspacing="0" cellpadding="0" border="1" style="width: 100%;">');
    html.push('      <thead>');
    // 选定栏位
    $.each(opts.filter, function(i, item) {
      html.push('      <tr>');
      html.push('        <th nowrap="true" style="white-space: nowrap; text-align: left;">');
      html.push(format('    <input type="checkbox" checked="true" ref="#emisSidebar_{0}" style="vertical-align: middle;">', item.field));
      html.push(format('    <span style="vertical-align: middle;">{0}</span>', item.text));
      html.push('        </th>');
      html.push('      </tr>');
      html.push('      <tr>');
      html.push(format('  <td align="left" id="emisSidebar_{0}">', item.field));
      // 生成过滤条件 start
      datas = [];
      for(tmp in item.rows){
        datas.push(tmp);
      }

      // 升序排序
      datas.sort(function (a, b) {
        return a > b ? 1 : -1;
      });

      item.cols = item.cols || 3;
      item.width = item.width || (Math.floor((opts.width - 20 * item.cols) / item.cols) - 20);
      // 输出HTML
      html.push("<div>");
      for (j = 0, len = datas.length; j < len; j++) {
        if (j > 0 && j % item.cols == 0){
          html.push("</div><div>");
        }
        html.push(format('    <input type="checkbox" checked="true" style="vertical-align: middle;" rows="{0}">', item.rows[datas[j]].join(",")));
        html.push(format('    <span style="vertical-align: middle;width:{0}px;display:inline-block;white-space:nowrap;text-overflow:ellipsis;overflow: hidden;" title="{1}">{1}</span>', item.width, datas[j]));
      }
      html.push("</div>");
      // 生成过滤条件 end      
      html.push('        </td>');
      html.push('      </tr>');
    });
    html.push('      </thead>');
    html.push('    </table>');
    html.push('  </div>');
    html.push('</div>');
    return html;
  }

  /**
   * 数据解释
   * @param item
   * @param value
   * @return {*}
   */
  function dataParse(item, value) {
    var tmp = item.date ? "DATE" : item.field;
    var func = item.handler || $.fn.emisSidebar.defaults.adapter[tmp];
    var re = value;
    if ($.isFunction(func)) {
      re = func(item, value);
    }
    return re || i18n("empty_data");
  }
})(jQuery);