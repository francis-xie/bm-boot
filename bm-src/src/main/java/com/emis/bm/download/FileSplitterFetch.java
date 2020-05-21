/**
 * 负责部分文件的抓取。
 */
package com.emis.bm.download;

import java.io.*;
import java.net.*;

//import org.apache.log4j.Logger;
//import com.emis.venus.util.log4j.emisLogger;

public class FileSplitterFetch extends Thread {
//	private final Logger oLog_ = emisLogger.getLog(this.getName());
	/*
	 * 下载网址
	 */
	String sURL;
	/*
	 * 下载开始指针
	 */
	long nStartPos;
	/*
	 * 下载结束指针
	 */
	long nEndPos; 
	/*
	 * 线程ID
	 */
	int nThreadID;
	/*
	 * 下载完成状态标识
	 */
	boolean bDownOver = false;
	/*
	 * 停止标识(Stop identical)
	 */
	boolean bStop = false;
	/*
	 * 文件存储接口
	 */
	FileAccess fileAccess = null;
	/**
	 * 文件分解下载线程
	 * @param sURL 下载网址
	 * @param sName 下载存储目标
	 * @param nStart 下载开始指针
	 * @param nEnd 下载结束指针 
	 * @param id 分解线程ID
	 * @throws java.io.IOException
	 */
	public FileSplitterFetch(String sURL, String sPath, String sName,
			long nStart, long nEnd, int id) throws IOException {
		this.sURL = sURL;
		this.nStartPos = nStart;
		this.nEndPos = nEnd;
		nThreadID = id;
		fileAccess = new FileAccess(sPath + File.separator + sName, nStartPos);
	}

	public void run() {
		// 重复下载时直接结束
		if (nStartPos >= nEndPos) {
			bDownOver = true;
			splitterStop();
		} else {
			HttpURLConnection httpConnection = null;
			InputStream input = null;
			while (nStartPos < nEndPos && !bStop) {			
				try {
					URL url = new URL(sURL);
					httpConnection = (HttpURLConnection) url.openConnection();
					// 设置 User-Agent 
					httpConnection.setRequestProperty("User-Agent", Utility.USERAGENT);
					// 设置断点续传的开始位置 **Resin不支持加end pos,会导致下载不全,奇怪！！！！**
					String sProperty = "bytes=" + nStartPos + "-";// + nEndPos; 
					httpConnection.setRequestProperty("RANGE", sProperty);
					httpConnection.setReadTimeout(20*1000);
					//oLog_.debug(sProperty);
					input = httpConnection.getInputStream();
					// 打印调试回应的头信息
					//logResponseHead(httpConnection);
					byte[] b = new byte[1024];
					int nRead;
					while ((nRead = input.read(b, 0, 1024)) > 0 && nStartPos < nEndPos && !bStop) {
						nStartPos += fileAccess.write(b, 0, nRead);
						//if(nThreadID == 1) Utility.log("nStartPos = " + nStartPos + ", nEndPos = " + nEndPos);
					}
					//oLog_.debug("Thread " + nThreadID + " is over!");
					bDownOver = true;
				} catch (SocketTimeoutException ste) {
//					oLog_.error(ste.getMessage(), ste);
					System.out.println(ste.getMessage());
					Utility.ERROR_CODE = Utility.ERROR_CODE_CONNECT_TIMEOUT;
				} catch (Exception e) {
//					oLog_.error(e.getMessage(), e);
					System.out.println(e.getMessage());
					Utility.ERROR_CODE = Utility.ERROR_CODE_OTHER_EXCEPTION;
				} finally {
					splitterStop();
					Utility.closeQuietly(input);
					Utility.close(httpConnection);
				}
			}
		}
	}

	/**
	 * 打印回应的头信息
	 * @param con
	 */
	public void logResponseHead(HttpURLConnection con) {
//		oLog_.debug("******************" + nThreadID + " Log Response Head Start ****************** ");
		System.out.println("******************" + nThreadID + " Log Response Head Start ****************** ");
		for (int i = 1;; i++) {
			String header = con.getHeaderFieldKey(i);
			if (header != null)
//				oLog_.debug(header + " : " + con.getHeaderField(header));
				System.out.println(header + " : " + con.getHeaderField(header));
			else
				break;
		}
//		oLog_.debug("------------------" + nThreadID + " Log Response Head End ------------------ ");
		System.out.println("------------------" + nThreadID + " Log Response Head End ------------------ ");
	}

	public void splitterStop() {
		bStop = true;
		fileAccess.close();
	}
}