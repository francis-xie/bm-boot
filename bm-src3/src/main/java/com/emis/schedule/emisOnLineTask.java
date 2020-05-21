/*

 * $Header: /repository/src3/src/com/emis/schedule/emisOnLineTask.java,v 1.1.1.1 2005/10/14 12:42:32 andy Exp $

 *

 * Copyright (c) EMIS Corp.

 * 2002/06/27 Jerry 加上註解.

 */

package com.emis.schedule;



import com.emis.user.emisUser;



import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;



/**

 * 此 Interface 是提供 emisTask 的子 Class 也可以用線上 (JSP,Servlet)

 * 的方式使用同一段程式碼,因為排程的工作有時候也需要可以在線上任意時間

 * 使用,但是線上的作業跑太久的話,Browser 會 TimeOut, 造成結果通常會看

 * 不到,所以使用另外一個 Thread 來跑,但是同一個系統只允許一個 sRegisterName

 * 跑,也就是一個人只能跑一個同樣的作業,emisTaskDescriptor 是用

 * 來描述作業進行的進度的

 * 範例:資料下傳作業

 *

 * @see com.emis.schedule.emisTaskDescriptor

 * @see com.emis.schedule.emisTask

 */

public interface emisOnLineTask {

  public emisTaskDescriptor getDescriptor();



  public void runOnLine(String sRegisterName, ServletContext oContext,

                        String sParameter, emisUser oUser) throws Exception;



  public void runOnLine(String sRegisterName, ServletContext oContext,

                        HttpServletRequest oRequest, emisUser oUser) throws Exception;

}

