package com.emis.user;

import com.emis.util.emisCommonEnum;

import java.util.ArrayList;
import java.util.Enumeration;

public class emisErosPermImpl implements emisPermission
{
  private ArrayList oPermArray_ ;

  public emisErosPermImpl (ArrayList oPermArray) {
    oPermArray_ = oPermArray;
  }

  public boolean hasPermission(String sRights) {
    return true;
  }

  public Enumeration getAllPermission() {
    emisCommonEnum e = new emisCommonEnum();
    e.add(oPermArray_);
    return e;
  }
}
