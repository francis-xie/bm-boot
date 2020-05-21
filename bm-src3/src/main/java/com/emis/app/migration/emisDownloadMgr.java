package com.emis.app.migration;

/**
 * Created by emis.
 * User: jeff
 * Date: Dec 24, 2002
 * Time: 2:55:06 PM
 * 提供手動下傳的管理機制.
 * Track+[13951] dana.gao 2009/12/05 調整下傳D_NO判斷方法,download.D_NO調整為2碼后,indexof判斷方法有誤.
 */

import com.emis.db.emisDb;
import com.emis.schedule.emisOnLineTask;
import com.emis.schedule.emisTask;
import com.emis.schedule.emisTaskDescriptor;
import com.emis.user.emisUser;

import javax.servlet.ServletContext;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

public class emisDownloadMgr extends emisTask implements emisTaskDescriptor {
	

	private HashMap oRealStores_ = new HashMap();

	private HashMap oStandbyTasks_ = new HashMap();

	private HashMap oStandbyName_ = new HashMap();

	private String sTypeName_ = null;

	private emisMigration oWorkTask_ = null;

	private String sWoringItem_ = "";

	private int taskLength = 1;


	public emisDownloadMgr() {
		super();
		
	}

	public void runTask() throws Exception {

		try {
			if (prepareDownload()) {
				downloading();
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
		}
	}


	private void downloading() {
		String _sStores[], _sClassName;
		Iterator iterator = oRealStores_.keySet().iterator();
		while (iterator.hasNext()) {
			String index = (String) iterator.next();
			_sStores = (String[]) oRealStores_.get(index); // _sStores 加 s
			// 表示做一個 Task
			// 時，同時做幾個門市
			if (_sStores.length > 0) {
				_sClassName = (String) oStandbyTasks_.get(index);
				this.sWoringItem_ = (String) this.oStandbyName_.get(index);
				oWorkTask_ = new emisMigration(oContext_, _sClassName, oUser_); // 多傳入
				// user
				// 物件
				oWorkTask_.setTargetStore(_sStores);
				oWorkTask_.run();
				this.sWoringItem_ = "";
			}
			oWorkTask_ = null;
		}
	}

	private boolean prepareDownload() throws Exception {
		String _sD_NO, _sStores[], _sD_CLASS;
		boolean _bRetval = false;
		emisDb _oDownload = emisDb.getInstance(oContext_);
		try {
			_oDownload
					.prepareStmt("select D_NO, D_PARAM, D_NAME, D_CLASS \n"
							+ "from Download \n"
							+ "where D_NO IS NOT null and D_CLASS IS NOT null and D_CLASS <> ' ' \n"
							+ "order by D_NO");
			_oDownload.prepareQuery();
			while (_oDownload.next()) {
				_sD_NO = _oDownload.getString("D_NO"); // class id
				_sD_CLASS = _oDownload.getString("D_CLASS");
				// 去掉SQL中的Nvl檢核,放到此處判斷,這樣SQL SERVER和ORACLE都可用. update by andy
				// 2005/12/28
				if ("".equals(_sD_CLASS))
					continue;
				this.taskLength = _sD_NO.length();
				oStandbyTasks_.put(_sD_NO, _oDownload.getString("D_PARAM"));
				oStandbyName_.put(_sD_NO, _oDownload.getString("D_NAME"));
				_sStores = searchStore(_sD_NO);
				oRealStores_.put(_sD_NO, _sStores);
			}
			if (!oStandbyTasks_.isEmpty() && !oRealStores_.isEmpty()) {
				_bRetval = true;
			}
		} finally {
			if (_oDownload != null) {
				_oDownload.close();
				_oDownload = null;
			}
		}
		return _bRetval;
	}

	private String[] searchStore(String sWorkId) {
		String _sStoreNo, _sWorkGroup;
		StringTokenizer _oToken;
		ArrayList ary = new ArrayList();
		try {
			_oToken = new StringTokenizer(sParameter_, ",");
			while (_oToken.hasMoreTokens()) {
				_sStoreNo = _oToken.nextToken();
				_sWorkGroup = "";
				if (_oToken.hasMoreTokens()) {
					_sWorkGroup = _oToken.nextToken();
//					int p = _sWorkGroup.indexOf(sWorkId);
					if (checkright(_sWorkGroup,sWorkId)){
						ary.add(_sStoreNo);
						// System.out.println("adding "+ _sStoreNo + " to " +
						// sWorkId);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			_oToken = null;
			_sStoreNo = _sWorkGroup = null;
		}
		return (String[]) ary.toArray(new String[] {});
	}

  //Track+[13951] dana.gao 2009/12/05 調整下傳D_NO判斷方法,download.D_NO調整為2碼后,indexof判斷方法有誤.
  private boolean checkright(String _sWorkGroup, String sWorkId) {
    boolean flag = false;
    int len = _sWorkGroup.length();
    while (len >= taskLength) {
      if (_sWorkGroup.substring(0, taskLength).equals(sWorkId)) {
        flag = true;
        break;
      } else {
        _sWorkGroup = _sWorkGroup.substring(taskLength, len);
        len = _sWorkGroup.length();
      }
    }
    return flag;
  }
	public emisTaskDescriptor getDescriptor() {
		return this;
	}

	public boolean isEnd_ = false;

	private Exception e_ = null;

	private boolean hasError_ = false;

	public boolean hasError() {
		return hasError_;
	}

	public Exception getError() {
		return e_;
	}

	public boolean isFinished() {
		return isEnd_;
	}

	public void descript(final Writer w) {
		PrintWriter out = new PrintWriter(w);
		if (!isEnd_) {
			out.println("作業:" + sWoringItem_ + "<BR>");
			// out.println("進度:" + String.valueOf(iCount_) + "<BR>");
		} else {
			out.println("作業結束<BR>");
		}
		out = null;
	}

	/*
	public static void main(String[] args) { 
		emisDownloadMgr dm = new emisDownloadMgr(); 
		try { 
 			String sDLStr_; 
 			if (args.length==0)
 			 	sDLStr_="B0019,041012"; 
 			else 
 				sDLStr_ = args[0]; 
 			ServletContext context = new emisGuiLessResourceBean().getServletContext();
 			dm.runOnLine("_GENCCR", context , sDLStr_ , null); 
 			// new	emisErosUserImpl(context, null, "root", "root","root", new Boolean(false), "sessionid") 
		} catch (Exception e) { 
			// Log here; 
			//To change body of catch statement use Options | File Templates. 
		} 
	}
	 */
}
