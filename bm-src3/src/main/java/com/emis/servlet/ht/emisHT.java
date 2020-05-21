package com.emis.servlet.ht;

import com.emis.db.emisDb;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.util.emisMultipartRequest;
import com.emis.util.emisUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * 進行盤點機資料上傳後的處理
 * 只處理一個檔
 */
public class emisHT extends HttpServlet
{
  public void doGet(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
  public void doPost(HttpServletRequest p0, HttpServletResponse p1) throws ServletException, IOException { service(p0,p1); }
  public void service( HttpServletRequest  request, HttpServletResponse response ) throws ServletException, IOException
  {
    ServletContext application = getServletContext();
    response.setContentType("text/html;charset="+emisUtil.FILENCODING);
    PrintWriter out = response.getWriter();
    try {
      emisMultipartRequest multi = new emisMultipartRequest(application,request);
      String sCertStr = multi.getParameter("EMIS_CERT");
      String sStoreNo = multi.getParameter("EMIS_S_NO");
      String sCompanyNo=multi.getParameter("EMIS_C_NO");
      checkCert(application,sStoreNo,sCompanyNo,sCertStr);
      // pass check , do work

      String sFileName=multi.getParameter("FILENAME");
      emisDirectory _oDir = emisFileMgr.getInstance(application).getDirectory("root").subDirectory("data").subDirectory("ht").subDirectory(sCompanyNo).subDirectory(sStoreNo);
      emisFile f = _oDir.getFile(sFileName);
      if( ! f.exists() )
        throw new Exception("File not Exists:"+f.getFullName());

      if( "ZIP".equalsIgnoreCase(f.getFileExt())) {
        // extract the zip file
        f = emisUtil.extractZip(f,_oDir,true);
      }

      // 比對最後一次結進去的檔,和此次的檔是不是完全一樣
      // 完全一樣則不轉入
      /**
      emisFile last = _oDir.subDirectory("last").getFile(f.getFileName());
      if( last.exists() ) {
        if( last.length() == f.length() ) {
          // 比對內容
          if( compareFile(last,f)) {
            throw new Exception("重覆資料上傳");
          }
        }
      }
      */
      emisHtHandler handler = new emisHtHandler(application);
      handler.processDataFile(sCompanyNo,f.getFullName());

      out.println("EMIS_RESULT:OK");
    } catch (Exception e) {

      out.println("EMIS_RESULT:"+emisUtil.getStackTrace(e));
    }
  }

  private void checkCert(ServletContext application,String sStoreNo,String sCompanyNo,String sCertStr)throws Exception
  {
    if( (sCertStr == null) || (sStoreNo == null) || (sCompanyNo == null)) {
      throw new Exception("無法取得認證或公司別/門市號碼");
    }
    int slen = sStoreNo.length();
    int clen = sCertStr.length();
    if( clen != slen * 3 ) {
      throw new Exception("認證錯誤1");
    }
    int j=1;
    for(int i=0;i<slen;i+=3)
    {
      int iValue = Integer.parseInt(sCertStr.substring(i,i+1));
      int jValue = Integer.parseInt(sCertStr.substring(i+1,i+2));
      int kValue = Integer.parseInt(sCertStr.substring(i+2,i+3));
      System.out.println(iValue+":"+jValue+":"+kValue);
      if(kValue != ((iValue * jValue * j) % 10)) {
        throw new Exception("認證錯誤2");
      }
      j++;
    }
    emisDb oDb = emisDb.getInstance(application);
    try {
      oDb.prepareStmt("SELECT 1 FROM STORE with (NOLOCK) WHERE S_NO=?");
      oDb.setString(1,sStoreNo);
      oDb.prepareQuery();
      if(!oDb.next()) {
        throw new Exception("門市編號不存在:"+sStoreNo);
      }
      oDb.prepareStmt("SELECT 1 FROM COMPANY with (NOLOCK) WHERE COMPANY_NO=?");
      oDb.setString(1,sCompanyNo);
      oDb.prepareQuery();
      if(!oDb.next()) {
        throw new Exception("公司別編號不存在:"+sCompanyNo);
      }
    } finally {
      oDb.close();
    }
  }

  // 為了保證不會重複入資料
  private boolean compareFile(emisFile f1,emisFile f2)  throws Exception
  {
    InputStream in1 = f1.getInStream();
    try {
      InputStream in2 = f2.getInStream();
      try {
        byte [] buffer1 = new byte [4096];
        byte [] buffer2 = new byte [4096];
        int iReaded1,iReaded2 = 0;
        while( (iReaded1 = in1.read(buffer1)) != -1) {
          iReaded2 = in2.read(buffer2,0,iReaded1);
          if( iReaded2 == -1 ) return false;
          if( iReaded1 != iReaded2 ) return false;
          for(int i=0;i<iReaded1;i++) {
            if( buffer1[i] != buffer2[i])  return false;
          }
        }
        return true;
      } finally {
        in2.close();
      }
    } finally {
      in1.close();
    }
  }
}

/*** SQL used for test

select * from ht_tmp_h

select * from ht_tmp_d

select * from back_h
select * from back_d


select * from Count_h
select * from Count_d


select * from ht

execute dbo.epos_transferHt

truncate table ht_tmp_h; truncate table ht_tmp_d;
truncate table ht
truncate table back_h ; truncate table back_d
truncate table shift_o_h; truncate table shift_o_d;
truncate table count_h; truncate table count_d;


select * from ht_maxNo


select MAXNO FROM HT_MAXNO
where C_NO='01' and S_NO='0019' and AREA='02'

insert into  HT_MAXNO
select Company_no,s_no,c_area as AREA,max(C_SN) MAXNO  from COUNT_D group by Company_no,s_no,c_Area

*/
