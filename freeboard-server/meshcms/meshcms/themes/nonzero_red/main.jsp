<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ taglib uri="meshcms-taglib" prefix="cms" %>
<cms:setlocale defaultValue="en" /> 

<!--

         Nonzero1.0 by nodethirtythree design
         http://www.nodethirtythree.com
         missing in a maze

-->
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
        <title><cms:pagetitle /> [MeshCMS]</title>
        <cms:defaultcss />
        <cms:pagehead /> 
        <meta name="description" content="<cms:info id="description" />" />
        <meta name="keywords" content="<cms:info id="keywords" />" />
        <meta name="author" content="<cms:info id="author" />" />
    </head>
    <body>
        <cms:editor>
        
            <div id="header">
                
                <div id="header_inner" class="fixed">
                    
                    <div id="logo">
                        <h1><cms:info id="name" /></h1>
                    </div>
                    
                    <div id="menu">
                        <cms:listmenu items="firstlevel" currentStyle="active"
                                      currentPathStyle="active" />
                    </div>
                    
                </div>
            </div>
            
            <div id="main">
                
                <div id="main_inner" class="fixed">
                    
                    <div id="primaryContent_2columns">
                        
                        <div id="columnA_2columns">
                            
                            <h3><cms:pagetitle /></h3>
                            
                            <cms:module location="top" alt="" />
                            <cms:pagebody /> 
                            <cms:module location="bottom" alt="" />
                            <cms:mailform />
                            
                            <br class="clear" />
                            
                        </div>
                        
                    </div>
                    
                    <div id="secondaryContent_2columns">
                        
                        <div id="columnC_2columns">
                            
                            <cms:ifmodule location="right">
                                <cms:moduletitle location="right" pre="<h4>" post="</h4>" />
                                <cms:module location="right" alt="" />
                            </cms:ifmodule>
                            
                            <h4>Navigation</h4>
                            <cms:listmenu items="onpath,lastlevel,children" style="links" />
                            
                            <h4>Admin Menu</h4>
                            <ul class="links">
                                <li class="first"><cms:adminmenu separator="</li><li>" /></li>
                            </ul>
                            
                        </div>
                        
                    </div>
                    
                    <br class="clear" />
                    
                </div>
                
            </div>
            
            <div id="footer" class="fixed">
                &copy; <a href="<cms:info id="authorurl" />"><cms:info id="author" /></a>.
                Design by <a href="http://www.nodethirtythree.com/">NodeThirtyThree Design</a>.
            </div>
            
        </cms:editor>
    </body>
</html>
