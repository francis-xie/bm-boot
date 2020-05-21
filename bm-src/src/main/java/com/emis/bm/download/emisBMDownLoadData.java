package com.emis.bm.download;

import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFileMgr;
import com.emis.util.emisLogger;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.List;

public class emisBMDownLoadData {
  private ServletContext context_;
  protected Logger oLogger_ = null;

  public emisBMDownLoadData(ServletContext context) {
    this.context_ = context;
    try {
      oLogger_ = emisLogger.getlog4j(context_, this.getClass().getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setServletContext(ServletContext context) {
    this.context_ = context;
    try {
      oLogger_ = emisLogger.getlog4j(context_, this.getClass().getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private SiteFileFetch[] fetchs;

  public void downData() {
    oLogger_.info("begin downData");
    String SME_URL = "";
    String S_NO = "";
    String ID_NO = "";
    emisDirectory dir = null;
    try {
      emisProp prop = emisProp.getInstance(context_);
      SME_URL = prop.get("SME_URL");
      S_NO = prop.get("S_NO");
      ID_NO = prop.get("ID_NO");
      dir = emisFileMgr.getInstance(context_).getDirectory("root");
    } catch (Exception ex) {

    }
    if ("".equals(SME_URL) || "".equals(S_NO) || "".equals(ID_NO)) {
      // 系统参数不完整，不执行后续动作。
      return;
    }


    // 下载数据包
    String sDownUrl = Utility.getDownloadURL(Utility.TYPE_DOWNLOAD, SME_URL, S_NO, ID_NO, "jsp/sas/bm_download.jsp");
//    oLogger_.info(sDownUrl);
    List<String> downList = Utility.getDownloadList(sDownUrl);
    oLogger_.info(downList.size());
    final String sTmpPath =  dir.subDirectory("data").subDirectory("temp").getDirectory();
    final String sSeparator = System.getProperty("file.separator");
    if (downList != null && downList.size() > 0) {
      fetchs = new SiteFileFetch[downList.size()];
      for (int i = 0, leng = downList.size(); i < leng; i++) {
        oLogger_.info(downList.get(i));
        final SiteFileFetch fetch = Utility.download(SME_URL + "/"
            + downList.get(i), sTmpPath, downList.get(i), 1);
        final String sName = downList.get(i);
        fetch.setDownOverCallback(new Callback() {
          @Override
          public void callback() {
            String SME_URL = "";
            String S_NO = "";
            String ID_NO = "";
            emisDirectory dir = null;
            try {
              emisProp prop = emisProp.getInstance(context_);
              SME_URL = prop.get("SME_URL");
              S_NO = prop.get("S_NO");
              ID_NO = prop.get("ID_NO");
              dir = emisFileMgr.getInstance(context_).getDirectory("root");
            } catch (Exception ex) {

            }
            File dataTemp = null;
            File dataTarget = null;
            // 从临时目录移转到解档转入目录
            dataTemp = new File(sTmpPath + sSeparator + sName);
            dataTarget = new File(dir.getDirectory() + sName);

            // 目录不存在时先创建
            if (!dataTarget.getParentFile().exists()) {
              dataTarget.getParentFile().mkdirs();
            }
            // 移动档案
            dataTemp.renameTo(dataTarget);

            // 清除已完成的续传档
            if (fetch.checkDoneClean()) {
              // 数据包下载完成通知后台
              Utility.notifyAfterDownload(Utility.TYPE_DOWNLOAD,
                  sName, S_NO, ID_NO, SME_URL + "/jsp/sas/bm_download_after.jsp");
            }
          }
        });
        fetchs[i] = fetch;
      }
    }
  }
}