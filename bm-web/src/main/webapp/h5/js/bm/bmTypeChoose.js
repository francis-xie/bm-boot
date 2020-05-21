// $Id$
var vm = new Vue({
  el: '#app',
  data: {
    // 轮播图地址
    ADImgList: [],
    ADImgOnly: '',
    date: '',
    time: '',
    comp: {
      "icon": "",
      "name": "",
      "tel": ""
    }
  },
  computed: {},
  beforeCreate: function () {
  },
  created: function () {
    this.pageReady();
  },
  beforeMount: function () {
  },
  mounted: function () {
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

      bmAPI.doAction('getStoreInfo', {}, function (ret) {
        self.comp.icon = ret.result.sIcon || '';
        self.comp.name = ret.result.sName || '';
        self.comp.tel = ret.result.sTel || '';
      }, function (ret) {
      });

      var date = new Date();
      var minute = date.getMinutes();
      var second = date.getSeconds();
      self.date = date.getFullYear() + '/' + (date.getMonth() + 1) + '/' + date.getDate();
      self.time = date.getHours() + ':' + (minute < 10 ? ('0' + minute) : minute) + ':' + (second < 10 ? ('0' + second) : second);
      self.setDateTime();
      setTimeout(function () {
        self.jumpToStart();
      }, 30000)
    },
    setDateTime: function () {
      var self = this;
      setInterval(function () {
        var date = new Date();
        var minute = date.getMinutes();
        var second = date.getSeconds();
        self.date = date.getFullYear() + '/' + (date.getMonth() + 1) + '/' + date.getDate();
        self.time = date.getHours() + ':' + (minute < 10 ? ('0' + minute) : minute) + ':' + (second < 10 ? ('0' + second) : second);
      }, 1000);
    },
    exitfull: function (oType) {
      if (parent) {
        parent.vm.fullScreenExit();
      }
    },
    jumpToOrder: function (oType) {
      window.location.href = "bmOrder.html?ver=1.0&oType=" + oType + "&ver2=" + Math.random();
    },
    jumpToStart: function () {
      sessionStorage.obj = null;
      window.location.href = "bmStart.html?ver=1.0&ver2=" + Math.random();
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