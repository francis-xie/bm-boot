/* $Id: emisMailer.java 9571 2017-09-12 03:37:50Z andy.he $

 *

 * Copyright (c) 2004 EMIS Corp. All Rights Reserved.

 */

package com.emis.mail;

import com.emis.db.emisProp;
import com.emis.trace.emisTracer;
import com.emis.user.emisUser;
import sun.misc.BASE64Encoder;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * the mail function use Java-Mail 1.2
 *
 * @version 2004/10/29 Jerry: upgrade to JavaMail 1.3.2, HTML mail
 *          Track+[11780] Reejay 2008/10/22 2W單據傳送 增加郵件發送功能
 *          Track+[13796] tommer.xie 2009/11/26 將發送郵件參數添加至EMISPROP系統參數表，由用戶自行設定。
 *          2015/09/23 Joe Modify： 同步SME_SM中修正的中文主旨乱码问题
 */
public class emisMailer {
  private InternetAddress From_;
  private InternetAddress[] To_;
  private InternetAddress[] CC_;
  private String sSubject_;
  private List sAttachment_ = new ArrayList();
  private StringBuffer sBuf_ = new StringBuffer();
  private Properties prop_; // [1165 ] merlin
  private static String INTERNET_ENCODING = "UTF-8";
  private emisProp oProp;
  private boolean bEncodeSubject = true;


  public emisMailer() throws Exception {
    this(null, null);
  }

  // [1165 ] merlin start
  public emisMailer(Properties prop) {
    prop_ = prop;
  }

  /**
   * 使用個人的 email address 當成 From address
   */
  public emisMailer(emisUser oUser, Properties prop) throws Exception {
    prop_ = prop;
    if (oUser != null) {
      // emisUser 會傳回 email address
      String _sName = oUser.getName();
      if ((_sName != null) && (!"".equals(_sName))) {
        setFrom(_sName, oUser.getMailAddr());
      } else {
        setFrom(oUser.getMailAddr());
      }
    }
  }
// [1165 ] merlin end

  /**
   * 使用個人的 email address 當成 From address
   *
   * @deprecated // [1165 ] merlin
   */
  public emisMailer(emisUser oUser) throws Exception {
    this(oUser, null); // [1165 ] merlin
  }

  public void setFrom(String sFrom) throws Exception {
    if ((sFrom == null) || ("".equals(sFrom))) return;
    From_ = new InternetAddress(sFrom);
  }

  public void setFrom(String sPersonalName, String sFrom) throws Exception {
    if ((sFrom == null) || ("".equals(sFrom))) return;
    if (sPersonalName == null) sPersonalName = "";
    From_ = new InternetAddress(sFrom, sPersonalName, INTERNET_ENCODING);
  }

  public void setTo(String sTo) throws Exception {
    if ((sTo != null) && (!"".equals(sTo))) {
      //針對向多個人發送郵件時 add by tommer.xie 2009/11/26
      String[] sToEmails = sTo.trim().split(";");
      int receiverCount = sToEmails.length;
      To_ = new InternetAddress[receiverCount];
      for (int i = 0; i < receiverCount; i++) {
        To_[i] = new InternetAddress(sToEmails[i]);
      }
    }
  }

  public void setCC(String sCC) throws Exception {
    if ((sCC != null) && (!"".equals(sCC))) {
      //針對向多個人發送郵件時 add by tommer.xie 2009/11/26
      String[] sToEmails = sCC.trim().split(";");
      int receiverCount = sToEmails.length;
      CC_ = new InternetAddress[receiverCount];
      for (int i = 0; i < receiverCount; i++) {
        CC_[i] = new InternetAddress(sToEmails[i]);
      }
    }
  }

  public void setSubject(String sSubject) {
    sSubject_ = sSubject;
  }

  public void setContent(String sContent) {
    if (sContent != null) {
      sBuf_.setLength(0);
      sBuf_.append(sContent);
    }
  }

  public void println(String sContent) {
    sBuf_.append(sContent);
    sBuf_.append("\n");
  }

