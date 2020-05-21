package com.emis.trace;



public class emisException extends Exception
{
    public emisException( int nErrorCode,String sMessage )
    {
      super("ErrorNo="+ nErrorCode + ":"+sMessage);
    }

    public String toString()
    {
      return getMessage();
    }

}