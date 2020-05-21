package com.emis.app.migration.action;

/**
 * User: merlin
 * Date: Apr 25, 2003
 * Time: 5:55:58 PM
 */
public final class emisMiUSRecno extends emisMiAction {
  final String[] src1 = new String[5];

  public final String act(final String[] src, final String[] param) {
    // 取得庫存調整單表身之 recno
    // 傳入的參數需為 [S_no,原始單據單號,p_no][]
    String recno = src[3].trim();
    if (recno.length() > 0) return recno;
    if (db == null) {
      initStmt("select min (recno) recno from ( " +
        "    select isnull(max((RECNO)+1),1) RECNO " +
        "    from useless_d " +
        "  where S_NO = ? and US_RSN_NO= ? " +
        "  union " +
        "  select RECNO " +
        " from useless_d " +
        " where S_NO = ? and US_RSN_NO= ? and p_no=? " +
        " ) us ");
    }
    src1[0] = src[0];
    src1[1] = src[1];
    src1[2] = src[0];
    src1[3] = src[1];
    src1[4] = src[2];
    recno = doQuery(src1);
    if (recno.length() == 0)
      recno = "1";
    return recno;
  }
}
