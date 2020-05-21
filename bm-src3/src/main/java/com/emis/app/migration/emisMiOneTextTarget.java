package com.emis.app.migration;

import com.emis.util.emisUtil;
import com.emis.report.emisString;

import java.io.*;

/**
 * User: harry
 * Date: 2008/07/21
 * 產生檔案後執行exe檔，不移動到門市目錄下
 */
public class emisMiOneTextTarget extends emisMiTarget {
  BufferedWriter out;
  private File outFile;
  private String sExeFileName = "IMPVIP.BAT";  //產生完檔案後執行的exe檔
   StringBuffer dataLine_ = new StringBuffer();
//  int count = 0;

  public  boolean open( emisMiConfig config) throws Exception {
    boolean deleted;
    boolean _bRet;
    fileName = getFileName();
     File outDir = new File(path);
    if (!outDir.exists()) {
      _bRet = outDir.mkdirs();
      if (!_bRet)
        return false;
    }
    outFile = new File(path + File.separator + fileName);
    if (clear && outFile.exists()) {
      deleted = outFile.delete();
    }
    out = new BufferedWriter(new FileWriter(outFile));
    writeCount = 0;
    return true;
  }

  public  boolean write( String[] data) throws IOException {
    dataLine_.setLength(0);
    for (int i = 0; i < data.length; i++)
      dataLine_.append(data[i]);
    out.write(dataLine_.toString());
    out.newLine();
    writeCount++;
    if (writeCount % 1024 == 0) {
       System.out.println("[" + emisUtil.now() + "]" + " :  "+ writeCount);
       out.flush();
    }
    return true;
  }

  public  boolean close( boolean closeDb) throws IOException {
    if (out != null) {
      out.flush();
      out.close();
      out = null;
    }
    return false;
  }

  public  void append( String[] path,  boolean reopen) throws Exception {
    out.flush();
    out.close();
    out = null;
    /*
//        try {
    for (int i = 0; i < path.length; i++) {
      //2005/05/12 andy:加入如下一行代碼.如資料庫中有空的記錄(如S_NO為空)時不產生檔案.
      if(path[i] == null || "".equals(emisString.trim(path[i]))) continue;
      //copyTo(this.path + File.separator + path[i], fileName, true);
      //2005/04/30 andy 修改:最後一個參數取前端的設定值
      //2005/05/08 andy :path加上subdir
      copyTo(this.path + File.separator + path[i] + File.separator + subdir, fileName, !clear);
    }
    */
    if (reopen)
      open(null);
//        } catch (Exception e) {
//            // Log here;  //To change body of catch statement use Options | File Templates.
//        }
  }

  private void copyTo( String pathTarget,  String fileName,  boolean isAppend) throws Exception {
     File dir = new File(pathTarget);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    if(new File(path + File.separator + fileName).length() == 0){
      //當源檔沒有資料時,不再產生空檔  update by andy 2006/01/24
      return;
    }
    FileOutputStream os = new FileOutputStream(pathTarget + File.separator + fileName, isAppend);
    try {
       BufferedOutputStream bos = new BufferedOutputStream(os);
       FileInputStream is = new FileInputStream(path + File.separator + fileName);
      BufferedInputStream bis = null;
      try {
        byte[] buf = new byte[4096];
        int readed;
        bis = new BufferedInputStream(is);
        while ((readed = bis.read(buf)) != -1) {
          bos.write(buf, 0, readed);
        }
        bos.flush();
      } finally {
        if( bis != null ){
          bis.close();
          bis = null;
        }
        if( is != null ) {
          is.close();
          is = null;
        }
        if( bos != null ) {
          bos.close();
          bos = null;
        }
      }
    } finally {
      if( os != null ) {
        os.close();
        os = null;
      }
    }
  }

  public  boolean clearTemp() {
    try {
      if(outFile != null && outFile.length() > 0) {
        //當源檔沒有資料時,不執行exe檔
        Runtime.getRuntime().exec(this.path + File.separator + sExeFileName);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

//    if (outFile != null) {
//      return outFile.delete();
//    }
    return true;
  }
}
