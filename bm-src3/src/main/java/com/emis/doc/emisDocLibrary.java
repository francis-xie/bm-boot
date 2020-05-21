package com.emis.doc;

import java.io.File;

/**
 * 此 Class 是獨立的一個 Class
 * 用來產生 emis Library JavaDoc
 * 因為 package 太多 , Command Line 無法下,
 * 請注意使用之前 classpath 要含
 * %JAVA_HOME%\lib\tools.jar;
 * 還有 Servlet Interface (Servlet.jar)
 * 在 resin 是 jsdk22.jar ( 或 jsdk23.jar )
 * 還有 XML (dom.jar, sax.jar,jaxp.jar)
 */
public class emisDocLibrary
{
  public static void main(String [] argv) throws Exception
  {
    if ( argv.length != 2 ) {
      System.out.println("usage\n  java.exe com.emis.doc.emisDocLibrary SourcePath OutputPath");
      return;
    }
    File oDir = new File(argv[0]);
    if( !oDir.exists() || ! oDir.isDirectory() )
    {
      System.out.println("Directory not exists or is not a Directory");
      return;
    }
    oDir = null;
    oDir = new File(argv[1]);
    if( !oDir.exists() || ! oDir.isDirectory() )
    {
      System.out.println("Directory not exists or is not a Directory");
      return;
    }
    String sHome=System.getProperty("java.home");
    String sClassPath = System.getProperty("java.class.path","");
    if( sClassPath.toUpperCase().indexOf("LIB" + File.separator + "TOOLS.JAR") == -1 )
    {
      oDir = new File(sHome);
      sHome = oDir.getParent();
      String newPath = sClassPath+File.pathSeparator+
                       sHome+
                       File.separator+"lib"+File.separator+"tools.jar";
//      System.out.println(newPath);
      System.setProperty("java.class.path",newPath);
    }

    String[] javadocargs = {
     "-sourcepath", argv[0],
     "-public",
     "-encoding", "UTF-8",

     "-nodeprecated",
     "-d", argv[1],
     "com.emis.business",
     "com.emis.cache",
     "com.emis.chart",
     "com.emis.classloader",
     "com.emis.db",
     /*
     "com.emis.db.inet",
     "com.emis.db.oracle",
     "com.emis.db.pervasive",
     "com.emis.db.odbc",
     */
     "com.emis.file",
     "com.emis.mail",
     "com.emis.manager",
     "com.emis.report",
     "com.emis.schedule",
/*     "com.emis.schedule.download",
     "com.emis.schedule.realtime",
     "com.emis.schedule.dailyclose",*/
     "com.emis.server",
     "com.emis.servlet",
     "com.emis.spool",
     "com.emis.trace",
     "com.emis.user",
     "com.emis.util",
     "com.emis.xml",
     "com.emis.qa",
     "com.emis.http"

     };
 //       Class.forName("com.sun.tools.javadoc.Main");
        com.sun.tools.javadoc.Main.main(javadocargs);
  }
}
