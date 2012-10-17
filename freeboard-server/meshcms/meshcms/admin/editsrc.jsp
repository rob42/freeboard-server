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
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  String cp = request.getContextPath();
  Path pagePath = new Path(Utils.decodeURL(request.getParameter("path")));

  if (!userInfo.canWrite(webSite, pagePath)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }

  File file = webSite.getFile(pagePath);
  String full = Utils.readFully(file);
  // session.setAttribute("MeshCMSNowEditing", pagePath); // no longer needed

  String codeSyntax = "";
  String fileName = pagePath.getLastElement();

  if (FileTypes.isLike(fileName, "html")) {
    codeSyntax = "html";
  } else if (FileTypes.isLike(fileName, "php")) {
    codeSyntax = "php";
  } else if (FileTypes.isLike(fileName, "css")) {
    codeSyntax = "css";
  } else if (FileTypes.isLike(fileName, "js")) {
    codeSyntax = "js";
  } else if (FileTypes.isLike(fileName, "xml")) {
    codeSyntax = "xml";
  }

  String clean = null;

  if (Utils.isTrue(request.getParameter("tidy"))) {
    clean = WebUtils.tidyHTML(webSite, full);
  }
%>

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="srcTitle" /></title>
<script type="text/javascript" src="scripts/jquery/jquery.min.js"></script>
<script type="text/javascript" src="scripts/edit_area/edit_area_full.js"></script>
<script type="text/javascript">
// <![CDATA[
  editAreaLoader.init({
    id : "fullsrc",
    display : "later",
    syntax: "<%= codeSyntax %>",
    language: "<fmt:message key="editAreaLang" />",
    start_highlight: true,
    font_size: 9
  });

  $(function() {
    $("#previewButton").click(function() {
      var action = $("#srceditor").attr("action");
      $("#fullsrc").val(editAreaLoader.getValue("fullsrc"));
      $("#srceditor").attr("action", "echo.jsp").attr("target", "_blank")[0].submit();
      $("#srceditor").attr("target", "").attr("action", action);
    });
  });
// ]]>
</script>
</head>

<body>

<form action="savepage.jsp" method="post" id="srceditor" name="srceditor">
  <input type="hidden" name="pagepath" value="<%= pagePath %>" />

  <fieldset class="meshcmseditor">
    <%
      if (clean != null) {
        full = clean;
    %>
      <p><fmt:message key="editTidyApplied" /></p>
    <%
      }
    %>

    <legend>
      <fmt:message key="srcEditing" />
      <a href="<%= cp + '/' + pagePath %>"><%= pagePath.getLastElement() %></a>
    </legend>

    <div class="meshcmsfield">
      <textarea style="width: 100%; height: 25em;" id="fullsrc" name="fullsrc"
       rows="25" cols="80"><%= Utils.encodeHTML(full, true) %></textarea>
    </div>

    <div class="meshcmsfield">
      <input type="checkbox" id="keepFileDate" name="keepFileDate" value="true" />
      <label for="keepFileDate"><fmt:message key="editorKeepFileDate" /></label>
    </div>

    <div class="meshcmsbuttons">
      <input type="submit" value="<fmt:message key="genericSave" />" />
      <% if (FileTypes.isPage(pagePath.getLastElement())) { %>
        <input type="button" id="previewButton" value="<fmt:message key="genericPreview" />" />
      <% } %>
    </div>
  </fieldset>
</form>

</body>
</html>
