package com.emis.business;

import com.emis.util.emisUtil;
import com.emis.util.emisXMLUtl;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletRequest;
import java.util.Vector;

/**
 *  emisDatabase 會用到此 Class,
 *  負責處理 XML tag 中的 cond
 */

public class emisCond
{
    private String sAppendSQL_;
    private String sRelation_="AND";
    private String sType_ ;
    private Vector oVExists_ ;
    private boolean isAlwaysTrue = false;
    private boolean isSetParam = true;
    private String sSetType_ = "pname";

    public emisCond( emisBusiness oBusiness ,Element eCond) throws Exception
    {
      sAppendSQL_ = emisXMLUtl.getElementValue(eCond);
      HttpServletRequest oRequest = oBusiness.getRequest();
      NamedNodeMap _oAttrs = eCond.getAttributes();


      for(int i=0; i<_oAttrs.getLength();i++)
      {
          Node n = _oAttrs.item(i);
          String _sNodeName = n.getNodeName();

          if("exists".equalsIgnoreCase(_sNodeName)) {
              String _sExists = n.getNodeValue();
              if( _sExists != null) {
                 oVExists_ = emisUtil.tokenizer(_sExists);
              }
              continue;
          }

          if("settype".equals(_sNodeName))
          {
              String _sSetType = n.getNodeValue();
              if("where".equalsIgnoreCase(_sSetType)) {
                sSetType_ = "where";
              }
              continue;
          }

          if("type".equalsIgnoreCase(_sNodeName))
          {
              sType_ = n.getNodeValue();
              continue;
          }

          if("always".equalsIgnoreCase(_sNodeName))  {
              if( "true".equalsIgnoreCase(n.getNodeValue()))  {
                  isAlwaysTrue = true;
              }
              continue;
          }


          if("setparam".equalsIgnoreCase(_sNodeName))  {
              if( "false".equalsIgnoreCase(n.getNodeValue())) {
                  isSetParam = false;
              }
              continue;
          }

          if("replace".equalsIgnoreCase(_sNodeName))
          {
              String sKey = n.getNodeValue();
              String sValue=oRequest.getParameter(sKey);
              if( sValue != null )
              {
                  sAppendSQL_ = transform(sAppendSQL_,sKey,sValue);
                  oBusiness.debug("emisCond 轉換:"+sAppendSQL_);
              } else {
                  oBusiness.debug("emisCond設定 replace, 但無輸入值:"+sKey);
              }
              continue;
          }

          if("relation".equalsIgnoreCase(_sNodeName))
          {
              sRelation_ = n.getNodeValue();
              continue;
          }
      } // end of attribute loop

      // check for existence...
      if( ! isAlwaysTrue )
      {
          if( oVExists_ == null )
              throw new Exception("condition exists tag not exists");
          if( sType_ == null )
              throw new Exception("conditon type not defined");
      }

      if( oVExists_ != null )
      {
          for( int i=0 ; i<oVExists_.size(); i++)
          {
            String _sExist = (String) oVExists_.get(i);
            String _sParameter = oRequest.getParameter(_sExist);
            // exists 和 always=true 一起使用,表示使用 exists 並且不檢查
            if( ! isAlwaysTrue ) {
              // not exists
              if( (_sParameter) == null || "".equals(_sParameter) )  {
                // since there is one not exists, break;
                throw new Exception("[emisCond] not exists:" + _sExist);
              }
            }
          }
      }
    }

    public String getRelation()
    {
      return sRelation_;
    }

    public int getParameterSize()
    {
      if(oVExists_ == null ) return 0;
      if(! isSetParam ) return 0;
      return oVExists_.size();

    }

    public String getParameter(int idx)
    {
        if(oVExists_ == null ) return null;
        return (String) oVExists_.elementAt(idx);
    }

    public String getSetType()
    {
        return sSetType_;
    }

    public int getType()
    {
        return emisDatabase.getType(sType_);
    }

    public String toString()
    {
      return sAppendSQL_;
    }
    //    //    //
    private String transform(String str,String sKey,String sValue)
    {
        if( str == null ) return str;
        String sReplace = "%" + sKey + "%";
        int s_idx = str.indexOf(sReplace) ;

        if (s_idx != -1)
        {
           str = str.substring(0,s_idx) + sValue + str.substring(s_idx+sReplace.length());
        }
        return str;
    }
}