/* Id$
 *
 * Copyright(c) EMIS Corp.
 */
package com.emis.http;

import com.emis.test.emisAbstractTestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * emisSasDownload test.
 * @author Jerry 2004/6/30 上午 10:23:53
 * @version 1.0
 */
public class emisSasDownloadTest2 extends emisAbstractTestCase {
  //-private ServletContext oContext_;

  /**
   * setUp.
   * @throws Exception
   */
  protected void setUp() throws Exception {
    super.setUp("eros");
  }

  /**
   * tearDown.
   * @throws Exception
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * 測試正在寫檔中的下傳處理.
   */
  public void testtestWriting() throws Exception {
    assertNotNull(oContext_);
    FileWriter out = null;
    try {
      String _sFile = "c:/wwwroot/eros/data/download/5001/test.dat";
      out = new FileWriter(_sFile,false);
      for (int i=1; i<=10000; i++) {
        out.write("this is a test.\n");
        //Thread.sleep(300);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      out.close();
    }
  }
  /**
   * 測試正在寫檔中的下傳處理.
   */
  public void testRename() throws Exception {
    assertNotNull(oContext_);
    try {
      String _sFile = "c:/wwwroot/eros/data/download/5001/test.dat";
      File _oFile = new File(_sFile);
      System.out.println("can write=" + _oFile.canWrite());
      File _oFile2 = new File(_sFile+".wrt");
      boolean _isOK = _oFile.setLastModified((new Date()).getTime());  //.renameTo(_oFile2);
      System.out.println("ok=" + _isOK);
      if (_oFile2.exists()) {
        System.out.println("copied.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
    }
  }

}
