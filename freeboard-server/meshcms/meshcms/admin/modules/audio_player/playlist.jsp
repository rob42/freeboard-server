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
--%><%@ page contentType="text/xml" import="java.io.*,java.util.*,org.meshcms.core.*,org.meshcms.util.*" %><jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" /><?xml version="1.0" encoding="<%= Utils.SYSTEM_CHARSET %>" ?>

<%
  Path path = new Path(request.getParameter("path"));
  Path mp = new Path(request.getParameter("modulepath"));
  String defaultImage = WebUtils.getContextHomeURL(request).append('/').append(mp).append("/defaultimage.jpg").toString();
%>

<playlist version="1" xmlns = "http://xspf.org/ns/0/">
  <title>Playlist loaded</title>

  <trackList>

<%
  File dir = webSite.getFile(path);
  String[] files = dir.list();

  if (files != null) {
    Arrays.sort(files);

    for (int i = 0; i < files.length; i++) {
      if (Utils.getExtension(files[i], false).equalsIgnoreCase("mp3")) {
        String base = WebUtils.getContextHomeURL(request).append('/').append(path).append('/').toString();
        String imgName = Utils.removeExtension(files[i]) + ".jpg";

        if (new File(dir, imgName).exists()) {
          imgName = base + imgName;
        } else {
          imgName = defaultImage;
        }
%>
    <track>
      <location><%= WebUtils.getContextHomeURL(request).append('/').append(path).append('/') %><%= files[i] %></location>
      <info><%= WebUtils.getContextHomeURL(request).append('/').append(path).append('/') %><%= files[i] %></info>
      <title><%= Utils.beautify(Utils.removeExtension(files[i]), true) %></title>
      <image><%= imgName %></image>
    </track>
<%
      }
    }
  }
%>
  </trackList>

</playlist>