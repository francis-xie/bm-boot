/**
 * 负责整个文件的抓取，控制内部线程 (FileSplitterFetch 类 ) 
 * 
 */
package com.emis.bm.download;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

//import org.apache.log4j.Logger;
//import com.emis.nb.data.DataUtil;
//import com.emis.venus.util.log4j.emisLogger;

public class SiteFileFetch extends Thread {

//	private final Logger oLog_ = emisLogger.getLog(this.getName());
	/*
	 * 文件信息 Bean
	 */
	private SiteInfoBean siteInfoBean = null;
	/*
	 * 开始位置
	 */
	private long[] nStartPos;
	/*
	 * 结束位置
	 */
	private long[] nEndPos; 
	/*
	 * 子线程对象
	 */
	private FileSplitterFetch[] fileSplitterFetch;
	/*
	 * 文件长度
	 */
	private long nFileLength;
	/*
	 * 是否第一次取文件
	 */
	private boolean bFirst = true;
	/*
	 * 停止标志
	 */
	private boolean bStop = false;
	/*
	 * 下载完成状态标识
	 */
	private boolean bDownOver = false;
	/*
	 * 文件下载的临时信息
	 */
	private File tmpFile;
	/*
	 * 下载完成状态百分比
	 */
	public int state = 0; 
	/*
	 * 下载完成后回调接口
	 */
	private Callback downOverCallback = null;

	public Callback getDownOverCallback() {
		return downOverCallback;
	}

	public void setDownOverCallback(Callback downOverCallback) {
		this.downOverCallback = downOverCallback;
	}

	public SiteFileFetch(SiteInfoBean bean) throws IOException {
		siteInfoBean = bean;
		// 读取前次下载信息，实现断点续传
		tmpFile = new File(bean.getSFilePath() + File.separator
				+ bean.getSFileName() + ".download");
		if (tmpFile.exists()) {
			bFirst = false;
			read_nPos();
		} else {
			tmpFile.getParentFile().mkdirs();
			nStartPos = new long[bean.getNSplitter()];
			nEndPos = new long[bean.getNSplitter()];
		}
	}

