/* Id$
 *
 * Copyright(c) EMIS Corp.
 */
package com.emis.http;

import com.emis.test.emisAbstractTestCase;

import java.io.FileWriter;
import java.io.IOException;

/**
 * emisSasDownload test.
 * @author Jerry 2004/6/30 上午 10:23:53
 * @version 1.0
 */
public class emisSasDownloadTest extends emisAbstractTestCase {
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
   * @throws Exception
   */
  public void testtestWriting() throws Exception {
    assertNotNull(oContext_);
    FileWriter out = null;
    try {
      out = new FileWriter("c:/wwwroot/eros/data/download/5001/test.dat");
      for (int i=1; i<=1000; i++) {
        out.write("2222222222222222222222222.\n");
        if (i % 10 == 0)
          out.flush();
        Thread.sleep(300);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      out.close();
    }
  }
}
