//$Id: ajax.js 2459 2016-01-15 04:44:20Z andy.he $
// by robert
// =================================================================================================================
// ajax part
// =================================================================================================================


// AJAX debug flag
var bDebugAjax = false;
var bDebugAction = "add"; // your debug target action
var request = httpRequest(); // ajax http xml objet

var requestQueryString; // request query string
var LastAjaxUpdateRows;

// 為了防止有時使用 document.all.xxx 會跟 form 的混淆, 這邊使用 formobj( id ) 的方式
/**
 * 取得Form表單中指定Name或ID的物件 <br>
 * 不傳Form Index時，默認取第一個
 *
 * @param {String} name
 * @param {int} idx
 * @param {Object} oParent Window(<b>Default</b>) DialogArguments Opener
 * @return {Element}
 */
formobj = function (name, idx, oParent) {
  idx = idx || 0;
  oParent = oParent || window;
  try {
    var obj = oParent.document.forms[idx].elements[name];
  } catch (e) {
  }
  return obj || docobj(name, oParent) || docobjs(name, oParent)[0];
}

/**
 * 取得當前頁面中指定ID值的物件
 *
 * @param {String} sId
 * @param {Object} oParent Window(<b>Default</b>) DialogArguments Opener
 * @return {Element}
 */
docobj = function (sId, oParent) {
  oParent = oParent || window;
  return oParent.document.getElementById(sId);
}
/**
 *
 * @param {String}
    *            name
 * @param {Object}
    *            oParent Window(<b>Default</b>) DialogArguments Opener
 * @return {Elements}
 */
docobjs = function (name, oParent) {
  oParent = oParent || window;
  return oParent.document.getElementsByName(name);
}

// sRequestType: "GET" or "POST"
// sURL: 叫用的Server URL
// isAsync: 非同步操作嗎？ true or false
// fnHandleResponse: Server回應後的處理函數
function httpRequest() {
  var req;
  if (window.XMLHttpRequest) { // Mozilla-based browsers
    req = new XMLHttpRequest();
    if (req.overrideMimeType) {
      req.overrideMimeType('text/xml');
    }
  } else if (window.ActiveXObject) { // IE 5, IE 6
    try {
      req = new ActiveXObject('Msxml2.XMLHTTP');
    } catch (e) {
      try {
        req = new ActiveXObject('Microsoft.XMLHTTP');
      } catch (e) {
      }
    }
  }
  return req;
} // ~ httpRequest

function getQueryString(excepts) {
  var requestQuery = [];
  var _oForm = document.forms[0];
  var _iNumElements = _oForm.elements.length;
  for (var i = 0; i < _iNumElements; i++) {
    var name = _oForm.elements[i].name;
    if (!emisEmpty(name) && (name != excepts)) {
      //修正当非IE时选中给值为1
      if (/CHECKBOX/i.test(_oForm.elements[i].getAttribute("type"))) {
        _oForm.elements[i].value = (_oForm.elements[i].checked == true ? "1" : "0");
      }
      //requestQuery.push(name+"="+encodeURIComponent(_oForm.elements[i].value));
      //requestQuery.push(name + "=" + _oForm.elements[i].value);

      // 2013/05/21 Joe 修正参数中有&符号时无法正确储存问题，自主把参数编码，并于后续加标识避免重复处理
      requestQuery.push(encodeURIComponent(name) + "=" +encodeURIComponent(_oForm.elements[i].value));
    }
  }
  // 2013/05/21 Joe 为配合修正参数中有&符号时无法正确储存问题，在此加入标识说明已把参数做了编码处理，避免在Ajax请求时重复编码
  requestQuery.push("ENCODESELF=Y");
  return requestQuery.join(requestQuery.length > 1 ? "&" : "");
}

my_check_error = function (xmldom) {
  if (xmldom != undefined) {
    var exceptions = xmldom.getElementsByTagName("exception");
    var errcodes = xmldom.getElementsByTagName("errcode");
    if (exceptions.length != 0) {
      var Msg = "";
      Msg = "exception=" + exceptions[0].childNodes[0].nodeValue + "\r\n";
      if (errcodes.length != 0) {
        Msg += "errcode=" + errcodes[0].childNodes[0].nodeValue;
      }
      //TODO: 待從源頭修正 Joe
      if (Msg.indexOf("編號重覆") >= 0) // 顯示錯誤訊息
        alert(emisEmpty(sSaveUniqueMsg_) ? jQuery.getMessage('db_save_unique_msg') : sSaveUniqueMsg_);
      else
        alert(Msg);
      return true;
    }
  }
  return false;
}

