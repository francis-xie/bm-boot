package com.emis.cache;

import javax.servlet.ServletContext;


/**
 * Description :
 * Author : Merlin
 * Date : 2004/8/6 下午 06:11:01
 * Revision : $Revision: 71118 $
 * History:
 * $Log: emisCachable.java,v $
 * Revision 1.1.1.1  2005/10/14 12:41:57  andy
 * add src3
 *
 * Revision 1.2  2004/08/07 10:29:05  merlin
 * NC
 *
 * Revision 1.1  2004/08/06 10:36:50  merlin
 * NC
 *
 */
public abstract class emisCachable {
//    HashMap conext 
    abstract public boolean reload(ServletContext context);
}
