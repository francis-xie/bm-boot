<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.emis.db.*,java.sql.*,java.util.*,com.emis.util.*" %>
<%
  // 建立 20 万笔发票,100 万笔表头
  // simulate 100 家门店,每家两个机号
  // 每天 1000 张发票,每张 5 笔表身,1年的资料

  Random r = new Random();
  long nInvoice = 1; // 从 1 号开始

  int nStartYear = 90; // 90 年
  for(int nStartMonth=1; nStartMonth <= 12 ; nStartMonth++) {
    for(int nStartDate=1; nStartDate <= 30; nStartDate++) {
      int nDate = nStartYear * 10000 + nStartMonth * 100 + nStartDate;
      for(int i=1; i<=100; i++ ) { // 一百家门店
        emisDb oDb = emisDb.getInstance(application);
        try {
          oDb.setAutoCommit(false);
          PreparedStatement h =
          oDb.prepareStmt("INSERT INTO OUT1_H (O_DATE,S_NO,INV_NO,ID_NO,"+
                          "SEQ_NO,INV_STATUS,INV_KIND,O_TIME ) VALUES (?,?,?,?,?,?,?,?)");
          PreparedStatement d =
          oDb.prepareStmt("INSERT INTO OUT1_D (O_DATE,S_NO,INV_NO,ID_NO,"+
                          "P_NO,RECNO,AC_NO,O_QTY,O_PRICE,O_COST,O_AMT,O_TAXKIND) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");


          // 门店补四位
          String sStore = emisChinese.lpad(String.valueOf(i),"0",4);
          String sCashReg1 = "1"+sStore+"1"; // 一号机
          String sCashReg2 = "1"+sStore+"2"; // 一号机


          for(int j=1;j<=1000;j++) { // 每天 1000 笔发票
            String sInvoice =  "MN" + emisChinese.lpad(String.valueOf(nInvoice++),"0",8);
            int nHour = r.nextInt(24);
            int nMin  = r.nextInt(60);
            int nTime = nHour * 100 + nMin;

            h.setInt(   1,nDate);
            h.setString(2,sStore);
            h.setString(3,sInvoice);
            String sCashReg = ((j%2) == 1 ) ? sCashReg1 : sCashReg2;
            h.setString(4,sCashReg);
            h.setInt(   5,j); // 交易序号
            h.setString(6, String.valueOf((j%4)+1)); // 1 ~ 4
            h.setString(7 ,"0"); // 正常 0 和作废 1
            h.setInt(   8,nTime); // 交易时间
            h.executeUpdate();
            // 每笔发票表头,五张表身
            for(int k=1; k<=5 ; k++) {

              int nQty = r.nextInt(3)+1;
              int nPrice = r.nextInt(1000);
              int nAmt = nPrice * nQty;
              int nPNo = r.nextInt(1000000000);
              String sPNo =  emisChinese.lpad( String.valueOf(nPNo),"0",13);
              d.setInt   (1,nDate);
              d.setString(2,sStore);
              d.setString(3,sInvoice);
              d.setString(4,sCashReg);
              d.setString(5,sPNo);
              d.setInt   (6,k);
              d.setString(7,"AP");
              d.setInt   (8,nQty);
              d.setInt   (9,nPrice);
              d.setInt   (10,nPrice);
              d.setInt   (11,nAmt);
              d.setString(12,"1");
              d.executeUpdate();
            }
          }
          oDb.commit();
        } catch (Exception e) {
          oDb.rollback();
          break;
        } finally {
          oDb.close();
        }
      }
    }
  }

%>
