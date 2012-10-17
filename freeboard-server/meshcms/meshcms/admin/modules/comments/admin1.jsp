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
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="org.meshcms.webui.*" %>
<%@ page import="com.opensymphony.module.sitemesh.*" %>
<%@ page import="com.opensymphony.module.sitemesh.parser.*" %>
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

  Path commentsPath = webSite.getModuleDataPath().add("comments");
  FileSearch search = new FileSearch(webSite.getFile(commentsPath), "mch_\\d+\\.(?:txt|html)");
  search.process();
  Path[] comments = search.getResults();
%>
<html>
  <head>
  <title><fmt:message key="commentsTitle" /></title>
  <%= webSite.getAdminMetaThemeTag() %>
  </head>
<body>
  <form action="admin2.jsp" method="post">
<%
  if (comments.length > 0) {
    for (int i = 0; i < comments.length; i++) {
      String name = comments[i].toString();
      String text;
      File f = webSite.getFile(commentsPath.add(comments[i]));

      if (name.endsWith(".txt")) {
        text = Utils.stripHTMLTags(Utils.readFully(f));
        text = text.replaceAll("\\n", "<br />");
      } else {
        HTMLPageParser fpp = new HTMLPageParser();
        Reader reader = new InputStreamReader(new FileInputStream(f),
                Utils.SYSTEM_CHARSET);
        HTMLPage pg = (HTMLPage) fpp.parse(Utils.readAllChars(reader));
        text = pg.getTitle() + "<br /><br />" + pg.getBody();
      }
%>
    <fieldset>
      <legend><%= comments[i].getParent().getParent().getAsLink() %></legend>
      <blockquote>
        <%= text %>
      </blockquote>
      <div>
        <input type="radio" id="leave_<%= i %>" name="<%= name %>" value="leave" checked="checked" />
        <label for="leave_<%= i%>"><fmt:message key="commentsLeave" /></label>
        <input type="radio" id="publish_<%= i %>" name="<%= name %>" value="publish"/>
        <label for="publish_<%= i%>"><fmt:message key="commentsPublish" /></label>
        <input type="radio" id="delete_<%= i %>" name="<%= name %>" value="delete"/>
        <label for="delete_<%= i%>"><fmt:message key="commentsRemove" /></label>
      </div>
    </fieldset>
<%    
    }
%>
    <input type="submit" value="<fmt:message key="genericSubmit" />"/>
<%    
  } else {
%>
    <p><fmt:message key="commentsNothing" /></p>
<%    
  }
%>
  </form>
</body>
</html>
