/* $Id: emisDataSrc.java 4 2015-05-27 08:13:47Z andy.he $

 *

 * Copyright (c) EMIS Corp. All Rights Reserved.

 */

package com.emis.business;



import com.emis.db.emisDb;

import com.emis.db.emisFieldFormat;
import com.emis.db.emisProp;

import com.emis.util.emisXMLUtl;

import com.emis.util.emisUtil;

import com.emis.trace.emisTracer;

import org.w3c.dom.Element;

import org.w3c.dom.Node;

import org.w3c.dom.NodeList;



import javax.servlet.ServletContext;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.Iterator;

import java.util.List;

import java.sql.Connection;

/**

 * emisDatabase 會用到此 Class, 負責處理 XML tag 中的 datasrc.

 * @author Robert

 * @version 2002

 * @version 2004/08/23 Jerry: add comment, add debug message to emisServer*.log
 * Track+[15095] sunny.zhuang 2010/06/30 增加"我的工作清單" 

 */

public class emisDataSrc {

    private emisBusiness oBusiness_;

    private ServletContext oContext_;

    private Element eRoot_;

    private emisHttpServletRequest oRequest_;

    private String sId_;

    private String sSQL_;

    private String sReportSQL_;



//    private emisSQLExecuter oSQLExecuter_;

    private ArrayList oCData_;

    private ArrayList oDateData_;

    private String sDateSeparator_;

    private String sQtyFormat_;

    private ArrayList oQtyData_;

    private NodeList oCondition_;

    private boolean isUsePrePareStatment = false;

    private boolean isGetReportSql = false;


    private ArrayList clones;

    private int nIsolationMode = java.sql.Connection.TRANSACTION_NONE;



    /**

     * 複製.

     * @return

     * @throws Exception

     */

    public emisDataSrc cloneDataSrc() throws Exception {

        emisDataSrc src = new emisDataSrc();

        src.oBusiness_ = oBusiness_;

        src.oContext_ = oContext_;

        src.oRequest_ = oRequest_;

        src.eRoot_ = eRoot_;

        src.init();

        if (clones == null)

            clones = new ArrayList();

        clones.add(src);

        return src;

    }



    /**

     * 因為 pervasive 不支援 top (SQL2000) 和 rownum (Orace)

     * 所以只好由系統做.

     */

    private int nMaxRows;



    /**

     * Constructor.

     */

    private emisDataSrc() {

    }



    /**

     * Constructor.

     * @param oBusiness

     * @param eRoot

     * @throws Exception

     */

    public emisDataSrc(emisBusiness oBusiness, Element eRoot) throws Exception {

        oBusiness_ = oBusiness;

        oContext_ = oBusiness.getContext();

        oRequest_ = (emisHttpServletRequest) oBusiness.getRequest();

        eRoot_ = eRoot;

        init();

    }



    /**

     * get ID.

     * @return

     */

    public String getId() {

        return sId_;

    }



    boolean isScrollable_ = false;



    /**

     * Init.

     * @throws Exception

     */

