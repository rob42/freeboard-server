<%--
 Copyright 2004-2009 Pierre Metras
 
 This file is part of MeshCMS.
 
 MeshCMS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 MeshCMS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with MeshCMS.  If not, see <http://www.gnu.org/licenses/>.

 Ajax Chat Module inspired by http://www.linuxuser.at/index.php?title=Most_Simple_Ajax_Chat_Ever
 Pierre Metras - 20060222
 See chat/server.jsp for the server side.
 
 Pierre Metras - 20070202
 * UTF-8 support for MeshCMS 3.0
 * XHTML
 * Multi-rooms (default to "public" shared room)
 * More responsive; added [Refresh] button
 * Change advanced parameters name to be compatible with other modules.
 * Localisation of messages
 TODO:
 * Set the chat room dimensions from Module parameters
 * Degrade gracefully with old browsers (No XMLHTTP object)
 * Support for naughty words and spam detection.
 * Whatever you want to add...
--%>

<%@ page import="java.io.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="com.opensymphony.module.sitemesh.parser.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module:
  - css = (name of a css class)
  - room = chat room identifier; else use public room shared by all pages and all sites
  - cols = number of columns of the chat list
  - rows = number of rows of the chat list
--%>

<%
  final String moduleCode = request.getParameter("modulecode");
  ModuleDescriptor md = null;

  if (moduleCode != null) {
    md = (ModuleDescriptor) request.getAttribute(moduleCode);
  }

  if (md == null) {
    if (!response.isCommitted()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    return;
  }

  final Locale locale = WebUtils.getPageLocale(pageContext);
  final ResourceBundle pageBundle = ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);

  final String cp = request.getContextPath();
  final String style = md.getFullCSSAttribute("css");

  // Get chat room identifier from advanced parameters
  final String roomId = md.getAdvancedParam("room", "public");
  final String c = md.getAdvancedParam("cols", "80");
  final String r = md.getAdvancedParam("rows", "10");
  int cols = 80;
  int rows = 10;
  int cline = 60;
  try {
    cols = Integer.parseInt(c);
  } catch (NumberFormatException nfe) {
    cols = 80;
  }
  try {
    rows = Integer.parseInt(r);
  } catch (NumberFormatException nfe) {
    rows = 10;
  }
  cline = (cols > 30) ? cols - 20 : 10;

  // Create chat room if it does not exist yet
  if (application.getAttribute("chatRoom_" + roomId) == null) {
    final List room = Collections.synchronizedList(new LinkedList());
	application.setAttribute("chatRoom_" + roomId, room);
  }
%>

<script type="text/javascript">
// <![CDATA[
  /* Chat room id */
var roomId = '<%= roomId %>';

  /* Timer id */
  var timeoutID;
  /* Refresh rate of chat room */
  var waitTime = 0;
  /* Add 2s to every display refresh rate when the user doesn't participate in discussion */
  var waitInc = 2000;

var debug = false;

/* Create and send a request with the given url, and call the callback function
  when the server answers. */
  function sendRequest(url, callback) {
    var request = false;
    if (window.XMLHttpRequest) {
      request = new XMLHttpRequest();
    } else if(window.ActiveXObject) {
      try {
              request = new ActiveXObject('Msxml2.XMLHTTP');
            } catch (e) {
              try {
                request = new ActiveXObject('Microsoft.XMLHTTP');
              } catch (e) {
              }
      }
    }
    if (!request) {
      writeStatus('Your browser does not support XMLHTTP object; please upgrade to use Ajax Chat');
      return;
    }

    request.onreadystatechange = function () {
      if (request.readyState == 4) {
        if (request.status == 200) {
          callback(request.responseText);
        writeDebug('');
        } else {
          writeStatus('HTTP error; Status=' + request.status);
        writeDebug('Headers=' + request.getAllResponseHeaders());
        }
      }
    };
    request.open('GET', url, true);
    request.send(null);
  }


  /* The more a user chats, the more often the display is refreshed. Users who don't
  participate in the discussion get slower and slower display refreshes to avoid
  to kill the server with the load. */
  function refreshChatRoom(reset) {
    if (reset) {
      waitTime = 2000;
    } else if (waitTime < 30000) {
      waitTime += waitInc;
    }
    clearTimeout(timeoutID);
    timeoutID = window.setTimeout('getChatRoom()', waitTime);
  writeStatus('<%= pageBundle.getString("chatRefreshIn") %> ' + (waitTime / 1000) + 's...');
  }


  /* Response acknowledge from server.jsp for a new message */
function msgReceived(content) {
  // Force refresh of the chat room
  if (content != '') {
    if (document.getElementById('chatwindow').value != content) {
      document.getElementById('chatwindow').value = content;
    }
  }
  refreshChatRoom(true);
  }


  /* Send entered message */
  function submitMsg() {
  var url = '<%= cp + '/' + md.getModulePath() %>/server.jsp?u=' + encodeURIComponent(document.getElementById('chatuser').value) + '&m=' + encodeURIComponent(document.getElementById('chatmsg').value) + '&r=' + roomId;
    sendRequest(url, msgReceived);
    document.getElementById('chatmsg').value = '';
    refreshChatRoom(true);
  }


  /* Response from server.jsp from the request for updated chat room content */
  function chatRoomReceived(content) {
    if (content != '') {
      if (document.getElementById('chatwindow').value != content) {
        document.getElementById('chatwindow').value = content;
            }
    }
    refreshChatRoom(false);
  }


  /* Request updated chat room content from chat server */
  function getChatRoom() {
    var url = '<%= cp + '/' + md.getModulePath() %>/server.jsp?r=' + roomId;
    sendRequest(url, chatRoomReceived);
  }


  /* Validate the message when the user presses [Enter] key */
  function keyup(keyCode) {
    if (keyCode == 13 || keyCode == 3) {
      submitMsg();
    }
  }


  /* Write a message to the chat status line */
  function writeStatus(msg) {
    document.getElementById('chatstatus').innerHTML = msg;
  }

/* Write debug message */
function writeDebug(msg) {
  if (debug) {
    document.getElementById('chatdebug').innerHTML = '[' + msg + ']';
  }
}
// ]]>
</script>

<div id="chat">
<textarea id="chatwindow" rows="<%= rows %>" cols="<%= cols %>" <%= style %> readonly="readonly"></textarea>
<br />
<input id="chatuser" type="text" size="10" maxlength="20" value="Anonymous" <%= style %> />&gt;&nbsp;
<input id="chatmsg" type="text" size="<%= cline %>" <%= style %> onkeyup="keyup(event.keyCode);" />
<input id="chatok" type="button" value="<%= pageBundle.getString("chatOK") %>" <%= style %> onclick="submitMsg();" />
<input id="chatrefresh" type="button" value="<%= pageBundle.getString("chatRefresh") %>" onclick="getChatRoom();" />
<br />
<span id="chatstatus" <%= style %>> </span><span id="chatdebug" <%= style %>> </span>
</div>

<script type="text/javascript">
// <![CDATA[
  /* Start access to chat server */
writeStatus('<%= pageBundle.getString("chatConnecting") %>');
  timeoutID = window.setTimeout('getChatRoom()', waitTime);
// ]]>
</script>

