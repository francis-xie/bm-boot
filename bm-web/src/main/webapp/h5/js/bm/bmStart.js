// $Id$
var vm = new Vue({
  el: '#app',
  data: {
    // 轮播图地址
    ADImgList: [],
    ADImgOnly: ''
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
      bmAPI.doAction('getIndexImgList', {}, function (ret) {
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
    },
    jumpToTypeChoose: function () {
      //console.log(parent.window.vm);
      window.location.href = "bmTypeChoose.html?ver=1.0&ver2=" + Math.random();
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
