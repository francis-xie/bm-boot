package com.emis.test;

import com.emis.db.emisDb;

public class emisDbTestMain extends emisTest{

  public emisDbTestMain() throws Exception
  {
      super(System.out);
  }

  public static void main(String[] args)  throws Exception
  {
      emisDbTestMain _oTestMain = new emisDbTestMain();
  }

  public void test() throws Exception
  {
      emisDb _oDb = emisDb.getInstance(oContext_);
      try {
          _oDb.executeQuery("Select * from store");
          while(_oDb.next())
          {
              super.out_.println(_oDb.getString("S_NAME"));
          }
      } finally {
          _oDb.close();
      }
  }
}