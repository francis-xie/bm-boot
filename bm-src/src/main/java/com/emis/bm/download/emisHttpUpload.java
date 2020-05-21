package com.emis.bm.download;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class emisHttpUpload {
	/**
	 * 上传的URL
	 */
	private String sURL;
	/**
	 * 上传编码格式
	 */
	private String sChartset = "UTF-8";
	/**
	 * 上传参数集合
	 */
	private Map<String, Object> params;
	
	public emisHttpUpload() {
		
	}

	public emisHttpUpload(String sURL) {
		this.sURL = sURL;
	}
	/**
	 * 获取上传URL
	 * @return
	 */
	public String getsURL() {
		return sURL;
	}
	/**
	 * 设置上传URL
	 * @param sURL
	 */
	public void setsURL(String sURL) {
		this.sURL = sURL;
	}
	/**
	 * 获取上传编码格式
	 * @return
	 */
	public String getChartset() {
		return sChartset;
	}
	/**
	 * 设置上传编辑模式
	 * @param sChartset
	 */
	public void setChartset(String sChartset) {
		this.sChartset = sChartset;
	}
	/**
	 * 获取上传编码格式
	 * @return
	 */
	public Map<String, Object> getPARAMS() {
		return params;
	}
	
	/**
	 * 设置上传编辑模式
	 * @param params
	 */
	public void setPARAMS(Map<String, Object> params) {
		this.params = params;
	}
	/**
	 * 上传文件
	 * @param fileList 文件集合
	 */
	public boolean upload(List<String> fileList) {
		HttpURLConnection conn = null;
		OutputStream out = null;
		BufferedReader reader = null;
		try {
			String boundary = "---------------------------" + System.currentTimeMillis();
			conn = getHttpConnection(boundary);			
			out = new DataOutputStream(conn.getOutputStream());
			// 上传参数
			sendParamaters(out, boundary);
			int leng = fileList.size();
			String fname;
			File file;
			for (int i = 0; i < leng; i++) {
				fname = fileList.get(i);
				file = new File(fname);
				// 上传文件
				sendFile(boundary, out, i, file);
			}
			// 定义最后数据分隔线
			byte[] end_data = ("\r\n--" + boundary + "--\r\n").getBytes();
			out.write(end_data);

			out.flush();
			out.close();
			
			// 定义BufferedReader输入流来读取URL的响应
			reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.indexOf("***OK***") >=0){
					return true;
				}
			}
		} catch (Exception e) {
			System.out.println("发送POST请求出现异常！" + e);
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(out);
			Utility.closeQuietly(reader);
			Utility.close(conn);
		}
		return false;
	}
	/**
	 * 读取文件内容进行发送
	 * @param boundary  分隔符
	 * @param out 上传流对象
	 * @param i 上传文件下标
	 * @param file 上传的文件
	 * @throws java.io.IOException
	 * @throws java.io.FileNotFoundException
	 */
	private void sendFile(String boundary, OutputStream out, int i, File file)
			throws IOException, FileNotFoundException {
		DataInputStream dis = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("--").append(boundary).append("\r\n");
			sb.append("Content-Disposition: form-data;name=\"file").append(i)
					.append("\";filename=\"").append(file.getName())
					.append("\"\r\n");
			sb.append("Content-Type:application/octet-stream").append(
					"\r\n\r\n");
			byte[] data = sb.toString().getBytes();
			out.write(data);
			dis = new DataInputStream(new FileInputStream(file));
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = dis.read(bufferOut)) != -1) {
				out.write(bufferOut, 0, bytes);
			}
			out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
		} finally {
			Utility.closeQuietly(dis);
		}
	}
	/**
	 * 获取连接对象
	 * @param boundary 分隔符
	 * @return
	 * @throws java.net.MalformedURLException
	 * @throws java.io.IOException
	 * @throws java.net.ProtocolException
	 */
	public HttpURLConnection getHttpConnection(String boundary)
			throws MalformedURLException, IOException, ProtocolException {		
		URL u = new URL(this.sURL);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("Charsert", this.getChartset());
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		// 指定流的大小，当内容达到这个值的时候就把流输出
		conn.setChunkedStreamingMode(10240);
		return conn;
	}
	/**
	 * 添加参数（即form表单属性）
	 * @param out 上传输出流
	 * @param boundary 分隔符
	 * @throws java.io.IOException
	 */
	private void sendParamaters(OutputStream out, String boundary) throws IOException {		
		if(params == null || params.isEmpty())
			return;
		
		StringBuffer sb = new StringBuffer();
		Iterator<String> keys = params.keySet().iterator();
		String key;
		while(keys.hasNext()){
			key = keys.next();
			sb.append("--").append(boundary).append("\r\n");
			sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"");
			sb.append("\r\n\r\n");
			sb.append(params.get(key));
			sb.append("\r\n"); // 多个参数时，二个之间加入这个
		}
		out.write(sb.toString().getBytes());
	}
}