cellvalue = function (cell) {
  if (cell.childNodes[0] == undefined)
    return '';
  return cell.childNodes[0].nodeValue;
}

setcell = function (cell, value) {
  if (cell.childNodes[0] == undefined) {
    // document.createTextNode 是 Microsoft 的, 和 ajax 的是不同,這邊要注意
    // 使用錯誤,會有 error
    var t = cell.ownerDocument.createTextNode(value);
    cell.appendChild(t);
    return;
  }
  cell.childNodes[0].nodeValue = value;
}

// this is called by ajax call back , don't use it
// 将原field参数改为 isAll 参数，为 Y 时可返回多行记录集。
function _ajax_get_data(xmldom, isAll) {
  if (my_check_error(xmldom)) {
    return -1;
  }
  // 2013/08/16 joe 取消empty="true"时直接返回-1导致判断错误
  // 修正空数据集时错误
//  var root = xmldom.getElementsByTagName("data");
//  var isEmpty = root[0].getAttribute("empty");
//  if (isEmpty == "true") return -1;

  var rows = xmldom.getElementsByTagName("_r");
  if(rows.length == 0) return -1;

  if(isAll == 'Y'){
    var rowArray = new Array(rows.length);
    for(var j = 0; j< rows.length; j++){
      var row = rows[j];
      var len = row.childNodes.length;

      if (len >= 1) {
        var darray = new Array(len);
        for (var i = 0; i < len; i++) {
          darray[i] = cellvalue(row.childNodes[i])
        }
        rowArray[j] = darray;
      }
    }
    return  rowArray;
  } else {
    var row = rows[0];

    var len = row.childNodes.length;

    if (len >= 1) {
      var darray = new Array(len);
      for (var i = 0; i < len; i++) {
        darray[i] = cellvalue(row.childNodes[i])
      }
      return darray;
    }
  }
  return -1;

}

var updaterows_;
function mycheckResult(xmldom) {
  LastAjaxUpdateRows = 0;

  if (my_check_error(xmldom)) {
    return false;
  }
  var rows = xmldom.getElementsByTagName("updatenum");
  if (rows.length > 0) {
    var row = rows[0];
    updaterows_ = row.childNodes[0].nodeValue;
    //alert('Success update ' + updaterows_ + ' rows');
    if(sSaveSuccessMsg_ === "nomsg") // 当有标识为不
      sSaveSuccessMsg_ = null; //每次处理完成后清空不提示标记
    else
      alert(emisEmpty(sSaveSuccessMsg_) ? jQuery.getMessage('process_success', updaterows_) : sSaveSuccessMsg_);
    //取消默認消息,如果有設置消息則彈出,默認不彈出
    //if(!emisEmpty(sSaveSuccessMsg_)&& !/nomsg/i.test(sSaveSuccessMsg_)) alert(sSaveSuccessMsg_);
    return true;
  }
  /*alert('mycheckResult:undefined');
  alert(request.responseText);*/
  alert('mycheckResult=>XML Parse Failed:\n'+request.responseText);
  return false;

}

// do_ajax_update 如果不使用 sURL 會自動去組 form 的
AjaxUpdate = function (sURL) {
  if (do_ajax_request(sURL)) {
//    return mycheckResult(request.responseXML);
    return mycheckResult(parseXmlDom(request));
  }
  return false;
};

// 主要是 masterTable 用的
do_ajax_request = function (sURL) {
  if (sURL == undefined) {
    var URL = getQueryString();
    //alert(URL);
    return __ajax_request(URL);
  } else {
    return __ajax_request(sURL);
  }
}

// if you want to simply get one data from result, you can use this API
// @sAction business xml 中的 action
// @ isAll : 为Y时反回多行数据集，否则只返回第一行数据
// 如果沒有傳 TITLE, 就會使用 '資料選擇作業' <select.xml>
// 回傳 array (請參考 _ajax_get_data) 或 -1 if error
//
AjaxGetData = function (sURL,isAll) {

  if (__ajax_request(sURL)) {
    //return _ajax_get_data(request.responseXML);
    return _ajax_get_data(parseXmlDom(request), isAll);
  } else {
    return -1;
  }

}

// ================================================================================================
//
// 處理 loading span
//
// ================================================================================================

var AjaxLoadingObjName = "_AjaxLoading";

function AjaxCreateLoadingDiv(parent) {
  var div = document.createElement("div");
  var img = document.createElement("img");
  img.src = sRoot + "/images/loading.gif";
  div.style.visibility = "hidden";
  div.style.display = "none";
  div.appendChild(img);
  div.id = AjaxLoadingObjName;
  parent.appendChild(div);
}

