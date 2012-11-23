var _ws;
var wsList = [];

function initSocket(){
	//make a web socket
	if(this._ws == null) {
		var location = "ws://"+window.location.hostname+":9090/navData";
		//alert(location);
		
		this._ws = new WebSocket(location);
		this._ws.onopen = function() {
		};
		this._ws.onmessage = function(m) {
			//iterate the array and process each
			jQuery.each(wsList, function(i, obj) {
			      obj.onmessage(m);
			    });
		};
		this._ws.onclose = function() {
			this._ws = null;
		};
	}
}

