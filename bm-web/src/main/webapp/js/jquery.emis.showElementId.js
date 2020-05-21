// jQuery Plugin for showElemeId start
(function ($) {
  if (typeof $ == "undefined") return;
  $.fn.showElemId = function (bgc, fc) {
    var $pups = $("span.emis_pup");
    if ($pups.length > 0) {
      $pups.remove();
      return;
    }
    // 把HTML裡的SQL=複製到剪貼簿.
    var _sURL = "--URL=" + window.location.href;
    var _sSource = $("body").html();
    var _iIndex = _sSource.indexOf("SQL=");
    if (_iIndex < 0) {
      _iIndex = _sSource.indexOf("sql=");
    }
    if (_iIndex > 0) {
      _sSource = _sSource.substring(_iIndex);
      _iIndex = _sSource.indexOf("ExecuteQuery(");
      if (_iIndex > 0) {
        _sSource = _sSource.substring(0, _iIndex);
        window.clipboardData.setData('Text', _sURL + "\n--" + _sSource);
      }
    } else {
      html = "<span class='emis_pup' style='position: absolute; z-index: 999; top:0;left:0;background-color:blue;color:white;'>" +
          window.location.href + "</span>";
      $("body").append(html);
    }

    // 把HTML的element顯示其Name或ID.
    return this.each(function () {
      var $elem = $(this);
      var _sName = $elem.attr("dataFld");
      if (_sName == undefined || _sName == "") _sName = $elem.attr("name");
      //if (!$elem.is(":visible")) return;
      var pos = $elem.offset();
      html = "<span class='emis_pup' onclick='alert(this.innerText);' style='position: absolute; z-index: 999; top: " +
          (pos.top + 1) + "px; left: " + pos.left +
          "px; background-color: " + (bgc || "red") + "; color: " +
          (fc || "white") + "; font-size: 10px; line-height: 12px; padding: 1px; cursor:pointer'>" +
          (_sName || "----") +
          "</span>";
      $("body").append(html);
    });
  };
  $(document).keydown(function (e) {
    if (e.ctrlKey && e.shiftKey && e.keyCode == 70) {  // CTRL + SHIFT + F
      $("input,select,textarea,div,span").filter(":visible").showElemId();
      return false;
    }
  });
})(jQuery);
// jQuery Plugin for showElemeId end