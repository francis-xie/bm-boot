package com.emis.business;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

abstract public class emisEvent implements Runnable
{
  private emisBusinessResourceBean oResourceBean_;
  private ResourceBundle oResBund_;
  public boolean run = false;
  public String sBaseName_="com.emis.property.testBundle";
  Hashtable oEventMap; 
  public emisEvent(emisBusinessResourceBean oResourceBean){
    try{
        this.oResourceBean_ = oResourceBean;
//       20040/08/07 Jacky 因為emisErrorEvent已改由emisCodeMapper更新
      //         oEventMap = ParseEventElement((Element)oResourceBean_.getEmisErrorEvent().
//              getEventElement(parse2EventHandleTag(this.getClass().getName())));
        oResBund_= ResourceBundle.getBundle (sBaseName_, Locale.getDefault (),
            this.getClass ().getClassLoader ());
    }catch(Exception e){
       e.printStackTrace ();
    }
  }
  public void doit () throws Exception{
    try{
      Thread runner = new Thread (this);
      runner.start ();
    }catch(Exception e){
      e.printStackTrace ();
    }
  }
  public String getTagValue(String sTagName){
    return (String)oEventMap.get(sTagName );
  }
  public String getIDMessage (String sTagName)
  {
    return oResBund_.getString (getTagValue(sTagName));
  }
 /* public String getMessage (String sMsgID)
  {
    
    System.out.println (this.getClass ().getClassLoader ());
    
    return (ResourceBundle.getBundle (sBaseName_, Locale.getDefault (),this.getClass ().getClassLoader ())).getString (sMsgID);
    
  }*/
  
  public String parse2EventHandleTag (String sTemp)
  {
    int iIndex = sTemp.lastIndexOf (".");
    
    return sTemp.substring (iIndex+1);
  }
  public Hashtable ParseEventElement (Element e)
  {
    Hashtable oMap = new Hashtable ();
    NodeList eAction  = e.getChildNodes ();
    for(int i =0 ; i< eAction.getLength (); i++)
    {
      Node n = eAction.item (i);
      if( n.getNodeType () != Node.ELEMENT_NODE) continue;
      String sNodeName = new String (n.getNodeName ());
      Node first = n.getFirstChild ();
      String sNodeValue = new String (first.getNodeValue ());
      
      oMap.put (sNodeName,sNodeValue);
      
      
    }
    return oMap;
  }
}