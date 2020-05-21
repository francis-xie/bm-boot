

package com.emis.business;



import com.emis.cipher.emisCipherMgr;

import com.emis.db.emisDb;

import com.emis.db.emisProp;

import com.emis.trace.emisError;

import com.emis.util.emisChinese;
import com.emis.util.emisUtil;


import com.emis.util.emisXMLUtl;

import org.w3c.dom.Element;

import org.w3c.dom.Node;

import org.w3c.dom.NodeList;



import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import java.io.Writer;

import java.math.BigDecimal;

import java.sql.*;

import java.util.*;

import java.sql.*;

import com.emis.util.*;
import com.emis.audit.emisAudit;


/**

 * 負責處理 XML tag 中 database,sequence,store_sequence 的部份

 * emisAction 的 sub-class

 * Track+[11598] zhong.xu 2008/09/17 FLOAT類型的setFloat改為setDouble

 */



public class emisDatabase extends emisAction

{

    String sSQL_ ;
    
    String sConditionSQL_ = null;

    String sSQLType_;

    NodeList aParam_ ;

    boolean isNeedTokenizer = false;

    NodeList nCondition_ = null;


    boolean isWithinTransaction = false;

    emisSQLExecuter oSQLExecuter_;


    int nIsolationMode = Connection.TRANSACTION_NONE;

    // 自定的 Type
    public static final int PASSWORD = 9999;
    public static final int DATE     = 9998;
    public static final int TIME     = 9997;

    int iResultType = emisAjax.AJAX_RESULT_XML;

    protected emisDatabase (emisBusiness oBusiness,Element e,Writer out) throws Exception
    {
      super(oBusiness,e,out);
      iResultType = "JSON".equalsIgnoreCase(this.request_.getParameter(emisAjax.AJAX_RESULT)) ? emisAjax.AJAX_RESULT_JSON : emisAjax.AJAX_RESULT_XML;
    }



    protected void init(Element e) throws Exception
    {

      sSQLType_ = null;
      sConditionSQL_ = null;

      String _sClassName = e.getAttribute("class");

      if( (_sClassName!= null) && ! ("".equals(_sClassName)) )
      {
          Class _oClass = Class.forName(_sClassName);
          Object _oInstance = _oClass.newInstance();
          if( _oInstance instanceof com.emis.business.emisSQLExecuter)
          {
              oSQLExecuter_ = (emisSQLExecuter) _oInstance;
              return; // if SQLExecuter is set , other setup doesn't care
          }
      }


//      nIsolationMode = Connection.TRANSACTION_NONE;

      nIsolationMode = toIsolation(e);

      NodeList _nSQL = e.getElementsByTagName("sql");

      for(int i=0; i< _nSQL.getLength() ; i++) {

          Node _n = _nSQL.item(i);

          if(_n.getNodeType() == Node.ELEMENT_NODE )  {

              Element typeElement = (Element) _n;
              sSQL_ = _n.getFirstChild().getNodeValue();
              sSQLType_ = typeElement.getAttribute("type");
              break;

          }

      }

      if( sSQL_ == null )
          oTrace_.sysError(this,emisError.ERR_BUSINESS_DATABASE_NULL_SQL,oBusiness_.getName());



      NodeList _oParam = emisXMLUtl.getNodeList(e,"param");

      nCondition_ = e.getElementsByTagName("condition");

      int nLen = _oParam.getLength();

      if( nLen > 0 ) {

        Node n = _oParam.item(0);

        aParam_ = n.getChildNodes();

      } else {

        aParam_ = null;

      }



      // read whether tokenizer

      isNeedTokenizer = false;

      String sToken = emisXMLUtl.getElementValue(e,"tokenizer");

      if( sToken != null ) {

        if( "true".equalsIgnoreCase( sToken ) ) {

            isNeedTokenizer = true;

        }

      }

    }





    /**

     *  專門給 emisTransaction 叫用的

     *  autoCommit=false

     */

    protected void doit(emisDb oDb,Element e) throws Exception

    {

      isWithinTransaction = true;
      init(e);
      _doit(oDb,e);
      

    }



    /**

     * 給 emisDatabase 用的

     * 用於一個簡單的 SQL (autoCommit=true)

     */

    public void doit() throws Exception

