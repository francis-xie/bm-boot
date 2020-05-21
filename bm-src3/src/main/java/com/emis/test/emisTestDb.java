package com.emis.test;

import com.emis.db.emisDb;

import javax.servlet.ServletContext;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2005/1/7
 * Time: 下午 01:14:07
 * To change this template use Options | File Templates.
 */
public class emisTestDb implements Runnable {
    private ServletContext oContext_;
    private emisApServerIsLive emisApServerisLive =null;

    public emisTestDb(ServletContext oContext_,emisApServerIsLive emisApServerisLive) {
        this.oContext_ = oContext_;
        this.emisApServerisLive =emisApServerisLive;

    }

    public void run() {
        emisDb odb = null;
        java.sql.PreparedStatement testStatment = null;
        try {
            odb = emisDb.getInstance(oContext_);
            testStatment = odb.prepareStmt("select S_NO from store where rownum = 1");
            testStatment.executeQuery();
            System.out.println("run emisTestDb");
            emisApServerisLive.isLive = true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                if (testStatment != null) testStatment.close();
                if (odb != null) odb.close();
            } catch (Exception ignore) {
                ;
            }
        }
    }
}
