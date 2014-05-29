--
-- Type: USER; Name: TM_WZ
--
CREATE USER "TM_WZ" IDENTIFIED BY VALUES 'S:DA4EDB9C0E0DCCADB7B62185D4D3BFC6000ED28FA8A99F1AAD6BB5770299;4F6EDC12B7127966'
   DEFAULT TABLESPACE "TRANSMART"
   TEMPORARY TABLESPACE "TEMP";

--
-- Type: SYSTEM_GRANT; Name: TM_WZ
--
GRANT UNLIMITED TABLESPACE TO "TM_WZ";

--
-- Type: TABLESPACE_QUOTA; Name: TM_WZ
--
--
-- Type: ROLE_GRANT; Name: TM_WZ
--
GRANT "CONNECT" TO "TM_WZ";

GRANT "RESOURCE" TO "TM_WZ";

GRANT "DBA" TO "TM_WZ";

