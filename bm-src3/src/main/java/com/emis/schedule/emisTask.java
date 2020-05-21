/*
 *
 * Copyright (c) EMIS Corp.
 * 2002/06/27 Jerry 加上註解.
 * 2005/04/03 mike 原來手動執行migration有問題,修改 sParameter_ 取request的值.
 * Track+[10143] zhong.xu 2008/06/05 間隔排程僅限於隔幾秒執行外，增加可設定起汔時間之功能
 */
package com.emis.schedule;

import com.emis.business.emisHttpServletRequest;
import com.emis.db.emisDb;
import com.emis.trace.emisTracer;
import com.emis.user.emisUser;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

/**
 *  所有排程工作的 super class,此 super class 只提供了
 *  排程所需的功能,所以子 class 不需要管排程的部份,
 *  只需要寫要做甚麼事情就可以了 (寫在 run() 裏面 )
 *  如果同時有線上使用的需求,子 class 可以多 implement
 *  emisOnLineTask 介面
 *  emisTask 的  member 變數如 ServletContext oContext_
 *  等會由 ScheduleMgr 或由 emisOnLineTask 的 method
 *  來 assign,子 class 只需拿來使用.
 *
 *  2010/1/6 robert , 改成需要 implement runTask , 原來是需要 implement run
 * Track+[14754] tommer.xie 2010/04/23 修正手動執行《日結作業》排程出錯
 * Track+[14915] dana.gao 2010/05/19 改善連線池,增加未關閉連接查找功能
 *
 *  @see com.emis.schedule.emisOnLineTask
 */
abstract public class emisTask implements Runnable , emisOnLineTask  {
  public static final int VIRGIN = 0;
  public static final int SCHEDULED = 1;
  public static final int EXECUTED = 2;
  public static final int CANCELLED = 3;

  private static String[] statuslist = {"初始化", "排程中", "執行中", "取消"};

  int state = VIRGIN;
  Object lock = new Object();

  // 此順序不可隨便換
  public static final int RUN_BY_INTERVAL = 0;
  public static final int RUN_EVERY_DAY = 1;
  public static final int RUN_EVERY_MONTH = 2;
  public static final int RUN_EVERY_YEAR = 3;
  public static final int RUN_EVERY_WEEK = 4;
  public static final int RUN_BY_SPECIFY = 5;
  protected static final String[] runLevels_ = {
      "週期性執行", "每日執行", "每月執行", "每年執行", "每週執行", "特定時間執行"
  };

  /** 在SCHED table 中的RunLevel欄位要填字串:
   * I: RUN_BY_INTERVAL
   * D: RUN_EVERY_DAY
   * M: RUN_EVERY_MONTH
   * Y: RUN_EVERY_YEAR
   * W: RUN_WEEK
   * S: RUN_BY_SPECIFY
   */
  int iRunLevel_;
  int iYear_ = -1;
  int iMonth_ = -1;
  int iDay_ = -1;
  int iHour_ = -1;
  int iTime_ = -1;
  int iHourEnd_ = -1;
  int iTimeEnd_ = -1;

  /** 每隔多久執行一次 */
  long nInterval_;

  /** 下次的 execute Time */
  long nNextExecution;
  /** Context物件 */
  protected ServletContext oContext_;
  /** schedule 的 name */
  protected String sName_;
  protected String sParameter_;
  protected HttpServletRequest oRequest_;
  /**
   * task的參數:
   *   schedParam_[0] = runLevel;
   *   schedParam_[1] = year;
   *   schedParam_[2] = month;
   *   schedParam_[3] = day;
   *   schedParam_[4] = hour;
   *   schedParam_[5] = time;
   *   schedParam_[6] = interval;
   *   schedParam_[7] = end hour; 每日時間段裏的週期執行
   *   schedParam_[8] = end time;
   */
  String[] schedParam_ = new String[9];
  protected emisUser oUser_;
  protected String sOnLineRegisterName_;
  protected Thread m_thread = null;


