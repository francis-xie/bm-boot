package com.emis.test;

import com.emis.server.emisServer;
import com.emis.server.emisServerFactory;

import javax.servlet.ServletContext;
import java.io.PrintStream;

abstract public class emisTest {
  protected PrintStream out_;
  emisServer oServer_ ;
  protected ServletContext oContext_;
  
  public emisTest(PrintStream out) throws Exception {
    oContext_ = new emisServletContext();
    oServer_ =emisServerFactory.createServer(oContext_,"C:\\wwwroot\\epos\\","C:\\resin\\epos.cfg",false);
    out_ = out;
    out_.println("Server Object="+oServer_);
    out_.println("Start Test_____________________");
    test();
  }
  
  abstract void test() throws Exception;
  
}