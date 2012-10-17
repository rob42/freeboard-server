<%--
 Copyright 2004-2009 Luciano Vernaschi
 
 This file is part of MeshCMS.
 
 MeshCMS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 MeshCMS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with MeshCMS.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="java.io.*" %>
<%@ page import="java.util.zip.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="org.meshcms.webui.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  if (!userInfo.canDo(UserInfo.CAN_BROWSE_FILES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }
%>

<html>
<head>
 <%= webSite.getDummyMetaThemeTag() %>
 <title><fmt:message key="fmUnzipTitle" /></title>
 <link href="filemanager.css" type="text/css" rel="stylesheet" />

 <style type="text/css">
  td { white-space: nowrap; }
  body { margin: 0px; overflow: scroll; }
 </style>
</head>

<body>

<%
  Path zipPath = new Path(request.getParameter("zip"));
%>


<table width="100%" border="0" cellspacing="0" cellpadding="3">
 <tr>
  <th align="left"><fmt:message key="fmUnzipList">
    <fmt:param value="<%= zipPath.getLastElement() %>" />
  </fmt:message></th>
  <th align="right"><%= Help.icon(webSite, webSite.getRequestedPath(request),
      Help.UNZIP, userInfo) %></th>
 </tr>

<%
  File zipFile = webSite.getFile(zipPath);
  InputStream in = new BufferedInputStream(new FileInputStream(zipFile));
  ZipInputStream zin = new ZipInputStream(in);
  ZipEntry e;
  int count = 0;

  while ((e = zin.getNextEntry()) != null) {
    count++;
    String name = e.getName();
    String icon;

    if (e.isDirectory()) {
      icon = FileTypes.DIR_ICON;
    } else {
      icon = FileTypes.getIconFile(name);
    }
%>
 <tr>
  <td colspan="2"><img class="icon" alt="" src="images/<%= icon %>"> <%= name %></td>
 </tr>
<%
  }

  zin.close();

  if (count == 0) {
%>
 <tr>
  <td colspan="2"><fmt:message key="fmUnzipNoFiles" /></td>
 </tr>
<%
  }
%>

</table>

<form name="where" action="unzip2.jsp">
<table width="100%" border="0" cellspacing="0" cellpadding="2">
 <tr>
  <th align="left">
   <input type="checkbox" name="createdir" value="true" style="border: none;"
    onclick="javascript:document.forms['where'].dirname.disabled=!this.checked;" />
   <fmt:message key="fmUnzipNewDir" />
   <input type="text" name="dirname" disabled="true"
    value="<%= Utils.removeExtension(zipPath.getLastElement()) %>" />
   <input type="hidden" name="zippath" value="<%= zipPath %>" />
   <input type="button" value="<fmt:message key="fmUnzipButton" />" <%= count == 0 ? "disabled=\"true\"" : ""%>
    onclick="javascript:window.parent.fm_doUnzip(document.forms['where']);" />
   <input type="button" value="<fmt:message key="genericCancel" />" onclick="javascript:history.back();" />
  </th>
 </tr>
</table>
</form>

<p><fmt:message key="fmUnzipWarn" /></p>

</body>
</html>
