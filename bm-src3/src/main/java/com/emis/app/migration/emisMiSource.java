package com.emis.app.migration;



import java.io.IOException;

import java.sql.SQLException;



public abstract class emisMiSource extends emisMiDataSet {  // extended by emisMiTableSource, emisMiTextSource

  String[] result; // or Object [] result;



  public abstract boolean open(emisMiConfig config) throws SQLException;



  public abstract String[] next() throws IOException, SQLException;



  public abstract boolean backup();



  public final void setFileName(final String fileName) {

  }

}

