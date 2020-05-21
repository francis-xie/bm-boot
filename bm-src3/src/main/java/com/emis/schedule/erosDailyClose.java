package com.emis.schedule;

import com.emis.db.emisDb;
import com.emis.app.migration.emisMigration;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2004/8/26
 * Time: 下午 03:20:36
 * To change this template use Options | File Templates.
 */
public class erosDailyClose extends emisTask
  {
    FileFilter filter;
    int maxLevel;
    int curLevel=0;

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }


  public static void UnZipFiles(String ZipFile, String dir) throws IOException {
    InputStream in = new BufferedInputStream(new FileInputStream(ZipFile));
    ZipInputStream zin = new ZipInputStream(in);
    ZipEntry e;

    while((e=zin.getNextEntry())!= null) {
          unzip(zin, dir + File.separatorChar + e.getName());
      }
    zin.close();
    }

    public erosDailyClose(ServletContext context) {
        this.oContext_ = context;
        this.filter = new ExtFilter(".zip");
    }

  public static void unzip(ZipInputStream zin, String s) throws IOException {
//    System.out.println("unzipping " + s);
    FileOutputStream out = new FileOutputStream(s);
    byte [] b = new byte[512];
    int len = 0;
    while ( (len=zin.read(b))!= -1 ) {
      out.write(b,0,len);
      }
    out.close();
    }

    public void processFile(File f) {
        emisDb db_ = null;
        try {
            String name = f.getCanonicalPath();
            UnZipFiles(name, "C:\\wwwroot\\ylib\\data\\upload\\all\\endofday\\work");
            db_ = emisDb.getInstance(this.oContext_);
            emisMigration mSale_H = new emisMigration( oContext_, "RT_STCSALE_H");
            mSale_H.setMiDb(db_);
            mSale_H.run();
            emisMigration mSale_D = new emisMigration(oContext_, "RT_STCSALE_D");
            mSale_D.setMiDb(db_);
            mSale_D.run();
            emisMigration mSale_Dis = new emisMigration(oContext_, "RT_STCSALE_DIS");
            mSale_Dis.setMiDb(db_);
            mSale_Dis.run();
            emisMigration mSale_I = new emisMigration(oContext_, "RT_STCSALE_I" );
            mSale_I.setMiDb(db_);
            mSale_I.run();
            emisMigration mSale_P = new emisMigration( oContext_, "RT_STCSALE_P");
            mSale_P.setMiDb(db_);
            mSale_P.run();
            File work = new File("C:\\wwwroot\\ylib\\data\\upload\\all\\endofday\\work");
            File [] list = work.listFiles();
            for (int i =0; i < list.length; i++)
               list[i].delete();
            backup(f);
//            db_.prepareStmt("update cash_id set close");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } finally {
            if (db_ != null) {
                db_.close();
                db_ = null;
            }
        }
    }

    public void recurseInDirFrom(File file) {
        curLevel ++;
        File list[];
        if (file.isDirectory()) {
            list = file.listFiles(filter);
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory() ) {
                    if ( maxLevel > 0 && curLevel < maxLevel) {
                       recurseInDirFrom(list[i]);
                    }
                }
                else {
                    processFile(list[i]);
                }
            }
        }
        curLevel--;
    }

    public static void main(String arg[]) {
        erosDailyClose nf = null;
        try {
     //       nf = new erosDailyClose(emisMigration.getServletContext("ylib"));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        nf.run();
    }

    public void runTask() throws Exception  // 改寫java.lang.Runnable.run()
    {
        setMaxLevel(1);
        String root = "C:\\wwwroot\\ylib\\data\\upload\\all\\endofday";
        File dir = new File(root);
        if (dir.exists()) {
            recurseInDirFrom(dir);
        } else {
            System.out.println(root + " doesn't exist");
        }
    }

    public erosDailyClose() {
        this.filter = new ExtFilter(".zip");
    }

    public boolean backup(File file) {
        try {
        String name = file.getCanonicalPath();
        String backpath  =   name.substring(0,name.lastIndexOf("\\")) + "\\backup\\";
        String cmd = "cmd.exe /c move "+ name  + " " + backpath;
          System.out.println("["+cmd+"]");
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            return false;
        }
        return true;
    }

}

