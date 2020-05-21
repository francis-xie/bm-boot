package com.emis.report;

/**
 * 用於每個SECTION的JAVA PUGIIN擴展
 * @author wing
 *
 */
public interface emisIRptSectionExAction {
	public void  setProvider(emisRptProvider oProvider);
	public void  setSection(emisRptSection section);
	public emisRptProvider  getProvider(emisRptProvider oProvider);
	public emisRptSection   getSection(emisRptSection section);
	public void  prepareRun();
	public void  run();
	public void  afterRun();

}
