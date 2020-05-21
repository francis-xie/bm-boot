package com.emis.servlet.ht;

import com.emis.db.emisDb;
import com.emis.util.emisStrBuf;
import com.emis.util.emisUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class emisHeaderBack implements emisHtHeader
{
  private ArrayList data_ = new ArrayList();


  private head h;

  public void lineToData(emisStrBuf sLine) throws Exception
  {
    // 因為有中文的關係
    byte [] bLine = sLine.toString().getBytes();
    int nBytelen = bLine.length;
    if( nBytelen != 36)
      throw new Exception("Error length in Back:"+nBytelen);
    h = new head();
    h.sSNo_  = sLine.trimedsubstring(2,6);
    h.sDate_ = sLine.trimedsubstring(6,13);
    h.sPackageNum_=sLine.trimedsubstring(13,15);
    int nLen = sLine.length();
    h.sType_ = sLine.trimedsubstring(nLen-1,nLen);
    byte [] newbLine = new byte [20];
    System.arraycopy(bLine,15,newbLine,0,20);
    h.sComment_   = new String(newbLine);
    data_.add(h);
  }

  public void addLineData(emisStrBuf sLine) throws Exception
  {
    if( sLine.length() != 19 )
      throw new Exception("Error length in Back:"+sLine.length());
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

    // 90.12.25 alb 修改規格,退貨要去減 STOCK
    try {
      PreparedStatement stmtH = oDb.prepareStmt("INSERT INTO BACK_H (B_NO,COMPANY_NO,S_NO,B_DATE,B_TYPE,PACKAGE_NO,B_CREATE_D,B_UPDATE_D,B_ENTRY,B_CLOSE_D,B_REMARK) VALUES (?,?,?,?,?,?,?,?,'HT 轉入','',?)");
      try {
        PreparedStatement stmtD = oDb.prepareStmt("INSERT INTO BACK_D (COMPANY_NO,S_NO,B_NO,P_NO,RECNO,B_QTY,B_UPDATE_D,B_ENTRY) VALUES (?,?,?,?,?,?,?,'HT 轉入')");
        try {
          PreparedStatement InsertStock = oDb.prepareStmt("INSERT INTO STOCK (COMPANY_NO,S_NO,P_NO,P_QTY) VALUES (?,?,?,?)");
          try {
            PreparedStatement UpdateStock = oDb.prepareStmt("UPDATE STOCK SET P_QTY=isNull(P_QTY,0)-? where COMPANY_NO=? AND S_NO=? and P_NO=?");
            try {

              String sToday = emisUtil.todayDate();
              for(int i=0;i<size;i++) {
                h = (head) data_.get(i);
                if( h.sSNo_.length() <= 3 )
                  throw new Exception("S_NO length less than 4");
                String _sSNo = h.sSNo_.substring(0,3);

                int bsize = h.oBody_.size();

                String sB_No = null;
                for( int j=1; j<= bsize ; j++) {
                  if ((j% 10) == 1) {
                    // insert header data
                    sB_No = oSeqDb.getStoreSequence("back_seq",h.sSNo_+sCompanyNo,"%M","G"+_sSNo+"%y%m%4S");
                    oDb.setCurrentPrepareStmt(stmtH);
                    oDb.setString(1,sB_No);
                    oDb.setString(2,sCompanyNo);
                    oDb.setString(3,h.sSNo_);
                    oDb.setString(4,h.sDate_);
                    oDb.setString(5,h.sType_);
                    oDb.setString(6,h.sPackageNum_);
                    oDb.setString(7,sToday);
                    oDb.setString(8,sToday);
                    oDb.setString(9,h.sComment_);
                    oDb.prepareUpdate();
                  }
                  body b = (body) h.oBody_.get(j-1);
                  // insert Detail data
                  stmtD.setString(1,sCompanyNo);
                  stmtD.setString(2,h.sSNo_);
                  stmtD.setString(3,sB_No);
                  stmtD.setString(4,b.sP_NO);
                  stmtD.setInt(5,j%10);
                  stmtD.setInt(6,b.nQty);
                  stmtD.setString(7,sToday);
                  stmtD.executeUpdate();

                  UpdateStock.setInt(1,b.nQty);
                  UpdateStock.setString(2,sCompanyNo);
                  UpdateStock.setString(3,h.sSNo_);
                  UpdateStock.setString(4,b.sP_NO);
                  if (UpdateStock.executeUpdate() <= 0 ) {
                    InsertStock.setString(1,sCompanyNo);
                    InsertStock.setString(2,h.sSNo_);
                    InsertStock.setString(3,b.sP_NO);
                    InsertStock.setInt(4,-b.nQty);
                    InsertStock.executeUpdate();
                  }
                }
              }
            } finally {
              UpdateStock.close();
            }
          } finally {
            InsertStock.close();
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

    PreparedStatement ht_header = oDb.prepareStmt("INSERT INTO HT_TMP_H (TYPE,C_NO,S_NO,HDATE,BOXNUM,BACK_TYPE,COMMENT) VALUES (?,?,?,?,?,?,?)");
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
              if( j%10 == 1) {
                // insert header
                ht_header.setInt(1,2);
                ht_header.setString(2,sCompanyNo);
                ht_header.setString(3,h.sSNo_);
                ht_header.setString(4,h.sDate_);
                ht_header.setString(5,h.sPackageNum_);
                ht_header.setString(6,h.sType_);
                ht_header.setString(7,h.sComment_);
                ht_header.executeUpdate();
                // get last identity of current database session
                ResultSet rs = identity.executeQuery();
                if( rs.next() ) {
                  iIdent = rs.getInt(1);
                } else {
                  throw new Exception("unable to get identity value");
                }
              }
              body b = (body) h.oBody_.get(j-1);
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
    public String sPackageNum_;
    public String sComment_;
    public String sType_;
    public ArrayList oBody_ = new ArrayList();
  }

  public class body
  {
    public String sP_NO;
    public int nQty;
  }
}
