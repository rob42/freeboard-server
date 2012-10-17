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

<%
  if (!userInfo.canDo(org.meshcms.core.UserInfo.CAN_BROWSE_FILES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have enough privileges");
    return;
  }

  Configuration configuration = webSite.getConfiguration();
  String cp = request.getContextPath();
%>

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="homeExport" /></title>
<script type="text/javascript">
  var contextPath = "<%= request.getContextPath() %>";
  var adminPath = "<%= webSite.getAdminPath() %>";
</script>
<script type="text/javascript" src="scripts/jquery/jquery.min.js"></script>
<script type="text/javascript" src="scripts/editor.js"></script>
<script type="text/javascript">
// <![CDATA[
  function showWaiting() {
    document.getElementById("subBtn").disabled = true;
    document.getElementById("waitMsg").style.visibility = "visible";
    document.getElementById("waitImg").style.visibility = "visible";
  }
// ]]>
</script>
</head>

<body>

<div align="right"><%= Help.icon(webSite, webSite.getRequestedPath(request), Help.STATIC_EXPORT, userInfo) %></div>

<form action="staticexport2.jsp" method="post" onsubmit="javascript:showWaiting();">
  <fieldset class="meshcmseditor">
    <legend><fmt:message key="exportParameters" /></legend>

    <div class="meshcmsfieldlabel">
      <label for="exportBaseURL"><fmt:message key="exportBaseURL" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="text" id="exportBaseURL" name="exportBaseURL"
       style="width: 90%;" value="<%= Utils.noNull(configuration.getExportBaseURL()) %>" />
    </div>

    <div class="meshcmsfieldlabel">
      <label for="exportDir"><fmt:message key="exportDir" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="text" id="exportDir" name="exportDir"
       style="width: 90%;" value="<%= Utils.noNull(configuration.getExportDir()) %>" />
    </div>

    <div class="meshcmscheckbox">
      <input type="checkbox" id="exportCheckDates" name="exportCheckDates"
       value="true"<%= configuration.isExportCheckDates() ? " checked='checked'" : "" %> />
      <label for="exportCheckDates"><fmt:message key="exportCheckDates" /></label>
    </div>

    <div class="meshcmsfieldlabel">
      <label for="exportCommand"><fmt:message key="exportCommand" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="text" id="exportCommand" name="exportCommand"
       style="width: 90%;" value="<%= Utils.noNull(configuration.getExportCommand()) %>" />
    </div>
  </fieldset>

  <div class="meshcmscheckbox">
    <input type="checkbox" id="exportSaveConfig" name="exportSaveConfig" value="true" checked="checked" />
    <label for="exportSaveConfig"><fmt:message key="exportSaveConfig" /></label>
  </div>

  <div class="meshcmsbuttons">
    <input type="submit" value="<fmt:message key="exportDo" />" id="subBtn" />
    <div id="waitImg" style="visibility:hidden;">
      <img src="filemanager/images/waiting.gif" vspace="24" alt="<fmt:message key="fmUploadImgAlt" />" />
    </div>
    <div id="waitMsg" style="visibility:hidden;"><fmt:message key="fmUploadWait" /></div>
  </div>
</form>

</body>
</html>
