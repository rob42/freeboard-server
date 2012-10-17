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

<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module:
  - css = (name of a css class)
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

  Path argPath = md.getModuleArgumentDirectoryPath(webSite, false);
  String style = md.getAdvancedParam("css", md.getStyle());
  Path pagePath = webSite.getRequestedPath(request);

  if (argPath == null) {
    argPath = md.getPagePath();
  }

  List list = webSite.getSiteMap().getPagesInDirectory(argPath, false);

  if (list != null && list.size() > 0) {
    PageInfo[] pages = (PageInfo[]) list.toArray(new PageInfo[list.size()]);
    %><ul>
     <li><%= Utils.generateList(webSite.getLinkList(pages, pagePath, null, style), "</li><li>") %></li>
    </ul><%
  }
%>
