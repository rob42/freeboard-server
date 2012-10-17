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

<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  if (!userInfo.canDo(UserInfo.CAN_MANAGE_FILES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }
%>

<html>
<head>
 <%= webSite.getDummyMetaThemeTag() %>
 <title><fmt:message key="deletepage" /></title>
 <link href="theme/main.css" type="text/css" rel="stylesheet" />
</head>

<body>

<p>&nbsp;</p>

<p align='center'>
<% Path path = webSite.getSiteMap().getServedPath(new Path(request.getParameter("path")));
boolean deleted = webSite.delete(userInfo, path, false);

if (deleted) {
  webSite.delete(userInfo, path.getParent(), false);
  webSite.updateSiteMap(true);
  %><script type='text/javascript'>window.opener.location.reload(true);window.close();</script><%
} else {
  %><fmt:message key="deletepageError" /><%
} %>
</p>

<p align='center'><a href="javascript:window.close();"><fmt:message key="genericClose" /></a></p>

</body>
</html>
