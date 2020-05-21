package com.emis.xml;

import com.emis.file.emisDirectory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

public class emisXmlFactory
{

  private static boolean isNewXmlInterface = false;
  static {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      Method [] methods = factory.getClass().getMethods();
      for(int i=0;i<methods.length;i++) {
        if(methods[i].getName().equals("setCoalescing")) {
          isNewXmlInterface = true;
          break;
        }
      }
    } catch (Exception ignore) {}
  }

  public static Document getXML(emisDirectory oDirectory,String sFileName) throws Exception
  {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // CDATA 和 TEXT 是否相連
      if(isNewXmlInterface) {
        factory.setCoalescing(true);
        factory.setIgnoringComments(true);
      }
      DocumentBuilder parser = factory.newDocumentBuilder();
      return parser.parse(oDirectory.getDirectory() + sFileName);
  }


  public static Document getXML( InputStream in ) throws Exception
  {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // CDATA 和 TEXT 是否相連
      if(isNewXmlInterface) {
        factory.setCoalescing(true);
        factory.setIgnoringComments(true);
      }
      DocumentBuilder parser = factory.newDocumentBuilder();
      return parser.parse(in);
  }
  public static void saveXML( Document doc , PrintWriter out) throws Exception
  {
    Element e = doc.getDocumentElement();
    writeElement(out,e,0);
  }

  private static void writeElement (PrintWriter out,Element e,int nLevel) {
    String sNodeName = e.getNodeName();

    outputLevel(out,nLevel);
    out.print("<"+sNodeName);

    // print attributes
    NamedNodeMap nmap = e.getAttributes();
    if( nmap.getLength() > 0 )
    {
      for(int i=0;i< nmap.getLength(); i++) {
        Node n = nmap.item(i);
        String _name = n.getNodeName();
        String _value = n.getNodeValue();
        out.print(" ");
        out.print(_name+"=\""+_value+"\"");
      }
    }
    out.println(">");

    NodeList nl = e.getChildNodes();
    for(int i=0;i<nl.getLength();i++) {
      Node n  = nl.item(i);
      if( n.getNodeType() == Node.ELEMENT_NODE ) {
        Element el = (Element) n ;
        writeElement(out,el,nLevel+1);
      } else {
//        out.print("TYPE=" + n.getNodeType()+ " ");
        outputLevel(out,nLevel);
        out.println( n.getNodeValue() );
      }
    }
    outputLevel(out,nLevel);
    out.println("</"+sNodeName+">");
  }

  public static void outputLevel(PrintWriter out,int level)
  {
    for(int i=0;i<level;i++) {
      out.print(" ");
    }
  }



    /** use transform sample
      DOMSource ds = new DOMSource(doc);
      StreamResult sr = new StreamResult(out);
      TransformerFactory transformerfactory = TransformerFactory.newInstance();
      Transformer transformer= transformerfactory.
      transformer.setOutputProperty( "indent", "yes" );
      transformer.transform(ds,sr);
      */
}

