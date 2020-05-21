package com.emis.db.dbf;
import java.io.IOException;
public class emisDbfHeader
{
  private    int X_version_1 = 3;    // 版本                1byte
  private int X_lastdate_2 = 0; // 最後更新日期      3byte
    private int X_lastdate_2_1 = 0; // 最後更新日期      3byte
    private int X_lastdate_2_2 = 0; // 最後更新日期      3byte
  private    int X_recordcnt_3 = 0;    // count;            4byte
  private    int X_headersize_4 = 32 ;  // 整個表頭的 size  2byte
  private    int X_recordsize_5 = 0  ;  // 每筆記錄的長度   2byte
  private emisDbfWriter oWriter_;
  public emisDbfHeader(emisDbfWriter oWriter)
  {
      oWriter_ = oWriter;
  }

  public void setRecCount(int recCount)
  {
    X_recordcnt_3 = recCount;
  }
  public void setColumnCount(int colCount)
  {
    X_headersize_4 = 34+colCount * 32;
  }

  public int getHeaderSize()
  {
    return X_headersize_4;
  }
  public void setRecSize(int recSize)
  {
    X_recordsize_5 = recSize;
  }
  public void writeTo() throws IOException
  {
    oWriter_.writeByte(X_version_1);
  //  oWriter_.writeStr(X_lastdate_2,3);
     oWriter_.writeByte(X_lastdate_2);
       oWriter_.writeByte(X_lastdate_2_1);
       oWriter_.writeByte(X_lastdate_2_2);
    oWriter_.writeInt(X_recordcnt_3);
    oWriter_.writeShort(X_headersize_4);
    oWriter_.writeShort(X_recordsize_5);
    oWriter_.skipBytes(20);
  }

}