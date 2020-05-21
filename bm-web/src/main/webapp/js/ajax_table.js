/*
 * Copyright (c) 2012.@EMIS
 */

//$Id: ajax_table.js 9080 2017-07-26 10:04:21Z andy.he $
//====================================================================================================================================
//   ajax table object  .           by Robert
//------------------------------------------------------------------------------
//   it operates xml dom data and <table> object
//   and support the navigator functions such like  first,previous,next,prevPage,nextPage,lastPage....etc
//
//   now we have toolbar , masterTable ,detailTable, navigator . keyboard ..etc
//
//      the class coding style is that , if it is private function , it will have a  '__' prefix ,
//      which means I don't want you to call outside this script

//==============================================================================
// some debug flag

//var bDebugTableFlds = true;
var bDebugTableFlds = false;

//==============================================================================
// global keyboard utility class
var kbrd = new keyboard();
var currentTable; // the current main MasterTable class object
// util class for url format
var url = new emisUrl();
/**
 * 調試Array
 * @param prefix
 * @param ary
 */
function dumpArray(prefix, ary) {
  var txt = prefix + "(" + ary.length + ")=";
  var i;
  for (i = 0; i < ary.length; i++) {
    txt += (ary[i] + ",");
  }
  alert(txt);
}
/**
 * 查找數組中是否存在指定元素（相當于indexOf）
 * @param ary 要查找的數組
 * @param value 被查找的元素
 */
function arraySearch(ary, value) {
  for (var i = 0; i < ary.length; i++) {
    if (ary[i] == value) return i;
  }
  return -1;
}
/**
 * 取得Select物件選中元素的值，若無選中則返回空字串
 * @param SelectObj Select物件
 */
function getSelectValue(SelectObj) {
  if (SelectObj.selectedIndex != undefined) { // 若 select object 被 disable , index 會是 undefined
    if (SelectObj.selectedIndex >= 0) {
      return SelectObj.options[SelectObj.selectedIndex].value;
    }
  }
  return '';
}
/**
 * 給Select物件指定值
 * @param SelectObj
 * @param value
 */
function setSelectValue(SelectObj, value) {
  // 當為字符類型時，直接賦值進行綁定，若失敗則跑迴圈並且轉成數值進行比較，若再綁定不到則為這空選項
  //alert([SelectObj.nodeName, SelectObj.outerHTML])
  //SelectObj.value = value;
  //if (SelectObj.selectedIndex == -1) {
    var val;
    for (var i = 0; i < SelectObj.length; i++) {
      val = SelectObj.options[i].value;
      if (val.toLowerCase() === value.toLowerCase() ) {
          //||(val.replace(/\s/g,'').length>0 && +val === +value) ) {
        SelectObj.selectedIndex = i;
        return true;
      }
    }
    // 20140929 Joe Core_Fix : 修正無匹配項時無法清除上次值
    $(SelectObj).attr('selectedIndex', '-1').find("option:selected").removeAttr("selected");
    return false;
  //}
  //return true;
}
/**
 * 取得Radio物件選中元素的值，若無選中則返回空字串
 * @param RadioObj Radio物件
 */
function getRadioValue(RadioObj) {
  for (var i = 0, len = RadioObj.length; i < len; i++) {
    if (RadioObj[i].checked) {
      return RadioObj[i].value;
    }
  }
  return '';
}
/**
 * 給Radio物件指定值
 * @param RadioObj Radio物件
 * @param value
 */
function setRadioValue(RadioObj, value) {
  // Radio Object
  for (var i = 0, len = RadioObj.length; i < len; i++) {
    if (RadioObj[i].value == value) {
      RadioObj[i].checked = (value == '1' ? true : false);
      return true;
    }
  }
  return false;
}
/**
 * 給指定物件賦值（Input Select Span Div）
 * @param component
 * @param value
 */
function setcValue(component, value) {
  //try {
  if (component.length > 0 && /RADIO/i.test(component[0].getAttribute("type"))) {
    setRadioValue(component, value);
  } else if (component.nodeName == "INPUT" || component.nodeName == "TEXTAREA") {
    component.value = value;
    // type属性在IE为小写，在Chrome，FF则为CheckBox
    if (/CHECKBOX/i.test(component.getAttribute("type"))) {
      component.checked = (value == '1' ? true : false);
    }
  } else if ((component.nodeName == "SPAN") || (component.nodeName == "DIV")) {
    component.innerHTML = value;
  } else if (component.nodeName == "SELECT") {
    setSelectValue(component, value);
  }else if (component.nodeName == "IMG" || ( component.getAttribute("type") && component.getAttribute("type").toUpperCase() == "IMAGE")) {
    component.src = value;
  }
  //} catch (ex) {
  //	return false;
  //}
  return true;
}
/**
 * 取得物件的值（Input Select Span Div）
 * @param component
 */
function getcValue(component) {
  if (component.length > 0 && /RADIO/i.test(component[0].getAttribute("type"))) {
    return getRadioValue(component);
  }else if (component.nodeName == "INPUT" || component.nodeName == "TEXTAREA") {
    // 2012/11/01 Joe Fixed: type属性在IE为小写，在Chrome，FF则为CheckBox
    if (component.getAttribute("type") && component.getAttribute("type").toUpperCase() == "CHECKBOX") {
      return component.checked ? "1" : "0";
    } else {
      return component.value;
    }
  } else if ((component.nodeName == "SPAN") || (component.nodeName == "DIV")) {
    return component.innerHTML;
  } else  if (component.nodeName == "SELECT") {
    return getSelectValue(component);
  } else if (component.nodeName == "IMG"
      || (component.getAttribute("type") && component.getAttribute("type").toUpperCase() == "IMAGE")) {
    return component.src;
  }else{
    return "";
  }
}
/**
 * 創建新元素
 * @param parent 父元素，用于存放新的元素容器
 * @param nodeName 創建類型
 * @param value 元素值
 */
function CreateElement(parent, nodeName, value) {
  var n = document.createElement(nodeName);
  if (parent != null && parent != undefined) {
    parent.appendChild(n);
  }
  if (value != null && value != undefined) {
    setcell(n, value);
  }
  return n;

}
/**
 * 複製數組（可將Dom對象複製成Array）
 * @param target
 * @param source
 */
function copyArray(target, source) {
  var i;
  target.length = source.length;
  for (i = 0; i < source.length; i++) {
    target[i] = source[i];
  }

}
/**
 * 給Dom中指定名稱的元素賦值(遞迴執行多層級)
 * @param dom
 * @param FieldName
 * @param value
 */
function setDataFld(dom, FieldName, value) {
  __setDataFld(dom, FieldName, value);
  for (var i = 0; i < dom.childNodes.length; i++) {
    setDataFld(dom.childNodes[i], FieldName, value);
  }
}
/**
 * 給Dom中指定名稱的單一元素賦值（私用）
 * @param dom
 * @param FieldName
 * @param value
 * @private
 */
function __setDataFld(dom, FieldName, value) {
  // 2010/09/25 joe modify: 增加对Option取dataFld异常处理
  if (dom.attributes == null || dom.nodeName == "OPTION") return;
  if (dom.getAttribute("dataFld") == FieldName) {
    //alert('setdatafld:'+dom.nodeName);
    setcValue(dom, value);
  }
}

//============================================================================================
/**
 * 對Dom進行迴圈處理，回調輸入的Callback函數（接口）
 * @param toSearch
 * @param callback
 */
function AjaxXMLSearch(toSearch, callback) {
  __xmlSearch(toSearch, callback);
}
/**
 * 對Dom進行迴圈處理，回調輸入的Callback函數（私用實現，如有多層會遞迴執行）
 * @param dom
 * @param callback
 */
function __xmlSearch(dom, callback) {  
  callback(dom);

  if (dom && dom.childNodes && dom.childNodes.length > 0) {
    for (var i = 0; i < dom.childNodes.length; i++) {
      //alert( dom.nodeName + ":" + dom.getAttribute("dataFld") );
      __xmlSearch(dom.childNodes[i], callback);
    }
  }
}

//============================================================================================
//              建立 table 和 data field 關係
//============================================================================================
/**
 * 從 tbody 中拿取第一個 row, 並且將其記錄起來之後,刪除他（IE 的 Table 會自動加一個 tbody. !!!!）
 * @param TableObject
 * @param isMaster
 * @param databind
 */
function getTableIdArray(TableObject, isMaster, databind) {
  var tr = null;
  var toSearch = null;
  if (isMaster) { // MasterTable
    var body = TableObject.getElementsByTagName("tbody");
    var trs = null;
    trs = body[0].getElementsByTagName("tr");
    if (trs.length == 0) {
      return null;
    }
    tr = trs[0].cloneNode(true);
    toSearch = tr;
  } else { // DetlTable
    toSearch = TableObject;
  }
  searchDataFldObjs(toSearch, databind);
  return tr;
}
/**
 * 從傳入的toSearch中找出所有dataFld的物件，并記錄起來用于數據綁定
 * @param toSearch
 * @param databind
 */
function searchDataFldObjs(toSearch, databind) {
  databind.dataFlds.length = 0;
  databind.fld2Objs.length = 0;

  AjaxXMLSearch(toSearch, function (dom) {
    if (dom && dom.attributes != null) {
      try { // getAttribute 常常會有 error , 光是 attributes 判斷還不夠
        var datafld = dom.getAttribute("dataFld");

        if (datafld != null && datafld != "") {
          if (arraySearch(databind.dataFlds, datafld) == -1) {
            databind.dataFlds.push(datafld);
            var ary = new Array();
            ary.push(dom);
            // note, 有可能會有兩個 component 對到同一個 datafld , 所以存在 array 內
            databind.fld2Objs[ datafld ] = ary;
            databind.fld2Objs.push(ary);
            //this.UiObjectArray.push(ary);
          } else {
            var ary = databind.fld2Objs[ datafld ];
            ary.push(dom);
          }
        }
      } catch (ignore) {
        //alert(ignore);
      }
    }
  });
}


//==========================================================================================================
/**
 * 模擬 IE data binding ,目前只做了 input 的 onblur , 和 checkbox 的 onclick
 * @param toSearch
 * @param tableObject
 */
function register_master_blur(toSearch, tableObject) {
  var databind = new DataBinding();
  searchDataFldObjs(toSearch, databind);
  databind.registerMasterOnBlur(tableObject);
  databind = null;
}

/**
 * 為了模擬連續新增時的 data binding 的寫回
 * this is automatically registered to &lt;input tag&gt; of detail table
 */
function ajax_detl_onblur() {
  if (this.__saved_onblur != undefined && typeof(this.__saved_onblur) == "function") {
    this.__saved_onblur();
  }
  this.__srctbl.loadFromDetl(this);
}
/**
 * MasterTable中Input物件的Onblur事件處理
 */
function ajax_master_input_onblur() {
  if (this.__saved_onblur != undefined && typeof(this.__saved_onblur) == "function") {
    this.__saved_onblur();
  }
  //this.__srctbl.loadFromMaster(this);
  load2Master(this.__srctbl, this);
}
/**
 * MasterTable中Checkbox物件的Onblur事件處理
 */
function ajax_master_checkbox_onclick() {
  if (this.__saved_onclick != undefined && typeof(this.__saved_onclick) == "function") {
    this.__saved_onclick();
  }
  load2Master(this.__srctbl, this);
}
/**
 * MasterTable的Onchange事件處理
 */
function ajax_master_onchange() {
  if (this.__saved_onchange != undefined && typeof(this.__saved_onchange) == "function") {
    this.__saved_onchange();
  }
  //this.__srctbl.loadFromMaster(this);
  load2Master(this.__srctbl, this);
}
/**
 * 把物件的值回寫到XmlDom
 * @param tableobj
 * @param src
 */
function load2Master(tableobj, src) {
  var fld = src.getAttribute("dataFld");
  var value = getcValue(src);

  while (src.parentNode) {
    src = src.parentNode;
    if (src.rowIndex != undefined) {
      // tableobj.setCurrPage(src.rowIndex - 1, fld, value);
      tableobj.setCurrPage(src.rowIndex - tableobj.thd.rows.length, fld, value);
      //alert( "(" + src.rowIndex + ") " +  fld + "="+value);
      break;
    }
  }
}

//==================================================================================================================
/*(function ($) {
  $.extend($.expr[':'], {
    focusable: function (element) {
      var nodeName = (element.nodeName || "").toLowerCase(),
          tabIndex = nodeName ? $.attr(element, 'tabindex') : -1;
      //return (/input|select|textarea|button|object/.test(nodeName)
      return (/input|select|textarea|object|button|img/.test(nodeName)
          ? !element.disabled && (tabIndex - 0) >= 0
          : 'a' == nodeName || 'area' == nodeName
          ? element.href || !isNaN(tabIndex)
          : !isNaN(tabIndex))
        // the element and all of its ancestors must be visible
        // the browser may report that the area is hidden
          && !$(element)['area' == nodeName ? 'parents' : 'closest'](':hidden').length;
    }
  });
})(jQuery);*/
(function ($) {
  // Copy from jQuery UI v1.9
  // support: jQuery <1.8
  if (!$.fn.addBack) {
    $.fn.addBack = function (selector) {
      return this.add(selector == null ?
        this.prevObject : this.prevObject.filter(selector)
      );
    };
  }
  function focusable(element, isTabIndexNotNaN) {
    var map, mapName, img,
      nodeName = (element.nodeName || "").toLowerCase();
    if ("area" === nodeName) {
      map = element.parentNode;
      mapName = map.name;
      if (!element.href || !mapName || map.nodeName.toLowerCase() !== "map") {
        return false;
      }
      img = $("img[usemap=#" + mapName + "]")[0];
      return !!img && visible(img);
    }
    return ( /input|select|textarea|button|object|img/.test(nodeName) ?
      !element.disabled :
      "a" === nodeName ?
        element.href || isTabIndexNotNaN :
        isTabIndexNotNaN) &&
      // the element and all of its ancestors must be visible
      visible(element);
  }

  function visible(element) {
    return $.expr.filters.visible(element) && !$(element).parents().addBack().filter(function () {
      return $.css(this, "visibility") === "hidden";
    }).length;
  }

  $.extend($.expr[ ":" ], {
    focusable: function (element) {
      return focusable(element, !isNaN(element.nodeName && $.attr(element, "tabindex")));
    },

    tabable: function (element) {
      var tabIndex = element.nodeName ? $.attr(element, 'tabindex') : -1,
        isTabIndexNaN = isNaN(tabIndex);
      return ( isTabIndexNaN || tabIndex >= 0 ) && focusable(element, !isTabIndexNaN);
    }
  });
})(jQuery);
/**
 * 把回车转为Tab
 * @return {boolean}
 */
function convertEnterToTab(e){
  if ((e.keyCode || e.which) == 13) {
    var focusables = jQuery(":tabable");
    var current = (e.srcElement || e.target);
    var currentIndex = focusables.index(current),
        next = focusables.eq(currentIndex + 1).length ? focusables.eq(currentIndex + 1) : focusables.eq(0);
    // TODO：Chrome 不会触发change事件，暂时改为手动触发
   /* if(/chrome/i.test(navigator.userAgent))
      jQuery(current).trigger("change");*/

    // 不能用jQuery物件去focus，否则不会触发blur事件
    //next.focus();
    try{
      setTimeout(function(){next.select();},1);
    }catch(e){}
    //2013/08/12 Modify By Jim 把focus挪到select之后，防止按ENTER之後，需要落回原焦點時回不去；BUT某些情況下還是會做2次驗證
    // 不能用jQuery物件去focus，否则不会触发blur和change事件
    jQuery(next)[0].focus();
    if (e.stopPropagation) {
      e.stopPropagation();
    }

    if (e.preventDefault) {
      e.preventDefault();
    }

    e.cancelBubble = true;
    e.returnValue = false;
    return false;
  }
  return true;
}
/**
 * 註冊OnkeyDown事件
 */
function ajax_document_on_keydown(e) {
  return kbrd.processKey(e);
}
/**
 * 註冊onClick事件
 */
function ajax_row_onclick() {
  return this.__masterTbl.click(this);
}

//=============================================================================================================
//                 start of declare DataBinding object
//=============================================================================================================
/**
 * 用來記錄 某個節點中 Dom 對於 data field 和 objects 的對應關係
 * <br>dataFlds 是所有的 DafaFld 的 collection
 * <br>fld2Objs 是某個 field name 對應哪些 object
 * <br>一個 DataFld 可能會對到好幾個 object ,
 * <br>譬如 input "S_NO" 和 Span "spaS_NO" 和 Select "selS_NO" 都是用 DataFld = "S_NO"
 * @class This is the basic Databinding class.
 */
