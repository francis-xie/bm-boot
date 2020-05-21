package com.emis.app.migration;



import com.emis.test.emisServletContext;

import com.emis.server.emisServerFactory;

//import com.emis.mail.emisMailer;



import javax.servlet.ServletContext;

import javax.mail.*;

import javax.mail.internet.*;

import java.io.*;

import java.util.*;

import java.lang.reflect.InvocationTargetException;



/**

 * User: merlin

 * Date: Apr 22, 2003

 * Time: 6:42:57 PM

 */

public class emisMiMailTarget extends emisMiTarget {

    String alias_;

    String from_;

    String to_;

    String subject_;

    String host_;

    String CC_;

    StringBuffer content_ = new StringBuffer();

//    emisMailer mailer_;

    private boolean useHTML_;





    InternetAddress[] EMIS_GetInternetAddress(String[] aMailto) {

        int _iCntr = 0;

        for (int i = 0; i < aMailto.length; i++) //計算有效的Mail Address

        {                                 //排除空白部份!!

            if (!EMIS_Empty(aMailto[i]))

                _iCntr++;



        }

        InternetAddress[] address = new InternetAddress[_iCntr];

        _iCntr = 0;

        try {

            for (int i = 0; i < aMailto.length; i++) {

                if (!EMIS_Empty(aMailto[i])) {

                    address[_iCntr] = new InternetAddress(aMailto[i]);

                    _iCntr++;

                }

            }



        } catch (MessagingException mex) {

            System.out.println(mex.toString());

        }

        return address;

    }





//OverLoading EMIS_Sendmail()

//aMailCC......副本收件者

//lHTMLForm....本文檔是否是HTML格式[true/false]



    void EMIS_Sendmail(String cSubject, String cFrom, String[] aMailto, String cBody, String cHost, String[] aMailCC, boolean lHTMLFrom) {

        boolean debug = false;

        // create some properties and get the default Session

        Properties props = new Properties();

        props.put("mail.smtp.host", cHost);

        if (debug) props.put("mail.debug", cHost);



        Session _session = Session.getDefaultInstance(props, null);

        _session.setDebug(debug);



        try {

            // create a message

            //Message msg = new MimeMessage(_session);

            MimeMessage msg = new MimeMessage(_session);

            msg.setFrom(new InternetAddress(cFrom));                     //寄件者

            if (aMailto != null) {

                InternetAddress[] address = EMIS_GetInternetAddress(aMailto); //收件者

                msg.setRecipients(Message.RecipientType.TO, address);

            }

            if (aMailCC != null) {

                InternetAddress[] CCaddress = EMIS_GetInternetAddress(aMailCC); //副本收件者

                msg.setRecipients(Message.RecipientType.CC, CCaddress);

            }

//            msg.setSubject(new String(cSubject.getBytes("UTF-8"), "ISO8859_1"));

            msg.setSubject(cSubject);

            //主旨

            msg.setSentDate(new java.util.Date());

            // If the desired charset is known, you can use

            // setText(text, charset)

            msg.setText(cBody, "UTF-8");

            msg.setHeader("Content-Transfer-Encoding", "8bit");

            if (lHTMLFrom) {

                msg.setHeader("Content-Type", "text/html; charset=UTF-8");   //使用HTML語法!

            } else {

                msg.setHeader("Content-Type", "charset=UTF-8");   //使用HTML語法!

            }

            Transport.send(msg);

        } catch (MessagingException mex) {

            System.out.println(mex.toString());

//        } catch (UnsupportedEncodingException e) {

//            e.printStackTrace();  //To change body of catch statement use Options | File Templates.

        }

    }





    private String[] tokensToArray(String sTarget) {

        if (sTarget == null)

            return null;

        StringTokenizer st = new StringTokenizer(sTarget, ";");

        int n = st.countTokens();

        String[] receiver = new String[n];



        int i = 0;

        while (st.hasMoreTokens()) {

            String s = st.nextToken().trim();

            if (s != null && s.length() > 0) {

                if (s.indexOf("@") < 0) {

                    s = s + "@" + host_;

                }

                receiver[i++] = s;

            }

        }

        return receiver;

    }











//    void EMIS_Sendmail(String cSubject, String cFrom, String[] aMailto,

//                     String cBody, String cHost, String[] aMailCC, boolean lHTMLFrom) {

