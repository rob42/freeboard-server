/*
 * Copyright 2004-2009 Luciano Vernaschi
 *
 * This file is part of MeshCMS.
 *
 * MeshCMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MeshCMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MeshCMS.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Functions in this file are used in the MeshCMS file manager.
 */

/**
 * Expands xTree up to the desired leaf
 */
function fm_xTreeExpandTo(leaf) {
  var lastLeaf = leaf;
  var leaves = new Array();

  while (leaf.parentNode) { // stores all parents
    leaf = leaf.parentNode;
    leaves.push(leaf);
  }

  leaf = leaves.pop();

  while (leaf) {
    leaf.expand(); // expands parents
    leaf = leaves.pop();
  }

  lastLeaf.select();
}

/**
 * (De)selects all files in the current folder
 */
function fm_selectAll(selAll) {
  var doc = window.frames['listframe'].document;
  var files = doc.getElementsByTagName('input');

  for (var i = 0; i < files.length; i++) {
    if (files[i].type == 'checkbox') {
      files[i].checked = selAll ? "checked" : "";
    }
  }
}

function fm_toggleAll() {
  var doc = window.frames['listframe'].document;
  var files = doc.getElementsByTagName('input');

  for (var i = 0; i < files.length; i++) {
    if (files[i].type == 'checkbox') {
      files[i].checked = files[i].checked ? "" : "checked";
    }
  }
}

/**
 * Encodes a URL
 */