  // used for class reloading
  private long lastModified_;
  private emisTracer oTracer_ = null;

  // robert,2011/11/25 , add for thread group
  protected String m_sThreadGroup;

  public String getThreadGroup() {
    if( m_sThreadGroup == null ) return "";
    return m_sThreadGroup;
  }
  public void setThreadGroup(String group) {
    m_sThreadGroup = group;
  }

  protected long getLastModified() {
    return lastModified_;
  }

  protected void setLastModified(long lastModified) {
    lastModified_ = lastModified;
  }

  public void setName(String sName) {
    sName_ = sName;
  }

  public void setContext(ServletContext oContext) {
    this.oContext_ = oContext;
  }

  public String getName() {
    return sName_;
  }

  public void setParam(String sStr) {
    sParameter_ = sStr;
  }

  public String getParam() {
    return sParameter_;
  }

  /**
   * 取得SCHED的參數([0]..[6]).
   */
  public String[] getSchedParam() {
    return schedParam_;
  }

  public void setRequest(HttpServletRequest request) {
    oRequest_ = request;
  }

  /**
   *  just used for sched reloading
   */
  protected void setSched(String[] schedParam) throws Exception {
    setSched(schedParam[0], schedParam[1], schedParam[2],
        schedParam[3], schedParam[4], schedParam[5],
        schedParam[6],schedParam[7],schedParam[8]);
  }

