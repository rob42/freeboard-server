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
  ResourceBundle bundle = WebUtils.getPageResourceBundle(pageContext);
  String edituser = Utils.noNull(request.getParameter("username"));
  String username = userInfo.getUsername();

  List errMsgs = new ArrayList();
  boolean error = false;
  boolean newUser = Utils.isTrue(request.getParameter("new"));
  UserInfo edit = null;

  if (username.equals("")) {
    error = true;
  } else if (!newUser && username.equals(edituser)) {
    edit = userInfo;
  } else if (edituser.equals("")) {
    errMsgs.add(bundle.getString("userNoUsername"));
  } else if (userInfo.canDo(UserInfo.CAN_ADD_USERS)) {
    edit = new UserInfo();
    edit.setUsername(edituser);

    if (userInfo.exists(webSite, edituser)) {
      errMsgs.add(bundle.getString("userExists"));
    }
  } else {
    error = true;
  }

  if (!error) {
    String authCode = WebUtils.getAuthCode(session, "edituser");

    if (!authCode.equals(request.getParameter("authcode"))) {
      error = true;
    }
  }

  if (error) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }
  
  if (edit != null) {
    try {
      edit.setPermissions(Integer.parseInt(request.getParameter("permissions")));
    } catch (Exception ex) {
      errMsgs.add(bundle.getString("userWrongPerm"));
    }

    String p1 = request.getParameter("password1").trim();
    String p2 = request.getParameter("password2").trim();

    if (p1.equals("") && p2.equals("")) {
      if (newUser) {
        errMsgs.add(bundle.getString("userNoPwd"));
      }
    } else {
      if (p1.equals(p2)) {
        edit.setPassword(p1);
      } else {
        errMsgs.add(bundle.getString("userNoPwdMatch"));
      }
    }

    String email = request.getParameter("email");

    if (!Utils.isNullOrEmpty(email)) {
      email = email.trim();

      if (Utils.checkAddress(email)) {
        edit.setEmail(email);
      } else {
        errMsgs.add(bundle.getString("userWrongMail"));
      }
    }

    if (newUser) {
      Path hp = new Path(request.getParameter("homepath"));
      File hf = webSite.getFile(hp);
      hf.mkdirs();

      if (hf.isDirectory()) {
        edit.setHomePath(hp);
      } else {
        errMsgs.add(bundle.getString("userWrongHome"));
      }
    }

    edit.setPreferredLocaleCode(request.getParameter("language"));

    Enumeration ps = request.getParameterNames();

    while(ps.hasMoreElements()) {
      String n = ps.nextElement().toString();
      edit.setDetail(n, request.getParameter(n));
    }
  }

  if (errMsgs.size() == 0) {
    if (!edit.store(webSite)) {
      errMsgs.add(bundle.getString("userFileError"));
    }
  }
%>

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
  <title><fmt:message key="<%= newUser ? \"userNew\" : \"userEdit\" %>" /></title>
</head>

<body>

<%
  if (errMsgs.size() == 0) {
%>
    <fmt:message key="userOk" >
      <fmt:param value="<%= edituser %>" />
    </fmt:message>
    <script type="text/javascript">location.replace('index.jsp');</script>
<%
  } else {
%>
    Some errors have occurred:
    <ul>
<%
    for (int i = 0; i < errMsgs.size(); i++) {
%>
      <li><%= errMsgs.get(i) %></li>
<%
    }
%>
    </ul>
    <a href="javascript:history.back();">Try again</a>
<%
  }
%>
</body>
</html>
