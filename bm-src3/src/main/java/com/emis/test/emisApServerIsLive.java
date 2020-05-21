package com.emis.test;



import com.emis.qa.emisServletContext;

import com.emis.server.emisServerFactory;

import com.emis.util.emisUtil;

//-import com.emis.schedule.download.caves.RunCAVES2Eros_Part;



import javax.servlet.ServletContext;



/**

 * Created by IntelliJ IDEA.

 * User: Administrator

 * Date: 2005/1/7

 * Time: 下午 01:06:37

 * To change this template use Options | File Templates.

 */

public class emisApServerIsLive {

    public  boolean isLive = false;

    private ServletContext oContext_;



    public emisApServerIsLive(ServletContext oContext_) {

        this.oContext_ = oContext_;

    }



    public boolean isLive() {



        emisTestDb emisTestDb = new emisTestDb(oContext_,this);

        Thread oInitThread = new Thread(emisTestDb);

        oInitThread.start();

        try{

        Thread.currentThread().sleep(5000);

        }catch(Exception e){;}

        if(oInitThread != null) oInitThread.interrupt();

        return isLive;

    }

    public static void main(String[] args) throws Exception {

         //20020311 增加驗證用

         emisServletContext servlet = new emisServletContext();

         emisServerFactory.createServer(servlet, "c:\\wwwroot\\caves",

     "c:\\resin3X\\caves.cfg", true);

         System.out.println("start to run:" + emisUtil.todayTimeS());

         emisApServerIsLive obj = new emisApServerIsLive(servlet);

         obj.isLive();

        System.out.println(obj.isLive());

         System.out.println("End of run:" + emisUtil.todayTimeS());

       }





}