function DataBinding() {
  /**
   * DataFld 數組
   * @param MasterObject
   */
  this.dataFlds = new Array(0);
  /**
   * 每個DataFld對應綁定的物件數組
   * @param MasterObject
   */
  this.fld2Objs = new Array(0);
  /**
   * 註冊MasterTable中Inupt物件的onBlur事件
   * @param MasterObject
   */
  this.registerMasterOnBlur = function(MasterObject) {
    for (var i = 0; i < this.fld2Objs.length; i++) {
      ary = this.fld2Objs[i];
      for (var j = 0; j < ary.length; j++) {
        var component = ary[j];

        if ((component.nodeName == "INPUT")) {
          component.__saved_onblur = component.onblur;
          component.__srctbl = MasterObject;
          // 2012/11/06 Joe commit 修正Chrome下Checkbox无法触发blur事件，改用click代替
          // Note: 除了Input以外的Component暂时不处理
          if (component.type && (component.type.toUpperCase() == "CHECKBOX" || component.type.toUpperCase() == "RADIO")) {
            jQuery(component).click(function (e) {
              ajax_master_input_onblur.call(this);
            });
          } else {
            // 2012/11/13 修正onblur被多次触发问题
            /*jQuery(component).blur(function (e) {
              ajax_master_input_onblur.call(this);
            });*/
            component.onblur = ajax_master_input_onblur;
          }
        }
      }
    }
  }
  /**
   * 註冊DetailTable中Inupt物件的onBlur事件
   * @param MasterObject
   */
  this.registerDetlOnBlur = function(MasterObject) {
    //alert('registerDetlOnBlur:'+this.fld2Objs.length);
    for (var i = 0; i < this.fld2Objs.length; i++) {
      ary = this.fld2Objs[i];
      for (var j = 0; j < ary.length; j++) {
        var component = ary[j];

        if ((component.nodeName == "INPUT")) {
          component.__saved_onblur = component.onblur;
          component.__srctbl = MasterObject;
          component.onblur = ajax_detl_onblur;
          continue;
        }
      }
    }
  }
  /**
   * 從XmlDom把數據綁定到UI
   * @param masterObject
   * @param rowIndex
   */
  this.loadFromMaster = function (masterObject, rowIndex) {
    var fld,objs;
    for (var j = 0; j < this.dataFlds.length; j++) {
      fld = this.dataFlds[j];
      // 2010/08/17 joe modify: 直接取当前行来处理，而非取fld2Objs[fld],它是最原始的记录非真实资料行
      setDataFld(masterObject.tbdy.rows[rowIndex % masterObject.datapagesize], fld, masterObject.get(rowIndex, fld));
     /* objs = this.fld2Objs[fld];
      for (var k = 0; k < objs.length; k++) {
        setcValue(objs[k], masterObject.get(rowIndex, fld));
      }*/
    }
  }
  /**
   * 取得指定元素的值
   * @param field
   */
  this.getValue = function (field) {
    var objs,obj,k,type,nodeName;
    objs = this.fld2Objs[field];
    // 對個別 dataFld 找出最可能的 component 取值
    // 經過測試 nodeName 回傳固定是大寫 , type 回傳值固定是小寫
    for (k = 0; k < objs.length; k++) {
      obj = objs[k];
      type = obj.getAttribute("type");
      nodeName = obj.nodeName;
      if (nodeName == "INPUT" && type != "hidden") {
        return getcValue(obj);
      } else if (nodeName == "TEXTAREA") {
        return getcValue(obj);
      } else if (nodeName == "SELECT") {
        return getcValue(obj);
      } else if (nodeName == "CHECKBOX") {
        return getcValue(obj);
      } else if (nodeName == "SPAN" || nodeName == "DIV") {
        return getcValue(obj);
      } else if (nodeName == "INPUT" && type == "hidden") {
        return getcValue(obj);
      } else if (nodeName == "IMG") {
        return getcValue(obj);
      }
    }
    return "";
  }
}


//------------------------------------------------------------------------------
//                 start of declare Master Table object
//------------------------------------------------------------------------------

/*
MasterTable class
comment : Master table 內含 toolbar, detailTable ,  , record span , page span 等等 component
主是用來管理 Rows ( from SQL result)
masterTable.setMode 會分別去 trigger 每個 component  的 setMode
masterTable.

properties:
  id:
  tbl:
  tbdy:
  LastDataXml:
  UiRowCount:
  detlTable:
Function:
  public:
      query
      lastclick
      delrow
      go
      search
  protected:
      loadXML
      lastclick
 */
/**
 * 用來管理數據庫查詢出來的Rows，幾乎所有的動作都從這個類發起
 * @class This is the basic MasterTable class. 
 * @param TableId 
 * @param varname 實例后的變數名稱，產生Navigator物件時事件綁定使用
 */
