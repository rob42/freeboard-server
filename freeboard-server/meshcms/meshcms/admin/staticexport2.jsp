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
<%@ page import="java.net.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.extra.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%
  if (!userInfo.canDo(org.meshcms.core.UserInfo.CAN_BROWSE_FILES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have enough privileges");
    return;
  }
%>

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="homeExport" /></title>
</head>

<body>

<%
  File exportDestination = null;
  String exportBaseURL = Utils.noNull(request.getParameter("exportBaseURL"));
  String exportDir = Utils.noNull(request.getParameter("exportDir"));
  boolean exportCheckDates = Utils.isTrue(request.getParameter("exportCheckDates"));
  String exportCommand = Utils.noNull(request.getParameter("exportCommand"));

  if (!exportDir.equals("")) {
    exportDestination = new File(exportDir);
    exportDestination.mkdirs();
  }

  if (exportDestination == null || !exportDestination.isDirectory()) {
    %><fmt:message key="exportErrorNoDir" /><%
  } else {

  URL contextURL = null;

  try {
    contextURL = new URL(Utils.addAtEnd(exportBaseURL, "/"));
  } catch (Exception ex) {
    contextURL = new URL(Utils.addAtEnd(WebUtils.getContextHomeURL(request).toString(), "/"));
  }
%>

<pre>
<%
    StaticExporter exporter = new StaticExporter(webSite, contextURL, exportDestination);
    exporter.setWriter(out);
    exporter.setCheckDates(exportCheckDates);
    exporter.process();

    if (!exportCommand.equals("")) {
      out.println("\nexecuting: " + exportCommand);
      Process process = Runtime.getRuntime().exec(exportCommand);
      out.println("standard output:");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Utils.copyStream(process.getInputStream(), baos, false);
      out.write(Utils.encodeHTML(baos.toString()));
      baos.reset();
      out.println("end of standard output\nerror output:");
      Utils.copyStream(process.getErrorStream(), baos, false);
      out.write(Utils.encodeHTML(baos.toString()));
      int exit = process.waitFor();
      out.println("end of error output\nexecution finished with exit code " + exit);
    }

    if (Utils.isTrue(request.getParameter("exportSaveConfig"))) {
      Configuration c = webSite.getConfiguration();
      c.setExportBaseURL(exportBaseURL);
      c.setExportDir(exportDir);
      c.setExportCheckDates(exportCheckDates);
      c.setExportCommand(exportCommand);
      c.store(webSite);
    }

    // don't disable custom theme even if there have been errors while exporting
    webSite.setLastAdminThemeBlock(0L);
%>
</pre>

<%
  }
%>

</body>
</html>
