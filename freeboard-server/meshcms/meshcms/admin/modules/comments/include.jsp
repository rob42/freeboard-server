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
<%@ page import="javax.mail.*" %>
<%@ page import="javax.mail.internet.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="com.opensymphony.module.sitemesh.*" %>
<%@ page import="com.opensymphony.module.sitemesh.parser.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />

<%--
  Advanced parameters for this module:
  - date = none (default) | normal | full
  - notify = (e-mail address to send notifications of new comments)
  - form_css = (name of a css class for full form)
  - field_css = (name of a css class for input fields)
  - max_age = (max number of days after which comments are not shown)
  - moderated = true | false (default) (logged users can always publish directly)
  - html = true | false (default) (show a basic HTML editor)
  - parse = true | false (default) if true, find hyperlinks in comments - only if not html
  - math = true | false (default)
  - captcha = true (default) | false
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

  Path commentsPath = md.getModuleArgumentPath(false);

  if (commentsPath == null) {
    commentsPath = md.getModuleDataPath(webSite).add(md.getPagePath(), md.getLocation());
  }

  File commentsDir = webSite.getFile(commentsPath);

  /* if (!commentsDir.isDirectory()) {
    throw new IllegalStateException(Utils.getFilePath(commentsDir) +
        " is not a directory");
  } */

  boolean moderated = Utils.isTrue(md.getAdvancedParam("moderated", "false"));
  boolean html = Utils.isTrue(md.getAdvancedParam("html", "false"));
  boolean parse = Utils.isTrue(md.getAdvancedParam("parse", "false"));
  boolean math = Utils.isTrue(md.getAdvancedParam("math", "false"));
  boolean captcha = Utils.isTrue(md.getAdvancedParam("captcha", "true"));

  if (!userInfo.isGuest()) {
    moderated = false;
  }

  Locale locale = WebUtils.getPageLocale(pageContext);
  ResourceBundle pageBundle = ResourceBundle.getBundle
      ("org/meshcms/webui/Locales", locale);
  
  String name = request.getParameter("mcc_name");
  String text = request.getParameter("mcc_text");

  String delId = request.getParameter("delId");

  if (!Utils.isNullOrEmpty(delId)) {
    File delFile = new File(commentsDir, delId);

    if (delFile.exists()) {
      if (userInfo.canWrite(webSite, md.getPagePath())) {
        webSite.delete(userInfo, webSite.getPath(delFile), false);
      } else {
        delFile.delete();
      }
    }
  }

  String showId = request.getParameter("showId");

  if (!Utils.isNullOrEmpty(showId)) {
    File hiddenFile = new File(commentsDir, showId);

    if (hiddenFile.exists()) {
      File visibleFile = new File(commentsDir, showId.replaceAll("mch", "mcc"));
      hiddenFile.renameTo(visibleFile);
    }
  }

  String hideId = request.getParameter("hideId");

  if (!Utils.isNullOrEmpty(hideId)) {
    File visibleFile = new File(commentsDir, hideId);

    if (visibleFile.exists()) {
      File hiddenFile = new File(commentsDir, hideId.replaceAll("mcc", "mch"));
      visibleFile.renameTo(hiddenFile);
    }
  }

  if (request.getMethod().equalsIgnoreCase("post") &&
      moduleCode.equals(request.getParameter("post_modulecode"))) {
    WebUtils.setBlockCache(request);
    WebUtils.removeFromCache(webSite, null, md.getPagePath());
    int sum = Utils.parseInt(request.getParameter("mcc_sum"), -1);
    int n1 = Utils.parseInt(request.getParameter("n1"), 0) /
        (Utils.SYSTEM_CHARSET.hashCode() >>> 8);
    int n2 = Utils.parseInt(request.getParameter("n2"), 0) /
        (WebSite.VERSION_ID.hashCode() >>> 8);
    boolean sumOK = !math || sum == n1 + n2;

    String cKey = (String) session.getAttribute
        (nl.captcha.servlet.Constants.SIMPLE_CAPCHA_SESSION_KEY) ;
    String cVal = request.getParameter("mcc_captcha");
    boolean captchaOK = !captcha ||
        (cKey != null && cKey.substring(0, 5).equalsIgnoreCase(cVal));

    if (!(Utils.isNullOrEmpty(name) || Utils.isNullOrEmpty(text)) &&
        sumOK && captchaOK) {
      if (name.length() > 20) {
        name = name.substring(0, 20);
      }
      
      PageAssembler pa = null;

      if (html) {
        pa = new PageAssembler();
        pa.addProperty("pagetitle", Utils.encodeHTML(name));
        pa.addProperty("meshcmsbody", text);
      }

      commentsDir.mkdirs();
      File commentFile = new File(commentsDir, (moderated ? "mch_" : "mcc_") +
          WebUtils.numericDateFormatter.format(new Date()) + (html ? ".html" : ".txt"));
      Utils.writeFully(commentFile, html ? pa.getPage() : name + "\n\n" + text);

      String email = md.getAdvancedParam("notify", null);

      if (Utils.checkAddress(email)) {
        InternetAddress address = new InternetAddress(email);
        Session mailSession = WebUtils.getMailSession(webSite);
        MimeMessage outMsg = new MimeMessage(mailSession);
        outMsg.setFrom(address);
        outMsg.addRecipient(Message.RecipientType.TO, address);
        outMsg.setSubject("Comment added on " + request.getServerName());
        outMsg.setHeader("Content-Transfer-Encoding", "8bit");
        outMsg.setHeader("X-MeshCMS-Log", "Sent from " + request.getRemoteAddr() +
            " at " + new Date() + " using page /" + md.getPagePath());
        
        String url = WebUtils.getContextHomeURL(request).append
            (md.getPagePath().getAsLink()).toString();
        StringBuffer sb = new StringBuffer();
        sb.append("A comment has been added to ");
        sb.append(url);
        sb.append(" by ");
        sb.append(name);
        sb.append(" (");
        sb.append(request.getRemoteAddr());
        sb.append("):\n\n");
        sb.append(text);
        sb.append("\n\nDelete: ");
        sb.append(url);
        sb.append("?delId=");
        sb.append(commentFile.getName());
        
        if (moderated) {
          sb.append("\nShow: ");
          sb.append(url);
          sb.append("?showId=");
          sb.append(commentFile.getName());
        } else {
          sb.append("\nHide: ");
          sb.append(url);
          sb.append("?hideId=");
          sb.append(commentFile.getName());
        }
        
        outMsg.setText(sb.toString());
        Transport.send(outMsg);
      }
      
      name = text = "";
    }
  }

  // numbers to verify submitted post
  int n1 = Utils.getRandomInt(30) + 1;
  int n2 = Utils.getRandomInt(30) + 1;

  String langCode = pageBundle.getString("TinyMCELangCode");

  if (Utils.isNullOrEmpty(langCode)) {
    langCode = locale.getLanguage();
  }

  if (html) {
%>
<script type='text/javascript'
 src='<%= request.getContextPath() + '/' + webSite.getAdminScriptsPath() %>/tiny_mce/tiny_mce.js'></script>
<%
  }
