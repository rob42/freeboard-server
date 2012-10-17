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
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module: none
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

  String cp = request.getContextPath();
  String mp = md.getModulePath().getAsLink();
  String fName = cp + mp + "/flowplayer.jsp?path=" + md.getModuleArgumentDirectoryPath(webSite, true) + "&modulepath=" + mp;
%>

<%--
<div align="center">
  <object type="application/x-shockwave-flash" data="<%= cp %><%= mp %>/flowplayer.swf" width="320" height="325" id="FlowPlayer">
    <param name="allowScriptAccess" value="sameDomain" />
    <param name="movie" value="<%= cp %><%= mp %>/flowplayer.swf" />
    <param name="quality" value="high" />
    <param name="scale" value="noScale" />
    <param name="flashvars" value="configFileName=<%= URLEncoder.encode(fName, Utils.SYSTEM_CHARSET) %>" />
  </object>
</div>
--%>

<script type="text/javascript" src="<%= cp %>/<%= webSite.getAdminPath() %>/scripts/swfobject/swfobject.js"></script>

<div id="flashcontent" align="center">
<%
    File[] files = md.getModuleFiles(webSite, true);

    if (files != null) {
      Locale locale = WebUtils.getPageLocale(pageContext);
      ResourceBundle bundle =
          ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);
      Arrays.sort(files, new FileNameComparator());
%>
<table align="center" border="0" cellspacing="10" cellpadding="0">
<%
      for (int i = 0; i < files.length; i++) {
        if (Utils.getExtension(files[i], false).equalsIgnoreCase("flv")) {
          WebUtils.updateLastModifiedTime(request, files[i]);
%>
 <tr valign="top">
  <td><img src="<%= cp + '/' + webSite.getAdminPath() %>/filemanager/images/<%= FileTypes.getIconFile(files[i].getName()) %>" border="0" alt="<%= FileTypes.getDescription(files[i].getName()) %>" /></td>
  <td><a href="<%= cp + "/servlet/org.meshcms.core.DownloadServlet/" + webSite.getPath(files[i]) %>"><%= files[i].getName() %></a></td>
  <td align="right"><%= WebUtils.formatFileLength(files[i].length(), locale, bundle) %></td>
 </tr>
<%
        }
      }
    }
%>
</table>
</div>

<script type="text/javascript">
// <![CDATA[
   var so = new SWFObject("<%= cp %><%= mp %>/flowplayer.swf", "FlowPlayer", "320", "325", "7", "#FFFFFF");
   so.addParam("flashvars", "configFileName=<%= URLEncoder.encode(fName, Utils.SYSTEM_CHARSET) %>");
   so.write("flashcontent");
// ]]>
</script>
