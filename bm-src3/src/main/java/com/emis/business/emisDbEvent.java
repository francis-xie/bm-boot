package com.emis.business;

import com.emis.db.emisDb;
/**
 *此class 會做 db event   寫資料到db
 *
 */
public class emisDbEvent extends emisEvent
{
  
  private emisBusinessResourceBean oResourceBean_;
  private String sDbTableName_;
  private emisDb oDb_;
  private String sError_code;
  private String sError_message;
  public emisDbEvent (emisBusinessResourceBean oResourceBean)
  {
    super(oResourceBean);
    this.oResourceBean_ =  oResourceBean;
  }
 /* public void doit ()
  {
  
  
    System.out.println ("<---------db Event---------------------->");
    try
    {
      //Hashtable oEventMap = ParseEventElement ((Element)oResourceBean_.getEmisErrorEvent ().getEventElement (parse2EventHandleTag (this.getClass ().getName ())));
      oDb_ = emisDb.getInstance (oResourceBean_.getServletContext ());
      //sDbTableName_ = (String)oEventMap.get ("Name");
      sDbTableName_ = getTagValue ("Name");
      sError_code = (String)oResourceBean_.getEmisErrorEvent ().getErrorCode ();
      //sError_message = getMessage ((String)oEventMap.get ("MessageID"));
      sError_message = getIDMessage ("MessageID");
      Thread runner = new Thread (this);
      runner.start ();
    }catch(Exception e)
    { e.printStackTrace ();}
  }*/
  public void run ()
  {
    
    try
    {

      oDb_ = emisDb.getInstance (oResourceBean_.getServletContext ());
      sDbTableName_ = getTagValue ("Name");
      sError_code = (String)oResourceBean_.getEmisErrorEvent ().getErrorCode ();
      sError_message = getIDMessage ("MessageID");
      oDb_.prepareStmt ("INSERT INTO "+ sDbTableName_+" (error_code,error_message) VALUES (?,?)");
      oDb_.setString (1,sError_code);
      oDb_.setString (2,sError_message);
      oDb_.prepareUpdate ();
    }catch(Exception e )
    { e.printStackTrace ();}
    finally
    {
      oDb_.close ();

    }
    
  }
}