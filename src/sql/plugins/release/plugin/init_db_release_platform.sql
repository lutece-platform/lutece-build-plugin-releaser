--
-- Platform release feature : the 5 steps of a platform release and the SCM URL of their aggregate
--

DELETE FROM releaser_platform_step_definition;
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (1, 'GLOBAL_POM', 'Global POM', 'https://github.com/lutece-platform/tools-maven-global-pom.git');
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (2, 'SITE_POM', 'Site POM', 'https://github.com/lutece-platform/tools-maven-site-pom.git');
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (3, 'CORE', 'Lutece core', 'https://github.com/lutece-platform/lutece-core.git');
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (4, 'PLATFORM_PLUGINS', 'Platform plugins', 'https://github.com/lutece-platform/lutece-platform.git');
INSERT INTO releaser_platform_step_definition (step_number, code, name, scm_url) VALUES (5, 'PLATFORM_STARTERS', 'Platform starters and BOM', 'https://github.com/lutece-platform/lutece-platform.git');
