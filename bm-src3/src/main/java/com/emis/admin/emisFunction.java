package com.emis.admin;

//import java.rmi.*;
//import javax.rmi.*;

abstract public class emisFunction//  extends PortableRemoteObject implements Remote
{

    private String sFunctionName_;
    public emisFunction (String sFunctionName) throws Exception
    {
        super();
        sFunctionName_ = sFunctionName;
    }

    public String getFunctionName() throws Exception
    {
        return sFunctionName_;
    }
    abstract void execute() throws Exception;



}