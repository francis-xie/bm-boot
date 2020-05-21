package com.emis.report;

/**
 * 功能：運算式類別
 * 說明：提供運算子及運算元之定義，並據此定義運算出結果
 * @author James Lee
 * @version 1.00, 07/09/01'
 *
 */
public class emisRptEquation {

  private emisRptField o1stRightOperand_ = null;
  private emisRptField o2ndRightOperand_ = null;
  private emisRptField oLeftOperand_ = null;

  private int iOperator_;

  public static final int NONE = 0;
  public static final int ADD = 1;
  public static final int SUBTRACT = 2;
  public static final int MULTIPLY = 3;
  public static final int DIVIDE = 4;
  public static final String OPERATOR = "()+-*/";

  /**
  * 目的: 建構元
  */
  public emisRptEquation() {

  }

  /**
  * 目的: 建構元
  * @param  oLeftOperand 左邊運算子
  * @param  o1stRightOperand 右邊第一個運算子
  * @param  iOperator 運算元代碼
  * @param  o2ndRightOperand 右邊第二個運算子
  */
  public emisRptEquation(emisRptField oLeftOperand,
                         emisRptField o1stRightOperand,
                         int iOperator,
                         emisRptField o2ndRightOperand) {

    o1stRightOperand_ = o1stRightOperand;
    o2ndRightOperand_ = o2ndRightOperand;
    oLeftOperand_ = oLeftOperand;
    iOperator_ = iOperator;

  }

  /**
  * 目的: 設定運算子
  * @param  iPosition 運算子位置, 0 表左邊, 1 表右邊第一個, 2 表右邊第二個
  * @param  oOperand 運算子之欄位物件
  * @return true/false 成功與否
  */
  public boolean setOperand(int iPosition, emisRptField oOperand) {
    boolean _isSetupOk = false;

    switch (iPosition) {
      case 0:
        oLeftOperand_= oOperand;
        break;
      case 1:
        o1stRightOperand_ = oOperand;
        _isSetupOk = true;
        break;
      case 2:
        o2ndRightOperand_ = oOperand;
        _isSetupOk = true;
        break;
      default:
        _isSetupOk = false;
        break;
    }

    return _isSetupOk;
  }

  /**
  * 目的: 取得運算元之代碼
  * @param  sOpeartor 運算元字串
  * @return 運算元代碼
  */
  public static int getOperatorType(String sOperator) {
    int _iOperator;

    if ("+".equals(sOperator)) {
      _iOperator = ADD;
    } else if ("-".equals(sOperator)) {
      _iOperator = SUBTRACT;
    } else if ("*".equals(sOperator)) {
      _iOperator = MULTIPLY;
    } else if ("/".equals(sOperator)) {
      _iOperator = DIVIDE;
    } else {
      _iOperator = 0;
    }

    return _iOperator;
  }

  /**
  * 目的: 檢查運算式是否合法
  * @return true/false 合法與否
  */
  public boolean isValid() {
    boolean _isValid = true;

    // 判斷 operands & operator 是否合法

    return _isValid;
  }

  /**
  * 目的: 執行運算
  * @return 運算的結果
  */
  public double getResult() {
    double _dOperand1;
    double _dOperand2;
    double _dResult = 0.0;

    _dOperand1 = o1stRightOperand_.getNumber();
    _dOperand2 = o2ndRightOperand_.getNumber();

    switch (iOperator_) {
      case ADD:
        _dResult = _dOperand1 + _dOperand2;
        break;
      case SUBTRACT:
        _dResult = _dOperand1 - _dOperand2;
        break;
      case MULTIPLY:
        _dResult = _dOperand1 * _dOperand2;
        break;
      case DIVIDE:
        if (_dOperand2 != 0.0) {
          _dResult = _dOperand1 / _dOperand2;
        } else {
          _dResult = 0;
        }
        break;
    }

    // set value into left operand
    oLeftOperand_.setContent(_dResult);

    return _dResult;
  }

  /**
  * 目的: 取得運算式之公式字串
  * @return 運算式字串
  */
  public String toString() {
    String _sOperator;

    switch (iOperator_) {
      case ADD:
        _sOperator = "+";
        break;
      case SUBTRACT:
        _sOperator = "-";
        break;
      case MULTIPLY:
        _sOperator = "*";
        break;
      case DIVIDE:
        _sOperator = "/";
        break;
      default:
        _sOperator = "";
    }

    return  oLeftOperand_.getName() + "=" + o1stRightOperand_.getName() +
            _sOperator + o2ndRightOperand_.getName();
  }

}