    private void init() throws Exception {



        sSQL_ = emisXMLUtl.getElementValue(eRoot_, "sql");

        sId_ = emisXMLUtl.getAttribute(eRoot_, "id");

        if (sId_ == null) sId_ = "xmlData";
        // 2010/09/28 Joe modify: 修正usePrepareStatment默认值处理逻辑，当有写为True时方为True，否则皆为False
        isUsePrePareStatment = "true".equals(emisXMLUtl.getAttribute(eRoot_,"usePrepareStatment")) ? true : false ;
        /*if (emisXMLUtl.getAttribute(eRoot_, "usePrepareStatment") != null) {

            isUsePrePareStatment = true;
        }*/

        if (emisXMLUtl.getAttribute(eRoot_, "isGetReportSql") != null) {

            isGetReportSql = true;
        }

        //nIsolationMode = emisDatabase.toIsolation(eRoot_);

        // robert 2009/12/11 , 這是報表 datasrc 的 isolation 設定,用來設定 ResultSet 的屬性
         
        String _sScrollable = emisXMLUtl.getAttribute(eRoot_, "isolation");
        if ("true".equals(_sScrollable)) {
            isScrollable_ = true;
        }
        



        /** 如果設定 class, 則其他 param 的設定都 ignore, 交給 class 做

         // oSQLExecuter_ = null;



         String _sClassName = emisXMLUtl.getAttribute(eRoot_,"class");

         if( _sClassName!= null )

         {

         Class _oClass = Class.forName(_sClassName);

         Object _oInstance = _oClass.newInstance();

         if( _oInstance instanceof com.emis.business.emisSQLExecuter)

         {

         oSQLExecuter_ = (emisSQLExecuter) _oInstance;

         }

         }

         */

        if (sSQL_ == null) {

            // 沒有可以做輸出的指定

            throw new Exception("dataSource 指令找不到 SQL 內容");

        }



        // 要把特殊字元做處理的輸出欄位

        String _sCDataFlds = emisXMLUtl.getAttribute(eRoot_, "cdata");

        oCData_ = null;

        if (_sCDataFlds != null) {

            oCData_ = tokenizer(_sCDataFlds);

        }



        if ((oCData_ != null) && oBusiness_.isDebug()) {

            for (int i = 0; i < oCData_.size(); i++) {

                oBusiness_.debug("CData column" + (i + 1) + ":" + oCData_.get(i));

            }

        }



        sDateSeparator_ = emisProp.getInstance(this.oContext_).get("EPOS_DATESEPA", "");

        if (!"".equals(sDateSeparator_)) {

            String _sDateDataFlds = emisXMLUtl.getAttribute(eRoot_, "datedata");

            oDateData_ = null;

            if (_sDateDataFlds != null) {

                oDateData_ = tokenizer(_sDateDataFlds);

            }



            if ((oDateData_ != null) && oBusiness_.isDebug()) {

                for (int i = 0; i < oDateData_.size(); i++) {

                    oBusiness_.debug("DateData column" + (i + 1) + ":" + oDateData_.get(i));

                }

            }

        }


      try {
        sQtyFormat_ = emisFieldFormat.getInstance(this.oContext_).get("$DQTY", "VALIDATION");
        if (!"".equals(sQtyFormat_)) {
          sQtyFormat_ = sQtyFormat_.substring(sQtyFormat_.length() - 1);
        }
      } catch (Exception e) {
        sQtyFormat_ = "";
      }


        if (!"".equals(sQtyFormat_)) {

            String _sQtyDataFlds = emisXMLUtl.getAttribute(eRoot_, "qtydata");

            oQtyData_ = null;

            if (_sQtyDataFlds != null) {

                oQtyData_ = tokenizer(_sQtyDataFlds);

            }



            if ((oQtyData_ != null) && oBusiness_.isDebug()) {

                for (int i = 0; i < oQtyData_.size(); i++) {

                    oBusiness_.debug("QtyData column" + (i + 1) + ":" + oQtyData_.get(i));

                }

            }

        }



        oCondition_ = eRoot_.getElementsByTagName("condition");

    }



    private emisDb lastAccess;

    private boolean hasMoreData_ = false;

    /**

     * 因為 emisTBody 和 emisTable 之間的跳行

     * ,所以有可能連續叫用兩次 processData

     * 以用兩個 HashMap 才不會同時衝到

     */

    private HashMap oData_ = new HashMap();

    private String[] sColumn_;



    /**

     *

     * @return

     */

    public boolean hasMoreData() {

        return hasMoreData_;

    }



    /**

     *

     * @return

     */

    public HashMap getData() {

        return oData_;

    }



    /**

     *

     * @return

     * @throws SQLException

     */

    public HashMap processData() throws SQLException {

        if (lastAccess == null) return null;

        hasMoreData_ = lastAccess.next();

        if (hasMoreData_) {

            // put data to hashMap  table

            int nColumnCnt = sColumn_.length;

            for (int i = 0; i < nColumnCnt; i++) {

                String sValue = lastAccess.getString(i + 1);

                oData_.put(sColumn_[i], sValue);

            }

        } else {

            return null;

        }

        return oData_;

    }



    public String getReportSql(){

        return this.sReportSQL_;

    }



    /**

     * SQL處理.

     * @return

     * @throws Exception

     */

    public emisDb processSQL() throws Exception {

        if (lastAccess != null) {

            freeResource();

        }

        emisDb oDb = emisDb.getInstance(oContext_, oBusiness_);

        try {
        	
            if (nIsolationMode != java.sql.Connection.TRANSACTION_NONE) {
                oDb.setTransactionIsolation(nIsolationMode);
                oBusiness_.debug("set Transaction Mode To:" + emisDatabase.toIsolation(nIsolationMode));
            }

            processContinueSQLInner(oDb);

        } catch (Exception e) {

        	try {
        		oDb.close();
        	} catch (Exception ignore) {}

            String _sMsg = "  emisDataSrc.processSQL: " + e.getMessage();

            System.err.println(_sMsg);

            emisTracer.get(oContext_).info(_sMsg);

            throw e;

        } 

        lastAccess = oDb;



        return oDb;

    }







    /**

     *

     * @throws Exception


    private void processContinueSQL() throws Exception {

        lastAccess.clearParameters();

        processContinueSQLInner(lastAccess);

    }
     */

    // 內含值的轉換





    /**

     *

     * @param oDb

     * @throws Exception

     */

