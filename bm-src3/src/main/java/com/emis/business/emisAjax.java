/**
 *  2009/11/25, Robert add Ajax support
 *  within the ajax tag , should be database tag
 *  we define one ajax should call only one database operate (Qyery,delete or update)
 *  <ajax> tag 本身像 <transaction> tag , 一樣,可以設定 transaction isolation
 *  同時可以執行多個 <database> tag
 */

package com.emis.business;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.w3c.dom.*;

import com.emis.db.*;
import com.emis.file.*;
import com.emis.report.emisRightsCheck;
import com.emis.trace.*;
import com.emis.user.emisUserButton;
import com.emis.util.*;

import javax.sql.*;
import java.sql.*;

public class emisAjax extends emisAction {



  protected emisAjax (emisBusiness oBusiness,Element e,Writer out) throws Exception
  {
    super(oBusiness,e,out);
  }

  static public final int AJAX_TYPE_QUERY = 0;
  static public final int AJAX_TYPE_UPDATE = 1;
  static public final int AJAX_RESULT_XML = 0;
  static public final int AJAX_RESULT_JSON = 1;
  static public final String AJAX_RESULT = "AJAX_RESULT";

  public void doit() throws Exception
  {
    int nIsolationMode = java.sql.Connection.TRANSACTION_NONE;



    int iSqlType;
    String sSqlType = eRoot_.getAttribute("sqltype");
    if("QUERY".equalsIgnoreCase(sSqlType)) {
      iSqlType = AJAX_TYPE_QUERY;
    } else {
      iSqlType = AJAX_TYPE_UPDATE;
    }
    int iResultType = "JSON".equalsIgnoreCase(this.request_.getParameter(emisAjax.AJAX_RESULT)) ? AJAX_RESULT_JSON : AJAX_RESULT_XML;

    NodeList dbNodes = eRoot_.getChildNodes();
    int nLen = dbNodes.getLength();
    int nDbTags = 0;
    // calculate how manay database tags ?
    for(int i=0; i < nLen; i++)  {
      Node n = dbNodes.item(i);
      if( n.getNodeType() != Node.ELEMENT_NODE ) continue;
      String _sName = ((Element)n).getNodeName();
      //oBusiness_.debug("Name:"+_sName);
      if("database".equalsIgnoreCase(_sName)) {
        nDbTags++;
      }
    }

    if( nDbTags <= 0 ) return;


    nIsolationMode = emisDatabase.toIsolation(eRoot_);

    oBusiness_.debug("--AJAX START--");
    // #34643 SRC3核心处理SQL执行事务问题， 调整emisAjax.java中开启事务的规则：多个database或update模式就开启手动提交事务控制
    boolean needCommit = ( nDbTags > 1 || iSqlType == AJAX_TYPE_UPDATE);
    emisDb oDb = emisDb.getInstance(oContext_,oBusiness_);
    emisDatabase _oDataBase = null;
    try {

      if( needCommit )
        oDb.setAutoCommit(false);
      else
        oDb.setAutoCommit(true);

      if ( nIsolationMode != java.sql.Connection.TRANSACTION_NONE ) {
        oDb.setTransactionIsolation(nIsolationMode);
        oBusiness_.debug("set Transaction Mode To:"+ emisDatabase.toIsolation(nIsolationMode));
      }


      _oDataBase = new emisDatabase(oBusiness_,null,out_);

      for(int i=0; i < nLen; i++)  {
        Node n = dbNodes.item(i);
        if( n.getNodeType() != Node.ELEMENT_NODE ) continue;
        Element e = (Element) n;
        String _sName = e.getNodeName();
        //oBusiness_.debug("Name:"+_sName);
        if("database".equals(_sName)) {
          _oDataBase.doAjax(oDb,e,iSqlType,needCommit);
        }
      }
      if( needCommit ) {
        try {
          oDb.commit();
        } catch (Exception ignore) {}
        if(iResultType == AJAX_RESULT_JSON)
          this.outputAjaxJsonUpdateRows(_oDataBase.ajax_total_updates, out_);
        else
          this.outputAjaxUpdateRows(_oDataBase.ajax_total_updates,out_);
      }

      oBusiness_.debug("--AJAX Normal END--");

    } catch (Exception e) {
      //e.printStackTrace();
      if( needCommit ) {
        try {
          oDb.rollback();
        } catch (Exception ignore) {}
      }
      if( _oDataBase != null ) {
        if( _oDataBase.sConditionSQL_ != null) {
          throw new Exception(_oDataBase.sConditionSQL_ + " " + oBusiness_.errToMsg(e));
        }
        if( _oDataBase.sSQL_ != null) {
          throw new Exception(_oDataBase.sSQL_ + " " +  oBusiness_.errToMsg(e));
        }

      } else {
        throw e;
      }

    } finally {
      oDb.close();
    }

  }

