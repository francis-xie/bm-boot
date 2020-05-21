// $Id$
var vm = new Vue({
  el: '#app',
  data: {
    oType: '',   // 外卖：1； 带走：2； 堂食：3；
    cNo: '',
    // 轮播图地址
    ADImgList: [],
    ADImgOnly: '',
    headHeight: '0px',
    comp: {
      "icon": "",
      "name": "",
      "tel": ""
    },
    scroll: null,
    scroll2: null,
    scroll3: null,
    scroll4: null,
    scroll5: null,
    partList: {
      "departList": []
    },
    partImgs: [],
    selectList: '',
    menuActive: '',  // 菜单选中项
    selector: {
      show: false,
      type: 'dp_HOT',
      index: 1,
      values: []
    },
    timesFix: 1800,
    times: 1800,
    timeInterval: undefined,
    isShowShoppingCart: false,  // 显示购物车详情
    promoteInfo: [],  // 促销信息
    usedPromote: [],  // {pmNo:pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmTotalAmt: pmTotalAmt}
    isShowFullPm: false,
    fullPmList: [],
    fullGiftPayBtn: '不用了',
    fullGiftPayBtnName: ['不用了', '加点'],
    isSm2: false,
    addCartBtn: '加入购物车',
    addCartBtnName: ['加入购物车', '不用了，谢谢', '选好了'],
    imgChangeLineCnt: 3
  },
  computed: {
    //合计数量价格
    totalCount: function () {
      var qty = 0;
      var total = 0;
      var pmAmt = 0;
      var itemCnt = 0;
      var self = this;
      self.partList.departList.forEach(function (v) {
        self.partList[v.dNo].forEach(function (v3) {
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
      });
      return {'qty': qty, 'total': (total.toFixed(2) - 0), 'itemCnt': itemCnt, 'pmAmt': (pmAmt.toFixed(2) - 0)};
    }
  },
  beforeCreate: function () {
  },
  created: function () {
    var self = this;
    self.oType = utils.GetQueryString("oType") || '';
    self.cNo = utils.GetQueryString("cNo") || '';
    if (self.oType != '2' && self.oType != '3') {
      self.oType = '';
    }
    if (!self.oType) {
      self.jumpToTypeChoose();
    }

    if (document.body.clientWidth < 925) {
      self.imgChangeLineCnt = 2;
    }
    self.pageReady();
  },
  beforeMount: function () {
  },
  mounted: function () {
    var self = this;

    setTimeout(function () {
      self.setPartListScroll();
      self.scroll = new BScroll(self.$refs.wrapper, {probeType: 3, click: true, disableTouch: false, mouseWheel: true});
      self.scrollLeft = new BScroll(self.$refs.wrapperLeft, {
        probeType: 3, click: true, disableTouch: false, mouseWheel: true
      });
      self.scroll.on('scroll', function (pos) {
        for (var departList of self.partList['departList']) {
          if (departList.dNoHeight > -self.scrollLeft.y + parseFloat(window.getComputedStyle(self.$refs.wrapperLeft).height)) {
            self.scrollLeft.scrollTo(0, -departList.dNoHeight, 1000);
            self.scrollLeft.options.probeType = 3;
            self.scrollLeft.refresh()
          } else if (departList.dNoHeight < parseFloat(window.getComputedStyle(self.$refs.wrapperLeft).height)) {
            self.scrollLeft.scrollTo(0, 0, 1000);
            self.scrollLeft.options.probeType = 3;
            self.scrollLeft.refresh()
          }
          if (-pos.y - departList.windowHeight < 0) {
            self.menuActive = self.selectList = departList.dNo;
            break;
          }
        }
      });
      //self.scroll2 = new BScroll('.wrapper2', {probeType: 3, click: true, disableTouch: false, mouseWheel: true});
      self.scroll3 = new BScroll('.wrapper3', {probeType: 1, click: true, disableTouch: false, mouseWheel: true});
      self.scroll4 = new BScroll('.wrapper4', {probeType: 3, click: true, disableTouch: false, mouseWheel: true});
      self.scroll5 = new BScroll('.wrapper5', {probeType: 1, click: true, disableTouch: false, mouseWheel: true});

      this.headHeight = $('#headImgDiv').css("height") || '0px';
    }, 1000);

    self.timeInterval = setInterval(function () {
      self.times -= 1;
      //console.log(self.times);
      if (self.times <= 0) {
        self.jumpToStart();
      }
    }, 1000);
  },
  beforeDestory: function () {
  },
  destroyed: function () {
  },
  methods: {
    pageReady: function () {
      var self = this;

      bmAPI.doAction('getOrderHeadImgList', {}, function (ret) {
        self.ADImgList = ret.result;
        if (self.ADImgList && self.ADImgList.length == 1) {
          self.ADImgOnly = self.ADImgList[0].indexImgUrl;
        } else {
          self.ADImgOnly = '';
        }

        if (self.ADImgOnly == '') {
          self.$nextTick(function () {
            var swiper = new Swiper('#swiper', {
              loop: true,
              spaceBetween: 0,
              centeredSlides: true,
              autoplay: {
                delay: 5000,
                disableOnInteraction: false
              },
              observeParents: true,
              pagination: {
                el: '.swiper-pagination',
                clickable: true
              },
              navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev'
              }
            });
          });
        }
      }, function (ret) {
      });

      self.getPartList();
      self.getPromoteInfo();
    },
    getPromoteInfo: function () {
      var self = this;
      bmAPI.doAction('getPromoteInfo', {}, function (pm) {
        self.promoteInfo = pm.prmoteList || [];
        //console.log(self.promoteInfo);
        setTimeout(function () {
          self.calPromote();
        }, 300);
      }, function (pm) {
        self.promoteInfo = [];
      });
    },
    getPartList: function () {
      var self = this;
      //console.log(self.partList)

      // 获取商品信息
      bmAPI.doAction('getPartList', {
        dataType: '2',
        oType: self.oType
      }, function (list) {
        //console.log(list.partList[0]);
        self.partImgs = list.partImgs || [];
        self.partList = list.partList[0] || {};
        if (sessionStorage.obj) {
          var obj = [];
          obj = JSON.parse(sessionStorage.obj);
          if (!!obj && !!obj.partList) {
            self.partList['departList'].map(function (v, k) {
              self.partList[v.dNo].map(function (v2, k2) {
                obj.partList.map(function (v3, k3) {
                  if (v3.pno == v2.pno && v2.saleOut != 'Y') {
                    self.partList[v.dNo][k2] = v3;
                  }
                });
              });
            });
          }
        }
        self.partList = Object.assign({}, self.partList);
        setTimeout(function () {
          self.calPromote();
        }, 300);

      });
    },

    setPartListScroll: function () {
      var self = this;
      let windowHeight = 0;
      let dNoHeight = 0;
      self.partList['departList'].map(function (v, k) {
        self.$nextTick(() => {
          dNoHeight += parseFloat(window.getComputedStyle(self.$refs[v.dNo][0]).height);  // 分类高度
          let departListHeight = 0;
          departListHeight += 20;  // 分类名称高度
          windowHeight += 20;

          let pnoHeight1 = 0;
          let pnoCnt = true;
          self.partList[v.dNo].map(function (v2, k2) {
            let item = self.$refs[v2.pno][0];  // 商品div
            let pnoHeight = parseFloat(window.getComputedStyle(item).height) + parseFloat(window.getComputedStyle(item).marginTop) + 1;  // 商品高度
            //console.log(item)
            //console.log(pnoHeight + " = " + parseFloat(window.getComputedStyle(item).height) + " + " + parseFloat(window.getComputedStyle(item).marginTop))
            if (k2 % 2 == 0) {
              pnoHeight1 = pnoHeight;
              //departListHeight += pnoHeight;
              //windowHeight += pnoHeight;
              pnoCnt = true;
            } else {
              pnoCnt = false;
              departListHeight += (pnoHeight1 > pnoHeight ? pnoHeight1 : pnoHeight);
              windowHeight += (pnoHeight1 > pnoHeight ? pnoHeight1 : pnoHeight);
            }
          });
          if (pnoCnt) {
            departListHeight += pnoHeight1;
            windowHeight += pnoHeight1;
          }

          if (!self.partList['departList'][k + 1]) {
            self.partList['departList'][k].departListTailHeight = parseFloat(window.getComputedStyle(self.$refs.wrapper).height) - departListHeight;
            self.partList['departList'][k].departListHeight = parseFloat(window.getComputedStyle(self.$refs.wrapper).height);
            self.partList['departList'][k].windowHeight = windowHeight + parseFloat(window.getComputedStyle(self.$refs.wrapper).height) - departListHeight;
          } else {
            self.partList['departList'][k].departListHeight = departListHeight;
            self.partList['departList'][k].windowHeight = windowHeight;
          }
          self.partList['departList'][k].dNoHeight = dNoHeight;
        })
      });
      self.menuActive = self.selectList = self.partList.departList[0].dNo || 'dp_HOT';
      //console.log(self.partList)
    },
    clickAllNav: function (index) {
      var self = this;
      self.menuActive = self.selectList = self.partList.departList[index].dNo;
      self.scroll.options.probeType = 2;
      self.scroll.scrollTo(0, -(self.partList.departList[index].windowHeight - self.partList.departList[index].departListHeight), 500);
      self.scroll.options.probeType = 3;
      self.scroll.refresh()
    },
    showSelector: function () {
      var self = this;
      self.selector.show = true;
      setTimeout(function () {
        //self.scroll2.refresh();
        self.scroll2 = new BScroll('.wrapper2', {probeType: 3, click: true, disableTouch: false, mouseWheel: true});
      }, 300);
    },
    showSelectorAdd: function (index, dNo) {
      var self = this;
      self.isSm2 = false;
      self.addCartBtn = self.addCartBtnName[0];
      if (dNo) self.selectList = dNo;
      var list = self.selectList;
      var item = self.partList[list][index];
      if (!item.seasoning && item.type != "1") {
        // 没有调味、不是套餐，则直接进行加减
        var selectors = item.selectors;
        if (!selectors || selectors.length == 0) {
          selectors = [{
            pno: item.pno,  // 商品编号
            name: item.name,  // 商品名称
            qty: 1,  // 数量
            seaItemNo: '',  // 明细调味编号
            seaAmt: 0,  // 调味加料金额
            pmAmt: 0,  // 促销金额
            pmQty: 0,  // 参与促销数量
            pmDetl: []  // 参与促销信息
          }];
        } else {
          selectors[0].qty += 1;
        }
        item.selectors = selectors;
        item.selectorsQty = (item.selectorsQty || 0) + 1;
        self.selector.list = list;
        self.selector.type = item.type;
        self.selector.index = index;
        self.selector.values = selectors;
        self.confirmSelector();
        return;
      }

      self.selector.list = list;
      self.selector.type = item.type;
      self.selector.index = index;
      self.selector.seasoning = item.seasoning;
      self.selector.smenu = item.smenu;
      var selectors = item.selectors;
      //console.log(selectors)
      if (!selectors || selectors.length == 0) {
        //添加默认项
        if (self.selector.type == '1') {
          // 套餐
          var smeItemNo = "";
          let _list = self.selector.smenu;
          //找出smenuNo=A的必选编号
          for (let i of _list) {
            if (i.smenuNo == 'A') {
              let objs = i.smenuItem;
              for (let i of objs) {
                smeItemNo += (i.ino + '/');
              }
            }
          }
          selectors = [{
            pno: item.pno,  // 套餐编号
            name: item.name,  // 套餐名称
            smenu: item.smenu,  // 套餐组别
            smeItemNo: smeItemNo,  // 明细调味编号
            seasoning: item.seasoning,  // 调味组别
            seaItemNo: '',  // 明细调味编号
            qty: 1,  // 商品数量
            seaAmt: 0,  // 调味金额
            showChoose: true,
            pmAmt: 0,  // 促销金额
            pmQty: 0,  // 参与促销数量
            pmDetl: []  // 参与促销信息
          }];
        } else if (self.selector.type == '0') {
          // 一般商品+含调味加料
          selectors = [{
            pno: item.pno,  // 商品编号
            name: item.name,  // 商品名称
            seasoning: item.seasoning,  // 调味组别
            seaItemNo: '',  // 明细调味编号
            qty: 1,  // 商品数量
            seaAmt: 0,  // 调味金额
            showChoose: true,
            pmAmt: 0,  // 促销金额
            pmQty: 0,  // 参与促销数量
            pmDetl: []  // 参与促销信息
          }];
        }
        self.selector.values = selectors;
      } else {
        self.selector.values = selectors;
        self.selectorAdd();
      }
      //console.log(selectors);
      //self.selector.values = selectors;

      self.showSelector();
    },
    selectorAdd: function () {
      var self = this;
      if (self.selector.values.length == 0) {
        // debugger;
        var item = self.partList[self.selectList][self.selector.index];
        var selectors = item.selectors;
        if (!selectors || selectors.length == 0) {
          //添加默认项
          if (self.selector.type == '1') {
            // 套餐
            var smeItemNo = "";
            let _list = self.selector.smenu;
            //找出smenuNo=A的必选编号
            for (let i of _list) {
              if (i.smenuNo == 'A') {
                let objs = i.smenuItem;
                for (let i of objs) {
                  smeItemNo += i.ino + '/';
                }
              }
            }
            selectors = [{
              pno: item.pno,  // 套餐编号
              name: item.name,  // 套餐名称
              smenu: item.smenu,  // 套餐组别
              smeItemNo: smeItemNo,  // 明细调味编号
              seasoning: item.seasoning,  // 调味组别
              seaItemNo: '',  // 明细调味编号
              qty: 1,  // 商品数量
              seaAmt: 0,  // 调味金额
              showChoose: true,
              pmAmt: 0,  // 促销金额
              pmQty: 0,  // 参与促销数量
              pmDetl: []  // 参与促销信息
            }];
          } else if (self.selector.type == '0')
            selectors = [{
              pno: item.pno,
              name: item.name,
              seasoning: item.seasoning,
              seaItemNo: '',
              qty: 1,
              seaAmt: 0,
              showChoose: true,
              pmAmt: 0,  // 促销金额
              pmQty: 0,  // 参与促销数量
              pmDetl: []  // 参与促销信息
            }];
        }
        self.selector.values = selectors;
      } else {
        self.selector.values.map(function (item, k2) {
          item.showChoose = false;
        });

        var list = Object.assign([], self.selector.values);
        var newItem = Object.assign({}, list[list.length - 1]);
        newItem.qty = 1;
        newItem.seaItemNo = '';
        newItem.seaAmt = 0;
        newItem.showChoose = true;
        if (self.selector.type == '1') {
          // 套餐
          var smeItemNo = "";
          let _list = self.selector.smenu;
          //找出smenuNo=A的必选编号
          for (let i of _list) {
            if (i.smenuNo == 'A') {
              let objs = i.smenuItem;
              for (let i of objs) {
                smeItemNo += i.ino + '/';
              }
            }
          }
          newItem.smeItemNo = smeItemNo;
        }
        list.push(newItem);
        self.selector.values = list;
        //console.log(self.selector.values)
      }
    },
    confirmSelector: function () {
      var self = this;
      //console.log(self.selector.values);
      //console.log(self.selector.values[0].smenu ? self.selector.values[0].smenu.length : "");
      var signChooseHas0PriceMsg = "";
      var showSm2Temp = false;
      if (self.selector.values[0].smenu && self.selector.values[0].smenu.length >= 2) {
        self.selector.values.map(function (v1, k1) {
          if (v1.showChoose) {
            var smeItemNoAll = v1.smeItemNo;
            var temp_smenuNo = "";
            var temp_smenuName = "";
            var temp_iprice = false;
            var temp_hasNo = false;
            v1.smenu.map(function (v2, k2) {
              /*if (v2.smenuNo != 'A' && v2.smenuNum == 1 && signChooseHas0PriceMsg == "") {
                //console.log(v2)
                if (temp_smenuNo != v2.smenuNo) {
                  if (temp_iprice && !temp_hasNo) {
                    signChooseHas0PriceMsg = "[" + temp_smenuName + "]需选择一项！";
                  }
                  temp_iprice = false;
                  temp_hasNo = false;
                }
                temp_smenuName = v2.smenuName;
                temp_smenuNo = v2.smenuNo;
                v2.smenuItem.map(function (v3, k3) {
                  var t = smeItemNoAll.split(v3.ino + '/').length - 1;
                  if (t >= 1) {
                    temp_hasNo = true;
                  }
                  if (v3.iprice == 0) {
                    temp_iprice = true;
                  }
                })
              }*/
              if (v2.smenuNo != 'A' && (v2.smenuMinNum || 0) > 0 && signChooseHas0PriceMsg == "") {
                var smeItemChooseCnt = 0;
                v2.smenuItem.map(function (v3, k3) {
                  var t = smeItemNoAll.split(v3.ino + '/').length - 1;
                  if (t >= 1) {
                    smeItemChooseCnt++;
                  }
                });
                if (smeItemChooseCnt < v2.smenuMinNum) {
                  signChooseHas0PriceMsg = "[" + v2.smenuName + "]需选择" + v2.smenuMinNum + "项！";
                }
              }
              if (v2.smenuNo != 'A' && (v2.smenuMinNum || 0) == 0) {
                showSm2Temp = true;
              }
            });
            if (temp_iprice && !temp_hasNo && signChooseHas0PriceMsg == "") {
              signChooseHas0PriceMsg = "[" + temp_smenuName + "]需选择一项！";
            }
          }
        });
      }
      if (signChooseHas0PriceMsg != "") {
        weui.topTips(signChooseHas0PriceMsg, {
          duration: 2000,
          className: 'my-topTips'
        });
        return;
      }

      // show isSm2
      if (!self.isSm2 && showSm2Temp) {
        self.isSm2 = true;
        self.addCartBtn = self.addCartBtnName[1];
        self.scroll2.refresh();
        self.scroll2.scrollTo(0, 0, 50);
        return;
      }
      self.isSm2 = false;
      //self.addCartBtnTitle = self.addCartBtnName[0];

      var list = Object.assign([], this.partList[this.selector.list]);
      this.selector.values.map(function (item, k2) {
        item.showChoose = false;
      });
      list[this.selector.index].selectors = this.selector.values;
      var selectorsQty = 0;
      if (list[this.selector.index].selectors && list[this.selector.index].selectors.length > 0) {
        //将勾选好的selector封装成对象
        let pno = list[this.selector.index].selectors[0].pno;
        let object = {
          pno: pno,
          obj: list[this.selector.index].selectors
        };
        list[this.selector.index].selectors.map(function (item, k2) {
          selectorsQty += item.qty;
        });
      }
      list[this.selector.index].selectorsQty = selectorsQty;
      this.partList[this.selector.list] = list;

      this.partFlyer(list[this.selector.index].imgUrl);
      self._selectorReset();
      self.calPromote();
    },
    _selectorReset: function () {
      this.selector.show = false;
      this.selector.values = [];
      this.selector.type = undefined;
      this.selector.index = undefined;
    },
    removeSelectorItem: function (index) {
      var list = Object.assign([], this.selector.values);
      list.splice(index, 1);
      this.selector.values = list;
    },
    hideSelector: function () {
      this._selectorReset();
    },
    // 商品选择点击事件
    setSeasoningChoose: function (index, seaNo, seaSingle, seaItemNo, seaPrice) {
      //console.log(index, seaNo, seaSingle, seaItemNo, seaPrice);
      //console.log(seaNo);
      //console.log(seaSingle);
      //console.log(seaItemNo);
      //console.log(seaPrice);
      var self = this;
      var list = Object.assign([], self.selector.values);
      if (list[index].seaItemNo.indexOf(seaItemNo + '/') > -1) {
        list[index].seaItemNo = list[index].seaItemNo.replace(seaItemNo + '/', '');
        list[index].seaAmt -= seaPrice;
      } else {
        if (seaSingle == "Y") {

          list[index].seasoning.map(function (v, k) {
            if (v.seano == seaNo) {
              v.item.map(function (v2, k2) {
                if (list[index].seaItemNo.indexOf(v2.ino + '/') > -1) {
                  list[index].seaItemNo = list[index].seaItemNo.replace(v2.ino + '/', '');
                  list[index].seaAmt -= v2.iprice;
                }
              });
            }
          });
        }
        list[index].seaItemNo += seaItemNo + '/';
        list[index].seaAmt += seaPrice;
      }
      self.selector.values = list;

      if (self.isShowShoppingCart) self.calPromote();
    },
    //套餐选择点击事件
    setPackageChoose: function (index, smenuNo, smeItemNo, iprice, smenu, smenuNum, smenuName, smenuMinNum) {
      var self = this;
      if (smenuNo == 'A') {
        /*weui.topTips('这个是必须选的哦', {
          duration: 2000,
          className: 'my-topTips'
        });*/
        return;
      }
      //console.log(index + "; " + smenuNo + "; " + smeItemNo + "; " + iprice + "; " + smenuNum + "; " + smenuName + "; " + smenuMinNum);
      var list = Object.assign([], self.selector.values);
      //console.log(list);
      var smeItemNoAll = list[index].smeItemNo || '';
      var time = 0;
      var signChooseOldSeaNo = "";  // 任选(只能1个)时，先记录起来原调味编号，以便于下面抓取价格做扣减
      var signChooseOldSeaPrice = 0;  // 任选(只能1个)时，先记录起来原调味加价
      var signChooseHas0Price = false;  // 任选(只能1个)时，判断是否有0元商品
      var chooseSm2 = false;
      // 遍历套餐数组smenu，获取种类对象v1
      list[index].smenu.map(function (v1, k1) {
        if (smenuNo == v1.smenuNo) {
          v1.smenuItem.map(function (v2, k2) {
            var t = smeItemNoAll.split(v2.ino + '/').length - 1;
            time += t;
            if (smenuNum == 1 && t >= 1) {
              signChooseOldSeaNo = v2.ino;
              signChooseOldSeaPrice = v2.iprice;
            }
            if (smenuNum == 1 && v2.iprice == 0) {
              signChooseHas0Price = true;
            }
          });
        }
        if (self.isSm2 && v1.smenuNo != 'A' && v1.smenuMinNum == 0 && !chooseSm2) {
          v1.smenuItem.map(function (v2, k2) {
            if (!chooseSm2) {
              if (v2.ino != smeItemNo) {
                if ((smeItemNoAll.split(v2.ino + '/').length - 1) >= 1) {
                  chooseSm2 = true;
                }
              } else {
                if ((smeItemNoAll.split(v2.ino + '/').length - 1) < 1) {
                  chooseSm2 = true;
                }
              }
            }
          });
        }
      });
      if (smenuNum == 1 && smenuMinNum == 1) {
        signChooseHas0Price = true;
      }


      //console.log(signChooseOldSeaNo + ", " + signChooseOldSeaPrice + ", time: " + time);
      // 获取当前semItemNo在selector中出现的次数
      var selfTime = smeItemNoAll.split(smeItemNo + '/').length - 1;
      if (selfTime == 0) {
        if (time < smenuNum) {
          list[index].smeItemNo += smeItemNo + '/';
          list[index].seaAmt += iprice;

        } else if (time == smenuNum && smenuNum == 1) {
          list[index].smeItemNo = list[index].smeItemNo.replace(signChooseOldSeaNo + '/', '');
          list[index].seaAmt -= signChooseOldSeaPrice;
          list[index].smeItemNo += smeItemNo + '/';
          list[index].seaAmt += iprice;
        } else if (time == smenuNum) {
          weui.topTips('[' + smenuName + ']最多只能选' + smenuNum + '项', {
            duration: 2000,
            className: 'my-topTips'
          });
        }
      } else if (!signChooseHas0Price) {
        list[index].smeItemNo = list[index].smeItemNo.replace(smeItemNo + '/', '');
        list[index].seaAmt -= iprice;
      } else {
        chooseSm2 = true;
      }
      self.selector.values = list;

      // change addCartBtn name
      if (self.isSm2) {
        self.addCartBtn = self.addCartBtnName[(chooseSm2 ? 2 : 1)];
      }
      if (self.isShowShoppingCart) self.calPromote();
    },
    minusSelectorCount: function (index) {
      var self = this;
      var selector = Object.assign({}, self.selector);
      if (selector.values[index].qty <= 1) {
        //selector.values.splice(index, 1);
        //self.selector = selector;
      } else {
        selector.values[index].qty -= 1;
        self.selector = selector;
      }
    },
    addSelectorCount: function (index) {
      var self = this;
      var selector = Object.assign({}, self.selector);
      selector.values[index].qty += 1;
      self.selector = selector;
    },
    showShoppingCart: function (isShowShoppingCart) {
      var self = this;

      this.isShowShoppingCart = isShowShoppingCart;
      if (isShowShoppingCart) {
        // 进入购物车时，模拟点击一次物品，触发commoditySea
        let partList = self.partList;
        let boon = false;
        for (let v1 of partList.departList) {
          if (boon) {
            break;
          }
          for (let v2 in partList[v1.dNo]) {
            if (partList[v1.dNo][v2].selectors && partList[v1.dNo][v2].selectors.length > 0) {
              this.commoditySea(partList[v1.dNo][v2].selectors[0], v1.dNo, v2, 0);
              boon = true;
              break;
            }
          }
        }
        setTimeout(function () {
          self.scroll3.refresh();
        }, 500)
      }
    },
    commoditySea: function (selector, dNo, commoditysIndex, selectorsIndex) {
      var self = this;
      var item = self.partList[dNo][commoditysIndex];
      var selectors = item.selectors;

      for (var i = 0; i < selectors.length; i++) {
        selectors[i].showChoose = i == selectorsIndex;
      }
      self.selector.list = dNo;
      self.selector.type = item.type;
      self.selector.index = selectorsIndex;
      self.selector.seasoning = item.seasoning;
      self.selector.smenu = item.smenu;
      self.selector.values = selectors;

      setTimeout(function () {
        self.scroll4.refresh();
      }, 500)
    },
    commodityDel: function (selector, dNo, commoditysIndex, selectorsIndex) {
      var self = this;
      var item = self.partList[dNo][commoditysIndex];
      var selectors = item.selectors;

      selectors.splice(selectorsIndex, 1);

      var list = Object.assign([], self.partList[dNo]);
      list[commoditysIndex].selectors = selectors;
      var selectorsQty = 0;
      if (list[commoditysIndex].selectors && list[commoditysIndex].selectors.length > 0) {
        list[commoditysIndex].selectors.map(function (item, k2) {
          selectorsQty += item.qty;
        });
      }
      list[commoditysIndex].selectorsQty = selectorsQty;
      self.partList[dNo] = list;
      self.partList = Object.assign({}, self.partList);
      var i = 0;
      for (var dNo in self.partList) {
        self.partList[dNo].forEach(function (commoditys, index) {
          if (commoditys.selectors && commoditys.selectors.length > 0) i++;
        })
      }
      if (i == 0) self.isShowShoppingCart = false;
      self.calPromote();
    },
    commodityReduce: function (selector, dNo, commoditysIndex, selectorsIndex) {
      var self = this;
      var item = self.partList[dNo][commoditysIndex];
      var selectors = item.selectors;
      selectors[selectorsIndex].qty -= 1;
      if (selectors[selectorsIndex].qty <= 0) {
        selectors.splice(selectorsIndex, 1);
      }
      var list = Object.assign([], self.partList[dNo]);
      list[commoditysIndex].selectors = selectors;
      var selectorsQty = 0;
      if (list[commoditysIndex].selectors && list[commoditysIndex].selectors.length > 0) {
        list[commoditysIndex].selectors.map(function (item, k2) {
          selectorsQty += item.qty;
        });
      }
      list[commoditysIndex].selectorsQty = selectorsQty;
      self.partList[dNo] = list;
      self.partList = Object.assign({}, self.partList);
      var i = 0;
      for (var dNo in self.partList) {
        self.partList[dNo].forEach(function (commoditys, index) {
          if (commoditys.selectors && commoditys.selectors.length > 0) i++;
        })
      }
      if (i == 0) self.isShowShoppingCart = false;

      self.calPromote();
    },
    commodityPlus: function (selector, dNo, commoditysIndex, selectorsIndex) {
      var self = this;
      var item = self.partList[dNo][commoditysIndex];
      var selectors = item.selectors;
      selectors[selectorsIndex].qty += 1;
      var list = Object.assign([], self.partList[dNo]);
      list[commoditysIndex].selectors = selectors;
      var selectorsQty = 0;
      if (list[commoditysIndex].selectors && list[commoditysIndex].selectors.length > 0) {
        list[commoditysIndex].selectors.map(function (item, k2) {
          selectorsQty += item.qty;
        });
      }
      list[commoditysIndex].selectorsQty = selectorsQty;
      self.partList[dNo] = list;
      self.partList = Object.assign({}, self.partList);

      self.calPromote();
    },
    //清空购物车
    emptyShoppingCart: function () {
      var self = this;
      for (var dNo in self.partList) {
        var list = Object.assign([], self.partList[dNo]);
        list.map(function (commoditys, index) {
          if (commoditys.selectors) {
            commoditys.selectors = [];
            commoditys.selectorsQty = 0;
          }
        });
        self.partList[dNo] = list;
        self.partList = Object.assign({}, self.partList);
        self.isShowShoppingCart = false;
      }
      self.usedPromote = [];
      self.fullPmList = [];
      self.isShowFullPm = false;
    },
    preventClick: function () {
    },
    partFlyer: function (imgUrl) {
      var flyer = $('<img class="u-flyer" src="' + imgUrl + '">');
      flyer.fly({
        start: {
          left: event.pageX,
          top: event.pageY
        },
        end: {
          left: cartIconOffset.left + 10,
          top: cartIconOffset.top + 10,
          width: 0,
          height: 0
        },
        onEnd: function () {
          flyer.remove();
        }
      });
    },
    saveData: function (procFullPm) {
      var self = this;
      var partList = Object.assign({}, self.partList);
      var obj = {
        orderInfo: {
          //sno: self.sno,
          oType: self.oType
          //deskId: self.deskId,
          //sname: self.shopInfo.name,
          //rname: self.shopInfo.rname,
          //shippingFee: self.shopInfo.shippingFee
        },
        partList: [],
        fullPmChooseList: [],
        usedPromote: []
      };
      partList['departList'].map(function (v, k) {
        partList[v.dNo].map(function (v3, k3) {
          if (v3.selectors && v3.selectors.length > 0) {
            obj.partList.push(v3);
          }
        });
      });
      if (procFullPm && self.fullPmList.length > 0) {
        var pmlist = Object.assign([], self.fullPmList);
        pmlist.forEach(function (v, k) {
          if (v.pmChooseQty > 0) {
            obj.fullPmChooseList.push(v);
          }
        });
      }
      if (self.usedPromote.length > 0) {
        var pmlist = Object.assign([], self.usedPromote);
        pmlist.forEach(function (v, k) {
          obj.usedPromote.push(v);
        });
      }
      var str_ = JSON.stringify(obj);
      sessionStorage.obj = str_;
    },
    resetTime: function () {
      var self = this;
      self.times = self.timesFix;
    },
    calPromote: function () {
      var self = this;
      //return;
      //console.log(self.promoteInfo);
      self.usedPromote = [];
      if (!self.promoteInfo || self.promoteInfo.length <= 0) {
        return;
      }

      var prmoteItem = [];
      self.partList.departList.forEach(function (v, k) {
        self.partList[v.dNo].forEach(function (v2, k2) {
          if (v2.selectors && v2.selectors.length > 0) {
            v2.selectors.map(function (item, k3) {
              item.pmAmt = 0;
              item.pmQty = 0;
              item.pmDetl = [];
              prmoteItem.push({
                pno: item.pno, name: item.name, qty: item.qty
                , price: (v2.type == '1' ? v2.priceWM : v2.price)
                 + ((self.oType == '1' || self.oType == '2') ? (v2.boxPrice * v2.boxQty) : 0)
                 + item.seaAmt
                , pDNo: v2.pDNo, pDDNo: v2.pDDNo, type: v2.type, usedQty: 0
                , selItem: {dNo: v.dNo, selDRecno: k2, selRecno: k3}
                , pm: {canPmQty: item.qty, pmDetl: []}
              });
            });
          }
        });
      });
      //console.log(prmoteItem);
      if (!prmoteItem || prmoteItem.length <= 0) {
        return;
      }

      self.promoteInfo.forEach(function (v, k) {
        // TODO 过滤 pmHourS & pmHourE 生效时间段
        if (v.pmCombine == '20' || v.pmCombine == '21' || v.pmCombine == '22') {
          // 满额促销，不处理
          //console.log("满额促销");
        } else if (v.pmCombine == '12' && (v.pmCalc == '50' || v.pmCalc == '51')) {
          //console.log(v.pmTheme);
          // 多重组合、多买多送
          self.pmComb12(v, prmoteItem);
        } else if ((v.pmCombine == '30' || v.pmCombine == '11')
          && (v.pmCalc == '20' || v.pmCalc == '21' || v.pmCalc == '22'
          || v.pmCalc == '40' || v.pmCalc == '41' || v.pmCalc == '42'
          )
        ) {
          //console.log(v.pmTheme);
          // 固定组合 or 红配绿
          self.pmComb11(v, prmoteItem);
        } else if ((v.pmCombine == '10')
          && (v.pmCalc == '60' || v.pmCalc == '61' || v.pmCalc == '62'
          || v.pmCalc == '40'  || v.pmCalc == '41' || v.pmCalc == '42'
          )
        ) {
          //console.log(v.pmTheme);
          // 任意组合 or 第二杯N价
          self.pmComb10(v, prmoteItem);
        } else {
          //console.log("暂不支持的促销类型");
        }
      });
      //console.log(self.usedPromote);
    },
    pmComb10: function (pmInfo, pmItem) {
      //console.log(pmInfo);
      //console.log(pmItem);
      if (pmInfo.pmDetails.length <= 0 || pmInfo.pmTtlQty <= 0) {
        // 表身无资料,退出
        return;
      }

      var self = this;
      let pmDNo = pmInfo.pmDNo;
      let pmPNoS = pmInfo.pmPNoS;
      let pmPNo = pmInfo.pmPNo;

      let tempCalPm = [];
      let curMatchQty = 0;
      let tempTotQty = 0;
      pmItem.forEach(function (v, k) {
        // 匹配符合的商品
        if (v.pm.canPmQty > 0 &&
          ( ((pmDNo != '') && (pmDNo.indexOf("[" + v.pDNo + "]") >= 0))
          || ((pmPNoS != '') && (pmPNoS.indexOf("[" + v.pDDNo + "]") >= 0))
          || ((pmPNo != '') && (pmPNo.indexOf("[" + v.pno + "]") >= 0)) )) {
          tempCalPm.push({rec: k, qty: v.pm.canPmQty, price: v.price});
          tempTotQty += v.pm.canPmQty;
        }
      });

      if (tempTotQty < pmInfo.pmTtlQty) {
        // 满足数量不够
        return;
      }

      curMatchQty = ((pmInfo.pmAccu == 'Y') ? Math.floor(tempTotQty / pmInfo.pmTtlQty) : 1) * pmInfo.pmTtlQty;
      //console.log(curMatchQty);
      //console.log(pmInfo);

      // cal promote amount
      tempCalPm.sort(self.compare('price'));  // 按价格排序

      // 表头优惠时， 先计算总计算金额和总折扣金额，用于分摊时计算。
      let tempTotalAmt = 0; // 总金额
      let tempTotalDisAmt = 0;  // 总折扣金额
      let iTempCalPm = 0;  // 最后一笔flag
      if (pmInfo.pmCalc == '41' || pmInfo.pmCalc == '42') {
        let iTempMatch = 0; // 已匹配量
        try {
          tempCalPm.forEach(function (v, k) {
            let useQty = v.qty - 0;  // 使用量
            iTempCalPm++;
            if (iTempMatch + useQty > curMatchQty) {
              useQty = curMatchQty - iTempMatch;
            }
            tempTotalAmt += v.price * useQty;
            iTempMatch += useQty - 0;
            if (iTempMatch >= curMatchQty) {
              // 分完了，跳下一个促销表身
              throw new Error('EndIterative');
            }
          })
        } catch (e) {
          if (e.message != 'EndIterative') throw e;
        }

        tempTotalDisAmt = Math.floor(curMatchQty / pmInfo.pmTtlQty) * pmInfo.pmPrice;
        if (pmInfo.pmCalc == '42') {
          // 表头-促销价: 折扣金额 = 总金额 - 实收;
          tempTotalDisAmt = tempTotalAmt - tempTotalDisAmt;
        }
        if (tempTotalDisAmt <= 0 || tempTotalAmt <= 0) {
          // 计算出的折扣金额 < 0，该促销不生效
          return;
        }
        tempTotalDisAmt = tempTotalDisAmt.toFixed(2) - 0;
      }

      let pmTotalAmt = 0;  // 总促销金额
      let iMatch = 0; // 已匹配量
      let iLastUseQty = 0; // 上一笔占有量
      let iCalPm = 0;
      try {
        tempCalPm.forEach(function (v, k) {
          let useQty = v.qty - 0;  // 使用量
          let calQty = 0;  // 计算量
          let calAmt = 0; // 折扣金额
          iCalPm++;

          if (iMatch + useQty > curMatchQty) {
            useQty = curMatchQty - iMatch;
          }
          calQty = Math.floor((useQty + iLastUseQty) / pmInfo.pmTtlQty);

          if (calQty > 0) {
            if (pmInfo.pmCalc == '60') {
              // 第二杯N加
              calAmt = ((v.price * pmInfo.pmPrice / 100) * calQty).toFixed(2) - 0;
            } else if (pmInfo.pmCalc == '61') {
              // 第二杯-折让
              calAmt = (Math.min(v.price, pmInfo.pmPrice) * calQty).toFixed(2) - 0;
            } else if (pmInfo.pmCalc == '62') {
              // 第二杯-促销价
              calAmt = v.price > pmInfo.pmPrice ? ((v.price - pmInfo.pmPrice) * calQty).toFixed(2) - 0 : 0;
            } else if (pmInfo.pmCalc == '40') {
              // 任意组合-表头-折扣
              calAmt = ((v.price * pmInfo.pmPrice / 100) * useQty).toFixed(2) - 0;
            } else if (pmInfo.pmCalc == '41' || pmInfo.pmCalc == '42') {
              if (iCalPm == iTempCalPm) {
                // 最后一笔
                calAmt = tempTotalDisAmt - pmTotalAmt.toFixed(2);
              } else {
                calAmt = v.price * useQty * tempTotalDisAmt / tempTotalAmt;
              }
            }
          }
          calAmt = calAmt.toFixed(2) - 0;

          iMatch += useQty - 0;
          iLastUseQty = iMatch % pmInfo.pmTtlQty;

          // 更新购物车（temp）
          pmItem[v.rec].usedQty += (useQty - 0);
          pmItem[v.rec].pm.canPmQty -= useQty;
          pmItem[v.rec].pm.pmDetl.push({
            pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmQty: (useQty - 0), pmAmt: calAmt
          });
          // 更新购物车
          self.partList[pmItem[v.rec].selItem.dNo][pmItem[v.rec].selItem.selDRecno].selectors[pmItem[v.rec].selItem.selRecno].pmQty += useQty;
          self.partList[pmItem[v.rec].selItem.dNo][pmItem[v.rec].selItem.selDRecno].selectors[pmItem[v.rec].selItem.selRecno].pmDetl.push({
            pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmAmt: calAmt, pmQty: useQty
          });
          // 更新促销总金额
          if (calAmt > 0) {
            self.partList[pmItem[v.rec].selItem.dNo][pmItem[v.rec].selItem.selDRecno].selectors[pmItem[v.rec].selItem.selRecno].pmAmt += (calAmt - 0);
            pmTotalAmt += calAmt - 0;
          }

          if (iMatch >= curMatchQty) {
            // 分完了，跳下一个促销表身
            throw new Error('EndIterative');
          }
        })
      } catch (e) {
        if (e.message != 'EndIterative') throw e;
      }

      if (pmTotalAmt > 0) {
        self.usedPromote.push({pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmTotalAmt: (pmTotalAmt.toFixed(2) - 0)});
      }
    },
    pmComb11: function (pmInfo, pmItem) {
      //console.log(pmInfo);
      //console.log(pmItem);
      if (pmInfo.pmRG == '' || pmInfo.pmRGQty == '') {
        // 非固定组合,退出
        return;
      }
      if (pmInfo.pmDetails.length <= 0) {
        // 表身无资料,退出
        return;
      }
      var self = this;

      let curMatchQty = 0;
      try {
        pmInfo.pmDetails.forEach(function (v, k) {
          v.usedTotalQty = 0;
          v.inputPno = false;

          let tempCalPm = [];
          pmItem.forEach(function (v2, k2) {
            // 匹配符合的商品
            if (v2.pm.canPmQty > 0 &&
              ( ((v.pmdDNo != '') && ( v.pmdDNo == v2.pDNo || (v.pmdDNo.indexOf("[" + v2.pDNo + "]") >= 0)))
              || ((v.pmdPNoS != '') && ( v.pmdPNoS == v2.pDDNo || (v.pmdPNoS.indexOf("[" + v2.pDDNo + "]") >= 0)))
              || ((v.pmdPNo != '') && ( v.pmdPNo == v2.pno || (v.pmdPNo.indexOf("[" + v2.pno + "]") >= 0))) )) {
              v.usedTotalQty += v2.pm.canPmQty - 0;
              v.inputPno = true;

              tempCalPm.push({rec: k2, pNo: v2.pno, qty: v2.pm.canPmQty, price: v2.price});
            }
          });

          if (!v.inputPno || v.usedTotalQty < v.pmdQty) {
            // 表身的商品不满足促销表身商品设置的数量,退出
            //console.log({inputPno: v.inputPno, usedTotalQty: v.usedTotalQty, pmdQty: v.pmdQty});
            //console.log("表身的商品不满足促销表身商品设置的数量,退出");
            throw new Error('EndIterative');
          }

          tempCalPm.sort(self.compare('price'));  // 按价格排序
          v.pmSaleDList = tempCalPm;  // 满足组别的商品项

          // 计算出生效多少个
          if (curMatchQty == 0) {
            curMatchQty = Math.floor(v.usedTotalQty / v.pmdQty);
          } else {
            curMatchQty = Math.min(curMatchQty, Math.floor(v.usedTotalQty / v.pmdQty));
          }
        });
      } catch (e) {
        if (e.message != 'EndIterative') {
          throw e;
        } else {
          return;
        }
      }

      if (!(pmInfo.pmAccu == 'Y')) { // 累计标识
        curMatchQty = 1;
      }
      //console.log(curMatchQty);
      //console.log(pmInfo);
      // TODO 更复杂的匹配处理,组别中用相同商品? PMComb11.checkMatchQTY PM_SALE_D_MIX


      if (pmInfo.pmCalc == '21' || pmInfo.pmCalc == '22') {
        pmInfo.pmDetails.forEach(function (v, k) {
          v.totalAmt = 0;
          v.totalDisAmt = 0;
          v.iPmSaleDList = 0;

          let totalAmt = 0; // 总金额
          let totalDisAmt = 0;  // 总折扣金额
          let iPmSaleDList = 0;  // 最后一笔flag

          const maxQty = curMatchQty * v.pmdQty;
          let useQty = 0; // 已用量
          //console.log(maxQty);
          try {
            v.pmSaleDList.forEach(function (v2, k2) {
              let calQty = Math.min(v2.qty, maxQty - useQty);  // 计算量
              useQty += calQty - 0;
              iPmSaleDList++;
              totalAmt += v2.price * calQty;
              if (useQty >= maxQty) {
                // 分完了，跳下一个促销表身
                throw new Error('EndIterative');
              }
            });
          } catch (e) {
            if (e.message != 'EndIterative')  throw e;
          }

          totalDisAmt = curMatchQty * v.pmdPrice;
          if (pmInfo.pmCalc == '22') {
            // 表头-促销价: 折扣金额 = 总金额 - 实收;
            totalDisAmt = totalAmt - totalDisAmt;
          }
          //if (totalDisAmt <= 0 || totalAmt <= 0) {
          //  // 计算出的折扣金额 < 0，该促销不生效
          //  return;
          //}
          totalDisAmt = totalDisAmt.toFixed(2) - 0;

          v.totalAmt = totalAmt - 0;
          v.totalDisAmt = totalDisAmt - 0;
          v.iPmSaleDList = iPmSaleDList - 0;
        });
      }
      // 表头优惠时， 先计算总计算金额和总折扣金额，用于分摊时计算。
      let tempTotalAmt = 0; // 总金额
      let tempTotalDisAmt = 0;  // 总折扣金额
      let iTempPmDetail = 0;  // 最后一笔flag
      let iTempPmSaleDList = 0;  // 最后一笔flag
      if (pmInfo.pmCalc == '41' || pmInfo.pmCalc == '42') {
        // 表头-折让 or 表头-促销价; 先计算出总折扣、总参与金额
        pmInfo.pmDetails.forEach(function (v, k) {
          const maxQty = curMatchQty * v.pmdQty;
          let useQty = 0; // 已用量
          iTempPmDetail++;
          iTempPmSaleDList = 0;
          //console.log(maxQty);
          try {
            v.pmSaleDList.forEach(function (v2, k2) {
              let calQty = Math.min(v2.qty, maxQty - useQty);  // 计算量
              useQty += calQty - 0;
              iTempPmSaleDList++;
              tempTotalAmt += v2.price * calQty;
              if (useQty >= maxQty) {
                // 分完了，跳下一个促销表身
                throw new Error('EndIterative');
              }
            });
          } catch (e) {
            if (e.message != 'EndIterative')  throw e;
          }
        });

        tempTotalDisAmt = curMatchQty * pmInfo.pmPrice;
        if (pmInfo.pmCalc == '42') {
          // 表头-促销价: 折扣金额 = 总金额 - 实收;
          tempTotalDisAmt = tempTotalAmt - tempTotalDisAmt;
        }
        if (tempTotalDisAmt <= 0 || tempTotalAmt <= 0) {
          // 计算出的折扣金额 < 0，该促销不生效
          return;
        }
        tempTotalDisAmt = tempTotalDisAmt.toFixed(2) - 0;
      }

      // cal promote amount
      let pmTotalAmt = 0;
      let iPmDetail = 0;
      pmInfo.pmDetails.forEach(function (v, k) {
        const maxQty = curMatchQty * v.pmdQty;
        let useQty = 0; // 已用量
        iPmDetail++;
        let iPmSaleDList = 0;
        let totalPmSaleDList = 0;
        //console.log(maxQty);
        try {
          v.pmSaleDList.forEach(function (v2, k2) {
            let calQty = Math.min(v2.qty, maxQty - useQty);  // 计算量
            useQty += calQty - 0;
            iPmSaleDList++;
            //console.log(useQty);
            let calAmt = 0; // 折扣金额
            if (pmInfo.pmCalc == '20') {
              // 表身-折扣 (商品价 * 折扣/100)
              calAmt = (v2.price * v.pmdPrice / 100) * calQty;
            } else if (pmInfo.pmCalc == '21' || pmInfo.pmCalc == '22') {
              // 表身-折让 & 表身-促销价
              if (iPmSaleDList == v.iPmSaleDList) {
                // 最后一笔
                calAmt = v.totalDisAmt - totalPmSaleDList.toFixed(2);
              } else {
                calAmt = v.totalAmt <= 0 ? 0 : v2.price * calQty * v.totalDisAmt / v.totalAmt;
              }
            } else if (pmInfo.pmCalc == '40') {
              // 表头-折扣 (商品价 * 头-折扣/100)
              calAmt = (v2.price * pmInfo.pmPrice / 100) * calQty;
            } else if (pmInfo.pmCalc == '41' || pmInfo.pmCalc == '42') {
              if (iPmDetail == iTempPmDetail && iPmSaleDList == iTempPmSaleDList) {
                // 最后一笔
                calAmt = tempTotalDisAmt - pmTotalAmt.toFixed(2);
              } else {
                calAmt = v2.price * calQty * tempTotalDisAmt / tempTotalAmt;
              }
            }
            calAmt = calAmt.toFixed(2) - 0;

            // 更新购物车（temp）
            pmItem[v2.rec].usedQty += (calQty - 0);
            pmItem[v2.rec].pm.canPmQty -= calQty;
            pmItem[v2.rec].pm.pmDetl.push({
              pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmQty: (calQty - 0), pmAmt: calAmt
            });
            // 更新购物车
            self.partList[pmItem[v2.rec].selItem.dNo][pmItem[v2.rec].selItem.selDRecno].selectors[pmItem[v2.rec].selItem.selRecno].pmQty += calQty;
            self.partList[pmItem[v2.rec].selItem.dNo][pmItem[v2.rec].selItem.selDRecno].selectors[pmItem[v2.rec].selItem.selRecno].pmDetl.push({
              pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmAmt: calAmt, pmQty: calQty
            });
            // 更新促销总金额
            if (calAmt > 0) {
              self.partList[pmItem[v2.rec].selItem.dNo][pmItem[v2.rec].selItem.selDRecno].selectors[pmItem[v2.rec].selItem.selRecno].pmAmt += calAmt - 0;
              pmTotalAmt += calAmt - 0;
              totalPmSaleDList += calAmt - 0;
            }

            if (useQty >= maxQty) {
              // 分完了，跳下一个促销表身
              throw new Error('EndIterative');
            }
          });
        } catch (e) {
          if (e.message != 'EndIterative')  throw e;
        }
      });

      if (pmTotalAmt > 0) {
        self.usedPromote.push({pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmTotalAmt: (pmTotalAmt.toFixed(2) - 0)});
      }
    },
    pmComb12: function (pmInfo, pmItem) {
      //console.log(pmInfo);
      //console.log(pmItem);
      var self = this;
      var pmDNo = pmInfo.pmDNo;
      var pmPNoS = pmInfo.pmPNoS;
      var pmPNo = pmInfo.pmPNo;
      var tempCalPm = [];
      var tempTotQty = 0;
      pmItem.forEach(function (v, k) {
        // 匹配符合的商品
        if (v.pm.canPmQty > 0 &&
          ( ((pmDNo != '') && (pmDNo.indexOf("[" + v.pDNo + "]") >= 0))
          || ((pmPNoS != '') && (pmPNoS.indexOf("[" + v.pDDNo + "]") >= 0))
          || ((pmPNo != '') && (pmPNo.indexOf("[" + v.pno + "]") >= 0)) )) {
          tempCalPm.push({rec: k, qty: v.pm.canPmQty, price: v.price});
          tempTotQty += v.pm.canPmQty;
        }
      });
      if (tempCalPm.length <= 0 || tempTotQty <= 0) {
        // 没有符合的，退出
        return;
      }

      // cal promote amount
      tempCalPm.sort(self.compare('price'));  // 按价格排序
      //console.log(tempCalPm);
      var pmTotalAmt = 0;
      var pmTtlQty = [pmInfo.pmTtlQty - 0, pmInfo.pmTtlQty2 - 0, pmInfo.pmTtlQty3 - 0];
      var pmPrice = [pmInfo.pmPrice - 0, pmInfo.pmPrice2 - 0, pmInfo.pmPrice3 - 0];
      if (tempTotQty < (pmTtlQty[2] > 0 ? pmTtlQty[2] : (pmTtlQty[1] > 0 ? pmTtlQty[1] : pmTtlQty[0]))) {
        // 没有符合最低数量， 退出
        return;
      }
      // 计算参与数量
      var balanceQty = tempTotQty - 0;
      var discQty = [0, 0, 0];  // 多重1，2，3步长
      var discFreeQty = [0, 0, 0];  // 多重1，2，3免减步长
      for (var i = 0; i <= 2; i++) {
        if (pmInfo.pmAccu == 'Y') { // 累计标识
          if (balanceQty > 0 && balanceQty >= pmTtlQty[i]) {
            discQty[i] = Math.floor(balanceQty / pmTtlQty[i]) * pmTtlQty[i];
            discFreeQty[i] = Math.floor(balanceQty / pmTtlQty[i]) * pmPrice[i];
            balanceQty = balanceQty % pmTtlQty[i];
          }
          if (balanceQty < pmTtlQty[2]) {
            break;
          }
        } else {
          if (balanceQty >= pmTtlQty[i]) {
            discQty[i] = pmTtlQty[i];
            discFreeQty[i] = pmPrice[i];
            break;
          }
        }
      }
      //console.log("discQTY: " + discQty[0] + ";discQTY2: " + discQty[1] + ";discQTY3: " + discQty[2] + ";balanceQty: " + balanceQty);

      var iMatch = 0; // 已匹配量
      var iLastUseQty = 0; // 上一笔占有量
      try {
        tempCalPm.forEach(function (v, k) {
          var bNext = false;
          var iQtyTmp = v.qty - 0;
          var totalDiscQty = 0;
          for (var i = 0; i <= 2; i++) {
            totalDiscQty += (discQty[i] - 0);
            if (discQty[i] > 0 && totalDiscQty > iMatch && !bNext) {
              var calQty = 0, useQty = 0, extQty = 0; // 计算量,促销量(算金额),溢出数量
              calQty = (iQtyTmp + iMatch) > totalDiscQty ? totalDiscQty - iMatch : iQtyTmp;
              extQty = iQtyTmp - calQty;

              if (pmInfo.pmCalc == '50') {
                // 多买多送 - 商家优惠
                useQty = totalDiscQty - discFreeQty[i] > calQty + iMatch ? 0 : (iMatch > (totalDiscQty - discFreeQty[i]) ? calQty : (calQty + iMatch) - (totalDiscQty - discFreeQty[i]));
              } else if (pmInfo.pmCalc == '51') {
                // 多买多送 - 客人优惠
                useQty = Math.floor(calQty / pmTtlQty[i]) * pmPrice[i];
                var modCalQtyTmp = calQty % pmTtlQty[i] - 0;  //1. 先计算自己满足的
                if ((iLastUseQty + modCalQtyTmp) >= pmTtlQty[i]) {
                  // 2. 再计算和上一笔剩余的，超出满足数量
                  useQty += (iLastUseQty > (pmTtlQty[i] - pmPrice[i]) ? pmTtlQty[i] - iLastUseQty : pmPrice[i]);
                  iLastUseQty = iLastUseQty + modCalQtyTmp - pmTtlQty[i];
                  // 3. 最后再算剩下的
                  if (iLastUseQty > (pmTtlQty[i] - pmPrice[i])) {
                    useQty += iLastUseQty - (pmTtlQty[i] - pmPrice[i]);
                  }
                } else {
                  if ((iLastUseQty + modCalQtyTmp) > (pmTtlQty[i] - pmPrice[i])) {
                    useQty += iLastUseQty > (pmTtlQty[i] - pmPrice[i]) ? modCalQtyTmp : ((iLastUseQty + modCalQtyTmp) - (pmTtlQty[i] - pmPrice[i]));
                  }
                  iLastUseQty += modCalQtyTmp;
                }
                if (extQty > 0) {
                  iLastUseQty = 0;
                }
              } else {
                return;
              }

              pmItem[v.rec].usedQty += (calQty - 0);
              pmItem[v.rec].pm.canPmQty -= calQty;
              pmItem[v.rec].pm.pmDetl.push({
                pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmQty: (calQty - 0), pmAmt: v.price * useQty
              });
              self.partList[pmItem[v.rec].selItem.dNo][pmItem[v.rec].selItem.selDRecno].selectors[pmItem[v.rec].selItem.selRecno].pmQty += calQty;
              self.partList[pmItem[v.rec].selItem.dNo][pmItem[v.rec].selItem.selDRecno].selectors[pmItem[v.rec].selItem.selRecno].pmDetl.push({
                pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmAmt: ((v.price * useQty).toFixed(2) - 0), pmQty: calQty
              });
              if (useQty > 0) {
                self.partList[pmItem[v.rec].selItem.dNo][pmItem[v.rec].selItem.selDRecno].selectors[pmItem[v.rec].selItem.selRecno].pmAmt += ((v.price * useQty).toFixed(2) - 0);
                pmTotalAmt += v.price * useQty;
              }
              iQtyTmp = extQty - 0;
              iMatch += (calQty - 0);
              bNext = (iQtyTmp <= 0);
              if (bNext) break;
            }
          }
          if (!bNext) {
            throw new Error('EndIterative');
          }
        })
      } catch (e) {
        if (e.message != 'EndIterative') throw e;
      }

      if (pmTotalAmt > 0) {
        self.usedPromote.push({pmNo: pmInfo.pmNo, pmTheme: pmInfo.pmTheme, pmTotalAmt: (pmTotalAmt.toFixed(2) - 0)});
      }
    },
    compare: function (fName, isDesc) {
      return function (a, b) {
        var value1 = a[fName];
        var value2 = b[fName];
        return isDesc || true ? value2 - value1 : value1 - value2;
      }
    },
    showFullPm: function (isShow) {
      var self = this;
      self.isShowFullPm = isShow;
      if (!isShow) {
        // 取消满额促销
        self.fullPmList = [];
      }
    },
    jumpToTypeChoose: function () {
      this.saveData();
      window.location.href = "bmTypeChoose.html?ver=1.0&ver2=" + Math.random();
    },
    jumpToStart: function () {
      sessionStorage.obj = null;
      window.location.href = "bmStart.html?ver=1.0&ver2=" + Math.random();
    },
    jumpToPay: function () {
      var self = this;
      self.fullPmList = [];
      if (self.promoteInfo && self.promoteInfo.length > 0) {
        // 处理满额促销
        var prompte = Object.assign([], self.promoteInfo);
        prompte.forEach(function (v, k) {
          if ((v.pmCombine == '20' || v.pmCombine == '21' || v.pmCombine == '22') && (v.pmCalc == '30' || v.pmCalc == '31')) {
            // 指定项、排除项
            let fitPmAmt = 0;
            if (v.pmCombine == '20' && self.usedPromote.length <= 0) {
              fitPmAmt = self.totalCount.total - 0;
            } else {
              self.partList.departList.forEach(function (v1) {
                self.partList[v1.dNo].forEach(function (v3) {
                  if (v3.selectors && v3.selectors.length > 0) {
                    v3.selectors.map(function (item, k3) {
                      let canCal = (v.pmCombine == '21' || v.pmCombine == '20');
                      if ((v.pmFullDNo != '') && (v.pmFullDNo.indexOf("[" + v3.pDNo + "]") != -1)) {
                        canCal = !(v.pmCombine == '21');
                      }
                      if ((v.pmFullPNos != '') && (v.pmFullPNos.indexOf("[" + v3.pDDNo + "]") != -1)) {
                        canCal = !(v.pmCombine == '21');
                      }
                      if ((v.pmFullPNo != '') && (v.pmFullPNo.indexOf("[" + v3.pno + "]") != -1)) {
                        canCal = !(v.pmCombine == '21');
                      }
                      if (canCal) {
                        fitPmAmt += (((self.oType == '1' ? v3.priceWM : v3.price) + ((self.oType == '1' || self.oType == '2') ? (v3.boxPrice * v3.boxQty) : 0))
                        * (item.qty - item.pmQty)) + ((item.qty - item.pmQty) * item.seaAmt);
                      }
                    });
                  }
                });
              });
            }

            if (v.pmFullAmt <= fitPmAmt) {
              v.pmFullGift.forEach(function (vg, kg) {
                vg.gChoose = 0;
              });
              let newItem = {};
              newItem.pmNo = v.pmNo;
              newItem.pmTheme = v.pmTheme;
              newItem.pmCombine = v.pmCombine;
              newItem.pmCalc = v.pmCalc;
              newItem.pmAccu = v.pmAccu;
              newItem.pmFullAmt = v.pmFullAmt;
              newItem.pmTtlQty = v.pmTtlQty;
              newItem.pmPrice = v.pmPrice;
              newItem.pmFullGift = v.pmFullGift;
              newItem.pmFitAmt = fitPmAmt;
              newItem.pmChoose = false;
              newItem.pmChooseQty = 0;
              self.fullPmList.push(newItem);
            }
          }
        });

        if (self.fullPmList.length > 0) {
          self.isShowFullPm = true;
          self.fullGiftPayBtn = self.fullGiftPayBtnName[0];
          setTimeout(function () {
            self.scroll5.refresh();
            //console.log(self.fullPmList);
          }, 500);
          return;
        }
      }
      this.saveData();
      window.location.href = "bmPay.html?cNo=" + this.cNo + "&ver2=" + Math.random();
    },
    changeFullPm: function (iFull, iGift, changeQty) {
      var self = this;
      let hasChoose = false;
      self.fullPmList.forEach(function (v, k) {
        if (k == iFull) {
          v.pmChooseQty += changeQty;
          v.pmFullGift[iGift].gChoose += changeQty;
          if (changeQty < 0) {
            if (v.pmFullGift[iGift].gChoose > 0) {
              hasChoose = true;
            } else {
              v.pmFullGift.forEach(function (v3, k3) {
                if (v3.gChoose > 0) {
                  hasChoose = true;
                }
              })
            }
          }
        } else {
          v.pmChooseQty = 0;
          v.pmFullGift.forEach(function (v2, k2) {
            v2.gChoose = 0;
          })
        }
      });
      self.fullGiftPayBtn = self.fullGiftPayBtnName[(changeQty > 0 || hasChoose ? 1 : 0)];
    },
    hideFullPm: function () {
      var self = this;
      self.fullPmList = [];
      self.isShowFullPm = false;
    },
    jumpToPay2: function () {
      this.saveData(true);
      window.location.href = "bmPay.html?cNo=" + this.cNo + "&ver2=" + Math.random();
    }
  }
});


// 禁止鼠标右键
document.oncontextmenu = function () {
  return false;
};
// 禁用网页上选取的内容
document.onselectstart = function () {
  return false;
};
// 禁用复制
document.oncopy = function () {
  return false;
};
var cartIconOffset = $("#cartIcon").offset();

/*
(function(){var timer = setTimeout(this.marquee, 1000);}());
function marquee() {
  var scrollWidth = $('#promoteMsg').width();
  var textWidth = $('.usedPromote').width();
  var i = scrollWidth * 0.1;
  setInterval(function () {
    i--;
    if (i < -textWidth) {
      i = scrollWidth;
    }
    $('.usedPromote').animate({'left': i + 'px'}, 20);
  }, 20);
}*/
