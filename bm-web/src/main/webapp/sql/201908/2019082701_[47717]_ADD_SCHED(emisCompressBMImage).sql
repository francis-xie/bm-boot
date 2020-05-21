/* 
 * $Id$
 * 需求 #47717 标准版-大屏自助点餐 下传资料处理排程
 */
insert into Sched(S_NAME, S_SERVER, S_DESC, S_CLASS, RUNLEVEL, SYEAR, SMONTH, SDAY, SHOUR, STIME, INTERVAL1, PARAM, SHOUR_END, STIME_END, S_MENU, THREAD_GROUP, REMARK)
select 'emisBMDownload', 'bigMonitor', '下载资料并处理', 'com.emis.schedule.epos.bm.emisBMDownload', 'I', '', '', '', '', '', '120', '', '', '', '2', 'download', ''
from dual
where not exists (select S_NAME from Sched where S_NAME = 'emisBMDownload')
GO