    {

      isWithinTransaction = false;
      init(eRoot_);

      if( oSQLExecuter_ != null ) {

        oSQLExecuter_.execute(oContext_,oBusiness_.getUser(),oBusiness_.getRequest());

      } else {

        emisDb oDb = emisDb.getInstance(this.oContext_,this.oBusiness_);

        // set database transaction mode
        // database tag 在 transaction tag 之下的話不用設,因為
        // 會用 transaction 的 isolation 設定

        try {
        	oDb.setAutoCommit(true);
        	
            if ( nIsolationMode != Connection.TRANSACTION_NONE ) {
                oDb.setTransactionIsolation(nIsolationMode);
                oBusiness_.debug("set Transaction Mode To:"+ toIsolation(nIsolationMode));
            }
            
        	_doit(oDb,eRoot_);
        
        } finally {

        	oDb.close();
      	  
        }

      }

    }



    /**

     *  isWithinTransactionTag 用來判別是否為 emisTransaction 所叫用

     *  是的話,isAutoCommit = false, 同時,由 emisTransaction

     *  所叫用時,不可使用 store_sequence 和 sequence 等功能

     */

    private void _doit(emisDb oDb,Element e) throws Exception

    {

      oBusiness_.debug("-----database tag start------");

      try {

        if((nCondition_ != null) && (nCondition_.getLength() > 0))
        {
          if( sSQL_ != null)
          {
            if (isNeedTokenizer)
            {
              throw new Exception("condition not support tokenizer yet");
            } else {
            	sConditionSQL_ = emisCondition.doCondition(oBusiness_,oDb,sSQL_,false,nCondition_);
                oDb.prepareUpdate();
            }
          }

        }else if( "procedure".equalsIgnoreCase(sSQLType_) )
        {
            callprocedure(oDb,isWithinTransaction,false);
        }

        else if(isNeedTokenizer)
        {
            batch(oDb,isWithinTransaction);
        } else {
            nobatch(oDb,isWithinTransaction);
            oDb.prepareUpdate();
            
        }

      } finally {
        oBusiness_.debug("--------database end--------");
      }

    }


  /**
   * 專門給 emisAjax 叫用
   * @param oDb
   * @param e
   * @param iSqlType 為 0,1 , 0 為 Query, 應該有 ResultSet, 1 為 update or delete , 要傳回 int
   * @param bNeedCommit
   * @throws Exception
   */
     public void doAjax(emisDb oDb, Element e, int iSqlType, boolean bNeedCommit) throws Exception {
      isWithinTransaction = bNeedCommit;
      init(e);
      if (sSQL_ == null) return;
      oBusiness_.debug("-----database ajax tag start------");
      boolean usePrepareStmt = "false".equals(emisXMLUtl.getAttribute(e, "usePrepareStatment")) ? false : true;
      try {
        Object[] obj = null;
        int updated = 0;
        //  Use Condition
        if ((nCondition_ != null) && (nCondition_.getLength() > 0)) {
          if (usePrepareStmt) {
            sConditionSQL_ = emisCondition.doCondition(oBusiness_, oDb, sSQL_, false, nCondition_);
          } else {
            obj = emisCondition.doConditionStmt(oBusiness_, oDb, sSQL_, false, nCondition_);
            sConditionSQL_ = (String) obj[0];
          }
        }
        // use Param ( No Batch) And Default Use PrepareStatement
        else if (!isNeedTokenizer) {
          nobatch(oDb, isWithinTransaction);
        }
        // Query
        if (iSqlType == emisAjax.AJAX_TYPE_QUERY) {
            ResultSet rs = null;
            if (usePrepareStmt) {
              rs = oDb.prepareQuery();
            } else {
              rs = oDb.executeQuery(emisUtil.replaceParam((String) obj[0], (List) obj[1]));
            }
            emisDataSrc eDataSrc = new emisDataSrc(oBusiness_, e);
          if(iResultType == emisAjax.AJAX_RESULT_JSON)
            emisAjax.OutputAjaxJsonResultSet(oDb, rs, out_, eDataSrc);
          else
            emisAjax.OutputAjaxResultSet(oDb, rs, out_, eDataSrc);
            if (rs != null) rs.close();
        } else { // Update
          // Batch
          if (isNeedTokenizer) {
            updated = batch(oDb, isWithinTransaction);
          }
          // No Batch And Use PrepareStatement
          else {
            updated = oDb.prepareUpdate();
          }
          
          if (!isWithinTransaction) {
            if(iResultType == emisAjax.AJAX_RESULT_JSON)
              emisAjax.outputAjaxJsonUpdateRows(updated, out_);
            else
              emisAjax.outputAjaxUpdateRows(updated, out_);
          } else {
            ajax_total_updates += updated;
          }
        }
      } finally {
        oBusiness_.debug("--------database ajax end--------");
      }
    }

