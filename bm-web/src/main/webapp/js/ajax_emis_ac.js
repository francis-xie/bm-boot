/*$Id: ajax_emis_ac.js 5 2015-05-27 08:15:12Z andy.he $
* eAC 公用JS
* eAC 专用 取单号;
* 取得单据最大单号加一的单号,并根据FIELDFORMAT档设定自动左补零
* 参数:sTable-操作TABLE  sFields-单号对应之字段(栏位)名
*      aCondFld-条件栏位名数组,与aCondVal对应
*      aCondVal-条件栏位值数组,与aCondFld对应
*      sFdType-对应到FIELDFORMAT表中的FD_TYPE,不传时取sFields
* 返回值:新单号字串.
* 调用范例:var billNo = emisGetACBillNo("APBILL_H","APB_NO",["COM_NO"],["0401"],"AP","APB_NO");
* 其他:也可用此函数取得表身最大RECNO+1(emisGetEposBillNo("IMP_PART_D","RECNO",["S_NO","IMP_NO"],["003","0000000008"]);)
*/
function AjaxGetACBillNo(sTable,sField,aCondFld,aCondVal,sFdHead,sDateFld){
  var sConds = " 1 = 1 ";
  var i;
  if(!emisEmpty(aCondFld)){
    for(i = 0; i < aCondFld.length; i++){
      sConds += " and " + aCondFld[i] + " = ''" + aCondVal[i] + "''";
    }
  }
  var _sURL = "ACT=ajax_getACBillNo"
      + "&FIELD=" + sField + "&TABLE=" + sTable
      + "&CONDS=" + sConds + "&FD_HEAD=" + sFdHead
      + "&DATEFLD=" + (emisEmpty(sDateFld) ? "CRE_DATE" : sDateFld );
  var sRetVal = AjaxGetData(_sURL)[0].split(",");
  sRetVal[0] = emisPadl((sRetVal[0].substring(6,12)-0)+1,6,"0" );
  sRetVal[0] = sRetVal[1]+ sFdHead + sRetVal[0];
  return sRetVal[0];
}