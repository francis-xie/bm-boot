package com.emis.report;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class emisRptXML {
  private NodeList nlNodeList_ = null;
  private int iCurrentItem_ = -1;
  private String sNodeName_ = "";
  private Node oCurrentNode_ = null;

  public emisRptXML(Node oNode) {
    String sNodeName = oNode.getNodeName();
    Node oParent = oNode.getParentNode();
    create(oParent, sNodeName);
  }

  public emisRptXML(Node oNode, String sNodeName) {
    create(oNode, sNodeName);
  }

  private void create(Node oNode, String sNodeName) {
    iCurrentItem_ = -1;
    Element e = (Element) oNode;

    int idx = -1;
    while ( (idx=sNodeName.indexOf(".")) != -1 ) {
      String _sPrefixName = sNodeName.substring(0,idx);
      nlNodeList_  = e.getElementsByTagName(_sPrefixName);
      if ( nlNodeList_.getLength() <= 0 ) return;
      e = (Element) nlNodeList_.item(0);
      sNodeName = sNodeName.substring(idx+1);
    }
    nlNodeList_  = e.getElementsByTagName(sNodeName);
    if ( nlNodeList_.getLength() <= 0 ) return;
    iCurrentItem_ = 0;
    oCurrentNode_ = nlNodeList_.item(iCurrentItem_);

  }

  public static Node searchNode(Node oNode, String sNodeName, String sAttrName, String sValue) {
    return searchNode((Element) oNode, sNodeName, sAttrName, sValue);
  }

  public static Node searchNode(Element oElement, String sNodeName, String sAttrName, String sValue) {
    NodeList _oNl = oElement.getElementsByTagName(sNodeName);
    Node _oRet = null;
    for (int i = 0; i < _oNl.getLength(); i++) {
      if (sValue.equals(getAttribute((Element) _oNl.item(i), sAttrName))) {
        _oRet = _oNl.item(i);
      }
    }
    return _oRet;
  }

  public static Node searchNode(Node oNode, String sNodeName) {
    if (oNode.getNodeType() == Node.DOCUMENT_NODE) {
      Node n = oNode.getFirstChild();
      if (n.getNodeName() == sNodeName) {
        return n;
      } else {
        return searchNode(n, sNodeName);
      }
    } else {
      Element e = (Element) oNode;

      NodeList _oNodeList  = e.getElementsByTagName(sNodeName);
      if (_oNodeList.getLength() > 0)
        return _oNodeList.item(0);
      else {
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
          if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
            searchNode(nl.item(i), sNodeName);
          }
        }
        return null;
      }
    }
  }

  public static NodeList searchNodes(Node oNode, String sNodeName) {
    Element e = (Element) oNode;

    NodeList _oNodeList  = e.getElementsByTagName(sNodeName);
    if (_oNodeList.getLength() > 0)
      return _oNodeList;
    else {
      NodeList nl = e.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
          searchNode(nl.item(i), sNodeName);
        }
      }
      return null;
    }
  }

  public Node getNode() {
    return oCurrentNode_;
  }

  public String getNodeName() {
    String sRet = null;
    if (iCurrentItem_ >= 0 )
      sRet = oCurrentNode_.getNodeName();
    return sRet;
  }

  public boolean next() {
    if (iCurrentItem_ < (nlNodeList_.getLength()-1)) {
      iCurrentItem_++;
      oCurrentNode_ = nlNodeList_.item(iCurrentItem_);
      return true;
    } else {
      return false;
    }
  }

  public boolean previous() {
    if (iCurrentItem_ > 0) {
      iCurrentItem_--;
      oCurrentNode_ = nlNodeList_.item(iCurrentItem_);
      return true;
    } else {
      return false;
    }
  }

  public NamedNodeMap getAttributes() {
    return oCurrentNode_.getAttributes();
  }

  public static String getAttribute(Node oNode, String sAttrName) {
    return getAttribute((Element) oNode, sAttrName);
  }

  public static String getAttribute(Element e, String sAttrName) {

    Node n = e.getAttributeNode(sAttrName);
    if( n != null ) return n.getNodeValue();
    return null;
  }

  public String getAttribute(String sAttrName) {
    Element e = (Element) oCurrentNode_;

    Node n = e.getAttributeNode(sAttrName);
    if( n != null ) return n.getNodeValue();
    return null;
  }

  public String getValue() {
    Node n = oCurrentNode_.getFirstChild();
    if( n != null ) return n.getNodeValue();
    return null;
  }

  public static String getValue(Node oNode) {
    Node n = oNode.getFirstChild();
    if( n != null ) return n.getNodeValue();
    return null;
  }

  public Element getElement() {
    return (Element) oCurrentNode_;
  }

  public Element getElement(String sElementName) {
    int idx = -1;
    Element e = (Element) oCurrentNode_;

    while ( (idx=sElementName.indexOf(".")) != -1 )
    {
        String _sPrefixName = sElementName.substring(0,idx);
        NodeList nList  = e.getElementsByTagName(_sPrefixName);
        if( nList.getLength() <= 0 ) return null;
        e = (Element) nList.item(0);

        sElementName = sElementName.substring(idx+1);
    }
    NodeList nList  = e.getElementsByTagName(sElementName);
    if( nList.getLength() <= 0 ) return null;
    e = (Element) nList.item(0);
    return e;

  }

  public int getLength() {
    return nlNodeList_.getLength();
  }

  public Node item(int p0) {
    return nlNodeList_.item(p0);
  }

}
