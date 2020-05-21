package com.emis.app.migration;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * User: merlin
 * Date: Apr 22, 2003
 * Time: 6:41:35 PM
 */
public class emisMiTextSource extends emisMiSource implements FilenameFilter {
  BufferedReader br = null;
  int offset[];
  protected emisMiField[] sourceFields;
  int dataLength;
  boolean wildCard = false;
  String FileList[] = null; // 多檔時使用
  int FileIndex = 0;

   String[] getFileList() {
     File dir = new File(path);
    return dir.list(this);
  }

  public boolean open( emisMiConfig config) {
    if (config.getMigration().getSourcePath() != null) {
      path = config.getMigration().getSourcePath();
    }
    if (wildCard) {
      if (FileList == null) {
        FileList = getFileList();
        if (FileList == null || FileList.length == 0)
          return false;
        FileIndex = 0;
        Arrays.sort(FileList);
      }
      fileName = FileList[FileIndex];
    } else
      fileName = getFileName();
    try {
       File f;
      if (path.endsWith(File.separator)) {
        f = new File(path + fileName);
      } else {
        f = new File(path + File.separator + fileName);
      }
      if (!f.exists())
        return false;
      br = new BufferedReader(new FileReader(f));
    } catch (FileNotFoundException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
      return false;
    }
    sourceFields = config.getSourceFields();
     int dataSize = sourceFields.length;
    if (dataSize == 0)
      System.out.println(fileName);
    result = new String[dataSize];
    offset = new int[dataSize];
    offset[0] = 0;
    for (int i = 1; i < dataSize; i++) {
      offset[i] = offset[i - 1] + sourceFields[i - 1].getLength();
      dataLength += sourceFields[i - 1].getLength();
    }
    dataLength += sourceFields[dataSize - 1].getLength();
    return (br != null);
  }

  public  String[] next() throws IOException {
    String s;
    do {
      s = br.readLine();
    } while (s != null && s.length() == 0);
    if (s == null)
      return null;
/*        if (s == null)
        {
            if (!wildCard)
               return null;
            else {
               while  (++this.FileIndex < FileList.length && s==null) {
                   close(true);       //  最後一個檔案由外部  close
                   backup();         //  最後一個檔案由外部  backup
                   open(this.config);    // 開下一個符合wildcard的檔案
                   s = br.readLine();   //
               }
               if (s == null)
                   return null;
            }
        }
*/
     byte[] buf = s.getBytes();
    if (buf.length < dataLength)
      return null;
    for (int i = 0; i < sourceFields.length; i++) {
       int m = offset[i];
       int n = sourceFields[i].getLength();
      result[i] = new String(buf, m, n);       // java 轉換中文內碼的byte array成字串
      if (result[i].length() == 0 && n != 0)        // 如果讀到最後1個BYTE 是內碼的前一個byte則會
        result[i] = new String(buf, m, n - 1);  // 轉換失敗結果的字串會變成空字串
      // 解決辦法是少轉一個byte
    }
    return result;
  }

  public  boolean close( boolean closeDb) {
    try {
      br.close();
    } catch (IOException e) {
      // Log here;  //To change body of catch statement use Options | File Templates.
    }
    return false;
  }

//  public static boolean backupFile(String path, String fileName, String bkTimestamp,
//                                  String backupPath_) {

  public boolean backup() {
     String backupFile;
    if (bkTimestamp != null && bkTimestamp.length() > 0)
      backupFile = getFileName(bkTimestamp);
    else
      backupFile = fileName;
    if (backupPath_ == null || backupPath_.length() == 0) {
      backupPath_ = path + "\\" + "backup";
    }
     String cmd = "cmd.exe /c move " + path + "\\" + fileName + "  " + backupPath_ + "\\" + backupFile;
    try {
      Runtime.getRuntime().exec(cmd);
    } catch (IOException e) {
    	e.printStackTrace();
      // Log here;  //To change body of catch statement use Options | File Templates.
      return false;
    }
    return true;
  }

   void parse( Hashtable h) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
    super.parse(h);
     Iterator it = h.keySet().iterator();
    String key;
    String val;
    while (it.hasNext()) {
      key = (String) it.next();
      val = (String) h.get(key);
      if (key.equalsIgnoreCase("wildcard")) {
        wildCard = !val.equalsIgnoreCase("false");
      }
    }
  }

  public boolean accept( File dir,  String name) {
    String upName = name.toUpperCase();
    String upPrefix = prefix.toUpperCase();
    String upExt = ext.toUpperCase();
    int nLen = upPrefix.length() + upExt.length() + 1; //  需加上句點的長度
    if (timestamp != null)
      nLen += timestamp.length();
    if (upName.startsWith(upPrefix) && upName.endsWith("." + upExt)) {
      return (name.length() == nLen);
    } else
      return false;
  }
}
