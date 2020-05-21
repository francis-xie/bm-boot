package com.emis.audit;



import com.emis.manager.*;

import com.emis.db.*;

import com.emis.user.*;

import com.emis.trace.*;

import com.emis.server.emisServer;

import com.emis.util.emisUtil;



import java.util.*;



import javax.servlet.http.*;

import javax.servlet.ServletContext;





/**

 * 負責稽核的功能,

 * 在系統設定檔中,將 emis.audit.enable=[false/true] 打開

 * 會將監督資料寫入 USERLOG Table

 * LOGTIME      DATETIME

 * USERID       VARCHAR

 * BUSINESSNAME VARCHAR

 * DO_ACTION    VARCHAR

 */

public class emisAudit extends emisAbstractMgr {

    public static final String STR_EMIS_AUDIT = "com.emis.audit";

    private static ServletContext application = null;

    private static boolean isSetLog = true;

    private static boolean isReadFlag = false;

    private static boolean isTruncate = true;

    private boolean isAuditEnabled_ = false;

    private int nErrorlog_;



    public emisAudit(ServletContext application, Properties props) throws Exception {

        super(application, STR_EMIS_AUDIT, "emisAudit");

        this.application = application;

        if ("true".equalsIgnoreCase(props.getProperty("emis.audit.enable", "false"))) {

            //audit enabled

            isAuditEnabled_ = true;

        }

    }



    public void audit(emisUser oUser, String sBusinessName, String sAction) {

        if (!isAuditEnabled_) return;

        try {

            emisDb oDb = emisDb.getInstance(application_);

            try {

                oDb.prepareStmt("INSERT INTO USERLOG (USERID,BUSINESSNAME,DO_ACTION,LOGTIME,LOGINIP) VALUES (?,?,?,?,?)");

                if (oUser != null)

                    oDb.setString(1, oUser.getID());

                else

                    oDb.setString(1, "unknow");

                oDb.setString(2, sBusinessName);

                oDb.setString(3, sAction);

                oDb.setString(4,emisUtil.formatDateTime("%y%M%D %h:%m:%s",emisUtil.now()));

                oDb.setString(5, emisUtil.parseString(oUser != null ? oUser.getAttribute("LOGIN_IP") : "unknow",""));

                oDb.prepareUpdate();

            } finally {

                oDb.close();

            }

        } catch (Exception ignore) {

            // just do 'some' log, it is audit...

            if (nErrorlog_ < 5) {

                nErrorlog_++;

                emisTracer.get(application_).warning(this, ignore);



            }

        }

    }



    public void setProperty(int propertyID, Object oValue) throws Exception {

    }



    /**

     * get the singleton emisAudit Object

     */

    public static emisAudit getInstance(ServletContext application) throws Exception {

        emisAudit _oMgr = (emisAudit) application.getAttribute(emisAudit.STR_EMIS_AUDIT);



        if (_oMgr == null) {

            emisTracer.get(application).sysError(null, emisError.ERR_SVROBJ_NOT_BIND, "emisAudit");

        }

        return _oMgr;

    }



    public static synchronized void setLog(String SQL, ArrayList oParam) {

    	// robert, change this to the top
    	if (!isReadFlag) readflag();
        if (!isSetLog) return;

    	
    	emisDb oDb = null;

        HttpServletRequest request = null;

        String UserID = "";

        String URL_FILTER = "";

        String BusinessName = "";

        String SQLSTmt ="";

        System.out.println("-- -------------------- SQL -------------------- --");
        System.out.println(replaceParam(SQL, oParam).trim());

/*        dana.gao 2012/01/19 目前系统没有SQLLOG这个表,会死循环,而且记录SQL的意义不大,有需要再放出来.
        try {

            request = (HttpServletRequest) application.getAttribute("SQLFilterHttpRequest");

            if (request == null) return;

            UserID = request.getAttribute("SQL_LOG_USERID")+"";

            URL_FILTER = request.getRequestURI();

            BusinessName = request.getAttribute("SQL_LOG_BusinessName")+"";

        } catch (Exception e) {

            e.printStackTrace();

            return;

        }

        if (SQL == null  || SQL.equalsIgnoreCase("")) return;





        try {

            oDb = emisDb.getInstance(application);

            if (isTruncate && Integer.parseInt((emisUtil.formatDateTime("%D", new Date()))) % 2 == 0) {

                oDb.prepareStmt("truncate  table  SQLLOG");

                oDb.prepareUpdate();

                isTruncate =false;

            }

            oDb.prepareStmt("INSERT INTO SQLLOG (USERID,URI,SQL,BUSSINESSNAME,TIME) VALUES (?,?,?,?,?)");

            SQLSTmt = replaceParam(SQL, oParam).trim();

            if (SQLSTmt.length() > 2000) SQLSTmt = SQLSTmt.substring(0, 999);

            oDb.setString(1, UserID.trim());

            oDb.setString(2, URL_FILTER.trim());

            oDb.setString(3, SQLSTmt.trim());

            oDb.setString(4, BusinessName.trim());

            oDb.setString(5, (emisUtil.formatDateTime("%y/%M/%D %h:%m:%s", new Date())).trim());

            oDb.prepareUpdate();

        } catch (Exception ignore) {

            System.err.println("UserID.trim()["+(UserID.trim()).length()+"]");

            System.err.println("URL_FILTER.trim()["+URL_FILTER.trim().length()+"]");

            System.err.println("SQLSTmt.trim()["+SQLSTmt.trim().length()+"]");

            System.err.println("BusinessName.trim()["+BusinessName.trim().length()+"]");

         //   ignore.printStackTrace();

        } finally {

            if (oDb != null) oDb.close();

        }*/



    }



    private static void readflag() {

        isReadFlag = true;



        try {

            emisServer _oServer = (emisServer) application.getAttribute(emisServer.STR_EMIS_SERVER);

            Properties _oProp = _oServer.getProperties();

            if (_oProp.getProperty("com.emis.auditSQLLOG").equalsIgnoreCase("true")) {

                isSetLog = true;

            }

        } catch (Exception e) {

        }

    }



    private static String replaceParam(String sSQL, ArrayList oParam) {

        String SQL = sSQL;

        if (oParam == null) return SQL;

        int size = oParam.size();

        if (size == 0) return SQL;

        try {
          StringBuffer sb = new StringBuffer(sSQL);
          StringBuffer tmp = new StringBuffer();
          for (int idx = 0, i = 0; i < size; i++) {
            // 修正参数有问号时替换错位
            idx = sb.indexOf("?", idx);
            if (idx == -1) // no ?
              break;
            tmp.setLength(0);
            if (oParam.get(i) == null) {
              tmp.append("''");
            } else {
              tmp.append("'").append(oParam.get(i).toString()).append("'");
            }
            sb.replace(idx, idx + 1, tmp.toString());
            idx += tmp.length();
          }
          SQL = sb.toString();
        } catch (Exception e) {
          return SQL;
        }


//      for (int i = 0; i < size; i++) {
//
//            int idx = SQL.indexOf("?");
//
//            if (idx == -1) // no ?
//
//                break;
//
//            Object o = oParam.get(i);
//
//            if (o == null)
//
//                SQL = SQL.substring(0, idx) + "''" + SQL.substring(idx + 1);
//
//            else
//
//                SQL = SQL.substring(0, idx) + "'" + o.toString() + "'" + SQL.substring(idx + 1);
//
//        }



        return SQL;

    }





}