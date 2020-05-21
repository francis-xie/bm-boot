package com.emis.webservices.xml.bean.impl;

import com.emis.webservices.xml.bean.BeanFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于模仿Spring实现的依赖注入，以实现简单解耦
 * emis-beans.xml定义说明（也可叫其他名称）：
 * <bean>
 * id : 必需唯一，不然会覆盖
 * class : 要创建的类对象
 * singleton : true | false 是否为单例，不写默认为 true
 *
 * <property>
 * name : 必需是目标class的一个属性，并提供setXXXX的方法， 如name > setName; lastName(String name) >> setLastName(String lastName)
 * type : string | int | float | double | long ，不写时默认为 string
 * bean : 用于指定配置中的bean id,即引用另一个类
 *
 * Sample:
 * <bean id="reportFactory" class="com.emis.xml.report.impl.ClassPathXmlReportContext" init-method="init" >
 *   <property name="resourcePath" value="emis-reports.xml"/>
 * </bean>
 *
 * <bean id="reportFactory" class="com.emis.xml.report.impl.ClassPathXmlReportContext" init-method="init" >
 *   <property name="name" value="joe"/>
 *   <property name="lastName" value="yao" type="string"/>
 *   <property name="age" value="18" type="int"/>
 *   <property name="reportFactory" bean="reportFactory" />
 * </bean>
 *
 * <bean id="member_bind" class="com.emis.webservices.service.espa.emisSpaBindMemberImpl" singleton="false" />
 * <bean id="appoint_bind" class="com.emis.webservices.service.espa.emisSpaAppointMemberImpl" />
 */
public class ClassPathXmlApplicationContext implements BeanFactory {

  /**
   * beans cache
   */
  private Map<String, Map<String, Object>> singletonsCollection = new HashMap<String, Map<String, Object>>();
  /**
   * Non-singleton beans cache
   */
  private Map<String, Map<String, Element>> mutilbeansCollection = new HashMap<String, Map<String, Element>>();

  /**
   * 默认配置文件路径（相对于ClassPath目录下）
   */
  private String resourceParth = "emis-beans.xml";

  /**
   * IOC Inverse of Control DI Dependency Injection
   *
   * @throws Exception
   */
  public ClassPathXmlApplicationContext() {

  }

  /**
   * IOC Inverse of Control DI Dependency Injection
   *
   * @throws Exception
   */
  public ClassPathXmlApplicationContext(String resourcePath) {
    this.resourceParth = resourcePath;
  }

  public void init() {
    try {
      parseXML(resourceParth);
    } catch (Exception ignore) {

    }
  }

  /**
   * 解析XML
   *
   * @param resourcePath
   * @throws JDOMException
   * @throws IOException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws IntrospectionException
   */
  private void parseXML(String resourcePath) throws IllegalAccessException, IntrospectionException,
      InstantiationException, NoSuchMethodException, JDOMException, InvocationTargetException, ClassNotFoundException {
    // 构造文档对象
    InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
    parseContext(resourceAsStream);
  }