function fm_encodeURL(urlStr) {
  return escape(urlStr).replace(/\+/g, '%2C').replace(/\"/g,'%22').replace(/\'/g, '%27');
}

/**
 * Triggers a dummy action. process.jsp will do nothing
 */
function fm_dummy() {
  var fmfm = document.forms['fmfm'];
  fmfm.f_action.value = "none";
  fmfm.submit();
}

/**
 * Asks process.jsp to rebuild the site map
 */
function fm_refresh() {
  var fmfm = document.forms['fmfm'];
  fmfm.f_action.value = "refresh";
  fmfm.submit();
}

/**
 * Asks process.jsp to delete all selected items
 */
function fm_deleteFiles() {
  var fmfm = document.forms['fmfm'];
  fm_collectFiles();

  if (fmfm.f_files.value.length > 1) {
    if (confirm(msgConfirmDelete(fmfm.f_files.value.substring(1)))) {
      fmfm.f_action.value = "delete";
      fmfm.submit();
    }
  }
}

/**
 * Asks process.jsp to fix file names of all selected items
 */
function fm_fixFileNames() {
  var fmfm = document.forms['fmfm'];
  fm_collectFiles();

  if (fmfm.f_files.value.length > 1) {
    fmfm.f_action.value = "fixnames";
    fmfm.submit();
  }
}

/**
 * Asks process.jsp to change the theme associated to the selected item(s)
 */
function fm_changeTheme(themeName) {
  var fmfm = document.forms['fmfm'];
  fm_collectFiles();

  if (fmfm.f_files.value.length > 1) {
    fmfm.f_action.value = themeName ? "theme" + themeName : "theme";
    fmfm.submit();
  }
}

/**
 * Asks process.jsp to rename the selected item
 */
function fm_renameFile() {
  var fmfm = document.forms['fmfm'];
  var fname = fm_getSelectedFile();

  if (fname) {
    newName = prompt(msgNewName(), fname);

    if (newName != null && newName != fname) {
      fmfm.f_action.value = "rename" + newName;
      fmfm.submit();
    }
  }
}

/**
 * Asks process.jsp to create a copy of the selected file in the same folder
 */
function fm_duplicateFile() {
  var fmfm = document.forms['fmfm'];
  var fname = fm_getSelectedFile();

  if (fname) {
    newName = prompt(msgCopyName(), fname);

    if (newName != null && newName != fname) {
      fmfm.f_action.value = "copy" + newName;
      fmfm.submit();
    }
  }
}

/**
 * Asks process.jsp to create a new file
 */
function fm_createFile() {
  name = prompt(msgNewFile(), "index.html");

  if (name != null && name != "" && name != "null") {
    var fmfm = document.forms['fmfm'];
    fmfm.f_action.value = "createfile" + name;
    fmfm.submit();
  }
}

/**
 * Asks process.jsp to create a new folder
 */
function fm_createDir() {
  name = prompt(msgNewFolder(), msgSuggestedFolderName());

  if (name != null && name != "" && name != "null") {
    var fmfm = document.forms['fmfm'];
    fmfm.f_action.value = "createdir" + name;
    fmfm.submit();
  }
}

/**
 * Asks process.jsp to show the selected item
 */
function fm_viewFile() {
  if (fm_getSelectedFile()) {
    var fmfm = document.forms['fmfm'];
    fmfm.f_action.value = "view";
    fmfm.submit();
  }
}

function fm_viewFileNewWindow() {
  var file = fm_getCompletePath();

  if (file) {
    window.open(cp + file);
  }
}

/**
 * Asks process.jsp to show the selected page in the editor
 */
function fm_editFile() {
  if (fm_getSelectedFile()) {
    var fmfm = document.forms['fmfm'];
    fmfm.f_action.value = "edit";
    fmfm.submit();
  }
}

function fm_editFileNewWindow() {
  var file = fm_getCompletePath();

  if (file) {
    window.open("../editsrc.jsp?path=" + file);
  }
}

/**
 * Asks process.jsp to load a simple text editor to modify the selected file
 */
function fm_editPage() {
  var file = fm_getSelectedFile();

  if (file) {
    if (isVisuallyEditable(file)) {
      var fmfm = document.forms['fmfm'];
      fmfm.f_action.value = "wysiwyg";
      fmfm.submit();
    } else {
      alert(msgNotVisuallyEditable().replace("{0}", fm_getSelectedFile()));
    }
  }
}

function fm_editPageNewWindow() {
  var file = fm_getCompletePath();

  if (file) {
    if (isVisuallyEditable(file)) {
      window.open(cp + file + "?meshcmsaction=edit");
    } else {
      alert(msgNotVisuallyEditable().replace("{0}", fm_getSelectedFile()));
    }
  }
}

/**
 * Calls the servlet that allows to download files of any kind
 */
function fm_downloadFile(contextPath) {
  var fpath = fm_getCompletePath();

  if (fpath) {
    location.href = contextPath +
      "/servlet/org.meshcms.core.DownloadServlet" + fpath;
  }
}

/**
 * Calls the servlet that allows to download files and folders as ZIP files
 */
function fm_downloadZip(contextPath) {
  var fpath = fm_getCompletePath();

  if (fpath) {
    location.href = contextPath +
      "/servlet/org.meshcms.core.DownloadZipServlet" + fpath;
  }
}

/**
 * Shows the page in a new window
 */
function fm_viewPage(name) {
  popup = window.open(name, '_blank');
  popup.focus();
}

/**
 * Opens the upload window
 */
function fm_uploadFile() {
  window.frames['listframe'].location.href = 'upload1.jsp';
}

/**
 * Opens the unzip window
 */
function fm_unzipFile() {
  var fpath = fm_getCompletePath();

  if (fpath) {
    window.frames['listframe'].location.href = 'unzip.jsp?zip=' + fpath;
  }
}

/**
 * Asks process.jsp to unzip the previously selected archive
 */
function fm_doUnzip(zform) {
  var zdir = zform.createdir.checked ? zform.dirname.value : "";
  var fmfm = document.forms['fmfm'];
  fmfm.f_action.value = "unzip" + zdir;
  fmfm.submit();
}

/**
 * Asks process.jsp to mark the selected items for moving
 */
function fm_clipboardCut() {
  var fmfm = document.forms['fmfm'];
  fm_collectFiles();

  if (fmfm.f_files.value.length > 1) {
    fmfm.f_action.value = "clipboardcut";
    fmfm.submit();
  }
}

/**
 * Asks process.jsp to mark the selected items for copying
 */
function fm_clipboardCopy() {
  var fmfm = document.forms['fmfm'];
  fm_collectFiles();

  if (fmfm.f_files.value.length > 1) {
    fmfm.f_action.value = "clipboardcopy";
    fmfm.submit();
  }
}

/**
 * Asks process.jsp to copy/move the previously marked items
 */
function fm_clipboardPaste() {
  var fmfm = document.forms['fmfm'];
  fmfm.f_action.value = "clipboardpaste";
  fmfm.submit();
}

/**
 * Finds all items that have been selected and stores a comma-separated list
 * in the "f_files" field
 */
function fm_collectFiles() {
  var doc = window.frames['listframe'].document;
  var files = doc.getElementsByTagName('input');
  var flist = "";

  for (var i = 0; i < files.length; i++) {
    if (files[i].type == 'checkbox' && files[i].checked) {
      flist += "," + files[i].value; // the list will start with a comma,
    }                 // but this is taken into account in other methods
  }

  document.forms['fmfm'].f_files.value = flist;
}

/**
 * Returns the name of the selected item. If zero or more than one item have
 * been selected, this function will return null, since this one is called
 * for operations that require a single file to work with (rename, view etc.)
 */
function fm_getSelectedFile() {
  var fmfm = document.forms['fmfm'];
  fm_collectFiles();

  if (fmfm.f_files.value.length > 1) {
    if (fmfm.f_files.value.indexOf(",", 1) != -1) {
      alert(msgSingleFile());
      return null;
    }

    return fmfm.f_files.value.substring(1);
  }
}

/**
 * Returns the current folder
 */
function fm_getDir() {
  var dirName = document.forms['fmfm'].f_dir.value;

  if (dirName != "") {
    dirName = "/" + dirName;
  }

  return dirName;
}

/**
 * Uses fm_getDir and fm_getSelectedFile to build the path of the selected item.
 * Since fm_getSelectedFile is used, this function returns a value if and only
 * if a single item has been selected
 */
function fm_getCompletePath() {
  var fname = fm_getSelectedFile();

  if (fname) {
    return fm_getDir() + "/" + fname;
  }
}

/**
 * Returns the selected item. This function is used when the file manager is
 * opened to select a file or a folder, e.g. from the page editor. If no items
 * have been selected, the user can choose to return the current folder
 */
function fm_return(field_name) {
  var fmfm = document.forms['fmfm'];
  fm_collectFiles(); // fills f_files
  var fdir = fm_getDir();
  var fpath;

  if (fmfm.f_files.value.length > 1) { // at least one item has been selected
    if (fmfm.f_files.value.indexOf(",", 1) != -1) { // more than one item
      alert(msgSingleFile());
      return null;
    }

    fpath = fdir + "/" + fmfm.f_files.value.substring(1); // get the item name
  } else if (confirm(msgSelectFolder())) {
    fpath = fdir ? fdir : "/"; // return the current folder
  }

  if (fpath) { // we have something to return
    window.opener.editor_setFile(field_name, fpath);
    window.close();
  }
}

/**
 * Asks process.jsp (not) to display thumbnails
 */
function fm_viewThumbnails(thumbs) {
  var fmfm = document.forms['fmfm'];
  fmfm.s_thumbs.value = thumbs;
  fmfm.f_action.value = "none";
  fmfm.submit();
}

/**
 * Closes the file manager
 */
function fm_closeFileManager() {
  if (window.name && window.name == "filemanager") { // it is a popup, so close the window
    window.close();
  } else { // it's not a popup, so show the control panel
    location.href = "../index.jsp";
  }
}

/**
 * Changes the last modified date to the selected files
 */
function fm_touchFiles() {
  var fmfm = document.forms['fmfm'];
  fm_collectFiles();

  if (fmfm.f_files.value.length > 1) {
    fmfm.f_action.value = "touch";
    fmfm.submit();
  }
}

function fm_closeCalendar(cal) {
  cal.hide();
  var fmfm = document.forms['fmfm'];

  if (fmfm.f_action.value) {
    fm_collectFiles();

    if (fmfm.f_files.value.length > 1) {
      fmfm.submit();
    }
  }
}
