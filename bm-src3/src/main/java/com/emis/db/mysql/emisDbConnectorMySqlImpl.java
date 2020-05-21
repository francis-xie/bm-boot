package com.emis.db.mysql;import com.emis.db.emisSQLCache;import com.emis.spool.emisPoolable;import com.emis.trace.emisTracer;import javax.servlet.ServletContext;import javax.sql.PooledConnection;import java.sql.*;import java.util.Properties;/** * MySql connection * */public class emisDbConnectorMySqlImpl extends com.emis.db.emisDbConnectorJDBC20 {  public emisDbConnectorMySqlImpl(ServletContext oContext, Properties props) throws Exception {    super(oContext, props);  }  @Override  protected void createSource(Properties props) throws Exception {//    OracleConnectionPoolDataSource _Ocpds = new OracleConnectionPoolDataSource();    com.mysql.cj.jdbc.MysqlConnectionPoolDataSource pds = new com.mysql.cj.jdbc.MysqlConnectionPoolDataSource();    String _url = (String) props.get("url");    String _username = props.getProperty("username", "");    String _password = props.getProperty("password", "");    String _charset = props.getProperty("encoding");    if (_charset == null) _charset = "UTF-8";    sDbCharset_ = _charset;    pds.setURL(_url);    pds.setUser(_username);    pds.setPassword(_password);    this.oDataSource_ = pds;  }  @Override  public emisPoolable generateRealPooledObject(int nTimeOut) throws Exception {    PooledConnection _oPool = null;    emisPoolable _oPooled = null;    try {      oDataSource_.setLoginTimeout(nTimeOut);      _oPool = oDataSource_.getPooledConnection();      _oPooled = new emisConnectProxyMySql(oContext_, _oPool, this);      return _oPooled;    } catch (SQLException ignore) {      emisTracer.get(oContext_).warning(ignore.getMessage());      throw ignore;    }  }  @Override  public String getSequenceNumber(Connection oConn, String sSequenceName, boolean checkAutoDrop, String sDropCondition, String sFormat) throws SQLException {    // TODO Auto-generated method stub    return null;    /*CallableStatement _oCall = oConn.prepareCall("begin ? := eposgetformatedseq(?,?,?,?); end;");    try {      _oCall.registerOutParameter(1, Types.VARCHAR);      _oCall.setString(2, sSequenceName);      _oCall.setInt(3, checkAutoDrop ? 1 : 0);      _oCall.setString(4, sDropCondition);      _oCall.setString(5, sFormat);      _oCall.execute();      return _oCall.getString(1);    } finally {      _oCall.close();      _oCall = null;    }*/  }  @Override  public String getStoreSequence(Connection oConn, String sSequenceName, String sSNO, String sDropCondition, String sFormat) throws SQLException {    // TODO Auto-generated method stub    return null;    /*if (sDropCondition == null)      sDropCondition = "";    CallableStatement call = oConn.prepareCall("begin ?:=eposGetStoreSeq(?,?,?,?); end;");    try {      call.registerOutParameter(1, Types.VARCHAR);      call.setString(2, sSNO);      call.setString(3, sSequenceName);      call.setString(4, sDropCondition);      call.setString(5, sFormat);      call.execute();      String _sSeqValue = call.getString(1);      return _sSeqValue;    } finally {      try {        call.close();      } catch (Exception ignore) {      }    }*/  }  @Override  public void expireSQLCache(Connection oConn, String sSQLName) throws SQLException {    PreparedStatement pstmt = oConn.prepareStatement("UPDATE " + emisSQLCache.SQLCACHETABLE + " SET LASTUPDATE=sysdate() WHERE SQLNAME=?");    try {      pstmt.setString(1, sSQLName);      pstmt.executeUpdate();    } finally {      pstmt.close();      pstmt = null;    }  }  /**   * 將錯誤碼轉換成字串, 以利使用者描述與理解.   */  public String errToMsg(SQLException e) {    if (e == null) return null;    int nErrCode = e.getErrorCode();    if (nErrCode == 1)      return "编号重复:请重新输入(1)";    return e.getMessage() + "(" + nErrCode + ")";  }  public boolean isPKError(SQLException e) {    if (e == null) return false;    int nErrCode = e.getErrorCode();    if (nErrCode == 1)      return true;    return true;  }  public String dualTestSQL() {    return "SELECT 1";  }  /**   * 获取数库的类型(如一种驱动可面向多种不同数据请写不同的Impl)   *   * @return   */  public String getDBType() {    return "mysql";  }}