//------------------------------------
//
//------------------------------------

// alib Vmenu object
function Vmenu() {};

//------------------------------------
//
//------------------------------------

Vmenu.prototype.addBehavior = function() {
	// get menu roots
	var node;
	var i = 0;
	var k = 1;
	while((node = document.getElementsByTagName('UL').item(i++))) {
		if (node.className == 'vmenu') {
			node.className = 'vmenuALIB';
			for (var i = 0; i < node.childNodes.length;i++) {
				if (node.childNodes[i].tagName == 'LI') {
					Vmenu.prototype.initMenuItem(node.childNodes[i], k++);
					// compute the height of whole block
					// cos we float top-level li`s we loose the height 
					// beware, we suppose that all are one-liners :(
					node.style.height = k*1.1+'em';
				}
			}
		}
	}
}

Vmenu.prototype.initMenuItem = function(li, num) {
	// to emulate :first-child
	if (num == 1) {
		li.style.borderTopWidth = '1px';
	}
	//li.style.backgroundColor = 'red';
	for (var i = 0; i < li.childNodes.length;i++) {
		if (li.childNodes[i].tagName == 'UL') {
			var nodeUL = li.childNodes[i];
			li.cachedUL = nodeUL;
			// 1 + n-level submenu
			if (li.parentNode.className != 'vmenuALIB') {
				// ul position -
				// TODO - i am unable to find its width
				// anyway it is a bad idea, cos it changes with zoom
				// nodeUL.style.left = li.style.pixelWidth;
				// background arrow
//				li.style.backgroundRepeat = 'no-repeat';
//				li.style.backgroundImage = 'url("menu.vertical/sub.gif")';
//				li.style.backgroundColor = 'white';
//				li.style.backgroundPosition = '99%';
				li.className += ' vmenuALIBsubli';               // apply image
			}
			alib.addListener(li, 'mouseover', Vmenu.prototype.showSubmenu);
			alib.addListener(li, 'mouseout',  Vmenu.prototype.hideSubmenu);
			var k = 1;
			for (var j = 0; j < nodeUL.childNodes.length;j++) {
				if (nodeUL.childNodes[j].tagName == 'LI')
					Vmenu.prototype.initMenuItem(nodeUL.childNodes[j], k++);
			}
		}
	}
}

Vmenu.prototype.showSubmenu = function(e) {
	if (!e) var e = window.event;
	var target = alib.getTarget(e);
	if (target.tagName == 'A')
		target = target.parentNode;
	if (target.cachedUL)
		target.cachedUL.style.display = 'block';
}

Vmenu.prototype.hideSubmenu = function(e) {
	if (!e) var e = window.event;
	var target = alib.getTarget(e);
	var related = alib.getRelatedTarget(e);
	if (!related || related.tagName == 'HTML')
		related = document.getElementsByTagName('body')[0];
	Vmenu.prototype.report('out '+target.tagName+'->'+related.tagName);
	Vmenu.prototype.report("\n");
	// maybe mouse left only into successor, handle
	if (target.tagName == 'A')
		target = target.parentNode;
	if (Vmenu.prototype.isAncestor(target, related))
		return;
	// it left into ancestor
	// find ul upon related node or body
	var commonUL = related;
	while (commonUL.tagName != 'UL' && commonUL.tagName != 'BODY')
		commonUL = commonUL.parentNode;
	// iterate up the tree from target till commonUL and close submenu
	Vmenu.prototype.report('coUL:'+commonUL.tagName+'::');
	while (target != commonUL && target.tagName != 'BODY') {
		Vmenu.prototype.report(target.tagName+'~');
		if (target.cachedUL)
			target.cachedUL.style.display = 'none';
		target = target.parentNode;
	}
	Vmenu.prototype.report("\n");
}


Vmenu.prototype.debug = 0;

Vmenu.prototype.report = function(txt) {
	var report = document.getElementById('report');
	if (report && Vmenu.prototype.debug) {
//		report.value += txt;
	}
}
Vmenu.prototype.reportln = function(txt) {
	var report = document.getElementById('report');
	if (report && debug) {
//		report.value += txt + "\n";
	}
}

Vmenu.prototype.isAncestor = function(ancestor, child) {
	while (child != ancestor && child.nodeName != 'BODY')
		child = child.parentNode;
	return child == ancestor ? 1 : 0;
}


/* INITS */

var vmenu = new Vmenu();

// register onload init
alib.addListener(window, 'load', vmenu.addBehavior);
