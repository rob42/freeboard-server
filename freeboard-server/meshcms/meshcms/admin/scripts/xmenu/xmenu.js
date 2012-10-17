/*----------------------------------------------------------------------------\
|                                XMenu 1.12                                   |
|-----------------------------------------------------------------------------|
|                          Created by Erik Arvidsson                          |
|                  (http://webfx.eae.net/contact.html#erik)                   |
|                      For WebFX (http://webfx.eae.net/)                      |
|-----------------------------------------------------------------------------|
| An object based tree widget,  emulating the one found in microsoft windows, |
| with persistence using cookies. Works in IE 5+, Mozilla and konqueror 3.    |
|-----------------------------------------------------------------------------|
|                   Copyright (c) 1999 - 2002 Emil A Eklund                   |
|-----------------------------------------------------------------------------|
| This software is provided "as is", without warranty of any kind, express or |
| implied, including  but not limited  to the warranties of  merchantability, |
| fitness for a particular purpose and noninfringement. In no event shall the |
| authors or  copyright  holders be  liable for any claim,  damages or  other |
| liability, whether  in an  action of  contract, tort  or otherwise, arising |
| from,  out of  or in  connection with  the software or  the  use  or  other |
| dealings in the software.                                                   |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| This  software is  available under the  three different licenses  mentioned |
| below.  To use this software you must chose, and qualify, for one of those. |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| The WebFX Non-Commercial License          http://webfx.eae.net/license.html |
| Permits  anyone the right to use the  software in a  non-commercial context |
| free of charge.                                                             |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| The WebFX Commercial license           http://webfx.eae.net/commercial.html |
| Permits the  license holder the right to use  the software in a  commercial |
| context. Such license must be specifically obtained, however it's valid for |
| any number of  implementations of the licensed software.                    |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| GPL - The GNU General Public License    http://www.gnu.org/licenses/gpl.txt |
| Permits anyone the right to use and modify the software without limitations |
| as long as proper  credits are given  and the original  and modified source |
| code are included. Requires  that the final product, software derivate from |
| the original  source or any  software  utilizing a GPL  component, such  as |
| this, is also licensed under the GPL license.                               |
|-----------------------------------------------------------------------------|
| 2001-01-12 | Original Version Posted.                                       |
| 2001-11-20 | Added hover mode support and removed Opera focus hacks         |
| 2001-12-20 | (1.1) Added auto positioning and some properties to support    |
|            | this.                                                          |
| 2002-08-13 | (1.11) toString used ' for attributes. Changed to " to allow   |
|            | in properties/arguments.                                       |
| 2003-04-27 | (1.12) Added support for target property on menu item/button   |
|-----------------------------------------------------------------------------|
| Created 2001-01-12 | All changes are in the log above. | Updated 2003-04-27 |
\----------------------------------------------------------------------------*/



// check browsers
var ua = navigator.userAgent;
var opera = /opera [56789]|opera\/[56789]/i.test(ua);
var ie = !opera && /MSIE/.test(ua);
var ie50 = ie && /MSIE 5\.[01234]/.test(ua);
var ie6 = ie && /MSIE [6789]/.test(ua);
var ieBox = ie && (document.compatMode == null || document.compatMode != "CSS1Compat");
var moz = !opera && /gecko/i.test(ua);
var nn6 = !opera && /netscape.*6\./i.test(ua);
// define the default values
webfxMenuDefaultWidth			= 100;

webfxMenuDefaultBorderLeft		= 1;
webfxMenuDefaultBorderRight		= 1;
webfxMenuDefaultBorderTop		= 1;
webfxMenuDefaultBorderBottom	= 1;
webfxMenuDefaultPaddingLeft		= 1;
webfxMenuDefaultPaddingRight	= 1;
webfxMenuDefaultPaddingTop		= 1;
webfxMenuDefaultPaddingBottom	= 1;

webfxMenuDefaultShadowLeft		= 0;
webfxMenuDefaultShadowRight		= ie && !ie50 && /win32/i.test(navigator.platform) ? 4 :0;
webfxMenuDefaultShadowTop		= 0;
webfxMenuDefaultShadowBottom	= ie && !ie50 && /win32/i.test(navigator.platform) ? 4 : 0;

