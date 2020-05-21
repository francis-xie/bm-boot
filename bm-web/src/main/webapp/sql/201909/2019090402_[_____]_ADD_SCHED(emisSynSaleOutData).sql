insert into Sched(S_NAME, S_SERVER, S_DESC, S_CLASS, RUNLEVEL, SYEAR, SMONTH, SDAY, SHOUR, STIME, INTERVAL1, PARAM, SHOUR_END, STIME_END, S_MENU, THREAD_GROUP, REMARK)
select 'emisSynSaleOutData', 'bigMonitor', '同步后台停售商品资料', 'com.emis.schedule.epos.bm.emisSynSaleOutData', 'I', '', '', '', '', '', '60', '', '', '', '2', 'syndata', ''
from dual
where not exists (select S_NAME from Sched where S_NAME = 'emisSynSaleOutData')
GO