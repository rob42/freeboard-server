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
        <meta name="author" content="<cms:info id="author" /> (<cms:info id="authorurl" />)" />
        <cms:feed />
    </head>
    
    <body id="realbody">
        <cms:editor> 
            <div id="container"> 
                <div id="sitename"> 
                    <h1><cms:pagetitle /></h1>
                    <h2><cms:info id="name" /></h2>
                </div>
                
                <div id="mainmenu">
                    <cms:listmenu items="firstlevel" current="link" currentStyle="current" />
                </div>
                
                <div id="wrap"> 
                    <div id="rightside">
                        <cms:ifnotediting>
                            <%-- To make this search form work, ensure that the Host (domain)
                            field in the configuration has been set correctly --%>
                            <form id="searchform" action="http://www.google.com/search" method="get">
                                <h1>Search</h1>
                                <p class="searchform">
                                    <input type="hidden" name="as_sitesearch" value="<cms:info id="host" />" />
                                    <input type="text" id="google_search" name="as_q" class="searchbox" />
                                    <input type="submit" value="Go!" class="searchbutton" />
                                </p>
                            </form>
                        </cms:ifnotediting>
                        
                        <h1>Navigation</h1>
                        <cms:listmenu items="onpath,lastlevel,children" style="linklist" />
                        
                        <h1><cms:moduletitle location="right" /></h1>
                        <cms:module location="right" alt="" />
                        
                        <h1>User Menu</h1>
                        <p><cms:adminmenu separator="<br />" /></p>
                    </div>
                    <div id="contentalt" >
                        <cms:ifmodule location="top">
                            <h2><cms:moduletitle location="top" /></h2>
                            <cms:module location="top" alt="" />
                        </cms:ifmodule>
                        
                        <cms:pagebody /> 
                        
                        <cms:ifmodule location="bottom">
                            <h2><cms:moduletitle location="bottom" /></h2>
                            <cms:module location="bottom" alt="" />
                        </cms:ifmodule>

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
