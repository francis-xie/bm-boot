package com.emis.business;


/**
 *此class 會做 throw exception 2 web
 *
 */
public class emisAlertEvent extends emisEvent
{
  
  private emisBusinessResourceBean oResourceBean_;
  public emisAlertEvent (emisBusinessResourceBean oResourceBean)
  {
    super(oResourceBean);
    this.oResourceBean_ = oResourceBean;
  }
/*  public void doit () throws Exception
  {
    System.out.println ("<---------Alert Event---------------------->");
    //Hashtable oEventMap = ParseEventElement ((Element)oResourceBean_.getEmisErrorEvent ().getEventElement (parse2EventHandleTag (this.getClass ().getName ())));
    try
    {
      //throw new Exception( getMessage((String)oEventMap.get("MessageID")));
      throw new Exception (getIDMessage ("MessageID"));
    }catch(Exception e)
    { throw e;}
 
  }*/
  public void run ()
  {
    try{

      throw new Exception (getIDMessage ("MessageID"));
    }catch(Exception e)
    {
      e.printStackTrace ();
    }finally{
      
    }
  }
}