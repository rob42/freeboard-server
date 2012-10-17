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
<%@ page import="java.util.regex.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="com.opensymphony.module.sitemesh.*" %>
<%@ page import="com.opensymphony.module.sitemesh.parser.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module:
  - css = (name of a css class)
  - date = none (default) | normal | full
  - sort = newest (default) | mostviewed
  - mode = html (default) | text
  - maxchars = maximum length of the excerpt for each article (default as in site configuration)
  - entries = number of entries per page (default 5)
  - history = true (default) | false (link to previous articles)
  - keywords = true (default) | false (keywords aka tags after each article)
  - readlink = true (default) | false (link after each article)
  - updatedate = true (default) | false (updates page last modified time)
  - imagesize = size in pixels of article images (index.html -> index_image.jpg) not shown if not specified
  - tag = a fixed tag to be matched by articles (taken from the url if not specified)
--%>

<%@ taglib prefix="c" uri="standard-core" %>
<%@ taglib prefix="f" uri="standard-fmt" %>

<%!
  public class PageDateComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      try {
        long f1 = ((PageInfo) o1).getLastModified();
        long f2 = ((PageInfo) o2).getLastModified();

        if (f1 > f2) {
          return -1;
        } else if (f1 < f2) {
          return 1;
        }
      } catch (ClassCastException ex) {
      }

      return 0;
    }
  }

  public class PageHitsComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      try {
        int f1 = ((PageInfo) o1).getTotalHits();
        int f2 = ((PageInfo) o2).getTotalHits();

        if (f1 > f2) {
          return -1;
        } else if (f1 < f2) {
          return 1;
        }
      } catch (ClassCastException ex) {}

      return 0;
    }
  }

  public class Entry {
    private String title;
    private String body;
    private String link;
    private String date;
    private String[] keywords;
    private String image;

    public boolean isHasKeywords() {
      return getKeywords() != null && getKeywords().length > 0;
    }

    public boolean isHasDate() {
      return !Utils.isNullOrEmpty(getDate());
    }

    public String getTitle() {
      return title;
    }

    public String getBody() {
      return body;
    }

    public String getLink() {
      return link;
    }

    public String getDate() {
      return date;
    }

    public String[] getKeywords() {
      return keywords;
    }

    public String getImage() {
      return image;
    }
  }
%>

