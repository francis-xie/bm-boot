package com.emis.admin;

//import java.rmi.*;
//import javax.rmi.*;

abstract public class emisProperty //extends PortableRemoteObject implements Remote
{

  private int nPType_;
  private String sPName_;
  private Object oCurrentValue_;

  public emisProperty(int nPType,String sPName,Object oCurrentValue) throws Exception
  {
      super();
      nPType_ = nPType;
      sPName_ = sPName;
      oCurrentValue_ = oCurrentValue;
  }

  public int getPropertyType() throws Exception
  {
      return nPType_;
  }

  public Object getProperty() throws Exception
  {
      return oCurrentValue_ ;
  }

  public void setProperty(Object oNewValue) throws Exception
  {
      if( oNewValue != null )  oCurrentValue_ = oNewValue;
  }

  abstract void apply() throws Exception;
}



