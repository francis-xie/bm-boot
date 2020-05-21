package com.emis.report;

public class emisNum
{
    public static final int EMIS_NUM_TYPE_INT = 1;
    public static final int EMIS_NUM_TYPE_LONGINT = 2;
    public static final int EMIS_NUM_TYPE_FLOAT = 3;

    private int nDataType_ ;
    private Object oNum_;

    public emisNum(int nValue)
    {
      nDataType_ = EMIS_NUM_TYPE_INT;
      oNum_ = new Integer(nValue);
    }
    public emisNum(float nValue)
    {
      nDataType_ = EMIS_NUM_TYPE_FLOAT;
      oNum_ = new Float(nValue);
    }
    public emisNum(long nValue)
    {
      nDataType_ = EMIS_NUM_TYPE_LONGINT;
      oNum_ = new Long(nValue);
    }

    public emisNum add( emisNum addNum) throws Exception
    {
        if( addNum.nDataType_ != nDataType_ )
        {
            throw new Exception("emisNum 相加,型態不合");
        }

        if( nDataType_ == EMIS_NUM_TYPE_INT)
        {
            Integer oLocal = (Integer) oNum_;
            Integer oAdd = (Integer) addNum.oNum_;
            return new emisNum( oLocal.intValue() + oAdd.intValue() );
        } else
        if( nDataType_ == EMIS_NUM_TYPE_FLOAT)
        {
            Float oLocal = (Float) oNum_;
            Float oAdd = (Float) addNum.oNum_;
            return new emisNum( oLocal.floatValue() + oAdd.floatValue() );
        } else
        if( nDataType_ == EMIS_NUM_TYPE_LONGINT)
        {
            Long oLocal = (Long) oNum_;
            Long oAdd = (Long) addNum.oNum_;
            return new emisNum( oLocal.longValue() + oAdd.longValue() );
        } else {
         throw new Exception("[emisNum] 不支援的 datatype");
        }
    }

    public void add( String sNum ) throws Exception
    {
      if( nDataType_ == EMIS_NUM_TYPE_INT)
      {
             Integer Int = (Integer) oNum_;
             try {
                 Int= new Integer(Int.intValue()+Integer.parseInt(sNum));
             }catch(Exception ignore) { }
             oNum_ = Int;

      } else
      if( nDataType_ == EMIS_NUM_TYPE_FLOAT)
      {
             Float flt = (Float) oNum_;
             try {
                 flt= new Float(flt.floatValue()+Float.parseFloat(sNum));
             }catch(Exception ignore) { }
             oNum_ = flt;

      } else
      if( nDataType_ == EMIS_NUM_TYPE_LONGINT)
      {
             Long lng = (Long) oNum_;
             try {
                 lng= new Long(lng.longValue()+Long.parseLong(sNum));
             }catch(Exception ignore) { }
             oNum_ = lng;

      } else {
         throw new Exception("[emisNum] 不支援的 datatype");
      }
    }

    public void reset() throws Exception
    {
      if( nDataType_ == EMIS_NUM_TYPE_INT)
      {
         oNum_ = new Integer(0);
      } else
      if( nDataType_ == EMIS_NUM_TYPE_FLOAT)
      {
         oNum_ = new Float(0);
      } else
      if( nDataType_ == EMIS_NUM_TYPE_LONGINT)
      {
         oNum_ = new Long(0);
      } else {
         throw new Exception("[emisNum] td 不支援的 type");
      }
    }

    public String toString()
    {
        return oNum_.toString();
    }

}