webfxMenuItemDefaultHeight		= 18;
webfxMenuItemDefaultText		= "Untitled";
webfxMenuItemDefaultHref		= "javascript:void(0)";

webfxMenuSeparatorDefaultHeight	= 6;

webfxMenuDefaultEmptyText		= "Empty";

webfxMenuDefaultUseAutoPosition	= nn6 ? false : true;

// other global constants
webfxMenuImagePath				= "";

webfxMenuUseHover				= opera ? true : false;
webfxMenuHideTime				= 500;
webfxMenuShowTime				= 200;

var webFXMenuHandler = {
	idCounter		:	0,
	idPrefix		:	"webfx-menu-object-",
	all				:	{},
	getId			:	function () { return this.idPrefix + this.idCounter++; },
	overMenuItem	:	function (oItem) {
		if (this.showTimeout != null)
			window.clearTimeout(this.showTimeout);
		if (this.hideTimeout != null)
			window.clearTimeout(this.hideTimeout);
		var jsItem = this.all[oItem.id];
		if (webfxMenuShowTime <= 0)
			this._over(jsItem);
		else
			//this.showTimeout = window.setTimeout(function () { webFXMenuHandler._over(jsItem) ; }, webfxMenuShowTime);
			// I hate IE5.0 because the piece of shit crashes when using setTimeout with a function object
			this.showTimeout = window.setTimeout("webFXMenuHandler._over(webFXMenuHandler.all['" + jsItem.id + "'])", webfxMenuShowTime);
	},
	outMenuItem	:	function (oItem) {
		if (this.showTimeout != null)
			window.clearTimeout(this.showTimeout);
		if (this.hideTimeout != null)
			window.clearTimeout(this.hideTimeout);
		var jsItem = this.all[oItem.id];
		if (webfxMenuHideTime <= 0)
			this._out(jsItem);
		else
			//this.hideTimeout = window.setTimeout(function () { webFXMenuHandler._out(jsItem) ; }, webfxMenuHideTime);
			this.hideTimeout = window.setTimeout("webFXMenuHandler._out(webFXMenuHandler.all['" + jsItem.id + "'])", webfxMenuHideTime);
	},
	blurMenu		:	function (oMenuItem) {
		window.setTimeout("webFXMenuHandler.all[\"" + oMenuItem.id + "\"].subMenu.hide();", webfxMenuHideTime);
	},
	_over	:	function (jsItem) {
		if (jsItem.subMenu) {
			jsItem.parentMenu.hideAllSubs();
			jsItem.subMenu.show();
		}
		else
			jsItem.parentMenu.hideAllSubs();
	},
	_out	:	function (jsItem) {
		// find top most menu
		var root = jsItem;
		var m;
		if (root instanceof WebFXMenuButton)
			m = root.subMenu;
		else {
			m = jsItem.parentMenu;
			while (m.parentMenu != null && !(m.parentMenu instanceof WebFXMenuBar))
				m = m.parentMenu;
		}
		if (m != null)
			m.hide();
	},
	hideMenu	:	function (menu) {
		if (this.showTimeout != null)
			window.clearTimeout(this.showTimeout);
		if (this.hideTimeout != null)
			window.clearTimeout(this.hideTimeout);

		this.hideTimeout = window.setTimeout("webFXMenuHandler.all['" + menu.id + "'].hide()", webfxMenuHideTime);
	},
	showMenu	:	function (menu, src, dir) {
		if (this.showTimeout != null)
			window.clearTimeout(this.showTimeout);
		if (this.hideTimeout != null)
			window.clearTimeout(this.hideTimeout);
		if (arguments.length < 3)
			dir = "vertical";

		menu.show(src, dir);
	}
};

function WebFXMenu() {
	this._menuItems	= [];
	this._subMenus	= [];
	this.id			= webFXMenuHandler.getId();
	this.top		= 0;
	this.left		= 0;
	this.shown		= false;
	this.parentMenu	= null;
	webFXMenuHandler.all[this.id] = this;
}

WebFXMenu.prototype.width			= webfxMenuDefaultWidth;
WebFXMenu.prototype.emptyText		= webfxMenuDefaultEmptyText;
WebFXMenu.prototype.useAutoPosition	= webfxMenuDefaultUseAutoPosition;

