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
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="org.meshcms.webui.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />
<jsp:useBean id="fileNameComparator" scope="session" class="org.meshcms.util.FileNameComparator" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  if (!userInfo.canDo(UserInfo.CAN_BROWSE_FILES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }
%>

<%
  String cp = request.getContextPath();
  SiteInfo siteInfo = webSite.getSiteInfo();
  boolean showThumbs = Utils.isTrue(request.getParameter("thumbnails"));
  Path folderPath = new Path(request.getParameter("folder"));
  File folder = webSite.getFile(folderPath);
  FileManagerThumbnail thumbMaker = new FileManagerThumbnail();
  thumbMaker.setHighQuality(webSite.getConfiguration().isHighQualityThumbnails());
  String thumbName = thumbMaker.getSuggestedFileName();

  if (folder.isDirectory()) {
    Locale locale = WebUtils.getPageLocale(pageContext);
    ResourceBundle bundle = WebUtils.getPageResourceBundle(pageContext);
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    File[] list = folder.listFiles();
    Arrays.sort(list, fileNameComparator);

    Path welcomePath = webSite.findCurrentWelcome(folderPath);
    String welcome = (welcomePath == null) ? null : welcomePath.getLastElement();
%>

<html>

<head>
  <%= webSite.getDummyMetaThemeTag() %>
  <title>List Files</title>
  <script type="text/javascript">
  // <![CDATA[
    if (window.parent.document.forms['fmfm'].s_thumbs) {
      window.parent.document.forms['fmfm'].f_dir.value = "<%= folderPath %>";
      window.parent.document.forms['fmfm'].s_thumbs.value = '<%= showThumbs %>';
      // window.parent.fm_xTreeExpandTo(window.parent.folder<%= WebUtils.getMenuCode(folderPath) %>);
    }

    function toggleSelection(chkId) {
      var elm = document.getElementById(chkId);
      elm.checked = elm.checked ? "" : "checked";
    }
  // ]]>
  </script>
  <link href="filemanager.css" type="text/css" rel="stylesheet" />

  <style type="text/css">
    body {
      margin: 0px;
      overflow: scroll;
    }

    .miniblock {
      display: block;
      width: <%= FileManagerThumbnail.THUMB_WIDTH %>px;
      max-width: <%= FileManagerThumbnail.THUMB_WIDTH %>px;
      overflow: hidden;
      padding: 0px;
    }

    td {
      white-space: nowrap;
    }

    input {
      border: none!important;
    }
  </style>
</head>

<body>
<form name="filelist">
<%
    if (!showThumbs) {
%>
<table cellspacing="0">
 <thead>
  <tr>
   <th>&nbsp;</th>
   <th>&nbsp;</th>
   <th><fmt:message key="fmListName" /></th>
   <th>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
   <th><fmt:message key="fmListSize" /></th>
   <th>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
   <th><fmt:message key="fmListDate" /></th>
   <th width="100%" align="right">
     <% if (!userInfo.canWrite(webSite, folderPath)) { %>
       <img src="images/lock.gif" title="<fmt:message key="fmListLocked" />" alt=""/>
     <% } %>
     <%= Help.icon(webSite, webSite.getRequestedPath(request), Help.FILE_MANAGER, userInfo) %>
   </th>
  </tr>
 </thead>

 <tbody>
<%
    } else {
%>
<div align="right" style="background-color: #D4D0C8;"><%= Help.icon(webSite, webSite.getRequestedPath(request), Help.FILE_MANAGER, userInfo) %></div>
<%
    }

    for (int i = 0; i < list.length; i++) {
      String name = list[i].getName();
      Path filePath = folderPath.add(name);
      FolderXTree.DirectoryInfo di =
          FolderXTree.getDirectoryInfo(webSite, userInfo, filePath);

      if (di.include) {
        String color = "";

        if (FileTypes.isPage(name)) {
          color = name.equals(welcome) ? "ffff99" : "eaeaea";
          color = " bgcolor='" + color + "'";
        }

        String id = "fs_" + WebUtils.getMenuCode(name);

        if (showThumbs) {
          Path thumbPath = null;

          if (!list[i].isDirectory() && FileTypes.isLike(list[i].getName(), "jpg")) {
            thumbPath = thumbMaker.checkAndCreate(webSite, filePath, thumbName);
          }
%>
  <table title="<%= name %>" style="display: inline; width: <%= FileManagerThumbnail.THUMB_WIDTH %>px;">
   <tr>
    <td onclick="javascript:toggleSelection('<%= id %>');"><% if (thumbPath != null) {
      %><img src="<%= cp + '/' + thumbPath %>" alt=""/><%
      } else {
        %><div style="border: 1px solid #D8CECB; padding: 51px 45px;"><img
           src="images/<%= list[i].isDirectory() ? FileTypes.DIR_ICON : FileTypes.getIconFile(name) %>" alt=""/></div><%
      } %></td>
   </tr>

   <tr<%= color %>>
    <td><div class="miniblock"><input type="checkbox" id="<%= id %>"
     name="<%= name %>" value="<%= name %>" />
     <label for="<%= id %>"><%= name %></label></div></td>
   </tr>
  </table>
<%
        } else {
%>
  <tr<%= color %>>
   <td onclick="javascript:toggleSelection('<%= id %>');"><img class="icon" src="images/<%= list[i].isDirectory() ? FileTypes.DIR_ICON :
     FileTypes.getIconFile(name) %>" vspace="2" title="<%= FileTypes.getDescription(name) %>" alt=""></td>
   <td><input type="checkbox" id="<%= id %>"
    name="<%= name %>" value="<%= name %>" /></td>
   <td><label for="<%= id %>"><%= name %></label>
     <%
          String theme = siteInfo.getPageTheme(filePath);

          if (!Utils.isNullOrEmpty(theme)) {
            Object[] args = { theme };
            theme = MessageFormat.format(bundle.getString("fmUsedTheme"), args);
     %>
       <img src="images/bullet_star.png" alt="<%= theme %>" title="<%= theme %>" />
     <%
          }
     %>
   </td>
   <td>&nbsp;</td>
   <td align="right"><%= list[i].isDirectory() ? "&nbsp;" : WebUtils.formatFileLength(list[i].length(), locale, bundle) %></td>
   <td>&nbsp;</td>
   <td><%= df.format(new Date(list[i].lastModified())) %></td>
   <td>&nbsp;</td>
  </tr>
<%
        }
      }
    }

    if (!showThumbs) {
%>
 </tbody>
</table>
<%
    }
  }
%>
</form>
</body>
</html>
