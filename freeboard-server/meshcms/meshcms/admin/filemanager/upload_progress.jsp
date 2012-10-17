<%--
 Copyright 2004-2009 Luciano Vernaschi

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
--%>

<%@ page contentType="application/json" %>
<%@ page import="org.meshcms.webui.*" %>

<%
  UploadProgressListener upl = (UploadProgressListener)
      request.getSession(true).getAttribute(UploadProgressListener.UPLOAD_PROGRESS_LISTENER);
%>

{
  "text": "<%= upl.getPercent() %>",
  "progress": <%= upl.getProgress() %>,
  "completed": <%= upl.isCompleted() %>
}
