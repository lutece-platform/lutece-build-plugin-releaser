--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'RELEASER_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('RELEASER_MANAGEMENT','releaser.adminFeature.ManageReleaser.name',1,'jsp/admin/plugins/releaser/ManageClusters.jsp','releaser.adminFeature.ManageReleaser.description',0,'releaser',NULL,NULL,NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'RELEASER_MANAGEMENT';
DELETE FROM core_user_right WHERE id_right = 'RELEASER_MANAGEMENT_COMPONENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('RELEASER_MANAGEMENT',1);

--
-- Platform release feature
--
DELETE FROM core_admin_right WHERE id_right = 'RELEASER_PLATFORM';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES
('RELEASER_PLATFORM','releaser.adminFeature.ManagePlatformRelease.name',1,'jsp/admin/plugins/releaser/ManagePlatformRelease.jsp','releaser.adminFeature.ManagePlatformRelease.description',0,'releaser',NULL,NULL,NULL,5);

DELETE FROM core_user_right WHERE id_right = 'RELEASER_PLATFORM';
INSERT INTO core_user_right (id_right,id_user) VALUES ('RELEASER_PLATFORM',1);

--
-- RBAC role of the platform release : the screens require the manage permission on the campaigns, even for a super administrator
--
DELETE FROM core_user_role WHERE role_key = 'releaser_platform_manager';
DELETE FROM core_admin_role_resource WHERE role_key = 'releaser_platform_manager';
DELETE FROM core_admin_role WHERE role_key = 'releaser_platform_manager';
INSERT INTO core_admin_role (role_key,role_description) VALUES ('releaser_platform_manager','Lutece platform release : manage the campaigns');
INSERT INTO core_admin_role_resource (role_key,resource_type,resource_id,permission) VALUES ('releaser_platform_manager','platform_release','*','managePlatformReleasePermission');
INSERT INTO core_user_role (role_key,id_user) VALUES ('releaser_platform_manager',1);