  /**
   * 將參數存入參數陣列schedParam_[0]..[6].
   */
  public void setSched(String runLevel,
                       String year, String month, String day,
                       String hour, String time, String interval,String endHour,String endTime) throws Exception {
    if (oTracer_ == null)
      this.oTracer_ = emisTracer.get(oContext_);

    schedParam_[0] = runLevel;
    schedParam_[1] = year;
    schedParam_[2] = month;
    schedParam_[3] = day;
    schedParam_[4] = hour;
    schedParam_[5] = time;
    schedParam_[6] = interval;
    schedParam_[7] = endHour;
    schedParam_[8] = endTime;

    iRunLevel_ = strToLevel(runLevel);
    if (iRunLevel_ == RUN_BY_INTERVAL) {
      if (interval != null) {
        int nInterval = Integer.parseInt(interval);
        nInterval_ = nInterval * 1000;
        this.setNextExecutetion(System.currentTimeMillis());
      } else {
        throw new Exception("there is not interval setted,while the RUN level is by interval");
      }
      return;
    }

    Calendar _oCalendar = Calendar.getInstance(emisUtil.LOCALE);
    _oCalendar.set(Calendar.SECOND, 0);

    if (iRunLevel_ == this.RUN_BY_SPECIFY) {
      iYear_ = Integer.parseInt(year) + 1911;
      _oCalendar.set(Calendar.YEAR, iYear_);
      iMonth_ = Integer.parseInt(month); // 月份在 Calendar 是從0 起算
      _oCalendar.set(Calendar.MONTH, iMonth_ - 1);
      iDay_ = Integer.parseInt(day);
      _oCalendar.set(Calendar.DATE, iDay_);
      iHour_ = Integer.parseInt(hour);
      iTime_ = Integer.parseInt(time);
      _oCalendar.set(Calendar.HOUR_OF_DAY, iHour_);
      _oCalendar.set(Calendar.MINUTE, iTime_);
    }
    if (iRunLevel_ == this.RUN_EVERY_YEAR) {
      // 拿月
      iMonth_ = Integer.parseInt(month); // 月份在 Calendar 是從0 起算
      _oCalendar.set(Calendar.MONTH, iMonth_ - 1);
      iDay_ = Integer.parseInt(day);
      _oCalendar.set(Calendar.DATE, iDay_);
      iHour_ = Integer.parseInt(hour);
      iTime_ = Integer.parseInt(time);
      _oCalendar.set(Calendar.HOUR_OF_DAY, iHour_);
      _oCalendar.set(Calendar.MINUTE, iTime_);
    }
    if (iRunLevel_ == this.RUN_EVERY_MONTH) {
      // 拿日期
      iDay_ = Integer.parseInt(day);
      _oCalendar.set(Calendar.DATE, iDay_);
      iHour_ = Integer.parseInt(hour);
      iTime_ = Integer.parseInt(time);
      _oCalendar.set(Calendar.HOUR_OF_DAY, iHour_);
      _oCalendar.set(Calendar.MINUTE, iTime_);
    }
    if (iRunLevel_ == this.RUN_EVERY_WEEK) {
//System.out.println("time="+_oCalendar.getTime());
      iDay_ = Integer.parseInt(day); // 星期幾
      // check for value
      if ((iDay_ < Calendar.SUNDAY) || (iDay_ > Calendar.SATURDAY)) {
        throw new Exception("不合理的星期設定,RUNLEVEL=W,1<= DAY <=7");
      }
//System.out.println("set week="+day_);
      int _week = _oCalendar.get(Calendar.DAY_OF_WEEK);
      int _time = _oCalendar.get(Calendar.HOUR) * 100 + _oCalendar.get(Calendar.MINUTE);
      iHour_ = Integer.parseInt(hour);
      iTime_ = Integer.parseInt(time);

//System.out.println("get current week="+_week);
      if ((iDay_ >= _week) && (_time > (iHour_ * 100 + iTime_))) {
        _week = iDay_ + (7 - _week); // 今天跟下周星期幾差幾天 ?
//System.out.println("set next week:"+_week);
      } else {
        _week = iDay_ - _week;
//System.out.println("set this week:"+_week);
      }
      _oCalendar.add(Calendar.DATE, _week);
      _oCalendar.set(Calendar.HOUR_OF_DAY, iHour_);
      _oCalendar.set(Calendar.MINUTE, iTime_);
//System.out.println("time="+_oCalendar.getTime());
    }

    if (iRunLevel_ == this.RUN_EVERY_DAY) {
      // 拿時,分
      iHour_ = Integer.parseInt(hour);
      iTime_ = Integer.parseInt(time);
      //結束時,分
      iHourEnd_ = Integer.parseInt(endHour);
      iTimeEnd_ = Integer.parseInt(endTime);
      //_oCalendar.set(Calendar.DATE, _oCalendar.get(Calendar.DATE));
      _oCalendar.set(Calendar.HOUR_OF_DAY, iHour_);
      _oCalendar.set(Calendar.MINUTE, iTime_);
      //每隔多少秒
      int nInterval = Integer.parseInt(interval);
      nInterval_ = nInterval * 1000;
      oTracer_.debug(this, "RunLevel=EVERY_DAY, hour="+iHour_+",time="+iTime_);
    }

    nNextExecution = _oCalendar.getTime().getTime();
    long now = System.currentTimeMillis();
    if (emisScheduleMgr.SCHED_DEBUG) {
      oTracer_.debug(sName_ + " " + new Date(now) + " " + new Date(nNextExecution));
    }
    if (now > nNextExecution) // 如果已經超過時間
    {
      if (iRunLevel_ == this.RUN_BY_SPECIFY)
        throw new Exception("this task already expired");
      this.setNextExecutetion(now);
    }
  }

  public int strToLevel(String sLevel) throws Exception {
    if ("I".equals(sLevel)) return this.RUN_BY_INTERVAL;
    if ("D".equals(sLevel)) return this.RUN_EVERY_DAY;
    if ("M".equals(sLevel)) return this.RUN_EVERY_MONTH;
    if ("Y".equals(sLevel)) return this.RUN_EVERY_YEAR;
    if ("W".equals(sLevel)) return this.RUN_EVERY_WEEK;
    if ("S".equals(sLevel)) return this.RUN_BY_SPECIFY;
    throw new Exception("unsupported schedule run level");
  }


