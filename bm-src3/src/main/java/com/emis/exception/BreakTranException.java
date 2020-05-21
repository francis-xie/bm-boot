package com.emis.exception;
/*
 * Created by IntelliJ IDEA.
 * User: Roland
 * Date: 2002/10/21
 * Time: 下午 04:11:54
 * To change this template use Options | File Templates.
 */
public class BreakTranException extends RuntimeException{
        /** The error code to look up */
    private String code;

    /** The error message corresponding to an error code */
    private String message;

    public BreakTranException ( String code, String message ) {
        this( message );
        this.code = code;
    }

    public BreakTranException ( String message ) {
        super( message );
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public String getCode()
    {
        return code;
    }
}
