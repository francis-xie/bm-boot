package com.emis.app.migration;

import java.util.ArrayList;

public abstract class emisCreationRule {
  emisMiConfig config;

  emisCreationRule(emisMiConfig config) {
    this.config = config;
  }

  abstract public String[] getConfigPath(String args[]) throws Exception;

  private String[] checkPath(String[] a, String[] b) {
    ArrayList aryList = new ArrayList();
    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < b.length; j++) {
        if (b[j].equals(a[i]))
          aryList.add(a[i]);
      }
    }
    return (String[]) aryList.toArray(new String[]{});
  }

  public String[] getPath(String[] sKeys) throws Exception {
    String[] configPath = getConfigPath(sKeys);
    if (configPath == null)
      return null;
    emisMigration m = this.config.getMigration();
    String[] result = m.getTargetStore();
    if (result != null) {
      return checkPath(configPath, result);
    }
    return configPath;
  }
}
