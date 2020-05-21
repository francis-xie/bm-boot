package com.emis.report;

import com.emis.messageResource.Messages;
import com.emis.business.emisBusiness;
import com.emis.business.emisDataSrc;
import com.emis.user.emisUser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class emisKsTextProvider implements emisRptProvider{
  protected File oPrintFile_  = null;
  protected FileWriter oPrintWriter_ =null;
  private String sPrintFile_ = ""; //$NON-NLS-1$
  private Document oDoc_ ;
  private Node oRoot_ ;

  public emisKsTextProvider() {
    try {
      oPrintWriter_ = new FileWriter(oPrintFile_);
    } catch(Exception e){}
  }
  public emisKsTextProvider(String sPrintFile) {
    try {
      oPrintFile_ = new File(sPrintFile);
      oPrintWriter_ = new FileWriter(oPrintFile_);
    } catch(Exception e){}
  }
  public void setPrintFile(String sPrintFile) throws IOException{
    if (oPrintWriter_ != null)
      oPrintWriter_.close();

    oPrintFile_ = new File(sPrintFile);
    oPrintWriter_ = new FileWriter(oPrintFile_);
  }
  public String getPrintFileName() throws IOException{
    if (oPrintFile_ == null)
      return ""; //$NON-NLS-1$
    return oPrintFile_.getPath()+oPrintFile_.getName();
  }
  public void printTr(emisTr oPrintLine) {
    int i;
    String _sContent = ""; //$NON-NLS-1$
    emisTd oTempTd;
    try {
      for (i=0;i < oPrintLine.size();i++){
        oTempTd = (emisTd)oPrintLine.get(i);
        _sContent += oTempTd.getContent() ;
      }
      _sContent += '\n';
      oPrintWriter_.write(_sContent);
    } catch(Exception e){}
  }
  public String  getParameter(String sParameterName) {
    if ("PROP_TITLE".equalsIgnoreCase(sParameterName)){ //$NON-NLS-1$
      return Messages.getString("emisKsTextProvider.4"); //$NON-NLS-1$
    } else if ("QRY_COND".equalsIgnoreCase(sParameterName)){ //$NON-NLS-1$
      return Messages.getString("emisKsTextProvider.6"); //$NON-NLS-1$
    } else if ("PROP_MODE".equalsIgnoreCase(sParameterName)){ //$NON-NLS-1$
      return "11"; //$NON-NLS-1$
    } else if ("PROP_TYPE".equalsIgnoreCase(sParameterName)){ //$NON-NLS-1$
      return ""; //$NON-NLS-1$
    } else if ("L_QRYNUM".equalsIgnoreCase(sParameterName)){ //$NON-NLS-1$
      return "0"; //$NON-NLS-1$
    }
    return ""; //$NON-NLS-1$
  }
  public String getCompany() {
    return Messages.getString("emisKsTextProvider.14"); //$NON-NLS-1$
  }

  public void printTr(String sPrintLine) throws IOException{
    String _sContent;
    _sContent = sPrintLine+'\n'+'\015';
    oPrintWriter_.write(_sContent);
  }
  public void eject() {
    try {
      oPrintWriter_.write("\014\n"); //插入跳頁符號 //$NON-NLS-1$
    } catch(Exception e){}
  }
  public void close() {
    try {
      oPrintWriter_.close();
    } catch(Exception e){}
  }
  public void loadXML(String sFileName) throws Exception{
    File oFile = new File(sFileName);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = factory.newDocumentBuilder();
    oDoc_ = parser.parse(oFile);
  }

  public Node getRoot() {
    Node _oNode = oDoc_.getFirstChild();
    oRoot_ = emisRptXML.searchNode(_oNode, "report"); //$NON-NLS-1$
    return oRoot_;
  }
      public void printTd( emisTd td ){}
    /**
     * printTr 只會加 LineNumber ,
     * 不會觸發 onEject
     */
    public void printTr( emisTr tr,int nAlign, int nSize){}

    /**
     * printTable 指的是印資料行
     */
    public void printTable( emisTr tr){}
    public void printTable( emisTr tr,int nAlign, int nSize){}

    public int getPageNum(){return 0;}
    public int getWidth(){return 0;}
    public int getHeight(){return 0;}

    /**
     * 手動增加 LineNumber,
     * 因為控制碼的關係
     */
    public void incRowNum(int count){}


    /************ event support *****************/
    public void registerListener( emisProviderEventListener listener){}

    /************ property support ***************/
    public String getProperty(String sKey,String sDefault){return "";} //$NON-NLS-1$
    public String getProperty(String sKey){return "";} //$NON-NLS-1$
    public int getProperty(String sKey,int nDefault){return 0;}

    public int getCurrentRow(){return 0;}

    /**
     * 依照 datasrc tag 所定的 id 的 emisDataSrc
     * 事實上 emisDataSrc 所 implement 的就是 XML
     * 中的 datasrc tag
     */
    public emisDataSrc getDataSrc(String sDataSrcName){return null;}
    public ServletContext getContext(){return null;}
    public emisUser getUser(){return null;}

    /**
     * 會寫在 JSP 的 debug 區
     */
    public void debug( String sStr){}

    public emisBusiness getBusiness(){ return null;}
}

