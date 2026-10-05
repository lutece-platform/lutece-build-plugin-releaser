--
-- Upgrade 1.6.1 -> 2.0.0
--
-- Platform release feature : campaigns, their steps and the step definitions (see plugin/create_db_release_platform.sql and init_db_release_platform.sql)
--

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

CREATE TABLE releaser_platform_step_definition (
step_number int NOT NULL,
code varchar(30) default '' NOT NULL,
name varchar(100) default '' NOT NULL,
scm_url varchar(255) default '' NOT NULL,
PRIMARY KEY (step_number)
);

INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (1, 'GLOBAL_POM', 'Global POM', 'https://github.com/lutece-platform/tools-maven-global-pom.git');
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (2, 'SITE_POM', 'Site POM', 'https://github.com/lutece-platform/tools-maven-site-pom.git');
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (3, 'CORE', 'Lutece core', 'https://github.com/lutece-platform/lutece-core.git');
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (4, 'PLATFORM_PLUGINS', 'Platform plugins', 'https://github.com/lutece-platform/lutece-platform.git');
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (5, 'PLATFORM_STARTERS', 'Platform starters and BOM', 'https://github.com/lutece-platform/lutece-platform.git');

--
-- Admin feature of the platform release (see core/init_core_release.sql)
--
DELETE FROM core_admin_right WHERE id_right = 'RELEASER_PLATFORM';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES
('RELEASER_PLATFORM','releaser.adminFeature.ManagePlatformRelease.name',1,'jsp/admin/plugins/releaser/ManagePlatformRelease.jsp','releaser.adminFeature.ManagePlatformRelease.description',0,'releaser',NULL,NULL,NULL,5);

DELETE FROM core_user_right WHERE id_right = 'RELEASER_PLATFORM';
INSERT INTO core_user_right (id_right,id_user) VALUES ('RELEASER_PLATFORM',1);
