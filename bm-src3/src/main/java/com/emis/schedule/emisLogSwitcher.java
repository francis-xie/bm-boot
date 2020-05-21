package com.emis.schedule;



import com.emis.trace.emisTracer;



/**

 *  此 Class 是要設定給 Scheduler 跑的

 *  和 emisTracer 是一起使用的

 *  可以將系統 log 檔關閉,並切換一個新的 log 檔

 *  這樣可以提供每天一個 log 檔的機置,但目前並無使用

 *

 *  @see com.emis.trace.emisTracer

 */

public class emisLogSwitcher extends emisTask

{

    public emisLogSwitcher()

    {

        super();

    }



    public void runTask() throws Exception

    {

        try {

            emisTracer oTr = emisTracer.get(super.oContext_);

            boolean isTrace = oTr.isTraceEnabled();

            if( ! isTrace ) return;

            oTr.reset();

        } catch (Exception ignore) {

            ignore.printStackTrace(System.out);

        }

    }

}