  /*
   // 2010/08/24 Joe Mark: 因舊邏輯有誤導致SQL被執行兩次，所以稍做調整，如 doAjax   
   public void doAjax_old(emisDb oDb, Element e, int iSqlType, boolean bNeedCommit) throws Exception {

      isWithinTransaction = bNeedCommit;
      init(e);
      if (sSQL_ == null) return;

      oBusiness_.debug("-----database ajax tag start------");


      boolean usePrepareStmt = "false".equals(emisXMLUtl.getAttribute(e, "usePrepareStatment")) ? false : true;

      try {
        Object[] obj = null;
        int updated = 0;

        if ((nCondition_ != null) && (nCondition_.getLength() > 0)) {
          if (usePrepareStmt) {
            sConditionSQL_ = emisCondition.doCondition(oBusiness_, oDb, sSQL_, false, nCondition_);
          } else {
            obj = emisCondition.doConditionStmt(oBusiness_, oDb, sSQL_, false, nCondition_);
            sConditionSQL_ = (String) obj[0];
            oDb.executeQuery(emisUtil.replaceParam((String) obj[0], (List) obj[1]));
          }
        } else if (isNeedTokenizer) {
          updated = batch(oDb, isWithinTransaction);
        } else {
          nobatch(oDb, isWithinTransaction);
        }

        if (iSqlType == emisAjax.AJAX_TYPE_QUERY) {
          ResultSet rs = null;
          if (usePrepareStmt) {
            rs = oDb.prepareQuery();
          } else {
            rs = oDb.executeQuery(emisUtil.replaceParam((String) obj[0], (List) obj[1]));
          }
          emisDataSrc eDataSrc = new emisDataSrc(oBusiness_, e);
          emisAjax.OutputAjaxResultSet(oDb, rs, out_, eDataSrc);
          if (rs != null) rs.close();
        } else {
          if (!isNeedTokenizer) {
            updated = oDb.prepareUpdate();
          }
          if (!isWithinTransaction) {
            emisAjax.outputAjaxUpdateRows(updated, out_);
          } else {
            ajax_total_updates += updated;
          }
        }


      } finally {
        oBusiness_.debug("--------database ajax end--------");
      }

    }*/


  private void callprocedure(emisDb oDb,boolean isTransaction,boolean isAjaxOutput) throws Exception

