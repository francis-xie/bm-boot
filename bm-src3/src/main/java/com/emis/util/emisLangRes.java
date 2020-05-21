package com.emis.util;

import com.emis.server.emisServer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Iterator;
import java.io.*;

import org.apache.commons.io.FilenameUtils;

/**
 * $Id: emisLangRes.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * @author: Joe.yao  2010/5/7  下午 03:19:58
 */
public class emisLangRes {
  /**
   * 是否初始化標記
   */
  private static boolean bInit = false;
  /**
   * 資料檔儲存路徑
   */
  private static String rootPath;
  /**
   * 所有資源檔緩存器
   */
  private static Hashtable I18N = new Hashtable();
  /**
   * 默認語系為英語
   */
  private static String defautLang = "EN";
  /**
   * 當前語系標識
   */
  private String language;
  /**
   * 登入User選定語系緩存器 Userid >> Lang
   */
  private static Hashtable userLangCache = new Hashtable();
  /**
   * 多語資源檔儲存相對路徑（相對于WebApp根目錄開始）
   */
  public static String ResourceSubPath = "/business/";

  public static emisLangRes getInstance(ServletContext oContext) throws Exception {
    emisLangRes lang = new emisLangRes();
    lang.initLangRes(oContext);
    lang.setLanguage(defautLang);

    return lang;
  }

  public static emisLangRes getInstance(HttpServletRequest request) throws Exception {
    emisLangRes lang = new emisLangRes();
    lang.initLangRes(request.getSession().getServletContext());
    String languageType = (String) request.getSession().getAttribute("languageType");
    if (languageType != null && !"".equals(languageType.trim()))
      lang.setLanguage(languageType);
    else
      lang.setLanguage(defautLang);

    return lang;
  }
  public static emisLangRes getInstance(String rootPath) throws Exception {
    emisLangRes lang = new emisLangRes();
    lang.initLangRes(rootPath);
    lang.setLanguage(defautLang);

    return lang;
  }

  /**
   * 記錄當前登入User選定語系
   * @param userid
   * @param lang
   */
  public static void setUserLang(String userid, String lang) {
    if (userid != null && !"".equals(userid.trim()) && lang != null && !"".equals(lang.trim())) {
      userLangCache.put(userid.toUpperCase(), lang.toUpperCase());
    }
  }

  /**
   * 取得指定登入User選定語系（無指定時返回DefalutLang）
   * @param userid
   * @return
   */
  public static String getUserLang(String userid) {
    if (userid == null || !userLangCache.containsKey(userid.toUpperCase()))
      return defautLang;
    else
      return (String) userLangCache.get(userid.toUpperCase());
  }

  /**
   * 刪除先前已記錄登入User選定語系
   * @param userid
   */
  public static void removeUserLang(String userid) {
    if (userid != null && !"".equals(userid.trim()))
      return;

    userLangCache.remove(userid.toUpperCase());
  }

  /**
   * 設定當前語系
   *
   * @param lang
   */
  public void setLanguage(String lang) {
    if (lang != null && !"".equals(lang.trim())) {
      this.language = lang.trim().toUpperCase();
    }
  }

  /**
   * 初始化
   *
   * @param oContext
   * @throws Exception
   */
  private void initLangRes(ServletContext oContext) throws Exception {
    if (!bInit) {
      rootPath = getLangResPath(oContext);
      if (rootPath != null && !"".equals(rootPath.trim())) {
        this.loadProerties();
        bInit = true;
      }
    }
  }

  /**
   * 初始化
   *
   * @param path
   * @throws Exception
   */
  private void initLangRes(String path) throws Exception {
    if (!bInit && path != null && !"".equals(path.trim())) {
      rootPath = path;
      this.loadProerties();
      bInit = true;
    }
  }

  /**
   * 加載所有資源檔案內容為 Properties 并緩存到 I18N（Hashtable）
   *
   * @throws Exception
   */
  private void loadProerties() throws Exception {
    File file = new File(rootPath);
    if (file.exists() && file.canRead()) {
      File[] properties = file.listFiles();
      for (int i = 0; i < properties.length; i++) {
        if(properties[i].getName().endsWith(".properties"))
          loadSingle(properties[i]);
      }
      bInit = true;
    }
  }

  /**
   * 动态加载未初始化的新资源档 Properties 并緩存到 I18N（Hashtable）
   * @param fileName
   * @throws Exception
   */
  private void addProerties(String fileName) throws Exception {
    File file = new File(rootPath);
    if (file.exists() && file.canRead()) {
      File[] properties = file.listFiles();
      for (int i = 0; i < properties.length; i++) {
        if ((fileName + ".properties").equalsIgnoreCase(properties[i].getName()))
          loadSingle(properties[i]);
      }
      bInit = true;
    }
  }

  public void loadSingle(File properties) throws Exception {
    String key = FilenameUtils.getBaseName(properties.getName()).toUpperCase();
    emisProperties oProps = getLangProperties(properties.getAbsolutePath());
    oProps.put("loadTime__", "" + properties.lastModified());
    oProps.put("AbsolutePath__", "" + properties.getAbsolutePath());
    I18N.put(key, oProps);
  }

