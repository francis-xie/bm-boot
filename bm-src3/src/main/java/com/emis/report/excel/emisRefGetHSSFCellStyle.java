
package com.emis.report.excel;


import java.util.HashMap;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.hssf.util.HSSFColor;


/**
 * @author Wing 目的:據XML字義的名稱取相HSSFCellStyle中的COLOR,
 * 填充方式的short或FINAL int數值
 *
 *
 */
public class emisRefGetHSSFCellStyle {

  static Logger logger = Logger.getLogger(emisRefGetHSSFCellStyle.class.getName());

  /**
   * 邊框設定
   * @param name
   * @return
   */
  public static short getCellBorder(String name) {
    short i = 0;
    try {

      i = HSSFCellStyle.class.getField(name).getShort(name);
      //logger.info("HSSFCellStyle i:"+ i);
    } catch (Exception ee) {
      //ee.printStackTrace();
      //logger.info("HSSFCellStyle COlor i ex:"+ ee.getMessage());
      i = 0;
    }
    
    return i;
  }

  public final static  byte getFontUnderLine(String lineName){
    try{
      if("U_NONE".equalsIgnoreCase(lineName))
        return HSSFFont.U_NONE;
      else if("U_SINGLE".equalsIgnoreCase(lineName))
        return HSSFFont.U_SINGLE;
      else if("U_DOUBLE".equalsIgnoreCase(lineName))
        return HSSFFont.U_DOUBLE;
      else if("U_SINGLE_ACCOUNTING".equalsIgnoreCase(lineName))
        return HSSFFont.U_SINGLE_ACCOUNTING;
      else if("U_DOUBLE_ACCOUNTING".equalsIgnoreCase(lineName))
        return HSSFFont.U_DOUBLE_ACCOUNTING;
     else  return HSSFFont.U_NONE;
    }catch(Exception e){
      return  HSSFFont.U_NONE;
    }
  }

  /**
   * color setting
   * @param name
   * @return
   */
  public static short getCellStyleColor(String name) {
    HashMap map = getIndexHash();
    try {
      short color = (short) ((Integer) map.get(name)).intValue();
      //logger.info("HSSFCellStyle COlor i:"+ color);
      return color;
    } catch (Exception e) {
      //logger.info("HSSFCellStyle COlor i ex:"+ e.getMessage());
      return (short) 0;
    }
  }



  //公共設置
  public static void setCellValue(HSSFSheet sheet, int rowNum, short colNum,
      String text) {

    HSSFRow row;
    HSSFCell cell;
    row = HSSFCellUtil.getRow(rowNum, sheet);
    cell = HSSFCellUtil.getCell(row, colNum);
    cell.setCellValue(text);

  }

  public static void setCellStyleBackgroupColor(HSSFWorkbook workbook,
    HSSFSheet sheet, int rowNum, short colNum, String ColorName,
    HSSFCellStyle inCell) {
    HSSFRow row;
    HSSFCell cell;
    //HSSFFont oldFont;
    row = HSSFCellUtil.getRow(rowNum, sheet);
    cell = HSSFCellUtil.getCell(row, colNum);
    //cell.getCellStyle().setFillBackgroundColor(
    // HSSFColor.ROSE.index);//(short)getIntForStringName(ColorName));
    setNewStyle(workbook, cell, getCellStyleColor(ColorName), inCell);
  }

  public static void setNewStyle(HSSFWorkbook workbook, HSSFCell cell,
      short fp, HSSFCellStyle inCell) {
    HSSFCellStyle style = inCell;
    style.setFillForegroundColor(fp);

    //style.setFillForegroundColor(HSSFColor.CORNFLOWER_BLUE.index);
    //style.setFillPattern(HSSFCellStyle.FINE_DOTS);
    style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
    cell.setCellStyle(style);
  }

