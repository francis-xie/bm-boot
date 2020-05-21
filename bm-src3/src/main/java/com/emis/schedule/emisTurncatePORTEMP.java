/**

 * Created by IntelliJ IDEA.

 * User: merlin

 * Date: Nov 19, 2002

 * Time: 10:36:36 AM

 * To change this template use Options | File Templates.

 */

package com.emis.schedule;



import com.emis.db.emisDb;

import com.emis.util.emisDate;



import java.sql.SQLException;



public abstract class emisTurncatePORTEMP extends emisTask {

  protected emisDb oDb_;



  public void run() {

    try {

      oDb_ = emisDb.getInstance(this.oContext_);

      oDb_.setAutoCommit(false);

      oDb_.prepareStmt("truncate table Portemp where CRE_DATE='" + new emisDate().toString() + "'");

      oDb_.prepareUpdate();

      oDb_.commit();

    } catch (Exception e) {

      try {

        oDb_.rollback();

      } catch (SQLException e1) {

        e1.printStackTrace();

      }

      e.printStackTrace();

    } finally {

      if (oDb_ != null) {

        oDb_.close();

        oDb_ = null;

      }

    }

  } // public void run()

}