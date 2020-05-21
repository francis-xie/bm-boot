package com.emis.util;









/**

 * 這個 class 提供 String 的功能, 但完全是用 Array

 * 做的 , 而且沒有 encapsulation, 用來加快 critical section

 * 的速度, this is an important class for system speed up
 *
 * Track+[15137] dana.gao 2010/06/23 將js中的特殊字符進行轉譯

 */



public class emisStrBuf

{

    protected char [] buf;

    protected int length; // position



    /**

     * 從 index fromIndex 到 toIndex

     * 是否都有字元,除了空白(space)之外

     * (exclusive toIndex )

     * 為了速度,改成只檢查 (fromIndex) 和 (toIndex - 1)

     * 的字元是否都不是空白字元

     */

    public boolean isFullOfChar(int fromIndex,int toIndex)

    {

      if( toIndex >= length ) return false; // index out of range

      if( (buf[fromIndex] != ' ') && (buf[toIndex-1] != ' ') )

        return true;

      return false;

    }



    public emisStrBuf()

    {

        this(16);

    }



    public emisStrBuf(int size)

    {

        buf = new char [size];

        length = 0;

    }



    public void assign(String str)

    {

        setZeroLength();

        append(str);

    }



    public String reverse()

    {

      int j = length-1;

      for(int i=0; i< length ; i++)

      {

        if( i < j )

        {

          char c = buf[i];

          buf[i] = buf[j];

          buf[j]=c;

        } else break;

        j--;

      }

      return new String(buf,0,length);

    }



    public emisStrBuf append(char [] clist)

    {

        if (clist == null) {

          return this;

        }



        int len = clist.length;



        int newcount = len + length;



        ensureCapacity(newcount);



        int idx=0;

        for(int i=length; i < newcount; i++)

        {

          buf[i] = clist[idx++];

        }



        length = newcount;

        return this;



    }

    public emisStrBuf append(char c)

    {

      ensureCapacity(length+1);

      buf[length++] = c;

      return this;

    }





    public emisStrBuf append(String str)

    {



        if (str == null) {

	          str = String.valueOf(str);

        }



        int len = str.length();



        int newcount = len + length;



        ensureCapacity(newcount);



        str.getChars(0,len,buf,length);



        length = newcount;

        return this;

    }





    public emisStrBuf append(emisStrBuf strBuf)

    {

        int len = strBuf.length();



        if (len == 0)

        return this;



        int newcount = len + length;

        ensureCapacity(newcount);

        System.arraycopy( strBuf.getArray() , 0, buf, length, len );

        length = newcount;

        return this;

    }



    public void insert(String str,int index) throws Exception

    {

        if (str == null) {

	          return;

        }

        int len = str.length();

        if ((index < 0) || (index > length))

  	      throw new StringIndexOutOfBoundsException();



        int newcount = len + length;

        ensureCapacity(newcount);

    	System.arraycopy(buf,index,buf,index + len,length - index);

	    str.getChars(0,len,buf,index);

        length = newcount;

    }





    public char [] getArray()

    {

        return buf;

    }

    public int length()

    {

        return length;

    }



    public void setZeroLength()

    {

        length = 0;

    }



    public String substring(int fromidx)

    {

        if( fromidx >= length)

        {

            return "";

        }

        return new String(buf,fromidx,length-fromidx);

    }



    /**

     *  將 sStr Copy 到 fromidx , total nSize

     */

    public void copyinto(String sStr,int fromidx,int nSize)

    {

        if( sStr == null ) return;

        int toidx = fromidx + nSize;

        ensureCapacity(toidx);

        sStr.getChars(fromidx,toidx,buf,fromidx);

    }





    public boolean equals(String sStr)

    {

        return equals(sStr,0,length);

    }



    /**

     * 在 emisStrBuf 的 fromidx 和 endidx 之間

     * 和 sStr 比是不是一樣的字串, endidx 是 exclusive

     */

    public boolean equals(String sStr,int fromidx,int endidx)

    {

        if( sStr == null ) return false;

        int nLen = sStr.length();



        // 超出範圍

        if( endidx > length ) return false;



        int nStartIdx = 0;

        for(int i=fromidx; i < endidx; i++)

        {

            if( sStr.charAt(nStartIdx++) != buf[i] )

            {

                return false;

            }

        }

        return true;

    }



    public String substring(int beginIndex,int endIndex) throws Exception

    {

        if (beginIndex < 0) {

            throw new StringIndexOutOfBoundsException(beginIndex + ":"+ new String(buf,0,length) );

        }

        if (endIndex > length) {

            throw new StringIndexOutOfBoundsException(endIndex + ":"+ new String(buf,0,length));

        }

        if (beginIndex > endIndex) {

            throw new StringIndexOutOfBoundsException(endIndex - beginIndex + ":"+ new String(buf,0,length));

        }

        return new String(buf,beginIndex,endIndex-beginIndex);

    }



    /**

     * 會把 substring 的左右先 trim 掉

     */

    public String trimedsubstring(int beginIndex,int endIndex) throws Exception

