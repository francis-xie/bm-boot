package com.emis.test;

import com.emis.db.emisDb;

public class emisDbTest extends emisTest{

  public emisDbTest() throws Exception
  {
      super(System.out);
  }

  public static void main(String[] args)  throws Exception
  {
      emisDbTest _oTest = new emisDbTest();
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