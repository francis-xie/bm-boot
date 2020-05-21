package com.emis.report;

import com.emis.user.emisUser;

public interface emisReportBuilder
{

    public abstract emisReport getReport(String s, emisUser emisuser)
        throws Exception;
}
