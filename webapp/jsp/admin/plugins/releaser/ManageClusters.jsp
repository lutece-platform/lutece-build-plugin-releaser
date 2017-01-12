<jsp:useBean id="managesitesCluster" scope="session" class="fr.paris.lutece.plugins.releaser.web.ClusterJspBean" />
<% String strContent = managesitesCluster.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
