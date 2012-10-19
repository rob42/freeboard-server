<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ taglib uri="meshcms-taglib" prefix="cms" %>
<cms:setlocale defaultValue="en" /> 

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
      <div id="container" >
        <div id="header">
          <h1><cms:pagetitle /></h1>
          <h2><cms:info id="name" /></h2>
        </div>

        <div id="navigation"><cms:listmenu items="firstlevel" current="link" currentStyle="selected" /></div>

        <div id="content">
          <cms:module location="top" alt="" />
          <cms:pagebody /> 
          <cms:module location="bottom" alt="" />
          <cms:mailform />
        </div>
        <div id="subcontent">
          <h2>Navigation</h2>

          <cms:langmenu separator="</li><li>" pre="<ul class='menublock'><li>"
          post="</li></ul>" flags="true" />

          <cms:listmenu items="onpath,lastlevel,children" style="menublock" />

          <cms:module location="right" alt="" />

          <h2>User Menu</h2>
          <ul class="menublock">
            <li>
              <cms:adminmenu separator="</li><li>" />
            </li>
          </ul>
    
          <cms:ifnotediting>
            <h2>Search</h2>
            <%-- To make this search form work, ensure that the Host (domain)
            field in the configuration has been set correctly --%>
            <form action="http://www.google.com/search" method="get">
              <p class="searchform"> 
                <input type="hidden" name="as_sitesearch" value="<cms:info id="host" />" />
                <input type="text" name="as_q" alt="Search" class="searchbox" size="14" />
                <input type="submit" value="Go!" class="searchbutton" />
              </p>
            </form>
          </cms:ifnotediting>
        </div>

        <div id="footer-pre"><p style="text-align: center;"><cms:lastmodified pre="Last modified: " /></p></div>
        <div id="footer"><p>&copy; <a href="<cms:info id="authorurl" />"><cms:info id="author" /></a>
        | Design by <a href="http://andreasviklund.com">Andreas Viklund</a></p></div>
      </div>
    </cms:editor>
  </body>
</html>
