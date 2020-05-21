/*
 * $History: emisStr.java $
 * 
 * *****************  Version 2  *****************
 * User: Jerry        Date: 01/04/09   Time: 2:31p
 * Updated in $/WWWroot/ePOS/classes/COM/emis/report
 */
package com.emis.report;

public class emisStr
{
  /*------
     the align value is fixed , don't change it,
     it will affect the algorithm  of emisStr.addStr
   -------*/
    public final static int ALIGN_LEFT =  0;
    public final static int ALIGN_CENTER = 1;
    public final static int ALIGN_RIGHT = 2;

    private static final String [] ALIGN_LIST  = { "align=\"left\"",
                                                   "align=\"center\"",
                                                   "align=\"right\"" };

    private int nAlign_ ;
    private String sStr_;
    private int nColSpan_=1; // colspan...

    public emisStr( String sStr )
    {
        sStr_ = (sStr==null) ? "" : sStr;
        nAlign_ = ALIGN_LEFT;
    }

    public emisStr( String sStr, int nAlign)
    {
        sStr_ = (sStr==null) ? "" : sStr;
        nAlign_ = nAlign;
    }

    public emisStr( String sStr,int nAlign,int nColSpan)
    {
        this(sStr,nAlign);
        nColSpan_ = nColSpan;
    }


    public int getAlign()
    {
        return nAlign_;
    }

    public int getColSpan()
    {
        return nColSpan_;
    }

    public String toString()
    {
        return sStr_;
    }

    protected static String alignToString(int nAlign)
    {
        return ALIGN_LIST[ nAlign ];
    }

}
