// Decompiled by DJ v3.5.5.77 Copyright 2003 Atanas Neshkov  Date: 2003/8/20 下午 08:51:45
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3)
// Source File Name:   emisMiFixBillNo.java
package com.emis.app.migration.action;

// Referenced classes of package com.emis.app.migration.action:
//            emisMiAction

public final class emisMiFixBillNo extends emisMiAction {
  public final String act(final String[] src, final String[] param) {
    final String BillNo = param[0] + src[0];
    return BillNo;
  }
}