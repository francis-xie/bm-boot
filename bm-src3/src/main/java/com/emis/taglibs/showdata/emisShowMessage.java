package com.emis.taglibs.showdata;

import com.emis.util.emisLangRes;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Joe.yao  2010/5/8  上午 10:24:01
 */
public class emisShowMessage extends TagSupport {
  /**
   * 資源檔
   */
  private String bundle;
  /**
   * 資源標識
   */
  private String key;
  /**
   * 使用語系
   */
  private String local;
  /**
   * 說明
   */
  private String description;

  public int doStartTag() {
    try {
      JspWriter out = pageContext.getOut();
      ServletContext oContext = pageContext.getServletContext();
      getMessage(oContext, out);
    } catch (Exception e) {
      System.err.println("emisShowMessage.doStartTag: " + e.getMessage());
    }
    return (SKIP_BODY);
  }

  /**
   * 取得指定標識的訊息
   * @param oContext
   * @param out
   * @throws Exception
   */
  private void getMessage(ServletContext oContext, JspWriter out) throws Exception {
    try {
      // 實例化資源總管
      emisLangRes lang = emisLangRes.getInstance(oContext);
      // 優先Tag中指定語系讀取資源
      if (this.local != null && !"".equals(this.local)) {
        lang.setLanguage(this.local.toUpperCase());
      } else {
        // 次之優先 Session 中記錄之語系讀取資源
        String languageType = (String) pageContext.getSession().getAttribute("languageType");
        if (languageType != null && !"".equals(languageType.trim())) {
          lang.setLanguage(languageType.trim());
        } else {
          // 依Client端默認語系讀取資源
          Locale userLocale = pageContext.getRequest().getLocale();
          lang.setLanguage(userLocale.toString());
        }
      }
      
      // 輸出資源訊息
      out.print(lang.getMessage(this.getBundle(), this.getKey()));
    } catch (Exception e) {
      System.err.println("emisShowMessage.getMessage: " + e.getMessage());
    }
  }

  public String getBundle() {
    return bundle;
  }

  public void setBundle(String bundle) {
    this.bundle = bundle;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getLocal() {
    return local;
  }

  public void setLocal(String local) {
    this.local = local;
  }
  
   public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