	public void run() {
		// 获得文件长度
		// 分割文件
		// 实例 FileSplitterFetch
		// 启动 FileSplitterFetch 线程
		// 等待子线程返回		
		try {
			if (bFirst) {
				// 第一次下载，获得文件长度
				nFileLength = getFileSize();
				if (nFileLength == -1) {
//					oLog_.error("未知文件大小!");
					System.out.println("未知文件大小!");
				} else if (nFileLength == -2) {
//					oLog_.error("无法读取文件!");
					System.out.println("无法读取文件!");
				} else {
					// 依并发数分解各子线程下载开始范围
					for (int i = 0; i < nStartPos.length; i++) {
						nStartPos[i] = (long) (i * (nFileLength / nStartPos.length));
					}
					// 依并发数分解各子线程下载结束范围
					for (int i = 0; i < nEndPos.length - 1; i++) {
						nEndPos[i] = nStartPos[i + 1];
					}
					nEndPos[nEndPos.length - 1] = nFileLength;
				}
			}
			// 启动子线程
			fileSplitterFetch = new FileSplitterFetch[nStartPos.length];
			for (int i = 0; i < nStartPos.length; i++) {
				fileSplitterFetch[i] = new FileSplitterFetch(
						siteInfoBean.getSSiteURL(), siteInfoBean.getSFilePath(),
						siteInfoBean.getSFileName(), nStartPos[i], nEndPos[i], i);
				//oLog_.error("Thread " + i + " , nStartPos = " + nStartPos[i] + ", nEndPos = " + nEndPos[i]);
				// 启动线程开始下载
				fileSplitterFetch[i].start();
			}
			// 是否结束 while 循环
			boolean breakWhile = false;
			bDownOver = false;
			while (!bStop) {
				write_nPos();
				Utility.sleep(500);
				breakWhile = true;
				for (int i = 0; i < nStartPos.length; i++) {
					if (!fileSplitterFetch[i].bDownOver) {
						breakWhile = false;
						break;
					}
				}
				if (breakWhile)
					break;
			}
			write_nPos();
			bDownOver = true;
			if (downOverCallback != null) {
				downOverCallback.callback();
			}
//			oLog_.info(siteInfoBean.getSFileName() + ",文件下载结束！");
//			DataUtil.setInfoLogs(siteInfoBean.getSFileName() + ",文件下载结束！");
			System.out.println(siteInfoBean.getSFileName() + ",文件下载结束！");
		} catch (Exception e) {
//			oLog_.error(siteInfoBean.getSFileName() + ",文件下载失败:" + e.getMessage(), e);
//			DataUtil.setErrorLogs(siteInfoBean.getSFileName() + ",文件下载失败:" + e.getMessage());
			System.out.println(siteInfoBean.getSFileName() + ",文件下载失败:" + e.getMessage());
		}
	}
	/**
	 * 获得文件长度
	 * @return
	 */
	public long getFileSize() {
		int nFileLength = -1;
		HttpURLConnection httpConnection = null;
		try {
			URL url = new URL(siteInfoBean.getSSiteURL());			
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("User-Agent", Utility.USERAGENT);
			int responseCode = httpConnection.getResponseCode();
			if (responseCode >= 400) {
				processErrorCode(responseCode);
				return -2; // -2 represent access is error
			}
			String sHeader;
			for (int i = 1;; i++) {
				sHeader = httpConnection.getHeaderFieldKey(i);
				if (sHeader != null) {
					// 读取头信息获取返回的文件长度内容
					if (sHeader.equals("Content-Length")) {
						nFileLength = Integer.parseInt(httpConnection.getHeaderField(sHeader));
						break;
					}
				} else
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.close(httpConnection);
		}
		return nFileLength;
	}

	/**
	 * 保存下载信息（文件指针位置）
	 */
	private void write_nPos() {
		DataOutputStream output = null; 
		try {
			output = new DataOutputStream(new FileOutputStream(tmpFile));
			output.writeInt(nStartPos.length);
			long curr = 0, total = fileSplitterFetch[nStartPos.length - 1].nEndPos;
			for (int i = 0; i < nStartPos.length; i++) {
				output.writeLong(fileSplitterFetch[i].nStartPos);
				output.writeLong(fileSplitterFetch[i].nEndPos);
				// 20140404 Joe modify: 修正计算断点续传分段下载进度完成百分比错误
				// 分段结束-分段开始=分段未完成
				curr += fileSplitterFetch[i].nEndPos - fileSplitterFetch[i].nStartPos;
			}
			// 总量 - 分段未完成 = 分段已完成
			curr = total - curr;			
			// 分段已完成 * 100 / 总量 = 分段完成百分比
			this.state = Math.round(curr * 100 / total);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(output);
		}
	}

	/**
	 * 读取保存的下载信息（文件指针位置）
	 */
	private void read_nPos() {
		DataInputStream input = null;
		try {
			input = new DataInputStream(new FileInputStream(tmpFile));
			int nCount = input.readInt();
			nStartPos = new long[nCount];
			nEndPos = new long[nCount];
			for (int i = 0; i < nCount; i++) {
				nStartPos[i] = input.readLong();
				nEndPos[i] = input.readLong();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			Utility.closeQuietly(input);
		}
	}
	/**
	 * 检查是否下载完成，完成即清除续传记录档
	 */
	public boolean checkDoneClean() {
		DataInputStream input = null;
		boolean isDone = true;		
		try {			
			input = new DataInputStream(new FileInputStream(tmpFile));
			int nCount = input.readInt();
			for (int i = 0; i < nCount; i++) {
				if (input.readLong() < input.readLong()) {
					isDone = false;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(input);
			if (isDone) {
				tmpFile.delete();
			}
		}
		return isDone;
	}
	private void processErrorCode(int nErrorCode) {
//		oLog_.error("Error Code : " + nErrorCode);
		System.out.println("Error Code : " + nErrorCode);
	}
	/**
	 * 是否下载完成
	 * @return
	 */
	public boolean isDownOver(){
		return this.bDownOver;
	}

	/**
	 * 停止文件下载
	 */
	public void siteStop() {
		bStop = true;
		for (int i = 0; i < nStartPos.length; i++)
			fileSplitterFetch[i].splitterStop();
	}
}