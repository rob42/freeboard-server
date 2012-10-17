// alib common object
function Alib() {};

//------------------------------------
//	retrieve target for event
//------------------------------------

Alib.prototype.getTarget = function(e) {
	var target;
	if (e.target) target = e.target;
	else if (e.srcElement) target = e.srcElement;
	if (target.nodeType == 3) // defeat Safari bug
		target = target.parentNode;

	return target;
}

//------------------------------------
//	retrieve related target for event
//------------------------------------

Alib.prototype.getRelatedTarget = function(e) {
	return (e.relatedTarget) ? e.relatedTarget : e.toElement;
}

Alib.prototype.addListener = function(obj, event, handler) {
	if (obj.addEventListener) {
		obj.addEventListener(event, handler, false);
	} else if (obj.attachEvent) {
		obj.attachEvent('on' + event, handler);
	} else {
		window.status = 'Please upgrade to more recent browser like ie6.0 or firefox.';
	}
}

/* warning, ie is choking on addStyleSheet */
Alib.prototype.addStyleSheet = function(title, href) {
	/*
	var head = document.getElementsByTagName('head')[0];
	var link = document.createElement('link')
	link.rel = 'stylesheet';
	link.type = 'text/css';
	link.title = '';
	link.href = href;

	head.appendChild(link);
	*/
	document.write('<link rel="stylesheet" href="'+href+'" type="text/css">');
}

var alib = new Alib();