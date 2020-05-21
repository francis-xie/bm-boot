/**
 *  20040513 abel add  extends java.io.Serializable  httpsession 需要 Serializable
 */

package com.emis.business;

import com.emis.user.emisUser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 和 JDBC 有關 特別複雜的作業, 需要使用
 * <sql class=...> 的寫法來指定 SQLExecuter
 * 系統會呼叫 interface
 */
public interface emisSQLExecuter extends java.io.Serializable
{
    /**
     *   傳回新增,刪除,或修改的筆數
     *   或是自己定義的數字
     *   在此 function 內不需要關掉 emisDb,business XML 會做
     */
    int execute(ServletContext application,emisUser oUser,HttpServletRequest request) throws Exception;
}