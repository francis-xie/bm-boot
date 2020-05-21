//$Id: ajax_util.js 10553 2017-12-28 03:53:46Z andy.he $
// 為了避免影響到之前的 project , 將 emis.js 和 epos.js 放在這個檔中

//=======================================================epos.js=========================================================
// 設定系統使用參數
var oQtyFmt=new Array(10,4);
var iQryNum_ = 100;       // 主作業查詢預設筆數
var iSelNum_ = 100;       // Select 作業查詢預設筆數
var bAltKeyDown_ = false; // 是否按下 ALT 鍵, 以作 ALT-F4 判斷
var sSaveUniqueMsg_;    // 自定儲存後重複資料之訊息
var sSaveSuccessMsg_;   // 自定储存后提示储存成功讯息
var sDisabledBGColor_ = "LightCyan";  // Disabled欄位的預設背景色

// 2005/08/11 add by andy start
var oRptWin_;     //指向原列印窗體的物件..
var oRptMsgWin_;  //指向列印訊息窗體(僅顯示一個跑馬燈效果)的物件
var bShowRptMsg_ = true; //是否顯示列印訊息視窗,開發階段建議不顯示.
// 2005/08/11 add by andy end
var sRptURL_ = ""; //保存emisRpt中傳入的URL,讓其在epos_rptkind_sel.htm能取得到. add by andy 2005/12/27
var sRptVersion_ = "1.3";   //報表版本控製

var isIE_ = false;  // 是否開啟IE原有的右鍵功能表
var isDevelop = false; //是否為開發階段,可開啟FullSource功能

//===========================================emis.js==================================================
var oQtyFmt=new Array(10,4);
var bDebug_ = false;     // 是否需於 emis_save.jsp , emis_data.jsp 作 Debug且不將視窗關閉
var bDateType_ = true;  // 日期格式是否為西元格式
var sDateSepa_ = "";     // 日期是否使用分隔符號
var sTimeSepa_ = "";     // 時間是否使用分隔符號

// *************************** Debug Start ********************************
//======================= useful debug ===============================
function dumpMasterTable(mstbl, separator) {
  if (!mstbl || !mstbl.rows || !mstbl.rows.length) return;
  var i, len = mstbl.rows.length, dumpxml = [], sep = separator || "";
  for (i = 0; i < len; i++)
    dumpxml[dumpxml.length] = __dumpXML(mstbl.rows[i]);
  return dumpxml.join(sep);
}

function dumpXML(xmlObj) {
  if (xmlObj)
    alert(__dumpXML(xmlObj));
}

function __dumpXML(xmlObj) {
  var i, xmldump = [];
  if (xmlObj == null) return;
  if (xmlObj.nodeType == 1) {
    xmldump[xmldump.length] = '<' + xmlObj.nodeName + '>';
    for (i = 0; i < xmlObj.childNodes.length; i++) {
      xmldump[xmldump.length] = __dumpXML(xmlObj.childNodes[i]);
    }
    xmldump[xmldump.length] = '</' + xmlObj.nodeName + '>';
  }
  if (xmlObj.nodeType == 3) {
    xmldump[xmldump.length] = xmlObj.nodeValue;
  }
  return xmldump.join("");
}
//===============================================================
//  easy function to see how many components in document.all
function dumpComponents() {
  var len = document.all.length; // we must keep it
  for (var i = 0; i < len; i++) {
    var obj = document.all[i];
    if (emisEmpty(obj.id)) {
      alert(obj.getAttribute("name"));
    } else {
      alert(obj.id);
    }
  }
}

//  把 < ! - - 和 最後的 --> 拿掉
function removeXMLComment(s) {
  var idx = s.indexOf("<!--");
  if (idx != -1) {
    s = s.substring(0, idx) + s.substring(idx + 4);
  }
  idx = s.lastIndexOf("-->");
  if (idx != -1) {
    s = s.substring(0, idx) + s.substring(idx + 3);
  }
  return s;
}

(function() {
  var myElenemt = function (elm) {
    this[0] = elm;
    this.show = function() {
      this[0].style.display = '';
    };
    this.hide = function() {
      this[0].style.display = 'none';
    };
    this.val = function(sVal) {
      if (typeof sVal != 'undefined') {
        try {
          this[0].value = sVal;
        } catch(e) {
          this[0].innerText = sVal;
        }
      } else return this[0].value || this[0].innerText;
    };
  };
  var $ = typeof jQuery != 'undefined' ? jQuery : function(sId) {
    return new myElenemt(document.getElementById(sId.substr(1)));
  };
  var padZero = function(str, len) {
    return (new Array(len + 1).join('0') + str).slice(-1 * len);
  };
  var Debug = {
    input: null,
    isShow: false,

    init: function() {
      var logdiv = document.createElement('div');
      logdiv.id = "debug";
      logdiv.style.cssText = "top: 20px; display:none;position: absolute; right:20px;background-color:#FFF;border:1px solid #666;padding:5px;filter:Alpha(Opacity=80);opacity:0.8;height:50%;width: 40%;";
      logdiv.innerHTML = '<div style="height:90%;"><textarea id="debug_txt" wrap="off" style="border:0px solid #FFF;font-size:12px;color:#666;height:100%;width:100%;"></textarea></div><div style="padding-top:5px;text-align:center"><button onclick="jsLogger.clear();">清除</button>  <button onclick="jsLogger.close();">关闭</button></div>';
      document.body.appendChild(logdiv);
      delete logdiv;
      this.input = $('#debug_txt');
    },

    write: function(p, type) {
      if (!this.input) this.init();
      if (!this.isShow) {
        $('#debug').show();
        this.isShow = true;
      }
      var t = new Date();
      var ts = padZero(t.getHours(), 2) + ':' + padZero(t.getMinutes(), 2) + ':' + padZero(t.getSeconds(), 2) + ' - ' + (type || 'I') + ' - ';
      this.input.val(this.input.val() + (this.input.val() == '' ? '' : '\r\n') + ts + p);
      this.input[0].scrollTop = this.input[0].scrollHeight;
    },

    clear: function() {
      this.input.val('');
    },

    close: function() {
      $('#debug').hide();
      this.isShow = false;
    }

    /*,scroll: function(){
     Como(window).on('scroll', function(){
     $('#debug').css('top', (Como(document.body).pos().top + 20) + 'px');
     });
     }*/
  };
  var log = function() {
    function parse(p) {
      if (typeof(p) == 'string') {
        return p;
      } else if (typeof(p) == "number") {
        return p.toString();
      } else if (typeof(p) == "boolean") {
        return p.toString();
      } else {
        return typeof(p);
      }
    }

    this.debug = function (msg) {
      Debug.write(parse(msg), 'D');
    };

    this.info = function (msg) {
      Debug.write(parse(msg), 'I');
    };

    this.warn = function (msg) {
      Debug.write(parse(msg), 'W');
    };

    this.error = function (msg) {
      Debug.write(parse(msg), 'E');
    };

    this.clear = function() {
      Debug.clear();
    };

    this.close = function() {
      Debug.close();
    };
  };
  window.jsLogger = new log();
})();
// *************************** Debug End ********************************

//===============================================================
//  讓 openDialog 和 window.open 兩者可以共用同一個
//
function getParent(owner) {
  /*if( typeof(dialogArguments) == "undefined" ) {
   return window.opener;
   }*/
  owner = owner || window;
  return (_emisTop && _emisTop.winCache && _emisTop.winCache[_sDwzWinid] && _emisTop.winCache[_sDwzWinid].parent)
      || owner.opener || (owner.dialogArguments && owner.dialogArguments.window) || owner.parent;
}
//===================================================================================================
// 项目的根路径
var basePath = (function () {
  var sFullPath = window.document.location.href;
  var sUriPath = window.document.location.pathname;
  var pos = sFullPath.indexOf(sUriPath);
  var prePath = sFullPath.substring(0, pos);
  var postPath = sUriPath.substring(0, sUriPath.substr(1).indexOf('/') + 1);
  return prePath + postPath + "/";
})();

// *************************** 表单式作用区域 Start ********************************
//========================================================================================
//   在表頭的 jsp 中使用  registerActive , showActve 就會生效
//========================================================================================
(function($) {
  // 修正 非IE下点击表身未正确显示作用区问题
  if (parent && $(parent.document).attr("EMIS_IS_BILL_MODE") === "Y") {
    var firstTime = true;
    $(document).click(function(e) {
      parent.showActive(2);
      parent.AjaxHideMenu(e);
      // 第一次时把作用区设定为表头
      if (firstTime) {
        firstTime = false;
        parent.showActive(1);
      }
    });
  }
})(jQuery);
function registerActiveArea(sFrameId) {
  sFrameId = '#' + (sFrameId || 'dataframe');
  jQuery(document).focus(function () {
    showActive(1);
  });
  jQuery(document).click(function (e) {
    showActive(1);
    for (var i = 0, len = window.frames.length; i < len; i++) {
      try {
        if (window.frames[i] && window.frames[i].AjaxHideMenu)
          window.frames[i].AjaxHideMenu(e);
      } catch(ex) {
        if( typeof(ymPromptAlert) == "function") {
          ymPromptAlert({msg:ex.message});
        } else {
          alert(ex.message);
        }
      }
    }
  }).attr("EMIS_IS_BILL_MODE", "Y");

  jQuery(sFrameId).focus(function () {
    showActive(2);
  });
  jQuery(sFrameId).click(function () {
    showActive(2);
  });
  // 默认显示为表头
  showActive(1);
}


function showActive(sType) {
  if (!emisEmpty(typeof(formobj('idActive')))) {
    if (sType == 1) {
      formobj('idActive').innerText = jQuery.getMessage("show_active_head");//"表頭";
      formobj('idActive').style.color = "blue";
    }
    else {
      formobj('idActive').innerText = jQuery.getMessage("show_active_body");//"表身";
      formobj('idActive').style.color = "green";
    }
  }
}
// *************************** 表单式作用区域 End ********************************


//===========================================emis.js==================================================
// ***************************** 字串函數 **********************************
// 重複產生字串
// sCh:重複字元
// iLen:重複個數
function emisReplicate(sCh, iLen) {
  var _sStr = "";
  for (var i = 0; i < iLen; i++)
    _sStr += sCh;
  return _sStr;
}

// 左邊去空白
// sValue:傳入之字串
function emisLtrim(sValue) {
  for (var i = 0; i <= sValue.length; i++) {
    if (sValue.substring(i, i + 1) != " ")
      break;
  }
  return sValue.substring(i, sValue.length);
}

// 右邊去空白
// sValue:傳入之字串
function emisRtrim(sValue) {
  for (var i = sValue.length; i >= 0; i--) {
    if (sValue.substring(i - 1, i) != " ")
      break;
  }
  return sValue.substring(0, i);
}

// 左右邊去空白
// sValue:傳入之字串
function emisTrim(sValue) {
  return emisRtrim(emisLtrim(sValue));
}

// 左邊補空白
// sValue:傳入之字串
// iLen:補入個數sPad 未傳入, 以 " " 字串
// sPad 未傳入, 以 " " 字串
//2004/06/15 Jacky 判斷是否空值若為空值責傳回空字串
function emisPadl(sStr, iLen, sPad, isEmpty) {
  if (arguments.length == 2) sPad = " ";
  if (! emisEmpty(isEmpty)) {
    if (emisEmpty(sStr))
      return sStr;
  }
  sStr = "" + sStr;
  //2004/10/20 [1176] Jacky 判斷若內容若沒有%則補零
  if (sStr.indexOf("%") < 0)
    sStr = emisReplicate(sPad, iLen - emisLength(sStr)) + sStr;
  return sStr;
}

// 右邊補空白
// sValue:傳入之字串
// iLen:補入個數sPad 未傳入, 以 " " 字串
// sPad 未傳入, 以 " " 字串
function emisPadr(sStr, iLen, sPad) {
  if (arguments.length == 2) sPad = " ";
  sStr = "" + sStr;
  return sStr + emisReplicate(sPad, iLen - emisLength(sStr));
}

// 左右邊補空白
// sValue:傳入之字串
// iLen:補入個數sPad 未傳入, 以 " " 字串
// sPad 未傳入, 以 " " 字串
function emisPadc(sStr, iLen, sPad) {
  if (arguments.length == 2) sPad = " ";
  sStr = "" + sStr;

  var _iLeft = 0, _iRight = 0;
  var _iLength = iLen - emisLength(sStr);
  if (_iLength > 0) {
    _iLeft = parseInt(_iLength / 2);
    _iRight = _iLength - _iLeft;
  }
  return emisReplicate(sPad, _iLeft) + sStr + emisReplicate(sPad, _iRight);
}

// 取出中文字以兩個Byte長度計
// 與 Java substr 寫法一樣
// 最後一個 Byte 為中文字第一 Bytes,則捨去此中文字
// sStr:傳入之字串
// iStart:開始取出之第?個Byte
// iEnd:結束之第?個Byte
function emisSubstr(sStr, iStart, iLength) {
  var _iCount = 0;
  var _iStart = -1, _iEnd = -1;
  for (var i = 0; i < sStr.length; i++) {
    if (sStr.charCodeAt(i) >= 0x2E80)
      _iCount += 2;
    else
      _iCount += 1;

    // 取得開始值
    if (_iStart == -1 && _iCount > iStart) {
      _iStart = i;
    }

    // 取得結束值
    if (_iEnd == -1 && _iCount > iStart - 0 + iLength - 1) {
      // 最後一個 Byte 為中文字第一 Bytes,則捨去此中文字
      if (_iCount > iStart - 0 + iLength)
        _iEnd = i - 1;
      else
        _iEnd = i;
      break;
    }
  }

  // 若無結束值則以字串長度
  if (_iEnd == -1) _iEnd = sStr.length;

  return sStr.substr(_iStart, _iEnd - _iStart - 0 + 1);
}