function MasterTable(TableId, varname) {
  /*
   * 因為有些自動產生的 script 需要 table object 變數名稱
   */
  this.varname = varname;
  /*
   * 全局變數，用于記錄當前MasterTable物件
   */
  currentTable = this;
  this.isXmlDataLoaded = false; //数据是否加载完成，用于表头检查表身状态，控制按钮
  //=========== page parameters===================================
  this.currentPage = 0; //當前頁碼 0 ~ ( totalPageCount -1 )
  this.currentPageRowCount = 0; //當前頁資料總筆數
  this.currRowIndex = -1; // the absolute row index , 0 ~ (totalRowCount - )
  this.totalRowCount = 0;
  this.totalPageCount = 0;
  this.UiRowCount = -1; // UI RowCount , always 小於 datapagesize , 跟 page size 會有關係
  this.datapagesize = 100; // a default sze
  this.isQueryAction = false;  // 2010/02/27 Joe.yao add: 首次進入頁面之查詢標識，避免查無資料時顯示【無任何查詢資料 ！】
  // this is added only by addRow and decrease by deleteLastRow
  // 這個是用來控制連續新增時,是否需要 reload
  this.tempRowCount = 0;

  // field relative
  this.databind = new DataBinding();

  this.domIds = new Array(); // dom xml 完整的 id list
  this.IdMatch = new Array(); // a match with databind.dataFlds and domIds , 用 index, 可拿 databind.dataFlds 的 index
  this.field2idx = new Array(); // 傳入 fieldname 可拿到 Dom 的field index


  // getElementsByTagName 傳回的 array 是 readonly , 這樣做 Add 時會沒辦法做, 所以改成用 Array
  this.rows = new Array();

  //========================================================================
  //  some ui control
  this.DisableComponentWithMode = new Array();

  //============ some callback==============================================
  // called when xml data is loaded , 這個每次 Query 之後只會被叫用一次
  this.XmlLoadReady = null;

  // called when page parameters change...., you can register it , if you need do something after the normal ui update
  // 這個是每次參數有變動時都會被叫用 ,譬如 first,next,prev,nextPage, set datapagesize ....etc
  this.updateUiCallback = null;

  // 这个函数仅会在页面数据被重新加载时才会调用
  this.pageLoadedCallback = null;

  // ===========some ui component ===========================================
  this.span_curr_record = null;
  this.span_ttl_records = null;
  this.span_curr_page = null;
  this.span_ttl_pages = null;
  this.span_pagesize = null;
  this.navigator = null;
  this.span_mode = null;
  this.toolbar = null;
  this.detlTable = null;
  this.useZebra = true;   //表格是否使用斑馬線分隔
  this.bMoveLast = false;  // 数据加载完成后是否移到最后一笔

  // edit mode, there are three kinds of edit mode , which are UPD,ADD and ''  (empty)
  this.sMode = "";
  //this.sDefaultMode = "";

  this.tblobjs = new Array(0); // we must keep the first object we got
  this.id = TableId;
  this.tbl = null;
  this.tbdy = null;//this.tbl.getElementsByTagName("tbody")[0];
  this.tr = null;
  this.thd = null;

  this.bForceTriggerClick = false;  // 用于连续新增时触发回调事件标识
  this.orgiLastRowForDowrec = null; // 用于记录每次查询返回XML未排序前最后一笔记录，便于前端排序后不影响向下查询的正确性

  var _AllRowFlag = 99999999; // 用於處理表頭時顯示全部
  /**
   * 用來處理載入數據前的初始化動作
   * 用來動態切換 table, 譬如批次更新時,這樣可以不用換頁
   * @param sTableId
   */
  this.loadTable = function (sTableId) {
    this.id = sTableId;
    this.tbl = document.getElementById(sTableId);
    if (this.tbl == null) {
      // we create a invisible table
      this.tbl = document.createElement("TABLE");
      this.tbl.setAttribute('dataPageSize',_AllRowFlag); // 當為表頭時無列表即為顯示全部
      var head = CreateElement(this.tbl, "THEAD");
      var tr = CreateElement(head, "TR");
      var td = CreateElement(head, "TD");
      var n = document.createTextNode("__xxx");
      td.appendChild(n);
      var body = CreateElement(this.tbl, "TBODY");
      tr = CreateElement(body, "TR");
      td = CreateElement(tr, "TD");
      var span = CreateElement(td, "span");
      span.setAttribute("dataFld", "__xxx"); // 注意 , IE 真正的 attribute 是 'dataFld' 這邊大小寫不對是不行的
      span.setAttribute("name", "__xxx"); // 注意 , IE 真正的 attribute 是 'dataFld' 這邊大小寫不對是不行的
      this.tbl.style.display="none";
      this.tbl.id = sTableId;
      //dumpXML(this.tbl);
      document.body.appendChild(this.tbl);

      //this.tbl = document.getElementById(sTableId);
    }


    // 後續的 table 操作,會把 tbody 的內容給刪掉,所以一定要先 clone 存起來
    // 而且會有切換 table 定義的 case , 所以這邊用 tblobjs array 存起來
    if (this.tblobjs[ sTableId ] == undefined || this.tblobjs[ sTableId ] == null) {
      this.tblobjs[ sTableId ] = this.tbl.cloneNode(true);
    } else {
      this.tbl = this.tblobjs[ sTableId ].cloneNode(true);
    }

    // 自動計算 datapagesize,還沒有好方法
    //var rects = this.tbl.getClientRects();
    //var rect = rects[0];
    //var height = rect.bottom - rect.top;
    //alert(height);
    //this.datapagesize = Math.floor(height / 20);

    /*// 從 table 的 datapagesize 設定
    var dpgsz = this.tbl.getAttribute("dataPageSize");
    if (! emisEmpty(dpgsz)) {
      this.datapagesize = dpgsz - 0;
    }*/

    // 注意,這個 tbdy 要抓真正畫面上的,不能使用 clone 的
    this.tbdy = document.getElementById(sTableId).getElementsByTagName("tbody")[0];
    // 注意，這里把Head取回來是為了后面表身定位時用來計算應減行數
    this.thd = document.getElementById(sTableId).getElementsByTagName("thead")[0];

    //alert(this.tbdy);
    this.tr = null;


    this.databind = new DataBinding();
    this.domIds = new Array(0);
    this.IdMatch = new Array(0);
    this.field2idx = new Array(0);


    //alert('x');
  }

  //   the init code
  this.loadTable(TableId); // 這行必須放在 宣告之後才能使用

  // 從 table 的 datapagesize 設定
  var dpgsz = this.tbl.getAttribute("dataPageSize");
  if (! emisEmpty(dpgsz)) {
    this.datapagesize = dpgsz - 0;
  }

  /**
   * 查詢并加載數據到畫面顯示
   * Query will set table to default mode
   * @param sAction
   */
  this.query = function(sAction, isQueryOnly) {
    //this.setMode(this.sDefaultMode);
    var oAct = formobj('act') || formobj('ACT');
    if(typeof oAct == "undefined"){
      alert(jQuery.getMessage('act_object_is_lost'));  //缺少ACT隱藏对象!
      return false;
    }
    if(sAction && sAction.replace(/\s/g, "").length > 0){
      oAct.value = sAction;
    }
    if(oAct.value.replace(/\s/g, "").length == 0){
      alert(jQuery.getMessage('act_is_require'));  //ACT不能为空值
      return false;
    }
    if (do_ajax_request()) {
      /*if (!my_check_error(request.responseXML)) {
        this.loadXML(request.responseXML);
      }*/
      var xmlDom = parseXmlDom(request);
      if (!my_check_error(xmlDom)) {
        if (isQueryOnly) {
          this.LastDataXml = xmlDom;
        } else {
          this.loadXML(xmlDom);
        }
      }
    }
  }

  /**
   * 查詢數據但不显示到畫面
   * Query will set table to default mode
   * @param sAction
   */
  this.queryOnly = function(sAction) {
    this.query(sAction, true);
    ////this.setMode(this.sDefaultMode);
    //if(sAction && sAction.replace(/\s/g, "").length > 0){
    //  formobj('act').value = sAction;
    //}
    //if(formobj('act').value.replace(/\s/g, "").length == 0){
    //  alert(jQuery.getMessage('act_is_require'));  //ACT不能为空值
    //  return false;
    //}
    //if (do_ajax_request()) {
    //  /*if (!my_check_error(request.responseXML)) {
    //   this.loadXML(request.responseXML);
    //   }*/
    //  var xmlDom = parseXmlDom(request);
    //  if (!my_check_error(xmlDom)) {
    //    this.LastDataXml = xmlDom;
    //  }
    //}
  };
  /**
   * 用來建立公用插件，如：record span , page record span , page span, mode span....etc , 沒寫就不會建立
   * @param navigatorStyle
   */
  this.commonInit = function (navigatorStyle) {
    this.createPageRecordSpan('pagerecordspan');
    this.createRecordSpan("recordspan");
    this.createPageSpan("pagespan");
    this.createModeSpan("modespan");

    if (!emisEmpty(document.getElementById("idNavigator"))) {
      // 2010/03/05 Joe add: styletype 為增設自定義屬性，1 - 有上一筆下一筆, 2 - 有上一頁下一頁
      if(emisEmpty(navigatorStyle)){
        navigatorStyle = document.getElementById("idNavigator").getAttribute("styletype")||"2";
//        navigatorStyle = (navigatorStyle == '4'? '1' : '2');
      }
      this.createNavigator("idNavigator", navigatorStyle);
      // 2012/05/22 Joe 去掉导航箭头上的右键看源码功能，如需要请用(Ctrl+Alt+F),再点右键选菜单中的【FullSource】
      /*//2010/03/02 Joe add: 保留原來導航欄中間位置右鍵顯示IE原有右鍵菜單
      jQuery('#idNavigator')[0].oncontextmenu = function(){
        window.event.cancelBubble = true; // don't bother bubbling to the document
        return true;
      };*/
    }
  }

  /**
   * 加載rows并顯示到畫面
   * this is called by Ajax callback architecture ( from do_ajax_request ...)
   * @param rows
   */
  this.loadRows = function (rows, isEmpty) {
    // because the xmlDom return rows is read only array , we need to copy it to our array.
    this.rows = rows;
    if (this.tbl.getAttribute('dataPageSize') == _AllRowFlag)
      this.datapagesize = this.rows.length;  // 當為999999時表示為表頭顯示全部

    if (this.databind.dataFlds.length == 0) {
      this.__getDomIdArray();

      var newTr = getTableIdArray(this.tbl, true, this.databind);
      if (newTr != null) {
        this.tr = newTr;
      } else {
        // 2013/05/15 Joe Fixed: 当表头为全新无数据单据时，表身的 tbl中的body已被删除，所以需重获取原始的内容
        this.tbl = this.tblobjs[ this.id ].cloneNode(true);
        newTr = getTableIdArray(this.tbl, true, this.databind);
        this.tr = newTr;
      }

      this.__createFieldMapping();
    }

    if (isEmpty == "true") {
      if (this.detlTable != null) { // here we need to do a special load for rowcount == 0
        this.detlTable.load(this.rows[0]);
      }
      this.rows.length = 0;
    }
    // 记录查询回来的最后一笔记录，用于取向下查询条件
    if (this.rows.length > 0)
      this.orgiLastRowForDowrec = this.rows[this.rows.length - 1];

    if (this.lastOrderAction) {
      // this.__makeDomOrder(this.lastOrderAction.fieldName,this.lastOrderAction.fieldType,this.lastOrderAction.orderType);
      this.__makeDomOrder(this.lastOrderAction);
    }

    // loadXML 產生 callback 順序固定為
    //  __callXmlLoadReady ,  updateUiCallback , 這點很重要
    // 所以才切割出 triggerclick , 和 loadCurrentPage 的 bTriggerClick 參數
    this.__calculatePosition();
    // 2013/10/10 Joe 为了配合在加载页面之前先呼叫 XmlLoadReady 以利于提前修改数据再显示
    this.__callXmlLoadReady();
    this.__loadCurrentPage(true, false);
    this.__triggerClick();
  };
  /**
   * 加載XmlDom并顯示到畫面
   * this is called by Ajax callback architecture ( from do_ajax_request ...)
   * @param xmlDom
   */
  this.loadXML = function (xmlDom) {
    this.LastDataXml = xmlDom;

    // because the xmlDom return rows is read only array , we need to copy it to our array.
    var dom_rows = xmlDom.getElementsByTagName("_r");    
    copyArray(this.rows, dom_rows);
    if(this.tbl.getAttribute('dataPageSize') == _AllRowFlag)
         this.datapagesize = this.rows.length;  // 當為999999時表示為表頭顯示全部

    if (this.databind.dataFlds.length == 0) {
      this.__getDomIdArray();

      var newTr = getTableIdArray(this.tbl, true, this.databind);
      if (newTr != null) {
        this.tr = newTr;
      } else {
        // 2013/05/15 Joe Fixed: 当表头为全新无数据单据时，表身的 tbl中的body已被删除，所以需重获取原始的内容
        this.tbl = this.tblobjs[ this.id ].cloneNode(true);
        newTr = getTableIdArray(this.tbl, true, this.databind);
        this.tr = newTr;
      }

      this.__createFieldMapping();
    }


    var root = xmlDom.getElementsByTagName("data");
    var isEmpty = root[0].getAttribute("empty");

    if (isEmpty == "true") {      
      if (this.detlTable != null) { // here we need to do a special load for rowcount == 0
        this.detlTable.load(this.rows[0]);
      }
      this.rows.length = 0;
    }
    // 记录查询回来的最后一笔记录，用于取向下查询条件
    if(this.rows.length > 0)
      this.orgiLastRowForDowrec = this.rows[this.rows.length-1];

    if(this.lastOrderAction){      
      // this.__makeDomOrder(this.lastOrderAction.fieldName,this.lastOrderAction.fieldType,this.lastOrderAction.orderType);
      this.__makeDomOrder(this.lastOrderAction);
    }

    // loadXML 產生 callback 順序固定為
    //  __callXmlLoadReady ,  updateUiCallback , 這點很重要
    // 所以才切割出 triggerclick , 和 loadCurrentPage 的 bTriggerClick 參數
    this.__calculatePosition();
    // 2013/10/10 Joe 为了配合在加载页面之前先呼叫 XmlLoadReady 以利于提前修改数据再显示
    this.__callXmlLoadReady();
    this.__loadCurrentPage(true, false);
    this.__triggerClick();
  }

  /**
   * 根據 dataBind.dataFlds 來產生 domIds, 通常是特殊操作會需要,譬如批次輸入時,沒有 xml ,需要自己產生
    */
  this.createDataFlds = function () {
    this.tbdy = document.getElementById(this.id).getElementsByTagName("tbody")[0];
    var newTr = getTableIdArray(this.tbl, true, this.databind);
    copyArray(this.domIds, this.databind.dataFlds);

    if (newTr != null) {
      this.tr = newTr;
    }
    this.__createFieldMapping();
  }

  /**
   * XmlDom 加載完成后進行回調處理，如未指定回調函數則不處理
   */
  this.__callXmlLoadReady = function() {
    if (this.XmlLoadReady && typeof(this.XmlLoadReady) == "function") {
      this.XmlLoadReady(this);
    }
  }

  /**
   * 進行頁數，筆數等邊界計算（做 index 的 boundary check）
   */
  this.__calculatePosition = function() {
    this.totalRowCount = this.rows.length;
    this.totalPageCount = Math.floor((this.totalRowCount + (this.datapagesize - 1)) / this.datapagesize);

    if (this.currRowIndex == -1) { // the first time to load
      this.__UpdateVarByRowIndex(0);
    } else {    
      // otherwise , we want to keep it the origianl one;
      if (this.currRowIndex >= this.totalRowCount) {
        this.currRowIndex = this.totalRowCount - 1;
      }
      this.__UpdateVarByRowIndex(this.currRowIndex);
    }
  }

  /**
   * 進行頁數，筆數等邊界計算并載入當前頁數據
   * load page 主要多了一個 boundary 計算的 check (__calculatePosition)
   */
  this.__loadPage = function () {
    this.__calculatePosition();

    this.__loadCurrentPage(true);
  }
  /**
   * 從 server 傳回的 xml 產生完整的 id list
   */
  this.__getDomIdArray = function () {
    var i;
    var cell;
    var row = this.rows[0];
    this.domIds.length = row.childNodes.length;
    for (i = 0; i < row.childNodes.length; i++) {
      cell = row.childNodes[i];
      this.domIds[i] = cell.nodeName;
    }
  }
  /**
   * 建立各種轉換的 mapping cache
   */
  this.__createFieldMapping = function () {
    var i,j;
    this.IdMatch.length = this.databind.dataFlds.length;
    for (i = 0; i < this.databind.dataFlds.length; i++) {
      for (j = 0; j < this.domIds.length; j++) {
        if (this.databind.dataFlds[i] == this.domIds[j]) {
          this.IdMatch[i] = j;
        }
      }
    }

    this.field2idx.length = this.domIds.length;
    for (i = 0; i < this.domIds.length; i++) {
      this.field2idx[this.domIds[i]] = i;
    }    
    if (bDebugTableFlds) {
      dumpArray('domids', this.domIds);
      //dumpArray( 'idMatch',this.IdMatch);
      dumpArray('field2idx', this.field2idx);
    }
  }
  /**
   * 主要是 check 跟 page 有關的參數,避免有怪怪的參數
   */
  this.__verifyPageParameter = function () {
    if (this.currentPage >= this.totalPageCount) {
      this.currentPage = this.totalPageCount - 1;
    }
    if (this.currRowIndex >= this.totalRowCount) {
      this.__UpdateVarByRowIndex(this.totalRowCount - 1);
    }
//    if( this.UiRowCount >= this.currentPageRowCount  ) {
//      this.__setUIRowCount(this.currentPageRowCount-1);
//    }
  }
  /**
   * onclick 會先決定 UI 上的 UiRowCount,
   * 再用 currentPage 與 datapagesize 和 UiRowCount  更新 currRowIndex
   * 所以用 __setUIRowCount 來確保這兩各變數的一致性
   * @param index
   */
  this.__setUIRowCount = function (index) {
    this.UiRowCount = index;
    this.currRowIndex = this.datapagesize * this.currentPage + this.UiRowCount;
  }

  /**
   * 參數當前行索引值重算并更新分頁相關變數
   * @param rowidx
   */
  this.__UpdateVarByRowIndex = function (rowidx) {
    if (this.totalRowCount == 0) {
      this.currRowIndex = 0;
      this.currentPage = 0;
      this.UiRowCount = 0;
      this.currentPageRowCount = 0;
      this.totalPageCount = 0;
      return;
    }

    this.currRowIndex = rowidx;
    this.currentPage = Math.floor(this.currRowIndex / this.datapagesize);
    this.UiRowCount = this.currRowIndex - this.currentPage * this.datapagesize;
    /*
     if( this.currentPage < this.totalPageCount -1 )
     this.currentPageRowCount = Math.min(this.datapagesize,this.totalRowCount);
     else
     this.currentPageRowCount = this.totalRowCount % this.datapagesize;
     */
    if (this.datapagesize > 0) {
      //this.currentPageRowCount = Math.ceil(this.totalRowCount / this.datapagesize);
      if (this.currentPage == this.totalPageCount - 1){ // last page
        // 正好整除時，導致 currentPageRowCount 為 0 錯誤
        //this.currentPageRowCount = this.totalRowCount % this.datapagesize;
        var cont = this.totalRowCount % this.datapagesize;
        this.currentPageRowCount = (cont == 0 ? this.datapagesize : cont);
      }else
        this.currentPageRowCount = this.datapagesize;
    } else {
      this.currentPageRowCount = 0;
    }   
  }
  /**
   * 從XMLDom取數據產生一筆資料并返回
   * @param RowCount
   * @param RowObject
   * @param idArray
   * @param match
   */
  this.__createUIRow = function(RowCount, RowObject, idArray, match) {
    // this clone is somehow not very precise, we better do change after the clone
    var newTr = this.tr.cloneNode(true);

    if (newTr.height == "") {
      newTr.height = "20px";
    }
    var that = this; // 缓存MasterTable对象
    AjaxXMLSearch(newTr, function(dom) {
      var cell, setv;
      try {
        var fld = dom.getAttribute("dataFld");
        var parser = dom.getAttribute("dataParser");
        if (fld != null && fld != "" && fld != undefined) {
          // looking for it
          var idx = match[fld];
          if (idx != undefined) {
            cell = RowObject.childNodes[idx];
            setv = cellvalue(cell);
            // 当有附加解析器时呼叫获取返回处理结果再写入
            if (!!parser && typeof window[parser] == "function") {
              setv = window[parser]({
                /*当前对象*/
                elm: dom,
                /*当前字段值*/
                val: setv,
                /*当前行*/
                tr: newTr,
                /*当前数据集*/
                ds: that,
                /*当前行索引*/
                idx: RowCount
              });
            }
            setcValue(dom, setv);
          }
          //setv = String.fromCharCode(160); // nbsp
        }
        // 当有附加解析器时呼叫获取返回处理结果再写入
        else if (!!parser && typeof window[parser] == "function") {
          setv = window[parser]({
            /*当前对象*/
            elm: dom,
            /*当前字段值*/
            val: setv,
            /*当前行*/
            tr: newTr,
            /*当前数据集*/
            ds: that,
            /*当前行索引*/
            idx: RowCount
          });
          setcValue(dom, setv);
        }
      } catch (ignore) {
      }
    });

    register_master_blur(newTr, this);

    //this.applyDisable( this.sMode ,newTr );
    return newTr;
  }
  /**
   * 加載當前頁數據
   * @param reConstructUI 是否重新產生UI
   * @param bTriggerClick 是否觸發點擊事件
   */
  this.__loadCurrentPage = function (reConstructUI, bTriggerClick) {
    // append all by tagname & td's id
    var i,j,tr,row;
	
    this.__verifyPageParameter();

    if (reConstructUI == true) {
      // 20100716 Joe Mark 解決導致jQuery失效問題
      // 插入加載提示信息
      //jQuery(this.tbl).before("<span id='loading'>Loading....</span>").remove();
      // 20100828 Joe Add 移除所有元素的事件后再删除元素，避免事件泄漏
      jQuery('*', this.tbdy).unbind();
      // 移除舊資料
      while ( this.tbdy.firstChild ) {
        this.tbdy.removeChild( this.tbdy.firstChild );
      }

      if (this.totalRowCount > 0) {
        var startIdx = this.currentPage * this.datapagesize;

        j = startIdx + this.datapagesize;
        // 產生臨時碎片文檔，最后才一次加到畫面
        var oFragment = document.createDocumentFragment();
        /*
                  Track+[18716] dana 2011/12/01 修正多笔式搜索框不能重复搜索问题.
                  整批修改时,发现原来浏览页面第一页的资料并没用删除,导致搜索异常
              */
        //jQuery("tr[_bshow=true]").removeAttr("_bshow");

        for (var count=0,i = startIdx; (i < this.totalRowCount) && (i < j); i++,count++) {
          row = this.rows[i];
          tr = this.__createUIRow(i, row, this.domIds, this.field2idx);
          tr.__masterTbl = this;
          /*tr.onclick = ajax_row_onclick;
          tr.onmousedown = ajax_row_onclick;*/
          jQuery(tr).click(ajax_row_onclick).bind('mousedown', ajax_row_onclick)//.attr("_bshow",true);
              // 2012/12/27 增加双击进入修改功能
              .dblclick(function(e){
                //jQuery("button[name=btnUpd]").trigger("click");
                var $btnUpd = jQuery("button[name=btnUpd]");
                if ($btnUpd.attr("disabled") == false && $btnUpd.is(":visible"))
                  $btnUpd.trigger("click");
              });
          if(this.useZebra)
            tr.className += (count % 2 == 0 ? " even " : " odd ");
          oFragment.appendChild(tr);
        }
        this.tbdy.appendChild(oFragment);        
        delete oFragment; // 刪除臨時碎片文檔，釋放資源
      }
      // 20100716 Joe Mark 解決導致jQuery失效問題
      // 移除加載提示信息
      //jQuery('#loading').before(this.tbl).remove();
      // 2010/08/27 Joe add: 增加加载完成回调函数，通常用于表身动态数据绑定事件  
      if (this.pageLoadedCallback && typeof( this.pageLoadedCallback ) == "function") {
        this.pageLoadedCallback(this);
      }
    }

    if (bTriggerClick != false) {
      this.__triggerClick();
    }
  }

  /**
   * 觸發點擊事件前邏輯檢查
   * 當add row 最後會需要觸發一次 click, 但是在 loadPage-> triggerClick  時
   * 有判斷如果是 ADD 或 UPD 就不觸發,在  ajax_mtn.js 中,AjaxMtnAdd 呼叫 addRow  再 setMode("ADD"); 的話
   * 會導致有些判斷 mode  的動作失常,所以要反過來,先 setMode 再 addRow
   * 但這樣的話,會導致 triggerClick 沒有觸發
   */
  this.__triggerClick = function () {
    // 表單式表頭新增回調表身時因Mode原因先法正確處理后續邏輯，故借用bForceTriggerClick來判別處理
    if (this.bForceTriggerClick) {
      this.__triggerClickAction();
      return;
    }
    // 非表單式表頭新增作業
    if (!(this.sMode == "ADD" || this.sMode == "UPD" )) {
      this.__triggerClickAction();
    } else {
      // cancel the mouse event?
    }
  }

  /**
   * 觸發點擊事件
   */
  this.__triggerClickAction = function () {
      if (this.totalRowCount > 0) {
        //this.tbdy.childNodes[this.UiRowCount].onclick(); // simu a click
        jQuery(this.tbdy.childNodes[this.bMoveLast ? this.totalRowCount - 1 : this.UiRowCount]).click(); // simu a click
        this.bMoveLast = false;
      } else {
        // we don't need to simu a click , but  we still call updateUiCallback
        this.__update_ui_callback();
      }
  }


  //=======================  add / delete row ================================
  /**
   * 新增一筆
   * 用在連續新增, add 和 delete 並不會真的去 dom 物件操作,只針對 this.rows 和 this.tbdy 操作
   * 做完之後 (連續新增) ,要 reload 一次才算完成
   * @param defaults {'key1':'value1','key2':'value2'}
   */
  this.addRow = function (defaults) {
    var i,cell,r,t;
    // document.createTextNode 是 Microsoft 的, 和 ajax 的是不同,這邊要注意
    // 使用錯誤,會有 error
    var doc = (this.LastDataXml && this.LastDataXml.childNodes[0].ownerDocument) || document;
    r = doc.createElement("_r");

    //var defaultVal = (sDefaultValue == undefined) ? String.fromCharCode(160) : sDefaultValue;
    for (i = 0; i < this.domIds.length; i++) {
      cell = doc.createElement(this.domIds[i]);
      if (defaults && defaults[this.domIds[i]]) { // 有傳入默認值時處理
        t = doc.createTextNode(defaults[this.domIds[i]]);
        cell.appendChild( t);
      }
      r.appendChild(cell);
    }
    this.rows.push(r);

    // set current row to last
    this.__UpdateVarByRowIndex(this.rows.length - 1);

    // robert,為了統一 setMode 和 addRow 的呼叫順序,把 setMode  動作,固定寫在 addRow 內
    this.setMode("ADD");
    this.bForceTriggerClick = true;
    this.__loadPage();
    this.bForceTriggerClick = false;

    this.tempRowCount++;
  }
  /**
   * 連續新增多筆資料筆，譬如：快速輸入畫面
   * @param iRows
   * @param  defaults {'key1':'value1','key2':'value2'}
   */
  this.addMutilRow = function (iRows, defaults) {
    // 2012/05/22 Added by Joe 当为快速输入状态关闭【页面数据搜索】功能
    if(detlSearchStatus) detlSearchStatus = false;
    var i,cell,r,t;
    // document.createTextNode 是 Microsoft 的, 和 ajax 的是不同,這邊要注意
    // 使用錯誤,會有 error
    var doc = (this.LastDataXml && this.LastDataXml.childNodes[0].ownerDocument) || document;
    r = doc.createElement("_r");
    //var defaultVal = (sDefaultValue == undefined) ? String.fromCharCode(160) : sDefaultValue;

    for (i = 0; i < this.domIds.length; i++) {
      cell = doc.createElement(this.domIds[i]);
      if (defaults && defaults[this.domIds[i]]) { // 有傳入默認值時處理
        t = doc.createTextNode(defaults[this.domIds[i]]);
        cell.appendChild( t);
      }
      r.appendChild(cell);
    }
    for (i = 0; i < iRows; i++) {
      this.rows.push(r.cloneNode(true));
    }

    // set current row to last
    this.__UpdateVarByRowIndex(this.rows.length - 1);

    this.__loadPage();

    this.tempRowCount++;
  }
  /**
   * 刪除最後一筆資料
   */
  this.deleteLastRow = function () {

    this.rows[this.rows.length - 1] = null;
    this.rows.length = this.rows.length - 1;
    // set current row to last
    this.__UpdateVarByRowIndex(this.rows.length - 1);

    //this.LastDataXml.appendChild(r);
    this.__loadPage();

    this.tempRowCount--;
    // 当没资料时更新为 0/0笔
    if(this.totalRowCount == 0)
      this.__update_ui_callback();
  }
  /**
   * 清空指定节点
   * @param node 目标节点
   * @param isDelSelf  是否清除自己
   * @private
   */
  this.__emptyNode = function (node, isDelSelf) {
    try {
      if (node) {
        if (node.nodeType == 1) {
          // 不能用For下标循环，因删除后下标变了，导致删除掉不到，除非倒着删除
          while (node.firstChild)
            this.__emptyNode(node.firstChild, true);  // 递归调用
        }
        if (isDelSelf)  //  是否清除自己
          node.parentNode.removeChild(node);
      }
    } catch (ignore) {
    }
  };
  /**
   * 刪除全部資料
   */
  this.deleteAllRows = function () {
    this.__UpdateVarByRowIndex(0);
    // 20160909 Joe Fixed: 将XML中的所有_r子节点删除，避免与页面搜索产生冲突。
    if (this.LastDataXml) {
      var xmlDom = this.LastDataXml.cloneNode(true);
      var dataNode = xmlDom.getElementsByTagName("data")[0];
      this.__emptyNode(dataNode, false);
      try {
        dataNode.setAttribute("empty", "true");
      } catch (e) {
      }
      this.LastDataXml = xmlDom;
    }

    this.rows.length = 0;
    this.__loadPage();
  }

  //============================================================================================================

  /**
   * 多筆式作業在新增或修改時，當數據修改完成并離開后更新到XmlDom及MasterTable UI
   * load current detl table data back這個只針對某一個 object , 譬如 input此 function 是給 ajax_blur 用的
   * @param src
   */
  this.loadFromDetl = function (src) {
    // we only allow this if in ADD mode or UPD mode
    // 因為修改要存成功了才算數
    if (!(this.sMode == "ADD" || this.sMode == "UPD")) {
      return;
    }
    if (this.totalRowCount == 0) {
      return
    }

    if (src.attributes == null) return;
    var fldName = src.getAttribute("dataFld");
    if (emisEmpty(fldName)) return;
    var value = getcValue(src);

    this.setCurrentUI(fldName, value);
    this.setCurrent(fldName, value);
  }

  /**
   * 設定各Mode禁用物件數組
   * @param sMode  表單Mode 譬如 "", "ADD", "UPD", "M", "M_UPD"...etc
   * @param idArray 傳入的是 id or name , 譬如 "QTY" , "P_NO"  ...etc
   */
  this.setDisable = function(sMode, idArray) {
    this.DisableComponentWithMode[sMode] = idArray;
  }
  /**
   * 根據Mdoe對相關物件做Disabled處理
   * @param sMode
   */
  this.applyDisable = function(sMode) {
    var idArray = this.DisableComponentWithMode[sMode];
    if (idArray != null && idArray != undefined) {
      for (var i = 0; i < this.tbdy.childNodes.length; i++) {
        var trObject = this.tbdy.childNodes[i];
        this.__DisableComponents(trObject, idArray);
      }
    }
  }
  /**
   * 控制最後一個 component 的 Enter 可以 jump 到 btnSave 去
   * @param obj
   */
  this.setEOFObject = function (obj) {
    if (emisEmpty(obj)) return;
    // 改用JQuery的事件对象兼容Browser
    jQuery(obj).keydown(function(ev) {
      var _iKeyCode = ev.keyCode || ev.which;
      if (_iKeyCode == kbrd.KEY_ENTER ||
          _iKeyCode == kbrd.KEY_TAB
      //|| _iKeyCode == 0  //  去掉该判断，因在firefox下切到中文输入法下按键keycode 为0，造成无法输入中文
          ) {

        // 這邊會出問題,當 focus 剛好在 eof 元件上, 因為這邊會先觸發,造成 document.onkeypress 沒有先處理,所以 btnSave 還沒有出現
        // 所以會 error
        try {
          formobj('btnSave').focus();
        } catch (e) {
        }
        return false;
      }
    });
  }
  /**
   * 把指定範圍（toSearch）內的物件做Disabled處理
   * @param toSearch
   * @param idArray
   */
  this.__DisableComponents = function (toSearch, idArray) {
    AjaxXMLSearch(toSearch, function (dom) {
      for (var i = 0; i < idArray.length; i++) {
        try {
          if (dom.getAttribute("name") == idArray[i]) {
            AjaxDisable(dom, true);
            return;
          }
        } catch (ignore) {
        }
        try {
          if (dom.id == idArray[i]) {
            AjaxDisable(dom, true);
            return;
          }
        } catch (ignore) {
        }
      }
    });
  }
  /**
   * 是否鎖定UI不更新，即是否不回調updateUiCallback的標識; true 不回調， false 回調
   */
  this.blockUiUpdate = false;
  /**
   * 更新畫面頁碼等邊界訊息
   * table object will call this when he need to update ui , and he will call the callback if registered
   * now it is only used as a click callback
   */
  this.__update_ui_callback = function() {
    // do some basic ui update
    if (!emisEmpty(this.span_curr_record))
      this.span_curr_record.innerHTML = (this.totalRowCount == 0 ? 0 : (this.currRowIndex + 1));//(this.currRowIndex + 1);

    if (!emisEmpty(this.span_ttl_records))
      this.span_ttl_records.innerHTML = this.totalRowCount;

    if (!emisEmpty(this.span_curr_page))
      this.span_curr_page.innerHTML = (this.totalPageCount == 0 ? 0 : (this.currentPage + 1)); //(this.currentPage + 1);

    if (!emisEmpty(this.span_ttl_pages))
      this.span_ttl_pages.innerHTML = this.totalPageCount;

    if (!emisEmpty(this.span_pagesize))
      this.span_pagesize.value = this.datapagesize;


    if (!this.blockUiUpdate && this.updateUiCallback != undefined && typeof( this.updateUiCallback ) == "function") {
      this.updateUiCallback(this);
    }
     this.isXmlDataLoaded=true;
  }
  /**
   * MasterTable UI 點擊事件處理
   * @param src
   */
  this.click = function (src) {
    if (this.sMode != "M_UPD" && this.sMode != "" && this.sMode != "M") {
      if (this.bForceTriggerClick == false) {
        if( typeof(ymPromptAlert) == "function") {
          ymPromptAlert({msg:jQuery.getMessage('edit_disabled_msg')});
        } else {
          alert(jQuery.getMessage('edit_disabled_msg')); //新增或修改模式不可选择
        }
        return false;
      }
    }

    if (this.LastUIRowObjectX != null && this.LastUIRowObjectX != "undefined")
    {
      // this.LastUIRowObjectX.style.color = "black";
      // this.LastUIRowObjectX.style.backgroundColor = "#FFFFCC";
      this.LastUIRowObjectX.className = this.LastUIRowObjectX.className.replace(/\bselected\b/g, '');
    }
    this.LastUIRowObjectX = src;
    // 此處不能固定減1，應減表頭的行數，因為LastUIRowObjectX.rowIndex是整個表格的行數Index，即從表頭開始算，如固定減1則會導致表表身定位錯誤
    //this.__setUIRowCount(src.rowIndex - 1);
    this.__setUIRowCount(src.rowIndex - this.thd.rows.length);

    //    src.style.color = "white";
    //    src.style.backgroundColor = "highlight";
    this.LastUIRowObjectX.className += ' selected';

    this.__scrollIntoView();

    this.lastDOMRow = this.rows[ this.currRowIndex ];

    if (this.detlTable != undefined && this.lastDOMRow != undefined) {
      this.detlTable.load(this.lastDOMRow);
    }

    this.__update_ui_callback();

    if(typeof(AjaxMtnCheckButtons) != "undefined") {
      AjaxMtnCheckButtons();
    }

    if (typeof(setTrStyle) == "function") {
      setTrStyle();
    }
    return true;
  }
  /**
   * 表身整批修改或快速輸入時上下移動后焦點定位欄位
   */
  this.moveFocusField = "";
  /**
   * 同步當前記錄于可視位置
   */
  this.__scrollIntoView = function (e) {
    if (!this.LastUIRowObjectX) return;
    //this.LastUIRowObjectX.scrollIntoView(false);
    try {
      if (this.moveFocusField != '') {
        // 这里的e是null（调用的地方没有传参）,不能使用。
        var evn = e || window.event || _oEven;
        var obj = evn.target || evn.srcElement ;
        // 修正移动时焦点乱跳的Bug
        if (evn.type == 'keydown' ||
            (evn.type == 'click' && obj && obj.nodeName != 'INPUT' && obj.nodeName != 'SELECT')) {
          var aFields = this.moveFocusField;
          // 2013/04/17 Joe 增加对数组的支持
          if (typeof aFields == "string") aFields = [aFields];
          if (Object.prototype.toString.call(aFields) === "[object Array]") {
            for (var i = 0; i < aFields.length; i++) {
              var oFocus = docobjs(aFields[i])[this.UiRowCount];
              if (!oFocus || oFocus.disabled == true || oFocus.readOnly) continue;
              try {
                oFocus.focus();
                window.setTimeout(function () {
                  oFocus.select();
                }, 1);
                break;
              } catch (e) {
              }
            }
          }
        }
        return true;
      }
    } catch(er) {
    }
  }
  /**
   * 觸發點擊事件
   * @param index
   */
  this.simulate_click = function (index) {
    var src = this.tbdy.childNodes[index];
    if (src != undefined) {
      // this.click(src);
      jQuery(src).click();
    }
  }

  //=====================================================================================
  //             get/set pair
  //=====================================================================================
  /**
   * 取得指定行中指定欄位的值
   * @param rowIndex
   * @param fieldName
   */
  this.get = function (rowIndex, fieldName) {      
    if (rowIndex >= this.totalRowCount) return null;
    if (rowIndex < 0) return null;

    var row = this.rows[rowIndex];
    var i = this.field2idx[ fieldName ];

    if (i != undefined) {
      //return row.childNodes[i].childNodes[0].nodeValue;
      return cellvalue(row.childNodes[i]);
    }

    //return null;
    return ""; // it is not good to return null , database will save "NULL" string , instead real null value
  }
  /**
   * 設定指定行中指定欄位的值
   * @param rowIndex
   * @param fieldName
   * @param value
   */
  this.set = function (rowIndex, fieldName, value) {
    if (rowIndex >= this.totalRowCount) return false;
    if (rowIndex < 0) return false;

    var row = this.rows[rowIndex];
    var i = this.field2idx[ fieldName ];
    if (i != undefined) {
      setcell(row.childNodes[i], value);
      // 数据同步到DetailTable
      if (this.detlTable != undefined && rowIndex == this.currRowIndex) {
        this.detlTable.setFieldValue(fieldName, value);
      }
      return true;
    }
    return false;
  }
  /**
   * 設定當前頁指定行中指定欄位的值
   * @param iUiRowIndex 是指每頁的 0 ~ ( datapagesize-1 )
   * @param fieldName
   * @param value
   */
  this.setCurrPage = function (iUiRowIndex, fieldName, value) {    
    var idx = this.datapagesize * this.currentPage + iUiRowIndex;
    return this.set(idx, fieldName, value);
  }
  /**
   * 取得指定名稱或索引的欄位值
   * 如果是傳 number (index) , 是以 server 傳回的 xml 的順序來看的 (domIds)
   * @param fieldName
   */
  this.getCurrent = function(fieldName) {
    if (typeof(fieldName) == "string") {
      return this.get(this.currRowIndex, fieldName);
    } else {
      return this.get(this.currRowIndex, this.domIds[fieldName]);
    }
  }
  /**
   * 設定指定名稱或索引的欄位值
   * 如果是傳 number (index) , 是以 server 傳回的 xml 的順序來看的 (domIds)
   * @param fieldName
   * @param value
   */
  this.setCurrent = function(fieldName, value) {
    if (typeof(fieldName) == "string") {
      this.set(this.currRowIndex, fieldName, value);
    } else {
      this.set(this.currRowIndex, this.domIds[fieldName], value);
    }
  }
  /**
   * 取出XMLDom中指定欄位唯一值數組
   * @param fieldName
   */
  this.getUniqueFieldValue = function (fieldName) {
    var datas = this.getFieldValue(fieldName);
    var done = {};
    var ret = [];
    for (var i = 0; i < datas.length; i++) {
      if (!done[datas[i]]) {
        done[datas[i]] = true;
        ret.push(datas[i]);
      }
    }
    delete done;
    return ret;
  }
  /**
   * 取出XMLDom中指定欄位值數組
   * @param fieldName
   */
  this.getFieldValue = function (fieldName) {
    var idx = this.field2idx[ fieldName ];
    var datas = new Array();
    if (typeof idx != 'undefined' && idx != null) {
      for (var i = 0; i < this.totalRowCount; i++) {
        datas.push(this.rows[i].childNodes[idx].childNodes[0].nodeValue);
      }
    }
    return datas;
  }
  /**
   * 設定XMLDom中指定欄位值全部設成指定值, 通常給 check box 用
   * @param fieldName
   * @param value  可以是值也可以是Function，由Function返回结果,<br>Function会自动带入两个参数(MasterTable, RowIndex)
   */
  this.setFieldValue = function (fieldName, value) {
    var idx = this.field2idx[ fieldName ];
    if (idx == undefined || idx == null) {
      return false;
    }
    for (var i = 0; i < this.totalRowCount; i++) {
      //this.rows[i].childNodes[idx].childNodes[0].nodeValue = value;
      var val = typeof value == 'function' ? value(this, i) : value;
      if(val != null && val != undefined) setcell(this.rows[i].childNodes[idx], val);
    }
  }
  /**
   * 設定整筆資料所有欄位值設成指定值，通常用來清空一行的值
   * setting all values of a tr object
   * @param iRowIndex
   * @param value
   */
  this.setRowValue = function(iRowIndex, value) {
    for (var i = 0; i < this.domIds.length; i++) {
      this.set(iRowIndex, this.domIds[i], value);
    }
  }
  /**
   * 設定整個XMLDom所有欄位值設成指定值
   * set all values of a table , usually used to clean a table value
   * @param value
   */
  this.setTableValue = function (value) {
    for (var i = 0; i < this.totalRowCount; i++) {
      this.setRowValue(i, value);
    }
  }
  /**
   * 取得指定欄位最大值，通常用來拿 RECNO 的最大值
   * @param fieldName
   */
  this.getMax = function (fieldName) {
    var m = 0, v, intV, i, idx = this.field2idx[ fieldName ];
    if (idx == undefined) return false;

    for (i = 0; i < this.totalRowCount; i++) {
      v = cellvalue(this.rows[i].childNodes[idx]);
      if (!emisEmpty(v)) {
        try {
          intV = parseInt(v);
          if (intV > m) {
            m = intV;
          }
        } catch (e) {
        }
      }
    }
    return m;
  }

  /**
   * 用來檢查某各 field 是否有重複的值
   * @param fieldName
   * @param value
   */
  this.isAlreadyExist = function(fieldName, value) {
    var i;
    var idx = this.field2idx[ fieldName ];
    if (idx == undefined) return false;

    for (i = 0; i < this.totalRowCount; i++) {
      if (cellvalue(this.rows[i].childNodes[idx]) == value) {
        return true;
      }
    }
    return false;
  }
  //=================================================================================================================
  //              to and from form object , or from table object
  //=================================================================================================================
  /**
   * 依據Form數據產生XmlDom中的一個新行
   * @param formobj
   */
  this.loadNewRowFromFormObj = function(formobj) {
    var i, obj;
    this.addRow();
    //alert( this.currRowIndex );
    for (i = 0; i < this.domIds.length; i++) {
      obj = formobj.elements[ this.domIds[i] ];
      if (obj != undefined) {
        this.setCurrent(i, obj.value);
        // this.setCurrentUI  -- set UI at the same time ?
      } else {
        //alert('set undef');
      }
    }
    this.__loadPage();
  }
 /**
   * 依domIds把當前行資料載入到表單中
   * @param oForm
   * @param oWin
   */
  this.loadFldsToForm = function (oForm, oWin) {
    var obj, fld, i;
    // should we keep 'act' and 'title' unchanged ? a little dangerous
    for (i = 0; i < this.domIds.length; i++) {
      fld = this.domIds[i];
      obj = oForm.elements[ fld ];
      if (obj != undefined) {
        setcValue(obj, this.getCurrent(i));
      }
      // 非IE下獲取不到元素的Window對象
      obj = formobj("spa" + fld, 0, oWin || oForm.ownerDocument.parentWindow);
      if (obj != undefined) {
        setcValue(obj, this.getCurrent(i));
      }
    }
  }
  /**
   * 將畫面中MasterTable UI的數據回寫到XmlDom
   *
   * 重新把 table 的 input ... 的 value , load 回來 (by DataFld)
   * 這個 API 只有處理 table 部分,使用 datafld 欄位來處理
   * form 的範圍比較大 , 這個通常是 mode="M" 時用的
   */
  this.loadValueFromTable = function () {
    var databind = new DataBinding();
    // 从画面把数据同步到XML时应依画面为主，因为画面和XML不一定是完全一致显示，如XML有10笔，画面仅显示3笔
    var carentPageFirstIndex = this.datapagesize * this.currentPage;
//    for (var i = 0; i < this.totalRowCount; i++) {
    for (var i = 0; i < this.tbdy.childNodes.length; i++) {
      searchDataFldObjs(this.tbdy.childNodes[i], databind);
      this.__setFld2Master( carentPageFirstIndex + i, databind);
    }
  }
  /**
   * 把畫面資料回寫到MasterTable XMLDom   
   *
   * 這邊包含了 table 和 form 的 value , 載回 masterTable 的 current row
   * table 的 loading 是用 dataFld , form 的 loading 是靠 name 和 id
   * masterTable 是用 domIds
   */
  this.loadDetlValue2CurrentRow = function () {
    // loading form form obj , 從 domId 去對映, 在 table 的 dataFld 已經有的就不再處理
    // 這邊只處理 hidden input , 而且是 name 不在 datafld 中的
    var formobj = document.forms[0];
    for (var i = 0; i < formobj.elements.length; i++) {
      var element = formobj.elements[i];
      if (element.nodeName == "INPUT") {
        if (element.getAttribute("type") == "hidden") {
          var name = element.getAttribute("name");
          if (emisEmpty(name)) {
            name = element.id;
          }
          if (arraySearch(this.detlTable.databind.dataFlds, name) == -1) {
            // ok , 經過重重檢查,這是我們要處理的
            this.setCurrent(name, getcValue(element));
          }
        }
      }
    }
    // loading table part
    this.__setFld2Master(this.currRowIndex, this.detlTable.databind);
  }

  /**
   * 依DataBind把物件的值回寫XMLDom
   * @param rowIndex
   * @param databind
   */
  this.__setFld2Master = function (rowIndex, databind) {
    var j,k,fld,objs,type,nodeName,obj;
    for (var j = 0; j < databind.dataFlds.length; j++) {
      fld = databind.dataFlds[j];
      this.set(rowIndex, fld, databind.getValue(fld));
    }
  }

  /**
   * 用XMLDom的值完整更新MasterTable UI
   * apply to ui，re-apply all table
   */
  this.applyDomValue2Ui = function () {
    for (var i = 0; i < this.totalRowCount; i++) {
      this.applyDomValue2UiRow(i);
    }
  }
  /**
   * 用XMLDom的值完整更新MasterTable UI
   * apply to ui，re-apply all table
   * add to Jim  from Joe 2013/05/20
   */
  this.applyDomValue2CurrUi = function () {
    for (var i = 0; i < this.currentPageRowCount; i++) {
      this.applyDomValue2UiRow(i + (this.datapagesize * this.currentPage));
    }
  }
  /**
   * 用XMLDom的值完整更新MasterTable 當前筆 UI
   */
  this.applyDomValue2UiCurrRow = function() {
    this.applyDomValue2UiRow(this.currRowIndex);
  }

  /**
   * 用XMLDom的值更新MasterTable UI 指定Index的資料行
   * @param rowIndex
   */
  this.applyDomValue2UiRow = function(rowIndex) {
    /*var databind = new DataBinding();
    searchDataFldObjs(this.tbdy.childNodes[rowIndex], databind);

    databind.loadFromMaster(this, rowIndex);
    databind = null;*/
    this.databind.loadFromMaster(this, rowIndex);
  }
  //=================================================================================================================
  /**
   * 依UI畫面當前行顯示綁定欄位更新XMLDom
   * @param FieldName
   * @param value
   */
  this.setCurrentUI = function (FieldName, value) {
    var tr = this.tbdy.childNodes[ this.UiRowCount ];
    setDataFld(tr, FieldName, value);
  }

  //=====================================================================================
  /**
   * 觸發最近一次選取資料行點擊事件
   */
  this.lastclick = function () {    
    jQuery(this.LastUIRowObjectX).click();
  }


  //=====================================================================================
  //             navigator operations
  //=====================================================================================
  /**
   * 導航至第一筆
   */
  this.first = function (elm) {
    if (this.rows.length == 0) return;
    if (this.currentPage == 0) {
      this.__UpdateVarByRowIndex(0);
      this.__loadCurrentPage(false);
    } else {
      if (elm && elm.nodeName == "input") {
        // 2012/11/19 Joe 取消触发Change事件，避免引发未改变值时触发Change事件
        //jQuery(elm).change().blur();
        jQuery(elm).blur();
      }
      this.__UpdateVarByRowIndex(0);
      this.__loadCurrentPage(true);
    }
  }
  /**
   * 導航至前一頁
   */
  this.prevPage = function (elm) {
    if (this.rows.length == 0) return;
    if (this.currentPage == 0) return;

    if (elm && elm.nodeName == "INPUT") {
      // 2012/11/19 Joe 取消触发Change事件，避免引发未改变值时触发Change事件
      //jQuery(elm).change().blur();
      jQuery(elm).blur();
    }
    // ok, now we have previous page to go
    // find the previous page's last record
    this.__UpdateVarByRowIndex((this.currentPage * this.datapagesize) - 1);
    this.__loadCurrentPage(true);
  }
  /**
   * 導航至上一筆
   */
  this.previous = function (elm) {
    if (this.rows.length == 0) return;
    if (this.currRowIndex == 0) return;

    if ((this.currRowIndex % this.datapagesize) == 0) { //相當於  UiRowCount==0 ,  要換頁
      if (elm && elm.nodeName == "INPUT") {
        // 2012/11/19 Joe 取消触发Change事件，避免引发未改变值时触发Change事件
        //jQuery(elm).change().blur();
        jQuery(elm).blur();
      }
      this.__UpdateVarByRowIndex(this.currRowIndex - 1);
      this.__loadCurrentPage(true);
    } else {
      this.__UpdateVarByRowIndex(this.currRowIndex - 1);
      this.__loadCurrentPage(false);
    }
  }
  /**
   * 導航至下一筆
   */
  this.next = function (elm) {
    if (this.rows.length == 0) return;

    if ((this.currRowIndex + 1) >= this.totalRowCount) return;

    if ((this.currRowIndex % this.datapagesize) == (this.datapagesize - 1)) { // 要換頁
      if (elm && elm.nodeName == "INPUT") {
        // 2012/11/19 Joe 取消触发Change事件，避免引发未改变值时触发Change事件
        //jQuery(elm).change().blur();
        jQuery(elm).blur();
      }
      this.__UpdateVarByRowIndex(this.currRowIndex + 1);
      this.__loadCurrentPage(true);
    } else {
      this.__UpdateVarByRowIndex(this.currRowIndex + 1);
      this.__loadCurrentPage(false);
    }
  }
  /**
   * 導航至下一頁
   */
  this.nextPage = function (elm) {
    if (this.rows.length == 0) return;
    if ((this.currentPage + 1) >= this.totalPageCount) return;

    if (elm && elm.nodeName == "INPUT") {
      // 2012/11/19 Joe 取消触发Change事件，避免引发未改变值时触发Change事件
      //jQuery(elm).change().blur();
      jQuery(elm).blur();
    }
    this.__UpdateVarByRowIndex((this.currentPage + 1) * this.datapagesize);
    this.__loadCurrentPage(true);

  }
  /**
   * 導航至最后一頁
   */
  this.lastPage = function (elm) {
    if (this.rows.length == 0) return;
    if (this.currentPage == this.totalPageCount - 1) {
      this.__UpdateVarByRowIndex((this.totalPageCount - 1) * this.datapagesize);
      this.__loadCurrentPage(false);
    } else {
      if (elm && elm.nodeName == "INPUT") {
        // 2012/11/19 Joe 取消触发Change事件，避免引发未改变值时触发Change事件
        //jQuery(elm).change().blur();
        jQuery(elm).blur();
      }
      this.__UpdateVarByRowIndex((this.totalPageCount - 1) * this.datapagesize);
      this.__loadCurrentPage(true);
    }

  }
  /**
   * 導航至最后一筆
   */
  this.last = function (elm) {
    if (this.rows.length == 0) return;

    if (this.currentPage == this.totalPageCount - 1) {
      this.__UpdateVarByRowIndex(this.totalRowCount - 1);
      this.__loadCurrentPage(false);
    } else {
      if (elm && elm.nodeName == "INPUT") {
        // 2012/11/19 Joe 取消触发Change事件，避免引发未改变值时触发Change事件
        //jQuery(elm).change().blur();
        jQuery(elm).blur();
      }
      this.__UpdateVarByRowIndex(this.totalRowCount - 1);
      this.__loadCurrentPage(true);
    }
  }
  /**
   * 調試時使用(debug usage....)
   */
  this.__dumpPageVars = function () {
    var txt = "";
    txt += "currentPage=" + this.currentPage + "\n";
    txt += "currentPageRowCount=" + this.currentPageRowCount + "\n";
    txt += "currRowIndex=" + this.currRowIndex + "\n";
    txt += "totalRowCount=" + this.totalRowCount + "\n";
    txt += "totalPageCount=" + this.totalPageCount + "\n";
    txt += "UiRowCount=" + this.UiRowCount + "\n";
    txt += "datapagesize=" + this.datapagesize;

    alert(txt);

  }
  /**
   * 在XMLDom搜索指定欄位中的指定值，通常用于【查找】功能
   * @param fieldName
   * @param searchValue
   */
  this.search = function (fieldName, searchValue) {
    if(!isNaN(fieldName))
      fieldName = this.domIds[fieldName];
    var i;
    searchValue = (searchValue + "").toUpperCase(); //转成大写匹配（不区分大小写,转换前先转成字串，不然如果是数字类型会报错）
    for (i = this.currRowIndex; i < this.totalRowCount; i++) {
      if (this.get(i, fieldName).toUpperCase() == searchValue) {
        this.searchResult = i;
        return true;
      }
    }
    for (i = 0; i < this.currRowIndex; i++) {
      if (this.get(i, fieldName).toUpperCase() == searchValue) {
        this.searchResult = i;
        return true;
      }
    }
    return false;
  }
  /**
   * 依轉入Index導舤至指定行（Index是相對于整個XMLDom而言）
   * go to the certain index row by Dom's view, it will simulate a click
   * @param nTargetRow
   */
  this.go = function(nTargetRow) {
    var nOldPage = this.currentPage;
    this.__UpdateVarByRowIndex(nTargetRow);

    if (nOldPage != this.currentPage) {
      this.__loadCurrentPage(true);
    }
    if (this.totalRowCount > 0) {
      // this.tbdy.childNodes[this.UiRowCount].onclick(); // simu a click
      jQuery(this.tbdy.childNodes[this.UiRowCount]).click();
    }
  }

  //=====================================================================================
  //      試做 client side 的 order by
  //      fieldType 為 NUMBER 和 STRING 兩種 , default 為 STRING
  //      orderType =  "DESC" , "ASC" , 沒傳 default 為 ASC
  //=====================================================================================
  // Sort Function Begin
  /**
   * 排序方法集合
   * @param fieldType < NUMBER, TEXT >
   * @param orderType < ASC, DESC >
   */
  this.__sorters = {
    // 数值排序
    NUMBER:{
      ASC: function (a, b) {
//        return a.value - b.value;
        return a - b;
      },
      DESC: function (a, b) {
//        return b.value - a.value;
        return b - a;
      }
    },
    // 字符值排序
    STRING2: {
      ASC: function (a, b) {
//        return ((a.value < b.value) ? -1 : ((a.value > b.value) ? 1 : 0));
        return ((a < b) ? -1 : ((a > b) ? 1 : 0));
      },
      DESC: function (a, b) {
//        return ((b.value < a.value) ? -1 : ((b.value > a.value) ? 1 : 0));
        return ((b < a) ? -1 : ((b > a) ? 1 : 0));
      }
    },
    // 拼音排序
    STRING: {
      ASC: function(a, b) {
//        return a.value.localeCompare(b.value);
        return a.localeCompare(b);
      },
      DESC:function(a, b) {
//        return b.value.localeCompare(a.value);
        return b.localeCompare(a);
      }
    }
  }
  /**
   * 對XMLDom依指定欄位，類型，方式進行排序
   * @param fieldName
   * @param fieldType
   * @param orderType
   */
  this.__makeDomOrder = function(sortList){
    var field, type, order, idx, s, e, i, len = sortList.length;
    var dynamicExp = "var sortWrapper = function(a, b) {";
    for (i = 0; i < len; i++) {
      field = sortList[i][0];
      type = sortList[i][1];
      order = sortList[i][2] || 'ASC';
      idx = this.field2idx[field];
      s = this.varname + '.' + '__sorters["' + type + '"]["' + order + '"]';
      e = "e" + i;

      dynamicExp += "var " + e + " = " + s + "(cellvalue(a.childNodes[" + idx + "]), cellvalue(b.childNodes[" + idx + "])); ";
      dynamicExp += "if(" + e + ") { return " + e + "; } ";
      dynamicExp += "else { ";
    }
    // if value is the same keep orignal order
    dynamicExp += "return a.getAttribute('index')-b.getAttribute('index');";
    for (var i = 0; i < len; i++) {
      dynamicExp += "}; ";
    }
    dynamicExp += "}; ";

    eval(dynamicExp);
    // 记录当前所在位置，用于排序比较中发现相同值时按原位排放
    for (var i = 0; i < this.rows.length; i++)
      this.rows[i].setAttribute("index", i);
    // 排序
    this.rows.sort(sortWrapper);
  }
  /**
   * 對XMLDom進行排序并重新載入頁面顯示
   * @param fieldName
   * @param fieldType
   * @param orderType
   */
  this.orderBy = function (sortList) {
    this.lastOrderAction = sortList;
    this.__makeDomOrder(sortList);

    // after all swap , we just call loadpage again
    this.__loadCurrentPage(true, true);
  }
  /**
   * 實現點選表頭進行排序功能（無鎖定表頭），通常用于表單式之表身作業
   * @param $  依賴jQuery
   * @param options 排序設定 預設為：{index: '0' , order: 'ASC'}
   */
  this.sorter = function ($, options) {
    var $sorttable = $(this.tbl);
    var $header = $sorttable.find("th[sortField]");
    // 若无排序栏位不做后续处理直接返回
    if($header.length == 0) return this;

    $sorttable.data("hasSorted", "true");
    var oMasterTbl = this;
    var defaults = {
      cssHeader: "headerSort",        // 排序前样式
      cssAsc: "headerSortUp",         // 升序样式
      cssDesc: "headerSortDown",      // 降序样式
      cancelMultiSort: false,         // 取消多列排序
      sortMultiSortKey: "shiftKey",   // 多列排序热键
      cancelSelection: true,          // 取消内容选取
      sortList: []                    // 排序规则[ [FIELD1, NUMBER, ASC, TITLE1],  [FIELD1, STRING, DESC, TITLE2]... ]
    };
    var config = $.extend({}, defaults, options);

    __prepare_sorter(oMasterTbl, $header, config);

    return this;
  }

  /**
   * 實現點選表頭進行排序功能（鎖定表頭），通常用于多筆式或單筆式作業
   * 注意：当内容宽度不固定时不能使用此API
   * @param $  依賴jQuery
   * @param options 排序設定 預設為：{index: '0' , order: 'ASC'}
   */
  this.scrollsorter = function ($, options) {
    // 因固定表头的方法仍有问题，故先使用非固定取代
    this.sorter($, options);
    return;
    if (typeof $ == 'undefined' || typeof $.fn.scrollTable == 'undefined') {
      alert("Sorter Table need jQuery and plugin scrollTable");
      return;
    }
    // 优先调用计算逻辑以便获取ListPanel的真实有效高度
    if(typeof mtnBeforeOnload == "function")
      mtnBeforeOnload();

    var oMasterTbl = this;
    // idDataList
    var $divDetail = $('#' + this.id).parent();
    // listPanel
    var $divListPanel = $divDetail.parent();
    // 取消ListPanel的滚动条
    $divListPanel.css("overflow","hidden");
    var sHeight = $divListPanel.height() - $('thead', this.tbl).height() - 2;
    var defaults = {
      className: oMasterTbl.tbl.className
      ,height: sHeight
      ,
      cssHeader: "headerSort",        // 排序前样式
      cssAsc: "headerSortUp",         // 升序样式
      cssDesc: "headerSortDown",      // 降序样式
      cancelMultiSort: false,         // 取消多列排序
      sortMultiSortKey: "shiftKey",   // 多列排序热键
      cancelSelection: true,          // 取消内容选取
      sortList: []                    // 排序规则[ [FIELD1, NUMBER, ASC, TITLE1],  [FIELD1, STRING, DESC, TITLE2]... ]
    };
    var config = $.extend({}, defaults, options);

    $divDetail.scrollTable({
      tableClassName: config.className,
      height: config.height,
      onComplete: function(header, footer) {
        var $header = $(header).find("th[sortField]");
        __prepare_sorter(oMasterTbl, $header, config);
      }
    });
    return this;
  }
  // Sort Function End
  /**
   * 实际处理排序逻辑，MasterTable.sorter & MasterTable.scrollsorter 共用
   * @param oMasterTbl
   * @param $header
   * @param config
   * @private
   */
  function __prepare_sorter(oMasterTbl, $header, config){
    $header.each(function() {
      $(this).data("default_html", jQuery(this).html())
          .css("cursor", "pointer")
          .wrapInner(function() {
            return '<div class="' + config.cssHeader + '"></div>';
          });
    });

    var fieldIndexOfArray = function (v, a) {
      var len = a.length;
      for (var i = 0; i < len; i++) {
        if (a[i][0] == v) {
          return i;
        }
      }
      return -1;
    };
    $header.bind('click', function(event) {
      if (oMasterTbl.sMode == 'ADD' || oMasterTbl.sMode == 'UPD' || oMasterTbl.sMode == 'M_UPD') {
        if( typeof(ymPromptAlert) == "function") {
          ymPromptAlert({msg:jQuery.getMessage('sort_disabled_msg')});
        } else {
          alert(jQuery.getMessage('sort_disabled_msg')); // 新增或修改模式不可排序
        }
        return false;
      }

      var $cell = jQuery(this);
      var field = $cell.attr('sortField');
      var type = ($cell.attr('sortType') || "STRING").toUpperCase();
      var count = +($cell.attr('count') || '0');
      var order = count++ % 2 == 0 ? "ASC" : "DESC" ;
      var title = $cell.text() + ('(' + (order == 'ASC' ? '↑' : '↓') + ')');

      // cancel multi sort or user only whants to sort on one column
      if (config.cancelMultiSort || !event[config.sortMultiSortKey]) {
        // flush the sort list
        config.sortList.splice(0, config.sortList.length);

        // 移除其他排序標記
        $header.each(function() {
          jQuery(this).html(jQuery(this).data("default_html"))
              .wrapInner(function() {
                return '<div class="' + config.cssHeader + '"></div>';
              });
        });

        // add column to sort list
        config.sortList.push([field, type, order, title]);
      }
      // multi column sorting
      else {
        // the user has clicked on an all ready sortet column.
        var idx = fieldIndexOfArray(field, config.sortList);
        if(idx != -1) {
          // change column sort type
          config.sortList[idx][2] = order;
          config.sortList[idx][3] = title;
        } else {
          // add column to sort list
          config.sortList.push([field, type, order, title]);
        }
      }
      $cell.attr('count', count);

      $cell.html(jQuery(this).data("default_html"))
          .wrapInner(function() {
            return '<div class="' + (order == 'ASC' ? config.cssAsc : config.cssDesc) + '" ></div>';
          });
      // 多列排序时，提示排序规则
      if (!config.cancelMultiSort && event[config.sortMultiSortKey]) {
        var len = config.sortList.length;
        title = jQuery.getMessage('sort_title_msg'); // 当前排序
        for (var i = 0; i < len; i++)
          title += "\n" + (i + 1) + '.' + config.sortList[i][3];
        jQuery('div.' + config.cssAsc + ',div.' + config.cssDesc, $header).attr('title', title);
      }
      // 进行排序并更新画面数据显示
      oMasterTbl.orderBy(config.sortList);
    })
      // cancel selection
        .mousedown(function() {
          if (config.cancelSelection) {
            this.onselectstart = function() {
              return false
            };
            return false;
          }
        });

    // 页面加载完成后自动排序
    (function(){
      if (config.sortList.length > 0) {
        var i, f = {}, len = config.sortList.length, error = false;
        for (i = 0; i < len; i++) {
          // 预设排序规则，必需有栏位名，排序类型
          if(config.sortList[i].length < 2){
            error = true;
            break;
          }
          f[config.sortList[i][0]] = config.sortList[i][2] || 'ASC';
        }
        if(!error){
          // 设置表头标题排序图例
          $header.each(function() {
            var $cell = jQuery(this);
            var order = f[$cell.attr('sortField')];
            if (order) {
              $cell.html($cell.data("default_html"))
                  .wrapInner(function() {
                    return '<div class="' + (order == 'ASC' ? config.cssAsc : config.cssDesc) + '" ></div>';
                  }).attr('count', order == 'ASC' ? 1 : 2);
            }
          });
          // 多列排序时，提示排序规则
          if (!config.cancelMultiSort && config.sortList.length > 1) {
            len = config.sortList.length;
            var title = jQuery.getMessage('sort_title_msg'); // 当前排序
            for (i = 0; i < len; i++)
              title += "\n" + (i + 1) + '.' + (config.sortList[i][3] || config.sortList[i][0]);
            jQuery('div.' + config.cssAsc + ',div.' + config.cssDesc, $header).attr('title', title);
          }
          // 执行排序
          window.setTimeout(function() {
            oMasterTbl.orderBy(config.sortList);
          }, 2);
        }
      }
    })();
  }
  // __prepare_sorter Function End
  //=====================================================================================
  //             page size setting
  //=====================================================================================
  /**
   * 設定每頁顯示筆數
   * @param nNewPageSize
   */
  this.changePageSize = function(nNewPageSize) {
    //  要注意要轉成 數字型別,不然會變成文字相連
    nNewPageSize = parseInt(nNewPageSize, 10);
    if (nNewPageSize <= 0) return;
    this.datapagesize = nNewPageSize;
    /*document.getElementById(this.id).setAttribute("datapagesize", nNewPageSize);
    this.tbl.setAttribute("datapagesize", nNewPageSize);
    this.tblobjs[ this.id ].setAttribute("datapagesize", nNewPageSize);*/
    this.__loadPage();
  }
  /**
   * 創建筆數UI
   * 格式: (當前筆)/(總筆數)筆
   * @param id
   */
  this.createRecordSpan = function(id) {
    var span = document.getElementById(id);
    if (emisEmpty(span)) return;
    //alert(div);
    span.innerHTML = "<span id='idTBLspanCurRecord'   style='font-size:10pt;color:blue'></span>/<span id='idTBLspanRecord' style='font-size:10pt;color:blue'>&nbsp;</span>"+jQuery.getMessage('toolbar_page_rows');
    this.span_curr_record = document.getElementById("idTBLspanCurRecord");
    this.span_ttl_records = document.getElementById("idTBLspanRecord");
  }
  /**
   * 創建頁碼UI
   * 格式: (當前頁)/(總筆頁)頁
   * @param id
   */
  this.createPageRecordSpan = function(id) {
    var div = document.getElementById(id);
    if (emisEmpty(div)) return;
    div.innerHTML = "<span id='idTBLspanCurPage' style='color:blue;font-size:10pt'></span>/<span id='idTBLspanPage' style='color:blue;font-size:10pt'>&nbsp;</span>"+jQuery.getMessage('toolbar_page_unit');
    this.span_curr_page = document.getElementById("idTBLspanCurPage");
    this.span_ttl_pages = document.getElementById("idTBLspanPage");
  }
  /**
   * 創建每頁筆數UI
   * 格式: 每頁[10]筆
   * @param id
   */
  this.createPageSpan = function(id) {
    var div = document.getElementById(id);
    if (emisEmpty(div)) return;

    div.innerHTML = jQuery.getMessage('toolbar_page_size',"<input id='idTBLspanPageSize' type='text' maxlength='3' size='2' style='font-size:10pt'>");

    var oMTbl = this;
    jQuery('#idTBLspanPageSize').blur( function() {
       oMTbl.changePageSize(this.value);
    });

    this.span_pagesize = document.getElementById("idTBLspanPageSize");

    this.span_pagesize.value = this.datapagesize;
  }
  /**
   * 創建模式UI
   * 格式： 模式：瀏覽/編輯...
   * @param id
   */
  this.createModeSpan = function(id) {
    var span = document.getElementById(id);
    if (emisEmpty(span)) return;
    var span_mode_id = this.varname + "_idMode";
    var txt = jQuery.getMessage("toolbar_mode_txt");
    span.innerHTML = "<span style='font-size:10pt'>"+txt+"</span><span style='color:red;font-size:10pt' id='" + span_mode_id + "'></span>";
    this.span_mode = document.getElementById(span_mode_id);
    //alert(this.span_mode);
  }
  /**
   * MasterTable設定當前作業模式
   * modes :  "" , "M" , "ADD","UPD","DEL","M_UPD"
   * ""  mode 是簡單的基本資料的 mode , MasterTable 不能編輯,detl table 可以編輯
   * M 和 M_UPD 是 masterTable 會可以 update  , 多半用在表頭表身的表身
   * @param sMode
   */
  this.setMode = function (sMode) {
    sMode = sMode.toUpperCase();
    this.sMode = sMode;

    if (sMode == "" || sMode == "M") {
      this.tempRowCount = 0;
      if (!emisEmpty(this.span_mode)) this.span_mode.innerHTML = jQuery.getMessage("toolbar_mode_browse");
    }

    //==============================================================================================
    // form control , table 包在 form 內,所以 table 的 component 都會被影響到 , button 和 hidden 物件不會
	  // 做這個的原因是因為 M mode 常常有 checkbox,需要 enable,所以才有這一段
	
    if (sMode == "") {
      emisFormAttrib(document.forms[0], "Disable");
    } else {
      emisFormAttrib(document.forms[0], "Enable");  // mode 'M'
    }
	
    //==============================================================================================
    if (sMode == "ADD") {
      if (!emisEmpty(this.span_mode)) this.span_mode.innerHTML = jQuery.getMessage("toolbar_mode_add");
    }

    if (sMode == "UPD" || sMode == "M_UPD") {
      if (!emisEmpty(this.span_mode)) this.span_mode.innerHTML = jQuery.getMessage("toolbar_mode_upd");
    }

    this.applyDisable(sMode);

    if (!emisEmpty(this.navigator)) {
      this.navigator.setMode(sMode);
    }
    if (!emisEmpty(this.toolbar)) {      
      this.toolbar.setMode(sMode);
    }
    if (!emisEmpty(this.detlTable)) {      
      this.detlTable.setMode(sMode);
    }
	
  }
  /**
   * 創建導航欄UI
   * @param spanId
   * @param style
   */
  this.createNavigator = function (spanId, style) {
    this.navigator = new Navigator(spanId, this, style);
  }
  /**
   * 創建功能欄UI
   * @param divId
   * @param attr 此參數于目前已沒有實際用處
   */
  this.createToolbar = function (divId, opts) {
    this.toolbar = new ToolBar(this);
    this.toolbar.init(divId, this, opts);
    return this.toolbar;
  }
  /**
   * 創建 DetailTable 物件，用于綁定呈現數據等
   * @param TableId
   * @param varname DetailTable實例之物件名，便于后繼作業使用
   */
  this.createDetlTable = function(TableId, varname) {
    this.detlTable = new DetailTable(TableId, varname);
    this.detlTable.domIds = this.domIds;
    this.detlTable.masterTbl = this;
    this.detlTable.databind.registerDetlOnBlur(this);
    return this.detlTable;
  }


  // ================================ debug util =============================================
  /**
   * 調試使用功能函數
   */
  this.dumpTable = function () {
    var tbl = document.createElement("table");
    var thead = document.createElement("thead");
    var tbody = document.createElement("tbody");

    var tr = document.createElement("tr");
    var td, n , i , j;
    for (i = 0; i < this.domIds.length; i++) {
      td = document.createElement("td");
      //alert(this.domIds[i]);
      n = document.createTextNode(this.domIds[i]);
      td.appendChild(n);
      td.setAttribute("nowrap", "0");
      tr.appendChild(td);
    }
    thead.appendChild(tr);


    for (i = 0; i < this.totalRowCount; i++) {
      tr = document.createElement("tr");
      for (j = 0; j < this.domIds.length; j++) {
        td = document.createElement("td");
        n = document.createTextNode(this.get(i, this.domIds[j]));
        td.appendChild(n);
        tr.appendChild(td);
      }
      tbody.appendChild(tr);
    }
    tbl.appendChild(thead);
    tbl.appendChild(tbody);
    tbl.border = 1;
    tbl.id = '__dumptbl';
    var old_tbl = document.getElementById(tbl.id);
    if (old_tbl != undefined) {
      document.body.removeChild(old_tbl);
    }
    document.body.appendChild(tbl);

  }


}

