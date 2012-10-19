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
</head>

<body id="realbody">
<cms:editor>
<div id="wrap">
  <div id="header">
    <p id="toplinks">Skip to: <a href="#contentalt">Content</a> | <a href="#sidebar">Navigation</a> | <a href="#footer">Footer</a></p>
    <h1><a href=""><cms:pagetitle /></a></h1>
    <p id="slogan"><cms:info id="name" /></p>
  </div>

  <div id="contentalt">
    <cms:module location="top" alt="" />
    <cms:pagebody /> 
    <cms:module location="bottom" alt="" />
    <cms:mailform />
  </div>

  <div id="sidebar">
    <h2>Navigation</h2>
    <cms:listmenu items="all" current="link" currentStyle="selected" />

    <cms:module location="right" alt="" />

    <h2>User Menu</h2>
    <cms:adminmenu style="ul" separator="<br />" />

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

  <div id="footer"><p>&copy; <a href="<cms:info id="authorurl" />"><cms:info id="author" /></a>
 | Design by <a href="http://andreasviklund.com">Andreas Viklund</a></p></div>
</div>
</cms:editor>
</body>
</html>