// 取得中文字串長度
// sValue:傳入之字串
function emisLength(sValue) {
  var _iLength = 0;
  /* 目前数据库存中文的字段类型均为nvarchar, 如nvarchar(10),实际是可以存10个中文字的。
  for (var i = 0; i < sValue.length; i++) {
    if (sValue.charCodeAt(i) >= 0x2E80)
      _iLength += 2;
    else
      _iLength += 1;
  }
  */
  _iLength = sValue.length;
  return _iLength;
}
// 用于获取兼容浏览器的事件源对象
function emisGetEvent() {
  if (document.all) {
    return window.event;//如果是ie
  }
  var func = arguments.callee.caller;
  while (func != null) {
    var arg0 = func.arguments[0];
    if (arg0) {
      if ((arg0.constructor == Event || arg0.constructor == MouseEvent)
          || (typeof(arg0) == "object" && arg0.preventDefault && arg0.stopPropagation)) {
        return arg0;
      }
    }
    func = func.caller;
  }
  return null;
}
// 非IE下檢查是否為功能鍵
function emisCheckFuncKey4NonIE(ev) {
  var keyCode = ev.keyCode || ev.which,
      charCode = ev.charCode;
  //  console.log("charCode>>" + charCode);
  if (charCode > 0) {
    return false;
  } else if (keyCode < 48) {
    return true;
  } else if (keyCode >= 112 && keyCode <= 123) {
    var str1 = String.fromCharCode(keyCode).toUpperCase(),
        str2 = String.fromCharCode(keyCode - 32);
    return !(str1 === str2);
  }
  return false;
}
// 輸入欄位 Picture 處理
// 必須在 onkeypress event 才會有作用, onkeydown 分不出字母大小寫
// sType="U" 轉換英文字母為大寫
//       "A" 僅能輸入英文字母
//       "B" 僅能輸入英文字母及數字
//       "C" 僅能輸入英文字母外加 空白跟#
//       "9" 僅能輸入數字
//       "N" 僅能輸入數字, "."
//       "$" 僅能輸入數字, ".", "+", "-"
//       "D" 僅能輸入數字, ".", "/"
//       "S" 僅能輸入數字, "+", "-"
//       "T" 僅供電話號碼專用 , 允許 數字,"+","-","(",")" ,"#"
// oField: 欄位物件
// iLength: 欄位長度控制
function emisPicture(sType, oField, iLength) {
  var ev = emisGetEvent();
  var isIE = !!document.all;
  var _iKeyCode = isIE ? ev.keyCode : ev.which;
  var _oSrcElement = isIE ? ev.srcElement : ev.target;
  //alert(_iKeyCode);
  if (!emisEmpty(oField)) {
    oField.setAttribute("PICURE", sType);
  }
  // 判斷中文字串長度
  if (!emisEmpty(oField)) {
    if (!emisChkLength(oField.value, iLength - 1)) {
      //window.event.returnValue = false
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
      return false;
    }
  }

  // ENTER, TAB 至下一欄位
  // 不是 Button 按 ENTER, TAB 至下一欄位
  if ((_iKeyCode == 13 || _iKeyCode == 9) && !/button/gi.test(_oSrcElement.getAttribute("type"))) {
    //window.event.keyCode = 9;
    if (isIE) ev.keyCode = 9;
    return true;
  }

  sType = sType.toUpperCase();

  // "U" 轉換英文字母為大寫
  if (sType == "U") {
    if (_iKeyCode > 96 && _iKeyCode < 123) {
      //window.event.keyCode = _iKeyCode - 32;
      if (isIE) {
        ev.keyCode = _iKeyCode - 32
      } else if (_oSrcElement && /text/gi.test(_oSrcElement.getAttribute("type"))) {
        // 非IE下，本应该使用KeyUp事件，但公用逻辑为KeyPress，所以用延迟来处理
        setTimeout(function () {
          var keychar = String.fromCharCode(_iKeyCode);
          var numcheck = /[a-z]/;
          if (numcheck.test(_oSrcElement.value)) {
            var start = Math.abs(_oSrcElement.value.indexOf(keychar)) + 1; //算出字串位置
            _oSrcElement.value = _oSrcElement.value.toUpperCase();
            _oSrcElement.selectionStart = start; //FF要游標出現的字串位置
            _oSrcElement.selectionEnd = start;
            // 2013/01/10 修正Chrome下输入小写字母自动转大写后不会自动易发change问题，因此增加一个trigger动作
            try {
              if (typeof jQuery != 'undefined') jQuery(_oSrcElement).trigger("change");
            } catch(e) {
              if (typeof console != "undefined" && console.log)
                console.log(e.message);
            }
          }
        }, 1);
      }
    }
    return true;
  }

  // "A" 僅能輸入英文字母
  if (sType == "A") {
    if (_iKeyCode < 65 || _iKeyCode > 123) {
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }

  // "B" 僅能輸入英文字母及數字
  if (sType == "B") {
    if (!((_iKeyCode >= 65 && _iKeyCode <= 123) || (_iKeyCode >= 48 && _iKeyCode <= 57))) {
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }
  // "C" 僅能輸入英文字母及數字 add by Jacky
  if (sType == "C") {
    if (!((_iKeyCode >= 65 && _iKeyCode <= 123) || (_iKeyCode >= 48 && _iKeyCode <= 57) || (_iKeyCode = 32) )) {
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }

  // "9" 僅能輸入數字
  if (sType == "9") {
    if (_iKeyCode < 48 || _iKeyCode > 57) {
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }

  // "N" 僅能輸入數字, "."
  if (sType == "N") {
    if ((_iKeyCode < 48 || _iKeyCode > 57) && _iKeyCode != 46) {
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }

  // "$" 僅能輸入數字及".", "+", "-"
  if (sType == "$") {
    if ((_iKeyCode < 48 || _iKeyCode > 57) &&
        _iKeyCode != 46 && _iKeyCode != 43 && _iKeyCode != 61 && _iKeyCode != 45 && _iKeyCode != 95) {
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }

  // "D" 僅能輸入數字及".", "/"
  if (sType == "D") {
    if ((_iKeyCode < 48 || _iKeyCode > 57) &&
        _iKeyCode != 46 && _iKeyCode != 47) {
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }

  // "S" 僅能輸入數字及"+", "-"
  if (sType == "S") {
    if ((_iKeyCode < 48 || _iKeyCode > 57) &&
        _iKeyCode != 45 && _iKeyCode != 43) {  //2004/09/13 Frankie 原_iKeyCode!=61 &&_iKeyCode!=45 && _iKeyCode!=95,移除,並加入_iKeyCode!=45
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }
  //2004/10/22 [1139] Jacky 增加電話號碼的picture "T"
  // "T"  僅供電話號碼專用 , 允許 數字,"+","-","(",")" ,"#"
  if (sType == "T") {
    if ((_iKeyCode < 48 || _iKeyCode > 57) &&
        _iKeyCode != 45 && _iKeyCode != 43 &&
        _iKeyCode != 40 && _iKeyCode != 41 &&
        _iKeyCode != 35 && _iKeyCode != 186) {
      //window.event.returnValue = false;
      if (isIE) {
        window.event.returnValue = false;
      } else if (!emisCheckFuncKey4NonIE(ev)) {
        ev.preventDefault();
      }
    }
    return true;
  }
} // emisPicture()

// 轉成貨幣數字表示
// iValue : 傳入的數值
// sSign  : 前置符號, 如 "$", 預設 ""
function emisMoney(iValue, sSign) {
  if (isNaN(iValue - 0))
    return iValue;

  var _sValue = iValue + "";
  var _iPoint = _sValue.indexOf(".");
  var _iStart = _iPoint < 0 ? _sValue.length : _iPoint;

  var _sRetValue = "";
  var _sChar = "";
  var _iCount = 0;
  for (var i = _iStart - 1; i >= 0; i--) {
    _sChar = _sValue.charAt(i);
    _iCount++;
    if (_iCount == 3 && i > 0) {
      _sChar = "," + _sChar;
      _iCount = 0;
    }
    _sRetValue = _sChar + _sRetValue;
  }

  _sRetValue = _iPoint < 0 ? _sRetValue : _sRetValue + _sValue.substr(_iPoint);

  return (emisEmpty(sSign) ? "" : sSign) + _sRetValue;
}

// 四捨五入函數
// iValue : 傳入的數值
// iDecimal : 所取的小數位數
function emisRound(iValue, iDecimal) {
  var _iReturn = iValue * Math.pow(10, iDecimal);

  _iReturn = Math.round(_iReturn) / Math.pow(10, iDecimal);
  return _iReturn;
}

// 補入 %, 以供 SQL Like 使用
// sSql: 傳入之 SQL 字串
// sKeyL:左邊補入, 不傳則預設為 "%"
// sKeyR:右邊補入, 不傳則預設為 "%"
// iLen:左邊補入個數sPad 未傳入, 以 " " 字串
// sPad 未傳入, 以 " " 字串
function emisSqlLike(sSql, sKeyL, sKeyR, iLen, sPad) {
  if (emisEmpty(sSql)) return "";

  //$ 91.4.19 sSql 有 %, 則不處理
  if (sSql.indexOf("%") >= 0) return sSql;

  //$ 91.4.19 支援補零處理
  if (!emisEmpty(iLen)) return emisPadl(sSql, iLen, sPad);

  var _iArgLength = arguments.length;
  sKeyL = _iArgLength < 2 ? "%" : sKeyL;
  sKeyR = _iArgLength < 3 ? "%" : sKeyR;
  return sKeyL + sSql + sKeyR;
}

// 去除 SQL Like 符號
// sSql: 傳入之 SQL 字串
// sKey: 去除之 Key, 不傳則預設為 "%"
function emisSqlLikeRepl(sSql, sKey) {
  if (emisEmpty(sSql)) return "";

  //$ 91.4.19 sSql 有 %, 則不處理
  //if (sSql.indexOf("%")>=0) return sSql; // 2003.10.11 刪除 KC

  var _sKey = arguments.length == 1 ? "%" : sKey;
  return sSql.replace(new RegExp(_sKey, "g"), "");
}

// Empty 之字串傳回空白字串
// sEmptyValue: 若傳入 sStr 為 Empty時, 傳回之值, 不傳預設為 ""
function emisSetEmpty(sStr, sEmptyValue) {
  sEmptyValue = arguments.length == 1 ? "" : sEmptyValue;
  return emisEmpty(sStr) ? sEmptyValue : sStr;
}

// *************************** 日期時間函數 ********************************
(function () {
  var padZero = function (s, t) {
    t = t || 2;
    var str = new Array(t + 1).join("0");
    try {
      var v = str + parseInt(s, 10);
      return v.substring(v.length - t);
    } catch (e) {
      return '&nbsp;';
    }
  };
  if (typeof Date.prototype.format != "function") {
    Date.prototype.format = function (pattern) {
      if (!this.valueOf())
        return '&nbsp;';
      var d = this;
      pattern = pattern || "yyyy-MM-dd HH:mm:ss SSS";
      return pattern.replace(/(yyyy|MM|dd|HH|mm|ss|SSS)/g,
          function ($1) {
            switch ($1) {
              case 'yyyy':
                return d.getFullYear();
              case 'MM':
                return padZero(d.getMonth() + 1);
              case 'dd':
                return padZero(d.getDate());
              case 'HH':
                return padZero(d.getHours());
              case 'mm':
                return padZero(d.getMinutes());
              case 'ss':
                return padZero(d.getSeconds());
              case 'SSS':
                return padZero(d.getMilliseconds(), 3);
            }
          }
      );
    };
  }
  var oServerDate = null;
  function startTimer() {
    // 定时器计算时间
    var oServerDateTimer;
    oServerDateTimer = setInterval(function () {
      if (!oServerDate) clearInterval(oServerDateTimer);
      //if (console && console.log) console.log(oServerDate.getTime());
      oServerDate = new Date(oServerDate.getTime() + 1000);
      //if (console && console.log) console.log(oServerDate.format("yyyy/MM/dd HH:mm:ss"));
    }, 1000);
  }
  /*  jQuery.ajax({
   type: "HEAD",
   url: location.href,
   //async: false,
   complete: function (xhr, textStatus) {
   if (xhr.readyState == 4 && xhr.status == 200) {
   var sDate = xhr.getResponseHeader("Date");
   oServerDate = new Date(sDate);
   startTimer();
   }
   }
   });*/

  /**
   * 获取服务器时间（异步方式取HEAD信息）
   * @return {Date}
   */
  window.emisServerDate = function() {
    if (oServerDate) return oServerDate;
    var sDate = "";
    jQuery.ajax({
      type: "HEAD",
      url: location.href,
      async: false,
      complete: function (xhr, textStatus) {
        if (xhr.readyState == 4 && xhr.status == 200) {
          sDate = xhr.getResponseHeader("Date");
        }
      }
    });
    oServerDate = new Date(sDate);
    startTimer();
    return  oServerDate;
  };
})();
// 取得今天或傳入日期的字串
// sDate: 傳入之日期的字串, 不傳預設為今天
// sSep: 日期格式分隔字, 不傳預設為 ""
function emisDate(sDate, sSep) {
  var _iDateYear = bDateType_ ? 0 : 1911;    // 是否為西元年格式
  //var _iDateLength=emisTrim(sDateSepa_).length;    // 日期是否多加一位數
  var _iDateLength = bDateType_ ? 1 : 0;       // 日期是否多加一位數

  // robert, 有傳值應該用傳的,emisEmpty 多判斷了空字串
  //var _sSep = emisEmpty(sSep) ? sDateSepa_ : sSep;
  var _sSep = (sSep == undefined) ? sDateSepa_ : sSep;

  if (emisEmpty(sDate)) {
    var _oDate = new Date();
    return emisPadl(_oDate.getFullYear() - _iDateYear, 3 + _iDateLength, "0") + _sSep +
        emisPadl(_oDate.getMonth() + 1, 2, "0") + _sSep +
        emisPadl(_oDate.getDate(), 2, "0");
  }
  else {
    var _sSep = emisEmpty(sSep) ? sDateSepa_ : sSep;
    sDate = sDate.replace(new RegExp(_sSep, "g"), "");

    if (sDate.length < 7 + _iDateLength)
      sDate = emisPadl(sDate, 7 + _iDateLength, "0");
    else if (sDate.length > 7 + _iDateLength)
      sDate = emisPadl(sDate.substr(0, 4) - _iDateYear, 3 + _iDateLength, "0") +
          sDate.substr(4, 2) + sDate.substr(6, 2);

    if (_sSep != "")
      sDate = sDate.substr(0, 3 + _iDateLength) + _sSep + sDate.substr(3 + _iDateLength, 2) + _sSep + sDate.substr(5 + _iDateLength, 2);
    return sDate;
  }
} // emisDate()

// 取得今天或傳入日期之"年"的字串
// sDate: 傳入之日期的字串, 不傳預設為今天
function emisYear(sDate) {
  var _iDateYear = bDateType_ ? 0 : 1911;    // 是否為西元年格式
  var _iDateLength = bDateType_ ? 1 : 0;       // 日期是否多加一位數
  if (emisEmpty(sDate))
    return emisPadl(new Date().getFullYear() - _iDateYear, 3 + _iDateLength, "0");
  else {
    sDate = emisDate(sDate);
    return sDate.substr(0, 3 + _iDateLength);
  }
}

// 取得今天或傳入日期之"月"的字串
// sDate: 傳入之日期的字串, 不傳預設為今天
function emisMonth(sDate) {
  var _iDateLength = bDateType_ ? 1 : 0;       // 日期是否多加一位數
  if (bDateType_)
    sDate = emisDate(sDate, "/");
  else
    sDate = emisCtoa(emisDate(sDate), "/");
  return emisPadl(new Date(sDate).getMonth() + 1, 2, "0");
}

// 取得今天或傳入日期之"日"的字串
// sDate: 傳入之日期的字串, 不傳預設為今天
function emisDay(sDate) {
  var _iDateLength = bDateType_ ? 1 : 0;       // 日期是否多加一位數
  if (bDateType_)
    sDate = emisDate(sDate, "/");
  else
    sDate = emisCtoa(emisDate(sDate), "/");
  return emisPadl(new Date(sDate).getDate(), 2, "0");
}

// 取得今天或傳入日期之"星期"的字串
// sDate: 傳入之日期的字串, 不傳預設為今天
// lNum: 星期之格式, 不傳預設為星期日、一..., 傳值則為數值 0,1...
function emisWeek(sDate, lNum) {
  if (bDateType_)
    sDate = emisDate(sDate, "/");
  else
    sDate = emisCtoa(emisDate(sDate), "/");

  var _nDay = new Date(sDate).getDay();
  if (emisEmpty(lNum))
    return eval(jQuery.getMessage("week_day"))[_nDay]; //"星期" + "日一二三四五六".substr(_nDay, 1);
  else
    return _nDay;  // 星期日傳回 "0"
}

// 將民國年字串轉換西元年字串
// sDate: 傳入之民國年日期的字串, 不傳預設為今天
// sSep: 日期格式分隔字, 不傳預設為 ""
function emisCtoa(sDate, sSep) {
  sDate = emisDate(sDate);
  var _sSep = emisEmpty(sSep) ? sDateSepa_ : sSep;
  sDate = sDate.replace(new RegExp(_sSep, "g"), "");
  return sDate.substr(0, 3) - 0 + 1911 + _sSep + sDate.substr(3, 2) + _sSep + sDate.substr(5, 2);
}

// 將西元年字串轉換民國年字串
// sDate: 傳入之西元年日期的字串
// sSep: 日期格式分隔字, 不傳預設為 ""
function emisAtoc(sDate, sSep) {
  var _sSep = emisEmpty(sSep) ? sDateSepa_ : sSep;
  sDate = sDate.replace(new RegExp(_sSep, "g"), "");
  return emisDate(sDate.substr(0, 4) - 1911 + sDate.substr(4, 2) + sDate.substr(6, 2), _sSep);
}

// 加減 iMonth 月數後的年月
// sMonth: 傳入之年月的字串, 不傳預設為今天
// iDiff: 相加月數
// sSep: 日期格式分隔字, 不傳預設為 ""
function emisMonthCal(sMonth, iMonth, sSep) {
  var _sSep = emisEmpty(sSep) ? sDateSepa_ : sSep;
  var _sDate = emisDate(emisEmpty(sMonth) ? "" : sMonth + _sSep + "01");
  var _iYear = emisYear(_sDate) - 0 + parseInt(iMonth / 12);
  var _iMonth = emisMonth(_sDate) - 0 + iMonth % 12;

  if (_iMonth == 0) {
    _iYear = _iYear - 1;
    _iMonth = "12";
  }
  else if (_iMonth > 12) {
    _iYear = _iYear + 1;
    _iMonth = _iMonth % 12;
  }
  else if (_iMonth < 0) {
    _iYear = _iYear - 1;
    _iMonth = 12 + _iMonth % 12;
  }

  var _iDateLength = bDateType_ ? 1 : 0;       // 日期是否多加一位數
  return emisPadl(_iYear, 3 + _iDateLength, "0") + _sSep + emisPadl(_iMonth, 2, "0");
}

// 加減 iMonth 月數後的日期
// sDate: 傳入之日期的字串, 不傳預設為今天
// iDiff: 相加月數
function emisMonthDiff(sDate, iMonth) {
  var _iYear = emisYear(sDate) - 0 + parseInt(iMonth / 12);
  var _iMonth = emisMonth(sDate) - 0 + iMonth % 12;
  var _iDay = emisDay(sDate);

  if (_iMonth == 0) {
    _iYear = _iYear - 1;
    _iMonth = "12";
  }
  else if (_iMonth > 12) {
    _iYear = _iYear + 1;
    _iMonth = _iMonth % 12;
  }
  else if (_iMonth < 0) {
    _iYear = _iYear - 1;
    _iMonth = 12 + _iMonth % 12;
  }

  var _iDateLength = bDateType_ ? 1 : 0;       // 日期是否多加一位數
  return emisDate(emisPadl(_iYear, 3 + _iDateLength, "0") + emisPadl(_iMonth, 2, "0") + emisPadl(_iDay, 2, "0"));
}

// 加減 iDiff 天數後的日期
// sDate: 傳入之日期的字串, 不傳預設為今天
// iDiff: 相加減天數
function emisDateDiff(sDate, iDiff) {
  var _sDate = sDate;
  if (bDateType_)
    _sDate = emisDate(sDate, "/");
  else
    _sDate = emisCtoa(sDate, "/");

  var _oDate = new Date(_sDate);
  var _iDiffSec = _oDate.getTime() + (24 * 60 * 60 * 1000 * iDiff);
  var _oDiffDate = new Date();
  _oDiffDate.setTime(_iDiffSec);
  if (bDateType_)
    return emisDate(_oDiffDate.getFullYear() + emisPadl(_oDiffDate.getMonth() + 1, 2, "0") +
        emisPadl(_oDiffDate.getDate(), 2, "0"));
  else
    return emisAtoc(_oDiffDate.getFullYear() + emisPadl(_oDiffDate.getMonth() + 1, 2, "0") +
        emisPadl(_oDiffDate.getDate(), 2, "0"));
}

// 日期相減
// sDate1, sDate2: 傳入之日期的字串
function emisDateSub(sDate1, sDate2) {
  var _sDate1 = sDate1;
  var _sDate2 = sDate2;
  if (bDateType_) {
    _sDate1 = emisDate(sDate1, "/");
    _sDate2 = emisDate(sDate2, "/");
  } else {
    _sDate1 = emisCtoa(sDate1, "/");
    _sDate2 = emisCtoa(sDate2, "/");
  }
  var _oDate1 = new Date(_sDate1);
  var _oDate2 = new Date(_sDate2);
  return parseInt((_oDate2 - _oDate1) / 24 / 60 / 60 / 1000);
}

// 將分隔符號去除
// sDate: 傳入之日期的字串
// sSep: 日期格式分隔字, 不傳預設為 ""
function emisDateSep(sDate, sSep) {
  var _sSep = emisEmpty(sSep) ? sDateSepa_ : sSep;
  sDate = sDate.replace(new RegExp(_sSep, "g"), "");
  return sDate;
}

// 將分隔符號加入
// sDate: 傳入之日期的字串
// sSep: 日期格式分隔字, 不傳預設為 ""
function emisDateSepConv(sDate, sSep) {
  if (!emisEmpty(sDate, sSep)) sDate = emisDate(sDate);
  return sDate;
}

// 取得系統時間 HH:MM:SS
// sSep:分隔符號, 如":", 不傳則預設無
// sSet:是否顯示 AM/PM, 預設不顯示
// lSet:是否顯示秒, 預設不顯示
function emisTime(sSep, sSet, lSet) {
  var _sSep = emisEmpty(sSep) ? sTimeSepa_ : sSep;
  var _sSet = "";
  var _oDate = new Date();
  var _sHr = emisPadl(_oDate.getHours(), 2, "0");
  var _sMin = emisPadl(_oDate.getMinutes(), 2, "0");
  var _sSec = emisPadl(_oDate.getSeconds(), 2, "0");

  if (!emisEmpty(sSet)) {
    if (_sHr > 12) {
      _sSet = "PM ";
      _sHr = emisPadl(_sHr - 12, 2, "0");
    }
    else if (_sHr == 12)
      _sSet = "PM ";
    else
      _sSet = "AM ";
  }
  return _sSet + _sHr + _sSep + _sMin + (emisEmpty(lSet) ? "" : _sSep + _sSec);
}

// 時間相減
// sTime1, sTime2: 傳入之時間:HHMM(時分)
// 傳回值為 4 位數 HHMM, 且自動補 0, 若 sTime1 > sTime2 則補負號
function emisTimeSub(sTime1, sTime2, sSep) {
  if (emisEmpty(sTime1) || emisEmpty(sTime2)) return;

  var _sSep = emisEmpty(sSep) ? sTimeSepa_ : sSep;
  sTime1 = emisPadl(sTime1.replace(":", ""), 4, "0");
  sTime2 = emisPadl(sTime2.replace(":", ""), 4, "0");
  var _iHour1 = sTime1.substr(0, 2) - 0;
  var sTime = "";
  if (sTime1 > sTime2) {
    // 將 sTime1 與 sTime2 互調
    sTime = sTime1;
    sTime1 = sTime2;
    sTime2 = sTime;
  }

  var _iHour1 = sTime1.substr(0, 2) - 0;
  var _iMin1 = sTime1.substr(2, 2) - 0;
  var _iHour2 = sTime2.substr(0, 2) - 0;
  var _iMin2 = sTime2.substr(2, 2) - 0;

  var _iHour = _iHour2 - _iHour1;
  var _iMin = _iMin2 - _iMin1;
  if (_iMin < 0) {
    _iHour--;
    _iMin = _iMin + 60;
  }

  return (sTime == "" ? "" : "-") + emisPadl(_iHour, 2, "0") + _sSep + emisPadl(_iMin, 2, "0");
}

// 將時間轉成小時
// sTime 傳入之時間:HHMM(時分)
// 傳回值為小時，取小一數1位
function emisTimeConv(sTime) {
  sTime = sTime.replace(":", "");

  var _sSign = "";
  if (sTime.substr(0, 1) == "-") {
    _sSign = "-";
    sTime = sTime.substr(1, sTime.length);
  }

  var _iHour = sTime.substr(0, 2) - 0;
  var _iMin = sTime.substr(2, 2) - 0;

  // 傳回小時單位，取小數一位, 並作四捨五入
  return _sSign + emisRound((_iHour * 60 + _iMin) / 60, 1) - 0;
}

// 日期時間相減
// sDate1, sTime1, sDate2, sTime2: 傳入之時間:HHMM(時分)
// 傳回值為小時，取小一數1位
function emisCalTime(sDate1, sTime1, sDate2, sTime2) {
  // 若有 Empty() 傳入值則傳回 -1
  if (emisEmpty(sDate1) | emisEmpty(sTime1) | emisEmpty(sDate2) | emisEmpty(sTime2))
    return -1;

  // 一天之工作時間起迄
  var _iTotalTime = 24;
  var _sStTime = "0000";
  var _sEndTime = "2400";

  var _iHours = (emisDateSub(sDate1, sDate2) - 0) * _iTotalTime; // 一天以24小時計

  if (_iHours == 0) {
    _iHours = emisTimeConv(emisTimeSub(sTime1, sTime2));
  } else {
    _iHours = _iHours + emisTimeConv(emisTimeSub(sTime1, _sEndTime))
        + emisTimeConv(emisTimeSub(_sStTime, sTime2))
        - _iTotalTime;
  }

  // 取小數一位, 並作四捨五入
  return emisRound(_iHours, 1);
}
// add by age 檢察時間輸入是否正確.
//  sStr 輸入的時候字符串
//  sSep 時間的分隔符號
//根據時間分隔符號截取不同的時位的值
function mySubstr(sStr, sSep) {
  if (sStr == "" || sSep == "") return sStr;
  if (sStr.indexOf(sSep) == -1) return sStr;
  else {
    return sStr.substr(0, sStr.indexOf(sSep));
  }
}

function eposChkTime(obj, sSep) {
  sSep = emisEmpty(sSep) ? ":" : sSep;
  var sTime;
  var _sHH, _sMM,_sSS;
  if ((obj.value).indexOf(sSep) == -1) {    //沒有輸入分隔符號
    sTime = obj.value;
    _sHH = emisPadl(sTime.substr(0, 2), 2, "0");
    _sMM = emisPadl(sTime.substr(2, 2), 2, "0");
    _sSS = emisPadl(sTime.substr(4, sTime.length), 2, "0");
  } else {                                //有輸入分隔符號
    _sHH = mySubstr(obj.value, sSep);
    _sMM = mySubstr(obj.value.substr(_sHH.length + 1, obj.length), sSep);
    _sSS = mySubstr(obj.value.substr((_sHH + _sMM).length + 2, obj.length), sSep);
    if (_sSS == "" && _sMM.length > 2) {
      _sSS = _sMM.substr(2, _sMM.length);
      _sMM = _sMM.substr(0, 2);
    }
    //小時,分鐘,秒數前補零
    _sHH = emisPadl(_sHH, 2, "0");
    _sMM = emisPadl(_sMM, 2, "0");
    _sSS = emisPadl(_sSS, 2, "0");
  }
  sTime = emisPadl(_sHH, 2, "0") + emisPadl(_sMM, 2, "0") + emisPadl(_sSS, 2, "0");

  if (sTime.length > 6)  return false;
  if (_sHH < "00" || _sHH >= "24" || _sMM < "00" || _sMM >= "60"
      || _sSS < "00" || _sSS >= "60") {
    return false;
  } else {
    obj.value = _sHH + sSep + _sMM + sSep + _sSS;
    return true;
  }
}

// end by age add

// *************************************************************************


// ***************************** 判斷處理函數 ******************************
// 判斷是否為空白
// xVaule:傳入之任何形態值
function emisEmpty(xValue) {
  if (xValue == null || xValue == "null" || xValue == "undefined" || xValue == "NaN" || xValue == "")
    return true;
  return false;
}

// 判斷是否為數值
// xVaule:傳入之任何形態值
// iFigure: 允許之整數位, 不傳則只判斷是否為數值
// iScale : 允許之小數位
// 傳回 ="": 是數值型態, !="": 不符合型態, 且傳回訊息字串
function emisChkNum(xValue, iFigure, iScale) {
  var _sType = typeof(xValue)
  var _sValue = xValue;

  // 轉換型態
  if (_sType == "number")
    _sValue = xValue + "";       // 轉型為字串
  else if (_sType == "object") {
    if (typeof(xValue.value) != "undefined")
      _sValue = xValue.value;  // 傳入為物件取其 value
  }
  else {
    _sValue = xValue + "";
  }

  // 空白則 return true
  if (_sValue == "") return "";

  var _iValue = _sValue - 0;
  // 判斷是否為數值
  if (isNaN(_iValue))
    return jQuery.getMessage("number_is_not_a_number"); //"不是數值型態！";

  _sValue=_iValue+"";   // 轉型為字串
  /*
   * 此處轉型有點問題,故將其註釋
   * fix by yan
   */
  var _iPoint = _sValue.indexOf(".");
  var _iLimit = emisReplicate("9", iFigure - 0) - 0;

  // 判斷整數
  if (!emisEmpty(iFigure)) {
    if (_iPoint > 0) _iValue = _sValue.substring(0, _iPoint);
    if (_iValue > _iLimit || _iValue < -_iLimit)
      return jQuery.getMessage("number_out_of_integer_digits", iFigure); //"整數位超出 " + iFigure + "位數！";
  }

  // 判斷小數
  /*if (_iPoint>0) {
   if (emisEmpty(iScale) || iScale==0) {
   return "不可有小數位！";
   } else {
   if (_sValue.substring(_iPoint+1,_sValue.length).length>iScale)
   return "小數位超出 " + iScale + "位數！";
   }
   }
   * 修改如下,因轉型問題而做如下修改
   * fix by yan
   */
  if (_iPoint > 0) {
    if (emisEmpty(iScale) || iScale == 0) {
      return jQuery.getMessage("number_can_not_have_decimal"); //"不可有小數位！";
    }
  }
  if (_iPoint >= 0) {
    if (_sValue.substring(_iPoint + 1, _sValue.length).length > iScale)
      return jQuery.getMessage("number_out_of_decimal_digits", iScale); //"小數位超出 " + iScale + "位數！";
  }

  return "";
} // emisChkNum()

// 判斷日期是否為正確
// sDate: 傳入之日期的字串
// sSep: 日期格式分隔字, 不傳預設為 ""
function emisChkDate(sDate, sSep) {
  // 處理補 "0", 及 "/"
  //add by Ben 2005/03/30 防止輸入『2000103』可以通過。
  var tempDate_ = sDate.replace(/\D/g, "");
  if (bDateType_ && tempDate_.length != 8
      && tempDate_.length != 4 && tempDate_.length != 2) {
    return false;
  }
  sDate = emisDate(sDate, sSep);

  // 強迫轉型為西元格式, 且加 "/"
  var _sDate;
  if (bDateType_) {
    _sDate = emisDate(sDate, "/");
  } else {
    _sDate = emisCtoa(sDate, "/");
  }

  var _oDate = new Date(_sDate);
  _sDate = emisDate(_oDate.getFullYear() + emisPadl(_oDate.getMonth() + 1, 2, "0") +
      emisPadl(_oDate.getDate(), 2, "0"), sSep);

  // 判斷日期與實際 Date() 傳回是否相同, 相同則正確
  if (_sDate == sDate)
    return true;
  return false;
}

// 判斷年月是否為正確
// sDate: 傳入之日期的字串
// sSep: 日期格式分隔字, 不傳預設為 ""
function emisChkMonth(sMonth, sSep) {
  var _sSep = emisEmpty(sSep) ? sDateSepa_ : sSep;
  return emisChkDate(sMonth + _sSep + "01", sSep)
}

// 判斷時間是否為正確
// sTime: 傳入之時間的字串
function emisChkTime(sTime, sSep) {
  var _sSep = emisEmpty(sSep) ? sTimeSepa_ : sSep;
  sTime = sTime.replace(_sSep, "");
  if (sTime.length < 4)
    return false;

  var _sHH, _sMM;
  _sHH = sTime.substr(0, 2);
  _sMM = sTime.substr(2, 2);
  if (_sHH < "00" || _sHH >= "24" ||
      _sMM < "00" || _sMM >= "60")
    return false;

  if (sTime.length > 4) {
    var _sSS = sTime.substr(4, 2);
    if (_sSS < "00" || _sSS >= "60")
      return false;
  }

  return true;
}


// 2004/08/18 Frankie 修正編碼規則如下所示
//編碼規則
//  項 目 計 算 方 法 說 明
//    統一編號 0 4 5 9 5 2 5 7 　
//    邏輯乘數 1 2 1 2 1 2 4 1       兩數上下對應相乘
//    乘    積 0 8 5 1 5 4 2 7
//                   8     0   乘積直寫並上下相加--------------------------------------------
//    乘積之和 0 8 5 9 5 4 2 7 將相加之和再相加 　
//    0+8+5+9+5+4+2+7=40 　 最後結果, 40 能被 10 整除, 故 04595257 符合邏輯。


// *若第七位數字為 7 時
// 統一編號 1 0 4 5 8 5 7 5 倒數號二位為 7
// 邏輯乘數 1 2 1 2 1 2 4 1 兩數上下對應相乘
// 乘    積 1 0 4 1 8 1 2 5
//                0   0 8   乘積直寫並上下相加---------------------------------------------
// 乘積之和 1 0 4 1 8 1 1 5
//                      0   再相加時最後第二位數取 0 或 1 均可。 　
// 1+0+4+1+8+1+1+5=21 　 　
// 1+0+4+1+8+1+0+5=20 　 最後結果中,
// 20 能被 10 整除, 故 10458575 符合邏輯。

// 判斷統一編號是否為正確
// sValue:傳入之字串

function emisChkP_ID(sValue) {
  if (sValue == "") return "";

  if (sValue.length != 8)
    return jQuery.getMessage("pid_length_error"); //"輸入長度錯誤！";

  // 統一編號
  for (var i = 0; i < 8; i++) {
    if (sValue.charAt(i) >= "A")
      return jQuery.getMessage("pid_is_not_number_error"); //"編號只能為數字！";
  }

  var _iValue = 0;
  var _iValue1 = 0;
  var _sChar;
  var _iChar;
  var _iValue2 = 0;
  for (var i = 0; i < 8; i++) {
    _sChar = sValue.charAt(i);
    if (i == 0 || i == 2 || i == 4 || i == 7) {                                 // 因為編碼在0,2,4,7位置的乘積為1
      // 第 0,2,4,7 直接將值加入
      _iChar = _sChar - 0;
      _iValue1 = _iValue1 + _iChar;
      _iValue = _iValue + _iChar;
      //      _iValue1= _iValue + _iChar;       //  2004/08/18  marked by Frankie
    }
    else if (i == 1 || i == 3 || i == 5) {                                     // 因為編碼在1,3,5位置的乘積為2
      // 第 1,3,5 將值*2 加入, 分十位及個位相加
      _sChar = emisPadl((_sChar - 0) * 2, 2, "0");
      _iChar = (_sChar.substr(0, 1) - 0) + (_sChar.substr(1, 1) - 0);
      _iValue1 = _iValue1 + _iChar;
      _iValue = _iValue + _iChar;
      //      _iValue1= _iValue1 + _iChar;       //  2004/08/18  marked by Frankie
    } else {
      // 第 6 將值*4 加入, 分十位及個位相加
      _sChar = emisPadl((_sChar - 0) * 4, 2, "0");                             // 因為第六碼編碼乘積為4
      if (sValue.charAt(6) - 0 != 7) {                                            // 如果第七碼編碼不是7則.....
        if (_sChar >= 10) {                                                     // 如果相乘後大於或等於10,
          _iChar = (_sChar.substr(0, 1) - 0) + (_sChar.substr(1, 1) - 0);     // 則再將十位數字和各位數字分割後相加
          if (_iChar >= 10) {                                                   // 如果相加後大於或等於10,
            _iChar = String(_iChar);
            _iChar = (_iChar.substr(0, 1) - 0) + (_iChar.substr(1, 1) - 0);   // 則再將十位數字和各位數字分割後相加
          }
          _iValue1 = _iValue1 + _iChar;
          _iValue = _iValue + _iChar;
        } else {                                                              // 如果相乘後小於10,
          _iChar = (_sChar - 0);
          _iValue1 = _iValue1 + _iChar;                                      // 直接把乘過的值加入
          _iValue = _iValue + _iChar;
        }


        // _iValue1 取十位及個位相加後之十位數
        //        _sChar = emisPadl(_iChar - 0,2,"0");                 //  2004/08/18  marked by Frankie
        //        _iValue1= _iValue1 + (_sChar.substr(0,1) - 0);       //  2004/08/18  marked by Frankie
      } else {                                                              // 如果第七碼編碼是7則.....
        if (_sChar >= 10) {                                                   // 如果相乘後大於或等於10,
          _iChar = (_sChar.substr(0, 1) - 0) + (_sChar.substr(1, 1) - 0);   // 則再將十位數字和各位數字分割後相加
          if (_iChar >= 10) {                                                 // 如果相加後大於或等於10,
            _iChar = String(_iChar);
            _iValue1 = _iValue1 + (_iChar.substr(1, 1) - 0);                // 則再將十位數字單獨加入_iValue1
            _iValue = _iValue + (_iChar.substr(0, 1) - 0);                // 則再將十位數字單獨加入_iValue

          } else {                                                          // 如果相加後小於10,
            _iValue1 = _iValue1 + _iChar;                                  // 直接把加過的值加入
            _iValue = _iValue + _iChar;
          }
        } else {                                                             // 如果相乘後小於10,
          _iChar = (_sChar - 0);
          _iValue1 = _iValue1 + _iChar;                                     // 直接把乘過的值加入
          _iValue = _iValue + _iChar;
        }

      }
    }
  }

  if (_iValue % 10 != 0) {
    if (( _iValue1 % 10 != 0)) {
      return jQuery.getMessage("pid_error"); //"編碼錯誤!!"
    } else {
      return ""
    }
  } else {
    return ""
  }
  //  return _iValue%10!=0 && (sValue.charAt(6)-0!=7 || _iValue1%10!=0)? "編碼11錯誤！" : "";       //  2004/08/18  marked by Frankie
}

// 判斷身份證號是否為正確
// sValue:傳入之字串
function emisChkID_NO(sValue) {
  if (sValue == "") return "";

  if (sValue.length != 10)
    return jQuery.getMessage("id_no_length_error"); //"輸入長度錯誤！";

  sValue = sValue.toUpperCase();
  if (sValue.length == 10) {
    // 身份證號
    var _sFValue = sValue.charAt(0);
    if (_sFValue < "A" || _sFValue > "Z")
      return jQuery.getMessage("id_no_first_must_be_letter"); //"證號第一碼只能為英文字母！";

    for (var i = 1; i < 10; i++) {
      if (sValue.charAt(i) < "0" || sValue.charAt(i) > "9")
        return jQuery.getMessage("id_no_must_end_with_number"); //"證號後九碼只能為數字！";
    }

    // 判斷身份證規則
    _sFValue = sValue.charAt(0);
    var _iFValue = sValue.charCodeAt(0);
    if (_sFValue >= "A" && _sFValue <= "H")
      _iFValue = _iFValue - 55;           // A=10,B=11......
    else if (_sFValue >= "J" && _sFValue <= "N")
      _iFValue = _iFValue - 56;           // J=18,K=19......
    else if (_sFValue >= "P" && _sFValue <= "V")
      _iFValue = _iFValue - 57;           // P=23,Q=24......
    else if (_sFValue == "X")
      _iFValue = 30;
    else if (_sFValue == "Y")
      _iFValue = 31;
    else if (_sFValue == "W")
      _iFValue = 32;
    else if (_sFValue == "Z")
      _iFValue = 33;
    else if (_sFValue == "I")
      _iFValue = 34;
    else if (_sFValue == "O")
      _iFValue = 35;

    var _sFNum = _iFValue + "";
    var _iFNum1 = _sFNum.charAt(0) - 0;
    var _iFNum2 = _sFNum.charAt(1) - 0;
    var _iFNum = _iFNum1 + 9 * _iFNum2

    for (var i = 1; i < 9; i++)
      _iFNum = parseInt(_iFNum) + (9 - i) * parseInt(sValue.charAt(i));

    if ((parseInt(_iFNum) + parseInt(sValue.charAt(9))) % 10 != 0)
      return jQuery.getMessage("id_no_error"); //"證號編碼錯誤！";
  }

  return "";
} // emisChkID_NO

// 判斷統一發票是否為正確
// sValue:傳入之字串
function emisChkInvoice(sValue) {
  if (sValue == "") return "";

  if (sValue.length != 10)
    return jQuery.getMessage("invoice_length_error"); //"輸入長度錯誤！";

  var _sFValue = sValue.charAt(0);
  if (_sFValue < "A" || _sFValue > "Z")
    return jQuery.getMessage("invoice_must_start_with_letter"); //"編號第一碼只能為英文字母！";

  var _sSValue = sValue.charAt(1);
  if (_sSValue < "A" || _sSValue > "Z")
    return jQuery.getMessage("invoice_second_must_be_letter"); //"編號第二碼只能為英文字母！";

  for (var i = 2; i < 10; i++) {
    if (sValue.charAt(i) < "0" || sValue.charAt(i) > "9")
      return jQuery.getMessage("invoice_must_end_with_letter"); //"統一發票後八碼只能為數字！";
  }

  return "";
} // emisChkInooice

// 判斷中文字串長度
// sValue:傳入之字串
// iLength:傳入之長度
function emisChkLength(sValue, iLength) {
  if (emisLength(sValue) > iLength)
    return false;
  return true;
}

// 判斷E-Mail是否合法
// sValue:傳入之字串
function emisChkEMail(sValue) {
  if (!emisEmpty(sValue) && sValue.indexOf("@") < 0)
    return false;
  if (!emisEmpty(sValue) && sValue.indexOf(".") < 0)
    return false;
  return true;
}

// 判斷WWW是否合法
// sValue:傳入之字串
function emisChkWWW(sValue) {
  if (!emisEmpty(sValue) && sValue.indexOf(".") < 0)
    return false;
  return true;
}
// *************************************************************************


// ************************** 欄位檢核處理函數 *****************************
// 判斷欄位是否為空白
// oField : 欄位物件
// sMsg   : 欄位名稱訊息
// iPage  : 此欄位分頁位置
function emisEmptyValid(oField, sMsg, iPage) {
  if (oField.value.replace(/ /g, "") == "") {
    // 舊寫法
    //alert("「" + sMsg + "」欄位不可為空白！");
    // 新寫法
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage('string_is_required', sMsg), callback:function(){
        setFocus(oField);
      }});
    } else {
      alert(jQuery.getMessage('string_is_required', sMsg));
      /*emisPageChg(iPage);
       oField.focus();*/
      setFocus(oField);
    }
    return false;
  }
  return true;
}

// 判斷欄位是否為數字
// oField : 欄位物件
// iFigure: 允許之整數位, 不傳則只判斷是否為數值
// iScale : 允許之小數位
// sMsg   : 欄位名稱訊息
// sUMsg  : 自訂訊息
// iPage  : 此欄位分頁位置
function emisNumValid(oField, iFigure, iScale, sMsg, sUMsg, iPage) {
  var _sMsg = emisChkNum(oField, iFigure, iScale);
  if (_sMsg != "") {
    if (emisEmpty(sUMsg)) {
      //alert((emisEmpty(sMsg)? "": "「" + sMsg + "」欄位") + _sMsg);
      sUMsg = (emisEmpty(sMsg) ? "" : jQuery.getMessage('field_valid_result', sMsg)) + _sMsg;
    }
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:sUMsg, callback:function(){
        oField.value = "";
        setFocus(oField);
      }});
    } else {
      alert(sUMsg);
      //		emisPageChg(iPage);
      oField.value = "";   //2004/02/19 Jacky 清空原值
      //		oField.focus();
      setFocus(oField);
    }

    return false;
  }
  if (oField.value != "")
    oField.value = (oField.value - 0).toFixed(iScale);
  /*
   * 自動給該小數補上小數部分的零
   * fix by yan
   */
  return true;
}

// 判斷欄位是否為日期
// oField : 欄位物件
// sMsg   : 欄位名稱訊息
// iPage  : 此欄位分頁位置
// bDefault: 是否允許空白, 不傳則預設允許
//2004/10/18 [1157] Jacky 增加下述需求
//若日期欄位只輸入2碼, 則系統自動幫他帶系統年月
//例如輸入20, 則focus離開, 會變成 2004/10/20 (或093/10/20)
//若日期欄位只輸入4碼, 則系統自動幫他帶系統年
//例如輸入1020, 則 focus離開, 會變成 2004/10/20 (或093/10/20)

function emisDateValid(oField, sMsg, iPage, bDefault) {
  var _sDate = oField.value.replace(/ /g, "");
  if (_sDate == "" && emisEmpty(bDefault)) return true;
  var _sTempYearMonth = emisDate();

  //2004/10/18 [1157]  Jacky 修改自動帶出系統年月的功能--Start
  //若為二位數則取得系統日期的年月
  if (_sDate.length == 2) {
    if (bDateType_) {
      _sDate = _sTempYearMonth.substr(0, 4) + _sTempYearMonth.substr(5, 2) + _sDate
    } else {
      _sDate = _sTempYearMonth.substr(0, 3) + _sTempYearMonth.substr(4, 2) + _sDate
    }
  } else {
    //若為四位數則取得系統日期的年
    if (_sDate.length == 4) {
      if (bDateType_) {
        _sDate = _sTempYearMonth.substr(0, 4) + _sDate
      } else {
        _sDate = _sTempYearMonth.substr(0, 3) + _sDate
      }
    }
  }
  //oField.value=emisDate(_sDate); mark by Ben 2005/03/31
  if (!emisChkDate(_sDate)) {
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg: jQuery.getMessage('date_is_invalidation', sMsg), callback: function () {
        setFocus(oField);
      }});
    } else {
      //alert("「" + sMsg + "」欄位日期格式錯誤！");
      alert(jQuery.getMessage('date_is_invalidation', sMsg));
      //		emisPageChg(iPage);
      //		oField.focus();
      setFocus(oField);
    }
    return false;
  }
  //2004/10/18 [1157]  Jacky 修改自動帶出系統年月的功能--end
  oField.value = emisDate(_sDate);  //add by Ben 2005/03/31
  return true;
}

