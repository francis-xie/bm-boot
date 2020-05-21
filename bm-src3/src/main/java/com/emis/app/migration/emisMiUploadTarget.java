/**
 *
 * User: shaw
 * Date: Jul 14, 2003
 * Time: 8:17:56 PM
 *
 */
package com.emis.app.migration;

import com.emis.util.emisDate;

import java.sql.SQLException;

public class emisMiUploadTarget extends emisMiRDBTarget {
  protected String billNo = "";
  protected int count = 0;
  protected int recNo = 0;
  protected String seqNo = null;

  public void actOK(String[] data) throws SQLException {
    if (!billNo.equals(data[8])) {
      refreshCount(data[8]);
      String sql = "insert  into HT_LOG( HT_TYPE, HT_CNT, HT_DATE, OBJ_NO, HT_STATUS, RECNO,HT_NO, S_NO, REMARK) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//      try {
      _db.prepareStmt(sql);
      _db.setString(1, "SHI");
      _db.setInt(2, 1);
      _db.setString(3, new emisDate().toString(this.config.getResourceBean().isAD()));
      _db.setString(4, data[8]);
      _db.setString(5, "0");
      _db.setInt(6, recNo);
      _db.setString(7, seqNo);
      _db.setString(8, data[5]);
      _db.setString(9, " 箱號:" + data[1] + " 檢貨單號:" + data[6]);
      _db.prepareUpdate();
//      } catch (SQLException e) {
//        // Log here;  //To change body of catch statement use Options | File Templates.
//      }
    } else {
      String sql = "update HT_LOG set HT_CNT = ? where HT_NO = ? and RECNO = ?";
//      try {
      _db.prepareStmt(sql);
      _db.setInt(1, count);
      _db.setString(2, seqNo);
      _db.setInt(3, recNo);
      _db.prepareUpdate();
//      } catch (SQLException e) {
//        // Log here;  //To change body of catch statement use Options | File Templates.
//      }
    }
    count++;
  }

  public void ackError(String[] data) throws SQLException {

    if (!billNo.equals(data[8])) {
      refreshCount(data[8]);
      String sql = "insert  into HT_LOG( HT_TYPE, HT_CNT, HT_DATE, OBJ_NO, HT_STATUS, RECNO,HT_NO, S_NO, REMARK) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//      try {
      _db.prepareStmt(sql);
      _db.setString(1, "SHI");
      _db.setInt(2, 1);
      _db.setString(3, new emisDate().toString(this.config.getResourceBean().isAD()));
      _db.setString(4, data[8]);
      _db.setString(5, "0");
      _db.setInt(6, recNo);
      _db.setString(7, seqNo);
      _db.setString(8, data[5]);
      _db.setString(9, " 箱號:" + data[1] + " 檢貨單號:" + data[6]);
      _db.prepareUpdate();
//      } catch (SQLException e) {
//        // Log here;  //To change body of catch statement use Options | File Templates.
//      }
    } else {
      String sql = "update HT_LOG set HT_CNT = ? where HT_NO = ? and RECNO = ?";
//      try {
      _db.prepareStmt(sql);
      _db.setInt(1, count);
      _db.setString(2, seqNo);
      _db.setInt(3, recNo);
      _db.prepareUpdate();
//      } catch (SQLException e) {
//        // Log here;  //To change body of catch statement use Options | File Templates.
//      }
    }
    count++;

  }

  private void refreshCount(String billNo) {
    this.count = 1;
    this.billNo = billNo;
    recNo++;
  }

  public void setConfig(emisMiConfig config) throws SQLException {
    super.setConfig(config);
//    try {
    _db = this.config.getDb();
    seqNo = _db.getSequenceNumber("HT_LOG", false, "%Y", "%4S");
//    } catch (SQLException e) {
//      // Log here;  //To change body of catch statement use Options | File Templates.
//    }
  }
}
