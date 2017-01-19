--
-- Dumping data for table `releaser_cluster`
--
INSERT INTO releaser_cluster (id_cluster, name, description) VALUES (1,'GRU Multi-Serveurs','GRU Multi-Serveurs');
INSERT INTO releaser_cluster (id_cluster, name, description) VALUES (2,'GRU Mono Serveur','GRU Mono serveur');

--
-- Dumping data for table `releaser_site`
--
INSERT INTO releaser_site (id_site, artifact_id, id_cluster, scm_url, name, description, jira_key) VALUES (1,'site-ticketing',1,'http://dev.lutece.paris.fr/svn/sites/gru/multi-sites/ticketing/trunk','Site ticketing ','Site ticketing ','');
