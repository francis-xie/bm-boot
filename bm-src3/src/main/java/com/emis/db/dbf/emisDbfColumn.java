package com.emis.db.dbf;

import java.io.IOException;
import java.sql.Types;
/**
 * 代表每個 column 32 byte 的 header 的 class
 */
public class emisDbfColumn
{
  private String sColumnName_ ;  // 10 byte
  // 第 11 byte 為 0
  private int nType_ = Types.VARCHAR ; // 1byte, C 為文字,N 為數字
  // 第 13-16 保留
  private int nLen_; // 1 byte , 寬度
  private int nScale_; // 1byte ,小數位
  // 19 ~ 32 byte 保留

  private emisDbfWriter oWriter_ ;
  public emisDbfColumn(emisDbfWriter oWriter,String sColumnName) {
    oWriter_ = oWriter;
    sColumnName_ = sColumnName;
  }
  public void setType(int nType,int nLen,int nScale)
  {
    nLen_ = nLen;
    nType_ = nType;
    nScale_ = nScale;
  }
  public void writeTo() throws IOException
  {
    oWriter_.writeColNameStr(sColumnName_,10);
    oWriter_.skipBytes(1);
    if( nType_ == Types.VARCHAR )
      oWriter_.writeByte((int) 'C' );
    else
      oWriter_.writeByte((int) 'N' );

    oWriter_.skipBytes(4);
    oWriter_.writeByte(nLen_);
    oWriter_.writeByte(nScale_);
    oWriter_.skipBytes(14);
  }
  public int getLength()
  {
    return nLen_;
  }
  public int getScale()
  {
    return nScale_;
  }

}