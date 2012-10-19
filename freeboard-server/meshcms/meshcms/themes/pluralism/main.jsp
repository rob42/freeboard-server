<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
Design by Free CSS Templates
http://www.freecsstemplates.org
Released for free under a Creative Commons Attribution 2.5 License

Name       : Pluralism
Description: A two-column, fixed-width template fit for 1024x768 screen resolutions.
Version    : 1.0
Released   : 20071218

-->

<%@ taglib uri="meshcms-taglib" prefix="cms" %>
<cms:setlocale value="en" /> 

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title><cms:pagetitle /> [MeshCMS]</title>
        <cms:defaultcss />
        <cms:pagehead /> 
        <meta name="description" content="<cms:info id="description" />" />
        <meta name="keywords" content="<cms:info id="keywords" />" />
        <meta name="author" content="<cms:info id="author" /> (<cms:info id="authorurl" />)" />
    </head>
    <body id="realbody">
        <cms:editor> 
            <div id="wrapper">
                <div id="wrapper2">
                    <div id="header">
                        <div id="logo">
                            <h1><cms:pagetitle /></h1>
                        </div>
                        <div id="menu">
                            <cms:listmenu items="firstlevel" current="link" currentStyle="current" />
                        </div>
                    </div>
                    <!-- end #header -->
                    <div id="page">
                        <div id="content">
                            <cms:ifmodule location="top">
                                <div class="post">
                                    <h2 class="title">
                                        <cms:moduletitle location="top" />
                                    </h2>
                                    <cms:module location="top" alt="" />
                                </div>
                            </cms:ifmodule>
                            
                            <div class="post">
                                <cms:pagebody /> 
                            </div>
                            
                            <cms:ifmodule location="bottom">
                                <div class="post">
                                    <h2 class="title">
                                        <cms:moduletitle location="bottom" />
                                    </h2>
                                    <cms:module location="bottom" alt="" />
                                </div>
                            </cms:ifmodule>
                            
                            <div class="post">
                                <cms:mailform /> 
                                <cms:lastmodified pre="<p class='meta'><span class='posted'>Last modified: "
                                                  post="</span></p>" />
                            </div>
                        </div>
                        <!-- end #content -->
                        <div id="sidebar">
                            <ul>
                                <cms:ifnotediting>
                                    <%-- To make this search form work, ensure that the Host (domain)
                                    field in the configuration has been set correctly --%>
                                    <li id="search">
                                        <h3>Search</h3>
                                        <form id="searchform" action="http://www.google.com/search" method="get">
                                            <input type="hidden" name="as_sitesearch" value="<cms:info id="host" />" />
                                            <div>
                                                <input type="text" name="as_q" id="s" size="15" />
                                                <br />
                                                <input name="submit" type="submit" value="Go" />
                                            </div>
                                        </form>
                                    </li>
                                </cms:ifnotediting>
                                <li>
                                    <h3>Navigation</h3>
                                    <cms:listmenu items="onpath,lastlevel,children" style="linklist" />
                                </li>
                                <cms:ifmodule location="right">
                                    <li>
                                        <h3>
                                            <cms:moduletitle location="right" />
                                        </h3>
                                        <cms:module location="right" alt="" />
                                    </li>
                                </cms:ifmodule>
                                <li>
                                    <h3>Admin Menu</h3>
                                    <ul>
                                        <li>
                                            <cms:adminmenu separator="</li><li>" />
                                        </li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                        <!-- end #sidebar -->
                        
                        <div style="clear: both;">&nbsp;</div>
                        
                    </div>
                    <!-- end #page -->
                </div>
                <!-- end #wrapper2 -->
                <div id="footer">
                    <p>
                        &copy; <a href="<cms:info id="authorurl" />"><cms:info id="author" /></a>.
                        Design by <a href="http://www.nodethirtythree.com/">NodeThirtyThree</a> +
                        <a href="http://www.freecsstemplates.org/">Free CSS Templates</a>
                    </p>
                </div>
            </div>
            <!-- end #wrapper -->
        </cms:editor>
    </body>
</html>
