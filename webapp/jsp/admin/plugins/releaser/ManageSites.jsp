<jsp:useBean id="managesites" scope="session" class="fr.paris.lutece.plugins.releaser.web.SiteJspBean" />
<% String strContent = managesites.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
