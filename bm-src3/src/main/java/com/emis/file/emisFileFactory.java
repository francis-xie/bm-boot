package com.emis.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Enumeration;
import java.util.zip.ZipOutputStream;


public interface emisFileFactory
{
    PrintWriter getWriter(String sDirectoryName,String sFileName,String sMode) throws Exception;
    PrintWriter getWriter(emisDirectory oDirectory,String sFileName,String sMode) throws Exception;

    Reader getReader(String sDirectoryName,String sFileName) throws Exception;
    Reader getReader(emisDirectory oDirectory,String sFileName) throws Exception;

    InputStream getInStream(String sDirectoryName,String sFileName) throws Exception;
    InputStream getInStream(emisDirectory oDirectory,String sFileName) throws Exception;

    OutputStream getOutStream(String sDirectoryName,String sFileName,String sMode) throws Exception;
    OutputStream getOutStream(emisDirectory oDirectory,String sFileName,String sMode) throws Exception;
    ZipOutputStream getZipOutStream(String sDirectoryName,String sFileName,String sExtractName,String sMode) throws Exception;
    ZipOutputStream getZipOutStream(emisDirectory oDirectory,String sFileName,String sExtractName,String sMode) throws Exception;




    emisDirectory getDirectory(String sDirectoryName) throws Exception;

    emisFile getFile(String sDirectoryName,String sFileName) throws Exception;
    emisFile getFile(emisDirectory oDirectory,String sFileName) throws Exception;

    String getFileSeparator() ;
    Enumeration getFileList(emisDirectory oDir);
    Enumeration getFileList(emisDirectory oDir,String sFilter);
    Enumeration getDirList(emisDirectory oDir);

    /**
     * 將此目錄下符合
     * sFilter 的檔都清掉,
     * sFilter 目前只支援 '*' 和 "*.java" 兩種格式
     */
    void cleanFile(emisDirectory oDir,String sFilter);
}

