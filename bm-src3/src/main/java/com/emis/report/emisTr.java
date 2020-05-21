package com.emis.report;


import java.util.ArrayList;


/**
 * 這個是為了效能及方便的考量
 * 適合印 table 的時候用
 */
public class emisTr
{

    ArrayList oTdList_ = new ArrayList();
    
    private boolean hasMutiRptRow=false;  //為false，則在EXCEL，PDF中合併成一行輸出
    

    public emisTr()
    {
    }

    public emisTr(String sStr,int nSize,int nAlign)
    {
        emisTd td = new emisTd(sStr,nSize,nAlign);
        add(td);
    }

    public emisTr(String sStr)
    {
        emisTd td = new emisTd(sStr);
        add(td);
    }

    public void add(emisTd td)
    {
        oTdList_.add(td);
    }

    public void set(int idx,emisTd td)
    {
        oTdList_.set(idx,td);
    }

    public emisTd get(int idx)
    {
        return (emisTd) oTdList_.get(idx);
    }

    public int size()
    {
        return oTdList_.size();
    }

    /**
     * @return Returns the hasMutiRptRow.
     */
    public boolean isHasMutiRptRow() {
      return hasMutiRptRow;
    }
    /**
     * @param hasMutiRptRow The hasMutiRptRow to set.
     */
    public void setHasMutiRptRow(boolean hasMutiRptRow) {
      this.hasMutiRptRow = hasMutiRptRow;
    }
}


