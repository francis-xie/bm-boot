package com.emis.classloader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 *  為了達成 Dynamic class loading
 *  在 %ROOT$\dynamic 下實作了 class dynamic loading
 *  放在 dynamic 下的 Class 只要更新了,就會重新載入
 *  不用重載入 JVM
 *  每一個載入的 Class ,會有一個相對映的 ClassLoader
 */
public class emisDynamicClassLoader extends ClassLoader
{
    private String sBasePath_;
    private long lastModified_;


    public emisDynamicClassLoader (String sBasePath,long lastModified)
    {
        this.sBasePath_ = sBasePath;
        this.lastModified_ = lastModified;
    }

    public long getLastModified()
    {
        return lastModified_;
    }

    public emisDynamicClassLoader (ClassLoader parent,String sBasePath,long lastModified)
    {
        super(parent);
        this.sBasePath_ = sBasePath;
        this.lastModified_ = lastModified;
    }

/*
    public Class loadClass(String name) throws ClassNotFoundException
    {
        return findClass(name);
    }
*/



    protected Class findClass(String sClassName) throws ClassNotFoundException
    {
//System.out.println("find:"+sClassName);
        Class oCachedClass = findLoadedClass(sClassName);
        if( oCachedClass != null )
        {
//System.out.println("get from cache");
            return oCachedClass;
        }
        FileInputStream fis;
        String sFileName = sBasePath_ + File.separator + sClassName.replace('.' ,File.separatorChar) + ".class";

        try {

            fis = new FileInputStream(sFileName);
            try {
                BufferedInputStream bis = new BufferedInputStream(fis);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    int c = bis.read();
                    while( c != -1) {
                        out.write(c);
                        c=bis.read();
                    }
                    byte [] classData = out.toByteArray();
//System.out.println("get from file");
                    Class oClass = defineClass(sClassName,classData,0,classData.length);
//System.out.println("get class:"+oClass);
                    return oClass;
                } catch (Exception e) {
//e.printStackTrace( System.out );
                    return null;
                } finally {
                    try {
                        out.close();
                    } catch (Exception ignore1) {}
                }
            } finally {
                // it is dynamic loading , not need to lock it
                try {
                    fis.close();
                } catch (Exception ignore) {}
            }
        }catch(Exception e) {
            return null;
        }

    }

}