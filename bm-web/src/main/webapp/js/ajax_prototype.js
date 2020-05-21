/**
 * 用于实现Javascript Core现在对象的扩展方法的合集
 */
// ************************** String *************************
// 設定字傳型態的外加功能
if (!String.prototype.emisSepYearMonth) {
  // 函數說明 : 將年度月份轉為有分隔符號的月份
  // parameter1: bDateType  => true:西曆 , fasle;國曆
  // parameter2: sSep => 分隔符號字元 , 預設值: /
  // 傳回值: 加入分隔符號後的月份
  //功能說明:將年度月份轉為有分隔符號的月份
  //       參數說明:
  //			 bDateType  => true:西曆 , fasle;國曆
  //       sSep => 分隔符號字元 , 預設值: /
  //       傳回值: 加入分隔符號後的月份
  String.prototype.emisSepYearMonth = function () {
    var _sRetval = this.toString();

    if (bDateType_) {
      _sRetval = _sRetval.substr(0, 4) + sDateSepa_ + _sRetval.substr(4, 2);
    } else {
      _sRetval = _sRetval.substr(0, 3) + sDateSepa_ + _sRetval.substr(3, 2);
    }

    return _sRetval;
  };
}
if (!String.prototype.emisUnDateSep) {
  // 函數說明: 將年度月份有分隔符號的轉成無分隔符號
  // parameter1: 參數說明:bDateType  => true:西曆 , fasle;國曆
  // parameter2: 參數說明:sSep => 分隔符號字元 , 預設值:1
  // 傳回值:加入分隔符號後的月份
  String.prototype.emisUnDateSep = function () {
    var _sRetval = this.toString();
    if (sDateSepa_ != "")
      _sRetval = _sRetval.replace(new RegExp(sDateSepa_, "g"), "");

    return _sRetval;
  };
}

if (!String.prototype.emisDateSep) {
  // 函數說明 :將分隔符號加入
  // parameter1: 參數說明:sSep: 日期格式分隔字, 不傳預設為 ""
  // 傳回值:_sRetval
  String.prototype.emisDateSep = function () {
    var _sRetval = this.toString();

    if (!emisEmpty(_sRetval, sDateSepa_)) _sRetval = emisDate(_sRetval);
    return _sRetval;
  };
}

if (!String.prototype.emisStuff) {
  // 函數說明 : 將某位置得字串置換成想要的字串
  // parameter1: 參數說明 iIndex = 位址
  // parameter2: 參數說明 sString = 字串
  // 傳回值:
  String.prototype.emisStuff = function (iIndex, sString) {
    var _sRetval = this.toString();
    var _sPrefix = _sRetval.substring(0, iIndex);
    var _sPosfix = _sRetval.substring(iIndex);

    if (_sPosfix.length <= sString.length) {
      _sRetval = _sPrefix + sString;
    } else {
      _sRetval = _sPrefix + sString + _sPosfix.substring(sString.length);
    }

    return _sRetval;
  };
}
if (!String.prototype.In) {
  //判斷一個值是否存在,等同於SQL的IN,add by zhong.xu 20080826
  //例:'aa'.In('bb','cc','aa')=true;
  //   'aa'.In(['bb','cc','aa'])=true;
  //   new Number(12).In('12,23,34'.split(',')) = true
  String.prototype.In = function () {
    var con = arguments[0].constructor;//第一個參數的類型
    var len = 0, value = this;
    if (this.constructor == Array) {//對像本身是數組
      len = this.length;
      con = this;
      value = arguments[0];
    } else if (con == Array) {
      len = arguments[0].length;
      con = arguments[0];
    } else if (con == String || con == Number) {
      len = arguments.length;
      con = arguments;
    }
    while (len--)
      if (con[len] == value) return true;
    return false;
  };
}
if (!String.prototype.notIn) {
  String.prototype.notIn = function () {
    return !this.In.apply(this, arguments);
  };
}
// ************************** Number *************************
if (!Number.prototype.In) {
  Number.prototype.In = function () {
    return String.prototype.In.apply(this, arguments);
  };
}

if (!Number.prototype.notIn) {
  Number.prototype.notIn = function () {
    return String.prototype.notIn.apply(this, arguments);
  };
}

// ************************** Array *************************
//给Array对象原型上添加contains方法.(如果没有的话)
if (!Array.prototype.contains) {
  Array.prototype.contains = function(obj) {
    var i = this.length;
    while (i--) {
      if (this[i] === obj) {
        return true;
      }
    }
    return false;
  };
}
//给Array对象原型上添加indexOf方法.(如果没有的话)
if (!Array.prototype.indexOf) {
  Array.prototype.indexOf = function(element, index) {
    var length = this.length;
    if (index == null) {
      index = 0;
    } else {
      index = +index || 0;
      if (index < 0) index += length;
      if (index < 0) index = 0;
    }
    for (var current; index < length; index++) {
      current = this[index];
      if (current === element) return index;
    }
    return -1;
  };
}
//给Array对象原型上添加lastIndexOf方法.(如果没有的话)
if (!Array.prototype.lastIndexOf) {
  Array.prototype.lastIndexOf = function(element, index) {
    var length = this.length;
    if (index == null) {
      index = length - 1;
    } else {
      index = +index || 0;
      if (index < 0) index += length;
      if (index < 0) index = -1;
      else if (index >= length) index = length - 1;
    }
    for (var current; index >= 0; index--) {
      current = this[index];
      if (current === element) return index;
    }
    return -1;
  };
}