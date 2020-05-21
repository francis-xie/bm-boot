/*
 * @author  Abel li
 *
 * All rights reserved.
 */
package com.emis.exception;


public class AppException extends RuntimeException 
{
    /** The error code to look up */
    private int code;

    /** The error message corresponding to an error code */
    private String message;

    public AppException ( int code, String message ) {
        this( message );
        this.code = code;
    }

    public AppException ( String message ) {
        super( message );
        this.message = message;
    }
    
    public String getMessage()
    {
        return message;
    }

    public int getCode()
    {
        return code;
    }
}