//------------------------------------------------------------------------------
//            start of declare Detail Table object
//------------------------------------------------------------------------------
/*
  detailTable 內含 input,select,span 之類 , 用來顯示每一筆 row 的資料 ( from MasterTable)
  properties
      currDOM : store the last load DOM object
  functions
      load: load one row from xmldom
      clearFields: clear input fields
*/
/**
 * 這是一個用來綁定顯示等功能實現的類，通常用于處理XMLDom中詳細數據處理
 * 如表頭，多筆式Form的內容等
 * @class This is the basic DetailTable class.
 * @param TableId
 * @param varname
 */
function DetailTable(TableId, varname) {
  this.varname = varname; // 因為有些自動產生的 script 需要 table object 變數名稱

  this.id = TableId;
  this.tbl = document.getElementById(TableId);

  this.databind = new DataBinding();

  getTableIdArray(this.tbl, false, this.databind);
  // cache the ui mapping
  this.domIds = null;
  this.masterTbl = null;

  //this.disableWhenUpdate = null;
  this.disableWithMode = new Array();
  this.focusWithMode = new Array();
  this.visibleWithMode = new Array();
  this.hiddenWithMode = new Array();

  this.editEof = null;

  /**
   * 載入一筆資料數據
   * <pre>
   * 注意 form 和 table 不可混在一起,這邊會抓 table 之內的 input ,
   * 如果把 form 的 hidden inputs 也放在 table 內會抓混淆
   * 這邊會自動用 xml 的 fileds 去抓 detail 的 components ,
   * 條件為  name, "spa" + name, "sel" + name
   * 同一個 name, 三種有符合的話,都會抓進去,會自動配對建立關係
   * </pre>
   * @param RowDOM MasterTable XMLDom 中的一筆
   */
  this.load = function(RowDOM) {
    this.currentDom = RowDOM;
    var i,value;
    for (i = 0; i < this.domIds.length; i++) {
      /*var carray = this.databind.fld2Objs[this.domIds[i]];
      value = cellvalue(RowDOM.childNodes[i]);
      if (carray != undefined) {
        //  有的 field 可能是找不到 ui component 的,譬如 A+B 所產生的欄位,所以要先判斷 undefined
        for (j = 0; j < carray.length; j++) {
          if (!setcValue(carray[j], value)) {
            alert("err setcValue " + i + ":" + carray[j] + "=" + value);
          }
        }
      }*/
      value = cellvalue(RowDOM.childNodes[i]);
      this.setFieldValue(this.domIds[i], value);
    }
  }
  /**
   * 给指定栏位赋值
   * @param RowDOM
   * @param domId
   * @param value
   */
  this.setFieldValue = function (domId, value) {
    var carray = this.databind.fld2Objs[domId];
    if (carray != undefined) {
      var j, val, parser;
      //  有的 field 可能是找不到 ui component 的,譬如 A+B 所產生的欄位,所以要先判斷 undefined
      for (j = 0; j < carray.length; j++) {
        parser = carray[j].getAttribute("dataParser");
        // 当有附加解析器时呼叫获取返回处理结果再写入
        if (!!parser && typeof window[parser] == "function") {
          val = window[parser]({
            /*当前对象*/
            elm: carray[j],
            /*当前字段值*/
            val: value
          });
          if (!setcValue(carray[j], val)) {
            alert("err setcValue " + domId + ":" + carray[j] + "=" + val);
          }
        } else {
          if (!setcValue(carray[j], value)) {
            alert("err setcValue " + domId + ":" + carray[j] + "=" + value);
          }
        }
      }
    }
    if (domId == 'FLS_NO' && typeof checkFLS_NO == 'function') {
      var obj = document.getElementById("spaFLS_NO");
      if (obj) {
        setcValue(obj, checkFLS_NO(value));
      }
      obj = document.getElementById("spaFLS_NAME");
      if (obj) {
        setcValue(obj, checkFLS_NO(value));
      }
    }
  }
  /**
   * 增加一個數據綁定物件
   * <pre>
   * some component are relative but not defined in jspf ,
   * for example , S_NO field relative to hidden &lt;input&gt;
   * but visible &lt;select&gt; is not relative , and we can add it
   * </pre>
   * @param fieldName
   * @param bindingObj
   */
  this.addDataBinding = function (fieldName, bindingObj) {
    var ary = this.databind.fld2Objs[fieldName];
    if (ary != undefined && ary != null) {
      ary.push(bindingObj);
    }
  }
  //==================================================================================================
  //   some disable , focus ,eof control with mode
  //==================================================================================================
  /**
   * 設定各種Mode相應禁用的物件數組
   * @param sMode
   * @param objArray
   */
  this.setDisable = function (sMode, objArray) {
    this.disableWithMode[sMode] = objArray;
  }
  /**
   * 依Mode禁用物件處理邏輯
   * @param sMode
   */
  this.applyDisable = function(sMode) {
    var objArray = this.disableWithMode[sMode];
    if (! emisEmpty(objArray)) {
      for (var i = 0; i < objArray.length; i++) {
        AjaxDisable(objArray[i],true);
      }
    }
  }
  /**
   * 設定各種Mode時焦點定位的物件
   * @param sMode
   * @param object
   */
  this.setFocus = function (sMode, object) {
    this.focusWithMode[sMode] = object;
  }
  /**
   * 依Mode焦點定位物件處理邏輯
   * @param sMode
   */
  this.applyFocus = function(sMode) {
    var obj = this.focusWithMode[sMode];
    if (! emisEmpty(obj)) {
      try {
        // joe Mark 改为可直持数组
        //obj.focus();
        setFocus(obj);
      } catch (ignore) {
      }
    }
  }
  /**
   * 控制無法固定指定最后一個Component時，依優先順序排放的數組設定
   * 在Keyboard。ProcessKey 中使用
   * 控制最後一個 component 的 Enter 可以 jump 到 btnSave 去
   * @param obj
   */
  this.EOFObjects = new Array();
  /**
   * 控制最後一個 component 的 Enter 可以 jump 到 btnSave 去
   * @param obj
   */
  this.setEOFObject = function (obj) {
    if (emisEmpty(obj)) return;

    jQuery(obj).keydown(function(e) {
      var _iKeyCode = e.keyCode || e.which;
      if (_iKeyCode == kbrd.KEY_ENTER ||
          _iKeyCode == kbrd.KEY_TAB
          //|| _iKeyCode == 0   //  去掉该判断，因在firefox下切到中文输入法下按键keycode 为0，造成无法输入中文
          ) {

        // 這邊會出問題,當 focus 剛好在 eof 元件上,
        // 因為這邊會先觸發,造成 document.onkeypress 沒有先處理,
        // 所以 btnSave 還沒有出現 所以會 error
        try {
          formobj('btnSave').focus();
        } catch (e) {
        }
        return false;
      }
    });
  }

  // setVisible 設定某個 mode 時要特別顯現的 object
  /**
   * 設定各種Mode時可使用物件數組
   * @param sMode
   * @param objArray
   */
  this.setVisible = function (sMode, objArray) {
    this.visibleWithMode[sMode] = objArray;
  }
  /**
   * 設定各種Mode時隱藏物件數組
   * @param sMode
   * @param objArray
   */
  this.setHidden = function (sMode, objArray) {
    this.hiddenWithMode[sMode] = objArray;
  }
  /**
   * 依Mode顯示物件處理邏輯
   * @param sMode
   */
  this.applyVisible = function(sMode) {
    var i;
    var objArray = this.visibleWithMode[sMode];
    if (!emisEmpty(objArray)) {
      for (i = 0; i < objArray.length; i++) {
        jQuery(objArray[i]).show();
      }
    }
  }
  /**
   * 依Mode隱藏物件處理邏輯
   * @param sMode
   */
  this.applyHidden = function(sMode) {
    var i;
    var objArray = this.hiddenWithMode[sMode];
    if (!emisEmpty(objArray)) {
      for (i = 0; i < objArray.length; i++) {
        jQuery(objArray[i]).hide();
      }
    }
  }

  //==================================================================================================
  /**
   * 依欄位名稱取值
   *
   * get value of certain field by name
   * 主要是給 masterTable.loadFromDetl 使用
   * @param fieldName
   */
  this.get = function(fieldName) {
    var fldobjArray = this.databind.fld2Objs[ fieldName ];
    if (emisEmpty(fldobjArray)) {
      return "";
    }
    return getcValue(fldobjArray[0]);
  }
  /**
   * 清空所有物件內容
   */
  this.clearFields = function () {
    var i,j,tarray;
    for (i = 0; i < this.databind.fld2Objs.length; i++) {
      tarray = this.databind.fld2Objs[i];
      if (tarray != undefined) {
        for (j = 0; j < tarray.length; j++) {
          setcValue(tarray[j], '');
        }
      }
    }
  }
  /**
   * DetailTable模式設定處理邏輯
   * @param sMode
   */
  this.setMode = function(sMode) {
    if (sMode == "UPD") {
    }

    if (sMode == "ADD") {
      this.clearFields();
    }

    if (sMode == "") {

      if (this.masterTbl != null) {
        if (this.masterTbl.totalRowCount == 0) {
          this.clearFields();
        }
      }
    }
    this.applyDisable(sMode);
    this.applyFocus(sMode);
    this.applyVisible(sMode);
    this.applyHidden(sMode);
  }
  /**
   * 把傳入之物件集進行Disabled處理
   * @param disableIdArrays
   */
  this.__disableFields = function (disableIdArrays) {
    var i;
    for (i = 0; i < disableIdArrays.length; i++) {
      var obj = disableIdArrays[i];
      if (!emisEmpty(obj)) {
        AjaxDisable(obj,true);
      } else {
        alert('__disableFields:' + disableIdArrays[i] + ' error');
      }
    }
  }
}