  /**
   * 加載單個資源檔案內容為 Properties
   *
   * @param sFileName
   * @return
   * @throws Exception
   */
  private emisProperties getLangProperties(String sFileName) throws Exception {
    FileInputStream in = null;
    try {
      in = new FileInputStream(sFileName);
      emisProperties _oProps = new emisProperties();
      _oProps.load(in, "UTF-8");

      return _oProps;
    } catch (FileNotFoundException e) {
      System.err.println("emisLangRes: File not exists " + e.getMessage());
    } catch (IOException e) {
      System.err.println("emisLangRes: IO error " + e.getMessage());
    } finally {
      if (in != null) in.close();
    }
    return null;
  }

  /**
   * 重新加載所有資源檔案
   *
   * @throws Exception
   */
  public void reload() throws Exception {
    this.loadProerties();
  }

  /**
   * 重新加載單個資源檔案
   *
   * @param fileName
   * @throws Exception
   */
  public void reload(String fileName) throws Exception {
    String key = FilenameUtils.getBaseName(fileName).toUpperCase();
    emisProperties oProps = getLangProperties(rootPath + fileName + ".properties");
    I18N.put(key, oProps);
  }

  /**
   * 取回資源檔案存放路徑
   *
   * @param oContext
   * @return
   */
  private String getLangResPath(ServletContext oContext) {
    String serverName = "";
    try {
      emisServer _oServer = (emisServer) oContext.getAttribute(emisServer.STR_EMIS_SERVER);
      if (_oServer == null) throw new Exception("server 不存在");
      Properties _oProp = _oServer.getProperties();
      serverName = _oProp.getProperty("documentroot") + ResourceSubPath;
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    return serverName;
  }

  /**
   * 設定Default Lang，一般不需要設定，為日后系統設定可動態設定默認語系準備
   *
   * @param defaultLang
   */
  public void setDefaultLang(String defaultLang) {
    if (defaultLang != null && !"".equals(defaultLang.trim())) {
      defautLang = defaultLang.toUpperCase();
    } else {
      defautLang = "EN";
    }
    this.setLanguage(defautLang);
  }

  /**
   * 取得資源訊息
   *
   * @param bundle 資源檔名
   * @param key    資源標識
   * @return
   */
  public String getMessage(String bundle, String key) {
    if (bundle == null || "".equals(bundle.trim()) || key == null || "".equals(key.trim()))
      return "";
    try {
      checkExpired(bundle);
    } catch (Exception e) {
      e.printStackTrace();
    }
    emisProperties properties = (emisProperties) I18N.get(bundle.toUpperCase() + "-" + this.language);
    String message = properties == null ? null : (String) properties.get(key);
    if (message == null || "".equals(message.trim())) { // 取Default Lang
      properties = (emisProperties) I18N.get(bundle.toUpperCase() + "-" + defautLang);
      message = properties == null ? null : (String) properties.get(key);
    }

    return (message == null || "".equals(message.trim())) ? "" : message;
  }

  private void checkExpired(String bundle) throws Exception {
    emisProperties properties = (emisProperties) I18N.get(bundle.toUpperCase() + "-" + this.language);
    if(properties == null) {
      // 当缓存中取不到资源档时，重新单独加载当前bundle+language的档案
      addProerties(bundle + "-" + this.language);
      return;
    }

    long loadTime_ = Long.parseLong(properties.getProperty("loadTime__"));
    String sAbsolutePath_ = properties.getProperty("AbsolutePath__");
    File oFile_ = new File(sAbsolutePath_);
    if (oFile_.lastModified() > loadTime_) {
      loadSingle(oFile_);
    }
  }
  public emisProperties getProperties(String bundle) {
    if (bundle == null || "".equals(bundle.trim()))
      return null;

    return (emisProperties) I18N.get(bundle + "-" + this.language);
  }

  public String getProperties2JSON(String bundle) {
    if (bundle == null || "".equals(bundle.trim()))
      return "{}";

    emisProperties properties = (emisProperties) I18N.get(bundle.toUpperCase() + "-" + this.language);
    if (properties == null) return "{}";

    int max = properties.size() - 1;
    StringBuffer buf = new StringBuffer();
    Iterator it = properties.keySet().iterator();
    buf.append("{");
    for (int i = 0; i <= max; i++) {
      String key = (String) it.next();
      if (key == null && "".equals(key.trim()))
        continue;

      buf.append("\"" + key + "\": \"" + properties.get(key) + "\"");

      if (i < max)
        buf.append(", ");
    }
    buf.append("}");
    return buf.toString();
  }

  public static void main(String[] args) throws Exception {
    /*
    // Type for Web
    emisServletContext servlet = new emisServletContext();
    emisServerFactory.createServer(servlet, "F:\\SME-UTF8-std\\wwwroot\\smepos-v3.0", "F:\\SME-UTF8-std\\resin\\epos.cfg", true);

    emisLangRes lang = emisLangRes.getInstance(servlet);
    */
    // Type for Java
    emisLangRes lang = emisLangRes.getInstance("F:\\SME-UTF8-std\\wwwroot\\smepos-v3.0\\business");

    System.out.println(lang.getMessage("index", "TITLE"));
    lang.setLanguage("zh_cn");
    System.out.println(lang.getMessage("index", "TITLE"));
    lang.setLanguage("zh_tw");
    System.out.println(lang.getMessage("index", "TITLE"));
    lang.setDefaultLang("zh_cn");
    System.out.println(lang.getMessage("index", "TITLE"));

    System.out.println(lang.getProperties2JSON("index"));
  }
}
