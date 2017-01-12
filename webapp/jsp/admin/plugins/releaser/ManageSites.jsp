<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="managesites" scope="session" class="fr.paris.lutece.plugins.releaser.web.ManageSitesJspBean" />

<% managesites.init( request, managesites.RIGHT_MANAGESITES ); %>
<%= managesites.getManageSitesHome ( request ) %>

<%@ include file="../../AdminFooter.jsp" %>