//=============================================================
//			start of ToolBar object declare
//=============================================================
/**
 * 功能欄UI
 * @class This is the basic ToolBar class.
 * @param masterTblObj
 */
function ToolBar(masterTblObj, options) {
  this.rights = null; // button rights array , support 四種權限控管
  this.masterTbl = masterTblObj;

  this.disableBtnWithMode = new Array();

  this.btnMap = new Array();

  this.activeBtnMap = new Array();

  // 当表头无新增，修改，删除按钮且表身无强制显示设定不处理时，
  // 默认不显示表身按钮：新增，修改，删除，整批，F10等编辑按钮
  // 如需自主控制，请以参数传入
  this.detlEditButtons = (function () {
    var btns = [];
    if (parent && jQuery(parent.document).attr("EMIS_IS_BILL_MODE") === "Y") {
      var opts = jQuery.extend({DetlButtons: [formobj("btnAdd"), formobj("btnUpd"), formobj("btnDel"),
        formobj("btnBat3"),formobj("btnUpdAll"), formobj("btnF10"), formobj("btnBat")], ProcDispDetlBtn: true}, options);
      if (opts.ProcDispDetlBtn) return btns;
      if (emisEmpty(typeof(window.parent.formobj("btnAdd"))) &&
          emisEmpty(typeof(window.parent.formobj("btnUpd"))) &&
          emisEmpty(typeof(window.parent.formobj("btnDel")))) {
        jQuery.each(opts, function () {
          var $this = jQuery(this);
          if ($this.length > 0) btns.push(($this.attr("id") || "").substring(3).toUpperCase());
        });
      }
    }
    return btns;
  })();
  /**
   * 事件處理，通常用于快速鍵回應
   * @param ev
   */
  this.processKey = function(e) {
    var ev = e || window.event, _oSrcElement;
    if(!ev) return;
    var cfg, code = ev.keyCode|| ev.which;
    // 2013/01/05 Joe 当KeyCode为0时不做任何处理，修正当KeyCode为0时触发按钮点击事件错误，IE不知为何有时出现这样的问题，待后续查证
    if(code == 0) return true;
    for (var i = 0; i < this.activeBtnMap.length; i++) {
      cfg = this.activeBtnMap[i];
      // 2010/02/23 Joe.yao modify: 修正Alt-F4時先彈出列印視窗Bug
      if (!emisEmpty(cfg.alt)) {// 要求Alt
        if (cfg.key != code || !kbrd.is_ALT(ev)) {
          continue;
        }
      } else if (kbrd.is_ALT(ev)) {// 已按了Alt
        if (cfg.key != code || emisEmpty(cfg.alt)) {
          continue;
        }
      } else { // 一般鍵
        if (cfg.key != code) {
          continue;
        }
      }

      if (!emisEmpty(cfg.btnObj)) {
        // 在新增修改 mode,其他 hotkey 應該鎖住,除了 ESC (Cancel) 和 Enter (Save)
        if ((this.masterTbl.sMode == "ADD" || this.masterTbl.sMode == "UPD")
            && !((cfg.name == "SAVE" ) || (cfg.name == "CANCEL"))) {
          continue;
        }

        if (!cfg.btnObj.disabled && cfg.btnObj.style.visibility != "hidden"
            && cfg.btnObj.style.display != "none")
        {
          // 2010/02/26 Joe.yao add: 處理當使用者按ESC鍵時提示儲存確認
          // 20140925 Joe.yao modify: 配合YmPrompt優化事件响应机制
          if (code == kbrd.KEY_ESC) { //27
            _oSrcElement = ev.target || ev.srcElement;
            ymPromptAnswer({
              msg:jQuery.getMessage("confirm_for_save_go_on"),
              dft: 1,
              type: "T",
              callback: function (_sAnswer) {
                var btnSave = formobj('btnSave');
                var btnCancel = formobj('btnCancel');
                if (/Yes/i.test(_sAnswer) && btnSave) {
                  // 是，觸發儲存
                  btnSave.focus();
                  jQuery(btnSave).click();
                  return true;
                }
                else if (/No/i.test(_sAnswer)  && btnCancel) {
                  // 否，觸發取消
                  btnCancel.focus();
                  jQuery(btnCancel).click();
                  return true;
                }
                else if (/Cancel/i.test(_sAnswer)) {
                  try {
                    // 取消
                    if (_oSrcElement) {
                      _oSrcElement.focus();
                    }
                  } catch (e) {
                  }
                  return false;
                }
                return true;
              }
            });
          }
          // 2012/11/19 Joe 当在TextArea或Textbox按下Del键时，不做任何处理
          else  if (code == kbrd.KEY_DEL) { //46
            try {
              _oSrcElement = ev.target || ev.srcElement;
              if(_oSrcElement.nodeName == "TEXTAREA" || /text/gi.test(_oSrcElement.getAttribute("type"))){
                return true;
              } else {
                jQuery(cfg.btnObj).click();
              }
            } catch (e) {
            }
          } else {
            jQuery(cfg.btnObj).click();
          }
          return true; // trigger 之後馬上 return , 之前有發現,因為 button 是觸動 ModalDialog
        }
      }
    }
    return false;
  }
  /**
   * 初始化處理
   * @param tdId
   * @param TableObject
   */
  this.init = function (tdId, TableObject, opts) {
    if (!emisEmpty(TableObject))
      TableObject.toolbar = this;

    // 2010/02/22 Joe.yao add : 自動設定權限
    if (!emisEmpty(toolbarRights))
      this.rights = toolbarRights;

    this.td = document.getElementById(tdId);

    if (emisEmpty(this.td)) {
      alert('toolbar creation need <td> id');
      return;
    }
    // 控制表身按钮
    this.__removeEditButtons(opts);
    // 改為配合Showdata標籤
    this.__loadButtons2Map();
  };
  // 当表头无新增，修改，删除按钮且表身无强制显示设定不处理时，
  // 默认不显示表身按钮：新增，修改，删除，整批，F10等编辑按钮
  // 如需自主控制，请以参数传入
  // var toolbar = masterTbl.createToolbar("idTD_BTN",{DetlButtons: [formobj("btnAdd"), formobj("btnUpd"), formobj("btnDel")]});
  // var toolbar = masterTbl.createToolbar("idTD_BTN",{ProcDispDetlBtn: false});
  this.__removeEditButtons = function (options) {
    if (parent && jQuery(parent.document).attr("EMIS_IS_BILL_MODE") === "Y") {
      var opts = jQuery.extend({DetlButtons: [formobj("btnAdd"), formobj("btnUpd"), formobj("btnDel"),
        formobj("btnBat3"), formobj("btnUpdAll"), formobj("btnF10"), formobj("btnBat")], ProcDispDetlBtn: true}, options);
      if (emisEmpty(typeof(window.parent.formobj("btnAdd")))
          && emisEmpty(typeof(window.parent.formobj("btnUpd")))
          && emisEmpty(typeof(window.parent.formobj("btnDel")))
          && opts.ProcDispDetlBtn) {
        jQuery.each(opts.DetlButtons, function () {
          jQuery(this).remove();
        });
      }
    }
  };
  /**
   * 設定功能權限
   * @param RightArray
   */
  this.setButtonRights = function(RightArray) {
    this.rights = RightArray;
  }
  /**
   * 設定在某種 mode 下需要 disable 的 buttons
   * toolbar 本身並不紀錄 mode, 只在 setMode 時做處理
   * @param sMode
   * @param btnArray
   */
  this.setDisable = function(sMode, btnArray) {
    this.disableBtnWithMode[sMode] = btnArray;
  }
  /**
   * 依Mode進行禁用處理邏輯
   * @param sMode
   */
  this.applyDisable = function(sMode) {
    var ary = this.disableBtnWithMode[sMode];
    if (emisEmpty(ary)) return;
    var cfg;

    for (var i = 0; i < ary.length; i++) {
      for (var j = 0; j < this.btnMap.length; j++) {
        cfg = this.btnMap[j];
        // 2010/02/22 Joe.yao modify : 修改判斷邏輯，避免空物件Error發生
        if (cfg && cfg.btnObj && ary[i] == cfg.name) {
          //AjaxDisable( cfg.btnObj );
          cfg.btnObj.disabled = true;
          if(cfg.imgObj) {
            cfg.imgObj.style.filter = "alpha(opacity=30)";    // IE
            cfg.imgObj.style.opacity = ".3";   /*Opera9.0+、Firefox1.5+、Safari、Chrome*/
          }
          break;
        }
      }
    }
  }
  /**
   * 把參數2的值覆蓋參數1的值，并返回最新值
   * @param cfg
   * @param attr
   */
  this.overWriteAttr = function (cfg, attr) {
    for(var item in attr){
      if(attr[item]!=undefined) {
        cfg[item] = attr[item];
      }
    }
    return cfg;
  }
  /**
   * 依畫面的按鈕產生Mapdding設定
   */
  this.__loadButtons2Map = function() {
    this.btns = this.td.getElementsByTagName("button");
    var btn_name;
    for (var i = 0; i < this.btns.length; i++) {
      btn_name = this.btns[i].id.substring(3).toUpperCase();
      if(btn_name=='QRY')
        jQuery(this.btns[i]).attr('defTitle',jQuery(this.btns[i]).attr('title'));
      var cfg ={
        name: btn_name,
        btnObj: this.btns[i],
        // 2012/05/23 Joe 修正在FF下读取Button中的Img对象错误
        //imgObj: document.getElementById("img" + btn_name),
        imgObj: this.btns[i].getElementsByTagName("img")[0],
        key: kbrd.str2key(btn_name),
        alt: btn_name=='CLOSE'?'true':''
      };

      this.btnMap.push(cfg);
      this.activeBtnMap.push(cfg);
      jQuery(cfg.btnObj)
          .mouseover(function(){jQuery(this).addClass('over');})
          .mouseout(function(){jQuery(this).removeClass('over');});
    }
  }
  /**
   * 創建提示訊息
   */
  this.__createTitle = function () {
    var i;
    var title = '';
    for (i = 0; i < this.btns.length; i++) {
      //Joe mark：不顯示或禁用的不出現在title
      if (!this.btns[i].disabled && this.btns[i].style.display != 'none') {
        title += ((i > 0 ? '\n' : '') + this.btns[i].title);
      }
    }
    this.mainTitle = title;
    this.td.title = title;
  }
  /**
   * 設定功能欄提示訊息，如有自定義函數可傳入取代公用并執行
   * @param setQueryTitle 作業自定算法
   */
  this.setMainTitle = function(setQueryTitle) {
    // 自定義的標題方法，一般情況下不建議使用
    if(typeof setQueryTitle == 'function'){
      setQueryTitle();
    }
    //joe add: 自動調用公用設定查詢條件的Title
    else if (emisSetQueryTitle){
      emisSetQueryTitle();
    }
    this.__createTitle();
    this.td.title = this.mainTitle;
  }
  /**
   * 設定 新增或修改 時 功能欄提示訊息(僅儲存與取消)
   */
  this.setSaveTitle = function() {
    //this.td.title = "儲存:[F10] 取消:[Esc]";
    this.td.title = jQuery.getMessage("save_cancel_title");
  }
  /**
   * 處理傳入之物件集是否顯示或隱藏
   * @param objArray
   * @param bShow true 顯示 ，false 隱藏
   */
  this.__displayBtns = function(objArray, bShow) {
    for (i = 0; i < objArray.length; i++) {
      // 2010/03/16 joe add : 當物件不存在時直接跳過，不做任何操作
      if(!objArray[i]) continue;
      if (bShow) {
        AjaxEnable(objArray[i], true);
        //objArray[i].style.visibility="visible";
        //objArray[i].style.display="";
        jQuery(objArray[i]).show();

      } else {
        jQuery(objArray[i]).hide();
        //objArray[i].style.visibility="hidden";
        //objArray[i].style.display="none";
      }
    }
  }
  /**
   * Toolbar模式設定邏輯
   * @param sMode
   */
  this.setMode = function (sMode) {
    if(this.procDetlButton()) return;
    //alert(sMode + ":" + src);
    if (sMode == '') {
      //this.setMainTitle();// Joe mark: 移到最后執行，解決Title少了部分按鈕的提示
      this.__displayBtns(this.btns, true);
      this.__displayBtns([formobj('btnSave'),formobj('btnCancel')], false);

      if (this.masterTbl.totalRowCount == 0) {
        this.applyDisable("EMPTY");
      }
      this.setMainTitle();
    }

    if (sMode == 'M') {
      //this.setMainTitle(); // Joe mark: 移到最后執行，解決Title少了部分按鈕的提示
      this.__displayBtns(this.btns, true);
      this.__displayBtns([formobj('btnSave'),formobj('btnCancel')], false);
      if (this.masterTbl.totalRowCount == 0) {
        this.applyDisable("EMPTY");
      }
      this.setMainTitle();
    }

    if (sMode == 'ADD' || sMode == "UPD" || sMode == "M_UPD") {

      this.setSaveTitle();
      this.__displayBtns(this.btns, false);
      this.__displayBtns([formobj('btnSave'),formobj('btnCancel')], true);
    }

    this.applyDisable(sMode);

    // 最後針對 user 權限做 disable
    this.buttonRights();
    this.procDetlButton();
  }

  // 新增,修改,刪除,報表, 這部份是從舊的 code 改過來的,沒有變動
  // 設定新增、修改...跟權限有關的
  this.aAddButton_ = ["formobj('btnAdd')","formobj('btnCopy')","formobj('btnInvo')","formobj('btnConf')"];
  this.aUpdButton_ = ["formobj('btnUpd')","formobj('btnInvo')","formobj('btnConf')"];
  this.aDelButton_ = ["formobj('btnDel')"];
  this.aRptButton_ = ["formobj('btnRpt')"];
  /**
   * 依權限設定對按鈕進行控管
   */
  this.buttonRights = function() {
    if (emisEmpty(this.rights)) return;
    if (this.rights[0] == "false") emisDisable(this.aAddButton_);
    if (this.rights[1] == "false") emisDisable(this.aUpdButton_);
    if (this.rights[2] == "false") emisDisable(this.aDelButton_);
    if (this.rights[3] == "false") emisDisable(this.aRptButton_);
    // 不可新增及修改時, 儲存功能 Disable
    if (this.rights[0] == "false" && this.rights[1] == "false") {
      emisDisable(["formobj('btnSave')"]);
    }
  }
  /**
   * 隱藏所有按鈕
   */
  this.hide = function () {
    var i;
    for (i = 0; i < this.btns.length; i++) {
      this.btns[i].style.visibility = "hidden";
    }
  }
  /**
   * 顯示所有按鈕
   */
  this.show = function () {
    var i;
    for (i = 0; i < this.btns.length; i++) {
      this.btns[i].style.visibility = "visible";
    }
  }
  /**
   * 檢查是否需要隱藏表身按鈕
   * 需在td增加一個自定義屬性來識別
   * <td id="idBtn" emis-type="detl">
   * @return {Boolean}
   */
  this.procDetlButton = function () {
    if(this.td && /detl/gi.test(this.td.getAttribute("emis-type"))){
      if (emisEmpty(typeof(window.parent.formobj('btnAdd'))) &&
          emisEmpty(typeof(window.parent.formobj('btnUpd'))) &&
          emisEmpty(typeof(window.parent.formobj('btnDel')))) {
        this.hide();
      } else {
        this.show();
      }
    }
  }

}

