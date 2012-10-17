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
<%@ page import="org.meshcms.webui.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  String cp = request.getContextPath();
%>

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="homeTitle" /></title>
<%-- <script type="text/javascript" src="scripts/jquery/jquery.min.js">
</script>
<script type="text/javascript">
  $(function() {
    var maxW = 0;
    var btns = $("a.meshcmspanelicon");

    btns.each(function() {
      maxW = Math.max($(this).width(), maxW);
    });

    maxW += 4;

    btns.each(function() {
      $(this).width(maxW);
    });
  });
</script> --%>
</head>

<body>

<div align="right"><%= Help.icon(webSite, webSite.getRequestedPath(request), Help.CONTROL_PANEL, userInfo) %></div>

<%
  if (userInfo.isGlobal()) {
%>
  <p style="text-align: center;"><fmt:message key="homeGlobal" /></p>
<%
  }
%>

<fieldset>
  <legend><fmt:message key="homeSite" /></legend>

  <a href="<%= cp %>/" class="meshcmspanelicon"><img src="filemanager/images/house.png" alt="" />
  <fmt:message key="homePage" /></a>

   <% if (userInfo.canDo(UserInfo.CAN_BROWSE_FILES)) { %>
  <a href="refresh.jsp" class="meshcmspanelicon"><img src="filemanager/images/arrow_refresh.png" alt="" />
  <fmt:message key="homeRefresh" /></a>
   <% } %>

   <% if (userInfo.canDo(UserInfo.CAN_EDIT_PAGES)) { %>
  <a href="editmap1.jsp" class="meshcmspanelicon"><img src="filemanager/images/table_multiple.png" alt="" />
  <fmt:message key="homePages" /></a>
   <% } %>

   <% if (userInfo.canDo(UserInfo.CAN_DO_ADMINTASKS)) { %>
  <a href="editconfig1.jsp" class="meshcmspanelicon"><img src="filemanager/images/wrench_orange.png" alt="" />
  <fmt:message key="homeConfigure" /></a>
   <% } %>
</fieldset>

<fieldset>
  <legend><fmt:message key="homeUsers" /></legend>

   <% if (userInfo.isGuest()) { %>
  <a href="login.jsp" class="meshcmspanelicon"><img src="filemanager/images/door_in.png" alt="" />
  <fmt:message key="homeLogin" /></a>
   <% } else { %>
  <a href="logout.jsp" class="meshcmspanelicon"><img src="filemanager/images/door_out.png" alt="" />
  <fmt:message key="homeLogout" /></a>
   <% } %>

   <% if (!userInfo.isGuest()) { %>
  <a href="edituser1.jsp?username=<%= userInfo.getUsername() %>" class="meshcmspanelicon"><img src="filemanager/images/user_edit.png" alt="" />
  <fmt:message key="homeProfile" /></a>
   <% } %>

   <% if (userInfo.canDo(UserInfo.CAN_ADD_USERS)) { %>
  <a href="edituser1.jsp" class="meshcmspanelicon"><img src="filemanager/images/user_add.png" alt="" />
  <fmt:message key="homeUser" /></a>
   <% } %>
</fieldset>

 <% if (!userInfo.isGuest()) { %>
<fieldset>
  <legend><fmt:message key="homeSystem" /></legend>

   <% if (userInfo.canDo(UserInfo.CAN_BROWSE_FILES)) { %>
  <a href="filemanager/index.jsp" class="meshcmspanelicon"><img src="filemanager/images/folder_magnify.png" alt="" />
  <fmt:message key="homeFile" /></a>
   <% } %>

   <% if (userInfo.canDo(UserInfo.CAN_BROWSE_FILES)) { %>
  <a href="staticexport1.jsp" class="meshcmspanelicon"><img src="filemanager/images/world_go.png" alt="" />
  <fmt:message key="homeExport" /></a>
   <% } %>

   <% if (userInfo.canDo(UserInfo.CAN_BROWSE_FILES)) { %>
  <a href="syncsite1.jsp" class="meshcmspanelicon"><img src="filemanager/images/arrow_merge.png" alt="" />
  <fmt:message key="homeSync" /></a>
   <% } %>

   <% if (!webSite.isVirtual() && userInfo.canDo(UserInfo.CAN_DO_ADMINTASKS)) { %>
  <a href="editsites1.jsp" class="meshcmspanelicon"><img src="filemanager/images/world_edit.png" alt="" />
  <fmt:message key="homeSites" /></a>
   <% } %>
</fieldset>
 <% } %>

</body>
</html>