    public boolean close(boolean closeDb) throws IOException {

        if (this.useHTML_) {

            content_.append("</Table>");

            content_.append("</BODY></HTML>");

        }

        System.setProperty("mail.smtp.host", host_);

        try {

//            mailer_ = new emisMailer();

            String[] receiver = tokensToArray(to_);

            String[] _aCC = tokensToArray(CC_);

            if (alias_ != null)

                from_ = new String(alias_.getBytes("UTF-8"), "ISO8859_1") + "<" + from_ + ">";

            EMIS_Sendmail(subject_ + this.getFileName(), from_, receiver,

                    content_.toString(), host_, _aCC, true);

        } catch (Exception e) {

            e.printStackTrace();  //To change body of catch statement use Options | File Templates.

        }

        return true;

    }



//    public boolean close(boolean closeDb) throws IOException {

//        if (this.useHTML_) {

//            content_.append("</Table>");

//            content_.append("</BODY></HTML>");

//        }

//        System.setProperty("mail.smtp.host", host_);

//        try {

//            mailer_ = new emisMailer();

//            StringTokenizer st = new StringTokenizer(to_, ";");

//            while (st.hasMoreTokens()) {

//                String receiver = st.nextToken().trim();

//                if (receiver != null && receiver.length() > 0) {

//                    mailer_.setFrom("", from_);

//                    mailer_.setSubject(subject_ + this.getFileName());

//                    mailer_.setContent(content_.toString());

//                    if (receiver.indexOf("@") < 0) {

//                        receiver = receiver + "@" + host_;

//                    }

//                    mailer_.setTo(receiver);

//                    mailer_.send();

//                }

//            }

//        } catch (Exception e) {

//            e.printStackTrace();  //To change body of catch statement use Options | File Templates.

//        }

//        return true;

//    }



    public boolean write(String[] data) throws IOException {

        if (this.useHTML_) {

            content_.append("<tr>");

        }



        for (int i = 0; i < data.length; i++) {

            String s = data[i];

            if (this.useHTML_) {

                content_.append("<td>");

            }

            content_.append(s);

            if (this.useHTML_) {

                content_.append("</td>");

            }

        }

        if (this.useHTML_) {

            content_.append("</tr>");

        }

        content_.append("\n");

        return true;

    }



    public boolean open(emisMiConfig config) throws Exception {

        if (this.useHTML_) {

            content_.append("<HTML><BODY>");

            content_.append("<Table border='1'>");

            content_.append("<tr>");

        }



        emisMiField fields[] = this.config.getTargetFields();

        for (int i = 0; i < fields.length; i++) {

            emisMiField f = fields[i];

            if (this.useHTML_) {

                content_.append("<td>");

            }

            String s = f.getName();

            int len = f.getLength();

            if (s.length() > len) {

                s = s.substring(0, len);

                content_.append(s);

            } else {

                content_.append(s);

                int diff = len - s.length();

                while (diff > 0) {

                    content_.append(' ');

                    diff--;

                }

            }

            if (this.useHTML_) {

                content_.append("</td>");

            }

        }



        if (this.useHTML_) {

            content_.append("</tr>");

        }

        content_.append("\n");

        return true;

    }



    void parse(Hashtable h) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {

        super.parse(h);

        Iterator it = h.keySet().iterator();

        String key;

        String val;

        while (it.hasNext()) {

            key = (String) it.next();

            val = (String) h.get(key);

            if (key.equalsIgnoreCase("To")) {

                to_ = val;

            } else if (key.equalsIgnoreCase("From")) {

                from_ = val;

            } else if (key.equalsIgnoreCase("Host")) {

                host_ = val;

            } else if (key.equalsIgnoreCase("subject")) {

                subject_ = val;

            } else if (key.equalsIgnoreCase("CC")) {

                CC_ = val;

            } else if (key.equalsIgnoreCase("ALIAS")) {

                alias_ = val;

            } else if (key.equalsIgnoreCase("HTML")) {

                useHTML_ = (val.toLowerCase().equals("true"));

            }

        }

    }



    public void append(String path[], boolean reopen) throws Exception {

    }



    public boolean clearTemp() {

        return false;

    }



    public static void main(String[] args) {

        ServletContext _oContext = new emisServletContext();

        try {

            emisServerFactory.createServer(_oContext, "c:\\wwwroot\\wtn",

                    "c:\\resin3X\\wtn.cfg", true);

            emisMigration migration = new emisMigration(_oContext, "PLU");

            migration.run();

        } catch (Exception e) {

            e.printStackTrace();  //To change body of catch statement use Options | File Templates.

        }



    }



    boolean EMIS_Empty(String s) {

        if (s != null && s.length() > 0)

            return false;

        return true;

    }



}