%>
<script type="text/javascript">
// <![CDATA[
  function deleteComment(id) {
    if (confirm("<%= pageBundle.getString("commentsConfirmDel") %>")) {
      var f = document.forms["mcc_<%= md.getLocation() %>"];
      f.delId.value = id;
      f.submit();
    }
  }

  function hideComment(id) {
    var f = document.forms["mcc_<%= md.getLocation() %>"];
    f.hideId.value = id;
    f.submit();
  }

  function showComment(id) {
    var f = document.forms["mcc_<%= md.getLocation() %>"];
    f.showId.value = id;
    f.submit();
  }

  function submitComment() {
    var f = document.forms["mcc_<%= md.getLocation() %>"];

    if (f.mcc_name.value == "") {
      alert("<%= pageBundle.getString("commentsNoName") %>");
      f.mcc_name.focus();
      return false;
    }

    if (window.tinyMCE) {
      tinyMCE.triggerSave();
    }

    if (f.mcc_text.value == "") {
      alert("<%= pageBundle.getString("commentsNoText") %>");
      f.mcc_text.focus();
      return false;
    }

    <% if (math) { %>
    if (isNaN(f.mcc_sum.value) || f.mcc_sum.value != <%= n1 + n2 %>) {
      alert("<%= pageBundle.getString("commentsWrongSum") %>");
      f.mcc_sum.focus();
      return false;
    }
    <% } %>

    return true;
  }

  if (window.tinyMCE) {
    tinyMCE.init({
      mode : "exact",
      theme : "simple",
      elements : "mcc_text",
      language : "<%= langCode %>"
    });
  }
// ]]>
</script>

<% if (userInfo.canDo(UserInfo.CAN_DO_ADMINTASKS)) { %>
<p><a href="<%= webSite.getLink(md.getModulePath().add("admin1.jsp"), md.getPagePath()) %>"><%= pageBundle.getString("commentsManage") %></a></p>
<% } %>

<form name="mcc_<%= md.getLocation() %>" method="post" action="">
<input type="hidden" name="post_modulecode" value="<%= moduleCode %>" />
<input type="hidden" name="delId" value="" />
<input type="hidden" name="showId" value="" />
<input type="hidden" name="hideId" value="" />
<div class="<%= md.getAdvancedParam("form_css", "mailform") %>">

