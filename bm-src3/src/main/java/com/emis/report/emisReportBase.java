package com.emis.report;

import com.emis.business.emisBusiness;
import com.emis.business.emisDataSrc;
import com.emis.db.emisProp;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;



/**
 * Track+[14524] tommer.xie 2010/04/10 新增列印EXCEL屬性增加圖片列印及td的height屬性，如果設置td的height則以該height來列印
 * 2010/04/30 sunny 增加段落重排的水平對齊方式 justify
 *
 */
abstract public class emisReportBase implements emisReport,emisProviderEventListener
{
    public static final int A_LEFT  = 1;
    public static final int A_CENTER= 2;
    public static final int A_RIGHT = 3;
    public static final int A_FILL = 4;
    public static final int A_JUSTIFY = 5;

    protected emisRptProvider oProvider_;
    protected emisBusiness oBusiness_;
    protected HttpServletRequest oRequest_;
    protected ServletContext oContext_;

    public emisReportBase(emisRptProvider oProvider) throws Exception
    {
        oProvider_ = oProvider;
        oProvider_.registerListener(this);
        oBusiness_ = oProvider.getBusiness();
        oRequest_ = oBusiness_.getRequest();
        oContext_ = oBusiness_.getContext();

    }


/********************** the implements of emisReport *******************/

    abstract public void printRpt()throws Exception;
    abstract public void onBeforeEject();
    abstract public void onAfterEject();

    public void printTd(emisTd td)
    {
        oProvider_.printTd(td);
    }


    /**
     * printTr 會自動加印一個跳行
     */
    public void printTr(emisTr tr)
    {
        oProvider_.printTr(tr);
    }


    /**
     * 印連續好幾個 emisTr(行)
     * 將 emisTr 放在 ArrayList 內
     */
    public void printTrList(ArrayList list)
    {
        if( list.size() == 0 )
            return;
        for(int i=0;i< list.size();i++)
        {
            emisTr tr = (emisTr) list.get(i);
            printTr(tr);
        }
    }

    public void printTr(emisTr tr,int nAlign,int nSize)
    {
        oProvider_.printTr(tr,nAlign,nSize);
    }

    /**
     *  printTable 和 printTr
     */
    public void printTable(emisTr tr)
    {
        oProvider_.printTable(tr);
    }

    public void printTable(emisTr tr,int nAlign,int nSize)
    {
        oProvider_.printTable(tr,nAlign,nSize);
    }

    public int getWidth()
    {
        return oProvider_.getWidth();
    }

    public int getHeight()
    {
        return oProvider_.getHeight();
    }


    /**
     * 依照 datasrc tag 所定的 id 的 emisDataSrc
     * 事實上 emisDataSrc 所 implement 的就是 XML
     * 中的 datasrc tag
     */
    public emisDataSrc getDataSrc(String sDataSrcName)
    {
        return (emisDataSrc) oProvider_.getDataSrc(sDataSrcName);
    }





    /**
     * 傳回資料庫中 EMISPROP 的 EPOS_COMPANY 所存的值
     */
    public String getCompany()
    {
        try {
            emisProp oProp = emisProp.getInstance(oProvider_.getContext());
            return (String) oProp.get("EPOS_COMPANY");
        } catch (Exception e) {
            return "";
        }
    }

  /**
     * 傳回資料庫中 EMISPROP 的 EPOS_LOGO 所存的值
     */
    public String getLogo()
    {
        try {
            emisProp oProp = emisProp.getInstance(oProvider_.getContext());
            return (String) oProp.get("EPOS_LOGO");
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * 固定傳回 XML 中 property 下的
     * title tag 所設定的值
     */
    public String getTitle()
    {
        return oProvider_.getProperty("title","");
    }

    /**
     * 固定傳回 XML 中 property 下的
     * title tag 所設定的值
     */
    public String getReportId()
    {
        return oProvider_.getProperty("id","");
    }

    /**
     * 將 left,center,right 轉成 int 的值
     * 如果找不到,就換成 A_LEFT
     */
    public int strToAlign(String sStr)
    {
        if( "left".equals(sStr) )
        {
            return A_LEFT;
        } else
        if( "center".equals(sStr) )
        {
            return A_CENTER;
        } else
        if( "right".equals(sStr))
        {
            return A_RIGHT;
        } else
        if( "fill".equals(sStr))
        {
            return A_FILL;
        } else if( "justify".equals(sStr))
        {
            return A_JUSTIFY;
        } else
        return A_LEFT;
    }

    public void debug(String sStr)
    {
        this.oProvider_.debug(sStr);
    }

    public String getParameter(String sStr)
    {
        return this.oProvider_.getParameter(sStr);
    }


}