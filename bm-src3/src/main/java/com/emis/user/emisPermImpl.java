package com.emis.user;

import com.emis.util.emisCommonEnum;

import java.util.Enumeration;

public class emisPermImpl implements emisPermission
{
    private String [] hasPermission;

    public emisPermImpl(String []list) {
        hasPermission = list;
    }

    public boolean hasPermission(String sRights)
    {

       if (hasPermission == null ) return false;
       try {
         int _nRights = strToRight(sRights);
         return "Y".equals(hasPermission[_nRights]);
       } catch (Exception ignore) { }

       return false;
    }

    private int strToRight(String sRights) throws Exception
    {
        if( sRights == null )
        {
            throw new Exception("拿取 Menu 權限錯誤,null sRights");
        }
        if( sRights.equalsIgnoreCase("ADD") )
        {
            return emisMenuConst.MENU_PERM_ADD;
        } else
        if( sRights.equalsIgnoreCase("UPD") )
        {
            return emisMenuConst.MENU_PERM_UPD;
        } else
        if( sRights.equalsIgnoreCase("DEL") )
        {
            return emisMenuConst.MENU_PERM_DEL;
        } else
        if( sRights.equalsIgnoreCase("RPT") )
        {
            return emisMenuConst.MENU_PERM_RPT;
        } else
        if( sRights.equalsIgnoreCase("CLOSE") )
        {
            return emisMenuConst.MENU_PERM_CLS;
        } else {
            throw new Exception("Menu 權限種類錯誤,不支援的 Rights:"+sRights);
        }
    }

    public Enumeration getAllPermission() {
      emisCommonEnum e = new emisCommonEnum();
      e.add("ADD");
      e.add("UPD");
      e.add("DEL");
      e.add("RPT");
      e.add("CLOSE");
      return e;
    }

}