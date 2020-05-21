package com.emis.business;

import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;

import java.io.PrintWriter;
/**
 *此class 是把message 寫入file
 */
public class emisLogEvent extends emisEvent
{
  
  private emisBusinessResourceBean oResourceBean_;
  private String sMessage_;
  private String sLogFileName_;
  public emisLogEvent (emisBusinessResourceBean oResourceBean)
  {
    super(oResourceBean);
    this.oResourceBean_ = oResourceBean;
  }
/*  public void doit ()
  {
    try
    {
      //Hashtable oEventMap = ParseEventElement ((Element)oResourceBean_.getEmisErrorEvent ().getEventElement (parse2EventHandleTag (this.getClass ().getName ())));
      System.out.println ("<----------Log Event--------------------->");
      //System.out.println ("MessageID    ==="+(String)oEventMap.get ("MessageID"));
      //System.out.println ("MessageID  value  ==="+getMessage ((String)oEventMap.get ("MessageID")));
      //sMessage_ = getMessage ( (String)oEventMap.get ("MessageID") );
      sMessage_ = getIDMessage ( "MessageID");
      sLogFileName_ = getTagValue ("Name");
      Thread runner = new Thread (this);
      runner.start ();
    }catch(Exception e)
    {e.printStackTrace ();}
  }*/
  public void run ()
  {
    emisErrorEvent errorEvent = oResourceBean_.getEmisErrorEvent ();
    String Message = null;
    PrintWriter oPrint= null;
    try
    {
      System.out.println ("<----------Log Event--------------------->");
      //System.out.println ("MessageID    ==="+(String)oEventMap.get ("MessageID"));
      //System.out.println ("MessageID  value  ==="+getMessage ((String)oEventMap.get ("MessageID")));
      //sMessage_ = getMessage ( (String)oEventMap.get ("MessageID") );
      sMessage_ = getIDMessage ( "MessageID");
      sLogFileName_ = getTagValue ("Name");
      emisDirectory _oRootDir = emisFileMgr.getInstance (oResourceBean_.getServletContext ()).getDirectory ("root");
      emisDirectory _oSubDir = _oRootDir.subDirectory ("errorLog");
      emisFile f = _oSubDir.getFile (sLogFileName_);
      oPrint = f.getWriter ("AF");
      oPrint.println (sMessage_);
      
    }catch(Exception e)
    {e.printStackTrace ();}
    finally
    {
      oPrint.close ();

    }
    
  }
}