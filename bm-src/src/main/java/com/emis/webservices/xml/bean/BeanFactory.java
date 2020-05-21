package com.emis.webservices.xml.bean;

public interface BeanFactory {
	public Object getBean(String id);

	public Object getBean(String typeId, String id);

	public void init();

}