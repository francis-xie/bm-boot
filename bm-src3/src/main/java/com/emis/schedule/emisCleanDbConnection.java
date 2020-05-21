package com.emis.schedule;

import com.emis.db.emisDbMgr;
import com.emis.db.emisDbConnector;
import com.emis.db.emisProxyDesc;
import com.emis.spool.emisComplexSpool;
import com.emis.spool.emisSpoolSnapShot;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;

/** * Created by IntelliJ IDEA. * User: Administrator * Date: Jun 7, 2004 * Time: 4:04:18 PM * To change this template use Options | File Templates. *  *  將 spool 的 size 減至最少 , 理論上應該是不需要用到, *  除非程式有造成沒有正常 free connection 的 case */
public class emisCleanDbConnection extends emisTask {

    public emisCleanDbConnection(){}
    public emisCleanDbConnection(ServletContext oContext){
        this.oContext_ =oContext;
    }
    public void runTask() throws Exception{
       try{
           System.gc();
           emisDbMgr _oMgr = emisDbMgr.getInstance(oContext_);
           emisDbConnector _oConnector = _oMgr.getConnector(); // default connector
           if( _oConnector == null ) {
               throw new Exception("NO Connection");
           }
           emisComplexSpool oSpool_ = (emisComplexSpool) _oConnector;
           emisSpoolSnapShot _oShot = oSpool_.getSnapShot();
           List CleanObj = new ArrayList();
           if( (_oShot.getPooledSize() + _oShot.getCheckedOutSize()) > 10 ){
				Enumeration e = _oShot.getDescriptor();				
				while( e.hasMoreElements()){				    emisProxyDesc _oDesc = (emisProxyDesc) e.nextElement();				    if(_oDesc.getDescription().equalsIgnoreCase("連線池") ){				
				       CleanObj.add(_oDesc.getId());				    }				}				    
				 for(int i =oSpool_.getMinSize(); i< CleanObj.size() ;i++) {				
				    oSpool_.onLineDelete((String)CleanObj.get(i));				
				 }
           }

       }catch(Exception e){
           e.printStackTrace();
       }
    }

}
