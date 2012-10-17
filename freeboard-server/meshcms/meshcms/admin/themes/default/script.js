/* begin Page */

/* Created by Artisteer v3.0.0.35180 */



(function() {
    // fix ie blinking
    var m = document.uniqueID && document.compatMode && !window.XMLHttpRequest && document.execCommand;
    try { if (!!m) { m('BackgroundImageCache', false, true); } }
    catch (oh) { };
    // css helper
    var u = navigator.userAgent.toLowerCase();
    var is = function(t) { return (u.indexOf(t) != -1) };
    jQuery('html').addClass([(!(/opera|webtv/i.test(u)) && /msie (\d)/.test(u)) ? ('ie ie' + RegExp.$1)
    : is('firefox/2') ? 'gecko firefox2'
    : is('firefox/3') ? 'gecko firefox3'
    : is('gecko/') ? 'gecko'
    : is('chrome/') ? 'chrome'
    : is('opera/9') ? 'opera opera9' : /opera (\d)/.test(u) ? 'opera opera' + RegExp.$1
    : is('konqueror') ? 'konqueror'
    : is('applewebkit/') ? 'webkit safari'
    : is('mozilla/') ? 'gecko' : '',
    (is('x11') || is('linux')) ? ' linux'
    : is('mac') ? ' mac'
    : is('win') ? ' win' : ''
    ].join(' '));
})();

var _artStyleUrlCached = null;
function artGetStyleUrl() {
    if (null == _artStyleUrlCached) {
        var ns;
        _artStyleUrlCached = '';
        ns = jQuery('link');
        for (var i = 0; i < ns.length; i++) {
            var l = ns[i].href;
            if (l && /style\.ie6\.css(\?.*)?$/.test(l))
                return _artStyleUrlCached = l.replace(/style\.ie6\.css(\?.*)?$/, '');
        }
        ns = jQuery('style');
        for (var i = 0; i < ns.length; i++) {
            var matches = new RegExp('import\\s+"([^"]+\\/)style\\.ie6\\.css"').exec(ns[i].html());
            if (null != matches && matches.length > 0)
                return _artStyleUrlCached = matches[1];
        }
    }
    return _artStyleUrlCached;
}

function artFixPNG(element) {
    if (jQuery.browser.msie && parseInt(jQuery.browser.version) < 7) {
		var src;
		if (element.tagName == 'IMG') {
			if (/\.png$/.test(element.src)) {
				src = element.src;
				element.src = artGetStyleUrl() + 'images/spacer.gif';
			}
		}
		else {
			src = element.currentStyle.backgroundImage.match(/url\("(.+\.png)"\)/i);
			if (src) {
				src = src[1];
				element.runtimeStyle.backgroundImage = 'none';
			}
		}
		if (src) element.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + src + "')";
	}
}

//jQuery(function() {
//    jQuery.each(jQuery('ul.art-menu>li:not(.art-menu-li-separator),ul.art-vmenu>li:not(.art-vmenu-separator)'), function (i, val) {
//        var l = jQuery(val); var s = l.children('span'); if (s.length == 0) return;
//        var t = l.find('span.t').last(); l.children('a').append(t.html(t.text()));
//        s.remove();
//    });
//});/* end Page */

/* begin Box, Sheet */

function artFluidSheetComputedWidth(percent, minval, maxval) {
    percent = parseInt(percent);
    var val = document.body.clientWidth / 100 * percent;
    return val < minval ? minval + 'px' : val > maxval ? maxval + 'px' : percent + '%';
}/* end Box, Sheet */

/* begin Layout */
jQuery(function () {
    if (!jQuery.browser.msie || parseInt(jQuery.browser.version) > 7) return;
    var c = jQuery('div.art-content');
    if (c.length !== 1) return;
    var s = c.parent().children('.art-layout-cell:not(.art-content)');
    jQuery(window).bind('resize', function () {
        var w = 0; c.css('width', "0");
        s.each(function () { w += this.clientWidth; });
        c.w = c.parent().width();c.css('width', c.w - w);
    }).trigger('resize');
    jQuery('div.art-content-layout-row').each(function () {
        this.c = jQuery(this).children('.art-layout-cell');
    }).bind('resize', function () {
        if (this.h == this.clientHeight) return;
        this.c.css('height', 'auto');
        this.h = this.clientHeight;
        this.c.css('height', this.h + 'px');
    }).trigger('resize');
});
/* end Layout */

/* begin Button */
function artButtonSetup(className) {
    jQuery.each(jQuery("a." + className + ", button." + className + ", input." + className), function(i, val) {
        var b = jQuery(val);
        if (!b.parent().hasClass('art-button-wrapper')) {
            if (!b.hasClass('art-button')) b.addClass('art-button');
            jQuery("<span class='art-button-wrapper'><span class='art-button-l'> </span><span class='art-button-r'> </span></span>").insertBefore(b).append(b);
            if (b.hasClass('active')) b.parent().addClass('active');
        }
        b.mouseover(function() { jQuery(this).parent().addClass("hover"); });
        b.mouseout(function() { var b = jQuery(this); b.parent().removeClass("hover"); if (!b.hasClass('active')) b.parent().removeClass('active'); });
        b.mousedown(function() { var b = jQuery(this); b.parent().removeClass("hover"); if (!b.hasClass('active')) b.parent().addClass('active'); });
        b.mouseup(function() { var b = jQuery(this); if (!b.hasClass('active')) b.parent().removeClass('active'); });
    });
}
jQuery(function() { artButtonSetup("art-button"); });

/* end Button */

/* begin VMenu */
jQuery(function() {
    jQuery('ul.art-vmenu li').not(':first').before('<li class="art-vsubmenu-separator"><span class="art-vsubmenu-separator-span"> </span></li>');
    jQuery('ul.art-vmenu > li.art-vsubmenu-separator').removeClass('art-vsubmenu-separator').addClass('art-vmenu-separator').children('span').removeClass('art-vsubmenu-separator-span').addClass('art-vmenu-separator-span');
    jQuery('ul.art-vmenu > li > ul > li.art-vsubmenu-separator:first-child').removeClass('art-vsubmenu-separator').addClass('art-vmenu-separator').addClass('art-vmenu-separator-first').children('span').removeClass('art-vsubmenu-separator-span').addClass('art-vmenu-separator-span');
});  /* end VMenu */

/* begin VMenuItem */
//jQuery(function() {
//    jQuery.each(jQuery('ul.art-vmenu'), function(idx, val) {
//        var m = jQuery(val);
//        m.find("ul, a").removeClass('active');
//        var links = m.find('a');
//        for (var i = 0; i < links.length; i++){
//            if (links[i].href == window.location.href) {
//                var a = jQuery(links[i]);
//                a.parent().children('ul').addClass('active');
//                a.parents('ul.art-vmenu ul').addClass('active');
//                a.parents('ul.art-vmenu li').children('a').addClass('active');
//                break; 
//            } 
//        }
//    });
//});
/* end VMenuItem */



jQuery(function () {
    artButtonSetup("button");
});
