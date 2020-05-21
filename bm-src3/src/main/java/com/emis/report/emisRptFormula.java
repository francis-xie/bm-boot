package com.emis.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;


/**
 * 功能：運算公式類別
 * 說明：提供運算子及運算元之定義，並據此定義運算出結果
 * @author James Lee
 * @version 1.00, 07/09/01'
 *
 */
public class emisRptFormula {

  private ArrayList oEquations_; // collection of emisRptEquation objects
  private HashMap oRegisters_ ;   // collection of emisRptField objects
  private int iRegisterCnt_ = 0;
  private String sFormula_ = null;

  /**
   * 目的: 建構元
   */
  public emisRptFormula() {
  }

  /**
   * 目的: 解析運算公式字串是否合法，並儲存之
   * @param  oResultField 儲存運算結果的欄位物件
   * @param  sFormula 運算公式字串
   * @param  oReportInfo 有關欄位物件等報表資訊之提供物件
   * @return true/false 合法與否
   */
  public boolean parseFormula(emisRptField oResultField,
                              String sFormula,
                              emisReportInfo oReportInfo) throws RuntimeException  {

    Stack _oTokenStack = new Stack();
    String _sToken;
    boolean _isValid = true;
    emisRptField _oField=null;
    emisRptField _o1stOperand;
    emisRptField _o2ndOperand;
    int _iOperator;
    emisRptEquation _oEquation;


    // 重新起始 oEquations_ 物件
    StringTokenizer st = new StringTokenizer(sFormula, emisRptEquation.OPERATOR, true);
    oEquations_ = new ArrayList();
    sFormula_ = sFormula;

    // Parsing 運算式
    while (st.hasMoreTokens()) {
      _sToken = st.nextToken().trim();

      // 處理 "("  運算子
      if ( "(".equals(_sToken) ) {
        continue;

      // 處理 ")"  運算子
      } else if ( ")".equals(_sToken) ) {
        // 產生 暫存變數
        if ( st.hasMoreTokens() || _oTokenStack.size() > 3) {
          if ((_oField = _getNewField()) == null) {
            _isValid = false;
          }
        } else {
          _oField = oResultField;
        }

        // 產生計算式
        if ( _isValid) {
          _o2ndOperand = (emisRptField) _oTokenStack.pop();
          _iOperator = emisRptEquation.getOperatorType((String) _oTokenStack.pop());
          _o1stOperand = (emisRptField) _oTokenStack.pop();
          _oEquation = new emisRptEquation(_oField, _o1stOperand, _iOperator,
                                           _o2ndOperand);
          oEquations_.add(_oEquation);
          if (st.hasMoreTokens() || _oTokenStack.size() > 0) {
            _oTokenStack.push(_oField);
          }
        } else {
          break;
        }

      // 處理 "+-*/" 等 運算子
      } else if ( emisRptEquation.OPERATOR.indexOf(_sToken) > 0 ) {
        _oTokenStack.push(_sToken);

      // PROCESS DIGITAL
      } else if ("x0123456789".indexOf(_sToken.charAt(0)) > 0 ) {
        if ((_oField = _getNewField()) == null) {
          _isValid = false;
        }

        // Push register into stack
        if ( _isValid) {
          _oField.setContent(_sToken);
          _oTokenStack.push(_oField);
        } else {
          break;
        }

      // 處理欄位變數
      } else {
        _oField = _fetchField(_sToken, oReportInfo);
        if ( _oField != null ) {
          _oTokenStack.push(_oField);
        } else {
          _isValid = false;
          break;
        }
      }
    }

    // 產生最後一個運算式
    if (_isValid ) {
      if (!_oTokenStack.empty()) {
        _o2ndOperand = (emisRptField) _oTokenStack.pop();
        _iOperator = emisRptEquation.getOperatorType((String) _oTokenStack.pop());
        _o1stOperand = (emisRptField) _oTokenStack.pop();
        _oEquation = new emisRptEquation(oResultField, _o1stOperand, _iOperator,
                                         _o2ndOperand);
        oEquations_.add(_oEquation);
      }
    } else {
      oEquations_ = null;
      oRegisters_ = null;
      /*
      throw new RuntimeException("parseFormula error in " +
                                 oResultField.getName()+" field: " +
                                 sFormula);
      */
    }

    return _isValid;
  }

