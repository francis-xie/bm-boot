package com.runqian;

import com.runqian.base4.util.Logger;
import com.runqian.report4.usermodel.AbstractCalculateListener;

public class CalculateListener extends AbstractCalculateListener {

  long starttime, endtime;


  public void beforeCalculate() throws Exception {

    starttime = System.currentTimeMillis();  //获取报表计算前系统时间
    Logger.debug(("快逸運算開始:--------------------" ));

  }

  public void afterCalculate() throws Exception {

    endtime = System.currentTimeMillis();  //获取报表计算后系统时间

    //计算时间间隔，对时间间隔做相应的业务处理

//    System.out.println("快逸運算時間:--------------------" + (endtime - starttime));

    Logger.debug(("快逸運算結束:--------------------" + (endtime - starttime)));


  }

}
