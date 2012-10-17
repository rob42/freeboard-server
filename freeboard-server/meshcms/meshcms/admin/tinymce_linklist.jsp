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

<%@ page contentType="text/javascript" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

var tinyMCELinkList = new Array(
<%
  String cp = request.getContextPath();
  Iterator iter = webSite.getSiteMap().getPagesList().iterator();
  SiteInfo siteInfo = webSite.getSiteInfo();
  String spacer = " . ";
  String separator = spacer;

  while (iter.hasNext()) {
    PageInfo pageInfo = (PageInfo) iter.next();
    int spaces = pageInfo.getLevel() * spacer.length();

    while (separator.length() < spaces) {
      separator += spacer;
    }

    out.write("[\"" + separator.substring(0, spaces) +
              siteInfo.getPageTitle(pageInfo) + "\", \"" + cp +
              webSite.getAbsoluteLink(pageInfo) + "\"]" + (iter.hasNext() ? "," : "") +
              "\n");
  }
%>
);