  /**
   * 目的: 產生暫存的欄位物件
   * @return 欄位物件
   */
  private emisRptField _getNewField() {
    String sName;
    emisRptField _oField;

    if ( oRegisters_ == null) {
      oRegisters_ = new HashMap();
    }

    iRegisterCnt_ ++;
    sName = "REG" + String.valueOf(iRegisterCnt_);
    while ((_oField = (emisRptField) oRegisters_.get(sName)) != null) {
      iRegisterCnt_ ++;
      if (iRegisterCnt_ > 100) {
        return null;
      } else {
        sName = "REG" + String.valueOf(iRegisterCnt_);
      }
    }
    if (_oField == null) {
      _oField = new emisRptNumber();
      _oField.setName(sName);
    }

    return _oField;
  }

  /**
   * 目的: 取得欄位物件
   * @param  sFieldName 欄位名稱
   * @param  oReportInfo 有關欄位物件等報表資訊之提供物件
   * @return 欄位物件，若找不到則傳回 null
   */
  private emisRptField _fetchField(String sFieldName, emisReportInfo oReportInfo) {
    emisRptField _oField;

    // 先由本物件找出欄位物件
    if ( oRegisters_ != null) {
      _oField = (emisRptField) oRegisters_.get(sFieldName);
    } else {
      _oField = null;
    }

    // local 找不到改至 oReportInfo 找
    if ( _oField == null) {
      _oField = oReportInfo.fetchField(sFieldName);
    }

    return _oField;
  }

  /**
   * 目的: 檢查本運算公式是否合法
   * @return true/false 合法與否
   */
  public boolean isValid() {
    boolean _isOk = true;

    if (oEquations_ ==  null) {
      _isOk = false;
    }

    return _isOk;
  }

  /**
   * 目的: 設定運算公式
   * @param  oLeftOperand 左邊運算子
   * @param  o1stRightOperand 右邊第一個運算子
   * @param  iOperator 運算元代碼
   * @param  o2ndRightOperand 右邊第二個運算子
   * @return true/false 合法與否
   */
  public boolean setFormula(emisRptField oLeftOperand,
                              emisRptField o1stRightOperand,
                              int iOperator,
                              emisRptField o2ndRightOperand) {

    emisRptEquation _oEquation = new emisRptEquation(oLeftOperand,
                                 o1stRightOperand, iOperator, o2ndRightOperand);

    oEquations_ = new ArrayList();
    oEquations_.add(_oEquation);
    // convert equation into sFormula_ string

    return true;
  }

  /**
   * 目的: 驗證運算公式的正確性，檢查相關的運算子的欄位物件是否存在
   * @param  oLeftOperand 左邊運算子
   * @param  oReportInfo 有關欄位物件等報表資訊之提供物件
   * @return true/false 合法與否
   */
  public boolean justifyFormula(emisRptField oLeftOperand,
                                emisReportInfo oReportInfo)  throws RuntimeException {
    boolean _isValid;

    if (! isValid()) {
      _isValid = parseFormula(oLeftOperand, sFormula_, oReportInfo);
      if ( ! _isValid) {
        throw new RuntimeException("parseFormula error in " +
                                 oLeftOperand.getName()+" field: " +
                                 sFormula_);
      }
    } else {
      _isValid = true;
    }

    return _isValid;
  }

  /**
   * 目的: 執行運算得到結果
   * @return 運算結果
   */
  public double getResult() {
    double _dResult = 0.0;
    boolean _isValid = true;
    emisRptEquation _oEquation;

    _isValid = true;
    for (int i=0; i < oEquations_.size(); i++) {
      _oEquation = (emisRptEquation) oEquations_.get(i);
      if ( ! _oEquation.isValid()) {
        _isValid = false;
        break;
      }
    }
    if (_isValid) {
      for (int i=0; i < oEquations_.size(); i++) {
        _oEquation = (emisRptEquation) oEquations_.get(i);
        _dResult = _oEquation.getResult();
      }
    }

    return _dResult;
  }

  /**
   * 目的: 列出運算公式的每個運算式字串
   */
  public void listFormula() {
    emisRptEquation _oEquation;
    if (oEquations_.size() > 0) {
      for (int i = 0; i < oEquations_.size(); i++) {
        _oEquation = (emisRptEquation) oEquations_.get(i);
        System.out.println(_oEquation.toString());
      }
    } else {
      System.out.println("No Formula");
    }
  }
}
