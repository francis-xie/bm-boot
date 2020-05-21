package com.emis.business;


import com.emis.db.emisDb;import org.w3c.dom.Element;import org.w3c.dom.Node;import org.w3c.dom.NodeList;import java.io.Writer;import java.sql.Connection;

public class emisTransaction extends emisAction
{
  protected emisTransaction (emisBusiness oBusiness,Element e,Writer out) throws Exception  {    super(oBusiness,e,out);  }
  public void doit() throws Exception
  {    int nIsolationMode = java.sql.Connection.TRANSACTION_NONE;
    NodeList dbNodes = eRoot_.getChildNodes();
    int nLen = dbNodes.getLength();
    if( nLen > 0 ) {

      nIsolationMode = emisDatabase.toIsolation(eRoot_);
      oBusiness_.debug("--TRANSACTION START--");
      emisDb oDb = emisDb.getInstance(oContext_,oBusiness_);
      try {
        if ( nIsolationMode != java.sql.Connection.TRANSACTION_NONE ) {          oDb.setTransactionIsolation(nIsolationMode);          oBusiness_.debug("set Transaction Mode To:"+ emisDatabase.toIsolation(nIsolationMode));        }

        oDb.setAutoCommit(false);
        emisDatabase _oDataBase = new emisDatabase(oBusiness_,null,out_);
        for(int i=0; i < nLen; i++)  {
            Node n = dbNodes.item(i);
            if( n.getNodeType() != Node.ELEMENT_NODE ) continue;
            Element e = (Element) n;
            String _sName = n.getNodeName();
            if("database".equals(_sName)) {
              _oDataBase.doit(oDb,e);
            }
        }
        oDb.commit();
        oBusiness_.debug("--TRANSACTION END--");
      } catch (Exception e) {
        try {
          oDb.rollback();
        } catch (Exception rollback) {
          oBusiness_.debug(rollback);
        }
        throw e;
      } finally {        oDb.close();
      }
    }

  }

}