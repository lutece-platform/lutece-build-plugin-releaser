<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:useBean id="manageComponentRelease" scope="session" class="fr.paris.lutece.plugins.releaser.web.ManageComponentReleaseJspBean" />
<% String strContent = manageComponentRelease.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
