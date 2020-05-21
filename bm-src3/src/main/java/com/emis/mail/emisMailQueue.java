package com.emis.mail;

import com.emis.manager.emisAbstractMgr;
import com.emis.trace.emisError;
import com.emis.trace.emisTracer;import com.emis.db.emisProp;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
/**
 * Track+[13796] tommer.xie 2009/11/26 將發送郵件參數添加至EMISPROP系統參數表，由用戶自行設定。
 */


public class emisMailQueue extends emisAbstractMgr implements Runnable
{
  public static final String STR_EMIS_MAILQUEUE = "com.emis.mail.queue";
  private Session oMailSession_;
  private Thread mailthread_ ;
  private long interval_;
  private long nSent_ ;
  private emisProp prop_;


  // vector 有 synchronization , ArrayList 沒有
  private Vector queue_ = new Vector();

  public emisMailQueue(ServletContext application,Properties props) throws Exception
  {

        super(application,STR_EMIS_MAILQUEUE,"MailQueue");

        emisTracer _oTr = emisTracer.get(application);
        prop_=emisProp.getInstance(application);
        if ( prop_.get("MAIL_SMTP_ADDRESS") == null )

        {
          _oTr.warning(this,"please setup mail host 'mail.smtp.host'");
        }

        // default 的秒數
        String _sInterval = props.getProperty("emis.mail.queue.interval","15");
        try {
          interval_ = Integer.parseInt(_sInterval);
        } catch (Exception errInterval) {
          emisTracer.get(application_).info("error setting mail queue interval:"+_sInterval);
          // set it to default value;
          interval_ = 15;
        }

        _oTr.info("set mail queue interval to "+interval_+"s");
        interval_ = interval_ * 1000;

        // start up thread
        mailthread_ = new Thread(this);
        mailthread_.setName("emis Mail Queue");
        mailthread_.setDaemon(true);
        mailthread_.start();
  }

  public void run()
  {
   ArrayList a ;
   queue_.iterator();
    try {
      while (true)
      {
        processMailQueue();
        Thread.currentThread().sleep(interval_);
      }

    } catch (Exception e ) {
      try {
        emisTracer.get(application_).warning(this,e);
      }catch (Exception ignore){}
    }
  }

  public void add( Message m )
  {
    queue_.add(m);
  }

  protected void processMailQueue()
  {
    int size = queue_.size();
    int idx = size-1;
    while ( idx >= 0 )
    {
      try {
        Message m = (Message) queue_.get(idx);
        Transport.send(m);
      } catch (Exception e) {
        try {
          emisTracer.get(application_).warning(this,e);
        }catch (Exception ignore){}
      } finally {
        try {
          queue_.removeElementAt(idx);
          nSent_++;
        } catch (Exception ignore1) {}
        idx--;
      }
    }
  }


  public void setProperty(int propertyID,Object oValue) throws Exception
  {
  }

  public static emisMailQueue getInstance(ServletContext application) throws Exception
  {
      emisMailQueue q = (emisMailQueue) application.getAttribute(STR_EMIS_MAILQUEUE);
      if( q == null )
      {
          emisTracer.get(application).sysError(null,emisError.ERR_SVROBJ_NOT_BIND,"emisMailQueue");
      }
      return q;
  }

  public long getMailSentCount()
  {
      return nSent_;
  }

  public Iterator iterator()
  {
      return queue_.iterator();
  }

}