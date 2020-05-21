package com.emis.app.migration;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Description :
 * Author : Merlin
 * Date : 2003/11/12 下午 09:54:10
 * Revision : $Revision: 71118 $
 * History:
 * $Log: emisParsableDoc.java,v $
 * Revision 1.1.1.1  2005/10/14 12:41:44  andy
 * add src3
 *
 * Revision 1.4  2004/01/19 11:03:53  joe
 * optimize imports
 * reformat
 *
 * Revision 1.3  2003/12/13 09:22:53  joe
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/12 04:59:55  merlin
 * no message
 *
 * Revision 1.2  2003/12/04 09:59:29  merlin
 * *** empty log message ***
 *
 * Revision 1.1.1.1  2003/12/04 05:12:36  merlin
 * 建立獨立的Migration Module
 *
 * Revision 1.1.2.1  2003/12/02 07:50:02  merlin
 * no message
 *
 */
public interface emisParsableDoc {
	void startDocument();

	void endDocument();

	void startElement(String elem, Hashtable h) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException, InvocationTargetException, NoSuchMethodException;

	void endElement(String elem);

	void text(String text) throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InstantiationException;
}