// 判斷欄位是否為年月
// oField : 欄位物件
// sMsg   : 欄位名稱訊息
// iPage  : 此欄位分頁位置
function emisMonthValid(oField, sMsg, iPage) {
  var _sMonth = oField.value.replace(/ /g, "");
  if (_sMonth == "") return true;
  var _sTempYearMonth = emisDate();

  if(_sMonth.length == 1 ){
    if(_sMonth > "0") _sMonth = _sTempYearMonth.substr(0, bDateType_?4:3) + "0" + _sMonth
  } else if(_sMonth.length == 2 ){
    if(_sMonth > "00" && _sMonth <= "12") _sMonth = _sTempYearMonth.substr(0, bDateType_?4:3) + _sMonth
  }
  if (_sMonth.length < 4) {
    //alert("「" + sMsg + "」欄位格式錯誤(年,月)！");
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage('date_YM_is_invalidation', sMsg), callback: function () {
        oField.value = "";
        setFocus(oField);
      }});
    } else {
      alert(jQuery.getMessage('date_YM_is_invalidation', sMsg));
      //		emisPageChg(iPage);
      oField.value = "";
      //		oField.focus();
      setFocus(oField);
    }
    return false;
  }
  if (!emisChkMonth(_sMonth)) {
    //alert("「" + sMsg + "」欄位年月格式錯誤！");
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage('date_YM_is_invalidation2', sMsg), callback: function () {
        setFocus(oField);
      }});
    } else {
      alert(jQuery.getMessage('date_YM_is_invalidation2', sMsg));
      //		emisPageChg(iPage);
      //		oField.focus();
      setFocus(oField);
    }
    return false;
  }
  var _iDateLength = emisTrim(sDateSepa_).length;    // 日期是否多加一位數
  oField.value = emisDate(_sMonth + "01");
  oField.value = oField.value.substring(0, oField.value.length - 2 - _iDateLength);
  return true;
}

// 判斷欄位是否為有效年份
// oField : 欄位物件
// sMsg   : 欄位名稱訊息
// iPage  : 此欄位分頁位置
function emisYearValid(oField, sMsg, iPage) {
  var _sYear = oField.value.replace(/ /g, "");
  if (_sYear == "") return true;
  if(bDateType_) {
    var _sTempYearMonth = emisDate();

    if (_sYear.length == 2) {
      _sYear = _sTempYearMonth.substr(0, 2) + _sYear
    }
    if (_sYear.length < 4 || _sYear < "1911") {
      if (typeof(ymPromptAlert) == "function") {
        ymPromptAlert({msg: jQuery.getMessage('year_is_invalidation', sMsg), callback: function () {
          oField.value = "";
          setFocus(oField);
        }});
      } else {
        alert(jQuery.getMessage('year_is_invalidation', sMsg));
        //		emisPageChg(iPage);
        oField.value = "";
        //		oField.focus();
        setFocus(oField);
      }
      return false;
    }

    oField.value = _sYear;
  }
  return true;
}

// 判斷欄位是否為時間
// oField : 欄位物件
// sMsg   : 欄位名稱訊息
// iPage  : 此欄位分頁位置
function emisTimeValid(oField, sMsg, iPage, sSep) {
  if (emisEmpty(oField.value)) return true;

  if (!emisChkTime(oField.value.replace(/ /g, ""), sSep)) {
    //alert("「" + sMsg + "」欄位時間格式錯誤！");
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage('time_is_invalidation', sMsg), callback: function () {
        setFocus(oField);
      }});
    } else {
      alert(jQuery.getMessage('time_is_invalidation', sMsg));
      //		emisPageChg(iPage);
      //		oField.focus();
      setFocus(oField);
    }
    return false;
  }

  if (!emisEmpty(sTimeSepa_) && oField.value.indexOf(sTimeSepa_) <= 0)
    oField.value = oField.value.substr(0, 2) + sTimeSepa_ + oField.value.substr(2, 4);

  return true;
}

// 判斷欄位是否為統一編號
// oField : 欄位物件
// sMsg   : 欄位名稱訊息
// bCheck : 是否必須 Check 正確才可輸出
function emisP_IDValid(oField, sMsg, bCheck, iPage) {
  var _sValue = oField.value.replace(/ /g, "");  // 去空白
  var _sMsg = emisChkP_ID(_sValue);
  if (_sMsg != "") {
    //alert("「" + sMsg + "」欄位" + _sMsg);
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage('field_valid_result', sMsg) + _sMsg});
    } else {
      alert(jQuery.getMessage('field_valid_result', sMsg) + _sMsg);
    }
    if (!emisEmpty(bCheck)) {
      //			emisPageChg(iPage);
      //			oField.focus();
      setFocus(oField);
      return false;
    }
  }
  oField.value = _sValue;
  return true;
}

// 判斷欄位是否為身份證號
// oField : 欄位物件
// sMsg   : 欄位名稱訊息
// bCheck : 是否必須 Check 正確才可輸出
function emisID_NOValid(oField, sMsg, bCheck, iPage) {
  var _sValue = oField.value.toUpperCase().replace(/ /g, "");  // 大寫,去空白
  var _sMsg = emisChkID_NO(_sValue);
  if (_sMsg != "") {
    //alert("「" + sMsg + "」欄位" + _sMsg);
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage('field_valid_result', sMsg) + _sMsg});
    } else {
      alert(jQuery.getMessage('field_valid_result', sMsg) + _sMsg);
    }
    if (!emisEmpty(bCheck)) {
      //			emisPageChg(iPage);
      //			oField.focus();
      setFocus(oField);
      return false;
    }
  }
  oField.value = _sValue;
  return true;
}

// 判斷欄位是否為統一發票號碼
// oField : 欄位物件
// sMsg   : 欄位名稱訊息
// iPage  : 此欄位分頁位置
function emisInvoiceValid(oField, sMsg, iPage) {
  var _sValue = oField.value.toUpperCase().replace(/ /g, "");  // 大寫,去空白
  var _sMsg = emisChkInvoice(_sValue);
  if (_sMsg != "") {
    //alert("「" + sMsg + "」欄位" + _sMsg);
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage('field_valid_result', sMsg) + _sMsg, callback: function () {
        setFocus(oField);
      }});
    } else {
      alert(jQuery.getMessage('field_valid_result', sMsg) + _sMsg);
      //		emisPageChg(iPage);
      //		oField.focus();
      setFocus(oField);
    }
    return false;
  }
  oField.value = _sValue;
  return true;
}

