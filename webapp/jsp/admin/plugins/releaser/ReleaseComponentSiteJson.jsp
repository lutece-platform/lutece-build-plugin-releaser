<jsp:useBean id="manageSiteRelease" scope="session" class="fr.paris.lutece.plugins.releaser.web.ManageSiteReleaseJspBean" />
<% String strContent = manageSiteRelease.processController ( request , response ); %>
<%= strContent %>
