package com.emis.test;


import com.emis.file.emisFileMgr;
import com.emis.file.emisDirectory;
import com.emis.db.emisDb;
import com.emis.qa.emisServletContext;
import com.emis.server.emisServerFactory;
import com.emis.util.emisUtil;
import com.emis.util.emisDate;
import com.emis.report.emisString;

import javax.servlet.ServletContext;
import java.sql.PreparedStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2004/12/14
 * Time: 下午 06:06:45
 * To change this template use Options | File Templates.
 */
public class test {
    private ServletContext oContext;
    private emisDb wtnDb;

    public test(ServletContext oContext) throws Exception {
           this.oContext = oContext;


           // System.out.println(oDir.getDirectory());
       }


    public void newDb() throws Exception {
        this.wtnDb = emisDb.getInstance(oContext);
        //    this.wtnDb.setAutoCommit(false);
    }

    public void closeDb() {
        try {
            wtnDb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private  int update(String S_NO,String P_NO,String PS_QTY,java.sql.PreparedStatement updatePS)throws Exception {
        updatePS.clearParameters();
        updatePS.setString(1,PS_QTY);
        updatePS.setString(2,S_NO);
        updatePS.setString(3,P_NO);

        return updatePS.executeUpdate();


    }

    private void run() {
        String select = " select ps.S_NO,ps.PS_QTY,P.* from part_s ps  left join(select * from part where P_NO >='0000000000000' and P_NO <='ZZZZZZZZZZZZZ') p  " +
                        "on p.p_NO=PS.P_NO  where S_NO >='003' and S_NO <='006'  order by PS.S_NO";
        String update = "update part_s set PS_QTY =? where S_NO=? and P_NO=?";

        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        try {
            newDb();
            selectStmt = wtnDb.prepareStmt(select);
            updateStmt = wtnDb.prepareStmt(update);
            selectStmt.setFetchSize(10000);
            java.sql.ResultSet rs = selectStmt.executeQuery();
            int count=0;
            int count1=0;
             System.out.println(" run:" + emisUtil.todayTimeS());
            while(rs.next()){
              // update(rs.getString("S_NO"),rs.getString("P_NO"),rs.getString("qty"),updateStmt);
              count = rs.getInt("PS_QTY");
                count1++;

            }
             System.out.println("count1:" + count1);
            System.out.println(" end run:" + emisUtil.todayTimeS());

        } catch (Exception e) {
                 e.printStackTrace();

        } finally {
           wtnDb.close();
        }


    }

    public static void main(String[] args) {

        try{
            /*
            String   s ="李鴻彬ddd";
              if (Character.UnicodeBlock.of(s.charAt(2)) != Character.UnicodeBlock.BASIC_LATIN) {
                  System.out.println("OK");
              }else{
                  System.out.println("???");
              } */
             String  _sLine ="李鴻彬ddd";
     //       byte[] ff = _sLine.getBytes();
     //       System.out.println(ff.length);
     //       System.out.println(_sLine.getBytes()..length);
            Integer.parseInt("");
           System.out.println(emisString.subStringBE(_sLine,0,7));
             System.out.println(emisString.subStringBE(_sLine,1,7));
             System.out.println(emisString.subStringBE(_sLine,2,7));
             System.out.println(emisString.subStringBE(_sLine,3,7));
              System.out.println(emisString.subStringBE(_sLine,4,7));
             System.out.println(emisString.subStringBE(_sLine,5,7));
            System.out.println(emisString.subStringBE(_sLine,6,7));

            emisDate d = new emisDate("941221");
        //   System.out.println(d.add(1).toString(false));
             System.out.println(d.getDiff("941231",false));
              System.out.println(d.getDiff("941231",true));
              System.out.println(d.getDiff("941231"));



         emisServletContext servlet = new emisServletContext();
        emisServerFactory.createServer(servlet, "c:\\wwwroot\\wtn",
                "c:\\resin3X\\wtn.cfg", true);
        emisFileMgr oFileMgr = emisFileMgr.getInstance(servlet);
        emisDirectory oDir1 = oFileMgr.getDirectory("root").subDirectory("data")
                .subDirectory("upload").subDirectory("all");
        System.out.println("start to run:" + emisUtil.todayTimeS());
             test obj = new test(servlet);
        //   obj.ThreadHandrun("20040201", "20040201", servlet);
        obj.run();
        System.out.println("End of run:" + emisUtil.todayTimeS());
        }catch(Exception e){
                e.printStackTrace();
        }
        /*  ;
       try{
           String temp ="  ";
       Process p =null;
         p = Runtime.getRuntime().exec("cmd.exe c:\\emis\restart.bat  ");
       }catch(Exception e){
            e.printStackTrace();

       }  */


    }
}
