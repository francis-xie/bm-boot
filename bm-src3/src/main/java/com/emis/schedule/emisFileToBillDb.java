/**

 * Created by IntelliJ IDEA.

 * User: merlin

 * Date: Nov 19, 2002

 * Time: 10:36:36 AM

 * To change this template use Options | File Templates.

 */

package com.emis.schedule;



import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.util.HashSet;



public abstract class emisFileToBillDb extends emisFileToDb {



    protected PreparedStatement oInsert_DStmt;

    protected PreparedStatement oQueryStmt;

    protected PreparedStatement oQuery_DStmt;

    protected char delStr = '|';

    protected int nItems  = 0;



    protected HashSet BillSet= null;

    protected PreparedStatement oDelStmt, oDelStmt_D;

    protected String s_no, billNo;





    protected int updateChangeBillData(char delchar) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(sourceStream_));

        String data="";

        int count=0;

        int ColumnWidth[]= new int[nItems];

        int offset[]     = new int[nItems];

        byte delbyte = (byte) delchar;

        while ((data = br.readLine()) !=null && data.length() > 0) {

            byte [] b = data.getBytes();

            int n=0;

            int k =0;

            offset[0]=0;

            while (n < b.length) {

                if (b[n]==delbyte) {

                    ColumnWidth[k]=n-offset[k];

                    k++;

                    offset[k]=n+1;

                }

                n++;

            }

            ColumnWidth[nItems-1]=b.length-offset[nItems-1];

            try {

                if (checkExistData(b, ColumnWidth, offset, oQueryStmt)) {

                   if (BillSet != null) {

                       if (!deleted(billNo)) {

                          delExistData(billNo);

                          setStmtParameters(b, ColumnWidth, offset, oInsertStmt);

                       }

                   }

                }  else  {

                    setStmtParameters(b, ColumnWidth, offset, oInsertStmt);

                    BillSet.add(billNo);

                }

                if (!checkExistData_d(b, ColumnWidth, offset, oQuery_DStmt)) {

                     setStmtParameters_d(b, ColumnWidth, offset, oInsert_DStmt);

                     System.out.println(count+" : " + data+ "    " +

                                     (new java.sql.Time(System.currentTimeMillis()).toString()));

                     count++;

                 }

                 oTargetDb_.commit();

                } catch (Exception e) {

                    e.printStackTrace(oLogWriter_);

                    e.printStackTrace();

                    badWriter.println(data);

                }

            }

        br.close();

        return count;

    }





    protected void setDelStmts(String Bill_h, String Bill_d, String b_no_name) throws SQLException {

        String sSQL = "delete from " + Bill_h + " where " + b_no_name + " = ? ";

        oDelStmt = this.oTargetDb_.prepareStmt(sSQL);

        sSQL = "delete from " + Bill_d + " where " + b_no_name + " = ? ";

        oDelStmt_D = this.oTargetDb_.prepareStmt(sSQL);

    }



    protected void delExistData(String Bill_NO) throws SQLException {

        this.oTargetDb_.setCurrentPrepareStmt(this.oDelStmt);

        oTargetDb_.setString(1, Bill_NO);

        oTargetDb_.prepareUpdate();

        this.oTargetDb_.setCurrentPrepareStmt(this.oDelStmt_D);

        oTargetDb_.setString(1, Bill_NO);

        oTargetDb_.prepareUpdate();

    }



    protected boolean deleted(String Bill_NO) {

        boolean bRet;

        if (BillSet.contains(Bill_NO))

            bRet = true;

        else {

            bRet = false;

            BillSet.add(Bill_NO);

        }

        return bRet;

    }



    protected int updateChangeBillData(int[] ColumnWidth) throws Exception {

        String data="";

        if (ColumnWidth == null)

           return (updateChangeBillData(delStr));

        int count = 0;

        int offset[] = new int [ColumnWidth.length];

        offset[0] = 0;

        for (int i = 1; i < ColumnWidth.length; i++) {

            offset[i] = ColumnWidth[i-1]+offset[i-1];

        }

        int bufLen = ColumnWidth[ColumnWidth.length-1] + offset[offset.length-1];

        byte b[] = new byte[bufLen];

        while ((sourceStream_.read(b)) >= bufLen-2) {

            data = new String(b);

            try {

                if (!checkExistData(b, ColumnWidth, offset, oQueryStmt)) {

                        setStmtParameters(b, ColumnWidth, offset, oInsertStmt);

                }

                if (!checkExistData_d(b, ColumnWidth, offset, oQuery_DStmt)) {

                     setStmtParameters_d(b, ColumnWidth, offset, oInsert_DStmt);

                     System.out.println(count+" : " + data+ "    " +

                                     (new java.sql.Time(System.currentTimeMillis()).toString()));

                     count++;

                 }

                 oTargetDb_.commit();

                } catch (Exception e) {

                    e.printStackTrace(oLogWriter_);

                    e.printStackTrace();

                    badWriter.println(data);

                }

            }

        sourceStream_.close();

        return count;

    }



    protected abstract boolean checkExistData_d(byte[] b, int[] columnWidth, int[] offset, PreparedStatement oQueryStmt) throws SQLException;



    protected abstract boolean checkExistData(byte[] b, int[] columnWidth, int[] offset, PreparedStatement oQueryStmt) throws SQLException;



    protected abstract int setStmtParameters_d(byte[] b, int[] columnWidth, int [] offset, PreparedStatement oInsert_dStmt) throws Exception;



}



