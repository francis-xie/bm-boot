package com.emis.report;

public interface emisRptNode
{
    public static final int EMIS_RPT_NODE_TABLE = 0;
    public static final int EMIS_RPT_NODE_THEAD = 1;
    public static final int EMIS_RPT_NODE_TBODY = 2;
    public static final int EMIS_RPT_NODE_TFOOTER = 3;
    public static final int EMIS_RPT_NODE_TR = 4;
    public static final int EMIS_RPT_NODE_TD = 5;
    public static final int EMIS_RPT_NODE_SUBTOTAL = 6;
    public static final int EMIS_RPT_NODE_TOTAL = 7;
    public static final int EMIS_RPT_NODE_ACCUMTOTAL = 8;
    public static final int EMIS_RPT_NODE_SHEET = 9;
    public static final int EMIS_RPT_NODE_SHEETPATTERN = 10;


    public static final String [] rpt_node_list  =
    {
      "table",
      "thead",
      "tbody",
      "tfooter",
      "tr",
      "td",
      "subtotal",
      "total",
      "accumtotal",
      "sheet",
      "sheetpattern",
    };

    String getNodeId();
    int getNodeType();
    String getNodeTypeStr();

    void addNode(emisRptNode node);
    /*
    int size();
    emisRptNode getNode(int nIdx);
    */

}