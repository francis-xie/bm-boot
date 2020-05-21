/*

 * $Header: /repository/src3/src/com/emis/schedule/emisTaskDescriptor.java,v 1.1.1.1 2005/10/14 12:42:34 andy Exp $

 *

 * Copyright (c) EMIS Corp.

 */

package com.emis.schedule;



import java.io.Writer;





/**

 *  此 class 提供 Jsp/servlet 可以查尋背景作業

 *  1.進度

 *  2.是否結束

 *  3.是否有錯誤

 *  4.錯誤是甚麼 

 *  的界面

 *

 *  @see com.emis.schedule.emisOnLineTask

 */

public interface emisTaskDescriptor

{

  public void descript(Writer out);

  public boolean isFinished();

  public boolean hasError();

  public Exception getError();



}