  public static void outputAjaxJsonUpdateRows (int updated , Writer out) throws Exception {
    out.write("{\"status\": \"ok\", \"updatenum\":" + updated + " }");
  }
  public static void outputAjaxJsonError(Exception e , Writer out) throws IOException
  {
    out.write("{\"status\" : \"error\", \"exception\" : \"" + e.getMessage() + "\"}");
  }
  public static void outputAjaxJsonError(SQLException e , Writer out) throws Exception
  {
    out.write("{\"status\" : \"sqlerror\", \"exception\" : \"" + e.getMessage() + "\", \"errcode\":\"" + e.getErrorCode() + "\"}");
  }
  public static void outputAjaxUpdateRows (int updated , Writer out) throws Exception {
    out.write("<xml><data><_r><updatenum>"+updated+"</updatenum></_r></data></xml>");
  }

  public static void outputAjaxError(Exception e , Writer out) throws IOException
  {
    out.write("<xml><data><exception><![CDATA["+e.getMessage()+"]]></exception></data></xml>");
  }
  public static void outputAjaxError(SQLException e , Writer out) throws Exception
  {
    out.write("<xml><data><exception><![CDATA["+e.getMessage()+"]]></exception>");
    out.write("<errcode>"+e.getErrorCode()+"</errcode></data></xml>");
  }

  // below are ajax common output of SQL ,including ResultSet , updated row num, error info
  public static void OutputAjaxResultSet (emisDb oDb,ResultSet rs,Writer out, emisDataSrc eDataSrc) throws Exception {
    // 增加日期檢查判斷所需變數
    ArrayList aDatedata = eDataSrc.getDateDataColumn();
    ArrayList oQtyData = eDataSrc.getQtyDataColumn();
    String _sDateSeparator = eDataSrc.getDateSeparator();
    String _sQtyFormat = eDataSrc.getQtyFomart();

    emisStrBuf estr = new emisStrBuf();
    int iColumnCnt = oDb.getColumnCount();
    //ResultSetMetaData rmd = rs.getMetaData();
    StringBuffer sb = new StringBuffer();
    String columnNames[] = new String[iColumnCnt+1];
    String columnNames_end_tag[] = new String[iColumnCnt+1];
    //int    columnTypes [] = new int[iColumnCnt+1];
    //rmd.getColumnType(column)
    Character nbsp = Character.valueOf( (char) 160);
    int i;
    for(i=1 ; i<= iColumnCnt;i++) {
      columnNames[i]= "<" + oDb.getColumnName(i) + ">";
      columnNames_end_tag[i] = "</" + oDb.getColumnName(i) + ">";
      //	columnTypes[i] = rmd.getColumnType(i);
    }
    out.write("<xml>");
    out.write(emisUtil.LINESEPARATOR);
    String sData;
    if( rs.next() ) {

      out.write("<data>");
      out.write(emisUtil.LINESEPARATOR);

      do {
        sb.append("<_r>");
        for(i=1 ; i<= iColumnCnt;i++) {
          sb.append(columnNames[i]);
          sData = rs.getString(i);
          if( sData != null ) {
            estr.setZeroLength();
            // 增加日期欄位檢查判斷并加入日期分隔符
            if(aDatedata!=null && aDatedata.contains(oDb.getColumnName(i))){
              int _nLen = sData.length();
              if (_nLen >= 4) {
                estr.assign(sData);
                estr.insert(_sDateSeparator, _nLen - 2);
                // 若為完整的日期才需加第二個Separator符號
                _nLen = estr.length() - 5;
                if (_nLen > 2)
                  estr.insert(_sDateSeparator, _nLen);
              }else{
                estr.escapeXMLEntity(sData);
              }
            }else if (oQtyData!=null&& !"".equals(_sQtyFormat) && oQtyData.contains(oDb.getColumnName(i))){
              int iPrecision = emisUtil.parseInt(_sQtyFormat);
              int iIndex = sData.lastIndexOf(".");
              if ( iPrecision > 0) {
                sData =sData.substring(0,iIndex + iPrecision+1);
              } else {
                sData=sData.substring(0,iIndex+1);
              }
              estr.escapeXMLEntity(sData);
            }else{
              estr.escapeXMLEntity(sData);
            }
//                estr.escapeXMLEntity(sData);
            sb.append(estr.toString());

          }
          //else
          //	sb.append( nbsp );
          sb.append(columnNames_end_tag[i]);
        }
        sb.append("</_r>");
        sb.append(emisUtil.LINESEPARATOR);
        out.write(sb.toString());
        sb.setLength(0);
      } while( oDb.next() );
    } else {
      //	 if no data , we write empty attribute and show only one row for
      // 	MasterTable javascript object can parse.
      out.write("<data empty='true'>");
      out.write(emisUtil.LINESEPARATOR);
      sb.append("<_r>");
      for(i=1 ; i<= iColumnCnt;i++) {
        sb.append(columnNames[i]);
        sb.append(columnNames_end_tag[i]);
      }
      sb.append("</_r>");
      sb.append(emisUtil.LINESEPARATOR);
      out.write(sb.toString());
    }
    out.write("</data>");
    out.write(emisUtil.LINESEPARATOR);
    out.write("</xml>");
    out.write(emisUtil.LINESEPARATOR);
    sb = null;
    //rmd = null;
  }