  public void setEncodeSubject(boolean bEncodeSubject) {
    this.bEncodeSubject = bEncodeSubject;
  }

  public void send(ServletContext application, HttpServletRequest request) throws Exception {
    // from 是可有可無
    if (From_ == null) {
      setFrom(request.getParameter("MAIL_FROM"));
    }
    setTo(request.getParameter("MAIL_TO"));
    setCC(request.getParameter("MAIL_CC"));
    sSubject_ = request.getParameter("MAIL_SUBJECT");
    setContent(request.getParameter("MAIL_CONTENT"));
    setAttachment(request.getParameter("MAIL_ATTACHMENT"));
    send(application);
  }

  /**
   * 傳入 Application Server 上的檔名
   */
  public void setAttachment(String sFileName) throws Exception {
    if ((sFileName == null) || ("".equals(sFileName))) return;
    File f = new File(sFileName);
    if (f.exists() && f.isFile()) {
      sAttachment_.add(sFileName);
    } else {
      throw new Exception("File doesn't exists or is not a file");
    }
  }

  /**
   * 馬上寄出,不需經過 Mail Queue
   */
  public void send(ServletContext application) throws Exception {
    send(application, true);
  }

  public void send(ServletContext application, boolean bMailImmediately) throws Exception {
    if(application == null)
      throw new Exception("application不能为空");
    MimeMessage msg = null;
    Multipart oMultipart_ = null;
    oProp = emisProp.getInstance(application);
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

    // 若要密碼 請user 設定一個 admin emissuper
    //PasswordAuthentication password = new PasswordAuthentication(oProp.get("MAIL_SMTP_USER"), oProp.get("MAIL_SMTP_PWD"));
    //判斷System的property中是否有mail.smtp.auth這個參數 add by tommer.xie 2009/11/25
    //如果缺少這個參數會報com.sun.mail.smtp.SMTPSendFailedException: 553 sorry, The sender of local domains must authentication
    //即你的域名需要驗證
    // 20150924 Joe 特别说明：此处参数优先取EMISPROP参数，若取不到则取CFG档配置参数
    Properties p = System.getProperties();
    final String host = oProp.get("MAIL_SMTP_HOST", p.getProperty("mail.smtp.host"));
    final String address = oProp.get("MAIL_SMTP_ADDRESS", p.getProperty("mail.smtp.address"));
    final String user = oProp.get("MAIL_SMTP_USER", p.getProperty("mail.smtp.user"));
    final String password = oProp.get("MAIL_SMTP_PWD", p.getProperty("mail.smtp.password"));
    final String auth = oProp.get("MAIL_SMTP_AUTH", p.getProperty("mail.smtp.auth", "false"));
    final String port = oProp.get("MAIL_SMTP_PORT", p.getProperty("mail.smtp.port", "25"));
    p.put("mail.smtp.host", host);
    p.put("mail.smtp.auth", "".equals(auth) ? "true" : auth);
    p.put("mail.smtp.port", "".equals(port) ? "25" : port);
    // 20150924 Joe add 增加默认以参数设定值发件人，特殊情况可以自己调用setFrom改变
    if (From_ == null) {
      if (address != null && !"".equals(address.trim()) && user != null && !"".equals(user.trim())) {
        this.setFrom(user, address);
      } else if (address != null && !"".equals(address.trim())) {
        this.setFrom(address);
      }
    }
    try {
      // Get a Session object
      //这里不用getDefaultInstance取得session, 因在有些客户环境会取不到session(暂不知道什么原因)
      Session session = (prop_ == null)                          // [1165 ] merlin start
          ? Session.getInstance(p,
          new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
              try {
                return new PasswordAuthentication(user, password);
              } catch (Exception ex) {
                ex.printStackTrace();
                return null;
              }
            }
          })
          : Session.getInstance(prop_, null);                 // [1165 ] merlin end

//        session.setPasswordAuthentication(new URLName(System.getProperty("mail.smtp.host")), password);

      msg = new MimeMessage(session);

      if (From_ != null)
        msg.setFrom(From_);
      else
        msg.setFrom();