    {

        oBusiness_.debug("-----do call procedure-----");

        CallableStatement cstmt = oDb.prepareCall(sSQL_);

        Hashtable OutVar = new Hashtable();

        int nCounter = 1;

        if( aParam_ != null )

        {

          int len = aParam_.getLength();

          for(int i=0; i < len ; i++)

          {

              Node n = aParam_.item(i);

              if( n.getNodeType() != Node.ELEMENT_NODE ) continue;



              Element e = (Element) n;

              String sNodeName = e.getNodeName();

              String _sPassBy = e.getAttribute("passby"); // 沒寫會是空字串,不是 null

              if( (_sPassBy == null) || "".equals(_sPassBy) ) // default use "in"

                _sPassBy = "IN";

              else

                _sPassBy = _sPassBy.toUpperCase();

              String _sSQLType = e.getAttribute("type");

              String _sKey = emisXMLUtl.getElementValue( e );



              int _nSQLType = getType(_sSQLType);

              if("pname".equalsIgnoreCase(sNodeName))

              {

                boolean isSet = false;



                // sequence 和 store-Sequence 用單獨讀的 autoCommit 的 connection

                if ("sequence".equalsIgnoreCase(_sSQLType) )

                {

                    if( isTransaction )

                      throw new Exception("sequence is not supported in Transaction Mode");

                    String _sSeq = getSequence(oDb,e,true,oBusiness_);

                    oBusiness_.debug("set InParameter"+nCounter+":"+"="+_sSeq);

                    setCallPname(oBusiness_.getContext(),oDb,Types.VARCHAR,_sSeq,nCounter++);

                    continue;

                }

                if ("store_sequence".equalsIgnoreCase(_sSQLType) )

                {

                    if( isTransaction )

                      throw new Exception("store sequence is not supported in Transaction Mode");

                    String _sSeq = getStoreSequence(oDb,e,oBusiness_);

                    oBusiness_.debug("set InParameter"+nCounter+":"+"="+_sSeq);

                    setCallPname(oBusiness_.getContext(),oDb,Types.VARCHAR,_sSeq,nCounter++);

                    continue;

                }





                if(_sPassBy.indexOf("OUT") != -1)

                {

                    oBusiness_.debug("set OutParameter"+nCounter+":"+_sKey);

                    OutVar.put(_sKey,new Integer(nCounter));

                    oDb.registerOutParameter(nCounter,_nSQLType);

                    isSet = true;



                }

                if(_sPassBy.indexOf("IN") != -1)

                {

                    String _sValue = request_.getParameter(_sKey);

                    oBusiness_.debug("set InParameter"+nCounter+":"+_sKey+"="+_sValue);

                    setCallPname(oBusiness_.getContext(),oDb,_nSQLType,_sValue,nCounter);

                    isSet = true;

                }

                if( isSet )

                  nCounter++;

                else

                  oBusiness_.debug("警告: pname 標籤的 passby 屬性設定錯誤");

              } else

              if("return".equalsIgnoreCase(sNodeName))

              {

                oBusiness_.debug("set return value"+nCounter+":"+_sKey);

                oDb.registerOutParameter(nCounter,_nSQLType);

                OutVar.put(_sKey,new Integer(nCounter));

                nCounter++;

              } else {

                oBusiness_.debug("警告:procedure 形態的 SQL 只支援 'pname' 和 'return' 兩種標籤");

              }

          } // end for

        }

        // all parameter is set

        try {

            if( oDb.executePrepareCall() ) {
            	int updated = cstmt.getUpdateCount();
                if( isAjaxOutput && !isTransaction ){
                  if(iResultType == emisAjax.AJAX_RESULT_JSON)
                    emisAjax.outputAjaxJsonUpdateRows(updated, out_);
                  else
                    emisAjax.outputAjaxUpdateRows(updated, out_);
                } else {
               	  ajax_total_updates +=updated;
                }            	
            }
            

        } catch (SQLException e) {
          if(iResultType == emisAjax.AJAX_RESULT_JSON)
        	  emisAjax.outputAjaxJsonError(e,out_);
          else
        	  emisAjax.outputAjaxError(e,out_);
            oBusiness_.debug("error calling procedure:"+e.getMessage());

        }



        Enumeration e = OutVar.keys();

        while (e.hasMoreElements() )

        {

            Object oKey = e.nextElement();

            Integer oSetCnt = (Integer) OutVar.get(oKey);

            Object oValue = oDb.callGetString(oSetCnt.intValue());

            oBusiness_.debug("set attribute:" + oKey + "=" + oValue);

            oBusiness_.setAttribute(oKey,oValue);

        }

    }

    int ajax_total_updates = 0;

    private void nobatch(emisDb oDb,boolean isTransaction) throws Exception
    {
    	
        oDb.prepareStmt(sSQL_);

        if( aParam_ != null )
        {

          int len = aParam_.getLength();

          int _nSetCounter=1;

          for(int i=0; i < len ; i++)
          {

              Node n = aParam_.item(i);

              if( n.getNodeType() != Node.ELEMENT_NODE ) continue;

              Element e = (Element) n;

              setValue(oDb,e,_nSetCounter,request_,null,true,oBusiness_,isTransaction);

              _nSetCounter++;

          } // end for

        }
        

    }



    private int batch(emisDb oDb,boolean isTransaction) throws Exception

