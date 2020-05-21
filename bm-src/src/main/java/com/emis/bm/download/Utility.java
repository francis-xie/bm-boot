/**
 * 工具类，放一些简单的方法。 
 */
package com.emis.bm.download;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;


public class Utility {
	public final static String USERAGENT = "EMIS for BigMonitor";
	
	public final static String TYPE_DOWNLOAD = "1";
	public final static String TYPE_MAINTAIN = "2";
	public final static String TYPE_UPGRADE = "3";
	// 异常代码
	public static int ERROR_CODE = 0;
	// 0 正常 
	public final static int ERROR_CODE_NORMAL = 0;
	// 1 其他异常
	public final static int ERROR_CODE_OTHER_EXCEPTION = 1;
	// 2 网络连接异常
	public final static int ERROR_CODE_CONNECT_TIMEOUT = 2;
	
	private Utility() {
		
	}
	/**
	 * 获取Web的URL
	 * @return
	 */
	/*public static String getWebRootURL() {
		return emisUtil.parseString(emisKeeper.getInstance().getEmisPropMap().get("SME_URL"));
	}*/
	/**
	 * 获取下载URL
	 * @param type
	 * @return
	 */
	public static String getDownloadURL(String type, String sWebRoot, String sSNo_, String sIdNo_, String sJSP) {
		/*String sWebRoot = getWebRootURL();
		String sSNo_ = emisUtil.parseString(NbUtils.getS_NO());
		String sIdNo_ = emisUtil.parseString(NbUtils.getID_NO());
		String sJSP = emisUtil.parseString(emisKeeper.getInstance()
				.getEmisPropMap().get("DOWNLOAD_JSP"),
				"jsp/spa/spa_download.jsp");*/
		StringBuffer sUrl = new StringBuffer();
		sUrl.append(sWebRoot);
		if(!sWebRoot.endsWith("/")){
			sUrl.append("/");
		}
		sUrl.append(sJSP);
		sUrl.append("?type=").append(type);
		sUrl.append("&s_no=").append(sSNo_);
		sUrl.append("&id_no=").append(sIdNo_);
		return sUrl.toString();
	}
	/**
	 * 获取下载URL
	 * @param type 通知类型：1 资料档(Download) 2 维护档(Maintain)  3 更新档(Upgrade)
	 * @param dataFile 下载文件
	 * @return 
	 */
	public static void notifyAfterDownload(String type, String dataFile, String sSNo_, String sIdNo_, String sJSP) {
		List<NameValuePair> oPm = new ArrayList<NameValuePair>();
		oPm.add(new BasicNameValuePair("type", type));
		oPm.add(new BasicNameValuePair("s_no", sSNo_));
		oPm.add(new BasicNameValuePair("id_no", sIdNo_));
		oPm.add(new BasicNameValuePair("target", dataFile));
		/*String sJSP = emisUtil.parseString(emisKeeper.getInstance()
				.getEmisPropMap().get("DOWN_NOTIFY_JSP"),
				"/jsp/spa/spa_download_after.jsp");*/
//		emisUtil.doPostUrlNoResponse(sJSP, oPm, 500);
		doPostUrlNoResponse(sJSP, oPm, 500);
	}
	/**
	 * 获取下载列表清单
	 * @param sDownUrl
	 * @return
	 */
	public static List<String> getDownloadList(String sDownUrl){
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		List<String> result = null;
		try {
			URL url = new URL(sDownUrl);			
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("Charsert", "UTF-8");
			conn.setRequestProperty("User-Agent", Utility.USERAGENT);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			if (conn.getResponseCode() == 200) {			
				// 定义BufferedReader输入流来读取URL的响应
				reader = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				String line = null;
				result = new ArrayList<String>();
				while ((line = reader.readLine()) != null) {
					if (!"".equals(line.trim())) {
						System.out.println(line);
						result.add(line);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(reader);
			Utility.close(conn);
		}
		return result;
	}
	/**
	 * 断点下传(默认5个并发线程)
	 * @param sUrl   下载目标地址
	 * @param savedPath  下载储存路径
	 * @param savedFile  下载储存文件
	 */
	public static SiteFileFetch download(String sUrl, String savedPath, String savedFile) {
		return download(sUrl, savedPath, savedFile, 5);
	}
	
	/**
	 * 断点下传
	 * @param sUrl   下载目标地址
	 * @param savedPath  下载储存路径
	 * @param savedFile  下载储存文件
	 * @param nThread  下载并发线程数
	 */
	public static SiteFileFetch download(String sUrl, String savedPath, String savedFile, int nThread) {
		try {						
			SiteInfoBean bean = new SiteInfoBean(sUrl, savedPath, savedFile, nThread);
			SiteFileFetch fileFetch = new SiteFileFetch(bean);
			fileFetch.start();
			return fileFetch;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/** 
	 * 上传(不支持断点续传)
	 * 
	 * @param sUrl   上传目标地址
	 * @param fileList  上传文件
	 */
	public static boolean upload(String sUrl, List<String> fileList) {
		return upload(sUrl, fileList, null);
	}
	/** 
	 * 上传(不支持断点续传)
	 * 
	 * @param sUrl   上传目标地址
	 * @param fileList  上传文件
	 */
	public static boolean upload(String sUrl, List<String> fileList, Map<String, Object> params) {
		if (sUrl == null || "".equals(sUrl.trim()) || fileList == null
				|| fileList.isEmpty()) {
			return false;
		}
			
		try {						
			emisHttpUpload client = new emisHttpUpload();
			client.setsURL(sUrl);
			if (params != null) {
				client.setPARAMS(params);
			}
			return client.upload(fileList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void sleep(int nSecond) {
		try {
			Thread.sleep(nSecond);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void log(String sMsg) {
		System.err.println(sMsg);
	}

	public static void log(int sMsg) {
		System.err.println(sMsg);
	}
	
    //---------------------------------Copy From Apache's IOUtil.java--------------------------------------
	
	/**
     * Closes a URLConnection.
     *
     * @param conn the connection to close.
     * @since 2.4
     */
    public static void close(final URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

    /**
     * Closes an <code>Reader</code> unconditionally.
     * <p>
     * Equivalent to {@link java.io.Reader#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   char[] data = new char[1024];
     *   Reader in = null;
     *   try {
     *       in = new FileReader("foo.txt");
     *       in.read(data);
     *       in.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(in);
     *   }
     * </pre>
     *
     * @param input  the Reader to close, may be null or already closed
     */
    public static void closeQuietly(final Reader input) {
        closeQuietly((Closeable)input);
    }

    /**
     * Closes an <code>Writer</code> unconditionally.
     * <p>
     * Equivalent to {@link java.io.Writer#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   Writer out = null;
     *   try {
     *       out = new StringWriter();
     *       out.write("Hello World");
     *       out.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(out);
     *   }
     * </pre>
     *
     * @param output  the Writer to close, may be null or already closed
     */
    public static void closeQuietly(final Writer output) {
        closeQuietly((Closeable)output);
    }

    /**
     * Closes an <code>InputStream</code> unconditionally.
     * <p>
     * Equivalent to {@link java.io.InputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   byte[] data = new byte[1024];
     *   InputStream in = null;
     *   try {
     *       in = new FileInputStream("foo.txt");
     *       in.read(data);
     *       in.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(in);
     *   }
     * </pre>
     *
     * @param input  the InputStream to close, may be null or already closed
     */
    public static void closeQuietly(final InputStream input) {
        closeQuietly((Closeable)input);
    }

    /**
     * Closes an <code>OutputStream</code> unconditionally.
     * <p>
     * Equivalent to {@link java.io.OutputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     * byte[] data = "Hello, World".getBytes();
     *
     * OutputStream out = null;
     * try {
     *     out = new FileOutputStream("foo.txt");
     *     out.write(data);
     *     out.close(); //close errors are handled
     * } catch (IOException e) {
     *     // error handling
     * } finally {
     *     IOUtils.closeQuietly(out);
     * }
     * </pre>
     *
     * @param output  the OutputStream to close, may be null or already closed
     */
    public static void closeQuietly(final OutputStream output) {
        closeQuietly((Closeable)output);
    }

    /**
     * Closes a <code>Closeable</code> unconditionally.
     * <p>
     * Equivalent to {@link java.io.Closeable#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   Closeable closeable = null;
     *   try {
     *       closeable = new FileReader("foo.txt");
     *       // process closeable
     *       closeable.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(closeable);
     *   }
     * </pre>
     *
     * @param closeable the object to close, may be null or already closed
     * @since 2.0
     */
    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    /**
     * Closes a <code>Socket</code> unconditionally.
     * <p>
     * Equivalent to {@link java.net.Socket#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   Socket socket = null;
     *   try {
     *       socket = new Socket("http://www.foo.com/", 80);
     *       // process socket
     *       socket.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(socket);
     *   }
     * </pre>
     *
     * @param sock the Socket to close, may be null or already closed
     * @since 2.0
     */
    public static void closeQuietly(final Socket sock){
        if (sock != null){
            try {
                sock.close();
            } catch (final IOException ioe) {
                // ignored
            }
        }
    }

    /**
     * Closes a <code>Selector</code> unconditionally.
     * <p>
     * Equivalent to {@link java.nio.channels.Selector#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   Selector selector = null;
     *   try {
     *       selector = Selector.open();
     *       // process socket
     *
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(selector);
     *   }
     * </pre>
     *
     * @param selector the Selector to close, may be null or already closed
     * @since 2.2
     */
    public static void closeQuietly(final Selector selector){
        if (selector != null){
            try {
              selector.close();
            } catch (final IOException ioe) {
                // ignored
            }
        }
    }

    /**
     * Closes a <code>ServerSocket</code> unconditionally.
     * <p>
     * Equivalent to {@link java.net.ServerSocket#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   ServerSocket socket = null;
     *   try {
     *       socket = new ServerSocket();
     *       // process socket
     *       socket.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(socket);
     *   }
     * </pre>
     *
     * @param sock the ServerSocket to close, may be null or already closed
     * @since 2.2
     */
    public static void closeQuietly(final ServerSocket sock){
        if (sock != null){
            try {
                sock.close();
            } catch (final IOException ioe) {
                // ignored
            }
        }
    }

	/**
	 * POST方式连线后台，不需要返回
	 */
	public static void doPostUrlNoResponse(String sUrl,
																				 List<NameValuePair> oPm, int iTimeOut) {
		if ("".equals(sUrl)) {
			return;
		}
		HttpClient _oHttpClient = new DefaultHttpClient();
		_oHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, iTimeOut);
		_oHttpClient.getParams().setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT, iTimeOut);
		HttpPost _oHttpRequest = new HttpPost(sUrl);
		try {
			// 查看是否需要参数
			if (oPm != null && oPm.size() > 0) {
				_oHttpRequest.setEntity(new UrlEncodedFormEntity(oPm, HTTP.UTF_8));
			}
			_oHttpRequest.getParams().setParameter( CoreConnectionPNames.SO_TIMEOUT, iTimeOut);
			_oHttpRequest.getParams().setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT, iTimeOut);
			_oHttpClient.execute(_oHttpRequest);
		} catch (Exception e) {
			// emisLogger.getLog(emisUtil.class).error(e);
		} finally {
			_oHttpClient.getConnectionManager().shutdown();
		}
	}

}