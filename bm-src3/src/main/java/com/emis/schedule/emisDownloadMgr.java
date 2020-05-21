package com.emis.schedule;

/**
 * Created by emis.
 * User: jeff
 * Date: Dec 24, 2002
 * Time: 2:55:06 PM
 * 提供手動下傳的管理機制.
 */

import com.emis.db.emisDb;
import com.emis.schedule.emisTask;
import com.emis.schedule.emisOnLineTask;
import com.emis.schedule.emisTaskDescriptor;
import com.emis.user.emisUser;
import com.emis.app.migration.emisMigration;
//import com.emis.business.test;
//import com.emis.app.tester.emisGuiLessResourceBean;

import javax.servlet.ServletContext;
import java.util.*;
import java.io.Writer;
import java.io.PrintWriter;

//import temp.test.emisGuiLessResourceBean;

public class emisDownloadMgr extends emisTask implements emisOnLineTask, emisTaskDescriptor{
  private ServletContext oContext_    = null;
  private HashMap oRealStores_        = new HashMap();
  private HashMap oStandbyTasks_      = new HashMap();
  private HashMap oStandbyName_      = new HashMap();
  private String sTypeName_ = null;
  private emisMigration oWorkTask_         = null;
  private String sWoringItem_="";

  public emisDownloadMgr(){
    super();
    //prepareMainData(sParameter);
  }
  public void runTask() throws Exception{
    try{
      if(prepareDownload()){
        downloading();
      }
    } catch (Exception e){
      System.out.println(e.toString());
    } finally {
      if( oUser_ != null ){
        oUser_.removeAttribute(sTypeName_);
      }
      oContext_      = null;
      oRealStores_   = null;
      oStandbyTasks_ = null;
      oUser_         = null;
      sParameter_    = null;
    }
  }


  private void downloading() throws Exception{
    String _sStores[], _sClassName = null;
    for(int i=1; i<=oRealStores_.size(); i++){
        String index = (i>9) ? String.valueOf(i) : "0"+ String.valueOf(i);
          _sStores = (String[]) oRealStores_.get(index); // _sStores 加 s 表示做一個　Task　時，同時做幾個門市
      if(_sStores.length > 0){
        _sClassName = (String) oStandbyTasks_.get(index);
        this.sWoringItem_ = (String) this.oStandbyName_.get(index);
        oWorkTask_  = new emisMigration(oContext_, _sClassName);
        oWorkTask_.setTargetStore(_sStores);
        oWorkTask_.run();
        this.sWoringItem_="";
      }
      oWorkTask_ = null;
    }
  }

  private boolean prepareDownload() throws Exception{
    String _sD_NO, _sStores[];
    boolean _bRetval = false;
    emisDb _oDownload = emisDb.getInstance(oContext_);
    _oDownload.prepareStmt("select D_NO, D_PARAM, D_NAME "
                         + "from Download "
                         + "where D_NO IS NOT null "
                         + "      and nvl(D_CLASS,' ') <> ' ' and D_CLASS <> ' ' and  D_CLASS IS NOT null "
                         + "order by D_NO");
    _oDownload.prepareQuery();
    while(_oDownload.next()){
      _sD_NO = _oDownload.getString("D_NO"); // class id
        oStandbyTasks_.put(_sD_NO, _oDownload.getString("D_PARAM"));
        oStandbyName_.put(_sD_NO, _oDownload.getString("D_NAME"));
        _sStores = searchStore(_sD_NO);
        oRealStores_.put(_sD_NO, _sStores);
    }
    if(!oStandbyTasks_.isEmpty() && !oRealStores_.isEmpty()){
      _bRetval = true;
    }
    if(_oDownload!=null){
      _oDownload.close();
      _oDownload = null;
    }
    return _bRetval;
  }
  // abel add and modify
    /**
     * 固定切字串為兩碼 以方便判斷是否有 workid
     * @param _sParameter
     * @return
     */
  private Hashtable judgeWorkItem(String _sParameter ){
           boolean flag =true;
           Hashtable oWorkItem = new Hashtable();
           int start=0,end=2;
           while(flag){
                if(end > _sParameter.length() ) break;
                 oWorkItem.put(_sParameter.substring(start,end),"");
                 start+=2;
                 end+=2;
           }
      return oWorkItem;

  }

  private String[] searchStore(String sWorkId){
    String _sStoreNo, _sWorkGroup;
    StringTokenizer _oToken;
    Hashtable _oWorkItem ;
      ArrayList ary = new ArrayList();
    try{
      _oToken = new StringTokenizer(sParameter_, ",");
      while(_oToken.hasMoreTokens()){
        _sStoreNo = _oToken.nextToken();
        _sWorkGroup = "";
        if (_oToken.hasMoreTokens()){
          _sWorkGroup = _oToken.nextToken();
          _oWorkItem = judgeWorkItem(_sWorkGroup);
          if (_oWorkItem.containsKey(sWorkId) )
               ary.add(_sStoreNo);

          /*

          int p=_sWorkGroup.indexOf(sWorkId) ;

          while (p>=0 && p % 2 != 0) {
            String s = _sWorkGroup.substring(p+1);
            p = s.indexOf(sWorkId);
          }
          if (p >= 0 )
            ary.add(_sStoreNo);
           */
        }
      }
    }catch (Exception e){
      System.out.println(e.toString());
    }finally{
      _oToken  = null;
      _sStoreNo = _sWorkGroup = null;
    }
    return (String[] ) ary.toArray(new String[]{});
  }

  public emisTaskDescriptor getDescriptor() {
      return this;
  }


    public boolean isEnd_ = false;
    private Exception e_=null;
    private boolean hasError_=false;

    public boolean hasError() {
      return hasError_;
    }
    public Exception getError() {
      return e_;
    }
    public boolean isFinished() {
      return isEnd_;
    }

    public void descript(Writer w) {
      PrintWriter out = new PrintWriter(w);
      if( !isEnd_ ){
          out.println("作業:" + sWoringItem_ + "<BR>");
//          out.println("進度:" + String.valueOf(iCount_) + "<BR>");
      } else {
        out.println("作業結束<BR>");
      }
      out = null;
    }



   public static void main(String[] args) {
       emisDownloadMgr dm = new emisDownloadMgr();
       try {
           String test ="123456";

           String sDLStr_;
           if (args.length==0)
               //sDLStr_="B0019,04101112,B0003,0110";
                 sDLStr_="B0019,04101112,B0003,0110";
           else
               sDLStr_ = args[0];
           //ServletContext context = new emisGuiLessResourceBean().getServletContext();
           //dm.runOnLine("_GENCCR", context , sDLStr_ , null);
           // new emisErosUserImpl(context, null, "root", "root","root", new Boolean(false), "sessionid")
       } catch (Exception e) {
           e.printStackTrace();  //To change body of catch statement use Options | File Templates.
       }
   }

}

