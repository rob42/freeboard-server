<% out.print("<?xml version='1.0' encoding='" +
             org.meshcms.util.Utils.SYSTEM_CHARSET + "'?>"); %>

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

<%@ page contentType="application/rss+xml" %>
<%@ page import="java.io.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="com.opensymphony.module.sitemesh.*" %>
<%@ page import="com.opensymphony.module.sitemesh.parser.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%
  SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
  Path root = new Path(request.getParameter("root"));
  List pagesList = new ArrayList(webSite.getSiteMap().getPagesList(root));
  
  Collections.sort(pagesList, new Comparator() {
    public int compare(Object o1, Object o2) {
      PageInfo p1 = (PageInfo) o1;
      PageInfo p2 = (PageInfo) o2;
      return new Long(p2.getLastModified()).compareTo(new Long(p1.getLastModified()));
    }
  });
  
  int max = Utils.parseInt(request.getParameter("max"), pagesList.size());
  
  Iterator iter = pagesList.iterator();
  SiteMap siteMap = webSite.getSiteMap();
  String homeURL = WebUtils.getContextHomeURL(request).toString();
  response.setContentType("text/xml; charset=" + Utils.SYSTEM_CHARSET);

  ResizedThumbnail thumbMaker = new ResizedThumbnail();
  thumbMaker.setHighQuality(webSite.getConfiguration().isHighQualityThumbnails());
  thumbMaker.setMode(ResizedThumbnail.MODE_SCALE);
  thumbMaker.setWidth(512);
  thumbMaker.setHeight(300);
%>
<rss version="2.0">
    <channel>
        <title><%= webSite.getConfiguration().getSiteName() %></title>
        <description><%= Utils.noNull(webSite.getConfiguration().getSiteDescription()) %></description>
        <link><%= homeURL %></link>
        <generator><%= WebSite.APP_NAME + ' ' + WebSite.VERSION_ID %></generator>
<%
  for (int i = 0; i < max && iter.hasNext(); i++) {
      PageInfo pi = (PageInfo) iter.next();
      Path servedPath = siteMap.getServedPath(pi.getPath());
      String imageName = Utils.removeExtension(servedPath.getLastElement()) +
          PageInfo.ARTICLE_IMAGE_SUFFIX;
      Path imagePath = servedPath.getParent().add(imageName);
      String imageTag = "";

      if (webSite.getFile(imagePath).exists()) {
        Path thumbPath = thumbMaker.checkAndCreate(webSite, imagePath,
            thumbMaker.getSuggestedFileName());

        if (thumbPath != null) {
          imageTag = "<p><img alt=\"\" src=\"" + homeURL + imagePath.getAsLink() + "\" /></p>\n";
        }
      }
%>
<item>
    <title><%= pi.getTitle() %></title>
    <link><%= homeURL + webSite.getAbsoluteLink(pi) %></link>
    <pubDate><%= dateFormat.format(new Date(pi.getLastModified())) %></pubDate>
    <description><![CDATA[<%= imageTag + pi.getExcerpt() %>]]></description>
</item>
<%
  }
%>
    </channel>
</rss>
