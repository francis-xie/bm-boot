package com.emis.cipher;

import com.emis.db.emisDb;
import com.emis.manager.emisAbstractMgr;
import com.emis.qa.emisServletContext;
import com.emis.trace.emisError;
import com.emis.trace.emisTracer;
import com.emis.util.emisUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.ServletContext;
import java.io.*;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;



/**
 *   Cipher Mgr 包含了兩組 key,
 *   其中一個為 Database 加密所用的,放在 epos.cfg 中
 *   emis.keystore 所指定的地方,最好在 C:\resin 之下,
 *   不要在 /wwwroot/epos/ 之下
 *   一個為 User Password 加密所用的,
 *   放在資料庫的 EMISKEY 這個 Table
 *   the first time,process will be slow, since the key generation
 *   will use about 30 seconds, and two keys (db and user) will spend
 *   1 minute,詳細說明請參考 epos programming guide
 */
public class emisCipherMgr extends emisAbstractMgr
{
  public static final String STR_EMIS_CIPHERMGR = "com.emis.ciphermgr";


  private Cipher desCipher;
  private KeyGenerator keygen;
  private SecretKey userKey;
  private SecretKey dbKey;
  private String sKeyStore_;

  private boolean isUserKeyNeedEncrypt = false;
  private boolean isDbNeedEncrypt = false;

  public emisCipherMgr (ServletContext application,Properties props)  throws Exception
  {
    super(application,STR_EMIS_CIPHERMGR,"emisCipherMgr");

    if ("true".equalsIgnoreCase(props.getProperty("user.password.encrypt","false")))
    {
      isUserKeyNeedEncrypt = true;
    }

    if( "true".equalsIgnoreCase(props.getProperty("db.password.encrypt")))
    {
      sKeyStore_ = props.getProperty("emis.keystore");
      if( (sKeyStore_ == null) || "".equals(sKeyStore_) )
      {
        throw new Exception("There is not key store specification,please setup emis.keystore");
      }
      isDbNeedEncrypt= true;
    }

    if( isDbNeedEncrypt || isUserKeyNeedEncrypt ) {
    // dynamic register JCE Security Provider
      Provider sunJce = (Provider) Class.forName("com.sun.crypto.provider.SunJCE").newInstance();
      Security.addProvider(sunJce);
      desCipher = Cipher.getInstance("DES");
      keygen = KeyGenerator.getInstance("DES");

      if( isDbNeedEncrypt) {
        genDbKey();
      }
    }




  }

  public boolean isDbNeedEncrypt()
  {
    return isDbNeedEncrypt;
  }

  public synchronized void genDbKey() throws Exception
  {
    if(!isDbNeedEncrypt) return;

    File _keyf = new File(sKeyStore_);
    if( ! (_keyf.exists() && _keyf.isFile()) ) {
      dbKey = keygen.generateKey();
      byte [] keyData = dbKey.getEncoded();
      // create a key, and store it in key store
      OutputStream out = new FileOutputStream(sKeyStore_);
      try {
        out.write(keyData,0,8);
        out.flush();
      } finally {
        out.close();
      }

    } else {
      InputStream in = new FileInputStream(sKeyStore_);
      try {
        byte [] keyData = new byte[8];
        if(in.read(keyData,0,8) != 8 ) { // 64 bit
          throw new Exception("key store file has been broken");
        }
        DESKeySpec desKeySpec = new DESKeySpec(keyData);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        dbKey = keyFactory.generateSecret(desKeySpec);
      } finally {
        in.close();
      }
    }

  }

  public synchronized void genUserKey() throws Exception
  {
    if(! isUserKeyNeedEncrypt ) return;

    emisDb oDb = emisDb.getInstance(this.application_);
    try {
      oDb.setDescription("get User SecretKey");
      oDb.prepareStmt("SELECT KEYDATA FROM EMISKEY WHERE KEYPK=1");
      oDb.prepareQuery();
      if( oDb.next() ) {
        String sKeyData = oDb.getString("KEYDATA");
        byte [] bKeyData = emisUtil.strToByte(sKeyData);
        DESKeySpec desKeySpec = new DESKeySpec(bKeyData);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        userKey = keyFactory.generateSecret(desKeySpec);
      } else {
        userKey = keygen.generateKey();
        byte [] encode = userKey.getEncoded();
        String sKeyEncode = emisUtil.byteToStr(encode);
        oDb.prepareStmt("INSERT INTO EMISKEY (KEYDATA,KEYPK) VALUES (?,1)");
        oDb.setString(1,sKeyEncode);
        oDb.prepareUpdate();
      }
    } finally {
      oDb.close();
    }
  }

  public synchronized String cipherUserData(String sStr) throws Exception
  {
      if(!isUserKeyNeedEncrypt) return sStr;

      int len = sStr.length();
      if( len % 6 != 0 )
        throw new Exception("decryption error length");
      byte b [] = new byte[ len /6 ];
      int j=0;
      for(int i=0;i<len;i+=6) {
        String sValue =  sStr.substring(i,i+2);
        String sValue1=  sStr.substring(i+2,i+4);
        String sRand =  sStr.substring(i+4,i+6);
        int nValue = Integer.parseInt(sValue,16) * 256 + Integer.parseInt(sValue1,16);
        int nRand = Integer.valueOf(sRand,16).intValue();
        nValue = (nValue - 68) / nRand;
        if( nValue > 128 ) {
          nValue = nValue - 256;
        }
        b[j++] = (byte) nValue;
      }

      // de-Cipher
      desCipher.init(Cipher.ENCRYPT_MODE, userKey);
      byte[] cipherbyte = desCipher.doFinal(b);
      return  emisUtil.byteToStr(cipherbyte);
  }

  public synchronized String cipherDbData(String sData) throws Exception
  {
    byte [] b = sData.getBytes();
    desCipher.init(Cipher.ENCRYPT_MODE, dbKey);
    byte[] cipherbyte = desCipher.doFinal(b);
    return  emisUtil.byteToStr(cipherbyte);
  }

  public synchronized String deCipherDbData(String sData) throws Exception
  {
    byte [] b = emisUtil.strToByte(sData);
    desCipher.init(Cipher.DECRYPT_MODE,dbKey);
    byte[] cleanbyte = desCipher.doFinal(b);
    return  new String(cleanbyte);
  }


  public void setProperty(int propertyID,Object oValue) throws Exception
  {
  }
  /**
   * get the singleton emisCipherMgr Object
   */
  public static emisCipherMgr getInstance(ServletContext application) throws Exception
  {
      emisCipherMgr _oMgr = (emisCipherMgr) application.getAttribute(emisCipherMgr.STR_EMIS_CIPHERMGR);

      if( _oMgr == null )
      {
          emisTracer.get(application).sysError(null,emisError.ERR_SVROBJ_NOT_BIND,"emisCipherMgr");
      }
      return _oMgr;
  }

  public static void main (String [] argvs) throws Exception
  {
    String data = "85044E52B9C89735";
    emisServletContext con = new emisServletContext();
    Properties p = new Properties();
    p.put("emis.keystore","c:\\resin\\emis.key");
    emisCipherMgr m = new emisCipherMgr(con,p);
    System.out.println(m.deCipherDbData(data));

  }

}