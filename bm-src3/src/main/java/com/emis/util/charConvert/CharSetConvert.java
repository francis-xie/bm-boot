package com.emis.util.charConvert;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2005/8/26
 * Time: 下午 06:51:51
 * To change this template use Options | File Templates.
 */
public class CharSetConvert {
	private final static int GB_CODE    = 0x01;
	private final static int BIG5_CODE  = 0x2;
	private final static int HZ_CODE    = 0x4;
	private final static int OTHER_CODE = 0x8;

	private static int sizeB2G_1,sizeB2G_2,sizeB2G_3,sizeB2G_4;
	private static int sizeG2B_1,sizeG2B_2;

	public CharSetConvert()
	{
	}

	public static void main(String[] args)
	{

    	String bigStr="您可以使用 [控制台] 中的圖示變更 Windows 2000 的外觀及功能。";
        String gbStr="蠟褫眕妏蚚 [諷秶怢] 笢腔芞尨曹載 Windows 2000 腔俋夤摯髡夔﹝";

		String str;


		System.out.println(bigStr);

        str = CharSetConvert.To_Gb(bigStr);

		System.out.println(str);

        if (gbStr.equals(str)) System.out.println("(B2G OK)");
		else                   System.out.println("(B2G Error)");


		System.out.println("----------------------------------------------");

		System.out.println(gbStr);

		str = CharSetConvert.To_Big(gbStr);

		System.out.println(str);

        if (bigStr.equals(str)) System.out.println("(G2B OK)");
		else                    System.out.println("(G2B Error)");


	}

	public static String To_Gb(String InStr)
	{

		byte In[]=InStr.getBytes();

    	byte Out[]=new byte[In.length];

		convert((byte)'G', In, Out);

		String str=new String(Out);

		return str;
	} // end of String To_Gb(String)

	public static String To_Big(String InStr)
	{

    	byte In[]=InStr.getBytes();

    	byte Out[]=new byte[In.length];

		convert((byte)'B', In, Out);

		String str=new String(Out);

		return str;

	} // end of To_Big(String)

	private static void convert(byte OutCodeMode,byte InStr[],byte OutStr[])
	{
		int incode;
		int count,cnt,kk;

    	sizeB2G_1=b2g_1.B2G_1.length;
    	sizeB2G_2=b2g_2.B2G_2.length+sizeB2G_1;
    	sizeB2G_3=b2g_3.B2G_3.length+sizeB2G_2;
    	sizeB2G_4=b2g_4.B2G_4.length+sizeB2G_3;
    	sizeG2B_1=g2b_1.G2B_1.length;
    	sizeG2B_2=g2b_1.G2B_1.length+sizeG2B_1;

		byte pbuf[]=new byte[InStr.length];

	    for (kk=0; kk<InStr.length; kk++)
    	{
        	pbuf[kk]=InStr[kk];
	    }

    	count=pbuf.length;

		incode=j_code(pbuf,count);

    	if (OutCodeMode=='B')
		{
			switch(incode)
			{
				case 2:
                	for (kk=0;kk<count;kk++)
                	{
                    	OutStr[kk]=pbuf[kk];
                	}
					break;
				case 1 :
					cnt=new_hzconvert (pbuf, count, (byte)'B');
                	for (kk=0;kk<cnt;kk++)
                	{
                    	OutStr[kk]=pbuf[kk];
                	}
					break;
				default:
                	for (kk=0;kk<count;kk++)
                	{
                    	OutStr[kk]=pbuf[kk];
                	}
					break;
			}
		}
		else if (OutCodeMode=='G')
		{
			switch(incode){
				case 1:
                	for (kk=0;kk<count;kk++)
                	{
                    	OutStr[kk]=pbuf[kk];
                	}
					break;
				case 2:
					cnt=new_hzconvert (pbuf, count, (byte)'G');
                	for (kk=0;kk<cnt;kk++)
                	{
                   	OutStr[kk]=pbuf[kk];
                	}
					break;
				default:
                	for (kk=0;kk<count;kk++)
                	{
                    	OutStr[kk]=pbuf[kk];
                	}
					break;
			}
		}
	} // end of void convert( ... )


	private static int new_hzconvert (byte s[], int slen,byte mode)
	{
		short pp;

		if (slen == 0)
			return 0;

		pp=0;

		while (pp<slen)
		{
			if ((s[pp] & 0x80)!=0) /* hi-bit on: hanzi */
			{
				if (pp < slen-1) 	/* not the last one */
				{
					if (mode=='B')
						g2b(s,pp);
					else
       					b2g(s,pp);
					pp++;
				}
			}
			pp++;
    	}
		return slen;
	}


