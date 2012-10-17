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
  if (!userInfo.canDo(UserInfo.CAN_DO_ADMINTASKS)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }
%>

<html>
<head>
<%= webSite.getAdminMetaThemeTag() %>
<title><fmt:message key="configSave" /></title>
</head>

<body>

<p>
<%
  Configuration c = webSite.getConfiguration();

  c.setVisualExtensions(Utils.tokenize(request.getParameter("visualTypes"), ":;,. "));
  c.setUseAdminTheme(Utils.isTrue(request.getParameter("useAdminTheme")));
  c.setPreventHotlinking(Utils.isTrue(request.getParameter("preventHotlinking")));
  c.setHighQualityThumbnails(Utils.isTrue(request.getParameter("highQualityThumbnails")));
  c.setReplaceThumbnails(Utils.isTrue(request.getParameter("replaceThumbnails")));

  c.setCacheType(Utils.parseInt(request.getParameter("cacheType"), Configuration.NO_CACHE));
  c.setTidy(Utils.parseInt(request.getParameter("tidy"), Configuration.TIDY_NO));
  c.setMailServer(request.getParameter("mailServer"));
  c.setSmtpUsername(request.getParameter("smtpUsername"));
  c.setSmtpPassword(request.getParameter("smtpPassword"));

  c.setUpdateInterval(Utils.parseInt(request.getParameter("updateInterval"), c.getUpdateInterval()));
  c.setBackupLife(Utils.parseInt(request.getParameter("backupLife"), c.getBackupLife()));
  c.setStatsLength(Utils.parseInt(request.getParameter("statsLength"), c.getStatsLength()));
  c.setAlwaysRedirectWelcomes(Utils.isTrue(request.getParameter("alwaysRedirectWelcomes")));
  c.setSearchMovedPages(Utils.isTrue(request.getParameter("searchMovedPages")));
  c.setAlwaysDenyDirectoryListings(Utils.isTrue(request.getParameter("alwaysDenyDirectoryListings")));
  c.setHideExceptions(Utils.isTrue(request.getParameter("hideExceptions")));
  c.setEditorModulesCollapsed(Utils.isTrue(request.getParameter("editorModulesCollapsed")));
  c.setRedirectRoot(Utils.isTrue(request.getParameter("redirectRoot")));
  c.setPasswordProtected(Utils.isTrue(request.getParameter("passwordProtected")));

  int el = Utils.parseInt(request.getParameter("excerptLength"), -1);
  if (el >= 0 && el != c.getExcerptLength()) {
    c.setExcerptLength(el);
    webSite.getSiteMap().setObsolete(true);
  }

  c.setSiteName(request.getParameter("siteName"));
  c.setSiteHost(request.getParameter("siteHost"));
  c.setSiteDescription(request.getParameter("siteDescription"));
  c.setSiteKeywords(request.getParameter("siteKeywords"));
  c.setSiteAuthor(request.getParameter("siteAuthor"));
  c.setSiteAuthorURL(request.getParameter("siteAuthorURL"));

  webSite.setLastAdminThemeBlock(0L); // re-enable the ability to use a custom admin theme
  webSite.updateSiteMap(true); // needed to re-init the cache

  if (c.store(webSite)) {
%>
    <fmt:message key="configSaveOk" />
    <script type="text/javascript">location.replace('index.jsp');</script>
<%
  } else {
%>
    <fmt:message key="configError" />
    <a href="editconfig1.jsp"><fmt:message key="genericRetry" /></a>.
<%
  }
%>
</p>

</body>
</html>
