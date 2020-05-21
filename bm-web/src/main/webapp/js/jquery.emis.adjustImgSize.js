(function($){
  //图片按比例缩放
  $.adjustImgSize = function (sImgId, boxWidth, boxHeight) {
    var $img = $(sImgId);
    var image = new Image();
    image.src = $img.attr("src") + ($.browser.msie ? "?" + (new Date().getTime()) : "");
    $img.hide();
    image.onload = function () {
      var imgWidth = image.width;
      var imgHeight = image.height;
      if (imgWidth == 0 || imgHeight == 0) return;
      var margin = 0;
      $img.css("margin", margin);
      //比较imgBox的长宽比与img的长宽比大小
      if ((boxWidth / boxHeight) >= (imgWidth / imgHeight)) {
        //重新设置img的width和height
        $img.width((boxHeight * imgWidth) / imgHeight);
        $img.height(boxHeight);
        //让图片居中显示
        margin = (boxWidth - $img.width()) / 2;
        $img.css("margin-left", margin);
      }
      else {
        //重新设置img的width和height
        $img.width(boxWidth);
        $img.height((boxWidth * imgHeight) / imgWidth);
        //让图片居中显示
        margin = (boxHeight - $img.height()) / 2;
        $img.css("margin-top", margin);
      }
      $img.show();
    };
  };
  //图片按比例缩放
  $.fn.adjustImgSize = function (options) {
    var opts = $.extend({width: -1, height: -1}, options);
    var boxWidth = opts.width, boxHeight = opts.height;
    return this.each(function (ele, i) {
      if (/img/i.test(this.nodeName)) {
        if (boxWidth <= 0 || boxHeight <= 0) {
          $(this).hide();
          var $div = $(this).parent();
          boxWidth = $div.width();
          boxHeight = $div.height();
          $.adjustImgSize(this, boxWidth, boxHeight);
          $(this).show();
        }
      } else {
        $.adjustImgSize(this, boxWidth, boxHeight);
      }
    });
  }
})(jQuery);