<%
    Locale locale = WebUtils.getPageLocale(pageContext);
    pageContext.setAttribute("pageLocale", locale.toString());

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

    Path argPath = md.getModuleArgumentDirectoryPath(webSite, true);
    String tag = md.getAdvancedParam("tag", request.getParameter("tag"));
    String date = request.getParameter("date");

    if (argPath != null) {
      SiteMap siteMap = webSite.getSiteMap();
      ArrayList pagesList = new ArrayList(siteMap.getPagesList(argPath));
      Iterator iter = pagesList.iterator();
      Path pagePathInMenu = siteMap.getPathInMenu(md.getPagePath());
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

      while (iter.hasNext()) {
        PageInfo item = (PageInfo) iter.next();

        if (item.getPath().equals(pagePathInMenu)) {
          iter.remove();
        } else if (!Utils.isNullOrEmpty(tag) &&
                Utils.searchString(item.getKeywords(), tag, false) < 0) {
          iter.remove();
        } else if (!Utils.isNullOrEmpty(date) &&
                !sdf.format(new Date(item.getLastModified())).equals(date)) {
          iter.remove();
        }
      }

      boolean sortByHits = "mostviewed".equalsIgnoreCase(md.getAdvancedParam("sort", null));
      Comparator comp = sortByHits ?
          (Comparator) new PageHitsComparator() : (Comparator) new PageDateComparator();
      Collections.sort(pagesList, comp);

      int imageSize = Utils.parseInt(md.getAdvancedParam("imagesize", null), 0);
      ResizedThumbnail thumbMaker = null;

      if(imageSize > 0) {
        thumbMaker = new ResizedThumbnail();
        thumbMaker.setHighQuality(webSite.getConfiguration().isHighQualityThumbnails());
        thumbMaker.setMode(ResizedThumbnail.MODE_SCALE);
        thumbMaker.setWidth(imageSize);
        thumbMaker.setHeight(imageSize);
      }

      Path dirPath = webSite.getDirectory(pagePathInMenu);
      DateFormat df = md.getDateFormat(locale, "date");
      boolean updateDate = Utils.isTrue(md.getAdvancedParam("updatedate", "true"));
      boolean readLink = Utils.isTrue(md.getAdvancedParam("readlink", "true"));
      boolean keywords = Utils.isTrue(md.getAdvancedParam("keywords", "true"));
      boolean history = Utils.isTrue(md.getAdvancedParam("history", "true"));
      pageContext.setAttribute("readLink", new Boolean(readLink));
      boolean asText = "text".equalsIgnoreCase(md.getAdvancedParam("mode", null));
      int maxChars = Utils.parseInt(md.getAdvancedParam("maxchars", ""),
          webSite.getConfiguration().getExcerptLength());
      int entries = Utils.parseInt(md.getAdvancedParam("entries", ""), 5);
      int firstEntry = history ? Utils.parseInt(request.getParameter("firstentry"), 0) : 0;
      pageContext.setAttribute("cssAttr", md.getCSSAttribute("css"));
      List pages = new ArrayList();

      for (int i = firstEntry; i < firstEntry + entries && i < pagesList.size(); i++) {
        PageInfo pi = (PageInfo) pagesList.get(i);

        if (updateDate) {
          WebUtils.updateLastModifiedTime(request, pi.getLastModified());
        }

        Entry e = new Entry();
        e.body = pi.getExcerpt();

        if (asText) {
          if (maxChars < webSite.getConfiguration().getExcerptLength()) {
            e.body = Utils.limitedLength(e.body, maxChars);
          }
        } else if (maxChars > 0) {
          Path servedPath = siteMap.getServedPath(pi.getPath());
          HTMLPageParser fpp = new HTMLPageParser();
          Reader reader = new InputStreamReader(new FileInputStream
              (webSite.getFile(servedPath)), Utils.SYSTEM_CHARSET);
          HTMLPage pg = (HTMLPage) fpp.parse(Utils.readAllChars(reader));
          reader.close();
          e.body = WebUtils.createExcerpt(webSite, pg.getBody(), maxChars,
                  request.getContextPath(), pi.getPath(), md.getPagePath());

          if (thumbMaker != null) {
            String imageName = Utils.removeExtension(servedPath.getLastElement()) +
                PageInfo.ARTICLE_IMAGE_SUFFIX;
            Path imagePath = servedPath.getParent().add(imageName);

            if (webSite.getFile(imagePath).exists()) {
              Path thumbPath = thumbMaker.checkAndCreate(webSite, imagePath,
                  thumbMaker.getSuggestedFileName());

              if (thumbPath != null) {
                e.image = webSite.getLink(thumbPath, dirPath).toString();
              }
            }
          }
        }

        if (df != null) {
          e.date = df.format(new Date(pi.getLastModified()));
        }

        e.title = pi.getTitle();
        e.link = webSite.getLink(pi, dirPath).toString();

        if (keywords) {
          e.keywords = pi.getKeywords();
        }

        pages.add(e);
      }

      pageContext.setAttribute("pages", pages);

      if (history && !sortByHits) {
        boolean newer = firstEntry > 0;
        boolean older = firstEntry + entries < pagesList.size();
        String baseURL = request.getContextPath() + md.getPagePath().getAsLink();
        String newerLink = null;
        String olderLink = null;

        if (newer || older) {
          if (!Utils.isNullOrEmpty(tag)) {
            baseURL = WebUtils.addToQueryString(baseURL, "tag", tag, true);
          }

          if (!Utils.isNullOrEmpty(date)) {
            baseURL = WebUtils.addToQueryString(baseURL, "date", date, false);
          }

          if (newer) {
            newerLink = firstEntry - entries > 0 ? WebUtils.addToQueryString(baseURL, "firstentry",
                    Integer.toString(firstEntry - entries), false) : baseURL;
          }

          if (older) {
            olderLink = WebUtils.addToQueryString(baseURL, "firstentry",
                    Integer.toString(firstEntry + entries), false);
          }
        }

        pageContext.setAttribute("newer", newerLink);
        pageContext.setAttribute("older", olderLink);
      }
    }
%>

<f:setLocale value="${pageLocale}"/>
<f:setBundle basename="org.meshcms.webui.Locales"/>

<div class="<c:out value="${cssAttr}"/>">
  <c:forEach var="page" items="${pages}">
    <div class="includeitem">
      <h3 class="includetitle">
        <a href="<c:out value="${page.link}"/>"><c:out value="${page.title}"/></a>
      </h3>
      <c:if test="${page.hasDate}">
        <h4 class="includedate">
          (<c:out value="${page.date}"/>)
        </h4>
      </c:if>
      <c:if test="${page.image != null}">
        <p class="includeimage">
          <img src="<c:out value="${page.image}"/>" alt="" />
        </p>
      </c:if>
      <div class="includetext">
        <c:out value="${page.body}" escapeXml="false"/>
      </div>
      <c:if test="${readLink}">
        <p class="includereadmore">
          <a href="<c:out value="${page.link}"/>"><f:message key="readMore"/></a>
        </p>
      </c:if>
      <c:if test="${page.hasKeywords}">
        <p class="includetags">
          <f:message key="includeTags"/>
          <c:forEach var="keyword" items="${page.keywords}" varStatus="idx">
            <a href="?tag=<c:out value="${keyword}"/>"><c:out value="${keyword}"/></a><c:if test="${!idx.last}">,</c:if>
          </c:forEach>
        </p>
      </c:if>
    </div>
  </c:forEach>
  <c:if test="${newer != null || older != null}">
    <p class="includenavigation">
      <c:if test="${newer != null}">
        <a href="<c:out value="${newer}"/>"><f:message key="includeNewer"/></a>
      </c:if>
      <c:if test="${newer != null && older != null}">|</c:if>
      <c:if test="${older != null}">
        <a href="<c:out value="${older}"/>"><f:message key="includeOlder"/></a>
      </c:if>
    </p>
  </c:if>
</div>
