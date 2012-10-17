<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ taglib uri="meshcms-taglib" prefix="cms" %>
<cms:setlocale value="en" /> 

<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="en-US" xml:lang="en">
  <head>
    <title><cms:pagetitle /> [MeshCMS]</title>
    
    <link rel="stylesheet" href="<cms:themepath/>/style.css" type="text/css" media="screen" />
    <cms:defaultcss />
    <!--[if IE 6]><link rel="stylesheet" href="<cms:themepath/>/style.ie6.css" type="text/css" media="screen" /><![endif]-->
    <!--[if IE 7]><link rel="stylesheet" href="<cms:themepath/>/style.ie7.css" type="text/css" media="screen" /><![endif]-->

    <script type="text/javascript" src="<cms:adminpath/>/scripts/jquery/jquery.min.js"></script>
    <script type="text/javascript" src="<cms:themepath/>/script.js"></script>
    
    <cms:pagehead /> 
    <meta name="description" content="<cms:info id="description" />" />
    <meta name="keywords" content="<cms:info id="keywords" />" />
    <meta name="author" content="<cms:info id="author" /> (<cms:info id="authorurl" />)" />
    <cms:feed />
  </head>
  <body>
    <cms:editor> 
      <div id="art-main">
        <div class="art-sheet">
          <div class="art-sheet-tl"></div>
          <div class="art-sheet-tr"></div>
          <div class="art-sheet-bl"></div>
          <div class="art-sheet-br"></div>
          <div class="art-sheet-tc"></div>
          <div class="art-sheet-bc"></div>
          <div class="art-sheet-cl"></div>
          <div class="art-sheet-cr"></div>
          <div class="art-sheet-cc"></div>
          <div class="art-sheet-body">
            <div class="art-header">
              <div class="art-header-center">
                <div class="art-header-png"></div>
                <div class="art-header-jpeg"></div>
              </div>
              <div class="art-logo">
                <h1 id="name-text" class="art-logo-name"><cms:pagetitle /></h1>
                <h2 id="slogan-text" class="art-logo-text"><cms:info id="name" /></h2>
              </div>
            </div>
            <div class="art-content-layout">
              <div class="art-content-layout-row">
                <div class="art-layout-cell art-content">
                  <div class="art-post">
                    <div class="art-post-tl"></div>
                    <div class="art-post-tr"></div>
                    <div class="art-post-bl"></div>
                    <div class="art-post-br"></div>
                    <div class="art-post-tc"></div>
                    <div class="art-post-bc"></div>
                    <div class="art-post-cl"></div>
                    <div class="art-post-cr"></div>
                    <div class="art-post-cc"></div>
                    <div class="art-post-body">
                      <div class="art-post-inner art-article">
                        <div class="art-postcontent">
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
                        </div>
                        <div class="cleared"></div>
                      </div>
                      
                      <div class="cleared"></div>
                    </div>
                  </div>
                  <div class="cleared"></div>
                </div>
                <div class="art-layout-cell art-sidebar1">
                  <div class="art-vmenublock">
                    <div class="art-vmenublock-tl"></div>
                    <div class="art-vmenublock-tr"></div>
                    <div class="art-vmenublock-bl"></div>
                    <div class="art-vmenublock-br"></div>
                    <div class="art-vmenublock-tc"></div>
                    <div class="art-vmenublock-bc"></div>
                    <div class="art-vmenublock-cl"></div>
                    <div class="art-vmenublock-cr"></div>
                    <div class="art-vmenublock-cc"></div>
                    <div class="art-vmenublock-body">
                      <div class="art-vmenublockcontent">
                        <div class="art-vmenublockcontent-body">
                          <cms:listmenu items="firstlevel,onpath,intermediatelevels,lastlevel,children" style="art-vmenu"
                                        current="link" artisteerMarkup="true" />
                          <div class="cleared"></div>
                        </div>
                      </div>
                      <div class="cleared"></div>
                    </div>
                  </div>
                  <cms:ifnotediting>
                    <div class="art-block">
                      <div class="art-block-body">
                        <div class="art-blockheader">
                          <div class="l"></div>
                          <div class="r"></div>
                          <h3 class="t">Search</h3>
                        </div>
                        <div class="art-blockcontent">
                          <div class="art-blockcontent-body">
                            <%-- To make this search form work, ensure that the Host (domain)
                                 field in the configuration has been set correctly --%>
                            <form method="get" name="searchform" action="http://www.google.com/search">
                              <input type="hidden" name="as_sitesearch" value="<cms:info id="host" />" />
                              <input type="text" value="" name="as_q" style="width: 95%;" /><span class="art-button-wrapper">
                                <span class="art-button-l"> </span>
                                <span class="art-button-r"> </span>
                                <input class="art-button" type="submit" name="search" value="Search" />
                              </span>
                              
                            </form>
                            
                            <div class="cleared"></div>
                          </div>
                        </div>
                        <div class="cleared"></div>
                      </div>
                    </div>
                  </cms:ifnotediting>
                  <cms:ifmodule location="right">
                    <div class="art-block">
                      <div class="art-block-body">
                        <div class="art-blockheader">
                          <div class="l"></div>
                          <div class="r"></div>
                          <h3 class="t"><cms:moduletitle location="right" /></h3>
                        </div>
                        <div class="art-blockcontent">
                          <div class="art-blockcontent-body">
                            <cms:module location="right" alt="" />
                            <div class="cleared"></div>
                          </div>
                        </div>
                        <div class="cleared"></div>
                      </div>
                    </div>
                  </cms:ifmodule>
                  <div class="art-block">
                    <div class="art-block-body">
                      <div class="art-blockheader">
                        <div class="l"></div>
                        <div class="r"></div>
                        <h3 class="t">User Menu</h3>
                      </div>
                      <div class="art-blockcontent">
                        <div class="art-blockcontent-body">
                          <ul>
                            <li>
                              <cms:adminmenu separator="</li><li>" />
                            </li>
                          </ul>
                          <div class="cleared"></div>
                        </div>
                      </div>
                      <div class="cleared"></div>
                    </div>
                  </div>
                  <div class="cleared"></div>
                </div>
              </div>
            </div>
            <div class="cleared"></div>
            <div class="art-footer">
              <div class="art-footer-body">
                <div class="art-footer-text">
                  <p>
                    <cms:lastmodified pre="Last modified: " post=" |" /> &copy; <a href="<cms:info id="authorurl" />"><cms:info id="author" /></a>
                  </p>
                </div>
                <div class="cleared"></div>
              </div>
            </div>
            <div class="cleared"></div>
          </div>
        </div>
        <div class="cleared"></div>
        <p class="art-page-footer">
          <a href="http://cromoteca.com/meshcms">Powered by MeshCMS</a>
        </p>
      </div>
    </cms:editor>     
  </body>
</html>