    {

        oBusiness_.debug("SQL="+sSQL_);

        try {

          //if( ! isTransaction ) {
          //  oDb.setAutoCommit(false);
          //}

          oDb.prepareStmt(sSQL_);

          HashMap _oMap = prepareParameters();

          // 因為 firstSet=true 用在 sequence 時會檢查
          //              =false 不會檢查

          boolean _isFirstSet = true;
          this.nCurrToken = 1; // 要先把 Token 的 Counter 歸 1
          ArrayList aParams_ = new ArrayList();  //参数暂存变量，用于输出SQL
          
          do
          {
              aParams_.clear();   // 清空参数
              oDb.clearParameters();

              int len = aParam_.getLength();

              int _nSetCounter = 1;



              for(int i=0; i < len ; i++)

              {

                  Node n = aParam_.item(i);

                  if( n.getNodeType() != Node.ELEMENT_NODE ) continue;



                  Element e = (Element) n;

                  String _sNodeName = n.getNodeName();

                  String _sName = emisXMLUtl.getElementValue( e );



                  if(_sName != null)

                  {

                      String sValue = this.getNext(_oMap,_sName);

                      //aParams_.add(sValue);  // 记录参数
                      // 2013/08/07 Joe 调用getSQLDebugValue以处理日期及时间参数，避免造成Debug的SQL中日期时间带用 / ：的假象
                      aParams_.add(getSQLDebugValue(e, sValue, oBusiness_));  // 记录参数

                      setValue(oDb,e,_nSetCounter,null,sValue,_isFirstSet,oBusiness_,isTransaction);

                      _isFirstSet = false;

                      _nSetCounter++;

                  }

              }
            if( nCurrToken < nMaxToken ) {  // 最后一笔的log在此不输出，因为在最后执行时会输出(修正如整批删除2笔但在控制台输出三条SQL的Bug)
              emisAudit.setLog(sSQL_, aParams_); // 增加tokenizer时输出SQL，因改用Batch方式后不调用prepareUpdate，故需在些特意调用
            }
              //_nUpdated += oDb.prepareUpdate();
              oDb.prepareAddBatch();

          } while( hasMoreParameters() );

          
          int []upd_rows= oDb.prepareExecuteBatch();
          int _nUpdated = 0;
    	  if( upd_rows != null && upd_rows.length > 0 ) {      	      
    		  
    		  for(int i=0;i<upd_rows.length;i++) {
    			  _nUpdated += upd_rows[i];
    		  }
    	  }
    	  oBusiness_.setAttribute("RETURNVALUE",new Integer(_nUpdated));
          return _nUpdated;
          

        } catch (SQLException e) {

          throw e;

        }

    }



    public static String getSequence(emisDb oDb,Element e,boolean isFirstSet,emisBusiness oBusiness) throws Exception

    {

      String _sAutoDrop = e.getAttribute("autodrop");

      String _sSeqName  = e.getAttribute("name");

      if( _sSeqName == null ) throw new Exception("sequence with no sequence name");

      String _sSeqFormat= emisXMLUtl.getElementValue(e);

      if( _sSeqFormat == null ) throw new Exception("sequence with no sequence format");



      boolean _isAutoDrop = false;

      // 用簡單的 logic 先取出 drop condition 的字串

      if( (_sAutoDrop != null) && (!"".equals(_sAutoDrop)) )
      {
          _isAutoDrop = true;
      }



      try {

          String _sSeqValue = null;

          _sSeqValue = oDb.getSequenceNumber(_sSeqName,isFirstSet,_isAutoDrop ? _sAutoDrop : null,_sSeqFormat);

          oBusiness.debug("get raw sequence:" + _sSeqValue);

          oBusiness.setAttribute(_sSeqName,_sSeqValue);

          return _sSeqValue;

      } catch (Exception seq) {

          oBusiness.debug("get Sequence Value:" + seq);

          throw seq;

      }

    }



    /**

     *   %E 為門市代碼

     */





    public static String getStoreSequence(emisDb oDb,Element e,emisBusiness oBusiness) throws Exception

    {

      String _sSNoParameter = e.getAttribute("sno");

      if( (_sSNoParameter == null) || "".equals(_sSNoParameter))

        _sSNoParameter = "S_NO";



      String _sSNO = oBusiness.getRequest().getParameter(_sSNoParameter);



      if( (_sSNO == null) || "".equals(_sSNO) )

      {

        throw new Exception("get store sequence , but user object is not store account");

      }

      _sSNO = emisChinese.lpad(_sSNO,"0",3);

      String _sAutoDrop = e.getAttribute("autodrop");

      String _sSeqName  = e.getAttribute("name");

      if( _sSeqName == null ) throw new Exception("store sequence with no sequence name");

      String _sSeqFormat= emisXMLUtl.getElementValue(e);

      if( _sSeqFormat == null ) throw new Exception("store sequence with no sequence format");




      oBusiness.debug("get store sequence name:" + _sSeqName);

      oBusiness.debug("get store sequence format:" + _sSeqFormat);



      String _sSeqValue = null;

      try {

          _sSeqValue = oDb.getStoreSequence(_sSeqName,_sSNO,_sAutoDrop,_sSeqFormat);

          oBusiness.debug("get raw sequence:" + _sSeqValue);

          oBusiness.setAttribute(_sSeqName,_sSeqValue);

          return _sSeqValue;

      } catch (Exception seq) {

          oBusiness.debug("get Sequence Value:" + seq);

          throw seq;

      }

    }





