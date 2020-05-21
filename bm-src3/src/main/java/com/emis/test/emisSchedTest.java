package com.emis.test;


import com.emis.schedule.emisTask;
import com.emis.trace.emisTracer;
import com.emis.util.emisUtil;

public class emisSchedTest extends emisTask
{

    public void runTask() throws Exception
    {
        emisTracer Tr = emisTracer.get(super.oContext_);
        Tr.info("[" + super.sName_+"]" + emisUtil.getLocaleCalendar().getTime());
    }

}