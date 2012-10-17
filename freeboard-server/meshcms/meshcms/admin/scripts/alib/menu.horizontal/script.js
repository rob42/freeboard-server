//------------------------------------
// Created on 8.3.2006
// (c) Jan Ptacek
//------------------------------------

// alib Hmenu object
function Hmenu() {};

//------------------------------------
//
//------------------------------------

Hmenu.prototype.addBehavior = function() {
	// get menu roots
	var node;
	var i = 0;
	var k = 1;
	while((node = document.getElementsByTagName('UL').item(i++))) {
		if (node.className == 'hmenu') {
			node.className = 'hmenuALIB';
			for (var j = 0; j < node.childNodes.length;j++) {
				if (node.childNodes[j].tagName == 'LI')
					Hmenu.prototype.initMenuItem(node.childNodes[j], k++);
			}
		}
	}
}

Hmenu.prototype.initMenuItem = function(li, num) {
	// to emulate :first-child
	if (num == 1) {
		li.style.borderTopWidth = '1px';
		li.style.borderLeftWidth = '1px';
	}

	for (var i = 0; i < li.childNodes.length;i++) {
		if (li.childNodes[i].tagName == 'UL') {
			var nodeUL = li.childNodes[i];
			li.cachedUL = nodeUL;
			// 1 + n-level submenu
			if (li.parentNode.className != 'hmenuALIB') {
				// background arrow
				li.childNodes[0].className += ' hmenuALIBsuba';  // shrink anchor
				li.className += ' hmenuALIBsubli';               // apply image
//				li.style.backgroundRepeat = 'no-repeat';
//				li.style.backgroundImage = 'url(/meshcms3/meshcms/admin/scripts/alib/menu.horizontal/sub.gif)';
//				li.style.backgroundColor = 'white';
//				li.style.backgroundPosition = '99%';
			}
			alib.addListener(li, 'mouseover', Hmenu.prototype.showSubmenu);
			alib.addListener(li, 'mouseout',  Hmenu.prototype.hideSubmenu);

			var k = 1;
			for (var j = 0; j < nodeUL.childNodes.length;j++) {
				if (nodeUL.childNodes[j].tagName == 'LI')
					this.initMenuItem(nodeUL.childNodes[j], k++);
			}
		}
	}
}

Hmenu.prototype.showSubmenu = function(e) {
	if (!e) var e = window.event;
	var target = alib.getTarget(e);
	if (target.tagName == 'A')
		target = target.parentNode;
	if (target.cachedUL) {
		target.cachedUL.style.display = 'block';
	}
}

Hmenu.prototype.hideSubmenu = function(e) {
	if (!e) var e = window.event;
	var target = alib.getTarget(e);
	var related = alib.getRelatedTarget(e);
	if (!related || related.tagName == 'HTML')
		related = document.getElementsByTagName('body')[0];
	Hmenu.prototype.report('out '+target.tagName+'->'+related.tagName);
	Hmenu.prototype.report("\n");
	// maybe mouse left only into successor, handle
	if (target.tagName == 'A')
		target = target.parentNode;
	if (Hmenu.prototype.isAncestor(target, related))
		return;
	// it left into ancestor
	// find ul upon related node or body
	var commonUL = related;
	while (commonUL.tagName != 'UL' && commonUL.tagName != 'BODY')
		commonUL = commonUL.parentNode;
	// iterate up the tree from target till commonUL and close submenu
	Hmenu.prototype.report('coUL:'+commonUL.tagName+'::');
	while (target != commonUL && target.tagName != 'BODY') {
		Hmenu.prototype.report(target.tagName+'~');
		if (target.cachedUL)
			target.cachedUL.style.display = 'none';
		target = target.parentNode;
	}
	Hmenu.prototype.report("\n");
}

Hmenu.prototype.isAncestor = function(ancestor, child) {
	while (child != ancestor && child.nodeName != 'BODY')
		child = child.parentNode;
	return child == ancestor ? 1 : 0;
}

Hmenu.prototype.report = function () {};

/* INITS */

var hmenu = new Hmenu();

// register onload init
alib.addListener(window, 'load', hmenu.addBehavior);