    public static void setValue(emisDb oDb,Element e,int nCounter,HttpServletRequest oRequest,String sTokenizedValue,boolean isFirstSet,emisBusiness oBusiness,boolean isTransaction) throws Exception

    {

        ServletContext oContext = oBusiness.getContext();

        String _sSQLType  = e.getAttribute("type");



        if( "sequence".equals(_sSQLType))

        {

            if( isTransaction ) {

              new Exception("sequence is not supported in Transaction Mode");

            }

            String _sSequence = getSequence(oDb,e,isFirstSet,oBusiness);

            oBusiness.debug("set "+nCounter+":SEQ:" + _sSequence);

            oDb.setString(nCounter,_sSequence);

            return;

        }



        if( "store_sequence".equals(_sSQLType))

        {

            if( isTransaction ) {

              new Exception("store_sequence is not supported in Transaction Mode");

            }

            String _sSequence = getStoreSequence(oDb,e,oBusiness);

            oBusiness.debug("set "+nCounter+":STORE SEQ:" + _sSequence);

            oDb.setString(nCounter,_sSequence);

            return;

        }





        String _sNodeName = e.getNodeName();



        // TagValue is the column name

        String _sTagValue = emisXMLUtl.getElementValue(e);



        if(_sTagValue == null ) return;



        String sValue = ( oRequest == null ) ? sTokenizedValue : oRequest.getParameter(_sTagValue);



        if( sValue != null )

        {

            oBusiness.setAttribute(_sTagValue,sValue);

        }



        int _nSQLType = Types.VARCHAR;

// mark 掉此行,因為 SQL2000 的 Empty String 和 "" 是不同的

//        if( "".equals(sValue)) sValue = null;



        _nSQLType = getType(_sSQLType);



        oBusiness.debug("set "+nCounter+":" + sValue);

        oBusiness.debug("type "+nCounter+":" + _sSQLType);

        if( "pname".equalsIgnoreCase(_sNodeName) )

        {

            if( _nSQLType == PASSWORD ) {

              sValue = emisCipherMgr.getInstance(oContext).deCipherDbData(sValue);

              _nSQLType = Types.VARCHAR;

            }

            setPname(oContext,oDb,_nSQLType,sValue,nCounter);

        } else

        if( "where".equalsIgnoreCase(_sNodeName))

        {

            setWhere(oContext,oDb,_nSQLType,sValue,nCounter);

        }

    }

  /**
   * emisAudit.setLog时日期及时间没有去掉分隔符，避免造成Debug的SQL中日期时间带用 / ：的假象
   * @param e
   * @param sValue
   * @param oBusiness
   * @return
   * @throws Exception
   */
  private String getSQLDebugValue(Element e, String sValue, emisBusiness oBusiness) {
    if ("pname".equalsIgnoreCase(e.getNodeName())) {
      String _sSQLType = e.getAttribute("type");
      int _nSQLType = getType(_sSQLType);
      if (_nSQLType == DATE) {
        String sDateSepa = null;
        try {
          sDateSepa = emisProp.getInstance(oBusiness.getContext()).get("EPOS_DATESEPA", "");
        } catch (Exception e1) {
        }
        if (!"".equals(sDateSepa)) {
          sValue = removeChar(sValue, sDateSepa.charAt(0));
        }
      } else if (_nSQLType == TIME) {
        sValue = removeChar(sValue, ':');
      }
    }
    return sValue;
  }


    public static void setWhere(ServletContext oContext,emisDb oDb,int nSQLType,String sValue,int nCounter) throws Exception

