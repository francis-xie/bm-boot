/*
 *dana.gao 2009/07/02 增加 页面数据搜索 功能控制参数 detlSearchStatus 默认为开启状态,
 *关闭只需在页面上设置变量 detlSearchStatus = false即可
 *fang 2009/08/27 表身无资料时失焦会出错,增加try语法
 * Track+[18716] dana 2011/12/01 修正多笔式搜索框不能重复搜索问题.
 * 2012/05/22 Joe 重构整理，多浏览器实现
 */
var detlSearchStatus = true;
var oDetlSearchContainer = null; //可指定搜索框显示位置
(function ($) {
  var searchstate = false;//数据是否处于搜索状态
  var popHintBox, searchTable, orig_mode;

  /**
   * 创建搜索文本框
   * @param oContainer
   * @return {*}
   */
  function createSearchBox(oContainer) {
    var elem = document.createElement("INPUT");//创建搜索框
    oContainer.appendChild(elem);//追加搜索框到第一行第一个单元格
    oContainer.removeAttribute('noWrap');
    //设置搜索框属性
    elem.setAttribute("id", "ajax_detlSearch_Id");
    elem.setAttribute("title", $.getMessage('ajax_search_title'));
    elem.setAttribute("value", $.getMessage('ajax_search_value'));
    elem.setAttribute("size", 12);
    elem.setAttribute("accessKey", "a");
    elem.setAttribute("class", "detlSearch");       // FF
    elem.setAttribute("className", "detlSearch");  // IE
    elem.setAttribute("disableautocomplete", "disableautocomplete"); // FF
    elem.setAttribute("autocomplete", "off");                            // IE
    return elem;
  }

  /**
   * 键盘事件过滤
   * @param e
   * @return {Boolean}
   */
  function keyEventFilter(e) {
    var ev = e || event;
    var keyCode = ev.which || ev.keyCode;
    var result = true;
    switch (keyCode) {
      case kbrd.KEY_DEL :
        // 終止冒泡
        ev.stopPropagation();
        break;
      case kbrd.KEY_ENTER :
        // 取消默認事件响应
        e.preventDefault();
        result = false;
        break;
    }
    return result;
  }

  /**
   * 检查是否有提示控件，没有则创建，然后显示提示信息
   * @param searchbox
   */
  function hintSearching(searchbox) {
    popHint(searchbox, $.getMessage('ajax_search_searching'), {_event:'blur'});
    popHintBox = $_('popHintText');
  }

  /**
   * 初始化搜索文本框的事件处理逻辑
   * @param searchbox
   */
  function initSearchBoxEvent(searchbox) {
    var $searchbox = $(searchbox);

    // 搜索框回车事件触发搜索动作
    $searchbox.keydown(function (e) {
      if (keyEventFilter(e))
        return true;
      if ($searchbox.data('lastSearch') != this.value) {
        doSearch($searchbox);
      }
      return false;
    });
    // 搜索框获得焦点事件
    $searchbox.focus(function (e) {
      if (this.value == $.getMessage('ajax_search_value')) {
        this.value = "";
        // 避免第一次无数据时产生空行
        $(this).data('lastSearch',"");
      }
      $searchbox.css({ color:"#000" });
    });
    // 搜索框失去焦點的事件
    $searchbox.blur(function (e) {
      // 如果最后一次搜索的值与当前值不一致时触发搜索
      if ($searchbox.data('lastSearch') != this.value) {
        doSearch($searchbox);
      }
      if ($.trim(this.value) == "") {
        // 如果当前值为空时，显示提示信息
        $searchbox.val($.getMessage('ajax_search_value'));
      }
      $searchbox.css({ color:"#ccc" })
    });
  }

  /**
   * 展示搜索过滤后的表格
   * @param newRows
   */
  function showTable(newRows) {
    searchTable.rows = newRows;
    // 延用最后的排序规则
    if(searchTable.lastOrderAction){
      searchTable.__makeDomOrder(searchTable.lastOrderAction);
    }
    // 加载页面
    searchTable.__loadPage();
    // 光标定位于第一条
    if (newRows.length > 0) {
      searchTable.simulate_click(0);
    }
  }

  /**
   * 显示搜索百分比
   * @param current
   * @param totals
   */
  function showSearchPercent(current, totals) {
    //設置搜索百分比
    window.setTimeout(function () {
      var percent = Math.ceil((current + 1) / totals * 100);
      popHintBox.innerHTML = $.getMessage('ajax_search_percent') + (percent > 100 ? 100 : percent) + "%";
    }, 0);
  }

  /**
   * 显示搜索完成信息，并于一定时间后消失
   */
  function showSearchFinish() {
    var ico = "<span class='popIcon right'></span>";
    window.setTimeout(function () {
      popHintBox.innerHTML = ico + $.getMessage('ajax_search_finish');
    }, 2);
    window.setTimeout(function () {
      if ($_("popHint"))$_("popHint").style.display = "none";
    }, 1500);
  }

  /**
   * 显示搜索完成无数据信息，并于一定时间后消失
   */
  function showSearchNoDataFound() {
    var ico = "<span class='popIcon wrong'></span>";
    window.setTimeout(function () {
      popHintBox.innerHTML = ico + $.getMessage('ajax_search_no_data_found');
    }, 2);
    window.setTimeout(function () {
      if ($_("popHint")) $_("popHint").style.display = "none";
    }, 2000);
  }

  /**
   * 执行搜索
   * @param $searchbox
   * @param nohint
   */
  function doSearch(/*jQuery Object*/ $searchbox, nohint) {
    if (!searchstate && $searchbox.val() != $.getMessage('ajax_search_value')) {
      if ($searchbox.val() != "" && !nohint)
        hintSearching($searchbox[0]);
      searchstate = true;// 标记数据处于搜索状态
      searching($searchbox.val(), nohint);
      $searchbox.data('lastSearch', $searchbox.val());
    }
  }

  /**
   * 搜索
   * @param words
   * @param nohint
   */
  function searching(words, nohint) {
    if(typeof searchTable == "undefined" || typeof searchTable.LastDataXml == "undefined"){
      showSearchNoDataFound();
      searchstate = false;
      return;
    }
    // 2013/03/26 Joe 修正空数据集时显示空行的错误
    var root = searchTable.LastDataXml.getElementsByTagName("data");
    var isEmpty = /true/i.test(root[0].getAttribute("empty"));
    // 无数据时给一个空数组
    var dom_rows = isEmpty ? [] : searchTable.LastDataXml.getElementsByTagName("_r");
    if (words == "") {
      initHighlight(words);
      // 此处必需定义一个新的数据来接收Dom数组，否则排序会无效
      var newRows = [];
      copyArray(newRows, dom_rows);
      showTable(newRows);
    } else {
      var reg_val = words.replace(/([\\\(\)\[\]\+\*\^\$\?\.])/ig, '\\$1'), reg = new RegExp(reg_val, "ig");
      var newRows = [], fields = searchTable.databind.dataFlds,fld2Objs = searchTable.databind.fld2Objs;
      var row, cell, field, index;
      for (var i = 0, len = dom_rows.length; i < len; i++) {
        row = dom_rows[i];
        for (var j = 0, size = fields.length; j < size; j++) {
          field = fields[j];
          index = searchTable.field2idx[field];
          // 20161220 Joe 排除Hideen控件，即只过滤可见控件对象
          if (!isVisible(fld2Objs, field)) continue;
          cell = row.childNodes[index];
          if (cell && (cellvalue(cell) || "").search(reg) > -1) {
            newRows.push(row);
            break;
          }
        }
        if (!nohint)
          showSearchPercent(i, len);
      }
      if (newRows.length > 0) {
        initHighlight(words);
        showTable(newRows);
      }
      if (!nohint){
        if (newRows.length == 0) {
          showSearchNoDataFound();
        }else{
          showSearchFinish();
        }
      }
    }
    searchstate = false;// 标记数据处于非搜索状态
  }
    /**
     * 检查控件对象是否可见
     * @param objs
     * @param field
     * @returns {boolean}
     */
    function isVisible(objs, field) {
        if (objs) {
            var elms = objs[field] || [];
            var hideCount = 0;
            for (var i = 0; i < elms.length; i++) {
                try {
                    if (/hidden/gi.test(elms[i].type))
                        hideCount++;
                } catch (e) {
                }
            }
            return elms.length != hideCount;
        }
        return true;
    }

  // 用于缓存现有 PageLoadedCallback 事件，以便附加新的事件逻辑
  var cachePageLoadedCallback = null;

  /**
   * 重写 PageLoadedCallback 事件，实现高亮显示搜索关键词
   * @param words
   */
  function initHighlight(words) {
    // check event
    if (cachePageLoadedCallback == null) {
      if (typeof searchTable["pageLoadedCallback"] == "function") {
        // cache event
        cachePageLoadedCallback = searchTable.pageLoadedCallback;
      } else {
        // generate a empty event
        cachePageLoadedCallback = function () {
        };
      }
    }
    // override the event
    searchTable["pageLoadedCallback"] = function () {
      cachePageLoadedCallback();
      var target = "#" + searchTable.id + " tbody";
      if (words != "") {
        $(target).highlight(words);
      } else {
        $(target).unhighlight(words);
      }
    }
  }

  $(document).ready(function () {
    if (!detlSearchStatus) return;
    if (typeof (currentTable) != "undefined" && currentTable.detlTable == null ) { //多笔式作业才增加此功能.
      searchTable = currentTable;
      var oContainer, idTable = $('.detail_data')[0];

      if (idTable) {  // 检查是否有样式为detail_data的表格
        idTable = $('.toolbar table')[0];
        if (oDetlSearchContainer || (idTable && idTable.rows[0] && idTable.rows[0].cells[0])) {
          oContainer = (oDetlSearchContainer)?oDetlSearchContainer:idTable.rows[0].cells[0];
          if (oContainer) {
            var searchbox = createSearchBox(oContainer);
            initSearchBoxEvent(searchbox);
            window.AjaxMtnAfterSearch = function () {
              doSearch($(searchbox), true);
            }
          }
        }
      }
    }
  });
})(jQuery);