package com.emis.db.dbf;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Types;
import java.util.ArrayList;

/**
 * DBF 由 表頭 34+32*N+(0X000D)+
 *             REC * ((0X20)+資料) +
 *             EOF(1A) 組成
 */
public class emisDbfWriter
{
  private RandomAccessFile random ;
  private ArrayList oColumns = new ArrayList();
  private emisDbfHeader oDbfHeader = new emisDbfHeader(this);
  private int nRecCounter_ ;

  public emisDbfWriter(String sFileName) throws IOException
  {
    random = new RandomAccessFile(sFileName,"rw");
    random.seek(34); // 先跳過檔頭
  }

  public void addColumn(String sColumnName,int nType,int nLen,int nScale) throws IOException
  {
      //  w.addColumn("P_NO1",Types.VARCHAR,12,0);
    emisDbfColumn col = new emisDbfColumn(this,sColumnName);
    col.setType(nType,nLen,nScale);
    oColumns.add(col);
    random.seek(34+32* oColumns.size());
  }

  // 每筆的開頭呼叫
  public void addRecordCount() throws IOException
  {
    nRecCounter_++;
    random.writeByte(32); // 每筆資料的開頭,表示沒被刪掉
  }

  public void write(String sStr) throws IOException
  {
    byte [] b = sStr.getBytes();
    random.write(b);
    b = null;
  }

  /**
   *
   */
  public void writeStr(String sColumnValue,int len) throws IOException
  {
    // 字元資料是左靠

    if( sColumnValue == null ) {
      for(int i=0; i<len;i++) {
        random.writeByte(0);
      }
      return;
    }
    byte [] b = sColumnValue.getBytes();
    int strlen = b.length;
    if( strlen >= len ) {
      random.write(b,0,len);
    } else {
      random.write(b);
      strlen = len-strlen;
      for(int i=0; i<strlen;i++)  {
        random.writeByte(32); // 填空白
      }
    }
  }
    public void writeColNameStr(String sColumnValue,int len) throws IOException
     {
       // 字元資料是左靠

       if( sColumnValue == null ) {
         for(int i=0; i<len;i++) {
           random.writeByte(0);
         }
         return;
       }
       byte [] b = sColumnValue.getBytes();
       int strlen = b.length;
       if( strlen >= len ) {
         random.write(b,0,len);
       } else {
         random.write(b);
         strlen = len-strlen;
         for(int i=0; i<strlen;i++)  {
           random.writeByte(0); // 填空白
         }
       }
     }


  /**
   *
   */
  public void writeNum(String sValue,int len) throws IOException
  {

    // 數字是右靠,前面補空白 (0x20)
    byte [] b = sValue.getBytes();
    int strlen = b.length;

    if( strlen >= len )
    {
      random.write(b,0,len);
    } else {

      strlen = len-strlen;
      for(int i=0; i<strlen;i++)  {
        random.writeByte(32); // 填空白
      }
      random.write(b);
    }
  }


  // 用來轉換 BigEndian 和 LittleEndian
  // Windows System 和 Solaris System
  // 可能寫法不一樣,先這樣寫
  // properties sun.cpu.endian..etc.
  private int [] bt = new int [4];

  public void writeByte(int nValue) throws IOException
  {
    random.writeByte(nValue);
  }
  public void writeShort(int nValue) throws IOException
  {
    short s = (short) nValue;
    bt[1] = (int)( s >>> 8 );
    bt[0] = (int)( s & 0xff );
    random.writeByte(bt[0]);
    random.writeByte(bt[1]);
  }
  public void writeInt(int nValue) throws IOException
  {
    bt[3] = nValue >>> 24;
    bt[2] = (nValue & 0xff0000) >>> 16;
    bt[1] = (nValue & 0xff00) >>> 8;
    bt[0] = nValue & 0xff;
    random.writeByte(bt[0]);
    random.writeByte(bt[1]);
    random.writeByte(bt[2]);
    random.writeByte(bt[3]);
  }

  public void skipBytes(int nlen) throws IOException
  {
    for(int i=0;i<nlen;i++)
    random.writeByte(0);
  }

  // 最後來寫 header
  public void close() throws IOException
  {
    try {
      random.seek(0);

      int _nColSize = oColumns.size();
      oDbfHeader.setColumnCount(_nColSize);

      int recSize = 0;
      for(int i=0; i< oColumns.size(); i++) {
        emisDbfColumn col = (emisDbfColumn) oColumns.get(i);
        recSize = recSize + col.getLength();
      }
      recSize++;
      oDbfHeader.setRecSize(recSize); // 要加上起始的那一個 byte
      oDbfHeader.setRecCount(nRecCounter_);

      oDbfHeader.writeTo();
      for(int i=0; i< oColumns.size(); i++) {
        emisDbfColumn col = (emisDbfColumn) oColumns.get(i);
        col.writeTo();
      }
      random.writeByte(13); // header 結尾 0x000D
      random.writeByte(0);
      int nLastPosition = oDbfHeader.getHeaderSize() + recSize * nRecCounter_;
      random.seek(nLastPosition); // 跑到最後
      random.writeByte(26); // 0x1A 為整個檔的結尾
      random.setLength( random.getFilePointer() );


    } finally {
      random.close();
    }
  }

  public static void main(String [] argvs) throws Exception
  {
    emisDbfWriter w = new emisDbfWriter("c:\\TEMP\\T3.DBF");
    try {
      w.addColumn("P_NO1",Types.VARCHAR,12,0);
      w.addColumn("P_PRICE",Types.NUMERIC,6,0);
      w.addRecordCount();
      w.writeStr("000000123456",12);
      w.writeNum(String.valueOf(100),6);
          w.addRecordCount();
       w.writeStr("00001234",12);
      w.writeNum(String.valueOf(100),6);
    } finally {
      w.close();
    }

  }
}