// $Id$
window.utils = {};
utils.urlParam = function (name) {
  var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
  if (results) {
    return results[1];
  } else {
    return 0;
  }
};

utils.renderPrice = function (price) {
  if (+price) {
    return (+price).toFixed(2);
  } else {
    return '';
  }
};

/**
 * 获取Get的参数
 * @param name
 * @returns {*}
 * @constructor
 */
utils.GetQueryString = function (name) {
  var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
  var r = window.location.search.substr(1).match(reg);
  //if (r != null) return decodeURIComponent(r[2]);
  if (r != null) {
    return decodeURIComponent(r[2]);
  } else {
    var reg2 = new RegExp("(^|&)wx_params=([^&]*)(&|$)");
    var r2 = window.location.search.substr(1).match(reg2);
    if (r2 != null) {
      r = r2[2].match(new RegExp("(^|@@@)" + name + "=([^@@@]*)(@@@|$)"));
      if (r != null) {
        return decodeURIComponent(r[2]);
      }
    }
  }
  return null;
};

utils.webPath = "";

// 项目的根路径
utils.basePath = function () {
  if (this.webPath == "") {
    var sFullPath = window.document.location.href;
    var sUriPath = window.document.location.pathname;
    var pos = sFullPath.indexOf(sUriPath);
    var prePath = sFullPath.substring(0, pos);
    var postPath = sUriPath.substring(0, sUriPath.substr(1).indexOf('/') + 1);
    this.webPath = prePath + postPath + "/";
  }
  return this.webPath;
};

utils.onBridgeReady = function () {
  try {
    WeixinJSBridge.call("hideOptionMenu")
  } catch (e) {
  }
};

utils.wxHiddenMenus = function() {
  if (typeof WeixinJSBridge === "undefined") {
    if (document.addEventListener) {
      document.addEventListener('WeixinJSBridgeReady', this.onBridgeReady);
    }
  } else {
    this.onBridgeReady();
  }
};

/**
 * 判断是否在微信客户端内
 * @returns {boolean}
 */
utils.isInWeixinApp = function () {
  return /MicroMessenger/.test(navigator.userAgent);
};

/**
 * 关闭微信开启的窗口
 */
utils.closeWin = function () {
  if (this.isInWeixinApp()) {
    if (typeof window.WeixinJSBridge == "undefined") {
      document.addEventListener('WeixinJSBridgeReady', function () {
        WeixinJSBridge.call('closeWindow');
      });
    } else {
      WeixinJSBridge.call('closeWindow');
    }
  }else {
    window.close();
  }
};

utils.wxSetCode = function ($) {
  if (!utils.isInWeixinApp()) {
    // 这里警告框会阻塞当前页面继续加载
    alert('请使用微信内置浏览器访问本页面！');
    // 以下代码是用javascript强行关闭当前页面
    var opened = window.open('about:blank', '_self');
    opened.opener = null;
    opened.close();
    utils.closeWin();
    return;
  } else {
    var code = utils.GetQueryString("code");
    var wx_cuid = utils.GetQueryString("wx_cuid");
    if (!!code) {
      var openid = sessionStorage.getItem("openid");
      var expires_in = sessionStorage.getItem("expires_in");
      var lasttime = sessionStorage.getItem("lasttime");
      var now = new Date().getTime();
      // 毫秒要除1000换算成秒比较
      if (!!openid && (now - lasttime) / 1000 < (expires_in - 10)) {
        $("#wx_id").val(openid);
      } else {
        // 过期或不存在则请求
        var url = '../openid.jsp?code=' + code + "&wx_cuid=" + wx_cuid;
        $.getJSON(url, function (data) {
          if (!!data.openid) {
            // 记录请求信息缓存到Session中
            data["lasttime"] = (new Date().getTime());

            sessionStorage.setItem("openid", data.openid);
            sessionStorage.setItem("expires_in", data.expires_in);
            sessionStorage.setItem("lasttime", (new Date().getTime()));
            $("#wx_id").val(data.openid);
          } else {
            if (utils.GetQueryString("reTry") == "2") {
              alert("身份验证失败!");
              // 清空数据以便重新获取
              sessionStorage.removeItem("openid");
              sessionStorage.removeItem("expires_in");
              sessionStorage.removeItem("lasttime");
              setTimeout(function() {
                utils.closeWin();
              }, 200);
            } else {
              var sUrl_ = window.location.href + '';
              if (sUrl_.indexOf("wx_params=") != -1 ) {
                window.location.href = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + wx_cuid
                + "&redirect_uri=" + sUrl_
                + "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
              } else {
                var sUrlHost_ = sUrl_.split('?')[0];
                var sUrlParam_ =  window.location.search.substr(1).split('&');
                var sTmpParam_ = "";
                for (var i = 0; i < sUrlParam_.length; i++) {
                  var sParamKey_ = sUrlParam_[i].split("=")[0];
                  var sParamValue_ = sUrlParam_[i].split("=")[1];
                  if (sParamKey_ != 'code' && sParamKey_ != 'from' && sParamKey_ != 'reTry' && sParamKey_ != '') {
                    sTmpParam_ = sTmpParam_ + '@@@' + sParamKey_ + '=' + sParamValue_;
                  }
                }
                window.location.href = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + wx_cuid
                + "&redirect_uri=" + sUrlHost_ + "?wx_params=reTry=2" + sTmpParam_
                + "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
              }
            }
          }
        });
      }
    } else {
      if (utils.GetQueryString("reTry") == "2") {
        alert("获取授权失败!");
        setTimeout(function() {
          utils.closeWin();
        }, 200);
      } else {
        var sUrl_ = window.location.href + '';
        if (sUrl_.indexOf("wx_params=") != -1 ) {
          window.location.href = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + wx_cuid
          + "&redirect_uri=" + sUrl_
          + "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
        } else {
          var sUrlHost_ = sUrl_.split('?')[0];
          var sUrlParam_ =  window.location.search.substr(1).split('&');
          var sTmpParam_ = "";
          for (var i = 0; i < sUrlParam_.length; i++) {
            var sParamKey_ = sUrlParam_[i].split("=")[0];
            var sParamValue_ = sUrlParam_[i].split("=")[1];
            if (sParamKey_ != 'code' && sParamKey_ != 'from' && sParamKey_ != 'reTry' && sParamKey_ != '') {
              sTmpParam_ = sTmpParam_ + '@@@' + sParamKey_ + '=' + sParamValue_;
            }
          }
          window.location.href = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + wx_cuid
          + "&redirect_uri=" + sUrlHost_ + "?wx_params=reTry=2" + sTmpParam_
          + "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
        }
      }
    }
    if (utils.GetQueryString("reTry") == "2") {
      setTimeout(function() {
        window.location.href = (window.location.href + '').replace('reTry=2@@@', '').replace('reTry=2', '');
      }, 500);
    }
  }
};

