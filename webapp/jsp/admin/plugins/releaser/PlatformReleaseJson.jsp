<jsp:useBean id="managePlatformRelease" scope="session" class="fr.paris.lutece.plugins.releaser.web.platform.ManagePlatformReleaseJspBean" />
<% String strContent = managePlatformRelease.processController ( request , response ); %>
<%= strContent %>
