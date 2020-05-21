package com.emis.report;

import java.util.HashMap;

import com.emis.business.emisBusiness;
import com.emis.db.emisDb;
import com.emis.user.emisUser;

public class emisRightsCheck {
	public static HashMap RISMap=null;//多個專案時可能有問題
	public static HashMap RISValMap=null;//多個專案時可能有問題
	
	public static boolean getShowSet(emisBusiness oBusiness_,String R_ID) {
		emisUser user = oBusiness_.getUser();
		String _sGroups = user.getGroups();
		if (null == _sGroups) {
			_sGroups = "";
		}
		
		if(RISMap==null){
		   RISMap=new HashMap();
		   RISValMap=new HashMap();
		   emisDb db = null;
	       try {
				db = emisDb.getInstance(oBusiness_.getContext());
                //加用戶群組   20060220table改名           
			    // com.emis.busines.emisShowData.java惠璶?
				String _sSql = "select R_ID,R_SHOW,USERGROUPS,DEFAULT_VAL from ESYS_FIELD_SET";			
				db.executeQuery(_sSql);
				while (db.next()) {
					//group璶
					RISMap.put(db.getString("USERGROUPS")+":"+db.getString("R_ID"), db.getString("R_SHOW"));
					RISValMap.put(db.getString("USERGROUPS")+":"+db.getString("R_ID"), db.getString("DEFAULT_VAL"));
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					db.close();
				} catch (Exception esql) {
					esql.printStackTrace();
				}
			}
		
		}	
	
		
		Object obj=RISMap.get(_sGroups+":"+R_ID);
        if(obj==null){ 
         	return true;
        }else{
           String R_SHOW=(String)obj;
           if("N".equals(R_SHOW))
        	   return false;
           else return true;
        }      	
		
	}
	
	public static String getShowSetVal(emisBusiness oBusiness_,String R_ID) {
		emisUser user = oBusiness_.getUser();
		String _sGroups = user.getGroups();
		if (null == _sGroups) {
			_sGroups = "";
		}
		String sRes="";
		Object valueObj=RISValMap.get(_sGroups+":"+R_ID);
        if(valueObj!=null){
        	sRes=(String)valueObj;
        }
        return sRes;
		
	}
	
	
	public  static synchronized void reload(emisBusiness oBusiness_){
		RISMap=null;
		RISValMap=null;
		getShowSet(oBusiness_,"RELOAD");	
	}
	
	

}
