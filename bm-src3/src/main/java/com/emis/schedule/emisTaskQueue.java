package com.emis.schedule;



import java.util.ArrayList;

import java.util.Iterator;



/**

 *  emisScheduleMgr 用來管理 emisTask 的 Queue Class

 *  

 *  @see com.emis.schedule.emisScheduleMgr

 */

public class emisTaskQueue

{

    private ArrayList oQueue_ = new ArrayList();



    public emisTaskQueue()
    {

    }



    public synchronized void add(emisTask oTask)

    {

        oTask.state = emisTask.SCHEDULED;

        oQueue_.add(oTask);


        //if( getMin() == oTask ) {
        notify();
        //}

    }





    public synchronized void clear()
    {
        oQueue_.clear();
    }



    public synchronized boolean isEmpty()
    {
       return ( oQueue_.size() ==0);
    }


    // 拿取 state 為 SCHEDULED ,的最小的
    public synchronized emisTask getMin(String sThreadGroup)
    {

        if( oQueue_.size() <= 0 ) return null;
        
        

        emisTask oTask = null;

        long MinTime = Long.MAX_VALUE;

        int nMin = -1;

        long eachTime;
        //long Now = System.currentTimeMillis();

        for(int idx=0; idx < oQueue_.size() ; idx++) {
            oTask = (emisTask) oQueue_.get(idx);
            
            // robert, 加上 threadGroup 判斷
            if( !sThreadGroup.equalsIgnoreCase( oTask.getThreadGroup() ) ) {
            	continue;
            }
            
            eachTime = oTask.nextExecutionTime();
            /*
            // Robert , 2010/12/1 ,發現似乎有執行時間以經是 "過去時間" 的 bug 
            // 所以在這邊加一個檢查
            if( (eachTime < Now) && (oTask.getRunLevel() != emisTask.RUN_BY_SPECIFY) ) {
         	   // 這不應該發生,如果發生,表示這個 Task 不會再被執行,這邊做個補強
            	oTask.setNextExecutetion(Now);
            	continue;
            }
            */
            if( (oTask.state == emisTask.SCHEDULED) && (eachTime < MinTime) )   {
               nMin = idx;
               MinTime = eachTime;
            }
            
        }
        if( nMin != -1 )
        	return (emisTask) oQueue_.get(nMin);
        else
        	return null;

    }



    public synchronized void remove( emisTask oTask )

    {
        if(oTask != null )  {
           oQueue_.remove(oTask);
        }
    }

    
    public synchronized void removeTaskCanceled () {
    	int idx=0;
    	emisTask oTask = null;
    	while(idx < oQueue_.size())
        {
            oTask = (emisTask) oQueue_.get(idx);
            if( oTask.state == emisTask.CANCELLED ) {
            	oQueue_.remove(idx);
            } else {
            	idx++;
            }
        }
    }


    public synchronized Iterator iterator()

    {

      return oQueue_.iterator();

    }

    public synchronized emisTask get(int idx)

    {

        return (emisTask) oQueue_.get(idx);

    }



    public synchronized int size()

    {

        return oQueue_.size();

    }

}