    {

        if (beginIndex < 0) {

            throw new StringIndexOutOfBoundsException(beginIndex + ":"+ new String(buf,0,length) );

        }

        if (endIndex > length) {

            throw new StringIndexOutOfBoundsException(endIndex + ":"+ new String(buf,0,length));

        }

        if (beginIndex > endIndex) {

            throw new StringIndexOutOfBoundsException(endIndex - beginIndex + ":"+ new String(buf,0,length));

        }

        for(int i=beginIndex ; i < endIndex ; i++)

        {

            if(buf[i] == ' ' )

              beginIndex++;

            else

              break;

        }

        for(int i=endIndex-1 ; i > beginIndex ; i--)

        {

            if(buf[i] == ' ' )

              endIndex--;

            else

              break;

        }

        return new String(buf,beginIndex,endIndex-beginIndex);

    }



    /**

     * just used for debug,

     * for speed, you should use just getArray();

     */

    public String toString()

    {

        return new String(buf,0,length);

    }



    private void ensureCapacity(int newcount)

    {

        if ( newcount >  buf.length )

        {



          // we must expand it.. with a slower speed

            char [] newbuf = new char[ newcount + (newcount/2 )];

            if( length > 0 )

              System.arraycopy(buf,0,newbuf,0,length);

            buf = newbuf;



        }

    }



    private static String amp = "&amp;";

    private static String lt  = "&lt;";

    private static String gt  = "&gt;";

    private static String apos  = "&apos;";

    private static String quot  = "&quot;";



    /**

     *  在內部將

     * 將 sStr 中的 "<" , ">" , "&" ,"'" , """ 等符號

     * 轉成 "&lt;" ,"&gt;" ,"&amp;" ,"&apos;" , "&quot;" 等

     * xml 的 entity

     * note: 此 function 會先將 call setZeroLength

     */



    public void escapeXMLEntity(String str)

    {

        int i=0;

        int len = str.length();

        length = 0;



        while( i < len )

        {

            char c = str.charAt(i);

            i++;



            if( c == '&' )

            {

                ensureCapacity(length+5);

                amp.getChars(0,5,buf,length);

                length += 5;

                continue;

            } else

            if( c == '<')

            {

                ensureCapacity(length+4);

                lt.getChars(0,4,buf,length);

                length += 4;

                continue;

            } else

            if( c == '>')

            {

                ensureCapacity(length+4);

                gt.getChars(0,4,buf,length);

                length += 4;

                continue;

            } else

            if( c == '\'')

            {

                ensureCapacity(length+6);

                apos.getChars(0,6,buf,length);

                length += 6;

                continue;

            } else

            if( c == '"' )

            {

                ensureCapacity(length+6);

                quot.getChars(0,6,buf,length);

                length += 6;

                continue;

            } else {

                ensureCapacity(length+1);

                buf[length] = c;

                length++;

            }

        }



    }

  /**
   * 校验sStr是否含有特殊符号,若有則需要轉譯
   * 要轉譯的字符包括：
   * 1,  中括號 []
   * 2,  大括號 {}
   * 3, 換行   \n
   * 4, 換行   \r
   * 5, 引號   \', "
   */

  private static String lsquarebracket = "\\[";
  private static String rsquarebracket = "\\]";
  private static String lbrace = "\\{";
  private static String rbrace = "\\}";
  private static String enter = "\\n";
  private static String newline = "\\r";
  private static String jsapos = "\\'";
  private static String jsquot = "\\\"";

  public void escapeJSEntity(String str)  {
    int i = 0;
    int len = str.length();
    length = 0;
    while (i < len)    {
      char c = str.charAt(i);
      i++;
      if (c == '[') {
        ensureCapacity(length + 2);
        lsquarebracket.getChars(0, 2, buf, length);
        length += 2;
        continue;
      } else if (c == ']') {
        ensureCapacity(length + 2);
        rsquarebracket.getChars(0, 2, buf, length);
        length += 2;
        continue;
      }else if (c == '{') {
        ensureCapacity(length + 2);
        lbrace.getChars(0, 2, buf, length);
        length += 2;
        continue;
      }else if (c == '}') {
        ensureCapacity(length + 2);
        rbrace.getChars(0, 2, buf, length);
        length += 2;
        continue;
      }else if (c == '\n')  {
        ensureCapacity(length + 2);
        enter.getChars(0, 2, buf, length);
        length += 2;
        continue;
      } else if (c == '\r')      {
        ensureCapacity(length + 2);
        newline.getChars(0, 2, buf, length);
        length += 2;
        continue;
      } else if (c == '\'') {
        ensureCapacity(length + 2);
        jsapos.getChars(0, 2, buf, length);
        length += 2;
        continue;
      } else if (c == '"') {
        ensureCapacity(length + 2);
        jsquot.getChars(0, 2, buf, length);
        length += 2;
        continue;
      } else {
        ensureCapacity(length + 1);
        buf[length] = c;
        length++;
      }
    }
  }



/********************** test section ********************************/



    public static void main(String [] argv) throws Exception

    {

        emisStrBuf buf = new emisStrBuf(36);

        buf.append("");

        System.out.println("'"+buf.reverse()+"'");

    }



}