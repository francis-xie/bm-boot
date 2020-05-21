// $Id$
var vm = new Vue({
  el: '#app',
  data: {
    oType: '',   // 外卖：1； 带走：2； 堂食：3；
    cNo: '',
    //videoSrc: 'http://x4.demo.openwbs.com/ow-content/uploads/videos/demovideo.mp4',
    headImg: 'images/temp/head.jpg',
    headHeight: 0,
    comp: {
      "icon": "images/temp/emis.jpg",
      "name": "群丰软件",
      "tel": "0756-3612999"
    },
    order: {},
    settingInfo: {},
    memberInfo: {},
    orderid: undefined,
    isProcess: false,

    scroll3: null,
    partList: {
      "departList": []
    },
    //selectList: 'dp_0108',
    menuActive: '',  // 菜单选中项
    selector: {
      show: false,
      type: 'dp_HOT',
      index: 1,
      values: []
    },
    payTime: false,
    times: 60,
    callbackTime: 10,
    timeInterval: undefined,
    timeInterval2: undefined,
    processState: '0',
    processMsg: '支付中',
    btnBackName: '取消支付'
  },
  computed: {
    //合计数量价格
    totalCount: function () {
      var qty = 0;
      var total = 0;
      var pmAmt = 0;
      var itemCnt = 0;
      var self = this;
      self.order.partList.map(function (v3, k3) {
        if (v3.selectors && v3.selectors.length > 0) {
          v3.selectors.map(function (item, k3) {
            total += (((self.oType == '1' ? v3.priceWM : v3.price) + ((self.oType == '1' || self.oType == '2') ? (v3.boxPrice * v3.boxQty) : 0)) * item.qty) + (item.qty * item.seaAmt) - 0;
            total -= (item.pmAmt || 0);
            pmAmt += (item.pmAmt || 0) - 0;
            qty += item.qty - 0;
            itemCnt++;
          });
        }
      });
      self.order.fullPmChooseList.map(function (v, k) {
        v.pmFullGift.map(function (v2, k2) {
          if (v2.gChoose > 0) {
            total += v2.gChoose * v2.gPrice;
            qty += v2.gChoose;
            itemCnt++;
          }
        });
      });

      total += (self.order.orderInfo.shippingFee - 0) || 0;
      return { 'qty': qty, 'total': total.toFixed(2), 'itemCnt': itemCnt, 'pmAmt': (pmAmt.toFixed(2)-0)};
    }
  },
  beforeCreate: function () {
  },
  created: function () {
    var self = this;
    self.order = JSON.parse(sessionStorage.obj);
    self.oType = self.order.orderInfo.oType || '';
    self.cNo = utils.GetQueryString("cNo") || '';
    console.log(self.order);

    if (self.oType != '2' && self.oType != '3') {
      self.oType = '';
    }
    if (!self.oType) {
      self.jumpToTypeChoose();
    }

    self.pageReady();
  },
  beforeMount: function () {
  },
  mounted: function () {
    var self = this;
    self.scroll3 = new BScroll('.wrapper3', {probeType: 3, click: true, disableTouch: false, mouseWheel: true});

    this.headHeight = $('#headImgDiv').css("height") || 0;
  },
  beforeDestory: function () {
  },
  destroyed: function () {
  },
  methods: {
    pageReady: function () {
      var self = this;

    },
    //点击添加商品按钮
    selectorAdd: function (index, selectorIndex) {
      this.order.partList[index].selectors[selectorIndex].qty++;
      this.order.partList[index].selectorsQty = (this.order.partList[index].selectorsQty || 0) + 1;
    },
    //点击减少商品按钮
    selectorMinus: function (index, selectorIndex) {
      this.order.partList[index].selectors[selectorIndex].qty--;
      this.order.partList[index].selectorsQty = (this.order.partList[index].selectorsQty || 0) - 1;
      if (this.order.partList[index].selectors[selectorIndex].qty <= 0) {
        this.order.partList[index].selectors.splice(selectorIndex, 1);
      }
    },

    commodityDel: function (selector, dno, commoditysIndex, selectorsIndex) {
      var self = this;
      var item = self.partList[dno][commoditysIndex];
      var selectors = item.selectors;

      selectors.splice(selectorsIndex, 1);

      var list = Object.assign([], self.partList[dno]);
      list[commoditysIndex].selectors = selectors;
      var selectorsQty = 0;
      if (list[commoditysIndex].selectors && list[commoditysIndex].selectors.length > 0) {
        list[commoditysIndex].selectors.map(function (item, k2) {
          selectorsQty += item.qty;
        });
      }
      list[commoditysIndex].selectorsQty = selectorsQty;
      self.partList[dno] = list;
      self.partList = Object.assign({}, self.partList);
      var i = 0;
      for (var dno in self.partList) {
        self.partList[dno].forEach(function (commoditys, index) {
          if (commoditys.selectors && commoditys.selectors.length > 0) i++;
        })
      }
      if (i == 0) self.jumpToOrder();
    },
    renderSelector: function (type, selector) {
      var text = '';
      if (type == '1') {
        if (!!selector.smeItemNo && selector.smeItemNo != "") {
          var itemno = '/' + selector.smeItemNo;
          selector.smenu.map(function (sea, k2) {
            sea.smenuItem.map(function (item, k3) {
              if (itemno.indexOf("/" + item.ino + "/") >= 0) {
                text += item.iname + (item.iprice > 0 ? ' ' + item.iprice + '元;' : ';');
              }
            });
          });
        }
      }

      //else if (type == '0') {
      if (!!selector.seaItemNo && selector.seaItemNo != "") {
        var itemno = '/' + selector.seaItemNo;
        selector.seasoning.map(function (sea, k2) {
          sea.item.map(function (item, k3) {
            if (itemno.indexOf("/" + item.ino + "/") >= 0) {
              text += item.iname + (item.iprice > 0 ? ' ' + item.iprice + '元;' : ';');
            }
          });
        });
      }
      //}
      return text;
    },
    renderPrice: utils.renderPrice,
    //清空购物车
    emptyShoppingCart: function () {
      var self = this;
      sessionStorage.obj = null;
      self.jumpToOrder();
    },
    saveData: function () {
      sessionStorage.obj = JSON.stringify(this.order);
    },
    showPayTime: function () {
      var self = this;
      self.payTime = true;
      self.timeInterval = setInterval(function () {
        self.times -= 1;
        //console.log(self.times);
        if (self.times <= 0 ) {
          self.closePayTime();
        }
      }, 1000);
    },
    //增加倒计时自动跳转
    showCallbackTime: function () {
      var self = this;
      self.timeInterval2 = setInterval(function () {
        self.callbackTime -= 1;
        if (self.callbackTime <= 0 ) {
          self.jumpToTypeChoose();
        }
      }, 1000);
    },

    closePayTime: function () {
      var self = this;
      self.payTime = false;
      self.closeTimeInterval();
      if (self.processState == "2") {
        self.jumpToTypeChoose();
      }
      self.processState = '0';
    },
    closeTimeInterval: function () {
      var self = this;
      if (self.timeInterval) {
        clearInterval(self.timeInterval);
        self.times = 60;
      }
    },
    doPay: function(payCode) {
      var self = this;

      if(!this.payTime) return;
      if (self.processState != '0') return;
      console.log("doPay:" + payCode);

      self.processState = '1';
      self.processMsg = '支付中,请稍等...';

      self.order.orderInfo.deskId = self.deskId;
      self.order.orderInfo.payCode = payCode;
      self.order.orderInfo.cNo = self.cNo;
      bmAPI.doAction('orderAdd',{
        order: JSON.stringify(self.order)
      }, function (result) {
        console.log(result);
        if (!!result && result['code'] == "0") {
          var slkey = result['slkey'];
          var prepay_id = result['prepay_id'];

/*          WeixinJSBridge.invoke(
            'getBrandWCPayRequest',
            {
              "appId": result['appId'],     //公众号名称，由商户传入
              "timeStamp": result['timeStamp'],         //时间戳，自1970年以来的秒数
              "nonceStr": result['nonceStr'], //随机串
              "package": "prepay_id=" + result['prepay_id'],
              "signType": "MD5",         //微信签名方式:
              "paySign": result['paySign'] //微信签名
            },
            function (res) {
              if (res.err_msg == "get_brand_wcpay_request:ok") {
                self.isProcess = true;
                orderAPI.orderPayFin({
                  wx_cuid: self.wx_cuid,
                  wx_id: document.getElementById("wx_id").value,
                  sno: self.sno,
                  "slkey": slkey,
                  "prepay_id": prepay_id
                }, function (result) {
                  if ((self.oType == "2" || self.oType == "3") && ((self.settingInfo.eiGetFoodType || "") != "2")) {
                    sessionStorage.obj = null;
                    document.location.href = "orderFinal.jsp?lng=" + self.myLng + '&lat=' + self.myLat
                    + '&wx_cuid=' + self.wx_cuid + '&code=' + self.code + '&oType=' + self.oType
                    + '&sno=' + self.sno + '&deskid=' + (self.deskId || '') + '&ordid=' + slkey;
                    return;
                  } else {
                    self.isProcess = true;
                    weui.topTips('付款成功', {
                      duration: 1000,
                      className: 'my-topTips',
                      callback: function () {
                        sessionStorage.obj = null;
                        document.location.href = "orderMyList.jsp?lng=" + self.myLng + '&lat=' + self.myLat
                        + '&wx_cuid=' + self.wx_cuid + '&code=' + self.code + '&oType=' + self.oType
                        + '&sno=' + self.sno + '&deskid=' + (self.deskId || '');
                        self.isProcess = false;
                      }
                    });
                  }
                }, function (result) {
                  // console.log(result);
                  self.isProcess = true;
                  weui.topTips(result.msg, {
                    duration: 3000,
                    className: 'my-topTips',
                    callback: function () {
                      self.isProcess = false;
                    }
                  });
                });
              } else {
                self.isProcess = true;
                weui.topTips('付款不成功，请到我的订单中进行付款。超过未付款，该订单将自动作废!', {
                  duration: 3000,
                  className: 'my-topTips',
                  callback: function () {
                    sessionStorage.obj = null;
                    self.isProcess = false;
                    document.location.href = "orderMyList.jsp?lng=" + self.myLng + '&lat=' + self.myLat
                    + '&wx_cuid=' + self.wx_cuid + '&code=' + self.code + '&oType=' + self.oType
                    + '&sno=' + self.sno + '&deskid=' + (self.deskId || '');
                  }
                });
              }
              //WeixinJSBridge.log(res.err_msg);
            }
          );*/
        } else if (!!result && result['code'] == "00") {
          self.processState = '2';
          self.showCallbackTime();
          self.processMsg = '支付成功，您的取餐码：' + (result['deskNo'] || '');
          self.btnBackName = '重新点餐';

          /*if ((self.oType == "2" || self.oType == "3") && ((self.settingInfo.eiGetFoodType || "") != "2")) {
            // TODO do this
            //var slkey = result['slkey'];
            sessionStorage.obj = null;
            //document.location.href = "orderFinal.jsp?lng=" + self.myLng + '&lat=' + self.myLat
            //+ '&wx_cuid=' + self.wx_cuid + '&code=' + self.code + '&oType=' + self.oType
            //+ '&sno=' + self.sno + '&deskid=' + (self.deskId || '') + '&ordid=' + slkey;
            return;
          } else {
            self.isProcess = true;
            weui.topTips('下单成功', {
              duration: 1000,
              className: 'my-topTips',
              callback: function () {
                //sessionStorage.obj = null;
                //self.isProcess = false;
                //document.location.href = "orderMyList.jsp?lng=" + self.myLng + '&lat=' + self.myLat
                //+ '&wx_cuid=' + self.wx_cuid + '&code=' + self.code + '&oType=' + self.oType
                //+ '&sno=' + self.sno + '&deskid=' + (self.deskId || '');
              }
            });
          }*/
        } else {
          self.isProcess = true;
          self.processState = '0';
          self.processMsg = '支付异常:' + (result['msg'] || '');

          weui.topTips(result.msg, {
            duration: 5000,
            className: 'my-topTips',
            callback: function () {
              self.isProcess = false;
            }
          });
        }
      }, function (result) {
        //loading.hide();
        if (result['errPart']) {
          var errPartName = "";
          result['errPart'].map(function (item, k2) {
            errPartName += "[" + item.name + "],";
          });
          self.isProcess = true;
          weui.topTips(errPartName + "已停售,请重新选择!", {
            duration: 3000,
            className: 'my-topTips',
            callback: function () {
              self.isProcess = false;
            }
          });
        } else {
          self.isProcess = true;
          weui.topTips(result.msg, {
            duration: 2000,
            className: 'my-topTips',
            callback: function () {
              self.isProcess = false;
            }
          });
        }
      });
    },

    jumpToStart: function () {
      sessionStorage.obj = null;
      window.location.href = "bmStart.html?ver=1.0&ver2=" + Math.random();
    },
    jumpToTypeChoose: function () {
      sessionStorage.obj = null;
      window.location.href = "bmTypeChoose.html?ver=1.0&ver2=" + Math.random();
    },
    jumpToOrder: function () {
      this.saveData();
      window.location.href = "bmOrder.html?ver=1.0&oType=" + this.oType + "&ver2=" + Math.random();
    }
  }
});


