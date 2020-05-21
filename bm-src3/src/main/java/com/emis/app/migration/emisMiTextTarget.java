package com.emis.app.migration;

import com.emis.db.emisDb;
import com.emis.util.*;
import com.emis.report.emisString;

import java.io.*;
import java.nio.channels.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.emis.db.emisProp;

/**
 * $Id: emisMiTextTarget.java 10243 2017-11-22 07:15:23Z andy.he $
 * User: merlin Date: Apr 22, 2003 Time: 6:42:57 PM wing
 * [14309] 2010/02/06 wing  string check 效能调整及20100201加入導出MS950,GBK,UTF-8功能
 */
public class emisMiTextTarget extends emisMiTarget {
    String fullName;
	// BufferedWriter out;
	private File outFile;

	private String sDateTime;  //臨時檔outFile 檔名后加入年月日時分秒
    private PrintWriter oWriter_;

	String ccrOutFileCharSet;

	StringBuffer dataLine_ = new StringBuffer();

  StringBuffer bufData = new StringBuffer();
  private String sCurSNo;
  private List listCurIdNo;
  private boolean bStoreServer = false;  // 门店是否有小后台。

	// int count = 0;

	public boolean open(emisMiConfig config) throws Exception {

		emisUTF8StringUtil.initUTF8HardChar();
		/**
		 * 取得輸出文件的格式
		 */
		// 避免reopen打開時傳入空對像
		if (config != null) {
			ccrOutFileCharSet = emisProp.getInstance(config.getContext()).get("CCR_OUTFILE_CHARSET", "UTF-8");
		}
		// boolean deleted;
		boolean _bRet;
		fileName = getFileName();
		File outDir = new File(path);
		if (!outDir.exists()) {
			_bRet = outDir.mkdirs();
			if (!_bRet)
				return false;
		}
    sDateTime = emisUtil.todayDateAD() + emisUtil.todayTimeS();
    fullName = path + File.separator + fileName + sDateTime + "_" + Thread.currentThread().getId();
		outFile = new File(fullName);
		if (clear && outFile.exists()) {
			// deleted = outFile.delete();
			if (!outFile.delete())
				return false;
		}
		// wing 20100128 創建文件輸出流
		oWriter_ = emisUTF8StringUtil.createWriter(ccrOutFileCharSet, outFile.getAbsolutePath());
		writeCount = 0;
		return true;
	}

	public boolean write(String[] data) throws IOException {
		dataLine_.setLength(0);
		for (int i = 0; i < data.length; i++)
			dataLine_.append(data[i]);

		dataLine_.append("\r\n");
		String str = dataLine_.toString();
		// 調用print過濾，不用再換行了
		print(str);
    writeCount++; // 记录资料笔数
		return true;
	}

	public boolean close(boolean closeDb) throws IOException {
		/*
		 * if (out != null) { out.close(); out = null; }
		 */

		if (oWriter_ != null) {
			oWriter_.close();
			oWriter_ = null;
		}
		return false;
	}

  // 檢查是否檔案有 zero
  protected void doCheck(String sFileName) {

	if( oLogWriter_ == null ) return;

  	try {
		DataInputStream din = new DataInputStream( new FileInputStream(sFileName) );
		try {
			byte [] buf = new byte[4096];
			int r;
			while( (r=din.read(buf)) != -1 ) {
				for(int i=0;i<r;i++) {
					if( buf[i]==0) {
						oLogWriter_.println("Error : Found Zero Byte in " + sFileName + " !");
						break;
					}
				}
			}
		} finally{
			din.close();
		}
  	} catch (Exception ignore) {
  	}

  }


