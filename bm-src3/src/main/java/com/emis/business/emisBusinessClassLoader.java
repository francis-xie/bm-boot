package com.emis.business;

import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;

import javax.servlet.ServletContext;
import java.io.FileInputStream;


/**
 */
public class emisBusinessClassLoader extends ClassLoader {
  
  //public static final boolean SCHED_RUN_IN_WEBINF = false;
  public String BUS_EMIS_ROOT = "";
  private String sPrjName_;
  private ServletContext oContext_;
  
  public emisBusinessClassLoader (ClassLoader parent,ServletContext oContext)  {
    super(parent);
    this.oContext_ = oContext;
    try{
        emisDirectory temp = emisFileMgr.getInstance(oContext_).getDirectory("root").subDirectory("WEB-INF").subDirectory("classes");

        this.BUS_EMIS_ROOT = temp.getDirectory();
    }catch(Exception e){ }    
  }
  
  protected Class findClass(String sClassName) throws ClassNotFoundException
  {
     
    
    Class oCachedClass = super.findLoadedClass(sClassName);
    if( oCachedClass != null )
    {
        return oCachedClass;
    }
     
    FileInputStream fis = null;
    
    	try{
    		
        fis = new FileInputStream(BUS_EMIS_ROOT + sClassName.replace('.','/') + ".class");
                
    		byte[] temp = new byte[fis.available()]; 
      		
      		int flag = fis.read(temp);  

          	return super.defineClass(sClassName,temp,0,temp.length);
      		
    	}catch(Exception e){
            e.printStackTrace();
            return null;
        }
    
    	finally{
    		try{
    			fis.close();
    		}catch(Exception e){;}
    	}
   }
}    