<%
  String fieldStyle = md.getAdvancedParam("field_css", "formfields");
  
  int maxAge = Utils.parseInt(md.getAdvancedParam("max_age", ""), 0);
  long start = (maxAge > 0) ? System.currentTimeMillis() - maxAge *
      Configuration.LENGTH_OF_DAY : 0L;
  
  File[] files = null;
  
  if (commentsDir.exists() && commentsDir.isDirectory()) {
    files = commentsDir.listFiles();
  }

  if (files != null && files.length > 0) {
    Arrays.sort(files, new ReverseComparator(new FileDateComparator()));
    DateFormat df = md.getDateFormat(locale, "date");

    for (int i = 0; i < files.length; i++) {
      boolean isHTML = FileTypes.isPage(files[i].getName());
      boolean isText = FileTypes.isLike(files[i].getName(), "txt");

      if ((isHTML || isText) && files[i].lastModified() > start) {
        WebUtils.updateLastModifiedTime(request, files[i]);
        boolean hidden = false;
        Reader reader = null;
        String title;
        String body;

        try {
          if (isHTML) {
            HTMLPageParser fpp = new HTMLPageParser();
            reader = new InputStreamReader(new FileInputStream(files[i]),
                Utils.SYSTEM_CHARSET);
            HTMLPage pg = (HTMLPage) fpp.parse(Utils.readAllChars(reader));
            title = pg.getTitle();
            body = pg.getBody();
          } else {
            body = Utils.encodeHTML(Utils.readFully(files[i]));
            title = "<em>Anonymous</em>";
            int nn = body.indexOf("\n\n");
            int nnLen = 2;

            if (nn < 0) {
              nn = body.indexOf("\r\n\r\n");
              nnLen = 4;
            }

            if (nn >= 0) {
                title = body.substring(0, nn);
                body = body.substring(nn + nnLen);
            }

            if (parse) {
              body = WebUtils.findLinks(body, true);
              body = WebUtils.findEmails(body);
            }

            body = body.replaceAll("\n", "<br />");
          }

          if (files[i].getName().startsWith("mch")) {
            hidden = true;

            if (userInfo.canWrite(webSite, md.getPagePath())) {
              body = "<h4>" + pageBundle.getString("commentsAuthorize") +
                  "</h4>\n" + body;
            } else {
              body = "<p><em>" + pageBundle.getString("commentsNotAuthorized") +
                  "</em></p>";
            }
          }
  %>
 <div class="includeitem">
  <div class="includetitle">
    <%= Utils.isNullOrEmpty(title) ? "&nbsp;" : title %>
    <% if (userInfo.canWrite(webSite, md.getPagePath())) { %>
      (
      <a href="javascript:deleteComment('<%= files[i].getName() %>');"><%= pageBundle.getString("commentsDelete") %></a>
      |
      <a href="javascript:<%= hidden ? "show" : "hide" %>Comment('<%= files[i].getName() %>');"><%= pageBundle.getString(hidden ? "commentsShow" : "commentsHide") %></a>
      )
    <% } %>
  </div>
<%
          if (df != null) {
%>
  <div class="includedate">
    (<%= df.format(new Date(files[i].lastModified())) %>)
  </div>
<%
          }
%>
  <div class="includetext">
    <%= body %>
  </div>
 </div>
<%
          if (reader != null) {
            reader.close();
          }
        } catch (Exception ex) {}
      }
    }
  }
%>

<%
  if (!org.meshcms.extra.StaticExporter.isExportRequest(request)) {
%>
 <div class="includeitem">
  <div class="includetext">
    <div><label for="mcc_name"><%= pageBundle.getString("commentsName") %></label></div>
    <div><input type="text" name="mcc_name" id="mcc_name" class="<%= fieldStyle %>"
     maxlength="20" value="<%= Utils.encodeHTML(name) %>" /></div>
  </div>
  <div class="includetext">
    <div><label for="mcc_text"><%= pageBundle.getString("commentsText") %></label></div>
    <div><textarea name="mcc_text" id="mcc_text" class="<%= fieldStyle %>"
      rows="12" cols="80" style="height: 12em;"><%= Utils.encodeHTML(text) %></textarea></div>
  </div>
  <div class="includetext">
    <% if (math) { %>
    <div>
      <label for="mcc_sum"><%= n1 %> + <%= n2 %> =</label>
      <input type="text" name="mcc_sum" id="mcc_sum" class="<%= fieldStyle %>" style="width: 3em;" />
      <input type="hidden" name="n1" value="<%= n1 * (Utils.SYSTEM_CHARSET.hashCode() >>> 8) %>" />
      <input type="hidden" name="n2" value="<%= n2 * (WebSite.VERSION_ID.hashCode() >>> 8) %>" />
    </div>
    <% } %>
    <% if (captcha) { %>
    <div>
      <img src="<%= request.getContextPath() %>/captcha.jpg" alt="captcha" align="right" />
      <label for="mcc_captcha"><%= pageBundle.getString("commentsCaptcha") %></label>
      <input type="text" name="mcc_captcha" id="mcc_captcha" class="<%= fieldStyle %>" style="width: 8em;" />
    </div>
    <% } %>
    <div style="margin-top: 1em; clear: both;">
      <input type="submit" value="<%= pageBundle.getString("commentsSubmit") %>" onclick="return submitComment();" />
    </div>
  </div>
 </div>
<%
  }
%>

</div>
</form>