  public final static HashMap getIndexHash() {
    HashMap hash = new HashMap();
    hash.put("HSSFColor.BLACK.index", new Integer(HSSFColor.BLACK.index));
    hash.put("HSSFColor.BROWN.index", new Integer(HSSFColor.BROWN.index));
    hash.put("HSSFColor.OLIVE_GREEN.index", new Integer(
        HSSFColor.OLIVE_GREEN.index));
    hash.put("HSSFColor.DARK_GREEN.index", new Integer(
        HSSFColor.DARK_GREEN.index));
    hash.put("HSSFColor.DARK_TEAL.index",
        new Integer(HSSFColor.DARK_TEAL.index));
    hash.put("HSSFColor.DARK_BLUE.index",
        new Integer(HSSFColor.DARK_BLUE.index));
    hash.put("HSSFColor.DARK_BLUE.index2", new Integer(
        HSSFColor.DARK_BLUE.index));
    hash.put("HSSFColor.INDIGO.index", new Integer(HSSFColor.INDIGO.index));
    hash.put("HSSFColor.GREY_80_PERCENT.index", new Integer(
        HSSFColor.GREY_80_PERCENT.index));
    hash.put("HSSFColor.ORANGE.index", new Integer(HSSFColor.ORANGE.index));
    hash.put("HSSFColor.DARK_YELLOW.index", new Integer(
        HSSFColor.DARK_YELLOW.index));
    hash.put("HSSFColor.GREEN.index", new Integer(HSSFColor.GREEN.index));
    hash.put("HSSFColor.TEAL.index", new Integer(HSSFColor.TEAL.index));
    hash.put("HSSFColor.TEAL.index2", new Integer(HSSFColor.TEAL.index));
    hash.put("HSSFColor.BLUE.index", new Integer(HSSFColor.BLUE.index));
    hash.put("HSSFColor.BLUE.index2", new Integer(HSSFColor.BLUE.index));
    hash.put("HSSFColor.BLUE_GREY.index",
        new Integer(HSSFColor.BLUE_GREY.index));
    hash.put("HSSFColor.GREY_50_PERCENT.index", new Integer(
        HSSFColor.GREY_50_PERCENT.index));
    hash.put("HSSFColor.RED.index", new Integer(HSSFColor.RED.index));
    hash.put("HSSFColor.LIGHT_ORANGE.index", new Integer(
        HSSFColor.LIGHT_ORANGE.index));
    hash.put("HSSFColor.LIME.index", new Integer(HSSFColor.LIME.index));
    hash.put("HSSFColor.SEA_GREEN.index",
        new Integer(HSSFColor.SEA_GREEN.index));
    hash.put("HSSFColor.AQUA.index", new Integer(HSSFColor.AQUA.index));
    hash.put("HSSFColor.LIGHT_BLUE.index", new Integer(
        HSSFColor.LIGHT_BLUE.index));
    hash.put("HSSFColor.VIOLET.index", new Integer(HSSFColor.VIOLET.index));
    hash.put("HSSFColor.VIOLET.index2", new Integer(HSSFColor.VIOLET.index));
    hash.put("HSSFColor.GREY_40_PERCENT.index", new Integer(
        HSSFColor.GREY_40_PERCENT.index));
    hash.put("HSSFColor.PINK.index", new Integer(HSSFColor.PINK.index));
    hash.put("HSSFColor.PINK.index2", new Integer(HSSFColor.PINK.index));
    hash.put("HSSFColor.GOLD.index", new Integer(HSSFColor.GOLD.index));
    hash.put("HSSFColor.YELLOW.index", new Integer(HSSFColor.YELLOW.index));
    hash.put("HSSFColor.YELLOW.index2", new Integer(HSSFColor.YELLOW.index));
    hash.put("HSSFColor.BRIGHT_GREEN.index", new Integer(
        HSSFColor.BRIGHT_GREEN.index));
    hash.put("HSSFColor.BRIGHT_GREEN.index2", new Integer(
        HSSFColor.BRIGHT_GREEN.index));
    hash.put("HSSFColor.TURQUOISE.index",
        new Integer(HSSFColor.TURQUOISE.index));
    hash.put("HSSFColor.TURQUOISE.index2", new Integer(
        HSSFColor.TURQUOISE.index));
    hash.put("HSSFColor.DARK_RED.index", new Integer(HSSFColor.DARK_RED.index));
    hash
        .put("HSSFColor.DARK_RED.index2", new Integer(HSSFColor.DARK_RED.index));
    hash.put("HSSFColor.SKY_BLUE.index", new Integer(HSSFColor.SKY_BLUE.index));
    hash.put("HSSFColor.PLUM.index", new Integer(HSSFColor.PLUM.index));
    hash.put("HSSFColor.PLUM.index2", new Integer(HSSFColor.PLUM.index));
    hash.put("HSSFColor.GREY_25_PERCENT.index", new Integer(
        HSSFColor.GREY_25_PERCENT.index));
    hash.put("HSSFColor.ROSE.index", new Integer(HSSFColor.ROSE.index));
    hash.put("HSSFColor.LIGHT_YELLOW.index", new Integer(
        HSSFColor.LIGHT_YELLOW.index));
    hash.put("HSSFColor.LIGHT_GREEN.index", new Integer(
        HSSFColor.LIGHT_GREEN.index));
    hash.put("HSSFColor.LIGHT_TURQUOISE.index", new Integer(
        HSSFColor.LIGHT_TURQUOISE.index));
    hash.put("HSSFColor.LIGHT_TURQUOISE.index2", new Integer(
        HSSFColor.LIGHT_TURQUOISE.index));
    hash.put("HSSFColor.PALE_BLUE.index",
        new Integer(HSSFColor.PALE_BLUE.index));
    hash.put("HSSFColor.LAVENDER.index", new Integer(HSSFColor.LAVENDER.index));
    hash.put("HSSFColor.WHITE.index", new Integer(HSSFColor.WHITE.index));
    hash.put("HSSFColor.CORNFLOWER_BLUE.index", new Integer(
        HSSFColor.CORNFLOWER_BLUE.index));
    hash.put("HSSFColor.LEMON_CHIFFON.index", new Integer(
        HSSFColor.LEMON_CHIFFON.index));
    hash.put("HSSFColor.MAROON.index", new Integer(HSSFColor.MAROON.index));
    hash.put("HSSFColor.ORCHID.index", new Integer(HSSFColor.ORCHID.index));
    hash.put("HSSFColor.CORAL.index", new Integer(HSSFColor.CORAL.index));
    hash.put("HSSFColor.ROYAL_BLUE.index", new Integer(
        HSSFColor.ROYAL_BLUE.index));
    hash.put("HSSFColor.LIGHT_CORNFLOWER_BLUE.index", new Integer(
        HSSFColor.LIGHT_CORNFLOWER_BLUE.index));
    return hash;
  }

}