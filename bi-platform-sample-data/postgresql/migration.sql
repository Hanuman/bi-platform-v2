\connect hibernate hibuser

begin;

-- Fixes a problem where the chronstring was not allowing nulls and couldn't be fixed by editing the mapping and reving the class
ALTER TABLE PRO_SCHEDULE ALTER COLUMN CRONSTRING DROP NOT NULL;
ALTER TABLE DATASOURCE ALTER COLUMN USERNAME DROP NOT NULL;
ALTER TABLE DATASOURCE ALTER COLUMN PASSWORD DROP NOT NULL;
ALTER TABLE DATASOURCE ALTER COLUMN QUERY DROP NOT NULL;
commit;