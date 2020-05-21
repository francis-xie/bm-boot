package com.emis.hardware.printer;

import java.awt.*;
import java.awt.print.*;
import java.util.ArrayList;

/**
 * 打印数据
 */
public class emisPrinterData implements Printable {

  private int baseFontSize = 10;
  private int baseLineHeight = 12;
  private int baseHeadHeight = 10;
  private int leftOffset = 11;
  private ArrayList<String> printData;

  public ArrayList<String> getPrintData() {
    return printData;
  }

  public void setPrintData(ArrayList<String> printData) {
    this.printData = printData;
  }

  public int getBaseFontSize() {
    return baseFontSize;
  }

  public void setBaseFontSize(int baseFontSize) {
    this.baseFontSize = baseFontSize;
  }

  public int getBaseLineHeight() {
    return baseLineHeight;
  }

  public void setBaseLineHeight(int baseLineHeight) {
    this.baseLineHeight = baseLineHeight;
  }

  public int getBaseHeadHeight() {
    return baseHeadHeight;
  }

  public void setBaseHeadHeight(int baseHeadHeight) {
    this.baseHeadHeight = baseHeadHeight;
  }

  public int getLeftOffset() {
    return leftOffset;
  }

  public void setLeftOffset(int leftOffset) {
    this.leftOffset = leftOffset;
  }

  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
    if (pageIndex > 0) {
      return NO_SUCH_PAGE;
    }

    if (printData == null || printData.isEmpty()) {
      return NO_SUCH_PAGE;
    }

    Graphics2D graphics2d = (Graphics2D) graphics;
    //设置字体
    graphics2d.setFont(new Font("宋体", Font.PLAIN, baseFontSize));
    graphics2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
    boolean isBaseSize = true;
    int changeFontCnt = 0;
    for(int i = 0; i < printData.size(); i++) {
      System.out.println(printData.get(i));
      if ("+++".equals(printData.get(i))) {
        isBaseSize = false;
        changeFontCnt++;
        continue;
      }
      if (isBaseSize) {
        graphics2d.drawString(printData.get(i), leftOffset, (i-changeFontCnt)* baseLineHeight + baseHeadHeight);
      } else {
        graphics2d.setFont(new Font("宋体", Font.PLAIN, baseFontSize+4));
        graphics2d.drawString(printData.get(i), leftOffset, (i-changeFontCnt)* baseLineHeight + baseHeadHeight);
        graphics2d.setFont(new Font("宋体", Font.PLAIN, baseFontSize));
      }
      isBaseSize = true;
    }

    //打印格式
//    graphics2d.drawString("-- 打印测试1-- ", 15, 10);
//    graphics2d.setFont(new Font("宋体", Font.PLAIN, 14));
//    graphics2d.drawString("-- 打印测试2-- ", 5, 30);
//    graphics2d.setFont(new Font("宋体", Font.PLAIN, 10));
//    graphics2d.drawString("------------------------------------------", 15, 40);
//    graphics2d.drawString("-- 打印测试3-- ", 5, 45);

    /*int i = 1;
    graphics2d.drawString("          魏家凉皮-平乐园", 0, baseHeadHeight);
    graphics2d.drawString("", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("取餐编号：B0001", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("点单时间:2019-08-20 07:13:18", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("002-0001  堂食  收银员:W001002", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("品名                  数量      金额", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("------------------------------------", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("肉包套餐                 1      9.80", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString(" 肉包                    1          ", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString(" 南瓜粥                  1          ", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("------------------------------------", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("合计                     1      9.80", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("微信支付                        9.80", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("        4200000396201908201032188504", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("找零                               0", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("------------------------------------", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("010-87716805", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("北京市朝阳区西大望路34号2号楼一层西 ", 0, (i++)* baseLineHeight + baseHeadHeight);
    graphics2d.drawString("南侧 ", 0, (i)* baseLineHeight + baseHeadHeight);*/

    return PAGE_EXISTS;
  }
}