    private void processContinueSQLInner(emisDb oDb) throws Exception {




        hasMoreData_ = false;



        long start,end;



        /**

         if( oSQLExecuter_ != null )

         {

         start = System.currentTimeMillis();

         oSQLExecuter_.query(oContext_,oDb,oBusiness_.getUser(),oRequest_,isScrollable_);

         end = System.currentTimeMillis();

         oBusiness_.debug("ExecuteQuery(Executer) in:" +(end-start) +" milliseconds");

         } else

         getElementsByTagName

         */







        if ((oCondition_ != null) && (oCondition_.getLength() > 0)) {

            if (sSQL_ != null) {

                // [0] 代表 sql  [1] 代表 內含值

                Object[] obj = emisCondition.doConditionStmt(oBusiness_, oDb, sSQL_, isScrollable_, oCondition_);

                start = System.currentTimeMillis();

                if (isUsePrePareStatment) {

                    oDb.prepareQuery();

                    //System.out.println("Use PREPARESTATMENT");

                } else if(isGetReportSql) {

                    sReportSQL_ = emisUtil.replaceParam((String) obj[0], (List) obj[1]);

                }else{

                    oDb.executeQuery(emisUtil.replaceParam((String) obj[0], (List) obj[1]));

                    // System.out.println("Use STATMENT");

                }

                end = System.currentTimeMillis();

                oBusiness_.debug("ExecuteQuery(Condition) in:" + (end - start) + " milliseconds");

            }

        } else // 普通的 SQL 情形

        {

            if (isScrollable_)

                oDb.prepareStmt(sSQL_, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            else

                oDb.prepareStmt(sSQL_);

            NodeList nList = eRoot_.getElementsByTagName("param");

            int nLen = nList.getLength();

            // 應該只會有一個 param Node

            if (nLen > 0) {

                NodeList _oParam = nList.item(0).getChildNodes();

                nLen = _oParam.getLength();



                int _nSetCounter = 1;

                for (int i = 0; i < nLen; i++) {

                    Node n = _oParam.item(i);

                    if (n.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element e = (Element) n;



                    emisDatabase.setValue(oDb, e, _nSetCounter, oRequest_, null, true, oBusiness_, false);

                    _nSetCounter++;

                }

            }

            start = System.currentTimeMillis();

            oDb.prepareQuery();

            end = System.currentTimeMillis();

            oBusiness_.debug("ExecuteQuery(Common) in:" + (end - start) + " milliseconds");



        }



        // init oDb's column record

        int nColumnCnt_ = oDb.getColumnCount();



        sColumn_ = new String[nColumnCnt_];

        for (int i = 0; i < nColumnCnt_; i++) {

            sColumn_[i] = oDb.getColumnName(i + 1).toUpperCase();

        }



    }



    /**

     *

     */

    public void freeResource() {

        if (lastAccess != null)

            lastAccess.close();

        lastAccess = null;

    }



    /**

     *

     */

    public void freeAllResource() {

        freeResource();

        if (clones != null) {

            for (int i = 0; i < clones.size(); i++) {

                emisDataSrc src = (emisDataSrc) clones.get(i);

                src.freeAllResource();

            }

        }

    }



    /**

     *

     * @return

     */

    public ArrayList getCDataColumn() {

        return oCData_;

    }



    /**

     *

     * @return

     */

    public ArrayList getDateDataColumn() {

        return oDateData_;

    }



    /**

     *

     * @return

     */

    public ArrayList getQtyDataColumn() {

        return oQtyData_;

    }



    /**

     *

     * @return

     */

    public String getDateSeparator() {

        return sDateSeparator_;

    }




    /**

     *

     * @return

     */

    public String getQtyFomart() {

        return sQtyFormat_;

    }



    /**

     *

     * @param sKey

     * @param sValue

     */

    public void setParameter(String sKey, String sValue) {

        if (oRequest_ != null) {

            oRequest_.setParameter(sKey, sValue);

        }

    }



    /**

     *

     * @param map

     */

    public void setParameter(HashMap map) {

        Iterator it = map.keySet().iterator();

        while (it.hasNext()) {

            Object key = it.next();

            Object value = map.get(key);

            oRequest_.setParameter((String) key, (String) value);

        }

    }



    /**

     * 此 tokenizer 為了比對 Column 方便,會轉成大寫.

     * @param sStr

     * @return

     */

    private ArrayList tokenizer(String sStr) {

        if (sStr == null) return null;



        ArrayList _oData = new ArrayList();

        int _nIdx = sStr.indexOf(",");

        while (_nIdx != -1) {

            _oData.add(sStr.substring(0, _nIdx).toUpperCase());

            sStr = sStr.substring(_nIdx + 1);

            _nIdx = sStr.indexOf(",");

        }

        if (!"".equals(sStr)) {

            _oData.add(sStr.toUpperCase());

        }

        if (_oData.size() > 0) return _oData;

        return null;

    }
    //返回SQL,emisShowDataCount里调用 zhong.xu
      public String getSQL() {
        return this.sSQL_;
      }
}

