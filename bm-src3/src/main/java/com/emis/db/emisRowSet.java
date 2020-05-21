package com.emis.db;

import com.emis.util.emisUtil;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * emisRowSet 類似 jdbc 的 RowSet, 可以將 Query
 * 結果先存在記憶體中,適合用在小量的資料,但需要重覆
 * 使用的情形
 */

public class emisRowSet
{
    ArrayList oDataArray = new ArrayList();
    Object [] oArray ;
    HashMap sColumnName = new HashMap();

    int nCurrentRow=-1;
    int nColumnCount;

    emisEncodingTransfer oEncodingTransfer_;

    public emisRowSet( emisDb oDb ) throws SQLException
    {
        oEncodingTransfer_ = oDb.getEncodingTransfer();

        nColumnCount = oDb.getColumnCount();

        for(int i=1;i<= nColumnCount; i++)
        {
            sColumnName.put(oDb.getColumnName(i).toUpperCase(), new Integer(i));
        }

        while( oDb.next() )
        {
            oArray = new Object[nColumnCount];
            for(int i=0;i< nColumnCount; i++)
            {
                oArray[i] = oDb.getObject(i+1);
            }
            oDataArray.add(oArray);
        }
    }

    public emisRowSet( ResultSet rs ) throws SQLException
    {
        oEncodingTransfer_ = null;

        ResultSetMetaData meta = rs.getMetaData();
        nColumnCount = meta.getColumnCount();
//  System.out.println(nColumnCount);

        for(int i=1;i<= nColumnCount; i++)
        {
            sColumnName.put(meta.getColumnName(i).toUpperCase(), new Integer(i));
        }

 // System.out.println("End of nColumnCount:" + emisUtil.todayTimeS());
        int j=0;
        while( rs.next() )
        {
            oArray = new Object[nColumnCount];
            for(int i=0;i< nColumnCount; i++)
            {
                oArray[i] = rs.getObject(i+1);
            }
            oDataArray.add(oArray);
 if(j!=0 && j%100000==0){
    System.out.println(" oDataArray:["+j+"]" + emisUtil.todayTimeS());
  }
             if(j > 2000000){
               System.out.println(" return oDataArray:["+j+"]" + emisUtil.todayTimeS());
               return;
            }
           j++;
        }
    }

    public int getRow()
    {
        return nCurrentRow;
    }

    public void first()
    {
        nCurrentRow = -1;
    }

    /**
     *  first column is 1
     */
    public String getString(int nColumn)
    {
        oArray = (Object[]) oDataArray.get(nCurrentRow);
        Object obj = oArray[nColumn-1];
        if( obj == null )  {
          return null;
        }
        if( obj instanceof String)
        {
          String sStr = (String) obj;
          if( oEncodingTransfer_ != null )
            return oEncodingTransfer_.dbToSys(sStr);
          return sStr;
        } else {
          return obj.toString();
        }
    }

    public String getString(String sColumn)
    {
        if( sColumn == null ) return null;
        sColumn = sColumn.toUpperCase();
        Integer nPosition = (Integer) sColumnName.get(sColumn);
        if( nPosition == null ) return null;
        return getString(nPosition.intValue());
    }

    /**
     * 此處用 java.util.Date 因為 java.sql.Timestamp 和
     * java.sql.Date 都 extends java.util.Date
     */
    public java.util.Date getTimestamp(int nColumn)
    {
        oArray = (Object[]) oDataArray.get(nCurrentRow);
        Object obj = oArray[nColumn-1];
        return (java.util.Date) obj;
    }


    public java.util.Date getTimestamp(String sColumn)
    {
        if( sColumn == null ) return null;
        sColumn = sColumn.toUpperCase();
        Integer nPosition = (Integer) sColumnName.get(sColumn);
        if( nPosition == null )
          return null;
        return getTimestamp(nPosition.intValue());
    }


    public boolean next()
    {
        int _nSize = oDataArray.size();
        nCurrentRow++;
        if ( (nCurrentRow < _nSize) && (_nSize > 0) )
          return true;
        return false;
    }

    public boolean absolute(int idx)
    {
        if ( (idx >= 0) && (idx < size()) )
        {
            nCurrentRow = idx;
            return true;
        }
        return false;
    }

    public int size()
    {
        return oDataArray.size();
    }

    /**
     * this will print the content , used for debug
     */
    public void show(PrintWriter out)
    {
      out.println("====emisRowSet Data====");
      int size = oDataArray.size();
      out.println("SIZE="+size);
      out.println("COL="+nColumnCount);
      Iterator it = sColumnName.keySet().iterator();
      while ( it.hasNext() )
      {
        Object k = it.next();

        out.println("COL"+sColumnName.get(k)+":"+k);
      }


      for(int i=0 ; i < size ; i++)
      {
        Object [] objlist = (Object []) oDataArray.get(i);
        out.print("[");
        for(int j=0 ; j < nColumnCount; j++)
        {
          Object o = objlist[j];
          out.print( o == null ? "" : o.toString());
          out.print(",");
        }
        out.println("]");
      }
      out.println("====end================");
      out.flush();
    }

  public void close(){
    try{
      if(oDataArray != null){
        oDataArray.clear();
        oDataArray = null;
      }
      if(oArray != null)    {
        oArray = null;
      }

      if(sColumnName != null) {
        sColumnName.clear();
        sColumnName =null;
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}