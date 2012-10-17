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

<%
  if (!userInfo.canDo(org.meshcms.core.UserInfo.CAN_BROWSE_FILES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have enough privileges");
    return;
  }
%>

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="sitesTitle" /></title>
<script type="text/javascript">
// <![CDATA[
 var contextPath = "<%= request.getContextPath() %>";
 var adminPath = "<%= webSite.getAdminPath() %>";
// ]]>
</script>
<script type="text/javascript" src="scripts/jquery/jquery.min.js"></script>
<script type="text/javascript" src="scripts/editor.js"></script>
</head>

<body>

<div align="right"><%= Help.icon(webSite, webSite.getRequestedPath(request),
    Help.SITE_MANAGER, userInfo) %></div>

<%
  MultiSiteManager msm = null;

  if (webSite instanceof MainWebSite) {
    msm = ((MainWebSite) webSite).getMultiSiteManager();
  }
%>
<form action="editsites2.jsp" method="post">

<% if (msm != null) { %>
  <fieldset class="meshcmseditor">
    <legend><fmt:message key="sitesGeneral" /></legend>

    <div class="meshcmsfield">
      <input type="checkbox" id="useDirsAsDomains" name="useDirsAsDomains"
       value="true"<%= msm.isUseDirsAsDomains() ? " checked='checked'" : "" %> />
      <label for="useDirsAsDomains"><fmt:message key="sitesDirsAsDomains" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="checkbox" id="manageTripleWs" name="manageTripleWs"
       value="true"<%= msm.isManageTripleWs() ? " checked='checked'" : "" %> />
      <label for="manageTripleWs"><fmt:message key="sitesManageTripleWs" /></label>
    </div>

    <div class="meshcmsfieldlabel">
      <label for="mainWebSiteDomains"><fmt:message key="sitesMainWebSiteDomains" /></label>
    </div>

    <div class="meshcmsfield">
      <input type="text" id="mainWebSiteDomains" name="mainWebSiteDomains"
       style="width: 90%;" value="<%= Utils.noNull(msm.getMainWebSiteDomains()) %>" />
    </div>
  </fieldset>
<% } else { %>
  <input type="hidden" name="useDirsAsDomains" value="true" />
  <input type="hidden" name="manageTripleWs" value="true" />
<% } %>

  <fieldset class="meshcmseditor">
    <legend><fmt:message key="sitesList" /></legend>

    <table class="meshcmseditor" cellspacing="0" style="width: 100%;">
      <tr>
        <th><fmt:message key="sitesHeaderName" /></th>
        <th><fmt:message key="sitesHeaderAliases" /></th>
        <th><fmt:message key="sitesHeaderCMS" /></th>
        <th><fmt:message key="sitesHeaderBlockJSP" /></th>
      </tr>
<%
  if (msm != null) {
    String[] dirs = webSite.getFile(webSite.getVirtualSitesPath()).list();
    List jspBlocks = msm.getJSPBlocks();

    for (int i = 0; i < dirs.length; i++) {
%>
      <tr>
        <td><label for="aliases_<%= dirs[i] %>"><%= dirs[i] %></label></td>
        <td>
          <input type="text" id="aliases_<%= dirs[i] %>"
           name="aliases_<%= dirs[i] %>" style="width: 90%;"
           value="<%= Utils.noNull(msm.getDomains(dirs[i])) %>" />
        </td>
        <td style="text-align: center;">
        <% if (((MainWebSite) webSite).getVirtualSite(dirs[i]).getCMSPath() != null) { %>
          <img src="filemanager/images/tick.png" alt=""
           style='vertical-align:middle;' title="<fmt:message key="genericYes" />" />
        <% } else { %>
          &nbsp;
        <% } %>
        </td>
        <td style="text-align: center;">
          <input type="checkbox" name="blockjsp_<%= dirs[i] %>" value="true"
           <%= jspBlocks.contains(dirs[i]) ? "checked='checked'" : "" %> />
        </td>
      </tr>
<%
    }
  }
%>
      <tr>
        <td style="width: 25%;">
          <input type="text" id="newsite_dirname"
           style="width: 90%;" name="newsite_dirname" />
        </td>
        <td style="width: 45%;">
          <input type="text" id="newsite_aliases"
           style="width: 90%;" name="newsite_aliases" />
        </td>
        <td style="width: 15%; text-align: center;">
          <input type="checkbox" id="newsite_cms" name="newsite_cms" value="true"
           checked="checked" />
        </td>
        <td style="width: 15%; text-align: center;">
          <input type="checkbox" name="newsite_blockjsp" value="true" />
        </td>
      </tr>

    </table>
  </fieldset>

  <div class="meshcmsbuttons">
    <input type="submit" value="<fmt:message key="genericUpdate" />" />
  </div>
</form>

</body>
</html>
