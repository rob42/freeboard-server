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
  String edituser = Utils.noNull(request.getParameter("username"));
  String username = userInfo.getUsername();

  boolean error = false;
  boolean newUser = false;
  UserInfo edit = null;

  if (username.equals("")) {
    error = true;
  } else if (username.equals(edituser)) {
    edit = userInfo;
  } else if (userInfo.canDo(UserInfo.CAN_ADD_USERS)) {
    if (userInfo.exists(webSite, edituser)) {
      error = true;
    } else {
      newUser = true;
      edit = new UserInfo();
      edit.setUsername(edituser);
      edit.setPreferredLocaleCode(userInfo.getPreferredLocaleCode());
    }
  } else {
    error = true;
  }

  if (error) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }

  String imagesPath = request.getContextPath() + '/' + webSite.getAdminPath() + "/images";
  String authCode = WebUtils.setAuthCode(session, "edituser");
%>

<html>
<head>
  <%= webSite.getAdminMetaThemeTag() %>
  <title><fmt:message key="<%= newUser ? \"userNew\" : \"userEdit\" %>" /></title>

  <script type="text/javascript">
    var contextPath = "<%= request.getContextPath() %>";
    var adminPath = "<%= webSite.getAdminPath() %>";
  </script>
  <script type="text/javascript" src="scripts/jquery/jquery.min.js"></script>
  <script type="text/javascript" src="scripts/editor.js"></script>
</head>

<body>
<div align="right"><%= Help.icon(webSite, webSite.getRequestedPath(request),
    newUser ? Help.NEW_USER : Help.EDIT_PROFILE, userInfo) %></div>

<form action="edituser2.jsp" method="post">
  <input type="hidden" name="authcode" value="<%= authCode %>" />
  <input type="hidden" name="new" value="<%= newUser %>" />

  <fieldset class="meshcmseditor">
    <legend><fmt:message key="<%= newUser ? \"userNew\" : \"userEdit\" %>" /></legend>

    <div class="meshcmsfieldlabel">
      <label for="username"><fmt:message key="loginUsername" /></label>
    </div>

    <div class="meshcmsfield">
      <% if (newUser) { %>
      <input type="text" id="username" name="username"
       style="width: 90%;" />
      <% } else { %>
       <strong><%= edit.getUsername() %></strong>
       <input type="hidden" id="username" name="username" value="<%= edit.getUsername() %>" />
      <% } %>
    </div>

    <div class="meshcmsfieldlabel">
      <label for="permissions"><fmt:message key="userType" /></label>
    </div>

    <div class="meshcmsfield">
      <% if (newUser) { %>
       <select name="permissions" id="permissions">
        <option value="<%= UserInfo.EDITOR %>"><fmt:message key="userEditor" /></option>
        <option value="<%= UserInfo.ADMIN %>"><fmt:message key="userAdmin" /></option>
        <option value="<%= UserInfo.MEMBER %>"><fmt:message key="userMember" /></option>
       </select>
      <% } else {
        int perm = edit.getPermissions();
        String type;
        ResourceBundle bundle = WebUtils.getPageResourceBundle(pageContext);

        if (perm == UserInfo.ADMIN) {
          type = bundle.getString("userAdmin");
        } else if (perm == UserInfo.MEMBER) {
          type = bundle.getString("userMember");
        } else if (perm == UserInfo.EDITOR) {
          type = bundle.getString("userEditor");
        } else if (perm == UserInfo.GUEST) {
          type = bundle.getString("userGuest");
        } else {
          type = bundle.getString("userCustom");
        }
        %><strong><%= type %></strong>
        <input type="hidden" name="permissions" id="permissions" value="<%= perm %>" />
      <% } %>
    </div>

    <div class="meshcmsfieldlabel">
      <label for="homepath"><fmt:message key="userHome" /></label>
    </div>

    <div class="meshcmsfield">
      <% String hPath = "/" + edit.getHomePath();

         if (newUser) { %>
      <input type="text" name="homepath" id="homepath"
       value="<%= hPath %>" style="width: 90%;" />
      <% } else { %>
       <strong><%= hPath %></strong>
       <input type="hidden" name="homepath" id="homepath" value="<%= hPath %>" />
      <% } %>
    </div>

    <div class="meshcmsfieldlabel">
      <label for="email"><fmt:message key="userMail" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="text" name="email" id="email"
       value="<%= edit.getEmail() %>" style="width: 90%;" />
    </div>

    <div class="meshcmsfieldlabel">
      <label for="language"><fmt:message key="userLanguage" /></label>
    </div>

    <div class="meshcmsfield">
      <select name="language" id="language"><% Locale[] locales = Locale.getAvailableLocales();
       Arrays.sort(locales, new LocaleComparator(Locale.ENGLISH));

       for (int i = 0; i < locales.length; i++) { %>
         <option value="<%= locales[i] %>"<%= locales[i].toString().equals(edit.getPreferredLocaleCode()) ?
           " selected='selected'" : "" %>><%= locales[i].getDisplayName(Locale.ENGLISH) %></option><%
       } %></select>
    </div>
  </fieldset>

  <fieldset class="meshcmseditor">
    <legend><fmt:message key="<%= newUser ? \"userInitPwd\" : \"userChangePwd\" %>" /></legend>

    <div class="meshcmsfieldlabel">
      <label for="password1"><fmt:message key="loginPassword" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="password" name="password1" id="password1"
       style="width: 90%;" />
    </div>

    <div class="meshcmsfieldlabel">
      <label for="password2"><fmt:message key="userConfirmPwd" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="password" name="password2" id="password2"
       style="width: 90%;" />
    </div>
  </fieldset>

  <div class="meshcmsbuttons">
    <input type="submit" value="<fmt:message key="genericSave" />" />
  </div>

  <fieldset class="meshcmseditor">
    <legend><fmt:message key="userOpt" /></legend>

<%
  for (int i = 0; i < UserInfo.DETAILS.length; i++) {
%>
    <div class="meshcmsfieldlabel">
      <label for="<%= UserInfo.DETAILS[i] %>"><fmt:message key="<%= \"user_\" + UserInfo.DETAILS[i] %>" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="text" name="<%= UserInfo.DETAILS[i] %>" id="<%= UserInfo.DETAILS[i] %>"
       value="<%= edit.getValue(UserInfo.DETAILS[i]) %>" style="width: 90%;" />
    </div>
<%
  }
%>
  </fieldset>
</form>

</body>
</html>
