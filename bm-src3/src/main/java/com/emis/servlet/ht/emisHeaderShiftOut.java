package com.emis.servlet.ht;
import com.emis.db.emisDb;
import com.emis.util.emisStrBuf;
import com.emis.util.emisUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class emisHeaderShiftOut implements emisHtHeader
{
  private ArrayList data_ = new ArrayList();


  private head h;

  public void lineToData(emisStrBuf sLine) throws Exception
  {

    if( sLine.length() != 19)
      throw new Exception("Error length in ShiftOut:"+sLine.length());
    h = new head();
    h.sSNo_  = sLine.trimedsubstring(2,6);
    h.sToSNo_= sLine.trimedsubstring(6,10);
    h.sDate_ = sLine.trimedsubstring(10,17);
    h.sPackageNum_=sLine.trimedsubstring(17,19);
    data_.add(h);
  }

  public void addLineData(emisStrBuf sLine) throws Exception
  {
    if( sLine.length() != 19 )
      throw new Exception("Error length in ShiftOut:"+sLine.length());
    if( h == null )
      throw new Exception("encounter body data with no header");

    body b = new body();
    b.sP_NO = sLine.trimedsubstring(2,15);
    b.nQty = Integer.parseInt(sLine.trimedsubstring(15,19));
    h.oBody_.add(b);
  }

  public void processToDatabase(emisDb oDb,emisDb oSeqDb,String sCompanyNo) throws Exception
  {
    int size = data_.size();
    if( size == 0 )
      return;

    // check for general error, check for null
    for(int i=0; i< size;i++) {
      h = (head) data_.get(i);
      int bsize = h.oBody_.size();
      if( bsize == 0 )
        throw new Exception("header with no body found");
    }

    // process data to database
    try {
      PreparedStatement stmtH = oDb.prepareStmt(
      "INSERT INTO SHIFT_O_H (SHO_NO,SHO_DATE,COMPANY_NO_OUT,COMPANY_NO_IN,S_NO_OUT,S_NO_IN,SHO_WARE_NO,SHO_PURPOSE,SHI_NO,SHO_PRINT,PACKAGE_NO,SHO_CREATE_D,SHO_UPDATE_D,SHO_ENTRY,SHO_CLOSE_D) VALUES (?,?,?,?,?,?,'','','','N',?,?,?,'HT 轉入','')"
      );

      try {
        PreparedStatement stmtD = oDb.prepareStmt(
        "INSERT INTO SHIFT_O_D (COMPANY_NO_OUT,S_NO_OUT,SHO_NO,P_NO,PACKAGE_NO,RECNO,SHO_QTY,SHI_QTY,SHO_REAL_QTY,SHO_CREATE_D,SHO_UPDATE_D,SHO_ENTRY,SHO_REMARK) VALUES (?,?,?,?,?,?,?,0,0,?,?,'HT 轉入','')"
        );
        try {

          String sToday = emisUtil.todayDate();
          for(int i=0;i<size;i++) {
            h = (head) data_.get(i);
            if( h.sSNo_.length() <= 3 )
              throw new Exception("S_NO length less than 4");
            String _sSNo = h.sSNo_.substring(0,3);

            int bsize = h.oBody_.size();

            String sShift_No = null;
            for( int j=1; j<= bsize ; j++) {
              if ((j% 10) == 1) {
                sShift_No = oSeqDb.getStoreSequence("sho_seq",h.sSNo_+sCompanyNo,"%M","T"+_sSNo+"%y%m%4S");
                oDb.setCurrentPrepareStmt(stmtH);
                oDb.setString(1,sShift_No);
                oDb.setString(2,h.sDate_);
                oDb.setString(3,sCompanyNo); // 轉出限定同一家公司,alb 說的
                oDb.setString(4,sCompanyNo);
                oDb.setString(5,h.sSNo_);
                oDb.setString(6,h.sToSNo_);
                oDb.setString(7,h.sPackageNum_);
                oDb.setString(8,sToday);
                oDb.setString(9,sToday);
                oDb.prepareUpdate();
              }
              body b = (body) h.oBody_.get(j-1);

              oDb.setCurrentPrepareStmt(stmtD);
              oDb.setString(1,sCompanyNo);
              oDb.setString(2,h.sSNo_);
              oDb.setString(3,sShift_No);
              oDb.setString(4,b.sP_NO);
              oDb.setString(5,h.sPackageNum_);
              oDb.setInt(6,j%10);
              oDb.setInt(7,b.nQty);
              oDb.setString(8,sToday);
              oDb.setString(9,sToday);
              oDb.prepareUpdate();
            }
          }
        } finally {
          stmtD.close();
        }
      } finally {
        stmtH.close();
      }
    } finally {
      data_ = null;
    }
  }

  public void processToTmpDatabase(emisDb oDb,String sCompanyNo) throws Exception
  {
    int size = data_.size();
    if( size == 0 )
      return;

    // check for general error, check for null
    for(int i=0; i< size;i++) {
      h = (head) data_.get(i);
      if( h.sSNo_.length() != 4 )
        throw new Exception("S_NO length <> 4");
      int bsize = h.oBody_.size();
      if( bsize == 0 )
        throw new Exception("header with no body found");
    }

    PreparedStatement ht_header = oDb.prepareStmt("INSERT INTO HT_TMP_H (TYPE,C_NO,S_NO,HDATE,BOXNUM,SSNO) VALUES (?,?,?,?,?,?)");
    try {
      PreparedStatement identity = oDb.prepareStmt("SELECT @@Identity");
      try {
        PreparedStatement ht_detl = oDb.prepareStmt("INSERT INTO HT_TMP_D (ID,P_NO,QTY,RECNO) VALUES (?,?,?,?)" );
        try {
          for(int i=0;i<size;i++) {
            h = (head) data_.get(i);
            int bsize = h.oBody_.size();
            int iIdent = 0;
            for( int j=1; j<= bsize ; j++) {
              body b = (body) h.oBody_.get(j-1);
              if( j%10 == 1) {
                ht_header.setInt(1,4);
                ht_header.setString(2,sCompanyNo);
                ht_header.setString(3,h.sSNo_);
                ht_header.setString(4,h.sDate_);
                ht_header.setString(5,h.sPackageNum_);
                ht_header.setString(6,h.sToSNo_);
                ht_header.executeUpdate();
                ResultSet rs = identity.executeQuery();
                if( rs.next() ) {
                  iIdent = rs.getInt(1);
                } else {
                  throw new Exception("unable to get identity value");
                }
              }
              ht_detl.setInt(1,iIdent);
              ht_detl.setString(2,b.sP_NO);
              ht_detl.setInt(3,b.nQty);
              ht_detl.setInt(4,(j%10 == 0) ? 10 : (j%10));
              ht_detl.executeUpdate();
            } // end of all body of one header
          } // end of all header
        } finally {
          ht_detl.close();   ht_detl =null;
        }
      } finally {
        identity.close(); identity = null;
      }
    } finally {
      ht_header.close();   ht_header = null;
    }

  }


  public class head
  {
    public String sDate_;
    public String sSNo_;
    public String sToSNo_; // 對方門市
    public String sPackageNum_;
    public ArrayList oBody_ = new ArrayList();
  }

  public class body
  {
    public String sP_NO;
    public int nQty;
  }

}