//=============================================================
//			start of declare navigator
//=============================================================
/**
 * 導航欄UI
 * style 分成兩種：0 = first,prev,next,last
 *               1 = first,prevpage,nextPage,last
 * @class This is the basic Navigator class.
 * @param spanId
 * @param linkTable
 * @param style
 */
function Navigator(spanId, linkTable, style) {

  var span = document.getElementById(spanId);
  if (emisEmpty(span)) return;

  /*if (style == "1") {
    span.innerHTML =
    "<IMG id='idTBLBrowFirstRec' value='首筆' alt='首筆' src='" + sRoot + "/images/firstrec.gif' border='0' onclick=' " + linkTable.varname + ".first();' TITLE='移至第一筆' style='cursor:hand'>" +
    "<IMG id='idTBLBrowPrevRec'  value='上筆' alt='上筆' src='" + sRoot + "/images/prevrec.gif'  border='0' onclick=' " + linkTable.varname + ".previous();' TITLE='移至上一筆' style='cursor:hand'>\n" +
    "<IMG id='idTBLBrowNextRec'  value='下筆' alt='下筆' src='" + sRoot + "/images/nextrec.gif'  border='0' onclick=' " + linkTable.varname + ".next();' TITLE='移至下一筆' style='cursor:hand'>" +
    "<IMG id='idTBLBrowLastRec'  value='末筆' alt='末筆' src='" + sRoot + "/images/lastrec.gif'  border='0' onclick=' " + linkTable.varname + ".last();'  TITLE='移至最末筆' style='cursor:hand'>";
  } else {
    // default
    span.innerHTML =
    "<IMG id='idTBLNaviFirstRec' value='首筆' alt='首筆' src='" + sRoot + "/images/firstrec.gif' border='0'  onclick='" + linkTable.varname + ".first();' TITLE='移至第一筆記錄' style='cursor:hand'>" +
    "<IMG id='idTBLNaviPrevPage' value='上頁' alt='上頁' src='" + sRoot + "/images/prevpage.gif' border='0'  onclick='" + linkTable.varname + ".prevPage();' TITLE='移至上一頁記錄' style='cursor:hand'>\n" +
//    "<IMG id='idTBLNaviPrevRec'  value='上筆' alt='上筆' src='" + sRoot + "/images/prevrec.gif'  border='0'  onclick='" + linkTable.varname + ".previous();' TITLE='移至上一筆記錄' style='cursor:hand'>&nbsp;" +
//    "<IMG id='idTBLNaviNextRec'  value='下筆' alt='下筆' src='" + sRoot + "/images/nextrec.gif'  border='0'  onclick='" + linkTable.varname + ".next();' TITLE='移至下一筆記錄' style='cursor:hand'>" +
    "<IMG id='idTBLNaviNextPage' value='下頁' alt='下頁' src='" + sRoot + "/images/nextpage.gif' border='0'  onclick='" + linkTable.varname + ".nextPage();' TITLE='移至下一頁記錄' style='cursor:hand'>" +
    "<IMG id='idTBLNaviLastRec'  value='末筆' alt='末筆' src='" + sRoot + "/images/lastrec.gif'  border='0'  onclick='" + linkTable.varname + ".last();' TITLE='移至最末筆記錄' style='cursor:hand'>";
  }*/
  if (style == "1") {
    span.innerHTML =
    "<A href='javascript:void(0);' onclick='return false;' hidefocus><IMG id='idTBLBrowFirstRec' src='" + sRoot + "/images/firstrec.gif' onclick='" + linkTable.varname + ".first();'    title='"+jQuery.getMessage('nav_first')+"'></A>" +
    "<A href='javascript:void(0);' onclick='return false;' hidefocus><IMG id='idTBLBrowPrevRec'  src='" + sRoot + "/images/prevrec.gif'  onclick='" + linkTable.varname + ".previous();' title='"+jQuery.getMessage('nav_prev_rec')+"'></A>" +
    "\n"+ // 用于實現右鍵開IE默認的右鍵菜單
    "<A href='javascript:void(0);' onclick='return false;' hidefocus><IMG id='idTBLBrowNextRec'  src='" + sRoot + "/images/nextrec.gif'  onclick='" + linkTable.varname + ".next();'     title='"+jQuery.getMessage('nav_next_rec')+"'></A>" +
    "<A href='javascript:void(0);' onclick='return false;' hidefocus><IMG id='idTBLBrowLastRec'  src='" + sRoot + "/images/lastrec.gif'  onclick='" + linkTable.varname + ".last();'     title='"+jQuery.getMessage('nav_last')+"'></A>";
  } else {
    // default
    span.innerHTML =
    "<A href='javascript:void(0);' onclick='return false;' hidefocus><IMG id='idTBLNaviFirstRec' src='" + sRoot + "/images/firstrec.gif' onclick='" + linkTable.varname + ".first();'    title='"+jQuery.getMessage('nav_first')+"'></A>" +
    "<A href='javascript:void(0);' onclick='return false;' hidefocus><IMG id='idTBLNaviPrevPage' src='" + sRoot + "/images/prevpage.gif' onclick='" + linkTable.varname + ".prevPage();' title='"+jQuery.getMessage('nav_prev_page')+"'></A>" +
    "\n"+ // 用于實現右鍵開IE默認的右鍵菜單
    "<A href='javascript:void(0);' onclick='return false;' hidefocus><IMG id='idTBLNaviNextPage' src='" + sRoot + "/images/nextpage.gif' onclick='" + linkTable.varname + ".nextPage();' title='"+jQuery.getMessage('nav_next_page')+"'></A>" +
    "<A href='javascript:void(0);' onclick='return false;' hidefocus><IMG id='idTBLNaviLastRec'  src='" + sRoot + "/images/lastrec.gif'  onclick='" + linkTable.varname + ".last();'     title='"+jQuery.getMessage('nav_last')+"'></A>";
  }
  //this.imgs = span.getElementsByTagName("img");
  this.imgs = span.getElementsByTagName("A");
  this.imgCount = this.imgs.length;

  /**
   * Navigator 模式處理邏輯
   * @param sMode
   */
  this.setMode = function (sMode) {
    if (sMode == "" || sMode == "M" || sMode == "PARENT_EMPTY") {
      this.show();
      return;
    }
    if (sMode == "M_UPD") {
      this.show();
      return;
    }

    this.hide();
  }

  // hide still occupy the object space, you need to use 'none' if you don't want to 'hide'
  /**
   * 隱藏導航欄
   */
  this.hide = function () {
    var i;
    for (i = 0; i < this.imgCount; i++) {
      this.imgs[i].style.visibility = "hidden";
    }
  }
  /**
   * 顯示導航欄
   */
  this.show = function () {
    var i;
    for (i = 0; i < this.imgCount; i++) {
      this.imgs[i].style.visibility = "visible";
    }
  }
}