WebFXMenu.prototype.borderLeft		= webfxMenuDefaultBorderLeft;
WebFXMenu.prototype.borderRight		= webfxMenuDefaultBorderRight;
WebFXMenu.prototype.borderTop		= webfxMenuDefaultBorderTop;
WebFXMenu.prototype.borderBottom	= webfxMenuDefaultBorderBottom;

WebFXMenu.prototype.paddingLeft		= webfxMenuDefaultPaddingLeft;
WebFXMenu.prototype.paddingRight	= webfxMenuDefaultPaddingRight;
WebFXMenu.prototype.paddingTop		= webfxMenuDefaultPaddingTop;
WebFXMenu.prototype.paddingBottom	= webfxMenuDefaultPaddingBottom;

WebFXMenu.prototype.shadowLeft		= webfxMenuDefaultShadowLeft;
WebFXMenu.prototype.shadowRight		= webfxMenuDefaultShadowRight;
WebFXMenu.prototype.shadowTop		= webfxMenuDefaultShadowTop;
WebFXMenu.prototype.shadowBottom	= webfxMenuDefaultShadowBottom;

WebFXMenu.prototype.add = function (menuItem) {
	this._menuItems[this._menuItems.length] = menuItem;
	if (menuItem.subMenu) {
		this._subMenus[this._subMenus.length] = menuItem.subMenu;
		menuItem.subMenu.parentMenu = this;
	}

	menuItem.parentMenu = this;
};

WebFXMenu.prototype.show = function (relObj, sDir) {
	if (this.useAutoPosition)
		this.position(relObj, sDir);

	var divElement = document.getElementById(this.id);
	divElement.style.left = opera ? this.left : this.left + "px";
	divElement.style.top = opera ? this.top : this.top + "px";
	divElement.style.visibility = "visible";
	this.shown = true;
	if (this.parentMenu)
		this.parentMenu.show();
};

WebFXMenu.prototype.hide = function () {
	this.hideAllSubs();
	var divElement = document.getElementById(this.id);
	divElement.style.visibility = "hidden";
	this.shown = false;
};

WebFXMenu.prototype.hideAllSubs = function () {
	for (var i = 0; i < this._subMenus.length; i++) {
		if (this._subMenus[i].shown)
			this._subMenus[i].hide();
	}
};
WebFXMenu.prototype.toString = function () {
	var top = this.top + this.borderTop + this.paddingTop;
	var str = "<div id='" + this.id + "' class='webfx-menu' style='" +
	"width:" + (!ieBox  ?
		this.width - this.borderLeft - this.paddingLeft - this.borderRight - this.paddingRight  :
		this.width) + "px;" +
	(this.useAutoPosition ?
		"left:" + this.left + "px;" + "top:" + this.top + "px;" :
		"") +
	(ie50 ? "filter: none;" : "") +
	"'>";

	if (this._menuItems.length == 0) {
		str +=	"<span class='webfx-menu-empty'>" + this.emptyText + "</span>";
	}
	else {
		// loop through all menuItems
		for (var i = 0; i < this._menuItems.length; i++) {
			var mi = this._menuItems[i];
			str += mi;
			if (!this.useAutoPosition) {
				if (mi.subMenu && !mi.subMenu.useAutoPosition)
					mi.subMenu.top = top - mi.subMenu.borderTop - mi.subMenu.paddingTop;
				top += mi.height;
			}
		}

	}

	str += "</div>";

	for (var i = 0; i < this._subMenus.length; i++) {
		this._subMenus[i].left = this.left + this.width - this._subMenus[i].borderLeft;
		str += this._subMenus[i];
	}

	return str;
};
// WebFXMenu.prototype.position defined later
function WebFXMenuItem(sText, sHref, sToolTip, oSubMenu) {
	this.text = sText || webfxMenuItemDefaultText;
	this.href = (sHref == null || sHref == "") ? webfxMenuItemDefaultHref : sHref;
	this.subMenu = oSubMenu;
	if (oSubMenu)
		oSubMenu.parentMenuItem = this;
	this.toolTip = sToolTip;
	this.id = webFXMenuHandler.getId();
	webFXMenuHandler.all[this.id] = this;
};
WebFXMenuItem.prototype.height = webfxMenuItemDefaultHeight;
WebFXMenuItem.prototype.toString = function () {
	return	"<a" +
			" id='" + this.id + "'" +
			" href=\"" + this.href + "\"" +
			(this.target ? " target=\"" + this.target + "\"" : "") +
			(this.toolTip ? " title=\"" + this.toolTip + "\"" : "") +
			" onmouseover='webFXMenuHandler.overMenuItem(this)'" +
			(webfxMenuUseHover ? " onmouseout='webFXMenuHandler.outMenuItem(this)'" : "") +
			(this.subMenu ? " unselectable='on' tabindex='-1'" : "") +
			">" +
			(this.subMenu ? "<img class='arrow' src=\"" + webfxMenuImagePath + "arrow.right.png\">" : "") +
			this.text +
			"</a>";
};


