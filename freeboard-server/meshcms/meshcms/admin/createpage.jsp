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
  if (!userInfo.canDo(UserInfo.CAN_EDIT_PAGES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }

  String cp = request.getContextPath();
%>

<html>
<head>
<title><fmt:message key="newpage" /></title>
<%
  boolean popup = Utils.isTrue(request.getParameter("popup"));

  if (popup) {
    out.write(webSite.getDummyMetaThemeTag());
    out.write("\n<link href='theme/main.css' type='text/css' rel='stylesheet' />");
    out.write("\n<link href='theme/meshcms.css' type='text/css' rel='stylesheet' />");
  } else {
    out.write(webSite.getAdminMetaThemeTag());
  }
%>
<script type="text/javascript">
  var contextPath = "<%= request.getContextPath() %>";
  var adminPath = "<%= webSite.getAdminPath() %>";
</script>
<script type="text/javascript" src="scripts/jquery/jquery.min.js"></script>
<script type="text/javascript" src="scripts/editor.js"></script>
</head>

<body>

<%
String title = Utils.noNull(request.getParameter("title"));
Path fullPath = new Path(request.getParameter("fullpath"));
Path path = new Path(request.getParameter("path"));

if (title.equals("") && fullPath.isRoot()) { %>
  <p align="right"><%= Help.icon(webSite, webSite.getRequestedPath(request), Help.NEW_PAGE, userInfo) %></p>

  <form action='createpage.jsp' method='post' id='createpage' name='createpage'>
    <input type="hidden" name="popup" value="<%= popup %>" />
    <input type='hidden' name='path' value='<%= path %>' />

    <fieldset class="meshcmseditor">
      <legend><fmt:message key="newpageBoxTitle" /></legend>
      <div class="meshcmsfieldlabel">
        <label for="titlefld"><fmt:message key="newpageTitle" /></label>
      </div>

      <div class="meshcmsfield">
        <input type='text' name='title' id='titlefld' style="width: 90%;" />
      </div>

      <%--
      <select name="fileext">
          <%
            String[] exts = webSite.getConfiguration().getVisualExtensions();
            for (int i = 0; i < exts.length; i++) {
          %>
            <option value="<%= exts[i] %>"><%= exts[i] %></option>
          <%
            }
          %>
      </select>
      --%>

      <div class="meshcmscheckbox">
        <input type='checkbox' name='newdir' checked='checked' value='true' id='newdirch' />
        <label for="newdirch"><fmt:message key="newpageFolder" /></label>
      </div>

      <div class="meshcmsbuttons">
        <input type='submit' value='<fmt:message key="newpageCreate" />' />
        <input type='button' value='<fmt:message key="genericCancel" />'
         onclick='javascript:<%= popup ? "window.close" : "history.back" %>();' />
      </div>
    </fieldset>
  </form>
<% } else {
  boolean newDir = false;
  String fileName = null;

  if (fullPath.isRoot()) {
    newDir = Utils.isTrue(request.getParameter("newdir"));
    fileName = WebUtils.fixFileName(title, false).toLowerCase();

    if (!newDir) {
      fileName += '.' + webSite.getConfiguration().getVisualExtensions()[0];
    }

    fileName = Utils.generateUniqueName(fileName, webSite.getFile(path));
  } else {
    path = fullPath.getParent();
    fileName = fullPath.getLastElement();
    title = "";
  }

  if (fileName == null) {
    %><fmt:message key="newpageError" /><br /><%
  } else {
    path = path.add(fileName);

    if (newDir) {
      path = path.add(webSite.getWelcomeFileNames()[0]);
    }

    title = Utils.encodeHTML(title);
    String text = webSite.getHTMLTemplate(title);

    if (webSite.saveToFile(userInfo, text, path)) {
      webSite.updateSiteMap(true);
      %><script type="text/javascript">
      // <![CDATA[
        var page = "<%= cp + '/' + path + '?' + HitFilter.ACTION_NAME + '=' + HitFilter.ACTION_EDIT %>";

        if (window.name && window.name == "smallpopup") { // it's a popup
          window.opener.location.href = page;
          window.close();
        } else {
          window.location.href = page;
        }
      // ]]>
      </script><%
    } else {
      %><fmt:message key="newpageError" /><%
    }
  }
%><p><a href="javascript:history.back();"><fmt:message key="genericBack" /></a></p><%
} %>

</body>
</html>
