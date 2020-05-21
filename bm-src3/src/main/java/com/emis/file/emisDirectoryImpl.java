package com.emis.file;

import com.emis.util.emisUtil;

import java.util.Enumeration;

/**
 *   emisDirectory 針對 local file system
 *   的實作
 */
public class emisDirectoryImpl implements emisDirectory
{
    private String sName_;
    private String sDirectory_;
    private String sRelative_;
    private emisFileFactory oFactory_ ;
    private String sSeparator_ ;

    // it is important to use protected ?
    protected emisDirectoryImpl(String sName,String sDirectory,String sRelative,emisFileFactory oFactory)
    {
          sName_ = sName.toLowerCase();
          sDirectory_ = sDirectory;
          oFactory_ = oFactory;
          sRelative_ = sRelative;
          sSeparator_ = oFactory.getFileSeparator();
          checkDirectory();
    }

    public String getDirectoryName() { return sName_; }
    public String getDirectory() { return sDirectory_; }

    public String getJavaScriptDirectory()
    {
        // 只有 DOS 的反斜要轉換成兩個反斜
        return emisUtil.stringReplace(sDirectory_,"\\","\\\\","a");
    }

    private void checkDirectory()
    {
        sDirectory_ = emisUtil.checkDirectory(sDirectory_,sSeparator_);
        sRelative_  = emisUtil.checkDirectory(sRelative_,"/");
    }

    public emisFile getFile(String sFileName) throws Exception
    {
      return oFactory_.getFile(this,sFileName);
    }

    public emisDirectory subDirectory(String sDir)
    {
         if( sDir != null)
         {
                String _sDirectory = sDirectory_.concat(sDir).concat(sSeparator_);
                String _sRelative  = null;
                if( sRelative_ != null )
                {
                    _sRelative = sRelative_ + sDir + "/";
                }

                emisDirectoryImpl _oDirectory = new emisDirectoryImpl(sName_, _sDirectory,_sRelative,oFactory_);
                if( (( emisFileFactoryBase)oFactory_).exists(_oDirectory))
                    return _oDirectory;

                if( ((emisFileFactoryBase)oFactory_).mkdir(_oDirectory))
                    return _oDirectory;
         }
         return null;
    }

    public emisFileFactory getFileFactory()
    {
        return oFactory_;
    }
    public String getRelative()
    {
        return sRelative_;
    }

    public Enumeration getFileList()
    {
        return oFactory_.getFileList(this);
    }
    public Enumeration getDirList()
    {
        return oFactory_.getDirList(this);
    }
    public Enumeration getFileList(String sFilter)
    {
        return oFactory_.getFileList(this,sFilter);
    }


    public void cleanFile(String sFilter)
    {
      oFactory_.cleanFile(this,sFilter);
    }
}