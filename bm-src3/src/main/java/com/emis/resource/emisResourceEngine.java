package com.emis.resource;

import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;

public class emisResourceEngine extends emisAbstractResourceEngine{
	                               
	
	public emisResourceEngine(HttpServletRequest request,String resourcePath){
	        
	        super(request,resourcePath);
	        
	        prepareEmisResourceBundle();
	}
	
	
	private void prepareEmisResourceBundle(){
	        
	        String sSupportLanguage = emisResourceHelp.getLanguageHelp(request.getHeader(emisResourceConst.ACCEPT_LANGUAGE));
	        
	        this.locale = emisResourceComposer.getLocale(sSupportLanguage);
	        
	}
	
	public void setLocal(String countryCode){
	
		this.locale = emisResourceComposer.getLocale(countryCode);
		
	}
	
	public void setResourcePath(String resourcePath){
	
		this.resourcePath = resourcePath;
		
	}
	
	public ResourceBundle getemisResourceBundle(){
	
		 return  ResourceBundle.getBundle(resourcePath, locale);
	
	}	  
 
}

