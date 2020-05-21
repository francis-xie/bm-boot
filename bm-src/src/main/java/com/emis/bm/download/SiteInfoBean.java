/**
 * 要抓取的文件的信息，如文件保存的目录，名字，抓取文件的 URL 等。  
 */
package com.emis.bm.download;

public class SiteInfoBean {
	/*
	 * 下载网址
	 */
	private String sSiteURL;
	/*
	 * 下载储存路径
	 */
	private String sFilePath;
	/*
	 * 下载储存文件名
	 */
	private String sFileName;
	/*
	 * 文件下载并发线程数(Count of Splited Downloading File)
	 */
	private int nSplitter;

	public SiteInfoBean() {
		// 文件下载并发线程数 默认为5
		this("", "", "", 5);
	}

	/**
	 * 下载信息实体
	 * @param sURL  下载网址
	 * @param sPath 下载储存路径
	 * @param sName 下载储存文件名
	 * @param nSpiltter 下载文件分解线程数
	 */
	public SiteInfoBean(String sURL, String sPath, String sName, int nSpiltter) {
		sSiteURL = sURL;
		sFilePath = sPath;
		sFileName = sName;
		this.nSplitter = nSpiltter;
	}
	/**
	 * 获取下载网址
	 * @return
	 */
	public String getSSiteURL() {
		return sSiteURL;
	}

	/**
	 * 设置下载网址
	 * @param value
	 */
	public void setSSiteURL(String value) {
		sSiteURL = value;
	}
	/**
	 * 获取下载储存路径
	 * @return
	 */
	public String getSFilePath() {
		return sFilePath;
	}
	/**
	 * 设置下载储存路径
	 * @param value
	 */
	public void setSFilePath(String value) {
		sFilePath = value;
	}
	/**
	 * 获取下载储存文件名
	 * @return
	 */
	public String getSFileName() {
		return sFileName;
	}
	/**
	 * 设置下载储存文件名
	 * @param value
	 */
	public void setSFileName(String value) {
		sFileName = value;
	}
	/**
	 * 获取文件下载并发线程数
	 * @return
	 */
	public int getNSplitter() {
		return nSplitter;
	}
	/**
	 * 设置文件下载并发线程数
	 * @param nCount
	 */
	public void setNSplitter(int nCount) {
		nSplitter = nCount;
	}
}