  public static void OutputAjaxJsonResultSet(emisDb oDb, ResultSet rs, Writer out, emisDataSrc eDataSrc) throws Exception {
    // 增加日期檢查判斷所需變數
    ArrayList aDatedata = eDataSrc.getDateDataColumn();
    ArrayList oQtyData = eDataSrc.getQtyDataColumn();
    String _sDateSeparator = eDataSrc.getDateSeparator();
    String _sQtyFormat = eDataSrc.getQtyFomart();

    emisStrBuf estr = new emisStrBuf();
    int iColumnCnt = oDb.getColumnCount();
    StringBuffer sb = new StringBuffer();
    int i, count = 0;
    String sData;
    if (rs.next()) {
      do {
        sb.append(emisUtil.LINESEPARATOR);
        sb.append(count++ > 0 ? ",{" : "{");
        for (i = 1; i <= iColumnCnt; i++) {
          sb.append(i > 1 ? "," : "");
          sb.append("\"" + oDb.getColumnName(i) + "\":");
          sData = rs.getString(i);
          if (sData != null) {
            estr.setZeroLength();
            // 增加日期欄位檢查判斷并加入日期分隔符
            if (aDatedata != null && aDatedata.contains(oDb.getColumnName(i))) {
              int _nLen = sData.length();
              if (_nLen >= 4) {
                estr.assign(sData);
                estr.insert(_sDateSeparator, _nLen - 2);
                // 若為完整的日期才需加第二個Separator符號
                _nLen = estr.length() - 5;
                if (_nLen > 2)
                  estr.insert(_sDateSeparator, _nLen);
              } else {
                estr.escapeXMLEntity(sData);
              }
            } else if (oQtyData != null && !"".equals(_sQtyFormat) && oQtyData.contains(oDb.getColumnName(i))) {
              int iPrecision = emisUtil.parseInt(_sQtyFormat);
              int iIndex = sData.lastIndexOf(".");
              if (iPrecision > 0) {
                sData = sData.substring(0, iIndex + iPrecision + 1);
              } else {
                sData = sData.substring(0, iIndex + 1);
              }
              estr.escapeXMLEntity(sData);
            } else {
              estr.escapeXMLEntity(sData);
            }
            // 20150413 Joe Fix 修正JSON格式返回的内容中遇到换行符错误问题
            sb.append("\"" + estr.toString().replaceAll("\r\n","\\\\n").replaceAll("\r","\\\\n").replaceAll("\n","\\\\n") + "\"");
          } else {
            sb.append("\"\"");
          }
        }
        sb.append("}");
        sb.append(emisUtil.LINESEPARATOR);
      } while (oDb.next());
      // 20120808 joe modify: 配置修正前台页面Datagrid无记录时产生空行错误
   /* } else {
      sb.append("{");
      for (i = 1; i <= iColumnCnt; i++) {
        sb.append(i > 1 ? "," : "");
        sb.append("\"" + oDb.getColumnName(i) + "\":\"\"");
      }
      sb.append("}");
    }*/
    }
    out.write("{\"total\":" + count + ",\"rows\":[" + sb.toString() + "]}");
    out.write(emisUtil.LINESEPARATOR);
    sb = null;
  }

}