// 判斷編號欄位之起始編號迄是否大於終止編號
// oField1: 起始欄位物件
// oField2: 結束欄位物件
// sMsg   : 欄位名稱訊息
// sUMsg  : 自訂訊息
// iPage  : 此欄位分頁位置
function emisSeqValid(oField1, oField2, sMsg, sUMsg, iPage) {
  var _bValid = true;
  if (oField1.value != "" && oField2.value != "") {
    if (isNaN(oField1.value) || isNaN(oField2.value)) {
      if (oField1.value > oField2.value) _bValid = false;
    } else {
      // 2005/05/18 [3246]Frankie
      // 2015/03/26 Joe Fix: 修正当数值比较时判断错误，如 9>150
      if (oField1.value - 0 > oField2.value - 0) _bValid = false;
    }

    if (!_bValid) {
      //alert(emisEmpty(sUMsg)? "「" + sMsg + "」起始不可大於終止！": sUMsg);
      var sUMsg = emisEmpty(sUMsg) ? jQuery.getMessage('sequence_begin_over_end_error', sMsg) : sUMsg;
      if( typeof(ymPromptAlert) == "function") {
        ymPromptAlert({msg:sUMsg, callback:function(){
          setFocus(oField1);
        }});
      } else {
        alert(sUMsg);
        //			emisPageChg(iPage);
        //			//20040822 Jacky [612] 若Focus的物件Disable則不跳Focus
        //			if(! oField1.disabled)
        //				 oField1.focus();
        setFocus(oField1);
      }
    }
  }
  return _bValid;
}

// 判斷欄位長度是否為超過
// oField : 欄位物件
// iLength: 欄位長度
// sMsg   : 欄位名稱訊息
function emisLengthValid(oField, iLength, sMsg, iPage) {
  // [863] Frankie start
  if (!emisEmpty(oField)) {
    var _sPic = oField.getAttribute("PICURE");
  }

  if (_sPic == "S") {
    var sMessage = emisChkNum(oField);
    if (sMessage != "") {
      if (typeof(ymPromptAlert) == "function") {
        ymPromptAlert({msg: sMessage, callback: function () {
          setFocus(oField);
        }});
      } else {
        alert(sMessage);
        //			emisPageChg(iPage);
        //			oField.focus();
        setFocus(oField);
      }
      return false;
    }
    if ("+" == oField.value.substring(0, 1)) {   // 若輸入的"+",則去除該欄位"+"
      oField.value = oField.value.substring(1, (oField.value.length));
    }
  }
  // [863] Frankie end

  if (!emisChkLength(oField.value, iLength)) {
    //alert("「" + sMsg + "」欄位輸入長度超過 " + iLength + " 字元！");
    if (typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg: jQuery.getMessage('string_length_over_error', sMsg, iLength), callback: function () {
        setFocus(oField);
      }});
    } else {
      alert(jQuery.getMessage('string_length_over_error', sMsg, iLength));
      //		emisPageChg(iPage);
      //		oField.focus();
      setFocus(oField);
    }
    return false;
  }
  return true;
}

// 判斷E-Mail欄位否為合法
// oField : 欄位物件
function emisChkEMailValid(oField, sMsg, iPage) {
  if (!emisChkEMail(oField.value)) {
    //alert("「" + sMsg + "」欄位必須輸入\"@\"和\".\"符號");
    if (typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg: jQuery.getMessage('email_required_character', sMsg), callback: function () {
        setFocus(oField);
      }});
    } else {
      alert(jQuery.getMessage('email_required_character', sMsg));
      //		emisPageChg(iPage);
      //		oField.focus();
      setFocus(oField);
    }
    return false;
  }
  return true;
}

// 判斷WWW欄位否為合法
// oField : 欄位物件
function emisChkWWWValid(oField, sMsg, iPage) {
  if (!emisChkWWW(oField.value)) {
    //alert("「" + sMsg + "」欄位必須輸入\".\"符號");
    if (typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage('url_required_character', sMsg), callback: function () {
        setFocus(oField);
      }});
    } else {
      alert(jQuery.getMessage('url_required_character', sMsg));
      //		emisPageChg(iPage);
      //		oField.focus();
      setFocus(oField);
    }
    return false;
  }
  return true;
}

// *************************************************************************


// ************************* 訊息處理函數 **********************************
// MsgBox 顯示訊息
// emisMsgBox(顯示字串, 顯示類型, 預設按鈕, 對話框預設值, 對話框Size, 對話框MaxLength, 輸入對話框之 Picture)
// 顯示類型: "?":確認訊息, "T":多重訊息, "F":資訊訊息, "I":輸入對話框, "P":密碼對話框
// 輸入對話框之Picture: sPicture="U" 轉換英文字母為大寫 ...
function emisMsgBox(sStr, sType, iDefault, sInput, iSize, iMaxLength, sPicture) {
  _sMsgStr_ = sStr;
  _sMsgType_ = sType;
  _iMsgDefault_ = emisEmpty(iDefault) ? 1 : iDefault;
  _sMsgInput_ = sInput;
  _iMsgSize_ = emisEmpty(iSize) ? 10 : iSize;
  _iMsgMaxLength_ = emisEmpty(iMaxLength) ? _iMsgSize_ : iMaxLength;
  _sMsgPicture_ = sPicture;

  // 各顯示類別預設值
  var _iWidth = 170;
  var _iHeight = 130;
  var _iNowLength = 15;
  if (sType == "?") {        // 確認訊息
    _iWidth = 260;
    _iNowLength = 24;
  }
  else if (sType == "T") {   // 多重訊息
    _iWidth = 360;
    _iNowLength = 40;
  }
  else if (sType == "I" || sType == "P") {   // 輸入對話框 & 密碼對話框
    _iWidth = 260 + (_iMsgSize_ - 10) * 10;
    _iNowLength = 10;
  }
  else if (sType == "M") {//用於存儲使用快捷鍵Alt+S時會出現值並沒有改變時所彈出的對話框 add by chou 2006/10/08 Track+[6868]
    _iWidth = 260;
    _iNowLength = 24;
  }
  // 20120730 add by Joe: 优化高宽计算逻辑 start
  _sMsgStr_ = sStr.replace(/</g, '&lt;').replace(/>/g, '&gt;').split("\n").join("<br/>");
  (function () {
    var box = document.getElementById("emis_message_box");
    if (!box) {
      var tmpBox = document.createElement("div");
      tmpBox.id = "emis_message_box";
      tmpBox.setAttribute("id", "emis_message_box");
      // 預設為 msgbox.htm 字體為 9pt
      tmpBox.style.fontSize = "9pt";
      tmpBox.style.visibility = "hidden";
      tmpBox.style.zIndex = "99999";
      tmpBox.style.display = "inline";
      tmpBox.style.position = "absolute";
      document.body.appendChild(tmpBox);
      delete tmpBox
      box = document.getElementById("emis_message_box");
    }
    box.innerHTML = "<div style='display:table;'>" + _sMsgStr_ + ((_iMsgSize_ || 0) > 0 ? "<input type='text' size='" + _iMsgSize_ + "'>" : "") + "</div>";
    var innerBox = box.getElementsByTagName("div")[0];
    _iHeight = Math.max(innerBox.offsetHeight, _iHeight);
    _iWidth = Math.max(innerBox.offsetWidth, _iWidth) + 150;
  })();
  /*
   // 處理輸入之字串
   var _aStr=sStr.split("\n");
   var _iMaxLength=0;
   var _iLength;
   for (var i=0; i<_aStr.length; i++) {
   _iLength=emisLength(_aStr[i])
   if (_iLength>_iMaxLength)
   _iMaxLength=_iLength;
   }
   // 依最大字數增加 Width
   if (_iMaxLength>_iNowLength)
   _iWidth=_iWidth + (_iMaxLength-_iNowLength)*10;  // 預設為 msgbox.htm 字體為 9pt

   // 依行數增加 Height
   _iHeight=_iHeight + (_aStr.length-1)*10;

   // For 螢幕 Size 處理
   if (screen.availWidth!=800) {
   _iWidth=_iWidth*screen.availWidth/800;
   _iHeight=_iHeight*screen.availHeight/600;
   }
   */
  // 20120730 add by Joe: 优化高宽计算逻辑 end
  //alert(_iWidth);
  var _sRetStr = emisShowModal("../../js/emis_msg.htm", _iWidth, _iHeight);

  if ((sType == "I" || sType == "P") && _sRetStr == "Cancel")
    return "";
  else
    return _sRetStr;
}

// 顯示詢問訊息  emisAnswer(訊息字串, 預設按鍵)
// emisAnswer("是否確定欲刪除此資料？ <Y/N>\n11\n22\n33", 2);
// emisAnswer("是否確定欲儲存此資料？ <Y/N/C>", 1, "T");
// sStr:訊息字串
// iDefault:預設按鍵
// sType:顯示類型
function emisAnswer(sStr, iDefault, sType) {
  sType = emisEmpty(sType) ? "?" : sType;
  return emisMsgBox(sStr, sType, iDefault);
}

// 顯示訊息  emisMsg(顯示字串)
// emisMsg("資料庫總計筆數:100筆！");
// sStr:訊息字串
function emisMsg(sStr) {
  return emisMsgBox(sStr, "F");
}

// 輸入對話框  emisPrompt(提示字串, 預設值, Size, MaxLength)
// emisPrompt("請輸入編號:", "0000001", 20);
function emisPrompt(sStr, sInput, iSize, iMaxLength, sPicture) {
  return emisMsgBox(sStr, "I", 1, sInput, iSize, iMaxLength, sPicture)
}
// *************************************************************************


// ************************* 視窗處理函數 **********************************
// 開啟 ShowModal 畫面, 不可切回上一畫面
// sURL   : 開啟之網頁
// iWidth : 視窗寬度, 不傳預設為 300px
// iHeigth: 視窗高度, 不傳預設為 100px
function emisShowModal(sURL, iWidth, iHeight) {
  iWidth = (emisEmpty(iWidth) ? 300 : iWidth);
  iHeight = (emisEmpty(iHeight) ? 100 : iHeight);
  if (!jQuery.browser.msie && !jQuery.browser.mozilla
      && typeof emisMask == "object" && typeof emisMask.show == "function") {
    emisMask.show();
  }
  return window.showModalDialog(sURL, window,
      "dialogWidth=" + iWidth + "px;dialogHeight=" + iHeight + "px;" +
      "dialogTop=" + ((screen.availHeight - iHeight) / 2) + "px;dialogLeft=" + ((screen.availWidth - iWidth) / 2) + "px;" +
      "center=yes;border=thin;help=no;menubar=no;toolbar=no;location=no;directories=no;status=no;resizable=no;scrollbars=no");
}

// 開啟一新視窗
// sURL   : 開啟之網頁
// iWidth : 視窗寬度, 不傳預設為 300px
// iHeigth: 視窗高度, 不傳預設為 100px
// iTop   : 視窗 Top 座標, 不傳預設為依解析度置中
// iLeft  : 視窗 Left 座標, 不傳預設為依解析度置中
function emisWinOpen(sURL, iWidth, iHeight, iTop, iLeft, sName) {
  sURL = encodeParam(sURL);
  if (arguments.length == 1) {
    var win = window.open(sURL);
    if (typeof emisWinManager != "undefined")
      emisWinManager.push(win);
    return win;
  }

  // -1表示開最大視窗
  if (iWidth == -1) iWidth = screen.availWidth;
  if (iHeight == -1) iHeight = screen.availHeight - 20;

  iWidth = (emisEmpty(iWidth) ? 300 : iWidth);
  iHeight = (emisEmpty(iHeight) ? 100 : iHeight);

  // 未傳座標則將視窗置中
  if (arguments.length < 5) {
    iTop = (screen.availHeight / 2) - (iHeight / 2) - 1;
    iLeft = (screen.availWidth / 2) - (iWidth / 2) - 1;
  }

  sName = (emisEmpty(sName) ? "" : sName);

  var win = window.open(sURL, sName,
      "width=" + iWidth + "px,height=" + iHeight + "px," +
      "top=" + iTop + "px,Left=" + iLeft + "px," +
      "border=thin,help=no,menubar=no,toolbar=no,location=no,directories=no,status=no,resizable=0,scrollbars=1");
  if (typeof emisWinManager != "undefined")
    emisWinManager.push(win);
  return win;
}

// 開一個視窗再傳入idForm的輸入值, 讓指定.jsp能用getParameter()取出輸入值
// sURL : 開啟之網頁
// sName: 網頁名稱
// oForm: submit Form 名稱
function emisOpen(sURL, sName, oForm) {
  var _sFlag = "width=300px,height=100px,status=1,scrollbars=1,resizable=1,hotkeys=0" +
               ",top=" + ((screen.availHeight - 200) / 2) +
               ",left=" + ((screen.availWidth - 400) / 2);
  // 無傳入 Form Object 則以第一個 Form
  var _oForm = oForm ; //add by Ben [3339] 20050530
  if (emisEmpty(oForm))
    _oForm = document.forms[0]; // modify by Ben [3339] 20050530
  // sName 以 Random 產生, 以避免會不同作業會開同一視窗
  sName = emisEmpty(sName) ? (Math.floor(Math.random() * 10000000000) - 0) + "" : sName;
  var _oWin = window.open("about:blank", sName, _sFlag);
  var _sAction = _oForm.action;
  var _sTarget = _oForm.target;

  _oForm.action = sURL;
  _oForm.target = sName;
  _oForm.submit();

  // 還原 action, target
  _oForm.action = _sAction;
  _oForm.target = _sTarget;
  return _oWin;
} // emisOpen()

//提供excel 报表列印
function emisRptOpen(sURL, sName, oForm) {
  //var _sFlag = "alwaysRaised=yes,border=thin,help=no,menubar=no,toolbar=no,location=no,directories=no,status=no,resizable=no,scrollbars=1,width=700px,height=100px,top=300,left=200";
  // 无传入 Form Object 则以第一个 Form
  var _oForm = oForm ; //add by Ben [3339] 20050530
  if (emisEmpty(oForm))
    _oForm = document.forms[0];

  sName = "rptdataframe007";
  if (!document.getElementsByName(sName)[0]) {
    if (navigator.userAgent.indexOf("MSIE") != -1) {
      var _oObject = document.createElement("<iframe name='" + sName + "' style='display:none'></iframe>");
      _oForm.insertBefore(_oObject);
    } else {
      var _oObject = document.createElement("iframe");
      _oObject.name = sName;
      _oObject.style.display = "none";

      _oForm.parentNode.insertBefore(_oObject, _oForm);
    }
  }
  var _sAction=_oForm.action;
  var _sTarget=_oForm.target;
  _oForm.action = sURL;
  _oForm.target = sName;
  _oForm.submit();
  _oForm.action = _sAction;
  _oForm.target = _sTarget;
  return true;
} // emisRptOpen()

// 調整 Style 屬性
// oStyle : 欲調整 Style 之物件
// sStyle : 欲調整 Style 之屬性
// aAttrib: 欲調整 Style 之屬性值 aAttrib[0]:800*600之處理,
//                                aAttrib[1]:1024*768之處理
function emisAdjustStyle(oStyle, sStyle, aAttrib) {
  if (sStyle.toLowerCase() == "height") {
    if (screen.availWidth >= 1024)
      oStyle.height = aAttrib[0];
    else
      oStyle.height = aAttrib[1];
  }
}

// 顯示跑馬燈畫面
// emis_marquee(window.idSpan, "start");
// emis_marquee(window.idSpan, "end");
// oObj: 處理之 span 物件
// sAction: 目前之動作, "start"=開始, "end"=結束
// sMsg: 顯示之訊息, 不傳入則顯示 "資料處理中，請稍待片刻．．．．"
function emisMarquee(oObj, sAction, sMsg) {
  if (emisEmpty(sMsg)) sMsg = jQuery.getMessage('marquee_msg'); //"資料處理中，請稍待片刻．．．．";
  if (sAction == "start")
    oObj.innerHTML = "<marquee id=marMsg scrolldelay=150>" +
        "<b style='color:#CC6666 ; cursor:hand'>" + sMsg + "</b></marquee>";
  else if (sAction == "end") {
    formobj('marMsg').disabled = true;
    oObj.innerHTML = "";
  }
  else
    oObj.innerHTML = "<marquee id=marMsg scrolldelay=150>" +
        "<b style='color:#CC6666 ; cursor:hand'>" + sMsg + "</b></marquee>";
}


// 畫面分頁處理
// iPage 檢查目前頁籤是否符合
//2005/01/27 [1507] 增加頁籤控管
//
function emisPageChk(iPageInto) {
  var iPage = iPageInto - 1;        // 將頁碼 -1, 以配合 Array 處理
  if (! emisEmpty(aPageRights_)) {
    if (aPageRights_[iPage] != "Y") {
      return false;
    }
  }
  return true;
}


//------- 2004/08/30 added by Frankie 633 start  -------//
//獲得會員卡終止日期的預設值
//_sStr1 : 起始日期欄位值
//_sStr2 : 卡別
//return : 該卡有效期限
function getEnd_Date(_sStr1, _sStr2) {
  var _iDiv,_iRound,_sRet,_sDay,_sDate,_sYear,_sMonth;
  //獲取有效月數
  var _sRet = emisGetData("ACT=getend_date&CRD_NO=" + _sStr2 + "&Title=CUST");
  //取整獲取對應的年數
  _iDiv = Math.floor(_sRet / 12);       // 2004/08/24 modified by Frankie 636
  //取模獲取月數
  _iRound = _sRet % 12;
  _sYear = parseInt(emisYear(_sStr1)) + _iDiv;
  _sMonth = (emisMonth(_sStr1) - 0) + _iRound;
  //判斷相加後的月數是否大於12，即跨年,如果大於12,則變量年需加1
  if (_sMonth / 12 > 1) {
    _sYear = _sYear + 1;   //(_sMonth/12);  [1101],zoe,計算跨年時,原算法會產生小數點,造成客戶卡別資料到期日期錯誤,應加1即可
    _sMonth = _sMonth % 12;
  }
  //處理月份，如果月份小於10，則需在月份前面補零，反之，不用;
  if (_sMonth < 10) _sDate = _sYear.toString() + "/" + "0" + _sMonth.toString();
  else _sDate = _sYear.toString() + "/" + _sMonth.toString();
  //判斷是否有閏年對應的月份-2月，若有則2月為29天，若無則為28天
  if (_sMonth == 1 || _sMonth == 3 || _sMonth == 5 || _sMonth == 7 || _sMonth == 8 || _sMonth == 10 || _sMonth == 12) {
    _sDay = "31";
  } else if (_sMonth == 4 || _sMonth == 6 || _sMonth == 9 || _sMonth == 11) {
    _sDay = "30";
  } else {
    if (((_sYear % 4 == 0) && (_sYear % 100 != 0)) || (_sYear % 400 == 0)) _sDay = "29";
    else _sDay = "28";
  }
  //返回處理後的日期作為終止日期的預設值
  return  _sDate = _sDate + "/" + _sDay;
}
//------- 2004/08/31 added by Frankie 633 start  -------//

// 將字串內的加號先轉成%2B, 使Java能正確接收到, 否則會變成空白.
// sMsg: 傳入要轉換的字串
function emisEscape(sMsg) {
  if (sMsg.indexOf("+") >= 0) {
    sMsg = sMsg.replace(/\+/g, "%2B");
  }
  return sMsg;
}

//[1326], zoe, 自動檢核縣市區域的郵遞區號,並填到zip欄位的function
function changeAreabyZipChg(oCity, oArea, oAreaTmp, oZip) {
  if (emisEmpty(oZip.value)) {
    oCity.value = "";
    emisOptionMutiSel(oAreaTmp, oArea, oCity.value, oCity.value);
    oArea.value = "";
  } else {
    var temno = emisGetData("ACT=getCity_Area" + "&ZIPCODE=" + oZip.value);  //取得郵遞區號相對應的地址
    //此getCity_Area要自行在xml撰寫,可參考批銷作業wslivprn.xml的作法
    oCity.value = temno[0];
    emisOptionMutiSel(oAreaTmp, oArea, oCity.value, oCity.value);
    oArea.value = temno[0] + temno[1];
  }
}

/** 2005/04/25 [3041] andy start:emisOnblurBefore()和emisOnchangeAfter()兩函數 */
// 欄位onblur事件處理公用函數
// @author:andy 2005/04/25
// 參數:oObject - 處理之欄位
// 調用範例:emisOnblurBefore(this);
function emisOnblurBefore(oObject) {
  if (emisEmpty(oObject)
      || typeof(oObject) != "object" || oObject.disabled || oObject.readOnly
      || typeof(eval("window.fncOnblurBefore" + oObject.name)) != "function"
      ) {
    return true;
  }

  try {
    //調用前端各作業中所定議的欄位onblur時需作其他處理的函數.
    //如欄位名為P_NO,前端所定義之函數應為:fncOnblurBeforeP_NO(obj) (參數名obj可任意命名)
    var isSuccessed = eval("window.fncOnblurBefore" + oObject.name + "(oObject);");
    return isSuccessed
  } catch (e) {
    if (typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:"fncOnblurBefore" + oObject.name + "() error！"});
    } else {
      alert("fncOnblurBefore" + oObject.name + "() error！");
    }
    return false;
  }
}


// 欄位onchange事件處理公用函數
// @author:andy 2005/04/25
// 參數:oObject - 處理之欄位
// 調用範例:emisOnblurAfter(this);
function emisOnblurAfter(oObject) {
  if (emisEmpty(oObject)
      || typeof(oObject) != "object" || oObject.disabled || oObject.readOnly
      || typeof(eval("window.fncOnblurAfter" + oObject.name)) != "function"
      ) {
    return true;
  }

  //try {
  //調用前端各作業中所定議的欄位onblur事件時需作其他處理的函數.
  //如欄位名為P_NO,前端所定義之函數應為:fncOnblurAfterP_NO(obj) (參數名obj可任意命名)
  var isSuccessed = eval("window.fncOnblurAfter" + oObject.name + "(oObject);");
  return isSuccessed

  // } catch (e) {
  //   alert("fncOnblurAfter" + oObject.name + "() error！");
  return false;
  /// }
}
/** 2005/04/25 [3041] andy end */

/**取得傳入日期年份的第一周星期一的日期,當前周數,當前周的星期一和星期日的日期
 參數:sDate-傳入之日期,不傳將取當日之日期;
 sSep-日期分隔符,有傳時將傳回的日期加上指定的分隔符;
 返回值:傳回一個有四個元素的數組:
 [0]-第一周星期一的日期
 [1]-周數
 [2]-所屬周的星期一的日期
 [3]-所屬周的星期日的日期
 [4]-當前系統日期
 */
function emisGetWeekInf(sDate, sSep) {
  var _sURL = "ACT=getWeekInf&TITLE=select&DATE=" + sDate;
  var aRetVal = emisGetData(_sURL)[0].split(",");

  if (!emisEmpty(sSep)) {
    aRetVal[0] = emisDate(aRetVal[0], sSep);
    aRetVal[2] = emisDate(aRetVal[2], sSep);
    aRetVal[3] = emisDate(aRetVal[3], sSep);
    aRetVal[4] = emisDate(aRetVal[4], sSep);
  }
  return aRetVal;
}

