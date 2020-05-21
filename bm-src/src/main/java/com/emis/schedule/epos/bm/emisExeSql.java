package com.emis.schedule.epos.bm;

import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;
import com.emis.schedule.emisScheduleMgr;
import com.emis.schedule.epos.emisEposAbstractSchedule;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 执行data/sqlscript目录下的sql文件
 */
public class emisExeSql extends emisEposAbstractSchedule {
  public emisExeSql() {
    super();
  }

  public emisExeSql(ServletContext oContext) {
    oContext_ = oContext;
  }

  protected void postAction() throws Exception {
    List<String> okList = new ArrayList<String>();
    List<String[]> failList = new ArrayList<String[]>();
    boolean reloadProp = false;
    boolean reloadSched = false;
    try {
      emisFileMgr oFileMgr = emisFileMgr.getInstance(oContext_);
      emisDirectory downloadDir = oFileMgr.getDirectory("root").subDirectory("data").subDirectory("sqlscript");
      String scriptFileDir = downloadDir.getDirectory();
      String[] sqlScripts = new File(scriptFileDir).list();
      Arrays.sort(sqlScripts);

      int count = 0, errCount = 0;
      ByteArrayOutputStream byteout = null;
      InputStream r = null;
      File f = null;
      boolean isError = false;
      for (String sqlScript : sqlScripts) {
        try {
          isError = false;
          f = new File(scriptFileDir, sqlScript);
          if (!f.isFile() || f.length() == 0 || !f.canWrite()) continue;

          r = new FileInputStream(scriptFileDir + "/" + sqlScript);
          byteout = new ByteArrayOutputStream();
          byte tmp[] = new byte[999999];
          byte context[];
          int i = 0, off = 0;
          while ((i = r.read(tmp)) != -1) {
            if (tmp[0] == -17 && tmp[1] == -69 && tmp[2] == -65) off = 3; // 如是utf-8 bom编码的文件，前面三位保存的是字节序
            else off = 0;
            byteout.write(tmp, off, tmp.length - off);
          }

          context = byteout.toByteArray();
          String sql = new String(context, "UTF-8");
          sql = sql.replaceAll("\\b(go|GO|Go|gO)\\b", "@GO@");
          String[] arraySql = sql.split("@GO@");
          sql = null;
          for (String sSql : arraySql) {
            if (sSql == null || "".equals(sSql.trim())) continue;
            try {
              oDataSrc_.execute(sSql);
              oDataSrc_.commit();
            } catch (Exception ee) {
              try {
                oDataSrc_.rollback();
              } catch (Exception ex) {
                oLogger_.error(ex, ex);
              }
              isError = true;
              failList.add(new String[]{sqlScript, sSql, ee.getMessage()});
//              break; // 有sql出错时，不再继续后面的sql执行，因为sql可能需要按顺序执行，需要修正当前有错的sql文件再继续。
            }
          }
          arraySql = null;
          context = null;
          tmp = null;
          if (r != null) {
            r.close();
            r = null;
          }
          if (byteout != null) {
            byteout.close();
            byteout = null;
          }
          if (isError) {
            errCount++;
//            break; // 有sql出错时，不再继续后面的sql执行，因为sql可能需要按顺序执行，需要修正当前有错的sql文件再继续。
          } else {
            count++;
            okList.add(sqlScript);
          }
          f.delete();
          f = null;
        } catch (Exception e) {
          try {
            oDataSrc_.rollback();
          } catch (Exception ex) {
            oLogger_.error(ex, ex);
          }
          errCount++;
          failList.add(new String[]{sqlScript, "", e.getMessage()});
          if (r != null) r.close();
          if (byteout != null) byteout.close();
//          break; // 有sql出错时，不再继续后面的sql执行，因为sql可能需要按顺序执行，需要修正当前有错的sql文件再继续。
        }
        if (sqlScript.indexOf("EMISPROP") > 0) {
          reloadProp = true;
        }
        if (sqlScript.indexOf("SCHED") > 0) {
          reloadSched = true;
        }
      }
      //out.write("●.总文件数：" + sqlScripts.length+"<br>|<br>已执行成功的文件数：" + count + "<br>|<br>执行失败的文件数：" + errCount);
      if (sqlScripts.length > 0) {
        oLogger_.info("●.总文件数：" + sqlScripts.length + "&nbsp;&nbsp;|&nbsp;&nbsp;已执行成功的文件数：" + count);
      }
      sqlScripts = null;
    } catch (Exception eee) {
      oLogger_.error(eee, eee);
    } finally {
      System.gc();
    }
    if (reloadProp) {
      try {
        emisProp.reload(oContext_);
      } catch (Exception ex) {
        oLogger_.error(ex, ex);
      }
    }
    if (reloadSched) {
      try {
        emisScheduleMgr.getInstance(oContext_).reload(null);
      } catch (Exception ex) {
        oLogger_.error(ex, ex);
      }
    }

    if (failList.size() > 0) {
      oLogger_.info("●.执行失败信息列表：");
      for (String errInfo[] : failList) {
        oLogger_.info(errInfo[0] + ": " + errInfo[2]);
      }
    }
    if (okList.size() > 0) {
      oLogger_.info("●.已执行成功文件列表：");
      for (String filename : okList) {
        oLogger_.info(filename);
      }
    }
    failList.clear();
    failList = null;
    okList.clear();
    okList = null;
  }

}