package com.emis.resource;

import java.util.Locale;

public class emisResourceComposer{
	
	public static Locale getLocale(String Language){
              
              Locale local = Locale.getDefault();
              
              if(Language.equalsIgnoreCase(emisResourceConst.LANGUAGE_ZH_TW)){ 
              	
              	local = local.TAIWAN;
              
              }else if(Language.equalsIgnoreCase(emisResourceConst.LANGUAGE_EN)){
                 
                  local = local.US;
              
              }else if(Language.equalsIgnoreCase(emisResourceConst.LANGUAGE_EN_US)){
                 
                  local = local.US;
                
              }else if(Language.equalsIgnoreCase(emisResourceConst.LANGUAGE_ZH_CN)){
                  
                 local = local.CHINA;
              }else{
                 System.out.println("not support Language--");
              }
               
	
	 return local;
	}	  
	
}