function WebFXMenuSeparator() {
	this.id = webFXMenuHandler.getId();
	webFXMenuHandler.all[this.id] = this;
};
WebFXMenuSeparator.prototype.height = webfxMenuSeparatorDefaultHeight;
WebFXMenuSeparator.prototype.toString = function () {
	return	"<div" +
			" id='" + this.id + "'" +
			(webfxMenuUseHover ?
			" onmouseover='webFXMenuHandler.overMenuItem(this)'" +
			" onmouseout='webFXMenuHandler.outMenuItem(this)'"
			:
			"") +
			"></div>"
};

function WebFXMenuBar() {
	this._parentConstructor = WebFXMenu;
	this._parentConstructor();
}
WebFXMenuBar.prototype = new WebFXMenu;
WebFXMenuBar.prototype.toString = function () {
	var str = "<div id='" + this.id + "' class='webfx-menu-bar'>";

	// loop through all menuButtons
	for (var i = 0; i < this._menuItems.length; i++)
		str += this._menuItems[i];

	str += "</div>";

	for (var i = 0; i < this._subMenus.length; i++)
		str += this._subMenus[i];

	return str;
};

function WebFXMenuButton(sText, sHref, sToolTip, oSubMenu) {
	this._parentConstructor = WebFXMenuItem;
	this._parentConstructor(sText, sHref, sToolTip, oSubMenu);
}
WebFXMenuButton.prototype = new WebFXMenuItem;
WebFXMenuButton.prototype.toString = function () {
	return	"<a" +
			" id='" + this.id + "'" +
			" href=\"" + this.href + "\"" +
			(this.target ? " target=\"" + this.target + "\"" : "") +
			(this.toolTip ? " title=\"" + this.toolTip + "\"" : "") +
			(webfxMenuUseHover ?
				(" onmouseover='webFXMenuHandler.overMenuItem(this)'" +
				" onmouseout='webFXMenuHandler.outMenuItem(this)'") :
				(
					" onfocus='webFXMenuHandler.overMenuItem(this)'" +
					(this.subMenu ?
						" onblur='webFXMenuHandler.blurMenu(this)'" :
						""
					)
				)) +
			">" +
			this.text +
			(this.subMenu ? " <img class='arrow' src=\"" + webfxMenuImagePath + "arrow.down.png\" align='absmiddle'>" : "") +
			"</a>";
};


/* Position functions */

function getInnerLeft(el) {
	if (el == null) return 0;
	if (ieBox && el == document.body || !ieBox && el == document.documentElement) return 0;
	return getLeft(el) + getBorderLeft(el);
}

function getLeft(el) {
	if (el == null) return 0;
	return el.offsetLeft + getInnerLeft(el.offsetParent);
}

function getInnerTop(el) {
	if (el == null) return 0;
	if (ieBox && el == document.body || !ieBox && el == document.documentElement) return 0;
	return getTop(el) + getBorderTop(el);
}

function getTop(el) {
	if (el == null) return 0;
	return el.offsetTop + getInnerTop(el.offsetParent);
}

function getBorderLeft(el) {
	return ie ?
		el.clientLeft :
		parseInt(window.getComputedStyle(el, null).getPropertyValue("border-left-width"));
}

function getBorderTop(el) {
	return ie ?
		el.clientTop :
		parseInt(window.getComputedStyle(el, null).getPropertyValue("border-top-width"));
}

function opera_getLeft(el) {
	if (el == null) return 0;
	return el.offsetLeft + opera_getLeft(el.offsetParent);
}

function opera_getTop(el) {
	if (el == null) return 0;
	return el.offsetTop + opera_getTop(el.offsetParent);
}

