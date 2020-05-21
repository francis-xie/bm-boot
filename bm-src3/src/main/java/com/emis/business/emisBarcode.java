package com.emis.business;

import com.emis.db.emisDb;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.util.emisChinese;
import com.emis.util.emisUtil;
import com.emis.util.emisXMLUtl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Vector;

public class emisBarcode extends emisAction
{
  protected emisBarcode (emisBusiness oBusiness,Element e,Writer out) throws Exception
  {
    super(oBusiness,e,out);
  }


  public void doit() throws Exception
  {
    NodeList nList = eRoot_.getChildNodes();
    emisDataSrc _oDataSrc = null;
    Vector _oColSize = null;
    Vector _oColumns = null;
    int nLen =  nList.getLength();
    if(  nLen > 0 )  {
      for(int i=0; i < nLen; i++) {
        Node n = nList.item(i);
        if( n.getNodeType() != Node.ELEMENT_NODE ) continue;
        Element e = (Element) n;
        String _sNodeName = n.getNodeName();
        if("datasrc".equals(_sNodeName)) {
          if( _oDataSrc != null )  { // 只能定義一次
            _oDataSrc.freeAllResource();
            _oDataSrc = null;
            throw new Exception("datasource already defined");
          }
          _oDataSrc = new emisDataSrc(oBusiness_,e);
        } else
        if( "property".equals(_sNodeName)) {
          String _sDataFld = emisXMLUtl.getElementValue(e,"datafld");
          String _sSize = emisXMLUtl.getElementValue(e,"size");
          if( (_sDataFld == null) || (_sSize == null) )
            throw new Exception("property setup error: size or datafld is null");
          _oColumns = emisUtil.tokenizer(_sDataFld);
          _oColSize = emisUtil.tokenizer(_sSize);
          if( _oColumns.size() != _oColSize.size() ) {
            throw new Exception("property setup error: size and datafld token not equal");
          }
          // transfer string to integer
          for(int j = 0 ; j< _oColSize.size(); j++)
          {
            String _tmp = (String) _oColSize.elementAt(j);
            Integer _i = new Integer(_tmp);
            _oColSize.setElementAt(_i,j);
          }
        }
      }
    } // end of for loop;

    if( (_oDataSrc == null) || (_oColSize == null) || (_oColumns == null)) {
      String _sComponent = null;
      if(_oDataSrc == null)
        _sComponent = "datasrc";
      if (_oColSize == null)
        _sComponent = "property:size";
      if (_oColumns == null)
        _sComponent = "property:datafld";
      throw new Exception("barcode setup component not complete:" +_sComponent);
    }
    genBarCode(_oDataSrc,_oColumns,_oColSize);
    genBarCodeScript();
  }

  private String sDownloadURL_ ;
  private void genBarCode(emisDataSrc oDataSrc,Vector oColumn,Vector oColSize) throws Exception
  {
    emisDb oDb = oDataSrc.processSQL();
    try {
      // open file
      emisDirectory root = emisFileMgr.getInstance(oContext_).getDirectory("root");
      String _sId = oBusiness_.getUser().getID();
      root = root.subDirectory(_sId);
      emisFile f = root.getFile("barcode.dat");

      String _sServerAddress = emisShowData.getServerAddress(oContext_,request_);

      sDownloadURL_ = (request_.isSecure() ? "https": "http")+"://"+_sServerAddress+":"+request_.getServerPort()+
                      root.getRelative()+ "barcode.dat";

      OutputStream barOut = f.getOutStream(null);
      int nSum = 0;
      for(int i=0 ; i< oColSize.size() ; i++)
      {
        Integer _i = (Integer)oColSize.elementAt(i);
        nSum = nSum + _i.intValue();
      }

      try {
        byte [] line = new byte[nSum];

        int _nCol = oColumn.size();
        int _byteStart = 0;
        while(oDb.next())  {
          _byteStart = 0;
          for(int i=0 ; i < _nCol ;i++) {
            String _col = (String) oColumn.elementAt(i);
            Integer oSz = (Integer)oColSize.elementAt(i);
            int _sz = oSz.intValue();
            String _val = oDb.getString(_col);
            if( _val == null )
              _val = "";
            _val = emisChinese.rpad(_val," ",_sz);
            byte[] bwrite = _val.getBytes();
            System.arraycopy(bwrite,0,line,_byteStart,_sz);
            _byteStart = _byteStart + _sz;
          }
          barOut.write(line);
          barOut.write(Character.LINE_SEPARATOR);
        }
        barOut.flush();
      } finally {
        barOut.close();
      }

    } finally {
      oDb.close();
    }
  }
  public void genBarCodeScript() throws Exception
  {
    out_.write("<script>\n");
    out_.write("    xmlUtil.download('"+sDownloadURL_+"','C:\\\\emis\\\\bp\\\\barcode.dat');\n");
    out_.write("    var Result = xmlUtil.execute('C:\\\\emis\\\\bp\\\\bprint.bat','');\n");
    out_.write("    emisCheckExecuteError(Result);\n");
    out_.write("</script>\n");
  }


}
