/**
 * Created by IntelliJ IDEA.
 * User: merlin
 * Date: Nov 19, 2002
 * Time: 10:36:36 AM
 * To change this template use Options | File Templates.
 */

package com.emis.schedule;

import com.emis.db.emisDb;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;
import com.emis.qa.emisServletContext;
import com.emis.server.emisServerFactory;
import com.emis.util.emisDate;
import com.emis.util.emisUtil;
import javax.servlet.ServletContext;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public abstract class emisFileToDb extends emisTask {

    protected File oLogFile_;
    protected PrintWriter oLogWriter_;
    protected String LOG_FILE_NAME = "";
    protected PrintWriter badWriter;
    protected String sToday_= new emisDate().toString();
    protected BufferedInputStream sourceStream_ ;
    protected emisDb oTargetDb_ ;
    protected PreparedStatement oInsertStmt;
    protected PreparedStatement oUpdateStmt ;
    protected String dataLine;
    protected emisDb tempDb_ ;
    protected PreparedStatement tempStmt_;
    protected PreparedStatement recnoStmt_;
    protected String pathname_;
    private int nCommit=1;
    private String badFile;
    protected boolean insertOnly = false;

    public void run() {
        int processed=0;
        try {
            if (prepareLogFileSucess()) {
                if (prepareChangeDataSource()) {
                    putMessageToLogFile("[" + emisUtil.now() + "]" + "..資料準備完成");
                    prepareUpdateStatements();
                    processed = updateChangeData();
                    putMessageToLogFile("[" + emisUtil.now() + "]" + "..作業完畢...");
                    closeChangeDataSource();
                    writeEDIEMIS(processed);
                    moveFile();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            putMessageToLogFile("[" + emisUtil.now() + "]" + e.getMessage());
        } finally {
            closeLogFile();
            if (oTargetDb_ != null) {
                oTargetDb_.close();
                oTargetDb_ = null;
            }
            if (tempDb_ != null) {
                tempDb_.close();
                tempDb_ = null;
            }
        }
    } // public void run()



    private void moveFile() {
        String sTime = new java.sql.Time(System.currentTimeMillis()).toString();
        sTime = sTime.substring(0,2)+sTime.substring(3,5)+sTime.substring(6,8);
        String ext=pathname_.substring(pathname_.lastIndexOf("."));
        String backupFile = pathname_.substring(0,pathname_.lastIndexOf("."))+sTime+ext;
        int p = backupFile.lastIndexOf("\\");
        backupFile = backupFile.substring(0,p)+"\\backup"+backupFile.substring(p);
        try {
            Runtime.getRuntime().exec("cmd.exe /c move "+pathname_ + "  " + backupFile);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }



    private void writeEDIEMIS(int processed) throws SQLException {
        oTargetDb_.prepareStmt(
                "select max(EDI_NO) from ediemis");
        ResultSet rs  = oTargetDb_.prepareQuery();
        int edi_no = 1;
        rs.next();
        String s = rs.getString(1);
        if (s== null)
            edi_no = 1;
        else
            edi_no = Integer.parseInt(s)+1;
        oTargetDb_.prepareStmt(
                "insert into ediemis (EDI_NO, EDI_DATE, EDI_TIME, EDI_WAY, EDI_FILE, " +
                      "EDI_COUNT, EDI_STATUS)" +
                "values(?, ?, ?, ?, ?, ?, ? )");
        oTargetDb_.setInt(1, edi_no);
        oTargetDb_.setString(2, sToday_);
        String sTime = new java.sql.Time(System.currentTimeMillis()).toString();
        sTime = sTime.substring(0,2)+sTime.substring(3,5)+sTime.substring(6,8);
        oTargetDb_.setString(3, sTime);
        oTargetDb_.setString(4, "I");
        oTargetDb_.setString(5, pathname_);
        oTargetDb_.setInt(6, processed);
        oTargetDb_.setString(7, "1");
        oTargetDb_.prepareUpdate();
    }



    protected void closeChangeDataSource() throws IOException {
        if (sourceStream_ != null) {
            sourceStream_.close();
            sourceStream_ = null;
        }

        if (badWriter != null) {
            badWriter.close();
            badWriter = null;
        }
    }



//     作業相關 methods   必須修改才能進行作業



    protected abstract boolean prepareChangeDataSource() throws Exception;

    protected boolean prepareDataFiles(String pathname) throws Exception {
        File oTextFile = null;
        this.pathname_ = pathname;
        oTextFile = new File(pathname);
        if  (!oTextFile.exists())
            return false;
        FileInputStream fr = new FileInputStream(oTextFile);
        sourceStream_ = new BufferedInputStream(fr);
        badFile = pathname.substring(0, pathname.lastIndexOf('.')) + ".bad";
        badFile = "C:\\EDI\\DN\\BAD\\"+ badFile.substring(badFile.lastIndexOf("\\"));
        badWriter = new PrintWriter(new FileWriter(badFile));
        return (sourceStream_ != null);
    }

    protected abstract void prepareUpdateStatements() throws Exception;

    protected abstract int updateChangeData() throws Exception;

    protected int updateChangeData(int[] ColumnWidth) throws Exception {
        int count = 0;
        int updn=0, insn=0;
        int offset[] = new int [ColumnWidth.length];
        offset[0] = 0;
        for (int i = 1; i < ColumnWidth.length; i++) {
            offset[i] = ColumnWidth[i-1]+offset[i-1];
        }

        int bufLen = ColumnWidth[ColumnWidth.length-1] + offset[offset.length-1];
        byte b[] = null;
        try {
            b  = new byte[bufLen];
            while ((sourceStream_.read(b)) >= bufLen-2) {
                if (oUpdateStmt != null && setStmtParameters(b, ColumnWidth, offset, oUpdateStmt) > 0) {
                    ++updn;
                }   else {
                    setStmtParameters(b, ColumnWidth, offset, oInsertStmt);
                    ++insn;
                }
                if (count % 100 == 1) {
                    System.out.println(count+" : " + new String(b)+ "    " +
                                       (new java.sql.Time(System.currentTimeMillis()).toString())+
                                       "  Upd : "+ (updn) +"   Ins: "+ (insn));
                    this.oTargetDb_.commit();
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (b !=null)
                System.out.println(new String(b));
        } finally {
            this.oTargetDb_.commit();
            sourceStream_.close();
        }
        return count;
    }



    protected int updateChangeData(int[] ColumnWidth, PreparedStatement[] InsertStmt,

              PreparedStatement[] UpdateStmt, String[] method) throws Exception {

        if   (InsertStmt.length != UpdateStmt.length || UpdateStmt.length != method.length)

              throw new Exception("argument numbers don't match ");



        int count = 0;

        int offset[] = new int [ColumnWidth.length];

        offset[0] = 0;

        for (int i = 1; i < ColumnWidth.length; i++) {

            offset[i] = ColumnWidth[i-1]+offset[i-1];

        }

        Class thisClass = this.getClass();

        Method setter[] = new Method[method.length];

        for (int i = 0; i < setter.length; i++) {

            setter[i] = thisClass.getMethod(method[i],

                     new Class[] {byte[].class, int[].class, int[].class, PreparedStatement.class});

        }

        Object param[] = new Object[4];

        int bufLen = ColumnWidth[ColumnWidth.length-1] + offset[offset.length-1];

        try {

            byte b[] = new byte[bufLen];

            oTargetDb_.setAutoCommit(false);

            while ((sourceStream_.read(b)) >= bufLen-2) {

                param[0] = b;

                param[1] = ColumnWidth;

                param[2] = offset;

                try {

                    for (int i = 0; i < setter.length; i++) {

                         if (UpdateStmt[i] != null ) {

                             param[3] = UpdateStmt[i];

                             Integer result = (Integer) setter[i].invoke(this, param);

                             if (result.intValue() == 0) {

                                 if (InsertStmt[i] != null ) {

                                     param[3] = InsertStmt[i];

                                     setter[i].invoke(this, param);

                                 }

                             }

                         }

                    }

                    if (count % nCommit == 0) {

                        this.oTargetDb_.commit();

                    }

                    count++;

                    if (count % 5 == 0) {

                        System.out.println(count+" : " + new String(b, 0, b.length-2) + "    " +

                                       (new java.sql.Time(System.currentTimeMillis()).toString()));

                    }



                } catch (Exception e) {

                    e.printStackTrace(oLogWriter_);

                    badWriter.println(new String(b));

                    oLogWriter_.println(new String(b));

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            this.oTargetDb_.commit();

            sourceStream_.close();

        }

        return count;

    }



    protected abstract int setStmtParameters(byte[] b, int ColumnWidth[], int[] offset, PreparedStatement ps) throws Exception;



//   Logging 相關methods



    protected boolean prepareLogFileSucess() throws Exception {

        boolean bRetVal = true;
        initPath();
        oLogFile_ = new File(LOG_FILE_NAME);
        if (!oLogFile_.exists()) {
            oLogFile_.createNewFile();
        } else if (!oLogFile_.canWrite()) {
            bRetVal = false;
        }

        if (bRetVal) {
            oLogWriter_ = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE_NAME, true)));
        }

        return bRetVal;

    }



    protected void writeBadData(String data) {

        try {

            if (badWriter != null)

                badWriter.println(data);

        } catch (Exception oe) {

        }

    }



    protected void putMessageToLogFile(String sLogString) {

        try {

            if (oLogWriter_ != null)

                oLogWriter_.println(sLogString);

        } catch (Exception oe) {

        }

    }

    protected void closeLogFile() {

        try {

            if (oLogWriter_ != null)

                oLogWriter_.close();

        } catch (Exception oe) {

        }

    }

    protected void initPath() throws Exception {

        emisDirectory _oTempDir = null;

        emisFileMgr _oFileMgr = emisFileMgr.getInstance(oContext_);

        _oTempDir = _oFileMgr.getDirectory("root");

        _oTempDir = _oTempDir.subDirectory("data").subDirectory("sas_log");

        LOG_FILE_NAME = _oTempDir.getDirectory() + this.getClass().getName() + ".log";

    }



//     公用函式

final static String zeroes="0000000000000000000000000000000000000000000000000000";

    public final String strFit(String s, int len){
        s = s.trim();
        byte [] b = s.getBytes();
        int endIndex = len - b.length;
        if (endIndex < 0)
           return new String(b, b.length-len, len);
        return (zeroes.substring(0,endIndex)+s);
    }



    public final String intToStr(int i, int len){
        return strFit(String.valueOf(i), len);
    }

    final protected int parseInt(byte[] b, int pos, int width){
        String s1 = new String(b, pos, width).trim();
        return (s1.length() > 0 ? Integer.parseInt(s1) : 0);
    }



    final protected int parseInt(byte[] b, int pos, int width, int vdefault) {
        int v;
        try {
            v = parseInt(b, pos ,width);
        } catch (NumberFormatException e) {
            v= vdefault;
        }
        return v;
    }



    final protected String getTableKey(PreparedStatement stmt, String DataValue, String sDefault) throws Exception {
        return getTableKey(stmt, new String[]{DataValue}, sDefault);
    }



    final protected String getTableKey(PreparedStatement stmt, String[] Values, String sDefault) throws Exception {
        String sResult = "";
        try {
            tempDb_.setCurrentPrepareStmt(stmt);
            for (int i = 0; i < Values.length; i++) {
                tempDb_.setString(i+1, Values[i]);
            }
            ResultSet rs = tempDb_.prepareQuery();
            if (rs.next()) {
                sResult = rs.getString(1);
            } else {
                sResult = sDefault;
            }
        } catch (Exception db) {
            String sERROR = db.getMessage();
            throw new Exception(sERROR);
        }

        return sResult;

    }



    protected int getRecNo(String[] Values) throws Exception {

        return getTableKey(recnoStmt_, Values, 0)+1;

    }



    final protected int getTableKey(PreparedStatement stmt, String DataValue, int vDefault) throws Exception {

        return getTableKey(stmt, new String[]{DataValue}, vDefault);

    }



    final protected int getTableKey(PreparedStatement stmt, String[] Values, int vDefault) throws Exception {

        int vResult;

        try {

            tempDb_.setCurrentPrepareStmt(stmt);

            for (int i = 0; i < Values.length; i++) {

                tempDb_.setString(i+1, Values[i]);

            }

            ResultSet rs = tempDb_.prepareQuery();

            if (rs.next()) {

                vResult = rs.getInt(1);

            } else {

                vResult = vDefault;

            }

        } catch (Exception db) {

            String sERROR = db.getMessage();

            throw new Exception(sERROR);

        }

        return vResult;

    }



    final static public String convertToWesternDate(String sDate) {

        String sRet = sDate = sDate.trim();

        try {

        sRet = (sDate.length() == 7) ? (Integer.parseInt(sDate.substring(0,3))+1911) + sDate.substring(3)

                   : (sDate.length() == 6) ?

                   (Integer.parseInt(sDate.substring(0,2))+1911) + sDate.substring(2)

                        : sDate;

        } catch (RuntimeException e) {



        }

        return sRet;

    }

    

/*    final protected String validateStoreSeq(String SeqName, String StoreNo, ) {

          String StoreSeq = MyDb.getStoreSequence(SeqName, StoreNo,   "%Y", "%4e%c%5s");

    }

 */

    final protected static ServletContext getServletContext() throws Exception {

        ServletContext _oContext = new emisServletContext();

        emisServerFactory.createServer(_oContext, "g:\\wwwroot\\fas", "g:\\resin\\fas.cfg", true);

        return  _oContext;

    }

}



