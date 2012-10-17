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
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  if (!userInfo.canDo(UserInfo.CAN_EDIT_PAGES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }

  SiteInfo siteInfo = webSite.getSiteInfo();
  Enumeration enumeration = request.getParameterNames();

  while (enumeration.hasMoreElements()) {
    String pName = (String) enumeration.nextElement();
    siteInfo.setValue(pName, request.getParameter(pName));
  }

  webSite.updateSiteMap(true);
%>

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="mapTitle" /></title>
</head>

<body>

<%
  if (siteInfo.store()) {
%>
    <fmt:message key="mapOk" />
    <script type="text/javascript">location.replace('index.jsp');</script>
<%
  } else {
%>
    <fmt:message key="mapError" />
    <a href="editmap1.jsp"><fmt:message key="genericRetry" /></a>.
<%
  }
%>
</body>
</html>
