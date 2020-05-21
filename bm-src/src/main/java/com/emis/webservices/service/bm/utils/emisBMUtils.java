package com.emis.webservices.service.bm.utils;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

/**
 * 大屏点餐 - 工具类
 * $Id$
 */
public class emisBMUtils {

  /**
   * private的constructor無法被new
   */
  private emisBMUtils() {
  }

  /**
   * 向指定URL发送GET方法的请求
   *
   * @param url   发送请求的URL
   * @param param 请求参数，请求参数应该是name1=value1&name2=value2的形式。
   * @return URL所代表远程资源的响应
   */
  public static String sendGet(String url, String param) {
    StringBuffer result = new StringBuffer("");
    BufferedReader in = null;
    try {
      String urlNameString = url + "?" + param;
      URL realUrl = new URL(urlNameString);
      // 打开和URL之间的连接
      URLConnection connection = realUrl.openConnection();
      // 设置通用的请求属性
      connection.setReadTimeout(60000);
      connection.setConnectTimeout(60000);
      connection.setRequestProperty("accept", "*/*");
      connection.setRequestProperty("connection", "Keep-Alive");
      connection.setRequestProperty("Charset", "UTF-8");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
      connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
      // 建立实际的连接
      connection.connect();
      // 定义 BufferedReader输入流来读取URL的响应
      in = new BufferedReader(new InputStreamReader(
          connection.getInputStream(), "UTF-8"));
      String line;
      while ((line = in.readLine()) != null) {
        result.append(line);
      }
    } catch (Exception e) {
      System.out.println("发送GET请求出现异常！" + e);
      e.printStackTrace();
    }
    // 使用finally块来关闭输入流
    finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    }
    return result.toString();
  }

  /**
   * 向指定URL发送POST方法的请求
   *
   * @param url   发送请求的URL
   * @param param 请求参数，请求参数应该是name1=value1&name2=value2的形式。
   * @return URL所代表远程资源的响应
   */
  public static String sendPost(String url, String param) {
    OutputStream out = null;
    BufferedReader in = null;
    StringBuffer result = new StringBuffer();
    try {
      URL realUrl = new URL(url);
      // 打开和URL之间的连接
      HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();

      // 设置通用的请求属性
      connection.setConnectTimeout(60000);
      connection.setReadTimeout(60000);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("accept", "*/*");
      connection.setRequestProperty("connection", "Keep-Alive");
      connection.setRequestProperty("Charset", "UTF-8");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
      connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
      // Post 请求不能使用缓存
      connection.setUseCaches(false);
      // 发送POST请求必须设置如下两行
      connection.setDoOutput(true);
      connection.setDoInput(true);
      // 获取URLConnection对象对应的输出流
      out = connection.getOutputStream();
      // 发送请求参数
      out.write(param.getBytes("UTF-8"));
      // flush输出流的缓冲
      out.flush();
      // 定义BufferedReader输入流来读取URL的响应
      in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
      String line;
      while ((line = in.readLine()) != null) {
        result.append(line + "\n");
      }
    } catch (Exception e) {
      System.out.println("发送POST请求出现异常！" + e);
      e.printStackTrace();
    }
    // 使用finally块来关闭输出流、输入流
    finally {
      try {
        if (out != null) {
          out.close();
        }
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return result.toString();
  }

  /**
   * 获取缩图图片名
   *
   * @param sSource 原图图片名
   * @param sExt    缩图扩展名
   * @return 缩图图片名
   */
  public static String getImgName(String sSource, String sExt) {
    if ("".equals(sExt)) return sSource;
    String sTarget;
    int var4 = sSource.lastIndexOf(46);
    if (var4 != -1) {
      sTarget = sSource.substring(0, var4);
      sTarget = sTarget + sExt;
      sTarget = sTarget + sSource.substring(var4);
    } else {
      sTarget = sSource + sExt;
    }
    return sTarget;
  }

  /**
   * 格式化双引号
   *
   * @param src 原字符串
   * @return
   */
  public static String escapeQuot(String src) {
    return src == null ? "" : src.replaceAll("\"", "\\\\\"");
  }

  /**
   * 格式化回车换行符
   *
   * @param src 原字符串
   * @return
   */
  public static String escapeLineBreak(String src) {
    return src == null ? "" : src.replaceAll("(\r\n|\n\r|\n|\r)", "\\\\n");
  }

  /**
   * 将回车、换行符修改为html的<br/>
   *
   * @param src 原字符串
   * @return
   */
  public static String escapeLineBreak2html(String src) {
    return src == null ? "" : src.replaceAll("(\r\n|\n\r|\n|\r)", "<br/>");
  }

  /**
   * 格式化json字符串
   *
   * @param src 原字符串
   * @return
   */
  public static String escapeJson(String src) {
    return src == null ? "" : src.replaceAll("\\\\", "\\\\\\\\").replaceAll("(\r\n|\n\r|\n|\r)", "\\\\n").replaceAll("\"", "\\\\\"");
  }

  /**
   * 格式化json字符串并将回车、换行转换成<br/>
   *
   * @param src 原字符串
   * @return
   */
  public static String escapeJson2html(String src) {
    return src == null ? "" : src.replaceAll("\\\\", "\\\\\\\\").replaceAll("(\r\n|\n\r|\n|\r)", "<br/>").replaceAll("\"", "\\\\\"");
  }

  /**
   * 去掉小数的0
   *
   * @param number 原数值
   * @return 处理后的数值
   */
  public static String getPrettyNumber(String number) {
    String sReturn;
    try {
      double tmp = Double.parseDouble(number);
      if (tmp == 0) {
        return "0";
      }
      sReturn = BigDecimal.valueOf(tmp).stripTrailingZeros().toPlainString();
    } catch (Exception ex) {
      sReturn = number;
    }
    return sReturn;
  }

  /**
   * 格式化数字（小数点处理）
   *
   * @param fNum  原数据
   * @param fType 格式化类型 (1: #,##0.####; 2: #,##0.00)
   * @return 格式化后数据
   */
  public static String formatNumber(double fNum, String fType) {
    DecimalFormat dfQty = null;
    if ("2".equals(fType)) {
      dfQty = new DecimalFormat("###0.00");
    } else {
      dfQty = new DecimalFormat("###0.####");
    }
    return dfQty.format(fNum);
  }

}