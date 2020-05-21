package com.emis.user;

import com.emis.db.emisRowSet;

import java.sql.ResultSet;
import java.sql.SQLException;


public class emisMenuPermImpl extends emisRowSet implements emisMenuPermission
{
    private int nCachedIndex_ ;

    private final static String []aPermission={ "BTNADD","BTNUPD","BTNDEL","BTNRPT","BTNCLOSE" };
//    private final static String []aPermission={ "BTNADD","BTNUPD","BTNDEL","BTNRPT" };


    public emisMenuPermImpl(  ResultSet rs ) throws SQLException
    {
        super(rs);
    }


    /**
     *  sKey 為 1A , 1B ...
     *  sRights 為  "ADD" , "UPD", "DEL", "RPT" , "CLOSE"
     *  傳回是否有權限
     */
    public emisPermission getPermission(String sKey)
    {
      try {
        if( findCache(sKey) )  {
          return getCurrentRow();
        }
        if (findall(sKey))  {
          return getCurrentRow();
        }
      } catch (Exception ignore) {}

      return new emisPermImpl( (String [])null);
    }

    private emisPermission getCurrentRow()
    {
        String []list = new String [5];
        list[emisMenuConst.MENU_PERM_ADD]=getString( aPermission[emisMenuConst.MENU_PERM_ADD] );
        list[emisMenuConst.MENU_PERM_UPD]=getString( aPermission[emisMenuConst.MENU_PERM_UPD] );
        list[emisMenuConst.MENU_PERM_DEL]=getString( aPermission[emisMenuConst.MENU_PERM_DEL] );
        list[emisMenuConst.MENU_PERM_RPT]=getString( aPermission[emisMenuConst.MENU_PERM_RPT] );
        list[emisMenuConst.MENU_PERM_CLS]="N";
        return new emisPermImpl(list);
    }



    protected boolean findCache (String sKey)
    {
        if ( absolute( this.nCachedIndex_) ) {
            return hitRecord(sKey);
        }
        return false;
    }

    protected boolean findall(String sKey)
    {
      for( int idx = 0 ; idx < size() ; idx++)  {
        if(absolute(idx)) {
          if( hitRecord( sKey) ) {
            this.nCachedIndex_ = getRow();
            return true;
          }
        }
      }
      return false;
    }

    private boolean hitRecord(String sKey)
    {
      String _sKey = getString("KEYS");
      if( sKey.equals(_sKey)) {
        return true;
      }
      return false;
    }


}