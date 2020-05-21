package com.emis.trace;

/**
 * 定義了所有的 Error Number
 */
public final class emisError
{

  public static final int ERR_SVROBJ_SERVER_NO_BINDNAME = 1;
  public static final int ERR_SVROBJ_NOT_BIND           = 2;

  public static final int ERR_SVROBJ_DBMGR_INIT         = 3;
  public static final int ERR_SVROBJ_SQLCACHE_INIT      = 4;
  public static final int ERR_SVROBJ_PROP_INIT          = 5;
  public static final int ERR_SVROBJ_CERT_INIT          = 6;
  public static final int ERR_SVROBJ_BUSINESS_INIT      = 7;
  public static final int ERR_SVROBJ_BUSINESSCACHE_INIT = 8;
  public static final int ERR_SVROBJ_SCHEDULE_INIT      = 9;
  public static final int ERR_SVROBJ_DYNAMICLOADER_INIT = 10;
  public static final int ERR_SVROBJ_DUPLICATE          = 11;
  public static final int ERR_SVROBJ_PROP_RESET         = 12;
  public static final int ERR_SVROBJ_MAILQUEUE_INIT     = 13;
  public static final int ERR_SVROBJ_CIPHER_INIT        = 14;
  public static final int ERR_SVROBJ_AUDIT_INIT         = 15;
  public static final int ERR_SVROBJ_PINGDB_INIT        = 16;

  public static final int ERR_DB_NOGET_CONNECT_FROM_SPOOL = 1001;
  public static final int ERR_DB_NOGET_CMDFACTORY         = 1002;
  public static final int ERR_DB_NOGET_CONNECT_PROXY      = 1003;
  public static final int ERR_DB_NULL_NAME_OF_CMDFACTORY  = 1004;
  public static final int ERR_DB_NULL_DIRECTORYNAME_OF_CMDFACTORY = 1005;
  public static final int ERR_DB_REG_SPOOL           = 1006;
  public static final int ERR_DB_SPOOL_NOT_FOUND          = 1007;
  public static final int ERR_DB_SPOOL_DEFAULT_NOT_DEFINED= 1008;
  public static final int ERR_DB_REG_SPOOL_NO_NAME_OR_CLASS= 1009;


  public static final int ERR_FILE_DIRECTORY_NOT_EXIST = 2001;

  public static final int ERR_BUSINESS_DATABASE_NULL_SQL = 3001;
  public static final int ERR_BUSINESS_NOGET             = 3002;
  public static final int ERR_BUSINESS_XML_LOADING       = 3003;
  public static final int ERR_BUSINESS_OUTPUT_NOT_SET    = 3004;
  public static final int ERR_BUSINESS_ACT_NOT_EXIST     = 3005;
  public static final int ERR_BUSINESS_ID_NO_SET         = 3006;


  public static final int ERR_USER_NOGET_FROM_SESSION = 4001;
  public static final int ERR_USER_GET_NULL_SESSION   = 4002;
  public static final int ERR_USER_CREATE             = 4003;
  public static final int ERR_USER_NULL_ID            = 4004;
  public static final int ERR_USER_NULL_GROUPS        = 4005;

  public static final int ERR_CLASS_NO_SET_CLASSNAME    = 5001;
  public static final int ERR_CLASS_UNABLE_LOAD_CLASS   = 5002;
  public static final int ERR_CLASS_ERROR_INTERFACE     = 5003;


}