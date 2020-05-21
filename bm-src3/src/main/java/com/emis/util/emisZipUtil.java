package com.emis.util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;

/**
 * User: zhong.xu
 * Date: 2009-3-5
 * Time: 9:27:45
 *
 * 將文件或文件夾打包成ZIP文件
 *
 */
public class emisZipUtil extends ZipOutputStream {

  public emisZipUtil(OutputStream outputStream) {
    this(outputStream, defaultEncoding, defaultLevel);
  }

  public emisZipUtil(String file) throws IOException {
    this(new FileOutputStream(new File(file)), defaultEncoding, defaultLevel);
  }

  public emisZipUtil(File file) throws IOException {
    this(new FileOutputStream(file), defaultEncoding, defaultLevel);
  }

  /**
   * 統一調用的構造函數
   *
   * @param outputStream 輸出流(輸出路徑),*.zip
   * @param encoding 編碼
   * @param level 壓縮級別 0-9
   * */
  public emisZipUtil(OutputStream outputStream, String encoding, int level) {
    super(outputStream);

    buf = new byte[1024];//1024 KB緩衝

    if (encoding != null || !"".equals(encoding))
      this.setEncoding(encoding);

    if (level < 0 || level > 9) level = 7;
    this.setLevel(level);

	  comment = new StringBuffer();
  }

  public String put(String fileName) throws IOException {
    return put(fileName, "");
  }

  /**
   * 加入要壓縮的文件或文件夾
   *
   * @param fileName 加入一個文件,或一個文件夾
   * @param pathName 生成ZIP時加的文件夾路徑
   * @return fileName
   * */
  public String put(String fileName, String pathName) throws IOException {
    File file = new File(fileName);

    if(!file.exists()) {
      //comment.append("發現一個不存在的文件或目錄: ").append(fileName).append("\n");
      return null;
    }

    //遞歸加入文件
    if (file.isDirectory()) {
      pathName += file.getName() + "/";
      String fileNames[] = file.list();
      if (fileNames != null) {
        for (String f : fileNames) put(fileName + "\\" + f, pathName);
      }
      return fileName;
    }

    fileCount++;
    //System.out.println(fileCount + " = " + fileName);
    //System.out.println("file = " + file.getAbsolutePath());

    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    try {
      in = new BufferedInputStream(new FileInputStream(file));
      out = new BufferedOutputStream(this);
      if (userFullPathName)
        pathName += file.getPath();
      this.putNextEntry(new ZipEntry(pathName + file.getName()));
      int len;
      //BufferedOutputStream會自動使用 this.buf,如果再使用in.read(buf)數據會錯誤
      while ((len = in.read()) > -1) out.write(len);
    } catch (IOException ex) {
      comment.append("一個文件讀取寫入時錯誤: ").append(fileName).append("\n");
    }

    if (out != null) out.flush();
    if(in != null) in.close();

    this.closeEntry();
    return file.getAbsolutePath();
  }

  public String[] put(String[] fileName) throws IOException {
    return put(fileName, "");
  }

  public String[] put(String[] fileName, String pathName) throws IOException {
    for (String file : fileName)
      put(file, pathName);
    return fileName;
  }

  /**
   * 壓縮的文件個數
   *
   * @return int
   * */
  public int getFileCount() {
    return this.fileCount;
  }

  //測試
  public static void main(String[] args) {
    try {
      java.util.Date d1 = new java.util.Date();
      emisZipUtil util = new emisZipUtil("C:\\emisZipUtil.zip");
      //util.buf = new byte[1024*2]; //可以指定緩存
      util.comment.append("報表批量下載!\n\n");

      util.put(new String[]{"C:\\temp","C:\\emis","C:\\wwwroot\\smepos_cn 路徑.txt"});
      util.put("D:\\JQuery\\Jquery 1.2.6 源碼分析\\jquery1.2.6-源碼文檔-cn.js");
      util.put("C:\\wwwroot\\smepos_cn\\版本更新說明.doc","doc\\");
      util.put("C:\\wwwroot\\smepos_cn\\版本更新說明2.doc");
      util.put("C:\\wwwroot\\smepos_cn2");
      util.put("C:\\wwwroot\\smepos_cn\\report_out\\pre_print\\ROOT\\5AROOT中分類 2009-03-04 17-08-14 1072940375_1.xls");
      util.comment.append("\n共成功壓縮文件: ").append(util.getFileCount()).append(" 個!");
      util.setComment(util.comment.toString());
      util.close();
      java.util.Date d2 = new java.util.Date();
      System.out.println("used time = " + (d2.getTime() - d1.getTime()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //壓縮級別:0-9
  public static int defaultLevel = 7;
  //編碼,簡體:GB2312,繁體:BIG5
  public static String defaultEncoding = "GB2312";
  //壓縮時用全路徑,會生成對應的目錄,false:不帶路徑,只有文件名
  public static boolean userFullPathName = false;
  //註釋
  public StringBuffer comment;

  private int fileCount = 0;
}