//=============================================================
//			start of declare keyboard
//=============================================================
/*
 45=Insert, 13=Enter, 46=Del, 115:F4 Print
 113=F2:Query, 117=F6:Copy, 119:F8 QQry, 121=F10:DownRec
 113=F2:Bat 表身之整批帶入
 117=F6:SQry 單品資料維護門市查詢
 117=F6:Conf 進貨驗收之確認驗收
 118=F7:Invo 進貨驗收之發票補登
 117=F6:Settle 撥出單之結案
*/
/**
 * 鍵盤事件處理
 * @class This is the basic keyboard class.
 */
function keyboard() {
  // const define
  this.KEY_ESC = 27;
  this.KEY_TAB = 9;
  // 導航欄使用
  this.KEY_UP = 38;
  this.KEY_DOWN = 40;
  this.KEY_LEFT = 37;
  this.KEY_RIGHT = 39;
  this.KEY_PAGEUP = 33;
  this.KEY_PAGEDOWN = 34;
  this.KEY_HOME = 36;
  this.KEY_END = 35;

  this.KEY_INSERT = 45;
  this.KEY_ENTER = 13;
  this.KEY_DEL = 46;

  this.KEY_F = 70;
  this.KEY_G = 71;
  this.KEY_R = 82;
  this.KEY_S = 83;

  this.KEY_F2 = 113;
  this.KEY_F3 = 114;
  this.KEY_F4 = 115;
  this.KEY_F5 = 116;
  this.KEY_F6 = 117;
  this.KEY_F7 = 118;
  this.KEY_F8 = 119;
  this.KEY_F9 = 120;
  this.KEY_F10 = 121;
  this.KEY_F11 = 122;
  this.KEY_F12 = 123;
  /**
   * 鍵值對照表
   * @param str
   */
  this.keyMapping = {
    'QRY': this.KEY_F2,
    'ADD': this.KEY_INSERT,
    'UPD': this.KEY_ENTER,
    'DEL': this.KEY_DEL,
    'RPT': this.KEY_F4,
    'QQRY': this.KEY_F8,
    'DOWNREC': this.KEY_F10,
    'F6': this.KEY_F6,
    'F7': this.KEY_F7,
    'F9': this.KEY_F9,
    'F10': this.KEY_F10,
    'F12': this.KEY_F12,
    'CLOSE': this.KEY_F4,
    'SAVE': this.KEY_F10,
    'CANCEL': this.KEY_ESC
  };
  /**
   * 依傳入鍵名取得相應鍵值
   * @param str
   */
  this.str2key = function(str){
    // 2015/05/05 Joe Fix 在FireFox下有中文输入法时KeyCode为0，导致判断错误
    return this.keyMapping[str] || -1;
  }
  /**
   * 依傳入鍵值取得相應鍵名
   * @param code
   */
  this.key2Str = function (code) {
    switch (code) {
      case this.KEY_ESC:
        return 'ESC';
      case this.KEY_TAB:
        return 'TAB';
      case this.KEY_INSERT:
        return 'INSERT';
      case this.KEY_ENTER:
        return 'ENTER';
      case this.KEY_DEL:
        return 'DEL';
      case this.KEY_F2:
        return 'F2';
      case this.KEY_F3:
        return 'F3';
      case this.KEY_F4:
        return 'F4';
      case this.KEY_F5:
        return 'F5';
      case this.KEY_F6:
        return 'F6';
      case this.KEY_F7:
        return 'F7';
      case this.KEY_F8:
        return 'F8';
      case this.KEY_F9:
        return 'F9';
      case this.KEY_F10:
        return 'F10';
      case this.KEY_F11:
        return 'F11';
      case this.KEY_F12:
        return 'F12';
      default:
        return '';
    }
  }
  /**
   * 是否允許按F5或Ctrl+R進行頁面刷新
   */
  this.bRefreshEnable = true;
  /**
   * 檢查是否按下CTrl+R
   */
  this.is_CTRL_R = function (ev) {
    ev = ev || window.event;
    if (ev.ctrlKey && ev.keyCode == this.KEY_R) {
      return true;
    }
    return false;
  }
  /**
   * 檢查是否按下Alt鍵
   */
  this.is_ALT = function (ev) {
    return (ev || window.event).altKey;
  }
  /**
   * 檢查是否按下CTRL鍵
   */
  this.is_CTRL = function (ev) {
    return (ev || window.event).ctrlKey;
  }
  /**
   * 檢查是否按下SHIFT鍵
   */
  this.is_SHIFT = function (ev) {
    return (ev || window.event).shiftKey;
  }

  /**
   * 終止事件冒泡
   * @param ev
   */
  this.captureKeyEvent = function(ev) {
    ev = ev || window.event;
    if (ev.preventDefault) {
      ev.preventDefault();
      //ev.stopPropagation();
    } else {
      ev.returnValue = false;
      ev.cancelBubble = true;
    }
    ev.keyCode = 0;
  }
  /**
   * 設定控制是否允許按F5或Ctrl+R進行頁面刷新
   * this will control whether the F5 and Ctrl-R can be used ( refresh)
   * @param bEnable
   */
  this.setRefresh = function(bEnable) {
    this.bRefreshEnable = bEnable;
  }
  /**
   * 事件處理邏輯
   */
  this.processKey = function(ev) {
    ev = ev || window.event;
    if(!ev) return ;
    
    var elm = ev.target || ev.srcElement;
    var code = ev.keyCode || ev.which;
    // Ajax Debug軟開關 （CTRL+ALT+G）
    if(this.is_CTRL(ev) && this.is_ALT(ev) && code == this.KEY_G){
      if (typeof bDebugAjax != 'undefined')
        bDebugAjax = bDebugAjax ? false : true;
      
      this.captureKeyEvent(ev);
      return false;
    }
    // 列印Debug軟開關 （CTRL+ALT+R）
    if(this.is_CTRL(ev) && this.is_ALT(ev) && code == this.KEY_R){
      if (typeof bDebug_ != 'undefined')
        bDebug_ = bDebug_ ? false : true;

      this.captureKeyEvent(ev);
      return false;
    }
    // 开发模式軟開關（CTRL+ALT+F）
    if(this.is_CTRL(ev) && this.is_ALT(ev) && code == this.KEY_F){
      if (typeof isDevelop != 'undefined')
        isDevelop = isDevelop ? false : true;

      this.captureKeyEvent(ev);
      return false;
    }
    if (!this.bRefreshEnable) {
      if (code == this.KEY_F5) {
        window.alert(jQuery.getMessage('f5_disabled_msg')); //請勿使用F5重新整理頁面
        this.captureKeyEvent(ev);
        return false;
      } else if (this.is_CTRL_R(ev)) {
        window.alert(jQuery.getMessage('ctrl_r_disabled_msg'));  //請勿使用 Ctrl-R 重新整理頁面
        this.captureKeyEvent(ev);
        return false;
      }
    /*} else {
      if (code == this.KEY_F5) {
        try {
          reload(true);  //2013/05/31  add by Jim 添加true 修正若表頭為結案狀態，表身按F5 ，表身按鈕被禁用的會被開啟。
          this.captureKeyEvent(ev);
          return false;
        } catch (e) {
          return true;
        }
      }*/
    }

    // 查詢畫面或列印畫面等非主畫面Event處理
    //if (emisEmpty(currentTable)) {
      // 2010/02/20 joe.yao add: 查询，新增，修改等非主窗体事件回应
      // 取消(C)、關閉(C)
      if (code == this.KEY_ESC) {
        var btnObj = ( formobj("btnClose") || formobj("btnCancel") );
        if (btnObj && !btnObj.disabled && btnObj.style.visibility != "hidden"
            && btnObj.style.display != "none"
            && (window.dialogArguments || (btnObj.title||"").toUpperCase().indexOf("[ALT-F4]")==-1 ))
        {
          jQuery(btnObj).click();
          return true;
        }
      }
      // F10: 確定(Y), 儲存(S)
      else if (code == this.KEY_F10) {
        var btnObj = ( docobj("btnOK") || docobj("btnSave") );
        if (btnObj && !btnObj.disabled && btnObj.style.visibility != "hidden"
            && btnObj.style.display != "none")
        {
          // 2013/08/14 Joe 解决按快捷键时，无法触发Text等物件的Blur事件，导致有的计算逻辑没有进行
          //jQuery(btnObj).click();
          jQuery(btnObj).focus();  //防止使用快捷鍵時沒觸發某些欄位的onblur事件
          setTimeout(function(){ jQuery(btnObj).click();}, 5);
          this.captureKeyEvent(ev);
          return true;
        }
      }
      // 2013/08/15 Joe add 為了配合系統控制Alt - S/Y 儲存(S), 確定(Y) 不觸發系統默認事件(依賴ajax_inc.htm的預處理)
      // ALT - S/Y  儲存(S), 確定(Y)
      else if (!this.is_CTRL(ev) && !this.is_SHIFT(ev) && this.is_ALT(ev)
          && ( code == "S".charCodeAt(0) || code == "Y".charCodeAt(0) )) {
        var btnObj = ( docobj("btnOK") || docobj("btnSave") );
        if (btnObj && !btnObj.disabled && btnObj.style.visibility != "hidden"
            && btnObj.style.display != "none" && /S|Y/gi.test(btnObj.getAttribute("emis-accesskey")))
        {
          // 2013/08/14 Joe 解决按快捷键时，无法触发Text等物件的Blur事件，导致有的计算逻辑没有进行
          //jQuery(btnObj).click();
          jQuery(btnObj).focus();  //防止使用快捷鍵時沒觸發某些欄位的onblur事件
          setTimeout(function () { jQuery(btnObj).click(); }, 5);
          this.captureKeyEvent(ev);
          return true;
        }
      }
    //}
    //使用键盘进行操作时,遇到跳页不会触发elm对象的onchange事件和onblur事件,导致资料未保存到MasterTable中
    if ((code == this.KEY_UP || code == this.KEY_DOWN || code == this.KEY_LEFT || code == this.KEY_RIGHT
        || code == this.KEY_HOME || code == this.KEY_END || code == this.KEY_PAGEUP || code == this.KEY_PAGEDOWN)
        && !emisEmpty(currentTable) && (currentTable.sMode == '' || currentTable.sMode == 'M' || currentTable.sMode == 'M_UPD')
        && (elm.nodeName != "TEXTAREA") && (elm.nodeName != "SELECT")) {
      // 2013/01/28 Joe 增加检查，当遇到有树控件的界面不使用键盘导航功能
      if( typeof _bDisabledNavigationOnTree != "undefined" && _bDisabledNavigationOnTree) return true;

      if (code == this.KEY_UP) {
        currentTable.previous(elm);
      } else if (code == this.KEY_DOWN) {
        currentTable.next(elm);
      } else if ((code == this.KEY_LEFT || code == this.KEY_PAGEUP )&& document.getElementById('idTBLNaviPrevPage')) {
        currentTable.prevPage(elm);
      } else if ((code == this.KEY_RIGHT || code == this.KEY_PAGEDOWN) && document.getElementById('idTBLNaviNextPage')) {
        currentTable.nextPage(elm);
      } else if (code == this.KEY_HOME) {//idTBLBrowLastRec idTBLNaviFirstRec
        currentTable.first(elm);
      } else if (code == this.KEY_END) {//idTBLBrowLastRec idTBLNaviLastRec
        currentTable.last(elm);
      }
      return true;
    }
    else if (code == this.KEY_ENTER && elm.type == "button") {
      // trigger the current focus button
      //jQuery(elm).click();
      // 2013/07/02 增加延时触发，避免Chrome及FF下连续触发两次
      setTimeout(function () {jQuery(elm).click();}, 0);
      this.captureKeyEvent(ev);
      return true;
    }
    // 2010/03/17 joe fix : 修正每頁筆數Enter鍵錯誤問題
    else if (code == this.KEY_ENTER && elm.id == "idTBLspanPageSize") {
      //ev.keyCode = this.KEY_TAB;
      convertEnterToTab(ev);
      window.focus();
      return true;
    }
    else if (!emisEmpty(currentTable) && !emisEmpty(currentTable.toolbar)
        && currentTable.toolbar.processKey(ev)) { // button hot keys
      if (code == this.KEY_F10 || code == this.KEY_F4 || code == this.KEY_F12) {
        this.captureKeyEvent(ev);
      }
      return true;
    }
    else if (((code == this.KEY_ENTER) || (code == this.KEY_TAB)) &&
             (elm.type != "button") && (elm.nodeName != "TEXTAREA")
        )
    {
      var hasNext = true;
      // 處理多個不確定欄位onblur后 Focus 到 BtnSave上
      if (!emisEmpty(currentTable) && !emisEmpty(currentTable.detlTable)){
        var EOFObjs = currentTable.detlTable.EOFObjects;
        var index = EOFObjs.indexOf(elm);
        if(index != -1){
          hasNext = false;
          while(++index < EOFObjs.length){
            if(!EOFObjs[index].disabled){
              hasNext = true;
              break;
            }
          }
        }
      }
      if(hasNext){
        //ev.keyCode = this.KEY_TAB;
        convertEnterToTab(ev);
      } else{
        try {
          docobj('btnSave').focus();
          this.captureKeyEvent(ev);
        } catch (e) {
        }
      }
//      window.event.keyCode = this.KEY_TAB;
      return true;
    }
    else
    {
      if(code == this.KEY_ESC){
        this.captureKeyEvent(ev);
      }
      return true;
    }
  } // end of this.processKey

}

