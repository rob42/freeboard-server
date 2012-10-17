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

<%
  Path path = new Path(request.getParameter("path"));
  String cp = request.getContextPath();
  boolean isImage = FileTypes.isLike(path.getLastElement(), "jpg");
%>

<html>
<head>
  <title>Hotlinking</title>
</head>

<body style="font: 12px monospace;">

<p align="center" style="border: 1px solid #cccccc; padding: 25px;">
  <% if (isImage) {
    %><img src="<%= cp + '/' + path %>" alt=""><%
  } else {
    %><img src="<%= cp + '/' + webSite.getAdminPath() + "/filemanager/images/" +
        FileTypes.getIconFile(path.getLastElement()) %>" alt="">
    Get <a href="<%= cp + '/' + path %>"><%= path.getLastElement() %></a><%
  } %>
</p>

<p align="center">
 <%= isImage ? "Image" : "File" %> from
 <a href="<%= cp  %>/"><%= request.getServerName() %></a>
</p>

</body>
</html>
