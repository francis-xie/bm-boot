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
 * Time: 下午 03:26:26
 * To change this template use Options | File Templates.
 */
public class erosRealTime extends emisTask {
    private emisDb _db =null;
    FileFilter filter;
    int maxLevel;
    int curLevel=0;

    public erosRealTime() {
        this.filter = new ExtFilter(".zip");
    }

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

    public erosRealTime(ServletContext context) {
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

     public  void newDb() throws Exception {

          _db = emisDb.getInstance(this.oContext_);
          _db.setAutoCommit(false);
     }

    public void processFile(File f) {
        //emisDb db_ = null;
        try {
            newDb();
            String name = f.getCanonicalPath();
            UnZipFiles(name, "C:\\wwwroot\\ylib\\data\\upload\\all\\realtime\\work");

            emisMigration mSale_H = new emisMigration( oContext_, "STCSALE_H");
            mSale_H.setMiDb(_db);
            mSale_H.run();
            emisMigration mSale_D = new emisMigration(oContext_, "STCSALE_D");
            mSale_D.setMiDb(_db);
            mSale_D.run();
            emisMigration mSale_I = new emisMigration(oContext_, "STCSALE_I" );
            mSale_I.setMiDb(_db);
            mSale_I.run();
            emisMigration mSale_P = new emisMigration( oContext_, "STCSALE_P");
            mSale_P.setMiDb(_db);
            mSale_P.run();
            emisMigration mSale_DIS = new emisMigration( oContext_, "STCSALE_DIS");
            mSale_DIS.setMiDb(_db);
            mSale_DIS.run();

            _db.commit();
            backup(f);
            File work = new File("C:\\wwwroot\\ylib\\data\\upload\\all\\realtime\\work");
            File [] list = work.listFiles();
            for (int i =0; i < list.length; i++)
               list[i].delete();
        } catch (IOException e) {
           e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (Exception e) {
                try{
                    _db.rollback();
                 }catch(Exception ioe){;}
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } finally {
            if (_db != null) {
                _db.close();
                _db = null;
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
        erosRealTime nf = null;
        try {
    //       nf = new erosRealTime(emisMigration.getServletContext("ylib"));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        nf.run();
    }

    public void runTask() throws Exception  // 改寫java.lang.Runnable.run()
    {
        System.out.println("start");
        setMaxLevel(1);
        String root = "C:\\wwwroot\\ylib\\data\\upload\\all\\realtime";
        File dir = new File(root);
        if (dir.exists()) {
//            System.out.println("recursive Dir from " + root);
            recurseInDirFrom(dir);
        } else {
            System.out.println(root + " doesn't exist");
        }
        System.out.println("end");
    }

    public boolean backup(File file) {
        try {
        String name = file.getCanonicalPath();
        String backpath  =   name.substring(0,name.lastIndexOf("\\")) + "\\backup\\";
        String cmd = "cmd.exe /c move "+ name  + " " + backpath;
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            return false;
        }
        return true;
    }

}

