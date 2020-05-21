package com.emis.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class emisResourceHelp{
	
  public static String getLanguageHelp(String Head){
  
  	List list = new ArrayList();
  	
  	StringTokenizer test = new StringTokenizer(Head,",");
    	
    	while(test.hasMoreTokens()){
    			list.add( test.nextToken());    		
    	}
        
        //String[] temp = (String[])list.toArray(new String[]{});
        
    	return (String)list.get(0);
  
  }	
}

