/*
 * Created on 2004/10/29
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.emis.report.excel;
import junit.framework.TestCase;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class emisRefGetHSSFCellStyleTest extends TestCase {

  public void testGetIntForStringName() {
    
    emisRefGetHSSFCellStyle test=new emisRefGetHSSFCellStyle();
    test.getCellStyleColor("HSSFColor.CORNFLOWER_BLUE");
  }

}
