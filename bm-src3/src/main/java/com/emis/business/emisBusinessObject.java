/* $Header: /repository/src3/src/com/emis/business/emisBusinessObject.java,v 1.1.1.1 2005/10/14 12:41:51 andy Exp $
 * @author: Abel
 * 2003/09/03 Jerry: Add debugging information.
 * 2004/07/03 abel : add DeadLock Pbo redo method
 */
package com.emis.business;

import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.trace.emisTracer;
import com.emis.user.emisUser;
import com.emis.util.emisXMLUtl;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContext;
import java.io.Writer;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * 操作所有商務邏輯(PBO)運作的管理類別.
 * 由XML檔案內讀取到掛載的PBO後會來此依序執行每個PBO的處理作業
 */
public class emisBusinessObject extends emisAction {

  // 是否須從新讀取emisBusiness 的設定
  private static boolean isInitDeadlockiInf_ = true; //尚未讀取deadlock 設定
  private static int iPboReDoInterval_ = 2000;  // pbo 重做的時間間隔
  private static int iPboReDoMaxCount_ = 1;  //pbo 重做次數
  private static Logger oLogger_;    //log4j 的logger
  private static String[] aDeadLockKeyWord = {"鎖定", "鎖死"}; //判斷deadlock 的KEWORD

  private List sClassName_ = new ArrayList();
  private HashMap oParameterData_ = new HashMap();
  private HashMap oExportData_ = new HashMap();
  private ServletContext oContext_;
  private emisHttpServletRequest request_;
  private emisHttpServletResponse response_;
//  private emisUser user_ ;
  private emisBusinessResourceBean oResourceBean_;
  private emisTracer oTracer_;  // Log to c:\wwwroot\xxx\logs\emisServer*.log


  public emisBusinessObject(emisBusiness oBusiness,
                            Element root,
                            Writer out,
                            ServletContext oContext,
                            emisHttpServletRequest request,
                            emisHttpServletResponse response,
                            emisUser user) throws Exception {

    super(oBusiness, root, out);
    this.oContext_ = oContext;
    this.request_ = request;
    this.response_ = response;
    oTracer_ = emisTracer.get(oContext);
    parseBusiness(root);
    creatResourceBean();
    initThreadLocalObject();

    if (emisBusinessObject.isInitDeadlockiInf_) {
      emisBusinessObject.initLogStatus(oContext);
    }
  }

  /**
   * parse xml 把所有class 寫在xml中的class 紀錄下來
   *
   * @param e XML的一個element
   */
  private void parseBusiness(Element e) throws Exception {
    NodeList eAction = e.getChildNodes();
    int eLength = eAction.getLength();
    for (int j = 0; j < eLength; j++) {
      Node n = eAction.item(j);
      if (n.getNodeType() != Node.ELEMENT_NODE) continue;

      Element e1 = (Element) n;
      sClassName_.add(emisXMLUtl.getElementValue(e1, "class"));
      if (oTracer_.isTraceEnabled()) {
        StringBuffer buf = new StringBuffer();

        buf.append("emisBusinessObject.parseBusiness: sClassName_=")
                .append(sClassName_)
                .append(",class=").append(emisXMLUtl.getElementValue(e1, "class"))
                .append(",import=").append(emisXMLUtl.getElementValue(e1, "import"))
                .append(",export=").append(emisXMLUtl.getElementValue(e1, "export"));
        //System.out.println("-->" + buf.toString());
        oTracer_.info(buf.toString());
      }
      parseImportData(n);
    }
  }

