<!-- $Id: dbqry.jsp 13502 2019-01-10 06:18:41Z harry.chen $

-->
<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="com.emis.db.*,java.sql.*,

                 com.emis.file.emisFile,
                 com.emis.file.emisFileMgr,
                 java.io.PrintWriter,

                 com.emis.file.emisDirectory" %>
<%@ page import="com.emis.app.migration.action.emisMiEncrypt" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.emis.user.emisUser" %>
<%@ page import="com.emis.user.emisCertFactory" %>
<%@ page import="com.emis.util.emisUtil" %>
<%@ page import="com.emis.user.emisPermission" %>

<%!
  PrintWriter oWriter_ = null;
  ResultSet oRS_ = null;
  boolean isCheckSchema_ = false;  // 是否要产生栏位结构?
  String sSchema_ = "";  // 栏位结构存放之字串
  String sComma_ = "";  // 栏位结构分隔字元存放之字串
%>
<%
	String _sPasswd = request.getParameter("PASSWD");
  if ("".equals(_sPasswd)) {
 		_sPasswd = (String) session.getAttribute("PASSWD");
  } else {
 		session.setAttribute("PASSWD", _sPasswd);
  }
  //boolean _isOK = emisDbQryLogin.login(application, _sPasswd);
  //if (!_isOK) {

  //取得年月日,計算公式所需要的值 y d m
  Calendar date = Calendar.getInstance();
  String day = date.get(Calendar.DAY_OF_MONTH)+"";
  String month = (date.get(Calendar.MONTH) + 1)+"";
  String year = date.get(Calendar.YEAR)+"";
  String y =  year.toString().substring(year.length()-1);
  String d =  day.toString().substring(day.length()-1);
  String m = month;
  if(month.equals("10")){
    m="a";
  }else if(month.equals("11")){
    m="b";
  }else if(month.equals("12")){
    m="c";
  }
  //獲取輸入的密碼,取qydm前面進行加密
  emisMiEncrypt encrypt = new emisMiEncrypt();

  String sPassWD = _sPasswd;
  String sPassWD1 = "";
  String sPassWD2 = "";
  if(sPassWD.indexOf(y + d + m) > 0){
    sPassWD1 = sPassWD.substring(0, sPassWD.indexOf(y + d + m));
    sPassWD2 = sPassWD.substring(sPassWD.indexOf(y + d + m));
  }
  String _sPassWord[] = {sPassWD1};
  String _sPWD = encrypt.act(_sPassWord, _sPassWord);
  _sPWD = _sPWD + sPassWD2;

  //取得root的密碼,
  emisDb oDb_ = null;
  PreparedStatement queryPreStmt_ = null;
  ResultSet _oRs = null;
  String sRes_ = "";
  try {
    oDb_ = emisDb.getInstance(application);
    String sSql_ = "select UPPER(USERID) USERID,PASSWD,USERNAME,S_NO from users\n"
      + " where UPPER(USERID)='root' ";
    queryPreStmt_ = oDb_.prepareStmt(sSql_);
    _oRs = queryPreStmt_.executeQuery();
    _oRs.next();
    if (_oRs.getRow() > 0) {
      sRes_ = _oRs.getString("PASSWD");
    }
  } catch (Exception e) {
    e.printStackTrace();
  } finally {
    try {
      if (queryPreStmt_ != null) oDb_.closePrepareStmt(queryPreStmt_);
      if (oDb_ != null && !oDb_.isClosed()) oDb_.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  System.out.println(sRes_);
  System.out.println(sRes_+y+d+m);
  System.out.println(_sPWD);
  if ((sRes_+y+d+m).equals(_sPWD) || (sRes_+y+d+m).equals(sPassWD)){}
    else{
    out.println("<h1>密碼錯誤，無法使用資料查詢功能</h1>");
    return;
    }
  String sql = request.getParameter("SQL");
  String sTextSQL = (sql==null) ? "" : sql;
  if (sTextSQL.endsWith(";")) {
    sTextSQL = sTextSQL.substring(0, sql.length()-1);
  }

  int stmttype= 0;

  String _sType = request.getParameter("stmttype");
  String _sStmtPwd = request.getParameter("stmtpwd");
  String _sChkPwd = "";

  if( _sType == null )

    _sType = "0";
  stmttype = Integer.parseInt(_sType);

  if (!"turbo".equals(_sStmtPwd) && (stmttype == 3 || stmttype == 4) ) {
    _sChkPwd = "NO";
  }

  String sP1 = request.getParameter("P1");
  String sP2 = request.getParameter("P2");
  String sP3 = request.getParameter("P3");
  String sDB_NAME = request.getParameter("DB_NAME");
  String _sSaveFile = request.getParameter("IS_SAVE");
  String _sChkSchema = request.getParameter("CHK1");

  if( sP1 == null ) sP1 = "";
  if( sP2 == null ) sP2 = "";
  if( sP3 == null ) sP3 = "";
  if( sDB_NAME == null ) sDB_NAME = "";
  if( _sChkSchema == null ) _sChkSchema = "";
  boolean _isSaveFile = _sSaveFile != null;
  isCheckSchema_ = _sChkSchema.equals("1");
  sSchema_ = "";
  sComma_ = "";

  boolean isQuery = true;
  if ((stmttype == 1 || stmttype == 2) && sTextSQL.toLowerCase().indexOf("/*query*/")<0) {
    if (sTextSQL.toLowerCase().indexOf("delete") >= 0) {
      isQuery = false;
    out.println("<TABLE border='0'  bgcolor='yellow'>");
    out.println("<TR><TD>＊＊查询功能禁止使用 DELETE 语法＊＊</TD></TR>");
    }
    if (sTextSQL.toLowerCase().indexOf("update") >= 0) {
      isQuery = false;
    out.println("<TABLE border='0'  bgcolor='yellow'>");
    out.println("<TR><TD>＊＊查询功能禁止使用 UPDATE 语法＊＊</TD></TR>");
    }
   if (sTextSQL.toLowerCase().indexOf("create") >= 0) {
      isQuery = false;
    out.println("<TABLE border='0'  bgcolor='yellow'>");
    out.println("<TR><TD>＊＊查询功能禁止使用 CREATE 语法＊＊</TD></TR>");
    }
   if (sTextSQL.toLowerCase().indexOf("drop") >= 0) {
      isQuery = false;
    out.println("<TABLE border='0'  bgcolor='yellow'>");
    out.println("<TR><TD>＊＊查询功能禁止使用 DROP 语法＊＊</TD></TR>");
    }
   if (sTextSQL.toLowerCase().indexOf("truncate") >= 0) {
      isQuery = false;
    out.println("<TABLE border='0'  bgcolor='yellow'>");
    out.println("<TR><TD>＊＊查询功能禁止使用 TRUNCATE 语法＊＊</TD></TR>");
    }
   if (sTextSQL.toLowerCase().indexOf("insert") >= 0) {
      isQuery = false;
    out.println("<TABLE border='0'  bgcolor='yellow'>");
    out.println("<TR><TD>＊＊查询功能禁止使用 INSERT 语法＊＊</TD></TR>");
    }
  }
  
%>
<%
//  emisUser _oUser = emisCertFactory.getUser(application, request);
  String _sTitle = "";
  String _skeys = "";
  //String _sTaskName = emisUtil.getTaskName(session, _skeys);

    /*固定寫法：設定新增、修改。。。權限，ajax_inc.htm中引用*/
//  emisPermission _oPermission = null;
//  emisProp oProp_ = emisProp.getInstance(application);
%>

<html>
<head>
  <style>
    table { font:10pt Arial; }
    .odd_row  { background-color:lightblue; }
    .even_row { background-color:lightyellow; }
    .smallfont { font-size:8pt; font-family: 'Arial'; }
    .data_table {
      width:100%;
      border: #d0d0d0 1px solid;
      margin: 0 auto;
      background: #ffefd5;
      border-collapse: collapse;
    }
  </style>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
  <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
  <META HTTP-EQUIV="Expires" CONTENT="0">
  <title>SQL 数据库测试</title>
  <%--<%@include file="../../ajax_inc.htm"%>--%>
  <script type="text/javascript" src="<%= request.getContextPath()%>/js/jquery-1.4.2.min.js"></script>
  <script type="text/javascript" src='<%=request.getContextPath()%>/js/lang/emis-lang_<%=(String)session.getAttribute("languageType")%>.js'></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/ajax.js"></script>
  <%@include file="../../ymPrompt_inc.htm"%>
</head>
<body>
  <form id=idform action='dbqry.jsp' method='post' target='_self'>
  <input type='Hidden' name='PASSWD' value='<%=_sPasswd%>'>
  <table class="data_table">
    <tr><td>
      <input type=checkbox value="1" name=IS_SAVE>另存新档?
      <a href="../../../data/tmp/<%= session.getId()+".txt" %>">下载档案</a>
      <input type=checkbox name=CHK1 value="1">产生结构?
    </td></tr>
    <tr><td>

        <textarea rows=15 cols=100 name=SQL><%=sTextSQL%></textarea><BR>

        param1<input type="text" name=P1 value="<%=sP1%>"> (Used for prepared statement)<BR>
        param2<input type="text" name=P2 value="<%=sP2%>"><BR>
        param3<input type="text" name=P3 value="<%=sP3%>"><BR>
        DB<input type="text" name="DB_NAME" value="<%=sDB_NAME%>"><BR>

        <input type=hidden name="stmttype" value="1">
        <input type=hidden name="stmtpwd" value="">



        <input type=button value="Execute Query[F9]" title="[F9]/[CTRL-ENTER]:Execute Query" onclick="setValue(1);">
        <input type=button value="Prepared Query"    title="[F6]:Prepared Query" onclick="setValue(2);">
        <input type=button value="Statement Update"  title="[F7]:Statement Update" onclick="setValue(3);">
        <input type=button value="Prepared Update"   title="[F8]:Prepared Update" onclick="setValue(4);">
    </td></tr>
    <div width="100%">
    <tr><td>
<%
  out.println("check=" + _sChkSchema+"$");
    try {
      if( sTextSQL != null )
      {
        lStart_ = System.currentTimeMillis();
        emisDb db = null;
        oRS_ = null;
        oWriter_ = null;
        emisFileMgr _oFileMgr = emisFileMgr.getInstance(application);
        emisDirectory _oDir = _oFileMgr.getDirectory("root").subDirectory("data").subDirectory("tmp");
        if (_isSaveFile) {
          oWriter_ = _oFileMgr.getWriter(_oDir, session.getId()+".txt","w");
        } else {
          emisFile _oFile = _oDir.getFile(session.getId()+".txt");
          if (_oFile.exists())
            _oFile.delete();
        }
        try {
          if (sDB_NAME != null && !"".equals(sDB_NAME)){
            db = emisDb.getInstance(application, null, sDB_NAME);
          } else {
            db = emisDb.getInstance(application);
          }

          db.setDescription("developer test");
          //-out.println("Connection="+db.getConnectionIdentifier());

          if( stmttype == 1 && isQuery) // Execute Query

          {
            oRS_ = db.executeQuery(sTextSQL);
            showData(out,db);
          }
          else

          if( stmttype == 2 && isQuery) // Prepared Query

          {
            db.prepareStmt(sTextSQL);
            if( !"".equals(sP1) )
            {
              db.setString(1,sP1);
              if(!"".equals(sP2))
              {
                db.setString(2,sP2);
                if(!"".equals(sP3))
                {
                  db.setString(3,sP3);
                }
              }
            }

            oRS_ = db.prepareQuery();
            showData(out,db);
          }
          else

          if( stmttype == 3 && "turbo".equals(_sStmtPwd)) // Statement Update

          {

            out.println("Updated:"+db.executeUpdate(sTextSQL));
          }
          else

          if( stmttype == 4 && "turbo".equals(_sStmtPwd) ) // Prepared Update

          {
            db.prepareStmt(sTextSQL);
            if( !"".equals(sP1) )
            {
              db.setString(1,sP1);
              if(!"".equals(sP2))
              {
                db.setString(2,sP2);
                if(!"".equals(sP3))
                {
                  db.setString(3,sP3);
                }
              }
            }
            out.println("Updated:"+db.prepareUpdate());
          }
        }catch(Exception e) {
          out.println("<xmp>");
          e.printStackTrace(new PrintWriter(out));
          out.println("</xmp>");
        }finally {
          if (oWriter_ != null) oWriter_.close();
          db.close();
        }
        out.println("<HR>");

        emisDbObjectSpool _oSpool = emisDbObjectSpool.getSpool(application);
        if( _oSpool == null )
        {
          out.println("Spool not exists");
        }
        else
        {
          //- out.println("emisDb Object:" + db.toString() + "<BR>");
          out.println("emisDb Object Spool   Size:" + _oSpool.getSpoolSize()+"<BR>" );
          out.println("Current Pooled Object Size:" + _oSpool.getSpoolObjectSize()+"<BR>" );
        }
      }
    } catch (SQLException sqle) {
      out.println("ERRORCODE:"+sqle.getErrorCode());
      out.println("STATA:"+sqle.getSQLState());
    }
%>
    </td></tr>
    </div>
  </table>
  </form>
<%
  if (isCheckSchema_) {
    out.println("<br><textarea cols=60 rows=10>");
    sSchema_ = sSchema_.substring(0, sSchema_.length()-1);
    out.println(sSchema_);
    out.println("</textarea><br>");
    out.println("<textarea cols=60 rows=10>");
    sComma_ = sComma_.substring(0, sComma_.length()-1);
    out.println(sComma_);
    out.println("</textarea>");
  }
%>
</body>
</html>

<script>
  function setValue(val) {
    if (val == "3" || val == "4") {
      /*var sRetStr = window.showModalDialog("chkPwd.jsp", window,
         "dialogWidth=500px;dialogHeight=200px;"+
         "center=yes;border=thin;help=no; menubar=no;toolbar=no;location=no;directories=no;status=no;resizable=0; scrollbars=0");
      document.all.stmtpwd.value = sRetStr;
      if(sRetStr == "") return;*/
      //emisShowModal("chkPwd.jsp",500,200);
      emisShowDialog({url:"chkPwd.jsp",width:500,height:150,handler:function(ret){
        if(ret!="close"){
          formobj("stmttype").value=val;
          formobj("stmtpwd").value=ret;

          idform.submit();return;
        }
      }});
    }else{
      formobj("stmttype").value=val;
      idform.submit();
    }
    //document.all.stmttype.value = val;
    /*formobj("stmttype").value=val;
    idform.submit();*/
  }
</script>

<%!
private long lStart_;
private void showData(JspWriter out,emisDb db) throws Exception
{
  int cols = db.getColumnCount();
  out.println("columns:" + String.valueOf(cols) + " " + (new Time(lStart_)) + "<BR>");

  out.println("<TABLE class=smallfont border='0' cellspacing='0' bgcolor='blue'>");

  // 显示栏位名称
  out.println("<TR class='odd_row' align='center'>");
  out.println("<TD>0</TD>");
  for(int i=1 ; i<=cols; i++)
  {
    String _sName = db.getColumnName(i);
    out.println("<TD>"+ _sName);
    out.println("</TD>");
    if (oWriter_ != null)
      oWriter_.write(db.getColumnName(i)+",");
    if (isCheckSchema_) {
      sSchema_ += _sName + ",";
      sComma_ += "?,";
    }
  }
  out.println("</TR>");
  if (oWriter_ != null) oWriter_.write("\r\n");

  // 显示栏位属性
  out.println("<TR class='odd_row' align='center'>");
  out.println("<TD class='even_row'></TD>");

  ResultSetMetaData _oMeta = (oRS_ == null) ? null : oRS_.getMetaData();
  for(int i=1 ; i<=cols; i++)
  {
    int iType = db.getColumnType(i);

    switch(iType)
    {
      case Types.VARCHAR:
           out.println("<TD bgcolor='lightgreen'>VARCHAR");
           break;
      case Types.CHAR:
           out.println("<TD bgcolor='lightpink'>CHAR");
           break;
      case Types.LONGVARCHAR:
           out.println("<TD bgcolor='paleturquoise'>LONGVARCHAR");
           break;
      case Types.INTEGER:
           out.println("<TD bgcolor='lightgreen'>INTEGER");
           break;
      case Types.NUMERIC:
           out.println("<TD bgcolor='lightgreen'>NUMERIC");
           break;
      default:
           out.println("<TD bgcolor='darkorange'>OBJECT");
    }
    if (oRS_ != null) {
      out.println("<br>" + _oMeta.getPrecision(i));
      if (iType == Types.NUMERIC)
        out.println("," + _oMeta.getScale(i));
    }
    out.println("</TD>");
  }
  out.println("</TR>");

  // 显示资料内容
  int size = 0;
  while( db.next() )
  {
    size++;
    out.println("<TR class='" + (size%2==0?"even_row" : "odd_row") + "'>");
    out.println("<TD align='center'>"+String.valueOf(size)+"</TD>");

    for(int i=1; i<= cols;i++)
    {
      int type = db.getColumnType(i);
      String name = db.getColumnName(i);

      if( (type == Types.VARCHAR ) ||
          (type == Types.CHAR )    ||
          (type == Types.LONGVARCHAR) )
      {
        String s = (String)db.getString(name);

        if( s == null ) {
          if (oWriter_ != null)
            s = "";
          else
            s ="&nbsp";
        }
        out.print("<TD>"+s+"</TD>");
        if (oWriter_ != null) {
          oWriter_.write(s + ",");
        }
      }
      else if (type == Types.INTEGER )
      {
        out.print("<TD align='right'>"+ String.valueOf(db.getInt(name)) +"</TD>" );
      }
      else if (type == Types.NUMERIC )
      {
        out.print("<TD align='right'>"+ String.valueOf(db.getFloat(name)) +"</TD>" );
      }
      else
      {
        Object obj = db.getObject(name);
        if( obj != null )
          out.println("<TD>" + obj.toString() + "</TD>");
        else
          out.println("<TD></TD>");
      }
    }
    out.println("</TR>");
    if (oWriter_ != null)
      oWriter_.write("\r\n");

    if (size % 100 == 0) out.flush();
  }
  out.println("</TABLE><BR>");
  long _lEnd = System.currentTimeMillis();
  out.println("Records: " + size + " " + (new Time(_lEnd)) + " Elapsed=" + (_lEnd - lStart_) + " Milli-seconds<BR>");
}
%>

<script>
  $(document).ready(function() {
    if ("<%=_sChkPwd%>" == "NO") {
      alert("密碼錯誤，不執行Update動作!");
    }
//    setFocus(["SQL"]);

    jQuery(".smallfont").css("border-collapse","collapse").css("border","none");
    jQuery(".smallfont td").css("border","solid #000 1px");
  });

  $(document).keydown(function(){
    var _iKeyCode=window.event.keyCode;
    // F9 或 Ctrl_Enter
    if (_iKeyCode==120 || (window.event.ctrlKey && _iKeyCode==13)) {
      setValue(1);
    } else if (_iKeyCode==117) {
      setValue(2);
    } else if (_iKeyCode==118) {
      setValue(3);
    } else if (_iKeyCode==119) {
      setValue(4);
    }
  });



</script>


<%--
<script for=document event=onkeydown>
  var _iKeyCode=window.event.keyCode;
  // F9 或 Ctrl_Enter
  if (_iKeyCode==120 || (window.event.ctrlKey && _iKeyCode==13)) {
    setValue(1);
  } else if (_iKeyCode==117) {
    setValue(2);
  } else if (_iKeyCode==118) {
    setValue(3);
  } else if (_iKeyCode==119) {
    setValue(4);
  }
</script>--%>