function AjaxGetEposBillNo(sTable, sField, aCondFld, aCondVal, sFdHead, sDateFld) {
  var sConds = " 1 = 1 ";
  if (!emisEmpty(aCondFld)) {
    for (i = 0; i < aCondFld.length; i++) {
      sConds += " and " + aCondFld[i] + " = ''" + aCondVal[i] + "''";
    }
  }

  url.clear();
  url.add('act', 'ajax_getEposBillNo');
  url.add('TITLE', 'select');
  url.add('FIELD', sField);
  url.add('TABLE', sTable);
  url.add('CONDS', sConds);
  url.add('FD_TYPE', sFdHead);
  url.add('DATEFLD', emisEmpty(sDateFld) ? "CRE_DATE" : sDateFld);

  //return AjaxGetData(url.toString());

  /*var sRetVal = AjaxGetData(url.toString())[0].split(",");
   sRetVal[0] = emisPadl((sRetVal[0].substring(4, 10) - 0) + 1, 6, "0");
   sRetVal[0] = sRetVal[1] + sFdHead + sRetVal[0];
   return sRetVal[0];*/
  //2013/01/07 SQL查询结果已处理好，直接返回即可
  var sRetVal = AjaxGetData(url.toString())[0]
  return sRetVal;
}


function AjaxGetVenusBillNo(sTable, sField, aCondFld, aCondVal, sFdHead, sDateFld) {
  var sConds = " 1 = 1 ";
  if (!emisEmpty(aCondFld)) {
    for (i = 0; i < aCondFld.length; i++) {
      sConds += " and " + aCondFld[i] + " = ''" + aCondVal[i] + "''";
    }
  }

  url.clear();
  url.add('act', 'ajax_getVenusBillNo');
  url.add('TITLE', 'select');
  url.add('FIELD', sField);
  url.add('TABLE', sTable);
  url.add('CONDS', sConds);
  url.add('FD_TYPE', sFdHead);
  url.add('DATEFLD', emisEmpty(sDateFld) ? " " : sDateFld);
  var sRet = AjaxGetData(url.toString());
  if (sRet == -1) {
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:'AjaxGetBillNo error'});
    } else {
      alert('AjaxGetBillNo error');
    }
    return;
  }
  return sRet;
}
function AjaxGetBestLmsBillNo(sTable, sField, aCondFld, aCondVal, sFdHead, sDateFld) {
  var sConds = " 1 = 1 ";
  if (!emisEmpty(aCondFld)) {
    for (i = 0; i < aCondFld.length; i++) {
      sConds += " and " + aCondFld[i] + " = ''" + aCondVal[i] + "''";
    }
  }

  url.clear();
  url.add('act', 'ajax_getBestLmsBillNo');
  url.add('TITLE', 'select');
  url.add('FIELD', sField);
  url.add('TABLE', sTable);
  url.add('CONDS', sConds);
  url.add('FD_TYPE', sFdHead);
  url.add('DATEFLD', emisEmpty(sDateFld) ? " " : sDateFld);
  var sRet = AjaxGetData(url.toString());
  if (sRet == -1) {
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:'AjaxGetBestLmsBillNo error'});
    } else {
      alert('AjaxGetBestLmsBillNo error');
    }
    return;
  }
  return sRet;
}

//Chitty 2012/08/08 add 會員編號區號規則 與 AjaxGetVenusBillNo類似，區別在于
//執行的存儲過程，參數@sFdYear 轉換為8獲取

function AjaxGetVenusCUST_CNO(sTable, sField, aCondFld, aCondVal, sDateFld) {
  var sConds = " 1 = 1 ";
  if (!emisEmpty(aCondFld)) {
    for (i = 0; i < aCondFld.length; i++) {
      sConds += " and " + aCondFld[i] + " = ''" + aCondVal[i] + "''";
    }
  }

  url.clear();
  url.add('act', 'ajax_getVenusCUST_CNO');
  url.add('TITLE', 'select');
  url.add('FIELD', sField);
  url.add('TABLE', sTable);
  url.add('CONDS', sConds);
  url.add('DATEFLD', emisEmpty(sDateFld) ? " " : sDateFld);
  var sRet = AjaxGetData(url.toString());
  if (sRet == -1) {
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:'AjaxGetBillNo error'});
    } else {
      alert('AjaxGetBillNo error');
    }
    return;
  }
  return sRet;
}


function AjaxGetBillNo(sTable, sField, aCondFld, aCondVal, sFdHead, sDateFld) {
  var sConds = " 1 = 1 ";
  if (!emisEmpty(aCondFld)) {
    for (i = 0; i < aCondFld.length; i++) {
      sConds += " and " + aCondFld[i] + " = ''" + aCondVal[i] + "''";
    }
  }

  url.clear();
  url.add('act', 'ajax_getBillNo');
  url.add('TITLE', 'select');
  url.add('FIELD', sField);
  url.add('TABLE', sTable);
  url.add('CONDS', sConds);
  url.add('FD_TYPE', emisEmpty(sFdHead) ? sField : sFdHead);
  url.add('DATEFLD', emisEmpty(sDateFld) ? "CRE_DATE" : sDateFld);

  var sRet = AjaxGetData(url.toString());
  if (sRet == -1) {
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:'AjaxGetBillNo error'});
    } else {
      alert('AjaxGetBillNo error');
    }
    return;
  }

  var sRetVal = sRet[0].split(",");


  if (sRetVal[2] == "Y" && !emisEmpty(sRetVal[1])) {
    sRetVal[0] = emisPadl((sRetVal[0] - 0) + 1, sRetVal[1], "0");
  } else {
    sRetVal[0] = (sRetVal[0] - 0) + 1;
  }

  return sRetVal[0];
}


/**
 * 取得單據最大單號加一的單號,並根據FIELDFORMAT檔設定自動左補零
 * @author:age 2005/10/13
 *    參數:sTable-操作TABLE  sFields-單號對應之字段(欄位)名
 *        aCondFld-條件欄位名數組,與aCondVal對應
 *        aCondVal-條件欄位值數組,與aCondFld對應
 *        sFdType-對應到FIELDFORMAT表中的FD_TYPE,不傳時取sFields
 *  返回值:新單號字串.
 *調用範例:var billNo = emisGetEposBillNo("IMP_PART_H","IMP_NO",["S_NO"],["003"]);
 *   其他:也可用此函數取得表身最大RECNO+1(emisGetEposBillNo("IMP_PART_D","RECNO",["S_NO","IMP_NO"],["003","0000000008"]);)
 */
function emisGetEposBillNo(sTable, sField, aCondFld, aCondVal, sFdHead, sDateFld) {
  var sConds = " 1 = 1 ";
  if (!emisEmpty(aCondFld)) {
    for (i = 0; i < aCondFld.length; i++) {
      sConds += " and " + aCondFld[i] + " = ''" + aCondVal[i] + "''";
    }
  }
  var _sURL = "ACT=ajaxeposGetBillNo&TITLE=select"
      + "&FIELD=" + sField + "&TABLE=" + sTable
      + "&CONDS=" + sConds + "&FD_HEAD=" + sFdHead
      + "&DATEFLD=" + (emisEmpty(sDateFld) ? "CRE_DATE" : sDateFld );

  return AjaxGetData(_sURL)[0];
}


/**取得單據最大單號加一的單號,並根據FIELDFORMAT檔設定自動左補零
 * @author:andy 2005/06/12
 *    參數:sTable-操作TABLE  sFields-單號對應之字段(欄位)名
 *        aCondFld-條件欄位名數組,與aCondVal對應
 *        aCondVal-條件欄位值數組,與aCondFld對應
 *        sFdType-對應到FIELDFORMAT表中的FD_TYPE,不傳時取sFields
 *  返回值:新單號字串.
 *調用範例:var billNo = emisGetBillNo("IMP_PART_H","IMP_NO",["S_NO"],["003"]);
 *   其他:也可用此函數取得表身最大RECNO+1(emisGetBillNo("IMP_PART_D","RECNO",["S_NO","IMP_NO"],["003","0000000008"]);)
 */
function emisGetBillNo(sTable, sField, aCondFld, aCondVal, sFdType, sDateFld) {
  var sConds = " 1 = 1 ";
  // sConds = emisSetWhereCond("",aCondFld,aCondVal);
  if (!emisEmpty(aCondFld)) {
    for (i = 0; i < aCondFld.length; i++) {
      sConds += " and " + aCondFld[i] + " = ''" + aCondVal[i] + "''";
    }
  }

  var _sURL = "ACT=ajax_getBillNo&TITLE=select"
      + "&FIELD=" + sField + "&TABLE=" + sTable
      + "&CONDS=" + sConds + "&FD_TYPE=" + ( emisEmpty(sFdType) ? sField : sFdType )
      + "&DATEFLD=" + (emisEmpty(sDateFld) ? "CRE_DATE" : sDateFld );

  var sRetVal = AjaxGetData(_sURL)[0].split(",");

  if (sRetVal[2] == "Y" && !emisEmpty(sRetVal[1])) {
    sRetVal[0] = emisPadl((sRetVal[0] - 0) + 1, sRetVal[1], "0");
  } else {
    sRetVal[0] = (sRetVal[0] - 0) + 1;
  }

  return sRetVal[0];
}


/* Author:	   	Devin
 * Date: 		    2007/07/16
 * Description: 根據傳入的編號(需為0-Z範圍)返回遞增的下一編號
 *              1.當sNo的長度等於siLength且sNo全為'Z'時將返回 null
 *         	    2.當sNo的長度大於siLength時返回null
 * Parameters:  sNo  	  --編號
 *  		        iLength --返回的編號的長度 (不能小於sNo的長度否則將返回null,若未指定該參數則預設顯sNo的長度)
 *
 * Examples:
 *  		eposGetNextNo('ZZZZ',4) return null
 *  		eposGetNextNo('0001',3) return null
 *  		eposGetNextNo('001Z',5) return 00020
 */

function eposGetNextNo(sNo, iLen) {
  var iPos;    //記錄位置
  var sTmp;    //存儲臨時提取的字符
  var iFlag;   //用於標記是否進位
  var sResult; //用於存儲結果

  iLen = emisEmpty(iLen) ? emisLength(sNo) : iLen;
  iFlag = 0;
  sResult = "";
  iPos = iLen - 1;
  sNo = emisPadl(sNo, iLen, '0');    //產生的編號為若小於指定的長度,則以右補'0'填充

  //若提供的NO大於指定的長度或是為'ZZZZ'不能再繼續Increment時則返回NULL
  if (iLen < emisLength(sNo) || sNo.toUpperCase() == emisPadl('', iLen, 'Z'))
    return null;

  while (iPos >= 0) {
    sTmp = sNo.substr(iPos, 1);
    var iAscCode = sTmp.charCodeAt(0);
    if (iAscCode >= 48 && iAscCode <= 57) {    //0至9
      if (iAscCode == 57) {
        iFlag = 0;
        sTmp = 'A';
      } else {
        iFlag = 0;
        sTmp = String.fromCharCode(iAscCode + 1);
      }
    } else if (((iAscCode >= 65 && iAscCode <= 90)) || ((iAscCode >= 97 && iAscCode <= 122))) { //A至Z
      sTmp = sTmp.toUpperCase();
      iAscCode = sTmp.charCodeAt(0);
      if (iAscCode == 90) {
        iFlag = 1;
        sTmp = '0';
      } else {
        iFlag = 0;
        sTmp = String.fromCharCode(iAscCode + 1);
      }
    }

    sResult = sTmp + sResult;
    iPos = iPos - 1;
    if (iFlag == 0)
      break;
  }

  sResult = sNo.substr(0, iLen - (iLen - iPos) + 1) + sResult;
  return sResult.toUpperCase();
}


//=======================================================epos.js=========================================================

// 自動於所有 Form 上產生 TITLE Hidden 物件 For Business
function emisGenTitle() {
  for (var i = 0; i < document.forms.length; i++) {
    var _oObject = document.createElement("<input type='hidden' name='TITLE' value='" + sBusiTitle_ + "'>");
    document.forms[i].insertBefore(_oObject);
  }
}


// 將輸入之起迄欄位補起迄範圍
// oObject1, oObject2: 起迄欄位物件
// iLength:  自動補入之長度
// sValue :  自動補入字元, 不傳入則預設為 "9"
// sDefault: 自動補入起始欄位字元, 不傳入則預設為 "0"
function emisNumAll(oObject1, oObject2, iLength, sValue, sDefault, btn) {
  if (oObject1.value != '') {  //Jerry 2004/08/25
    oObject1.value = '';
    oObject2.value = '';
    oObject1.focus();
    return true;
  }
  sValue = emisEmpty(sValue) ? "9" : sValue;
  /*
  if (emisEmpty(sDefault))
    oObject1.value = emisReplicate("0", iLength);
  else
    oObject1.value = emisReplicate(sDefault, iLength);
    */
  // 开始栏位不需要按长度补全字符，因为可能实际查询的值跟开始值字符相同，但长度不足时会查询不出来：
  // 如有一笔记录的字段值为00， 如果开始条件为 000000，就查不到 ‘00’这笔了。
  oObject1.value = emisEmpty(sDefault) ? "0":sDefault;

  oObject2.value = emisReplicate(sValue.toUpperCase(), iLength);
  return true;
}

// 將輸入之起始值送至結束值, 並作 select 反白
// oObject1, oObject2: 起迄欄位物件
// sType : 特殊欄位處理, "D":日期格式, "M":年月格式
function emisQrySel(oObject1, oObject2, sType) {
  // 判斷第一個欄位是否超長, 以免造成 dead lock
  if (!emisChkLength(oObject1.value, oObject1.maxLength)) {
    return false;
  }
  // 判斷日期是否正確, 以免造成 dead lock
  if (sType == "D" && !emisChkDate(oObject1.value))
    return false;
  if (sType == "M" && !emisChkMonth(oObject1.value))
    return false;

  if (oObject2.value == "") {
    oObject2.value = oObject1.value;
    try {
      oObject2.select();
    } catch(e) {
    }
  }
  return true;
}

// emis 列印处理, 预设使用 Submit
// sURL: 传入之 URL 字串
// sMode: 开启列印时之视窗型态, 不传则用 winodw.open, "get"=showModal
// sAlert: 列印完成之讯息, 若传入 "NOMESSAGE" 则无资料时不会显示讯息
// 2005/06/02 andy 修改:加入一个参数sFun作为列印后调用的函数
// 2008/05/27 andy 增加一个oform参数，可以取指定FORM的栏位。
function AjaxRpt(sURL, sMode, sAlert, sFun, oForm) {
  try {
    // 统一加上用户数据权限访问的参数
    if (formobj("AC_USER_ID") && sURL.indexOf("&AC_USER_ID=") < 0) {
      sURL += "&AC_USER_ID=" + formobj("AC_USER_ID").value;
    }
  }catch(e){ }

  //判断页面是否有调用YmPrompt方式的弹出层
  if (typeof AjaxRptByYmPrompt == "function"){
    return AjaxRptByYmPrompt(sURL, sMode, sAlert, sFun, oForm);
  }
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
  var _sRptVer = sRptVersion_ ;
  var oRPT_VER = formobj("RPT_VER");
  if (_sURL.indexOf("&RPT_VER=1.0") >= 0
      || (typeof(oRPT_VER) == "object" && oRPT_VER.value == "1.0")) {
    _sRptVer = "1.0";
  }
  //当RPT_VER设为1.0时,不提供报表选择画面,按旧的方式列印.
  if (!emisEmpty(_sRptVer) && _sRptVer != "1.0") {
    sRptURL_ = _sURL;
    if (sRptVersion_ == "1.1") {
      _sRetStr = emisShowModal(basePath + "jsp/ajax_rptkind_sel.jsp", 400, 220);
    } else {
      _sRetStr = emisShowModal(basePath + "jsp/ajax_rptkind_sel.jsp", 400, 280);
    }
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

    if (_sRetStr == "1") {  //文字报表(WSHOW)
      _sURL += "&RPT_OUTER_TYPE=TXT&WREP=false&EXCEL_SINGLE_PAGE=false";
    } else if (_sRetStr == "2") {   //图形报表(WREP)
      _sURL += "&RPT_OUTER_TYPE=TXT&WREP=true&EXCEL_SINGLE_PAGE=false";
    } else if (_sRetStr == "3") {   //EXCEL报表
      var oRPT_OCX_SHOW = formobj("RPT_OCX_SHOW");
      //_sURL += "&RPT_OUTER_TYPE=EXCEL&WREP=false&EXCEL_SINGLE_PAGE=false&RPT_EXCEL_DOWNFILE=Y" ;
      // Excel报表的打开方式改为在IE中直接开启档案的方式.  update by andy
      _sURL += "&RPT_OUTER_TYPE=EXCEL&WREP=false&EXCEL_SINGLE_PAGE=false" ;
      if ((typeof(oRPT_OCX_SHOW) == "object" && oRPT_OCX_SHOW.value == "Y")
          || _sURL.indexOf("RPT_OCX_SHOW=Y") >= 0) {
        ;// 仍使用OCX下载EXCEL报到并打开。
      } else {
        _sURL = _sURL.replace(/rpt_data.jsp/g, "emis_data_execel.jsp");
      }
    } else if (_sRetStr == "4") {   //EXCEL一页式报表
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
      return;
    }
  }

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
    _sRetStr = emisShowModal(_sURL);
    if (typeof(oSpaMessage) != "undefined")
      emisMarquee(oSpaMessage, "end");


    //2004/11/19 [1494] Jacky 修正若以"get"方式处理的错误
    try {
      if (_sRetStr == "") {
        return true;
      }
    } catch(e) {
      if (_sRetStr[0] == "") {
        return true;
      }
    }
    if (sAlert.toUpperCase() != "NOMESSAGE") {
      alert(_sRetStr);
      return false;
    }
  }
  return true;
}
// Ajax新版叫用iReport報表.
// sURL: 必須有act=name以取得XML裡的getsql action
// sReportName: 報表名稱
function AjaxIRpt(sURL, sReportName) {
  var _sURL = sURL;
  _sURL = "../ajax_ireport.jsp?" + sURL +
      ((sURL.indexOf("&TITLE=") > 0 ? "" : "&TITLE=" + emisGetTitle()) +
          '&QRY_RPT_NAME=' + sReportName +
          '&QRY_RPT_KIND=' + formobj("QRY_RPT_KIND").value +
          '&QRY_RPT_TYPE=' + formobj("QRY_RPT_TYPE").value);
  //    alert(_sURL);
  var _oWin = emisOpen(_sURL);
  _oWin.focus();
}

// emis 列印處理 Header
// sHead: Head 之每一欄位之列印抬頭,各欄位以 ","隔開
// sSize: Head 之每一欄位之列印寬度,各欄位以 ","隔開
// sHead: Head 之每一欄位之列印間隔,各欄位以 ","隔開
// oHead: 組好之列印 Head 物件
// oHeadLine: 組好之列印 Head 底線物件
function emisRptHead(sHead, sSize, sGap, oHead, oHeadLine) {
  var _aHead = sHead.split(",");
  var _aSize = sSize.split(",");
  var _aGap = sGap.split(",");
  oHead.value = "";
  oHeadLine.value = "";
  for (var i = 0; i < _aHead.length; i++) {
    oHead.value += emisPadc(_aHead[i], _aSize[i]) + emisReplicate(" ", _aGap[i]);
    oHeadLine.value += emisReplicate("-", _aSize[i]) + emisReplicate(" ", _aGap[i]);
  }
}

// E_MAIL 處理
// sMsg: 處理訊息
function emisMail(sMsg) {
  var _oWin;
  var _sURL = "../emis_mail.jsp?" +
      (emisEmpty(sMsg) ? "" : "MESSAGE=" + sMsg) + (emisEmpty(bDebug_) ? "" : "&DEBUG=true");
  _oWin = emisOpen(_sURL);
  _oWin.focus();

  return true;
}

// E_MAIL 訊息處理
// sRetStr: 傳入之郵寄訊息字串, ""=成功, 其他則為失敗
// sMsg: 寄出成功後 alert 之訊息
function emisMailMsg(sRetStr, sMsg) {
  if (sRetStr == "") {
    if (emisEmpty(sMsg)) {
      if( typeof(ymPromptAlert) == "function") {
        ymPromptAlert({msg:jQuery.getMessage("mail_send_successful")});
      } else {
        alert(jQuery.getMessage("mail_send_successful"));//alert("郵件寄出成功！");
      }
    } else {
      if (sMsg == "NOMESSAGE")
        return true;
      else {
        if( typeof(ymPromptAlert) == "function") {
          ymPromptAlert({msg:sMsg});
        } else {
          alert(sMsg);
        }
      }
    }
    return true;
  } else {
    //alert("郵件無法寄出！\n\n[錯誤訊息]:\n"+sRetStr);   // 顯示錯誤訊息
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage("mail_can_not_send_error") + sRetStr});
    } else {
      alert(jQuery.getMessage("mail_can_not_send_error") + sRetStr);   // 顯示錯誤訊息
    }
    return false;
  }
}

// BarCode 列印處理
function emisBarCode() {
  var _sRetStr = "";
  var _oWin;
  var _sURL = "../emis_data.jsp?ACT=barcode" + "&TITLE=" + emisGetTitle() +
      (emisEmpty(bDebug_) ? "" : "&DEBUG=true");
  _oWin = emisOpen(_sURL);
  _oWin.focus();

  return _sRetStr;
}

/**
 * UpLoad 上載處理
 * @param sURL 傳入之 URL 字串
 * @param bNonIFrame 是否使用iFrame
 */
function emisUpload(sURL, bNonIFrame) {
  var _sRetStr = "";
  var _oWin;
  //用于限制上传文件大小用，配合systab_d(CUSTOM_MAX_POST_SIZE)设定
  var sPMS="?PMS=", oPMS = formobj("POST_MAX_SIZE") || formobj("TITLE");
  if(!!oPMS && !emisEmpty(oPMS.value))
    sPMS += oPMS.value;
  sURL = (emisEmpty(sURL) ? "../" : sURL + "jsp/" ) + "emis_upload.jsp" + sPMS+"&Test=1&a=b";
  if (bNonIFrame) {
    var _sURL = sURL + (emisEmpty(bDebug_) ? "" : ((sURL.indexOf("?") == -1 ? "?" : "&") + "DEBUG=true"));
    _oWin = emisOpen(_sURL);
    _oWin.focus();
  } else {
    var sTarget = "UploadFrame";
    var sId = "id" + sTarget;
    var oFrame = document.getElementById(sId);
    if (emisEmpty(oFrame)) {
      var oDiv = document.createElement("div");
      oDiv.style.display = "none";
      oDiv.innerHTML = ("<iframe name='" + sTarget + "' id='" + sId + "'></iframe>");
      document.body.appendChild(oDiv);
    }
    var _oForm = document.forms[0];
    if (emisEmpty(_oForm)) {
      if( typeof(ymPromptAlert) == "function") {
        ymPromptAlert({msg:jQuery.getMessage('up_no_form_msg')});
      } else {
        alert(jQuery.getMessage('up_no_form_msg')); //"找不到Form表单!"
      }
      return "";
    }
    var _sAction = _oForm.action;
    var _sTarget = _oForm.target;
    sURL = encodeParam(sURL);
    _oForm.action = sURL;
    _oForm.target = sTarget;
    _oForm.submit();

    // 还原 action, target
    _oForm.action = _sAction;
    _oForm.target = _sTarget;
  }
  return _sRetStr;
}
// UpLoad 訊息處理
// sRetStr: 傳入之上載訊息字串, ""=成功, 其他則為失敗
function emisUploadMsg(sRetStr) {
  if (sRetStr == "") {
    //alert("檔案上載成功！");
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage("file_upload_successful")});
    } else {
      alert(jQuery.getMessage("file_upload_successful"));
    }
    return true;
  }
  else {
    //alert("檔案無法上載！\n\n[錯誤訊息]:\n"+sRetStr);   // 顯示錯誤訊息
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:jQuery.getMessage("file_can_not_upload_error") + sRetStr});
    } else {
      alert(jQuery.getMessage("file_can_not_upload_error") + sRetStr);    // 顯示錯誤訊息
    }
    return false;
  }
}

