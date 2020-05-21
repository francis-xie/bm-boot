package com.emis.report;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 用於每個emisRptField的擴展ＡＣＴＩＯＮ
 * @author Wing
 *
 */
public interface emisIRptDyAction {
   
	//設置來源字段
	public void  setFetchFieldS(HashMap hmFieldsPool_);
	public void  setProvider(emisRptProvider oProvider);
	public void  setSection(emisRptSection section);
	public void  setGroupList(ArrayList alGroupList_);
	public emisRptGroup getEmisRptGroup(int i);
	public String getFieldName();
	public void  setFieldName(String fieldName); 
	public void setSourceRptField(emisRptField sourceField);
	public emisRptField fetchField(String fieldName);
	public String getContent();	
	public double getNumber();
	public void run();
	
	
}
