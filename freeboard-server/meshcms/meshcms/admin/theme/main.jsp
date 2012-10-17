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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<!--
     MeshCMS | open source web content management system
       more info at http://www.cromoteca.com/meshcms

       developed by Luciano Vernaschi
       released under the GNU General Public License (GPL)
       visit http://www.gnu.org/licenses/gpl.html for details on GPL
//-->

<%
  String themePath = request.getContextPath() + "/" + webSite.getAdminThemePath();
  String scriptsPath = request.getContextPath() + "/" + webSite.getAdminScriptsPath();
  String adminPath = request.getContextPath() + "/" + webSite.getAdminPath();
%>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>MeshCMS: <decorator:title default="" /></title>
    <link rel="stylesheet" type="text/css" href="<%= themePath %>/main.css" />
    <link rel="stylesheet" type="text/css" href="<%= themePath %>/meshcms.css" />
    <decorator:head />
    <script type="text/javascript">
      function maximizeArea(img) {
        document.getElementById("pagecontent").style.margin = "0";
        document.getElementById("additionalcolumn").style.display = "none";
        img.style.display = "none";
      }
    </script>
  </head>

  <body <decorator:getProperty property="body.onload" writeEntireProperty="true" /> id="cmsbody">
    <div id="header">
    </div>

    <div id="pagecontentcolumn">
      <div id="pagecontent">
        <div id="mainarea">
          <img align="right" onclick="javascript:maximizeArea(this);"
           src="<%= request.getContextPath() + '/' + webSite.getAdminPath() %>/filemanager/images/arrow_right.png" />
          <div class="pagebody">
            <h3>
              <a href="<%= request.getContextPath() + '/' + webSite.getAdminPath() %>/index.jsp">MeshCMS</a>:
              <decorator:title default="" />
            </h3>
            <decorator:body />
          </div>
        </div>
      </div>
    </div>

    <div id="additionalcolumn">
      <div class="cmssidemodule">
        <% if (userInfo.canDo(UserInfo.CAN_DO_ADMINTASKS)) {
          Runtime runtime = Runtime.getRuntime();
        %>
          <h4><fmt:message key="sysTitle" /></h4>
          <div class="sysinfo">
            <fmt:message key="sysVersion" />
            <%= WebSite.VERSION_ID %>
          </div>
          
          <div class="sysinfo">
            <fmt:message key="sysCharset" />
            <%= Utils.SYSTEM_CHARSET %>
          </div>
          
          <div class="sysinfo">
            <fmt:message key="sysMemory" />
            <%= (runtime.totalMemory() - runtime.freeMemory()) * 100 / runtime.maxMemory() %>%
          </div>
          
          <% if (webSite instanceof MainWebSite) { %>
            <div class="sysinfo">
              <fmt:message key="sysVirtuals" />
              <%= ((MainWebSite) webSite).getMultiSiteManager().getSiteCount() %>
            </div>
          <% } %>

          <div class="sysinfo">
            <fmt:message key="sysUser" />
            <%= userInfo.getDisplayName() %>
          </div>
        <% } %>
      </div>
    </div>

    <div id="footer">
      Copyright &copy; 2004-2009
      <a href="http://www.cromoteca.com/" target="blank">Luciano Vernaschi</a> |
      Powered by <a href="http://www.cromoteca.com/meshcms/" target="blank">MeshCMS</a>
    </div>
  </body>
</html>