  public String levelToStr() {
    return runLevels_[iRunLevel_];
  }

  public int getRunLevel() {
    return iRunLevel_;
  }

  public boolean cancel() {
    synchronized (lock) {
      boolean result = (state == SCHEDULED);
      state = CANCELLED;
      return result;
    }
  }

  public String getContextRegisterThreadGroup(){
    try {
      //手动执行排程时，取自动排程中设定的Thread group
      if(m_sThreadGroup == null && schedParam_[0] == null && oContext_ != null && sName_ != null){
        m_sThreadGroup = emisScheduleMgr.getInstance(oContext_).getThreadGroup(sName_);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    String sThreadGroup = this.getThreadGroup();
    String sRegisterName = "taskThreadGroup." + sThreadGroup + ".emis";
    return sRegisterName;
  }

  // ScheduleMgr 也有用到,主要是統一 一個方式產生這個 name 保證他是 unique 就好
  public String getContextRegisterName() {
    String sClassName = this.getClass().getName();
    String sRegisterName = "taskmgnspace." + sClassName + ".emis";
    return sRegisterName;
  }
  //  檢查此 task 是否已經有 instance 在 run
  void checkTaskReadyExist() throws Exception {
    if (IsTaskReadyExist()) {
      throw new Exception("已經存在一個 process 在執行中");
    }

    if(IsThreadGroupReadyExist()){
      throw new Exception("已經存在一個與當前排程相同群組的 process 在執行中");
    }
  }

  // no throw exception
  boolean IsTaskReadyExist() {
    String sName = getContextRegisterName();
    Object obj = oContext_.getAttribute(sName);
    if (obj != null) {
      return true;
    }
    return false;
  }

  boolean IsThreadGroupReadyExist(){
    String sName = getContextRegisterThreadGroup();
    Object obj = oContext_.getAttribute(sName);
    if (obj != null) {
      return true;
    }
    return false;
  }

  private void registerContext() {
    String sName = getContextRegisterName();
    oContext_.setAttribute(sName, this);

    String sGroupName = getContextRegisterThreadGroup();
    oContext_.setAttribute(sGroupName, this);
  }

  void removeTaskFromContext() {
    String sName = getContextRegisterName();
    oContext_.removeAttribute(sName);

    String sRegisterThreadGroup = getContextRegisterThreadGroup();
    oContext_.removeAttribute(sRegisterThreadGroup);
  }

  private void endOfTaskExecute() {
    setNextExecutetion(System.currentTimeMillis());
    removeTaskFromContext();
    if( state != this.CANCELLED) {
      state = SCHEDULED;
    }
  }


  // 改成 startTaskThread 為 RunOnLine 和 ScheduleMgr 統一呼叫的介面
  /*
  public synchronized void startTaskThread() throws Exception {

    checkTaskExist(false);
    try {
      m_thread = new Thread(this);
      m_thread.setName( getClass().getName() );
      m_thread.start();
      state = EXECUTED;
    } catch (Exception e) {
      e.printStackTrace();
      endOfTaskExecute();
        emisTracer.get(oContext_).warning(e.getMessage());
        throw e;
    }
  }
  */

  public int getState() {
    return state;
  }


  public synchronized void run() {
    // 移到Try块之外做检查，避免Finally把正在跑的registerContext 清掉，导致第二次自动跑时挡不到
    if (IsTaskReadyExist() || IsThreadGroupReadyExist()) {
      System.out.println("emisTask block because already exists:" + this.getClass().getName());
      // 参考endOfTaskExecute()添加相应处理
      setNextExecutetion(System.currentTimeMillis());
      return;
    }
    try {
      registerContext();

      try {
        emisDb.startCheckPoint(this, oRequest_);
        try {
          state = this.EXECUTED;
          runTask();
        } catch (Throwable e) {
          //2010/04/23 tommer.xie 如果將存在一排程的異常信息保存在session中
          if( oRequest_ != null ) {
            oRequest_.getSession().setAttribute("ErrAmtMsg", e.getMessage());
          }
          e.printStackTrace(System.err);
        }
      }  finally {
        emisDb.endCheckPoint(this, oRequest_);
      }
    } finally {
      endOfTaskExecute();
    }
  }

  abstract public void runTask() throws Exception;


  /**
   * 由外部來呼叫,可以馬上執行
   *  robert 2010/01/06
   *  檢查是否重複部份,改由 servletContext 和 className, 而不是由 registerName 和 User Object
   */

  public void runOnLine(String sRegisterName, ServletContext oContext, String sParameter, emisUser oUser) throws Exception {
    oContext_ = oContext;
    sParameter_ = sParameter;
    oUser_ = oUser;
    this.setName(sRegisterName);
    checkTaskReadyExist();
    this.run();

  }

  public void runOnLine(String sRegisterName, ServletContext oContext, HttpServletRequest oRequest, emisUser oUser) throws Exception {
    oContext_ = oContext;
    sParameter_=oRequest.getParameter("PARAM");//mike 2005/04/03 修改call migration寫法
    if(sParameter_!=null&&("".equalsIgnoreCase(sParameter_)||" ".equalsIgnoreCase(sParameter_))){
      sParameter_ = null;
    }
    oRequest_ = new emisHttpServletRequest(oRequest);
    oUser_ = oUser;
    this.setName(sRegisterName);
    checkTaskReadyExist();
    this.run();
  }

  public emisTaskDescriptor getDescriptor() {
    return null;
  }

  public long nextExecutionTime() {
    return nNextExecution;
  }

  public String getStatus() {
    return statuslist[this.state];
  }

  public String getYear() {
    if (iYear_ >= 0)
      return String.valueOf(iYear_ - 1911) + "年";
    return "";
  }

  public String getMonth() {
    if (iMonth_ >= 0)
      return String.valueOf(iMonth_) + "月";
    return "";
  }

  public String getDay() {
    if (iDay_ >= 0) {
      if (this.iRunLevel_ == this.RUN_EVERY_WEEK) {
        if (iDay_ == Calendar.SUNDAY) {
          return "星期日";
        }
        if (iDay_ == Calendar.MONDAY) {
          return "星期一";
        }
        if (iDay_ == Calendar.TUESDAY) {
          return "星期二";
        }
        if (iDay_ == Calendar.WEDNESDAY) {
          return "星期三";
        }
        if (iDay_ == Calendar.THURSDAY) {
          return "星期四";
        }
        if (iDay_ == Calendar.FRIDAY) {
          return "星期五";
        }
        if (iDay_ == Calendar.SATURDAY) {
          return "星期六";
        }
        return "未知";
      } else {
        return String.valueOf(iDay_) + "日";
      }
    }
    return "";
  }

  public String getHour() {
    if (iHour_ >= 0)
      return String.valueOf(iHour_) + "時";
    return "";
  }

  public String getMinute() {
    if (iTime_ >= 0)
      return String.valueOf(iTime_) + "分";
    return "";
  }

  public long getInterval() {
    return this.nInterval_;
  }

  public void setNextExecutetion(long now) {
    if (iRunLevel_ == this.RUN_BY_INTERVAL) { // 週期性的執行
      nNextExecution = now + nInterval_;
      return;
    }

    if (iRunLevel_ == this.RUN_BY_SPECIFY) { // 定時
      nNextExecution = Long.MAX_VALUE;
      return;
    }

    Calendar c = Calendar.getInstance(emisUtil.LOCALE);
    c.setTime(new Date(now));

    c.set(Calendar.SECOND, 0);
    /*if (iRunLevel_ == this.RUN_EVERY_DAY) {
      Calendar cEnd = Calendar.getInstance(emisUtil.LOCALE);
      cEnd.setTime(new Date(now));
      cEnd.set(Calendar.SECOND, 0);
      cEnd.set(Calendar.HOUR_OF_DAY, iHourEnd_);
      cEnd.set(Calendar.MINUTE, iTimeEnd_);
      long end = cEnd.getTime().getTime();

      //週期性的執行
      if(nNextExecution <= now  && now <end && (nNextExecution < end)){
        nNextExecution = now + nInterval_;
        return;
      }else if(now >= end){ //明天再跑
       c.set(Calendar.DATE, c.get(Calendar.DATE) + 1);
       c.set(Calendar.HOUR_OF_DAY, iHour_);
       c.set(Calendar.MINUTE, iTime_);
      } else {
        //某些排程可能會跳過第一第二個判斷，導致不斷的執行（死循?）
        //加上這步可以避免，但會比設定的值多執行一兩次（大部分不會）
        nNextExecution = now + nInterval_;
        return;
      }
    }*/
    // Robert 修正跨天導致排程重複跑問題
    if (iRunLevel_ == this.RUN_EVERY_DAY) {
      Calendar cEnd = Calendar.getInstance(emisUtil.LOCALE);
      cEnd.setTime(new Date(now));
      cEnd.set(Calendar.SECOND, 0);
      cEnd.set(Calendar.HOUR_OF_DAY, iHourEnd_);
      cEnd.set(Calendar.MINUTE, iTimeEnd_);
      long end = cEnd.getTime().getTime();

      //週期性的執行
      //if(nNextExecution <= now  && now <end && (nNextExecution < end)){
      if (now < end) {
        long nNewNextExecution = nNextExecution + nInterval_;
        if (nNextExecution > now){

        } else if (nNewNextExecution > now) {
          nNextExecution = nNewNextExecution;
        } else {
          nNextExecution = now + nInterval_;
        }
        return;
      } else if (now >= end) { //明天再跑
        c.set(Calendar.DATE, c.get(Calendar.DATE) + 1);
        c.set(Calendar.HOUR_OF_DAY, iHour_);
        c.set(Calendar.MINUTE, iTime_);
      } else {
        //某些排程可能會跳過第一第二個判斷，導致不斷的執行（死循?）
        //加上這步可以避免，但會比設定的值多執行一兩次（大部分不會）
        nNextExecution = now + nInterval_;
        return;
      }
    }


    if (iRunLevel_ == this.RUN_EVERY_MONTH) {
      c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
      c.set(Calendar.DATE, iDay_);
      c.set(Calendar.HOUR_OF_DAY, iHour_);
      c.set(Calendar.MINUTE, iTime_);
    }

    if (iRunLevel_ == this.RUN_EVERY_YEAR) {
      c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 1);
      c.set(Calendar.MONTH, iMonth_ - 1); // month 從 0 開始
      c.set(Calendar.DATE, iDay_);
      c.set(Calendar.HOUR_OF_DAY, iHour_);
      c.set(Calendar.MINUTE, iTime_);
    }

    if (iRunLevel_ == this.RUN_EVERY_WEEK) {
      // 當週期設 'W' 時,每星期幾的固定時間會執行
      // SUNDAY=1, SATURDAY=6 (同 JDK)
      int _week = c.get(Calendar.DAY_OF_WEEK);
      if (_week != iDay_) {
        _week = iDay_ + (7 - _week);
      } else {
        _week = 7; // 下星期
      }
//        System.out.println("Set Next week:"+_week);
      c.add(Calendar.DATE, _week);
      c.set(Calendar.HOUR_OF_DAY, iHour_);
      c.set(Calendar.MINUTE, iTime_);
    }
    nNextExecution = c.getTime().getTime();
  } //~setNextExecution

  // 執行完後是否取消，用於處理正在執行的排程被重載的問題
  protected boolean isCancel = false;
  public void cancelAfterSched() {
    isCancel = true;
  }
  public boolean needCancel(){
    return isCancel;
  }
}