package com.emis.mail;


import com.emis.db.emisProp;
import com.emis.qa.emisServletContext;
import com.emis.server.emisServerFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import javax.activation.*;
import javax.servlet.ServletContext;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 2013/6/24
 * Time: 上午 8:54
 * To change this template use File | Settings | File Templates.
 */
public class emisMailNew {
  private String host = "";  //smtp服务器
  private String from = "";  //发件人地址
  private InternetAddress[] to ;    //收件人地址
  private ArrayList  affix=new ArrayList(); //附件地址
  private ArrayList affixName=new ArrayList(); //附件名称
  private String user = "";  //用户名
  private String pwd = "";   //密码
  private String subject = ""; //邮件标题
  private emisProp oProp;
  private String content="";//郵件內容

  public void setAddress(String from,String to,String subject,String content)throws  Exception{
    this.from = from;
    this.to   = InternetAddress.parse(to,false);
    this.subject = subject;
    this.content=content;
  }

  public void setAffix(String affix,String affixName){
    if(affix!=null){
      this.affix .add(affix) ;
      this.affixName.add(affixName);
    }
  }

  public void send(ServletContext application) throws Exception{
    oProp = emisProp.getInstance(application);

    Properties props = new Properties();

    //设置发送邮件的邮件服务器的属性（这里使用网易的smtp服务器）
    props.put("mail.smtp.host", oProp.get("EROS_MAILSERVER"));
    //需要经过授权，也就是有户名和密码的校验，这样才能通过验证
    props.put("mail.smtp.auth", oProp.get("MAIL_SMTP_AUTH"));

    final String user = oProp.get("MAIL_SMTP_USER");
    final String password = oProp.get("MAIL_SMTP_PWD");
    //用刚刚设置好的props对象构建一个session
    //Session session = Session.getDefaultInstance(props);
    // 这里不用getDefaultInstance取得session, 因在有些客户环境会取不到session(暂不知道什么原因)
    Session session = Session.getInstance(props,
        new Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            try {
              return new PasswordAuthentication(user, password);
            } catch (Exception ex) {
              ex.printStackTrace();
              return null;
            }
          }
        });

    //有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
    //用（你可以在控制台（console)上看到发送邮件的过程）
   // session.setDebug(true);

    //用session为参数定义消息对象
    MimeMessage message = new MimeMessage(session);
    try{
      //加载发件人地址
      message.setFrom(new InternetAddress(from));
      //加载收件人地址
      message.setRecipients(Message.RecipientType.TO,to);
      //加载标题
      message.setSubject(subject);

      // 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
      Multipart multipart = new MimeMultipart();


      //设置邮件的文本内容
      BodyPart contentPart = new MimeBodyPart();
      contentPart.setContent(content,"text/html;charset=UTF-8");
      multipart.addBodyPart(contentPart);
      //添加附件
      for(int i=0;i<affix.size();i++){
      BodyPart messageBodyPart= new MimeBodyPart();
      DataSource source = new FileDataSource(affix.get(i).toString());
      //添加附件的内容
      messageBodyPart.setDataHandler(new DataHandler(source));
      //添加附件的标题
      //这里很重要，通过下面的Base64编码的转换可以保证你的中文附件标题名在发送时不会变成乱码
      sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
      messageBodyPart.setFileName(MimeUtility.encodeText(affixName.get(i).toString()));
      multipart.addBodyPart(messageBodyPart);
     }

      //将multipart对象放到message中
      message.setContent(multipart);
      //保存邮件
      message.saveChanges();
      //   发送邮件
      Transport transport = session.getTransport("smtp");
      //连接服务器的邮箱
      transport.connect(oProp.get("EROS_MAILSERVER"),oProp.get("MAIL_SMTP_USER"),oProp.get("MAIL_SMTP_PWD"));
      //把邮件发送出去
      try {
        transport.sendMessage(message, message.getAllRecipients());
      } catch(SendFailedException e){
        // 针对未发送的有效地址重新发送一次，防止群发时因其中一个mail地址错误而造成所有人都发送失败的情况。
        if(e.getValidUnsentAddresses() != null){
          List<Address> toList = new ArrayList<Address>();

          if(to != null && to.length > 0)
            for(Address tmpTo: e.getValidUnsentAddresses()) {
              if(toList.contains(tmpTo)) continue;
              for (Address addr : to)
                if (tmpTo.toString().equals(addr.toString())) toList.add(tmpTo);
            }

          to = toList.toArray(new InternetAddress[toList.size()]);
          if (to.length > 0) {
            message.setRecipients(Message.RecipientType.TO, to);

            transport.sendMessage(message, message.getAllRecipients());
          }
          toList.clear();
          toList = null;
        }
        if(e.getInvalidAddresses() != null){
          List<String> invalidList = new ArrayList<String>();
          for(Address invalid : e.getInvalidAddresses()) invalidList.add(invalid.toString());

          throw new Exception("Invalid Addresses:" + invalidList.toString());
        }
      }
      transport.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}
