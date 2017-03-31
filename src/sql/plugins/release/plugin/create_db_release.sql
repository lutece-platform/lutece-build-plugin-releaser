
--
-- Structure for table releaser_site
--

DROP TABLE IF EXISTS releaser_site;
CREATE TABLE releaser_site (
id_site int(6) NOT NULL,
artifact_id varchar(50) default '' NOT NULL,
id_cluster int(11) default '0' NOT NULL,
scm_url varchar(255) default '' NOT NULL,
name varchar(50) default '' NOT NULL,
description varchar(255) default '' NOT NULL,
jira_key varchar(50) default '',
is_theme SMALLINT DEFAULT 0,
PRIMARY KEY (id_site)
);

--
-- Structure for table releaser_cluster
--

DROP TABLE IF EXISTS releaser_cluster;
CREATE TABLE releaser_cluster (
id_cluster int(6) NOT NULL,
name varchar(50) default '' NOT NULL,
description varchar(255) default '' NOT NULL,
PRIMARY KEY (id_cluster)
);
