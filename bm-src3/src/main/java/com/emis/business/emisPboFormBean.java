package com.emis.business;

import com.emis.util.emisDate;
import com.emis.user.emisUser;

import javax.servlet.http.HttpServletRequest;


/**
 * 供 PBO 使用之 form bean
 * User: Shaw
 * Date: Dec 11, 2002
 * Time: 10:34:06 PM
 */

public class emisPboFormBean extends emisFormBean {
	public emisPboFormBean(HttpServletRequest request) {
		super(request);
	}

	/**

	 * 取得字串型態之值

	 * @param key 參數值

	 * @return

	 */

	public String getString(String key) {
		String temp = getParameter(key.toUpperCase());
		if (temp == null) {
			//throw new emisViewParamNotSendException(key);
		}
		return temp;

	}


	/**

	 *取得 日期字串 型態之值,移除"/"傳回字串

	 * @param key 參數值

	 * @return

	 * @throws Exception

	 */

	public String getDate(String key, boolean isAD) throws Exception {

		String value = "";

		String _sKey = getParameter(key.toUpperCase());

		if (_sKey != null && !_sKey.equals("")) {

			try {

				value = new emisDate(_sKey).toString(isAD);

			} catch (Exception e) {

				e.printStackTrace();

				throw e;  //To change body of catch statement use Options | File Templates.

			}

		}

		return value;

	}

	/**

	 *取得 日期字串 型態之值,移除"/"傳回字串

	 * @param key 參數值

	 * @return

	 * @throws Exception

	 */

	public emisDate getEmisDate(String key) throws Exception {

		emisDate value = null;

		String _sKey = getParameter(key.toUpperCase());

		if (_sKey != null && !_sKey.equals("")) {

			try {

				value = new emisDate(_sKey);

			} catch (Exception e) {

				e.printStackTrace();

				throw e;  //To change body of catch statement use Options | File Templates.

			}

		}

		return value;

	}


	/**

	 * 取得整數型態之值

	 * @param key

	 * @return  參數值

	 * @throws NumberFormatException

	 */

	public int getInt(String key) throws NumberFormatException {

		int value = 0;

		try {

			String temp = getParameter(key.toUpperCase());


			if (temp != null) {

				value = Integer.parseInt(temp);
			} else {

				//throw new emisViewParamNotSendException(key);


			}


		} catch (NumberFormatException e) {

			new NumberFormatException();

		}

		return value;

	}


	/**

	 *取得 Float 型態之值

	 * @param key 參數值

	 * @return

	 * @throws NumberFormatException

	 */

	public float getFloat(String key) throws NumberFormatException {

		float value = 0;

		try {

			String temp = getParameter(key.toUpperCase());


			if (temp != null) {

				value = Float.parseFloat(temp);
			} else {

				//throw new emisViewParamNotSendException(key);

			}


		} catch (NumberFormatException e) {

			value = 0;

		}

		return value;

	}


	/**

	 *取得 double 型態之值

	 * @param key 參數值

	 * @return

	 * @throws NumberFormatException

	 */

	public double getDouble(String key) throws NumberFormatException {

		double value = 0;

		try {

			String temp = getParameter(key.toUpperCase());


			if (temp != null) {

				value = Double.parseDouble(temp);
			} else {

				//throw new emisViewParamNotSendException(key);

			}


		} catch (NumberFormatException e) {

			value = 0;

		}

		return value;
	}
  /**
   * 取得 user 之  ST_KEY
   * @return
   */
	public String getStaffKey(emisUser user) {
		String ST_KEY = "";
		if (user != null) {
			ST_KEY = user.getStKey();
		} else {
			ST_KEY = getString("ST_KEY");
		}
		return (ST_KEY == null) ? "" : ST_KEY;
	}
}

