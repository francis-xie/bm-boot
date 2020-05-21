package com.emis.webservices.xml;

import com.emis.webservices.xml.bean.impl.ClassPathXmlApplicationContext;
import com.emis.webservices.xml.util.BeanUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Bootstrap listener
 * <p/>
 * <p>This listener should be registered after
 * in <code>web.xml</code>, if the latter is used.
 *
 * <context-param>
 *  <param-name>com.emis.webservices.xml.contextConfigLocation</param-name>
 *  <param-value>
 *    /business/applicationContext.xml
 *  </param-value>
 * </context-param>
 *
 * <listener>
 *   <listener-class>com.emis.webservices.xml.ContextLoaderListener</listener-class>
 * </listener>
 *
 */
public class ContextLoaderListener implements ServletContextListener {


  /**
   * Initialize the root web application context.
   */
  public void contextInitialized(ServletContextEvent event) {
    String path = event.getServletContext().getInitParameter("com.emis.webservices.xml.contextConfigLocation");
    InputStream resourceAsStream = null;
    try {
      path = event.getServletContext().getRealPath(path);
      if (path != null && !"".equals(path.trim())) {
        resourceAsStream = new FileInputStream(path);
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
        applicationContext.parseContext(resourceAsStream);
        BeanUtil.setApplicationContext(applicationContext);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Close the root web application context.
   */
  public void contextDestroyed(ServletContextEvent event) {

  }

}