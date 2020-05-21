package com.emis.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Security;

/**Dana 2011/01/06 简单的ASE加密解密功能
 * 快逸报表会将SQL显示在报表页面源码中,可能造成sql注入,
 */
public class emisSecurity {

  private static KeyGenerator keygen ;
  private static SecretKey secretKey;
  private static Cipher cipher;
  private static emisSecurity security = null;

  private emisSecurity(){

  }

  public static emisSecurity getInstance() throws Exception{
      if(security == null){
        security = new emisSecurity();
        keygen = KeyGenerator.getInstance("AES");
        secretKey = keygen.generateKey();
        cipher =Cipher.getInstance("AES");
      }
    return security;
  }


  //加密
  public String encrypt(String str, String charset) throws Exception{
    cipher.init(Cipher.ENCRYPT_MODE,secretKey);

    byte [] src =  str.getBytes(charset);
    byte [] enc = cipher.doFinal(src);

    return parseByte2HexStr(enc);
  }
  //加密
  public String encrypt(String str) throws Exception{
    return encrypt(str, emisUtil.FILENCODING);
  }

  //解密
  public String decrypt(String str, String charset) throws Exception{
    cipher.init(Cipher.DECRYPT_MODE,secretKey);

    byte[] enc = parseHexStr2Byte(str);
    byte [] dec = cipher.doFinal(enc);

    return new String(dec, charset);
  }

  //解密
  public String decrypt(String str) throws Exception{
    return decrypt(str, emisUtil.FILENCODING);
  }

  /**将16进制转换为二进制
   * @param hexStr
   * @return
   */
  public static byte[] parseHexStr2Byte(String hexStr) {
    if (hexStr.length() < 1)
      return null;
    byte[] result = new byte[hexStr.length()/2];
    for (int i = 0;i< hexStr.length()/2; i++) {
      int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
      int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
      result[i] = (byte) (high * 16 + low);
    }
    return result;
  }

  /**将二进制转换成16进制
   * @param buf
   * @return
   */
  public static String parseByte2HexStr(byte buf[]) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < buf.length; i++) {
      String hex = Integer.toHexString(buf[i] & 0xFF);
      if (hex.length() == 1) {
        hex = '0' + hex;
      }
      sb.append(hex.toUpperCase());
    }
    return sb.toString();
  }

    // Robert, 2015/05/7 , I add this for new chrome plugin verification
    // that we can check this to verify the request is from EMIS application or not
    // we need to take first 16 byte as key , decode it , and check whether have session_id + emis
    //
    // @param token , should be a String with length 16 (0~9,A~Z)
    public static String generateChromeSecurityToken( String token )  {
        try {
            String toEncrypt = token + System.currentTimeMillis() +  "EMIS";

            StringBuffer sb = new StringBuffer();
            java.util.Random r = new java.util.Random();
            for(int i=0;i< toEncrypt.length();i++) { // insert half random
                sb.append(toEncrypt.charAt(i));
                sb.append(randomChar(r));
            }
            //System.out.println("to:"+toEncrypt);
            toEncrypt = sb.toString();

            // 先轉成 hex array ,再把高低 order 對調
            toEncrypt = parseByte2HexStr(toEncrypt.getBytes());

            sb = new StringBuffer();
            sb.append(toEncrypt.charAt(0));
            for(int i=1;i< toEncrypt.length()-1;i+=2) {
                char c1 = toEncrypt.charAt(i);
                char c2 = toEncrypt.charAt(i+1);
                sb.append(c2).append(c1);
            }
            sb.append(toEncrypt.charAt(toEncrypt.length()-1));
            return sb.toString();

        } catch (Exception e) {

        }
        return "";
    }

    private static char randomChar(java.util.Random r) {
        char [] charArray = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                '0','1','2','3','4','5','6','7','8','9'};
        return charArray[r.nextInt( charArray.length )];
    }




  public static void main(String[] args) throws Exception{

    String str = "select * from users 我";
    String ss =  emisSecurity.getInstance().encrypt(str) ;
    System.out.println(ss);
    System.out.println(emisSecurity.getInstance().decrypt(ss));

  }

}