    {



      boolean isEmpty = (sValue == null) || ("".equals(sValue)) ;



      if (nSQLType == Types.VARCHAR ) {

        if( isEmpty ) {

          sValue = "@";

        }

        oDb.setString(nCounter,sValue);

      }

      if (nSQLType == Types.NUMERIC )  {

        if(isEmpty) {

          oDb.setBigDecimal(nCounter,new BigDecimal(-65535));

        } else {

          oDb.setBigDecimal(nCounter,new BigDecimal(sValue));

        }

      } else

      if(nSQLType == Types.BIGINT)  {

        if(isEmpty) {

          oDb.setLong(nCounter,-65535);

        } else {

          oDb.setLong(nCounter,Long.parseLong(sValue));

        }

      } else

      if ( (nSQLType == Types.FLOAT ) )

      {

        if(isEmpty)

        {

          oDb.setFloat(nCounter,-65535f);

        } else {

          oDb.setFloat(nCounter,Float.parseFloat(sValue));

        }

      } else {

        // char data

        if( isEmpty ) {

          sValue = "@";

        } else

        if( nSQLType == DATE ) {

          String sDateSepa = emisProp.getInstance(oContext).get("EPOS_DATESEPA","");

          if(!"".equals(sDateSepa)) {

            sValue = removeChar(sValue,sDateSepa.charAt(0));

          }

        } else

        if( nSQLType == TIME ) {

          sValue = removeChar(sValue,':');

        }

        oDb.setString(nCounter,sValue);

      }



    }



    private static String removeChar(String sValue,char removeChar) {

      if ( sValue == null ) return sValue;

      int len = sValue.length();

      StringBuffer buf = new StringBuffer();

      for(int i=0;i<len;i++) {

        char c = sValue.charAt(i);

        if( c != removeChar ) {

          buf.append( sValue.charAt(i) );

        }

      }

      return buf.toString();

    }



    public static void setPname(ServletContext oContext,emisDb oDb,int nSQLType,String sValue,int nCounter) throws Exception

    {

        // 因為空自串在 SQL 2000 中和 null 不同,所以要改成

        // 空字串會存 null , pname 才能這樣做, where 就不行了



        if( sValue == null ) {

            oDb.setNull(nCounter,nSQLType);

        } else {

            if(nSQLType == Types.VARCHAR)  {

              oDb.setString(nCounter,sValue);

              return;

            }

            if(nSQLType == DATE)  {

              String sDateSepa = emisProp.getInstance(oContext).get("EPOS_DATESEPA","");

              if(!"".equals(sDateSepa)) {

                sValue = removeChar(sValue,sDateSepa.charAt(0));

              }

              oDb.setString(nCounter,sValue);

              return;

            }

            if( nSQLType == TIME) {

              sValue = removeChar(sValue,':');

              oDb.setString(nCounter,sValue);

              return;

            }

            boolean isEmpty = "".equals(sValue);

            if( isEmpty ) {

              oDb.setInt(nCounter,0);

            } else

            if(nSQLType == Types.NUMERIC) {

                oDb.setBigDecimal(nCounter,new BigDecimal(sValue));

            } else

            if( nSQLType == Types.FLOAT ) {
                //oDb.setFloat(nCounter,Float.parseFloat(sValue));
                oDb.setDouble(nCounter,Double.parseDouble(sValue));
            } else

            if( nSQLType == Types.BIGINT ) {

                oDb.setLong(nCounter,Long.parseLong(sValue));

            } else {

              oDb.setString(nCounter,sValue);

            }

        } // end if



    }

    private static void setCallPname(ServletContext oContext,emisDb oDb,int nSQLType,String sValue,int nCounter) throws Exception

    {

        if( sValue == null )

        {

            oDb.callSetNull(nCounter,nSQLType);

        } else {

            if(nSQLType == Types.VARCHAR) {

                oDb.callSetString(nCounter,sValue);

                return;

            }

            if(nSQLType == DATE)  {

              String sDateSepa = emisProp.getInstance(oContext).get("EPOS_DATESEPA","");

              if(!"".equals(sDateSepa)) {

                sValue = removeChar(sValue,sDateSepa.charAt(0));

              }

              oDb.callSetString(nCounter,sValue);

              return;

            }

            if( nSQLType == TIME) {

              sValue = removeChar(sValue,':');

              oDb.callSetString(nCounter,sValue);

              return;

            }



            if("".equals(sValue))

            {

                oDb.callSetInt(nCounter,0);

                return;

            }

            if(nSQLType == Types.NUMERIC)

            {

                oDb.callSetBigDecimal(nCounter,new BigDecimal(sValue));

            } else

            if(nSQLType == Types.FLOAT)

            {

                oDb.callSetFloat(nCounter,Float.parseFloat(sValue));

            } else

            if(nSQLType == Types.BIGINT)

            {

                oDb.callSetLong(nCounter,Long.parseLong(sValue));

            } else {

                oDb.callSetString(nCounter,sValue);

            }

        } // end if

    }



    // just use simple impl at first

    public static int getType(String sType)

