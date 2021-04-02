
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




DROP TABLE IF EXISTS releaser_workflow_context_history;
CREATE TABLE releaser_workflow_context_history (
id_wf_context int AUTO_INCREMENT,
date_begin TIMESTAMP DEFAULT 0,
date_end TIMESTAMP DEFAULT 0,
artifact_id varchar(255) default '',
user_name varchar(255) default '',
data long varchar,
PRIMARY KEY (id_wf_context)
);