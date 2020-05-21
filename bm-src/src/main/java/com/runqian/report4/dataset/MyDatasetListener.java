package com.runqian.report4.dataset;

import com.runqian.base4.util.Logger;
import com.runqian.report4.usermodel.*;

import java.security.Security;
import java.util.Map;


public class MyDatasetListener implements IDataSetFactoryListener { //实现IDataSetFactoryListener接口

  long starttime, endtime;
  public void beforeCreated(Context ctx, DataSetConfig dsc, DataSet ds) { //数据集产生之前执行的方法
    Map <String,Object> map = ctx.getParamMap(true);

    for (Map.Entry<String,Object> entry: map.entrySet()){
       Logger.debug("parameter==========="+entry.getKey()+"=="+entry.getValue());
    }
    starttime = System.currentTimeMillis();
    Logger.debug(("數據集創建之前:--------------------" ));
  }

  public void afterCreated(Context ctx, DataSetConfig dsc, DataSet ds) { //数据集产生之后执行的方法
    endtime = System.currentTimeMillis();
    Logger.debug(("數據集創建完成:--------------------" + (endtime - starttime)));
  }

}

