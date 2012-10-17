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

<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="org.meshcms.webui.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />
<jsp:useBean id="userInfo" scope="session" class="org.meshcms.core.UserInfo" />
<jsp:useBean id="fileClipboard" scope="session" class="org.meshcms.webui.FileClipboard" />

<%@ taglib prefix="fmt" uri="standard-fmt-rt" %>
<fmt:setLocale value="<%= userInfo.getPreferredLocaleCode() %>" scope="request" />
<fmt:setBundle basename="org.meshcms.webui.Locales" scope="page" />

<%
  if (!userInfo.canDo(UserInfo.CAN_BROWSE_FILES)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                       "You don't have enough privileges");
    return;
  }
%>

<%
  ResourceBundle bundle = WebUtils.getPageResourceBundle(pageContext);

  String cp = request.getContextPath();
  String field = Utils.noNull(request.getParameter("field"));

  String thumbsParam = request.getParameter("thumbnails");
  // "type" is "image" when the file manager is called from within TinyMCE to
  // select an image
  boolean thumbnails = (thumbsParam == null) ?
    "image".equals(request.getParameter("type")) : Utils.isTrue(thumbsParam);
  thumbsParam = "&thumbnails=" + thumbnails;

  String folderParam = request.getParameter("folder");
  Path folderPath = (folderParam == null) ? userInfo.getHomePath() :
    new Path(folderParam);
%>

<html>
<head>
  <%= webSite.getDummyMetaThemeTag() %>
  <title><fmt:message key="homeFile" /></title>
  <link href="filemanager.css" type="text/css" rel="stylesheet" />
  <link href="../scripts/xmenu/xmenu.css" type="text/css" rel="stylesheet" />
  <link href="../scripts/xmenu/xmenu.windows.css" type="text/css" rel="stylesheet" />
  <link href="../scripts/xtree/xtree.css" type="text/css" rel="stylesheet">
  <link href="../scripts/jscalendar/calendar-win2k-1.css" type="text/css" rel="stylesheet">

  <script type="text/javascript">
  // <![CDATA[
    function msgConfirmDelete(path) {
      return "<fmt:message key="msgConfirmDelete"><fmt:param value="\" + path + \"" /></fmt:message>";
    }

    function msgSelectFolder() {
      return "<fmt:message key="msgSelectFolder" />";
    }

    function msgSingleFile() {
      return "<fmt:message key="msgSingleFile" />";
    }

    function msgNewName() {
      return "<fmt:message key="msgNewName" />";
    }

    function msgCopyName() {
      return "<fmt:message key="msgCopyName" />";
    }

    function msgNewFile() {
      return "<fmt:message key="msgNewFile" />";
    }

    function msgNewFolder() {
      return "<fmt:message key="msgNewFolder" />";
    }

    function msgNotVisuallyEditable() {
      return "<fmt:message key="fmErrorNoHTML" />";
    }

    function msgSuggestedFolderName() {
      return "<fmt:message key="msgSuggestedFolderName" />";
    }
    
    cp = "<%= cp %>";
    
    function isVisuallyEditable(file) {
      return file.match(/^.*?\.(?:<%=
        Utils.generateList(webSite.getConfiguration().getVisualExtensions(), "|")
      %>)$/);
    }
  // ]]>
  </script>
  <script type="text/javascript" src="../scripts/xmenu/cssexpr.js"></script>
  <script type="text/javascript" src="../scripts/xmenu/xmenu.js"></script>
  <script type="text/javascript" src="../scripts/xtree/xtree.js"></script>
  <script type="text/javascript" src="../scripts/filemanager.js"></script>
  <script type="text/javascript" src="../scripts/jscalendar/calendar.js"></script>
  <script type="text/javascript" src="../scripts/jscalendar/lang/<fmt:message key="DHTMLCalendarLangCode" />.js" charset="<fmt:message key="DHTMLCalendarLangCharset" />"></script>
  <script type="text/javascript" src="../scripts/jscalendar/calendar-setup.js"></script>
</head>

