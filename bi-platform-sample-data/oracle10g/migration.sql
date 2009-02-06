set escape on;

conn hibuser/password;

-- Fixes a problem where the chronstring was not allowing nulls and couldn't be fixed by editing the mapping and reving the class
ALTER TABLE PRO_SCHEDULE MODIFY CRONSTRING VARCHAR(256) NULL;
ALTER TABLE DATASOURCE MODIFY USERNAME VARCHAR(50) NULL;
ALTER TABLE DATASOURCE MODIFY PASSWORD VARCHAR(150) NULL;
ALTER TABLE DATASOURCE MODIFY QUERY VARCHAR(100) NULL;
commit;