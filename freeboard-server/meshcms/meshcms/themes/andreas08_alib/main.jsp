<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ taglib uri="meshcms-taglib" prefix="cms" %>
<cms:setlocale value="en" /> 

<html>
<head>
<title><cms:pagetitle /> [MeshCMS]</title>
<cms:defaultcss />
<cms:pagehead /> 
<meta name="description" content="<cms:info id="description" />" />
<meta name="keywords" content="<cms:info id="keywords" />" />
<meta name="author" content="<cms:info id="author" />" />
<cms:alibmenu part="head" orientation="horizontal" />
</head>

<body id="realbody">
<cms:editor>
<div id="container" >
  <div id="header">
    <h1><cms:pagetitle /></h1>
    <h2><cms:info id="name" /></h2>
  </div>

  <div id="navigation"><cms:listmenu style="hmenu" items="all" current="link" currentStyle="selected" currentPathStyle="selected" /></div>

  <div id="content">
    <cms:module location="top" alt="" />
    <cms:pagebody /> 
    <cms:module location="bottom" alt="" />
    <cms:mailform />
  </div>
  <div id="subcontent">
    <h2>Navigation</h2>
    <cms:listmenu items="onpath,lastlevel,children" style="menublock" />

    <cms:module location="right" alt="" />

    <h2>User Menu</h2>
    <cms:adminmenu style="menublock" separator="<br />" />

    <%-- search is not available at the moment
    <cms:ifnotediting>
    <h1>Search</h1>
    <p class="searchform"> 
      <input type="text" alt="Search" class="searchbox" />
      <input type="submit" value="Go!" class="searchbutton" />
    </p>
    </cms:ifnotediting>
    --%>
</div>

<div id="footer-pre"><p style="text-align: center;"><cms:lastmodified pre="Last modified: " /></p></div>
<div id="footer"><p>&copy; <a href="<cms:info id="authorurl" />"><cms:info id="author" /></a>
 | Design by <a href="http://andreasviklund.com">Andreas Viklund</a></p></div>
</div>
</cms:editor>
</body>
</html>