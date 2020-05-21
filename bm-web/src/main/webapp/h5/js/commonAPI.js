// 负责与后端对接数据 这里先使用前端mock实现
// $Id$
window.commonAPI = (function () {
  var ENV = 'dev';
  var api = {};

  if (ENV == 'dev') {
    // 通用 get 请求
    var get = function (path, param, success, fail) {
      $.ajax({
        url: utils.basePath() + path,
        method: 'GET',
        data: param,
        success: function (ret) {
          if (success && typeof (success) == 'function') {
            success(ret);
          }
        },
        error: function (ret) {
          if (fail && typeof (fail) == 'function') {
            fail(ret);
          }
        }
      })
    };

    // 通用 post 请求
    var post = function (path, param, success, fail) {
      $.ajax({
        url: utils.basePath() + path,
        method: 'POST',
        data: param,
        success: function (ret) {
          if (success && typeof (success) == 'function') {
            success(ret);
          }
        },
        error: function (ret) {
          if (fail && typeof (fail) == 'function') {
            fail(ret);
          }
        }
      })
    };

    api = {
      get: get,
      post: post,
      // 公共资料(系统参数)查询
      commonProp: function (param, success, fail) {
        get('ws/bm/common/prop', {
          "wx_id": param.wx_id,
          "wx_cuid": param.wx_cuid,
          "propKey": param.propKey
        }, function (ret) {
          if (ret.code == '0') {
            if (success && typeof (success) == 'function') {
              success(ret);
            }
          } else {
            if (fail && typeof (fail) == 'function') {
              fail(ret);
            }
          }
        }, fail);
      }
    }
  }
  return api;
})();