  public  void append( String[] path,  boolean reopen) throws Exception {
    close(false);
//    out.flush();
//    out.close();
//    out = null;

    //  robert,2010/03/02 寫完檔,先做 check,做 log,確定問題的範圍
    //doCheck(fullName);

    String id_no[];
    long length = 0;
    emisMiLogToDb logToDb = this.config.getMiLogToDb();
    logToDb.setDL_FILE_D(emisUtil.todayDateAD());
    logToDb.setDL_FILE_T(emisUtil.todayTimeS(false));
    logToDb.setCRE_USER(this.config.getMigration().getUserName());
    logToDb.setDL_FILE(fileName);
    for (int i = 0; i < path.length; i++) {
      sCurSNo = path[i];
      //2005/05/12 andy:加入如下一行代碼.如資料庫中有空的記錄(如S_NO為空)時不產生檔案.
      if(path[i] == null || "".equals(emisString.trim(path[i]))) continue;
      //copyTo(this.path + File.separator + path[i], fileName, true);
      //2005/04/30 andy 修改:最後一個參數取前端的設定值
      //2005/05/08 andy :path加上subdir

      logToDb.setDL_S_NO(sCurSNo);
       id_no=getStoreCashId_no(path[i]).split(",");


      // robert,2010/02/10 修改,先在 temp copy 一份,然後用  move 的

      //copyTo(this.path + File.separator + path[i] + File.separator + subdir, fileName, !clear);
      //System.out.println(fullName + " copy to " + fullName + "~");
      // 2013/06/18 Joe add 获取下传档案大小，以记录到Download_Log
      length = new File(fullName).length();
      if ("".equals(id_no[0])) {//按門市产生档案,
        emisUtil.copyTo(fullName + "~", fullName);
        //doCheck(fullName + "~");
        if (moveTo(this.path + File.separator + path[i] + File.separator + subdir, fullName + "~")) {
          logToDb.setDL_FILE_DIR(this.path + File.separator + path[i] + File.separator + subdir);
          logToDb.setDL_FILE_SIZE(length);
          logToDb.setDL_FILE_ROWS(String.valueOf(this.writeCount));
          writeLog(bStoreServer);
        }
      } else {  // 按机台号产生档案,
        for (int loop = 0; loop < id_no.length; loop++) {
          emisUtil.copyTo(fullName + "~", fullName);
          if (moveTo(this.path + File.separator + path[i] + File.separator + id_no[loop] + File.separator + subdir, fullName + "~")) {
            logToDb.setDL_FILE_DIR(this.path + File.separator + path[i] + File.separator + id_no[loop] + File.separator + subdir);
            logToDb.setDL_FILE_SIZE(length);
            logToDb.setDL_FILE_ROWS(String.valueOf(this.writeCount));
            logToDb.setDL_ID_NO(id_no[loop]);
            writeLog(false);
          }
        }
      }
    }
    if (reopen)
      open(null);
  }

  /*
  private void copyTo( String pathTarget,  String fileName,  boolean isAppend) throws Exception {
     File dir = new File(pathTarget);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    // 2009/07/03 add by Harry 臨時檔outFile 檔名后加入年月日時分秒
    if(new File(path + File.separator + fileName + sDateTime).length() == 0){
      //當源檔沒有資料時,不再產生空檔  update by andy 2006/01/24
      return;
    }
    FileOutputStream os = new FileOutputStream(pathTarget + File.separator + fileName, isAppend);
    try {
       BufferedOutputStream bos = new BufferedOutputStream(os);
      // 2009/07/03 add by Harry 臨時檔outFile 檔名后加入年月日時分秒
       FileInputStream is = new FileInputStream(path + File.separator + fileName + sDateTime);
      BufferedInputStream bis = null;
      try {
        byte[] buf = new byte[4096];
        int readed;
        bis = new BufferedInputStream(is);
        while ((readed = bis.read(buf)) != -1) {
          bos.write(buf, 0, readed);
        }
        bos.flush();
      } finally {
        if( bis != null ){
          bis.close();
          bis = null;
        }
        if( is != null ) {
          is.close();
          is = null;
        }
        if( bos != null ) {
          bos.close();
          bos = null;
        }
      }
    } finally {
      if( os != null ) {
        os.close();
        os = null;
      }
    }
  }
  */

  private boolean moveTo( String pathTarget,  String srcName) throws Exception {
     File dir = new File(pathTarget);
     if (!dir.exists()) {
       dir.mkdirs();
     }
     File fSrc = new File(srcName);
     if( !fSrc.exists() ||  fSrc.length() == 0) {
       return false;
     }

     if( !pathTarget.endsWith( File.separator )) {
    	 pathTarget += File.separator;
     }

     File fTarget = new File(pathTarget + fileName);
     //System.out.println("Move to " + pathTarget + File.separator + fileName);

     if( fTarget.exists()) {
    	 if( !fTarget.delete() ) {
    		 return false;
    	 }
     }
     return fSrc.renameTo(fTarget);

  }


  //增加依据机台切资料

