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
  SortedSet dates = new TreeSet(new ReverseComparator());

  if (argPath != null) {
    SiteMap siteMap = webSite.getSiteMap();
    ArrayList pagesList = new ArrayList(siteMap.getPagesList(argPath));
    Iterator iter = pagesList.iterator();
    Path pagePathInMenu = siteMap.getPathInMenu(md.getPagePath());
    
    while (iter.hasNext()) {
      PageInfo item = (PageInfo) iter.next();
      
      if (!item.getPath().equals(pagePathInMenu)) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(item.getLastModified());
        dates.add(Integer.toString(cal.get(Calendar.YEAR) * 100 +
            cal.get(Calendar.MONTH) + 1));
      }
    }
    
    if (dates.size() > 0) {
      SimpleDateFormat insdf = new SimpleDateFormat("yyyyMM");
      SimpleDateFormat outsdf = new SimpleDateFormat("MMMM yyyy", locale);
      Iterator di = dates.iterator();
%>
<form action="" method="get">
<select name="date">
<option value=""><%= pageBundle.getString("fmSelAll") %></option>
<%
      while (di.hasNext()) {
        String sd = (String) di.next();
        Date date = insdf.parse(sd);
%>
<option value="<%= sd %>"><%= outsdf.format(date) %></option>
<%
      }
%>
</select>
<input type="submit" value="<%= pageBundle.getString("genericSelect") %>" />
</form>
<%
    }
  }
%>
