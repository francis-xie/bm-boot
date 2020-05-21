package com.emis.report;

import com.emis.business.emisBusiness;
import com.emis.business.emisDataSrc;
import com.emis.user.emisUser;
import org.w3c.dom.Node;

import javax.servlet.ServletContext;

public interface emisRptProvider
{
    public void printTd( emisTd td );
    /**
     * printTr 只會加 LineNumber ,
     * 不會觸發 onEject
     */
    public void printTr( emisTr tr );
    public void printTr( emisTr tr,int nAlign, int nSize);

    /**
     * printTable 指的是印資料行
     */
    public void printTable( emisTr tr);
    public void printTable( emisTr tr,int nAlign, int nSize);

    public int getPageNum();
    public int getWidth();
    public int getHeight();

    /**
     * 手動增加 LineNumber,
     * 因為控制碼的關係
     */
    public void incRowNum(int count);


    /************ event support *****************/
    public void registerListener( emisProviderEventListener listener);

    /************ property support ***************/
    public String getProperty(String sKey,String sDefault);
    public String getProperty(String sKey);
    public int getProperty(String sKey,int nDefault);

    public int getCurrentRow();
    public Node getRoot();

    /**
     * 依照 datasrc tag 所定的 id 的 emisDataSrc
     * 事實上 emisDataSrc 所 implement 的就是 XML
     * 中的 datasrc tag
     */
    public emisDataSrc getDataSrc(String sDataSrcName);
    public void eject();

    public ServletContext getContext();
    public emisUser getUser();

    /**
     * 會寫在 JSP 的 debug 區
     */
    public void debug( String sStr);

    public emisBusiness getBusiness();

    public void close();

    public String getParameter(String sName);
}