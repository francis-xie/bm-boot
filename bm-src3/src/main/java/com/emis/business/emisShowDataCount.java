package com.emis.business;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.Writer;
import java.util.List;
import java.util.regex.Pattern;

import com.emis.db.emisDb;
import com.emis.qa.emisServletContext;
import com.emis.server.emisServerFactory;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;


/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2009-1-14
 * Time: 9:32:02
 * To change this template use File | Settings | File Templates.
 */
public class emisShowDataCount extends emisShowData {
  protected emisShowDataCount(emisBusiness oBusiness, Element e, Writer out) throws Exception {
    super(oBusiness, e, out);
  }

  protected emisShowDataCount(emisBusiness oBusiness, Element e, Writer out, String preQueryName) throws Exception {
    super(oBusiness, e, out);
    this.name = preQueryName;
  }

  public void doit() throws Exception {
    //從emisShowData複製的,在這裡只處理datasrc
    NodeList nList = eRoot_.getChildNodes();
    int nLen = nList.getLength();
    if (nLen > 0) {
      for (int i = 0; i < nLen; i++) {
        Node n = nList.item(i);
        String _sNodeName = n.getNodeName();

        if ((n.getNodeType() == Node.CDATA_SECTION_NODE) || (n.getNodeType() == Node.TEXT_NODE)) {
          continue;
        }

        if (n.getNodeType() != Node.ELEMENT_NODE) continue;
        Element e = (Element) n;
        oDb_ = emisDb.getInstance(oBusiness_.getContext());
        if ("datasrc".equals(_sNodeName)||"database".equals(_sNodeName)) {
          try {
            //System.out.println("============emisShowDataCount==============");
            emisDataSrc oDataSrc = new emisDataSrc(oBusiness_, e);
            Object[] obj = emisCondition.doConditionStmt(oBusiness_, oDb_, oDataSrc.getSQL(), false, eRoot_.getElementsByTagName("condition"));

            String sReportSQL_ = emisUtil.replaceParam((String) obj[0], (List) obj[1]).trim();
            int select = sReportSQL_.indexOf(SPLIT);
            if (select < 0) {
              emisGetPreQuery.setSQL(name, DEFAULT_SQL);
              return;
            }
            //從新組合查詢SQL,去掉字段,返回count(1)
            sReportSQL_ = "select count(1) [count]" + sReportSQL_.substring(select + SPLIT.length());
            Pattern p = Pattern.compile("order( |\\n)*by(.|\\n)*", Pattern.CASE_INSENSITIVE);
            sReportSQL_ = p.matcher(sReportSQL_).replaceAll("");
            emisGetPreQuery.setSQL(name, sReportSQL_);
            //System.out.println("============emisShowDataCount==============");
          } catch (Exception ex) {
            ex.printStackTrace();
          } finally {
            oDb_.close();
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    try {
      //ServletContext servlet = new emisServletContext();
      //emisServerFactory.createServer(servlet, "C:\\wwwroot\\smepos_cn", "C:\\resincn\\smepos_cn.cfg", true);
      //emisEposUserImpl u = new emisEposUserImpl(servlet, "", "", "root", "turbo", new Boolean(false), "");

      //emisBusinessImpl _oBusiness = new emisBusinessImpl("2B", servlet, u, "2B.xml", true);
      //emisFile _oFile = emisFileMgr.getInstance(servlet).getFactory().getFile("business", "2B.xml");
      //System.out.println(_oFile.getFileName());
      //emisGetPreQuery query = new emisGetPreQuery(servlet,"2B","root");
      //query.getParams();
     //_oBusiness.process("query");
      //protected emisDirectoryImpl(String sName,String sDirectory,String sRelative, emisFileFactory oFactory)

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private static emisDb oDb_ = null;
  private String name;
  public static final String SPLIT = "/*:showCount*/";  
  public static final String DEFAULT_SQL = "select 0 [count]";
}
