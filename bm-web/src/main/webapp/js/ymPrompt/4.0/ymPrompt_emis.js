//$Id: ymPrompt_emis.js 13350 2018-12-18 08:39:42Z andy.he $
jQuery(document).ready(function () {
  ymPrompt.setDefaultCfg({maskAlpha: '0.2'});
  // 重新绑定关闭事件; 20161011 Joe Fix: 当窗体为Open方式打开时，原判断方式不准，导致关闭失效。
  //if (typeof parentYmPrompt != "undefined" && (parent !== top || (parent === top && !!parent.opener))) {
  //暂不判断parent（因如果是从前台按钮开启后台功能时点取消会有问题。）
  if (typeof parentYmPrompt != "undefined" && (parent !== top || (parent === top && !!parent.opener) || emisEmpty(_sDwzWinid))) {
    jQuery('#btnClose,:button.ExitButton').unbind("click").bind('click', function (ev) {
      /*try{
       (parentYmPrompt||ymPrompt).doHandler("close");
       }catch(e){
       (parentYmPrompt||ymPrompt).close();
       }*/
      try {
        emisCloseDialog();
      } catch(e){}
    });
  }
});

// 用于辅助YmPrompt开窗时获取表身
function getDetlWindow() {
  return parent.__detl_window;
}
function setDetlWindow(win) {
  parent.__detl_window = win;
}

