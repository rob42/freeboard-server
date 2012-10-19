<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ taglib uri="meshcms-taglib" prefix="cms" %>

<cms:setlocale />

<html>
<head>
	<title><cms:info id="name" /> - <cms:pagetitle /></title>
	<cms:defaultcss />
	<cms:pagehead />
	<meta name="description" content="<cms:info id="description" />" />
	<meta name="keywords" content="<cms:info id="keywords" />" />
	<meta name="author" content="<cms:info id="author" /> (<cms:info id="authorurl" />)" />
</head>


<body id="realbody">
<cms:editor>
<div id="container">

<div id="header" style="">
	<p id="top_info">
      <cms:adminmenu separator=" | "/>
	</p>
	<div id="logo" style="">
		<h2 style="">
			<a href="<cms:info id="host" />" title="<cms:info id="name" />" style="">
				<cms:info id="name" />
			</a>
		</h2>
		<p style="">
			<div class="left"><cms:info id="description" /></div>
			<div class="right"><cms:langmenu separator=" | "/></div>
		</p>
	</div>
</div>

<div id="tabs" style="" >
	<div id="mainmenu">
		<cms:listmenu items="firstlevel" current="link" currentStyle="current" />
	</div>
</div>

    <cms:ifnotediting>
<div id="search" style="">
	<form id="searchform" action="http://www.google.com/search" method="get">
		<p style="">
			<input type="hidden" name="as_sitesearch" value="<cms:info id="host" />"/>
			<input type="text" class="search" name="as_q" size="15" value="" style=""/>
			<input type="submit" class="button" value="Search"/>
		</p>
	</form>

	<%-- cms search is not available at the moment
	<cms:ifnotediting>
		<p class="searchform">
			<input type="text" alt="Search" class="searchbox" />
			<input type="submit" value="Go!" class="searchbutton" />
		</p>
	</cms:ifnotediting>
	--%>

</div>
    </cms:ifnotediting>

<div class="gboxtop">&nbsp;</div>
<div class="gbox">
<cms:module location="gbox" name="parse:/gbox.txt" />
</div>

<div id="leftside" style="">
	<h3 class="title maintitle"><span><cms:pagetitle /></span></h3>
	<h3><cms:moduletitle location="top" /></h3>
	<cms:module location="top" alt="" />
	<cms:pagebody />
	<h3><cms:moduletitle location="bottom" /></h3>
	<cms:module location="bottom" alt="" />
	<p class="right lastmodified">Last modified: <cms:lastmodified/></p>
</div>

<div id="rightside" style="">

<%-- hide navigation on home page and contacts page --%>
<div class="boxtop">&nbsp;</div>
<div class="box">
	  <h1>Navigation</h1>
	  <cms:listmenu items="lastlevel,onpath,children" style="linklist" />
</div>

<cms:ifmodule location="right">
	<div class="boxtop">&nbsp;</div>
	<div class="box" align="center">
		<h1><cms:moduletitle location="right" /></h1>
		<cms:module location="right" alt="" />
	</div>
</cms:ifmodule>

<cms:ifmodule location="right2">
	<div class="boxtop">&nbsp;</div>
	<div class="box" align="center">
		<h1><cms:moduletitle location="right2" /></h1>
		<cms:module location="right2" alt="" />
	</div>
</cms:ifmodule>

<cms:ifmailform>
	<div class="boxtop">&nbsp;</div>
	<div class="box">
		<h1>Ask quick question</h1>
		<cms:mailform />
	</div>
</cms:ifmailform>


</div>


<div class="footer">


<div class="gbox" style="">
	<div style="clear: both;">&nbsp;</div>
</div>

<p>
	&copy; <a href="<cms:info id="authorurl" />"><cms:info id="author" /></a> &middot;
        Design by <a href="http://www.solucija.com/"
        title="Information Architecture and Web Design">Luka Cvrk</a> &middot;
        MeshCMS integrated by <a href="http://www.palivoda.id.lv/"
        title="Palivoda IT Solutions - Make your next move in e-business with our solution!">
        Rostislav Palivoda</a>
</p>
</div>

</div>
</cms:editor>

</body>
</html>
