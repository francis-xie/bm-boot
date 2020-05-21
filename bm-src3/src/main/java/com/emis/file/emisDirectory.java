package com.emis.file;
import java.util.Enumeration;

/**
 *  代表一個目錄的 interface
 *
 *  @see com.emis.file.emisDirectoryImpl
 */
public interface emisDirectory
{
    /**
     *  取得目錄的代表名稱
     */
    String getDirectoryName();

    /**
     *  取得目錄的 full path
     */
    String getDirectory();

    /**
     *  把 c:\aa\bb\cc\t.txt
     *  換成 c:\\aa\\bb\cc\\t.txt
     */
    String getJavaScriptDirectory();

    /**
     *  此目錄相對於 Document Root 的相對路徑
     */
    String getRelative();

    /**
     *  取的子目錄,會自動建立子目錄
     */
    emisDirectory subDirectory(String sDir);

    /**
     *  取得 Factory 物件
     */
    emisFileFactory getFileFactory();

    /**
     *  取得代表此目錄下名為 sFileName
     *  檔名的 emisFile 物件
     */
    emisFile getFile(String sFileName)throws Exception;

    /**
     * get a list of files in this directory
     * 要用 emisFile 去轉型
     */
    Enumeration getFileList();

    /**
     * get a list of Files by filter in this directory
     * 要用 emisFile 去轉型
     * filter 可用 "*.*" , "*.TXT" 之類
     */
    Enumeration getFileList(String sFilter);

    /**
     * get a list of directories in this directory
     * 要用 emisDirectory 去轉型
     */
    Enumeration getDirList();

    /**
     * 可以將此目錄下符合 sFilter 的檔案清空
     */
    void cleanFile(String sFilter);

}