// 打开ymPrompt视窗
function emisShowDialog(opts) {
  // 无参数或无url且无内容，不做任何处理
  if (!opts || (!opts.url && !opts.msg)) return;
  var op = jQuery.extend({}, opts);
  delete op["url"];
  delete op["msg"];
  delete op["max"];
  delete op["showHeadMask"];
  delete op["detlWindow"];

  if (opts.url) {
    opts.url += /\?/g.test(opts.url) ? "&ymprompt=1" : "?ymprompt=1";
    op.message = opts.url.replace(/\'/g,"&#39;"); //将单引号转码，不然参数值有单引号时会传值不到调用页面
    op.iframe = true;
  } else if (opts.msg) {
    op.message = opts.msg;
  }
  if (opts.width) op.width = Math.min(opts.width, document.body.clientWidth);
  if (opts.height) op.height = Math.min(opts.height, document.body.clientHeight);

  // 暂时关闭所有标题，等有空做不同皮肤时再把此行注解，开放其下一行，依标题控制是否显示
  op.titleBar = !!(opts.titleBar === true);
  //    op.titleBar = !!op.title;
  //    op.maxBtn = true;
  //    op.minBtn = true;
  if (opts.detlWindow) {
    window.__detl_window = opts.detlWindow;
  }
  if (opts.showHeadMask) {

    /*if (parent && parent.emisMask) {
     parent.emisMask.show(true);
     var _parent = parent;
     if (op.handler) {
     var _handler = op.handler;
     op.handler = function (act) {
     _parent.emisMask.hide();
     _handler(act);
     }
     } else {
     op.handler = function (act) {
     _parent.emisMask.hide();
     }
     }
     }*/
  }
  ymPrompt.win(op);
  if (opts.max == true)
    ymPrompt.max();
}

/**
 * 以Submit方式打开ymPrompt视窗
 * opts{url:"target.jsp", name:"myWin", form:document.forms[0], width:300, height:200}
 */
function emisSubmitDialog(opts) {
    // 无参数或无url且无内容，不做任何处理
    if (!opts || !opts.url) return;
    var sURL = opts.url, sName = opts.name, oForm = opts.form, iWidth = opts.width, iHeight = opts.height;
    // -1表示开最大视窗
    if (iWidth == -1) iWidth = document.availWidth;
    if (iHeight == -1) iHeight = document.availHeight;
    var width = iWidth || 300, height = iHeight || 100;

    var op = jQuery.extend({}, opts);
    delete op["url"];
    delete op["form"];
    delete op["name"];
    op["iframe"] =  {id: sName, name: sName, src: 'about:blank'};
    // 暂时关闭所有标题，等有空做不同皮肤时再把此行注解，开放其下一行，依标题控制是否显示
    op["titleBar"] = !!(opts.titleBar === true);
    // 开启窗体
    ymPrompt.win(op);
    // 无传入 Form Object 则以第一个 Form
    var _oForm = oForm || document.forms[0];
    // sName 以 Random 产生, 以避免会不同作业会开同一视窗
    sName = sName || (Math.floor(Math.random() * 10000000000) - 0) + "";
    var _sAction = _oForm.action;
    var _sTarget = _oForm.target;
    _oForm.action = sURL;
    _oForm.target = sName;
    _oForm.submit();
    // 还原 action, target
    _oForm.action = _sAction;
    _oForm.target = _sTarget;
} // emisOpen()
/**
 * 关闭ymPrompt视窗
 * 默认取第一个参数为回传参数，无时给ymPrompt回传close,给Window回传空值
 */
function emisCloseDialog() {
  var _sRet = arguments[0] || "close";
  if (window.dialogArguments) {
    window.returnValue = (_sRet == 'close' ? "" : _sRet);
    self.close();
  } else {
    try {
      parentYmPrompt.doHandler(_sRet);
    } catch (e) {
    }
  }
}
/**
 * 2013/10/28 Joe Add
 * 实现在已开启的窗体再开另一个窗体，以适应特别需求
 * 原理：在原弹出层调用这个方法，并传入参数parent指向该层的父窗体（即开出层的源头窗体）
 * @param opts
 */
function emisShowSubDialog(opts) {
  if (!opts) return;
  if (opts.parent && opts.parent.document && opts.parent.document.body) {
    var iWidth = opts.width
    var iHeight = opts.height;
    var parent = opts.parent;
    var hasTitle = false;
    // 缓存当前层的大小
    var iBodyOldWidth = document.body.clientWidth;
    var iBodyOldHeight = document.body.clientHeight;
    // 得到父頁面大小
    var iMaxWidth = parent.document.body.clientWidth;
    var iMaxHeight = parent.document.body.clientHeight;
    // 调整窗体大小在有效范围内
    iWidth = Math.min(iWidth, iMaxWidth);
    iHeight = Math.min(iHeight, iMaxHeight);
    // 检查是否有标题栏，以利控制窗体大小
    var $title = opts.parent.jQuery("#ym-tl").find("div.ym-tc");
    if (!$title.hasClass("ym-ttc")) {
      hasTitle = true;
      if (!!opts.title) $title.addClass("ym-ttc");
    }
    // 緩存原回調函數，实现回调重写
    var orig_handler = opts.handler;
    opts.handler = function (_aRetStr) {
      if (hasTitle) {
        var $title = opts.parent.jQuery("#ym-tl");
        $title.find("div.ym-tc").removeClass("ym-ttc");
        iBodyOldHeight = (iBodyOldHeight || 0) - 0 + $title.height();
      }
      //還原窗體
      parent.ymPrompt.resizeWin(iBodyOldWidth, iBodyOldHeight);
      //調用原回調函數，把數組返回至頁面
      if (jQuery.isFunction(orig_handler)) {
        orig_handler(_aRetStr);
      }
    };
    //放大窗體，以适应新开的大小
    parent.ymPrompt.resizeWin(Math.max(iBodyOldWidth, iWidth), Math.max(iBodyOldHeight, iHeight));
  }
  //調用開窗方式
  emisShowDialog(opts);
}
/**
 * 三點按鈕時調用方法
 * @param opts
 * @returns {*}
 * @constructor
 */
function AjaxSelectByYmPrompt(opts) {
  if (!opts) return;
  var sURL, setComponent, oNoDataFocus, oNextFocus, sMsg, iWidth, iHeight, fncSelCallback, targetTable, sNoDataMsg;

  var iBodyOldWidth = document.body.getAttribute("cacheWidth") || document.body.clientWidth;
  var iBodyOldHeight = document.body.getAttribute("cacheHeight") || document.body.clientHeight;
  document.body.setAttribute("cacheWidth", iBodyOldWidth);
  document.body.setAttribute("cacheHeight", iBodyOldHeight);

  if(!opts.noCheckUserType) {
    if (formobj("AC_USER_ID")) {
      opts.url += "&AC_USER_ID=" + formobj("AC_USER_ID").value;
    }
  }
  var hasResize = false, hasTitle = false;
  sURL = opts.url;
  setComponent = opts.setcomponent;
  oNoDataFocus = opts.nodatafocus;
  oNextFocus = opts.nextfocus;
  sMsg = opts.msg;
  sNoDataMsg = opts.noDataMsg;  // 例外的无资料时的消息提示。
  iWidth = opts.width;
  iHeight = opts.height;
  fncSelCallback = opts.callback || function () {
  };
  targetTable = opts.targetTable;
  if (typeof targetTable == "undefined" && typeof currentTable != "undefined")
    targetTable = currentTable;
  // ? oNoDataFocus 即?入之欄位有被改為 readOnly 或 Disable, 則不?理
  if (oNoDataFocus.readOnly || oNoDataFocus.disabled)
    return true;
  //  Add By Chitty 2013/01/30 增加detlWindow判斷用於 ，F10 與整批修改時三點按鈕綁定值
  if (opts.detlWindow)
    setDetlWindow(opts.detlWindow);

  iWidth = emisEmpty(iWidth) ? 760 : iWidth;
  iHeight = emisEmpty(iHeight) ? 520 : iHeight;

  var _aRetStr = new Array();
  var _oWin;
  // 若不傳入會取 select.xml
  var _selectURL = sRoot + "/jsp/ajax_sel_ymprompt.jsp?" + encodeParam(sURL);


  if (!do_ajax_request(sURL)) {
    return fncSelCallback(false);
  }
  //	if( my_check_error( request.responseXML) ) {
  if (my_check_error(parseXmlDom(request))) {
    return fncSelCallback(false);
  }
  //	AjaxSelectXML = request.responseXML;
  AjaxSelectXML = parseXmlDom(request);
  var emptyFlag = "?????";
  var datas = AjaxSelectXML.getElementsByTagName('data');
  if (datas.length > 0) {
     // 先清除一下输入的原始值，因为如果是区间时，在第一个栏位输入一个不完整的值，按回车或Tab键操作时，会将输入的原始值带入到第二个栏位中，而不是带入正确查询出来的值。
    if(opts.nodatafocus){
      opts.nodatafocus.value = "";
    }
    var empty = datas[0].getAttribute("empty");
    if (empty == "true") {
      _aRetStr[0] = emptyFlag;
      selectcallback(_aRetStr);
    } else {
      var rows = AjaxSelectXML.getElementsByTagName("_r");
      // 2013/07/23 Joe 当有强制开启(always:true)视窗开关时，有一笔记录也要显示视窗，以满足特别需求
      if (rows.length > 1 || opts.always) {
        // 開窗
        if (emisEmpty(bDebug_)) {
          //_aRetStr = emisShowModal(_selectURL, iWidth, iHeight);
          if (opts.parent && opts.parent.document && opts.parent.document.body) {
            var iMaxWidth = opts.parent.document.body.clientWidth;
            var iMaxHeight = opts.parent.document.body.clientHeight;
            iWidth = Math.min(iWidth, iMaxWidth);
            iHeight = Math.min(iHeight, iMaxHeight);
            var $title = opts.parent.jQuery("#ym-tl").find("div.ym-tc");
            if (!$title.hasClass("ym-ttc")) {
              hasTitle = true;
              if (!!opts.title) $title.addClass("ym-ttc");
            }
            opts.parent.ymPrompt.resizeWin(Math.max(iBodyOldWidth, iWidth), Math.max(iBodyOldHeight, iHeight));
            hasResize = true;
          }
          emisShowDialog({url: _selectURL, width: iWidth, height: iHeight, title: opts.title,
            showMask: true, handler: selectcallback, showHeadMask: opts.showHeadMask || false});
        }
        else {
          _oWin = window.open(_selectURL);
          _oWin.focus();
          return fncSelCallback(false);
        }
      } else if (rows.length == 0) {
        _aRetStr[0] = emptyFlag;
        selectcallback(_aRetStr);
      } else if (rows.length == 1) {
        var i;
        if (sURL.indexOf('FIELD') >= 0) {
          var paras = sURL.split("&");
          var oParam = {};
          for (i = 0; i < paras.length; i++) {
            if (paras[i].indexOf('FIELD') == -1) continue;
            var str = paras[i].split("=");
            oParam[str[0]] = str[1];
          }
          var oField = [];
          for (i = 1; i < 100; i++) {
            if (oParam["FIELD" + i]) {
              oField.push(oParam["FIELD" + i]);
            }
          }
          for (i = 0; i < oField.length; i++) {
            var node = rows[0].getElementsByTagName(oField[i])[0];
            _aRetStr.push(node ? cellvalue(node) : "");
          }
        } else {
          for (i = 0; i < rows[0].childNodes.length; i++) {
            _aRetStr.push(cellvalue(rows[0].childNodes[i]));
          }
        }

        selectcallback(_aRetStr);
      }
    }

  } else {
    _aRetStr[0] = emptyFlag;
    selectcallback(_aRetStr);
  }
  function selectcallback(_aRetStr) {
    if (hasResize) {
      if (hasTitle) {
        var $title = opts.parent.jQuery("#ym-tl");
        $title.find("div.ym-tc").removeClass("ym-ttc");
        iBodyOldHeight = (iBodyOldHeight || 0) - 0 + $title.height();
      }
      opts.parent.ymPrompt.resizeWin(iBodyOldWidth, iBodyOldHeight);
    }
    var customCallback = fncSelCallback;
    if (_aRetStr === 'close') _aRetStr = [''];

    if (_aRetStr[0] == emptyFlag) {
      //alert((emisEmpty(sMsg)?"":"「" + sMsg + "」欄位輸入錯誤，\n\n") + "無任何查詢資料，請重新輸入！")
      if( typeof(ymPromptAlert) == "function") {
        ymPromptAlert({msg:(emisEmpty(sMsg) ? "" : jQuery.getMessage("query_input_error", sMsg) + '<br>') + (emisEmpty(sNoDataMsg)?jQuery.getMessage("query_nothing_msg"):sNoDataMsg), callback:function(){
          oNextFocus.focus();
          if (!emisEmpty(oNoDataFocus)) oNoDataFocus.focus();
        }});
      } else {
        alert((emisEmpty(sMsg) ? "" : jQuery.getMessage("query_input_error", sMsg)) + jQuery.getMessage("query_nothing_msg"))
      }
      //return false;  // Joe 2010/08/26 Mark: 解決無資料時無清空原值Bug
    }
    // 非表格模式
    if (sURL.indexOf("SEL_TYPE=TABLE") == -1) {
      // 未選取或無任何查詢資料
      if (_aRetStr[0] == "" || _aRetStr[0] == emptyFlag) {
        // 將值清空
        for (var i = 0; i < setComponent.length; i++)
          eval(setComponent[i] + "=''");

        // onchange 時須先移至下一欄位輸入,再移回無資料之欄位
        if(_aRetStr[0] == "" || typeof(ymPromptAlert) != "function") {  // IE会在ymPromptAlert窗口显示光标
          oNextFocus.focus();
          if (!emisEmpty(oNoDataFocus)) oNoDataFocus.focus();
        }
        return customCallback(false);
      }
      // 有選取資料將值回傳
      var notFocusFlds = "";
      for (var i = 0; i < setComponent.length; i++) {
        // 處理單引號
        try {

          //alert(setComponent[i] + "='" + _aRetStr[i] +"'");
          _aRetStr[i] = _aRetStr[i].replace(new RegExp("'", "g"), "\\'");
          eval(setComponent[i] + "='" + _aRetStr[i] + "'");
        } catch (e) {
          //alert("無法設定欄位值 : " +  setComponent[i] )
          //alert(jQuery.getMessage("query_can_not_focus") + setComponent[i])
          notFocusFlds += "["+setComponent[i]+"]";
        }
      }
      if(notFocusFlds != ""){
        if( typeof(ymPromptAlert) == "function") {
          ymPromptAlert({msg:jQuery.getMessage("query_can_not_focus") + notFocusFlds});
        } else {
          alert(jQuery.getMessage("query_can_not_focus") + notFocusFlds);
        }
      }
    }
    // 表格模式
    else {
      // 未選取或無任何查詢資料
      if (_aRetStr[0] == "" || _aRetStr[0] == emptyFlag) {
        // onchange 時須先移至下一欄位輸入,再移回無資料之欄位
        if(_aRetStr[0] == "" || typeof(ymPromptAlert) != "function") {
          oNextFocus.focus();
          if (!emisEmpty(oNoDataFocus)) oNoDataFocus.focus();
        }
        return customCallback(false);
      }
      if (!targetTable) return customCallback(false);

      // 有選取資料將值回傳
      // setComponent  >> Fileds  >> Field Name Array
      // _aRetStr      >> Query Result  [[001,002,003],[商品1,商品2,商品3]]
      // targetTable   >> masterTbl
      // 1. 檢查可被回填的空行
      // 2. 依字段名字把數據回填
      (function () {
        var i, j, k = 0, len1, len2, isEmptyRow;
        // 拆分子元素
        for (i = 0, len1 = _aRetStr.length; i < len1; i++)
          _aRetStr[i] = _aRetStr[i].split(",");
        // 檢查可被回填的空行
        for (i = 0, len1 = targetTable.totalRowCount; i < len1; i++) {
          isEmptyRow = true;
          for (j = 0, len2 = setComponent.length; j < len2; j++) {
            if (targetTable.get(i, setComponent[j]) != "") {
              isEmptyRow = false;
              break;
            }
          }
          // 如果當前行不為空，則檢查下一行
          if (!isEmptyRow) continue;
          // 當記錄超出時停止迴圈
          if (k >= _aRetStr[0].length) break;
          //[
          // [001,002,003],
          // [商品1,商品2,商品3]
          //]
          for (j = 0, len2 = setComponent.length; j < len2; j++) {
            targetTable.set(i, setComponent[j], _aRetStr[j][k]);
          }
          k++;
        }
        targetTable.applyDomValue2Ui();
      })();
    }
    try {
      oNextFocus.focus();    // 移至下一欄位輸入
    } catch (e) {
      //alert("駐點無法移至下一欄位!")
    }
    customCallback(true);
  }
}
/**
 * 三點按鈕 标准通用的回调函数
 * @param oBtn
 * @returns {Function}
 * @constructor
 */
function AjaxSelectByYmPromptGenCallback(oBtn) {
  return function (bReturn) {
    if (selectAgain(bReturn)) {
      // 取回资料的最后一个编号 向下查询
      formobj('L_QRYNO_END').value = bReturn;
      alert(bReturn)
      jQuery(oBtn).click();
      return;
    }
    // 清掉原有值, 使由开头查起
    formobj('L_QRYNO_END').value = "";
  }
}
// 2013/01/26 Joe.Yao :新增树状选择资料公用逻辑   AjaxTreeSelectByYmPrompt start
var AjaxTreeSelectXML;
/**
 * 树状选择资料公用逻辑
 * @param opts
 * @returns {*}
 * @constructor
 */
function AjaxTreeSelectByYmPrompt(opts) {
  if (!opts) return;
  var sURL, setComponent, oNoDataFocus, oNextFocus, sMsg, iWidth, iHeight, fncSelCallback, targetTable;

  var iBodyOldWidth = document.body.getAttribute("cacheWidth") || document.body.clientWidth;
  var iBodyOldHeight = document.body.getAttribute("cacheHeight") || document.body.clientHeight;
  document.body.setAttribute("cacheWidth", iBodyOldWidth);
  document.body.setAttribute("cacheHeight", iBodyOldHeight);

  var hasResize = false, hasTitle = false;
  sURL = opts.url;
  setComponent = opts.setcomponent;
  oNoDataFocus = opts.nodatafocus;
  oNextFocus = opts.nextfocus;
  sMsg = opts.msg;
  iWidth = opts.width;
  iHeight = opts.height;
  fncSelCallback = opts.callback || function () {
  };
  targetTable = opts.targetTable;
  if (typeof targetTable == "undefined" && typeof currentTable != "undefined")
    targetTable = currentTable;
  // ? oNoDataFocus 即?入之欄位有被改為 readOnly 或 Disable, 則不?理
  /* if (oNoDataFocus.readOnly || oNoDataFocus.disabled)
   return true;*/

  if (opts.detlWindow)
    setDetlWindow(opts.detlWindow);

  iWidth = emisEmpty(iWidth) ? 300 : iWidth;
  iHeight = emisEmpty(iHeight) ? 520 : iHeight;

  var _aRetStr = new Array();
  var _oWin;
  // 若不傳入會取 select.xml
  var _selectURL = basePath + "/jsp/ajax_tree_ymprompt.jsp?" + encodeParam(sURL);


  if (!do_ajax_request(sURL)) {
    return fncSelCallback(false);
  }
  if (my_check_error(parseXmlDom(request))) {
    return fncSelCallback(false);
  }
  AjaxTreeSelectXML = parseXmlDom(request);
  var emptyFlag = "?????";
  var datas = AjaxTreeSelectXML.getElementsByTagName('data');
  if (datas.length > 0) {
    var empty = datas[0].getAttribute("empty");
    if (empty == "true") {
      _aRetStr[0] = emptyFlag;
      selectcallback(_aRetStr);
    } else {
      var rows = AjaxTreeSelectXML.getElementsByTagName("_r");
      if (rows.length > 1) {
        // 開窗
        if (emisEmpty(bDebug_)) {
          //_aRetStr = emisShowModal(_selectURL, iWidth, iHeight);
          if (opts.parent && opts.parent.document && opts.parent.document.body) {
            var iMaxWidth = opts.parent.document.body.clientWidth;
            var iMaxHeight = opts.parent.document.body.clientHeight;
            iWidth = Math.min(iWidth, iMaxWidth);
            iHeight = Math.min(iHeight, iMaxHeight);
            var $title = opts.parent.jQuery("#ym-tl").find("div.ym-tc");
            if (!$title.hasClass("ym-ttc")) {
              hasTitle = true;
              if (!!opts.title) $title.addClass("ym-ttc");
            }
            opts.parent.ymPrompt.resizeWin(Math.max(iBodyOldWidth, iWidth), Math.max(iBodyOldHeight, iHeight));
            hasResize = true;
          }
          emisShowDialog({url: _selectURL, width: iWidth, height: iHeight, title: opts.title,
            showMask: true, handler: selectcallback, showHeadMask: opts.showHeadMask || false});
        }
        else {
          _oWin = window.open(_selectURL);
          _oWin.focus();
          return fncSelCallback(false);
        }
      } else if (rows.length == 0) {
        _aRetStr[0] = emptyFlag;
        selectcallback(_aRetStr);
      } else if (rows.length == 1) {
        var i;
        if (sURL.indexOf('FIELD') >= 0) {
          var paras = sURL.split("&");
          var oParam = {};
          for (i = 0; i < paras.length; i++) {
            if (paras[i].indexOf('FIELD') == -1) continue;
            var str = paras[i].split("=");
            oParam[str[0]] = str[1];
          }
          var oField = [];
          for (i = 1; i < 100; i++) {
            if (oParam["FIELD" + i]) {
              oField.push(oParam["FIELD" + i]);
            }
          }
          for (i = 0; i < oField.length; i++) {
            var node = rows[0].getElementsByTagName(oField[i])[0];
            _aRetStr.push(node ? cellvalue(node) : "");
          }
        } else {
          for (i = 0; i < rows[0].childNodes.length; i++) {
            _aRetStr.push(cellvalue(rows[0].childNodes[i]));
          }
        }

        selectcallback(_aRetStr);
      }
    }

  } else {
    _aRetStr[0] = emptyFlag;
    selectcallback(_aRetStr);
  }

  function selectcallback(_aRetStr) {
    if (hasResize) {
      if (hasTitle) {
        var $title = opts.parent.jQuery("#ym-tl");
        $title.find("div.ym-tc").removeClass("ym-ttc");
        iBodyOldHeight = (iBodyOldHeight || 0) - 0 + $title.height();
      }
      opts.parent.ymPrompt.resizeWin(iBodyOldWidth, iBodyOldHeight);
    }
    var customCallback = fncSelCallback;
    //if (_aRetStr === 'close') _aRetStr = [''];
    if (_aRetStr === 'close') return;

    if (_aRetStr[0] == emptyFlag) {
      //alert((emisEmpty(sMsg)?"":"「" + sMsg + "」欄位輸入錯誤，\n\n") + "無任何查詢資料，請重新輸入！")
      alert((emisEmpty(sMsg) ? "" : jQuery.getMessage("query_input_error", sMsg)) + jQuery.getMessage("load_nothing_msg"));
      //return false;  // Joe 2010/08/26 Mark: 解決無資料時無清空原值Bug
    }
    // 非表格模式
    if (sURL.indexOf("SEL_TYPE=TABLE") == -1) {
      // 未選取或無任何查詢資料
      if (_aRetStr[0] == "" || _aRetStr[0] == emptyFlag) {
        // 將值清空
        /* for (var i = 0; i < setComponent.length; i++)
         eval(setComponent[i] + "=''");*/

        // onchange 時須先移至下一欄位輸入,再移回無資料之欄位
        oNextFocus.focus();
        //if (!emisEmpty(oNoDataFocus)) oNoDataFocus.focus();
        return customCallback(false);
      }

      // 有選取資料將值回傳
      for (var i = 0; i < setComponent.length; i++) {
        // 處理單引號
        try {

          //alert(setComponent[i] + "='" + _aRetStr[i] +"'");
          _aRetStr[i] = _aRetStr[i].replace(new RegExp("'", "g"), "\\'");
          eval(setComponent[i] + "='" + _aRetStr[i] + "'");
        } catch (e) {
          //alert("無法設定欄位值 : " +  setComponent[i] )
          alert(jQuery.getMessage("query_can_not_focus") + setComponent[i])
        }
      }
    }
    // 表格模式
    else {
      // 未選取或無任何查詢資料
      if (_aRetStr[0] == "" || _aRetStr[0] == emptyFlag) {
        // onchange 時須先移至下一欄位輸入,再移回無資料之欄位
        oNextFocus.focus();
        //if (!emisEmpty(oNoDataFocus)) oNoDataFocus.focus();
        return customCallback(false);
      }
      if (!targetTable) return customCallback(false);

      // 有選取資料將值回傳
      // setComponent  >> Fileds  >> Field Name Array
      // _aRetStr      >> Query Result  [[001,002,003],[商品1,商品2,商品3]]
      // targetTable  >> masterTbl
      // 1. 檢查可被回填的空行
      // 2. 依字段名字把數據回填
      (function () {
        var i, j, k = 0, len1, len2, isEmptyRow;

        // 拆分子元素
        for (i = 0, len1 = _aRetStr.length; i < len1; i++)
          _aRetStr[i] = _aRetStr[i].split(",");

        // 檢查可被回填的空行
        for (i = 0, len1 = targetTable.totalRowCount; i < len1; i++) {
          isEmptyRow = true;
          for (j = 0, len2 = setComponent.length; j < len2; j++) {
            if (targetTable.get(i, setComponent[j]) != "") {
              isEmptyRow = false;
              break;
            }
          }
          // 如果當前行不為空，則檢查下一行
          if (!isEmptyRow) continue;
          // 當記錄超出時停止迴圈
          if (k >= _aRetStr[0].length) break;

          //[
          // [001,002,003],
          // [商品1,商品2,商品3]
          //]
          for (j = 0, len2 = setComponent.length; j < len2; j++) {
            targetTable.set(i, setComponent[j], _aRetStr[j][k]);
          }
          k++;
        }
        targetTable.applyDomValue2Ui();
      })();
    }

    try {
      oNextFocus.focus();    // 移至下一欄位輸入
    } catch (e) {
      //alert("駐點無法移至下一欄位!")
    }
    customCallback(true);
  }
} // AjaxTreeSelectByYmPrompt end

/**
 * 单笔式（含表单式之表头）删除一笔数据
 * @param TableObject 要进行删除的MasterTable
 * @param sAct 默认为 del，可依实据需求传入代替
 */
function AjaxMtnDelByYmPrompt(options) {
  if (emisEmpty(options) || emisEmpty(options.table)) return;
  // 2013/02/04 增加判断表身是否加载完成
  if (!AjaxMtnCheckDetlIsReady()) return false;
  ymPromptAnswer({
    title: jQuery.getMessage("ymprompt_tip_title"),
    msg: options.message == null ?jQuery.getMessage('confirm_for_delete') : options.message,
    dft: 2,
    type: "?",
    callback: function (ret) {
      if (ret == "yes") {
        options.table.setMode("DEL");
        if (formobj('act')) {
          formobj('act').value = options.act || "del";
        }
        if (typeof(delInit) == "function") {
          delInit();
        }
        if (AjaxUpdate()) {
          AjaxMtnAfterSave(options.table, true, updaterows_);
        }
      }
    }
  });
} // AjaxMtnDelByYmPrompt end
/**
 * 多笔式(含表单式之表身)删除
 * @param opts
 */
function AjaxTabUpdByYmPrompt(options) {
  if (!options) return;
  var opts = jQuery.extend({}, options);
  var sAct = opts.act,
      checkfld = opts.check,
      fldArray = opts.fields,
      sMessage = opts.message,
      fldAlias = opts.alias,
      _sTitle = (opts.title || (!!opts.detlWindow ? opts.detlWindow.sTitle : (typeof sTitle == "undefined" ? "" : sTitle)) || ""),
      callback = opts.callback || function (bRet) {
      };
  if (opts.detlWindow) {
    window.__detl_window = opts.detlWindow;
  }
  var tokenizer = new Array(fldArray.length);
  var i;

  if (emisEmpty(fldAlias)) fldAlias = fldArray;
  // setting '&P_NO=' start prefix
  // 当别名为空时，取原栏位名
  for (i = 0; i < fldArray.length; i++) {
    tokenizer[i] = "&" + (fldAlias[i] || fldArray[i]) + "=";
  }

  var need_update_cnt = 0;
  var targetTable = !!opts.detlWindow
      ? opts.detlWindow.currentTable
      : (typeof currentTable == "undefined" ? null : currentTable);

  if (typeof targetTable != "undefined") {
    var _getTokenFlds = function (idx, tokenizer, fldArray) {
      for (var i = 0; i < fldArray.length; i++) {
        tokenizer[i] = tokenizer[i] + targetTable.get(idx, fldArray[i]) + ",";
      }
    };
    for (i = 0; i < targetTable.totalRowCount; i++) {
      var chk = targetTable.get(i, checkfld);
      if (chk == "1") {
        need_update_cnt++;
        _getTokenFlds(i, tokenizer, fldArray);
      }
    }
  }

  // 組成最後的 request
  var result = "act=" + sAct + "&TITLE=" + _sTitle;

  for (i = 0; i < fldArray.length; i++) {
    result += tokenizer[i].substring(0, tokenizer[i].length - 1);
  }
  var _AjaxTabUpdAdditional = !!opts.detlWindow
      ? opts.detlWindow.AjaxTabUpdAdditional
      : (typeof AjaxTabUpdAdditional == "undefined" ? null : AjaxTabUpdAdditional);
  // 刪除時增加附加條件，要求返回"&a=xx&b=yy&c=zz"
  if (typeof ( _AjaxTabUpdAdditional ) == "function") {
    result += _AjaxTabUpdAdditional();
  }

  //alert(result);
  if (need_update_cnt > 0) {
    // 2012/11/23 依Angel要求移到檢查勾選后
    sMessage = sMessage || jQuery.getMessage("confirm_for_delete");
    ymPromptAnswer({
      title: jQuery.getMessage("ymprompt_tip_title"),
      msg: sMessage,
      dft: 2,
      type: "?",
      //btn:[[jQuery.getMessage("ymprompt_btn_ok"),'yes'], [jQuery.getMessage("ymprompt_btn_cancel"),'no']],
      callback: function (ret) {
        if (ret == "yes") {
          callback(!!opts.detlWindow ? opts.detlWindow.AjaxUpdate(result) : AjaxUpdate(result));
        } else {
          callback(false);
        }
      }
    });
  } else {
    var msg = jQuery.getMessage("has_no_data_selected");
    //如果表身有NAME叫CHKDEL的物件,並且為隱藏,則提示整批修改的信息,否則提示刪除的信息
    if ((jQuery("[name=CHKDEL]") && jQuery("[name=CHKDEL]").is(":hidden"))
        || (opts.detlWindow && opts.detlWindow.jQuery("[name=CHKDEL]") && opts.detlWindow.jQuery("[name=CHKDEL]").is(":hidden") )) {
    //if (jQuery("[name=CHKDEL]") && jQuery("[name=CHKDEL]").is(":hidden")) {
      // 沒有選擇的資料
      msg = jQuery.getMessage("has_no_data_update");
    }
    ymPromptAlert({
      title: jQuery.getMessage("ymprompt_tip_title"),
      msg: msg,
      handler: function () {
        callback(false);
      }
    });
  }
} // AjaxTabUpdByYmPrompt end

/**
 * 寻找视窗
 * AjaxMtnQQryByYmPrompt({table: masterTbl, msg: msg, field: "C_NO", size: 10, picture: "U", padZero: "Y"});
 * @param options
 * @constructor
 */
function AjaxMtnQQryByYmPrompt(options) {
  if (emisEmpty(options) || emisEmpty(options.table) || emisEmpty(options.field)) return;
  var opts = {
    msg: jQuery.getMessage('qqry_msg', options.msg), // 请输入欲【寻找】之『{0}』：
    leftZero: (options.padZero === 'Y'),// 有傳此參數則自動補零
    val: options.table.getCurrent(options.field),// set first value;
    size: options.size || 10,
    maxLen: options.maxLen || options.size || 10,
    picture: options.picture,
    type: "I",
    callback: function (val) {
      if (!emisEmpty(val) && !/cancel/i.test(val)) {
        if (options.table.search(options.field, val)) {
          options.table.go(options.table.searchResult);
        } else {
          ymPromptShow({
            title: jQuery.getMessage("ymprompt_tip_title"),
            msg: jQuery.getMessage('qqry_notfound_msg', options.msg, val),
            icoCls: "ymPrompt_alert",
            callback: function () {
              var newOpts = jQuery.extend({}, opts);
              newOpts.val = val;
              ymPromptPrompt(newOpts);
            }
          });
        }
      }
    }
  };
  ymPromptPrompt(opts);
}// AjaxMtnQQryByYmPrompt end
/**
 * 顯示訊息
 * ymPromptAlert({msg:"訊息字串"});
 * ymPromptAlert({msg:"訊息字串",callback: fncCallback});
 * ymPromptAlert({msg:"訊息字串",detlWindow: window, callback: fncCallback});
 */
function ymPromptAlert(options) {
  if (emisEmpty(options) || emisEmpty(options.msg)) return;
  var opts = jQuery.extend({}, options);
  if (emisEmpty(opts.icoCls)) opts.icoCls = 'ymPrompt_alert';
  if (emisEmpty(opts.callback)) opts.callback = function () {
  };
  if (typeof opts.titleBar == "undefined" && emisEmpty(opts.titleBar)) opts.titleBar = true;
  if (opts.detlWindow) {
    window.__detl_window = opts.detlWindow;
    delete opts["detlWindow"];
  }
  delete opts["showHeadMask"];
  ymPromptShow(opts);
}
/**
 * 顯示詢問訊息
 * ymPromptAnswer({msg:"訊息字串", dft:預設按鍵, type:顯示類型});
 * ymPromptAnswer({msg:"是否確定欲刪除此資料？ <Y/N>", dft:2});
 * ymPromptAnswer({msg:"是否確定欲儲存此資料？ <Y/N/C>", dft:1, type:"T"});
 * @param opts
 */
function ymPromptAnswer(options) {
  var opts = jQuery.extend({}, options);
  if (emisEmpty(opts) || emisEmpty(opts.msg)) return;
  if (emisEmpty(opts.type)) opts.type = "?";
  if (emisEmpty(opts.icoCls)) opts.icoCls = 'ymPrompt_confirm';
  if (emisEmpty(opts.callback)) opts.callback = function () {
  };
  if (typeof opts.titleBar == "undefined" && emisEmpty(opts.titleBar)) opts.titleBar = true;

  if (opts.detlWindow) {
    window.__detl_window = opts.detlWindow;
    delete opts["detlWindow"];
  }
  ymPromptShow(opts);
}
/**
 * 提示输入
 * ymPromptPrompt({msg:"请输入要查找的商品编号：",calback: fncCallback});
 * ymPromptPrompt({msg:"请输入要查找的商品编号：", size:10, maxLen:10, calback: fncCallback});
 * ymPromptPrompt({msg:"请输入要查找的商品编号：",type: "I", calback: fncCallback});
 * ymPromptPrompt({msg:"请输入密码：",type: "P", calback: fncCallback});
 * @param opts
 */
function ymPromptPrompt(options) {
  var opts = jQuery.extend({}, options);
  if (emisEmpty(opts) || emisEmpty(opts.msg)) return;
  if (emisEmpty(opts.type)) opts.type = "I";
  if (emisEmpty(opts.icoCls)) opts.icoCls = 'ymPrompt_alert';
  if (typeof opts.titleBar == "undefined" && emisEmpty(opts.titleBar)) opts.titleBar = true;
  if (emisEmpty(opts.size)) opts.size = 10;
  if (emisEmpty(opts.maxLen)) opts.maxLen = opts.size;
  if (opts.type == "I") {
    opts.msg = opts.msg + " <input type='text' id='edtInput' " +
        "value='" + opts.val + "' size='" + opts.size + "' maxlength='" + opts.maxLen + "'" +
      //       (bMsgPadLeftZero==false?"":" onblur='this.value=emisPadl(this.value, "+iMsgSize_+",\"0\")'") +
        (emisEmpty(opts.picture) ? "" : " onkeypress=\"emisPicture('" + opts.picture + "')") + "\">";
  } else if (opts.type == "P") {
    opts.msg = opts.msg + " <input type='password' id='edtInput' " +
        " size='" + opts.size + "' maxlength='" + opts.maxLen + "'" +
        (emisEmpty(opts.picture) ? "" : " onkeypress=\"emisPicture('" + opts.picture + "')") + "\">";
  }

  var origCallback = opts.callback;
  var newCallback = function (ret) {
    if (!emisEmpty(origCallback)) {
      if (ret == "ok") {
        var sVal = document.getElementById("edtInput").value;
        if (opts.leftZero && opts.type == "I" && !/cancel/i.test(sVal))
          sVal = emisPadl(sVal, opts.size, "0");
        origCallback(sVal);
      } else {
        origCallback("");
      }
    }
  };
  opts.callback = newCallback;

  if (opts.detlWindow) {
    window.__detl_window = opts.detlWindow;
    delete opts["detlWindow"];
  }
  ymPromptShow(opts);
  //return emisMsgBox(sStr, "I", 1, sInput, iSize, iMaxLength, sPicture)
}

/**
 * 显示消息层
 * ymPromptAlert / ymPromptAnswer / ymPromptPrompt 实际处理逻辑
 * @param options
 */
function ymPromptShow(options) {
  var opts = jQuery.extend({}, options);
  if (opts.type == "T") {
    if (emisEmpty(opts.btn)) {
      opts.btn = [
        [jQuery.getMessage("ymprompt_btn_yes"), 'yes'],
        [jQuery.getMessage("ymprompt_btn_no"), 'no'],
        [jQuery.getMessage("ymprompt_btn_cancel"), 'cancel']
      ];
    }
  } else if (opts.type == "I" || opts.type == "P") {
    if (emisEmpty(opts.btn)) {
      opts.btn = [
        [jQuery.getMessage("ymprompt_btn_ok"), 'ok'],
        [jQuery.getMessage("ymprompt_btn_cancel"), 'cancel']
      ];
    }
  } else if (opts.type == "?") {
    //ymPrompt.confirmInfo({title:opts.title,message:opts.msg,handler:opts.callback});
    if (emisEmpty(opts.btn)) {
      opts.btn = [
        [jQuery.getMessage("ymprompt_btn_yes"), 'yes'],
        [jQuery.getMessage("ymprompt_btn_no"), 'no']
      ];
    }
  } else {
    //ymPrompt.confirmInfo({title:opts.title,message:opts.msg,handler:opts.callback});
    if (emisEmpty(opts.btn)) {
      opts.btn = [
        [jQuery.getMessage("ymprompt_btn_ok"), 'ok']
      ];
    }
  }
  // 暂时关闭所有标题，等有空做不同皮肤时再把此行注解，开放其下一行，依标题控制是否显示
  opts.titleBar = false;

  var _opts = {title: opts.title, titleBar: opts.titleBar, message: opts.msg, icoCls: opts.icoCls, btn: opts.btn, handler: opts.callback};
  // 优化高宽计算逻辑
  opts.msg = opts.msg.split("\n").join("<br/>");
  (function () {
    var box = document.getElementById("emis_message_box");
    if (!box) {
      var tmpBox = document.createElement("div");
      tmpBox.id = "emis_message_box";
      tmpBox.setAttribute("id", "emis_message_box");
      // 預設為 msgbox.htm 字體為 9pt
      tmpBox.style.fontSize = "9pt";
      tmpBox.style.visibility = "hidden";
      tmpBox.style.display = "inline";
      tmpBox.style.position = "absolute";
      // 2015/01/16 Joe fix 修正Chrome下計算實際高寬錯誤問題 start
      tmpBox.style.top="0px";
      tmpBox.style.left="0px";
      // 2015/01/16 Joe fix 修正Chrome下計算實際高寬錯誤問題 end
      document.body.appendChild(tmpBox);
      delete tmpBox;
      box = document.getElementById("emis_message_box");
    }
    box.innerHTML = "<div style='display:table;'>" + opts.msg + "</div>";
    var innerBox = box.getElementsByTagName("div")[0];
    _opts.width = innerBox.offsetWidth + 150;
    _opts.height = innerBox.offsetHeight + 150;
  })();
  ymPrompt.win(_opts);

  setTimeout(function () {
    if (opts.type == "I" || opts.type == "P") {
      setFocus("edtInput");
      $(formobj("edtInput")).keydown(function (e) {
        var keycode = e.keyCode;
        if (keycode == 13) {
          convertEnterToTab(e);
        }
      })
    }
    // 这里不知道为什么IE下会导致脚本错误，暂时注释掉
    else {
      try {
        var btns = ymPrompt.getButtons();
        var idx = emisEmpty(opts.dft) ? 0 : opts.dft > btns.length ? 0 : opts.dft - 1;
        btns[idx].focus();
      } catch (e) {
      }
    }
  }, 200);

}

// *************************************************************************
//YmPrompt方式报表
function AjaxRptByYmPrompt(sURL, sMode, sAlert, sFun, oForm) {
  if (window.prePrintSaveUrl && prePrintSaveUrl.isSave) return prePrintSaveUrl(sURL);
  // 显示列印讯息
  var _sMessage = "";
  var oSpaMessage = formobj("spaMessage");
  if (typeof(oSpaMessage) != "undefined") {
    emisMarquee(oSpaMessage);
    _sMessage = "Yes";
  }

  var _sRetStr = "";
  //var _oWin;
  var _sURL = basePath + "jsp/rpt_data.jsp?" + sURL +
      (sURL.indexOf("&TITLE=") > 0 ? "" : "&TITLE=" + emisGetTitle()) +
      (emisEmpty(bDebug_) ? "" : "&DEBUG=true") + "&MESSAGE=" + _sMessage +
      (emisEmpty(sFun) ? "" : "&FUN=" + sFun) + // 2005/06/02 andy :加入此行
      (emisEmpty(sAlert) ? "" : "&ALERT=" + sAlert);

  // 加入报表种类选择的画面 add by andy 2005/12/26 start
  var _sRptVer = sRptVersion_;
  var oRPT_VER = formobj("RPT_VER");
  if (_sURL.indexOf("&RPT_VER=1.0") >= 0
      || (typeof(oRPT_VER) == "object" && oRPT_VER.value == "1.0")) {
    _sRptVer = "1.0";
  }
  /////////////////////////
  var callRpt = function (_sURL) {
    // 加入报表种类选择的画面 add by andy 2005/12/26 end
    _sURL = encodeParam(_sURL);  //对URL进行统一编码,以防止URL中包含中文字符而导致后端接收不到 devin.xie 2007/08/24
    sRptURL_ = _sURL;
    if (emisEmpty(sMode) || sMode != "get") {
      // 2005/08/11 add by andy start:列印时显示一个跑马灯效果的信息视窗,因为资料多时,原列印视窗不能显示信息
      //_oWin=emisOpen(_sURL);       // 使用 Submit Open
      //_oWin.focus();
      if (bShowRptMsg_) {
        //oRptMsgWin_ = emisOpen("../emis_rpt.htm");
        if (_sRetStr == "3" || _sRetStr == "4") {
          //oRptMsgWin_ = emisRptOpen("../ajax_rpt.jsp", "", oForm);
          //直接调用emis_data_execel.jsp, 不是以ocx方式开档，不再调用进度条的显示了。
          emisRptOpen(_sURL, "", oForm);
        } else {
          oRptMsgWin_ = emisOpen("../ajax_rpt.jsp");
        }
      } else {
        if (_sRetStr == "3" || _sRetStr == "4") {
          oRptWin_ = emisRptOpen(_sURL, "", oForm);
        } else {
          oRptWin_ = emisOpen(_sURL);
        }
      }
      // 2005/08/11 add by andy end
    }
    else if (sMode == "get") {
      emisShowDialog({url: _sURL, width: 300, height: 100, handler: function (sRetStr) {
        if (typeof(oSpaMessage) != "undefined")
          emisMarquee(oSpaMessage, "end");
        //2004/11/19 [1494] Jacky 修正若以"get"方式处理的错误
        if (sRetStr == "" || (jQuery.isArray(sRetStr) && sRetStr[0] == "")) {
          return true;
        }
        if (sAlert.toUpperCase() != "NOMESSAGE") {
          alert(sRetStr);
          return false;
        }
      }
      });
    }
  }
  //当RPT_VER设为1.0时,不提供报表选择画面,按旧的方式列印.
  if (!emisEmpty(_sRptVer) && _sRptVer != "1.0") {
    sRptURL_ = _sURL;
    var iHeight = 280;
    if (sRptVersion_ == "1.1") {
      iHeight = 220;
    }
    emisShowDialog({url: basePath + "jsp/ajax_rptkind_sel.jsp", width: 400, height: iHeight, handler: function (sRetStr) {
      _sURL = _sURL.replace(/&RPT_OUTER_TYPE=TXT/g, "&RPT_TYPE_OLD=TXT");
      _sURL = _sURL.replace(/&RPT_OUTER_TYPE=EXCEL/g, "&RPT_TYPE_OLD=EXCEL");
      _sURL = _sURL.replace(/&WREP=false/g, "");
      _sURL = _sURL.replace(/&WREP=true/g, "");
      _sURL = _sURL.replace(/&EXCEL_SINGLE_PAGE=false/g, "");
      _sURL = _sURL.replace(/&EXCEL_SINGLE_PAGE=true/g, "");
      _sURL = _sURL.replace(/&RPT_OUTER_TYPE=/g, "");
      _sURL = _sURL.replace(/&WREP=false=/g, "");
      _sURL = _sURL.replace(/&EXCEL_SINGLE_PAGE=/g, "");

      var oRPT_OUTER_TYPE = formobj("RPT_OUTER_TYPE");
      if (typeof(oRPT_OUTER_TYPE) == "object" && _sURL.indexOf("&RPT_TYPE_OLD") < 0) {
        _sURL += "&RPT_TYPE_OLD=" + oRPT_OUTER_TYPE.value;
      }

      if (sRetStr == "1") {  //文字报表(WSHOW)
        _sURL += "&RPT_OUTER_TYPE=TXT&WREP=false&EXCEL_SINGLE_PAGE=false";
      } else if (sRetStr == "2") {   //图形报表(WREP)
        _sURL += "&RPT_OUTER_TYPE=TXT&WREP=true&EXCEL_SINGLE_PAGE=false";
      } else if (sRetStr == "3") {   //EXCEL报表
        var oRPT_OCX_SHOW = formobj("RPT_OCX_SHOW");
        //_sURL += "&RPT_OUTER_TYPE=EXCEL&WREP=false&EXCEL_SINGLE_PAGE=false&RPT_EXCEL_DOWNFILE=Y" ;
        // Excel报表的打开方式改为在IE中直接开启档案的方式.  update by andy
        _sURL += "&RPT_OUTER_TYPE=EXCEL&WREP=false&EXCEL_SINGLE_PAGE=false";
        if ((typeof(oRPT_OCX_SHOW) == "object" && oRPT_OCX_SHOW.value == "Y")
            || _sURL.indexOf("RPT_OCX_SHOW=Y") >= 0) {
          ;// 仍使用OCX下载EXCEL报到并打开。
        } else {
          _sURL = _sURL.replace(/rpt_data.jsp/g, "emis_data_execel.jsp");
        }
      } else if (sRetStr == "4") {   //EXCEL一页式报表
        var oRPT_OCX_SHOW = formobj("RPT_OCX_SHOW");
        //_sURL += "&RPT_OUTER_TYPE=EXCEL&WREP=false&EXCEL_SINGLE_PAGE=true&RPT_EXCEL_DOWNFILE=Y" ;
        // Excel报表的打开方式改为在IE中直接开启档案的方式.  update by andy
        _sURL += "&RPT_OUTER_TYPE=EXCEL&WREP=false&EXCEL_SINGLE_PAGE=true";
        if ((typeof(oRPT_OCX_SHOW) == "object" && oRPT_OCX_SHOW.value == "Y")
            || _sURL.indexOf("RPT_OCX_SHOW=Y") >= 0) {
          ;// 仍使用OCX下载EXCEL报到并打开。
        } else {
          _sURL = _sURL.replace(/rpt_data.jsp/g, "emis_data_execel.jsp");
        }
      } else {
        return false;
      }
      _sRetStr = sRetStr;
      callRpt(_sURL);
    }
    });
  } else {
    callRpt(_sURL);
  }
}

// 判斷 Form 之所有物件 是否有輸入值
// oFormObj : Form 物件
// oFocusObj: 無任何物件有輸入值之 Focus 物件
// sMsg     : 無任何物件有輸入值之訊息
function emisFormEmptyByYmPrompt(oFormObj, oFocusObj, sMsg, callback) {
  // 判斷是否有輸入值
  var bEmpty = true;
  var _oElement = new Object;
  var sType,sIgnore, reg = /button|hidden|reset|radio/i;
  for (var i = 0, len = oFormObj.elements.length; i < len; i++) {
    _oElement = oFormObj.elements[i];
    sType = _oElement.getAttribute("type");
    sIgnore = _oElement.getAttribute("ignoreFormEmpty");
    // Check 不是 Button, Hidden 物件
    if (!reg.test(sType) && !/true/i.test(sIgnore) && !/L_QRYNUM/i.test(_oElement.name) && !emisEmpty(_oElement.value) &&
        _oElement.value != ">" && _oElement.value != "=" && _oElement.value != "<") {
      bEmpty = false;
      if (/checkbox/i.test(sType)) {
        bEmpty = !_oElement.checked;
      }
      if (!bEmpty) break;
    }
  }
  if (bEmpty) {
    ymPromptAlert({
      msg: emisEmpty(sMsg) ? jQuery.getMessage("query_have_not_condition") : sMsg,
      callback: function () {
        if (typeof callback == "function") {
          callback(false);
        }
        oFocusObj.focus();
      }
    });
  } else {
    if (typeof callback == "function") {
      callback(true);
    }
  }
}

// 增加ymPrompt开启的信息提示窗口中快捷键的处理。by Joe
(function ($) {
  var reg = new RegExp("([\(a-zA-Z{1}\)])", "g");
  var isMatch = function (val, keycode) {
    if (!val)
      return false;
    var matchs1 = val.toLowerCase().match(reg);
    var matchs2 = val.toUpperCase().match(reg);
    if (matchs1.length == 3) {
      if (keycode == matchs1[1].charCodeAt(0) || keycode == matchs2[1].charCodeAt(0)) {
        return true;
      }
    }
    return false;
  };
//$(document).keydown(function (e) {  // 用jquery写法，在IE上有些小问题。
  document.onkeydown=function(evt){
    var e = evt || window.event;
    var keycode = e.keyCode;
    if ((keycode >= 65 && keycode <= 90)) { // 仅处理字母键
      try{
        var btns = ymPrompt.getButtons();
        for (var idx in btns) {
          if (isMatch(btns[idx].value, keycode)) {
            btns[idx].click();
            break;
          }
        }
      } catch(e) {

      }
    }
  };
  //);
})(jQuery);