function getOuterRect(el) {
	return {
		left:	(opera ? opera_getLeft(el) : getLeft(el)),
		top:	(opera ? opera_getTop(el) : getTop(el)),
		width:	el.offsetWidth,
		height:	el.offsetHeight
	};
}

// mozilla bug! scrollbars not included in innerWidth/height
function getDocumentRect(el) {
	return {
		left:	0,
		top:	0,
		width:	(ie ?
					(ieBox ? document.body.clientWidth : document.documentElement.clientWidth) :
					window.innerWidth
				),
		height:	(ie ?
					(ieBox ? document.body.clientHeight : document.documentElement.clientHeight) :
					window.innerHeight
				)
	};
}

function getScrollPos(el) {
	return {
		left:	(ie ?
					(ieBox ? document.body.scrollLeft : document.documentElement.scrollLeft) :
					window.pageXOffset
				),
		top:	(ie ?
					(ieBox ? document.body.scrollTop : document.documentElement.scrollTop) :
					window.pageYOffset
				)
	};
}

/* end position functions */

WebFXMenu.prototype.position = function (relEl, sDir) {
	var dir = sDir;
	// find parent item rectangle, piRect
	var piRect;
	if (!relEl) {
		var pi = this.parentMenuItem;
		if (!this.parentMenuItem)
			return;

		relEl = document.getElementById(pi.id);
		if (dir == null)
			dir = pi instanceof WebFXMenuButton ? "vertical" : "horizontal";

		piRect = getOuterRect(relEl);
	}
	else if (relEl.left != null && relEl.top != null && relEl.width != null && relEl.height != null) {	// got a rect
		piRect = relEl;
	}
	else
		piRect = getOuterRect(relEl);

	var menuEl = document.getElementById(this.id);
	var menuRect = getOuterRect(menuEl);
	var docRect = getDocumentRect();
	var scrollPos = getScrollPos();
	var pMenu = this.parentMenu;

	if (dir == "vertical") {
		if (piRect.left + menuRect.width - scrollPos.left <= docRect.width)
			this.left = piRect.left;
		else if (docRect.width >= menuRect.width)
			this.left = docRect.width + scrollPos.left - menuRect.width;
		else
			this.left = scrollPos.left;

		if (piRect.top + piRect.height + menuRect.height <= docRect.height + scrollPos.top)
			this.top = piRect.top + piRect.height;
		else if (piRect.top - menuRect.height >= scrollPos.top)
			this.top = piRect.top - menuRect.height;
		else if (docRect.height >= menuRect.height)
			this.top = docRect.height + scrollPos.top - menuRect.height;
		else
			this.top = scrollPos.top;
	}
	else {
		if (piRect.top + menuRect.height - this.borderTop - this.paddingTop <= docRect.height + scrollPos.top)
			this.top = piRect.top - this.borderTop - this.paddingTop;
		else if (piRect.top + piRect.height - menuRect.height + this.borderTop + this.paddingTop >= 0)
			this.top = piRect.top + piRect.height - menuRect.height + this.borderBottom + this.paddingBottom + this.shadowBottom;
		else if (docRect.height >= menuRect.height)
			this.top = docRect.height + scrollPos.top - menuRect.height;
		else
			this.top = scrollPos.top;

		var pMenuPaddingLeft = pMenu ? pMenu.paddingLeft : 0;
		var pMenuBorderLeft = pMenu ? pMenu.borderLeft : 0;
		var pMenuPaddingRight = pMenu ? pMenu.paddingRight : 0;
		var pMenuBorderRight = pMenu ? pMenu.borderRight : 0;

		if (piRect.left + piRect.width + menuRect.width + pMenuPaddingRight +
			pMenuBorderRight - this.borderLeft + this.shadowRight <= docRect.width + scrollPos.left)
			this.left = piRect.left + piRect.width + pMenuPaddingRight + pMenuBorderRight - this.borderLeft;
		else if (piRect.left - menuRect.width - pMenuPaddingLeft - pMenuBorderLeft + this.borderRight + this.shadowRight >= 0)
			this.left = piRect.left - menuRect.width - pMenuPaddingLeft - pMenuBorderLeft + this.borderRight + this.shadowRight;
		else if (docRect.width >= menuRect.width)
			this.left = docRect.width  + scrollPos.left - menuRect.width;
		else
			this.left = scrollPos.left;
	}
};