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
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  String fullSrc = request.getParameter("fullsrc");
  Path filePath = null;
  String title = null;

  if (Utils.isNullOrEmpty(fullSrc)) {
    PageAssembler pa = new PageAssembler();
    Enumeration names = request.getParameterNames();

    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();
      String value = request.getParameter(name);

      if (name.equals("pagepath")) {
        filePath = new Path(value);
      } else {
        if (name.equals("pagetitle")) {
          value = Utils.encodeHTML(value);
          title = value;
        }

        pa.addProperty(name, value);
      }
    }

    fullSrc = pa.getPage();
    
    if (Utils.isTrue(request.getParameter("tidy"))) {
      String tidySrc = WebUtils.tidyHTML(webSite, fullSrc);
      
      if (tidySrc != null) {
        fullSrc = tidySrc;
      }
    }
  } else {
    filePath = new Path(request.getParameter("pagepath"));
  }

  if (!userInfo.canWrite(webSite, filePath)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }
%>

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="saveTitle" /></title>
</head>

<body>

<%
  long time = 0L;
  File file = webSite.getFile(filePath);

  if (file.exists() && Utils.isTrue(request.getParameter("keepFileDate"))) {
    time = file.lastModified();
  }
  
  if (webSite.saveToFile(userInfo, fullSrc, filePath)) {
    if (time != 0L) {
      file.setLastModified(time + 3000L);
    }
    
    webSite.updateSiteMap(true);
%>
  <p><% if (Utils.isNullOrEmpty(title)) { %>
      <fmt:message key="saveOkNoTitle" />
  <% } else { %>
      <fmt:message key="saveOk"><fmt:param value="<%= title %>" /></fmt:message>
  <% } %></p>

  <p><fmt:message key="saveContinue"><fmt:param value="<%= request.getHeader(\"referer\") %>" /></fmt:message></p>
<%
    if (!webSite.isSystem(filePath) && !filePath.isContainedIn(webSite.getCMSPath()) &&
        FileTypes.isPage(filePath.getLastElement())) {
%>
  <p><fmt:message key="saveView"><fmt:param value="<%= WebUtils.getPathInContext(request, filePath) %>" /></fmt:message></p>
<%
      if (webSite.getSiteMap().getPageInfo(filePath) != null) {
%>
  <script type="text/javascript">location.replace("<%= WebUtils.getPathInContext(request, filePath) %>")</script>
<%
      }
    }
  } else {
%>
  <p><fmt:message key="saveError" /></p>
<%
  }
%>
</body>
</html>