      if (To_ == null)
        throw new Exception("unable to send null recipient");
      msg.setRecipients(Message.RecipientType.TO, To_);

      if (CC_ != null) {
        msg.setRecipients(Message.RecipientType.CC, CC_);
      }
      if (sSubject_ != null) {
        //   System.out.println("sSubject_="+sSubject_);
        if (this.bEncodeSubject) {
          // 解決亂碼
          BASE64Encoder enc = new BASE64Encoder();//該類位於jre/lib/rt.jar中
          //fds為FileDataSource實例
          msg.setSubject("=?" + INTERNET_ENCODING + "?B?" + enc.encode(sSubject_.getBytes(INTERNET_ENCODING)).replaceAll("\r\n", "") + "?=");
        } else {
          msg.setSubject(sSubject_);
        }
      }

//            String subject = msg.getSubject();
//            String s1[] = msg.getHeader("Subject");
//            System.out.println(s1[0]);
//            if(s1[0].indexOf("=?GBK?")!=-1) {
//            subject = MimeUtility.decodeText(subject);
//            }else if(s1[0].indexOf("=?gbk")!=-1) {
//            subject = MimeUtility.decodeText(subject);
//            }else if(s1[0].indexOf("=?utf-8")!=-1) {
//            subject = MimeUtility.decodeText(subject);
//            }else if(s1[0].indexOf("=?iso-8859-1?")!=-1){
//            subject = new String(subject.getBytes("iso-8859-1"),"GBK");
//            }else if(s1[0].indexOf("=?GB2312?")!=-1) {
//            subject = subject.replace("GB2312", "GBK");
//            subject = MimeUtility.decodeText(subject);
//            }else if(s1[0].indexOf("=?big5")!=-1) {
//            subject = subject.replace("big5", "GBK");
//            subject = MimeUtility.decodeText(subject);
//            }else{
//            subject = new String(subject.getBytes("iso-8859-1"),"GBK");
//            }

      oMultipart_ = new MimeMultipart();

      MimeBodyPart _oBody = new MimeBodyPart();
      _oBody.setContent(sBuf_.toString(), "text/html;charset=" + INTERNET_ENCODING);
      oMultipart_.addBodyPart(_oBody);

      //針對當多個附件傳送時處理 add by tommer.xie 2009/11/25
      if (sAttachment_ != null) {
        if (sAttachment_ != null) {
          for (int i = 0; i < sAttachment_.size(); i++) {
            FileDataSource _fds = new FileDataSource(sAttachment_.get(i).toString());

            MimeBodyPart _oAttach = new MimeBodyPart();

            DataHandler _dh = new DataHandler(_fds);

            _oAttach.setDataHandler(_dh);
            _oAttach.setFileName(MimeUtility.encodeWord(_fds.getName()));
            oMultipart_.addBodyPart(_oAttach);
          }
        }
      }

      msg.setContent(oMultipart_);
      msg.saveChanges();

