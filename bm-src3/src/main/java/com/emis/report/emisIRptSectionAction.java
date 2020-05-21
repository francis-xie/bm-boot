package com.emis.report;

import java.util.HashMap;

public interface emisIRptSectionAction {
    //設置來源字段
	public void  setFetchFieldS(HashMap hmFieldsPool_);
	public void  setEmisRptDataSrc(emisRptDataSrc  oDataSrc);
	public void  setProvider(emisRptProvider oProvider);
	public void  setSection(emisRptSection section);
	public emisRptField fetchField(String fieldName);
	public void run();
}
