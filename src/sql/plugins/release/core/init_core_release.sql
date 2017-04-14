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


