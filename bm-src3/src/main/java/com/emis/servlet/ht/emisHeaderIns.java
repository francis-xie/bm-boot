package com.emis.servlet.ht;

import com.emis.db.emisDb;
import com.emis.util.emisStrBuf;
import com.emis.util.emisUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class emisHeaderIns implements emisHtHeader
{

  private ArrayList data_ = new ArrayList();


  private head h;

  public void lineToData(emisStrBuf sLine) throws Exception
  {

    if( sLine.length() != 15)
      throw new Exception("Error length in INS:"+sLine.length());
    h = new head();
    h.sSNo_  = sLine.trimedsubstring(2,6);
    h.sDate_ = sLine.trimedsubstring(6,13);
    h.sPackageNum_=sLine.trimedsubstring(13,15);
    data_.add(h);
  }

  public void addLineData(emisStrBuf sLine) throws Exception
  {
    if( sLine.length() != 19 )
      throw new Exception("Error length in INS:"+sLine.length());
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
    // 90.12.26 alb 修改規格,轉進 HT
    try {
      PreparedStatement ht = oDb.prepareStmt("INSERT INTO HT (COMPANY_NO,S_NO,HT_TYPE,HT_SOURCE,HT_DATE,HT_PACKAGE,P_NO,HT_QTY,HT_UPDATE_D) VALUES (?,?,?,?,?,?,?,?,?)" );
      try {
          String sToday = emisUtil.todayDate();
          for(int i=0;i<size;i++) {
            h = (head) data_.get(i);
            if( h.sSNo_.length() <= 3 )
              throw new Exception("S_NO length less than 4");

            int bsize = h.oBody_.size();

            for( int j=0; j< bsize ; j++) {
              body b = (body) h.oBody_.get(j);
              ht.setString(1,sCompanyNo);
              ht.setString(2,h.sSNo_);
              ht.setString(3,"1");
              ht.setString(4,"H");
              ht.setString(5,h.sDate_);
              ht.setString(6,h.sPackageNum_);
              ht.setString(7,b.sP_NO);
              ht.setInt(8,b.nQty);
              ht.setString(9,sToday);
              ht.executeUpdate();
            }
          }
      } finally {
        ht.close();
        ht= null;
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

    PreparedStatement ht_header = oDb.prepareStmt("INSERT INTO HT_TMP_H (TYPE,C_NO,S_NO,HDATE,BOXNUM) VALUES (?,?,?,?,?)");
    try {
      PreparedStatement identity = oDb.prepareStmt("SELECT @@Identity");
      try {
        PreparedStatement ht_detl = oDb.prepareStmt("INSERT INTO HT_TMP_D (ID,P_NO,QTY,RECNO) VALUES (?,?,?,?)" );
        try {
          for(int i=0;i<size;i++) {
            h = (head) data_.get(i);
            ht_header.setInt(1,1);
            ht_header.setString(2,sCompanyNo);
            ht_header.setString(3,h.sSNo_);
            ht_header.setString(4,h.sDate_);
            ht_header.setString(5,h.sPackageNum_);
            ht_header.executeUpdate();
            int bsize = h.oBody_.size();
            ResultSet rs = identity.executeQuery();
            if( rs.next() ) {
              int iIdent = rs.getInt(1);
              for( int j=0; j< bsize ; j++) {
                body b = (body) h.oBody_.get(j);
                ht_detl.setInt(1,iIdent);
                ht_detl.setString(2,b.sP_NO);
                ht_detl.setInt(3,b.nQty);
                ht_detl.setInt(4,j+1);
                ht_detl.executeUpdate();
              } // end of all body of one header
            } else {
              throw new Exception("unable to get identity value");
            }
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
    public ArrayList oBody_ = new ArrayList();
  }

  public class body
  {
    public String sP_NO;
    public int nQty;
  }

}
