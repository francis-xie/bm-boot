package com.emis.util;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/*
*Track+[14714] dana.gao 2010/04/29 如果是三點按鈕,替換掉%01%避免url被截斷,後臺再替換回來.
*當URL中包含%01這個字符時,用showModalDialog打開,包含%01以後的所有內容就被截斷
*導致很多參數無法正常傳過去.
*目前多發生在3點按鈕中,手動輸入%01,先在前端js中把%01%或%2501%25替換為@01@,然後在此處替換回%01%
* */
public class emisReplaceURIParam extends HttpServletRequestWrapper {
  private Map params;
  private HttpServletRequest request_;
  private Map filterMap = new HashMap();

  public emisReplaceURIParam(HttpServletRequest request) {

    super(request);

    request_ = request;

    this.params = request_.getParameterMap();

  }

  public String getQueryString(){

     return filter(request_.getQueryString());

  }

  public Map getParameterMap() {

    Iterator it = params.entrySet().iterator();
    
    while (it.hasNext()) {

      Map.Entry pairs = (Map.Entry) it.next();

      filterMap.put(pairs.getKey(), getParameterValues(pairs.getKey().toString()));

    }

    return filterMap;
  }

  public String[] getParameterValues(String name) {
    Object v = params.get(name);
    if (v == null) {
      return null;
    } else if (v instanceof String[]) {
      String[] a = (String[]) v;
      String[] b = new String[]{};
      for (int i = 0; i < a.length; i++) {
        String c = filter(a[i].toString());
        b[i] = c;
      }
      return b;
    } else if (v instanceof String) {
      String[] a = (String[]) v;
      String b = filter(a[0]);
      return new String[]{(String) b};
    } else {
      return new String[]{filter(v.toString())};
    }
  }

  public String getParameter(String name) {

    Object v = params.get(name);
    if (v == null) {
      return null;
    } else if (v instanceof String[]) {
      String[] strArr = (String[]) v;
      if (strArr.length > 0) {
        return filter(strArr[0]);
      } else {
        return null;
      }
    } else if (v instanceof String) {
      return filter((String) v);
    } else {
      return filter(v.toString());
    }
  }

  public String filter(String avaluelue) {
    if (avaluelue == null) {
      return null;
    }
    String parm = avaluelue;
    if (avaluelue.indexOf("@01@") > -1) {
      parm = avaluelue.replaceAll("@01@", "%01%");
    }
   /* StringBuffer parm = new StringBuffer();
    for (int i = 0; i < avaluelue.length(); ++i) {
      char j = avaluelue.charAt(i);
      if (j != '<' && j != '>' && j != '"' && j != '\'' && j != '%' && j != ';' && j != ')' && j != '(' && j != '+') {
        parm.append(j);
      }
    }*/
    return parm.toString().trim();
  }

}