  /**
   * 處理由XML傳來的參數
   *
   * @param oAction
   * @throws Exception
   */
  protected void parseImportData(Node oAction) throws Exception {
    NodeList _oActionChild = oAction.getChildNodes();
    int _iLength = _oActionChild.getLength();
    for (int i = 0; i < _iLength; i++) {
      Node _oSubNode = _oActionChild.item(i);
      if (_oSubNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      if ("param".equalsIgnoreCase(_oSubNode.getNodeName())) {
        String _sName = "";
        String _sValue = "";
        Element _oSubElement = (Element) _oSubNode;
        NamedNodeMap _oAttributeMap = _oSubElement.getAttributes();

        Node _oNameNode = _oAttributeMap.getNamedItem("name");
        Node _oValueNode = _oAttributeMap.getNamedItem("value");
        if (_oNameNode != null) {
          _sName = _oNameNode.getNodeValue();
          if (_oValueNode != null) {
            _sValue = _oValueNode.getNodeValue();
          } else {
            _sValue = request_.getParameter(_sName);
          }
          oParameterData_.put(_sName, _sValue);
        }

      }
    }
  }

  /**
   * creat resourcebean
   */
  private void creatResourceBean() {
    emisDb oDb = null;
    oResourceBean_ = new emisBusinessResourceBean();
    try {
      oDb = emisDb.getInstance(oContext_);
      oDb.setAutoCommit(false);
    } catch (Exception ignore) {
      System.out.println("emisBusinessObject: emisDb.getInstance failed.");
    }

    oResourceBean_.setEmisDb(oDb);
    oResourceBean_.setEmisHttpServletRequest(request_);
    oResourceBean_.setEmisHttpServletResponse(response_);
    oResourceBean_.setServletContext(oContext_);
    oResourceBean_.setBusiness(this.oBusiness_);
    oResourceBean_.setUseCase(sClassName_.size());
    //20040807 Jacky 修改ErrorEvent 的Constructor Sinalture
    oResourceBean_.setEmisErrorEvent(new emisErrorEvent(oContext_));
    emisProp _oProp = null;
    try {
      _oProp = emisProp.getInstance(oContext_);
      oResourceBean_.setAD_(_oProp.isAD()); // 04/01/03' Jeff     取得系統設定的日期格式是西元/民國，並在 resourceBean 產生時會記錄在 bean 裡面
    } catch (Exception e) {
      System.out.println("emisBusinessObject: emisProp.getInstance failed.");
    }
  }

  /**
   * 把準備好的resourcebean pass 給 c:\wwwroot\專案\web-inf\classes\ ***.class
   * 並且當所有的*.class 執行完畢  會 close  db connection
   */
  public void doit() throws Exception {
    try {
      //System.out.println("do it ---->");
      //emisBusinessClassLoader oClassLoader = new emisBusinessClassLoader(ClassLoader.getSystemClassLoader(), oContext_);
      int iReDoCount = 0;
      String sClassName = null;
      ClassLoader oClassLoader = Thread.currentThread().getContextClassLoader();
      for (int i = 0; i < sClassName_.size(); i++) {
        sClassName = (String) sClassName_.get(i);
        Class oPboClass = oClassLoader.loadClass(sClassName);
        //填入emisdb 的描述
        oResourceBean_.getEmisDb().setDescription(sClassName);
        Object[] argValues = {oResourceBean_, oParameterData_, oExportData_};//args.toArray();
        Class[] argtypes = {emisBusinessResourceBean.class, HashMap.class, HashMap.class};
        Method oPboMethod = oPboClass.getMethod("runBusiness", argtypes);
        Object oPboClassInstance = oPboClass.newInstance();

        if (oTracer_.isTraceEnabled()) {
          StringBuffer buf = new StringBuffer();
          buf.append("emisBusinessObject.doit: i=").append(i)
                  .append(" class=").append(oPboClass.getName());
          oTracer_.info(buf.toString());
        }
        iReDoCount = 0;// 每個pbo 的redo 都需要重新計算 99999是代表跳出迴圈
        do {
          try {
            oPboMethod.invoke(oPboClassInstance, argValues);// 呼叫 pbo 的地方
            iReDoCount = 99999;
          } catch (InvocationTargetException e) {
            String msg = e.getTargetException().getMessage();
            iReDoCount++;
            if (this.isNotDeadLock(msg)) {
              iReDoCount = 99999; //判斷不為deadlock 的錯誤則不重新做pbo
            } else {
              //這邊會記錄所有的 deadlock 的 info
              logPboInf("[DEADLOCK PBO]" + (String) sClassName_.get(i));
              logPboInf("[DEADLOCK PBO]" + (String) sClassName_.get(i), e.getTargetException());
            }
            if (iReDoCount > iPboReDoMaxCount_) {
              throw e;
            }

            Thread.currentThread().sleep(iPboReDoInterval_); //
          }
        } while (iReDoCount < iPboReDoMaxCount_);
      }
      try {
        //20041013 Jacky 修正不丟出commit的機制
        if ("".equals(oResourceBean_.getEmisErrorEvent().getBreakMessage())) {
          (oResourceBean_.getEmisDb()).commit();
        } else {
          //2004/08/07 Jacky增加錯誤的機制
          if (!oResourceBean_.getEmisErrorEvent().isBreakTransation()) {
            (oResourceBean_.getEmisDb()).commit();
          }
        }
      } catch (Exception ignore) {
      }

    } catch (Exception e) {
      //20041013 Jacky 修正錯誤不丟出Exception的機制
      if ("".equals(oResourceBean_.getEmisErrorEvent().getBreakMessage())) {
        (oResourceBean_.getEmisDb()).rollback();
        //2004/10/19 Jacky 增加填入RET1傳到前端處理
        oResourceBean_.getBusiness().setAttribute("RET1", e.getMessage());
        // throw e;
      } else {
        //2004/08/07 Jacky增加錯誤的機制
        if (oResourceBean_.getEmisErrorEvent().isBreakTransation()) {
          (oResourceBean_.getEmisDb()).rollback();
          
          //2004/10/19 Jacky 增加填入RET1傳到前端處理
          oResourceBean_.getBusiness().setAttribute("RET1",
                  oResourceBean_.getEmisErrorEvent().getBreakMessage());
        }
      }
    } finally {
      //20041014 Jacky  修正無法丟出Exception的錯誤
      if (oResourceBean_ != null) {
        if (oResourceBean_.getEmisDb() != null)
          (oResourceBean_.getEmisDb()).close();

        oResourceBean_ = null;
      }
    }

  }

  /**
   * 初始化 ThreadLocal 物件 added by Shaw
   */
  private void initThreadLocalObject() {
    emisThreadLocalObject.setDebug(new Boolean(this.oTracer_.isDebug_()));
  }

  /**
   * 透過錯誤訊息 (sMsg) 來決定是否為deadlock
   *
   * @param sMsg
   * @return
   */
  private boolean isNotDeadLock(String sMsg) {
    if (sMsg == null) {
      return true;
    }


    for (int i = 0; i < aDeadLockKeyWord.length; i++) {
      if (sMsg.indexOf(aDeadLockKeyWord[i]) > -1) {
        return false;
      }
    }
    return true;
  }

  /**
   * 讀入外部設定檔的程式
   *
   * @param oContext
   * @return
   * @throws IOException
   */

  private static boolean initLogStatus(ServletContext oContext) throws IOException {
    isInitDeadlockiInf_ = false;

    Properties _oProp = new Properties();

    File _oFile = new File(oContext.getInitParameter("emiscfg"));
    if (!_oFile.exists()) return false;
    FileInputStream _oFileIn = null;
    try {
      _oFileIn = new FileInputStream(_oFile);
      _oProp.load(_oFileIn);
    } finally {
      _oFileIn.close();
    }
    String _sLogProperties = _oProp.getProperty("documentroot") +
            "//WEB-INF//emisBusinessObject.properties";  // 取出webapp根目錄
    _oFile = new File(_sLogProperties);
    if (!_oFile.exists()) return false;

    try {
      iPboReDoInterval_ = (Integer.parseInt(_oProp.getProperty("emis.pbo.redoInterval")) * 1000);
      iPboReDoMaxCount_ = Integer.parseInt(_oProp.getProperty("emis.pbo.redoMaxCount"));
    } catch (Exception ignore) {
      ;
    } finally {
      _oProp.clear();
    }
    oLogger_ = Logger.getLogger("emisBusinessObject");
    PropertyConfigurator.configure(_sLogProperties);
    return true;
  }

  private static void logPboInf(java.lang.Throwable e) {
    oLogger_.info(e);
  }

  private static void logPboInf(String sMsg, java.lang.Throwable e) {
    oLogger_.info(sMsg, e);
  }

  private static void logPboInf(String sMsg) {
    oLogger_.info(sMsg);
  }

}