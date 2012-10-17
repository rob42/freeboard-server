/*
See http://www.howtocreate.co.uk/perfectPopups.html and http://www.howtocreate.co.uk/jslibs/termsOfUse.html
for details and terms of use.
To call this script, use something like (the number is a delay before it closes or 0 for no timed closing -
the true/false says if the window should close when they switch to another window):
<script type="text/javascript"><!--
//you can style this, but don't try to text-align it to the right, it will break the resizing effect
//keep it narrow, if it is wider than the image, the window will wrap to this width
//the makeright class tells the script to automatically align it to the right
var extraHTML = '<br><a href="javascript:window.close()" style="text-decoration:none;color:#777;background-color:#bbb;font-weight:bold;border-left:2px solid #000;outline:none !important" class="makeright">Close<\/a>';
//--></script>
<a href="me.jpg" onclick="return popImageExtra(this.href,'Site author',true,3000,extraHTML);">link</a>
*/

//really not important (the first two should be small for Opera's sake)
PositionX = 10;
PositionY = 10;
defaultWidth  = 600;
defaultHeight = 400;

//don't touch (except to modify the window contents)
function popImageExtra(imageURL,imageTitle,AutoClose,oTimeClose,extraHTML){
	var imgWin = window.open('','_blank','scrollbars=yes,resizable=1,width='+defaultWidth+',height='+defaultHeight+',left='+PositionX+',top='+PositionY);
	if( !imgWin ) { return true; } //popup blockers should not cause errors
	imgWin.document.write('<html><head><title>'+imageTitle+'<\/title><script type="text\/javascript">\n'+
		'function getRefToDivMod( divID, oDoc ) {\n'+
			'if( !oDoc ) { oDoc = document; }\n'+
			'if( document.layers ) {\n'+
			'if( oDoc.layers[divID] ) { return oDoc.layers[divID]; } else {\n'+
			'for( var x = 0, y; !y && x < oDoc.layers.length; x++ ) {\n'+
			'y = getRefToDivNest(divID,oDoc.layers[x].document); }\n'+
			'return y; } }\n'+
			'if( document.getElementById ) { return oDoc.getElementById(divID); }\n'+
			'if( document.all ) { return oDoc.all[divID]; }\n'+
			'return document[divID];\n'+
		'}\n'+
		'function resizeWinTo() {\n'+
			'if( !document.images.length ) { document.images[0] = document.layers[0].images[0]; }'+
			'if( !document.images[0].height || window.doneAlready ) { return; }\n'+ //in case images are disabled
			'var oH = getRefToDivMod( \'myID\' ); if( !oH ) { return false; }\n'+
			'var oW = oH.clip ? oH.clip.width : oH.offsetWidth;\n'+
			'var oH = oH.clip ? oH.clip.height : oH.offsetHeight; if( !oH ) { return false; }\n'+
			'if( !oH || window.doneAlready ) { return; }\n'+ //in case images are disabled
			'window.doneAlready = true;\n'+ //for Safari and Opera

			'var mH = screen.availHeight-200, mW = screen.availWidth-200;\n'+
			'if( oH > mH || oW > mW ) {\n'+

			'document.images[0].fullH = oH;\n'+
			'document.images[0].fullW = oW;\n'+

			'var hDif = oH - document.images[0].height;\n'+
			'var wDif = oW - document.images[0].width;\n'+
			'mH = mH - hDif; mW = mW - wDif;\n'+
			'mH = mH \/ document.images[0].height;\n'+
			'mW = mW \/ document.images[0].width;\n'+
			'var zoomFactor = ( mH < mW ) ? mH : mW;\n'+
			'oH = Math.floor( document.images[0].height * zoomFactor );\n'+
			'oW = Math.floor( document.images[0].width * zoomFactor );\n'+

			'document.images[0].style.cursor = \'crosshair\';\n'+
			'document.images[0].title = \'Click to resize image\';\n'+
			'document.images[0].oldHeight = document.images[0].height;\n'+
			'document.images[0].oldWidth = document.images[0].width;\n'+
			'document.images[0].newHeight = oH;\n'+
			'document.images[0].newWidth = oW;\n'+
			'document.images[0].onclick = function () { '+
			'if( this.oldHeight == this.height ) { '+
			'this.height = this.newHeight; this.width = this.newWidth; '+
			'setTimeout(\'window.resizeTo(\'+this.oW+\',\'+this.oH+\'); '+
			'if( !window.opera ) { '+
			'window.moveTo(\'+Math.round((this.scW-this.oW)/2)+\','+
			'\'+Math.round((this.scH-this.oH)/2)+\'); }\',1);\n'+
			'} else { this.height = this.oldHeight; this.width = this.oldWidth; '+
			'window.moveTo(0,0); window.resizeTo(this.scW,this.scH); '+
			'var myW = 0, myH = 0, d = x.document.documentElement, b = x.document.body;\n'+
			'if( x.innerWidth ) { myW = x.innerWidth; myH = x.innerHeight; }\n'+
			'else if( d && d.clientWidth ) { myW = d.clientWidth; myH = d.clientHeight; }\n'+
			'else if( b && b.clientWidth ) { myW = b.clientWidth; myH = b.clientHeight; }\n'+
			'if( window.opera && !document.childNodes ) { myW += 16; }\n'+
			'if( this.fullH < myH ) { var rs = this.fullH - myH; window.resizeBy(0,rs); '+
			'if( !window.opera ) { window.moveBy(0,Math.round(rs/-2)); } }'+
			'if( this.fullW < myW ) { var rs = this.fullW - myW; window.resizeBy(rs,0); '+
			'if( !window.opera ) { window.moveBy(Math.round(rs/-2),0); } }'+
			'} };\n'+

			'document.images[0].height = oH;\n'+
			'document.images[0].width = oW;\n'+
			'oH += hDif; oW += wDif;\n'+
			'}\n'+

			'if(document.getElementsByTagName) {\n'+
				'for( var l = document.getElementsByTagName(\'a\'), x = 0; l[x]; x++ ) {\n'+
					'if(l[x].className==\'makeright\'&&!l[x].style.position){\n'+
						'l[x].style.position=\'relative\';\n'+
						'l[x].style.left=(document.images[0].width-(l[x].offsetWidth+l[x].offsetLeft))+\'px\';\n'+
			'}}}\n'+
			'var x = window; x.resizeTo( oW + 200, oH + 200 );\n'+
			'var myW = 0, myH = 0, d = x.document.documentElement, b = x.document.body;\n'+
			'if( x.innerWidth ) { myW = x.innerWidth; myH = x.innerHeight; }\n'+
			'else if( d && d.clientWidth ) { myW = d.clientWidth; myH = d.clientHeight; }\n'+
			'else if( b && b.clientWidth ) { myW = b.clientWidth; myH = b.clientHeight; }\n'+
			'if( window.opera && !document.childNodes ) { myW += 16; }\n'+
			'x.resizeTo( oW = oW + ( ( oW + 200 ) - myW ), oH = oH + ( (oH + 200 ) - myH ) );\n'+
			'var scW = screen.availWidth ? screen.availWidth : screen.width;\n'+
			'var scH = screen.availHeight ? screen.availHeight : screen.height;\n'+

			'document.images[0].oH = oH;\n'+
			'document.images[0].oW = oW;\n'+
			'document.images[0].scH = scH;\n'+
			'document.images[0].scW = scW;\n'+

			'if( !window.opera ) { x.moveTo(Math.round((scW-oW)/2),Math.round((scH-oH)/2)); }\n'+
			(oTimeClose?('window.setTimeout(\'window.close()\','+oTimeClose+');\n'):'')+
		'}\n'+
		'<\/script>'+
		'<\/head><body onload="resizeWinTo();"'+(AutoClose?' onblur="self.close();"':'')+'>'+
		(document.layers?('<layer left="0" top="0" id="myID">'):('<div style="position:absolute;left:0px;top:0px;display:table;" id="myID">'))+
		'<img src="'+imageURL+'" alt="Loading image ..." title="" onload="resizeWinTo();">'+
		(extraHTML?extraHTML:'')+(document.layers?'<\/layer>':'<\/div>')+'<\/body><\/html>');
	imgWin.document.close();
	if( imgWin.focus ) { imgWin.focus(); }
	return false;
}