function AjaxShowLoading() {

  var loadingSpan = document.getElementById(AjaxLoadingObjName);
  if (!emisEmpty(loadingSpan)) {
    /*
     * var h = (screen.availHeight - 32) / 2;
     * var w = (screen.availWidth - 32 ) / 2;
     * loadingSpan.style.position ="absolute";
     * loadingSpan.style.left=w; loadingSpan.style.top = h;
     */

    loadingSpan.style.visibility = "visible";
    loadingSpan.style.display = "";
  }
}
function AjaxHideLoading() {

  var loadingSpan = document.getElementById(AjaxLoadingObjName);
  if (!emisEmpty(loadingSpan)) {
    loadingSpan.style.visibility = "hidden";
    loadingSpan.style.display = "none";
  }
}

// ================================================================================================
// 為了顯示 loading , 需要使用 timer
// ================================================================================================

// ================================================================================================
// 唯一的 ajax 入口, 透過這個入口的 debug , 可以 easy debug 所有的 action ,
// 不要增加其他的入口,以免系統 debug 困難
// ================================================================================================

var bAjaxRequestDone;
function AjaxHandler() {
  if (request.readyState == 4) {
    // alert("AjaxHandler done");
    bAjaxRequestDone = true;
    AjaxHideLoading();
  }
}

// 這邊試過用非同步的的方式,但是這樣每個 ajax call 都要寫一個 callback , 對我們的應用來說,是很麻煩的
// 因為 javascript engine 和 GUI 是 run 在同一個 thread, 所以用同步的方式使用 ajax , 就沒辦法做出
// loading 的效果了
function __ajax_request(sURL) {
  // request.open("POST", sRoot + "/jsp/ajax.jsp" , false);
  request.open("POST", sRoot + "/servlet/com.emis.servlet.emisAjaxServlet", false);
  request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

  if (sURL.indexOf("&TITLE=") == -1) {
    //sURL += "&TITLE=" + encodeURIComponent("資料選擇作業");
//    sURL += "&TITLE=資料選擇作業";
    sURL += "&TITLE=select";
  }
  var isDebug = bDebugAjax; // checkDebugState(sURL);
  if (isDebug) {
    sURL += "&AJAX_DEBUG=true";
  }

  // request.timeOut = 300;
  request.send(sURL.indexOf("&ENCODESELF=Y") == -1 ? encodeParam(sURL) : sURL);

  if (isDebug) {
    /*window.open("").document.body.innerHTML = '<textarea style="width:100%;height:99%">'
     + request.responseText + '</textarea>';*/
    jsLogger.info(request.responseText);
    //return false
  }

  if ((request.readyState == 4) && (request.status == 200)) {
    return true;
  } else {
    alert('There are problem with the request. http status=' + request.status);
    return false;
  }

}
/**
 * 取XmlDom物件
 * @param http
 */
function parseXmlDom(http) {
  if (bDebugAjax) {
    var parser,xmlDoc,start,end,text;
    start = http.responseText.indexOf("<xml>");
    end = http.responseText.indexOf("</xml>") + "</xml>".length;
    var text = request.responseText.substring(start, end);
    if (window.DOMParser) {
      parser = new DOMParser();
      xmlDoc = parser.parseFromString(text, "text/xml");
    } else {// Internet Explorer
      xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
      xmlDoc.async = "false";
      xmlDoc.loadXML(text);
    }
    return xmlDoc;
  } else {
    return http.responseXML;
  }
}
function checkDebugState(sUrl) {
  if (typeof sUrl != 'string')
    return false;

  var index = 0;
  if ((index = sUrl.indexOf("?")) != -1) {
    sUrl = sUrl.substr(index + 1);
  }
  var paras = sUrl.toUpperCase().split("&");
  var sAct = 'ACT=' + bDebugAction.toUpperCase();
  for (var i = 0; i < paras.length; i++) {
    if (paras[i] === sAct) {
      return bDebugAjax;
    }
  }
  return false;
}

/**
 * 对传入参数键值进行转码
 * @param sUrl
 */
function encodeParam(sUrl) {
  if (!sUrl) return sUrl;
  var key, val, ret = [];
  var index = sUrl.indexOf("?");
  var urlhead="";
  var param="";
  if (index > -1) {
    urlhead =  sUrl.substring(0,index+1)
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

  return ret.length > 0 ? urlhead+ret.join('&') : sUrl;
}
