<%@ taglib uri="meshcms-taglib" prefix="cms" %>
<cms:setlocale value="en_US" />

<html>

<head>
  <title><cms:pagetitle /> [MeshCMS]</title>
  <cms:defaultcss />
  <cms:pagehead />
</head>

<body style="margin: 0px;"><cms:editor>
<table width="770" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td colspan="2" valign="bottom" class="header"><cms:pagetitle /></td>
  </tr>
  <tr>
    <td class="leftcolumn" valign="top" width="180"><table border="0" align="center" cellpadding="3" cellspacing="0">
      <tr>
        <th>Site Menu:</th>
      </tr>
      <tr>
        <td><cms:simplemenu expand="true" /></td>
      </tr>
      <tr>
        <th>MeshCMS Menu:</th>
      </tr>
      <tr>
        <td><cms:adminmenu separator="<br />" /></td>
      </tr>
      <tr>
        <td><cms:module location="left" date="normal" /></td>
      </tr>
    </table></td>
    <td valign="top" width="590"><table border="0" cellspacing="0" cellpadding="5" width="580">
      <tr>
        <td class="breadcrumbs"><cms:breadcrumbs mode="links" separator=" &raquo; " pre="You are viewing: " /></td>
      </tr>
      <tr>
        <td><cms:pagebody /></td>
      </tr>
      <tr>
        <td><cms:module location="bottom" date="normal" /></td>
      </tr>
      <tr>
        <td><cms:mailform /></td>
      </tr>
      <tr>
        <td class="lastmodified"><cms:lastmodified pre="Last modified: " /></td>
      </tr>
    </table></td>
  </tr>
  <tr align="center">
    <td colspan="2" bgcolor="#DDBC00">
      Powered by <a href="http://www.cromoteca.com/meshcms/">MeshCMS</a>
      | Generated: <%= new java.util.Date() %>
    </td>
  </tr>
</table>
</cms:editor></body>

</html>
