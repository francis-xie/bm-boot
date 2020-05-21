package com.emis.hardware.printer;

import java.awt.print.*;

/**
 * 打印
 */
public class emisPrinter {

  int pageSize = 80;

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public void doPrint(emisPrinterData printData) {
    try {
      int pageWidth = pageSize == 58 ? 158 : 210;

      Book book = new Book();
      PageFormat pf = new PageFormat();
      pf.setOrientation(PageFormat.PORTRAIT);
      Paper p = new Paper();
      //设置打印纸的大小一般是158，10000随便设的因为这是一卷纸不像A4纸是规定的
      p.setSize(pageWidth,10000);
      //打印区域
      p.setImageableArea(0,0, pageWidth,10000);
      pf.setPaper(p);
      book.append(printData, pf);

      PrinterJob job = PrinterJob.getPrinterJob();
      job.setPageable(book);
      job.print();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}