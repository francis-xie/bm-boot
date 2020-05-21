//===================================================================================================
// global context menu handler
(function ($) {
  var isMenuShow = false;
  window.ajax_menu = function (e) {
    // Added by Joe 如果为模式窗口时且不为开发状态，不允许显示右键菜单；否则则允许
    if ((window.dialogArguments || $("div.toolbar").length == 0) && !isDevelop)
      return false;
    e = e || window.event;
    var whichDiv = e.target || e.srcElement;
    if (!isMenuShow && whichDiv && whichDiv.type &&
        (whichDiv.type.toLowerCase() == "text" || whichDiv.type.toLowerCase() == "textarea")) {
      return true;  // 標示文字時使用IE功能表
    }
    // 当有罩层时不出右键菜单
    if ($("#emisMask:visible").length > 0) return false;
    if ($("#maskLevel:visible").length > 0) return false;
    // 表头或表身出菜单前把
    if ($(document).attr("EMIS_IS_BILL_MODE") === "Y") {
      showActive(1);
      window.frames[0].AjaxHideMenu(e);
    } else if (parent && $(parent.document).attr("EMIS_IS_BILL_MODE") === "Y") {
      parent.showActive(2);
      parent.AjaxHideMenu(e);
    }

    AjaxDisplayMenu(e);
    return false;
  };

  // 顯示右鍵功能表(context menu)
  // 同步修改emis_mtn.htm,emis_tab.htm加入document的oncontextmenu event
  // 建立功能表: makeMenu
  // 顯示功能表: displayMenu
  // 在功能表裡移動時反白光棒: switchMenu
  // 在功能表裡按左鈕以執行: clickMenu
  function AjaxDisplayMenu(e) {
    AjaxMakeMenu(e);
    var $menu = $("#contextMenu");
    $menu.css({"left": 0, "top": 0}).show();
    isMenuShow = true;

    // 超出底部時往上移動
    var top = $menu.css("top"), left = $menu.css("left");
    if (e.clientY + $menu.height() > $(document).height() - 5) {
      top = ((e.clientY - $menu.height()) >= 0 ? (e.clientY - $menu.height()) : 0);
    }else{
      top = e.clientY;
    }

    if (e.clientX + $menu.width() > $(document).width() - 5) {
      left = e.clientX - $menu.width();
    }else{
      left = e.clientX;
    }
    $menu.css({"left": Math.max(left, 0), "top": Math.max(top, 0)});

    // 注册事件，使在菜单以外的地方点击时关闭菜单
    $(document).one('click', AjaxHideMenu);
  }

  function AjaxMakeMenu(e) {
    AjaxHideMenu(e);
    if (parent && $(parent.document).attr("EMIS_IS_BILL_MODE") === "Y") {
      if (parent.currentTable && parent.currentTable.sMode != "") {
        return;
      } else {
        jQuery(parent.document).one('click', AjaxHideMenu);
      }
    }
    var _oElement, _sEleId, _sEleType, _sEleName, _sImg, _sTitle;
    var contextMenu = document.createElement("DIV");  // 動態建立DIV
    contextMenu.id = "contextMenu";
    contextMenu.className = "contextMenu";
    contextMenu.style.display = "none";
    contextMenu.innerHTML =
        [
          "<iframe id='bgiframe' class='bgiframe' frameborder='0'></iframe>",
          "<div id='submenu' class='menuDiv' onselectstart='return false'></div>"
        ].join("");
    document.body.appendChild(contextMenu);  // 把Menu加入body
    $('#contextMenu').click(function (ev) {
      AjaxClickMenu(ev);
    }).mouseover(function (ev) {
          AjaxSwitchMenu(ev);
        }).mouseout(function (ev) {
          AjaxSwitchMenu(ev);
        });
    var _oDiv = document.getElementById("submenu");
    var _oChildDiv, notImg = '<img src="' + sRoot + '/skins/images/s.gif">';

    if (kbrd.bRefreshEnable) { // 如果 keyboard 設定不能 refresh ,這邊也不應該出現重整
      _oChildDiv = document.createElement("DIV");
      _oChildDiv.id = "F5";
      _oChildDiv.className = "menuItem";
      _oChildDiv.innerHTML = notImg + jQuery.getMessage("contextmenu_reload"); //"重新整理";
      _oDiv.appendChild(_oChildDiv);
    }

    _oChildDiv = document.createElement("DIV");
    _oChildDiv.id = "Info";
    _oChildDiv.className = "menuItem";
    _oChildDiv.innerHTML = notImg + jQuery.getMessage("contextmenu_properties"); //"內容";
    _oDiv.appendChild(_oChildDiv);

    if (isDevelop) {
      _oChildDiv = document.createElement("DIV");
      _oChildDiv.id = "FullSource";
      _oChildDiv.className = "menuItem";
      _oChildDiv.innerHTML = notImg + "FullSource"; //"內容";
      _oDiv.appendChild(_oChildDiv);
    }

    var findByClass = function (elems, className) {
      if (!elems || elems.length == 0) return null;
      for (var i = 0, len = elems.length; i < len; i++) {
        if ((elems[i].className || "").search(className) > -1) {
          return elems[i];
        }
      }
    };
    //alert("is here?")
    // 2012/12/19 Joe 调整为只处理TopPanel中的Buttons
    var buttons = (function () {
      var divs = document.getElementsByTagName("div");
      var panel = findByClass(divs, "topPanel");
      return panel == null ? [] : panel.getElementsByTagName("button");
      /*if(panel)
       return panel.getElementsByTagName("button");
       else{
       var tds = document.getElementsByTagName("td");
       panel = findByClass(tds  ,"functions");
       return panel == null ? [] : panel.getElementsByTagName("button");
       }*/
    })();
    //alert(buttons.length)
    var firstFlag = true;
    for (var i = 0; i < buttons.length; i++) {
      _oElement = buttons[i];
      _sEleId = _oElement.id;
      _sEleType = _oElement.type;
      _sEleName = _oElement.name;
      if (_sEleName == '') {  // 有的button用id=, 有的用name=
        _sEleName = _sEleId;
      }
      // alert("is here 2 ? "  + _sEleType)
      if (_sEleType.toLowerCase() === "button" || _sEleType.toLowerCase() === "submit") {
        // 2012/05/24 Added by Joe 修复[全部]按钮出现在菜单的错误
        var reg = /(btn)(.*)(_All)/gi, sId = _oElement.id || _oElement.getAttribute("id") || "";
        if (!_oElement.disabled && !(_oElement.style.display == 'none')
            && !(_oElement.style.visibility == 'hidden') && !reg.test(sId)) {
          if (firstFlag) {
            _oChildDiv = document.createElement("SPAN");
            _oChildDiv.className = "separator";
            _oDiv.appendChild(_oChildDiv);
            firstFlag = false;
          }
          _sImg = _oElement.getElementsByTagName("img")[0];
          // alert(_sImg.outerHTML);
          if (_sImg && _sImg.getAttribute("src") != "") {
            _sImg = '<img src="' + _sImg.getAttribute("src") + '"> ';
          } else {
            _sImg = notImg;
          }
          try {
            _oChildDiv = document.createElement("DIV");
            _oChildDiv.id = "menu_" + _sEleName;
            _oChildDiv.className = "menuItem";
            _sTitle = _oElement.title;
            if (_sEleName == "btnQry") {
              //_sTitle = "查詢:[F2]";
              _sTitle = _oElement.getAttribute("defTitle");
            } else {
              _sTitle = _oElement.getAttribute("title");
            }
            // alert([_sImg,_sTitle]);
            _oChildDiv.innerHTML = _sImg + _sTitle;
            _oDiv.appendChild(_oChildDiv);
          } catch (ex) {
          }
        }
      }
    }

    // 查詢總筆數
    _oElement = jQuery('#btnTotalRec');
    if (_oElement[0] && !_oElement[0].disabled && !(_oElement[0].style.display == 'none')
        && !(_oElement[0].style.visibility == 'hidden')) {
      _oChildDiv = document.createElement("SPAN");
      _oChildDiv.className = "separator";
      _oDiv.appendChild(_oChildDiv);

      _oChildDiv = document.createElement("DIV");
      _oChildDiv.id = "menu_btnTotalRec";
      _oChildDiv.className = "menuItem";
      _oChildDiv.innerHTML = notImg + _oElement.attr('title');
      _oDiv.appendChild(_oChildDiv);
    }
    contextMenu.style.display = "block";
    contextMenu.style.height = _oDiv.offsetHeight;
    contextMenu.style.width = _oDiv.offsetWidth;

    var bgiframe = document.getElementById('bgiframe');
    bgiframe.style.height = _oDiv.offsetHeight;
    bgiframe.style.width = _oDiv.offsetWidth;
    bgiframe.style.top = _oDiv.offsetTop;
    bgiframe.style.left = _oDiv.offsetLeft;

    return contextMenu;
  }

  function AjaxHideMenu(e) {
    var contextMenu = document.getElementById("contextMenu");
    if (document.body.contains(contextMenu)) {
      document.body.removeChild(contextMenu);
      isMenuShow = false;
    }
  }
  window.AjaxHideMenu = AjaxHideMenu;

  function AjaxClickMenu(e) {
    e = e || event;
    var _sID = (e.target || e.srcElement).id;  // menu_btnAdd, ...

    AjaxHideMenu(e);

    if (_sID == "F5") {
      window.location.reload();
      return;
    } else if (_sID == "Info") {
      alert(window.location);
      return;
    } else if (_sID == 'FullSource') {
      AjaxFullSource();
      return;
    }

    _sID = _sID.substring(5);
    try {
      var obj = $("#" + _sID);
      if (!obj[0].disabled) {
        obj.click();
      }
    } catch (e) {
    }

    return true;
  }

  function AjaxSwitchMenu(e) {
    e = e || event;
    var el = e.target || e.srcElement;
    if (el.className == "menuItem") {
      el.className = "highlightItem";
    } else if (el.className == "highlightItem") {
      el.className = "menuItem";
    }
  }

  function AjaxFullSource() {
    var mywin = window.open("about:blank", "fullsource" + new Date().getTime(), "toolbar=no,location=no,menubar=yes,status=yes,scrollbars=yes,resizable=yes");
    var myDoc = mywin.document;
    myDoc.open("text/html");
    myDoc.write("<pre>" + document.documentElement.outerHTML.replace(/</g, "&lt;").replace(/>/g, "&gt;") + "</pre>");
    myDoc.close();
    myDoc.title = "Full Source for: " + window.location.href;
  }

  // when develop , we may want not to debug
  document.oncontextmenu = ajax_menu;
})(jQuery);
//===================================================================================================
