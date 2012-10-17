FlowPlayer
========== 
FlowPlayer is a video player for FLV. It is designed to be
embedded into an HTML page. FLV is handled using 'progressive download' with
Flash's NetStream and NetConnection classes. FLV is supported with Flash Player
version 7 or higher.

Version history 
===============

0.9     Initial public release. All basic features are in place.

0.9.1   Added 'autoPlay' variable that can be used to specify whether the
        playback should begin immediately when the player has been loaded into
        the Web browser.

0.9.2   Bug fixes.

0.9.3   Added new 'bufferLength' variable that can be used in the HTML page to
        specify the length of the buffer in seconds when streaming FLV. Fixed a
        bug that prevented playback after an FLV was completely played.

0.9.4   Added a 'baseURL' variable that can be used to specify the location of
        the video file to be loaded by Player.swf. See Player.html for an
        example.

        If the 'videoFile' variable's value contains a '.flv' or '.swf'
        extension, the standalone player (Player.swf) will NOT append this based
        on the detected flash version. If a prefix is not present, the player
        always appends either one of these prefixes.

1.0
	- Displays a "BUFFERING..." text when filling the video buffer.
	- Fixed playback of the start and the end of the video where the player
	  was errorneously cutting off some of the video.
	- Added a new start() function to the FlowPlayer class.
	- Fixed Sample.fla

1.1
    - Added ability to loop; Contains a new toggle button to control looping.
      Default looping state is defined in 'loop' parameter. Thanks Jeff Wagner
      for contributing the initial looping support.
    - Now resizes according to the size defined in the HTML page
    - Fixed some flaws in the graphics
    - The color of the progress bar is now gray by default (more neutral). The
      color can be customized by parameters.
    - Removed support to play videos in SWF format.

1.2
    - Added a 'autoBuffering' option and welcome image support.
    - Added a 'hideContols' option to hide all buttons and other widgets and
      leaving only the video display showing.
    - Added support for welcome images
    - Most of the UI is now built dynamically with ActionScript instead of using
      pre-drawn images. This results in 50% smaller download size.

1.2.b2
	- Fixed binary build that contained an old buggy FlowPlayer.swf

1.3
    - Fixed resizing problem that occurred with Internet Explorer: The video was
      not resized when the page was refreshed.

1.4
	- Removed the blue the background color of the player. The light blue color
	  became visible when using only the obect-tag to embed the player into a page.
	  By using only the object tag it's possible to author valid XHTML. The sample
	  FlowPlayer.html now shows this kind of markup.

1.5
	- Support for playlists
	- Extenal configuration file that enables configuring all the existing
	  settings. All settings defined in this configuration file can be
	  overridden using flashvars in the HTML object tag.
	- Basic skinning support: Images for all buttons (play, pause, looping
	  toggle, and dragger) can be loaded from external JPG files. Smaller
	  versions of the default buttons are provided as an example.
	  FlowPlayerLiht.swf is meant to be used with skinning: it does not contain
	  any button images in itself and therefore is slightly smaller in download
	  size.
	- 'hideBorder' option
	- visual improvement of control buttons
	- dragging can be now done by clicking anywhere in the progress bar area
	- clicking on the video area pauses and resumes playback
	- scaling the splash image is now optional. Alternatively it can be centered
	  into the video area.
	- removed the border surrounding the video area
	- plus some more minor changes
	Bug fixes:
	- Seeking using the dragger button is more accurate. Now it is possible to
	  seek to the very beginning of a clip.

1.6
	Bug fixes:
	- Does not buffer unnessessarily if looping through one clip again and again
	- Playback can be started normally again when the clip is done and looping
	is off. There was a bug that prevented this.
	- Clicking on the video area did not start playing if splash not used and
	when autoPlay=false
	- The seeker button aligned to the right from the mouse position when it was
	grabbed using the mouse button. Now it stays on the same position it was in
	when the mouse button was pressed down.
	- It was not possible to use characters 'y' and 'Y' in the names inside 
	the playList. Now following characters are available: 
	"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz?!/-:,. 1234567890"

1.7
	Fixes:
	- baseURLs are not appended to file name values that are complete URLs
	- minor visual enhancements
	New features:
	- Support for long videos: Initial support for streaming servers (tested with red5) +
	thumbnails corresponding to cue points.
	- Video duration and time indicator
	- Resizing of the video area through a menu

1.7.1
	- Now the original FLV video dimensions are detected from the FLV metadata.
	  The different resizing options are based on those. Initially the size is 
	  maximized according to the 'videoHeight' setting and preserving the aspect ratio.
	Fixes:
	- progress bar goes to the start position (left) even when autoPlay=false 
	and autoBuffering=false [1574640]
	- resizing menu does not toggle pause/resume anymore [1581085]
	- missing audio on some FLV files [1568612]
	- Flash Media Servers seems to send the FLV metadata again and again when seeking. This
	  caused unnessessary rearranging of the thumbnails.
	- Thumnail list's scrollbar is not shown if all thumbnails fit the available visible space

Distribution file contents 
===========================

The binary distribution contains following files:

FlowPlayer.swf		A player that can be used as is in a Web page.
FlowPlayer.html		A sample HTML file that uses the player.

The source distribution contains all documented ActionScript 2 files and also a
FLA-files: FlowPlayer.fla. FlowPlayer.fla contains all the movie clips and other
symbols in it's library.

How to use it
=============

FlowPlayer.swf is a player that is ready to be used out-of-the box.
FlowPlayer.html is an example of Web page that uses it. The name of the video
file to be loaded by the player is specified in html page in the <object> tag's
flashvars attribute. 

By default, the player loads the video file from the same location as it loads
FlowPlayer.swf. The name of this variable is 'videoFile' and the value should be
the name of the video file with or without an extension.

There is also a way to specify a complete URL. This is done using the 'baseURL'
variable. When this mechanism is used the video files and FlowPlayer.swf may
reside in different locations within the Web server or even in different
servers.

Please refer to flowPlayer.js for descriptions of all HTML parameters used to
control the player. 

Hacking
-------

FlowPlayer has following dependencies:
* Luminic Box logger (http://luminicbox.com)
* as2lib (http://www.as2lib.org)

You need to specify locations for these in the build.xml. Modify the file accordingly.

To compile FlowPlayer you need following tools:
* mtasc compiler (http://www.mtasc.org)
* swfmill (http://iterative.org/swfmill)
* Ant (http://ant.apache.org)
* as2ant (http://www.as2lib.org)

You need to set up as2ant targets for Ant. I have configured as2ant targets using 
Eclipse's integrated Ant configuration (Eclipse/Preferences/Ant/Runtime/Tasks). 
After that you should be able to buld FlowPlayer using Ant. There is no need to
use the Adobe/Macromedia Flash tool at all, everything can be done using these tools.

To build run: ant build

To test cd to 'build' directory and open FlowPlayer.html using a browser. 
Change the parameters in FlowPlayer.html to try with your own videos.

todo
====
- code cleanup: playlist package to separate model & UI package
- Skinning documentation, image names etc.

Bug reports, questions, contributing 
==================================== 
If you have bug reports, questions or would like to contribute, please send a
message to: api@iki.fi
