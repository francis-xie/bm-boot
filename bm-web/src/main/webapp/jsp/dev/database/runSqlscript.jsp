<%-- $Id: runSqlscript.jsp 11267 2018-03-28 09:30:37Z andy.he $
调用范例：http://localhost:8082/V3/jsp/dev/database/runSqlscript.jsp?DIR=data/sqlscript/0301
--%>
<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="com.emis.db.*,java.sql.*,

                 com.emis.file.emisFile,
                 com.emis.file.emisFileMgr,

                 com.emis.file.emisDirectory" %>
<%@ page import="com.emis.app.migration.action.emisMiEncrypt" %>
<%@ page import="com.emis.user.emisUser" %>
<%@ page import="com.emis.user.emisCertFactory" %>
<%@ page import="com.emis.util.emisUtil" %>
<%@ page import="com.emis.user.emisPermission" %>
<%@ page import="com.emis.qa.emisServletContext" %>
<%@ page import="com.emis.util.emisLogger" %>
<%@ page import="java.io.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.emis.util.emisZipUtil" %>
<%@ page import="javax.xml.transform.Result" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.emis.util.emisDate" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.lang.management.OperatingSystemMXBean" %>
<%@ page import="java.lang.management.ManagementFactory" %>

<%!
  public void exec(ServletContext servlet, Writer out, String scriptFileDir) throws Exception{
    emisDb db = null;

    try{
      scriptFileDir = scriptFileDir.replaceAll("\\\\","/");
      out.write(scriptFileDir);
      out.write("\r\n<br>");
      String[] sqlScripts = new File(scriptFileDir).list();
      Arrays.sort(sqlScripts);

      db = emisDb.getInstance(servlet);
      db.setAutoCommit(false);
      int count = 0, errCount = 0;
      ByteArrayOutputStream byteout = null;
      InputStream r = null;
      File f = null;
      for(String sqlScript : sqlScripts){
        try {
          //out.write(sqlScript);
          //out.write("\r\n<br>");
          f = new File(scriptFileDir,sqlScript);
          if (!f.isFile()) continue;

          r = new FileInputStream(scriptFileDir + "/" + sqlScript);
          byteout = new ByteArrayOutputStream();
          byte tmp[] = new byte[99999];
          byte context[];
          int i = 0, off = 0;
          while ((i = r.read(tmp)) != -1) {
            if(tmp[0] == -17 && tmp[1] == -69 && tmp[2] == -65) off = 3; // 如是utf-8 bom编码的文件，前面三位保存的是字节序
            else off = 0;
            byteout.write(tmp,off, tmp.length - off);
          }

          context = byteout.toByteArray();
          String sql = new String(context, "UTF-8");
          sql = sql.replaceAll("\\b(go|GO|Go|gO)\\b","");
          //out.write(sql);
          //out.write("\r\n<br>");
          db.execute(sql);
          db.commit();
          count++;
          out.write(sqlScript + " is OK !");
          out.write("\r\n<br>");
          if(r!=null) r.close();
          if(byteout!=null) byteout.close();
          f.delete();
        } catch(Exception e){
          db.rollback();
          errCount++;
          out.write(sqlScript + " is Error!" + e.getMessage() );
          out.write("\r\n<br>");
          if(r!=null) r.close();
          if(byteout!=null) byteout.close();
        }
      }
      out.write("执行成功的文件数：" + count + " | 执行失败的文件数：" + errCount);
    } finally {
      if(db != null) db.close();
    }
  }

  public void exec2(ServletContext servlet, Writer out, String scriptFileDir, String dbName) throws Exception{
    emisDb db = null;
    List<String> okList = new ArrayList<String>();
    List<String[]> failList = new ArrayList<String[]>();
    try{
      scriptFileDir = scriptFileDir.replaceAll("\\\\","/");
      out.write(scriptFileDir);
      out.write("\r\n<br>");
      String[] sqlScripts = new File(scriptFileDir).list();
      Arrays.sort(sqlScripts);

      db = emisDb.getInstance(servlet, dbName);
      db.setAutoCommit(false);
      int count = 0, errCount = 0;
      ByteArrayOutputStream byteout = null;
      InputStream r = null;
      File f = null;
      boolean isError  = false;
      for(String sqlScript : sqlScripts){
        try {
          //out.write(sqlScript);
          //out.write("\r\n<br>");
          isError  = false;
          f = new File(scriptFileDir,sqlScript);
          if (!f.isFile() || f.length() == 0) continue;

          r = new FileInputStream(scriptFileDir + "/" + sqlScript);
          byteout = new ByteArrayOutputStream();
          byte tmp[] = new byte[999999];
          byte context[];
          int i = 0, off = 0;
          while ((i = r.read(tmp)) != -1) {
            if(tmp[0] == -17 && tmp[1] == -69 && tmp[2] == -65) off = 3; // 如是utf-8 bom编码的文件，前面三位保存的是字节序
            else off = 0;
            byteout.write(tmp,off, tmp.length - off);
          }

          context = byteout.toByteArray();
          String sql = new String(context, "UTF-8");
          sql = sql.replaceAll("\\b(go|GO|Go|gO)\\b","@GO@");
          String[] arraySql = sql.split("@GO@");
          sql = null;
          for(String sSql : arraySql) {
            //System.out.println(sSql);
            if(sSql == null || "".equals(sSql.trim())) continue;
            try {
              db.execute(sSql);
              db.commit();
            } catch(Exception ee){
              db.rollback();
              isError = true;
              failList.add(new String[]{sqlScript,sSql, ee.getMessage()});
              break; // 有sql出错时，不再继续后面的sql执行，因为sql可能需要按顺序执行，需要修正当前有错的sql文件再继续。
            }
          }
          arraySql = null;
          context = null;
          tmp=null;
          if(r!=null) {
            r.close();
            r=null;
          }
          if(byteout!=null) {
            byteout.close();
            byteout=null;
          }
          if(isError){
            errCount++;
            break; // 有sql出错时，不再继续后面的sql执行，因为sql可能需要按顺序执行，需要修正当前有错的sql文件再继续。
          } else {
            count++;
            okList.add(sqlScript);
            f.delete();
          }
          f=null;
          //out.write(sqlScript + " is OK !");
          //out.write("\r\n<br>");
        } catch(Exception e){
          db.rollback();
          errCount++;
          failList.add(new String[]{sqlScript, "", e.getMessage()});
          if(r!=null) r.close();
          if(byteout!=null) byteout.close();
          break; // 有sql出错时，不再继续后面的sql执行，因为sql可能需要按顺序执行，需要修正当前有错的sql文件再继续。
        }
      }
      //out.write("●.总文件数：" + sqlScripts.length+"<br>|<br>已执行成功的文件数：" + count + "<br>|<br>执行失败的文件数：" + errCount);
      out.write("●.总文件数：" + sqlScripts.length+"&nbsp;&nbsp;|&nbsp;&nbsp;已执行成功的文件数：" + count);
      out.write("<br>");
      sqlScripts = null;
    } catch(Exception eee){

    } finally {
      if(db != null) {
        db.close();
        db = null;
      }
      System.gc();
    }
    if(failList.size()>0){
      out.write("●.执行失败信息列表：<br>");
      out.write("<table width='100%' border='1'><tr><td nowrap width='10%'>文件名</td><td nowrap width='60%'>执行SQL</td><td nowrap width='40%'>错误讯息</td></tr>");
      for(String errInfo[] :failList) {
        out.write("<tr>");
        out.write("<td nowrap>" + errInfo[0] +"</td>");
        out.write("<td >" + errInfo[1] +"</td>");
        out.write("<td >" + errInfo[2] +"</td>");
        out.write("</tr>");
      }
      out.write("</table>");
      out.write("</br>");
    }
    if(okList.size()>0){
      out.write("●.已执行成功文件列表：<br>");
      out.write("<table width='100%' border='1'>");
      for(String filename :okList) {
        out.write("<tr><td>" + filename +"</td></tr>");
      }
      out.write("</table><br>");
    }
    failList.clear();
    failList = null;
    okList.clear();
    okList = null;
  }

  /*
   * 执行大批次的insert into 语句
   */
  public void batImportData(ServletContext servlet, Writer out, String scriptFileDir, String dbName) throws Exception{
    emisDb db = null;
    List<String> okList = new ArrayList<String>();
    List<String[]> failList = new ArrayList<String[]>();
    try {

      scriptFileDir = scriptFileDir.replaceAll("\\\\", "/");
      out.write(scriptFileDir);
      out.write("\r\n<br>");
      String[] sqlScripts = new File(scriptFileDir).list();
      Arrays.sort(sqlScripts);

      db = emisDb.getInstance(servlet,dbName);
      db.setAutoCommit(true);
      int count = 0, errCount = 0;

      File f = null;

      for (String sqlScript : sqlScripts) {

        BufferedReader br = new BufferedReader(new FileReader(scriptFileDir+"/"+sqlScript));
        try {
          String line = "";
          int i = 0;
          StringBuffer sql = new StringBuffer("");
          while ((line = br.readLine()) != null) {
            //out.write(line);
            //out.write("<br>");
            sql.append(line);
            if(++i % 20000 == 0) {
              db.executeUpdate(sql.toString());
              sql.setLength(0);
              out.write("●."+scriptFileDir+"/"+sqlScript+" : " + i/2 );
              out.write("<br>");
            }
          }
          if(sql.length() > 0){
            db.executeUpdate(sql.toString());
            out.write("●."+scriptFileDir+"/"+sqlScript+" : " + i/2 );
            out.write("<br>");
          }
          //out.write("●.总文件数：" + sqlScripts.length+"<br>|<br>已执行成功的文件数：" + count + "<br>|<br>执行失败的文件数：" + errCount);


        } catch (Exception eee) {
          out.write("XX ->"+scriptFileDir+"/"+sqlScript+" : " +eee.getMessage()+"<br>");
        } finally {
          if(br != null ) br.close();
        }

      }
    }catch(Exception ee){
      out.write("XX ->"+ee.getMessage()+"<br>");
    }finally {
      if (db != null) db.close();
    }
    if(okList.size()>0){
      out.write("●.已执行成功文件列表：<br>");
      out.write("<table width='100%' border='1'>");
      for(String filename :okList) {
        out.write("<tr><td>" + filename +"</td></tr>");
      }
      out.write("</table><br>");
    }
    if(failList.size()>0){
      out.write("●.执行失败信息列表：<br>");
      out.write("<table width='100%' border='1'><tr><td nowrap width='10%'>文件名</td><td nowrap width='60%'>执行SQL</td><td nowrap width='40%'>错误讯息</td></tr>");
      for(String errInfo[] :failList) {
        out.write("<tr>");
        out.write("<td nowrap>" + errInfo[0] +"</td>");
        out.write("<td >" + errInfo[1] +"</td>");
        out.write("<td >" + errInfo[2] +"</td>");
        out.write("</tr>");
      }
      out.write("</table>");
    }
  }

  public void execCMD(String cmd, Writer out) throws IOException {
    Process child = null;
    try {
      if (cmd == null || "".equals(cmd)) {
        out.write("没有需执行的命令！");
        return;
      }
      out.write(cmd);
      child = Runtime.getRuntime().exec("cmd.exe /C start " + cmd);
      child.waitFor();
    } catch(Exception e){
      System.out.println(e);
      out.write(e.getMessage());
    } finally {
      if(child != null) child.destroy();
    }
  }

  public void copyBackFiles(Writer out, emisDirectory backFileDir, emisDirectory copyToDir, emisZipUtil zip) throws Exception {
    String path = backFileDir.getDirectory();
    File files = new File(path);
    for (File f : files.listFiles()) {
      String name = f.getName();
      //System.out.println(name);

      if (f.isDirectory()) {
        copyBackFiles(out, backFileDir.subDirectory(name), copyToDir, zip);
      } else {
        if (name.endsWith("DUTY.TXT")) {
          //backFileDir.getFile(name).copyTo(copyToDir);
          out.write(f.getPath().replaceAll("\\\\", "/") + "<br>");
          if(zip != null)  zip.put(f.getPath().replaceAll("\\\\","/"));
          backFileDir.getFile(name).copyTo(copyToDir);
        }
      }
    }
  }
  public void moveEndofdayFilesForJIJITOWN(ServletContext servlet, Writer out){
    emisDb db = null;
    emisRowSet oRs = null;
    try{
      String fileName = null;
      File moveFile = null;
      db = emisDb.getInstance(servlet);
      db.executeQuery("select distinct sh.S_NO from SALE_H sh with(nolock)\n" +
          "where sh.SL_DATE=convert(nvarchar(8),dbo.GetLocalDate()-1,112) and not exists(\n" +
          "  select top 1 SL_KEY from SALE_D with(nolock) where SL_KEY = sh.SL_KEY\n" +
          ")");
      oRs = new emisRowSet(db);

      db.prepareStmt("select uh.UL_FILE_ZIP,uh.UL_FILE_BAK,uh.UL_FILE_DIR \n" +
          "from UPLOAD_LOG_H uh with(nolock) \n" +
          "where UL_DATE>=convert(nvarchar(8),dbo.GetLocalDate()-1,112) and UL_S_NO=? and UL_FILE_ZIP like 'X.%' and UL_FILE_DIR like '%endofday%'");
      while(oRs.next()){
        db.clearParameters();
        db.setString(1, oRs.getString("S_NO"));
        db.prepareQuery();
        while(db.next()){
          fileName = db.getString("UL_FILE_ZIP");
          moveFile = new File(db.getString("UL_FILE_BAK"),fileName);
          if(fileName.endsWith(".ZIP") && moveFile.exists()){
            emisUtil.copyFile(moveFile, db.getString("UL_FILE_DIR"));
            out.write("M  [" + emisUtil.todayDateAD("-") + " " + emisUtil.todayTimeS(true) + "] " + fileName +"\n");
          }
        }
      }
    } catch(Exception e){
      try {
        out.write(e.getMessage());
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } finally {
      if(db != null) db.close();
      if(oRs != null) oRs.close();
    }
  }

  public void jijitownMoveErrorFiles(ServletContext servlet, Writer out) throws Exception {
    emisDb db = null;
    PreparedStatement oUpdStmt = null;
    try{
      emisDirectory oRootDir = emisFileMgr.getInstance(servlet).getDirectory("root");
      String errorDir = oRootDir.subDirectory("data/upload/all/realtime/error").getDirectory();
      String fileName = null;
      File errorFile = null;
      db = emisDb.getInstance(servlet);
      oUpdStmt = db.prepareStmt("update UPLOAD_LOG_H set REPEATNUM = REPEATNUM + 1 where UL_FILE_ZIP = ? ");
      String sql =
          "select distinct uh.UL_FILE_ZIP,uh.UL_FILE_BAK,uh.UL_FILE_DIR,uh.REPEATNUM \n" +
              "from UPLOAD_LOG_H uh with(nolock)\n" +
              "where uh.UL_DATE >=? and uh.UL_DATE <= ? and uh.REPEATNUM<=1 and uh.ISSENDEMAIL='N' and ( ( uh.FLS_NO in ('3','4') and ( (uh.REMARK not like N'%PRIMARY%' and uh.REMARK not like N'%未定%') ))\n" +
              "   or ( uh.FLS_NO = '2' and uh.REMARK like N'%作死%' ) )";
      db.prepareStmt(sql);
      String today = emisUtil.todayDateAD();
      db.setString(1, emisUtil.getDateString(today, -1));
      db.setString(2, today);
      db.prepareQuery();
      while (db.next()){
        fileName = db.getString("UL_FILE_ZIP");
        errorFile = new File(db.getString("UL_FILE_BAK"),fileName);
        if(errorFile.exists()){
          emisUtil.copyFile(errorFile, db.getString("UL_FILE_DIR"));
          out.write("M  [" + emisUtil.todayDateAD("-") + " " + emisUtil.todayTimeS(true) + "] " + fileName +"\n");
          //同时删除error目录下的档案。
          new File(errorDir,fileName).delete();

          oUpdStmt.clearParameters();
          oUpdStmt.setString(1, fileName);
          oUpdStmt.execute();
        }
      }
    } finally {
      if(oUpdStmt != null) db.closePrepareStmt(oUpdStmt);
      if(db != null) db.close();
    }
  }

  public void gmxMoveErrorFiles(ServletContext servlet, Writer out) throws Exception {
    emisDb db = null;
    PreparedStatement oUpdStmt = null;
    try{
      emisDirectory oRootDir = emisFileMgr.getInstance(servlet).getDirectory("root");
      String errorDir = oRootDir.subDirectory("data/upload/all/realtime/error").getDirectory();
      String fileName = null;
      File errorFile = null;
      db = emisDb.getInstance(servlet);
      oUpdStmt = db.prepareStmt("update UPLOAD_LOG_H set REPEATNUM = REPEATNUM + 1 where UL_FILE_ZIP = ? ");
      String sql =
          "select distinct uh.UL_FILE_ZIP,uh.UL_FILE_BAK,uh.UL_FILE_DIR,uh.REPEATNUM \n" +
              "from UPLOAD_LOG_H uh with(nolock)\n" +
              "where uh.UL_FILE_ZIP like '%ORDER.ZIP' and uh.UL_DATE >=? and uh.UL_DATE <= ? and uh.FLS_NO='2' and uh.REPEATNUM<=1 and uh.REMARK like N'%PK_SALE_ H%'";
      db.prepareStmt(sql);
      String today = emisUtil.todayDateAD();
      db.setString(1, emisUtil.getDateString(today, -1));
      db.setString(2, today);
      db.prepareQuery();
      while (db.next()){
        fileName = db.getString("UL_FILE_ZIP");
        errorFile = new File(db.getString("UL_FILE_BAK"),fileName);
        if(errorFile.exists()){
          emisUtil.copyFile(errorFile, db.getString("UL_FILE_DIR"));
          out.write("M  [" + emisUtil.todayDateAD("-") + " " + emisUtil.todayTimeS(true) + "] " + fileName +"\n");
          //同时删除error目录下的档案。
          new File(errorDir,fileName).delete();

          oUpdStmt.clearParameters();
          oUpdStmt.setString(1, fileName);
          oUpdStmt.execute();
        }
      }
    } finally {
      if(oUpdStmt != null) db.closePrepareStmt(oUpdStmt);
      if(db != null) db.close();
    }
  }

  public void genMS_Description(ServletContext servlet, Writer out){
    emisDb db = null;
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try{
      StringBuffer script = new StringBuffer();
      db = emisDb.getInstance(servlet);
      stmt = db.prepareStmt("select name,colid from syscolumns where id=object_id(?)");
      rs = db.executeQuery("select top 10 [name] from sysobjects where xtype='U' and type='U'");
      db.setCurrentPrepareStmt(stmt);
      while(rs.next()){
        script.append("/**********").append(rs.getString("name")).append("**********/\r\n");
        script.append("if(not exists(select * from sys.extended_properties a where a.major_id=object_id('").append(rs.getString("name")).append("') and major_id=0))\r\n");
        script.append("EXEC sp_dropextendedproperty 'MS_Description','user',dbo,'table','").append(rs.getString("name")).append("',NULL,NULL\r\n");
        script.append("EXECUTE sp_addextendedproperty N'MS_Description', N'',").append("--请填写表描述\r\n    ").append(" N'user', N'dbo', N'table', N'").append(rs.getString("name")).append("',NULL, NULL\r\n");
        script.append("---------------------------------------------------------------\r\n");
        db.clearParameters();
        db.setString(1, rs.getString("name"));
        db.prepareQuery();
        while(db.next()){
          script.append("if(not exists(select * from sys.extended_properties a where a.major_id=object_id('")
              .append(rs.getString("name")).append("') and major_id=").append(db.getString("colid")).append("))\r\n");
          script.append("EXEC sp_dropextendedproperty 'MS_Description','user',dbo,'table','").append(rs.getString("name")).append("',column,").append(db.getString("name")).append("\r\n");
          script.append("EXECUTE sp_addextendedproperty N'MS_Description', N'',").append("--请填写表字段<").append(db.getString("name")).append(">描述\r\n    ").append(" N'user', N'dbo', N'table', N'").append(rs.getString("name")).append("',column,").append(db.getString("name")).append("\r\n");
        }
      }
      System.out.println(script.toString());

    } catch(Exception e){
      e.printStackTrace();
    } finally {
      if(stmt != null) db.closePrepareStmt(stmt);
      if(db!=null) db.close();
    }
  }

  public void exportData(ServletContext servlet, String tableName, String exportSql, HttpServletResponse response) throws Exception{
    emisDb db = null;
    try{
      response.setContentType("application/x-msdownload");
      response.setHeader("Content-Disposition", "attachment;filename="+tableName+".txt");

      db = emisDb.getInstance(servlet);
      StringBuffer insSql = new StringBuffer();
      db.executeQuery(exportSql);

      StringBuffer sbTmp = new StringBuffer("insert into ").append(tableName).append("(");
      for(int i = 1; i<=db.getColumnCount(); i++){
        // System.out.println(db.getColumnName(i) +">>>>>>>>"+db.getColumnType(i));
        if(i>1) sbTmp.append(",");
        sbTmp.append(db.getColumnName(i));
      }
      sbTmp.append(")\r\n values (%replace%) \r\n");
      String sql = sbTmp.toString();
      sbTmp.setLength(0);
      while(db.next()){
        for(int i = 1; i<=db.getColumnCount(); i++){
          if(i>1) sbTmp.append(",");
          switch (db.getColumnType(i)) {
            case Types.CHAR:
            case Types.VARCHAR:
              if(db.getString(i) == null) sbTmp.append("null");
              else sbTmp.append("N'").append(db.getString(i).replaceAll("'","''")).append("'");
              break;
            /* 不知为什么数据库为nvarchar类型上面取出来的类型为 VARCHAR
            case Types.NVARCHAR:
              if(db.getString(i) == null) sbTmp.append("null");
              else sbTmp.append("N'").append(db.getString(i)).append("'");
              break;
              */
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
              sbTmp.append(db.getFloat(i));
              break;
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
              sbTmp.append(db.getInt(i));
              break;
            default:
              sbTmp.append("null");
              break;
          }
        }
        insSql.append(sql.replaceAll("%replace%",sbTmp.toString().replaceAll("\\\\","\\\\\\\\")));
        sbTmp.setLength(0);
      }
      //out.write(insSql.toString());

      response.getOutputStream().write(insSql.toString().getBytes());
    } finally {
      if(db != null) db.close();
    }
  }

  private Map<String,String> getWhereMap() {
    Map<String, String> where = new HashMap<String, String>();
    where.put("ANNOUNCE",          "ANN_STOP_D>=convert(nvarchar(8),dbo.GetLocalDate(),112) or isnull(ANN_STOP_D,'') = ''");
    where.put("BACK_D",            "exists(select BA_NO from [Vi].dbo.BACK_H with (nolock) where BA_NO=d.BA_NO and S_NO=d.S_NO and BA_DATE>='201701' and BA_DATE<'201707')");
    where.put("BACK_H",            "BA_DATE>='201701' and BA_DATE<'201707'");
    where.put("BARCARD_AMT",       "SL_DATE>='201701' and SL_DATE<'201707' ");
    where.put("COUNT_D",           "exists(select CO_NO from [Vi].dbo.[COUNT_H] with (nolock) where CO_NO=d.CO_NO and S_NO=d.S_NO and CO_DATE>='201701' and CO_DATE<'201707')");
    where.put("COUNT_H",           "CO_DATE>='201701' and CO_DATE<'201707'");

    where.put("DAILYCLOSE",        "CLOSE_DATE>='201701' and  CLOSE_DATE<'201707'");
    where.put("DAILYCLOSE_AMT",    "CLOSE_DATE>='201701' and CLOSE_DATE<'201707'");
    where.put("DAILYCLOSE_LOG",    "CLOSE_DATE>='201701' and CLOSE_DATE<'201707'");
    where.put("DCLOSE",            "SL_DATE>='201701' and SL_DATE<'201707' ");
    where.put("DCLOSE_P_PRICE",    "DC_DATE>='201701' and DC_DATE<'201707'");
    where.put("DCLOSE_REPORT",     "DC_DATE>='201701' and DC_DATE<'201707'");
    where.put("INS_D",             "exists(select IN_NO from [Vi].dbo.[INS_H] with (nolock) where IN_NO=d.IN_NO and S_NO=d.S_NO and IN_DATE>='201701' and IN_DATE<'201707')");
    where.put("INS_H",             "IN_DATE>='201701' and IN_DATE<'201707'");
    where.put("PART_S_LOG",        "PLS_DATE>='201701' and PLS_DATE<'201707'");
    where.put("PO_H",              "PO_DATE>='201701' and PO_DATE<'201707'");
    where.put("POR",               "POR_DATE>='201701' and POR_DATE<'201707'");
    where.put("RAT_PREALLOT_H",    "POR_DATE>='201701' and POR_DATE<'201707'");
    where.put("RAT_PREALLOT_P",    "exists(select PA_NO from [Vi].dbo.[RAT_PREALLOT_H] with (nolock) where PA_NO=d.PA_NO and S_NO_OUT=d.S_NO_OUT and POR_DATE>='201701' and POR_DATE<'201707')");
    where.put("RAT_PREALLOT_S",    "exists(select PA_NO from [Vi].dbo.[RAT_PREALLOT_H] with (nolock) where PA_NO=d.PA_NO and S_NO_OUT=d.S_NO_OUT and POR_DATE>='201701' and POR_DATE<'201707')");
    where.put("REC_D",             "exists(select RC_NO from [Vi].dbo.REC_H with(nolock) where RC_NO=d.RC_NO and RC_DATE>='201701' and RC_DATE<'201707')");
    where.put("REC_H",             "RC_DATE>='201701' and RC_DATE<'201707'");
    where.put("S_STOR_BOM",        "SPK_DATE>='201701' and SPK_DATE<'201707'");
    where.put("S_STOR_D",          "exists(select SST_NO from [Vi].dbo.S_STOR_H with (nolock) where S_NO=d.S_NO and SST_NO=d.SST_NO and SPK_DATE>='201701' and SPK_DATE<'201707')");
    where.put("S_STOR_H",          "SPK_DATE>='201701' and SPK_DATE<'201707'");

    where.put("CASH_MGMT",          "CA_DATE>='201701' and CA_DATE<'201707'");
    where.put("CWSEND_D",          "exists(select WD_NO from [Vi].dbo.[CWSEND_H] with (nolock) where WD_NO=d.WD_NO and S_NO_OUT=d.S_NO_OUT and WD_DATE>='200101' and WD_DATE<'201707')");
    where.put("CWSEND_H",          "WD_DATE>='200101' and WD_DATE<'201707'");
    where.put("CCR_UPDATE_LOG",    "CRE_DATE >= convert(nvarchar(8),dbo.GetLocalDate()-365,112)");
    where.put("DUTY",    "CRE_DATE>='201712' and CRE_DATE<'201802'");
    where.put("DUTY_HR_LOG",    "CRE_DATE>='201701' and CRE_DATE<'201707'");
    where.put("GIFT_LIFE",         "GI_DATE>='200101' and GI_DATE<'201707'");
    where.put("GIFT_PAY",          "GP_DATE>='200101' and GP_DATE<'201707'");
    where.put("ORDER_GIFT_DATA",          "SL_DATE>='201701' and SL_DATE<'201707'");
    where.put("POINT_V_LOG",       "PTL_DATE>='200101' and  PTL_DATE<'201701'");
    where.put("IC_GIFT_INIT",      "INIT_DATE>='200101' and INIT_DATE<'201707'");
    where.put("POR_GIFT_DATA",     "POR_DATE>='201701' and POR_DATE<'201707'");
    where.put("IC_GIFT_RECHARGE",  "OPT_TIME>='200101' and OPT_TIME<'201707'");
    where.put("SALE_CARD",         "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_CARD_PAY",     "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_D",            "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_D_BOM",        "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_DIS",          "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_DP",           "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_H",            "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_INFO",         "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_ORDER_CARD",   "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_ORDER_D",      "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_ORDER_DIS",    "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_ORDER_H",      "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SALE_SL_NO",        "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("SCRAP_D",           "exists(select SC_NO from [Vi].dbo.[SCRAP_H] with (nolock) where SC_NO=d.SC_NO and S_NO=d.S_NO and SC_DATE>='201701' and SC_DATE<'201707')");
    where.put("SCRAP_H",           "SC_DATE>='201701' and SC_DATE<'201707'");
    where.put("SELLGT_D",          "exists(select ST_NO from [Vi].dbo.[SELLGT_H] with (nolock) where ST_NO=d.ST_NO and S_NO=d.S_NO and ST_DATE>='200101' and ST_DATE<'201707')");
    where.put("SELLGT_H",          "ST_DATE>='200101' and ST_DATE<'201707'");
    where.put("SELLGT_SE",         "exists(select ST_NO from [Vi].dbo.[SELLGT_H] with (nolock) where ST_NO=d.ST_NO and S_NO=d.S_NO and ST_DATE>='200101' and ST_DATE<'201707')");
    where.put("TRAN",              "SL_DATE>='200101' and SL_DATE<'201707'");
    where.put("TRAN_D",            "exists( select TR_NO from [Vi].dbo.[TRAN_H] with (nolock)  where TR_NO=d.TR_NO and S_NO_OUT=d.S_NO_OUT and TR_DATE>='201701' and TR_DATE<'201707')");
    where.put("TRAN_H",            "TR_DATE>='201701' and TR_DATE<'201707'");
    where.put("USELESS_D",         "exists(select US_NO from [Vi].dbo.[USELESS_H] with (nolock) where US_NO=d.US_NO and S_NO=d.S_NO and US_DATE>='201701' and US_DATE<'201707')");
    where.put("USELESS_H",         "US_DATE>='201701' and US_DATE<'201707'");
    where.put("WSORD_D",           "exists(select WO_NO from [Vi].dbo.WSORD_H with (nolock) where WO_NO=d.WO_NO and S_NO=d.S_NO and WO_DATE>='200101' and WO_DATE<'201707')");
    where.put("WSORD_H",           "WO_DATE>='200101' and WO_DATE<'201707'");
    where.put("DOWNLOAD_LOG",     "0=1");
    where.put("CUST_V_EDT_LOG",   "0=1");
    where.put("COUNT_D_REPEAT",   "0=1");
    where.put("CARD",              "0=1");
    where.put("CUST_V_QRY_LOG",   "0=1");
    where.put("ERR_DATA",          "0=1");
    where.put("ERROR_LOG",         "0=1");
    where.put("FLOWLOG",            "0=1");
    where.put("PART_LOG",           "0=1");
    where.put("RAT_PREALLOT_P_LOG","0=1");
    where.put("TRAN_LOG",           "0=1");
    where.put("UPLOAD_LOG_D",       "0=1");
    where.put("UPLOAD_LOG_H",       "0=1");
    where.put("USERLOG",             "0=1");
    where.put("PO_D",                 "exists(select POR_NO from [Vi].dbo.[POR] with (nolock) where POR_NO=d.POR_NO and S_NO=d.S_NO and POR_DATE>='201701' and POR_DATE<'201707') or exists(select PO_NO from [Vi].dbo.[PO_H] with (nolock) where PO_NO=d.PO_NO and S_NO=d.S_NO and PO_DATE>='201701' and PO_DATE<'201707')");

    return where;
  }

  public void genUpgradeSql(ServletContext servlet, String sourceDB, String sourceDbName, HttpServletResponse response) throws Exception{
    emisDb source = null;
    emisDb target = null;
    Statement sourceStmt = null;
    ResultSet sourceRs  = null;
    try{
      response.setContentType("application/x-msdownload");
      response.setHeader("Content-Disposition", "attachment;filename=upgrade.txt");
      String excludeTables = "'AUCTIONUSER','AUCTIONITEM','BID','AGE_SEG','BUSINESSBEANS','CARD_LEVEL','CITY','DOWNLOAD','EMISPROP'," +
          "'FIELDFORMAT','FLOW_MENUS','FLOW_USERRIGHTS','FLOWITEMS','FLOWS','FUNC_NAME','FUNCTIONS','GROUPITEM','MENUFUNCS','MENUGROUPS'," +
          "'MENUS','MONTHCLOSE','NUMS','PROPERTIES','PROVINCE','REGION_B','RPT_D','RPT_M','SCHED','SQLCACHE','SYSTAB_D','SYSTAB_H','TAB_D'," +
          "'TAB_H','USERGROUPS','USERRIGHTS','USERS','PAY_SET'";
      source = emisDb.getInstance(servlet, sourceDB);
      target = emisDb.getInstance(servlet);

      ResultSet rs = source.executeQuery("select object_name (i.ID) as TNAME, i.ROWCNT, objectproperty(i.ID,'TableHasIdentity') as hasIdentity \n" +
              "from sysindexes i \n" +
              "inner join sysObjects o  on (o.id = i.id and o.xType = 'U ') \n" +
              "where indid < 2  and rows > 0 and object_name (i.ID) not in(" + excludeTables +") \n" +
              "order by 1"
      );
      List<String> sourceColName =  new ArrayList<String>();
      List<String> targetColName =  new ArrayList<String>();
      StringBuffer notExistInSource = new StringBuffer();
      StringBuffer notExistInTarget = new StringBuffer();
      StringBuffer notExistTable = new StringBuffer("--目标数据库不存在的表：");
      StringBuffer syncSql = new StringBuffer();
      StringBuffer sbTmp = new StringBuffer();
      String tmpSyncSql = "", tabname = "";
      Map<String,String> where = getWhereMap();
      sourceStmt = source.createStatement();
      sourceRs  = null;
      while(rs.next()){
        tabname = rs.getString("TNAME").toUpperCase();
        // source.executeQuery("select * from " + tabname + " where 1 = 0");
        sourceRs = sourceStmt.executeQuery("select * from [" + tabname + "] where 1 = 0");
        try{
          target.executeQuery("select * from [" + tabname + "] where 1 = 0");

          tmpSyncSql = "insert into [" + tabname +"] (%replaceColumn%) \n" +
              "select %replaceColumn% \n" +
              "from " + sourceDbName + ".dbo.["  + tabname +"] d with (nolock)\n" +
              //"where 1=0 --and %" +tabname +"%\n" +
              "where " + (where.containsKey(tabname)?where.get(tabname):"1=1") + "\n" +
              "GO\n";

          for(int i = 1; i<=sourceRs.getMetaData().getColumnCount();i++){
            sourceColName.add(sourceRs.getMetaData().getColumnName(i));
          }
          for(int i = 1; i<=target.getColumnCount();i++){
            targetColName.add(target.getColumnName(i));
          }
          for(int i=0; i < sourceColName.size(); i++){
            if(targetColName.contains(sourceColName.get(i))){
              if(sbTmp.length() > 0 ) sbTmp.append(",");
              sbTmp.append("[").append(sourceColName.get(i)).append("]");
            } else{
              notExistInTarget.append(sourceColName.get(i)).append(",");
            }
          }
          syncSql.append("/**").append(tabname).append(": ").append(rs.getInt("ROWCNT")).append(" **/\n");
          if(rs.getInt("hasIdentity") == 1){
            syncSql.append("set identity_insert [").append(tabname).append("] ON\nGO\n");
          }
          syncSql.append(tmpSyncSql.replaceAll("%replaceColumn%", sbTmp.toString()));
          if(rs.getInt("hasIdentity") == 1){
            syncSql.append("set identity_insert [").append(tabname).append("] OFF\nGO\n");
          }
          for(int i=0; i < targetColName.size(); i++){
            if(!sourceColName.contains(targetColName.get(i))){
              notExistInSource.append(targetColName.get(i)).append(",");
            }
          }
          if(notExistInSource.length() > 0 || notExistInTarget.length() > 0){
            syncSql.append("/*").append("\n");
            if(notExistInTarget.length() > 0) {
              syncSql.append("--目标数据库表不存在的字段：").append(notExistInTarget.toString()).append("\n");
            }
            if(notExistInSource.length() > 0) {
              syncSql.append("--源数据库表不存在的字段：").append(notExistInSource.toString()).append("\n");
            }
            syncSql.append("*/").append("\n");
          }
          response.getOutputStream().write(syncSql.toString().getBytes());
          response.getOutputStream().flush();
          sourceColName.clear();
          targetColName.clear();
          notExistInSource.setLength(0);
          notExistInTarget.setLength(0);
          syncSql.setLength(0);
          sbTmp.setLength(0);
          if(sourceRs != null){
            sourceRs.close();
            sourceRs = null;
          }
        } catch(Exception e) {
          notExistTable.append(tabname).append(" , ");
        }
      }
      response.getOutputStream().write(notExistTable.toString().getBytes());
      response.getOutputStream().flush();
      notExistTable.setLength(0);
    } finally {
      try{
        if(sourceStmt != null){
          sourceStmt.close();
        }
        if(sourceRs != null){
          sourceRs.close();
        }
      } catch(Exception e) {}
      if(source != null) source.close();
      if(target != null) target.close();
    }
  }

  public void copyFile(String fromPath, String toPath, boolean bDelete, Writer out) throws IOException {
    File fDir = new File(fromPath);
    if(!fDir.exists()) {
      out.write("指定目录或文件不存！>>" + fromPath +"<br>");
      return; //指定目录不存，不继续往下处理。
    }
    File tmpDir = fDir;
    boolean breakFor = false;
    // 只复制一个文件
    if(fDir.isFile()) {
      breakFor = true;
      tmpDir = new File(fDir.getParent());
    }

    for(File f : tmpDir.listFiles()){
      if(breakFor) f = fDir;

      if(f.isDirectory()){
        copyFile(fromPath + "/" + f.getName(), toPath + "/" + f.getName(), bDelete, out);
      } else {
        try {
          if(emisUtil.copyFile(f, toPath).exists()){
            out.write("文件复制成功 >>" + f.getPath() +" >>> " + toPath +"<br>");
            if(bDelete && f.delete()){
              out.write("源文件删除成功 >>" + f.getPath()  +"<br>");
            }
          }
        } catch (Exception e) {
          out.write("文件复制失败 >>" + f.getPath() +" >>> " + toPath +"<br>" + e.getMessage() +"<br>");
        }
        if(breakFor) break;
      }
    }
  }

  public void checkFileDate(String dir, String when, String exclude, StringBuffer out, SimpleDateFormat dateFormater){
    String name = "";
    File fDir = new File(dir);
    if(!fDir.exists()) return; //指定目录不存，不继续往下处理。

    for(File f : fDir.listFiles()){
      name = f.getName();
      if(f.isDirectory()) {
        // 如果目录为设定的排除目录则不作处理
        if (exclude.indexOf(";" + name + ";") >= 0) continue;
        else checkFileDate(f.getPath(), when, exclude, out, dateFormater);
      } else {
        Date fileDate = new Date(f.lastModified());
        //String temp =  dateFormater.format(fileDate);
        //if(when.compareTo(fileDate) <=0 ){
        if(dateFormater.format(fileDate).compareTo(when) >=0 ){
          out.append(f.getPath()).append("\r\n");
        }
      }
    }
  }

  public void getUpdateFiles(HttpServletResponse response, String rootDir, String whenDate) throws IOException {
    //dir = "D:\\projects\\jijitown2\\wwwroot";
    //whenDate = "2017-01-03 11:10";
    System.out.println("********* " + whenDate);
    response.setContentType("application/x-msdownload");
    response.setHeader("Content-Disposition", "attachment;filename=Files.txt");
    String exclude=".svn;cache;data;dynamic;logs;report_def;report_out;users;migration;tmp;work;version;sql";
    int whenY = Integer.parseInt(whenDate.substring(0,4)) - 1900;
    int whenM = Integer.parseInt(whenDate.substring(5,7)) - 1;
    int whenD = Integer.parseInt(whenDate.substring(8,10));
    int whenH = Integer.parseInt(whenDate.substring(11,13));
    int whenMin = Integer.parseInt(whenDate.substring(14,16));
    //Date when = new Date( whenY, whenM,whenD,whenH, whenMin);
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-ddHH:mm");
    dateFormater.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    StringBuffer out =  new StringBuffer();
    exclude = ";" + exclude + ";";
    checkFileDate(rootDir, whenDate, exclude, out, dateFormater);
    response.getOutputStream().write(out.toString().getBytes());
    response.flushBuffer();
    out.setLength(0);
  }
%>

<%
  //if(1==1) genMS_Description(application, out);
  String sAct = request.getParameter("ACT");
  String dir = request.getParameter("DIR");
  String isCheckUtf8 = request.getParameter("CHK_UTF8");

  //String cmd = request.getParameter("CMD");
  String db = request.getParameter("DB");
  if("".equals(db)) db  = null;
  /*
  if(StringUtils.isNotEmpty(cmd)){
    execCMD(cmd, out);
    return;
  }
  */
  if(sAct != null && "COPY".equalsIgnoreCase(sAct)){
    // 将wwwroot目录下指定子目录下的文件复制到wwwroot目录下的其它子目录下
    String sourcePath = request.getParameter("BACK_PATH");
    String descPath = request.getParameter("COPY_PATH");
    String zipPath = request.getParameter("ZIP_PATH");
    String zipName = request.getParameter("ZIP_NAME");

    emisFileMgr _oFileMgr = emisFileMgr.getInstance(application);
    emisDirectory backFileDir = _oFileMgr.getDirectory("root").subDirectory(sourcePath);
    emisDirectory copyToDir = _oFileMgr.getDirectory("root").subDirectory(descPath);
    emisZipUtil zip = null;
    if(zipPath != null && !"".equalsIgnoreCase(zipPath) && zipName != null && !"".equals(zipName)) {
      emisDirectory zipDir = _oFileMgr.getDirectory("root").subDirectory(zipPath);
      zip = new emisZipUtil(zipDir.getDirectory() + "//" + zipName);
    }
    try {
      copyBackFiles(out, backFileDir, copyToDir, zip);
    } finally{
      if(zip!=null) zip.close();
    }
    return;
  }


  if(sAct != null && "COPY2".equalsIgnoreCase(sAct)){
    //将服务器上指目录下的文件复制其它的目录下（不限制在wwwroot目录下）

    /**
     * http://localhost:8082/V3/jsp/dev/database/runSqlscript.jsp?ACT=COPY2&FROM=D:/projects/V3/trunk/wwwroot/WEB-INF/classes/com/runqian&TO=D:/temp/test
     * http://localhost:8082/V3/jsp/dev/database/runSqlscript.jsp?ACT=COPY2&FROM=@/report_out/emisCleanUp.class&TO=@user.dir/emis/classes/com/emis/schedule/cleanup&DELETE=Y
     * @/ - 是指wwwroot后的相对目录; @user.dir/ - 是指resin的安装目录
     */
    String copyFrom = request.getParameter("FROM");
    String copyTo = request.getParameter("TO");
    String isDeleteFrom = request.getParameter("DELETE");

    copyFrom = emisUtil.stringReplace(copyFrom, "@user.dir", System.getProperty("user.dir"), "a");
    copyTo = emisUtil.stringReplace(copyTo, "@user.dir", System.getProperty("user.dir"), "a");
    emisFileMgr oFMgr_ = emisFileMgr.getInstance(application);
    emisDirectory oDirRoot_ = oFMgr_.getFactory().getDirectory("root");
    System.out.println(copyFrom);
    System.out.println(copyFrom.substring(1));
    if(copyFrom.startsWith("@")){
      copyFrom = oDirRoot_.getDirectory() + copyFrom.substring(1);
    }
    if(copyTo.startsWith("@")){
      copyTo = oDirRoot_.getDirectory() + copyTo.substring(1);
    }
    copyFile(copyFrom, copyTo, "Y".equalsIgnoreCase(isDeleteFrom), out);
    return;
  }

  // 导出成insert into 语句
  if(sAct != null && "EXPORT".equals(sAct)){
    /**
     * http://localhost:8082/V3/jsp/dev/database/runSqlscript.jsp?ACT=EXPORT&TABLE_NAME=GIFT_TOKEN&EXPORT_SQL=select * from GIFT_TOKEN where GT_NO='190123'
     * encodeURIComponent();
     */
    String exportSql = request.getParameter("EXPORT_SQL");
    if(exportSql == null || "".equals(exportSql)) exportSql = "select * from " + request.getParameter("TABLE_NAME");
    out.clear();
    exportData(application, request.getParameter("TABLE_NAME"), exportSql, response);
    return;
  }

  // 批次执行 insert into 语句
  if(sAct != null && "IMPORT".equals(sAct)){
    String path = request.getParameter("SQL_DIR");
    batImportData(application, out, path, db);
    return;
  }

  // 批次执行 insert into 语句
  if(sAct != null && "CMD".equals(sAct)){
    execCMD(request.getParameter("CMD"), out);
    return;
  }

  if(sAct != null && "JIJITOWN".equalsIgnoreCase(sAct)) {
    out.clear();
    //jijitownMoveErrorFiles(application, out);
    moveEndofdayFilesForJIJITOWN(application, out);
    out.flush();
    return;
  }

  if(sAct != null && "GMX".equalsIgnoreCase(sAct)) {
    //供美香发生客订档解档时出现SALE_H主键重复的问题，重转档案又可以，暂无法模拟这种情况，先用重解档的方式处理。
    out.clear();
    gmxMoveErrorFiles(application, out);
    out.flush();
    return;
  }
  // 导出成insert into 语句
  if(sAct != null && "UPGRADE".equals(sAct)){
    /**
     * http://localhost:8082/V3/jsp/dev/database/runSqlscript.jsp?ACT=UPGRADE&S_DB=rosa2&S_DB_NAME=rosa2
     */
    if(request.getParameter("S_DB") == null || "".equals(request.getParameter("S_DB"))
        || request.getParameter("S_DB_NAME") == null || "".equals(request.getParameter("S_DB_NAME"))){
      out.write("请求参数不全！（S_DB/S_DB_NAME）");
      return;
    }
    out.clear();
    genUpgradeSql(application, request.getParameter("S_DB"), request.getParameter("S_DB_NAME"), response);
    return;
  }

  // 获取指定目录下指定开始日期之后有异动的文件列表
  if(sAct != null && "FILES".equals(sAct)){
    /**
     * http://localhost:8080/vi/jsp/dev/database/runSqlscript.jsp?ACT=FILES&dir=d:\projects\vi\wwwroot\&fromDate=2017-01-03 00:00
     * encodeURIComponent();
     */
    String rootDir = request.getParameter("dir");
    String fromDate = request.getParameter("fromDate");
    out.clear();
    getUpdateFiles(response,rootDir,fromDate);
    return;
  }
  // 手动执行GC处理
  if("GC".equalsIgnoreCase(sAct)){
    System.gc();
    out.clear();
    String imageName  = request.getParameter("IMAGENAME");
    if(imageName != null && !"".equals(imageName)){
      Process proc = null;
      BufferedReader br = null;
      try {
        proc = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq "+imageName+"\" /FO CSV /NH");
        br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        //String info = br.readLine();
        out.print(br.readLine());
        /*
        while (info != null) {
          info = br.readLine();
        }
        */
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if(proc != null) {
          proc.destroy();
          proc = null;
        }
        if(br != null){
          br.close();
          br = null;
        }
      }
    }
    return;
  }

  if(StringUtils.isEmpty(dir)) {
    dir = "data/sqlscript";
  }
  emisFileMgr _oFileMgr = emisFileMgr.getInstance(application);
  emisDirectory _oDir = _oFileMgr.getDirectory("root").subDirectory(dir);
  exec2(application, out, _oDir.getDirectory(), db);
%>