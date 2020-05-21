package com.emis.user;
import java.util.Enumeration;
public interface emisPermission {
  public boolean hasPermission(String sRights);
  public Enumeration getAllPermission();
}