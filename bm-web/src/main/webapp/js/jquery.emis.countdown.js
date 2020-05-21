(function($){
  var className = "loginCountdown";

  function setTimeText(target, maxtime){
    var minutes = Math.floor(maxtime / 60);
    var seconds = Math.floor(maxtime % 60);
    $(target).text((minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
  }
  function run(target, options){
    var timer = options.timer, maxtime = options.maxtime - 1;
    if(!!timer) clearInterval(timer);
    timer = setInterval(function () {
      if (maxtime >= 0) {
        setTimeText(target, maxtime);
        --maxtime;
      } else {
        clearInterval(timer);
        options.handler(target, options);
      }
    },1000);
    options.timer = timer;
  }
  $.fn.loginCountdown = function(options, params){
    // 方法调用
    if (typeof options == 'string') {
      if ($.isFunction($.fn.loginCountdown.methods[options])) {
        this.each(function () {
          return $.fn.loginCountdown.methods[options](this, params);
        });
      }
      return false;
    }

    // 初始化
    options = options || {};
    return this.each(function () {
      var opts, state = $.data(this, className);
      // 第二次处理初始化参数
      if (state) {
        opts = $.extend({}, state.options, options);
      }
      // 首次处理初始化参数
      else {
        opts = $.extend({}, $.fn.loginCountdown.defaults, options);
      }
      // 储存参数
      $.data(this, className, {options:opts});
      // 启动计时
      run(this, opts);
    });
  };
  $.fn.loginCountdown.methods = {
    reset: function(target, options){
      var state = $.data(target, className), opts = state.options;
      run(target, opts);
    }
  };
  $.fn.loginCountdown.defaults = {
    maxtime: 30 * 60,
    handler: function (target, options) {
    }
  };
})(jQuery);
