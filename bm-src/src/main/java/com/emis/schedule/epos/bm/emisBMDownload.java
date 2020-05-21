package com.emis.schedule.epos.bm;

import com.emis.bm.download.emisBMDownLoadData;
import com.emis.db.emisDb;
import com.emis.db.emisProp;
import com.emis.file.emisDirectory;
import com.emis.file.emisFile;
import com.emis.file.emisFileMgr;
import com.emis.schedule.epos.emisEposAbstractSchedule;
import com.emis.util.emisUtil;
import com.emis.util.emisZipUtil;

import javax.servlet.ServletContext;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * 抓取下传档
 */
public class emisBMDownload extends emisEposAbstractSchedule {

  protected emisDirectory oErrDir_ = null;   // 有问题之交易档目录

  String partImgName = "-BMPARTIMG.ZIP";
  String departImgName = "-BMDEPARTIMG.ZIP";
  String settingImgName = "-BMSETTINGTIMG.ZIP";
  String upgrade = "-BMUPGRADE.ZIP";

  public emisBMDownload() {
    super();
  }

  public emisBMDownload(ServletContext oContext) {
    oContext_ = oContext;
  }

  protected void postAction() throws Exception {
    emisProp prop = emisProp.getInstance(oContext_);
    String S_NO = prop.get("S_NO");
    String ID_NO = prop.get("ID_NO");

    if ("".equals(S_NO) || "".equals(ID_NO)) {
      oLogger_.warn("not set cash");
      return;
    }

    emisBMDownLoadData dwn = new emisBMDownLoadData(oContext_);
    dwn.downData();

    emisFileMgr oFileMgr = emisFileMgr.getInstance(oContext_);
    emisDirectory downloadDir = oFileMgr.getDirectory("root").subDirectory("data").subDirectory("download").subDirectory(S_NO).subDirectory(ID_NO);
    oErrDir_ = oFileMgr.getDirectory("root").subDirectory("data").subDirectory("errorData");

    emisDirectory partImgDir = oFileMgr.getDirectory("root").subDirectory("images").subDirectory("part");
    emisDirectory departImgDir = oFileMgr.getDirectory("root").subDirectory("images").subDirectory("bm").subDirectory("depart");
    emisDirectory settingImgDir = oFileMgr.getDirectory("root").subDirectory("images").subDirectory("bm").subDirectory("setting");
    emisDirectory upgradeDir = oFileMgr.getDirectory("root");

    Enumeration targetEnum_ = downloadDir.getFileList();
    while (targetEnum_.hasMoreElements()) {
      try {
        emisFile ef_ = (emisFile) targetEnum_.nextElement();
        if (ef_.getFileName().toUpperCase().endsWith(".SQL")) {
          // 执行SQL文件
          if (ef_.exists() && ef_.canWrite()) {
            oLogger_.info("exec sql: " + ef_.getFileName());
            execSQL(ef_);
          }
        } else if (ef_.getFileName().toUpperCase().endsWith(partImgName)) {
          // 更新商品图片
          if (ef_.exists() && ef_.canWrite()) {
            if (extractZipToDir(ef_, partImgDir)) {
              ef_.delete();
            }
          }
        } else if (ef_.getFileName().toUpperCase().endsWith(departImgName)) {
          // 更新分类图片
          if (ef_.exists() && ef_.canWrite()) {
            if (extractZipToDir(ef_, departImgDir)) {
              ef_.delete();
            }
          }
        } else if (ef_.getFileName().toUpperCase().endsWith(settingImgName)) {
          // 更新广告轮播图图片
          if (ef_.exists() && ef_.canWrite()) {
            if (extractZipToDir(ef_, settingImgDir)) {
              ef_.delete();
            }
          }
        } else if (ef_.getFileName().toUpperCase().endsWith(upgrade)) {
          // 版本更新
          if (ef_.exists() && ef_.canWrite()) {
            versionUpgrade(ef_, upgradeDir);
          }
        } else {
          // 其他文件，丢error目录等待删除
          if (ef_.exists() && ef_.canWrite()) {
            backupFile(oErrDir_, ef_);
          }
        }
      } catch (Exception ex) {
        oLogger_.error(ex, ex);
      }
    }
  }

  /**
   * 将 ZIP 档解压缩到指定的目录
   *
   * @param dir 指定的目录名称
   * @return true/false 解压缩成功与否
   * @throws java.lang.Exception
   */
  protected final boolean extractZipToDir(emisFile file, emisDirectory dir) throws Exception {
    boolean _bRetVal = true;
    if (file.getFileExt().equalsIgnoreCase("ZIP")) {
      //emisUtil.extractAllZip(oActiveFile_, dir, false, true);
      try {
        emisUtil.extractAllZip(file, dir, false, true, true);//不要路径
      } catch (Exception e) {
        backupFile(oErrDir_, file);
        e.printStackTrace();
      }
    } else {
      // oActiveFile_.copyTo(dir);    // MARK掉，此时 oActiveFile_还是null;
      _bRetVal = false;
    }
    return _bRetVal;
  }