// 取得郵遞區號
// sAddr   : 地址
// oZipCode: 郵遞區號欄位
function emisZipCode(sAddr, oZipCode) {
  if (!emisEmpty(oZipCode) && emisEmpty(oZipCode.value)) {
    var _sURL = "../emis_zipcode.jsp?ADDR=" + sAddr;
    var _sZipCode = emisShowModal(_sURL);
    if (!emisEmpty(oZipCode))
      oZipCode.value = _sZipCode;
  }
  return _sZipCode;
}


// robert, 用來取代 emisSelect ,只改了使用 ajax_sel.jsp
var AjaxSelectXML;
function AjaxSelect(sURL, setComponent, oNoDataFocus, oNextFocus, sMsg, iWidth, iHeight) {
  // 當 oNoDataFocus 即輸入之欄位有被改為 readOnly 或 Disable, 則不處理
  if (oNoDataFocus.readOnly || oNoDataFocus.disabled)
    return true;

  iWidth = emisEmpty(iWidth) ? 760 : iWidth;
  iHeight = emisEmpty(iHeight) ? 520 : iHeight;

  var _aRetStr = new Array();
  var _oWin;
  // 若不傳入會取 select.xml
  var _selectURL = sRoot + "/jsp/ajax_sel.jsp?" + encodeParam(sURL);

  if (!do_ajax_request(sURL)) {
    return false;
  }
  //	if( my_check_error( request.responseXML) ) {
  if (my_check_error(parseXmlDom(request))) {
    return false;
  }
  //	AjaxSelectXML = request.responseXML;
  AjaxSelectXML = parseXmlDom(request);
  var emptyFlag = "?????";
  var datas = AjaxSelectXML.getElementsByTagName('data');
  if (datas.length > 0) {
    var empty = datas[0].getAttribute("empty") ;
    if (empty == "true") {
      _aRetStr[0] = emptyFlag;
    } else {
      var rows = AjaxSelectXML.getElementsByTagName("_r");

      if (rows.length > 1) {
        // 開窗
        if (emisEmpty(bDebug_))
          _aRetStr = emisShowModal(_selectURL, iWidth, iHeight);
        else {
          _oWin = window.open(_selectURL);
          _oWin.focus();
          return false;
        }
      } else if (rows.length == 0) {
        _aRetStr[0] = emptyFlag;
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
      }
    }
  } else {
    _aRetStr[0] = emptyFlag;
  }

  if (_aRetStr[0] == emptyFlag) {
    //alert((emisEmpty(sMsg)?"":"「" + sMsg + "」欄位輸入錯誤，\n\n") + "無任何查詢資料，請重新輸入！")
    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:(emisEmpty(sMsg) ? "" : jQuery.getMessage("query_input_error", sMsg)) + jQuery.getMessage("query_nothing_msg"), callback:function(){
        oNextFocus.focus();
        if (!emisEmpty(oNoDataFocus)) oNoDataFocus.focus();
      }});
    } else {
      alert((emisEmpty(sMsg) ? "" : jQuery.getMessage("query_input_error", sMsg)) + jQuery.getMessage("query_nothing_msg"));
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
      if(_aRetStr[0] == "" || typeof(ymPromptAlert) != "function") {
        oNextFocus.focus();
        if (!emisEmpty(oNoDataFocus)) oNoDataFocus.focus();
      }
      return false;
    }

    // 有選取資料將值回傳
    var notFocusFlds = "";
    for (var i = 0; i < setComponent.length; i++) {
      // 處理單引號
      try {

        //alert(setComponent[i] + "='" + _aRetStr[i] +"'");
        _aRetStr[i] = _aRetStr[i].replace(new RegExp("'", "g"), "\\'");
        eval(setComponent[i] + "='" + _aRetStr[i] + "'");
      } catch(e) {
        //alert("無法設定欄位值 : " +  setComponent[i] )
        //alert(jQuery.getMessage("query_can_not_focus") + setComponent[i])
        notFocusFlds += "[" +setComponent[i] +"]";
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
      return false;
    }
    if (!currentTable) return  false;

    // 有選取資料將值回傳
    // setComponent  >> Fileds  >> Field Name Array
    // _aRetStr      >> Query Result  [[001,002,003],[商品1,商品2,商品3]]
    // currentTable  >> masterTbl
    // 1. 檢查可被回填的空行
    // 2. 依字段名字把數據回填
    (function() {
      var i, j, k = 0, len1, len2, isEmptyRow;

      // 拆分子元素
      for (i = 0,len1 = _aRetStr.length; i < len1; i++)
        _aRetStr[i] = _aRetStr[i].split(",");

      // 檢查可被回填的空行
      for (i = 0,len1 = currentTable.totalRowCount; i < len1; i++) {
        isEmptyRow = true;
        for (j = 0,len2 = setComponent.length; j < len2; j++) {
          if (currentTable.get(i, setComponent[j]) != "") {
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
        for (j = 0,len2 = setComponent.length; j < len2; j++) {
          currentTable.set(i, setComponent[j], _aRetStr[j][k]);
        }
        k++;
      }
      currentTable.applyDomValue2Ui();
    })();
  }
  try {
    oNextFocus.focus();    // 移至下一欄位輸入
  } catch(e) {
    //alert("駐點無法移至下一欄位!")
  }
  return true;
}

// 取得 Select Option 物件之 Name 值
// oOption: select option 物件
// oObj   : 取得之 Name 值之物件
// bAll   : 是否取全部 Text 值
// 傳回為所取之 select option 物件以空白之 Name 值
// 用於 SqlCache 所產生之 select 為 NO + " " +NAME, 而取出 Name
function emisOptionName(oOption, oObj, bAll) {
  var _iSelIndex = oOption.selectedIndex;
  if (_iSelIndex < 0)
    return "";

  var _sName = new String(oOption.options[_iSelIndex].text);
  if (emisEmpty(bAll)) {
    var _aName = _sName.split(" ");
    _sName = "";
    for (var i = 1; i < _aName.length; i++)
      _sName = _sName + (i == 1 ? "" : " ") + _aName[i];

    if (!emisEmpty(oObj))
      oObj.value = _sName;
  }

  return _sName;
}

// 設定 Select Option 物件之預設值
// oOption: select option 物件
// sValue : 預設之值
function emisOptionSet(oOption, sValue) {
  var _iLength = oOption.length;
  for (var i = 0; i < _iLength; i++) {
    if (oOption.options[i].value == sValue) {
      oOption.options[i].selected = true;
      break;
    }
  }

  return true;
}

// 自動尋找 Select Option 物件之值
// oOption: select option 物件
// sValue : 輸入之值
function emisOptionSearch(oOption, sValue) {
  var _iLength = oOption.length;
  var _sOpValue;
  oOption.value = null;   // 將 Option 清空
  for (var i = 0; i < _iLength; i++) {
    _sOpValue = oOption.options[i].value;
    if (sValue == _sOpValue.substring(0, sValue.length)) {
      oOption.value = oOption.options[i].value;
      break;
    }
  }
}

// 切換多層次下拉選項處理
// oSurOption: 來源 select option 物件
// oTarOption: 目的 select option 物件
// sValue1 : 起始過濾 oSurOption value 之值
// sValue2 : 結束過濾 oSurOption value 之值
// iStart  : 取得 oSurOption value 之值之起始 Byte
// iEnd    : 取得 oSurOption value 之值之結束 Byte
// bNoEmpty: 是否不產生第一筆為空白
// iValueStart  : value 取得 oSurOption value 之值之起始 Byte
// iValueEnd    : value 取得 oSurOption value 之值之結束 Byte
function emisOptionMutiSel(oSurOption, oTarOption, sValue1, sValue2, iStart, iEnd, bNoEmpty, iValueStart, iValueEnd) {
  oTarOption.length = 0;   // 將 Option 清空
  // sValue1 is Empty, or sValue1 > sValue2 則不處理
  if (emisEmpty(sValue1) || (!emisEmpty(sValue2) && sValue1 > sValue2)) {
    return true;
  }
  var _iSurLength = oSurOption.length;
  var _sStr = "";
  var _iStart = emisEmpty(iStart) ? 0 : iStart;
  var _iEnd = emisEmpty(iEnd) ? sValue1.length : iEnd;
  var _iValueStart = emisEmpty(iValueStart) ? 0 : iValueStart;
  var _iValueEnd = emisEmpty(iValueEnd) ? sValue1.length : iValueEnd;
  var _oOption;
  // 預設新增一筆空白
  if (emisEmpty(bNoEmpty)) {
    _oOption = document.createElement("OPTION");
    _oOption.value = "";
    _oOption.text = "";
    oTarOption.add(_oOption);
  }
  for (var i = 0; i < _iSurLength; i++) {
    _sStr = new String(oSurOption.options[i].value).substring(_iStart, _iEnd);
    if ((emisEmpty(sValue2) && _sStr == sValue1) ||
        (!emisEmpty(sValue2) && _sStr >= sValue1 && _sStr <= sValue2)) {
      _oOption = document.createElement("OPTION");
      if (emisEmpty(_iValueStart))
        _oOption.value = oSurOption.options[i].value;
      else
        _oOption.value = oSurOption.options[i].value.substring(_iValueStart, _iValueEnd);
      _oOption.text = oSurOption.options[i].text;
      oTarOption.add(_oOption);
    }
  }
  return true;
}

// 複製下拉選項物件
// oSurOption: 來源 select option 物件
// oTarOption: 目的 select option 物件
function emisOptionCopy(oSurOption, oTarOption) {
  oTarOption.length = 0;   // 將 Option 清空
  var _iSurLength = oSurOption.length;
  var _oOption;
  for (var i = 0; i < _iSurLength; i++) {
    _oOption = document.createElement("OPTION");
    _oOption.value = oSurOption.options[i].value;
    _oOption.text = oSurOption.options[i].text;
    oTarOption.add(_oOption);
  }
  return true;
}

// 依傳入之欄位Array之物件產生 URL, 以供 Save 處理
// aFieldName:recordset field Name 以供增修時寫回 recordset 欄位
function AjaxCompURL(aFieldName) {
  var _sURL = [];
  var _oElm;
  for (var i = 0; i < aFieldName.length; i++) {
    // 處理資料中 "%" 符號而無法透過 URL 傳過
    if (typeof(_oElm = formobj(aFieldName[i])) != "undefined") {
      _sURL.push(aFieldName[i] + "=" + _oElm.value);
    } // 2005/07/02 add by andy start
    else if (typeof(_oElm = formobj('spa' + aFieldName[i])) != "undefined") {
      _sURL.push(aFieldName[i] + "=" + _oElm.innerText);
    } // 2005/07/02 add by andy end
  }
  return _sURL.length > 0 ? "&" + _sURL.join("&") : "";
}

/**
 * 检查是否为函数对象
 * @param obj
 * @returns {boolean}
 */
function isFunction(obj){
  return Object.prototype.toString.call(obj) === "[object Function]";
}
/**
 * 检查是否为数组对象
 * @param obj
 * @returns {boolean}
 */
function isArray( obj ) {
  return Object.prototype.toString.call(obj) === "[object Array]";
}

//add for Jim  2013/05/06 start
/**
 * 依傳入之欄位Array之物件產生此 Value 之 Array, 以供 ajaxTabUpdRecord() 使用
 * @param aFieldName 栏位名称集合  currentTable.domIds 以供增修時寫回 currentTable 欄位
 * @param dataParser 数据转换器
 * @returns {Array} 返回与栏位名称对应的值数组
 * @constructor
 */
function AjaxCompArray(aFieldName, dataParser) {
  var _oElm;
  var _aValue = new Array(aFieldName.length);
  for (var i = 0; i < aFieldName.length; i++) {
    if (typeof(_oElm = formobj(aFieldName[i])) != "undefined")
      _aValue[i] = _oElm.value || "";
    if (typeof(_oElm = formobj('spa' + aFieldName[i])) != "undefined")
      _aValue[i] = _oElm.innerText || "";
    else if (/CHKDEL|CHKUPD/gi.test(aFieldName[i]))
      _aValue[i] = "0";
    // 最后调用作业自己定义的转换方法
    if(isFunction(dataParser))
      _aValue[i] = dataParser(aFieldName[i], _aValue[i]);
    // 处理undefined
    _aValue[i] = _aValue[i] || "";
  }
  return _aValue;
}  //end ajaxCompArray

// 依傳入之欄位Array之物件欄位清空
// aFieldName:recordset field Name 以供增修時寫回 recordset 欄位
function AjaxCompClear(aFieldName, bKeep) {
  var _oElm;
  for (var i = 0; i < aFieldName.length; i++) {
    if (typeof(_oElm = formobj(aFieldName[i])) != "undefined") {
      if (bKeep && (_oElm.disabled || _oElm.readOnly)) continue;
      _oElm.value = "";
    } // 2005/07/02 add by andy start
    if (typeof(_oElm = formobj('spa' + aFieldName[i])) != "undefined") {
      _oElm.innerText = "";
    } // 2005/07/02 add by andy end
  }
}

// ************************** 物件顯示處理 *********************************

// 不顯示傳入之物件陣列(不佔物件位置) AJAX版，不使用eval()
// aObj: 處理之物件 Array
function AjaxNonDisplay(aObj) {
  for (var i = 0; i < aObj.length; i++) {
    var _oObj = aObj[i];
    if (typeof(_oObj) != "undefined") {
      _oObj.style.display = "none";
      try {
        //如有日期下拉选框一起隐藏。
        var name = _oObj.getAttribute("name");
        if (emisEmpty(name)) {
          name = _oObj.id;
        }
        var dsobj = formobj("selDateScope" + name);
        if (dsobj == undefined && "1".indexOf(name.substring(name.length - 1)) >= 0) {
          dsobj = formobj("selDateScope" + name.substring(0, name.length - 1));
        }
        if (dsobj != undefined) {
          dsobj.style.display = "none";
          dsobj.value = "";
        }
      }catch(e){}
    }
  }
} // emisNonDisplay( )

// 顯示傳入之物件陣列(不佔物件位置) AJAX版，不使用eval()
// aObj: 處理之物件 Array
function AjaxDisplay(aObj) {
  for (var i = 0; i < aObj.length; i++) {
    var _oObj = aObj[i];
    if (typeof(_oObj) != "undefined") {
      _oObj.style.display = "";
      try {
        //如有日期下拉选框一起显示。
        var name = _oObj.getAttribute("name");
        if (emisEmpty(name)) {
          name = _oObj.id;
        }
        var dsobj = formobj("selDateScope" + name);
        if (dsobj == undefined && "1".indexOf(name.substring(name.length - 1)) >= 0) {
          dsobj = formobj("selDateScope" + name.substring(0, name.length - 1));
        }
        if (dsobj != undefined) {
          dsobj.style.display = "";
          dsobj.value = "";
        }
      }catch(e){}
    }
  }
} // emisDisplay( )


// 從 emisDiable 稍為改一下來的,不使用 document.all
function AjaxDisable(_oElms, bColor) {
  if (typeof _oElms == 'undefined') return;
  if (_oElms && _oElms.constructor != Array
      && (_oElms.nodeName == 'SELECT' || ( _oElms.nodeName != 'SELECT' && typeof _oElms.length == 'undefined'))) {
    _oElms = [_oElms];
  }
  for (var k = 0; k < _oElms.length; k++) {
    var _oObj = _oElms[k];
    if (_oObj == undefined) continue;

    _oObj.disabled = true;

    if (_oObj.type && _oObj.type.indexOf("text") >= 0)
      _oObj.style.border = "1px solid #C9C7BA";
    // 判斷是否有 image 檔顯示時作處理, 將 image 圖檔變 Disable

    // 處理像 <button...><img....></button> 這種
    if (typeof( _oObj.childNodes) != 'undefined') {
      if (typeof(_oObj.childNodes.length) != 'undefined') {
        for (var i = 0; i < _oObj.childNodes.length; i++) {
          if (_oObj.childNodes[i].nodeName == "IMG") {
            _oObj.childNodes[i].style.filter = "alpha(opacity=30)";  // IE
            _oObj.childNodes[i].style.opacity = ".3";
            /*Opera9.0+、Firefox1.5+、Safari、Chrome*/
          }
        }
        //if (_sIObj.indexOf("img")>=0 && typeof(_oIObj)!="undefined") {
        // 	_oIObj.style.filter="alpha(opacity=30)";
        //}
      }
    }

    // 判斷名稱是否有 "imgCalendar"+名稱 和"selDateScope"+名称的物件, 若有要一起做disable
    // 用erosm/jsp/test/calendar1.jsp測試
    var name = _oObj.getAttribute("name");
    if (emisEmpty(name)) {
      name = _oObj.id;
    }
    try {
      var _sIObj = eval("formobj('imgCalendar" + name + "')");
      if (_sIObj != undefined) {
        _sIObj.disabled = true;
        _sIObj.style.filter = "alpha(opacity=30)";   // IE
        _sIObj.style.opacity = ".3";
        /*Opera9.0+、Firefox1.5+、Safari、Chrome*/
      }

      _sIObj = formobj("selDateScope"+name);
      if(_sIObj == undefined && "1" == name.substring(name.length-1)) {
        _sIObj = formobj("selDateScope" + name.substring(0,name.length - 1));
      }
      if (_sIObj != undefined) {
        _sIObj.disabled = true;
        _sIObj.value = "";
        _sIObj.style.backgroundColor = sDisabledBGColor_;
      }
    } catch(e) {
    }
    ;

    // 將顏色設為淺藍色
    if (!emisEmpty(bColor) && !/button/i.test(_oObj.getAttribute("type")))
      _oObj.style.backgroundColor = sDisabledBGColor_;

  }

} // AjaxDisabled( )
function AjaxEnable(_oElms, bColor) {
  if (typeof _oElms == 'undefined') return;
  if (_oElms && _oElms.constructor != Array
      && (_oElms.nodeName == 'SELECT' || ( _oElms.nodeName != 'SELECT' && typeof _oElms.length == 'undefined'))) {
    _oElms = [_oElms];
  }

  for (var k = 0; k < _oElms.length; k++) {
    var _oObj = _oElms[k];
    if (_oObj == undefined) continue;

    _oObj.disabled = false;

    if (_oObj.type && _oObj.type.indexOf("text") >= 0)
      _oObj.style.border = "1px solid #7F9DB9";
    // 判斷是否有 image 檔顯示時作處理, 將 image 圖檔變 Disable

    // 處理像 <button...><img....></button> 這種
    if (typeof( _oObj.childNodes) != 'undefined') {
      if (typeof(_oObj.childNodes.length) != 'undefined') {
        for (var i = 0; i < _oObj.childNodes.length; i++) {
          if (_oObj.childNodes[i].nodeName == "IMG") {
            //alert('enable img');
            _oObj.childNodes[i].style.filter = ""; // IE
            _oObj.childNodes[i].style.opacity = "";
            /*Opera9.0+、Firefox1.5+、Safari、Chrome*/
            break;
          }
        }
        //if (_sIObj.indexOf("img")>=0 && typeof(_oIObj)!="undefined") {
        // 	_oIObj.style.filter="alpha(opacity=30)";
        //}
      }
    }

    // 判斷名稱是否有 "imgCalendar"+名稱和"selDateScope"+名称 的物件, 若有要一起做disable
    // 用erosm/jsp/test/calendar1.jsp測試
    var name = _oObj.getAttribute("name");
    if (emisEmpty(name)) {
      name = _oObj.id;
    }
    try {
      var _sIObj = formobj('imgCalendar' + name);
      if (_sIObj != undefined) {
        //alert('a');
        _sIObj.disabled = false;
        _sIObj.style.filter = ""; // IE
        _sIObj.style.opacity = "";
        /*Opera9.0+、Firefox1.5+、Safari、Chrome*/
      }
      _sIObj = formobj("selDateScope"+name);
      if(_sIObj == undefined && "1" == name.substring(name.length-1)) {
        _sIObj = formobj("selDateScope" + name.substring(0,name.length - 1));
      }
      if (_sIObj != undefined) {
        _sIObj.disabled = false;
        _sIObj.value = "";
        _sIObj.style.backgroundColor = '#FFFFFF';
      }
    } catch(e) {
    }
    ;

    // 將顏色設為白色
    if (!emisEmpty(bColor) && !/button/i.test(_oObj.getAttribute("type")))
      _oObj.style.backgroundColor = '#FFFFFF';
    _oObj.style.color = "";
  }
} // AjaxEnable( )


// 處理 Form 之物件 Enable, Disable
// oFormObj: Form 物件
// sAttrib : ENABLE, DISABLE
// sEnColor, sEnBgColor: Enable 之前,背景顏色
// sDisColor, sDisBgColor: Disable 之前,背景顏色
function emisFormAttrib(oFormObj, sAttrib, sEnColor, sEnBgColor, sDisColor, sDisBgColor) {
  sEnColor = emisEmpty(sEnColor) ? "" : sEnColor;
  sEnBgColor = emisEmpty(sEnBgColor) ? "" : sBgEnColor;
  sDisColor = emisEmpty(sDisColor) ? "#ACA899" : sDisColor;
  sDisBgColor = emisEmpty(sDisBgColor) ? sDisabledBGColor_ : sBgDisColor;
  // sDisBgColor=emisEmpty(sDisBgColor)? "#CCFFFF": sBgDisColor;

  var _oElement = new Object;
  var _sEleType = "";
  // 2010/01/26 Joe Modify: 改寫通用語法
  var images = oFormObj.getElementsByTagName('img');
  for (var i = 0; i < images.length; i++) {
    _oElement = images[i];
    //alert("id="+_oElement.id + ",name=" + _oElement.name);
    //因日期欄位的按鈕名稱現修改為imgCalendar+日期欄位名了,所以其檢核條件也要作修改 update by andy
    if (_oElement.name.indexOf("imgCalendar") == 0 || _oElement.name == "imgCalendar") {
      if (sAttrib.toUpperCase() == "DISABLE") {
        _oElement.disabled = true;
        _oElement.style.filter = "alpha(opacity=30)";  // IE
        _oElement.style.opacity = ".3";
        /*Opera9.0+、Firefox1.5+、Safari、Chrome*/
      } else {
        _oElement.disabled = false;
        _oElement.style.filter = ""; // IE
        _oElement.style.opacity = "";
        /*Opera9.0+、Firefox1.5+、Safari、Chrome*/
      }
    }
  }

  for (var i = 0; i < oFormObj.elements.length; i++) {
    _oElement = oFormObj.elements[i];
    _sEleId = _oElement.id.toUpperCase();
    _sEleType = _oElement.type;

    // 僅處理不是 Button 及 Hidden 物件
    if (_sEleType.indexOf("button") < 0 && _sEleType.indexOf("hidden") < 0 &&
        _sEleId.indexOf("PAGESIZE") < 0) {
      if (sAttrib.toUpperCase() == "DISABLE") {
        // 瀏覽模式, Disable
        if (_sEleType.indexOf("select") >= 0 || _sEleType.indexOf("checkbox") >= 0 || _sEleType.indexOf("radio") >= 0)
          _oElement.disabled = true;    // type: select, checkboc, radio無 readOnly 屬性
        else {
          _oElement.disabled = false;
          _oElement.readOnly = true;
        }
        if (_sEleType.indexOf("text") >= 0)
          _oElement.style.border = "1px solid #C9C7BA";
        _oElement.style.color = sDisColor;
        _oElement.style.backgroundColor = sDisBgColor;
      }
      else {
        // 編輯模式, Enable
        _oElement.disabled = false;
        _oElement.readOnly = false;
        _oElement.style.color = sEnColor;
        _oElement.style.backgroundColor = sEnBgColor;
        if (_sEleType.indexOf("text") >= 0)
          _oElement.style.border = "1px solid #7F9DB9";
      }
      // 將字體強迫改為 9pt
      //_oElement.style.fontSize = "9pt";
    }
  }
}

// 判斷 Form 之所有物件 是否有輸入值
// oFormObj : Form 物件
// oFocusObj: 無任何物件有輸入值之 Focus 物件
// sMsg     : 無任何物件有輸入值之訊息
function emisFormEmpty(oFormObj, oFocusObj, sMsg) {
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
    //alert(emisEmpty(sMsg)? "請輸入欲查詢之條件 ！": sMsg);
    sMsg = emisEmpty(sMsg) ? jQuery.getMessage("query_have_not_condition") : sMsg;

    if( typeof(ymPromptAlert) == "function") {
      ymPromptAlert({msg:sMsg, callback:function(){
        setFocus([oFocusObj]);
      }});
    } else {
      alert(sMsg);
      setFocus([oFocusObj]);
    }
    return false;
  }
  return true;
}

