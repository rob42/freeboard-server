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

 Look at the chat client for usage.

 The server expects 3 types of parameters:
 - r: room identifier
 - m: message to add to the chat room
 - u: name of the user posting the message
 
--%>

<%@ page language="java" contentType="text/plain; charset=UTF-8" pageEncoding="ISO-8859-1" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%
  final int MAX_MESSAGES = 100;
  final String RESET_CHAT_CMD = "clean room!";

  // Select the chat room
  String roomId = request.getParameter("r");
  if (roomId == null) {
    roomId = "";
  }

  List room = (List) application.getAttribute("chatRoom_" + roomId);
  if (room == null) {
    room = Collections.synchronizedList(new LinkedList());
    application.setAttribute("chatRoom_" + roomId, room);
  }

  // We have to UTF-8 decode parameters because they are sent
  // using encodeURIComponent().
  String msg = request.getParameter("m");
  if (msg != null) {
    try {
      msg = new String(msg.getBytes(), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      msg = "(EncodingException)" + msg;
	    }
    }

  String user = request.getParameter("u");
  if (user != null) {
    try {
      user = new String(user.getBytes(), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      user = "(EncodingException)" + user;
    }
  }


  // Message posted: add it to the chat room
  if (msg != null) {
	  // Clear chat room cache
    application.setAttribute("chatRoomCache_" + roomId, null);

    synchronized (room) {
	    // Special command to clean room
      if (RESET_CHAT_CMD.equals(msg)) {
	    room.clear();

	    // Else accept message, even empty ones
      } else {
        room.add(0, ((user == null || "".equals(user)) ? "Anonymous" : user) + "> " + msg);

        while (room.size() > MAX_MESSAGES) {
          room.remove(room.size() - 1);
        }
      }
    }
  }

  // Content requested
  if (!"".equals(roomId)) {
    out.clear();
    String chatRoomText = (String) application.getAttribute("chatRoomCache_" + roomId);
  
    // If we don't already have the content of the chat room in the cache, we
    // generate it. Old Javascript (namely Konqueror3) doesn't support to receive
    // raw Unicode characters (as sent by the HTTP stream), so we have to encode them...
    if (chatRoomText == null) {
      synchronized (room) {
        final StringBuffer sb = new StringBuffer(MAX_MESSAGES * 30);
        int i = 0;
        for (final Iterator it = room.iterator(); it.hasNext() && i < MAX_MESSAGES; i++) {
          sb.append((String) it.next()).append('\n');
	  /** DEBUG - Print Unicode values
          final String s = (String) it.next();
          final int length = s.length();
          for (int j = 0; j < length; j++) {
            final String hex = "0000" + Integer.toHexString((int) s.charAt(j));
            sb.append("\\u");
            sb.append(hex.substring(hex.length() - 4, hex.length()));
          }
          sb.append("\\u000A");
	  */
        }
        chatRoomText = sb.toString();
        application.setAttribute("chatRoomCache_" + roomId, chatRoomText);
      }
    }
    out.print(chatRoomText);
    out.flush();
  }
%>