    {

        if( sType == null ) return Types.VARCHAR ;

        if( "string".equalsIgnoreCase(sType) ) return Types.VARCHAR;

        if( "int".equalsIgnoreCase(sType) ) return Types.NUMERIC;

        if( "float".equalsIgnoreCase(sType) ) return Types.FLOAT;

        if( "longint".equalsIgnoreCase(sType) ) return Types.BIGINT;

        if( "password".equalsIgnoreCase(sType)) return PASSWORD;

        if( "date".equalsIgnoreCase(sType)) return DATE;

        if( "time".equalsIgnoreCase(sType)) return TIME;

        return Types.VARCHAR;

    }



    private int nMaxToken;

    private int nCurrToken;



    private HashMap prepareParameters() throws Exception

    {

        nMaxToken = 0;

        HashMap hm = new HashMap();

        if( aParam_ == null )

          throw new Exception("you have to use pname or pwhere if you use Tokenizer");



        int len = aParam_.getLength();

        // check for all null or all empty

        // 如果全部都是 null or empty 的話,不行

        for(int i=0; i < len ; i++)

        {

            Node n = aParam_.item(i);

            if( n.getNodeType() != Node.ELEMENT_NODE ) continue;

            Element e = (Element) n;

            String _sNodeName = n.getNodeName();

            String _sName = emisXMLUtl.getElementValue( e );

            if(_sName != null)  {

              String _sRequest = request_.getParameter(_sName);

              if( _sRequest != null)  {

                int _nToken = getTokenCount(_sRequest);

                if( nMaxToken < _nToken ) {

                  nMaxToken = _nToken;

                }

                if( _nToken > 0 )

                  hm.put(_sName,_sRequest);

              }

            }

        }

        if(nMaxToken == 0 )

          throw new Exception("tokenizer 參數不可全部為空字串或 null,是否傳遞錯誤");



        return hm;

    }

    /**

     * 傳回一個字串的 token 數 (以 "," 為分格)

     */

    private int  getTokenCount(String sStr)

    {

      if( "".equals(sStr) ) return 0;

      int nCount = 1;

      int len = sStr.length();

      for(int i=0; i<len;i++)

      {

        if ( sStr.charAt(i) == ',')

          nCount++;

      }

      return nCount;

    }



    private boolean hasMoreParameters()

    {

        if( nCurrToken < nMaxToken ) {

          nCurrToken++;

          return true;

        }

        return false;

    }



    public String getNext(HashMap oMap,String sName) throws Exception

    {

        String sValue = (String) oMap.get(sName);
        if( (sValue == null) || "".equals(sValue))
          return "";

        int idx = sValue.indexOf(",");

        if( idx != -1 )
        {
            String sRestore = sValue.substring(idx+1);
            sValue = sValue.substring(0,idx);
            oMap.put(sName,sRestore);
            return sValue;

        } else {

            oMap.put(sName,"");
            return sValue;

        }

    }



    public static String toIsolation(int nIsolation)

    {

      if (nIsolation == Connection.TRANSACTION_READ_COMMITTED)

        return "READ_COMMITTED";

      else

      if (nIsolation == Connection.TRANSACTION_READ_UNCOMMITTED)

        return "READ_UNCOMMITTED";

      else

      if (nIsolation == Connection.TRANSACTION_REPEATABLE_READ)

        return "REPEATABLE_READ";

      else

      if (nIsolation == Connection.TRANSACTION_SERIALIZABLE)

        return "SERIALIZABLE";

      else

        return "NONE";

    }

    public static int toIsolation(Element e)
    {
    	
      String sIsolation = e.getAttribute("isolation");
    	
      if( sIsolation == null )
    	  return Connection.TRANSACTION_NONE;

      if( "".equals(sIsolation ) )
    	  return Connection.TRANSACTION_NONE;
      
      sIsolation = sIsolation.toUpperCase();      

      if( "READ_COMMITTED".equals(sIsolation) )

        return Connection.TRANSACTION_READ_COMMITTED;

      else

      if( "READ_UNCOMMITTED".equals(sIsolation) )

        return Connection.TRANSACTION_READ_UNCOMMITTED;

      else

      if( "REPEATABLE_READ".equals(sIsolation) )

        return Connection.TRANSACTION_REPEATABLE_READ;

      else

      if( "SERIALIZABLE".equals(sIsolation) )

        return Connection.TRANSACTION_SERIALIZABLE;

      else

        return Connection.TRANSACTION_NONE;

    }

}