      msg.setHeader("X-Mailer", "javamail_1.3.2");
      //    msg.setDataHandler(new DataHandler(
      //      new ByteArrayDataSource(sBuf_.toString(), "text/html;charset=" + INTERNET_ENCODING)));
      msg.setSentDate(new Date());
      // before send , log it to system...
      // if( application != null ) {
      if (!bMailImmediately) {
        // send to the mail queue
        emisMailQueue.getInstance(application).add(msg);
      } else {
        // send it right now
        Transport transport = session.getTransport("smtp");
        transport.connect(host, user, password);
        try {
          transport.sendMessage(msg, msg.getAllRecipients());
        } catch(SendFailedException e){
          // 针对未发送的有效地址重新发送一次，防止群发时因其中一个mail地址错误而造成所有人都发送失败的情况。
          if(e.getValidUnsentAddresses() != null){
            List<Address> toList = new ArrayList<Address>();
            List<Address> ccList = new ArrayList<Address>();
            if(To_ != null && To_.length > 0)
              for(Address tmpTo: e.getValidUnsentAddresses()) {
                if(toList.contains(tmpTo)) continue;
                for (Address to : To_)
                  if (tmpTo.toString().equals(to.toString())) toList.add(tmpTo);
              }

            if(CC_ != null && CC_.length > 0)
              for(Address tmpCc: e.getValidUnsentAddresses()) {
                if(toList.contains(tmpCc)) continue;
                for (Address cc : CC_)
                  if (tmpCc.toString().equals(cc.toString())) ccList.add(tmpCc);
              }

            To_ = toList.toArray(new InternetAddress[toList.size()]);

            if (To_.length > 0) {
              msg.setRecipients(Message.RecipientType.TO, To_);
              CC_ = ccList.toArray(new InternetAddress[ccList.size()]);
              if (CC_.length > 0) msg.setRecipients(Message.RecipientType.CC, CC_);
              else  msg.setRecipients(Message.RecipientType.CC, "");

              transport.sendMessage(msg, msg.getAllRecipients());
            }
            toList.clear();
            toList = null;
            ccList.clear();
            ccList = null;
          }
          if(e.getInvalidAddresses() != null){
            List<String> invalidList = new ArrayList<String>();
            for(Address invalid : e.getInvalidAddresses()) invalidList.add(invalid.toString());

            throw new Exception("Invalid Addresses:" + invalidList.toString());
          }
        }
        transport.close();
//          Transport.send(msg);
      }
    } catch (Exception ignore) {
      ignore.printStackTrace();
      if (application != null) {
        try {
          emisTracer.get(application).warning(this, ignore);
        } catch (Exception ignore1) {
        }
      }
      throw ignore;
    } finally {
      // free object reference
      msg = null;
      From_ = null;
      To_ = null;
      CC_ = null;
      sSubject_ = null;
      sAttachment_ = null;
    }
  }

  public static void main(String argv[]) throws Exception {
/*
    emisMailer m = new emisMailer();
    emisHttpServletRequest r = new emisHttpServletRequest();
    r.setParameter("MAIL_FROM","robert@mail.emis.com.tw");
    r.setParameter("MAIL_TO","csf@mail.emis.com.tw");
    r.setParameter("MAIL_CC","csf24@mail.emis.com.tw");
    r.setParameter("MAIL_SUBJECT","csf24@mail.emis.com.tw");
    r.setParameter("MAIL_CONTENT","TEST");
    m.send(r);
*/
//       System.setProperty("mail.transport.protocol", "smtp");
//       System.setProperty("mail.smtp.host", "smtp.gmail.com");
//       System.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//       System.setProperty("mail.smtp.socketFactory.fallback", "false");
//       System.setProperty("mail.smtp.port", "465");
//       System.setProperty("mail.smtp.socketFactory.port", "465");
//       System.setProperty( "mail.smtp.auth ", "true");

//       System.setProperty("mail.debug", "true");
//       System.setProperty("mail.smtp.starttls.enable","true");

//       System.setProperty("mail.smtp.password", "100902agemma");
//       System.setProperty("mail.smtp.user",  "agemmamail");
//       System.setProperty("mail.smtp.quitwait", "false");

//    System.setProperty("mail.smtp.host","mail.emis.com.tw");

//     Properties props = System.getProperties();
//      props.setProperty("mail.smtp.host", "smtp.gmail.com");
//      props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//      props.setProperty("mail.smtp.socketFactory.fallback", "false");
//      props.setProperty("mail.smtp.port", "465");
//      props.setProperty("mail.smtp.socketFactory.port", "465");
//      props.put("mail.smtp.auth", "true");

    emisMailer m = new emisMailer();
    m.setFrom("tommer.xie@emiszh.com");
    m.setTo("370913063@qq.com");
    m.setSubject("TEST");
    m.setContent("dfdf");
    //m.setAttachment("");
    //m.send();

   /* m.setFrom("管峰益","robert@mail.emis.com.tw");
    m.setTo("robert@mail.emis.com.tw");
    m.setSubject("TEST");
    m.setContent("Jerry B");
    m.send();
     */

  }
}

