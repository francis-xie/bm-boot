package com.emis.qa;

import com.emis.db.emisDb;
import com.emis.db.emisDbConnector;
import com.emis.db.emisDbMgr;
import com.emis.spool.emisComplexSpool;
import com.emis.spool.emisSpoolSnapShot;

public class emisDbSpoolTest extends emisTest {

  public emisDbSpoolTest() throws Exception
  {
      super(System.out);
  }

  public static void main(String[] args)  throws Exception
  {
      emisDbSpoolTest _oTest = new emisDbSpoolTest();
  }

  public void test() throws Exception
  {
      emisDbMgr _oMgr = emisDbMgr.getInstance(oContext_);
      if( _oMgr == null )
      {
          out_.println("Null Database Connector Manager");
          return;
      }
      emisDbConnector _oConnector = _oMgr.getConnector("pos");
      if( _oConnector == null )
      {
          out_.println("Null Database Connector");
      }
      emisComplexSpool _oSpool = (emisComplexSpool) _oConnector;

      out_.println("Expire:" + _oSpool.getExpire());
      out_.println("TimeOut:" + _oSpool.getTimeOut());
      out_.println("Orphan:"+ _oSpool.getOrphan());
      out_.println("Interval:"+ _oSpool.getInterval());
      out_.println("Max:" + _oSpool.getMaxSize() );
      out_.println("Min:" + _oSpool.getMinSize() );
      out_.println("Init:" + _oSpool.getInitSize() );
      showStatus(_oSpool);

      out_.println("_______________________________");
//      out_.println("Orphan test:");
//      emisDb _oDb = emisDb.instanciate("pos");

      out_.println("Over Resource test:");
      /*
      emisDb _oDb1 = null;
      _oDb1 = emisDb.instanciate("pos");
      _oDb1 = emisDb.instanciate("pos");
      _oDb1 = emisDb.instanciate("pos");
      _oDb1 = emisDb.instanciate("pos");
      */


      int _nCounter = 1;
      while( true )
      {
        Thread.currentThread().sleep(5 * 1000);

        _nCounter++;
        try {
//             showStatus(_oSpool);
             emisDb _oDb3 = emisDb.getInstance(oContext_,"pos");
             if( _oDb3 != null ) _oDb3.close();
             out_.println("----after");
//             showStatus(_oSpool);
             out_.println("_________________________________" + _nCounter);
        } catch(Exception e) {
            System.out.println("Exception:" + e.getMessage() );
        }

      }
  }

  void showStatus(emisComplexSpool _oSpool)
  {
      emisSpoolSnapShot _oShot = _oSpool.getSnapShot();
      out_.println("Pooled:"+ _oShot.getPooledSize());
      out_.println("CheckOut:"+_oShot.getCheckedOutSize());

  }

}