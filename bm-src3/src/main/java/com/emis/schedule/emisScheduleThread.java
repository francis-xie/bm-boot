package com.emis.schedule;

/*
 * Robert 2011/11/25 為了實現 Scheduler 的 ThreadGroup 而加的
 */
public class emisScheduleThread extends Thread {

	private String m_ThreadGroup = "";
	public emisScheduleThread(Runnable r , String threadGroup) {
		super(r);
		m_ThreadGroup = threadGroup;
	}
	
	public String getMyThreadGroup() {
		return m_ThreadGroup;
	}
}
