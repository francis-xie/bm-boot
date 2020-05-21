/**
 * Created by IntelliJ IDEA.
 * User: jacky
 * Date: Apr 8, 2003
 * Time: 9:48:10 AM
 * To change this template use Options | File Templates.
 * 2004/02/19 Jacky 增加整數一位小數二位的格式
 * 2010/06/04 lisa.huang 增加整數位數和小數位數自定義的格式
 */
package com.emis.taglibs.showdata;



public class emisValidFactory {
  public emisValidFactory(){
  }

  public emisValidFormat  getValidFormat(String sValidType){
    emisValidFormat _oRetPattern = null ;
    /*if(sValidType!=null&&sValidType.startsWith("GENERAL")){
        return new emisNumberGeneralFormat(sValidType);
    }
    if ("NUMBER1_2".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber12Format();
    }
    if ("NUMBER1_4".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber14Format();
    }
    if ("NUMBER2_2".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber22Format();
    }
    if ("NUMBER3_4".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber34Format();
    }
    if ("NUMBER4_2".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber42Format();
    }
    if ("NUMBER5_2".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber52Format();
    }
    if ("NUMBER5_4".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber52Format();
    }
    if ("NUMBER".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumberFormat();
    }
    if ("NUMBER8_2".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber82Format();
    }
    if ("STRLENGTH".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisSTRLENFormat();
    }

    //2005/12/01 andy:ePos中有取兩位整數,兩位小數的.也加到SRC3中
    if ("NUMBER2_2".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisNumber22Format();
    }*/
    // 2014/11/12 Joe 统一所有的Number使用emisNumberGeneralFormat类处理，ZR的设定选项由 Systab_d 维护 FD_VALIDATION
    if (sValidType != null && sValidType.toUpperCase().startsWith("NUMBER")) {
      return new emisNumberGeneralFormat(sValidType);
    }

    if ("STRLENGTH".equalsIgnoreCase(sValidType)){
      _oRetPattern = new emisSTRLENFormat();
    }
    return _oRetPattern;
  }
}
