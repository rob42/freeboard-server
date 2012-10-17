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

<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="loginTitle" /></title>
</head>

<body>

<%
  String username = Utils.encodeHTML(request.getParameter("username"));
  String password = Utils.encodeHTML(request.getParameter("password"));
  boolean loaded = false;

  if (!username.equals("") && !password.equals("")) {
    loaded = userInfo.load(webSite, username, password);

    if (!loaded) {
      %><ul><li><fmt:message key="loginError" /></li></ul><%
    }
  }

  if (loaded) {
%>
    <fmt:message key="loginOk" />
    <script type="text/javascript">location.replace('index.jsp');</script>
<%
  } else {
    if (!request.isRequestedSessionIdFromCookie()) {
%>
      <p><fmt:message key="loginCookies" /></p>
<%
    }
%>
    <form name="loginform" action="login.jsp" method="post">
     <div class="meshcmsfieldlabel">
      <label for="username">
       <fmt:message key="loginUsername" />
      </label>
     </div>

     <div class="meshcmsfield">
      <input type="text" id="username" name="username" value="<%= username %>" />
     </div>

     <div class="meshcmsfieldlabel">
      <label for="password">
       <fmt:message key="loginPassword" />
      </label>
     </div>

     <div class="meshcmsfield">
      <input type="password" id="password" name="password" />
     </div>

     <div class="meshcmsfield">
      <input type="submit" value="<fmt:message key="loginSubmit" />" />
     </div>
    </form>
<%
  }
%>

</body>
</html>
