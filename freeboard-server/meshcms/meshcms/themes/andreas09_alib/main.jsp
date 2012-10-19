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
<div id="container"> 
  <div id="sitename"> 
    <h2><cms:info id="name" /> :: <cms:pagetitle /></h2>
  </div>

  <div id="navigation"><cms:alibmenu part="body" orientation="horizontal" currentPathStyle="selected" /> </div>

  <div id="wrap"> 
    <div id="rightside">
      <h1>Navigation</h1>
      <cms:listmenu items="onpath,lastlevel,children" style="linklist" />

      <cms:module location="right" alt="" />
	  
      <h1>User Menu</h1>
      <p><cms:adminmenu separator="<br />" /></p>

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
    <div id="contentalt" >
      <cms:module location="top" alt="" />
      <cms:pagebody /> 
      <cms:module location="bottom" alt="" />
      <cms:mailform /> 
      <p style="text-align: right;"><cms:lastmodified pre="Last modified: " /></p>
    </div>
	
    <div class="clearingdiv">&nbsp;</div>
  </div>
</div>

<div id="footer">&copy; <a href="<cms:info id="authorurl" />"><cms:info id="author" /></a>
 | Design by <a href="http://andreasviklund.com">Andreas Viklund</a></div>
</cms:editor>
</body>
</html>
