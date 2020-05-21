/*
 * $History: emisLine.java $
 * 
 * *****************  Version 2  *****************
 * User: Jerry        Date: 01/04/09   Time: 2:31p
 * Updated in $/WWWroot/ePOS/classes/COM/emis/report
 */
package com.emis.report;

import java.util.ArrayList;

/** 產生報表的一列 */
public class emisLine
{
    private ArrayList oStrList_ = new ArrayList();
    public emisLine() {}
    public emisLine(String sStr)
    {
        oStrList_.add( new emisStr(sStr));
    }

    /**
     *   addStr 有一些 logic , 如 LEFT ALIGN 會放到
     *   CENTER ALIGN 的左邊
     */
    public void addStr( emisStr oStr )
    {
        if( oStr == null ) return;

        int _nAlign = oStr.getAlign();

        if( _nAlign == emisStr.ALIGN_RIGHT )
        {
            oStrList_.add(oStr);
            return;
        }

        int _nLen = oStrList_.size();

        // left,center align 都是
        // 從左邊找第一個小於 N 的值
        // 然後插入 ArrayList
        boolean _hasInsert = false;
        for(int i=0; i< _nLen ; i++)
        {
            emisStr _listStr = (emisStr) oStrList_.get(i);
            int _nListAlign = _listStr.getAlign();
            if( _nAlign < _nListAlign )
            {
                // found it..,insert
                oStrList_.add(i,oStr);
                _hasInsert = true;
                break;
            }
        }

        if( ! _hasInsert )
        {
            oStrList_.add(oStr);
        }
    }

    public ArrayList getStrList ()
    {
        return oStrList_;
    }

/*---------------------test program-------------------------*/
    public static void main ( String [] argv ) throws Exception
    {
        emisLine l = new emisLine();
        emisStr s = new emisStr("A",emisStr.ALIGN_LEFT);
        l.addStr(s);
                s = new emisStr("B",emisStr.ALIGN_CENTER);
        l.addStr(s);
                s = new emisStr("C",emisStr.ALIGN_RIGHT);
        l.addStr(s);
                s = new emisStr("D",emisStr.ALIGN_LEFT);
        l.addStr(s);
                s = new emisStr("E",emisStr.ALIGN_CENTER);
        l.addStr(s);
                s = new emisStr("F",emisStr.ALIGN_RIGHT);
        l.addStr(s);

        ArrayList list = l.getStrList();
        for(int i=0;i<list.size();i++)
        {
            System.out.println(list.get(i));
        }
    }

}