<body style="margin: 0px;" onload="fm_xTreeExpandTo(folder<%= WebUtils.getMenuCode(folderPath) %>)">
 <table cellspacing="0" class="full">
  <tr>
   <td colspan="2">
    <script type="text/javascript">
    // <![CDATA[
      webfxMenuImagePath = "../scripts/xmenu/images/";
      webfxMenuUseHover = true;
      WebFXMenu.prototype.borderLeft = 2;
      WebFXMenu.prototype.borderRight = 2;
      WebFXMenu.prototype.borderTop = 2;
      WebFXMenu.prototype.borderBottom = 2;
      WebFXMenu.prototype.paddingLeft = 1;
      WebFXMenu.prototype.paddingRight = 1;
      WebFXMenu.prototype.paddingTop = 1;
      WebFXMenu.prototype.paddingBottom	= 1;
      WebFXMenu.prototype.shadowLeft = 0;
      WebFXMenu.prototype.shadowRight = 0;
      WebFXMenu.prototype.shadowTop = 0;
      WebFXMenu.prototype.shadowBottom = 0;

      var fileMenu = new WebFXMenu;
      fileMenu.width = 200;
      
      var viewSubMenu = new WebFXMenu;
      viewSubMenu.width = 200;
      viewSubMenu.add(new WebFXMenuItem('<img src=\'images/application.png\' class=\'menuicon\'><fmt:message key="fmThisWindow" />', 'javascript:fm_viewFile()'));
      viewSubMenu.add(new WebFXMenuItem('<img src=\'images/application_double.png\' class=\'menuicon\'><fmt:message key="fmNewWindow" />', 'javascript:fm_viewFileNewWindow()'));
      fileMenu.add(new WebFXMenuItem('<img src=\'images/folder_page_white.png\' class=\'menuicon\'><fmt:message key="fmViewFile" />', null, null, viewSubMenu));
      
      var wysiwygSubMenu = new WebFXMenu;
      wysiwygSubMenu.width = 200;
      wysiwygSubMenu.add(new WebFXMenuItem('<img src=\'images/application.png\' class=\'menuicon\'><fmt:message key="fmThisWindow" />', 'javascript:fm_editPage()'));
      wysiwygSubMenu.add(new WebFXMenuItem('<img src=\'images/application_double.png\' class=\'menuicon\'><fmt:message key="fmNewWindow" />', 'javascript:fm_editPageNewWindow()'));
      fileMenu.add(new WebFXMenuItem('<img src=\'images/page_edit.png\' class=\'menuicon\'><fmt:message key="fmEditVisually" />', null, null, wysiwygSubMenu));
      
      var editSubMenu = new WebFXMenu;
      editSubMenu.width = 200;
      editSubMenu.add(new WebFXMenuItem('<img src=\'images/application.png\' class=\'menuicon\'><fmt:message key="fmThisWindow" />', 'javascript:fm_editFile()'));
      editSubMenu.add(new WebFXMenuItem('<img src=\'images/application_double.png\' class=\'menuicon\'><fmt:message key="fmNewWindow" />', 'javascript:fm_editFileNewWindow()'));
      fileMenu.add(new WebFXMenuItem('<img src=\'images/page_white_edit.png\' class=\'menuicon\'><fmt:message key="fmEditSrc" />', null, null, editSubMenu));
      
      fileMenu.add(new WebFXMenuSeparator());
      fileMenu.add(new WebFXMenuItem('<img src=\'images/folder_add.png\' class=\'menuicon\'><fmt:message key="fmNewFolder" />', 'javascript:fm_createDir()'));
      fileMenu.add(new WebFXMenuItem('<img src=\'images/page_add.png\' class=\'menuicon\'><fmt:message key="fmNewFile" />', 'javascript:fm_createFile()'));
      fileMenu.add(new WebFXMenuSeparator());
      fileMenu.add(new WebFXMenuItem('<img src=\'images/cross.png\' class=\'menuicon\'><fmt:message key="fmDelete" />', 'javascript:fm_deleteFiles()'));
      fileMenu.add(new WebFXMenuItem('<img src=\'images/textfield_rename.png\' class=\'menuicon\'><fmt:message key="fmRename" />', 'javascript:fm_renameFile()'));
      fileMenu.add(new WebFXMenuItem('<img src=\'images/tick.png\' class=\'menuicon\'><fmt:message key="fmFixFileNames" />', 'javascript:fm_fixFileNames()'));
      fileMenu.add(new WebFXMenuItem('<img src=\'images/date.png\' class=\'menuicon\'><fmt:message key="fmTouch" />', 'javascript:fm_touchFiles()'));
      fileMenu.add(new WebFXMenuItem('<img src=\'images/date_edit.png\' class=\'menuicon\'><fmt:message key="fmChangeDate" />', null));

      var editMenu = new WebFXMenu;
      editMenu.width = 200;
      editMenu.add(new WebFXMenuItem('<img src=\'images/cut.png\' class=\'menuicon\'><fmt:message key="fmCut" />', 'javascript:fm_clipboardCut()'));
      editMenu.add(new WebFXMenuItem('<img src=\'images/page_copy.png\' class=\'menuicon\'><fmt:message key="fmCopy" />', 'javascript:fm_clipboardCopy()'));
      editMenu.add(new WebFXMenuItem('<img src=\'images/paste_plain.png\' class=\'menuicon\'><fmt:message key="fmPaste" />', 'javascript:fm_clipboardPaste()'));
      editMenu.add(new WebFXMenuItem('<img src=\'images/application_double.png\' class=\'menuicon\'><fmt:message key="fmDuplicate" />', 'javascript:fm_duplicateFile()'));
      editMenu.add(new WebFXMenuSeparator());
      editMenu.add(new WebFXMenuItem('<img src=\'images/tag_blue_add.png\' class=\'menuicon\'><fmt:message key="fmSelAll" />', 'javascript:fm_selectAll(true)'));
      editMenu.add(new WebFXMenuItem('<img src=\'images/tag_blue_delete.png\' class=\'menuicon\'><fmt:message key="fmSelNone" />', 'javascript:fm_selectAll(false)'));
      editMenu.add(new WebFXMenuItem('<img src=\'images/tag_blue_edit.png\' class=\'menuicon\'><fmt:message key="fmSelInv" />', 'javascript:fm_toggleAll()'));

      var viewMenu = new WebFXMenu;
      viewMenu.width = 200;
      viewMenu.add(new WebFXMenuItem('<img src=\'images/application_view_detail.png\' class=\'menuicon\'><fmt:message key="fmDetails" />', 'javascript:fm_viewThumbnails(false)'));
      viewMenu.add(new WebFXMenuItem('<img src=\'images/application_view_tile.png\' class=\'menuicon\'><fmt:message key="fmThumbs" />', 'javascript:fm_viewThumbnails(true)'));
      viewMenu.add(new WebFXMenuSeparator());
      viewMenu.add(new WebFXMenuItem('<img src=\'images/arrow_refresh.png\' class=\'menuicon\'><fmt:message key="fmRefresh" />', 'javascript:fm_dummy()'));

      var toolsMenu = new WebFXMenu;
      toolsMenu.width = 200;
      toolsMenu.add(new WebFXMenuItem('<img src=\'images/application_get.png\' class=\'menuicon\'><fmt:message key="fmUpload" />', 'javascript:fm_uploadFile()'));
      toolsMenu.add(new WebFXMenuItem('<img src=\'images/application_put.png\' class=\'menuicon\'><fmt:message key="fmDownload" />', 'javascript:fm_downloadFile(\'<%= cp %>\')'));
      toolsMenu.add(new WebFXMenuItem('<img src=\'images/application_put.png\' class=\'menuicon\'><fmt:message key="fmDownloadZip" />', 'javascript:fm_downloadZip(\'<%= cp %>\')'));
      toolsMenu.add(new WebFXMenuSeparator());
      toolsMenu.add(new WebFXMenuItem('<img src=\'images/compress.png\' class=\'menuicon\'><fmt:message key="fmUnzip" />', 'javascript:fm_unzipFile()'));

      var themesMenu = new WebFXMenu;
      themesMenu.width = 200;
      themesMenu.add(new WebFXMenuItem('<img src=\'../theme/tx1x1.gif\' width=\'16\' height=\'16\' class=\'menuicon\'><fmt:message key="fmInherit" />', 'javascript:fm_changeTheme()'));
      themesMenu.add(new WebFXMenuItem('<img src=\'../theme/tx1x1.gif\' width=\'16\' height=\'16\' class=\'menuicon\'><fmt:message key="fmNoTheme" />', 'javascript:fm_changeTheme(\'<%= PageAssembler.EMPTY %>\')'));
      themesMenu.add(new WebFXMenuSeparator());

      <% String[] themes = webSite.getSiteMap().getThemeNames();

      for (int i = 0; i < themes.length; i++) { %>
        themesMenu.add(new WebFXMenuItem('<img src=\'../theme/tx1x1.gif\' width=\'16\' height=\'16\' class=\'menuicon\'><%=
            Utils.escapeSingleQuotes(Utils.beautify(themes[i], true)) %>', 'javascript:fm_changeTheme(\'<%= Utils.escapeSingleQuotes(themes[i]) %>\')'));
      <% } %>

      var toolBar = new WebFXMenuBar;
      toolBar.add(new WebFXMenuButton('<fmt:message key="fmFile" />', null, null, fileMenu));
      toolBar.add(new WebFXMenuButton('<fmt:message key="fmEdit" />', null, null, editMenu));
      toolBar.add(new WebFXMenuButton('<fmt:message key="fmView" />', null, null, viewMenu));
      toolBar.add(new WebFXMenuButton('<fmt:message key="fmTools" />', null, null, toolsMenu));
      toolBar.add(new WebFXMenuButton('<fmt:message key="fmThemes" />', null, null, themesMenu));
      toolBar.add(new WebFXMenuButton('<fmt:message key="fmClose" />', 'javascript:fm_closeFileManager()', null, null));
      document.write(toolBar);
    // ]]>
    </script>
   </td>
  </tr>

  <tr class="full">
   <td width="240" valign="top">
    <div style="width: 240px; height: 100%; overflow:auto;">
      <script type="text/javascript">
      // <![CDATA[
        webFXTreeConfig['usePersistence'] = false;

        webFXTreeConfig['rootIcon'] = 'images/world.png';
        webFXTreeConfig['openRootIcon'] = 'images/world_go.png';
        webFXTreeConfig['folderIcon'] = 'images/folder.png';
        webFXTreeConfig['openFolderIcon'] = 'images/folder_go.png';
        webFXTreeConfig['fileIcon'] = 'images/page_white.png';
        webFXTreeConfig['iIcon'] = '../scripts/xtree/images/I.png';
        webFXTreeConfig['lIcon'] = '../scripts/xtree/images/L.png';
        webFXTreeConfig['lMinusIcon'] = '../scripts/xtree/images/Lminus.png';
        webFXTreeConfig['lPlusIcon'] = '../scripts/xtree/images/Lplus.png';
        webFXTreeConfig['tIcon'] = '../scripts/xtree/images/T.png';
        webFXTreeConfig['tMinusIcon'] = '../scripts/xtree/images/Tminus.png';
        webFXTreeConfig['tPlusIcon'] = '../scripts/xtree/images/Tplus.png';
        webFXTreeConfig['blankIcon'] = '../scripts/xtree/images/blank.png';
        <% new FolderXTree(webSite, userInfo, pageContext.getOut(),
            thumbsParam, bundle.getString("fmSiteRoot")).process(); %>
        document.write(folder0);
      // ]]>
      </script>
    </div>
   </td>

   <td width="100%"><iframe src="showlist.jsp?folder=<%= folderPath %><%= thumbsParam %>"
    class="full" id="listframe" name="listframe"></iframe></td>
  </tr>

  <tr>
    <td colspan="2" bgcolor="#D4D0C8"
     style="border: 1px inset #D4D0C8; padding: 1px 6px 1px 6px;">
<%
  int n = fileClipboard.countFiles();

  if (n == 0) {
    out.write(bundle.getString("fmClipEmpty"));
  } else {
    Locale locale = WebUtils.getPageLocale(pageContext);
    MessageFormat formatter = new MessageFormat("", locale);
    double[] fileLimits = { 1, 2 };
    String [] fileStrings = { bundle.getString("fmClipOneFile"),
        bundle.getString("fmClipManyFiles") };
    ChoiceFormat choiceFormat = new ChoiceFormat(fileLimits, fileStrings);
    String pattern = bundle.getString("fmClipFull");
    Format[] formats = { choiceFormat, null, NumberFormat.getInstance() };
    formatter.applyPattern(pattern);
    formatter.setFormats(formats);
    Object[] args = { new Integer(n),
        fileClipboard.getDirPath().getAsLink(), new Integer(n) };
    out.write(formatter.format(args));
  }

  if (!field.equals("")) {
%>
     <div align="right"><input type="button" value="<fmt:message key="genericSelect" />"
       onclick="javascript:fm_return('<%= field %>');" />
      <input type="button" value="<fmt:message key="genericCancel" />"
       onclick="javascript:window.close();" /></div>
<%
  }
%>
    </td>
  </tr>

 <%--
 <tr>
  <td colspan="2">
   <form name="fmfm" method="post" action="process.jsp">
    action: <input type="text" name="f_action" id="f_action" />
    dir: <input type="text" name="f_dir" id="f_dir" value="<%= folderPath %>" />
    files: <input type="text" name="f_files" id="f_files" />
    thumbs: <input type="text" name="s_thumbs" id="s_thumbs" value="<%= thumbnails %>" />
    field: <input type="text" name="s_field" id="s_field" value="<%= field %>" />
   </form>
  </td>
 </tr>
 --%>
 </table>

 <form name="fmfm" method="post" action="process.jsp">
  <input type="hidden" name="f_action" id="f_action" />
  <input type="hidden" name="f_dir" id="f_dir" value="<%= folderPath %>" />
  <input type="hidden" name="f_files" id="f_files" />
  <input type="hidden" name="s_thumbs" id="s_thumbs" value="<%= thumbnails %>" />
  <input type="hidden" name="s_field" id="s_field" value="<%= field %>" />
 </form>

 <script type="text/javascript">
 // <![CDATA[
  Calendar.setup(
    {
      inputField : "f_action",
      ifFormat : "cdate%Y%m%d%H%M%S",
      button : "webfx-menu-object-23",
      onClose : fm_closeCalendar,
      showsTime : true,
      singleClick : false,
      align : "bR"
    }
  );
 // ]]>
 </script>
</body>
</html>
