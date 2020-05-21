/* $Header: /repository/src3/src/com/emis/util/emisExcelReader.java,v 1.1.1.1 2005/10/14 12:43:20 andy Exp $
 * 必須在lib中存放xlrd.jar
 */
package com.emis.util;

import xlrd.Workbook;
import xlrd.Sheet;
import xlrd.Cell;
import xlrd.CellType;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

/**
 * Created by IntelliJ IDEA.
 * User: jerry
 * Date: 2003/10/17
 * Time: 上午 11:42:11
 */
public class emisExcelReader {
  Workbook oWorkbook_;

  public emisExcelReader(String sFileName) throws Exception {
    File _oFile = new File(sFileName);
    if (!_oFile.exists()) {
      throw new Exception("File does not exists.");
    }
    oWorkbook_ = Workbook.getWorkbook(_oFile);
  }

  public Workbook getWorkbook() {
    return oWorkbook_;
  }

  public Sheet getSheet(int i) {
    return oWorkbook_.getSheet(i);
  }

  public static void main(String[] args) throws Exception {
    BufferedWriter bw = null;
    OutputStreamWriter osw = null;
    try {
      emisExcelReader obj = new emisExcelReader("d:/tmp/test.xls");
      osw = new OutputStreamWriter(System.out, "UTF-8");

      bw = new BufferedWriter(osw);

      Workbook _oWorkbook = obj.getWorkbook();
      if (_oWorkbook.getNumberOfSheets() == 0) {
        System.out.println("Empty workbook");
        return;
      }

      Sheet _oSheet = _oWorkbook.getSheet(0);  // 只要處理第一個Sheet

      // bw.write(_oSheet.getName());  // Sheet的名稱
      //  bw.newLine();

      Cell[] row = null;  // 一列的所有儲存格

      // 第0列是表頭, skip it.
      for (int i = 1; i < _oSheet.getRows(); i++) {
        row = _oSheet.getRow(i);  // 取出第 i 列的內容放入Cell[ ]

        // Find the last non-blank entry in the row
        int nonblank = 0;
        for (int j = row.length - 1; j >= 0; j--) {
          if (row[j].getType() != CellType.EMPTY) {
            nonblank = j;
            break;
          }
        }
        bw.write(row[0].getContents());
        for (int j = 1; j <= nonblank; j++) {
          bw.write(',');
          bw.write(row[j].getContents());
        }
        bw.newLine();
      }
      bw.flush();
      bw.close();

      _oWorkbook.close();
    } catch (Throwable t) {
      System.out.println(t.toString());
      t.printStackTrace();
    }
  }



}
