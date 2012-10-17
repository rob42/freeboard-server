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
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="com.opensymphony.module.sitemesh.*" %>
<%@ page import="com.opensymphony.module.sitemesh.parser.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module:
  - css = (name of a css class)
  - date = none (default) | normal | full
  - sort = newest (default) | alphabetical
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
    boolean sortAZ = "alphabetical".equalsIgnoreCase(md.getAdvancedParam(
            "sort", null));
        Comparator c = sortAZ ? (Comparator) new FileNameComparator()
            : (Comparator) new FileDateComparator();
    Arrays.sort(files, c);
    DateFormat df = md.getDateFormat(WebUtils.getPageLocale(pageContext), "date");
%>

<div<%= md.getFullCSSAttribute("css") %>>
<%
    for (int i = 0; i < files.length; i++) {
      if (FileTypes.isPage(files[i].getName())) {
        WebUtils.updateLastModifiedTime(request, files[i]);
        HTMLPageParser fpp = new HTMLPageParser();

        try {
          Reader reader = new InputStreamReader(new FileInputStream(files[i]),
              Utils.SYSTEM_CHARSET);
          HTMLPage pg = (HTMLPage) fpp.parse(Utils.readAllChars(reader));
          String title = pg.getTitle();
          String body = pg.getBody();
          body = WebUtils.fixLinks(webSite, body, request.getContextPath(),
              webSite.getPath(files[i]), md.getPagePath());
          body = WebUtils.replaceThumbnails(webSite, body, request.getContextPath(),
              md.getPagePath());
%>
 <div class="includeitem">
  <div class="includetitle">
    <%= Utils.isNullOrEmpty(title) ? "&nbsp;" : title %>
  </div>
<%
          if (df != null) {
%>
  <div class="includedate">
    (<%= df.format(new Date(files[i].lastModified())) %>)
  </div>
<%
          }
%>
  <div class="includetext">
    <%= body %>
  </div>
 </div>
<%
          reader.close();
        } catch (Exception ex) {}
      }
    }
%>
</div>

<%
  }
%>