//=============================================================
//			util function of url
//              方便把 url   act=xxx&TITLE=yyy&..... 拼出來
//=============================================================
/**
 * 用于組織URL參數功能類
 * 為了保持輸入的順序,所以用了兩個 array
 * @class This is the basic emisUrl class.
 */
function emisUrl() {
  var items = new Array();
  var mapping = new Array();

  /**
   * 增加一組參數（無轉碼）
   * @param name
   * @param value
   */
  this.add = function (name, value) {
    items[ items.length ] = name;
    mapping[name] = value;
  }
  /**
   * 增加一組參數（有轉碼）
   * @param name
   * @param value
   */
  this.add_encode = function (name, value) {
    this.add(name, encodeURIComponent(value));
  }
  /**
   * 清空參數
   */
  this.clean = function () {
    this.clear();
  }
  /**
   * 清空參數
   */
  this.clear = function () {
    items.length = 0;
    mapping.length = 0;
  }
  /**
   * 取得參數組成的URL
   */
  this.toString = function() {
    var i;
    var name;
    var value;
    var url = "";
    for (i = 0; i < items.length; i++) {
      name = items[i];
      value = mapping[name];
      // for chinese UTF8 handling
      //value = encodeURIComponent(value);
      if (i == 0)
        url += (name + "=" + value);
      else
        url += ("&" + name + "=" + value);
    }
    return url;
  }
}