// 禁止鼠标右键
document.oncontextmenu = function () {
  return false;
};
// 禁用网页上选取的内容
document.onselectstart = function(){
  return false;
};
// 禁用复制
document.oncopy = function(){
  return false;
};

var keycode = "";
var lastTime = null, nextTime;
var lastCode = null, nextCode;
document.onkeydown = function (e) {
  if (window.event) {
    // IE
    nextCode = e.keyCode
  } else if (e.which) {
    // Netscape/Firefox/Opera
    nextCode = e.which
  }

  ////+键，增加新数据行
  //if (nextCode == 107 || nextCode == 187) {
  //  addNewGoodLine();
  //}
  ////-键，删除最后一条数据行
  //else if (nextCode == 109 || nextCode == 189) {
  //  //$(".new_products:last").remove();
  //}
  ////字母上方 数字键0-9 对应键码值 48-57
  ////数字键盘 数字键0-9 对应键码值 96-105
  //else
  if ((nextCode >= 48 && nextCode <= 57) || (nextCode >= 96 && nextCode <= 105)) {
    //数字键盘的键码值对应的字符有问题，所以手动调整键码值
    var codes = {
      '48': 48, '49': 49, '50': 50, '51': 51, '52': 52, '53': 53, '54': 54, '55': 55, '56': 56, '57': 57,
      '96': 48, '97': 49, '98': 50, '99': 51, '100': 52, '101': 53, '102': 54, '103': 55, '104': 56, '105': 57
    };
    nextCode = codes[nextCode];
    nextTime = new Date().getTime();

    //console.log(nextCode + '; ' + keycode  + '; ' + lastCode + '; ' + lastTime  + '; ' + nextTime );

    if (lastCode != null && lastTime != null && nextTime - lastTime <= 50) {
      keycode += String.fromCharCode(nextCode);
    } else {
      keycode = String.fromCharCode(nextCode);
    }
/*    if (lastCode == null && lastTime == null) {
      keycode = String.fromCharCode(nextCode);
    } else if (lastCode != null && lastTime != null && nextTime - lastTime <= 30) {
      keycode += String.fromCharCode(nextCode);
    } else {
      keycode = "";
      lastCode = null;
      lastTime = null;
    }*/
    lastCode = nextCode;
    lastTime = nextTime;
  }
  //13 为按键Enter
  else if (nextCode == 13 && keycode != "") {
    //var code = $(".new_products:last .code").val();
    //if (code != "") {
    //  //最后一行已录入数据，重新生成新行
    //  addNewGoodLine();
    //}
    addNewGoodLine();
    //$(".new_products:last .code").val(keycode).blur();
    //keycode = "";
    //lastCode = null;
    //lastTime = null;
  }
};

function addNewGoodLine() {
  vm.closeTimeInterval();
  console.log("addNewGoodLine:" + keycode);
  vm.doPay(keycode);
  //alert(keycode);
  keycode = "";
}

