package com.emis.test;

import java.util.Enumeration;
import java.util.Properties;
public class emisProperty {

    public static void main (String [] argv ) throws Exception
    {

        Properties p = System.getProperties();
        Enumeration e = p.keys();
        while( e.hasMoreElements() )
        {
            String k = (String) e.nextElement();
            System.out.println(k+"="+p.getProperty(k));
        }
    }
}