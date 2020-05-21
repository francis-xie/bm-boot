package com.emis.schedule;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Apr 16, 2004
 * Time: 11:59:46 AM
 * To change this template use Options | File Templates.
 */
public class emisSchDiskSpaceHelp {
  public emisSchDiskSpaceHelp(){}

  public String ByteToKbyte(long Value){

       return  Value/1024+"KB\n";
  }
    /**
     * C:\reing\log
     * C:\wwwroot\專案\log
     * @param sPathName
     * @return
     */
 public String getLogFileSize(String sPathName){
      long iLogFileSize=0;
      File f = new File(sPathName);
      File[] oLogFile  =   f.listFiles();
      for(int i=0 ; i< oLogFile.length ;i++ ){
           iLogFileSize += oLogFile[i].length();
      }
      return ByteToKbyte(iLogFileSize);
      //return iLogFileSize+"";
  }
/**
 * 直接抓取 wwwroot的log
 * @param sDriver
 * @param sProjectName
 * @return
 */
 public String getWWWWROOTLogFileSize(String sDriver,String sProjectName){
      long iLogFileSize=0;
      File f = new File(sDriver +":\\wwwroot\\"+sProjectName+"\\log");
      File[] oLogFile  =   f.listFiles();
      for(int i=0 ; i< oLogFile.length ;i++ ){
           iLogFileSize += oLogFile[i].length();
      }
      return ByteToKbyte(iLogFileSize);
      //return iLogFileSize+"";
  }
 /**
  * 直接抓resin 的log
  * @param sDriver
  * @return
  */

  public String getResinLogFileSize(String sDriver){
      long iLogFileSize=0;
      File f = new File(sDriver +":\\resin\\log");
      File[] oLogFile  =   f.listFiles();
      for(int i=0 ; i< oLogFile.length ;i++ ){
           iLogFileSize += oLogFile[i].length();
      }
      return ByteToKbyte(iLogFileSize);
      //return iLogFileSize+"";
  }
  /**
   * 抓取db的 space
    * @param sDrive db 所在位置
   * @return
   */
  public String getDbFreeSpace(String sDrive){
     String sMessage="偵測不到資料庫空間";
     List list = new ArrayList();

     Process p =null;
     InputStream io =null;
     InputStreamReader  InputStreamReader = null;
     BufferedReader br =null;
     try{
         p = Runtime.getRuntime().exec("cmd.exe /c  dir "+sDrive+":");
         io = p.getInputStream();
         InputStreamReader = new InputStreamReader(io);

         String Line;
         br = new BufferedReader( InputStreamReader );
         while ((Line = br.readLine()) != null) {
                 list.add(Line);
         }
        if(list.size() > 0 ){
            try{
             sMessage = ((String)list.get(list.size()-1)).substring(20,32)+"KB\n";  //KB
            }catch(Exception e ){
             sMessage ="can not connect DB SPACE";
            }
        }
     }catch(Exception e){
        e.printStackTrace();
     }finally{
        try{
         if(p != null)  p.destroy();
         if(io != null) io.close();
         if(InputStreamReader != null) InputStreamReader.close();
         if(br != null)br.close();
        }catch(Exception ioe){

        }

     }
      //return ByteToKbyte(Long.parseLong(((String)list.get(list.size()-1)).substring(20,36)));

     return sMessage;
  }
    /**
      *
      * @param sType  ZIP FILE的檔頭  resin20040421.zip
      * @param sBackupDir  zip 備份的路徑
      * @param sSourceDir  zip 從哪邊備份的
      * @param isDeletefile   備份檔案須不需要刪除
      * @return
      */
  public String jarFile(String sType, String sBackupDir,String sSourceDir,boolean isDeletefile){
     try{
     List oCommand = new ArrayList();
     List oFileDelete = new ArrayList();
     oCommand.add("-cvfM");
     oCommand.add(sBackupDir+"\\"+sType+getNextDate(0,"yyyyMMdd")+".zip");
     File f = new File(sSourceDir);
     File[] oLogFile  =   f.listFiles();
         for(int i =0;i<oLogFile.length;i++){
               if(oLogFile[i].isFile()){
                   oCommand.add("-C");
                   oCommand.add(sSourceDir);
                   oCommand.add(oLogFile[i].getName());
                   oFileDelete.add(oLogFile[i]);
               }
         }
     //String[] test={"-cvfM","c:\\abeltest.zip","-C","c:\\resin\\log","error.log","c:\\resin\\log","error.log.1"};
     sun.tools.jar.Main jar = new sun.tools.jar.Main(System.out, System.err, "jar");
     jar.run((String[]) oCommand.toArray(new String[]{}));
           if(isDeletefile){
                  for(int i =0 ;i < oFileDelete.size(); i++){
                        try{
                          ((File)oFileDelete.get(i)).delete();
                        }catch(Exception e){;}
                  }
            }
      return "成功\n";
     }catch(Exception e){e.printStackTrace();
       return "失敗\n";
     }

    }
    /**
     * 抓天的method
     * @param roll 旋轉天數
     * @param format  日期 format
     * @return
     */
    private String getNextDate(int roll, String format) {

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            calendar.add(Calendar.DATE, roll);
            return formatter.format(calendar.getTime());
    }

    public static void main(String[] args){
           emisSchDiskSpaceHelp d = new emisSchDiskSpaceHelp();
           // System.out.println(d.getResinLogFileSize("c"));
           // System.out.println(d.getResinLogFileSize("c"));
            System.out.println(d.getDbFreeSpace("c"));

    }
}
