/**
 * 负责文件的存储。
 */
package com.emis.bm.download;

import java.io.*;

public class FileAccess implements Serializable {
	private static final long serialVersionUID = 1L;
	private RandomAccessFile oSavedFile;

	public FileAccess() throws IOException {
		this("", 0);
	}

	public FileAccess(String sName, long nPos) throws IOException {	
		oSavedFile = new RandomAccessFile(sName, "rw");		
		oSavedFile.seek(nPos);
	}

	/**
	 * 写入文件
	 * 
	 * @param b  写入内容
	 * @param nStart 写入起始位置
	 * @param nLen 写入长度
	 * @return
	 */
	public synchronized int write(byte[] b, int nStart, int nLen) {
		int n = -1;
		try {
			oSavedFile.write(b, nStart, nLen);
			n = nLen;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return n;
	}

	/**
	 * 读出文件
	 * 
	 * @param b  读出内容
	 * @param nStart 读出起始位置
	 * @param nLen 读出长度
	 * @return
	 */
	public synchronized int read(byte[] b, int nStart, int nLen) {
		int n = -1;
		try {
			oSavedFile.read(b, nStart, nLen);
			n = nLen;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return n;
	}

	/**
	 * 关闭文件流
	 * 
	 * @throws java.io.IOException
	 */
	public void close() {
		Utility.closeQuietly(oSavedFile);
	}
}