  private String getStoreCashId_no(String s_no) throws Exception {
    String id_no = "";
    emisDb db = null;
    try {
      emisProp op = emisProp.getInstance(this.config.getContext());
      db = emisDb.getInstance(this.config.getContext());
      if (listCurIdNo != null) {
        listCurIdNo.clear();
      } else {
        listCurIdNo = new ArrayList();
      }
      try {
        if (posType == null || "".equals(posType)) {
          db.executeQuery("select ID_NO from Cash_id where S_NO='" + s_no + "' and (isnull(POS_TYPE,'') ='' or POS_TYPE = 'V')");
        } else {
          db.executeQuery("select ID_NO from Cash_id where S_NO='" + s_no + "' and charindex(case when isnull(POS_TYPE,'')='' then 'V' else POS_TYPE end,'" + posType + "') > 0");
        }
      } catch(SQLException sqle){
        sqle.printStackTrace();
        // 如果上面执行有错，则按旧的Sql查询
        db.executeQuery("select ID_NO from Cash_id where S_NO='" + s_no + "'");
      }
      while (db.next()) {
        listCurIdNo.add(db.getString("ID_NO"));
        if (op.get("EP_DOWNFORIDNO") != null && op.get("EP_DOWNFORIDNO").equalsIgnoreCase("Y")) {//由系统参数确认
          if ("".endsWith(id_no)) id_no = db.getString("ID_NO");
          else id_no = (id_no + "," + db.getString("ID_NO"));
        }
      }
      if ("Y".equalsIgnoreCase(op.get("EP_IS_STORESERVER"))) {  // 判断是否有门店小后台
        bStoreServer = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (db != null) db.close();
    }
    return id_no;
  }


  public boolean clearTemp() {
		if (outFile != null) {
			return outFile.delete();
		}
		return false;
	}

	/**
	 * wing 20100128 通過print,println方法轉換,提供直接輸出字, 減少輸出?亂碼機會 統一調用，並轉換
	 *
	 * @param lineContent
	 * @throws IOException
	 */
	protected void print(String lineContent) throws IOException {

        //wing 2010/0/206 通過print,println方法轉換,提供直接輸出字, 減少輸出?亂碼機會 統一調用，並不进行简繁轉換
        //[14309] 2010/02/06 wing 字符串效能调整，可以直接使用oWriter_.write(lineContent);
		//或者可以使用oWriter_.write(lineContent)输出,但会在输出的档案中有?码出现，输出小票的也会有?出现
		if (ccrOutFileCharSet.equals(emisUTF8StringUtil.TURNOUT_GBK)) {
			 oWriter_.write(emisUTF8StringUtil.complTosimpleNoMappingBig5(lineContent));
	    } else if (ccrOutFileCharSet.equals(emisUTF8StringUtil.TURNOUT_MS950)) {
			 oWriter_.write(emisUTF8StringUtil.complToMS950NoMappingGBK(lineContent));
	    } else {
			 oWriter_.write(lineContent);
		}
//	wing 20100128 通過print,println方法轉換,提供直接輸出字, 減少輸出?亂碼機會 統一調用，自动简繁轉換
//     if (ccrOutFileCharSet.equals(emisUTF8StringUtil.TURNOUT_GBK)) {
//		 oWriter_.write(emisUTF8StringUtil.complTosimple(lineContent)); } else
//		 if (ccrOutFileCharSet.equals(emisUTF8StringUtil.TURNOUT_MS950)) {
//		 oWriter_.write(emisUTF8StringUtil.complToMS950(lineContent)); } else {
//		 oWriter_.write(lineContent); }

		// 先不考慮繁簡互轉,但需要考慮超過MS950的字符，如中文難字不過濾轉換會有輸出?現象，應轉換為"  "輸出
		//oWriter_.write(emisUTF8StringUtil.complFilterNoUTF8CharsetToMS950(lineContent));

  }

	/**
	 * wing 20100128 輸出內容並換行
	 *
	 * @param lineContent
	 * @throws IOException
	 */
	protected void println(String lineContent) throws IOException {
		print(lineContent + "\n\r");
	}

  public void writeLog(boolean bStoreServer){
    emisMiLogToDb logToDb = this.config.getMiLogToDb();
    if(bStoreServer){    // 门店有小后台的情况，档案统一下到小后台，再分到各收银机台
      for(int i=0; i< listCurIdNo.size(); i++){
        logToDb.setDL_ID_NO((String)listCurIdNo.get(i));
        logToDb.insertProc();
      }
    } else {
      logToDb.insertProc();
    }
  }
  
}
