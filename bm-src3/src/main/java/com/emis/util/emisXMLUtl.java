package com.emis.util;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class emisXMLUtl
{

    public static String getElementValue(Element e,String sElementName)
    {
        int idx = -1;
        while ( (idx=sElementName.indexOf(".")) != -1 )
        {
            String _sPrefixName = sElementName.substring(0,idx);
            NodeList nList  = e.getElementsByTagName(_sPrefixName);
            if( nList.getLength() <= 0 ) return null;
            e = (Element) nList.item(0);
            sElementName = sElementName.substring(idx+1);
        }
        NodeList nList  = e.getElementsByTagName(sElementName);
        if(nList.getLength() > 0 )
        {
            Node n = nList.item(0);
            Node first = n.getFirstChild();
            if( first != null )
            {
                return first.getNodeValue();
            }
        }
        return null;
    }

    public static NodeList getNodeList(Element e ,String sElementName)
    {
        int idx = -1;
        while ( (idx=sElementName.indexOf(".")) != -1 )
        {
            String _sPrefixName = sElementName.substring(0,idx);
            NodeList nList  = e.getElementsByTagName(_sPrefixName);
            if( nList.getLength() <= 0 ) return nList;
            e = (Element) nList.item(0);
            sElementName = sElementName.substring(idx+1);
        }
        NodeList nList  = e.getElementsByTagName(sElementName);
        return nList;

    }

    public static String getAttribute(Element e,String sAttrName)
    {
        Node n = e.getAttributeNode(sAttrName);
        if( n != null ) return n.getNodeValue();
        return null;
    }

    public static String getElementValue(Element e)
    {
        Node n = e.getFirstChild();
        if( n != null )
        {
          return n.getNodeValue();
        }
        return "";
    }

}