  public void parseContext(InputStream resourceAsStream) throws JDOMException, IllegalAccessException,
      InvocationTargetException, IntrospectionException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
    if (resourceAsStream != null) {
      SAXBuilder sb = new SAXBuilder();
      Document doc = sb.build(resourceAsStream);
      // 获取根元素HD
      Element root = doc.getRootElement();
      // 取名字为disk的所有元素
      @SuppressWarnings("unchecked")
      List<Element> listCollection = root.getChildren("beans");
      for (Element elementCollection : listCollection) {
        String typeId = (elementCollection.getAttributeValue("typeId") != null) ? elementCollection.getAttributeValue("typeId") : "";

        Map<String, Object> singletons = new HashMap<String, Object>();
        Map<String, Element> mutilbeans = new HashMap<String, Element>();

        @SuppressWarnings("unchecked")
        List<Element> list = elementCollection.getChildren("bean");
        String id, singleton;
        // 预先处理，以解决定义在后面的Bean前面要用到
        for (Element element : list) {
          id = element.getAttributeValue("id");
          mutilbeans.put(id, element);
        }
        // 真正进行实例化，并清除单例对象
        for (Element element : list) {
          id = element.getAttributeValue("id");
          singleton = element.getAttributeValue("singleton");
          if (isSingleton(singleton)) {
            singletons.put(id, parseObject(element));
            mutilbeans.remove(id);
          }
        }
        singletonsCollection.put(typeId, singletons);
        mutilbeansCollection.put(typeId, mutilbeans);
      }
    }
  }

  /**
   * 实例化Bean对象
   *
   * @param element
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ClassNotFoundException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws IntrospectionException
   */
  private Object parseObject(Element element) throws InstantiationException,
      IllegalAccessException, ClassNotFoundException, SecurityException,
      NoSuchMethodException, IllegalArgumentException,
      InvocationTargetException, IntrospectionException {
    if (element == null)
      return null;
    String clazz = element.getAttributeValue("class");
    Class<?> cls = Class.forName(clazz);
    if (cls == null)
      return null;

    Object obj = cls.newInstance();
    initProperties(element, cls, obj);
    callInitMethod(element, obj);

    return obj;
  }

  /**
   * 初始化Bean对象的属性
   *
   * @param element
   * @param cls
   * @param obj
   * @throws IntrospectionException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private void initProperties(Element element, Class<?> cls, Object obj)
      throws IntrospectionException, IllegalAccessException,
      InvocationTargetException {
    // 给属性赋值
    @SuppressWarnings("unchecked")
    List<Element> properties = element.getChildren("property");
    if (properties != null && properties.size() > 0) {
      // 属性相关
      String name, bean, value, dataType;
      Method method;
      for (Element propertyElement : properties) {
        name = propertyElement.getAttributeValue("name");
        bean = propertyElement.getAttributeValue("bean");
        value = propertyElement.getAttributeValue("value");
        dataType = propertyElement.getAttributeValue("type");
        if (name != null && !"".equals(name.trim())) {
          method = getWriteMethod(cls, name);
          invokeMethod(obj, bean, value, dataType, method);
        }
      }
    }
  }

  /**
   * 调用初始化方法
   *
   * @param element
   * @param obj
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private void callInitMethod(Element element, Object obj)
      throws NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {
    // 调用初始化方法
    String initMethod = element.getAttributeValue("init-method");
    if (initMethod != null && !"".equals(initMethod.trim())) {
      Method method = obj.getClass().getMethod(initMethod);
      method.invoke(obj);
    }
  }

  /**
   * 获取指定属性名对应的Set方法
   *
   * @param cls
   * @param name
   * @return
   * @throws IntrospectionException
   */
  private Method getWriteMethod(Class<?> cls, String name)
      throws IntrospectionException {
    // 获取对应class的信息
    BeanInfo info = Introspector.getBeanInfo(cls);
    // 获取其属性描述
    PropertyDescriptor pd[] = info.getPropertyDescriptors();
    for (int i = 0; i < pd.length; i++) {
      if (name.equalsIgnoreCase(pd[i].getName())) {
        return pd[i].getWriteMethod();
      }
    }
    return null;
  }

  /**
   * 通过反射调用指定对象的方法（主要是赋值）
   *
   * @param obj
   * @param bean
   * @param value
   * @param dataType
   * @param method
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private void invokeMethod(Object obj, String bean, String value,
                            String dataType, Method method) throws IllegalAccessException,
      InvocationTargetException {
    if (method == null)
      return;

    if (bean != null && !"".equals(bean.trim())) {
      Object beanObject = getBean(bean);// Bean instance
      method.invoke(obj, beanObject);
    } else if (value != null && !"".equals(value.trim())) {
      if (dataType == null || "".equals(dataType.trim())) {
        dataType = "string";
      }
      dataType = dataType.toLowerCase();
      if ("string".equals(dataType)) {
        method.invoke(obj, value);
      } else if ("int".equals(dataType)) {
        method.invoke(obj, parseInt(value));
      } else if ("float".equals(dataType)) {
        method.invoke(obj, parseFloat(value));
      } else if ("double".equals(dataType)) {
        method.invoke(obj, parseDouble(value));
      } else if ("long".equals(dataType)) {
        method.invoke(obj, parseLong(value));
      }
    }
  }

  private int parseInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private float parseFloat(String value) {
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private double parseDouble(String value) {
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private long parseLong(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private boolean isSingleton(String singleton) {
    return !"false".equalsIgnoreCase(singleton);
  }

  /**
   * 根据ID获取默认类型实例对象
   */
  public Object getBean(String id) {
    return getBean("", id);
  }

  /**
   * 根据接口类型及ID获取实例对象
   */
  public Object getBean(String typeId, String id) {
    Object obj = (singletonsCollection.get(typeId)).get(id);
    if (obj != null) {
      return obj;
    } else {
      try {
        return parseObject((mutilbeansCollection.get(typeId)).get(id));
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
  }

}