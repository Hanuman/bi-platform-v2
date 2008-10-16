--
-- note: this script assumes pg_hba.conf is configured correctly
--

-- \connect postgres postgres

drop database if exists hibernate;
drop user if exists hibuser;

CREATE USER hibuser PASSWORD 'password';

CREATE DATABASE hibernate WITH OWNER = hibuser ENCODING = 'UTF8' TABLESPACE = pg_default;

GRANT ALL PRIVILEGES ON DATABASE hibernate to hibuser;

\connect hibernate hibuser

begin;

DROP TABLE IF EXISTS DATASOURCE;

CREATE TABLE DATASOURCE(NAME VARCHAR(50) NOT NULL PRIMARY KEY,MAXACTCONN INTEGER NOT NULL,DRIVERCLASS VARCHAR(50) NOT NULL,IDLECONN INTEGER NOT NULL,USERNAME VARCHAR(50) NOT NULL,PASSWORD VARCHAR(150) NOT NULL,URL VARCHAR(100) NOT NULL,QUERY VARCHAR(100) NOT NULL,WAIT INTEGER NOT NULL);

-- Fixes a problem where the chronstring was not allowing nulls and couldn't be fixed by editing the mapping and reving the class
ALTER TABLE PRO_SCHEDULE MODIFY CRONSTRING VARCHAR(256) NULL;
commit;