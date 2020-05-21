package com.emis.app.migration;

import com.emis.util.emisUtil;
import com.emis.report.emisString;

import java.io.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * User: harry
 * Date: 2008/07/21
 * 產生檔案後壓縮為zip
 */
public class emisMiZipTextTarget extends emisMiTarget {
  BufferedWriter out;
  private File outFile;
  private File outZipFile;
  private String sZipFileName;
   StringBuffer dataLine_ = new StringBuffer();
//  int count = 0;

  public  boolean open( emisMiConfig config) throws Exception {
    boolean deleted;
    boolean _bRet;
    fileName = getFileName();
    sZipFileName = prefix + ".zip";
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
    outZipFile = new File(path + File.separator + sZipFileName);
    if (clear && outZipFile.exists()) {
      deleted = outZipFile.delete();
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

    if(outFile.length() > 0) {
      //當源檔沒有資料時,不產生Zip檔
      zipFiles(this.path, sZipFileName, this.path, fileName);
    }

    for (int i = 0; i < path.length; i++) {
      //2005/05/12 andy:加入如下一行代碼.如資料庫中有空的記錄(如S_NO為空)時不產生檔案.
      if(path[i] == null || "".equals(emisString.trim(path[i]))) continue;
      //copyTo(this.path + File.separator + path[i], fileName, true);
      //2005/04/30 andy 修改:最後一個參數取前端的設定值
      //2005/05/08 andy :path加上subdir
//      copyTo(this.path + File.separator + path[i] + File.separator + subdir, fileName, !clear);
      copyTo(this.path + File.separator + path[i] + File.separator + subdir, sZipFileName, !clear);
    }
    if (reopen)
      open(null);
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
    if (outFile != null) {
      if (outZipFile != null) {
        outZipFile.delete();
      }
      return outFile.delete();
    }
    return false;
  }

  /**
   * 將產生的檔案加到壓縮檔中
   */
  protected void zipFiles(String sZipDir,String sZipName,String sSourFileDir, String sSourFileName) throws IOException {
    //先指定壓縮檔的位置及檔名，建立一個FileOutputStream
    FileOutputStream oOutStream = null;
    ZipOutputStream oZipOutStream = null;
    //每個檔案要壓縮，都要透過ZipEntry來處理
    ZipEntry oZip = null;
    FileInputStream oInStream =null;
    try{
      if(sZipDir.endsWith("\\") || sZipDir.endsWith("/")) {
        oOutStream = new FileOutputStream(sZipDir + sZipName);
      } else {
        oOutStream = new FileOutputStream(sZipDir + File.separator + sZipName);
      }

      //建立ZipOutputStream並將oOutStream傳入
      oZipOutStream =new ZipOutputStream(oOutStream);
      oZipOutStream.setLevel(9);  //設定壓縮率,可選0-9
      //tring rootstr="e:\\temp";   //設定要壓縮的資料夾

      byte[] ch = new byte[256];
      File oSourFile =new File(sSourFileDir,sSourFileName);
      if (oSourFile.isFile()) {
        //以上是只以檔案的名字當Entry，也可以自己再加上額外的路徑
        //oZip=new ZipEntry(myfiles[i].getName());

        //如此壓縮檔內的每個檔案都會加上路徑
        oZip =new ZipEntry(oSourFile.getName());

        oInStream =new FileInputStream(oSourFile);
        //將ZipEntry透過ZipOutputStream的putNextEntry的方式送進去處理
        oZipOutStream.putNextEntry(oZip);

        int len;
        while((len=oInStream.read(ch))!=-1) {
          oZipOutStream.write(ch,0,len); //開始將原始檔案讀進ZipOutputStream
        }
        oInStream.close();
        oInStream = null;
        oZipOutStream.closeEntry();
      }
    } finally {
      if(oInStream != null )  oInStream.close();
      if(oZipOutStream != null) oZipOutStream.close();
      if(oOutStream != null ) oOutStream.close();
      if(oZipOutStream != null) oZipOutStream.close();
    }
  }

}
