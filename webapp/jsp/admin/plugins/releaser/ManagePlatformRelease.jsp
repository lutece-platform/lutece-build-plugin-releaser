<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:useBean id="managePlatformRelease" scope="session" class="fr.paris.lutece.plugins.releaser.web.platform.ManagePlatformReleaseJspBean" />
<% String strContent = managePlatformRelease.processController ( request , response ); %>

<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
