package com.emis.app.migration;



import java.io.IOException;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;

import java.lang.reflect.InvocationTargetException;

import java.sql.SQLException;

import java.util.Hashtable;

import java.util.Iterator;



public abstract class emisMiTarget extends emisMiDataSet {  // extended by emisMiTableTaget, emisMiTextTaget

  int commitCount = Integer.MAX_VALUE;

  int writeCount = 0;

  protected boolean clear = false;

  protected emisCreationRule creationRule = null;

  protected String[] creationKey = null;

  public PrintWriter oLogWriter_;

  /*
    配合控菜系统需求，增加按收银机类型来控制产生下传档
    产生档案的收银机类型：V - 一般收银端，I-财务端，S-控菜端，C-厨房端，Q-叫号显示端；如未设定该参数则只会产生档案给一般收银端的收银机(CASH_ID.POS_TYPE为空或等于V)
   */
  protected String posType = null;

  public final emisCreationRule getCreationRule() {

    return creationRule;

  }



  public final String[] getCreationKey() {

    return creationKey;

  }



  public abstract boolean write(String[] data) throws SQLException, IOException;



  final void setCommitCount(final int count) {

    this.commitCount = count;

  }



  void parse(final Hashtable h) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {

    super.parse(h);

    final Iterator it = h.keySet().iterator();

    String key;

    String val;

    while (it.hasNext()) {

      key = (String) it.next();

      val = (String) h.get(key);

      if (key.equalsIgnoreCase("Clear")) {

        if (val.equalsIgnoreCase("true"))

          clear = true;

        else

          clear = false;

      } else if (key.equalsIgnoreCase("creationkey")) {

        creationKey = emisMiField.parseStr(val);

      } else if (key.equalsIgnoreCase("foridno")){
        foridno = ("true".equalsIgnoreCase(val)) ;
      } else if(key.equalsIgnoreCase("posType")){
        posType = val;
      } else if (key.equalsIgnoreCase("creationrule")) {

//                try {

        final Constructor ctor = Class.forName(val).getConstructor(new Class[]{emisMiConfig.class});

        creationRule = (emisCreationRule) ctor.newInstance(new Object[]{config});

//                } catch (InstantiationException e) {

//                    // Log here;  //To change body of catch statement use Options | File Templates.

//                } catch (IllegalAccessException e) {

//                    // Log here;  //To change body of catch statement use Options | File Templates.

//                } catch (ClassNotFoundException e) {

//                    // Log here;  //To change body of catch statement use Options | File Templates.

//                } catch (NoSuchMethodException e) {

//                    // Log here;  //To change body of catch statement use Options | File Templates.

//                } catch (InvocationTargetException e) {

//                    // Log here;  //To change body of catch statement use Options | File Templates.

//                }

      }

    }

  }



  public abstract void append(String path[], boolean reopen) throws Exception;



  public final void cloneByKey(final String[] sKey, final boolean reopen) throws Exception {

    final String[] path = creationRule.getPath(sKey);

    if (path == null)

      return;

    append(path, reopen);

  }



  public boolean backup() {

    return false;

  }



    public abstract boolean clearTemp();



}