utils.checkPageJump = function (wx_cuid) {
  var pageJumpName = "emisPageJumpV3_" + wx_cuid;
  //console.log("check:" + pageJumpName);
  //console.log("check:" + sessionStorage.getItem(pageJumpName));
  var pageJump = sessionStorage.getItem(pageJumpName);
  //console.log("check:" + pageJump);
  return !(pageJump == null || pageJump == "");
};

utils.setPageJump = function (wx_cuid, obj) {
  var pageJumpName = "emisPageJumpV3_" + wx_cuid;
  sessionStorage.setItem(pageJumpName, "'" + JSON.stringify(obj));
  //console.log("set:" + pageJumpName);
  //console.log("set:" + JSON.stringify(obj));
  //console.log("set:" + sessionStorage.getItem(pageJumpName));
};

utils.getPageJump = function (wx_cuid, name) {
  var pageJumpName = "emisPageJumpV3_" + wx_cuid;
  //console.log("get:" + pageJumpName);
  //console.log("get:" + sessionStorage.getItem(pageJumpName));
  var pageJump = sessionStorage.getItem(pageJumpName);
  //console.log("get:" + pageJump);

  if (pageJump == null || pageJump == "") {
    return name + ".jsp";
  }
  var jsonstr = JSON.parse(pageJump.substr(1, pageJump.length));
  if((jsonstr[name] || "") == "") {
    return name + ".jsp";
  } else {
    //console.log("get:" + jsonstr[name]);
    return jsonstr[name];
  }
};

/**
 * 兼容浏览器 Polyfill
 * MDN: https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Object/assign#Browser_compatibility
 */
if (typeof Object.assign != 'function') {
  // Must be writable: true, enumerable: false, configurable: true
  Object.defineProperty(Object, "assign", {
    value: function assign(target, varArgs) { // .length of function is 2
      'use strict';
      if (target == null) { // TypeError if undefined or null
        throw new TypeError('Cannot convert undefined or null to object');
      }

      var to = Object(target);

      for (var index = 1; index < arguments.length; index++) {
        var nextSource = arguments[index];

        if (nextSource != null) { // Skip over if undefined or null
          for (var nextKey in nextSource) {
            // Avoid bugs when hasOwnProperty is shadowed
            if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
              to[nextKey] = nextSource[nextKey];
            }
          }
        }
      }
      return to;
    },
    writable: true,
    configurable: true
  });
}
// 处理微信(android)点击图片会默认放大图片显示。导致影响触发图片点击事件。
$(document).on('click', 'img', function (e) {
  e.preventDefault();
});