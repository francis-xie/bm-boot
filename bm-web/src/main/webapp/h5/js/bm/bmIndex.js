// $Id$
var vm = new Vue({
  el: '#app',
  data: {
    iLoadCnt: 0,
    page: 'login',
    loginData: {
      sPosUrl: '',
      sNo: '',
      sIdNo: '',
      uId: '',
      uPwd: ''
    },
    bmVersion: ''
  },
  computed: {},
  beforeCreate: function () {
  },
  created: function () {
    document.getElementById('ifmOrder').style.height = document.body.clientHeight + "px";
    this.checkSystem();
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
    checkSystem: function () {
      // 验证身份
      var self = this;
      self.pageReady();
      /*if (self.iLoadCnt <= 40) {
       self.iLoadCnt++;
       if (document.getElementById('wx_id').value == "") {
       setTimeout(function () {
       self.checkSystem();
       }, 50)
       } else {
       self.pageReady();
       }
       } else {
       // 超时，先进入页面
       self.pageReady();
       }*/
    },
    pageReady: function () {
      // 进入页面处理内容
      var self = this;
      weui.loading('系统启动中...');
      bmAPI.doAction('commonProp',{
        propKey: "bmSNo,bmIdNo,bmPosUrl,bmPosUId,bmPosUPwd,bmVersion"
      }, function (ret) {
        //console.log(ret);
        self.loginData.sPosUrl = ret.result.bmPosUrl;
        self.loginData.sNo = ret.result.bmSNo;
        self.loginData.sIdNo = ret.result.bmIdNo;
        self.loginData.uId = ret.result.bmPosUId;
        self.loginData.uPwd = ret.result.bmPosUPwd;
        self.bmVersion = ret.result.bmVersion || '';
        weui.loading('loading').hide();
      }, function (ret) {
        weui.topTips(ret.msg, {
          duration: 1500,
          className: 'my-topTips'
        });
        weui.loading('loading').hide();
      });
    },
    showKeyboard: function (obj) {
      //$('#'+obj).getkeyboard();
      //$('#'+obj).focus();
      var kb = $('#' + obj).getkeyboard();
      // close the keyboard if the keyboard is visible and the button is clicked a second time
      if (kb.isOpen) {
        kb.close();
      } else {
        kb.reveal();
      }

    },
    openSales: function () {
      // 开启销售页面
      var self = this;
      self.fullScreenOpen();

      self.loginData.sPosUrl = $('#sPosUrl').val();
      if (self.loginData.sPosUrl == '') {
        weui.topTips("[后台地址]不能为空！", {
          duration: 3000,
          className: 'my-topTips'
        });
        return;
      }
      self.loginData.sNo = $('#sNo').val();
      if (self.loginData.sNo == '') {
        weui.topTips("[门店编号]不能为空！", {
          duration: 3000,
          className: 'my-topTips'
        });
        return;
      }
      self.loginData.sIdNo = $('#sIdNo').val();
      if (self.loginData.sIdNo == '') {
        weui.topTips("[机台编号]不能为空！", {
          duration: 3000,
          className: 'my-topTips'
        });
        return;
      }
      self.loginData.uId = $('#uId').val();
      if (self.loginData.uId == '') {
        weui.topTips("[登录账号]不能为空！", {
          duration: 3000,
          className: 'my-topTips'
        });
        return;
      }
      self.loginData.uPwd = $('#uPwd').val();
      if (self.loginData.uPwd == '') {
        weui.topTips("[登录密码]不能为空！", {
          duration: 3000,
          className: 'my-topTips'
        });
        return;
      }

      weui.loading("登录验证中");
      bmAPI.doAction('checkLogin',{
        "sPosUrl": self.loginData.sPosUrl,
        "sNo": self.loginData.sNo,
        "idNo": self.loginData.sIdNo,
        "uId": self.loginData.uId,
        "uPwd": self.loginData.uPwd
      }, function (ret) {
        weui.loading('loading').hide(function(){
          weui.loading('更新资料中...');
          bmAPI.doAction('synPartData',{}, function (ret) {
            document.getElementById('ifmOrder').src = "bmStart.html?ver=1.1&ver2=" + Math.random();
            self.fullScreenOpen();
            self.page = 'sales';
            weui.loading('loading').hide();
          }, function (ret) {
            weui.topTips(ret.msg, {
              duration: 2000,
              className: 'my-topTips'
            });
            weui.loading('loading').hide();
          });
        });
      }, function (ret) {
        weui.topTips(ret.msg, {
          duration: 2000,
          className: 'my-topTips'
        });
        weui.loading('loading').hide();
      });
    },
    fullScreenOpen: function () {
      // 全屏模式
      var docElm = document.documentElement;
      if (docElm.requestFullscreen) {
        //W3C
        docElm.requestFullscreen();
        //console.log("W3C")
      } else if (docElm.mozRequestFullScreen) {
        //FireFox
        docElm.mozRequestFullScreen();
        //console.log("FireFox")
      } else if (docElm.webkitRequestFullScreen) {
        //Chrome等
        docElm.webkitRequestFullScreen();
        //console.log("Chrome等")
      } else if (elem.msRequestFullscreen) {
        //IE11
        elem.msRequestFullscreen();
        //console.log("IE11")
      }
    },
    fullScreenExit: function () {
      // 退出全屏模式
      var self = this;
      if (document.exitFullscreen) {
        document.exitFullscreen();
      }
      else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      }
      else if (document.webkitCancelFullScreen) {
        document.webkitCancelFullScreen();
      }
      else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
      //document.getElementById('ifmOrder').src = "";
      //self.page = 'login';
    }
  }
});
window.onresize = resize;
function resize() {
  document.getElementById('ifmOrder').style.height = document.body.clientHeight + "px";
}