// 取得 Business TITLE 值
function emisGetTitle() {
  var _sTitle = "window.sBusiTitle_";
  var _sBusiTitle = _sTitle;

  // 先取得本網頁之 sBusiTitle_
  if (typeof(eval(_sBusiTitle)) != "undefined")
    return eval(_sBusiTitle);

  // 取得父網頁之 sBusiTitle_
  _sBusiTitle = "window.parent." + _sTitle;
  if (typeof(eval(_sBusiTitle)) != "undefined")
    return eval(_sBusiTitle);

  try {
    // 取得 Opener 網頁之 sBusiTitle_
    _sBusiTitle = "window.opener." + _sTitle;
    if (typeof(eval(_sBusiTitle)) != "undefined")
      return eval(_sBusiTitle);
  } catch(e) {
    try {
      // 取得 dialog 網頁之 sBusiTitle_
      _sBusiTitle = "dialogArguments." + _sTitle;
      if (typeof(eval(_sBusiTitle)) != "undefined")
        return eval(_sBusiTitle);
    } catch(e) {
    }
  }
  return "";
}

// 開啟...選擇視窗, 並填所選值到元件欄位內.
// @param oField 輸入的元件
// @param iLen 欄寬
// @param sChar 左補零的字元("0")
// @param _true
// @author Jerry
// @version 2004/08/27
// 2005/03/23 ANDY 從標準版移至WTN
function emisOpenSelectWindow(oField, iLen, sChar, _true) {
  if (emisEmpty(oField.value)) return true;  //為空時不補零  update by andy
  // onblur的欄位是oField, 其activeElement則是被操作的下一個元件, 如...視窗
  //var _sName = oField.document.activeElement.name;
  //var _sName = oField.document.activeElement.name;
  // 2013/01/25 Joe 取下一個相鄰的物件
  //var _sName = jQuery(oField).next().first().attr("name");
  // 2013/03/06 Joe 取下一個可獲得焦點的物件
  var _sName = (function() {
    var focusables = jQuery(":focusable");  // 依賴Ajax_table.js
    var currentIndex = focusables.index(oField),
        next = focusables.eq(currentIndex + 1).length ? focusables.eq(currentIndex + 1) : focusables.eq(0);
    return jQuery(next).attr("name");
  })();
  var _sValue = oField.value;
  // alert(_sName);

  //2004/10/11 [1175] Jacky 判斷若內容有%則補零
  if (oField.value.indexOf("%") < 0)
    oField.value = emisPadl(oField.value, iLen, sChar, _true);

  // 2013/08/19 Joe 取消自动触发三点按钮逻辑
  /*if (_sName == "btn" + oField.name) {  // 元件是btnQRY_P_NO1才要開啟...視窗
   if (typeof(eval("formobj('btn" + oField.name+"')") == "object")) {
   //eval("document.all.btn" + oField.name + ".onclick()");
   eval("jQuery(formobj('btn" + oField.name + "')).click()");
   return true;
   }
   }*/
  return true;
}


//取得物件數組當前操作的是第幾個元素
//_obj:當前操作物件(一般傳this)
//_array:數組對像(如document.all.P_NO)
function wtnGetAElement(_obj, _array, oMsTbl) {
  var i = 0,j = 0;
  // 2010/09/11 joe add 当画面只有一笔资料时且当前栏位为Select物件_array 不是数组,直接返回-1
  if (_array && _array.nodeName == "SELECT") return -1;
  for (; i < _array.length; i++) {
    j++;
    if (_obj == _array[i]) break;
  }
  if (j == 0) return -1;    //不為數組時返回-1
  else if (oMsTbl) return oMsTbl.datapagesize * oMsTbl.currentPage + i;
  else return i;
}
/**
 * 清空窗體中text物件的值
 * 參數:oFcsObj - 清除後獲得焦點欄位.
 * @param oFcsObj 处理完成后焦点定位物件
 * @param bKeep  是否保留唯读或禁用栏位的值
 */
function wtnFormClear(oFcsObj, bKeep) {
  var node,_oFormObj = document.forms[0].elements;

  for (var i = 0; i < _oFormObj.length; i++) {
    node = _oFormObj[i];
    if (!node || !node.type) continue;
    if (bKeep && (node.disabled || node.readOnly)) continue;

    if (node.type.indexOf("text") >= 0 || node.nodeName == 'SELECT' || node.nodeName == 'TEXTAREA') {
      node.value = "";
    } else if (node.type.indexOf("radio") >= 0 || node.type.indexOf("checkbox") >= 0) {
      node.checked = false;
    }
  }
  if (!emisEmpty(oFcsObj) && typeof(oFcsObj) != "undefined" && !oFcsObj.disabled) {
    oFcsObj.focus();
  }
}

//清空SPAN物件的值
//參數:aSpan-span物件名數組(如:["spaP_NAME","spaP_ENO1"]);
function wtnFormClearSpans(aSpan) {
  for (i = 0; i < aSpan.length; i++) {
    var oSpans = docobjs(aSpan[i]);
    for (j = 0; j < oSpans.length; j++) {
      oSpans[j].innerText = "";
    }
  }
}


// 判斷時間是否正確
// obj: 時間物件
// sSep:分隔符號
function wtnChkTime(obj, sSep) {
  sSep = emisEmpty(sSep) ? ":" : sSep;
  var sTime;
  var _sHH, _sMM,_sSS;
  if ((obj.value).indexOf(sSep) == -1) {    //沒有輸入分隔符號
    sTime = obj.value;
    _sHH = emisPadl(sTime.substr(0, 2), 2, "0");
    _sMM = emisPadl(sTime.substr(2, 2), 2, "0");
    _sSS = emisPadl(sTime.substr(4, sTime.length), 2, "0");
  } else {                                //有輸入分隔符號
    _sHH = wtnSubstr(obj.value, sSep);
    _sMM = wtnSubstr(obj.value.substr(_sHH.length + 1, obj.length), sSep);
    _sSS = wtnSubstr(obj.value.substr((_sHH + _sMM).length + 2, obj.length), sSep);
    if (_sSS == "" && _sMM.length > 2) {
      _sSS = _sMM.substr(2, _sMM.length);
      _sMM = _sMM.substr(0, 2);
    }
    //小時,分鐘,秒數前補零
    _sHH = emisPadl(_sHH, 2, "0");
    _sMM = emisPadl(_sMM, 2, "0");
    _sSS = emisPadl(_sSS, 2, "0");
  }
  sTime = emisPadl(_sHH, 2, "0") + emisPadl(_sMM, 2, "0") + emisPadl(_sSS, 2, "0");

  if (sTime.length > 6)  return false;
  if (_sHH < "00" || _sHH >= "24" || _sMM < "00" || _sMM >= "60"
      || _sSS < "00" || _sSS >= "60") {
    return false;
  } else {
    obj.value = _sHH + sSep + _sMM + sSep + _sSS;
    return true;
  }
}
//根據指定的字符截取字串,配合上面時間欄位的函數wtnChkTime.
function wtnSubstr(sStr, sSep) {
  if (sStr == "" || sSep == "") return sStr;
  if (sStr.indexOf(sSep) == -1) return sStr;
  else {
    return sStr.substr(0, sStr.indexOf(sSep));
  }
}


/**
 * 共用取數據函數(不用再在每個作業的XML中都去寫一個ACT,前端調用也比較方便)
 * @author:andy 2005/06/16
 *    參數:sTable-操作TABLE
 *        sFields-需取得之字段(欄位)名,可以為多個,以逗號隔開 ,傳空串時預設取FLS_NO
 *        aCondFld-條件欄位名數組,與aCondVal對應
 *        aCondVal-條件欄位值數組,與aCondFld對應
 *        sOtherCond-其他特殊條件(如:FLS_NO != '0');
 *  返回值:取得的數組值.
 * 調用範例:
 * var aRetVal = wtnGetData("PO_H","FLS_NO",["S_NO","NO"],["003","0000000001"]);
 * var aRetVal = wtnGetData("IMP_PART_H","ENTER_DATE",["S_NO","IMP_NO"],["003","0000000006"]);
 * var aRetVal = wtnGetData("IMP_PART_H","ENTER_DATE,REMARK,IS_HQ",["S_NO","IMP_NO"],["003","0000000006"]);
 * var aRetVal = wtnGetData("IMP_PART_H","Count(1) as CNT",["S_NO"],["003"],"IS_H != '1'");
 */
function wtnGetData(sTable, sField, aCondFld, aCondVal, sOtherCond) {
  var sConds = " 1 = 1";

  if (emisEmpty(sField)) {
    sField = " FLS_NO ";
  }

  sConds = emisSetWhereCond("", aCondFld, aCondVal, sOtherCond);

  var _sURL = "ACT=ajax_GetData&TITLE=select"
      + "&FIELD=" + sField + "&TABLE=" + sTable
      + "&CONDS=" + sConds;
  var sRetVal = AjaxGetData(_sURL);

  return sRetVal;
}
/**
 * 組條件字串函數
 * 參數:sCondName-條件字串接收物件(如:"CONF_WHERE";可以傳空字串)
 *     aCondFld-條件欄位名數組,與aCondVal對應
 *     aCondVal-條件欄位值數組,與aCondFld對應
 *     sOtherCond-其他特殊條件(運算符不是"="的)
 * 返回值:組合好的條件字串
 */
function emisSetWhereCond(sCondName, aCondFld, aCondVal, sOtherCond) {
  var sRetCond = " 1 = 1";
  if (!emisEmpty(aCondFld)) {
    for (i = 0; i < aCondFld.length; i++) {
      sRetCond += " and " + aCondFld[i] + " = '" + aCondVal[i] + "'";
    }
  }

  if (!emisEmpty(sOtherCond)) {
    sRetCond += " and " + sOtherCond;
  }

  if (!emisEmpty(sCondName)) {
    eval("formobj('" + sCondName + "')").value = sRetCond;
  }
  return sRetCond;
}
/**
 * 組結案檢核的URL 字串 (這樣只需在主jsp頁面定義一個setConfInf的函數,其他頁面可不作其他修改)
 */
function emisCompConfURL() {
  var sConfURL = "";

  if (typeof(setConfInf) == "function") {
    setConfInf();
    sConfURL = "&CONF_TABLE=" + formobj('CONF_TABLE').value
        + "&CONF_WHERE=" + formobj('CONF_WHERE').value
  } else if (typeof(window.parent.setConfInf) == "function") {
    window.parent.setConfInf();
    sConfURL = "&CONF_TABLE=" + formobj('CONF_TABLE', 0, window.parent).value
        + "&CONF_WHERE=" + formobj('CONF_WHERE', 0, window.parent).value
  } else if (typeof(dialogArguments) == "object"
      && typeof(dialogArguments.setConfInf) == "function") {
    dialogArguments.setConfInf();
    sConfURL = "&CONF_TABLE=" + formobj('CONF_TABLE', 0, dialogArguments).value
        + "&CONF_WHERE=" + formobj('CONF_WHERE', 0, dialogArguments).value
  } else if (typeof(dialogArguments) == "object"
      && typeof(dialogArguments.window.parent.setConfInf) == "function") {
    dialogArguments.window.parent.setConfInf();
    sConfURL = "&CONF_TABLE=" + formobj('CONF_TABLE', 0, dialogArguments).value
        + "&CONF_WHERE=" + formobj('CONF_WHERE', 0, dialogArguments).value
  } else if (typeof(window.opener) == "object"
      && (typeof(window.opener.setConfInf) == "function"
          || typeof(window.opener.setConfInf) == "object")) {
    window.opener.setConfInf();
    sConfURL = "&CONF_TABLE=" + formobj('CONF_TABLE', 0, window.opener).value
        + "&CONF_WHERE=" + formobj('CONF_WHERE', 0, window.opener).value
  }
  return sConfURL;
}

// 傳回的是字串才要繼續查詢
function selectAgain(value) {
  return typeof(value) == "string";
}

// add by age 顯示單據狀態.
//  FLS_NO = 單據狀態欄位的名字.


// 如: document.all.spaFLS_NO.innerText = checkFLS_NO("FLS_NO");
function checkFLS_NO(FLS_NO) {
  var obj = docobjs(FLS_NO)[0];
  var FLS_NO_N = obj ? obj.value : FLS_NO;
  var FLS_NO_V = "";
  if (FLS_NO_N == "ED" || FLS_NO_N == "1") {
    //FLS_NO_V = "編輯";
    FLS_NO_V = jQuery.getMessage("fls_no_ed");
  } else if (FLS_NO_N == "CO") {
    //FLS_NO_V = "結案";
    FLS_NO_V = jQuery.getMessage("fls_no_co");
  } else if (FLS_NO_N == "CF" || FLS_NO_N == "3") {
    //FLS_NO_V = "確認";
    FLS_NO_V = jQuery.getMessage("fls_no_cf");
  } else if (FLS_NO_N == "FF" || FLS_NO_N == "2") {//add by tommer.xie 2009/06/12 (背景確認)用於2V_批銷作業排程確認
    //FLS_NO_V="背景確認";
    FLS_NO_V = jQuery.getMessage("fls_no_ff");
  } else if (FLS_NO_N == "MO" || FLS_NO_N == "6") {
    //FLS_NO_V = "月結";
    FLS_NO_V = jQuery.getMessage("fls_no_mo");
  } else if (FLS_NO_N == "9") {
    //FLS_NO_V = "已沖銷";
    FLS_NO_V = jQuery.getMessage("fls_no_9");
  } else if (FLS_NO_N == "CL") {
    //FLS_NO_V = "註銷";
    FLS_NO_V = jQuery.getMessage("fls_no_cl");
  } else if (FLS_NO_N == "CS") { //chou 2006/11/29 加入CS(背景結案)狀態用於2C_進貨單據排程結案
    //FLS_NO_V = "背景結案";
    FLS_NO_V = jQuery.getMessage("fls_no_cs");
  } else if (FLS_NO_N == "CG") {  // add by devin.xie 2007/10/20 FLS_NO = "CG" 即 "配送中"
    //FLS_NO_V = "配送中";
    FLS_NO_V = jQuery.getMessage("fls_no_cg");
  } else if (FLS_NO_N == "SH") {  // add by devin.xie 2007/10/20 FLS_NO = "SH" 即 "出貨"{
    //FLS_NO_V = "出貨";
    FLS_NO_V = jQuery.getMessage("fls_no_sh");
  } else if (FLS_NO_N == "PP") { // add by devin.xie 2007/10/20 FLS_NO = "PP" 即 "提出"
    //FLS_NO_V = "提出";
    FLS_NO_V = jQuery.getMessage("fls_no_pp");
  } else if (FLS_NO_N == "DG") { // add by devin.xie 2007/10/20 FLS_NO = "DG" 即 "送貨"
    //FLS_NO_V = "送貨";
    FLS_NO_V = jQuery.getMessage("fls_no_dg");
  } else if (FLS_NO_N == "BL") { // add by devin xie 2007/10/20  FLS_NO = "BL" 即 "退訂"
    //FLS_NO_V = "退訂";
    FLS_NO_V = jQuery.getMessage("fls_no_bl");
  } else if (FLS_NO_N == "DL") { // add by devin xie 2007/10/20  FLS_NO = "BL" 即 "退訂"
    //FLS_NO_V = "刪除";
    FLS_NO_V = jQuery.getMessage("fls_no_dl");
  } else if (FLS_NO_N == "AP") { // add by devin xie 2007/10/20  FLS_NO = "BL" 即 "退訂"
    //FLS_NO_V = "核準";
    FLS_NO_V = jQuery.getMessage("fls_no_ap");
  } else if (FLS_NO_N == "AG") { //pear 2008/8/05 以下的??
    //FLS_NO_V = "採購同意";
    FLS_NO_V = jQuery.getMessage("fls_no_ag");
  } else if (FLS_NO_N == "GP") {
    //FLS_NO_V = "採購主管確認";
    FLS_NO_V = jQuery.getMessage("fls_no_gp");
  } else if (FLS_NO_N == "EX") {
    //FLS_NO_V = "轉出貨";
    FLS_NO_V = jQuery.getMessage("fls_no_ex");
  } else if (FLS_NO_N == "GN") {
    //FLS_NO_V = "已制單";
    FLS_NO_V = jQuery.getMessage("fls_no_gn");
  } else if (FLS_NO_N == "FX") {
    //FLS_NO_V = "已送廠商";
    FLS_NO_V = jQuery.getMessage("fls_no_fx");
  } else if (FLS_NO_N == "GR") {
    //FLS_NO_V = "部分驗收";
    FLS_NO_V = jQuery.getMessage("fls_no_gr");
  } else if (FLS_NO_N == "0") {//dana 2009/04/09 2K背景刪除
    //FLS_NO_V = "背景刪除";
    FLS_NO_V = jQuery.getMessage("fls_no_0");
  } else if (FLS_NO_N == "RJ") {//zero 2011/04/12 11F 拒绝
    //FLS_NO_V = "拒绝";
    FLS_NO_V = jQuery.getMessage("fls_no_rj");
  }
  return FLS_NO_V;
}


/**
 * harry 2005/10/27  add
 * pF_TAX       : 稅別  2:內含 ; 1:外加 ; 3: 免稅
 * dTAX         : 稅率
 * dAMT         : 單據金額 default = 0
 * dP_DISC_RATE : 折扣比率  default = 0
 * dP_SDISC_AMT : 折讓金額  default = 0
 * return       : 未稅金額, 稅額, 含稅金額
 * countTAX(2, 0.05, 1000, 0.1, 10)
 * 2005/11/02 chou modify 將emisRound小數位改為0(去掉稅額的小數位)
 * 2013/09/20 Joe modify 將emisRound小數位改為4(恢复稅額的小數位)
 */
function countTAX(iF_TAX, dTAX, dAMT, dP_DISC_RATE, dP_SDISC_AMT) {
  var dF_WOTAXAMT = 0;  //未稅金額
  var dF_TAXAMT = 0;    //稅    額
  var dF_AMT = 0;       //含稅金額
  dTAX = dTAX/100.00;

  if (dAMT == null || dAMT == 0) {
    return [0,0,0];
  }
  if (dP_DISC_RATE == null) dP_DISC_RATE = 0;
  if (dP_SDISC_AMT == null) dP_SDISC_AMT = 0;

  if (iF_TAX == "1") {  //外加
    dF_WOTAXAMT = emisRound(dAMT * (100 -dP_DISC_RATE)/100 - dP_SDISC_AMT,4);
    dF_TAXAMT = emisRound(dF_WOTAXAMT * dTAX, 4);
    dF_AMT = dF_WOTAXAMT + dF_TAXAMT;
  } else if (iF_TAX == "2") {  //內含
    dF_AMT = emisRound(dAMT * (100 -dP_DISC_RATE)/100 - dP_SDISC_AMT,4);
    dF_TAXAMT = emisRound((dF_AMT / (1 + parseFloat(dTAX))) * dTAX, 4);
    dF_WOTAXAMT = dF_AMT - dF_TAXAMT;
  } else if (iF_TAX == "3") { //免稅
    dF_TAXAMT = 0;
    dF_WOTAXAMT = dF_AMT = emisRound(dAMT * (100 -dP_DISC_RATE)/100 - dP_SDISC_AMT,4);
  } else {
    return [dAMT,0,0]; // 代收 ?
  }
  return [emisRound(dF_WOTAXAMT,4),emisRound(dF_TAXAMT,4),emisRound(dF_AMT,4)];
}

// 重新reload SQLCACHE
// 叫emisReloader servlet
function emisReloadSQLCACHE() {
  /*
  window.open("../../servlet/com.emis.servlet.emisReloader?target=sqlcache", "SQLCache",
      "width=100" + ",height=50" +
      ",top=0,left=0,status=yes,scrollbars=yes,resizable=no,hotkeys=no").close();
      */
  jQuery.ajax({
    url: "../../servlet/com.emis.servlet.emisReloader",
    data:"target=SQLCache",
    async: true,  //异步
    dataType: "script"
  });
}

