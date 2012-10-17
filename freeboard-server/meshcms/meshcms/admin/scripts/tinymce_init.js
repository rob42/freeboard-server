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

/*
 This file defines the configuration of TinyMCE when editing pages. Some
 variables are defined before loading this file:

 languageCode - the language for TinyMCE (from user preferences)
 linkListPath - the path to the list of links to pages in the site map
 cssPath - the path to the main css of the current theme
 contextPath - the web application context path (usually empty)
 adminPath - the path to the admin folder
  (TinyMCE is contained in adminPath + '/scripts/tiny_mce')

 If you want to modify this file, copy it into the folder that contains your
 theme and make your changes there.
*/

tinyMCE.init({
  theme : 'advanced',
  language : languageCode,
  mode : 'exact',
  elements : 'meshcmsbody',
  external_link_list_url : linkListPath,
  entity_encoding : 'raw',
  auto_cleanup_word : true,
  fix_list_elements : true,
  plugins : 'contextmenu,directionality,table,inlinepopups,paste',
  theme_advanced_buttons1_add : 'separator,ltr,rtl',
  theme_advanced_buttons2_add_before : 'cut,copy,paste,pastetext,pasteword,selectall,separator',
  theme_advanced_buttons3_add_before : 'tablecontrols,separator',
  theme_advanced_toolbar_location : 'top',
  theme_advanced_toolbar_align : 'left',
  theme_advanced_statusbar_location : "bottom",
  relative_urls : true,
  content_css : cssPath,
  file_browser_callback : 'editor_fileBrowserCallBack',
  theme_advanced_resizing : true,
  theme_advanced_resizing_use_cookie : false,
  theme_advanced_resize_horizontal : true,
  debug : false
});