	private static void g2b(byte str[],int pp)
	{
		int i;
		short c1=str[pp],c2=str[pp+1];

		if (c1<0) c1 += 256;
		if (c2<0) c2 += 256;

		if ((c2 >= 0xa1) && (c2 <= 0xfe))
		{
			if ((c1 >= 0xa1) && (c1 <= 0xa9))
			{
				i = ((c1 - 0xa1) * 94 + (c2 - 0xa1)) * 2;
			}
			else if ((c1 >= 0xb0) && (c1 <= 0xf7))
			{
				i = ((c1 - 0xb0 + 9) * 94 + (c2 - 0xa1)) * 2;
			}
          	else
             	return;

			if (i<sizeG2B_1)
			{
				str[pp]   =(byte) g2b_1.G2B_1[i];
				str[pp+1] =(byte) g2b_1.G2B_1[i+1];
			}
			else if (i<sizeG2B_2)
			{
				i-=sizeG2B_1;
				str[pp]   =(byte) g2b_2.G2B_2[i];
				str[pp+1] =(byte) g2b_2.G2B_2[i+1];
			}

			return;

		}

		str[pp] = (byte)0xa1;  str[pp+1] = (byte)0xbc;
	}



	private static void b2g(byte str[],int pp)
	{
		int i;
		short c1=str[pp],c2=str[pp+1];

		if (c1<0) c1 += 256;
		if (c2<0) c2 += 256;

		if ((c1 >= 0xa1) && (c1 <= 0xf9))
		{
			if ((c2 >= 0x40) && (c2 <= 0x7e))
			{
				i = ((c1 - 0xa1) * 157 + (c2 - 0x40)) * 2;
			}
			else if ((c2 >= 0xa1) && (c2 <= 0xfe))
			{
				i = ((c1 - 0xa1) * 157 + (c2 - 0xa1) + 63) * 2;
			}
        	else
            	return;

			if (i < sizeB2G_1)
			{
				str[pp]   =(byte) b2g_1.B2G_1[i];
				str[pp+1] =(byte) b2g_1.B2G_1[i+1];
			}
			else if (i<sizeB2G_2)
			{
				i-=sizeB2G_1;
				str[pp] =(byte) b2g_2.B2G_2[i];
				str[pp+1] =(byte) b2g_2.B2G_2[i+1];
			}
			else if (i<sizeB2G_3)
			{
				i-=sizeB2G_2;
				str[pp] =(byte) b2g_3.B2G_3[i];
				str[pp+1] =(byte) b2g_3.B2G_3[i+1];
			}
			else if (i<sizeB2G_4)
			{
				i-=sizeB2G_3;
				str[pp] =(byte) b2g_4.B2G_4[i];
				str[pp+1] =(byte) b2g_4.B2G_4[i+1];
			}
			return;

		}

		str[pp] = (byte)0xa1;  str[pp+1] = (byte)0xf5;
	}


	private static int j_code2(byte buff[],int count)
	{
		int c_gb=0;

		int pp;
		short b0,b1;

		for (pp=0;pp<count;pp++)
		{
			b0=buff[pp];
			if (b0<0) b0+=256;

			if ((b0 & 0x80)!=0)
			{

				b1 = buff[pp+1];

				if (b1<0) b1 +=256;

				if ((b0>= 0xF8 && b0 <= 0xF9) && ( (b1>= 0x40 && b1<= 0x7E) || (b1>= 0xA1 && b1<= 0xFE )))
				{
					return BIG5_CODE;
				}
				if ((b0 >= 0xA1 && b0 <= 0xF7) && (b1>= 0x40 && b1 <= 0x7E))
				{
					return BIG5_CODE;
				}
				if ((b0 >= 0xA1 && b0 <= 0xF7) && (b1>= 0xA1 && b1<= 0xFE))
				{
					c_gb++;
				}
				pp++;
				continue;
			}
		}

		if( c_gb ==0 )
		{
			return OTHER_CODE;
		}
		else
		{
			return GB_CODE;
		}

	}

	private static int j_code(byte buff[],int count)
	{
		int c_gb   = 0;
		int c_big5 = 0;

		int pp;
		short b0,b1;


		for (pp = 0; pp < count; pp++)
		{

			b0 = buff[pp];

			if (b0 < 0) b0 += 256;

			if ((b0 & 0x80) !=0)
			{
				b1 = buff[pp + 1];

				if (b1<0) b1 += 256;

				if ( (b0 == 0xB5 && b1 == 0xC4) || ((b0 == 0xCE) && b1==0xD2) )
				{
					c_gb++;
					pp++;
					continue;
				}
				else if ( (b0 == 0xAA && b1 == 0xBA) || ((b0 == 0xA7) && b1 == 0xDA) )
				{
					c_big5++;
					pp++;
					continue;
				}
				pp++;
			}
		}


		if(c_gb > c_big5)
		{
			return GB_CODE;
		}
		else if (c_gb ==  c_big5)
		{
			return j_code2(buff,count);
		}
		else
		{
			return BIG5_CODE;
		}
	}

} // end of class CharSetConvert