  protected final void backupFile(emisDirectory destDir, emisFile bakFile) {
    try {
      bakFile.copyTo(destDir);
    } catch (Exception e) {
      oLogger_.warn(e, e);
    }
  }

  protected final void execSQL(emisFile sqlFile) {
    InputStream r = null;
    ByteArrayOutputStream byteout = null;
    try {
      r = new FileInputStream(sqlFile.getFullName());
      byteout = new ByteArrayOutputStream();
      byte tmp[] = new byte[99999];
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
        //System.out.println(sSql);
        if (sSql == null || "".equals(sSql.trim())) continue;
        try {
          oDataSrc_.execute(sSql);
          oDataSrc_.commit();
        } catch (Exception ee) {
          oLogger_.error(ee);
          oDataSrc_.rollback();
          break; // 有sql出错时，不再继续后面的sql执行，因为sql可能需要按顺序执行，需要修正当前有错的sql文件再继续。
        }
      }
      r.close();
      byteout.close();
    } catch (Exception e) {
      try {
        oDataSrc_.rollback();
      } catch (SQLException e1) {
        oLogger_.error(e1);
      }
      oLogger_.error(e);
    }
    sqlFile.delete();
  }

  protected final void versionUpgrade(emisFile upgradeFile, emisDirectory upgradeDir) {
    try {
      // BM-1.0.0.19082700-20190827160000-BMUPGRADE.ZIP
      String ver = upgradeFile.getFileName();
      oLogger_.info("versionUpgrade: " + ver);
      String[] verSplit = ver.split("-");
      if (extractZipToDir(upgradeFile, upgradeDir)) {
        upgradeFile.delete();
      }
      if (verSplit.length >= 2) {
        updEmisprop(verSplit[1]);
      }
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
    }
  }

  private boolean updEmisprop(String upgradeVersion) {
    PreparedStatement updEmispropStmt = null;
    PreparedStatement insEmispropStmt = null;
    try {
      String today = emisUtil.todayDateAD();
      String nowTime = emisUtil.todayDateAD("/") + " " + emisUtil.todayTimeS(true);
      updEmispropStmt = oDataSrc_.prepareStmt("update emisprop set VALUE = ?, UPD_DATE = ? where NAME = ?");
      insEmispropStmt = oDataSrc_.prepareStmt("insert into emisprop (NAME, VALUE, KIND, REMARK, UPD_DATE) values (?, ?, ?, ?, ?)");

      // BM_VERSION
      try {
        updEmispropStmt.clearParameters();
        updEmispropStmt.setString(1, upgradeVersion);
        updEmispropStmt.setString(2, today);
        updEmispropStmt.setString(3, "BM_VERSION");
        if (updEmispropStmt.executeUpdate() <= 0) {
          insEmispropStmt.clearParameters();
          insEmispropStmt.setString(1, "BM_VERSION");
          insEmispropStmt.setString(2, upgradeVersion);
          insEmispropStmt.setString(3, "SYS");
          insEmispropStmt.setString(4, "系统版本号");
          insEmispropStmt.setString(5, today);
          insEmispropStmt.executeUpdate();
        }
        oDataSrc_.commit();
      } catch (Exception ex) {
        oLogger_.error("update BM_VERSION error");
      }

      // BM_UPDATE_TIME
      try {
        updEmispropStmt.clearParameters();
        updEmispropStmt.setString(1, nowTime);
        updEmispropStmt.setString(2, today);
        updEmispropStmt.setString(3, "BM_UPDATE_TIME");
        if (updEmispropStmt.executeUpdate() <= 0) {
          insEmispropStmt.clearParameters();
          insEmispropStmt.setString(1, "BM_UPDATE_TIME");
          insEmispropStmt.setString(2, nowTime);
          insEmispropStmt.setString(3, "SYS");
          insEmispropStmt.setString(4, "系统更新时间");
          insEmispropStmt.setString(5, today);
          insEmispropStmt.executeUpdate();
        }
        oDataSrc_.commit();
      } catch (Exception ex) {
        oLogger_.error("update BM_UPDATE_TIME error");
      }

      emisProp.reload(oContext_);
    } catch (Exception ex) {
      oLogger_.error(ex, ex);
    } finally {
      if (updEmispropStmt != null) {
        oDataSrc_.closePrepareStmt(updEmispropStmt);
        updEmispropStmt = null;
      }
      if (insEmispropStmt != null) {
        oDataSrc_.closePrepareStmt(insEmispropStmt);
        insEmispropStmt = null;
      }
    }
    return true;
  }


}