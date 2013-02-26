 /*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 *
 *  FreeBoard is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  FreeBoard is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with FreeBoard.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Largely copied from http://cometd.org sample code !
 */
   

    function FreeboardComet()
    {
        var _self = this;
        var _connected = false;
        var _disconnecting;
        var _subscription;
        

        this.join = function()
        {
            _disconnecting = false;
            $.cometd.websocketEnabled = false;
            $.cometd.configure({
                url: 'http://'+window.location.host+':8082/cometd',
                logLevel: 'info'
            });
            $.cometd.handshake();
        };

        this.leave = function()
        {
           
            $.cometd.disconnect();
            _disconnecting = true;
        };

 

        this.receive = function(m)
        {
        	//console.log(m.data);
        	if(m.data.trim().startsWith('$'))return;		
    		var mArray=m.data.trim().split(",");
    		jQuery.each(wsList, function(i, obj) {
    		      obj.onmessage(mArray);
    		  });
    		m=null;

        };

      

        function _unsubscribe()
        {
            if (_subscription)
            {
                $.cometd.unsubscribe(_subscription);
            }
            _subscription = null;
          
        }

        function _subscribe()
        {
            _subscription = $.cometd.subscribe('/freeboard/json', _self.receive);
            //console.log('Subscribed to /freeboard/test');
           
        }

       

        function _connectionEstablished()
        {
            // connection establish (maybe not for first time)
           
          
        	//console.log('Connection to Server Opened');
            _subscribe();
        }

        function _connectionBroken()
        {
            console.log('Connection to Server Broken');
        }

        function _connectionClosed()
        {
           
            //console.log('Connection to Server Closed');
        }

        function _metaConnect(message)
        {
            if (_disconnecting)
            {
                _connected = false;
                _connectionClosed();
            }
            else
            {
                var wasConnected = _connected;
                _connected = message.successful === true;
                if (!wasConnected && _connected)
                {
                    _connectionEstablished();
                }
                else if (wasConnected && !_connected)
                {
                    _connectionBroken();
                }
            }
        }

        function _metaHandshake(message)
        {
            if (message.successful)
            {
                _connectionInitialized();
            }
        }

        $.cometd.addListener('/meta/handshake', _metaHandshake);
        $.cometd.addListener('/meta/connect', _metaConnect);
        this.join();
        
        
        $(window).unload(function()
        {
 
                $.cometd.disconnect();
  
        });
    }
