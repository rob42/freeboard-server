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

<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module:
  - css = (name of a css class)
  - step = step for item css classes (default 1 - use 0 to remove classes)
  - max = maximum for item css classes (default no max)
--%>

<%
  Locale locale = WebUtils.getPageLocale(pageContext);
  ResourceBundle pageBundle = ResourceBundle.getBundle
      ("org/meshcms/webui/Locales", locale);

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

  Path argPath = md.getModuleArgumentDirectoryPath(webSite, true);
  SortedMap tags = new TreeMap();

  if (argPath != null) {
    SiteMap siteMap = webSite.getSiteMap();
    ArrayList pagesList = new ArrayList(siteMap.getPagesList(argPath));
    Iterator iter = pagesList.iterator();
    Path pagePathInMenu = siteMap.getPathInMenu(md.getPagePath());
    
    while (iter.hasNext()) {
      PageInfo item = (PageInfo) iter.next();
      
      if (!item.getPath().equals(pagePathInMenu)) {
        String[] pageTags = item.getKeywords();
        
        if (pageTags != null) {
          for (int i = 0; i < pageTags.length; i++) {
            int[] n = (int[]) tags.get(pageTags[i]);
            
            if (n == null) {
              n = new int[1];
              n[0] = 1;
            } else {
              n[0]++;
            }
            
            tags.put(pageTags[i], n);
          }
        }
      }
    }
    
    if (tags.size() > 0) {
      int step = Utils.parseInt(md.getAdvancedParam("step", null), 1);
      int max = Utils.parseInt(md.getAdvancedParam("max", null), Integer.MAX_VALUE);
%>
<ul<%= md.getFullCSSAttribute("css") %>>
<%
      Iterator ti = tags.keySet().iterator();
      
      while (ti.hasNext()) {
        String tag = (String) ti.next();
        int[] n = (int[]) tags.get(tag);
        String style;
        
        if (step == 0) {
          style = "";
        } else {
          style = " class='level" + (Math.min(n[0] / step, max)) + "'";
        }
%>
<li<%= style %>><a href="?tag=<%= Utils.encodeURL(tag) %>"><%= tag %></a>
<%
      }
%>
</ul>
<%
    }
  }
%>
