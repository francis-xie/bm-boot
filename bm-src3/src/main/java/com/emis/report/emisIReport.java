/* $Id: emisIReport.java 4 2015-05-27 08:13:47Z andy.he $
 *
 * Copyright (c) 2005 EMIS Corp. All Rights Reserved.
 */
package com.emis.report;

import com.emis.db.emisConnectProxy;
import com.emis.db.emisDbConnector;
import com.emis.db.emisDbMgr;
import com.emis.qa.emisServletContext;
import com.emis.server.emisServerFactory;
import com.emis.util.emisUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassDesc.
 *
 * @author Jerry 2005/4/1 下午 03:47:55
 * @version 1.0
 * @version 1.1 2005/12/30 增加RTF格式
 * @version 1.2 2006/01/18 增加HTML,CSV,TEXT等格式
 * Track+[15515] dana.gao 2010/08/20 增加列印ireport報表相關程式
 * modify by tommer.xie 2011/03/25 將ireport版本升至3.7.1版
 */
public class emisIReport {
  private ServletContext oContext_;
  private Map mapParameters_;
  private String sJasperFile_;
  private String sOutputFile_;
  private String sReportType_;

  public byte[] generate() throws Exception {
    return generate(oContext_, mapParameters_, sJasperFile_, sOutputFile_, sReportType_);
  }

  public byte[] generate(ServletContext application, Map parameters,
      String sJasperFile, String sOutputFile, String sRptType) throws Exception {
    oContext_ = application;
    mapParameters_ = parameters;
    sJasperFile_ = sJasperFile;
    sOutputFile_ = sOutputFile;
    sReportType_ = sRptType;
    byte[] output = null;
    Connection conn = null;
    emisConnectProxy _oProxy = null;
    File reportFile = new File(application.getRealPath(sJasperFile));
    //System.out.println("fn="+reportFile.getPath());
    emisDbMgr _oMgr = null;
    emisDbConnector _oConnector = null;
    ByteArrayOutputStream baos = null;
    try {
      //1.載入Driver類別
      _oMgr = emisDbMgr.getInstance(application);
      _oConnector = _oMgr.getConnector(); // default connector
      _oProxy = _oConnector.getConnection();
      conn = _oProxy.getConnection();
      //out.println("conn="+conn);
      //Class.forName("com.inet.tds.TdsDriver");

      //2.透過DriverManager取得連線
      //conn = DriverManager.getConnection("jdbc:inetdae7:localhost?charset=MS950","sa","turbo");
      //conn.setCatalog("EMIS_MIS");

      //byte[] bytes = JasperRunManager.runReportToPdf(reportFile.getPath(),map,conn);
      //JasperExportManager.ex
      //JasperViewer.viewReport(bytes);

      JasperPrint jasperPrint;

      // Fill the report and produce a print object
      try {
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportFile);
        jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
      } catch (JRException e) {
        System.out.println("loadObject: " + e.getMessage());
        throw new ServletException(e.getMessage(), e);
      }

      //response.setContentType("application/vnd.ms-excel");
      JRAbstractExporter exporter = null;
      if ("XLS".equalsIgnoreCase(sRptType)) {
        exporter = new JRXlsExporter();
      } else if ("RTF".equalsIgnoreCase(sRptType)) {
        exporter = new JRRtfExporter();
      } else if ("CSV".equalsIgnoreCase(sRptType)) {
        exporter = new JRCsvExporter();
        exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING,emisUtil.FILENCODING);
      } else if ("TXT".equalsIgnoreCase(sRptType)) {
        exporter = new JRTextExporter();
        exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING,emisUtil.FILENCODING);
        //exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, new Integer(132));
        exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, new Integer(3));
        exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, new Integer(10));
        exporter.setParameter(JRTextExporterParameter.BETWEEN_PAGES_TEXT, "\u000C");
      }else if ("DOC".equalsIgnoreCase(sRptType) || "DOCX".equalsIgnoreCase(sRptType)) {
        exporter = new JRDocxExporter();
      }else if ("HTML".equalsIgnoreCase(sRptType)) {
        exporter = new JRHtmlExporter();
      } else if ("XML".equalsIgnoreCase(sRptType)) {
        exporter = new JRXmlExporter();
      } else {
        exporter = new JRPdfExporter();
      }
      baos = new ByteArrayOutputStream();

      exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
      exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, sOutputFile);
      //exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
      exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
      exporter.exportReport();

      output = baos.toByteArray();
    } catch (Exception e) {
      e.printStackTrace(System.err);
      String _sMsg = "emisIReport:" + e.getMessage();
      System.err.println(_sMsg);
      throw new ServletException(e.getMessage(), e);
    } finally {
      conn.close();
      baos.close();
      _oProxy.close();
      _oMgr = null;
      conn = null;
    }
    return output;
  }

  public void setContext(ServletContext oContext) {
    oContext_ = oContext;
  }

  public void setParameters(Map mapParameters) {
    mapParameters_ = mapParameters;
  }

  public void setJasperFile(String sJasperFile) {
    sJasperFile_ = sJasperFile;
  }

  public void setOutputFile(String sOutputFile) {
    sOutputFile_ = sOutputFile;
  }

  public void setReportType(String sReportType) {
    sReportType_ = sReportType;
  }

  public static void main(String[] args) throws Exception {
    emisServletContext _oContext = new emisServletContext();
    emisServerFactory.createServer(_oContext,"d:\\wwwroot\\smepos","d:\\resin\\resin3\\epos.cfg", true);
    Map map = new HashMap();
    map.put("REP_SEQ", "5");
    map.put("AREA", "TP");
    map.put("ADDR", "台北市");
    map.put("ZIP", "106");
    map.put("TEL", "27050488");
    map.put("EXT", "106");
    map.put("FAX", "27050490");
    map.put("QT_DATE", "2005/04/02");
    map.put("MTN_PERIOD", "內");
    map.put("ATTACH", "電源供應器");
    map.put("SPARE_PART", "提供");
    map.put("AMT_NOTAX", "150");
    map.put("AMT_TAX", ""+"15");
    map.put("AMT_ALL", "165");

    emisIReport _report = new emisIReport();
    String _sFilename = _oContext.getRealPath("/report_out/test.xls");
    String _sJasperFile = _oContext.getRealPath("/report_def/repair_quotation1.jasper");
    System.out.println("fn=" + _sFilename);
    byte[] output = _report.generate(_oContext, map, _sJasperFile, _sFilename, "XLS");
  }
}
