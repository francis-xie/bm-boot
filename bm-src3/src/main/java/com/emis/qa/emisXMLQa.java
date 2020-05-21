package com.emis.qa;

import com.emis.xml.emisXmlFactory;
import org.w3c.dom.*;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

public class emisXMLQa
{
  public static void main ( String [] args ) throws Exception
  {
      out = new PrintWriter(System.out);
      // abel modify
      ByteArrayInputStream in = new ByteArrayInputStream(sXML.getBytes());
      //StringBufferInputStream in = new StringBufferInputStream(sXML);
      
      
      
      try {
        Document doc = emisXmlFactory.getXML(in);
        restructXML(doc);
      } finally {
        in.close();
        out.close();
      }
  }
  private static PrintWriter out;

  public static void restructXML(Document doc) throws Exception
  {
    Element e = doc.getDocumentElement();
    showTree(e,0);
  }

  public static void showTree(Element e,int level) {
    String sNodeName = e.getNodeName();

    outputLevel(level);
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
        showTree(el,level+1);
      } else {
        out.print("TYPE=" + n.getNodeType()+ " ");
        outputLevel(level);
        out.println( n.getNodeValue() );
      }
    }
    outputLevel(level);
    out.println("</"+sNodeName+">");

  }

  public static void outputLevel(int level)
  {
    for(int i=0;i<level;i++) {
      out.print(" ");
    }
  }


  static String sXML =
"<act name=\"query\" for=\"sys\">\n"+
"  <showdata tableid=\"idTBL\">\n"+
"    <datasrc id=\"xmlData\" cdata=\"SA_NAME,SA_ADDR1,SA_ADDR2\">\n"+
"      <sql>\n"+
"        select a.*, s.S_NAME from Saler a, Store s\n"+
"          where %replace%\n"+
"          order by a.S_NO, SA_NO\n"+
"      </sql>\n"+
"      <condition replace=\"replace\">\n"+
"        <cond always=\"true\">\n"+
"          ( a.S_NO=s.S_NO(+) )\n"+
"        </cond>\n"+
"        <cond exists=\"S_QRYNO\" type=\"string\">\n"+
"          <![CDATA[\n"+
"            ( a.S_NO = ? )\n"+
"          ]]>\n"+
"        </cond>\n"+
"        <cond exists=\"L_QRYNO_END\" type=\"string\">\n"+
"          <![CDATA[\n"+
"            (SA_NO > ?)\n"+
"          ]]>\n"+
"        </cond>\n"+
"      </condition>\n"+
"    </datasrc>\n"+
"  </showdata>\n"+
"  <next>\n"+
"     <goto>htmltable</goto>\n"+
"  </next>\n"+
"</act>";

}