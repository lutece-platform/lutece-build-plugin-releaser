--
-- Upgrade 1.4.1 -> 1.4.2
--
-- The unused site jira_key column is removed
--

ALTER TABLE releaser_site DROP COLUMN jira_key;
