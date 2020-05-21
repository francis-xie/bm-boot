package com.emis.servlet.ht;

import com.emis.db.emisDb;
import com.emis.util.emisStrBuf;
import com.emis.util.emisUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;


public class emisHeaderCount implements emisHtHeader
{
  private ArrayList data_ = new ArrayList();


  private head h;

  public void lineToData(emisStrBuf sLine) throws Exception
  {

    if( sLine.length() != 15)
      throw new Exception("Error length in COUNT:"+sLine.length());
    h = new head();
    h.sSNo_  = sLine.trimedsubstring(2,6);
    h.sDate_ = sLine.trimedsubstring(6,13);
    h.sArea_  = sLine.trimedsubstring(13,15);
    data_.add(h);
  }

  public void addLineData(emisStrBuf sLine) throws Exception
  {
    if( sLine.length() != 23 )
      throw new Exception("Error length in COUNT:"+sLine.length());
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
      PreparedStatement stmtH = oDb.prepareStmt("INSERT INTO COUNT_H (C_NO,C_DATE,COMPANY_NO,S_NO,C_AREA,C_CREATE_D,C_UPDATE_D,C_ENTRY,C_CLOSE_D) VALUES (?,?,?,?,?,?,?,'HT 轉入','')");
      try {
        PreparedStatement stmtD = oDb.prepareStmt("INSERT INTO COUNT_D (C_NO,P_NO,C_AREA,C_SN,RECNO,C_QTY,COMPANY_NO,S_NO) VALUES (?,?,?,?,?,?,?,?)");
        try {
          PreparedStatement maxD = oDb.prepareStmt("SELECT isNull(MAX(C_SN),0) as CNT from COUNT_D where COMPANY_NO=? and S_NO=? and C_AREA=?");
          try {
            String sToday = emisUtil.todayDate();

            HashMap maxSeqMap = new HashMap();

            for(int i=0;i<size;i++) {
              h = (head) data_.get(i);
              if( h.sSNo_.length() <= 3 )
                throw new Exception("S_NO length less than 4");
              String _sSNo = h.sSNo_.substring(0,3);
              String sCount_No = oSeqDb.getStoreSequence("cnt_seq",h.sSNo_+sCompanyNo,"%M","O"+_sSNo+"%y%m%4S");


              oDb.setCurrentPrepareStmt(stmtH);
              oDb.setString(1,sCount_No);
              oDb.setString(2,h.sDate_);
              oDb.setString(3,sCompanyNo);
              oDb.setString(4,h.sSNo_);
              oDb.setString(5,h.sArea_);
              oDb.setString(6,sToday);
              oDb.setString(7,sToday);
              oDb.prepareUpdate();

              int bsize = h.oBody_.size();
              for( int j=1; j<= bsize ; j++) {
                body b = (body) h.oBody_.get(j-1);

                int iMax = getMax(maxSeqMap,oDb,maxD,sCompanyNo,h);

                oDb.setCurrentPrepareStmt(stmtD);
                oDb.setString(1,sCount_No);
                oDb.setString(2,b.sP_NO);
                oDb.setString(3,h.sArea_);
                // 找出最大號
                oDb.setInt(4,iMax);
                oDb.setInt(5,j);
                oDb.setInt(6,b.nQty);
                oDb.setString(7,sCompanyNo);
                oDb.setString(8,h.sSNo_);
                oDb.prepareUpdate();
              }
            }
          } finally {
            maxD.close();
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

  private int getMax(HashMap maxSeqMap,emisDb oDb,PreparedStatement maxD,String sCompanyNo,head h)  throws Exception
  {
    String sKey = sCompanyNo + "." + h.sSNo_ + "." + h.sArea_;
    Integer iMax = (Integer) maxSeqMap.get(sKey);
    if( iMax == null ) {
      oDb.setCurrentPrepareStmt(maxD);
      oDb.setString(1,sCompanyNo);
      oDb.setString(2,h.sSNo_);
      oDb.setString(3,h.sArea_);
      ResultSet rs = oDb.prepareQuery();
      try {
        if( rs.next() ) {
          int i = rs.getInt(1); // 目前的最大值
          i++;
          iMax = new Integer(i);
          maxSeqMap.put(sKey,iMax);
          return i;
        } else {
          throw new Exception("unable to get max value by key:"+sKey);
        }
      } finally {
        rs.close();
      }
    } else {
      int i = iMax.intValue();
      i++;
      maxSeqMap.put( sKey, new Integer(i));
      return i;
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
            ht_header.setInt(1,5);
            ht_header.setString(2,sCompanyNo);
            ht_header.setString(3,h.sSNo_);
            ht_header.setString(4,h.sDate_);
            ht_header.setString(5,h.sArea_);
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
    public String sArea_;
    public ArrayList oBody_ = new ArrayList();
  }

  public class body
  {
    public String sP_NO;
    public int nQty;
  }
}
