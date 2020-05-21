package com.emis.rights;

import com.emis.db.emisDb;

import javax.servlet.ServletContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Jacky
 * Date: 2005/6/15
 * Time: 下午 05:23:52
 * [3606]提供產生動態報表權限設定的工廠
 */
public class emisRptRightsFactory extends emisAbstractRightsFactory {
  private final String RightsType = "RPT";

  public emisRptRightsFactory(ServletContext context) {
    super(context);
  }

  public emisDyRightsComponent createRightsComponent(String userid , String usergroup) throws Exception {
    emisDyRptRightsComp _oRptComp = null;
    emisDb _oDb = emisDb.getInstance(Context);
    try {
      _oRptComp = new emisDyRptRightsComp(this.Context);
      _oRptComp.setUserGroups(usergroup);
      _oRptComp.setUserID(userid);
      _oRptComp.setRights( createRightsDecorators(_oRptComp , userid , usergroup , _oDb) );
    } catch (Exception e) {
      throw e;
    } finally {
      if (_oDb !=null)
        _oDb.close();
    }
    return _oRptComp;
  }

  protected HashMap createRightsDecorators(emisDyRptRightsComp comp,String userid ,
                                           String usergroup, emisDb db) throws Exception {
    PreparedStatement _oProp = null;
    HashMap _oMapList = new HashMap();

    //定義尋找群組權限的SQL
    String _sGroupSQL =
        "   select em.KEYS, em.R_CLASS, em.R_SEQ , em.R_GROUP , isnull(eu1.R_ENABLE ,isnull(eu.R_ENABLE,'Y')) as R_ENABLE" +
        "             , isnull(rst.R_ENABLE,'N') as D_RIGHTS, er.*   " +
        "         from Esys_menumap em   " +
        "         left join  (select * from   " +
        "                         Esys_usermap    " +
        "                         where USERGROUPS = ?) eu on em.KEYS=eu.KEYS and   " +
        "                            em.R_CLASS=eu.R_CLASS and em.R_TYPE=eu.R_TYPE and em.R_ID=eu.R_ID     " +
        "         left join  (select * from   " +
        "                         Esys_usermap    " +
        "                         where USERID = ?) eu1 on em.KEYS=eu1.KEYS and    em.R_CLASS=eu1.R_CLASS and em.R_TYPE=eu1.R_TYPE and em.R_ID=eu1.R_ID     " +
        "         left join  (select * from   " +
        "                         Esys_Rptset    " +
        "                         ) rst on em.KEYS=rst.KEYS and em.R_CLASS=rst.R_CLASS  and em.R_ID=rst.R_ID     " +
        "         inner join  (select * from Esys_rights er where R_TYPE=? ) er on em.R_TYPE=er.R_TYPE and  em.R_ID=er.R_ID    " +
        "         order by  em.R_SEQ ";

    try {
      _oProp = db.prepareStmt(_sGroupSQL);
      db.setString(1, usergroup);
      db.setString(2, userid);
      db.setString(3, RightsType);
      db.prepareQuery();
      while (db.next()) {
        emisDyRptRightsDeco _oDeco = createDecoratorObject(comp,db);
        _oMapList.put(_oDeco.getPrimaryKeys(), _oDeco);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      if (_oProp != null)
        db.closePrepareStmt(_oProp);

    }
    return _oMapList;
  }

  /**
   * 產生單一的權限物件
   *
   * @param db
   * @return
   * @throws SQLException
   */
  protected emisDyRptRightsDeco createDecoratorObject(emisDyRptRightsComp comp , emisDb db) throws SQLException {
    emisDyRptRightsDeco _oDeco = new emisDyRptRightsDeco();

    String _sKeys = db.getString("KEYS");
    String _sClass = db.getString("R_CLASS");
    String _sRID = db.getString("R_ID");
    String _sEnable = db.getString("R_ENABLE");
    String _sDefault = db.getString("D_RIGHTS");
    int iSeq = db.getInt("R_SEQ");

    _oDeco.setMyContainer(comp);
    _oDeco.setRightsMenuKeys(_sKeys);
    _oDeco.setRightsClass(_sClass);
    _oDeco.setRightsId(_sRID);
    _oDeco.setRightsSeq(iSeq);
    _oDeco.setRightsType(RightsType);
    _oDeco.setRightsName(db.getString("R_NAME"));
    _oDeco.setRightsGroupID(db.getString("R_GROUP"));

    if ("N".equals(_sEnable)) {
      _oDeco.setRightsEnable(false);
    } else {
      _oDeco.setRightsEnable(true);
    }

    if ("Y".equals(_sDefault)) {
      _oDeco.setDefaultChecked(true);
    }

    HashMap _oMap = new HashMap();
    _oMap.put("R_ATTR1", db.getString("R_ATTR1"));
    _oMap.put("R_ATTR2", db.getString("R_ATTR2"));
    _oMap.put("R_ATTR3", db.getString("R_ATTR3"));
    _oMap.put("R_ATTR4", db.getString("R_ATTR4"));
    _oMap.put("R_ATTR5", db.getString("R_ATTR5"));
    _oMap.put("R_ATTR6", db.getString("R_ATTR6"));
    _oMap.put("R_ATTR7", db.getString("R_ATTR7"));
    _oMap.put("R_ATTR8", db.getString("R_ATTR8"));
    _oMap.put("R_ATTR9", db.getString("R_ATTR9"));
    _oMap.put("R_ATTR10", db.getString("R_ATTR10"));
    _oDeco.setRightsAttributes(_oMap);
    return _oDeco;
  }
}
