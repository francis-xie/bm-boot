package com.emis.test;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
public class emisResourceTest {
  public static void main(String[] args)  throws Exception
  {
      ResourceBundle _res = PropertyResourceBundle.getBundle("test");
      System.out.println(_res.getString("test"));
      try {
          System.out.println(_res.getString("test1"));
      }catch(Exception e) {
      }

  }
}