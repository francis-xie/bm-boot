package com.emis.app.migration.action;

/**
 * User: merlin
 * Date: 2003/5/21
 * Time: 下午 06:48:34
 */
public final class emisMiGetPartSCost extends emisMiAction {
  /**
   * @param src   傳入所有參照到的欄位字串值
   *              依序為 ID_NO, SL_DATE, SL_NO
   * @param param is not used in this class
   * @return 商品在門市的成本 (由 part_s傳回)
   */
  public final String act(final String[] src, final String[] param) {
    if (db == null)
      initStmt("select PS_COST from PART_S where p_no=? and s_no=?");
    String sCost = doQuery(src);
    if (sCost == null)
      sCost = "0";
    return sCost;
  }
}
