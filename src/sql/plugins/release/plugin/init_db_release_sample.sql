--
-- Dumping data for table `releaser_cluster`
--
INSERT INTO releaser_cluster (id_cluster, name, description) VALUES (1,'GRU Multi-Serveurs','GRU Multi-Serveurs');
INSERT INTO releaser_cluster (id_cluster, name, description) VALUES (2,'GRU Mono Serveur','GRU Mono serveur');

--
-- Dumping data for table `releaser_site`
--
INSERT INTO releaser_site (id_site, artifact_id, id_cluster, scm_url, name, description, jira_key) VALUES (1,'site-ticketing',1,'http://dev.lutece.paris.fr/svn/sites/gru/multi-sites/ticketing/trunk','Site Ticketing (multi-instances)','Site de gestion des sollicitations en contexte multi-instances ','');
INSERT INTO releaser_site (id_site, artifact_id, id_cluster, scm_url, name, description, jira_key) VALUES (2,'site-avatars',1,'http://dev.lutece.paris.fr/svn/sites/gru/multi-sites/avatars/trunk','Site Avatars (multi-instances)','Site de gestion des avatars des utilisateurs des back-offices GRU ','');
INSERT INTO releaser_site (id_site, artifact_id, id_cluster, scm_url, name, description, jira_key) VALUES (3,'site-notifications',1,'http://dev.lutece.paris.fr/svn/sites/gru/multi-sites/notifications/trunk','Site Notifications (multi-instances)','Site de traitement des notifications reçues via le bus applicatif','');
INSERT INTO releaser_site (id_site, artifact_id, id_cluster, scm_url, name, description, jira_key) VALUES (4,'site-vue360',1,'http://dev.lutece.paris.fr/svn/sites/gru/multi-sites/vue360/trunk','Site Vue 360 (multi-instances)','Site de la vue 360 de la GRU ','');
INSERT INTO releaser_site (id_site, artifact_id, id_cluster, scm_url, name, description, jira_key) VALUES (5,'site-identity',1,'http://dev.lutece.paris.fr/svn/sites/gru/multi-sites/identity/trunk','Site de gestion des identités (multi-instances)','Site de gestion des identités ','');
INSERT INTO releaser_site (id_site, artifact_id, id_cluster, scm_url, name, description, jira_key) VALUES (6,'site-moncompte',1,'http://dev.lutece.paris.fr/svn/sites/gru/multi-sites/moncompte/trunk','Site Mon Compte (multi-instances)','Tableau de bord de l\'usager de Paris.fr ','');
