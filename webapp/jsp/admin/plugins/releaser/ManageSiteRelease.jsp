<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:useBean id="manageSiteRelease" scope="session" class="fr.paris.lutece.plugins.releaser.web.ManageSiteReleaseJspBean" />
<% String strContent = manageSiteRelease.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
