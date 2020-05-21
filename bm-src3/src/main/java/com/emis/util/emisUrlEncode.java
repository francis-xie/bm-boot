package com.emis.util;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2009-3-17
 * Time: 14:41:53
 * To change this template use File | Settings | File Templates.
 */
public class emisUrlEncode {

  public static String encode(String url) {
    return new sun.misc.BASE64Encoder().encode(url.getBytes());
  }

  public static String decode(String url) throws IOException {
    return new String(new sun.misc.BASE64Decoder().decodeBuffer(url), "UTF8");
  }

  public static Map<String, String> decodeEmisLonginUrl(String url) throws IOException {
    StringBuilder builder = new StringBuilder(url);
    String nextpage = builder.substring(url.indexOf('[')+1,url.indexOf(']'));    
    url = builder.delete(builder.indexOf("NEXTPAGE=[")-1,url.indexOf(']')+1).toString();
    Map<String, String> map = new HashMap<String, String>();
    String[] kv_;

    for (String kv : url.split("&")) {
      int idx = kv.indexOf("=");
      if (idx > 0) {
        map.put(kv.substring(0, idx), kv.substring(idx+1, kv.length()));
      }
    }
    map.put("NEXTPAGE",nextpage);
    return map;
  }

  public static void main(String[] args) throws IOException {
    String s = "SUQ9Uk9PVCZQQVNTV0Q9dHVyYm8mU19OTz0mTkVYVFBBR0U9W2pzcC9lbWlzX1ByZVByaW50LmpzcD9hY3Q9cnB0MTEmdXNlcj1ST09UJnRpdGxlPTVBJnByZVByaW50TmFtZT10ZXh0XQ==";

    Map<String, String> map = decodeEmisLonginUrl(s);

    for(Map.Entry<String,String> entry : map.entrySet())
      System.out.println(entry.getKey() + " = " + entry.getValue());
  }
}
