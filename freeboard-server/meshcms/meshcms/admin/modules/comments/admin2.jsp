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
<%@ page import="org.meshcms.webui.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  if (!userInfo.canDo(UserInfo.CAN_DO_ADMINTASKS)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }

  Path commentsPath = webSite.getModuleDataPath().add("comments");
  Enumeration en = request.getParameterNames();
  
  while (en.hasMoreElements()) {
    String param = (String) en.nextElement();
    String value = request.getParameter(param);
    Path commentPath = commentsPath.add(param);
    
    if ("publish".equals(value)) {
      webSite.rename(userInfo, commentPath, commentPath.getLastElement().replaceAll("mch", "mcc"));
    } else if ("delete".equals(value)) {
      webSite.delete(userInfo, commentPath, false);
    }
  }
%>

<html>
  <head>
    <title><fmt:message key="commentsTitle" /></title>
    <%= webSite.getAdminMetaThemeTag() %>
  </head>
  <body>
    <fmt:message key="commentsDone" />
    <script type="text/javascript">location.replace('admin1.jsp');</script>
  </body>
</html>
