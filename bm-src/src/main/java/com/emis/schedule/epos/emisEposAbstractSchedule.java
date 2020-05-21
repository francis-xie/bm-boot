package com.emis.schedule.epos;

import com.emis.business.emisBusinessResourceBean;
import com.emis.business.emisHttpServletRequest;
import com.emis.db.emisDb;
import com.emis.file.emisFileMgr;
import com.emis.schedule.emisOnLineTask;
import com.emis.schedule.emisTask;
import com.emis.user.emisUser;
import com.emis.util.emisLogger;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * $Header: /repository/epos_src/com/emis/schedule/epos/emisEposAbstractSchedule.java,v 1.7 2006/06/13 08:42:23 Ben Exp $
 * 排程的supper class.
 *
 * @author mike
 * @version 1.0
 */
abstract public class emisEposAbstractSchedule extends emisTask {
  protected Logger oLogger_ = null;
  protected emisDb oDataSrc_ = null;
  protected emisBusinessResourceBean resourceBean_ = null;


  public void runTask () throws Exception  {

    try {
      getResourceBean();
      oLogger_.info("-----------Running");

      // 实作中继承并override该Method
      postAction();

      oLogger_.info("-----------End");
      this.oDataSrc_.commit();
    } catch (Exception e) {
      oLogger_.error("",e);
      try {
        oDataSrc_.rollback();
      } catch (Exception er) {
        er.printStackTrace();
      }
    } finally {
      resourceBean_ = null;
      if (oDataSrc_ != null)
        this.oDataSrc_.close();
    }
  }

  /**
   * 取得Db 的商务逻辑.
   *
   * @return resourceBean_
   */
  public final emisBusinessResourceBean getResourceBean() {
    if (resourceBean_ == null) {
      try {
        oLogger_ = emisLogger.getlog4j(oContext_, this.getClass().getName());
        oDataSrc_ = emisDb.getInstance(oContext_);
        oDataSrc_.setAutoCommit(false);
        resourceBean_ = new emisBusinessResourceBean();
        resourceBean_.setEmisDb(oDataSrc_);
        resourceBean_.setFileMgr(emisFileMgr.getInstance(oContext_));
        resourceBean_.setEmisHttpServletRequest(new emisHttpServletRequest());
        resourceBean_.setServletContext(oContext_);
      } catch (Exception e) {
        e.printStackTrace();
        // Log here;  //To change body of catch statement use Options | File Templates.
      }
    }
    return resourceBean_;
  }

  /**
 *   提供gettor 介面供子类别以外的class使用.
  * @return   the Log4J logger instance being used.
 */
  public Logger getLogger() {
    return oLogger_;
  }

  abstract protected void postAction() throws Exception;
}
