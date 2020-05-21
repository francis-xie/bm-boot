package com.emis.webservices.xml.util;

import com.emis.webservices.xml.bean.BeanFactory;
import com.emis.webservices.xml.bean.impl.ClassPathXmlApplicationContext;

public class BeanUtil {

  private static BeanFactory applicationContext = null;

  public static void setApplicationContext(BeanFactory context) {
    applicationContext = context;
  }

  public static Object getBean(String id) {
    return getBean("", id);
  }

  public static Object getBean(String typeId, String id) {
    if (applicationContext == null) {
      applicationContext = new ClassPathXmlApplicationContext();

      applicationContext.init();
    }
    return applicationContext.getBean(typeId, id);
  }

}