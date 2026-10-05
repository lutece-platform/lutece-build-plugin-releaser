--
-- Platform release feature : release of the Lutece platform (global-pom, site-pom, core, lutece-platform) driven step by step
--

--
-- Structure for table releaser_platform_release (a release campaign)
--

DROP TABLE IF EXISTS releaser_platform_release;
CREATE TABLE releaser_platform_release (
id_platform_release int AUTO_INCREMENT,
name varchar(255) default '' NOT NULL,
release_type varchar(20) default 'STABLE' NOT NULL,
core_major int default 8 NOT NULL,
current_step int default 1 NOT NULL,
status varchar(20) default 'READY' NOT NULL,
export_verified SMALLINT DEFAULT 0,
user_name varchar(255) default '',
date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
date_update TIMESTAMP DEFAULT NULL NULL,
PRIMARY KEY (id_platform_release)
);

--
-- Structure for table releaser_platform_release_step (one row per step of a campaign)
--

DROP TABLE IF EXISTS releaser_platform_release_step;
CREATE TABLE releaser_platform_release_step (
id_step int AUTO_INCREMENT,
id_platform_release int NOT NULL,
step_number int NOT NULL,
status varchar(20) default 'TODO' NOT NULL,
plan_json long varchar,
result_json long varchar,
jenkins_build_url varchar(500) default '',
jenkins_build_number int default 0,
jenkins_result varchar(255) default '',
date_begin TIMESTAMP DEFAULT NULL NULL,
date_end TIMESTAMP DEFAULT NULL NULL,
PRIMARY KEY (id_step)
);

--
-- Structure for table releaser_platform_step_definition (the 5 steps and the SCM URL of their aggregate, editable)
--

DROP TABLE IF EXISTS releaser_platform_step_definition;
CREATE TABLE releaser_platform_step_definition (
step_number int NOT NULL,
code varchar(30) default '' NOT NULL,
name varchar(100) default '' NOT NULL,
scm_url varchar(255) default '' NOT NULL,
PRIMARY KEY (step_number)
);
