package com.emis.schedule.epos.bm;

import com.emis.bm.synPosData.emisBMSynDataPart;
import com.emis.schedule.epos.emisEposAbstractSchedule;

import javax.servlet.ServletContext;

/**
 * 抓取下传档
 */
public class emisSynSaleOutData extends emisEposAbstractSchedule {

  public emisSynSaleOutData() {
    super();
  }

  public emisSynSaleOutData(ServletContext oContext) {
    oContext_ = oContext;
  }

  protected void postAction() throws Exception {
    emisBMSynDataPart syn = new emisBMSynDataPart(oContext_);
    syn.synPartSaleOut();
  }

}