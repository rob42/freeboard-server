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
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module:
  - css = (name of a css class)
  - date = none (default) | normal | full
  - force = true (default) | false
--%>

<%
  String moduleCode = request.getParameter("modulecode");
  ModuleDescriptor md = null;

  if (moduleCode != null) {
    md = (ModuleDescriptor) request.getAttribute(moduleCode);
  }

  if (md == null) {
    if (!response.isCommitted()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    return;
  }

  File[] files = md.getModuleFiles(webSite, false);

  if (files != null && files.length > 0) {
    boolean force = Utils.isTrue(md.getAdvancedParam("force", "true"));
    Locale locale = WebUtils.getPageLocale(pageContext);
    ResourceBundle bundle =
        ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);
    Arrays.sort(files, new FileNameComparator());
    DateFormat df = md.getDateFormat(locale, "date");
    Path pagePath = webSite.getRequestedPath(request);
    Path iconPath = webSite.getLink(webSite.getAdminPath().add("filemanager/images"), pagePath);
%>

<table<%= md.getFullCSSAttribute("css") %> align="center" border="0" cellspacing="10" cellpadding="0">
<%
    for (int i = 0; i < files.length; i++) {
      if (!files[i].isDirectory()) {
        WebUtils.updateLastModifiedTime(request, files[i]);
        Path link = force ?
            new Path("servlet/org.meshcms.core.DownloadServlet", webSite.getPath(files[i])) :
            webSite.getPath(files[i]);
%>
 <tr valign="top">
  <td><img src="<%= iconPath.add(FileTypes.getIconFile(files[i].getName())) %>" border="0" alt="<%= FileTypes.getDescription(files[i].getName()) %>" /></td>
  <td><a href="<%= webSite.getLink(link, pagePath) %>"><%= files[i].getName() %></a></td>
  <td align="right"><%= WebUtils.formatFileLength(files[i].length(), locale, bundle) %></td>
  <% if (df != null) { %><td><%= df.format(new Date(files[i].lastModified())) %></td><% } %>
 </tr>
<%
      }
    }
%>
</table>

<%
  }
%>
