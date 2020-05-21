<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%
//  $Id$
  String sServPort = "";
  int iServPort = request.getServerPort();
  if (iServPort != 80) sServPort = ":" + iServPort;
  String url = request.getScheme() + "://" + request.getServerName() + sServPort + request.getContextPath() + "/h5/bmIndex.html";
  response.sendRedirect(url);
%>