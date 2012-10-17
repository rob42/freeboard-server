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
 * Functions in this file are used in the MeshCMS page editor of the. Some
 * functions are also used in the Configuration editor.
 */

  /**
   * Some default values, just to avoid JavaScript errors
   */
  contextPath = window.contextPath || "";
  adminPath = window.adminPath || "meshcms/admin";
  languageCode = window.languageCode || "en";
  linkListPath = window.linkListPath || "/meshcms/admin/tinymce_linklist.jsp";
  cssPath = window.cssPath || "/meshcms/admin/theme/main.css";

  /**
   * Full path of the admin folder (context path included)
   */
  var adminFullPath = contextPath + '/' + adminPath;

  /**
   * Used to store a reference to the TinyMCE window used by the
   * file_browser_callback
   */
  var mcewin = null;

  /**
   * Called by TinyMCE to display the file manager to choose links and images.
   */
  function editor_fileBrowserCallBack(field_name, url, type, win) {
    if (win) {
      mcewin = win; // store for future use in editor_setFile
    } else {
      mcewin = null;
    }

    var popup = window.open(
      adminFullPath + '/filemanager/index.jsp?field='+field_name+'&type='+type,
      'filemanager',
      'width=630,height=420,menubar=no,status=yes,toolbar=no,resizable=yes'
    );

    popup.focus();
  }

  /**
   * Opens the file manager
   *
   * field_name: the id of the field that will store the return value
   *
   * see: editor_setFile below and fm_return in admin/filemanager.js
   */
  function editor_openFileManager(field_name) {
    var url = contextPath + '/' + adminPath + '/filemanager/index.jsp';

    if (field_name) {
      url += '?field=' + field_name;
      mcewin = null; // file manager not opened from within TinyMCE, so clear this
    }

    var popup = window.open(
      url,
      'filemanager',
      'width=680,height=420,menubar=no,status=yes,toolbar=no,resizable=yes'
    );

    popup.focus();
  }

  /**
   * Stores the return value of the file manager into the desired field
   */
  function editor_setFile(field_name, filePath) {
    // if mcewin contains a valid value, use it
    if (mcewin && mcewin.document && mcewin.document.getElementById(field_name)) {
      mcewin.document.getElementById(field_name).value = contextPath + filePath;
    } else { // field_name is in the document
      document.getElementById(field_name).value = filePath;
    }
  }

  /**
   * Shows the module entry fields allowing the module to be edited.
   */
   function editor_moduleShow(cont_id,elem_id,icon_id) {
    var cont = document.getElementById(cont_id);
    cont.parentNode.removeChild(cont);
    editor_toggleHideShow(elem_id,icon_id);
   }

  /**
   * Shows or hides the element and sets correct icon.
   */
  function editor_toggleHideShow(elem_id,icon_id) {
    var elem = document.getElementById(elem_id);
    var icon = icon_id != null ? document.getElementById(icon_id) : null;
    if (elem.style.display == 'none') {
      elem.style.display = '';
      if (icon != null) {
	      icon.src = adminFullPath + '/filemanager/images/bullet_toggle_minus.png';
      }
    } else {
      elem.style.display = 'none';
      if (icon != null) {
	      icon.src = adminFullPath + '/filemanager/images/bullet_toggle_plus.png';
	    }
    }
  }

  /**
   * Clears a field
   */
  function editor_clr(fid) {
    document.getElementById(fid).value = "";
    document.getElementById(fid).focus();
  }

  /**
   * jQuery scripts
  $(function() {
    $("input[@type=text]").each(function(i) {
      $(this).before("<img src='" + adminFullPath +
        "/filemanager/images/textfield_delete.png' class='fldclr' id='fldclr-" +
        this.id + "'/>&nbsp;");
    });

    $(".fldclr").click(function(i) {
      var el = document.getElementById(this.id.substring(7));
      el.value = "";
      el.focus();
    });
  });
   */
