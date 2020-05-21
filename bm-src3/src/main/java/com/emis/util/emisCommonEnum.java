package com.emis.util;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 *  實作一個Enumeration類別以存取Object
 */
public class emisCommonEnum implements Enumeration
{
    ArrayList oArray_ = new ArrayList();
    int nCurrentCursor_ = 0;

    public emisCommonEnum()
    {
    }
    public void add( Object obj )
    {
        if( obj != null ) oArray_.add(obj);
    }
    public boolean hasMoreElements()
    {
        if( this.nCurrentCursor_ < oArray_.size() )
        {
            return true;
        }
        return false;
    }

    public Object nextElement()
    {
        return oArray_.get(nCurrentCursor_++);
    }
    public int size() {
        return oArray_.size();
    }
}