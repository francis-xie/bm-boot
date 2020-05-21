/**
 *
 * User: shaw
 * Date: May 17, 2003
 * Time: 5:04:57 PM
 *
 */
package com.emis.report;

import com.emis.business.emisBusinessResourceBean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public final class emisStringTokenizerContainer extends HashMap {
  //用於儲存原始的前端資料
  final HashMap backup = new HashMap();

  public final void add(final String key, String content, final String delim) {
    final int _iPos = content.indexOf(delim + delim);
    if (_iPos >= 0)
      content = content.substring(0, _iPos + 1) + " " + content.substring(_iPos + 1);
    final emisStringTokenizer token = new emisStringTokenizer(content, delim);
    this.put(key, token);
    backup.put(key, content);
  }

  public final String getNextToken(final String key) {
    final emisStringTokenizer token = (emisStringTokenizer) this.get(key);
    return token.nextToken();
  }

  public final boolean hasMoreToken(final emisBusinessResourceBean resource) {
    boolean flag = false;
    final Set keys = this.keySet();
    Iterator a = keys.iterator();
    while (a.hasNext()) {
      final String fieldName = (String) a.next();
      final emisStringTokenizer token = (emisStringTokenizer) this.get(fieldName);
      if (token.hasMoreTokens()) {
        flag = true;
      }
      //此set動作會直接影響到resourcebean的原始資料
      resource.getEmisFormBean().setParameter(fieldName, token.nextToken());
    }
    //當資料繞完後,將備份資料還原
    if (flag == false) {
      a = keys.iterator();
      while (a.hasNext()) {
        final String fieldName = (String) a.next();
        final String content = (String) backup.get(fieldName);
        resource.getEmisFormBean().setParameter(fieldName, content);
      }
      backup.clear();
    }
    return flag;
  }

}
