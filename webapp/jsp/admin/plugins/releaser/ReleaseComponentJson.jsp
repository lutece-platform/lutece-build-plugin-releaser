<jsp:useBean id="manageComponentRelease" scope="session" class="fr.paris.lutece.plugins.releaser.web.ManageComponentReleaseJspBean" />
<% String strContent = manageComponentRelease.processController ( request , response ); %>
<%= strContent %>
