/*
 * $Header: /repository/src3/src/com/emis/file/emisFileFactoryBase.java,v 1.1.1.1 2005/10/14 12:42:09 andy Exp $
 *
 * Copyright (c) EMIS Corp.
 */
package com.emis.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;

/**
 * 抽像類別用來定義 FileFactory
 */
abstract public class emisFileFactoryBase implements emisFileFactory
{
    protected HashMap oDirectories_ = new HashMap();
    protected String sFileSeparator_;

    abstract public PrintWriter getWriter(String sDirectoryName,String sFilename,String sMode) throws Exception;
    abstract public PrintWriter getWriter(emisDirectory oDirectory,String sFileName,String sMode) throws Exception;

    abstract public Reader getReader(String sDirectoryName,String sFileName) throws Exception;
    abstract public Reader getReader(emisDirectory oDirectory,String sFileName) throws Exception;

    abstract public InputStream getInStream(String sDirectoryName,String sFileName) throws Exception;
    abstract public OutputStream getOutStream(String sDirectoryName,String sFileName,String sMode) throws Exception;
    abstract protected boolean mkdir(emisDirectory oDir);
    abstract protected boolean exists(emisDirectory oDir);
    protected void register(emisDirectory oDir)
    {
        oDirectories_.put(oDir.getDirectoryName(),oDir);
    }

    protected void register(String sRegName,emisDirectory oDir)
    {
        oDirectories_.put(sRegName,oDir);
    }

    /** 以傳入的目錄註冊值找出<code>emisDirectory</code>物件 */
    public emisDirectory getDirectory(String sDirectoryName) throws Exception
    {
        emisDirectory _oDirectory = (emisDirectory) oDirectories_.get(sDirectoryName);
        if( _oDirectory == null ) throw new Exception("Directory Name:" + sDirectoryName + " not exists");
        return _oDirectory;
    }

    public String getFileSeparator()
    {
        return sFileSeparator_;
    }
}