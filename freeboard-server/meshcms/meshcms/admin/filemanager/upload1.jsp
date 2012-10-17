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
<%@ page import="org.meshcms.webui.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  if (!userInfo.canDo(UserInfo.CAN_MANAGE_FILES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }
%>

<html>
<head>
  <%= webSite.getDummyMetaThemeTag() %>
  <title><fmt:message key="fmUploadTitle" /></title>
  <link href="filemanager.css" type="text/css" rel="stylesheet" />
  <script type="text/javascript" src="../scripts/jquery/jquery.min.js"></script>
  <script type="text/javascript" src="../scripts/progressbar.js"></script>
</head>

<body>
  <p align="right"><%= Help.icon(webSite, webSite.getRequestedPath(request),
      Help.UPLOAD, userInfo) %></p>

  <form name="upform" action="upload2.jsp" method="post"
   enctype="multipart/form-data" onsubmit="javascript:showWaiting();">
  <input type="hidden" name="dir" />
    <table align="center" border="0" cellspacing="10" cellpadding="2">
      <tr>
        <td><fmt:message key="fmUploadHint" /></td>
      </tr>

      <tr>
        <td><input type="file" name="upfile" size="25"></td>
      </tr>

      <tr>
        <td>
          <input type="checkbox" name="fixname" id="fixname" value="true" checked="checked">
          <label for="fixname"><fmt:message key="fmUploadFixFileName" /></label>
        </td>
      </tr>

      <tr class="upBtns">
        <th>
          <input type="submit" value="<fmt:message key="fmUploadButton" />">
          <input type="button" value="<fmt:message key="genericCancel" />" onclick="javascript:history.back();" />
        </th>
      </tr>

      <tr>
        <td align="center">
          <div id="warningMsg"><fmt:message key="fmUploadWarn" /></div>
          <div id="waitMsg" style="visibility:hidden;"><fmt:message key="fmUploadWait" /></div>
          <div id="pb"></div>
        </td>
      </tr>
    </table>
  </form>
  <script type="text/javascript">
  // <![CDATA[
    document.forms['upform'].dir.value = window.parent.document.forms['fmfm'].f_dir.value;

    function showWaiting() {
      $(".upBtns").hide();
      $('#warningMsg').hide();
      $('#waitMsg').show();
      $('#pb').progressBar({
        url: 'upload_progress.jsp',
        backgroundColor: '#061f32',
        hideAtEnd: true
      });
    }
  // ]]>
  </script>
</body>
</html>