function emisReloadProp(){
  jQuery.ajax({
    url: "../../servlet/com.emis.servlet.emisReloader",
    data:"target=prop",
    async: true,  //异步
    dataType: "script"
  });
}

//把val 賦值給 ele
function emisSetValue(ele, val) {
  if (val == null) {
    val = "";
  }

  var orig = ele;
  ele = Emis$(ele);
  if (ele == null) {
    return false;
  }

  if (emisIsHTMLElement(ele, "select")) {
    var found = false;
    var i;

    for (i = 0; i < ele.options.length; i++) {
      if (ele.options[i].value == val) {
        ele.options[i].selected = true;
        found = true;
      } else {
        ele.options[i].selected = false;
      }
    }

    if (found) {
      return true;
    }

    for (i = 0; i < ele.options.length; i++) {
      if (ele.options[i].text == val) {
        ele.options[i].selected = true;
        break;
      }
    }

    return true;
  }

  if (emisIsHTMLElement(ele, "input")) {
    switch (ele.type) {

      case "checkbox":
      case "check-box":
      case "radio":
        ele.checked = (val == true);
        return true;

      default:
        ele.value = val;
        return true;
    }
  }

  if (emisIsHTMLElement(ele, "textarea")) {
    ele.value = val;
    return true;
  }

  ele.innerHTML = val;
  return true;
}

//是否為HTML Elenment
function emisIsHTMLElement(ele, nodeName) {

  if (ele == null || typeof ele != "object" || ele.nodeName == null) {
    return false;
  }

  if (nodeName != null) {
    var test = ele.nodeName.toLowerCase();
    if (typeof nodeName == "string") {
      return test == nodeName.toLowerCase();
    }

    if (emisIsArray(nodeName)) {
      var match = false;
      for (var i = 0; i < nodeName.length && !match; i++) {
        if (test == nodeName[i].toLowerCase()) {
          match = true;
        }
      }
      return match;
    }
  }
  return false;
}

function emisGetValue(ele) {
  var orig = ele;
  ele = Emis$(ele);
  if (ele == null) {
    return;
  }

  if (emisIsHTMLElement(ele, "select")) {
    var sel = ele.selectedIndex;
    if (sel != -1) {
      var reply = ele.options[sel].value;
      if (reply == null || reply == "") {
        reply = ele.options[sel].text;
      }

      return reply;
    } else {
      return "";
    }
  }

  if (emisIsHTMLElement(ele, "input")) {
    switch (ele.type) {

      case "checkbox":
      case "check-box":
      case "radio":
        return ele.checked;

      default:
        return ele.value;
    }
  }

  if (emisIsHTMLElement(ele, "textarea")) {
    return ele.value;
  }

  return ele.innerHTML;
}

/*
 將復選框中選中的值組成一個用","分隔的 SQL 字符串
 sSelectName:欲處理之復選框名稱
 sseparator:分隔符
 sMark:分隔符
 sReturn: 返回組成之字串
 */
function emisOptionSql(sSelectName, sSeparator, sMark) {
  var oField_ = Emis$(sSelectName);
  if (oField_ == null) {
    return;
  }
  var sSeparator_ = sSeparator == null ? "," : sSeparator;
  var sMark_ = sMark == null ? "'" : sMark;
  var sReturn = "";
  for (var i = 0; i < oField_.length; i++) {
    if (oField_[i].selected) {
      sReturn += sSeparator_ + sMark_ + oField_[i].value + sMark_;
    }
  }
  sReturn = sReturn.substring(sSeparator_.length, sReturn.length);
  if (sReturn.length == (sMark_.length * 2)) {
    sReturn = "";
  }
  return sReturn;
}

//@todo 設置預設值 add by Ben
function emisSetDefaultValue(oDefBean) {
  try {
    var _oDefBean = oDefBean ;
    if (emisEmpty(oDefBean))
      _oDefBean = eval(emisGetTitle().toLowerCase() + "Bean");

    for (key in _oDefBean) {
      emisSetValue(key, _oDefBean[key]);
    }
  } catch(ex) {
  }
}

//空白是才預設值
function emisSetEmptyDefaultValue(oDefBean) {
  try {
    var _oDefBean = oDefBean ;
    if (emisEmpty(oDefBean))
      _oDefBean = eval(emisGetTitle().toLowerCase() + "Bean");

    for (key in _oDefBean) {
      if (emisGetValue(key) == "") {
        emisSetValue(key, _oDefBean[key]);
      }
    }
  } catch(ex) {
  }
}

// 功能說明: 設定游標註點
//  傳入參數:
//                  aFocusField : 欲設定駐點之欄位集合
// 使用範例:
//  setFocus(["S_NO1","S_NO2","TR_DATE"]);
// 傳回值: 無

// 函數說明
// parameter1: 參數說明
// parameter2: 參數說明
// 傳回值:
function setFocus(aFocusField) {
  try {
    if (aFocusField && aFocusField.constructor != Array) {
      aFocusField = [aFocusField];
    }
    // Joe Mark: Blur時會導致Alert死循環
    //window.focus();
    //document.body.focus();
    var field;
    for (var i = 0; i < aFocusField.length; i++) {
      field = typeof aFocusField[i] == "string" ? formobj(aFocusField[i]) : aFocusField[i];
      if (field) {
        try{
          // 切换页签
          jQuery(field).data('tabObject').click();
        }catch(ex) {}

        //field.focus();
        // 修正在FF下focus无效，且在IE下如果是按Enter失去焦点也无法返回的问题
        window.setTimeout(function(){
          try {
            field.focus();
          } catch(ee){}
        },50);
        //Joe Mark: IE8下有時會使光標丟失
        /*try{
         // 选中内容
         field.select();
         }catch(ex) {}*/
        break;
      }
    }
  }
  catch(e) {}
}

// 資料_欄＿必要的處理：加*與加ID
// 初始化頁面
function emisAddRequiredMark() {
  var _lResult = true;
  jQuery("th").each(function() {
    var node = this;
    // 修正emisAddRequireMark非IE下firstChild的Bug
    if (this.children.length > 0 && this.children[0].nodeType == 1)
      node = this.children[0];
    if (node.firstChild && node.firstChild.nodeType != 3) return;
    // 自動充填過短的欄位文字
    var $node = jQuery(node);
    var _sText = $node.text().replace(/\s|\u00a0/g, '');
    var reg = /^[u4E00-u9FA5]+$/;
    if (!reg.test(_sText)) { // 非中文不处理
      if (_sText.length == 2) {
        _sText = _sText.substring(0, 1) + "　　" + _sText.substring(1, 2);
        $node.text(_sText);
      } else if (_sText.length == 3) {
        _sText = _sText.substring(0, 1) + " " + _sText.substring(1, 2)
            + " " + _sText.substring(2, 3);
        $node.text(_sText);
      }
    }
  });

  /*jQuery(".required").each(function() {
   // 自動充填過短的欄位文字
   jQuery(this).css("color","blue");
   var _sText = jQuery(this).text();
   if (_sText.length == 2) {
   _sText = _sText.substring(0, 1) + "　　" + _sText.substring(1, 2);
   jQuery(this).text(_sText);
   } else if (_sText.length == 3) {
   _sText = _sText.substring(0, 1) + " " + _sText.substring(1, 2)
   + " " + _sText.substring(2, 3);
   jQuery(this).text(_sText);
   }
   jQuery(this).append("<font color='red'>*</font>");
   });*/

  //emisInputFocus();
}

/**
 * 2004/12/31: 將本頁的值寫回上頁或上頁寫到本頁.<br>
 * onload: emisSetElementValue(getParent(), window, "QRY");<br>
 * btnOK: emisSetElementValue(window, getParent(), "QRY");
 * ZERO 2011/07/12 添加不同前缀回传值功能
 * @param {Window} oToWin
 * @param {Window} oFromWin
 * @param {String} sLeadStr
 * @param {boolean} iContainEmpty
 * @return {Boolean}
 */
function emisSetElementValue(oFromWin, oToWin, sFromPrefix, sToPrefix, bContainEmpty) {
  if (emisEmpty(oToWin) || emisEmpty(oFromWin)) return false;
  var _oToElements = oToWin.document.forms[0].elements;
  var _iFromPrefixLen = (sFromPrefix = sFromPrefix || "").length;
  var _iToPrefixLen = (sToPrefix = sToPrefix || "").length;
  var _sType, _sName, _sPrefix, _oObj, _sValue;
  for (var i = 0; i < _oToElements.length; i++) {
    try {
      _sType = _oToElements[i].type;
      _sName = _oToElements[i].name;

      if (emisEmpty(_sName)) continue;
      // Age add  增加查詢筆試的處理邏輯
      if (!emisEmpty(sToPrefix)) { //zero add 添加不同前缀回传功能
        _sPrefix = _sName.substring(0, _iToPrefixLen);
      } else {
        _sPrefix = _sName.substring(0, _iFromPrefixLen);
      }

      if (_sPrefix != sFromPrefix && _sPrefix != sToPrefix && _sName != "L_QRYNUM") continue;

      if (!emisEmpty(sToPrefix)) { //zero add 添加不同前缀回传功能
        _oObj = formobj(sFromPrefix + _sName.substring(_iToPrefixLen), 0, oFromWin);
      } else {
        _oObj = formobj(_sName, 0, oFromWin);
      }

      if (!_oObj) continue;
      _sValue = _oObj ? _oObj.value : "";

      //  用此變數判斷（bContainEmpty） 是否要把 "" 值賦給對應物件 默認為真。
      if (emisEmpty(bContainEmpty)) {
        _oToElements[i].value = _sValue;
      } else {
        if (!emisEmpty(_sValue)) { // 有可能要設成 null
          _oToElements[i].value = _sValue;
        }
      }
      // Age add  增加查詢筆試的處理邏輯
      if (_sName === "L_QRYNUM") {
        _oToElements[i].value = _sValue || iQryNum_ || "100";
      }
    } catch (e) {
      window.status = ('setParentValue: ' + e.message + " " + _sName);
    }
  }
  return true;
}

//將自動組好btnQry 的title 屬性
function emisSetQueryTitle() {
  //var _sQueryString = "\n*****************\n目前查詢條件:";
  var _sQueryString = jQuery.getMessage("ajax_query_button_title");
  var done = {}, keys = [], vals = [], idx, key, val;
  jQuery("input[type='hidden'][title][name*='QRY']").each(function() {

    if (this.name.substring(0, 3).toUpperCase() == "QRY" ||
        this.name == "L_QRYNUM") {
      key = (this.title || this.name);
      val = this.name.indexOf("FLS_NO") >= 0 ? checkFLS_NO(jQuery.trim(this.value)) : jQuery.trim(this.value);
      idx = done[key];
      if (typeof idx != 'undefined') {
        if (jQuery.trim(this.value).length > 0)
          vals[+idx].push(val);
      } else {
        keys.push(key);
        vals.push(val.length > 0 ? [val] : []);
        done[key] = keys.length - 1;
      }
      //_sQueryString += "\n" + (this.title || this.name) + "=" + this.value;
    }
  });
  for (var i = 0; i < keys.length; i++) {
    _sQueryString += "\n" + keys[i] + "：" + vals[i].join("～");
  }
  _sQueryString += "\n*****************";
  jQuery("#btnQry").attr("title", jQuery("#btnQry").attr("defTitle") + _sQueryString);
}

/**
 * Ajax版用于提交时使用的方法,常用于表头提交到表身
 * @param oForm
 * @param sUrl
 * @param sTarget
 */
function emisFormSubmit(oForm, sUrl, sTarget) {
  var action = oForm.action;
  var target = oForm.target;
  oForm.action = sUrl;
  oForm.target = sTarget || "";
  oForm.submit();
  oForm.action = action;
  oForm.target = target;
  return true;
}

/**
 * 取得File對象的Value，解決IE6+ 取不到值問題
 * @param oFile  <input type='FILE' ...>
 */
function emisFilePath(id) {
  var oFile = (typeof id == "string") ? document.getElementById(id) : id;
  // 2013/08/22 Joe 由于安全问题目前没有一个通用的算法实现获取File的真正路径，
  // 所以建议不要全用xmlUtil.exists("XXX"); 只检查扩展名即可。
  return !!oFile ? oFile.value : "";  //放开注释，只判断扩展名即可 by Andy
  var fileValue = "";
  if (oFile) {
    var ua = window.navigator.userAgent.toLowerCase();
    if (ua.indexOf("msie") >= 1) {
      oFile.select();
      oFile.blur();//dana upd 修正IE9拒绝访问错误.
      fileValue = document.selection.createRange().text;
      document.selection.empty();
    } else if (ua.indexOf("firefox") >= 1 && oFile.files) {
      if(oFile.files.item(0).getAsDataURL) {  // 新版本的firefox不支持getAsDataURL方法
        fileValue = oFile.files.item(0).getAsDataURL();
      } else {
        fileValue = window.URL.createObjectURL(oFile.files.item(0));
      }
    }
    // IE11按上面取value为空，这里再按.value取一次;
    if(emisEmpty(fileValue)) fileValue = oFile.value;
    return fileValue;
  }
  return "";
}

/**
 * 清空File对象的Value
 * @param aFiled
 */
function emisFileClear(id) {
  var up = (typeof id == "string") ? document.getElementById(id) : id;
  if (typeof up != "object") return null;
  var tt = document.createElement("span");
  tt.id = "__tt__";
  up.parentNode.insertBefore(tt, up);
  var tf = document.createElement("form");
  tf.appendChild(up);
  document.getElementsByTagName("body")[0].appendChild(tf);
  tf.reset();
  tt.parentNode.insertBefore(up, tt);
  tt.parentNode.removeChild(tt);
  tt = null;
  tf.parentNode.removeChild(tf);
  tf = null;
}

var emisWinManager = (function() {
  var winHandler = [];
  //给Array对象原型上添加contains方法.(如果没有的话)
  if (!Array.prototype.contains) {
    Array.prototype.contains = function(obj) {
      var i = this.length;
      while (i--) {
        if (this[i] === obj) {
          return true;
        }
      }
      return false;
    }
  }
  function Manager() {
    this.push = function(win) {
      if (!winHandler.contains(win)) {
        winHandler[winHandler.length] = win;
      }
    }
    this.close = function(win) {
      if (win && winHandler.contains(win)) {
        for (var i = 0; i < winHandler.length; i++) {
          if (winHandler[i] == win)
            winHandler.splice(i, 1); //删除元素
        }
        try {
          win.close();
        } catch(e) {
        }
      } else {
        for (var i = 0; i < winHandler.length; i++) {
          try {
            if (winHandler[i])
              winHandler[i].close();
          } catch(e) {
          }
        }
        winHandler.splice(0, winHandler.length);
      }
    }
  }
  if (jQuery) {
    jQuery(window).bind("unload", function() {
      emisWinManager.close();
    });
  }
  return new Manager();
})();

// 叫用quiee報表.
// sURL: 传递给报表的参数
//sReportName:调用的.raq文件名称
// oForm:表单
function emisQRpt(sURL, sReportName, oForm, sFun) {
  if(window.prePrintSaveUrl && prePrintSaveUrl.isSave) return prePrintSaveUrl(sURL,"quiee");

  if(window.chgPrePrintName) {
    chgPrePrintName();
  }
  var _sURL = "";
  _sURL = sQuieeServer+"/quiee/reportJsp/showReport.jsp?" + sURL +
      '&raq=./' + sReportName + '&FUN=' + sFun;

  var _oWin = emisOpenQuiee(encodeParam(_sURL),sReportName,oForm);
  _oWin.focus();
}

// 打开quiee報表.
// sURL: 传递给报表的参数
// sName: 打开页面名称
// oForm:表单
function emisOpenQuiee(sURL, sName, oForm) {

  var iWidth = screen.availWidth;
  var iHeight = screen.availHeight - 55;
  var _sFlag = "width=" + iWidth + ",height=" + iHeight + ",status=yes,scrollbars=1,resizable=1,hotkeys=0" +
               ",top=0,left=0";
  // 無傳入 Form Object 則以第一個 Form
  var _oForm = oForm ; //add by Ben [3339] 20050530
  if (emisEmpty(oForm))
    _oForm = document.forms[0]; // modify by Ben [3339] 20050530
  // sName 以 Random 產生, 以避免會不同作業會開同一視窗
  sName = emisEmpty(sName) ? (Math.floor(Math.random() * 10000000000) - 0) + "" : sName;
  var _oWin = window.open("about:blank", sName, _sFlag);
  _oWin.name = sName;
  var _sAction = _oForm.action;
  var _sTarget = _oForm.target;

  _oForm.action = sURL;
  _oForm.target = sName;
  _oForm.submit();

  // 還原 action, target
  _oForm.action = _sAction;
  _oForm.target = _sTarget;
  return _oWin;
}

//取得xml中组好的sql
//sURL:必須有act=name以取得XML裡的getsql action
function emisGetRptSql(sURL) {

  var _sURL = sURL + "&AJAX=YES" + (sURL.indexOf("&TITLE=") > 0 ? "" : "&TITLE=" + emisGetTitle());
  try {
    // 统一加上用户数据权限访问的参数
    if (formobj("AC_USER_ID") && sURL.indexOf("&AC_USER_ID=") < 0) {
      _sURL += "&AC_USER_ID=" + formobj("AC_USER_ID").value;
    }
  }catch(e){ }

  var _sRetObj = jQuery.ajax({url: "../emis_sql.jsp",data: encodeParam(_sURL),async: false}).responseText;

  return  _sRetObj;

}

//檢核針對單據回溯的關帳月份
//sS_NO_:傳過來的條件
//sDate_:傳過來的日期
//sMsg_:不符合條件需要提示的信息
function chkIsMclock(sS_NO_, sDate_, sMsg_) {
  var _IS_MCLOCKED = AjaxGetData("ACT=getLAST_CL_YM&TITLE=select&S_NO=" + sS_NO_ + "&APJ_DATE=" + sDate_);
  if (_IS_MCLOCKED[0] == "true") {
    if (emisEmpty(sMsg_))
      ymPromptAlert({msg: jQuery.getMessage('chkIsMclock_msg1')});
    else
      ymPromptAlert({msg: sMsg_});

    return false;
  }
  return true;
}

/**
 * @param: sTable:"INS_H", sFields:"S_NO,IN_NO,UPD_DATE,UPD_TIME",sWhere:"S_NO between 'xxxx' and 'xxxx' and IN_DATE between 'xxxxx' and 'xxxxx'"
 * @returns : [["0001","16IN000038",""],["0002","16IN000038"]]
 */
function getBatchUpdates(sTable, sFields, sWhere){
  var _sURL = "ACT=ajax_GetData&TITLE=select"
      + "&FIELD=" +  sFields + "&TABLE=" + sTable
      + "&CONDS=" + sWhere;
  var rows = AjaxGetData(_sURL,"Y"); //返回多条记录
  return rows;
};

/*
 * Track+[18493] dana.gao 2011/09/15 增加使用encodeURIComponent處理參數的方法,
 * 替換公共方法裏面的encodeURI,因為使用encodeURI時一些特殊字符(;/?:@&=+$,#)並不會處理
 *
 * */
function encodeParam(sUrl) {
  if (!sUrl) return sUrl;
  var key, val, ret = [];
  var index = sUrl.indexOf("?");
  var urlhead = "";
  var param = "";
  if (index > -1) {
    urlhead = sUrl.substring(0, index + 1)
    param = sUrl.substring(index + 1, sUrl.length);
  } else {
    param = sUrl;
  }

  var params = param.split("&");
  for (var i = 0; i < params.length; i++) {
    var pos = params[i].indexOf('=');
    if (pos == -1) {
      //      ret[ret.length] = encodeURIComponent(params[i]);
      continue;
    } else {
      key = params[i].substring(0, pos);
      val = params[i].substring(pos + 1);
      ret[ret.length] = encodeURIComponent(key) + "=" + encodeURIComponent(val);
    }
  }

  return ret.length > 0 ? urlhead + ret.join('&') : sUrl;
}

/**
 * 日期下拉选择框change时调用函数（原来是在预设报表的相关程序中处理，移到该公用js中处理）
 * @param o
 */
function selDateScopeChange(o){
  if (!o) return;
  var sStart = o.getAttribute("dateStart"),
      sEnd = o.getAttribute("dateEnd");
  if (!sStart || !sEnd) return;
  var start = formobj(sStart);
  var end = formobj(sEnd);
  if (o.value != '') {
    var d = parseDate(o.value);
    start.value = d[0];
    end.value = d[1];
  } else {
    start.value = end.value = '';
  }
  try {
    if (sStart == sEnd) {
      jQuery(start).change();
    }
    setFocus(start)
  } catch (e) {
  }
}

//日期区间范围
function parseDate(d) {
  //jsLogger.debug(d);
  var date = ['',''];
  if(d == "today") date[0] = date[1] = emisDate("","/");
  else if(d == "yesterday") date[0] = date[1] = emisDateDiff("",-1);
  else if(d == "threeDay"){
    date[1] = emisDate("","/");
    date[0] = emisDateDiff(date[1],-3);
  }else if(d == "week"){
    var w = emisWeek("",9);
    date[0] = emisDateDiff("",-w);
    date[1] = emisDateDiff("",6-w);
  }else if(d == "lastWeek"){
    var w = emisWeek("",9);
    date[0] = emisDateDiff("",-w-7);
    date[1] = emisDateDiff("",-w-1);
  }else if(d == "month"){
    date[0] = emisDate("","/").substring(0, 7) + "/01";
    date[1] = emisDateDiff(emisMonthCal(date[0],1)+"01",-1);
  }else if(d == "lastMonth"){
    date[0] = emisMonthCal("",-1,"/") + "/01";
    date[1] = emisDateDiff(emisMonthCal(date[0],1)+"01",-1);
  }else if(d == "threeMonth"){
    date[0] = emisMonthCal("",-3,"/") + "/01";
    date[1] = emisDateDiff(emisDate("","/").substring(0, 7)+"01",-1);
  }else if(d == "ym_month"){
    date[0] = date[1] = emisDate("","/").substring(0, 7);
  }else if(d == "ym_lastMonth"){
    date[0] = date[1] = emisMonthCal("",-1,"/");
  }else if(d == "ym_threeMonth"){
    date[0] = emisMonthCal("",-3,"/");
    date[1] = emisMonthCal("",-1,"/");
  }else if(d == "ym_thisYear"){
    var y = emisYear();
    date[0] = y + '/01';
    date[1] = y + '/12';
  }else if(d == "ym_lastYear"){
    var y = (emisYear() - 1) + "";
    date[0] = y + '/01';
    date[1] = y + '/12';
  }else if(d == "y_thisYear"){
    date[0] = date[1] = emisYear();
  }else if(d == "y_lastYear"){
    date[0] = date[1] = emisYear() - 1;
  }
  return date;
}
