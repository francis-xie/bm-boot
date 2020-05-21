insert into Sched(S_NAME, S_SERVER, S_DESC, S_CLASS, RUNLEVEL, SYEAR, SMONTH, SDAY, SHOUR, STIME, INTERVAL1, PARAM, SHOUR_END, STIME_END, S_MENU, THREAD_GROUP, REMARK)
select 'emisExeSql', 'bigMonitor', '执行SQL文件', 'com.emis.schedule.epos.bm.emisExeSql', 'I', '', '', '', '', '', '60', '', '', '', '2', 'exec', ''
from dual
where not exists (select S_NAME from Sched where S_NAME = 'emisExeSql')
GO