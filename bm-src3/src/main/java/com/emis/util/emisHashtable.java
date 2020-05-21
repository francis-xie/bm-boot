package com.emis.util;

import java.util.Hashtable;

/**
 *  此 class 只是為了 override get method
 *  在 null 時能回傳空字串
 */
public class emisHashtable extends Hashtable
{
    public Object get(Object key)
    {
        Object obj = super.get(key);
        if( obj == null ) return "";
        return obj;
    }
}