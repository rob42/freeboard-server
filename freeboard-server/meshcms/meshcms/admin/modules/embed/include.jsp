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

<%--
  Advanced parameters for this module:
  - delay = true | false (default)
--%>

<%
  String moduleCode = request.getParameter("modulecode");
  ModuleDescriptor md = null;

  if (moduleCode != null) {
    md = (ModuleDescriptor) request.getAttribute(moduleCode);
  }

  if (md == null) {
    if (!response.isCommitted()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    return;
  }

  String arg = md.getArgument();
  Path argPath = new Path(arg);

  if (webSite.getFile(argPath).isFile()) {
    arg = Utils.readFully(webSite.getFile(argPath));
  }

  arg = Utils.decodeHTML(arg);
  Path pagePath = webSite.getRequestedPath(request);
  boolean delay = Utils.isTrue(md.getAdvancedParam("delay", "false"));

  if (delay) {
%>
<script type="text/javascript">
  if (!window.jQuery) {
    document.write("<scr" + "ipt type='text/javascript' src='<%= webSite.getLink(webSite.getAdminScriptsPath().add("jquery/jquery.min.js"), pagePath) %>'></scr" + "ipt>");
  }
</script>

<script type="text/javascript">
  $(function() {
    var tm = window.setTimeout(function() {
      $("#embed_<%= moduleCode %>").empty().append("<%= arg.replaceAll("\"", "\\\\\"") %>");
    }, 1000);
  });
</script>

<div id="embed_<%= moduleCode %>">
  <img src="<%= webSite.getLink(md.getModulePath(), pagePath) %>/ajax-loader.gif"
   alt="Loading..." />
</div>
<%
  } else {
%>
<div><%= arg %></div>
<%
  }
%>
