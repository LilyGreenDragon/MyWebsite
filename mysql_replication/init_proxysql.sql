INSERT INTO mysql_users (username, password, active, default_hostgroup)
VALUES ('remote_admin', 'remote_pass', 1, 0);

UPDATE global_variables
SET variable_value = 'admin:admin_pass;remote_admin:remote_pass'
WHERE variable_name = 'admin-admin_credentials';

LOAD MYSQL USERS TO RUNTIME;
LOAD ADMIN VARIABLES TO RUNTIME;
SAVE MYSQL USERS TO DISK;
SAVE ADMIN VARIABLES TO DISK;