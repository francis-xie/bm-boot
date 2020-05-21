package com.emis.report;

import com.emis.business.emisDataSrc;
import com.emis.db.emisDb;
import com.emis.util.emisChinese;
import com.emis.util.emisUtil;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class emisTabReport extends emisReportBase
{
  public emisTabReport(emisRptProvider oProvider) throws Exception
  {
    super(oProvider);
  }

  private boolean hasMoreData_ = false;
  private int nWidth_;


  private ArrayList oTitleLines_ = new ArrayList();
  private ArrayList oDataFld_   = new ArrayList();
  private emisTr oDataLine_ = new emisTr();
  private emisTr sMarkLine_ ;
  private emisTr oHead_ ;
  private emisTr oHeadUnderLine_;
  private emisTd oPageNum_;


  public void printRpt() throws Exception
  {
    prepareText();

    emisDataSrc _oData = oProvider_.getDataSrc("xmlData");
    if( _oData == null )
        throw new Exception("無法取得 emisDataSrc 物件:xmlData");

    emisDb oDb = _oData.processSQL();
    try {

      printTitle();
      printHeader();


      hasMoreData_ = oDb.next();
      while(hasMoreData_)
      {
        for(int i=0;i<oDataFld_.size();i++)
        {
          emisTd td = oDataLine_.get(i);
          td.setContent( oDb.getString( (String) oDataFld_.get(i)) );
        }
        hasMoreData_ = oDb.next();
        printTable(oDataLine_,A_CENTER,nWidth_);
      }
      // 結尾
      printTr(sMarkLine_);
    } finally {
      oDb.close();
    }
  }

  public void prepareText() throws Exception
  {
    nWidth_ = getWidth();

    String _sCompany = getCompany();
    String _sTitle   = getTitle();

    oHead_ = getHeader();
    oHeadUnderLine_ = getHeaderUnderLine();



    emisTr _oLine = new emisTr();
    emisTd _oToday = new emisTd("製表日期："+emisUtil.getFullDate("/"),A_LEFT,20);
    emisTd _oCompany = new emisTd(_sCompany,A_CENTER,40);
    oPageNum_ = new emisTd("頁次："+oProvider_.getPageNum(),A_RIGHT,getWidth()-60);

    _oLine.add(_oToday);
    _oLine.add(_oCompany);
    _oLine.add(oPageNum_);
    oTitleLines_.add(_oLine);

    _oLine = new emisTr();
    emisTd _oRptId = new emisTd("[" + getReportId()+"]",A_RIGHT,nWidth_);
    _oLine.add(_oRptId);
    oTitleLines_.add(_oLine);


    _oLine = new emisTr();
    emisTd _oRptTitle = new emisTd(_sTitle,A_CENTER,nWidth_);
    _oLine.add(_oRptTitle);
    oTitleLines_.add(_oLine);

    _oLine = new emisTr();
    String _sUnderLine=  emisChinese.duplicate("=",emisChinese.clen(_sTitle));
    emisTd _oRptTitleUnderLine = new emisTd(_sUnderLine ,A_CENTER,nWidth_);
    _oLine.add(_oRptTitleUnderLine);
    oTitleLines_.add(_oLine);
  }


  public void printTitle()
  {
    // 設定新的頁數
    // 直接設定 oPageNum 這個物件的內容即可
    oPageNum_.setContent( "頁次："+String.valueOf(oProvider_.getPageNum()) );
    printTrList(oTitleLines_);
  }


  public void printHeader()
  {
    printTr(sMarkLine_);
    printTr(oHead_,A_CENTER,nWidth_);
    printTr(oHeadUnderLine_,A_CENTER,nWidth_);
  }

  /**
   * 由 printTable 所觸發
   */
  public void onBeforeEject()
  {
    if( hasMoreData_ )
    {
      printTr(sMarkLine_);
    }
  }
  /**
   * 由 printTable 所觸發
   */
  public void onAfterEject()
  {
    if( hasMoreData_ )
    {
      printTitle();
      printHeader();
    }
  }


  /**
   * 傳回 String 的 ArrayList
   */
  public emisTr getHeader() throws Exception
  {

    emisTr head = new emisTr();
    String _sHead   = oProvider_.getProperty("head");
    String _sSize   = oProvider_.getProperty("size");
    String _sAlign  = oProvider_.getProperty("align");
    String _sDataFld= oProvider_.getProperty("datafld");

    int _nHeaderTotalSize =0;
    if( (_sHead != null) && (_sSize != null) && (_sAlign != null) && (_sDataFld != null) )
    {
      StringTokenizer _stHead = new StringTokenizer( _sHead, ",");
      StringTokenizer _stSize = new StringTokenizer( _sSize, ",");
      StringTokenizer _stAlign= new StringTokenizer( _sAlign,",");
      StringTokenizer _stDataFld= new StringTokenizer( _sDataFld,",");

      int _nTokenSize = _stHead.countTokens();
      if( ( _nTokenSize != _stSize.countTokens()) ||
          ( _nTokenSize != _stAlign.countTokens()) ||
          ( _nTokenSize != _stDataFld.countTokens())
      )
      {
          throw new Exception("參數個數不合");
      }

      while( _stDataFld.hasMoreTokens() )
      {
        oDataFld_.add( _stDataFld.nextToken());
      }

      int _nHeadSize;
      while (_stHead.hasMoreTokens())
      {
        String _sHToken =  _stHead.nextToken();
        String _sSToken =  _stSize.nextToken();
        String _sAToken =  _stAlign.nextToken();

        _nHeadSize = 10;
        try {
          _nHeadSize = Integer.parseInt(_sSToken);
          _nHeaderTotalSize += _nHeadSize;
        } catch (Exception parseError) {
          oProvider_.debug("number parse error:"+parseError);
        }
        emisTd _td = new emisTd(_sHToken,A_CENTER,_nHeadSize);
        head.add(_td);
          _td = new emisTd("",A_LEFT,Integer.parseInt(_sSToken),_nHeadSize);
        oDataLine_.add(_td);
      }
    }

    if( _nHeaderTotalSize <= 0 )
      _nHeaderTotalSize = nWidth_;
    sMarkLine_ = new emisTr(emisChinese.duplicate("=",_nHeaderTotalSize),A_CENTER,nWidth_);

    return head;
  }

  /**
   *    編號  名稱
   *    ----  ----
   *
   *    算出底下的 underline,
   *    除了最後一個是 fullsize
   *    其他的都減一
   */
  private emisTr getHeaderUnderLine()
  {
    emisTr _oheadUnderLine = new emisTr();
    int _nSize = oHead_.size();
    for( int i=0 ; i< _nSize; i++)
    {
      emisTd td = (emisTd) oHead_.get(i);
      int _nHeadSize = td.getSize();
      if( i < (_nSize-1))
        _oheadUnderLine.add( new emisTd(emisChinese.duplicate("-",_nHeadSize-1),A_LEFT,_nHeadSize));
      else
        _oheadUnderLine.add( new emisTd(emisChinese.duplicate("-",_nHeadSize),A_LEFT,_nHeadSize));
    }
    return _oheadUnderLine;
  }

}