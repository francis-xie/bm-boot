package com.emis.classloader;



import com.emis.file.emisDirectory;

import com.emis.file.emisFileMgr;

import com.emis.manager.emisAbstractMgr;

import com.emis.trace.emisError;

import com.emis.trace.emisTracer;

import sun.tools.javac.Main;



import javax.servlet.ServletContext;

import java.io.ByteArrayOutputStream;

import java.io.File;

import java.io.FileInputStream;

import java.io.PrintStream;

import java.util.HashMap;



/**

 *  emisLoaderMgr 負責管理 Dynamic 下的 Class

 *  如果檔案有更新 (.java) 則會自動 Compiler,load

 *  方便開發

 */

public class emisLoaderMgr extends emisAbstractMgr

{

    public static final String STR_EMIS_RPT_CLASSLOADER="com.emis.rpt.loader";



    private String sBasePath_;

    // Class 和 ClassLoader 的對映的 Map

    private HashMap oLoaderMap_ = new HashMap();



    public emisLoaderMgr(ServletContext application) throws Exception

    {

        super(application,STR_EMIS_RPT_CLASSLOADER,"class dynamic loader");



        emisFileMgr _oFMgr = emisFileMgr.getInstance(application);

        emisDirectory _oDir = _oFMgr.getDirectory("dynamic");

        this.sBasePath_ = _oDir.getDirectory();

    }



    public static emisLoaderMgr getInstance( ServletContext oContext ) throws Exception

    {

        emisLoaderMgr _oMgr = (emisLoaderMgr) oContext.getAttribute(emisLoaderMgr.STR_EMIS_RPT_CLASSLOADER);

        if( _oMgr == null )

        {

          emisTracer.get(oContext).sysError(null,emisError.ERR_SVROBJ_NOT_BIND,"emisLoaderMgr");

        }

        return _oMgr;

    }







    public Class findClass(String sClassName) throws Exception

    {

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      try {
        return cl.loadClass("com.emis.dynamicRpt." + sClassName);
      } catch (Exception e) {
      }
      return cl.loadClass(sClassName);

    }



    private void compileIt(String sFileName) throws Exception

    {

        String sJavaFileName = sFileName + ".java";

        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        try {

          PrintStream ps = new PrintStream(bs,true);

          try {

            sun.tools.javac.Main main = new Main(ps,"javac");

            String [] sParam = new String[7];

            sParam[0]= sJavaFileName;

            sParam[1]= "-classpath";



            sParam[2]= System.getProperty("java.class.path") +File.pathSeparator+ sBasePath_;

            sParam[3]= "-d"; // output directory

            sParam[4]= sBasePath_;

            // 增加encoding參數，指定為utf-8,這樣我們在改寫報表類中的中文不要另外轉碼了  update by Andy 2008/10/08
            sParam[5]= "-encoding" ;
            sParam[6]= "utf-8" ;


            if (! main.compile(sParam) )

            {  // not successful

               String sErrorMsg = new String( bs.toByteArray());

               // it will generate class file , we should delete it !

               File f = new File(sFileName + ".class");

               if( f.exists() && f.isFile() )

               {

                 f.delete();

               }

               throw new Exception(sErrorMsg);

            }

          } finally {

            ps.close();

            ps = null;

          }

        } finally {

          bs.close();

          bs = null;

        }

    }

    public void setProperty(int propertyID,Object oValue) throws Exception

    {

    }

}