USE hibernate;
-- Fixes a problem where the chronstring was not allowing nulls and couldn't be fixed by editing the mapping and reving the class
ALTER TABLE PRO_SCHEDULE MODIFY CRONSTRING VARCHAR(256) NULL;
commit;