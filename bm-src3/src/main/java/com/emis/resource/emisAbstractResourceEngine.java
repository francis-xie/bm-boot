package com.emis.resource;

import com.emis.exception.AppException;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.ResourceBundle;
       
public abstract class emisAbstractResourceEngine{

	HttpServletRequest request= null;
	
	ResourceBundle resource = null;
     	
	Locale locale =null;
	
	String resourcePath= null;
	
	public emisAbstractResourceEngine(HttpServletRequest request,String resourcePath){
	
	 	this.request = request;
	 	
	 	this.resourcePath = resourcePath;
	 	
	}
	
	public abstract ResourceBundle getemisResourceBundle() throws AppException;	
	public abstract void setLocal(String countryCode) throws AppException;	
	public abstract void setResourcePath(String resourcePath) throws AppException;	
	
}

