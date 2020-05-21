// 负责与后端对接数据 这里先使用前端mock实现
// $Id$
window.bmAPI = (function () {
  var ENV = 'dev';
  var api = {};

  var inArray = function (arr, item) {
    if (!arr) {
      return false
    }
    for (var i = 0; i < arr.length; i++) {
      if (arr[i] == item) {
        return true
      }
    }
    return false
  };

  if (ENV == 'dev') {
    var callAjax = function (method, path, param, success, fail, netError) {
      if (!method || method == '') {
        method = 'GET';
      }
      $.ajax({
        url: utils.basePath() + path,
        method: method,
        data: param,
        success: function (ret) {
          if (success && typeof (success) == 'function') {
            success(ret);
          } else {
          }
        },
        error: function (ret) {
          if (fail && typeof (fail) == 'function') {
            fail(ret);
          } else {
          }
        }
      }).done().fail(function () {
        if (netError && typeof (netError) == 'function') {
          netError();
        }
      })
    };
    var actList = {
      // 公共资料(系统参数)查询
      commonProp: {
        key: 'ws/bm/common/prop',
        checkCode: ['0'],  // default '0'
        method: 'GET'  // default GET
      },
      // 1.1 获取主页轮播图
      getIndexImgList: {
        key: 'ws/bm/images/getIndexImgList'
      },
      // 1.2 获取点餐页头部轮播图
      getOrderHeadImgList: {
        key: 'ws/bm/images/getOrderHeadImgList'
      },
      // 1.3 获取门店基本信息
      getStoreInfo: {
        key: 'ws/bm/store/getStoreInfo'
      },
      // 1.20 获取商品分类列表
      getDepartList: {
        key: 'ws/bm/part/getDepartList'
      },
      // 1.21 获取商品列表
      getPartList: {
        key: 'ws/bm/part/getPartList'
      },
      // 1.22 获取商品信息
      getPartInfo: {
        key: 'ws/bm/part/getPartInfo'
      },
      // 1.23 获取促销设置
      getPromoteInfo: {
        key: 'ws/bm/part/getPromoteInfo'
      },
      // 1.40 产生订单
      orderAdd: {
        key: 'ws/bm/order/orderAdd',
        checkCode: ['0','00'],  // default '0'
        method: 'POST'  // default GET
      },
      // 1.90 同步数据
      synPartData: {
        key: 'ws/bm/syndata/synPartData'
      },
      // 1.91 登录检查
      checkLogin: {
        key: 'ws/bm/syndata/checkLogin'
      }
    };

    api = {
      //callAjax: callAjax,
      doAction: function (act, param, success, fail, netError) {
        console.log(act);
        if (!(actList[act])) {
          if (fail && typeof(fail) == 'function') {
            fail({
              code: '999',
              msg: '无效接口'
            })
          }
          return;
        }
        var checkCode = actList[act].checkCode || ['0'];
        var method = actList[act].method || 'GET';
        //param.userId = sessionStorage.getItem('userId');
        //param.userType = sessionStorage.getItem('userType');
        console.log(param);
        callAjax(method, actList[act].key, param,
          function (ret) {
            if (inArray(checkCode, (ret.code || ''))) {
              if (success && typeof (success) == 'function') {
                success(ret);
              }
            } else {
              if (fail && typeof (fail) == 'function') {
                fail(ret);
              }
            }
          }, fail, netError);
      }
    }
  }
  return api;
})();