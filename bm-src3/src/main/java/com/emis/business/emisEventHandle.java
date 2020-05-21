package com.emis.business;

import java.util.Iterator;
import java.util.List;

/**
 *此class是處理 event  並去執行
 */
public class emisEventHandle
{
  private List oQueue ;

  public emisEventHandle ()
  {
    
  }
  //Roland added
  public static void eventHandle (emisBusinessResourceBean resourceBean) throws Exception
  {
    
    emisEventHandle eventHandle = new emisEventHandle ();
    //if( (resourceBean.getEmisErrorEvent ()).getErrorCode () != "GRN0000" ){    
        eventHandle.eventHandleHelp (resourceBean);    
        eventHandle.doEvent ();
    //}else{
     // return true;
    //}
  }
  private void eventHandleHelp (emisBusinessResourceBean resourceBean) throws Exception
  {  
//    String _sErrorHandle ="ErrorHandle";
//    oQueue = new ArrayList ();
//    String[] sEvents = (String[])(resourceBean.getEmisErrorEvent()).getEventElement(_sErrorHandle);
//    //String[] sEventName = parse2EventHandleTag(sEvents);
//    int iSize = sEvents.length;
//    Element oElement;
//        //emisBusinessClassLoader oClassLoader = new emisBusinessClassLoader(ClassLoader.getSystemClassLoader(),sPrjName_);
//        for(int i =0 ; i< iSize ;i++){
//        //Class c = oClassLoader.loadClass((String)sClassName_.get(i));
//            //oElement = (Element)resourceBean.getEmisErrorEvent().getEventElement(sEventName[i]);
//            Object[] argValues={resourceBean};//args.toArray();
//            Class[] argtypes={emisBusinessResourceBean.class};
//            Class c =Class.forName(sEvents[i],true,ClassLoader.getSystemClassLoader());
//        oQueue.add (c.getConstructor(argtypes).newInstance(argValues));
//    }
    /*
    try
    { 
        if( (level & DOWEB)  > 0 )
        { oQueue.add (new emisWebEvent (resourceBean));}
        if( (level & DOMAIL) > 0 )
        { oQueue.add (new emisMailEvent (resourceBean));}
        if( (level & DOLOG)  > 0 )
        {oQueue.add (new emisLogEvent (resourceBean));}
        if( (level & DODB)   >0  )
        {oQueue.add (new emisDbEvent (resourceBean));}
      
    }catch(Exception ignore )
    { ;
    }
    */
  }
 private String parse2EventHandleTag(String sTemp){
              int iIndex = sTemp.lastIndexOf(".");
       
       return sTemp.substring(iIndex);
 }  
  /**
   *執行queue 中的 event
   */
  private void doEvent () throws Exception
  {
    boolean Alert =false; 
    Exception e = null;
    Iterator iterator = oQueue.iterator ();
      while(iterator.hasNext ())
      {
        emisEvent oEvent = (emisEvent)iterator.next ();
        try{
            oEvent.doit ();
        }catch(Exception ignore){  Alert = true; e = ignore;}    
      }
          if(Alert) throw e;            
       }
    //return true;
  }
