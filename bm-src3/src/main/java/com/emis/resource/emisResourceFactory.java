package com.emis.resource;

import javax.servlet.http.HttpServletRequest;

public class emisResourceFactory{
    
    HttpServletRequest request = null;
    String resourcePath =null;
    public emisResourceFactory(HttpServletRequest request,String resourcePath){
    	
    	this.request = request;
    	
        this.resourcePath = resourcePath;	
    }
	
    public emisAbstractResourceEngine getResource() {
        
        return getResource(emisResourceConst.EMIS_DEFAULT_RESOURCE);
    }

    public emisAbstractResourceEngine getResource(int ResourceType) {
    
        if (ResourceType == emisResourceConst.EMIS_DEFAULT_RESOURCE) {
        
            return new emisResourceEngine(request,resourcePath);
            
        } else if (ResourceType == emisResourceConst.EMIS_ANOTHER_RESOURCE) {
        
            return new emisResourceEngine(request,resourcePath);
            
        }else{
            return new emisResourceEngine(request,resourcePath);
        }
    }
	
}

