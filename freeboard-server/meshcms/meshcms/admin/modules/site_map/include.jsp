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
  - allowHiding = false (default) | true (honour "hide submenu" setting)
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

  String cp = request.getContextPath();
  Path argPath = md.getModuleArgumentDirectoryPath(webSite, true);

  if (argPath != null) {
    SiteMap siteMap = webSite.getSiteMap();
    SiteInfo siteInfo = webSite.getSiteInfo();
    int lastLevel = argPath.getElementCount() - 1;
    Path pagePath = webSite.getRequestedPath(request);

    if (siteMap.getPageInfo(argPath) != null) {
      boolean allowHiding = Utils.isTrue(md.getAdvancedParam("allowHiding", "false"));
      SiteMapIterator iter = new SiteMapIterator(webSite, argPath);
      iter.setSkipHiddenSubPages(allowHiding);
      PageInfo pageInfo;

      while ((pageInfo = iter.getNextPage()) != null) {
        int level = pageInfo.getLevel();

        for (int i = lastLevel; i < level; i++) {
          %><ul><%
        }

        for (int i = level; i < lastLevel; i++) {
          %></ul><%
        }
        
        %>
            <li><a href="<%= webSite.getLink(pageInfo, pagePath) %>"><%=
              siteInfo.getPageTitle(pageInfo) %></a></li>
        <%
        lastLevel = level;
      }

      for (int i = argPath.getElementCount() - 1; i < lastLevel; i++) {
        %></ul><%
      }
    }
  }
%>
